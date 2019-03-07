/*
    Structorizer
    A little tool which you can use to create Nassi-Schneiderman Diagrams (NSD)

    Copyright (C) 2009  Bob Fisch
    Copyright (C) 2017  StructorizerParserTemplate.pgt: Kay Gürtzig
    Copyright (C) 2017-2018  COBOLParser: Simon Sobisch, Kay Gürtzig

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
 *      Author:         Simon Sobisch
 *
 *      Description:    Class to parse a COBOL file and build structograms from the reduction tree.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017-03-10      First Issue (automatically generated with GOLDprog.exe,
 *                                      constants for COBOL-85.grm downloaded from URL
 *                                      http://www.goldparser.org/grammars/files/COBOL-85.zip)
 *      Simon Sobisch   2017-03-22      COBOL preparser for **valid** COBOL sources: reference-format
 *                                      free-form and fixed-form (with continuation + debugging lines),
 *                                      minimal subset of compiler directives, passes NIST EXEC85.cob
 *                                      and DB201A.CBL (with manual source changes because of
 *                                      insufficient COBOL-85.grm)
 *      Kay Gürtzig     2017-03-26      Fix #384: New temp file mechanism for the prepared text file
 *      Simon Sobisch   2017-04-24      Moved from COBOL-85.grm (NOT being COBOL 85!!!) to GnuCOBOL.grm
 *      Kay Gürtzig     2017-05-07      ADD/SUBTRACT/MULTIPLY/DIVIDE with ROUNDED mode implemented, SET
 *                                      statement and string manipulations (ref mod) realized.
 *      Kay Gürtzig     2017-05-10      Further accomplishments for EVALUATE (ALSO included)
 *      Kay Gürtzig     2017-05-13      EVALUATE import as Case element fixed. FileAPI support begun
 *      Kay Gürtzig     2017-05-14      PERFORM VARYING with composed condition or defective step fixed
 *      Kay Gürtzig     2017-05-16      SET TO TRUE/FALSE import implemented
 *      Kay Gürtzig     2017-05-24      READ statement implemented, file var declarations added, ACCEPT enhanced
 *                                      STRING statement implemented (refined with help of enh. #413)
 *      Kay Gürtzig     2017-05-28      First rough approach to implement UNSTRING import and PERFOM &lt;procedure&gt;
 *      Kay Gürtzig     2017-06-06      Correction in importUnstring(...) w.r.t. ALL clause
 *      Simon Sobisch   2017-06-23      Added Classes CobTools (CobProg, CobVar) for COBOL-view of these
 *                                      structures. Will be extended when needed.
 *                                      Translation COBOL -> Java Types completely rewritten and validated
 *                                      SET var [var2] TO (TRUE | FALSE) with lookup of condition names implemented
 *                                      condition-names in expressions replaced (further improvements need work on expressions)
 *                                      new option "is32bit" for var types and for later care in preparser
 *                                      Optimization of getContent_R: use static Patterns and Matchers as
 *                                      this function is called very often
 *      Kay Gürtzig     2017-10-01      Enh. #420: Comment import mechanism built in and roughly configured
 *      Kay Gürtzig     2017-10-05      Enh. #423: Record type detection and declaration implemented
 *      Kay Gürtzig     2017-10-06      SEARCH statement implemented, approach to OCCURS ... INDEXED clause
 *      Kay Gürtzig     2017-10-08      Enh. #423: index placement in qualified names and conditions fixed
 *                                      Decisive improvements for SEARCH and SET statements
 *      Simon Sobisch   2017-10-10      Fixed numeric case items for alphanumeric variables in EVALUATE (TODO: needed for every expression)
 *                                      Fixed getContentToken_R to correctly replace SPACE/ZERO/NULL
 *      Kay Gürtzig     2017-10-19      Mechanism to use Sections and Paragraphs as data-sharing subroutines
 *                                      TODO: More sensible import for EXIT statements in sections, paragraphs
 *      Kay Gürtzig     2017-10-20      Function register introduced to tell array access from function calls,
 *                                      loop condition transformation
 *      Kay Gürtzig     2017-10-22      File status assignments added according to the proposal of Simon Sobisch
 *      Kay Gürtzig     2017-10-26      File maps moved to CobProg to avoid name clashes
 *      Kay Gürtzig     2017-10-31      Bugfix #445: Face empty sections / paragraphs on refactoring
 *      Simon Sobisch   2017-11-27      Some fixes for USAGE, SEARCH and EXIT
 *      Kay Gürtzig     2017-11-27      Bugfix #475: A paragraph starting just after the section header closed the section
 *      Kay Gürtzig     2017-12-01      Bugfix #480: Correct handling of level-77 data, initialization of arrays of records
 *      Kay Gürtzig     2017-12-04      Bugfix #475: Paragraph handling revised, bugfix #473 approach,
 *                                      issue #485 workaround (prefixing intrinsic functions with FUNCTION)
 *      Kay Gürtzig     2017-12-05      Bugfix #483: mild version of disabled optionImportVarDecl,
 *                                      Bugfix #486: Return mechanism in imported functions enforced
 *      Kay Gürtzig     2017-12-10      Issue #475: Calls to empty or corrupt COBOL procedures now disabled
 *      Simon Sobisch   2017-12-15      Issues #493, #494 (related to SEARCH statement variants) fixed.
 *      Kay Gürtzig     2018-04-04      Fixed an inconvenience on importing DISPLAY statements (display clauses, KGU#513)
 *      Kay Gürtzig     2018-07-01      Enh. #553 - thread cancellation hooks added
 *      Simon Sobisch   2018-10-24      Fix #626 Issues with parsing of string literals.
 *                                      Skip lines that look like preprocessor directives (starting with #).
 *      Simon Sobisch   2018-10-25      Auto-switch to free-format before preparsing if source looks preprocessed by cobc.
 *      Kay Gürtzig     2018-10-29      Issue #630 (exit attempt on REPLACE/COPY), bugfix #635: commas in expression lists
 *      Kay Gürtzig     2018-12-14      Issue #631 - removal of ';' and ',', first preparations for INSPECT import
 *      Kay Gürtzig     2018-12-17      Issue #631 - Implementation for all three flavours of INSPECT statement
 *      Kay Gürtzig     2019-01-18      Bugfix #665 (related to #631) parsing of the resource diagrams had failed.
 *      Kay Gürtzig     2019-03-04/07   Issue #407: Condition heuristics extended to cop with some expressions of kind "a = 5 or 9"
 *      Kay Gürtzig     2019-03-04      Bugfix #695: Arrays of basic types (e.g. Strings) haven't been imported properly
 *      Kay Gürtzig     2019-03-05      Bugfix #631 (update): commas in pic clauses (e.g. 01 test pic z,zzz,zz9.) now preserved
 *
 ******************************************************************************************************
 *
 *     Comment:
 *     Licensed Material - Property of Ralph Iden (GOLDParser) and Mathew Hawkins (parts of the template)
 *     GOLDParser - code downloaded from https://github.com/ridencww/goldengine on 2017-03-05.<br>
 *     Modifications to this code are allowed as it is a helper class to use the engine.<br>
 *     Template File:  StructorizerParserTemplate.pgt (with elements of both<br>
 *                     Java-MatthewHawkins.pgt and Java-IdenEngine.pgt)<br>
 *     Authors:        Ralph Iden, Matthew Hawkins, Bob Fisch, Kay Gürtzig, Simon Sobisch<br>
 *
 *     Note:
 *     Process the grammar to get a ".skel" file to merge changes in the grammar:
 *     GOLDbuild.exe GnuCOBOL.grm && GOLDprog.exe GnuCOBOL.egt StructorizerParserTemplate.pgt COBOLParser.java.skel && dos2unix COBOLParser.java.skel
 *
 *     Language-specific options:
 *     - debugLines: boolean, default = false;
 *     - decimalComma: boolean, default = false;
 *     - fixedForm: boolean, default = true;
 *     - fixedColumnIndicator: integer, default = 7;
 *     - fixedColumnText: integer, default = 73;
 *     - ignoreUnstringAll: boolean, default = true;
 *     - is32bit, default = true;
 *
 ******************************************************************************************************/

import java.awt.Color;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.creativewidgetworks.goldparser.engine.Reduction;
import com.creativewidgetworks.goldparser.engine.Token;
import com.creativewidgetworks.goldparser.engine.enums.SymbolType;

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
import lu.fisch.structorizer.elements.Repeat;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.structorizer.elements.TypeMapEntry;
import lu.fisch.structorizer.elements.While;
import lu.fisch.structorizer.parsers.CobTools.CobProg;
import lu.fisch.structorizer.parsers.CobTools.CobVar;
import lu.fisch.structorizer.parsers.CobTools.Usage;
//import lu.fisch.structorizer.parsers.CodeParser.FilePreparationException;
import lu.fisch.utils.BString;
import lu.fisch.utils.StringList;

/**
 * Code import parser class of Structorizer 3.27, based on GOLDParser 5.0 for the GnuCOBOL language.
 * This file contains grammar-specific constants and individual routines to build
 * structograms (Nassi-Shneiderman diagrams) from the parsing tree.
 * @author Kay Gürtzig
 */
public class COBOLParser extends CodeParser
{

	//---------------------- Grammar specification ---------------------------

	@Override
	protected final String getCompiledGrammar()
	{
		return "GnuCOBOL.egt";
	}

	@Override
	protected final String getGrammarTableName()
	{
		return "GnuCOBOL";
	}

	/**
	 * If this flag is set then program names derived from file name will be made uppercase
	 * This default will be initialized in consistency with the Analyser check
	 */
	private boolean optionUpperCaseProgName = false;

	//------------------------------ Constructor -----------------------------

	/**
	 * Constructs a parser for language GnuCOBOL, loads the grammar as resource and
	 * specifies whether to generate the parse tree as string
	 */
	public COBOLParser() {
	}

	//---------------------- File Filter configuration -----------------------

	@Override
	public String getDialogTitle() {
		return "COBOL";
	}

	@Override
	protected String getFileDescription() {
		return "COBOL Source Files";
	}

 	@Override
	public String[] getFileExtensions() {
		final String[] exts = { "COB", "CBL", "CPY" };
		return exts;
	}

	//------------------- Comment delimiter specification ---------------------------------

	// START KGU#407 2017-09-30: Enh. #420
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.parsers.CodeParser#getCommentDelimiters()
	 */
	@Override
	protected String[][] getCommentDelimiters()
	{
		return new String[][]{
			{"*>"}
		};
	}
	// END KGU#407 2017-09-30

	//---------------------- Grammar table constants DON'T MODIFY! ---------------------------

	// Symbolic constants naming the table indices of the symbols of the grammar
	//@SuppressWarnings("unused")
//	private interface SymbolConstants
//	{
//		final int SYM_EOF                                        =    0;  // (EOF)
//		final int SYM_ERROR                                      =    1;  // (Error)
//		final int SYM_COMMENT                                    =    2;  // Comment
//		final int SYM_NEWLINE                                    =    3;  // NewLine
//		final int SYM_WHITESPACE                                 =    4;  // Whitespace
//		final int SYM_TIMESGT                                    =    5;  // '*>'
//		final int SYM_ABS                                        =    6;  // ABS
//		final int SYM_ACCEPT                                     =    7;  // ACCEPT
//		final int SYM_ACCESS                                     =    8;  // ACCESS
//		final int SYM_ACOS                                       =    9;  // ACOS
//		final int SYM_ACUBINNUMLITERAL                           =   10;  // AcuBinNumLiteral
//		final int SYM_ACUHEXNUMLITERAL                           =   11;  // AcuHexNumLiteral
//		final int SYM_ACUOCTNUMLITERAL                           =   12;  // AcuOctNumLiteral
//		final int SYM_ADD                                        =   13;  // ADD
//		final int SYM_ADDRESS                                    =   14;  // ADDRESS
//		final int SYM_ADVANCING                                  =   15;  // ADVANCING
//		final int SYM_AFTER                                      =   16;  // AFTER
//		final int SYM_ALL                                        =   17;  // ALL
//		final int SYM_ALLOCATE                                   =   18;  // ALLOCATE
//		final int SYM_ALPHABET                                   =   19;  // ALPHABET
//		final int SYM_ALPHABETIC                                 =   20;  // ALPHABETIC
//		final int SYM_ALPHABETIC_LOWER                           =   21;  // 'ALPHABETIC_LOWER'
//		final int SYM_ALPHABETIC_UPPER                           =   22;  // 'ALPHABETIC_UPPER'
//		final int SYM_ALPHANUMERIC                               =   23;  // ALPHANUMERIC
//		final int SYM_ALPHANUMERIC_EDITED                        =   24;  // 'ALPHANUMERIC_EDITED'
//		final int SYM_ALSO                                       =   25;  // ALSO
//		final int SYM_ALTER                                      =   26;  // ALTER
//		final int SYM_ALTERNATE                                  =   27;  // ALTERNATE
//		final int SYM_AND                                        =   28;  // AND
//		final int SYM_ANNUITY                                    =   29;  // ANNUITY
//		final int SYM_ANY                                        =   30;  // ANY
//		final int SYM_ARE                                        =   31;  // ARE
//		final int SYM_AREA                                       =   32;  // AREA
//		final int SYM_AREAS                                      =   33;  // AREAS
//		final int SYM_ARGUMENT_NUMBER                            =   34;  // 'ARGUMENT_NUMBER'
//		final int SYM_ARGUMENT_VALUE                             =   35;  // 'ARGUMENT_VALUE'
//		final int SYM_AS                                         =   36;  // AS
//		final int SYM_ASCENDING                                  =   37;  // ASCENDING
//		final int SYM_ASCII                                      =   38;  // ASCII
//		final int SYM_ASIN                                       =   39;  // ASIN
//		final int SYM_ASSIGN                                     =   40;  // ASSIGN
//		final int SYM_AT                                         =   41;  // AT
//		final int SYM_ATAN                                       =   42;  // ATAN
//		final int SYM_ATTRIBUTE                                  =   43;  // ATTRIBUTE
//		final int SYM_AUTHOR                                     =   44;  // AUTHOR
//		final int SYM_AUTO                                       =   45;  // AUTO
//		final int SYM_AUTOMATIC                                  =   46;  // AUTOMATIC
//		final int SYM_AWAY_FROM_ZERO                             =   47;  // 'AWAY_FROM_ZERO'
//		final int SYM_BACKGROUND_COLOR                           =   48;  // 'BACKGROUND_COLOR'
//		final int SYM_BASED                                      =   49;  // BASED
//		final int SYM_BEFORE                                     =   50;  // BEFORE
//		final int SYM_BELL                                       =   51;  // BELL
//		final int SYM_BINARY                                     =   52;  // BINARY
//		final int SYM_BINARY_CHAR                                =   53;  // 'BINARY_CHAR'
//		final int SYM_BINARY_C_LONG                              =   54;  // 'BINARY_C_LONG'
//		final int SYM_BINARY_DOUBLE                              =   55;  // 'BINARY_DOUBLE'
//		final int SYM_BINARY_LONG                                =   56;  // 'BINARY_LONG'
//		final int SYM_BINARY_SHORT                               =   57;  // 'BINARY_SHORT'
//		final int SYM_BLANK                                      =   58;  // BLANK
//		final int SYM_BLINK                                      =   59;  // BLINK
//		final int SYM_BLOCK                                      =   60;  // BLOCK
//		final int SYM_BOOLEANHEXLITERAL                          =   61;  // BooleanHexLiteral
//		final int SYM_BOOLEANLITERAL                             =   62;  // BooleanLiteral
//		final int SYM_BOOLEAN_OF_INTEGER                         =   63;  // 'BOOLEAN_OF_INTEGER'
//		final int SYM_BOTTOM                                     =   64;  // BOTTOM
//		final int SYM_BY                                         =   65;  // BY
//		final int SYM_BYTE_LENGTH                                =   66;  // 'BYTE_LENGTH'
//		final int SYM_CALL                                       =   67;  // CALL
//		final int SYM_CANCEL                                     =   68;  // CANCEL
//		final int SYM_CAPACITY                                   =   69;  // CAPACITY
//		final int SYM_CARD_PUNCH                                 =   70;  // 'CARD_PUNCH'
//		final int SYM_CARD_READER                                =   71;  // 'CARD_READER'
//		final int SYM_CASSETTE                                   =   72;  // CASSETTE
//		final int SYM_CD                                         =   73;  // CD
//		final int SYM_CF                                         =   74;  // CF
//		final int SYM_CH                                         =   75;  // CH
//		final int SYM_CHAINING                                   =   76;  // CHAINING
//		final int SYM_CHAR                                       =   77;  // CHAR
//		final int SYM_CHARACTER                                  =   78;  // CHARACTER
//		final int SYM_CHARACTERS                                 =   79;  // CHARACTERS
//		final int SYM_CHAR_NATIONAL                              =   80;  // 'CHAR_NATIONAL'
//		final int SYM_CLASS                                      =   81;  // CLASS
//		final int SYM_CLASSIFICATION                             =   82;  // CLASSIFICATION
//		final int SYM_CLOSE                                      =   83;  // CLOSE
//		final int SYM_COBOL                                      =   84;  // COBOL
//		final int SYM_COBOLWORD                                  =   85;  // COBOLWord
//		final int SYM_CODE                                       =   86;  // CODE
//		final int SYM_CODE_SET                                   =   87;  // 'CODE_SET'
//		final int SYM_COL                                        =   88;  // COL
//		final int SYM_COLLATING                                  =   89;  // COLLATING
//		final int SYM_COLS                                       =   90;  // COLS
//		final int SYM_COLUMN                                     =   91;  // COLUMN
//		final int SYM_COLUMNS                                    =   92;  // COLUMNS
//		final int SYM_COMBINED_DATETIME                          =   93;  // 'COMBINED_DATETIME'
//		final int SYM_COMMA                                      =   94;  // COMMA
//		final int SYM_COMMAND_LINE                               =   95;  // 'COMMAND_LINE'
//		final int SYM_COMMA_DELIM                                =   96;  // 'COMMA_DELIM'
//		final int SYM_COMMIT                                     =   97;  // COMMIT
//		final int SYM_COMMON                                     =   98;  // COMMON
//		final int SYM_COMMUNICATION                              =   99;  // COMMUNICATION
//		final int SYM_COMP                                       =  100;  // COMP
//		final int SYM_COMPUTE                                    =  101;  // COMPUTE
//		final int SYM_COMP_1                                     =  102;  // 'COMP_1'
//		final int SYM_COMP_2                                     =  103;  // 'COMP_2'
//		final int SYM_COMP_3                                     =  104;  // 'COMP_3'
//		final int SYM_COMP_4                                     =  105;  // 'COMP_4'
//		final int SYM_COMP_5                                     =  106;  // 'COMP_5'
//		final int SYM_COMP_6                                     =  107;  // 'COMP_6'
//		final int SYM_COMP_X                                     =  108;  // 'COMP_X'
//		final int SYM_CONCATENATE                                =  109;  // CONCATENATE
//		final int SYM_CONFIGURATION                              =  110;  // CONFIGURATION
//		final int SYM_CONSTANT                                   =  111;  // CONSTANT
//		final int SYM_CONTAINS                                   =  112;  // CONTAINS
//		final int SYM_CONTENT                                    =  113;  // CONTENT
//		final int SYM_CONTINUE                                   =  114;  // CONTINUE
//		final int SYM_CONTROL                                    =  115;  // CONTROL
//		final int SYM_CONTROLS                                   =  116;  // CONTROLS
//		final int SYM_CONVERSION                                 =  117;  // CONVERSION
//		final int SYM_CONVERTING                                 =  118;  // CONVERTING
//		final int SYM_CORRESPONDING                              =  119;  // CORRESPONDING
//		final int SYM_COS                                        =  120;  // COS
//		final int SYM_COUNT                                      =  121;  // COUNT
//		final int SYM_CRT                                        =  122;  // CRT
//		final int SYM_CRT_UNDER                                  =  123;  // 'CRT_UNDER'
//		final int SYM_CURRENCY                                   =  124;  // CURRENCY
//		final int SYM_CURRENCY_SYMBOL                            =  125;  // 'CURRENCY_SYMBOL'
//		final int SYM_CURRENT_DATE                               =  126;  // 'CURRENT_DATE'
//		final int SYM_CURSOR                                     =  127;  // CURSOR
//		final int SYM_CYCLE                                      =  128;  // CYCLE
//		final int SYM_DATA                                       =  129;  // DATA
//		final int SYM_DATE                                       =  130;  // DATE
//		final int SYM_DATE_COMPILED                              =  131;  // 'DATE_COMPILED'
//		final int SYM_DATE_OF_INTEGER                            =  132;  // 'DATE_OF_INTEGER'
//		final int SYM_DATE_TO_YYYYMMDD                           =  133;  // 'DATE_TO_YYYYMMDD'
//		final int SYM_DATE_WRITTEN                               =  134;  // 'DATE_WRITTEN'
//		final int SYM_DAY                                        =  135;  // DAY
//		final int SYM_DAY_OF_INTEGER                             =  136;  // 'DAY_OF_INTEGER'
//		final int SYM_DAY_OF_WEEK                                =  137;  // 'DAY_OF_WEEK'
//		final int SYM_DAY_TO_YYYYDDD                             =  138;  // 'DAY_TO_YYYYDDD'
//		final int SYM_DE                                         =  139;  // DE
//		final int SYM_DEBUGGING                                  =  140;  // DEBUGGING
//		final int SYM_DECIMALLITERAL                             =  141;  // DecimalLiteral
//		final int SYM_DECIMAL_POINT                              =  142;  // 'DECIMAL_POINT'
//		final int SYM_DECLARATIVES                               =  143;  // DECLARATIVES
//		final int SYM_DEFAULT                                    =  144;  // DEFAULT
//		final int SYM_DELETE                                     =  145;  // DELETE
//		final int SYM_DELIMITED                                  =  146;  // DELIMITED
//		final int SYM_DELIMITER                                  =  147;  // DELIMITER
//		final int SYM_DEPENDING                                  =  148;  // DEPENDING
//		final int SYM_DESCENDING                                 =  149;  // DESCENDING
//		final int SYM_DESTINATION                                =  150;  // DESTINATION
//		final int SYM_DETAIL                                     =  151;  // DETAIL
//		final int SYM_DISABLE                                    =  152;  // DISABLE
//		final int SYM_DISC                                       =  153;  // DISC
//		final int SYM_DISK                                       =  154;  // DISK
//		final int SYM_DISPLAY                                    =  155;  // DISPLAY
//		final int SYM_DISPLAY_OF                                 =  156;  // 'DISPLAY_OF'
//		final int SYM_DIVIDE                                     =  157;  // DIVIDE
//		final int SYM_DIVISION                                   =  158;  // DIVISION
//		final int SYM_DOWN                                       =  159;  // DOWN
//		final int SYM_DUPLICATES                                 =  160;  // DUPLICATES
//		final int SYM_DYNAMIC                                    =  161;  // DYNAMIC
//		final int SYM_E                                          =  162;  // E
//		final int SYM_EBCDIC                                     =  163;  // EBCDIC
//		final int SYM_EC                                         =  164;  // EC
//		final int SYM_ECHO                                       =  165;  // ECHO
//		final int SYM_EGI                                        =  166;  // EGI
//		final int SYM_EIGHTY_EIGHT                               =  167;  // 'EIGHTY_EIGHT'
//		final int SYM_ELSE                                       =  168;  // ELSE
//		final int SYM_EMI                                        =  169;  // EMI
//		final int SYM_ENABLE                                     =  170;  // ENABLE
//		final int SYM_END                                        =  171;  // END
//		final int SYM_END_ACCEPT                                 =  172;  // 'END_ACCEPT'
//		final int SYM_END_ADD                                    =  173;  // 'END_ADD'
//		final int SYM_END_CALL                                   =  174;  // 'END_CALL'
//		final int SYM_END_COMPUTE                                =  175;  // 'END_COMPUTE'
//		final int SYM_END_DELETE                                 =  176;  // 'END_DELETE'
//		final int SYM_END_DISPLAY                                =  177;  // 'END_DISPLAY'
//		final int SYM_END_DIVIDE                                 =  178;  // 'END_DIVIDE'
//		final int SYM_END_EVALUATE                               =  179;  // 'END_EVALUATE'
//		final int SYM_END_FUNCTION                               =  180;  // 'END_FUNCTION'
//		final int SYM_END_IF                                     =  181;  // 'END_IF'
//		final int SYM_END_MULTIPLY                               =  182;  // 'END_MULTIPLY'
//		final int SYM_END_PERFORM                                =  183;  // 'END_PERFORM'
//		final int SYM_END_PROGRAM                                =  184;  // 'END_PROGRAM'
//		final int SYM_END_READ                                   =  185;  // 'END_READ'
//		final int SYM_END_RECEIVE                                =  186;  // 'END_RECEIVE'
//		final int SYM_END_RETURN                                 =  187;  // 'END_RETURN'
//		final int SYM_END_REWRITE                                =  188;  // 'END_REWRITE'
//		final int SYM_END_SEARCH                                 =  189;  // 'END_SEARCH'
//		final int SYM_END_START                                  =  190;  // 'END_START'
//		final int SYM_END_STRING                                 =  191;  // 'END_STRING'
//		final int SYM_END_SUBTRACT                               =  192;  // 'END_SUBTRACT'
//		final int SYM_END_UNSTRING                               =  193;  // 'END_UNSTRING'
//		final int SYM_END_WRITE                                  =  194;  // 'END_WRITE'
//		final int SYM_ENTRY                                      =  195;  // ENTRY
//		final int SYM_ENTRY_CONVENTION                           =  196;  // 'ENTRY_CONVENTION'
//		final int SYM_ENVIRONMENT                                =  197;  // ENVIRONMENT
//		final int SYM_ENVIRONMENT_VALUE                          =  198;  // 'ENVIRONMENT_VALUE'
//		final int SYM_EOL                                        =  199;  // EOL
//		final int SYM_EOP                                        =  200;  // EOP
//		final int SYM_EOS                                        =  201;  // EOS
//		final int SYM_EQUAL                                      =  202;  // EQUAL
//		final int SYM_ERASE                                      =  203;  // ERASE
//		final int SYM_ERROR2                                     =  204;  // ERROR
//		final int SYM_ESCAPE                                     =  205;  // ESCAPE
//		final int SYM_ESI                                        =  206;  // ESI
//		final int SYM_EVALUATE                                   =  207;  // EVALUATE
//		final int SYM_EVENT_STATUS                               =  208;  // 'EVENT_STATUS'
//		final int SYM_EXCEPTION                                  =  209;  // EXCEPTION
//		final int SYM_EXCEPTION_CONDITION                        =  210;  // 'EXCEPTION_CONDITION'
//		final int SYM_EXCEPTION_FILE                             =  211;  // 'EXCEPTION_FILE'
//		final int SYM_EXCEPTION_FILE_N                           =  212;  // 'EXCEPTION_FILE_N'
//		final int SYM_EXCEPTION_LOCATION                         =  213;  // 'EXCEPTION_LOCATION'
//		final int SYM_EXCEPTION_LOCATION_N                       =  214;  // 'EXCEPTION_LOCATION_N'
//		final int SYM_EXCEPTION_STATEMENT                        =  215;  // 'EXCEPTION_STATEMENT'
//		final int SYM_EXCEPTION_STATUS                           =  216;  // 'EXCEPTION_STATUS'
//		final int SYM_EXCLUSIVE                                  =  217;  // EXCLUSIVE
//		final int SYM_EXIT                                       =  218;  // EXIT
//		final int SYM_EXP                                        =  219;  // EXP
//		final int SYM_EXPONENTIATION                             =  220;  // EXPONENTIATION
//		final int SYM_EXTEND                                     =  221;  // EXTEND
//		final int SYM_EXTERNAL                                   =  222;  // EXTERNAL
//		final int SYM_F                                          =  223;  // F
//		final int SYM_FACTORIAL                                  =  224;  // FACTORIAL
//		final int SYM_FD                                         =  225;  // FD
//		final int SYM_FILE_CONTROL                               =  226;  // 'FILE_CONTROL'
//		final int SYM_FILE_ID                                    =  227;  // 'FILE_ID'
//		final int SYM_FILLER                                     =  228;  // FILLER
//		final int SYM_FINAL                                      =  229;  // FINAL
//		final int SYM_FIRST                                      =  230;  // FIRST
//		final int SYM_FIXED                                      =  231;  // FIXED
//		final int SYM_FLOATLITERAL                               =  232;  // FloatLiteral
//		final int SYM_FLOAT_BINARY_128                           =  233;  // 'FLOAT_BINARY_128'
//		final int SYM_FLOAT_BINARY_32                            =  234;  // 'FLOAT_BINARY_32'
//		final int SYM_FLOAT_BINARY_64                            =  235;  // 'FLOAT_BINARY_64'
//		final int SYM_FLOAT_DECIMAL_16                           =  236;  // 'FLOAT_DECIMAL_16'
//		final int SYM_FLOAT_DECIMAL_34                           =  237;  // 'FLOAT_DECIMAL_34'
//		final int SYM_FLOAT_DECIMAL_7                            =  238;  // 'FLOAT_DECIMAL_7'
//		final int SYM_FLOAT_EXTENDED                             =  239;  // 'FLOAT_EXTENDED'
//		final int SYM_FLOAT_LONG                                 =  240;  // 'FLOAT_LONG'
//		final int SYM_FLOAT_SHORT                                =  241;  // 'FLOAT_SHORT'
//		final int SYM_FOOTING                                    =  242;  // FOOTING
//		final int SYM_FOR                                        =  243;  // FOR
//		final int SYM_FOREGROUND_COLOR                           =  244;  // 'FOREGROUND_COLOR'
//		final int SYM_FOREVER                                    =  245;  // FOREVER
//		final int SYM_FORMATTED_CURRENT_DATE                     =  246;  // 'FORMATTED_CURRENT_DATE'
//		final int SYM_FORMATTED_DATE                             =  247;  // 'FORMATTED_DATE'
//		final int SYM_FORMATTED_DATETIME                         =  248;  // 'FORMATTED_DATETIME'
//		final int SYM_FORMATTED_TIME                             =  249;  // 'FORMATTED_TIME'
//		final int SYM_FRACTION_PART                              =  250;  // 'FRACTION_PART'
//		final int SYM_FREE                                       =  251;  // FREE
//		final int SYM_FROM                                       =  252;  // FROM
//		final int SYM_FROM_CRT                                   =  253;  // 'FROM_CRT'
//		final int SYM_FULL                                       =  254;  // FULL
//		final int SYM_FUNCTION                                   =  255;  // FUNCTION
//		final int SYM_FUNCTION_ID                                =  256;  // 'FUNCTION_ID'
//		final int SYM_GENERATE                                   =  257;  // GENERATE
//		final int SYM_GIVING                                     =  258;  // GIVING
//		final int SYM_GLOBAL                                     =  259;  // GLOBAL
//		final int SYM_GO                                         =  260;  // GO
//		final int SYM_GOBACK                                     =  261;  // GOBACK
//		final int SYM_GREATER                                    =  262;  // GREATER
//		final int SYM_GREATER_OR_EQUAL                           =  263;  // 'GREATER_OR_EQUAL'
//		final int SYM_GRID                                       =  264;  // GRID
//		final int SYM_GROUP                                      =  265;  // GROUP
//		final int SYM_HEADING                                    =  266;  // HEADING
//		final int SYM_HEXLITERAL                                 =  267;  // HexLiteral
//		final int SYM_HIGHEST_ALGEBRAIC                          =  268;  // 'HIGHEST_ALGEBRAIC'
//		final int SYM_HIGHLIGHT                                  =  269;  // HIGHLIGHT
//		final int SYM_HIGH_VALUE                                 =  270;  // 'HIGH_VALUE'
//		final int SYM_ID                                         =  271;  // ID
//		final int SYM_IDENTIFICATION                             =  272;  // IDENTIFICATION
//		final int SYM_IF                                         =  273;  // IF
//		final int SYM_IGNORE                                     =  274;  // IGNORE
//		final int SYM_IGNORING                                   =  275;  // IGNORING
//		final int SYM_IN                                         =  276;  // IN
//		final int SYM_INDEX                                      =  277;  // INDEX
//		final int SYM_INDEXED                                    =  278;  // INDEXED
//		final int SYM_INDICATE                                   =  279;  // INDICATE
//		final int SYM_INITIALIZE                                 =  280;  // INITIALIZE
//		final int SYM_INITIALIZED                                =  281;  // INITIALIZED
//		final int SYM_INITIATE                                   =  282;  // INITIATE
//		final int SYM_INPUT                                      =  283;  // INPUT
//		final int SYM_INPUT_OUTPUT                               =  284;  // 'INPUT_OUTPUT'
//		final int SYM_INSPECT                                    =  285;  // INSPECT
//		final int SYM_INSTALLATION                               =  286;  // INSTALLATION
//		final int SYM_INTEGER                                    =  287;  // INTEGER
//		final int SYM_INTEGER_OF_BOOLEAN                         =  288;  // 'INTEGER_OF_BOOLEAN'
//		final int SYM_INTEGER_OF_DATE                            =  289;  // 'INTEGER_OF_DATE'
//		final int SYM_INTEGER_OF_DAY                             =  290;  // 'INTEGER_OF_DAY'
//		final int SYM_INTEGER_OF_FORMATTED_DATE                  =  291;  // 'INTEGER_OF_FORMATTED_DATE'
//		final int SYM_INTEGER_PART                               =  292;  // 'INTEGER_PART'
//		final int SYM_INTERMEDIATE                               =  293;  // INTERMEDIATE
//		final int SYM_INTLITERAL                                 =  294;  // IntLiteral
//		final int SYM_INTO                                       =  295;  // INTO
//		final int SYM_INTRINSIC                                  =  296;  // INTRINSIC
//		final int SYM_INVALID_KEY                                =  297;  // 'INVALID_KEY'
//		final int SYM_IS                                         =  298;  // IS
//		final int SYM_I_O                                        =  299;  // 'I_O'
//		final int SYM_I_O_CONTROL                                =  300;  // 'I_O_CONTROL'
//		final int SYM_JUSTIFIED                                  =  301;  // JUSTIFIED
//		final int SYM_KEPT                                       =  302;  // KEPT
//		final int SYM_KEY                                        =  303;  // KEY
//		final int SYM_KEYBOARD                                   =  304;  // KEYBOARD
//		final int SYM_LABEL                                      =  305;  // LABEL
//		final int SYM_LAST                                       =  306;  // LAST
//		final int SYM_LEADING                                    =  307;  // LEADING
//		final int SYM_LEFT                                       =  308;  // LEFT
//		final int SYM_LEFTLINE                                   =  309;  // LEFTLINE
//		final int SYM_LENGTH                                     =  310;  // LENGTH
//		final int SYM_LENGTH_OF                                  =  311;  // 'LENGTH_OF'
//		final int SYM_LESS                                       =  312;  // LESS
//		final int SYM_LESS_OR_EQUAL                              =  313;  // 'LESS_OR_EQUAL'
//		final int SYM_LIMIT                                      =  314;  // LIMIT
//		final int SYM_LIMITS                                     =  315;  // LIMITS
//		final int SYM_LINAGE                                     =  316;  // LINAGE
//		final int SYM_LINAGE_COUNTER                             =  317;  // 'LINAGE_COUNTER'
//		final int SYM_LINE                                       =  318;  // LINE
//		final int SYM_LINES                                      =  319;  // LINES
//		final int SYM_LINE_COUNTER                               =  320;  // 'LINE_COUNTER'
//		final int SYM_LINKAGE                                    =  321;  // LINKAGE
//		final int SYM_LOCALE                                     =  322;  // LOCALE
//		final int SYM_LOCALE_COMPARE                             =  323;  // 'LOCALE_COMPARE'
//		final int SYM_LOCALE_DATE                                =  324;  // 'LOCALE_DATE'
//		final int SYM_LOCALE_TIME                                =  325;  // 'LOCALE_TIME'
//		final int SYM_LOCALE_TIME_FROM_SECONDS                   =  326;  // 'LOCALE_TIME_FROM_SECONDS'
//		final int SYM_LOCAL_STORAGE                              =  327;  // 'LOCAL_STORAGE'
//		final int SYM_LOCK                                       =  328;  // LOCK
//		final int SYM_LOG                                        =  329;  // LOG
//		final int SYM_LOWER                                      =  330;  // LOWER
//		final int SYM_LOWER_CASE                                 =  331;  // 'LOWER_CASE'
//		final int SYM_LOWEST_ALGEBRAIC                           =  332;  // 'LOWEST_ALGEBRAIC'
//		final int SYM_LOWLIGHT                                   =  333;  // LOWLIGHT
//		final int SYM_LOW_VALUE                                  =  334;  // 'LOW_VALUE'
//		final int SYM_MAGNETIC_TAPE                              =  335;  // 'MAGNETIC_TAPE'
//		final int SYM_MANUAL                                     =  336;  // MANUAL
//		final int SYM_MAX                                        =  337;  // MAX
//		final int SYM_MEAN                                       =  338;  // MEAN
//		final int SYM_MEDIAN                                     =  339;  // MEDIAN
//		final int SYM_MEMORY                                     =  340;  // MEMORY
//		final int SYM_MERGE                                      =  341;  // MERGE
//		final int SYM_MESSAGE                                    =  342;  // MESSAGE
//		final int SYM_MIDRANGE                                   =  343;  // MIDRANGE
//		final int SYM_MIN                                        =  344;  // MIN
//		final int SYM_MINUS                                      =  345;  // MINUS
//		final int SYM_MNEMONIC_NAME                              =  346;  // 'MNEMONIC_NAME'
//		final int SYM_MOD                                        =  347;  // MOD
//		final int SYM_MODE                                       =  348;  // MODE
//		final int SYM_MODULE_CALLER_ID                           =  349;  // 'MODULE_CALLER_ID'
//		final int SYM_MODULE_DATE                                =  350;  // 'MODULE_DATE'
//		final int SYM_MODULE_FORMATTED_DATE                      =  351;  // 'MODULE_FORMATTED_DATE'
//		final int SYM_MODULE_ID                                  =  352;  // 'MODULE_ID'
//		final int SYM_MODULE_PATH                                =  353;  // 'MODULE_PATH'
//		final int SYM_MODULE_SOURCE                              =  354;  // 'MODULE_SOURCE'
//		final int SYM_MODULE_TIME                                =  355;  // 'MODULE_TIME'
//		final int SYM_MONETARY_DECIMAL_POINT                     =  356;  // 'MONETARY_DECIMAL_POINT'
//		final int SYM_MONETARY_THOUSANDS_SEPARATOR               =  357;  // 'MONETARY_THOUSANDS_SEPARATOR'
//		final int SYM_MOVE                                       =  358;  // MOVE
//		final int SYM_MULTIPLE                                   =  359;  // MULTIPLE
//		final int SYM_MULTIPLY                                   =  360;  // MULTIPLY
//		final int SYM_NAME                                       =  361;  // NAME
//		final int SYM_NATIONAL                                   =  362;  // NATIONAL
//		final int SYM_NATIONALHEXLITERAL                         =  363;  // NationalHexLiteral
//		final int SYM_NATIONALLITERAL                            =  364;  // NationalLiteral
//		final int SYM_NATIONAL_EDITED                            =  365;  // 'NATIONAL_EDITED'
//		final int SYM_NATIONAL_OF                                =  366;  // 'NATIONAL_OF'
//		final int SYM_NATIVE                                     =  367;  // NATIVE
//		final int SYM_NEAREST_AWAY_FROM_ZERO                     =  368;  // 'NEAREST_AWAY_FROM_ZERO'
//		final int SYM_NEAREST_EVEN                               =  369;  // 'NEAREST_EVEN'
//		final int SYM_NEAREST_TOWARD_ZERO                        =  370;  // 'NEAREST_TOWARD_ZERO'
//		final int SYM_NEGATIVE                                   =  371;  // NEGATIVE
//		final int SYM_NESTED                                     =  372;  // NESTED
//		final int SYM_NEXT                                       =  373;  // NEXT
//		final int SYM_NEXT_PAGE                                  =  374;  // 'NEXT_PAGE'
//		final int SYM_NO                                         =  375;  // NO
//		final int SYM_NORMAL                                     =  376;  // NORMAL
//		final int SYM_NOT                                        =  377;  // NOT
//		final int SYM_NOTHING                                    =  378;  // NOTHING
//		final int SYM_NOT_END                                    =  379;  // 'NOT_END'
//		final int SYM_NOT_EOP                                    =  380;  // 'NOT_EOP'
//		final int SYM_NOT_EQUAL                                  =  381;  // 'NOT_EQUAL'
//		final int SYM_NOT_ESCAPE                                 =  382;  // 'NOT_ESCAPE'
//		final int SYM_NOT_EXCEPTION                              =  383;  // 'NOT_EXCEPTION'
//		final int SYM_NOT_INVALID_KEY                            =  384;  // 'NOT_INVALID_KEY'
//		final int SYM_NOT_OVERFLOW                               =  385;  // 'NOT_OVERFLOW'
//		final int SYM_NOT_SIZE_ERROR                             =  386;  // 'NOT_SIZE_ERROR'
//		final int SYM_NO_ADVANCING                               =  387;  // 'NO_ADVANCING'
//		final int SYM_NO_DATA                                    =  388;  // 'NO_DATA'
//		final int SYM_NO_ECHO                                    =  389;  // 'NO_ECHO'
//		final int SYM_NUMBER                                     =  390;  // NUMBER
//		final int SYM_NUMBERS                                    =  391;  // NUMBERS
//		final int SYM_NUMERIC                                    =  392;  // NUMERIC
//		final int SYM_NUMERIC_DECIMAL_POINT                      =  393;  // 'NUMERIC_DECIMAL_POINT'
//		final int SYM_NUMERIC_EDITED                             =  394;  // 'NUMERIC_EDITED'
//		final int SYM_NUMERIC_THOUSANDS_SEPARATOR                =  395;  // 'NUMERIC_THOUSANDS_SEPARATOR'
//		final int SYM_NUMVAL                                     =  396;  // NUMVAL
//		final int SYM_NUMVAL_C                                   =  397;  // 'NUMVAL_C'
//		final int SYM_NUMVAL_F                                   =  398;  // 'NUMVAL_F'
//		final int SYM_OBJECT_COMPUTER                            =  399;  // 'OBJECT_COMPUTER'
//		final int SYM_OCCURS                                     =  400;  // OCCURS
//		final int SYM_OF                                         =  401;  // OF
//		final int SYM_OFF                                        =  402;  // OFF
//		final int SYM_OMITTED                                    =  403;  // OMITTED
//		final int SYM_ON                                         =  404;  // ON
//		final int SYM_ONLY                                       =  405;  // ONLY
//		final int SYM_OPEN                                       =  406;  // OPEN
//		final int SYM_OPTIONAL                                   =  407;  // OPTIONAL
//		final int SYM_OPTIONS                                    =  408;  // OPTIONS
//		final int SYM_OR                                         =  409;  // OR
//		final int SYM_ORD                                        =  410;  // ORD
//		final int SYM_ORDER                                      =  411;  // ORDER
//		final int SYM_ORD_MAX                                    =  412;  // 'ORD_MAX'
//		final int SYM_ORD_MIN                                    =  413;  // 'ORD_MIN'
//		final int SYM_ORGANIZATION                               =  414;  // ORGANIZATION
//		final int SYM_OTHER                                      =  415;  // OTHER
//		final int SYM_OUTPUT                                     =  416;  // OUTPUT
//		final int SYM_OVERLINE                                   =  417;  // OVERLINE
//		final int SYM_PACKED_DECIMAL                             =  418;  // 'PACKED_DECIMAL'
//		final int SYM_PADDING                                    =  419;  // PADDING
//		final int SYM_PAGE                                       =  420;  // PAGE
//		final int SYM_PAGE_COUNTER                               =  421;  // 'PAGE_COUNTER'
//		final int SYM_PARAGRAPH                                  =  422;  // PARAGRAPH
//		final int SYM_PERFORM                                    =  423;  // PERFORM
//		final int SYM_PF                                         =  424;  // PF
//		final int SYM_PH                                         =  425;  // PH
//		final int SYM_PI                                         =  426;  // PI
//		final int SYM_PICTURE_DEF                                =  427;  // 'Picture_Def'
//		final int SYM_PICTURE_SYMBOL                             =  428;  // 'PICTURE_SYMBOL'
//		final int SYM_PLUS                                       =  429;  // PLUS
//		final int SYM_POINTER                                    =  430;  // POINTER
//		final int SYM_POSITION                                   =  431;  // POSITION
//		final int SYM_POSITIVE                                   =  432;  // POSITIVE
//		final int SYM_PRESENT                                    =  433;  // PRESENT
//		final int SYM_PRESENT_VALUE                              =  434;  // 'PRESENT_VALUE'
//		final int SYM_PREVIOUS                                   =  435;  // PREVIOUS
//		final int SYM_PRINT                                      =  436;  // PRINT
//		final int SYM_PRINTER                                    =  437;  // PRINTER
//		final int SYM_PRINTER_1                                  =  438;  // 'PRINTER_1'
//		final int SYM_PRINTING                                   =  439;  // PRINTING
//		final int SYM_PROCEDURE                                  =  440;  // PROCEDURE
//		final int SYM_PROCEDURES                                 =  441;  // PROCEDURES
//		final int SYM_PROCEED                                    =  442;  // PROCEED
//		final int SYM_PROGRAM                                    =  443;  // PROGRAM
//		final int SYM_PROGRAM_ID                                 =  444;  // 'PROGRAM_ID'
//		final int SYM_PROGRAM_POINTER                            =  445;  // 'PROGRAM_POINTER'
//		final int SYM_PROHIBITED                                 =  446;  // PROHIBITED
//		final int SYM_PROMPT                                     =  447;  // PROMPT
//		final int SYM_PROTECTED                                  =  448;  // PROTECTED
//		final int SYM_PURGE                                      =  449;  // PURGE
//		final int SYM_QUEUE                                      =  450;  // QUEUE
//		final int SYM_QUOTE                                      =  451;  // QUOTE
//		final int SYM_RANDOM                                     =  452;  // RANDOM
//		final int SYM_RANGE                                      =  453;  // RANGE
//		final int SYM_RD                                         =  454;  // RD
//		final int SYM_READ                                       =  455;  // READ
//		final int SYM_READY_TRACE                                =  456;  // 'READY_TRACE'
//		final int SYM_RECEIVE                                    =  457;  // RECEIVE
//		final int SYM_RECORD                                     =  458;  // RECORD
//		final int SYM_RECORDING                                  =  459;  // RECORDING
//		final int SYM_RECORDS                                    =  460;  // RECORDS
//		final int SYM_RECURSIVE                                  =  461;  // RECURSIVE
//		final int SYM_REDEFINES                                  =  462;  // REDEFINES
//		final int SYM_REEL                                       =  463;  // REEL
//		final int SYM_REFERENCE                                  =  464;  // REFERENCE
//		final int SYM_REFERENCES                                 =  465;  // REFERENCES
//		final int SYM_RELATIVE                                   =  466;  // RELATIVE
//		final int SYM_RELEASE                                    =  467;  // RELEASE
//		final int SYM_REM                                        =  468;  // REM
//		final int SYM_REMAINDER                                  =  469;  // REMAINDER
//		final int SYM_REMOVAL                                    =  470;  // REMOVAL
//		final int SYM_RENAMES                                    =  471;  // RENAMES
//		final int SYM_REPLACING                                  =  472;  // REPLACING
//		final int SYM_REPORT                                     =  473;  // REPORT
//		final int SYM_REPORTING                                  =  474;  // REPORTING
//		final int SYM_REPORTS                                    =  475;  // REPORTS
//		final int SYM_REPOSITORY                                 =  476;  // REPOSITORY
//		final int SYM_REQUIRED                                   =  477;  // REQUIRED
//		final int SYM_RESERVE                                    =  478;  // RESERVE
//		final int SYM_RESET                                      =  479;  // RESET
//		final int SYM_RESET_TRACE                                =  480;  // 'RESET_TRACE'
//		final int SYM_RETRY                                      =  481;  // RETRY
//		final int SYM_RETURN                                     =  482;  // RETURN
//		final int SYM_RETURNING                                  =  483;  // RETURNING
//		final int SYM_REVERSE                                    =  484;  // REVERSE
//		final int SYM_REVERSED                                   =  485;  // REVERSED
//		final int SYM_REVERSE_VIDEO                              =  486;  // 'REVERSE_VIDEO'
//		final int SYM_REWIND                                     =  487;  // REWIND
//		final int SYM_REWRITE                                    =  488;  // REWRITE
//		final int SYM_RF                                         =  489;  // RF
//		final int SYM_RH                                         =  490;  // RH
//		final int SYM_RIGHT                                      =  491;  // RIGHT
//		final int SYM_ROLLBACK                                   =  492;  // ROLLBACK
//		final int SYM_ROUNDED                                    =  493;  // ROUNDED
//		final int SYM_ROUNDING                                   =  494;  // ROUNDING
//		final int SYM_RUN                                        =  495;  // RUN
//		final int SYM_S                                          =  496;  // S
//		final int SYM_SAME                                       =  497;  // SAME
//		final int SYM_SCREEN                                     =  498;  // SCREEN
//		final int SYM_SCREEN_CONTROL                             =  499;  // 'SCREEN_CONTROL'
//		final int SYM_SCROLL                                     =  500;  // SCROLL
//		final int SYM_SD                                         =  501;  // SD
//		final int SYM_SEARCH                                     =  502;  // SEARCH
//		final int SYM_SECONDS                                    =  503;  // SECONDS
//		final int SYM_SECONDS_FROM_FORMATTED_TIME                =  504;  // 'SECONDS_FROM_FORMATTED_TIME'
//		final int SYM_SECONDS_PAST_MIDNIGHT                      =  505;  // 'SECONDS_PAST_MIDNIGHT'
//		final int SYM_SECTION                                    =  506;  // SECTION
//		final int SYM_SECURE                                     =  507;  // SECURE
//		final int SYM_SECURITY                                   =  508;  // SECURITY
//		final int SYM_SEGMENT                                    =  509;  // SEGMENT
//		final int SYM_SEGMENT_LIMIT                              =  510;  // 'SEGMENT_LIMIT'
//		final int SYM_SELECT                                     =  511;  // SELECT
//		final int SYM_SEMI_COLON                                 =  512;  // 'SEMI_COLON'
//		final int SYM_SEND                                       =  513;  // SEND
//		final int SYM_SENTENCE                                   =  514;  // SENTENCE
//		final int SYM_SEPARATE                                   =  515;  // SEPARATE
//		final int SYM_SEQUENCE                                   =  516;  // SEQUENCE
//		final int SYM_SEQUENTIAL                                 =  517;  // SEQUENTIAL
//		final int SYM_SET                                        =  518;  // SET
//		final int SYM_SEVENTY_EIGHT                              =  519;  // 'SEVENTY_EIGHT'
//		final int SYM_SHARING                                    =  520;  // SHARING
//		final int SYM_SIGN                                       =  521;  // SIGN
//		final int SYM_SIGNED                                     =  522;  // SIGNED
//		final int SYM_SIGNED_INT                                 =  523;  // 'SIGNED_INT'
//		final int SYM_SIGNED_LONG                                =  524;  // 'SIGNED_LONG'
//		final int SYM_SIGNED_SHORT                               =  525;  // 'SIGNED_SHORT'
//		final int SYM_SIN                                        =  526;  // SIN
//		final int SYM_SIXTY_SIX                                  =  527;  // 'SIXTY_SIX'
//		final int SYM_SIZE                                       =  528;  // SIZE
//		final int SYM_SIZE_ERROR                                 =  529;  // 'SIZE_ERROR'
//		final int SYM_SORT                                       =  530;  // SORT
//		final int SYM_SORT_MERGE                                 =  531;  // 'SORT_MERGE'
//		final int SYM_SOURCE                                     =  532;  // SOURCE
//		final int SYM_SOURCE_COMPUTER                            =  533;  // 'SOURCE_COMPUTER'
//		final int SYM_SPACE                                      =  534;  // SPACE
//		final int SYM_SPECIAL_NAMES                              =  535;  // 'SPECIAL_NAMES'
//		final int SYM_SQRT                                       =  536;  // SQRT
//		final int SYM_STANDARD                                   =  537;  // STANDARD
//		final int SYM_STANDARD_1                                 =  538;  // 'STANDARD_1'
//		final int SYM_STANDARD_2                                 =  539;  // 'STANDARD_2'
//		final int SYM_STANDARD_COMPARE                           =  540;  // 'STANDARD_COMPARE'
//		final int SYM_STANDARD_DEVIATION                         =  541;  // 'STANDARD_DEVIATION'
//		final int SYM_START                                      =  542;  // START
//		final int SYM_STATIC                                     =  543;  // STATIC
//		final int SYM_STATUS                                     =  544;  // STATUS
//		final int SYM_STDCALL                                    =  545;  // STDCALL
//		final int SYM_STEP                                       =  546;  // STEP
//		final int SYM_STOP                                       =  547;  // STOP
//		final int SYM_STORED_CHAR_LENGTH                         =  548;  // 'STORED_CHAR_LENGTH'
//		final int SYM_STRING                                     =  549;  // STRING
//		final int SYM_STRINGLITERAL                              =  550;  // StringLiteral
//		final int SYM_SUBSTITUTE                                 =  551;  // SUBSTITUTE
//		final int SYM_SUBSTITUTE_CASE                            =  552;  // 'SUBSTITUTE_CASE'
//		final int SYM_SUBTRACT                                   =  553;  // SUBTRACT
//		final int SYM_SUB_QUEUE_1                                =  554;  // 'SUB_QUEUE_1'
//		final int SYM_SUB_QUEUE_2                                =  555;  // 'SUB_QUEUE_2'
//		final int SYM_SUB_QUEUE_3                                =  556;  // 'SUB_QUEUE_3'
//		final int SYM_SUM                                        =  557;  // SUM
//		final int SYM_SUPPRESS                                   =  558;  // SUPPRESS
//		final int SYM_SYMBOLIC                                   =  559;  // SYMBOLIC
//		final int SYM_SYNCHRONIZED                               =  560;  // SYNCHRONIZED
//		final int SYM_SYSTEM_DEFAULT                             =  561;  // 'SYSTEM_DEFAULT'
//		final int SYM_SYSTEM_OFFSET                              =  562;  // 'SYSTEM_OFFSET'
//		final int SYM_TAB                                        =  563;  // TAB
//		final int SYM_TABLE                                      =  564;  // TABLE
//		final int SYM_TALLYING                                   =  565;  // TALLYING
//		final int SYM_TAN                                        =  566;  // TAN
//		final int SYM_TAPE                                       =  567;  // TAPE
//		final int SYM_TERMINAL                                   =  568;  // TERMINAL
//		final int SYM_TERMINATE                                  =  569;  // TERMINATE
//		final int SYM_TEST                                       =  570;  // TEST
//		final int SYM_TEST_DATE_YYYYMMDD                         =  571;  // 'TEST_DATE_YYYYMMDD'
//		final int SYM_TEST_DAY_YYYYDDD                           =  572;  // 'TEST_DAY_YYYYDDD'
//		final int SYM_TEST_FORMATTED_DATETIME                    =  573;  // 'TEST_FORMATTED_DATETIME'
//		final int SYM_TEST_NUMVAL                                =  574;  // 'TEST_NUMVAL'
//		final int SYM_TEST_NUMVAL_F                              =  575;  // 'TEST_NUMVAL_F'
//		final int SYM_TEXT                                       =  576;  // TEXT
//		final int SYM_THEN                                       =  577;  // THEN
//		final int SYM_THRU                                       =  578;  // THRU
//		final int SYM_TIME                                       =  579;  // TIME
//		final int SYM_TIMES                                      =  580;  // TIMES
//		final int SYM_TIME_OUT                                   =  581;  // 'TIME_OUT'
//		final int SYM_TO                                         =  582;  // TO
//		final int SYM_TOK_AMPER                                  =  583;  // 'TOK_AMPER'
//		final int SYM_TOK_CLOSE_PAREN                            =  584;  // 'TOK_CLOSE_PAREN'
//		final int SYM_TOK_COLON                                  =  585;  // 'TOK_COLON'
//		final int SYM_TOK_DIV                                    =  586;  // 'TOK_DIV'
//		final int SYM_TOK_DOT                                    =  587;  // 'TOK_DOT'
//		final int SYM_TOK_EQUAL                                  =  588;  // 'TOK_EQUAL'
//		final int SYM_TOK_EXTERN                                 =  589;  // 'TOK_EXTERN'
//		final int SYM_TOK_FALSE                                  =  590;  // 'TOK_FALSE'
//		final int SYM_TOK_FILE                                   =  591;  // 'TOK_FILE'
//		final int SYM_TOK_GREATER                                =  592;  // 'TOK_GREATER'
//		final int SYM_TOK_INITIAL                                =  593;  // 'TOK_INITIAL'
//		final int SYM_TOK_LESS                                   =  594;  // 'TOK_LESS'
//		final int SYM_TOK_MINUS                                  =  595;  // 'TOK_MINUS'
//		final int SYM_TOK_MUL                                    =  596;  // 'TOK_MUL'
//		final int SYM_TOK_NULL                                   =  597;  // 'TOK_NULL'
//		final int SYM_TOK_OPEN_PAREN                             =  598;  // 'TOK_OPEN_PAREN'
//		final int SYM_TOK_OVERFLOW                               =  599;  // 'TOK_OVERFLOW'
//		final int SYM_TOK_PLUS                                   =  600;  // 'TOK_PLUS'
//		final int SYM_TOK_TRUE                                   =  601;  // 'TOK_TRUE'
//		final int SYM_TOP                                        =  602;  // TOP
//		final int SYM_TOWARD_GREATER                             =  603;  // 'TOWARD_GREATER'
//		final int SYM_TOWARD_LESSER                              =  604;  // 'TOWARD_LESSER'
//		final int SYM_TRAILING                                   =  605;  // TRAILING
//		final int SYM_TRANSFORM                                  =  606;  // TRANSFORM
//		final int SYM_TRIM                                       =  607;  // TRIM
//		final int SYM_TRUNCATION                                 =  608;  // TRUNCATION
//		final int SYM_TYPE                                       =  609;  // TYPE
//		final int SYM_U                                          =  610;  // U
//		final int SYM_UNBOUNDED                                  =  611;  // UNBOUNDED
//		final int SYM_UNDERLINE                                  =  612;  // UNDERLINE
//		final int SYM_UNIT                                       =  613;  // UNIT
//		final int SYM_UNLOCK                                     =  614;  // UNLOCK
//		final int SYM_UNSIGNED                                   =  615;  // UNSIGNED
//		final int SYM_UNSIGNED_INT                               =  616;  // 'UNSIGNED_INT'
//		final int SYM_UNSIGNED_LONG                              =  617;  // 'UNSIGNED_LONG'
//		final int SYM_UNSIGNED_SHORT                             =  618;  // 'UNSIGNED_SHORT'
//		final int SYM_UNSTRING                                   =  619;  // UNSTRING
//		final int SYM_UNTIL                                      =  620;  // UNTIL
//		final int SYM_UP                                         =  621;  // UP
//		final int SYM_UPDATE                                     =  622;  // UPDATE
//		final int SYM_UPON                                       =  623;  // UPON
//		final int SYM_UPON_ARGUMENT_NUMBER                       =  624;  // 'UPON_ARGUMENT_NUMBER'
//		final int SYM_UPON_COMMAND_LINE                          =  625;  // 'UPON_COMMAND_LINE'
//		final int SYM_UPON_ENVIRONMENT_NAME                      =  626;  // 'UPON_ENVIRONMENT_NAME'
//		final int SYM_UPON_ENVIRONMENT_VALUE                     =  627;  // 'UPON_ENVIRONMENT_VALUE'
//		final int SYM_UPPER                                      =  628;  // UPPER
//		final int SYM_UPPER_CASE                                 =  629;  // 'UPPER_CASE'
//		final int SYM_USAGE                                      =  630;  // USAGE
//		final int SYM_USE                                        =  631;  // USE
//		final int SYM_USER                                       =  632;  // USER
//		final int SYM_USER_DEFAULT                               =  633;  // 'USER_DEFAULT'
//		final int SYM_USING                                      =  634;  // USING
//		final int SYM_V                                          =  635;  // V
//		final int SYM_VALUE                                      =  636;  // VALUE
//		final int SYM_VARIABLE                                   =  637;  // VARIABLE
//		final int SYM_VARIANCE                                   =  638;  // VARIANCE
//		final int SYM_VARYING                                    =  639;  // VARYING
//		final int SYM_WAIT                                       =  640;  // WAIT
//		final int SYM_WHEN                                       =  641;  // WHEN
//		final int SYM_WHEN_COMPILED                              =  642;  // 'WHEN_COMPILED'
//		final int SYM_WITH                                       =  643;  // WITH
//		final int SYM_WITH_DATA                                  =  644;  // 'WITH_DATA'
//		final int SYM_WORDS                                      =  645;  // WORDS
//		final int SYM_WORKING_STORAGE                            =  646;  // 'WORKING_STORAGE'
//		final int SYM_WRITE                                      =  647;  // WRITE
//		final int SYM_YEAR_TO_YYYY                               =  648;  // 'YEAR_TO_YYYY'
//		final int SYM_YYYYDDD                                    =  649;  // YYYYDDD
//		final int SYM_YYYYMMDD                                   =  650;  // YYYYMMDD
//		final int SYM_ZERO                                       =  651;  // ZERO
//		final int SYM_ZLITERAL                                   =  652;  // ZLiteral
//		final int SYM_ACCEPT_BODY                                =  653;  // <accept_body>
//		final int SYM_ACCEPT_CLAUSE                              =  654;  // <accept_clause>
//		final int SYM_ACCEPT_CLAUSES                             =  655;  // <accept_clauses>
//		final int SYM_ACCEPT_STATEMENT                           =  656;  // <accept_statement>
//		final int SYM_ACCESS_MODE                                =  657;  // <access_mode>
//		final int SYM_ACCESS_MODE_CLAUSE                         =  658;  // <access_mode_clause>
//		final int SYM_ACCP_ATTR                                  =  659;  // <accp_attr>
//		final int SYM_ACCP_IDENTIFIER                            =  660;  // <accp_identifier>
//		final int SYM_ACCP_NOT_ON_EXCEPTION                      =  661;  // <accp_not_on_exception>
//		final int SYM_ACCP_ON_EXCEPTION                          =  662;  // <accp_on_exception>
//		final int SYM_ADD_BODY                                   =  663;  // <add_body>
//		final int SYM_ADD_STATEMENT                              =  664;  // <add_statement>
//		final int SYM_ADVANCING_LOCK_OR_RETRY                    =  665;  // <advancing_lock_or_retry>
//		final int SYM_ALLOCATE_BODY                              =  666;  // <allocate_body>
//		final int SYM_ALLOCATE_RETURNING                         =  667;  // <allocate_returning>
//		final int SYM_ALLOCATE_STATEMENT                         =  668;  // <allocate_statement>
//		final int SYM_ALNUM_OR_ID                                =  669;  // <alnum_or_id>
//		final int SYM_ALPHABET_ALSO_SEQUENCE                     =  670;  // <alphabet_also_sequence>
//		final int SYM_ALPHABET_DEFINITION                        =  671;  // <alphabet_definition>
//		final int SYM_ALPHABET_LITERAL                           =  672;  // <alphabet_literal>
//		final int SYM_ALPHABET_LITERAL_LIST                      =  673;  // <alphabet_literal_list>
//		final int SYM_ALPHABET_LITS                              =  674;  // <alphabet_lits>
//		final int SYM_ALPHABET_NAME                              =  675;  // <alphabet_name>
//		final int SYM_ALPHABET_NAME_CLAUSE                       =  676;  // <alphabet_name_clause>
//		final int SYM_ALTERNATIVE_RECORD_KEY_CLAUSE              =  677;  // <alternative_record_key_clause>
//		final int SYM_ALTER_BODY                                 =  678;  // <alter_body>
//		final int SYM_ALTER_ENTRY                                =  679;  // <alter_entry>
//		final int SYM_ALTER_STATEMENT                            =  680;  // <alter_statement>
//		final int SYM_ANY_LENGTH_CLAUSE                          =  681;  // <any_length_clause>
//		final int SYM_ARITHMETIC_X                               =  682;  // <arithmetic_x>
//		final int SYM_ARITHMETIC_X_LIST                          =  683;  // <arithmetic_x_list>
//		final int SYM_ARITH_X                                    =  684;  // <arith_x>
//		final int SYM_ASCENDING_OR_DESCENDING                    =  685;  // <ascending_or_descending>
//		final int SYM_ASSIGNMENT_NAME                            =  686;  // <assignment_name>
//		final int SYM_ASSIGN_CLAUSE                              =  687;  // <assign_clause>
//		final int SYM_AT_END                                     =  688;  // <at_end>
//		final int SYM_AT_END_CLAUSE                              =  689;  // <at_end_clause>
//		final int SYM_AT_EOP_CLAUSE                              =  690;  // <at_eop_clause>
//		final int SYM_AT_EOP_CLAUSES                             =  691;  // <at_eop_clauses>
//		final int SYM_AT_LINE_COLUMN                             =  692;  // <at_line_column>
//		final int SYM_BASED_CLAUSE                               =  693;  // <based_clause>
//		final int SYM_BASIC_LITERAL                              =  694;  // <basic_literal>
//		final int SYM_BASIC_VALUE                                =  695;  // <basic_value>
//		final int SYM_BEFORE_OR_AFTER                            =  696;  // <before_or_after>
//		final int SYM_BLANK_CLAUSE                               =  697;  // <blank_clause>
//		final int SYM_BLOCK_CONTAINS_CLAUSE                      =  698;  // <block_contains_clause>
//		final int SYM_CALL_BODY                                  =  699;  // <call_body>
//		final int SYM_CALL_EXCEPTION_PHRASES                     =  700;  // <call_exception_phrases>
//		final int SYM_CALL_NOT_ON_EXCEPTION                      =  701;  // <call_not_on_exception>
//		final int SYM_CALL_ON_EXCEPTION                          =  702;  // <call_on_exception>
//		final int SYM_CALL_PARAM                                 =  703;  // <call_param>
//		final int SYM_CALL_PARAM_LIST                            =  704;  // <call_param_list>
//		final int SYM_CALL_RETURNING                             =  705;  // <call_returning>
//		final int SYM_CALL_STATEMENT                             =  706;  // <call_statement>
//		final int SYM_CALL_TYPE                                  =  707;  // <call_type>
//		final int SYM_CALL_USING                                 =  708;  // <call_using>
//		final int SYM_CALL_X                                     =  709;  // <call_x>
//		final int SYM_CANCEL_BODY                                =  710;  // <cancel_body>
//		final int SYM_CANCEL_STATEMENT                           =  711;  // <cancel_statement>
//		final int SYM_CD_NAME                                    =  712;  // <cd_name>
//		final int SYM_CF_KEYWORD                                 =  713;  // <cf_keyword>
//		final int SYM_CHAR_LIST                                  =  714;  // <char_list>
//		final int SYM_CH_KEYWORD                                 =  715;  // <ch_keyword>
//		final int SYM_CLASS_ITEM                                 =  716;  // <class_item>
//		final int SYM_CLASS_ITEM_LIST                            =  717;  // <class_item_list>
//		final int SYM_CLASS_NAME                                 =  718;  // <CLASS_NAME>
//		final int SYM_CLASS_NAME_CLAUSE                          =  719;  // <class_name_clause>
//		final int SYM_CLASS_VALUE                                =  720;  // <class_value>
//		final int SYM_CLOSE_BODY                                 =  721;  // <close_body>
//		final int SYM_CLOSE_OPTION                               =  722;  // <close_option>
//		final int SYM_CLOSE_STATEMENT                            =  723;  // <close_statement>
//		final int SYM_CODE_SET_CLAUSE                            =  724;  // <code_set_clause>
//		final int SYM_COLLATING_SEQUENCE_CLAUSE                  =  725;  // <collating_sequence_clause>
//		final int SYM_COLL_SEQUENCE                              =  726;  // <coll_sequence>
//		final int SYM_COLUMNS_OR_COLS                            =  727;  // <columns_or_cols>
//		final int SYM_COLUMN_CLAUSE                              =  728;  // <column_clause>
//		final int SYM_COLUMN_NUMBER                              =  729;  // <column_number>
//		final int SYM_COLUMN_OR_COL                              =  730;  // <column_or_col>
//		final int SYM_COL_KEYWORD_CLAUSE                         =  731;  // <col_keyword_clause>
//		final int SYM_COL_OR_PLUS                                =  732;  // <col_or_plus>
//		final int SYM_COMMENTITEM                                =  733;  // <Comment Item>
//		final int SYM_COMMIT_STATEMENT                           =  734;  // <commit_statement>
//		final int SYM_COMMON_FUNCTION                            =  735;  // <COMMON_FUNCTION>
//		final int SYM_COMMUNICATION_DESCRIPTION                  =  736;  // <communication_description>
//		final int SYM_COMMUNICATION_DESCRIPTION_CLAUSE           =  737;  // <communication_description_clause>
//		final int SYM_COMMUNICATION_DESCRIPTION_ENTRY            =  738;  // <communication_description_entry>
//		final int SYM_COMMUNICATION_MODE                         =  739;  // <communication_mode>
//		final int SYM_COMPILATION_GROUP                          =  740;  // <compilation_group>
//		final int SYM_COMPUTER_WORDS                             =  741;  // <computer_words>
//		final int SYM_COMPUTE_BODY                               =  742;  // <compute_body>
//		final int SYM_COMPUTE_STATEMENT                          =  743;  // <compute_statement>
//		final int SYM_COMP_EQUAL                                 =  744;  // <comp_equal>
//		final int SYM_CONCATENATE_FUNC                           =  745;  // <CONCATENATE_FUNC>
//		final int SYM_CONDITION                                  =  746;  // <condition>
//		final int SYM_CONDITION_NAME_ENTRY                       =  747;  // <condition_name_entry>
//		final int SYM_CONDITION_OP                               =  748;  // <condition_op>
//		final int SYM_CONDITION_OR_CLASS                         =  749;  // <condition_or_class>
//		final int SYM_COND_OR_EXIT                               =  750;  // <cond_or_exit>
//		final int SYM_CONSTANT_ENTRY                             =  751;  // <constant_entry>
//		final int SYM_CONSTANT_SOURCE                            =  752;  // <constant_source>
//		final int SYM_CONST_GLOBAL                               =  753;  // <const_global>
//		final int SYM_CONTINUE_STATEMENT                         =  754;  // <continue_statement>
//		final int SYM_CONTROL_CLAUSE                             =  755;  // <control_clause>
//		final int SYM_CONTROL_FIELD_LIST                         =  756;  // <control_field_list>
//		final int SYM_CONTROL_KEYWORD                            =  757;  // <control_keyword>
//		final int SYM_CONVENTION_TYPE                            =  758;  // <convention_type>
//		final int SYM_CON_IDENTIFIER                             =  759;  // <con_identifier>
//		final int SYM_CRT_STATUS_CLAUSE                          =  760;  // <crt_status_clause>
//		final int SYM_CRT_UNDER2                                 =  761;  // <crt_under>
//		final int SYM_CURRENCY_SIGN_CLAUSE                       =  762;  // <currency_sign_clause>
//		final int SYM_CURRENT_DATE_FUNC                          =  763;  // <CURRENT_DATE_FUNC>
//		final int SYM_CURSOR_CLAUSE                              =  764;  // <cursor_clause>
//		final int SYM_DATA_DESCRIPTION                           =  765;  // <data_description>
//		final int SYM_DATA_DESCRIPTION_CLAUSE                    =  766;  // <data_description_clause>
//		final int SYM_DATA_OR_FINAL                              =  767;  // <data_or_final>
//		final int SYM_DATA_RECORDS_CLAUSE                        =  768;  // <data_records_clause>
//		final int SYM_DEBUGGING_LIST                             =  769;  // <debugging_list>
//		final int SYM_DEBUGGING_TARGET                           =  770;  // <debugging_target>
//		final int SYM_DECIMAL_POINT_CLAUSE                       =  771;  // <decimal_point_clause>
//		final int SYM_DELETE_BODY                                =  772;  // <delete_body>
//		final int SYM_DELETE_FILE_LIST                           =  773;  // <delete_file_list>
//		final int SYM_DELETE_STATEMENT                           =  774;  // <delete_statement>
//		final int SYM_DETAIL_KEYWORD                             =  775;  // <detail_keyword>
//		final int SYM_DISABLE_STATEMENT                          =  776;  // <disable_statement>
//		final int SYM_DISALLOWED_OP                              =  777;  // <disallowed_op>
//		final int SYM_DISPLAY_ATOM                               =  778;  // <display_atom>
//		final int SYM_DISPLAY_BODY                               =  779;  // <display_body>
//		final int SYM_DISPLAY_CLAUSE                             =  780;  // <display_clause>
//		final int SYM_DISPLAY_CLAUSES                            =  781;  // <display_clauses>
//		final int SYM_DISPLAY_IDENTIFIER                         =  782;  // <display_identifier>
//		final int SYM_DISPLAY_LIST                               =  783;  // <display_list>
//		final int SYM_DISPLAY_OF_FUNC                            =  784;  // <DISPLAY_OF_FUNC>
//		final int SYM_DISPLAY_STATEMENT                          =  785;  // <display_statement>
//		final int SYM_DISPLAY_UPON                               =  786;  // <display_upon>
//		final int SYM_DISP_ATTR                                  =  787;  // <disp_attr>
//		final int SYM_DISP_LIST                                  =  788;  // <disp_list>
//		final int SYM_DISP_NOT_ON_EXCEPTION                      =  789;  // <disp_not_on_exception>
//		final int SYM_DISP_ON_EXCEPTION                          =  790;  // <disp_on_exception>
//		final int SYM_DIVIDE_BODY                                =  791;  // <divide_body>
//		final int SYM_DIVIDE_STATEMENT                           =  792;  // <divide_statement>
//		final int SYM_DOUBLE_USAGE                               =  793;  // <double_usage>
//		final int SYM_ENABLE_DISABLE_HANDLING                    =  794;  // <enable_disable_handling>
//		final int SYM_ENABLE_STATEMENT                           =  795;  // <enable_statement>
//		final int SYM_END_ACCEPT2                                =  796;  // <end_accept>
//		final int SYM_END_ADD2                                   =  797;  // <end_add>
//		final int SYM_END_CALL2                                  =  798;  // <end_call>
//		final int SYM_END_COMPUTE2                               =  799;  // <end_compute>
//		final int SYM_END_DELETE2                                =  800;  // <end_delete>
//		final int SYM_END_DISPLAY2                               =  801;  // <end_display>
//		final int SYM_END_DIVIDE2                                =  802;  // <end_divide>
//		final int SYM_END_EVALUATE2                              =  803;  // <end_evaluate>
//		final int SYM_END_FUNCTION2                              =  804;  // <end_function>
//		final int SYM_END_IF2                                    =  805;  // <end_if>
//		final int SYM_END_MULTIPLY2                              =  806;  // <end_multiply>
//		final int SYM_END_PERFORM2                               =  807;  // <end_perform>
//		final int SYM_END_PROGRAM2                               =  808;  // <end_program>
//		final int SYM_END_PROGRAM_LIST                           =  809;  // <end_program_list>
//		final int SYM_END_PROGRAM_NAME                           =  810;  // <end_program_name>
//		final int SYM_END_READ2                                  =  811;  // <end_read>
//		final int SYM_END_RECEIVE2                               =  812;  // <end_receive>
//		final int SYM_END_RETURN2                                =  813;  // <end_return>
//		final int SYM_END_REWRITE2                               =  814;  // <end_rewrite>
//		final int SYM_END_SEARCH2                                =  815;  // <end_search>
//		final int SYM_END_START2                                 =  816;  // <end_start>
//		final int SYM_END_STRING2                                =  817;  // <end_string>
//		final int SYM_END_SUBTRACT2                              =  818;  // <end_subtract>
//		final int SYM_END_UNSTRING2                              =  819;  // <end_unstring>
//		final int SYM_END_WRITE2                                 =  820;  // <end_write>
//		final int SYM_ENTRY_BODY                                 =  821;  // <entry_body>
//		final int SYM_ENTRY_STATEMENT                            =  822;  // <entry_statement>
//		final int SYM_EOL2                                       =  823;  // <eol>
//		final int SYM_EOS2                                       =  824;  // <eos>
//		final int SYM_EQ                                         =  825;  // <eq>
//		final int SYM_ERROR_STMT_RECOVER                         =  826;  // <error_stmt_recover>
//		final int SYM_ESCAPE_OR_EXCEPTION                        =  827;  // <escape_or_exception>
//		final int SYM_EVALUATE_BODY                              =  828;  // <evaluate_body>
//		final int SYM_EVALUATE_CASE                              =  829;  // <evaluate_case>
//		final int SYM_EVALUATE_CASE_LIST                         =  830;  // <evaluate_case_list>
//		final int SYM_EVALUATE_CONDITION_LIST                    =  831;  // <evaluate_condition_list>
//		final int SYM_EVALUATE_OBJECT                            =  832;  // <evaluate_object>
//		final int SYM_EVALUATE_OBJECT_LIST                       =  833;  // <evaluate_object_list>
//		final int SYM_EVALUATE_OTHER                             =  834;  // <evaluate_other>
//		final int SYM_EVALUATE_STATEMENT                         =  835;  // <evaluate_statement>
//		final int SYM_EVALUATE_SUBJECT                           =  836;  // <evaluate_subject>
//		final int SYM_EVALUATE_SUBJECT_LIST                      =  837;  // <evaluate_subject_list>
//		final int SYM_EVALUATE_WHEN_LIST                         =  838;  // <evaluate_when_list>
//		final int SYM_EVENT_STATUS2                              =  839;  // <event_status>
//		final int SYM_EXCEPTION_OR_ERROR                         =  840;  // <exception_or_error>
//		final int SYM_EXIT_BODY                                  =  841;  // <exit_body>
//		final int SYM_EXIT_PROGRAM_RETURNING                     =  842;  // <exit_program_returning>
//		final int SYM_EXIT_STATEMENT                             =  843;  // <exit_statement>
//		final int SYM_EXP2                                       =  844;  // <exp>
//		final int SYM_EXPR                                       =  845;  // <expr>
//		final int SYM_EXPR_TOKEN                                 =  846;  // <expr_token>
//		final int SYM_EXPR_TOKENS                                =  847;  // <expr_tokens>
//		final int SYM_EXPR_X                                     =  848;  // <expr_x>
//		final int SYM_EXP_ATOM                                   =  849;  // <exp_atom>
//		final int SYM_EXP_FACTOR                                 =  850;  // <exp_factor>
//		final int SYM_EXP_LIST                                   =  851;  // <exp_list>
//		final int SYM_EXP_TERM                                   =  852;  // <exp_term>
//		final int SYM_EXP_UNARY                                  =  853;  // <exp_unary>
//		final int SYM_EXTENDED_WITH_LOCK                         =  854;  // <extended_with_lock>
//		final int SYM_EXTERNAL_CLAUSE                            =  855;  // <external_clause>
//		final int SYM_FILE_CONTROL_ENTRY                         =  856;  // <file_control_entry>
//		final int SYM_FILE_DESCRIPTION                           =  857;  // <file_description>
//		final int SYM_FILE_DESCRIPTION_CLAUSE                    =  858;  // <file_description_clause>
//		final int SYM_FILE_DESCRIPTION_ENTRY                     =  859;  // <file_description_entry>
//		final int SYM_FILE_ID2                                   =  860;  // <file_id>
//		final int SYM_FILE_NAME                                  =  861;  // <file_name>
//		final int SYM_FILE_NAME_LIST                             =  862;  // <file_name_list>
//		final int SYM_FILE_OR_RECORD_NAME                        =  863;  // <file_or_record_name>
//		final int SYM_FILE_STATUS_CLAUSE                         =  864;  // <file_status_clause>
//		final int SYM_FILE_TYPE                                  =  865;  // <file_type>
//		final int SYM_FIRST_DETAIL                               =  866;  // <first_detail>
//		final int SYM_FLAG_ALL                                   =  867;  // <flag_all>
//		final int SYM_FLAG_DUPLICATES                            =  868;  // <flag_duplicates>
//		final int SYM_FLAG_INITIALIZED                           =  869;  // <flag_initialized>
//		final int SYM_FLAG_INITIALIZED_TO                        =  870;  // <flag_initialized_to>
//		final int SYM_FLAG_OPTIONAL                              =  871;  // <flag_optional>
//		final int SYM_FLAG_ROUNDED                               =  872;  // <flag_rounded>
//		final int SYM_FLAG_SEPARATE                              =  873;  // <flag_separate>
//		final int SYM_FLOAT_USAGE                                =  874;  // <float_usage>
//		final int SYM_FOOTING_CLAUSE                             =  875;  // <footing_clause>
//		final int SYM_FORMATTED_DATETIME_ARGS                    =  876;  // <formatted_datetime_args>
//		final int SYM_FORMATTED_DATETIME_FUNC                    =  877;  // <FORMATTED_DATETIME_FUNC>
//		final int SYM_FORMATTED_DATE_FUNC                        =  878;  // <FORMATTED_DATE_FUNC>
//		final int SYM_FORMATTED_TIME_ARGS                        =  879;  // <formatted_time_args>
//		final int SYM_FORMATTED_TIME_FUNC                        =  880;  // <FORMATTED_TIME_FUNC>
//		final int SYM_FP128_USAGE                                =  881;  // <fp128_usage>
//		final int SYM_FP32_USAGE                                 =  882;  // <fp32_usage>
//		final int SYM_FP64_USAGE                                 =  883;  // <fp64_usage>
//		final int SYM_FREE_BODY                                  =  884;  // <free_body>
//		final int SYM_FREE_STATEMENT                             =  885;  // <free_statement>
//		final int SYM_FROM_IDENTIFIER                            =  886;  // <from_identifier>
//		final int SYM_FROM_OPTION                                =  887;  // <from_option>
//		final int SYM_FROM_PARAMETER                             =  888;  // <from_parameter>
//		final int SYM_FUNCTION2                                  =  889;  // <function>
//		final int SYM_FUNCTION_DEFINITION                        =  890;  // <function_definition>
//		final int SYM_FUNCTION_ID_PARAGRAPH                      =  891;  // <function_id_paragraph>
//		final int SYM_FUNCTION_NAME                              =  892;  // <FUNCTION_NAME>
//		final int SYM_FUNC_ARGS                                  =  893;  // <func_args>
//		final int SYM_FUNC_MULTI_PARM                            =  894;  // <func_multi_parm>
//		final int SYM_FUNC_NO_PARM                               =  895;  // <func_no_parm>
//		final int SYM_FUNC_ONE_PARM                              =  896;  // <func_one_parm>
//		final int SYM_FUNC_REFMOD                                =  897;  // <func_refmod>
//		final int SYM_GE                                         =  898;  // <ge>
//		final int SYM_GENERAL_DEVICE_NAME                        =  899;  // <general_device_name>
//		final int SYM_GENERATE_BODY                              =  900;  // <generate_body>
//		final int SYM_GENERATE_STATEMENT                         =  901;  // <generate_statement>
//		final int SYM_GLOBAL_CLAUSE                              =  902;  // <global_clause>
//		final int SYM_GOBACK_STATEMENT                           =  903;  // <goback_statement>
//		final int SYM_GOTO_DEPENDING                             =  904;  // <goto_depending>
//		final int SYM_GOTO_STATEMENT                             =  905;  // <goto_statement>
//		final int SYM_GO_BODY                                    =  906;  // <go_body>
//		final int SYM_GROUP_INDICATE_CLAUSE                      =  907;  // <group_indicate_clause>
//		final int SYM_GT                                         =  908;  // <gt>
//		final int SYM_HEADING_CLAUSE                             =  909;  // <heading_clause>
//		final int SYM_IDENTIFICATION_OR_ID                       =  910;  // <identification_or_id>
//		final int SYM_IDENTIFIER                                 =  911;  // <identifier>
//		final int SYM_IDENTIFIER_1                               =  912;  // <identifier_1>
//		final int SYM_IDENTIFIER_LIST                            =  913;  // <identifier_list>
//		final int SYM_IDENTIFIER_OR_FILE_NAME                    =  914;  // <identifier_or_file_name>
//		final int SYM_ID_OR_LIT                                  =  915;  // <id_or_lit>
//		final int SYM_ID_OR_LIT_OR_FUNC                          =  916;  // <id_or_lit_or_func>
//		final int SYM_ID_OR_LIT_OR_FUNC_AS                       =  917;  // <id_or_lit_or_func_as>
//		final int SYM_ID_OR_LIT_OR_LENGTH_OR_FUNC                =  918;  // <id_or_lit_or_length_or_func>
//		final int SYM_ID_OR_LIT_OR_PROGRAM_NAME                  =  919;  // <id_or_lit_or_program_name>
//		final int SYM_IF_ELSE_STATEMENTS                         =  920;  // <if_else_statements>
//		final int SYM_IF_STATEMENT                               =  921;  // <if_statement>
//		final int SYM_IGNORING_LOCK                              =  922;  // <ignoring_lock>
//		final int SYM_INITIALIZE_BODY                            =  923;  // <initialize_body>
//		final int SYM_INITIALIZE_CATEGORY                        =  924;  // <initialize_category>
//		final int SYM_INITIALIZE_REPLACING_ITEM                  =  925;  // <initialize_replacing_item>
//		final int SYM_INITIALIZE_REPLACING_LIST                  =  926;  // <initialize_replacing_list>
//		final int SYM_INITIALIZE_STATEMENT                       =  927;  // <initialize_statement>
//		final int SYM_INITIATE_BODY                              =  928;  // <initiate_body>
//		final int SYM_INITIATE_STATEMENT                         =  929;  // <initiate_statement>
//		final int SYM_INIT_OR_RECURSE                            =  930;  // <init_or_recurse>
//		final int SYM_INIT_OR_RECURSE_AND_COMMON                 =  931;  // <init_or_recurse_and_common>
//		final int SYM_INSPECT_AFTER                              =  932;  // <inspect_after>
//		final int SYM_INSPECT_BEFORE                             =  933;  // <inspect_before>
//		final int SYM_INSPECT_BODY                               =  934;  // <inspect_body>
//		final int SYM_INSPECT_CONVERTING                         =  935;  // <inspect_converting>
//		final int SYM_INSPECT_LIST                               =  936;  // <inspect_list>
//		final int SYM_INSPECT_REGION                             =  937;  // <inspect_region>
//		final int SYM_INSPECT_REPLACING                          =  938;  // <inspect_replacing>
//		final int SYM_INSPECT_STATEMENT                          =  939;  // <inspect_statement>
//		final int SYM_INSPECT_TALLYING                           =  940;  // <inspect_tallying>
//		final int SYM_INTEGER2                                   =  941;  // <integer>
//		final int SYM_INTEGER_LABEL                              =  942;  // <integer_label>
//		final int SYM_INTEGER_LIST                               =  943;  // <integer_list>
//		final int SYM_INTEGER_OR_WORD                            =  944;  // <integer_or_word>
//		final int SYM_INTERMEDIATE_ROUNDING_CHOICE               =  945;  // <intermediate_rounding_choice>
//		final int SYM_INTLITERALORWORD                           =  946;  // <IntLiteral or WORD>
//		final int SYM_INVALID_KEY_PHRASES                        =  947;  // <invalid_key_phrases>
//		final int SYM_INVALID_KEY_SENTENCE                       =  948;  // <invalid_key_sentence>
//		final int SYM_IN_OF                                      =  949;  // <in_of>
//		final int SYM_I_O_CONTROL_CLAUSE                         =  950;  // <i_o_control_clause>
//		final int SYM_I_O_CONTROL_LIST                           =  951;  // <i_o_control_list>
//		final int SYM_JUSTIFIED_CLAUSE                           =  952;  // <justified_clause>
//		final int SYM_KEY_OR_SPLIT_KEYS                          =  953;  // <key_or_split_keys>
//		final int SYM_LABEL2                                     =  954;  // <label>
//		final int SYM_LABEL_OPTION                               =  955;  // <label_option>
//		final int SYM_LABEL_RECORDS_CLAUSE                       =  956;  // <label_records_clause>
//		final int SYM_LAST_DETAIL                                =  957;  // <last_detail>
//		final int SYM_LAST_HEADING                               =  958;  // <last_heading>
//		final int SYM_LE                                         =  959;  // <le>
//		final int SYM_LENGTH_ARG                                 =  960;  // <length_arg>
//		final int SYM_LENGTH_FUNC                                =  961;  // <LENGTH_FUNC>
//		final int SYM_LEVEL_NUMBER                               =  962;  // <level_number>
//		final int SYM_LINAGE_BOTTOM                              =  963;  // <linage_bottom>
//		final int SYM_LINAGE_CLAUSE                              =  964;  // <linage_clause>
//		final int SYM_LINAGE_FOOTING                             =  965;  // <linage_footing>
//		final int SYM_LINAGE_LINES                               =  966;  // <linage_lines>
//		final int SYM_LINAGE_TOP                                 =  967;  // <linage_top>
//		final int SYM_LINES_OR_NUMBER                            =  968;  // <lines_or_number>
//		final int SYM_LINE_CLAUSE                                =  969;  // <line_clause>
//		final int SYM_LINE_KEYWORD_CLAUSE                        =  970;  // <line_keyword_clause>
//		final int SYM_LINE_LINAGE_PAGE_COUNTER                   =  971;  // <line_linage_page_counter>
//		final int SYM_LINE_NUMBER                                =  972;  // <line_number>
//		final int SYM_LINE_OR_LINES                              =  973;  // <line_or_lines>
//		final int SYM_LINE_OR_PLUS                               =  974;  // <line_or_plus>
//		final int SYM_LINE_SEQ_DEVICE_NAME                       =  975;  // <line_seq_device_name>
//		final int SYM_LITERAL                                    =  976;  // <literal>
//		final int SYM_LITERAL_TOK                                =  977;  // <LITERAL_TOK>
//		final int SYM_LIT_OR_LENGTH                              =  978;  // <lit_or_length>
//		final int SYM_LOCALE_CLASS                               =  979;  // <locale_class>
//		final int SYM_LOCALE_CLAUSE                              =  980;  // <locale_clause>
//		final int SYM_LOCALE_DATE_FUNC                           =  981;  // <LOCALE_DATE_FUNC>
//		final int SYM_LOCALE_DT_ARGS                             =  982;  // <locale_dt_args>
//		final int SYM_LOCALE_TIME_FROM_FUNC                      =  983;  // <LOCALE_TIME_FROM_FUNC>
//		final int SYM_LOCALE_TIME_FUNC                           =  984;  // <LOCALE_TIME_FUNC>
//		final int SYM_LOCK_MODE                                  =  985;  // <lock_mode>
//		final int SYM_LOCK_MODE_CLAUSE                           =  986;  // <lock_mode_clause>
//		final int SYM_LOCK_PHRASES                               =  987;  // <lock_phrases>
//		final int SYM_LOCK_RECORDS                               =  988;  // <lock_records>
//		final int SYM_LOWER_CASE_FUNC                            =  989;  // <LOWER_CASE_FUNC>
//		final int SYM_LT                                         =  990;  // <lt>
//		final int SYM_MERGE_STATEMENT                            =  991;  // <merge_statement>
//		final int SYM_MESSAGE_OR_SEGMENT                         =  992;  // <message_or_segment>
//		final int SYM_MINUS_MINUS                                =  993;  // <minus_minus>
//		final int SYM_MNEMONIC_CHOICES                           =  994;  // <mnemonic_choices>
//		final int SYM_MNEMONIC_NAME2                             =  995;  // <mnemonic_name>
//		final int SYM_MNEMONIC_NAME_CLAUSE                       =  996;  // <mnemonic_name_clause>
//		final int SYM_MNEMONIC_NAME_LIST                         =  997;  // <mnemonic_name_list>
//		final int SYM_MNEMONIC_NAME_TOK                          =  998;  // <MNEMONIC_NAME_TOK>
//		final int SYM_MODE_IS_BLOCK                              =  999;  // <mode_is_block>
//		final int SYM_MOVE_BODY                                  = 1000;  // <move_body>
//		final int SYM_MOVE_STATEMENT                             = 1001;  // <move_statement>
//		final int SYM_MULTIPLE_FILE                              = 1002;  // <multiple_file>
//		final int SYM_MULTIPLE_FILE_LIST                         = 1003;  // <multiple_file_list>
//		final int SYM_MULTIPLE_FILE_TAPE_CLAUSE                  = 1004;  // <multiple_file_tape_clause>
//		final int SYM_MULTIPLY_BODY                              = 1005;  // <multiply_body>
//		final int SYM_MULTIPLY_STATEMENT                         = 1006;  // <multiply_statement>
//		final int SYM_NAMED_INPUT_CD_CLAUSE                      = 1007;  // <named_input_cd_clause>
//		final int SYM_NAMED_INPUT_CD_CLAUSES                     = 1008;  // <named_input_cd_clauses>
//		final int SYM_NAMED_I_O_CD_CLAUSE                        = 1009;  // <named_i_o_cd_clause>
//		final int SYM_NAMED_I_O_CD_CLAUSES                       = 1010;  // <named_i_o_cd_clauses>
//		final int SYM_NATIONAL_OF_FUNC                           = 1011;  // <NATIONAL_OF_FUNC>
//		final int SYM_NESTED_LIST                                = 1012;  // <nested_list>
//		final int SYM_NEXT_GROUP_CLAUSE                          = 1013;  // <next_group_clause>
//		final int SYM_NOISE                                      = 1014;  // <Noise>
//		final int SYM_NOISELIST                                  = 1015;  // <NoiseList>
//		final int SYM_NOT2                                       = 1016;  // <not>
//		final int SYM_NOT_AT_END_CLAUSE                          = 1017;  // <not_at_end_clause>
//		final int SYM_NOT_AT_EOP_CLAUSE                          = 1018;  // <not_at_eop_clause>
//		final int SYM_NOT_EQUAL_OP                               = 1019;  // <not_equal_op>
//		final int SYM_NOT_ESCAPE_OR_NOT_EXCEPTION                = 1020;  // <not_escape_or_not_exception>
//		final int SYM_NOT_INVALID_KEY_SENTENCE                   = 1021;  // <not_invalid_key_sentence>
//		final int SYM_NOT_ON_OVERFLOW                            = 1022;  // <not_on_overflow>
//		final int SYM_NOT_ON_SIZE_ERROR                          = 1023;  // <not_on_size_error>
//		final int SYM_NO_DATA_SENTENCE                           = 1024;  // <no_data_sentence>
//		final int SYM_NO_ECHO2                                   = 1025;  // <no_echo>
//		final int SYM_NO_OR_INTEGER                              = 1026;  // <no_or_integer>
//		final int SYM_NULL_OR_OMITTED                            = 1027;  // <null_or_omitted>
//		final int SYM_NUMERIC_IDENTIFIER                         = 1028;  // <numeric_identifier>
//		final int SYM_NUMERIC_SIGN_CLAUSE                        = 1029;  // <numeric_sign_clause>
//		final int SYM_NUMVALC_ARGS                               = 1030;  // <numvalc_args>
//		final int SYM_NUMVALC_FUNC                               = 1031;  // <NUMVALC_FUNC>
//		final int SYM_NUM_ID_OR_LIT                              = 1032;  // <num_id_or_lit>
//		final int SYM_OBJECT_CHAR_OR_WORD                        = 1033;  // <object_char_or_word>
//		final int SYM_OBJECT_CLAUSES                             = 1034;  // <object_clauses>
//		final int SYM_OBJECT_CLAUSES_LIST                        = 1035;  // <object_clauses_list>
//		final int SYM_OBJECT_COMPUTER_CLASS                      = 1036;  // <object_computer_class>
//		final int SYM_OBJECT_COMPUTER_MEMORY                     = 1037;  // <object_computer_memory>
//		final int SYM_OBJECT_COMPUTER_PARAGRAPH                  = 1038;  // <object_computer_paragraph>
//		final int SYM_OBJECT_COMPUTER_SEGMENT                    = 1039;  // <object_computer_segment>
//		final int SYM_OBJECT_COMPUTER_SEQUENCE                   = 1040;  // <object_computer_sequence>
//		final int SYM_OCCURS_CLAUSE                              = 1041;  // <occurs_clause>
//		final int SYM_OCCURS_INDEX                               = 1042;  // <occurs_index>
//		final int SYM_OCCURS_INDEXED                             = 1043;  // <occurs_indexed>
//		final int SYM_OCCURS_INDEX_LIST                          = 1044;  // <occurs_index_list>
//		final int SYM_OCCURS_KEYS                                = 1045;  // <occurs_keys>
//		final int SYM_OCCURS_KEY_FIELD                           = 1046;  // <occurs_key_field>
//		final int SYM_OCCURS_KEY_LIST                            = 1047;  // <occurs_key_list>
//		final int SYM_ON_OFF_CLAUSES                             = 1048;  // <on_off_clauses>
//		final int SYM_ON_OFF_CLAUSES_1                           = 1049;  // <on_off_clauses_1>
//		final int SYM_ON_OR_OFF                                  = 1050;  // <on_or_off>
//		final int SYM_ON_OVERFLOW                                = 1051;  // <on_overflow>
//		final int SYM_ON_SIZE_ERROR                              = 1052;  // <on_size_error>
//		final int SYM_ON_SIZE_ERROR_PHRASES                      = 1053;  // <on_size_error_phrases>
//		final int SYM_OPEN_BODY                                  = 1054;  // <open_body>
//		final int SYM_OPEN_FILE_ENTRY                            = 1055;  // <open_file_entry>
//		final int SYM_OPEN_MODE                                  = 1056;  // <open_mode>
//		final int SYM_OPEN_OPTION                                = 1057;  // <open_option>
//		final int SYM_OPEN_SHARING                               = 1058;  // <open_sharing>
//		final int SYM_OPEN_STATEMENT                             = 1059;  // <open_statement>
//		final int SYM_OPTIONAL_REFERENCE                         = 1060;  // <optional_reference>
//		final int SYM_OPTIONAL_REFERENCE_LIST                    = 1061;  // <optional_reference_list>
//		final int SYM_ORGANIZATION2                              = 1062;  // <organization>
//		final int SYM_ORGANIZATION_CLAUSE                        = 1063;  // <organization_clause>
//		final int SYM_OUTPUT_CD_CLAUSE                           = 1064;  // <output_cd_clause>
//		final int SYM_OUTPUT_CD_CLAUSES                          = 1065;  // <output_cd_clauses>
//		final int SYM_PADDING_CHARACTER_CLAUSE                   = 1066;  // <padding_character_clause>
//		final int SYM_PAGE_DETAIL                                = 1067;  // <page_detail>
//		final int SYM_PAGE_LIMIT_CLAUSE                          = 1068;  // <page_limit_clause>
//		final int SYM_PAGE_LINE_COLUMN                           = 1069;  // <page_line_column>
//		final int SYM_PARAGRAPH_HEADER                           = 1070;  // <paragraph_header>
//		final int SYM_PARTIAL_EXPR                               = 1071;  // <partial_expr>
//		final int SYM_PERFORM_BODY                               = 1072;  // <perform_body>
//		final int SYM_PERFORM_OPTION                             = 1073;  // <perform_option>
//		final int SYM_PERFORM_PROCEDURE                          = 1074;  // <perform_procedure>
//		final int SYM_PERFORM_STATEMENT                          = 1075;  // <perform_statement>
//		final int SYM_PERFORM_TEST                               = 1076;  // <perform_test>
//		final int SYM_PERFORM_VARYING                            = 1077;  // <perform_varying>
//		final int SYM_PERFORM_VARYING_LIST                       = 1078;  // <perform_varying_list>
//		final int SYM_PF_KEYWORD                                 = 1079;  // <pf_keyword>
//		final int SYM_PH_KEYWORD                                 = 1080;  // <ph_keyword>
//		final int SYM_PICTURE_CLAUSE                             = 1081;  // <picture_clause>
//		final int SYM_PLUS_PLUS                                  = 1082;  // <plus_plus>
//		final int SYM_POINTER_LEN                                = 1083;  // <pointer_len>
//		final int SYM_POSITIVE_ID_OR_LIT                         = 1084;  // <positive_id_or_lit>
//		final int SYM_POS_NUM_ID_OR_LIT                          = 1085;  // <pos_num_id_or_lit>
//		final int SYM_PRESENT_WHEN_CONDITION                     = 1086;  // <present_when_condition>
//		final int SYM_PRINTER_NAME                               = 1087;  // <printer_name>
//		final int SYM_PROCEDURE2                                 = 1088;  // <procedure>
//		final int SYM_PROCEDURE_NAME                             = 1089;  // <procedure_name>
//		final int SYM_PROCEDURE_NAME_LIST                        = 1090;  // <procedure_name_list>
//		final int SYM_PROCEDURE_PARAM                            = 1091;  // <procedure_param>
//		final int SYM_PROCEDURE_PARAM_LIST                       = 1092;  // <procedure_param_list>
//		final int SYM_PROGRAM_DEFINITION                         = 1093;  // <program_definition>
//		final int SYM_PROGRAM_ID_NAME                            = 1094;  // <program_id_name>
//		final int SYM_PROGRAM_ID_PARAGRAPH                       = 1095;  // <program_id_paragraph>
//		final int SYM_PROGRAM_NAME                               = 1096;  // <PROGRAM_NAME>
//		final int SYM_PROGRAM_OR_PROTOTYPE                       = 1097;  // <program_or_prototype>
//		final int SYM_PROGRAM_START_END                          = 1098;  // <program_start_end>
//		final int SYM_PROGRAM_TYPE_CLAUSE                        = 1099;  // <program_type_clause>
//		final int SYM_PROG_COLL_SEQUENCE                         = 1100;  // <prog_coll_sequence>
//		final int SYM_PROG_OR_ENTRY                              = 1101;  // <prog_or_entry>
//		final int SYM_PURGE_STATEMENT                            = 1102;  // <purge_statement>
//		final int SYM_QUALIFIED_WORD                             = 1103;  // <qualified_word>
//		final int SYM_READY_STATEMENT                            = 1104;  // <ready_statement>
//		final int SYM_READ_BODY                                  = 1105;  // <read_body>
//		final int SYM_READ_HANDLER                               = 1106;  // <read_handler>
//		final int SYM_READ_INTO                                  = 1107;  // <read_into>
//		final int SYM_READ_KEY                                   = 1108;  // <read_key>
//		final int SYM_READ_STATEMENT                             = 1109;  // <read_statement>
//		final int SYM_RECEIVE_BODY                               = 1110;  // <receive_body>
//		final int SYM_RECEIVE_STATEMENT                          = 1111;  // <receive_statement>
//		final int SYM_RECORDING_MODE                             = 1112;  // <recording_mode>
//		final int SYM_RECORDING_MODE_CLAUSE                      = 1113;  // <recording_mode_clause>
//		final int SYM_RECORDS2                                   = 1114;  // <records>
//		final int SYM_RECORD_CLAUSE                              = 1115;  // <record_clause>
//		final int SYM_RECORD_DELIMITER_CLAUSE                    = 1116;  // <record_delimiter_clause>
//		final int SYM_RECORD_DESCRIPTION_LIST                    = 1117;  // <record_description_list>
//		final int SYM_RECORD_KEY_CLAUSE                          = 1118;  // <record_key_clause>
//		final int SYM_RECORD_NAME                                = 1119;  // <record_name>
//		final int SYM_REDEFINES_CLAUSE                           = 1120;  // <redefines_clause>
//		final int SYM_REEL_OR_UNIT                               = 1121;  // <reel_or_unit>
//		final int SYM_REFERENCE2                                 = 1122;  // <reference>
//		final int SYM_REFERENCE_LIST                             = 1123;  // <reference_list>
//		final int SYM_REFERENCE_OR_LITERAL                       = 1124;  // <reference_or_literal>
//		final int SYM_REFMOD                                     = 1125;  // <refmod>
//		final int SYM_RELATIVE_KEY_CLAUSE                        = 1126;  // <relative_key_clause>
//		final int SYM_RELEASE_BODY                               = 1127;  // <release_body>
//		final int SYM_RELEASE_STATEMENT                          = 1128;  // <release_statement>
//		final int SYM_RENAMES_ENTRY                              = 1129;  // <renames_entry>
//		final int SYM_REPLACING_ITEM                             = 1130;  // <replacing_item>
//		final int SYM_REPLACING_LIST                             = 1131;  // <replacing_list>
//		final int SYM_REPLACING_REGION                           = 1132;  // <replacing_region>
//		final int SYM_REPORT_CLAUSE                              = 1133;  // <report_clause>
//		final int SYM_REPORT_COL_INTEGER_LIST                    = 1134;  // <report_col_integer_list>
//		final int SYM_REPORT_DESCRIPTION                         = 1135;  // <report_description>
//		final int SYM_REPORT_DESCRIPTION_OPTION                  = 1136;  // <report_description_option>
//		final int SYM_REPORT_GROUP_DESCRIPTION_ENTRY             = 1137;  // <report_group_description_entry>
//		final int SYM_REPORT_GROUP_OPTION                        = 1138;  // <report_group_option>
//		final int SYM_REPORT_INTEGER                             = 1139;  // <report_integer>
//		final int SYM_REPORT_KEYWORD                             = 1140;  // <report_keyword>
//		final int SYM_REPORT_LINE_INTEGER_LIST                   = 1141;  // <report_line_integer_list>
//		final int SYM_REPORT_NAME                                = 1142;  // <report_name>
//		final int SYM_REPORT_OCCURS_CLAUSE                       = 1143;  // <report_occurs_clause>
//		final int SYM_REPORT_USAGE_CLAUSE                        = 1144;  // <report_usage_clause>
//		final int SYM_REPORT_X_LIST                              = 1145;  // <report_x_list>
//		final int SYM_REPOSITORY_LIST                            = 1146;  // <repository_list>
//		final int SYM_REPOSITORY_NAME                            = 1147;  // <repository_name>
//		final int SYM_REPOSITORY_NAME_LIST                       = 1148;  // <repository_name_list>
//		final int SYM_REP_KEYWORD                                = 1149;  // <rep_keyword>
//		final int SYM_REP_NAME_LIST                              = 1150;  // <rep_name_list>
//		final int SYM_RESERVE_CLAUSE                             = 1151;  // <reserve_clause>
//		final int SYM_RESET_STATEMENT                            = 1152;  // <reset_statement>
//		final int SYM_RETRY_OPTIONS                              = 1153;  // <retry_options>
//		final int SYM_RETRY_PHRASE                               = 1154;  // <retry_phrase>
//		final int SYM_RETURN_AT_END                              = 1155;  // <return_at_end>
//		final int SYM_RETURN_BODY                                = 1156;  // <return_body>
//		final int SYM_RETURN_GIVE                                = 1157;  // <return_give>
//		final int SYM_RETURN_STATEMENT                           = 1158;  // <return_statement>
//		final int SYM_REVERSE_FUNC                               = 1159;  // <REVERSE_FUNC>
//		final int SYM_REVERSE_VIDEO2                             = 1160;  // <reverse_video>
//		final int SYM_REWRITE_BODY                               = 1161;  // <rewrite_body>
//		final int SYM_REWRITE_STATEMENT                          = 1162;  // <rewrite_statement>
//		final int SYM_RF_KEYWORD                                 = 1163;  // <rf_keyword>
//		final int SYM_RH_KEYWORD                                 = 1164;  // <rh_keyword>
//		final int SYM_ROLLBACK_STATEMENT                         = 1165;  // <rollback_statement>
//		final int SYM_ROUND_CHOICE                               = 1166;  // <round_choice>
//		final int SYM_ROUND_MODE                                 = 1167;  // <round_mode>
//		final int SYM_SAME_CLAUSE                                = 1168;  // <same_clause>
//		final int SYM_SCOPE_TERMINATOR                           = 1169;  // <scope_terminator>
//		final int SYM_SCREEN_COL_NUMBER                          = 1170;  // <screen_col_number>
//		final int SYM_SCREEN_CONTROL2                            = 1171;  // <screen_control>
//		final int SYM_SCREEN_DESCRIPTION                         = 1172;  // <screen_description>
//		final int SYM_SCREEN_DESCRIPTION_LIST                    = 1173;  // <screen_description_list>
//		final int SYM_SCREEN_GLOBAL_CLAUSE                       = 1174;  // <screen_global_clause>
//		final int SYM_SCREEN_LINE_NUMBER                         = 1175;  // <screen_line_number>
//		final int SYM_SCREEN_OCCURS_CLAUSE                       = 1176;  // <screen_occurs_clause>
//		final int SYM_SCREEN_OPTION                              = 1177;  // <screen_option>
//		final int SYM_SCREEN_OR_DEVICE_DISPLAY                   = 1178;  // <screen_or_device_display>
//		final int SYM_SCROLL_LINE_OR_LINES                       = 1179;  // <scroll_line_or_lines>
//		final int SYM_SEARCH_AT_END                              = 1180;  // <search_at_end>
//		final int SYM_SEARCH_BODY                                = 1181;  // <search_body>
//		final int SYM_SEARCH_STATEMENT                           = 1182;  // <search_statement>
//		final int SYM_SEARCH_VARYING                             = 1183;  // <search_varying>
//		final int SYM_SEARCH_WHEN                                = 1184;  // <search_when>
//		final int SYM_SEARCH_WHENS                               = 1185;  // <search_whens>
//		final int SYM_SECTION_HEADER                             = 1186;  // <section_header>
//		final int SYM_SELECT_CLAUSE                              = 1187;  // <select_clause>
//		final int SYM_SEND_BODY                                  = 1188;  // <send_body>
//		final int SYM_SEND_IDENTIFIER                            = 1189;  // <send_identifier>
//		final int SYM_SEND_STATEMENT                             = 1190;  // <send_statement>
//		final int SYM_SET_ATTR                                   = 1191;  // <set_attr>
//		final int SYM_SET_ATTR_CLAUSE                            = 1192;  // <set_attr_clause>
//		final int SYM_SET_ATTR_ONE                               = 1193;  // <set_attr_one>
//		final int SYM_SET_BODY                                   = 1194;  // <set_body>
//		final int SYM_SET_ENVIRONMENT                            = 1195;  // <set_environment>
//		final int SYM_SET_LAST_EXCEPTION_TO_OFF                  = 1196;  // <set_last_exception_to_off>
//		final int SYM_SET_STATEMENT                              = 1197;  // <set_statement>
//		final int SYM_SET_TO                                     = 1198;  // <set_to>
//		final int SYM_SET_TO_ON_OFF                              = 1199;  // <set_to_on_off>
//		final int SYM_SET_TO_ON_OFF_SEQUENCE                     = 1200;  // <set_to_on_off_sequence>
//		final int SYM_SET_TO_TRUE_FALSE                          = 1201;  // <set_to_true_false>
//		final int SYM_SET_TO_TRUE_FALSE_SEQUENCE                 = 1202;  // <set_to_true_false_sequence>
//		final int SYM_SET_UP_DOWN                                = 1203;  // <set_up_down>
//		final int SYM_SHARING_CLAUSE                             = 1204;  // <sharing_clause>
//		final int SYM_SHARING_OPTION                             = 1205;  // <sharing_option>
//		final int SYM_SIGN_CLAUSE                                = 1206;  // <sign_clause>
//		final int SYM_SIMPLE_ALL_VALUE                           = 1207;  // <simple_all_value>
//		final int SYM_SIMPLE_DISPLAY_ALL_VALUE                   = 1208;  // <simple_display_all_value>
//		final int SYM_SIMPLE_DISPLAY_VALUE                       = 1209;  // <simple_display_value>
//		final int SYM_SIMPLE_PROG                                = 1210;  // <simple_prog>
//		final int SYM_SIMPLE_VALUE                               = 1211;  // <simple_value>
//		final int SYM_SINGLE_REFERENCE                           = 1212;  // <single_reference>
//		final int SYM_SIZELEN_CLAUSE                             = 1213;  // <sizelen_clause>
//		final int SYM_SIZE_IS_INTEGER                            = 1214;  // <size_is_integer>
//		final int SYM_SIZE_OR_LENGTH                             = 1215;  // <size_or_length>
//		final int SYM_SORT_BODY                                  = 1216;  // <sort_body>
//		final int SYM_SORT_COLLATING                             = 1217;  // <sort_collating>
//		final int SYM_SORT_INPUT                                 = 1218;  // <sort_input>
//		final int SYM_SORT_KEY_LIST                              = 1219;  // <sort_key_list>
//		final int SYM_SORT_OUTPUT                                = 1220;  // <sort_output>
//		final int SYM_SORT_STATEMENT                             = 1221;  // <sort_statement>
//		final int SYM_SOURCE_CLAUSE                              = 1222;  // <source_clause>
//		final int SYM_SOURCE_COMPUTER_PARAGRAPH                  = 1223;  // <source_computer_paragraph>
//		final int SYM_SOURCE_ELEMENT                             = 1224;  // <source_element>
//		final int SYM_SOURCE_ELEMENT_LIST                        = 1225;  // <source_element_list>
//		final int SYM_SPACE_OR_ZERO                              = 1226;  // <space_or_zero>
//		final int SYM_SPECIAL_NAME                               = 1227;  // <special_name>
//		final int SYM_SPECIAL_NAMES_SENTENCE_LIST                = 1228;  // <special_names_sentence_list>
//		final int SYM_SPECIAL_NAME_LIST                          = 1229;  // <special_name_list>
//		final int SYM_START2                                     = 1230;  // <start>
//		final int SYM_START_BODY                                 = 1231;  // <start_body>
//		final int SYM_START_KEY                                  = 1232;  // <start_key>
//		final int SYM_START_OP                                   = 1233;  // <start_op>
//		final int SYM_START_STATEMENT                            = 1234;  // <start_statement>
//		final int SYM_STATEMENT                                  = 1235;  // <statement>
//		final int SYM_STATEMENTS                                 = 1236;  // <statements>
//		final int SYM_STATEMENT_LIST                             = 1237;  // <statement_list>
//		final int SYM_STOP_LITERAL                               = 1238;  // <stop_literal>
//		final int SYM_STOP_RETURNING                             = 1239;  // <stop_returning>
//		final int SYM_STOP_STATEMENT                             = 1240;  // <stop_statement>
//		final int SYM_STRING_BODY                                = 1241;  // <string_body>
//		final int SYM_STRING_DELIMITER                           = 1242;  // <string_delimiter>
//		final int SYM_STRING_ITEM                                = 1243;  // <string_item>
//		final int SYM_STRING_ITEM_LIST                           = 1244;  // <string_item_list>
//		final int SYM_STRING_STATEMENT                           = 1245;  // <string_statement>
//		final int SYM_SUBREF                                     = 1246;  // <subref>
//		final int SYM_SUBSTITUTE_CASE_FUNC                       = 1247;  // <SUBSTITUTE_CASE_FUNC>
//		final int SYM_SUBSTITUTE_FUNC                            = 1248;  // <SUBSTITUTE_FUNC>
//		final int SYM_SUBTRACT_BODY                              = 1249;  // <subtract_body>
//		final int SYM_SUBTRACT_STATEMENT                         = 1250;  // <subtract_statement>
//		final int SYM_SUB_IDENTIFIER                             = 1251;  // <sub_identifier>
//		final int SYM_SUB_IDENTIFIER_1                           = 1252;  // <sub_identifier_1>
//		final int SYM_SUM_CLAUSE_LIST                            = 1253;  // <sum_clause_list>
//		final int SYM_SUPPRESS_STATEMENT                         = 1254;  // <suppress_statement>
//		final int SYM_SYMBOLIC_CHARACTERS_CLAUSE                 = 1255;  // <symbolic_characters_clause>
//		final int SYM_SYMBOLIC_CHARS_LIST                        = 1256;  // <symbolic_chars_list>
//		final int SYM_SYMBOLIC_CHARS_PHRASE                      = 1257;  // <symbolic_chars_phrase>
//		final int SYM_SYMBOLIC_COLLECTION                        = 1258;  // <symbolic_collection>
//		final int SYM_SYMBOLIC_INTEGER                           = 1259;  // <symbolic_integer>
//		final int SYM_SYNCHRONIZED_CLAUSE                        = 1260;  // <synchronized_clause>
//		final int SYM_TABLE_IDENTIFIER                           = 1261;  // <table_identifier>
//		final int SYM_TABLE_NAME                                 = 1262;  // <table_name>
//		final int SYM_TALLYING_ITEM                              = 1263;  // <tallying_item>
//		final int SYM_TALLYING_LIST                              = 1264;  // <tallying_list>
//		final int SYM_TARGET_IDENTIFIER                          = 1265;  // <target_identifier>
//		final int SYM_TARGET_IDENTIFIER_1                        = 1266;  // <target_identifier_1>
//		final int SYM_TARGET_X                                   = 1267;  // <target_x>
//		final int SYM_TARGET_X_LIST                              = 1268;  // <target_x_list>
//		final int SYM_TERMINATE_BODY                             = 1269;  // <terminate_body>
//		final int SYM_TERMINATE_STATEMENT                        = 1270;  // <terminate_statement>
//		final int SYM_TERM_OR_DOT                                = 1271;  // <term_or_dot>
//		final int SYM_TO_INIT_VAL                                = 1272;  // <to_init_val>
//		final int SYM_TRANSFORM_BODY                             = 1273;  // <transform_body>
//		final int SYM_TRANSFORM_STATEMENT                        = 1274;  // <transform_statement>
//		final int SYM_TRIM_ARGS                                  = 1275;  // <trim_args>
//		final int SYM_TRIM_FUNC                                  = 1276;  // <TRIM_FUNC>
//		final int SYM_TYPE_CLAUSE                                = 1277;  // <type_clause>
//		final int SYM_TYPE_OPTION                                = 1278;  // <type_option>
//		final int SYM_UNDEFINED_WORD                             = 1279;  // <undefined_word>
//		final int SYM_UNIQUE_WORD                                = 1280;  // <unique_word>
//		final int SYM_UNLOCK_BODY                                = 1281;  // <unlock_body>
//		final int SYM_UNLOCK_STATEMENT                           = 1282;  // <unlock_statement>
//		final int SYM_UNNAMED_INPUT_CD_CLAUSES                   = 1283;  // <unnamed_input_cd_clauses>
//		final int SYM_UNNAMED_I_O_CD_CLAUSES                     = 1284;  // <unnamed_i_o_cd_clauses>
//		final int SYM_UNSTRING_BODY                              = 1285;  // <unstring_body>
//		final int SYM_UNSTRING_DELIMITED_ITEM                    = 1286;  // <unstring_delimited_item>
//		final int SYM_UNSTRING_DELIMITED_LIST                    = 1287;  // <unstring_delimited_list>
//		final int SYM_UNSTRING_INTO                              = 1288;  // <unstring_into>
//		final int SYM_UNSTRING_INTO_ITEM                         = 1289;  // <unstring_into_item>
//		final int SYM_UNSTRING_STATEMENT                         = 1290;  // <unstring_statement>
//		final int SYM_UPDATE_DEFAULT                             = 1291;  // <update_default>
//		final int SYM_UPPER_CASE_FUNC                            = 1292;  // <UPPER_CASE_FUNC>
//		final int SYM_UP_OR_DOWN                                 = 1293;  // <up_or_down>
//		final int SYM_USAGE2                                     = 1294;  // <usage>
//		final int SYM_USAGE_CLAUSE                               = 1295;  // <usage_clause>
//		final int SYM_USER_ENTRY_NAME                            = 1296;  // <user_entry_name>
//		final int SYM_USER_FUNCTION_NAME                         = 1297;  // <USER_FUNCTION_NAME>
//		final int SYM_USE_DEBUGGING                              = 1298;  // <use_debugging>
//		final int SYM_USE_EXCEPTION                              = 1299;  // <use_exception>
//		final int SYM_USE_EX_KEYW                                = 1300;  // <use_ex_keyw>
//		final int SYM_USE_FILE_EXCEPTION                         = 1301;  // <use_file_exception>
//		final int SYM_USE_FILE_EXCEPTION_TARGET                  = 1302;  // <use_file_exception_target>
//		final int SYM_USE_GLOBAL                                 = 1303;  // <use_global>
//		final int SYM_USE_PHRASE                                 = 1304;  // <use_phrase>
//		final int SYM_USE_REPORTING                              = 1305;  // <use_reporting>
//		final int SYM_USE_START_END                              = 1306;  // <use_start_end>
//		final int SYM_USE_STATEMENT                              = 1307;  // <use_statement>
//		final int SYM_U_OR_S                                     = 1308;  // <u_or_s>
//		final int SYM_VALUEOF_NAME                               = 1309;  // <valueof_name>
//		final int SYM_VALUE_CLAUSE                               = 1310;  // <value_clause>
//		final int SYM_VALUE_ITEM                                 = 1311;  // <value_item>
//		final int SYM_VALUE_ITEM_LIST                            = 1312;  // <value_item_list>
//		final int SYM_VALUE_OF_CLAUSE                            = 1313;  // <value_of_clause>
//		final int SYM_VARYING_CLAUSE                             = 1314;  // <varying_clause>
//		final int SYM_VERB                                       = 1315;  // <verb>
//		final int SYM_WHEN_COMPILED_FUNC                         = 1316;  // <WHEN_COMPILED_FUNC>
//		final int SYM_WITH_DATA_SENTENCE                         = 1317;  // <with_data_sentence>
//		final int SYM_WITH_DUPS                                  = 1318;  // <with_dups>
//		final int SYM_WITH_INDICATOR                             = 1319;  // <with_indicator>
//		final int SYM_WITH_LOCK                                  = 1320;  // <with_lock>
//		final int SYM_WORD                                       = 1321;  // <WORD>
//		final int SYM_WRITE_BODY                                 = 1322;  // <write_body>
//		final int SYM_WRITE_HANDLER                              = 1323;  // <write_handler>
//		final int SYM_WRITE_OPTION                               = 1324;  // <write_option>
//		final int SYM_WRITE_STATEMENT                            = 1325;  // <write_statement>
//		final int SYM_X                                          = 1326;  // <x>
//		final int SYM_X_COMMON                                   = 1327;  // <x_common>
//		final int SYM_X_LIST                                     = 1328;  // <x_list>
//		final int SYM__ACCEPT_CLAUSES                            = 1329;  // <_accept_clauses>
//		final int SYM__ACCEPT_EXCEPTION_PHRASES                  = 1330;  // <_accept_exception_phrases>
//		final int SYM__ACCP_NOT_ON_EXCEPTION                     = 1331;  // <_accp_not_on_exception>
//		final int SYM__ACCP_ON_EXCEPTION                         = 1332;  // <_accp_on_exception>
//		final int SYM__ADD_TO                                    = 1333;  // <_add_to>
//		final int SYM__ADVANCING                                 = 1334;  // <_advancing>
//		final int SYM__AFTER                                     = 1335;  // <_after>
//		final int SYM__ALL_REFS                                  = 1336;  // <_all_refs>
//		final int SYM__ARE                                       = 1337;  // <_are>
//		final int SYM__AREA                                      = 1338;  // <_area>
//		final int SYM__AREAS                                     = 1339;  // <_areas>
//		final int SYM__AS                                        = 1340;  // <_as>
//		final int SYM__ASSIGNMENT_NAME                           = 1341;  // <_assignment_name>
//		final int SYM__AS_EXTNAME                                = 1342;  // <_as_extname>
//		final int SYM__AS_LITERAL                                = 1343;  // <_as_literal>
//		final int SYM__AT                                        = 1344;  // <_at>
//		final int SYM__AT_END_CLAUSE                             = 1345;  // <_at_end_clause>
//		final int SYM__AT_EOP_CLAUSE                             = 1346;  // <_at_eop_clause>
//		final int SYM__BEFORE                                    = 1347;  // <_before>
//		final int SYM__BINARY                                    = 1348;  // <_binary>
//		final int SYM__BY                                        = 1349;  // <_by>
//		final int SYM__CALL_NOT_ON_EXCEPTION                     = 1350;  // <_call_not_on_exception>
//		final int SYM__CALL_ON_EXCEPTION                         = 1351;  // <_call_on_exception>
//		final int SYM__CAPACITY_IN                               = 1352;  // <_capacity_in>
//		final int SYM__CHARACTER                                 = 1353;  // <_character>
//		final int SYM__CHARACTERS                                = 1354;  // <_characters>
//		final int SYM__COMMENTITEMS                              = 1355;  // <_Comment Items>
//		final int SYM__COMMUNICATION_DESCRIPTION_CLAUSE_SEQUENCE = 1356;  // <_communication_description_clause_sequence>
//		final int SYM__COMMUNICATION_DESCRIPTION_SEQUENCE        = 1357;  // <_communication_description_sequence>
//		final int SYM__COMMUNICATION_SECTION                     = 1358;  // <_communication_section>
//		final int SYM__CONFIGURATION_HEADER                      = 1359;  // <_configuration_header>
//		final int SYM__CONFIGURATION_SECTION                     = 1360;  // <_configuration_section>
//		final int SYM__CONTAINS                                  = 1361;  // <_contains>
//		final int SYM__CONTROL_FINAL                             = 1362;  // <_control_final>
//		final int SYM__DATA                                      = 1363;  // <_data>
//		final int SYM__DATA_DESCRIPTION_CLAUSE_SEQUENCE          = 1364;  // <_data_description_clause_sequence>
//		final int SYM__DATA_DIVISION                             = 1365;  // <_data_division>
//		final int SYM__DATA_DIVISION_HEADER                      = 1366;  // <_data_division_header>
//		final int SYM__DATA_SENTENCE_PHRASES                     = 1367;  // <_data_sentence_phrases>
//		final int SYM__DEFAULT_ROUNDED_CLAUSE                    = 1368;  // <_default_rounded_clause>
//		final int SYM__DEST_INDEX                                = 1369;  // <_dest_index>
//		final int SYM__DISPLAY_EXCEPTION_PHRASES                 = 1370;  // <_display_exception_phrases>
//		final int SYM__DISP_NOT_ON_EXCEPTION                     = 1371;  // <_disp_not_on_exception>
//		final int SYM__DISP_ON_EXCEPTION                         = 1372;  // <_disp_on_exception>
//		final int SYM__ENABLE_DISABLE_KEY                        = 1373;  // <_enable_disable_key>
//		final int SYM__END_OF                                    = 1374;  // <_end_of>
//		final int SYM__END_PROGRAM_LIST                          = 1375;  // <_end_program_list>
//		final int SYM__ENTRY_CONVENTION_CLAUSE                   = 1376;  // <_entry_convention_clause>
//		final int SYM__ENTRY_NAME                                = 1377;  // <_entry_name>
//		final int SYM__ENVIRONMENT_DIVISION                      = 1378;  // <_environment_division>
//		final int SYM__ENVIRONMENT_HEADER                        = 1379;  // <_environment_header>
//		final int SYM__EVALUATE_THRU_EXPR                        = 1380;  // <_evaluate_thru_expr>
//		final int SYM__EXTENDED_WITH_LOCK                        = 1381;  // <_extended_with_lock>
//		final int SYM__EXT_CLAUSE                                = 1382;  // <_ext_clause>
//		final int SYM__E_SEP                                     = 1383;  // <_e_sep>
//		final int SYM__FALSE_IS                                  = 1384;  // <_false_is>
//		final int SYM__FILE                                      = 1385;  // <_file>
//		final int SYM__FILE_CONTROL_HEADER                       = 1386;  // <_file_control_header>
//		final int SYM__FILE_CONTROL_SEQUENCE                     = 1387;  // <_file_control_sequence>
//		final int SYM__FILE_DESCRIPTION_CLAUSE_SEQUENCE          = 1388;  // <_file_description_clause_sequence>
//		final int SYM__FILE_DESCRIPTION_SEQUENCE                 = 1389;  // <_file_description_sequence>
//		final int SYM__FILE_OR_SORT                              = 1390;  // <_file_or_sort>
//		final int SYM__FILE_SECTION_HEADER                       = 1391;  // <_file_section_header>
//		final int SYM__FILLER                                    = 1392;  // <_filler>
//		final int SYM__FINAL                                     = 1393;  // <_final>
//		final int SYM__FLAG_NEXT                                 = 1394;  // <_flag_next>
//		final int SYM__FLAG_NOT                                  = 1395;  // <_flag_not>
//		final int SYM__FOR                                       = 1396;  // <_for>
//		final int SYM__FOR_SUB_RECORDS_CLAUSE                    = 1397;  // <_for_sub_records_clause>
//		final int SYM__FROM                                      = 1398;  // <_from>
//		final int SYM__FROM_IDENTIFIER                           = 1399;  // <_from_identifier>
//		final int SYM__FROM_IDX_TO_IDX                           = 1400;  // <_from_idx_to_idx>
//		final int SYM__FROM_INTEGER                              = 1401;  // <_from_integer>
//		final int SYM__GLOBAL_CLAUSE                             = 1402;  // <_global_clause>
//		final int SYM__IDENTIFICATION_HEADER                     = 1403;  // <_identification_header>
//		final int SYM__IN                                        = 1404;  // <_in>
//		final int SYM__INDEX                                     = 1405;  // <_index>
//		final int SYM__INDICATE                                  = 1406;  // <_indicate>
//		final int SYM__INITIAL                                   = 1407;  // <_initial>
//		final int SYM__INITIALIZE_DEFAULT                        = 1408;  // <_initialize_default>
//		final int SYM__INITIALIZE_FILLER                         = 1409;  // <_initialize_filler>
//		final int SYM__INITIALIZE_REPLACING                      = 1410;  // <_initialize_replacing>
//		final int SYM__INITIALIZE_VALUE                          = 1411;  // <_initialize_value>
//		final int SYM__INPUT_CD_CLAUSES                          = 1412;  // <_input_cd_clauses>
//		final int SYM__INPUT_OUTPUT_HEADER                       = 1413;  // <_input_output_header>
//		final int SYM__INPUT_OUTPUT_SECTION                      = 1414;  // <_input_output_section>
//		final int SYM__INTERMEDIATE_ROUNDING_CLAUSE              = 1415;  // <_intermediate_rounding_clause>
//		final int SYM__INTO                                      = 1416;  // <_into>
//		final int SYM__INVALID_KEY_PHRASES                       = 1417;  // <_invalid_key_phrases>
//		final int SYM__INVALID_KEY_SENTENCE                      = 1418;  // <_invalid_key_sentence>
//		final int SYM__IN_ORDER                                  = 1419;  // <_in_order>
//		final int SYM__IS                                        = 1420;  // <_is>
//		final int SYM__IS_ARE                                    = 1421;  // <_is_are>
//		final int SYM__I_O_CD_CLAUSES                            = 1422;  // <_i_o_cd_clauses>
//		final int SYM__I_O_CONTROL                               = 1423;  // <_i_o_control>
//		final int SYM__I_O_CONTROL_HEADER                        = 1424;  // <_i_o_control_header>
//		final int SYM__KEY                                       = 1425;  // <_key>
//		final int SYM__KEY_LIST                                  = 1426;  // <_key_list>
//		final int SYM__LEFT_OR_RIGHT                             = 1427;  // <_left_or_right>
//		final int SYM__LIMITS                                    = 1428;  // <_limits>
//		final int SYM__LINAGE_SEQUENCE                           = 1429;  // <_linage_sequence>
//		final int SYM__LINE                                      = 1430;  // <_line>
//		final int SYM__LINES                                     = 1431;  // <_lines>
//		final int SYM__LINE_ADV_FILE                             = 1432;  // <_line_adv_file>
//		final int SYM__LINE_OR_LINES                             = 1433;  // <_line_or_lines>
//		final int SYM__LINKAGE_SECTION                           = 1434;  // <_linkage_section>
//		final int SYM__LOCAL_STORAGE_SECTION                     = 1435;  // <_local_storage_section>
//		final int SYM__LOCK_WITH                                 = 1436;  // <_lock_with>
//		final int SYM__MESSAGE                                   = 1437;  // <_message>
//		final int SYM__MNEMONIC_CONV                             = 1438;  // <_mnemonic_conv>
//		final int SYM__MODE                                      = 1439;  // <_mode>
//		final int SYM__MULTIPLE_FILE_POSITION                    = 1440;  // <_multiple_file_position>
//		final int SYM__NOT                                       = 1441;  // <_not>
//		final int SYM__NOT_AT_END_CLAUSE                         = 1442;  // <_not_at_end_clause>
//		final int SYM__NOT_AT_EOP_CLAUSE                         = 1443;  // <_not_at_eop_clause>
//		final int SYM__NOT_INVALID_KEY_SENTENCE                  = 1444;  // <_not_invalid_key_sentence>
//		final int SYM__NOT_ON_OVERFLOW                           = 1445;  // <_not_on_overflow>
//		final int SYM__NOT_ON_SIZE_ERROR                         = 1446;  // <_not_on_size_error>
//		final int SYM__NO_DATA_SENTENCE                          = 1447;  // <_no_data_sentence>
//		final int SYM__NUMBER                                    = 1448;  // <_number>
//		final int SYM__NUMBERS                                   = 1449;  // <_numbers>
//		final int SYM__OBJECT_COMPUTER_ENTRY                     = 1450;  // <_object_computer_entry>
//		final int SYM__OCCURS_DEPENDING                          = 1451;  // <_occurs_depending>
//		final int SYM__OCCURS_FROM_INTEGER                       = 1452;  // <_occurs_from_integer>
//		final int SYM__OCCURS_INDEXED                            = 1453;  // <_occurs_indexed>
//		final int SYM__OCCURS_INITIALIZED                        = 1454;  // <_occurs_initialized>
//		final int SYM__OCCURS_INTEGER_TO                         = 1455;  // <_occurs_integer_to>
//		final int SYM__OCCURS_KEYS_AND_INDEXED                   = 1456;  // <_occurs_keys_and_indexed>
//		final int SYM__OCCURS_STEP                               = 1457;  // <_occurs_step>
//		final int SYM__OCCURS_TO_INTEGER                         = 1458;  // <_occurs_to_integer>
//		final int SYM__OF                                        = 1459;  // <_of>
//		final int SYM__ON                                        = 1460;  // <_on>
//		final int SYM__ONOFF_STATUS                              = 1461;  // <_onoff_status>
//		final int SYM__ON_OVERFLOW                               = 1462;  // <_on_overflow>
//		final int SYM__ON_OVERFLOW_PHRASES                       = 1463;  // <_on_overflow_phrases>
//		final int SYM__ON_SIZE_ERROR                             = 1464;  // <_on_size_error>
//		final int SYM__OPTIONS_CLAUSES                           = 1465;  // <_options_clauses>
//		final int SYM__OPTIONS_PARAGRAPH                         = 1466;  // <_options_paragraph>
//		final int SYM__OR_PAGE                                   = 1467;  // <_or_page>
//		final int SYM__OTHER                                     = 1468;  // <_other>
//		final int SYM__OUTPUT_CD_CLAUSES                         = 1469;  // <_output_cd_clauses>
//		final int SYM__PAGE_HEADING_LIST                         = 1470;  // <_page_heading_list>
//		final int SYM__PRINTING                                  = 1471;  // <_printing>
//		final int SYM__PROCEDURE                                 = 1472;  // <_procedure>
//		final int SYM__PROCEDURE_DECLARATIVES                    = 1473;  // <_procedure_declaratives>
//		final int SYM__PROCEDURE_DIVISION                        = 1474;  // <_procedure_division>
//		final int SYM__PROCEDURE_LIST                            = 1475;  // <_procedure_list>
//		final int SYM__PROCEDURE_OPTIONAL                        = 1476;  // <_procedure_optional>
//		final int SYM__PROCEDURE_RETURNING                       = 1477;  // <_procedure_returning>
//		final int SYM__PROCEDURE_TYPE                            = 1478;  // <_procedure_type>
//		final int SYM__PROCEDURE_USING_CHAINING                  = 1479;  // <_procedure_using_chaining>
//		final int SYM__PROCEED_TO                                = 1480;  // <_proceed_to>
//		final int SYM__PROGRAM                                   = 1481;  // <_program>
//		final int SYM__PROGRAM_BODY                              = 1482;  // <_program_body>
//		final int SYM__PROGRAM_TYPE                              = 1483;  // <_program_type>
//		final int SYM__RECORD                                    = 1484;  // <_record>
//		final int SYM__RECORDS                                   = 1485;  // <_records>
//		final int SYM__RECORDS_OR_CHARACTERS                     = 1486;  // <_records_or_characters>
//		final int SYM__RECORD_DEPENDING                          = 1487;  // <_record_depending>
//		final int SYM__RECORD_DESCRIPTION_LIST                   = 1488;  // <_record_description_list>
//		final int SYM__RENAMES_THRU                              = 1489;  // <_renames_thru>
//		final int SYM__REPLACING_LINE                            = 1490;  // <_replacing_line>
//		final int SYM__REPORT_DESCRIPTION_OPTIONS                = 1491;  // <_report_description_options>
//		final int SYM__REPORT_DESCRIPTION_SEQUENCE               = 1492;  // <_report_description_sequence>
//		final int SYM__REPORT_GROUP_DESCRIPTION_LIST             = 1493;  // <_report_group_description_list>
//		final int SYM__REPORT_GROUP_OPTIONS                      = 1494;  // <_report_group_options>
//		final int SYM__REPORT_SECTION                            = 1495;  // <_report_section>
//		final int SYM__REPOSITORY_ENTRY                          = 1496;  // <_repository_entry>
//		final int SYM__REPOSITORY_PARAGRAPH                      = 1497;  // <_repository_paragraph>
//		final int SYM__RESET_CLAUSE                              = 1498;  // <_reset_clause>
//		final int SYM__RETRY_PHRASE                              = 1499;  // <_retry_phrase>
//		final int SYM__RIGHT                                     = 1500;  // <_right>
//		final int SYM__SAME_OPTION                               = 1501;  // <_same_option>
//		final int SYM__SCREEN_COL_PLUS_MINUS                     = 1502;  // <_screen_col_plus_minus>
//		final int SYM__SCREEN_DESCRIPTION_LIST                   = 1503;  // <_screen_description_list>
//		final int SYM__SCREEN_LINE_PLUS_MINUS                    = 1504;  // <_screen_line_plus_minus>
//		final int SYM__SCREEN_OPTIONS                            = 1505;  // <_screen_options>
//		final int SYM__SCREEN_SECTION                            = 1506;  // <_screen_section>
//		final int SYM__SCROLL_LINES                              = 1507;  // <_scroll_lines>
//		final int SYM__SEGMENT                                   = 1508;  // <_segment>
//		final int SYM__SELECT_CLAUSES_OR_ERROR                   = 1509;  // <_select_clauses_or_error>
//		final int SYM__SELECT_CLAUSE_SEQUENCE                    = 1510;  // <_select_clause_sequence>
//		final int SYM__SIGN                                      = 1511;  // <_sign>
//		final int SYM__SIGNED                                    = 1512;  // <_signed>
//		final int SYM__SIGN_IS                                   = 1513;  // <_sign_is>
//		final int SYM__SIZE                                      = 1514;  // <_size>
//		final int SYM__SIZE_OPTIONAL                             = 1515;  // <_size_optional>
//		final int SYM__SORT_DUPLICATES                           = 1516;  // <_sort_duplicates>
//		final int SYM__SOURCE_COMPUTER_ENTRY                     = 1517;  // <_source_computer_entry>
//		final int SYM__SOURCE_OBJECT_COMPUTER_PARAGRAPHS         = 1518;  // <_source_object_computer_paragraphs>
//		final int SYM__SPECIAL_NAMES_PARAGRAPH                   = 1519;  // <_special_names_paragraph>
//		final int SYM__SPECIAL_NAMES_SENTENCE_LIST               = 1520;  // <_special_names_sentence_list>
//		final int SYM__SPECIAL_NAME_MNEMONIC_ON_OFF              = 1521;  // <_special_name_mnemonic_on_off>
//		final int SYM__STANDARD                                  = 1522;  // <_standard>
//		final int SYM__STATUS                                    = 1523;  // <_status>
//		final int SYM__STATUS_X                                  = 1524;  // <_status_x>
//		final int SYM__STRING_DELIMITED                          = 1525;  // <_string_delimited>
//		final int SYM__SUPPRESS_CLAUSE                           = 1526;  // <_suppress_clause>
//		final int SYM__SYMBOLIC                                  = 1527;  // <_symbolic>
//		final int SYM__SYM_IN_WORD                               = 1528;  // <_sym_in_word>
//		final int SYM__TAPE                                      = 1529;  // <_tape>
//		final int SYM__TERMINAL                                  = 1530;  // <_terminal>
//		final int SYM__THEN                                      = 1531;  // <_then>
//		final int SYM__TIMES                                     = 1532;  // <_times>
//		final int SYM__TO                                        = 1533;  // <_to>
//		final int SYM__TO_INTEGER                                = 1534;  // <_to_integer>
//		final int SYM__TO_USING                                  = 1535;  // <_to_using>
//		final int SYM__UNSTRING_DELIMITED                        = 1536;  // <_unstring_delimited>
//		final int SYM__UNSTRING_INTO_COUNT                       = 1537;  // <_unstring_into_count>
//		final int SYM__UNSTRING_INTO_DELIMITER                   = 1538;  // <_unstring_into_delimiter>
//		final int SYM__UNSTRING_TALLYING                         = 1539;  // <_unstring_tallying>
//		final int SYM__USE_STATEMENT                             = 1540;  // <_use_statement>
//		final int SYM__WHEN                                      = 1541;  // <_when>
//		final int SYM__WHEN_SET_TO                               = 1542;  // <_when_set_to>
//		final int SYM__WITH                                      = 1543;  // <_with>
//		final int SYM__WITH_DATA_SENTENCE                        = 1544;  // <_with_data_sentence>
//		final int SYM__WITH_DEBUGGING_MODE                       = 1545;  // <_with_debugging_mode>
//		final int SYM__WITH_LOCK                                 = 1546;  // <_with_lock>
//		final int SYM__WITH_PIC_SYMBOL                           = 1547;  // <_with_pic_symbol>
//		final int SYM__WITH_POINTER                              = 1548;  // <_with_pointer>
//		final int SYM__WORKING_STORAGE_SECTION                   = 1549;  // <_working_storage_section>
//		final int SYM__X_LIST                                    = 1550;  // <_x_list>
//	};

	// Symbolic constants naming the table indices of the grammar rules
	//@SuppressWarnings("unused")
	private interface RuleConstants
	{
//		final int PROD_CLASS_NAME_COBOLWORD                                                  =    0;  // <CLASS_NAME> ::= COBOLWord
//		final int PROD_FUNCTION_NAME_FUNCTION                                                =    1;  // <FUNCTION_NAME> ::= FUNCTION <COMMON_FUNCTION>
//		final int PROD_MNEMONIC_NAME_TOK_MNEMONIC_NAME                                       =    2;  // <MNEMONIC_NAME_TOK> ::= 'MNEMONIC_NAME'
//		final int PROD_PROGRAM_NAME_COBOLWORD                                                =    3;  // <PROGRAM_NAME> ::= COBOLWord
//		final int PROD_USER_FUNCTION_NAME_FUNCTION_COBOLWORD                                 =    4;  // <USER_FUNCTION_NAME> ::= FUNCTION COBOLWord
//		final int PROD_WORD_COBOLWORD                                                        =    5;  // <WORD> ::= COBOLWord
//		final int PROD_LITERAL_TOK_STRINGLITERAL                                             =    6;  // <LITERAL_TOK> ::= StringLiteral
//		final int PROD_LITERAL_TOK_HEXLITERAL                                                =    7;  // <LITERAL_TOK> ::= HexLiteral
//		final int PROD_LITERAL_TOK_ZLITERAL                                                  =    8;  // <LITERAL_TOK> ::= ZLiteral
//		final int PROD_LITERAL_TOK_BOOLEANLITERAL                                            =    9;  // <LITERAL_TOK> ::= BooleanLiteral
//		final int PROD_LITERAL_TOK_BOOLEANHEXLITERAL                                         =   10;  // <LITERAL_TOK> ::= BooleanHexLiteral
//		final int PROD_LITERAL_TOK_NATIONALLITERAL                                           =   11;  // <LITERAL_TOK> ::= NationalLiteral
//		final int PROD_LITERAL_TOK_NATIONALHEXLITERAL                                        =   12;  // <LITERAL_TOK> ::= NationalHexLiteral
//		final int PROD_LITERAL_TOK_ACUBINNUMLITERAL                                          =   13;  // <LITERAL_TOK> ::= AcuBinNumLiteral
//		final int PROD_LITERAL_TOK_ACUOCTNUMLITERAL                                          =   14;  // <LITERAL_TOK> ::= AcuOctNumLiteral
//		final int PROD_LITERAL_TOK_ACUHEXNUMLITERAL                                          =   15;  // <LITERAL_TOK> ::= AcuHexNumLiteral
//		final int PROD_LITERAL_TOK_INTLITERAL                                                =   16;  // <LITERAL_TOK> ::= IntLiteral
//		final int PROD_LITERAL_TOK_DECIMALLITERAL                                            =   17;  // <LITERAL_TOK> ::= DecimalLiteral
//		final int PROD_LITERAL_TOK_SIXTY_SIX                                                 =   18;  // <LITERAL_TOK> ::= 'SIXTY_SIX'
//		final int PROD_LITERAL_TOK_SEVENTY_EIGHT                                             =   19;  // <LITERAL_TOK> ::= 'SEVENTY_EIGHT'
//		final int PROD_LITERAL_TOK_EIGHTY_EIGHT                                              =   20;  // <LITERAL_TOK> ::= 'EIGHTY_EIGHT'
//		final int PROD_LITERAL_TOK_FLOATLITERAL                                              =   21;  // <LITERAL_TOK> ::= FloatLiteral
//		final int PROD_CONCATENATE_FUNC_FUNCTION_CONCATENATE                                 =   22;  // <CONCATENATE_FUNC> ::= FUNCTION CONCATENATE
//		final int PROD_CURRENT_DATE_FUNC_FUNCTION_CURRENT_DATE                               =   23;  // <CURRENT_DATE_FUNC> ::= FUNCTION 'CURRENT_DATE'
//		final int PROD_DISPLAY_OF_FUNC_FUNCTION_DISPLAY_OF                                   =   24;  // <DISPLAY_OF_FUNC> ::= FUNCTION 'DISPLAY_OF'
//		final int PROD_FORMATTED_DATE_FUNC_FUNCTION_FORMATTED_DATE                           =   25;  // <FORMATTED_DATE_FUNC> ::= FUNCTION 'FORMATTED_DATE'
//		final int PROD_FORMATTED_DATETIME_FUNC_FUNCTION_FORMATTED_DATETIME                   =   26;  // <FORMATTED_DATETIME_FUNC> ::= FUNCTION 'FORMATTED_DATETIME'
//		final int PROD_FORMATTED_TIME_FUNC_FUNCTION_FORMATTED_TIME                           =   27;  // <FORMATTED_TIME_FUNC> ::= FUNCTION 'FORMATTED_TIME'
//		final int PROD_LENGTH_FUNC_FUNCTION_LENGTH                                           =   28;  // <LENGTH_FUNC> ::= FUNCTION LENGTH
//		final int PROD_LENGTH_FUNC_FUNCTION_BYTE_LENGTH                                      =   29;  // <LENGTH_FUNC> ::= FUNCTION 'BYTE_LENGTH'
//		final int PROD_LOCALE_DATE_FUNC_FUNCTION_LOCALE_DATE                                 =   30;  // <LOCALE_DATE_FUNC> ::= FUNCTION 'LOCALE_DATE'
//		final int PROD_LOCALE_TIME_FUNC_FUNCTION_LOCALE_TIME                                 =   31;  // <LOCALE_TIME_FUNC> ::= FUNCTION 'LOCALE_TIME'
//		final int PROD_LOCALE_TIME_FROM_FUNC_FUNCTION_LOCALE_TIME_FROM_SECONDS               =   32;  // <LOCALE_TIME_FROM_FUNC> ::= FUNCTION 'LOCALE_TIME_FROM_SECONDS'
//		final int PROD_LOWER_CASE_FUNC_FUNCTION_LOWER_CASE                                   =   33;  // <LOWER_CASE_FUNC> ::= FUNCTION 'LOWER_CASE'
//		final int PROD_NATIONAL_OF_FUNC_FUNCTION_NATIONAL_OF                                 =   34;  // <NATIONAL_OF_FUNC> ::= FUNCTION 'NATIONAL_OF'
//		final int PROD_NUMVALC_FUNC_FUNCTION_NUMVAL_C                                        =   35;  // <NUMVALC_FUNC> ::= FUNCTION 'NUMVAL_C'
//		final int PROD_REVERSE_FUNC_FUNCTION_REVERSE                                         =   36;  // <REVERSE_FUNC> ::= FUNCTION REVERSE
//		final int PROD_SUBSTITUTE_FUNC_FUNCTION_SUBSTITUTE                                   =   37;  // <SUBSTITUTE_FUNC> ::= FUNCTION SUBSTITUTE
//		final int PROD_SUBSTITUTE_CASE_FUNC_FUNCTION_SUBSTITUTE_CASE                         =   38;  // <SUBSTITUTE_CASE_FUNC> ::= FUNCTION 'SUBSTITUTE_CASE'
//		final int PROD_TRIM_FUNC_FUNCTION_TRIM                                               =   39;  // <TRIM_FUNC> ::= FUNCTION TRIM
//		final int PROD_UPPER_CASE_FUNC_FUNCTION_UPPER_CASE                                   =   40;  // <UPPER_CASE_FUNC> ::= FUNCTION 'UPPER_CASE'
//		final int PROD_WHEN_COMPILED_FUNC_FUNCTION_WHEN_COMPILED                             =   41;  // <WHEN_COMPILED_FUNC> ::= FUNCTION 'WHEN_COMPILED'
//		final int PROD_COMMON_FUNCTION_ABS                                                   =   42;  // <COMMON_FUNCTION> ::= ABS
//		final int PROD_COMMON_FUNCTION_ACOS                                                  =   43;  // <COMMON_FUNCTION> ::= ACOS
//		final int PROD_COMMON_FUNCTION_ANNUITY                                               =   44;  // <COMMON_FUNCTION> ::= ANNUITY
//		final int PROD_COMMON_FUNCTION_ASIN                                                  =   45;  // <COMMON_FUNCTION> ::= ASIN
//		final int PROD_COMMON_FUNCTION_ATAN                                                  =   46;  // <COMMON_FUNCTION> ::= ATAN
//		final int PROD_COMMON_FUNCTION_BOOLEAN_OF_INTEGER                                    =   47;  // <COMMON_FUNCTION> ::= 'BOOLEAN_OF_INTEGER'
//		final int PROD_COMMON_FUNCTION_CHAR                                                  =   48;  // <COMMON_FUNCTION> ::= CHAR
//		final int PROD_COMMON_FUNCTION_CHAR_NATIONAL                                         =   49;  // <COMMON_FUNCTION> ::= 'CHAR_NATIONAL'
//		final int PROD_COMMON_FUNCTION_COMBINED_DATETIME                                     =   50;  // <COMMON_FUNCTION> ::= 'COMBINED_DATETIME'
//		final int PROD_COMMON_FUNCTION_COS                                                   =   51;  // <COMMON_FUNCTION> ::= COS
//		final int PROD_COMMON_FUNCTION_CURRENCY_SYMBOL                                       =   52;  // <COMMON_FUNCTION> ::= 'CURRENCY_SYMBOL'
//		final int PROD_COMMON_FUNCTION_DATE_OF_INTEGER                                       =   53;  // <COMMON_FUNCTION> ::= 'DATE_OF_INTEGER'
//		final int PROD_COMMON_FUNCTION_DATE_TO_YYYYMMDD                                      =   54;  // <COMMON_FUNCTION> ::= 'DATE_TO_YYYYMMDD'
//		final int PROD_COMMON_FUNCTION_DAY_OF_INTEGER                                        =   55;  // <COMMON_FUNCTION> ::= 'DAY_OF_INTEGER'
//		final int PROD_COMMON_FUNCTION_DAY_TO_YYYYDDD                                        =   56;  // <COMMON_FUNCTION> ::= 'DAY_TO_YYYYDDD'
//		final int PROD_COMMON_FUNCTION_E                                                     =   57;  // <COMMON_FUNCTION> ::= E
//		final int PROD_COMMON_FUNCTION_EXCEPTION_FILE                                        =   58;  // <COMMON_FUNCTION> ::= 'EXCEPTION_FILE'
//		final int PROD_COMMON_FUNCTION_EXCEPTION_FILE_N                                      =   59;  // <COMMON_FUNCTION> ::= 'EXCEPTION_FILE_N'
//		final int PROD_COMMON_FUNCTION_EXCEPTION_LOCATION                                    =   60;  // <COMMON_FUNCTION> ::= 'EXCEPTION_LOCATION'
//		final int PROD_COMMON_FUNCTION_EXCEPTION_LOCATION_N                                  =   61;  // <COMMON_FUNCTION> ::= 'EXCEPTION_LOCATION_N'
//		final int PROD_COMMON_FUNCTION_EXCEPTION_STATEMENT                                   =   62;  // <COMMON_FUNCTION> ::= 'EXCEPTION_STATEMENT'
//		final int PROD_COMMON_FUNCTION_EXCEPTION_STATUS                                      =   63;  // <COMMON_FUNCTION> ::= 'EXCEPTION_STATUS'
//		final int PROD_COMMON_FUNCTION_EXP                                                   =   64;  // <COMMON_FUNCTION> ::= EXP
//		final int PROD_COMMON_FUNCTION_FACTORIAL                                             =   65;  // <COMMON_FUNCTION> ::= FACTORIAL
//		final int PROD_COMMON_FUNCTION_FORMATTED_CURRENT_DATE                                =   66;  // <COMMON_FUNCTION> ::= 'FORMATTED_CURRENT_DATE'
//		final int PROD_COMMON_FUNCTION_FRACTION_PART                                         =   67;  // <COMMON_FUNCTION> ::= 'FRACTION_PART'
//		final int PROD_COMMON_FUNCTION_HIGHEST_ALGEBRAIC                                     =   68;  // <COMMON_FUNCTION> ::= 'HIGHEST_ALGEBRAIC'
//		final int PROD_COMMON_FUNCTION_INTEGER                                               =   69;  // <COMMON_FUNCTION> ::= INTEGER
//		final int PROD_COMMON_FUNCTION_INTEGER_OF_BOOLEAN                                    =   70;  // <COMMON_FUNCTION> ::= 'INTEGER_OF_BOOLEAN'
//		final int PROD_COMMON_FUNCTION_INTEGER_OF_DATE                                       =   71;  // <COMMON_FUNCTION> ::= 'INTEGER_OF_DATE'
//		final int PROD_COMMON_FUNCTION_INTEGER_OF_DAY                                        =   72;  // <COMMON_FUNCTION> ::= 'INTEGER_OF_DAY'
//		final int PROD_COMMON_FUNCTION_INTEGER_OF_FORMATTED_DATE                             =   73;  // <COMMON_FUNCTION> ::= 'INTEGER_OF_FORMATTED_DATE'
//		final int PROD_COMMON_FUNCTION_INTEGER_PART                                          =   74;  // <COMMON_FUNCTION> ::= 'INTEGER_PART'
//		final int PROD_COMMON_FUNCTION_LOCALE_COMPARE                                        =   75;  // <COMMON_FUNCTION> ::= 'LOCALE_COMPARE'
//		final int PROD_COMMON_FUNCTION_LOG                                                   =   76;  // <COMMON_FUNCTION> ::= LOG
//		final int PROD_COMMON_FUNCTION_LOWEST_ALGEBRAIC                                      =   77;  // <COMMON_FUNCTION> ::= 'LOWEST_ALGEBRAIC'
//		final int PROD_COMMON_FUNCTION_MAX                                                   =   78;  // <COMMON_FUNCTION> ::= MAX
//		final int PROD_COMMON_FUNCTION_MEAN                                                  =   79;  // <COMMON_FUNCTION> ::= MEAN
//		final int PROD_COMMON_FUNCTION_MEDIAN                                                =   80;  // <COMMON_FUNCTION> ::= MEDIAN
//		final int PROD_COMMON_FUNCTION_MIDRANGE                                              =   81;  // <COMMON_FUNCTION> ::= MIDRANGE
//		final int PROD_COMMON_FUNCTION_MIN                                                   =   82;  // <COMMON_FUNCTION> ::= MIN
//		final int PROD_COMMON_FUNCTION_MOD                                                   =   83;  // <COMMON_FUNCTION> ::= MOD
//		final int PROD_COMMON_FUNCTION_MODULE_CALLER_ID                                      =   84;  // <COMMON_FUNCTION> ::= 'MODULE_CALLER_ID'
//		final int PROD_COMMON_FUNCTION_MODULE_DATE                                           =   85;  // <COMMON_FUNCTION> ::= 'MODULE_DATE'
//		final int PROD_COMMON_FUNCTION_MODULE_FORMATTED_DATE                                 =   86;  // <COMMON_FUNCTION> ::= 'MODULE_FORMATTED_DATE'
//		final int PROD_COMMON_FUNCTION_MODULE_ID                                             =   87;  // <COMMON_FUNCTION> ::= 'MODULE_ID'
//		final int PROD_COMMON_FUNCTION_MODULE_PATH                                           =   88;  // <COMMON_FUNCTION> ::= 'MODULE_PATH'
//		final int PROD_COMMON_FUNCTION_MODULE_SOURCE                                         =   89;  // <COMMON_FUNCTION> ::= 'MODULE_SOURCE'
//		final int PROD_COMMON_FUNCTION_MODULE_TIME                                           =   90;  // <COMMON_FUNCTION> ::= 'MODULE_TIME'
//		final int PROD_COMMON_FUNCTION_MONETARY_DECIMAL_POINT                                =   91;  // <COMMON_FUNCTION> ::= 'MONETARY_DECIMAL_POINT'
//		final int PROD_COMMON_FUNCTION_MONETARY_THOUSANDS_SEPARATOR                          =   92;  // <COMMON_FUNCTION> ::= 'MONETARY_THOUSANDS_SEPARATOR'
//		final int PROD_COMMON_FUNCTION_NUMERIC_DECIMAL_POINT                                 =   93;  // <COMMON_FUNCTION> ::= 'NUMERIC_DECIMAL_POINT'
//		final int PROD_COMMON_FUNCTION_NUMERIC_THOUSANDS_SEPARATOR                           =   94;  // <COMMON_FUNCTION> ::= 'NUMERIC_THOUSANDS_SEPARATOR'
//		final int PROD_COMMON_FUNCTION_NUMVAL                                                =   95;  // <COMMON_FUNCTION> ::= NUMVAL
//		final int PROD_COMMON_FUNCTION_NUMVAL_F                                              =   96;  // <COMMON_FUNCTION> ::= 'NUMVAL_F'
//		final int PROD_COMMON_FUNCTION_ORD                                                   =   97;  // <COMMON_FUNCTION> ::= ORD
//		final int PROD_COMMON_FUNCTION_ORD_MAX                                               =   98;  // <COMMON_FUNCTION> ::= 'ORD_MAX'
//		final int PROD_COMMON_FUNCTION_ORD_MIN                                               =   99;  // <COMMON_FUNCTION> ::= 'ORD_MIN'
//		final int PROD_COMMON_FUNCTION_PI                                                    =  100;  // <COMMON_FUNCTION> ::= PI
//		final int PROD_COMMON_FUNCTION_PRESENT_VALUE                                         =  101;  // <COMMON_FUNCTION> ::= 'PRESENT_VALUE'
//		final int PROD_COMMON_FUNCTION_RANDOM                                                =  102;  // <COMMON_FUNCTION> ::= RANDOM
//		final int PROD_COMMON_FUNCTION_RANGE                                                 =  103;  // <COMMON_FUNCTION> ::= RANGE
//		final int PROD_COMMON_FUNCTION_REM                                                   =  104;  // <COMMON_FUNCTION> ::= REM
//		final int PROD_COMMON_FUNCTION_SECONDS_FROM_FORMATTED_TIME                           =  105;  // <COMMON_FUNCTION> ::= 'SECONDS_FROM_FORMATTED_TIME'
//		final int PROD_COMMON_FUNCTION_SECONDS_PAST_MIDNIGHT                                 =  106;  // <COMMON_FUNCTION> ::= 'SECONDS_PAST_MIDNIGHT'
//		final int PROD_COMMON_FUNCTION_SIGN                                                  =  107;  // <COMMON_FUNCTION> ::= SIGN
//		final int PROD_COMMON_FUNCTION_SIN                                                   =  108;  // <COMMON_FUNCTION> ::= SIN
//		final int PROD_COMMON_FUNCTION_SQRT                                                  =  109;  // <COMMON_FUNCTION> ::= SQRT
//		final int PROD_COMMON_FUNCTION_STANDARD_COMPARE                                      =  110;  // <COMMON_FUNCTION> ::= 'STANDARD_COMPARE'
//		final int PROD_COMMON_FUNCTION_STANDARD_DEVIATION                                    =  111;  // <COMMON_FUNCTION> ::= 'STANDARD_DEVIATION'
//		final int PROD_COMMON_FUNCTION_STORED_CHAR_LENGTH                                    =  112;  // <COMMON_FUNCTION> ::= 'STORED_CHAR_LENGTH'
//		final int PROD_COMMON_FUNCTION_SUM                                                   =  113;  // <COMMON_FUNCTION> ::= SUM
//		final int PROD_COMMON_FUNCTION_TAN                                                   =  114;  // <COMMON_FUNCTION> ::= TAN
//		final int PROD_COMMON_FUNCTION_TEST_DATE_YYYYMMDD                                    =  115;  // <COMMON_FUNCTION> ::= 'TEST_DATE_YYYYMMDD'
//		final int PROD_COMMON_FUNCTION_TEST_DAY_YYYYDDD                                      =  116;  // <COMMON_FUNCTION> ::= 'TEST_DAY_YYYYDDD'
//		final int PROD_COMMON_FUNCTION_TEST_FORMATTED_DATETIME                               =  117;  // <COMMON_FUNCTION> ::= 'TEST_FORMATTED_DATETIME'
//		final int PROD_COMMON_FUNCTION_TEST_NUMVAL                                           =  118;  // <COMMON_FUNCTION> ::= 'TEST_NUMVAL'
//		final int PROD_COMMON_FUNCTION_TEST_NUMVAL_F                                         =  119;  // <COMMON_FUNCTION> ::= 'TEST_NUMVAL_F'
//		final int PROD_COMMON_FUNCTION_VARIANCE                                              =  120;  // <COMMON_FUNCTION> ::= VARIANCE
//		final int PROD_COMMON_FUNCTION_YEAR_TO_YYYY                                          =  121;  // <COMMON_FUNCTION> ::= 'YEAR_TO_YYYY'
//		final int PROD_START                                                                 =  122;  // <start> ::= <compilation_group>
//		final int PROD_COMPILATION_GROUP                                                     =  123;  // <compilation_group> ::= <simple_prog>
//		final int PROD_COMPILATION_GROUP2                                                    =  124;  // <compilation_group> ::= <nested_list>
//		final int PROD_NESTED_LIST                                                           =  125;  // <nested_list> ::= <source_element_list>
//		final int PROD_SOURCE_ELEMENT_LIST                                                   =  126;  // <source_element_list> ::= <source_element>
//		final int PROD_SOURCE_ELEMENT_LIST2                                                  =  127;  // <source_element_list> ::= <source_element_list> <source_element>
//		final int PROD_SOURCE_ELEMENT                                                        =  128;  // <source_element> ::= <program_definition>
//		final int PROD_SOURCE_ELEMENT2                                                       =  129;  // <source_element> ::= <function_definition>
//		final int PROD_SIMPLE_PROG                                                           =  130;  // <simple_prog> ::= <_program_body>
		final int PROD_PROGRAM_DEFINITION                                                    =  131;  // <program_definition> ::= <_identification_header> <program_id_paragraph> <_Comment Items> <_options_paragraph> <_program_body> <_end_program_list>
		final int PROD_FUNCTION_DEFINITION                                                   =  132;  // <function_definition> ::= <_identification_header> <function_id_paragraph> <_Comment Items> <_options_paragraph> <_program_body> <end_function>
//		final int PROD__COMMENTITEMS                                                         =  133;  // <_Comment Items> ::= <_Comment Items> <Comment Item>
//		final int PROD__COMMENTITEMS2                                                        =  134;  // <_Comment Items> ::=
//		final int PROD_COMMENTITEM_AUTHOR_TOK_DOT_TOK_DOT                                    =  135;  // <Comment Item> ::= AUTHOR 'TOK_DOT' <NoiseList> 'TOK_DOT'
//		final int PROD_COMMENTITEM_INSTALLATION_TOK_DOT_TOK_DOT                              =  136;  // <Comment Item> ::= INSTALLATION 'TOK_DOT' <NoiseList> 'TOK_DOT'
//		final int PROD_COMMENTITEM_DATE_WRITTEN_TOK_DOT_TOK_DOT                              =  137;  // <Comment Item> ::= 'DATE_WRITTEN' 'TOK_DOT' <NoiseList> 'TOK_DOT'
//		final int PROD_COMMENTITEM_DATE_COMPILED_TOK_DOT_TOK_DOT                             =  138;  // <Comment Item> ::= 'DATE_COMPILED' 'TOK_DOT' <NoiseList> 'TOK_DOT'
//		final int PROD_COMMENTITEM_SECURITY_TOK_DOT_TOK_DOT                                  =  139;  // <Comment Item> ::= SECURITY 'TOK_DOT' <NoiseList> 'TOK_DOT'
//		final int PROD_NOISELIST                                                             =  140;  // <NoiseList> ::= <NoiseList> <Noise>
//		final int PROD_NOISELIST2                                                            =  141;  // <NoiseList> ::= <Noise>
//		final int PROD_NOISE_STRINGLITERAL                                                   =  142;  // <Noise> ::= StringLiteral
//		final int PROD_NOISE_INTLITERAL                                                      =  143;  // <Noise> ::= IntLiteral
//		final int PROD_NOISE_DECIMALLITERAL                                                  =  144;  // <Noise> ::= DecimalLiteral
//		final int PROD_NOISE_COBOLWORD                                                       =  145;  // <Noise> ::= COBOLWord
//		final int PROD_NOISE_COMMA_DELIM                                                     =  146;  // <Noise> ::= 'COMMA_DELIM'
//		final int PROD__END_PROGRAM_LIST                                                     =  147;  // <_end_program_list> ::=
//		final int PROD__END_PROGRAM_LIST2                                                    =  148;  // <_end_program_list> ::= <end_program_list>
//		final int PROD_END_PROGRAM_LIST                                                      =  149;  // <end_program_list> ::= <end_program>
//		final int PROD_END_PROGRAM_LIST2                                                     =  150;  // <end_program_list> ::= <end_program_list> <end_program>
//		final int PROD_END_PROGRAM_END_PROGRAM_TOK_DOT                                       =  151;  // <end_program> ::= 'END_PROGRAM' <end_program_name> 'TOK_DOT'
//		final int PROD_END_FUNCTION_END_FUNCTION_TOK_DOT                                     =  152;  // <end_function> ::= 'END_FUNCTION' <end_program_name> 'TOK_DOT'
//		final int PROD__PROGRAM_BODY                                                         =  153;  // <_program_body> ::= <_environment_division> <_data_division> <_procedure_division>
//		final int PROD__IDENTIFICATION_HEADER                                                =  154;  // <_identification_header> ::=
//		final int PROD__IDENTIFICATION_HEADER_DIVISION_TOK_DOT                               =  155;  // <_identification_header> ::= <identification_or_id> DIVISION 'TOK_DOT'
//		final int PROD_IDENTIFICATION_OR_ID_IDENTIFICATION                                   =  156;  // <identification_or_id> ::= IDENTIFICATION
//		final int PROD_IDENTIFICATION_OR_ID_ID                                               =  157;  // <identification_or_id> ::= ID
		final int PROD_PROGRAM_ID_PARAGRAPH_PROGRAM_ID_TOK_DOT_TOK_DOT                       =  158;  // <program_id_paragraph> ::= 'PROGRAM_ID' 'TOK_DOT' <program_id_name> <_as_literal> <_program_type> 'TOK_DOT'
		final int PROD_FUNCTION_ID_PARAGRAPH_FUNCTION_ID_TOK_DOT_TOK_DOT                     =  159;  // <function_id_paragraph> ::= 'FUNCTION_ID' 'TOK_DOT' <program_id_name> <_as_literal> 'TOK_DOT'
//		final int PROD_PROGRAM_ID_NAME                                                       =  160;  // <program_id_name> ::= <PROGRAM_NAME>
//		final int PROD_PROGRAM_ID_NAME2                                                      =  161;  // <program_id_name> ::= <LITERAL_TOK>
//		final int PROD_END_PROGRAM_NAME                                                      =  162;  // <end_program_name> ::= <PROGRAM_NAME>
//		final int PROD_END_PROGRAM_NAME2                                                     =  163;  // <end_program_name> ::= <LITERAL_TOK>
//		final int PROD__AS_LITERAL                                                           =  164;  // <_as_literal> ::=
//		final int PROD__AS_LITERAL_AS                                                        =  165;  // <_as_literal> ::= AS <LITERAL_TOK>
//		final int PROD__PROGRAM_TYPE                                                         =  166;  // <_program_type> ::=
//		final int PROD__PROGRAM_TYPE2                                                        =  167;  // <_program_type> ::= <_is> <program_type_clause> <_program>
//		final int PROD_PROGRAM_TYPE_CLAUSE_COMMON                                            =  168;  // <program_type_clause> ::= COMMON
//		final int PROD_PROGRAM_TYPE_CLAUSE                                                   =  169;  // <program_type_clause> ::= <init_or_recurse_and_common>
//		final int PROD_PROGRAM_TYPE_CLAUSE2                                                  =  170;  // <program_type_clause> ::= <init_or_recurse>
//		final int PROD_PROGRAM_TYPE_CLAUSE_EXTERNAL                                          =  171;  // <program_type_clause> ::= EXTERNAL
//		final int PROD_INIT_OR_RECURSE_AND_COMMON_COMMON                                     =  172;  // <init_or_recurse_and_common> ::= <init_or_recurse> COMMON
//		final int PROD_INIT_OR_RECURSE_AND_COMMON_COMMON2                                    =  173;  // <init_or_recurse_and_common> ::= COMMON <init_or_recurse>
//		final int PROD_INIT_OR_RECURSE_TOK_INITIAL                                           =  174;  // <init_or_recurse> ::= 'TOK_INITIAL'
//		final int PROD_INIT_OR_RECURSE_RECURSIVE                                             =  175;  // <init_or_recurse> ::= RECURSIVE
//		final int PROD__OPTIONS_PARAGRAPH                                                    =  176;  // <_options_paragraph> ::=
//		final int PROD__OPTIONS_PARAGRAPH_OPTIONS_TOK_DOT                                    =  177;  // <_options_paragraph> ::= OPTIONS 'TOK_DOT' <_options_clauses>
//		final int PROD__OPTIONS_CLAUSES_TOK_DOT                                              =  178;  // <_options_clauses> ::= <_default_rounded_clause> <_entry_convention_clause> <_intermediate_rounding_clause> 'TOK_DOT'
//		final int PROD__DEFAULT_ROUNDED_CLAUSE                                               =  179;  // <_default_rounded_clause> ::=
//		final int PROD__DEFAULT_ROUNDED_CLAUSE_DEFAULT_ROUNDED                               =  180;  // <_default_rounded_clause> ::= DEFAULT ROUNDED <_mode> <_is> <round_choice>
//		final int PROD__ENTRY_CONVENTION_CLAUSE                                              =  181;  // <_entry_convention_clause> ::=
//		final int PROD__ENTRY_CONVENTION_CLAUSE_ENTRY_CONVENTION                             =  182;  // <_entry_convention_clause> ::= 'ENTRY_CONVENTION' <_is> <convention_type>
//		final int PROD_CONVENTION_TYPE_COBOL                                                 =  183;  // <convention_type> ::= COBOL
//		final int PROD_CONVENTION_TYPE_TOK_EXTERN                                            =  184;  // <convention_type> ::= 'TOK_EXTERN'
//		final int PROD_CONVENTION_TYPE_STDCALL                                               =  185;  // <convention_type> ::= STDCALL
//		final int PROD__INTERMEDIATE_ROUNDING_CLAUSE                                         =  186;  // <_intermediate_rounding_clause> ::=
//		final int PROD__INTERMEDIATE_ROUNDING_CLAUSE_INTERMEDIATE_ROUNDING                   =  187;  // <_intermediate_rounding_clause> ::= INTERMEDIATE ROUNDING <_is> <intermediate_rounding_choice>
//		final int PROD_INTERMEDIATE_ROUNDING_CHOICE_NEAREST_AWAY_FROM_ZERO                   =  188;  // <intermediate_rounding_choice> ::= 'NEAREST_AWAY_FROM_ZERO'
//		final int PROD_INTERMEDIATE_ROUNDING_CHOICE_NEAREST_EVEN                             =  189;  // <intermediate_rounding_choice> ::= 'NEAREST_EVEN'
//		final int PROD_INTERMEDIATE_ROUNDING_CHOICE_PROHIBITED                               =  190;  // <intermediate_rounding_choice> ::= PROHIBITED
//		final int PROD_INTERMEDIATE_ROUNDING_CHOICE_TRUNCATION                               =  191;  // <intermediate_rounding_choice> ::= TRUNCATION
//		final int PROD__ENVIRONMENT_DIVISION                                                 =  192;  // <_environment_division> ::= <_environment_header> <_configuration_section> <_input_output_section>
//		final int PROD__ENVIRONMENT_HEADER                                                   =  193;  // <_environment_header> ::=
//		final int PROD__ENVIRONMENT_HEADER_ENVIRONMENT_DIVISION_TOK_DOT                      =  194;  // <_environment_header> ::= ENVIRONMENT DIVISION 'TOK_DOT'
//		final int PROD__CONFIGURATION_SECTION                                                =  195;  // <_configuration_section> ::= <_configuration_header> <_source_object_computer_paragraphs> <_special_names_paragraph> <_special_names_sentence_list> <_repository_paragraph>
//		final int PROD__CONFIGURATION_HEADER                                                 =  196;  // <_configuration_header> ::=
//		final int PROD__CONFIGURATION_HEADER_CONFIGURATION_SECTION_TOK_DOT                   =  197;  // <_configuration_header> ::= CONFIGURATION SECTION 'TOK_DOT'
//		final int PROD__SOURCE_OBJECT_COMPUTER_PARAGRAPHS                                    =  198;  // <_source_object_computer_paragraphs> ::=
//		final int PROD__SOURCE_OBJECT_COMPUTER_PARAGRAPHS2                                   =  199;  // <_source_object_computer_paragraphs> ::= <source_computer_paragraph>
//		final int PROD__SOURCE_OBJECT_COMPUTER_PARAGRAPHS3                                   =  200;  // <_source_object_computer_paragraphs> ::= <object_computer_paragraph>
//		final int PROD__SOURCE_OBJECT_COMPUTER_PARAGRAPHS4                                   =  201;  // <_source_object_computer_paragraphs> ::= <source_computer_paragraph> <object_computer_paragraph>
//		final int PROD__SOURCE_OBJECT_COMPUTER_PARAGRAPHS5                                   =  202;  // <_source_object_computer_paragraphs> ::= <object_computer_paragraph> <source_computer_paragraph>
//		final int PROD_SOURCE_COMPUTER_PARAGRAPH_SOURCE_COMPUTER_TOK_DOT                     =  203;  // <source_computer_paragraph> ::= 'SOURCE_COMPUTER' 'TOK_DOT' <_source_computer_entry>
//		final int PROD__SOURCE_COMPUTER_ENTRY                                                =  204;  // <_source_computer_entry> ::=
//		final int PROD__SOURCE_COMPUTER_ENTRY_TOK_DOT                                        =  205;  // <_source_computer_entry> ::= <computer_words> <_with_debugging_mode> 'TOK_DOT'
//		final int PROD__WITH_DEBUGGING_MODE                                                  =  206;  // <_with_debugging_mode> ::=
//		final int PROD__WITH_DEBUGGING_MODE_DEBUGGING_MODE                                   =  207;  // <_with_debugging_mode> ::= <_with> DEBUGGING MODE
//		final int PROD_OBJECT_COMPUTER_PARAGRAPH_OBJECT_COMPUTER_TOK_DOT                     =  208;  // <object_computer_paragraph> ::= 'OBJECT_COMPUTER' 'TOK_DOT' <_object_computer_entry>
//		final int PROD__OBJECT_COMPUTER_ENTRY                                                =  209;  // <_object_computer_entry> ::=
//		final int PROD__OBJECT_COMPUTER_ENTRY_TOK_DOT                                        =  210;  // <_object_computer_entry> ::= <computer_words> 'TOK_DOT'
//		final int PROD__OBJECT_COMPUTER_ENTRY_TOK_DOT2                                       =  211;  // <_object_computer_entry> ::= <computer_words> <object_clauses_list> 'TOK_DOT'
//		final int PROD__OBJECT_COMPUTER_ENTRY_TOK_DOT3                                       =  212;  // <_object_computer_entry> ::= <object_clauses_list> 'TOK_DOT'
//		final int PROD_OBJECT_CLAUSES_LIST                                                   =  213;  // <object_clauses_list> ::= <object_clauses>
//		final int PROD_OBJECT_CLAUSES_LIST2                                                  =  214;  // <object_clauses_list> ::= <object_clauses_list> <object_clauses>
//		final int PROD_OBJECT_CLAUSES                                                        =  215;  // <object_clauses> ::= <object_computer_memory>
//		final int PROD_OBJECT_CLAUSES2                                                       =  216;  // <object_clauses> ::= <object_computer_sequence>
//		final int PROD_OBJECT_CLAUSES3                                                       =  217;  // <object_clauses> ::= <object_computer_segment>
//		final int PROD_OBJECT_CLAUSES4                                                       =  218;  // <object_clauses> ::= <object_computer_class>
//		final int PROD_OBJECT_COMPUTER_MEMORY_MEMORY_SIZE                                    =  219;  // <object_computer_memory> ::= MEMORY SIZE <_is> <integer> <object_char_or_word>
//		final int PROD_OBJECT_COMPUTER_SEQUENCE                                              =  220;  // <object_computer_sequence> ::= <prog_coll_sequence> <_is> <single_reference>
//		final int PROD_OBJECT_COMPUTER_SEGMENT_SEGMENT_LIMIT                                 =  221;  // <object_computer_segment> ::= 'SEGMENT_LIMIT' <_is> <integer>
//		final int PROD_OBJECT_COMPUTER_CLASS_CLASSIFICATION                                  =  222;  // <object_computer_class> ::= <_character> CLASSIFICATION <_is> <locale_class>
//		final int PROD_LOCALE_CLASS                                                          =  223;  // <locale_class> ::= <single_reference>
//		final int PROD_LOCALE_CLASS_LOCALE                                                   =  224;  // <locale_class> ::= LOCALE
//		final int PROD_LOCALE_CLASS_USER_DEFAULT                                             =  225;  // <locale_class> ::= 'USER_DEFAULT'
//		final int PROD_LOCALE_CLASS_SYSTEM_DEFAULT                                           =  226;  // <locale_class> ::= 'SYSTEM_DEFAULT'
//		final int PROD_COMPUTER_WORDS                                                        =  227;  // <computer_words> ::= <WORD>
//		final int PROD_COMPUTER_WORDS2                                                       =  228;  // <computer_words> ::= <computer_words> <WORD>
//		final int PROD__REPOSITORY_PARAGRAPH                                                 =  229;  // <_repository_paragraph> ::=
//		final int PROD__REPOSITORY_PARAGRAPH_REPOSITORY_TOK_DOT                              =  230;  // <_repository_paragraph> ::= REPOSITORY 'TOK_DOT' <_repository_entry>
//		final int PROD__REPOSITORY_ENTRY                                                     =  231;  // <_repository_entry> ::=
//		final int PROD__REPOSITORY_ENTRY_TOK_DOT                                             =  232;  // <_repository_entry> ::= <repository_list> 'TOK_DOT'
//		final int PROD_REPOSITORY_LIST                                                       =  233;  // <repository_list> ::= <repository_name>
//		final int PROD_REPOSITORY_LIST2                                                      =  234;  // <repository_list> ::= <repository_list> <repository_name>
//		final int PROD_REPOSITORY_NAME_FUNCTION_ALL_INTRINSIC                                =  235;  // <repository_name> ::= FUNCTION ALL INTRINSIC
		final int PROD_REPOSITORY_NAME_FUNCTION                                              =  236;  // <repository_name> ::= FUNCTION <WORD> <_as_literal>
//		final int PROD_REPOSITORY_NAME_FUNCTION_INTRINSIC                                    =  237;  // <repository_name> ::= FUNCTION <repository_name_list> INTRINSIC
//		final int PROD_REPOSITORY_NAME_PROGRAM                                               =  238;  // <repository_name> ::= PROGRAM <WORD> <_as_literal>
//		final int PROD_REPOSITORY_NAME_LIST                                                  =  239;  // <repository_name_list> ::= <FUNCTION_NAME>
//		final int PROD_REPOSITORY_NAME_LIST2                                                 =  240;  // <repository_name_list> ::= <repository_name_list> <FUNCTION_NAME>
//		final int PROD__SPECIAL_NAMES_PARAGRAPH                                              =  241;  // <_special_names_paragraph> ::=
//		final int PROD__SPECIAL_NAMES_PARAGRAPH_SPECIAL_NAMES_TOK_DOT                        =  242;  // <_special_names_paragraph> ::= 'SPECIAL_NAMES' 'TOK_DOT'
//		final int PROD__SPECIAL_NAMES_SENTENCE_LIST                                          =  243;  // <_special_names_sentence_list> ::=
//		final int PROD__SPECIAL_NAMES_SENTENCE_LIST2                                         =  244;  // <_special_names_sentence_list> ::= <special_names_sentence_list>
//		final int PROD_SPECIAL_NAMES_SENTENCE_LIST_TOK_DOT                                   =  245;  // <special_names_sentence_list> ::= <special_name_list> 'TOK_DOT'
//		final int PROD_SPECIAL_NAMES_SENTENCE_LIST_TOK_DOT2                                  =  246;  // <special_names_sentence_list> ::= <special_names_sentence_list> <special_name_list> 'TOK_DOT'
//		final int PROD_SPECIAL_NAME_LIST                                                     =  247;  // <special_name_list> ::= <special_name>
//		final int PROD_SPECIAL_NAME_LIST2                                                    =  248;  // <special_name_list> ::= <special_name_list> <special_name>
//		final int PROD_SPECIAL_NAME                                                          =  249;  // <special_name> ::= <mnemonic_name_clause>
//		final int PROD_SPECIAL_NAME2                                                         =  250;  // <special_name> ::= <alphabet_name_clause>
//		final int PROD_SPECIAL_NAME3                                                         =  251;  // <special_name> ::= <symbolic_characters_clause>
//		final int PROD_SPECIAL_NAME4                                                         =  252;  // <special_name> ::= <locale_clause>
//		final int PROD_SPECIAL_NAME5                                                         =  253;  // <special_name> ::= <class_name_clause>
//		final int PROD_SPECIAL_NAME6                                                         =  254;  // <special_name> ::= <currency_sign_clause>
//		final int PROD_SPECIAL_NAME7                                                         =  255;  // <special_name> ::= <decimal_point_clause>
//		final int PROD_SPECIAL_NAME8                                                         =  256;  // <special_name> ::= <numeric_sign_clause>
//		final int PROD_SPECIAL_NAME9                                                         =  257;  // <special_name> ::= <cursor_clause>
//		final int PROD_SPECIAL_NAME10                                                        =  258;  // <special_name> ::= <crt_status_clause>
//		final int PROD_SPECIAL_NAME11                                                        =  259;  // <special_name> ::= <screen_control>
//		final int PROD_SPECIAL_NAME12                                                        =  260;  // <special_name> ::= <event_status>
//		final int PROD_SPECIAL_NAME_COMMA_DELIM                                              =  261;  // <special_name> ::= 'COMMA_DELIM'
//		final int PROD_MNEMONIC_NAME_CLAUSE                                                  =  262;  // <mnemonic_name_clause> ::= <WORD> <mnemonic_choices>
//		final int PROD_MNEMONIC_CHOICES_CRT                                                  =  263;  // <mnemonic_choices> ::= <_is> CRT
//		final int PROD_MNEMONIC_CHOICES                                                      =  264;  // <mnemonic_choices> ::= <integer> <_is> <undefined_word>
//		final int PROD_MNEMONIC_CHOICES2                                                     =  265;  // <mnemonic_choices> ::= <_is> <undefined_word> <_special_name_mnemonic_on_off>
//		final int PROD_MNEMONIC_CHOICES3                                                     =  266;  // <mnemonic_choices> ::= <on_off_clauses>
//		final int PROD__SPECIAL_NAME_MNEMONIC_ON_OFF                                         =  267;  // <_special_name_mnemonic_on_off> ::=
//		final int PROD__SPECIAL_NAME_MNEMONIC_ON_OFF2                                        =  268;  // <_special_name_mnemonic_on_off> ::= <on_off_clauses>
//		final int PROD_ON_OFF_CLAUSES                                                        =  269;  // <on_off_clauses> ::= <on_off_clauses_1>
//		final int PROD_ON_OFF_CLAUSES_1                                                      =  270;  // <on_off_clauses_1> ::= <on_or_off> <_onoff_status> <undefined_word>
//		final int PROD_ON_OFF_CLAUSES_12                                                     =  271;  // <on_off_clauses_1> ::= <on_off_clauses_1> <on_or_off> <_onoff_status> <undefined_word>
//		final int PROD_ALPHABET_NAME_CLAUSE_ALPHABET                                         =  272;  // <alphabet_name_clause> ::= ALPHABET <undefined_word> <_is> <alphabet_definition>
//		final int PROD_ALPHABET_DEFINITION_NATIVE                                            =  273;  // <alphabet_definition> ::= NATIVE
//		final int PROD_ALPHABET_DEFINITION_STANDARD_1                                        =  274;  // <alphabet_definition> ::= 'STANDARD_1'
//		final int PROD_ALPHABET_DEFINITION_STANDARD_2                                        =  275;  // <alphabet_definition> ::= 'STANDARD_2'
//		final int PROD_ALPHABET_DEFINITION_EBCDIC                                            =  276;  // <alphabet_definition> ::= EBCDIC
//		final int PROD_ALPHABET_DEFINITION_ASCII                                             =  277;  // <alphabet_definition> ::= ASCII
//		final int PROD_ALPHABET_DEFINITION                                                   =  278;  // <alphabet_definition> ::= <alphabet_literal_list>
//		final int PROD_ALPHABET_LITERAL_LIST                                                 =  279;  // <alphabet_literal_list> ::= <alphabet_literal>
//		final int PROD_ALPHABET_LITERAL_LIST2                                                =  280;  // <alphabet_literal_list> ::= <alphabet_literal_list> <alphabet_literal>
//		final int PROD_ALPHABET_LITERAL                                                      =  281;  // <alphabet_literal> ::= <alphabet_lits>
//		final int PROD_ALPHABET_LITERAL_THRU                                                 =  282;  // <alphabet_literal> ::= <alphabet_lits> THRU <alphabet_lits>
//		final int PROD_ALPHABET_LITERAL_ALSO                                                 =  283;  // <alphabet_literal> ::= <alphabet_lits> ALSO <alphabet_also_sequence>
//		final int PROD_ALPHABET_ALSO_SEQUENCE                                                =  284;  // <alphabet_also_sequence> ::= <alphabet_lits>
//		final int PROD_ALPHABET_ALSO_SEQUENCE_ALSO                                           =  285;  // <alphabet_also_sequence> ::= <alphabet_also_sequence> ALSO <alphabet_lits>
//		final int PROD_ALPHABET_LITS                                                         =  286;  // <alphabet_lits> ::= <LITERAL_TOK>
//		final int PROD_ALPHABET_LITS_SPACE                                                   =  287;  // <alphabet_lits> ::= SPACE
//		final int PROD_ALPHABET_LITS_ZERO                                                    =  288;  // <alphabet_lits> ::= ZERO
//		final int PROD_ALPHABET_LITS_QUOTE                                                   =  289;  // <alphabet_lits> ::= QUOTE
//		final int PROD_ALPHABET_LITS_HIGH_VALUE                                              =  290;  // <alphabet_lits> ::= 'HIGH_VALUE'
//		final int PROD_ALPHABET_LITS_LOW_VALUE                                               =  291;  // <alphabet_lits> ::= 'LOW_VALUE'
//		final int PROD_SPACE_OR_ZERO_SPACE                                                   =  292;  // <space_or_zero> ::= SPACE
//		final int PROD_SPACE_OR_ZERO_ZERO                                                    =  293;  // <space_or_zero> ::= ZERO
//		final int PROD_SYMBOLIC_CHARACTERS_CLAUSE                                            =  294;  // <symbolic_characters_clause> ::= <symbolic_collection> <_sym_in_word>
//		final int PROD__SYM_IN_WORD                                                          =  295;  // <_sym_in_word> ::=
//		final int PROD__SYM_IN_WORD_IN                                                       =  296;  // <_sym_in_word> ::= IN <WORD>
//		final int PROD_SYMBOLIC_COLLECTION_SYMBOLIC                                          =  297;  // <symbolic_collection> ::= SYMBOLIC <_characters> <symbolic_chars_list>
//		final int PROD_SYMBOLIC_CHARS_LIST                                                   =  298;  // <symbolic_chars_list> ::= <symbolic_chars_phrase>
//		final int PROD_SYMBOLIC_CHARS_LIST2                                                  =  299;  // <symbolic_chars_list> ::= <symbolic_chars_list> <symbolic_chars_phrase>
//		final int PROD_SYMBOLIC_CHARS_PHRASE                                                 =  300;  // <symbolic_chars_phrase> ::= <char_list> <_is_are> <integer_list>
//		final int PROD_CHAR_LIST                                                             =  301;  // <char_list> ::= <unique_word>
//		final int PROD_CHAR_LIST2                                                            =  302;  // <char_list> ::= <char_list> <unique_word>
//		final int PROD_INTEGER_LIST                                                          =  303;  // <integer_list> ::= <symbolic_integer>
//		final int PROD_INTEGER_LIST2                                                         =  304;  // <integer_list> ::= <integer_list> <symbolic_integer>
//		final int PROD_CLASS_NAME_CLAUSE_CLASS                                               =  305;  // <class_name_clause> ::= CLASS <undefined_word> <_is> <class_item_list>
//		final int PROD_CLASS_ITEM_LIST                                                       =  306;  // <class_item_list> ::= <class_item>
//		final int PROD_CLASS_ITEM_LIST2                                                      =  307;  // <class_item_list> ::= <class_item_list> <class_item>
//		final int PROD_CLASS_ITEM                                                            =  308;  // <class_item> ::= <class_value>
//		final int PROD_CLASS_ITEM_THRU                                                       =  309;  // <class_item> ::= <class_value> THRU <class_value>
//		final int PROD_LOCALE_CLAUSE_LOCALE                                                  =  310;  // <locale_clause> ::= LOCALE <undefined_word> <_is> <LITERAL_TOK>
//		final int PROD_CURRENCY_SIGN_CLAUSE_CURRENCY                                         =  311;  // <currency_sign_clause> ::= CURRENCY <_sign> <_is> <LITERAL_TOK> <_with_pic_symbol>
//		final int PROD__WITH_PIC_SYMBOL                                                      =  312;  // <_with_pic_symbol> ::=
//		final int PROD__WITH_PIC_SYMBOL_PICTURE_SYMBOL                                       =  313;  // <_with_pic_symbol> ::= <_with> 'PICTURE_SYMBOL' <LITERAL_TOK>
//		final int PROD_DECIMAL_POINT_CLAUSE_DECIMAL_POINT_COMMA                              =  314;  // <decimal_point_clause> ::= 'DECIMAL_POINT' <_is> COMMA
//		final int PROD_NUMERIC_SIGN_CLAUSE_NUMERIC_SIGN_TRAILING_SEPARATE                    =  315;  // <numeric_sign_clause> ::= NUMERIC SIGN <_is> TRAILING SEPARATE
//		final int PROD_CURSOR_CLAUSE_CURSOR                                                  =  316;  // <cursor_clause> ::= CURSOR <_is> <reference>
//		final int PROD_CRT_STATUS_CLAUSE_CRT_STATUS                                          =  317;  // <crt_status_clause> ::= CRT STATUS <_is> <reference>
//		final int PROD_SCREEN_CONTROL_SCREEN_CONTROL                                         =  318;  // <screen_control> ::= 'SCREEN_CONTROL' <_is> <reference>
//		final int PROD_EVENT_STATUS_EVENT_STATUS                                             =  319;  // <event_status> ::= 'EVENT_STATUS' <_is> <reference>
//		final int PROD__INPUT_OUTPUT_SECTION                                                 =  320;  // <_input_output_section> ::= <_input_output_header> <_file_control_header> <_file_control_sequence> <_i_o_control_header> <_i_o_control>
//		final int PROD__INPUT_OUTPUT_HEADER                                                  =  321;  // <_input_output_header> ::=
//		final int PROD__INPUT_OUTPUT_HEADER_INPUT_OUTPUT_SECTION_TOK_DOT                     =  322;  // <_input_output_header> ::= 'INPUT_OUTPUT' SECTION 'TOK_DOT'
//		final int PROD__FILE_CONTROL_HEADER                                                  =  323;  // <_file_control_header> ::=
//		final int PROD__FILE_CONTROL_HEADER_FILE_CONTROL_TOK_DOT                             =  324;  // <_file_control_header> ::= 'FILE_CONTROL' 'TOK_DOT'
//		final int PROD__I_O_CONTROL_HEADER                                                   =  325;  // <_i_o_control_header> ::=
//		final int PROD__I_O_CONTROL_HEADER_I_O_CONTROL_TOK_DOT                               =  326;  // <_i_o_control_header> ::= 'I_O_CONTROL' 'TOK_DOT'
//		final int PROD__FILE_CONTROL_SEQUENCE                                                =  327;  // <_file_control_sequence> ::=
//		final int PROD__FILE_CONTROL_SEQUENCE2                                               =  328;  // <_file_control_sequence> ::= <_file_control_sequence> <file_control_entry>
		final int PROD_FILE_CONTROL_ENTRY_SELECT                                             =  329;  // <file_control_entry> ::= SELECT <flag_optional> <undefined_word> <_select_clauses_or_error>
//		final int PROD__SELECT_CLAUSES_OR_ERROR_TOK_DOT                                      =  330;  // <_select_clauses_or_error> ::= <_select_clause_sequence> 'TOK_DOT'
//		final int PROD__SELECT_CLAUSE_SEQUENCE                                               =  331;  // <_select_clause_sequence> ::=
		final int PROD__SELECT_CLAUSE_SEQUENCE2                                              =  332;  // <_select_clause_sequence> ::= <_select_clause_sequence> <select_clause>
//		final int PROD_SELECT_CLAUSE                                                         =  333;  // <select_clause> ::= <assign_clause>
//		final int PROD_SELECT_CLAUSE2                                                        =  334;  // <select_clause> ::= <access_mode_clause>
//		final int PROD_SELECT_CLAUSE3                                                        =  335;  // <select_clause> ::= <alternative_record_key_clause>
//		final int PROD_SELECT_CLAUSE4                                                        =  336;  // <select_clause> ::= <collating_sequence_clause>
//		final int PROD_SELECT_CLAUSE5                                                        =  337;  // <select_clause> ::= <file_status_clause>
//		final int PROD_SELECT_CLAUSE6                                                        =  338;  // <select_clause> ::= <lock_mode_clause>
//		final int PROD_SELECT_CLAUSE7                                                        =  339;  // <select_clause> ::= <organization_clause>
//		final int PROD_SELECT_CLAUSE8                                                        =  340;  // <select_clause> ::= <padding_character_clause>
//		final int PROD_SELECT_CLAUSE9                                                        =  341;  // <select_clause> ::= <record_delimiter_clause>
//		final int PROD_SELECT_CLAUSE10                                                       =  342;  // <select_clause> ::= <record_key_clause>
//		final int PROD_SELECT_CLAUSE11                                                       =  343;  // <select_clause> ::= <relative_key_clause>
//		final int PROD_SELECT_CLAUSE12                                                       =  344;  // <select_clause> ::= <reserve_clause>
//		final int PROD_SELECT_CLAUSE13                                                       =  345;  // <select_clause> ::= <sharing_clause>
//		final int PROD_ASSIGN_CLAUSE_ASSIGN                                                  =  346;  // <assign_clause> ::= ASSIGN <_to_using> <_ext_clause> <_line_adv_file> <assignment_name>
//		final int PROD_ASSIGN_CLAUSE_ASSIGN2                                                 =  347;  // <assign_clause> ::= ASSIGN <_to_using> <_ext_clause> <general_device_name> <_assignment_name>
//		final int PROD_ASSIGN_CLAUSE_ASSIGN3                                                 =  348;  // <assign_clause> ::= ASSIGN <_to_using> <_ext_clause> <line_seq_device_name> <_assignment_name>
//		final int PROD_ASSIGN_CLAUSE_ASSIGN_DISPLAY                                          =  349;  // <assign_clause> ::= ASSIGN <_to_using> <_ext_clause> DISPLAY <_assignment_name>
//		final int PROD_ASSIGN_CLAUSE_ASSIGN_KEYBOARD                                         =  350;  // <assign_clause> ::= ASSIGN <_to_using> <_ext_clause> KEYBOARD <_assignment_name>
//		final int PROD_ASSIGN_CLAUSE_ASSIGN4                                                 =  351;  // <assign_clause> ::= ASSIGN <_to_using> <_ext_clause> <printer_name> <_assignment_name>
//		final int PROD_PRINTER_NAME_PRINTER                                                  =  352;  // <printer_name> ::= PRINTER
//		final int PROD_PRINTER_NAME_PRINTER_1                                                =  353;  // <printer_name> ::= 'PRINTER_1'
//		final int PROD_PRINTER_NAME_PRINT                                                    =  354;  // <printer_name> ::= PRINT
//		final int PROD_GENERAL_DEVICE_NAME_DISC                                              =  355;  // <general_device_name> ::= DISC
//		final int PROD_GENERAL_DEVICE_NAME_DISK                                              =  356;  // <general_device_name> ::= DISK
//		final int PROD_GENERAL_DEVICE_NAME_TAPE                                              =  357;  // <general_device_name> ::= TAPE
//		final int PROD_GENERAL_DEVICE_NAME_RANDOM                                            =  358;  // <general_device_name> ::= RANDOM
//		final int PROD_LINE_SEQ_DEVICE_NAME_CARD_PUNCH                                       =  359;  // <line_seq_device_name> ::= 'CARD_PUNCH'
//		final int PROD_LINE_SEQ_DEVICE_NAME_CARD_READER                                      =  360;  // <line_seq_device_name> ::= 'CARD_READER'
//		final int PROD_LINE_SEQ_DEVICE_NAME_CASSETTE                                         =  361;  // <line_seq_device_name> ::= CASSETTE
//		final int PROD_LINE_SEQ_DEVICE_NAME_INPUT                                            =  362;  // <line_seq_device_name> ::= INPUT
//		final int PROD_LINE_SEQ_DEVICE_NAME_INPUT_OUTPUT                                     =  363;  // <line_seq_device_name> ::= 'INPUT_OUTPUT'
//		final int PROD_LINE_SEQ_DEVICE_NAME_MAGNETIC_TAPE                                    =  364;  // <line_seq_device_name> ::= 'MAGNETIC_TAPE'
//		final int PROD_LINE_SEQ_DEVICE_NAME_OUTPUT                                           =  365;  // <line_seq_device_name> ::= OUTPUT
//		final int PROD__LINE_ADV_FILE                                                        =  366;  // <_line_adv_file> ::=
//		final int PROD__LINE_ADV_FILE_LINE_ADVANCING                                         =  367;  // <_line_adv_file> ::= LINE ADVANCING <_file>
//		final int PROD__EXT_CLAUSE                                                           =  368;  // <_ext_clause> ::=
//		final int PROD__EXT_CLAUSE_EXTERNAL                                                  =  369;  // <_ext_clause> ::= EXTERNAL
//		final int PROD__EXT_CLAUSE_DYNAMIC                                                   =  370;  // <_ext_clause> ::= DYNAMIC
//		final int PROD_ASSIGNMENT_NAME                                                       =  371;  // <assignment_name> ::= <LITERAL_TOK>
//		final int PROD_ASSIGNMENT_NAME2                                                      =  372;  // <assignment_name> ::= <qualified_word>
//		final int PROD__ASSIGNMENT_NAME                                                      =  373;  // <_assignment_name> ::=
//		final int PROD__ASSIGNMENT_NAME2                                                     =  374;  // <_assignment_name> ::= <LITERAL_TOK>
//		final int PROD__ASSIGNMENT_NAME3                                                     =  375;  // <_assignment_name> ::= <qualified_word>
//		final int PROD_ACCESS_MODE_CLAUSE_ACCESS                                             =  376;  // <access_mode_clause> ::= ACCESS <_mode> <_is> <access_mode>
//		final int PROD_ACCESS_MODE_SEQUENTIAL                                                =  377;  // <access_mode> ::= SEQUENTIAL
//		final int PROD_ACCESS_MODE_DYNAMIC                                                   =  378;  // <access_mode> ::= DYNAMIC
//		final int PROD_ACCESS_MODE_RANDOM                                                    =  379;  // <access_mode> ::= RANDOM
//		final int PROD_ALTERNATIVE_RECORD_KEY_CLAUSE_ALTERNATE                               =  380;  // <alternative_record_key_clause> ::= ALTERNATE <_record> <_key> <_is> <key_or_split_keys> <flag_duplicates> <_suppress_clause>
//		final int PROD__SUPPRESS_CLAUSE                                                      =  381;  // <_suppress_clause> ::=
//		final int PROD__SUPPRESS_CLAUSE_SUPPRESS_WHEN_ALL                                    =  382;  // <_suppress_clause> ::= SUPPRESS WHEN ALL <basic_value>
//		final int PROD__SUPPRESS_CLAUSE_SUPPRESS_WHEN                                        =  383;  // <_suppress_clause> ::= SUPPRESS WHEN <space_or_zero>
//		final int PROD_COLLATING_SEQUENCE_CLAUSE                                             =  384;  // <collating_sequence_clause> ::= <coll_sequence> <_is> <alphabet_name>
//		final int PROD_ALPHABET_NAME                                                         =  385;  // <alphabet_name> ::= <WORD>
//		final int PROD_FILE_STATUS_CLAUSE_STATUS                                             =  386;  // <file_status_clause> ::= <_file_or_sort> STATUS <_is> <reference>
//		final int PROD__FILE_OR_SORT                                                         =  387;  // <_file_or_sort> ::=
		final int PROD__FILE_OR_SORT_TOK_FILE                                                =  388;  // <_file_or_sort> ::= 'TOK_FILE'
//		final int PROD__FILE_OR_SORT_SORT                                                    =  389;  // <_file_or_sort> ::= SORT
//		final int PROD_LOCK_MODE_CLAUSE_LOCK                                                 =  390;  // <lock_mode_clause> ::= LOCK <_mode> <_is> <lock_mode>
//		final int PROD_LOCK_MODE_MANUAL                                                      =  391;  // <lock_mode> ::= MANUAL <_lock_with>
//		final int PROD_LOCK_MODE_AUTOMATIC                                                   =  392;  // <lock_mode> ::= AUTOMATIC <_lock_with>
//		final int PROD_LOCK_MODE_EXCLUSIVE                                                   =  393;  // <lock_mode> ::= EXCLUSIVE
//		final int PROD__LOCK_WITH                                                            =  394;  // <_lock_with> ::=
//		final int PROD__LOCK_WITH_WITH_LOCK_ON                                               =  395;  // <_lock_with> ::= WITH LOCK ON <lock_records>
//		final int PROD__LOCK_WITH_WITH_LOCK_ON_MULTIPLE                                      =  396;  // <_lock_with> ::= WITH LOCK ON MULTIPLE <lock_records>
//		final int PROD__LOCK_WITH_WITH_ROLLBACK                                              =  397;  // <_lock_with> ::= WITH ROLLBACK
//		final int PROD_ORGANIZATION_CLAUSE_ORGANIZATION                                      =  398;  // <organization_clause> ::= ORGANIZATION <_is> <organization>
//		final int PROD_ORGANIZATION_CLAUSE                                                   =  399;  // <organization_clause> ::= <organization>
//		final int PROD_ORGANIZATION_INDEXED                                                  =  400;  // <organization> ::= INDEXED
//		final int PROD_ORGANIZATION_SEQUENTIAL                                               =  401;  // <organization> ::= <_record> <_binary> SEQUENTIAL
//		final int PROD_ORGANIZATION_RELATIVE                                                 =  402;  // <organization> ::= RELATIVE
		final int PROD_ORGANIZATION_LINE_SEQUENTIAL                                          =  403;  // <organization> ::= LINE SEQUENTIAL
//		final int PROD_PADDING_CHARACTER_CLAUSE_PADDING                                      =  404;  // <padding_character_clause> ::= PADDING <_character> <_is> <reference_or_literal>
//		final int PROD_RECORD_DELIMITER_CLAUSE_RECORD_DELIMITER_STANDARD_1                   =  405;  // <record_delimiter_clause> ::= RECORD DELIMITER <_is> 'STANDARD_1'
//		final int PROD_RECORD_KEY_CLAUSE_RECORD                                              =  406;  // <record_key_clause> ::= RECORD <_key> <_is> <key_or_split_keys>
//		final int PROD_KEY_OR_SPLIT_KEYS                                                     =  407;  // <key_or_split_keys> ::= <reference>
//		final int PROD_KEY_OR_SPLIT_KEYS_TOK_EQUAL                                           =  408;  // <key_or_split_keys> ::= <reference> 'TOK_EQUAL' <reference_list>
//		final int PROD_KEY_OR_SPLIT_KEYS_SOURCE                                              =  409;  // <key_or_split_keys> ::= <reference> SOURCE <_is> <reference_list>
//		final int PROD_RELATIVE_KEY_CLAUSE_RELATIVE                                          =  410;  // <relative_key_clause> ::= RELATIVE <_key> <_is> <reference>
//		final int PROD_RESERVE_CLAUSE_RESERVE                                                =  411;  // <reserve_clause> ::= RESERVE <no_or_integer> <_areas>
//		final int PROD_NO_OR_INTEGER_NO                                                      =  412;  // <no_or_integer> ::= NO
//		final int PROD_NO_OR_INTEGER                                                         =  413;  // <no_or_integer> ::= <integer>
//		final int PROD_SHARING_CLAUSE_SHARING                                                =  414;  // <sharing_clause> ::= SHARING <_with> <sharing_option>
//		final int PROD_SHARING_OPTION_ALL                                                    =  415;  // <sharing_option> ::= ALL <_other>
//		final int PROD_SHARING_OPTION_NO                                                     =  416;  // <sharing_option> ::= NO <_other>
//		final int PROD_SHARING_OPTION_READ_ONLY                                              =  417;  // <sharing_option> ::= READ ONLY
//		final int PROD__I_O_CONTROL                                                          =  418;  // <_i_o_control> ::=
//		final int PROD__I_O_CONTROL_TOK_DOT                                                  =  419;  // <_i_o_control> ::= <i_o_control_list> 'TOK_DOT'
//		final int PROD_I_O_CONTROL_LIST                                                      =  420;  // <i_o_control_list> ::= <i_o_control_clause>
//		final int PROD_I_O_CONTROL_LIST2                                                     =  421;  // <i_o_control_list> ::= <i_o_control_list> <i_o_control_clause>
//		final int PROD_I_O_CONTROL_CLAUSE                                                    =  422;  // <i_o_control_clause> ::= <same_clause>
//		final int PROD_I_O_CONTROL_CLAUSE2                                                   =  423;  // <i_o_control_clause> ::= <multiple_file_tape_clause>
//		final int PROD_SAME_CLAUSE_SAME                                                      =  424;  // <same_clause> ::= SAME <_same_option> <_area> <_for> <file_name_list>
//		final int PROD__SAME_OPTION                                                          =  425;  // <_same_option> ::=
//		final int PROD__SAME_OPTION_RECORD                                                   =  426;  // <_same_option> ::= RECORD
//		final int PROD__SAME_OPTION_SORT                                                     =  427;  // <_same_option> ::= SORT
//		final int PROD__SAME_OPTION_SORT_MERGE                                               =  428;  // <_same_option> ::= 'SORT_MERGE'
//		final int PROD_MULTIPLE_FILE_TAPE_CLAUSE_MULTIPLE                                    =  429;  // <multiple_file_tape_clause> ::= MULTIPLE <_file> <_tape> <_contains> <multiple_file_list>
//		final int PROD_MULTIPLE_FILE_LIST                                                    =  430;  // <multiple_file_list> ::= <multiple_file>
//		final int PROD_MULTIPLE_FILE_LIST2                                                   =  431;  // <multiple_file_list> ::= <multiple_file_list> <multiple_file>
//		final int PROD_MULTIPLE_FILE                                                         =  432;  // <multiple_file> ::= <file_name> <_multiple_file_position>
//		final int PROD__MULTIPLE_FILE_POSITION                                               =  433;  // <_multiple_file_position> ::=
//		final int PROD__MULTIPLE_FILE_POSITION_POSITION                                      =  434;  // <_multiple_file_position> ::= POSITION <integer>
//		final int PROD__DATA_DIVISION                                                        =  435;  // <_data_division> ::= <_data_division_header> <_file_section_header> <_file_description_sequence> <_working_storage_section> <_communication_section> <_local_storage_section> <_linkage_section> <_report_section> <_screen_section>
//		final int PROD__DATA_DIVISION_HEADER                                                 =  436;  // <_data_division_header> ::=
//		final int PROD__DATA_DIVISION_HEADER_DATA_DIVISION_TOK_DOT                           =  437;  // <_data_division_header> ::= DATA DIVISION 'TOK_DOT'
//		final int PROD__FILE_SECTION_HEADER                                                  =  438;  // <_file_section_header> ::=
		final int PROD__FILE_SECTION_HEADER_TOK_FILE_SECTION_TOK_DOT                         =  439;  // <_file_section_header> ::= 'TOK_FILE' SECTION 'TOK_DOT'
//		final int PROD__FILE_DESCRIPTION_SEQUENCE                                            =  440;  // <_file_description_sequence> ::=
//		final int PROD__FILE_DESCRIPTION_SEQUENCE2                                           =  441;  // <_file_description_sequence> ::= <_file_description_sequence> <file_description>
		final int PROD_FILE_DESCRIPTION                                                      =  442;  // <file_description> ::= <file_description_entry> <_record_description_list>
//		final int PROD_FILE_DESCRIPTION_ENTRY_TOK_DOT                                        =  443;  // <file_description_entry> ::= <file_type> <file_name> <_file_description_clause_sequence> 'TOK_DOT'
//		final int PROD_FILE_TYPE_FD                                                          =  444;  // <file_type> ::= FD
//		final int PROD_FILE_TYPE_SD                                                          =  445;  // <file_type> ::= SD
//		final int PROD__FILE_DESCRIPTION_CLAUSE_SEQUENCE                                     =  446;  // <_file_description_clause_sequence> ::=
//		final int PROD__FILE_DESCRIPTION_CLAUSE_SEQUENCE2                                    =  447;  // <_file_description_clause_sequence> ::= <_file_description_clause_sequence> <file_description_clause>
//		final int PROD_FILE_DESCRIPTION_CLAUSE_EXTERNAL                                      =  448;  // <file_description_clause> ::= <_is> EXTERNAL
//		final int PROD_FILE_DESCRIPTION_CLAUSE_GLOBAL                                        =  449;  // <file_description_clause> ::= <_is> GLOBAL
//		final int PROD_FILE_DESCRIPTION_CLAUSE                                               =  450;  // <file_description_clause> ::= <block_contains_clause>
//		final int PROD_FILE_DESCRIPTION_CLAUSE2                                              =  451;  // <file_description_clause> ::= <record_clause>
//		final int PROD_FILE_DESCRIPTION_CLAUSE3                                              =  452;  // <file_description_clause> ::= <label_records_clause>
//		final int PROD_FILE_DESCRIPTION_CLAUSE4                                              =  453;  // <file_description_clause> ::= <value_of_clause>
//		final int PROD_FILE_DESCRIPTION_CLAUSE5                                              =  454;  // <file_description_clause> ::= <data_records_clause>
//		final int PROD_FILE_DESCRIPTION_CLAUSE6                                              =  455;  // <file_description_clause> ::= <linage_clause>
//		final int PROD_FILE_DESCRIPTION_CLAUSE7                                              =  456;  // <file_description_clause> ::= <recording_mode_clause>
//		final int PROD_FILE_DESCRIPTION_CLAUSE8                                              =  457;  // <file_description_clause> ::= <code_set_clause>
//		final int PROD_FILE_DESCRIPTION_CLAUSE9                                              =  458;  // <file_description_clause> ::= <report_clause>
//		final int PROD_BLOCK_CONTAINS_CLAUSE_BLOCK                                           =  459;  // <block_contains_clause> ::= BLOCK <_contains> <integer> <_to_integer> <_records_or_characters>
//		final int PROD__RECORDS_OR_CHARACTERS                                                =  460;  // <_records_or_characters> ::=
//		final int PROD__RECORDS_OR_CHARACTERS_RECORDS                                        =  461;  // <_records_or_characters> ::= RECORDS
//		final int PROD__RECORDS_OR_CHARACTERS_CHARACTERS                                     =  462;  // <_records_or_characters> ::= CHARACTERS
//		final int PROD_RECORD_CLAUSE_RECORD                                                  =  463;  // <record_clause> ::= RECORD <_contains> <integer> <_characters>
//		final int PROD_RECORD_CLAUSE_RECORD_TO                                               =  464;  // <record_clause> ::= RECORD <_contains> <integer> TO <integer> <_characters>
//		final int PROD_RECORD_CLAUSE_RECORD_VARYING                                          =  465;  // <record_clause> ::= RECORD <_is> VARYING <_in> <_size> <_from_integer> <_to_integer> <_characters> <_record_depending>
//		final int PROD__RECORD_DEPENDING                                                     =  466;  // <_record_depending> ::=
//		final int PROD__RECORD_DEPENDING_DEPENDING                                           =  467;  // <_record_depending> ::= DEPENDING <_on> <reference>
//		final int PROD__FROM_INTEGER                                                         =  468;  // <_from_integer> ::=
//		final int PROD__FROM_INTEGER2                                                        =  469;  // <_from_integer> ::= <_from> <integer>
//		final int PROD__TO_INTEGER                                                           =  470;  // <_to_integer> ::=
//		final int PROD__TO_INTEGER_TO                                                        =  471;  // <_to_integer> ::= TO <integer>
//		final int PROD_LABEL_RECORDS_CLAUSE_LABEL                                            =  472;  // <label_records_clause> ::= LABEL <records> <label_option>
//		final int PROD_VALUE_OF_CLAUSE_VALUE_OF                                              =  473;  // <value_of_clause> ::= VALUE OF <file_id> <_is> <valueof_name>
//		final int PROD_VALUE_OF_CLAUSE_VALUE_OF_FILE_ID                                      =  474;  // <value_of_clause> ::= VALUE OF 'FILE_ID' <_is> <valueof_name>
//		final int PROD_FILE_ID                                                               =  475;  // <file_id> ::= <WORD>
//		final int PROD_FILE_ID_ID                                                            =  476;  // <file_id> ::= ID
//		final int PROD_VALUEOF_NAME                                                          =  477;  // <valueof_name> ::= <LITERAL_TOK>
//		final int PROD_VALUEOF_NAME2                                                         =  478;  // <valueof_name> ::= <qualified_word>
//		final int PROD_DATA_RECORDS_CLAUSE_DATA                                              =  479;  // <data_records_clause> ::= DATA <records> <optional_reference_list>
//		final int PROD_LINAGE_CLAUSE_LINAGE                                                  =  480;  // <linage_clause> ::= LINAGE <_is> <reference_or_literal> <_lines> <_linage_sequence>
//		final int PROD__LINAGE_SEQUENCE                                                      =  481;  // <_linage_sequence> ::=
//		final int PROD__LINAGE_SEQUENCE2                                                     =  482;  // <_linage_sequence> ::= <_linage_sequence> <linage_lines>
//		final int PROD_LINAGE_LINES                                                          =  483;  // <linage_lines> ::= <linage_footing>
//		final int PROD_LINAGE_LINES2                                                         =  484;  // <linage_lines> ::= <linage_top>
//		final int PROD_LINAGE_LINES3                                                         =  485;  // <linage_lines> ::= <linage_bottom>
//		final int PROD_LINAGE_FOOTING_FOOTING                                                =  486;  // <linage_footing> ::= <_with> FOOTING <_at> <reference_or_literal>
//		final int PROD_LINAGE_TOP_TOP                                                        =  487;  // <linage_top> ::= TOP <reference_or_literal>
//		final int PROD_LINAGE_BOTTOM_BOTTOM                                                  =  488;  // <linage_bottom> ::= BOTTOM <reference_or_literal>
//		final int PROD_RECORDING_MODE_CLAUSE_RECORDING                                       =  489;  // <recording_mode_clause> ::= RECORDING <_mode> <_is> <recording_mode>
//		final int PROD_RECORDING_MODE_F                                                      =  490;  // <recording_mode> ::= F
//		final int PROD_RECORDING_MODE_V                                                      =  491;  // <recording_mode> ::= V
//		final int PROD_RECORDING_MODE_FIXED                                                  =  492;  // <recording_mode> ::= FIXED
//		final int PROD_RECORDING_MODE_VARIABLE                                               =  493;  // <recording_mode> ::= VARIABLE
//		final int PROD_RECORDING_MODE                                                        =  494;  // <recording_mode> ::= <u_or_s>
//		final int PROD_U_OR_S_U                                                              =  495;  // <u_or_s> ::= U
//		final int PROD_U_OR_S_S                                                              =  496;  // <u_or_s> ::= S
//		final int PROD_CODE_SET_CLAUSE_CODE_SET                                              =  497;  // <code_set_clause> ::= 'CODE_SET' <_is> <alphabet_name> <_for_sub_records_clause>
//		final int PROD__FOR_SUB_RECORDS_CLAUSE                                               =  498;  // <_for_sub_records_clause> ::=
//		final int PROD__FOR_SUB_RECORDS_CLAUSE_FOR                                           =  499;  // <_for_sub_records_clause> ::= FOR <reference_list>
//		final int PROD_REPORT_CLAUSE                                                         =  500;  // <report_clause> ::= <report_keyword> <rep_name_list>
//		final int PROD_REPORT_KEYWORD_REPORT                                                 =  501;  // <report_keyword> ::= REPORT <_is>
//		final int PROD_REPORT_KEYWORD_REPORTS                                                =  502;  // <report_keyword> ::= REPORTS <_are>
//		final int PROD_REP_NAME_LIST                                                         =  503;  // <rep_name_list> ::= <undefined_word>
//		final int PROD_REP_NAME_LIST2                                                        =  504;  // <rep_name_list> ::= <rep_name_list> <undefined_word>
//		final int PROD__COMMUNICATION_SECTION                                                =  505;  // <_communication_section> ::=
//		final int PROD__COMMUNICATION_SECTION_COMMUNICATION_SECTION_TOK_DOT                  =  506;  // <_communication_section> ::= COMMUNICATION SECTION 'TOK_DOT' <_communication_description_sequence>
//		final int PROD__COMMUNICATION_DESCRIPTION_SEQUENCE                                   =  507;  // <_communication_description_sequence> ::=
//		final int PROD__COMMUNICATION_DESCRIPTION_SEQUENCE2                                  =  508;  // <_communication_description_sequence> ::= <_communication_description_sequence> <communication_description>
//		final int PROD_COMMUNICATION_DESCRIPTION                                             =  509;  // <communication_description> ::= <communication_description_entry> <_record_description_list>
//		final int PROD_COMMUNICATION_DESCRIPTION_ENTRY_CD_TOK_DOT                            =  510;  // <communication_description_entry> ::= CD <undefined_word> <_communication_description_clause_sequence> 'TOK_DOT'
//		final int PROD__COMMUNICATION_DESCRIPTION_CLAUSE_SEQUENCE                            =  511;  // <_communication_description_clause_sequence> ::=
//		final int PROD__COMMUNICATION_DESCRIPTION_CLAUSE_SEQUENCE2                           =  512;  // <_communication_description_clause_sequence> ::= <_communication_description_clause_sequence> <communication_description_clause>
//		final int PROD_COMMUNICATION_DESCRIPTION_CLAUSE_INPUT                                =  513;  // <communication_description_clause> ::= <_for> <_initial> INPUT <_input_cd_clauses>
//		final int PROD_COMMUNICATION_DESCRIPTION_CLAUSE_OUTPUT                               =  514;  // <communication_description_clause> ::= <_for> OUTPUT <_output_cd_clauses>
//		final int PROD_COMMUNICATION_DESCRIPTION_CLAUSE_I_O                                  =  515;  // <communication_description_clause> ::= <_for> <_initial> 'I_O' <_i_o_cd_clauses>
//		final int PROD__INPUT_CD_CLAUSES                                                     =  516;  // <_input_cd_clauses> ::=
//		final int PROD__INPUT_CD_CLAUSES2                                                    =  517;  // <_input_cd_clauses> ::= <named_input_cd_clauses>
//		final int PROD__INPUT_CD_CLAUSES3                                                    =  518;  // <_input_cd_clauses> ::= <unnamed_input_cd_clauses>
//		final int PROD_NAMED_INPUT_CD_CLAUSES                                                =  519;  // <named_input_cd_clauses> ::= <named_input_cd_clause>
//		final int PROD_NAMED_INPUT_CD_CLAUSES2                                               =  520;  // <named_input_cd_clauses> ::= <named_input_cd_clauses> <named_input_cd_clause>
//		final int PROD_NAMED_INPUT_CD_CLAUSE_QUEUE                                           =  521;  // <named_input_cd_clause> ::= <_symbolic> QUEUE <_is> <identifier>
//		final int PROD_NAMED_INPUT_CD_CLAUSE_SUB_QUEUE_1                                     =  522;  // <named_input_cd_clause> ::= <_symbolic> 'SUB_QUEUE_1' <_is> <identifier>
//		final int PROD_NAMED_INPUT_CD_CLAUSE_SUB_QUEUE_2                                     =  523;  // <named_input_cd_clause> ::= <_symbolic> 'SUB_QUEUE_2' <_is> <identifier>
//		final int PROD_NAMED_INPUT_CD_CLAUSE_SUB_QUEUE_3                                     =  524;  // <named_input_cd_clause> ::= <_symbolic> 'SUB_QUEUE_3' <_is> <identifier>
//		final int PROD_NAMED_INPUT_CD_CLAUSE_MESSAGE_DATE                                    =  525;  // <named_input_cd_clause> ::= MESSAGE DATE <_is> <identifier>
//		final int PROD_NAMED_INPUT_CD_CLAUSE_MESSAGE_TIME                                    =  526;  // <named_input_cd_clause> ::= MESSAGE TIME <_is> <identifier>
//		final int PROD_NAMED_INPUT_CD_CLAUSE_SOURCE                                          =  527;  // <named_input_cd_clause> ::= <_symbolic> SOURCE <_is> <identifier>
//		final int PROD_NAMED_INPUT_CD_CLAUSE_TEXT_LENGTH                                     =  528;  // <named_input_cd_clause> ::= TEXT LENGTH <_is> <identifier>
//		final int PROD_NAMED_INPUT_CD_CLAUSE_END_KEY                                         =  529;  // <named_input_cd_clause> ::= END KEY <_is> <identifier>
//		final int PROD_NAMED_INPUT_CD_CLAUSE_STATUS_KEY                                      =  530;  // <named_input_cd_clause> ::= STATUS KEY <_is> <identifier>
//		final int PROD_NAMED_INPUT_CD_CLAUSE_COUNT                                           =  531;  // <named_input_cd_clause> ::= <_message> COUNT <_is> <identifier>
//		final int PROD_UNNAMED_INPUT_CD_CLAUSES                                              =  532;  // <unnamed_input_cd_clauses> ::= <identifier> <identifier> <identifier> <identifier> <identifier> <identifier> <identifier> <identifier> <identifier> <identifier> <identifier>
//		final int PROD__OUTPUT_CD_CLAUSES                                                    =  533;  // <_output_cd_clauses> ::=
//		final int PROD__OUTPUT_CD_CLAUSES2                                                   =  534;  // <_output_cd_clauses> ::= <output_cd_clauses>
//		final int PROD_OUTPUT_CD_CLAUSES                                                     =  535;  // <output_cd_clauses> ::= <output_cd_clause>
//		final int PROD_OUTPUT_CD_CLAUSES2                                                    =  536;  // <output_cd_clauses> ::= <output_cd_clauses> <output_cd_clause>
//		final int PROD_OUTPUT_CD_CLAUSE_DESTINATION_COUNT                                    =  537;  // <output_cd_clause> ::= DESTINATION COUNT <_is> <identifier>
//		final int PROD_OUTPUT_CD_CLAUSE_TEXT_LENGTH                                          =  538;  // <output_cd_clause> ::= TEXT LENGTH <_is> <identifier>
//		final int PROD_OUTPUT_CD_CLAUSE_STATUS_KEY                                           =  539;  // <output_cd_clause> ::= STATUS KEY <_is> <identifier>
//		final int PROD_OUTPUT_CD_CLAUSE_DESTINATION_TABLE_OCCURS                             =  540;  // <output_cd_clause> ::= DESTINATION TABLE OCCURS <integer> <_times> <_occurs_indexed>
//		final int PROD_OUTPUT_CD_CLAUSE_ERROR_KEY                                            =  541;  // <output_cd_clause> ::= ERROR KEY <_is> <identifier>
//		final int PROD_OUTPUT_CD_CLAUSE_DESTINATION                                          =  542;  // <output_cd_clause> ::= DESTINATION <_is> <identifier>
//		final int PROD_OUTPUT_CD_CLAUSE_SYMBOLIC_DESTINATION                                 =  543;  // <output_cd_clause> ::= SYMBOLIC DESTINATION <_is> <identifier>
//		final int PROD__I_O_CD_CLAUSES                                                       =  544;  // <_i_o_cd_clauses> ::=
//		final int PROD__I_O_CD_CLAUSES2                                                      =  545;  // <_i_o_cd_clauses> ::= <named_i_o_cd_clauses>
//		final int PROD__I_O_CD_CLAUSES3                                                      =  546;  // <_i_o_cd_clauses> ::= <unnamed_i_o_cd_clauses>
//		final int PROD_NAMED_I_O_CD_CLAUSES                                                  =  547;  // <named_i_o_cd_clauses> ::= <named_i_o_cd_clause>
//		final int PROD_NAMED_I_O_CD_CLAUSES2                                                 =  548;  // <named_i_o_cd_clauses> ::= <named_i_o_cd_clauses> <named_i_o_cd_clause>
//		final int PROD_NAMED_I_O_CD_CLAUSE_MESSAGE_DATE                                      =  549;  // <named_i_o_cd_clause> ::= MESSAGE DATE <_is> <identifier>
//		final int PROD_NAMED_I_O_CD_CLAUSE_MESSAGE_TIME                                      =  550;  // <named_i_o_cd_clause> ::= MESSAGE TIME <_is> <identifier>
//		final int PROD_NAMED_I_O_CD_CLAUSE_TERMINAL                                          =  551;  // <named_i_o_cd_clause> ::= <_symbolic> TERMINAL <_is> <identifier>
//		final int PROD_NAMED_I_O_CD_CLAUSE_TEXT_LENGTH                                       =  552;  // <named_i_o_cd_clause> ::= TEXT LENGTH <_is> <identifier>
//		final int PROD_NAMED_I_O_CD_CLAUSE_END_KEY                                           =  553;  // <named_i_o_cd_clause> ::= END KEY <_is> <identifier>
//		final int PROD_NAMED_I_O_CD_CLAUSE_STATUS_KEY                                        =  554;  // <named_i_o_cd_clause> ::= STATUS KEY <_is> <identifier>
//		final int PROD_UNNAMED_I_O_CD_CLAUSES                                                =  555;  // <unnamed_i_o_cd_clauses> ::= <identifier> <identifier> <identifier> <identifier> <identifier> <identifier>
//		final int PROD__WORKING_STORAGE_SECTION                                              =  556;  // <_working_storage_section> ::=
		final int PROD__WORKING_STORAGE_SECTION_WORKING_STORAGE_SECTION_TOK_DOT              =  557;  // <_working_storage_section> ::= 'WORKING_STORAGE' SECTION 'TOK_DOT' <_record_description_list>
		final int PROD__RECORD_DESCRIPTION_LIST                                              =  558;  // <_record_description_list> ::=
//		final int PROD__RECORD_DESCRIPTION_LIST2                                             =  559;  // <_record_description_list> ::= <record_description_list>
//		final int PROD_RECORD_DESCRIPTION_LIST_TOK_DOT                                       =  560;  // <record_description_list> ::= <data_description> 'TOK_DOT'
		final int PROD_RECORD_DESCRIPTION_LIST_TOK_DOT2                                      =  561;  // <record_description_list> ::= <record_description_list> <data_description> 'TOK_DOT'
//		final int PROD_DATA_DESCRIPTION                                                      =  562;  // <data_description> ::= <constant_entry>
//		final int PROD_DATA_DESCRIPTION2                                                     =  563;  // <data_description> ::= <renames_entry>
//		final int PROD_DATA_DESCRIPTION3                                                     =  564;  // <data_description> ::= <condition_name_entry>
		final int PROD_DATA_DESCRIPTION4                                                     =  565;  // <data_description> ::= <level_number> <_entry_name> <_data_description_clause_sequence>
//		final int PROD_LEVEL_NUMBER_INTLITERAL                                               =  566;  // <level_number> ::= IntLiteral
//		final int PROD__FILLER                                                               =  567;  // <_filler> ::=
//		final int PROD__FILLER_FILLER                                                        =  568;  // <_filler> ::= FILLER
//		final int PROD__ENTRY_NAME                                                           =  569;  // <_entry_name> ::= <_filler>
//		final int PROD__ENTRY_NAME2                                                          =  570;  // <_entry_name> ::= <user_entry_name>
//		final int PROD_USER_ENTRY_NAME                                                       =  571;  // <user_entry_name> ::= <WORD>
//		final int PROD_CONST_GLOBAL                                                          =  572;  // <const_global> ::=
		final int PROD_CONST_GLOBAL_GLOBAL                                                   =  573;  // <const_global> ::= <_is> GLOBAL
//		final int PROD_LIT_OR_LENGTH                                                         =  574;  // <lit_or_length> ::= <literal>
//		final int PROD_LIT_OR_LENGTH_LENGTH_OF                                               =  575;  // <lit_or_length> ::= 'LENGTH_OF' <con_identifier>
//		final int PROD_LIT_OR_LENGTH_LENGTH                                                  =  576;  // <lit_or_length> ::= LENGTH <con_identifier>
//		final int PROD_LIT_OR_LENGTH_BYTE_LENGTH                                             =  577;  // <lit_or_length> ::= 'BYTE_LENGTH' <_of> <con_identifier>
//		final int PROD_CON_IDENTIFIER                                                        =  578;  // <con_identifier> ::= <identifier_1>
//		final int PROD_CON_IDENTIFIER_BINARY_CHAR                                            =  579;  // <con_identifier> ::= 'BINARY_CHAR'
//		final int PROD_CON_IDENTIFIER_BINARY_SHORT                                           =  580;  // <con_identifier> ::= 'BINARY_SHORT'
//		final int PROD_CON_IDENTIFIER_BINARY_LONG                                            =  581;  // <con_identifier> ::= 'BINARY_LONG'
//		final int PROD_CON_IDENTIFIER_BINARY_DOUBLE                                          =  582;  // <con_identifier> ::= 'BINARY_DOUBLE'
//		final int PROD_CON_IDENTIFIER_BINARY_C_LONG                                          =  583;  // <con_identifier> ::= 'BINARY_C_LONG'
//		final int PROD_CON_IDENTIFIER2                                                       =  584;  // <con_identifier> ::= <pointer_len>
//		final int PROD_CON_IDENTIFIER3                                                       =  585;  // <con_identifier> ::= <float_usage>
//		final int PROD_CON_IDENTIFIER4                                                       =  586;  // <con_identifier> ::= <double_usage>
//		final int PROD_CON_IDENTIFIER5                                                       =  587;  // <con_identifier> ::= <fp32_usage>
//		final int PROD_CON_IDENTIFIER6                                                       =  588;  // <con_identifier> ::= <fp64_usage>
//		final int PROD_CON_IDENTIFIER7                                                       =  589;  // <con_identifier> ::= <fp128_usage>
//		final int PROD_FP32_USAGE_FLOAT_BINARY_32                                            =  590;  // <fp32_usage> ::= 'FLOAT_BINARY_32'
//		final int PROD_FP32_USAGE_FLOAT_DECIMAL_7                                            =  591;  // <fp32_usage> ::= 'FLOAT_DECIMAL_7'
//		final int PROD_FP64_USAGE_FLOAT_BINARY_64                                            =  592;  // <fp64_usage> ::= 'FLOAT_BINARY_64'
//		final int PROD_FP64_USAGE_FLOAT_DECIMAL_16                                           =  593;  // <fp64_usage> ::= 'FLOAT_DECIMAL_16'
//		final int PROD_FP128_USAGE_FLOAT_BINARY_128                                          =  594;  // <fp128_usage> ::= 'FLOAT_BINARY_128'
//		final int PROD_FP128_USAGE_FLOAT_DECIMAL_34                                          =  595;  // <fp128_usage> ::= 'FLOAT_DECIMAL_34'
//		final int PROD_FP128_USAGE_FLOAT_EXTENDED                                            =  596;  // <fp128_usage> ::= 'FLOAT_EXTENDED'
//		final int PROD_POINTER_LEN_POINTER                                                   =  597;  // <pointer_len> ::= POINTER
//		final int PROD_POINTER_LEN_PROGRAM_POINTER                                           =  598;  // <pointer_len> ::= 'PROGRAM_POINTER'
//		final int PROD_RENAMES_ENTRY_SIXTY_SIX_RENAMES                                       =  599;  // <renames_entry> ::= 'SIXTY_SIX' <user_entry_name> RENAMES <qualified_word> <_renames_thru>
//		final int PROD__RENAMES_THRU                                                         =  600;  // <_renames_thru> ::=
//		final int PROD__RENAMES_THRU_THRU                                                    =  601;  // <_renames_thru> ::= THRU <qualified_word>
		final int PROD_CONDITION_NAME_ENTRY_EIGHTY_EIGHT                                     =  602;  // <condition_name_entry> ::= 'EIGHTY_EIGHT' <user_entry_name> <value_clause>
		final int PROD_CONSTANT_ENTRY_CONSTANT                                               =  603;  // <constant_entry> ::= <level_number> <user_entry_name> CONSTANT <const_global> <constant_source>
		final int PROD_CONSTANT_ENTRY_SEVENTY_EIGHT                                          =  604;  // <constant_entry> ::= 'SEVENTY_EIGHT' <user_entry_name> <_global_clause> <value_clause>
//		final int PROD_CONSTANT_SOURCE                                                       =  605;  // <constant_source> ::= <_as> <lit_or_length>
//		final int PROD_CONSTANT_SOURCE_FROM                                                  =  606;  // <constant_source> ::= FROM <WORD>
//		final int PROD__DATA_DESCRIPTION_CLAUSE_SEQUENCE                                     =  607;  // <_data_description_clause_sequence> ::=
		final int PROD__DATA_DESCRIPTION_CLAUSE_SEQUENCE2                                    =  608;  // <_data_description_clause_sequence> ::= <_data_description_clause_sequence> <data_description_clause>
//		final int PROD_DATA_DESCRIPTION_CLAUSE                                               =  609;  // <data_description_clause> ::= <redefines_clause>
//		final int PROD_DATA_DESCRIPTION_CLAUSE2                                              =  610;  // <data_description_clause> ::= <external_clause>
//		final int PROD_DATA_DESCRIPTION_CLAUSE3                                              =  611;  // <data_description_clause> ::= <global_clause>
//		final int PROD_DATA_DESCRIPTION_CLAUSE4                                              =  612;  // <data_description_clause> ::= <picture_clause>
//		final int PROD_DATA_DESCRIPTION_CLAUSE5                                              =  613;  // <data_description_clause> ::= <usage_clause>
//		final int PROD_DATA_DESCRIPTION_CLAUSE6                                              =  614;  // <data_description_clause> ::= <sign_clause>
//		final int PROD_DATA_DESCRIPTION_CLAUSE7                                              =  615;  // <data_description_clause> ::= <occurs_clause>
//		final int PROD_DATA_DESCRIPTION_CLAUSE8                                              =  616;  // <data_description_clause> ::= <justified_clause>
//		final int PROD_DATA_DESCRIPTION_CLAUSE9                                              =  617;  // <data_description_clause> ::= <synchronized_clause>
//		final int PROD_DATA_DESCRIPTION_CLAUSE10                                             =  618;  // <data_description_clause> ::= <blank_clause>
//		final int PROD_DATA_DESCRIPTION_CLAUSE11                                             =  619;  // <data_description_clause> ::= <based_clause>
//		final int PROD_DATA_DESCRIPTION_CLAUSE12                                             =  620;  // <data_description_clause> ::= <value_clause>
//		final int PROD_DATA_DESCRIPTION_CLAUSE13                                             =  621;  // <data_description_clause> ::= <any_length_clause>
		final int PROD_REDEFINES_CLAUSE_REDEFINES                                            =  622;  // <redefines_clause> ::= REDEFINES <identifier_1>
		final int PROD_EXTERNAL_CLAUSE_EXTERNAL                                              =  623;  // <external_clause> ::= <_is> EXTERNAL <_as_extname>
//		final int PROD__AS_EXTNAME                                                           =  624;  // <_as_extname> ::=
//		final int PROD__AS_EXTNAME_AS                                                        =  625;  // <_as_extname> ::= AS <LITERAL_TOK>
//		final int PROD__GLOBAL_CLAUSE                                                        =  626;  // <_global_clause> ::=
//		final int PROD__GLOBAL_CLAUSE2                                                       =  627;  // <_global_clause> ::= <global_clause>
		final int PROD_GLOBAL_CLAUSE_GLOBAL                                                  =  628;  // <global_clause> ::= <_is> GLOBAL
		final int PROD_PICTURE_CLAUSE_PICTURE_DEF                                            =  629;  // <picture_clause> ::= 'Picture_Def'
//		final int PROD_USAGE_CLAUSE                                                          =  630;  // <usage_clause> ::= <usage>
		final int PROD_USAGE_CLAUSE_USAGE                                                    =  631;  // <usage_clause> ::= USAGE <_is> <usage>
		final int PROD_USAGE_BINARY                                                          =  632;  // <usage> ::= BINARY
		final int PROD_USAGE_COMP                                                            =  633;  // <usage> ::= COMP
		final int PROD_USAGE                                                                 =  634;  // <usage> ::= <float_usage>
		final int PROD_USAGE2                                                                =  635;  // <usage> ::= <double_usage>
		final int PROD_USAGE_COMP_3                                                          =  636;  // <usage> ::= 'COMP_3'
		final int PROD_USAGE_COMP_4                                                          =  637;  // <usage> ::= 'COMP_4'
		final int PROD_USAGE_COMP_5                                                          =  638;  // <usage> ::= 'COMP_5'
		final int PROD_USAGE_COMP_6                                                          =  639;  // <usage> ::= 'COMP_6'
		final int PROD_USAGE_COMP_X                                                          =  640;  // <usage> ::= 'COMP_X'
		final int PROD_USAGE_DISPLAY                                                         =  641;  // <usage> ::= DISPLAY
		final int PROD_USAGE_INDEX                                                           =  642;  // <usage> ::= INDEX
		final int PROD_USAGE_PACKED_DECIMAL                                                  =  643;  // <usage> ::= 'PACKED_DECIMAL'
		final int PROD_USAGE_POINTER                                                         =  644;  // <usage> ::= POINTER
		final int PROD_USAGE_PROGRAM_POINTER                                                 =  645;  // <usage> ::= 'PROGRAM_POINTER'
		final int PROD_USAGE_SIGNED_SHORT                                                    =  646;  // <usage> ::= 'SIGNED_SHORT'
		final int PROD_USAGE_SIGNED_INT                                                      =  647;  // <usage> ::= 'SIGNED_INT'
		final int PROD_USAGE_SIGNED_LONG                                                     =  648;  // <usage> ::= 'SIGNED_LONG'
		final int PROD_USAGE_UNSIGNED_SHORT                                                  =  649;  // <usage> ::= 'UNSIGNED_SHORT'
		final int PROD_USAGE_UNSIGNED_INT                                                    =  650;  // <usage> ::= 'UNSIGNED_INT'
		final int PROD_USAGE_UNSIGNED_LONG                                                   =  651;  // <usage> ::= 'UNSIGNED_LONG'
		final int PROD_USAGE_BINARY_CHAR                                                     =  652;  // <usage> ::= 'BINARY_CHAR' <_signed>
		final int PROD_USAGE_BINARY_CHAR_UNSIGNED                                            =  653;  // <usage> ::= 'BINARY_CHAR' UNSIGNED
		final int PROD_USAGE_BINARY_SHORT                                                    =  654;  // <usage> ::= 'BINARY_SHORT' <_signed>
		final int PROD_USAGE_BINARY_SHORT_UNSIGNED                                           =  655;  // <usage> ::= 'BINARY_SHORT' UNSIGNED
		final int PROD_USAGE_BINARY_LONG                                                     =  656;  // <usage> ::= 'BINARY_LONG' <_signed>
		final int PROD_USAGE_BINARY_LONG_UNSIGNED                                            =  657;  // <usage> ::= 'BINARY_LONG' UNSIGNED
		final int PROD_USAGE_BINARY_DOUBLE                                                   =  658;  // <usage> ::= 'BINARY_DOUBLE' <_signed>
		final int PROD_USAGE_BINARY_DOUBLE_UNSIGNED                                          =  659;  // <usage> ::= 'BINARY_DOUBLE' UNSIGNED
		final int PROD_USAGE_BINARY_C_LONG                                                   =  660;  // <usage> ::= 'BINARY_C_LONG' <_signed>
		final int PROD_USAGE_BINARY_C_LONG_UNSIGNED                                          =  661;  // <usage> ::= 'BINARY_C_LONG' UNSIGNED
		final int PROD_USAGE_FLOAT_BINARY_32                                                 =  662;  // <usage> ::= 'FLOAT_BINARY_32'
		final int PROD_USAGE_FLOAT_BINARY_64                                                 =  663;  // <usage> ::= 'FLOAT_BINARY_64'
		final int PROD_USAGE_FLOAT_BINARY_128                                                =  664;  // <usage> ::= 'FLOAT_BINARY_128'
		final int PROD_USAGE_FLOAT_DECIMAL_16                                                =  665;  // <usage> ::= 'FLOAT_DECIMAL_16'
		final int PROD_USAGE_FLOAT_DECIMAL_34                                                =  666;  // <usage> ::= 'FLOAT_DECIMAL_34'
//		final int PROD_USAGE_NATIONAL                                                        =  667;  // <usage> ::= NATIONAL
		final int PROD_FLOAT_USAGE_COMP_1                                                    =  668;  // <float_usage> ::= 'COMP_1'
		final int PROD_FLOAT_USAGE_FLOAT_SHORT                                               =  669;  // <float_usage> ::= 'FLOAT_SHORT'
		final int PROD_DOUBLE_USAGE_COMP_2                                                   =  670;  // <double_usage> ::= 'COMP_2'
		final int PROD_DOUBLE_USAGE_FLOAT_LONG                                               =  671;  // <double_usage> ::= 'FLOAT_LONG'
//		final int PROD_SIGN_CLAUSE_LEADING                                                   =  672;  // <sign_clause> ::= <_sign_is> LEADING <flag_separate>
//		final int PROD_SIGN_CLAUSE_TRAILING                                                  =  673;  // <sign_clause> ::= <_sign_is> TRAILING <flag_separate>
//		final int PROD_REPORT_OCCURS_CLAUSE_OCCURS                                           =  674;  // <report_occurs_clause> ::= OCCURS <integer_or_word> <_occurs_to_integer> <_times> <_occurs_depending> <_occurs_step>
//		final int PROD__OCCURS_STEP                                                          =  675;  // <_occurs_step> ::=
//		final int PROD__OCCURS_STEP_STEP                                                     =  676;  // <_occurs_step> ::= STEP <integer_or_word>
		final int PROD_OCCURS_CLAUSE_OCCURS                                                  =  677;  // <occurs_clause> ::= OCCURS <integer_or_word> <_occurs_to_integer> <_times> <_occurs_depending> <_occurs_keys_and_indexed>
//		final int PROD_OCCURS_CLAUSE_OCCURS_UNBOUNDED_DEPENDING                              =  678;  // <occurs_clause> ::= OCCURS <_occurs_integer_to> UNBOUNDED <_times> DEPENDING <_on> <reference> <_occurs_keys_and_indexed>
//		final int PROD_OCCURS_CLAUSE_OCCURS_DYNAMIC                                          =  679;  // <occurs_clause> ::= OCCURS DYNAMIC <_capacity_in> <_occurs_from_integer> <_occurs_to_integer> <_occurs_initialized> <_occurs_keys_and_indexed>
//		final int PROD__OCCURS_TO_INTEGER                                                    =  680;  // <_occurs_to_integer> ::=
		final int PROD__OCCURS_TO_INTEGER_TO                                                 =  681;  // <_occurs_to_integer> ::= TO <integer_or_word>
//		final int PROD__OCCURS_FROM_INTEGER                                                  =  682;  // <_occurs_from_integer> ::=
//		final int PROD__OCCURS_FROM_INTEGER_FROM                                             =  683;  // <_occurs_from_integer> ::= FROM <integer_or_word>
//		final int PROD__OCCURS_INTEGER_TO                                                    =  684;  // <_occurs_integer_to> ::=
//		final int PROD__OCCURS_INTEGER_TO_TO                                                 =  685;  // <_occurs_integer_to> ::= <integer_or_word> TO
//		final int PROD_INTEGER_OR_WORD                                                       =  686;  // <integer_or_word> ::= <integer>
//		final int PROD_INTEGER_OR_WORD_COBOLWORD                                             =  687;  // <integer_or_word> ::= COBOLWord
//		final int PROD__OCCURS_DEPENDING                                                     =  688;  // <_occurs_depending> ::=
//		final int PROD__OCCURS_DEPENDING_DEPENDING                                           =  689;  // <_occurs_depending> ::= DEPENDING <_on> <reference>
//		final int PROD__CAPACITY_IN                                                          =  690;  // <_capacity_in> ::=
//		final int PROD__CAPACITY_IN_CAPACITY                                                 =  691;  // <_capacity_in> ::= CAPACITY <_in> <WORD>
//		final int PROD__OCCURS_INITIALIZED                                                   =  692;  // <_occurs_initialized> ::=
//		final int PROD__OCCURS_INITIALIZED_INITIALIZED                                       =  693;  // <_occurs_initialized> ::= INITIALIZED
//		final int PROD__OCCURS_KEYS_AND_INDEXED                                              =  694;  // <_occurs_keys_and_indexed> ::=
		final int PROD__OCCURS_KEYS_AND_INDEXED2                                             =  695;  // <_occurs_keys_and_indexed> ::= <occurs_keys> <occurs_indexed>
		final int PROD__OCCURS_KEYS_AND_INDEXED3                                             =  696;  // <_occurs_keys_and_indexed> ::= <occurs_indexed> <occurs_keys>
		final int PROD__OCCURS_KEYS_AND_INDEXED4                                             =  697;  // <_occurs_keys_and_indexed> ::= <occurs_indexed>
//		final int PROD__OCCURS_KEYS_AND_INDEXED5                                             =  698;  // <_occurs_keys_and_indexed> ::= <occurs_keys>
//		final int PROD_OCCURS_KEYS                                                           =  699;  // <occurs_keys> ::= <occurs_key_list>
//		final int PROD_OCCURS_KEY_LIST                                                       =  700;  // <occurs_key_list> ::= <occurs_key_field>
//		final int PROD_OCCURS_KEY_LIST2                                                      =  701;  // <occurs_key_list> ::= <occurs_key_field> <occurs_key_list>
//		final int PROD_OCCURS_KEY_FIELD                                                      =  702;  // <occurs_key_field> ::= <ascending_or_descending> <_key> <_is> <reference_list>
//		final int PROD_ASCENDING_OR_DESCENDING_ASCENDING                                     =  703;  // <ascending_or_descending> ::= ASCENDING
//		final int PROD_ASCENDING_OR_DESCENDING_DESCENDING                                    =  704;  // <ascending_or_descending> ::= DESCENDING
//		final int PROD__OCCURS_INDEXED                                                       =  705;  // <_occurs_indexed> ::=
//		final int PROD__OCCURS_INDEXED2                                                      =  706;  // <_occurs_indexed> ::= <occurs_indexed>
		final int PROD_OCCURS_INDEXED_INDEXED                                                =  707;  // <occurs_indexed> ::= INDEXED <_by> <occurs_index_list>
//		final int PROD_OCCURS_INDEX_LIST                                                     =  708;  // <occurs_index_list> ::= <occurs_index>
		final int PROD_OCCURS_INDEX_LIST2                                                    =  709;  // <occurs_index_list> ::= <occurs_index_list> <occurs_index>
//		final int PROD_OCCURS_INDEX                                                          =  710;  // <occurs_index> ::= <WORD>
//		final int PROD_JUSTIFIED_CLAUSE_JUSTIFIED                                            =  711;  // <justified_clause> ::= JUSTIFIED <_right>
//		final int PROD_SYNCHRONIZED_CLAUSE_SYNCHRONIZED                                      =  712;  // <synchronized_clause> ::= SYNCHRONIZED <_left_or_right>
//		final int PROD_BLANK_CLAUSE_BLANK_ZERO                                               =  713;  // <blank_clause> ::= BLANK <_when> ZERO
//		final int PROD_BASED_CLAUSE_BASED                                                    =  714;  // <based_clause> ::= BASED
		final int PROD_VALUE_CLAUSE_VALUE                                                    =  715;  // <value_clause> ::= VALUE <_is_are> <value_item_list> <_false_is>
//		final int PROD_VALUE_ITEM_LIST                                                       =  716;  // <value_item_list> ::= <value_item>
//		final int PROD_VALUE_ITEM_LIST2                                                      =  717;  // <value_item_list> ::= <value_item_list> <value_item>
//		final int PROD_VALUE_ITEM                                                            =  718;  // <value_item> ::= <lit_or_length>
//		final int PROD_VALUE_ITEM_THRU                                                       =  719;  // <value_item> ::= <lit_or_length> THRU <lit_or_length>
		final int PROD_VALUE_ITEM_COMMA_DELIM                                                =  720;  // <value_item> ::= 'COMMA_DELIM'
//		final int PROD__FALSE_IS                                                             =  721;  // <_false_is> ::=
//		final int PROD__FALSE_IS_TOK_FALSE                                                   =  722;  // <_false_is> ::= <_when_set_to> 'TOK_FALSE' <_is> <lit_or_length>
		final int PROD_ANY_LENGTH_CLAUSE_ANY_LENGTH                                          =  723;  // <any_length_clause> ::= ANY LENGTH
		final int PROD_ANY_LENGTH_CLAUSE_ANY_NUMERIC                                         =  724;  // <any_length_clause> ::= ANY NUMERIC
//		final int PROD__LOCAL_STORAGE_SECTION                                                =  725;  // <_local_storage_section> ::=
		final int PROD__LOCAL_STORAGE_SECTION_LOCAL_STORAGE_SECTION_TOK_DOT                  =  726;  // <_local_storage_section> ::= 'LOCAL_STORAGE' SECTION 'TOK_DOT' <_record_description_list>
//		final int PROD__LINKAGE_SECTION                                                      =  727;  // <_linkage_section> ::=
		final int PROD__LINKAGE_SECTION_LINKAGE_SECTION_TOK_DOT                              =  728;  // <_linkage_section> ::= LINKAGE SECTION 'TOK_DOT' <_record_description_list>
//		final int PROD__REPORT_SECTION                                                       =  729;  // <_report_section> ::=
		final int PROD__REPORT_SECTION_REPORT_SECTION_TOK_DOT                                =  730;  // <_report_section> ::= REPORT SECTION 'TOK_DOT' <_report_description_sequence>
//		final int PROD__REPORT_DESCRIPTION_SEQUENCE                                          =  731;  // <_report_description_sequence> ::=
//		final int PROD__REPORT_DESCRIPTION_SEQUENCE2                                         =  732;  // <_report_description_sequence> ::= <_report_description_sequence> <report_description>
//		final int PROD_REPORT_DESCRIPTION_RD_TOK_DOT                                         =  733;  // <report_description> ::= RD <report_name> <_report_description_options> 'TOK_DOT' <_report_group_description_list>
//		final int PROD__REPORT_DESCRIPTION_OPTIONS                                           =  734;  // <_report_description_options> ::=
//		final int PROD__REPORT_DESCRIPTION_OPTIONS2                                          =  735;  // <_report_description_options> ::= <_report_description_options> <report_description_option>
//		final int PROD_REPORT_DESCRIPTION_OPTION_GLOBAL                                      =  736;  // <report_description_option> ::= <_is> GLOBAL
//		final int PROD_REPORT_DESCRIPTION_OPTION_CODE                                        =  737;  // <report_description_option> ::= CODE <_is> <id_or_lit>
//		final int PROD_REPORT_DESCRIPTION_OPTION                                             =  738;  // <report_description_option> ::= <control_clause>
//		final int PROD_REPORT_DESCRIPTION_OPTION2                                            =  739;  // <report_description_option> ::= <page_limit_clause>
//		final int PROD_CONTROL_CLAUSE                                                        =  740;  // <control_clause> ::= <control_keyword> <control_field_list>
//		final int PROD_CONTROL_FIELD_LIST                                                    =  741;  // <control_field_list> ::= <_final> <identifier_list>
//		final int PROD_IDENTIFIER_LIST                                                       =  742;  // <identifier_list> ::= <identifier>
//		final int PROD_IDENTIFIER_LIST2                                                      =  743;  // <identifier_list> ::= <identifier_list> <identifier>
//		final int PROD_PAGE_LIMIT_CLAUSE_PAGE                                                =  744;  // <page_limit_clause> ::= PAGE <_limits> <page_line_column> <_page_heading_list>
//		final int PROD_PAGE_LINE_COLUMN                                                      =  745;  // <page_line_column> ::= <report_integer>
//		final int PROD_PAGE_LINE_COLUMN2                                                     =  746;  // <page_line_column> ::= <report_integer> <line_or_lines> <report_integer> <columns_or_cols>
//		final int PROD_PAGE_LINE_COLUMN3                                                     =  747;  // <page_line_column> ::= <report_integer> <line_or_lines>
//		final int PROD__PAGE_HEADING_LIST                                                    =  748;  // <_page_heading_list> ::=
//		final int PROD__PAGE_HEADING_LIST2                                                   =  749;  // <_page_heading_list> ::= <_page_heading_list> <page_detail>
//		final int PROD_PAGE_DETAIL                                                           =  750;  // <page_detail> ::= <heading_clause>
//		final int PROD_PAGE_DETAIL2                                                          =  751;  // <page_detail> ::= <first_detail>
//		final int PROD_PAGE_DETAIL3                                                          =  752;  // <page_detail> ::= <last_heading>
//		final int PROD_PAGE_DETAIL4                                                          =  753;  // <page_detail> ::= <last_detail>
//		final int PROD_PAGE_DETAIL5                                                          =  754;  // <page_detail> ::= <footing_clause>
//		final int PROD_HEADING_CLAUSE_HEADING                                                =  755;  // <heading_clause> ::= HEADING <_is> <report_integer>
//		final int PROD_FIRST_DETAIL_FIRST                                                    =  756;  // <first_detail> ::= FIRST <detail_keyword> <_is> <report_integer>
//		final int PROD_LAST_HEADING_LAST                                                     =  757;  // <last_heading> ::= LAST <ch_keyword> <_is> <report_integer>
//		final int PROD_LAST_DETAIL_LAST                                                      =  758;  // <last_detail> ::= LAST <detail_keyword> <_is> <report_integer>
//		final int PROD_FOOTING_CLAUSE_FOOTING                                                =  759;  // <footing_clause> ::= FOOTING <_is> <report_integer>
//		final int PROD__REPORT_GROUP_DESCRIPTION_LIST                                        =  760;  // <_report_group_description_list> ::=
//		final int PROD__REPORT_GROUP_DESCRIPTION_LIST2                                       =  761;  // <_report_group_description_list> ::= <_report_group_description_list> <report_group_description_entry>
//		final int PROD_REPORT_GROUP_DESCRIPTION_ENTRY_TOK_DOT                                =  762;  // <report_group_description_entry> ::= <level_number> <_entry_name> <_report_group_options> 'TOK_DOT'
//		final int PROD__REPORT_GROUP_OPTIONS                                                 =  763;  // <_report_group_options> ::=
//		final int PROD__REPORT_GROUP_OPTIONS2                                                =  764;  // <_report_group_options> ::= <_report_group_options> <report_group_option>
//		final int PROD_REPORT_GROUP_OPTION                                                   =  765;  // <report_group_option> ::= <type_clause>
//		final int PROD_REPORT_GROUP_OPTION2                                                  =  766;  // <report_group_option> ::= <next_group_clause>
//		final int PROD_REPORT_GROUP_OPTION3                                                  =  767;  // <report_group_option> ::= <line_clause>
//		final int PROD_REPORT_GROUP_OPTION4                                                  =  768;  // <report_group_option> ::= <picture_clause>
//		final int PROD_REPORT_GROUP_OPTION5                                                  =  769;  // <report_group_option> ::= <report_usage_clause>
//		final int PROD_REPORT_GROUP_OPTION6                                                  =  770;  // <report_group_option> ::= <sign_clause>
//		final int PROD_REPORT_GROUP_OPTION7                                                  =  771;  // <report_group_option> ::= <justified_clause>
//		final int PROD_REPORT_GROUP_OPTION8                                                  =  772;  // <report_group_option> ::= <column_clause>
//		final int PROD_REPORT_GROUP_OPTION9                                                  =  773;  // <report_group_option> ::= <blank_clause>
//		final int PROD_REPORT_GROUP_OPTION10                                                 =  774;  // <report_group_option> ::= <source_clause>
//		final int PROD_REPORT_GROUP_OPTION11                                                 =  775;  // <report_group_option> ::= <sum_clause_list>
//		final int PROD_REPORT_GROUP_OPTION12                                                 =  776;  // <report_group_option> ::= <value_clause>
//		final int PROD_REPORT_GROUP_OPTION13                                                 =  777;  // <report_group_option> ::= <present_when_condition>
//		final int PROD_REPORT_GROUP_OPTION14                                                 =  778;  // <report_group_option> ::= <group_indicate_clause>
//		final int PROD_REPORT_GROUP_OPTION15                                                 =  779;  // <report_group_option> ::= <report_occurs_clause>
//		final int PROD_REPORT_GROUP_OPTION16                                                 =  780;  // <report_group_option> ::= <varying_clause>
//		final int PROD_TYPE_CLAUSE_TYPE                                                      =  781;  // <type_clause> ::= TYPE <_is> <type_option>
//		final int PROD_TYPE_OPTION                                                           =  782;  // <type_option> ::= <rh_keyword>
//		final int PROD_TYPE_OPTION2                                                          =  783;  // <type_option> ::= <ph_keyword>
//		final int PROD_TYPE_OPTION3                                                          =  784;  // <type_option> ::= <ch_keyword> <_control_final>
//		final int PROD_TYPE_OPTION4                                                          =  785;  // <type_option> ::= <detail_keyword>
//		final int PROD_TYPE_OPTION5                                                          =  786;  // <type_option> ::= <cf_keyword> <_control_final>
//		final int PROD_TYPE_OPTION6                                                          =  787;  // <type_option> ::= <pf_keyword>
//		final int PROD_TYPE_OPTION7                                                          =  788;  // <type_option> ::= <rf_keyword>
//		final int PROD__CONTROL_FINAL                                                        =  789;  // <_control_final> ::=
//		final int PROD__CONTROL_FINAL2                                                       =  790;  // <_control_final> ::= <identifier> <_or_page>
//		final int PROD__CONTROL_FINAL_FINAL                                                  =  791;  // <_control_final> ::= FINAL <_or_page>
//		final int PROD__OR_PAGE                                                              =  792;  // <_or_page> ::=
//		final int PROD__OR_PAGE_OR_PAGE                                                      =  793;  // <_or_page> ::= OR PAGE
//		final int PROD_NEXT_GROUP_CLAUSE_NEXT_GROUP                                          =  794;  // <next_group_clause> ::= NEXT GROUP <_is> <line_or_plus>
//		final int PROD_SUM_CLAUSE_LIST_SUM                                                   =  795;  // <sum_clause_list> ::= SUM <_of> <report_x_list> <_reset_clause>
//		final int PROD__RESET_CLAUSE                                                         =  796;  // <_reset_clause> ::=
//		final int PROD__RESET_CLAUSE_RESET                                                   =  797;  // <_reset_clause> ::= RESET <_on> <data_or_final>
//		final int PROD_DATA_OR_FINAL                                                         =  798;  // <data_or_final> ::= <identifier>
//		final int PROD_DATA_OR_FINAL_FINAL                                                   =  799;  // <data_or_final> ::= FINAL
//		final int PROD_PRESENT_WHEN_CONDITION_PRESENT_WHEN                                   =  800;  // <present_when_condition> ::= PRESENT WHEN <condition>
//		final int PROD_VARYING_CLAUSE_VARYING_FROM_BY                                        =  801;  // <varying_clause> ::= VARYING <identifier> FROM <arith_x> BY <arith_x>
//		final int PROD_LINE_CLAUSE                                                           =  802;  // <line_clause> ::= <line_keyword_clause> <report_line_integer_list>
//		final int PROD_LINE_KEYWORD_CLAUSE_LINE                                              =  803;  // <line_keyword_clause> ::= LINE <_numbers> <_is_are>
//		final int PROD_LINE_KEYWORD_CLAUSE_LINES                                             =  804;  // <line_keyword_clause> ::= LINES <_are>
//		final int PROD_COLUMN_CLAUSE                                                         =  805;  // <column_clause> ::= <col_keyword_clause> <report_col_integer_list>
//		final int PROD_COL_KEYWORD_CLAUSE                                                    =  806;  // <col_keyword_clause> ::= <column_or_col> <_numbers> <_is_are>
//		final int PROD_COL_KEYWORD_CLAUSE2                                                   =  807;  // <col_keyword_clause> ::= <columns_or_cols> <_are>
//		final int PROD_REPORT_LINE_INTEGER_LIST                                              =  808;  // <report_line_integer_list> ::= <line_or_plus>
//		final int PROD_REPORT_LINE_INTEGER_LIST2                                             =  809;  // <report_line_integer_list> ::= <report_line_integer_list> <line_or_plus>
//		final int PROD_LINE_OR_PLUS_PLUS                                                     =  810;  // <line_or_plus> ::= PLUS <integer>
//		final int PROD_LINE_OR_PLUS                                                          =  811;  // <line_or_plus> ::= <report_integer>
//		final int PROD_LINE_OR_PLUS_NEXT_PAGE                                                =  812;  // <line_or_plus> ::= 'NEXT_PAGE'
//		final int PROD_REPORT_COL_INTEGER_LIST                                               =  813;  // <report_col_integer_list> ::= <col_or_plus>
//		final int PROD_REPORT_COL_INTEGER_LIST2                                              =  814;  // <report_col_integer_list> ::= <report_col_integer_list> <col_or_plus>
//		final int PROD_COL_OR_PLUS_PLUS                                                      =  815;  // <col_or_plus> ::= PLUS <integer>
//		final int PROD_COL_OR_PLUS                                                           =  816;  // <col_or_plus> ::= <report_integer>
//		final int PROD_SOURCE_CLAUSE_SOURCE                                                  =  817;  // <source_clause> ::= SOURCE <_is> <arith_x> <flag_rounded>
//		final int PROD_GROUP_INDICATE_CLAUSE_GROUP                                           =  818;  // <group_indicate_clause> ::= GROUP <_indicate>
//		final int PROD_REPORT_USAGE_CLAUSE_USAGE_DISPLAY                                     =  819;  // <report_usage_clause> ::= USAGE <_is> DISPLAY
//		final int PROD__SCREEN_SECTION                                                       =  820;  // <_screen_section> ::=
		final int PROD__SCREEN_SECTION_SCREEN_SECTION_TOK_DOT                                =  821;  // <_screen_section> ::= SCREEN SECTION 'TOK_DOT' <_screen_description_list>
//		final int PROD__SCREEN_DESCRIPTION_LIST                                              =  822;  // <_screen_description_list> ::=
//		final int PROD__SCREEN_DESCRIPTION_LIST2                                             =  823;  // <_screen_description_list> ::= <screen_description_list>
//		final int PROD_SCREEN_DESCRIPTION_LIST_TOK_DOT                                       =  824;  // <screen_description_list> ::= <screen_description> 'TOK_DOT'
//		final int PROD_SCREEN_DESCRIPTION_LIST_TOK_DOT2                                      =  825;  // <screen_description_list> ::= <screen_description_list> <screen_description> 'TOK_DOT'
//		final int PROD_SCREEN_DESCRIPTION                                                    =  826;  // <screen_description> ::= <constant_entry>
//		final int PROD_SCREEN_DESCRIPTION2                                                   =  827;  // <screen_description> ::= <level_number> <_entry_name> <_screen_options>
//		final int PROD__SCREEN_OPTIONS                                                       =  828;  // <_screen_options> ::=
//		final int PROD__SCREEN_OPTIONS2                                                      =  829;  // <_screen_options> ::= <_screen_options> <screen_option>
//		final int PROD_SCREEN_OPTION_BLANK_LINE                                              =  830;  // <screen_option> ::= BLANK LINE
//		final int PROD_SCREEN_OPTION_BLANK_SCREEN                                            =  831;  // <screen_option> ::= BLANK SCREEN
//		final int PROD_SCREEN_OPTION_BELL                                                    =  832;  // <screen_option> ::= BELL
//		final int PROD_SCREEN_OPTION_BLINK                                                   =  833;  // <screen_option> ::= BLINK
//		final int PROD_SCREEN_OPTION_ERASE                                                   =  834;  // <screen_option> ::= ERASE <eol>
//		final int PROD_SCREEN_OPTION_ERASE2                                                  =  835;  // <screen_option> ::= ERASE <eos>
//		final int PROD_SCREEN_OPTION_HIGHLIGHT                                               =  836;  // <screen_option> ::= HIGHLIGHT
//		final int PROD_SCREEN_OPTION_LOWLIGHT                                                =  837;  // <screen_option> ::= LOWLIGHT
//		final int PROD_SCREEN_OPTION                                                         =  838;  // <screen_option> ::= <reverse_video>
//		final int PROD_SCREEN_OPTION_UNDERLINE                                               =  839;  // <screen_option> ::= UNDERLINE
//		final int PROD_SCREEN_OPTION_OVERLINE                                                =  840;  // <screen_option> ::= OVERLINE
//		final int PROD_SCREEN_OPTION_GRID                                                    =  841;  // <screen_option> ::= GRID
//		final int PROD_SCREEN_OPTION_LEFTLINE                                                =  842;  // <screen_option> ::= LEFTLINE
//		final int PROD_SCREEN_OPTION_AUTO                                                    =  843;  // <screen_option> ::= AUTO
//		final int PROD_SCREEN_OPTION_TAB                                                     =  844;  // <screen_option> ::= TAB
//		final int PROD_SCREEN_OPTION_SECURE                                                  =  845;  // <screen_option> ::= SECURE
//		final int PROD_SCREEN_OPTION2                                                        =  846;  // <screen_option> ::= <no_echo>
//		final int PROD_SCREEN_OPTION_REQUIRED                                                =  847;  // <screen_option> ::= REQUIRED
//		final int PROD_SCREEN_OPTION_FULL                                                    =  848;  // <screen_option> ::= FULL
//		final int PROD_SCREEN_OPTION_PROMPT_CHARACTER                                        =  849;  // <screen_option> ::= PROMPT CHARACTER <_is> <id_or_lit>
//		final int PROD_SCREEN_OPTION_PROMPT                                                  =  850;  // <screen_option> ::= PROMPT
//		final int PROD_SCREEN_OPTION_TOK_INITIAL                                             =  851;  // <screen_option> ::= 'TOK_INITIAL'
//		final int PROD_SCREEN_OPTION_LINE                                                    =  852;  // <screen_option> ::= LINE <screen_line_number>
//		final int PROD_SCREEN_OPTION3                                                        =  853;  // <screen_option> ::= <column_or_col> <screen_col_number>
//		final int PROD_SCREEN_OPTION_FOREGROUND_COLOR                                        =  854;  // <screen_option> ::= 'FOREGROUND_COLOR' <_is> <num_id_or_lit>
//		final int PROD_SCREEN_OPTION_BACKGROUND_COLOR                                        =  855;  // <screen_option> ::= 'BACKGROUND_COLOR' <_is> <num_id_or_lit>
//		final int PROD_SCREEN_OPTION4                                                        =  856;  // <screen_option> ::= <usage_clause>
//		final int PROD_SCREEN_OPTION5                                                        =  857;  // <screen_option> ::= <blank_clause>
//		final int PROD_SCREEN_OPTION6                                                        =  858;  // <screen_option> ::= <screen_global_clause>
//		final int PROD_SCREEN_OPTION7                                                        =  859;  // <screen_option> ::= <justified_clause>
//		final int PROD_SCREEN_OPTION8                                                        =  860;  // <screen_option> ::= <sign_clause>
//		final int PROD_SCREEN_OPTION9                                                        =  861;  // <screen_option> ::= <value_clause>
//		final int PROD_SCREEN_OPTION10                                                       =  862;  // <screen_option> ::= <picture_clause>
//		final int PROD_SCREEN_OPTION11                                                       =  863;  // <screen_option> ::= <screen_occurs_clause>
//		final int PROD_SCREEN_OPTION_USING                                                   =  864;  // <screen_option> ::= USING <identifier>
//		final int PROD_SCREEN_OPTION_FROM                                                    =  865;  // <screen_option> ::= FROM <from_parameter>
//		final int PROD_SCREEN_OPTION_TO                                                      =  866;  // <screen_option> ::= TO <identifier>
//		final int PROD_EOL_EOL                                                               =  867;  // <eol> ::= EOL
//		final int PROD_EOL_LINE                                                              =  868;  // <eol> ::= <_end_of> LINE
//		final int PROD_EOS_EOS                                                               =  869;  // <eos> ::= EOS
//		final int PROD_EOS_SCREEN                                                            =  870;  // <eos> ::= <_end_of> SCREEN
//		final int PROD_PLUS_PLUS_PLUS                                                        =  871;  // <plus_plus> ::= PLUS
//		final int PROD_PLUS_PLUS_TOK_PLUS                                                    =  872;  // <plus_plus> ::= 'TOK_PLUS'
//		final int PROD_MINUS_MINUS_MINUS                                                     =  873;  // <minus_minus> ::= MINUS
//		final int PROD_MINUS_MINUS_TOK_MINUS                                                 =  874;  // <minus_minus> ::= 'TOK_MINUS'
//		final int PROD_SCREEN_LINE_NUMBER                                                    =  875;  // <screen_line_number> ::= <_number> <_is> <_screen_line_plus_minus> <num_id_or_lit>
//		final int PROD__SCREEN_LINE_PLUS_MINUS                                               =  876;  // <_screen_line_plus_minus> ::=
//		final int PROD__SCREEN_LINE_PLUS_MINUS2                                              =  877;  // <_screen_line_plus_minus> ::= <plus_plus>
//		final int PROD__SCREEN_LINE_PLUS_MINUS3                                              =  878;  // <_screen_line_plus_minus> ::= <minus_minus>
//		final int PROD_SCREEN_COL_NUMBER                                                     =  879;  // <screen_col_number> ::= <_number> <_is> <_screen_col_plus_minus> <num_id_or_lit>
//		final int PROD__SCREEN_COL_PLUS_MINUS                                                =  880;  // <_screen_col_plus_minus> ::=
//		final int PROD__SCREEN_COL_PLUS_MINUS2                                               =  881;  // <_screen_col_plus_minus> ::= <plus_plus>
//		final int PROD__SCREEN_COL_PLUS_MINUS3                                               =  882;  // <_screen_col_plus_minus> ::= <minus_minus>
//		final int PROD_SCREEN_OCCURS_CLAUSE_OCCURS                                           =  883;  // <screen_occurs_clause> ::= OCCURS <integer> <_times>
//		final int PROD_SCREEN_GLOBAL_CLAUSE_GLOBAL                                           =  884;  // <screen_global_clause> ::= <_is> GLOBAL
//		final int PROD__PROCEDURE_DIVISION                                                   =  885;  // <_procedure_division> ::=
//		final int PROD__PROCEDURE_DIVISION_PROCEDURE_DIVISION_TOK_DOT                        =  886;  // <_procedure_division> ::= PROCEDURE DIVISION <_mnemonic_conv> <_procedure_using_chaining> <_procedure_returning> 'TOK_DOT' <_procedure_declaratives> <_procedure_list>
//		final int PROD__PROCEDURE_DIVISION_TOK_DOT                                           =  887;  // <_procedure_division> ::= <statements> 'TOK_DOT' <_procedure_list>
//		final int PROD__PROCEDURE_USING_CHAINING                                             =  888;  // <_procedure_using_chaining> ::=
		final int PROD__PROCEDURE_USING_CHAINING_USING                                       =  889;  // <_procedure_using_chaining> ::= USING <procedure_param_list>
//		final int PROD__PROCEDURE_USING_CHAINING_CHAINING                                    =  890;  // <_procedure_using_chaining> ::= CHAINING <procedure_param_list>
//		final int PROD_PROCEDURE_PARAM_LIST                                                  =  891;  // <procedure_param_list> ::= <procedure_param>
//		final int PROD_PROCEDURE_PARAM_LIST2                                                 =  892;  // <procedure_param_list> ::= <procedure_param_list> <procedure_param>
		final int PROD_PROCEDURE_PARAM                                                       =  893;  // <procedure_param> ::= <_procedure_type> <_size_optional> <_procedure_optional> <WORD>
//		final int PROD_PROCEDURE_PARAM_COMMA_DELIM                                           =  894;  // <procedure_param> ::= 'COMMA_DELIM'
//		final int PROD__PROCEDURE_TYPE                                                       =  895;  // <_procedure_type> ::=
//		final int PROD__PROCEDURE_TYPE_REFERENCE                                             =  896;  // <_procedure_type> ::= <_by> REFERENCE
//		final int PROD__PROCEDURE_TYPE_VALUE                                                 =  897;  // <_procedure_type> ::= <_by> VALUE
//		final int PROD__SIZE_OPTIONAL                                                        =  898;  // <_size_optional> ::=
//		final int PROD__SIZE_OPTIONAL_SIZE_AUTO                                              =  899;  // <_size_optional> ::= SIZE <_is> AUTO
//		final int PROD__SIZE_OPTIONAL_SIZE_DEFAULT                                           =  900;  // <_size_optional> ::= SIZE <_is> DEFAULT
//		final int PROD__SIZE_OPTIONAL_UNSIGNED_SIZE_AUTO                                     =  901;  // <_size_optional> ::= UNSIGNED SIZE <_is> AUTO
//		final int PROD__SIZE_OPTIONAL_UNSIGNED                                               =  902;  // <_size_optional> ::= UNSIGNED <size_is_integer>
//		final int PROD__SIZE_OPTIONAL2                                                       =  903;  // <_size_optional> ::= <size_is_integer>
//		final int PROD_SIZE_IS_INTEGER_SIZE                                                  =  904;  // <size_is_integer> ::= SIZE <_is> <integer>
//		final int PROD__PROCEDURE_OPTIONAL                                                   =  905;  // <_procedure_optional> ::=
//		final int PROD__PROCEDURE_OPTIONAL_OPTIONAL                                          =  906;  // <_procedure_optional> ::= OPTIONAL
//		final int PROD__PROCEDURE_RETURNING                                                  =  907;  // <_procedure_returning> ::=
//		final int PROD__PROCEDURE_RETURNING_RETURNING_OMITTED                                =  908;  // <_procedure_returning> ::= RETURNING OMITTED
		final int PROD__PROCEDURE_RETURNING_RETURNING                                        =  909;  // <_procedure_returning> ::= RETURNING <WORD>
//		final int PROD__PROCEDURE_DECLARATIVES                                               =  910;  // <_procedure_declaratives> ::=
//		final int PROD__PROCEDURE_DECLARATIVES_DECLARATIVES_TOK_DOT_END_DECLARATIVES_TOK_DOT =  911;  // <_procedure_declaratives> ::= DECLARATIVES 'TOK_DOT' <_procedure_list> END DECLARATIVES 'TOK_DOT'
//		final int PROD__PROCEDURE_LIST                                                       =  912;  // <_procedure_list> ::=
//		final int PROD__PROCEDURE_LIST2                                                      =  913;  // <_procedure_list> ::= <_procedure_list> <procedure>
//		final int PROD_PROCEDURE                                                             =  914;  // <procedure> ::= <section_header>
//		final int PROD_PROCEDURE2                                                            =  915;  // <procedure> ::= <paragraph_header>
		final int PROD_PROCEDURE_TOK_DOT                                                     =  916;  // <procedure> ::= <statements> 'TOK_DOT'
		final int PROD_PROCEDURE_TOK_DOT2                                                    =  917;  // <procedure> ::= 'TOK_DOT'
		final int PROD_SECTION_HEADER_SECTION_TOK_DOT                                        =  918;  // <section_header> ::= <WORD> SECTION <_segment> 'TOK_DOT' <_use_statement>
//		final int PROD__USE_STATEMENT                                                        =  919;  // <_use_statement> ::=
//		final int PROD__USE_STATEMENT_TOK_DOT                                                =  920;  // <_use_statement> ::= <use_statement> 'TOK_DOT'
		final int PROD_PARAGRAPH_HEADER_TOK_DOT                                              =  921;  // <paragraph_header> ::= <IntLiteral or WORD> 'TOK_DOT'
//		final int PROD_INTLITERALORWORD_INTLITERAL                                           =  922;  // <IntLiteral or WORD> ::= IntLiteral
//		final int PROD_INTLITERALORWORD                                                      =  923;  // <IntLiteral or WORD> ::= <WORD>
//		final int PROD__SEGMENT                                                              =  924;  // <_segment> ::=
//		final int PROD__SEGMENT2                                                             =  925;  // <_segment> ::= <integer>
//		final int PROD_STATEMENT_LIST                                                        =  926;  // <statement_list> ::= <statements>
//		final int PROD_STATEMENTS                                                            =  927;  // <statements> ::= <statement>
//		final int PROD_STATEMENTS2                                                           =  928;  // <statements> ::= <statements> <statement>
//		final int PROD_STATEMENT                                                             =  929;  // <statement> ::= <accept_statement>
//		final int PROD_STATEMENT2                                                            =  930;  // <statement> ::= <add_statement>
//		final int PROD_STATEMENT3                                                            =  931;  // <statement> ::= <allocate_statement>
//		final int PROD_STATEMENT4                                                            =  932;  // <statement> ::= <alter_statement>
//		final int PROD_STATEMENT5                                                            =  933;  // <statement> ::= <call_statement>
//		final int PROD_STATEMENT6                                                            =  934;  // <statement> ::= <cancel_statement>
//		final int PROD_STATEMENT7                                                            =  935;  // <statement> ::= <close_statement>
//		final int PROD_STATEMENT8                                                            =  936;  // <statement> ::= <commit_statement>
//		final int PROD_STATEMENT9                                                            =  937;  // <statement> ::= <compute_statement>
//		final int PROD_STATEMENT10                                                           =  938;  // <statement> ::= <continue_statement>
//		final int PROD_STATEMENT11                                                           =  939;  // <statement> ::= <delete_statement>
//		final int PROD_STATEMENT12                                                           =  940;  // <statement> ::= <disable_statement>
//		final int PROD_STATEMENT13                                                           =  941;  // <statement> ::= <display_statement>
//		final int PROD_STATEMENT14                                                           =  942;  // <statement> ::= <divide_statement>
//		final int PROD_STATEMENT15                                                           =  943;  // <statement> ::= <enable_statement>
//		final int PROD_STATEMENT16                                                           =  944;  // <statement> ::= <entry_statement>
//		final int PROD_STATEMENT17                                                           =  945;  // <statement> ::= <evaluate_statement>
//		final int PROD_STATEMENT18                                                           =  946;  // <statement> ::= <exit_statement>
//		final int PROD_STATEMENT19                                                           =  947;  // <statement> ::= <free_statement>
//		final int PROD_STATEMENT20                                                           =  948;  // <statement> ::= <generate_statement>
//		final int PROD_STATEMENT21                                                           =  949;  // <statement> ::= <goto_statement>
//		final int PROD_STATEMENT22                                                           =  950;  // <statement> ::= <goback_statement>
//		final int PROD_STATEMENT23                                                           =  951;  // <statement> ::= <if_statement>
//		final int PROD_STATEMENT24                                                           =  952;  // <statement> ::= <initialize_statement>
//		final int PROD_STATEMENT25                                                           =  953;  // <statement> ::= <initiate_statement>
//		final int PROD_STATEMENT26                                                           =  954;  // <statement> ::= <inspect_statement>
//		final int PROD_STATEMENT27                                                           =  955;  // <statement> ::= <merge_statement>
//		final int PROD_STATEMENT28                                                           =  956;  // <statement> ::= <move_statement>
//		final int PROD_STATEMENT29                                                           =  957;  // <statement> ::= <multiply_statement>
//		final int PROD_STATEMENT30                                                           =  958;  // <statement> ::= <open_statement>
//		final int PROD_STATEMENT31                                                           =  959;  // <statement> ::= <perform_statement>
//		final int PROD_STATEMENT32                                                           =  960;  // <statement> ::= <purge_statement>
//		final int PROD_STATEMENT33                                                           =  961;  // <statement> ::= <read_statement>
//		final int PROD_STATEMENT34                                                           =  962;  // <statement> ::= <ready_statement>
//		final int PROD_STATEMENT35                                                           =  963;  // <statement> ::= <receive_statement>
//		final int PROD_STATEMENT36                                                           =  964;  // <statement> ::= <release_statement>
//		final int PROD_STATEMENT37                                                           =  965;  // <statement> ::= <reset_statement>
//		final int PROD_STATEMENT38                                                           =  966;  // <statement> ::= <return_statement>
//		final int PROD_STATEMENT39                                                           =  967;  // <statement> ::= <rewrite_statement>
//		final int PROD_STATEMENT40                                                           =  968;  // <statement> ::= <rollback_statement>
//		final int PROD_STATEMENT41                                                           =  969;  // <statement> ::= <search_statement>
//		final int PROD_STATEMENT42                                                           =  970;  // <statement> ::= <send_statement>
//		final int PROD_STATEMENT43                                                           =  971;  // <statement> ::= <set_statement>
//		final int PROD_STATEMENT44                                                           =  972;  // <statement> ::= <sort_statement>
//		final int PROD_STATEMENT45                                                           =  973;  // <statement> ::= <start_statement>
//		final int PROD_STATEMENT46                                                           =  974;  // <statement> ::= <stop_statement>
//		final int PROD_STATEMENT47                                                           =  975;  // <statement> ::= <string_statement>
//		final int PROD_STATEMENT48                                                           =  976;  // <statement> ::= <subtract_statement>
//		final int PROD_STATEMENT49                                                           =  977;  // <statement> ::= <suppress_statement>
//		final int PROD_STATEMENT50                                                           =  978;  // <statement> ::= <terminate_statement>
//		final int PROD_STATEMENT51                                                           =  979;  // <statement> ::= <transform_statement>
//		final int PROD_STATEMENT52                                                           =  980;  // <statement> ::= <unlock_statement>
//		final int PROD_STATEMENT53                                                           =  981;  // <statement> ::= <unstring_statement>
//		final int PROD_STATEMENT54                                                           =  982;  // <statement> ::= <write_statement>
//		final int PROD_STATEMENT_NEXT_SENTENCE                                               =  983;  // <statement> ::= NEXT SENTENCE
		final int PROD_ACCEPT_STATEMENT_ACCEPT                                               =  984;  // <accept_statement> ::= ACCEPT <accept_body> <end_accept>
		final int PROD_ACCEPT_BODY                                                           =  985;  // <accept_body> ::= <accp_identifier> <_accept_clauses> <_accept_exception_phrases>
//		final int PROD_ACCEPT_BODY_FROM                                                      =  986;  // <accept_body> ::= <identifier> FROM <lines_or_number>
//		final int PROD_ACCEPT_BODY_FROM2                                                     =  987;  // <accept_body> ::= <identifier> FROM <columns_or_cols>
		final int PROD_ACCEPT_BODY_FROM_DATE_YYYYMMDD                                        =  988;  // <accept_body> ::= <identifier> FROM DATE YYYYMMDD
		final int PROD_ACCEPT_BODY_FROM_DATE                                                 =  989;  // <accept_body> ::= <identifier> FROM DATE
		final int PROD_ACCEPT_BODY_FROM_DAY_YYYYDDD                                          =  990;  // <accept_body> ::= <identifier> FROM DAY YYYYDDD
		final int PROD_ACCEPT_BODY_FROM_DAY                                                  =  991;  // <accept_body> ::= <identifier> FROM DAY
//		final int PROD_ACCEPT_BODY_FROM_DAY_OF_WEEK                                          =  992;  // <accept_body> ::= <identifier> FROM 'DAY_OF_WEEK'
//		final int PROD_ACCEPT_BODY_FROM_ESCAPE_KEY                                           =  993;  // <accept_body> ::= <identifier> FROM ESCAPE KEY
//		final int PROD_ACCEPT_BODY_FROM_EXCEPTION_STATUS                                     =  994;  // <accept_body> ::= <identifier> FROM EXCEPTION STATUS
		final int PROD_ACCEPT_BODY_FROM_TIME                                                 =  995;  // <accept_body> ::= <identifier> FROM TIME
//		final int PROD_ACCEPT_BODY_FROM_USER_NAME                                            =  996;  // <accept_body> ::= <identifier> FROM USER NAME
//		final int PROD_ACCEPT_BODY_FROM_COMMAND_LINE                                         =  997;  // <accept_body> ::= <identifier> FROM 'COMMAND_LINE'
//		final int PROD_ACCEPT_BODY_FROM_ENVIRONMENT_VALUE                                    =  998;  // <accept_body> ::= <identifier> FROM 'ENVIRONMENT_VALUE' <_accept_exception_phrases>
		final int PROD_ACCEPT_BODY_FROM_ENVIRONMENT                                          =  999;  // <accept_body> ::= <identifier> FROM ENVIRONMENT <simple_display_value> <_accept_exception_phrases>
//		final int PROD_ACCEPT_BODY_FROM_ARGUMENT_NUMBER                                      = 1000;  // <accept_body> ::= <identifier> FROM 'ARGUMENT_NUMBER'
//		final int PROD_ACCEPT_BODY_FROM_ARGUMENT_VALUE                                       = 1001;  // <accept_body> ::= <identifier> FROM 'ARGUMENT_VALUE' <_accept_exception_phrases>
		final int PROD_ACCEPT_BODY_FROM3                                                     = 1002;  // <accept_body> ::= <identifier> FROM <mnemonic_name>
//		final int PROD_ACCEPT_BODY_FROM4                                                     = 1003;  // <accept_body> ::= <identifier> FROM <WORD>
//		final int PROD_ACCEPT_BODY_COUNT                                                     = 1004;  // <accept_body> ::= <cd_name> <_message> COUNT
//		final int PROD_ACCP_IDENTIFIER                                                       = 1005;  // <accp_identifier> ::= <identifier>
//		final int PROD_ACCP_IDENTIFIER_OMITTED                                               = 1006;  // <accp_identifier> ::= OMITTED
//		final int PROD__ACCEPT_CLAUSES                                                       = 1007;  // <_accept_clauses> ::=
//		final int PROD__ACCEPT_CLAUSES2                                                      = 1008;  // <_accept_clauses> ::= <accept_clauses>
//		final int PROD_ACCEPT_CLAUSES                                                        = 1009;  // <accept_clauses> ::= <accept_clause>
//		final int PROD_ACCEPT_CLAUSES2                                                       = 1010;  // <accept_clauses> ::= <accept_clauses> <accept_clause>
//		final int PROD_ACCEPT_CLAUSE                                                         = 1011;  // <accept_clause> ::= <at_line_column>
//		final int PROD_ACCEPT_CLAUSE_FROM_CRT                                                = 1012;  // <accept_clause> ::= 'FROM_CRT'
//		final int PROD_ACCEPT_CLAUSE2                                                        = 1013;  // <accept_clause> ::= <mode_is_block>
//		final int PROD_ACCEPT_CLAUSE3                                                        = 1014;  // <accept_clause> ::= <_with> <accp_attr>
//		final int PROD_ACCEPT_CLAUSE_TIME                                                    = 1015;  // <accept_clause> ::= <_before> TIME <positive_id_or_lit>
//		final int PROD_LINES_OR_NUMBER_LINES                                                 = 1016;  // <lines_or_number> ::= LINES
//		final int PROD_LINES_OR_NUMBER_LINE_NUMBER                                           = 1017;  // <lines_or_number> ::= LINE NUMBER
//		final int PROD_AT_LINE_COLUMN                                                        = 1018;  // <at_line_column> ::= <_at> <line_number>
//		final int PROD_AT_LINE_COLUMN2                                                       = 1019;  // <at_line_column> ::= <_at> <column_number>
//		final int PROD_AT_LINE_COLUMN_AT                                                     = 1020;  // <at_line_column> ::= AT <num_id_or_lit>
//		final int PROD_LINE_NUMBER_LINE                                                      = 1021;  // <line_number> ::= LINE <_number> <num_id_or_lit>
//		final int PROD_COLUMN_NUMBER                                                         = 1022;  // <column_number> ::= <column_or_col> <_number> <num_id_or_lit>
//		final int PROD_COLUMN_NUMBER_POSITION                                                = 1023;  // <column_number> ::= POSITION <_number> <num_id_or_lit>
//		final int PROD_MODE_IS_BLOCK_MODE_BLOCK                                              = 1024;  // <mode_is_block> ::= MODE <_is> BLOCK
//		final int PROD_ACCP_ATTR_AUTO                                                        = 1025;  // <accp_attr> ::= AUTO
//		final int PROD_ACCP_ATTR_TAB                                                         = 1026;  // <accp_attr> ::= TAB
//		final int PROD_ACCP_ATTR_BELL                                                        = 1027;  // <accp_attr> ::= BELL
//		final int PROD_ACCP_ATTR_BLINK                                                       = 1028;  // <accp_attr> ::= BLINK
//		final int PROD_ACCP_ATTR_CONVERSION                                                  = 1029;  // <accp_attr> ::= CONVERSION
//		final int PROD_ACCP_ATTR_FULL                                                        = 1030;  // <accp_attr> ::= FULL
//		final int PROD_ACCP_ATTR_HIGHLIGHT                                                   = 1031;  // <accp_attr> ::= HIGHLIGHT
//		final int PROD_ACCP_ATTR_LEFTLINE                                                    = 1032;  // <accp_attr> ::= LEFTLINE
//		final int PROD_ACCP_ATTR_LOWER                                                       = 1033;  // <accp_attr> ::= LOWER
//		final int PROD_ACCP_ATTR_LOWLIGHT                                                    = 1034;  // <accp_attr> ::= LOWLIGHT
//		final int PROD_ACCP_ATTR                                                             = 1035;  // <accp_attr> ::= <no_echo>
//		final int PROD_ACCP_ATTR_OVERLINE                                                    = 1036;  // <accp_attr> ::= OVERLINE
//		final int PROD_ACCP_ATTR_PROMPT_CHARACTER                                            = 1037;  // <accp_attr> ::= PROMPT CHARACTER <_is> <id_or_lit>
//		final int PROD_ACCP_ATTR_PROMPT                                                      = 1038;  // <accp_attr> ::= PROMPT
//		final int PROD_ACCP_ATTR_REQUIRED                                                    = 1039;  // <accp_attr> ::= REQUIRED
//		final int PROD_ACCP_ATTR2                                                            = 1040;  // <accp_attr> ::= <reverse_video>
//		final int PROD_ACCP_ATTR_SECURE                                                      = 1041;  // <accp_attr> ::= SECURE
//		final int PROD_ACCP_ATTR_PROTECTED_SIZE                                              = 1042;  // <accp_attr> ::= PROTECTED SIZE <_is> <num_id_or_lit>
//		final int PROD_ACCP_ATTR_SIZE                                                        = 1043;  // <accp_attr> ::= SIZE <_is> <num_id_or_lit>
//		final int PROD_ACCP_ATTR_UNDERLINE                                                   = 1044;  // <accp_attr> ::= UNDERLINE
//		final int PROD_ACCP_ATTR_NO                                                          = 1045;  // <accp_attr> ::= NO <update_default>
//		final int PROD_ACCP_ATTR3                                                            = 1046;  // <accp_attr> ::= <update_default>
//		final int PROD_ACCP_ATTR_UPPER                                                       = 1047;  // <accp_attr> ::= UPPER
//		final int PROD_ACCP_ATTR_FOREGROUND_COLOR                                            = 1048;  // <accp_attr> ::= 'FOREGROUND_COLOR' <_is> <num_id_or_lit>
//		final int PROD_ACCP_ATTR_BACKGROUND_COLOR                                            = 1049;  // <accp_attr> ::= 'BACKGROUND_COLOR' <_is> <num_id_or_lit>
//		final int PROD_ACCP_ATTR_SCROLL_UP                                                   = 1050;  // <accp_attr> ::= SCROLL UP <_scroll_lines>
//		final int PROD_ACCP_ATTR_SCROLL_DOWN                                                 = 1051;  // <accp_attr> ::= SCROLL DOWN <_scroll_lines>
//		final int PROD_ACCP_ATTR_TIME_OUT                                                    = 1052;  // <accp_attr> ::= 'TIME_OUT' <_after> <positive_id_or_lit>
//		final int PROD_NO_ECHO_NO_ECHO                                                       = 1053;  // <no_echo> ::= NO ECHO
//		final int PROD_NO_ECHO_NO_ECHO2                                                      = 1054;  // <no_echo> ::= 'NO_ECHO'
//		final int PROD_NO_ECHO_OFF                                                           = 1055;  // <no_echo> ::= OFF
//		final int PROD_REVERSE_VIDEO_REVERSE_VIDEO                                           = 1056;  // <reverse_video> ::= 'REVERSE_VIDEO'
//		final int PROD_REVERSE_VIDEO_REVERSED                                                = 1057;  // <reverse_video> ::= REVERSED
//		final int PROD_REVERSE_VIDEO_REVERSE                                                 = 1058;  // <reverse_video> ::= REVERSE
//		final int PROD_UPDATE_DEFAULT_UPDATE                                                 = 1059;  // <update_default> ::= UPDATE
//		final int PROD_UPDATE_DEFAULT_DEFAULT                                                = 1060;  // <update_default> ::= DEFAULT
//		final int PROD_END_ACCEPT                                                            = 1061;  // <end_accept> ::=
//		final int PROD_END_ACCEPT_END_ACCEPT                                                 = 1062;  // <end_accept> ::= 'END_ACCEPT'
		final int PROD_ADD_STATEMENT_ADD                                                     = 1063;  // <add_statement> ::= ADD <add_body> <end_add>
		final int PROD_ADD_BODY_TO                                                           = 1064;  // <add_body> ::= <x_list> TO <arithmetic_x_list> <on_size_error_phrases>
		final int PROD_ADD_BODY_GIVING                                                       = 1065;  // <add_body> ::= <x_list> <_add_to> GIVING <arithmetic_x_list> <on_size_error_phrases>
//		final int PROD_ADD_BODY_CORRESPONDING_TO                                             = 1066;  // <add_body> ::= CORRESPONDING <identifier> TO <identifier> <flag_rounded> <on_size_error_phrases>
//		final int PROD_ADD_BODY_TABLE_TO                                                     = 1067;  // <add_body> ::= TABLE <table_identifier> TO <table_identifier> <flag_rounded> <_from_idx_to_idx> <_dest_index> <on_size_error_phrases>
//		final int PROD__ADD_TO                                                               = 1068;  // <_add_to> ::=
//		final int PROD__ADD_TO_TO                                                            = 1069;  // <_add_to> ::= TO <x>
//		final int PROD_END_ADD                                                               = 1070;  // <end_add> ::=
//		final int PROD_END_ADD_END_ADD                                                       = 1071;  // <end_add> ::= 'END_ADD'
//		final int PROD_ALLOCATE_STATEMENT_ALLOCATE                                           = 1072;  // <allocate_statement> ::= ALLOCATE <allocate_body>
//		final int PROD_ALLOCATE_BODY                                                         = 1073;  // <allocate_body> ::= <identifier> <flag_initialized> <allocate_returning>
//		final int PROD_ALLOCATE_BODY_CHARACTERS                                              = 1074;  // <allocate_body> ::= <exp> CHARACTERS <flag_initialized_to> <allocate_returning>
//		final int PROD_ALLOCATE_RETURNING                                                    = 1075;  // <allocate_returning> ::=
//		final int PROD_ALLOCATE_RETURNING_RETURNING                                          = 1076;  // <allocate_returning> ::= RETURNING <target_x>
//		final int PROD_ALTER_STATEMENT_ALTER                                                 = 1077;  // <alter_statement> ::= ALTER <alter_body>
//		final int PROD_ALTER_BODY                                                            = 1078;  // <alter_body> ::= <alter_entry>
//		final int PROD_ALTER_BODY2                                                           = 1079;  // <alter_body> ::= <alter_body> <alter_entry>
//		final int PROD_ALTER_ENTRY_TO                                                        = 1080;  // <alter_entry> ::= <procedure_name> TO <_proceed_to> <procedure_name>
//		final int PROD__PROCEED_TO                                                           = 1081;  // <_proceed_to> ::=
//		final int PROD__PROCEED_TO_PROCEED_TO                                                = 1082;  // <_proceed_to> ::= PROCEED TO
		final int PROD_CALL_STATEMENT_CALL                                                   = 1083;  // <call_statement> ::= CALL <call_body> <end_call>
//		final int PROD_CALL_BODY                                                             = 1084;  // <call_body> ::= <_mnemonic_conv> <program_or_prototype> <call_using> <call_returning> <call_exception_phrases>
//		final int PROD__MNEMONIC_CONV                                                        = 1085;  // <_mnemonic_conv> ::=
//		final int PROD__MNEMONIC_CONV_STATIC                                                 = 1086;  // <_mnemonic_conv> ::= STATIC
//		final int PROD__MNEMONIC_CONV_STDCALL                                                = 1087;  // <_mnemonic_conv> ::= STDCALL
//		final int PROD__MNEMONIC_CONV_TOK_EXTERN                                             = 1088;  // <_mnemonic_conv> ::= 'TOK_EXTERN'
//		final int PROD__MNEMONIC_CONV2                                                       = 1089;  // <_mnemonic_conv> ::= <MNEMONIC_NAME_TOK>
//		final int PROD_PROGRAM_OR_PROTOTYPE                                                  = 1090;  // <program_or_prototype> ::= <id_or_lit_or_func>
//		final int PROD_PROGRAM_OR_PROTOTYPE2                                                 = 1091;  // <program_or_prototype> ::= <id_or_lit_or_func_as> <PROGRAM_NAME>
//		final int PROD_PROGRAM_OR_PROTOTYPE_AS_NESTED                                        = 1092;  // <program_or_prototype> ::= <LITERAL_TOK> AS NESTED
//		final int PROD_ID_OR_LIT_OR_FUNC_AS_AS                                               = 1093;  // <id_or_lit_or_func_as> ::= <id_or_lit_or_func> AS
//		final int PROD_CALL_USING                                                            = 1094;  // <call_using> ::=
//		final int PROD_CALL_USING_USING                                                      = 1095;  // <call_using> ::= USING <call_param_list>
//		final int PROD_CALL_PARAM_LIST                                                       = 1096;  // <call_param_list> ::= <call_param>
//		final int PROD_CALL_PARAM_LIST2                                                      = 1097;  // <call_param_list> ::= <call_param_list> <call_param>
		final int PROD_CALL_PARAM_OMITTED                                                    = 1098;  // <call_param> ::= <call_type> OMITTED
		final int PROD_CALL_PARAM                                                            = 1099;  // <call_param> ::= <call_type> <_size_optional> <call_x>
//		final int PROD_CALL_PARAM_COMMA_DELIM                                                = 1100;  // <call_param> ::= 'COMMA_DELIM'
//		final int PROD_CALL_TYPE                                                             = 1101;  // <call_type> ::=
//		final int PROD_CALL_TYPE_REFERENCE                                                   = 1102;  // <call_type> ::= <_by> REFERENCE
//		final int PROD_CALL_TYPE_CONTENT                                                     = 1103;  // <call_type> ::= <_by> CONTENT
//		final int PROD_CALL_TYPE_VALUE                                                       = 1104;  // <call_type> ::= <_by> VALUE
//		final int PROD_CALL_RETURNING                                                        = 1105;  // <call_returning> ::=
		final int PROD_CALL_RETURNING2                                                       = 1106;  // <call_returning> ::= <return_give> <_into> <identifier>
//		final int PROD_CALL_RETURNING3                                                       = 1107;  // <call_returning> ::= <return_give> <null_or_omitted>
//		final int PROD_CALL_RETURNING_NOTHING                                                = 1108;  // <call_returning> ::= <return_give> NOTHING
//		final int PROD_CALL_RETURNING_ADDRESS                                                = 1109;  // <call_returning> ::= <return_give> ADDRESS <_of> <identifier>
//		final int PROD_RETURN_GIVE_RETURNING                                                 = 1110;  // <return_give> ::= RETURNING
//		final int PROD_RETURN_GIVE_GIVING                                                    = 1111;  // <return_give> ::= GIVING
//		final int PROD_NULL_OR_OMITTED_TOK_NULL                                              = 1112;  // <null_or_omitted> ::= 'TOK_NULL'
//		final int PROD_NULL_OR_OMITTED_OMITTED                                               = 1113;  // <null_or_omitted> ::= OMITTED
//		final int PROD_CALL_EXCEPTION_PHRASES                                                = 1114;  // <call_exception_phrases> ::=
//		final int PROD_CALL_EXCEPTION_PHRASES2                                               = 1115;  // <call_exception_phrases> ::= <call_on_exception> <_call_not_on_exception>
//		final int PROD_CALL_EXCEPTION_PHRASES3                                               = 1116;  // <call_exception_phrases> ::= <call_not_on_exception> <_call_on_exception>
//		final int PROD__CALL_ON_EXCEPTION                                                    = 1117;  // <_call_on_exception> ::=
//		final int PROD__CALL_ON_EXCEPTION2                                                   = 1118;  // <_call_on_exception> ::= <call_on_exception>
//		final int PROD_CALL_ON_EXCEPTION_EXCEPTION                                           = 1119;  // <call_on_exception> ::= EXCEPTION <statement_list>
//		final int PROD_CALL_ON_EXCEPTION_TOK_OVERFLOW                                        = 1120;  // <call_on_exception> ::= 'TOK_OVERFLOW' <statement_list>
//		final int PROD__CALL_NOT_ON_EXCEPTION                                                = 1121;  // <_call_not_on_exception> ::=
//		final int PROD__CALL_NOT_ON_EXCEPTION2                                               = 1122;  // <_call_not_on_exception> ::= <call_not_on_exception>
//		final int PROD_CALL_NOT_ON_EXCEPTION_NOT_EXCEPTION                                   = 1123;  // <call_not_on_exception> ::= 'NOT_EXCEPTION' <statement_list>
//		final int PROD_END_CALL                                                              = 1124;  // <end_call> ::=
//		final int PROD_END_CALL_END_CALL                                                     = 1125;  // <end_call> ::= 'END_CALL'
//		final int PROD_CANCEL_STATEMENT_CANCEL                                               = 1126;  // <cancel_statement> ::= CANCEL <cancel_body>
//		final int PROD_CANCEL_BODY                                                           = 1127;  // <cancel_body> ::= <id_or_lit_or_program_name>
//		final int PROD_CANCEL_BODY2                                                          = 1128;  // <cancel_body> ::= <cancel_body> <id_or_lit_or_program_name>
//		final int PROD_ID_OR_LIT_OR_PROGRAM_NAME                                             = 1129;  // <id_or_lit_or_program_name> ::= <id_or_lit>
		final int PROD_CLOSE_STATEMENT_CLOSE                                                 = 1130;  // <close_statement> ::= CLOSE <close_body>
//		final int PROD_CLOSE_BODY                                                            = 1131;  // <close_body> ::= <file_name> <close_option>
//		final int PROD_CLOSE_BODY2                                                           = 1132;  // <close_body> ::= <close_body> <file_name> <close_option>
//		final int PROD_CLOSE_OPTION                                                          = 1133;  // <close_option> ::=
//		final int PROD_CLOSE_OPTION2                                                         = 1134;  // <close_option> ::= <reel_or_unit>
//		final int PROD_CLOSE_OPTION_REMOVAL                                                  = 1135;  // <close_option> ::= <reel_or_unit> <_for> REMOVAL
//		final int PROD_CLOSE_OPTION_NO_REWIND                                                = 1136;  // <close_option> ::= <_with> NO REWIND
//		final int PROD_CLOSE_OPTION_LOCK                                                     = 1137;  // <close_option> ::= <_with> LOCK
		final int PROD_COMPUTE_STATEMENT_COMPUTE                                             = 1138;  // <compute_statement> ::= COMPUTE <compute_body> <end_compute>
//		final int PROD_COMPUTE_BODY                                                          = 1139;  // <compute_body> ::= <arithmetic_x_list> <comp_equal> <exp> <on_size_error_phrases>
//		final int PROD_END_COMPUTE                                                           = 1140;  // <end_compute> ::=
//		final int PROD_END_COMPUTE_END_COMPUTE                                               = 1141;  // <end_compute> ::= 'END_COMPUTE'
//		final int PROD_COMMIT_STATEMENT_COMMIT                                               = 1142;  // <commit_statement> ::= COMMIT
//		final int PROD_CONTINUE_STATEMENT_CONTINUE                                           = 1143;  // <continue_statement> ::= CONTINUE
		final int PROD_DELETE_STATEMENT_DELETE                                               = 1144;  // <delete_statement> ::= DELETE <delete_body> <end_delete>
//		final int PROD_DELETE_BODY                                                           = 1145;  // <delete_body> ::= <file_name> <_record> <_retry_phrase> <_invalid_key_phrases>
//		final int PROD_DELETE_BODY_TOK_FILE                                                  = 1146;  // <delete_body> ::= 'TOK_FILE' <delete_file_list>
//		final int PROD_DELETE_FILE_LIST                                                      = 1147;  // <delete_file_list> ::= <file_name>
//		final int PROD_DELETE_FILE_LIST2                                                     = 1148;  // <delete_file_list> ::= <delete_file_list> <file_name>
//		final int PROD_END_DELETE                                                            = 1149;  // <end_delete> ::=
//		final int PROD_END_DELETE_END_DELETE                                                 = 1150;  // <end_delete> ::= 'END_DELETE'
//		final int PROD_DISABLE_STATEMENT_DISABLE                                             = 1151;  // <disable_statement> ::= DISABLE <enable_disable_handling>
//		final int PROD_ENABLE_DISABLE_HANDLING                                               = 1152;  // <enable_disable_handling> ::= <communication_mode> <cd_name> <_enable_disable_key>
//		final int PROD__ENABLE_DISABLE_KEY                                                   = 1153;  // <_enable_disable_key> ::=
//		final int PROD__ENABLE_DISABLE_KEY_KEY                                               = 1154;  // <_enable_disable_key> ::= <_with> KEY <id_or_lit>
//		final int PROD_COMMUNICATION_MODE                                                    = 1155;  // <communication_mode> ::=
//		final int PROD_COMMUNICATION_MODE_INPUT                                              = 1156;  // <communication_mode> ::= INPUT <_terminal>
//		final int PROD_COMMUNICATION_MODE_OUTPUT                                             = 1157;  // <communication_mode> ::= OUTPUT
//		final int PROD_COMMUNICATION_MODE_I_O_TERMINAL                                       = 1158;  // <communication_mode> ::= 'I_O' TERMINAL
//		final int PROD_COMMUNICATION_MODE_TERMINAL                                           = 1159;  // <communication_mode> ::= TERMINAL
		final int PROD_DISPLAY_STATEMENT_DISPLAY                                             = 1160;  // <display_statement> ::= DISPLAY <display_body> <end_display>
//		final int PROD_DISPLAY_BODY_UPON_ENVIRONMENT_NAME                                    = 1161;  // <display_body> ::= <id_or_lit> 'UPON_ENVIRONMENT_NAME' <_display_exception_phrases>
//		final int PROD_DISPLAY_BODY_UPON_ENVIRONMENT_VALUE                                   = 1162;  // <display_body> ::= <id_or_lit> 'UPON_ENVIRONMENT_VALUE' <_display_exception_phrases>
//		final int PROD_DISPLAY_BODY_UPON_ARGUMENT_NUMBER                                     = 1163;  // <display_body> ::= <id_or_lit> 'UPON_ARGUMENT_NUMBER' <_display_exception_phrases>
//		final int PROD_DISPLAY_BODY_UPON_COMMAND_LINE                                        = 1164;  // <display_body> ::= <id_or_lit> 'UPON_COMMAND_LINE' <_display_exception_phrases>
//		final int PROD_DISPLAY_BODY                                                          = 1165;  // <display_body> ::= <screen_or_device_display> <_display_exception_phrases>
//		final int PROD_SCREEN_OR_DEVICE_DISPLAY                                              = 1166;  // <screen_or_device_display> ::= <display_list> <_x_list>
//		final int PROD_SCREEN_OR_DEVICE_DISPLAY2                                             = 1167;  // <screen_or_device_display> ::= <x_list>
//		final int PROD_DISPLAY_LIST                                                          = 1168;  // <display_list> ::= <display_atom>
//		final int PROD_DISPLAY_LIST2                                                         = 1169;  // <display_list> ::= <display_list> <display_atom>
//		final int PROD_DISPLAY_ATOM                                                          = 1170;  // <display_atom> ::= <disp_list> <display_clauses>
//		final int PROD_DISP_LIST                                                             = 1171;  // <disp_list> ::= <x_list>
//		final int PROD_DISP_LIST_OMITTED                                                     = 1172;  // <disp_list> ::= OMITTED
//		final int PROD_DISPLAY_CLAUSES                                                       = 1173;  // <display_clauses> ::= <display_clause>
//		final int PROD_DISPLAY_CLAUSES2                                                      = 1174;  // <display_clauses> ::= <display_clauses> <display_clause>
//		final int PROD_DISPLAY_CLAUSE                                                        = 1175;  // <display_clause> ::= <display_upon>
//		final int PROD_DISPLAY_CLAUSE_NO_ADVANCING                                           = 1176;  // <display_clause> ::= <_with> 'NO_ADVANCING'
//		final int PROD_DISPLAY_CLAUSE2                                                       = 1177;  // <display_clause> ::= <mode_is_block>
//		final int PROD_DISPLAY_CLAUSE3                                                       = 1178;  // <display_clause> ::= <at_line_column>
//		final int PROD_DISPLAY_CLAUSE4                                                       = 1179;  // <display_clause> ::= <_with> <disp_attr>
//		final int PROD_DISPLAY_UPON_UPON                                                     = 1180;  // <display_upon> ::= UPON <mnemonic_name>
//		final int PROD_DISPLAY_UPON_UPON2                                                    = 1181;  // <display_upon> ::= UPON <WORD>
//		final int PROD_DISPLAY_UPON_UPON_PRINTER                                             = 1182;  // <display_upon> ::= UPON PRINTER
//		final int PROD_DISPLAY_UPON_UPON3                                                    = 1183;  // <display_upon> ::= UPON <crt_under>
//		final int PROD_CRT_UNDER_CRT                                                         = 1184;  // <crt_under> ::= CRT
//		final int PROD_CRT_UNDER_CRT_UNDER                                                   = 1185;  // <crt_under> ::= 'CRT_UNDER'
//		final int PROD_DISP_ATTR_BELL                                                        = 1186;  // <disp_attr> ::= BELL
//		final int PROD_DISP_ATTR_BLANK_LINE                                                  = 1187;  // <disp_attr> ::= BLANK LINE
//		final int PROD_DISP_ATTR_BLANK_SCREEN                                                = 1188;  // <disp_attr> ::= BLANK SCREEN
//		final int PROD_DISP_ATTR_BLINK                                                       = 1189;  // <disp_attr> ::= BLINK
//		final int PROD_DISP_ATTR_CONVERSION                                                  = 1190;  // <disp_attr> ::= CONVERSION
//		final int PROD_DISP_ATTR_ERASE                                                       = 1191;  // <disp_attr> ::= ERASE <eol>
//		final int PROD_DISP_ATTR_ERASE2                                                      = 1192;  // <disp_attr> ::= ERASE <eos>
//		final int PROD_DISP_ATTR_HIGHLIGHT                                                   = 1193;  // <disp_attr> ::= HIGHLIGHT
//		final int PROD_DISP_ATTR_LOWLIGHT                                                    = 1194;  // <disp_attr> ::= LOWLIGHT
//		final int PROD_DISP_ATTR_OVERLINE                                                    = 1195;  // <disp_attr> ::= OVERLINE
//		final int PROD_DISP_ATTR                                                             = 1196;  // <disp_attr> ::= <reverse_video>
//		final int PROD_DISP_ATTR_SIZE                                                        = 1197;  // <disp_attr> ::= SIZE <_is> <num_id_or_lit>
//		final int PROD_DISP_ATTR_UNDERLINE                                                   = 1198;  // <disp_attr> ::= UNDERLINE
//		final int PROD_DISP_ATTR_FOREGROUND_COLOR                                            = 1199;  // <disp_attr> ::= 'FOREGROUND_COLOR' <_is> <num_id_or_lit>
//		final int PROD_DISP_ATTR_BACKGROUND_COLOR                                            = 1200;  // <disp_attr> ::= 'BACKGROUND_COLOR' <_is> <num_id_or_lit>
//		final int PROD_DISP_ATTR_SCROLL_UP                                                   = 1201;  // <disp_attr> ::= SCROLL UP <_scroll_lines>
//		final int PROD_DISP_ATTR_SCROLL_DOWN                                                 = 1202;  // <disp_attr> ::= SCROLL DOWN <_scroll_lines>
//		final int PROD_END_DISPLAY                                                           = 1203;  // <end_display> ::=
//		final int PROD_END_DISPLAY_END_DISPLAY                                               = 1204;  // <end_display> ::= 'END_DISPLAY'
		final int PROD_DIVIDE_STATEMENT_DIVIDE                                               = 1205;  // <divide_statement> ::= DIVIDE <divide_body> <end_divide>
		final int PROD_DIVIDE_BODY_INTO                                                      = 1206;  // <divide_body> ::= <x> INTO <arithmetic_x_list> <on_size_error_phrases>
//		final int PROD_DIVIDE_BODY_INTO_GIVING                                               = 1207;  // <divide_body> ::= <x> INTO <x> GIVING <arithmetic_x_list> <on_size_error_phrases>
		final int PROD_DIVIDE_BODY_BY_GIVING                                                 = 1208;  // <divide_body> ::= <x> BY <x> GIVING <arithmetic_x_list> <on_size_error_phrases>
		final int PROD_DIVIDE_BODY_INTO_GIVING_REMAINDER                                     = 1209;  // <divide_body> ::= <x> INTO <x> GIVING <arithmetic_x> REMAINDER <arithmetic_x> <on_size_error_phrases>
		final int PROD_DIVIDE_BODY_BY_GIVING_REMAINDER                                       = 1210;  // <divide_body> ::= <x> BY <x> GIVING <arithmetic_x> REMAINDER <arithmetic_x> <on_size_error_phrases>
//		final int PROD_END_DIVIDE                                                            = 1211;  // <end_divide> ::=
//		final int PROD_END_DIVIDE_END_DIVIDE                                                 = 1212;  // <end_divide> ::= 'END_DIVIDE'
//		final int PROD_ENABLE_STATEMENT_ENABLE                                               = 1213;  // <enable_statement> ::= ENABLE <enable_disable_handling>
//		final int PROD_ENTRY_STATEMENT_ENTRY                                                 = 1214;  // <entry_statement> ::= ENTRY <entry_body>
//		final int PROD_ENTRY_BODY                                                            = 1215;  // <entry_body> ::= <_mnemonic_conv> <LITERAL_TOK> <call_using>
		final int PROD_EVALUATE_STATEMENT_EVALUATE                                           = 1216;  // <evaluate_statement> ::= EVALUATE <evaluate_body> <end_evaluate>
//		final int PROD_EVALUATE_BODY                                                         = 1217;  // <evaluate_body> ::= <evaluate_subject_list> <evaluate_condition_list>
//		final int PROD_EVALUATE_SUBJECT_LIST                                                 = 1218;  // <evaluate_subject_list> ::= <evaluate_subject>
		final int PROD_EVALUATE_SUBJECT_LIST_ALSO                                            = 1219;  // <evaluate_subject_list> ::= <evaluate_subject_list> ALSO <evaluate_subject>
//		final int PROD_EVALUATE_SUBJECT                                                      = 1220;  // <evaluate_subject> ::= <expr>
		final int PROD_EVALUATE_SUBJECT_TOK_TRUE                                             = 1221;  // <evaluate_subject> ::= 'TOK_TRUE'
		final int PROD_EVALUATE_SUBJECT_TOK_FALSE                                            = 1222;  // <evaluate_subject> ::= 'TOK_FALSE'
		final int PROD_EVALUATE_CONDITION_LIST                                               = 1223;  // <evaluate_condition_list> ::= <evaluate_case_list> <evaluate_other>
//		final int PROD_EVALUATE_CONDITION_LIST2                                              = 1224;  // <evaluate_condition_list> ::= <evaluate_case_list>
//		final int PROD_EVALUATE_CASE_LIST                                                    = 1225;  // <evaluate_case_list> ::= <evaluate_case>
//		final int PROD_EVALUATE_CASE_LIST2                                                   = 1226;  // <evaluate_case_list> ::= <evaluate_case_list> <evaluate_case>
//		final int PROD_EVALUATE_CASE                                                         = 1227;  // <evaluate_case> ::= <evaluate_when_list> <statement_list>
//		final int PROD_EVALUATE_OTHER_WHEN_OTHER                                             = 1228;  // <evaluate_other> ::= WHEN OTHER <statement_list>
//		final int PROD_EVALUATE_WHEN_LIST_WHEN                                               = 1229;  // <evaluate_when_list> ::= WHEN <evaluate_object_list>
		final int PROD_EVALUATE_WHEN_LIST_WHEN2                                              = 1230;  // <evaluate_when_list> ::= <evaluate_when_list> WHEN <evaluate_object_list>
//		final int PROD_EVALUATE_OBJECT_LIST                                                  = 1231;  // <evaluate_object_list> ::= <evaluate_object>
		final int PROD_EVALUATE_OBJECT_LIST_ALSO                                             = 1232;  // <evaluate_object_list> ::= <evaluate_object_list> ALSO <evaluate_object>
		final int PROD_EVALUATE_OBJECT                                                       = 1233;  // <evaluate_object> ::= <partial_expr> <_evaluate_thru_expr>
		final int PROD_EVALUATE_OBJECT_ANY                                                   = 1234;  // <evaluate_object> ::= ANY
//		final int PROD_EVALUATE_OBJECT_TOK_TRUE                                              = 1235;  // <evaluate_object> ::= 'TOK_TRUE'
//		final int PROD_EVALUATE_OBJECT_TOK_FALSE                                             = 1236;  // <evaluate_object> ::= 'TOK_FALSE'
		final int PROD__EVALUATE_THRU_EXPR                                                   = 1237;  // <_evaluate_thru_expr> ::=
		final int PROD__EVALUATE_THRU_EXPR_THRU                                              = 1238;  // <_evaluate_thru_expr> ::= THRU <expr>
//		final int PROD_END_EVALUATE                                                          = 1239;  // <end_evaluate> ::=
//		final int PROD_END_EVALUATE_END_EVALUATE                                             = 1240;  // <end_evaluate> ::= 'END_EVALUATE'
		final int PROD_EXIT_STATEMENT_EXIT                                                   = 1241;  // <exit_statement> ::= EXIT <exit_body>
		final int PROD_EXIT_BODY                                                             = 1242;  // <exit_body> ::=
		final int PROD_EXIT_BODY_PROGRAM                                                     = 1243;  // <exit_body> ::= PROGRAM <exit_program_returning>
		final int PROD_EXIT_BODY_FUNCTION                                                    = 1244;  // <exit_body> ::= FUNCTION
		final int PROD_EXIT_BODY_PERFORM_CYCLE                                               = 1245;  // <exit_body> ::= PERFORM CYCLE
		final int PROD_EXIT_BODY_PERFORM                                                     = 1246;  // <exit_body> ::= PERFORM
		final int PROD_EXIT_BODY_SECTION                                                     = 1247;  // <exit_body> ::= SECTION
		final int PROD_EXIT_BODY_PARAGRAPH                                                   = 1248;  // <exit_body> ::= PARAGRAPH
//		final int PROD_EXIT_PROGRAM_RETURNING                                                = 1249;  // <exit_program_returning> ::=
		final int PROD_EXIT_PROGRAM_RETURNING2                                               = 1250;  // <exit_program_returning> ::= <return_give> <x>
//		final int PROD_FREE_STATEMENT_FREE                                                   = 1251;  // <free_statement> ::= FREE <free_body>
//		final int PROD_FREE_BODY                                                             = 1252;  // <free_body> ::= <target_x_list>
//		final int PROD_GENERATE_STATEMENT_GENERATE                                           = 1253;  // <generate_statement> ::= GENERATE <generate_body>
//		final int PROD_GENERATE_BODY                                                         = 1254;  // <generate_body> ::= <qualified_word>
		final int PROD_GOTO_STATEMENT_GO                                                     = 1255;  // <goto_statement> ::= GO <go_body>
//		final int PROD_GO_BODY                                                               = 1256;  // <go_body> ::= <_to> <procedure_name_list> <goto_depending>
//		final int PROD_GOTO_DEPENDING                                                        = 1257;  // <goto_depending> ::=
//		final int PROD_GOTO_DEPENDING_DEPENDING                                              = 1258;  // <goto_depending> ::= DEPENDING <_on> <identifier>
		final int PROD_GOBACK_STATEMENT_GOBACK                                               = 1259;  // <goback_statement> ::= GOBACK <exit_program_returning>
		final int PROD_IF_STATEMENT_IF                                                       = 1260;  // <if_statement> ::= IF <condition> <_then> <if_else_statements> <end_if>
		final int PROD_IF_ELSE_STATEMENTS_ELSE                                               = 1261;  // <if_else_statements> ::= <statement_list> ELSE <statement_list>
		final int PROD_IF_ELSE_STATEMENTS_ELSE2                                              = 1262;  // <if_else_statements> ::= ELSE <statement_list>
//		final int PROD_IF_ELSE_STATEMENTS                                                    = 1263;  // <if_else_statements> ::= <statement_list>
//		final int PROD_END_IF                                                                = 1264;  // <end_if> ::=
//		final int PROD_END_IF_END_IF                                                         = 1265;  // <end_if> ::= 'END_IF'
//		final int PROD_INITIALIZE_STATEMENT_INITIALIZE                                       = 1266;  // <initialize_statement> ::= INITIALIZE <initialize_body>
//		final int PROD_INITIALIZE_BODY                                                       = 1267;  // <initialize_body> ::= <target_x_list> <_initialize_filler> <_initialize_value> <_initialize_replacing> <_initialize_default>
//		final int PROD__INITIALIZE_FILLER                                                    = 1268;  // <_initialize_filler> ::=
//		final int PROD__INITIALIZE_FILLER_FILLER                                             = 1269;  // <_initialize_filler> ::= <_with> FILLER
//		final int PROD__INITIALIZE_VALUE                                                     = 1270;  // <_initialize_value> ::=
//		final int PROD__INITIALIZE_VALUE_ALL_VALUE                                           = 1271;  // <_initialize_value> ::= ALL <_to> VALUE
//		final int PROD__INITIALIZE_VALUE_VALUE                                               = 1272;  // <_initialize_value> ::= <initialize_category> <_to> VALUE
//		final int PROD__INITIALIZE_REPLACING                                                 = 1273;  // <_initialize_replacing> ::=
//		final int PROD__INITIALIZE_REPLACING_REPLACING                                       = 1274;  // <_initialize_replacing> ::= <_then> REPLACING <initialize_replacing_list>
//		final int PROD_INITIALIZE_REPLACING_LIST                                             = 1275;  // <initialize_replacing_list> ::= <initialize_replacing_item>
//		final int PROD_INITIALIZE_REPLACING_LIST2                                            = 1276;  // <initialize_replacing_list> ::= <initialize_replacing_list> <initialize_replacing_item>
//		final int PROD_INITIALIZE_REPLACING_ITEM_BY                                          = 1277;  // <initialize_replacing_item> ::= <initialize_category> <_data> BY <x>
//		final int PROD_INITIALIZE_CATEGORY_ALPHABETIC                                        = 1278;  // <initialize_category> ::= ALPHABETIC
//		final int PROD_INITIALIZE_CATEGORY_ALPHANUMERIC                                      = 1279;  // <initialize_category> ::= ALPHANUMERIC
//		final int PROD_INITIALIZE_CATEGORY_NUMERIC                                           = 1280;  // <initialize_category> ::= NUMERIC
//		final int PROD_INITIALIZE_CATEGORY_ALPHANUMERIC_EDITED                               = 1281;  // <initialize_category> ::= 'ALPHANUMERIC_EDITED'
//		final int PROD_INITIALIZE_CATEGORY_NUMERIC_EDITED                                    = 1282;  // <initialize_category> ::= 'NUMERIC_EDITED'
//		final int PROD_INITIALIZE_CATEGORY_NATIONAL                                          = 1283;  // <initialize_category> ::= NATIONAL
//		final int PROD_INITIALIZE_CATEGORY_NATIONAL_EDITED                                   = 1284;  // <initialize_category> ::= 'NATIONAL_EDITED'
//		final int PROD__INITIALIZE_DEFAULT                                                   = 1285;  // <_initialize_default> ::=
//		final int PROD__INITIALIZE_DEFAULT_DEFAULT                                           = 1286;  // <_initialize_default> ::= <_then> <_to> DEFAULT
//		final int PROD_INITIATE_STATEMENT_INITIATE                                           = 1287;  // <initiate_statement> ::= INITIATE <initiate_body>
//		final int PROD_INITIATE_BODY                                                         = 1288;  // <initiate_body> ::= <report_name>
//		final int PROD_INITIATE_BODY2                                                        = 1289;  // <initiate_body> ::= <initiate_body> <report_name>
		final int PROD_INSPECT_STATEMENT_INSPECT                                             = 1290;  // <inspect_statement> ::= INSPECT <inspect_body>
//		final int PROD_INSPECT_BODY                                                          = 1291;  // <inspect_body> ::= <send_identifier> <inspect_list>
//		final int PROD_SEND_IDENTIFIER                                                       = 1292;  // <send_identifier> ::= <identifier>
//		final int PROD_SEND_IDENTIFIER2                                                      = 1293;  // <send_identifier> ::= <literal>
//		final int PROD_SEND_IDENTIFIER3                                                      = 1294;  // <send_identifier> ::= <function>
		final int PROD_INSPECT_LIST                                                          = 1295;  // <inspect_list> ::= <inspect_tallying> <inspect_replacing>
//		final int PROD_INSPECT_LIST2                                                         = 1296;  // <inspect_list> ::= <inspect_tallying>
//		final int PROD_INSPECT_LIST3                                                         = 1297;  // <inspect_list> ::= <inspect_replacing>
//		final int PROD_INSPECT_LIST4                                                         = 1298;  // <inspect_list> ::= <inspect_converting>
		final int PROD_INSPECT_TALLYING_TALLYING                                             = 1299;  // <inspect_tallying> ::= TALLYING <tallying_list>
		final int PROD_INSPECT_REPLACING_REPLACING                                           = 1300;  // <inspect_replacing> ::= REPLACING <replacing_list>
		final int PROD_INSPECT_CONVERTING_CONVERTING_TO                                      = 1301;  // <inspect_converting> ::= CONVERTING <simple_display_value> TO <simple_display_all_value> <inspect_region>
//		final int PROD_TALLYING_LIST                                                         = 1302;  // <tallying_list> ::= <tallying_item>
		final int PROD_TALLYING_LIST2                                                        = 1303;  // <tallying_list> ::= <tallying_list> <tallying_item>
		final int PROD_TALLYING_ITEM_FOR                                                     = 1304;  // <tallying_item> ::= <numeric_identifier> FOR
		final int PROD_TALLYING_ITEM_CHARACTERS                                              = 1305;  // <tallying_item> ::= CHARACTERS <inspect_region>
		final int PROD_TALLYING_ITEM_ALL                                                     = 1306;  // <tallying_item> ::= ALL
		final int PROD_TALLYING_ITEM_LEADING                                                 = 1307;  // <tallying_item> ::= LEADING
		final int PROD_TALLYING_ITEM_TRAILING                                                = 1308;  // <tallying_item> ::= TRAILING
		final int PROD_TALLYING_ITEM                                                         = 1309;  // <tallying_item> ::= <simple_display_value> <inspect_region>
//		final int PROD_REPLACING_LIST                                                        = 1310;  // <replacing_list> ::= <replacing_item>
		final int PROD_REPLACING_LIST2                                                       = 1311;  // <replacing_list> ::= <replacing_list> <replacing_item>
		final int PROD_REPLACING_ITEM_CHARACTERS_BY                                          = 1312;  // <replacing_item> ::= CHARACTERS BY <simple_display_value> <inspect_region>
		final int PROD_REPLACING_ITEM                                                        = 1313;  // <replacing_item> ::= <rep_keyword> <replacing_region>
//		final int PROD_REP_KEYWORD                                                           = 1314;  // <rep_keyword> ::=
		final int PROD_REP_KEYWORD_ALL                                                       = 1315;  // <rep_keyword> ::= ALL
		final int PROD_REP_KEYWORD_LEADING                                                   = 1316;  // <rep_keyword> ::= LEADING
		final int PROD_REP_KEYWORD_FIRST                                                     = 1317;  // <rep_keyword> ::= FIRST
		final int PROD_REP_KEYWORD_TRAILING                                                  = 1318;  // <rep_keyword> ::= TRAILING
//		final int PROD_REPLACING_REGION_BY                                                   = 1319;  // <replacing_region> ::= <simple_display_value> BY <simple_display_all_value> <inspect_region>
//		final int PROD_INSPECT_REGION                                                        = 1320;  // <inspect_region> ::=
//		final int PROD_INSPECT_REGION2                                                       = 1321;  // <inspect_region> ::= <inspect_before>
//		final int PROD_INSPECT_REGION3                                                       = 1322;  // <inspect_region> ::= <inspect_after>
		final int PROD_INSPECT_REGION4                                                       = 1323;  // <inspect_region> ::= <inspect_before> <inspect_after>
		final int PROD_INSPECT_REGION5                                                       = 1324;  // <inspect_region> ::= <inspect_after> <inspect_before>
		final int PROD_INSPECT_BEFORE_BEFORE                                                 = 1325;  // <inspect_before> ::= BEFORE <_initial> <x>
		final int PROD_INSPECT_AFTER_AFTER                                                   = 1326;  // <inspect_after> ::= AFTER <_initial> <x>
//		final int PROD_MERGE_STATEMENT_MERGE                                                 = 1327;  // <merge_statement> ::= MERGE <sort_body>
		final int PROD_MOVE_STATEMENT_MOVE                                                   = 1328;  // <move_statement> ::= MOVE <move_body>
//		final int PROD_MOVE_BODY_TO                                                          = 1329;  // <move_body> ::= <x> TO <target_x_list>
//		final int PROD_MOVE_BODY_CORRESPONDING_TO                                            = 1330;  // <move_body> ::= CORRESPONDING <x> TO <target_x_list>
		final int PROD_MULTIPLY_STATEMENT_MULTIPLY                                           = 1331;  // <multiply_statement> ::= MULTIPLY <multiply_body> <end_multiply>
//		final int PROD_MULTIPLY_BODY_BY                                                      = 1332;  // <multiply_body> ::= <x> BY <arithmetic_x_list> <on_size_error_phrases>
		final int PROD_MULTIPLY_BODY_BY_GIVING                                               = 1333;  // <multiply_body> ::= <x> BY <x> GIVING <arithmetic_x_list> <on_size_error_phrases>
//		final int PROD_END_MULTIPLY                                                          = 1334;  // <end_multiply> ::=
//		final int PROD_END_MULTIPLY_END_MULTIPLY                                             = 1335;  // <end_multiply> ::= 'END_MULTIPLY'
		final int PROD_OPEN_STATEMENT_OPEN                                                   = 1336;  // <open_statement> ::= OPEN <open_body>
//		final int PROD_OPEN_BODY                                                             = 1337;  // <open_body> ::= <open_file_entry>
//		final int PROD_OPEN_BODY2                                                            = 1338;  // <open_body> ::= <open_body> <open_file_entry>
//		final int PROD_OPEN_FILE_ENTRY                                                       = 1339;  // <open_file_entry> ::= <open_mode> <open_sharing> <_retry_phrase> <file_name_list> <open_option>
		final int PROD_OPEN_MODE_INPUT                                                       = 1340;  // <open_mode> ::= INPUT
		final int PROD_OPEN_MODE_OUTPUT                                                      = 1341;  // <open_mode> ::= OUTPUT
		final int PROD_OPEN_MODE_I_O                                                         = 1342;  // <open_mode> ::= 'I_O'
		final int PROD_OPEN_MODE_EXTEND                                                      = 1343;  // <open_mode> ::= EXTEND
//		final int PROD_OPEN_SHARING                                                          = 1344;  // <open_sharing> ::=
//		final int PROD_OPEN_SHARING_SHARING                                                  = 1345;  // <open_sharing> ::= SHARING <_with> <sharing_option>
//		final int PROD_OPEN_OPTION                                                           = 1346;  // <open_option> ::=
//		final int PROD_OPEN_OPTION_NO_REWIND                                                 = 1347;  // <open_option> ::= <_with> NO REWIND
//		final int PROD_OPEN_OPTION_LOCK                                                      = 1348;  // <open_option> ::= <_with> LOCK
//		final int PROD_OPEN_OPTION_REVERSED                                                  = 1349;  // <open_option> ::= REVERSED
		final int PROD_PERFORM_STATEMENT_PERFORM                                             = 1350;  // <perform_statement> ::= PERFORM <perform_body>
		final int PROD_PERFORM_BODY                                                          = 1351;  // <perform_body> ::= <perform_procedure> <perform_option>
		final int PROD_PERFORM_BODY2                                                         = 1352;  // <perform_body> ::= <perform_option> <statement_list> <end_perform>
//		final int PROD_PERFORM_BODY3                                                         = 1353;  // <perform_body> ::= <perform_option> <term_or_dot>
//		final int PROD_END_PERFORM                                                           = 1354;  // <end_perform> ::=
//		final int PROD_END_PERFORM_END_PERFORM                                               = 1355;  // <end_perform> ::= 'END_PERFORM'
//		final int PROD_TERM_OR_DOT_END_PERFORM                                               = 1356;  // <term_or_dot> ::= 'END_PERFORM'
//		final int PROD_TERM_OR_DOT_TOK_DOT                                                   = 1357;  // <term_or_dot> ::= 'TOK_DOT'
//		final int PROD_PERFORM_PROCEDURE                                                     = 1358;  // <perform_procedure> ::= <procedure_name>
//		final int PROD_PERFORM_PROCEDURE_THRU                                                = 1359;  // <perform_procedure> ::= <procedure_name> THRU <procedure_name>
		final int PROD_PERFORM_OPTION                                                        = 1360;  // <perform_option> ::=
		final int PROD_PERFORM_OPTION_TIMES                                                  = 1361;  // <perform_option> ::= <id_or_lit_or_length_or_func> TIMES
		final int PROD_PERFORM_OPTION_FOREVER                                                = 1362;  // <perform_option> ::= FOREVER
		final int PROD_PERFORM_OPTION_UNTIL                                                  = 1363;  // <perform_option> ::= <perform_test> UNTIL <cond_or_exit>
		final int PROD_PERFORM_OPTION_VARYING                                                = 1364;  // <perform_option> ::= <perform_test> VARYING <perform_varying_list>
		final int PROD_PERFORM_TEST                                                          = 1365;  // <perform_test> ::=
		final int PROD_PERFORM_TEST_TEST                                                     = 1366;  // <perform_test> ::= <_with> TEST <before_or_after>
//		final int PROD_COND_OR_EXIT_EXIT                                                     = 1367;  // <cond_or_exit> ::= EXIT
//		final int PROD_COND_OR_EXIT                                                          = 1368;  // <cond_or_exit> ::= <condition>
//		final int PROD_PERFORM_VARYING_LIST                                                  = 1369;  // <perform_varying_list> ::= <perform_varying>
		final int PROD_PERFORM_VARYING_LIST_AFTER                                            = 1370;  // <perform_varying_list> ::= <perform_varying_list> AFTER <perform_varying>
//		final int PROD_PERFORM_VARYING_FROM_BY_UNTIL                                         = 1371;  // <perform_varying> ::= <identifier> FROM <x> BY <x> UNTIL <condition>
//		final int PROD_PURGE_STATEMENT_PURGE                                                 = 1372;  // <purge_statement> ::= PURGE <cd_name>
		final int PROD_READ_STATEMENT_READ                                                   = 1373;  // <read_statement> ::= READ <read_body> <end_read>
//		final int PROD_READ_BODY                                                             = 1374;  // <read_body> ::= <file_name> <_flag_next> <_record> <read_into> <lock_phrases> <read_key> <read_handler>
//		final int PROD_READ_INTO                                                             = 1375;  // <read_into> ::=
		final int PROD_READ_INTO_INTO                                                        = 1376;  // <read_into> ::= INTO <identifier>
//		final int PROD_LOCK_PHRASES                                                          = 1377;  // <lock_phrases> ::=
//		final int PROD_LOCK_PHRASES2                                                         = 1378;  // <lock_phrases> ::= <ignoring_lock>
//		final int PROD_LOCK_PHRASES3                                                         = 1379;  // <lock_phrases> ::= <advancing_lock_or_retry> <_extended_with_lock>
//		final int PROD_LOCK_PHRASES4                                                         = 1380;  // <lock_phrases> ::= <extended_with_lock>
//		final int PROD_IGNORING_LOCK_IGNORING_LOCK                                           = 1381;  // <ignoring_lock> ::= IGNORING LOCK
//		final int PROD_IGNORING_LOCK_IGNORE_LOCK                                             = 1382;  // <ignoring_lock> ::= <_with> IGNORE LOCK
//		final int PROD_ADVANCING_LOCK_OR_RETRY_ADVANCING_LOCK                                = 1383;  // <advancing_lock_or_retry> ::= ADVANCING <_on> LOCK
//		final int PROD_ADVANCING_LOCK_OR_RETRY                                               = 1384;  // <advancing_lock_or_retry> ::= <retry_phrase>
//		final int PROD__RETRY_PHRASE                                                         = 1385;  // <_retry_phrase> ::=
//		final int PROD__RETRY_PHRASE2                                                        = 1386;  // <_retry_phrase> ::= <retry_phrase>
//		final int PROD_RETRY_PHRASE                                                          = 1387;  // <retry_phrase> ::= <retry_options>
//		final int PROD_RETRY_OPTIONS_RETRY_TIMES                                             = 1388;  // <retry_options> ::= RETRY <_for> <exp> TIMES
//		final int PROD_RETRY_OPTIONS_RETRY_SECONDS                                           = 1389;  // <retry_options> ::= RETRY <_for> <exp> SECONDS
//		final int PROD_RETRY_OPTIONS_RETRY_FOREVER                                           = 1390;  // <retry_options> ::= RETRY FOREVER
//		final int PROD__EXTENDED_WITH_LOCK                                                   = 1391;  // <_extended_with_lock> ::=
//		final int PROD__EXTENDED_WITH_LOCK2                                                  = 1392;  // <_extended_with_lock> ::= <extended_with_lock>
//		final int PROD_EXTENDED_WITH_LOCK                                                    = 1393;  // <extended_with_lock> ::= <with_lock>
//		final int PROD_EXTENDED_WITH_LOCK_KEPT_LOCK                                          = 1394;  // <extended_with_lock> ::= <_with> KEPT LOCK
//		final int PROD_EXTENDED_WITH_LOCK_WAIT                                               = 1395;  // <extended_with_lock> ::= <_with> WAIT
//		final int PROD_READ_KEY                                                              = 1396;  // <read_key> ::=
//		final int PROD_READ_KEY_KEY                                                          = 1397;  // <read_key> ::= KEY <_is> <identifier>
//		final int PROD_READ_HANDLER                                                          = 1398;  // <read_handler> ::= <_invalid_key_phrases>
//		final int PROD_READ_HANDLER2                                                         = 1399;  // <read_handler> ::= <at_end>
//		final int PROD_END_READ                                                              = 1400;  // <end_read> ::=
//		final int PROD_END_READ_END_READ                                                     = 1401;  // <end_read> ::= 'END_READ'
//		final int PROD_READY_STATEMENT_READY_TRACE                                           = 1402;  // <ready_statement> ::= 'READY_TRACE'
//		final int PROD_RECEIVE_STATEMENT_RECEIVE                                             = 1403;  // <receive_statement> ::= RECEIVE <receive_body> <end_receive>
//		final int PROD_RECEIVE_BODY_INTO                                                     = 1404;  // <receive_body> ::= <cd_name> <message_or_segment> INTO <identifier> <_data_sentence_phrases>
//		final int PROD_MESSAGE_OR_SEGMENT_MESSAGE                                            = 1405;  // <message_or_segment> ::= MESSAGE
//		final int PROD_MESSAGE_OR_SEGMENT_SEGMENT                                            = 1406;  // <message_or_segment> ::= SEGMENT
//		final int PROD__DATA_SENTENCE_PHRASES                                                = 1407;  // <_data_sentence_phrases> ::=
//		final int PROD__DATA_SENTENCE_PHRASES2                                               = 1408;  // <_data_sentence_phrases> ::= <no_data_sentence> <_with_data_sentence>
//		final int PROD__DATA_SENTENCE_PHRASES3                                               = 1409;  // <_data_sentence_phrases> ::= <with_data_sentence> <_no_data_sentence>
//		final int PROD__NO_DATA_SENTENCE                                                     = 1410;  // <_no_data_sentence> ::=
//		final int PROD__NO_DATA_SENTENCE2                                                    = 1411;  // <_no_data_sentence> ::= <no_data_sentence>
//		final int PROD_NO_DATA_SENTENCE_NO_DATA                                              = 1412;  // <no_data_sentence> ::= 'NO_DATA' <statement_list>
//		final int PROD__WITH_DATA_SENTENCE                                                   = 1413;  // <_with_data_sentence> ::=
//		final int PROD__WITH_DATA_SENTENCE2                                                  = 1414;  // <_with_data_sentence> ::= <with_data_sentence>
//		final int PROD_WITH_DATA_SENTENCE_WITH_DATA                                          = 1415;  // <with_data_sentence> ::= 'WITH_DATA' <statement_list>
//		final int PROD_END_RECEIVE                                                           = 1416;  // <end_receive> ::=
//		final int PROD_END_RECEIVE_END_RECEIVE                                               = 1417;  // <end_receive> ::= 'END_RECEIVE'
//		final int PROD_RELEASE_STATEMENT_RELEASE                                             = 1418;  // <release_statement> ::= RELEASE <release_body>
//		final int PROD_RELEASE_BODY                                                          = 1419;  // <release_body> ::= <record_name> <from_option>
//		final int PROD_RESET_STATEMENT_RESET_TRACE                                           = 1420;  // <reset_statement> ::= 'RESET_TRACE'
//		final int PROD_RETURN_STATEMENT_RETURN                                               = 1421;  // <return_statement> ::= RETURN <return_body> <end_return>
//		final int PROD_RETURN_BODY                                                           = 1422;  // <return_body> ::= <file_name> <_record> <read_into> <return_at_end>
//		final int PROD_END_RETURN                                                            = 1423;  // <end_return> ::=
//		final int PROD_END_RETURN_END_RETURN                                                 = 1424;  // <end_return> ::= 'END_RETURN'
		final int PROD_REWRITE_STATEMENT_REWRITE                                             = 1425;  // <rewrite_statement> ::= REWRITE <rewrite_body> <end_rewrite>
//		final int PROD_REWRITE_BODY                                                          = 1426;  // <rewrite_body> ::= <file_or_record_name> <from_option> <_retry_phrase> <_with_lock> <_invalid_key_phrases>
//		final int PROD__WITH_LOCK                                                            = 1427;  // <_with_lock> ::=
//		final int PROD__WITH_LOCK2                                                           = 1428;  // <_with_lock> ::= <with_lock>
//		final int PROD_WITH_LOCK_LOCK                                                        = 1429;  // <with_lock> ::= <_with> LOCK
//		final int PROD_WITH_LOCK_NO_LOCK                                                     = 1430;  // <with_lock> ::= <_with> NO LOCK
//		final int PROD_END_REWRITE                                                           = 1431;  // <end_rewrite> ::=
//		final int PROD_END_REWRITE_END_REWRITE                                               = 1432;  // <end_rewrite> ::= 'END_REWRITE'
//		final int PROD_ROLLBACK_STATEMENT_ROLLBACK                                           = 1433;  // <rollback_statement> ::= ROLLBACK
		final int PROD_SEARCH_STATEMENT_SEARCH                                               = 1434;  // <search_statement> ::= SEARCH <search_body> <end_search>
		final int PROD_SEARCH_BODY                                                           = 1435;  // <search_body> ::= <table_name> <search_varying> <search_at_end> <search_whens>
//		final int PROD_SEARCH_BODY_ALL_WHEN                                                  = 1436;  // <search_body> ::= ALL <table_name> <search_at_end> WHEN <expr> <statement_list>
//		final int PROD_SEARCH_VARYING                                                        = 1437;  // <search_varying> ::=
		final int PROD_SEARCH_VARYING_VARYING                                                = 1438;  // <search_varying> ::= VARYING <identifier>
//		final int PROD_SEARCH_AT_END                                                         = 1439;  // <search_at_end> ::=
		final int PROD_SEARCH_AT_END_END                                                     = 1440;  // <search_at_end> ::= END <statement_list>
//		final int PROD_SEARCH_WHENS                                                          = 1441;  // <search_whens> ::= <search_when>
		final int PROD_SEARCH_WHENS2                                                         = 1442;  // <search_whens> ::= <search_when> <search_whens>
//		final int PROD_SEARCH_WHEN_WHEN                                                      = 1443;  // <search_when> ::= WHEN <condition> <statement_list>
//		final int PROD_END_SEARCH                                                            = 1444;  // <end_search> ::=
//		final int PROD_END_SEARCH_END_SEARCH                                                 = 1445;  // <end_search> ::= 'END_SEARCH'
//		final int PROD_SEND_STATEMENT_SEND                                                   = 1446;  // <send_statement> ::= SEND <send_body>
//		final int PROD_SEND_BODY                                                             = 1447;  // <send_body> ::= <cd_name> <from_identifier>
//		final int PROD_SEND_BODY2                                                            = 1448;  // <send_body> ::= <cd_name> <_from_identifier> <with_indicator> <write_option> <_replacing_line>
//		final int PROD__FROM_IDENTIFIER                                                      = 1449;  // <_from_identifier> ::=
//		final int PROD__FROM_IDENTIFIER2                                                     = 1450;  // <_from_identifier> ::= <from_identifier>
//		final int PROD_FROM_IDENTIFIER_FROM                                                  = 1451;  // <from_identifier> ::= FROM <identifier>
//		final int PROD_WITH_INDICATOR                                                        = 1452;  // <with_indicator> ::= <_with> <identifier>
//		final int PROD_WITH_INDICATOR_ESI                                                    = 1453;  // <with_indicator> ::= <_with> ESI
//		final int PROD_WITH_INDICATOR_EMI                                                    = 1454;  // <with_indicator> ::= <_with> EMI
//		final int PROD_WITH_INDICATOR_EGI                                                    = 1455;  // <with_indicator> ::= <_with> EGI
//		final int PROD__REPLACING_LINE                                                       = 1456;  // <_replacing_line> ::=
//		final int PROD__REPLACING_LINE_REPLACING                                             = 1457;  // <_replacing_line> ::= REPLACING <_line>
		final int PROD_SET_STATEMENT_SET                                                     = 1458;  // <set_statement> ::= SET <set_body>
//		final int PROD_SET_BODY                                                              = 1459;  // <set_body> ::= <set_environment>
//		final int PROD_SET_BODY2                                                             = 1460;  // <set_body> ::= <set_attr>
//		final int PROD_SET_BODY3                                                             = 1461;  // <set_body> ::= <set_to>
//		final int PROD_SET_BODY4                                                             = 1462;  // <set_body> ::= <set_up_down>
//		final int PROD_SET_BODY5                                                             = 1463;  // <set_body> ::= <set_to_on_off_sequence>
//		final int PROD_SET_BODY6                                                             = 1464;  // <set_body> ::= <set_to_true_false_sequence>
//		final int PROD_SET_BODY7                                                             = 1465;  // <set_body> ::= <set_last_exception_to_off>
//		final int PROD_ON_OR_OFF_ON                                                          = 1466;  // <on_or_off> ::= ON
//		final int PROD_ON_OR_OFF_OFF                                                         = 1467;  // <on_or_off> ::= OFF
//		final int PROD_UP_OR_DOWN_UP                                                         = 1468;  // <up_or_down> ::= UP
//		final int PROD_UP_OR_DOWN_DOWN                                                       = 1469;  // <up_or_down> ::= DOWN
//		final int PROD_SET_ENVIRONMENT_ENVIRONMENT_TO                                        = 1470;  // <set_environment> ::= ENVIRONMENT <simple_display_value> TO <simple_display_value>
//		final int PROD_SET_ATTR_ATTRIBUTE                                                    = 1471;  // <set_attr> ::= <sub_identifier> ATTRIBUTE <set_attr_clause>
//		final int PROD_SET_ATTR_CLAUSE                                                       = 1472;  // <set_attr_clause> ::= <set_attr_one>
//		final int PROD_SET_ATTR_CLAUSE2                                                      = 1473;  // <set_attr_clause> ::= <set_attr_clause> <set_attr_one>
//		final int PROD_SET_ATTR_ONE_BELL                                                     = 1474;  // <set_attr_one> ::= BELL <on_or_off>
//		final int PROD_SET_ATTR_ONE_BLINK                                                    = 1475;  // <set_attr_one> ::= BLINK <on_or_off>
//		final int PROD_SET_ATTR_ONE_HIGHLIGHT                                                = 1476;  // <set_attr_one> ::= HIGHLIGHT <on_or_off>
//		final int PROD_SET_ATTR_ONE_LOWLIGHT                                                 = 1477;  // <set_attr_one> ::= LOWLIGHT <on_or_off>
//		final int PROD_SET_ATTR_ONE_REVERSE_VIDEO                                            = 1478;  // <set_attr_one> ::= 'REVERSE_VIDEO' <on_or_off>
//		final int PROD_SET_ATTR_ONE_UNDERLINE                                                = 1479;  // <set_attr_one> ::= UNDERLINE <on_or_off>
//		final int PROD_SET_ATTR_ONE_LEFTLINE                                                 = 1480;  // <set_attr_one> ::= LEFTLINE <on_or_off>
//		final int PROD_SET_ATTR_ONE_OVERLINE                                                 = 1481;  // <set_attr_one> ::= OVERLINE <on_or_off>
//		final int PROD_SET_TO_TO_ENTRY                                                       = 1482;  // <set_to> ::= <target_x_list> TO ENTRY <alnum_or_id>
		final int PROD_SET_TO_TO                                                             = 1483;  // <set_to> ::= <target_x_list> TO <x>
//		final int PROD_SET_UP_DOWN_BY                                                        = 1484;  // <set_up_down> ::= <target_x_list> <up_or_down> BY <x>
//		final int PROD_SET_TO_ON_OFF_SEQUENCE                                                = 1485;  // <set_to_on_off_sequence> ::= <set_to_on_off>
//		final int PROD_SET_TO_ON_OFF_SEQUENCE2                                               = 1486;  // <set_to_on_off_sequence> ::= <set_to_on_off_sequence> <set_to_on_off>
//		final int PROD_SET_TO_ON_OFF_TO                                                      = 1487;  // <set_to_on_off> ::= <mnemonic_name_list> TO <on_or_off>
		final int PROD_SET_TO_TRUE_FALSE_SEQUENCE                                            = 1488;  // <set_to_true_false_sequence> ::= <set_to_true_false>
		final int PROD_SET_TO_TRUE_FALSE_SEQUENCE2                                           = 1489;  // <set_to_true_false_sequence> ::= <set_to_true_false_sequence> <set_to_true_false>
		final int PROD_SET_TO_TRUE_FALSE_TO_TOK_TRUE                                         = 1490;  // <set_to_true_false> ::= <target_x_list> TO 'TOK_TRUE'
		final int PROD_SET_TO_TRUE_FALSE_TO_TOK_FALSE                                        = 1491;  // <set_to_true_false> ::= <target_x_list> TO 'TOK_FALSE'
//		final int PROD_SET_LAST_EXCEPTION_TO_OFF_LAST_EXCEPTION_TO_OFF                       = 1492;  // <set_last_exception_to_off> ::= LAST EXCEPTION TO OFF
//		final int PROD_SORT_STATEMENT_SORT                                                   = 1493;  // <sort_statement> ::= SORT <sort_body>
//		final int PROD_SORT_BODY                                                             = 1494;  // <sort_body> ::= <table_identifier> <sort_key_list> <_sort_duplicates> <sort_collating> <sort_input> <sort_output>
//		final int PROD_SORT_KEY_LIST                                                         = 1495;  // <sort_key_list> ::=
//		final int PROD_SORT_KEY_LIST2                                                        = 1496;  // <sort_key_list> ::= <sort_key_list> <_on> <ascending_or_descending> <_key> <_key_list>
//		final int PROD__KEY_LIST                                                             = 1497;  // <_key_list> ::=
//		final int PROD__KEY_LIST2                                                            = 1498;  // <_key_list> ::= <_key_list> <qualified_word>
//		final int PROD__SORT_DUPLICATES                                                      = 1499;  // <_sort_duplicates> ::=
//		final int PROD__SORT_DUPLICATES2                                                     = 1500;  // <_sort_duplicates> ::= <with_dups> <_in_order>
//		final int PROD_SORT_COLLATING                                                        = 1501;  // <sort_collating> ::=
//		final int PROD_SORT_COLLATING2                                                       = 1502;  // <sort_collating> ::= <coll_sequence> <_is> <reference>
//		final int PROD_SORT_INPUT                                                            = 1503;  // <sort_input> ::=
//		final int PROD_SORT_INPUT_USING                                                      = 1504;  // <sort_input> ::= USING <file_name_list>
//		final int PROD_SORT_INPUT_INPUT_PROCEDURE                                            = 1505;  // <sort_input> ::= INPUT PROCEDURE <_is> <perform_procedure>
//		final int PROD_SORT_OUTPUT                                                           = 1506;  // <sort_output> ::=
//		final int PROD_SORT_OUTPUT_GIVING                                                    = 1507;  // <sort_output> ::= GIVING <file_name_list>
//		final int PROD_SORT_OUTPUT_OUTPUT_PROCEDURE                                          = 1508;  // <sort_output> ::= OUTPUT PROCEDURE <_is> <perform_procedure>
		final int PROD_START_STATEMENT_START                                                 = 1509;  // <start_statement> ::= START <start_body> <end_start>
//		final int PROD_START_BODY                                                            = 1510;  // <start_body> ::= <file_name> <start_key> <sizelen_clause> <_invalid_key_phrases>
//		final int PROD_SIZELEN_CLAUSE                                                        = 1511;  // <sizelen_clause> ::=
//		final int PROD_SIZELEN_CLAUSE2                                                       = 1512;  // <sizelen_clause> ::= <_with> <size_or_length> <exp>
//		final int PROD_START_KEY                                                             = 1513;  // <start_key> ::=
//		final int PROD_START_KEY_KEY                                                         = 1514;  // <start_key> ::= KEY <_is> <start_op> <identifier>
//		final int PROD_START_KEY_FIRST                                                       = 1515;  // <start_key> ::= FIRST
//		final int PROD_START_KEY_LAST                                                        = 1516;  // <start_key> ::= LAST
//		final int PROD_START_OP                                                              = 1517;  // <start_op> ::= <eq>
//		final int PROD_START_OP2                                                             = 1518;  // <start_op> ::= <_flag_not> <gt>
//		final int PROD_START_OP3                                                             = 1519;  // <start_op> ::= <_flag_not> <lt>
//		final int PROD_START_OP4                                                             = 1520;  // <start_op> ::= <_flag_not> <ge>
//		final int PROD_START_OP5                                                             = 1521;  // <start_op> ::= <_flag_not> <le>
//		final int PROD_START_OP6                                                             = 1522;  // <start_op> ::= <disallowed_op>
//		final int PROD_DISALLOWED_OP                                                         = 1523;  // <disallowed_op> ::= <not_equal_op>
//		final int PROD_NOT_EQUAL_OP_NOT                                                      = 1524;  // <not_equal_op> ::= NOT <eq>
//		final int PROD_NOT_EQUAL_OP_NOT_EQUAL                                                = 1525;  // <not_equal_op> ::= 'NOT_EQUAL'
//		final int PROD_END_START                                                             = 1526;  // <end_start> ::=
//		final int PROD_END_START_END_START                                                   = 1527;  // <end_start> ::= 'END_START'
		final int PROD_STOP_STATEMENT_STOP_RUN                                               = 1528;  // <stop_statement> ::= STOP RUN <stop_returning>
		final int PROD_STOP_STATEMENT_STOP                                                   = 1529;  // <stop_statement> ::= STOP <stop_literal>
//		final int PROD_STOP_RETURNING                                                        = 1530;  // <stop_returning> ::=
//		final int PROD_STOP_RETURNING2                                                       = 1531;  // <stop_returning> ::= <return_give> <x>
//		final int PROD_STOP_RETURNING3                                                       = 1532;  // <stop_returning> ::= <x>
//		final int PROD_STOP_RETURNING_ERROR                                                  = 1533;  // <stop_returning> ::= <_with> ERROR <_status> <_status_x>
//		final int PROD_STOP_RETURNING_NORMAL                                                 = 1534;  // <stop_returning> ::= <_with> NORMAL <_status> <_status_x>
//		final int PROD__STATUS_X                                                             = 1535;  // <_status_x> ::=
//		final int PROD__STATUS_X2                                                            = 1536;  // <_status_x> ::= <x>
//		final int PROD_STOP_LITERAL                                                          = 1537;  // <stop_literal> ::= <LITERAL_TOK>
//		final int PROD_STOP_LITERAL_SPACE                                                    = 1538;  // <stop_literal> ::= SPACE
//		final int PROD_STOP_LITERAL_ZERO                                                     = 1539;  // <stop_literal> ::= ZERO
//		final int PROD_STOP_LITERAL_QUOTE                                                    = 1540;  // <stop_literal> ::= QUOTE
		final int PROD_STRING_STATEMENT_STRING                                               = 1541;  // <string_statement> ::= STRING <string_body> <end_string>
//		final int PROD_STRING_BODY_INTO                                                      = 1542;  // <string_body> ::= <string_item_list> INTO <identifier> <_with_pointer> <_on_overflow_phrases>
//		final int PROD_STRING_ITEM_LIST                                                      = 1543;  // <string_item_list> ::= <string_item>
		final int PROD_STRING_ITEM_LIST2                                                     = 1544;  // <string_item_list> ::= <string_item_list> <string_item>
//		final int PROD_STRING_ITEM                                                           = 1545;  // <string_item> ::= <x> <_string_delimited>
//		final int PROD__STRING_DELIMITED                                                     = 1546;  // <_string_delimited> ::=
//		final int PROD__STRING_DELIMITED_DELIMITED                                           = 1547;  // <_string_delimited> ::= DELIMITED <_by> <string_delimiter>
//		final int PROD_STRING_DELIMITER_SIZE                                                 = 1548;  // <string_delimiter> ::= SIZE
//		final int PROD_STRING_DELIMITER                                                      = 1549;  // <string_delimiter> ::= <x>
//		final int PROD__WITH_POINTER                                                         = 1550;  // <_with_pointer> ::=
//		final int PROD__WITH_POINTER_POINTER                                                 = 1551;  // <_with_pointer> ::= <_with> POINTER <_is> <identifier>
//		final int PROD_END_STRING                                                            = 1552;  // <end_string> ::=
//		final int PROD_END_STRING_END_STRING                                                 = 1553;  // <end_string> ::= 'END_STRING'
		final int PROD_SUBTRACT_STATEMENT_SUBTRACT                                           = 1554;  // <subtract_statement> ::= SUBTRACT <subtract_body> <end_subtract>
//		final int PROD_SUBTRACT_BODY_FROM                                                    = 1555;  // <subtract_body> ::= <x_list> FROM <arithmetic_x_list> <on_size_error_phrases>
		final int PROD_SUBTRACT_BODY_FROM_GIVING                                             = 1556;  // <subtract_body> ::= <x_list> FROM <x> GIVING <arithmetic_x_list> <on_size_error_phrases>
//		final int PROD_SUBTRACT_BODY_CORRESPONDING_FROM                                      = 1557;  // <subtract_body> ::= CORRESPONDING <identifier> FROM <identifier> <flag_rounded> <on_size_error_phrases>
//		final int PROD_SUBTRACT_BODY_TABLE_FROM                                              = 1558;  // <subtract_body> ::= TABLE <table_identifier> FROM <table_identifier> <flag_rounded> <_from_idx_to_idx> <_dest_index> <on_size_error_phrases>
//		final int PROD_END_SUBTRACT                                                          = 1559;  // <end_subtract> ::=
//		final int PROD_END_SUBTRACT_END_SUBTRACT                                             = 1560;  // <end_subtract> ::= 'END_SUBTRACT'
//		final int PROD_SUPPRESS_STATEMENT_SUPPRESS                                           = 1561;  // <suppress_statement> ::= SUPPRESS <_printing>
//		final int PROD__PRINTING                                                             = 1562;  // <_printing> ::=
//		final int PROD__PRINTING_PRINTING                                                    = 1563;  // <_printing> ::= PRINTING
//		final int PROD_TERMINATE_STATEMENT_TERMINATE                                         = 1564;  // <terminate_statement> ::= TERMINATE <terminate_body>
//		final int PROD_TERMINATE_BODY                                                        = 1565;  // <terminate_body> ::= <report_name>
//		final int PROD_TERMINATE_BODY2                                                       = 1566;  // <terminate_body> ::= <terminate_body> <report_name>
//		final int PROD_TRANSFORM_STATEMENT_TRANSFORM                                         = 1567;  // <transform_statement> ::= TRANSFORM <transform_body>
//		final int PROD_TRANSFORM_BODY_FROM_TO                                                = 1568;  // <transform_body> ::= <display_identifier> FROM <simple_display_value> TO <simple_display_all_value>
//		final int PROD_UNLOCK_STATEMENT_UNLOCK                                               = 1569;  // <unlock_statement> ::= UNLOCK <unlock_body>
//		final int PROD_UNLOCK_BODY                                                           = 1570;  // <unlock_body> ::= <file_name> <_records>
		final int PROD_UNSTRING_STATEMENT_UNSTRING                                           = 1571;  // <unstring_statement> ::= UNSTRING <unstring_body> <end_unstring>
//		final int PROD_UNSTRING_BODY                                                         = 1572;  // <unstring_body> ::= <identifier> <_unstring_delimited> <unstring_into> <_with_pointer> <_unstring_tallying> <_on_overflow_phrases>
//		final int PROD__UNSTRING_DELIMITED                                                   = 1573;  // <_unstring_delimited> ::=
//		final int PROD__UNSTRING_DELIMITED_DELIMITED                                         = 1574;  // <_unstring_delimited> ::= DELIMITED <_by> <unstring_delimited_list>
//		final int PROD_UNSTRING_DELIMITED_LIST                                               = 1575;  // <unstring_delimited_list> ::= <unstring_delimited_item>
//		final int PROD_UNSTRING_DELIMITED_LIST_OR                                            = 1576;  // <unstring_delimited_list> ::= <unstring_delimited_list> OR <unstring_delimited_item>
//		final int PROD_UNSTRING_DELIMITED_ITEM                                               = 1577;  // <unstring_delimited_item> ::= <flag_all> <simple_display_value>
		final int PROD_UNSTRING_INTO_INTO                                                    = 1578;  // <unstring_into> ::= INTO <unstring_into_item>
//		final int PROD_UNSTRING_INTO                                                         = 1579;  // <unstring_into> ::= <unstring_into> <unstring_into_item>
		final int PROD_UNSTRING_INTO_ITEM                                                    = 1580;  // <unstring_into_item> ::= <identifier> <_unstring_into_delimiter> <_unstring_into_count>
//		final int PROD_UNSTRING_INTO_ITEM_COMMA_DELIM                                        = 1581;  // <unstring_into_item> ::= 'COMMA_DELIM'
//		final int PROD__UNSTRING_INTO_DELIMITER                                              = 1582;  // <_unstring_into_delimiter> ::=
//		final int PROD__UNSTRING_INTO_DELIMITER_DELIMITER                                    = 1583;  // <_unstring_into_delimiter> ::= DELIMITER <_in> <identifier>
//		final int PROD__UNSTRING_INTO_COUNT                                                  = 1584;  // <_unstring_into_count> ::=
//		final int PROD__UNSTRING_INTO_COUNT_COUNT                                            = 1585;  // <_unstring_into_count> ::= COUNT <_in> <identifier>
//		final int PROD__UNSTRING_TALLYING                                                    = 1586;  // <_unstring_tallying> ::=
//		final int PROD__UNSTRING_TALLYING_TALLYING                                           = 1587;  // <_unstring_tallying> ::= TALLYING <_in> <identifier>
//		final int PROD_END_UNSTRING                                                          = 1588;  // <end_unstring> ::=
//		final int PROD_END_UNSTRING_END_UNSTRING                                             = 1589;  // <end_unstring> ::= 'END_UNSTRING'
//		final int PROD_USE_STATEMENT_USE                                                     = 1590;  // <use_statement> ::= USE <use_phrase>
//		final int PROD_USE_PHRASE                                                            = 1591;  // <use_phrase> ::= <use_file_exception>
//		final int PROD_USE_PHRASE2                                                           = 1592;  // <use_phrase> ::= <use_debugging>
//		final int PROD_USE_PHRASE3                                                           = 1593;  // <use_phrase> ::= <use_start_end>
//		final int PROD_USE_PHRASE4                                                           = 1594;  // <use_phrase> ::= <use_reporting>
//		final int PROD_USE_PHRASE5                                                           = 1595;  // <use_phrase> ::= <use_exception>
//		final int PROD_USE_FILE_EXCEPTION                                                    = 1596;  // <use_file_exception> ::= <use_global> <_after> <_standard> <exception_or_error> <_procedure> <_on> <use_file_exception_target>
//		final int PROD_USE_GLOBAL                                                            = 1597;  // <use_global> ::=
//		final int PROD_USE_GLOBAL_GLOBAL                                                     = 1598;  // <use_global> ::= GLOBAL
//		final int PROD_USE_FILE_EXCEPTION_TARGET                                             = 1599;  // <use_file_exception_target> ::= <file_name_list>
//		final int PROD_USE_FILE_EXCEPTION_TARGET_INPUT                                       = 1600;  // <use_file_exception_target> ::= INPUT
//		final int PROD_USE_FILE_EXCEPTION_TARGET_OUTPUT                                      = 1601;  // <use_file_exception_target> ::= OUTPUT
//		final int PROD_USE_FILE_EXCEPTION_TARGET_I_O                                         = 1602;  // <use_file_exception_target> ::= 'I_O'
//		final int PROD_USE_FILE_EXCEPTION_TARGET_EXTEND                                      = 1603;  // <use_file_exception_target> ::= EXTEND
//		final int PROD_USE_DEBUGGING_DEBUGGING                                               = 1604;  // <use_debugging> ::= <_for> DEBUGGING <_on> <debugging_list>
//		final int PROD_DEBUGGING_LIST                                                        = 1605;  // <debugging_list> ::= <debugging_target>
//		final int PROD_DEBUGGING_LIST2                                                       = 1606;  // <debugging_list> ::= <debugging_list> <debugging_target>
//		final int PROD_DEBUGGING_TARGET                                                      = 1607;  // <debugging_target> ::= <label>
//		final int PROD_DEBUGGING_TARGET_ALL_PROCEDURES                                       = 1608;  // <debugging_target> ::= ALL PROCEDURES
//		final int PROD_DEBUGGING_TARGET_ALL                                                  = 1609;  // <debugging_target> ::= ALL <_all_refs> <qualified_word>
//		final int PROD__ALL_REFS                                                             = 1610;  // <_all_refs> ::=
//		final int PROD__ALL_REFS_REFERENCES                                                  = 1611;  // <_all_refs> ::= REFERENCES
//		final int PROD__ALL_REFS_REFERENCES_OF                                               = 1612;  // <_all_refs> ::= REFERENCES OF
//		final int PROD__ALL_REFS_OF                                                          = 1613;  // <_all_refs> ::= OF
//		final int PROD_USE_START_END_PROGRAM                                                 = 1614;  // <use_start_end> ::= <_at> PROGRAM <program_start_end>
//		final int PROD_PROGRAM_START_END_START                                               = 1615;  // <program_start_end> ::= START
//		final int PROD_PROGRAM_START_END_END                                                 = 1616;  // <program_start_end> ::= END
//		final int PROD_USE_REPORTING_BEFORE_REPORTING                                        = 1617;  // <use_reporting> ::= <use_global> BEFORE REPORTING <identifier>
//		final int PROD_USE_EXCEPTION                                                         = 1618;  // <use_exception> ::= <use_ex_keyw>
//		final int PROD_USE_EX_KEYW_EXCEPTION_CONDITION                                       = 1619;  // <use_ex_keyw> ::= 'EXCEPTION_CONDITION'
//		final int PROD_USE_EX_KEYW_EC                                                        = 1620;  // <use_ex_keyw> ::= EC
		final int PROD_WRITE_STATEMENT_WRITE                                                 = 1621;  // <write_statement> ::= WRITE <write_body> <end_write>
//		final int PROD_WRITE_BODY                                                            = 1622;  // <write_body> ::= <file_or_record_name> <from_option> <write_option> <_retry_phrase> <_with_lock> <write_handler>
//		final int PROD_FROM_OPTION                                                           = 1623;  // <from_option> ::=
		final int PROD_FROM_OPTION_FROM                                                      = 1624;  // <from_option> ::= FROM <from_parameter>
//		final int PROD_WRITE_OPTION                                                          = 1625;  // <write_option> ::=
//		final int PROD_WRITE_OPTION2                                                         = 1626;  // <write_option> ::= <before_or_after> <_advancing> <num_id_or_lit> <_line_or_lines>
//		final int PROD_WRITE_OPTION3                                                         = 1627;  // <write_option> ::= <before_or_after> <_advancing> <mnemonic_name>
//		final int PROD_WRITE_OPTION_PAGE                                                     = 1628;  // <write_option> ::= <before_or_after> <_advancing> PAGE
		final int PROD_BEFORE_OR_AFTER_BEFORE                                                = 1629;  // <before_or_after> ::= BEFORE
		final int PROD_BEFORE_OR_AFTER_AFTER                                                 = 1630;  // <before_or_after> ::= AFTER
//		final int PROD_WRITE_HANDLER                                                         = 1631;  // <write_handler> ::=
//		final int PROD_WRITE_HANDLER2                                                        = 1632;  // <write_handler> ::= <invalid_key_phrases>
//		final int PROD_WRITE_HANDLER3                                                        = 1633;  // <write_handler> ::= <at_eop_clauses>
//		final int PROD_END_WRITE                                                             = 1634;  // <end_write> ::=
//		final int PROD_END_WRITE_END_WRITE                                                   = 1635;  // <end_write> ::= 'END_WRITE'
//		final int PROD__ACCEPT_EXCEPTION_PHRASES                                             = 1636;  // <_accept_exception_phrases> ::=
//		final int PROD__ACCEPT_EXCEPTION_PHRASES2                                            = 1637;  // <_accept_exception_phrases> ::= <accp_on_exception> <_accp_not_on_exception>
//		final int PROD__ACCEPT_EXCEPTION_PHRASES3                                            = 1638;  // <_accept_exception_phrases> ::= <accp_not_on_exception> <_accp_on_exception>
//		final int PROD__ACCP_ON_EXCEPTION                                                    = 1639;  // <_accp_on_exception> ::=
//		final int PROD__ACCP_ON_EXCEPTION2                                                   = 1640;  // <_accp_on_exception> ::= <accp_on_exception>
//		final int PROD_ACCP_ON_EXCEPTION                                                     = 1641;  // <accp_on_exception> ::= <escape_or_exception> <statement_list>
//		final int PROD_ESCAPE_OR_EXCEPTION_ESCAPE                                            = 1642;  // <escape_or_exception> ::= ESCAPE
//		final int PROD_ESCAPE_OR_EXCEPTION_EXCEPTION                                         = 1643;  // <escape_or_exception> ::= EXCEPTION
//		final int PROD__ACCP_NOT_ON_EXCEPTION                                                = 1644;  // <_accp_not_on_exception> ::=
//		final int PROD__ACCP_NOT_ON_EXCEPTION2                                               = 1645;  // <_accp_not_on_exception> ::= <accp_not_on_exception>
//		final int PROD_ACCP_NOT_ON_EXCEPTION                                                 = 1646;  // <accp_not_on_exception> ::= <not_escape_or_not_exception> <statement_list>
//		final int PROD_NOT_ESCAPE_OR_NOT_EXCEPTION_NOT_ESCAPE                                = 1647;  // <not_escape_or_not_exception> ::= 'NOT_ESCAPE'
//		final int PROD_NOT_ESCAPE_OR_NOT_EXCEPTION_NOT_EXCEPTION                             = 1648;  // <not_escape_or_not_exception> ::= 'NOT_EXCEPTION'
//		final int PROD__DISPLAY_EXCEPTION_PHRASES                                            = 1649;  // <_display_exception_phrases> ::=
//		final int PROD__DISPLAY_EXCEPTION_PHRASES2                                           = 1650;  // <_display_exception_phrases> ::= <disp_on_exception> <_disp_not_on_exception>
//		final int PROD__DISPLAY_EXCEPTION_PHRASES3                                           = 1651;  // <_display_exception_phrases> ::= <disp_not_on_exception> <_disp_on_exception>
//		final int PROD__DISP_ON_EXCEPTION                                                    = 1652;  // <_disp_on_exception> ::=
//		final int PROD__DISP_ON_EXCEPTION2                                                   = 1653;  // <_disp_on_exception> ::= <disp_on_exception>
//		final int PROD_DISP_ON_EXCEPTION_EXCEPTION                                           = 1654;  // <disp_on_exception> ::= EXCEPTION <statement_list>
//		final int PROD__DISP_NOT_ON_EXCEPTION                                                = 1655;  // <_disp_not_on_exception> ::=
//		final int PROD__DISP_NOT_ON_EXCEPTION2                                               = 1656;  // <_disp_not_on_exception> ::= <disp_not_on_exception>
//		final int PROD_DISP_NOT_ON_EXCEPTION_NOT_EXCEPTION                                   = 1657;  // <disp_not_on_exception> ::= 'NOT_EXCEPTION' <statement_list>
//		final int PROD_ON_SIZE_ERROR_PHRASES                                                 = 1658;  // <on_size_error_phrases> ::=
//		final int PROD_ON_SIZE_ERROR_PHRASES2                                                = 1659;  // <on_size_error_phrases> ::= <on_size_error> <_not_on_size_error>
//		final int PROD_ON_SIZE_ERROR_PHRASES3                                                = 1660;  // <on_size_error_phrases> ::= <not_on_size_error> <_on_size_error>
//		final int PROD__ON_SIZE_ERROR                                                        = 1661;  // <_on_size_error> ::=
//		final int PROD__ON_SIZE_ERROR2                                                       = 1662;  // <_on_size_error> ::= <on_size_error>
//		final int PROD_ON_SIZE_ERROR_SIZE_ERROR                                              = 1663;  // <on_size_error> ::= 'SIZE_ERROR' <statement_list>
//		final int PROD__NOT_ON_SIZE_ERROR                                                    = 1664;  // <_not_on_size_error> ::=
//		final int PROD__NOT_ON_SIZE_ERROR2                                                   = 1665;  // <_not_on_size_error> ::= <not_on_size_error>
//		final int PROD_NOT_ON_SIZE_ERROR_NOT_SIZE_ERROR                                      = 1666;  // <not_on_size_error> ::= 'NOT_SIZE_ERROR' <statement_list>
//		final int PROD__ON_OVERFLOW_PHRASES                                                  = 1667;  // <_on_overflow_phrases> ::=
//		final int PROD__ON_OVERFLOW_PHRASES2                                                 = 1668;  // <_on_overflow_phrases> ::= <on_overflow> <_not_on_overflow>
//		final int PROD__ON_OVERFLOW_PHRASES3                                                 = 1669;  // <_on_overflow_phrases> ::= <not_on_overflow> <_on_overflow>
//		final int PROD__ON_OVERFLOW                                                          = 1670;  // <_on_overflow> ::=
//		final int PROD__ON_OVERFLOW2                                                         = 1671;  // <_on_overflow> ::= <on_overflow>
//		final int PROD_ON_OVERFLOW_TOK_OVERFLOW                                              = 1672;  // <on_overflow> ::= 'TOK_OVERFLOW' <statement_list>
//		final int PROD__NOT_ON_OVERFLOW                                                      = 1673;  // <_not_on_overflow> ::=
//		final int PROD__NOT_ON_OVERFLOW2                                                     = 1674;  // <_not_on_overflow> ::= <not_on_overflow>
//		final int PROD_NOT_ON_OVERFLOW_NOT_OVERFLOW                                          = 1675;  // <not_on_overflow> ::= 'NOT_OVERFLOW' <statement_list>
//		final int PROD_RETURN_AT_END                                                         = 1676;  // <return_at_end> ::= <at_end_clause> <_not_at_end_clause>
//		final int PROD_RETURN_AT_END2                                                        = 1677;  // <return_at_end> ::= <not_at_end_clause> <at_end_clause>
//		final int PROD_AT_END                                                                = 1678;  // <at_end> ::= <at_end_clause> <_not_at_end_clause>
//		final int PROD_AT_END2                                                               = 1679;  // <at_end> ::= <not_at_end_clause> <_at_end_clause>
//		final int PROD__AT_END_CLAUSE                                                        = 1680;  // <_at_end_clause> ::=
//		final int PROD__AT_END_CLAUSE2                                                       = 1681;  // <_at_end_clause> ::= <at_end_clause>
//		final int PROD_AT_END_CLAUSE_END                                                     = 1682;  // <at_end_clause> ::= END <statement_list>
//		final int PROD__NOT_AT_END_CLAUSE                                                    = 1683;  // <_not_at_end_clause> ::=
//		final int PROD__NOT_AT_END_CLAUSE2                                                   = 1684;  // <_not_at_end_clause> ::= <not_at_end_clause>
//		final int PROD_NOT_AT_END_CLAUSE_NOT_END                                             = 1685;  // <not_at_end_clause> ::= 'NOT_END' <statement_list>
//		final int PROD_AT_EOP_CLAUSES                                                        = 1686;  // <at_eop_clauses> ::= <at_eop_clause> <_not_at_eop_clause>
//		final int PROD_AT_EOP_CLAUSES2                                                       = 1687;  // <at_eop_clauses> ::= <not_at_eop_clause> <_at_eop_clause>
//		final int PROD__AT_EOP_CLAUSE                                                        = 1688;  // <_at_eop_clause> ::=
//		final int PROD__AT_EOP_CLAUSE2                                                       = 1689;  // <_at_eop_clause> ::= <at_eop_clause>
//		final int PROD_AT_EOP_CLAUSE_EOP                                                     = 1690;  // <at_eop_clause> ::= EOP <statement_list>
//		final int PROD__NOT_AT_EOP_CLAUSE                                                    = 1691;  // <_not_at_eop_clause> ::=
//		final int PROD__NOT_AT_EOP_CLAUSE2                                                   = 1692;  // <_not_at_eop_clause> ::= <not_at_eop_clause>
//		final int PROD_NOT_AT_EOP_CLAUSE_NOT_EOP                                             = 1693;  // <not_at_eop_clause> ::= 'NOT_EOP' <statement_list>
//		final int PROD__INVALID_KEY_PHRASES                                                  = 1694;  // <_invalid_key_phrases> ::=
//		final int PROD__INVALID_KEY_PHRASES2                                                 = 1695;  // <_invalid_key_phrases> ::= <invalid_key_phrases>
//		final int PROD_INVALID_KEY_PHRASES                                                   = 1696;  // <invalid_key_phrases> ::= <invalid_key_sentence> <_not_invalid_key_sentence>
//		final int PROD_INVALID_KEY_PHRASES2                                                  = 1697;  // <invalid_key_phrases> ::= <not_invalid_key_sentence> <_invalid_key_sentence>
//		final int PROD__INVALID_KEY_SENTENCE                                                 = 1698;  // <_invalid_key_sentence> ::=
//		final int PROD__INVALID_KEY_SENTENCE2                                                = 1699;  // <_invalid_key_sentence> ::= <invalid_key_sentence>
//		final int PROD_INVALID_KEY_SENTENCE_INVALID_KEY                                      = 1700;  // <invalid_key_sentence> ::= 'INVALID_KEY' <statement_list>
//		final int PROD__NOT_INVALID_KEY_SENTENCE                                             = 1701;  // <_not_invalid_key_sentence> ::=
//		final int PROD__NOT_INVALID_KEY_SENTENCE2                                            = 1702;  // <_not_invalid_key_sentence> ::= <not_invalid_key_sentence>
//		final int PROD_NOT_INVALID_KEY_SENTENCE_NOT_INVALID_KEY                              = 1703;  // <not_invalid_key_sentence> ::= 'NOT_INVALID_KEY' <statement_list>
//		final int PROD__SCROLL_LINES                                                         = 1704;  // <_scroll_lines> ::=
//		final int PROD__SCROLL_LINES2                                                        = 1705;  // <_scroll_lines> ::= <pos_num_id_or_lit> <scroll_line_or_lines>
//		final int PROD_CONDITION                                                             = 1706;  // <condition> ::= <expr>
//		final int PROD_EXPR                                                                  = 1707;  // <expr> ::= <partial_expr>
//		final int PROD_PARTIAL_EXPR                                                          = 1708;  // <partial_expr> ::= <expr_tokens>
//		final int PROD_EXPR_TOKENS                                                           = 1709;  // <expr_tokens> ::= <expr_token>
//		final int PROD_EXPR_TOKENS2                                                          = 1710;  // <expr_tokens> ::= <expr_tokens> <expr_token>
//		final int PROD_EXPR_TOKEN                                                            = 1711;  // <expr_token> ::= <x>
		final int PROD_EXPR_TOKEN_IS                                                         = 1712;  // <expr_token> ::= IS <CLASS_NAME>
		final int PROD_EXPR_TOKEN2                                                           = 1713;  // <expr_token> ::= <_is> <condition_op>
		final int PROD_EXPR_TOKEN_IS2                                                        = 1714;  // <expr_token> ::= IS <not> <condition_or_class>
		final int PROD_EXPR_TOKEN_IS_ZERO                                                    = 1715;  // <expr_token> ::= IS <_not> ZERO
//		final int PROD_EXPR_TOKEN_TOK_OPEN_PAREN                                             = 1716;  // <expr_token> ::= 'TOK_OPEN_PAREN'
//		final int PROD_EXPR_TOKEN_TOK_CLOSE_PAREN                                            = 1717;  // <expr_token> ::= 'TOK_CLOSE_PAREN'
//		final int PROD_EXPR_TOKEN_TOK_PLUS                                                   = 1718;  // <expr_token> ::= 'TOK_PLUS'
//		final int PROD_EXPR_TOKEN_TOK_MINUS                                                  = 1719;  // <expr_token> ::= 'TOK_MINUS'
//		final int PROD_EXPR_TOKEN_TOK_MUL                                                    = 1720;  // <expr_token> ::= 'TOK_MUL'
//		final int PROD_EXPR_TOKEN_TOK_DIV                                                    = 1721;  // <expr_token> ::= 'TOK_DIV'
//		final int PROD_EXPR_TOKEN_EXPONENTIATION                                             = 1722;  // <expr_token> ::= EXPONENTIATION
//		final int PROD_EXPR_TOKEN3                                                           = 1723;  // <expr_token> ::= <not>
		final int PROD_EXPR_TOKEN_AND                                                        = 1724;  // <expr_token> ::= AND
		final int PROD_EXPR_TOKEN_OR                                                         = 1725;  // <expr_token> ::= OR
//		final int PROD__NOT                                                                  = 1726;  // <_not> ::=
		final int PROD__NOT2                                                                 = 1727;  // <_not> ::= <not>
//		final int PROD_NOT_NOT                                                               = 1728;  // <not> ::= NOT
		final int PROD_CONDITION_OR_CLASS                                                    = 1729;  // <condition_or_class> ::= <CLASS_NAME>
//		final int PROD_CONDITION_OR_CLASS2                                                   = 1730;  // <condition_or_class> ::= <condition_op>
//		final int PROD_CONDITION_OP                                                          = 1731;  // <condition_op> ::= <eq>
//		final int PROD_CONDITION_OP2                                                         = 1732;  // <condition_op> ::= <gt>
//		final int PROD_CONDITION_OP3                                                         = 1733;  // <condition_op> ::= <lt>
//		final int PROD_CONDITION_OP4                                                         = 1734;  // <condition_op> ::= <ge>
//		final int PROD_CONDITION_OP5                                                         = 1735;  // <condition_op> ::= <le>
		final int PROD_CONDITION_OP_NOT_EQUAL                                                = 1736;  // <condition_op> ::= 'NOT_EQUAL'
//		final int PROD_CONDITION_OP_OMITTED                                                  = 1737;  // <condition_op> ::= OMITTED
//		final int PROD_CONDITION_OP_NUMERIC                                                  = 1738;  // <condition_op> ::= NUMERIC
//		final int PROD_CONDITION_OP_ALPHABETIC                                               = 1739;  // <condition_op> ::= ALPHABETIC
//		final int PROD_CONDITION_OP_ALPHABETIC_LOWER                                         = 1740;  // <condition_op> ::= 'ALPHABETIC_LOWER'
//		final int PROD_CONDITION_OP_ALPHABETIC_UPPER                                         = 1741;  // <condition_op> ::= 'ALPHABETIC_UPPER'
//		final int PROD_CONDITION_OP_POSITIVE                                                 = 1742;  // <condition_op> ::= POSITIVE
//		final int PROD_CONDITION_OP_NEGATIVE                                                 = 1743;  // <condition_op> ::= NEGATIVE
		final int PROD_EQ_TOK_EQUAL                                                          = 1744;  // <eq> ::= 'TOK_EQUAL'
		final int PROD_EQ_EQUAL                                                              = 1745;  // <eq> ::= EQUAL <_to>
		final int PROD_GT_TOK_GREATER                                                        = 1746;  // <gt> ::= 'TOK_GREATER'
		final int PROD_GT_GREATER                                                            = 1747;  // <gt> ::= GREATER
		final int PROD_LT_TOK_LESS                                                           = 1748;  // <lt> ::= 'TOK_LESS'
		final int PROD_LT_LESS                                                               = 1749;  // <lt> ::= LESS
		final int PROD_GE_GREATER_OR_EQUAL                                                   = 1750;  // <ge> ::= 'GREATER_OR_EQUAL'
		final int PROD_LE_LESS_OR_EQUAL                                                      = 1751;  // <le> ::= 'LESS_OR_EQUAL'
//		final int PROD_EXP_LIST                                                              = 1752;  // <exp_list> ::= <exp>
//		final int PROD_EXP_LIST2                                                             = 1753;  // <exp_list> ::= <exp_list> <_e_sep> <exp>
//		final int PROD__E_SEP                                                                = 1754;  // <_e_sep> ::=
//		final int PROD__E_SEP_COMMA_DELIM                                                    = 1755;  // <_e_sep> ::= 'COMMA_DELIM'
//		final int PROD__E_SEP_SEMI_COLON                                                     = 1756;  // <_e_sep> ::= 'SEMI_COLON'
//		final int PROD_EXP_TOK_PLUS                                                          = 1757;  // <exp> ::= <exp> 'TOK_PLUS' <exp_term>
//		final int PROD_EXP_TOK_MINUS                                                         = 1758;  // <exp> ::= <exp> 'TOK_MINUS' <exp_term>
//		final int PROD_EXP                                                                   = 1759;  // <exp> ::= <exp_term>
//		final int PROD_EXP_TERM_TOK_MUL                                                      = 1760;  // <exp_term> ::= <exp_term> 'TOK_MUL' <exp_factor>
//		final int PROD_EXP_TERM_TOK_DIV                                                      = 1761;  // <exp_term> ::= <exp_term> 'TOK_DIV' <exp_factor>
//		final int PROD_EXP_TERM                                                              = 1762;  // <exp_term> ::= <exp_factor>
		final int PROD_EXP_FACTOR_EXPONENTIATION                                             = 1763;  // <exp_factor> ::= <exp_unary> EXPONENTIATION <exp_factor>
//		final int PROD_EXP_FACTOR                                                            = 1764;  // <exp_factor> ::= <exp_unary>
//		final int PROD_EXP_UNARY_TOK_PLUS                                                    = 1765;  // <exp_unary> ::= 'TOK_PLUS' <exp_atom>
//		final int PROD_EXP_UNARY_TOK_MINUS                                                   = 1766;  // <exp_unary> ::= 'TOK_MINUS' <exp_atom>
//		final int PROD_EXP_UNARY                                                             = 1767;  // <exp_unary> ::= <exp_atom>
//		final int PROD_EXP_ATOM_TOK_OPEN_PAREN_TOK_CLOSE_PAREN                               = 1768;  // <exp_atom> ::= 'TOK_OPEN_PAREN' <exp> 'TOK_CLOSE_PAREN'
//		final int PROD_EXP_ATOM                                                              = 1769;  // <exp_atom> ::= <arith_x>
//		final int PROD_LINE_LINAGE_PAGE_COUNTER_LINAGE_COUNTER                               = 1770;  // <line_linage_page_counter> ::= 'LINAGE_COUNTER'
//		final int PROD_LINE_LINAGE_PAGE_COUNTER_LINAGE_COUNTER2                              = 1771;  // <line_linage_page_counter> ::= 'LINAGE_COUNTER' <in_of> <WORD>
//		final int PROD_LINE_LINAGE_PAGE_COUNTER_LINE_COUNTER                                 = 1772;  // <line_linage_page_counter> ::= 'LINE_COUNTER'
//		final int PROD_LINE_LINAGE_PAGE_COUNTER_LINE_COUNTER2                                = 1773;  // <line_linage_page_counter> ::= 'LINE_COUNTER' <in_of> <WORD>
//		final int PROD_LINE_LINAGE_PAGE_COUNTER_PAGE_COUNTER                                 = 1774;  // <line_linage_page_counter> ::= 'PAGE_COUNTER'
//		final int PROD_LINE_LINAGE_PAGE_COUNTER_PAGE_COUNTER2                                = 1775;  // <line_linage_page_counter> ::= 'PAGE_COUNTER' <in_of> <WORD>
//		final int PROD_ARITHMETIC_X_LIST                                                     = 1776;  // <arithmetic_x_list> ::= <arithmetic_x>
//		final int PROD_ARITHMETIC_X_LIST2                                                    = 1777;  // <arithmetic_x_list> ::= <arithmetic_x_list> <arithmetic_x>
//		final int PROD_ARITHMETIC_X                                                          = 1778;  // <arithmetic_x> ::= <target_x> <flag_rounded>
//		final int PROD_RECORD_NAME                                                           = 1779;  // <record_name> ::= <qualified_word>
//		final int PROD_FILE_OR_RECORD_NAME                                                   = 1780;  // <file_or_record_name> ::= <record_name>
		final int PROD_FILE_OR_RECORD_NAME_TOK_FILE                                          = 1781;  // <file_or_record_name> ::= 'TOK_FILE' <WORD>
//		final int PROD_TABLE_NAME                                                            = 1782;  // <table_name> ::= <qualified_word>
//		final int PROD_FILE_NAME_LIST                                                        = 1783;  // <file_name_list> ::= <file_name>
//		final int PROD_FILE_NAME_LIST2                                                       = 1784;  // <file_name_list> ::= <file_name_list> <file_name>
//		final int PROD_FILE_NAME                                                             = 1785;  // <file_name> ::= <WORD>
//		final int PROD_CD_NAME                                                               = 1786;  // <cd_name> ::= <WORD>
//		final int PROD_REPORT_NAME                                                           = 1787;  // <report_name> ::= <WORD>
//		final int PROD_MNEMONIC_NAME_LIST                                                    = 1788;  // <mnemonic_name_list> ::= <mnemonic_name>
//		final int PROD_MNEMONIC_NAME_LIST2                                                   = 1789;  // <mnemonic_name_list> ::= <mnemonic_name_list> <mnemonic_name>
//		final int PROD_MNEMONIC_NAME                                                         = 1790;  // <mnemonic_name> ::= <MNEMONIC_NAME_TOK>
//		final int PROD_PROCEDURE_NAME_LIST                                                   = 1791;  // <procedure_name_list> ::=
//		final int PROD_PROCEDURE_NAME_LIST2                                                  = 1792;  // <procedure_name_list> ::= <procedure_name_list> <procedure_name>
//		final int PROD_PROCEDURE_NAME                                                        = 1793;  // <procedure_name> ::= <label>
//		final int PROD_LABEL                                                                 = 1794;  // <label> ::= <qualified_word>
//		final int PROD_LABEL2                                                                = 1795;  // <label> ::= <integer_label>
//		final int PROD_LABEL3                                                                = 1796;  // <label> ::= <integer_label> <in_of> <integer_label>
//		final int PROD_INTEGER_LABEL                                                         = 1797;  // <integer_label> ::= <LITERAL_TOK>
//		final int PROD_REFERENCE_LIST                                                        = 1798;  // <reference_list> ::= <reference>
//		final int PROD_REFERENCE_LIST2                                                       = 1799;  // <reference_list> ::= <reference_list> <reference>
//		final int PROD_REFERENCE                                                             = 1800;  // <reference> ::= <qualified_word>
//		final int PROD_SINGLE_REFERENCE                                                      = 1801;  // <single_reference> ::= <WORD>
//		final int PROD_OPTIONAL_REFERENCE_LIST                                               = 1802;  // <optional_reference_list> ::= <optional_reference>
//		final int PROD_OPTIONAL_REFERENCE_LIST2                                              = 1803;  // <optional_reference_list> ::= <optional_reference_list> <optional_reference>
//		final int PROD_OPTIONAL_REFERENCE                                                    = 1804;  // <optional_reference> ::= <WORD>
//		final int PROD_REFERENCE_OR_LITERAL                                                  = 1805;  // <reference_or_literal> ::= <reference>
//		final int PROD_REFERENCE_OR_LITERAL2                                                 = 1806;  // <reference_or_literal> ::= <LITERAL_TOK>
//		final int PROD_UNDEFINED_WORD                                                        = 1807;  // <undefined_word> ::= <WORD>
//		final int PROD_UNIQUE_WORD                                                           = 1808;  // <unique_word> ::= <WORD>
//		final int PROD_TARGET_X_LIST                                                         = 1809;  // <target_x_list> ::= <target_x>
//		final int PROD_TARGET_X_LIST2                                                        = 1810;  // <target_x_list> ::= <target_x_list> <target_x>
//		final int PROD_TARGET_X                                                              = 1811;  // <target_x> ::= <target_identifier>
//		final int PROD_TARGET_X2                                                             = 1812;  // <target_x> ::= <basic_literal>
//		final int PROD_TARGET_X_ADDRESS                                                      = 1813;  // <target_x> ::= ADDRESS <_of> <identifier_1>
		final int PROD_TARGET_X_COMMA_DELIM                                                  = 1814;  // <target_x> ::= 'COMMA_DELIM'
//		final int PROD__X_LIST                                                               = 1815;  // <_x_list> ::=
//		final int PROD__X_LIST2                                                              = 1816;  // <_x_list> ::= <x_list>
//		final int PROD_X_LIST                                                                = 1817;  // <x_list> ::= <x>
//		final int PROD_X_LIST2                                                               = 1818;  // <x_list> ::= <x_list> <x>
//		final int PROD_X                                                                     = 1819;  // <x> ::= <identifier>
//		final int PROD_X2                                                                    = 1820;  // <x> ::= <x_common>
		final int PROD_X_COMMA_DELIM                                                         = 1821;  // <x> ::= 'COMMA_DELIM'
//		final int PROD_CALL_X                                                                = 1822;  // <call_x> ::= <identifier_or_file_name>
//		final int PROD_CALL_X2                                                               = 1823;  // <call_x> ::= <x_common>
//		final int PROD_X_COMMON                                                              = 1824;  // <x_common> ::= <literal>
//		final int PROD_X_COMMON2                                                             = 1825;  // <x_common> ::= <function>
//		final int PROD_X_COMMON3                                                             = 1826;  // <x_common> ::= <line_linage_page_counter>
//		final int PROD_X_COMMON_LENGTH_OF                                                    = 1827;  // <x_common> ::= 'LENGTH_OF' <identifier_1>
//		final int PROD_X_COMMON_LENGTH_OF2                                                   = 1828;  // <x_common> ::= 'LENGTH_OF' <basic_literal>
//		final int PROD_X_COMMON_LENGTH_OF3                                                   = 1829;  // <x_common> ::= 'LENGTH_OF' <function>
//		final int PROD_X_COMMON_ADDRESS                                                      = 1830;  // <x_common> ::= ADDRESS <_of> <prog_or_entry> <alnum_or_id>
//		final int PROD_X_COMMON_ADDRESS2                                                     = 1831;  // <x_common> ::= ADDRESS <_of> <identifier_1>
//		final int PROD_X_COMMON4                                                             = 1832;  // <x_common> ::= <MNEMONIC_NAME_TOK>
//		final int PROD_REPORT_X_LIST                                                         = 1833;  // <report_x_list> ::= <arith_x>
//		final int PROD_REPORT_X_LIST2                                                        = 1834;  // <report_x_list> ::= <report_x_list> <arith_x>
//		final int PROD_EXPR_X                                                                = 1835;  // <expr_x> ::= <identifier>
//		final int PROD_EXPR_X2                                                               = 1836;  // <expr_x> ::= <basic_literal>
//		final int PROD_EXPR_X3                                                               = 1837;  // <expr_x> ::= <function>
//		final int PROD_ARITH_X                                                               = 1838;  // <arith_x> ::= <identifier>
//		final int PROD_ARITH_X2                                                              = 1839;  // <arith_x> ::= <basic_literal>
//		final int PROD_ARITH_X3                                                              = 1840;  // <arith_x> ::= <function>
//		final int PROD_ARITH_X4                                                              = 1841;  // <arith_x> ::= <line_linage_page_counter>
//		final int PROD_ARITH_X_LENGTH_OF                                                     = 1842;  // <arith_x> ::= 'LENGTH_OF' <identifier_1>
//		final int PROD_ARITH_X_LENGTH_OF2                                                    = 1843;  // <arith_x> ::= 'LENGTH_OF' <basic_literal>
//		final int PROD_ARITH_X_LENGTH_OF3                                                    = 1844;  // <arith_x> ::= 'LENGTH_OF' <function>
//		final int PROD_PROG_OR_ENTRY_PROGRAM                                                 = 1845;  // <prog_or_entry> ::= PROGRAM
//		final int PROD_PROG_OR_ENTRY_ENTRY                                                   = 1846;  // <prog_or_entry> ::= ENTRY
//		final int PROD_ALNUM_OR_ID                                                           = 1847;  // <alnum_or_id> ::= <identifier_1>
//		final int PROD_ALNUM_OR_ID2                                                          = 1848;  // <alnum_or_id> ::= <LITERAL_TOK>
//		final int PROD_SIMPLE_DISPLAY_VALUE                                                  = 1849;  // <simple_display_value> ::= <simple_value>
//		final int PROD_SIMPLE_DISPLAY_ALL_VALUE                                              = 1850;  // <simple_display_all_value> ::= <simple_all_value>
//		final int PROD_SIMPLE_VALUE                                                          = 1851;  // <simple_value> ::= <identifier>
//		final int PROD_SIMPLE_VALUE2                                                         = 1852;  // <simple_value> ::= <basic_literal>
//		final int PROD_SIMPLE_VALUE3                                                         = 1853;  // <simple_value> ::= <function>
//		final int PROD_SIMPLE_ALL_VALUE                                                      = 1854;  // <simple_all_value> ::= <identifier>
//		final int PROD_SIMPLE_ALL_VALUE2                                                     = 1855;  // <simple_all_value> ::= <literal>
//		final int PROD_ID_OR_LIT                                                             = 1856;  // <id_or_lit> ::= <identifier>
//		final int PROD_ID_OR_LIT2                                                            = 1857;  // <id_or_lit> ::= <LITERAL_TOK>
//		final int PROD_ID_OR_LIT_OR_FUNC                                                     = 1858;  // <id_or_lit_or_func> ::= <identifier>
//		final int PROD_ID_OR_LIT_OR_FUNC2                                                    = 1859;  // <id_or_lit_or_func> ::= <LITERAL_TOK>
//		final int PROD_ID_OR_LIT_OR_FUNC3                                                    = 1860;  // <id_or_lit_or_func> ::= <function>
//		final int PROD_ID_OR_LIT_OR_LENGTH_OR_FUNC                                           = 1861;  // <id_or_lit_or_length_or_func> ::= <identifier>
//		final int PROD_ID_OR_LIT_OR_LENGTH_OR_FUNC2                                          = 1862;  // <id_or_lit_or_length_or_func> ::= <lit_or_length>
//		final int PROD_ID_OR_LIT_OR_LENGTH_OR_FUNC3                                          = 1863;  // <id_or_lit_or_length_or_func> ::= <function>
//		final int PROD_NUM_ID_OR_LIT                                                         = 1864;  // <num_id_or_lit> ::= <sub_identifier>
//		final int PROD_NUM_ID_OR_LIT2                                                        = 1865;  // <num_id_or_lit> ::= <integer>
//		final int PROD_NUM_ID_OR_LIT_ZERO                                                    = 1866;  // <num_id_or_lit> ::= ZERO
//		final int PROD_POSITIVE_ID_OR_LIT                                                    = 1867;  // <positive_id_or_lit> ::= <sub_identifier>
//		final int PROD_POSITIVE_ID_OR_LIT2                                                   = 1868;  // <positive_id_or_lit> ::= <report_integer>
//		final int PROD_POS_NUM_ID_OR_LIT                                                     = 1869;  // <pos_num_id_or_lit> ::= <sub_identifier>
//		final int PROD_POS_NUM_ID_OR_LIT2                                                    = 1870;  // <pos_num_id_or_lit> ::= <integer>
//		final int PROD_FROM_PARAMETER                                                        = 1871;  // <from_parameter> ::= <identifier>
//		final int PROD_FROM_PARAMETER2                                                       = 1872;  // <from_parameter> ::= <literal>
//		final int PROD_FROM_PARAMETER3                                                       = 1873;  // <from_parameter> ::= <function>
//		final int PROD_SUB_IDENTIFIER                                                        = 1874;  // <sub_identifier> ::= <sub_identifier_1>
//		final int PROD_TABLE_IDENTIFIER                                                      = 1875;  // <table_identifier> ::= <sub_identifier_1>
//		final int PROD_SUB_IDENTIFIER_1                                                      = 1876;  // <sub_identifier_1> ::= <qualified_word>
		final int PROD_SUB_IDENTIFIER_12                                                     = 1877;  // <sub_identifier_1> ::= <qualified_word> <subref>
//		final int PROD_DISPLAY_IDENTIFIER                                                    = 1878;  // <display_identifier> ::= <identifier>
//		final int PROD_NUMERIC_IDENTIFIER                                                    = 1879;  // <numeric_identifier> ::= <identifier>
//		final int PROD_IDENTIFIER_OR_FILE_NAME                                               = 1880;  // <identifier_or_file_name> ::= <identifier_1>
//		final int PROD_IDENTIFIER                                                            = 1881;  // <identifier> ::= <identifier_1>
		final int PROD_IDENTIFIER_1                                                          = 1882;  // <identifier_1> ::= <qualified_word> <subref> <refmod>
		final int PROD_IDENTIFIER_12                                                         = 1883;  // <identifier_1> ::= <qualified_word> <subref>
		final int PROD_IDENTIFIER_13                                                         = 1884;  // <identifier_1> ::= <qualified_word> <refmod>
//		final int PROD_IDENTIFIER_14                                                         = 1885;  // <identifier_1> ::= <qualified_word>
//		final int PROD_TARGET_IDENTIFIER                                                     = 1886;  // <target_identifier> ::= <target_identifier_1>
		final int PROD_TARGET_IDENTIFIER_1                                                   = 1887;  // <target_identifier_1> ::= <qualified_word> <subref> <refmod>
		final int PROD_TARGET_IDENTIFIER_12                                                  = 1888;  // <target_identifier_1> ::= <qualified_word> <subref>
		final int PROD_TARGET_IDENTIFIER_13                                                  = 1889;  // <target_identifier_1> ::= <qualified_word> <refmod>
//		final int PROD_TARGET_IDENTIFIER_14                                                  = 1890;  // <target_identifier_1> ::= <qualified_word>
//		final int PROD_QUALIFIED_WORD                                                        = 1891;  // <qualified_word> ::= <WORD>
		final int PROD_QUALIFIED_WORD2                                                       = 1892;  // <qualified_word> ::= <WORD> <in_of> <qualified_word>
		final int PROD_SUBREF_TOK_OPEN_PAREN_TOK_CLOSE_PAREN                                 = 1893;  // <subref> ::= 'TOK_OPEN_PAREN' <exp_list> 'TOK_CLOSE_PAREN'
//		final int PROD_REFMOD_TOK_OPEN_PAREN_TOK_COLON_TOK_CLOSE_PAREN                       = 1894;  // <refmod> ::= 'TOK_OPEN_PAREN' <exp> 'TOK_COLON' 'TOK_CLOSE_PAREN'
//		final int PROD_REFMOD_TOK_OPEN_PAREN_TOK_COLON_TOK_CLOSE_PAREN2                      = 1895;  // <refmod> ::= 'TOK_OPEN_PAREN' <exp> 'TOK_COLON' <exp> 'TOK_CLOSE_PAREN'
//		final int PROD_INTEGER_INTLITERAL                                                    = 1896;  // <integer> ::= IntLiteral
//		final int PROD_SYMBOLIC_INTEGER_INTLITERAL                                           = 1897;  // <symbolic_integer> ::= IntLiteral
//		final int PROD_REPORT_INTEGER_INTLITERAL                                             = 1898;  // <report_integer> ::= IntLiteral
//		final int PROD_CLASS_VALUE                                                           = 1899;  // <class_value> ::= <LITERAL_TOK>
//		final int PROD_CLASS_VALUE_SPACE                                                     = 1900;  // <class_value> ::= SPACE
//		final int PROD_CLASS_VALUE_ZERO                                                      = 1901;  // <class_value> ::= ZERO
//		final int PROD_CLASS_VALUE_QUOTE                                                     = 1902;  // <class_value> ::= QUOTE
//		final int PROD_CLASS_VALUE_HIGH_VALUE                                                = 1903;  // <class_value> ::= 'HIGH_VALUE'
//		final int PROD_CLASS_VALUE_LOW_VALUE                                                 = 1904;  // <class_value> ::= 'LOW_VALUE'
//		final int PROD_CLASS_VALUE_TOK_NULL                                                  = 1905;  // <class_value> ::= 'TOK_NULL'
//		final int PROD_LITERAL                                                               = 1906;  // <literal> ::= <basic_literal>
//		final int PROD_LITERAL_ALL                                                           = 1907;  // <literal> ::= ALL <basic_value>
//		final int PROD_BASIC_LITERAL                                                         = 1908;  // <basic_literal> ::= <basic_value>
//		final int PROD_BASIC_LITERAL_TOK_AMPER                                               = 1909;  // <basic_literal> ::= <basic_literal> 'TOK_AMPER' <basic_value>
//		final int PROD_BASIC_VALUE                                                           = 1910;  // <basic_value> ::= <LITERAL_TOK>
//		final int PROD_BASIC_VALUE_SPACE                                                     = 1911;  // <basic_value> ::= SPACE
//		final int PROD_BASIC_VALUE_ZERO                                                      = 1912;  // <basic_value> ::= ZERO
//		final int PROD_BASIC_VALUE_QUOTE                                                     = 1913;  // <basic_value> ::= QUOTE
//		final int PROD_BASIC_VALUE_HIGH_VALUE                                                = 1914;  // <basic_value> ::= 'HIGH_VALUE'
//		final int PROD_BASIC_VALUE_LOW_VALUE                                                 = 1915;  // <basic_value> ::= 'LOW_VALUE'
//		final int PROD_BASIC_VALUE_TOK_NULL                                                  = 1916;  // <basic_value> ::= 'TOK_NULL'
		final int PROD_FUNCTION                                                              = 1917;  // <function> ::= <func_no_parm> <func_refmod>
//		final int PROD_FUNCTION_TOK_OPEN_PAREN_TOK_CLOSE_PAREN                               = 1918;  // <function> ::= <func_one_parm> 'TOK_OPEN_PAREN' <expr_x> 'TOK_CLOSE_PAREN' <func_refmod>
//		final int PROD_FUNCTION_TOK_OPEN_PAREN_TOK_CLOSE_PAREN2                              = 1919;  // <function> ::= <func_multi_parm> 'TOK_OPEN_PAREN' <exp_list> 'TOK_CLOSE_PAREN' <func_refmod>
//		final int PROD_FUNCTION_TOK_OPEN_PAREN_TOK_CLOSE_PAREN3                              = 1920;  // <function> ::= <TRIM_FUNC> 'TOK_OPEN_PAREN' <trim_args> 'TOK_CLOSE_PAREN' <func_refmod>
//		final int PROD_FUNCTION_TOK_OPEN_PAREN_TOK_CLOSE_PAREN4                              = 1921;  // <function> ::= <LENGTH_FUNC> 'TOK_OPEN_PAREN' <length_arg> 'TOK_CLOSE_PAREN'
//		final int PROD_FUNCTION_TOK_OPEN_PAREN_TOK_CLOSE_PAREN5                              = 1922;  // <function> ::= <NUMVALC_FUNC> 'TOK_OPEN_PAREN' <numvalc_args> 'TOK_CLOSE_PAREN'
//		final int PROD_FUNCTION_TOK_OPEN_PAREN_TOK_CLOSE_PAREN6                              = 1923;  // <function> ::= <LOCALE_DATE_FUNC> 'TOK_OPEN_PAREN' <locale_dt_args> 'TOK_CLOSE_PAREN' <func_refmod>
//		final int PROD_FUNCTION_TOK_OPEN_PAREN_TOK_CLOSE_PAREN7                              = 1924;  // <function> ::= <LOCALE_TIME_FUNC> 'TOK_OPEN_PAREN' <locale_dt_args> 'TOK_CLOSE_PAREN' <func_refmod>
//		final int PROD_FUNCTION_TOK_OPEN_PAREN_TOK_CLOSE_PAREN8                              = 1925;  // <function> ::= <LOCALE_TIME_FROM_FUNC> 'TOK_OPEN_PAREN' <locale_dt_args> 'TOK_CLOSE_PAREN' <func_refmod>
//		final int PROD_FUNCTION_TOK_OPEN_PAREN_TOK_CLOSE_PAREN9                              = 1926;  // <function> ::= <FORMATTED_DATETIME_FUNC> 'TOK_OPEN_PAREN' <formatted_datetime_args> 'TOK_CLOSE_PAREN' <func_refmod>
//		final int PROD_FUNCTION_TOK_OPEN_PAREN_TOK_CLOSE_PAREN10                             = 1927;  // <function> ::= <FORMATTED_TIME_FUNC> 'TOK_OPEN_PAREN' <formatted_time_args> 'TOK_CLOSE_PAREN' <func_refmod>
//		final int PROD_FUNCTION2                                                             = 1928;  // <function> ::= <FUNCTION_NAME> <func_args>
//		final int PROD_FUNCTION3                                                             = 1929;  // <function> ::= <USER_FUNCTION_NAME> <func_args>
//		final int PROD_FUNCTION4                                                             = 1930;  // <function> ::= <DISPLAY_OF_FUNC> <func_args>
//		final int PROD_FUNCTION5                                                             = 1931;  // <function> ::= <NATIONAL_OF_FUNC> <func_args>
//		final int PROD_FUNC_NO_PARM                                                          = 1932;  // <func_no_parm> ::= <CURRENT_DATE_FUNC>
//		final int PROD_FUNC_NO_PARM2                                                         = 1933;  // <func_no_parm> ::= <WHEN_COMPILED_FUNC>
//		final int PROD_FUNC_ONE_PARM                                                         = 1934;  // <func_one_parm> ::= <UPPER_CASE_FUNC>
//		final int PROD_FUNC_ONE_PARM2                                                        = 1935;  // <func_one_parm> ::= <LOWER_CASE_FUNC>
//		final int PROD_FUNC_ONE_PARM3                                                        = 1936;  // <func_one_parm> ::= <REVERSE_FUNC>
//		final int PROD_FUNC_MULTI_PARM                                                       = 1937;  // <func_multi_parm> ::= <CONCATENATE_FUNC>
//		final int PROD_FUNC_MULTI_PARM2                                                      = 1938;  // <func_multi_parm> ::= <FORMATTED_DATE_FUNC>
//		final int PROD_FUNC_MULTI_PARM3                                                      = 1939;  // <func_multi_parm> ::= <SUBSTITUTE_FUNC>
//		final int PROD_FUNC_MULTI_PARM4                                                      = 1940;  // <func_multi_parm> ::= <SUBSTITUTE_CASE_FUNC>
//		final int PROD_FUNC_REFMOD                                                           = 1941;  // <func_refmod> ::=
//		final int PROD_FUNC_REFMOD_TOK_OPEN_PAREN_TOK_COLON_TOK_CLOSE_PAREN                  = 1942;  // <func_refmod> ::= 'TOK_OPEN_PAREN' <exp> 'TOK_COLON' 'TOK_CLOSE_PAREN'
//		final int PROD_FUNC_REFMOD_TOK_OPEN_PAREN_TOK_COLON_TOK_CLOSE_PAREN2                 = 1943;  // <func_refmod> ::= 'TOK_OPEN_PAREN' <exp> 'TOK_COLON' <exp> 'TOK_CLOSE_PAREN'
//		final int PROD_FUNC_ARGS                                                             = 1944;  // <func_args> ::=
//		final int PROD_FUNC_ARGS_TOK_OPEN_PAREN_TOK_CLOSE_PAREN                              = 1945;  // <func_args> ::= 'TOK_OPEN_PAREN' <exp_list> 'TOK_CLOSE_PAREN'
//		final int PROD_FUNC_ARGS_TOK_OPEN_PAREN_TOK_CLOSE_PAREN2                             = 1946;  // <func_args> ::= 'TOK_OPEN_PAREN' 'TOK_CLOSE_PAREN'
//		final int PROD_TRIM_ARGS                                                             = 1947;  // <trim_args> ::= <expr_x>
//		final int PROD_TRIM_ARGS_LEADING                                                     = 1948;  // <trim_args> ::= <expr_x> <_e_sep> LEADING
//		final int PROD_TRIM_ARGS_TRAILING                                                    = 1949;  // <trim_args> ::= <expr_x> <_e_sep> TRAILING
//		final int PROD_LENGTH_ARG                                                            = 1950;  // <length_arg> ::= <expr_x>
//		final int PROD_NUMVALC_ARGS                                                          = 1951;  // <numvalc_args> ::= <expr_x>
//		final int PROD_NUMVALC_ARGS2                                                         = 1952;  // <numvalc_args> ::= <expr_x> <_e_sep> <expr_x>
//		final int PROD_LOCALE_DT_ARGS                                                        = 1953;  // <locale_dt_args> ::= <exp>
//		final int PROD_LOCALE_DT_ARGS2                                                       = 1954;  // <locale_dt_args> ::= <exp> <_e_sep> <reference>
//		final int PROD_FORMATTED_DATETIME_ARGS                                               = 1955;  // <formatted_datetime_args> ::= <exp_list>
//		final int PROD_FORMATTED_DATETIME_ARGS_SYSTEM_OFFSET                                 = 1956;  // <formatted_datetime_args> ::= <exp_list> <_e_sep> 'SYSTEM_OFFSET'
//		final int PROD_FORMATTED_TIME_ARGS                                                   = 1957;  // <formatted_time_args> ::= <exp_list>
//		final int PROD_FORMATTED_TIME_ARGS_SYSTEM_OFFSET                                     = 1958;  // <formatted_time_args> ::= <exp_list> <_e_sep> 'SYSTEM_OFFSET'
//		final int PROD_FLAG_ALL                                                              = 1959;  // <flag_all> ::=
//		final int PROD_FLAG_ALL_ALL                                                          = 1960;  // <flag_all> ::= ALL
//		final int PROD_FLAG_DUPLICATES                                                       = 1961;  // <flag_duplicates> ::=
//		final int PROD_FLAG_DUPLICATES2                                                      = 1962;  // <flag_duplicates> ::= <with_dups>
//		final int PROD_FLAG_INITIALIZED                                                      = 1963;  // <flag_initialized> ::=
//		final int PROD_FLAG_INITIALIZED_INITIALIZED                                          = 1964;  // <flag_initialized> ::= INITIALIZED
//		final int PROD_FLAG_INITIALIZED_TO                                                   = 1965;  // <flag_initialized_to> ::=
//		final int PROD_FLAG_INITIALIZED_TO_INITIALIZED                                       = 1966;  // <flag_initialized_to> ::= INITIALIZED <to_init_val>
//		final int PROD_TO_INIT_VAL                                                           = 1967;  // <to_init_val> ::=
//		final int PROD_TO_INIT_VAL_TO                                                        = 1968;  // <to_init_val> ::= TO <simple_all_value>
//		final int PROD__FLAG_NEXT                                                            = 1969;  // <_flag_next> ::=
//		final int PROD__FLAG_NEXT_NEXT                                                       = 1970;  // <_flag_next> ::= NEXT
		final int PROD__FLAG_NEXT_PREVIOUS                                                   = 1971;  // <_flag_next> ::= PREVIOUS
//		final int PROD__FLAG_NOT                                                             = 1972;  // <_flag_not> ::=
//		final int PROD__FLAG_NOT_NOT                                                         = 1973;  // <_flag_not> ::= NOT
//		final int PROD_FLAG_OPTIONAL                                                         = 1974;  // <flag_optional> ::=
//		final int PROD_FLAG_OPTIONAL_OPTIONAL                                                = 1975;  // <flag_optional> ::= OPTIONAL
//		final int PROD_FLAG_OPTIONAL_NOT_OPTIONAL                                            = 1976;  // <flag_optional> ::= NOT OPTIONAL
//		final int PROD_FLAG_ROUNDED                                                          = 1977;  // <flag_rounded> ::=
//		final int PROD_FLAG_ROUNDED_ROUNDED                                                  = 1978;  // <flag_rounded> ::= ROUNDED <round_mode>
//		final int PROD_ROUND_MODE                                                            = 1979;  // <round_mode> ::=
//		final int PROD_ROUND_MODE_MODE                                                       = 1980;  // <round_mode> ::= MODE <_is> <round_choice>
//		final int PROD_ROUND_CHOICE_AWAY_FROM_ZERO                                           = 1981;  // <round_choice> ::= 'AWAY_FROM_ZERO'
//		final int PROD_ROUND_CHOICE_NEAREST_AWAY_FROM_ZERO                                   = 1982;  // <round_choice> ::= 'NEAREST_AWAY_FROM_ZERO'
//		final int PROD_ROUND_CHOICE_NEAREST_EVEN                                             = 1983;  // <round_choice> ::= 'NEAREST_EVEN'
//		final int PROD_ROUND_CHOICE_NEAREST_TOWARD_ZERO                                      = 1984;  // <round_choice> ::= 'NEAREST_TOWARD_ZERO'
//		final int PROD_ROUND_CHOICE_PROHIBITED                                               = 1985;  // <round_choice> ::= PROHIBITED
//		final int PROD_ROUND_CHOICE_TOWARD_GREATER                                           = 1986;  // <round_choice> ::= 'TOWARD_GREATER'
//		final int PROD_ROUND_CHOICE_TOWARD_LESSER                                            = 1987;  // <round_choice> ::= 'TOWARD_LESSER'
//		final int PROD_ROUND_CHOICE_TRUNCATION                                               = 1988;  // <round_choice> ::= TRUNCATION
//		final int PROD_FLAG_SEPARATE                                                         = 1989;  // <flag_separate> ::=
//		final int PROD_FLAG_SEPARATE_SEPARATE                                                = 1990;  // <flag_separate> ::= SEPARATE <_character>
//		final int PROD__FROM_IDX_TO_IDX                                                      = 1991;  // <_from_idx_to_idx> ::=
//		final int PROD__FROM_IDX_TO_IDX_FROM_TO                                              = 1992;  // <_from_idx_to_idx> ::= FROM <_index> <pos_num_id_or_lit> TO <pos_num_id_or_lit>
//		final int PROD__DEST_INDEX                                                           = 1993;  // <_dest_index> ::=
//		final int PROD__DEST_INDEX_DESTINATION                                               = 1994;  // <_dest_index> ::= DESTINATION <_index> <pos_num_id_or_lit>
//		final int PROD_ERROR_STMT_RECOVER_TOK_DOT                                            = 1995;  // <error_stmt_recover> ::= 'TOK_DOT'
//		final int PROD_ERROR_STMT_RECOVER                                                    = 1996;  // <error_stmt_recover> ::= <verb>
//		final int PROD_ERROR_STMT_RECOVER2                                                   = 1997;  // <error_stmt_recover> ::= <scope_terminator>
//		final int PROD_VERB_ACCEPT                                                           = 1998;  // <verb> ::= ACCEPT
//		final int PROD_VERB_ADD                                                              = 1999;  // <verb> ::= ADD
//		final int PROD_VERB_ALLOCATE                                                         = 2000;  // <verb> ::= ALLOCATE
//		final int PROD_VERB_ALTER                                                            = 2001;  // <verb> ::= ALTER
//		final int PROD_VERB_CALL                                                             = 2002;  // <verb> ::= CALL
//		final int PROD_VERB_CANCEL                                                           = 2003;  // <verb> ::= CANCEL
//		final int PROD_VERB_CLOSE                                                            = 2004;  // <verb> ::= CLOSE
//		final int PROD_VERB_COMMIT                                                           = 2005;  // <verb> ::= COMMIT
//		final int PROD_VERB_COMPUTE                                                          = 2006;  // <verb> ::= COMPUTE
//		final int PROD_VERB_CONTINUE                                                         = 2007;  // <verb> ::= CONTINUE
//		final int PROD_VERB_DELETE                                                           = 2008;  // <verb> ::= DELETE
//		final int PROD_VERB_DISPLAY                                                          = 2009;  // <verb> ::= DISPLAY
//		final int PROD_VERB_DIVIDE                                                           = 2010;  // <verb> ::= DIVIDE
//		final int PROD_VERB_ELSE                                                             = 2011;  // <verb> ::= ELSE
//		final int PROD_VERB_ENTRY                                                            = 2012;  // <verb> ::= ENTRY
//		final int PROD_VERB_EVALUATE                                                         = 2013;  // <verb> ::= EVALUATE
//		final int PROD_VERB_EXIT                                                             = 2014;  // <verb> ::= EXIT
//		final int PROD_VERB_FREE                                                             = 2015;  // <verb> ::= FREE
//		final int PROD_VERB_GENERATE                                                         = 2016;  // <verb> ::= GENERATE
//		final int PROD_VERB_GO                                                               = 2017;  // <verb> ::= GO
//		final int PROD_VERB_GOBACK                                                           = 2018;  // <verb> ::= GOBACK
//		final int PROD_VERB_IF                                                               = 2019;  // <verb> ::= IF
//		final int PROD_VERB_INITIALIZE                                                       = 2020;  // <verb> ::= INITIALIZE
//		final int PROD_VERB_INITIATE                                                         = 2021;  // <verb> ::= INITIATE
//		final int PROD_VERB_INSPECT                                                          = 2022;  // <verb> ::= INSPECT
//		final int PROD_VERB_MERGE                                                            = 2023;  // <verb> ::= MERGE
//		final int PROD_VERB_MOVE                                                             = 2024;  // <verb> ::= MOVE
//		final int PROD_VERB_MULTIPLY                                                         = 2025;  // <verb> ::= MULTIPLY
//		final int PROD_VERB_NEXT                                                             = 2026;  // <verb> ::= NEXT
//		final int PROD_VERB_OPEN                                                             = 2027;  // <verb> ::= OPEN
//		final int PROD_VERB_PERFORM                                                          = 2028;  // <verb> ::= PERFORM
//		final int PROD_VERB_READ                                                             = 2029;  // <verb> ::= READ
//		final int PROD_VERB_RELEASE                                                          = 2030;  // <verb> ::= RELEASE
//		final int PROD_VERB_RETURN                                                           = 2031;  // <verb> ::= RETURN
//		final int PROD_VERB_REWRITE                                                          = 2032;  // <verb> ::= REWRITE
//		final int PROD_VERB_ROLLBACK                                                         = 2033;  // <verb> ::= ROLLBACK
//		final int PROD_VERB_SEARCH                                                           = 2034;  // <verb> ::= SEARCH
//		final int PROD_VERB_SET                                                              = 2035;  // <verb> ::= SET
//		final int PROD_VERB_SORT                                                             = 2036;  // <verb> ::= SORT
//		final int PROD_VERB_START                                                            = 2037;  // <verb> ::= START
//		final int PROD_VERB_STOP                                                             = 2038;  // <verb> ::= STOP
//		final int PROD_VERB_STRING                                                           = 2039;  // <verb> ::= STRING
//		final int PROD_VERB_SUBTRACT                                                         = 2040;  // <verb> ::= SUBTRACT
//		final int PROD_VERB_SUPPRESS                                                         = 2041;  // <verb> ::= SUPPRESS
//		final int PROD_VERB_TERMINATE                                                        = 2042;  // <verb> ::= TERMINATE
//		final int PROD_VERB_TRANSFORM                                                        = 2043;  // <verb> ::= TRANSFORM
//		final int PROD_VERB_UNLOCK                                                           = 2044;  // <verb> ::= UNLOCK
//		final int PROD_VERB_UNSTRING                                                         = 2045;  // <verb> ::= UNSTRING
//		final int PROD_VERB_WRITE                                                            = 2046;  // <verb> ::= WRITE
//		final int PROD_SCOPE_TERMINATOR_END_ACCEPT                                           = 2047;  // <scope_terminator> ::= 'END_ACCEPT'
//		final int PROD_SCOPE_TERMINATOR_END_ADD                                              = 2048;  // <scope_terminator> ::= 'END_ADD'
//		final int PROD_SCOPE_TERMINATOR_END_CALL                                             = 2049;  // <scope_terminator> ::= 'END_CALL'
//		final int PROD_SCOPE_TERMINATOR_END_COMPUTE                                          = 2050;  // <scope_terminator> ::= 'END_COMPUTE'
//		final int PROD_SCOPE_TERMINATOR_END_DELETE                                           = 2051;  // <scope_terminator> ::= 'END_DELETE'
//		final int PROD_SCOPE_TERMINATOR_END_DISPLAY                                          = 2052;  // <scope_terminator> ::= 'END_DISPLAY'
//		final int PROD_SCOPE_TERMINATOR_END_DIVIDE                                           = 2053;  // <scope_terminator> ::= 'END_DIVIDE'
//		final int PROD_SCOPE_TERMINATOR_END_EVALUATE                                         = 2054;  // <scope_terminator> ::= 'END_EVALUATE'
//		final int PROD_SCOPE_TERMINATOR_END_IF                                               = 2055;  // <scope_terminator> ::= 'END_IF'
//		final int PROD_SCOPE_TERMINATOR_END_MULTIPLY                                         = 2056;  // <scope_terminator> ::= 'END_MULTIPLY'
//		final int PROD_SCOPE_TERMINATOR_END_PERFORM                                          = 2057;  // <scope_terminator> ::= 'END_PERFORM'
//		final int PROD_SCOPE_TERMINATOR_END_READ                                             = 2058;  // <scope_terminator> ::= 'END_READ'
//		final int PROD_SCOPE_TERMINATOR_END_RECEIVE                                          = 2059;  // <scope_terminator> ::= 'END_RECEIVE'
//		final int PROD_SCOPE_TERMINATOR_END_RETURN                                           = 2060;  // <scope_terminator> ::= 'END_RETURN'
//		final int PROD_SCOPE_TERMINATOR_END_REWRITE                                          = 2061;  // <scope_terminator> ::= 'END_REWRITE'
//		final int PROD_SCOPE_TERMINATOR_END_SEARCH                                           = 2062;  // <scope_terminator> ::= 'END_SEARCH'
//		final int PROD_SCOPE_TERMINATOR_END_START                                            = 2063;  // <scope_terminator> ::= 'END_START'
//		final int PROD_SCOPE_TERMINATOR_END_STRING                                           = 2064;  // <scope_terminator> ::= 'END_STRING'
//		final int PROD_SCOPE_TERMINATOR_END_SUBTRACT                                         = 2065;  // <scope_terminator> ::= 'END_SUBTRACT'
//		final int PROD_SCOPE_TERMINATOR_END_UNSTRING                                         = 2066;  // <scope_terminator> ::= 'END_UNSTRING'
//		final int PROD_SCOPE_TERMINATOR_END_WRITE                                            = 2067;  // <scope_terminator> ::= 'END_WRITE'
//		final int PROD__ADVANCING                                                            = 2068;  // <_advancing> ::=
//		final int PROD__ADVANCING_ADVANCING                                                  = 2069;  // <_advancing> ::= ADVANCING
//		final int PROD__AFTER                                                                = 2070;  // <_after> ::=
//		final int PROD__AFTER_AFTER                                                          = 2071;  // <_after> ::= AFTER
//		final int PROD__ARE                                                                  = 2072;  // <_are> ::=
//		final int PROD__ARE_ARE                                                              = 2073;  // <_are> ::= ARE
//		final int PROD__AREA                                                                 = 2074;  // <_area> ::=
//		final int PROD__AREA_AREA                                                            = 2075;  // <_area> ::= AREA
//		final int PROD__AREAS                                                                = 2076;  // <_areas> ::=
//		final int PROD__AREAS_AREA                                                           = 2077;  // <_areas> ::= AREA
//		final int PROD__AREAS_AREAS                                                          = 2078;  // <_areas> ::= AREAS
//		final int PROD__AS                                                                   = 2079;  // <_as> ::=
//		final int PROD__AS_AS                                                                = 2080;  // <_as> ::= AS
//		final int PROD__AT                                                                   = 2081;  // <_at> ::=
//		final int PROD__AT_AT                                                                = 2082;  // <_at> ::= AT
//		final int PROD__BEFORE                                                               = 2083;  // <_before> ::=
//		final int PROD__BEFORE_BEFORE                                                        = 2084;  // <_before> ::= BEFORE
//		final int PROD__BINARY                                                               = 2085;  // <_binary> ::=
//		final int PROD__BINARY_BINARY                                                        = 2086;  // <_binary> ::= BINARY
//		final int PROD__BY                                                                   = 2087;  // <_by> ::=
//		final int PROD__BY_BY                                                                = 2088;  // <_by> ::= BY
//		final int PROD__CHARACTER                                                            = 2089;  // <_character> ::=
//		final int PROD__CHARACTER_CHARACTER                                                  = 2090;  // <_character> ::= CHARACTER
//		final int PROD__CHARACTERS                                                           = 2091;  // <_characters> ::=
//		final int PROD__CHARACTERS_CHARACTERS                                                = 2092;  // <_characters> ::= CHARACTERS
//		final int PROD__CONTAINS                                                             = 2093;  // <_contains> ::=
//		final int PROD__CONTAINS_CONTAINS                                                    = 2094;  // <_contains> ::= CONTAINS
//		final int PROD__DATA                                                                 = 2095;  // <_data> ::=
//		final int PROD__DATA_DATA                                                            = 2096;  // <_data> ::= DATA
//		final int PROD__END_OF                                                               = 2097;  // <_end_of> ::=
//		final int PROD__END_OF_END                                                           = 2098;  // <_end_of> ::= END <_of>
//		final int PROD__FILE                                                                 = 2099;  // <_file> ::=
//		final int PROD__FILE_TOK_FILE                                                        = 2100;  // <_file> ::= 'TOK_FILE'
//		final int PROD__FINAL                                                                = 2101;  // <_final> ::=
//		final int PROD__FINAL_FINAL                                                          = 2102;  // <_final> ::= FINAL
//		final int PROD__FOR                                                                  = 2103;  // <_for> ::=
//		final int PROD__FOR_FOR                                                              = 2104;  // <_for> ::= FOR
//		final int PROD__FROM                                                                 = 2105;  // <_from> ::=
//		final int PROD__FROM_FROM                                                            = 2106;  // <_from> ::= FROM
//		final int PROD__IN                                                                   = 2107;  // <_in> ::=
//		final int PROD__IN_IN                                                                = 2108;  // <_in> ::= IN
//		final int PROD__IN_ORDER                                                             = 2109;  // <_in_order> ::=
//		final int PROD__IN_ORDER_ORDER                                                       = 2110;  // <_in_order> ::= ORDER
//		final int PROD__IN_ORDER_IN_ORDER                                                    = 2111;  // <_in_order> ::= IN ORDER
//		final int PROD__INDEX                                                                = 2112;  // <_index> ::=
//		final int PROD__INDEX_INDEX                                                          = 2113;  // <_index> ::= INDEX
//		final int PROD__INDICATE                                                             = 2114;  // <_indicate> ::=
//		final int PROD__INDICATE_INDICATE                                                    = 2115;  // <_indicate> ::= INDICATE
//		final int PROD__INITIAL                                                              = 2116;  // <_initial> ::=
//		final int PROD__INITIAL_TOK_INITIAL                                                  = 2117;  // <_initial> ::= 'TOK_INITIAL'
//		final int PROD__INTO                                                                 = 2118;  // <_into> ::=
//		final int PROD__INTO_INTO                                                            = 2119;  // <_into> ::= INTO
//		final int PROD__IS                                                                   = 2120;  // <_is> ::=
//		final int PROD__IS_IS                                                                = 2121;  // <_is> ::= IS
//		final int PROD__IS_ARE                                                               = 2122;  // <_is_are> ::=
//		final int PROD__IS_ARE_IS                                                            = 2123;  // <_is_are> ::= IS
//		final int PROD__IS_ARE_ARE                                                           = 2124;  // <_is_are> ::= ARE
//		final int PROD__KEY                                                                  = 2125;  // <_key> ::=
//		final int PROD__KEY_KEY                                                              = 2126;  // <_key> ::= KEY
//		final int PROD__LEFT_OR_RIGHT                                                        = 2127;  // <_left_or_right> ::=
//		final int PROD__LEFT_OR_RIGHT_LEFT                                                   = 2128;  // <_left_or_right> ::= LEFT
//		final int PROD__LEFT_OR_RIGHT_RIGHT                                                  = 2129;  // <_left_or_right> ::= RIGHT
//		final int PROD__LINE                                                                 = 2130;  // <_line> ::=
//		final int PROD__LINE_LINE                                                            = 2131;  // <_line> ::= LINE
//		final int PROD__LINE_OR_LINES                                                        = 2132;  // <_line_or_lines> ::=
//		final int PROD__LINE_OR_LINES_LINE                                                   = 2133;  // <_line_or_lines> ::= LINE
//		final int PROD__LINE_OR_LINES_LINES                                                  = 2134;  // <_line_or_lines> ::= LINES
//		final int PROD__LIMITS                                                               = 2135;  // <_limits> ::=
//		final int PROD__LIMITS_LIMIT                                                         = 2136;  // <_limits> ::= LIMIT <_is>
//		final int PROD__LIMITS_LIMITS                                                        = 2137;  // <_limits> ::= LIMITS <_are>
//		final int PROD__LINES                                                                = 2138;  // <_lines> ::=
//		final int PROD__LINES_LINES                                                          = 2139;  // <_lines> ::= LINES
//		final int PROD__MESSAGE                                                              = 2140;  // <_message> ::=
//		final int PROD__MESSAGE_MESSAGE                                                      = 2141;  // <_message> ::= MESSAGE
//		final int PROD__MODE                                                                 = 2142;  // <_mode> ::=
//		final int PROD__MODE_MODE                                                            = 2143;  // <_mode> ::= MODE
//		final int PROD__NUMBER                                                               = 2144;  // <_number> ::=
//		final int PROD__NUMBER_NUMBER                                                        = 2145;  // <_number> ::= NUMBER
//		final int PROD__NUMBERS                                                              = 2146;  // <_numbers> ::=
//		final int PROD__NUMBERS_NUMBER                                                       = 2147;  // <_numbers> ::= NUMBER
//		final int PROD__NUMBERS_NUMBERS                                                      = 2148;  // <_numbers> ::= NUMBERS
//		final int PROD__OF                                                                   = 2149;  // <_of> ::=
//		final int PROD__OF_OF                                                                = 2150;  // <_of> ::= OF
//		final int PROD__ON                                                                   = 2151;  // <_on> ::=
//		final int PROD__ON_ON                                                                = 2152;  // <_on> ::= ON
//		final int PROD__ONOFF_STATUS                                                         = 2153;  // <_onoff_status> ::=
//		final int PROD__ONOFF_STATUS_STATUS_IS                                               = 2154;  // <_onoff_status> ::= STATUS IS
//		final int PROD__ONOFF_STATUS_STATUS                                                  = 2155;  // <_onoff_status> ::= STATUS
//		final int PROD__ONOFF_STATUS_IS                                                      = 2156;  // <_onoff_status> ::= IS
//		final int PROD__OTHER                                                                = 2157;  // <_other> ::=
//		final int PROD__OTHER_OTHER                                                          = 2158;  // <_other> ::= OTHER
//		final int PROD__PROCEDURE                                                            = 2159;  // <_procedure> ::=
//		final int PROD__PROCEDURE_PROCEDURE                                                  = 2160;  // <_procedure> ::= PROCEDURE
//		final int PROD__PROGRAM                                                              = 2161;  // <_program> ::=
//		final int PROD__PROGRAM_PROGRAM                                                      = 2162;  // <_program> ::= PROGRAM
//		final int PROD__RECORD                                                               = 2163;  // <_record> ::=
//		final int PROD__RECORD_RECORD                                                        = 2164;  // <_record> ::= RECORD
//		final int PROD__RECORDS                                                              = 2165;  // <_records> ::=
//		final int PROD__RECORDS_RECORD                                                       = 2166;  // <_records> ::= RECORD
//		final int PROD__RECORDS_RECORDS                                                      = 2167;  // <_records> ::= RECORDS
//		final int PROD__RIGHT                                                                = 2168;  // <_right> ::=
//		final int PROD__RIGHT_RIGHT                                                          = 2169;  // <_right> ::= RIGHT
//		final int PROD__SIGN                                                                 = 2170;  // <_sign> ::=
//		final int PROD__SIGN_SIGN                                                            = 2171;  // <_sign> ::= SIGN
//		final int PROD__SIGNED                                                               = 2172;  // <_signed> ::=
//		final int PROD__SIGNED_SIGNED                                                        = 2173;  // <_signed> ::= SIGNED
//		final int PROD__SIGN_IS                                                              = 2174;  // <_sign_is> ::=
//		final int PROD__SIGN_IS_SIGN                                                         = 2175;  // <_sign_is> ::= SIGN
//		final int PROD__SIGN_IS_SIGN_IS                                                      = 2176;  // <_sign_is> ::= SIGN IS
//		final int PROD__SIZE                                                                 = 2177;  // <_size> ::=
//		final int PROD__SIZE_SIZE                                                            = 2178;  // <_size> ::= SIZE
//		final int PROD__STANDARD                                                             = 2179;  // <_standard> ::=
//		final int PROD__STANDARD_STANDARD                                                    = 2180;  // <_standard> ::= STANDARD
//		final int PROD__STATUS                                                               = 2181;  // <_status> ::=
//		final int PROD__STATUS_STATUS                                                        = 2182;  // <_status> ::= STATUS
//		final int PROD__SYMBOLIC                                                             = 2183;  // <_symbolic> ::=
//		final int PROD__SYMBOLIC_SYMBOLIC                                                    = 2184;  // <_symbolic> ::= SYMBOLIC
//		final int PROD__TAPE                                                                 = 2185;  // <_tape> ::=
//		final int PROD__TAPE_TAPE                                                            = 2186;  // <_tape> ::= TAPE
//		final int PROD__TERMINAL                                                             = 2187;  // <_terminal> ::=
//		final int PROD__TERMINAL_TERMINAL                                                    = 2188;  // <_terminal> ::= TERMINAL
//		final int PROD__THEN                                                                 = 2189;  // <_then> ::=
//		final int PROD__THEN_THEN                                                            = 2190;  // <_then> ::= THEN
//		final int PROD__TIMES                                                                = 2191;  // <_times> ::=
//		final int PROD__TIMES_TIMES                                                          = 2192;  // <_times> ::= TIMES
//		final int PROD__TO                                                                   = 2193;  // <_to> ::=
//		final int PROD__TO_TO                                                                = 2194;  // <_to> ::= TO
//		final int PROD__TO_USING                                                             = 2195;  // <_to_using> ::=
//		final int PROD__TO_USING_TO                                                          = 2196;  // <_to_using> ::= TO
//		final int PROD__TO_USING_USING                                                       = 2197;  // <_to_using> ::= USING
//		final int PROD__WHEN                                                                 = 2198;  // <_when> ::=
//		final int PROD__WHEN_WHEN                                                            = 2199;  // <_when> ::= WHEN
//		final int PROD__WHEN_SET_TO                                                          = 2200;  // <_when_set_to> ::=
//		final int PROD__WHEN_SET_TO_WHEN_SET_TO                                              = 2201;  // <_when_set_to> ::= WHEN SET TO
//		final int PROD__WITH                                                                 = 2202;  // <_with> ::=
//		final int PROD__WITH_WITH                                                            = 2203;  // <_with> ::= WITH
//		final int PROD_COLL_SEQUENCE_COLLATING_SEQUENCE                                      = 2204;  // <coll_sequence> ::= COLLATING SEQUENCE
//		final int PROD_COLL_SEQUENCE_SEQUENCE                                                = 2205;  // <coll_sequence> ::= SEQUENCE
//		final int PROD_COLUMN_OR_COL_COLUMN                                                  = 2206;  // <column_or_col> ::= COLUMN
//		final int PROD_COLUMN_OR_COL_COL                                                     = 2207;  // <column_or_col> ::= COL
//		final int PROD_COLUMNS_OR_COLS_COLUMNS                                               = 2208;  // <columns_or_cols> ::= COLUMNS
//		final int PROD_COLUMNS_OR_COLS_COLS                                                  = 2209;  // <columns_or_cols> ::= COLS
//		final int PROD_COMP_EQUAL_TOK_EQUAL                                                  = 2210;  // <comp_equal> ::= 'TOK_EQUAL'
//		final int PROD_COMP_EQUAL_EQUAL                                                      = 2211;  // <comp_equal> ::= EQUAL
//		final int PROD_EXCEPTION_OR_ERROR_EXCEPTION                                          = 2212;  // <exception_or_error> ::= EXCEPTION
//		final int PROD_EXCEPTION_OR_ERROR_ERROR                                              = 2213;  // <exception_or_error> ::= ERROR
//		final int PROD_IN_OF_IN                                                              = 2214;  // <in_of> ::= IN
//		final int PROD_IN_OF_OF                                                              = 2215;  // <in_of> ::= OF
//		final int PROD_LABEL_OPTION_STANDARD                                                 = 2216;  // <label_option> ::= STANDARD
//		final int PROD_LABEL_OPTION_OMITTED                                                  = 2217;  // <label_option> ::= OMITTED
//		final int PROD_LINE_OR_LINES_LINE                                                    = 2218;  // <line_or_lines> ::= LINE
//		final int PROD_LINE_OR_LINES_LINES                                                   = 2219;  // <line_or_lines> ::= LINES
//		final int PROD_LOCK_RECORDS_RECORD                                                   = 2220;  // <lock_records> ::= RECORD
//		final int PROD_LOCK_RECORDS_RECORDS                                                  = 2221;  // <lock_records> ::= RECORDS
//		final int PROD_OBJECT_CHAR_OR_WORD_CHARACTERS                                        = 2222;  // <object_char_or_word> ::= CHARACTERS
//		final int PROD_OBJECT_CHAR_OR_WORD_WORDS                                             = 2223;  // <object_char_or_word> ::= WORDS
//		final int PROD_RECORDS_RECORD                                                        = 2224;  // <records> ::= RECORD <_is>
//		final int PROD_RECORDS_RECORDS                                                       = 2225;  // <records> ::= RECORDS <_are>
//		final int PROD_REEL_OR_UNIT_REEL                                                     = 2226;  // <reel_or_unit> ::= REEL
//		final int PROD_REEL_OR_UNIT_UNIT                                                     = 2227;  // <reel_or_unit> ::= UNIT
//		final int PROD_SCROLL_LINE_OR_LINES_LINE                                             = 2228;  // <scroll_line_or_lines> ::= LINE
//		final int PROD_SCROLL_LINE_OR_LINES_LINES                                            = 2229;  // <scroll_line_or_lines> ::= LINES
//		final int PROD_SIZE_OR_LENGTH_SIZE                                                   = 2230;  // <size_or_length> ::= SIZE
//		final int PROD_SIZE_OR_LENGTH_LENGTH                                                 = 2231;  // <size_or_length> ::= LENGTH
//		final int PROD_WITH_DUPS_WITH_DUPLICATES                                             = 2232;  // <with_dups> ::= WITH DUPLICATES
//		final int PROD_WITH_DUPS_DUPLICATES                                                  = 2233;  // <with_dups> ::= DUPLICATES
//		final int PROD_PROG_COLL_SEQUENCE_PROGRAM_COLLATING_SEQUENCE                         = 2234;  // <prog_coll_sequence> ::= PROGRAM COLLATING SEQUENCE
//		final int PROD_PROG_COLL_SEQUENCE_COLLATING_SEQUENCE                                 = 2235;  // <prog_coll_sequence> ::= COLLATING SEQUENCE
//		final int PROD_PROG_COLL_SEQUENCE_SEQUENCE                                           = 2236;  // <prog_coll_sequence> ::= SEQUENCE
//		final int PROD_DETAIL_KEYWORD_DETAIL                                                 = 2237;  // <detail_keyword> ::= DETAIL
//		final int PROD_DETAIL_KEYWORD_DE                                                     = 2238;  // <detail_keyword> ::= DE
//		final int PROD_CH_KEYWORD_CONTROL_HEADING                                            = 2239;  // <ch_keyword> ::= CONTROL HEADING
//		final int PROD_CH_KEYWORD_CH                                                         = 2240;  // <ch_keyword> ::= CH
//		final int PROD_CF_KEYWORD_CONTROL_FOOTING                                            = 2241;  // <cf_keyword> ::= CONTROL FOOTING
//		final int PROD_CF_KEYWORD_CF                                                         = 2242;  // <cf_keyword> ::= CF
//		final int PROD_PH_KEYWORD_PAGE_HEADING                                               = 2243;  // <ph_keyword> ::= PAGE HEADING
//		final int PROD_PH_KEYWORD_PH                                                         = 2244;  // <ph_keyword> ::= PH
//		final int PROD_PF_KEYWORD_PAGE_FOOTING                                               = 2245;  // <pf_keyword> ::= PAGE FOOTING
//		final int PROD_PF_KEYWORD_PF                                                         = 2246;  // <pf_keyword> ::= PF
//		final int PROD_RH_KEYWORD_REPORT_HEADING                                             = 2247;  // <rh_keyword> ::= REPORT HEADING
//		final int PROD_RH_KEYWORD_RH                                                         = 2248;  // <rh_keyword> ::= RH
//		final int PROD_RF_KEYWORD_REPORT_FOOTING                                             = 2249;  // <rf_keyword> ::= REPORT FOOTING
//		final int PROD_RF_KEYWORD_RF                                                         = 2250;  // <rf_keyword> ::= RF
//		final int PROD_CONTROL_KEYWORD_CONTROL                                               = 2251;  // <control_keyword> ::= CONTROL <_is>
//		final int PROD_CONTROL_KEYWORD_CONTROLS                                              = 2252;  // <control_keyword> ::= CONTROLS <_are>
	};

	//------------------- Comment association specification ---------------------------------

	// START KGU#407 2017-10-01: Enh. #420 - Slightly improved approach - still to be tuned
	/** rule ids representing statements, used as stoppers for comment retrieval */
	private static final int[] statementIds = new int[]{
			RuleConstants.PROD_PROGRAM_DEFINITION,
			RuleConstants.PROD_FUNCTION_DEFINITION,
			//RuleConstants.PROD_IDENTIFICATION_OR_ID_IDENTIFICATION,
			//RuleConstants.PROD_IDENTIFICATION_OR_ID_ID,
			RuleConstants.PROD_FUNCTION_ID_PARAGRAPH_FUNCTION_ID_TOK_DOT_TOK_DOT,
			RuleConstants.PROD_PROGRAM_ID_PARAGRAPH_PROGRAM_ID_TOK_DOT_TOK_DOT,
			RuleConstants.PROD__PROCEDURE_USING_CHAINING_USING,
			RuleConstants.PROD_SECTION_HEADER_SECTION_TOK_DOT,
			RuleConstants.PROD_PARAGRAPH_HEADER_TOK_DOT,
			RuleConstants.PROD_PROCEDURE_TOK_DOT,
			RuleConstants.PROD_PROCEDURE_TOK_DOT2,
			RuleConstants.PROD_IF_STATEMENT_IF,
			RuleConstants.PROD_EVALUATE_STATEMENT_EVALUATE,
			RuleConstants.PROD_PERFORM_STATEMENT_PERFORM,
			RuleConstants.PROD_MOVE_STATEMENT_MOVE,
			RuleConstants.PROD_SET_STATEMENT_SET,
			RuleConstants.PROD_COMPUTE_STATEMENT_COMPUTE,
			RuleConstants.PROD_ADD_STATEMENT_ADD,
			RuleConstants.PROD_SUBTRACT_STATEMENT_SUBTRACT,
			RuleConstants.PROD_MULTIPLY_STATEMENT_MULTIPLY,
			RuleConstants.PROD_DIVIDE_STATEMENT_DIVIDE,
			RuleConstants.PROD_ACCEPT_STATEMENT_ACCEPT,
			RuleConstants.PROD_DISPLAY_STATEMENT_DISPLAY,
			RuleConstants.PROD_FILE_CONTROL_ENTRY_SELECT,
			RuleConstants.PROD_FILE_DESCRIPTION,
			RuleConstants.PROD_OPEN_STATEMENT_OPEN,
			RuleConstants.PROD_READ_STATEMENT_READ,
			RuleConstants.PROD_WRITE_STATEMENT_WRITE,
			RuleConstants.PROD_REWRITE_STATEMENT_REWRITE,
			RuleConstants.PROD_DELETE_STATEMENT_DELETE,
			RuleConstants.PROD_CLOSE_STATEMENT_CLOSE,
			RuleConstants.PROD_CALL_STATEMENT_CALL,
			RuleConstants.PROD_EXIT_STATEMENT_EXIT,
			RuleConstants.PROD_GOBACK_STATEMENT_GOBACK,
			RuleConstants.PROD_STOP_STATEMENT_STOP,
			RuleConstants.PROD_GOTO_STATEMENT_GO,
			RuleConstants.PROD_STRING_STATEMENT_STRING,
			RuleConstants.PROD_UNSTRING_STATEMENT_UNSTRING,
			RuleConstants.PROD_SEARCH_STATEMENT_SEARCH,
			RuleConstants.PROD__WORKING_STORAGE_SECTION_WORKING_STORAGE_SECTION_TOK_DOT,
			RuleConstants.PROD__LOCAL_STORAGE_SECTION_LOCAL_STORAGE_SECTION_TOK_DOT,
			RuleConstants.PROD__LINKAGE_SECTION_LINKAGE_SECTION_TOK_DOT,
			RuleConstants.PROD__FILE_SECTION_HEADER_TOK_FILE_SECTION_TOK_DOT,
			RuleConstants.PROD__REPORT_SECTION_REPORT_SECTION_TOK_DOT,
			RuleConstants.PROD__SCREEN_SECTION_SCREEN_SECTION_TOK_DOT
	};
	// END KGU#407 2017-10-01

	//----------------------- Local helper functions -------------------------

//	/**
//	 * replace a single character at offset {@code pos} in {@code oldString} with another char
//	 * @param oldString
//	 * @param newChar
//	 * @param pos
//	 * @return replaced String
//	 */
//	private String replaceCharPosInString(String oldString, char newChar, int pos) {
//		int strLen = 0;
//		if (oldString != null) {
//			strLen = oldString.length();
//		}
//		if (oldString == null || strLen < 2) {
//			return newChar + ""; // convert char to String
//		} else if (pos >= 0 && pos <= strLen-1) {
//			return oldString.substring(0, pos) + newChar + oldString.substring(pos + 1);
//		}
//		return oldString;
//	}

	//----------------------------- Preprocessor -----------------------------

	// line length for source if source format is VARIABLE
	private static final int TEXTCOLUMN_VARIABLE = 500;

	// START KGU#473 2017-12-04: Bugfix #485
	/** Names of all known intrinsic functions to be prefixed with "FUNCTION" for the parser */
	private static final String[] INTRINSIC_FUNCTION_NAMES = {
		"ABS",
		"ABSOLUTE-VALUE",
		"ACOS",
		"ANNUITY",
		"ASIN",
		"ATAN",
		"BOOLEAN-OF-INTEGER",
		"BYTE-LENGTH",
		"CHAR",
		"CHAR-NATIONAL",
		"COMBINED-DATETIME",
		"CONCATENATE",
		"COS",
		"CURRENCY-SYMBOL",
		"CURRENT-DATE",
		"DATE-OF-INTEGER",
		"DATE-TO-YYYYMMDD",
		"DAY-OF-INTEGER",
		"DAY-TO-YYYYDDD",
		"DISPLAY-OF",
		"E",
		"EXCEPTION-FILE",
		"EXCEPTION-FILE-N",
		"EXCEPTION-LOCATION",
		"EXCEPTION-LOCATION-N",
		"EXCEPTION-STATEMENT",
		"EXCEPTION-STATUS",
		"EXP",
		"EXP10",
		"FACTORIAL",
		"FORMATTED-CURRENT-DATE",
		"FORMATTED-DATE",
		"FORMATTED-DATETIME",
		"FORMATTED-TIME",
		"FRACTION-PART",
		"HIGHEST-ALGEBRAIC",
		"INTEGER",
		"INTEGER-OF-BOOLEAN",
		"INTEGER-OF-DATE",
		"INTEGER-OF-DAY",
		"INTEGER-OF-FORMATTED-DATE",
		"INTEGER-PART",
		"LENGTH",
		"LENGTH-AN",
		"LOCALE-COMPARE",
		"LOCALE-DATE",
		"LOCALE-TIME",
		"LOCALE-TIME-FROM-SECONDS",
		"LOG",
		"LOG10",
		"LOWER-CASE",
		"LOWEST-ALGEBRAIC",
		"MAX",
		"MEAN",
		"MEDIAN",
		"MIDRANGE",
		"MIN",
		"MOD",
		"MODULE-CALLER-ID",
		"MODULE-DATE",
		"MODULE-FORMATTED-DATE",
		"MODULE-ID",
		"MODULE-PATH",
		"MODULE-SOURCE",
		"MODULE-TIME",
		"MONETARY-DECIMAL-POINT",
		"MONETARY-THOUSANDS-SEPARATOR",
		"NATIONAL-OF",
		"NUMERIC-DECIMAL-POINT",
		"NUMERIC-THOUSANDS-SEPARATOR",
		"NUMVAL",
		"NUMVAL-C",
		"NUMVAL-F",
		"ORD",
		"ORD-MAX",
		"ORD-MIN",
		"PI",
		"PRESENT-VALUE",
		"RANDOM",
		"RANGE",
		"REM",
		"REVERSE",
		"SECONDS-FROM-FORMATTED-TIME",
		"SECONDS-PAST-MIDNIGHT",
		"SIGN",
		"SIN",
		"SQRT",
		"STANDARD-COMPARE",
		"STANDARD-DEVIATION",
		"STORED-CHAR-LENGTH",
		"SUBSTITUTE",
		"SUBSTITUTE-CASE",
		"SUM",
		"TAN",
		"TEST-DATE-YYYYMMDD",
		"TEST-DAY-YYYYDDD",
		"TEST-FORMATTED-DATETIME",
		"TEST-NUMVAL",
		"TEST-NUMVAL-C",
		"TEST-NUMVAL-F",
		"TRIM",
		"UPPER-CASE",
		"VARIANCE",
		"WHEN-COMPILED",
		"YEAR-TO-YYYY"
	};
	private static class RepositoryAutomaton {
		enum State {
			RA_START,
			RA_DIV0, RA_SECT0, RA_SECT1,
			RA_ENV0, RA_ENV1, RA_ENV,
			RA_CONF0, RA_CONF1, RA_CONF,
			RA_REP0, RA_REP1, RA_REP2, RA_REP3a, RA_REP3b, RA_REP4a, RA_REP4b, RA_REP5b,
			RA_READY,
			RA_PROC0, RA_PROC1, RA_PROC2, RA_PROC,
			RA_END
			};

		private State state = State.RA_START;
		private static HashSet<String> intrinsicFunctions = new HashSet<String>();
		static {
			for (String fn: INTRINSIC_FUNCTION_NAMES) {
				intrinsicFunctions.add(fn);
			}
		}
		private HashSet<String> privilegedFunctions = new HashSet<String>();
		private String pendingName = null;

		public String process(String line)
		{
			boolean replacementsDone = false;
//			StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(line));
//			tokenizer.quoteChar('"');
//			tokenizer.quoteChar('\'');
//			tokenizer.slashStarComments(false);
//			tokenizer.slashSlashComments(false);
//			tokenizer.commentChar('*');
//			tokenizer.parseNumbers();
//			// Underscore must be added to word characters!
//			tokenizer.wordChars('_', '_');
//			tokenizer.wordChars('-', '-');

			// START KGU#613 2018-12-13: Issue #631 - cope with string literals and eliminate obsolete separators
			//StringList tokens = StringList.explodeWithDelimiter(line.trim(), ".");
			StringList tokens0 = StringList.explodeWithDelimiter(line.trim(), "\"");
			tokens0 = StringList.explodeWithDelimiter(tokens0, "'");
			tokens0 = StringList.explodeWithDelimiter(tokens0, "(");
			tokens0 = StringList.explodeWithDelimiter(tokens0, ")");
			tokens0 = StringList.explodeWithDelimiter(tokens0, ";");
			tokens0 = StringList.explodeWithDelimiter(tokens0, ",");
			// START KGU#672 2019-03-05: Bugfix #631 commas must not be eliminated within pic clauses
			tokens0 = StringList.explodeWithDelimiter(tokens0, " PICTURE ", false);
			tokens0 = StringList.explodeWithDelimiter(tokens0, " PIC ", false);
			tokens0 = StringList.explodeWithDelimiter(tokens0, " VALUE ", false);
			// END KGU#672 2019-03-05
			StringList tokens = new StringList();
			StringList literals = new StringList();
			boolean separatorsRemoved = false;
			int parenthLevel = 0;
			int posDelim = -1;
			// START KGU#672 2019-03-04: Bugfix #631 commas must not be eliminated within pic clauses
			boolean inPic = false;
			// END KGU#672 2019-03-04
			for (int i = 0; i < tokens0.count(); i++) {
				String token = tokens0.get(i);
				if (posDelim < 0) {
					if (token.equals("\"") || token.equals("'")) {
						posDelim = i;
						token = null;
					}
					// START KGU#672 2019-03-04: Bugfix #631 commas must not be eliminated within pic clauses
					else if (token.equalsIgnoreCase(" pic ") || token.equalsIgnoreCase(" picture ")) {
						inPic = true;
					}
					else if (inPic && (token.equalsIgnoreCase(" value ") || i == tokens0.count()-1)) {
						inPic = false;
					}
					// END KGU#672 2019-03-04
					else if (token.equals("(")) {
						parenthLevel++;
					}
					else if (token.equals(")") && parenthLevel > 0) {
						parenthLevel--;
					}
					// START KGU#672 2019-03-04: Bugfix #631 commas must not be eliminated within pic clauses
					//else if (token.equals(";") || parenthLevel == 0 && token.equals(",")) {
					else if (token.equals(";") || parenthLevel == 0 && !inPic && token.equals(",")) {
					// END KGU#672 2019-03-04
						token = " ";	// Multiple spaces will be removed later
						separatorsRemoved = true;
					}
					if (token != null) {
						tokens.add(token);
					}
				}
				else if (token.equals(tokens0.get(posDelim)) || i == tokens0.count()-1) {
					// On duplicate delimiters don't close the literal!
					if (!(i+1 < tokens0.count() && tokens.get(i+1).equals(tokens0.get(posDelim)))) {
						// Replace the string literal by a dummy token
						tokens.add("'§STRINGLITERAL§'");
						// Cache the original String literal including delimiters
						literals.add(tokens0.subSequence(posDelim, i+1).concatenate());
						posDelim = -1;
					}
				}
			}
			// First reconcatenate the parts lest too many separating blanks should be inserted in the end
			tokens = StringList.explodeWithDelimiter(tokens.concatenate(), ".");
			// END KGU#613 2018-12-13
			tokens = StringList.explode(tokens, "\\s+");
			// START KGU#613 2018-12-16: Issue #631 - Previous splittings may have left empty strings
			//if (tokens.count() == 0 || tokens.get(0).startsWith("*") || state == State.RA_END) {
			tokens.removeAll("");	// Wipe off splitting artifacts lest they should get padded afterwards
			if (tokens.count() == 0 || tokens.get(0).startsWith("*")) {
			// END KGU#613 2018-12-16
				// Nothing to do here
				return line;
			}
			int pos = -1;
			boolean followsFUNCTION = false;
			// FIXME We must find a way to skip string literals
			while (pos+1 < tokens.count() && state != State.RA_END) {
				int pos1 = -1;
				switch (state) {
				case RA_START:	// No repository section seen
					if ((pos1 = tokens.indexOf("ENVIRONMENT", pos+1, false)) >= 0) {
						// Might be the begin of the ENVIRONMENT DIVISION
						pos = pos1;
						state = State.RA_ENV0;
					}
					else if ((pos1 = tokens.indexOf("DATA", pos+1, false)) >= 0) {
						// We assume to enter the DATA division instead -> no repository
						pos = pos1;
						state = State.RA_DIV0;
					}
					else if ((pos1 = tokens.indexOf("PROCEDURE", pos+1, false)) >= 0) {
						// We assume to enter the PROCEDURE division instead -> no repository
						pos = pos1;
						state = State.RA_DIV0;
					}
					else {
						pos++;
					}
					break;
				case RA_DIV0:	// Waitig for "DIVISION" - to end - or to fall back otherwise
					if (tokens.get(++pos).equalsIgnoreCase("DIVISION")) {
						// No REPOSITORY SECTION...
						state = State.RA_END;
					}
					else {
						// Fall back, was something else
						state = State.RA_START;
					}
					break;

				case RA_ENV0:	// "ENVIRONMENT" seen, waiting for "DIVISION"
					if (tokens.get(++pos).equalsIgnoreCase("DIVISION")) {
						// ENVIRONMENT SECTION beginning?
						state = State.RA_ENV1;
					}
					else {
						// Was something else
						state = State.RA_START;
					}
					break;
				case RA_ENV1:	// "ENVIRONMENT DIVISION" seen, waiting for TOK-DOT
					if (tokens.get(++pos).equals(".")) {
						state = State.RA_ENV;
					}
					else {
						// Was likely a syntax error
						state = State.RA_START;
					}
					break;
				case RA_ENV:	// "ENVIRONMENT DIVISION." seen, waiting for "CONFIGURATION"
					if ((pos1 = tokens.indexOf("CONFIGURATION", ++pos, false)) >= 0) {
						state = State.RA_CONF0;
						pos = pos1;
					}
					else if ((pos1 = tokens.indexOf("INPUT-OUTPUT", ++pos, false)) >= 0) {
						state = State.RA_SECT0;
						pos = pos1;
					}
					break;
				case RA_SECT0:	// Waiting for "SECTION" - to end - or to fall back otherwise
					if (tokens.get(++pos).equalsIgnoreCase("SECTION")) {
						// No REPOSITORY SECTION...
						state = State.RA_END;
					}
					else {
						// Fall back, was something else
						state = State.RA_ENV;
					}
					break;

				case RA_CONF0:	// "CONFIGURATION" seen, waiting for "SECTION"
					if (tokens.get(++pos).equalsIgnoreCase("SECTION")) {
						state = State.RA_CONF1;
					}
					else {
						// Not a CONFIGURATION SECTION, fall back
						state = State.RA_ENV;
					}
					break;
				case RA_CONF1:	// "CONFIGURATION SECTION" seen, waiting for TOK-DOT
					if (tokens.get(++pos).equals(".")) {
						state = State.RA_CONF;
					}
					else {
						// Was likely a syntax error
						state = State.RA_ENV;
					}
					break;
				case RA_CONF:	// in CONFIGURATION SECTION, waiting for "REPOSITORY"
					if ((pos1 = tokens.indexOf("REPOSITORY", pos+1, false)) >= 0) {
						state = State.RA_REP0;
						pos = pos1;
					}
					else if ((pos1 = tokens.indexOf("INPUT-OUTPUT", pos+1, false)) >= 0) {
						state = State.RA_SECT1;
						pos = pos1;
					}
					else {
						pos++;
					}
					break;
				case RA_SECT1:	// Waitig for "SECTION" - to end - or to fall back otherwise
					if (tokens.get(++pos).equalsIgnoreCase("SECTION")) {
						// No REPOSITORY SECTION...
						state = State.RA_END;
					}
					else {
						// Fall back, was something else
						state = State.RA_CONF;
					}
					break;

				case RA_REP0:	// Seen "REPOSITORY", waiting for TOK-DOT
					if (tokens.get(++pos).equals(".")) {
						state = State.RA_REP1;
					}
					else {
						// Must have been something else
						state = State.RA_CONF;
					}
					break;
				case RA_REP1:	// Inside REPOSITORY, awaiting a FUNCTION clause
					if (tokens.get(++pos).equalsIgnoreCase("FUNCTION")) {
						state = State.RA_REP2;
					}
					else {
						// We are done with REPOSITORY
						state = State.RA_READY;
					}
					break;
				case RA_REP2:	// Seen "FUNCTION", expecting a function name or "ALL"
				{
					String word = tokens.get(++pos).toUpperCase();
					if (word.equals("ALL") || intrinsicFunctions.contains(word)) {
						pendingName = word;
						state = State.RA_REP3a;
					}
					else {
						// Ignore the name, wait for "AS" or TOK_DOT
						state = State.RA_REP3b;
					}
					break;
				}
				case RA_REP3a:	// Seen "FUNCTION <name>", waiting for "INTRINSIC" or "AS"
				{
					String word = tokens.get(++pos).toUpperCase();
					if (word.equals("INTRINSIC")) {
						if (pendingName.equals("ALL")) {
							privilegedFunctions.addAll(intrinsicFunctions);
						}
						else {
							privilegedFunctions.add(pendingName);
						}
						// Okay, now there MAY be a TOK_DOT
						state = State.RA_REP4a;
					}
					else if (word.equals("AS")) {
						// Now wait for the alias
						state = State.RA_REP4b;
					}
					else {
						// Looks like a syntax error
						pendingName = null;
						state = State.RA_READY;
					}
					break;
				}
				case RA_REP4a:	// Seen "FUNCTION <name> INTRINSIC", waiting for TOK-DOT
					if (tokens.get(++pos).equals(".")) {
						// Clause is ready, another one might come...
						state = State.RA_REP1;
					}
					else if (tokens.get(pos).equalsIgnoreCase("FUNCTION")) {
						// Another FUNCTION clause without TOK-DOT
						state = State.RA_REP2;
					}
					else {
						// Looks like a syntax error
						state = State.RA_READY;
					}
					pendingName = null;
					break;
				case RA_REP3b:	// Seen "FUNCTION <name>", waiting for "AS" or TOK-DOT
				{
					String word = tokens.get(++pos).toUpperCase();
					if (word.equals("AS")) {
						state = State.RA_REP4b;
					}
					else if (word.equals(".")) {
						// Ready for next FUNCTION clause
						state = State.RA_REP1;
					}
					else if (word.equalsIgnoreCase("FUNCTION")) {
						// Another FUNCTION clause without TOK-DOT
						state = State.RA_REP2;
					}
					pendingName = null;
					break;
				}
				case RA_REP4b:	// Having seen "FUNCTION <name> AS", waiting for alias
					privilegedFunctions.add(tokens.get(++pos));
					// Still wait for the TOK-DOT
					state = State.RA_REP5b;
					break;
				case RA_REP5b:
					if (tokens.get(++pos).equals(".")) {
						// Ready for next FUNCTION clause
						state = State.RA_REP1;
					}
					else if (tokens.get(pos).equalsIgnoreCase("FUNCTION")) {
						// Another FUNCTION clause without TOK-DOT
						state = State.RA_REP2;
					}
					else {
						// Apparently syntax error
						state = State.RA_READY;
					}

				case RA_READY:	// Ready with REPOSITORY, waiting for PROCEDURE DIVISION
					if ((pos1 = tokens.indexOf("PROCEDURE", ++pos, false)) >= 0) {
						pos = pos1;
						state = State.RA_PROC0;
					}
					break;

				case RA_PROC0:	// Seen "PROCEDURE", waiting for "DIVISION"
					if (tokens.get(++pos).equalsIgnoreCase("DIVISION")) {
						state = State.RA_PROC1;
					}
					else {
						// Must have been something different
						state = State.RA_READY;
					}
					break;
				case RA_PROC1:	// Seen "PROCEDURE DIVISION", awaiting TOK-DOT
					if (tokens.get(++pos).equals(".")) {
						state = State.RA_PROC;
					}
					break;
				case RA_PROC:	// The only state where there may be replacements
				{
					// String literals should have been protected sufficiently by temporary substitution
					String token = tokens.get(++pos);
					if (token.equalsIgnoreCase("FUNCTION")) {
						followsFUNCTION = true;
						break;
					}
					StringList parts = StringList.explodeWithDelimiter(token, "(");
					parts = StringList.explodeWithDelimiter(parts, ")");
					for (int i = 0; i < parts.count(); i++) {
						String part = parts.get(i);
						if (followsFUNCTION) {
							followsFUNCTION = part.equalsIgnoreCase("FUNCTION");
							continue;
						}
						if (part.equalsIgnoreCase("FUNCTION")) {
							followsFUNCTION = true;
							continue;
						}
						// FIXME: We might have looked for a following parenthesis
						if (privilegedFunctions.contains(part.toUpperCase())) {
							parts.set(i, "FUNCTION " + part);
							replacementsDone = true;
						}
					}
					tokens.set(pos, parts.concatenate());
					break;
				}
				default:
					// Something must have gone wrong
					state = State.RA_END;
					break;
				}
				while (pos+1 < tokens.count() && tokens.get(pos+1).trim().isEmpty()) {
					pos++;
				}
			}
			// Build a new line if replacements have been done
			// START KGU#613 2018-12-16: Bugfix #631 (1st attempt)
			//if (replacementsDone) {
			//	int leftOffs = line.indexOf(line.trim());
			//	line = line.substring(0, leftOffs) + tokens.concatenate(" ");
			//}
			if (replacementsDone || separatorsRemoved || literals.count() > 0) {
				int leftOffs = line.indexOf(line.trim());
				line = line.substring(0, leftOffs) + tokens.concatenate(" ");
				// Restore temporarily substituted string literals
				if (literals.count() > 0) {
					tokens = StringList.explodeWithDelimiter(line, "'§STRINGLITERAL§'", true);
					for (int i = 0; i < literals.count(); i++) {
						int nextPos = tokens.indexOf("'§STRINGLITERAL§'");
						if (nextPos >= 0) {
							tokens.set(nextPos, literals.get(i));
						}
						else {
							// Some error occurred
							// FIXME: Find a way properly to report it!
							System.err.println("*** RepositoryAutomaton - Lost string literal in line:");
							System.err.println("\t" + line);
							System.err.println("\t" + tokens.concatenate(" "));
						}
					}
					line = tokens.concatenate();
				}
			}
			// END KGU#613 2018-12-13
			return line;
		}
	};
	// END KGU#473 2017-12-04

	/* configuration settings */
	// reference-format with column aware-parts
	private boolean settingFixedForm;
	private int settingFixedColumnIndicator;
	private int settingFixedColumnText;

	// special columns-> only set via setColumns as this recalculates settingCodeLength
	private int settingColumnIndicator;
	private int settingColumnText;

	private boolean srcCodeDebugLines;
	private boolean decimalComma;

	private boolean ignoreUnstringAllClauses;

	// 32 or 64 bit, used for different size calculations and possibly in preparser
	private boolean is32bit;

	// FIXME If the referenced fields are options then this will have to be recalculated
	private int settingCodeLength = settingColumnText - settingColumnIndicator - 1;
	
	// START KGU#605 2018-10-30: Issue #630 - first approach to warn on preprocessor directives
	/** Indicates the number of REPLACE or a COPY directives found in the code on preprocessing */
	private int lineNo = 0;
	private StringList codeLinesREPLACEorCOPY = null;
	// END KGU#605 2018-10-30

	/** Holds the base name for includable diagrams derived from the file name where all non-id characters are replaced with underscores */
	private String sourceName;
	
	/** A pair of cached line position and code length for internal text transformation */
	private class CodePosAndLength {
		public int pos, length;
		public CodePosAndLength(int _pos, int _length) {
			pos = _pos;
			length = _length;
		}
	};

	/**
	 * Performs some necessary preprocessing for the text file. Actually opens the
	 * file, filters it and writes a new temporary file "Structorizer.COB", which is
	 * then actually parsed.
	 * For the COBOL Parser e.g. the compiler directives must be removed and possibly
	 * be executed (at least the [COPY] REPLACE, with >> IF it should be possible as
	 * this is rarely used in COBOL).
	 * The preprocessed file will always be saved with UTF-8 encoding.
	 * @param _textToParse - name (path) of the source file
	 * @param _encoding - the expected encoding of the source file.
	 * @return The File object associated with the preprocessed source file.
	 */
	@Override
	protected File prepareTextfile(String _textToParse, String _encoding) throws ParserCancelled, FilePreparationException
	{
		/* TODO for preparsing:
		 * minimal handling compiler directives, at least SOURCE FORMAT [IS] FREE|FIXED
		 * for the start: remove compiler directives, later: more handling for them
		 * include the content of comment-entries (AUTHOR, SECURITY, ...) as a string (maybe
		 * remove the line breaks in them and provide a single string for easy parsing later)
		 * in fixed-form reference format:
		 *     remove column 1-6 / 7 / 73+ [later: use a setting for this]
		 *     hack debugging lines as comments [later: use a setting for including them]
		 *     do word concatenation      VAR -IABLE ('-' in indicator area)
		 *     hack literal continuation as string concatenation (end literal with '" &)
		 * if DECIMAL-POINT [IS] COMMA is active: change Digit,Digit to Digit.Digit
		 * remove ';' and ',' that are not part of a string/integer - cater also for ";;,,;"
		 * recognize and store constants (78 name value [is] literal | 01 name constant as literal)
		 * and replace them by tokens (must be redone during parsing)
		 * Register all intrinsic functions named in the repository
		 */

		// START KGU#473 2017-12-04: Bugfix #485
		RepositoryAutomaton repAuto = new RepositoryAutomaton();
		// END KGU#473 2017-12-04

		File interm = null;
		try
		{
			File file = new File(_textToParse);
			storeFileName(file);
			DataInputStream in = new DataInputStream(new FileInputStream(file));
			// START KGU#193 2016-05-04
			BufferedReader br = new BufferedReader(new InputStreamReader(in, _encoding));
			// END KGU#193 2016-05-04
			String strLine;
			StringBuilder srcCode = new StringBuilder();

			srcCodeDebugLines = (boolean)this.getPluginOption("debugLines", false);
			decimalComma = (boolean)this.getPluginOption("decimalComma", false);
			settingFixedForm = (boolean)this.getPluginOption("fixedForm", true);
			settingFixedColumnIndicator = (int)this.getPluginOption("fixedColumnIndicator", 7);
			settingFixedColumnText = (int)this.getPluginOption("fixedColumnText", 73);
			ignoreUnstringAllClauses = (boolean)this.getPluginOption("ignoreUnstringAll", true);
			is32bit = (boolean)this.getPluginOption("is32bit", true);

			CodePosAndLength lastPosAndLength = new CodePosAndLength(0, settingCodeLength);

			if (settingFixedForm) {
				// check for pre-processed by OpenCOBOL/GnuCOBOL --> set to free-form
				if ((strLine = br.readLine()) != null) {
					if (strLine.startsWith("# 1") || strLine.toUpperCase().startsWith("#LINE")) {
						// assume pre-processed by OpenCOBOL/GnuCOBOL --> set to free-form
						this.adjustSourceFormat("FREE");
					}
				}
				if (settingFixedForm) {
					setColumns(settingFixedColumnIndicator, settingFixedColumnText);
				}
				lastPosAndLength.length = settingCodeLength;	// setting may have changed by the previous actions
				// Now process the already read line before we enter the loop
				if (strLine != null) {
					prepareTextLine(repAuto, strLine, srcCode, lastPosAndLength);
				}
			}

			//Read File Line By Line
			/* FIXME Preprocessor directives are not tolerated by the grammar, so drop them
			 * or try to do the [COPY] REPLACE replacements (at least roughly..., issue #636)
			 */
			// START KGU#605 2018-10-30: Issue #630
			this.codeLinesREPLACEorCOPY = new StringList();
			// END KGU#605 2018-10-30
			while ((strLine = br.readLine()) != null)
			{
				// START KGU#605 2018-10-30: Issue #630
				this.lineNo++;
				// END KGU#605 2018-10-30
				prepareTextLine(repAuto, strLine, srcCode, lastPosAndLength);
			}
			//Close the input stream
			in.close();

			//System.out.println(srcCode);

			// trim and save as new file
			interm = File.createTempFile("Structorizer", "." + getFileExtensions()[0]);
			OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(interm), "UTF-8");
			ow.write(srcCode.toString()+"\n");
			ow.close();
		}
		catch (Exception e)
		{
			getLogger().log(Level.SEVERE, " -> ", e);
		}
		// START KGU#605 2018-10-30: Issue #630 - this is a temporary ugly mechanism also lacking translation support...
		if (this.codeLinesREPLACEorCOPY != null && this.codeLinesREPLACEorCOPY.count() > 0)  {
			throw new FilePreparationException("Found " + this.codeLinesREPLACEorCOPY.count()
					+ " REPLACE or COPY directive(s) the parser can't handle:\n"
					+ this.codeLinesREPLACEorCOPY.concatenate("\n----------\n")
					+ "\n\nPlease preprocess the file with a COBOL preparser or manually adjust before restarting the import.");
		}
		// END KGU#605 2018-10-30
		return interm;
	}

	/**
	 * For the COBOL Parser e.g. the compiler directives must be removed and possibly
	 * be executed (at least the [COPY] REPLACE, with >> IF it should be possible as
	 * this is rarely used in COBOL).<br/>
	 * This is a helper routine for {@link #prepareTextfile(String, String)}
	 * and does it for just the given line {@code strLine}, appending the result
	 * to {@code srcCode}.
	 * @param repAuto - the current {@link RepositoryAutomaton}
	 * @param strLine - the line just read
	 * @param srcCode - the prepared code being constructed
	 * @param posAndLength - pair of last position in source line and last code length
	 * @see #prepareTextfile(String, String)
	 */
	private void prepareTextLine(RepositoryAutomaton repAuto, String strLine, StringBuilder srcCode,
			CodePosAndLength posAndLength)
	{
		/* TODO for preparsing: see TODO comment in prepareTextfile(String, String) */
		if (settingFixedForm) { // fixed-form reference-format

			if (strLine.length() < settingColumnIndicator) {
				srcCode.append (strLine + "\n");
				posAndLength.pos += strLine.length() + 1;
				return;
			}

			String srcLineCode = strLine.substring(settingColumnIndicator);
			if (srcLineCode.length() > settingCodeLength) {
				// FIXME: Better check if the string contains *> already and is not part of a literal,
				//        if yes don't cut the string; if not: insert "*> TEXT AREA" in
				srcLineCode = srcLineCode.substring(0, settingCodeLength);
			}

			char srcLineIndicator = strLine.charAt(settingColumnIndicator - 1);
			if (srcLineIndicator == '*') {
				if (srcLineCode.length() < 1 || srcLineCode.trim().length() == 0) {
					strLine = "*>";
				} else if (srcLineCode.charAt(0) == '>') {
					strLine = "*>" + srcLineCode.substring(1);
				} else {
					strLine = "*>" + srcLineCode;
				}
			} else if (srcLineIndicator == '/') {
				if (srcLineCode.length() < 1 || srcLineCode.trim().length() == 0) {
					strLine = "*> page eject requested" + "\n" + "*>";
				} else {
					strLine = "*> page eject requested" + "\n" + "*>" + srcLineCode;
				}
			} else if (srcLineIndicator == 'D' && !srcCodeDebugLines) {
				if (srcLineCode.length() < 1 || srcLineCode.trim().length() == 0) {
					strLine = "*> DEBUG: ";
				} else {
					strLine = "*> DEBUG: " + srcLineCode;
				}
			} else if (srcLineIndicator == '-') {
				char firstNonSpaceInLine = srcLineCode.trim().charAt(0);
				// word continuation
				if (firstNonSpaceInLine != '\'' && firstNonSpaceInLine != '"') {
					srcCode.insert(posAndLength.pos - 1, srcLineCode);
					posAndLength.pos += srcLineCode.length();
					return;
				}
				// literal continuation
				// FIXME hack: string concatenation backwards (will only
				// work on "clean" sources)
				// and if the same literal symbol was used
				if (posAndLength.pos != 0) {
					while (posAndLength.length < settingCodeLength) {
						srcCode.insert(posAndLength.pos - 1, " ");
						posAndLength.pos++;	// last pos
						posAndLength.length++;	// last length
					}
					srcCode.insert(posAndLength.pos - 1, firstNonSpaceInLine + " &");
					posAndLength.pos += 4;
				}
				strLine = srcLineCode;
				posAndLength.length = strLine.length();
				posAndLength.pos += posAndLength.length;
			} else {
				String resultLine = checkForDirectives(srcLineCode);
				if (resultLine != null) {
					strLine = resultLine;
				} else {
//					int i = 0;
//					int im = srcLineCode.length();
//					char lastLit = ' ';
//					char lastChar = ' ';
//					while (i <= im) {
//						char currChar = srcLineCode.charAt(i);
//						// not within a literal
//						if (lastLit == ' ') {
//							// check if new literal starts
//							if (currChar == '"' || currChar == '\'') {
//								lastLit = currChar;
//							}
//								// check if we want to replace last separator comma/semicolon
//								if (lastChar == ';' || (lastChar == ',' && !decimalComma)) {
//									srcLineCode = replaceCharPosInString (srcLineCode, ' ', i - 1);
//								} else if (lastChar == ',') { // decimal comma is not active
//									if (!decimalComma) {
//										srcLineCode = replaceCharPosInString (srcLineCode, ' ', i - 1);
//									}
//								}
//							// within a literal --> only check if literal ends
//							} else if (lastLit == currChar && currChar != lastChar) {
//								lastLit = ' ';
//							}
//							lastChar = currChar;
					// }
					if (srcLineCode.trim().length() != 0) {
						strLine = srcLineCode;
						posAndLength.length = strLine.length();
					} else {
						strLine = "";
						posAndLength.length = 0;
					}
					posAndLength.pos = srcCode.length() + posAndLength.length + 1;
				}
			}

		} else { // free-form reference-format

			// skip empty lines
			if (strLine.trim().length() == 0) {
				srcCode.append ("\n");
				return;
			}

			String resultLine = checkForDirectives(strLine);
			if (resultLine != null) {
				strLine = resultLine;
			}

		}
		//srcCodeLastPos += 1;   // really needed for free-form reference-format?
		// START KGU#473 2017-12-04: Bugfix #485
		strLine = repAuto.process(strLine);
		// END KGU#473 2017-12-04
		srcCode.append (strLine + "\n");
	}

	private void storeFileName(File file) {
		String fileName = file.getName();
		int posDot = fileName.lastIndexOf(".");
		if (posDot > 0) {
			fileName = fileName.substring(0, posDot);
		}
		this.sourceName = fileName.replaceAll("[^A-Za-z0-9_]", "_");
	}

	/**
	 * function for checking the line for compiler directives and handle them appropriate
	 * @param codeLine   String to check
	 * @return true = directive found, line has not to be pre-parsed anymore
	 */
	private String checkForDirectives(String codeLine) {

		String resultLine = null;

		String[] tokenSeparator = codeLine.trim().split("\\s", 2); // we only want the first token here
		String firstToken = tokenSeparator[0].toUpperCase();

		// check for GnuCOBOL extension floating debugging line, no space here
		if (firstToken.startsWith(">>D")) {
			if (!srcCodeDebugLines) {
				if (codeLine.trim().length() == 0) {
					resultLine  = "*> DEBUG: ";
				} else {
					resultLine = "*> DEBUG: " + codeLine;
				}
			}

		// check for "normal" directive
		} else if (firstToken.startsWith(">>") || firstToken.startsWith("$")) {
			handleDirective(codeLine);
			// Directive --> added as comment
			resultLine = "*> DIRECTIVE: " + codeLine;

		// check for preprocessor directive
		} else if (firstToken.startsWith("#")) {
			// Directive --> added as comment
			resultLine = "*> PREPROCESSOR DIRECTIVE: " + codeLine;

		// check for COPY or REPLACE statements (rough check, only first token)
		} else if (firstToken.equals("COPY")) {
			// handleCopyStatement(strLine);
			// removed because must be set as comment until next period (multiple lines):
			// resultLine = "*> COPY book included: " + codeLine;
			// TODO log error - no support for COPY statement and present it to the user
			// as a warning after the parsing is finished
			// HACK for now: replace as comment if it looks like a one-line statement
			if (codeLine.endsWith(".")) {
				resultLine = "*> COPY statement: " + codeLine;
			}
			// we may revert this part later if we have not processed a COBOL statement
			// and convert it to a NSD CALL instruction of an IMPORT diagram
			// which could be created by a seperate run on the original copybook.
			// START KGU#605 2018-10-30: Issue #630
			else {
				this.codeLinesREPLACEorCOPY.add(String.format("%5d: %s", this.lineNo, codeLine));
			}
			// END KGU#605 2018-10-30
		} else if (firstToken.equals("REPLACE")) {
			// TODO store the replacements and do them
			// removed because must be set as comment until next period (multiple lines):
			// resultLine = "*> REPLACE: " + codeLine;
			// TODO log error - no support for REPLACE statement and present it to the user
			// as a warning after the parsing is finished
			// START KGU#605 2018-10-30: Issue #630
			this.codeLinesREPLACEorCOPY.add(String.format("%5d: %s", this.lineNo, codeLine));
			// END KGU#605 2018-10-30
		}

		return resultLine;
	}

	private void setColumns(int settingColumnIndicator, int settingColumnText) {
		this.settingColumnIndicator = settingColumnIndicator;
		this.settingColumnText = settingColumnText;
		this.settingCodeLength = settingColumnText - settingColumnIndicator - 1;
	}

	private void handleDirective(String strLine) {
		// create an array of tokens from the uppercased version of strLine,
		// make sure that >> directives have >> as a single token
		String[] tokens = strLine.trim().toUpperCase().replaceFirst(">>", ">> ").split("\\s+", 6);

		if (tokens[0].equals(">>")) { // handling COBOL standard directives
			int readToken = 1;

			// handling >>SOURCE [FORMAT] [IS] format
			if (tokens[readToken].equals("SOURCE")) {
				readToken++;

				// skip optional FORMAT
				if (tokens.length > readToken && tokens[readToken].equals("FORMAT")) {
					readToken++;
				}
				// skip optional IS
				if (tokens.length > readToken && tokens[readToken].equals("IS")) {
					readToken++;
				}
				// only go on if the token length is correct
				if (tokens.length == readToken + 1) {
					if (!adjustSourceFormat(tokens[readToken])) {
						// TODO: log invalid $SET SOURCEFORMAT value;
					}
				} else {
					// TODO: log invalid SOURCE FORMAT value
				}
			} else {
				// TODO: log unknown >> directive
			}


		} else { // handling MicroFocus $ directives

			// handling $SET directives
			if (tokens[0].equals("$SET")) {
				// handling $SET SOURCEFORMAT format
				if (tokens[1].equals("SOURCEFORMAT")) {
					// only go on if the token length is correct
					if (tokens.length != 3) {
						String sourceFormat = tokens[2];
						// FIXME: convert "format" / 'format' --> format
						// all three options are ok, we want to check only one per value
						if (!adjustSourceFormat(sourceFormat)) {
							// TODO: log invalid $SET SOURCEFORMAT value;
						}
					} else {
						// TODO: log invalid $SET SOURCEFORMAT value
					}
				} else {
					// TODO: log unknown $SET directive
				}
			} else {
				// TODO: log unknown $ directive
			}
		}

	}

	/**
	 * adjust the parameters for format (settingFixedForm, settingColumnIndicator, SettingColumnText)
	 * for the given format
	 * @param sourceFormat as string: FIXED / FREE / VARIABLE are recognized
	 * @return false if format wasn't recognized
	 */
	private boolean adjustSourceFormat(String sourceFormat) {
		if (sourceFormat.equals("FIXED")) {
			settingFixedForm = true;
			setColumns(settingFixedColumnIndicator, settingFixedColumnText);
		} else if (sourceFormat.equals("FREE")) {
			settingFixedForm = false;
		} else if (sourceFormat.equals("VARIABLE")) {
			settingFixedForm = true;
			setColumns(1, TEXTCOLUMN_VARIABLE);
		} else {
			return false;
		}
		return true;

	}

	//---------------------- Build fields and methods for structograms ---------------------------

	// START KGU#464 2017-12-03: Bugfix #475
	/** Target type for accomplishment of a {@link SectionOrParagraph} object */
	private static enum SoPTarget {SOP_SECTION, SOP_PARAGRAPH, SOP_ANY};
	// END KGU#464 2017-12-03

	/** Record for detected sections and paragraphs if needed as reference for possible calls */
	private class SectionOrParagraph {
		public String name;
		public boolean isSection = true;
		public int startsAt = -1;		// Element index of first statement within parent
		public int endsBefore = -1;	// Element index beyond closing TOK_DOT within parent
		public Subqueue parent = null;
		// START KGU#464 2017-12-03: Bugfix #475
		//public Element firstElement = null, lastElement = null;
		public SectionOrParagraph containedBy = null;
		public LinkedList<Jump> sectionExits = new LinkedList<Jump>();
		public LinkedList<Jump> paragraphExits = new LinkedList<Jump>();
		// END KGU#464 2017-12-03

		public SectionOrParagraph(String _name, boolean _isSection, int _startIndex, Subqueue _parentNode, SectionOrParagraph _containingSoP)
		{
			name = _name;
			isSection = _isSection;
			startsAt = _startIndex;
			parent = _parentNode;
			containedBy = _containingSoP;
		}

		public String toString()
		{
			return getClass().getSimpleName() + "(" + (this.isSection ? "SECTION " : "") + this.name + ":" + this.startsAt + ".." + this.endsBefore + ")";
		}

		// START KGU#464 2017-12-03: Bugfix #475 - these methods replace the former fields
		public Element getFirstElement()
		{
			Element element = null;
			if (this.parent != null && this.startsAt > -1 && this.startsAt < this.parent.getSize()) {
				element = this.parent.getElement(this.startsAt);
			}
			return element;
		}
		public Element getLastElement()
		{
			Element element = null;
			int lastIndex = this.endsBefore ;
			if (this.parent != null && (lastIndex == -1 || lastIndex > this.startsAt) && lastIndex <= this.parent.getSize()) {
				if (lastIndex == -1) lastIndex = this.parent.getSize();
				element = this.parent.getElement(lastIndex - 1);
			}
			return element;
		}
		public int getSize()
		{
			return (this.endsBefore < 0 ? this.parent.getSize() : this.endsBefore) - this.startsAt;
		}
		// END KGU#464 2017-12-03
	}

	// START KGU#464 2017-12-04: Bugfix #475 - Now obsolete
//	/** Visitor class responsible for updating disabled "EXIT *" elements to
//	 * "return" elements (might have become superfluous by bugfix #475)
//	 */
//	private final class JumpConverter implements IElementVisitor
//	{
//		@Override
//		public boolean visitPreOrder(Element _ele) {
//			String text = _ele.getText().getLongString();
//			if (_ele instanceof Jump && _ele.disabled &&
//					(text.equalsIgnoreCase("EXIT SECTION") || text.equalsIgnoreCase("EXIT PARAGRAPH"))) {
//				// Replace the comment by the previous text
//				_ele.setComment(text);
//				_ele.setText(getKeywordOrDefault("preReturn", "return"));
//				_ele.setColor(Color.WHITE);
//				_ele.disabled = false;
//			}
//			return true;
//		}
//		@Override
//		public boolean visitPostOrder(Element _ele) {
//			return true;
//		}
//	};
	// END KGU#464 2017-12-04
	// START KGU#475 2017-12-05: Bugfix #486 - New mechanism to enforce value return
	/**
	 * Visitor class responsible for adding the result variable name to otherwise
	 * empty return Jumps
	 */
	private final class ReturnEnforcer implements IElementVisitor
	{
		String resultVar;

		ReturnEnforcer(String _resultVar)
		{
			resultVar = _resultVar;
		}

		@Override
		public boolean visitPreOrder(Element _ele) {
			String text = _ele.getText().getLongString();
			if (_ele instanceof Jump && ((Jump)_ele).isReturn()) {
				if (text.trim().equalsIgnoreCase(getKeywordOrDefault("preReturn", "return"))) {
					_ele.setText(text + " " + resultVar);
				};
				_ele.setColor(Color.WHITE);
				_ele.disabled = false;
			}
			return true;
		}
		@Override
		public boolean visitPostOrder(Element _ele) {
			return true;
		}
	};
	// END KGU#475 2017-12-05

	/** During build phase, all detected sections and paragraphs are listed here for resolution of internal calls */
	private LinkedList<SectionOrParagraph> procedureList = new LinkedList<SectionOrParagraph>();

	private LinkedHashMap< String, LinkedList<Call> > internalCalls = new LinkedHashMap< String, LinkedList<Call> >();
	// START KGU#476 2017-12-05: Try to distinguish superfluous paragraph labels
	private LinkedHashMap< String, HashSet<Root> > internalGotos = new LinkedHashMap< String, HashSet<Root> >();
	// END KGU#476 2017-12-05

	/**
	 * Associates the name of the result variable to the respective function Root
	 */
	private HashMap<Root, String> returnMap = new HashMap<Root, String>();

	/**
	 * Registers whether a file status evaluation function diagram was added to {@link CodeParser#subRoots}.
	 */
	private boolean fileStatusFctAdded = false;
	private static final String fileStatusCaseText = "fileDescr\n0, -1\n-2\n-3\ndefault";
	private static final String[] fileStatusCodes = {"39", "35", "37", "00"};
	private static final String[] fileStatusComments = {"General failure", "File not found", "File access denied", "Ok"};

//	/**
//	 * Used to combine nested declarations within one element
//	 */
//	private Instruction previousDeclaration = null;
//	private int	previousDeclarationLevelDepth = 0;
//	private int	previousDeclarationLevelNumber = 0;

	/* (non-Javadoc)
	 * @see CodeParser#initializeBuildNSD()
	 */
	@Override
	protected void initializeBuildNSD() throws ParserCancelled
	{
		// START KGU#407 2017-10-01: Enh. #420: Configure the lookup table for comment retrieval
		this.registerStatementRuleIds(statementIds);
		// END KGU#407 2017-10-01
		externalRoot = new Root(StringList.getNew(this.sourceName + "Externals"));
		externalRoot.setInclude();
	}

	private CobTools cobTools = new CobTools();
	private CobProg currentProg = null;
	final static String STRUCTORIZER_PARTIAL = "Structorizer Partial";
	/** Remembers the declarations put to the externalRoot lest they should be defined twice */
	private HashSet<String> declaredExternals = new HashSet<String>();
	/** Remembers the declarations first put to some global Root (from {@link globaRoots}) */
	private HashMap<Root, HashSet<String>> declaredGlobals = new HashMap<Root, HashSet<String>>();
	/** Maps the CobProgs to a corresponding global Root */
	private HashMap<CobProg, Root> globalMap = new HashMap<CobProg, Root>();
	/** An includable diagram for external definitions (to be included from all sub-diagrams referring to some of the declarations) */
	private Root externalRoot = null;
	/** Maps programs and functions to their end element index of the data section */
	private HashMap<Root, Integer> dataSectionEnds = new HashMap<Root, Integer>();
	/** Maps programs and functions to the name of their includable Root representing the data shared with internal subroutines */
	private HashMap<Root, String> dataSectionIncludes = new HashMap<Root, String>();
	/** List of declared function names and aliases to distinguish them from e.g. array variables */
	private StringList functionNames = new StringList();
	// START KGU#614 2018-12-17: Enh. #631 - Flags for import of INSPECT statements
	/** Flag for loaded diagram INSPECT_TALLYING-6.nsd */
	private boolean isLoadedInspectTallying = false;
	/** Flag for loaded diagram INSPECT_REPORTING-6.nsd */
	private boolean isLoadedInspectReplacing = false;
	/** Flag for loaded diagram INSPECT_CONVERTING-5.nsd */
	private boolean isLoadedInspectConverting = false;
	// END KGU#614 2018-12-17

	private static Matcher mCopyFunction = Pattern.compile("^copy\\((.*),(.*),(.*)\\)$").matcher("");
	// START KGU#402 2019-03-07: Issue #407
	private static final Matcher STRING_MATCHER = Pattern.compile("^[HhXxZz]?([\"][^\"]*[\"]|['][^']*['])$").matcher("");
	private static final Matcher NUMBER_MATCHER = Pattern.compile("^[+-]?[0-9]+([.][0-9]*)?(E[+-]?[0-9]+)?$").matcher("");
	// END KGU#402 2019-03-07

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.parsers.CodeParser#buildNSD_R(com.creativewidgetworks.goldparser.engine.Reduction, lu.fisch.structorizer.elements.Subqueue)
	 */
	@Override
	protected void buildNSD_R(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled
	{
		// START KGU#537 2018-07-01: Enh. #553
		checkCancelled();
		// END KGU#537 2018-07-01
		if (_reduction.isEmpty()) {
			return;
		}

		// create a dummy-program because we may have only a partial source that doesn't start with program definition
		if (currentProg == null) {
			currentProg = cobTools.new CobProg(STRUCTORIZER_PARTIAL, null, false, null);
		}

		String rule = _reduction.getParent().toString();
		//System.out.println(rule);
		//String ruleHead = _reduction.getParent().getHead().toString();
		int ruleId = _reduction.getParent().getTableIndex();
		log("buildNSD_R(" + rule + ", " + _parentNode.parent + ")...\n", true);
		//System.out.println("buildNSD_R(" + rule + ", " + _parentNode.parent + ")...");

//		// FIXME KGU#464 2027-12-03: DEBUG check for issue #475
//		for (SectionOrParagraph sop: this.procedureList) {
//			if (sop.getFirstElement() == null || sop.getLastElement() == null) {
//				System.out.println("!!!" + sop + " still open!");
//			}
//		}
//		// END KGU#464 2017-12-03

		switch (ruleId) {
		case RuleConstants.PROD_PROGRAM_DEFINITION:
		case RuleConstants.PROD_FUNCTION_DEFINITION:
		{
			//System.out.println("PROD_PROGRAM_DEFINITION or PROD_FUNCTION_DEFINITION");
			Root prevRoot = root;	// Cache the original root
			root = new Root();	// Prepare a new root for the (sub)routine
			this.equipWithSourceComment(root, _reduction);
			this.addRoot(root);
			Reduction secRed = _reduction.get(1).asReduction();	// program or function id paragraph
			boolean isFunction = secRed.getParent().getTableIndex() == RuleConstants.PROD_FUNCTION_ID_PARAGRAPH_FUNCTION_ID_TOK_DOT_TOK_DOT;
			if (isFunction) {
				this.root.setProgram(false);
			}
			String content = this.getContent_R(secRed.get(2).asReduction(), "");
			// Arguments and return value will be fetched from LINKAGE rule
			if (content.startsWith("\"") || content.startsWith("\'")) {
				content = content.substring(1, content.length() - 1);
			}
			String extName = null;
			Reduction extNameRed = secRed.get(3).asReduction();
			if (extNameRed.size() >= 1) {
				extName = this.getContent_R(extNameRed.get(1).asReduction(), "");
			}
			root.setText(content);

			// check if we still have an empty COBOL view of program
			if (currentProg.getName().equals(STRUCTORIZER_PARTIAL)) {
				if (currentProg.isEmtpy()) {
					currentProg = null;
				}
			}
			// create COBOL view of program
			currentProg = cobTools.new CobProg(content, extName, isFunction, currentProg);

			if (_reduction.get(4).getType() == SymbolType.NON_TERMINAL)
			{
				// build the program body into the new root
				buildNSD_R(_reduction.get(4).asReduction(), root.children);
			}
			// Restore the original root
			root = prevRoot;
		}
		break;
//			case RuleConstants.PROD_PROGRAM_ID_PARAGRAPH_PROGRAM_ID_TOK_DOT_TOK_DOT:
//			case RuleConstants.PROD_FUNCTION_ID_PARAGRAPH_FUNCTION_ID_TOK_DOT_TOK_DOT:
//			{
//			}
//			break;
		// START KGU 2017-10-20 To register function names should help to tell them from array variables
		case RuleConstants.PROD_REPOSITORY_NAME_FUNCTION:
		{
			//System.out.println("PROD_REPOSITORY_NAME_FUNCTION");
			String fctName = getWord(_reduction.get(1));
			this.functionNames.addIfNew(fctName);
			Reduction asRed = _reduction.get(2).asReduction();
			if (asRed.size() > 0) {
				fctName = this.getContent_R(asRed.get(1).asReduction(), "").trim();
				if (fctName.startsWith("\"") && fctName.endsWith("\"")) {
					fctName = fctName.substring(1, fctName.length()-1);
				}
				this.functionNames.addIfNew(fctName);
			}
		}
		break;
		case RuleConstants.PROD__PROCEDURE_USING_CHAINING_USING:
		{
			//System.out.println("PROD__PROCEDURE_USING_CHAINING_USING");
			//String arguments = this.getContent_R(_reduction.get(1).asReduction(), "").trim();
			//root.setText(root.getText().getLongString() + "(" + arguments + ")");
			StringList arguments = this.getParameterList(_reduction.get(1).asReduction(), "<procedure_param_list>", RuleConstants.PROD_PROCEDURE_PARAM, 3);
			if (arguments.count() > 0) {
				for (int i = 0; i < arguments.count(); i++) {
					String varName = arguments.get(i);
					// START KGU#465 2017-12-04: Bugfix #473
					//String type = CobTools.getTypeString(currentProg.getCobVar(varName), false);
					String type = null;
					CobVar var = currentProg.getCobVar(varName);	// Variables retrieved with COPY may be missing
					if (var != null) {
						type = CobTools.getTypeName(currentProg.getCobVar(varName), true);
					}
					// END KGU#465 2017-12-04
					if (type != null) {
						arguments.set(i, type + " " + varName) ;
					}
				}
				root.setText(root.getText().getLongString() + "(" + arguments.concatenate(", ") + ")");
				root.setProgram(false);
			}
		}
		break;
		case RuleConstants.PROD__PROCEDURE_RETURNING_RETURNING:
		{
			// Debug....
			String resultVar = this.getContent_R(_reduction.get(1).asReduction(), "");
			this.returnMap.put(root, resultVar);
			StringList rootText = root.getText();
			// START KGU#465 2017-12-04: Bugfix #473
			//String resultType = CobTools.getTypeString(currentProg.getCobVar(resultVar), false);
			String resultType = CobTools.getTypeName(currentProg.getCobVar(resultVar), true);
			// END KGU#564 2017-12-04
			if (resultType != null) {
				// START KGU#475 2017-12-05: Bugfix #486
				if (!root.getParameterNames().contains(resultVar) && this.optionImportVarDecl) {
					Instruction decl = new Instruction("var " + resultVar + ": " + resultType);
					decl.setComment("Result variable");
					decl.setColor(colorDecl);
					_parentNode.addElement(decl);
				}
				// END KGU#475 2017-12-05
				if (rootText.count() >= 1
					&& rootText.getLongString().trim().endsWith(")")) {
					rootText.set(rootText.count()-1, rootText.get(rootText.count()-1) + ": " + resultType);
				}
			}
		}
		break;
		case RuleConstants.PROD_SECTION_HEADER_SECTION_TOK_DOT:
		{
			// <section_header> ::= <WORD> SECTION <_segment> 'TOK_DOT' <_use_statement>
			// Note: this starts a new section AND is the only way (despite of END PROGRAM / EOF)
			//       to close the previous section and the previous paragraph
			accomplishPrevSoP(_parentNode, SoPTarget.SOP_SECTION);

			String name = this.getContent_R(_reduction.get(0).asReduction(), "").trim();
			// We ignore segment number (if given) and delaratives i.e. <_use_statemant>

			// add to NSD
			Call sec = new Call(name);
			sec.disabled = true;
			_parentNode.addElement(this.equipWithSourceComment(sec, _reduction));
			sec.getComment().insert("Definition of section " + name, 0);

			// add to procedureList for later handling
			addProcedureToList(_parentNode, name, true);
		}
		break;
		case RuleConstants.PROD_PARAGRAPH_HEADER_TOK_DOT:
		{
			// <paragraph_header> ::= <IntLiteral or WORD> 'TOK_DOT'
			// Note: this starts a new paragraph AND closes the previous paragraph (if existent)
			// START KGU#464 2017-11-27: Bugfix #475
			accomplishPrevSoP(_parentNode, SoPTarget.SOP_PARAGRAPH);
			// END KGU#464 2017-11-27

			String name = this.getContent_R(_reduction.get(0).asReduction(), "").trim();

			// add to NSD
			Call par = new Call(name);
			par.disabled = true;
			_parentNode.addElement(this.equipWithSourceComment(par, _reduction));
			par.getComment().insert("Definition of paragraph " + name, 0);

			// add to procedureList for later handling
			if (Character.isDigit(name.charAt(0))) {
				name = "sub" + name;
			}
			addProcedureToList(_parentNode, name, false);
		}
		break;
//			deactivated as we get an ArrayIndexOutOfBoundsException sometimes
		case RuleConstants.PROD_PROCEDURE_TOK_DOT:	// <procedure> ::= <statements> 'TOK_DOT'
			// First process the statements then handle the TOK_DOT with the subsequent case
			buildNSD_R(_reduction.get(0).asReduction(), _parentNode);
			// No break; here!
		case RuleConstants.PROD_PROCEDURE_TOK_DOT2:	// This case should never occur since the rule is supposed to fall through to TOK-DOT!
			// FIXME: Remove this DEBUG logging!
			if (ruleId == RuleConstants.PROD_PROCEDURE_TOK_DOT2) this.log("===> PROD_PROCEDURE_TOK_DOT2\n", false);
			// START KGU#464 2017-12-03: Bugfix #475
			// DON't close the last unsatisfied "procedure" here unless a SECTION ends (which is detected otherwise)
			//accomplishPrevSoP(_parentNode, SoPTarget.SOP_ANY);
			// END KGU#464 2017-12-03
			break;
		case RuleConstants.PROD_IF_STATEMENT_IF:
			//System.out.println("PROD_IF_STATEMENT_IF");
			this.importIf(_reduction, _parentNode);
			break;
		case RuleConstants.PROD_EVALUATE_STATEMENT_EVALUATE:
			//System.out.println("PROD_EVALUATE_STATEMENT_EVALUATE");
			this.importEvaluate(_reduction, _parentNode);
			break;
		case RuleConstants.PROD_PERFORM_STATEMENT_PERFORM:
			//System.out.println("PROD_PERFORM_STATEMENT_PERFORM");
			this.importPerform(_reduction, _parentNode);
			break;
		case RuleConstants.PROD_MOVE_STATEMENT_MOVE:
			//System.out.println("PROD_MOVE_STATEMENT_MOVE");
			this.importMove(_reduction, _parentNode);
			break;
		case RuleConstants.PROD_SET_STATEMENT_SET:
		{
			//System.out.println("PROD_SET_STATEMENT_SET");
			if (!importSet(_reduction, _parentNode)) {
				String content = this.getContent_R(_reduction, "");
				Instruction instr = new Instruction(content);
				instr.setColor(Color.RED);
				instr.disabled = true;
				_parentNode.addElement(this.equipWithSourceComment(instr, _reduction));
				instr.getComment().add("This statement cannot be converted into a sensible diagram element!");
			}
		}
		break;
		case RuleConstants.PROD_COMPUTE_STATEMENT_COMPUTE:
		{
			//System.out.println("PROD_COMPUTE_STATEMENT_COMPUTE");
			Reduction secRed = _reduction.get(1).asReduction();
			StringList targets = this.getExpressionList(secRed.get(0).asReduction(), "<arithmetic_x_list>", RuleConstants.PROD_TARGET_X_COMMA_DELIM);
			String expr = this.getContent_R(secRed.get(2).asReduction(), "");
			if (targets.count() > 0) {
				StringList content = StringList.getNew(targets.get(0) + " <- " + expr);
				for (int i = 1; i < targets.count(); i++) {
					// FIXME Caution: target.get(0) might be a "reference modification"!
					content.add(targets.get(i) + " <- " + targets.get(0));
				}
				_parentNode.addElement(this.equipWithSourceComment(new Instruction(content), _reduction));
			}
		}
		break;
		case RuleConstants.PROD_ADD_STATEMENT_ADD:
			//System.out.println("PROD_ADD_STATEMENT_ADD");
			this.importAdd(_reduction, _parentNode);
			break;
		case RuleConstants.PROD_SUBTRACT_STATEMENT_SUBTRACT:
			//System.out.println("PROD_SUBTRACT_STATEMENT_SUBTRACT");
			this.importSubtract(_reduction, _parentNode);
			break;
		case RuleConstants.PROD_MULTIPLY_STATEMENT_MULTIPLY:
			//System.out.println("PROD_MULTIPLY_STATEMENT_MULTIPLY");
			this.importMultiply(_reduction, _parentNode);
			break;
		case RuleConstants.PROD_DIVIDE_STATEMENT_DIVIDE:
			//System.out.println("PROD_DIVIDE_STATEMENT_DIVIDE");
			this.importDivide(_reduction, _parentNode);
			break;
		case RuleConstants.PROD_ACCEPT_STATEMENT_ACCEPT:
		{
			// Input instruction
			//System.out.println("PROD_ACCEPT_STATEMENT_ACCEPT");
			if (!this.importAccept(_reduction, _parentNode)) {
				Instruction dummy = new Instruction(this.getContent_R(_reduction, ""));
				dummy.setColor(Color.RED);
				dummy.disabled = true;
				_parentNode.addElement(this.equipWithSourceComment(dummy, _reduction));
				dummy.getComment().add(StringList.explode("An import for this kind of ACCEPT instruction is not implemented:\n"
						+ this.getOriginalText(_reduction, ""), "\n"));
			}
		}
		break;
		case RuleConstants.PROD_DISPLAY_STATEMENT_DISPLAY:
		{
			// Output instruction
			//System.out.println("PROD_DISPLAY_STATEMENT_DISPLAY");
			// TODO: Identify whether fileAPI s to be used.
			Reduction secRed = _reduction.get(1).asReduction();	// display body
			// TODO: Define a specific routine to extract the expressions
			//String content = this.appendDisplayBody(secRed, "");
			String content = this.getContent_R(secRed, "", ", ");	// This is only a quick hack!
			if (content.startsWith(", ")) {
				content = content.substring(2);
			}
			if (content.endsWith(", ")) {
				content = content.substring(0,  content.length()-2);
			}
			content = CodeParser.getKeyword("output") + " " + content;
			Instruction instr = new Instruction(content);
			_parentNode.addElement(this.equipWithSourceComment(instr, _reduction));
		}
		break;
		case RuleConstants.PROD_FILE_CONTROL_ENTRY_SELECT:
			this.importFileControl(_reduction, _parentNode);
			break;
		case RuleConstants.PROD_FILE_DESCRIPTION:
		{
			//System.out.println("PROD_FILE_DESCRIPTION");
			Reduction fdRed = _reduction.get(0).asReduction();	// <file_description_enry>
			Reduction reclRed = _reduction.get(1).asReduction();	// <_record_description_list>
			// TODO map the record names (if any) to the file descriptor name (and vice versa?)
			// for READ and WRITE statements
			if (this.getContent_R(fdRed.get(0).asReduction(), "").trim().equalsIgnoreCase("fd")
					&& reclRed.getParent().getTableIndex() != RuleConstants.PROD__RECORD_DESCRIPTION_LIST) {
				String fdName = this.getContent_R(fdRed.get(1).asReduction(), "").trim();
				// The file descriptor should have been declared with the SELECT clause
				do {
					Reduction datRed = reclRed.get(0).asReduction();
					if (reclRed.getParent().getTableIndex() == RuleConstants.PROD_RECORD_DESCRIPTION_LIST_TOK_DOT2) {
						datRed = reclRed.get(1).asReduction();
						reclRed = reclRed.get(0).asReduction();
					}
					else {
						reclRed = null;
					}
					HashMap<String, String> typeMap = new HashMap<String, String>();
					// START KGU 2017-05-24: We do not only want the type info here but also create declarations
					//this.processDataDescriptions(datRed, null, typeMap);
					this.processDataDescriptions(datRed, typeMap);
					// END KGU 2017-05-24
					for (String recName: typeMap.keySet()) {
						currentProg.fileRecordMap.put(recName, fdName);
					}
				} while (reclRed != null);
			}
		}
		break;
		case RuleConstants.PROD_OPEN_STATEMENT_OPEN:
		{
			//System.out.println("PROD_OPEN_STATEMENT_OPEN");
			if (!this.importOpen(_reduction, _parentNode)) {
				String content = this.getOriginalText(_reduction, "");
				Instruction instr = new Instruction(content);
				instr.setColor(Color.RED);
				_parentNode.addElement(this.equipWithSourceComment(instr, _reduction));
				instr.getComment().add("TODO: there is still no automatic conversion for this statement");
			}
		}
		break;
		case RuleConstants.PROD_START_STATEMENT_START:
		{
			//System.out.println("PROD_START_STATEMENT_START
			String content = this.getOriginalText(_reduction, "");
			Instruction instr = new Instruction(content);
			instr.setColor(Color.RED);
			_parentNode.addElement(this.equipWithSourceComment(instr, _reduction));
			instr.getComment().add("TODO: Structorizer File API does not support indexed or other non-text files");
		}
		break;
		case RuleConstants.PROD_READ_STATEMENT_READ:
		{
			//System.out.println("PROD_READ_STATEMENT_READ");
			if (!this.importRead(_reduction, _parentNode)) {
				String content = this.getOriginalText(_reduction, "");
				Instruction instr = new Instruction(content);
				instr.setColor(Color.RED);
				_parentNode.addElement(this.equipWithSourceComment(instr, _reduction));
				instr.getComment().add("TODO: there is still no automatic conversion for this statement");
			}
		}
		break;
		case RuleConstants.PROD_WRITE_STATEMENT_WRITE:
		{
			//System.out.println("PROD_WRITE_STATEMENT_WRITE");
			if (!this.importWrite(_reduction, _parentNode)) {
				String content = this.getOriginalText(_reduction, "");
				Instruction instr = new Instruction(content);
				instr.setColor(Color.RED);
				_parentNode.addElement(this.equipWithSourceComment(instr, _reduction));
				instr.getComment().add("TODO: there is still no automatic conversion for this statement");
			}
		}
		break;
		case RuleConstants.PROD_REWRITE_STATEMENT_REWRITE:
		case RuleConstants.PROD_DELETE_STATEMENT_DELETE:
		{
			String content = this.getOriginalText(_reduction, "");
			Instruction instr = new Instruction(content);
			instr.setColor(Color.RED);
			_parentNode.addElement(this.equipWithSourceComment(instr, _reduction));
			instr.getComment().add("TODO: Structorizer File API does not support indexed or other non-text files");
		}
		break;
		case RuleConstants.PROD_CLOSE_STATEMENT_CLOSE:
		{
			//System.out.println("PROD_CLOSE_STATEMENT_CLOSE");
			String fileDescr = this.getContent_R(_reduction.get(1).asReduction().get(0).asReduction(), "").trim();
			Instruction instr = null;
			if (currentProg.fileMap.containsKey(fileDescr)) {
				instr = new Instruction("fileClose(" + fileDescr + ")");
				this.equipWithSourceComment(instr, _reduction);
			}
			else {
				instr = new Instruction(this.getOriginalText(_reduction, ""));
				instr.setColor(Color.RED);
				this.equipWithSourceComment(instr, _reduction);
				instr.getComment().add("TODO: there is still no automatic conversion for this statement");
			}
			if (instr != null) {
				_parentNode.addElement(instr);
			}
		}
		break;
		case RuleConstants.PROD_CALL_STATEMENT_CALL:
			this.importCall(_reduction, _parentNode);
			break;
		case RuleConstants.PROD_EXIT_STATEMENT_EXIT:
			//System.out.println("PROD_EXIT_STATEMENT_EXIT");
			this.importExit(_reduction, _parentNode);
			break;
		case RuleConstants.PROD_GOBACK_STATEMENT_GOBACK:
		{
			//System.out.println("PROD_GOBACK_STATEMENT_GOBACK");
			Reduction secRed = _reduction.get(1).asReduction();
			String content = CodeParser.getKeywordOrDefault("preReturn", "return");
			if (secRed.getParent().getTableIndex() == RuleConstants.PROD_EXIT_PROGRAM_RETURNING2) {
				content = this.getContent_R(secRed.get(1).asReduction(), content + " ");
			}
			_parentNode.addElement(this.equipWithSourceComment(new Jump(content.trim()), _reduction));
		}
		break;
		case RuleConstants.PROD_STOP_STATEMENT_STOP:
		case RuleConstants.PROD_STOP_STATEMENT_STOP_RUN:
		{
			//System.out.println("PROD_STOP_STATEMENT_STOP[_RUN]");
			int contentIx = (ruleId == RuleConstants.PROD_STOP_STATEMENT_STOP) ? 1 : 2;
			Reduction secRed = _reduction.get(contentIx).asReduction();
			String content = CodeParser.getKeywordOrDefault("preExit", "exit");
			String exitVal = this.getContent_R(secRed, "").trim();
			if (exitVal.isEmpty()) exitVal = "0";
			_parentNode.addElement(this.equipWithSourceComment(new Jump(content + " " + exitVal), _reduction));
		}
		break;
		case RuleConstants.PROD_GOTO_STATEMENT_GO:
		{
			//System.out.println("PROD_GOTO_STATEMENT_GO");
			String content = this.getContent_R(_reduction.get(1).asReduction(), "").trim();
			if (content.toUpperCase().startsWith("TO ")) {
				content = content.substring(3);
			}
			// START KGU#476 2017-12-05
			String contentLower = content.toLowerCase();
			if (!this.internalGotos.containsKey(contentLower)) {
				this.internalGotos.put(content, new HashSet<Root>());
			}
			this.internalGotos.get(content).add(root);
			// END KGU#476 2017-12-05
			Jump jmp = new Jump("goto " + content);
			jmp.setColor(Color.RED);
			_parentNode.addElement(this.equipWithSourceComment(jmp, _reduction));
			jmp.getComment().add("GO TO statements are not supported in structured programming!");
		}
		break;
		case RuleConstants.PROD_STRING_STATEMENT_STRING:
		{
			//System.out.println("PROD_STRING_STATEMENT_STRING");
			if (!this.importString(_reduction, _parentNode)) {
				String content = this.getOriginalText(_reduction, "");
				Instruction instr = new Instruction(content);
				instr.setColor(Color.RED);
				instr.disabled = true;
				_parentNode.addElement(this.equipWithSourceComment(instr, _reduction));
				instr.getComment().add("TODO: there is still no automatic conversion for this statement");
			}
		}
		break;
		case RuleConstants.PROD_UNSTRING_STATEMENT_UNSTRING:
		{
			//System.out.println("PROD_UNSTRING_STATEMENT_UNSTRING");
			if (!this.importUnstring(_reduction, _parentNode)) {
				String content = this.getOriginalText(_reduction, "");
				Instruction instr = new Instruction(content);
				instr.setColor(Color.RED);
				_parentNode.addElement(this.equipWithSourceComment(instr, _reduction));
				instr.getComment().add("TODO: there is still no automatic conversion for this statement");
			}
		}
		break;
		// START KGU#614 2018-12-14: Enh. #631 - INSPECT implementation
		case RuleConstants.PROD_INSPECT_STATEMENT_INSPECT:
			//System.out.println("PROD_INSPECT_STATEMENT_INSPECT");
			importInspect(_reduction, _parentNode);
			break;
		// END KGU#614 2018-12-14
		case RuleConstants.PROD_SEARCH_STATEMENT_SEARCH:
			//System.out.println("PROD_SEARCH_STATEMENT_SEARCH");
			importSearch(_reduction, _parentNode);
			break;
		case RuleConstants.PROD__WORKING_STORAGE_SECTION_WORKING_STORAGE_SECTION_TOK_DOT:
		{
			currentProg.setCurrentStorage(CobTools.Storage.STORAGE_WORKING);
			this.processDataDescriptions(_reduction.get(3).asReduction(), null);
			// FIXME! TEST ONLY - provide the correct diagram Subqueues!
			this.buildDataSection(currentProg.getWorkingStorage(), _parentNode);
		}
		break;
		case RuleConstants.PROD__LOCAL_STORAGE_SECTION_LOCAL_STORAGE_SECTION_TOK_DOT:
		{
			currentProg.setCurrentStorage(CobTools.Storage.STORAGE_LOCAL);
			this.processDataDescriptions(_reduction.get(3).asReduction(), null);
			// FIXME! TEST ONLY
			this.buildDataSection(currentProg.getLocalStorage(), _parentNode);
		}
		break;
		case RuleConstants.PROD__LINKAGE_SECTION_LINKAGE_SECTION_TOK_DOT:
		{
			currentProg.setCurrentStorage(CobTools.Storage.STORAGE_LINKAGE);
			this.processDataDescriptions(_reduction.get(3).asReduction(), null);
			// START KGU#465 2017-12-04: Bugfix #473 - produce an includable diagram for record definitions
			boolean hasRecordTypes = false;
			CobVar arg = currentProg.getLinkageStorage();
			while (arg != null && !hasRecordTypes) {
				getLogger().log(Level.CONFIG, "{0}: {1}",
						new Object[]{arg.getName(), arg.deriveTypeName()});
				if (arg.hasChild()) {
					hasRecordTypes = true;
				}
				arg = arg.getSister();
			}
			if (hasRecordTypes) {
				// FIXME (KGU 2017-12-04): The lacking type support in COBOL forces us to unify types in a postprocess
				Root incl = new Root();
				incl.setText(root.getMethodName() + "_ArgTypes");
				incl.setComment("Argument type definitions for routine " + root.getMethodName());
				incl.setInclude();
				this.buildDataSection(currentProg.getLinkageStorage(), incl.children);
				int i = 0;
				while (i < incl.children.getSize()) {
					Element el = incl.children.getElement(i);
					if (!(el instanceof Instruction) || !(((Instruction)el).isTypeDefinition() || ((Instruction)el).getText().get(0).startsWith("const "))) {
						incl.children.removeElement(i);
					}
					else i++;
				}
				if (incl.children.getSize() > 0) {
					this.addRoot(incl);
					root.addToIncludeList(incl);
				}
			}
			// END KGU#465 2017-12-04
		}
		break;
		case RuleConstants.PROD__FILE_SECTION_HEADER_TOK_FILE_SECTION_TOK_DOT:
		{
			// just set the storage here, the other parts are done in PROD_FILE_DESCRIPTION
			currentProg.setCurrentStorage(CobTools.Storage.STORAGE_FILE);
		}
		break;
		case RuleConstants.PROD__REPORT_SECTION_REPORT_SECTION_TOK_DOT:
		{
			currentProg.setCurrentStorage(CobTools.Storage.STORAGE_REPORT);
			// TODO this is unlikely to work - take care later, ignore for now
			//this.processDataDescriptions(_reduction.get(3).asReduction(), _parentNode, null);
		}
		break;
		case RuleConstants.PROD__SCREEN_SECTION_SCREEN_SECTION_TOK_DOT:
		{
			currentProg.setCurrentStorage(CobTools.Storage.STORAGE_SCREEN);
			// TODO this is unlikely to work - take care later, ignore for now
			//this.processDataDescriptions(_reduction.get(3).asReduction(), _parentNode, null);
		}
		break;
		default:
			if (_reduction.size()>0)
			{
				for(int i=0; i<_reduction.size(); i++)
				{
					if (_reduction.get(i).getType() == SymbolType.NON_TERMINAL)
					{
						buildNSD_R(_reduction.get(i).asReduction(), _parentNode);
					}
				}
			}
		}
	}

	// START KGU#614 2018-12-14: Issue #631
	/**
	 * Imports an INSPECT statement into some kind of augmented {@link Call} element
	 * @param _reduction - The associated {@link Reduction}
	 * @param _parentNode - The {@link Subqueue} to which the element(s) are to be appended
	 * @throws ParserCancelled on user abort
	 */
	private void importInspect(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled {
		Reduction redBody = _reduction.get(1).asReduction();
		String target = getContent_R(redBody.get(0).asReduction(), "");
		redBody = redBody.get(1).asReduction();
		boolean built = false;
		int idBody = redBody.getParent().getTableIndex();
		switch (idBody) {
		case RuleConstants.PROD_INSPECT_LIST:
			// We must construct two builds in series (tallying + replacing)
			built = importInspectTallying(target, redBody.get(0).asReduction().get(1).asReduction(), _reduction, _parentNode);
			built = built && importInspectReplacing(target, redBody.get(1).asReduction().get(1).asReduction(), _reduction, _parentNode);
			break;
		case RuleConstants.PROD_INSPECT_TALLYING_TALLYING:
			built = importInspectTallying(target, redBody.get(1).asReduction(), _reduction, _parentNode);
			break;
		case RuleConstants.PROD_INSPECT_REPLACING_REPLACING:
			built = importInspectReplacing(target, redBody.get(1).asReduction(), _reduction, _parentNode);
			break;
		case RuleConstants.PROD_INSPECT_CONVERTING_CONVERTING_TO:
			built = importInspectConverting(target, redBody, _reduction, _parentNode);
			break;
		}
		// Fallback for the case the specific routine failed to create an equivalent
		if (!built) {
			String content = this.getContent_R(_reduction, "");
			Instruction instr = new Instruction(content);
			instr.disabled = true;
			instr.setColor(Color.RED);
			_parentNode.addElement(equipWithSourceComment(instr, _reduction));
			instr.getComment().add("Import of INSPECT statements hasn't been implemented yet!");
		}
	}

	private boolean importInspectTallying(String _target, Reduction _redBody, Reduction _redInspect, Subqueue _parentNode) throws ParserCancelled {
		boolean isDone = false;
		if (!isLoadedInspectTallying) {
			// START KGU#636 2019-01-18: Bugfix #665
			//try {
			//	URL diagrURL = this.getClass().getResource("INSPECT_TALLYING-6.nsd");
			//	if (diagrURL != null) {
			//		File f = new File(diagrURL.getPath());
			//		NSDParser parser = new NSDParser();
			//		Root sub = parser.parse(f);
			//		sub.origin = f.getPath();
			//		this.addRoot(sub);
			//		isLoadedInspectTallying = true;
			//	}
			//}
			//catch (Exception ex) {
			//	this.log(ex.toString(), true);
			//	this.getLogger().warning(ex.toString());
			//	return false;
			//}
			if (parseResourceDiagram("INSPECT_TALLYING-6.nsd")) {
				isLoadedInspectTallying = true;
			}
			else {
				return false;
			}
			// END KGU#636 2019-01-18
		}
		StringList counters = new StringList();
		StringList modes = new StringList();
		StringList subjects = new StringList();
		StringList afters = new StringList();
		StringList befores = new StringList();
		while (_redBody != null) {
			Reduction redItem = _redBody;
			if (_redBody.getParent().getTableIndex() == RuleConstants.PROD_TALLYING_LIST2) {
				redItem = _redBody.get(1).asReduction();
				_redBody = _redBody.get(0).asReduction();
			}
			else {
				_redBody = null;
			}
			switch (redItem.getParent().getTableIndex()) {
			case RuleConstants.PROD_TALLYING_ITEM_CHARACTERS:
				modes.add("\"CHARACTERS\"");
				subjects.add("\"\"");
				addInspectRegions(redItem.get(1).asReduction(), afters, befores);
				break;
			case RuleConstants.PROD_TALLYING_ITEM_ALL:
			case RuleConstants.PROD_TALLYING_ITEM_LEADING:
			case RuleConstants.PROD_TALLYING_ITEM_TRAILING:
				modes.add(redItem.toString().replace('[', '"').replace(']', '"'));
				break;
			case RuleConstants.PROD_TALLYING_ITEM_FOR:
			{
				/* Several imtems might be associated to a single counter like in
				 * COUNT-0 FOR ALL "AB", ALL "D"
				 */
				String cnt = getContent_R(redItem.get(0).asReduction(), "");
				while (counters.count() < subjects.count()) {
					counters.insert(cnt, 0);
				}
			}
				break;
			case RuleConstants.PROD_TALLYING_ITEM:
				subjects.add(getContent_R(redItem.get(0).asReduction(), ""));
				addInspectRegions(redItem.get(1).asReduction(), afters, befores);
			}
		}
		int nClauses = counters.count();
		if (nClauses == modes.count() && nClauses == subjects.count() && nClauses == afters.count() && nClauses == befores.count()) {
			Call call = new Call("");
			String hash = Integer.toHexString(call.hashCode());
			String counter = "counts" + hash;
			StringList content = new StringList();
			content.add("INSPECT_TALLYING(" + _target + ",\\");
			content.add(counter + ",\\");
			content.add("{" + modes.reverse().concatenate(", ") + "},\\");
			content.add("{" + subjects.reverse().concatenate(", ") + "},\\");
			content.add("{" + afters.reverse().concatenate(", ") + "},\\");
			content.add("{" + befores.reverse().concatenate(", ") + "})");
			call.setText(content);
			call.setColor(colorMisc);
			Element el = new Instruction(counter + "[" + (counters.count() - 1) + "] <- 0");
			el.setColor(colorMisc);
			_parentNode.addElement(el);
			_parentNode.addElement(this.equipWithSourceComment(call, _redInspect));
			Set<String> counterVars = new HashSet<String>();
			for (int i = 0; i < counters.count(); i++) {
				counterVars.add(counters.get(i));
			}
			content = new StringList();
			for (int i = 0; i < counters.count(); i++) {
				counterVars.add(counters.get(i));
			}
			for (String var: counterVars) {
				String line = var + " <- " + var;
				for (int i = 0; i < counters.count(); i++) {
					if (var.equals(counters.get(i))) {
						line += " + " + counter + "[" + i + "]";
					}
				}
				content.add(line);
			}
			el = new Instruction(content);
			el.setColor(colorMisc);
			_parentNode.addElement(el);
			isDone = true;
		}
		return isDone;
	}

	private boolean importInspectReplacing(String _target, Reduction _redBody, Reduction _redInspect, Subqueue _parentNode) throws ParserCancelled {
		boolean isDone = false;
		if (!isLoadedInspectReplacing) {
			// START KGU#636 2019-01-18: Bugfix #665
			//try {
			//	URL diagrURL = this.getClass().getResource("INSPECT_REPLACING-6.nsd");
			//	if (diagrURL != null) {
			//		File f = new File(diagrURL.getPath());
			//		NSDParser parser = new NSDParser();
			//		Root sub = parser.parse(f);
			//		sub.origin = f.getPath();
			//		this.addRoot(sub);
			//		isLoadedInspectReplacing = true;
			//	}
			//}
			//catch (Exception ex) {
			//	this.log(ex.toString(), true);
			//	this.getLogger().warning(ex.toString());
			//	return false;
			//}
			if (parseResourceDiagram("INSPECT_REPLACING-6.nsd")) {
				isLoadedInspectReplacing = true;
			}
			else {
				return false;
			}
			// END KGU#636 2019-01-18
		}
		StringList modes = new StringList();
		StringList subjects = new StringList();
		StringList replacers = new StringList();
		StringList afters = new StringList();
		StringList befores = new StringList();
		while (_redBody != null) {
			Reduction redItem = _redBody;
			if (_redBody.getParent().getTableIndex() == RuleConstants.PROD_REPLACING_LIST2) {
				redItem = _redBody.get(1).asReduction();
				_redBody = _redBody.get(0).asReduction();
			}
			else {
				_redBody = null;
			}
			switch (redItem.getParent().getTableIndex()) {
			case RuleConstants.PROD_REPLACING_ITEM_CHARACTERS_BY:
				modes.add("\"CHARACTERS\"");
				subjects.add("\"\"");
				replacers.add(getContent_R(redItem.get(2).asReduction(), ""));
				addInspectRegions(redItem.get(3).asReduction(), afters, befores);
				break;
			case RuleConstants.PROD_REPLACING_ITEM:
				switch (redItem.get(0).asReduction().getParent().getTableIndex()) {
				case RuleConstants.PROD_REP_KEYWORD_ALL:
				case RuleConstants.PROD_REP_KEYWORD_FIRST:
				case RuleConstants.PROD_REP_KEYWORD_LEADING:
				case RuleConstants.PROD_REP_KEYWORD_TRAILING:
				{
					/* Several imtems might be associated to a single mode like in
					 * ALL "AB" BY "XY", "D" BY "X"
					 */
					String mode = redItem.get(0).asString().replace('[', '"').replace(']', '"');
					while (modes.count() < subjects.count()) {
						modes.add(mode);
					}
				}
					modes.add(redItem.get(0).asString().replace('[', '"').replace(']', '"'));
					break;
				default:
					while (modes.count() < subjects.count()) {
						modes.add("\"ALL\"");
					}
				}
				redItem = redItem.get(1).asReduction();
				subjects.add(getContent_R(redItem.get(0).asReduction(), ""));
				replacers.add(getContent_R(redItem.get(2).asReduction(), ""));
				addInspectRegions(redItem.get(3).asReduction(), afters, befores);
				break;
			}
		}
		int nClauses = modes.count();
		if (nClauses == replacers.count() && nClauses == subjects.count() && nClauses == afters.count() && nClauses == befores.count()) {
			StringList content = new StringList();
			content.add(_target + " <- INSPECT_REPLACING(" + _target + ",\\");
			content.add("{" + modes.reverse().concatenate(", ") + "},\\");
			content.add("{" + subjects.reverse().concatenate(", ") + "},\\");
			content.add("{" + replacers.reverse().concatenate(", ") + "},\\");
			content.add("{" + afters.reverse().concatenate(", ") + "},\\");
			content.add("{" + befores.reverse().concatenate(", ") + "})");
			Call call = new Call(content);
			call.setColor(colorMisc);
			_parentNode.addElement(this.equipWithSourceComment(call, _redInspect));
			isDone = true;
		}
		return isDone;
	}

	private boolean importInspectConverting(String _target, Reduction _redBody, Reduction _redInspect, Subqueue _parentNode) throws ParserCancelled {
		boolean isDone = false;
		if (!isLoadedInspectConverting) {
			// START KGU#636 2019-01-18: Bugfix #665
			//try {
			//	URL diagrURL = this.getClass().getResource("INSPECT_CONVERTING-5.nsd");
			//	if (diagrURL != null) {
			//		File f = new File(diagrURL.getPath());
			//		NSDParser parser = new NSDParser();
			//		Root sub = parser.parse(f);
			//		sub.origin = f.getPath();
			//		this.addRoot(sub);
			//		isLoadedInspectConverting = true;
			//	}
			//}
			//catch (Exception ex) {
			//	this.log(ex.toString(), true);
			//	this.getLogger().warning(ex.toString());
			//	return false;
			//}
			if (parseResourceDiagram("INSPECT_CONVERTING-5.nsd")) {
				isLoadedInspectConverting = true;
			}
			else {
				return false;
			}
			// END KGU#636 2019-01-18
		}
		String subjects = getContent_R(_redBody.get(1).asReduction(), "");
		String replacers = getContent_R(_redBody.get(3).asReduction(), "");
		StringList afters = new StringList();
		StringList befores = new StringList();
		addInspectRegions(_redBody.get(4).asReduction(), afters, befores);
		StringList content = new StringList();
		content.add(_target + " <- INSPECT_CONVERTING(" + _target + ",\\");
		content.add(subjects + ",\\");
		content.add(replacers + ",\\");
		content.add(afters.get(0) + ",\\");
		content.add(befores.get(0) + ")");
		Call call = new Call(content);
		call.setColor(colorMisc);
		_parentNode.addElement(this.equipWithSourceComment(call, _redInspect));
		isDone = true;
		return isDone;
	}

	// START KGU#636 2019-01-18: Bugfix #665
	/**
	 * Tries to parse the diagram resource specified by {@code filename} (which should be
	 * a flat name bound to the class resource location).
	 * @param filename - the file name of an NSD file in the resource envirmonment of this class.
	 * @return true if the parsing succeeded, false otherwise
	 */
	private boolean parseResourceDiagram(String filename)
	{
		boolean done = false;
		try {
			URL diagrURL = this.getClass().getResource(filename);
			if (diagrURL != null) {
				InputStream is = this.getClass().getResourceAsStream(filename);
				NSDParser parser = new NSDParser();
				Root sub = parser.parse(is);
				sub.origin = diagrURL.toString();
				this.addRoot(sub);
				is.close();
				done = true;
			}
		}
		catch (Exception ex) {
			this.log(ex.toString(), true);
			this.getLogger().warning(ex.toString());
		}
		return done;
	}

	private void addInspectRegions(Reduction redRegion, StringList afters, StringList befores) throws ParserCancelled {
		// Region may be empty or have AFTER clause or BEFORE clause or both
		switch (redRegion.getParent().getTableIndex()) {
		case RuleConstants.PROD_INSPECT_REGION4:
			afters.add(getContent_R(redRegion.get(1).asReduction().get(2).asReduction(),""));
			befores.add(getContent_R(redRegion.get(0).asReduction().get(2).asReduction(),""));
			break;
		case RuleConstants.PROD_INSPECT_REGION5:
			afters.add(getContent_R(redRegion.get(0).asReduction().get(2).asReduction(),""));
			befores.add(getContent_R(redRegion.get(1).asReduction().get(2).asReduction(),""));
			break;
		case RuleConstants.PROD_INSPECT_BEFORE_BEFORE:
			afters.add("\"\"");
			befores.add(getContent_R(redRegion.get(2).asReduction(),""));
			break;
		case RuleConstants.PROD_INSPECT_AFTER_AFTER:
			afters.add(getContent_R(redRegion.get(2).asReduction(),""));
			befores.add("\"\"");
			break;
		default:
			afters.add("\"\"");
			befores.add("\"\"");
		}
	}
	// END KGU#614 2018-12-14

	/**
	 * Inserts a new {@link SectionOrParagraph} object at the front of {@link #procedureList}.
	 * The new procedure reference object will start at the current end of @{@link Subqueue} {@code _parentNode}.
	 * @param _parentNode - the {@link Subqueue} we are referrung to
	 * @param _name - name of the section or paragraph starting here
	 * @param _asSection - whether it is a SECTION.
	 */
	protected void addProcedureToList(Subqueue _parentNode, String _name, boolean _asSection) {
		SectionOrParagraph containingSoP = null;
		for (SectionOrParagraph sop: this.procedureList) {
			// Same Subqueue and not closed? Then we will link it
			if (sop.parent == _parentNode && sop.endsBefore < 0) {
				containingSoP = sop;
				break;
			}
		}
		this.procedureList.addFirst(new SectionOrParagraph(_name, _asSection, _parentNode.getSize(), _parentNode, containingSoP));
	}

	/**
	 * Accomplishes the element references of the preceding (and here-ending) unsatisfied paragraphs
	 * until to the first occurring unsaturated section entry.
	 * @param _parentNode - the Subqueue to add elements to
	 * @param _what - whether a section is to be accomplished (involves closing of all begun paragraphs),
	 * or just a paragraph will be accomplished (if there is an open one) or just the last open entry.
	 * @return whether a matching open entry was found.
	 */
	private boolean accomplishPrevSoP(Subqueue _parentNode, SoPTarget _what) {
		Iterator<SectionOrParagraph> iter = procedureList.iterator();
		boolean found = false;
		while (!found && iter.hasNext()) {
			SectionOrParagraph sop = iter.next();
			if (sop.parent == _parentNode) {
				// START KGU#464 2017-12-03: Bugfix #475
				//sop.endsBefore = _parentNode.getSize();
				if (_what != SoPTarget.SOP_SECTION && sop.endsBefore >= 0) {
					// Leave if we are not to satisfy a section but the last entry isn't open anymore
					break;
				}
				if (_what != SoPTarget.SOP_PARAGRAPH || !sop.isSection) {
					// This definitively ends the section or paragraph - it will no longer be found as open entity
					sop.endsBefore = _parentNode.getSize();
				}
				// If we are to close a section, then we must close all open paragraphs first until we find the
				// last open section and may leave. Otherwise we may leave just now.
				if (_what != SoPTarget.SOP_SECTION || sop.isSection) {
					found = true;
				}
				// END KGU#464 2017-12-03
			}
		}
		return found;
	}

	/**
	 * Builds an equivalent loop structure for a SEARCH statement
	 *
	 * @param _reduction - the statement reduction
	 * @param _parentNode - the {@link Subqueue} to which the element(s) are to be appended
	 * @throws ParserCancelled on user abort
	 */
	private void importSearch(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled {
		/*
		 * FIXME: At least add variable name parsing the same way we have in
		 * other places and add some line breaks
		 */
		Reduction redBody = _reduction.get(1).asReduction();
		if (redBody.getParent().getTableIndex() == RuleConstants.PROD_SEARCH_BODY) {
			String varName = this.getContent_R(redBody.get(0).asReduction(), "");
			CobVar table = this.currentProg.getCobVar(varName);
			if (table == null) {
//				String content = this.getOriginalText(_reduction, "");
//				Instruction instr = new Instruction(content);
//				instr.setColor(Color.RED);
//				_parentNode.addElement(this.equipWithSourceComment(instr, _reduction));
//				instr.getComment().add("FIXME: Couldn't identify the table variable!");
//				return;
				getLogger().log(Level.INFO, "Couldn't identify the table variable \"{0}\"!", varName);
				// create table and its index for rest of the function
				table = this.cobTools.new CobVar(1, varName, null, null, null, null, false, false, 0, 99, null);
			}
			// Note: SEARCH *allways* changes and searches with the first index,
			//       this does NOT change depending on the WHEN
			//       VARYING identifier-2 does an *additional* increase of identifier-2
			CobVar indexVar = table.getIndexedBy(0);
			if (indexVar == null) {
				getLogger().log(Level.INFO, "Couldn't get the index variable for \"{0}\"!", table.getName());
				indexVar = this.cobTools.new CobVar(varName + "MissingIdx", table);
			}
			CobVar indexAdditionalVar = null;
			if (redBody.get(1).asReduction().getParent().getTableIndex() == RuleConstants.PROD_SEARCH_VARYING_VARYING) {
				String indexAdditionalName = this.getContent_R(redBody.get(1).asReduction().get(1).asReduction(), "");
				indexAdditionalVar = this.currentProg.getCobVar(indexAdditionalName);
				if (indexAdditionalVar == null) {
					getLogger().log(Level.INFO, "couldn't get the index variable \"{0}\"!", indexAdditionalName);
					indexAdditionalVar = this.cobTools.new CobVar(indexAdditionalName, table);
				}
			}
			// In case we could identify an index variable we might possibly use
			// a FOR-IN loop?
//			if (indexVar == null) {
//				String content = this.getOriginalText(_reduction, "");
//				Instruction instr = new Instruction(content);
//				instr.setColor(Color.RED);
//				_parentNode.addElement(this.equipWithSourceComment(instr, _reduction));
//				instr.getComment().add("FIXME: Couldn't identify an index variable!");
//				return;
//			} else
			// Sometimes the COBOL programmer didn't specify the actual array
			// component but some ancestor...
			if (!table.isArray() && indexVar.isIndex()) {
				// If the subscript is actually an index then we can mend it.
				CobVar actTable = indexVar.getParent();
				if (actTable.isComponentOf(table)) {
					table = actTable;
				}
			}
			// Create the WHILE element and the loop initialisation Instruction. Since the arrays length
			// is in no case a valid index, the loop must end before the array length.
			While wLoop = new While(indexVar.getName() + " < length(" + table.getQualifiedName() + ")");
			String testVarName = "wasFound" + Integer.toHexString(wLoop.hashCode());
			wLoop.setText(wLoop.getText().getText() + " and not " + testVarName);
			Instruction testInit = new Instruction(testVarName + " <- false");
			testInit.setColor(colorMisc);
			wLoop.setColor(colorMisc);
			_parentNode.addElement(testInit);
			_parentNode.addElement(this.equipWithSourceComment(wLoop, _reduction));
			// Now convert the WHEN clauses and add the resulting Alternatives to the loop body
			Reduction redWhens = redBody.get(3).asReduction();
			// Alternatively, we could use a Jump "leave" here (advantage: index won't be incremented, drawback: unstructured code)
			String stopStmt = testVarName + " <- true";
			do {
				Reduction redWhen = redWhens;
				if (redWhens.getParent().getTableIndex() == RuleConstants.PROD_SEARCH_WHENS2) {
					redWhen = redWhen.get(0).asReduction();
					redWhens = redWhens.get(1).asReduction();
				} else {
					redWhens = null;
				}
				String cond = this.transformCondition(redWhen.get(1).asReduction(), null);
				Alternative when = new Alternative(cond);
				when.setColor(colorMisc);
				wLoop.getBody().addElement(this.equipWithSourceComment(when, redWhen));
				this.buildNSD_R(redWhen.get(2).asReduction(), when.qTrue);
				when.qTrue.addElement(new Instruction(stopStmt));
			} while (redWhens != null);
			// Now add the increment to the loop body
			String InstrString = "inc(" + indexVar.getName() + ")";
			if (indexAdditionalVar != null) {
				InstrString += "\n inc(" + indexAdditionalVar.getName() + ")";
			}
			Instruction incr = new Instruction(InstrString);
			incr.setColor(colorMisc);
			wLoop.getBody().addElement(incr);
			// Finally convert and add the AT END clause after the loop.
			redWhens = redBody.get(2).asReduction();
			if (redWhens.getParent().getTableIndex() == RuleConstants.PROD_SEARCH_AT_END_END) {
				Alternative endTest = new Alternative("not " + testVarName);
				endTest.setColor(colorMisc);
				_parentNode.addElement(this.equipWithSourceComment(endTest, redWhens));
				this.buildNSD_R(redWhens.get(1).asReduction(), endTest.qTrue);
			}
		} else {
			// This is a SEARCH ALL statement - we haven't got a strategy yet
			String content = this.getOriginalText(_reduction, "");
			Instruction instr = new Instruction(content);
			instr.setColor(Color.RED);
			_parentNode.addElement(this.equipWithSourceComment(instr, _reduction));
			instr.getComment().add("TODO: there is still no automatic conversion for this statement");
		}
	}

	/**
	 * Tries to build a sensible instruction (sequence) from an imported STRING statement,
	 * i.e. a string concatenation
	 * @param _reduction - the STRING statement reduction
	 * @param _parentNode - {@link Subqueue} to append the resulting elements to
	 * @return indicates whether some halfway usable element (sequence) could be generated
	 * @throws ParserCancelled
	 */
	private boolean importString(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled {
		Reduction secRed = _reduction.get(1).asReduction();
		// <string_body> ::= <string_item_list> INTO <identifier> <_with_pointer> <_on_overflow_phrases>
		String varName = this.getContent_R(secRed.get(2).asReduction(), "");	// target variable
		Reduction withRed = secRed.get(3).asReduction();
		String start = null;	// start position within the target
		if (withRed.size() > 0) {
			// <_with_pointer> ::= <_with> POINTER <_is> <identifier>
			start = this.getContent_R(withRed.get(3).asReduction(), "");
		}
		// Now process the items backwards, this way forming an instruction sequence from last to first
		// This way we avoid unnecessary recursion
		// IDEA: This approach automatically produces several lines. We might first gather the contributors
		// and then decide if their concatenation might fit into a single line.
		StringList assignments = new StringList();
		StringList preparations = new StringList();
		int suffix = Math.abs(secRed.hashCode());		// unique number as suffix for auxiliary variables
		Reduction itemlRed = secRed.get(0).asReduction();
		do {
			// The first assigment (produced as the last one here) will just overwrite the target variable
			String asgnmt = varName + " <- ";
			Reduction itemRed = itemlRed;	// <string_item> ::= <x> <_string_delimited>
			if (itemlRed.getParent().getTableIndex() == RuleConstants.PROD_STRING_ITEM_LIST2) {
				itemRed = itemlRed.get(1).asReduction();
				itemlRed = itemlRed.get(0).asReduction();
				// If there are preceding items then simply concatenate the new item content
				asgnmt += varName + " + ";
			}
			else {
				itemlRed = null;	// Prepare the loop exit
				if (start != null) {
					// If there is a start pointer then there will be an additional preparation assignment, so concatenate
					asgnmt += varName + " + ";
				}
			}
			// Now we analyse the item
			String itemId = this.getContent_R(itemRed.get(0).asReduction(), "");	// source variable id
			// Is there some extra delimiter?
			String delimiter = null;
			if (itemRed.get(1).asReduction().size() > 0) {
				// <_string_delimited> ::= DELIMITED <_by> <string_delimiter>
				delimiter = this.getContent_R(itemRed.get(1).asReduction().get(2).asReduction(), "");
				if (delimiter.equalsIgnoreCase("SIZE")) {
					delimiter = null;	// this is a dummy information, not delimiting at all
				}
			}
			asgnmt += itemId;
			if (delimiter != null) {
				preparations.add(itemId + suffix + " <- split(" + itemId + ", " + delimiter + ")");
				asgnmt += suffix + "[0]";
			}
			assignments.add(asgnmt);
		} while (itemlRed != null);
		// Now if there was a start pointer, an additional preparation is necessary: Replace the target
		// variable by its prefix left of the pointer (otherwise it will completely be overwritten)
		if (start != null) {
			assignments.add(varName + " <- copy(" + varName + ", 1, " + start + ")");
		}
		// Append all the preparations to the assignments (such that they be at top afterwards)
		assignments.add(preparations);
		// Now add all the assignments in reverse order as a single element
		Instruction instr = new Instruction(assignments.reverse());
		_parentNode.addElement(this.equipWithSourceComment(instr, _reduction));
		instr.getComment().add("-----------------------------------");
		instr.getComment().add(this.getOriginalText(_reduction, ""));
		return true;
	}

	private boolean importUnstring(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled {
		boolean done = false;
		Reduction secRed = _reduction.get(1).asReduction();
		// <unstring_body> ::= <identifier> <_unstring_delimited> <unstring_into> <_with_pointer> <_unstring_tallying> <_on_overflow_phrases>
		String source = this.getContent_R(secRed.get(0).asReduction(), "");
		Reduction delimRed = secRed.get(1).asReduction();
		StringList delimiters = new StringList();
		long allFlags = 0;	// one bit for every delimiter = 1 if "ALL" was given, 0 otherwise, first delimiter = last Bit
		if (delimRed.size() > 0) {
			Reduction itemlRed = delimRed.get(2).asReduction();
			do {
				Reduction itemRed = itemlRed;
				if (itemlRed.getParent().getHead().toString().equals("<unstring_delimited_list>")) {
					itemRed = itemlRed.get(2).asReduction();
					itemlRed = itemlRed.get(0).asReduction();
				}
				else {
					itemlRed = null;
				}
				delimiters.add(this.getContent_R(itemRed.get(1).asReduction(), ""));
				allFlags <<= 1;
				//if (itemRed.get(0).asReduction().getParent().getTableIndex() == RuleConstants.PROD_FLAG_ALL_ALL) {
				if (itemRed.get(0).asReduction().size() > 0) {
					allFlags |= 1;
				}
			} while (itemlRed != null);
			delimiters = delimiters.reverse();
		}
		// The elements of the list are String arrays with following content:
		// [0] = target variable (or substring), [1] = delim variable or null, [2] = count variable or null
		LinkedList<String[]> targets = new LinkedList<String[]>();
		Reduction intoRed = secRed.get(2).asReduction();
		do {
			Reduction itemRed = intoRed.get(1).asReduction();
			if (intoRed.getParent().getTableIndex() == RuleConstants.PROD_UNSTRING_INTO_INTO) {
				// Just a single item - end of the loop
				intoRed = null;
			}
			else {
				intoRed = intoRed.get(0).asReduction();
			}
			// Make sure that it's not just a comma
			if (itemRed.getParent().getTableIndex() == RuleConstants.PROD_UNSTRING_INTO_ITEM) {
				String[] vars = new String[3];
				for (int i = 0; i < vars.length; i++) {
					Reduction varRed = itemRed.get(i).asReduction();
//					int varRuleId = varRed.getParent().getTableIndex();
					if (varRed.size() > 0) {
						if (i == 0) {
							vars[i] = this.getContent_R(varRed, "").trim();
						}
						else {
							vars[i] = this.getContent_R(varRed.get(varRed.size()-1).asReduction(), "").trim();
						}
					}
					else {
						vars[i] = null;
					}
				}
				targets.addFirst(vars);
			}
		} while (intoRed != null);
		String start = null;
		if (secRed.get(3).asReduction().size() > 0) {
			start = this.getContent_R(secRed.get(3).asReduction().get(3).asReduction(), "");
			source = "copy(" + source + ", " + start + ", length(" + source + ") - " + start + ")";
		}
		String tallying = null;
		if (secRed.get(4).asReduction().size() > 0) {
			tallying = this.getContent_R(secRed.get(4).asReduction().get(2).asReduction(), "");
		}
		// Now we have all information together and may compose the resulting algorithm
		// Since this is significantly easier for a single separator we start with this
		String suffix = Integer.toHexString(_reduction.hashCode());
		if (delimiters.count() >= 1) {
			String content = "unstring_"+suffix + "_0 <- split(" + source + ", " + delimiters.get(0) + ")";
			String indexVar = "index_" + suffix;	// Used for substring traversal (with several delmiters and ALL handling)
			Instruction instr = new Instruction(content);
			instr.setColor(colorMisc);
			_parentNode.addElement(this.equipWithSourceComment(instr, _reduction));
			instr.getComment().add("-----------------------------------");
			instr.getComment().add(this.getOriginalText(_reduction, ""));
			for (int i = 1; i < delimiters.count(); i++) {
				instr = new Instruction(indexVar + " <- 0");
				instr.setColor(colorMisc);
				_parentNode.addElement(instr);
				For loop = new For("part_" + suffix, "unstring_" + suffix + "_" + (1 - i % 2));
				loop.setColor(colorMisc);
				_parentNode.addElement(loop);
				instr = new Instruction("split_" + suffix + " <- split(part_" + suffix + ", " +delimiters.get(i) + ")");
				instr.setColor(colorMisc);
				loop.getBody().addElement(instr);
				For loop1 = new For("item_" + suffix, "split_" + suffix);
				loop1.setColor(colorMisc);
				loop.getBody().addElement(loop1);
				instr = new Instruction("unstring_" + suffix + "_" + (i % 2) + "[index_" + suffix + "] <- item_" + suffix);
				instr.setColor(colorMisc);
				instr.getText().add("inc(index_" + suffix + ", 1)");
				loop1.getBody().addElement(instr);
			}
			suffix += "_" + (1 - delimiters.count() % 2);
			int index = 0;
			// FIXME Handling of ALL clausues is still unclear
			if (!ignoreUnstringAllClauses) {
				instr = new Instruction(indexVar + " <- 0");
				instr.setColor(colorMisc);
				_parentNode.addElement(instr);
			}
			for (String[] target: targets) {
				// If there is an ALL flag set then we have to skip empty substrings from the split result.
				// The trouble here is: we don't know anymore, which empty part resulted from which
				// delimiter, and it can hardly be guessed at compile time. We would have to implement a
				// complex detection mechanism which seems beyond reasonable efforts.
				String expr = "unstring_" + suffix + "[" + index + "]";
				boolean all = (allFlags & 1) != 0;
				{ allFlags >>= 1; }	// Strangely, this instruction without block caused indentation defects in Eclipse
				// FIXME Handling of ALL clauses is still not correct (see remark above)
				if (!ignoreUnstringAllClauses) {
					if (all) {
						While loop = new While("(" + indexVar + " < length(unstring_" + suffix + ")) and (length(unstring_" + suffix + "["+indexVar+"] = 0)");
						loop.setColor(colorMisc);
						_parentNode.addElement(loop);
						instr = new Instruction("inc(" + indexVar + ", 1)");
						instr.setColor(colorMisc);
						loop.getBody().addElement(instr);
					}
					expr = "unstring_" + suffix + "[" + indexVar + "]";
				}

				StringList assignments = new StringList();
				if (mCopyFunction.reset(target[0]).matches()) {
					assignments.add(mCopyFunction.replaceFirst("delete($1, $2, $3)"));
					assignments.add(mCopyFunction.replaceFirst(Matcher.quoteReplacement("insert(" + expr) + ", $1, $2)"));
				}
				else {
					assignments.add(target[0] + " <- " + expr);
				}
				if (target[2] != null) {	// counter specified?
					assignments.add(target[2] + " <- length(" + target[0] + ")");
				}
				if (target[1] != null) {	// delimiter variable specified?
					// FIXME This is only okay for the case of a single unique delimiter, otherwise I have no idea how to identify the responsible delimiter (i.e. with justifiable efforts)
					assignments.add(target[1] + " <- " + delimiters.get(0));
				}
				if (tallying != null) {
					assignments.add("inc(" + tallying + ", 1)");
				}
				instr = new Instruction(assignments);
				instr.setColor(colorMisc);
				// FIXME Handling of ALL clausues is still unclear
				String indexStr = (ignoreUnstringAllClauses ? Integer.toString(index) : indexVar);
				Alternative alt = new Alternative("length(unstring_" + suffix + ") > " + indexStr);
				alt.setColor(colorMisc);
				_parentNode.addElement(alt);
				alt.qTrue.addElement(instr);
				// FIXME Handling of ALL clausues is still unclear
				if (!ignoreUnstringAllClauses) {
					instr = new Instruction("inc(" + indexVar + ", 1)");
					instr.setColor(colorMisc);
					_parentNode.addElement(instr);
				}

				index++;
			}
			done = true;
		}
		return done;
	}

	private boolean importAccept(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled {
		boolean done = false;
		Reduction secRed = _reduction.get(1).asReduction();		// <accept_body>
		int secRuleId = secRed.getParent().getTableIndex();
		Reduction targetRed = secRed.get(0).asReduction();
		String varName = this.getContent_R(targetRed, "");
		if (varName.equalsIgnoreCase("OMITTED")) {
			varName = "";
		}
		if (secRuleId == RuleConstants.PROD_ACCEPT_BODY || secRuleId == RuleConstants.PROD_ACCEPT_BODY_FROM3) {
			// For these types we can offer a conversion
			String content = getKeywordOrDefault("input", "input");
			content += " " + varName;
			_parentNode.addElement(new Instruction(content.trim()));
			done = true;
		}
		else if (!varName.isEmpty()) {
			String content = "";
			String comment = null;
			boolean requiresManualAction = true;
			switch (secRuleId) {
			case RuleConstants.PROD_ACCEPT_BODY_FROM_TIME:
				content = varName + " <- " + "getTime()";
				break;
			case RuleConstants.PROD_ACCEPT_BODY_FROM_DATE:
			case RuleConstants.PROD_ACCEPT_BODY_FROM_DATE_YYYYMMDD:
			case RuleConstants.PROD_ACCEPT_BODY_FROM_DAY:
			case RuleConstants.PROD_ACCEPT_BODY_FROM_DAY_YYYYDDD:
				content = varName + " <- " + "getDate()";
				comment = this.getOriginalText(_reduction, "");
				break;
			case RuleConstants.PROD_ACCEPT_BODY_FROM_ENVIRONMENT:
				content = this.getContent_R(secRed.get(3).asReduction(), "");
				content = varName + " <- System.getenv(" + content + ")";
			}
			if (!content.trim().isEmpty()) {
				Instruction instr = new Instruction(content.trim());
				if (requiresManualAction) {
					instr.setColor(colorMisc);
				}
				_parentNode.addElement(this.equipWithSourceComment(instr, _reduction));
				if (comment != null) {
					instr.getComment().add(comment);
				}
				done = true;
			}
		}
		return done;
	}

	private void importMove(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled {
		Reduction secRed = _reduction.get(1).asReduction();
		String expr = this.getContent_R(secRed.get(0).asReduction(), "");
		// FIXME: This doesn't work for array elements - we need an expression list splitter
		// but what exactly is an expression in a space-separated list looking like "A (I) B (J)"?
		//String targetString = this.getContent_R(secRed.get(2).asReduction(), "");
		//String[] targets;
		// FIXME: the unwanted "," should not be passed to the engine at all,
		// we currently split "literal, with a comma in" into two targets
		//if (targetString.contains(",")) {
		//	targets = targetString.split(",");
		//}
		//else {
		//	targets = targetString.split(" ");
		//}
		//if (targets.length > 0) {
		StringList targets = this.getExpressionList(secRed.get(2).asReduction(), "<target_x_list>", RuleConstants.PROD_TARGET_X_COMMA_DELIM);
		if (targets.count() > 0)
		{
			StringList assignments = new StringList();
			for (int i = 0; i < targets.count(); i++) {
				String target = targets.get(i).trim();
				// We must do something to avoid copy() calls on the left-hand side
				if (mCopyFunction.reset(target).matches()) {
					assignments.add(mCopyFunction.replaceFirst("delete($1, $2, $3)"));
					assignments.add(mCopyFunction.replaceFirst(Matcher.quoteReplacement("insert(" + expr) + ", $1, $2)"));
				}
				else {
					assignments.add(target + " <- " + expr);
					if (i == 0) {
						expr = target;
					}
				}
			}
			_parentNode.addElement(this.equipWithSourceComment(new Instruction(assignments), _reduction));
		}
	}

	private void importIf(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled {
		String content = this.transformCondition(_reduction.get(1).asReduction(), "");
		//System.out.println("\tCondition: " + content);
		Reduction secRed = _reduction.get(3).asReduction();
		int secRuleId = secRed.getParent().getTableIndex();
		Reduction trueRed = null;
		Reduction falseRed = null;
		switch (secRuleId) {
		case RuleConstants.PROD_IF_ELSE_STATEMENTS_ELSE:
			trueRed = secRed.get(0).asReduction();
			falseRed = secRed.get(2).asReduction();
			break;
		case RuleConstants.PROD_IF_ELSE_STATEMENTS_ELSE2:
			trueRed = secRed.get(1).asReduction();
			content = negateCondition(content);
			break;
		default:
			trueRed = secRed;
			break;
		}
		Alternative alt = new Alternative(content);
		if (trueRed != null) {
			//System.out.println("\tTHEN branch...");
			this.buildNSD_R(trueRed, alt.qTrue);
		}
		if (falseRed != null) {
			//System.out.println("\tELSE branch...");
			this.buildNSD_R(falseRed, alt.qFalse);
		}
		if (alt.qTrue.getSize() == 0 && alt.qFalse.getSize() > 0) {
			alt.qTrue = alt.qFalse;
			alt.qFalse = new Subqueue();
			alt.qFalse.parent = alt;
			alt.setText(negateCondition(content));
		}
		_parentNode.addElement(this.equipWithSourceComment(alt, _reduction));
		//System.out.println("\tEND_IF");
	}

	/** Resolve SET var [var2,var3] TO TRUE | FLASE
	 * @throws ParserCancelled */
	private boolean importSet(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled {
		boolean done = false;
		Reduction secRed = _reduction.get(1).asReduction();	// <set_body>
		int secRuleId = secRed.getParent().getTableIndex();
		switch (secRuleId) {
		// COBOL: SET index1, index2 index3 TO index (or number)
		case RuleConstants.PROD_SET_TO_TO:	// <set_to> ::= <target_x_list> TO <x>
		{
			String expr = this.getContent_R(secRed.get(2).asReduction(), "");
			StringList targets = this.getExpressionList(secRed.get(0).asReduction(), "<target_x_list>", RuleConstants.PROD_TARGET_X_COMMA_DELIM);
			if (targets.count() > 0)
			{
				StringList assignments = new StringList();
				StringList comments = new StringList();
				for (int i = 0; i < targets.count(); i++) {
					String target = targets.get(i).trim();
					// We must do something to avoid copy() calls on the left-hand side
//					Simon: should not be necessary here, at least as long as we have "valid" COBOL sources
//					if (target.matches("^copy\\((.*),(.*),(.*)\\)$")) {
//						assignments.add(target.replaceFirst("^copy\\((.*),(.*),(.*)\\)$", "delete($1, $2, $3)"));
//						assignments.add(target.replaceFirst("^copy\\((.*),(.*),(.*)\\)$", Matcher.quoteReplacement("insert(" + expr) + ", $1, $2)"));
//					}
//					else {
					String comment = null;
					CobVar var = currentProg.getCobVar(target);
					if (var != null && (comment = var.getComment()) != null) {
						target = var.getQualifiedName();
						comments.add(comment);
					}
					assignments.add(target + " <- " + expr);
//					}
				}
				Element instr = this.equipWithSourceComment(new Instruction(assignments), _reduction);
				if (comments.count() > 0) {
					instr.getComment().add(comments);
				}
				_parentNode.addElement(instr);
				done = true;
			}
			break;
		}
		// COBOL: SET var1 TO TRUE  var2 TO TRUE  var3 TO FALSE
		case RuleConstants.PROD_SET_TO_TRUE_FALSE_SEQUENCE:
		case RuleConstants.PROD_SET_TO_TRUE_FALSE_SEQUENCE2:
		{
			Reduction setRed = secRed;
			StringList assignments = new StringList();
			do {
				// more entries to come?
				if (secRuleId == RuleConstants.PROD_SET_TO_TRUE_FALSE_SEQUENCE2) {
					setRed = secRed.get(1).asReduction();
					secRed = secRed.get(0).asReduction();
					secRuleId = secRed.getParent().getTableIndex();
				}
				else {
					setRed = secRed;
					secRed = null;
				}
				this.addBoolAssignments(setRed, assignments);
			} while (secRed != null);
			if (assignments.count() > 0) {
				assignments = assignments.reverse();
				_parentNode.addElement(this.equipWithSourceComment(
						new Instruction(assignments), _reduction));
			}
			done = true;
			break;
		}
		// COBOL: SET var1 TO TRUE | FALSE
		case RuleConstants.PROD_SET_TO_TRUE_FALSE_TO_TOK_TRUE:
		case RuleConstants.PROD_SET_TO_TRUE_FALSE_TO_TOK_FALSE:
		{
			StringList assignments = new StringList();
			this.addBoolAssignments(secRed, assignments);
			if (assignments.count() > 0) {
				_parentNode.addElement(this.equipWithSourceComment(
						new Instruction(assignments), _reduction));
			}
			done = true;
			break;
		}
		}
		return done;
	}

	/**
	 * Helper method for {@link #importSet(Reduction, Subqueue)}, just adding the assignments
	 * of the <target_x_list> of the given reduction {@code setRed} to {@code assignments}
	 * @param setRed
	 * @param assignments
	 * @throws ParserCancelled
	 */
	private void addBoolAssignments(Reduction setRed, StringList assignments) throws ParserCancelled {
		String value = setRed.get(2).asString().toLowerCase();	// gets "true" or "false"
		StringList targets = this.getExpressionList(setRed.get(0).asReduction(), "<target_x_list>", RuleConstants.PROD_TARGET_X_COMMA_DELIM);
		for (int i = 0; i < targets.count(); i++) {
			// resolve condName to get the original name and value assigned to the condition
			// Note: this can only work correctly for "complete" sources,
			//       not possible if we haven't parsed the condition variable before
			//       therefore we leave the fallback "condition-name <- true/false"
			String condName = targets.get(i);
			CobVar condVar = currentProg.getCobVar(condName);
			if (condVar != null) {
				String newValue;
				if (value.equals("true")) {
					newValue = condVar.getValueFirst();
				} else {
					newValue = condVar.getValueFalse();
				}
				// check if we actually have a true/false saved
				if (newValue != null) {
					String varName = condVar.getParent().getName();
					assignments.add(varName + " <- " + newValue);
					// Idea: we could place all the true/false values that are *actually set*
					//       into a list of "const condName$value = newvalue", insert these
					//       into the end of the import NSD and do the following here
					// assignments.add(varName + " <- " + condName + "$" + value);
					continue;
				}
			}
			// not found in variable list: leave as is
			assignments.add(condName + " <- " + value);
		}
	}

	private void importCall(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled {
		boolean callOk = false;
		Reduction secRed = _reduction.get(1).asReduction();	// <call_body>
		String name = this.getContent_R(secRed.get(1).asReduction(), "").trim();
		// FIXME: This can be a lot of things, consider tokenizing it ...
		String[] nameTokens = name.split("\\s+");
		// Maybe the actual name is given as string literal rather than an identifier
		if (nameTokens.length == 1 && (name.matches("\\\".*?\\\"") || name.matches("['].*?[']"))) {
			name = name.substring(1, name.length()-1).replace("-", "_");
			callOk = true;
		}
		StringList args = new StringList();
		if (secRed.get(2).asReduction().size() > 0) {
			args = this.getParameterList(secRed.get(2).asReduction().get(1).asReduction(), "<call_param_list>", RuleConstants.PROD_CALL_PARAM, 2);
		}
		String content = name + "(" + args.concatenate(", ") + ")";
		Reduction retRed = secRed.get(3).asReduction();
		if (retRed.getParent().getTableIndex() == RuleConstants.PROD_CALL_RETURNING2) {
			content = this.getContent_R(retRed.get(2).asReduction(), "") + " <- " + content;
		}
		Call ele = new Call(content);
		String comment = this.getOriginalText(_reduction, "");
		if (!callOk) {
			ele.setColor(Color.RED);
			comment = "A call with computed routine name is not supported in Structorizer!\n" + comment;
		}
		_parentNode.addElement(this.equipWithSourceComment(ele, _reduction));
		ele.getComment().add(StringList.explode(comment, "\n"));
	}

	private void importFileControl(Reduction _reduction, Subqueue _subqueue) throws ParserCancelled {
		String fileDescr = this.getContent_R(_reduction.get(2).asReduction(), "");
		boolean isSuited = true;
		// Now fetch the file name (if available) and make sure it's a line-sequential file
		// (otherwise we can't provide FileAPI support)
		Reduction selsRed = _reduction.get(3).asReduction().get(0).asReduction();
		while (selsRed.getParent().getTableIndex() == RuleConstants.PROD__SELECT_CLAUSE_SEQUENCE2) {
			Reduction selRed = selsRed.get(1).asReduction();
			selsRed = selsRed.get(0).asReduction();
			String selHead = selRed.getParent().getHead().toString();
			if (selHead.equals("<assign_clause>")) {
				String fileName = this.getContent_R(selRed.get(4).asReduction(), "");
				currentProg.fileMap.put(fileDescr, fileName);
			}
			else if (selHead.equals("<file_status_clause>")) {
				//System.out.println(this.getContent_R(selRed.get(0).asReduction(), ""));
				if (selRed.get(0).asReduction().getParent().getTableIndex() == RuleConstants.PROD__FILE_OR_SORT_TOK_FILE) {
					// map the status variable to the file descriptor!
					String statusVar = this.getContent_R(selRed.get(3).asReduction(), "");
					currentProg.fileStatusMap.put(fileDescr, statusVar);
				}
			}
			else if (selHead.equals("<organization_clause>")) {
				// TODO make sure it's a text file
				if (selRed.get(selRed.size()-1).asReduction().getParent().getTableIndex() != RuleConstants.PROD_ORGANIZATION_LINE_SEQUENTIAL) {
					log("File organization of '" + fileDescr + "' unsuited for Structorizer FileAPI!", false);
					isSuited = false;
				}
			}
		}
		if (this.optionImportVarDecl) {
			Instruction decl = new Instruction("var " + fileDescr + ": int");
			decl.setColor(isSuited ? colorDecl : Color.RED);
			_subqueue.addElement(this.equipWithSourceComment(decl, _reduction));
			decl.comment.add("-----------------------------------");
			decl.comment.add(this.getOriginalText(_reduction, ""));
			if (!isSuited) {
				decl.comment.add("Unsuited for Structorizer FileAPI!");
			}
		}
	}

	private Element addStatusAssignment(Subqueue _parentNode, String fdName) {
		String statName = currentProg.fileStatusMap.get(fdName);
		Call statusCheck = null;
		if (statName != null && currentProg.getCobVar(statName) != null) {
			statusCheck = new Call(statName + " <- fileStatusToCobol(" + fdName + ")");
			_parentNode.addElement(statusCheck);
			if (!this.fileStatusFctAdded) {
				Root fileStatusFct = new Root();
				fileStatusFct.setText("fileStatusToCobol(fileDescr: int): String");
				fileStatusFct.setComment("Derives a COBOL file status value from the file descriptor of the Structorizer File_API.");
				fileStatusFct.setProgram(false);
				Case fileStatusCase = new Case(StringList.explode(fileStatusCaseText, "\n"));
				for (int i = 0; i < fileStatusCodes.length; i++) {
					Instruction asgnmt = new Instruction("file_status <- \"" + fileStatusCodes[i] + "\"");
					asgnmt.setComment(fileStatusComments[i]);
					fileStatusCase.qs.get(i).addElement(asgnmt);
				}
				fileStatusFct.children.addElement(fileStatusCase);
				fileStatusFct.children.addElement(new Instruction(CodeParser.getKeywordOrDefault("preReturn", "return") + " file_status"));
				this.addRoot(fileStatusFct);
				this.fileStatusFctAdded = true;
			}
		}
		return statusCheck;
	}

	private boolean importWrite(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled {
		// TODO: Find a sensible conversion!
		boolean done = false;
		Reduction secRed = _reduction.get(1).asReduction();	// <write_body>
		String fdName = null;
		String dataStr = null;
		Reduction nameRed = secRed.get(0).asReduction();	// <file_or_record_name>
		if (nameRed.getParent().getTableIndex() == RuleConstants.PROD_FILE_OR_RECORD_NAME_TOK_FILE) {
			fdName = this.getContent_R(nameRed.get(1).asReduction(), "");	// Is rather the file name (path)
			if (currentProg.fileMap.containsValue(fdName)) {
				// Perform a reverse search for the descriptor name
				for (HashMap.Entry<String, String> entry: currentProg.fileMap.entrySet()) {
					if (entry.getValue().equals(fdName)) {
						fdName = entry.getKey();
						break;
					}
				}
			}
		}
		else {
			dataStr = this.getContent_R(nameRed, "");
			fdName = currentProg.fileRecordMap.get(dataStr);
		}
		Reduction fromRed = secRed.get(1).asReduction();
		if (fromRed.getParent().getTableIndex() == RuleConstants.PROD_FROM_OPTION_FROM) {
			dataStr = this.getContent_R(fromRed.get(1).asReduction(), "").trim();
		}
		if (fdName != null && dataStr != null) {
			// TODO: try to consider types here.
			Instruction writeInstr = new Instruction("fileWrite(" + fdName + ", " + dataStr + ")");
			_parentNode.addElement(this.equipWithSourceComment(writeInstr, _reduction));
			addStatusAssignment(_parentNode, fdName);
			done = true;
		}
		return done;
	}

	private boolean importRead(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled {
		boolean done = false;
		// <read_body> ::= <file_name> <_flag_next> <_record> <read_into> <lock_phrases> <read_key> <read_handler>
		Reduction bodyRed = _reduction.get(1).asReduction();
		// <_flag_next> - we support only forward reading
		if (bodyRed.get(1).asReduction().getParent().getTableIndex() == RuleConstants.PROD__FLAG_NEXT_PREVIOUS) {
			return false;
		}
		String fdName = this.getContent_R(bodyRed.get(0).asReduction(), "");	// File descriptor
		String target = null;
		Reduction intoRed = bodyRed.get(3).asReduction();
		if (intoRed.getParent().getTableIndex() == RuleConstants.PROD_READ_INTO_INTO) {
			target = this.getContent_R(intoRed.get(1).asReduction(), "");
		}
		// Without an INTO clause we will have to search the file record map
		if (target == null) {
			// ... the entry should be unique then. Unfortunately we have to search backwards
			for (Entry<String, String> entry: currentProg.fileRecordMap.entrySet()) {
				if (fdName.equalsIgnoreCase(entry.getValue())) {
					target = entry.getKey();
					break;
				}
			}
		}
		if (target != null) {
			String fnName = "fileRead";	// The default function name
			// In order to find the best fileRead function we try to get the typ info from root
			TypeMapEntry typeInfo = root.getTypeInfo().get(target);
			if (typeInfo != null && typeInfo.isConflictFree()) {
				String type = typeInfo.getTypes().get(0);
				if (type.equals("int") || type.equals("integer") || type.equals("short") || type.equals("long")) {
					fnName = "fileReadInit";
				}
				else if (type.equals("double") || type.equals("float")) {
					fnName = "fileReadDouble";
				}
				else if (type.equals("char")) {
					fnName = "fileReadChar";
				}
				else if (type.equalsIgnoreCase("string")) {
					fnName = "fileReadLine";
				}
			}
			// we just ignore the lock clause
			Instruction instr = new Instruction(target + " <- " + fnName + "(" + fdName + ")");
			_parentNode.addElement(this.equipWithSourceComment(instr, _reduction));
			instr.getComment().add(this.getOriginalText(_reduction, ""));
			addStatusAssignment(_parentNode, fdName);
			done = true;
		}
		return done;
	}

	/**
	 * Tries to build an equivalent for the OPEN statement
	 * @param _reduction - the corresponding {@link Reduction}
	 * @param _parentNode - the {@link Subqueue} to which the element(s) are to be appended
	 * @return true if done, false otherwise
	 * @throws ParserCancelled
	 */
	private boolean importOpen(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled {
		boolean done = false;
		Reduction bodyRed = _reduction.get(1).asReduction();
		int pos = _parentNode.getSize();	// Insertion position at end (to compensate reverse retrieval)
		do {
			Reduction entryRed = bodyRed;
			if (bodyRed.getParent().getHead().toString().equals("<open_body>")) {
				entryRed = bodyRed.get(1).asReduction();
				bodyRed = bodyRed.get(0).asReduction();
			}
			else {
				bodyRed = null;
			}
			// File descriptor
			boolean unsupportedMode = false;
			String fileDescr = this.getContent_R(entryRed.get(3).asReduction(), ""); // FIXME there could be several ones
			String content = fileDescr + " <- ";
			// Add the correct opening function
			switch (entryRed.get(0).asReduction().getParent().getTableIndex()) {
			case RuleConstants.PROD_OPEN_MODE_INPUT:
				content += "fileOpen";
				break;
			case RuleConstants.PROD_OPEN_MODE_I_O:	// FIXME - we have no actual input + output support
				unsupportedMode = true;
			case RuleConstants.PROD_OPEN_MODE_OUTPUT:
				content += "fileCreate";
				break;
			case RuleConstants.PROD_OPEN_MODE_EXTEND:
				content += " <- fileAppend";
				break;
			default:
				content += this.getContent_R(entryRed.get(0).asReduction(), "file");
				unsupportedMode = true;
			}
			// Now get the file name
			String fileName = currentProg.fileMap.get(fileDescr);
			if (fileName == null || fileName.isEmpty()) {
				unsupportedMode = true;
				content += "(???)";
			}
			else {
				content += "(" + fileName + ")";
			}
			Instruction instr = new Instruction(content);
			this.equipWithSourceComment(instr, _reduction);
			if (unsupportedMode) {
				instr.getComment().add("File mode unknown or not supported");
				instr.setColor(Color.RED);
			}
			_parentNode.insertElementAt(instr, pos);
			Element statusEl = addStatusAssignment(_parentNode, fileDescr);
			if (unsupportedMode) {
				statusEl.disabled = true;
			}
			done = true;
		} while (bodyRed != null);
		return done;
	}

	/**
	 * Builds an appropriate Instruction element from the ADD statement represented by {@code _reduction}.
	 * @param _reduction - the top Reduction of the parsed ADD statement
	 * @param _parentNode - the Subqueue to append the built elements to
	 * @throws ParserCancelled
	 */
	private void importAdd(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled {
		Reduction secRed = _reduction.get(1).asReduction();
		int targetIx = -1;			// token index for the targets
		String summands1 = null;
		String summand2 = null;
		int secRuleId = secRed.getParent().getTableIndex();
		switch (secRuleId) {
		case RuleConstants.PROD_ADD_BODY_GIVING:
			summands1 = this.getExpressionList(secRed.get(0).asReduction(), "<x_list>", RuleConstants.PROD_X_COMMA_DELIM).concatenate(" + ");
			if (secRed.get(1).asReduction().size() == 2) {
				summand2 = this.getContent_R(secRed.get(1).asReduction().get(1).asReduction(), "");
			}
			targetIx = 3;
			break;
		case RuleConstants.PROD_ADD_BODY_TO:
			summands1 = this.getExpressionList(secRed.get(0).asReduction(), "<x_list>", RuleConstants.PROD_X_COMMA_DELIM).concatenate(" + ");
			targetIx = 2;
			break;
			// FIXME: There is no idea how to import the remaining ADD statement varieties
		}
		StringList targets = null;
		if (targetIx >= 0) {
			targets = this.getExpressionList(secRed.get(targetIx).asReduction(), "<arithmetic_x_list>", RuleConstants.PROD_X_COMMA_DELIM);
		}
		if (targets != null && targets.count() > 0) {
			String lastResult = null;
			StringList content = new StringList();
			for (int i = 0; i < targets.count(); i++) {
				String target = targets.get(i).trim();
				lastResult = this.addArithmOperation(content, "+", target, (summand2 != null ? summand2 : target), summands1, lastResult);
				if (summand2 == null) {
					lastResult = null;
				}
			}
			_parentNode.addElement(this.equipWithSourceComment(
					new Instruction(content), _reduction));
		}
		else {
			Instruction defective = new Instruction(this.getContent_R(_reduction, "", " "));
			defective.setColor(Color.RED);
			defective.disabled = true;
			_parentNode.addElement(this.equipWithSourceComment(defective, _reduction));
			defective.getComment().add("COBOL import still not implemented");
		}
	}

	/**
	 * Builds an appropriate Instruction element from the SUBTRACT statement represented by {@code _reduction}.
	 * @param _reduction - the top Reduction of the parsed SUBTRACT statement
	 * @param _parentNode - the Subqueue to append the built elements to
	 * @throws ParserCancelled
	 */
	private final void importSubtract(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled {
		Reduction secRed = _reduction.get(1).asReduction();
		int secRuleId = secRed.getParent().getTableIndex();
		int targetIx = 2;
		String summands1 = this.getExpressionList(secRed.get(0).asReduction(), "<x_list>", RuleConstants.PROD_X_COMMA_DELIM).concatenate(" + ");
		String summand2 = null;
		if (secRuleId == RuleConstants.PROD_SUBTRACT_BODY_FROM_GIVING) {
			targetIx = 4;
			summand2 = this.getContent_R(secRed.get(2).asReduction(), "");
		}
		StringList targets = this.getExpressionList(secRed.get(targetIx).asReduction(), "<arithmetic_x_list>", RuleConstants.PROD_TARGET_X_COMMA_DELIM);
		if (targets.count() > 0) {
			String lastResult = null;
			StringList content = new StringList();
			//StringList.getNew(targets[0] + " <- " + (summand2 != null ? summand2 : targets[0]) + " - (" + summands1 + ")");
			for (int i = 0; i < targets.count(); i++) {
				String target = targets.get(i).trim();
				lastResult = this.addArithmOperation(content, "-", target, (summand2 != null ? summand2 : target), "(" + summands1 + ")", lastResult);
				if (summand2 == null) {
					// We may not re-use the result if the minuend changes
					lastResult = null;
				}
			}
			_parentNode.addElement(this.equipWithSourceComment(
					new Instruction(content), _reduction));
		}
		else {
			Instruction defective = new Instruction(this.getContent_R(_reduction, "", " "));
			defective.setColor(Color.RED);
			defective.disabled = true;
			_parentNode.addElement(this.equipWithSourceComment(defective, _reduction));
			defective.getComment().add("COBOL import still not implemented");
		}
	}

	/**
	 * Builds an approptiate Instruction element from the MULTIPLY statement represented by {@code _reduction}.
	 * @param _reduction - the top Reduction of the parsed MULTIPLY statement
	 * @param _parentNode - the Subqueue to append the built elements to
	 * @throws ParserCancelled
	 */
	private final void importMultiply(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled {
		Reduction secRed = _reduction.get(1).asReduction();
		int secRuleId = secRed.getParent().getTableIndex();
		int targetIx = 2;
		String factor1 = this.getContent_R(secRed.get(0).asReduction(), "");
		String factor2 = null;
		if (secRuleId == RuleConstants.PROD_MULTIPLY_BODY_BY_GIVING) {
			targetIx = 4;
			factor2 = this.getContent_R(secRed.get(2).asReduction(), "");
		}
		// FIXME: Handle the <flag rounded>
		StringList targets = this.getExpressionList(secRed.get(targetIx).asReduction(), "<arithmetic_x_list>", RuleConstants.PROD_TARGET_X_COMMA_DELIM);
		//String[] targets = this.getContent_R(secRed.get(targetIx).asReduction(), "").split(" ");
		//if (targets.length > 0) {
		if (targets.count() > 0) {
			String lastResult = null;
			StringList content = new StringList();
			for (int i = 0; i < targets.count(); i++) {
				String target = targets.get(i).trim();
				lastResult = this.addArithmOperation(content, "*", target, (factor2 != null ? factor2 : target), factor1, lastResult);
				if (factor2 == null) {
					// Don't reuse the former result if the 2nd operand changes
					lastResult = null;
				}
			}
			_parentNode.addElement(this.equipWithSourceComment(
					new Instruction(content), _reduction));
		}
		else {
			Instruction defective = new Instruction(this.getContent_R(_reduction, "", " "));
			defective.setColor(Color.RED);
			defective.disabled = true;
			this.equipWithSourceComment(defective, _reduction);
			defective.getComment().add("COBOL import still not implemented");
			_parentNode.addElement(defective);
		}
	}

	/**
	 * Builds an approptiate Instruction element from the DIVIDE statement represented by {@code _reduction}.
	 * @param _reduction - the top Reduction of the parsed DIVIDE statement
	 * @param _parentNode - the Subqueue to append the built elements to
	 * @throws ParserCancelled
	 */
	private final void importDivide(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled {
		Reduction secRed = _reduction.get(1).asReduction();
		int secRuleId = secRed.getParent().getTableIndex();
		int targetIx = 4;
		String divisor = this.getContent_R(secRed.get(0).asReduction(), "");
		String dividend = null;
		String remainder = null;
		if (secRuleId == RuleConstants.PROD_DIVIDE_BODY_INTO) {
			targetIx = 2;
		}
		else {
			dividend = this.getContent_R(secRed.get(2).asReduction(), "");
		}
		if (secRuleId == RuleConstants.PROD_DIVIDE_BODY_BY_GIVING
				|| secRuleId == RuleConstants.PROD_DIVIDE_BODY_BY_GIVING_REMAINDER) {
			// In the BY statements the operand roles are swapped
			String tmp = dividend;
			dividend = divisor;
			divisor = tmp;
		}
		if (secRuleId == RuleConstants.PROD_DIVIDE_BODY_INTO_GIVING_REMAINDER
				|| secRuleId == RuleConstants.PROD_DIVIDE_BODY_BY_GIVING_REMAINDER) {
			remainder = this.getContent_R(secRed.get(6).asReduction(), "");
		}
		StringList targets = this.getExpressionList(secRed.get(targetIx).asReduction(),
				"<arithmetic_x_list>", RuleConstants.PROD_TARGET_X_COMMA_DELIM);

		//else if (secRuleId == RuleConstants.PROD_DIVIDE_BODY_BY_GIVING || )
		// FIXME: Handle the <flag rounded>
		if (targets.count() > 0) {
			StringList content = new StringList();
			String lastResult = null;
			for (int i = 0; i < targets.count(); i++) {
				String target = targets.get(i).trim();
				lastResult = this.addArithmOperation(content, "/", target, (dividend != null ? dividend : target), divisor, lastResult);
				if (dividend == null) {
					// Don't re-use the former result if the dividend changes
					lastResult = null;
				}
			}
			if (remainder != null) {
				content.add(remainder + " <- " + dividend + " mod " + divisor);
			}
			_parentNode.addElement(this.equipWithSourceComment(
					new Instruction(content), _reduction));
		}
		else {
			Instruction defective = new Instruction(this.getContent_R(_reduction, "", " "));
			defective.setColor(Color.RED);
			defective.disabled = true;
			this.equipWithSourceComment(defective, _reduction);
			defective.getComment().add("COBOL import still not implemented");
			_parentNode.addElement(defective);
		}
	}

	/**
	 * Builds an approptiate Jump element from the EXIT statement represented by {@code _reduction}.
	 * @param _reduction - the top Reduction of the parsed EXIT statement
	 * @param _parentNode - the Subqueue to append the built elements to
	 * @throws ParserCancelled
	 */
	private final void importExit(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled {
		String content = "";
		String comment = "";
		Color color = null;
		Reduction secRed = _reduction.get(1).asReduction();
		int secRuleId = secRed.getParent().getTableIndex();
		SoPTarget exitTarget = SoPTarget.SOP_ANY;
		switch (secRuleId) {
		case RuleConstants.PROD_EXIT_BODY:	// (empty)
			content = "(exit from paragraph)";
			color = colorMisc;
			break;
		case RuleConstants.PROD_EXIT_BODY_PROGRAM:	// <exit_body> ::= PROGRAM <exit_program_returning>
		case RuleConstants.PROD_EXIT_BODY_FUNCTION:	// <exit_body> ::= FUNCTION
			{
				content = CodeParser.getKeywordOrDefault("preReturn", "return");
				if (secRed.getParent().getTableIndex() == RuleConstants.PROD_EXIT_PROGRAM_RETURNING2) {
					content = this.getContent_R(secRed.get(1).asReduction(), content + " ");
				}
			}
			break;
		case RuleConstants.PROD_EXIT_BODY_PERFORM:	// <exit_body> ::= PERFORM
			content = CodeParser.getKeywordOrDefault("preLeave", "leave");
			break;
		case RuleConstants.PROD_EXIT_BODY_PERFORM_CYCLE: // <exit_body> ::= PERFORM CYCLE
			content = "continue";	// may even work in some code exports, can be understood
			color = Color.RED;
			comment = "Unsupported kind of JUMP, was: " + this.getContent_R(_reduction, "");
			break;
		case RuleConstants.PROD_EXIT_BODY_SECTION:	// <exit_body> ::= SECTION
		case RuleConstants.PROD_EXIT_BODY_PARAGRAPH:// <exit_body> ::= PARAGRAPH
			// START KGU#464 2017-12-04: Bugfix #475 - If we are in an appropriate context, we may generate Return instruction
			//content = this.getContent_R(_reduction, "");
			//color = Color.RED;
			//comment = "Unsupported kind of JUMP";
			content = CodeParser.getKeywordOrDefault("preReturn", "return");
			comment = "EXIT " + secRed.get(0).asString();
			exitTarget = (secRuleId == RuleConstants.PROD_EXIT_BODY_SECTION) ? SoPTarget.SOP_SECTION : SoPTarget.SOP_PARAGRAPH;
			break;
			// END KGU#464 2017-12-04
		}
		if (content != null) {
			Jump jmp = new Jump(content.trim());
			this.equipWithSourceComment(jmp, _reduction);
			if (!comment.isEmpty()) {
				jmp.getComment().add(comment);
			}
			if (color != null) {
				jmp.setColor(color);
				jmp.disabled = true;
			}
			_parentNode.addElement(jmp);
			// START KGU#464 201-12-04: Bugfix #475
			if (exitTarget != SoPTarget.SOP_ANY) {
				registerExitInProcedureContext(jmp, exitTarget);
			}
			// END KGU#464 2017-12-04
		}
	}

	/**
	 * Checks whether there is a open section or paragraph context and if so marks it as
	 * containing an EXIT statement. Returns true if the category of the innermost context
	 * matches the argument.
	 * @param _exitSection - true if the EXIT statement was an EXIT SECTION
	 * @return true if the current procedure context is a section and {@code _exitSection} is true or
	 * if the context is a paragraph and {@code _exitSection} is false
	 */
	private void registerExitInProcedureContext(Jump _jump, SoPTarget _target) {
		if (!this.procedureList.isEmpty()) {
			for (SectionOrParagraph sop: this.procedureList) {

				if (sop.endsBefore < 0) {
					if (_target == SoPTarget.SOP_SECTION) {
						sop.sectionExits.add(_jump);
					}
					else {
						sop.paragraphExits.add(_jump);
					}
					return;
				}
			}
		}
	}

	/**
	 * Builds a loop or Call element from the PERFORM statement represented by {@code _reduction}.
	 * @param _reduction - the top Reduction of the parsed PERFORM statement
	 * @param _parentNode - the Subqueue to append the built elements to
	 * @throws ParserCancelled
	 */
	private final void importPerform(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled {
		// We will have to find out what kind of loop this is.
		// In the worst case the body is just a paragraph name (PROD_PERFORM_BODY), which
		// forces us to find that paragraph and to copy its content into the body Subqueue.
		// The best case would be PROD_PERFORM_BODY2 - then the body is just a statement list
		// Then there is the case of an empty body (PROD_PERFORM_BODY3) - only an option is given
		Reduction bodyRed = _reduction.get(1).asReduction();
		int bodyRuleId = bodyRed.getParent().getTableIndex();
		int optionIx = (bodyRuleId == RuleConstants.PROD_PERFORM_BODY) ? 1 : 0;
		Reduction optRed = bodyRed.get(optionIx).asReduction();
		String content = "";
		ILoop loop = null;
		//System.out.println("\t" + optRed.getParent().toString());
		switch (optRed.getParent().getTableIndex()) {
		case RuleConstants.PROD_PERFORM_OPTION_TIMES:
			// FOR loop
			{
				// Prepare a generic variable name
				content = this.getContent_R(optRed.get(0).asReduction(), content);
				loop = new For("varStructorizer", "1", content, 1);
				this.equipWithSourceComment((For)loop, _reduction);
				content = ((For)loop).getText().getLongString();
				((For)loop).setText(content.replace("varStructorizer", "var" + loop.hashCode()));
			}
			break;
		case RuleConstants.PROD_PERFORM_OPTION_VARYING:	// <perform_option> ::= <perform_test> VARYING <perform_varying_list>
			// FOR loop
			{
				// Classify the test position
				Reduction testRed = optRed.get(0).asReduction();
				Reduction controlRed = optRed.get(2).asReduction();
				boolean testAfter = testRed.getParent().getTableIndex() == RuleConstants.PROD_PERFORM_TEST_TEST
						&& testRed.get(2).asReduction().getParent().getTableIndex() == RuleConstants.PROD_BEFORE_OR_AFTER_AFTER
						|| controlRed.getParent().getTableIndex() == RuleConstants.PROD_PERFORM_VARYING_LIST_AFTER;
				// Get actual FOR clause parameters
				Reduction forRed = controlRed;
				if (controlRed.getParent().getTableIndex() == RuleConstants.PROD_PERFORM_VARYING_LIST_AFTER) {
					forRed = controlRed.get(controlRed.size()-1).asReduction();
				}
				Reduction condRed = forRed.get(6).asReduction();
				String varName = this.getContent_R(forRed.get(0).asReduction(), "").replaceAll("-", "_");
				String from = this.getContent_R(forRed.get(2).asReduction(), "");
				String by = this.getContent_R(forRed.get(4).asReduction(), "");
				String cond = this.getContent_R(condRed, "").trim();
				// Check whether the condition might be composed and build a while loop in this case!
				StringList condTokens = Element.splitLexically(cond, true);
				// FIXME this is just a quick hack
				int step = 0;
				try {
					step = Integer.parseInt(by);
				}
				catch (NumberFormatException ex) {}
				if (step == 0
						|| step > 0 && !cond.matches(BString.breakup(varName) + "\\s* > .*") && !cond.matches(".* < \\s*" + BString.breakup(varName))
						|| step < 0 && !cond.matches(BString.breakup(varName) + "\\s* < .*") && !cond.matches(".* > \\s*" + BString.breakup(varName))
						|| condTokens.contains("or")
						|| condTokens.contains("and")
						|| condTokens.contains("not")
						|| condTokens.contains("xor")) {
					Instruction init = new Instruction(varName + " <- " + from);
					// Mark this part of the decomposed not-exactly FOR loop
					init.setColor(colorMisc);
					_parentNode.addElement(init);
					While wloop = new While(negateCondition(cond));
					if (bodyRuleId == RuleConstants.PROD_PERFORM_BODY2) {
						this.buildNSD_R(bodyRed.get(1).asReduction(), wloop.getBody());
						Instruction incr = new Instruction(varName + " <- " + varName + " + (" + by + ")");
						// Mark the parts of the decomposed not-exactly FOR loop
						incr.setColor(colorMisc);
						wloop.setColor(colorMisc);
						wloop.getBody().addElement(incr);
						_parentNode.addElement(this.equipWithSourceComment(wloop, _reduction));
					}
					else {
						Instruction defective = new Instruction(this.getContent_R(_reduction, ""));
						defective.setColor(Color.RED);
						defective.disabled = true;
						_parentNode.addElement(this.equipWithSourceComment(defective, _reduction));
					}
				}
//				else if (testAfter) {
//					// TODO: We will have to convert the loop to a REPEAT loop
//				}
				else {
					if (cond.matches(BString.breakup(varName) + "\\s* [<>] .*")) {
						cond = cond.replaceAll(BString.breakup(varName) + "\\s* [<>] (.*)", "$1");
					}
					else {
						cond = cond.replaceAll("(.*) [<>] \\s*" + BString.breakup(varName), "$1");
					}
					loop = new For(varName, from, cond.trim(), step);
					this.equipWithSourceComment((For)loop, _reduction);
					// FIXME: This should have become superfluous as soon as the conversion to a REPEAT loop above is implemented
					if (testAfter) {
						((For)loop).setComment("WARNING: In the original code this loop was specified to do the test AFTER the body!");
					}
				}
			}
			break;
		case RuleConstants.PROD_PERFORM_OPTION_FOREVER:
			// FOREVER loop
			loop = new Forever();
			this.equipWithSourceComment((Forever)loop, _reduction);
			break;
		case RuleConstants.PROD_PERFORM_OPTION_UNTIL:
			// WHILE or REPEAT loop
			{
				// Get the condition itself
				//content = this.getContent_R(optRed.get(2).asReduction(), "");
				content = this.transformCondition(optRed.get(2).asReduction(), null);
				// Classify the test position
				Reduction testRed = optRed.get(0).asReduction();
				if (testRed.getParent().getTableIndex() == RuleConstants.PROD_PERFORM_TEST
						|| testRed.get(2).asReduction().getParent().getTableIndex() == RuleConstants.PROD_BEFORE_OR_AFTER_BEFORE) {
					loop = new While(negateCondition(content));
					this.equipWithSourceComment((While)loop, _reduction);
				}
				else {
					loop = new Repeat(content);
					this.equipWithSourceComment((Repeat)loop, _reduction);
				}
			}
			break;
		case RuleConstants.PROD_PERFORM_OPTION:
			// Just a macro (block):	PERFORM x
			buildPerformCall(bodyRed, _parentNode);
			break;
		default:
			// CHECKME: Should never be reached	anymore
			getLogger().log(Level.INFO, "UNRECOGNIZED: Index {0} {1}",
					new Object[]{optRed.getParent().getTableIndex(), this.getContent_R(_reduction, "")});
			buildPerformCall(bodyRed, _parentNode);
		}
		if (loop != null) {
			switch (bodyRed.getParent().getTableIndex()) {
			case RuleConstants.PROD_PERFORM_BODY:
				// <perform_body> ::= <perform_procedure> <perform_option>
				// a macro (block) within a loop:	PERFORM x UNTIL ...
 				this.buildPerformCall(bodyRed, loop.getBody());
				break;
			case RuleConstants.PROD_PERFORM_BODY2:
				// <perform_body> ::= <perform_option> <statement_list> <end_perform>
				this.buildNSD_R(bodyRed.get(1).asReduction(), loop.getBody());
				break;
			default:
				// FIXME
				getLogger().log(Level.INFO, "We have no idea how to convert this: {0}", this.getContent_R(_reduction, ""));
			}
			_parentNode.addElement((Element)loop);
		}
	}

	/**
	 * Builds a Call element from the PERFORM statement for PROD_PERFORM_BODY represented by {@code _reduction}.
	 * @param _reduction - the top Reduction of the parsed PERFORM statement
	 * @param _parentNode - the Subqueue to append the built elements to
	 * @throws ParserCancelled
	 */
	private final void buildPerformCall(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled {
		// <perform_body> ::= <perform_procedure> <perform_option>
		// Ideally we find the named label and either copy its content into the body Subqueue or
		// export it to a new NSD.
		String name = this.getContent_R(_reduction.get(0).asReduction(), "").trim();
		if (Character.isDigit(name.charAt(0))) {
			name = "sub" + name;
		}
		String content = name + "()";
		Call dummyCall = new Call(content);
		dummyCall.setColor(Color.RED);
		this.equipWithSourceComment(dummyCall, _reduction);
		dummyCall.getComment().add("This was a call of an internal section or paragraph");
		_parentNode.addElement(dummyCall);
		// Now we register the call for later linking
		LinkedList<Call> otherCalls = this.internalCalls.get(name.toLowerCase());
		if (otherCalls == null) {
			otherCalls = new LinkedList<Call>();
			this.internalCalls.put(name.toLowerCase(), otherCalls);
		}
		otherCalls.add(dummyCall);
	}

	/**
	 * Builds Case elements or nested Alternatives from the EVALUATE statement
	 * represented by {@code _reduction}.
	 * @param _reduction - the top Reduction of the parsed EVALUATE statement
	 * @param _parentNode - the Subqueue to append the built elements to
	 * @throws ParserCancelled
	 */
	private final void importEvaluate(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled {
		// Possibly a CASE instruction, may have to be decomposed to an IF chain.
		Reduction secRed = _reduction.get(1).asReduction();	// <evaluate_body>
		Reduction subjlRed = secRed.get(0).asReduction();	// <evaluate_subject_list>
		Reduction condlRed = secRed.get(1).asReduction();		// <evaluate_condition_list>
		log(subjlRed.getParent().toString(), false);
		log(condlRed.getParent().toString(), false);
		int subjlRuleId = subjlRed.getParent().getTableIndex();
		if (subjlRuleId == RuleConstants.PROD_EVALUATE_SUBJECT_LIST_ALSO) {
			// TODO: merge this with the subsequent case!
			// This can only be represented by nested alternatives
			//System.out.println("EVALUATE: PROD_EVALUATE_SUBJECT_LIST_ALSO");
			StringList subjects = this.getExpressionList(subjlRed, "<evaluate_subject_list>", -1);
			Reduction otherRed = null;
			Element elseBranch = null;
			if (condlRed.getParent().getTableIndex() == RuleConstants.PROD_EVALUATE_CONDITION_LIST) {
				otherRed = condlRed.get(1).asReduction();	// <evaluate_other>
				elseBranch = new Subqueue();
				buildNSD_R(otherRed.get(2).asReduction(), (Subqueue)elseBranch);
				condlRed = condlRed.get(0).asReduction();	// <evaluate_case_list>
			}
			// condlRed should be an "<evaluate_case_list>" (being either an <evaluate_case> or recursive)
			do {
				String caseHead = condlRed.getParent().getHead().toString();
				Reduction caseRed = condlRed;	// could be <evaluate_case>
				if (caseHead.equals("<evaluate_case_list>")) {
					caseRed = condlRed.get(condlRed.size()-1).asReduction();	// get the <evaluate_case>
				}
				// Get the condition(s)
				Reduction whenlRed = caseRed.get(0).asReduction();	// <evaluate_when_list>
				String conds = "";
				while (whenlRed != null) {
					String cond = "";
					Reduction condRed = null;
					if (whenlRed.getParent().getTableIndex() == RuleConstants.PROD_EVALUATE_WHEN_LIST_WHEN2) {
						condRed = whenlRed.get(2).asReduction();
						whenlRed = whenlRed.get(0).asReduction();
					}
					else {
						// FIXME: provide the subject of the first condition (i.e. the discriminator...)
						condRed = whenlRed.get(1).asReduction();
						whenlRed = null;
					}
					// FIXME: Set up a list of <evaluate_object> reductions to match against the subjects
					LinkedList<Reduction> objReds = new LinkedList<Reduction>();
					do {
						Reduction objRed = condRed;
						if (condRed.getParent().getTableIndex() == RuleConstants.PROD_EVALUATE_OBJECT_LIST_ALSO) {
							objRed = condRed.get(2).asReduction();
							condRed = condRed.get(0).asReduction();
						}
						else {
							condRed = null;
						}
						objReds.addFirst(objRed);
					} while (condRed != null);
					int i = 0;
					for (Reduction objRed: objReds) {
						if (objRed.getParent().getTableIndex() != RuleConstants.PROD_EVALUATE_OBJECT_ANY) {
							if (i >= subjects.count()) {
								log(this.getOriginalText(_reduction, "Too many selection objects in the WHEN clause of: "), true);
								break;
							}
							boolean negated = false;
							String subject = subjects.get(i).trim();
							if (subject.equals("true")) {
								subject = "";
							} else if (subject.equals("false")) {
								subject = "";
								negated = true;
							}
							String partCond = this.transformCondition(objRed, subjects.get(i));
							if (negated) {
								partCond = this.negateCondition(partCond);
							}
							cond += (cond.trim().isEmpty() ? "" : " and ") + "(" + partCond + ")";
						}
						i++;
					}
					conds = "(" + cond + ") or " + conds;
				}
				conds = conds.trim();
				if (conds.endsWith(" or")) {
					conds = conds.substring(0, conds.length()-" or".length());
				}
				Alternative alt = new Alternative(conds);
				// Get the instruction part
				if (elseBranch instanceof Subqueue) {
					alt.qFalse = (Subqueue)elseBranch;
					elseBranch.parent = alt;
				}
				else if (elseBranch != null) {
					alt.qFalse.addElement(elseBranch);
				}
				buildNSD_R(caseRed.get(1).asReduction(), alt.qTrue);	// <statement_list>
				elseBranch = alt;
				condlRed = (caseHead.equals("<evaluate_case_list>")) ? condlRed.get(0).asReduction() : null;
			} while (condlRed != null);
			if (elseBranch != null) {
				// elseBranch should not be a Subqueue here!
				_parentNode.addElement(elseBranch);
			}
		}
		else if (
				subjlRuleId == RuleConstants.PROD_EVALUATE_SUBJECT_TOK_TRUE
				||
				subjlRuleId == RuleConstants.PROD_EVALUATE_SUBJECT_TOK_FALSE
				) {
			// Independent conditions, will be converted to nested alternatives
			boolean negate = subjlRuleId == RuleConstants.PROD_EVALUATE_SUBJECT_TOK_FALSE;
			//System.out.println("\tEVALUATE: PROD_EVALUATE_SUBJECT_TOK_TRUE/FALSE");
			Reduction otherRed = null;
			Element elseBranch = null;
			if (condlRed.getParent().getTableIndex() == RuleConstants.PROD_EVALUATE_CONDITION_LIST) {
				otherRed = condlRed.get(1).asReduction();	// <evaluate_other>
				elseBranch = new Subqueue();
				buildNSD_R(otherRed.get(2).asReduction(), (Subqueue)elseBranch);
				condlRed = condlRed.get(0).asReduction();	// <evaluate_case_list>
			}
			// condlRed should be an "<evaluate_case_list>" (being either an <evaluate_case> or recursive)
			do {
				String caseHead = condlRed.getParent().getHead().toString();
				Reduction caseRed = condlRed;	// could be <evaluate_case>
				if (caseHead.equals("<evaluate_case_list>")) {
					caseRed = condlRed.get(condlRed.size()-1).asReduction();	// get the <evaluate_case>
				}
				// Get the condition(s)
				Reduction whenlRed = caseRed.get(0).asReduction();	// <evaluate_when_list>
				String conds = "";
				while (whenlRed != null) {
					String cond = "";
					Reduction condRed = null;
					/* the token list is reversed -> as long as we get PROD_EVALUATE_WHEN_LIST_WHEN2
					   we have more braching parts to check */
					if (whenlRed.getParent().getTableIndex() == RuleConstants.PROD_EVALUATE_WHEN_LIST_WHEN2) {
						condRed = whenlRed.get(2).asReduction();
						whenlRed = whenlRed.get(0).asReduction();
					}
					else { // PROD_EVALUATE_WHEN_LIST_WHEN -> last branch (first in the code)
						condRed = whenlRed.get(1).asReduction();
						whenlRed = null;
					}
					cond = this.transformCondition(condRed, "");
					if (negate) {
						cond = this.negateCondition(cond);
					}
					conds = cond + " or " + conds;
				}
				conds = conds.trim();
				if (conds.endsWith(" or")) {
					// fixing reversed token list by prefixing conds with the current one
					conds = conds.substring(0, conds.length()-" or".length());
				}
				Alternative alt = new Alternative(conds);
				// Get the instruction part
				if (elseBranch instanceof Subqueue) {
					alt.qFalse = (Subqueue)elseBranch;
					elseBranch.parent = alt;
				}
				else if (elseBranch != null) {
					alt.qFalse.addElement(elseBranch);
				}
				buildNSD_R(caseRed.get(1).asReduction(), alt.qTrue);	// <statement_list>
				elseBranch = alt;
				condlRed = (caseHead.equals("<evaluate_case_list>")) ? condlRed.get(0).asReduction() : null;
			} while (condlRed != null);
			if (elseBranch != null) {
				// elseBranch should not be a Subqueue here!
				_parentNode.addElement(elseBranch);
			}
		}
		else {
			// Single discriminator expression - there might be a chance to convert this to a CASE element
			//System.out.println("\tEVALUATE: PROD_EVALUATE_SUBJECT_LIST");
			StringList caseText = StringList.getNew(this.getContent_R(subjlRed, ""));
			int caseVarStringLength = 0;
			if (caseText != null) {
				String possibleVarName = caseText.toString();
				if (possibleVarName.matches("\".+\"")) {
					possibleVarName = possibleVarName.substring(1, possibleVarName.length()-1);
				}
				CobVar caseVar = currentProg.getCobVar(possibleVarName);
				String type = CobTools.getTypeString(caseVar, false);
				if (type != null && type.equals("String")) {
					caseVarStringLength = caseVar.getCharLength();
				}
			}

			Case ele = new Case();
			// Now analyse the branches
			Reduction otherRed = null;
			if (condlRed.getParent().getTableIndex() == RuleConstants.PROD_EVALUATE_CONDITION_LIST) {
				otherRed = condlRed.get(1).asReduction();	// <evaluate_other>
				Subqueue sq = new Subqueue();
				sq.parent = ele;
				buildNSD_R(otherRed.get(2).asReduction(), sq);
				caseText.add("default");	// last line for default branch
				condlRed = condlRed.get(0).asReduction();	// <evaluate_case_list>
				ele.qs.addElement(sq);
			}
			else {
				caseText.add("%");	// suppress the default branch
				ele.qs.addElement(new Subqueue());
			}
			// condlRed should be an "<evaluate_case_list>" (being either an <evaluate_case> or recursive)
			do {
				String caseHead = condlRed.getParent().getHead().toString();
				Reduction caseRed = condlRed;	// could be <evaluate_case>
				if (caseHead.equals("<evaluate_case_list>")) {
					caseRed = condlRed.get(condlRed.size()-1).asReduction();	// get the <evaluate_case>
				}
				// Get the instruction part
				Subqueue sq = new Subqueue();
				sq.parent = ele;
				buildNSD_R(caseRed.get(1).asReduction(), sq);	// <statement_list>
				ele.qs.add(0, sq);
				// Now collect the WHEN clauses and concoct the compound condition
				Reduction whenlRed = caseRed.get(0).asReduction();	// <evaluate_when_list>
				String selectors = null;
				while (whenlRed != null) {
					String selector = null;
					// FIXME: At this point we cannot handle incomplete expressions sensibly
					// (as soon as we bump into an incomplete comparison expression or the like we
					// would have had to convert the entire CASE element into a nested alternative tree.
					// The trouble is that all kinds of selectors (literals, complete expressions, and
					// incomplete expressions may occur among the listed selectors.
					if (whenlRed.getParent().getTableIndex() == RuleConstants.PROD_EVALUATE_WHEN_LIST_WHEN2) {
						selector = this.getContent_R(whenlRed.get(2).asReduction(), "");
						whenlRed = whenlRed.get(0).asReduction();
					} else {
						selector = this.getContent_R(whenlRed.get(1).asReduction(), "");
						whenlRed = null;
					}
					// special case: case is alphanumeric while selector isn't --> transform selector
					if (caseVarStringLength != 0 && selector.matches("[0-9]+")) {
						int selLitSize = selector.length();
						while (selLitSize < caseVarStringLength) {
							selector = "0" + selector;
							selLitSize++;
						}
						selector = "\"" + selector + "\"";
					}
					if (selectors != null) {
						selectors = selector + ", " + selectors;
					} else {
						selectors = selector;
					}
				}
//				selectors = selectors.trim();
//				if (selectors.endsWith(",")) {
//					selectors = selectors.substring(0, selectors.length()-1);
//				}
				caseText.insert(selectors, 1);
				condlRed = (caseHead.equals("<evaluate_case_list>")) ? condlRed.get(0).asReduction() : null;
			} while (condlRed != null);
			ele.setText(caseText);
			_parentNode.addElement(this.equipWithSourceComment(ele, _reduction));
		}
	}

//	private String appendDisplayBody(Reduction bodyRed, String content) {
//		Reduction secRed = bodyRed.get(0).asReduction();
//		switch (secRed.getParent().getTableIndex()) {
//		case RuleConstants.PROD_ID_OR_LIT:
//		case RuleConstants.PROD_ID_OR_LIT2:
//			content = this.getContent_R(secRed, content + ", ");
//			break;
//		case RuleConstants.PROD_SCREEN_OR_DEVICE_DISPLAY:
//		case RuleConstants.PROD_SCREEN_OR_DEVICE_DISPLAY2:
//			break;
//		}
//		return content;
//	}

	/**
	 * Helper method for importing ADD, SUBTRACT, MULTIPLY, and DIVIDE statements, generates the text for the
	 * arithmetic instruction series
	 * @param content - the multi-line Instruction text to append the specified assignment to
	 * @param operator - the operator symbol t be used (one of "+", "-", "*", "/").
	 * @param target - the variable (name) the expression result is to be assigned to (as string)
	 * @param operand1 - first operand (as string)
	 * @param operand2 - second operand (as string)
	 * @param prevResult - the result of the previous operation (an expression, preferrably a variable name)
	 * @return a String representing the result of the assignment (to be used as prevResult in the next
	 *  call of this routine)
	 */
	private final String addArithmOperation(StringList content, String operator, String target, String operand1, String operand2, String prevResult) {
		final String rounded = " ROUNDED ";
		String rounder = "%%%";
		int posRounded = target.toUpperCase().indexOf(rounded);
		if (posRounded > 0) {
			rounder = this.deriveRoundingFunction(target.substring(posRounded + rounded.length()));
			target = target.substring(0, posRounded).trim();
		}
		if (prevResult == null) {
			// operand1 may contain a rounding clause if it's the result at the same time
			if ((posRounded = operand1.indexOf(rounded)) > 0) {
				operand1 = operand1.substring(0, posRounded);
			}
			// prepare the assignment
			prevResult = operand1 + " " + operator + " " + operand2;
		}
		content.add(target + " <- " + rounder.replace("%%%", prevResult));
		// If the current target is not identical to the dividend and it does not contain
		// a rounded result then we may reuse the result saved in target
		if (!operand1.trim().equalsIgnoreCase(target) && rounder.equals("%%%")) {
			prevResult = target;
		}
		return prevResult;
	}

	private final String deriveRoundingFunction(String roundingClause) {
		String[] tokens = roundingClause.split("\\s+");
		roundingClause = tokens[tokens.length-1];
		String pattern = "%%%";
		if (roundingClause.equalsIgnoreCase("AWAY-FROM-ZERO")) {
			pattern = "sgn(%%%) * round(ceil(abs(%%%)))";
		}
		else if (roundingClause.equalsIgnoreCase("TRUNCATION")) {
			pattern = "sgn(%%%) * round(floor(abs(%%%)))";
		}
		else if (roundingClause.toUpperCase().startsWith("NEAREST-")) {
			// FIXME This is too simple of course, but fine-tuning can be done later...
			// a built-in function round_even in Structorizer would be helpful
			pattern = "round(%%%)";
		}
		else if (roundingClause.equalsIgnoreCase("TOWARD-GREATER")) {
			pattern = "round(ceil(%%%))";
		}
		else if (roundingClause.equalsIgnoreCase("TOWARD-LESSER")) {
			pattern = "round(floor(%%%))";
		}
		return pattern;
	}

	private final void processDataDescriptions(Reduction _reduction, HashMap<String, String> _typeInfo) throws ParserCancelled
	{
		int ruleId = _reduction.getParent().getTableIndex();
		if (ruleId == RuleConstants.PROD_DATA_DESCRIPTION4)
		{
			// NOTE: we can never get any constants here as there are seperate rules for them
			//System.out.println("PROD_DATA_DESCIPTION4");

			int level = 0;
			try {
				level = Integer.parseInt(this.getContent_R(_reduction.get(0).asReduction(), ""));
			} catch (Exception ex) {
			}
			// We must suppress automatic name qualification here
			String varName = this.getWord(_reduction.get(1));
			Reduction seqRed = _reduction.get(2).asReduction();
			String value = null;
			//				String valueFalse = null;
			String picture = null;
			String redefines = null;
			String occursString = null;
			int occurs = 0;
			StringList indexVars = null;
			CobTools.Usage usage = null;
			boolean isGlobal = false;
			boolean isExternal = false;
			int anyLength = 0;
			// START KGU#427 2017-10-05: workaround for initialization bug (enh. #354)
			cobTools.setProgram(currentProg);
			// END KGU#427 2017-10-05
			// We may not do anything if description is empty
			while (seqRed.getParent().getTableIndex() == RuleConstants.PROD__DATA_DESCRIPTION_CLAUSE_SEQUENCE2) {
				Reduction descrRed = seqRed.get(1).asReduction();
				int descrRuleId = descrRed.getParent().getTableIndex();
				switch (descrRuleId) {
				case RuleConstants.PROD_PICTURE_CLAUSE_PICTURE_DEF: // <picture_clause> --> type info
					picture = descrRed.get(0).asString().split("\\s+", 3)[1];
					// assume the first token to be PIC or PICTURE and a second token to be available
					//(otherwise the grammar has a defect)
					break;
				case RuleConstants.PROD_USAGE_CLAUSE_USAGE: // <usage_clause> ::= USAGE <_is> <usage> --> type info
					usage = getUsageFromReduction(descrRed.get(2).asReduction());
					break;
				case RuleConstants.PROD_VALUE_CLAUSE_VALUE: // <value_clause> ::= VALUE <_is_are> <value_item_list> <_false_is>
					// note: only one entry for value clause of plain data items, no false clause
					value = this.getContent_R(descrRed.get(2).asReduction(), "");
					break;
				case RuleConstants.PROD_REDEFINES_CLAUSE_REDEFINES: // <redefines_clause> ::= REDEFINES <identifier_1>
					// shares the same memory area
					// FIXME at least add a comment
					redefines = this.getContent_R(descrRed.get(1).asReduction(), "");
					break;
				case RuleConstants.PROD_EXTERNAL_CLAUSE_EXTERNAL: // <external_clause>
					// only occurs on level 01/77, this record or single variable shares the same value in *independent* programs
					// which could but not have to be *nested* in general this is a rare cause but to be "correct" we would need
					// to share this variable in a single IMPORT NSD (name: var name)
					isExternal = true;
					break;
//				case RuleConstants.PROD_DATA_DESCRIPTION_CLAUSE3: // <global_clause> --> global import
				case RuleConstants.PROD_GLOBAL_CLAUSE_GLOBAL:	//<global_clause> ::= <_is> GLOBAL
					// only occurs on level 01/77, this record or single variable shares the same value in *nested* programs
					// in general this is a rare case but to be "correct" we would need to share this
					// variable in a single IMPORT NSD (name: first program's name that uses it + var name)
					isGlobal = true;
					break;
				case RuleConstants.PROD_ANY_LENGTH_CLAUSE_ANY_LENGTH: // <any_length_clause> ::= ANY LENGTH
					// only occurs on level 01/77, variable is alphanumeric and can have any length
					anyLength = 1;
					break;
				case RuleConstants.PROD_ANY_LENGTH_CLAUSE_ANY_NUMERIC: // <any_length_clause> ::= ANY NUMERIC
					// only occurs on level 01/77, variable is numeric, can have any usage and can have any length
					anyLength = 2;
					break;
					// START KGU 2017-10-04: We should of course be aware of array structure, too
					// FIXME handle the other types of OCCURS clause, too
				case RuleConstants.PROD_OCCURS_CLAUSE_OCCURS:
				{
					// FIXME: What about a possible integer-2 value?
					String int1 = this.getContent_R(descrRed.get(1).asReduction(), "");
					// It could be an integer literal, but be prepared to find a constant identifier instead
					CobVar int1const = currentProg.getCobVar(int1);
					if (int1const != null && int1const.isConstant(true)) {
						// This is quite nice but bad in practise: we lose the connection to the constant
						int1 = int1const.getValueFirst();
						// So we should store both the string and the value
						occursString = int1const.getQualifiedName();
					}
					// If it is a variant array then fetch the upper bound
					if (descrRed.get(2).asReduction().getParent().getTableIndex() == RuleConstants.PROD__OCCURS_TO_INTEGER_TO) {
						String int2 = this.getContent_R(descrRed.get(2).asReduction().get(1).asReduction(), "");
						CobVar int2const = currentProg.getCobVar(int2);
						if (int2const != null && int2const.isConstant(true)) {
							occursString = int2const.getQualifiedName();
							int2 = int2const.getValueFirst();
						}
						int1 = int2;
					}
					try {
						occurs = Integer.parseInt(int1);
						if (occursString == null) {
							occursString = Integer.toString(occurs);
						}
					}
					catch (NumberFormatException ex) {
					}
					// Get hold of a possible INDEXED BY clause, which may be needed for the
					// correct import of SEARCH statements etc.
					Reduction keyIxdRed = descrRed.get(5).asReduction();
					int ixIxd = -1;	// Token index of the INDEXED clause (if any, -1 = none)
					switch (keyIxdRed.getParent().getTableIndex()) {
					case RuleConstants.PROD__OCCURS_KEYS_AND_INDEXED2:
						ixIxd = 1;
						break;
					case RuleConstants.PROD__OCCURS_KEYS_AND_INDEXED3:
					case RuleConstants.PROD__OCCURS_KEYS_AND_INDEXED4:
						ixIxd = 2;
						break;
					}
					if (ixIxd >= 0) {
						keyIxdRed = keyIxdRed.get(ixIxd).asReduction();
					}
					if (keyIxdRed.getParent().getTableIndex() == RuleConstants.PROD_OCCURS_INDEXED_INDEXED) {
						keyIxdRed = keyIxdRed.get(2).asReduction();
						if (indexVars == null) {
							// We will definitely have to register index variables now
							//indexVars = new ArrayList<CobVar>();
							indexVars = new StringList();
						}
						while (keyIxdRed.getParent().getTableIndex() == RuleConstants.PROD_OCCURS_INDEX_LIST2) {
							String ixdName = this.getContent_R(keyIxdRed.get(0).asReduction(), "");
							// If we created a CobVar object (at level 01!) now, this might compromise the table structure we are
							// within, so we just register the names and good. An explicit declaration of such an index variable
							// isn't necessary anyway because SEARCH requires a prior initialization, wich will implicitly introduce
							// the variable in Structorizer
							//indexVars.add(cobTools.new CobVar(1, ixdName, null, Usage.USAGE_INDEX, null, null, isGlobal, isExternal, 0, 0, null));
							indexVars.add(ixdName.trim().toLowerCase());
							keyIxdRed = keyIxdRed.get(1).asReduction();
						}
						// This should now be a COBOL Word
						String ixdName = this.getContentToken_R(keyIxdRed.get(0), "", "", true);
						// If we created a CobVar object (at level 01!) now, this might compromise the table structure we are
						// within, so we just register the name and good. An explicit declaration of such an index variable
						// isn't necessary anyway because SEARCH requires a prior initialization, wich will implicitly introduce
						// the variable in Structorizer
						//indexVars.add(cobTools.new CobVar(1, ixdName, null, Usage.USAGE_INDEX, null, null, isGlobal, isExternal, 0, 0, null));
						indexVars.add(ixdName.trim().toLowerCase());
					}
					break;
				}
				// END KGU 2017-10-04
				default:
					// a variable without explicitly given USAGE (77 myvar COMP-2) goes here;
					usage = getUsageFromReduction(descrRed);
				}
				seqRed = seqRed.get(0).asReduction();
			}

			CobVar currentVar = cobTools.new CobVar(level, varName, picture, usage, value, currentProg.getCobVar(redefines), isGlobal, isExternal, anyLength, occurs, occursString);
			currentVar.setComment(this.retrieveComment(_reduction));
			// START KGU 2017-10-06: Support for tables (OCCURS ... INDEXED BY clause)
			if (indexVars != null) {
				currentVar.setIndexedBy(indexVars, currentProg);
			}
			// END KGU 2017-10-06
			currentProg.insertVar(currentVar);

			// The generation of NSD elements is postponed until everything will heve been parsed.
			// We may always get the start variable of each section via CobProg.getWorkingStorage(),
			// CobProg.getLinkage(), ...
			// and iterate by CobVar.sister, subs in CobVar.child, ... - with the complete type declarations!
		}
		else if (ruleId == RuleConstants.PROD_CONSTANT_ENTRY_CONSTANT) {
			boolean isGlobal = _reduction.get(3).asReduction().getParent().getTableIndex() == RuleConstants.PROD_CONST_GLOBAL_GLOBAL;
			String constName = this.getContent_R(_reduction.get(1).asReduction(), "");
			// NOTE: While the current grammar does not allow it we could have a constant expression here:
			// 01 myconst AS CONSTANT 55 - 33 / 12.
			String value = this.getContent_R(_reduction.get(4).asReduction().get(1).asReduction(), "");

			CobVar currentVar = cobTools.new CobVar(1, constName, value, isGlobal);
			currentVar.setComment(this.retrieveComment(_reduction));
			currentProg.insertVar(currentVar);

			String type = Element.identifyExprType(null, value, true);
			if (!type.isEmpty() && _typeInfo != null) {
				_typeInfo.put(constName, type);
			}
			// Leave this to this.buildDataSection(varRoot, externalNode, globalNode, localNode);
		}
		else if (ruleId == RuleConstants.PROD_CONSTANT_ENTRY_SEVENTY_EIGHT) {
			// Note: Though the current grammar still doesn't allow it, we could have a constant expression here e.g.:
			// 78 myval  VALUE 55 - 33 / 12.
			boolean isGlobal = _reduction.get(2).asReduction().getParent().getTableIndex() == RuleConstants.PROD_GLOBAL_CLAUSE_GLOBAL;
			String constName = this.getContent_R(_reduction.get(1).asReduction(), "");
			Reduction valClRed = _reduction.get(3).asReduction(); // <value_clause>
			StringList values = this.getExpressionList(valClRed.get(2).asReduction(), "<value_item_list>",
					RuleConstants.PROD_VALUE_ITEM_COMMA_DELIM); // FIXME: the parser should not get the COMMA_DELIM and normally spaces are used

			cobTools.setProgram(currentProg);
			CobVar currentVar = cobTools.new CobVar(78, constName, values.get(0), isGlobal);
			currentVar.setComment(this.retrieveComment(_reduction));
			currentProg.insertVar(currentVar);

//			String value = null;
//			String type = ""; // unused
//			if (values.count() == 1) {
//				value = values.get(0).trim();
//				type = Element.identifyExprType(null, value, true); // unused
//			}
//			else {
//				value = "{" + values.concatenate(", ") + "}";
//			}
//			// START KGU 2017-10-04 Now leave this to this.buildDataSection(varRoot, externalNode, globalNode, localNode);
//			if (_parentNode != null && value != null) {
//				// FIXME: in case of isGlobal enforce the placement in a global diagram to be imported wherever needed
//				Instruction def = new Instruction("const " + currentVar.getName() + " <- " + value);
//				def.setColor(colorConst);
//				_parentNode.addElement(this.equipWithSourceComment(def, _reduction));
//			}
			// END KGU 2017-10-04
		}
		else if (ruleId == RuleConstants.PROD_CONDITION_NAME_ENTRY_EIGHTY_EIGHT) {
			// <condition_name_entry> ::= 'EIGHTY_EIGHT' <user_entry_name> <value_clause>
			String condName = this.getContent_R(_reduction.get(1).asReduction(), "");
			String valueFalse = null;
			String[] values = null;

			Reduction valClRed = _reduction.get(2).asReduction(); // result: <value_clause> ::= VALUE <_is_are> <value_item_list> <_false_is>
			StringList valuesList = this.getExpressionList(valClRed.get(2).asReduction(), "<value_item_list>",
					RuleConstants.PROD_VALUE_ITEM_COMMA_DELIM); // FIXME: the parser should not get the COMMA_DELIM and normally spaces are used
			Reduction valFalseRed = valClRed.get(3).asReduction(); // result: <_false_is> ::= <_when_set_to> 'TOK_FALSE' <_is> <lit_or_length>
			if (valFalseRed != null && !valFalseRed.isEmpty()) {
				valueFalse = this.getContent_R(valFalseRed.get(3).asReduction(), "");
			}

			values = valuesList.toArray();

			CobVar currentVar = cobTools.new CobVar(condName, values, valueFalse);
			currentProg.insertVar(currentVar);
		}
		else {
			for (int i = 0; i < _reduction.size(); i++) {
				if (_reduction.get(i).getType() == SymbolType.NON_TERMINAL) {
					this.processDataDescriptions(_reduction.get(i).asReduction(), _typeInfo);
				}
			}
		}
	}

	private Usage getUsageFromReduction(Reduction usageRed) {
		int descrRuleId = usageRed.getParent().getTableIndex();
		switch (descrRuleId) {
//		Should not be needed as we check the sub-values directly
		case RuleConstants.PROD_USAGE:                             // <usage> ::= <float_usage>
//			return CobTools.Usage.USAGE_FLOAT;                    // COMP-1 + FLOAT-SHORT
			return getUsageFromReduction(usageRed.get(0).asReduction());
		case RuleConstants.PROD_USAGE2:                            // <usage> ::= <double_usage>
//			return CobTools.Usage.USAGE_DOUBLE;                   // COMP-2 + FLOAT-LONG
			return getUsageFromReduction(usageRed.get(0).asReduction());
		case RuleConstants.PROD_USAGE_DISPLAY:                     // <usage> ::= DISPLAY
			return CobTools.Usage.USAGE_DISPLAY;
		case RuleConstants.PROD_USAGE_POINTER:                     // <usage> ::= POINTER
			return CobTools.Usage.USAGE_POINTER;
		case RuleConstants.PROD_USAGE_PROGRAM_POINTER:             // <usage> ::= 'PROGRAM_POINTER'
			return CobTools.Usage.USAGE_PROGRAM_POINTER;
		case RuleConstants.PROD_USAGE_BINARY_CHAR:                 // <usage> ::= 'BINARY_CHAR' <_signed>
			return CobTools.Usage.USAGE_SIGNED_CHAR;
		case RuleConstants.PROD_USAGE_BINARY_CHAR_UNSIGNED:        // <usage> ::= 'BINARY_CHAR' UNSIGNED
			return CobTools.Usage.USAGE_UNSIGNED_CHAR;
		case RuleConstants.PROD_USAGE_SIGNED_SHORT:                // <usage> ::= 'SIGNED_SHORT'
		case RuleConstants.PROD_USAGE_BINARY_SHORT:                // <usage> ::= 'BINARY_SHORT' <_signed>
			return CobTools.Usage.USAGE_SIGNED_SHORT;
		case RuleConstants.PROD_USAGE_UNSIGNED_SHORT:              // <usage> ::= 'UNSIGNED_SHORT'
		case RuleConstants.PROD_USAGE_BINARY_SHORT_UNSIGNED:       // <usage> ::= 'BINARY_SHORT' UNSIGNED
			return CobTools.Usage.USAGE_UNSIGNED_SHORT;
		case RuleConstants.PROD_USAGE_INDEX:                       // <usage> ::= INDEX
			return CobTools.Usage.USAGE_INDEX;
		case RuleConstants.PROD_USAGE_SIGNED_INT:                  // <usage> ::= 'SIGNED_INT'
			return CobTools.Usage.USAGE_SIGNED_INT;
		case RuleConstants.PROD_USAGE_UNSIGNED_INT:                // <usage> ::= 'UNSIGNED_INT'
			return CobTools.Usage.USAGE_UNSIGNED_INT;
		case RuleConstants.PROD_USAGE_SIGNED_LONG:                 // <usage> ::= 'SIGNED_LONG'
		case RuleConstants.PROD_USAGE_BINARY_C_LONG:               // <usage> ::= 'BINARY_C_LONG' <_signed>
			if (is32bit) {
				return CobTools.Usage.USAGE_SIGNED_INT;			// correct on 32bit
			} else {
				return CobTools.Usage.USAGE_SIGNED_LONG;		// correct on 64bit
			}
		case RuleConstants.PROD_USAGE_UNSIGNED_LONG:               // <usage> ::= 'UNSIGNED_LONG'
		case RuleConstants.PROD_USAGE_BINARY_C_LONG_UNSIGNED:      // <usage> ::= 'BINARY_C_LONG' UNSIGNED
			if (is32bit) {
				return CobTools.Usage.USAGE_UNSIGNED_INT;			// correct on 32bit
			} else {
				return CobTools.Usage.USAGE_UNSIGNED_LONG;		// correct on 64bit
			}
		case RuleConstants.PROD_USAGE_BINARY_DOUBLE:               // <usage> ::= 'BINARY_DOUBLE' <_signed>
			return CobTools.Usage.USAGE_SIGNED_LONG;
		case RuleConstants.PROD_USAGE_BINARY_DOUBLE_UNSIGNED:      // <usage> ::= 'BINARY_DOUBLE' UNSIGNED
			return CobTools.Usage.USAGE_UNSIGNED_LONG;
		case RuleConstants.PROD_USAGE_BINARY_LONG:                 // <usage> ::= 'BINARY_LONG' <_signed>
			return CobTools.Usage.USAGE_SIGNED_INT;
		case RuleConstants.PROD_USAGE_BINARY_LONG_UNSIGNED:        // <usage> ::= 'BINARY_LONG' UNSIGNED
			return CobTools.Usage.USAGE_UNSIGNED_INT;
		case RuleConstants.PROD_USAGE_BINARY:                      // <usage> ::= 'BINARY'
		case RuleConstants.PROD_USAGE_COMP:                        // <usage> ::= 'COMP'
		case RuleConstants.PROD_USAGE_COMP_4:                      // <usage> ::= 'COMP_4'
			return CobTools.Usage.USAGE_BINARY;
		case RuleConstants.PROD_USAGE_COMP_5:                      // <usage> ::= 'COMP_5'
			return CobTools.Usage.USAGE_COMP_5;
		case RuleConstants.PROD_USAGE_COMP_6:                      // <usage> ::= 'COMP_6'
			return CobTools.Usage.USAGE_COMP_6;
		case RuleConstants.PROD_USAGE_COMP_X:                      // <usage> ::= 'COMP_X'
			return CobTools.Usage.USAGE_COMP_X;
		case RuleConstants.PROD_USAGE_FLOAT_BINARY_32:             // <usage> ::= 'FLOAT_BINARY_32'
			return CobTools.Usage.USAGE_FP_BIN32;
		case RuleConstants.PROD_FLOAT_USAGE_COMP_1:
		case RuleConstants.PROD_FLOAT_USAGE_FLOAT_SHORT:
			return CobTools.Usage.USAGE_FLOAT;
		case RuleConstants.PROD_USAGE_COMP_3:                      // <usage> ::= 'COMP_3'
		case RuleConstants.PROD_USAGE_PACKED_DECIMAL:              // <usage> ::= 'PACKED_DECIMAL'
			return CobTools.Usage.USAGE_PACKED;
		case RuleConstants.PROD_USAGE_FLOAT_BINARY_64:             // <usage> ::= 'FLOAT_BINARY_64'
			return CobTools.Usage.USAGE_FP_BIN64;
		case RuleConstants.PROD_USAGE_FLOAT_BINARY_128:            // <usage> ::= 'FLOAT_BINARY_128'
			return CobTools.Usage.USAGE_FP_BIN128;
		case RuleConstants.PROD_USAGE_FLOAT_DECIMAL_16:            // <usage> ::= 'FLOAT_DECIMAL_16'
			return CobTools.Usage.USAGE_FP_DEC64;
		case RuleConstants.PROD_USAGE_FLOAT_DECIMAL_34:            // <usage> ::= 'FLOAT_DECIMAL_34'
			return CobTools.Usage.USAGE_FP_DEC128;
		case RuleConstants.PROD_DOUBLE_USAGE_FLOAT_LONG:
		case RuleConstants.PROD_DOUBLE_USAGE_COMP_2:
			return CobTools.Usage.USAGE_DOUBLE;
		}
		return null;
	}

	/**
	 * Traverses the recursive rule {@code _paramRed} to obtain a left-to-right
	 * list of argument declarations (e.g. for a function call), particularly
	 * coping with omittable arguments.
	 * @param _paramRed - the rule recursively comprising the argument list
	 * @param _listHead - the rule head representing the recursive part
	 * @param _ruleId - id of a rule terminating the exploration
	 * @param _nameIx - index of the name token within the reduction
	 * @return list of expressions as strings
	 * @throws ParserCancelled
	 * @see #getParameterList(Reduction, String, int, int)
	 */
	private final StringList getParameterList(Reduction _paramlRed, String _listHead, int _ruleId, int _nameIx) throws ParserCancelled {
		StringList args = new StringList();
		do {
			String paramHead = _paramlRed.getParent().getHead().toString();
			Reduction paramRed = _paramlRed;	// could be <call_param>
			if (paramHead.equals(_listHead)) {
				paramRed = _paramlRed.get(_paramlRed.size()-1).asReduction();	// get the <evaluate_case>
			}
			int paramRuleId = paramRed.getParent().getTableIndex();
			if (paramRuleId == _ruleId) {
				args.add(this.getContent_R(paramRed.get(_nameIx).asReduction(), ""));
			}
			else if (paramRuleId == RuleConstants.PROD_CALL_PARAM_OMITTED) {
				// This is going to be something not supported
				args.add("null");
			}
			_paramlRed = (paramHead.equals(_listHead)) ? _paramlRed.get(0).asReduction() : null;
		} while (_paramlRed != null);
		return args.reverse();
	}

	// START KGU#606 2018-10-30: Bugfix #635
	private final Matcher COMMA_SEMI_MATCHER = Pattern.compile("[,;]+").matcher("");
	// END KGU#606 2018-10-30

	/**
	 * Traverses the recursive rule to obtain a left-to-right list of expressions (e.g.
	 * as argument list for a function call)
	 * @param _exprlRed - the rule recursively comprising the expression
	 * @param _listHead - the rule head representing the recursive part
	 * @param _exclRuleId - id of a rule terminating the exploration
	 * @return list of expressions as strings
	 * @throws ParserCancelled
	 * @see #getParameterList(Reduction, String, int, int)
	 */
	private final StringList getExpressionList(Reduction _exprlRed, String _listHead, int _exclRuleId) throws ParserCancelled {
		StringList exprs = new StringList();
		do {
			String exprlHead = _exprlRed.getParent().getHead().toString();
			Reduction exprRed = _exprlRed;	// could be <call_param>
			if (exprlHead.equals(_listHead)) {
				exprRed = _exprlRed.get(_exprlRed.size()-1).asReduction();	// get the <evaluate_case>
				_exprlRed = _exprlRed.get(0).asReduction();
			}
			else {
				_exprlRed = null;
			}
			int exprRuleId = exprRed.getParent().getTableIndex();
			if (exprRuleId != _exclRuleId) {
				// START KGU#606 2018-10-29: Bugfix #635
				//exprs.add(this.getContent_R(exprRed, ""));
				String expr = this.getContent_R(exprRed, "").trim();
				if (_exclRuleId != RuleConstants.PROD_TARGET_X_COMMA_DELIM && _exclRuleId != RuleConstants.PROD_X_COMMA_DELIM
						|| !COMMA_SEMI_MATCHER.reset(expr).matches()) {
					exprs.add(expr);
				}
				// END KGU#606 2018-10-29
			}
		} while (_exprlRed != null);
		return exprs.reverse();
	}

	/**
	 * Derives an expression that makes some sense as Boolean condition from the given
	 * {@link Reduction} {@code _reduction}. Tries to handle incomplete expressions,
	 * condition names as variable attributes etc.
	 * @param _reduction - the top rule for the condition
	 * @param _lastSubject - a comparison subject in case of an incomplete expression
	 * (e.g. the discriminator in a CASE structure)
	 * @return the derived expression in Structorizer-compatible syntax
	 * @throws ParserCancelled
	 */
	private final String transformCondition(Reduction _reduction, String _lastSubject) throws ParserCancelled {
		// We must resolve expressions like "expr1 = expr2 or > expr3" or "expr1 = expr2 or expr3".
		// Unfortunately the <condition> node is not defined as hierarchical expression
		// tree dominated by operator nodes but as left-recursive "list".
		// We should transform the left-recursive <expr_tokens> list into a linear
		// list of <expr_token> we can analyse from left to right, such that we can
		// identify the first token as comparison operator. (It seems rather simpler
		// to inspect the prefix of the composed string.)
		String thruExpr = "";
		int ruleId = _reduction.getParent().getTableIndex();
		// If the condition consists of just a qualified name then this may be the result ...
		if (ruleId == RuleConstants.PROD_QUALIFIED_WORD2 && (_lastSubject == null || _lastSubject.isEmpty())) {
			String qualName = this.getContent_R(_reduction, "");
			CobVar var = currentProg.getCobVar(qualName);
			// in case of a condition name resolve the comparison
			if (var != null && var.isConditionName()) {
				qualName = var.getValuesAsExpression(true);
			}
			return qualName;
		}

		if (ruleId == RuleConstants.PROD_EVALUATE_OBJECT) {
			Reduction thruRed = _reduction.get(1).asReduction();
			if (thruRed.getParent().getTableIndex() == RuleConstants.PROD__EVALUATE_THRU_EXPR_THRU) {
				thruExpr = this.getContent_R(thruRed.get(1).asReduction(), " .. ");
			}
			_reduction = _reduction.get(0).asReduction();	// Should be <partial_expr> i.e. <expr_tokens>
		}
		LinkedList<Token> expr_tokens = new LinkedList<Token>();
		this.lineariseTokenList(expr_tokens, _reduction, "<expr_tokens>");
		String cond = "";
		// START KGU#402 2019-03-04: Issue #407: Approach to solve expressions like "a = 3 or 5"
		String lastRelOp = null;
		// END KGU#402 2019-03-04
		//String cond = this.getContent_R(_reduction, "").trim();
		// Test if cond starts with a comparison operator. In this case add lastSubject...
		//if (cond.startsWith("<") || cond.startsWith("=") || cond.startsWith(">")) {
		//	if (lastSubject != null) {
		//		cond = (lastSubject + " " + cond).trim();
		//	}
		//}
		ruleId = -1;
		if (!expr_tokens.isEmpty() && expr_tokens.getFirst().getType() == SymbolType.NON_TERMINAL) {
			ruleId = expr_tokens.getFirst().asReduction().getParent().getTableIndex();
		}
		if (_lastSubject == null || _lastSubject.isEmpty()) {
			Token tok = expr_tokens.getFirst();
			if (!isComparisonOpRuleId(ruleId)) {
				if (tok.getType() == SymbolType.CONTENT) {
					_lastSubject = tok.asString();
					if (tok.getName().equals("COBOLWord")) {
						_lastSubject = _lastSubject.replace("-", "_");
						// Try to identify a variable and if so, fetch its qualified name
						CobVar var = currentProg.getCobVar(_lastSubject);
						if (var != null) {
							if (var.isConditionName()) {
								_lastSubject = var.getParent().getQualifiedName();
							}
							else {
								_lastSubject = var.getQualifiedName();
							}
						}
						/* FIXME: if the current word matches an internal register,
						 * then check if it exists and create it otherwise.
						 * Note: depending on the register we should fill it, too
						 * (RETURN-CODE, NUMBER-OF-CALL-PARAMETERS, ...)
						 * else if (cT.matchesRegister(lastSubject)) {
						 * 		...
						 * }
						 */
						;
					}
				}
				else {
					_lastSubject = this.getContent_R(tok.asReduction(), "");
				}
			}
			else {
				_lastSubject = "";
			}
		}
		boolean afterLogOpr = true;
		for (Token tok: expr_tokens) {
			String tokStr = "";
			if (tok.getType() == SymbolType.NON_TERMINAL) {
				ruleId = tok.asReduction().getParent().getTableIndex();
				tokStr = this.getContent_R(tok.asReduction(), "");
				CobVar checkedVar = currentProg.getCobVar(tokStr);
				if (checkedVar != null && checkedVar.isConditionName()) {
					tokStr = checkedVar.getValuesAsExpression(true);
				}
				// START KGU#402 2019-03-04: Issue #407 - we may have to complete conds like "a = 4 or 7"
				else if (!_lastSubject.isEmpty() && ruleId == RuleConstants.PROD_EXPR_TOKEN2) {
					lastRelOp = tokStr.trim();
				}
			}
			else {
				tokStr = tok.asString();
				// FIXME also address qualified names (rule PROD_QUALIFIED_WORD2)
				if (tok.getName().equals("COBOLWord")) {
					tokStr = tokStr.replace("-", "_");
					CobVar checkedVar = currentProg.getCobVar(_lastSubject);
					if (checkedVar != null) {
						// First of all accomplish the name as fallback
						tokStr = checkedVar.getQualifiedName();
						// Now look for some configured comparison expression
						String condString = checkedVar.getValuesAsExpression(true);
						if (!condString.isEmpty()) {
							tokStr = condString;
						}
					}
				}
				// START KGU#402 2019-03-04: Issue #407 - FIXME this patch may be superfluous
				else if (!_lastSubject.isEmpty() && isComparisonOperator(tokStr)) {
					lastRelOp = tokStr.trim();
				}
				// END KGU#402 2019-03-04
			}
			if (!tokStr.trim().isEmpty()) {
				// START KGU#402 2019-03-04: Issue #407: Approach to solve expressions like "a = 3 or 5"
				//if (afterLogOpr && isComparisonOpRuleId(ruleId)) {
				//	// Place the last comparison subject to accomplish the next incomplete expression
				//	cond += " " + _lastSubject;
				//}
				if (afterLogOpr) {
					if (isComparisonOpRuleId(ruleId)) {
						// Place the last comparison subject to accomplish the next incomplete expression
						cond += " " + _lastSubject;
						lastRelOp = tokStr.trim();
					}
					else if (lastRelOp != null && isNonBooleanOperand(tokStr) && !_lastSubject.isEmpty()) {
						// What we do here is pretty vague. To be more exact, we would have to analyse the next token...
						cond += " " + _lastSubject + " " + lastRelOp;
					}
				}
				// END KGU#402 2019-03-04
				afterLogOpr = (ruleId == RuleConstants.PROD_EXPR_TOKEN_AND || ruleId == RuleConstants.PROD_EXPR_TOKEN_OR);
				if (afterLogOpr) {
					tokStr = tokStr.toLowerCase();
				}
				cond += " " + tokStr;
			}
		}
		// TODO We currently don't resolve the cond-name of "NOT cond-name"
		cond += thruExpr;
		if (cond.matches("(.*?\\W)" + BString.breakup("NOT") + "\\s*=(.*?)")) {
			cond = cond.replaceAll("(.*?\\W)" + BString.breakup("NOT") + "\\s*=(.*?)", "$1 <> $2");
		}
		// bad check, the comparision can include the *text* " OF "!
//		if (cond.contains(" OF ")) {
//			System.out.println("A record access slipped through badly...");
//		}
		return cond.trim();	// This is just an insufficient first default approach
	}

	// START KGU#402 2019-03-04: Issue #407 - More heuristics for abbreviated comparison like "a = 4 or 7"
	private boolean isNonBooleanOperand(String tokStr) {
		boolean mayBeBoolean = true;
		// First quick check for primitive literals (i.e. numbers, strings)
		if (STRING_MATCHER.reset(tokStr).matches() || NUMBER_MATCHER.reset(tokStr).matches()) {
			mayBeBoolean = false;
		}
		// Now check as variables
		else {
			CobVar checkedVar = currentProg.getCobVar(tokStr);
			if (checkedVar != null && !checkedVar.isConditionName()) {
				String type = CobTools.getTypeString(checkedVar, true);
				if (!type.isEmpty() && !type.equals(CobTools.UNKNOWN_TYPE)) {
					mayBeBoolean = false;
				}
			}
		}
		return !mayBeBoolean;
	}
	
	private boolean isComparisonOperator(String tokStr) {
		tokStr = tokStr.trim();
		// Apparently there is no operator symbol for unequality
		return tokStr.equals("=") || tokStr.equals("<") || tokStr.equals(">") || tokStr.equals("<=") || tokStr.equals(">=");
	}
	// END KGU#402 2019-03-04

	private final boolean isComparisonOpRuleId(int ruleId)
	{
		switch (ruleId) {
		case RuleConstants.PROD_EQ_TOK_EQUAL:
		case RuleConstants.PROD_EQ_EQUAL:
		case RuleConstants.PROD_GT_TOK_GREATER:
		case RuleConstants.PROD_GT_GREATER:
		case RuleConstants.PROD_LT_TOK_LESS:
		case RuleConstants.PROD_LT_LESS:
		case RuleConstants.PROD_GE_GREATER_OR_EQUAL:
		case RuleConstants.PROD_LE_LESS_OR_EQUAL:
		case RuleConstants.PROD_EXPR_TOKEN2:
		case RuleConstants.PROD_EXPR_TOKEN_IS:
		case RuleConstants.PROD_EXPR_TOKEN_IS2:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Converts the left-recursive token list representing the non-terminal {@code _listRuleHead} and
	 * held by {@link Reduction} {@code _reduction} into a {@link LinkedList}, appending its item {@link Token}s
	 * to {@code _tokens}.
	 * @param _tokens - the list the {@link Token}s are to be added to
	 * @param _reduction - the {@link Reduction} representing the recursive token list rule
	 * @param _listRuleHead - the name of the non-terminal (e.g. "{@code <expr_tokens>}")
	 */
	private final void lineariseTokenList(LinkedList<Token> _tokens, Reduction _reduction, String _listRuleHead)
	{
		if (_reduction.getParent().getHead().toString().equals(_listRuleHead) && _reduction.size() > 1) {
			_tokens.addFirst(_reduction.get(_reduction.size()-1));
			Reduction red0 = _reduction.get(0).asReduction();
			//int ruleId = red0.getParent().getTableIndex();
			String ruleHead = red0.getParent().getHead().toString();
//			switch (ruleId) {
//			// FIXME: Check subscripts (<subref>)!
//			// Ensure the processing of refmods!
//			case RuleConstants.PROD_IDENTIFIER_1:
//			case RuleConstants.PROD_IDENTIFIER_13:
//			// Don't split qualified identifiers!
//			case RuleConstants.PROD_QUALIFIED_WORD2:
//				_tokens.addFirst(_reduction.get(0));
//				break;
//			default:
//				lineariseTokenList(_tokens, _reduction.get(0).asReduction(), _listRuleHead);
//			}
			if (ruleHead.equals("<identifier_1>") || ruleHead.equals("<qualified_word>")) {
				_tokens.addFirst(_reduction.get(0));	// Why at first?
			}
			else {
				lineariseTokenList(_tokens, red0, _listRuleHead);
			}
		}
		else {
			for (int i = 0; i < _reduction.size(); i++) {
				_tokens.add(i, _reduction.get(i));
			}
		}
	}

	private final String negateCondition(String condStr) {
		return Element.negateCondition(condStr);
	}

	private String getOriginalText(Reduction _reduction, String _content)
	{
		for(int i=0; i<_reduction.size(); i++)
		{
			Token token = _reduction.get(i);
			switch (token.getType())
			{
			case NON_TERMINAL:
				{
					_content = this.getOriginalText(token.asReduction(), _content);
				}
				break;
			case CONTENT:
				{
					_content += " " + token.asString();
				}
				break;
			default:
				break;
			}
		}
		return _content;
	}

	// START KGU#467 2017-12-02: Bugfix #480
	/** Maximum number of characters for concatenation of nested multi-line initializer expressions */
	private static final int MAX_INITIALIZER_LINE_LENGTH = 80;
	// END KGU#467 2017-12-02
	// Patterns and Matchers needed for getContent_R()
	// (reusable, otherwise both get created and compiled over and over again)
	private static final Pattern pHexLiteral = Pattern.compile("^[Nn]?[Xx][\"']([0-9A-Fa-f]+)[\"']");
	private static final Pattern pIntLiteral = Pattern.compile("^0+([0-9]+)");
	private static final Pattern pAcuNumLiteral = Pattern.compile("^[BbOoXxHh]#([0-9A-Fa-f]+)");
	private static final Pattern pEscapedApostrophe = Pattern.compile("''");
	private static final Pattern pEscapedQuote = Pattern.compile("\"\"");
	private static final Pattern pQuote = Pattern.compile("\"");

	private static Matcher mHexLiteral = pHexLiteral.matcher("");
	private static Matcher mIntLiteral = pIntLiteral.matcher("");
	private static Matcher mAcuNumLiteral = pAcuNumLiteral.matcher("");
	private static Matcher mEscapedApostrophe = pEscapedApostrophe.matcher("");
	private static Matcher mEscapedQuote = pEscapedQuote.matcher("");
	private static Matcher mQuote = pQuote.matcher("");

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.parsers.CodeParser#getContent_R(com.creativewidgetworks.goldparser.engine.Reduction, java.lang.String)
	 */
	@Override
	protected String getContent_R(Reduction _reduction, String _content) throws ParserCancelled
	{
		return getContent_R(_reduction, _content, "");
	}

	/**
	 * Recursively converts the substructure of {@link Reduction} {@code _reduction} into a target code string
	 * and appens it to the given string {@code _content}.
	 * @param _reduction - the current {@link Reduction} object
	 * @param _content - previous content the string representation of {@code _reduction} is to be appended to.
	 * @param _separator - a separator string to be put among sub-token results
	 * @return the composed string
	 * @throws ParserCancelled
	 * @see #getContentToken_R(Token, String, String, boolean)
	 */
	protected String getContent_R(Reduction _reduction, String _content, String _separator) throws ParserCancelled
	{
		// START KGU#537 2018-07-01: Enh. #553
		checkCancelled();
		// END KGU#537 2018-07-01
		int ruleId = _reduction.getParent().getTableIndex();
		String ruleHead = _reduction.getParent().getHead().toString();
		if (ruleHead.equals("<function>") && ruleId != RuleConstants.PROD_FUNCTION) {
			String functionName = this.getContent_R(_reduction.get(0).asReduction(),"").toLowerCase().replace("-", "_");
			if (!functionName.equals("mod")) {
				if (functionName.equals("char")) {
					functionName = "chr";
				}
				String argList = "()";
				if (_reduction.size() > 2) {
					argList = getContent_R(_reduction.get(2).asReduction(), "(").trim() + ")";
				}
				else {
					// The second token is supposed to be <func_args> and already contains parentheses
					argList = getContent_R(_reduction.get(1).asReduction(), "").trim();
				}
				_content += (_content.matches(".*\\w") ? " " : "") + functionName + argList;
			}
			else {
				// We must convert mod(arg1, arg2) to (arg1 mod arg2)
				Reduction argRed = _reduction.get(1).asReduction().get(1).asReduction();
				if (argRed.size() != 3) {
					// Something is wrong here!
					_content += this.getContent_R(argRed, _content + _separator +"mod");
				}
				String arg1 = this.getContent_R(argRed.get(0).asReduction(), "").trim();
				String arg2 = this.getContent_R(argRed.get(2).asReduction(), "").trim();
				// If we knew the inner structure of the arguments then we could better decide whether
				// parentheses are really necessary... So we just use a rough heuristics
				if (arg1.contains(" ") || arg1.contains(",")) {
					arg1 = "(" + arg1 + ")";
				}
				if (arg2.contains(" ") || arg2.contains(",")) {
					arg2 = "(" + arg2 + ")";
				}
				_content += "(" + arg1 + " mod " + arg2 + ")";
			}
		}
		// START KGU#513 2018-04-04: Display clauses had been misinterpreted as expression lists.
		else if (ruleHead.equals("<display_clause>") || ruleHead.equals("<display_upon>")) {
			String displayClause = "";
			for (int i = 0; i < _reduction.size(); i++) {
				displayClause = this.getContentToken_R(_reduction.get(i), displayClause, "_", displayClause.isEmpty());
			}
			displayClause = displayClause.replace(" ", "_").replace("__","_");
			_content += displayClause;
		}
		// END KGU#513 2018-04-04
		else {
			boolean hasRefMod = false;
			int posSub = -1;
			switch (ruleId) {
			case RuleConstants.PROD_IDENTIFIER_1:	// <identifier_1> ::= <qualified_word> <subref> <refmod>
			case RuleConstants.PROD_TARGET_IDENTIFIER_1:	// <target_identifier_1> ::= <qualified_word> <subref> <refmod>
				hasRefMod = true;
			case RuleConstants.PROD_SUB_IDENTIFIER_12:	// <identifier_1> ::= <qualified_word> <subref>
			case RuleConstants.PROD_IDENTIFIER_12:	// <identifier_1> ::= <qualified_word> <subref>
			case RuleConstants.PROD_TARGET_IDENTIFIER_12:	// <identifier_1> ::= <qualified_word> <subref>
				posSub = 1;
			case RuleConstants.PROD_IDENTIFIER_13:	// <identifier_1> ::= <qualified_word> <refmod>
			case RuleConstants.PROD_TARGET_IDENTIFIER_13:	// <target_identifier_1> ::= <qualified_word> <refmod>
				if (!hasRefMod && posSub < 0) hasRefMod = true;
			{
				// This will already return a qualified name with paceholders for table indices
				String qualName = this.getContent_R(_reduction.get(0).asReduction(), "");
				if (posSub > 0) {
					String indexStr = this.getContent_R(_reduction.get(posSub).asReduction(), "");
					// For the case of a multidimensional table split the index iexpressions
					StringList ixExprs = Element.splitExpressionList(indexStr.substring(1), ",");
					if (this.functionNames.contains(qualName)) {
						// We take all as arguments ...
						qualName += indexStr;
						// ... and prevent from indexing
						ixExprs.clear();
					}
					// In case of indexing, the qualName might just be an undeclared alias, so we may
					// have to identify the real table name from the index variable, presumably the
					// last one
					else if (currentProg.getCobVar(qualName) == null) {
//					if (currentProg.getCobVar(qualName) == null) {
						for (int i = 0; i < ixExprs.count(); i++) {
							CobVar ixVar = currentProg.getCobVar(ixExprs.get(i));
							if (ixVar != null && ixVar.isIndex()) {
								qualName = ixVar.getTableName();
							}
						}
					}
					// Now place the index expressions into the respective place holders
					for (int i = 0; i < ixExprs.count(); i++) {
						String placeHolder = "%" + (i+1);
						if (qualName.contains(placeHolder)) {
							qualName = qualName.replace(placeHolder, ixExprs.get(i));
						}
						else {
							qualName += "[" + ixExprs.get(i) + "]";
						}
					}
				}
				// Okay, the index replacement done, we cater for possible referencing (might even be applied to a function result)
				if (hasRefMod) {
					// The <refmod> is always the last token
					Reduction refModRed = _reduction.get(_reduction.size() - 1).asReduction();
					String startStr = this.getContent_R(refModRed.get(1).asReduction(), "");
					String lengthStr = "1";
					if (refModRed.size() > 4) {
						lengthStr = this.getContent_R(refModRed.get(3).asReduction(), "");
					}
					_content += " copy(" + qualName + ", " + startStr +  ", " + lengthStr + ") ";
				}
				else {
					_content += _separator + qualName;
				}
				break;
			}
			case RuleConstants.PROD_EXP_FACTOR_EXPONENTIATION:
			{
				_content += " pow(" + this.getContent_R(_reduction.get(0).asReduction(), "")
				+ this.getContent_R(_reduction.get(2).asReduction(), ", ") + ")";
				break;
			}
			case RuleConstants.PROD_QUALIFIED_WORD2:
			{
				_content = this.getContent_R(_reduction.get(2).asReduction(), _content);
				_content = this.getContent_R(_reduction.get(0).asReduction(), _content + ".");
				// START KGU#388 2017-10-04: Enh. #423
				CobVar var = this.currentProg.getCobVar(_content);
				if (var != null) {
					if (var.isConditionName()) {
						// FIXME_: What if the name appears on the left side of an assignment? Can this happen?
						_content = var.getValuesAsExpression(true);
					}
					_content = var.getQualifiedName();
				}
				// END KGU#388 2017-10-04
				break;
			}
			case RuleConstants.PROD__EVALUATE_THRU_EXPR:
			// FIXME: likely relevant for more items that are optional -> emtpy
			{
				// Empty THRU expression --> don't change _content
				break;
			}
			default:
			{
				for(int i=0; i<_reduction.size(); i++)
				{
					Token token = _reduction.get(i);
					_content = getContentToken_R(token, _content, _separator, i == 0);
				}
			}
			} // switch(ruleId)
		}
		return _content;
	}

	/**
	 * Subroutine of {@link #getContent_R(Reduction, String, String)} for the conversion of sub-tokens,
	 * which are not necessarily non-terminals.
	 * @param _token - the current token
	 * @param _content - previous content the string representation of this token is to be appended to.
	 * @param _separator - a string to be put between the result for sub-tokens
	 * @param _isFirst - whether this token is the first in a sequence (i.e. if a separator isn't needed before)
	 * @return the string composed from {@code _content} and this {@link Token}.
	 * @throws ParserCancelled
	 */
	private String getContentToken_R(Token _token, String _content, String _separator, boolean _isFirst) throws ParserCancelled {
		switch (_token.getType())
		{
		case NON_TERMINAL:
		{
			Reduction subRed = _token.asReduction();
			int subRuleId = subRed.getParent().getTableIndex();
			String subHead = subRed.getParent().getHead().toString();
			if (subHead.equals("<COMMON_FUNCTION>")) {
				_content += getContent_R(subRed, _content).toLowerCase().replace("-",  "_");
			}
			else if (subHead.equals("<eq>")) {
				_content += " = ";
			}
			else if (subHead.equals("<lt>")) {
				_content += " < ";
			}
			else if (subHead.equals("<gt>")) {
				_content += " > ";
			}
			else if (subHead.equals("<le>")) {
				_content += " <= ";
			}
			else if (subHead.equals("<ge>")) {
				_content += " >= ";
			}
			else if (subRuleId == RuleConstants.PROD_CONDITION_OP_NOT_EQUAL) {
				_content += " <> ";
			}
			else if (subHead.equals("<not>")) {
				_content += " not ";
			}
			else if (subRuleId == RuleConstants.PROD_EXPR_TOKEN_AND) {
				_content += " and ";
			}
			else if (subRuleId == RuleConstants.PROD_EXPR_TOKEN_OR) {
				_content += " or ";
			}
			else if (subRuleId == RuleConstants.PROD_EXPR_TOKEN_IS_ZERO) {
				if (subRed.get(1).asReduction().getParent().getTableIndex() == RuleConstants.PROD__NOT2) {
					_content += " <> 0";
				} else {
					_content += " = 0";
				}
			}
			else if (subRuleId == RuleConstants.PROD_EXPR_TOKEN_IS) {
				// FIXME Now we get into trouble. We cannot replace the "IS <CLASS>" construct by a built-in
				// function call (which would be ideal for Executor) because the left operand would have to
				// be passed as argument, too, but unfortunately we are absolutely not sure about the left
				// operand, since expressions (namely conditions) may just be a recursive list of tokens rather
				// than a hierarchical tree and the left operand is already gone... We could only inspect
				// _content (regarding parentheses, operator precedence etc.) but even this is vague - it might
				// be incomplete.
				// So we replace it by the Java instanceof operator, which comes nearest but will hardly identify
				// COBOL classes and doesn't work for primitive types of course.
				_content += " instanceof (" + this.getContent_R(subRed.get(1).asReduction(), "") + ") ";
			}
			else if (subRuleId == RuleConstants.PROD_EXPR_TOKEN_IS2) {
				if (subRed.get(2).asReduction().getParent().getTableIndex() == RuleConstants.PROD_CONDITION_OR_CLASS) {
					// FIXME: Now here we get into even deeper trouble: The negation has to be placed around the
					// entire expression but again: the left operand cannot be identified (not part of this reduction,
					// might even be a composed expression).
					// We would have to analyse _content though we cannot even be sure that it's complete...
					// So we place the not operator at this awkward position and leave the correction to the user.
					_content += " not instanceof (" + this.getContent_R(subRed.get(2).asReduction(), "") + ") ";
				} else {
					_content += " <> " + this.getContent_R(subRed.get(2).asReduction(), "");
				}
			}
			// FIXME (KGU#397): In certain cases, the <subref> token is also used for parameter lists in routine calls!
			// (This can only be solved on the parent rule level - we don't have the context here)
			else if (subRuleId == RuleConstants.PROD_SUBREF_TOK_OPEN_PAREN_TOK_CLOSE_PAREN) {
				_content += "[" + this.getContent_R(subRed.get(1).asReduction(), "") + "] ";	// FIXME: spaces!?
			}
			else {
				String sepa = "";
				String toAdd = getContent_R(_token.asReduction(), "", _separator);
				// START KGU#513 2018-04-04: Avoids end-standing separators with empty rules at recursion end
				//if (!_isFirst && !_separator.isEmpty()) {
				//	sepa = _separator;
				//}
				//else if (!toAdd.isEmpty() && _content.matches(".*\\w") && !(toAdd.startsWith("(") || toAdd.startsWith(" "))) {
				//	sepa = " ";
				//}
				//_content += sepa + toAdd;
				if (!toAdd.trim().isEmpty()) {
					if (!_isFirst && !_separator.isEmpty()) {
						sepa = _separator;
					}
					else if (_content.matches(".*\\w") && !(toAdd.startsWith("(") || toAdd.startsWith(" "))) {
						sepa = " ";
					}
					_content += sepa + toAdd;
				}
				// END KGU#513 2018-04-04
			}
		}
		break;
		case CONTENT:
		{
			String toAdd = _token.asString();
			String name = _token.getName();
			if (toAdd.toLowerCase().matches(".*true.*") || toAdd.toLowerCase().matches(".*false.*") || name.toLowerCase().matches(".*true.*") || name.toLowerCase().matches(".*false.*")) {
				getLogger().log(Level.CONFIG, "CONTENT: {0} / {1}", new Object[]{name, toAdd});
			}
			final String trueName = name;
			// just drop the "zero terminated" and "Unicode" parts here
			if (name.equals("ZLiteral") || name.equals("NationalLiteral")) {
				toAdd = toAdd.substring(1);
				name = "StringLiteral";
			}
			//
			if (name.equals("COBOLWord")) {
				toAdd = toAdd.replace("-", "_");
				// START KGU#388 2017-10-04: Enh. #423
				CobVar var = this.currentProg.getCobVar(toAdd);
				if (var != null) {
					toAdd = var.getQualifiedName();
				}
				// END KGU#388 2017-10-04
			}
			else if (name.equals("StringLiteral")) {
				// convert from 'COBOL " Literal' --> "COBOL "" Literal";
				// changing COBOL escape to java escape "COBOL "" Literal" -> "COBOL \"\" Literal"
				String strCont = toAdd.substring(1, toAdd.length() - 1);
				if (strCont.length() != 0) {
					if (toAdd.charAt(0) == '\'') {
						mEscapedApostrophe.reset(strCont);
						strCont = mEscapedApostrophe.replaceAll("'");
						mQuote.reset(strCont);
						strCont = mQuote.replaceAll("\\\\\"");
					} else {
						mEscapedQuote.reset(strCont);
						strCont = mEscapedQuote.replaceAll("\\\\\"");
					}
				}
				if (!trueName.equals("ZLiteral")) { // handle zero terminated strings
					toAdd = "\"" + strCont + "\"";
				} else { // handle zero terminated strings
					toAdd = "\"" + strCont + "\\\\\\\\000\"";
				}
				//toAdd += " ";
			}
			else if (name.equals("HexLiteral")) { // this one defines a STRING literal in (non-unicode) Hex notation
				mHexLiteral.reset(toAdd);
				String hexText = mHexLiteral.replaceAll("$1");
				// MicroFocus COBOL allows an odd number - strange people...
				if (hexText.length() % 2 == 1) {
					hexText = "0" + hexText;
				}
				toAdd = "\"";
				for (int j = 0; j < hexText.length() - 1; j += 2) {
					toAdd += "\\u00" + hexText.substring(j, j+2);
				}
				toAdd += "\"";
			}
			else if (name.equals("NationalHexLiteral")) { // this one defines a STRING literal in (unicode) Hex notation
				mHexLiteral.reset(toAdd);
				String hexText = mHexLiteral.replaceAll("$1");
				toAdd = "\"";
				for (int j = 0; j < hexText.length() - 3; j += 4) {
					toAdd += "\\u" + hexText.substring(j, j+4);
				}
				toAdd += "\"";
			}
			// Make sure IntLiteral [+-]?{Number}+ isn't falsely recognized as octal
			else if (name.equals("IntLiteral")) {
				mIntLiteral.reset(toAdd);
				toAdd = mIntLiteral.replaceAll("$1");
			}
			// NOTE: if we do convert Decimals to BigDecimal some day the following is needed
			//       we may set this as primitive type and add "/ 100" for 2 decimal places
			else if (name.equals("DecimalLiteral")) {
				//toAdd = toAdd + "b";
			}
			// Make sure FloatLiteral [+-]?{Number}+ '.' {Number}+ 'E' [+-]?{Number}+ is recognized as float
			else if (name.equals("FloatLiteral")) {
				toAdd = toAdd + "f";
			}
			else if (name.equals("AcuBinNumLiteral")) { // this one defines an INTEGER literal in binary notation
				mAcuNumLiteral.reset(toAdd);
				toAdd = mAcuNumLiteral.replaceAll("0b$1");
			}
			else if (name.equals("AcuOctNumLiteral")) { // this one defines an INTEGER literal in Octal notation
				mAcuNumLiteral.reset(toAdd);
				toAdd = mAcuNumLiteral.replaceAll("0$1");
			}
			else if (name.equals("AcuHexNumLiteral")) { // this one defines an INTEGER literal in Hex notation
				mAcuNumLiteral.reset(toAdd);
				toAdd = mAcuNumLiteral.replaceAll("0x$1");
			}
			else if (name.equals("TOK_AMPER")) {
				//toAdd = " + ";
				toAdd = "+";
			}
			else if (name.equals("TOK_PLUS") || name.equals("TOK_MINUS")) {
				//toAdd = " " + toAdd + " ";
			}
			else if (name.equals("ZERO") || toAdd.matches("0+")) { // note: ZEROS and ZEROES replaced by grammar
				toAdd = "0";
			}
			else if (name.equals("SPACE")) { // note: SPACES replaced by grammar
				toAdd = "\' \'";
			}
			else if (name.equals("TOK_NULL")) {
				toAdd = "\'\\0\'";
			}
			else if (name.equals("TOK_TRUE") || name.equals("TOK_FALSE")) {
				// FIXME This branch is never entered - how can we get hold of TRUE and FALSE?
				toAdd = toAdd.substring(4).toLowerCase();
			}
//			else {
//				System.out.println("getContentToken_R: inudentified token: " + name + " / " + toAdd);
//			}
			// Keywords FUNCTION and IS are to be suppressed
			if (!name.equalsIgnoreCase("FUNCTION") && !name.equalsIgnoreCase("IS")) {
				String sepa = "";
				if (!_isFirst && !(_content + _separator).endsWith(" ") && !toAdd.startsWith(" ")) {
					sepa = " ";
				}
				_content += (_isFirst ? "" : _separator + sepa) + toAdd;
			}
		}
		break;
		default:
			break;
		}
		return _content;
	}

	/**
	 * Drastically simplified method to retrieve a name expected as (optional) content of the given {@link Token}
	 * {@code _token}.
	 * @param _token
	 * @return
	 */
	private String getWord(Token _token)
	{
		String word = "";
		if (_token.getType() == SymbolType.CONTENT) {
			if (_token.getName().equals("COBOLWord")) {
				word = _token.asString().replace('-', '_');
			}
			else if (_token.getName().equals("FILLER")) {
				word = _token.getName();
			}
		}
		else {
			Reduction red = _token.asReduction();
			if (red.size() > 0) {
				return getWord(red.get(0));
			}
		}
		return word;
	}

	//------------------------- Data Conversion -------------------------

	// START KGU#388 2017-10-03: Enh.#423
	/**
	 * Generates the necessary tye definitions, constant definitions and variable declarations (initialization
	 * inclusive if available) for the variables link with {@code varRoot}, which is supposed to be the first
	 * top-level variable of a {@link CobProg} context.
	 * @param varRoot - the root of the varaible tree
	 * @param localNode - the insertion node for internal definitions (supposedly in {@link COBOLParser#root})
	 */
	private void buildDataSection(CobVar varRoot, Subqueue localNode)
	{
		// First gather all necessary record type definitions recursively.
		// The typenames will be generic i.e. derived from the respective variable
		// name and a hashcode.
		// Simultaneously compose the declarations and initialisations
		StringList declarations = new StringList();
		boolean containsExternals = false;
		boolean containsGlobals = false;
		CobVar currentVar = varRoot;
		Root globalRoot = new Root();
		globalRoot.setText(currentProg.getName() + "_Globals");
		globalRoot.setInclude();
		Subqueue globalNode = globalRoot.children;
		Subqueue externalNode = externalRoot.children;
		while (currentVar != null) {
			String varName = currentVar.forceName();
			boolean isExternal = currentVar.isExternal();
			boolean isGlobal = currentVar.isGlobal();
			if (isExternal && this.declaredExternals.contains(varName)) {
				containsExternals = true;
				// Don't add a declaration in case of a reference to a former external declaration
				// (but declarations list must contain an empty entry for the final CobVar loop))
				declarations.add("");
			}
			else {
				String typeName = insertTypedefs(currentVar,
						externalRoot.children, (globalRoot != null ? globalRoot.children : null),
						localNode, 0);
				String declaration = currentVar.forceName();
				if (typeName != null && (this.optionImportVarDecl || currentVar.hasChild(true))) {
					if (!typeName.isEmpty()) {
						typeName = ": " + typeName;
					}
					declaration = (currentVar.isConstant(true) ? "const " : "var ") + declaration + typeName;
				}
				String init = makeInitialization(currentVar, 0);
				if (init != null && !init.isEmpty()) {
					declaration += " <- " + init;
					// If it's a constant then we better add it already in case following types
					// might depend on it.
					if (currentVar.isConstant(true)) {
						if (isExternal) {
							addDeclToDiagram(externalNode, currentVar, declaration, true);
						}
						else if (isGlobal) {
							addDeclToDiagram(globalNode, currentVar, declaration, true);
						}
						else {
							addDeclToDiagram(localNode, currentVar, declaration, true);
						}
						declaration = "";
					}
				}
				// START KGU#471 2017-12-05: Bugfix #483 - suppress unshared non-complex mere declarations
				else if (!currentVar.hasChild(true) && !this.optionImportVarDecl && !isGlobal && !isExternal) {
					declaration = "";
				}
				// END KGU#471 2017-12-05
				// We postpone all other declarations
				declarations.add(declaration);
			}
			if (isGlobal) {
				containsGlobals = true;
			}
			currentVar = currentVar.getSister();
		}
		// Now it's safe to add all the composed declarations as elements
		currentVar = varRoot;
		int i = 0;
		while (currentVar != null) {
			String declaration = declarations.get(i++);
			// If declaration is empty then the element has already been created
			if (!declaration.isEmpty()) {
				String varName = currentVar.getName();
				boolean isConstant = currentVar.isConstant(false);
				if (currentVar.isExternal()) {
					addDeclToDiagram(externalNode, currentVar, declaration, isConstant);
					declaredExternals.add(varName);
				}
				else if (currentVar.isGlobal()) {
					addDeclToDiagram(globalNode, currentVar, declaration, isConstant);
					declaredGlobals.get(globalRoot).add(varName);
				}
				else {
					addDeclToDiagram(localNode, currentVar, declaration, isConstant);
				}
			}
			currentVar = currentVar.getSister();
		}
		if (localNode.parent instanceof Root) {
			this.dataSectionEnds.put((Root)localNode.parent, localNode.getSize());
		}
		if (containsExternals) {
			root.addToIncludeList(externalRoot);
		}
		// Add the name of globalRoot to the include list of this Root if there are references
		if (containsGlobals) {
			// If it is a new global definition and there is no global definition context then create
			// a new diagram for these declarations and map the current cobProg to it
			globalMap.put(currentProg, globalRoot);
			root.addToIncludeList(globalRoot);
		}
		else {
			// Prepares the recursive inclusion of super-globals
			globalRoot = root;
		}
		// Now check whether the current prog is a subprogram of another one defining global data ...
		CobProg ancestor = currentProg.getParent();
		while (ancestor != null) {
			Root includable;
			if ((includable = globalMap.get(ancestor)) != null) {
				globalRoot.addToIncludeList(includable);
				// All done
				break;
			}
			ancestor = ancestor.getParent();
		}
	}

	/**
	 * Adds an instruction with text {@code text} declaring variable {@code currentVar} to
	 * the {@link Subqueue} {@code targetNode}.<br/>
	 * Note that option {@link #optionImportVarDecl} has no effect here as this method
	 * doesn't know whether the element is to be shared.
	 * @param externalNode - the element sequence to append to
	 * @param currentVar - the variable object to create a declaration for
	 * @param text - the prepared instruction text
	 * @param isConst - whether it is a constant definition
	 */
	private void addDeclToDiagram(Subqueue targetNode, CobVar currentVar, String text, boolean isConst) {
		Instruction decl = new Instruction(text);
		String comment = currentVar.getComment();
		if (comment != null) {
			decl.setComment(comment);
		}
		CobVar redefined = currentVar.getRedefines();
		if (redefined != null) {
			decl.getComment().add("*** Redefines " + redefined.getQualifiedName());
		}
		if (isConst) {
			decl.setColor(colorConst);
		}
		else if (!decl.isAssignment()) {
			decl.setColor(colorDecl);
		}
		else if (currentVar.isGlobal() || currentVar.isExternal()) {
			decl.setColor(colorGlobal);
		}
		targetNode.addElement(decl);
	}

	/**
	 * Generates and immediately inserts the necessary type definitions for the given variable entry {@code var}.
	 * If the variable is declared globally then the type definitions will also be appended to {@code globalNode},
	 * if the variable is declared externally then the type definitions are appended to {@code externalNode}, and
	 * otherwise they are appended to {@code localNode}. If some of the these {@link Subqueue}s are null then the
	 * respective elements won't be created. If some of them are identical, this shouldn't be a problem, of course.
	 * If {@code var} is of a primitive type then no type definition element will be created. If {@code var} represents
	 * a COBOL table (i.e. an array) then the defined type represents the element type, not the entire array; the
	 * returned typename will contain the index range though (in Java notation, for the sake of simplicity and shortness).
	 * @param var - the variable enry recursively to be modelled with record type definitions if structured.
	 * @param externalNode - the target {@link Subqueue} for external definitions
	 * @param globalNode - the target {@link Subqueue} for global definitions
	 * @param localNode - the target {@link Subqueue} for normal (local) definitions
	 * @param declLevel - the current hierarchy level (not the COBOL "level"!)
	 * @return the typename associated with {@code var}, non matter whether a type definition was created or not.
	 */
	private static String insertTypedefs(CobVar var, Subqueue externalNode, Subqueue globalNode, Subqueue localNode, int declLevel)
	{
		// Do a depth-first search (traverse in postorder)
		CobVar child = var.getChild();
		String typeName = CobTools.getTypeString(var, true);
		if (child != null && !child.isConditionName()) {
			// FIXME: For global types we must not redefine this within a routine but refer to the globally defined name!!!
			typeName = var.deriveTypeName();
			StringBuilder typedef = new StringBuilder("type " + typeName + " = record");
			String sepa = "{\\\n";
			do {
				String subType = insertTypedefs(child, externalNode, globalNode, localNode, declLevel+1);
				typedef.append(sepa + child.forceName()+ ": " + subType);
				sepa = ";\\\n";
				child = child.getSister();
			} while (child != null);
			typedef.append("}");
			Instruction instr = new Instruction(typedef.toString());
			if (var.getComment() != null) {
				instr.setComment(var.getComment());
			}
			CobVar redefined = var.getRedefines();
			if (redefined != null) {
				instr.getComment().add("*** Redefines type of var " + redefined.getQualifiedName() + " (type " + redefined.deriveTypeName() + "?)");
			}
			// FIXME: For global types we must not repeat this within a routine but refer to the globally defined name!!!
			if (var.isExternal()) {
				instr.setColor(colorGlobal);
				externalNode.addElement(instr);
			}
			if (var.isGlobal()) {
				instr.setColor(colorGlobal);
				globalNode.addElement(instr);
			}
			else {
				localNode.addElement(instr);
			}
			// START KGU#674 2019-03-04: Bugfix #695 - this seemed to be misplaced - put out of the branch
			//if (var.isArray()) {
			//	typeName += "[" + var.getOccursString() + "]";
			//}
			// END KGU#674 2019-03-04
		}
		// START KGU#674 2019-03-04: Bugfix #695 Arrays of basic types weren't reflected
		if (var.isArray()) {
			typeName += "[" + var.getOccursString() + "]";
		}
		// END KGU#674 2019-03-04
		return typeName;
	}

	/**
	 * Creates an initialization expression (the right side of the assignment or the value entry
	 * of a record initializer) for the given {@link CobVar} variable entry {@code var}
	 * if the variable had been initialized in the code.
	 * @param var - the variable entry
	 * @param declLevel - the current hierarchy level
	 * @return - the composed initialization string
	 */
	private static String makeInitialization(CobVar var, int declLevel)
	{
		// Do a depth-first search (traverse in postorder)
		CobVar child = var.getChild();
		String initialization = var.getValueFirst();
		// FIXME What do we do with arrays here? Is this okay?
		if (var.isArray() && initialization != null) {
			initialization = "{" + var.getValueList(", ", null) + "}";
		}
		if (child != null && !child.isConditionName()) {
			String typeName = var.forceName() + "_" + (declLevel == 0 ? "type" : "t" + Integer.toHexString(var.hashCode()));
			typeName = Character.toUpperCase(typeName.charAt(0)) + typeName.substring(1);
			StringBuilder init = new StringBuilder(typeName);
			String sepa = "{\\\n";
			do {
				String value = makeInitialization(child, declLevel+1);
				if (value != null) {
					init.append(sepa + child.forceName() + ": " + value);
					sepa = ",\\\n";
				}
				child = child.getSister();
			} while (child != null);
			init.append("}");
			// Anything added at all? (In this case the initial separator would have changed)
			if (!sepa.equals("{\\\n")) {
				initialization = init.toString();
				// START KGU#467 2017-12-01: Bugfix #480 - in case of an array of sub-records multiply the initialization
				int arraySize = var.getArraySize();
				// FIXME: Should we restrict the array size for this approach (but how to communicate it then?)
				if (arraySize > 0) {
					if (initialization.length() < MAX_INITIALIZER_LINE_LENGTH) {
						initialization = initialization.replace("\\\n", " ");
					}
					StringBuilder arrayInit = new StringBuilder("{\\\n");
					sepa = "";
					for (int i = 0; i < arraySize; i++) {
						arrayInit.append(sepa + initialization);
						sepa = ",\\\n";
					}
					arrayInit.append("\\\n}");
					initialization = arrayInit.toString();
				}
				// END KGU#467 2017-12-01
			}
		}
		return initialization;
	}
	// END KGU#388 2017-10-03

	//------------------------- Postprocessor ---------------------------

	// TODO Use this subclassable hook if some postprocessing for the generated roots is necessary
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.parsers.CodeParser#subclassUpdateRoot(lu.fisch.structorizer.elements.Root, java.lang.String)
	 */
	@Override
	protected void subclassUpdateRoot(Root aRoot, String textToParse) throws ParserCancelled {
		// THIS CODE EXAMPLE IS FROM THE CPARSER (derives a name for the main program)
		if (aRoot.getMethodName().equals("???")) {
			if (aRoot.getParameterNames().count() == 0) {	// How could there be arguments?
				String fileName = new File(textToParse).getName();
				if (fileName.contains(".")) {
					fileName = fileName.substring(0, fileName.lastIndexOf('.'));
				}
				fileName = fileName.replaceAll("[^A-Za-z0-9_]", "_");
				if (this.optionUpperCaseProgName) {
					fileName = fileName.toUpperCase();
				}
				aRoot.setText(fileName);
				// FIXME: Might also become an includable diagram!
				aRoot.setProgram(true);
			}
			// If there is a non-empty diagram with external definitions then refer to it
			// In case of a parsing error we may get here without build initialization!
			// FIXME Should alrady have been done.
//			if (externalRoot != null && aRoot != externalRoot && externalRoot.children.getSize() > 0) {
//				aRoot.addToIncludeList(externalRoot);
//			}
		}
		// Force returning of the specified result
		if (this.returnMap.containsKey(aRoot)) {
			String resultVar = this.returnMap.get(aRoot);
			int nElements = aRoot.children.getSize();
			if (!aRoot.getMethodName().equals(resultVar) && !resultVar.equalsIgnoreCase("RESULT")) {
				// Revise all return elements (make sure there isn't any without value)
				aRoot.traverse(new ReturnEnforcer(resultVar));
				// Now make sure that the routine ends with a return element
				if (nElements == 0 || !(aRoot.children.getElement(nElements-1) instanceof Jump)) {
					aRoot.children.addElement(new Instruction(getKeywordOrDefault("preReturn", "return") + " " + this.returnMap.get(aRoot)));
				}
			}
		}
	}

	// START KGU 2017-05-28: Now we try to resolve internal calls
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.parsers.CodeParser#subclassPostProcess(java.lang.String)
	 */
	protected void subclassPostProcess(String _textToParse) throws ParserCancelled
	{
		// The automatic conversion of Instructions to Calls may have invalidated element references.
		// Hence update the procedureList before elements are going to be moved.
		finishProcedureList();

		// Now the actual extraction of local procedures may begin.
		for (SectionOrParagraph sop: this.procedureList) {
			LinkedList<Call> clients = this.internalCalls.get(sop.name.toLowerCase());
			Element firstElement = sop.getFirstElement();
			Element lastElement = sop.getLastElement();
			if (firstElement != null && lastElement != null) {
				Subqueue sq = sop.parent;
				// We will have to copy the content of the replacing call. Therefore we must
				// have an opportunity to find the call. This should be feasible via the index
				// of the first element of the subsequence. But we cannot be sure that sop.start
				// is still correct - the original context may already have been outsourced itself
				// so we search for it in the current context.
				int startIndex = sop.startsAt;
				int nextIndex = sop.endsBefore;
				if (startIndex < 0) {
					this.log("Corrupt diagram: Parts of section or paragraph \"" + sop.name + "\" not detected!", true);
					continue;
				}
				// START KGU#464 2017-12-04: Bugfix #475 - Check if there are EXIT PARAGRAPH or EXIT SECTION Jumps
				//if (clients != null) {
				if (clients != null || sop.isSection && !sop.sectionExits.isEmpty() || !sop.isSection && !sop.paragraphExits.isEmpty()) {
				// END KGU#464 2017-12-04
					// No longer bothering to detect arguments and results, we may simply move the elements
					//Root proc = owner.outsourceToSubroutine(elements, sop.name, null);
					String callText = sop.name + "()";

					// This the decisive step (bugfix #475: now already refactors indices)
					Call replacingCall = extractSectionOrParagraph(sop, callText);

					int callIndex = sq.getIndexOf(replacingCall);	// index of replacingCall may have changed by data outsourcing
					if (callIndex != sop.startsAt) {
						getLogger().log(Level.INFO, "*** Refactoring of {0} failed!", sop);
					}
					// Now cleanup and get rid of place-holding dummy elements
					Element doomedEl = null;
					if (callIndex > 0 && (doomedEl = sq.getElement(callIndex-1)) instanceof Call) {
						// Get rid of the dummy Call now
						if (doomedEl.disabled && doomedEl.getText().getLongString().equalsIgnoreCase(sop.name)) {
							replacingCall.setComment(doomedEl.getComment());
							//System.out.println("=== Cleanup Call: " + sq.getElement(callIndex-1));
							sq.removeElement(callIndex-1);
							// START KGU#464 2017-12-03: Bugfix #475
							sop.startsAt--;
							sop.endsBefore--;
							// END KGU#464 2017-12-03
						}
					}
					// START KGU#464 2017-12-03: Bugfix #475 - mark inappropriate multi-level returns
					if (!sop.isSection) {
						for (Jump jp: sop.sectionExits) {
							jp.setColor(Color.RED);
							jp.getComment().add("UNSUPPORTED: Cannot exit the calling subroutine!");
						}
					}
					// END KGU#464 2017-12-03
					// Both the original proc text (now overwritten) and the replacingCall text contain
					// no arguments anymore, so we don't need to check whether we got all declarations
					for (Call client: clients) {
						// We may have to care for an includable Root that defines all necessary variables
						client.setText(callText);
						client.setColor(colorMisc);	// No longer needs to be red
						client.disabled = false;
					}
					// At the original place we most likely won't need the Call anymore (not reachable).
					if (!sq.isReachable(sop.startsAt, false)) {
						sq.removeElement(replacingCall);
						// START KGU#464 2017-12-03: Bugfix #475
						sop.endsBefore--;
						// END KGU#464 2017-12-03
					}
				}
				// Not explicitly used anywhere and just consisting of a disabled dummy jump?
				else if (sop.getSize() == 1 && firstElement instanceof Jump) {
					Element dummyCall = null;
					Jump dummyJump = (Jump)firstElement;
					// Cleanup if the content is just a dummy jump and it is preceded by a real jump and a dummy call
					// both being un-reachable; then we will drop the two dummy elements now
					if (startIndex > 0 && dummyJump.disabled && dummyJump.getText().getLongString().startsWith("(") && (dummyCall = sq.getElement(startIndex-1)) instanceof Call) {
						if (dummyCall.getText().getLongString().equalsIgnoreCase(sop.name)) {
							if (!sq.isReachable(startIndex-1, false)) {
								//System.out.println("=== Cleanup Jump: " + sq.getElement(ix));
								sq.removeElement(startIndex);	// This is the dummyJump itself
								//System.out.println("=== Cleanup Call: " + sq.getElement(ix-1));
								sq.removeElement(startIndex-1);	// This is the preceding dummyCall
								// START KGU#464 2017-12-03: Bugfix #475
								sop.startsAt--;
								sop.endsBefore -= 2;
								// END KGU#464 2017-12-03
							}
						}
					}
				}
				// START KGU#464 2017-12-03: Bugfix #475
				else if (!sop.isSection) {
					SectionOrParagraph contSoP = sop.containedBy;
					while (contSoP != null && !contSoP.isSection) {
						contSoP = contSoP.containedBy;
					}
					if (contSoP != null && contSoP.endsBefore < 0) {
						for (Jump jp: sop.sectionExits) {
							contSoP.sectionExits.add(jp);
						}
					}
				}
				refactorProcedureList(sop, startIndex, nextIndex);
				// END KGU#464 2017-12-03
			}
			// START KGU#478 2017-12-10: Issue #475 - S. Sobisch wanted calls to empty or corrupt sections be disabled
			else if (clients != null) {
				for (Call client: clients) {
					client.getComment().add("The called " + (sop.isSection ? "section" : "paragraph") + " seems to be empty, corrupt, or vanished.");
					client.disabled = true;
				}
			}
			// END KGU#478 2017-12-10
		}
		// START KGU#376 2017-10-04: Enh. #389
		if (externalRoot != null && externalRoot.children.getSize() > 0) {
			this.addRoot(externalRoot);
		}
		// END KGU#376 2017-10-04
		// START KGU#376 2017-10-19: Enh. #389
		if (!declaredGlobals.isEmpty()) {
			this.addAllRoots(declaredGlobals.keySet());
		}
		// END KGU#376 2017-10-19
	}
	// END KGU 2017-05-28

	/**
	 * Extracts the COBOL section or paragraph comprised by {@code sop} from its {@link Subqueue}
	 * to a new subdiagram, which is going to be registered in {@link CodeParser#subRoots}.
	 * Usually this is accompanied by the extraction of all potentially shared data declarations (type
	 * and constant definitions, variable declarations and initialisations) from the owning {@link Root}
	 * to a new includable diagram, which will be registered in {@link #dataSectionIncludes} (if all that hadn't
	 * already been done in relation with another extraction from the owning {@link Root}).
	 * @param sop - the {@link SectionOrParagraph} to be outsourced.
	 * @param callText - the text to be placed in the substituting Call.
	 * @return the {@link Call} element replacing the section or paragraph in {@code owner}.
	 */
	private Call extractSectionOrParagraph(SectionOrParagraph sop, String callText) {
		Root owner = Element.getRoot(sop.parent);
		Root proc = new Root();
		proc.setText(callText);
		proc.setProgram(false);
		int nElements = sop.getSize();
		//System.out.println("==== Extracting " + sop.name + " ===...");
		for (int i = 0; i < nElements; i++) {
			proc.children.addElement(sop.parent.getElement(sop.startsAt));
			//System.out.println("\t" + (callIndex + i) + " " + elements.getElement(0));
			sop.parent.removeElement(sop.startsAt);
			sop.endsBefore--;
		}
		// START KGU#464 2017-12-04): Bugfix #475 This became obsolete now
		// Now we convert all EXIT SECTION or EXIT PARAGRAPH elements into return Jumps.
		//proc.traverse(new JumpConverter());
		// END KGU#464 2017-12-04

		Call replacingCall = new Call(callText);
		sop.parent.insertElementAt(replacingCall, sop.startsAt);
		sop.endsBefore++;
		// Has the owner Root still shareable data at its beginning? Then outsource them...
		extractShareableData(owner);
		// If the owner has a mapped includable let the new proc include it as well
		if (dataSectionIncludes.containsKey(owner)) {
			proc.addToIncludeList(dataSectionIncludes.get(owner));
		}

		// Put the new subroutine diagram to the set of results as well
		this.addRoot(proc);

		return replacingCall;
	}

	/**
	 * Extracts the possibly still contained data section from {@link Root} {@code owner} and
	 * places the definitions, initializations etc. in a new includable diagram. Does nothing
	 * otherwise.
	 * Modifies {@link CodeParser#subRoots}, {@link #dataSectionIncludes}, and {@link #dataSectionEnds}
	 * in case elements are outsourced.
	 * @param owner - the diagram to ripped
	 */
	private void extractShareableData(Root owner) {
		Integer endDataIx = dataSectionEnds.get(owner);
		if (endDataIx != null && endDataIx <= owner.children.getSize()) {
			// Move all data declarations to a new shared includable
			//System.out.println("=== Removing " + endDataIx + " lines of shared data...");
			String dataName = owner.getMethodName() + "_Shared";
			Root shared = new Root();
			shared.setText(dataName);
			shared.setInclude();
			for (int i = 0; i < endDataIx; i++) {
				Element el = owner.children.getElement(0);
				owner.children.removeElement(0);
				shared.children.addElement(el);
			}
			this.addRoot(shared);			// Put the new diagram to the set of results
			dataSectionEnds.remove(owner);	// Un-register the root from those holding their own data declarations
			dataSectionIncludes.put(owner, dataName);	// register the mapped includable instead
			owner.addToIncludeList(dataName);	// ... and let the former owner include it
			// START KGU#464 2017-12-03: Bugfix #475 - adapt all section or paragraph references affected from this extraction
			for (SectionOrParagraph sop: this.procedureList) {
				if (sop.parent == owner.children && sop.startsAt >= endDataIx) {
					if (sop.endsBefore >= sop.startsAt) {
						sop.endsBefore -= endDataIx;
					}
					sop.startsAt -= endDataIx;
				}
			}
			// END KGU#464 2017-12-03
		}
	}

	// START KGU#464 2017-12-03: Bugfix #475 - we have to face nested paragraphs with require reference modifications
	/**
	 * Checks all {@link SectioOrParagraph} entries in {@code this.}{@link #procedureList} for open
	 * ends and sets the size of the respective parent {@link Subqueue} as end index in these cases.
	 * This is due before the extraction of sections and paragraphs to subdiagrams.
	 * (Submethod of {@link #subclassPostProcess(String)})
	 */
	private void finishProcedureList() {
		for (SectionOrParagraph sop: this.procedureList) {
			// START KGU#452 2017-10-30: Bugfix #445 - There may be empty sections or paragraphs
			if (sop.getFirstElement() == null) {
				// We can't do anything here
				continue;
			}
			// END KGU#452 2017-10-30
			// START KGU#464 2017-12-03: Bugfix #475 - close open SoPs (usually not closed at EOF)
			else if (sop.endsBefore < 0) {
				sop.endsBefore = sop.parent.getSize();
			}
//			int i = 0;
//			for (Element el: new Element[]{sop.firstElement, sop.lastElement}) {
//				int ix = (i == 0 ? sop.startsAt : sop.endsBefore - 1);
//				Subqueue sq = null;
//				Element newEl = null;
//				// Was the element an Instruction and has it vanished?
//				if (el instanceof Instruction && (sq = (Subqueue)el.parent).getIndexOf(el) < 0) {
//					if (ix < sq.getSize() && (newEl = sq.getElement(ix)) instanceof Call &&
//							newEl.getText().getText().equals(el.getText().getText())) {
//						if (i == 0) {
//							sop.firstElement = newEl;
//						}
//						else {
//							sop.lastElement = newEl;
//						}
//					}
//					else {
//						System.err.println("Instructions of Section/Paragraph " + sop.name + " got out of sight!");
//					}
//				}
//				i++;
//			}
			// END KGU#464 2017-12-03
		}
	}

	/**
	 * Checks and updates element index ranges in all {@link SectionOrParagraph} entries directly or
	 * indirectly including the given {@code _sop} along the containdBy links in
	 * {@code this.}{@link #procedureList}. This is due whenever a modification (subroutine extraction or
	 * elimination of dummy calls or jumps) was done.
	 * Ensures that the index references will match again.
	 * @param _sop - the {@link SectionOrParagraph} object just manipulated
	 * @param _formerStartIndex - its former start index
	 * @param _formerNextIndex - its former end index
	 */
	private void refactorProcedureList(SectionOrParagraph _sop, int _formerStartIndex, int _formerNextIndex)
	{
		int startChange = _sop.startsAt - _formerStartIndex;	// usually <= 0
		int sizeChange =  _sop.getSize() - (_formerNextIndex - _formerStartIndex);	// usually <= 0
		if (startChange == 0 && sizeChange == 0) {
			// Nothing to do
			return;
		}
		SectionOrParagraph nextSop = _sop.containedBy;
		while (nextSop != null) {
			// It will always hold that nextSop.startsAt >= _sop.startsAt (otherwise they wouldn't have been
			// linked).
			// If the size has changed and the linked (containing) sector or paragraph had ended at or after
			// the old end of _sop then index endsBefore will have to be adapted accordingly
			if (sizeChange != 0 && nextSop.endsBefore >= 0 && nextSop.endsBefore >= _formerNextIndex) {
				nextSop.endsBefore += sizeChange;
			}
			// If both _sop and nextSop shared the start index and _sop.startsAt has decreased
			// then nextSop will also be moved the same distance.
			if (startChange < 0) {
				if (nextSop.startsAt == _formerStartIndex) {
					nextSop.startsAt += startChange;
				}
				if (nextSop.endsBefore >= 0) {
					nextSop.endsBefore += startChange;
				}
			}
			nextSop = nextSop.containedBy;
		}
	}
	// END KGU#464 2017-12-03

}


class CobTools {

	// START KGU 2018-03-21
	public static final Logger logger = Logger.getLogger(CobTools.class.getName());
	// END KGU 2018-03-21

	/** COBOL field types */
	public static enum Usage {
		USAGE_BINARY,
		USAGE_BIT,
		USAGE_COMP_5,
		USAGE_COMP_X,
		USAGE_DISPLAY,
		USAGE_FLOAT,
		USAGE_DOUBLE,
		USAGE_INDEX,
		USAGE_NATIONAL,
		USAGE_OBJECT,
		USAGE_PACKED,
		USAGE_POINTER,
		USAGE_LENGTH,
		USAGE_PROGRAM_POINTER,
		USAGE_UNSIGNED_CHAR,
		USAGE_SIGNED_CHAR,
		USAGE_UNSIGNED_SHORT,
		USAGE_SIGNED_SHORT,
		USAGE_UNSIGNED_INT,
		USAGE_SIGNED_INT,
		USAGE_UNSIGNED_LONG,
		USAGE_SIGNED_LONG,
		USAGE_COMP_6,
		USAGE_FP_DEC64,
		USAGE_FP_DEC128,
		USAGE_FP_BIN32,
		USAGE_FP_BIN64,
		USAGE_FP_BIN128,
		USAGE_LONG_DOUBLE,
		/** calculated picture: usage DISPLAY with only numeric values */
		USAGE_DISPLAY_NUMERIC,
		/** no explicit usage given, handle as USAGE_DISPLAY */
		USAGE_NOT_SET
	};

	/** COBOL storages */
	public static enum Storage {
		/** WORKING-STORAGE section (permanent storage) */
		STORAGE_WORKING,
		/** LOCAL-STORAGE section (temporary storage per instance) */
		STORAGE_LOCAL,
		/** LINKAGE section (arguments passed by caller)*/
		STORAGE_LINKAGE,
		/** SCREEN section (user interaction) */
		STORAGE_SCREEN,
		/** REPORT section */
		STORAGE_REPORT,
		/** No storage set */
		STORAGE_UNKNOWN,
		/** FILE section */
		STORAGE_FILE
	}

	// START KGU#465 2017-12-04: Bugfix #473
	/** A dummy string specifying an un-recognized type (neither pic nor usage) */
	protected static final String UNKNOWN_TYPE = "-unknown-type-";
	// END KGU#465 2017-12-04

	/**
	 * Current node in the CobProg tree
	 */
	private CobProg currentProgram = null;
//	private CobProg lastProgram = null;

	/**
	 * Resource node in the tree of callable entities of a COBOL source,
	 * holds different storage areas with their respective variable trees.
	 * Is linked to the parent node, the next sibling and the first child.
	 */
	class CobProg {

		private String name = null;
		private String extName = null;
		private boolean isFunction = false;

		private CobProg parent = null;
		private CobProg child = null;
		private CobProg sister = null;

		/** internal map for faster lookup of a variable by its name */
		private HashMap<String,ArrayList<CobVar>> varNames = null;

		/* first variable in different sections for possible IMPORT nsd generation */
		private CobVar workingStorage = null;
		private CobVar localStorage = null;
		private CobVar linkageStorage = null;
		private CobVar screenStorage = null;
		private CobVar reportStorage = null;

		private Storage currentStorage = Storage.STORAGE_UNKNOWN;

		/**
		 * Maps fle descriptor variable names to file names
		 */
		public HashMap<String, String> fileMap = new HashMap<String, String>();
		/**
		 * Maps file record names (from file section) to the owning file descriptor name
		 */
		public HashMap<String, String> fileRecordMap = new HashMap<String, String>();
		/**
		 * Maps the file status variables to the respective file descriptor
		 */
		public HashMap<String, String> fileStatusMap = new HashMap<String, String>();

		/** Set the new storage for the program and resets lastVar
		 * @param currentStorage the currentStorage to set
		 */
		public void setCurrentStorage(Storage currentStorage) {
			this.currentStorage = currentStorage;
			lastVar = null;
		}

		public void insertVar(CobVar variable) {

			/* store first entry in current storage enabling iterations later */
			switch (currentStorage) {
			case STORAGE_LINKAGE:
				if (linkageStorage == null) {
					linkageStorage = variable;
				}
				break;
			case STORAGE_LOCAL:
				if (localStorage == null) {
					localStorage = variable;
				}
				break;
			case STORAGE_REPORT:
				if (reportStorage == null) {
					reportStorage = variable;
				}
				break;
			case STORAGE_SCREEN:
				if (screenStorage == null) {
					screenStorage = variable;
				}
				break;
			case STORAGE_UNKNOWN:
				// assume WORKING-STORAGE
				currentStorage = Storage.STORAGE_WORKING;
				// fall-through
			case STORAGE_WORKING:
				if (workingStorage == null) {
					workingStorage = variable;
				}
				break;
			case STORAGE_FILE:
				// TODO add handling - per file
				break;
			}

			String varName = variable.getName();

			// check if we already have a List of variables with the given name
			ArrayList<CobVar> varList = null;
			if (varNames != null) {
				varList = varNames.get(varName);
			} else {
				varNames = new HashMap<String,ArrayList<CobVar>>();
			}

			// Otherwise create the List entry and add it to the name Map
			if (varList == null) {
				varList = new ArrayList<CobVar>();
				varNames.put(varName, varList);
			}

			// finally insert the variable to the new/found List
			varList.add(variable);
		}

		/**
		 * @param name
		 * @param extName
		 * @param isFunction
		 * @param parent
		 */
		public CobProg(String name, String extName, boolean isFunction, CobProg parent) {
			this.name = name;
			this.extName = extName;
			this.isFunction = isFunction;
			this.parent = parent;
		}

		/** check if the program has any content (currently only variables) */
		public boolean isEmtpy() {
			if (varNames != null || currentStorage != Storage.STORAGE_UNKNOWN) {
				return false;
			}
			return true;
		}

		/**
		 * @return the child
		 */
		public CobProg getChild() {
			return child;
		}
		/**
		 * @param child the child to set
		 */
		public void setChild(CobProg child) {
			this.child = child;
		}
		/**
		 * @return the sister
		 */
		public CobProg getSister() {
			return sister;
		}
		/**
		 * @param sister the sister to set
		 */
		public void setSister(CobProg sister) {
			this.sister = sister;
		}
		/**
		 * @return the workingStorage
		 */
		public CobVar getWorkingStorage() {
			return workingStorage;
		}
		/**
		 * @return the localStorage
		 */
		public CobVar getLocalStorage() {
			return localStorage;
		}
		/**
		 * @return the linkageStorage
		 */
		public CobVar getLinkageStorage() {
			return linkageStorage;
		}
		/**
		 * @return the screenStorage
		 */
		public CobVar getScreenStorage() {
			return screenStorage;
		}
		/**
		 * @return the reportStorage
		 */
		public CobVar getReportStorage() {
			return reportStorage;
		}
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
		/**
		 * @return the extName
		 */
		public String getExtName() {
			return extName;
		}
		/**
		 * @return true if this represents a function, false otherwies
		 */
		public boolean isFunction() {
			return isFunction;
		}
		/**
		 * @return the parent
		 */
		public CobProg getParent() {
			return parent;
		}

		public CobVar getCobVar(String nameOfVar) {

			if (varNames == null || nameOfVar == null || nameOfVar.isEmpty()) {
				return null;
			}

			nameOfVar = nameOfVar.toLowerCase();

			// get unqualified name (1st part) and possible qualifiers
			String names[];
			if (nameOfVar.contains(".")) {
				// Apparently it is already converted but the path might be incomplete
				StringList parts = StringList.explode(nameOfVar, "\\.");
				names = parts.reverse().toArray();
				// Wipe off possible index place holders
				for (int i = 0; i < names.length; i++) {
					String part = names[i];
					int posBrack = part.indexOf("[");
					if (posBrack >= 0) {
						names[i] = part.substring(0, posBrack);
					}
				}
			}
			else {
				names = nameOfVar.split("\\s+(in|of)\\s+");
			}

			// search for List of variables with the given (unqualified) name
			ArrayList<CobVar> varList = varNames.get(names[0]);

			// if the entry exists check for neccessary qualification
			if (varList == null) {
				return null;
			}
			if (varList.size() == 1 && names.length == 1) {
				return varList.get(0);
			}

			for (Iterator<CobVar> iterator = varList.iterator(); iterator.hasNext();) {
				CobVar candidate = iterator.next();

//				if (candidate.hasParent()) {
//					if (candidate.getParent().getName().equals(names[0])) {
//						// TODO add code for qualified search
//					}
//					boolean matches = true;
//					CobVar parent = candidate.getChild();
//					for (int i = 1; matches && i < names.length; i++) {
//						matches = parent.getName().equals(names[i]);
//					}
//					if (matches) {
//						return candidate;
//					}
//				}
				// Well, obviously first the next name in the list must be matched by any ancestor
				// If that is okay, then the remaining names must match higher ancestors in order
				boolean candidateVerified = true;
				CobVar parent = candidate.getParent();
				for (int i = 1; i < names.length; i++) {
					while (parent != null) {
						if (parent.getName() != null && parent.getName().equals(names[i])) {
							break;
						}
						parent = parent.getParent();
					}
					if (parent == null) {
						candidateVerified = false;
						break;
					}
				}
				if (candidateVerified) {
					return candidate;
				}
			}

			return varList.get(0);	// FIXME: Shouldn't this rather be null?
		}

	}

	private CobVar lastVar;
	//private static CobVar lastRealVar;
	private int fillerCount = 0;


	class CobVar {

		/** level of COBOL field */
		private int	level;
		/** COBOL field name */
		private String name;
		/** Original picture of COBOL field */
		private String picture;
		/** usage of COBOL field */
		private Usage usage;
		/** COBOL field is signed */
		private boolean hasSign;
		/** COBOL field is Decimal (TODO: replace with decimal position) */
		private boolean hasDecimal;

		/** length of COBOL field, mostly relevant for STRING and record-handling */
		private int charLength;

		/**
		 * @return the charLength
		 */
		public int getCharLength() {
			return charLength;
		}

		private CobVar parent;
		private CobVar child;
		private CobVar sister;
		private CobVar redefines;
		private CobVar redefinedBy;	// FIXME: In theory, there might be several redefinitions...

		/** Array of COBOL values */
		private String[] values;
		/** converted ArrayList as Java Expression - done on first request */
		private String valuesAsExpression;
		/** Literal Value for "SET var TO TRUE */
		// TODO: Create "const" item with name this.name+"$"+"TRUE" for assignments
		private String valueFirst;
		/** Literal Value for "SET var TO false */
		// TODO: Create "const" item with name this.name+"$"+"FALSE" for assignments
		private String valueFalse;
		/** variable is GLOBAL --> should be considered to put in a single NSD
		 * and include in all NSDs that are using it */
		private boolean isGlobal;
		/** variable is EXTERNAL --> should be considered to put in a single NSD */
		private boolean isExternal;
		/** variable is edited --> must be handled on MOVE (editing/de-editing) */
		private boolean isEdited;
		/** variable is filler */
		private boolean isFiller;
		/** variable has any alphanumeric (1) or numeric (2) length */
		private int anyLength;
		/** the source comment associated with this variable */
		private String comment = null;
		/** the (maximum) number of elements in case of a table (array), where 0 means a non-table */
		private int occurs = 0;
		/** list of the associated index variable(s) in case of an IDEXED BY clause */
		private ArrayList<CobVar> indexedBy = null;
		/** the originating expression or constant name for the {@link occurs} value */
		private String occursString = null;
		/** Memorizes whether this was declared as constant */
		private boolean constant = false;

		/**
		 * In case of a condition name returns a Boolean expression suited for comparison
		 * with unqualified component names.
		 * @return the comparison expression
		 * @see #getValuesAsExpression(boolean)
		 */
		public String getValuesAsExpression() {
			return getValuesAsExpression(false);
		}

		/** @return true if {@code ancestor} is a superstructure (an ancestor) of this */
		public boolean isComponentOf(CobVar ancestor) {
			CobVar parent = this.parent;
			while (parent != null) {
				if (parent == ancestor) {
					return true;
				}
				parent = parent.parent;
			}
			return false;
		}

		/** @return true if this variable was introduced by an INDEXED BY ... clause */
		public boolean isIndex() {
			return this.level == 100;
		}
		/** @return the qualified name of the table this variable is declared as index for, or null */
		public String getTableName() {
			if (!this.isIndex()) {
				return null;
			}
			return this.parent.getQualifiedName();
		}

		/**
		 * In case of a condition name returns a Boolean expression suited for comparison
		 * @param fullyQualified TODO
		 * @return the valuesAsExpression
		 */
		public String getValuesAsExpression(boolean fullyQualified) {
			if (this.valuesAsExpression == null) {
				if (this.level == 88) {
					// CHECKME: generate kind of SWITCH statement?
					String varName = this.parent.name;
					if (fullyQualified) {
						varName = this.parent.getQualifiedName();
					}
					StringBuilder exprSB = new StringBuilder(this.values.length * (varName.length() + 10));
					for (int i = 0; i < values.length; i++) {
						String value = values[i];
						if (value.equals("THRU")) {
							i++;
							value = values[i];
							exprSB.append(" .. " + value);
						} else {
							if (i == 0) {
								//exprSB.append(varName + " == " + value);
								exprSB.append(varName + " = " + value);
							} else {
								//exprSB.append(" || \\\n" + varName + " == " + value);
								exprSB.append(" or \\\n" + varName + " = " + value);
							}
						}
					}
					this.valuesAsExpression = exprSB.toString();
				} else {
					// FIXME shouldn't this work like getValueList() here?
					this.valuesAsExpression = "";
				}
			}
			return this.valuesAsExpression;
		}

		public boolean isNumeric() {

			switch (this.usage) {
			case USAGE_FLOAT:
			case USAGE_FP_BIN32:
			case USAGE_DOUBLE:
			case USAGE_FP_BIN64:
			case USAGE_FP_BIN128:
			case USAGE_FP_DEC64:
			case USAGE_FP_DEC128:
			case USAGE_INDEX:
			case USAGE_LENGTH:
			case USAGE_SIGNED_CHAR:
			case USAGE_UNSIGNED_CHAR:
			case USAGE_PACKED:
			case USAGE_SIGNED_INT:
			case USAGE_UNSIGNED_INT:
			case USAGE_SIGNED_LONG:
			case USAGE_UNSIGNED_LONG:
			case USAGE_LONG_DOUBLE:
			case USAGE_COMP_X:
			case USAGE_COMP_5:
			case USAGE_COMP_6:
			case USAGE_SIGNED_SHORT:
			case USAGE_UNSIGNED_SHORT:
			case USAGE_BINARY:
			case USAGE_DISPLAY_NUMERIC:
				return true;
			//case USAGE_BIT: // CHECKME
			default:
				return false;
			}
		}

		public boolean hasChild() {
			return hasChild(false);
		}

		public boolean hasChild(boolean ignoreConditionNames) {
			if (this.child != null) {
				return ignoreConditionNames || child.level != 88;
			} else {
				return false;
			}
		}

		/**
		 * @return the isGlobal
		 */
		public boolean isGlobal() {
			return isGlobal;
		}

		/**
		 * @return the isExternal
		 */
		public boolean isExternal() {
			return isExternal;
		}

		/**
		 * @return the isEdited
		 */
		public boolean isEdited() {
			return isEdited;
		}

		/**
		 * @return the isFiller
		 */
		public boolean isFiller() {
			return isFiller;
		}

		/**
		 * @return the text that is to be used for SET var TO TRUE and for constant values
		 */
		public String getValueFirst() {
			return valueFirst;
		}

		/**
		 * @return the text that is to be used for SET var TO FALSE
		 */
		public String getValueFalse() {
			return valueFalse;
		}

		/**
		 * If there are any stored values at all (check with {@link #getValueFirst()}!) then composes
		 * as string containing value literals or expressions for the array elements. Otherwise returns
		 * null.
		 * @param separator - the separator string to be put between two value strings
		 * @param defaultString - a string that is to be placed for unset element values. If null then
		 * missing values at the end (less value stored than elements declared) will be omitted, missing
		 * value inbetween will produce an empty item.		 *
		 * @return a String composed of the value strings separated by {@code searator} or null!
		 * @see #isArray()
		 * @see #getValueFirst()
		 * @see #getValuesAsExpression()
		 */
		public String getValueList(String separator, String defaultString) {
			StringBuilder valueList = new StringBuilder(10 * occurs);
			int nVals = occurs;
			if (values == null) {
				return null;
			}
			if (defaultString == null && values.length < occurs) {
				nVals = values.length;
			}
			// For null valus in the value array...
			if (defaultString == null) {
				defaultString = "";
			}
			for (int i = 0; i < nVals; i++) {
				if (i > 0) {
					valueList.append(separator);
				}
				if (i >= values.length || values[i] == null) {

					valueList.append(defaultString);
				}
				else {
					valueList.append(values[i]);
				}
			}
			return valueList.toString();
		}

		/**
		 * Detects, based on the specific declaration levels and - if {@code checkValue} is
		 * true - the existence of a value, whether this variable is meant to be a constant.
		 * @param checkValue - if the existence of a value is checked as prerequisite
		 * @return true if this was created with level 01 or 78 and hence represents a constant
		 */
		public boolean isConstant(boolean checkValue) {
			return (this.constant) && (!checkValue || this.values != null);
		}

		/**
		 * @return whether this "component" merely represents a condition phrase (could be regarded as a Boolean method)
		 */
		public boolean isConditionName() {
			return this.level == 88;
		}

		/**
		 * Attach the associated comment to this variable
		 * @param comment - the comment found in the source code near the declaration
		 * @see #getComment()
		 */
		public void setComment(String comment) {
			this.comment = comment;
		}
		/**
		 * @return the comment attached in the source code to this variable
		 * @see #setComment(String)
		 */
		public String getComment() {
			return this.comment;
		}

		/** @return a corresponding type name for this variable respecting */
		public String deriveTypeName() {
			// At top-level a simple "_type" name suffix will be sufficient but at lower levels
			// we cannot rule out name clashes with substructure of other record types.
			String typeName = this.name + "_" + (this.parent == null ? "type" : "t" + Integer.toHexString(this.hashCode()));
			typeName = Character.toUpperCase(typeName.charAt(0)) + typeName.substring(1);
			return typeName;
		}

		/**
		 * @return true if this variable/component was declared with an occurs clause
		 * @see #getArraySize()
		 * @see #getOccursString()
		 * @see #getIndexedBy()
		 */
		public boolean isArray() {
			return (this.occurs > 0) || (this.occursString != null);
		}

		/**
		 * @return element number if this variable/component is an array and its value could be identified (otherwise 0)
		 * @see #isArray()
		 * @see #getOccursString()
		 */
		public int getArraySize() {
			return this.occurs;
		}
		/**
		 * @return a string describing the number of elements as found in the source code.
		 * This may be an integer literal or a transformed constant expression.
		 * @see #isArray()
		 * @see #getArraySize()
		 */
		public String getOccursString() {
			return this.occursString;
		}
		/**
		 * @return the list of possibly associated index variables if this is a table (array), or null.
		 * @see #getIndexedBy(int)
		 */
		public ArrayList<CobVar> getIndexedBy() {
			return this.indexedBy;
		}
		/**
		 * @return the n-th one of the possibly associated index variables if this is a table (array), or null.
		 * @see #getIndexedBy()
		 */
		public CobVar getIndexedBy(int n) {
			CobVar ixdVar = null;
			if (this.indexedBy != null && n >= 0 && n < this.indexedBy.size()) {
				ixdVar = this.indexedBy.get(n);
			}
			return ixdVar;
		}
		/**
		 * Associates the given index variable {@code indexVar} with this variable occording to
		 * an OCCURS ... INDEXED BY clause.
		 * @param indexVarNames
		 * @param currentProg
		 */
		public void setIndexedBy(StringList indexVarNames, CobProg currentProg) {
			this.indexedBy = new ArrayList<CobVar>(indexVarNames.count());
			for (int i = 0; i < indexVarNames.count(); i++) {
				CobVar ixdVar = new CobVar(indexVarNames.get(i), this);
				this.indexedBy.add(ixdVar);
				currentProg.insertVar(ixdVar);
			}
		}

		@Override
		public String toString()
		{
			return this.getClass().getSimpleName() + "(" + this.getQualifiedName() + ")";
		}

		/**
		 * General constructor<br/>
		 * Use {@link #setIndexedBy(CobVar)} afterwards to if in case of a table (array) an index variable was specified
		 * @param level COBOL level number
		 * @param name
		 * @param picture
		 * @param usage
		 * @param redefines
		 * @param isGlobal
		 * @param isExternal
		 * @param anyLength
		 * @param times - the maximum number of occurrences of this component (array elements)
		 * @param timesString - the (transformed) expression from which the {@code times} value was computed
		 */
		public CobVar(int level, String name, String picture, Usage usage, String value, CobVar redefines, boolean isGlobal, boolean isExternal, int anyLength, int times, String timesString) {
			super();

			// FIXME: check for level 66 before calling constructor
			if (level != 1 && level != 77 && lastVar == null) {
				// partial code import, generate implicit filler
				lastVar = new CobVar (1, null, null, null, null, null, false, false, 0, times, timesString);
			}

			this.level = level;
			// START KGU#467 2017-12-01: Bugfix #480 - level 77 data had been handled in a wrong way
			// Handle level 77 data as if they had level 1 (we avoid further consistency checks here)
			if (level == 77) {
				level = 1;
			}
			// END KGU#467 2017-12-01
			if (name != null && !name.isEmpty()) {
				this.name = name.trim().toLowerCase();
			} else {
				this.name = "filler";
			}
			if (this.name.equals("filler")) {
				CobTools.this.fillerCount++;
				// START KGU#388 2017-10-04: Enh. #423 - we need a valid identifier
				// this.name += "_$" + fillerCount;
				this.name += "_" + String.format("%1$02d", CobTools.this.fillerCount);
				// END KGU#388_2017-10-04
			}
			if (picture != null && !picture.isEmpty()) {
				this.picture = picture.trim();
				CobTools.this.setVarAttributesFromPic(this, picture);
			} else {
				this.picture = "";
			}

			/* set relation to other fields */
			this.parent = null;
			if (CobTools.this.lastVar != null && CobTools.this.lastVar.level < level) {
				this.parent = CobTools.this.lastVar;
				if (CobTools.this.lastVar.child == null) {
					CobTools.this.lastVar.child = this;
				}
			} else {
				for (CobVar v = CobTools.this.lastVar; v != null; v = v.parent) {
					// START KGU#467 2017-12-01: Bugfix #480 - level 77 data had been handled in a wrong way
					//if (level == v.level || level == 1 && v.level == 78) {
					if (level == v.level || level == 1 && (v.level == 77 || v.level == 78)) {
					// END KGU#467 2017-12-01
						this.parent = v.parent;
						v.sister = this;
						break;
					} else if (v.level < level) {
						this.parent = v;
						this.insertFiller(v, this);
						break;
					}
				}
			}
			this.child = null;
			this.sister = null;

			// usage explicitly given overrides usage calculated from PICTURE
			if (usage != null) {
				this.usage = usage;
			} else {
				// group item without usage
				if (picture == null || picture.isEmpty()) {
					if (this.parent != null) {
						this.usage = this.parent.usage;
					} else if (this.usage == null) {
						this.usage = Usage.USAGE_NOT_SET;
					}
				// normal item, take the usage from parent, if set, otherwise from picture
				} else if (this.parent != null && this.parent.usage != Usage.USAGE_NOT_SET) {
					this.usage = this.parent.usage;
				// no usage set by picture --> explicit set to standard value
				} else if (this.usage == null) {
					if (this.usage == null) {
						this.usage = Usage.USAGE_DISPLAY;
					}
				}
			}

			if (value != null) {
				this.values = new String[] { value };
				this.valueFirst = value;
			}

			this.redefines = redefines;
			if (redefines != null) {
				redefines.redefinedBy = this;
			}
			this.isGlobal = isGlobal;
			this.isExternal = isExternal;
			this.anyLength = anyLength;
			this.occurs = times;
			this.occursString = timesString;
			// lastRealVar = this;
			CobTools.this.lastVar = this;
		}

		/**
		 * Special constructor for creating condition-names (level 88 variables)
		 *
		 * @param name
		 * @param values
		 * @param valueFalse
		 */
		public CobVar(String name, String[] values, String valueFalse) {
			super();

			this.level = 88;

			if (name != null && !name.isEmpty()) {
				this.name = name.trim().toLowerCase();
			} else {
				this.name = "BAD-CONDITION";
			}

			if (values != null) {
				ArrayList<String> sortList = new ArrayList<>(Arrays.asList(values));
				Collections.reverse(sortList);
				this.values = sortList.toArray(new String[sortList.size()]);
				this.valueFirst = this.values[0];
			}
			this.valueFalse = valueFalse;

			if (CobTools.this.lastVar == null) {
				// create "correct" picture first
				String picString = CobTools.this.createPicStringFromValues(values);
				// partial code import, generate implicit filler
				CobTools.this.lastVar = new CobVar(1, null, picString, null, null, null, false, false, 0, 0, null);
			}

			this.picture = null;

			/* set relation to other fields */
			if (CobTools.this.lastVar.level == 88) {
				this.parent = CobTools.this.lastVar.parent;
				CobTools.this.lastVar.sister = this;
			} else {
				this.parent = CobTools.this.lastVar;
				if (CobTools.this.lastVar.child == null) {
					CobTools.this.lastVar.child = this;
				}
			}
			this.child = null;
			this.sister = null;
			this.constant  = true;

			/* set usage from parent */
			this.usage = this.parent.usage;

			CobTools.this.lastVar = this;
		}

		/**
		 * Special constructor for creating constants (level 01 CONSTANT as / level 78 variables)
		 * @param level - the COBOL level (either 01 or 78)
		 * @param constName - name (of the constant)
		 * @param value - the string representation of the value
		 * @param isGlobal - whether the constant is global
		 */
		public CobVar(int level, String constName, String value, boolean isGlobal) {
			super();

			this.level = level;

			if (constName != null && !constName.isEmpty()) {
				this.name = constName.trim().toLowerCase();
			} else {
				this.name = "BAD-CONST";
			}

			if (value != null) {
				this.values = new String[] { value };
				this.valueFirst = value;
			}
			this.valueFalse = null;

			// create assumed picture first
			this.picture = CobTools.this.createPicStringFromValues(this.values);

			if (CobTools.this.lastVar == null) {
				CobTools.this.lastVar = this;
			} else {
			/* set relation to other fields */
				if (CobTools.this.lastVar.level == level) {
					this.parent = CobTools.this.lastVar.parent;
					CobTools.this.lastVar.sister = this;
				} else {
					/* set relation to other fields */
					for (CobVar v = CobTools.this.lastVar; v != null; v = v.parent) {
						if (v.level == 01 || v.level == 78) {
							this.parent = v.parent;
							v.sister = this;
							break;
						}
					}
				}
			}
			this.child = null;
			this.sister = null;

			/* set usage from picture */
			CobTools.this.setVarAttributesFromPic(this, this.picture);

			this.isGlobal = isGlobal;
			this.constant = true;

			CobTools.this.lastVar = this;
		}

		/**
		 * Special constructor for index variables (OCCURS ... INDEXED BY clauses).<br/>
		 * This constructor does definitely not modify {@link lastVar}!
		 * @param name
		 * @param table - the table (array) this index is declared for
		 */
		public CobVar(String name, CobVar table) {
			super();

			this.level = 100;	// This is an artificial invented level in order to distinguish them easier

			if (name != null && !name.isEmpty()) {
				this.name = name.trim().toLowerCase();
			} else {
				this.name = "BAD-INDEX";
			}

			this.picture = null;
			this.values = null;
			this.usage = Usage.USAGE_INDEX;

			/* set relation to other fields */
			this.parent = table;	// it is linked to the array variable but not reversely via child/sister

			this.child = null;
			this.sister = null;
			this.constant  = true;

			this.isGlobal = table.isGlobal;
			this.isExternal = table.isExternal;

			this.isEdited = false;
			this.isFiller = false;
			this.redefines = null;

			// This comment might get used in a SET statement (there won't be a declaration for this...)
			this.comment = "Index variable for " + table.getQualifiedName();

			// This constructor will definitely not modify lastVar!
		}

		/** create an implicit FILLER into parentVar and adjust childVar to be its sister */
		private void insertFiller(CobVar parentVar, CobVar childVar) {

			/* sample code (compiling with "relaxed" syntax from different vendors)
			  01 var1.
			     <- FILLER INSERTED HERE with level 02 ->
			        03 var2
			           04 var3 pic x.
			     02 var4 pic x.
			 */

			lastVar = parentVar;
			CobVar fillerVar = new CobVar (childVar.level, null, null, null, null, null, false, false, 0, 0, null);
			fillerVar.child = parentVar.child;
			parentVar.child = fillerVar;
			fillerVar.sister = childVar;

		}

		public boolean hasParent() {
			return this.parent != null;
		}

		/**
		 * @return the child
		 */
		public CobVar getChild() {
			return this.child;
		}

		/**
		 * @return the sister
		 */
		public CobVar getSister() {
			return this.sister;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return this.name;
		}

		// START KGU#388 2017-10-03: Enh. #423
		/**
		 * Garantees a (local) name for this variable, i.e. if it hasn't been named
		 * a reproducable generic name will be returned, starting with "FILLER_X".<br/>
		 * NOTE: This might have become superfluous since filler names are forced on
		 * {@link CobVar} creation now. So method {@link #getName()} might be sufficient
		 * in any case now.
		 * @return the actually associated or generated name
		 */
		//
		public String forceName() {
			String myName = this.name;
			if (myName == null && this.parent != null) {
				// It is anonymous (i.e. a filler)
				// So count the younger siblings and generate a component name
				// (under the assumption that it is all a hieracrhical record structure).
				// It must only be unique at this level of hierarchy.
				int count = 0;
				CobVar sibling = this.parent.child;
				while (sibling != null && sibling != this) {
					sibling = sibling.sister;
					count++;
				}
				if (sibling != null) {
					myName = String.format("FILLER_X%1$02d", count);
				}
			}
			if (myName == null) {
				// This is the last measure...
				myName = "FILLER" + Integer.toHexString(this.hashCode());
			}
			return myName;
		}
		// END KGU#388 2017-10-03

		// START KGU388 2017-06-26: Enh. #423
		/**
		 * Returns a fully qualified variable name for this CobVar (i.e. the complete
		 * dot-separated path for a component of a record). This is needed for Structorizer.
		 * For levels representing a table (array) there will be a placeholder "[%i]" next
		 * the the respective component name (where i = 1...n is the index level from top
		 * to bottom), such that "%i" can be replaced by the respective i-th index expression.
		 * @return fully qualified name (e.g. "top.foo.bar").
		 */
		public String getQualifiedName() {
			String qualName = this.forceName();
			CobVar ancestor = this.parent;
			int nArrayLevels = 0;
			if (this.level < 100) {	// For index variables we don't use qualified names...
				while (ancestor != null) {
					String index = "";
					if (ancestor.isArray()) {
						nArrayLevels++;
						// put an index bracket with reverse level first
						index = "[%r" + nArrayLevels + "]";
					}
					qualName = ancestor.forceName() + index + "." + qualName;
					ancestor = ancestor.parent;
				}
			}
			// Now revert the provisional index place holders
			for (int i = 0; i < nArrayLevels; i++) {
				qualName = qualName.replace("%r" + (i + 1), "%" + (nArrayLevels - i));
			}
			return qualName;
		}
		// END KGU 2017-06-26

		/**
		 * @return the parent
		 */
		public CobVar getParent() {
			return this.parent;
		}

		/**
		 * @return the redefines
		 */
		public CobVar getRedefines() {
			return this.redefines;
		}

		/**
		 * @return the values as text for comparing, note: this text is a Java expression
		 */
		public String getValueComparisonString() {
			boolean firstValue = true;
			String valueComparison = "";
			for (Object value : this.values) {
				if (firstValue) {
					firstValue = false;
				} else {
					valueComparison += " || ";
				}
				valueComparison += value.toString();
			}
			return valueComparison;
		}

		public boolean isAnyLength() {
			return (this.anyLength == 1);
		}

		public boolean isAnyNumeric() {
			return (this.anyLength == 2);
		}

	}

	/**
	 * set attributes to a given variable from a valid (!) PICTURE clause
	 * sample inputs:  "s9(5)v9(2)", "zzzz999.99", "x(5000)", 9(number-of-items)
	 * @param CobVar
	 * @param picString validated picture, may contain constants
	 */
	private void setVarAttributesFromPic(CobVar variable, String picString) {
		boolean wantsDecimal = false;
		int vPos = 0;
		boolean wantsSign = false;

		Matcher picMatcher = null;
		StringBuffer picSB = null;

		picString = picString.toUpperCase();

		// replace constant values first (can be removed if we move this to the preparser later)
		picMatcher = Pattern.compile("\\(([^)]*[A-Z_-][^)]*)\\)").matcher(picString);
		picSB  = new StringBuffer(picString.length());
		while (picMatcher.find()) {
			// FIXME (KGU 2017-10-05) currentProgram is only initialized to null... Where can get it from?
			CobVar constVar = currentProgram.getCobVar(picMatcher.group());
			// END KGU 2017-10-05
			String constVal = "";
			if (constVar != null && constVar.getValueFirst() != null) {
				constVal = "(" + constVar.getValueFirst() + ")";
			}
			picMatcher.appendReplacement(picSB, constVal);
		}
		picMatcher.appendTail(picSB);
		picString = picSB.toString();

		vPos = picString.indexOf('V');
		if (picString.matches(".*P.+")) {
			// no remove for P as we'd need to shift the value in all places
			// TODO: Add handling of shifting numeric values
			wantsDecimal = true;
		} else if (vPos != -1) {
			wantsDecimal = true;
			picString = picString.substring(0, vPos) + picString.substring(vPos + 1);
		}

		if (picString.startsWith("S")) {
			picString = picString.substring(1);
			wantsSign = true;
		} else if (picString.endsWith("S")) {
			picString = picString.substring(0, picString.length() - 1);
			wantsSign = true;
		}

		variable.hasSign = wantsSign;
		variable.hasDecimal = wantsDecimal;

		// get length of value and remove the group part
		picMatcher = Pattern.compile("(.\\([0-9]+\\))").matcher(picString);
		picSB  = new StringBuffer(picString.length());

		variable.charLength = 0;

		while (picMatcher.find()) {
			// found group  X(6) or 9(0123); remove first 2 and last char, then parse
			String picGroup = picMatcher.group();
			String counterStr = picGroup.substring(2, picGroup.length() - 1);
			variable.charLength += Integer.parseInt(counterStr) - 1;
			picMatcher.appendReplacement(picSB, picGroup.substring(0, 1));
		}
		picMatcher.appendTail(picSB);
		variable.charLength += picSB.length();

		picString = picSB.toString();

		// calculate usage
		if (picString.contains("N")) {
			variable.usage = Usage.USAGE_NATIONAL;
			if (!picString.matches("N*")) {
				variable.isEdited = true;
			}
		} else if (picString.matches("9*")) {
			variable.usage = Usage.USAGE_DISPLAY_NUMERIC;
		} else if (picString.matches("[XA]*")) {
			variable.usage = Usage.USAGE_DISPLAY;
		} else {
			variable.usage = Usage.USAGE_DISPLAY;
			variable.isEdited = true;
		}
	}

	// START KGU#427 2017-10-05: Quick hack as workaround for the
	// NullPointerException in setVarAttributesFromPic()
	public void setProgram(CobProg currentProg) {
		this.currentProgram = currentProg;
	}
	// END KGU#427 2017-10-05

	public String createPicStringFromValues(String[] values) {
		String picString;
		int len = 0;
		if (values == null || values[0].isEmpty()) {
			// really bad code...
			picString = "X";
		} else {
			boolean isNumeric = false;
			char first = values[0].charAt(0);
			if (first == '\'' || first == '\"') {
				if (values[0].substring(1).startsWith("\\u00")) {
				picString = "X";
			} else {
					picString = "N";
				}
				} else {
				if (first == '-' || (first >= '0' && first <= '9')) {
					picString = "9";
					isNumeric = true;
				} else {
					picString = "X";
				}
			}
			for (String value : values) {
				if (value.length() > len) {
					len = value.length();
					if (isNumeric && value.charAt(0) == '-') {
						len--;
						picString = "S9";
					}
				}
				}
			// don't count quotes and "\\u", count as non-unicode
			if (!isNumeric) {
				len = (len - 4) / 4;
			}
			picString += "(" + len + ")";

		}
		return picString;
	}

	/**
	 * Returns the Java type of a given CobVar depending on its attributes including
	 * usage, picture and length.<br/>
	 * Note that in case of an array only the ELEMENT TYPE String will be returned
	 * unless {@code withArraySize} is set!
	 * @param CobVar - variable (or component) to return the type string for
	 * @param withArraySize - if in case of a table (array) the array size is to be appended as {@code [<size>]}.
	 * @return Java type representation as string, may be {@link #UNKNOWN_TYPE} in case of an unset usage
	 * if "string" and "long" can be excluded.
	 * @see CobVar#isArray()
	 * @see CobVar#getArraySize()
	 * @see #getTypeName(CobVar, boolean)
	 */
	public static String getTypeString(CobVar variable, boolean withArraySize) {
		if (variable == null) {
			return null;
		}
		// START KGU#388 2017-10-04: Enh. #423
		// usage can be null if this CobVar was created via CobVar(String, String[], String)
		else if (variable.usage == null) {
			logger.log(Level.INFO, "Variable {0} has unset usage field!", variable);
			return "";
		}
		// END KGU#388 2017-10-04
		String arraySuffix = "";
		if (withArraySize && variable.isArray()) {
			arraySuffix = "[" + variable.getArraySize() + "]";
		}
		switch (variable.usage) {
		case USAGE_BIT: // CHECKME
			return "";
		case USAGE_FLOAT:		// "plain" float  --> mapping to IEEE Std 754-1985 bin 32
		case USAGE_FP_BIN32:	// IEEE Std 754-2008 bin  32
			return "float" + arraySuffix;
		case USAGE_DOUBLE:		// "plain" double --> mapping to IEEE Std 754-1985 bin 64
		case USAGE_FP_BIN64:	// IEEE Std 754-2008 bin  64
		case USAGE_FP_BIN128:	// IEEE Std 754-2008 bin 128 - no 128bit floating point data in Java...
		case USAGE_FP_DEC64:	// IEEE Std 754-2008 dec  64 - no decimal floating point data in Java...
		case USAGE_FP_DEC128:	// IEEE Std 754-2008 dec 128 - no decimal/128bit floating point data in Java...
			return "double" + arraySuffix;
		case USAGE_INDEX:
			return "integer" + arraySuffix;
		case USAGE_LENGTH:
			return "integer" + arraySuffix;
		case USAGE_DISPLAY:
			// Note: this isn't "correct" as String (and char) are already 16-bit Unicode types
			// CHECKME: maybe return char[picsize]
			return "String";
		case USAGE_NATIONAL:
			// CHECKME: maybe return char[picsize]
			//return "String";
			return "char" + arraySuffix;
		case USAGE_OBJECT:
			return "Object" + arraySuffix;
		// Address types cannot be handled by Executor
		case USAGE_POINTER:
		case USAGE_PROGRAM_POINTER:
			return "pointer" + arraySuffix;
		case USAGE_SIGNED_CHAR:		//-128 [-2**7]			< n < 128 [2**7]
			return "byte" + arraySuffix;
		case USAGE_UNSIGNED_CHAR:	// 0 					≤ n < 256 [2**8]
			return "short" + arraySuffix;
		case USAGE_PACKED:
			return "double" + arraySuffix;
		case USAGE_SIGNED_INT:		// -2147483648 [-2**31]	< n < 2147483648 [2**31]
		case USAGE_UNSIGNED_INT:	// 0					≤ n < 4294967296 [2**32]
		case USAGE_SIGNED_LONG:		// -2**63				< n < 2**63
		case USAGE_UNSIGNED_LONG:	// 0					≤ n < 2**64		// not available in plain Java
		case USAGE_LONG_DOUBLE:		// checked
		case USAGE_COMP_X: // CHECKME
		case USAGE_COMP_5: // CHECKME
		case USAGE_COMP_6: // CHECKME
			return "long" + arraySuffix;
		case USAGE_SIGNED_SHORT:	// -32768 [-2**15] < n < 32768 [2**15]
			return "short" + arraySuffix;
		case USAGE_UNSIGNED_SHORT:	//  0 ≤ n < 65536 [2**16]
			return "integer" + arraySuffix;
		case USAGE_BINARY: //  two's-complement binary big-endian
		case USAGE_DISPLAY_NUMERIC:
			if (variable.hasDecimal) {
				// FIXME: Should be BigDecimal in Java or manual shifting with primitive data type should be done
				return "double" + arraySuffix;
			} else if (variable.charLength > 9) {
				if (variable.hasSign) {
					return "long" + arraySuffix;		// identical to USAGE_SIGNED_LONG;
				} else {
					return "long" + arraySuffix;		// identical to USAGE_UNSIGNED_LONG;
				}
			} else if (variable.charLength > 4) {
				if (variable.hasSign) {
					return "long" + arraySuffix;		// identical to USAGE_SIGNED_INT;
				} else {
					return "long" + arraySuffix;		// identical to USAGE_UNSIGNED_INT;
				}
			} else {
				if (variable.hasSign) {
					return "short" + arraySuffix;		// identical to USAGE_SIGNED_SHORT;
				} else {
					return "integer" + arraySuffix;	// identical to USAGE_UNSIGNED_SHORT;
				}
			}
		case USAGE_NOT_SET:
			if (variable.isAnyLength()) {
				return "String";
			} else if (variable.isAnyNumeric()) {
				return "long" + arraySuffix;
			} else {
				// CHECKME: does this happen? if not raise a warning or at least log a warning
				//return "";
				return UNKNOWN_TYPE;
			}
		// we explicitly don't want a default, allowing to check if all USAGEs have a value assigned
		}
		return "";
	}

	// START KGU#465 2017-12-04: Bugfix #473 - For hierarchical arguments we need a type name
	/**
	 * Returns the Java type of a given CobVar depending on its attributes including
	 * usage, picture and length.<br/>
	 * Note that in case of an array only the ELEMENT TYPE String will be returned
	 * unless {@code withArraySize} is set!<br/>
	 * This method is nearly identical to {@link #getTypeString(CobVar, boolean)} except
	 * that it derives a new type name from the variable name in cases the latter
	 * would return the {@value #UNKNOWN_TYPE} string.
	 * @param CobVar - variable (or component) to return the type string for
	 * @param withArraySize - if in case of a table (array) the array size is to be appended as {@code [<size>]}.
	 * @return Java type representation as string
	 * @see CobVar#isArray()
	 * @see CobVar#getArraySize()
	 * @see #getTypeString(CobVar, boolean)
	 */
	public static String getTypeName (CobVar variable, boolean withArraySize) {
		String typeName = getTypeString(variable, withArraySize);
		if (typeName.equals(UNKNOWN_TYPE)) {
			typeName = variable.deriveTypeName();
		}
		return typeName;
	}
	// END KGU#465 2017-12-04

}

/*
    Structorizer
    A little tool which you can use to create Nassi-Schneiderman Diagrams (NSD)

    Copyright (C) 2009  Bob Fisch
    Copyright (C) 2017  StructorizerParserTemplate.pgt: Kay Gürtzig
    Copyright (C) 2017  COBOLParser: Simon Sobisch

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
 *      Kay Gürtzig     2017.03.10      First Issue (automatically generated with GOLDprog.exe,
 *                                      constants for COBOL-85.grm downloaded from URL
 *                                      http://www.goldparser.org/grammars/files/COBOL-85.zip)
 *      Simon Sobisch   2017.03.22      COBOL preparser for **valid** COBOL sources: reference-format
 *                                      free-form and fixed-form (with continuation + debugging lines),
 *                                      minimal subset of compiler directives, passes NIST EXEC85.cob
 *                                      and DB201A.CBL (with manual source changes because of
 *                                      insufficent COBOL-85.grm)
 *      Kay Gürtzig     2017.03.26      Fix #384: New temp file mechanism for the prepared text file
 *      Simon Sobisch   2017.04.24      Moved from COBOL-85.grm (NOT being COBOL 85!!!) to GnuCOBOL.grm
 *      Kay Gürtzig     2017.05.07      ADD/SUBTRACT/MULTIPLY/DIVIDE with ROUNDED mode implemented, SET
 *                                      statement and string manipulations (ref mod) realized.
 *      Kay Gürtzig     2017.05.10      Further accomplishments for EVALUATE (ALSO included)
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
 ******************************************************************************************************/

import java.awt.Color;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.creativewidgetworks.goldparser.engine.*;
import com.creativewidgetworks.goldparser.engine.enums.SymbolType;

import lu.fisch.structorizer.elements.Alternative;
import lu.fisch.structorizer.elements.Call;
import lu.fisch.structorizer.elements.Case;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.Forever;
import lu.fisch.structorizer.elements.ILoop;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.elements.Jump;
import lu.fisch.structorizer.elements.Repeat;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.structorizer.elements.While;
import lu.fisch.utils.BString;
import lu.fisch.utils.StringList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.creativewidgetworks.goldparser.parser.GOLDParser;

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

	//---------------------- Grammar table constants DON'T MODIFY! ---------------------------

	// Symbolic constants naming the table indices of the symbols of the grammar 
	@SuppressWarnings("unused")
	private interface SymbolConstants 
	{
		final int SYM_EOF                                        =    0;  // (EOF)
		final int SYM_ERROR                                      =    1;  // (Error)
		final int SYM_COMMENT                                    =    2;  // Comment
		final int SYM_NEWLINE                                    =    3;  // NewLine
		final int SYM_WHITESPACE                                 =    4;  // Whitespace
		final int SYM_TIMESGT                                    =    5;  // '*>'
		final int SYM_ABS                                        =    6;  // ABS
		final int SYM_ACCEPT                                     =    7;  // ACCEPT
		final int SYM_ACCESS                                     =    8;  // ACCESS
		final int SYM_ACOS                                       =    9;  // ACOS
		final int SYM_ACUBINNUMLITERAL                           =   10;  // AcuBinNumLiteral
		final int SYM_ACUHEXNUMLITERAL                           =   11;  // AcuHexNumLiteral
		final int SYM_ACUOCTNUMLITERAL                           =   12;  // AcuOctNumLiteral
		final int SYM_ADD                                        =   13;  // ADD
		final int SYM_ADDRESS                                    =   14;  // ADDRESS
		final int SYM_ADVANCING                                  =   15;  // ADVANCING
		final int SYM_AFTER                                      =   16;  // AFTER
		final int SYM_ALL                                        =   17;  // ALL
		final int SYM_ALLOCATE                                   =   18;  // ALLOCATE
		final int SYM_ALPHABET                                   =   19;  // ALPHABET
		final int SYM_ALPHABETIC                                 =   20;  // ALPHABETIC
		final int SYM_ALPHABETIC_LOWER                           =   21;  // 'ALPHABETIC_LOWER'
		final int SYM_ALPHABETIC_UPPER                           =   22;  // 'ALPHABETIC_UPPER'
		final int SYM_ALPHANUMERIC                               =   23;  // ALPHANUMERIC
		final int SYM_ALPHANUMERIC_EDITED                        =   24;  // 'ALPHANUMERIC_EDITED'
		final int SYM_ALSO                                       =   25;  // ALSO
		final int SYM_ALTER                                      =   26;  // ALTER
		final int SYM_ALTERNATE                                  =   27;  // ALTERNATE
		final int SYM_AND                                        =   28;  // AND
		final int SYM_ANNUITY                                    =   29;  // ANNUITY
		final int SYM_ANY                                        =   30;  // ANY
		final int SYM_ARE                                        =   31;  // ARE
		final int SYM_AREA                                       =   32;  // AREA
		final int SYM_AREAS                                      =   33;  // AREAS
		final int SYM_ARGUMENT_NUMBER                            =   34;  // 'ARGUMENT_NUMBER'
		final int SYM_ARGUMENT_VALUE                             =   35;  // 'ARGUMENT_VALUE'
		final int SYM_AS                                         =   36;  // AS
		final int SYM_ASCENDING                                  =   37;  // ASCENDING
		final int SYM_ASCII                                      =   38;  // ASCII
		final int SYM_ASIN                                       =   39;  // ASIN
		final int SYM_ASSIGN                                     =   40;  // ASSIGN
		final int SYM_AT                                         =   41;  // AT
		final int SYM_ATAN                                       =   42;  // ATAN
		final int SYM_ATTRIBUTE                                  =   43;  // ATTRIBUTE
		final int SYM_AUTHOR                                     =   44;  // AUTHOR
		final int SYM_AUTO                                       =   45;  // AUTO
		final int SYM_AUTOMATIC                                  =   46;  // AUTOMATIC
		final int SYM_AWAY_FROM_ZERO                             =   47;  // 'AWAY_FROM_ZERO'
		final int SYM_BACKGROUND_COLOR                           =   48;  // 'BACKGROUND_COLOR'
		final int SYM_BASED                                      =   49;  // BASED
		final int SYM_BEFORE                                     =   50;  // BEFORE
		final int SYM_BELL                                       =   51;  // BELL
		final int SYM_BINARY                                     =   52;  // BINARY
		final int SYM_BINARY_CHAR                                =   53;  // 'BINARY_CHAR'
		final int SYM_BINARY_C_LONG                              =   54;  // 'BINARY_C_LONG'
		final int SYM_BINARY_DOUBLE                              =   55;  // 'BINARY_DOUBLE'
		final int SYM_BINARY_LONG                                =   56;  // 'BINARY_LONG'
		final int SYM_BINARY_SHORT                               =   57;  // 'BINARY_SHORT'
		final int SYM_BLANK                                      =   58;  // BLANK
		final int SYM_BLINK                                      =   59;  // BLINK
		final int SYM_BLOCK                                      =   60;  // BLOCK
		final int SYM_BOOLEANHEXLITERAL                          =   61;  // BooleanHexLiteral
		final int SYM_BOOLEANLITERAL                             =   62;  // BooleanLiteral
		final int SYM_BOOLEAN_OF_INTEGER                         =   63;  // 'BOOLEAN_OF_INTEGER'
		final int SYM_BOTTOM                                     =   64;  // BOTTOM
		final int SYM_BY                                         =   65;  // BY
		final int SYM_BYTE_LENGTH                                =   66;  // 'BYTE_LENGTH'
		final int SYM_CALL                                       =   67;  // CALL
		final int SYM_CANCEL                                     =   68;  // CANCEL
		final int SYM_CAPACITY                                   =   69;  // CAPACITY
		final int SYM_CARD_PUNCH                                 =   70;  // 'CARD_PUNCH'
		final int SYM_CARD_READER                                =   71;  // 'CARD_READER'
		final int SYM_CASSETTE                                   =   72;  // CASSETTE
		final int SYM_CD                                         =   73;  // CD
		final int SYM_CF                                         =   74;  // CF
		final int SYM_CH                                         =   75;  // CH
		final int SYM_CHAINING                                   =   76;  // CHAINING
		final int SYM_CHAR                                       =   77;  // CHAR
		final int SYM_CHARACTER                                  =   78;  // CHARACTER
		final int SYM_CHARACTERS                                 =   79;  // CHARACTERS
		final int SYM_CHAR_NATIONAL                              =   80;  // 'CHAR_NATIONAL'
		final int SYM_CLASS                                      =   81;  // CLASS
		final int SYM_CLASSIFICATION                             =   82;  // CLASSIFICATION
		final int SYM_CLOSE                                      =   83;  // CLOSE
		final int SYM_COBOL                                      =   84;  // COBOL
		final int SYM_COBOLWORD                                  =   85;  // COBOLWord
		final int SYM_CODE                                       =   86;  // CODE
		final int SYM_CODE_SET                                   =   87;  // 'CODE_SET'
		final int SYM_COL                                        =   88;  // COL
		final int SYM_COLLATING                                  =   89;  // COLLATING
		final int SYM_COLS                                       =   90;  // COLS
		final int SYM_COLUMN                                     =   91;  // COLUMN
		final int SYM_COLUMNS                                    =   92;  // COLUMNS
		final int SYM_COMBINED_DATETIME                          =   93;  // 'COMBINED_DATETIME'
		final int SYM_COMMA                                      =   94;  // COMMA
		final int SYM_COMMAND_LINE                               =   95;  // 'COMMAND_LINE'
		final int SYM_COMMA_DELIM                                =   96;  // 'COMMA_DELIM'
		final int SYM_COMMIT                                     =   97;  // COMMIT
		final int SYM_COMMON                                     =   98;  // COMMON
		final int SYM_COMMUNICATION                              =   99;  // COMMUNICATION
		final int SYM_COMP                                       =  100;  // COMP
		final int SYM_COMPUTE                                    =  101;  // COMPUTE
		final int SYM_COMP_1                                     =  102;  // 'COMP_1'
		final int SYM_COMP_2                                     =  103;  // 'COMP_2'
		final int SYM_COMP_3                                     =  104;  // 'COMP_3'
		final int SYM_COMP_4                                     =  105;  // 'COMP_4'
		final int SYM_COMP_5                                     =  106;  // 'COMP_5'
		final int SYM_COMP_6                                     =  107;  // 'COMP_6'
		final int SYM_COMP_X                                     =  108;  // 'COMP_X'
		final int SYM_CONCATENATE                                =  109;  // CONCATENATE
		final int SYM_CONFIGURATION                              =  110;  // CONFIGURATION
		final int SYM_CONSTANT                                   =  111;  // CONSTANT
		final int SYM_CONTAINS                                   =  112;  // CONTAINS
		final int SYM_CONTENT                                    =  113;  // CONTENT
		final int SYM_CONTINUE                                   =  114;  // CONTINUE
		final int SYM_CONTROL                                    =  115;  // CONTROL
		final int SYM_CONTROLS                                   =  116;  // CONTROLS
		final int SYM_CONVERSION                                 =  117;  // CONVERSION
		final int SYM_CONVERTING                                 =  118;  // CONVERTING
		final int SYM_CORRESPONDING                              =  119;  // CORRESPONDING
		final int SYM_COS                                        =  120;  // COS
		final int SYM_COUNT                                      =  121;  // COUNT
		final int SYM_CRT                                        =  122;  // CRT
		final int SYM_CRT_UNDER                                  =  123;  // 'CRT_UNDER'
		final int SYM_CURRENCY                                   =  124;  // CURRENCY
		final int SYM_CURRENCY_SYMBOL                            =  125;  // 'CURRENCY_SYMBOL'
		final int SYM_CURRENT_DATE                               =  126;  // 'CURRENT_DATE'
		final int SYM_CURSOR                                     =  127;  // CURSOR
		final int SYM_CYCLE                                      =  128;  // CYCLE
		final int SYM_DATA                                       =  129;  // DATA
		final int SYM_DATE                                       =  130;  // DATE
		final int SYM_DATE_COMPILED                              =  131;  // 'DATE_COMPILED'
		final int SYM_DATE_OF_INTEGER                            =  132;  // 'DATE_OF_INTEGER'
		final int SYM_DATE_TO_YYYYMMDD                           =  133;  // 'DATE_TO_YYYYMMDD'
		final int SYM_DATE_WRITTEN                               =  134;  // 'DATE_WRITTEN'
		final int SYM_DAY                                        =  135;  // DAY
		final int SYM_DAY_OF_INTEGER                             =  136;  // 'DAY_OF_INTEGER'
		final int SYM_DAY_OF_WEEK                                =  137;  // 'DAY_OF_WEEK'
		final int SYM_DAY_TO_YYYYDDD                             =  138;  // 'DAY_TO_YYYYDDD'
		final int SYM_DE                                         =  139;  // DE
		final int SYM_DEBUGGING                                  =  140;  // DEBUGGING
		final int SYM_DECIMALLITERAL                             =  141;  // DecimalLiteral
		final int SYM_DECIMAL_POINT                              =  142;  // 'DECIMAL_POINT'
		final int SYM_DECLARATIVES                               =  143;  // DECLARATIVES
		final int SYM_DEFAULT                                    =  144;  // DEFAULT
		final int SYM_DELETE                                     =  145;  // DELETE
		final int SYM_DELIMITED                                  =  146;  // DELIMITED
		final int SYM_DELIMITER                                  =  147;  // DELIMITER
		final int SYM_DEPENDING                                  =  148;  // DEPENDING
		final int SYM_DESCENDING                                 =  149;  // DESCENDING
		final int SYM_DESTINATION                                =  150;  // DESTINATION
		final int SYM_DETAIL                                     =  151;  // DETAIL
		final int SYM_DISABLE                                    =  152;  // DISABLE
		final int SYM_DISC                                       =  153;  // DISC
		final int SYM_DISK                                       =  154;  // DISK
		final int SYM_DISPLAY                                    =  155;  // DISPLAY
		final int SYM_DISPLAY_OF                                 =  156;  // 'DISPLAY_OF'
		final int SYM_DIVIDE                                     =  157;  // DIVIDE
		final int SYM_DIVISION                                   =  158;  // DIVISION
		final int SYM_DOWN                                       =  159;  // DOWN
		final int SYM_DUPLICATES                                 =  160;  // DUPLICATES
		final int SYM_DYNAMIC                                    =  161;  // DYNAMIC
		final int SYM_E                                          =  162;  // E
		final int SYM_EBCDIC                                     =  163;  // EBCDIC
		final int SYM_EC                                         =  164;  // EC
		final int SYM_ECHO                                       =  165;  // ECHO
		final int SYM_EGI                                        =  166;  // EGI
		final int SYM_EIGHTY_EIGHT                               =  167;  // 'EIGHTY_EIGHT'
		final int SYM_ELSE                                       =  168;  // ELSE
		final int SYM_EMI                                        =  169;  // EMI
		final int SYM_ENABLE                                     =  170;  // ENABLE
		final int SYM_END                                        =  171;  // END
		final int SYM_END_ACCEPT                                 =  172;  // 'END_ACCEPT'
		final int SYM_END_ADD                                    =  173;  // 'END_ADD'
		final int SYM_END_CALL                                   =  174;  // 'END_CALL'
		final int SYM_END_COMPUTE                                =  175;  // 'END_COMPUTE'
		final int SYM_END_DELETE                                 =  176;  // 'END_DELETE'
		final int SYM_END_DISPLAY                                =  177;  // 'END_DISPLAY'
		final int SYM_END_DIVIDE                                 =  178;  // 'END_DIVIDE'
		final int SYM_END_EVALUATE                               =  179;  // 'END_EVALUATE'
		final int SYM_END_FUNCTION                               =  180;  // 'END_FUNCTION'
		final int SYM_END_IF                                     =  181;  // 'END_IF'
		final int SYM_END_MULTIPLY                               =  182;  // 'END_MULTIPLY'
		final int SYM_END_PERFORM                                =  183;  // 'END_PERFORM'
		final int SYM_END_PROGRAM                                =  184;  // 'END_PROGRAM'
		final int SYM_END_READ                                   =  185;  // 'END_READ'
		final int SYM_END_RECEIVE                                =  186;  // 'END_RECEIVE'
		final int SYM_END_RETURN                                 =  187;  // 'END_RETURN'
		final int SYM_END_REWRITE                                =  188;  // 'END_REWRITE'
		final int SYM_END_SEARCH                                 =  189;  // 'END_SEARCH'
		final int SYM_END_START                                  =  190;  // 'END_START'
		final int SYM_END_STRING                                 =  191;  // 'END_STRING'
		final int SYM_END_SUBTRACT                               =  192;  // 'END_SUBTRACT'
		final int SYM_END_UNSTRING                               =  193;  // 'END_UNSTRING'
		final int SYM_END_WRITE                                  =  194;  // 'END_WRITE'
		final int SYM_ENTRY                                      =  195;  // ENTRY
		final int SYM_ENTRY_CONVENTION                           =  196;  // 'ENTRY_CONVENTION'
		final int SYM_ENVIRONMENT                                =  197;  // ENVIRONMENT
		final int SYM_ENVIRONMENT_VALUE                          =  198;  // 'ENVIRONMENT_VALUE'
		final int SYM_EOL                                        =  199;  // EOL
		final int SYM_EOP                                        =  200;  // EOP
		final int SYM_EOS                                        =  201;  // EOS
		final int SYM_EQUAL                                      =  202;  // EQUAL
		final int SYM_ERASE                                      =  203;  // ERASE
		final int SYM_ERROR2                                     =  204;  // ERROR
		final int SYM_ESCAPE                                     =  205;  // ESCAPE
		final int SYM_ESI                                        =  206;  // ESI
		final int SYM_EVALUATE                                   =  207;  // EVALUATE
		final int SYM_EVENT_STATUS                               =  208;  // 'EVENT_STATUS'
		final int SYM_EXCEPTION                                  =  209;  // EXCEPTION
		final int SYM_EXCEPTION_CONDITION                        =  210;  // 'EXCEPTION_CONDITION'
		final int SYM_EXCEPTION_FILE                             =  211;  // 'EXCEPTION_FILE'
		final int SYM_EXCEPTION_FILE_N                           =  212;  // 'EXCEPTION_FILE_N'
		final int SYM_EXCEPTION_LOCATION                         =  213;  // 'EXCEPTION_LOCATION'
		final int SYM_EXCEPTION_LOCATION_N                       =  214;  // 'EXCEPTION_LOCATION_N'
		final int SYM_EXCEPTION_STATEMENT                        =  215;  // 'EXCEPTION_STATEMENT'
		final int SYM_EXCEPTION_STATUS                           =  216;  // 'EXCEPTION_STATUS'
		final int SYM_EXCLUSIVE                                  =  217;  // EXCLUSIVE
		final int SYM_EXIT                                       =  218;  // EXIT
		final int SYM_EXP                                        =  219;  // EXP
		final int SYM_EXPONENTIATION                             =  220;  // EXPONENTIATION
		final int SYM_EXTEND                                     =  221;  // EXTEND
		final int SYM_EXTERNAL                                   =  222;  // EXTERNAL
		final int SYM_F                                          =  223;  // F
		final int SYM_FACTORIAL                                  =  224;  // FACTORIAL
		final int SYM_FD                                         =  225;  // FD
		final int SYM_FILE_CONTROL                               =  226;  // 'FILE_CONTROL'
		final int SYM_FILE_ID                                    =  227;  // 'FILE_ID'
		final int SYM_FILLER                                     =  228;  // FILLER
		final int SYM_FINAL                                      =  229;  // FINAL
		final int SYM_FIRST                                      =  230;  // FIRST
		final int SYM_FIXED                                      =  231;  // FIXED
		final int SYM_FLOATLITERAL                               =  232;  // FloatLiteral
		final int SYM_FLOAT_BINARY_128                           =  233;  // 'FLOAT_BINARY_128'
		final int SYM_FLOAT_BINARY_32                            =  234;  // 'FLOAT_BINARY_32'
		final int SYM_FLOAT_BINARY_64                            =  235;  // 'FLOAT_BINARY_64'
		final int SYM_FLOAT_DECIMAL_16                           =  236;  // 'FLOAT_DECIMAL_16'
		final int SYM_FLOAT_DECIMAL_34                           =  237;  // 'FLOAT_DECIMAL_34'
		final int SYM_FLOAT_DECIMAL_7                            =  238;  // 'FLOAT_DECIMAL_7'
		final int SYM_FLOAT_EXTENDED                             =  239;  // 'FLOAT_EXTENDED'
		final int SYM_FLOAT_LONG                                 =  240;  // 'FLOAT_LONG'
		final int SYM_FLOAT_SHORT                                =  241;  // 'FLOAT_SHORT'
		final int SYM_FOOTING                                    =  242;  // FOOTING
		final int SYM_FOR                                        =  243;  // FOR
		final int SYM_FOREGROUND_COLOR                           =  244;  // 'FOREGROUND_COLOR'
		final int SYM_FOREVER                                    =  245;  // FOREVER
		final int SYM_FORMATTED_CURRENT_DATE                     =  246;  // 'FORMATTED_CURRENT_DATE'
		final int SYM_FORMATTED_DATE                             =  247;  // 'FORMATTED_DATE'
		final int SYM_FORMATTED_DATETIME                         =  248;  // 'FORMATTED_DATETIME'
		final int SYM_FORMATTED_TIME                             =  249;  // 'FORMATTED_TIME'
		final int SYM_FRACTION_PART                              =  250;  // 'FRACTION_PART'
		final int SYM_FREE                                       =  251;  // FREE
		final int SYM_FROM                                       =  252;  // FROM
		final int SYM_FROM_CRT                                   =  253;  // 'FROM_CRT'
		final int SYM_FULL                                       =  254;  // FULL
		final int SYM_FUNCTION                                   =  255;  // FUNCTION
		final int SYM_FUNCTION_ID                                =  256;  // 'FUNCTION_ID'
		final int SYM_GENERATE                                   =  257;  // GENERATE
		final int SYM_GIVING                                     =  258;  // GIVING
		final int SYM_GLOBAL                                     =  259;  // GLOBAL
		final int SYM_GO                                         =  260;  // GO
		final int SYM_GOBACK                                     =  261;  // GOBACK
		final int SYM_GREATER                                    =  262;  // GREATER
		final int SYM_GREATER_OR_EQUAL                           =  263;  // 'GREATER_OR_EQUAL'
		final int SYM_GRID                                       =  264;  // GRID
		final int SYM_GROUP                                      =  265;  // GROUP
		final int SYM_HEADING                                    =  266;  // HEADING
		final int SYM_HEXLITERAL                                 =  267;  // HexLiteral
		final int SYM_HIGHEST_ALGEBRAIC                          =  268;  // 'HIGHEST_ALGEBRAIC'
		final int SYM_HIGHLIGHT                                  =  269;  // HIGHLIGHT
		final int SYM_HIGH_VALUE                                 =  270;  // 'HIGH_VALUE'
		final int SYM_ID                                         =  271;  // ID
		final int SYM_IDENTIFICATION                             =  272;  // IDENTIFICATION
		final int SYM_IF                                         =  273;  // IF
		final int SYM_IGNORE                                     =  274;  // IGNORE
		final int SYM_IGNORING                                   =  275;  // IGNORING
		final int SYM_IN                                         =  276;  // IN
		final int SYM_INDEX                                      =  277;  // INDEX
		final int SYM_INDEXED                                    =  278;  // INDEXED
		final int SYM_INDICATE                                   =  279;  // INDICATE
		final int SYM_INITIALIZE                                 =  280;  // INITIALIZE
		final int SYM_INITIALIZED                                =  281;  // INITIALIZED
		final int SYM_INITIATE                                   =  282;  // INITIATE
		final int SYM_INPUT                                      =  283;  // INPUT
		final int SYM_INPUT_OUTPUT                               =  284;  // 'INPUT_OUTPUT'
		final int SYM_INSPECT                                    =  285;  // INSPECT
		final int SYM_INSTALLATION                               =  286;  // INSTALLATION
		final int SYM_INTEGER                                    =  287;  // INTEGER
		final int SYM_INTEGER_OF_BOOLEAN                         =  288;  // 'INTEGER_OF_BOOLEAN'
		final int SYM_INTEGER_OF_DATE                            =  289;  // 'INTEGER_OF_DATE'
		final int SYM_INTEGER_OF_DAY                             =  290;  // 'INTEGER_OF_DAY'
		final int SYM_INTEGER_OF_FORMATTED_DATE                  =  291;  // 'INTEGER_OF_FORMATTED_DATE'
		final int SYM_INTEGER_PART                               =  292;  // 'INTEGER_PART'
		final int SYM_INTERMEDIATE                               =  293;  // INTERMEDIATE
		final int SYM_INTLITERAL                                 =  294;  // IntLiteral
		final int SYM_INTO                                       =  295;  // INTO
		final int SYM_INTRINSIC                                  =  296;  // INTRINSIC
		final int SYM_INVALID_KEY                                =  297;  // 'INVALID_KEY'
		final int SYM_IS                                         =  298;  // IS
		final int SYM_I_O                                        =  299;  // 'I_O'
		final int SYM_I_O_CONTROL                                =  300;  // 'I_O_CONTROL'
		final int SYM_JUSTIFIED                                  =  301;  // JUSTIFIED
		final int SYM_KEPT                                       =  302;  // KEPT
		final int SYM_KEY                                        =  303;  // KEY
		final int SYM_KEYBOARD                                   =  304;  // KEYBOARD
		final int SYM_LABEL                                      =  305;  // LABEL
		final int SYM_LAST                                       =  306;  // LAST
		final int SYM_LEADING                                    =  307;  // LEADING
		final int SYM_LEFT                                       =  308;  // LEFT
		final int SYM_LEFTLINE                                   =  309;  // LEFTLINE
		final int SYM_LENGTH                                     =  310;  // LENGTH
		final int SYM_LENGTH_OF                                  =  311;  // 'LENGTH_OF'
		final int SYM_LESS                                       =  312;  // LESS
		final int SYM_LESS_OR_EQUAL                              =  313;  // 'LESS_OR_EQUAL'
		final int SYM_LIMIT                                      =  314;  // LIMIT
		final int SYM_LIMITS                                     =  315;  // LIMITS
		final int SYM_LINAGE                                     =  316;  // LINAGE
		final int SYM_LINAGE_COUNTER                             =  317;  // 'LINAGE_COUNTER'
		final int SYM_LINE                                       =  318;  // LINE
		final int SYM_LINES                                      =  319;  // LINES
		final int SYM_LINE_COUNTER                               =  320;  // 'LINE_COUNTER'
		final int SYM_LINKAGE                                    =  321;  // LINKAGE
		final int SYM_LOCALE                                     =  322;  // LOCALE
		final int SYM_LOCALE_COMPARE                             =  323;  // 'LOCALE_COMPARE'
		final int SYM_LOCALE_DATE                                =  324;  // 'LOCALE_DATE'
		final int SYM_LOCALE_TIME                                =  325;  // 'LOCALE_TIME'
		final int SYM_LOCALE_TIME_FROM_SECONDS                   =  326;  // 'LOCALE_TIME_FROM_SECONDS'
		final int SYM_LOCAL_STORAGE                              =  327;  // 'LOCAL_STORAGE'
		final int SYM_LOCK                                       =  328;  // LOCK
		final int SYM_LOG                                        =  329;  // LOG
		final int SYM_LOWER                                      =  330;  // LOWER
		final int SYM_LOWER_CASE                                 =  331;  // 'LOWER_CASE'
		final int SYM_LOWEST_ALGEBRAIC                           =  332;  // 'LOWEST_ALGEBRAIC'
		final int SYM_LOWLIGHT                                   =  333;  // LOWLIGHT
		final int SYM_LOW_VALUE                                  =  334;  // 'LOW_VALUE'
		final int SYM_MAGNETIC_TAPE                              =  335;  // 'MAGNETIC_TAPE'
		final int SYM_MANUAL                                     =  336;  // MANUAL
		final int SYM_MAX                                        =  337;  // MAX
		final int SYM_MEAN                                       =  338;  // MEAN
		final int SYM_MEDIAN                                     =  339;  // MEDIAN
		final int SYM_MEMORY                                     =  340;  // MEMORY
		final int SYM_MERGE                                      =  341;  // MERGE
		final int SYM_MESSAGE                                    =  342;  // MESSAGE
		final int SYM_MIDRANGE                                   =  343;  // MIDRANGE
		final int SYM_MIN                                        =  344;  // MIN
		final int SYM_MINUS                                      =  345;  // MINUS
		final int SYM_MNEMONIC_NAME                              =  346;  // 'MNEMONIC_NAME'
		final int SYM_MOD                                        =  347;  // MOD
		final int SYM_MODE                                       =  348;  // MODE
		final int SYM_MODULE_CALLER_ID                           =  349;  // 'MODULE_CALLER_ID'
		final int SYM_MODULE_DATE                                =  350;  // 'MODULE_DATE'
		final int SYM_MODULE_FORMATTED_DATE                      =  351;  // 'MODULE_FORMATTED_DATE'
		final int SYM_MODULE_ID                                  =  352;  // 'MODULE_ID'
		final int SYM_MODULE_PATH                                =  353;  // 'MODULE_PATH'
		final int SYM_MODULE_SOURCE                              =  354;  // 'MODULE_SOURCE'
		final int SYM_MODULE_TIME                                =  355;  // 'MODULE_TIME'
		final int SYM_MONETARY_DECIMAL_POINT                     =  356;  // 'MONETARY_DECIMAL_POINT'
		final int SYM_MONETARY_THOUSANDS_SEPARATOR               =  357;  // 'MONETARY_THOUSANDS_SEPARATOR'
		final int SYM_MOVE                                       =  358;  // MOVE
		final int SYM_MULTIPLE                                   =  359;  // MULTIPLE
		final int SYM_MULTIPLY                                   =  360;  // MULTIPLY
		final int SYM_NAME                                       =  361;  // NAME
		final int SYM_NATIONAL                                   =  362;  // NATIONAL
		final int SYM_NATIONALHEXLITERAL                         =  363;  // NationalHexLiteral
		final int SYM_NATIONALLITERAL                            =  364;  // NationalLiteral
		final int SYM_NATIONAL_EDITED                            =  365;  // 'NATIONAL_EDITED'
		final int SYM_NATIONAL_OF                                =  366;  // 'NATIONAL_OF'
		final int SYM_NATIVE                                     =  367;  // NATIVE
		final int SYM_NEAREST_AWAY_FROM_ZERO                     =  368;  // 'NEAREST_AWAY_FROM_ZERO'
		final int SYM_NEAREST_EVEN                               =  369;  // 'NEAREST_EVEN'
		final int SYM_NEAREST_TOWARD_ZERO                        =  370;  // 'NEAREST_TOWARD_ZERO'
		final int SYM_NEGATIVE                                   =  371;  // NEGATIVE
		final int SYM_NESTED                                     =  372;  // NESTED
		final int SYM_NEXT                                       =  373;  // NEXT
		final int SYM_NEXT_PAGE                                  =  374;  // 'NEXT_PAGE'
		final int SYM_NO                                         =  375;  // NO
		final int SYM_NORMAL                                     =  376;  // NORMAL
		final int SYM_NOT                                        =  377;  // NOT
		final int SYM_NOTHING                                    =  378;  // NOTHING
		final int SYM_NOT_END                                    =  379;  // 'NOT_END'
		final int SYM_NOT_EOP                                    =  380;  // 'NOT_EOP'
		final int SYM_NOT_EQUAL                                  =  381;  // 'NOT_EQUAL'
		final int SYM_NOT_ESCAPE                                 =  382;  // 'NOT_ESCAPE'
		final int SYM_NOT_EXCEPTION                              =  383;  // 'NOT_EXCEPTION'
		final int SYM_NOT_INVALID_KEY                            =  384;  // 'NOT_INVALID_KEY'
		final int SYM_NOT_OVERFLOW                               =  385;  // 'NOT_OVERFLOW'
		final int SYM_NOT_SIZE_ERROR                             =  386;  // 'NOT_SIZE_ERROR'
		final int SYM_NO_ADVANCING                               =  387;  // 'NO_ADVANCING'
		final int SYM_NO_DATA                                    =  388;  // 'NO_DATA'
		final int SYM_NO_ECHO                                    =  389;  // 'NO_ECHO'
		final int SYM_NUMBER                                     =  390;  // NUMBER
		final int SYM_NUMBERS                                    =  391;  // NUMBERS
		final int SYM_NUMERIC                                    =  392;  // NUMERIC
		final int SYM_NUMERIC_DECIMAL_POINT                      =  393;  // 'NUMERIC_DECIMAL_POINT'
		final int SYM_NUMERIC_EDITED                             =  394;  // 'NUMERIC_EDITED'
		final int SYM_NUMERIC_THOUSANDS_SEPARATOR                =  395;  // 'NUMERIC_THOUSANDS_SEPARATOR'
		final int SYM_NUMVAL                                     =  396;  // NUMVAL
		final int SYM_NUMVAL_C                                   =  397;  // 'NUMVAL_C'
		final int SYM_NUMVAL_F                                   =  398;  // 'NUMVAL_F'
		final int SYM_OBJECT_COMPUTER                            =  399;  // 'OBJECT_COMPUTER'
		final int SYM_OCCURS                                     =  400;  // OCCURS
		final int SYM_OF                                         =  401;  // OF
		final int SYM_OFF                                        =  402;  // OFF
		final int SYM_OMITTED                                    =  403;  // OMITTED
		final int SYM_ON                                         =  404;  // ON
		final int SYM_ONLY                                       =  405;  // ONLY
		final int SYM_OPEN                                       =  406;  // OPEN
		final int SYM_OPTIONAL                                   =  407;  // OPTIONAL
		final int SYM_OPTIONS                                    =  408;  // OPTIONS
		final int SYM_OR                                         =  409;  // OR
		final int SYM_ORD                                        =  410;  // ORD
		final int SYM_ORDER                                      =  411;  // ORDER
		final int SYM_ORD_MAX                                    =  412;  // 'ORD_MAX'
		final int SYM_ORD_MIN                                    =  413;  // 'ORD_MIN'
		final int SYM_ORGANIZATION                               =  414;  // ORGANIZATION
		final int SYM_OTHER                                      =  415;  // OTHER
		final int SYM_OUTPUT                                     =  416;  // OUTPUT
		final int SYM_OVERLINE                                   =  417;  // OVERLINE
		final int SYM_PACKED_DECIMAL                             =  418;  // 'PACKED_DECIMAL'
		final int SYM_PADDING                                    =  419;  // PADDING
		final int SYM_PAGE                                       =  420;  // PAGE
		final int SYM_PAGE_COUNTER                               =  421;  // 'PAGE_COUNTER'
		final int SYM_PARAGRAPH                                  =  422;  // PARAGRAPH
		final int SYM_PERFORM                                    =  423;  // PERFORM
		final int SYM_PF                                         =  424;  // PF
		final int SYM_PH                                         =  425;  // PH
		final int SYM_PI                                         =  426;  // PI
		final int SYM_PICTURE_DEF                                =  427;  // 'Picture_Def'
		final int SYM_PICTURE_SYMBOL                             =  428;  // 'PICTURE_SYMBOL'
		final int SYM_PLUS                                       =  429;  // PLUS
		final int SYM_POINTER                                    =  430;  // POINTER
		final int SYM_POSITION                                   =  431;  // POSITION
		final int SYM_POSITIVE                                   =  432;  // POSITIVE
		final int SYM_PRESENT                                    =  433;  // PRESENT
		final int SYM_PRESENT_VALUE                              =  434;  // 'PRESENT_VALUE'
		final int SYM_PREVIOUS                                   =  435;  // PREVIOUS
		final int SYM_PRINT                                      =  436;  // PRINT
		final int SYM_PRINTER                                    =  437;  // PRINTER
		final int SYM_PRINTER_1                                  =  438;  // 'PRINTER_1'
		final int SYM_PRINTING                                   =  439;  // PRINTING
		final int SYM_PROCEDURE                                  =  440;  // PROCEDURE
		final int SYM_PROCEDURES                                 =  441;  // PROCEDURES
		final int SYM_PROCEED                                    =  442;  // PROCEED
		final int SYM_PROGRAM                                    =  443;  // PROGRAM
		final int SYM_PROGRAM_ID                                 =  444;  // 'PROGRAM_ID'
		final int SYM_PROGRAM_POINTER                            =  445;  // 'PROGRAM_POINTER'
		final int SYM_PROHIBITED                                 =  446;  // PROHIBITED
		final int SYM_PROMPT                                     =  447;  // PROMPT
		final int SYM_PROTECTED                                  =  448;  // PROTECTED
		final int SYM_PURGE                                      =  449;  // PURGE
		final int SYM_QUEUE                                      =  450;  // QUEUE
		final int SYM_QUOTE                                      =  451;  // QUOTE
		final int SYM_RANDOM                                     =  452;  // RANDOM
		final int SYM_RANGE                                      =  453;  // RANGE
		final int SYM_RD                                         =  454;  // RD
		final int SYM_READ                                       =  455;  // READ
		final int SYM_READY_TRACE                                =  456;  // 'READY_TRACE'
		final int SYM_RECEIVE                                    =  457;  // RECEIVE
		final int SYM_RECORD                                     =  458;  // RECORD
		final int SYM_RECORDING                                  =  459;  // RECORDING
		final int SYM_RECORDS                                    =  460;  // RECORDS
		final int SYM_RECURSIVE                                  =  461;  // RECURSIVE
		final int SYM_REDEFINES                                  =  462;  // REDEFINES
		final int SYM_REEL                                       =  463;  // REEL
		final int SYM_REFERENCE                                  =  464;  // REFERENCE
		final int SYM_REFERENCES                                 =  465;  // REFERENCES
		final int SYM_RELATIVE                                   =  466;  // RELATIVE
		final int SYM_RELEASE                                    =  467;  // RELEASE
		final int SYM_REM                                        =  468;  // REM
		final int SYM_REMAINDER                                  =  469;  // REMAINDER
		final int SYM_REMOVAL                                    =  470;  // REMOVAL
		final int SYM_RENAMES                                    =  471;  // RENAMES
		final int SYM_REPLACING                                  =  472;  // REPLACING
		final int SYM_REPORT                                     =  473;  // REPORT
		final int SYM_REPORTING                                  =  474;  // REPORTING
		final int SYM_REPORTS                                    =  475;  // REPORTS
		final int SYM_REPOSITORY                                 =  476;  // REPOSITORY
		final int SYM_REQUIRED                                   =  477;  // REQUIRED
		final int SYM_RESERVE                                    =  478;  // RESERVE
		final int SYM_RESET                                      =  479;  // RESET
		final int SYM_RESET_TRACE                                =  480;  // 'RESET_TRACE'
		final int SYM_RETRY                                      =  481;  // RETRY
		final int SYM_RETURN                                     =  482;  // RETURN
		final int SYM_RETURNING                                  =  483;  // RETURNING
		final int SYM_REVERSE                                    =  484;  // REVERSE
		final int SYM_REVERSED                                   =  485;  // REVERSED
		final int SYM_REVERSE_VIDEO                              =  486;  // 'REVERSE_VIDEO'
		final int SYM_REWIND                                     =  487;  // REWIND
		final int SYM_REWRITE                                    =  488;  // REWRITE
		final int SYM_RF                                         =  489;  // RF
		final int SYM_RH                                         =  490;  // RH
		final int SYM_RIGHT                                      =  491;  // RIGHT
		final int SYM_ROLLBACK                                   =  492;  // ROLLBACK
		final int SYM_ROUNDED                                    =  493;  // ROUNDED
		final int SYM_ROUNDING                                   =  494;  // ROUNDING
		final int SYM_RUN                                        =  495;  // RUN
		final int SYM_S                                          =  496;  // S
		final int SYM_SAME                                       =  497;  // SAME
		final int SYM_SCREEN                                     =  498;  // SCREEN
		final int SYM_SCREEN_CONTROL                             =  499;  // 'SCREEN_CONTROL'
		final int SYM_SCROLL                                     =  500;  // SCROLL
		final int SYM_SD                                         =  501;  // SD
		final int SYM_SEARCH                                     =  502;  // SEARCH
		final int SYM_SECONDS                                    =  503;  // SECONDS
		final int SYM_SECONDS_FROM_FORMATTED_TIME                =  504;  // 'SECONDS_FROM_FORMATTED_TIME'
		final int SYM_SECONDS_PAST_MIDNIGHT                      =  505;  // 'SECONDS_PAST_MIDNIGHT'
		final int SYM_SECTION                                    =  506;  // SECTION
		final int SYM_SECURE                                     =  507;  // SECURE
		final int SYM_SECURITY                                   =  508;  // SECURITY
		final int SYM_SEGMENT                                    =  509;  // SEGMENT
		final int SYM_SEGMENT_LIMIT                              =  510;  // 'SEGMENT_LIMIT'
		final int SYM_SELECT                                     =  511;  // SELECT
		final int SYM_SEMI_COLON                                 =  512;  // 'SEMI_COLON'
		final int SYM_SEND                                       =  513;  // SEND
		final int SYM_SENTENCE                                   =  514;  // SENTENCE
		final int SYM_SEPARATE                                   =  515;  // SEPARATE
		final int SYM_SEQUENCE                                   =  516;  // SEQUENCE
		final int SYM_SEQUENTIAL                                 =  517;  // SEQUENTIAL
		final int SYM_SET                                        =  518;  // SET
		final int SYM_SEVENTY_EIGHT                              =  519;  // 'SEVENTY_EIGHT'
		final int SYM_SHARING                                    =  520;  // SHARING
		final int SYM_SIGN                                       =  521;  // SIGN
		final int SYM_SIGNED                                     =  522;  // SIGNED
		final int SYM_SIGNED_INT                                 =  523;  // 'SIGNED_INT'
		final int SYM_SIGNED_LONG                                =  524;  // 'SIGNED_LONG'
		final int SYM_SIGNED_SHORT                               =  525;  // 'SIGNED_SHORT'
		final int SYM_SIN                                        =  526;  // SIN
		final int SYM_SIXTY_SIX                                  =  527;  // 'SIXTY_SIX'
		final int SYM_SIZE                                       =  528;  // SIZE
		final int SYM_SIZE_ERROR                                 =  529;  // 'SIZE_ERROR'
		final int SYM_SORT                                       =  530;  // SORT
		final int SYM_SORT_MERGE                                 =  531;  // 'SORT_MERGE'
		final int SYM_SOURCE                                     =  532;  // SOURCE
		final int SYM_SOURCE_COMPUTER                            =  533;  // 'SOURCE_COMPUTER'
		final int SYM_SPACE                                      =  534;  // SPACE
		final int SYM_SPECIAL_NAMES                              =  535;  // 'SPECIAL_NAMES'
		final int SYM_SQRT                                       =  536;  // SQRT
		final int SYM_STANDARD                                   =  537;  // STANDARD
		final int SYM_STANDARD_1                                 =  538;  // 'STANDARD_1'
		final int SYM_STANDARD_2                                 =  539;  // 'STANDARD_2'
		final int SYM_STANDARD_COMPARE                           =  540;  // 'STANDARD_COMPARE'
		final int SYM_STANDARD_DEVIATION                         =  541;  // 'STANDARD_DEVIATION'
		final int SYM_START                                      =  542;  // START
		final int SYM_STATIC                                     =  543;  // STATIC
		final int SYM_STATUS                                     =  544;  // STATUS
		final int SYM_STDCALL                                    =  545;  // STDCALL
		final int SYM_STEP                                       =  546;  // STEP
		final int SYM_STOP                                       =  547;  // STOP
		final int SYM_STORED_CHAR_LENGTH                         =  548;  // 'STORED_CHAR_LENGTH'
		final int SYM_STRING                                     =  549;  // STRING
		final int SYM_STRINGLITERAL                              =  550;  // StringLiteral
		final int SYM_SUBSTITUTE                                 =  551;  // SUBSTITUTE
		final int SYM_SUBSTITUTE_CASE                            =  552;  // 'SUBSTITUTE_CASE'
		final int SYM_SUBTRACT                                   =  553;  // SUBTRACT
		final int SYM_SUB_QUEUE_1                                =  554;  // 'SUB_QUEUE_1'
		final int SYM_SUB_QUEUE_2                                =  555;  // 'SUB_QUEUE_2'
		final int SYM_SUB_QUEUE_3                                =  556;  // 'SUB_QUEUE_3'
		final int SYM_SUM                                        =  557;  // SUM
		final int SYM_SUPPRESS                                   =  558;  // SUPPRESS
		final int SYM_SYMBOLIC                                   =  559;  // SYMBOLIC
		final int SYM_SYNCHRONIZED                               =  560;  // SYNCHRONIZED
		final int SYM_SYSTEM_DEFAULT                             =  561;  // 'SYSTEM_DEFAULT'
		final int SYM_SYSTEM_OFFSET                              =  562;  // 'SYSTEM_OFFSET'
		final int SYM_TAB                                        =  563;  // TAB
		final int SYM_TABLE                                      =  564;  // TABLE
		final int SYM_TALLYING                                   =  565;  // TALLYING
		final int SYM_TAN                                        =  566;  // TAN
		final int SYM_TAPE                                       =  567;  // TAPE
		final int SYM_TERMINAL                                   =  568;  // TERMINAL
		final int SYM_TERMINATE                                  =  569;  // TERMINATE
		final int SYM_TEST                                       =  570;  // TEST
		final int SYM_TEST_DATE_YYYYMMDD                         =  571;  // 'TEST_DATE_YYYYMMDD'
		final int SYM_TEST_DAY_YYYYDDD                           =  572;  // 'TEST_DAY_YYYYDDD'
		final int SYM_TEST_FORMATTED_DATETIME                    =  573;  // 'TEST_FORMATTED_DATETIME'
		final int SYM_TEST_NUMVAL                                =  574;  // 'TEST_NUMVAL'
		final int SYM_TEST_NUMVAL_F                              =  575;  // 'TEST_NUMVAL_F'
		final int SYM_TEXT                                       =  576;  // TEXT
		final int SYM_THEN                                       =  577;  // THEN
		final int SYM_THRU                                       =  578;  // THRU
		final int SYM_TIME                                       =  579;  // TIME
		final int SYM_TIMES                                      =  580;  // TIMES
		final int SYM_TIME_OUT                                   =  581;  // 'TIME_OUT'
		final int SYM_TO                                         =  582;  // TO
		final int SYM_TOK_AMPER                                  =  583;  // 'TOK_AMPER'
		final int SYM_TOK_CLOSE_PAREN                            =  584;  // 'TOK_CLOSE_PAREN'
		final int SYM_TOK_COLON                                  =  585;  // 'TOK_COLON'
		final int SYM_TOK_DIV                                    =  586;  // 'TOK_DIV'
		final int SYM_TOK_DOT                                    =  587;  // 'TOK_DOT'
		final int SYM_TOK_EQUAL                                  =  588;  // 'TOK_EQUAL'
		final int SYM_TOK_EXTERN                                 =  589;  // 'TOK_EXTERN'
		final int SYM_TOK_FALSE                                  =  590;  // 'TOK_FALSE'
		final int SYM_TOK_FILE                                   =  591;  // 'TOK_FILE'
		final int SYM_TOK_GREATER                                =  592;  // 'TOK_GREATER'
		final int SYM_TOK_INITIAL                                =  593;  // 'TOK_INITIAL'
		final int SYM_TOK_LESS                                   =  594;  // 'TOK_LESS'
		final int SYM_TOK_MINUS                                  =  595;  // 'TOK_MINUS'
		final int SYM_TOK_MUL                                    =  596;  // 'TOK_MUL'
		final int SYM_TOK_NULL                                   =  597;  // 'TOK_NULL'
		final int SYM_TOK_OPEN_PAREN                             =  598;  // 'TOK_OPEN_PAREN'
		final int SYM_TOK_OVERFLOW                               =  599;  // 'TOK_OVERFLOW'
		final int SYM_TOK_PLUS                                   =  600;  // 'TOK_PLUS'
		final int SYM_TOK_TRUE                                   =  601;  // 'TOK_TRUE'
		final int SYM_TOP                                        =  602;  // TOP
		final int SYM_TOWARD_GREATER                             =  603;  // 'TOWARD_GREATER'
		final int SYM_TOWARD_LESSER                              =  604;  // 'TOWARD_LESSER'
		final int SYM_TRAILING                                   =  605;  // TRAILING
		final int SYM_TRANSFORM                                  =  606;  // TRANSFORM
		final int SYM_TRIM                                       =  607;  // TRIM
		final int SYM_TRUNCATION                                 =  608;  // TRUNCATION
		final int SYM_TYPE                                       =  609;  // TYPE
		final int SYM_U                                          =  610;  // U
		final int SYM_UNBOUNDED                                  =  611;  // UNBOUNDED
		final int SYM_UNDERLINE                                  =  612;  // UNDERLINE
		final int SYM_UNIT                                       =  613;  // UNIT
		final int SYM_UNLOCK                                     =  614;  // UNLOCK
		final int SYM_UNSIGNED                                   =  615;  // UNSIGNED
		final int SYM_UNSIGNED_INT                               =  616;  // 'UNSIGNED_INT'
		final int SYM_UNSIGNED_LONG                              =  617;  // 'UNSIGNED_LONG'
		final int SYM_UNSIGNED_SHORT                             =  618;  // 'UNSIGNED_SHORT'
		final int SYM_UNSTRING                                   =  619;  // UNSTRING
		final int SYM_UNTIL                                      =  620;  // UNTIL
		final int SYM_UP                                         =  621;  // UP
		final int SYM_UPDATE                                     =  622;  // UPDATE
		final int SYM_UPON                                       =  623;  // UPON
		final int SYM_UPON_ARGUMENT_NUMBER                       =  624;  // 'UPON_ARGUMENT_NUMBER'
		final int SYM_UPON_COMMAND_LINE                          =  625;  // 'UPON_COMMAND_LINE'
		final int SYM_UPON_ENVIRONMENT_NAME                      =  626;  // 'UPON_ENVIRONMENT_NAME'
		final int SYM_UPON_ENVIRONMENT_VALUE                     =  627;  // 'UPON_ENVIRONMENT_VALUE'
		final int SYM_UPPER                                      =  628;  // UPPER
		final int SYM_UPPER_CASE                                 =  629;  // 'UPPER_CASE'
		final int SYM_USAGE                                      =  630;  // USAGE
		final int SYM_USE                                        =  631;  // USE
		final int SYM_USER                                       =  632;  // USER
		final int SYM_USER_DEFAULT                               =  633;  // 'USER_DEFAULT'
		final int SYM_USING                                      =  634;  // USING
		final int SYM_V                                          =  635;  // V
		final int SYM_VALUE                                      =  636;  // VALUE
		final int SYM_VARIABLE                                   =  637;  // VARIABLE
		final int SYM_VARIANCE                                   =  638;  // VARIANCE
		final int SYM_VARYING                                    =  639;  // VARYING
		final int SYM_WAIT                                       =  640;  // WAIT
		final int SYM_WHEN                                       =  641;  // WHEN
		final int SYM_WHEN_COMPILED                              =  642;  // 'WHEN_COMPILED'
		final int SYM_WITH                                       =  643;  // WITH
		final int SYM_WITH_DATA                                  =  644;  // 'WITH_DATA'
		final int SYM_WORDS                                      =  645;  // WORDS
		final int SYM_WORKING_STORAGE                            =  646;  // 'WORKING_STORAGE'
		final int SYM_WRITE                                      =  647;  // WRITE
		final int SYM_YEAR_TO_YYYY                               =  648;  // 'YEAR_TO_YYYY'
		final int SYM_YYYYDDD                                    =  649;  // YYYYDDD
		final int SYM_YYYYMMDD                                   =  650;  // YYYYMMDD
		final int SYM_ZERO                                       =  651;  // ZERO
		final int SYM_ZLITERAL                                   =  652;  // ZLiteral
		final int SYM_ACCEPT_BODY                                =  653;  // <accept_body>
		final int SYM_ACCEPT_CLAUSE                              =  654;  // <accept_clause>
		final int SYM_ACCEPT_CLAUSES                             =  655;  // <accept_clauses>
		final int SYM_ACCEPT_STATEMENT                           =  656;  // <accept_statement>
		final int SYM_ACCESS_MODE                                =  657;  // <access_mode>
		final int SYM_ACCESS_MODE_CLAUSE                         =  658;  // <access_mode_clause>
		final int SYM_ACCP_ATTR                                  =  659;  // <accp_attr>
		final int SYM_ACCP_IDENTIFIER                            =  660;  // <accp_identifier>
		final int SYM_ACCP_NOT_ON_EXCEPTION                      =  661;  // <accp_not_on_exception>
		final int SYM_ACCP_ON_EXCEPTION                          =  662;  // <accp_on_exception>
		final int SYM_ADD_BODY                                   =  663;  // <add_body>
		final int SYM_ADD_STATEMENT                              =  664;  // <add_statement>
		final int SYM_ADVANCING_LOCK_OR_RETRY                    =  665;  // <advancing_lock_or_retry>
		final int SYM_ALLOCATE_BODY                              =  666;  // <allocate_body>
		final int SYM_ALLOCATE_RETURNING                         =  667;  // <allocate_returning>
		final int SYM_ALLOCATE_STATEMENT                         =  668;  // <allocate_statement>
		final int SYM_ALNUM_OR_ID                                =  669;  // <alnum_or_id>
		final int SYM_ALPHABET_ALSO_SEQUENCE                     =  670;  // <alphabet_also_sequence>
		final int SYM_ALPHABET_DEFINITION                        =  671;  // <alphabet_definition>
		final int SYM_ALPHABET_LITERAL                           =  672;  // <alphabet_literal>
		final int SYM_ALPHABET_LITERAL_LIST                      =  673;  // <alphabet_literal_list>
		final int SYM_ALPHABET_LITS                              =  674;  // <alphabet_lits>
		final int SYM_ALPHABET_NAME                              =  675;  // <alphabet_name>
		final int SYM_ALPHABET_NAME_CLAUSE                       =  676;  // <alphabet_name_clause>
		final int SYM_ALTERNATIVE_RECORD_KEY_CLAUSE              =  677;  // <alternative_record_key_clause>
		final int SYM_ALTER_BODY                                 =  678;  // <alter_body>
		final int SYM_ALTER_ENTRY                                =  679;  // <alter_entry>
		final int SYM_ALTER_STATEMENT                            =  680;  // <alter_statement>
		final int SYM_ANY_LENGTH_CLAUSE                          =  681;  // <any_length_clause>
		final int SYM_ARITHMETIC_X                               =  682;  // <arithmetic_x>
		final int SYM_ARITHMETIC_X_LIST                          =  683;  // <arithmetic_x_list>
		final int SYM_ARITH_X                                    =  684;  // <arith_x>
		final int SYM_ASCENDING_OR_DESCENDING                    =  685;  // <ascending_or_descending>
		final int SYM_ASSIGNMENT_NAME                            =  686;  // <assignment_name>
		final int SYM_ASSIGN_CLAUSE                              =  687;  // <assign_clause>
		final int SYM_AT_END                                     =  688;  // <at_end>
		final int SYM_AT_END_CLAUSE                              =  689;  // <at_end_clause>
		final int SYM_AT_EOP_CLAUSE                              =  690;  // <at_eop_clause>
		final int SYM_AT_EOP_CLAUSES                             =  691;  // <at_eop_clauses>
		final int SYM_AT_LINE_COLUMN                             =  692;  // <at_line_column>
		final int SYM_BASED_CLAUSE                               =  693;  // <based_clause>
		final int SYM_BASIC_LITERAL                              =  694;  // <basic_literal>
		final int SYM_BASIC_VALUE                                =  695;  // <basic_value>
		final int SYM_BEFORE_OR_AFTER                            =  696;  // <before_or_after>
		final int SYM_BLANK_CLAUSE                               =  697;  // <blank_clause>
		final int SYM_BLOCK_CONTAINS_CLAUSE                      =  698;  // <block_contains_clause>
		final int SYM_CALL_BODY                                  =  699;  // <call_body>
		final int SYM_CALL_EXCEPTION_PHRASES                     =  700;  // <call_exception_phrases>
		final int SYM_CALL_NOT_ON_EXCEPTION                      =  701;  // <call_not_on_exception>
		final int SYM_CALL_ON_EXCEPTION                          =  702;  // <call_on_exception>
		final int SYM_CALL_PARAM                                 =  703;  // <call_param>
		final int SYM_CALL_PARAM_LIST                            =  704;  // <call_param_list>
		final int SYM_CALL_RETURNING                             =  705;  // <call_returning>
		final int SYM_CALL_STATEMENT                             =  706;  // <call_statement>
		final int SYM_CALL_TYPE                                  =  707;  // <call_type>
		final int SYM_CALL_USING                                 =  708;  // <call_using>
		final int SYM_CALL_X                                     =  709;  // <call_x>
		final int SYM_CANCEL_BODY                                =  710;  // <cancel_body>
		final int SYM_CANCEL_STATEMENT                           =  711;  // <cancel_statement>
		final int SYM_CD_NAME                                    =  712;  // <cd_name>
		final int SYM_CF_KEYWORD                                 =  713;  // <cf_keyword>
		final int SYM_CHAR_LIST                                  =  714;  // <char_list>
		final int SYM_CH_KEYWORD                                 =  715;  // <ch_keyword>
		final int SYM_CLASS_ITEM                                 =  716;  // <class_item>
		final int SYM_CLASS_ITEM_LIST                            =  717;  // <class_item_list>
		final int SYM_CLASS_NAME                                 =  718;  // <CLASS_NAME>
		final int SYM_CLASS_NAME_CLAUSE                          =  719;  // <class_name_clause>
		final int SYM_CLASS_VALUE                                =  720;  // <class_value>
		final int SYM_CLOSE_BODY                                 =  721;  // <close_body>
		final int SYM_CLOSE_OPTION                               =  722;  // <close_option>
		final int SYM_CLOSE_STATEMENT                            =  723;  // <close_statement>
		final int SYM_CODE_SET_CLAUSE                            =  724;  // <code_set_clause>
		final int SYM_COLLATING_SEQUENCE_CLAUSE                  =  725;  // <collating_sequence_clause>
		final int SYM_COLL_SEQUENCE                              =  726;  // <coll_sequence>
		final int SYM_COLUMNS_OR_COLS                            =  727;  // <columns_or_cols>
		final int SYM_COLUMN_CLAUSE                              =  728;  // <column_clause>
		final int SYM_COLUMN_NUMBER                              =  729;  // <column_number>
		final int SYM_COLUMN_OR_COL                              =  730;  // <column_or_col>
		final int SYM_COL_KEYWORD_CLAUSE                         =  731;  // <col_keyword_clause>
		final int SYM_COL_OR_PLUS                                =  732;  // <col_or_plus>
		final int SYM_COMMENTITEM                                =  733;  // <Comment Item>
		final int SYM_COMMIT_STATEMENT                           =  734;  // <commit_statement>
		final int SYM_COMMON_FUNCTION                            =  735;  // <COMMON_FUNCTION>
		final int SYM_COMMUNICATION_DESCRIPTION                  =  736;  // <communication_description>
		final int SYM_COMMUNICATION_DESCRIPTION_CLAUSE           =  737;  // <communication_description_clause>
		final int SYM_COMMUNICATION_DESCRIPTION_ENTRY            =  738;  // <communication_description_entry>
		final int SYM_COMMUNICATION_MODE                         =  739;  // <communication_mode>
		final int SYM_COMPILATION_GROUP                          =  740;  // <compilation_group>
		final int SYM_COMPUTER_WORDS                             =  741;  // <computer_words>
		final int SYM_COMPUTE_BODY                               =  742;  // <compute_body>
		final int SYM_COMPUTE_STATEMENT                          =  743;  // <compute_statement>
		final int SYM_COMP_EQUAL                                 =  744;  // <comp_equal>
		final int SYM_CONCATENATE_FUNC                           =  745;  // <CONCATENATE_FUNC>
		final int SYM_CONDITION                                  =  746;  // <condition>
		final int SYM_CONDITION_NAME_ENTRY                       =  747;  // <condition_name_entry>
		final int SYM_CONDITION_OP                               =  748;  // <condition_op>
		final int SYM_CONDITION_OR_CLASS                         =  749;  // <condition_or_class>
		final int SYM_COND_OR_EXIT                               =  750;  // <cond_or_exit>
		final int SYM_CONSTANT_ENTRY                             =  751;  // <constant_entry>
		final int SYM_CONSTANT_SOURCE                            =  752;  // <constant_source>
		final int SYM_CONST_GLOBAL                               =  753;  // <const_global>
		final int SYM_CONTINUE_STATEMENT                         =  754;  // <continue_statement>
		final int SYM_CONTROL_CLAUSE                             =  755;  // <control_clause>
		final int SYM_CONTROL_FIELD_LIST                         =  756;  // <control_field_list>
		final int SYM_CONTROL_KEYWORD                            =  757;  // <control_keyword>
		final int SYM_CONVENTION_TYPE                            =  758;  // <convention_type>
		final int SYM_CON_IDENTIFIER                             =  759;  // <con_identifier>
		final int SYM_CRT_STATUS_CLAUSE                          =  760;  // <crt_status_clause>
		final int SYM_CRT_UNDER2                                 =  761;  // <crt_under>
		final int SYM_CURRENCY_SIGN_CLAUSE                       =  762;  // <currency_sign_clause>
		final int SYM_CURRENT_DATE_FUNC                          =  763;  // <CURRENT_DATE_FUNC>
		final int SYM_CURSOR_CLAUSE                              =  764;  // <cursor_clause>
		final int SYM_DATA_DESCRIPTION                           =  765;  // <data_description>
		final int SYM_DATA_DESCRIPTION_CLAUSE                    =  766;  // <data_description_clause>
		final int SYM_DATA_OR_FINAL                              =  767;  // <data_or_final>
		final int SYM_DATA_RECORDS_CLAUSE                        =  768;  // <data_records_clause>
		final int SYM_DEBUGGING_LIST                             =  769;  // <debugging_list>
		final int SYM_DEBUGGING_TARGET                           =  770;  // <debugging_target>
		final int SYM_DECIMAL_POINT_CLAUSE                       =  771;  // <decimal_point_clause>
		final int SYM_DELETE_BODY                                =  772;  // <delete_body>
		final int SYM_DELETE_FILE_LIST                           =  773;  // <delete_file_list>
		final int SYM_DELETE_STATEMENT                           =  774;  // <delete_statement>
		final int SYM_DETAIL_KEYWORD                             =  775;  // <detail_keyword>
		final int SYM_DISABLE_STATEMENT                          =  776;  // <disable_statement>
		final int SYM_DISALLOWED_OP                              =  777;  // <disallowed_op>
		final int SYM_DISPLAY_ATOM                               =  778;  // <display_atom>
		final int SYM_DISPLAY_BODY                               =  779;  // <display_body>
		final int SYM_DISPLAY_CLAUSE                             =  780;  // <display_clause>
		final int SYM_DISPLAY_CLAUSES                            =  781;  // <display_clauses>
		final int SYM_DISPLAY_IDENTIFIER                         =  782;  // <display_identifier>
		final int SYM_DISPLAY_LIST                               =  783;  // <display_list>
		final int SYM_DISPLAY_OF_FUNC                            =  784;  // <DISPLAY_OF_FUNC>
		final int SYM_DISPLAY_STATEMENT                          =  785;  // <display_statement>
		final int SYM_DISPLAY_UPON                               =  786;  // <display_upon>
		final int SYM_DISP_ATTR                                  =  787;  // <disp_attr>
		final int SYM_DISP_LIST                                  =  788;  // <disp_list>
		final int SYM_DISP_NOT_ON_EXCEPTION                      =  789;  // <disp_not_on_exception>
		final int SYM_DISP_ON_EXCEPTION                          =  790;  // <disp_on_exception>
		final int SYM_DIVIDE_BODY                                =  791;  // <divide_body>
		final int SYM_DIVIDE_STATEMENT                           =  792;  // <divide_statement>
		final int SYM_DOUBLE_USAGE                               =  793;  // <double_usage>
		final int SYM_ENABLE_DISABLE_HANDLING                    =  794;  // <enable_disable_handling>
		final int SYM_ENABLE_STATEMENT                           =  795;  // <enable_statement>
		final int SYM_END_ACCEPT2                                =  796;  // <end_accept>
		final int SYM_END_ADD2                                   =  797;  // <end_add>
		final int SYM_END_CALL2                                  =  798;  // <end_call>
		final int SYM_END_COMPUTE2                               =  799;  // <end_compute>
		final int SYM_END_DELETE2                                =  800;  // <end_delete>
		final int SYM_END_DISPLAY2                               =  801;  // <end_display>
		final int SYM_END_DIVIDE2                                =  802;  // <end_divide>
		final int SYM_END_EVALUATE2                              =  803;  // <end_evaluate>
		final int SYM_END_FUNCTION2                              =  804;  // <end_function>
		final int SYM_END_IF2                                    =  805;  // <end_if>
		final int SYM_END_MULTIPLY2                              =  806;  // <end_multiply>
		final int SYM_END_PERFORM2                               =  807;  // <end_perform>
		final int SYM_END_PROGRAM2                               =  808;  // <end_program>
		final int SYM_END_PROGRAM_LIST                           =  809;  // <end_program_list>
		final int SYM_END_PROGRAM_NAME                           =  810;  // <end_program_name>
		final int SYM_END_READ2                                  =  811;  // <end_read>
		final int SYM_END_RECEIVE2                               =  812;  // <end_receive>
		final int SYM_END_RETURN2                                =  813;  // <end_return>
		final int SYM_END_REWRITE2                               =  814;  // <end_rewrite>
		final int SYM_END_SEARCH2                                =  815;  // <end_search>
		final int SYM_END_START2                                 =  816;  // <end_start>
		final int SYM_END_STRING2                                =  817;  // <end_string>
		final int SYM_END_SUBTRACT2                              =  818;  // <end_subtract>
		final int SYM_END_UNSTRING2                              =  819;  // <end_unstring>
		final int SYM_END_WRITE2                                 =  820;  // <end_write>
		final int SYM_ENTRY_BODY                                 =  821;  // <entry_body>
		final int SYM_ENTRY_STATEMENT                            =  822;  // <entry_statement>
		final int SYM_EOL2                                       =  823;  // <eol>
		final int SYM_EOS2                                       =  824;  // <eos>
		final int SYM_EQ                                         =  825;  // <eq>
		final int SYM_ERROR_STMT_RECOVER                         =  826;  // <error_stmt_recover>
		final int SYM_ESCAPE_OR_EXCEPTION                        =  827;  // <escape_or_exception>
		final int SYM_EVALUATE_BODY                              =  828;  // <evaluate_body>
		final int SYM_EVALUATE_CASE                              =  829;  // <evaluate_case>
		final int SYM_EVALUATE_CASE_LIST                         =  830;  // <evaluate_case_list>
		final int SYM_EVALUATE_CONDITION_LIST                    =  831;  // <evaluate_condition_list>
		final int SYM_EVALUATE_OBJECT                            =  832;  // <evaluate_object>
		final int SYM_EVALUATE_OBJECT_LIST                       =  833;  // <evaluate_object_list>
		final int SYM_EVALUATE_OTHER                             =  834;  // <evaluate_other>
		final int SYM_EVALUATE_STATEMENT                         =  835;  // <evaluate_statement>
		final int SYM_EVALUATE_SUBJECT                           =  836;  // <evaluate_subject>
		final int SYM_EVALUATE_SUBJECT_LIST                      =  837;  // <evaluate_subject_list>
		final int SYM_EVALUATE_WHEN_LIST                         =  838;  // <evaluate_when_list>
		final int SYM_EVENT_STATUS2                              =  839;  // <event_status>
		final int SYM_EXCEPTION_OR_ERROR                         =  840;  // <exception_or_error>
		final int SYM_EXIT_BODY                                  =  841;  // <exit_body>
		final int SYM_EXIT_PROGRAM_RETURNING                     =  842;  // <exit_program_returning>
		final int SYM_EXIT_STATEMENT                             =  843;  // <exit_statement>
		final int SYM_EXP2                                       =  844;  // <exp>
		final int SYM_EXPR                                       =  845;  // <expr>
		final int SYM_EXPR_TOKEN                                 =  846;  // <expr_token>
		final int SYM_EXPR_TOKENS                                =  847;  // <expr_tokens>
		final int SYM_EXPR_X                                     =  848;  // <expr_x>
		final int SYM_EXP_ATOM                                   =  849;  // <exp_atom>
		final int SYM_EXP_FACTOR                                 =  850;  // <exp_factor>
		final int SYM_EXP_LIST                                   =  851;  // <exp_list>
		final int SYM_EXP_TERM                                   =  852;  // <exp_term>
		final int SYM_EXP_UNARY                                  =  853;  // <exp_unary>
		final int SYM_EXTENDED_WITH_LOCK                         =  854;  // <extended_with_lock>
		final int SYM_EXTERNAL_CLAUSE                            =  855;  // <external_clause>
		final int SYM_FILE_CONTROL_ENTRY                         =  856;  // <file_control_entry>
		final int SYM_FILE_DESCRIPTION                           =  857;  // <file_description>
		final int SYM_FILE_DESCRIPTION_CLAUSE                    =  858;  // <file_description_clause>
		final int SYM_FILE_DESCRIPTION_ENTRY                     =  859;  // <file_description_entry>
		final int SYM_FILE_ID2                                   =  860;  // <file_id>
		final int SYM_FILE_NAME                                  =  861;  // <file_name>
		final int SYM_FILE_NAME_LIST                             =  862;  // <file_name_list>
		final int SYM_FILE_OR_RECORD_NAME                        =  863;  // <file_or_record_name>
		final int SYM_FILE_STATUS_CLAUSE                         =  864;  // <file_status_clause>
		final int SYM_FILE_TYPE                                  =  865;  // <file_type>
		final int SYM_FIRST_DETAIL                               =  866;  // <first_detail>
		final int SYM_FLAG_ALL                                   =  867;  // <flag_all>
		final int SYM_FLAG_DUPLICATES                            =  868;  // <flag_duplicates>
		final int SYM_FLAG_INITIALIZED                           =  869;  // <flag_initialized>
		final int SYM_FLAG_INITIALIZED_TO                        =  870;  // <flag_initialized_to>
		final int SYM_FLAG_OPTIONAL                              =  871;  // <flag_optional>
		final int SYM_FLAG_ROUNDED                               =  872;  // <flag_rounded>
		final int SYM_FLAG_SEPARATE                              =  873;  // <flag_separate>
		final int SYM_FLOAT_USAGE                                =  874;  // <float_usage>
		final int SYM_FOOTING_CLAUSE                             =  875;  // <footing_clause>
		final int SYM_FORMATTED_DATETIME_ARGS                    =  876;  // <formatted_datetime_args>
		final int SYM_FORMATTED_DATETIME_FUNC                    =  877;  // <FORMATTED_DATETIME_FUNC>
		final int SYM_FORMATTED_DATE_FUNC                        =  878;  // <FORMATTED_DATE_FUNC>
		final int SYM_FORMATTED_TIME_ARGS                        =  879;  // <formatted_time_args>
		final int SYM_FORMATTED_TIME_FUNC                        =  880;  // <FORMATTED_TIME_FUNC>
		final int SYM_FP128_USAGE                                =  881;  // <fp128_usage>
		final int SYM_FP32_USAGE                                 =  882;  // <fp32_usage>
		final int SYM_FP64_USAGE                                 =  883;  // <fp64_usage>
		final int SYM_FREE_BODY                                  =  884;  // <free_body>
		final int SYM_FREE_STATEMENT                             =  885;  // <free_statement>
		final int SYM_FROM_IDENTIFIER                            =  886;  // <from_identifier>
		final int SYM_FROM_OPTION                                =  887;  // <from_option>
		final int SYM_FROM_PARAMETER                             =  888;  // <from_parameter>
		final int SYM_FUNCTION2                                  =  889;  // <function>
		final int SYM_FUNCTION_DEFINITION                        =  890;  // <function_definition>
		final int SYM_FUNCTION_ID_PARAGRAPH                      =  891;  // <function_id_paragraph>
		final int SYM_FUNCTION_NAME                              =  892;  // <FUNCTION_NAME>
		final int SYM_FUNC_ARGS                                  =  893;  // <func_args>
		final int SYM_FUNC_MULTI_PARM                            =  894;  // <func_multi_parm>
		final int SYM_FUNC_NO_PARM                               =  895;  // <func_no_parm>
		final int SYM_FUNC_ONE_PARM                              =  896;  // <func_one_parm>
		final int SYM_FUNC_REFMOD                                =  897;  // <func_refmod>
		final int SYM_GE                                         =  898;  // <ge>
		final int SYM_GENERAL_DEVICE_NAME                        =  899;  // <general_device_name>
		final int SYM_GENERATE_BODY                              =  900;  // <generate_body>
		final int SYM_GENERATE_STATEMENT                         =  901;  // <generate_statement>
		final int SYM_GLOBAL_CLAUSE                              =  902;  // <global_clause>
		final int SYM_GOBACK_STATEMENT                           =  903;  // <goback_statement>
		final int SYM_GOTO_DEPENDING                             =  904;  // <goto_depending>
		final int SYM_GOTO_STATEMENT                             =  905;  // <goto_statement>
		final int SYM_GO_BODY                                    =  906;  // <go_body>
		final int SYM_GROUP_INDICATE_CLAUSE                      =  907;  // <group_indicate_clause>
		final int SYM_GT                                         =  908;  // <gt>
		final int SYM_HEADING_CLAUSE                             =  909;  // <heading_clause>
		final int SYM_IDENTIFICATION_OR_ID                       =  910;  // <identification_or_id>
		final int SYM_IDENTIFIER                                 =  911;  // <identifier>
		final int SYM_IDENTIFIER_1                               =  912;  // <identifier_1>
		final int SYM_IDENTIFIER_LIST                            =  913;  // <identifier_list>
		final int SYM_IDENTIFIER_OR_FILE_NAME                    =  914;  // <identifier_or_file_name>
		final int SYM_ID_OR_LIT                                  =  915;  // <id_or_lit>
		final int SYM_ID_OR_LIT_OR_FUNC                          =  916;  // <id_or_lit_or_func>
		final int SYM_ID_OR_LIT_OR_FUNC_AS                       =  917;  // <id_or_lit_or_func_as>
		final int SYM_ID_OR_LIT_OR_LENGTH_OR_FUNC                =  918;  // <id_or_lit_or_length_or_func>
		final int SYM_ID_OR_LIT_OR_PROGRAM_NAME                  =  919;  // <id_or_lit_or_program_name>
		final int SYM_IF_ELSE_STATEMENTS                         =  920;  // <if_else_statements>
		final int SYM_IF_STATEMENT                               =  921;  // <if_statement>
		final int SYM_IGNORING_LOCK                              =  922;  // <ignoring_lock>
		final int SYM_INITIALIZE_BODY                            =  923;  // <initialize_body>
		final int SYM_INITIALIZE_CATEGORY                        =  924;  // <initialize_category>
		final int SYM_INITIALIZE_REPLACING_ITEM                  =  925;  // <initialize_replacing_item>
		final int SYM_INITIALIZE_REPLACING_LIST                  =  926;  // <initialize_replacing_list>
		final int SYM_INITIALIZE_STATEMENT                       =  927;  // <initialize_statement>
		final int SYM_INITIATE_BODY                              =  928;  // <initiate_body>
		final int SYM_INITIATE_STATEMENT                         =  929;  // <initiate_statement>
		final int SYM_INIT_OR_RECURSE                            =  930;  // <init_or_recurse>
		final int SYM_INIT_OR_RECURSE_AND_COMMON                 =  931;  // <init_or_recurse_and_common>
		final int SYM_INSPECT_AFTER                              =  932;  // <inspect_after>
		final int SYM_INSPECT_BEFORE                             =  933;  // <inspect_before>
		final int SYM_INSPECT_BODY                               =  934;  // <inspect_body>
		final int SYM_INSPECT_CONVERTING                         =  935;  // <inspect_converting>
		final int SYM_INSPECT_LIST                               =  936;  // <inspect_list>
		final int SYM_INSPECT_REGION                             =  937;  // <inspect_region>
		final int SYM_INSPECT_REPLACING                          =  938;  // <inspect_replacing>
		final int SYM_INSPECT_STATEMENT                          =  939;  // <inspect_statement>
		final int SYM_INSPECT_TALLYING                           =  940;  // <inspect_tallying>
		final int SYM_INTEGER2                                   =  941;  // <integer>
		final int SYM_INTEGER_LABEL                              =  942;  // <integer_label>
		final int SYM_INTEGER_LIST                               =  943;  // <integer_list>
		final int SYM_INTEGER_OR_WORD                            =  944;  // <integer_or_word>
		final int SYM_INTERMEDIATE_ROUNDING_CHOICE               =  945;  // <intermediate_rounding_choice>
		final int SYM_INTLITERALORWORD                           =  946;  // <IntLiteral or WORD>
		final int SYM_INVALID_KEY_PHRASES                        =  947;  // <invalid_key_phrases>
		final int SYM_INVALID_KEY_SENTENCE                       =  948;  // <invalid_key_sentence>
		final int SYM_IN_OF                                      =  949;  // <in_of>
		final int SYM_I_O_CONTROL_CLAUSE                         =  950;  // <i_o_control_clause>
		final int SYM_I_O_CONTROL_LIST                           =  951;  // <i_o_control_list>
		final int SYM_JUSTIFIED_CLAUSE                           =  952;  // <justified_clause>
		final int SYM_KEY_OR_SPLIT_KEYS                          =  953;  // <key_or_split_keys>
		final int SYM_LABEL2                                     =  954;  // <label>
		final int SYM_LABEL_OPTION                               =  955;  // <label_option>
		final int SYM_LABEL_RECORDS_CLAUSE                       =  956;  // <label_records_clause>
		final int SYM_LAST_DETAIL                                =  957;  // <last_detail>
		final int SYM_LAST_HEADING                               =  958;  // <last_heading>
		final int SYM_LE                                         =  959;  // <le>
		final int SYM_LENGTH_ARG                                 =  960;  // <length_arg>
		final int SYM_LENGTH_FUNC                                =  961;  // <LENGTH_FUNC>
		final int SYM_LEVEL_NUMBER                               =  962;  // <level_number>
		final int SYM_LINAGE_BOTTOM                              =  963;  // <linage_bottom>
		final int SYM_LINAGE_CLAUSE                              =  964;  // <linage_clause>
		final int SYM_LINAGE_FOOTING                             =  965;  // <linage_footing>
		final int SYM_LINAGE_LINES                               =  966;  // <linage_lines>
		final int SYM_LINAGE_TOP                                 =  967;  // <linage_top>
		final int SYM_LINES_OR_NUMBER                            =  968;  // <lines_or_number>
		final int SYM_LINE_CLAUSE                                =  969;  // <line_clause>
		final int SYM_LINE_KEYWORD_CLAUSE                        =  970;  // <line_keyword_clause>
		final int SYM_LINE_LINAGE_PAGE_COUNTER                   =  971;  // <line_linage_page_counter>
		final int SYM_LINE_NUMBER                                =  972;  // <line_number>
		final int SYM_LINE_OR_LINES                              =  973;  // <line_or_lines>
		final int SYM_LINE_OR_PLUS                               =  974;  // <line_or_plus>
		final int SYM_LINE_SEQ_DEVICE_NAME                       =  975;  // <line_seq_device_name>
		final int SYM_LITERAL                                    =  976;  // <literal>
		final int SYM_LITERAL_TOK                                =  977;  // <LITERAL_TOK>
		final int SYM_LIT_OR_LENGTH                              =  978;  // <lit_or_length>
		final int SYM_LOCALE_CLASS                               =  979;  // <locale_class>
		final int SYM_LOCALE_CLAUSE                              =  980;  // <locale_clause>
		final int SYM_LOCALE_DATE_FUNC                           =  981;  // <LOCALE_DATE_FUNC>
		final int SYM_LOCALE_DT_ARGS                             =  982;  // <locale_dt_args>
		final int SYM_LOCALE_TIME_FROM_FUNC                      =  983;  // <LOCALE_TIME_FROM_FUNC>
		final int SYM_LOCALE_TIME_FUNC                           =  984;  // <LOCALE_TIME_FUNC>
		final int SYM_LOCK_MODE                                  =  985;  // <lock_mode>
		final int SYM_LOCK_MODE_CLAUSE                           =  986;  // <lock_mode_clause>
		final int SYM_LOCK_PHRASES                               =  987;  // <lock_phrases>
		final int SYM_LOCK_RECORDS                               =  988;  // <lock_records>
		final int SYM_LOWER_CASE_FUNC                            =  989;  // <LOWER_CASE_FUNC>
		final int SYM_LT                                         =  990;  // <lt>
		final int SYM_MERGE_STATEMENT                            =  991;  // <merge_statement>
		final int SYM_MESSAGE_OR_SEGMENT                         =  992;  // <message_or_segment>
		final int SYM_MINUS_MINUS                                =  993;  // <minus_minus>
		final int SYM_MNEMONIC_CHOICES                           =  994;  // <mnemonic_choices>
		final int SYM_MNEMONIC_NAME2                             =  995;  // <mnemonic_name>
		final int SYM_MNEMONIC_NAME_CLAUSE                       =  996;  // <mnemonic_name_clause>
		final int SYM_MNEMONIC_NAME_LIST                         =  997;  // <mnemonic_name_list>
		final int SYM_MNEMONIC_NAME_TOK                          =  998;  // <MNEMONIC_NAME_TOK>
		final int SYM_MODE_IS_BLOCK                              =  999;  // <mode_is_block>
		final int SYM_MOVE_BODY                                  = 1000;  // <move_body>
		final int SYM_MOVE_STATEMENT                             = 1001;  // <move_statement>
		final int SYM_MULTIPLE_FILE                              = 1002;  // <multiple_file>
		final int SYM_MULTIPLE_FILE_LIST                         = 1003;  // <multiple_file_list>
		final int SYM_MULTIPLE_FILE_TAPE_CLAUSE                  = 1004;  // <multiple_file_tape_clause>
		final int SYM_MULTIPLY_BODY                              = 1005;  // <multiply_body>
		final int SYM_MULTIPLY_STATEMENT                         = 1006;  // <multiply_statement>
		final int SYM_NAMED_INPUT_CD_CLAUSE                      = 1007;  // <named_input_cd_clause>
		final int SYM_NAMED_INPUT_CD_CLAUSES                     = 1008;  // <named_input_cd_clauses>
		final int SYM_NAMED_I_O_CD_CLAUSE                        = 1009;  // <named_i_o_cd_clause>
		final int SYM_NAMED_I_O_CD_CLAUSES                       = 1010;  // <named_i_o_cd_clauses>
		final int SYM_NATIONAL_OF_FUNC                           = 1011;  // <NATIONAL_OF_FUNC>
		final int SYM_NESTED_LIST                                = 1012;  // <nested_list>
		final int SYM_NEXT_GROUP_CLAUSE                          = 1013;  // <next_group_clause>
		final int SYM_NOISE                                      = 1014;  // <Noise>
		final int SYM_NOISELIST                                  = 1015;  // <NoiseList>
		final int SYM_NOT2                                       = 1016;  // <not>
		final int SYM_NOT_AT_END_CLAUSE                          = 1017;  // <not_at_end_clause>
		final int SYM_NOT_AT_EOP_CLAUSE                          = 1018;  // <not_at_eop_clause>
		final int SYM_NOT_EQUAL_OP                               = 1019;  // <not_equal_op>
		final int SYM_NOT_ESCAPE_OR_NOT_EXCEPTION                = 1020;  // <not_escape_or_not_exception>
		final int SYM_NOT_INVALID_KEY_SENTENCE                   = 1021;  // <not_invalid_key_sentence>
		final int SYM_NOT_ON_OVERFLOW                            = 1022;  // <not_on_overflow>
		final int SYM_NOT_ON_SIZE_ERROR                          = 1023;  // <not_on_size_error>
		final int SYM_NO_DATA_SENTENCE                           = 1024;  // <no_data_sentence>
		final int SYM_NO_ECHO2                                   = 1025;  // <no_echo>
		final int SYM_NO_OR_INTEGER                              = 1026;  // <no_or_integer>
		final int SYM_NULL_OR_OMITTED                            = 1027;  // <null_or_omitted>
		final int SYM_NUMERIC_IDENTIFIER                         = 1028;  // <numeric_identifier>
		final int SYM_NUMERIC_SIGN_CLAUSE                        = 1029;  // <numeric_sign_clause>
		final int SYM_NUMVALC_ARGS                               = 1030;  // <numvalc_args>
		final int SYM_NUMVALC_FUNC                               = 1031;  // <NUMVALC_FUNC>
		final int SYM_NUM_ID_OR_LIT                              = 1032;  // <num_id_or_lit>
		final int SYM_OBJECT_CHAR_OR_WORD                        = 1033;  // <object_char_or_word>
		final int SYM_OBJECT_CLAUSES                             = 1034;  // <object_clauses>
		final int SYM_OBJECT_CLAUSES_LIST                        = 1035;  // <object_clauses_list>
		final int SYM_OBJECT_COMPUTER_CLASS                      = 1036;  // <object_computer_class>
		final int SYM_OBJECT_COMPUTER_MEMORY                     = 1037;  // <object_computer_memory>
		final int SYM_OBJECT_COMPUTER_PARAGRAPH                  = 1038;  // <object_computer_paragraph>
		final int SYM_OBJECT_COMPUTER_SEGMENT                    = 1039;  // <object_computer_segment>
		final int SYM_OBJECT_COMPUTER_SEQUENCE                   = 1040;  // <object_computer_sequence>
		final int SYM_OCCURS_CLAUSE                              = 1041;  // <occurs_clause>
		final int SYM_OCCURS_INDEX                               = 1042;  // <occurs_index>
		final int SYM_OCCURS_INDEXED                             = 1043;  // <occurs_indexed>
		final int SYM_OCCURS_INDEX_LIST                          = 1044;  // <occurs_index_list>
		final int SYM_OCCURS_KEYS                                = 1045;  // <occurs_keys>
		final int SYM_OCCURS_KEY_FIELD                           = 1046;  // <occurs_key_field>
		final int SYM_OCCURS_KEY_LIST                            = 1047;  // <occurs_key_list>
		final int SYM_ON_OFF_CLAUSES                             = 1048;  // <on_off_clauses>
		final int SYM_ON_OFF_CLAUSES_1                           = 1049;  // <on_off_clauses_1>
		final int SYM_ON_OR_OFF                                  = 1050;  // <on_or_off>
		final int SYM_ON_OVERFLOW                                = 1051;  // <on_overflow>
		final int SYM_ON_SIZE_ERROR                              = 1052;  // <on_size_error>
		final int SYM_ON_SIZE_ERROR_PHRASES                      = 1053;  // <on_size_error_phrases>
		final int SYM_OPEN_BODY                                  = 1054;  // <open_body>
		final int SYM_OPEN_FILE_ENTRY                            = 1055;  // <open_file_entry>
		final int SYM_OPEN_MODE                                  = 1056;  // <open_mode>
		final int SYM_OPEN_OPTION                                = 1057;  // <open_option>
		final int SYM_OPEN_SHARING                               = 1058;  // <open_sharing>
		final int SYM_OPEN_STATEMENT                             = 1059;  // <open_statement>
		final int SYM_OPTIONAL_REFERENCE                         = 1060;  // <optional_reference>
		final int SYM_OPTIONAL_REFERENCE_LIST                    = 1061;  // <optional_reference_list>
		final int SYM_ORGANIZATION2                              = 1062;  // <organization>
		final int SYM_ORGANIZATION_CLAUSE                        = 1063;  // <organization_clause>
		final int SYM_OUTPUT_CD_CLAUSE                           = 1064;  // <output_cd_clause>
		final int SYM_OUTPUT_CD_CLAUSES                          = 1065;  // <output_cd_clauses>
		final int SYM_PADDING_CHARACTER_CLAUSE                   = 1066;  // <padding_character_clause>
		final int SYM_PAGE_DETAIL                                = 1067;  // <page_detail>
		final int SYM_PAGE_LIMIT_CLAUSE                          = 1068;  // <page_limit_clause>
		final int SYM_PAGE_LINE_COLUMN                           = 1069;  // <page_line_column>
		final int SYM_PARAGRAPH_HEADER                           = 1070;  // <paragraph_header>
		final int SYM_PARTIAL_EXPR                               = 1071;  // <partial_expr>
		final int SYM_PERFORM_BODY                               = 1072;  // <perform_body>
		final int SYM_PERFORM_OPTION                             = 1073;  // <perform_option>
		final int SYM_PERFORM_PROCEDURE                          = 1074;  // <perform_procedure>
		final int SYM_PERFORM_STATEMENT                          = 1075;  // <perform_statement>
		final int SYM_PERFORM_TEST                               = 1076;  // <perform_test>
		final int SYM_PERFORM_VARYING                            = 1077;  // <perform_varying>
		final int SYM_PERFORM_VARYING_LIST                       = 1078;  // <perform_varying_list>
		final int SYM_PF_KEYWORD                                 = 1079;  // <pf_keyword>
		final int SYM_PH_KEYWORD                                 = 1080;  // <ph_keyword>
		final int SYM_PICTURE_CLAUSE                             = 1081;  // <picture_clause>
		final int SYM_PLUS_PLUS                                  = 1082;  // <plus_plus>
		final int SYM_POINTER_LEN                                = 1083;  // <pointer_len>
		final int SYM_POSITIVE_ID_OR_LIT                         = 1084;  // <positive_id_or_lit>
		final int SYM_POS_NUM_ID_OR_LIT                          = 1085;  // <pos_num_id_or_lit>
		final int SYM_PRESENT_WHEN_CONDITION                     = 1086;  // <present_when_condition>
		final int SYM_PRINTER_NAME                               = 1087;  // <printer_name>
		final int SYM_PROCEDURE2                                 = 1088;  // <procedure>
		final int SYM_PROCEDURE_NAME                             = 1089;  // <procedure_name>
		final int SYM_PROCEDURE_NAME_LIST                        = 1090;  // <procedure_name_list>
		final int SYM_PROCEDURE_PARAM                            = 1091;  // <procedure_param>
		final int SYM_PROCEDURE_PARAM_LIST                       = 1092;  // <procedure_param_list>
		final int SYM_PROGRAM_DEFINITION                         = 1093;  // <program_definition>
		final int SYM_PROGRAM_ID_NAME                            = 1094;  // <program_id_name>
		final int SYM_PROGRAM_ID_PARAGRAPH                       = 1095;  // <program_id_paragraph>
		final int SYM_PROGRAM_NAME                               = 1096;  // <PROGRAM_NAME>
		final int SYM_PROGRAM_OR_PROTOTYPE                       = 1097;  // <program_or_prototype>
		final int SYM_PROGRAM_START_END                          = 1098;  // <program_start_end>
		final int SYM_PROGRAM_TYPE_CLAUSE                        = 1099;  // <program_type_clause>
		final int SYM_PROG_COLL_SEQUENCE                         = 1100;  // <prog_coll_sequence>
		final int SYM_PROG_OR_ENTRY                              = 1101;  // <prog_or_entry>
		final int SYM_PURGE_STATEMENT                            = 1102;  // <purge_statement>
		final int SYM_QUALIFIED_WORD                             = 1103;  // <qualified_word>
		final int SYM_READY_STATEMENT                            = 1104;  // <ready_statement>
		final int SYM_READ_BODY                                  = 1105;  // <read_body>
		final int SYM_READ_HANDLER                               = 1106;  // <read_handler>
		final int SYM_READ_INTO                                  = 1107;  // <read_into>
		final int SYM_READ_KEY                                   = 1108;  // <read_key>
		final int SYM_READ_STATEMENT                             = 1109;  // <read_statement>
		final int SYM_RECEIVE_BODY                               = 1110;  // <receive_body>
		final int SYM_RECEIVE_STATEMENT                          = 1111;  // <receive_statement>
		final int SYM_RECORDING_MODE                             = 1112;  // <recording_mode>
		final int SYM_RECORDING_MODE_CLAUSE                      = 1113;  // <recording_mode_clause>
		final int SYM_RECORDS2                                   = 1114;  // <records>
		final int SYM_RECORD_CLAUSE                              = 1115;  // <record_clause>
		final int SYM_RECORD_DELIMITER_CLAUSE                    = 1116;  // <record_delimiter_clause>
		final int SYM_RECORD_DESCRIPTION_LIST                    = 1117;  // <record_description_list>
		final int SYM_RECORD_KEY_CLAUSE                          = 1118;  // <record_key_clause>
		final int SYM_RECORD_NAME                                = 1119;  // <record_name>
		final int SYM_REDEFINES_CLAUSE                           = 1120;  // <redefines_clause>
		final int SYM_REEL_OR_UNIT                               = 1121;  // <reel_or_unit>
		final int SYM_REFERENCE2                                 = 1122;  // <reference>
		final int SYM_REFERENCE_LIST                             = 1123;  // <reference_list>
		final int SYM_REFERENCE_OR_LITERAL                       = 1124;  // <reference_or_literal>
		final int SYM_REFMOD                                     = 1125;  // <refmod>
		final int SYM_RELATIVE_KEY_CLAUSE                        = 1126;  // <relative_key_clause>
		final int SYM_RELEASE_BODY                               = 1127;  // <release_body>
		final int SYM_RELEASE_STATEMENT                          = 1128;  // <release_statement>
		final int SYM_RENAMES_ENTRY                              = 1129;  // <renames_entry>
		final int SYM_REPLACING_ITEM                             = 1130;  // <replacing_item>
		final int SYM_REPLACING_LIST                             = 1131;  // <replacing_list>
		final int SYM_REPLACING_REGION                           = 1132;  // <replacing_region>
		final int SYM_REPORT_CLAUSE                              = 1133;  // <report_clause>
		final int SYM_REPORT_COL_INTEGER_LIST                    = 1134;  // <report_col_integer_list>
		final int SYM_REPORT_DESCRIPTION                         = 1135;  // <report_description>
		final int SYM_REPORT_DESCRIPTION_OPTION                  = 1136;  // <report_description_option>
		final int SYM_REPORT_GROUP_DESCRIPTION_ENTRY             = 1137;  // <report_group_description_entry>
		final int SYM_REPORT_GROUP_OPTION                        = 1138;  // <report_group_option>
		final int SYM_REPORT_INTEGER                             = 1139;  // <report_integer>
		final int SYM_REPORT_KEYWORD                             = 1140;  // <report_keyword>
		final int SYM_REPORT_LINE_INTEGER_LIST                   = 1141;  // <report_line_integer_list>
		final int SYM_REPORT_NAME                                = 1142;  // <report_name>
		final int SYM_REPORT_OCCURS_CLAUSE                       = 1143;  // <report_occurs_clause>
		final int SYM_REPORT_USAGE_CLAUSE                        = 1144;  // <report_usage_clause>
		final int SYM_REPORT_X_LIST                              = 1145;  // <report_x_list>
		final int SYM_REPOSITORY_LIST                            = 1146;  // <repository_list>
		final int SYM_REPOSITORY_NAME                            = 1147;  // <repository_name>
		final int SYM_REPOSITORY_NAME_LIST                       = 1148;  // <repository_name_list>
		final int SYM_REP_KEYWORD                                = 1149;  // <rep_keyword>
		final int SYM_REP_NAME_LIST                              = 1150;  // <rep_name_list>
		final int SYM_RESERVE_CLAUSE                             = 1151;  // <reserve_clause>
		final int SYM_RESET_STATEMENT                            = 1152;  // <reset_statement>
		final int SYM_RETRY_OPTIONS                              = 1153;  // <retry_options>
		final int SYM_RETRY_PHRASE                               = 1154;  // <retry_phrase>
		final int SYM_RETURN_AT_END                              = 1155;  // <return_at_end>
		final int SYM_RETURN_BODY                                = 1156;  // <return_body>
		final int SYM_RETURN_GIVE                                = 1157;  // <return_give>
		final int SYM_RETURN_STATEMENT                           = 1158;  // <return_statement>
		final int SYM_REVERSE_FUNC                               = 1159;  // <REVERSE_FUNC>
		final int SYM_REVERSE_VIDEO2                             = 1160;  // <reverse_video>
		final int SYM_REWRITE_BODY                               = 1161;  // <rewrite_body>
		final int SYM_REWRITE_STATEMENT                          = 1162;  // <rewrite_statement>
		final int SYM_RF_KEYWORD                                 = 1163;  // <rf_keyword>
		final int SYM_RH_KEYWORD                                 = 1164;  // <rh_keyword>
		final int SYM_ROLLBACK_STATEMENT                         = 1165;  // <rollback_statement>
		final int SYM_ROUND_CHOICE                               = 1166;  // <round_choice>
		final int SYM_ROUND_MODE                                 = 1167;  // <round_mode>
		final int SYM_SAME_CLAUSE                                = 1168;  // <same_clause>
		final int SYM_SCOPE_TERMINATOR                           = 1169;  // <scope_terminator>
		final int SYM_SCREEN_COL_NUMBER                          = 1170;  // <screen_col_number>
		final int SYM_SCREEN_CONTROL2                            = 1171;  // <screen_control>
		final int SYM_SCREEN_DESCRIPTION                         = 1172;  // <screen_description>
		final int SYM_SCREEN_DESCRIPTION_LIST                    = 1173;  // <screen_description_list>
		final int SYM_SCREEN_GLOBAL_CLAUSE                       = 1174;  // <screen_global_clause>
		final int SYM_SCREEN_LINE_NUMBER                         = 1175;  // <screen_line_number>
		final int SYM_SCREEN_OCCURS_CLAUSE                       = 1176;  // <screen_occurs_clause>
		final int SYM_SCREEN_OPTION                              = 1177;  // <screen_option>
		final int SYM_SCREEN_OR_DEVICE_DISPLAY                   = 1178;  // <screen_or_device_display>
		final int SYM_SCROLL_LINE_OR_LINES                       = 1179;  // <scroll_line_or_lines>
		final int SYM_SEARCH_AT_END                              = 1180;  // <search_at_end>
		final int SYM_SEARCH_BODY                                = 1181;  // <search_body>
		final int SYM_SEARCH_STATEMENT                           = 1182;  // <search_statement>
		final int SYM_SEARCH_VARYING                             = 1183;  // <search_varying>
		final int SYM_SEARCH_WHEN                                = 1184;  // <search_when>
		final int SYM_SEARCH_WHENS                               = 1185;  // <search_whens>
		final int SYM_SECTION_HEADER                             = 1186;  // <section_header>
		final int SYM_SELECT_CLAUSE                              = 1187;  // <select_clause>
		final int SYM_SEND_BODY                                  = 1188;  // <send_body>
		final int SYM_SEND_IDENTIFIER                            = 1189;  // <send_identifier>
		final int SYM_SEND_STATEMENT                             = 1190;  // <send_statement>
		final int SYM_SET_ATTR                                   = 1191;  // <set_attr>
		final int SYM_SET_ATTR_CLAUSE                            = 1192;  // <set_attr_clause>
		final int SYM_SET_ATTR_ONE                               = 1193;  // <set_attr_one>
		final int SYM_SET_BODY                                   = 1194;  // <set_body>
		final int SYM_SET_ENVIRONMENT                            = 1195;  // <set_environment>
		final int SYM_SET_LAST_EXCEPTION_TO_OFF                  = 1196;  // <set_last_exception_to_off>
		final int SYM_SET_STATEMENT                              = 1197;  // <set_statement>
		final int SYM_SET_TO                                     = 1198;  // <set_to>
		final int SYM_SET_TO_ON_OFF                              = 1199;  // <set_to_on_off>
		final int SYM_SET_TO_ON_OFF_SEQUENCE                     = 1200;  // <set_to_on_off_sequence>
		final int SYM_SET_TO_TRUE_FALSE                          = 1201;  // <set_to_true_false>
		final int SYM_SET_TO_TRUE_FALSE_SEQUENCE                 = 1202;  // <set_to_true_false_sequence>
		final int SYM_SET_UP_DOWN                                = 1203;  // <set_up_down>
		final int SYM_SHARING_CLAUSE                             = 1204;  // <sharing_clause>
		final int SYM_SHARING_OPTION                             = 1205;  // <sharing_option>
		final int SYM_SIGN_CLAUSE                                = 1206;  // <sign_clause>
		final int SYM_SIMPLE_ALL_VALUE                           = 1207;  // <simple_all_value>
		final int SYM_SIMPLE_DISPLAY_ALL_VALUE                   = 1208;  // <simple_display_all_value>
		final int SYM_SIMPLE_DISPLAY_VALUE                       = 1209;  // <simple_display_value>
		final int SYM_SIMPLE_PROG                                = 1210;  // <simple_prog>
		final int SYM_SIMPLE_VALUE                               = 1211;  // <simple_value>
		final int SYM_SINGLE_REFERENCE                           = 1212;  // <single_reference>
		final int SYM_SIZELEN_CLAUSE                             = 1213;  // <sizelen_clause>
		final int SYM_SIZE_IS_INTEGER                            = 1214;  // <size_is_integer>
		final int SYM_SIZE_OR_LENGTH                             = 1215;  // <size_or_length>
		final int SYM_SORT_BODY                                  = 1216;  // <sort_body>
		final int SYM_SORT_COLLATING                             = 1217;  // <sort_collating>
		final int SYM_SORT_INPUT                                 = 1218;  // <sort_input>
		final int SYM_SORT_KEY_LIST                              = 1219;  // <sort_key_list>
		final int SYM_SORT_OUTPUT                                = 1220;  // <sort_output>
		final int SYM_SORT_STATEMENT                             = 1221;  // <sort_statement>
		final int SYM_SOURCE_CLAUSE                              = 1222;  // <source_clause>
		final int SYM_SOURCE_COMPUTER_PARAGRAPH                  = 1223;  // <source_computer_paragraph>
		final int SYM_SOURCE_ELEMENT                             = 1224;  // <source_element>
		final int SYM_SOURCE_ELEMENT_LIST                        = 1225;  // <source_element_list>
		final int SYM_SPACE_OR_ZERO                              = 1226;  // <space_or_zero>
		final int SYM_SPECIAL_NAME                               = 1227;  // <special_name>
		final int SYM_SPECIAL_NAMES_SENTENCE_LIST                = 1228;  // <special_names_sentence_list>
		final int SYM_SPECIAL_NAME_LIST                          = 1229;  // <special_name_list>
		final int SYM_START2                                     = 1230;  // <start>
		final int SYM_START_BODY                                 = 1231;  // <start_body>
		final int SYM_START_KEY                                  = 1232;  // <start_key>
		final int SYM_START_OP                                   = 1233;  // <start_op>
		final int SYM_START_STATEMENT                            = 1234;  // <start_statement>
		final int SYM_STATEMENT                                  = 1235;  // <statement>
		final int SYM_STATEMENTS                                 = 1236;  // <statements>
		final int SYM_STATEMENT_LIST                             = 1237;  // <statement_list>
		final int SYM_STOP_LITERAL                               = 1238;  // <stop_literal>
		final int SYM_STOP_RETURNING                             = 1239;  // <stop_returning>
		final int SYM_STOP_STATEMENT                             = 1240;  // <stop_statement>
		final int SYM_STRING_BODY                                = 1241;  // <string_body>
		final int SYM_STRING_DELIMITER                           = 1242;  // <string_delimiter>
		final int SYM_STRING_ITEM                                = 1243;  // <string_item>
		final int SYM_STRING_ITEM_LIST                           = 1244;  // <string_item_list>
		final int SYM_STRING_STATEMENT                           = 1245;  // <string_statement>
		final int SYM_SUBREF                                     = 1246;  // <subref>
		final int SYM_SUBSTITUTE_CASE_FUNC                       = 1247;  // <SUBSTITUTE_CASE_FUNC>
		final int SYM_SUBSTITUTE_FUNC                            = 1248;  // <SUBSTITUTE_FUNC>
		final int SYM_SUBTRACT_BODY                              = 1249;  // <subtract_body>
		final int SYM_SUBTRACT_STATEMENT                         = 1250;  // <subtract_statement>
		final int SYM_SUB_IDENTIFIER                             = 1251;  // <sub_identifier>
		final int SYM_SUB_IDENTIFIER_1                           = 1252;  // <sub_identifier_1>
		final int SYM_SUM_CLAUSE_LIST                            = 1253;  // <sum_clause_list>
		final int SYM_SUPPRESS_STATEMENT                         = 1254;  // <suppress_statement>
		final int SYM_SYMBOLIC_CHARACTERS_CLAUSE                 = 1255;  // <symbolic_characters_clause>
		final int SYM_SYMBOLIC_CHARS_LIST                        = 1256;  // <symbolic_chars_list>
		final int SYM_SYMBOLIC_CHARS_PHRASE                      = 1257;  // <symbolic_chars_phrase>
		final int SYM_SYMBOLIC_COLLECTION                        = 1258;  // <symbolic_collection>
		final int SYM_SYMBOLIC_INTEGER                           = 1259;  // <symbolic_integer>
		final int SYM_SYNCHRONIZED_CLAUSE                        = 1260;  // <synchronized_clause>
		final int SYM_TABLE_IDENTIFIER                           = 1261;  // <table_identifier>
		final int SYM_TABLE_NAME                                 = 1262;  // <table_name>
		final int SYM_TALLYING_ITEM                              = 1263;  // <tallying_item>
		final int SYM_TALLYING_LIST                              = 1264;  // <tallying_list>
		final int SYM_TARGET_IDENTIFIER                          = 1265;  // <target_identifier>
		final int SYM_TARGET_IDENTIFIER_1                        = 1266;  // <target_identifier_1>
		final int SYM_TARGET_X                                   = 1267;  // <target_x>
		final int SYM_TARGET_X_LIST                              = 1268;  // <target_x_list>
		final int SYM_TERMINATE_BODY                             = 1269;  // <terminate_body>
		final int SYM_TERMINATE_STATEMENT                        = 1270;  // <terminate_statement>
		final int SYM_TERM_OR_DOT                                = 1271;  // <term_or_dot>
		final int SYM_TO_INIT_VAL                                = 1272;  // <to_init_val>
		final int SYM_TRANSFORM_BODY                             = 1273;  // <transform_body>
		final int SYM_TRANSFORM_STATEMENT                        = 1274;  // <transform_statement>
		final int SYM_TRIM_ARGS                                  = 1275;  // <trim_args>
		final int SYM_TRIM_FUNC                                  = 1276;  // <TRIM_FUNC>
		final int SYM_TYPE_CLAUSE                                = 1277;  // <type_clause>
		final int SYM_TYPE_OPTION                                = 1278;  // <type_option>
		final int SYM_UNDEFINED_WORD                             = 1279;  // <undefined_word>
		final int SYM_UNIQUE_WORD                                = 1280;  // <unique_word>
		final int SYM_UNLOCK_BODY                                = 1281;  // <unlock_body>
		final int SYM_UNLOCK_STATEMENT                           = 1282;  // <unlock_statement>
		final int SYM_UNNAMED_INPUT_CD_CLAUSES                   = 1283;  // <unnamed_input_cd_clauses>
		final int SYM_UNNAMED_I_O_CD_CLAUSES                     = 1284;  // <unnamed_i_o_cd_clauses>
		final int SYM_UNSTRING_BODY                              = 1285;  // <unstring_body>
		final int SYM_UNSTRING_DELIMITED_ITEM                    = 1286;  // <unstring_delimited_item>
		final int SYM_UNSTRING_DELIMITED_LIST                    = 1287;  // <unstring_delimited_list>
		final int SYM_UNSTRING_INTO                              = 1288;  // <unstring_into>
		final int SYM_UNSTRING_INTO_ITEM                         = 1289;  // <unstring_into_item>
		final int SYM_UNSTRING_STATEMENT                         = 1290;  // <unstring_statement>
		final int SYM_UPDATE_DEFAULT                             = 1291;  // <update_default>
		final int SYM_UPPER_CASE_FUNC                            = 1292;  // <UPPER_CASE_FUNC>
		final int SYM_UP_OR_DOWN                                 = 1293;  // <up_or_down>
		final int SYM_USAGE2                                     = 1294;  // <usage>
		final int SYM_USAGE_CLAUSE                               = 1295;  // <usage_clause>
		final int SYM_USER_ENTRY_NAME                            = 1296;  // <user_entry_name>
		final int SYM_USER_FUNCTION_NAME                         = 1297;  // <USER_FUNCTION_NAME>
		final int SYM_USE_DEBUGGING                              = 1298;  // <use_debugging>
		final int SYM_USE_EXCEPTION                              = 1299;  // <use_exception>
		final int SYM_USE_EX_KEYW                                = 1300;  // <use_ex_keyw>
		final int SYM_USE_FILE_EXCEPTION                         = 1301;  // <use_file_exception>
		final int SYM_USE_FILE_EXCEPTION_TARGET                  = 1302;  // <use_file_exception_target>
		final int SYM_USE_GLOBAL                                 = 1303;  // <use_global>
		final int SYM_USE_PHRASE                                 = 1304;  // <use_phrase>
		final int SYM_USE_REPORTING                              = 1305;  // <use_reporting>
		final int SYM_USE_START_END                              = 1306;  // <use_start_end>
		final int SYM_USE_STATEMENT                              = 1307;  // <use_statement>
		final int SYM_U_OR_S                                     = 1308;  // <u_or_s>
		final int SYM_VALUEOF_NAME                               = 1309;  // <valueof_name>
		final int SYM_VALUE_CLAUSE                               = 1310;  // <value_clause>
		final int SYM_VALUE_ITEM                                 = 1311;  // <value_item>
		final int SYM_VALUE_ITEM_LIST                            = 1312;  // <value_item_list>
		final int SYM_VALUE_OF_CLAUSE                            = 1313;  // <value_of_clause>
		final int SYM_VARYING_CLAUSE                             = 1314;  // <varying_clause>
		final int SYM_VERB                                       = 1315;  // <verb>
		final int SYM_WHEN_COMPILED_FUNC                         = 1316;  // <WHEN_COMPILED_FUNC>
		final int SYM_WITH_DATA_SENTENCE                         = 1317;  // <with_data_sentence>
		final int SYM_WITH_DUPS                                  = 1318;  // <with_dups>
		final int SYM_WITH_INDICATOR                             = 1319;  // <with_indicator>
		final int SYM_WITH_LOCK                                  = 1320;  // <with_lock>
		final int SYM_WORD                                       = 1321;  // <WORD>
		final int SYM_WRITE_BODY                                 = 1322;  // <write_body>
		final int SYM_WRITE_HANDLER                              = 1323;  // <write_handler>
		final int SYM_WRITE_OPTION                               = 1324;  // <write_option>
		final int SYM_WRITE_STATEMENT                            = 1325;  // <write_statement>
		final int SYM_X                                          = 1326;  // <x>
		final int SYM_X_COMMON                                   = 1327;  // <x_common>
		final int SYM_X_LIST                                     = 1328;  // <x_list>
		final int SYM__ACCEPT_CLAUSES                            = 1329;  // <_accept_clauses>
		final int SYM__ACCEPT_EXCEPTION_PHRASES                  = 1330;  // <_accept_exception_phrases>
		final int SYM__ACCP_NOT_ON_EXCEPTION                     = 1331;  // <_accp_not_on_exception>
		final int SYM__ACCP_ON_EXCEPTION                         = 1332;  // <_accp_on_exception>
		final int SYM__ADD_TO                                    = 1333;  // <_add_to>
		final int SYM__ADVANCING                                 = 1334;  // <_advancing>
		final int SYM__AFTER                                     = 1335;  // <_after>
		final int SYM__ALL_REFS                                  = 1336;  // <_all_refs>
		final int SYM__ARE                                       = 1337;  // <_are>
		final int SYM__AREA                                      = 1338;  // <_area>
		final int SYM__AREAS                                     = 1339;  // <_areas>
		final int SYM__AS                                        = 1340;  // <_as>
		final int SYM__ASSIGNMENT_NAME                           = 1341;  // <_assignment_name>
		final int SYM__AS_EXTNAME                                = 1342;  // <_as_extname>
		final int SYM__AS_LITERAL                                = 1343;  // <_as_literal>
		final int SYM__AT                                        = 1344;  // <_at>
		final int SYM__AT_END_CLAUSE                             = 1345;  // <_at_end_clause>
		final int SYM__AT_EOP_CLAUSE                             = 1346;  // <_at_eop_clause>
		final int SYM__BEFORE                                    = 1347;  // <_before>
		final int SYM__BINARY                                    = 1348;  // <_binary>
		final int SYM__BY                                        = 1349;  // <_by>
		final int SYM__CALL_NOT_ON_EXCEPTION                     = 1350;  // <_call_not_on_exception>
		final int SYM__CALL_ON_EXCEPTION                         = 1351;  // <_call_on_exception>
		final int SYM__CAPACITY_IN                               = 1352;  // <_capacity_in>
		final int SYM__CHARACTER                                 = 1353;  // <_character>
		final int SYM__CHARACTERS                                = 1354;  // <_characters>
		final int SYM__COMMENTITEMS                              = 1355;  // <_Comment Items>
		final int SYM__COMMUNICATION_DESCRIPTION_CLAUSE_SEQUENCE = 1356;  // <_communication_description_clause_sequence>
		final int SYM__COMMUNICATION_DESCRIPTION_SEQUENCE        = 1357;  // <_communication_description_sequence>
		final int SYM__COMMUNICATION_SECTION                     = 1358;  // <_communication_section>
		final int SYM__CONFIGURATION_HEADER                      = 1359;  // <_configuration_header>
		final int SYM__CONFIGURATION_SECTION                     = 1360;  // <_configuration_section>
		final int SYM__CONTAINS                                  = 1361;  // <_contains>
		final int SYM__CONTROL_FINAL                             = 1362;  // <_control_final>
		final int SYM__DATA                                      = 1363;  // <_data>
		final int SYM__DATA_DESCRIPTION_CLAUSE_SEQUENCE          = 1364;  // <_data_description_clause_sequence>
		final int SYM__DATA_DIVISION                             = 1365;  // <_data_division>
		final int SYM__DATA_DIVISION_HEADER                      = 1366;  // <_data_division_header>
		final int SYM__DATA_SENTENCE_PHRASES                     = 1367;  // <_data_sentence_phrases>
		final int SYM__DEFAULT_ROUNDED_CLAUSE                    = 1368;  // <_default_rounded_clause>
		final int SYM__DEST_INDEX                                = 1369;  // <_dest_index>
		final int SYM__DISPLAY_EXCEPTION_PHRASES                 = 1370;  // <_display_exception_phrases>
		final int SYM__DISP_NOT_ON_EXCEPTION                     = 1371;  // <_disp_not_on_exception>
		final int SYM__DISP_ON_EXCEPTION                         = 1372;  // <_disp_on_exception>
		final int SYM__ENABLE_DISABLE_KEY                        = 1373;  // <_enable_disable_key>
		final int SYM__END_OF                                    = 1374;  // <_end_of>
		final int SYM__END_PROGRAM_LIST                          = 1375;  // <_end_program_list>
		final int SYM__ENTRY_CONVENTION_CLAUSE                   = 1376;  // <_entry_convention_clause>
		final int SYM__ENTRY_NAME                                = 1377;  // <_entry_name>
		final int SYM__ENVIRONMENT_DIVISION                      = 1378;  // <_environment_division>
		final int SYM__ENVIRONMENT_HEADER                        = 1379;  // <_environment_header>
		final int SYM__EVALUATE_THRU_EXPR                        = 1380;  // <_evaluate_thru_expr>
		final int SYM__EXTENDED_WITH_LOCK                        = 1381;  // <_extended_with_lock>
		final int SYM__EXT_CLAUSE                                = 1382;  // <_ext_clause>
		final int SYM__E_SEP                                     = 1383;  // <_e_sep>
		final int SYM__FALSE_IS                                  = 1384;  // <_false_is>
		final int SYM__FILE                                      = 1385;  // <_file>
		final int SYM__FILE_CONTROL_HEADER                       = 1386;  // <_file_control_header>
		final int SYM__FILE_CONTROL_SEQUENCE                     = 1387;  // <_file_control_sequence>
		final int SYM__FILE_DESCRIPTION_CLAUSE_SEQUENCE          = 1388;  // <_file_description_clause_sequence>
		final int SYM__FILE_DESCRIPTION_SEQUENCE                 = 1389;  // <_file_description_sequence>
		final int SYM__FILE_OR_SORT                              = 1390;  // <_file_or_sort>
		final int SYM__FILE_SECTION_HEADER                       = 1391;  // <_file_section_header>
		final int SYM__FILLER                                    = 1392;  // <_filler>
		final int SYM__FINAL                                     = 1393;  // <_final>
		final int SYM__FLAG_NEXT                                 = 1394;  // <_flag_next>
		final int SYM__FLAG_NOT                                  = 1395;  // <_flag_not>
		final int SYM__FOR                                       = 1396;  // <_for>
		final int SYM__FOR_SUB_RECORDS_CLAUSE                    = 1397;  // <_for_sub_records_clause>
		final int SYM__FROM                                      = 1398;  // <_from>
		final int SYM__FROM_IDENTIFIER                           = 1399;  // <_from_identifier>
		final int SYM__FROM_IDX_TO_IDX                           = 1400;  // <_from_idx_to_idx>
		final int SYM__FROM_INTEGER                              = 1401;  // <_from_integer>
		final int SYM__GLOBAL_CLAUSE                             = 1402;  // <_global_clause>
		final int SYM__IDENTIFICATION_HEADER                     = 1403;  // <_identification_header>
		final int SYM__IN                                        = 1404;  // <_in>
		final int SYM__INDEX                                     = 1405;  // <_index>
		final int SYM__INDICATE                                  = 1406;  // <_indicate>
		final int SYM__INITIAL                                   = 1407;  // <_initial>
		final int SYM__INITIALIZE_DEFAULT                        = 1408;  // <_initialize_default>
		final int SYM__INITIALIZE_FILLER                         = 1409;  // <_initialize_filler>
		final int SYM__INITIALIZE_REPLACING                      = 1410;  // <_initialize_replacing>
		final int SYM__INITIALIZE_VALUE                          = 1411;  // <_initialize_value>
		final int SYM__INPUT_CD_CLAUSES                          = 1412;  // <_input_cd_clauses>
		final int SYM__INPUT_OUTPUT_HEADER                       = 1413;  // <_input_output_header>
		final int SYM__INPUT_OUTPUT_SECTION                      = 1414;  // <_input_output_section>
		final int SYM__INTERMEDIATE_ROUNDING_CLAUSE              = 1415;  // <_intermediate_rounding_clause>
		final int SYM__INTO                                      = 1416;  // <_into>
		final int SYM__INVALID_KEY_PHRASES                       = 1417;  // <_invalid_key_phrases>
		final int SYM__INVALID_KEY_SENTENCE                      = 1418;  // <_invalid_key_sentence>
		final int SYM__IN_ORDER                                  = 1419;  // <_in_order>
		final int SYM__IS                                        = 1420;  // <_is>
		final int SYM__IS_ARE                                    = 1421;  // <_is_are>
		final int SYM__I_O_CD_CLAUSES                            = 1422;  // <_i_o_cd_clauses>
		final int SYM__I_O_CONTROL                               = 1423;  // <_i_o_control>
		final int SYM__I_O_CONTROL_HEADER                        = 1424;  // <_i_o_control_header>
		final int SYM__KEY                                       = 1425;  // <_key>
		final int SYM__KEY_LIST                                  = 1426;  // <_key_list>
		final int SYM__LEFT_OR_RIGHT                             = 1427;  // <_left_or_right>
		final int SYM__LIMITS                                    = 1428;  // <_limits>
		final int SYM__LINAGE_SEQUENCE                           = 1429;  // <_linage_sequence>
		final int SYM__LINE                                      = 1430;  // <_line>
		final int SYM__LINES                                     = 1431;  // <_lines>
		final int SYM__LINE_ADV_FILE                             = 1432;  // <_line_adv_file>
		final int SYM__LINE_OR_LINES                             = 1433;  // <_line_or_lines>
		final int SYM__LINKAGE_SECTION                           = 1434;  // <_linkage_section>
		final int SYM__LOCAL_STORAGE_SECTION                     = 1435;  // <_local_storage_section>
		final int SYM__LOCK_WITH                                 = 1436;  // <_lock_with>
		final int SYM__MESSAGE                                   = 1437;  // <_message>
		final int SYM__MNEMONIC_CONV                             = 1438;  // <_mnemonic_conv>
		final int SYM__MODE                                      = 1439;  // <_mode>
		final int SYM__MULTIPLE_FILE_POSITION                    = 1440;  // <_multiple_file_position>
		final int SYM__NOT                                       = 1441;  // <_not>
		final int SYM__NOT_AT_END_CLAUSE                         = 1442;  // <_not_at_end_clause>
		final int SYM__NOT_AT_EOP_CLAUSE                         = 1443;  // <_not_at_eop_clause>
		final int SYM__NOT_INVALID_KEY_SENTENCE                  = 1444;  // <_not_invalid_key_sentence>
		final int SYM__NOT_ON_OVERFLOW                           = 1445;  // <_not_on_overflow>
		final int SYM__NOT_ON_SIZE_ERROR                         = 1446;  // <_not_on_size_error>
		final int SYM__NO_DATA_SENTENCE                          = 1447;  // <_no_data_sentence>
		final int SYM__NUMBER                                    = 1448;  // <_number>
		final int SYM__NUMBERS                                   = 1449;  // <_numbers>
		final int SYM__OBJECT_COMPUTER_ENTRY                     = 1450;  // <_object_computer_entry>
		final int SYM__OCCURS_DEPENDING                          = 1451;  // <_occurs_depending>
		final int SYM__OCCURS_FROM_INTEGER                       = 1452;  // <_occurs_from_integer>
		final int SYM__OCCURS_INDEXED                            = 1453;  // <_occurs_indexed>
		final int SYM__OCCURS_INITIALIZED                        = 1454;  // <_occurs_initialized>
		final int SYM__OCCURS_INTEGER_TO                         = 1455;  // <_occurs_integer_to>
		final int SYM__OCCURS_KEYS_AND_INDEXED                   = 1456;  // <_occurs_keys_and_indexed>
		final int SYM__OCCURS_STEP                               = 1457;  // <_occurs_step>
		final int SYM__OCCURS_TO_INTEGER                         = 1458;  // <_occurs_to_integer>
		final int SYM__OF                                        = 1459;  // <_of>
		final int SYM__ON                                        = 1460;  // <_on>
		final int SYM__ONOFF_STATUS                              = 1461;  // <_onoff_status>
		final int SYM__ON_OVERFLOW                               = 1462;  // <_on_overflow>
		final int SYM__ON_OVERFLOW_PHRASES                       = 1463;  // <_on_overflow_phrases>
		final int SYM__ON_SIZE_ERROR                             = 1464;  // <_on_size_error>
		final int SYM__OPTIONS_CLAUSES                           = 1465;  // <_options_clauses>
		final int SYM__OPTIONS_PARAGRAPH                         = 1466;  // <_options_paragraph>
		final int SYM__OR_PAGE                                   = 1467;  // <_or_page>
		final int SYM__OTHER                                     = 1468;  // <_other>
		final int SYM__OUTPUT_CD_CLAUSES                         = 1469;  // <_output_cd_clauses>
		final int SYM__PAGE_HEADING_LIST                         = 1470;  // <_page_heading_list>
		final int SYM__PRINTING                                  = 1471;  // <_printing>
		final int SYM__PROCEDURE                                 = 1472;  // <_procedure>
		final int SYM__PROCEDURE_DECLARATIVES                    = 1473;  // <_procedure_declaratives>
		final int SYM__PROCEDURE_DIVISION                        = 1474;  // <_procedure_division>
		final int SYM__PROCEDURE_LIST                            = 1475;  // <_procedure_list>
		final int SYM__PROCEDURE_OPTIONAL                        = 1476;  // <_procedure_optional>
		final int SYM__PROCEDURE_RETURNING                       = 1477;  // <_procedure_returning>
		final int SYM__PROCEDURE_TYPE                            = 1478;  // <_procedure_type>
		final int SYM__PROCEDURE_USING_CHAINING                  = 1479;  // <_procedure_using_chaining>
		final int SYM__PROCEED_TO                                = 1480;  // <_proceed_to>
		final int SYM__PROGRAM                                   = 1481;  // <_program>
		final int SYM__PROGRAM_BODY                              = 1482;  // <_program_body>
		final int SYM__PROGRAM_TYPE                              = 1483;  // <_program_type>
		final int SYM__RECORD                                    = 1484;  // <_record>
		final int SYM__RECORDS                                   = 1485;  // <_records>
		final int SYM__RECORDS_OR_CHARACTERS                     = 1486;  // <_records_or_characters>
		final int SYM__RECORD_DEPENDING                          = 1487;  // <_record_depending>
		final int SYM__RECORD_DESCRIPTION_LIST                   = 1488;  // <_record_description_list>
		final int SYM__RENAMES_THRU                              = 1489;  // <_renames_thru>
		final int SYM__REPLACING_LINE                            = 1490;  // <_replacing_line>
		final int SYM__REPORT_DESCRIPTION_OPTIONS                = 1491;  // <_report_description_options>
		final int SYM__REPORT_DESCRIPTION_SEQUENCE               = 1492;  // <_report_description_sequence>
		final int SYM__REPORT_GROUP_DESCRIPTION_LIST             = 1493;  // <_report_group_description_list>
		final int SYM__REPORT_GROUP_OPTIONS                      = 1494;  // <_report_group_options>
		final int SYM__REPORT_SECTION                            = 1495;  // <_report_section>
		final int SYM__REPOSITORY_ENTRY                          = 1496;  // <_repository_entry>
		final int SYM__REPOSITORY_PARAGRAPH                      = 1497;  // <_repository_paragraph>
		final int SYM__RESET_CLAUSE                              = 1498;  // <_reset_clause>
		final int SYM__RETRY_PHRASE                              = 1499;  // <_retry_phrase>
		final int SYM__RIGHT                                     = 1500;  // <_right>
		final int SYM__SAME_OPTION                               = 1501;  // <_same_option>
		final int SYM__SCREEN_COL_PLUS_MINUS                     = 1502;  // <_screen_col_plus_minus>
		final int SYM__SCREEN_DESCRIPTION_LIST                   = 1503;  // <_screen_description_list>
		final int SYM__SCREEN_LINE_PLUS_MINUS                    = 1504;  // <_screen_line_plus_minus>
		final int SYM__SCREEN_OPTIONS                            = 1505;  // <_screen_options>
		final int SYM__SCREEN_SECTION                            = 1506;  // <_screen_section>
		final int SYM__SCROLL_LINES                              = 1507;  // <_scroll_lines>
		final int SYM__SEGMENT                                   = 1508;  // <_segment>
		final int SYM__SELECT_CLAUSES_OR_ERROR                   = 1509;  // <_select_clauses_or_error>
		final int SYM__SELECT_CLAUSE_SEQUENCE                    = 1510;  // <_select_clause_sequence>
		final int SYM__SIGN                                      = 1511;  // <_sign>
		final int SYM__SIGNED                                    = 1512;  // <_signed>
		final int SYM__SIGN_IS                                   = 1513;  // <_sign_is>
		final int SYM__SIZE                                      = 1514;  // <_size>
		final int SYM__SIZE_OPTIONAL                             = 1515;  // <_size_optional>
		final int SYM__SORT_DUPLICATES                           = 1516;  // <_sort_duplicates>
		final int SYM__SOURCE_COMPUTER_ENTRY                     = 1517;  // <_source_computer_entry>
		final int SYM__SOURCE_OBJECT_COMPUTER_PARAGRAPHS         = 1518;  // <_source_object_computer_paragraphs>
		final int SYM__SPECIAL_NAMES_PARAGRAPH                   = 1519;  // <_special_names_paragraph>
		final int SYM__SPECIAL_NAMES_SENTENCE_LIST               = 1520;  // <_special_names_sentence_list>
		final int SYM__SPECIAL_NAME_MNEMONIC_ON_OFF              = 1521;  // <_special_name_mnemonic_on_off>
		final int SYM__STANDARD                                  = 1522;  // <_standard>
		final int SYM__STATUS                                    = 1523;  // <_status>
		final int SYM__STATUS_X                                  = 1524;  // <_status_x>
		final int SYM__STRING_DELIMITED                          = 1525;  // <_string_delimited>
		final int SYM__SUPPRESS_CLAUSE                           = 1526;  // <_suppress_clause>
		final int SYM__SYMBOLIC                                  = 1527;  // <_symbolic>
		final int SYM__SYM_IN_WORD                               = 1528;  // <_sym_in_word>
		final int SYM__TAPE                                      = 1529;  // <_tape>
		final int SYM__TERMINAL                                  = 1530;  // <_terminal>
		final int SYM__THEN                                      = 1531;  // <_then>
		final int SYM__TIMES                                     = 1532;  // <_times>
		final int SYM__TO                                        = 1533;  // <_to>
		final int SYM__TO_INTEGER                                = 1534;  // <_to_integer>
		final int SYM__TO_USING                                  = 1535;  // <_to_using>
		final int SYM__UNSTRING_DELIMITED                        = 1536;  // <_unstring_delimited>
		final int SYM__UNSTRING_INTO_COUNT                       = 1537;  // <_unstring_into_count>
		final int SYM__UNSTRING_INTO_DELIMITER                   = 1538;  // <_unstring_into_delimiter>
		final int SYM__UNSTRING_TALLYING                         = 1539;  // <_unstring_tallying>
		final int SYM__USE_STATEMENT                             = 1540;  // <_use_statement>
		final int SYM__WHEN                                      = 1541;  // <_when>
		final int SYM__WHEN_SET_TO                               = 1542;  // <_when_set_to>
		final int SYM__WITH                                      = 1543;  // <_with>
		final int SYM__WITH_DATA_SENTENCE                        = 1544;  // <_with_data_sentence>
		final int SYM__WITH_DEBUGGING_MODE                       = 1545;  // <_with_debugging_mode>
		final int SYM__WITH_LOCK                                 = 1546;  // <_with_lock>
		final int SYM__WITH_PIC_SYMBOL                           = 1547;  // <_with_pic_symbol>
		final int SYM__WITH_POINTER                              = 1548;  // <_with_pointer>
		final int SYM__WORKING_STORAGE_SECTION                   = 1549;  // <_working_storage_section>
		final int SYM__X_LIST                                    = 1550;  // <_x_list>
	};

	// Symbolic constants naming the table indices of the grammar rules
	@SuppressWarnings("unused")
	private interface RuleConstants
	{
		final int PROD_CLASS_NAME_COBOLWORD                                                  =    0;  // <CLASS_NAME> ::= COBOLWord
		final int PROD_FUNCTION_NAME_FUNCTION                                                =    1;  // <FUNCTION_NAME> ::= FUNCTION <COMMON_FUNCTION>
		final int PROD_MNEMONIC_NAME_TOK_MNEMONIC_NAME                                       =    2;  // <MNEMONIC_NAME_TOK> ::= 'MNEMONIC_NAME'
		final int PROD_PROGRAM_NAME_COBOLWORD                                                =    3;  // <PROGRAM_NAME> ::= COBOLWord
		final int PROD_USER_FUNCTION_NAME_FUNCTION_COBOLWORD                                 =    4;  // <USER_FUNCTION_NAME> ::= FUNCTION COBOLWord
		final int PROD_WORD_COBOLWORD                                                        =    5;  // <WORD> ::= COBOLWord
		final int PROD_LITERAL_TOK_STRINGLITERAL                                             =    6;  // <LITERAL_TOK> ::= StringLiteral
		final int PROD_LITERAL_TOK_HEXLITERAL                                                =    7;  // <LITERAL_TOK> ::= HexLiteral
		final int PROD_LITERAL_TOK_ZLITERAL                                                  =    8;  // <LITERAL_TOK> ::= ZLiteral
		final int PROD_LITERAL_TOK_BOOLEANLITERAL                                            =    9;  // <LITERAL_TOK> ::= BooleanLiteral
		final int PROD_LITERAL_TOK_BOOLEANHEXLITERAL                                         =   10;  // <LITERAL_TOK> ::= BooleanHexLiteral
		final int PROD_LITERAL_TOK_NATIONALLITERAL                                           =   11;  // <LITERAL_TOK> ::= NationalLiteral
		final int PROD_LITERAL_TOK_NATIONALHEXLITERAL                                        =   12;  // <LITERAL_TOK> ::= NationalHexLiteral
		final int PROD_LITERAL_TOK_ACUBINNUMLITERAL                                          =   13;  // <LITERAL_TOK> ::= AcuBinNumLiteral
		final int PROD_LITERAL_TOK_ACUOCTNUMLITERAL                                          =   14;  // <LITERAL_TOK> ::= AcuOctNumLiteral
		final int PROD_LITERAL_TOK_ACUHEXNUMLITERAL                                          =   15;  // <LITERAL_TOK> ::= AcuHexNumLiteral
		final int PROD_LITERAL_TOK_INTLITERAL                                                =   16;  // <LITERAL_TOK> ::= IntLiteral
		final int PROD_LITERAL_TOK_DECIMALLITERAL                                            =   17;  // <LITERAL_TOK> ::= DecimalLiteral
		final int PROD_LITERAL_TOK_SIXTY_SIX                                                 =   18;  // <LITERAL_TOK> ::= 'SIXTY_SIX'
		final int PROD_LITERAL_TOK_SEVENTY_EIGHT                                             =   19;  // <LITERAL_TOK> ::= 'SEVENTY_EIGHT'
		final int PROD_LITERAL_TOK_EIGHTY_EIGHT                                              =   20;  // <LITERAL_TOK> ::= 'EIGHTY_EIGHT'
		final int PROD_LITERAL_TOK_FLOATLITERAL                                              =   21;  // <LITERAL_TOK> ::= FloatLiteral
		final int PROD_CONCATENATE_FUNC_FUNCTION_CONCATENATE                                 =   22;  // <CONCATENATE_FUNC> ::= FUNCTION CONCATENATE
		final int PROD_CURRENT_DATE_FUNC_FUNCTION_CURRENT_DATE                               =   23;  // <CURRENT_DATE_FUNC> ::= FUNCTION 'CURRENT_DATE'
		final int PROD_DISPLAY_OF_FUNC_FUNCTION_DISPLAY_OF                                   =   24;  // <DISPLAY_OF_FUNC> ::= FUNCTION 'DISPLAY_OF'
		final int PROD_FORMATTED_DATE_FUNC_FUNCTION_FORMATTED_DATE                           =   25;  // <FORMATTED_DATE_FUNC> ::= FUNCTION 'FORMATTED_DATE'
		final int PROD_FORMATTED_DATETIME_FUNC_FUNCTION_FORMATTED_DATETIME                   =   26;  // <FORMATTED_DATETIME_FUNC> ::= FUNCTION 'FORMATTED_DATETIME'
		final int PROD_FORMATTED_TIME_FUNC_FUNCTION_FORMATTED_TIME                           =   27;  // <FORMATTED_TIME_FUNC> ::= FUNCTION 'FORMATTED_TIME'
		final int PROD_LENGTH_FUNC_FUNCTION_LENGTH                                           =   28;  // <LENGTH_FUNC> ::= FUNCTION LENGTH
		final int PROD_LENGTH_FUNC_FUNCTION_BYTE_LENGTH                                      =   29;  // <LENGTH_FUNC> ::= FUNCTION 'BYTE_LENGTH'
		final int PROD_LOCALE_DATE_FUNC_FUNCTION_LOCALE_DATE                                 =   30;  // <LOCALE_DATE_FUNC> ::= FUNCTION 'LOCALE_DATE'
		final int PROD_LOCALE_TIME_FUNC_FUNCTION_LOCALE_TIME                                 =   31;  // <LOCALE_TIME_FUNC> ::= FUNCTION 'LOCALE_TIME'
		final int PROD_LOCALE_TIME_FROM_FUNC_FUNCTION_LOCALE_TIME_FROM_SECONDS               =   32;  // <LOCALE_TIME_FROM_FUNC> ::= FUNCTION 'LOCALE_TIME_FROM_SECONDS'
		final int PROD_LOWER_CASE_FUNC_FUNCTION_LOWER_CASE                                   =   33;  // <LOWER_CASE_FUNC> ::= FUNCTION 'LOWER_CASE'
		final int PROD_NATIONAL_OF_FUNC_FUNCTION_NATIONAL_OF                                 =   34;  // <NATIONAL_OF_FUNC> ::= FUNCTION 'NATIONAL_OF'
		final int PROD_NUMVALC_FUNC_FUNCTION_NUMVAL_C                                        =   35;  // <NUMVALC_FUNC> ::= FUNCTION 'NUMVAL_C'
		final int PROD_REVERSE_FUNC_FUNCTION_REVERSE                                         =   36;  // <REVERSE_FUNC> ::= FUNCTION REVERSE
		final int PROD_SUBSTITUTE_FUNC_FUNCTION_SUBSTITUTE                                   =   37;  // <SUBSTITUTE_FUNC> ::= FUNCTION SUBSTITUTE
		final int PROD_SUBSTITUTE_CASE_FUNC_FUNCTION_SUBSTITUTE_CASE                         =   38;  // <SUBSTITUTE_CASE_FUNC> ::= FUNCTION 'SUBSTITUTE_CASE'
		final int PROD_TRIM_FUNC_FUNCTION_TRIM                                               =   39;  // <TRIM_FUNC> ::= FUNCTION TRIM
		final int PROD_UPPER_CASE_FUNC_FUNCTION_UPPER_CASE                                   =   40;  // <UPPER_CASE_FUNC> ::= FUNCTION 'UPPER_CASE'
		final int PROD_WHEN_COMPILED_FUNC_FUNCTION_WHEN_COMPILED                             =   41;  // <WHEN_COMPILED_FUNC> ::= FUNCTION 'WHEN_COMPILED'
		final int PROD_COMMON_FUNCTION_ABS                                                   =   42;  // <COMMON_FUNCTION> ::= ABS
		final int PROD_COMMON_FUNCTION_ACOS                                                  =   43;  // <COMMON_FUNCTION> ::= ACOS
		final int PROD_COMMON_FUNCTION_ANNUITY                                               =   44;  // <COMMON_FUNCTION> ::= ANNUITY
		final int PROD_COMMON_FUNCTION_ASIN                                                  =   45;  // <COMMON_FUNCTION> ::= ASIN
		final int PROD_COMMON_FUNCTION_ATAN                                                  =   46;  // <COMMON_FUNCTION> ::= ATAN
		final int PROD_COMMON_FUNCTION_BOOLEAN_OF_INTEGER                                    =   47;  // <COMMON_FUNCTION> ::= 'BOOLEAN_OF_INTEGER'
		final int PROD_COMMON_FUNCTION_CHAR                                                  =   48;  // <COMMON_FUNCTION> ::= CHAR
		final int PROD_COMMON_FUNCTION_CHAR_NATIONAL                                         =   49;  // <COMMON_FUNCTION> ::= 'CHAR_NATIONAL'
		final int PROD_COMMON_FUNCTION_COMBINED_DATETIME                                     =   50;  // <COMMON_FUNCTION> ::= 'COMBINED_DATETIME'
		final int PROD_COMMON_FUNCTION_COS                                                   =   51;  // <COMMON_FUNCTION> ::= COS
		final int PROD_COMMON_FUNCTION_CURRENCY_SYMBOL                                       =   52;  // <COMMON_FUNCTION> ::= 'CURRENCY_SYMBOL'
		final int PROD_COMMON_FUNCTION_DATE_OF_INTEGER                                       =   53;  // <COMMON_FUNCTION> ::= 'DATE_OF_INTEGER'
		final int PROD_COMMON_FUNCTION_DATE_TO_YYYYMMDD                                      =   54;  // <COMMON_FUNCTION> ::= 'DATE_TO_YYYYMMDD'
		final int PROD_COMMON_FUNCTION_DAY_OF_INTEGER                                        =   55;  // <COMMON_FUNCTION> ::= 'DAY_OF_INTEGER'
		final int PROD_COMMON_FUNCTION_DAY_TO_YYYYDDD                                        =   56;  // <COMMON_FUNCTION> ::= 'DAY_TO_YYYYDDD'
		final int PROD_COMMON_FUNCTION_E                                                     =   57;  // <COMMON_FUNCTION> ::= E
		final int PROD_COMMON_FUNCTION_EXCEPTION_FILE                                        =   58;  // <COMMON_FUNCTION> ::= 'EXCEPTION_FILE'
		final int PROD_COMMON_FUNCTION_EXCEPTION_FILE_N                                      =   59;  // <COMMON_FUNCTION> ::= 'EXCEPTION_FILE_N'
		final int PROD_COMMON_FUNCTION_EXCEPTION_LOCATION                                    =   60;  // <COMMON_FUNCTION> ::= 'EXCEPTION_LOCATION'
		final int PROD_COMMON_FUNCTION_EXCEPTION_LOCATION_N                                  =   61;  // <COMMON_FUNCTION> ::= 'EXCEPTION_LOCATION_N'
		final int PROD_COMMON_FUNCTION_EXCEPTION_STATEMENT                                   =   62;  // <COMMON_FUNCTION> ::= 'EXCEPTION_STATEMENT'
		final int PROD_COMMON_FUNCTION_EXCEPTION_STATUS                                      =   63;  // <COMMON_FUNCTION> ::= 'EXCEPTION_STATUS'
		final int PROD_COMMON_FUNCTION_EXP                                                   =   64;  // <COMMON_FUNCTION> ::= EXP
		final int PROD_COMMON_FUNCTION_FACTORIAL                                             =   65;  // <COMMON_FUNCTION> ::= FACTORIAL
		final int PROD_COMMON_FUNCTION_FORMATTED_CURRENT_DATE                                =   66;  // <COMMON_FUNCTION> ::= 'FORMATTED_CURRENT_DATE'
		final int PROD_COMMON_FUNCTION_FRACTION_PART                                         =   67;  // <COMMON_FUNCTION> ::= 'FRACTION_PART'
		final int PROD_COMMON_FUNCTION_HIGHEST_ALGEBRAIC                                     =   68;  // <COMMON_FUNCTION> ::= 'HIGHEST_ALGEBRAIC'
		final int PROD_COMMON_FUNCTION_INTEGER                                               =   69;  // <COMMON_FUNCTION> ::= INTEGER
		final int PROD_COMMON_FUNCTION_INTEGER_OF_BOOLEAN                                    =   70;  // <COMMON_FUNCTION> ::= 'INTEGER_OF_BOOLEAN'
		final int PROD_COMMON_FUNCTION_INTEGER_OF_DATE                                       =   71;  // <COMMON_FUNCTION> ::= 'INTEGER_OF_DATE'
		final int PROD_COMMON_FUNCTION_INTEGER_OF_DAY                                        =   72;  // <COMMON_FUNCTION> ::= 'INTEGER_OF_DAY'
		final int PROD_COMMON_FUNCTION_INTEGER_OF_FORMATTED_DATE                             =   73;  // <COMMON_FUNCTION> ::= 'INTEGER_OF_FORMATTED_DATE'
		final int PROD_COMMON_FUNCTION_INTEGER_PART                                          =   74;  // <COMMON_FUNCTION> ::= 'INTEGER_PART'
		final int PROD_COMMON_FUNCTION_LOCALE_COMPARE                                        =   75;  // <COMMON_FUNCTION> ::= 'LOCALE_COMPARE'
		final int PROD_COMMON_FUNCTION_LOG                                                   =   76;  // <COMMON_FUNCTION> ::= LOG
		final int PROD_COMMON_FUNCTION_LOWEST_ALGEBRAIC                                      =   77;  // <COMMON_FUNCTION> ::= 'LOWEST_ALGEBRAIC'
		final int PROD_COMMON_FUNCTION_MAX                                                   =   78;  // <COMMON_FUNCTION> ::= MAX
		final int PROD_COMMON_FUNCTION_MEAN                                                  =   79;  // <COMMON_FUNCTION> ::= MEAN
		final int PROD_COMMON_FUNCTION_MEDIAN                                                =   80;  // <COMMON_FUNCTION> ::= MEDIAN
		final int PROD_COMMON_FUNCTION_MIDRANGE                                              =   81;  // <COMMON_FUNCTION> ::= MIDRANGE
		final int PROD_COMMON_FUNCTION_MIN                                                   =   82;  // <COMMON_FUNCTION> ::= MIN
		final int PROD_COMMON_FUNCTION_MOD                                                   =   83;  // <COMMON_FUNCTION> ::= MOD
		final int PROD_COMMON_FUNCTION_MODULE_CALLER_ID                                      =   84;  // <COMMON_FUNCTION> ::= 'MODULE_CALLER_ID'
		final int PROD_COMMON_FUNCTION_MODULE_DATE                                           =   85;  // <COMMON_FUNCTION> ::= 'MODULE_DATE'
		final int PROD_COMMON_FUNCTION_MODULE_FORMATTED_DATE                                 =   86;  // <COMMON_FUNCTION> ::= 'MODULE_FORMATTED_DATE'
		final int PROD_COMMON_FUNCTION_MODULE_ID                                             =   87;  // <COMMON_FUNCTION> ::= 'MODULE_ID'
		final int PROD_COMMON_FUNCTION_MODULE_PATH                                           =   88;  // <COMMON_FUNCTION> ::= 'MODULE_PATH'
		final int PROD_COMMON_FUNCTION_MODULE_SOURCE                                         =   89;  // <COMMON_FUNCTION> ::= 'MODULE_SOURCE'
		final int PROD_COMMON_FUNCTION_MODULE_TIME                                           =   90;  // <COMMON_FUNCTION> ::= 'MODULE_TIME'
		final int PROD_COMMON_FUNCTION_MONETARY_DECIMAL_POINT                                =   91;  // <COMMON_FUNCTION> ::= 'MONETARY_DECIMAL_POINT'
		final int PROD_COMMON_FUNCTION_MONETARY_THOUSANDS_SEPARATOR                          =   92;  // <COMMON_FUNCTION> ::= 'MONETARY_THOUSANDS_SEPARATOR'
		final int PROD_COMMON_FUNCTION_NUMERIC_DECIMAL_POINT                                 =   93;  // <COMMON_FUNCTION> ::= 'NUMERIC_DECIMAL_POINT'
		final int PROD_COMMON_FUNCTION_NUMERIC_THOUSANDS_SEPARATOR                           =   94;  // <COMMON_FUNCTION> ::= 'NUMERIC_THOUSANDS_SEPARATOR'
		final int PROD_COMMON_FUNCTION_NUMVAL                                                =   95;  // <COMMON_FUNCTION> ::= NUMVAL
		final int PROD_COMMON_FUNCTION_NUMVAL_F                                              =   96;  // <COMMON_FUNCTION> ::= 'NUMVAL_F'
		final int PROD_COMMON_FUNCTION_ORD                                                   =   97;  // <COMMON_FUNCTION> ::= ORD
		final int PROD_COMMON_FUNCTION_ORD_MAX                                               =   98;  // <COMMON_FUNCTION> ::= 'ORD_MAX'
		final int PROD_COMMON_FUNCTION_ORD_MIN                                               =   99;  // <COMMON_FUNCTION> ::= 'ORD_MIN'
		final int PROD_COMMON_FUNCTION_PI                                                    =  100;  // <COMMON_FUNCTION> ::= PI
		final int PROD_COMMON_FUNCTION_PRESENT_VALUE                                         =  101;  // <COMMON_FUNCTION> ::= 'PRESENT_VALUE'
		final int PROD_COMMON_FUNCTION_RANDOM                                                =  102;  // <COMMON_FUNCTION> ::= RANDOM
		final int PROD_COMMON_FUNCTION_RANGE                                                 =  103;  // <COMMON_FUNCTION> ::= RANGE
		final int PROD_COMMON_FUNCTION_REM                                                   =  104;  // <COMMON_FUNCTION> ::= REM
		final int PROD_COMMON_FUNCTION_SECONDS_FROM_FORMATTED_TIME                           =  105;  // <COMMON_FUNCTION> ::= 'SECONDS_FROM_FORMATTED_TIME'
		final int PROD_COMMON_FUNCTION_SECONDS_PAST_MIDNIGHT                                 =  106;  // <COMMON_FUNCTION> ::= 'SECONDS_PAST_MIDNIGHT'
		final int PROD_COMMON_FUNCTION_SIGN                                                  =  107;  // <COMMON_FUNCTION> ::= SIGN
		final int PROD_COMMON_FUNCTION_SIN                                                   =  108;  // <COMMON_FUNCTION> ::= SIN
		final int PROD_COMMON_FUNCTION_SQRT                                                  =  109;  // <COMMON_FUNCTION> ::= SQRT
		final int PROD_COMMON_FUNCTION_STANDARD_COMPARE                                      =  110;  // <COMMON_FUNCTION> ::= 'STANDARD_COMPARE'
		final int PROD_COMMON_FUNCTION_STANDARD_DEVIATION                                    =  111;  // <COMMON_FUNCTION> ::= 'STANDARD_DEVIATION'
		final int PROD_COMMON_FUNCTION_STORED_CHAR_LENGTH                                    =  112;  // <COMMON_FUNCTION> ::= 'STORED_CHAR_LENGTH'
		final int PROD_COMMON_FUNCTION_SUM                                                   =  113;  // <COMMON_FUNCTION> ::= SUM
		final int PROD_COMMON_FUNCTION_TAN                                                   =  114;  // <COMMON_FUNCTION> ::= TAN
		final int PROD_COMMON_FUNCTION_TEST_DATE_YYYYMMDD                                    =  115;  // <COMMON_FUNCTION> ::= 'TEST_DATE_YYYYMMDD'
		final int PROD_COMMON_FUNCTION_TEST_DAY_YYYYDDD                                      =  116;  // <COMMON_FUNCTION> ::= 'TEST_DAY_YYYYDDD'
		final int PROD_COMMON_FUNCTION_TEST_FORMATTED_DATETIME                               =  117;  // <COMMON_FUNCTION> ::= 'TEST_FORMATTED_DATETIME'
		final int PROD_COMMON_FUNCTION_TEST_NUMVAL                                           =  118;  // <COMMON_FUNCTION> ::= 'TEST_NUMVAL'
		final int PROD_COMMON_FUNCTION_TEST_NUMVAL_F                                         =  119;  // <COMMON_FUNCTION> ::= 'TEST_NUMVAL_F'
		final int PROD_COMMON_FUNCTION_VARIANCE                                              =  120;  // <COMMON_FUNCTION> ::= VARIANCE
		final int PROD_COMMON_FUNCTION_YEAR_TO_YYYY                                          =  121;  // <COMMON_FUNCTION> ::= 'YEAR_TO_YYYY'
		final int PROD_START                                                                 =  122;  // <start> ::= <compilation_group>
		final int PROD_COMPILATION_GROUP                                                     =  123;  // <compilation_group> ::= <simple_prog>
		final int PROD_COMPILATION_GROUP2                                                    =  124;  // <compilation_group> ::= <nested_list>
		final int PROD_NESTED_LIST                                                           =  125;  // <nested_list> ::= <source_element_list>
		final int PROD_SOURCE_ELEMENT_LIST                                                   =  126;  // <source_element_list> ::= <source_element>
		final int PROD_SOURCE_ELEMENT_LIST2                                                  =  127;  // <source_element_list> ::= <source_element_list> <source_element>
		final int PROD_SOURCE_ELEMENT                                                        =  128;  // <source_element> ::= <program_definition>
		final int PROD_SOURCE_ELEMENT2                                                       =  129;  // <source_element> ::= <function_definition>
		final int PROD_SIMPLE_PROG                                                           =  130;  // <simple_prog> ::= <_program_body>
		final int PROD_PROGRAM_DEFINITION                                                    =  131;  // <program_definition> ::= <_identification_header> <program_id_paragraph> <_Comment Items> <_options_paragraph> <_program_body> <_end_program_list>
		final int PROD_FUNCTION_DEFINITION                                                   =  132;  // <function_definition> ::= <_identification_header> <function_id_paragraph> <_Comment Items> <_options_paragraph> <_program_body> <end_function>
		final int PROD__COMMENTITEMS                                                         =  133;  // <_Comment Items> ::= <_Comment Items> <Comment Item>
		final int PROD__COMMENTITEMS2                                                        =  134;  // <_Comment Items> ::= 
		final int PROD_COMMENTITEM_AUTHOR_TOK_DOT_TOK_DOT                                    =  135;  // <Comment Item> ::= AUTHOR 'TOK_DOT' <NoiseList> 'TOK_DOT'
		final int PROD_COMMENTITEM_INSTALLATION_TOK_DOT_TOK_DOT                              =  136;  // <Comment Item> ::= INSTALLATION 'TOK_DOT' <NoiseList> 'TOK_DOT'
		final int PROD_COMMENTITEM_DATE_WRITTEN_TOK_DOT_TOK_DOT                              =  137;  // <Comment Item> ::= 'DATE_WRITTEN' 'TOK_DOT' <NoiseList> 'TOK_DOT'
		final int PROD_COMMENTITEM_DATE_COMPILED_TOK_DOT_TOK_DOT                             =  138;  // <Comment Item> ::= 'DATE_COMPILED' 'TOK_DOT' <NoiseList> 'TOK_DOT'
		final int PROD_COMMENTITEM_SECURITY_TOK_DOT_TOK_DOT                                  =  139;  // <Comment Item> ::= SECURITY 'TOK_DOT' <NoiseList> 'TOK_DOT'
		final int PROD_NOISELIST                                                             =  140;  // <NoiseList> ::= <NoiseList> <Noise>
		final int PROD_NOISELIST2                                                            =  141;  // <NoiseList> ::= <Noise>
		final int PROD_NOISE_STRINGLITERAL                                                   =  142;  // <Noise> ::= StringLiteral
		final int PROD_NOISE_INTLITERAL                                                      =  143;  // <Noise> ::= IntLiteral
		final int PROD_NOISE_DECIMALLITERAL                                                  =  144;  // <Noise> ::= DecimalLiteral
		final int PROD_NOISE_COBOLWORD                                                       =  145;  // <Noise> ::= COBOLWord
		final int PROD_NOISE_COMMA_DELIM                                                     =  146;  // <Noise> ::= 'COMMA_DELIM'
		final int PROD__END_PROGRAM_LIST                                                     =  147;  // <_end_program_list> ::= 
		final int PROD__END_PROGRAM_LIST2                                                    =  148;  // <_end_program_list> ::= <end_program_list>
		final int PROD_END_PROGRAM_LIST                                                      =  149;  // <end_program_list> ::= <end_program>
		final int PROD_END_PROGRAM_LIST2                                                     =  150;  // <end_program_list> ::= <end_program_list> <end_program>
		final int PROD_END_PROGRAM_END_PROGRAM_TOK_DOT                                       =  151;  // <end_program> ::= 'END_PROGRAM' <end_program_name> 'TOK_DOT'
		final int PROD_END_FUNCTION_END_FUNCTION_TOK_DOT                                     =  152;  // <end_function> ::= 'END_FUNCTION' <end_program_name> 'TOK_DOT'
		final int PROD__PROGRAM_BODY                                                         =  153;  // <_program_body> ::= <_environment_division> <_data_division> <_procedure_division>
		final int PROD__IDENTIFICATION_HEADER                                                =  154;  // <_identification_header> ::= 
		final int PROD__IDENTIFICATION_HEADER_DIVISION_TOK_DOT                               =  155;  // <_identification_header> ::= <identification_or_id> DIVISION 'TOK_DOT'
		final int PROD_IDENTIFICATION_OR_ID_IDENTIFICATION                                   =  156;  // <identification_or_id> ::= IDENTIFICATION
		final int PROD_IDENTIFICATION_OR_ID_ID                                               =  157;  // <identification_or_id> ::= ID
		final int PROD_PROGRAM_ID_PARAGRAPH_PROGRAM_ID_TOK_DOT_TOK_DOT                       =  158;  // <program_id_paragraph> ::= 'PROGRAM_ID' 'TOK_DOT' <program_id_name> <_as_literal> <_program_type> 'TOK_DOT'
		final int PROD_FUNCTION_ID_PARAGRAPH_FUNCTION_ID_TOK_DOT_TOK_DOT                     =  159;  // <function_id_paragraph> ::= 'FUNCTION_ID' 'TOK_DOT' <program_id_name> <_as_literal> 'TOK_DOT'
		final int PROD_PROGRAM_ID_NAME                                                       =  160;  // <program_id_name> ::= <PROGRAM_NAME>
		final int PROD_PROGRAM_ID_NAME2                                                      =  161;  // <program_id_name> ::= <LITERAL_TOK>
		final int PROD_END_PROGRAM_NAME                                                      =  162;  // <end_program_name> ::= <PROGRAM_NAME>
		final int PROD_END_PROGRAM_NAME2                                                     =  163;  // <end_program_name> ::= <LITERAL_TOK>
		final int PROD__AS_LITERAL                                                           =  164;  // <_as_literal> ::= 
		final int PROD__AS_LITERAL_AS                                                        =  165;  // <_as_literal> ::= AS <LITERAL_TOK>
		final int PROD__PROGRAM_TYPE                                                         =  166;  // <_program_type> ::= 
		final int PROD__PROGRAM_TYPE2                                                        =  167;  // <_program_type> ::= <_is> <program_type_clause> <_program>
		final int PROD_PROGRAM_TYPE_CLAUSE_COMMON                                            =  168;  // <program_type_clause> ::= COMMON
		final int PROD_PROGRAM_TYPE_CLAUSE                                                   =  169;  // <program_type_clause> ::= <init_or_recurse_and_common>
		final int PROD_PROGRAM_TYPE_CLAUSE2                                                  =  170;  // <program_type_clause> ::= <init_or_recurse>
		final int PROD_PROGRAM_TYPE_CLAUSE_EXTERNAL                                          =  171;  // <program_type_clause> ::= EXTERNAL
		final int PROD_INIT_OR_RECURSE_AND_COMMON_COMMON                                     =  172;  // <init_or_recurse_and_common> ::= <init_or_recurse> COMMON
		final int PROD_INIT_OR_RECURSE_AND_COMMON_COMMON2                                    =  173;  // <init_or_recurse_and_common> ::= COMMON <init_or_recurse>
		final int PROD_INIT_OR_RECURSE_TOK_INITIAL                                           =  174;  // <init_or_recurse> ::= 'TOK_INITIAL'
		final int PROD_INIT_OR_RECURSE_RECURSIVE                                             =  175;  // <init_or_recurse> ::= RECURSIVE
		final int PROD__OPTIONS_PARAGRAPH                                                    =  176;  // <_options_paragraph> ::= 
		final int PROD__OPTIONS_PARAGRAPH_OPTIONS_TOK_DOT                                    =  177;  // <_options_paragraph> ::= OPTIONS 'TOK_DOT' <_options_clauses>
		final int PROD__OPTIONS_CLAUSES_TOK_DOT                                              =  178;  // <_options_clauses> ::= <_default_rounded_clause> <_entry_convention_clause> <_intermediate_rounding_clause> 'TOK_DOT'
		final int PROD__DEFAULT_ROUNDED_CLAUSE                                               =  179;  // <_default_rounded_clause> ::= 
		final int PROD__DEFAULT_ROUNDED_CLAUSE_DEFAULT_ROUNDED                               =  180;  // <_default_rounded_clause> ::= DEFAULT ROUNDED <_mode> <_is> <round_choice>
		final int PROD__ENTRY_CONVENTION_CLAUSE                                              =  181;  // <_entry_convention_clause> ::= 
		final int PROD__ENTRY_CONVENTION_CLAUSE_ENTRY_CONVENTION                             =  182;  // <_entry_convention_clause> ::= 'ENTRY_CONVENTION' <_is> <convention_type>
		final int PROD_CONVENTION_TYPE_COBOL                                                 =  183;  // <convention_type> ::= COBOL
		final int PROD_CONVENTION_TYPE_TOK_EXTERN                                            =  184;  // <convention_type> ::= 'TOK_EXTERN'
		final int PROD_CONVENTION_TYPE_STDCALL                                               =  185;  // <convention_type> ::= STDCALL
		final int PROD__INTERMEDIATE_ROUNDING_CLAUSE                                         =  186;  // <_intermediate_rounding_clause> ::= 
		final int PROD__INTERMEDIATE_ROUNDING_CLAUSE_INTERMEDIATE_ROUNDING                   =  187;  // <_intermediate_rounding_clause> ::= INTERMEDIATE ROUNDING <_is> <intermediate_rounding_choice>
		final int PROD_INTERMEDIATE_ROUNDING_CHOICE_NEAREST_AWAY_FROM_ZERO                   =  188;  // <intermediate_rounding_choice> ::= 'NEAREST_AWAY_FROM_ZERO'
		final int PROD_INTERMEDIATE_ROUNDING_CHOICE_NEAREST_EVEN                             =  189;  // <intermediate_rounding_choice> ::= 'NEAREST_EVEN'
		final int PROD_INTERMEDIATE_ROUNDING_CHOICE_PROHIBITED                               =  190;  // <intermediate_rounding_choice> ::= PROHIBITED
		final int PROD_INTERMEDIATE_ROUNDING_CHOICE_TRUNCATION                               =  191;  // <intermediate_rounding_choice> ::= TRUNCATION
		final int PROD__ENVIRONMENT_DIVISION                                                 =  192;  // <_environment_division> ::= <_environment_header> <_configuration_section> <_input_output_section>
		final int PROD__ENVIRONMENT_HEADER                                                   =  193;  // <_environment_header> ::= 
		final int PROD__ENVIRONMENT_HEADER_ENVIRONMENT_DIVISION_TOK_DOT                      =  194;  // <_environment_header> ::= ENVIRONMENT DIVISION 'TOK_DOT'
		final int PROD__CONFIGURATION_SECTION                                                =  195;  // <_configuration_section> ::= <_configuration_header> <_source_object_computer_paragraphs> <_special_names_paragraph> <_special_names_sentence_list> <_repository_paragraph>
		final int PROD__CONFIGURATION_HEADER                                                 =  196;  // <_configuration_header> ::= 
		final int PROD__CONFIGURATION_HEADER_CONFIGURATION_SECTION_TOK_DOT                   =  197;  // <_configuration_header> ::= CONFIGURATION SECTION 'TOK_DOT'
		final int PROD__SOURCE_OBJECT_COMPUTER_PARAGRAPHS                                    =  198;  // <_source_object_computer_paragraphs> ::= 
		final int PROD__SOURCE_OBJECT_COMPUTER_PARAGRAPHS2                                   =  199;  // <_source_object_computer_paragraphs> ::= <source_computer_paragraph>
		final int PROD__SOURCE_OBJECT_COMPUTER_PARAGRAPHS3                                   =  200;  // <_source_object_computer_paragraphs> ::= <object_computer_paragraph>
		final int PROD__SOURCE_OBJECT_COMPUTER_PARAGRAPHS4                                   =  201;  // <_source_object_computer_paragraphs> ::= <source_computer_paragraph> <object_computer_paragraph>
		final int PROD__SOURCE_OBJECT_COMPUTER_PARAGRAPHS5                                   =  202;  // <_source_object_computer_paragraphs> ::= <object_computer_paragraph> <source_computer_paragraph>
		final int PROD_SOURCE_COMPUTER_PARAGRAPH_SOURCE_COMPUTER_TOK_DOT                     =  203;  // <source_computer_paragraph> ::= 'SOURCE_COMPUTER' 'TOK_DOT' <_source_computer_entry>
		final int PROD__SOURCE_COMPUTER_ENTRY                                                =  204;  // <_source_computer_entry> ::= 
		final int PROD__SOURCE_COMPUTER_ENTRY_TOK_DOT                                        =  205;  // <_source_computer_entry> ::= <computer_words> <_with_debugging_mode> 'TOK_DOT'
		final int PROD__WITH_DEBUGGING_MODE                                                  =  206;  // <_with_debugging_mode> ::= 
		final int PROD__WITH_DEBUGGING_MODE_DEBUGGING_MODE                                   =  207;  // <_with_debugging_mode> ::= <_with> DEBUGGING MODE
		final int PROD_OBJECT_COMPUTER_PARAGRAPH_OBJECT_COMPUTER_TOK_DOT                     =  208;  // <object_computer_paragraph> ::= 'OBJECT_COMPUTER' 'TOK_DOT' <_object_computer_entry>
		final int PROD__OBJECT_COMPUTER_ENTRY                                                =  209;  // <_object_computer_entry> ::= 
		final int PROD__OBJECT_COMPUTER_ENTRY_TOK_DOT                                        =  210;  // <_object_computer_entry> ::= <computer_words> 'TOK_DOT'
		final int PROD__OBJECT_COMPUTER_ENTRY_TOK_DOT2                                       =  211;  // <_object_computer_entry> ::= <computer_words> <object_clauses_list> 'TOK_DOT'
		final int PROD__OBJECT_COMPUTER_ENTRY_TOK_DOT3                                       =  212;  // <_object_computer_entry> ::= <object_clauses_list> 'TOK_DOT'
		final int PROD_OBJECT_CLAUSES_LIST                                                   =  213;  // <object_clauses_list> ::= <object_clauses>
		final int PROD_OBJECT_CLAUSES_LIST2                                                  =  214;  // <object_clauses_list> ::= <object_clauses_list> <object_clauses>
		final int PROD_OBJECT_CLAUSES                                                        =  215;  // <object_clauses> ::= <object_computer_memory>
		final int PROD_OBJECT_CLAUSES2                                                       =  216;  // <object_clauses> ::= <object_computer_sequence>
		final int PROD_OBJECT_CLAUSES3                                                       =  217;  // <object_clauses> ::= <object_computer_segment>
		final int PROD_OBJECT_CLAUSES4                                                       =  218;  // <object_clauses> ::= <object_computer_class>
		final int PROD_OBJECT_COMPUTER_MEMORY_MEMORY_SIZE                                    =  219;  // <object_computer_memory> ::= MEMORY SIZE <_is> <integer> <object_char_or_word>
		final int PROD_OBJECT_COMPUTER_SEQUENCE                                              =  220;  // <object_computer_sequence> ::= <prog_coll_sequence> <_is> <single_reference>
		final int PROD_OBJECT_COMPUTER_SEGMENT_SEGMENT_LIMIT                                 =  221;  // <object_computer_segment> ::= 'SEGMENT_LIMIT' <_is> <integer>
		final int PROD_OBJECT_COMPUTER_CLASS_CLASSIFICATION                                  =  222;  // <object_computer_class> ::= <_character> CLASSIFICATION <_is> <locale_class>
		final int PROD_LOCALE_CLASS                                                          =  223;  // <locale_class> ::= <single_reference>
		final int PROD_LOCALE_CLASS_LOCALE                                                   =  224;  // <locale_class> ::= LOCALE
		final int PROD_LOCALE_CLASS_USER_DEFAULT                                             =  225;  // <locale_class> ::= 'USER_DEFAULT'
		final int PROD_LOCALE_CLASS_SYSTEM_DEFAULT                                           =  226;  // <locale_class> ::= 'SYSTEM_DEFAULT'
		final int PROD_COMPUTER_WORDS                                                        =  227;  // <computer_words> ::= <WORD>
		final int PROD_COMPUTER_WORDS2                                                       =  228;  // <computer_words> ::= <computer_words> <WORD>
		final int PROD__REPOSITORY_PARAGRAPH                                                 =  229;  // <_repository_paragraph> ::= 
		final int PROD__REPOSITORY_PARAGRAPH_REPOSITORY_TOK_DOT                              =  230;  // <_repository_paragraph> ::= REPOSITORY 'TOK_DOT' <_repository_entry>
		final int PROD__REPOSITORY_ENTRY                                                     =  231;  // <_repository_entry> ::= 
		final int PROD__REPOSITORY_ENTRY_TOK_DOT                                             =  232;  // <_repository_entry> ::= <repository_list> 'TOK_DOT'
		final int PROD_REPOSITORY_LIST                                                       =  233;  // <repository_list> ::= <repository_name>
		final int PROD_REPOSITORY_LIST2                                                      =  234;  // <repository_list> ::= <repository_list> <repository_name>
		final int PROD_REPOSITORY_NAME_FUNCTION_ALL_INTRINSIC                                =  235;  // <repository_name> ::= FUNCTION ALL INTRINSIC
		final int PROD_REPOSITORY_NAME_FUNCTION                                              =  236;  // <repository_name> ::= FUNCTION <WORD> <_as_literal>
		final int PROD_REPOSITORY_NAME_FUNCTION_INTRINSIC                                    =  237;  // <repository_name> ::= FUNCTION <repository_name_list> INTRINSIC
		final int PROD_REPOSITORY_NAME_PROGRAM                                               =  238;  // <repository_name> ::= PROGRAM <WORD> <_as_literal>
		final int PROD_REPOSITORY_NAME_LIST                                                  =  239;  // <repository_name_list> ::= <FUNCTION_NAME>
		final int PROD_REPOSITORY_NAME_LIST2                                                 =  240;  // <repository_name_list> ::= <repository_name_list> <FUNCTION_NAME>
		final int PROD__SPECIAL_NAMES_PARAGRAPH                                              =  241;  // <_special_names_paragraph> ::= 
		final int PROD__SPECIAL_NAMES_PARAGRAPH_SPECIAL_NAMES_TOK_DOT                        =  242;  // <_special_names_paragraph> ::= 'SPECIAL_NAMES' 'TOK_DOT'
		final int PROD__SPECIAL_NAMES_SENTENCE_LIST                                          =  243;  // <_special_names_sentence_list> ::= 
		final int PROD__SPECIAL_NAMES_SENTENCE_LIST2                                         =  244;  // <_special_names_sentence_list> ::= <special_names_sentence_list>
		final int PROD_SPECIAL_NAMES_SENTENCE_LIST_TOK_DOT                                   =  245;  // <special_names_sentence_list> ::= <special_name_list> 'TOK_DOT'
		final int PROD_SPECIAL_NAMES_SENTENCE_LIST_TOK_DOT2                                  =  246;  // <special_names_sentence_list> ::= <special_names_sentence_list> <special_name_list> 'TOK_DOT'
		final int PROD_SPECIAL_NAME_LIST                                                     =  247;  // <special_name_list> ::= <special_name>
		final int PROD_SPECIAL_NAME_LIST2                                                    =  248;  // <special_name_list> ::= <special_name_list> <special_name>
		final int PROD_SPECIAL_NAME                                                          =  249;  // <special_name> ::= <mnemonic_name_clause>
		final int PROD_SPECIAL_NAME2                                                         =  250;  // <special_name> ::= <alphabet_name_clause>
		final int PROD_SPECIAL_NAME3                                                         =  251;  // <special_name> ::= <symbolic_characters_clause>
		final int PROD_SPECIAL_NAME4                                                         =  252;  // <special_name> ::= <locale_clause>
		final int PROD_SPECIAL_NAME5                                                         =  253;  // <special_name> ::= <class_name_clause>
		final int PROD_SPECIAL_NAME6                                                         =  254;  // <special_name> ::= <currency_sign_clause>
		final int PROD_SPECIAL_NAME7                                                         =  255;  // <special_name> ::= <decimal_point_clause>
		final int PROD_SPECIAL_NAME8                                                         =  256;  // <special_name> ::= <numeric_sign_clause>
		final int PROD_SPECIAL_NAME9                                                         =  257;  // <special_name> ::= <cursor_clause>
		final int PROD_SPECIAL_NAME10                                                        =  258;  // <special_name> ::= <crt_status_clause>
		final int PROD_SPECIAL_NAME11                                                        =  259;  // <special_name> ::= <screen_control>
		final int PROD_SPECIAL_NAME12                                                        =  260;  // <special_name> ::= <event_status>
		final int PROD_SPECIAL_NAME_COMMA_DELIM                                              =  261;  // <special_name> ::= 'COMMA_DELIM'
		final int PROD_MNEMONIC_NAME_CLAUSE                                                  =  262;  // <mnemonic_name_clause> ::= <WORD> <mnemonic_choices>
		final int PROD_MNEMONIC_CHOICES_CRT                                                  =  263;  // <mnemonic_choices> ::= <_is> CRT
		final int PROD_MNEMONIC_CHOICES                                                      =  264;  // <mnemonic_choices> ::= <integer> <_is> <undefined_word>
		final int PROD_MNEMONIC_CHOICES2                                                     =  265;  // <mnemonic_choices> ::= <_is> <undefined_word> <_special_name_mnemonic_on_off>
		final int PROD_MNEMONIC_CHOICES3                                                     =  266;  // <mnemonic_choices> ::= <on_off_clauses>
		final int PROD__SPECIAL_NAME_MNEMONIC_ON_OFF                                         =  267;  // <_special_name_mnemonic_on_off> ::= 
		final int PROD__SPECIAL_NAME_MNEMONIC_ON_OFF2                                        =  268;  // <_special_name_mnemonic_on_off> ::= <on_off_clauses>
		final int PROD_ON_OFF_CLAUSES                                                        =  269;  // <on_off_clauses> ::= <on_off_clauses_1>
		final int PROD_ON_OFF_CLAUSES_1                                                      =  270;  // <on_off_clauses_1> ::= <on_or_off> <_onoff_status> <undefined_word>
		final int PROD_ON_OFF_CLAUSES_12                                                     =  271;  // <on_off_clauses_1> ::= <on_off_clauses_1> <on_or_off> <_onoff_status> <undefined_word>
		final int PROD_ALPHABET_NAME_CLAUSE_ALPHABET                                         =  272;  // <alphabet_name_clause> ::= ALPHABET <undefined_word> <_is> <alphabet_definition>
		final int PROD_ALPHABET_DEFINITION_NATIVE                                            =  273;  // <alphabet_definition> ::= NATIVE
		final int PROD_ALPHABET_DEFINITION_STANDARD_1                                        =  274;  // <alphabet_definition> ::= 'STANDARD_1'
		final int PROD_ALPHABET_DEFINITION_STANDARD_2                                        =  275;  // <alphabet_definition> ::= 'STANDARD_2'
		final int PROD_ALPHABET_DEFINITION_EBCDIC                                            =  276;  // <alphabet_definition> ::= EBCDIC
		final int PROD_ALPHABET_DEFINITION_ASCII                                             =  277;  // <alphabet_definition> ::= ASCII
		final int PROD_ALPHABET_DEFINITION                                                   =  278;  // <alphabet_definition> ::= <alphabet_literal_list>
		final int PROD_ALPHABET_LITERAL_LIST                                                 =  279;  // <alphabet_literal_list> ::= <alphabet_literal>
		final int PROD_ALPHABET_LITERAL_LIST2                                                =  280;  // <alphabet_literal_list> ::= <alphabet_literal_list> <alphabet_literal>
		final int PROD_ALPHABET_LITERAL                                                      =  281;  // <alphabet_literal> ::= <alphabet_lits>
		final int PROD_ALPHABET_LITERAL_THRU                                                 =  282;  // <alphabet_literal> ::= <alphabet_lits> THRU <alphabet_lits>
		final int PROD_ALPHABET_LITERAL_ALSO                                                 =  283;  // <alphabet_literal> ::= <alphabet_lits> ALSO <alphabet_also_sequence>
		final int PROD_ALPHABET_ALSO_SEQUENCE                                                =  284;  // <alphabet_also_sequence> ::= <alphabet_lits>
		final int PROD_ALPHABET_ALSO_SEQUENCE_ALSO                                           =  285;  // <alphabet_also_sequence> ::= <alphabet_also_sequence> ALSO <alphabet_lits>
		final int PROD_ALPHABET_LITS                                                         =  286;  // <alphabet_lits> ::= <LITERAL_TOK>
		final int PROD_ALPHABET_LITS_SPACE                                                   =  287;  // <alphabet_lits> ::= SPACE
		final int PROD_ALPHABET_LITS_ZERO                                                    =  288;  // <alphabet_lits> ::= ZERO
		final int PROD_ALPHABET_LITS_QUOTE                                                   =  289;  // <alphabet_lits> ::= QUOTE
		final int PROD_ALPHABET_LITS_HIGH_VALUE                                              =  290;  // <alphabet_lits> ::= 'HIGH_VALUE'
		final int PROD_ALPHABET_LITS_LOW_VALUE                                               =  291;  // <alphabet_lits> ::= 'LOW_VALUE'
		final int PROD_SPACE_OR_ZERO_SPACE                                                   =  292;  // <space_or_zero> ::= SPACE
		final int PROD_SPACE_OR_ZERO_ZERO                                                    =  293;  // <space_or_zero> ::= ZERO
		final int PROD_SYMBOLIC_CHARACTERS_CLAUSE                                            =  294;  // <symbolic_characters_clause> ::= <symbolic_collection> <_sym_in_word>
		final int PROD__SYM_IN_WORD                                                          =  295;  // <_sym_in_word> ::= 
		final int PROD__SYM_IN_WORD_IN                                                       =  296;  // <_sym_in_word> ::= IN <WORD>
		final int PROD_SYMBOLIC_COLLECTION_SYMBOLIC                                          =  297;  // <symbolic_collection> ::= SYMBOLIC <_characters> <symbolic_chars_list>
		final int PROD_SYMBOLIC_CHARS_LIST                                                   =  298;  // <symbolic_chars_list> ::= <symbolic_chars_phrase>
		final int PROD_SYMBOLIC_CHARS_LIST2                                                  =  299;  // <symbolic_chars_list> ::= <symbolic_chars_list> <symbolic_chars_phrase>
		final int PROD_SYMBOLIC_CHARS_PHRASE                                                 =  300;  // <symbolic_chars_phrase> ::= <char_list> <_is_are> <integer_list>
		final int PROD_CHAR_LIST                                                             =  301;  // <char_list> ::= <unique_word>
		final int PROD_CHAR_LIST2                                                            =  302;  // <char_list> ::= <char_list> <unique_word>
		final int PROD_INTEGER_LIST                                                          =  303;  // <integer_list> ::= <symbolic_integer>
		final int PROD_INTEGER_LIST2                                                         =  304;  // <integer_list> ::= <integer_list> <symbolic_integer>
		final int PROD_CLASS_NAME_CLAUSE_CLASS                                               =  305;  // <class_name_clause> ::= CLASS <undefined_word> <_is> <class_item_list>
		final int PROD_CLASS_ITEM_LIST                                                       =  306;  // <class_item_list> ::= <class_item>
		final int PROD_CLASS_ITEM_LIST2                                                      =  307;  // <class_item_list> ::= <class_item_list> <class_item>
		final int PROD_CLASS_ITEM                                                            =  308;  // <class_item> ::= <class_value>
		final int PROD_CLASS_ITEM_THRU                                                       =  309;  // <class_item> ::= <class_value> THRU <class_value>
		final int PROD_LOCALE_CLAUSE_LOCALE                                                  =  310;  // <locale_clause> ::= LOCALE <undefined_word> <_is> <LITERAL_TOK>
		final int PROD_CURRENCY_SIGN_CLAUSE_CURRENCY                                         =  311;  // <currency_sign_clause> ::= CURRENCY <_sign> <_is> <LITERAL_TOK> <_with_pic_symbol>
		final int PROD__WITH_PIC_SYMBOL                                                      =  312;  // <_with_pic_symbol> ::= 
		final int PROD__WITH_PIC_SYMBOL_PICTURE_SYMBOL                                       =  313;  // <_with_pic_symbol> ::= <_with> 'PICTURE_SYMBOL' <LITERAL_TOK>
		final int PROD_DECIMAL_POINT_CLAUSE_DECIMAL_POINT_COMMA                              =  314;  // <decimal_point_clause> ::= 'DECIMAL_POINT' <_is> COMMA
		final int PROD_NUMERIC_SIGN_CLAUSE_NUMERIC_SIGN_TRAILING_SEPARATE                    =  315;  // <numeric_sign_clause> ::= NUMERIC SIGN <_is> TRAILING SEPARATE
		final int PROD_CURSOR_CLAUSE_CURSOR                                                  =  316;  // <cursor_clause> ::= CURSOR <_is> <reference>
		final int PROD_CRT_STATUS_CLAUSE_CRT_STATUS                                          =  317;  // <crt_status_clause> ::= CRT STATUS <_is> <reference>
		final int PROD_SCREEN_CONTROL_SCREEN_CONTROL                                         =  318;  // <screen_control> ::= 'SCREEN_CONTROL' <_is> <reference>
		final int PROD_EVENT_STATUS_EVENT_STATUS                                             =  319;  // <event_status> ::= 'EVENT_STATUS' <_is> <reference>
		final int PROD__INPUT_OUTPUT_SECTION                                                 =  320;  // <_input_output_section> ::= <_input_output_header> <_file_control_header> <_file_control_sequence> <_i_o_control_header> <_i_o_control>
		final int PROD__INPUT_OUTPUT_HEADER                                                  =  321;  // <_input_output_header> ::= 
		final int PROD__INPUT_OUTPUT_HEADER_INPUT_OUTPUT_SECTION_TOK_DOT                     =  322;  // <_input_output_header> ::= 'INPUT_OUTPUT' SECTION 'TOK_DOT'
		final int PROD__FILE_CONTROL_HEADER                                                  =  323;  // <_file_control_header> ::= 
		final int PROD__FILE_CONTROL_HEADER_FILE_CONTROL_TOK_DOT                             =  324;  // <_file_control_header> ::= 'FILE_CONTROL' 'TOK_DOT'
		final int PROD__I_O_CONTROL_HEADER                                                   =  325;  // <_i_o_control_header> ::= 
		final int PROD__I_O_CONTROL_HEADER_I_O_CONTROL_TOK_DOT                               =  326;  // <_i_o_control_header> ::= 'I_O_CONTROL' 'TOK_DOT'
		final int PROD__FILE_CONTROL_SEQUENCE                                                =  327;  // <_file_control_sequence> ::= 
		final int PROD__FILE_CONTROL_SEQUENCE2                                               =  328;  // <_file_control_sequence> ::= <_file_control_sequence> <file_control_entry>
		final int PROD_FILE_CONTROL_ENTRY_SELECT                                             =  329;  // <file_control_entry> ::= SELECT <flag_optional> <undefined_word> <_select_clauses_or_error>
		final int PROD__SELECT_CLAUSES_OR_ERROR_TOK_DOT                                      =  330;  // <_select_clauses_or_error> ::= <_select_clause_sequence> 'TOK_DOT'
		final int PROD__SELECT_CLAUSE_SEQUENCE                                               =  331;  // <_select_clause_sequence> ::= 
		final int PROD__SELECT_CLAUSE_SEQUENCE2                                              =  332;  // <_select_clause_sequence> ::= <_select_clause_sequence> <select_clause>
		final int PROD_SELECT_CLAUSE                                                         =  333;  // <select_clause> ::= <assign_clause>
		final int PROD_SELECT_CLAUSE2                                                        =  334;  // <select_clause> ::= <access_mode_clause>
		final int PROD_SELECT_CLAUSE3                                                        =  335;  // <select_clause> ::= <alternative_record_key_clause>
		final int PROD_SELECT_CLAUSE4                                                        =  336;  // <select_clause> ::= <collating_sequence_clause>
		final int PROD_SELECT_CLAUSE5                                                        =  337;  // <select_clause> ::= <file_status_clause>
		final int PROD_SELECT_CLAUSE6                                                        =  338;  // <select_clause> ::= <lock_mode_clause>
		final int PROD_SELECT_CLAUSE7                                                        =  339;  // <select_clause> ::= <organization_clause>
		final int PROD_SELECT_CLAUSE8                                                        =  340;  // <select_clause> ::= <padding_character_clause>
		final int PROD_SELECT_CLAUSE9                                                        =  341;  // <select_clause> ::= <record_delimiter_clause>
		final int PROD_SELECT_CLAUSE10                                                       =  342;  // <select_clause> ::= <record_key_clause>
		final int PROD_SELECT_CLAUSE11                                                       =  343;  // <select_clause> ::= <relative_key_clause>
		final int PROD_SELECT_CLAUSE12                                                       =  344;  // <select_clause> ::= <reserve_clause>
		final int PROD_SELECT_CLAUSE13                                                       =  345;  // <select_clause> ::= <sharing_clause>
		final int PROD_ASSIGN_CLAUSE_ASSIGN                                                  =  346;  // <assign_clause> ::= ASSIGN <_to_using> <_ext_clause> <_line_adv_file> <assignment_name>
		final int PROD_ASSIGN_CLAUSE_ASSIGN2                                                 =  347;  // <assign_clause> ::= ASSIGN <_to_using> <_ext_clause> <general_device_name> <_assignment_name>
		final int PROD_ASSIGN_CLAUSE_ASSIGN3                                                 =  348;  // <assign_clause> ::= ASSIGN <_to_using> <_ext_clause> <line_seq_device_name> <_assignment_name>
		final int PROD_ASSIGN_CLAUSE_ASSIGN_DISPLAY                                          =  349;  // <assign_clause> ::= ASSIGN <_to_using> <_ext_clause> DISPLAY <_assignment_name>
		final int PROD_ASSIGN_CLAUSE_ASSIGN_KEYBOARD                                         =  350;  // <assign_clause> ::= ASSIGN <_to_using> <_ext_clause> KEYBOARD <_assignment_name>
		final int PROD_ASSIGN_CLAUSE_ASSIGN4                                                 =  351;  // <assign_clause> ::= ASSIGN <_to_using> <_ext_clause> <printer_name> <_assignment_name>
		final int PROD_PRINTER_NAME_PRINTER                                                  =  352;  // <printer_name> ::= PRINTER
		final int PROD_PRINTER_NAME_PRINTER_1                                                =  353;  // <printer_name> ::= 'PRINTER_1'
		final int PROD_PRINTER_NAME_PRINT                                                    =  354;  // <printer_name> ::= PRINT
		final int PROD_GENERAL_DEVICE_NAME_DISC                                              =  355;  // <general_device_name> ::= DISC
		final int PROD_GENERAL_DEVICE_NAME_DISK                                              =  356;  // <general_device_name> ::= DISK
		final int PROD_GENERAL_DEVICE_NAME_TAPE                                              =  357;  // <general_device_name> ::= TAPE
		final int PROD_GENERAL_DEVICE_NAME_RANDOM                                            =  358;  // <general_device_name> ::= RANDOM
		final int PROD_LINE_SEQ_DEVICE_NAME_CARD_PUNCH                                       =  359;  // <line_seq_device_name> ::= 'CARD_PUNCH'
		final int PROD_LINE_SEQ_DEVICE_NAME_CARD_READER                                      =  360;  // <line_seq_device_name> ::= 'CARD_READER'
		final int PROD_LINE_SEQ_DEVICE_NAME_CASSETTE                                         =  361;  // <line_seq_device_name> ::= CASSETTE
		final int PROD_LINE_SEQ_DEVICE_NAME_INPUT                                            =  362;  // <line_seq_device_name> ::= INPUT
		final int PROD_LINE_SEQ_DEVICE_NAME_INPUT_OUTPUT                                     =  363;  // <line_seq_device_name> ::= 'INPUT_OUTPUT'
		final int PROD_LINE_SEQ_DEVICE_NAME_MAGNETIC_TAPE                                    =  364;  // <line_seq_device_name> ::= 'MAGNETIC_TAPE'
		final int PROD_LINE_SEQ_DEVICE_NAME_OUTPUT                                           =  365;  // <line_seq_device_name> ::= OUTPUT
		final int PROD__LINE_ADV_FILE                                                        =  366;  // <_line_adv_file> ::= 
		final int PROD__LINE_ADV_FILE_LINE_ADVANCING                                         =  367;  // <_line_adv_file> ::= LINE ADVANCING <_file>
		final int PROD__EXT_CLAUSE                                                           =  368;  // <_ext_clause> ::= 
		final int PROD__EXT_CLAUSE_EXTERNAL                                                  =  369;  // <_ext_clause> ::= EXTERNAL
		final int PROD__EXT_CLAUSE_DYNAMIC                                                   =  370;  // <_ext_clause> ::= DYNAMIC
		final int PROD_ASSIGNMENT_NAME                                                       =  371;  // <assignment_name> ::= <LITERAL_TOK>
		final int PROD_ASSIGNMENT_NAME2                                                      =  372;  // <assignment_name> ::= <qualified_word>
		final int PROD__ASSIGNMENT_NAME                                                      =  373;  // <_assignment_name> ::= 
		final int PROD__ASSIGNMENT_NAME2                                                     =  374;  // <_assignment_name> ::= <LITERAL_TOK>
		final int PROD__ASSIGNMENT_NAME3                                                     =  375;  // <_assignment_name> ::= <qualified_word>
		final int PROD_ACCESS_MODE_CLAUSE_ACCESS                                             =  376;  // <access_mode_clause> ::= ACCESS <_mode> <_is> <access_mode>
		final int PROD_ACCESS_MODE_SEQUENTIAL                                                =  377;  // <access_mode> ::= SEQUENTIAL
		final int PROD_ACCESS_MODE_DYNAMIC                                                   =  378;  // <access_mode> ::= DYNAMIC
		final int PROD_ACCESS_MODE_RANDOM                                                    =  379;  // <access_mode> ::= RANDOM
		final int PROD_ALTERNATIVE_RECORD_KEY_CLAUSE_ALTERNATE                               =  380;  // <alternative_record_key_clause> ::= ALTERNATE <_record> <_key> <_is> <key_or_split_keys> <flag_duplicates> <_suppress_clause>
		final int PROD__SUPPRESS_CLAUSE                                                      =  381;  // <_suppress_clause> ::= 
		final int PROD__SUPPRESS_CLAUSE_SUPPRESS_WHEN_ALL                                    =  382;  // <_suppress_clause> ::= SUPPRESS WHEN ALL <basic_value>
		final int PROD__SUPPRESS_CLAUSE_SUPPRESS_WHEN                                        =  383;  // <_suppress_clause> ::= SUPPRESS WHEN <space_or_zero>
		final int PROD_COLLATING_SEQUENCE_CLAUSE                                             =  384;  // <collating_sequence_clause> ::= <coll_sequence> <_is> <alphabet_name>
		final int PROD_ALPHABET_NAME                                                         =  385;  // <alphabet_name> ::= <WORD>
		final int PROD_FILE_STATUS_CLAUSE_STATUS                                             =  386;  // <file_status_clause> ::= <_file_or_sort> STATUS <_is> <reference>
		final int PROD__FILE_OR_SORT                                                         =  387;  // <_file_or_sort> ::= 
		final int PROD__FILE_OR_SORT_TOK_FILE                                                =  388;  // <_file_or_sort> ::= 'TOK_FILE'
		final int PROD__FILE_OR_SORT_SORT                                                    =  389;  // <_file_or_sort> ::= SORT
		final int PROD_LOCK_MODE_CLAUSE_LOCK                                                 =  390;  // <lock_mode_clause> ::= LOCK <_mode> <_is> <lock_mode>
		final int PROD_LOCK_MODE_MANUAL                                                      =  391;  // <lock_mode> ::= MANUAL <_lock_with>
		final int PROD_LOCK_MODE_AUTOMATIC                                                   =  392;  // <lock_mode> ::= AUTOMATIC <_lock_with>
		final int PROD_LOCK_MODE_EXCLUSIVE                                                   =  393;  // <lock_mode> ::= EXCLUSIVE
		final int PROD__LOCK_WITH                                                            =  394;  // <_lock_with> ::= 
		final int PROD__LOCK_WITH_WITH_LOCK_ON                                               =  395;  // <_lock_with> ::= WITH LOCK ON <lock_records>
		final int PROD__LOCK_WITH_WITH_LOCK_ON_MULTIPLE                                      =  396;  // <_lock_with> ::= WITH LOCK ON MULTIPLE <lock_records>
		final int PROD__LOCK_WITH_WITH_ROLLBACK                                              =  397;  // <_lock_with> ::= WITH ROLLBACK
		final int PROD_ORGANIZATION_CLAUSE_ORGANIZATION                                      =  398;  // <organization_clause> ::= ORGANIZATION <_is> <organization>
		final int PROD_ORGANIZATION_CLAUSE                                                   =  399;  // <organization_clause> ::= <organization>
		final int PROD_ORGANIZATION_INDEXED                                                  =  400;  // <organization> ::= INDEXED
		final int PROD_ORGANIZATION_SEQUENTIAL                                               =  401;  // <organization> ::= <_record> <_binary> SEQUENTIAL
		final int PROD_ORGANIZATION_RELATIVE                                                 =  402;  // <organization> ::= RELATIVE
		final int PROD_ORGANIZATION_LINE_SEQUENTIAL                                          =  403;  // <organization> ::= LINE SEQUENTIAL
		final int PROD_PADDING_CHARACTER_CLAUSE_PADDING                                      =  404;  // <padding_character_clause> ::= PADDING <_character> <_is> <reference_or_literal>
		final int PROD_RECORD_DELIMITER_CLAUSE_RECORD_DELIMITER_STANDARD_1                   =  405;  // <record_delimiter_clause> ::= RECORD DELIMITER <_is> 'STANDARD_1'
		final int PROD_RECORD_KEY_CLAUSE_RECORD                                              =  406;  // <record_key_clause> ::= RECORD <_key> <_is> <key_or_split_keys>
		final int PROD_KEY_OR_SPLIT_KEYS                                                     =  407;  // <key_or_split_keys> ::= <reference>
		final int PROD_KEY_OR_SPLIT_KEYS_TOK_EQUAL                                           =  408;  // <key_or_split_keys> ::= <reference> 'TOK_EQUAL' <reference_list>
		final int PROD_KEY_OR_SPLIT_KEYS_SOURCE                                              =  409;  // <key_or_split_keys> ::= <reference> SOURCE <_is> <reference_list>
		final int PROD_RELATIVE_KEY_CLAUSE_RELATIVE                                          =  410;  // <relative_key_clause> ::= RELATIVE <_key> <_is> <reference>
		final int PROD_RESERVE_CLAUSE_RESERVE                                                =  411;  // <reserve_clause> ::= RESERVE <no_or_integer> <_areas>
		final int PROD_NO_OR_INTEGER_NO                                                      =  412;  // <no_or_integer> ::= NO
		final int PROD_NO_OR_INTEGER                                                         =  413;  // <no_or_integer> ::= <integer>
		final int PROD_SHARING_CLAUSE_SHARING                                                =  414;  // <sharing_clause> ::= SHARING <_with> <sharing_option>
		final int PROD_SHARING_OPTION_ALL                                                    =  415;  // <sharing_option> ::= ALL <_other>
		final int PROD_SHARING_OPTION_NO                                                     =  416;  // <sharing_option> ::= NO <_other>
		final int PROD_SHARING_OPTION_READ_ONLY                                              =  417;  // <sharing_option> ::= READ ONLY
		final int PROD__I_O_CONTROL                                                          =  418;  // <_i_o_control> ::= 
		final int PROD__I_O_CONTROL_TOK_DOT                                                  =  419;  // <_i_o_control> ::= <i_o_control_list> 'TOK_DOT'
		final int PROD_I_O_CONTROL_LIST                                                      =  420;  // <i_o_control_list> ::= <i_o_control_clause>
		final int PROD_I_O_CONTROL_LIST2                                                     =  421;  // <i_o_control_list> ::= <i_o_control_list> <i_o_control_clause>
		final int PROD_I_O_CONTROL_CLAUSE                                                    =  422;  // <i_o_control_clause> ::= <same_clause>
		final int PROD_I_O_CONTROL_CLAUSE2                                                   =  423;  // <i_o_control_clause> ::= <multiple_file_tape_clause>
		final int PROD_SAME_CLAUSE_SAME                                                      =  424;  // <same_clause> ::= SAME <_same_option> <_area> <_for> <file_name_list>
		final int PROD__SAME_OPTION                                                          =  425;  // <_same_option> ::= 
		final int PROD__SAME_OPTION_RECORD                                                   =  426;  // <_same_option> ::= RECORD
		final int PROD__SAME_OPTION_SORT                                                     =  427;  // <_same_option> ::= SORT
		final int PROD__SAME_OPTION_SORT_MERGE                                               =  428;  // <_same_option> ::= 'SORT_MERGE'
		final int PROD_MULTIPLE_FILE_TAPE_CLAUSE_MULTIPLE                                    =  429;  // <multiple_file_tape_clause> ::= MULTIPLE <_file> <_tape> <_contains> <multiple_file_list>
		final int PROD_MULTIPLE_FILE_LIST                                                    =  430;  // <multiple_file_list> ::= <multiple_file>
		final int PROD_MULTIPLE_FILE_LIST2                                                   =  431;  // <multiple_file_list> ::= <multiple_file_list> <multiple_file>
		final int PROD_MULTIPLE_FILE                                                         =  432;  // <multiple_file> ::= <file_name> <_multiple_file_position>
		final int PROD__MULTIPLE_FILE_POSITION                                               =  433;  // <_multiple_file_position> ::= 
		final int PROD__MULTIPLE_FILE_POSITION_POSITION                                      =  434;  // <_multiple_file_position> ::= POSITION <integer>
		final int PROD__DATA_DIVISION                                                        =  435;  // <_data_division> ::= <_data_division_header> <_file_section_header> <_file_description_sequence> <_working_storage_section> <_communication_section> <_local_storage_section> <_linkage_section> <_report_section> <_screen_section>
		final int PROD__DATA_DIVISION_HEADER                                                 =  436;  // <_data_division_header> ::= 
		final int PROD__DATA_DIVISION_HEADER_DATA_DIVISION_TOK_DOT                           =  437;  // <_data_division_header> ::= DATA DIVISION 'TOK_DOT'
		final int PROD__FILE_SECTION_HEADER                                                  =  438;  // <_file_section_header> ::= 
		final int PROD__FILE_SECTION_HEADER_TOK_FILE_SECTION_TOK_DOT                         =  439;  // <_file_section_header> ::= 'TOK_FILE' SECTION 'TOK_DOT'
		final int PROD__FILE_DESCRIPTION_SEQUENCE                                            =  440;  // <_file_description_sequence> ::= 
		final int PROD__FILE_DESCRIPTION_SEQUENCE2                                           =  441;  // <_file_description_sequence> ::= <_file_description_sequence> <file_description>
		final int PROD_FILE_DESCRIPTION                                                      =  442;  // <file_description> ::= <file_description_entry> <_record_description_list>
		final int PROD_FILE_DESCRIPTION_ENTRY_TOK_DOT                                        =  443;  // <file_description_entry> ::= <file_type> <file_name> <_file_description_clause_sequence> 'TOK_DOT'
		final int PROD_FILE_TYPE_FD                                                          =  444;  // <file_type> ::= FD
		final int PROD_FILE_TYPE_SD                                                          =  445;  // <file_type> ::= SD
		final int PROD__FILE_DESCRIPTION_CLAUSE_SEQUENCE                                     =  446;  // <_file_description_clause_sequence> ::= 
		final int PROD__FILE_DESCRIPTION_CLAUSE_SEQUENCE2                                    =  447;  // <_file_description_clause_sequence> ::= <_file_description_clause_sequence> <file_description_clause>
		final int PROD_FILE_DESCRIPTION_CLAUSE_EXTERNAL                                      =  448;  // <file_description_clause> ::= <_is> EXTERNAL
		final int PROD_FILE_DESCRIPTION_CLAUSE_GLOBAL                                        =  449;  // <file_description_clause> ::= <_is> GLOBAL
		final int PROD_FILE_DESCRIPTION_CLAUSE                                               =  450;  // <file_description_clause> ::= <block_contains_clause>
		final int PROD_FILE_DESCRIPTION_CLAUSE2                                              =  451;  // <file_description_clause> ::= <record_clause>
		final int PROD_FILE_DESCRIPTION_CLAUSE3                                              =  452;  // <file_description_clause> ::= <label_records_clause>
		final int PROD_FILE_DESCRIPTION_CLAUSE4                                              =  453;  // <file_description_clause> ::= <value_of_clause>
		final int PROD_FILE_DESCRIPTION_CLAUSE5                                              =  454;  // <file_description_clause> ::= <data_records_clause>
		final int PROD_FILE_DESCRIPTION_CLAUSE6                                              =  455;  // <file_description_clause> ::= <linage_clause>
		final int PROD_FILE_DESCRIPTION_CLAUSE7                                              =  456;  // <file_description_clause> ::= <recording_mode_clause>
		final int PROD_FILE_DESCRIPTION_CLAUSE8                                              =  457;  // <file_description_clause> ::= <code_set_clause>
		final int PROD_FILE_DESCRIPTION_CLAUSE9                                              =  458;  // <file_description_clause> ::= <report_clause>
		final int PROD_BLOCK_CONTAINS_CLAUSE_BLOCK                                           =  459;  // <block_contains_clause> ::= BLOCK <_contains> <integer> <_to_integer> <_records_or_characters>
		final int PROD__RECORDS_OR_CHARACTERS                                                =  460;  // <_records_or_characters> ::= 
		final int PROD__RECORDS_OR_CHARACTERS_RECORDS                                        =  461;  // <_records_or_characters> ::= RECORDS
		final int PROD__RECORDS_OR_CHARACTERS_CHARACTERS                                     =  462;  // <_records_or_characters> ::= CHARACTERS
		final int PROD_RECORD_CLAUSE_RECORD                                                  =  463;  // <record_clause> ::= RECORD <_contains> <integer> <_characters>
		final int PROD_RECORD_CLAUSE_RECORD_TO                                               =  464;  // <record_clause> ::= RECORD <_contains> <integer> TO <integer> <_characters>
		final int PROD_RECORD_CLAUSE_RECORD_VARYING                                          =  465;  // <record_clause> ::= RECORD <_is> VARYING <_in> <_size> <_from_integer> <_to_integer> <_characters> <_record_depending>
		final int PROD__RECORD_DEPENDING                                                     =  466;  // <_record_depending> ::= 
		final int PROD__RECORD_DEPENDING_DEPENDING                                           =  467;  // <_record_depending> ::= DEPENDING <_on> <reference>
		final int PROD__FROM_INTEGER                                                         =  468;  // <_from_integer> ::= 
		final int PROD__FROM_INTEGER2                                                        =  469;  // <_from_integer> ::= <_from> <integer>
		final int PROD__TO_INTEGER                                                           =  470;  // <_to_integer> ::= 
		final int PROD__TO_INTEGER_TO                                                        =  471;  // <_to_integer> ::= TO <integer>
		final int PROD_LABEL_RECORDS_CLAUSE_LABEL                                            =  472;  // <label_records_clause> ::= LABEL <records> <label_option>
		final int PROD_VALUE_OF_CLAUSE_VALUE_OF                                              =  473;  // <value_of_clause> ::= VALUE OF <file_id> <_is> <valueof_name>
		final int PROD_VALUE_OF_CLAUSE_VALUE_OF_FILE_ID                                      =  474;  // <value_of_clause> ::= VALUE OF 'FILE_ID' <_is> <valueof_name>
		final int PROD_FILE_ID                                                               =  475;  // <file_id> ::= <WORD>
		final int PROD_FILE_ID_ID                                                            =  476;  // <file_id> ::= ID
		final int PROD_VALUEOF_NAME                                                          =  477;  // <valueof_name> ::= <LITERAL_TOK>
		final int PROD_VALUEOF_NAME2                                                         =  478;  // <valueof_name> ::= <qualified_word>
		final int PROD_DATA_RECORDS_CLAUSE_DATA                                              =  479;  // <data_records_clause> ::= DATA <records> <optional_reference_list>
		final int PROD_LINAGE_CLAUSE_LINAGE                                                  =  480;  // <linage_clause> ::= LINAGE <_is> <reference_or_literal> <_lines> <_linage_sequence>
		final int PROD__LINAGE_SEQUENCE                                                      =  481;  // <_linage_sequence> ::= 
		final int PROD__LINAGE_SEQUENCE2                                                     =  482;  // <_linage_sequence> ::= <_linage_sequence> <linage_lines>
		final int PROD_LINAGE_LINES                                                          =  483;  // <linage_lines> ::= <linage_footing>
		final int PROD_LINAGE_LINES2                                                         =  484;  // <linage_lines> ::= <linage_top>
		final int PROD_LINAGE_LINES3                                                         =  485;  // <linage_lines> ::= <linage_bottom>
		final int PROD_LINAGE_FOOTING_FOOTING                                                =  486;  // <linage_footing> ::= <_with> FOOTING <_at> <reference_or_literal>
		final int PROD_LINAGE_TOP_TOP                                                        =  487;  // <linage_top> ::= TOP <reference_or_literal>
		final int PROD_LINAGE_BOTTOM_BOTTOM                                                  =  488;  // <linage_bottom> ::= BOTTOM <reference_or_literal>
		final int PROD_RECORDING_MODE_CLAUSE_RECORDING                                       =  489;  // <recording_mode_clause> ::= RECORDING <_mode> <_is> <recording_mode>
		final int PROD_RECORDING_MODE_F                                                      =  490;  // <recording_mode> ::= F
		final int PROD_RECORDING_MODE_V                                                      =  491;  // <recording_mode> ::= V
		final int PROD_RECORDING_MODE_FIXED                                                  =  492;  // <recording_mode> ::= FIXED
		final int PROD_RECORDING_MODE_VARIABLE                                               =  493;  // <recording_mode> ::= VARIABLE
		final int PROD_RECORDING_MODE                                                        =  494;  // <recording_mode> ::= <u_or_s>
		final int PROD_U_OR_S_U                                                              =  495;  // <u_or_s> ::= U
		final int PROD_U_OR_S_S                                                              =  496;  // <u_or_s> ::= S
		final int PROD_CODE_SET_CLAUSE_CODE_SET                                              =  497;  // <code_set_clause> ::= 'CODE_SET' <_is> <alphabet_name> <_for_sub_records_clause>
		final int PROD__FOR_SUB_RECORDS_CLAUSE                                               =  498;  // <_for_sub_records_clause> ::= 
		final int PROD__FOR_SUB_RECORDS_CLAUSE_FOR                                           =  499;  // <_for_sub_records_clause> ::= FOR <reference_list>
		final int PROD_REPORT_CLAUSE                                                         =  500;  // <report_clause> ::= <report_keyword> <rep_name_list>
		final int PROD_REPORT_KEYWORD_REPORT                                                 =  501;  // <report_keyword> ::= REPORT <_is>
		final int PROD_REPORT_KEYWORD_REPORTS                                                =  502;  // <report_keyword> ::= REPORTS <_are>
		final int PROD_REP_NAME_LIST                                                         =  503;  // <rep_name_list> ::= <undefined_word>
		final int PROD_REP_NAME_LIST2                                                        =  504;  // <rep_name_list> ::= <rep_name_list> <undefined_word>
		final int PROD__COMMUNICATION_SECTION                                                =  505;  // <_communication_section> ::= 
		final int PROD__COMMUNICATION_SECTION_COMMUNICATION_SECTION_TOK_DOT                  =  506;  // <_communication_section> ::= COMMUNICATION SECTION 'TOK_DOT' <_communication_description_sequence>
		final int PROD__COMMUNICATION_DESCRIPTION_SEQUENCE                                   =  507;  // <_communication_description_sequence> ::= 
		final int PROD__COMMUNICATION_DESCRIPTION_SEQUENCE2                                  =  508;  // <_communication_description_sequence> ::= <_communication_description_sequence> <communication_description>
		final int PROD_COMMUNICATION_DESCRIPTION                                             =  509;  // <communication_description> ::= <communication_description_entry> <_record_description_list>
		final int PROD_COMMUNICATION_DESCRIPTION_ENTRY_CD_TOK_DOT                            =  510;  // <communication_description_entry> ::= CD <undefined_word> <_communication_description_clause_sequence> 'TOK_DOT'
		final int PROD__COMMUNICATION_DESCRIPTION_CLAUSE_SEQUENCE                            =  511;  // <_communication_description_clause_sequence> ::= 
		final int PROD__COMMUNICATION_DESCRIPTION_CLAUSE_SEQUENCE2                           =  512;  // <_communication_description_clause_sequence> ::= <_communication_description_clause_sequence> <communication_description_clause>
		final int PROD_COMMUNICATION_DESCRIPTION_CLAUSE_INPUT                                =  513;  // <communication_description_clause> ::= <_for> <_initial> INPUT <_input_cd_clauses>
		final int PROD_COMMUNICATION_DESCRIPTION_CLAUSE_OUTPUT                               =  514;  // <communication_description_clause> ::= <_for> OUTPUT <_output_cd_clauses>
		final int PROD_COMMUNICATION_DESCRIPTION_CLAUSE_I_O                                  =  515;  // <communication_description_clause> ::= <_for> <_initial> 'I_O' <_i_o_cd_clauses>
		final int PROD__INPUT_CD_CLAUSES                                                     =  516;  // <_input_cd_clauses> ::= 
		final int PROD__INPUT_CD_CLAUSES2                                                    =  517;  // <_input_cd_clauses> ::= <named_input_cd_clauses>
		final int PROD__INPUT_CD_CLAUSES3                                                    =  518;  // <_input_cd_clauses> ::= <unnamed_input_cd_clauses>
		final int PROD_NAMED_INPUT_CD_CLAUSES                                                =  519;  // <named_input_cd_clauses> ::= <named_input_cd_clause>
		final int PROD_NAMED_INPUT_CD_CLAUSES2                                               =  520;  // <named_input_cd_clauses> ::= <named_input_cd_clauses> <named_input_cd_clause>
		final int PROD_NAMED_INPUT_CD_CLAUSE_QUEUE                                           =  521;  // <named_input_cd_clause> ::= <_symbolic> QUEUE <_is> <identifier>
		final int PROD_NAMED_INPUT_CD_CLAUSE_SUB_QUEUE_1                                     =  522;  // <named_input_cd_clause> ::= <_symbolic> 'SUB_QUEUE_1' <_is> <identifier>
		final int PROD_NAMED_INPUT_CD_CLAUSE_SUB_QUEUE_2                                     =  523;  // <named_input_cd_clause> ::= <_symbolic> 'SUB_QUEUE_2' <_is> <identifier>
		final int PROD_NAMED_INPUT_CD_CLAUSE_SUB_QUEUE_3                                     =  524;  // <named_input_cd_clause> ::= <_symbolic> 'SUB_QUEUE_3' <_is> <identifier>
		final int PROD_NAMED_INPUT_CD_CLAUSE_MESSAGE_DATE                                    =  525;  // <named_input_cd_clause> ::= MESSAGE DATE <_is> <identifier>
		final int PROD_NAMED_INPUT_CD_CLAUSE_MESSAGE_TIME                                    =  526;  // <named_input_cd_clause> ::= MESSAGE TIME <_is> <identifier>
		final int PROD_NAMED_INPUT_CD_CLAUSE_SOURCE                                          =  527;  // <named_input_cd_clause> ::= <_symbolic> SOURCE <_is> <identifier>
		final int PROD_NAMED_INPUT_CD_CLAUSE_TEXT_LENGTH                                     =  528;  // <named_input_cd_clause> ::= TEXT LENGTH <_is> <identifier>
		final int PROD_NAMED_INPUT_CD_CLAUSE_END_KEY                                         =  529;  // <named_input_cd_clause> ::= END KEY <_is> <identifier>
		final int PROD_NAMED_INPUT_CD_CLAUSE_STATUS_KEY                                      =  530;  // <named_input_cd_clause> ::= STATUS KEY <_is> <identifier>
		final int PROD_NAMED_INPUT_CD_CLAUSE_COUNT                                           =  531;  // <named_input_cd_clause> ::= <_message> COUNT <_is> <identifier>
		final int PROD_UNNAMED_INPUT_CD_CLAUSES                                              =  532;  // <unnamed_input_cd_clauses> ::= <identifier> <identifier> <identifier> <identifier> <identifier> <identifier> <identifier> <identifier> <identifier> <identifier> <identifier>
		final int PROD__OUTPUT_CD_CLAUSES                                                    =  533;  // <_output_cd_clauses> ::= 
		final int PROD__OUTPUT_CD_CLAUSES2                                                   =  534;  // <_output_cd_clauses> ::= <output_cd_clauses>
		final int PROD_OUTPUT_CD_CLAUSES                                                     =  535;  // <output_cd_clauses> ::= <output_cd_clause>
		final int PROD_OUTPUT_CD_CLAUSES2                                                    =  536;  // <output_cd_clauses> ::= <output_cd_clauses> <output_cd_clause>
		final int PROD_OUTPUT_CD_CLAUSE_DESTINATION_COUNT                                    =  537;  // <output_cd_clause> ::= DESTINATION COUNT <_is> <identifier>
		final int PROD_OUTPUT_CD_CLAUSE_TEXT_LENGTH                                          =  538;  // <output_cd_clause> ::= TEXT LENGTH <_is> <identifier>
		final int PROD_OUTPUT_CD_CLAUSE_STATUS_KEY                                           =  539;  // <output_cd_clause> ::= STATUS KEY <_is> <identifier>
		final int PROD_OUTPUT_CD_CLAUSE_DESTINATION_TABLE_OCCURS                             =  540;  // <output_cd_clause> ::= DESTINATION TABLE OCCURS <integer> <_times> <_occurs_indexed>
		final int PROD_OUTPUT_CD_CLAUSE_ERROR_KEY                                            =  541;  // <output_cd_clause> ::= ERROR KEY <_is> <identifier>
		final int PROD_OUTPUT_CD_CLAUSE_DESTINATION                                          =  542;  // <output_cd_clause> ::= DESTINATION <_is> <identifier>
		final int PROD_OUTPUT_CD_CLAUSE_SYMBOLIC_DESTINATION                                 =  543;  // <output_cd_clause> ::= SYMBOLIC DESTINATION <_is> <identifier>
		final int PROD__I_O_CD_CLAUSES                                                       =  544;  // <_i_o_cd_clauses> ::= 
		final int PROD__I_O_CD_CLAUSES2                                                      =  545;  // <_i_o_cd_clauses> ::= <named_i_o_cd_clauses>
		final int PROD__I_O_CD_CLAUSES3                                                      =  546;  // <_i_o_cd_clauses> ::= <unnamed_i_o_cd_clauses>
		final int PROD_NAMED_I_O_CD_CLAUSES                                                  =  547;  // <named_i_o_cd_clauses> ::= <named_i_o_cd_clause>
		final int PROD_NAMED_I_O_CD_CLAUSES2                                                 =  548;  // <named_i_o_cd_clauses> ::= <named_i_o_cd_clauses> <named_i_o_cd_clause>
		final int PROD_NAMED_I_O_CD_CLAUSE_MESSAGE_DATE                                      =  549;  // <named_i_o_cd_clause> ::= MESSAGE DATE <_is> <identifier>
		final int PROD_NAMED_I_O_CD_CLAUSE_MESSAGE_TIME                                      =  550;  // <named_i_o_cd_clause> ::= MESSAGE TIME <_is> <identifier>
		final int PROD_NAMED_I_O_CD_CLAUSE_TERMINAL                                          =  551;  // <named_i_o_cd_clause> ::= <_symbolic> TERMINAL <_is> <identifier>
		final int PROD_NAMED_I_O_CD_CLAUSE_TEXT_LENGTH                                       =  552;  // <named_i_o_cd_clause> ::= TEXT LENGTH <_is> <identifier>
		final int PROD_NAMED_I_O_CD_CLAUSE_END_KEY                                           =  553;  // <named_i_o_cd_clause> ::= END KEY <_is> <identifier>
		final int PROD_NAMED_I_O_CD_CLAUSE_STATUS_KEY                                        =  554;  // <named_i_o_cd_clause> ::= STATUS KEY <_is> <identifier>
		final int PROD_UNNAMED_I_O_CD_CLAUSES                                                =  555;  // <unnamed_i_o_cd_clauses> ::= <identifier> <identifier> <identifier> <identifier> <identifier> <identifier>
		final int PROD__WORKING_STORAGE_SECTION                                              =  556;  // <_working_storage_section> ::= 
		final int PROD__WORKING_STORAGE_SECTION_WORKING_STORAGE_SECTION_TOK_DOT              =  557;  // <_working_storage_section> ::= 'WORKING_STORAGE' SECTION 'TOK_DOT' <_record_description_list>
		final int PROD__RECORD_DESCRIPTION_LIST                                              =  558;  // <_record_description_list> ::= 
		final int PROD__RECORD_DESCRIPTION_LIST2                                             =  559;  // <_record_description_list> ::= <record_description_list>
		final int PROD_RECORD_DESCRIPTION_LIST_TOK_DOT                                       =  560;  // <record_description_list> ::= <data_description> 'TOK_DOT'
		final int PROD_RECORD_DESCRIPTION_LIST_TOK_DOT2                                      =  561;  // <record_description_list> ::= <record_description_list> <data_description> 'TOK_DOT'
		final int PROD_DATA_DESCRIPTION                                                      =  562;  // <data_description> ::= <constant_entry>
		final int PROD_DATA_DESCRIPTION2                                                     =  563;  // <data_description> ::= <renames_entry>
		final int PROD_DATA_DESCRIPTION3                                                     =  564;  // <data_description> ::= <condition_name_entry>
		final int PROD_DATA_DESCRIPTION4                                                     =  565;  // <data_description> ::= <level_number> <_entry_name> <_data_description_clause_sequence>
		final int PROD_LEVEL_NUMBER_INTLITERAL                                               =  566;  // <level_number> ::= IntLiteral
		final int PROD__FILLER                                                               =  567;  // <_filler> ::= 
		final int PROD__FILLER_FILLER                                                        =  568;  // <_filler> ::= FILLER
		final int PROD__ENTRY_NAME                                                           =  569;  // <_entry_name> ::= <_filler>
		final int PROD__ENTRY_NAME2                                                          =  570;  // <_entry_name> ::= <user_entry_name>
		final int PROD_USER_ENTRY_NAME                                                       =  571;  // <user_entry_name> ::= <WORD>
		final int PROD_CONST_GLOBAL                                                          =  572;  // <const_global> ::= 
		final int PROD_CONST_GLOBAL_GLOBAL                                                   =  573;  // <const_global> ::= <_is> GLOBAL
		final int PROD_LIT_OR_LENGTH                                                         =  574;  // <lit_or_length> ::= <literal>
		final int PROD_LIT_OR_LENGTH_LENGTH_OF                                               =  575;  // <lit_or_length> ::= 'LENGTH_OF' <con_identifier>
		final int PROD_LIT_OR_LENGTH_LENGTH                                                  =  576;  // <lit_or_length> ::= LENGTH <con_identifier>
		final int PROD_LIT_OR_LENGTH_BYTE_LENGTH                                             =  577;  // <lit_or_length> ::= 'BYTE_LENGTH' <_of> <con_identifier>
		final int PROD_CON_IDENTIFIER                                                        =  578;  // <con_identifier> ::= <identifier_1>
		final int PROD_CON_IDENTIFIER_BINARY_CHAR                                            =  579;  // <con_identifier> ::= 'BINARY_CHAR'
		final int PROD_CON_IDENTIFIER_BINARY_SHORT                                           =  580;  // <con_identifier> ::= 'BINARY_SHORT'
		final int PROD_CON_IDENTIFIER_BINARY_LONG                                            =  581;  // <con_identifier> ::= 'BINARY_LONG'
		final int PROD_CON_IDENTIFIER_BINARY_DOUBLE                                          =  582;  // <con_identifier> ::= 'BINARY_DOUBLE'
		final int PROD_CON_IDENTIFIER_BINARY_C_LONG                                          =  583;  // <con_identifier> ::= 'BINARY_C_LONG'
		final int PROD_CON_IDENTIFIER2                                                       =  584;  // <con_identifier> ::= <pointer_len>
		final int PROD_CON_IDENTIFIER3                                                       =  585;  // <con_identifier> ::= <float_usage>
		final int PROD_CON_IDENTIFIER4                                                       =  586;  // <con_identifier> ::= <double_usage>
		final int PROD_CON_IDENTIFIER5                                                       =  587;  // <con_identifier> ::= <fp32_usage>
		final int PROD_CON_IDENTIFIER6                                                       =  588;  // <con_identifier> ::= <fp64_usage>
		final int PROD_CON_IDENTIFIER7                                                       =  589;  // <con_identifier> ::= <fp128_usage>
		final int PROD_FP32_USAGE_FLOAT_BINARY_32                                            =  590;  // <fp32_usage> ::= 'FLOAT_BINARY_32'
		final int PROD_FP32_USAGE_FLOAT_DECIMAL_7                                            =  591;  // <fp32_usage> ::= 'FLOAT_DECIMAL_7'
		final int PROD_FP64_USAGE_FLOAT_BINARY_64                                            =  592;  // <fp64_usage> ::= 'FLOAT_BINARY_64'
		final int PROD_FP64_USAGE_FLOAT_DECIMAL_16                                           =  593;  // <fp64_usage> ::= 'FLOAT_DECIMAL_16'
		final int PROD_FP128_USAGE_FLOAT_BINARY_128                                          =  594;  // <fp128_usage> ::= 'FLOAT_BINARY_128'
		final int PROD_FP128_USAGE_FLOAT_DECIMAL_34                                          =  595;  // <fp128_usage> ::= 'FLOAT_DECIMAL_34'
		final int PROD_FP128_USAGE_FLOAT_EXTENDED                                            =  596;  // <fp128_usage> ::= 'FLOAT_EXTENDED'
		final int PROD_POINTER_LEN_POINTER                                                   =  597;  // <pointer_len> ::= POINTER
		final int PROD_POINTER_LEN_PROGRAM_POINTER                                           =  598;  // <pointer_len> ::= 'PROGRAM_POINTER'
		final int PROD_RENAMES_ENTRY_SIXTY_SIX_RENAMES                                       =  599;  // <renames_entry> ::= 'SIXTY_SIX' <user_entry_name> RENAMES <qualified_word> <_renames_thru>
		final int PROD__RENAMES_THRU                                                         =  600;  // <_renames_thru> ::= 
		final int PROD__RENAMES_THRU_THRU                                                    =  601;  // <_renames_thru> ::= THRU <qualified_word>
		final int PROD_CONDITION_NAME_ENTRY_EIGHTY_EIGHT                                     =  602;  // <condition_name_entry> ::= 'EIGHTY_EIGHT' <user_entry_name> <value_clause>
		final int PROD_CONSTANT_ENTRY_CONSTANT                                               =  603;  // <constant_entry> ::= <level_number> <user_entry_name> CONSTANT <const_global> <constant_source>
		final int PROD_CONSTANT_ENTRY_SEVENTY_EIGHT                                          =  604;  // <constant_entry> ::= 'SEVENTY_EIGHT' <user_entry_name> <_global_clause> <value_clause>
		final int PROD_CONSTANT_SOURCE                                                       =  605;  // <constant_source> ::= <_as> <lit_or_length>
		final int PROD_CONSTANT_SOURCE_FROM                                                  =  606;  // <constant_source> ::= FROM <WORD>
		final int PROD__DATA_DESCRIPTION_CLAUSE_SEQUENCE                                     =  607;  // <_data_description_clause_sequence> ::= 
		final int PROD__DATA_DESCRIPTION_CLAUSE_SEQUENCE2                                    =  608;  // <_data_description_clause_sequence> ::= <_data_description_clause_sequence> <data_description_clause>
		final int PROD_DATA_DESCRIPTION_CLAUSE                                               =  609;  // <data_description_clause> ::= <redefines_clause>
		final int PROD_DATA_DESCRIPTION_CLAUSE2                                              =  610;  // <data_description_clause> ::= <external_clause>
		final int PROD_DATA_DESCRIPTION_CLAUSE3                                              =  611;  // <data_description_clause> ::= <global_clause>
		final int PROD_DATA_DESCRIPTION_CLAUSE4                                              =  612;  // <data_description_clause> ::= <picture_clause>
		final int PROD_DATA_DESCRIPTION_CLAUSE5                                              =  613;  // <data_description_clause> ::= <usage_clause>
		final int PROD_DATA_DESCRIPTION_CLAUSE6                                              =  614;  // <data_description_clause> ::= <sign_clause>
		final int PROD_DATA_DESCRIPTION_CLAUSE7                                              =  615;  // <data_description_clause> ::= <occurs_clause>
		final int PROD_DATA_DESCRIPTION_CLAUSE8                                              =  616;  // <data_description_clause> ::= <justified_clause>
		final int PROD_DATA_DESCRIPTION_CLAUSE9                                              =  617;  // <data_description_clause> ::= <synchronized_clause>
		final int PROD_DATA_DESCRIPTION_CLAUSE10                                             =  618;  // <data_description_clause> ::= <blank_clause>
		final int PROD_DATA_DESCRIPTION_CLAUSE11                                             =  619;  // <data_description_clause> ::= <based_clause>
		final int PROD_DATA_DESCRIPTION_CLAUSE12                                             =  620;  // <data_description_clause> ::= <value_clause>
		final int PROD_DATA_DESCRIPTION_CLAUSE13                                             =  621;  // <data_description_clause> ::= <any_length_clause>
		final int PROD_REDEFINES_CLAUSE_REDEFINES                                            =  622;  // <redefines_clause> ::= REDEFINES <identifier_1>
		final int PROD_EXTERNAL_CLAUSE_EXTERNAL                                              =  623;  // <external_clause> ::= <_is> EXTERNAL <_as_extname>
		final int PROD__AS_EXTNAME                                                           =  624;  // <_as_extname> ::= 
		final int PROD__AS_EXTNAME_AS                                                        =  625;  // <_as_extname> ::= AS <LITERAL_TOK>
		final int PROD__GLOBAL_CLAUSE                                                        =  626;  // <_global_clause> ::= 
		final int PROD__GLOBAL_CLAUSE2                                                       =  627;  // <_global_clause> ::= <global_clause>
		final int PROD_GLOBAL_CLAUSE_GLOBAL                                                  =  628;  // <global_clause> ::= <_is> GLOBAL
		final int PROD_PICTURE_CLAUSE_PICTURE_DEF                                            =  629;  // <picture_clause> ::= 'Picture_Def'
		final int PROD_USAGE_CLAUSE                                                          =  630;  // <usage_clause> ::= <usage>
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
		final int PROD_USAGE_NATIONAL                                                        =  667;  // <usage> ::= NATIONAL
		final int PROD_FLOAT_USAGE_COMP_1                                                    =  668;  // <float_usage> ::= 'COMP_1'
		final int PROD_FLOAT_USAGE_FLOAT_SHORT                                               =  669;  // <float_usage> ::= 'FLOAT_SHORT'
		final int PROD_DOUBLE_USAGE_COMP_2                                                   =  670;  // <double_usage> ::= 'COMP_2'
		final int PROD_DOUBLE_USAGE_FLOAT_LONG                                               =  671;  // <double_usage> ::= 'FLOAT_LONG'
		final int PROD_SIGN_CLAUSE_LEADING                                                   =  672;  // <sign_clause> ::= <_sign_is> LEADING <flag_separate>
		final int PROD_SIGN_CLAUSE_TRAILING                                                  =  673;  // <sign_clause> ::= <_sign_is> TRAILING <flag_separate>
		final int PROD_REPORT_OCCURS_CLAUSE_OCCURS                                           =  674;  // <report_occurs_clause> ::= OCCURS <integer_or_word> <_occurs_to_integer> <_times> <_occurs_depending> <_occurs_step>
		final int PROD__OCCURS_STEP                                                          =  675;  // <_occurs_step> ::= 
		final int PROD__OCCURS_STEP_STEP                                                     =  676;  // <_occurs_step> ::= STEP <integer_or_word>
		final int PROD_OCCURS_CLAUSE_OCCURS                                                  =  677;  // <occurs_clause> ::= OCCURS <integer_or_word> <_occurs_to_integer> <_times> <_occurs_depending> <_occurs_keys_and_indexed>
		final int PROD_OCCURS_CLAUSE_OCCURS_UNBOUNDED_DEPENDING                              =  678;  // <occurs_clause> ::= OCCURS <_occurs_integer_to> UNBOUNDED <_times> DEPENDING <_on> <reference> <_occurs_keys_and_indexed>
		final int PROD_OCCURS_CLAUSE_OCCURS_DYNAMIC                                          =  679;  // <occurs_clause> ::= OCCURS DYNAMIC <_capacity_in> <_occurs_from_integer> <_occurs_to_integer> <_occurs_initialized> <_occurs_keys_and_indexed>
		final int PROD__OCCURS_TO_INTEGER                                                    =  680;  // <_occurs_to_integer> ::= 
		final int PROD__OCCURS_TO_INTEGER_TO                                                 =  681;  // <_occurs_to_integer> ::= TO <integer_or_word>
		final int PROD__OCCURS_FROM_INTEGER                                                  =  682;  // <_occurs_from_integer> ::= 
		final int PROD__OCCURS_FROM_INTEGER_FROM                                             =  683;  // <_occurs_from_integer> ::= FROM <integer_or_word>
		final int PROD__OCCURS_INTEGER_TO                                                    =  684;  // <_occurs_integer_to> ::= 
		final int PROD__OCCURS_INTEGER_TO_TO                                                 =  685;  // <_occurs_integer_to> ::= <integer_or_word> TO
		final int PROD_INTEGER_OR_WORD                                                       =  686;  // <integer_or_word> ::= <integer>
		final int PROD_INTEGER_OR_WORD_COBOLWORD                                             =  687;  // <integer_or_word> ::= COBOLWord
		final int PROD__OCCURS_DEPENDING                                                     =  688;  // <_occurs_depending> ::= 
		final int PROD__OCCURS_DEPENDING_DEPENDING                                           =  689;  // <_occurs_depending> ::= DEPENDING <_on> <reference>
		final int PROD__CAPACITY_IN                                                          =  690;  // <_capacity_in> ::= 
		final int PROD__CAPACITY_IN_CAPACITY                                                 =  691;  // <_capacity_in> ::= CAPACITY <_in> <WORD>
		final int PROD__OCCURS_INITIALIZED                                                   =  692;  // <_occurs_initialized> ::= 
		final int PROD__OCCURS_INITIALIZED_INITIALIZED                                       =  693;  // <_occurs_initialized> ::= INITIALIZED
		final int PROD__OCCURS_KEYS_AND_INDEXED                                              =  694;  // <_occurs_keys_and_indexed> ::= 
		final int PROD__OCCURS_KEYS_AND_INDEXED2                                             =  695;  // <_occurs_keys_and_indexed> ::= <occurs_keys> <occurs_indexed>
		final int PROD__OCCURS_KEYS_AND_INDEXED3                                             =  696;  // <_occurs_keys_and_indexed> ::= <occurs_indexed> <occurs_keys>
		final int PROD__OCCURS_KEYS_AND_INDEXED4                                             =  697;  // <_occurs_keys_and_indexed> ::= <occurs_indexed>
		final int PROD__OCCURS_KEYS_AND_INDEXED5                                             =  698;  // <_occurs_keys_and_indexed> ::= <occurs_keys>
		final int PROD_OCCURS_KEYS                                                           =  699;  // <occurs_keys> ::= <occurs_key_list>
		final int PROD_OCCURS_KEY_LIST                                                       =  700;  // <occurs_key_list> ::= <occurs_key_field>
		final int PROD_OCCURS_KEY_LIST2                                                      =  701;  // <occurs_key_list> ::= <occurs_key_field> <occurs_key_list>
		final int PROD_OCCURS_KEY_FIELD                                                      =  702;  // <occurs_key_field> ::= <ascending_or_descending> <_key> <_is> <reference_list>
		final int PROD_ASCENDING_OR_DESCENDING_ASCENDING                                     =  703;  // <ascending_or_descending> ::= ASCENDING
		final int PROD_ASCENDING_OR_DESCENDING_DESCENDING                                    =  704;  // <ascending_or_descending> ::= DESCENDING
		final int PROD__OCCURS_INDEXED                                                       =  705;  // <_occurs_indexed> ::= 
		final int PROD__OCCURS_INDEXED2                                                      =  706;  // <_occurs_indexed> ::= <occurs_indexed>
		final int PROD_OCCURS_INDEXED_INDEXED                                                =  707;  // <occurs_indexed> ::= INDEXED <_by> <occurs_index_list>
		final int PROD_OCCURS_INDEX_LIST                                                     =  708;  // <occurs_index_list> ::= <occurs_index>
		final int PROD_OCCURS_INDEX_LIST2                                                    =  709;  // <occurs_index_list> ::= <occurs_index_list> <occurs_index>
		final int PROD_OCCURS_INDEX                                                          =  710;  // <occurs_index> ::= <WORD>
		final int PROD_JUSTIFIED_CLAUSE_JUSTIFIED                                            =  711;  // <justified_clause> ::= JUSTIFIED <_right>
		final int PROD_SYNCHRONIZED_CLAUSE_SYNCHRONIZED                                      =  712;  // <synchronized_clause> ::= SYNCHRONIZED <_left_or_right>
		final int PROD_BLANK_CLAUSE_BLANK_ZERO                                               =  713;  // <blank_clause> ::= BLANK <_when> ZERO
		final int PROD_BASED_CLAUSE_BASED                                                    =  714;  // <based_clause> ::= BASED
		final int PROD_VALUE_CLAUSE_VALUE                                                    =  715;  // <value_clause> ::= VALUE <_is_are> <value_item_list> <_false_is>
		final int PROD_VALUE_ITEM_LIST                                                       =  716;  // <value_item_list> ::= <value_item>
		final int PROD_VALUE_ITEM_LIST2                                                      =  717;  // <value_item_list> ::= <value_item_list> <value_item>
		final int PROD_VALUE_ITEM                                                            =  718;  // <value_item> ::= <lit_or_length>
		final int PROD_VALUE_ITEM_THRU                                                       =  719;  // <value_item> ::= <lit_or_length> THRU <lit_or_length>
		final int PROD_VALUE_ITEM_COMMA_DELIM                                                =  720;  // <value_item> ::= 'COMMA_DELIM'
		final int PROD__FALSE_IS                                                             =  721;  // <_false_is> ::= 
		final int PROD__FALSE_IS_TOK_FALSE                                                   =  722;  // <_false_is> ::= <_when_set_to> 'TOK_FALSE' <_is> <lit_or_length>
		final int PROD_ANY_LENGTH_CLAUSE_ANY_LENGTH                                          =  723;  // <any_length_clause> ::= ANY LENGTH
		final int PROD_ANY_LENGTH_CLAUSE_ANY_NUMERIC                                         =  724;  // <any_length_clause> ::= ANY NUMERIC
		final int PROD__LOCAL_STORAGE_SECTION                                                =  725;  // <_local_storage_section> ::= 
		final int PROD__LOCAL_STORAGE_SECTION_LOCAL_STORAGE_SECTION_TOK_DOT                  =  726;  // <_local_storage_section> ::= 'LOCAL_STORAGE' SECTION 'TOK_DOT' <_record_description_list>
		final int PROD__LINKAGE_SECTION                                                      =  727;  // <_linkage_section> ::= 
		final int PROD__LINKAGE_SECTION_LINKAGE_SECTION_TOK_DOT                              =  728;  // <_linkage_section> ::= LINKAGE SECTION 'TOK_DOT' <_record_description_list>
		final int PROD__REPORT_SECTION                                                       =  729;  // <_report_section> ::= 
		final int PROD__REPORT_SECTION_REPORT_SECTION_TOK_DOT                                =  730;  // <_report_section> ::= REPORT SECTION 'TOK_DOT' <_report_description_sequence>
		final int PROD__REPORT_DESCRIPTION_SEQUENCE                                          =  731;  // <_report_description_sequence> ::= 
		final int PROD__REPORT_DESCRIPTION_SEQUENCE2                                         =  732;  // <_report_description_sequence> ::= <_report_description_sequence> <report_description>
		final int PROD_REPORT_DESCRIPTION_RD_TOK_DOT                                         =  733;  // <report_description> ::= RD <report_name> <_report_description_options> 'TOK_DOT' <_report_group_description_list>
		final int PROD__REPORT_DESCRIPTION_OPTIONS                                           =  734;  // <_report_description_options> ::= 
		final int PROD__REPORT_DESCRIPTION_OPTIONS2                                          =  735;  // <_report_description_options> ::= <_report_description_options> <report_description_option>
		final int PROD_REPORT_DESCRIPTION_OPTION_GLOBAL                                      =  736;  // <report_description_option> ::= <_is> GLOBAL
		final int PROD_REPORT_DESCRIPTION_OPTION_CODE                                        =  737;  // <report_description_option> ::= CODE <_is> <id_or_lit>
		final int PROD_REPORT_DESCRIPTION_OPTION                                             =  738;  // <report_description_option> ::= <control_clause>
		final int PROD_REPORT_DESCRIPTION_OPTION2                                            =  739;  // <report_description_option> ::= <page_limit_clause>
		final int PROD_CONTROL_CLAUSE                                                        =  740;  // <control_clause> ::= <control_keyword> <control_field_list>
		final int PROD_CONTROL_FIELD_LIST                                                    =  741;  // <control_field_list> ::= <_final> <identifier_list>
		final int PROD_IDENTIFIER_LIST                                                       =  742;  // <identifier_list> ::= <identifier>
		final int PROD_IDENTIFIER_LIST2                                                      =  743;  // <identifier_list> ::= <identifier_list> <identifier>
		final int PROD_PAGE_LIMIT_CLAUSE_PAGE                                                =  744;  // <page_limit_clause> ::= PAGE <_limits> <page_line_column> <_page_heading_list>
		final int PROD_PAGE_LINE_COLUMN                                                      =  745;  // <page_line_column> ::= <report_integer>
		final int PROD_PAGE_LINE_COLUMN2                                                     =  746;  // <page_line_column> ::= <report_integer> <line_or_lines> <report_integer> <columns_or_cols>
		final int PROD_PAGE_LINE_COLUMN3                                                     =  747;  // <page_line_column> ::= <report_integer> <line_or_lines>
		final int PROD__PAGE_HEADING_LIST                                                    =  748;  // <_page_heading_list> ::= 
		final int PROD__PAGE_HEADING_LIST2                                                   =  749;  // <_page_heading_list> ::= <_page_heading_list> <page_detail>
		final int PROD_PAGE_DETAIL                                                           =  750;  // <page_detail> ::= <heading_clause>
		final int PROD_PAGE_DETAIL2                                                          =  751;  // <page_detail> ::= <first_detail>
		final int PROD_PAGE_DETAIL3                                                          =  752;  // <page_detail> ::= <last_heading>
		final int PROD_PAGE_DETAIL4                                                          =  753;  // <page_detail> ::= <last_detail>
		final int PROD_PAGE_DETAIL5                                                          =  754;  // <page_detail> ::= <footing_clause>
		final int PROD_HEADING_CLAUSE_HEADING                                                =  755;  // <heading_clause> ::= HEADING <_is> <report_integer>
		final int PROD_FIRST_DETAIL_FIRST                                                    =  756;  // <first_detail> ::= FIRST <detail_keyword> <_is> <report_integer>
		final int PROD_LAST_HEADING_LAST                                                     =  757;  // <last_heading> ::= LAST <ch_keyword> <_is> <report_integer>
		final int PROD_LAST_DETAIL_LAST                                                      =  758;  // <last_detail> ::= LAST <detail_keyword> <_is> <report_integer>
		final int PROD_FOOTING_CLAUSE_FOOTING                                                =  759;  // <footing_clause> ::= FOOTING <_is> <report_integer>
		final int PROD__REPORT_GROUP_DESCRIPTION_LIST                                        =  760;  // <_report_group_description_list> ::= 
		final int PROD__REPORT_GROUP_DESCRIPTION_LIST2                                       =  761;  // <_report_group_description_list> ::= <_report_group_description_list> <report_group_description_entry>
		final int PROD_REPORT_GROUP_DESCRIPTION_ENTRY_TOK_DOT                                =  762;  // <report_group_description_entry> ::= <level_number> <_entry_name> <_report_group_options> 'TOK_DOT'
		final int PROD__REPORT_GROUP_OPTIONS                                                 =  763;  // <_report_group_options> ::= 
		final int PROD__REPORT_GROUP_OPTIONS2                                                =  764;  // <_report_group_options> ::= <_report_group_options> <report_group_option>
		final int PROD_REPORT_GROUP_OPTION                                                   =  765;  // <report_group_option> ::= <type_clause>
		final int PROD_REPORT_GROUP_OPTION2                                                  =  766;  // <report_group_option> ::= <next_group_clause>
		final int PROD_REPORT_GROUP_OPTION3                                                  =  767;  // <report_group_option> ::= <line_clause>
		final int PROD_REPORT_GROUP_OPTION4                                                  =  768;  // <report_group_option> ::= <picture_clause>
		final int PROD_REPORT_GROUP_OPTION5                                                  =  769;  // <report_group_option> ::= <report_usage_clause>
		final int PROD_REPORT_GROUP_OPTION6                                                  =  770;  // <report_group_option> ::= <sign_clause>
		final int PROD_REPORT_GROUP_OPTION7                                                  =  771;  // <report_group_option> ::= <justified_clause>
		final int PROD_REPORT_GROUP_OPTION8                                                  =  772;  // <report_group_option> ::= <column_clause>
		final int PROD_REPORT_GROUP_OPTION9                                                  =  773;  // <report_group_option> ::= <blank_clause>
		final int PROD_REPORT_GROUP_OPTION10                                                 =  774;  // <report_group_option> ::= <source_clause>
		final int PROD_REPORT_GROUP_OPTION11                                                 =  775;  // <report_group_option> ::= <sum_clause_list>
		final int PROD_REPORT_GROUP_OPTION12                                                 =  776;  // <report_group_option> ::= <value_clause>
		final int PROD_REPORT_GROUP_OPTION13                                                 =  777;  // <report_group_option> ::= <present_when_condition>
		final int PROD_REPORT_GROUP_OPTION14                                                 =  778;  // <report_group_option> ::= <group_indicate_clause>
		final int PROD_REPORT_GROUP_OPTION15                                                 =  779;  // <report_group_option> ::= <report_occurs_clause>
		final int PROD_REPORT_GROUP_OPTION16                                                 =  780;  // <report_group_option> ::= <varying_clause>
		final int PROD_TYPE_CLAUSE_TYPE                                                      =  781;  // <type_clause> ::= TYPE <_is> <type_option>
		final int PROD_TYPE_OPTION                                                           =  782;  // <type_option> ::= <rh_keyword>
		final int PROD_TYPE_OPTION2                                                          =  783;  // <type_option> ::= <ph_keyword>
		final int PROD_TYPE_OPTION3                                                          =  784;  // <type_option> ::= <ch_keyword> <_control_final>
		final int PROD_TYPE_OPTION4                                                          =  785;  // <type_option> ::= <detail_keyword>
		final int PROD_TYPE_OPTION5                                                          =  786;  // <type_option> ::= <cf_keyword> <_control_final>
		final int PROD_TYPE_OPTION6                                                          =  787;  // <type_option> ::= <pf_keyword>
		final int PROD_TYPE_OPTION7                                                          =  788;  // <type_option> ::= <rf_keyword>
		final int PROD__CONTROL_FINAL                                                        =  789;  // <_control_final> ::= 
		final int PROD__CONTROL_FINAL2                                                       =  790;  // <_control_final> ::= <identifier> <_or_page>
		final int PROD__CONTROL_FINAL_FINAL                                                  =  791;  // <_control_final> ::= FINAL <_or_page>
		final int PROD__OR_PAGE                                                              =  792;  // <_or_page> ::= 
		final int PROD__OR_PAGE_OR_PAGE                                                      =  793;  // <_or_page> ::= OR PAGE
		final int PROD_NEXT_GROUP_CLAUSE_NEXT_GROUP                                          =  794;  // <next_group_clause> ::= NEXT GROUP <_is> <line_or_plus>
		final int PROD_SUM_CLAUSE_LIST_SUM                                                   =  795;  // <sum_clause_list> ::= SUM <_of> <report_x_list> <_reset_clause>
		final int PROD__RESET_CLAUSE                                                         =  796;  // <_reset_clause> ::= 
		final int PROD__RESET_CLAUSE_RESET                                                   =  797;  // <_reset_clause> ::= RESET <_on> <data_or_final>
		final int PROD_DATA_OR_FINAL                                                         =  798;  // <data_or_final> ::= <identifier>
		final int PROD_DATA_OR_FINAL_FINAL                                                   =  799;  // <data_or_final> ::= FINAL
		final int PROD_PRESENT_WHEN_CONDITION_PRESENT_WHEN                                   =  800;  // <present_when_condition> ::= PRESENT WHEN <condition>
		final int PROD_VARYING_CLAUSE_VARYING_FROM_BY                                        =  801;  // <varying_clause> ::= VARYING <identifier> FROM <arith_x> BY <arith_x>
		final int PROD_LINE_CLAUSE                                                           =  802;  // <line_clause> ::= <line_keyword_clause> <report_line_integer_list>
		final int PROD_LINE_KEYWORD_CLAUSE_LINE                                              =  803;  // <line_keyword_clause> ::= LINE <_numbers> <_is_are>
		final int PROD_LINE_KEYWORD_CLAUSE_LINES                                             =  804;  // <line_keyword_clause> ::= LINES <_are>
		final int PROD_COLUMN_CLAUSE                                                         =  805;  // <column_clause> ::= <col_keyword_clause> <report_col_integer_list>
		final int PROD_COL_KEYWORD_CLAUSE                                                    =  806;  // <col_keyword_clause> ::= <column_or_col> <_numbers> <_is_are>
		final int PROD_COL_KEYWORD_CLAUSE2                                                   =  807;  // <col_keyword_clause> ::= <columns_or_cols> <_are>
		final int PROD_REPORT_LINE_INTEGER_LIST                                              =  808;  // <report_line_integer_list> ::= <line_or_plus>
		final int PROD_REPORT_LINE_INTEGER_LIST2                                             =  809;  // <report_line_integer_list> ::= <report_line_integer_list> <line_or_plus>
		final int PROD_LINE_OR_PLUS_PLUS                                                     =  810;  // <line_or_plus> ::= PLUS <integer>
		final int PROD_LINE_OR_PLUS                                                          =  811;  // <line_or_plus> ::= <report_integer>
		final int PROD_LINE_OR_PLUS_NEXT_PAGE                                                =  812;  // <line_or_plus> ::= 'NEXT_PAGE'
		final int PROD_REPORT_COL_INTEGER_LIST                                               =  813;  // <report_col_integer_list> ::= <col_or_plus>
		final int PROD_REPORT_COL_INTEGER_LIST2                                              =  814;  // <report_col_integer_list> ::= <report_col_integer_list> <col_or_plus>
		final int PROD_COL_OR_PLUS_PLUS                                                      =  815;  // <col_or_plus> ::= PLUS <integer>
		final int PROD_COL_OR_PLUS                                                           =  816;  // <col_or_plus> ::= <report_integer>
		final int PROD_SOURCE_CLAUSE_SOURCE                                                  =  817;  // <source_clause> ::= SOURCE <_is> <arith_x> <flag_rounded>
		final int PROD_GROUP_INDICATE_CLAUSE_GROUP                                           =  818;  // <group_indicate_clause> ::= GROUP <_indicate>
		final int PROD_REPORT_USAGE_CLAUSE_USAGE_DISPLAY                                     =  819;  // <report_usage_clause> ::= USAGE <_is> DISPLAY
		final int PROD__SCREEN_SECTION                                                       =  820;  // <_screen_section> ::= 
		final int PROD__SCREEN_SECTION_SCREEN_SECTION_TOK_DOT                                =  821;  // <_screen_section> ::= SCREEN SECTION 'TOK_DOT' <_screen_description_list>
		final int PROD__SCREEN_DESCRIPTION_LIST                                              =  822;  // <_screen_description_list> ::= 
		final int PROD__SCREEN_DESCRIPTION_LIST2                                             =  823;  // <_screen_description_list> ::= <screen_description_list>
		final int PROD_SCREEN_DESCRIPTION_LIST_TOK_DOT                                       =  824;  // <screen_description_list> ::= <screen_description> 'TOK_DOT'
		final int PROD_SCREEN_DESCRIPTION_LIST_TOK_DOT2                                      =  825;  // <screen_description_list> ::= <screen_description_list> <screen_description> 'TOK_DOT'
		final int PROD_SCREEN_DESCRIPTION                                                    =  826;  // <screen_description> ::= <constant_entry>
		final int PROD_SCREEN_DESCRIPTION2                                                   =  827;  // <screen_description> ::= <level_number> <_entry_name> <_screen_options>
		final int PROD__SCREEN_OPTIONS                                                       =  828;  // <_screen_options> ::= 
		final int PROD__SCREEN_OPTIONS2                                                      =  829;  // <_screen_options> ::= <_screen_options> <screen_option>
		final int PROD_SCREEN_OPTION_BLANK_LINE                                              =  830;  // <screen_option> ::= BLANK LINE
		final int PROD_SCREEN_OPTION_BLANK_SCREEN                                            =  831;  // <screen_option> ::= BLANK SCREEN
		final int PROD_SCREEN_OPTION_BELL                                                    =  832;  // <screen_option> ::= BELL
		final int PROD_SCREEN_OPTION_BLINK                                                   =  833;  // <screen_option> ::= BLINK
		final int PROD_SCREEN_OPTION_ERASE                                                   =  834;  // <screen_option> ::= ERASE <eol>
		final int PROD_SCREEN_OPTION_ERASE2                                                  =  835;  // <screen_option> ::= ERASE <eos>
		final int PROD_SCREEN_OPTION_HIGHLIGHT                                               =  836;  // <screen_option> ::= HIGHLIGHT
		final int PROD_SCREEN_OPTION_LOWLIGHT                                                =  837;  // <screen_option> ::= LOWLIGHT
		final int PROD_SCREEN_OPTION                                                         =  838;  // <screen_option> ::= <reverse_video>
		final int PROD_SCREEN_OPTION_UNDERLINE                                               =  839;  // <screen_option> ::= UNDERLINE
		final int PROD_SCREEN_OPTION_OVERLINE                                                =  840;  // <screen_option> ::= OVERLINE
		final int PROD_SCREEN_OPTION_GRID                                                    =  841;  // <screen_option> ::= GRID
		final int PROD_SCREEN_OPTION_LEFTLINE                                                =  842;  // <screen_option> ::= LEFTLINE
		final int PROD_SCREEN_OPTION_AUTO                                                    =  843;  // <screen_option> ::= AUTO
		final int PROD_SCREEN_OPTION_TAB                                                     =  844;  // <screen_option> ::= TAB
		final int PROD_SCREEN_OPTION_SECURE                                                  =  845;  // <screen_option> ::= SECURE
		final int PROD_SCREEN_OPTION2                                                        =  846;  // <screen_option> ::= <no_echo>
		final int PROD_SCREEN_OPTION_REQUIRED                                                =  847;  // <screen_option> ::= REQUIRED
		final int PROD_SCREEN_OPTION_FULL                                                    =  848;  // <screen_option> ::= FULL
		final int PROD_SCREEN_OPTION_PROMPT_CHARACTER                                        =  849;  // <screen_option> ::= PROMPT CHARACTER <_is> <id_or_lit>
		final int PROD_SCREEN_OPTION_PROMPT                                                  =  850;  // <screen_option> ::= PROMPT
		final int PROD_SCREEN_OPTION_TOK_INITIAL                                             =  851;  // <screen_option> ::= 'TOK_INITIAL'
		final int PROD_SCREEN_OPTION_LINE                                                    =  852;  // <screen_option> ::= LINE <screen_line_number>
		final int PROD_SCREEN_OPTION3                                                        =  853;  // <screen_option> ::= <column_or_col> <screen_col_number>
		final int PROD_SCREEN_OPTION_FOREGROUND_COLOR                                        =  854;  // <screen_option> ::= 'FOREGROUND_COLOR' <_is> <num_id_or_lit>
		final int PROD_SCREEN_OPTION_BACKGROUND_COLOR                                        =  855;  // <screen_option> ::= 'BACKGROUND_COLOR' <_is> <num_id_or_lit>
		final int PROD_SCREEN_OPTION4                                                        =  856;  // <screen_option> ::= <usage_clause>
		final int PROD_SCREEN_OPTION5                                                        =  857;  // <screen_option> ::= <blank_clause>
		final int PROD_SCREEN_OPTION6                                                        =  858;  // <screen_option> ::= <screen_global_clause>
		final int PROD_SCREEN_OPTION7                                                        =  859;  // <screen_option> ::= <justified_clause>
		final int PROD_SCREEN_OPTION8                                                        =  860;  // <screen_option> ::= <sign_clause>
		final int PROD_SCREEN_OPTION9                                                        =  861;  // <screen_option> ::= <value_clause>
		final int PROD_SCREEN_OPTION10                                                       =  862;  // <screen_option> ::= <picture_clause>
		final int PROD_SCREEN_OPTION11                                                       =  863;  // <screen_option> ::= <screen_occurs_clause>
		final int PROD_SCREEN_OPTION_USING                                                   =  864;  // <screen_option> ::= USING <identifier>
		final int PROD_SCREEN_OPTION_FROM                                                    =  865;  // <screen_option> ::= FROM <from_parameter>
		final int PROD_SCREEN_OPTION_TO                                                      =  866;  // <screen_option> ::= TO <identifier>
		final int PROD_EOL_EOL                                                               =  867;  // <eol> ::= EOL
		final int PROD_EOL_LINE                                                              =  868;  // <eol> ::= <_end_of> LINE
		final int PROD_EOS_EOS                                                               =  869;  // <eos> ::= EOS
		final int PROD_EOS_SCREEN                                                            =  870;  // <eos> ::= <_end_of> SCREEN
		final int PROD_PLUS_PLUS_PLUS                                                        =  871;  // <plus_plus> ::= PLUS
		final int PROD_PLUS_PLUS_TOK_PLUS                                                    =  872;  // <plus_plus> ::= 'TOK_PLUS'
		final int PROD_MINUS_MINUS_MINUS                                                     =  873;  // <minus_minus> ::= MINUS
		final int PROD_MINUS_MINUS_TOK_MINUS                                                 =  874;  // <minus_minus> ::= 'TOK_MINUS'
		final int PROD_SCREEN_LINE_NUMBER                                                    =  875;  // <screen_line_number> ::= <_number> <_is> <_screen_line_plus_minus> <num_id_or_lit>
		final int PROD__SCREEN_LINE_PLUS_MINUS                                               =  876;  // <_screen_line_plus_minus> ::= 
		final int PROD__SCREEN_LINE_PLUS_MINUS2                                              =  877;  // <_screen_line_plus_minus> ::= <plus_plus>
		final int PROD__SCREEN_LINE_PLUS_MINUS3                                              =  878;  // <_screen_line_plus_minus> ::= <minus_minus>
		final int PROD_SCREEN_COL_NUMBER                                                     =  879;  // <screen_col_number> ::= <_number> <_is> <_screen_col_plus_minus> <num_id_or_lit>
		final int PROD__SCREEN_COL_PLUS_MINUS                                                =  880;  // <_screen_col_plus_minus> ::= 
		final int PROD__SCREEN_COL_PLUS_MINUS2                                               =  881;  // <_screen_col_plus_minus> ::= <plus_plus>
		final int PROD__SCREEN_COL_PLUS_MINUS3                                               =  882;  // <_screen_col_plus_minus> ::= <minus_minus>
		final int PROD_SCREEN_OCCURS_CLAUSE_OCCURS                                           =  883;  // <screen_occurs_clause> ::= OCCURS <integer> <_times>
		final int PROD_SCREEN_GLOBAL_CLAUSE_GLOBAL                                           =  884;  // <screen_global_clause> ::= <_is> GLOBAL
		final int PROD__PROCEDURE_DIVISION                                                   =  885;  // <_procedure_division> ::= 
		final int PROD__PROCEDURE_DIVISION_PROCEDURE_DIVISION_TOK_DOT                        =  886;  // <_procedure_division> ::= PROCEDURE DIVISION <_mnemonic_conv> <_procedure_using_chaining> <_procedure_returning> 'TOK_DOT' <_procedure_declaratives> <_procedure_list>
		final int PROD__PROCEDURE_DIVISION_TOK_DOT                                           =  887;  // <_procedure_division> ::= <statements> 'TOK_DOT' <_procedure_list>
		final int PROD__PROCEDURE_USING_CHAINING                                             =  888;  // <_procedure_using_chaining> ::= 
		final int PROD__PROCEDURE_USING_CHAINING_USING                                       =  889;  // <_procedure_using_chaining> ::= USING <procedure_param_list>
		final int PROD__PROCEDURE_USING_CHAINING_CHAINING                                    =  890;  // <_procedure_using_chaining> ::= CHAINING <procedure_param_list>
		final int PROD_PROCEDURE_PARAM_LIST                                                  =  891;  // <procedure_param_list> ::= <procedure_param>
		final int PROD_PROCEDURE_PARAM_LIST2                                                 =  892;  // <procedure_param_list> ::= <procedure_param_list> <procedure_param>
		final int PROD_PROCEDURE_PARAM                                                       =  893;  // <procedure_param> ::= <_procedure_type> <_size_optional> <_procedure_optional> <WORD>
		final int PROD_PROCEDURE_PARAM_COMMA_DELIM                                           =  894;  // <procedure_param> ::= 'COMMA_DELIM'
		final int PROD__PROCEDURE_TYPE                                                       =  895;  // <_procedure_type> ::= 
		final int PROD__PROCEDURE_TYPE_REFERENCE                                             =  896;  // <_procedure_type> ::= <_by> REFERENCE
		final int PROD__PROCEDURE_TYPE_VALUE                                                 =  897;  // <_procedure_type> ::= <_by> VALUE
		final int PROD__SIZE_OPTIONAL                                                        =  898;  // <_size_optional> ::= 
		final int PROD__SIZE_OPTIONAL_SIZE_AUTO                                              =  899;  // <_size_optional> ::= SIZE <_is> AUTO
		final int PROD__SIZE_OPTIONAL_SIZE_DEFAULT                                           =  900;  // <_size_optional> ::= SIZE <_is> DEFAULT
		final int PROD__SIZE_OPTIONAL_UNSIGNED_SIZE_AUTO                                     =  901;  // <_size_optional> ::= UNSIGNED SIZE <_is> AUTO
		final int PROD__SIZE_OPTIONAL_UNSIGNED                                               =  902;  // <_size_optional> ::= UNSIGNED <size_is_integer>
		final int PROD__SIZE_OPTIONAL2                                                       =  903;  // <_size_optional> ::= <size_is_integer>
		final int PROD_SIZE_IS_INTEGER_SIZE                                                  =  904;  // <size_is_integer> ::= SIZE <_is> <integer>
		final int PROD__PROCEDURE_OPTIONAL                                                   =  905;  // <_procedure_optional> ::= 
		final int PROD__PROCEDURE_OPTIONAL_OPTIONAL                                          =  906;  // <_procedure_optional> ::= OPTIONAL
		final int PROD__PROCEDURE_RETURNING                                                  =  907;  // <_procedure_returning> ::= 
		final int PROD__PROCEDURE_RETURNING_RETURNING_OMITTED                                =  908;  // <_procedure_returning> ::= RETURNING OMITTED
		final int PROD__PROCEDURE_RETURNING_RETURNING                                        =  909;  // <_procedure_returning> ::= RETURNING <WORD>
		final int PROD__PROCEDURE_DECLARATIVES                                               =  910;  // <_procedure_declaratives> ::= 
		final int PROD__PROCEDURE_DECLARATIVES_DECLARATIVES_TOK_DOT_END_DECLARATIVES_TOK_DOT =  911;  // <_procedure_declaratives> ::= DECLARATIVES 'TOK_DOT' <_procedure_list> END DECLARATIVES 'TOK_DOT'
		final int PROD__PROCEDURE_LIST                                                       =  912;  // <_procedure_list> ::= 
		final int PROD__PROCEDURE_LIST2                                                      =  913;  // <_procedure_list> ::= <_procedure_list> <procedure>
		final int PROD_PROCEDURE                                                             =  914;  // <procedure> ::= <section_header>
		final int PROD_PROCEDURE2                                                            =  915;  // <procedure> ::= <paragraph_header>
		final int PROD_PROCEDURE_TOK_DOT                                                     =  916;  // <procedure> ::= <statements> 'TOK_DOT'
		final int PROD_PROCEDURE_TOK_DOT2                                                    =  917;  // <procedure> ::= 'TOK_DOT'
		final int PROD_SECTION_HEADER_SECTION_TOK_DOT                                        =  918;  // <section_header> ::= <WORD> SECTION <_segment> 'TOK_DOT' <_use_statement>
		final int PROD__USE_STATEMENT                                                        =  919;  // <_use_statement> ::= 
		final int PROD__USE_STATEMENT_TOK_DOT                                                =  920;  // <_use_statement> ::= <use_statement> 'TOK_DOT'
		final int PROD_PARAGRAPH_HEADER_TOK_DOT                                              =  921;  // <paragraph_header> ::= <IntLiteral or WORD> 'TOK_DOT'
		final int PROD_INTLITERALORWORD_INTLITERAL                                           =  922;  // <IntLiteral or WORD> ::= IntLiteral
		final int PROD_INTLITERALORWORD                                                      =  923;  // <IntLiteral or WORD> ::= <WORD>
		final int PROD__SEGMENT                                                              =  924;  // <_segment> ::= 
		final int PROD__SEGMENT2                                                             =  925;  // <_segment> ::= <integer>
		final int PROD_STATEMENT_LIST                                                        =  926;  // <statement_list> ::= <statements>
		final int PROD_STATEMENTS                                                            =  927;  // <statements> ::= <statement>
		final int PROD_STATEMENTS2                                                           =  928;  // <statements> ::= <statements> <statement>
		final int PROD_STATEMENT                                                             =  929;  // <statement> ::= <accept_statement>
		final int PROD_STATEMENT2                                                            =  930;  // <statement> ::= <add_statement>
		final int PROD_STATEMENT3                                                            =  931;  // <statement> ::= <allocate_statement>
		final int PROD_STATEMENT4                                                            =  932;  // <statement> ::= <alter_statement>
		final int PROD_STATEMENT5                                                            =  933;  // <statement> ::= <call_statement>
		final int PROD_STATEMENT6                                                            =  934;  // <statement> ::= <cancel_statement>
		final int PROD_STATEMENT7                                                            =  935;  // <statement> ::= <close_statement>
		final int PROD_STATEMENT8                                                            =  936;  // <statement> ::= <commit_statement>
		final int PROD_STATEMENT9                                                            =  937;  // <statement> ::= <compute_statement>
		final int PROD_STATEMENT10                                                           =  938;  // <statement> ::= <continue_statement>
		final int PROD_STATEMENT11                                                           =  939;  // <statement> ::= <delete_statement>
		final int PROD_STATEMENT12                                                           =  940;  // <statement> ::= <disable_statement>
		final int PROD_STATEMENT13                                                           =  941;  // <statement> ::= <display_statement>
		final int PROD_STATEMENT14                                                           =  942;  // <statement> ::= <divide_statement>
		final int PROD_STATEMENT15                                                           =  943;  // <statement> ::= <enable_statement>
		final int PROD_STATEMENT16                                                           =  944;  // <statement> ::= <entry_statement>
		final int PROD_STATEMENT17                                                           =  945;  // <statement> ::= <evaluate_statement>
		final int PROD_STATEMENT18                                                           =  946;  // <statement> ::= <exit_statement>
		final int PROD_STATEMENT19                                                           =  947;  // <statement> ::= <free_statement>
		final int PROD_STATEMENT20                                                           =  948;  // <statement> ::= <generate_statement>
		final int PROD_STATEMENT21                                                           =  949;  // <statement> ::= <goto_statement>
		final int PROD_STATEMENT22                                                           =  950;  // <statement> ::= <goback_statement>
		final int PROD_STATEMENT23                                                           =  951;  // <statement> ::= <if_statement>
		final int PROD_STATEMENT24                                                           =  952;  // <statement> ::= <initialize_statement>
		final int PROD_STATEMENT25                                                           =  953;  // <statement> ::= <initiate_statement>
		final int PROD_STATEMENT26                                                           =  954;  // <statement> ::= <inspect_statement>
		final int PROD_STATEMENT27                                                           =  955;  // <statement> ::= <merge_statement>
		final int PROD_STATEMENT28                                                           =  956;  // <statement> ::= <move_statement>
		final int PROD_STATEMENT29                                                           =  957;  // <statement> ::= <multiply_statement>
		final int PROD_STATEMENT30                                                           =  958;  // <statement> ::= <open_statement>
		final int PROD_STATEMENT31                                                           =  959;  // <statement> ::= <perform_statement>
		final int PROD_STATEMENT32                                                           =  960;  // <statement> ::= <purge_statement>
		final int PROD_STATEMENT33                                                           =  961;  // <statement> ::= <read_statement>
		final int PROD_STATEMENT34                                                           =  962;  // <statement> ::= <ready_statement>
		final int PROD_STATEMENT35                                                           =  963;  // <statement> ::= <receive_statement>
		final int PROD_STATEMENT36                                                           =  964;  // <statement> ::= <release_statement>
		final int PROD_STATEMENT37                                                           =  965;  // <statement> ::= <reset_statement>
		final int PROD_STATEMENT38                                                           =  966;  // <statement> ::= <return_statement>
		final int PROD_STATEMENT39                                                           =  967;  // <statement> ::= <rewrite_statement>
		final int PROD_STATEMENT40                                                           =  968;  // <statement> ::= <rollback_statement>
		final int PROD_STATEMENT41                                                           =  969;  // <statement> ::= <search_statement>
		final int PROD_STATEMENT42                                                           =  970;  // <statement> ::= <send_statement>
		final int PROD_STATEMENT43                                                           =  971;  // <statement> ::= <set_statement>
		final int PROD_STATEMENT44                                                           =  972;  // <statement> ::= <sort_statement>
		final int PROD_STATEMENT45                                                           =  973;  // <statement> ::= <start_statement>
		final int PROD_STATEMENT46                                                           =  974;  // <statement> ::= <stop_statement>
		final int PROD_STATEMENT47                                                           =  975;  // <statement> ::= <string_statement>
		final int PROD_STATEMENT48                                                           =  976;  // <statement> ::= <subtract_statement>
		final int PROD_STATEMENT49                                                           =  977;  // <statement> ::= <suppress_statement>
		final int PROD_STATEMENT50                                                           =  978;  // <statement> ::= <terminate_statement>
		final int PROD_STATEMENT51                                                           =  979;  // <statement> ::= <transform_statement>
		final int PROD_STATEMENT52                                                           =  980;  // <statement> ::= <unlock_statement>
		final int PROD_STATEMENT53                                                           =  981;  // <statement> ::= <unstring_statement>
		final int PROD_STATEMENT54                                                           =  982;  // <statement> ::= <write_statement>
		final int PROD_STATEMENT_NEXT_SENTENCE                                               =  983;  // <statement> ::= NEXT SENTENCE
		final int PROD_ACCEPT_STATEMENT_ACCEPT                                               =  984;  // <accept_statement> ::= ACCEPT <accept_body> <end_accept>
		final int PROD_ACCEPT_BODY                                                           =  985;  // <accept_body> ::= <accp_identifier> <_accept_clauses> <_accept_exception_phrases>
		final int PROD_ACCEPT_BODY_FROM                                                      =  986;  // <accept_body> ::= <identifier> FROM <lines_or_number>
		final int PROD_ACCEPT_BODY_FROM2                                                     =  987;  // <accept_body> ::= <identifier> FROM <columns_or_cols>
		final int PROD_ACCEPT_BODY_FROM_DATE_YYYYMMDD                                        =  988;  // <accept_body> ::= <identifier> FROM DATE YYYYMMDD
		final int PROD_ACCEPT_BODY_FROM_DATE                                                 =  989;  // <accept_body> ::= <identifier> FROM DATE
		final int PROD_ACCEPT_BODY_FROM_DAY_YYYYDDD                                          =  990;  // <accept_body> ::= <identifier> FROM DAY YYYYDDD
		final int PROD_ACCEPT_BODY_FROM_DAY                                                  =  991;  // <accept_body> ::= <identifier> FROM DAY
		final int PROD_ACCEPT_BODY_FROM_DAY_OF_WEEK                                          =  992;  // <accept_body> ::= <identifier> FROM 'DAY_OF_WEEK'
		final int PROD_ACCEPT_BODY_FROM_ESCAPE_KEY                                           =  993;  // <accept_body> ::= <identifier> FROM ESCAPE KEY
		final int PROD_ACCEPT_BODY_FROM_EXCEPTION_STATUS                                     =  994;  // <accept_body> ::= <identifier> FROM EXCEPTION STATUS
		final int PROD_ACCEPT_BODY_FROM_TIME                                                 =  995;  // <accept_body> ::= <identifier> FROM TIME
		final int PROD_ACCEPT_BODY_FROM_USER_NAME                                            =  996;  // <accept_body> ::= <identifier> FROM USER NAME
		final int PROD_ACCEPT_BODY_FROM_COMMAND_LINE                                         =  997;  // <accept_body> ::= <identifier> FROM 'COMMAND_LINE'
		final int PROD_ACCEPT_BODY_FROM_ENVIRONMENT_VALUE                                    =  998;  // <accept_body> ::= <identifier> FROM 'ENVIRONMENT_VALUE' <_accept_exception_phrases>
		final int PROD_ACCEPT_BODY_FROM_ENVIRONMENT                                          =  999;  // <accept_body> ::= <identifier> FROM ENVIRONMENT <simple_display_value> <_accept_exception_phrases>
		final int PROD_ACCEPT_BODY_FROM_ARGUMENT_NUMBER                                      = 1000;  // <accept_body> ::= <identifier> FROM 'ARGUMENT_NUMBER'
		final int PROD_ACCEPT_BODY_FROM_ARGUMENT_VALUE                                       = 1001;  // <accept_body> ::= <identifier> FROM 'ARGUMENT_VALUE' <_accept_exception_phrases>
		final int PROD_ACCEPT_BODY_FROM3                                                     = 1002;  // <accept_body> ::= <identifier> FROM <mnemonic_name>
		final int PROD_ACCEPT_BODY_FROM4                                                     = 1003;  // <accept_body> ::= <identifier> FROM <WORD>
		final int PROD_ACCEPT_BODY_COUNT                                                     = 1004;  // <accept_body> ::= <cd_name> <_message> COUNT
		final int PROD_ACCP_IDENTIFIER                                                       = 1005;  // <accp_identifier> ::= <identifier>
		final int PROD_ACCP_IDENTIFIER_OMITTED                                               = 1006;  // <accp_identifier> ::= OMITTED
		final int PROD__ACCEPT_CLAUSES                                                       = 1007;  // <_accept_clauses> ::= 
		final int PROD__ACCEPT_CLAUSES2                                                      = 1008;  // <_accept_clauses> ::= <accept_clauses>
		final int PROD_ACCEPT_CLAUSES                                                        = 1009;  // <accept_clauses> ::= <accept_clause>
		final int PROD_ACCEPT_CLAUSES2                                                       = 1010;  // <accept_clauses> ::= <accept_clauses> <accept_clause>
		final int PROD_ACCEPT_CLAUSE                                                         = 1011;  // <accept_clause> ::= <at_line_column>
		final int PROD_ACCEPT_CLAUSE_FROM_CRT                                                = 1012;  // <accept_clause> ::= 'FROM_CRT'
		final int PROD_ACCEPT_CLAUSE2                                                        = 1013;  // <accept_clause> ::= <mode_is_block>
		final int PROD_ACCEPT_CLAUSE3                                                        = 1014;  // <accept_clause> ::= <_with> <accp_attr>
		final int PROD_ACCEPT_CLAUSE_TIME                                                    = 1015;  // <accept_clause> ::= <_before> TIME <positive_id_or_lit>
		final int PROD_LINES_OR_NUMBER_LINES                                                 = 1016;  // <lines_or_number> ::= LINES
		final int PROD_LINES_OR_NUMBER_LINE_NUMBER                                           = 1017;  // <lines_or_number> ::= LINE NUMBER
		final int PROD_AT_LINE_COLUMN                                                        = 1018;  // <at_line_column> ::= <_at> <line_number>
		final int PROD_AT_LINE_COLUMN2                                                       = 1019;  // <at_line_column> ::= <_at> <column_number>
		final int PROD_AT_LINE_COLUMN_AT                                                     = 1020;  // <at_line_column> ::= AT <num_id_or_lit>
		final int PROD_LINE_NUMBER_LINE                                                      = 1021;  // <line_number> ::= LINE <_number> <num_id_or_lit>
		final int PROD_COLUMN_NUMBER                                                         = 1022;  // <column_number> ::= <column_or_col> <_number> <num_id_or_lit>
		final int PROD_COLUMN_NUMBER_POSITION                                                = 1023;  // <column_number> ::= POSITION <_number> <num_id_or_lit>
		final int PROD_MODE_IS_BLOCK_MODE_BLOCK                                              = 1024;  // <mode_is_block> ::= MODE <_is> BLOCK
		final int PROD_ACCP_ATTR_AUTO                                                        = 1025;  // <accp_attr> ::= AUTO
		final int PROD_ACCP_ATTR_TAB                                                         = 1026;  // <accp_attr> ::= TAB
		final int PROD_ACCP_ATTR_BELL                                                        = 1027;  // <accp_attr> ::= BELL
		final int PROD_ACCP_ATTR_BLINK                                                       = 1028;  // <accp_attr> ::= BLINK
		final int PROD_ACCP_ATTR_CONVERSION                                                  = 1029;  // <accp_attr> ::= CONVERSION
		final int PROD_ACCP_ATTR_FULL                                                        = 1030;  // <accp_attr> ::= FULL
		final int PROD_ACCP_ATTR_HIGHLIGHT                                                   = 1031;  // <accp_attr> ::= HIGHLIGHT
		final int PROD_ACCP_ATTR_LEFTLINE                                                    = 1032;  // <accp_attr> ::= LEFTLINE
		final int PROD_ACCP_ATTR_LOWER                                                       = 1033;  // <accp_attr> ::= LOWER
		final int PROD_ACCP_ATTR_LOWLIGHT                                                    = 1034;  // <accp_attr> ::= LOWLIGHT
		final int PROD_ACCP_ATTR                                                             = 1035;  // <accp_attr> ::= <no_echo>
		final int PROD_ACCP_ATTR_OVERLINE                                                    = 1036;  // <accp_attr> ::= OVERLINE
		final int PROD_ACCP_ATTR_PROMPT_CHARACTER                                            = 1037;  // <accp_attr> ::= PROMPT CHARACTER <_is> <id_or_lit>
		final int PROD_ACCP_ATTR_PROMPT                                                      = 1038;  // <accp_attr> ::= PROMPT
		final int PROD_ACCP_ATTR_REQUIRED                                                    = 1039;  // <accp_attr> ::= REQUIRED
		final int PROD_ACCP_ATTR2                                                            = 1040;  // <accp_attr> ::= <reverse_video>
		final int PROD_ACCP_ATTR_SECURE                                                      = 1041;  // <accp_attr> ::= SECURE
		final int PROD_ACCP_ATTR_PROTECTED_SIZE                                              = 1042;  // <accp_attr> ::= PROTECTED SIZE <_is> <num_id_or_lit>
		final int PROD_ACCP_ATTR_SIZE                                                        = 1043;  // <accp_attr> ::= SIZE <_is> <num_id_or_lit>
		final int PROD_ACCP_ATTR_UNDERLINE                                                   = 1044;  // <accp_attr> ::= UNDERLINE
		final int PROD_ACCP_ATTR_NO                                                          = 1045;  // <accp_attr> ::= NO <update_default>
		final int PROD_ACCP_ATTR3                                                            = 1046;  // <accp_attr> ::= <update_default>
		final int PROD_ACCP_ATTR_UPPER                                                       = 1047;  // <accp_attr> ::= UPPER
		final int PROD_ACCP_ATTR_FOREGROUND_COLOR                                            = 1048;  // <accp_attr> ::= 'FOREGROUND_COLOR' <_is> <num_id_or_lit>
		final int PROD_ACCP_ATTR_BACKGROUND_COLOR                                            = 1049;  // <accp_attr> ::= 'BACKGROUND_COLOR' <_is> <num_id_or_lit>
		final int PROD_ACCP_ATTR_SCROLL_UP                                                   = 1050;  // <accp_attr> ::= SCROLL UP <_scroll_lines>
		final int PROD_ACCP_ATTR_SCROLL_DOWN                                                 = 1051;  // <accp_attr> ::= SCROLL DOWN <_scroll_lines>
		final int PROD_ACCP_ATTR_TIME_OUT                                                    = 1052;  // <accp_attr> ::= 'TIME_OUT' <_after> <positive_id_or_lit>
		final int PROD_NO_ECHO_NO_ECHO                                                       = 1053;  // <no_echo> ::= NO ECHO
		final int PROD_NO_ECHO_NO_ECHO2                                                      = 1054;  // <no_echo> ::= 'NO_ECHO'
		final int PROD_NO_ECHO_OFF                                                           = 1055;  // <no_echo> ::= OFF
		final int PROD_REVERSE_VIDEO_REVERSE_VIDEO                                           = 1056;  // <reverse_video> ::= 'REVERSE_VIDEO'
		final int PROD_REVERSE_VIDEO_REVERSED                                                = 1057;  // <reverse_video> ::= REVERSED
		final int PROD_REVERSE_VIDEO_REVERSE                                                 = 1058;  // <reverse_video> ::= REVERSE
		final int PROD_UPDATE_DEFAULT_UPDATE                                                 = 1059;  // <update_default> ::= UPDATE
		final int PROD_UPDATE_DEFAULT_DEFAULT                                                = 1060;  // <update_default> ::= DEFAULT
		final int PROD_END_ACCEPT                                                            = 1061;  // <end_accept> ::= 
		final int PROD_END_ACCEPT_END_ACCEPT                                                 = 1062;  // <end_accept> ::= 'END_ACCEPT'
		final int PROD_ADD_STATEMENT_ADD                                                     = 1063;  // <add_statement> ::= ADD <add_body> <end_add>
		final int PROD_ADD_BODY_TO                                                           = 1064;  // <add_body> ::= <x_list> TO <arithmetic_x_list> <on_size_error_phrases>
		final int PROD_ADD_BODY_GIVING                                                       = 1065;  // <add_body> ::= <x_list> <_add_to> GIVING <arithmetic_x_list> <on_size_error_phrases>
		final int PROD_ADD_BODY_CORRESPONDING_TO                                             = 1066;  // <add_body> ::= CORRESPONDING <identifier> TO <identifier> <flag_rounded> <on_size_error_phrases>
		final int PROD_ADD_BODY_TABLE_TO                                                     = 1067;  // <add_body> ::= TABLE <table_identifier> TO <table_identifier> <flag_rounded> <_from_idx_to_idx> <_dest_index> <on_size_error_phrases>
		final int PROD__ADD_TO                                                               = 1068;  // <_add_to> ::= 
		final int PROD__ADD_TO_TO                                                            = 1069;  // <_add_to> ::= TO <x>
		final int PROD_END_ADD                                                               = 1070;  // <end_add> ::= 
		final int PROD_END_ADD_END_ADD                                                       = 1071;  // <end_add> ::= 'END_ADD'
		final int PROD_ALLOCATE_STATEMENT_ALLOCATE                                           = 1072;  // <allocate_statement> ::= ALLOCATE <allocate_body>
		final int PROD_ALLOCATE_BODY                                                         = 1073;  // <allocate_body> ::= <identifier> <flag_initialized> <allocate_returning>
		final int PROD_ALLOCATE_BODY_CHARACTERS                                              = 1074;  // <allocate_body> ::= <exp> CHARACTERS <flag_initialized_to> <allocate_returning>
		final int PROD_ALLOCATE_RETURNING                                                    = 1075;  // <allocate_returning> ::= 
		final int PROD_ALLOCATE_RETURNING_RETURNING                                          = 1076;  // <allocate_returning> ::= RETURNING <target_x>
		final int PROD_ALTER_STATEMENT_ALTER                                                 = 1077;  // <alter_statement> ::= ALTER <alter_body>
		final int PROD_ALTER_BODY                                                            = 1078;  // <alter_body> ::= <alter_entry>
		final int PROD_ALTER_BODY2                                                           = 1079;  // <alter_body> ::= <alter_body> <alter_entry>
		final int PROD_ALTER_ENTRY_TO                                                        = 1080;  // <alter_entry> ::= <procedure_name> TO <_proceed_to> <procedure_name>
		final int PROD__PROCEED_TO                                                           = 1081;  // <_proceed_to> ::= 
		final int PROD__PROCEED_TO_PROCEED_TO                                                = 1082;  // <_proceed_to> ::= PROCEED TO
		final int PROD_CALL_STATEMENT_CALL                                                   = 1083;  // <call_statement> ::= CALL <call_body> <end_call>
		final int PROD_CALL_BODY                                                             = 1084;  // <call_body> ::= <_mnemonic_conv> <program_or_prototype> <call_using> <call_returning> <call_exception_phrases>
		final int PROD__MNEMONIC_CONV                                                        = 1085;  // <_mnemonic_conv> ::= 
		final int PROD__MNEMONIC_CONV_STATIC                                                 = 1086;  // <_mnemonic_conv> ::= STATIC
		final int PROD__MNEMONIC_CONV_STDCALL                                                = 1087;  // <_mnemonic_conv> ::= STDCALL
		final int PROD__MNEMONIC_CONV_TOK_EXTERN                                             = 1088;  // <_mnemonic_conv> ::= 'TOK_EXTERN'
		final int PROD__MNEMONIC_CONV2                                                       = 1089;  // <_mnemonic_conv> ::= <MNEMONIC_NAME_TOK>
		final int PROD_PROGRAM_OR_PROTOTYPE                                                  = 1090;  // <program_or_prototype> ::= <id_or_lit_or_func>
		final int PROD_PROGRAM_OR_PROTOTYPE2                                                 = 1091;  // <program_or_prototype> ::= <id_or_lit_or_func_as> <PROGRAM_NAME>
		final int PROD_PROGRAM_OR_PROTOTYPE_AS_NESTED                                        = 1092;  // <program_or_prototype> ::= <LITERAL_TOK> AS NESTED
		final int PROD_ID_OR_LIT_OR_FUNC_AS_AS                                               = 1093;  // <id_or_lit_or_func_as> ::= <id_or_lit_or_func> AS
		final int PROD_CALL_USING                                                            = 1094;  // <call_using> ::= 
		final int PROD_CALL_USING_USING                                                      = 1095;  // <call_using> ::= USING <call_param_list>
		final int PROD_CALL_PARAM_LIST                                                       = 1096;  // <call_param_list> ::= <call_param>
		final int PROD_CALL_PARAM_LIST2                                                      = 1097;  // <call_param_list> ::= <call_param_list> <call_param>
		final int PROD_CALL_PARAM_OMITTED                                                    = 1098;  // <call_param> ::= <call_type> OMITTED
		final int PROD_CALL_PARAM                                                            = 1099;  // <call_param> ::= <call_type> <_size_optional> <call_x>
		final int PROD_CALL_PARAM_COMMA_DELIM                                                = 1100;  // <call_param> ::= 'COMMA_DELIM'
		final int PROD_CALL_TYPE                                                             = 1101;  // <call_type> ::= 
		final int PROD_CALL_TYPE_REFERENCE                                                   = 1102;  // <call_type> ::= <_by> REFERENCE
		final int PROD_CALL_TYPE_CONTENT                                                     = 1103;  // <call_type> ::= <_by> CONTENT
		final int PROD_CALL_TYPE_VALUE                                                       = 1104;  // <call_type> ::= <_by> VALUE
		final int PROD_CALL_RETURNING                                                        = 1105;  // <call_returning> ::= 
		final int PROD_CALL_RETURNING2                                                       = 1106;  // <call_returning> ::= <return_give> <_into> <identifier>
		final int PROD_CALL_RETURNING3                                                       = 1107;  // <call_returning> ::= <return_give> <null_or_omitted>
		final int PROD_CALL_RETURNING_NOTHING                                                = 1108;  // <call_returning> ::= <return_give> NOTHING
		final int PROD_CALL_RETURNING_ADDRESS                                                = 1109;  // <call_returning> ::= <return_give> ADDRESS <_of> <identifier>
		final int PROD_RETURN_GIVE_RETURNING                                                 = 1110;  // <return_give> ::= RETURNING
		final int PROD_RETURN_GIVE_GIVING                                                    = 1111;  // <return_give> ::= GIVING
		final int PROD_NULL_OR_OMITTED_TOK_NULL                                              = 1112;  // <null_or_omitted> ::= 'TOK_NULL'
		final int PROD_NULL_OR_OMITTED_OMITTED                                               = 1113;  // <null_or_omitted> ::= OMITTED
		final int PROD_CALL_EXCEPTION_PHRASES                                                = 1114;  // <call_exception_phrases> ::= 
		final int PROD_CALL_EXCEPTION_PHRASES2                                               = 1115;  // <call_exception_phrases> ::= <call_on_exception> <_call_not_on_exception>
		final int PROD_CALL_EXCEPTION_PHRASES3                                               = 1116;  // <call_exception_phrases> ::= <call_not_on_exception> <_call_on_exception>
		final int PROD__CALL_ON_EXCEPTION                                                    = 1117;  // <_call_on_exception> ::= 
		final int PROD__CALL_ON_EXCEPTION2                                                   = 1118;  // <_call_on_exception> ::= <call_on_exception>
		final int PROD_CALL_ON_EXCEPTION_EXCEPTION                                           = 1119;  // <call_on_exception> ::= EXCEPTION <statement_list>
		final int PROD_CALL_ON_EXCEPTION_TOK_OVERFLOW                                        = 1120;  // <call_on_exception> ::= 'TOK_OVERFLOW' <statement_list>
		final int PROD__CALL_NOT_ON_EXCEPTION                                                = 1121;  // <_call_not_on_exception> ::= 
		final int PROD__CALL_NOT_ON_EXCEPTION2                                               = 1122;  // <_call_not_on_exception> ::= <call_not_on_exception>
		final int PROD_CALL_NOT_ON_EXCEPTION_NOT_EXCEPTION                                   = 1123;  // <call_not_on_exception> ::= 'NOT_EXCEPTION' <statement_list>
		final int PROD_END_CALL                                                              = 1124;  // <end_call> ::= 
		final int PROD_END_CALL_END_CALL                                                     = 1125;  // <end_call> ::= 'END_CALL'
		final int PROD_CANCEL_STATEMENT_CANCEL                                               = 1126;  // <cancel_statement> ::= CANCEL <cancel_body>
		final int PROD_CANCEL_BODY                                                           = 1127;  // <cancel_body> ::= <id_or_lit_or_program_name>
		final int PROD_CANCEL_BODY2                                                          = 1128;  // <cancel_body> ::= <cancel_body> <id_or_lit_or_program_name>
		final int PROD_ID_OR_LIT_OR_PROGRAM_NAME                                             = 1129;  // <id_or_lit_or_program_name> ::= <id_or_lit>
		final int PROD_CLOSE_STATEMENT_CLOSE                                                 = 1130;  // <close_statement> ::= CLOSE <close_body>
		final int PROD_CLOSE_BODY                                                            = 1131;  // <close_body> ::= <file_name> <close_option>
		final int PROD_CLOSE_BODY2                                                           = 1132;  // <close_body> ::= <close_body> <file_name> <close_option>
		final int PROD_CLOSE_OPTION                                                          = 1133;  // <close_option> ::= 
		final int PROD_CLOSE_OPTION2                                                         = 1134;  // <close_option> ::= <reel_or_unit>
		final int PROD_CLOSE_OPTION_REMOVAL                                                  = 1135;  // <close_option> ::= <reel_or_unit> <_for> REMOVAL
		final int PROD_CLOSE_OPTION_NO_REWIND                                                = 1136;  // <close_option> ::= <_with> NO REWIND
		final int PROD_CLOSE_OPTION_LOCK                                                     = 1137;  // <close_option> ::= <_with> LOCK
		final int PROD_COMPUTE_STATEMENT_COMPUTE                                             = 1138;  // <compute_statement> ::= COMPUTE <compute_body> <end_compute>
		final int PROD_COMPUTE_BODY                                                          = 1139;  // <compute_body> ::= <arithmetic_x_list> <comp_equal> <exp> <on_size_error_phrases>
		final int PROD_END_COMPUTE                                                           = 1140;  // <end_compute> ::= 
		final int PROD_END_COMPUTE_END_COMPUTE                                               = 1141;  // <end_compute> ::= 'END_COMPUTE'
		final int PROD_COMMIT_STATEMENT_COMMIT                                               = 1142;  // <commit_statement> ::= COMMIT
		final int PROD_CONTINUE_STATEMENT_CONTINUE                                           = 1143;  // <continue_statement> ::= CONTINUE
		final int PROD_DELETE_STATEMENT_DELETE                                               = 1144;  // <delete_statement> ::= DELETE <delete_body> <end_delete>
		final int PROD_DELETE_BODY                                                           = 1145;  // <delete_body> ::= <file_name> <_record> <_retry_phrase> <_invalid_key_phrases>
		final int PROD_DELETE_BODY_TOK_FILE                                                  = 1146;  // <delete_body> ::= 'TOK_FILE' <delete_file_list>
		final int PROD_DELETE_FILE_LIST                                                      = 1147;  // <delete_file_list> ::= <file_name>
		final int PROD_DELETE_FILE_LIST2                                                     = 1148;  // <delete_file_list> ::= <delete_file_list> <file_name>
		final int PROD_END_DELETE                                                            = 1149;  // <end_delete> ::= 
		final int PROD_END_DELETE_END_DELETE                                                 = 1150;  // <end_delete> ::= 'END_DELETE'
		final int PROD_DISABLE_STATEMENT_DISABLE                                             = 1151;  // <disable_statement> ::= DISABLE <enable_disable_handling>
		final int PROD_ENABLE_DISABLE_HANDLING                                               = 1152;  // <enable_disable_handling> ::= <communication_mode> <cd_name> <_enable_disable_key>
		final int PROD__ENABLE_DISABLE_KEY                                                   = 1153;  // <_enable_disable_key> ::= 
		final int PROD__ENABLE_DISABLE_KEY_KEY                                               = 1154;  // <_enable_disable_key> ::= <_with> KEY <id_or_lit>
		final int PROD_COMMUNICATION_MODE                                                    = 1155;  // <communication_mode> ::= 
		final int PROD_COMMUNICATION_MODE_INPUT                                              = 1156;  // <communication_mode> ::= INPUT <_terminal>
		final int PROD_COMMUNICATION_MODE_OUTPUT                                             = 1157;  // <communication_mode> ::= OUTPUT
		final int PROD_COMMUNICATION_MODE_I_O_TERMINAL                                       = 1158;  // <communication_mode> ::= 'I_O' TERMINAL
		final int PROD_COMMUNICATION_MODE_TERMINAL                                           = 1159;  // <communication_mode> ::= TERMINAL
		final int PROD_DISPLAY_STATEMENT_DISPLAY                                             = 1160;  // <display_statement> ::= DISPLAY <display_body> <end_display>
		final int PROD_DISPLAY_BODY_UPON_ENVIRONMENT_NAME                                    = 1161;  // <display_body> ::= <id_or_lit> 'UPON_ENVIRONMENT_NAME' <_display_exception_phrases>
		final int PROD_DISPLAY_BODY_UPON_ENVIRONMENT_VALUE                                   = 1162;  // <display_body> ::= <id_or_lit> 'UPON_ENVIRONMENT_VALUE' <_display_exception_phrases>
		final int PROD_DISPLAY_BODY_UPON_ARGUMENT_NUMBER                                     = 1163;  // <display_body> ::= <id_or_lit> 'UPON_ARGUMENT_NUMBER' <_display_exception_phrases>
		final int PROD_DISPLAY_BODY_UPON_COMMAND_LINE                                        = 1164;  // <display_body> ::= <id_or_lit> 'UPON_COMMAND_LINE' <_display_exception_phrases>
		final int PROD_DISPLAY_BODY                                                          = 1165;  // <display_body> ::= <screen_or_device_display> <_display_exception_phrases>
		final int PROD_SCREEN_OR_DEVICE_DISPLAY                                              = 1166;  // <screen_or_device_display> ::= <display_list> <_x_list>
		final int PROD_SCREEN_OR_DEVICE_DISPLAY2                                             = 1167;  // <screen_or_device_display> ::= <x_list>
		final int PROD_DISPLAY_LIST                                                          = 1168;  // <display_list> ::= <display_atom>
		final int PROD_DISPLAY_LIST2                                                         = 1169;  // <display_list> ::= <display_list> <display_atom>
		final int PROD_DISPLAY_ATOM                                                          = 1170;  // <display_atom> ::= <disp_list> <display_clauses>
		final int PROD_DISP_LIST                                                             = 1171;  // <disp_list> ::= <x_list>
		final int PROD_DISP_LIST_OMITTED                                                     = 1172;  // <disp_list> ::= OMITTED
		final int PROD_DISPLAY_CLAUSES                                                       = 1173;  // <display_clauses> ::= <display_clause>
		final int PROD_DISPLAY_CLAUSES2                                                      = 1174;  // <display_clauses> ::= <display_clauses> <display_clause>
		final int PROD_DISPLAY_CLAUSE                                                        = 1175;  // <display_clause> ::= <display_upon>
		final int PROD_DISPLAY_CLAUSE_NO_ADVANCING                                           = 1176;  // <display_clause> ::= <_with> 'NO_ADVANCING'
		final int PROD_DISPLAY_CLAUSE2                                                       = 1177;  // <display_clause> ::= <mode_is_block>
		final int PROD_DISPLAY_CLAUSE3                                                       = 1178;  // <display_clause> ::= <at_line_column>
		final int PROD_DISPLAY_CLAUSE4                                                       = 1179;  // <display_clause> ::= <_with> <disp_attr>
		final int PROD_DISPLAY_UPON_UPON                                                     = 1180;  // <display_upon> ::= UPON <mnemonic_name>
		final int PROD_DISPLAY_UPON_UPON2                                                    = 1181;  // <display_upon> ::= UPON <WORD>
		final int PROD_DISPLAY_UPON_UPON_PRINTER                                             = 1182;  // <display_upon> ::= UPON PRINTER
		final int PROD_DISPLAY_UPON_UPON3                                                    = 1183;  // <display_upon> ::= UPON <crt_under>
		final int PROD_CRT_UNDER_CRT                                                         = 1184;  // <crt_under> ::= CRT
		final int PROD_CRT_UNDER_CRT_UNDER                                                   = 1185;  // <crt_under> ::= 'CRT_UNDER'
		final int PROD_DISP_ATTR_BELL                                                        = 1186;  // <disp_attr> ::= BELL
		final int PROD_DISP_ATTR_BLANK_LINE                                                  = 1187;  // <disp_attr> ::= BLANK LINE
		final int PROD_DISP_ATTR_BLANK_SCREEN                                                = 1188;  // <disp_attr> ::= BLANK SCREEN
		final int PROD_DISP_ATTR_BLINK                                                       = 1189;  // <disp_attr> ::= BLINK
		final int PROD_DISP_ATTR_CONVERSION                                                  = 1190;  // <disp_attr> ::= CONVERSION
		final int PROD_DISP_ATTR_ERASE                                                       = 1191;  // <disp_attr> ::= ERASE <eol>
		final int PROD_DISP_ATTR_ERASE2                                                      = 1192;  // <disp_attr> ::= ERASE <eos>
		final int PROD_DISP_ATTR_HIGHLIGHT                                                   = 1193;  // <disp_attr> ::= HIGHLIGHT
		final int PROD_DISP_ATTR_LOWLIGHT                                                    = 1194;  // <disp_attr> ::= LOWLIGHT
		final int PROD_DISP_ATTR_OVERLINE                                                    = 1195;  // <disp_attr> ::= OVERLINE
		final int PROD_DISP_ATTR                                                             = 1196;  // <disp_attr> ::= <reverse_video>
		final int PROD_DISP_ATTR_SIZE                                                        = 1197;  // <disp_attr> ::= SIZE <_is> <num_id_or_lit>
		final int PROD_DISP_ATTR_UNDERLINE                                                   = 1198;  // <disp_attr> ::= UNDERLINE
		final int PROD_DISP_ATTR_FOREGROUND_COLOR                                            = 1199;  // <disp_attr> ::= 'FOREGROUND_COLOR' <_is> <num_id_or_lit>
		final int PROD_DISP_ATTR_BACKGROUND_COLOR                                            = 1200;  // <disp_attr> ::= 'BACKGROUND_COLOR' <_is> <num_id_or_lit>
		final int PROD_DISP_ATTR_SCROLL_UP                                                   = 1201;  // <disp_attr> ::= SCROLL UP <_scroll_lines>
		final int PROD_DISP_ATTR_SCROLL_DOWN                                                 = 1202;  // <disp_attr> ::= SCROLL DOWN <_scroll_lines>
		final int PROD_END_DISPLAY                                                           = 1203;  // <end_display> ::= 
		final int PROD_END_DISPLAY_END_DISPLAY                                               = 1204;  // <end_display> ::= 'END_DISPLAY'
		final int PROD_DIVIDE_STATEMENT_DIVIDE                                               = 1205;  // <divide_statement> ::= DIVIDE <divide_body> <end_divide>
		final int PROD_DIVIDE_BODY_INTO                                                      = 1206;  // <divide_body> ::= <x> INTO <arithmetic_x_list> <on_size_error_phrases>
		final int PROD_DIVIDE_BODY_INTO_GIVING                                               = 1207;  // <divide_body> ::= <x> INTO <x> GIVING <arithmetic_x_list> <on_size_error_phrases>
		final int PROD_DIVIDE_BODY_BY_GIVING                                                 = 1208;  // <divide_body> ::= <x> BY <x> GIVING <arithmetic_x_list> <on_size_error_phrases>
		final int PROD_DIVIDE_BODY_INTO_GIVING_REMAINDER                                     = 1209;  // <divide_body> ::= <x> INTO <x> GIVING <arithmetic_x> REMAINDER <arithmetic_x> <on_size_error_phrases>
		final int PROD_DIVIDE_BODY_BY_GIVING_REMAINDER                                       = 1210;  // <divide_body> ::= <x> BY <x> GIVING <arithmetic_x> REMAINDER <arithmetic_x> <on_size_error_phrases>
		final int PROD_END_DIVIDE                                                            = 1211;  // <end_divide> ::= 
		final int PROD_END_DIVIDE_END_DIVIDE                                                 = 1212;  // <end_divide> ::= 'END_DIVIDE'
		final int PROD_ENABLE_STATEMENT_ENABLE                                               = 1213;  // <enable_statement> ::= ENABLE <enable_disable_handling>
		final int PROD_ENTRY_STATEMENT_ENTRY                                                 = 1214;  // <entry_statement> ::= ENTRY <entry_body>
		final int PROD_ENTRY_BODY                                                            = 1215;  // <entry_body> ::= <_mnemonic_conv> <LITERAL_TOK> <call_using>
		final int PROD_EVALUATE_STATEMENT_EVALUATE                                           = 1216;  // <evaluate_statement> ::= EVALUATE <evaluate_body> <end_evaluate>
		final int PROD_EVALUATE_BODY                                                         = 1217;  // <evaluate_body> ::= <evaluate_subject_list> <evaluate_condition_list>
		final int PROD_EVALUATE_SUBJECT_LIST                                                 = 1218;  // <evaluate_subject_list> ::= <evaluate_subject>
		final int PROD_EVALUATE_SUBJECT_LIST_ALSO                                            = 1219;  // <evaluate_subject_list> ::= <evaluate_subject_list> ALSO <evaluate_subject>
		final int PROD_EVALUATE_SUBJECT                                                      = 1220;  // <evaluate_subject> ::= <expr>
		final int PROD_EVALUATE_SUBJECT_TOK_TRUE                                             = 1221;  // <evaluate_subject> ::= 'TOK_TRUE'
		final int PROD_EVALUATE_SUBJECT_TOK_FALSE                                            = 1222;  // <evaluate_subject> ::= 'TOK_FALSE'
		final int PROD_EVALUATE_CONDITION_LIST                                               = 1223;  // <evaluate_condition_list> ::= <evaluate_case_list> <evaluate_other>
		final int PROD_EVALUATE_CONDITION_LIST2                                              = 1224;  // <evaluate_condition_list> ::= <evaluate_case_list>
		final int PROD_EVALUATE_CASE_LIST                                                    = 1225;  // <evaluate_case_list> ::= <evaluate_case>
		final int PROD_EVALUATE_CASE_LIST2                                                   = 1226;  // <evaluate_case_list> ::= <evaluate_case_list> <evaluate_case>
		final int PROD_EVALUATE_CASE                                                         = 1227;  // <evaluate_case> ::= <evaluate_when_list> <statement_list>
		final int PROD_EVALUATE_OTHER_WHEN_OTHER                                             = 1228;  // <evaluate_other> ::= WHEN OTHER <statement_list>
		final int PROD_EVALUATE_WHEN_LIST_WHEN                                               = 1229;  // <evaluate_when_list> ::= WHEN <evaluate_object_list>
		final int PROD_EVALUATE_WHEN_LIST_WHEN2                                              = 1230;  // <evaluate_when_list> ::= <evaluate_when_list> WHEN <evaluate_object_list>
		final int PROD_EVALUATE_OBJECT_LIST                                                  = 1231;  // <evaluate_object_list> ::= <evaluate_object>
		final int PROD_EVALUATE_OBJECT_LIST_ALSO                                             = 1232;  // <evaluate_object_list> ::= <evaluate_object_list> ALSO <evaluate_object>
		final int PROD_EVALUATE_OBJECT                                                       = 1233;  // <evaluate_object> ::= <partial_expr> <_evaluate_thru_expr>
		final int PROD_EVALUATE_OBJECT_ANY                                                   = 1234;  // <evaluate_object> ::= ANY
		final int PROD_EVALUATE_OBJECT_TOK_TRUE                                              = 1235;  // <evaluate_object> ::= 'TOK_TRUE'
		final int PROD_EVALUATE_OBJECT_TOK_FALSE                                             = 1236;  // <evaluate_object> ::= 'TOK_FALSE'
		final int PROD__EVALUATE_THRU_EXPR                                                   = 1237;  // <_evaluate_thru_expr> ::= 
		final int PROD__EVALUATE_THRU_EXPR_THRU                                              = 1238;  // <_evaluate_thru_expr> ::= THRU <expr>
		final int PROD_END_EVALUATE                                                          = 1239;  // <end_evaluate> ::= 
		final int PROD_END_EVALUATE_END_EVALUATE                                             = 1240;  // <end_evaluate> ::= 'END_EVALUATE'
		final int PROD_EXIT_STATEMENT_EXIT                                                   = 1241;  // <exit_statement> ::= EXIT <exit_body>
		final int PROD_EXIT_BODY                                                             = 1242;  // <exit_body> ::= 
		final int PROD_EXIT_BODY_PROGRAM                                                     = 1243;  // <exit_body> ::= PROGRAM <exit_program_returning>
		final int PROD_EXIT_BODY_FUNCTION                                                    = 1244;  // <exit_body> ::= FUNCTION
		final int PROD_EXIT_BODY_PERFORM_CYCLE                                               = 1245;  // <exit_body> ::= PERFORM CYCLE
		final int PROD_EXIT_BODY_PERFORM                                                     = 1246;  // <exit_body> ::= PERFORM
		final int PROD_EXIT_BODY_SECTION                                                     = 1247;  // <exit_body> ::= SECTION
		final int PROD_EXIT_BODY_PARAGRAPH                                                   = 1248;  // <exit_body> ::= PARAGRAPH
		final int PROD_EXIT_PROGRAM_RETURNING                                                = 1249;  // <exit_program_returning> ::= 
		final int PROD_EXIT_PROGRAM_RETURNING2                                               = 1250;  // <exit_program_returning> ::= <return_give> <x>
		final int PROD_FREE_STATEMENT_FREE                                                   = 1251;  // <free_statement> ::= FREE <free_body>
		final int PROD_FREE_BODY                                                             = 1252;  // <free_body> ::= <target_x_list>
		final int PROD_GENERATE_STATEMENT_GENERATE                                           = 1253;  // <generate_statement> ::= GENERATE <generate_body>
		final int PROD_GENERATE_BODY                                                         = 1254;  // <generate_body> ::= <qualified_word>
		final int PROD_GOTO_STATEMENT_GO                                                     = 1255;  // <goto_statement> ::= GO <go_body>
		final int PROD_GO_BODY                                                               = 1256;  // <go_body> ::= <_to> <procedure_name_list> <goto_depending>
		final int PROD_GOTO_DEPENDING                                                        = 1257;  // <goto_depending> ::= 
		final int PROD_GOTO_DEPENDING_DEPENDING                                              = 1258;  // <goto_depending> ::= DEPENDING <_on> <identifier>
		final int PROD_GOBACK_STATEMENT_GOBACK                                               = 1259;  // <goback_statement> ::= GOBACK <exit_program_returning>
		final int PROD_IF_STATEMENT_IF                                                       = 1260;  // <if_statement> ::= IF <condition> <_then> <if_else_statements> <end_if>
		final int PROD_IF_ELSE_STATEMENTS_ELSE                                               = 1261;  // <if_else_statements> ::= <statement_list> ELSE <statement_list>
		final int PROD_IF_ELSE_STATEMENTS_ELSE2                                              = 1262;  // <if_else_statements> ::= ELSE <statement_list>
		final int PROD_IF_ELSE_STATEMENTS                                                    = 1263;  // <if_else_statements> ::= <statement_list>
		final int PROD_END_IF                                                                = 1264;  // <end_if> ::= 
		final int PROD_END_IF_END_IF                                                         = 1265;  // <end_if> ::= 'END_IF'
		final int PROD_INITIALIZE_STATEMENT_INITIALIZE                                       = 1266;  // <initialize_statement> ::= INITIALIZE <initialize_body>
		final int PROD_INITIALIZE_BODY                                                       = 1267;  // <initialize_body> ::= <target_x_list> <_initialize_filler> <_initialize_value> <_initialize_replacing> <_initialize_default>
		final int PROD__INITIALIZE_FILLER                                                    = 1268;  // <_initialize_filler> ::= 
		final int PROD__INITIALIZE_FILLER_FILLER                                             = 1269;  // <_initialize_filler> ::= <_with> FILLER
		final int PROD__INITIALIZE_VALUE                                                     = 1270;  // <_initialize_value> ::= 
		final int PROD__INITIALIZE_VALUE_ALL_VALUE                                           = 1271;  // <_initialize_value> ::= ALL <_to> VALUE
		final int PROD__INITIALIZE_VALUE_VALUE                                               = 1272;  // <_initialize_value> ::= <initialize_category> <_to> VALUE
		final int PROD__INITIALIZE_REPLACING                                                 = 1273;  // <_initialize_replacing> ::= 
		final int PROD__INITIALIZE_REPLACING_REPLACING                                       = 1274;  // <_initialize_replacing> ::= <_then> REPLACING <initialize_replacing_list>
		final int PROD_INITIALIZE_REPLACING_LIST                                             = 1275;  // <initialize_replacing_list> ::= <initialize_replacing_item>
		final int PROD_INITIALIZE_REPLACING_LIST2                                            = 1276;  // <initialize_replacing_list> ::= <initialize_replacing_list> <initialize_replacing_item>
		final int PROD_INITIALIZE_REPLACING_ITEM_BY                                          = 1277;  // <initialize_replacing_item> ::= <initialize_category> <_data> BY <x>
		final int PROD_INITIALIZE_CATEGORY_ALPHABETIC                                        = 1278;  // <initialize_category> ::= ALPHABETIC
		final int PROD_INITIALIZE_CATEGORY_ALPHANUMERIC                                      = 1279;  // <initialize_category> ::= ALPHANUMERIC
		final int PROD_INITIALIZE_CATEGORY_NUMERIC                                           = 1280;  // <initialize_category> ::= NUMERIC
		final int PROD_INITIALIZE_CATEGORY_ALPHANUMERIC_EDITED                               = 1281;  // <initialize_category> ::= 'ALPHANUMERIC_EDITED'
		final int PROD_INITIALIZE_CATEGORY_NUMERIC_EDITED                                    = 1282;  // <initialize_category> ::= 'NUMERIC_EDITED'
		final int PROD_INITIALIZE_CATEGORY_NATIONAL                                          = 1283;  // <initialize_category> ::= NATIONAL
		final int PROD_INITIALIZE_CATEGORY_NATIONAL_EDITED                                   = 1284;  // <initialize_category> ::= 'NATIONAL_EDITED'
		final int PROD__INITIALIZE_DEFAULT                                                   = 1285;  // <_initialize_default> ::= 
		final int PROD__INITIALIZE_DEFAULT_DEFAULT                                           = 1286;  // <_initialize_default> ::= <_then> <_to> DEFAULT
		final int PROD_INITIATE_STATEMENT_INITIATE                                           = 1287;  // <initiate_statement> ::= INITIATE <initiate_body>
		final int PROD_INITIATE_BODY                                                         = 1288;  // <initiate_body> ::= <report_name>
		final int PROD_INITIATE_BODY2                                                        = 1289;  // <initiate_body> ::= <initiate_body> <report_name>
		final int PROD_INSPECT_STATEMENT_INSPECT                                             = 1290;  // <inspect_statement> ::= INSPECT <inspect_body>
		final int PROD_INSPECT_BODY                                                          = 1291;  // <inspect_body> ::= <send_identifier> <inspect_list>
		final int PROD_SEND_IDENTIFIER                                                       = 1292;  // <send_identifier> ::= <identifier>
		final int PROD_SEND_IDENTIFIER2                                                      = 1293;  // <send_identifier> ::= <literal>
		final int PROD_SEND_IDENTIFIER3                                                      = 1294;  // <send_identifier> ::= <function>
		final int PROD_INSPECT_LIST                                                          = 1295;  // <inspect_list> ::= <inspect_tallying> <inspect_replacing>
		final int PROD_INSPECT_LIST2                                                         = 1296;  // <inspect_list> ::= <inspect_tallying>
		final int PROD_INSPECT_LIST3                                                         = 1297;  // <inspect_list> ::= <inspect_replacing>
		final int PROD_INSPECT_LIST4                                                         = 1298;  // <inspect_list> ::= <inspect_converting>
		final int PROD_INSPECT_TALLYING_TALLYING                                             = 1299;  // <inspect_tallying> ::= TALLYING <tallying_list>
		final int PROD_INSPECT_REPLACING_REPLACING                                           = 1300;  // <inspect_replacing> ::= REPLACING <replacing_list>
		final int PROD_INSPECT_CONVERTING_CONVERTING_TO                                      = 1301;  // <inspect_converting> ::= CONVERTING <simple_display_value> TO <simple_display_all_value> <inspect_region>
		final int PROD_TALLYING_LIST                                                         = 1302;  // <tallying_list> ::= <tallying_item>
		final int PROD_TALLYING_LIST2                                                        = 1303;  // <tallying_list> ::= <tallying_list> <tallying_item>
		final int PROD_TALLYING_ITEM_FOR                                                     = 1304;  // <tallying_item> ::= <numeric_identifier> FOR
		final int PROD_TALLYING_ITEM_CHARACTERS                                              = 1305;  // <tallying_item> ::= CHARACTERS <inspect_region>
		final int PROD_TALLYING_ITEM_ALL                                                     = 1306;  // <tallying_item> ::= ALL
		final int PROD_TALLYING_ITEM_LEADING                                                 = 1307;  // <tallying_item> ::= LEADING
		final int PROD_TALLYING_ITEM_TRAILING                                                = 1308;  // <tallying_item> ::= TRAILING
		final int PROD_TALLYING_ITEM                                                         = 1309;  // <tallying_item> ::= <simple_display_value> <inspect_region>
		final int PROD_REPLACING_LIST                                                        = 1310;  // <replacing_list> ::= <replacing_item>
		final int PROD_REPLACING_LIST2                                                       = 1311;  // <replacing_list> ::= <replacing_list> <replacing_item>
		final int PROD_REPLACING_ITEM_CHARACTERS_BY                                          = 1312;  // <replacing_item> ::= CHARACTERS BY <simple_display_value> <inspect_region>
		final int PROD_REPLACING_ITEM                                                        = 1313;  // <replacing_item> ::= <rep_keyword> <replacing_region>
		final int PROD_REP_KEYWORD                                                           = 1314;  // <rep_keyword> ::= 
		final int PROD_REP_KEYWORD_ALL                                                       = 1315;  // <rep_keyword> ::= ALL
		final int PROD_REP_KEYWORD_LEADING                                                   = 1316;  // <rep_keyword> ::= LEADING
		final int PROD_REP_KEYWORD_FIRST                                                     = 1317;  // <rep_keyword> ::= FIRST
		final int PROD_REP_KEYWORD_TRAILING                                                  = 1318;  // <rep_keyword> ::= TRAILING
		final int PROD_REPLACING_REGION_BY                                                   = 1319;  // <replacing_region> ::= <simple_display_value> BY <simple_display_all_value> <inspect_region>
		final int PROD_INSPECT_REGION                                                        = 1320;  // <inspect_region> ::= 
		final int PROD_INSPECT_REGION2                                                       = 1321;  // <inspect_region> ::= <inspect_before>
		final int PROD_INSPECT_REGION3                                                       = 1322;  // <inspect_region> ::= <inspect_after>
		final int PROD_INSPECT_REGION4                                                       = 1323;  // <inspect_region> ::= <inspect_before> <inspect_after>
		final int PROD_INSPECT_REGION5                                                       = 1324;  // <inspect_region> ::= <inspect_after> <inspect_before>
		final int PROD_INSPECT_BEFORE_BEFORE                                                 = 1325;  // <inspect_before> ::= BEFORE <_initial> <x>
		final int PROD_INSPECT_AFTER_AFTER                                                   = 1326;  // <inspect_after> ::= AFTER <_initial> <x>
		final int PROD_MERGE_STATEMENT_MERGE                                                 = 1327;  // <merge_statement> ::= MERGE <sort_body>
		final int PROD_MOVE_STATEMENT_MOVE                                                   = 1328;  // <move_statement> ::= MOVE <move_body>
		final int PROD_MOVE_BODY_TO                                                          = 1329;  // <move_body> ::= <x> TO <target_x_list>
		final int PROD_MOVE_BODY_CORRESPONDING_TO                                            = 1330;  // <move_body> ::= CORRESPONDING <x> TO <target_x_list>
		final int PROD_MULTIPLY_STATEMENT_MULTIPLY                                           = 1331;  // <multiply_statement> ::= MULTIPLY <multiply_body> <end_multiply>
		final int PROD_MULTIPLY_BODY_BY                                                      = 1332;  // <multiply_body> ::= <x> BY <arithmetic_x_list> <on_size_error_phrases>
		final int PROD_MULTIPLY_BODY_BY_GIVING                                               = 1333;  // <multiply_body> ::= <x> BY <x> GIVING <arithmetic_x_list> <on_size_error_phrases>
		final int PROD_END_MULTIPLY                                                          = 1334;  // <end_multiply> ::= 
		final int PROD_END_MULTIPLY_END_MULTIPLY                                             = 1335;  // <end_multiply> ::= 'END_MULTIPLY'
		final int PROD_OPEN_STATEMENT_OPEN                                                   = 1336;  // <open_statement> ::= OPEN <open_body>
		final int PROD_OPEN_BODY                                                             = 1337;  // <open_body> ::= <open_file_entry>
		final int PROD_OPEN_BODY2                                                            = 1338;  // <open_body> ::= <open_body> <open_file_entry>
		final int PROD_OPEN_FILE_ENTRY                                                       = 1339;  // <open_file_entry> ::= <open_mode> <open_sharing> <_retry_phrase> <file_name_list> <open_option>
		final int PROD_OPEN_MODE_INPUT                                                       = 1340;  // <open_mode> ::= INPUT
		final int PROD_OPEN_MODE_OUTPUT                                                      = 1341;  // <open_mode> ::= OUTPUT
		final int PROD_OPEN_MODE_I_O                                                         = 1342;  // <open_mode> ::= 'I_O'
		final int PROD_OPEN_MODE_EXTEND                                                      = 1343;  // <open_mode> ::= EXTEND
		final int PROD_OPEN_SHARING                                                          = 1344;  // <open_sharing> ::= 
		final int PROD_OPEN_SHARING_SHARING                                                  = 1345;  // <open_sharing> ::= SHARING <_with> <sharing_option>
		final int PROD_OPEN_OPTION                                                           = 1346;  // <open_option> ::= 
		final int PROD_OPEN_OPTION_NO_REWIND                                                 = 1347;  // <open_option> ::= <_with> NO REWIND
		final int PROD_OPEN_OPTION_LOCK                                                      = 1348;  // <open_option> ::= <_with> LOCK
		final int PROD_OPEN_OPTION_REVERSED                                                  = 1349;  // <open_option> ::= REVERSED
		final int PROD_PERFORM_STATEMENT_PERFORM                                             = 1350;  // <perform_statement> ::= PERFORM <perform_body>
		final int PROD_PERFORM_BODY                                                          = 1351;  // <perform_body> ::= <perform_procedure> <perform_option>
		final int PROD_PERFORM_BODY2                                                         = 1352;  // <perform_body> ::= <perform_option> <statement_list> <end_perform>
		final int PROD_PERFORM_BODY3                                                         = 1353;  // <perform_body> ::= <perform_option> <term_or_dot>
		final int PROD_END_PERFORM                                                           = 1354;  // <end_perform> ::= 
		final int PROD_END_PERFORM_END_PERFORM                                               = 1355;  // <end_perform> ::= 'END_PERFORM'
		final int PROD_TERM_OR_DOT_END_PERFORM                                               = 1356;  // <term_or_dot> ::= 'END_PERFORM'
		final int PROD_TERM_OR_DOT_TOK_DOT                                                   = 1357;  // <term_or_dot> ::= 'TOK_DOT'
		final int PROD_PERFORM_PROCEDURE                                                     = 1358;  // <perform_procedure> ::= <procedure_name>
		final int PROD_PERFORM_PROCEDURE_THRU                                                = 1359;  // <perform_procedure> ::= <procedure_name> THRU <procedure_name>
		final int PROD_PERFORM_OPTION                                                        = 1360;  // <perform_option> ::= 
		final int PROD_PERFORM_OPTION_TIMES                                                  = 1361;  // <perform_option> ::= <id_or_lit_or_length_or_func> TIMES
		final int PROD_PERFORM_OPTION_FOREVER                                                = 1362;  // <perform_option> ::= FOREVER
		final int PROD_PERFORM_OPTION_UNTIL                                                  = 1363;  // <perform_option> ::= <perform_test> UNTIL <cond_or_exit>
		final int PROD_PERFORM_OPTION_VARYING                                                = 1364;  // <perform_option> ::= <perform_test> VARYING <perform_varying_list>
		final int PROD_PERFORM_TEST                                                          = 1365;  // <perform_test> ::= 
		final int PROD_PERFORM_TEST_TEST                                                     = 1366;  // <perform_test> ::= <_with> TEST <before_or_after>
		final int PROD_COND_OR_EXIT_EXIT                                                     = 1367;  // <cond_or_exit> ::= EXIT
		final int PROD_COND_OR_EXIT                                                          = 1368;  // <cond_or_exit> ::= <condition>
		final int PROD_PERFORM_VARYING_LIST                                                  = 1369;  // <perform_varying_list> ::= <perform_varying>
		final int PROD_PERFORM_VARYING_LIST_AFTER                                            = 1370;  // <perform_varying_list> ::= <perform_varying_list> AFTER <perform_varying>
		final int PROD_PERFORM_VARYING_FROM_BY_UNTIL                                         = 1371;  // <perform_varying> ::= <identifier> FROM <x> BY <x> UNTIL <condition>
		final int PROD_PURGE_STATEMENT_PURGE                                                 = 1372;  // <purge_statement> ::= PURGE <cd_name>
		final int PROD_READ_STATEMENT_READ                                                   = 1373;  // <read_statement> ::= READ <read_body> <end_read>
		final int PROD_READ_BODY                                                             = 1374;  // <read_body> ::= <file_name> <_flag_next> <_record> <read_into> <lock_phrases> <read_key> <read_handler>
		final int PROD_READ_INTO                                                             = 1375;  // <read_into> ::= 
		final int PROD_READ_INTO_INTO                                                        = 1376;  // <read_into> ::= INTO <identifier>
		final int PROD_LOCK_PHRASES                                                          = 1377;  // <lock_phrases> ::= 
		final int PROD_LOCK_PHRASES2                                                         = 1378;  // <lock_phrases> ::= <ignoring_lock>
		final int PROD_LOCK_PHRASES3                                                         = 1379;  // <lock_phrases> ::= <advancing_lock_or_retry> <_extended_with_lock>
		final int PROD_LOCK_PHRASES4                                                         = 1380;  // <lock_phrases> ::= <extended_with_lock>
		final int PROD_IGNORING_LOCK_IGNORING_LOCK                                           = 1381;  // <ignoring_lock> ::= IGNORING LOCK
		final int PROD_IGNORING_LOCK_IGNORE_LOCK                                             = 1382;  // <ignoring_lock> ::= <_with> IGNORE LOCK
		final int PROD_ADVANCING_LOCK_OR_RETRY_ADVANCING_LOCK                                = 1383;  // <advancing_lock_or_retry> ::= ADVANCING <_on> LOCK
		final int PROD_ADVANCING_LOCK_OR_RETRY                                               = 1384;  // <advancing_lock_or_retry> ::= <retry_phrase>
		final int PROD__RETRY_PHRASE                                                         = 1385;  // <_retry_phrase> ::= 
		final int PROD__RETRY_PHRASE2                                                        = 1386;  // <_retry_phrase> ::= <retry_phrase>
		final int PROD_RETRY_PHRASE                                                          = 1387;  // <retry_phrase> ::= <retry_options>
		final int PROD_RETRY_OPTIONS_RETRY_TIMES                                             = 1388;  // <retry_options> ::= RETRY <_for> <exp> TIMES
		final int PROD_RETRY_OPTIONS_RETRY_SECONDS                                           = 1389;  // <retry_options> ::= RETRY <_for> <exp> SECONDS
		final int PROD_RETRY_OPTIONS_RETRY_FOREVER                                           = 1390;  // <retry_options> ::= RETRY FOREVER
		final int PROD__EXTENDED_WITH_LOCK                                                   = 1391;  // <_extended_with_lock> ::= 
		final int PROD__EXTENDED_WITH_LOCK2                                                  = 1392;  // <_extended_with_lock> ::= <extended_with_lock>
		final int PROD_EXTENDED_WITH_LOCK                                                    = 1393;  // <extended_with_lock> ::= <with_lock>
		final int PROD_EXTENDED_WITH_LOCK_KEPT_LOCK                                          = 1394;  // <extended_with_lock> ::= <_with> KEPT LOCK
		final int PROD_EXTENDED_WITH_LOCK_WAIT                                               = 1395;  // <extended_with_lock> ::= <_with> WAIT
		final int PROD_READ_KEY                                                              = 1396;  // <read_key> ::= 
		final int PROD_READ_KEY_KEY                                                          = 1397;  // <read_key> ::= KEY <_is> <identifier>
		final int PROD_READ_HANDLER                                                          = 1398;  // <read_handler> ::= <_invalid_key_phrases>
		final int PROD_READ_HANDLER2                                                         = 1399;  // <read_handler> ::= <at_end>
		final int PROD_END_READ                                                              = 1400;  // <end_read> ::= 
		final int PROD_END_READ_END_READ                                                     = 1401;  // <end_read> ::= 'END_READ'
		final int PROD_READY_STATEMENT_READY_TRACE                                           = 1402;  // <ready_statement> ::= 'READY_TRACE'
		final int PROD_RECEIVE_STATEMENT_RECEIVE                                             = 1403;  // <receive_statement> ::= RECEIVE <receive_body> <end_receive>
		final int PROD_RECEIVE_BODY_INTO                                                     = 1404;  // <receive_body> ::= <cd_name> <message_or_segment> INTO <identifier> <_data_sentence_phrases>
		final int PROD_MESSAGE_OR_SEGMENT_MESSAGE                                            = 1405;  // <message_or_segment> ::= MESSAGE
		final int PROD_MESSAGE_OR_SEGMENT_SEGMENT                                            = 1406;  // <message_or_segment> ::= SEGMENT
		final int PROD__DATA_SENTENCE_PHRASES                                                = 1407;  // <_data_sentence_phrases> ::= 
		final int PROD__DATA_SENTENCE_PHRASES2                                               = 1408;  // <_data_sentence_phrases> ::= <no_data_sentence> <_with_data_sentence>
		final int PROD__DATA_SENTENCE_PHRASES3                                               = 1409;  // <_data_sentence_phrases> ::= <with_data_sentence> <_no_data_sentence>
		final int PROD__NO_DATA_SENTENCE                                                     = 1410;  // <_no_data_sentence> ::= 
		final int PROD__NO_DATA_SENTENCE2                                                    = 1411;  // <_no_data_sentence> ::= <no_data_sentence>
		final int PROD_NO_DATA_SENTENCE_NO_DATA                                              = 1412;  // <no_data_sentence> ::= 'NO_DATA' <statement_list>
		final int PROD__WITH_DATA_SENTENCE                                                   = 1413;  // <_with_data_sentence> ::= 
		final int PROD__WITH_DATA_SENTENCE2                                                  = 1414;  // <_with_data_sentence> ::= <with_data_sentence>
		final int PROD_WITH_DATA_SENTENCE_WITH_DATA                                          = 1415;  // <with_data_sentence> ::= 'WITH_DATA' <statement_list>
		final int PROD_END_RECEIVE                                                           = 1416;  // <end_receive> ::= 
		final int PROD_END_RECEIVE_END_RECEIVE                                               = 1417;  // <end_receive> ::= 'END_RECEIVE'
		final int PROD_RELEASE_STATEMENT_RELEASE                                             = 1418;  // <release_statement> ::= RELEASE <release_body>
		final int PROD_RELEASE_BODY                                                          = 1419;  // <release_body> ::= <record_name> <from_option>
		final int PROD_RESET_STATEMENT_RESET_TRACE                                           = 1420;  // <reset_statement> ::= 'RESET_TRACE'
		final int PROD_RETURN_STATEMENT_RETURN                                               = 1421;  // <return_statement> ::= RETURN <return_body> <end_return>
		final int PROD_RETURN_BODY                                                           = 1422;  // <return_body> ::= <file_name> <_record> <read_into> <return_at_end>
		final int PROD_END_RETURN                                                            = 1423;  // <end_return> ::= 
		final int PROD_END_RETURN_END_RETURN                                                 = 1424;  // <end_return> ::= 'END_RETURN'
		final int PROD_REWRITE_STATEMENT_REWRITE                                             = 1425;  // <rewrite_statement> ::= REWRITE <rewrite_body> <end_rewrite>
		final int PROD_REWRITE_BODY                                                          = 1426;  // <rewrite_body> ::= <file_or_record_name> <from_option> <_retry_phrase> <_with_lock> <_invalid_key_phrases>
		final int PROD__WITH_LOCK                                                            = 1427;  // <_with_lock> ::= 
		final int PROD__WITH_LOCK2                                                           = 1428;  // <_with_lock> ::= <with_lock>
		final int PROD_WITH_LOCK_LOCK                                                        = 1429;  // <with_lock> ::= <_with> LOCK
		final int PROD_WITH_LOCK_NO_LOCK                                                     = 1430;  // <with_lock> ::= <_with> NO LOCK
		final int PROD_END_REWRITE                                                           = 1431;  // <end_rewrite> ::= 
		final int PROD_END_REWRITE_END_REWRITE                                               = 1432;  // <end_rewrite> ::= 'END_REWRITE'
		final int PROD_ROLLBACK_STATEMENT_ROLLBACK                                           = 1433;  // <rollback_statement> ::= ROLLBACK
		final int PROD_SEARCH_STATEMENT_SEARCH                                               = 1434;  // <search_statement> ::= SEARCH <search_body> <end_search>
		final int PROD_SEARCH_BODY                                                           = 1435;  // <search_body> ::= <table_name> <search_varying> <search_at_end> <search_whens>
		final int PROD_SEARCH_BODY_ALL_WHEN                                                  = 1436;  // <search_body> ::= ALL <table_name> <search_at_end> WHEN <expr> <statement_list>
		final int PROD_SEARCH_VARYING                                                        = 1437;  // <search_varying> ::= 
		final int PROD_SEARCH_VARYING_VARYING                                                = 1438;  // <search_varying> ::= VARYING <identifier>
		final int PROD_SEARCH_AT_END                                                         = 1439;  // <search_at_end> ::= 
		final int PROD_SEARCH_AT_END_END                                                     = 1440;  // <search_at_end> ::= END <statement_list>
		final int PROD_SEARCH_WHENS                                                          = 1441;  // <search_whens> ::= <search_when>
		final int PROD_SEARCH_WHENS2                                                         = 1442;  // <search_whens> ::= <search_when> <search_whens>
		final int PROD_SEARCH_WHEN_WHEN                                                      = 1443;  // <search_when> ::= WHEN <condition> <statement_list>
		final int PROD_END_SEARCH                                                            = 1444;  // <end_search> ::= 
		final int PROD_END_SEARCH_END_SEARCH                                                 = 1445;  // <end_search> ::= 'END_SEARCH'
		final int PROD_SEND_STATEMENT_SEND                                                   = 1446;  // <send_statement> ::= SEND <send_body>
		final int PROD_SEND_BODY                                                             = 1447;  // <send_body> ::= <cd_name> <from_identifier>
		final int PROD_SEND_BODY2                                                            = 1448;  // <send_body> ::= <cd_name> <_from_identifier> <with_indicator> <write_option> <_replacing_line>
		final int PROD__FROM_IDENTIFIER                                                      = 1449;  // <_from_identifier> ::= 
		final int PROD__FROM_IDENTIFIER2                                                     = 1450;  // <_from_identifier> ::= <from_identifier>
		final int PROD_FROM_IDENTIFIER_FROM                                                  = 1451;  // <from_identifier> ::= FROM <identifier>
		final int PROD_WITH_INDICATOR                                                        = 1452;  // <with_indicator> ::= <_with> <identifier>
		final int PROD_WITH_INDICATOR_ESI                                                    = 1453;  // <with_indicator> ::= <_with> ESI
		final int PROD_WITH_INDICATOR_EMI                                                    = 1454;  // <with_indicator> ::= <_with> EMI
		final int PROD_WITH_INDICATOR_EGI                                                    = 1455;  // <with_indicator> ::= <_with> EGI
		final int PROD__REPLACING_LINE                                                       = 1456;  // <_replacing_line> ::= 
		final int PROD__REPLACING_LINE_REPLACING                                             = 1457;  // <_replacing_line> ::= REPLACING <_line>
		final int PROD_SET_STATEMENT_SET                                                     = 1458;  // <set_statement> ::= SET <set_body>
		final int PROD_SET_BODY                                                              = 1459;  // <set_body> ::= <set_environment>
		final int PROD_SET_BODY2                                                             = 1460;  // <set_body> ::= <set_attr>
		final int PROD_SET_BODY3                                                             = 1461;  // <set_body> ::= <set_to>
		final int PROD_SET_BODY4                                                             = 1462;  // <set_body> ::= <set_up_down>
		final int PROD_SET_BODY5                                                             = 1463;  // <set_body> ::= <set_to_on_off_sequence>
		final int PROD_SET_BODY6                                                             = 1464;  // <set_body> ::= <set_to_true_false_sequence>
		final int PROD_SET_BODY7                                                             = 1465;  // <set_body> ::= <set_last_exception_to_off>
		final int PROD_ON_OR_OFF_ON                                                          = 1466;  // <on_or_off> ::= ON
		final int PROD_ON_OR_OFF_OFF                                                         = 1467;  // <on_or_off> ::= OFF
		final int PROD_UP_OR_DOWN_UP                                                         = 1468;  // <up_or_down> ::= UP
		final int PROD_UP_OR_DOWN_DOWN                                                       = 1469;  // <up_or_down> ::= DOWN
		final int PROD_SET_ENVIRONMENT_ENVIRONMENT_TO                                        = 1470;  // <set_environment> ::= ENVIRONMENT <simple_display_value> TO <simple_display_value>
		final int PROD_SET_ATTR_ATTRIBUTE                                                    = 1471;  // <set_attr> ::= <sub_identifier> ATTRIBUTE <set_attr_clause>
		final int PROD_SET_ATTR_CLAUSE                                                       = 1472;  // <set_attr_clause> ::= <set_attr_one>
		final int PROD_SET_ATTR_CLAUSE2                                                      = 1473;  // <set_attr_clause> ::= <set_attr_clause> <set_attr_one>
		final int PROD_SET_ATTR_ONE_BELL                                                     = 1474;  // <set_attr_one> ::= BELL <on_or_off>
		final int PROD_SET_ATTR_ONE_BLINK                                                    = 1475;  // <set_attr_one> ::= BLINK <on_or_off>
		final int PROD_SET_ATTR_ONE_HIGHLIGHT                                                = 1476;  // <set_attr_one> ::= HIGHLIGHT <on_or_off>
		final int PROD_SET_ATTR_ONE_LOWLIGHT                                                 = 1477;  // <set_attr_one> ::= LOWLIGHT <on_or_off>
		final int PROD_SET_ATTR_ONE_REVERSE_VIDEO                                            = 1478;  // <set_attr_one> ::= 'REVERSE_VIDEO' <on_or_off>
		final int PROD_SET_ATTR_ONE_UNDERLINE                                                = 1479;  // <set_attr_one> ::= UNDERLINE <on_or_off>
		final int PROD_SET_ATTR_ONE_LEFTLINE                                                 = 1480;  // <set_attr_one> ::= LEFTLINE <on_or_off>
		final int PROD_SET_ATTR_ONE_OVERLINE                                                 = 1481;  // <set_attr_one> ::= OVERLINE <on_or_off>
		final int PROD_SET_TO_TO_ENTRY                                                       = 1482;  // <set_to> ::= <target_x_list> TO ENTRY <alnum_or_id>
		final int PROD_SET_TO_TO                                                             = 1483;  // <set_to> ::= <target_x_list> TO <x>
		final int PROD_SET_UP_DOWN_BY                                                        = 1484;  // <set_up_down> ::= <target_x_list> <up_or_down> BY <x>
		final int PROD_SET_TO_ON_OFF_SEQUENCE                                                = 1485;  // <set_to_on_off_sequence> ::= <set_to_on_off>
		final int PROD_SET_TO_ON_OFF_SEQUENCE2                                               = 1486;  // <set_to_on_off_sequence> ::= <set_to_on_off_sequence> <set_to_on_off>
		final int PROD_SET_TO_ON_OFF_TO                                                      = 1487;  // <set_to_on_off> ::= <mnemonic_name_list> TO <on_or_off>
		final int PROD_SET_TO_TRUE_FALSE_SEQUENCE                                            = 1488;  // <set_to_true_false_sequence> ::= <set_to_true_false>
		final int PROD_SET_TO_TRUE_FALSE_SEQUENCE2                                           = 1489;  // <set_to_true_false_sequence> ::= <set_to_true_false_sequence> <set_to_true_false>
		final int PROD_SET_TO_TRUE_FALSE_TO_TOK_TRUE                                         = 1490;  // <set_to_true_false> ::= <target_x_list> TO 'TOK_TRUE'
		final int PROD_SET_TO_TRUE_FALSE_TO_TOK_FALSE                                        = 1491;  // <set_to_true_false> ::= <target_x_list> TO 'TOK_FALSE'
		final int PROD_SET_LAST_EXCEPTION_TO_OFF_LAST_EXCEPTION_TO_OFF                       = 1492;  // <set_last_exception_to_off> ::= LAST EXCEPTION TO OFF
		final int PROD_SORT_STATEMENT_SORT                                                   = 1493;  // <sort_statement> ::= SORT <sort_body>
		final int PROD_SORT_BODY                                                             = 1494;  // <sort_body> ::= <table_identifier> <sort_key_list> <_sort_duplicates> <sort_collating> <sort_input> <sort_output>
		final int PROD_SORT_KEY_LIST                                                         = 1495;  // <sort_key_list> ::= 
		final int PROD_SORT_KEY_LIST2                                                        = 1496;  // <sort_key_list> ::= <sort_key_list> <_on> <ascending_or_descending> <_key> <_key_list>
		final int PROD__KEY_LIST                                                             = 1497;  // <_key_list> ::= 
		final int PROD__KEY_LIST2                                                            = 1498;  // <_key_list> ::= <_key_list> <qualified_word>
		final int PROD__SORT_DUPLICATES                                                      = 1499;  // <_sort_duplicates> ::= 
		final int PROD__SORT_DUPLICATES2                                                     = 1500;  // <_sort_duplicates> ::= <with_dups> <_in_order>
		final int PROD_SORT_COLLATING                                                        = 1501;  // <sort_collating> ::= 
		final int PROD_SORT_COLLATING2                                                       = 1502;  // <sort_collating> ::= <coll_sequence> <_is> <reference>
		final int PROD_SORT_INPUT                                                            = 1503;  // <sort_input> ::= 
		final int PROD_SORT_INPUT_USING                                                      = 1504;  // <sort_input> ::= USING <file_name_list>
		final int PROD_SORT_INPUT_INPUT_PROCEDURE                                            = 1505;  // <sort_input> ::= INPUT PROCEDURE <_is> <perform_procedure>
		final int PROD_SORT_OUTPUT                                                           = 1506;  // <sort_output> ::= 
		final int PROD_SORT_OUTPUT_GIVING                                                    = 1507;  // <sort_output> ::= GIVING <file_name_list>
		final int PROD_SORT_OUTPUT_OUTPUT_PROCEDURE                                          = 1508;  // <sort_output> ::= OUTPUT PROCEDURE <_is> <perform_procedure>
		final int PROD_START_STATEMENT_START                                                 = 1509;  // <start_statement> ::= START <start_body> <end_start>
		final int PROD_START_BODY                                                            = 1510;  // <start_body> ::= <file_name> <start_key> <sizelen_clause> <_invalid_key_phrases>
		final int PROD_SIZELEN_CLAUSE                                                        = 1511;  // <sizelen_clause> ::= 
		final int PROD_SIZELEN_CLAUSE2                                                       = 1512;  // <sizelen_clause> ::= <_with> <size_or_length> <exp>
		final int PROD_START_KEY                                                             = 1513;  // <start_key> ::= 
		final int PROD_START_KEY_KEY                                                         = 1514;  // <start_key> ::= KEY <_is> <start_op> <identifier>
		final int PROD_START_KEY_FIRST                                                       = 1515;  // <start_key> ::= FIRST
		final int PROD_START_KEY_LAST                                                        = 1516;  // <start_key> ::= LAST
		final int PROD_START_OP                                                              = 1517;  // <start_op> ::= <eq>
		final int PROD_START_OP2                                                             = 1518;  // <start_op> ::= <_flag_not> <gt>
		final int PROD_START_OP3                                                             = 1519;  // <start_op> ::= <_flag_not> <lt>
		final int PROD_START_OP4                                                             = 1520;  // <start_op> ::= <_flag_not> <ge>
		final int PROD_START_OP5                                                             = 1521;  // <start_op> ::= <_flag_not> <le>
		final int PROD_START_OP6                                                             = 1522;  // <start_op> ::= <disallowed_op>
		final int PROD_DISALLOWED_OP                                                         = 1523;  // <disallowed_op> ::= <not_equal_op>
		final int PROD_NOT_EQUAL_OP_NOT                                                      = 1524;  // <not_equal_op> ::= NOT <eq>
		final int PROD_NOT_EQUAL_OP_NOT_EQUAL                                                = 1525;  // <not_equal_op> ::= 'NOT_EQUAL'
		final int PROD_END_START                                                             = 1526;  // <end_start> ::= 
		final int PROD_END_START_END_START                                                   = 1527;  // <end_start> ::= 'END_START'
		final int PROD_STOP_STATEMENT_STOP_RUN                                               = 1528;  // <stop_statement> ::= STOP RUN <stop_returning>
		final int PROD_STOP_STATEMENT_STOP                                                   = 1529;  // <stop_statement> ::= STOP <stop_literal>
		final int PROD_STOP_RETURNING                                                        = 1530;  // <stop_returning> ::= 
		final int PROD_STOP_RETURNING2                                                       = 1531;  // <stop_returning> ::= <return_give> <x>
		final int PROD_STOP_RETURNING3                                                       = 1532;  // <stop_returning> ::= <x>
		final int PROD_STOP_RETURNING_ERROR                                                  = 1533;  // <stop_returning> ::= <_with> ERROR <_status> <_status_x>
		final int PROD_STOP_RETURNING_NORMAL                                                 = 1534;  // <stop_returning> ::= <_with> NORMAL <_status> <_status_x>
		final int PROD__STATUS_X                                                             = 1535;  // <_status_x> ::= 
		final int PROD__STATUS_X2                                                            = 1536;  // <_status_x> ::= <x>
		final int PROD_STOP_LITERAL                                                          = 1537;  // <stop_literal> ::= <LITERAL_TOK>
		final int PROD_STOP_LITERAL_SPACE                                                    = 1538;  // <stop_literal> ::= SPACE
		final int PROD_STOP_LITERAL_ZERO                                                     = 1539;  // <stop_literal> ::= ZERO
		final int PROD_STOP_LITERAL_QUOTE                                                    = 1540;  // <stop_literal> ::= QUOTE
		final int PROD_STRING_STATEMENT_STRING                                               = 1541;  // <string_statement> ::= STRING <string_body> <end_string>
		final int PROD_STRING_BODY_INTO                                                      = 1542;  // <string_body> ::= <string_item_list> INTO <identifier> <_with_pointer> <_on_overflow_phrases>
		final int PROD_STRING_ITEM_LIST                                                      = 1543;  // <string_item_list> ::= <string_item>
		final int PROD_STRING_ITEM_LIST2                                                     = 1544;  // <string_item_list> ::= <string_item_list> <string_item>
		final int PROD_STRING_ITEM                                                           = 1545;  // <string_item> ::= <x> <_string_delimited>
		final int PROD__STRING_DELIMITED                                                     = 1546;  // <_string_delimited> ::= 
		final int PROD__STRING_DELIMITED_DELIMITED                                           = 1547;  // <_string_delimited> ::= DELIMITED <_by> <string_delimiter>
		final int PROD_STRING_DELIMITER_SIZE                                                 = 1548;  // <string_delimiter> ::= SIZE
		final int PROD_STRING_DELIMITER                                                      = 1549;  // <string_delimiter> ::= <x>
		final int PROD__WITH_POINTER                                                         = 1550;  // <_with_pointer> ::= 
		final int PROD__WITH_POINTER_POINTER                                                 = 1551;  // <_with_pointer> ::= <_with> POINTER <_is> <identifier>
		final int PROD_END_STRING                                                            = 1552;  // <end_string> ::= 
		final int PROD_END_STRING_END_STRING                                                 = 1553;  // <end_string> ::= 'END_STRING'
		final int PROD_SUBTRACT_STATEMENT_SUBTRACT                                           = 1554;  // <subtract_statement> ::= SUBTRACT <subtract_body> <end_subtract>
		final int PROD_SUBTRACT_BODY_FROM                                                    = 1555;  // <subtract_body> ::= <x_list> FROM <arithmetic_x_list> <on_size_error_phrases>
		final int PROD_SUBTRACT_BODY_FROM_GIVING                                             = 1556;  // <subtract_body> ::= <x_list> FROM <x> GIVING <arithmetic_x_list> <on_size_error_phrases>
		final int PROD_SUBTRACT_BODY_CORRESPONDING_FROM                                      = 1557;  // <subtract_body> ::= CORRESPONDING <identifier> FROM <identifier> <flag_rounded> <on_size_error_phrases>
		final int PROD_SUBTRACT_BODY_TABLE_FROM                                              = 1558;  // <subtract_body> ::= TABLE <table_identifier> FROM <table_identifier> <flag_rounded> <_from_idx_to_idx> <_dest_index> <on_size_error_phrases>
		final int PROD_END_SUBTRACT                                                          = 1559;  // <end_subtract> ::= 
		final int PROD_END_SUBTRACT_END_SUBTRACT                                             = 1560;  // <end_subtract> ::= 'END_SUBTRACT'
		final int PROD_SUPPRESS_STATEMENT_SUPPRESS                                           = 1561;  // <suppress_statement> ::= SUPPRESS <_printing>
		final int PROD__PRINTING                                                             = 1562;  // <_printing> ::= 
		final int PROD__PRINTING_PRINTING                                                    = 1563;  // <_printing> ::= PRINTING
		final int PROD_TERMINATE_STATEMENT_TERMINATE                                         = 1564;  // <terminate_statement> ::= TERMINATE <terminate_body>
		final int PROD_TERMINATE_BODY                                                        = 1565;  // <terminate_body> ::= <report_name>
		final int PROD_TERMINATE_BODY2                                                       = 1566;  // <terminate_body> ::= <terminate_body> <report_name>
		final int PROD_TRANSFORM_STATEMENT_TRANSFORM                                         = 1567;  // <transform_statement> ::= TRANSFORM <transform_body>
		final int PROD_TRANSFORM_BODY_FROM_TO                                                = 1568;  // <transform_body> ::= <display_identifier> FROM <simple_display_value> TO <simple_display_all_value>
		final int PROD_UNLOCK_STATEMENT_UNLOCK                                               = 1569;  // <unlock_statement> ::= UNLOCK <unlock_body>
		final int PROD_UNLOCK_BODY                                                           = 1570;  // <unlock_body> ::= <file_name> <_records>
		final int PROD_UNSTRING_STATEMENT_UNSTRING                                           = 1571;  // <unstring_statement> ::= UNSTRING <unstring_body> <end_unstring>
		final int PROD_UNSTRING_BODY                                                         = 1572;  // <unstring_body> ::= <identifier> <_unstring_delimited> <unstring_into> <_with_pointer> <_unstring_tallying> <_on_overflow_phrases>
		final int PROD__UNSTRING_DELIMITED                                                   = 1573;  // <_unstring_delimited> ::= 
		final int PROD__UNSTRING_DELIMITED_DELIMITED                                         = 1574;  // <_unstring_delimited> ::= DELIMITED <_by> <unstring_delimited_list>
		final int PROD_UNSTRING_DELIMITED_LIST                                               = 1575;  // <unstring_delimited_list> ::= <unstring_delimited_item>
		final int PROD_UNSTRING_DELIMITED_LIST_OR                                            = 1576;  // <unstring_delimited_list> ::= <unstring_delimited_list> OR <unstring_delimited_item>
		final int PROD_UNSTRING_DELIMITED_ITEM                                               = 1577;  // <unstring_delimited_item> ::= <flag_all> <simple_display_value>
		final int PROD_UNSTRING_INTO_INTO                                                    = 1578;  // <unstring_into> ::= INTO <unstring_into_item>
		final int PROD_UNSTRING_INTO                                                         = 1579;  // <unstring_into> ::= <unstring_into> <unstring_into_item>
		final int PROD_UNSTRING_INTO_ITEM                                                    = 1580;  // <unstring_into_item> ::= <identifier> <_unstring_into_delimiter> <_unstring_into_count>
		final int PROD_UNSTRING_INTO_ITEM_COMMA_DELIM                                        = 1581;  // <unstring_into_item> ::= 'COMMA_DELIM'
		final int PROD__UNSTRING_INTO_DELIMITER                                              = 1582;  // <_unstring_into_delimiter> ::= 
		final int PROD__UNSTRING_INTO_DELIMITER_DELIMITER                                    = 1583;  // <_unstring_into_delimiter> ::= DELIMITER <_in> <identifier>
		final int PROD__UNSTRING_INTO_COUNT                                                  = 1584;  // <_unstring_into_count> ::= 
		final int PROD__UNSTRING_INTO_COUNT_COUNT                                            = 1585;  // <_unstring_into_count> ::= COUNT <_in> <identifier>
		final int PROD__UNSTRING_TALLYING                                                    = 1586;  // <_unstring_tallying> ::= 
		final int PROD__UNSTRING_TALLYING_TALLYING                                           = 1587;  // <_unstring_tallying> ::= TALLYING <_in> <identifier>
		final int PROD_END_UNSTRING                                                          = 1588;  // <end_unstring> ::= 
		final int PROD_END_UNSTRING_END_UNSTRING                                             = 1589;  // <end_unstring> ::= 'END_UNSTRING'
		final int PROD_USE_STATEMENT_USE                                                     = 1590;  // <use_statement> ::= USE <use_phrase>
		final int PROD_USE_PHRASE                                                            = 1591;  // <use_phrase> ::= <use_file_exception>
		final int PROD_USE_PHRASE2                                                           = 1592;  // <use_phrase> ::= <use_debugging>
		final int PROD_USE_PHRASE3                                                           = 1593;  // <use_phrase> ::= <use_start_end>
		final int PROD_USE_PHRASE4                                                           = 1594;  // <use_phrase> ::= <use_reporting>
		final int PROD_USE_PHRASE5                                                           = 1595;  // <use_phrase> ::= <use_exception>
		final int PROD_USE_FILE_EXCEPTION                                                    = 1596;  // <use_file_exception> ::= <use_global> <_after> <_standard> <exception_or_error> <_procedure> <_on> <use_file_exception_target>
		final int PROD_USE_GLOBAL                                                            = 1597;  // <use_global> ::= 
		final int PROD_USE_GLOBAL_GLOBAL                                                     = 1598;  // <use_global> ::= GLOBAL
		final int PROD_USE_FILE_EXCEPTION_TARGET                                             = 1599;  // <use_file_exception_target> ::= <file_name_list>
		final int PROD_USE_FILE_EXCEPTION_TARGET_INPUT                                       = 1600;  // <use_file_exception_target> ::= INPUT
		final int PROD_USE_FILE_EXCEPTION_TARGET_OUTPUT                                      = 1601;  // <use_file_exception_target> ::= OUTPUT
		final int PROD_USE_FILE_EXCEPTION_TARGET_I_O                                         = 1602;  // <use_file_exception_target> ::= 'I_O'
		final int PROD_USE_FILE_EXCEPTION_TARGET_EXTEND                                      = 1603;  // <use_file_exception_target> ::= EXTEND
		final int PROD_USE_DEBUGGING_DEBUGGING                                               = 1604;  // <use_debugging> ::= <_for> DEBUGGING <_on> <debugging_list>
		final int PROD_DEBUGGING_LIST                                                        = 1605;  // <debugging_list> ::= <debugging_target>
		final int PROD_DEBUGGING_LIST2                                                       = 1606;  // <debugging_list> ::= <debugging_list> <debugging_target>
		final int PROD_DEBUGGING_TARGET                                                      = 1607;  // <debugging_target> ::= <label>
		final int PROD_DEBUGGING_TARGET_ALL_PROCEDURES                                       = 1608;  // <debugging_target> ::= ALL PROCEDURES
		final int PROD_DEBUGGING_TARGET_ALL                                                  = 1609;  // <debugging_target> ::= ALL <_all_refs> <qualified_word>
		final int PROD__ALL_REFS                                                             = 1610;  // <_all_refs> ::= 
		final int PROD__ALL_REFS_REFERENCES                                                  = 1611;  // <_all_refs> ::= REFERENCES
		final int PROD__ALL_REFS_REFERENCES_OF                                               = 1612;  // <_all_refs> ::= REFERENCES OF
		final int PROD__ALL_REFS_OF                                                          = 1613;  // <_all_refs> ::= OF
		final int PROD_USE_START_END_PROGRAM                                                 = 1614;  // <use_start_end> ::= <_at> PROGRAM <program_start_end>
		final int PROD_PROGRAM_START_END_START                                               = 1615;  // <program_start_end> ::= START
		final int PROD_PROGRAM_START_END_END                                                 = 1616;  // <program_start_end> ::= END
		final int PROD_USE_REPORTING_BEFORE_REPORTING                                        = 1617;  // <use_reporting> ::= <use_global> BEFORE REPORTING <identifier>
		final int PROD_USE_EXCEPTION                                                         = 1618;  // <use_exception> ::= <use_ex_keyw>
		final int PROD_USE_EX_KEYW_EXCEPTION_CONDITION                                       = 1619;  // <use_ex_keyw> ::= 'EXCEPTION_CONDITION'
		final int PROD_USE_EX_KEYW_EC                                                        = 1620;  // <use_ex_keyw> ::= EC
		final int PROD_WRITE_STATEMENT_WRITE                                                 = 1621;  // <write_statement> ::= WRITE <write_body> <end_write>
		final int PROD_WRITE_BODY                                                            = 1622;  // <write_body> ::= <file_or_record_name> <from_option> <write_option> <_retry_phrase> <_with_lock> <write_handler>
		final int PROD_FROM_OPTION                                                           = 1623;  // <from_option> ::= 
		final int PROD_FROM_OPTION_FROM                                                      = 1624;  // <from_option> ::= FROM <from_parameter>
		final int PROD_WRITE_OPTION                                                          = 1625;  // <write_option> ::= 
		final int PROD_WRITE_OPTION2                                                         = 1626;  // <write_option> ::= <before_or_after> <_advancing> <num_id_or_lit> <_line_or_lines>
		final int PROD_WRITE_OPTION3                                                         = 1627;  // <write_option> ::= <before_or_after> <_advancing> <mnemonic_name>
		final int PROD_WRITE_OPTION_PAGE                                                     = 1628;  // <write_option> ::= <before_or_after> <_advancing> PAGE
		final int PROD_BEFORE_OR_AFTER_BEFORE                                                = 1629;  // <before_or_after> ::= BEFORE
		final int PROD_BEFORE_OR_AFTER_AFTER                                                 = 1630;  // <before_or_after> ::= AFTER
		final int PROD_WRITE_HANDLER                                                         = 1631;  // <write_handler> ::= 
		final int PROD_WRITE_HANDLER2                                                        = 1632;  // <write_handler> ::= <invalid_key_phrases>
		final int PROD_WRITE_HANDLER3                                                        = 1633;  // <write_handler> ::= <at_eop_clauses>
		final int PROD_END_WRITE                                                             = 1634;  // <end_write> ::= 
		final int PROD_END_WRITE_END_WRITE                                                   = 1635;  // <end_write> ::= 'END_WRITE'
		final int PROD__ACCEPT_EXCEPTION_PHRASES                                             = 1636;  // <_accept_exception_phrases> ::= 
		final int PROD__ACCEPT_EXCEPTION_PHRASES2                                            = 1637;  // <_accept_exception_phrases> ::= <accp_on_exception> <_accp_not_on_exception>
		final int PROD__ACCEPT_EXCEPTION_PHRASES3                                            = 1638;  // <_accept_exception_phrases> ::= <accp_not_on_exception> <_accp_on_exception>
		final int PROD__ACCP_ON_EXCEPTION                                                    = 1639;  // <_accp_on_exception> ::= 
		final int PROD__ACCP_ON_EXCEPTION2                                                   = 1640;  // <_accp_on_exception> ::= <accp_on_exception>
		final int PROD_ACCP_ON_EXCEPTION                                                     = 1641;  // <accp_on_exception> ::= <escape_or_exception> <statement_list>
		final int PROD_ESCAPE_OR_EXCEPTION_ESCAPE                                            = 1642;  // <escape_or_exception> ::= ESCAPE
		final int PROD_ESCAPE_OR_EXCEPTION_EXCEPTION                                         = 1643;  // <escape_or_exception> ::= EXCEPTION
		final int PROD__ACCP_NOT_ON_EXCEPTION                                                = 1644;  // <_accp_not_on_exception> ::= 
		final int PROD__ACCP_NOT_ON_EXCEPTION2                                               = 1645;  // <_accp_not_on_exception> ::= <accp_not_on_exception>
		final int PROD_ACCP_NOT_ON_EXCEPTION                                                 = 1646;  // <accp_not_on_exception> ::= <not_escape_or_not_exception> <statement_list>
		final int PROD_NOT_ESCAPE_OR_NOT_EXCEPTION_NOT_ESCAPE                                = 1647;  // <not_escape_or_not_exception> ::= 'NOT_ESCAPE'
		final int PROD_NOT_ESCAPE_OR_NOT_EXCEPTION_NOT_EXCEPTION                             = 1648;  // <not_escape_or_not_exception> ::= 'NOT_EXCEPTION'
		final int PROD__DISPLAY_EXCEPTION_PHRASES                                            = 1649;  // <_display_exception_phrases> ::= 
		final int PROD__DISPLAY_EXCEPTION_PHRASES2                                           = 1650;  // <_display_exception_phrases> ::= <disp_on_exception> <_disp_not_on_exception>
		final int PROD__DISPLAY_EXCEPTION_PHRASES3                                           = 1651;  // <_display_exception_phrases> ::= <disp_not_on_exception> <_disp_on_exception>
		final int PROD__DISP_ON_EXCEPTION                                                    = 1652;  // <_disp_on_exception> ::= 
		final int PROD__DISP_ON_EXCEPTION2                                                   = 1653;  // <_disp_on_exception> ::= <disp_on_exception>
		final int PROD_DISP_ON_EXCEPTION_EXCEPTION                                           = 1654;  // <disp_on_exception> ::= EXCEPTION <statement_list>
		final int PROD__DISP_NOT_ON_EXCEPTION                                                = 1655;  // <_disp_not_on_exception> ::= 
		final int PROD__DISP_NOT_ON_EXCEPTION2                                               = 1656;  // <_disp_not_on_exception> ::= <disp_not_on_exception>
		final int PROD_DISP_NOT_ON_EXCEPTION_NOT_EXCEPTION                                   = 1657;  // <disp_not_on_exception> ::= 'NOT_EXCEPTION' <statement_list>
		final int PROD_ON_SIZE_ERROR_PHRASES                                                 = 1658;  // <on_size_error_phrases> ::= 
		final int PROD_ON_SIZE_ERROR_PHRASES2                                                = 1659;  // <on_size_error_phrases> ::= <on_size_error> <_not_on_size_error>
		final int PROD_ON_SIZE_ERROR_PHRASES3                                                = 1660;  // <on_size_error_phrases> ::= <not_on_size_error> <_on_size_error>
		final int PROD__ON_SIZE_ERROR                                                        = 1661;  // <_on_size_error> ::= 
		final int PROD__ON_SIZE_ERROR2                                                       = 1662;  // <_on_size_error> ::= <on_size_error>
		final int PROD_ON_SIZE_ERROR_SIZE_ERROR                                              = 1663;  // <on_size_error> ::= 'SIZE_ERROR' <statement_list>
		final int PROD__NOT_ON_SIZE_ERROR                                                    = 1664;  // <_not_on_size_error> ::= 
		final int PROD__NOT_ON_SIZE_ERROR2                                                   = 1665;  // <_not_on_size_error> ::= <not_on_size_error>
		final int PROD_NOT_ON_SIZE_ERROR_NOT_SIZE_ERROR                                      = 1666;  // <not_on_size_error> ::= 'NOT_SIZE_ERROR' <statement_list>
		final int PROD__ON_OVERFLOW_PHRASES                                                  = 1667;  // <_on_overflow_phrases> ::= 
		final int PROD__ON_OVERFLOW_PHRASES2                                                 = 1668;  // <_on_overflow_phrases> ::= <on_overflow> <_not_on_overflow>
		final int PROD__ON_OVERFLOW_PHRASES3                                                 = 1669;  // <_on_overflow_phrases> ::= <not_on_overflow> <_on_overflow>
		final int PROD__ON_OVERFLOW                                                          = 1670;  // <_on_overflow> ::= 
		final int PROD__ON_OVERFLOW2                                                         = 1671;  // <_on_overflow> ::= <on_overflow>
		final int PROD_ON_OVERFLOW_TOK_OVERFLOW                                              = 1672;  // <on_overflow> ::= 'TOK_OVERFLOW' <statement_list>
		final int PROD__NOT_ON_OVERFLOW                                                      = 1673;  // <_not_on_overflow> ::= 
		final int PROD__NOT_ON_OVERFLOW2                                                     = 1674;  // <_not_on_overflow> ::= <not_on_overflow>
		final int PROD_NOT_ON_OVERFLOW_NOT_OVERFLOW                                          = 1675;  // <not_on_overflow> ::= 'NOT_OVERFLOW' <statement_list>
		final int PROD_RETURN_AT_END                                                         = 1676;  // <return_at_end> ::= <at_end_clause> <_not_at_end_clause>
		final int PROD_RETURN_AT_END2                                                        = 1677;  // <return_at_end> ::= <not_at_end_clause> <at_end_clause>
		final int PROD_AT_END                                                                = 1678;  // <at_end> ::= <at_end_clause> <_not_at_end_clause>
		final int PROD_AT_END2                                                               = 1679;  // <at_end> ::= <not_at_end_clause> <_at_end_clause>
		final int PROD__AT_END_CLAUSE                                                        = 1680;  // <_at_end_clause> ::= 
		final int PROD__AT_END_CLAUSE2                                                       = 1681;  // <_at_end_clause> ::= <at_end_clause>
		final int PROD_AT_END_CLAUSE_END                                                     = 1682;  // <at_end_clause> ::= END <statement_list>
		final int PROD__NOT_AT_END_CLAUSE                                                    = 1683;  // <_not_at_end_clause> ::= 
		final int PROD__NOT_AT_END_CLAUSE2                                                   = 1684;  // <_not_at_end_clause> ::= <not_at_end_clause>
		final int PROD_NOT_AT_END_CLAUSE_NOT_END                                             = 1685;  // <not_at_end_clause> ::= 'NOT_END' <statement_list>
		final int PROD_AT_EOP_CLAUSES                                                        = 1686;  // <at_eop_clauses> ::= <at_eop_clause> <_not_at_eop_clause>
		final int PROD_AT_EOP_CLAUSES2                                                       = 1687;  // <at_eop_clauses> ::= <not_at_eop_clause> <_at_eop_clause>
		final int PROD__AT_EOP_CLAUSE                                                        = 1688;  // <_at_eop_clause> ::= 
		final int PROD__AT_EOP_CLAUSE2                                                       = 1689;  // <_at_eop_clause> ::= <at_eop_clause>
		final int PROD_AT_EOP_CLAUSE_EOP                                                     = 1690;  // <at_eop_clause> ::= EOP <statement_list>
		final int PROD__NOT_AT_EOP_CLAUSE                                                    = 1691;  // <_not_at_eop_clause> ::= 
		final int PROD__NOT_AT_EOP_CLAUSE2                                                   = 1692;  // <_not_at_eop_clause> ::= <not_at_eop_clause>
		final int PROD_NOT_AT_EOP_CLAUSE_NOT_EOP                                             = 1693;  // <not_at_eop_clause> ::= 'NOT_EOP' <statement_list>
		final int PROD__INVALID_KEY_PHRASES                                                  = 1694;  // <_invalid_key_phrases> ::= 
		final int PROD__INVALID_KEY_PHRASES2                                                 = 1695;  // <_invalid_key_phrases> ::= <invalid_key_phrases>
		final int PROD_INVALID_KEY_PHRASES                                                   = 1696;  // <invalid_key_phrases> ::= <invalid_key_sentence> <_not_invalid_key_sentence>
		final int PROD_INVALID_KEY_PHRASES2                                                  = 1697;  // <invalid_key_phrases> ::= <not_invalid_key_sentence> <_invalid_key_sentence>
		final int PROD__INVALID_KEY_SENTENCE                                                 = 1698;  // <_invalid_key_sentence> ::= 
		final int PROD__INVALID_KEY_SENTENCE2                                                = 1699;  // <_invalid_key_sentence> ::= <invalid_key_sentence>
		final int PROD_INVALID_KEY_SENTENCE_INVALID_KEY                                      = 1700;  // <invalid_key_sentence> ::= 'INVALID_KEY' <statement_list>
		final int PROD__NOT_INVALID_KEY_SENTENCE                                             = 1701;  // <_not_invalid_key_sentence> ::= 
		final int PROD__NOT_INVALID_KEY_SENTENCE2                                            = 1702;  // <_not_invalid_key_sentence> ::= <not_invalid_key_sentence>
		final int PROD_NOT_INVALID_KEY_SENTENCE_NOT_INVALID_KEY                              = 1703;  // <not_invalid_key_sentence> ::= 'NOT_INVALID_KEY' <statement_list>
		final int PROD__SCROLL_LINES                                                         = 1704;  // <_scroll_lines> ::= 
		final int PROD__SCROLL_LINES2                                                        = 1705;  // <_scroll_lines> ::= <pos_num_id_or_lit> <scroll_line_or_lines>
		final int PROD_CONDITION                                                             = 1706;  // <condition> ::= <expr>
		final int PROD_EXPR                                                                  = 1707;  // <expr> ::= <partial_expr>
		final int PROD_PARTIAL_EXPR                                                          = 1708;  // <partial_expr> ::= <expr_tokens>
		final int PROD_EXPR_TOKENS                                                           = 1709;  // <expr_tokens> ::= <expr_token>
		final int PROD_EXPR_TOKENS2                                                          = 1710;  // <expr_tokens> ::= <expr_tokens> <expr_token>
		final int PROD_EXPR_TOKEN                                                            = 1711;  // <expr_token> ::= <x>
		final int PROD_EXPR_TOKEN_IS                                                         = 1712;  // <expr_token> ::= IS <CLASS_NAME>
		final int PROD_EXPR_TOKEN2                                                           = 1713;  // <expr_token> ::= <_is> <condition_op>
		final int PROD_EXPR_TOKEN_IS2                                                        = 1714;  // <expr_token> ::= IS <not> <condition_or_class>
		final int PROD_EXPR_TOKEN_IS_ZERO                                                    = 1715;  // <expr_token> ::= IS <_not> ZERO
		final int PROD_EXPR_TOKEN_TOK_OPEN_PAREN                                             = 1716;  // <expr_token> ::= 'TOK_OPEN_PAREN'
		final int PROD_EXPR_TOKEN_TOK_CLOSE_PAREN                                            = 1717;  // <expr_token> ::= 'TOK_CLOSE_PAREN'
		final int PROD_EXPR_TOKEN_TOK_PLUS                                                   = 1718;  // <expr_token> ::= 'TOK_PLUS'
		final int PROD_EXPR_TOKEN_TOK_MINUS                                                  = 1719;  // <expr_token> ::= 'TOK_MINUS'
		final int PROD_EXPR_TOKEN_TOK_MUL                                                    = 1720;  // <expr_token> ::= 'TOK_MUL'
		final int PROD_EXPR_TOKEN_TOK_DIV                                                    = 1721;  // <expr_token> ::= 'TOK_DIV'
		final int PROD_EXPR_TOKEN_EXPONENTIATION                                             = 1722;  // <expr_token> ::= EXPONENTIATION
		final int PROD_EXPR_TOKEN3                                                           = 1723;  // <expr_token> ::= <not>
		final int PROD_EXPR_TOKEN_AND                                                        = 1724;  // <expr_token> ::= AND
		final int PROD_EXPR_TOKEN_OR                                                         = 1725;  // <expr_token> ::= OR
		final int PROD__NOT                                                                  = 1726;  // <_not> ::= 
		final int PROD__NOT2                                                                 = 1727;  // <_not> ::= <not>
		final int PROD_NOT_NOT                                                               = 1728;  // <not> ::= NOT
		final int PROD_CONDITION_OR_CLASS                                                    = 1729;  // <condition_or_class> ::= <CLASS_NAME>
		final int PROD_CONDITION_OR_CLASS2                                                   = 1730;  // <condition_or_class> ::= <condition_op>
		final int PROD_CONDITION_OP                                                          = 1731;  // <condition_op> ::= <eq>
		final int PROD_CONDITION_OP2                                                         = 1732;  // <condition_op> ::= <gt>
		final int PROD_CONDITION_OP3                                                         = 1733;  // <condition_op> ::= <lt>
		final int PROD_CONDITION_OP4                                                         = 1734;  // <condition_op> ::= <ge>
		final int PROD_CONDITION_OP5                                                         = 1735;  // <condition_op> ::= <le>
		final int PROD_CONDITION_OP_NOT_EQUAL                                                = 1736;  // <condition_op> ::= 'NOT_EQUAL'
		final int PROD_CONDITION_OP_OMITTED                                                  = 1737;  // <condition_op> ::= OMITTED
		final int PROD_CONDITION_OP_NUMERIC                                                  = 1738;  // <condition_op> ::= NUMERIC
		final int PROD_CONDITION_OP_ALPHABETIC                                               = 1739;  // <condition_op> ::= ALPHABETIC
		final int PROD_CONDITION_OP_ALPHABETIC_LOWER                                         = 1740;  // <condition_op> ::= 'ALPHABETIC_LOWER'
		final int PROD_CONDITION_OP_ALPHABETIC_UPPER                                         = 1741;  // <condition_op> ::= 'ALPHABETIC_UPPER'
		final int PROD_CONDITION_OP_POSITIVE                                                 = 1742;  // <condition_op> ::= POSITIVE
		final int PROD_CONDITION_OP_NEGATIVE                                                 = 1743;  // <condition_op> ::= NEGATIVE
		final int PROD_EQ_TOK_EQUAL                                                          = 1744;  // <eq> ::= 'TOK_EQUAL'
		final int PROD_EQ_EQUAL                                                              = 1745;  // <eq> ::= EQUAL <_to>
		final int PROD_GT_TOK_GREATER                                                        = 1746;  // <gt> ::= 'TOK_GREATER'
		final int PROD_GT_GREATER                                                            = 1747;  // <gt> ::= GREATER
		final int PROD_LT_TOK_LESS                                                           = 1748;  // <lt> ::= 'TOK_LESS'
		final int PROD_LT_LESS                                                               = 1749;  // <lt> ::= LESS
		final int PROD_GE_GREATER_OR_EQUAL                                                   = 1750;  // <ge> ::= 'GREATER_OR_EQUAL'
		final int PROD_LE_LESS_OR_EQUAL                                                      = 1751;  // <le> ::= 'LESS_OR_EQUAL'
		final int PROD_EXP_LIST                                                              = 1752;  // <exp_list> ::= <exp>
		final int PROD_EXP_LIST2                                                             = 1753;  // <exp_list> ::= <exp_list> <_e_sep> <exp>
		final int PROD__E_SEP                                                                = 1754;  // <_e_sep> ::= 
		final int PROD__E_SEP_COMMA_DELIM                                                    = 1755;  // <_e_sep> ::= 'COMMA_DELIM'
		final int PROD__E_SEP_SEMI_COLON                                                     = 1756;  // <_e_sep> ::= 'SEMI_COLON'
		final int PROD_EXP_TOK_PLUS                                                          = 1757;  // <exp> ::= <exp> 'TOK_PLUS' <exp_term>
		final int PROD_EXP_TOK_MINUS                                                         = 1758;  // <exp> ::= <exp> 'TOK_MINUS' <exp_term>
		final int PROD_EXP                                                                   = 1759;  // <exp> ::= <exp_term>
		final int PROD_EXP_TERM_TOK_MUL                                                      = 1760;  // <exp_term> ::= <exp_term> 'TOK_MUL' <exp_factor>
		final int PROD_EXP_TERM_TOK_DIV                                                      = 1761;  // <exp_term> ::= <exp_term> 'TOK_DIV' <exp_factor>
		final int PROD_EXP_TERM                                                              = 1762;  // <exp_term> ::= <exp_factor>
		final int PROD_EXP_FACTOR_EXPONENTIATION                                             = 1763;  // <exp_factor> ::= <exp_unary> EXPONENTIATION <exp_factor>
		final int PROD_EXP_FACTOR                                                            = 1764;  // <exp_factor> ::= <exp_unary>
		final int PROD_EXP_UNARY_TOK_PLUS                                                    = 1765;  // <exp_unary> ::= 'TOK_PLUS' <exp_atom>
		final int PROD_EXP_UNARY_TOK_MINUS                                                   = 1766;  // <exp_unary> ::= 'TOK_MINUS' <exp_atom>
		final int PROD_EXP_UNARY                                                             = 1767;  // <exp_unary> ::= <exp_atom>
		final int PROD_EXP_ATOM_TOK_OPEN_PAREN_TOK_CLOSE_PAREN                               = 1768;  // <exp_atom> ::= 'TOK_OPEN_PAREN' <exp> 'TOK_CLOSE_PAREN'
		final int PROD_EXP_ATOM                                                              = 1769;  // <exp_atom> ::= <arith_x>
		final int PROD_LINE_LINAGE_PAGE_COUNTER_LINAGE_COUNTER                               = 1770;  // <line_linage_page_counter> ::= 'LINAGE_COUNTER'
		final int PROD_LINE_LINAGE_PAGE_COUNTER_LINAGE_COUNTER2                              = 1771;  // <line_linage_page_counter> ::= 'LINAGE_COUNTER' <in_of> <WORD>
		final int PROD_LINE_LINAGE_PAGE_COUNTER_LINE_COUNTER                                 = 1772;  // <line_linage_page_counter> ::= 'LINE_COUNTER'
		final int PROD_LINE_LINAGE_PAGE_COUNTER_LINE_COUNTER2                                = 1773;  // <line_linage_page_counter> ::= 'LINE_COUNTER' <in_of> <WORD>
		final int PROD_LINE_LINAGE_PAGE_COUNTER_PAGE_COUNTER                                 = 1774;  // <line_linage_page_counter> ::= 'PAGE_COUNTER'
		final int PROD_LINE_LINAGE_PAGE_COUNTER_PAGE_COUNTER2                                = 1775;  // <line_linage_page_counter> ::= 'PAGE_COUNTER' <in_of> <WORD>
		final int PROD_ARITHMETIC_X_LIST                                                     = 1776;  // <arithmetic_x_list> ::= <arithmetic_x>
		final int PROD_ARITHMETIC_X_LIST2                                                    = 1777;  // <arithmetic_x_list> ::= <arithmetic_x_list> <arithmetic_x>
		final int PROD_ARITHMETIC_X                                                          = 1778;  // <arithmetic_x> ::= <target_x> <flag_rounded>
		final int PROD_RECORD_NAME                                                           = 1779;  // <record_name> ::= <qualified_word>
		final int PROD_FILE_OR_RECORD_NAME                                                   = 1780;  // <file_or_record_name> ::= <record_name>
		final int PROD_FILE_OR_RECORD_NAME_TOK_FILE                                          = 1781;  // <file_or_record_name> ::= 'TOK_FILE' <WORD>
		final int PROD_TABLE_NAME                                                            = 1782;  // <table_name> ::= <qualified_word>
		final int PROD_FILE_NAME_LIST                                                        = 1783;  // <file_name_list> ::= <file_name>
		final int PROD_FILE_NAME_LIST2                                                       = 1784;  // <file_name_list> ::= <file_name_list> <file_name>
		final int PROD_FILE_NAME                                                             = 1785;  // <file_name> ::= <WORD>
		final int PROD_CD_NAME                                                               = 1786;  // <cd_name> ::= <WORD>
		final int PROD_REPORT_NAME                                                           = 1787;  // <report_name> ::= <WORD>
		final int PROD_MNEMONIC_NAME_LIST                                                    = 1788;  // <mnemonic_name_list> ::= <mnemonic_name>
		final int PROD_MNEMONIC_NAME_LIST2                                                   = 1789;  // <mnemonic_name_list> ::= <mnemonic_name_list> <mnemonic_name>
		final int PROD_MNEMONIC_NAME                                                         = 1790;  // <mnemonic_name> ::= <MNEMONIC_NAME_TOK>
		final int PROD_PROCEDURE_NAME_LIST                                                   = 1791;  // <procedure_name_list> ::= 
		final int PROD_PROCEDURE_NAME_LIST2                                                  = 1792;  // <procedure_name_list> ::= <procedure_name_list> <procedure_name>
		final int PROD_PROCEDURE_NAME                                                        = 1793;  // <procedure_name> ::= <label>
		final int PROD_LABEL                                                                 = 1794;  // <label> ::= <qualified_word>
		final int PROD_LABEL2                                                                = 1795;  // <label> ::= <integer_label>
		final int PROD_LABEL3                                                                = 1796;  // <label> ::= <integer_label> <in_of> <integer_label>
		final int PROD_INTEGER_LABEL                                                         = 1797;  // <integer_label> ::= <LITERAL_TOK>
		final int PROD_REFERENCE_LIST                                                        = 1798;  // <reference_list> ::= <reference>
		final int PROD_REFERENCE_LIST2                                                       = 1799;  // <reference_list> ::= <reference_list> <reference>
		final int PROD_REFERENCE                                                             = 1800;  // <reference> ::= <qualified_word>
		final int PROD_SINGLE_REFERENCE                                                      = 1801;  // <single_reference> ::= <WORD>
		final int PROD_OPTIONAL_REFERENCE_LIST                                               = 1802;  // <optional_reference_list> ::= <optional_reference>
		final int PROD_OPTIONAL_REFERENCE_LIST2                                              = 1803;  // <optional_reference_list> ::= <optional_reference_list> <optional_reference>
		final int PROD_OPTIONAL_REFERENCE                                                    = 1804;  // <optional_reference> ::= <WORD>
		final int PROD_REFERENCE_OR_LITERAL                                                  = 1805;  // <reference_or_literal> ::= <reference>
		final int PROD_REFERENCE_OR_LITERAL2                                                 = 1806;  // <reference_or_literal> ::= <LITERAL_TOK>
		final int PROD_UNDEFINED_WORD                                                        = 1807;  // <undefined_word> ::= <WORD>
		final int PROD_UNIQUE_WORD                                                           = 1808;  // <unique_word> ::= <WORD>
		final int PROD_TARGET_X_LIST                                                         = 1809;  // <target_x_list> ::= <target_x>
		final int PROD_TARGET_X_LIST2                                                        = 1810;  // <target_x_list> ::= <target_x_list> <target_x>
		final int PROD_TARGET_X                                                              = 1811;  // <target_x> ::= <target_identifier>
		final int PROD_TARGET_X2                                                             = 1812;  // <target_x> ::= <basic_literal>
		final int PROD_TARGET_X_ADDRESS                                                      = 1813;  // <target_x> ::= ADDRESS <_of> <identifier_1>
		final int PROD_TARGET_X_COMMA_DELIM                                                  = 1814;  // <target_x> ::= 'COMMA_DELIM'
		final int PROD__X_LIST                                                               = 1815;  // <_x_list> ::= 
		final int PROD__X_LIST2                                                              = 1816;  // <_x_list> ::= <x_list>
		final int PROD_X_LIST                                                                = 1817;  // <x_list> ::= <x>
		final int PROD_X_LIST2                                                               = 1818;  // <x_list> ::= <x_list> <x>
		final int PROD_X                                                                     = 1819;  // <x> ::= <identifier>
		final int PROD_X2                                                                    = 1820;  // <x> ::= <x_common>
		final int PROD_X_COMMA_DELIM                                                         = 1821;  // <x> ::= 'COMMA_DELIM'
		final int PROD_CALL_X                                                                = 1822;  // <call_x> ::= <identifier_or_file_name>
		final int PROD_CALL_X2                                                               = 1823;  // <call_x> ::= <x_common>
		final int PROD_X_COMMON                                                              = 1824;  // <x_common> ::= <literal>
		final int PROD_X_COMMON2                                                             = 1825;  // <x_common> ::= <function>
		final int PROD_X_COMMON3                                                             = 1826;  // <x_common> ::= <line_linage_page_counter>
		final int PROD_X_COMMON_LENGTH_OF                                                    = 1827;  // <x_common> ::= 'LENGTH_OF' <identifier_1>
		final int PROD_X_COMMON_LENGTH_OF2                                                   = 1828;  // <x_common> ::= 'LENGTH_OF' <basic_literal>
		final int PROD_X_COMMON_LENGTH_OF3                                                   = 1829;  // <x_common> ::= 'LENGTH_OF' <function>
		final int PROD_X_COMMON_ADDRESS                                                      = 1830;  // <x_common> ::= ADDRESS <_of> <prog_or_entry> <alnum_or_id>
		final int PROD_X_COMMON_ADDRESS2                                                     = 1831;  // <x_common> ::= ADDRESS <_of> <identifier_1>
		final int PROD_X_COMMON4                                                             = 1832;  // <x_common> ::= <MNEMONIC_NAME_TOK>
		final int PROD_REPORT_X_LIST                                                         = 1833;  // <report_x_list> ::= <arith_x>
		final int PROD_REPORT_X_LIST2                                                        = 1834;  // <report_x_list> ::= <report_x_list> <arith_x>
		final int PROD_EXPR_X                                                                = 1835;  // <expr_x> ::= <identifier>
		final int PROD_EXPR_X2                                                               = 1836;  // <expr_x> ::= <basic_literal>
		final int PROD_EXPR_X3                                                               = 1837;  // <expr_x> ::= <function>
		final int PROD_ARITH_X                                                               = 1838;  // <arith_x> ::= <identifier>
		final int PROD_ARITH_X2                                                              = 1839;  // <arith_x> ::= <basic_literal>
		final int PROD_ARITH_X3                                                              = 1840;  // <arith_x> ::= <function>
		final int PROD_ARITH_X4                                                              = 1841;  // <arith_x> ::= <line_linage_page_counter>
		final int PROD_ARITH_X_LENGTH_OF                                                     = 1842;  // <arith_x> ::= 'LENGTH_OF' <identifier_1>
		final int PROD_ARITH_X_LENGTH_OF2                                                    = 1843;  // <arith_x> ::= 'LENGTH_OF' <basic_literal>
		final int PROD_ARITH_X_LENGTH_OF3                                                    = 1844;  // <arith_x> ::= 'LENGTH_OF' <function>
		final int PROD_PROG_OR_ENTRY_PROGRAM                                                 = 1845;  // <prog_or_entry> ::= PROGRAM
		final int PROD_PROG_OR_ENTRY_ENTRY                                                   = 1846;  // <prog_or_entry> ::= ENTRY
		final int PROD_ALNUM_OR_ID                                                           = 1847;  // <alnum_or_id> ::= <identifier_1>
		final int PROD_ALNUM_OR_ID2                                                          = 1848;  // <alnum_or_id> ::= <LITERAL_TOK>
		final int PROD_SIMPLE_DISPLAY_VALUE                                                  = 1849;  // <simple_display_value> ::= <simple_value>
		final int PROD_SIMPLE_DISPLAY_ALL_VALUE                                              = 1850;  // <simple_display_all_value> ::= <simple_all_value>
		final int PROD_SIMPLE_VALUE                                                          = 1851;  // <simple_value> ::= <identifier>
		final int PROD_SIMPLE_VALUE2                                                         = 1852;  // <simple_value> ::= <basic_literal>
		final int PROD_SIMPLE_VALUE3                                                         = 1853;  // <simple_value> ::= <function>
		final int PROD_SIMPLE_ALL_VALUE                                                      = 1854;  // <simple_all_value> ::= <identifier>
		final int PROD_SIMPLE_ALL_VALUE2                                                     = 1855;  // <simple_all_value> ::= <literal>
		final int PROD_ID_OR_LIT                                                             = 1856;  // <id_or_lit> ::= <identifier>
		final int PROD_ID_OR_LIT2                                                            = 1857;  // <id_or_lit> ::= <LITERAL_TOK>
		final int PROD_ID_OR_LIT_OR_FUNC                                                     = 1858;  // <id_or_lit_or_func> ::= <identifier>
		final int PROD_ID_OR_LIT_OR_FUNC2                                                    = 1859;  // <id_or_lit_or_func> ::= <LITERAL_TOK>
		final int PROD_ID_OR_LIT_OR_FUNC3                                                    = 1860;  // <id_or_lit_or_func> ::= <function>
		final int PROD_ID_OR_LIT_OR_LENGTH_OR_FUNC                                           = 1861;  // <id_or_lit_or_length_or_func> ::= <identifier>
		final int PROD_ID_OR_LIT_OR_LENGTH_OR_FUNC2                                          = 1862;  // <id_or_lit_or_length_or_func> ::= <lit_or_length>
		final int PROD_ID_OR_LIT_OR_LENGTH_OR_FUNC3                                          = 1863;  // <id_or_lit_or_length_or_func> ::= <function>
		final int PROD_NUM_ID_OR_LIT                                                         = 1864;  // <num_id_or_lit> ::= <sub_identifier>
		final int PROD_NUM_ID_OR_LIT2                                                        = 1865;  // <num_id_or_lit> ::= <integer>
		final int PROD_NUM_ID_OR_LIT_ZERO                                                    = 1866;  // <num_id_or_lit> ::= ZERO
		final int PROD_POSITIVE_ID_OR_LIT                                                    = 1867;  // <positive_id_or_lit> ::= <sub_identifier>
		final int PROD_POSITIVE_ID_OR_LIT2                                                   = 1868;  // <positive_id_or_lit> ::= <report_integer>
		final int PROD_POS_NUM_ID_OR_LIT                                                     = 1869;  // <pos_num_id_or_lit> ::= <sub_identifier>
		final int PROD_POS_NUM_ID_OR_LIT2                                                    = 1870;  // <pos_num_id_or_lit> ::= <integer>
		final int PROD_FROM_PARAMETER                                                        = 1871;  // <from_parameter> ::= <identifier>
		final int PROD_FROM_PARAMETER2                                                       = 1872;  // <from_parameter> ::= <literal>
		final int PROD_FROM_PARAMETER3                                                       = 1873;  // <from_parameter> ::= <function>
		final int PROD_SUB_IDENTIFIER                                                        = 1874;  // <sub_identifier> ::= <sub_identifier_1>
		final int PROD_TABLE_IDENTIFIER                                                      = 1875;  // <table_identifier> ::= <sub_identifier_1>
		final int PROD_SUB_IDENTIFIER_1                                                      = 1876;  // <sub_identifier_1> ::= <qualified_word>
		final int PROD_SUB_IDENTIFIER_12                                                     = 1877;  // <sub_identifier_1> ::= <qualified_word> <subref>
		final int PROD_DISPLAY_IDENTIFIER                                                    = 1878;  // <display_identifier> ::= <identifier>
		final int PROD_NUMERIC_IDENTIFIER                                                    = 1879;  // <numeric_identifier> ::= <identifier>
		final int PROD_IDENTIFIER_OR_FILE_NAME                                               = 1880;  // <identifier_or_file_name> ::= <identifier_1>
		final int PROD_IDENTIFIER                                                            = 1881;  // <identifier> ::= <identifier_1>
		final int PROD_IDENTIFIER_1                                                          = 1882;  // <identifier_1> ::= <qualified_word> <subref> <refmod>
		final int PROD_IDENTIFIER_12                                                         = 1883;  // <identifier_1> ::= <qualified_word> <subref>
		final int PROD_IDENTIFIER_13                                                         = 1884;  // <identifier_1> ::= <qualified_word> <refmod>
		final int PROD_IDENTIFIER_14                                                         = 1885;  // <identifier_1> ::= <qualified_word>
		final int PROD_TARGET_IDENTIFIER                                                     = 1886;  // <target_identifier> ::= <target_identifier_1>
		final int PROD_TARGET_IDENTIFIER_1                                                   = 1887;  // <target_identifier_1> ::= <qualified_word> <subref> <refmod>
		final int PROD_TARGET_IDENTIFIER_12                                                  = 1888;  // <target_identifier_1> ::= <qualified_word> <subref>
		final int PROD_TARGET_IDENTIFIER_13                                                  = 1889;  // <target_identifier_1> ::= <qualified_word> <refmod>
		final int PROD_TARGET_IDENTIFIER_14                                                  = 1890;  // <target_identifier_1> ::= <qualified_word>
		final int PROD_QUALIFIED_WORD                                                        = 1891;  // <qualified_word> ::= <WORD>
		final int PROD_QUALIFIED_WORD2                                                       = 1892;  // <qualified_word> ::= <WORD> <in_of> <qualified_word>
		final int PROD_SUBREF_TOK_OPEN_PAREN_TOK_CLOSE_PAREN                                 = 1893;  // <subref> ::= 'TOK_OPEN_PAREN' <exp_list> 'TOK_CLOSE_PAREN'
		final int PROD_REFMOD_TOK_OPEN_PAREN_TOK_COLON_TOK_CLOSE_PAREN                       = 1894;  // <refmod> ::= 'TOK_OPEN_PAREN' <exp> 'TOK_COLON' 'TOK_CLOSE_PAREN'
		final int PROD_REFMOD_TOK_OPEN_PAREN_TOK_COLON_TOK_CLOSE_PAREN2                      = 1895;  // <refmod> ::= 'TOK_OPEN_PAREN' <exp> 'TOK_COLON' <exp> 'TOK_CLOSE_PAREN'
		final int PROD_INTEGER_INTLITERAL                                                    = 1896;  // <integer> ::= IntLiteral
		final int PROD_SYMBOLIC_INTEGER_INTLITERAL                                           = 1897;  // <symbolic_integer> ::= IntLiteral
		final int PROD_REPORT_INTEGER_INTLITERAL                                             = 1898;  // <report_integer> ::= IntLiteral
		final int PROD_CLASS_VALUE                                                           = 1899;  // <class_value> ::= <LITERAL_TOK>
		final int PROD_CLASS_VALUE_SPACE                                                     = 1900;  // <class_value> ::= SPACE
		final int PROD_CLASS_VALUE_ZERO                                                      = 1901;  // <class_value> ::= ZERO
		final int PROD_CLASS_VALUE_QUOTE                                                     = 1902;  // <class_value> ::= QUOTE
		final int PROD_CLASS_VALUE_HIGH_VALUE                                                = 1903;  // <class_value> ::= 'HIGH_VALUE'
		final int PROD_CLASS_VALUE_LOW_VALUE                                                 = 1904;  // <class_value> ::= 'LOW_VALUE'
		final int PROD_CLASS_VALUE_TOK_NULL                                                  = 1905;  // <class_value> ::= 'TOK_NULL'
		final int PROD_LITERAL                                                               = 1906;  // <literal> ::= <basic_literal>
		final int PROD_LITERAL_ALL                                                           = 1907;  // <literal> ::= ALL <basic_value>
		final int PROD_BASIC_LITERAL                                                         = 1908;  // <basic_literal> ::= <basic_value>
		final int PROD_BASIC_LITERAL_TOK_AMPER                                               = 1909;  // <basic_literal> ::= <basic_literal> 'TOK_AMPER' <basic_value>
		final int PROD_BASIC_VALUE                                                           = 1910;  // <basic_value> ::= <LITERAL_TOK>
		final int PROD_BASIC_VALUE_SPACE                                                     = 1911;  // <basic_value> ::= SPACE
		final int PROD_BASIC_VALUE_ZERO                                                      = 1912;  // <basic_value> ::= ZERO
		final int PROD_BASIC_VALUE_QUOTE                                                     = 1913;  // <basic_value> ::= QUOTE
		final int PROD_BASIC_VALUE_HIGH_VALUE                                                = 1914;  // <basic_value> ::= 'HIGH_VALUE'
		final int PROD_BASIC_VALUE_LOW_VALUE                                                 = 1915;  // <basic_value> ::= 'LOW_VALUE'
		final int PROD_BASIC_VALUE_TOK_NULL                                                  = 1916;  // <basic_value> ::= 'TOK_NULL'
		final int PROD_FUNCTION                                                              = 1917;  // <function> ::= <func_no_parm> <func_refmod>
		final int PROD_FUNCTION_TOK_OPEN_PAREN_TOK_CLOSE_PAREN                               = 1918;  // <function> ::= <func_one_parm> 'TOK_OPEN_PAREN' <expr_x> 'TOK_CLOSE_PAREN' <func_refmod>
		final int PROD_FUNCTION_TOK_OPEN_PAREN_TOK_CLOSE_PAREN2                              = 1919;  // <function> ::= <func_multi_parm> 'TOK_OPEN_PAREN' <exp_list> 'TOK_CLOSE_PAREN' <func_refmod>
		final int PROD_FUNCTION_TOK_OPEN_PAREN_TOK_CLOSE_PAREN3                              = 1920;  // <function> ::= <TRIM_FUNC> 'TOK_OPEN_PAREN' <trim_args> 'TOK_CLOSE_PAREN' <func_refmod>
		final int PROD_FUNCTION_TOK_OPEN_PAREN_TOK_CLOSE_PAREN4                              = 1921;  // <function> ::= <LENGTH_FUNC> 'TOK_OPEN_PAREN' <length_arg> 'TOK_CLOSE_PAREN'
		final int PROD_FUNCTION_TOK_OPEN_PAREN_TOK_CLOSE_PAREN5                              = 1922;  // <function> ::= <NUMVALC_FUNC> 'TOK_OPEN_PAREN' <numvalc_args> 'TOK_CLOSE_PAREN'
		final int PROD_FUNCTION_TOK_OPEN_PAREN_TOK_CLOSE_PAREN6                              = 1923;  // <function> ::= <LOCALE_DATE_FUNC> 'TOK_OPEN_PAREN' <locale_dt_args> 'TOK_CLOSE_PAREN' <func_refmod>
		final int PROD_FUNCTION_TOK_OPEN_PAREN_TOK_CLOSE_PAREN7                              = 1924;  // <function> ::= <LOCALE_TIME_FUNC> 'TOK_OPEN_PAREN' <locale_dt_args> 'TOK_CLOSE_PAREN' <func_refmod>
		final int PROD_FUNCTION_TOK_OPEN_PAREN_TOK_CLOSE_PAREN8                              = 1925;  // <function> ::= <LOCALE_TIME_FROM_FUNC> 'TOK_OPEN_PAREN' <locale_dt_args> 'TOK_CLOSE_PAREN' <func_refmod>
		final int PROD_FUNCTION_TOK_OPEN_PAREN_TOK_CLOSE_PAREN9                              = 1926;  // <function> ::= <FORMATTED_DATETIME_FUNC> 'TOK_OPEN_PAREN' <formatted_datetime_args> 'TOK_CLOSE_PAREN' <func_refmod>
		final int PROD_FUNCTION_TOK_OPEN_PAREN_TOK_CLOSE_PAREN10                             = 1927;  // <function> ::= <FORMATTED_TIME_FUNC> 'TOK_OPEN_PAREN' <formatted_time_args> 'TOK_CLOSE_PAREN' <func_refmod>
		final int PROD_FUNCTION2                                                             = 1928;  // <function> ::= <FUNCTION_NAME> <func_args>
		final int PROD_FUNCTION3                                                             = 1929;  // <function> ::= <USER_FUNCTION_NAME> <func_args>
		final int PROD_FUNCTION4                                                             = 1930;  // <function> ::= <DISPLAY_OF_FUNC> <func_args>
		final int PROD_FUNCTION5                                                             = 1931;  // <function> ::= <NATIONAL_OF_FUNC> <func_args>
		final int PROD_FUNC_NO_PARM                                                          = 1932;  // <func_no_parm> ::= <CURRENT_DATE_FUNC>
		final int PROD_FUNC_NO_PARM2                                                         = 1933;  // <func_no_parm> ::= <WHEN_COMPILED_FUNC>
		final int PROD_FUNC_ONE_PARM                                                         = 1934;  // <func_one_parm> ::= <UPPER_CASE_FUNC>
		final int PROD_FUNC_ONE_PARM2                                                        = 1935;  // <func_one_parm> ::= <LOWER_CASE_FUNC>
		final int PROD_FUNC_ONE_PARM3                                                        = 1936;  // <func_one_parm> ::= <REVERSE_FUNC>
		final int PROD_FUNC_MULTI_PARM                                                       = 1937;  // <func_multi_parm> ::= <CONCATENATE_FUNC>
		final int PROD_FUNC_MULTI_PARM2                                                      = 1938;  // <func_multi_parm> ::= <FORMATTED_DATE_FUNC>
		final int PROD_FUNC_MULTI_PARM3                                                      = 1939;  // <func_multi_parm> ::= <SUBSTITUTE_FUNC>
		final int PROD_FUNC_MULTI_PARM4                                                      = 1940;  // <func_multi_parm> ::= <SUBSTITUTE_CASE_FUNC>
		final int PROD_FUNC_REFMOD                                                           = 1941;  // <func_refmod> ::= 
		final int PROD_FUNC_REFMOD_TOK_OPEN_PAREN_TOK_COLON_TOK_CLOSE_PAREN                  = 1942;  // <func_refmod> ::= 'TOK_OPEN_PAREN' <exp> 'TOK_COLON' 'TOK_CLOSE_PAREN'
		final int PROD_FUNC_REFMOD_TOK_OPEN_PAREN_TOK_COLON_TOK_CLOSE_PAREN2                 = 1943;  // <func_refmod> ::= 'TOK_OPEN_PAREN' <exp> 'TOK_COLON' <exp> 'TOK_CLOSE_PAREN'
		final int PROD_FUNC_ARGS                                                             = 1944;  // <func_args> ::= 
		final int PROD_FUNC_ARGS_TOK_OPEN_PAREN_TOK_CLOSE_PAREN                              = 1945;  // <func_args> ::= 'TOK_OPEN_PAREN' <exp_list> 'TOK_CLOSE_PAREN'
		final int PROD_FUNC_ARGS_TOK_OPEN_PAREN_TOK_CLOSE_PAREN2                             = 1946;  // <func_args> ::= 'TOK_OPEN_PAREN' 'TOK_CLOSE_PAREN'
		final int PROD_TRIM_ARGS                                                             = 1947;  // <trim_args> ::= <expr_x>
		final int PROD_TRIM_ARGS_LEADING                                                     = 1948;  // <trim_args> ::= <expr_x> <_e_sep> LEADING
		final int PROD_TRIM_ARGS_TRAILING                                                    = 1949;  // <trim_args> ::= <expr_x> <_e_sep> TRAILING
		final int PROD_LENGTH_ARG                                                            = 1950;  // <length_arg> ::= <expr_x>
		final int PROD_NUMVALC_ARGS                                                          = 1951;  // <numvalc_args> ::= <expr_x>
		final int PROD_NUMVALC_ARGS2                                                         = 1952;  // <numvalc_args> ::= <expr_x> <_e_sep> <expr_x>
		final int PROD_LOCALE_DT_ARGS                                                        = 1953;  // <locale_dt_args> ::= <exp>
		final int PROD_LOCALE_DT_ARGS2                                                       = 1954;  // <locale_dt_args> ::= <exp> <_e_sep> <reference>
		final int PROD_FORMATTED_DATETIME_ARGS                                               = 1955;  // <formatted_datetime_args> ::= <exp_list>
		final int PROD_FORMATTED_DATETIME_ARGS_SYSTEM_OFFSET                                 = 1956;  // <formatted_datetime_args> ::= <exp_list> <_e_sep> 'SYSTEM_OFFSET'
		final int PROD_FORMATTED_TIME_ARGS                                                   = 1957;  // <formatted_time_args> ::= <exp_list>
		final int PROD_FORMATTED_TIME_ARGS_SYSTEM_OFFSET                                     = 1958;  // <formatted_time_args> ::= <exp_list> <_e_sep> 'SYSTEM_OFFSET'
		final int PROD_FLAG_ALL                                                              = 1959;  // <flag_all> ::= 
		final int PROD_FLAG_ALL_ALL                                                          = 1960;  // <flag_all> ::= ALL
		final int PROD_FLAG_DUPLICATES                                                       = 1961;  // <flag_duplicates> ::= 
		final int PROD_FLAG_DUPLICATES2                                                      = 1962;  // <flag_duplicates> ::= <with_dups>
		final int PROD_FLAG_INITIALIZED                                                      = 1963;  // <flag_initialized> ::= 
		final int PROD_FLAG_INITIALIZED_INITIALIZED                                          = 1964;  // <flag_initialized> ::= INITIALIZED
		final int PROD_FLAG_INITIALIZED_TO                                                   = 1965;  // <flag_initialized_to> ::= 
		final int PROD_FLAG_INITIALIZED_TO_INITIALIZED                                       = 1966;  // <flag_initialized_to> ::= INITIALIZED <to_init_val>
		final int PROD_TO_INIT_VAL                                                           = 1967;  // <to_init_val> ::= 
		final int PROD_TO_INIT_VAL_TO                                                        = 1968;  // <to_init_val> ::= TO <simple_all_value>
		final int PROD__FLAG_NEXT                                                            = 1969;  // <_flag_next> ::= 
		final int PROD__FLAG_NEXT_NEXT                                                       = 1970;  // <_flag_next> ::= NEXT
		final int PROD__FLAG_NEXT_PREVIOUS                                                   = 1971;  // <_flag_next> ::= PREVIOUS
		final int PROD__FLAG_NOT                                                             = 1972;  // <_flag_not> ::= 
		final int PROD__FLAG_NOT_NOT                                                         = 1973;  // <_flag_not> ::= NOT
		final int PROD_FLAG_OPTIONAL                                                         = 1974;  // <flag_optional> ::= 
		final int PROD_FLAG_OPTIONAL_OPTIONAL                                                = 1975;  // <flag_optional> ::= OPTIONAL
		final int PROD_FLAG_OPTIONAL_NOT_OPTIONAL                                            = 1976;  // <flag_optional> ::= NOT OPTIONAL
		final int PROD_FLAG_ROUNDED                                                          = 1977;  // <flag_rounded> ::= 
		final int PROD_FLAG_ROUNDED_ROUNDED                                                  = 1978;  // <flag_rounded> ::= ROUNDED <round_mode>
		final int PROD_ROUND_MODE                                                            = 1979;  // <round_mode> ::= 
		final int PROD_ROUND_MODE_MODE                                                       = 1980;  // <round_mode> ::= MODE <_is> <round_choice>
		final int PROD_ROUND_CHOICE_AWAY_FROM_ZERO                                           = 1981;  // <round_choice> ::= 'AWAY_FROM_ZERO'
		final int PROD_ROUND_CHOICE_NEAREST_AWAY_FROM_ZERO                                   = 1982;  // <round_choice> ::= 'NEAREST_AWAY_FROM_ZERO'
		final int PROD_ROUND_CHOICE_NEAREST_EVEN                                             = 1983;  // <round_choice> ::= 'NEAREST_EVEN'
		final int PROD_ROUND_CHOICE_NEAREST_TOWARD_ZERO                                      = 1984;  // <round_choice> ::= 'NEAREST_TOWARD_ZERO'
		final int PROD_ROUND_CHOICE_PROHIBITED                                               = 1985;  // <round_choice> ::= PROHIBITED
		final int PROD_ROUND_CHOICE_TOWARD_GREATER                                           = 1986;  // <round_choice> ::= 'TOWARD_GREATER'
		final int PROD_ROUND_CHOICE_TOWARD_LESSER                                            = 1987;  // <round_choice> ::= 'TOWARD_LESSER'
		final int PROD_ROUND_CHOICE_TRUNCATION                                               = 1988;  // <round_choice> ::= TRUNCATION
		final int PROD_FLAG_SEPARATE                                                         = 1989;  // <flag_separate> ::= 
		final int PROD_FLAG_SEPARATE_SEPARATE                                                = 1990;  // <flag_separate> ::= SEPARATE <_character>
		final int PROD__FROM_IDX_TO_IDX                                                      = 1991;  // <_from_idx_to_idx> ::= 
		final int PROD__FROM_IDX_TO_IDX_FROM_TO                                              = 1992;  // <_from_idx_to_idx> ::= FROM <_index> <pos_num_id_or_lit> TO <pos_num_id_or_lit>
		final int PROD__DEST_INDEX                                                           = 1993;  // <_dest_index> ::= 
		final int PROD__DEST_INDEX_DESTINATION                                               = 1994;  // <_dest_index> ::= DESTINATION <_index> <pos_num_id_or_lit>
		final int PROD_ERROR_STMT_RECOVER_TOK_DOT                                            = 1995;  // <error_stmt_recover> ::= 'TOK_DOT'
		final int PROD_ERROR_STMT_RECOVER                                                    = 1996;  // <error_stmt_recover> ::= <verb>
		final int PROD_ERROR_STMT_RECOVER2                                                   = 1997;  // <error_stmt_recover> ::= <scope_terminator>
		final int PROD_VERB_ACCEPT                                                           = 1998;  // <verb> ::= ACCEPT
		final int PROD_VERB_ADD                                                              = 1999;  // <verb> ::= ADD
		final int PROD_VERB_ALLOCATE                                                         = 2000;  // <verb> ::= ALLOCATE
		final int PROD_VERB_ALTER                                                            = 2001;  // <verb> ::= ALTER
		final int PROD_VERB_CALL                                                             = 2002;  // <verb> ::= CALL
		final int PROD_VERB_CANCEL                                                           = 2003;  // <verb> ::= CANCEL
		final int PROD_VERB_CLOSE                                                            = 2004;  // <verb> ::= CLOSE
		final int PROD_VERB_COMMIT                                                           = 2005;  // <verb> ::= COMMIT
		final int PROD_VERB_COMPUTE                                                          = 2006;  // <verb> ::= COMPUTE
		final int PROD_VERB_CONTINUE                                                         = 2007;  // <verb> ::= CONTINUE
		final int PROD_VERB_DELETE                                                           = 2008;  // <verb> ::= DELETE
		final int PROD_VERB_DISPLAY                                                          = 2009;  // <verb> ::= DISPLAY
		final int PROD_VERB_DIVIDE                                                           = 2010;  // <verb> ::= DIVIDE
		final int PROD_VERB_ELSE                                                             = 2011;  // <verb> ::= ELSE
		final int PROD_VERB_ENTRY                                                            = 2012;  // <verb> ::= ENTRY
		final int PROD_VERB_EVALUATE                                                         = 2013;  // <verb> ::= EVALUATE
		final int PROD_VERB_EXIT                                                             = 2014;  // <verb> ::= EXIT
		final int PROD_VERB_FREE                                                             = 2015;  // <verb> ::= FREE
		final int PROD_VERB_GENERATE                                                         = 2016;  // <verb> ::= GENERATE
		final int PROD_VERB_GO                                                               = 2017;  // <verb> ::= GO
		final int PROD_VERB_GOBACK                                                           = 2018;  // <verb> ::= GOBACK
		final int PROD_VERB_IF                                                               = 2019;  // <verb> ::= IF
		final int PROD_VERB_INITIALIZE                                                       = 2020;  // <verb> ::= INITIALIZE
		final int PROD_VERB_INITIATE                                                         = 2021;  // <verb> ::= INITIATE
		final int PROD_VERB_INSPECT                                                          = 2022;  // <verb> ::= INSPECT
		final int PROD_VERB_MERGE                                                            = 2023;  // <verb> ::= MERGE
		final int PROD_VERB_MOVE                                                             = 2024;  // <verb> ::= MOVE
		final int PROD_VERB_MULTIPLY                                                         = 2025;  // <verb> ::= MULTIPLY
		final int PROD_VERB_NEXT                                                             = 2026;  // <verb> ::= NEXT
		final int PROD_VERB_OPEN                                                             = 2027;  // <verb> ::= OPEN
		final int PROD_VERB_PERFORM                                                          = 2028;  // <verb> ::= PERFORM
		final int PROD_VERB_READ                                                             = 2029;  // <verb> ::= READ
		final int PROD_VERB_RELEASE                                                          = 2030;  // <verb> ::= RELEASE
		final int PROD_VERB_RETURN                                                           = 2031;  // <verb> ::= RETURN
		final int PROD_VERB_REWRITE                                                          = 2032;  // <verb> ::= REWRITE
		final int PROD_VERB_ROLLBACK                                                         = 2033;  // <verb> ::= ROLLBACK
		final int PROD_VERB_SEARCH                                                           = 2034;  // <verb> ::= SEARCH
		final int PROD_VERB_SET                                                              = 2035;  // <verb> ::= SET
		final int PROD_VERB_SORT                                                             = 2036;  // <verb> ::= SORT
		final int PROD_VERB_START                                                            = 2037;  // <verb> ::= START
		final int PROD_VERB_STOP                                                             = 2038;  // <verb> ::= STOP
		final int PROD_VERB_STRING                                                           = 2039;  // <verb> ::= STRING
		final int PROD_VERB_SUBTRACT                                                         = 2040;  // <verb> ::= SUBTRACT
		final int PROD_VERB_SUPPRESS                                                         = 2041;  // <verb> ::= SUPPRESS
		final int PROD_VERB_TERMINATE                                                        = 2042;  // <verb> ::= TERMINATE
		final int PROD_VERB_TRANSFORM                                                        = 2043;  // <verb> ::= TRANSFORM
		final int PROD_VERB_UNLOCK                                                           = 2044;  // <verb> ::= UNLOCK
		final int PROD_VERB_UNSTRING                                                         = 2045;  // <verb> ::= UNSTRING
		final int PROD_VERB_WRITE                                                            = 2046;  // <verb> ::= WRITE
		final int PROD_SCOPE_TERMINATOR_END_ACCEPT                                           = 2047;  // <scope_terminator> ::= 'END_ACCEPT'
		final int PROD_SCOPE_TERMINATOR_END_ADD                                              = 2048;  // <scope_terminator> ::= 'END_ADD'
		final int PROD_SCOPE_TERMINATOR_END_CALL                                             = 2049;  // <scope_terminator> ::= 'END_CALL'
		final int PROD_SCOPE_TERMINATOR_END_COMPUTE                                          = 2050;  // <scope_terminator> ::= 'END_COMPUTE'
		final int PROD_SCOPE_TERMINATOR_END_DELETE                                           = 2051;  // <scope_terminator> ::= 'END_DELETE'
		final int PROD_SCOPE_TERMINATOR_END_DISPLAY                                          = 2052;  // <scope_terminator> ::= 'END_DISPLAY'
		final int PROD_SCOPE_TERMINATOR_END_DIVIDE                                           = 2053;  // <scope_terminator> ::= 'END_DIVIDE'
		final int PROD_SCOPE_TERMINATOR_END_EVALUATE                                         = 2054;  // <scope_terminator> ::= 'END_EVALUATE'
		final int PROD_SCOPE_TERMINATOR_END_IF                                               = 2055;  // <scope_terminator> ::= 'END_IF'
		final int PROD_SCOPE_TERMINATOR_END_MULTIPLY                                         = 2056;  // <scope_terminator> ::= 'END_MULTIPLY'
		final int PROD_SCOPE_TERMINATOR_END_PERFORM                                          = 2057;  // <scope_terminator> ::= 'END_PERFORM'
		final int PROD_SCOPE_TERMINATOR_END_READ                                             = 2058;  // <scope_terminator> ::= 'END_READ'
		final int PROD_SCOPE_TERMINATOR_END_RECEIVE                                          = 2059;  // <scope_terminator> ::= 'END_RECEIVE'
		final int PROD_SCOPE_TERMINATOR_END_RETURN                                           = 2060;  // <scope_terminator> ::= 'END_RETURN'
		final int PROD_SCOPE_TERMINATOR_END_REWRITE                                          = 2061;  // <scope_terminator> ::= 'END_REWRITE'
		final int PROD_SCOPE_TERMINATOR_END_SEARCH                                           = 2062;  // <scope_terminator> ::= 'END_SEARCH'
		final int PROD_SCOPE_TERMINATOR_END_START                                            = 2063;  // <scope_terminator> ::= 'END_START'
		final int PROD_SCOPE_TERMINATOR_END_STRING                                           = 2064;  // <scope_terminator> ::= 'END_STRING'
		final int PROD_SCOPE_TERMINATOR_END_SUBTRACT                                         = 2065;  // <scope_terminator> ::= 'END_SUBTRACT'
		final int PROD_SCOPE_TERMINATOR_END_UNSTRING                                         = 2066;  // <scope_terminator> ::= 'END_UNSTRING'
		final int PROD_SCOPE_TERMINATOR_END_WRITE                                            = 2067;  // <scope_terminator> ::= 'END_WRITE'
		final int PROD__ADVANCING                                                            = 2068;  // <_advancing> ::= 
		final int PROD__ADVANCING_ADVANCING                                                  = 2069;  // <_advancing> ::= ADVANCING
		final int PROD__AFTER                                                                = 2070;  // <_after> ::= 
		final int PROD__AFTER_AFTER                                                          = 2071;  // <_after> ::= AFTER
		final int PROD__ARE                                                                  = 2072;  // <_are> ::= 
		final int PROD__ARE_ARE                                                              = 2073;  // <_are> ::= ARE
		final int PROD__AREA                                                                 = 2074;  // <_area> ::= 
		final int PROD__AREA_AREA                                                            = 2075;  // <_area> ::= AREA
		final int PROD__AREAS                                                                = 2076;  // <_areas> ::= 
		final int PROD__AREAS_AREA                                                           = 2077;  // <_areas> ::= AREA
		final int PROD__AREAS_AREAS                                                          = 2078;  // <_areas> ::= AREAS
		final int PROD__AS                                                                   = 2079;  // <_as> ::= 
		final int PROD__AS_AS                                                                = 2080;  // <_as> ::= AS
		final int PROD__AT                                                                   = 2081;  // <_at> ::= 
		final int PROD__AT_AT                                                                = 2082;  // <_at> ::= AT
		final int PROD__BEFORE                                                               = 2083;  // <_before> ::= 
		final int PROD__BEFORE_BEFORE                                                        = 2084;  // <_before> ::= BEFORE
		final int PROD__BINARY                                                               = 2085;  // <_binary> ::= 
		final int PROD__BINARY_BINARY                                                        = 2086;  // <_binary> ::= BINARY
		final int PROD__BY                                                                   = 2087;  // <_by> ::= 
		final int PROD__BY_BY                                                                = 2088;  // <_by> ::= BY
		final int PROD__CHARACTER                                                            = 2089;  // <_character> ::= 
		final int PROD__CHARACTER_CHARACTER                                                  = 2090;  // <_character> ::= CHARACTER
		final int PROD__CHARACTERS                                                           = 2091;  // <_characters> ::= 
		final int PROD__CHARACTERS_CHARACTERS                                                = 2092;  // <_characters> ::= CHARACTERS
		final int PROD__CONTAINS                                                             = 2093;  // <_contains> ::= 
		final int PROD__CONTAINS_CONTAINS                                                    = 2094;  // <_contains> ::= CONTAINS
		final int PROD__DATA                                                                 = 2095;  // <_data> ::= 
		final int PROD__DATA_DATA                                                            = 2096;  // <_data> ::= DATA
		final int PROD__END_OF                                                               = 2097;  // <_end_of> ::= 
		final int PROD__END_OF_END                                                           = 2098;  // <_end_of> ::= END <_of>
		final int PROD__FILE                                                                 = 2099;  // <_file> ::= 
		final int PROD__FILE_TOK_FILE                                                        = 2100;  // <_file> ::= 'TOK_FILE'
		final int PROD__FINAL                                                                = 2101;  // <_final> ::= 
		final int PROD__FINAL_FINAL                                                          = 2102;  // <_final> ::= FINAL
		final int PROD__FOR                                                                  = 2103;  // <_for> ::= 
		final int PROD__FOR_FOR                                                              = 2104;  // <_for> ::= FOR
		final int PROD__FROM                                                                 = 2105;  // <_from> ::= 
		final int PROD__FROM_FROM                                                            = 2106;  // <_from> ::= FROM
		final int PROD__IN                                                                   = 2107;  // <_in> ::= 
		final int PROD__IN_IN                                                                = 2108;  // <_in> ::= IN
		final int PROD__IN_ORDER                                                             = 2109;  // <_in_order> ::= 
		final int PROD__IN_ORDER_ORDER                                                       = 2110;  // <_in_order> ::= ORDER
		final int PROD__IN_ORDER_IN_ORDER                                                    = 2111;  // <_in_order> ::= IN ORDER
		final int PROD__INDEX                                                                = 2112;  // <_index> ::= 
		final int PROD__INDEX_INDEX                                                          = 2113;  // <_index> ::= INDEX
		final int PROD__INDICATE                                                             = 2114;  // <_indicate> ::= 
		final int PROD__INDICATE_INDICATE                                                    = 2115;  // <_indicate> ::= INDICATE
		final int PROD__INITIAL                                                              = 2116;  // <_initial> ::= 
		final int PROD__INITIAL_TOK_INITIAL                                                  = 2117;  // <_initial> ::= 'TOK_INITIAL'
		final int PROD__INTO                                                                 = 2118;  // <_into> ::= 
		final int PROD__INTO_INTO                                                            = 2119;  // <_into> ::= INTO
		final int PROD__IS                                                                   = 2120;  // <_is> ::= 
		final int PROD__IS_IS                                                                = 2121;  // <_is> ::= IS
		final int PROD__IS_ARE                                                               = 2122;  // <_is_are> ::= 
		final int PROD__IS_ARE_IS                                                            = 2123;  // <_is_are> ::= IS
		final int PROD__IS_ARE_ARE                                                           = 2124;  // <_is_are> ::= ARE
		final int PROD__KEY                                                                  = 2125;  // <_key> ::= 
		final int PROD__KEY_KEY                                                              = 2126;  // <_key> ::= KEY
		final int PROD__LEFT_OR_RIGHT                                                        = 2127;  // <_left_or_right> ::= 
		final int PROD__LEFT_OR_RIGHT_LEFT                                                   = 2128;  // <_left_or_right> ::= LEFT
		final int PROD__LEFT_OR_RIGHT_RIGHT                                                  = 2129;  // <_left_or_right> ::= RIGHT
		final int PROD__LINE                                                                 = 2130;  // <_line> ::= 
		final int PROD__LINE_LINE                                                            = 2131;  // <_line> ::= LINE
		final int PROD__LINE_OR_LINES                                                        = 2132;  // <_line_or_lines> ::= 
		final int PROD__LINE_OR_LINES_LINE                                                   = 2133;  // <_line_or_lines> ::= LINE
		final int PROD__LINE_OR_LINES_LINES                                                  = 2134;  // <_line_or_lines> ::= LINES
		final int PROD__LIMITS                                                               = 2135;  // <_limits> ::= 
		final int PROD__LIMITS_LIMIT                                                         = 2136;  // <_limits> ::= LIMIT <_is>
		final int PROD__LIMITS_LIMITS                                                        = 2137;  // <_limits> ::= LIMITS <_are>
		final int PROD__LINES                                                                = 2138;  // <_lines> ::= 
		final int PROD__LINES_LINES                                                          = 2139;  // <_lines> ::= LINES
		final int PROD__MESSAGE                                                              = 2140;  // <_message> ::= 
		final int PROD__MESSAGE_MESSAGE                                                      = 2141;  // <_message> ::= MESSAGE
		final int PROD__MODE                                                                 = 2142;  // <_mode> ::= 
		final int PROD__MODE_MODE                                                            = 2143;  // <_mode> ::= MODE
		final int PROD__NUMBER                                                               = 2144;  // <_number> ::= 
		final int PROD__NUMBER_NUMBER                                                        = 2145;  // <_number> ::= NUMBER
		final int PROD__NUMBERS                                                              = 2146;  // <_numbers> ::= 
		final int PROD__NUMBERS_NUMBER                                                       = 2147;  // <_numbers> ::= NUMBER
		final int PROD__NUMBERS_NUMBERS                                                      = 2148;  // <_numbers> ::= NUMBERS
		final int PROD__OF                                                                   = 2149;  // <_of> ::= 
		final int PROD__OF_OF                                                                = 2150;  // <_of> ::= OF
		final int PROD__ON                                                                   = 2151;  // <_on> ::= 
		final int PROD__ON_ON                                                                = 2152;  // <_on> ::= ON
		final int PROD__ONOFF_STATUS                                                         = 2153;  // <_onoff_status> ::= 
		final int PROD__ONOFF_STATUS_STATUS_IS                                               = 2154;  // <_onoff_status> ::= STATUS IS
		final int PROD__ONOFF_STATUS_STATUS                                                  = 2155;  // <_onoff_status> ::= STATUS
		final int PROD__ONOFF_STATUS_IS                                                      = 2156;  // <_onoff_status> ::= IS
		final int PROD__OTHER                                                                = 2157;  // <_other> ::= 
		final int PROD__OTHER_OTHER                                                          = 2158;  // <_other> ::= OTHER
		final int PROD__PROCEDURE                                                            = 2159;  // <_procedure> ::= 
		final int PROD__PROCEDURE_PROCEDURE                                                  = 2160;  // <_procedure> ::= PROCEDURE
		final int PROD__PROGRAM                                                              = 2161;  // <_program> ::= 
		final int PROD__PROGRAM_PROGRAM                                                      = 2162;  // <_program> ::= PROGRAM
		final int PROD__RECORD                                                               = 2163;  // <_record> ::= 
		final int PROD__RECORD_RECORD                                                        = 2164;  // <_record> ::= RECORD
		final int PROD__RECORDS                                                              = 2165;  // <_records> ::= 
		final int PROD__RECORDS_RECORD                                                       = 2166;  // <_records> ::= RECORD
		final int PROD__RECORDS_RECORDS                                                      = 2167;  // <_records> ::= RECORDS
		final int PROD__RIGHT                                                                = 2168;  // <_right> ::= 
		final int PROD__RIGHT_RIGHT                                                          = 2169;  // <_right> ::= RIGHT
		final int PROD__SIGN                                                                 = 2170;  // <_sign> ::= 
		final int PROD__SIGN_SIGN                                                            = 2171;  // <_sign> ::= SIGN
		final int PROD__SIGNED                                                               = 2172;  // <_signed> ::= 
		final int PROD__SIGNED_SIGNED                                                        = 2173;  // <_signed> ::= SIGNED
		final int PROD__SIGN_IS                                                              = 2174;  // <_sign_is> ::= 
		final int PROD__SIGN_IS_SIGN                                                         = 2175;  // <_sign_is> ::= SIGN
		final int PROD__SIGN_IS_SIGN_IS                                                      = 2176;  // <_sign_is> ::= SIGN IS
		final int PROD__SIZE                                                                 = 2177;  // <_size> ::= 
		final int PROD__SIZE_SIZE                                                            = 2178;  // <_size> ::= SIZE
		final int PROD__STANDARD                                                             = 2179;  // <_standard> ::= 
		final int PROD__STANDARD_STANDARD                                                    = 2180;  // <_standard> ::= STANDARD
		final int PROD__STATUS                                                               = 2181;  // <_status> ::= 
		final int PROD__STATUS_STATUS                                                        = 2182;  // <_status> ::= STATUS
		final int PROD__SYMBOLIC                                                             = 2183;  // <_symbolic> ::= 
		final int PROD__SYMBOLIC_SYMBOLIC                                                    = 2184;  // <_symbolic> ::= SYMBOLIC
		final int PROD__TAPE                                                                 = 2185;  // <_tape> ::= 
		final int PROD__TAPE_TAPE                                                            = 2186;  // <_tape> ::= TAPE
		final int PROD__TERMINAL                                                             = 2187;  // <_terminal> ::= 
		final int PROD__TERMINAL_TERMINAL                                                    = 2188;  // <_terminal> ::= TERMINAL
		final int PROD__THEN                                                                 = 2189;  // <_then> ::= 
		final int PROD__THEN_THEN                                                            = 2190;  // <_then> ::= THEN
		final int PROD__TIMES                                                                = 2191;  // <_times> ::= 
		final int PROD__TIMES_TIMES                                                          = 2192;  // <_times> ::= TIMES
		final int PROD__TO                                                                   = 2193;  // <_to> ::= 
		final int PROD__TO_TO                                                                = 2194;  // <_to> ::= TO
		final int PROD__TO_USING                                                             = 2195;  // <_to_using> ::= 
		final int PROD__TO_USING_TO                                                          = 2196;  // <_to_using> ::= TO
		final int PROD__TO_USING_USING                                                       = 2197;  // <_to_using> ::= USING
		final int PROD__WHEN                                                                 = 2198;  // <_when> ::= 
		final int PROD__WHEN_WHEN                                                            = 2199;  // <_when> ::= WHEN
		final int PROD__WHEN_SET_TO                                                          = 2200;  // <_when_set_to> ::= 
		final int PROD__WHEN_SET_TO_WHEN_SET_TO                                              = 2201;  // <_when_set_to> ::= WHEN SET TO
		final int PROD__WITH                                                                 = 2202;  // <_with> ::= 
		final int PROD__WITH_WITH                                                            = 2203;  // <_with> ::= WITH
		final int PROD_COLL_SEQUENCE_COLLATING_SEQUENCE                                      = 2204;  // <coll_sequence> ::= COLLATING SEQUENCE
		final int PROD_COLL_SEQUENCE_SEQUENCE                                                = 2205;  // <coll_sequence> ::= SEQUENCE
		final int PROD_COLUMN_OR_COL_COLUMN                                                  = 2206;  // <column_or_col> ::= COLUMN
		final int PROD_COLUMN_OR_COL_COL                                                     = 2207;  // <column_or_col> ::= COL
		final int PROD_COLUMNS_OR_COLS_COLUMNS                                               = 2208;  // <columns_or_cols> ::= COLUMNS
		final int PROD_COLUMNS_OR_COLS_COLS                                                  = 2209;  // <columns_or_cols> ::= COLS
		final int PROD_COMP_EQUAL_TOK_EQUAL                                                  = 2210;  // <comp_equal> ::= 'TOK_EQUAL'
		final int PROD_COMP_EQUAL_EQUAL                                                      = 2211;  // <comp_equal> ::= EQUAL
		final int PROD_EXCEPTION_OR_ERROR_EXCEPTION                                          = 2212;  // <exception_or_error> ::= EXCEPTION
		final int PROD_EXCEPTION_OR_ERROR_ERROR                                              = 2213;  // <exception_or_error> ::= ERROR
		final int PROD_IN_OF_IN                                                              = 2214;  // <in_of> ::= IN
		final int PROD_IN_OF_OF                                                              = 2215;  // <in_of> ::= OF
		final int PROD_LABEL_OPTION_STANDARD                                                 = 2216;  // <label_option> ::= STANDARD
		final int PROD_LABEL_OPTION_OMITTED                                                  = 2217;  // <label_option> ::= OMITTED
		final int PROD_LINE_OR_LINES_LINE                                                    = 2218;  // <line_or_lines> ::= LINE
		final int PROD_LINE_OR_LINES_LINES                                                   = 2219;  // <line_or_lines> ::= LINES
		final int PROD_LOCK_RECORDS_RECORD                                                   = 2220;  // <lock_records> ::= RECORD
		final int PROD_LOCK_RECORDS_RECORDS                                                  = 2221;  // <lock_records> ::= RECORDS
		final int PROD_OBJECT_CHAR_OR_WORD_CHARACTERS                                        = 2222;  // <object_char_or_word> ::= CHARACTERS
		final int PROD_OBJECT_CHAR_OR_WORD_WORDS                                             = 2223;  // <object_char_or_word> ::= WORDS
		final int PROD_RECORDS_RECORD                                                        = 2224;  // <records> ::= RECORD <_is>
		final int PROD_RECORDS_RECORDS                                                       = 2225;  // <records> ::= RECORDS <_are>
		final int PROD_REEL_OR_UNIT_REEL                                                     = 2226;  // <reel_or_unit> ::= REEL
		final int PROD_REEL_OR_UNIT_UNIT                                                     = 2227;  // <reel_or_unit> ::= UNIT
		final int PROD_SCROLL_LINE_OR_LINES_LINE                                             = 2228;  // <scroll_line_or_lines> ::= LINE
		final int PROD_SCROLL_LINE_OR_LINES_LINES                                            = 2229;  // <scroll_line_or_lines> ::= LINES
		final int PROD_SIZE_OR_LENGTH_SIZE                                                   = 2230;  // <size_or_length> ::= SIZE
		final int PROD_SIZE_OR_LENGTH_LENGTH                                                 = 2231;  // <size_or_length> ::= LENGTH
		final int PROD_WITH_DUPS_WITH_DUPLICATES                                             = 2232;  // <with_dups> ::= WITH DUPLICATES
		final int PROD_WITH_DUPS_DUPLICATES                                                  = 2233;  // <with_dups> ::= DUPLICATES
		final int PROD_PROG_COLL_SEQUENCE_PROGRAM_COLLATING_SEQUENCE                         = 2234;  // <prog_coll_sequence> ::= PROGRAM COLLATING SEQUENCE
		final int PROD_PROG_COLL_SEQUENCE_COLLATING_SEQUENCE                                 = 2235;  // <prog_coll_sequence> ::= COLLATING SEQUENCE
		final int PROD_PROG_COLL_SEQUENCE_SEQUENCE                                           = 2236;  // <prog_coll_sequence> ::= SEQUENCE
		final int PROD_DETAIL_KEYWORD_DETAIL                                                 = 2237;  // <detail_keyword> ::= DETAIL
		final int PROD_DETAIL_KEYWORD_DE                                                     = 2238;  // <detail_keyword> ::= DE
		final int PROD_CH_KEYWORD_CONTROL_HEADING                                            = 2239;  // <ch_keyword> ::= CONTROL HEADING
		final int PROD_CH_KEYWORD_CH                                                         = 2240;  // <ch_keyword> ::= CH
		final int PROD_CF_KEYWORD_CONTROL_FOOTING                                            = 2241;  // <cf_keyword> ::= CONTROL FOOTING
		final int PROD_CF_KEYWORD_CF                                                         = 2242;  // <cf_keyword> ::= CF
		final int PROD_PH_KEYWORD_PAGE_HEADING                                               = 2243;  // <ph_keyword> ::= PAGE HEADING
		final int PROD_PH_KEYWORD_PH                                                         = 2244;  // <ph_keyword> ::= PH
		final int PROD_PF_KEYWORD_PAGE_FOOTING                                               = 2245;  // <pf_keyword> ::= PAGE FOOTING
		final int PROD_PF_KEYWORD_PF                                                         = 2246;  // <pf_keyword> ::= PF
		final int PROD_RH_KEYWORD_REPORT_HEADING                                             = 2247;  // <rh_keyword> ::= REPORT HEADING
		final int PROD_RH_KEYWORD_RH                                                         = 2248;  // <rh_keyword> ::= RH
		final int PROD_RF_KEYWORD_REPORT_FOOTING                                             = 2249;  // <rf_keyword> ::= REPORT FOOTING
		final int PROD_RF_KEYWORD_RF                                                         = 2250;  // <rf_keyword> ::= RF
		final int PROD_CONTROL_KEYWORD_CONTROL                                               = 2251;  // <control_keyword> ::= CONTROL <_is>
		final int PROD_CONTROL_KEYWORD_CONTROLS                                              = 2252;  // <control_keyword> ::= CONTROLS <_are>
	};

	//----------------------- Local helper functions -------------------------

	/**
	 * replace a single character at offset {@code pos} in {@code oldString} with another char 
	 * @param oldString
	 * @param newChar
	 * @param pos
	 * @return replaced String
	 */
	private String replaceCharPosInString(String oldString, char newChar, int pos) {
		int strLen = 0;
		if (oldString != null) {
			strLen = oldString.length();
		}
		if (oldString == null || strLen < 2) {
			return newChar + ""; // convert char to String
		} else if (pos >= 0 && pos <= strLen-1) {
			return oldString.substring(0, pos) + newChar + oldString.substring(pos + 1);			
		}
		return oldString;
	}

	//----------------------------- Preprocessor -----------------------------

	// line length for source if source format is VARIABLE
	private static final int TEXTCOLUMN_VARIABLE = 500;
	
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
	
	private int settingCodeLength = settingColumnText - settingColumnIndicator - 1;

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
	protected File prepareTextfile(String _textToParse, String _encoding)
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
		 */
		
		File interm = null;
		try
		{
			File file = new File(_textToParse);
			DataInputStream in = new DataInputStream(new FileInputStream(file));
			// START KGU#193 2016-05-04
			BufferedReader br = new BufferedReader(new InputStreamReader(in, _encoding));
			// END KGU#193 2016-05-04
			String strLine;
			StringBuilder srcCode = new StringBuilder();

			srcCodeDebugLines = false; // FIXME: get from setting/parsing
			decimalComma = false; // FIXME: get from setting/parsing
			settingFixedForm = true; // FIXME: get from setting/parsing
			settingFixedColumnIndicator = 7;  // FIXME: get from setting/parsing
			settingFixedColumnText = 73;  // FIXME: get from setting/parsing
			
			if (settingFixedForm) {
				setColumns(settingFixedColumnIndicator, settingFixedColumnText);
			}
			
			int srcCodeLastPos = 0;
			int srcLastCodeLenght = settingCodeLength;
			
			// 
			
			//Read File Line By Line
			// Preprocessor directives are not tolerated by the grammar, so drop them or try to
			// do the [COPY] REPLACE replacements (at least roughly...)
			while ((strLine = br.readLine()) != null)   
			{
				if (settingFixedForm) { // fixed-form reference-format
					
					if (strLine.length() < settingColumnIndicator) {
						srcCode.append (strLine + "\n");
						srcCodeLastPos += strLine.length() + 1;	
						continue; // read next line
					}
					
					String srcLineCode = strLine.substring(settingColumnIndicator);
					if (srcLineCode.length() > settingCodeLength) {
						// FIXME: Better check if the string contains *> already and is not part of a literal,
						//        if yes don't cut the string; if not: insert "*> TEXT AREA" in   
						srcLineCode = srcLineCode.substring(0, settingCodeLength);
					}
					
//					if (srcLineCode.trim().length() == 0) {
//						srcCode.append ("\n");
//						srcCodeLastPos += 1;
//						continue; // read next line
//					}

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
							srcCode.insert(srcCodeLastPos - 1, srcLineCode);
							srcCodeLastPos += srcLineCode.length();
							continue; // read next line
						}
						// literal continuation
						// FIXME hack: string concatenation backwards (will only
						// work on "clean" sources)
						// and if the same literal symbol was used
						if (srcCodeLastPos != 0) {
							while (srcLastCodeLenght < settingCodeLength) {
								srcCode.insert(srcCodeLastPos - 1, " ");
								srcCodeLastPos++;
								srcLastCodeLenght++;
							}
							srcCode.insert(srcCodeLastPos - 1, firstNonSpaceInLine + " &");
							srcCodeLastPos += 3;
						}
						strLine = srcLineCode;
						srcLastCodeLenght = strLine.length();
						srcCodeLastPos += srcLastCodeLenght;
					} else {
						String resultLine = checkForDirectives(srcLineCode);
						if (resultLine != null) {
							strLine = resultLine;
						} else {
//							int i = 0;
//							int im = srcLineCode.length();
//							char lastLit = ' ';
//							char lastChar = ' ';
//							while (i <= im) {
//								char currChar = srcLineCode.charAt(i);
//								// not within a literal
//								if (lastLit == ' ') {
//									// check if new literal starts
//									if (currChar == '"' || currChar == '\'') {
//										lastLit = currChar; 
//									}
//									// check if we want to replace last separator comma/semicolon
//									if (lastChar == ';' || (lastChar == ',' && !decimalComma)) {
//										srcLineCode = replaceCharPosInString (srcLineCode, ' ', i - 1);
//									} else if (lastChar == ',') { // decimal comma is not active
//										if (!decimalComma) {
//											srcLineCode = replaceCharPosInString (srcLineCode, ' ', i - 1);
//										}
//									}
//								// within a literal --> only check if literal ends
//								} else if (lastLit == currChar && currChar != lastChar) {								
//									lastLit = ' ';
//								}
//								lastChar = currChar;
							// }
							if (srcLineCode.trim().length() != 0) {
								strLine = srcLineCode;
								srcLastCodeLenght = strLine.length();
							} else {
								strLine = "";
								srcLastCodeLenght = 0;
							}
							srcCodeLastPos = srcCode.length() + srcLastCodeLenght;
							srcCodeLastPos += 1; // counting newline
						}
					}

				} else { // free-form reference-format

					// skip empty lines
					if (strLine.trim().length() == 0) {
						srcCode.append ("\n");
						//srcCodeLastPos += 1;   // really needed for free-form reference-format?
						continue; // read next line
					}

					String resultLine = checkForDirectives(strLine);
					if (resultLine != null) {
						strLine = resultLine;
					}

				}
				//srcCodeLastPos += 1;   // really needed for free-form reference-format?
				srcCode.append (strLine + "\n");
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
			System.err.println("COBOLParser.prepareTextfile() -> " + e.getMessage());
			e.printStackTrace();	
		}
		return interm;
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
		} else if (firstToken.equals("REPLACE")) {
			// TODO store the replacements and do them
			// removed because must be set as comment until next period (multiple lines):
			// resultLine = "*> REPLACE: " + codeLine;
			// TODO log error - no support for REPLACE statement and present it to the user
			// as a warning after the parsing is finished
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

	//---------------------- Build methods for structograms ---------------------------

	private HashMap<Root, HashMap<String, String>> paramTypeMap = new HashMap<Root, HashMap<String, String>>();

	/**
	 * Associates the name of the result variable to the respective function Root
	 */
	private HashMap<Root, String> returnMap = new HashMap<Root, String>();
	
//	/* (non-Javadoc)
//	 * @see CodeParser#initializeBuildNSD()
//	 */
//	@Override
//	protected void initializeBuildNSD()
//	{
//		// TODO insert initializations for the build phase if necessary ...
//	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.parsers.CodeParser#buildNSD_R(com.creativewidgetworks.goldparser.engine.Reduction, lu.fisch.structorizer.elements.Subqueue)
	 */
	@Override
	protected void buildNSD_R(Reduction _reduction, Subqueue _parentNode)
	{
		//String content = new String();

		if (_reduction.size() > 0)
		{
			String rule = _reduction.getParent().toString();
			System.out.println(rule);
			String ruleHead = _reduction.getParent().getHead().toString();
			int ruleId = _reduction.getParent().getTableIndex();
			log("buildNSD_R(" + rule + ", " + _parentNode.parent + ")...\n", true);
			System.out.println("buildNSD_R(" + rule + ", " + _parentNode.parent + ")...");

			if (
					ruleId == RuleConstants.PROD_PROGRAM_DEFINITION
					||
					ruleId == RuleConstants.PROD_FUNCTION_DEFINITION
					)
			{
				System.out.println("PROD_PROGRAM_DEFINITION or PROD_FUNCTION_DEFINITION");
				Root prevRoot = root;	// Cache the original root
				root = new Root();	// Prepare a new root for the (sub)routine
				subRoots.add(root);
				Reduction secRed = _reduction.get(1).asReduction();	// program or function id paragraph
				String content = this.getContent_R(secRed.get(2).asReduction(), "");
				if (secRed.getParent().getTableIndex() == RuleConstants.PROD_FUNCTION_ID_PARAGRAPH_FUNCTION_ID_TOK_DOT_TOK_DOT) {
					this.root.isProgram = false;
				}
				// Arguments and return value will be fetched from a different rule
				root.setText(content);

				if (_reduction.get(4).getType() == SymbolType.NON_TERMINAL)
				{
					buildNSD_R(_reduction.get(4).asReduction(), root.children);
				}
				// Restore the original root
				root = prevRoot;
			}
//			else if (
//					ruleId == RuleConstants.PROD_PROGRAM_ID_PARAGRAPH_PROGRAM_ID_TOK_DOT_TOK_DOT
//					||
//					ruleId == RuleConstants.PROD_FUNCTION_ID_PARAGRAPH_FUNCTION_ID_TOK_DOT_TOK_DOT
//					)
//			{
//			}
			else if (ruleId == RuleConstants.PROD__PROCEDURE_USING_CHAINING_USING)
			{
				System.out.println("PROD__PROCEDURE_USING_CHAINING_USING");
				//String arguments = this.getContent_R(_reduction.get(1).asReduction(), "").trim();
				//root.setText(root.getText().getLongString() + "(" + arguments + ")");
				StringList arguments = this.getParameterList(_reduction.get(1).asReduction(), "<procedure_param_list>", RuleConstants.PROD_PROCEDURE_PARAM, 3);
				HashMap<String, String> paramTypes = this.paramTypeMap.get(root);
				if (paramTypes != null && paramTypes.size() > 0) {
					for (int i = 0; i < arguments.count(); i++) {
						String type = paramTypes.get(arguments.get(i));
						if (type != null && !type.isEmpty() && !type.equals("???")) {
							arguments.set(i, type + " " + arguments.get(i)) ;
						}
					}
				}
				if (arguments.count() > 0) {
					root.setText(root.getText().getLongString() + "(" + arguments.concatenate(", ") + ")");
					root.isProgram = false;
				}
			}
			else if (ruleId == RuleConstants.PROD__PROCEDURE_RETURNING_RETURNING)
			{
				String resultVar = this.getContent_R(_reduction.get(1).asReduction(), "");
				this.returnMap .put(root, resultVar);
				StringList rootText = root.getText();
				if (this.paramTypeMap.containsKey(root)) {
					HashMap<String, String> paramTypes = this.paramTypeMap.get(root);
					if (paramTypes.containsKey(resultVar)
						&& rootText.count() >= 1
						&& rootText.getLongString().trim().endsWith(")")) {
						rootText.set(rootText.count()-1, rootText.get(rootText.count()-1) + ": " + paramTypes.get(resultVar));
					}
				}
			}
			else if (ruleId == RuleConstants.PROD_IF_STATEMENT_IF)
			{
				System.out.println("PROD_IF_STATEMENT_IF");
				String content = this.transformCondition(_reduction.get(1).asReduction(), "");
				System.out.println("\tCondition: " + content);
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
					System.out.println("\tTHEN branch...");
					this.buildNSD_R(trueRed, alt.qTrue);
				}
				if (falseRed != null) {
					System.out.println("\tELSE branch...");
					this.buildNSD_R(falseRed, alt.qFalse);
				}
				if (alt.qTrue.getSize() == 0 && alt.qFalse.getSize() > 0) {
					alt.qTrue = alt.qFalse;
					alt.qFalse = new Subqueue();
					alt.qFalse.parent = alt;
					alt.setText(negateCondition(content));
				}
				_parentNode.addElement(alt);
				System.out.println("\tEND_IF");
			}
			else if (ruleId == RuleConstants.PROD_EVALUATE_STATEMENT_EVALUATE)
			{
				this.importEvaluate(_reduction, _parentNode);
			}
			else if (ruleId == RuleConstants.PROD_PERFORM_STATEMENT_PERFORM)
			{
				this.importPerform(_reduction, _parentNode);
			}
			else if (ruleId == RuleConstants.PROD_MOVE_STATEMENT_MOVE)
			{
				System.out.println("PROD_MOVE_STATEMENT_MOVE");
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
					// We must do something to avoid copy() calls on the left-hand side
					StringList assignments = new StringList();
					for (int i = 0; i < targets.count(); i++) {
						String target = targets.get(i).trim();
						if (target.matches("^copy\\((.*),(.*),(.*)\\)$")) {
							assignments.add(target.replaceFirst("^copy\\((.*),(.*),(.*)\\)$", "delete($1, $2, $3)"));
							assignments.add(target.replaceFirst("^copy\\((.*),(.*),(.*)\\)$", Matcher.quoteReplacement("insert(" + expr) + ", $1, $2)"));
						}
						else {
							assignments.add(target + " <- " + expr);
							if (i == 0) {
								expr = target;
							}
						}
					}
					_parentNode.addElement(new Instruction(assignments));
				}
			}
			else if (ruleId == RuleConstants.PROD_SET_STATEMENT_SET)
			{
				System.out.println("PROD_SET_STATEMENT_SET");
				Reduction secRed = _reduction.get(1).asReduction();	// <set_body>
				String content = "";
				if (secRed.getParent().getTableIndex() != RuleConstants.PROD_SET_TO_TO) {
					content = this.getContent_R(_reduction, "");
					Instruction instr = new Instruction(content);
					instr.setComment("This statement cannot be converted into a sensible diagram element!");
					instr.setColor(Color.RED);
					instr.disabled = true;
					_parentNode.addElement(instr);
				}
				else {
					String expr = this.getContent_R(secRed.get(2).asReduction(), "");
					StringList targets = this.getExpressionList(secRed.get(0).asReduction(), "<target_x_list>", RuleConstants.PROD_TARGET_X_COMMA_DELIM);
					if (targets.count() > 0)
					{
						StringList assignments = new StringList();
						for (int i = 0; i < targets.count(); i++) {
							String target = targets.get(i).trim();
							if (target.matches("^copy\\((.*),(.*),(.*)\\)$")) {
								assignments.add(target.replaceFirst("^copy\\((.*),(.*),(.*)\\)$", "delete($1, $2, $3)"));
								assignments.add(target.replaceFirst("^copy\\((.*),(.*),(.*)\\)$", Matcher.quoteReplacement("insert(" + expr) + ", $1, $2)"));
							}
							else {
								assignments.add(target + " <- " + expr);
							}
						}
						_parentNode.addElement(new Instruction(assignments));
					}
				}
			}
			else if (ruleId == RuleConstants.PROD_COMPUTE_STATEMENT_COMPUTE)
			{
				System.out.println("PROD_COMPUTE_STATEMENT_COMPUTE");
				Reduction secRed = _reduction.get(1).asReduction();
				StringList targets = this.getExpressionList(secRed.get(0).asReduction(), "<arithmetic_x_list>", RuleConstants.PROD_TARGET_X_COMMA_DELIM);
				String expr = this.getContent_R(secRed.get(2).asReduction(), "");
				if (targets.count() > 0) {
					StringList content = StringList.getNew(targets.get(0) + " <- " + expr);
					for (int i = 1; i < targets.count(); i++) {
						// FIXME Caution: target.get(0) might be a "reference modification"!
						content.add(targets.get(i) + " <- " + targets.get(0));
					}
					_parentNode.addElement(new Instruction(content));
				}				
			}
			else if (ruleId == RuleConstants.PROD_ADD_STATEMENT_ADD)
			{
				System.out.println("PROD_ADD_STATEMENT_ADD");
				this .importAdd(_reduction, _parentNode);
			}
			else if (ruleId == RuleConstants.PROD_SUBTRACT_STATEMENT_SUBTRACT)
			{
				System.out.println("PROD_SUBTRACT_STATEMENT_SUBTRACT");
				this.importSubtract(_reduction, _parentNode);
			}
			else if (ruleId == RuleConstants.PROD_MULTIPLY_STATEMENT_MULTIPLY)
			{
				System.out.println("PROD_MULTIPLY_STATEMENT_MULTIPLY");
				this.importMultiply(_reduction, _parentNode);
			}
			else if (ruleId == RuleConstants.PROD_DIVIDE_STATEMENT_DIVIDE)
			{
				System.out.println("PROD_DIVIDE_STATEMENT_DIVIDE");
				this.importDivide(_reduction, _parentNode);
			}
			else if (ruleId == RuleConstants.PROD_ACCEPT_STATEMENT_ACCEPT)
			{
				// Input instruction
				System.out.println("PROD_ACCEPT_STATEMENT_ACCEPT");
				Reduction secRed = _reduction.get(1).asReduction();		// <accept_body>
				int secRuleId = secRed.getParent().getTableIndex();
				if (secRuleId == RuleConstants.PROD_ACCEPT_BODY || secRuleId == RuleConstants.PROD_ACCEPT_BODY_FROM3) {
					// For these types we can offer a conversion
					String content = getKeywordOrDefault("input", "input");
					String varName = this.getContent_R(secRed.get(0).asReduction(), "");
					if (varName.equalsIgnoreCase("OMITTED")) {
						varName = "";
					}
					content += " " + varName;
					_parentNode.addElement(new Instruction(content.trim()));
				}
				else {
					Instruction dummy = new Instruction(this.getContent_R(_reduction, ""));
					dummy.setComment(StringList.explode("An import for this kind of ACCEPT instruction is not implemented:\n"
							+ this.getOriginalText(_reduction, ""), "\n"));
					dummy.setColor(Color.RED);
					dummy.disabled = true;
					_parentNode.addElement(dummy);
				}
			}
			else if (ruleId == RuleConstants.PROD_DISPLAY_STATEMENT_DISPLAY )
			{
				// Output instruction
				System.out.println("PROD_DISPLAY_STATEMENT_DISPLAY");
				// TODO: Identify whether fileAPI s to be used.
				Reduction secRed = _reduction.get(1).asReduction();	// display body
				// TODO: Define a specific routine to extract the exressions
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
				_parentNode.addElement(instr);
			}
			else if (ruleId == RuleConstants.PROD_OPEN_STATEMENT_OPEN)
			{
				System.out.println("PROD_OPEN_STATEMENT_OPEN");
				// FIXME: Find a sensible conversion!
				String content = this.getContent_R(_reduction, "");
				Instruction instr = new Instruction(content);
				instr.setColor(Color.RED);
				instr.setComment("TODO: there is still no automatic conversion for this statement");
				_parentNode.addElement(instr);
			}
			else if (ruleId == RuleConstants.PROD_READ_STATEMENT_READ)
			{
				System.out.println("PROD_READ_STATEMENT_READ");
				// FIXME: Find a sensible conversion!
				String content = this.getContent_R(_reduction, "");
				Instruction instr = new Instruction(content);
				instr.setColor(Color.RED);
				instr.setComment("TODO: there is still no automatic conversion for this statement");
				_parentNode.addElement(instr);
			else if (ruleId == RuleConstants.PROD_WRITE_STATEMENT_WRITE)
			{
				System.out.println("PROD_WRITE_STATEMENT_WRITE");
				// FIXME: Find a sensible conversion!
				String content = this.getContent_R(_reduction, "");
				Instruction instr = new Instruction(content);
				instr.setColor(Color.RED);
				instr.setComment("TODO: there is still no automatic conversion for this statement");
				_parentNode.addElement(instr);
			}
			else if (ruleId == RuleConstants.PROD_REWRITE_STATEMENT_REWRITE)
			{
				System.out.println("PROD_REWRITE_STATEMENT_REWRITE");
				// FIXME: Find a sensible conversion!
				String content = this.getContent_R(_reduction, "");
				Instruction instr = new Instruction(content);
				instr.setColor(Color.RED);
				instr.setComment("TODO: there is still no automatic conversion for this statement");
				_parentNode.addElement(instr);
			}
			else if (ruleId == RuleConstants.PROD_DELETE_STATEMENT_DELETE)
			{
				System.out.println("PROD_DELETE_STATEMENT_DELETE");
				// FIXME: Find a sensible conversion!
				String content = this.getContent_R(_reduction, "");
				Instruction instr = new Instruction(content);
				instr.setColor(Color.RED);
				instr.setComment("TODO: there is still no automatic conversion for this statement");
				_parentNode.addElement(instr);
			}
			else if (ruleId == RuleConstants.PROD_CLOSE_STATEMENT_CLOSE)
			{
				System.out.println("PROD_CLOSE_STATEMENT_CLOSE");
				// FIXME: Find a sensible conversion!
				String content = this.getContent_R(_reduction, "");
				Instruction instr = new Instruction(content);
				instr.setColor(Color.RED);
				instr.setComment("TODO: there is still no automatic conversion for this statement");
				_parentNode.addElement(instr);
			}
			else if (ruleId == RuleConstants.PROD_CALL_STATEMENT_CALL)
			{
				Reduction secRed = _reduction.get(1).asReduction();	// <call_body>
				String name = this.getContent_R(secRed.get(1).asReduction(), "").trim();
				// FIXME: This can be a lot of things, consider tokenizing it ...
				String[] nameTokens = name.split("\\s+");
				for (int i = 0; i < nameTokens.length; i++) {
					// FIXME cut all from an "as" keyword on...
				}
				// Maybe the actual name is given as string literal rather than an identifier
				if (name.matches("\\\".*?\\\"")) {
					name = name.substring(1, name.length()-1).replace("-", "_");
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
				ele.setComment(this.getOriginalText(_reduction, ""));
				_parentNode.addElement(ele);
			}
			else if (ruleId == RuleConstants.PROD_EXIT_STATEMENT_EXIT)
			{
				System.out.println("PROD_EXIT_STATEMENT_EXIT");
				this.importExit(_reduction, _parentNode);
			}
			else if (ruleId == RuleConstants.PROD_GOBACK_STATEMENT_GOBACK )
			{
				System.out.println("PROD_GOBACK_STATEMENT_GOBACK");
				Reduction secRed = _reduction.get(1).asReduction();
				String content = CodeParser.getKeywordOrDefault("preReturn", "return");
				if (secRed.getParent().getTableIndex() == RuleConstants.PROD_EXIT_PROGRAM_RETURNING2) {
					content = this.getContent_R(secRed.get(1).asReduction(), content + " ");
				}
				_parentNode.addElement(new Jump(content.trim()));
			}
			else if (
					ruleId == RuleConstants.PROD_STOP_STATEMENT_STOP
					||
					ruleId == RuleConstants.PROD_STOP_STATEMENT_STOP_RUN
					)
			{
				System.out.println("PROD_STOP_STATEMENT_STOP[_RUN]");
				int contentIx = (ruleId == RuleConstants.PROD_STOP_STATEMENT_STOP) ? 1 : 2;
				Reduction secRed = _reduction.get(contentIx).asReduction();
				String content = CodeParser.getKeywordOrDefault("preExit", "exit");
				content = this.getContent_R(secRed, content + " ");
				_parentNode.addElement(new Jump(content));
			}
			else if (ruleId == RuleConstants.PROD_GOTO_STATEMENT_GO)
			{
				System.out.println("PROD_GOTO_STATEMENT_GO");
				String content = this.getContent_R(_reduction.get(1).asReduction(), "").trim();
				if (content.toUpperCase().startsWith("TO ")) {
					content = content.substring(3);
				}
				Jump jmp = new Jump("goto " + content);
				jmp.setColor(Color.RED);
				jmp.setComment("GO TO statements are not supported in structured programming!");
				_parentNode.addElement(jmp);
			}
			else if (ruleId == RuleConstants.PROD__WORKING_STORAGE_SECTION_WORKING_STORAGE_SECTION_TOK_DOT)
			{
				this.processDataDescriptions(_reduction.get(3).asReduction(), _parentNode, null);
			}
			else if (ruleId == RuleConstants.PROD__LINKAGE_SECTION_LINKAGE_SECTION_TOK_DOT)
			{
				if (!this.paramTypeMap.containsKey(root)) {
					this.paramTypeMap.put(root, new HashMap<String, String>());
				}
				this.processDataDescriptions(_reduction.get(3).asReduction(), null, this.paramTypeMap.get(root));
			}
			else
			{
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
	}	

	/**
	 * Builds an approptiate Instruction element from the ADD statement represented by {@code _reduction}.
	 * @param _reduction - the top Reduction of the parsed ADD statement
	 * @param _parentNode - the Subqueue to append the built elements to
	 */
	private void importAdd(Reduction _reduction, Subqueue _parentNode) {
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
			_parentNode.addElement(new Instruction(content));
		}
		else {
			Instruction defective = new Instruction(this.getContent_R(_reduction, "", " "));
			defective.setColor(Color.RED);
			defective.disabled = true;
			defective.setComment("COBOL import still not implemented");
			_parentNode.addElement(defective);
		}
	}

	/**
	 * Builds an approptiate Instruction element from the SUBTRACT statement represented by {@code _reduction}.
	 * @param _reduction - the top Reduction of the parsed SUBTRACT statement
	 * @param _parentNode - the Subqueue to append the built elements to
	 */
	private final void importSubtract(Reduction _reduction, Subqueue _parentNode) {
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
			_parentNode.addElement(new Instruction(content));
		}
		else {
			Instruction defective = new Instruction(this.getContent_R(_reduction, "", " "));
			defective.setColor(Color.RED);
			defective.disabled = true;
			defective.setComment("COBOL import still not implemented");
			_parentNode.addElement(defective);
		}
	}

	/**
	 * Builds an approptiate Instruction element from the MULTIPLY statement represented by {@code _reduction}.
	 * @param _reduction - the top Reduction of the parsed MULTIPLY statement
	 * @param _parentNode - the Subqueue to append the built elements to
	 */
	private final void importMultiply(Reduction _reduction, Subqueue _parentNode) {
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
			_parentNode.addElement(new Instruction(content));
		}
		else {
			Instruction defective = new Instruction(this.getContent_R(_reduction, "", " "));
			defective.setColor(Color.RED);
			defective.disabled = true;
			defective.setComment("COBOL import still not implemented");
			_parentNode.addElement(defective);
		}
	}

	/**
	 * Builds an approptiate Instruction element from the DIVIDE statement represented by {@code _reduction}.
	 * @param _reduction - the top Reduction of the parsed DIVIDE statement
	 * @param _parentNode - the Subqueue to append the built elements to
	 */
	private final void importDivide(Reduction _reduction, Subqueue _parentNode) {
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
			_parentNode.addElement(new Instruction(content));
		}
		else {
			Instruction defective = new Instruction(this.getContent_R(_reduction, "", " "));
			defective.setColor(Color.RED);
			defective.disabled = true;
			defective.setComment("COBOL import still not implemented");
			_parentNode.addElement(defective);
		}
	}

	/**
	 * Builds an approptiate Jump element from the EXIT statement represented by {@code _reduction}.
	 * @param _reduction - the top Reduction of the parsed EXIT statement
	 * @param _parentNode - the Subqueue to append the built elements to
	 */
	private final void importExit(Reduction _reduction, Subqueue _parentNode) {
		String content = "";
		String comment = "";
		Color color = null;
		Reduction secRed = _reduction.get(1).asReduction();
		int secRuleId = secRed.getParent().getTableIndex();
		switch (secRuleId) {
		case RuleConstants.PROD_EXIT_BODY:	// (empty)
			content = "(exit from paragraph)";
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
		case RuleConstants.PROD_EXIT_BODY_SECTION:	// <exit_body> ::= SECTION
		case RuleConstants.PROD_EXIT_BODY_PARAGRAPH:// <exit_body> ::= PARAGRAPH
			content = this.getContent_R(_reduction, "");
			color = Color.RED;
			comment = "Unsupported kind of JUMP";
			break;
		}
		if (content != null) {
			Jump jmp = new Jump(content.trim());
			if (!comment.isEmpty()) {
				jmp.setComment(comment);
			}
			if (color != null) {
				jmp.setColor(color);
				jmp.disabled = true;
			}
			_parentNode.addElement(jmp);
		}
	}

	/**
	 * Buildsa  loop or Call element from the PERFORM statement represented by {@code _reduction}.
	 * @param _reduction - the top Reduction of the parsed PERFORM statement
	 * @param _parentNode - the Subqueue to append the built elements to
	 */
	private final void importPerform(Reduction _reduction, Subqueue _parentNode) {
		System.out.println("PROD_PERFORM_STATEMENT_PERFORM");
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
		System.out.println("\t" + optRed.getParent().toString());
		switch (optRed.getParent().getTableIndex()) {
		case RuleConstants.PROD_PERFORM_OPTION_TIMES:
			// FOR loop
		{
			// Prepare a generic variable name
			content = this.getContent_R(optRed.get(0).asReduction(), content);
			loop = new For("varStructorizer", "1", content, 1);
			content = ((For)loop).getText().getLongString();
			((For)loop).setText(content.replace("varStructorizer", "var" + loop.hashCode()));
		}
		break;
		case RuleConstants.PROD_PERFORM_OPTION_VARYING:
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
				// FIXME this is just a quick hack
				int step = 0;
				try {
					step = Integer.parseInt(by);
				}
				catch (NumberFormatException ex) {}
				if (step == 0
						|| step > 0 && !cond.matches(BString.breakup(varName) + "\\s* > .*") && !cond.matches(".* < \\s*" + BString.breakup(varName))
						|| step < 0 && !cond.matches(BString.breakup(varName) + "\\s* < .*") && !cond.matches(".* > \\s*" + BString.breakup(varName))) {
					_parentNode.addElement(new Instruction(varName + " <- " + from));
					While wloop = new While(negateCondition(cond));
					if (bodyRuleId == RuleConstants.PROD_PERFORM_BODY2) {
						this.buildNSD_R(bodyRed.get(1).asReduction(), wloop.getBody());
						wloop.getBody().addElement(new Instruction(varName + " <- " + varName + " + (" + by + ")"));
					}
					else {
						Instruction defective = new Instruction(this.getContent_R(_reduction, ""));
						defective.setColor(Color.RED);
						defective.disabled = true;
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
			break;
		case RuleConstants.PROD_PERFORM_OPTION_UNTIL:
			// WHILE or REPEAT loop
			{
				// Get the condition itself
				content = this.getContent_R(optRed.get(2).asReduction(), "");
				// Classify the test position
				Reduction testRed = optRed.get(0).asReduction();
				if (testRed.getParent().getTableIndex() == RuleConstants.PROD_PERFORM_TEST
						|| testRed.get(2).asReduction().getParent().getTableIndex() == RuleConstants.PROD_BEFORE_OR_AFTER_BEFORE) {
					loop = new While(negateCondition(content));
				}
				else {
					loop = new Repeat(content);
				}
			}
			break;
		default:
			// Just a macro (block)	
			{
				content = this.getContent_R(bodyRed.get(0).asReduction(), "").trim().replace(' ', '_') + "()";
				if (Character.isDigit(content.charAt(0))) {
					content = "sub" + content;
				}
				Call dummyCall = new Call(content);
				dummyCall.setColor(Color.RED);
				dummyCall.setComment("Seems to be a call of an internal paragraph/macro, which is still not supported");
				_parentNode.addElement(dummyCall);
			}
		}
		if (loop != null && bodyRuleId == RuleConstants.PROD_PERFORM_BODY2) {
			this.buildNSD_R(bodyRed.get(1).asReduction(), loop.getBody());
		}
		else {
			// FIXME
			System.err.println("We have no idea how to convert this: " + this.getContent_R(_reduction, ""));
		}
		if (loop != null) {
			_parentNode.addElement((Element)loop);
		}
	}

	/**
	 * Builds Case elements or nested Alternatives from the EVALUATE statement
	 * represented by {@code _reduction}.
	 * @param _reduction - the top Reduction of the parsed EVALUATE statement
	 * @param _parentNode - the Subqueue to append the built elements to
	 */
	private final void importEvaluate(Reduction _reduction, Subqueue _parentNode) {
		System.out.println("PROD_EVALUATE_STATEMENT_EVALUATE");
		// Possibly a CASE instruction, may have to be decomposed to an IF chain.
		Reduction secRed = _reduction.get(1).asReduction();	// <evaluate_body>
		Reduction subjlRed = secRed.get(0).asReduction();	// <evaluate_subject_list>
		Reduction condlRed = secRed.get(1).asReduction();		// <evaluate_condition_list>
		log(subjlRed.getParent().toString(), false);
		log(condlRed.getParent().toString(), false);
		int subjlRuleId = subjlRed.getParent().getTableIndex();
		if (subjlRuleId == RuleConstants.PROD_EVALUATE_SUBJECT) {
			// Single discriminator expression - there might be a chance to convert this to a CASE element
			System.out.println("\tEVALUATE: PROD_EVALUATE_SUBJECT_LIST");
			StringList caseText = StringList.getNew(this.getContent_R(subjlRed, ""));
			Case ele = new Case();
			// Now analyse the branches
			Reduction otherRed = null;
			if (condlRed.getParent().getTableIndex() == RuleConstants.PROD_EVALUATE_CONDITION_LIST) {
				otherRed = condlRed.get(1).asReduction();	// <evaluate_other>
				Subqueue sq = new Subqueue();
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
				buildNSD_R(caseRed.get(1).asReduction(), sq);	// <statement_list>
				ele.qs.add(0, sq);
				// Now collect the WHEN clauses and concoct the compound condition
				Reduction whenlRed = caseRed.get(0).asReduction();	// <evaluate_when_list>
				String selectors = "";
				while (whenlRed != null) {
					// FIXME: At this point we cannot handle incomplete expressions sensibly
					// (as soon as we bump into an incomlete comparison expression or the like we
					// would have had to convert the entire CASE element into a nested alternative tree.
					// The trouble is that all kinds of selectors (literals, complete expressions, and
					// incomplete expressions may occur among the listed selectors.
					if (whenlRed.getParent().getTableIndex() == RuleConstants.PROD_EVALUATE_WHEN_LIST_WHEN2) {
						selectors = this.getContent_R(whenlRed.get(2).asReduction(), "") + ", " + selectors;
						whenlRed = whenlRed.get(0).asReduction();
					}
					else {
						selectors = this.getContent_R(whenlRed.get(1).asReduction(), "") + ", " + selectors;
						whenlRed = null;
					}
				}
				selectors = selectors.trim();
				if (selectors.endsWith(",")) {
					selectors = selectors.substring(0, selectors.length()-1);
				}
				caseText.insert(selectors, 1);
				//}
				condlRed = (caseHead.equals("<evaluate_case_list>")) ? condlRed.get(0).asReduction() : null;
			} while (condlRed != null);
			ele.setText(caseText);
			_parentNode.addElement(ele);
		}
		else if (
				subjlRuleId == RuleConstants.PROD_EVALUATE_SUBJECT_TOK_TRUE
				||
				subjlRuleId == RuleConstants.PROD_EVALUATE_SUBJECT_TOK_FALSE
				) {
			// Independent conditions, will be converted to nested alternatives
			boolean negate = subjlRuleId == RuleConstants.PROD_EVALUATE_SUBJECT_TOK_FALSE;
			System.out.println("\tEVALUATE: PROD_EVALUATE_SUBJECT_TOK_TRUE/FALSE");
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
			// TODO: merge this with the previous case!
			// This can only be represented by nested alternatives
			System.out.println("EVALUATE: PROD_EVALUATE_SUBJECT_LIST_ALSO");
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
							String partCond = this.transformCondition(objRed, subjects.get(i));
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
		// a rounded resul then we may reuse the result saved in target 
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

	private void processDataDescriptions(Reduction _reduction, Subqueue _parentNode, HashMap<String, String> _typeInfo)
	{
		int ruleId = _reduction.getParent().getTableIndex();
		if (ruleId == RuleConstants.PROD_DATA_DESCRIPTION4)
		{
			System.out.println("PROD_DATA_DESCIPTION4");
			int level = 0;
			try {
				level = Integer.parseInt(this.getContent_R(_reduction.get(0).asReduction(), ""));
			}
			catch (Exception ex) {}
			// This constant detection is just a fallback unlikely ever to be used - there is a separate rule
			// for SEVENTY-EIGHT constants below, which is supposed to catch respective cases.
			boolean isConst = level == 78;
			// Picture clause: Find variable initializations and declarations
			// FIXME In a first approach we neglect records and delare all as plain variables
			String varName = this.getContent_R(_reduction.get(1).asReduction(), "");
			// as long as we don't use records there is no use in parsing FILLER items
			// and as long as we do so group items that have no VALUE clause and only containing
			// FILLER items which have a VALUE clause are not parsed correctly
			if (!varName.isEmpty() && !varName.equalsIgnoreCase("FILLER")) {				
				Reduction seqRed = _reduction.get(2).asReduction();
				String type = "";
				String value = "";
				String picture = "";
				boolean isGlobal = false;
				// We may not do anything if description is empty
				while (seqRed.getParent().getTableIndex() == RuleConstants.PROD__DATA_DESCRIPTION_CLAUSE_SEQUENCE2) {
					Reduction descrRed = seqRed.get(1).asReduction();
					int descrRuleId = descrRed.getParent().getTableIndex();
					switch (descrRuleId) {
					case RuleConstants.PROD_DATA_DESCRIPTION_CLAUSE4: // <picture_clause> --> type info
						picture = descrRed.get(0).asReduction().get(0).asString();
						break;
					case RuleConstants.PROD_PICTURE_CLAUSE_PICTURE_DEF: // <picture_clause> --> type info
						picture = descrRed.get(0).asString();
						break;
					case RuleConstants.PROD_DATA_DESCRIPTION_CLAUSE5: // <usage_clause> --> type info
						{
							String usage = this.getContent_R(descrRed.get(1).asReduction(), "").toLowerCase();
							final String[] binTypes = new String[]{"float", "double", "short", "int", "long"};
							for (String typeKey: binTypes) {
								if (usage.contains(typeKey)) {
									type = typeKey;
									break;
								}
							}
						}
						break;
					case RuleConstants.PROD_USAGE:                             // <usage> ::= <float_usage>
						type = "float";
						break;							
					case RuleConstants.PROD_USAGE2:                            // <usage> ::= <double_usage>
						type = "double";
						break;
					case RuleConstants.PROD_USAGE_DISPLAY:                     // <usage> ::= DISPLAY
						type = "string";
						break;
					case RuleConstants.PROD_USAGE_POINTER:                     // <usage> ::= POINTER
					case RuleConstants.PROD_USAGE_PROGRAM_POINTER:             // <usage> ::= 'PROGRAM_POINTER'
						// Address types cannot be handled by Structorizer
						type = "pointer";
						break;
					case RuleConstants.PROD_USAGE_BINARY_CHAR:                 // <usage> ::= 'BINARY_CHAR' <_signed>
					case RuleConstants.PROD_USAGE_BINARY_CHAR_UNSIGNED:        // <usage> ::= 'BINARY_CHAR' UNSIGNED
						type = "byte";
						break;
					case RuleConstants.PROD_USAGE_SIGNED_SHORT:                // <usage> ::= 'SIGNED_SHORT'
					case RuleConstants.PROD_USAGE_UNSIGNED_SHORT:              // <usage> ::= 'UNSIGNED_SHORT'
					case RuleConstants.PROD_USAGE_BINARY_SHORT:                // <usage> ::= 'BINARY_SHORT' <_signed>
					case RuleConstants.PROD_USAGE_BINARY_SHORT_UNSIGNED:       // <usage> ::= 'BINARY_SHORT' UNSIGNED
						type = "short";
						break;
					case RuleConstants.PROD_USAGE_INDEX:                       // <usage> ::= INDEX
					case RuleConstants.PROD_USAGE_SIGNED_INT:                  // <usage> ::= 'SIGNED_INT'
					case RuleConstants.PROD_USAGE_UNSIGNED_INT:                // <usage> ::= 'UNSIGNED_INT'
					case RuleConstants.PROD_USAGE_SIGNED_LONG:                 // <usage> ::= 'SIGNED_LONG'
					case RuleConstants.PROD_USAGE_UNSIGNED_LONG:               // <usage> ::= 'UNSIGNED_LONG'
						type = "integer";
						break;
					case RuleConstants.PROD_USAGE_COMP_4:                      // <usage> ::= 'COMP_4'
					case RuleConstants.PROD_USAGE_COMP_5:                      // <usage> ::= 'COMP_5'
					case RuleConstants.PROD_USAGE_COMP_6:                      // <usage> ::= 'COMP_6'
					case RuleConstants.PROD_USAGE_COMP_X:                      // <usage> ::= 'COMP_X'
					case RuleConstants.PROD_USAGE_BINARY_LONG:                 // <usage> ::= 'BINARY_LONG' <_signed>
					case RuleConstants.PROD_USAGE_BINARY_LONG_UNSIGNED:        // <usage> ::= 'BINARY_LONG' UNSIGNED
					case RuleConstants.PROD_USAGE_BINARY_C_LONG:               // <usage> ::= 'BINARY_C_LONG' <_signed>
					case RuleConstants.PROD_USAGE_BINARY_C_LONG_UNSIGNED:      // <usage> ::= 'BINARY_C_LONG' UNSIGNED
					case RuleConstants.PROD_USAGE_BINARY_DOUBLE:               // <usage> ::= 'BINARY_DOUBLE' <_signed>
					case RuleConstants.PROD_USAGE_BINARY_DOUBLE_UNSIGNED:      // <usage> ::= 'BINARY_DOUBLE' UNSIGNED
						type = "long";
						break;
					case RuleConstants.PROD_USAGE_FLOAT_BINARY_32:             // <usage> ::= 'FLOAT_BINARY_32'
					case RuleConstants.PROD_FLOAT_USAGE_COMP_1:
					case RuleConstants.PROD_FLOAT_USAGE_FLOAT_SHORT:
						type = "float";
						break;
					case RuleConstants.PROD_USAGE_COMP_3:                      // <usage> ::= 'COMP_3'
					case RuleConstants.PROD_USAGE_PACKED_DECIMAL:              // <usage> ::= 'PACKED_DECIMAL'
					case RuleConstants.PROD_USAGE_FLOAT_BINARY_64:             // <usage> ::= 'FLOAT_BINARY_64'
					case RuleConstants.PROD_USAGE_FLOAT_BINARY_128:            // <usage> ::= 'FLOAT_BINARY_128'
					case RuleConstants.PROD_USAGE_FLOAT_DECIMAL_16:            // <usage> ::= 'FLOAT_DECIMAL_16'
					case RuleConstants.PROD_USAGE_FLOAT_DECIMAL_34:            // <usage> ::= 'FLOAT_DECIMAL_34'
					case RuleConstants.PROD_DOUBLE_USAGE_FLOAT_LONG:
					case RuleConstants.PROD_DOUBLE_USAGE_COMP_2:
						type = "double";
						break;
					case RuleConstants.PROD_DATA_DESCRIPTION_CLAUSE12: // <value_clause> --> initialisation
						// FIXME: this is a quick and dirty hack
						value = this.getContent_R(descrRed.get(0).asReduction().get(2).asReduction(), "");
						break;
					case RuleConstants.PROD_VALUE_CLAUSE_VALUE: // <value_clause> --> initialisation
						value = this.getContent_R(descrRed.get(2).asReduction(), "");
						break;
					case RuleConstants.PROD_DATA_DESCRIPTION_CLAUSE3: // <global_clause> --> global import
					case RuleConstants.PROD_GLOBAL_CLAUSE_GLOBAL:
						// FIXME Is this to be put to a globals Root or is the current diagram serving as an import Root?
						isGlobal = true;
						break;
					}
					seqRed = seqRed.get(0).asReduction();
				}
				if (!picture.isEmpty()) {
					type = deriveTypeInfoFromPic(picture);
				}
				// if we still have no type we're parsing a group item without usage
				// --> this is always seen as COBOL alphanumeric (internal like a byte[]) -> set to string
				if (type.isEmpty()) {
					type = "string";
				}
				if (!isConst) {
					if (_parentNode != null && this.optionImportVarDecl) {
					// Add the declaration
					Instruction decl = new Instruction("var " + varName + ": " + type);
					decl.setComment(picture);
					decl.setColor(colorDecl);
					_parentNode.addElement(decl);
					}
					if (_typeInfo != null) {
						_typeInfo.put(varName, type);
					}
				}
				if (!value.isEmpty()) {
					// Add the assignment
					//FIXME hexedecimal literals and other literal types must be converted (each literal tpye has its own
					// terminal, otherwise the Executor doesn't know what x'09' is)
					if (value.equalsIgnoreCase("zero") || value.equalsIgnoreCase("zeros")) {	// FIXME should no longer be necessary here
						value = "0";
					}
					//FIXME: Hack for now to force the Executor to use double data type for too big literals
					// we should pass the datatype from deriveTypeInfo instead (and return "long" there)
					if ((type.equals("long") || type.equals("integer"))
							&& value.length() > 9) {
						value = value + "L";
					}
					String content = varName + " <- " + value;
					if (isConst) {
						content = "const " + content;
					}
					Instruction def = new Instruction(content);
					if (isConst) {
						def.setColor(colorConst);
					}
					// FIXME: in case of isGlobal enforce the placement in a global diagram to be imported wherever needed
					_parentNode.addElement(def);
				}
				//TODO stash the variables without a value clause somewhere to add
				// all definitions that are used as variables within the NSD later, otherwise
				// the executor may use the wrong data type
//				else {
//					stashVariable(varName, type);
//				}
			}
		}
		else if (ruleId == RuleConstants.PROD_CONSTANT_ENTRY_CONSTANT) {
			boolean isGlobal = _reduction.get(3).asReduction().getParent().getTableIndex() == RuleConstants.PROD_CONST_GLOBAL_GLOBAL;
			String constName = this.getContent_R(_reduction.get(1).asReduction(), "");
			String value = this.getContent_R(_reduction.get(4).asReduction().get(1).asReduction(), "");
			String type = Element.identifyExprType(null, value, true);
			if (!type.isEmpty() && _typeInfo != null) {
				_typeInfo.put(constName, type);
			}
			// FIXME: in case of isGlobal enforce the palcement in a global diagram to be imported wherever needed
			if (_parentNode != null) {
				Instruction def = new Instruction("const " + constName + " <- " + value);
				def.setColor(colorConst);
				_parentNode.addElement(def);
			}
		}
		else if (ruleId == RuleConstants.PROD_CONSTANT_ENTRY_SEVENTY_EIGHT) {
			boolean isGlobal = _reduction.get(2).asReduction().getParent().getTableIndex() == RuleConstants.PROD_GLOBAL_CLAUSE_GLOBAL;
			String constName = this.getContent_R(_reduction.get(1).asReduction(), "");
			StringList values = this.getExpressionList(_reduction.get(3).asReduction(), "<value_item_list>", RuleConstants.PROD_VALUE_ITEM_COMMA_DELIM);
			String value = null;
			String type = "";
			if (values.count() == 1) {
				value = values.get(0);
				type = Element.identifyExprType(null, value, true);
			}
			else {
				value = "{" + values.concatenate(", ") + "}";
			}
			if (_parentNode != null && value != null) {
				// FIXME: in case of isGlobal enforce the placement in a global diagram to be imported wherever needed
				Instruction def = new Instruction("const " + constName + " <- " + value);
				def.setColor(colorConst);
				_parentNode.addElement(def);
			}
		}
		else {
			for (int i = 0; i < _reduction.size(); i++) {
				if (_reduction.get(i).getType() == SymbolType.NON_TERMINAL) {
					this.processDataDescriptions(_reduction.get(i).asReduction(), _parentNode, _typeInfo);
				}
			}
		}
	}

	private StringList getParameterList(Reduction _paramlRed, String _listHead, int _ruleId, int _nameIx) {
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

	private StringList getExpressionList(Reduction _exprlRed, String _listHead, int _exclRuleId) {
		StringList exprs = new StringList();
		do {
			String exprlHead = _exprlRed.getParent().getHead().toString();
			Reduction exprRed = _exprlRed;	// could be <call_param>
			if (exprlHead.equals(_listHead)) {
				exprRed = _exprlRed.get(_exprlRed.size()-1).asReduction();	// get the <evaluate_case>
			}
			int exprRuleId = exprRed.getParent().getTableIndex();
			if (exprRuleId != _exclRuleId) {
				exprs.add(this.getContent_R(exprRed, ""));
			}
			_exprlRed = (exprlHead.equals(_listHead)) ? _exprlRed.get(0).asReduction() : null;
		} while (_exprlRed != null);
		return exprs.reverse();
	}

	private String transformCondition(Reduction _reduction, String lastSubject) {
		// TODO We must resolve expressions like "expr1 = expr2 or > expr3".
		// Unfortunately the <condition> node is not defined as hierarchical expression
		// tree dominated by operator nodes but as left-recursive "list".
		// We should transform the left-recursive <expr_tokens> list into a linear
		// list of <expr_token> we can analyse from left to right, such that we can
		// identify the first token as comparison operator. (It seems rather simpler
		// to inspect the prefix of the composed string.)
		String thruExpr = "";
		if (_reduction.getParent().getTableIndex() == RuleConstants.PROD_EVALUATE_OBJECT) {
			Reduction thruRed = _reduction.get(1).asReduction();
			if (thruRed.getParent().getTableIndex() == RuleConstants.PROD__EVALUATE_THRU_EXPR_THRU) {
				thruExpr = this.getContent_R(thruRed.get(1).asReduction(), " .. ");
			}
			_reduction = _reduction.get(0).asReduction();
		}
		LinkedList<Token> expr_tokens = new LinkedList<Token>();
		this.lineariseTokenList(expr_tokens, _reduction, "<expr_tokens>");
		String cond = "";
		//String cond = this.getContent_R(_reduction, "").trim();
		// Test if cond starts with a comparison operator. In this case add lastSubject...
		//if (cond.startsWith("<") || cond.startsWith("=") || cond.startsWith(">")) {
		//	if (lastSubject != null) {
		//		cond = (lastSubject + " " + cond).trim();
		//	}
		//}
		int ruleId = -1;
		if (!expr_tokens.isEmpty() && expr_tokens.getFirst().getType() == SymbolType.NON_TERMINAL) {
			ruleId = expr_tokens.getFirst().asReduction().getParent().getTableIndex();
		}
		if (lastSubject == null || lastSubject.isEmpty()) {
			Token tok = expr_tokens.getFirst();
			if (!isComparisonOpRuleId(ruleId)) {
				if (tok.getType() == SymbolType.CONTENT) {
					lastSubject = tok.asString();
					if (tok.getName().equals("COBOLWord")) {
						lastSubject = lastSubject.replace("-", "_");
					}
				}
				else {
					lastSubject = this.getContent_R(tok.asReduction(), "");
				}
			}
			else {
				lastSubject = "";
			}
		}
		boolean afterLogOpr = true;
		for (Token tok: expr_tokens) {
			String tokStr = "";
			if (tok.getType() == SymbolType.NON_TERMINAL) {
				ruleId = tok.asReduction().getParent().getTableIndex();
				tokStr = this.getContent_R(tok.asReduction(), "");
			}
			else {
				tokStr = tok.asString();
				if (tok.getName().equals("COBOLWord")) {
					tokStr = tokStr.replace("-", "_");
				}
			}
			if (!tokStr.trim().isEmpty()) {
				if (afterLogOpr && isComparisonOpRuleId(ruleId)) {
					cond += " " + lastSubject;
				}
				afterLogOpr = (ruleId == RuleConstants.PROD_EXPR_TOKEN_AND || ruleId == RuleConstants.PROD_EXPR_TOKEN_OR);
				if (afterLogOpr) {
					tokStr = tokStr.toLowerCase();
				}
				cond += " " + tokStr;
			}
		}
		cond += thruExpr;
		return cond.trim();	// This is just an insufficient first default approach
	}
	
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

	private void lineariseTokenList(LinkedList<Token> _tokens, Reduction _reduction, String _listRuleHead) {
		if (_reduction.getParent().getHead().toString().equals(_listRuleHead) && _reduction.size() > 1) {
			_tokens.add(0, _reduction.get(_reduction.size()-1));
			lineariseTokenList(_tokens, _reduction.get(0).asReduction(), _listRuleHead);
		}
		else {
			for (int i = 0; i < _reduction.size(); i++) {
				_tokens.add(i, _reduction.get(i));
			}
		}
	}

	private String negateCondition(String condStr) {
		return Element.negateCondition(condStr);
	}
	
	/**
	 * get type from PICTURE clause
	 * sample inputs:  "pic s9(5)v9(2)", "pIcTuRe    zzzz999.99", "pic x(5000)"
	 * @param picture
	 * @return one of the following: double, [long,] integer, string
	 */
	private String deriveTypeInfoFromPic(String picture) {
		String type = "";
		String[] tokens = picture.toUpperCase().split("\\s+", 3);
		// assume the first token to be PIC or PICTURE and a second token to be available
		// (otherwise the grammar has a defect)
		String spec = tokens[1];
		if (spec.startsWith("S")) {
			// Skip the sign placeholder for now...
			spec = spec.substring(1);
		} else 
			while (spec.startsWith("P")) {
				spec = spec.substring(1);
			}
		if (spec.startsWith("9")) {
			if (spec.contains("V")) {
				type = "double";
			}
			else {
				int nDigits = 0;
				int i = 0;
				while (i < spec.length() && spec.charAt(i) == '9') {
					nDigits++;
					i++;
				}
				if (nDigits == 1 && spec.substring(i).matches("\\([0-9]+\\).*")) {
					try {
						nDigits = Integer.parseInt(spec.substring(i).replaceFirst("\\(([0-9]+)\\).*", "$1"));
					}
					catch (Exception ex) {}
				}
				// FIXME: we currently need to manually derive constant values here, may not be needed later
				//        if the preprocessor replace them (not sure yet if we want to do so in all places
				//        as a const declaration in the NSD would likely lead to better results
				//else {
				//}
				if (nDigits >= 9) {
					type = "long";
				}
				else if (nDigits < 5) {
					type = "short";
				}
				else {
					type = "integer";
				}
			}
		}
		else {
			type = "string";
		}
		return type;
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

	@Override
	protected String getContent_R(Reduction _reduction, String _content)
	{
		return getContent_R(_reduction, _content, "");
	}

	protected String getContent_R(Reduction _reduction, String _content, String _separator)
	{
		int ruleId = _reduction.getParent().getTableIndex();
		String ruleHead = _reduction.getParent().getHead().toString();
		if (ruleHead.equals("<function>") && ruleId != RuleConstants.PROD_FUNCTION) {
			String functionName = this.getContent_R(_reduction.get(0).asReduction(),"").toLowerCase().replaceAll("-", "_");
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
				// Id we knew the inner structure of the arguments then we could better decide whether
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
		else if (ruleId == RuleConstants.PROD_IDENTIFIER_1 || ruleId == RuleConstants.PROD_IDENTIFIER_13 ||
				ruleId == RuleConstants.PROD_TARGET_IDENTIFIER_1 || ruleId == RuleConstants.PROD_TARGET_IDENTIFIER_13)
		{
			String qualName = this.getContent_R(_reduction.get(0).asReduction(), "");
			String indexStr = "";
			String lengthStr = "1";
			if (_reduction.size() > 2) {
				indexStr = this.getContent_R(_reduction.get(1).asReduction(), "");
			}
			Reduction refModRed = _reduction.get(_reduction.size() - 1).asReduction();
			String startStr = this.getContent_R(refModRed.get(1).asReduction(), "");
			if (refModRed.size() > 4) {
				lengthStr = this.getContent_R(refModRed.get(3).asReduction(), "");
			}
			_content += " copy(" + qualName + indexStr + ", " + startStr +  ", " + lengthStr + ") ";
		} 
		else {
			for(int i=0; i<_reduction.size(); i++)
			{
				Token token = _reduction.get(i);
				switch (token.getType()) 
				{
				case NON_TERMINAL:
				{
					Reduction subRed = token.asReduction();
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
						_content += " <> 0";
					}
					else if (subRuleId == RuleConstants.PROD_EXPR_TOKEN_IS) {
						_content += " isTypeOf(" + this.getContent_R(subRed.get(1).asReduction(), "") + ") ";
					}
					else if (subRuleId == RuleConstants.PROD_SUBREF_TOK_OPEN_PAREN_TOK_CLOSE_PAREN) {
						_content += "[" + this.getContent_R(subRed.get(1).asReduction(), "") + "] ";	// FIXME: spaces!?
					}
					else {
						String sepa = "";
						String toAdd = getContent_R(token.asReduction(), "", _separator);
						if (i > 0 && !_separator.isEmpty()) {
							sepa = _separator;
						}
						else if (_content.matches(".*\\w") && !(toAdd.startsWith("(") || toAdd.startsWith(" "))) {
							sepa = " ";
						}
						_content += sepa + toAdd;
					}
				}
				break;
				case CONTENT:
				{
					String toAdd = token.asString();
					String name = token.getName();
					if (name.equals("COBOLWord")) {
						toAdd = toAdd.replace("-", "_");
					}
					else if (name.equals("HexLiteral")) {
						String hexText = toAdd.replaceAll("[Xx][\"']([0-9A-Fa-f]+)[\"']", "$1");
						toAdd = " \"";
						for (int j = 0; j < hexText.length(); j += 2) {
							String code = hexText.substring(j, j+2);
							int val = Integer.parseInt(code, 16);
							toAdd += "\\" + Integer.toOctalString(val);
						}
						toAdd += "\" ";
					}
					else if (name.equals("TOK_PLUS") || name.equals("TOK_MINUS")) {
						toAdd = " " + toAdd + " ";
					}
					else if (toAdd.equalsIgnoreCase("zero") || toAdd.equalsIgnoreCase("zeroes")) {
						toAdd = "0";
					}
					else if (toAdd.equalsIgnoreCase("space") || toAdd.equalsIgnoreCase("spaces")) {
						toAdd = "\' \'";
					}
					// Keywords FUNCTION and IS are to be suppressed
					if (!name.equalsIgnoreCase("FUNCTION") && !name.equalsIgnoreCase("IS")) {
						String sepa = "";
						if (i > 0 && !(_content + _separator).endsWith(" ") && !toAdd.startsWith(" ")) {
							sepa = " ";
						}
						_content += (i == 0 ? "" : _separator + sepa) + toAdd;
					}
				}
				break;
				default:
					break;
				}
			}
		}
		return _content;
	}

	//------------------------- Postprocessor ---------------------------

	// TODO Use this subclassable hook if some postprocessing for the generated roots is necessary
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.parsers.CodeParser#subclassUpdateRoot(lu.fisch.structorizer.elements.Root, java.lang.String)
	 */
	@Override
	protected void subclassUpdateRoot(Root aRoot, String textToParse) {
		// THIS CODE EXAMPLE IS FROM THE CPARSER (derives a name for the main program)
		if (aRoot.getMethodName().equals("???")) {
			if (aRoot.getParameterNames().count() == 0) {
				String fileName = new File(textToParse).getName();
				if (fileName.contains(".")) {
					fileName = fileName.substring(0, fileName.indexOf('.'));
				}
				if (this.optionUpperCaseProgName) {
					fileName = fileName.toUpperCase();
				}
				aRoot.setText(fileName);
				aRoot.isProgram = true;
			}
		}
		// Force returning of the specified result
		if (this.returnMap.containsKey(aRoot)) {
			String resultVar = this.returnMap.get(aRoot);
			int nElements = aRoot.children.getSize();
			if (!aRoot.getMethodName().equals(resultVar) && !resultVar.equalsIgnoreCase("RESULT")
					&& (nElements == 0 || !(aRoot.children.getElement(nElements-1) instanceof Jump))) {
				aRoot.children.addElement(new Instruction(getKeywordOrDefault("preReturn", "return") + " " + this.returnMap.get(aRoot)));
			}
		}
	}

}

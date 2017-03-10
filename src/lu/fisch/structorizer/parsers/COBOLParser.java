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

/**
 ******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Class to parse an COBOL 85 file and build structograms from the reduction tree.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.03.10      First Issue (automatically generated with GOLDprog.exe)
 *
 ******************************************************************************************************
 *
 *     Comment:		
 *     Licensed Material - Property of Ralph Iden (GOLDParser) and Mathew Hawkins (parts of the template)
 *     GOLDParser - code downloaded from https://github.com/ridencww/goldengine on 2017-03-05.<br>
 *     Modifications to this code are allowed as it is a helper class to use the engine.<br>
 *     Template File:  StructorizerParserTemplate.pgt (with elements of both<br>
 *                     Java-MatthewHawkins.pgt and Java-IdenEngine.pgt)<br>
 *     Authors:        Ralph Iden, Matthew Hawkins, Bob Fisch, Kay Gürtzig<br>
 *     Description:    A Sample class, takes in a file and runs the GOLDParser engine on it.<br>
 *
 ******************************************************************************************************/

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.regex.Matcher;

import com.creativewidgetworks.goldparser.parser.*;
import com.creativewidgetworks.goldparser.engine.*;
import com.creativewidgetworks.goldparser.engine.enums.SymbolType;

import lu.fisch.structorizer.elements.Alternative;
import lu.fisch.structorizer.elements.Case;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.elements.Jump;
import lu.fisch.structorizer.elements.Repeat;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.structorizer.elements.While;
import lu.fisch.utils.BString;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.creativewidgetworks.goldparser.engine.ParserException;
import com.creativewidgetworks.goldparser.parser.GOLDParser;

/**
 * Code import parser class of Structorizer 3.27, based on GOLDParser 5.0 for the COBOL 85 language.
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
		return "COBOL-85.egt";
	}
	
	@Override
	protected final String getGrammarTableName()
	{
		return "COBOL 85";
	}

	/**
	 * If this flag is set then program names derived from file name will be made uppercase
	 * This default will be initialized in consistency with the Analyser check 
	 */
	private boolean optionUpperCaseProgName = false;

	//---------------------------- Constructor ---------------------------

	/**
	 * Constructs a parser for language COBOL 85, loads the grammar as resource and
	 * specifies whether to generate the parse tree as string
	 */
	public COBOLParser() {
	}

	//---------------------- File Filter configuration ---------------------------
	
	@Override
	public String getDialogTitle() {
		return "COBOL 85";
	}

	@Override
	protected String getFileDescription() {
		return "COBOL 85 Source Files";
	}

 	@Override
	public String[] getFileExtensions() {
		// TODO specify here the usual file name extensions for COBOL 85 source files!";
		final String[] exts = { "COB", "CBL" };
		return exts;
	}

	//---------------------- Grammar table constants ---------------------------

	// Symbolic constants naming the table indices of the symbols of the grammar 
	private interface SymbolConstants 
	{
       final int SYM_EOF                        =   0;  // (EOF)
       final int SYM_ERROR                      =   1;  // (Error)
       final int SYM_WHITESPACE                 =   2;  // Whitespace
       final int SYM_LPAREN                     =   3;  // '('
       final int SYM_RPAREN                     =   4;  // ')'
       final int SYM_TIMES                      =   5;  // '*'
       final int SYM_PLUS                       =   6;  // '+'
       final int SYM_COMMA                      =   7;  // ','
       final int SYM_MINUS                      =   8;  // '-'
       final int SYM_DOT                        =   9;  // '.'
       final int SYM_DIV                        =  10;  // '/'
       final int SYM_66                         =  11;  // '66'
       final int SYM_77                         =  12;  // '77'
       final int SYM_88                         =  13;  // '88'
       final int SYM_COLON                      =  14;  // ':'
       final int SYM_LT                         =  15;  // '<'
       final int SYM_LTEQ                       =  16;  // '<='
       final int SYM_EQ                         =  17;  // '='
       final int SYM_GT                         =  18;  // '>'
       final int SYM_GTEQ                       =  19;  // '>='
       final int SYM_ACCEPT                     =  20;  // ACCEPT
       final int SYM_ACCESS                     =  21;  // ACCESS
       final int SYM_ADD                        =  22;  // ADD
       final int SYM_ADVANCING                  =  23;  // ADVANCING
       final int SYM_AFTER                      =  24;  // AFTER
       final int SYM_ALL                        =  25;  // ALL
       final int SYM_ALPHABET                   =  26;  // ALPHABET
       final int SYM_ALPHABETIC                 =  27;  // ALPHABETIC
       final int SYM_ALPHABETICMINUSLOWER       =  28;  // 'ALPHABETIC-LOWER'
       final int SYM_ALPHABETICMINUSUPPER       =  29;  // 'ALPHABETIC-UPPER'
       final int SYM_ALPHANUMERIC               =  30;  // ALPHANUMERIC
       final int SYM_ALPHANUMERICMINUSEDITED    =  31;  // 'ALPHANUMERIC-EDITED'
       final int SYM_ALSO                       =  32;  // ALSO
       final int SYM_ALTER                      =  33;  // ALTER
       final int SYM_ALTERNATIVE                =  34;  // ALTERNATIVE
       final int SYM_AND                        =  35;  // And
       final int SYM_ANY                        =  36;  // ANY
       final int SYM_ARE                        =  37;  // ARE
       final int SYM_AREA                       =  38;  // AREA
       final int SYM_AREAS                      =  39;  // AREAS
       final int SYM_ASCENDING                  =  40;  // ASCENDING
       final int SYM_ASSIGN                     =  41;  // ASSIGN
       final int SYM_AT                         =  42;  // AT
       final int SYM_AUTHOR                     =  43;  // AUTHOR
       final int SYM_BACKGROUNDMINUSCOLOR       =  44;  // 'BACKGROUND-COLOR'
       final int SYM_BEFORE                     =  45;  // BEFORE
       final int SYM_BINARY                     =  46;  // BINARY
       final int SYM_BLANK                      =  47;  // BLANK
       final int SYM_BLINK                      =  48;  // BLINK
       final int SYM_BLOCK                      =  49;  // BLOCK
       final int SYM_BOTTOM                     =  50;  // BOTTOM
       final int SYM_BY                         =  51;  // BY
       final int SYM_CALL                       =  52;  // CALL
       final int SYM_CANCEL                     =  53;  // CANCEL
       final int SYM_CD                         =  54;  // CD
       final int SYM_CF                         =  55;  // CF
       final int SYM_CH                         =  56;  // CH
       final int SYM_CHARACTER                  =  57;  // CHARACTER
       final int SYM_CHARACTERS                 =  58;  // CHARACTERS
       final int SYM_CLASS                      =  59;  // CLASS
       final int SYM_CLOCKMINUSUNITS            =  60;  // 'CLOCK-UNITS'
       final int SYM_CLOSE                      =  61;  // CLOSE
       final int SYM_CODE                       =  62;  // CODE
       final int SYM_CODEMINUSSET               =  63;  // 'CODE-SET'
       final int SYM_COLLATING                  =  64;  // COLLATING
       final int SYM_COLUMN                     =  65;  // COLUMN
       final int SYM_COMMA2                     =  66;  // COMMA
       final int SYM_COMMON                     =  67;  // COMMON
       final int SYM_COMMUNICATION              =  68;  // COMMUNICATION
       final int SYM_COMP                       =  69;  // COMP
       final int SYM_COMPUTATIONAL              =  70;  // COMPUTATIONAL
       final int SYM_COMPUTE                    =  71;  // COMPUTE
       final int SYM_CONFIGURATION              =  72;  // CONFIGURATION
       final int SYM_CONSOLE                    =  73;  // CONSOLE
       final int SYM_CONTAINS                   =  74;  // CONTAINS
       final int SYM_CONTENT                    =  75;  // CONTENT
       final int SYM_CONTINUE                   =  76;  // CONTINUE
       final int SYM_CONTROL                    =  77;  // CONTROL
       final int SYM_CONTROLS                   =  78;  // CONTROLS
       final int SYM_CONVERTING                 =  79;  // CONVERTING
       final int SYM_CORR                       =  80;  // CORR
       final int SYM_CORRESPONDING              =  81;  // CORRESPONDING
       final int SYM_COUNT                      =  82;  // COUNT
       final int SYM_CURRENCY                   =  83;  // CURRENCY
       final int SYM_DATA                       =  84;  // DATA
       final int SYM_DATE                       =  85;  // DATE
       final int SYM_DATEMINUSCOMPILED          =  86;  // 'DATE-COMPILED'
       final int SYM_DATEMINUSWRITTEN           =  87;  // 'DATE-WRITTEN'
       final int SYM_DAY                        =  88;  // DAY
       final int SYM_DAYMINUSOFMINUSWEEK        =  89;  // 'DAY-OF-WEEK'
       final int SYM_DE                         =  90;  // DE
       final int SYM_DEBUGGING                  =  91;  // DEBUGGING
       final int SYM_DECIMALMINUSPOINT          =  92;  // 'DECIMAL-POINT'
       final int SYM_DECLARATIVES               =  93;  // DECLARATIVES
       final int SYM_DELETE                     =  94;  // DELETE
       final int SYM_DELIMITED                  =  95;  // DELIMITED
       final int SYM_DELIMITER                  =  96;  // DELIMITER
       final int SYM_DEPENDING                  =  97;  // DEPENDING
       final int SYM_DESCENDING                 =  98;  // DESCENDING
       final int SYM_DESTINATION                =  99;  // DESTINATION
       final int SYM_DETAIL                     = 100;  // DETAIL
       final int SYM_DISABLE                    = 101;  // DISABLE
       final int SYM_DISPLAY                    = 102;  // DISPLAY
       final int SYM_DIVIDE                     = 103;  // DIVIDE
       final int SYM_DIVISION                   = 104;  // DIVISION
       final int SYM_DOWN                       = 105;  // DOWN
       final int SYM_DUPLICATES                 = 106;  // DUPLICATES
       final int SYM_DYNAMIC                    = 107;  // DYNAMIC
       final int SYM_EGI                        = 108;  // EGI
       final int SYM_ELSE                       = 109;  // ELSE
       final int SYM_EMI                        = 110;  // EMI
       final int SYM_ENABLE                     = 111;  // ENABLE
       final int SYM_END                        = 112;  // END
       final int SYM_ENDMINUSADD                = 113;  // 'END-ADD'
       final int SYM_ENDMINUSCALL               = 114;  // 'END-CALL'
       final int SYM_ENDMINUSCOMPUTE            = 115;  // 'END-COMPUTE'
       final int SYM_ENDMINUSDELETE             = 116;  // 'END-DELETE'
       final int SYM_ENDMINUSDIVIDE             = 117;  // 'END-DIVIDE'
       final int SYM_ENDMINUSEVALUATE           = 118;  // 'END-EVALUATE'
       final int SYM_ENDMINUSIF                 = 119;  // 'END-IF'
       final int SYM_ENDMINUSMOVE               = 120;  // 'END-MOVE'
       final int SYM_ENDMINUSMULTIPLY           = 121;  // 'END-MULTIPLY'
       final int SYM_ENDMINUSOFMINUSPAGE        = 122;  // 'END-OF-PAGE'
       final int SYM_ENDMINUSPERFORM            = 123;  // 'END-PERFORM'
       final int SYM_ENDMINUSREAD               = 124;  // 'END-READ'
       final int SYM_ENDMINUSREWRITE            = 125;  // 'END-REWRITE'
       final int SYM_ENDMINUSSEARCH             = 126;  // 'END-SEARCH'
       final int SYM_ENDMINUSSTART              = 127;  // 'END-START'
       final int SYM_ENDMINUSSTRING             = 128;  // 'END-STRING'
       final int SYM_ENDMINUSSUBTRACT           = 129;  // 'END-SUBTRACT'
       final int SYM_ENDMINUSUNSTRING           = 130;  // 'END-UNSTRING'
       final int SYM_ENDMINUSWRITE              = 131;  // 'END-WRITE'
       final int SYM_ENVIRONMENT                = 132;  // ENVIRONMENT
       final int SYM_EOP                        = 133;  // EOP
       final int SYM_EQUAL                      = 134;  // EQUAL
       final int SYM_ERROR2                     = 135;  // ERROR
       final int SYM_ESI                        = 136;  // ESI
       final int SYM_EVALUATE                   = 137;  // EVALUATE
       final int SYM_EVERY                      = 138;  // EVERY
       final int SYM_EXCEPTION                  = 139;  // EXCEPTION
       final int SYM_EXIT                       = 140;  // EXIT
       final int SYM_EXTEND                     = 141;  // EXTEND
       final int SYM_EXTERNAL                   = 142;  // EXTERNAL
       final int SYM_FD                         = 143;  // FD
       final int SYM_FILE                       = 144;  // FILE
       final int SYM_FILEMINUSCONTROL           = 145;  // 'FILE-CONTROL'
       final int SYM_FILLER                     = 146;  // FILLER
       final int SYM_FINAL                      = 147;  // FINAL
       final int SYM_FIRST                      = 148;  // FIRST
       final int SYM_FLOATLITERAL               = 149;  // FloatLiteral
       final int SYM_FOOTING                    = 150;  // FOOTING
       final int SYM_FOR                        = 151;  // FOR
       final int SYM_FOREGROUNDMINUSCOLOR       = 152;  // 'FOREGROUND-COLOR'
       final int SYM_FROM                       = 153;  // FROM
       final int SYM_GENERATE                   = 154;  // GENERATE
       final int SYM_GIVING                     = 155;  // GIVING
       final int SYM_GLOBAL                     = 156;  // GLOBAL
       final int SYM_GO                         = 157;  // GO
       final int SYM_GREATER                    = 158;  // GREATER
       final int SYM_GROUP                      = 159;  // GROUP
       final int SYM_HEADING                    = 160;  // HEADING
       final int SYM_HIGHMINUSVALUE             = 161;  // 'HIGH-VALUE'
       final int SYM_HIGHMINUSVALUES            = 162;  // 'HIGH-VALUES'
       final int SYM_HIGHLIGHT                  = 163;  // HIGHLIGHT
       final int SYM_IMINUSO                    = 164;  // 'I-O'
       final int SYM_IMINUSOMINUSCONTROL        = 165;  // 'I-O-CONTROL'
       final int SYM_IDENTIFICATION             = 166;  // IDENTIFICATION
       final int SYM_IDENTIFIER                 = 167;  // Identifier
       final int SYM_IF                         = 168;  // IF
       final int SYM_IN                         = 169;  // IN
       final int SYM_INDEX                      = 170;  // INDEX
       final int SYM_INDEXED                    = 171;  // INDEXED
       final int SYM_INDICATE                   = 172;  // INDICATE
       final int SYM_INITIAL                    = 173;  // INITIAL
       final int SYM_INITIALIZE                 = 174;  // INITIALIZE
       final int SYM_INITIATE                   = 175;  // INITIATE
       final int SYM_INPUT                      = 176;  // INPUT
       final int SYM_INPUTMINUSOUTPUT           = 177;  // 'INPUT-OUTPUT'
       final int SYM_INSPECT                    = 178;  // INSPECT
       final int SYM_INSTALLATION               = 179;  // INSTALLATION
       final int SYM_INTERNAL                   = 180;  // INTERNAL
       final int SYM_INTLITERAL                 = 181;  // IntLiteral
       final int SYM_INTO                       = 182;  // INTO
       final int SYM_INVALID                    = 183;  // INVALID
       final int SYM_IS                         = 184;  // IS
       final int SYM_JUST                       = 185;  // JUST
       final int SYM_JUSTIFIED                  = 186;  // JUSTIFIED
       final int SYM_KEY                        = 187;  // KEY
       final int SYM_LABEL                      = 188;  // LABEL
       final int SYM_LEADING                    = 189;  // LEADING
       final int SYM_LEFT                       = 190;  // LEFT
       final int SYM_LENGTH                     = 191;  // LENGTH
       final int SYM_LESS                       = 192;  // LESS
       final int SYM_LIMIT                      = 193;  // LIMIT
       final int SYM_LIMITS                     = 194;  // LIMITS
       final int SYM_LINAGE                     = 195;  // LINAGE
       final int SYM_LINE                       = 196;  // LINE
       final int SYM_LINES                      = 197;  // LINES
       final int SYM_LINKAGE                    = 198;  // LINKAGE
       final int SYM_LOCK                       = 199;  // LOCK
       final int SYM_LOWMINUSVALUE              = 200;  // 'LOW-VALUE'
       final int SYM_LOWMINUSVALUES             = 201;  // 'LOW-VALUES'
       final int SYM_MERGE                      = 202;  // MERGE
       final int SYM_MESSAGE                    = 203;  // MESSAGE
       final int SYM_MODE                       = 204;  // MODE
       final int SYM_MOVE                       = 205;  // MOVE
       final int SYM_MULTIPLE                   = 206;  // MULTIPLE
       final int SYM_MULTIPLY                   = 207;  // MULTIPLY
       final int SYM_NATIVE                     = 208;  // NATIVE
       final int SYM_NEXT                       = 209;  // NEXT
       final int SYM_NO                         = 210;  // NO
       final int SYM_NOT                        = 211;  // NOT
       final int SYM_NULL                       = 212;  // NULL
       final int SYM_NULLS                      = 213;  // NULLS
       final int SYM_NUMBER                     = 214;  // NUMBER
       final int SYM_NUMERIC                    = 215;  // NUMERIC
       final int SYM_NUMERICMINUSEDITED         = 216;  // 'NUMERIC-EDITED'
       final int SYM_OBJECTMINUSCOMPUTER        = 217;  // 'OBJECT-COMPUTER'
       final int SYM_OCCURS                     = 218;  // OCCURS
       final int SYM_OF                         = 219;  // OF
       final int SYM_OFF                        = 220;  // OFF
       final int SYM_OMITTED                    = 221;  // OMITTED
       final int SYM_ON                         = 222;  // ON
       final int SYM_OPEN                       = 223;  // OPEN
       final int SYM_OPTIONAL                   = 224;  // OPTIONAL
       final int SYM_OR                         = 225;  // OR
       final int SYM_ORDER                      = 226;  // ORDER
       final int SYM_ORGANIZATION               = 227;  // ORGANIZATION
       final int SYM_OTHER                      = 228;  // OTHER
       final int SYM_OUTPUT                     = 229;  // OUTPUT
       final int SYM_OVERFLOW                   = 230;  // OVERFLOW
       final int SYM_PACKEDMINUSDECIMAL         = 231;  // 'PACKED-DECIMAL'
       final int SYM_PADDING                    = 232;  // PADDING
       final int SYM_PAGE                       = 233;  // PAGE
       final int SYM_PERFORM                    = 234;  // PERFORM
       final int SYM_PF                         = 235;  // PF
       final int SYM_PH                         = 236;  // PH
       final int SYM_PICSTRING                  = 237;  // PicString
       final int SYM_PLUS2                      = 238;  // PLUS
       final int SYM_POINTER                    = 239;  // POINTER
       final int SYM_POSITION                   = 240;  // POSITION
       final int SYM_PRINTING                   = 241;  // PRINTING
       final int SYM_PROCEDURE                  = 242;  // PROCEDURE
       final int SYM_PROCEDURES                 = 243;  // PROCEDURES
       final int SYM_PROCEED                    = 244;  // PROCEED
       final int SYM_PROGRAM                    = 245;  // PROGRAM
       final int SYM_PROGRAMMINUSID             = 246;  // 'PROGRAM-ID'
       final int SYM_QUEUE                      = 247;  // QUEUE
       final int SYM_QUOTE                      = 248;  // QUOTE
       final int SYM_QUOTES                     = 249;  // QUOTES
       final int SYM_RANDOM                     = 250;  // RANDOM
       final int SYM_RD                         = 251;  // RD
       final int SYM_READ                       = 252;  // READ
       final int SYM_RECORD                     = 253;  // RECORD
       final int SYM_RECORDS                    = 254;  // RECORDS
       final int SYM_REDEFINES                  = 255;  // REDEFINES
       final int SYM_REEL                       = 256;  // REEL
       final int SYM_REFERENCE                  = 257;  // REFERENCE
       final int SYM_REFERENCES                 = 258;  // REFERENCES
       final int SYM_RELATIVE                   = 259;  // RELATIVE
       final int SYM_RELEASE                    = 260;  // RELEASE
       final int SYM_REMAINDER                  = 261;  // REMAINDER
       final int SYM_REMOVAL                    = 262;  // REMOVAL
       final int SYM_RENAMES                    = 263;  // RENAMES
       final int SYM_REPLACING                  = 264;  // REPLACING
       final int SYM_REPORT                     = 265;  // REPORT
       final int SYM_REPORTING                  = 266;  // REPORTING
       final int SYM_RERUN                      = 267;  // RERUN
       final int SYM_RESERVE                    = 268;  // RESERVE
       final int SYM_RESET                      = 269;  // RESET
       final int SYM_RETURN                     = 270;  // RETURN
       final int SYM_REVERSEMINUSVIDEO          = 271;  // 'REVERSE-VIDEO'
       final int SYM_REWIND                     = 272;  // REWIND
       final int SYM_REWRITE                    = 273;  // REWRITE
       final int SYM_RF                         = 274;  // RF
       final int SYM_RH                         = 275;  // RH
       final int SYM_RIGHT                      = 276;  // RIGHT
       final int SYM_ROUNDED                    = 277;  // ROUNDED
       final int SYM_RUN                        = 278;  // RUN
       final int SYM_SAME                       = 279;  // SAME
       final int SYM_SCREEN                     = 280;  // SCREEN
       final int SYM_SD                         = 281;  // SD
       final int SYM_SEARCH                     = 282;  // SEARCH
       final int SYM_SECTION                    = 283;  // SECTION
       final int SYM_SECURITY                   = 284;  // SECURITY
       final int SYM_SEGMENTMINUSLIMIT          = 285;  // 'SEGMENT-LIMIT'
       final int SYM_SELECT                     = 286;  // SELECT
       final int SYM_SEND                       = 287;  // SEND
       final int SYM_SENTENCE                   = 288;  // SENTENCE
       final int SYM_SEPARATE                   = 289;  // SEPARATE
       final int SYM_SEQUENCE                   = 290;  // SEQUENCE
       final int SYM_SEQUENTIAL                 = 291;  // SEQUENTIAL
       final int SYM_SET                        = 292;  // SET
       final int SYM_SIGN                       = 293;  // SIGN
       final int SYM_SIZE                       = 294;  // SIZE
       final int SYM_SORT                       = 295;  // SORT
       final int SYM_SORTMINUSMERGE             = 296;  // 'SORT-MERGE'
       final int SYM_SOURCE                     = 297;  // SOURCE
       final int SYM_SOURCEMINUSCOMPUTER        = 298;  // 'SOURCE-COMPUTER'
       final int SYM_SPACE                      = 299;  // SPACE
       final int SYM_SPACES                     = 300;  // SPACES
       final int SYM_SPECIALMINUSNAMES          = 301;  // 'SPECIAL-NAMES'
       final int SYM_STANDARD                   = 302;  // STANDARD
       final int SYM_STANDARDMINUS1             = 303;  // 'STANDARD-1'
       final int SYM_STANDARDMINUS2             = 304;  // 'STANDARD-2'
       final int SYM_START                      = 305;  // START
       final int SYM_STATUS                     = 306;  // STATUS
       final int SYM_STOP                       = 307;  // STOP
       final int SYM_STRING                     = 308;  // STRING
       final int SYM_STRINGLITERAL              = 309;  // StringLiteral
       final int SYM_SUBMINUSQUEUEMINUS1        = 310;  // 'SUB-QUEUE-1'
       final int SYM_SUBMINUSQUEUEMINUS2        = 311;  // 'SUB-QUEUE-2'
       final int SYM_SUBMINUSQUEUEMINUS3        = 312;  // 'SUB-QUEUE-3'
       final int SYM_SUBTRACT                   = 313;  // SUBTRACT
       final int SYM_SUM                        = 314;  // SUM
       final int SYM_SUPPRESS                   = 315;  // SUPPRESS
       final int SYM_SYMBOLIC                   = 316;  // SYMBOLIC
       final int SYM_SYNC                       = 317;  // SYNC
       final int SYM_SYNCHRONIZED               = 318;  // SYNCHRONIZED
       final int SYM_TABLES                     = 319;  // TABLES
       final int SYM_TALLYING                   = 320;  // TALLYING
       final int SYM_TAPE                       = 321;  // TAPE
       final int SYM_TERMINAL                   = 322;  // TERMINAL
       final int SYM_TERMINATE                  = 323;  // TERMINATE
       final int SYM_TEST                       = 324;  // TEST
       final int SYM_TEXT                       = 325;  // TEXT
       final int SYM_THAN                       = 326;  // THAN
       final int SYM_THEN                       = 327;  // THEN
       final int SYM_THROUGH                    = 328;  // THROUGH
       final int SYM_THRU                       = 329;  // THRU
       final int SYM_TIME                       = 330;  // TIME
       final int SYM_TIMES2                     = 331;  // TIMES
       final int SYM_TO                         = 332;  // TO
       final int SYM_TOP                        = 333;  // TOP
       final int SYM_TRAILING                   = 334;  // TRAILING
       final int SYM_TRUE                       = 335;  // TRUE
       final int SYM_TYPE                       = 336;  // TYPE
       final int SYM_UNDERLINE                  = 337;  // UNDERLINE
       final int SYM_UNIT                       = 338;  // UNIT
       final int SYM_UNSTRING                   = 339;  // UNSTRING
       final int SYM_UNTIL                      = 340;  // UNTIL
       final int SYM_UP                         = 341;  // UP
       final int SYM_UPON                       = 342;  // UPON
       final int SYM_USAGE                      = 343;  // USAGE
       final int SYM_USE                        = 344;  // USE
       final int SYM_USING                      = 345;  // USING
       final int SYM_VALUE                      = 346;  // VALUE
       final int SYM_VALUES                     = 347;  // VALUES
       final int SYM_VARYING                    = 348;  // VARYING
       final int SYM_WHEN                       = 349;  // WHEN
       final int SYM_WITH                       = 350;  // WITH
       final int SYM_WORKINGMINUSSTORAGE        = 351;  // 'WORKING-STORAGE'
       final int SYM_WRITE                      = 352;  // WRITE
       final int SYM_ZERO                       = 353;  // ZERO
       final int SYM_ZEROES                     = 354;  // ZEROES
       final int SYM_ZEROS                      = 355;  // ZEROS
       final int SYM_ACCEPTEMBED                = 356;  // <Accept Embed>
       final int SYM_ACCEPTFROMARG              = 357;  // <Accept From Arg>
       final int SYM_ACCEPTIMP                  = 358;  // <Accept Imp>
       final int SYM_ACCEPTSENT                 = 359;  // <Accept Sent>
       final int SYM_ACCEPTSTM                  = 360;  // <Accept Stm>
       final int SYM_ACCESSMODE                 = 361;  // <Access Mode>
       final int SYM_ADDEMBED                   = 362;  // <Add Embed>
       final int SYM_ADDIMP                     = 363;  // <Add Imp>
       final int SYM_ADDITEM                    = 364;  // <Add Item>
       final int SYM_ADDITEMS                   = 365;  // <Add Items>
       final int SYM_ADDSENT                    = 366;  // <Add Sent>
       final int SYM_ADDSTM                     = 367;  // <Add Stm>
       final int SYM_ADVANCINGCLAUSE            = 368;  // <Advancing Clause>
       final int SYM_ADVANCINGOPT               = 369;  // <ADVANCING Opt>
       final int SYM_ALLOPT                     = 370;  // <ALL Opt>
       final int SYM_ALPHABETITEM               = 371;  // <Alphabet Item>
       final int SYM_ALTEREMBED                 = 372;  // <Alter Embed>
       final int SYM_ALTERIMP                   = 373;  // <Alter Imp>
       final int SYM_ALTERSENT                  = 374;  // <Alter Sent>
       final int SYM_ALTERSTM                   = 375;  // <Alter Stm>
       final int SYM_ANDEXP                     = 376;  // <And Exp>
       final int SYM_AREOPT                     = 377;  // <ARE Opt>
       final int SYM_AREAOPT                    = 378;  // <AREA Opt>
       final int SYM_ATENDCLAUSE                = 379;  // <At End Clause>
       final int SYM_ATENDCLAUSES               = 380;  // <At End Clauses>
       final int SYM_ATEOPCLAUSE                = 381;  // <AT EOP Clause>
       final int SYM_ATEOPCLAUSES               = 382;  // <AT EOP Clauses>
       final int SYM_ATOPT                      = 383;  // <AT Opt>
       final int SYM_BEFOREAFTER                = 384;  // <BEFORE AFTER>
       final int SYM_BOOLEANEXP                 = 385;  // <Boolean Exp>
       final int SYM_BYOPT                      = 386;  // <BY Opt>
       final int SYM_CALLEMBED                  = 387;  // <Call Embed>
       final int SYM_CALLIMP                    = 388;  // <Call Imp>
       final int SYM_CALLITEM                   = 389;  // <Call Item>
       final int SYM_CALLITEMS                  = 390;  // <Call Items>
       final int SYM_CALLSENT                   = 391;  // <Call Sent>
       final int SYM_CALLSTM                    = 392;  // <Call Stm>
       final int SYM_CANCELEMBED                = 393;  // <Cancel Embed>
       final int SYM_CANCELIMP                  = 394;  // <Cancel Imp>
       final int SYM_CANCELSENT                 = 395;  // <Cancel Sent>
       final int SYM_CANCELSTM                  = 396;  // <Cancel Stm>
       final int SYM_CHARACTEROPT               = 397;  // <Character Opt>
       final int SYM_CHARACTERSOPT              = 398;  // <CHARACTERS Opt>
       final int SYM_CLOSEEMBED                 = 399;  // <Close Embed>
       final int SYM_CLOSEIMP                   = 400;  // <Close Imp>
       final int SYM_CLOSEITEM                  = 401;  // <Close Item>
       final int SYM_CLOSEITEMS                 = 402;  // <Close Items>
       final int SYM_CLOSEMETHOD                = 403;  // <Close Method>
       final int SYM_CLOSEOPTIONS               = 404;  // <Close Options>
       final int SYM_CLOSESENT                  = 405;  // <Close Sent>
       final int SYM_CLOSESTM                   = 406;  // <Close Stm>
       final int SYM_COLLATINGCLAUSE            = 407;  // <Collating Clause>
       final int SYM_COLLATINGOPT               = 408;  // <COLLATING Opt>
       final int SYM_COMMDESC                   = 409;  // <Comm Desc>
       final int SYM_COMMDESCLIST               = 410;  // <Comm Desc List>
       final int SYM_COMMIMINUSOBODY            = 411;  // <Comm I-O Body>
       final int SYM_COMMIMINUSOOPTION          = 412;  // <Comm I-O Option>
       final int SYM_COMMIMINUSOOPTIONS         = 413;  // <Comm I-O Options>
       final int SYM_COMMINPUTBODY              = 414;  // <Comm Input Body>
       final int SYM_COMMINPUTOPTION            = 415;  // <Comm Input Option>
       final int SYM_COMMINPUTOPTIONS           = 416;  // <Comm Input Options>
       final int SYM_COMMOUTPUTOPTION           = 417;  // <Comm Output Option>
       final int SYM_COMMOUTPUTOPTIONS          = 418;  // <Comm Output Options>
       final int SYM_COMMONINITIAL              = 419;  // <Common Initial>
       final int SYM_COMMUNICATIONSECTION       = 420;  // <Communication Section>
       final int SYM_COMPAREEXP                 = 421;  // <Compare Exp>
       final int SYM_COMPAREOP                  = 422;  // <Compare Op>
       final int SYM_COMPUTEEMBED               = 423;  // <Compute Embed>
       final int SYM_COMPUTEIMP                 = 424;  // <Compute Imp>
       final int SYM_COMPUTESENT                = 425;  // <Compute Sent>
       final int SYM_COMPUTESTM                 = 426;  // <Compute Stm>
       final int SYM_CONFIGSECTION              = 427;  // <Config Section>
       final int SYM_CONFIGSECTIONITEM          = 428;  // <Config Section Item>
       final int SYM_CONFIGSECTIONITEMS         = 429;  // <Config Section Items>
       final int SYM_CONTAINITEM                = 430;  // <Contain Item>
       final int SYM_CONTAINLIST                = 431;  // <Contain List>
       final int SYM_CONTAINSOPT                = 432;  // <CONTAINS Opt>
       final int SYM_CONTINUEEMBED              = 433;  // <Continue Embed>
       final int SYM_CONTINUEIMP                = 434;  // <Continue Imp>
       final int SYM_CONTINUESENT               = 435;  // <Continue Sent>
       final int SYM_CONTINUESTM                = 436;  // <Continue Stm>
       final int SYM_CONTROLIS                  = 437;  // <CONTROL IS>
       final int SYM_CORRESPONDING2             = 438;  // <CORRESPONDING>
       final int SYM_DATADIVISION               = 439;  // <Data Division>
       final int SYM_DATAOPT                    = 440;  // <DATA Opt>
       final int SYM_DATASECTIONENTRY           = 441;  // <Data Section Entry>
       final int SYM_DATASECTIONLIST            = 442;  // <Data Section List>
       final int SYM_DECLARATIVEBLOCK           = 443;  // <Declarative Block>
       final int SYM_DECLARATIVESECTION         = 444;  // <Declarative Section>
       final int SYM_DECLARATIVESECTIONS        = 445;  // <Declarative Sections>
       final int SYM_DELETEEMBED                = 446;  // <Delete Embed>
       final int SYM_DELETEIMP                  = 447;  // <Delete Imp>
       final int SYM_DELETESENT                 = 448;  // <Delete Sent>
       final int SYM_DELETESTM                  = 449;  // <Delete Stm>
       final int SYM_DELIMITERCLAUSE            = 450;  // <Delimiter Clause>
       final int SYM_DISABLEEMBED               = 451;  // <Disable Embed>
       final int SYM_DISABLEIMP                 = 452;  // <Disable Imp>
       final int SYM_DISABLESENT                = 453;  // <Disable Sent>
       final int SYM_DISABLESTM                 = 454;  // <Disable Stm>
       final int SYM_DISPLAYEMBED               = 455;  // <Display Embed>
       final int SYM_DISPLAYIMP                 = 456;  // <Display Imp>
       final int SYM_DISPLAYSENT                = 457;  // <Display Sent>
       final int SYM_DISPLAYSTM                 = 458;  // <Display Stm>
       final int SYM_DISPLAYTARGET              = 459;  // <Display Target>
       final int SYM_DIVIDEEMBED                = 460;  // <Divide Embed>
       final int SYM_DIVIDEIMP                  = 461;  // <Divide Imp>
       final int SYM_DIVIDESENT                 = 462;  // <Divide Sent>
       final int SYM_DIVIDESTM                  = 463;  // <Divide Stm>
       final int SYM_DUPLICATESCLAUSEOPT        = 464;  // <Duplicates Clause Opt>
       final int SYM_EMBEDSTM                   = 465;  // <Embed Stm>
       final int SYM_EMBEDSTMS                  = 466;  // <Embed Stms>
       final int SYM_ENABLEDISABLEKEY           = 467;  // <Enable Disable Key>
       final int SYM_ENABLEEMBED                = 468;  // <Enable Embed>
       final int SYM_ENABLEIMP                  = 469;  // <Enable Imp>
       final int SYM_ENABLESENT                 = 470;  // <Enable Sent>
       final int SYM_ENABLESTM                  = 471;  // <Enable Stm>
       final int SYM_ENABLEDDISABLEMODE         = 472;  // <Enabled Disable Mode>
       final int SYM_ENDOFOPT                   = 473;  // <End Of Opt>
       final int SYM_ENDOFPAGE                  = 474;  // <End of Page>
       final int SYM_ENDMINUSADDOPT             = 475;  // <END-ADD Opt>
       final int SYM_ENDMINUSCALLOPT            = 476;  // <END-CALL Opt>
       final int SYM_ENDMINUSCOMPUTEOPT         = 477;  // <END-COMPUTE Opt>
       final int SYM_ENDMINUSDELETEOPT          = 478;  // <END-DELETE Opt>
       final int SYM_ENDMINUSDIVIDEOPT          = 479;  // <END-DIVIDE Opt>
       final int SYM_ENDMINUSEVALUATEOPT        = 480;  // <END-EVALUATE Opt>
       final int SYM_ENDMINUSIFOPT              = 481;  // <END-IF Opt>
       final int SYM_ENDMINUSMOVEOPT            = 482;  // <END-MOVE Opt>
       final int SYM_ENDMINUSMULTIPLYOPT        = 483;  // <END-MULTIPLY Opt>
       final int SYM_ENDMINUSPERFORMOPT         = 484;  // <END-PERFORM Opt>
       final int SYM_ENDMINUSREADOPT            = 485;  // <END-READ Opt>
       final int SYM_ENDMINUSREWRITEOPT         = 486;  // <END-REWRITE Opt>
       final int SYM_ENDMINUSSEARCHOPT          = 487;  // <END-SEARCH Opt>
       final int SYM_ENDMINUSSTARTOPT           = 488;  // <END-START Opt>
       final int SYM_ENDMINUSSTRINGOPT          = 489;  // <END-STRING Opt>
       final int SYM_ENDMINUSSUBTRACTOPT        = 490;  // <END-SUBTRACT Opt>
       final int SYM_ENDMINUSUNSTRINGOPT        = 491;  // <END-UNSTRING Opt>
       final int SYM_ENDMINUSWRITEOPT           = 492;  // <END-WRITE Opt>
       final int SYM_ENVIRONMENTDIVISION        = 493;  // <Environment Division>
       final int SYM_EQUALOP                    = 494;  // <Equal Op>
       final int SYM_ERRORCAUSE                 = 495;  // <Error Cause>
       final int SYM_EVALUATEEMBED              = 496;  // <Evaluate Embed>
       final int SYM_EVALUATEIMP                = 497;  // <Evaluate Imp>
       final int SYM_EVALUATESENT               = 498;  // <Evaluate Sent>
       final int SYM_EVALUATESTM                = 499;  // <Evaluate Stm>
       final int SYM_EVERYCLAUSE                = 500;  // <Every Clause>
       final int SYM_EVERYENDTARGET             = 501;  // <Every End Target>
       final int SYM_EVERYOPT                   = 502;  // <EVERY Opt>
       final int SYM_EXCEPTIONCLAUSE            = 503;  // <Exception Clause>
       final int SYM_EXCEPTIONCLAUSES           = 504;  // <Exception Clauses>
       final int SYM_EXITEMBED                  = 505;  // <Exit Embed>
       final int SYM_EXITIMP                    = 506;  // <Exit Imp>
       final int SYM_EXITSENT                   = 507;  // <Exit Sent>
       final int SYM_EXITSTM                    = 508;  // <Exit Stm>
       final int SYM_FIELDDEFITEM               = 509;  // <Field Def Item>
       final int SYM_FIELDDEFLIST               = 510;  // <Field Def List>
       final int SYM_FIELDNAMEOPT               = 511;  // <Field Name opt>
       final int SYM_FIGURATIVE                 = 512;  // <Figurative>
       final int SYM_FILEBLOCKOPTION            = 513;  // <File Block Option>
       final int SYM_FILEBLOCKUNITS             = 514;  // <File Block Units>
       final int SYM_FILECODEMINUSSETOPTION     = 515;  // <File Code-Set Option>
       final int SYM_FILEDATAOPTION             = 516;  // <File Data Option>
       final int SYM_FILEDESCBLOCK              = 517;  // <File Desc Block>
       final int SYM_FILEDESCENTRY              = 518;  // <File Desc Entry>
       final int SYM_FILEISOPTION               = 519;  // <File Is Option>
       final int SYM_FILELABELOPTION            = 520;  // <File Label Option>
       final int SYM_FILELABELTYPE              = 521;  // <File Label Type>
       final int SYM_FILELINAGEBOTTOM           = 522;  // <File Linage Bottom>
       final int SYM_FILELINAGEFOOTING          = 523;  // <File Linage Footing>
       final int SYM_FILELINAGEOPTION           = 524;  // <File Linage Option>
       final int SYM_FILELINAGETOP              = 525;  // <File Linage Top>
       final int SYM_FILENAME                   = 526;  // <File Name>
       final int SYM_FILENAMELIST               = 527;  // <File Name List>
       final int SYM_FILEOPT                    = 528;  // <FILE Opt>
       final int SYM_FILEOPTION                 = 529;  // <File Option>
       final int SYM_FILEOPTIONLIST             = 530;  // <File Option List>
       final int SYM_FILERECORDDEPENDINGCLAUSE  = 531;  // <File Record Depending Clause>
       final int SYM_FILERECORDOPTION           = 532;  // <File Record Option>
       final int SYM_FILERECORDSIZECLAUSE       = 533;  // <File Record Size Clause>
       final int SYM_FILESECTION                = 534;  // <File Section>
       final int SYM_FILEVALUEITEM              = 535;  // <File Value Item>
       final int SYM_FILEVALUELIST              = 536;  // <File Value List>
       final int SYM_FILEVALUEOPTION            = 537;  // <File Value Option>
       final int SYM_FILEMINUSCONTROL2          = 538;  // <File-Control>
       final int SYM_FINALOPT                   = 539;  // <FINAL Opt>
       final int SYM_FOROPT                     = 540;  // <FOR Opt>
       final int SYM_GENERATEEMBED              = 541;  // <Generate Embed>
       final int SYM_GENERATEIMP                = 542;  // <Generate Imp>
       final int SYM_GENERATESENT               = 543;  // <Generate Sent>
       final int SYM_GENERATESTM                = 544;  // <Generate Stm>
       final int SYM_GIVINGCLAUSE               = 545;  // <Giving Clause>
       final int SYM_GIVINGCLAUSEOPT            = 546;  // <Giving Clause Opt>
       final int SYM_GLOBALOPT                  = 547;  // <GLOBAL Opt>
       final int SYM_GOTOEMBED                  = 548;  // <Go To Embed>
       final int SYM_GOTOIMP                    = 549;  // <Go To Imp>
       final int SYM_GOTOSENT                   = 550;  // <Go To Sent>
       final int SYM_GOTOSTM                    = 551;  // <Go To Stm>
       final int SYM_GREATEREQOP                = 552;  // <Greater Eq Op>
       final int SYM_GREATEROP                  = 553;  // <Greater Op>
       final int SYM_IMINUSOMINUSCONTROL2       = 554;  // <I-O-Control>
       final int SYM_IDENTIFICATIONDIVISION     = 555;  // <Identification Division>
       final int SYM_IDENTIFIERRANGE            = 556;  // <Identifier Range>
       final int SYM_IDENTIFIERS                = 557;  // <Identifiers>
       final int SYM_IFEMBED                    = 558;  // <If Embed>
       final int SYM_IFIMP                      = 559;  // <If Imp>
       final int SYM_IFSENT                     = 560;  // <If Sent>
       final int SYM_IFSTM                      = 561;  // <If Stm>
       final int SYM_IMPERATIVESTM              = 562;  // <Imperative Stm>
       final int SYM_IMPERATIVESTMS             = 563;  // <Imperative Stms>
       final int SYM_INOPT                      = 564;  // <IN Opt>
       final int SYM_INDEXCLAUSE                = 565;  // <Index Clause>
       final int SYM_INDICATEOPT                = 566;  // <INDICATE Opt>
       final int SYM_INITIALOPT                 = 567;  // <INITIAL Opt>
       final int SYM_INITIALIZEEMBED            = 568;  // <Initialize Embed>
       final int SYM_INITIALIZEIMP              = 569;  // <Initialize Imp>
       final int SYM_INITIALIZESENT             = 570;  // <Initialize Sent>
       final int SYM_INITIALIZESTM              = 571;  // <Initialize Stm>
       final int SYM_INITIATEEMBED              = 572;  // <Initiate Embed>
       final int SYM_INITIATEIMP                = 573;  // <Initiate Imp>
       final int SYM_INITIATESENT               = 574;  // <Initiate Sent>
       final int SYM_INITIATESTM                = 575;  // <Initiate Stm>
       final int SYM_INPUTMINUSOUTPUTSECTION    = 576;  // <Input-Output Section>
       final int SYM_INSPECTEMBED               = 577;  // <Inspect Embed>
       final int SYM_INSPECTIMP                 = 578;  // <Inspect Imp>
       final int SYM_INSPECTSENT                = 579;  // <Inspect Sent>
       final int SYM_INSPECTSPEC                = 580;  // <Inspect Spec>
       final int SYM_INSPECTSPECS               = 581;  // <Inspect Specs>
       final int SYM_INSPECTSTM                 = 582;  // <Inspect Stm>
       final int SYM_INTCONSTANT                = 583;  // <Int Constant>
       final int SYM_INTEGER                    = 584;  // <Integer>
       final int SYM_INVALIDKEYCLAUSE           = 585;  // <Invalid Key Clause>
       final int SYM_INVALIDKEYCLAUSES          = 586;  // <Invalid Key Clauses>
       final int SYM_ISAREOPT                   = 587;  // <IS ARE Opt>
       final int SYM_ISOPT                      = 588;  // <IS Opt>
       final int SYM_KEYCLAUSE                  = 589;  // <Key Clause>
       final int SYM_KEYOPT                     = 590;  // <KEY Opt>
       final int SYM_LEFTRIGHTOPT               = 591;  // <Left Right Opt>
       final int SYM_LESSEQOP                   = 592;  // <Less Eq Op>
       final int SYM_LESSOP                     = 593;  // <Less Op>
       final int SYM_LEVELNAME                  = 594;  // <Level Name>
       final int SYM_LIMITSISOPT                = 595;  // <LIMITS IS Opt>
       final int SYM_LINEOPT                    = 596;  // <LINE Opt>
       final int SYM_LINESOPT                   = 597;  // <LINES Opt>
       final int SYM_LINKAGESECTION             = 598;  // <Linkage Section>
       final int SYM_LITERAL                    = 599;  // <Literal>
       final int SYM_MATHEXP                    = 600;  // <Math Exp>
       final int SYM_MERGEEMBED                 = 601;  // <Merge Embed>
       final int SYM_MERGEIMP                   = 602;  // <Merge Imp>
       final int SYM_MERGESENT                  = 603;  // <Merge Sent>
       final int SYM_MERGESTM                   = 604;  // <Merge Stm>
       final int SYM_MESSAGEOPT                 = 605;  // <MESSAGE opt>
       final int SYM_MODEOPT                    = 606;  // <MODE Opt>
       final int SYM_MOVEEMBED                  = 607;  // <Move Embed>
       final int SYM_MOVEIMP                    = 608;  // <Move Imp>
       final int SYM_MOVESENT                   = 609;  // <Move Sent>
       final int SYM_MOVESTM                    = 610;  // <Move Stm>
       final int SYM_MULTEXP                    = 611;  // <Mult Exp>
       final int SYM_MULTIPLEITEM               = 612;  // <Multiple Item>
       final int SYM_MULTIPLELIST               = 613;  // <Multiple List>
       final int SYM_MULTIPLYEMBED              = 614;  // <Multiply Embed>
       final int SYM_MULTIPLYIMP                = 615;  // <Multiply Imp>
       final int SYM_MULTIPLYITEM               = 616;  // <Multiply Item>
       final int SYM_MULTIPLYITEMS              = 617;  // <Multiply Items>
       final int SYM_MULTIPLYSENT               = 618;  // <Multiply Sent>
       final int SYM_MULTIPLYSTM                = 619;  // <Multiply Stm>
       final int SYM_NAMESTATUSITEM             = 620;  // <Name Status Item>
       final int SYM_NAMESTATUSITEMS            = 621;  // <Name Status Items>
       final int SYM_NEGATEEXP                  = 622;  // <Negate Exp>
       final int SYM_NEGATIONEXP                = 623;  // <Negation Exp>
       final int SYM_NEXTOPT                    = 624;  // <NEXT Opt>
       final int SYM_NUMBEROPT                  = 625;  // <NUMBER Opt>
       final int SYM_NUMERIC2                   = 626;  // <Numeric>
       final int SYM_OBJECTCLAUSE               = 627;  // <Object Clause>
       final int SYM_OBJECTCLAUSES              = 628;  // <Object Clauses>
       final int SYM_OBJECTCOMPUTER             = 629;  // <Object Computer>
       final int SYM_OBJECTCOMPUTERCLAUSEOPT    = 630;  // <Object Computer Clause Opt>
       final int SYM_OFOPT                      = 631;  // <OF Opt>
       final int SYM_ONOPT                      = 632;  // <ON Opt>
       final int SYM_OPENEMBED                  = 633;  // <Open Embed>
       final int SYM_OPENENTRY                  = 634;  // <Open Entry>
       final int SYM_OPENIMP                    = 635;  // <Open Imp>
       final int SYM_OPENLIST                   = 636;  // <Open List>
       final int SYM_OPENNOREWIND               = 637;  // <Open No Rewind>
       final int SYM_OPENSENT                   = 638;  // <Open Sent>
       final int SYM_OPENSTM                    = 639;  // <Open Stm>
       final int SYM_OPTIONALOPT                = 640;  // <Optional Opt>
       final int SYM_ORDEROPT                   = 641;  // <ORDER opt>
       final int SYM_ORGANIZATIONKIND           = 642;  // <Organization Kind>
       final int SYM_OVERFLOWCLAUSE             = 643;  // <Overflow Clause>
       final int SYM_OVERFLOWCLAUSES            = 644;  // <Overflow Clauses>
       final int SYM_PADDINGKIND                = 645;  // <Padding Kind>
       final int SYM_PARAGRAPH                  = 646;  // <Paragraph>
       final int SYM_PARAGRAPHS                 = 647;  // <Paragraphs>
       final int SYM_PERFORMBLOCK               = 648;  // <Perform Block>
       final int SYM_PERFORMEMBED               = 649;  // <Perform Embed>
       final int SYM_PERFORMFORLIST             = 650;  // <Perform For List>
       final int SYM_PERFORMFORRANGE            = 651;  // <Perform For Range>
       final int SYM_PERFORMIMP                 = 652;  // <Perform Imp>
       final int SYM_PERFORMLOOP                = 653;  // <Perform Loop>
       final int SYM_PERFORMSENT                = 654;  // <Perform Sent>
       final int SYM_PERFORMSTM                 = 655;  // <Perform Stm>
       final int SYM_PHRASE                     = 656;  // <Phrase>
       final int SYM_PHRASES                    = 657;  // <Phrases>
       final int SYM_PICTURE                    = 658;  // <Picture>
       final int SYM_POINTERCLAUSE              = 659;  // <Pointer Clause>
       final int SYM_PRINTINGOPT                = 660;  // <PRINTING Opt>
       final int SYM_PROCEDUREDIVISION          = 661;  // <Procedure Division>
       final int SYM_PROGID                     = 662;  // <Prog ID>
       final int SYM_PROGNAMEOPT                = 663;  // <Prog Name Opt>
       final int SYM_PROGRAM2                   = 664;  // <Program>
       final int SYM_PROGRAMINFOITEM            = 665;  // <Program Info Item>
       final int SYM_PROGRAMINFOITEMS           = 666;  // <Program Info Items>
       final int SYM_PROGRAMOPT                 = 667;  // <Program Opt>
       final int SYM_READEMBED                  = 668;  // <Read Embed>
       final int SYM_READIMP                    = 669;  // <Read Imp>
       final int SYM_READKEYOPT                 = 670;  // <Read Key Opt>
       final int SYM_READMSGCLAUSES             = 671;  // <Read Msg Clauses>
       final int SYM_READSENT                   = 672;  // <Read Sent>
       final int SYM_READSTM                    = 673;  // <Read Stm>
       final int SYM_RECORDDELIMITERKIND        = 674;  // <Record Delimiter Kind>
       final int SYM_RECORDENTRY                = 675;  // <Record Entry>
       final int SYM_RECORDENTRYBLOCK           = 676;  // <Record Entry Block>
       final int SYM_RECORDOPT                  = 677;  // <RECORD Opt>
       final int SYM_RECORDOPTION               = 678;  // <Record Option>
       final int SYM_RECORDOPTIONLIST           = 679;  // <Record Option List>
       final int SYM_REFERENCESOPT              = 680;  // <REFERENCES Opt>
       final int SYM_RELATIVEKEYOPT             = 681;  // <Relative Key Opt>
       final int SYM_RELEASEEMBED               = 682;  // <Release Embed>
       final int SYM_RELEASEIMP                 = 683;  // <Release Imp>
       final int SYM_RELEASESENT                = 684;  // <Release Sent>
       final int SYM_RELEASESTM                 = 685;  // <Release Stm>
       final int SYM_REMAINDEROPT               = 686;  // <Remainder Opt>
       final int SYM_REPLACECHAR                = 687;  // <Replace Char>
       final int SYM_REPLACECHARS               = 688;  // <Replace Chars>
       final int SYM_REPLACEITEM                = 689;  // <Replace Item>
       final int SYM_REPLACEITEMS               = 690;  // <Replace Items>
       final int SYM_REPLACINGITEM              = 691;  // <Replacing Item>
       final int SYM_REPLACINGITEMS             = 692;  // <Replacing Items>
       final int SYM_REPLACINGOPT               = 693;  // <Replacing Opt>
       final int SYM_REPLACINGTYPE              = 694;  // <Replacing Type>
       final int SYM_REPORTDESC                 = 695;  // <Report Desc>
       final int SYM_REPORTDESCLIST             = 696;  // <Report Desc List>
       final int SYM_REPORTENTRY                = 697;  // <Report Entry>
       final int SYM_REPORTENTRYBLOCK           = 698;  // <Report Entry Block>
       final int SYM_REPORTENTRYNEXTGROUP       = 699;  // <Report Entry Next Group>
       final int SYM_REPORTENTRYOPTION          = 700;  // <Report Entry Option>
       final int SYM_REPORTENTRYOPTIONS         = 701;  // <Report Entry Options>
       final int SYM_REPORTENTRYRESULTCLAUSE    = 702;  // <Report Entry Result Clause>
       final int SYM_REPORTENTRYTYPE            = 703;  // <Report Entry Type>
       final int SYM_REPORTHEADINGOPT           = 704;  // <Report Heading Opt>
       final int SYM_REPORTOPTION               = 705;  // <Report Option>
       final int SYM_REPORTOPTIONS              = 706;  // <Report Options>
       final int SYM_REPORTSECTION              = 707;  // <Report Section>
       final int SYM_RERUNCLAUSEOPT             = 708;  // <Rerun Clause Opt>
       final int SYM_RERUNITEM                  = 709;  // <Rerun Item>
       final int SYM_RERUNLIST                  = 710;  // <Rerun List>
       final int SYM_RETURNEMBED                = 711;  // <Return Embed>
       final int SYM_RETURNIMP                  = 712;  // <Return Imp>
       final int SYM_RETURNSENT                 = 713;  // <Return Sent>
       final int SYM_RETURNSTM                  = 714;  // <Return Stm>
       final int SYM_REWRITEEMBED               = 715;  // <Rewrite Embed>
       final int SYM_REWRITEIMP                 = 716;  // <Rewrite Imp>
       final int SYM_REWRITESENT                = 717;  // <Rewrite Sent>
       final int SYM_REWRITESTM                 = 718;  // <Rewrite Stm>
       final int SYM_RIGHTOPT                   = 719;  // <RIGHT Opt>
       final int SYM_ROUNDEDOPT                 = 720;  // <ROUNDED Opt>
       final int SYM_SAMEITEM                   = 721;  // <Same Item>
       final int SYM_SAMELIST                   = 722;  // <Same List>
       final int SYM_SAMESOURCE                 = 723;  // <Same Source>
       final int SYM_SCREENFIELD                = 724;  // <Screen Field>
       final int SYM_SCREENFIELDLIST            = 725;  // <Screen Field List>
       final int SYM_SCREENSECTION              = 726;  // <Screen Section>
       final int SYM_SEARCHEMBED                = 727;  // <Search Embed>
       final int SYM_SEARCHIMP                  = 728;  // <Search Imp>
       final int SYM_SEARCHSENT                 = 729;  // <Search Sent>
       final int SYM_SEARCHSTM                  = 730;  // <Search Stm>
       final int SYM_SELECTBLOCK                = 731;  // <Select Block>
       final int SYM_SELECTOPTLIST              = 732;  // <Select Opt List>
       final int SYM_SELECTOPTION               = 733;  // <Select Option>
       final int SYM_SELECTPARAGRAPH            = 734;  // <Select Paragraph>
       final int SYM_SENDADVANCE                = 735;  // <Send Advance>
       final int SYM_SENDEMBED                  = 736;  // <Send Embed>
       final int SYM_SENDIMP                    = 737;  // <Send Imp>
       final int SYM_SENDREPLACINGOPT           = 738;  // <Send Replacing Opt>
       final int SYM_SENDSENT                   = 739;  // <Send Sent>
       final int SYM_SENDSPEC                   = 740;  // <Send Spec>
       final int SYM_SENDSTM                    = 741;  // <Send Stm>
       final int SYM_SENDWITH                   = 742;  // <Send With>
       final int SYM_SENTSTM                    = 743;  // <Sent Stm>
       final int SYM_SENTENCE2                  = 744;  // <Sentence>
       final int SYM_SENTENCES                  = 745;  // <Sentences>
       final int SYM_SEPCHAROPTION              = 746;  // <Sep Char Option>
       final int SYM_SETEMBED                   = 747;  // <Set Embed>
       final int SYM_SETIMP                     = 748;  // <Set Imp>
       final int SYM_SETSENT                    = 749;  // <Set Sent>
       final int SYM_SETSTM                     = 750;  // <Set Stm>
       final int SYM_SETVALUE                   = 751;  // <Set Value>
       final int SYM_SIGNARGS                   = 752;  // <Sign Args>
       final int SYM_SIGNOPT                    = 753;  // <SIGN Opt>
       final int SYM_SIZECLAUSE                 = 754;  // <Size Clause>
       final int SYM_SIZECLAUSES                = 755;  // <Size Clauses>
       final int SYM_SIZEOPT                    = 756;  // <SIZE Opt>
       final int SYM_SORTDUPLICATESOPT          = 757;  // <Sort Duplicates Opt>
       final int SYM_SORTEMBED                  = 758;  // <Sort Embed>
       final int SYM_SORTIMP                    = 759;  // <Sort Imp>
       final int SYM_SORTKEY                    = 760;  // <Sort Key>
       final int SYM_SORTKEYS                   = 761;  // <Sort Keys>
       final int SYM_SORTSENT                   = 762;  // <Sort Sent>
       final int SYM_SORTSOURCE                 = 763;  // <Sort Source>
       final int SYM_SORTSTM                    = 764;  // <Sort Stm>
       final int SYM_SORTTARGET                 = 765;  // <Sort Target>
       final int SYM_SOURCECOMPUTER             = 766;  // <Source Computer>
       final int SYM_SOURCECOMPUTERCLAUSEOPT    = 767;  // <Source Computer Clause Opt>
       final int SYM_SOURCEDEBUGOPT             = 768;  // <Source Debug Opt>
       final int SYM_SPECIALNAMELIST            = 769;  // <Special Name List>
       final int SYM_SPECIALNAMES               = 770;  // <Special Names>
       final int SYM_SPECIALNAMESITEM           = 771;  // <Special Names Item>
       final int SYM_SPECIALRANGE               = 772;  // <Special Range>
       final int SYM_SPECIALRANGES              = 773;  // <Special Ranges>
       final int SYM_STANDARDOPT                = 774;  // <STANDARD Opt>
       final int SYM_STARTEMBED                 = 775;  // <Start Embed>
       final int SYM_STARTIMP                   = 776;  // <Start Imp>
       final int SYM_STARTKEYOPT                = 777;  // <Start Key Opt>
       final int SYM_STARTSENT                  = 778;  // <Start Sent>
       final int SYM_STARTSTM                   = 779;  // <Start Stm>
       final int SYM_STATUSOPT                  = 780;  // <STATUS Opt>
       final int SYM_STOPEMBED                  = 781;  // <Stop Embed>
       final int SYM_STOPIMP                    = 782;  // <Stop Imp>
       final int SYM_STOPSENT                   = 783;  // <Stop Sent>
       final int SYM_STOPSTM                    = 784;  // <Stop Stm>
       final int SYM_STRINGEMBED                = 785;  // <String Embed>
       final int SYM_STRINGIMP                  = 786;  // <String Imp>
       final int SYM_STRINGITEM                 = 787;  // <String Item>
       final int SYM_STRINGITEMS                = 788;  // <String Items>
       final int SYM_STRINGSENT                 = 789;  // <String Sent>
       final int SYM_STRINGSTM                  = 790;  // <String Stm>
       final int SYM_SUBJECT                    = 791;  // <Subject>
       final int SYM_SUBJECTS                   = 792;  // <Subjects>
       final int SYM_SUBSETS                    = 793;  // <Subsets>
       final int SYM_SUBTRACTEMBED              = 794;  // <Subtract Embed>
       final int SYM_SUBTRACTIMP                = 795;  // <Subtract Imp>
       final int SYM_SUBTRACTSENT               = 796;  // <Subtract Sent>
       final int SYM_SUBTRACTSTM                = 797;  // <Subtract Stm>
       final int SYM_SUPPRESSEMBED              = 798;  // <Suppress Embed>
       final int SYM_SUPPRESSIMP                = 799;  // <Suppress Imp>
       final int SYM_SUPPRESSSENT               = 800;  // <Suppress Sent>
       final int SYM_SUPPRESSSTM                = 801;  // <Suppress Stm>
       final int SYM_SYMBOLICCHARLIST           = 802;  // <Symbolic Char List>
       final int SYM_SYMBOLICCHARACTER          = 803;  // <Symbolic Character>
       final int SYM_SYMBOLICCHARACTERS         = 804;  // <Symbolic Characters>
       final int SYM_SYMBOLICOPT                = 805;  // <SYMBOLIC Opt>
       final int SYM_SYMBOLICVALUE              = 806;  // <Symbolic Value>
       final int SYM_TALLYITEM                  = 807;  // <Tally Item>
       final int SYM_TALLYITEMS                 = 808;  // <Tally Items>
       final int SYM_TALLYVARIABLE              = 809;  // <Tally Variable>
       final int SYM_TALLYVARIABLES             = 810;  // <Tally Variables>
       final int SYM_TAPEOPT                    = 811;  // <TAPE Opt>
       final int SYM_TERMINATEEMBED             = 812;  // <Terminate Embed>
       final int SYM_TERMINATEIMP               = 813;  // <Terminate Imp>
       final int SYM_TERMINATESENT              = 814;  // <Terminate Sent>
       final int SYM_TERMINATESTM               = 815;  // <Terminate Stm>
       final int SYM_THANOPT                    = 816;  // <THAN Opt>
       final int SYM_THENOPT                    = 817;  // <THEN Opt>
       final int SYM_THRU2                      = 818;  // <THRU>
       final int SYM_TIMESOPT                   = 819;  // <Times Opt>
       final int SYM_TOOPT                      = 820;  // <TO Opt>
       final int SYM_UNSTRINGDELIMITER          = 821;  // <Unstring Delimiter>
       final int SYM_UNSTRINGDELIMITERLIST      = 822;  // <Unstring Delimiter List>
       final int SYM_UNSTRINGEMBED              = 823;  // <Unstring Embed>
       final int SYM_UNSTRINGIMP                = 824;  // <Unstring Imp>
       final int SYM_UNSTRINGOPTION             = 825;  // <Unstring Option>
       final int SYM_UNSTRINGOPTIONS            = 826;  // <Unstring Options>
       final int SYM_UNSTRINGSENT               = 827;  // <Unstring Sent>
       final int SYM_UNSTRINGSTM                = 828;  // <Unstring Stm>
       final int SYM_USAGEARGS                  = 829;  // <Usage Args>
       final int SYM_USEACCESS                  = 830;  // <Use Access>
       final int SYM_USEDEBUG                   = 831;  // <Use Debug>
       final int SYM_USEEMBED                   = 832;  // <Use Embed>
       final int SYM_USEIMP                     = 833;  // <Use Imp>
       final int SYM_USEPROCTYPE                = 834;  // <Use Proc Type>
       final int SYM_USESENT                    = 835;  // <Use Sent>
       final int SYM_USESTM                     = 836;  // <Use Stm>
       final int SYM_USINGCLAUSEOPT             = 837;  // <Using Clause Opt>
       final int SYM_VALUE2                     = 838;  // <Value>
       final int SYM_VALUES2                    = 839;  // <Values>
       final int SYM_VARIABLE                   = 840;  // <Variable>
       final int SYM_VARIABLES                  = 841;  // <Variables>
       final int SYM_VARYINGOPT                 = 842;  // <Varying Opt>
       final int SYM_WHENCLAUSE                 = 843;  // <When Clause>
       final int SYM_WHENCLAUSES                = 844;  // <When Clauses>
       final int SYM_WHENOPT                    = 845;  // <WHEN Opt>
       final int SYM_WITHOPT                    = 846;  // <WITH Opt>
       final int SYM_WITHTEST                   = 847;  // <With Test>
       final int SYM_WORDITEM                   = 848;  // <Word Item>
       final int SYM_WORDLIST                   = 849;  // <Word List>
       final int SYM_WORKINGMINUSSTORAGESECTION = 850;  // <Working-Storage Section>
       final int SYM_WRITEADVANCE               = 851;  // <Write Advance>
       final int SYM_WRITEEMBED                 = 852;  // <Write Embed>
       final int SYM_WRITEIMP                   = 853;  // <Write Imp>
       final int SYM_WRITEOPTIONS               = 854;  // <Write Options>
       final int SYM_WRITESENT                  = 855;  // <Write Sent>
       final int SYM_WRITESTM                   = 856;  // <Write Stm>
	}

	// Symbolic constants naming the table indices of the grammar rules
    private interface RuleConstants
    {
       final int PROD_PROGRAM                                                              =   0;  // <Program> ::= <Identification Division> <Environment Division> <Data Division> <Procedure Division>
       final int PROD_ADVANCINGOPT_ADVANCING                                               =   1;  // <ADVANCING Opt> ::= ADVANCING
       final int PROD_ADVANCINGOPT                                                         =   2;  // <ADVANCING Opt> ::= 
       final int PROD_ALLOPT_ALL                                                           =   3;  // <ALL Opt> ::= ALL
       final int PROD_ALLOPT                                                               =   4;  // <ALL Opt> ::= 
       final int PROD_AREOPT_ARE                                                           =   5;  // <ARE Opt> ::= ARE
       final int PROD_AREOPT                                                               =   6;  // <ARE Opt> ::= 
       final int PROD_AREAOPT_AREA                                                         =   7;  // <AREA Opt> ::= AREA
       final int PROD_AREAOPT_AREAS                                                        =   8;  // <AREA Opt> ::= AREAS
       final int PROD_AREAOPT                                                              =   9;  // <AREA Opt> ::= 
       final int PROD_ATOPT_AT                                                             =  10;  // <AT Opt> ::= AT
       final int PROD_ATOPT                                                                =  11;  // <AT Opt> ::= 
       final int PROD_BYOPT_BY                                                             =  12;  // <BY Opt> ::= BY
       final int PROD_BYOPT                                                                =  13;  // <BY Opt> ::= 
       final int PROD_CHARACTERSOPT_CHARACTERS                                             =  14;  // <CHARACTERS Opt> ::= CHARACTERS
       final int PROD_CHARACTERSOPT                                                        =  15;  // <CHARACTERS Opt> ::= 
       final int PROD_COLLATINGOPT_COLLATING                                               =  16;  // <COLLATING Opt> ::= COLLATING
       final int PROD_COLLATINGOPT                                                         =  17;  // <COLLATING Opt> ::= 
       final int PROD_CONTAINSOPT_CONTAINS                                                 =  18;  // <CONTAINS Opt> ::= CONTAINS
       final int PROD_CONTAINSOPT                                                          =  19;  // <CONTAINS Opt> ::= 
       final int PROD_DATAOPT_DATA                                                         =  20;  // <DATA Opt> ::= DATA
       final int PROD_DATAOPT                                                              =  21;  // <DATA Opt> ::= 
       final int PROD_EVERYOPT_EVERY                                                       =  22;  // <EVERY Opt> ::= EVERY
       final int PROD_EVERYOPT                                                             =  23;  // <EVERY Opt> ::= 
       final int PROD_FILEOPT_FILE                                                         =  24;  // <FILE Opt> ::= FILE
       final int PROD_FILEOPT                                                              =  25;  // <FILE Opt> ::= 
       final int PROD_FINALOPT_FINAL                                                       =  26;  // <FINAL Opt> ::= FINAL
       final int PROD_FINALOPT                                                             =  27;  // <FINAL Opt> ::= 
       final int PROD_FOROPT_FOR                                                           =  28;  // <FOR Opt> ::= FOR
       final int PROD_FOROPT                                                               =  29;  // <FOR Opt> ::= 
       final int PROD_GLOBALOPT_GLOBAL                                                     =  30;  // <GLOBAL Opt> ::= GLOBAL
       final int PROD_GLOBALOPT                                                            =  31;  // <GLOBAL Opt> ::= 
       final int PROD_INOPT_IN                                                             =  32;  // <IN Opt> ::= IN
       final int PROD_INOPT                                                                =  33;  // <IN Opt> ::= 
       final int PROD_INDICATEOPT_INDICATE                                                 =  34;  // <INDICATE Opt> ::= INDICATE
       final int PROD_INDICATEOPT                                                          =  35;  // <INDICATE Opt> ::= 
       final int PROD_INITIALOPT_INITIAL                                                   =  36;  // <INITIAL Opt> ::= INITIAL
       final int PROD_INITIALOPT                                                           =  37;  // <INITIAL Opt> ::= 
       final int PROD_ISOPT_IS                                                             =  38;  // <IS Opt> ::= IS
       final int PROD_ISOPT                                                                =  39;  // <IS Opt> ::= 
       final int PROD_KEYOPT_KEY                                                           =  40;  // <KEY Opt> ::= KEY
       final int PROD_KEYOPT                                                               =  41;  // <KEY Opt> ::= 
       final int PROD_LINEOPT_LINE                                                         =  42;  // <LINE Opt> ::= LINE
       final int PROD_LINEOPT                                                              =  43;  // <LINE Opt> ::= 
       final int PROD_LINESOPT_LINE                                                        =  44;  // <LINES Opt> ::= LINE
       final int PROD_LINESOPT_LINES                                                       =  45;  // <LINES Opt> ::= LINES
       final int PROD_LINESOPT                                                             =  46;  // <LINES Opt> ::= 
       final int PROD_MESSAGEOPT_MESSAGE                                                   =  47;  // <MESSAGE opt> ::= MESSAGE
       final int PROD_MESSAGEOPT                                                           =  48;  // <MESSAGE opt> ::= 
       final int PROD_MODEOPT_MODE                                                         =  49;  // <MODE Opt> ::= MODE
       final int PROD_MODEOPT                                                              =  50;  // <MODE Opt> ::= 
       final int PROD_NEXTOPT_NEXT                                                         =  51;  // <NEXT Opt> ::= NEXT
       final int PROD_NEXTOPT                                                              =  52;  // <NEXT Opt> ::= 
       final int PROD_NUMBEROPT_NUMBER                                                     =  53;  // <NUMBER Opt> ::= NUMBER
       final int PROD_NUMBEROPT                                                            =  54;  // <NUMBER Opt> ::= 
       final int PROD_OFOPT_OF                                                             =  55;  // <OF Opt> ::= OF
       final int PROD_OFOPT                                                                =  56;  // <OF Opt> ::= 
       final int PROD_ONOPT_ON                                                             =  57;  // <ON Opt> ::= ON
       final int PROD_ONOPT                                                                =  58;  // <ON Opt> ::= 
       final int PROD_ORDEROPT_ORDER                                                       =  59;  // <ORDER opt> ::= ORDER
       final int PROD_ORDEROPT                                                             =  60;  // <ORDER opt> ::= 
       final int PROD_PRINTINGOPT_PRINTING                                                 =  61;  // <PRINTING Opt> ::= PRINTING
       final int PROD_PRINTINGOPT                                                          =  62;  // <PRINTING Opt> ::= 
       final int PROD_RECORDOPT_RECORD                                                     =  63;  // <RECORD Opt> ::= RECORD
       final int PROD_RECORDOPT                                                            =  64;  // <RECORD Opt> ::= 
       final int PROD_REFERENCESOPT_REFERENCES                                             =  65;  // <REFERENCES Opt> ::= REFERENCES
       final int PROD_REFERENCESOPT                                                        =  66;  // <REFERENCES Opt> ::= 
       final int PROD_RIGHTOPT_RIGHT                                                       =  67;  // <RIGHT Opt> ::= RIGHT
       final int PROD_RIGHTOPT                                                             =  68;  // <RIGHT Opt> ::= 
       final int PROD_ROUNDEDOPT_ROUNDED                                                   =  69;  // <ROUNDED Opt> ::= ROUNDED
       final int PROD_ROUNDEDOPT                                                           =  70;  // <ROUNDED Opt> ::= 
       final int PROD_STANDARDOPT_STANDARD                                                 =  71;  // <STANDARD Opt> ::= STANDARD
       final int PROD_STANDARDOPT                                                          =  72;  // <STANDARD Opt> ::= 
       final int PROD_SIGNOPT_SIGN                                                         =  73;  // <SIGN Opt> ::= SIGN
       final int PROD_SIGNOPT                                                              =  74;  // <SIGN Opt> ::= 
       final int PROD_SIZEOPT_SIZE                                                         =  75;  // <SIZE Opt> ::= SIZE
       final int PROD_SIZEOPT                                                              =  76;  // <SIZE Opt> ::= 
       final int PROD_STATUSOPT_STATUS                                                     =  77;  // <STATUS Opt> ::= STATUS
       final int PROD_STATUSOPT                                                            =  78;  // <STATUS Opt> ::= 
       final int PROD_SYMBOLICOPT_SYMBOLIC                                                 =  79;  // <SYMBOLIC Opt> ::= SYMBOLIC
       final int PROD_SYMBOLICOPT                                                          =  80;  // <SYMBOLIC Opt> ::= 
       final int PROD_TAPEOPT_TAPE                                                         =  81;  // <TAPE Opt> ::= TAPE
       final int PROD_TAPEOPT                                                              =  82;  // <TAPE Opt> ::= 
       final int PROD_THENOPT_THEN                                                         =  83;  // <THEN Opt> ::= THEN
       final int PROD_THENOPT                                                              =  84;  // <THEN Opt> ::= 
       final int PROD_THANOPT_THAN                                                         =  85;  // <THAN Opt> ::= THAN
       final int PROD_THANOPT                                                              =  86;  // <THAN Opt> ::= 
       final int PROD_TOOPT_TO                                                             =  87;  // <TO Opt> ::= TO
       final int PROD_TOOPT                                                                =  88;  // <TO Opt> ::= 
       final int PROD_WHENOPT_WHEN                                                         =  89;  // <WHEN Opt> ::= WHEN
       final int PROD_WHENOPT                                                              =  90;  // <WHEN Opt> ::= 
       final int PROD_WITHOPT_WITH                                                         =  91;  // <WITH Opt> ::= WITH
       final int PROD_WITHOPT                                                              =  92;  // <WITH Opt> ::= 
       final int PROD_THRU_THRU                                                            =  93;  // <THRU> ::= THRU
       final int PROD_THRU_THROUGH                                                         =  94;  // <THRU> ::= THROUGH
       final int PROD_ISAREOPT_IS                                                          =  95;  // <IS ARE Opt> ::= IS
       final int PROD_ISAREOPT_ARE                                                         =  96;  // <IS ARE Opt> ::= ARE
       final int PROD_ISAREOPT                                                             =  97;  // <IS ARE Opt> ::= 
       final int PROD_CORRESPONDING_CORRESPONDING                                          =  98;  // <CORRESPONDING> ::= CORRESPONDING
       final int PROD_CORRESPONDING_CORR                                                   =  99;  // <CORRESPONDING> ::= CORR
       final int PROD_GIVINGCLAUSEOPT_GIVING_IDENTIFIER                                    = 100;  // <Giving Clause Opt> ::= GIVING Identifier
       final int PROD_GIVINGCLAUSEOPT                                                      = 101;  // <Giving Clause Opt> ::= 
       final int PROD_POINTERCLAUSE_POINTER                                                = 102;  // <Pointer Clause> ::= <WITH Opt> POINTER <Variable>
       final int PROD_POINTERCLAUSE                                                        = 103;  // <Pointer Clause> ::= 
       final int PROD_FILENAME_IDENTIFIER                                                  = 104;  // <File Name> ::= Identifier
       final int PROD_FILENAME_STRINGLITERAL                                               = 105;  // <File Name> ::= StringLiteral
       final int PROD_FILENAMELIST                                                         = 106;  // <File Name List> ::= <File Name List> <File Name>
       final int PROD_FILENAMELIST2                                                        = 107;  // <File Name List> ::= <File Name>
       final int PROD_INTCONSTANT_IDENTIFIER                                               = 108;  // <Int Constant> ::= Identifier
       final int PROD_INTCONSTANT                                                          = 109;  // <Int Constant> ::= <Integer>
       final int PROD_INDEXCLAUSE_INDEXED_IDENTIFIER                                       = 110;  // <Index Clause> ::= INDEXED <BY Opt> Identifier
       final int PROD_INDEXCLAUSE                                                          = 111;  // <Index Clause> ::= 
       final int PROD_BEFOREAFTER_BEFORE                                                   = 112;  // <BEFORE AFTER> ::= BEFORE
       final int PROD_BEFOREAFTER_AFTER                                                    = 113;  // <BEFORE AFTER> ::= AFTER
       final int PROD_SYMBOLICVALUE                                                        = 114;  // <Symbolic Value> ::= <Literal>
       final int PROD_SYMBOLICVALUE2                                                       = 115;  // <Symbolic Value> ::= <Variable>
       final int PROD_SYMBOLICVALUE3                                                       = 116;  // <Symbolic Value> ::= <Figurative>
       final int PROD_VALUES                                                               = 117;  // <Values> ::= <Values> <Value>
       final int PROD_VALUES2                                                              = 118;  // <Values> ::= <Value>
       final int PROD_VALUE                                                                = 119;  // <Value> ::= <Literal>
       final int PROD_VALUE2                                                               = 120;  // <Value> ::= <Variable>
       final int PROD_NUMERIC                                                              = 121;  // <Numeric> ::= <Integer>
       final int PROD_NUMERIC_IDENTIFIER                                                   = 122;  // <Numeric> ::= Identifier
       final int PROD_LITERAL                                                              = 123;  // <Literal> ::= <Integer>
       final int PROD_LITERAL_FLOATLITERAL                                                 = 124;  // <Literal> ::= FloatLiteral
       final int PROD_LITERAL_STRINGLITERAL                                                = 125;  // <Literal> ::= StringLiteral
       final int PROD_LITERAL_QUOTE                                                        = 126;  // <Literal> ::= QUOTE
       final int PROD_LITERAL_QUOTES                                                       = 127;  // <Literal> ::= QUOTES
       final int PROD_INTEGER_INTLITERAL                                                   = 128;  // <Integer> ::= IntLiteral
       final int PROD_INTEGER_66                                                           = 129;  // <Integer> ::= '66'
       final int PROD_INTEGER_77                                                           = 130;  // <Integer> ::= '77'
       final int PROD_INTEGER_88                                                           = 131;  // <Integer> ::= '88'
       final int PROD_FIGURATIVE_ZERO                                                      = 132;  // <Figurative> ::= ZERO
       final int PROD_FIGURATIVE_ZEROS                                                     = 133;  // <Figurative> ::= ZEROS
       final int PROD_FIGURATIVE_ZEROES                                                    = 134;  // <Figurative> ::= ZEROES
       final int PROD_FIGURATIVE_SPACE                                                     = 135;  // <Figurative> ::= SPACE
       final int PROD_FIGURATIVE_SPACES                                                    = 136;  // <Figurative> ::= SPACES
       final int PROD_FIGURATIVE_HIGHMINUSVALUE                                            = 137;  // <Figurative> ::= 'HIGH-VALUE'
       final int PROD_FIGURATIVE_HIGHMINUSVALUES                                           = 138;  // <Figurative> ::= 'HIGH-VALUES'
       final int PROD_FIGURATIVE_LOWMINUSVALUE                                             = 139;  // <Figurative> ::= 'LOW-VALUE'
       final int PROD_FIGURATIVE_LOWMINUSVALUES                                            = 140;  // <Figurative> ::= 'LOW-VALUES'
       final int PROD_FIGURATIVE_ALL_STRINGLITERAL                                         = 141;  // <Figurative> ::= ALL StringLiteral
       final int PROD_FIGURATIVE_NULL                                                      = 142;  // <Figurative> ::= NULL
       final int PROD_FIGURATIVE_NULLS                                                     = 143;  // <Figurative> ::= NULLS
       final int PROD_IDENTIFIERS_IDENTIFIER                                               = 144;  // <Identifiers> ::= <Identifiers> Identifier
       final int PROD_IDENTIFIERS_IDENTIFIER2                                              = 145;  // <Identifiers> ::= Identifier
       final int PROD_VARIABLES_IDENTIFIER                                                 = 146;  // <Variables> ::= <Variables> Identifier
       final int PROD_VARIABLES_IDENTIFIER2                                                = 147;  // <Variables> ::= Identifier
       final int PROD_VARIABLE_IDENTIFIER                                                  = 148;  // <Variable> ::= Identifier
       final int PROD_VARIABLE_IDENTIFIER_LPAREN_RPAREN                                    = 149;  // <Variable> ::= Identifier '(' <Subsets> ')'
       final int PROD_SUBSETS_COLON                                                        = 150;  // <Subsets> ::= <Subsets> ':' <Numeric>
       final int PROD_SUBSETS                                                              = 151;  // <Subsets> ::= <Numeric>
       final int PROD_IDENTIFICATIONDIVISION_IDENTIFICATION_DIVISION_DOT                   = 152;  // <Identification Division> ::= IDENTIFICATION DIVISION '.' <Prog ID> <Program Info Items>
       final int PROD_PROGID_PROGRAMMINUSID_DOT_DOT                                        = 153;  // <Prog ID> ::= 'PROGRAM-ID' '.' <Word List> <Prog Name Opt> '.'
       final int PROD_PROGRAMINFOITEMS                                                     = 154;  // <Program Info Items> ::= <Program Info Items> <Program Info Item>
       final int PROD_PROGRAMINFOITEMS2                                                    = 155;  // <Program Info Items> ::= 
       final int PROD_PROGRAMINFOITEM_AUTHOR_DOT_DOT                                       = 156;  // <Program Info Item> ::= AUTHOR '.' <Word List> '.'
       final int PROD_PROGRAMINFOITEM_INSTALLATION_DOT_DOT                                 = 157;  // <Program Info Item> ::= INSTALLATION '.' <Word List> '.'
       final int PROD_PROGRAMINFOITEM_DATEMINUSWRITTEN_DOT_DOT                             = 158;  // <Program Info Item> ::= 'DATE-WRITTEN' '.' <Word List> '.'
       final int PROD_PROGRAMINFOITEM_DATEMINUSCOMPILED_DOT_DOT                            = 159;  // <Program Info Item> ::= 'DATE-COMPILED' '.' <Word List> '.'
       final int PROD_PROGRAMINFOITEM_SECURITY_DOT_DOT                                     = 160;  // <Program Info Item> ::= SECURITY '.' <Word List> '.'
       final int PROD_WORDLIST                                                             = 161;  // <Word List> ::= <Word List> <Word Item>
       final int PROD_WORDLIST2                                                            = 162;  // <Word List> ::= <Word Item>
       final int PROD_WORDITEM_IDENTIFIER                                                  = 163;  // <Word Item> ::= Identifier
       final int PROD_WORDITEM                                                             = 164;  // <Word Item> ::= <Integer>
       final int PROD_WORDITEM_FLOATLITERAL                                                = 165;  // <Word Item> ::= FloatLiteral
       final int PROD_WORDITEM_STRINGLITERAL                                               = 166;  // <Word Item> ::= StringLiteral
       final int PROD_WORDITEM_DIV                                                         = 167;  // <Word Item> ::= '/'
       final int PROD_WORDITEM_COMMA                                                       = 168;  // <Word Item> ::= ','
       final int PROD_PROGNAMEOPT                                                          = 169;  // <Prog Name Opt> ::= <IS Opt> <Common Initial> <Program Opt>
       final int PROD_PROGNAMEOPT2                                                         = 170;  // <Prog Name Opt> ::= 
       final int PROD_COMMONINITIAL_COMMON                                                 = 171;  // <Common Initial> ::= COMMON
       final int PROD_COMMONINITIAL_INITIAL                                                = 172;  // <Common Initial> ::= INITIAL
       final int PROD_PROGRAMOPT_PROGRAM                                                   = 173;  // <Program Opt> ::= PROGRAM
       final int PROD_PROGRAMOPT                                                           = 174;  // <Program Opt> ::= 
       final int PROD_ENVIRONMENTDIVISION_ENVIRONMENT_DIVISION_DOT                         = 175;  // <Environment Division> ::= ENVIRONMENT DIVISION '.' <Config Section> <Input-Output Section>
       final int PROD_ENVIRONMENTDIVISION                                                  = 176;  // <Environment Division> ::= 
       final int PROD_CONFIGSECTION_CONFIGURATION_SECTION_DOT                              = 177;  // <Config Section> ::= CONFIGURATION SECTION '.' <Config Section Items>
       final int PROD_CONFIGSECTION                                                        = 178;  // <Config Section> ::= 
       final int PROD_CONFIGSECTIONITEMS                                                   = 179;  // <Config Section Items> ::= <Config Section Items> <Config Section Item>
       final int PROD_CONFIGSECTIONITEMS2                                                  = 180;  // <Config Section Items> ::= 
       final int PROD_CONFIGSECTIONITEM                                                    = 181;  // <Config Section Item> ::= <Source Computer>
       final int PROD_CONFIGSECTIONITEM2                                                   = 182;  // <Config Section Item> ::= <Object Computer>
       final int PROD_CONFIGSECTIONITEM3                                                   = 183;  // <Config Section Item> ::= <Special Names>
       final int PROD_SOURCECOMPUTER_SOURCEMINUSCOMPUTER_DOT                               = 184;  // <Source Computer> ::= 'SOURCE-COMPUTER' '.' <Source Computer Clause Opt>
       final int PROD_SOURCECOMPUTERCLAUSEOPT_IDENTIFIER_DOT                               = 185;  // <Source Computer Clause Opt> ::= Identifier <Source Debug Opt> '.'
       final int PROD_SOURCECOMPUTERCLAUSEOPT                                              = 186;  // <Source Computer Clause Opt> ::= 
       final int PROD_SOURCEDEBUGOPT_DEBUGGING_MODE                                        = 187;  // <Source Debug Opt> ::= <WITH Opt> DEBUGGING MODE
       final int PROD_OBJECTCOMPUTER_OBJECTMINUSCOMPUTER_DOT                               = 188;  // <Object Computer> ::= 'OBJECT-COMPUTER' '.' <Object Computer Clause Opt>
       final int PROD_OBJECTCOMPUTERCLAUSEOPT_IDENTIFIER_DOT                               = 189;  // <Object Computer Clause Opt> ::= Identifier <Object Clauses> '.'
       final int PROD_OBJECTCOMPUTERCLAUSEOPT                                              = 190;  // <Object Computer Clause Opt> ::= 
       final int PROD_OBJECTCLAUSES                                                        = 191;  // <Object Clauses> ::= <Object Clause> <Object Clauses>
       final int PROD_OBJECTCLAUSES2                                                       = 192;  // <Object Clauses> ::= 
       final int PROD_OBJECTCLAUSE_SEQUENCE_IDENTIFIER                                     = 193;  // <Object Clause> ::= <Program Opt> <COLLATING Opt> SEQUENCE <IS Opt> Identifier
       final int PROD_OBJECTCLAUSE_SEGMENTMINUSLIMIT                                       = 194;  // <Object Clause> ::= 'SEGMENT-LIMIT' <IS Opt> <Integer>
       final int PROD_SPECIALNAMES_SPECIALMINUSNAMES_DOT                                   = 195;  // <Special Names> ::= 'SPECIAL-NAMES' '.' <Special Name List>
       final int PROD_SPECIALNAMELIST                                                      = 196;  // <Special Name List> ::= <Special Name List> <Special Names Item>
       final int PROD_SPECIALNAMELIST2                                                     = 197;  // <Special Name List> ::= 
       final int PROD_SPECIALNAMESITEM_IDENTIFIER_IDENTIFIER_DOT                           = 198;  // <Special Names Item> ::= Identifier <IS Opt> Identifier <Name Status Items> '.'
       final int PROD_SPECIALNAMESITEM_IDENTIFIER_DOT                                      = 199;  // <Special Names Item> ::= Identifier <Name Status Items> '.'
       final int PROD_SPECIALNAMESITEM_SYMBOLIC_DOT                                        = 200;  // <Special Names Item> ::= SYMBOLIC <CHARACTERS Opt> <Symbolic Char List> '.'
       final int PROD_SPECIALNAMESITEM_ALPHABET_IDENTIFIER_DOT                             = 201;  // <Special Names Item> ::= ALPHABET Identifier <IS Opt> <Alphabet Item> '.'
       final int PROD_SPECIALNAMESITEM_CLASS_IDENTIFIER_DOT                                = 202;  // <Special Names Item> ::= CLASS Identifier <IS Opt> <Special Ranges> '.'
       final int PROD_SPECIALNAMESITEM_CURRENCY_DOT                                        = 203;  // <Special Names Item> ::= CURRENCY <SIGN Opt> <IS Opt> <Literal> '.'
       final int PROD_SPECIALNAMESITEM_DECIMALMINUSPOINT_COMMA_DOT                         = 204;  // <Special Names Item> ::= 'DECIMAL-POINT' <IS Opt> COMMA '.'
       final int PROD_NAMESTATUSITEMS                                                      = 205;  // <Name Status Items> ::= <Name Status Items> <Name Status Item>
       final int PROD_NAMESTATUSITEMS2                                                     = 206;  // <Name Status Items> ::= 
       final int PROD_NAMESTATUSITEM_ON_IDENTIFIER                                         = 207;  // <Name Status Item> ::= ON <STATUS Opt> <IS Opt> Identifier
       final int PROD_NAMESTATUSITEM_OFF_IDENTIFIER                                        = 208;  // <Name Status Item> ::= OFF <STATUS Opt> <IS Opt> Identifier
       final int PROD_ALPHABETITEM_STANDARDMINUS1                                          = 209;  // <Alphabet Item> ::= 'STANDARD-1'
       final int PROD_ALPHABETITEM_STANDARDMINUS2                                          = 210;  // <Alphabet Item> ::= 'STANDARD-2'
       final int PROD_ALPHABETITEM_NATIVE                                                  = 211;  // <Alphabet Item> ::= NATIVE
       final int PROD_ALPHABETITEM_IDENTIFIER                                              = 212;  // <Alphabet Item> ::= Identifier
       final int PROD_ALPHABETITEM                                                         = 213;  // <Alphabet Item> ::= <Special Ranges>
       final int PROD_SPECIALRANGES_ALSO                                                   = 214;  // <Special Ranges> ::= <Special Ranges> ALSO <Special Range>
       final int PROD_SPECIALRANGES                                                        = 215;  // <Special Ranges> ::= <Special Range>
       final int PROD_SPECIALRANGE                                                         = 216;  // <Special Range> ::= <Literal> <THRU> <Literal>
       final int PROD_SYMBOLICCHARLIST                                                     = 217;  // <Symbolic Char List> ::= <Symbolic Characters>
       final int PROD_SYMBOLICCHARLIST_IN_IDENTIFIER                                       = 218;  // <Symbolic Char List> ::= <Symbolic Characters> IN Identifier
       final int PROD_SYMBOLICCHARACTERS                                                   = 219;  // <Symbolic Characters> ::= <Symbolic Character> <Symbolic Value>
       final int PROD_SYMBOLICCHARACTERS2                                                  = 220;  // <Symbolic Characters> ::= <Symbolic Character>
       final int PROD_SYMBOLICCHARACTER_IDENTIFIER                                         = 221;  // <Symbolic Character> ::= Identifier <IS ARE Opt> <Literal>
       final int PROD_INPUTOUTPUTSECTION_INPUTMINUSOUTPUT_SECTION_DOT                      = 222;  // <Input-Output Section> ::= 'INPUT-OUTPUT' SECTION '.' <File-Control> <I-O-Control>
       final int PROD_INPUTOUTPUTSECTION                                                   = 223;  // <Input-Output Section> ::= 
       final int PROD_FILECONTROL_FILEMINUSCONTROL_DOT                                     = 224;  // <File-Control> ::= 'FILE-CONTROL' '.' <Select Block>
       final int PROD_FILECONTROL                                                          = 225;  // <File-Control> ::= 
       final int PROD_SELECTBLOCK                                                          = 226;  // <Select Block> ::= <Select Block> <Select Paragraph>
       final int PROD_SELECTBLOCK2                                                         = 227;  // <Select Block> ::= 
       final int PROD_SELECTPARAGRAPH_SELECT_IDENTIFIER_ASSIGN_IDENTIFIER_DOT              = 228;  // <Select Paragraph> ::= SELECT <Optional Opt> Identifier ASSIGN <TO Opt> Identifier <Select Opt List> '.'
       final int PROD_OPTIONALOPT_OPTIONAL                                                 = 229;  // <Optional Opt> ::= OPTIONAL
       final int PROD_OPTIONALOPT                                                          = 230;  // <Optional Opt> ::= 
       final int PROD_SELECTOPTLIST                                                        = 231;  // <Select Opt List> ::= <Select Option> <Select Opt List>
       final int PROD_SELECTOPTLIST2                                                       = 232;  // <Select Opt List> ::= 
       final int PROD_SELECTOPTION_RESERVE                                                 = 233;  // <Select Option> ::= RESERVE <Integer> <AREA Opt>
       final int PROD_SELECTOPTION_ORGANIZATION                                            = 234;  // <Select Option> ::= ORGANIZATION <IS Opt> <Organization Kind>
       final int PROD_SELECTOPTION                                                         = 235;  // <Select Option> ::= <Organization Kind>
       final int PROD_SELECTOPTION_PADDING                                                 = 236;  // <Select Option> ::= PADDING <Character Opt> <IS Opt> <Padding Kind>
       final int PROD_SELECTOPTION_RECORD_DELIMITER                                        = 237;  // <Select Option> ::= RECORD DELIMITER <IS Opt> <Record Delimiter Kind>
       final int PROD_SELECTOPTION_RECORD_IDENTIFIER                                       = 238;  // <Select Option> ::= RECORD <KEY Opt> <IS Opt> Identifier
       final int PROD_SELECTOPTION_ALTERNATIVE_RECORD_IDENTIFIER                           = 239;  // <Select Option> ::= ALTERNATIVE RECORD <KEY Opt> <IS Opt> Identifier <Duplicates Clause Opt>
       final int PROD_SELECTOPTION_ACCESS                                                  = 240;  // <Select Option> ::= ACCESS <MODE Opt> <IS Opt> <Access Mode>
       final int PROD_SELECTOPTION_STATUS_IDENTIFIER                                       = 241;  // <Select Option> ::= <FILE Opt> STATUS <IS Opt> Identifier
       final int PROD_ACCESSMODE_SEQUENTIAL                                                = 242;  // <Access Mode> ::= SEQUENTIAL
       final int PROD_ACCESSMODE_RANDOM                                                    = 243;  // <Access Mode> ::= RANDOM
       final int PROD_ACCESSMODE_DYNAMIC                                                   = 244;  // <Access Mode> ::= DYNAMIC
       final int PROD_ORGANIZATIONKIND_SEQUENTIAL                                          = 245;  // <Organization Kind> ::= SEQUENTIAL
       final int PROD_ORGANIZATIONKIND_LINE_SEQUENTIAL                                     = 246;  // <Organization Kind> ::= LINE SEQUENTIAL
       final int PROD_ORGANIZATIONKIND_RELATIVE                                            = 247;  // <Organization Kind> ::= RELATIVE <Relative Key Opt>
       final int PROD_ORGANIZATIONKIND_INDEXED                                             = 248;  // <Organization Kind> ::= INDEXED
       final int PROD_RELATIVEKEYOPT_IDENTIFIER                                            = 249;  // <Relative Key Opt> ::= <KEY Opt> <IS Opt> Identifier
       final int PROD_RELATIVEKEYOPT                                                       = 250;  // <Relative Key Opt> ::= 
       final int PROD_RECORDDELIMITERKIND_STANDARDMINUS1                                   = 251;  // <Record Delimiter Kind> ::= 'STANDARD-1'
       final int PROD_RECORDDELIMITERKIND_IDENTIFIER                                       = 252;  // <Record Delimiter Kind> ::= Identifier
       final int PROD_PADDINGKIND_IDENTIFIER                                               = 253;  // <Padding Kind> ::= Identifier
       final int PROD_PADDINGKIND                                                          = 254;  // <Padding Kind> ::= <Literal>
       final int PROD_DUPLICATESCLAUSEOPT_DUPLICATES                                       = 255;  // <Duplicates Clause Opt> ::= <WITH Opt> DUPLICATES
       final int PROD_DUPLICATESCLAUSEOPT                                                  = 256;  // <Duplicates Clause Opt> ::= 
       final int PROD_IOCONTROL_IMINUSOMINUSCONTROL_DOT_DOT                                = 257;  // <I-O-Control> ::= 'I-O-CONTROL' '.' <Rerun List> <Same List> <Multiple List> '.'
       final int PROD_RERUNLIST                                                            = 258;  // <Rerun List> ::= <Rerun List> <Rerun Item>
       final int PROD_RERUNLIST2                                                           = 259;  // <Rerun List> ::= 
       final int PROD_RERUNITEM_RERUN                                                      = 260;  // <Rerun Item> ::= RERUN <Rerun Clause Opt> <EVERY Opt> <Every Clause>
       final int PROD_RERUNCLAUSEOPT_ON                                                    = 261;  // <Rerun Clause Opt> ::= ON <File Name>
       final int PROD_RERUNCLAUSEOPT                                                       = 262;  // <Rerun Clause Opt> ::= 
       final int PROD_EVERYCLAUSE                                                          = 263;  // <Every Clause> ::= <End Of Opt> <Every End Target> <OF Opt> <File Name>
       final int PROD_EVERYCLAUSE_RECORDS                                                  = 264;  // <Every Clause> ::= <Integer> RECORDS
       final int PROD_EVERYCLAUSE_CLOCKMINUSUNITS                                          = 265;  // <Every Clause> ::= <Integer> 'CLOCK-UNITS'
       final int PROD_EVERYCLAUSE_IDENTIFIER                                               = 266;  // <Every Clause> ::= Identifier
       final int PROD_ENDOFOPT_END                                                         = 267;  // <End Of Opt> ::= END <OF Opt>
       final int PROD_ENDOFOPT                                                             = 268;  // <End Of Opt> ::= 
       final int PROD_EVERYENDTARGET_REEL                                                  = 269;  // <Every End Target> ::= REEL
       final int PROD_EVERYENDTARGET_UNIT                                                  = 270;  // <Every End Target> ::= UNIT
       final int PROD_SAMELIST                                                             = 271;  // <Same List> ::= <Same List> <Same Item>
       final int PROD_SAMELIST2                                                            = 272;  // <Same List> ::= 
       final int PROD_SAMEITEM_SAME                                                        = 273;  // <Same Item> ::= SAME <Same Source> <AREA Opt> <File Name> <File Name List>
       final int PROD_SAMESOURCE_RECORD                                                    = 274;  // <Same Source> ::= RECORD
       final int PROD_SAMESOURCE_SORT                                                      = 275;  // <Same Source> ::= SORT
       final int PROD_SAMESOURCE_SORTMINUSMERGE                                            = 276;  // <Same Source> ::= 'SORT-MERGE'
       final int PROD_MULTIPLELIST                                                         = 277;  // <Multiple List> ::= <Multiple List> <Multiple Item>
       final int PROD_MULTIPLELIST2                                                        = 278;  // <Multiple List> ::= 
       final int PROD_MULTIPLEITEM_MULTIPLE_FILE                                           = 279;  // <Multiple Item> ::= MULTIPLE FILE <TAPE Opt> <CONTAINS Opt> <Contain List>
       final int PROD_CONTAINLIST                                                          = 280;  // <Contain List> ::= <Contain List> <Contain Item>
       final int PROD_CONTAINLIST2                                                         = 281;  // <Contain List> ::= <Contain Item>
       final int PROD_CONTAINITEM_POSITION                                                 = 282;  // <Contain Item> ::= <File Name> POSITION <IS Opt> <Integer>
       final int PROD_CONTAINITEM                                                          = 283;  // <Contain Item> ::= <File Name>
       final int PROD_DATADIVISION_DATA_DIVISION_DOT                                       = 284;  // <Data Division> ::= DATA DIVISION '.' <Data Section List>
       final int PROD_DATASECTIONLIST                                                      = 285;  // <Data Section List> ::= <Data Section Entry> <Data Section List>
       final int PROD_DATASECTIONLIST2                                                     = 286;  // <Data Section List> ::= 
       final int PROD_DATASECTIONENTRY                                                     = 287;  // <Data Section Entry> ::= <File Section>
       final int PROD_DATASECTIONENTRY2                                                    = 288;  // <Data Section Entry> ::= <Working-Storage Section>
       final int PROD_DATASECTIONENTRY3                                                    = 289;  // <Data Section Entry> ::= <Linkage Section>
       final int PROD_DATASECTIONENTRY4                                                    = 290;  // <Data Section Entry> ::= <Screen Section>
       final int PROD_DATASECTIONENTRY5                                                    = 291;  // <Data Section Entry> ::= <Communication Section>
       final int PROD_DATASECTIONENTRY6                                                    = 292;  // <Data Section Entry> ::= <Report Section>
       final int PROD_RECORDENTRYBLOCK                                                     = 293;  // <Record Entry Block> ::= <Record Entry Block> <Record Entry>
       final int PROD_RECORDENTRYBLOCK2                                                    = 294;  // <Record Entry Block> ::= 
       final int PROD_RECORDENTRY_INTLITERAL_DOT                                           = 295;  // <Record Entry> ::= IntLiteral <Level Name> <Record Option List> '.'
       final int PROD_RECORDENTRY_66_RENAMES_DOT                                           = 296;  // <Record Entry> ::= '66' <Level Name> RENAMES <Identifier Range> '.'
       final int PROD_RECORDENTRY_77_DOT                                                   = 297;  // <Record Entry> ::= '77' <Level Name> <Record Option List> '.'
       final int PROD_RECORDENTRY_88_VALUES_DOT                                            = 298;  // <Record Entry> ::= '88' <Level Name> VALUES <IS ARE Opt> <Values> '.'
       final int PROD_LEVELNAME_IDENTIFIER                                                 = 299;  // <Level Name> ::= Identifier
       final int PROD_LEVELNAME_FILLER                                                     = 300;  // <Level Name> ::= FILLER
       final int PROD_TIMESOPT_TIMES                                                       = 301;  // <Times Opt> ::= TIMES
       final int PROD_TIMESOPT                                                             = 302;  // <Times Opt> ::= 
       final int PROD_RECORDOPTIONLIST                                                     = 303;  // <Record Option List> ::= <Record Option List> <Record Option>
       final int PROD_RECORDOPTIONLIST2                                                    = 304;  // <Record Option List> ::= 
       final int PROD_RECORDOPTION_REDEFINES_IDENTIFIER                                    = 305;  // <Record Option> ::= REDEFINES Identifier
       final int PROD_RECORDOPTION_EXTERNAL                                                = 306;  // <Record Option> ::= <IS Opt> EXTERNAL
       final int PROD_RECORDOPTION_INTERNAL                                                = 307;  // <Record Option> ::= <IS Opt> INTERNAL
       final int PROD_RECORDOPTION                                                         = 308;  // <Record Option> ::= <Picture>
       final int PROD_RECORDOPTION_USAGE                                                   = 309;  // <Record Option> ::= USAGE <IS Opt> <Usage Args>
       final int PROD_RECORDOPTION2                                                        = 310;  // <Record Option> ::= <Usage Args>
       final int PROD_RECORDOPTION_SIGN                                                    = 311;  // <Record Option> ::= SIGN <IS Opt> <Sign Args> <Sep Char Option>
       final int PROD_RECORDOPTION_OCCURS                                                  = 312;  // <Record Option> ::= OCCURS <Numeric> <Times Opt> <Key Clause> <Index Clause>
       final int PROD_RECORDOPTION_OCCURS_TO_DEPENDING_IDENTIFIER                          = 313;  // <Record Option> ::= OCCURS <Numeric> TO <Numeric> <Times Opt> DEPENDING <ON Opt> Identifier <Key Clause> <Index Clause>
       final int PROD_RECORDOPTION_SYNCHRONIZED                                            = 314;  // <Record Option> ::= SYNCHRONIZED <Left Right Opt>
       final int PROD_RECORDOPTION_SYNC                                                    = 315;  // <Record Option> ::= SYNC <Left Right Opt>
       final int PROD_RECORDOPTION_JUSTIFIED                                               = 316;  // <Record Option> ::= JUSTIFIED <RIGHT Opt>
       final int PROD_RECORDOPTION_JUST                                                    = 317;  // <Record Option> ::= JUST <RIGHT Opt>
       final int PROD_RECORDOPTION_BLANK_ZERO                                              = 318;  // <Record Option> ::= BLANK <WHEN Opt> ZERO
       final int PROD_RECORDOPTION_VALUE                                                   = 319;  // <Record Option> ::= VALUE <IS Opt> <Symbolic Value>
       final int PROD_PICTURE_PICSTRING                                                    = 320;  // <Picture> ::= PicString
       final int PROD_KEYCLAUSE_ASCENDING                                                  = 321;  // <Key Clause> ::= ASCENDING <KEY Opt> <IS Opt> <Identifiers>
       final int PROD_KEYCLAUSE_DESCENDING                                                 = 322;  // <Key Clause> ::= DESCENDING <KEY Opt> <IS Opt> <Identifiers>
       final int PROD_KEYCLAUSE                                                            = 323;  // <Key Clause> ::= 
       final int PROD_USAGEARGS_BINARY                                                     = 324;  // <Usage Args> ::= BINARY
       final int PROD_USAGEARGS_COMPUTATIONAL                                              = 325;  // <Usage Args> ::= COMPUTATIONAL
       final int PROD_USAGEARGS_COMP                                                       = 326;  // <Usage Args> ::= COMP
       final int PROD_USAGEARGS_DISPLAY                                                    = 327;  // <Usage Args> ::= DISPLAY
       final int PROD_USAGEARGS_INDEX                                                      = 328;  // <Usage Args> ::= INDEX
       final int PROD_USAGEARGS_PACKEDMINUSDECIMAL                                         = 329;  // <Usage Args> ::= 'PACKED-DECIMAL'
       final int PROD_SIGNARGS_LEADING                                                     = 330;  // <Sign Args> ::= LEADING
       final int PROD_SIGNARGS_TRAILING                                                    = 331;  // <Sign Args> ::= TRAILING
       final int PROD_SEPCHAROPTION_SEPARATE                                               = 332;  // <Sep Char Option> ::= SEPARATE <Character Opt>
       final int PROD_SEPCHAROPTION                                                        = 333;  // <Sep Char Option> ::= 
       final int PROD_CHARACTEROPT_CHARACTER                                               = 334;  // <Character Opt> ::= CHARACTER
       final int PROD_CHARACTEROPT                                                         = 335;  // <Character Opt> ::= 
       final int PROD_LEFTRIGHTOPT_LEFT                                                    = 336;  // <Left Right Opt> ::= LEFT
       final int PROD_LEFTRIGHTOPT_RIGHT                                                   = 337;  // <Left Right Opt> ::= RIGHT
       final int PROD_LEFTRIGHTOPT                                                         = 338;  // <Left Right Opt> ::= 
       final int PROD_FILESECTION_FILE_SECTION_DOT                                         = 339;  // <File Section> ::= FILE SECTION '.' <File Desc Block>
       final int PROD_FILEDESCBLOCK                                                        = 340;  // <File Desc Block> ::= <File Desc Entry> <File Desc Block>
       final int PROD_FILEDESCBLOCK2                                                       = 341;  // <File Desc Block> ::= 
       final int PROD_FILEDESCENTRY_FD_IDENTIFIER_DOT                                      = 342;  // <File Desc Entry> ::= FD Identifier <File Option List> '.' <Record Entry Block>
       final int PROD_FILEDESCENTRY_SD_IDENTIFIER_DOT                                      = 343;  // <File Desc Entry> ::= SD Identifier <File Option List> '.' <Record Entry Block>
       final int PROD_FILEOPTIONLIST                                                       = 344;  // <File Option List> ::= <File Option List> <File Option>
       final int PROD_FILEOPTIONLIST2                                                      = 345;  // <File Option List> ::= 
       final int PROD_FILEOPTION                                                           = 346;  // <File Option> ::= <File Is Option>
       final int PROD_FILEOPTION2                                                          = 347;  // <File Option> ::= <File Block Option>
       final int PROD_FILEOPTION3                                                          = 348;  // <File Option> ::= <File Record Option>
       final int PROD_FILEOPTION4                                                          = 349;  // <File Option> ::= <File Label Option>
       final int PROD_FILEOPTION5                                                          = 350;  // <File Option> ::= <File Value Option>
       final int PROD_FILEOPTION6                                                          = 351;  // <File Option> ::= <File Data Option>
       final int PROD_FILEOPTION7                                                          = 352;  // <File Option> ::= <File Linage Option>
       final int PROD_FILEOPTION8                                                          = 353;  // <File Option> ::= <File Code-Set Option>
       final int PROD_FILEISOPTION_EXTERNAL                                                = 354;  // <File Is Option> ::= <IS Opt> EXTERNAL
       final int PROD_FILEISOPTION_INTERNAL                                                = 355;  // <File Is Option> ::= <IS Opt> INTERNAL
       final int PROD_FILEBLOCKOPTION_BLOCK                                                = 356;  // <File Block Option> ::= BLOCK <CONTAINS Opt> <Numeric> <File Block Units>
       final int PROD_FILEBLOCKOPTION_BLOCK_TO                                             = 357;  // <File Block Option> ::= BLOCK <CONTAINS Opt> <Numeric> TO <Numeric> <File Block Units>
       final int PROD_FILEBLOCKUNITS_RECORDS                                               = 358;  // <File Block Units> ::= RECORDS
       final int PROD_FILEBLOCKUNITS_CHARACTERS                                            = 359;  // <File Block Units> ::= CHARACTERS
       final int PROD_FILEBLOCKUNITS                                                       = 360;  // <File Block Units> ::= 
       final int PROD_FILERECORDOPTION_RECORD                                              = 361;  // <File Record Option> ::= RECORD <CONTAINS Opt> <Numeric> <CHARACTERS Opt>
       final int PROD_FILERECORDOPTION_RECORD_TO                                           = 362;  // <File Record Option> ::= RECORD <CONTAINS Opt> <Numeric> TO <Numeric> <CHARACTERS Opt>
       final int PROD_FILERECORDOPTION_RECORD_VARYING                                      = 363;  // <File Record Option> ::= RECORD <IS Opt> VARYING <IN Opt> <SIZE Opt> <File Record Size Clause> <File Record Depending Clause>
       final int PROD_FILERECORDSIZECLAUSE_FROM                                            = 364;  // <File Record Size Clause> ::= FROM <Numeric> <CHARACTERS Opt>
       final int PROD_FILERECORDSIZECLAUSE_TO                                              = 365;  // <File Record Size Clause> ::= TO <Numeric> <CHARACTERS Opt>
       final int PROD_FILERECORDSIZECLAUSE_FROM_TO                                         = 366;  // <File Record Size Clause> ::= FROM <Numeric> TO <Numeric> <CHARACTERS Opt>
       final int PROD_FILERECORDSIZECLAUSE                                                 = 367;  // <File Record Size Clause> ::= 
       final int PROD_FILERECORDDEPENDINGCLAUSE_DEPENDING_IDENTIFIER                       = 368;  // <File Record Depending Clause> ::= DEPENDING <ON Opt> Identifier
       final int PROD_FILERECORDDEPENDINGCLAUSE                                            = 369;  // <File Record Depending Clause> ::= 
       final int PROD_FILELABELOPTION_LABEL_RECORD                                         = 370;  // <File Label Option> ::= LABEL RECORD <IS Opt> <File Label Type>
       final int PROD_FILELABELOPTION_LABEL_RECORDS                                        = 371;  // <File Label Option> ::= LABEL RECORDS <IS Opt> <File Label Type>
       final int PROD_FILELABELTYPE_STANDARD                                               = 372;  // <File Label Type> ::= STANDARD
       final int PROD_FILELABELTYPE_OMITTED                                                = 373;  // <File Label Type> ::= OMITTED
       final int PROD_FILEVALUEOPTION_VALUE_OF                                             = 374;  // <File Value Option> ::= VALUE OF <File Value List>
       final int PROD_FILEVALUELIST                                                        = 375;  // <File Value List> ::= <File Value List> <File Value Item>
       final int PROD_FILEVALUELIST2                                                       = 376;  // <File Value List> ::= <File Value Item>
       final int PROD_FILEVALUEITEM_IDENTIFIER_IDENTIFIER                                  = 377;  // <File Value Item> ::= Identifier <IS Opt> Identifier
       final int PROD_FILEVALUEITEM_IDENTIFIER                                             = 378;  // <File Value Item> ::= Identifier <IS Opt> <Literal>
       final int PROD_FILEDATAOPTION_DATA_RECORD                                           = 379;  // <File Data Option> ::= DATA RECORD <IS Opt> <Identifiers>
       final int PROD_FILEDATAOPTION_DATA_RECORDS                                          = 380;  // <File Data Option> ::= DATA RECORDS <ARE Opt> <Identifiers>
       final int PROD_FILELINAGEOPTION_LINAGE                                              = 381;  // <File Linage Option> ::= LINAGE <IS Opt> <Int Constant> <LINES Opt> <File Linage Footing> <File Linage Top> <File Linage Bottom>
       final int PROD_FILELINAGEFOOTING_FOOTING                                            = 382;  // <File Linage Footing> ::= <WITH Opt> FOOTING <AT Opt> <Int Constant>
       final int PROD_FILELINAGETOP_TOP                                                    = 383;  // <File Linage Top> ::= <LINES Opt> <AT Opt> TOP <Int Constant>
       final int PROD_FILELINAGEBOTTOM_BOTTOM                                              = 384;  // <File Linage Bottom> ::= <LINES Opt> <AT Opt> BOTTOM <Int Constant>
       final int PROD_FILECODESETOPTION_CODEMINUSSET_IDENTIFIER                            = 385;  // <File Code-Set Option> ::= 'CODE-SET' <IS Opt> Identifier
       final int PROD_WORKINGSTORAGESECTION_WORKINGMINUSSTORAGE_SECTION_DOT                = 386;  // <Working-Storage Section> ::= 'WORKING-STORAGE' SECTION '.' <Record Entry Block>
       final int PROD_LINKAGESECTION_LINKAGE_SECTION_DOT                                   = 387;  // <Linkage Section> ::= LINKAGE SECTION '.' <Record Entry Block>
       final int PROD_SCREENSECTION_SCREEN_SECTION_DOT                                     = 388;  // <Screen Section> ::= SCREEN SECTION '.' <Screen Field List>
       final int PROD_SCREENFIELDLIST                                                      = 389;  // <Screen Field List> ::= <Screen Field> <Screen Field List>
       final int PROD_SCREENFIELDLIST2                                                     = 390;  // <Screen Field List> ::= <Screen Field>
       final int PROD_SCREENFIELD_DOT                                                      = 391;  // <Screen Field> ::= <Integer> <Field Name opt> <Field Def List> '.'
       final int PROD_FIELDNAMEOPT_IDENTIFIER                                              = 392;  // <Field Name opt> ::= Identifier
       final int PROD_FIELDNAMEOPT                                                         = 393;  // <Field Name opt> ::= 
       final int PROD_FIELDDEFLIST                                                         = 394;  // <Field Def List> ::= <Field Def List> <Field Def Item>
       final int PROD_FIELDDEFLIST2                                                        = 395;  // <Field Def List> ::= 
       final int PROD_FIELDDEFITEM_LINE                                                    = 396;  // <Field Def Item> ::= LINE <Numeric>
       final int PROD_FIELDDEFITEM_COLUMN                                                  = 397;  // <Field Def Item> ::= COLUMN <Numeric>
       final int PROD_FIELDDEFITEM_FOREGROUNDMINUSCOLOR                                    = 398;  // <Field Def Item> ::= 'FOREGROUND-COLOR' <Numeric>
       final int PROD_FIELDDEFITEM_BACKGROUNDMINUSCOLOR                                    = 399;  // <Field Def Item> ::= 'BACKGROUND-COLOR' <Numeric>
       final int PROD_FIELDDEFITEM_VALUE                                                   = 400;  // <Field Def Item> ::= VALUE <IS Opt> <Symbolic Value>
       final int PROD_FIELDDEFITEM                                                         = 401;  // <Field Def Item> ::= <Picture>
       final int PROD_FIELDDEFITEM_FROM_IDENTIFIER                                         = 402;  // <Field Def Item> ::= FROM Identifier
       final int PROD_FIELDDEFITEM_USING_IDENTIFIER                                        = 403;  // <Field Def Item> ::= USING Identifier
       final int PROD_FIELDDEFITEM_HIGHLIGHT                                               = 404;  // <Field Def Item> ::= HIGHLIGHT
       final int PROD_FIELDDEFITEM_REVERSEMINUSVIDEO                                       = 405;  // <Field Def Item> ::= 'REVERSE-VIDEO'
       final int PROD_FIELDDEFITEM_BLINK                                                   = 406;  // <Field Def Item> ::= BLINK
       final int PROD_FIELDDEFITEM_UNDERLINE                                               = 407;  // <Field Def Item> ::= UNDERLINE
       final int PROD_FIELDDEFITEM_BLANK_SCREEN                                            = 408;  // <Field Def Item> ::= BLANK SCREEN
       final int PROD_COMMUNICATIONSECTION_COMMUNICATION_SECTION_DOT                       = 409;  // <Communication Section> ::= COMMUNICATION SECTION '.' <Comm Desc List>
       final int PROD_COMMDESCLIST                                                         = 410;  // <Comm Desc List> ::= <Comm Desc List> <Comm Desc>
       final int PROD_COMMDESCLIST2                                                        = 411;  // <Comm Desc List> ::= 
       final int PROD_COMMDESC_CD_IDENTIFIER_INPUT_DOT                                     = 412;  // <Comm Desc> ::= CD Identifier <FOR Opt> <INITIAL Opt> INPUT <Comm Input Body> '.' <Record Entry Block>
       final int PROD_COMMDESC_CD_IDENTIFIER_OUTPUT_DOT                                    = 413;  // <Comm Desc> ::= CD Identifier <FOR Opt> OUTPUT <Comm Output Options> '.' <Record Entry Block>
       final int PROD_COMMDESC_CD_IDENTIFIER_IMINUSO_DOT                                   = 414;  // <Comm Desc> ::= CD Identifier <FOR Opt> <INITIAL Opt> 'I-O' <Comm I-O Body> '.' <Record Entry Block>
       final int PROD_COMMINPUTBODY                                                        = 415;  // <Comm Input Body> ::= <Identifiers>
       final int PROD_COMMINPUTBODY2                                                       = 416;  // <Comm Input Body> ::= <Comm Input Options>
       final int PROD_COMMINPUTOPTIONS                                                     = 417;  // <Comm Input Options> ::= <Comm Input Options> <Comm Input Option>
       final int PROD_COMMINPUTOPTIONS2                                                    = 418;  // <Comm Input Options> ::= 
       final int PROD_COMMINPUTOPTION_QUEUE_IDENTIFIER                                     = 419;  // <Comm Input Option> ::= <SYMBOLIC Opt> QUEUE <IS Opt> Identifier
       final int PROD_COMMINPUTOPTION_SUBMINUSQUEUEMINUS1_IDENTIFIER                       = 420;  // <Comm Input Option> ::= <SYMBOLIC Opt> 'SUB-QUEUE-1' <IS Opt> Identifier
       final int PROD_COMMINPUTOPTION_SUBMINUSQUEUEMINUS2_IDENTIFIER                       = 421;  // <Comm Input Option> ::= <SYMBOLIC Opt> 'SUB-QUEUE-2' <IS Opt> Identifier
       final int PROD_COMMINPUTOPTION_SUBMINUSQUEUEMINUS3_IDENTIFIER                       = 422;  // <Comm Input Option> ::= <SYMBOLIC Opt> 'SUB-QUEUE-3' <IS Opt> Identifier
       final int PROD_COMMINPUTOPTION_MESSAGE_DATE_IDENTIFIER                              = 423;  // <Comm Input Option> ::= MESSAGE DATE <IS Opt> Identifier
       final int PROD_COMMINPUTOPTION_MESSAGE_TIME_IDENTIFIER                              = 424;  // <Comm Input Option> ::= MESSAGE TIME <IS Opt> Identifier
       final int PROD_COMMINPUTOPTION_SOURCE_IDENTIFIER                                    = 425;  // <Comm Input Option> ::= <SYMBOLIC Opt> SOURCE <IS Opt> Identifier
       final int PROD_COMMINPUTOPTION_TEXT_LENGTH                                          = 426;  // <Comm Input Option> ::= TEXT LENGTH <IS Opt> <Numeric>
       final int PROD_COMMINPUTOPTION_END_KEY_IDENTIFIER                                   = 427;  // <Comm Input Option> ::= END KEY <IS Opt> Identifier
       final int PROD_COMMINPUTOPTION_STATUS_KEY_IDENTIFIER                                = 428;  // <Comm Input Option> ::= STATUS KEY <IS Opt> Identifier
       final int PROD_COMMINPUTOPTION_MESSAGE_COUNT_IDENTIFIER                             = 429;  // <Comm Input Option> ::= MESSAGE COUNT <IS Opt> Identifier
       final int PROD_COMMOUTPUTOPTIONS                                                    = 430;  // <Comm Output Options> ::= <Comm Output Options> <Comm Output Option>
       final int PROD_COMMOUTPUTOPTIONS2                                                   = 431;  // <Comm Output Options> ::= 
       final int PROD_COMMOUTPUTOPTION_DESTINATION_TABLES_OCCURS                           = 432;  // <Comm Output Option> ::= DESTINATION TABLES OCCURS <Numeric> <Times Opt> <Index Clause>
       final int PROD_COMMOUTPUTOPTION_DESTINATION_COUNT_IDENTIFIER                        = 433;  // <Comm Output Option> ::= DESTINATION COUNT <IS Opt> Identifier
       final int PROD_COMMOUTPUTOPTION_TEXT_LENGTH_IDENTIFIER                              = 434;  // <Comm Output Option> ::= TEXT LENGTH <IS Opt> Identifier
       final int PROD_COMMOUTPUTOPTION_STATUS_KEY_IDENTIFIER                               = 435;  // <Comm Output Option> ::= STATUS KEY <IS Opt> Identifier
       final int PROD_COMMOUTPUTOPTION_ERROR_KEY_IDENTIFIER                                = 436;  // <Comm Output Option> ::= ERROR KEY <IS Opt> Identifier
       final int PROD_COMMOUTPUTOPTION_SYMBOLIC_DESTINATION_IDENTIFIER                     = 437;  // <Comm Output Option> ::= SYMBOLIC DESTINATION <IS Opt> Identifier
       final int PROD_COMMOUTPUTOPTION_DESTINATION_IDENTIFIER                              = 438;  // <Comm Output Option> ::= DESTINATION <IS Opt> Identifier
       final int PROD_COMMIOBODY                                                           = 439;  // <Comm I-O Body> ::= <Identifiers>
       final int PROD_COMMIOBODY2                                                          = 440;  // <Comm I-O Body> ::= <Comm I-O Options>
       final int PROD_COMMIOOPTIONS                                                        = 441;  // <Comm I-O Options> ::= <Comm I-O Options> <Comm I-O Option>
       final int PROD_COMMIOOPTIONS2                                                       = 442;  // <Comm I-O Options> ::= 
       final int PROD_COMMIOOPTION_MESSAGE_DATE_IDENTIFIER                                 = 443;  // <Comm I-O Option> ::= MESSAGE DATE <IS Opt> Identifier
       final int PROD_COMMIOOPTION_MESSAGE_TIME_IDENTIFIER                                 = 444;  // <Comm I-O Option> ::= MESSAGE TIME <IS Opt> Identifier
       final int PROD_COMMIOOPTION_TERMINAL_IDENTIFIER                                     = 445;  // <Comm I-O Option> ::= <SYMBOLIC Opt> TERMINAL <IS Opt> Identifier
       final int PROD_COMMIOOPTION_TEXT_LENGTH                                             = 446;  // <Comm I-O Option> ::= TEXT LENGTH <IS Opt> <Numeric>
       final int PROD_COMMIOOPTION_END_KEY_IDENTIFIER                                      = 447;  // <Comm I-O Option> ::= END KEY <IS Opt> Identifier
       final int PROD_COMMIOOPTION_STATUS_KEY_IDENTIFIER                                   = 448;  // <Comm I-O Option> ::= STATUS KEY <IS Opt> Identifier
       final int PROD_REPORTSECTION_REPORT_SECTION_DOT                                     = 449;  // <Report Section> ::= REPORT SECTION '.' <Report Desc List>
       final int PROD_REPORTDESCLIST                                                       = 450;  // <Report Desc List> ::= <Report Desc List> <Report Desc>
       final int PROD_REPORTDESCLIST2                                                      = 451;  // <Report Desc List> ::= 
       final int PROD_REPORTDESC_RD_IDENTIFIER                                             = 452;  // <Report Desc> ::= RD Identifier <Report Options> <Report Entry Block>
       final int PROD_REPORTOPTIONS                                                        = 453;  // <Report Options> ::= <Report Options> <Report Option>
       final int PROD_REPORTOPTIONS2                                                       = 454;  // <Report Options> ::= 
       final int PROD_REPORTOPTION_GLOBAL                                                  = 455;  // <Report Option> ::= <IS Opt> GLOBAL
       final int PROD_REPORTOPTION_CODE                                                    = 456;  // <Report Option> ::= CODE <Literal>
       final int PROD_REPORTOPTION                                                         = 457;  // <Report Option> ::= <CONTROL IS> <FINAL Opt> <Identifiers>
       final int PROD_REPORTOPTION_PAGE                                                    = 458;  // <Report Option> ::= PAGE <LIMITS IS Opt> <Numeric> <LINES Opt> <Report Heading Opt>
       final int PROD_CONTROLIS_CONTROL_IS                                                 = 459;  // <CONTROL IS> ::= CONTROL IS
       final int PROD_CONTROLIS_CONTROLS_ARE                                               = 460;  // <CONTROL IS> ::= CONTROLS ARE
       final int PROD_LIMITSISOPT_LIMIT_IS                                                 = 461;  // <LIMITS IS Opt> ::= LIMIT IS
       final int PROD_LIMITSISOPT_LIMITS_ARE                                               = 462;  // <LIMITS IS Opt> ::= LIMITS ARE
       final int PROD_LIMITSISOPT                                                          = 463;  // <LIMITS IS Opt> ::= 
       final int PROD_REPORTHEADINGOPT_HEADING                                             = 464;  // <Report Heading Opt> ::= HEADING <Integer>
       final int PROD_REPORTHEADINGOPT                                                     = 465;  // <Report Heading Opt> ::= 
       final int PROD_REPORTENTRYBLOCK                                                     = 466;  // <Report Entry Block> ::= <Report Entry Block> <Report Entry>
       final int PROD_REPORTENTRYBLOCK2                                                    = 467;  // <Report Entry Block> ::= 
       final int PROD_REPORTENTRY_IDENTIFIER_DOT                                           = 468;  // <Report Entry> ::= <Integer> Identifier <Report Entry Options> '.'
       final int PROD_REPORTENTRYOPTIONS                                                   = 469;  // <Report Entry Options> ::= <Report Entry Option> <Report Entry Options>
       final int PROD_REPORTENTRYOPTIONS2                                                  = 470;  // <Report Entry Options> ::= 
       final int PROD_REPORTENTRYOPTION_LINE                                               = 471;  // <Report Entry Option> ::= LINE <NUMBER Opt> <IS Opt> <Numeric>
       final int PROD_REPORTENTRYOPTION_LINE_ON_NEXT_PAGE                                  = 472;  // <Report Entry Option> ::= LINE <NUMBER Opt> <IS Opt> <Numeric> ON NEXT PAGE
       final int PROD_REPORTENTRYOPTION_LINE_PLUS                                          = 473;  // <Report Entry Option> ::= LINE <NUMBER Opt> <IS Opt> PLUS <Numeric>
       final int PROD_REPORTENTRYOPTION_NEXT_GROUP                                         = 474;  // <Report Entry Option> ::= NEXT GROUP <IS Opt> <Report Entry Next Group>
       final int PROD_REPORTENTRYOPTION_TYPE                                               = 475;  // <Report Entry Option> ::= TYPE <IS Opt> <Report Entry Type>
       final int PROD_REPORTENTRYOPTION_USAGE_DISPLAY                                      = 476;  // <Report Entry Option> ::= USAGE <IS Opt> DISPLAY
       final int PROD_REPORTENTRYOPTION_DISPLAY                                            = 477;  // <Report Entry Option> ::= DISPLAY
       final int PROD_REPORTENTRYOPTION                                                    = 478;  // <Report Entry Option> ::= <Picture>
       final int PROD_REPORTENTRYOPTION_SIGN                                               = 479;  // <Report Entry Option> ::= SIGN <IS Opt> <Sign Args> <Sep Char Option>
       final int PROD_REPORTENTRYOPTION_JUSTIFIED                                          = 480;  // <Report Entry Option> ::= JUSTIFIED <RIGHT Opt>
       final int PROD_REPORTENTRYOPTION_JUST                                               = 481;  // <Report Entry Option> ::= JUST <RIGHT Opt>
       final int PROD_REPORTENTRYOPTION_BLANK_ZERO                                         = 482;  // <Report Entry Option> ::= BLANK <WHEN Opt> ZERO
       final int PROD_REPORTENTRYOPTION_COLUMN                                             = 483;  // <Report Entry Option> ::= COLUMN <NUMBER Opt> <IS Opt> <Numeric>
       final int PROD_REPORTENTRYOPTION_SOURCE                                             = 484;  // <Report Entry Option> ::= SOURCE <IS Opt> <Numeric>
       final int PROD_REPORTENTRYOPTION_VALUE                                              = 485;  // <Report Entry Option> ::= VALUE <IS Opt> <Symbolic Value>
       final int PROD_REPORTENTRYOPTION_SUM                                                = 486;  // <Report Entry Option> ::= SUM <Identifiers>
       final int PROD_REPORTENTRYOPTION_SUM_UPON                                           = 487;  // <Report Entry Option> ::= SUM <Identifiers> UPON <Identifiers> <Report Entry Result Clause>
       final int PROD_REPORTENTRYOPTION_GROUP                                              = 488;  // <Report Entry Option> ::= GROUP <INDICATE Opt>
       final int PROD_REPORTENTRYNEXTGROUP                                                 = 489;  // <Report Entry Next Group> ::= <Numeric>
       final int PROD_REPORTENTRYNEXTGROUP_PLUS                                            = 490;  // <Report Entry Next Group> ::= PLUS <Numeric>
       final int PROD_REPORTENTRYNEXTGROUP_NEXT_PAGE                                       = 491;  // <Report Entry Next Group> ::= NEXT PAGE
       final int PROD_REPORTENTRYTYPE_REPORT_HEADING                                       = 492;  // <Report Entry Type> ::= REPORT HEADING
       final int PROD_REPORTENTRYTYPE_RH                                                   = 493;  // <Report Entry Type> ::= RH
       final int PROD_REPORTENTRYTYPE_PAGE_HEADING                                         = 494;  // <Report Entry Type> ::= PAGE HEADING
       final int PROD_REPORTENTRYTYPE_PH                                                   = 495;  // <Report Entry Type> ::= PH
       final int PROD_REPORTENTRYTYPE_CONTROL_HEADING                                      = 496;  // <Report Entry Type> ::= CONTROL HEADING
       final int PROD_REPORTENTRYTYPE_CH                                                   = 497;  // <Report Entry Type> ::= CH
       final int PROD_REPORTENTRYTYPE_DETAIL                                               = 498;  // <Report Entry Type> ::= DETAIL
       final int PROD_REPORTENTRYTYPE_DE                                                   = 499;  // <Report Entry Type> ::= DE
       final int PROD_REPORTENTRYTYPE_CONTROL_FOOTING                                      = 500;  // <Report Entry Type> ::= CONTROL FOOTING
       final int PROD_REPORTENTRYTYPE_CF                                                   = 501;  // <Report Entry Type> ::= CF
       final int PROD_REPORTENTRYTYPE_PAGE_FOOTING                                         = 502;  // <Report Entry Type> ::= PAGE FOOTING
       final int PROD_REPORTENTRYTYPE_PF                                                   = 503;  // <Report Entry Type> ::= PF
       final int PROD_REPORTENTRYTYPE_REPORT_FOOTING                                       = 504;  // <Report Entry Type> ::= REPORT FOOTING
       final int PROD_REPORTENTRYTYPE_RF                                                   = 505;  // <Report Entry Type> ::= RF
       final int PROD_REPORTENTRYRESULTCLAUSE_RESET_IDENTIFIER                             = 506;  // <Report Entry Result Clause> ::= RESET <ON Opt> Identifier
       final int PROD_REPORTENTRYRESULTCLAUSE_RESET_FINAL                                  = 507;  // <Report Entry Result Clause> ::= RESET <ON Opt> FINAL
       final int PROD_REPORTENTRYRESULTCLAUSE                                              = 508;  // <Report Entry Result Clause> ::= 
       final int PROD_PROCEDUREDIVISION_PROCEDURE_DIVISION_DOT                             = 509;  // <Procedure Division> ::= PROCEDURE DIVISION <Using Clause Opt> <Declarative Block> '.' <Paragraphs>
       final int PROD_USINGCLAUSEOPT_USING                                                 = 510;  // <Using Clause Opt> ::= USING <Identifiers>
       final int PROD_USINGCLAUSEOPT                                                       = 511;  // <Using Clause Opt> ::= 
       final int PROD_PARAGRAPHS                                                           = 512;  // <Paragraphs> ::= <Paragraphs> <Paragraph>
       final int PROD_PARAGRAPHS2                                                          = 513;  // <Paragraphs> ::= 
       final int PROD_PARAGRAPH_IDENTIFIER_SECTION_DOT_IDENTIFIER_DOT                      = 514;  // <Paragraph> ::= Identifier SECTION '.' Identifier '.' <Sentences>
       final int PROD_PARAGRAPH_IDENTIFIER_DOT                                             = 515;  // <Paragraph> ::= Identifier '.' <Sentences>
       final int PROD_DECLARATIVEBLOCK_DECLARATIVES_DOT_END_DECLARATIVES_DOT               = 516;  // <Declarative Block> ::= DECLARATIVES '.' <Declarative Sections> END DECLARATIVES '.'
       final int PROD_DECLARATIVEBLOCK                                                     = 517;  // <Declarative Block> ::= 
       final int PROD_DECLARATIVESECTIONS                                                  = 518;  // <Declarative Sections> ::= <Declarative Sections> <Declarative Section>
       final int PROD_DECLARATIVESECTIONS2                                                 = 519;  // <Declarative Sections> ::= 
       final int PROD_DECLARATIVESECTION_IDENTIFIER_SECTION_DOT_USE_ERROR_PROCEDURE_ON_DOT = 520;  // <Declarative Section> ::= Identifier SECTION '.' USE <BEFORE AFTER> <STANDARD Opt> ERROR PROCEDURE ON <Error Cause> '.' <Sentences>
       final int PROD_DECLARATIVESECTION_IDENTIFIER_DOT                                    = 521;  // <Declarative Section> ::= Identifier '.' <Sentences>
       final int PROD_ERRORCAUSE_IDENTIFIER                                                = 522;  // <Error Cause> ::= Identifier
       final int PROD_ERRORCAUSE_INPUT                                                     = 523;  // <Error Cause> ::= INPUT
       final int PROD_ERRORCAUSE_OUTPUT                                                    = 524;  // <Error Cause> ::= OUTPUT
       final int PROD_ERRORCAUSE_IMINUSO                                                   = 525;  // <Error Cause> ::= 'I-O'
       final int PROD_ERRORCAUSE_EXTEND                                                    = 526;  // <Error Cause> ::= EXTEND
       final int PROD_SORTKEYS                                                             = 527;  // <Sort Keys> ::= <Sort Keys> <Sort Key>
       final int PROD_SORTKEYS2                                                            = 528;  // <Sort Keys> ::= <Sort Key>
       final int PROD_SORTKEY_ASCENDING_IDENTIFIER                                         = 529;  // <Sort Key> ::= <ON Opt> ASCENDING <KEY Opt> Identifier
       final int PROD_SORTKEY_DESCENDING_IDENTIFIER                                        = 530;  // <Sort Key> ::= <ON Opt> DESCENDING <KEY Opt> Identifier
       final int PROD_COLLATINGCLAUSE_SEQUENCE_IDENTIFIER                                  = 531;  // <Collating Clause> ::= <COLLATING Opt> SEQUENCE <IS Opt> Identifier
       final int PROD_COLLATINGCLAUSE                                                      = 532;  // <Collating Clause> ::= 
       final int PROD_SORTSOURCE_INPUT_PROCEDURE                                           = 533;  // <Sort Source> ::= INPUT PROCEDURE <IS Opt> <Identifier Range>
       final int PROD_SORTSOURCE_USING                                                     = 534;  // <Sort Source> ::= USING <Values>
       final int PROD_SORTTARGET_OUTPUT_PROCEDURE                                          = 535;  // <Sort Target> ::= OUTPUT PROCEDURE <IS Opt> <Identifier Range>
       final int PROD_SORTTARGET_GIVING                                                    = 536;  // <Sort Target> ::= GIVING <Values>
       final int PROD_IDENTIFIERRANGE_IDENTIFIER                                           = 537;  // <Identifier Range> ::= Identifier
       final int PROD_IDENTIFIERRANGE_IDENTIFIER_THRU_IDENTIFIER                           = 538;  // <Identifier Range> ::= Identifier THRU Identifier
       final int PROD_IDENTIFIERRANGE_IDENTIFIER_THROUGH_IDENTIFIER                        = 539;  // <Identifier Range> ::= Identifier THROUGH Identifier
       final int PROD_ENABLEDDISABLEMODE_INPUT                                             = 540;  // <Enabled Disable Mode> ::= INPUT
       final int PROD_ENABLEDDISABLEMODE_INPUT_TERMINAL                                    = 541;  // <Enabled Disable Mode> ::= INPUT TERMINAL
       final int PROD_ENABLEDDISABLEMODE_IMINUSO_TERMINAL                                  = 542;  // <Enabled Disable Mode> ::= 'I-O' TERMINAL
       final int PROD_ENABLEDDISABLEMODE_OUTPUT                                            = 543;  // <Enabled Disable Mode> ::= OUTPUT
       final int PROD_ENABLEDISABLEKEY_KEY                                                 = 544;  // <Enable Disable Key> ::= <WITH Opt> KEY <Value>
       final int PROD_ENABLEDISABLEKEY                                                     = 545;  // <Enable Disable Key> ::= 
       final int PROD_BOOLEANEXP_OR                                                        = 546;  // <Boolean Exp> ::= <Boolean Exp> OR <And Exp>
       final int PROD_BOOLEANEXP                                                           = 547;  // <Boolean Exp> ::= <And Exp>
       final int PROD_ANDEXP_AND                                                           = 548;  // <And Exp> ::= <Negation Exp> And <And Exp>
       final int PROD_ANDEXP                                                               = 549;  // <And Exp> ::= <Negation Exp>
       final int PROD_NEGATIONEXP                                                          = 550;  // <Negation Exp> ::= <Compare Exp>
       final int PROD_NEGATIONEXP_NOT                                                      = 551;  // <Negation Exp> ::= NOT <Compare Exp>
       final int PROD_COMPAREEXP                                                           = 552;  // <Compare Exp> ::= <Symbolic Value> <Compare Op> <Symbolic Value>
       final int PROD_COMPAREEXP_IS_ALPHABETIC                                             = 553;  // <Compare Exp> ::= <Symbolic Value> IS ALPHABETIC
       final int PROD_COMPAREEXP_IS_ALPHABETICMINUSUPPER                                   = 554;  // <Compare Exp> ::= <Symbolic Value> IS 'ALPHABETIC-UPPER'
       final int PROD_COMPAREEXP_IS_ALPHABETICMINUSLOWER                                   = 555;  // <Compare Exp> ::= <Symbolic Value> IS 'ALPHABETIC-LOWER'
       final int PROD_COMPAREEXP_LPAREN_RPAREN                                             = 556;  // <Compare Exp> ::= '(' <Boolean Exp> ')'
       final int PROD_COMPAREEXP2                                                          = 557;  // <Compare Exp> ::= <Symbolic Value>
       final int PROD_COMPAREOP                                                            = 558;  // <Compare Op> ::= <IS ARE Opt> <Greater Op>
       final int PROD_COMPAREOP_NOT                                                        = 559;  // <Compare Op> ::= <IS ARE Opt> NOT <Greater Op>
       final int PROD_COMPAREOP2                                                           = 560;  // <Compare Op> ::= <IS ARE Opt> <Greater Eq Op>
       final int PROD_COMPAREOP_NOT2                                                       = 561;  // <Compare Op> ::= <IS ARE Opt> NOT <Greater Eq Op>
       final int PROD_COMPAREOP3                                                           = 562;  // <Compare Op> ::= <IS ARE Opt> <Less Op>
       final int PROD_COMPAREOP_NOT3                                                       = 563;  // <Compare Op> ::= <IS ARE Opt> NOT <Less Op>
       final int PROD_COMPAREOP4                                                           = 564;  // <Compare Op> ::= <IS ARE Opt> <Less Eq Op>
       final int PROD_COMPAREOP_NOT4                                                       = 565;  // <Compare Op> ::= <IS ARE Opt> NOT <Less Eq Op>
       final int PROD_COMPAREOP5                                                           = 566;  // <Compare Op> ::= <IS ARE Opt> <Equal Op>
       final int PROD_COMPAREOP_NOT5                                                       = 567;  // <Compare Op> ::= <IS ARE Opt> NOT <Equal Op>
       final int PROD_GREATEROP_GREATER                                                    = 568;  // <Greater Op> ::= GREATER <THAN Opt>
       final int PROD_GREATEROP_GT                                                         = 569;  // <Greater Op> ::= '>'
       final int PROD_GREATEREQOP_GREATER_OR_EQUAL                                         = 570;  // <Greater Eq Op> ::= GREATER <THAN Opt> OR EQUAL <TO Opt>
       final int PROD_GREATEREQOP_GTEQ                                                     = 571;  // <Greater Eq Op> ::= '>='
       final int PROD_LESSOP_LESS                                                          = 572;  // <Less Op> ::= LESS <THAN Opt>
       final int PROD_LESSOP_LT                                                            = 573;  // <Less Op> ::= '<'
       final int PROD_LESSEQOP_LESS_OR_EQUAL                                               = 574;  // <Less Eq Op> ::= LESS <THAN Opt> OR EQUAL <TO Opt>
       final int PROD_LESSEQOP_LTEQ                                                        = 575;  // <Less Eq Op> ::= '<='
       final int PROD_EQUALOP_EQUAL                                                        = 576;  // <Equal Op> ::= EQUAL <TO Opt>
       final int PROD_EQUALOP_EQ                                                           = 577;  // <Equal Op> ::= '='
       final int PROD_SENTENCES                                                            = 578;  // <Sentences> ::= <Sentence> <Sentences>
       final int PROD_SENTENCES2                                                           = 579;  // <Sentences> ::= 
       final int PROD_SENTENCE_DOT                                                         = 580;  // <Sentence> ::= <Sent Stm> '.'
       final int PROD_SENTSTM                                                              = 581;  // <Sent Stm> ::= <Accept Sent>
       final int PROD_SENTSTM2                                                             = 582;  // <Sent Stm> ::= <Add Sent>
       final int PROD_SENTSTM3                                                             = 583;  // <Sent Stm> ::= <Alter Sent>
       final int PROD_SENTSTM4                                                             = 584;  // <Sent Stm> ::= <Call Sent>
       final int PROD_SENTSTM5                                                             = 585;  // <Sent Stm> ::= <Cancel Sent>
       final int PROD_SENTSTM6                                                             = 586;  // <Sent Stm> ::= <Close Sent>
       final int PROD_SENTSTM7                                                             = 587;  // <Sent Stm> ::= <Compute Sent>
       final int PROD_SENTSTM8                                                             = 588;  // <Sent Stm> ::= <Continue Sent>
       final int PROD_SENTSTM9                                                             = 589;  // <Sent Stm> ::= <Delete Sent>
       final int PROD_SENTSTM10                                                            = 590;  // <Sent Stm> ::= <Disable Sent>
       final int PROD_SENTSTM11                                                            = 591;  // <Sent Stm> ::= <Display Sent>
       final int PROD_SENTSTM12                                                            = 592;  // <Sent Stm> ::= <Divide Sent>
       final int PROD_SENTSTM13                                                            = 593;  // <Sent Stm> ::= <Enable Sent>
       final int PROD_SENTSTM14                                                            = 594;  // <Sent Stm> ::= <Evaluate Sent>
       final int PROD_SENTSTM15                                                            = 595;  // <Sent Stm> ::= <Exit Sent>
       final int PROD_SENTSTM16                                                            = 596;  // <Sent Stm> ::= <Generate Sent>
       final int PROD_SENTSTM17                                                            = 597;  // <Sent Stm> ::= <Go To Sent>
       final int PROD_SENTSTM18                                                            = 598;  // <Sent Stm> ::= <If Sent>
       final int PROD_SENTSTM19                                                            = 599;  // <Sent Stm> ::= <Initialize Sent>
       final int PROD_SENTSTM20                                                            = 600;  // <Sent Stm> ::= <Initiate Sent>
       final int PROD_SENTSTM21                                                            = 601;  // <Sent Stm> ::= <Inspect Sent>
       final int PROD_SENTSTM22                                                            = 602;  // <Sent Stm> ::= <Merge Sent>
       final int PROD_SENTSTM23                                                            = 603;  // <Sent Stm> ::= <Move Sent>
       final int PROD_SENTSTM24                                                            = 604;  // <Sent Stm> ::= <Multiply Sent>
       final int PROD_SENTSTM25                                                            = 605;  // <Sent Stm> ::= <Open Sent>
       final int PROD_SENTSTM26                                                            = 606;  // <Sent Stm> ::= <Perform Sent>
       final int PROD_SENTSTM27                                                            = 607;  // <Sent Stm> ::= <Read Sent>
       final int PROD_SENTSTM28                                                            = 608;  // <Sent Stm> ::= <Release Sent>
       final int PROD_SENTSTM29                                                            = 609;  // <Sent Stm> ::= <Return Sent>
       final int PROD_SENTSTM30                                                            = 610;  // <Sent Stm> ::= <Rewrite Sent>
       final int PROD_SENTSTM31                                                            = 611;  // <Sent Stm> ::= <Search Sent>
       final int PROD_SENTSTM32                                                            = 612;  // <Sent Stm> ::= <Send Sent>
       final int PROD_SENTSTM33                                                            = 613;  // <Sent Stm> ::= <Set Sent>
       final int PROD_SENTSTM34                                                            = 614;  // <Sent Stm> ::= <Sort Sent>
       final int PROD_SENTSTM35                                                            = 615;  // <Sent Stm> ::= <Start Sent>
       final int PROD_SENTSTM36                                                            = 616;  // <Sent Stm> ::= <Stop Sent>
       final int PROD_SENTSTM37                                                            = 617;  // <Sent Stm> ::= <String Sent>
       final int PROD_SENTSTM38                                                            = 618;  // <Sent Stm> ::= <Subtract Sent>
       final int PROD_SENTSTM39                                                            = 619;  // <Sent Stm> ::= <Suppress Sent>
       final int PROD_SENTSTM40                                                            = 620;  // <Sent Stm> ::= <Terminate Sent>
       final int PROD_SENTSTM41                                                            = 621;  // <Sent Stm> ::= <Unstring Sent>
       final int PROD_SENTSTM42                                                            = 622;  // <Sent Stm> ::= <Use Sent>
       final int PROD_SENTSTM43                                                            = 623;  // <Sent Stm> ::= <Write Sent>
       final int PROD_EMBEDSTMS                                                            = 624;  // <Embed Stms> ::= <Embed Stms> <Embed Stm>
       final int PROD_EMBEDSTMS2                                                           = 625;  // <Embed Stms> ::= <Embed Stm>
       final int PROD_EMBEDSTM                                                             = 626;  // <Embed Stm> ::= <Accept Embed>
       final int PROD_EMBEDSTM2                                                            = 627;  // <Embed Stm> ::= <Add Embed>
       final int PROD_EMBEDSTM3                                                            = 628;  // <Embed Stm> ::= <Alter Embed>
       final int PROD_EMBEDSTM4                                                            = 629;  // <Embed Stm> ::= <Call Embed>
       final int PROD_EMBEDSTM5                                                            = 630;  // <Embed Stm> ::= <Cancel Embed>
       final int PROD_EMBEDSTM6                                                            = 631;  // <Embed Stm> ::= <Close Embed>
       final int PROD_EMBEDSTM7                                                            = 632;  // <Embed Stm> ::= <Compute Embed>
       final int PROD_EMBEDSTM8                                                            = 633;  // <Embed Stm> ::= <Continue Embed>
       final int PROD_EMBEDSTM9                                                            = 634;  // <Embed Stm> ::= <Delete Embed>
       final int PROD_EMBEDSTM10                                                           = 635;  // <Embed Stm> ::= <Disable Embed>
       final int PROD_EMBEDSTM11                                                           = 636;  // <Embed Stm> ::= <Display Embed>
       final int PROD_EMBEDSTM12                                                           = 637;  // <Embed Stm> ::= <Divide Embed>
       final int PROD_EMBEDSTM13                                                           = 638;  // <Embed Stm> ::= <Enable Embed>
       final int PROD_EMBEDSTM14                                                           = 639;  // <Embed Stm> ::= <Evaluate Embed>
       final int PROD_EMBEDSTM15                                                           = 640;  // <Embed Stm> ::= <Exit Embed>
       final int PROD_EMBEDSTM16                                                           = 641;  // <Embed Stm> ::= <Generate Embed>
       final int PROD_EMBEDSTM17                                                           = 642;  // <Embed Stm> ::= <Go To Embed>
       final int PROD_EMBEDSTM18                                                           = 643;  // <Embed Stm> ::= <If Embed>
       final int PROD_EMBEDSTM19                                                           = 644;  // <Embed Stm> ::= <Initialize Embed>
       final int PROD_EMBEDSTM20                                                           = 645;  // <Embed Stm> ::= <Initiate Embed>
       final int PROD_EMBEDSTM21                                                           = 646;  // <Embed Stm> ::= <Inspect Embed>
       final int PROD_EMBEDSTM22                                                           = 647;  // <Embed Stm> ::= <Merge Embed>
       final int PROD_EMBEDSTM23                                                           = 648;  // <Embed Stm> ::= <Move Embed>
       final int PROD_EMBEDSTM24                                                           = 649;  // <Embed Stm> ::= <Multiply Embed>
       final int PROD_EMBEDSTM25                                                           = 650;  // <Embed Stm> ::= <Open Embed>
       final int PROD_EMBEDSTM26                                                           = 651;  // <Embed Stm> ::= <Perform Embed>
       final int PROD_EMBEDSTM27                                                           = 652;  // <Embed Stm> ::= <Read Embed>
       final int PROD_EMBEDSTM28                                                           = 653;  // <Embed Stm> ::= <Release Embed>
       final int PROD_EMBEDSTM29                                                           = 654;  // <Embed Stm> ::= <Return Embed>
       final int PROD_EMBEDSTM30                                                           = 655;  // <Embed Stm> ::= <Rewrite Embed>
       final int PROD_EMBEDSTM31                                                           = 656;  // <Embed Stm> ::= <Search Embed>
       final int PROD_EMBEDSTM32                                                           = 657;  // <Embed Stm> ::= <Send Embed>
       final int PROD_EMBEDSTM33                                                           = 658;  // <Embed Stm> ::= <Set Embed>
       final int PROD_EMBEDSTM34                                                           = 659;  // <Embed Stm> ::= <Sort Embed>
       final int PROD_EMBEDSTM35                                                           = 660;  // <Embed Stm> ::= <Start Embed>
       final int PROD_EMBEDSTM36                                                           = 661;  // <Embed Stm> ::= <Stop Embed>
       final int PROD_EMBEDSTM37                                                           = 662;  // <Embed Stm> ::= <String Embed>
       final int PROD_EMBEDSTM38                                                           = 663;  // <Embed Stm> ::= <Subtract Embed>
       final int PROD_EMBEDSTM39                                                           = 664;  // <Embed Stm> ::= <Suppress Embed>
       final int PROD_EMBEDSTM40                                                           = 665;  // <Embed Stm> ::= <Terminate Embed>
       final int PROD_EMBEDSTM41                                                           = 666;  // <Embed Stm> ::= <Unstring Embed>
       final int PROD_EMBEDSTM42                                                           = 667;  // <Embed Stm> ::= <Use Embed>
       final int PROD_EMBEDSTM43                                                           = 668;  // <Embed Stm> ::= <Write Embed>
       final int PROD_IMPERATIVESTMS                                                       = 669;  // <Imperative Stms> ::= <Imperative Stms> <Imperative Stm>
       final int PROD_IMPERATIVESTMS2                                                      = 670;  // <Imperative Stms> ::= <Imperative Stm>
       final int PROD_IMPERATIVESTM                                                        = 671;  // <Imperative Stm> ::= <Accept Imp>
       final int PROD_IMPERATIVESTM2                                                       = 672;  // <Imperative Stm> ::= <Add Imp>
       final int PROD_IMPERATIVESTM3                                                       = 673;  // <Imperative Stm> ::= <Alter Imp>
       final int PROD_IMPERATIVESTM4                                                       = 674;  // <Imperative Stm> ::= <Call Imp>
       final int PROD_IMPERATIVESTM5                                                       = 675;  // <Imperative Stm> ::= <Cancel Imp>
       final int PROD_IMPERATIVESTM6                                                       = 676;  // <Imperative Stm> ::= <Close Imp>
       final int PROD_IMPERATIVESTM7                                                       = 677;  // <Imperative Stm> ::= <Compute Imp>
       final int PROD_IMPERATIVESTM8                                                       = 678;  // <Imperative Stm> ::= <Continue Imp>
       final int PROD_IMPERATIVESTM9                                                       = 679;  // <Imperative Stm> ::= <Delete Imp>
       final int PROD_IMPERATIVESTM10                                                      = 680;  // <Imperative Stm> ::= <Disable Imp>
       final int PROD_IMPERATIVESTM11                                                      = 681;  // <Imperative Stm> ::= <Display Imp>
       final int PROD_IMPERATIVESTM12                                                      = 682;  // <Imperative Stm> ::= <Divide Imp>
       final int PROD_IMPERATIVESTM13                                                      = 683;  // <Imperative Stm> ::= <Enable Imp>
       final int PROD_IMPERATIVESTM14                                                      = 684;  // <Imperative Stm> ::= <Evaluate Imp>
       final int PROD_IMPERATIVESTM15                                                      = 685;  // <Imperative Stm> ::= <Exit Imp>
       final int PROD_IMPERATIVESTM16                                                      = 686;  // <Imperative Stm> ::= <Generate Imp>
       final int PROD_IMPERATIVESTM17                                                      = 687;  // <Imperative Stm> ::= <Go To Imp>
       final int PROD_IMPERATIVESTM18                                                      = 688;  // <Imperative Stm> ::= <If Imp>
       final int PROD_IMPERATIVESTM19                                                      = 689;  // <Imperative Stm> ::= <Initialize Imp>
       final int PROD_IMPERATIVESTM20                                                      = 690;  // <Imperative Stm> ::= <Initiate Imp>
       final int PROD_IMPERATIVESTM21                                                      = 691;  // <Imperative Stm> ::= <Inspect Imp>
       final int PROD_IMPERATIVESTM22                                                      = 692;  // <Imperative Stm> ::= <Merge Imp>
       final int PROD_IMPERATIVESTM23                                                      = 693;  // <Imperative Stm> ::= <Move Imp>
       final int PROD_IMPERATIVESTM24                                                      = 694;  // <Imperative Stm> ::= <Multiply Imp>
       final int PROD_IMPERATIVESTM25                                                      = 695;  // <Imperative Stm> ::= <Open Imp>
       final int PROD_IMPERATIVESTM26                                                      = 696;  // <Imperative Stm> ::= <Perform Imp>
       final int PROD_IMPERATIVESTM27                                                      = 697;  // <Imperative Stm> ::= <Read Imp>
       final int PROD_IMPERATIVESTM28                                                      = 698;  // <Imperative Stm> ::= <Release Imp>
       final int PROD_IMPERATIVESTM29                                                      = 699;  // <Imperative Stm> ::= <Return Imp>
       final int PROD_IMPERATIVESTM30                                                      = 700;  // <Imperative Stm> ::= <Rewrite Imp>
       final int PROD_IMPERATIVESTM31                                                      = 701;  // <Imperative Stm> ::= <Search Imp>
       final int PROD_IMPERATIVESTM32                                                      = 702;  // <Imperative Stm> ::= <Send Imp>
       final int PROD_IMPERATIVESTM33                                                      = 703;  // <Imperative Stm> ::= <Set Imp>
       final int PROD_IMPERATIVESTM34                                                      = 704;  // <Imperative Stm> ::= <Sort Imp>
       final int PROD_IMPERATIVESTM35                                                      = 705;  // <Imperative Stm> ::= <Start Imp>
       final int PROD_IMPERATIVESTM36                                                      = 706;  // <Imperative Stm> ::= <Stop Imp>
       final int PROD_IMPERATIVESTM37                                                      = 707;  // <Imperative Stm> ::= <String Imp>
       final int PROD_IMPERATIVESTM38                                                      = 708;  // <Imperative Stm> ::= <Subtract Imp>
       final int PROD_IMPERATIVESTM39                                                      = 709;  // <Imperative Stm> ::= <Suppress Imp>
       final int PROD_IMPERATIVESTM40                                                      = 710;  // <Imperative Stm> ::= <Terminate Imp>
       final int PROD_IMPERATIVESTM41                                                      = 711;  // <Imperative Stm> ::= <Unstring Imp>
       final int PROD_IMPERATIVESTM42                                                      = 712;  // <Imperative Stm> ::= <Use Imp>
       final int PROD_IMPERATIVESTM43                                                      = 713;  // <Imperative Stm> ::= <Write Imp>
       final int PROD_SIZECLAUSES                                                          = 714;  // <Size Clauses> ::= <Size Clauses> <Size Clause>
       final int PROD_SIZECLAUSES2                                                         = 715;  // <Size Clauses> ::= <Size Clause>
       final int PROD_SIZECLAUSE_SIZE_ERROR                                                = 716;  // <Size Clause> ::= <ON Opt> SIZE ERROR <Imperative Stms>
       final int PROD_SIZECLAUSE_NOT_SIZE_ERROR                                            = 717;  // <Size Clause> ::= NOT <ON Opt> SIZE ERROR <Imperative Stms>
       final int PROD_INVALIDKEYCLAUSES                                                    = 718;  // <Invalid Key Clauses> ::= <Invalid Key Clauses> <Invalid Key Clause>
       final int PROD_INVALIDKEYCLAUSES2                                                   = 719;  // <Invalid Key Clauses> ::= <Invalid Key Clause>
       final int PROD_INVALIDKEYCLAUSE_INVALID                                             = 720;  // <Invalid Key Clause> ::= INVALID <KEY Opt> <Imperative Stms>
       final int PROD_INVALIDKEYCLAUSE_NOT_INVALID                                         = 721;  // <Invalid Key Clause> ::= NOT INVALID <KEY Opt> <Imperative Stms>
       final int PROD_EXCEPTIONCLAUSES                                                     = 722;  // <Exception Clauses> ::= <Exception Clauses> <Exception Clause>
       final int PROD_EXCEPTIONCLAUSES2                                                    = 723;  // <Exception Clauses> ::= <Exception Clause>
       final int PROD_EXCEPTIONCLAUSE_EXCEPTION                                            = 724;  // <Exception Clause> ::= <ON Opt> EXCEPTION <Imperative Stms>
       final int PROD_EXCEPTIONCLAUSE_NOT_EXCEPTION                                        = 725;  // <Exception Clause> ::= NOT <ON Opt> EXCEPTION <Imperative Stms>
       final int PROD_OVERFLOWCLAUSES                                                      = 726;  // <Overflow Clauses> ::= <Overflow Clauses> <Overflow Clause>
       final int PROD_OVERFLOWCLAUSES2                                                     = 727;  // <Overflow Clauses> ::= <Overflow Clause>
       final int PROD_OVERFLOWCLAUSE_OVERFLOW                                              = 728;  // <Overflow Clause> ::= <ON Opt> OVERFLOW <Imperative Stms>
       final int PROD_OVERFLOWCLAUSE_NOT_OVERFLOW                                          = 729;  // <Overflow Clause> ::= NOT <ON Opt> OVERFLOW <Imperative Stms>
       final int PROD_ATENDCLAUSES                                                         = 730;  // <At End Clauses> ::= <At End Clauses> <At End Clause>
       final int PROD_ATENDCLAUSES2                                                        = 731;  // <At End Clauses> ::= <At End Clause>
       final int PROD_ATENDCLAUSE_AT_END                                                   = 732;  // <At End Clause> ::= AT END <Imperative Stms>
       final int PROD_ATENDCLAUSE_NOT_AT_END                                               = 733;  // <At End Clause> ::= NOT AT END <Imperative Stms>
       final int PROD_ATEOPCLAUSES                                                         = 734;  // <AT EOP Clauses> ::= <AT EOP Clauses> <AT EOP Clause>
       final int PROD_ATEOPCLAUSES2                                                        = 735;  // <AT EOP Clauses> ::= <AT EOP Clause>
       final int PROD_ATEOPCLAUSE_AT                                                       = 736;  // <AT EOP Clause> ::= AT <End of Page> <Imperative Stms>
       final int PROD_ATEOPCLAUSE_NOT_AT                                                   = 737;  // <AT EOP Clause> ::= NOT AT <End of Page> <Imperative Stms>
       final int PROD_ENDOFPAGE_ENDMINUSOFMINUSPAGE                                        = 738;  // <End of Page> ::= 'END-OF-PAGE'
       final int PROD_ENDOFPAGE_EOP                                                        = 739;  // <End of Page> ::= EOP
       final int PROD_ACCEPTSENT                                                           = 740;  // <Accept Sent> ::= <Accept Stm>
       final int PROD_ACCEPTEMBED                                                          = 741;  // <Accept Embed> ::= <Accept Stm>
       final int PROD_ACCEPTIMP                                                            = 742;  // <Accept Imp> ::= <Accept Stm>
       final int PROD_ACCEPTSTM_ACCEPT_IDENTIFIER                                          = 743;  // <Accept Stm> ::= ACCEPT Identifier
       final int PROD_ACCEPTSTM_ACCEPT_IDENTIFIER_FROM                                     = 744;  // <Accept Stm> ::= ACCEPT Identifier FROM <Accept From Arg>
       final int PROD_ACCEPTSTM_ACCEPT_IDENTIFIER_COUNT                                    = 745;  // <Accept Stm> ::= ACCEPT Identifier <MESSAGE opt> COUNT
       final int PROD_ACCEPTFROMARG_FROM_DATE                                              = 746;  // <Accept From Arg> ::= FROM DATE
       final int PROD_ACCEPTFROMARG_FROM_DAY                                               = 747;  // <Accept From Arg> ::= FROM DAY
       final int PROD_ACCEPTFROMARG_FROM_DAYMINUSOFMINUSWEEK                               = 748;  // <Accept From Arg> ::= FROM 'DAY-OF-WEEK'
       final int PROD_ACCEPTFROMARG_FROM_TIME                                              = 749;  // <Accept From Arg> ::= FROM TIME
       final int PROD_ACCEPTFROMARG_FROM_CONSOLE                                           = 750;  // <Accept From Arg> ::= FROM CONSOLE
       final int PROD_ACCEPTFROMARG_FROM_IDENTIFIER                                        = 751;  // <Accept From Arg> ::= FROM Identifier
       final int PROD_ACCEPTFROMARG                                                        = 752;  // <Accept From Arg> ::= 
       final int PROD_ADDSENT                                                              = 753;  // <Add Sent> ::= <Add Stm> <Size Clauses> <END-ADD Opt>
       final int PROD_ADDSENT2                                                             = 754;  // <Add Sent> ::= <Add Stm>
       final int PROD_ADDEMBED_ENDMINUSADD                                                 = 755;  // <Add Embed> ::= <Add Stm> <Size Clauses> 'END-ADD'
       final int PROD_ADDEMBED                                                             = 756;  // <Add Embed> ::= <Add Stm>
       final int PROD_ADDIMP                                                               = 757;  // <Add Imp> ::= <Add Stm>
       final int PROD_ADDSTM_ADD_TO                                                        = 758;  // <Add Stm> ::= ADD <Values> TO <Add Items> <Giving Clause>
       final int PROD_ADDSTM_ADD_IDENTIFIER_TO_IDENTIFIER                                  = 759;  // <Add Stm> ::= ADD <CORRESPONDING> Identifier TO Identifier <ROUNDED Opt>
       final int PROD_GIVINGCLAUSE_GIVING                                                  = 760;  // <Giving Clause> ::= GIVING <Add Item>
       final int PROD_GIVINGCLAUSE                                                         = 761;  // <Giving Clause> ::= 
       final int PROD_ADDITEMS                                                             = 762;  // <Add Items> ::= <Add Items> <Add Item>
       final int PROD_ADDITEMS2                                                            = 763;  // <Add Items> ::= <Add Item>
       final int PROD_ADDITEM                                                              = 764;  // <Add Item> ::= <Variable> <ROUNDED Opt>
       final int PROD_ENDADDOPT_ENDMINUSADD                                                = 765;  // <END-ADD Opt> ::= 'END-ADD'
       final int PROD_ENDADDOPT                                                            = 766;  // <END-ADD Opt> ::= 
       final int PROD_ALTERSENT                                                            = 767;  // <Alter Sent> ::= <Alter Stm>
       final int PROD_ALTEREMBED                                                           = 768;  // <Alter Embed> ::= <Alter Stm>
       final int PROD_ALTERIMP                                                             = 769;  // <Alter Imp> ::= <Alter Stm>
       final int PROD_ALTERSTM_ALTER_IDENTIFIER_TO_IDENTIFIER                              = 770;  // <Alter Stm> ::= ALTER Identifier TO Identifier
       final int PROD_ALTERSTM_ALTER_IDENTIFIER_TO_PROCEED_TO_IDENTIFIER                   = 771;  // <Alter Stm> ::= ALTER Identifier TO PROCEED TO Identifier
       final int PROD_CALLSENT                                                             = 772;  // <Call Sent> ::= <Call Stm> <Exception Clauses> <END-CALL Opt>
       final int PROD_CALLSENT2                                                            = 773;  // <Call Sent> ::= <Call Stm> <Overflow Clauses> <END-CALL Opt>
       final int PROD_CALLSENT3                                                            = 774;  // <Call Sent> ::= <Call Stm>
       final int PROD_CALLEMBED_ENDMINUSCALL                                               = 775;  // <Call Embed> ::= <Call Stm> <Exception Clauses> 'END-CALL'
       final int PROD_CALLEMBED_ENDMINUSCALL2                                              = 776;  // <Call Embed> ::= <Call Stm> <Overflow Clauses> 'END-CALL'
       final int PROD_CALLEMBED                                                            = 777;  // <Call Embed> ::= <Call Stm>
       final int PROD_CALLIMP                                                              = 778;  // <Call Imp> ::= <Call Stm>
       final int PROD_CALLSTM_CALL                                                         = 779;  // <Call Stm> ::= CALL <Value>
       final int PROD_CALLSTM_CALL_USING                                                   = 780;  // <Call Stm> ::= CALL <Value> USING <Call Items>
       final int PROD_CALLITEMS                                                            = 781;  // <Call Items> ::= <Call Items> <Call Item>
       final int PROD_CALLITEMS2                                                           = 782;  // <Call Items> ::= <Call Item>
       final int PROD_CALLITEM                                                             = 783;  // <Call Item> ::= <Variable>
       final int PROD_CALLITEM_REFERENCE                                                   = 784;  // <Call Item> ::= <BY Opt> REFERENCE <Variable>
       final int PROD_CALLITEM_CONTENT                                                     = 785;  // <Call Item> ::= <BY Opt> CONTENT <Variable>
       final int PROD_ENDCALLOPT_ENDMINUSCALL                                              = 786;  // <END-CALL Opt> ::= 'END-CALL'
       final int PROD_ENDCALLOPT                                                           = 787;  // <END-CALL Opt> ::= 
       final int PROD_CANCELSENT                                                           = 788;  // <Cancel Sent> ::= <Cancel Stm>
       final int PROD_CANCELEMBED                                                          = 789;  // <Cancel Embed> ::= <Cancel Stm>
       final int PROD_CANCELIMP                                                            = 790;  // <Cancel Imp> ::= <Cancel Stm>
       final int PROD_CANCELSTM_CANCEL                                                     = 791;  // <Cancel Stm> ::= CANCEL <Values>
       final int PROD_CLOSESENT                                                            = 792;  // <Close Sent> ::= <Close Stm>
       final int PROD_CLOSEEMBED                                                           = 793;  // <Close Embed> ::= <Close Stm>
       final int PROD_CLOSEIMP                                                             = 794;  // <Close Imp> ::= <Close Stm>
       final int PROD_CLOSESTM_CLOSE                                                       = 795;  // <Close Stm> ::= CLOSE <Close Items>
       final int PROD_CLOSEITEMS                                                           = 796;  // <Close Items> ::= <Close Items> <Close Item>
       final int PROD_CLOSEITEMS2                                                          = 797;  // <Close Items> ::= <Close Item>
       final int PROD_CLOSEITEM_IDENTIFIER                                                 = 798;  // <Close Item> ::= Identifier <Close Options>
       final int PROD_CLOSEOPTIONS_UNIT                                                    = 799;  // <Close Options> ::= UNIT <Close Method>
       final int PROD_CLOSEOPTIONS_REEL                                                    = 800;  // <Close Options> ::= REEL <Close Method>
       final int PROD_CLOSEOPTIONS_LOCK                                                    = 801;  // <Close Options> ::= <WITH Opt> LOCK
       final int PROD_CLOSEOPTIONS_NO_REWIND                                               = 802;  // <Close Options> ::= <WITH Opt> NO REWIND
       final int PROD_CLOSEOPTIONS                                                         = 803;  // <Close Options> ::= 
       final int PROD_CLOSEMETHOD_REMOVAL                                                  = 804;  // <Close Method> ::= <FOR Opt> REMOVAL
       final int PROD_CLOSEMETHOD                                                          = 805;  // <Close Method> ::= 
       final int PROD_COMPUTESENT                                                          = 806;  // <Compute Sent> ::= <Compute Stm> <Size Clauses> <END-COMPUTE Opt>
       final int PROD_COMPUTESENT2                                                         = 807;  // <Compute Sent> ::= <Compute Stm>
       final int PROD_COMPUTEEMBED_ENDMINUSCOMPUTE                                         = 808;  // <Compute Embed> ::= <Compute Stm> <Size Clauses> 'END-COMPUTE'
       final int PROD_COMPUTEEMBED                                                         = 809;  // <Compute Embed> ::= <Compute Stm>
       final int PROD_COMPUTEIMP                                                           = 810;  // <Compute Imp> ::= <Compute Stm>
       final int PROD_COMPUTESTM_COMPUTE_IDENTIFIER                                        = 811;  // <Compute Stm> ::= COMPUTE Identifier <ROUNDED Opt> <Equal Op> <Math Exp>
       final int PROD_MATHEXP_PLUS                                                         = 812;  // <Math Exp> ::= <Math Exp> '+' <Mult Exp>
       final int PROD_MATHEXP_MINUS                                                        = 813;  // <Math Exp> ::= <Math Exp> '-' <Mult Exp>
       final int PROD_MATHEXP                                                              = 814;  // <Math Exp> ::= <Mult Exp>
       final int PROD_MULTEXP_TIMES                                                        = 815;  // <Mult Exp> ::= <Mult Exp> '*' <Negate Exp>
       final int PROD_MULTEXP_DIV                                                          = 816;  // <Mult Exp> ::= <Mult Exp> '/' <Negate Exp>
       final int PROD_MULTEXP                                                              = 817;  // <Mult Exp> ::= <Negate Exp>
       final int PROD_NEGATEEXP_MINUS                                                      = 818;  // <Negate Exp> ::= '-' <Value>
       final int PROD_NEGATEEXP                                                            = 819;  // <Negate Exp> ::= <Value>
       final int PROD_NEGATEEXP_LPAREN_RPAREN                                              = 820;  // <Negate Exp> ::= '(' <Math Exp> ')'
       final int PROD_ENDCOMPUTEOPT_ENDMINUSCOMPUTE                                        = 821;  // <END-COMPUTE Opt> ::= 'END-COMPUTE'
       final int PROD_ENDCOMPUTEOPT                                                        = 822;  // <END-COMPUTE Opt> ::= 
       final int PROD_CONTINUESENT                                                         = 823;  // <Continue Sent> ::= <Continue Stm>
       final int PROD_CONTINUEEMBED                                                        = 824;  // <Continue Embed> ::= <Continue Stm>
       final int PROD_CONTINUEIMP                                                          = 825;  // <Continue Imp> ::= <Continue Stm>
       final int PROD_CONTINUESTM_CONTINUE                                                 = 826;  // <Continue Stm> ::= CONTINUE
       final int PROD_DELETESENT                                                           = 827;  // <Delete Sent> ::= <Delete Stm> <Invalid Key Clauses> <END-DELETE Opt>
       final int PROD_DELETESENT2                                                          = 828;  // <Delete Sent> ::= <Delete Stm>
       final int PROD_DELETEEMBED_ENDMINUSDELETE                                           = 829;  // <Delete Embed> ::= <Delete Stm> <Invalid Key Clauses> 'END-DELETE'
       final int PROD_DELETEEMBED                                                          = 830;  // <Delete Embed> ::= <Delete Stm>
       final int PROD_DELETEIMP                                                            = 831;  // <Delete Imp> ::= <Delete Stm>
       final int PROD_DELETESTM_DELETE_IDENTIFIER                                          = 832;  // <Delete Stm> ::= DELETE Identifier <RECORD Opt>
       final int PROD_ENDDELETEOPT_ENDMINUSDELETE                                          = 833;  // <END-DELETE Opt> ::= 'END-DELETE'
       final int PROD_ENDDELETEOPT                                                         = 834;  // <END-DELETE Opt> ::= 
       final int PROD_DISABLESENT                                                          = 835;  // <Disable Sent> ::= <Disable Stm>
       final int PROD_DISABLEEMBED                                                         = 836;  // <Disable Embed> ::= <Disable Stm>
       final int PROD_DISABLEIMP                                                           = 837;  // <Disable Imp> ::= <Disable Stm>
       final int PROD_DISABLESTM_DISABLE_IDENTIFIER                                        = 838;  // <Disable Stm> ::= DISABLE <Enabled Disable Mode> Identifier <Enable Disable Key>
       final int PROD_DISPLAYSENT                                                          = 839;  // <Display Sent> ::= <Display Stm>
       final int PROD_DISPLAYEMBED                                                         = 840;  // <Display Embed> ::= <Display Stm>
       final int PROD_DISPLAYIMP                                                           = 841;  // <Display Imp> ::= <Display Stm>
       final int PROD_DISPLAYSTM_DISPLAY                                                   = 842;  // <Display Stm> ::= DISPLAY <Values> <Display Target> <Advancing Clause>
       final int PROD_DISPLAYTARGET_UPON_IDENTIFIER                                        = 843;  // <Display Target> ::= UPON Identifier
       final int PROD_DISPLAYTARGET                                                        = 844;  // <Display Target> ::= 
       final int PROD_ADVANCINGCLAUSE_NO_ADVANCING                                         = 845;  // <Advancing Clause> ::= <WITH Opt> NO ADVANCING
       final int PROD_ADVANCINGCLAUSE                                                      = 846;  // <Advancing Clause> ::= 
       final int PROD_DIVIDESENT                                                           = 847;  // <Divide Sent> ::= <Divide Stm> <Size Clauses> <END-DIVIDE Opt>
       final int PROD_DIVIDESENT2                                                          = 848;  // <Divide Sent> ::= <Divide Stm>
       final int PROD_DIVIDEEMBED_ENDMINUSDIVIDE                                           = 849;  // <Divide Embed> ::= <Divide Stm> <Size Clauses> 'END-DIVIDE'
       final int PROD_DIVIDEEMBED                                                          = 850;  // <Divide Embed> ::= <Divide Stm>
       final int PROD_DIVIDEIMP                                                            = 851;  // <Divide Imp> ::= <Divide Stm>
       final int PROD_DIVIDESTM_DIVIDE_BY                                                  = 852;  // <Divide Stm> ::= DIVIDE <Values> BY <Values>
       final int PROD_DIVIDESTM_DIVIDE_BY_GIVING                                           = 853;  // <Divide Stm> ::= DIVIDE <Values> BY <Values> GIVING <Variable> <ROUNDED Opt>
       final int PROD_DIVIDESTM_DIVIDE_INTO                                                = 854;  // <Divide Stm> ::= DIVIDE <Values> INTO <Values>
       final int PROD_DIVIDESTM_DIVIDE_INTO_GIVING                                         = 855;  // <Divide Stm> ::= DIVIDE <Values> INTO <Values> GIVING <Variable> <ROUNDED Opt> <Remainder Opt>
       final int PROD_REMAINDEROPT_REMAINDER                                               = 856;  // <Remainder Opt> ::= REMAINDER <Variable>
       final int PROD_REMAINDEROPT                                                         = 857;  // <Remainder Opt> ::= 
       final int PROD_ENDDIVIDEOPT_ENDMINUSDIVIDE                                          = 858;  // <END-DIVIDE Opt> ::= 'END-DIVIDE'
       final int PROD_ENDDIVIDEOPT                                                         = 859;  // <END-DIVIDE Opt> ::= 
       final int PROD_ENABLESENT                                                           = 860;  // <Enable Sent> ::= <Enable Stm>
       final int PROD_ENABLEEMBED                                                          = 861;  // <Enable Embed> ::= <Enable Stm>
       final int PROD_ENABLEIMP                                                            = 862;  // <Enable Imp> ::= <Enable Stm>
       final int PROD_ENABLESTM_ENABLE_IDENTIFIER                                          = 863;  // <Enable Stm> ::= ENABLE <Enabled Disable Mode> Identifier <Enable Disable Key>
       final int PROD_EVALUATESENT                                                         = 864;  // <Evaluate Sent> ::= <Evaluate Stm> <END-EVALUATE Opt>
       final int PROD_EVALUATEEMBED_ENDMINUSEVALUATE                                       = 865;  // <Evaluate Embed> ::= <Evaluate Stm> 'END-EVALUATE'
       final int PROD_EVALUATEIMP_ENDMINUSEVALUATE                                         = 866;  // <Evaluate Imp> ::= <Evaluate Stm> 'END-EVALUATE'
       final int PROD_EVALUATESTM_EVALUATE                                                 = 867;  // <Evaluate Stm> ::= EVALUATE <Subjects> <When Clauses>
       final int PROD_SUBJECTS_ALSO                                                        = 868;  // <Subjects> ::= <Subjects> ALSO <Subject>
       final int PROD_SUBJECTS                                                             = 869;  // <Subjects> ::= <Subject>
       final int PROD_SUBJECT_TRUE                                                         = 870;  // <Subject> ::= TRUE
       final int PROD_SUBJECT                                                              = 871;  // <Subject> ::= <Boolean Exp>
       final int PROD_WHENCLAUSES                                                          = 872;  // <When Clauses> ::= <When Clauses> <When Clause>
       final int PROD_WHENCLAUSES2                                                         = 873;  // <When Clauses> ::= <When Clause>
       final int PROD_WHENCLAUSE_WHEN                                                      = 874;  // <When Clause> ::= WHEN <Phrases> <THEN Opt> <Embed Stms>
       final int PROD_WHENCLAUSE_WHEN_OTHER                                                = 875;  // <When Clause> ::= WHEN OTHER <THEN Opt> <Embed Stms>
       final int PROD_PHRASES_ALSO                                                         = 876;  // <Phrases> ::= <Phrases> ALSO <Phrase>
       final int PROD_PHRASES                                                              = 877;  // <Phrases> ::= <Phrase>
       final int PROD_PHRASE_ANY                                                           = 878;  // <Phrase> ::= ANY
       final int PROD_PHRASE_THROUGH                                                       = 879;  // <Phrase> ::= <Symbolic Value> THROUGH <Symbolic Value>
       final int PROD_PHRASE_THRU                                                          = 880;  // <Phrase> ::= <Symbolic Value> THRU <Symbolic Value>
       final int PROD_PHRASE                                                               = 881;  // <Phrase> ::= <Symbolic Value>
       final int PROD_ENDEVALUATEOPT_ENDMINUSEVALUATE                                      = 882;  // <END-EVALUATE Opt> ::= 'END-EVALUATE'
       final int PROD_ENDEVALUATEOPT                                                       = 883;  // <END-EVALUATE Opt> ::= 
       final int PROD_EXITSENT                                                             = 884;  // <Exit Sent> ::= <Exit Stm>
       final int PROD_EXITEMBED                                                            = 885;  // <Exit Embed> ::= <Exit Stm>
       final int PROD_EXITIMP                                                              = 886;  // <Exit Imp> ::= <Exit Stm>
       final int PROD_EXITSTM_EXIT                                                         = 887;  // <Exit Stm> ::= EXIT
       final int PROD_EXITSTM_EXIT_PROGRAM                                                 = 888;  // <Exit Stm> ::= EXIT PROGRAM
       final int PROD_GENERATESENT                                                         = 889;  // <Generate Sent> ::= <Generate Stm>
       final int PROD_GENERATEEMBED                                                        = 890;  // <Generate Embed> ::= <Generate Stm>
       final int PROD_GENERATEIMP                                                          = 891;  // <Generate Imp> ::= <Generate Stm>
       final int PROD_GENERATESTM_GENERATE_IDENTIFIER                                      = 892;  // <Generate Stm> ::= GENERATE Identifier
       final int PROD_GOTOSENT                                                             = 893;  // <Go To Sent> ::= <Go To Stm>
       final int PROD_GOTOEMBED                                                            = 894;  // <Go To Embed> ::= <Go To Stm>
       final int PROD_GOTOIMP                                                              = 895;  // <Go To Imp> ::= <Go To Stm>
       final int PROD_GOTOSTM_GO_TO_IDENTIFIER                                             = 896;  // <Go To Stm> ::= GO TO Identifier
       final int PROD_GOTOSTM_GO_TO_DEPENDING_ON                                           = 897;  // <Go To Stm> ::= GO TO <Identifiers> DEPENDING ON <Variable>
       final int PROD_IFSENT                                                               = 898;  // <If Sent> ::= <If Stm> <END-IF Opt>
       final int PROD_IFEMBED_ENDMINUSIF                                                   = 899;  // <If Embed> ::= <If Stm> 'END-IF'
       final int PROD_IFIMP_ENDMINUSIF                                                     = 900;  // <If Imp> ::= <If Stm> 'END-IF'
       final int PROD_IFSTM_IF                                                             = 901;  // <If Stm> ::= IF <Boolean Exp> <THEN Opt> <Embed Stms>
       final int PROD_IFSTM_IF_ELSE                                                        = 902;  // <If Stm> ::= IF <Boolean Exp> <THEN Opt> <Embed Stms> ELSE <THEN Opt> <Embed Stms>
       final int PROD_IFSTM_IF_ELSE_NEXT_SENTENCE                                          = 903;  // <If Stm> ::= IF <Boolean Exp> <THEN Opt> <Embed Stms> ELSE NEXT SENTENCE
       final int PROD_ENDIFOPT_ENDMINUSIF                                                  = 904;  // <END-IF Opt> ::= 'END-IF'
       final int PROD_ENDIFOPT                                                             = 905;  // <END-IF Opt> ::= 
       final int PROD_INITIALIZESENT                                                       = 906;  // <Initialize Sent> ::= <Initialize Stm>
       final int PROD_INITIALIZEEMBED                                                      = 907;  // <Initialize Embed> ::= <Initialize Stm>
       final int PROD_INITIALIZEIMP                                                        = 908;  // <Initialize Imp> ::= <Initialize Stm>
       final int PROD_INITIALIZESTM_INITIALIZE                                             = 909;  // <Initialize Stm> ::= INITIALIZE <Variables> <Replacing Opt>
       final int PROD_REPLACINGOPT_REPLACING                                               = 910;  // <Replacing Opt> ::= REPLACING <Replacing Items>
       final int PROD_REPLACINGITEMS                                                       = 911;  // <Replacing Items> ::= <Replacing Items> <Replacing Item>
       final int PROD_REPLACINGITEMS2                                                      = 912;  // <Replacing Items> ::= <Replacing Item>
       final int PROD_REPLACINGITEM                                                        = 913;  // <Replacing Item> ::= <Replacing Type> <DATA Opt> <Value>
       final int PROD_REPLACINGTYPE_ALPHABETIC                                             = 914;  // <Replacing Type> ::= ALPHABETIC
       final int PROD_REPLACINGTYPE_ALPHANUMERIC                                           = 915;  // <Replacing Type> ::= ALPHANUMERIC
       final int PROD_REPLACINGTYPE_NUMERIC                                                = 916;  // <Replacing Type> ::= NUMERIC
       final int PROD_REPLACINGTYPE_ALPHANUMERICMINUSEDITED                                = 917;  // <Replacing Type> ::= 'ALPHANUMERIC-EDITED'
       final int PROD_REPLACINGTYPE_NUMERICMINUSEDITED                                     = 918;  // <Replacing Type> ::= 'NUMERIC-EDITED'
       final int PROD_INITIATESENT                                                         = 919;  // <Initiate Sent> ::= <Initiate Stm>
       final int PROD_INITIATEEMBED                                                        = 920;  // <Initiate Embed> ::= <Initiate Stm>
       final int PROD_INITIATEIMP                                                          = 921;  // <Initiate Imp> ::= <Initiate Stm>
       final int PROD_INITIATESTM_INITIATE                                                 = 922;  // <Initiate Stm> ::= INITIATE <Identifiers>
       final int PROD_INSPECTSENT                                                          = 923;  // <Inspect Sent> ::= <Inspect Stm>
       final int PROD_INSPECTEMBED                                                         = 924;  // <Inspect Embed> ::= <Inspect Stm>
       final int PROD_INSPECTIMP                                                           = 925;  // <Inspect Imp> ::= <Inspect Stm>
       final int PROD_INSPECTSTM_INSPECT_TALLYING                                          = 926;  // <Inspect Stm> ::= INSPECT <Variable> TALLYING <Tally Variables>
       final int PROD_INSPECTSTM_INSPECT_TALLYING_REPLACING                                = 927;  // <Inspect Stm> ::= INSPECT <Variable> TALLYING <Tally Variables> REPLACING <Replace Chars>
       final int PROD_INSPECTSTM_INSPECT_REPLACING                                         = 928;  // <Inspect Stm> ::= INSPECT <Variable> REPLACING <Replace Chars>
       final int PROD_INSPECTSTM_INSPECT_CONVERTING_TO                                     = 929;  // <Inspect Stm> ::= INSPECT <Variable> CONVERTING <Value> TO <Value> <Inspect Specs>
       final int PROD_TALLYVARIABLES                                                       = 930;  // <Tally Variables> ::= <Tally Variables> <Tally Variable>
       final int PROD_TALLYVARIABLES2                                                      = 931;  // <Tally Variables> ::= <Tally Variable>
       final int PROD_TALLYVARIABLE_FOR                                                    = 932;  // <Tally Variable> ::= <Variable> FOR <Tally Items>
       final int PROD_TALLYITEMS                                                           = 933;  // <Tally Items> ::= <Tally Items> <Tally Item>
       final int PROD_TALLYITEMS2                                                          = 934;  // <Tally Items> ::= <Tally Item>
       final int PROD_TALLYITEM_CHARACTERS                                                 = 935;  // <Tally Item> ::= CHARACTERS <Inspect Specs>
       final int PROD_TALLYITEM_ALL                                                        = 936;  // <Tally Item> ::= ALL <Value> <Inspect Specs>
       final int PROD_TALLYITEM_LEADING                                                    = 937;  // <Tally Item> ::= LEADING <Value> <Inspect Specs>
       final int PROD_REPLACECHARS                                                         = 938;  // <Replace Chars> ::= <Replace Chars> <Replace Char>
       final int PROD_REPLACECHARS2                                                        = 939;  // <Replace Chars> ::= <Replace Char>
       final int PROD_REPLACECHAR_CHARACTERS_BY                                            = 940;  // <Replace Char> ::= CHARACTERS BY <Value> <Inspect Specs>
       final int PROD_REPLACECHAR_ALL                                                      = 941;  // <Replace Char> ::= ALL <Replace Items>
       final int PROD_REPLACECHAR_LEADING                                                  = 942;  // <Replace Char> ::= LEADING <Replace Items>
       final int PROD_REPLACECHAR_FIRST                                                    = 943;  // <Replace Char> ::= FIRST <Replace Items>
       final int PROD_REPLACEITEMS                                                         = 944;  // <Replace Items> ::= <Replace Items> <Replace Item>
       final int PROD_REPLACEITEMS2                                                        = 945;  // <Replace Items> ::= <Replace Item>
       final int PROD_REPLACEITEM_BY                                                       = 946;  // <Replace Item> ::= <Value> BY <Value> <Inspect Specs>
       final int PROD_INSPECTSPECS                                                         = 947;  // <Inspect Specs> ::= <Inspect Specs> <Inspect Spec>
       final int PROD_INSPECTSPECS2                                                        = 948;  // <Inspect Specs> ::= 
       final int PROD_INSPECTSPEC_INITIAL                                                  = 949;  // <Inspect Spec> ::= <BEFORE AFTER> INITIAL <Value>
       final int PROD_MERGESENT                                                            = 950;  // <Merge Sent> ::= <Merge Stm>
       final int PROD_MERGEEMBED                                                           = 951;  // <Merge Embed> ::= <Merge Stm>
       final int PROD_MERGEIMP                                                             = 952;  // <Merge Imp> ::= <Merge Stm>
       final int PROD_MERGESTM_MERGE_IDENTIFIER_USING                                      = 953;  // <Merge Stm> ::= MERGE Identifier <Sort Keys> <Collating Clause> USING <Identifiers> <Sort Target>
       final int PROD_MOVESENT                                                             = 954;  // <Move Sent> ::= <Move Stm> <Size Clauses> <END-MOVE Opt>
       final int PROD_MOVESENT2                                                            = 955;  // <Move Sent> ::= <Move Stm>
       final int PROD_MOVEEMBED_ENDMINUSMOVE                                               = 956;  // <Move Embed> ::= <Move Stm> <Size Clauses> 'END-MOVE'
       final int PROD_MOVEEMBED                                                            = 957;  // <Move Embed> ::= <Move Stm>
       final int PROD_MOVEIMP                                                              = 958;  // <Move Imp> ::= <Move Stm>
       final int PROD_MOVESTM_MOVE_TO                                                      = 959;  // <Move Stm> ::= MOVE <Symbolic Value> TO <Variables>
       final int PROD_MOVESTM_MOVE_IDENTIFIER_TO_IDENTIFIER                                = 960;  // <Move Stm> ::= MOVE <CORRESPONDING> Identifier TO Identifier
       final int PROD_ENDMOVEOPT_ENDMINUSMOVE                                              = 961;  // <END-MOVE Opt> ::= 'END-MOVE'
       final int PROD_ENDMOVEOPT                                                           = 962;  // <END-MOVE Opt> ::= 
       final int PROD_MULTIPLYSENT                                                         = 963;  // <Multiply Sent> ::= <Multiply Stm> <Size Clauses> <END-MULTIPLY Opt>
       final int PROD_MULTIPLYSENT2                                                        = 964;  // <Multiply Sent> ::= <Multiply Stm>
       final int PROD_MULTIPLYEMBED_ENDMINUSMULTIPLY                                       = 965;  // <Multiply Embed> ::= <Multiply Stm> <Size Clauses> 'END-MULTIPLY'
       final int PROD_MULTIPLYEMBED                                                        = 966;  // <Multiply Embed> ::= <Multiply Stm>
       final int PROD_MULTIPLYIMP                                                          = 967;  // <Multiply Imp> ::= <Multiply Stm>
       final int PROD_MULTIPLYSTM_MULTIPLY_BY                                              = 968;  // <Multiply Stm> ::= MULTIPLY <Variables> BY <Multiply Items> <Giving Clause Opt>
       final int PROD_MULTIPLYITEMS                                                        = 969;  // <Multiply Items> ::= <Multiply Items> <Multiply Item>
       final int PROD_MULTIPLYITEMS2                                                       = 970;  // <Multiply Items> ::= <Multiply Item>
       final int PROD_MULTIPLYITEM                                                         = 971;  // <Multiply Item> ::= <Value> <ROUNDED Opt>
       final int PROD_ENDMULTIPLYOPT_ENDMINUSMULTIPLY                                      = 972;  // <END-MULTIPLY Opt> ::= 'END-MULTIPLY'
       final int PROD_ENDMULTIPLYOPT                                                       = 973;  // <END-MULTIPLY Opt> ::= 
       final int PROD_OPENSENT                                                             = 974;  // <Open Sent> ::= <Open Stm>
       final int PROD_OPENEMBED                                                            = 975;  // <Open Embed> ::= <Open Stm>
       final int PROD_OPENIMP                                                              = 976;  // <Open Imp> ::= <Open Stm>
       final int PROD_OPENSTM_OPEN                                                         = 977;  // <Open Stm> ::= OPEN <Open List>
       final int PROD_OPENLIST                                                             = 978;  // <Open List> ::= <Open List> <Open Entry>
       final int PROD_OPENLIST2                                                            = 979;  // <Open List> ::= <Open Entry>
       final int PROD_OPENENTRY_INPUT                                                      = 980;  // <Open Entry> ::= INPUT <Variables> <Open No Rewind>
       final int PROD_OPENENTRY_OUTPUT                                                     = 981;  // <Open Entry> ::= OUTPUT <Variables> <Open No Rewind>
       final int PROD_OPENENTRY_EXTEND                                                     = 982;  // <Open Entry> ::= EXTEND <Variables> <Open No Rewind>
       final int PROD_OPENENTRY_IMINUSO                                                    = 983;  // <Open Entry> ::= 'I-O' <Variables> <Open No Rewind>
       final int PROD_OPENNOREWIND_NO_REWIND                                               = 984;  // <Open No Rewind> ::= <WITH Opt> NO REWIND
       final int PROD_OPENNOREWIND                                                         = 985;  // <Open No Rewind> ::= 
       final int PROD_PERFORMSENT                                                          = 986;  // <Perform Sent> ::= <Perform Block> <END-PERFORM Opt>
       final int PROD_PERFORMSENT2                                                         = 987;  // <Perform Sent> ::= <Perform Stm>
       final int PROD_PERFORMSENT3                                                         = 988;  // <Perform Sent> ::= <Perform Loop>
       final int PROD_PERFORMEMBED_ENDMINUSPERFORM                                         = 989;  // <Perform Embed> ::= <Perform Block> 'END-PERFORM'
       final int PROD_PERFORMEMBED                                                         = 990;  // <Perform Embed> ::= <Perform Stm>
       final int PROD_PERFORMEMBED2                                                        = 991;  // <Perform Embed> ::= <Perform Loop>
       final int PROD_PERFORMIMP_ENDMINUSPERFORM                                           = 992;  // <Perform Imp> ::= <Perform Block> 'END-PERFORM'
       final int PROD_PERFORMIMP                                                           = 993;  // <Perform Imp> ::= <Perform Stm>
       final int PROD_PERFORMSTM_PERFORM                                                   = 994;  // <Perform Stm> ::= PERFORM <Identifier Range>
       final int PROD_PERFORMSTM_PERFORM_TIMES                                             = 995;  // <Perform Stm> ::= PERFORM <Identifier Range> <Numeric> TIMES
       final int PROD_PERFORMLOOP_PERFORM_UNTIL                                            = 996;  // <Perform Loop> ::= PERFORM <Identifier Range> <With Test> UNTIL <Boolean Exp>
       final int PROD_PERFORMLOOP_PERFORM_VARYING                                          = 997;  // <Perform Loop> ::= PERFORM <Identifier Range> <With Test> VARYING <Perform For List>
       final int PROD_PERFORMBLOCK_PERFORM_UNTIL                                           = 998;  // <Perform Block> ::= PERFORM <With Test> UNTIL <Boolean Exp> <Embed Stms>
       final int PROD_PERFORMBLOCK_PERFORM_VARYING                                         = 999;  // <Perform Block> ::= PERFORM <With Test> VARYING <Perform For List> <Embed Stms>
       final int PROD_PERFORMBLOCK_PERFORM_TIMES                                           = 1000;  // <Perform Block> ::= PERFORM <Numeric> TIMES <Embed Stms>
       final int PROD_WITHTEST_TEST                                                        = 1001;  // <With Test> ::= <WITH Opt> TEST <BEFORE AFTER>
       final int PROD_WITHTEST                                                             = 1002;  // <With Test> ::= 
       final int PROD_PERFORMFORLIST_AFTER                                                 = 1003;  // <Perform For List> ::= <Perform For List> AFTER <Perform For Range>
       final int PROD_PERFORMFORLIST                                                       = 1004;  // <Perform For List> ::= <Perform For Range>
       final int PROD_PERFORMFORRANGE_FROM_BY_UNTIL                                        = 1005;  // <Perform For Range> ::= <Variable> FROM <Numeric> BY <Numeric> UNTIL <Boolean Exp>
       final int PROD_ENDPERFORMOPT_ENDMINUSPERFORM                                        = 1006;  // <END-PERFORM Opt> ::= 'END-PERFORM'
       final int PROD_ENDPERFORMOPT                                                        = 1007;  // <END-PERFORM Opt> ::= 
       final int PROD_READSENT                                                             = 1008;  // <Read Sent> ::= <Read Stm> <Read Msg Clauses> <END-READ Opt>
       final int PROD_READSENT2                                                            = 1009;  // <Read Sent> ::= <Read Stm>
       final int PROD_READEMBED_ENDMINUSREAD                                               = 1010;  // <Read Embed> ::= <Read Stm> <Read Msg Clauses> 'END-READ'
       final int PROD_READEMBED                                                            = 1011;  // <Read Embed> ::= <Read Stm>
       final int PROD_READIMP                                                              = 1012;  // <Read Imp> ::= <Read Stm>
       final int PROD_READSTM_READ_IDENTIFIER                                              = 1013;  // <Read Stm> ::= READ Identifier <NEXT Opt> <RECORD Opt>
       final int PROD_READSTM_READ_IDENTIFIER_INTO                                         = 1014;  // <Read Stm> ::= READ Identifier <NEXT Opt> <RECORD Opt> INTO <Variable> <Read Key Opt>
       final int PROD_READMSGCLAUSES                                                       = 1015;  // <Read Msg Clauses> ::= <At End Clauses>
       final int PROD_READMSGCLAUSES2                                                      = 1016;  // <Read Msg Clauses> ::= <Invalid Key Clauses>
       final int PROD_READKEYOPT_KEY_IDENTIFIER                                            = 1017;  // <Read Key Opt> ::= KEY <IS Opt> Identifier
       final int PROD_READKEYOPT                                                           = 1018;  // <Read Key Opt> ::= 
       final int PROD_ENDREADOPT_ENDMINUSREAD                                              = 1019;  // <END-READ Opt> ::= 'END-READ'
       final int PROD_ENDREADOPT                                                           = 1020;  // <END-READ Opt> ::= 
       final int PROD_RELEASESENT                                                          = 1021;  // <Release Sent> ::= <Release Stm>
       final int PROD_RELEASEEMBED                                                         = 1022;  // <Release Embed> ::= <Release Stm>
       final int PROD_RELEASEIMP                                                           = 1023;  // <Release Imp> ::= <Release Stm>
       final int PROD_RELEASESTM_RELEASE_IDENTIFIER                                        = 1024;  // <Release Stm> ::= RELEASE Identifier
       final int PROD_RELEASESTM_RELEASE_IDENTIFIER_FROM_IDENTIFIER                        = 1025;  // <Release Stm> ::= RELEASE Identifier FROM Identifier
       final int PROD_RETURNSENT                                                           = 1026;  // <Return Sent> ::= <Return Stm>
       final int PROD_RETURNEMBED                                                          = 1027;  // <Return Embed> ::= <Return Stm>
       final int PROD_RETURNIMP                                                            = 1028;  // <Return Imp> ::= <Return Stm>
       final int PROD_RETURNSTM_RETURN_IDENTIFIER                                          = 1029;  // <Return Stm> ::= RETURN Identifier <RECORD Opt>
       final int PROD_RETURNSTM_RETURN_IDENTIFIER_INTO_IDENTIFIER                          = 1030;  // <Return Stm> ::= RETURN Identifier <RECORD Opt> INTO Identifier
       final int PROD_REWRITESENT                                                          = 1031;  // <Rewrite Sent> ::= <Rewrite Stm> <Invalid Key Clauses> <END-REWRITE Opt>
       final int PROD_REWRITESENT2                                                         = 1032;  // <Rewrite Sent> ::= <Rewrite Stm>
       final int PROD_REWRITEEMBED_ENDMINUSREWRITE                                         = 1033;  // <Rewrite Embed> ::= <Rewrite Stm> <Invalid Key Clauses> 'END-REWRITE'
       final int PROD_REWRITEEMBED                                                         = 1034;  // <Rewrite Embed> ::= <Rewrite Stm>
       final int PROD_REWRITEIMP                                                           = 1035;  // <Rewrite Imp> ::= <Rewrite Stm>
       final int PROD_REWRITESTM_REWRITE_IDENTIFIER                                        = 1036;  // <Rewrite Stm> ::= REWRITE Identifier
       final int PROD_REWRITESTM_REWRITE_IDENTIFIER_FROM_IDENTIFIER                        = 1037;  // <Rewrite Stm> ::= REWRITE Identifier FROM Identifier
       final int PROD_ENDREWRITEOPT_ENDMINUSREWRITE                                        = 1038;  // <END-REWRITE Opt> ::= 'END-REWRITE'
       final int PROD_ENDREWRITEOPT                                                        = 1039;  // <END-REWRITE Opt> ::= 
       final int PROD_SEARCHSENT                                                           = 1040;  // <Search Sent> ::= <Search Stm> <END-SEARCH Opt>
       final int PROD_SEARCHEMBED_ENDMINUSSEARCH                                           = 1041;  // <Search Embed> ::= <Search Stm> 'END-SEARCH'
       final int PROD_SEARCHIMP_ENDMINUSSEARCH                                             = 1042;  // <Search Imp> ::= <Search Stm> 'END-SEARCH'
       final int PROD_SEARCHSTM_SEARCH_IDENTIFIER                                          = 1043;  // <Search Stm> ::= SEARCH Identifier <Varying Opt> <At End Clauses> <When Clauses>
       final int PROD_SEARCHSTM_SEARCH_ALL_IDENTIFIER                                      = 1044;  // <Search Stm> ::= SEARCH ALL Identifier <At End Clauses> <When Clauses>
       final int PROD_VARYINGOPT_VARYING                                                   = 1045;  // <Varying Opt> ::= VARYING <Value>
       final int PROD_VARYINGOPT                                                           = 1046;  // <Varying Opt> ::= 
       final int PROD_ENDSEARCHOPT_ENDMINUSSEARCH                                          = 1047;  // <END-SEARCH Opt> ::= 'END-SEARCH'
       final int PROD_ENDSEARCHOPT                                                         = 1048;  // <END-SEARCH Opt> ::= 
       final int PROD_SENDSENT                                                             = 1049;  // <Send Sent> ::= <Send Stm>
       final int PROD_SENDEMBED                                                            = 1050;  // <Send Embed> ::= <Send Stm>
       final int PROD_SENDIMP                                                              = 1051;  // <Send Imp> ::= <Send Stm>
       final int PROD_SENDSTM_SEND_IDENTIFIER_FROM                                         = 1052;  // <Send Stm> ::= SEND Identifier FROM <Variable>
       final int PROD_SENDSTM_SEND_IDENTIFIER_FROM2                                        = 1053;  // <Send Stm> ::= SEND Identifier FROM <Variable> <Send With> <Send Spec> <Send Replacing Opt>
       final int PROD_SENDSTM_SEND_IDENTIFIER                                              = 1054;  // <Send Stm> ::= SEND Identifier <Send With> <Send Spec> <Send Replacing Opt>
       final int PROD_SENDWITH_IDENTIFIER                                                  = 1055;  // <Send With> ::= <WITH Opt> Identifier
       final int PROD_SENDWITH_ESI                                                         = 1056;  // <Send With> ::= <WITH Opt> ESI
       final int PROD_SENDWITH_EMI                                                         = 1057;  // <Send With> ::= <WITH Opt> EMI
       final int PROD_SENDWITH_EGI                                                         = 1058;  // <Send With> ::= <WITH Opt> EGI
       final int PROD_SENDSPEC                                                             = 1059;  // <Send Spec> ::= <BEFORE AFTER> <ADVANCING Opt> <Send Advance>
       final int PROD_SENDADVANCE                                                          = 1060;  // <Send Advance> ::= <Value> <LINES Opt>
       final int PROD_SENDADVANCE_PAGE                                                     = 1061;  // <Send Advance> ::= PAGE
       final int PROD_SENDREPLACINGOPT_REPLACING                                           = 1062;  // <Send Replacing Opt> ::= REPLACING <LINE Opt>
       final int PROD_SETSENT                                                              = 1063;  // <Set Sent> ::= <Set Stm>
       final int PROD_SETEMBED                                                             = 1064;  // <Set Embed> ::= <Set Stm>
       final int PROD_SETIMP                                                               = 1065;  // <Set Imp> ::= <Set Stm>
       final int PROD_SETSTM_SET_UP_BY                                                     = 1066;  // <Set Stm> ::= SET <Variables> UP BY <Value>
       final int PROD_SETSTM_SET_DOWN_BY                                                   = 1067;  // <Set Stm> ::= SET <Variables> DOWN BY <Value>
       final int PROD_SETSTM_SET_TO                                                        = 1068;  // <Set Stm> ::= SET <Variables> TO <Set Value>
       final int PROD_SETVALUE_ON                                                          = 1069;  // <Set Value> ::= ON
       final int PROD_SETVALUE_OFF                                                         = 1070;  // <Set Value> ::= OFF
       final int PROD_SETVALUE_TRUE                                                        = 1071;  // <Set Value> ::= TRUE
       final int PROD_SORTSENT                                                             = 1072;  // <Sort Sent> ::= <Sort Stm>
       final int PROD_SORTEMBED                                                            = 1073;  // <Sort Embed> ::= <Sort Stm>
       final int PROD_SORTIMP                                                              = 1074;  // <Sort Imp> ::= <Sort Stm>
       final int PROD_SORTSTM_SORT                                                         = 1075;  // <Sort Stm> ::= SORT <Value> <Sort Keys> <Sort Duplicates Opt> <COLLATING Opt> <Sort Source> <Sort Target>
       final int PROD_SORTDUPLICATESOPT_DUPLICATES                                         = 1076;  // <Sort Duplicates Opt> ::= <WITH Opt> DUPLICATES <IN Opt> <ORDER opt>
       final int PROD_SORTDUPLICATESOPT                                                    = 1077;  // <Sort Duplicates Opt> ::= 
       final int PROD_STARTSENT                                                            = 1078;  // <Start Sent> ::= <Start Stm> <Invalid Key Clauses> <END-START Opt>
       final int PROD_STARTSENT2                                                           = 1079;  // <Start Sent> ::= <Start Stm>
       final int PROD_STARTEMBED_ENDMINUSSTART                                             = 1080;  // <Start Embed> ::= <Start Stm> <Invalid Key Clauses> 'END-START'
       final int PROD_STARTEMBED                                                           = 1081;  // <Start Embed> ::= <Start Stm>
       final int PROD_STARTIMP                                                             = 1082;  // <Start Imp> ::= <Start Stm>
       final int PROD_STARTSTM_START_IDENTIFIER                                            = 1083;  // <Start Stm> ::= START Identifier <Start Key Opt>
       final int PROD_STARTKEYOPT_KEY_IDENTIFIER                                           = 1084;  // <Start Key Opt> ::= KEY <Compare Op> Identifier
       final int PROD_STARTKEYOPT                                                          = 1085;  // <Start Key Opt> ::= 
       final int PROD_ENDSTARTOPT_ENDMINUSSTART                                            = 1086;  // <END-START Opt> ::= 'END-START'
       final int PROD_ENDSTARTOPT                                                          = 1087;  // <END-START Opt> ::= 
       final int PROD_STOPSENT                                                             = 1088;  // <Stop Sent> ::= <Stop Stm>
       final int PROD_STOPEMBED                                                            = 1089;  // <Stop Embed> ::= <Stop Stm>
       final int PROD_STOPIMP                                                              = 1090;  // <Stop Imp> ::= <Stop Stm>
       final int PROD_STOPSTM_STOP_RUN                                                     = 1091;  // <Stop Stm> ::= STOP RUN
       final int PROD_STOPSTM_STOP                                                         = 1092;  // <Stop Stm> ::= STOP <Literal>
       final int PROD_STRINGSENT                                                           = 1093;  // <String Sent> ::= <String Stm> <Overflow Clauses> <END-STRING Opt>
       final int PROD_STRINGSENT2                                                          = 1094;  // <String Sent> ::= <String Stm>
       final int PROD_STRINGEMBED_ENDMINUSSTRING                                           = 1095;  // <String Embed> ::= <String Stm> <Overflow Clauses> 'END-STRING'
       final int PROD_STRINGEMBED                                                          = 1096;  // <String Embed> ::= <String Stm>
       final int PROD_STRINGIMP                                                            = 1097;  // <String Imp> ::= <String Stm>
       final int PROD_STRINGSTM_STRING_INTO                                                = 1098;  // <String Stm> ::= STRING <String Items> INTO <Variable> <Pointer Clause>
       final int PROD_STRINGITEMS                                                          = 1099;  // <String Items> ::= <String Item> <String Items>
       final int PROD_STRINGITEMS2                                                         = 1100;  // <String Items> ::= <String Item>
       final int PROD_STRINGITEM_DELIMITED                                                 = 1101;  // <String Item> ::= <Values> DELIMITED <BY Opt> <Value>
       final int PROD_ENDSTRINGOPT_ENDMINUSSTRING                                          = 1102;  // <END-STRING Opt> ::= 'END-STRING'
       final int PROD_ENDSTRINGOPT                                                         = 1103;  // <END-STRING Opt> ::= 
       final int PROD_SUBTRACTSENT                                                         = 1104;  // <Subtract Sent> ::= <Subtract Stm> <Size Clauses> <END-SUBTRACT Opt>
       final int PROD_SUBTRACTSENT2                                                        = 1105;  // <Subtract Sent> ::= <Subtract Stm>
       final int PROD_SUBTRACTEMBED_ENDMINUSSUBTRACT                                       = 1106;  // <Subtract Embed> ::= <Subtract Stm> <Size Clauses> 'END-SUBTRACT'
       final int PROD_SUBTRACTEMBED                                                        = 1107;  // <Subtract Embed> ::= <Subtract Stm>
       final int PROD_SUBTRACTIMP                                                          = 1108;  // <Subtract Imp> ::= <Subtract Stm>
       final int PROD_SUBTRACTSTM_SUBTRACT_FROM                                            = 1109;  // <Subtract Stm> ::= SUBTRACT <Values> FROM <Variables> <Giving Clause Opt>
       final int PROD_SUBTRACTSTM_SUBTRACT_FROM_IDENTIFIER                                 = 1110;  // <Subtract Stm> ::= SUBTRACT <CORRESPONDING> <Value> FROM Identifier
       final int PROD_ENDSUBTRACTOPT_ENDMINUSSUBTRACT                                      = 1111;  // <END-SUBTRACT Opt> ::= 'END-SUBTRACT'
       final int PROD_ENDSUBTRACTOPT                                                       = 1112;  // <END-SUBTRACT Opt> ::= 
       final int PROD_SUPPRESSSENT                                                         = 1113;  // <Suppress Sent> ::= <Suppress Stm>
       final int PROD_SUPPRESSEMBED                                                        = 1114;  // <Suppress Embed> ::= <Suppress Stm>
       final int PROD_SUPPRESSIMP                                                          = 1115;  // <Suppress Imp> ::= <Suppress Stm>
       final int PROD_SUPPRESSSTM_SUPPRESS                                                 = 1116;  // <Suppress Stm> ::= SUPPRESS <PRINTING Opt>
       final int PROD_TERMINATESENT                                                        = 1117;  // <Terminate Sent> ::= <Terminate Stm>
       final int PROD_TERMINATEEMBED                                                       = 1118;  // <Terminate Embed> ::= <Terminate Stm>
       final int PROD_TERMINATEIMP                                                         = 1119;  // <Terminate Imp> ::= <Terminate Stm>
       final int PROD_TERMINATESTM_TERMINATE                                               = 1120;  // <Terminate Stm> ::= TERMINATE <Identifiers>
       final int PROD_UNSTRINGSENT                                                         = 1121;  // <Unstring Sent> ::= <Unstring Stm> <Overflow Clauses> <END-UNSTRING Opt>
       final int PROD_UNSTRINGSENT2                                                        = 1122;  // <Unstring Sent> ::= <Unstring Stm>
       final int PROD_UNSTRINGEMBED_ENDMINUSUNSTRING                                       = 1123;  // <Unstring Embed> ::= <Unstring Stm> <Overflow Clauses> 'END-UNSTRING'
       final int PROD_UNSTRINGEMBED                                                        = 1124;  // <Unstring Embed> ::= <Unstring Stm>
       final int PROD_UNSTRINGIMP                                                          = 1125;  // <Unstring Imp> ::= <Unstring Stm>
       final int PROD_UNSTRINGSTM_UNSTRING_IDENTIFIER_INTO_IDENTIFIER                      = 1126;  // <Unstring Stm> ::= UNSTRING Identifier <Delimiter Clause> INTO Identifier <Unstring Options>
       final int PROD_DELIMITERCLAUSE_DELIMITED                                            = 1127;  // <Delimiter Clause> ::= DELIMITED <BY Opt> <Unstring Delimiter List>
       final int PROD_DELIMITERCLAUSE                                                      = 1128;  // <Delimiter Clause> ::= 
       final int PROD_UNSTRINGDELIMITERLIST_OR                                             = 1129;  // <Unstring Delimiter List> ::= <Unstring Delimiter List> OR <Unstring Delimiter>
       final int PROD_UNSTRINGDELIMITERLIST                                                = 1130;  // <Unstring Delimiter List> ::= <Unstring Delimiter>
       final int PROD_UNSTRINGDELIMITER                                                    = 1131;  // <Unstring Delimiter> ::= <ALL Opt> <Value>
       final int PROD_UNSTRINGOPTIONS                                                      = 1132;  // <Unstring Options> ::= <Unstring Options> <Unstring Option>
       final int PROD_UNSTRINGOPTIONS2                                                     = 1133;  // <Unstring Options> ::= 
       final int PROD_UNSTRINGOPTION_POINTER                                               = 1134;  // <Unstring Option> ::= <WITH Opt> POINTER <Variable>
       final int PROD_UNSTRINGOPTION_TALLYING_IDENTIFIER                                   = 1135;  // <Unstring Option> ::= TALLYING <IN Opt> Identifier
       final int PROD_ENDUNSTRINGOPT_ENDMINUSUNSTRING                                      = 1136;  // <END-UNSTRING Opt> ::= 'END-UNSTRING'
       final int PROD_ENDUNSTRINGOPT                                                       = 1137;  // <END-UNSTRING Opt> ::= 
       final int PROD_USESENT                                                              = 1138;  // <Use Sent> ::= <Use Stm>
       final int PROD_USEEMBED                                                             = 1139;  // <Use Embed> ::= <Use Stm>
       final int PROD_USEIMP                                                               = 1140;  // <Use Imp> ::= <Use Stm>
       final int PROD_USESTM_USE_AFTER_STANDARD_PROCEDURE_ON                               = 1141;  // <Use Stm> ::= USE <GLOBAL Opt> AFTER STANDARD <Use Proc Type> PROCEDURE ON <Use Access>
       final int PROD_USESTM_USE_BEFORE_REPORTING_IDENTIFIER                               = 1142;  // <Use Stm> ::= USE <GLOBAL Opt> BEFORE REPORTING Identifier
       final int PROD_USESTM_USE_DEBUGGING                                                 = 1143;  // <Use Stm> ::= USE <FOR Opt> DEBUGGING <ON Opt> <Use Debug>
       final int PROD_USEPROCTYPE_EXCEPTION                                                = 1144;  // <Use Proc Type> ::= EXCEPTION
       final int PROD_USEPROCTYPE_ERROR                                                    = 1145;  // <Use Proc Type> ::= ERROR
       final int PROD_USEACCESS_INPUT                                                      = 1146;  // <Use Access> ::= INPUT
       final int PROD_USEACCESS_OUTPUT                                                     = 1147;  // <Use Access> ::= OUTPUT
       final int PROD_USEACCESS_IMINUSO                                                    = 1148;  // <Use Access> ::= 'I-O'
       final int PROD_USEACCESS_EXTEND                                                     = 1149;  // <Use Access> ::= EXTEND
       final int PROD_USEACCESS                                                            = 1150;  // <Use Access> ::= <Value>
       final int PROD_USEDEBUG_ALL_IDENTIFIER                                              = 1151;  // <Use Debug> ::= ALL <REFERENCES Opt> <OF Opt> Identifier
       final int PROD_USEDEBUG_ALL_PROCEDURES                                              = 1152;  // <Use Debug> ::= ALL PROCEDURES
       final int PROD_USEDEBUG                                                             = 1153;  // <Use Debug> ::= <Value>
       final int PROD_WRITESENT                                                            = 1154;  // <Write Sent> ::= <Write Stm> <Invalid Key Clauses> <END-WRITE Opt>
       final int PROD_WRITESENT2                                                           = 1155;  // <Write Sent> ::= <Write Stm> <AT EOP Clauses> <END-WRITE Opt>
       final int PROD_WRITESENT3                                                           = 1156;  // <Write Sent> ::= <Write Stm>
       final int PROD_WRITEEMBED_ENDMINUSWRITE                                             = 1157;  // <Write Embed> ::= <Write Stm> <Invalid Key Clauses> 'END-WRITE'
       final int PROD_WRITEEMBED_ENDMINUSWRITE2                                            = 1158;  // <Write Embed> ::= <Write Stm> <AT EOP Clauses> 'END-WRITE'
       final int PROD_WRITEEMBED                                                           = 1159;  // <Write Embed> ::= <Write Stm>
       final int PROD_WRITEIMP                                                             = 1160;  // <Write Imp> ::= <Write Stm>
       final int PROD_WRITESTM_WRITE_IDENTIFIER                                            = 1161;  // <Write Stm> ::= WRITE Identifier <Write Options>
       final int PROD_WRITESTM_WRITE_IDENTIFIER_FROM_IDENTIFIER                            = 1162;  // <Write Stm> ::= WRITE Identifier FROM Identifier <Write Options>
       final int PROD_WRITEOPTIONS                                                         = 1163;  // <Write Options> ::= <BEFORE AFTER> <ADVANCING Opt> <Write Advance>
       final int PROD_WRITEADVANCE                                                         = 1164;  // <Write Advance> ::= <Value> <LINES Opt>
       final int PROD_WRITEADVANCE_PAGE                                                    = 1165;  // <Write Advance> ::= PAGE
       final int PROD_ENDWRITEOPT_ENDMINUSWRITE                                            = 1166;  // <END-WRITE Opt> ::= 'END-WRITE'
       final int PROD_ENDWRITEOPT                                                          = 1167;  // <END-WRITE Opt> ::= 
    };

	//------------------------- Preprocessor ---------------------------

	/**
	 * Performs some necessary preprocessing for the text file. Actually opens the
	 * file, filters it and writes a new file _textToParse+".structorizer" to the
	 * same directory, which is then actually parsed.
	 * The preprocessed file will always be saved with UTF-8 encoding.
	 * @param _textToParse - name (path) of the source file
	 * @param _encoding - the expected encoding of the source file.
	 * @return The File object associated with the preprocessed source file.
	 */
	@Override
	protected File prepareTextfile(String _textToParse, String _encoding)
	{
		File interm = null;
		try
		{
			File file = new File(_textToParse);
			HashMap<String, String> defines = new LinkedHashMap<String, String>();
			DataInputStream in = new DataInputStream(new FileInputStream(_textToParse));
			// START KGU#193 2016-05-04
			BufferedReader br = new BufferedReader(new InputStreamReader(in, _encoding));
			// END KGU#193 2016-05-04
			String strLine;
			String srcCode = new String();
			//Read File Line By Line
			// Preprocessor directives are not tolerated by the grammar, so drop them or try to
			// do the #define replacements (at least roughly...)
			while ((strLine = br.readLine()) != null)   
			{
				// TODO: Place preprocessing of strLine here if necessary
				srcCode += strLine + "\n";
			}
			//Close the input stream
			in.close();

			//System.out.println(srcCode);

			// trim and save as new file
			interm = new File(_textToParse + ".structorizer");
			OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(interm), "UTF-8");
			ow.write(srcCode.trim()+"\n");
			ow.close();
		}
		catch (Exception e) 
		{
			System.out.println(e.getMessage());
		}
		return interm;
	}

	//---------------------- Build methods for structograms ---------------------------

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
			String ruleHead = _reduction.getParent().getHead().toString();
			int ruleId = _reduction.getParent().getTableIndex();
			//System.out.println("buildNSD_R(" + rule + ", " + _parentNode.parent + ")...");

			/* -------- Begin code example for tree analysis and build -------- */
//			if (
//					// Assignment or procedure call?
//					ruleId == RuleConstants.PROD_OPASSIGN_EQ
//					||
//					ruleId == RuleConstants.PROD_VALUE_ID_LPAREN_RPAREN
//					||
//					ruleId == RuleConstants.PROD_VALUE_ID_LPAREN_RPAREN2
//					||
//					ruleId == RuleConstants.PROD_VALUE_ID_LPAREN_RPAREN
//					)
//			{
//				// Simply convet it as text and create an instruction. In case of a call
//				// we'll try to transmute it after all subroutines will have been parsed.
//				String content = new String();
//				content = getContent_R(_reduction, content).trim();
//				//System.out.println(ruleName + ": " + content);
//				// In case of a variable declaration get rid of the trailing semicolon
//				//if (content.endsWith(";")) {
//				//	content = content.substring(0, content.length() - 1).trim();
//				//}
//				_parentNode.addElement(new Instruction(translateContent(content)));
//			}
//			else if (ruleHead.equals("<Decls>") {
//				...
//			}
			/* -------- End code example for tree analysis and build -------- */
			// Block...?
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
	 * Helper method to retrieve and compose the text of the given reduction, combine it with previously
	 * assembled string _content and adapt it to syntactical conventions of Structorizer. Finally return
	 * the text phrase.
	 * @param _content - A string already assembled, may be used as prefix, ignored or combined in another
	 * way 
	 * @return composed and translated text.
	 */
	private String translateContent(String _content)
	{
		String output = getKeyword("output");
		String input = getKeyword("input");
		_content = _content.replaceAll(BString.breakup("printf")+"[ ((](.*?)[))]", output+"$1");
		_content = _content.replaceAll(BString.breakup("scanf")+"[ ((](.*?),[ ]*[&]?(.*?)[))]", input+"$2");
		
		//System.out.println(_content);
		
		/*
		 _content:=ReplaceEntities(_content);
		*/
		
		// Convert the pseudo function back to array initializers
//		int posIni = _content.indexOf(arrayIniFunc);
//		if (posIni >= 0) {
//			StringList items = Element.splitExpressionList(_content.substring(posIni + arrayIniFunc.length()), ",", true);
//			_content = _content.substring(0, posIni) + "{" + items.subSequence(0, items.count()-1).concatenate(", ") +
//					"}" + items.get(items.count()-1).substring(1);
//		}
		
		//_content = BString.replace(_content, ":="," \u2190 ");
		//_content = BString.replace(_content, " = "," <- "); already done by getContent_R()!

		return _content.trim();
	}
	
	@Override
	protected String getContent_R(Reduction _reduction, String _content)
	{
		for(int i=0; i<_reduction.size(); i++)
		{
			Token token = _reduction.get(i);
			/* -------- Begin code example for text retrieval and translation -------- */
			switch (token.getType()) 
			{
			case NON_TERMINAL:
//				int ruleId = _reduction.getParent().getTableIndex();
//				_content = getContent_R(token.asReduction(), _content);	
				break;
			case CONTENT:
//				{
//					String toAdd = "";
//					int idx = token.getTableIndex();
//					switch (idx) {
//					case SymbolConstants.SYM_EXCLAM:
//						_content += " not ";
//						break;
//					...
//					}
//				}
				break;
			default:
				break;
			}
			/* -------- End code example for text retrieval and translation -------- */
		}
		
		return _content;
	}

	//------------------------- Postprocessor ---------------------------

	// TODO Use this subclassable hook if some postprocessing for the generated roots is necessary
//	/* (non-Javadoc)
//	 * @see lu.fisch.structorizer.parsers.CodeParser#subclassUpdateRoot(lu.fisch.structorizer.elements.Root, java.lang.String)
//	 */
//	@Override
//	protected void subclassUpdateRoot(Root aRoot, String textToParse) {
//		// THIS CODE EXAMPLE IS FROM THE CPARSER (derives a name for the main program)
//		if (aRoot.getMethodName().equals("main")) {
//			if (aRoot.getParameterNames().count() == 0) {
//				String fileName = new File(textToParse).getName();
//				if (fileName.contains(".")) {
//					fileName = fileName.substring(0, fileName.indexOf('.'));
//				}
//				if (this.optionUpperCaseProgName) {
//					fileName = fileName.toUpperCase();
//				}
//				aRoot.setText(fileName);
//			}
//			aRoot.isProgram = true;
//		}
//	}

}

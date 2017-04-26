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
 *      Description:    Class to parse an ANSI C file.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.03.02      First Issue
 *      Kay Gürtzig     2017.03.06      Bug in diagram synthesis mended (do-while, switch)
 *      Kay Gürtzig     2017.03.26      Fix #384: New temp file mechanism for the prepared text file
 *      Kay Gürtzig     2017.03.27      Issue #354: number literal suffixes and scanf import fixed
 *      Kay Gürtzig     2017.03.31      Enh. #388: import of constants supported
 *      Kay Gürtzig     2017.04.11      Enh. #389: Global definitions now induce import CALLs to a global program,
 *                                      typedef mechanism implemented, grammar enhancements 
 *      Kay Gürtzig     2017.04.16      Issues #354, #389: Grammar revisions, correction in synthesis of
 *                                      function declarations, mechanism to ensure sensible naming of the global Root
 *      Kay Gürtzig     2017.04.26      prepareTextfile() now eliminates void casts (the grammar doesn't cope with them) 
 *
 ******************************************************************************************************
 *
 *     Comment:		
 *     Licensed Material - Property of Matthew Hawkins (hawkini@4email.net)<br>
 *     GOLDParser - code ported from VB - Author Devin Cook. All rights reserved.<br>
 *     Modifications to this code are allowed as it is a helper class to use the engine.<br>
 *     Template File:  Java-MatthewHawkins.pgt<br>
 *     Author:         Matthew Hawkins<br>
 *     Description:    A Sample class, takes in a file and runs the GOLDParser engine on it.<br>
 *
 ******************************************************************************************************/

import java.awt.Color;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;

import com.creativewidgetworks.goldparser.engine.*;
import com.creativewidgetworks.goldparser.engine.enums.SymbolType;

import lu.fisch.structorizer.elements.Alternative;
import lu.fisch.structorizer.elements.Call;
import lu.fisch.structorizer.elements.Case;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.ILoop;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.elements.Jump;
import lu.fisch.structorizer.elements.Repeat;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.structorizer.elements.While;
import lu.fisch.utils.StringList;

/**
 * Code import parser class of Structorizer 3.27, based on GOLDParser 5.0 for the %Name% language.
 * This file cntains grammar-specific constants and individual routines to build
 * structograms (Nassi-Shneiderman diagrams) from the parsing tree. 
 * @author Kay Gürtzig
 */
public class CParser extends CodeParser
{
	
	// DEfault diagram name for an importable program diagram with global definitions
	private static final String defaultGlobalName = "GlobalDefinitions";

	// This is a switch for debugging - if set to true then the build process will be logged to System.out
	private boolean debugprint = true;
	
	//---------------------- Grammar specification ---------------------------

	@Override
	protected final String getCompiledGrammar()
	{
		return "C-ANSIplus.egt";
	}
	
	@Override
	protected final String getGrammarTableName() {
		return "ANSI-Cplus";
	}
	
	/**
	 * If this flag is set then program names derived from file name will be made uppercase
	 * This default will be initialized in consistency with the Analyser check 
	 */
	private boolean optionUpperCaseProgName = false;
	
	//---------------------- File Filter configuration ---------------------------
	
	@Override
	public String getDialogTitle() {
		return "ANSI C";
	}

	@Override
	protected String getFileDescription() {
		return "ANSI C Source Code";
	}

	@Override
	public String[] getFileExtensions() {
		final String[] exts = { "c" };
		return exts;
	}

	//------------------- Grammar table constants DON'T MODIFY! ---------------------------
	
	// Symbolic constants naming the table indices of the symbols of the grammar 
	@SuppressWarnings("unused")
	private interface SymbolConstants 
	{
		final int SYM_EOF           =   0;  // (EOF)
		final int SYM_ERROR         =   1;  // (Error)
		final int SYM_COMMENT       =   2;  // Comment
		final int SYM_NEWLINE       =   3;  // NewLine
		final int SYM_WHITESPACE    =   4;  // Whitespace
		final int SYM_TIMESDIV      =   5;  // '*/'
		final int SYM_DIVTIMES      =   6;  // '/*'
		final int SYM_DIVDIV        =   7;  // '//'
		final int SYM_MINUS         =   8;  // '-'
		final int SYM_MINUSMINUS    =   9;  // '--'
		final int SYM_EXCLAM        =  10;  // '!'
		final int SYM_EXCLAMEQ      =  11;  // '!='
		final int SYM_PERCENT       =  12;  // '%'
		final int SYM_AMP           =  13;  // '&'
		final int SYM_AMPAMP        =  14;  // '&&'
		final int SYM_AMPEQ         =  15;  // '&='
		final int SYM_LPAREN        =  16;  // '('
		final int SYM_RPAREN        =  17;  // ')'
		final int SYM_TIMES         =  18;  // '*'
		final int SYM_TIMESEQ       =  19;  // '*='
		final int SYM_COMMA         =  20;  // ','
		final int SYM_DOT           =  21;  // '.'
		final int SYM_DIV           =  22;  // '/'
		final int SYM_DIVEQ         =  23;  // '/='
		final int SYM_COLON         =  24;  // ':'
		final int SYM_SEMI          =  25;  // ';'
		final int SYM_QUESTION      =  26;  // '?'
		final int SYM_LBRACKET      =  27;  // '['
		final int SYM_RBRACKET      =  28;  // ']'
		final int SYM_CARET         =  29;  // '^'
		final int SYM_CARETEQ       =  30;  // '^='
		final int SYM_LBRACE        =  31;  // '{'
		final int SYM_PIPE          =  32;  // '|'
		final int SYM_PIPEPIPE      =  33;  // '||'
		final int SYM_PIPEEQ        =  34;  // '|='
		final int SYM_RBRACE        =  35;  // '}'
		final int SYM_TILDE         =  36;  // '~'
		final int SYM_PLUS          =  37;  // '+'
		final int SYM_PLUSPLUS      =  38;  // '++'
		final int SYM_PLUSEQ        =  39;  // '+='
		final int SYM_LT            =  40;  // '<'
		final int SYM_LTLT          =  41;  // '<<'
		final int SYM_LTLTEQ        =  42;  // '<<='
		final int SYM_LTEQ          =  43;  // '<='
		final int SYM_EQ            =  44;  // '='
		final int SYM_MINUSEQ       =  45;  // '-='
		final int SYM_EQEQ          =  46;  // '=='
		final int SYM_GT            =  47;  // '>'
		final int SYM_MINUSGT       =  48;  // '->'
		final int SYM_GTEQ          =  49;  // '>='
		final int SYM_GTGT          =  50;  // '>>'
		final int SYM_GTGTEQ        =  51;  // '>>='
		final int SYM_AUTO          =  52;  // auto
		final int SYM_BREAK         =  53;  // break
		final int SYM_CASE          =  54;  // case
		final int SYM_CHAR          =  55;  // char
		final int SYM_CHARLITERAL   =  56;  // CharLiteral
		final int SYM_CONST         =  57;  // const
		final int SYM_CONTINUE      =  58;  // continue
		final int SYM_DECLITERAL    =  59;  // DecLiteral
		final int SYM_DEFAULT       =  60;  // default
		final int SYM_DO            =  61;  // do
		final int SYM_DOUBLE        =  62;  // double
		final int SYM_ELSE          =  63;  // else
		final int SYM_ENUM          =  64;  // enum
		final int SYM_EXTERN        =  65;  // extern
		final int SYM_FLOAT         =  66;  // float
		final int SYM_FLOATLITERAL  =  67;  // FloatLiteral
		final int SYM_FOR           =  68;  // for
		final int SYM_GOTO          =  69;  // goto
		final int SYM_HEXLITERAL    =  70;  // HexLiteral
		final int SYM_ID            =  71;  // Id
		final int SYM_IF            =  72;  // if
		final int SYM_INT           =  73;  // int
		final int SYM_LONG          =  74;  // long
		final int SYM_OCTLITERAL    =  75;  // OctLiteral
		final int SYM_REGISTER      =  76;  // register
		final int SYM_RETURN        =  77;  // return
		final int SYM_SHORT         =  78;  // short
		final int SYM_SIGNED        =  79;  // signed
		final int SYM_SIZEOF        =  80;  // sizeof
		final int SYM_STATIC        =  81;  // static
		final int SYM_STRINGLITERAL =  82;  // StringLiteral
		final int SYM_STRUCT        =  83;  // struct
		final int SYM_SWITCH        =  84;  // switch
		final int SYM_TYPEDEF       =  85;  // typedef
		final int SYM_UNION         =  86;  // union
		final int SYM_UNSIGNED      =  87;  // unsigned
		final int SYM_USER_TYPE_001 =  88;  // 'user_type_001'
		final int SYM_USER_TYPE_002 =  89;  // 'user_type_002'
		final int SYM_USER_TYPE_003 =  90;  // 'user_type_003'
		final int SYM_USER_TYPE_004 =  91;  // 'user_type_004'
		final int SYM_USER_TYPE_005 =  92;  // 'user_type_005'
		final int SYM_USER_TYPE_006 =  93;  // 'user_type_006'
		final int SYM_USER_TYPE_007 =  94;  // 'user_type_007'
		final int SYM_USER_TYPE_009 =  95;  // 'user_type_009'
		final int SYM_USER_TYPE_010 =  96;  // 'user_type_010'
		final int SYM_USER_TYPE_011 =  97;  // 'user_type_011'
		final int SYM_USER_TYPE_012 =  98;  // 'user_type_012'
		final int SYM_USER_TYPE_013 =  99;  // 'user_type_013'
		final int SYM_USER_TYPE_014 = 100;  // 'user_type_014'
		final int SYM_USER_TYPE_015 = 101;  // 'user_type_015'
		final int SYM_USER_TYPE_016 = 102;  // 'user_type_016'
		final int SYM_USER_TYPE_017 = 103;  // 'user_type_017'
		final int SYM_USER_TYPE_018 = 104;  // 'user_type_018'
		final int SYM_USER_TYPE_019 = 105;  // 'user_type_019'
		final int SYM_USER_TYPE_020 = 106;  // 'user_type_020'
		final int SYM_USER_TYPE_021 = 107;  // 'user_type_021'
		final int SYM_USER_TYPE_022 = 108;  // 'user_type_022'
		final int SYM_USER_TYPE_023 = 109;  // 'user_type_023'
		final int SYM_USER_TYPE_024 = 110;  // 'user_type_024'
		final int SYM_USER_TYPE_025 = 111;  // 'user_type_025'
		final int SYM_USER_TYPE_026 = 112;  // 'user_type_026'
		final int SYM_USER_TYPE_027 = 113;  // 'user_type_027'
		final int SYM_USER_TYPE_028 = 114;  // 'user_type_028'
		final int SYM_USER_TYPE_029 = 115;  // 'user_type_029'
		final int SYM_USER_TYPE_030 = 116;  // 'user_type_030'
		final int SYM_VOID          = 117;  // void
		final int SYM_VOLATILE      = 118;  // volatile
		final int SYM_WCHAR_T       = 119;  // 'wchar_t'
		final int SYM_WHILE         = 120;  // while
		final int SYM_ARG           = 121;  // <Arg>
		final int SYM_ARRAY         = 122;  // <Array>
		final int SYM_BASE          = 123;  // <Base>
		final int SYM_BLOCK         = 124;  // <Block>
		final int SYM_CALLID        = 125;  // <Call Id>
		final int SYM_CASESTMS      = 126;  // <Case Stms>
		final int SYM_CONSTMOD      = 127;  // <ConstMod>
		final int SYM_CONSTPOINTERS = 128;  // <ConstPointers>
		final int SYM_CONSTTYPE     = 129;  // <ConstType>
		final int SYM_DECL          = 130;  // <Decl>
		final int SYM_DECLEND       = 131;  // <Decl End>
		final int SYM_DECLSTMLIST   = 132;  // <Decl Stm List>
		final int SYM_ENUMDECL      = 133;  // <Enum Decl>
		final int SYM_ENUMDEF       = 134;  // <Enum Def>
		final int SYM_ENUMVAL       = 135;  // <Enum Val>
		final int SYM_EXPR          = 136;  // <Expr>
		final int SYM_EXPRINI       = 137;  // <ExprIni>
		final int SYM_EXTDECL       = 138;  // <ExtDecl>
		final int SYM_EXTDECLS      = 139;  // <ExtDecls>
		final int SYM_FUNCDECL      = 140;  // <Func Decl>
		final int SYM_FUNCID        = 141;  // <Func ID>
		final int SYM_FUNCPROTO     = 142;  // <Func Proto>
		final int SYM_IDLIST        = 143;  // <Id List>
		final int SYM_INITIALIZER   = 144;  // <Initializer>
		final int SYM_MOD           = 145;  // <Mod>
		final int SYM_NORMALSTM     = 146;  // <Normal Stm>
		final int SYM_OPADD         = 147;  // <Op Add>
		final int SYM_OPAND         = 148;  // <Op And>
		final int SYM_OPASSIGN      = 149;  // <Op Assign>
		final int SYM_OPBINAND      = 150;  // <Op BinAND>
		final int SYM_OPBINOR       = 151;  // <Op BinOR>
		final int SYM_OPBINXOR      = 152;  // <Op BinXOR>
		final int SYM_OPCOMPARE     = 153;  // <Op Compare>
		final int SYM_OPEQUATE      = 154;  // <Op Equate>
		final int SYM_OPIF          = 155;  // <Op If>
		final int SYM_OPMULT        = 156;  // <Op Mult>
		final int SYM_OPOR          = 157;  // <Op Or>
		final int SYM_OPPOINTER     = 158;  // <Op Pointer>
		final int SYM_OPSHIFT       = 159;  // <Op Shift>
		final int SYM_OPUNARY       = 160;  // <Op Unary>
		final int SYM_PARAM         = 161;  // <Param>
		final int SYM_PARAMS        = 162;  // <Params>
		final int SYM_POINTERS      = 163;  // <Pointers>
		final int SYM_SCALAR        = 164;  // <Scalar>
		final int SYM_SIGN          = 165;  // <Sign>
		final int SYM_STM           = 166;  // <Stm>
		final int SYM_STMLIST       = 167;  // <Stm List>
		final int SYM_STRUCTDECL    = 168;  // <Struct Decl>
		final int SYM_STRUCTDEF     = 169;  // <Struct Def>
		final int SYM_THENSTM       = 170;  // <Then Stm>
		final int SYM_TYPE          = 171;  // <Type>
		final int SYM_TYPEDEFDECL   = 172;  // <Typedef Decl>
		final int SYM_TYPES         = 173;  // <Types>
		final int SYM_UNIONDECL     = 174;  // <Union Decl>
		final int SYM_USERTYPE      = 175;  // <User Type>
		final int SYM_VALUE         = 176;  // <Value>
		final int SYM_VAR           = 177;  // <Var>
		final int SYM_VARDECL       = 178;  // <Var Decl>
		final int SYM_VARITEM       = 179;  // <Var Item>
		final int SYM_VARLIST       = 180;  // <Var List>
	};

	// Symbolic constants naming the table indices of the grammar rules
	@SuppressWarnings("unused")
	private interface RuleConstants
	{
		final int PROD_EXTDECLS                                     =   0;  // <ExtDecls> ::= <ExtDecl> <ExtDecls>
		final int PROD_EXTDECLS2                                    =   1;  // <ExtDecls> ::= 
		final int PROD_EXTDECL                                      =   2;  // <ExtDecl> ::= <Func Decl>
		final int PROD_EXTDECL2                                     =   3;  // <ExtDecl> ::= <Func Proto>
		final int PROD_EXTDECL3                                     =   4;  // <ExtDecl> ::= <Decl>
		final int PROD_DECL                                         =   5;  // <Decl> ::= <Struct Decl>
		final int PROD_DECL2                                        =   6;  // <Decl> ::= <Union Decl>
		final int PROD_DECL3                                        =   7;  // <Decl> ::= <Enum Decl>
		final int PROD_DECL4                                        =   8;  // <Decl> ::= <Var Decl>
		final int PROD_DECL5                                        =   9;  // <Decl> ::= <Typedef Decl>
		final int PROD_FUNCPROTO_LPAREN_RPAREN_SEMI                 =  10;  // <Func Proto> ::= <Func ID> '(' <Types> ')' ';'
		final int PROD_FUNCPROTO_LPAREN_RPAREN_SEMI2                =  11;  // <Func Proto> ::= <Func ID> '(' <Params> ')' ';'
		final int PROD_FUNCPROTO_LPAREN_VOID_RPAREN_SEMI            =  12;  // <Func Proto> ::= <Func ID> '(' void ')' ';'
		final int PROD_FUNCPROTO_LPAREN_RPAREN_SEMI3                =  13;  // <Func Proto> ::= <Func ID> '(' ')' ';'
		final int PROD_FUNCDECL_LPAREN_RPAREN                       =  14;  // <Func Decl> ::= <Func ID> '(' <Params> ')' <Block>
		final int PROD_FUNCDECL_LPAREN_RPAREN2                      =  15;  // <Func Decl> ::= <Func ID> '(' <Id List> ')' <Struct Def> <Block>
		final int PROD_FUNCDECL_LPAREN_VOID_RPAREN                  =  16;  // <Func Decl> ::= <Func ID> '(' void ')' <Block>
		final int PROD_FUNCDECL_LPAREN_RPAREN3                      =  17;  // <Func Decl> ::= <Func ID> '(' ')' <Block>
		final int PROD_PARAMS_COMMA                                 =  18;  // <Params> ::= <Param> ',' <Params>
		final int PROD_PARAMS                                       =  19;  // <Params> ::= <Param>
		final int PROD_PARAM_ID                                     =  20;  // <Param> ::= <ConstType> Id <Array>
		final int PROD_TYPES_COMMA                                  =  21;  // <Types> ::= <ConstType> ',' <Types>
		final int PROD_TYPES                                        =  22;  // <Types> ::= <ConstType>
		final int PROD_IDLIST_ID_COMMA                              =  23;  // <Id List> ::= Id ',' <Id List>
		final int PROD_IDLIST_ID                                    =  24;  // <Id List> ::= Id
		final int PROD_FUNCID_ID                                    =  25;  // <Func ID> ::= <ConstMod> <Type> Id
		final int PROD_FUNCID_CONST_ID                              =  26;  // <Func ID> ::= <Mod> const <Type> Id
		final int PROD_FUNCID_ID2                                   =  27;  // <Func ID> ::= <ConstType> Id
		final int PROD_FUNCID_VOID_ID                               =  28;  // <Func ID> ::= void Id
		final int PROD_FUNCID_VOID_ID2                              =  29;  // <Func ID> ::= <Mod> void Id
		final int PROD_FUNCID_ID3                                   =  30;  // <Func ID> ::= Id
		final int PROD_TYPEDEFDECL_TYPEDEF                          =  31;  // <Typedef Decl> ::= typedef <Var Decl>
		final int PROD_STRUCTDECL_STRUCT_ID_LBRACE_RBRACE           =  32;  // <Struct Decl> ::= struct Id '{' <Struct Def> '}' <Decl End>
		final int PROD_UNIONDECL_UNION_ID_LBRACE_RBRACE             =  33;  // <Union Decl> ::= union Id '{' <Struct Def> '}' <Decl End>
		final int PROD_STRUCTDEF                                    =  34;  // <Struct Def> ::= <Var Decl> <Struct Def>
		final int PROD_STRUCTDEF2                                   =  35;  // <Struct Def> ::= <Var Decl>
		final int PROD_DECLEND_SEMI                                 =  36;  // <Decl End> ::= ';'
		final int PROD_DECLEND_SEMI2                                =  37;  // <Decl End> ::= <Var Item> <Var List> ';'
		final int PROD_VARDECL_SEMI                                 =  38;  // <Var Decl> ::= <ConstMod> <Type> <Var> <Var List> ';'
		final int PROD_VARDECL_CONST_SEMI                           =  39;  // <Var Decl> ::= <Mod> const <Type> <Var> <Var List> ';'
		final int PROD_VARDECL_SEMI2                                =  40;  // <Var Decl> ::= <ConstType> <Var> <Var List> ';'
		final int PROD_VARDECL_SEMI3                                =  41;  // <Var Decl> ::= <ConstMod> <Var> <Var List> ';'
		final int PROD_VARDECL_CONST_SEMI2                          =  42;  // <Var Decl> ::= const <Var> <Var List> ';'
		final int PROD_VAR_ID                                       =  43;  // <Var> ::= Id <Array>
		final int PROD_VAR_ID_EQ                                    =  44;  // <Var> ::= Id <Array> '=' <Initializer>
		final int PROD_ARRAY_LBRACKET_RBRACKET                      =  45;  // <Array> ::= '[' <Expr> ']'
		final int PROD_ARRAY_LBRACKET_RBRACKET2                     =  46;  // <Array> ::= '[' ']'
		final int PROD_ARRAY                                        =  47;  // <Array> ::= 
		final int PROD_VARLIST_COMMA                                =  48;  // <Var List> ::= ',' <Var Item> <Var List>
		final int PROD_VARLIST                                      =  49;  // <Var List> ::= 
		final int PROD_VARITEM                                      =  50;  // <Var Item> ::= <Pointers> <Var>
		final int PROD_CONSTMOD_CONST                               =  51;  // <ConstMod> ::= const <Mod>
		final int PROD_CONSTMOD                                     =  52;  // <ConstMod> ::= <Mod>
		final int PROD_MOD_EXTERN                                   =  53;  // <Mod> ::= extern
		final int PROD_MOD_STATIC                                   =  54;  // <Mod> ::= static
		final int PROD_MOD_REGISTER                                 =  55;  // <Mod> ::= register
		final int PROD_MOD_AUTO                                     =  56;  // <Mod> ::= auto
		final int PROD_MOD_VOLATILE                                 =  57;  // <Mod> ::= volatile
		final int PROD_ENUMDECL_ENUM_ID_LBRACE_RBRACE               =  58;  // <Enum Decl> ::= enum Id '{' <Enum Def> '}' <Decl End>
		final int PROD_ENUMDEF_COMMA                                =  59;  // <Enum Def> ::= <Enum Val> ',' <Enum Def>
		final int PROD_ENUMDEF                                      =  60;  // <Enum Def> ::= <Enum Val>
		final int PROD_ENUMVAL_ID                                   =  61;  // <Enum Val> ::= Id
		final int PROD_ENUMVAL_ID_EQ_OCTLITERAL                     =  62;  // <Enum Val> ::= Id '=' OctLiteral
		final int PROD_ENUMVAL_ID_EQ_HEXLITERAL                     =  63;  // <Enum Val> ::= Id '=' HexLiteral
		final int PROD_ENUMVAL_ID_EQ_DECLITERAL                     =  64;  // <Enum Val> ::= Id '=' DecLiteral
		final int PROD_CONSTTYPE_CONST                              =  65;  // <ConstType> ::= const <Type>
		final int PROD_CONSTTYPE                                    =  66;  // <ConstType> ::= <Type>
		final int PROD_TYPE                                         =  67;  // <Type> ::= <Base> <Pointers>
		final int PROD_BASE                                         =  68;  // <Base> ::= <Sign> <Scalar>
		final int PROD_BASE_STRUCT_ID                               =  69;  // <Base> ::= struct Id
		final int PROD_BASE_STRUCT_LBRACE_RBRACE                    =  70;  // <Base> ::= struct '{' <Struct Def> '}'
		final int PROD_BASE_UNION_ID                                =  71;  // <Base> ::= union Id
		final int PROD_BASE_UNION_LBRACE_RBRACE                     =  72;  // <Base> ::= union '{' <Struct Def> '}'
		final int PROD_BASE_ENUM_ID                                 =  73;  // <Base> ::= enum Id
		final int PROD_BASE_ENUM_LBRACE_RBRACE                      =  74;  // <Base> ::= enum '{' <Enum Def> '}'
		final int PROD_BASE_VOID_TIMES                              =  75;  // <Base> ::= void '*'
		final int PROD_BASE2                                        =  76;  // <Base> ::= <User Type>
		final int PROD_USERTYPE_USER_TYPE_001                       =  77;  // <User Type> ::= 'user_type_001'
		final int PROD_USERTYPE_USER_TYPE_002                       =  78;  // <User Type> ::= 'user_type_002'
		final int PROD_USERTYPE_USER_TYPE_003                       =  79;  // <User Type> ::= 'user_type_003'
		final int PROD_USERTYPE_USER_TYPE_004                       =  80;  // <User Type> ::= 'user_type_004'
		final int PROD_USERTYPE_USER_TYPE_005                       =  81;  // <User Type> ::= 'user_type_005'
		final int PROD_USERTYPE_USER_TYPE_006                       =  82;  // <User Type> ::= 'user_type_006'
		final int PROD_USERTYPE_USER_TYPE_007                       =  83;  // <User Type> ::= 'user_type_007'
		final int PROD_USERTYPE_USER_TYPE_009                       =  84;  // <User Type> ::= 'user_type_009'
		final int PROD_USERTYPE_USER_TYPE_010                       =  85;  // <User Type> ::= 'user_type_010'
		final int PROD_USERTYPE_USER_TYPE_011                       =  86;  // <User Type> ::= 'user_type_011'
		final int PROD_USERTYPE_USER_TYPE_012                       =  87;  // <User Type> ::= 'user_type_012'
		final int PROD_USERTYPE_USER_TYPE_013                       =  88;  // <User Type> ::= 'user_type_013'
		final int PROD_USERTYPE_USER_TYPE_014                       =  89;  // <User Type> ::= 'user_type_014'
		final int PROD_USERTYPE_USER_TYPE_015                       =  90;  // <User Type> ::= 'user_type_015'
		final int PROD_USERTYPE_USER_TYPE_016                       =  91;  // <User Type> ::= 'user_type_016'
		final int PROD_USERTYPE_USER_TYPE_017                       =  92;  // <User Type> ::= 'user_type_017'
		final int PROD_USERTYPE_USER_TYPE_018                       =  93;  // <User Type> ::= 'user_type_018'
		final int PROD_USERTYPE_USER_TYPE_019                       =  94;  // <User Type> ::= 'user_type_019'
		final int PROD_USERTYPE_USER_TYPE_020                       =  95;  // <User Type> ::= 'user_type_020'
		final int PROD_USERTYPE_USER_TYPE_021                       =  96;  // <User Type> ::= 'user_type_021'
		final int PROD_USERTYPE_USER_TYPE_022                       =  97;  // <User Type> ::= 'user_type_022'
		final int PROD_USERTYPE_USER_TYPE_023                       =  98;  // <User Type> ::= 'user_type_023'
		final int PROD_USERTYPE_USER_TYPE_024                       =  99;  // <User Type> ::= 'user_type_024'
		final int PROD_USERTYPE_USER_TYPE_025                       = 100;  // <User Type> ::= 'user_type_025'
		final int PROD_USERTYPE_USER_TYPE_026                       = 101;  // <User Type> ::= 'user_type_026'
		final int PROD_USERTYPE_USER_TYPE_027                       = 102;  // <User Type> ::= 'user_type_027'
		final int PROD_USERTYPE_USER_TYPE_028                       = 103;  // <User Type> ::= 'user_type_028'
		final int PROD_USERTYPE_USER_TYPE_029                       = 104;  // <User Type> ::= 'user_type_029'
		final int PROD_USERTYPE_USER_TYPE_030                       = 105;  // <User Type> ::= 'user_type_030'
		final int PROD_SIGN_SIGNED                                  = 106;  // <Sign> ::= signed
		final int PROD_SIGN_UNSIGNED                                = 107;  // <Sign> ::= unsigned
		final int PROD_SIGN                                         = 108;  // <Sign> ::= 
		final int PROD_SCALAR_CHAR                                  = 109;  // <Scalar> ::= char
		final int PROD_SCALAR_WCHAR_T                               = 110;  // <Scalar> ::= 'wchar_t'
		final int PROD_SCALAR_INT                                   = 111;  // <Scalar> ::= int
		final int PROD_SCALAR_SHORT                                 = 112;  // <Scalar> ::= short
		final int PROD_SCALAR_LONG                                  = 113;  // <Scalar> ::= long
		final int PROD_SCALAR_SHORT_INT                             = 114;  // <Scalar> ::= short int
		final int PROD_SCALAR_LONG_INT                              = 115;  // <Scalar> ::= long int
		final int PROD_SCALAR_LONG_LONG                             = 116;  // <Scalar> ::= long long
		final int PROD_SCALAR_LONG_LONG_INT                         = 117;  // <Scalar> ::= long long int
		final int PROD_SCALAR_FLOAT                                 = 118;  // <Scalar> ::= float
		final int PROD_SCALAR_DOUBLE                                = 119;  // <Scalar> ::= double
		final int PROD_POINTERS_TIMES                               = 120;  // <Pointers> ::= '*' <Pointers>
		final int PROD_POINTERS_TIMES_CONST                         = 121;  // <Pointers> ::= '*' const <ConstPointers>
		final int PROD_POINTERS                                     = 122;  // <Pointers> ::= 
		final int PROD_CONSTPOINTERS_TIMES_CONST                    = 123;  // <ConstPointers> ::= '*' const <ConstPointers>
		final int PROD_CONSTPOINTERS_TIMES                          = 124;  // <ConstPointers> ::= '*'
		final int PROD_CONSTPOINTERS                                = 125;  // <ConstPointers> ::= 
		final int PROD_STM_ID_COLON                                 = 126;  // <Stm> ::= Id ':'
		final int PROD_STM_IF_LPAREN_RPAREN                         = 127;  // <Stm> ::= if '(' <Expr> ')' <Stm>
		final int PROD_STM_IF_LPAREN_RPAREN_ELSE                    = 128;  // <Stm> ::= if '(' <Expr> ')' <Then Stm> else <Stm>
		final int PROD_STM_WHILE_LPAREN_RPAREN                      = 129;  // <Stm> ::= while '(' <Expr> ')' <Stm>
		final int PROD_STM_FOR_LPAREN_SEMI_SEMI_RPAREN              = 130;  // <Stm> ::= for '(' <Arg> ';' <Arg> ';' <Arg> ')' <Stm>
		final int PROD_STM                                          = 131;  // <Stm> ::= <Normal Stm>
		final int PROD_THENSTM_IF_LPAREN_RPAREN_ELSE                = 132;  // <Then Stm> ::= if '(' <Expr> ')' <Then Stm> else <Then Stm>
		final int PROD_THENSTM_WHILE_LPAREN_RPAREN                  = 133;  // <Then Stm> ::= while '(' <Expr> ')' <Then Stm>
		final int PROD_THENSTM_FOR_LPAREN_SEMI_SEMI_RPAREN          = 134;  // <Then Stm> ::= for '(' <Arg> ';' <Arg> ';' <Arg> ')' <Then Stm>
		final int PROD_THENSTM                                      = 135;  // <Then Stm> ::= <Normal Stm>
		final int PROD_NORMALSTM_DO_WHILE_LPAREN_RPAREN             = 136;  // <Normal Stm> ::= do <Stm> while '(' <Expr> ')'
		final int PROD_NORMALSTM_SWITCH_LPAREN_RPAREN_LBRACE_RBRACE = 137;  // <Normal Stm> ::= switch '(' <Expr> ')' '{' <Case Stms> '}'
		final int PROD_NORMALSTM                                    = 138;  // <Normal Stm> ::= <Block>
		final int PROD_NORMALSTM_SEMI                               = 139;  // <Normal Stm> ::= <Expr> ';'
		final int PROD_NORMALSTM_GOTO_ID_SEMI                       = 140;  // <Normal Stm> ::= goto Id ';'
		final int PROD_NORMALSTM_BREAK_SEMI                         = 141;  // <Normal Stm> ::= break ';'
		final int PROD_NORMALSTM_CONTINUE_SEMI                      = 142;  // <Normal Stm> ::= continue ';'
		final int PROD_NORMALSTM_RETURN_SEMI                        = 143;  // <Normal Stm> ::= return <Expr> ';'
		final int PROD_NORMALSTM_SEMI2                              = 144;  // <Normal Stm> ::= ';'
		final int PROD_ARG                                          = 145;  // <Arg> ::= <Expr>
		final int PROD_ARG2                                         = 146;  // <Arg> ::= 
		final int PROD_CASESTMS_CASE_COLON                          = 147;  // <Case Stms> ::= case <Value> ':' <Stm List> <Case Stms>
		final int PROD_CASESTMS_DEFAULT_COLON                       = 148;  // <Case Stms> ::= default ':' <Stm List>
		final int PROD_CASESTMS                                     = 149;  // <Case Stms> ::= 
		final int PROD_BLOCK_LBRACE_RBRACE                          = 150;  // <Block> ::= '{' <Decl Stm List> '}'
		final int PROD_DECLSTMLIST                                  = 151;  // <Decl Stm List> ::= <Decl> <Decl Stm List>
		final int PROD_DECLSTMLIST2                                 = 152;  // <Decl Stm List> ::= <Stm List>
		final int PROD_STMLIST                                      = 153;  // <Stm List> ::= <Stm> <Stm List>
		final int PROD_STMLIST2                                     = 154;  // <Stm List> ::= 
		final int PROD_INITIALIZER                                  = 155;  // <Initializer> ::= <Op If>
		final int PROD_INITIALIZER_LBRACE_RBRACE                    = 156;  // <Initializer> ::= '{' <ExprIni> '}'
		final int PROD_EXPR_COMMA                                   = 157;  // <Expr> ::= <Expr> ',' <Op Assign>
		final int PROD_EXPR                                         = 158;  // <Expr> ::= <Op Assign>
		final int PROD_EXPRINI_COMMA                                = 159;  // <ExprIni> ::= <ExprIni> ',' <Initializer>
		final int PROD_EXPRINI                                      = 160;  // <ExprIni> ::= <Initializer>
		final int PROD_OPASSIGN_EQ                                  = 161;  // <Op Assign> ::= <Op If> '=' <Op Assign>
		final int PROD_OPASSIGN_PLUSEQ                              = 162;  // <Op Assign> ::= <Op If> '+=' <Op Assign>
		final int PROD_OPASSIGN_MINUSEQ                             = 163;  // <Op Assign> ::= <Op If> '-=' <Op Assign>
		final int PROD_OPASSIGN_TIMESEQ                             = 164;  // <Op Assign> ::= <Op If> '*=' <Op Assign>
		final int PROD_OPASSIGN_DIVEQ                               = 165;  // <Op Assign> ::= <Op If> '/=' <Op Assign>
		final int PROD_OPASSIGN_CARETEQ                             = 166;  // <Op Assign> ::= <Op If> '^=' <Op Assign>
		final int PROD_OPASSIGN_AMPEQ                               = 167;  // <Op Assign> ::= <Op If> '&=' <Op Assign>
		final int PROD_OPASSIGN_PIPEEQ                              = 168;  // <Op Assign> ::= <Op If> '|=' <Op Assign>
		final int PROD_OPASSIGN_GTGTEQ                              = 169;  // <Op Assign> ::= <Op If> '>>=' <Op Assign>
		final int PROD_OPASSIGN_LTLTEQ                              = 170;  // <Op Assign> ::= <Op If> '<<=' <Op Assign>
		final int PROD_OPASSIGN                                     = 171;  // <Op Assign> ::= <Op If>
		final int PROD_OPIF_QUESTION_COLON                          = 172;  // <Op If> ::= <Op Or> '?' <Op If> ':' <Op If>
		final int PROD_OPIF                                         = 173;  // <Op If> ::= <Op Or>
		final int PROD_OPOR_PIPEPIPE                                = 174;  // <Op Or> ::= <Op Or> '||' <Op And>
		final int PROD_OPOR                                         = 175;  // <Op Or> ::= <Op And>
		final int PROD_OPAND_AMPAMP                                 = 176;  // <Op And> ::= <Op And> '&&' <Op BinOR>
		final int PROD_OPAND                                        = 177;  // <Op And> ::= <Op BinOR>
		final int PROD_OPBINOR_PIPE                                 = 178;  // <Op BinOR> ::= <Op BinOR> '|' <Op BinXOR>
		final int PROD_OPBINOR                                      = 179;  // <Op BinOR> ::= <Op BinXOR>
		final int PROD_OPBINXOR_CARET                               = 180;  // <Op BinXOR> ::= <Op BinXOR> '^' <Op BinAND>
		final int PROD_OPBINXOR                                     = 181;  // <Op BinXOR> ::= <Op BinAND>
		final int PROD_OPBINAND_AMP                                 = 182;  // <Op BinAND> ::= <Op BinAND> '&' <Op Equate>
		final int PROD_OPBINAND                                     = 183;  // <Op BinAND> ::= <Op Equate>
		final int PROD_OPEQUATE_EQEQ                                = 184;  // <Op Equate> ::= <Op Equate> '==' <Op Compare>
		final int PROD_OPEQUATE_EXCLAMEQ                            = 185;  // <Op Equate> ::= <Op Equate> '!=' <Op Compare>
		final int PROD_OPEQUATE                                     = 186;  // <Op Equate> ::= <Op Compare>
		final int PROD_OPCOMPARE_LT                                 = 187;  // <Op Compare> ::= <Op Compare> '<' <Op Shift>
		final int PROD_OPCOMPARE_GT                                 = 188;  // <Op Compare> ::= <Op Compare> '>' <Op Shift>
		final int PROD_OPCOMPARE_LTEQ                               = 189;  // <Op Compare> ::= <Op Compare> '<=' <Op Shift>
		final int PROD_OPCOMPARE_GTEQ                               = 190;  // <Op Compare> ::= <Op Compare> '>=' <Op Shift>
		final int PROD_OPCOMPARE                                    = 191;  // <Op Compare> ::= <Op Shift>
		final int PROD_OPSHIFT_LTLT                                 = 192;  // <Op Shift> ::= <Op Shift> '<<' <Op Add>
		final int PROD_OPSHIFT_GTGT                                 = 193;  // <Op Shift> ::= <Op Shift> '>>' <Op Add>
		final int PROD_OPSHIFT                                      = 194;  // <Op Shift> ::= <Op Add>
		final int PROD_OPADD_PLUS                                   = 195;  // <Op Add> ::= <Op Add> '+' <Op Mult>
		final int PROD_OPADD_MINUS                                  = 196;  // <Op Add> ::= <Op Add> '-' <Op Mult>
		final int PROD_OPADD                                        = 197;  // <Op Add> ::= <Op Mult>
		final int PROD_OPMULT_TIMES                                 = 198;  // <Op Mult> ::= <Op Mult> '*' <Op Unary>
		final int PROD_OPMULT_DIV                                   = 199;  // <Op Mult> ::= <Op Mult> '/' <Op Unary>
		final int PROD_OPMULT_PERCENT                               = 200;  // <Op Mult> ::= <Op Mult> '%' <Op Unary>
		final int PROD_OPMULT                                       = 201;  // <Op Mult> ::= <Op Unary>
		final int PROD_OPUNARY_EXCLAM                               = 202;  // <Op Unary> ::= '!' <Op Unary>
		final int PROD_OPUNARY_TILDE                                = 203;  // <Op Unary> ::= '~' <Op Unary>
		final int PROD_OPUNARY_MINUS                                = 204;  // <Op Unary> ::= '-' <Op Unary>
		final int PROD_OPUNARY_TIMES                                = 205;  // <Op Unary> ::= '*' <Op Unary>
		final int PROD_OPUNARY_AMP                                  = 206;  // <Op Unary> ::= '&' <Op Unary>
		final int PROD_OPUNARY_PLUSPLUS                             = 207;  // <Op Unary> ::= '++' <Op Unary>
		final int PROD_OPUNARY_MINUSMINUS                           = 208;  // <Op Unary> ::= '--' <Op Unary>
		final int PROD_OPUNARY_PLUSPLUS2                            = 209;  // <Op Unary> ::= <Op Pointer> '++'
		final int PROD_OPUNARY_MINUSMINUS2                          = 210;  // <Op Unary> ::= <Op Pointer> '--'
		final int PROD_OPUNARY_LPAREN_RPAREN                        = 211;  // <Op Unary> ::= '(' <Type> ')' <Op Unary>
		final int PROD_OPUNARY_SIZEOF_LPAREN_RPAREN                 = 212;  // <Op Unary> ::= sizeof '(' <Type> ')'
		final int PROD_OPUNARY_SIZEOF_LPAREN_RPAREN2                = 213;  // <Op Unary> ::= sizeof '(' <Pointers> <Op Pointer> ')'
		final int PROD_OPUNARY                                      = 214;  // <Op Unary> ::= <Op Pointer>
		final int PROD_OPPOINTER_DOT                                = 215;  // <Op Pointer> ::= <Op Pointer> '.' <Call Id>
		final int PROD_OPPOINTER_MINUSGT                            = 216;  // <Op Pointer> ::= <Op Pointer> '->' <Call Id>
		final int PROD_OPPOINTER_LBRACKET_RBRACKET                  = 217;  // <Op Pointer> ::= <Op Pointer> '[' <Expr> ']'
		final int PROD_OPPOINTER                                    = 218;  // <Op Pointer> ::= <Value>
		final int PROD_CALLID_ID_LPAREN_RPAREN                      = 219;  // <Call Id> ::= Id '(' <Expr> ')'
		final int PROD_CALLID_ID_LPAREN_RPAREN2                     = 220;  // <Call Id> ::= Id '(' ')'
		final int PROD_CALLID_ID                                    = 221;  // <Call Id> ::= Id
		final int PROD_VALUE_OCTLITERAL                             = 222;  // <Value> ::= OctLiteral
		final int PROD_VALUE_HEXLITERAL                             = 223;  // <Value> ::= HexLiteral
		final int PROD_VALUE_DECLITERAL                             = 224;  // <Value> ::= DecLiteral
		final int PROD_VALUE_STRINGLITERAL                          = 225;  // <Value> ::= StringLiteral
		final int PROD_VALUE_CHARLITERAL                            = 226;  // <Value> ::= CharLiteral
		final int PROD_VALUE_FLOATLITERAL                           = 227;  // <Value> ::= FloatLiteral
		final int PROD_VALUE                                        = 228;  // <Value> ::= <Call Id>
		final int PROD_VALUE_LPAREN_RPAREN                          = 229;  // <Value> ::= '(' <Expr> ')'
	};

	private enum PreprocState {TEXT, TYPEDEF, ENUMTYPE, STRUCTUNIONTYPE, COMPLIST, /*ENUMLIST, STRUCTLIST,*/ TYPEID};
	private StringList typedefs = new StringList();
	
	// Import Call elements with provisional Name, which have to be renamed as soon as the name gets known
	private LinkedList<Call> provisionalImportCalls = new LinkedList<Call>();
	
	/**
	 * Constructs a parser for language ANSI-C, loads the grammar as resource and
	 * specifies whether to generate the parse tree as string
	 */
	public CParser() {

	}

	//------------------------- Preprocessor ---------------------------

	/**
	 * Performs some necessary preprocessing for the text file. Actually opens the
	 * file, filters it and writes a new file _textToParse+".structorizer" to the
	 * same directory, which is then actually parsed. For the C Parser e.g. the
	 * preprocessor directives must be removed and possibly be executed (at least the
	 * defines. with #if it would get difficult).
	 * The preprocessed file will always be saved with UTF-8 encoding.
	 * @param _textToParse - name (path) of the source file
	 * @param _encoding - the expected encoding of the source file.
	 * @return The File object associated with the preprocessed source file.
	 */
	@Override
	protected File prepareTextfile(String _textToParse, String _encoding)
	{
		final String voidCastPattern = "(^\\s*|.*?\\W+\\s*)\\(\\s*void\\s*\\)(.*?)";
		final String[][] typeReplacements = new String[][] {
			{"size_t", "unsigned long"},
			{"time_t", "unsigned long"}
		};
		final String definePattern = "^#define\\s+([\\w].*)\\s+(.+)";
		File interm = null;
		try
		{
			File file = new File(_textToParse);
			HashMap<String, String> defines = new LinkedHashMap<String, String>();
			DataInputStream in = new DataInputStream(new FileInputStream(file));
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
				String trimmedLine = strLine.trim();
				if (trimmedLine.matches(definePattern) && !trimmedLine.contains("(") && !trimmedLine.contains(")")) {
					String symbol = trimmedLine.replaceAll(definePattern, "$1");
					String subst = trimmedLine.replaceAll(definePattern, "$2").trim();
					defines.put(symbol, subst);
				}
				else if (!trimmedLine.startsWith("#")) {
					// The grammar doesn't cope with customer-defined type names nor library-defined ones, so we will have to
					// replace as many as possible of them in advance.
					// We cannot guess however, what's included since include files won't be available for us.
					for (String[] pair: typeReplacements) {
						String search = "(^|.*\\W)"+pair[0]+"(\\W.*|$)";
						if (strLine.matches(search)) {
							strLine = strLine.replaceAll(search, "$1" + pair[1] + "$2");
						}
					}
					if (strLine.matches(voidCastPattern)) {
						strLine = strLine.replaceAll(voidCastPattern, "$1$2");
					}
					srcCode += strLine + "\n";
				}
			}
			//Close the input stream
			in.close();

			for (Entry<String, String> entry: defines.entrySet()) {
				if (debugprint) {
					System.out.println("CParser.prepareTextfile(): " + Matcher.quoteReplacement((String)entry.getValue()));
				}
				srcCode = srcCode.replaceAll("(.*?\\W)" + entry.getKey() + "(\\W.*?)", "$1"+ Matcher.quoteReplacement((String)entry.getValue()) + "$2");
			}
			
			// Now we try to replace all type names introduced by typedef declarations
			// because the grammar doesn't cope with user-defined type ids.
			// In a first step we gather all type names defined via typedef in a
			// StringList mapping them by their index to generic type ids being
			// defined in the grammar ("user_type_##"). It will be a rudimentary parsing
			// i.e. we don't consider anything except typedef declarations and we expect
			// a syntactically correct construct. If something
			// strange occurs then we just ignore the text until we bump into another
			// typedef keyword.
			// In the second step we replace all identifiers occurring in the map with
			// their associated generic name.
			// TODO in future this will have to care of the block scope...
			
			typedefs.clear();
			Vector<Integer[]> blockRanges = new Vector<Integer[]>();
			
			Stack<Character> parenthStack = new Stack<Character>();
			Stack<Integer> blockStarts = new Stack<Integer>();
			int blockStartLine = -1;
			int typedefLevel = -1;
			int indexDepth = 0;
			PreprocState state = PreprocState.TEXT;
			String lastId = null;
			char expected = '\0';
			
			StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(srcCode));
			tokenizer.quoteChar('"');
			tokenizer.quoteChar('\'');
			tokenizer.slashStarComments(true);
			tokenizer.slashSlashComments(true);
			tokenizer.parseNumbers();
			tokenizer.eolIsSignificant(true);
			
			while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
				String word = null;
				System.out.print("(" + tokenizer.lineno() + ")");
				switch (tokenizer.ttype) {
				case StreamTokenizer.TT_EOL:
					System.out.println("**newline**");
					break;
				case StreamTokenizer.TT_NUMBER:
					System.out.println("number: " + tokenizer.nval);
					break;
				case StreamTokenizer.TT_WORD:
					System.out.println("word: " + tokenizer.sval);
					word = tokenizer.sval;
					if (state == PreprocState.TYPEDEF) {
						if (word.equals("enum")) {
							state = PreprocState.ENUMTYPE;
						}
						else if (word.equals("struct") || word.equals("union")) {
							state = PreprocState.STRUCTUNIONTYPE;
						}
						else {
							lastId = word;	// Might be the defined type id if no identifier will follow
						}
					}
					else if (state == PreprocState.TYPEID && indexDepth == 0) {
						typedefs.add(word);
						blockRanges.add(new Integer[]{tokenizer.lineno()+1, (blockStarts.isEmpty() ? -1 : blockStarts.peek())});
					}
					else if (word.equals("typedef")) {
						typedefLevel = blockStarts.size();
						state = PreprocState.TYPEDEF;
					}
					// in case of ENUMTYPE or STRUCTUNIONTYPE just skip the id
					break;
				case '\'':
					System.out.println("character: '" + tokenizer.sval + "'");
					break;
				case '"':
					System.out.println("string: \"" + tokenizer.sval + "\"");
					break;
				case '{':
					blockStarts.add(tokenizer.lineno());
					if (state == PreprocState.STRUCTUNIONTYPE || state == PreprocState.ENUMTYPE) {
						state = PreprocState.COMPLIST;
					}
					parenthStack.push('}');
					break;
				case '(':
					parenthStack.push(')');
					break;
				case '[':	// FIXME: Handle index lists in typedefs!
					if (state == PreprocState.TYPEID) {
						indexDepth++;
					}
					parenthStack.push(']');
					break;
				case '}':
					blockStartLine = blockStarts.pop();
					// Store the start and current line no as block range if there are typedefs on this block level
					int blockEndLine = tokenizer.lineno();
					Integer[] entry;
					for (int i = blockRanges.size()-1; i >= 0 && (entry = blockRanges.get(i))[1] >= blockStartLine; i--) {
						if (entry[1] == blockStartLine && entry[1] < entry[0]) {
							entry[1] = blockEndLine;
						}
					}
					if (state == PreprocState.COMPLIST && typedefLevel == blockStarts.size()) {
						// After the closing brace, type ids are expected to follow
						state = PreprocState.TYPEID;
					}
				case ')':
				case ']':	// Handle index lists in typedef!s
				{
					if (parenthStack.isEmpty() || tokenizer.ttype != (expected = parenthStack.pop().charValue())) {
						System.err.println("**FILE PREPARATION TROUBLE** in line " + tokenizer.lineno()
						+ " of file \"" + _textToParse + "\": unmatched '" + (char)tokenizer.ttype
						+ "' (expected: '" + (expected == '\0' ? '\u25a0' : expected) + "')!");
					}
					else if (tokenizer.ttype == ']' && state == PreprocState.TYPEID) {
						indexDepth--;
					}
				}
					break;
				case '*':
					if (state == PreprocState.TYPEDEF) {
						state = PreprocState.TYPEID;
					}
					break;
				case ',':
					if (state == PreprocState.TYPEDEF && lastId != null) {
						typedefs.add(lastId);
						blockRanges.add(new Integer[]{tokenizer.lineno()+1, (blockStarts.isEmpty() ? -1 : blockStarts.peek())});
						state = PreprocState.TYPEID;
					}
					break;
				case ';':
					if (state == PreprocState.TYPEDEF && lastId != null) {
						typedefs.add(lastId);
						blockRanges.add(new Integer[]{tokenizer.lineno()+1, (blockStarts.isEmpty() ? -1 : blockStarts.peek())});
						state = PreprocState.TEXT;
					}
					else if (state == PreprocState.TYPEID) {
						state = PreprocState.TEXT;						
					}
				default:
					char tokenChar = (char)tokenizer.ttype;
					System.out.println("other: " + tokenChar);
				}
			}
			
			StringList srcLines = StringList.explode(srcCode, "\n");
			// Now we replace the detected user-specific type names by the respective generic ones.
			for (int i = 0; i < typedefs.count(); i++) {
				String typeName = typedefs.get(i);
				Integer[] range = blockRanges.get(i);
				// Global range?
				if (range[1] < 0) {
					range[1] = srcLines.count()-1;
				}
				String pattern = "(^|.*?\\W)("+typeName+")(\\W.*?|$)";
				String subst = String.format("user_type_%03d", i+1);
				this.replacedIds.put(subst, typeName);
				subst = "$1" + subst + "$3";
				for (int j = range[0]; j <= range[1]; j++) {
					if (srcLines.get(j).matches(pattern)) {
						srcLines.set(j, srcLines.get(j).replaceAll(pattern, subst));
					}
				}
			}
			srcCode = srcLines.concatenate("\n");

//			Regex r = new Regex("(.*\\()\\s*?void\\s*?(\\).*)", "$1$2");
//			srcCode = r.replaceAll(srcCode);
			
			//System.out.println(srcCode);

			// trim and save as new file
			//interm = new File(_textToParse + ".structorizer");
			interm = File.createTempFile("Structorizer", ".c");
			OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(interm), "UTF-8");
			ow.write(srcCode.trim()+"\n");
			//System.out.println("==> "+filterNonAscii(srcCode.trim()+"\n"));
			ow.close();
		}
		catch (Exception e) 
		{
			System.err.println("CParser.prepareTextfile() -> " + e.getMessage());
		}
		return interm;
	}

	//---------------------- Build methods for structograms ---------------------------

	private Root globalRoot = null;	//  dummy Root for global definitions (will be put to main or the only function)
	
	/**
	 * Preselects the type of the initial diagram to be imported as function.
	 * @see CodeParser#initializeBuildNSD()
	 */
	@Override
	protected void initializeBuildNSD()
	{
		root.isProgram = false;	// C programs are functions, primarily
		this.optionUpperCaseProgName = Root.check(6);
	}

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
			String ruleName = _reduction.getParent().getHead().toString();
			int ruleId = _reduction.getParent().getTableIndex();
			if (debugprint) {
				System.out.println("buildNSD_R(" + rule + ", " + _parentNode.parent + ")...");
			}
			
			if (
					// Procedure call?
					ruleId == RuleConstants.PROD_CALLID_ID_LPAREN_RPAREN
					||
					ruleId == RuleConstants.PROD_CALLID_ID_LPAREN_RPAREN2
					)
			{
				String content = "";
				String procName = _reduction.get(0).asString();
				StringList arguments = null;
				if (ruleId == RuleConstants.PROD_CALLID_ID_LPAREN_RPAREN) {
					arguments = this.getExpressionList(_reduction.get(2).asReduction());
				}
				if (procName.equals("exit")) {
					content = getKeywordOrDefault("preExit", "exit");
					if (arguments.count() > 0) {
						content += arguments.get(0);
						_parentNode.addElement(new Jump(content));
					}
				}
				else if (procName.equals("printf") || procName.equals("puts") && arguments.count() == 1)
				{
					buildOutput(_reduction, procName, arguments, _parentNode);
				}
				else if (procName.equals("scanf") || procName.equals("gets") && arguments.count() == 1){
					buildInput(_reduction, procName, arguments, _parentNode);
				}
				else if (!convertBuiltInRoutines(_reduction, procName, arguments, _parentNode)) {
					_parentNode.addElement(new Instruction(getContent_R(_reduction, content)));
				}
			}
			else if (
					// Assignment?
					ruleId == RuleConstants.PROD_OPASSIGN_EQ
					)
			{
				// Simply convert it as text and create an instruction. In case of an
				// external function call we'll try to transmute it after all subroutines
				// will have been built.
				String content = new String();
				content = getContent_R(_reduction, content).trim();
				//System.out.println(ruleName + ": " + content);
				// In case of a variable declaration get rid of the trailing semicolon
				//if (content.endsWith(";")) {
				//	content = content.substring(0, content.length() - 1).trim();
				//}
				_parentNode.addElement(new Instruction(translateContent(content)));
			}
			else if (
					// Variable declaration with or without initialization?
					ruleName.equals("<Var Decl>")	// var declaration			
					)
			{
				// If declaration import is allowed then we make an instruction in
				// Pascal syntax out of it.
				String rootName = root.getMethodName();
				Subqueue parentNode = _parentNode;
				boolean isGlobal = rootName.equals("???"); 
				if (isGlobal) {
					if (this.globalRoot == null) {
						this.globalRoot = root;
//						this.globalRoot.setText("global");
//						subRoots.add(this.globalRoot);
					}
					parentNode = this.globalRoot.children;
				}
				boolean isConstant = false;
				int typeIx = -1;	// token index of the type description
				switch (ruleId) {
				case RuleConstants.PROD_VARDECL_SEMI:
					isConstant = getContent_R(_reduction.get(0).asReduction(), "").contains("const");
					typeIx = 1;
					break;
				case RuleConstants.PROD_VARDECL_CONST_SEMI:
					isConstant = true;
					typeIx = 2;
					break;
				case RuleConstants.PROD_VARDECL_SEMI2:
					typeIx = 0;
					break;
				case RuleConstants.PROD_VARDECL_CONST_SEMI2:
					isConstant = true;
				}
				String type = "int";	// default type if there is only a modifier
				if (typeIx >= 0) {
					type = getContent_R(_reduction.get(typeIx).asReduction(), "");
					if (type.matches("(^|.*?\\s+)const(\\s+.*?|$)")) {
						type = type.replaceAll("(^|.*?\\s+)const(\\s+.*?|$)", "$1$2");
						isConstant = true;
					}
				}
				if (isConstant) {
					type = "const " + type;
				}
				// Now concern on the first declaration of the list
				Reduction secReduc = _reduction.get(_reduction.size() - 3).asReduction();
				buildDeclOrAssignment(secReduc, type, parentNode);
//				if (_reduction.size() > typeIx+2) {
					if (debugprint) {
						System.out.println("\tanalyzing <Var List> ...");
					}
//					secReduc = _reduction.get(typeIx + 2).asReduction();	// <Var List>
					secReduc = _reduction.get(_reduction.size()-2).asReduction();	// <Var List>
					ruleId = secReduc.getParent().getTableIndex();
					while (ruleId == RuleConstants.PROD_VARLIST_COMMA) {
						Reduction thdReduc = secReduc.get(1).asReduction();	// <Var Item>
						// Get the pointers part
						String pointers = getContent_R(thdReduc.get(0).asReduction(), "");
						// Delegate the sub-reduction <Var>
						buildDeclOrAssignment(secReduc.get(1).asReduction(), type+pointers, parentNode);
						// Get the list tail
						secReduc = secReduc.get(2).asReduction();	// <Var List>
						ruleId = secReduc.getParent().getTableIndex();
					}
					if (debugprint) {
						System.out.println("\t<Var List> done.");
					}
//				}
			}
			else if (
					// Type definitions
						ruleId == RuleConstants.PROD_STRUCTDECL_STRUCT_ID_LBRACE_RBRACE
						||
						ruleId == RuleConstants.PROD_UNIONDECL_UNION_ID_LBRACE_RBRACE
						||
						ruleId == RuleConstants.PROD_ENUMDECL_ENUM_ID_LBRACE_RBRACE
						||
						ruleId == RuleConstants.PROD_TYPEDEFDECL_TYPEDEF
					)
			{
				// If we don't handle struct and union definitions then their components would be made
				// variable declarations.
				// We will create disabled declaration instructions for all type declarations instead.
				// For global type definitions we will create a dummy "import" diagram for global stuff
				// (enh. #389).
				// Moreover, a grammar enhancement of 2017-04-11 now supports the combination of a type
				// definition with variable declarations. So we will have to handle them as well.
				String rootName = root.getMethodName();
				Subqueue parentNode = _parentNode;
				boolean isGlobal = rootName.equals("???"); 
				if (isGlobal) {
					if (this.globalRoot == null) {
						this.globalRoot = root;
						//subRoots.add(this.globalRoot);
					}
					parentNode = this.globalRoot.children;
				}
				String content = this.getContent_R(_reduction, "");
				int insertAt = parentNode.getSize();
				if (ruleId != RuleConstants.PROD_TYPEDEFDECL_TYPEDEF) {
					Reduction varDecl = _reduction.get(5).asReduction();
					// Does it contain variable declarations?
					if (varDecl.getParent().getTableIndex() == RuleConstants.PROD_DECLEND_SEMI2) {
						String type = content.substring(0, content.indexOf("{")).trim();
						this.buildDeclOrAssignment(varDecl.get(0).asReduction(), type, _parentNode);
						varDecl = varDecl.get(1).asReduction();
						while (varDecl.getParent().getTableIndex() == RuleConstants.PROD_VARLIST_COMMA) {
							this.buildDeclOrAssignment(varDecl.get(1).asReduction(), type, _parentNode);
							varDecl = varDecl.get(2).asReduction();
						}
						content = this.getContent_R(_reduction.get(3).asReduction(), type + " {") + "}";
					}
				}
				Instruction decl = new Instruction(translateContent(content));
				decl.disabled = true;
				if (isGlobal) {
					decl.setColor(colorGlobal);
				}
				parentNode.insertElementAt(decl, insertAt);
			}
			else if (
					// BREAK instruction
					ruleId == RuleConstants.PROD_NORMALSTM_BREAK_SEMI
					)
			{
				String content = getKeyword("preLeave");
				_parentNode.addElement(new Jump(content.trim()));								
			}
			else if (
					// RETURN instruction
					ruleId == RuleConstants.PROD_NORMALSTM_RETURN_SEMI
					)
			{
				String content = translateContent(getKeyword("preReturn") + 
						" " + getContent_R(_reduction.get(1).asReduction(), ""));
				_parentNode.addElement(new Jump(content.trim()));												
			}
			else if (
					// GOTO instruction
					ruleId == RuleConstants.PROD_NORMALSTM_GOTO_ID_SEMI
					)
			{
				String content = _reduction.get(0).asString() + " " + _reduction.get(1).asString();
				Jump el = new Jump(content.trim());
				el.setComment("FIXME: Goto is not supported in structured algorithms!");
				el.setColor(Color.RED);
				_parentNode.addElement(el);				
			}
			else if (
					// Jump label!
					ruleId == RuleConstants.PROD_STM_ID_COLON
					)
			{
				String content = _reduction.get(0).asString() + ":";
				Instruction el = new Instruction(content);
				el.setColor(Color.RED);	// will only be seen if the user enables the element
				el.disabled = true;
				el.setComment("FIXME: Goto instructions are not supported in structured algorithms!");
				_parentNode.addElement(el);
			}
			else if (
					ruleId == RuleConstants.PROD_NORMALSTM_CONTINUE_SEMI
					)
			{
				// FIXME This going to get tricky here. Maybe we can solve it better in the ready diagram
				// Typically the continue instruction will be in the TRUE branch of an alternative within
				// a loop. But we may not know whether or not the FALSE branch of the alternative will remain
				// empty (then and if the alternative is a direct child of the loop we could just append the
				// remaining elements to the FALSE branch of the alternative. But what if the FALSE branch
				// isn't empty or if the alternative is deeper nested. And the continue instruction could
				// also be positioned within a case branch...
				// So we'll just add a red jump for now
				if (_parentNode.parent instanceof ILoop) {
					Element el = new Jump(_reduction.get(0).asString());
					el.setColor(Color.RED);
					_parentNode.addElement(el);
				}
			}
			else if (
					// Combined assignment operator expression (+=, -= etc.)?
					ruleId >= RuleConstants.PROD_OPASSIGN_PLUSEQ
					&&
					ruleId <= RuleConstants.PROD_OPASSIGN_LTLTEQ
					)
			{
				final String oprs[] = {" + ", " - ", " * ", " / ", " xor ", " and ", " or ", " shr ", " shl "};
				String content = new String();
				String lval = getContent_R(_reduction.get(0).asReduction(), "");
				String expr = getContent_R(_reduction.get(2).asReduction(), "");
				content = lval + " <- " + lval + oprs[ruleId - RuleConstants.PROD_OPASSIGN_PLUSEQ] + expr;
				//System.out.println(ruleName + ": " + content);
				_parentNode.addElement(new Instruction(translateContent(content)));
			}
			else if (
					// Autoincrement / autodecrement (i++, i--, ++i, --i)
					ruleId >= RuleConstants.PROD_OPUNARY_PLUSPLUS
					&&
					ruleId <= RuleConstants.PROD_OPUNARY_MINUSMINUS2
					)
			{
				// Token index of the variable
				int lvalIx = (ruleId <= RuleConstants.PROD_OPUNARY_MINUSMINUS) ? 1 : 0;
				// Variable name
				String lval = getContent_R(_reduction.get(lvalIx).asReduction(), "");
				// Operator + or - ?
				String opr = (ruleId % 2 == RuleConstants.PROD_OPUNARY_PLUSPLUS % 2) ? " + " : " - ";
				String content = lval + " <- " + lval + opr + "1";
				_parentNode.addElement(new Instruction(translateContent(content)));
			}
			else if (
					// Function declaration?
					ruleName.equals("<Func Decl>")
					)
			{
				// Find out the name of the function
				Reduction secReduc = _reduction.get(0).asReduction();
				int nameIx = -1;	// Token index of the function id
				int typeIx = -1;	// Token index of the type specifier ("const") neglected
				int funcId = secReduc.getParent().getTableIndex();
				switch (funcId) {
				case RuleConstants.PROD_FUNCID_ID:
					typeIx = 1;
					nameIx = 2;
					break;
				case RuleConstants.PROD_FUNCID_CONST_ID:
					typeIx = 2;
					nameIx = 3;
					break;
				case RuleConstants.PROD_FUNCID_VOID_ID2:
					typeIx = 1;
					nameIx = 2;
					break;
				case RuleConstants.PROD_FUNCID_ID2:
				case RuleConstants.PROD_FUNCID_VOID_ID:
					typeIx = 0;
					nameIx = 1;
					break;
				case RuleConstants.PROD_FUNCID_ID3:
					nameIx = 0;
					break;
				}
				String funcName = secReduc.get(nameIx).getData().toString();
				Root prevRoot = root;	// Cache the original root
				root = new Root();	// Prepare a new root for the (sub)routine
				root.isProgram = false;
				subRoots.add(root);
				String content = new String();
				// Is there a type specification different from void?
				if (typeIx >= 0) {
					Token typeToken = secReduc.get(typeIx);
					if (typeToken.getType() == SymbolType.CONTENT) {
						content += typeToken.asString();
					}
					else {
						content = getContent_R(secReduc.get(typeIx).asReduction(), content).trim() + " ";
					}
					content = content.replace("const ", "");
				}
				content += funcName + "(";
				int bodyIndex = 4;
				String params = "";
				switch (ruleId) {
				case RuleConstants.PROD_FUNCDECL_LPAREN_RPAREN2:
					params = getParamListFromStructDecl(_reduction.get(4).asReduction());
					bodyIndex = 5;
					// Here is by design no break!
				case RuleConstants.PROD_FUNCDECL_LPAREN_RPAREN:
					params = getContent_R(_reduction.get(2).asReduction(), "");
					break;
				case RuleConstants.PROD_FUNCDECL_LPAREN_RPAREN3:
					bodyIndex = 3;
					break;
				}
				content += params + ")";
				root.setText(content);
				if (_reduction.get(bodyIndex).getType() == SymbolType.NON_TERMINAL)
				{
					buildNSD_R(_reduction.get(bodyIndex).asReduction(), root.children);
				}
				// Restore the original root
				root = prevRoot;
			}
			// END KGU#194 2016-05-08
			else if (
					// WHILE loop?
					ruleId == RuleConstants.PROD_STM_WHILE_LPAREN_RPAREN
					||
					ruleId == RuleConstants.PROD_THENSTM_WHILE_LPAREN_RPAREN
					 )
			{
				String content = new String();
				content = getContent_R(_reduction.get(2).asReduction(), content);
				While ele = new While((getKeyword("preWhile").trim() + " " + translateContent(content) + " " + getKeyword("postWhile").trim()).trim());
				_parentNode.addElement(ele);
				
				Reduction secReduc = _reduction.get(4).asReduction();
				buildNSD_R(secReduc,ele.q);
			}
			else if (
					// REPEAT loop?
					ruleId == RuleConstants.PROD_NORMALSTM_DO_WHILE_LPAREN_RPAREN
					 )
			{
				String content = new String();
				content = getContent_R(_reduction.get(4).asReduction(), content);
				// FIXME We might look for kinds of expressions with direct negation possibility,
				// e.g. PROD_OPEQUATE_EQEQ, PROD_OPEQUATE_EXCLAMEQ, PROD_OPCOMPARE_LT, PROD_OPCOMPARE_GT
				// etc. where we could try to replace the reduction by its opposite.
				Repeat ele = new Repeat((getKeyword("preRepeat").trim() + " not (" + content + ") " + getKeyword("postRepeat").trim()).trim());
				_parentNode.addElement(ele);
				
				Reduction secReduc = _reduction.get(1).asReduction();
				buildNSD_R(secReduc, ele.q);
			}
			else if (
					// FOR loop?
					 ruleId == RuleConstants.PROD_STM_FOR_LPAREN_SEMI_SEMI_RPAREN
					 ||
					 ruleId == RuleConstants.PROD_THENSTM_FOR_LPAREN_SEMI_SEMI_RPAREN
					 )
			{
				// The easiest (and default) approach is always to build WHILE loops here
				// Only in very few cases which are difficult to verify, a FOR loop might
				// be built: The first part must be a single assignment, the variable of
				// which must occur in a comparison in part 2 and in a simple incrementation
				// or decrementation in part3. The next trouble. The incrementation/decremention
				// should only have one of the forms i++, ++i, i--, --i - only then can we be
				// sure it's an integer increment/decrement and the step sign is clear such
				// that the comparison will not have to be modified fundamentally.
				
				// get first part - should be an assignment...
				// We make a separate instruction out of it
				Reduction secReduc = _reduction.get(2).asReduction();
				buildNSD_R(secReduc, _parentNode);
				// Mark all offsprings of the FOR loop with a (by default) yellowish colour
				_parentNode.getElement(_parentNode.getSize()-1).setColor(Element.color2);
				
				// get the second part - should be an ordinary condition
				String content = getContent_R(_reduction.get(4).asReduction(), "");
				While ele = new While((getKeyword("preWhile").trim() + " " + translateContent(content) + " " + getKeyword("postWhile").trim()).trim());
				// Mark all offsprings of the FOR loop with a (by default) yellowish colour
				ele.setColor(colorMisc);
				_parentNode.addElement(ele);
				
				// Get and convert the body
				secReduc = _reduction.get(8).asReduction();
				buildNSD_R(secReduc, ele.q);

				// get the last part of the header now and append it to the body
				secReduc = _reduction.get(6).asReduction();
				// Problem is that it is typically a simple operator expression,
				// e.g. i++ or --i, so it won't be recognized as statement unless we
				/// impose some extra status
				buildNSD_R(secReduc, ele.q);
				// Mark all offsprings of the FOR loop with a (by default) yellowish colour
				ele.q.getElement(ele.q.getSize()-1).setColor(colorMisc);

			}
			else if (
					// IF statement?
					ruleId == RuleConstants.PROD_STM_IF_LPAREN_RPAREN
					||
					ruleId == RuleConstants.PROD_STM_IF_LPAREN_RPAREN_ELSE
					)
			{
				String content = new String();
				// get the condition
				content = getContent_R(_reduction.get(2).asReduction(), content);
				
				Alternative ele = new Alternative((getKeyword("preAlt").trim()+ " " + translateContent(content) + " " + getKeyword("postAlt").trim()).trim());
				_parentNode.addElement(ele);
				
				Reduction secReduc = _reduction.get(4).asReduction();
				buildNSD_R(secReduc,ele.qTrue);
				if (ruleId == RuleConstants.PROD_STM_IF_LPAREN_RPAREN_ELSE)
				{
					secReduc = _reduction.get(6).asReduction();
					buildNSD_R(secReduc, ele.qFalse);
				}
			}
			else if (
					// CASE branch?
					ruleId == RuleConstants.PROD_CASESTMS_CASE_COLON
					||
					ruleId == RuleConstants.PROD_CASESTMS_DEFAULT_COLON
					)
			{
				buildCaseBranch(_reduction, ruleId, (Case) _parentNode.parent);
			}
			else if (
					// CASE statement (switch)
					 ruleId == RuleConstants.PROD_NORMALSTM_SWITCH_LPAREN_RPAREN_LBRACE_RBRACE
					 )
			{
				//this.firstCaseWithoutBreak.push(0);
				buildCase(_reduction, _parentNode);
			}
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
	 * Helper method for he analysis of the very old C function declaration syntax:
	 * <Type> <Func ID> '(' <Id List> ')' <Struct Def> <Block>
	 * @param _structDef - the reduction of <Struct Def>
	 * @return a String comprising all parameter declarations in modern syntax
	 */
	private String getParamListFromStructDecl(Reduction _structDef) {
		StringList params = new StringList();
		do {
			Reduction varDecl = _structDef.get(0).asReduction();
			int nameIx = varDecl.size() - 2;
			String type = "int";
			if (varDecl.getParent().getTableIndex() < RuleConstants.PROD_VARDECL_SEMI3) {
				type = getContent_R(varDecl.get(nameIx -1).asReduction(), "");
			}
			params.add(type + " " + varDecl.get(nameIx).asString());
			Reduction varList = varDecl.get(nameIx+1).asReduction();
			while (varList.size() > 0) {
				params.add(type + " " + varList.get(1).asString());
				varList = varList.get(2).asReduction();
			}
			if (_structDef.size() > 1) {
				_structDef = _structDef.get(1).asReduction();
			}
		} while (_structDef != null);
		return params.concatenate(", ");
	}

	/**
	 * Converts a detected C library function to the corresponding Structorizer
	 * built-in routine if possible.
	 * @param _reduction a rule of type &lt;Value&gt; ::= Id '(' [&lt;Expr&gt;] ')'
	 * @param procName - the already extracted routine identifier
	 * @param arguments - list of argument strings
	 * @param _parentNode - the Subqueue the derived instruction is to be appended to 
	 * @return true if a specific conversion could be applied and all is done.
	 */
	private boolean convertBuiltInRoutines(Reduction _reduction, String procName, StringList arguments, Subqueue _parentNode) {
		// TODO Auto-generated method stub
		// Here we should convert certain known library functions to Structorizer built-in procedures
		return false;
	}

	/**
	 * Converts a rule with head &lt;Var&gt; (as part of a declaration) into an
	 * Instruction element.  
	 * @param _reduc - the Reduction object (PROD_VAR_ID or PROD_VAR_ID_EQ)
	 * @param _type - the data type as string
	 * @param _parentNode - the Subqueue the built Instruction is to be appended to
	 */
	private void buildDeclOrAssignment(Reduction _reduc, String _type, Subqueue _parentNode)
	{
		boolean isConstant = _type != null && _type.startsWith("const ");
		int ruleId = _reduc.getParent().getTableIndex();
		String content = getContent_R(_reduc, "");	// Default???
		if (ruleId == RuleConstants.PROD_VAR_ID) {
			if (debugprint) {
				System.out.println("\ttrying PROD_VAR_ID ...");
			}
			// Simple declaration - if allowed then make it to a Pascal decl.
			if (this.optionImportVarDecl) {
				content = "var " + content + ": " + _type;
				Element instr = new Instruction(translateContent(content));
				if (_parentNode.parent instanceof Root && ((Root)_parentNode.parent).getMethodName().equals("???")) {
					instr.setComment("globally declared!");
					instr.setColor(colorGlobal);
				}
				else {
					instr.setColor(colorDecl);	// local declarations with a smooth green
				}
				_parentNode.addElement(instr);
			}
		}
		else if (ruleId == RuleConstants.PROD_VARITEM) {
			if (debugprint) {
				System.out.println("\ttrying PROD_VARITEM ...");
			}
			// This should be the <Pointers> token...
			String ptype = this.getContent_R(_reduc.get(0).asReduction(), _type);
			// .. and this is assumed to be the <Var> token
			buildDeclOrAssignment(_reduc.get(1).asReduction(), ptype, _parentNode);
		}
		else if (ruleId == RuleConstants.PROD_VAR_ID_EQ) {
			// assignment
			if (debugprint) {
				System.out.println("\ttrying PROD_VAR_ID_EQ ...");
			}
			// Should be RuleConstants.PROD_VAR_ID_EQ. Now it can get tricky if arrays
			// are involved - remember this is a declaration rule!
			// The executor now copes with lines like int data[4] <- {2,5,6,3}. On the
			// other hand, an import without the type but with index brackets would
			// induce a totally wrong semantics. So we must drop both or none.
			// Without declaration however, the parser won't accept initializers
			// anymore - which is sound with ANSI C.
			// Don't be afraid of multidimensional arrays. The grammar doesn't accept
			// multiple indices in a declaration (only in assignments or as expression)
			String varName = _reduc.get(0).asString();
			//String arrayTag = this.getContent_R((Reduction)thdReduc.getToken(1).getData(), "");
			String expr = this.getContent_R(_reduc.get(3).asReduction(), "");
			content = translateContent(content);
			Element instr = null;
			if (this.optionImportVarDecl) {
				instr = new Instruction(translateContent(_type) + " " + content);
				if (isConstant) {
					instr.setColor(colorConst);
				}
			}
			else {
				instr = new Instruction((isConstant ? "const " : "") + varName + " <- " + translateContent(expr));
				if (isConstant) {
					instr.setColor(colorConst);
				}
			}
			if (_parentNode.parent instanceof Root && ((Root)_parentNode.parent).getMethodName().equals("???")) {
				instr.setComment("globally declared!");
				instr.setColor(colorGlobal);
			}
			_parentNode.addElement(instr);
		}
		else if (debugprint) {
			System.out.println("\tfallen back with rule " + ruleId + " (" + _reduc.getParent().toString() + ")");
		}
	}
	
	/**
	 * Converts a rule of type PROD_NORMALSTM_SWITCH_LPAREN_RPAREN_LBRACE_RBRACE into the
	 * skeleton of a Case element. The case branches will be handled separately
	 * @param _reduction - Reduction rule of a switch instruction
	 * @param _parentNode - the Subqueue this Case elemen is to be appended to
	 */
	private void buildCase(Reduction _reduction, Subqueue _parentNode)
	{
		String content = new String();
		// Put the discriminator into the first line of content
		content = getKeyword("preCase")+getContent_R(_reduction.get(2).asReduction(), content)+getKeyword("postCase");

		// How many branches has the CASE element? We must count the non-empty statement lists!
		Reduction sr = _reduction.get(5).asReduction();
		int j = 0;
		//System.out.println(sr.getParentRule().getText());  // <<<<<<<
		while (sr.getParent().getTableIndex() == RuleConstants.PROD_CASESTMS_CASE_COLON)
		{
			Reduction stmList = (Reduction) sr.get(3).getData();
			if (stmList.getParent().getTableIndex() == RuleConstants.PROD_STMLIST) {
				// non-empty statement list, so we will have to set up a branch
				j++;
				content += "\n??";
			}
			sr = sr.get(4).asReduction();
		}

		if (sr.getParent().getTableIndex() == RuleConstants.PROD_CASESTMS_DEFAULT_COLON)
		{
			content += "\ndefault";
		}
		else {
			content += "\n%";
		}
		j++;

		// Pooh, the translation is risky...
		Case ele = new Case(translateContent(content));
		//ele.setText(updateContent(content));
		_parentNode.addElement(ele);

		// Create the selector branches
		Reduction secReduc = _reduction.get(5).asReduction();
		buildNSD_R(secReduc, (Subqueue) ele.qs.get(0));

		// In theory, all branches should end with a break instruction
		// unless they end with return or exit. Drop the break instructions
		// (and only these) now.
		for (int i = 0; i < ele.qs.size(); i++) {
			Subqueue sq = ele.qs.get(i);
			int size = sq.getSize();
			if (size > 0) {
				Element el = sq.getElement(size-1);
				if (el instanceof Jump && ((Jump)el).isLeave()) {
					sq.removeElement(size-1);
				}
			}
		}

		// cut off else, if possible
		if (((Subqueue) ele.qs.get(j-1)).getSize()==0)
		{
			ele.getText().set(ele.getText().count()-1,"%");
		}

	}
	
	private void buildCaseBranch(Reduction _reduction, int _ruleId, Case _case)
	{
		// We should first make clear what could happen here. A case analysis
		// switch(discriminator) {
		// case 1:
		// case 2:		// to be merged with previous one -> selector 1,2
		//    instr21;
		//    instr22;
		// case 3:		// can be merged with 1,2 if instr1; instr2; are put to an alternative
		// case 4:		// to be merged with previous one 
		//    instr41;	// either to be copied to case 1,2 if 3 hadn't been merged with 1,2
		//    instr42;	//		or to be merged to case 1,2,3 (if that had an alternative)
		//    break;	// To be removed after all branches are complete 
		// case 5:		// new branch
		//    instr51;
		//    instr52;
		//    return;	// Must not be removed
		// case 6:		// new branch
		// default:		// cannot be merged with previous branch
		//    instr0;	// must be copied to case 6
		//    [break;]	// To be removed after all branches are complete
		// }
		// The first (easier) approach here is to copy/append instr41; instr42; (and instr0;)
		int nLines = _case.getText().count();
		int iNext = 0;	// line index of the next free selector entry
		// buildCase(...) had marked all selector lines (but the default) with "??"
		for (int i = 1; i < nLines && iNext == 0; i++) {
			if (_case.getText().get(i).equals("??"))
			{
				iNext = i;
			}
		}
		// Only default branch open? Then select the last line
		// Be aware, though, that this rule is not necessarily the default branch rule!
		// (The previous branch may be empty such that we are to merge them)
		if (iNext == 0) { iNext = nLines-1; }
		
		// Now we must find out whether this branch is to be merged with the previous one
		boolean lastCaseWasEmpty = iNext > 1 && _case.qs.get(iNext-2).getSize() == 0;
		int stmListIx = 2;	// <Stm List> index for default rule (has different structure)...
		// Now we first handle the branches with explicit selector
		if (_ruleId == RuleConstants.PROD_CASESTMS_CASE_COLON) {
			// <Case Stms> ::= case <Value> ':' <Stm List> <Case Stms>
			// Get the selector constant
			String selector = getContent_R(_reduction.get(1).asReduction(), "");
			// If the last branch was empty then just add the selector to the list
			// and reduce the index
			if (lastCaseWasEmpty) {
				String selectors = _case.getText().get(iNext-1) + ", " + selector;
				_case.getText().set(iNext - 1, selectors);
				iNext--;
			}
			else {
				_case.getText().set(iNext, selector);
			}
			stmListIx = 3;	// <Stm List> index for explicit branch
		}
		// Add the branch content
		Reduction secReduc = _reduction.get(stmListIx).asReduction();
		Subqueue sq = (Subqueue) _case.qs.get(iNext-1);
		// Fill the branch with the instructions (if there are any)
		buildNSD_R(secReduc, sq);
				
		// Which is the last branch ending with jump instruction?
		int lastCaseWithJump = iNext-1;
		for (int i = iNext-2; i >= 0; i--) {
			int size = _case.qs.get(i).getSize();
			if (size > 0 && (_case.qs.get(i).getElement(size-1) instanceof Jump)) {
				lastCaseWithJump = i;
				break;
			}
		}
		// append copies of the elements of the new case to all cases still not terminated
		for (int i = lastCaseWithJump+1; i < iNext-1; i++) {
			Subqueue sq1 = _case.qs.get(i);
			for (int j = 0; j < sq.getSize(); j++) {
				Element el = sq.getElement(j).copy();	// FIXME: Need a new Id!
				sq1.addElement(el);
			}
		}
		
		// If this is an explicit case branch then the last token holds the subsequent branches
		if (_ruleId == RuleConstants.PROD_CASESTMS_CASE_COLON) {
			// We may pass an arbitrary subqueue, the case branch rule goes up to the Case element anyway
			buildNSD_R(_reduction.get(stmListIx+1).asReduction(), _case.qs.get(0));					
		}
		
	}
	
	private void buildOutput(Reduction _reduction, String _name, StringList _args, Subqueue _parentNode)
	{
		//content = content.replaceAll(BString.breakup("printf")+"[ ((](.*?)[))]", output+" $1");
		String content = getKeyword("output") + " ";
		if (_args != null) {
			int nExpr = _args.count();
			// Find the format mask
			if (nExpr > 1 && _name.equals("printf") && _args.get(0).matches("^[\"].*[\"]$")) {
				// We try to split the string by the "%" signs which is of course dirty
				// Unfortunately, we can't use split because it eats empty chunks
				StringList newExprList = new StringList();
				String formatStr = _args.get(0);
				int posPerc = -1;
				formatStr = formatStr.substring(1, formatStr.length()-1);
				int i = 1;
				while ((posPerc = formatStr.indexOf('%')) > 0 && i < _args.count()) {
					newExprList.add('"' + formatStr.substring(0, posPerc) + '"');
					formatStr = formatStr.substring(posPerc+1).replaceFirst(".*?[idxucsefg](.*)", "$1");
					newExprList.add(_args.get(i++));
				}
				if (!formatStr.isEmpty()) {
					newExprList.add('"' + formatStr + '"');
				}
				if (i < _args.count()) {
					newExprList.add(_args.concatenate(", "));
				}
				_args = newExprList;
			}
			else {
				// Drop an end-standing newline since Structorizer produces a newline automatically
				String last = _args.get(nExpr - 1);
				if (last.equals("\"\n\"")) {
					_args.remove(--nExpr);
				}
				else if (last.endsWith("\n\"")) {
					_args.set(nExpr-1, last.substring(0, last.length()-2) + '"');
				}
			}
			content += _args.concatenate(", ");
		}
		_parentNode.addElement(new Instruction(content.trim()));
	}

	private void buildInput(Reduction _reduction, String _name, StringList _args, Subqueue _parentNode)
	{
		//content = content.replaceAll(BString.breakup("scanf")+"[ ((](.*?),[ ]*[&]?(.*?)[))]", input+" $2");
		String content = getKeyword("input");
		if (_args != null) {
			// Forget the format string
			if (_name.equals("scanf")) {
				_args.remove(0);
				for (int i = 0; i < _args.count(); i++) {
					String varItem = _args.get(i).trim();
					if (varItem.startsWith("&")) {
						_args.set(i, varItem.substring(1));
					}
				}
			}
			content += _args.concatenate(", ");
		}
		_parentNode.addElement(new Instruction(content.trim()));
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

		// START KGU 2017-04-11
		_content = undoIdReplacements(_content);
		// END KGU 2017-04-11
		return _content.trim();
	}
	
	@Override
	protected String getContent_R(Reduction _reduction, String _content)
	{
		for(int i=0; i<_reduction.size(); i++)
		{
			Token token = _reduction.get(i);
			switch (token.getType()) 
			{
			case NON_TERMINAL:
				_content = getContent_R(token.asReduction(), _content);	
				break;
			case CONTENT:
			{
				String toAdd = token.asString();
				int idx = token.getTableIndex();
				switch (idx) {
				case SymbolConstants.SYM_EXCLAM:
					_content += " not ";
					break;
				case SymbolConstants.SYM_PERCENT:
					_content += " mod ";
					break;
				case SymbolConstants.SYM_AMPAMP:
					_content += " and ";
					break;
				case SymbolConstants.SYM_PIPEPIPE:
					_content += " or ";
					break;
				case SymbolConstants.SYM_LTLT:
					_content += " shl ";
					break;
				case SymbolConstants.SYM_GTGT:
					_content += " shr ";
					break;
				case SymbolConstants.SYM_EQ:
					_content += " <- ";
					break;
				case SymbolConstants.SYM_EQEQ:
					_content += " = ";
					break;
				case SymbolConstants.SYM_EXCLAMEQ:
					_content += " <> ";
					break;
				case SymbolConstants.SYM_LBRACE:
					_content += " {";
					break;
				case SymbolConstants.SYM_RBRACE:
					_content += "} ";
					break;
				case SymbolConstants.SYM_MINUS:
				case SymbolConstants.SYM_PLUS:
				case SymbolConstants.SYM_TIMES:
				case SymbolConstants.SYM_DIV:
				case SymbolConstants.SYM_AMP:
				case SymbolConstants.SYM_LT:
				case SymbolConstants.SYM_GT:
				case SymbolConstants.SYM_LTEQ:
				case SymbolConstants.SYM_GTEQ:
				case SymbolConstants.SYM_CARET:
				case SymbolConstants.SYM_PIPE:
				case SymbolConstants.SYM_TILDE:
					_content += " " + toAdd + " ";
					break;
				case SymbolConstants.SYM_STRINGLITERAL:
					if (toAdd.trim().startsWith("L")) {
						toAdd = toAdd.trim().substring(1);
					}
					_content += toAdd;
					break;
				case SymbolConstants.SYM_DECLITERAL:
				case SymbolConstants.SYM_HEXLITERAL:
				case SymbolConstants.SYM_OCTLITERAL:
				case SymbolConstants.SYM_FLOATLITERAL:
					// Remove type-specific suffixes
					if (toAdd.matches(".*?[uUlL]+")) {
						toAdd = toAdd.replaceAll("(.*?)[uUlL]+", "$1");
					}
					else if (idx == SymbolConstants.SYM_FLOATLITERAL && toAdd.matches(".+?[fF]")) {
						toAdd = toAdd.replaceAll("(.+?)[fFlL]", "$1");
					}
				default:
					if (toAdd.matches("^\\w.*") && _content.matches(".*\\w$") || _content.matches(".*[,;]$")) {
						_content += " ";
					}
					_content += toAdd;
				}
			}
			break;
			default:
				break;
			}
		}
		
		return _content;
	}
	
	/**
	 * Routine will return the list of the (translated) expressions (at top level, if a tree is needed,
	 * than keep with the reduction tree.)
	 * @param _reduc - a rule with head &lt;Expr&gt; or &lt;ExprIni&gt; 
	 * @return the list of expressions as strings
	 */
	private StringList getExpressionList(Reduction _reduc)
	{
		StringList exprList = new StringList();
		String ruleHead = _reduc.getParent().getHead().toString();
		while (ruleHead.equals("<Expr>") || ruleHead.equals("<ExprIni>")) {
			// Get the content from right to left to avoid recursion
			exprList.add(getContent_R(_reduc.get(_reduc.size()-1).asReduction(), ""));
			if (_reduc.size() > 1) {
				_reduc = _reduc.get(0).asReduction();
				ruleHead = _reduc.getParent().getHead().toString();
				if (!ruleHead.equals("<Expr>")) {
					exprList.add(getContent_R(_reduc, ""));	
				}
			}
			else {
				ruleHead = "";
				//exprList.add(getContent_R(_reduc.get(0).asReduction(), ""));
			}
		}
		return exprList.reverse();
	}
	
	//------------------------- Postprocessor ---------------------------

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.parsers.CodeParser#subclassUpdateRoot(lu.fisch.structorizer.elements.Root, java.lang.String)
	 */
	@Override
	protected void subclassUpdateRoot(Root aRoot, String textToParse) {
		if (aRoot.getMethodName().equals("main")) {
			String fileName = new File(textToParse).getName();
			if (fileName.contains(".")) {
				fileName = fileName.substring(0, fileName.indexOf('.'));
			}
			if (this.optionUpperCaseProgName) {
				fileName = fileName.toUpperCase();
			}
			fileName = fileName.replace(" ", "_");
			if (aRoot.getParameterNames().count() > 0) {
				String header = aRoot.getText().getText();
				header = header.replaceFirst("(.*?)main([((].*)", "$1" + fileName + "$2");
				aRoot.setText(header);
			}
			else {
				aRoot.setText(fileName);
				aRoot.isProgram = true;
			}
			// Are there some global definitions to be imported?
			if (this.globalRoot != null) {
				String progName = fileName + "Globals";
				this.globalRoot.setText(progName);
				this.globalRoot.isProgram = true;
				for (Call provCall: this.provisionalImportCalls) {
					provCall.setText(provCall.getText().get(0).replace("???", progName));
				}
				this.provisionalImportCalls.clear();
			}
		}
		// START KGU#376 2017-04-11: enh. #389 import mechanism for globals
		if (this.globalRoot != null && this.globalRoot != aRoot) {
			String globalName = this.globalRoot.getMethodName();
			Call importCall = new Call(getKeywordOrDefault("preImport", "import") + " " + (globalName.equals("???") ? defaultGlobalName : globalName));
			importCall.setColor(colorGlobal);
			aRoot.children.insertElementAt(importCall, 0);
			if (globalName.equals("???")) {
				this.provisionalImportCalls.add(importCall);
			}
		}
		// END KGU#376 2017-04-11
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.parsers.CodeParser#postProcess(java.lang.String)
	 */
	protected void subclassPostProcess(String textToParse)
	{
		// May there was no main function but global definitions
		if (this.globalRoot != null && this.globalRoot.getMethodName().equals("???") && this.globalRoot.children.getSize() > 0) {
			this.globalRoot.setText(defaultGlobalName);
			this.globalRoot.isProgram = true;
		}
	}

}

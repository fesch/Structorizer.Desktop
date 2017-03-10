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

import java.awt.Color;

/**
 ******************************************************************************************************
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
import lu.fisch.structorizer.elements.ILoop;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.elements.Jump;
import lu.fisch.structorizer.elements.Repeat;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.structorizer.elements.While;
import lu.fisch.utils.BString;
import lu.fisch.utils.StringList;

/**
 * Code import parser class of Structorizer 3.27, based on GOLDParser 5.0 for the %Name% language.
 * This file cntains grammar-specific constants and individual routines to build
 * structograms (Nassi-Shneiderman diagrams) from the parsing tree. 
 * @author Kay Gürtzig
 */
public class CParser extends CodeParser
{
	
	// This is a switch for debugging - if set to true then the build process will be logged to System.out
	private boolean debugprint = false;
	
	//---------------------- Grammar specification ---------------------------

	@Override
	protected final String getCompiledGrammar()
	{
		return "C-ANSIplus.egt";
	}
	
	@Override
	protected final String getGrammarTableName() {
		return "C-ANSIplus";
	}
	
	/**
	 * If this flag is set then program names derived from file name will be made uppercase
	 * This default will be initialized in consistentlywith the Analyser check 
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
	       final int SYM_EXCLAM        =   8;  // '!'
	       final int SYM_EXCLAMEQ      =   9;  // '!='
	       final int SYM_PERCENT       =  10;  // '%'
	       final int SYM_AMP           =  11;  // '&'
	       final int SYM_AMPAMP        =  12;  // '&&'
	       final int SYM_AMPEQ         =  13;  // '&='
	       final int SYM_LPAREN        =  14;  // '('
	       final int SYM_RPAREN        =  15;  // ')'
	       final int SYM_TIMES         =  16;  // '*'
	       final int SYM_TIMESEQ       =  17;  // '*='
	       final int SYM_PLUS          =  18;  // '+'
	       final int SYM_PLUSPLUS      =  19;  // '++'
	       final int SYM_PLUSEQ        =  20;  // '+='
	       final int SYM_COMMA         =  21;  // ','
	       final int SYM_MINUS         =  22;  // '-'
	       final int SYM_MINUSMINUS    =  23;  // '--'
	       final int SYM_MINUSEQ       =  24;  // '-='
	       final int SYM_MINUSGT       =  25;  // '->'
	       final int SYM_DOT           =  26;  // '.'
	       final int SYM_DIV           =  27;  // '/'
	       final int SYM_DIVEQ         =  28;  // '/='
	       final int SYM_COLON         =  29;  // ':'
	       final int SYM_SEMI          =  30;  // ';'
	       final int SYM_LT            =  31;  // '<'
	       final int SYM_LTLT          =  32;  // '<<'
	       final int SYM_LTLTEQ        =  33;  // '<<='
	       final int SYM_LTEQ          =  34;  // '<='
	       final int SYM_EQ            =  35;  // '='
	       final int SYM_EQEQ          =  36;  // '=='
	       final int SYM_GT            =  37;  // '>'
	       final int SYM_GTEQ          =  38;  // '>='
	       final int SYM_GTGT          =  39;  // '>>'
	       final int SYM_GTGTEQ        =  40;  // '>>='
	       final int SYM_QUESTION      =  41;  // '?'
	       final int SYM_AUTO          =  42;  // auto
	       final int SYM_BREAK         =  43;  // break
	       final int SYM_CASE          =  44;  // case
	       final int SYM_CHAR          =  45;  // char
	       final int SYM_CHARLITERAL   =  46;  // CharLiteral
	       final int SYM_CONST         =  47;  // const
	       final int SYM_CONTINUE      =  48;  // continue
	       final int SYM_DECLITERAL    =  49;  // DecLiteral
	       final int SYM_DEFAULT       =  50;  // default
	       final int SYM_DO            =  51;  // do
	       final int SYM_DOUBLE        =  52;  // double
	       final int SYM_ELSE          =  53;  // else
	       final int SYM_ENUM          =  54;  // enum
	       final int SYM_EXTERN        =  55;  // extern
	       final int SYM_FLOAT         =  56;  // float
	       final int SYM_FLOATLITERAL  =  57;  // FloatLiteral
	       final int SYM_FOR           =  58;  // for
	       final int SYM_GOTO          =  59;  // goto
	       final int SYM_HEXLITERAL    =  60;  // HexLiteral
	       final int SYM_ID            =  61;  // Id
	       final int SYM_IF            =  62;  // if
	       final int SYM_INT           =  63;  // int
	       final int SYM_LONG          =  64;  // long
	       final int SYM_OCTLITERAL    =  65;  // OctLiteral
	       final int SYM_REGISTER      =  66;  // register
	       final int SYM_RETURN        =  67;  // return
	       final int SYM_SHORT         =  68;  // short
	       final int SYM_SIGNED        =  69;  // signed
	       final int SYM_SIZEOF        =  70;  // sizeof
	       final int SYM_STATIC        =  71;  // static
	       final int SYM_STRINGLITERAL =  72;  // StringLiteral
	       final int SYM_STRUCT        =  73;  // struct
	       final int SYM_SWITCH        =  74;  // switch
	       final int SYM_TYPEDEF       =  75;  // typedef
	       final int SYM_UNION         =  76;  // union
	       final int SYM_UNSIGNED      =  77;  // unsigned
	       final int SYM_VOID          =  78;  // void
	       final int SYM_VOLATILE      =  79;  // volatile
	       final int SYM_WHILE         =  80;  // while
	       final int SYM_LBRACKET      =  81;  // '['
	       final int SYM_RBRACKET      =  82;  // ']'
	       final int SYM_CARET         =  83;  // '^'
	       final int SYM_CARETEQ       =  84;  // '^='
	       final int SYM_LBRACE        =  85;  // '{'
	       final int SYM_PIPE          =  86;  // '|'
	       final int SYM_PIPEEQ        =  87;  // '|='
	       final int SYM_PIPEPIPE      =  88;  // '||'
	       final int SYM_RBRACE        =  89;  // '}'
	       final int SYM_TILDE         =  90;  // '~'
	       final int SYM_ARG           =  91;  // <Arg>
	       final int SYM_ARRAY         =  92;  // <Array>
	       final int SYM_BASE          =  93;  // <Base>
	       final int SYM_BLOCK         =  94;  // <Block>
	       final int SYM_CASESTMS      =  95;  // <Case Stms>
	       final int SYM_DECL          =  96;  // <Decl>
	       final int SYM_DECLS         =  97;  // <Decls>
	       final int SYM_ENUMDECL      =  98;  // <Enum Decl>
	       final int SYM_ENUMDEF       =  99;  // <Enum Def>
	       final int SYM_ENUMVAL       = 100;  // <Enum Val>
	       final int SYM_EXPR          = 101;  // <Expr>
	       final int SYM_EXPRINI       = 102;  // <ExprIni>
	       final int SYM_FUNCDECL      = 103;  // <Func Decl>
	       final int SYM_FUNCID        = 104;  // <Func ID>
	       final int SYM_FUNCPROTO     = 105;  // <Func Proto>
	       final int SYM_IDLIST        = 106;  // <Id List>
	       final int SYM_INITIALIZER   = 107;  // <Initializer>
	       final int SYM_MOD           = 108;  // <Mod>
	       final int SYM_NORMALSTM     = 109;  // <Normal Stm>
	       final int SYM_OPADD         = 110;  // <Op Add>
	       final int SYM_OPAND         = 111;  // <Op And>
	       final int SYM_OPASSIGN      = 112;  // <Op Assign>
	       final int SYM_OPBINAND      = 113;  // <Op BinAND>
	       final int SYM_OPBINOR       = 114;  // <Op BinOR>
	       final int SYM_OPBINXOR      = 115;  // <Op BinXOR>
	       final int SYM_OPCOMPARE     = 116;  // <Op Compare>
	       final int SYM_OPEQUATE      = 117;  // <Op Equate>
	       final int SYM_OPIF          = 118;  // <Op If>
	       final int SYM_OPMULT        = 119;  // <Op Mult>
	       final int SYM_OPOR          = 120;  // <Op Or>
	       final int SYM_OPPOINTER     = 121;  // <Op Pointer>
	       final int SYM_OPSHIFT       = 122;  // <Op Shift>
	       final int SYM_OPUNARY       = 123;  // <Op Unary>
	       final int SYM_PARAM         = 124;  // <Param>
	       final int SYM_PARAMS        = 125;  // <Params>
	       final int SYM_POINTERS      = 126;  // <Pointers>
	       final int SYM_SCALAR        = 127;  // <Scalar>
	       final int SYM_SIGN          = 128;  // <Sign>
	       final int SYM_STM           = 129;  // <Stm>
	       final int SYM_STMLIST       = 130;  // <Stm List>
	       final int SYM_STRUCTDECL    = 131;  // <Struct Decl>
	       final int SYM_STRUCTDEF     = 132;  // <Struct Def>
	       final int SYM_THENSTM       = 133;  // <Then Stm>
	       final int SYM_TYPE          = 134;  // <Type>
	       final int SYM_TYPEDEFDECL   = 135;  // <Typedef Decl>
	       final int SYM_TYPES         = 136;  // <Types>
	       final int SYM_UNIONDECL     = 137;  // <Union Decl>
	       final int SYM_VALUE         = 138;  // <Value>
	       final int SYM_VAR           = 139;  // <Var>
	       final int SYM_VARDECL       = 140;  // <Var Decl>
	       final int SYM_VARITEM       = 141;  // <Var Item>
	       final int SYM_VARLIST       = 142;  // <Var List>
	};

	// Symbolic constants naming the table indices of the grammar rules
	private interface RuleConstants
	{
	       final int PROD_DECLS                                        =   0;  // <Decls> ::= <Decl> <Decls>
	       final int PROD_DECLS2                                       =   1;  // <Decls> ::= 
	       final int PROD_DECL                                         =   2;  // <Decl> ::= <Func Decl>
	       final int PROD_DECL2                                        =   3;  // <Decl> ::= <Func Proto>
	       final int PROD_DECL3                                        =   4;  // <Decl> ::= <Struct Decl>
	       final int PROD_DECL4                                        =   5;  // <Decl> ::= <Union Decl>
	       final int PROD_DECL5                                        =   6;  // <Decl> ::= <Enum Decl>
	       final int PROD_DECL6                                        =   7;  // <Decl> ::= <Var Decl>
	       final int PROD_DECL7                                        =   8;  // <Decl> ::= <Typedef Decl>
	       final int PROD_FUNCPROTO_LPAREN_RPAREN_SEMI                 =   9;  // <Func Proto> ::= <Func ID> '(' <Types> ')' ';'
	       final int PROD_FUNCPROTO_LPAREN_RPAREN_SEMI2                =  10;  // <Func Proto> ::= <Func ID> '(' <Params> ')' ';'
	       final int PROD_FUNCPROTO_LPAREN_VOID_RPAREN_SEMI            =  11;  // <Func Proto> ::= <Func ID> '(' void ')' ';'
	       final int PROD_FUNCPROTO_LPAREN_RPAREN_SEMI3                =  12;  // <Func Proto> ::= <Func ID> '(' ')' ';'
	       final int PROD_FUNCDECL_LPAREN_RPAREN                       =  13;  // <Func Decl> ::= <Func ID> '(' <Params> ')' <Block>
	       final int PROD_FUNCDECL_LPAREN_RPAREN2                      =  14;  // <Func Decl> ::= <Func ID> '(' <Id List> ')' <Struct Def> <Block>
	       final int PROD_FUNCDECL_LPAREN_VOID_RPAREN                  =  15;  // <Func Decl> ::= <Func ID> '(' void ')' <Block>
	       final int PROD_FUNCDECL_LPAREN_RPAREN3                      =  16;  // <Func Decl> ::= <Func ID> '(' ')' <Block>
	       final int PROD_PARAMS_COMMA                                 =  17;  // <Params> ::= <Param> ',' <Params>
	       final int PROD_PARAMS                                       =  18;  // <Params> ::= <Param>
	       final int PROD_PARAM_CONST_ID                               =  19;  // <Param> ::= const <Type> Id <Array>
	       final int PROD_PARAM_ID                                     =  20;  // <Param> ::= <Type> Id <Array>
	       final int PROD_TYPES_COMMA                                  =  21;  // <Types> ::= <Type> ',' <Types>
	       final int PROD_TYPES                                        =  22;  // <Types> ::= <Type>
	       final int PROD_IDLIST_ID_COMMA                              =  23;  // <Id List> ::= Id ',' <Id List>
	       final int PROD_IDLIST_ID                                    =  24;  // <Id List> ::= Id
	       final int PROD_FUNCID_ID                                    =  25;  // <Func ID> ::= <Type> Id
	       final int PROD_FUNCID_VOID_ID                               =  26;  // <Func ID> ::= void Id
	       final int PROD_FUNCID_ID2                                   =  27;  // <Func ID> ::= Id
	       final int PROD_TYPEDEFDECL_TYPEDEF_ID_SEMI                  =  28;  // <Typedef Decl> ::= typedef <Type> Id ';'
	       final int PROD_STRUCTDECL_STRUCT_ID_LBRACE_RBRACE_SEMI      =  29;  // <Struct Decl> ::= struct Id '{' <Struct Def> '}' ';'
	       final int PROD_UNIONDECL_UNION_ID_LBRACE_RBRACE_SEMI        =  30;  // <Union Decl> ::= union Id '{' <Struct Def> '}' ';'
	       final int PROD_STRUCTDEF                                    =  31;  // <Struct Def> ::= <Var Decl> <Struct Def>
	       final int PROD_STRUCTDEF2                                   =  32;  // <Struct Def> ::= <Var Decl>
	       final int PROD_VARDECL_SEMI                                 =  33;  // <Var Decl> ::= <Mod> <Type> <Var> <Var List> ';'
	       final int PROD_VARDECL_SEMI2                                =  34;  // <Var Decl> ::= <Type> <Var> <Var List> ';'
	       final int PROD_VARDECL_SEMI3                                =  35;  // <Var Decl> ::= <Mod> <Var> <Var List> ';'
	       final int PROD_VAR_ID                                       =  36;  // <Var> ::= Id <Array>
	       final int PROD_VAR_ID_EQ                                    =  37;  // <Var> ::= Id <Array> '=' <Initializer>
	       final int PROD_ARRAY_LBRACKET_RBRACKET                      =  38;  // <Array> ::= '[' <Expr> ']'
	       final int PROD_ARRAY_LBRACKET_RBRACKET2                     =  39;  // <Array> ::= '[' ']'
	       final int PROD_ARRAY                                        =  40;  // <Array> ::= 
	       final int PROD_VARLIST_COMMA                                =  41;  // <Var List> ::= ',' <Var Item> <Var List>
	       final int PROD_VARLIST                                      =  42;  // <Var List> ::= 
	       final int PROD_VARITEM                                      =  43;  // <Var Item> ::= <Pointers> <Var>
	       final int PROD_MOD_EXTERN                                   =  44;  // <Mod> ::= extern
	       final int PROD_MOD_STATIC                                   =  45;  // <Mod> ::= static
	       final int PROD_MOD_REGISTER                                 =  46;  // <Mod> ::= register
	       final int PROD_MOD_AUTO                                     =  47;  // <Mod> ::= auto
	       final int PROD_MOD_VOLATILE                                 =  48;  // <Mod> ::= volatile
	       final int PROD_MOD_CONST                                    =  49;  // <Mod> ::= const
	       final int PROD_ENUMDECL_ENUM_ID_LBRACE_RBRACE_SEMI          =  50;  // <Enum Decl> ::= enum Id '{' <Enum Def> '}' ';'
	       final int PROD_ENUMDEF_COMMA                                =  51;  // <Enum Def> ::= <Enum Val> ',' <Enum Def>
	       final int PROD_ENUMDEF                                      =  52;  // <Enum Def> ::= <Enum Val>
	       final int PROD_ENUMVAL_ID                                   =  53;  // <Enum Val> ::= Id
	       final int PROD_ENUMVAL_ID_EQ_OCTLITERAL                     =  54;  // <Enum Val> ::= Id '=' OctLiteral
	       final int PROD_ENUMVAL_ID_EQ_HEXLITERAL                     =  55;  // <Enum Val> ::= Id '=' HexLiteral
	       final int PROD_ENUMVAL_ID_EQ_DECLITERAL                     =  56;  // <Enum Val> ::= Id '=' DecLiteral
	       final int PROD_TYPE                                         =  57;  // <Type> ::= <Base> <Pointers>
	       final int PROD_BASE                                         =  58;  // <Base> ::= <Sign> <Scalar>
	       final int PROD_BASE_STRUCT_ID                               =  59;  // <Base> ::= struct Id
	       final int PROD_BASE_STRUCT_LBRACE_RBRACE                    =  60;  // <Base> ::= struct '{' <Struct Def> '}'
	       final int PROD_BASE_UNION_ID                                =  61;  // <Base> ::= union Id
	       final int PROD_BASE_UNION_LBRACE_RBRACE                     =  62;  // <Base> ::= union '{' <Struct Def> '}'
	       final int PROD_BASE_ENUM_ID                                 =  63;  // <Base> ::= enum Id
	       final int PROD_BASE_VOID_TIMES                              =  64;  // <Base> ::= void '*'
	       final int PROD_SIGN_SIGNED                                  =  65;  // <Sign> ::= signed
	       final int PROD_SIGN_UNSIGNED                                =  66;  // <Sign> ::= unsigned
	       final int PROD_SIGN                                         =  67;  // <Sign> ::= 
	       final int PROD_SCALAR_CHAR                                  =  68;  // <Scalar> ::= char
	       final int PROD_SCALAR_INT                                   =  69;  // <Scalar> ::= int
	       final int PROD_SCALAR_SHORT                                 =  70;  // <Scalar> ::= short
	       final int PROD_SCALAR_LONG                                  =  71;  // <Scalar> ::= long
	       final int PROD_SCALAR_SHORT_INT                             =  72;  // <Scalar> ::= short int
	       final int PROD_SCALAR_LONG_INT                              =  73;  // <Scalar> ::= long int
	       final int PROD_SCALAR_FLOAT                                 =  74;  // <Scalar> ::= float
	       final int PROD_SCALAR_DOUBLE                                =  75;  // <Scalar> ::= double
	       final int PROD_POINTERS_TIMES                               =  76;  // <Pointers> ::= '*' <Pointers>
	       final int PROD_POINTERS                                     =  77;  // <Pointers> ::= 
	       final int PROD_STM                                          =  78;  // <Stm> ::= <Var Decl>
	       final int PROD_STM_ID_COLON                                 =  79;  // <Stm> ::= Id ':'
	       final int PROD_STM_IF_LPAREN_RPAREN                         =  80;  // <Stm> ::= if '(' <Expr> ')' <Stm>
	       final int PROD_STM_IF_LPAREN_RPAREN_ELSE                    =  81;  // <Stm> ::= if '(' <Expr> ')' <Then Stm> else <Stm>
	       final int PROD_STM_WHILE_LPAREN_RPAREN                      =  82;  // <Stm> ::= while '(' <Expr> ')' <Stm>
	       final int PROD_STM_FOR_LPAREN_SEMI_SEMI_RPAREN              =  83;  // <Stm> ::= for '(' <Arg> ';' <Arg> ';' <Arg> ')' <Stm>
	       final int PROD_STM2                                         =  84;  // <Stm> ::= <Normal Stm>
	       final int PROD_THENSTM_IF_LPAREN_RPAREN_ELSE                =  85;  // <Then Stm> ::= if '(' <Expr> ')' <Then Stm> else <Then Stm>
	       final int PROD_THENSTM_WHILE_LPAREN_RPAREN                  =  86;  // <Then Stm> ::= while '(' <Expr> ')' <Then Stm>
	       final int PROD_THENSTM_FOR_LPAREN_SEMI_SEMI_RPAREN          =  87;  // <Then Stm> ::= for '(' <Arg> ';' <Arg> ';' <Arg> ')' <Then Stm>
	       final int PROD_THENSTM                                      =  88;  // <Then Stm> ::= <Normal Stm>
	       final int PROD_NORMALSTM_DO_WHILE_LPAREN_RPAREN             =  89;  // <Normal Stm> ::= do <Stm> while '(' <Expr> ')'
	       final int PROD_NORMALSTM_SWITCH_LPAREN_RPAREN_LBRACE_RBRACE =  90;  // <Normal Stm> ::= switch '(' <Expr> ')' '{' <Case Stms> '}'
	       final int PROD_NORMALSTM                                    =  91;  // <Normal Stm> ::= <Block>
	       final int PROD_NORMALSTM_SEMI                               =  92;  // <Normal Stm> ::= <Expr> ';'
	       final int PROD_NORMALSTM_GOTO_ID_SEMI                       =  93;  // <Normal Stm> ::= goto Id ';'
	       final int PROD_NORMALSTM_BREAK_SEMI                         =  94;  // <Normal Stm> ::= break ';'
	       final int PROD_NORMALSTM_CONTINUE_SEMI                      =  95;  // <Normal Stm> ::= continue ';'
	       final int PROD_NORMALSTM_RETURN_SEMI                        =  96;  // <Normal Stm> ::= return <Expr> ';'
	       final int PROD_NORMALSTM_SEMI2                              =  97;  // <Normal Stm> ::= ';'
	       final int PROD_ARG                                          =  98;  // <Arg> ::= <Expr>
	       final int PROD_ARG2                                         =  99;  // <Arg> ::= 
	       final int PROD_CASESTMS_CASE_COLON                          = 100;  // <Case Stms> ::= case <Value> ':' <Stm List> <Case Stms>
	       final int PROD_CASESTMS_DEFAULT_COLON                       = 101;  // <Case Stms> ::= default ':' <Stm List>
	       final int PROD_CASESTMS                                     = 102;  // <Case Stms> ::= 
	       final int PROD_BLOCK_LBRACE_RBRACE                          = 103;  // <Block> ::= '{' <Stm List> '}'
	       final int PROD_STMLIST                                      = 104;  // <Stm List> ::= <Stm> <Stm List>
	       final int PROD_STMLIST2                                     = 105;  // <Stm List> ::= 
	       final int PROD_INITIALIZER                                  = 106;  // <Initializer> ::= <Op If>
	       final int PROD_INITIALIZER_LBRACE_RBRACE                    = 107;  // <Initializer> ::= '{' <ExprIni> '}'
	       final int PROD_EXPR_COMMA                                   = 108;  // <Expr> ::= <Expr> ',' <Op Assign>
	       final int PROD_EXPR                                         = 109;  // <Expr> ::= <Op Assign>
	       final int PROD_EXPRINI_COMMA                                = 110;  // <ExprIni> ::= <ExprIni> ',' <Initializer>
	       final int PROD_EXPRINI                                      = 111;  // <ExprIni> ::= <Initializer>
	       final int PROD_OPASSIGN_EQ                                  = 112;  // <Op Assign> ::= <Op If> '=' <Op Assign>
	       final int PROD_OPASSIGN_PLUSEQ                              = 113;  // <Op Assign> ::= <Op If> '+=' <Op Assign>
	       final int PROD_OPASSIGN_MINUSEQ                             = 114;  // <Op Assign> ::= <Op If> '-=' <Op Assign>
	       final int PROD_OPASSIGN_TIMESEQ                             = 115;  // <Op Assign> ::= <Op If> '*=' <Op Assign>
	       final int PROD_OPASSIGN_DIVEQ                               = 116;  // <Op Assign> ::= <Op If> '/=' <Op Assign>
	       final int PROD_OPASSIGN_CARETEQ                             = 117;  // <Op Assign> ::= <Op If> '^=' <Op Assign>
	       final int PROD_OPASSIGN_AMPEQ                               = 118;  // <Op Assign> ::= <Op If> '&=' <Op Assign>
	       final int PROD_OPASSIGN_PIPEEQ                              = 119;  // <Op Assign> ::= <Op If> '|=' <Op Assign>
	       final int PROD_OPASSIGN_GTGTEQ                              = 120;  // <Op Assign> ::= <Op If> '>>=' <Op Assign>
	       final int PROD_OPASSIGN_LTLTEQ                              = 121;  // <Op Assign> ::= <Op If> '<<=' <Op Assign>
	       final int PROD_OPASSIGN                                     = 122;  // <Op Assign> ::= <Op If>
	       final int PROD_OPIF_QUESTION_COLON                          = 123;  // <Op If> ::= <Op Or> '?' <Op If> ':' <Op If>
	       final int PROD_OPIF                                         = 124;  // <Op If> ::= <Op Or>
	       final int PROD_OPOR_PIPEPIPE                                = 125;  // <Op Or> ::= <Op Or> '||' <Op And>
	       final int PROD_OPOR                                         = 126;  // <Op Or> ::= <Op And>
	       final int PROD_OPAND_AMPAMP                                 = 127;  // <Op And> ::= <Op And> '&&' <Op BinOR>
	       final int PROD_OPAND                                        = 128;  // <Op And> ::= <Op BinOR>
	       final int PROD_OPBINOR_PIPE                                 = 129;  // <Op BinOR> ::= <Op BinOR> '|' <Op BinXOR>
	       final int PROD_OPBINOR                                      = 130;  // <Op BinOR> ::= <Op BinXOR>
	       final int PROD_OPBINXOR_CARET                               = 131;  // <Op BinXOR> ::= <Op BinXOR> '^' <Op BinAND>
	       final int PROD_OPBINXOR                                     = 132;  // <Op BinXOR> ::= <Op BinAND>
	       final int PROD_OPBINAND_AMP                                 = 133;  // <Op BinAND> ::= <Op BinAND> '&' <Op Equate>
	       final int PROD_OPBINAND                                     = 134;  // <Op BinAND> ::= <Op Equate>
	       final int PROD_OPEQUATE_EQEQ                                = 135;  // <Op Equate> ::= <Op Equate> '==' <Op Compare>
	       final int PROD_OPEQUATE_EXCLAMEQ                            = 136;  // <Op Equate> ::= <Op Equate> '!=' <Op Compare>
	       final int PROD_OPEQUATE                                     = 137;  // <Op Equate> ::= <Op Compare>
	       final int PROD_OPCOMPARE_LT                                 = 138;  // <Op Compare> ::= <Op Compare> '<' <Op Shift>
	       final int PROD_OPCOMPARE_GT                                 = 139;  // <Op Compare> ::= <Op Compare> '>' <Op Shift>
	       final int PROD_OPCOMPARE_LTEQ                               = 140;  // <Op Compare> ::= <Op Compare> '<=' <Op Shift>
	       final int PROD_OPCOMPARE_GTEQ                               = 141;  // <Op Compare> ::= <Op Compare> '>=' <Op Shift>
	       final int PROD_OPCOMPARE                                    = 142;  // <Op Compare> ::= <Op Shift>
	       final int PROD_OPSHIFT_LTLT                                 = 143;  // <Op Shift> ::= <Op Shift> '<<' <Op Add>
	       final int PROD_OPSHIFT_GTGT                                 = 144;  // <Op Shift> ::= <Op Shift> '>>' <Op Add>
	       final int PROD_OPSHIFT                                      = 145;  // <Op Shift> ::= <Op Add>
	       final int PROD_OPADD_PLUS                                   = 146;  // <Op Add> ::= <Op Add> '+' <Op Mult>
	       final int PROD_OPADD_MINUS                                  = 147;  // <Op Add> ::= <Op Add> '-' <Op Mult>
	       final int PROD_OPADD                                        = 148;  // <Op Add> ::= <Op Mult>
	       final int PROD_OPMULT_TIMES                                 = 149;  // <Op Mult> ::= <Op Mult> '*' <Op Unary>
	       final int PROD_OPMULT_DIV                                   = 150;  // <Op Mult> ::= <Op Mult> '/' <Op Unary>
	       final int PROD_OPMULT_PERCENT                               = 151;  // <Op Mult> ::= <Op Mult> '%' <Op Unary>
	       final int PROD_OPMULT                                       = 152;  // <Op Mult> ::= <Op Unary>
	       final int PROD_OPUNARY_EXCLAM                               = 153;  // <Op Unary> ::= '!' <Op Unary>
	       final int PROD_OPUNARY_TILDE                                = 154;  // <Op Unary> ::= '~' <Op Unary>
	       final int PROD_OPUNARY_MINUS                                = 155;  // <Op Unary> ::= '-' <Op Unary>
	       final int PROD_OPUNARY_TIMES                                = 156;  // <Op Unary> ::= '*' <Op Unary>
	       final int PROD_OPUNARY_AMP                                  = 157;  // <Op Unary> ::= '&' <Op Unary>
	       final int PROD_OPUNARY_PLUSPLUS                             = 158;  // <Op Unary> ::= '++' <Op Unary>
	       final int PROD_OPUNARY_MINUSMINUS                           = 159;  // <Op Unary> ::= '--' <Op Unary>
	       final int PROD_OPUNARY_PLUSPLUS2                            = 160;  // <Op Unary> ::= <Op Pointer> '++'
	       final int PROD_OPUNARY_MINUSMINUS2                          = 161;  // <Op Unary> ::= <Op Pointer> '--'
	       final int PROD_OPUNARY_LPAREN_RPAREN                        = 162;  // <Op Unary> ::= '(' <Type> ')' <Op Unary>
	       final int PROD_OPUNARY_SIZEOF_LPAREN_RPAREN                 = 163;  // <Op Unary> ::= sizeof '(' <Type> ')'
	       final int PROD_OPUNARY_SIZEOF_LPAREN_ID_RPAREN              = 164;  // <Op Unary> ::= sizeof '(' Id <Pointers> ')'
	       final int PROD_OPUNARY                                      = 165;  // <Op Unary> ::= <Op Pointer>
	       final int PROD_OPPOINTER_DOT                                = 166;  // <Op Pointer> ::= <Op Pointer> '.' <Value>
	       final int PROD_OPPOINTER_MINUSGT                            = 167;  // <Op Pointer> ::= <Op Pointer> '->' <Value>
	       final int PROD_OPPOINTER_LBRACKET_RBRACKET                  = 168;  // <Op Pointer> ::= <Op Pointer> '[' <Expr> ']'
	       final int PROD_OPPOINTER                                    = 169;  // <Op Pointer> ::= <Value>
	       final int PROD_VALUE_OCTLITERAL                             = 170;  // <Value> ::= OctLiteral
	       final int PROD_VALUE_HEXLITERAL                             = 171;  // <Value> ::= HexLiteral
	       final int PROD_VALUE_DECLITERAL                             = 172;  // <Value> ::= DecLiteral
	       final int PROD_VALUE_STRINGLITERAL                          = 173;  // <Value> ::= StringLiteral
	       final int PROD_VALUE_CHARLITERAL                            = 174;  // <Value> ::= CharLiteral
	       final int PROD_VALUE_FLOATLITERAL                           = 175;  // <Value> ::= FloatLiteral
	       final int PROD_VALUE_ID_LPAREN_RPAREN                       = 176;  // <Value> ::= Id '(' <Expr> ')'
	       final int PROD_VALUE_ID_LPAREN_RPAREN2                      = 177;  // <Value> ::= Id '(' ')'
	       final int PROD_VALUE_ID                                     = 178;  // <Value> ::= Id
	       final int PROD_VALUE_LPAREN_RPAREN                          = 179;  // <Value> ::= '(' <Expr> ')'
	};

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
				if (strLine.matches("^#define\\s+([\\w].*)\\s+(.+)")) {
					String symbol = strLine.replaceAll("^#define\\s+([\\w].*)\\s+(.+)", "$1");
					String subst = strLine.replaceAll("^#define\\s+([\\w].*)\\s+(.+)", "$2").trim();
					defines.put(symbol, subst);
				}
				else if (!strLine.startsWith("#")) {
					// This grammar doesn't cope with array initializers, so replace them by a pseudo function call
//					if (strLine.matches(".*[^!=<>][=]\\s*?[{].*[}].*")) {
//						StringList tokens = Element.splitLexically(strLine, true);
//						int posAsgn = tokens.indexOf("=");
//						int posLBrace = tokens.indexOf("{", posAsgn+1);
//						StringList items = Element.splitExpressionList(tokens.subSequence(posLBrace+1, tokens.count()).concatenate(), ",", true);
//						if (items.count() > 0 && items.get(items.count()-1).startsWith("}")) {
//							strLine = tokens.subSequence(0, posLBrace-1).concatenate() +
//									arrayIniFunc + items.subSequence(0, items.count()-1).concatenate(", ") +
//									")" + items.get(items.count()-1).substring(1);
//						}
//					}
					srcCode += strLine + "\n";
				}
			}
			//Close the input stream
			in.close();

			for (Entry entry: defines.entrySet()) {
				if (debugprint) {
					System.out.println("CParser.prepareTextfile(): " + Matcher.quoteReplacement((String)entry.getValue()));
				}
				srcCode = srcCode.replaceAll("(.*?\\W)" + entry.getKey() + "(\\W.*?)", "$1"+ Matcher.quoteReplacement((String)entry.getValue()) + "$2");
			}

//			Regex r = new Regex("(.*\\()\\s*?void\\s*?(\\).*)", "$1$2");
//			srcCode = r.replaceAll(srcCode);
			
			//System.out.println(srcCode);

			// trim and save as new file
			interm = new File(_textToParse + ".structorizer");
			OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(interm), "UTF-8");
			ow.write(srcCode.trim()+"\n");
			//System.out.println("==> "+filterNonAscii(pasCode.trim()+"\n"));
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
					ruleId == RuleConstants.PROD_VALUE_ID_LPAREN_RPAREN
					||
					ruleId == RuleConstants.PROD_VALUE_ID_LPAREN_RPAREN2
					)
			{
				boolean done = false;
				String content = "";
				String procName = _reduction.get(0).asString();
				StringList arguments = null;
				if (ruleId == RuleConstants.PROD_VALUE_ID_LPAREN_RPAREN) {
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
					// Assignment or procedure call?
					ruleId == RuleConstants.PROD_OPASSIGN_EQ
					||
					ruleId == RuleConstants.PROD_VALUE_ID_LPAREN_RPAREN
					||
					ruleId == RuleConstants.PROD_VALUE_ID_LPAREN_RPAREN2
					)
			{
				// Simply convert it as text and create an instruction. In case of a call
				// we'll try to transmute it after all subroutines will have been parsed.
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
				int typeIx = 0;	// token index of the type description
				if (ruleId == RuleConstants.PROD_VARDECL_SEMI) {
					typeIx++;
				}
				else if (ruleId == RuleConstants.PROD_VARDECL_SEMI3) {
					typeIx--;
				}
				String type = "int";	// default type if there is only a modifier
				if (typeIx >= 0) {
					type = getContent_R(_reduction.get(typeIx).asReduction(), "");
				}
				else {
					// In case of PROD_VARDECL_SEMI3 we must get behind the modifier
					typeIx = 0;
				}
				// Now concern on the first declaration of the list
				Reduction secReduc = _reduction.get(typeIx + 1).asReduction();
				buildDeclOrAssignment(secReduc, type, _parentNode);
				if (_reduction.size() > typeIx+2) {
					if (debugprint) {
						System.out.println("\tanalyzing <Var List> ...");
					}
					secReduc = _reduction.get(typeIx + 2).asReduction();	// <Var List>
					ruleId = secReduc.getParent().getTableIndex();
					while (ruleId == RuleConstants.PROD_VARLIST_COMMA) {
						Reduction thdReduc = secReduc.get(1).asReduction();	// <Var Item>
						// Get the pointers part
						String pointers = getContent_R(thdReduc.get(0).asReduction(), "");
						// Delegate the sub-reduction <Var>
						buildDeclOrAssignment(secReduc.get(1).asReduction(), type+pointers, _parentNode);
						// Get the list tail
						secReduc = secReduc.get(2).asReduction();	// <Var List>
						ruleId = secReduc.getParent().getTableIndex();
					}
				}
			}
			else if (
					// Type definitions
						ruleId == RuleConstants.PROD_STRUCTDECL_STRUCT_ID_LBRACE_RBRACE_SEMI
						||
						ruleId == RuleConstants.PROD_UNIONDECL_UNION_ID_LBRACE_RBRACE_SEMI
						||
						ruleId == RuleConstants.PROD_TYPEDEFDECL_TYPEDEF_ID_SEMI
					)
			{
				// Union and struct definitions may only be global in this grammar. If we don't handle
				// them then their components would be made variable declarations.
				// We might create disabled declaration instructions for them instead, but where to place
				// them? Create a dummy diagram for type definitions? Well, maybe Or better collect them
				// for the main program? The latter makes some sense. So we will park them in a "globalRoot"
				String rootName = root.getMethodName();
				if (this.globalRoot == null) {
					if (rootName.equals("main") || rootName.equals("???")) {
						this.globalRoot = root;
					}
					else {
						this.globalRoot = new Root();
						this.globalRoot.isProgram = false;
					}
				}
				String content = this.getContent_R(_reduction, "");
				Instruction decl = new Instruction(content);
				decl.disabled = true;
				this.globalRoot.children.addElement(decl);
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
				int nameIx = 1;	// Token index of the function id
				int funcId = secReduc.getParent().getTableIndex();
				if (funcId == RuleConstants.PROD_FUNCID_ID2) {
					nameIx = 0;
				}
				String funcName = secReduc.get(nameIx).getData().toString();
				Root prevRoot = root;	// Cache the original root
				root = new Root();	// Prepare a new root for the (sub)routine
				root.isProgram = false;
				subRoots.add(root);
				String content = new String();
				// Is there a type specification different from void?
				if (funcId == RuleConstants.PROD_FUNCID_ID) {
					content = getContent_R(secReduc.get(0).asReduction(), content).trim() + " ";					
				}
				content += funcName + "(";
				int bodyIndex = 4;
				String params = "";
				switch (ruleId) {
				case RuleConstants.PROD_FUNCDECL_LPAREN_RPAREN2:
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
				While ele = new While(getKeyword("preWhile")+translateContent(content)+getKeyword("postWhile"));
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
				Repeat ele = new Repeat(getKeyword("preRepeat") + " not (" + content + ")" + getKeyword("postRepeat"));
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
				While ele = new While(getKeyword("preWhile")+translateContent(content)+getKeyword("postWhile"));
				// Mark all offsprings of the FOR loop with a (by default) yellowish colour
				ele.setColor(Element.color2);
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
				ele.q.getElement(ele.q.getSize()-1).setColor(Element.color2);

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
				
				Alternative ele = new Alternative(getKeyword("preAlt") + translateContent(content) + getKeyword("postAlt"));
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
		int ruleId = _reduc.getParent().getTableIndex();
		String content = getContent_R(_reduc, "");	// 
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
					instr.setColor(Color.CYAN);
				}
				else {
					instr.setColor(Element.color3);	// local declarations with a smooth green
				}
				_parentNode.addElement(instr);
			}
		}
		else if (ruleId == RuleConstants.PROD_VARITEM) {
			if (debugprint) {
				System.out.println("\ttrying PROD_VARITEM ...");
			}
			String ptype = this.getContent_R(_reduc.get(0).asReduction(), _type);
			buildDeclOrAssignment(_reduc.get(1).asReduction(), ptype, _parentNode);
		}
		else {
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
				instr = new Instruction(_type + " " + content);
			}
			else {
				instr = new Instruction(varName + " <- " + translateContent(expr));
			}
			if (_parentNode.parent instanceof Root && ((Root)_parentNode.parent).getMethodName().equals("???")) {
				instr.setComment("globally declared!");
				instr.setColor(Color.CYAN);
			}
			_parentNode.addElement(instr);
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
		String content = new String();
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
		Reduction secRule = null;
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
				// Drop an endstanding newline since Structorizer produces a newline automatically
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
				for (int i = 1; i < _args.count(); i++) {
					String varItem = _args.get(i);
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
				int ruleId = _reduction.getParent().getTableIndex();
				_content = getContent_R(token.asReduction(), _content);	
				break;
			case CONTENT:
			{
				String toAdd = "";
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
					_content += " " + token.asString() + " ";
					break;
				default:
					toAdd = token.asString();
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
			if (aRoot.getParameterNames().count() > 0) {
				String header = aRoot.getText().getText();
				header = header.replaceFirst("(.*?)main([((].*)", "$1" + fileName + "$2");
				aRoot.setText(header);
			}
			else {
				aRoot.setText(fileName);
			}
			aRoot.isProgram = true;
			// Are there some global definitions to be taken over?
			if (this.root.getText().getText().equals("???")) {
				for (int i = this.root.children.getSize(); i > 0; i--) {
					aRoot.children.insertElementAt(this.root.children.getElement(i-1), 0);
				}
				this.root.children.clear();
			}
			// Are there some more global definitions to be taken over?
			if (this.globalRoot != null && this.globalRoot != aRoot) {
				for (int i = this.globalRoot.children.getSize(); i > 0; i--) {
					aRoot.children.insertElementAt(this.globalRoot.children.getElement(i-1), 0);
				}
				this.globalRoot.children.clear();
				this.globalRoot = null;
			}
		}
	}
	
}

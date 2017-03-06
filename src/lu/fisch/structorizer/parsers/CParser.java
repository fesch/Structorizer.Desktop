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
 *     Comment:		While setting up this class, I had a deep look at the following package:
 *
 *     Licensed Material - Property of Matthew Hawkins (hawkini@4email.net)
 *     GOLDParser - code ported from VB - Author Devin Cook. All rights reserved.
 *     Modifications to this code are allowed as it is a helper class to use the engine.
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

import com.stevesoft.pat.Regex;

import goldengine.java.*;
import lu.fisch.structorizer.elements.Alternative;
import lu.fisch.structorizer.elements.Case;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.elements.Jump;
import lu.fisch.structorizer.elements.Repeat;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.structorizer.elements.While;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.utils.BString;
import lu.fisch.utils.StringList;

public class CParser extends CodeParser implements GPMessageConstants
{

	private final String compiledGrammar = "C-ANSI.cgt";
	
	/**
	 *  This is the substitutor for the opening brace of an array (or struct) initializer.
	 * (The C-ANSI gold grammar doesn't accept initializer expressions.)
	 */
	private static final String arrayIniFunc = "arrayInitializer(";
	
	/**
	 * If this flag is set then program names derived from file name will be made uppercase
	 * This default will be initialized in consistentlywith the Analyser check 
	 */
	private boolean optionUpperCaseProgName = false;
	
	@Override
	public String getDialogTitle() {
		return "ANSI C";
	}

	@Override
	protected String getFileDescription() {
		return "ANSI C Source Code";
	}

	@Override
	protected String[] getFileExtensions() {
		final String[] exts = { "c" };
		return exts;
	}

	private interface SymbolConstants 
	{
		final int SYM_EOF           =   0;  // (EOF)
		final int SYM_ERROR         =   1;  // (Error)
		final int SYM_WHITESPACE    =   2;  // Whitespace
		final int SYM_COMMENTEND    =   3;  // 'Comment End'
		final int SYM_COMMENTLINE   =   4;  // 'Comment Line'
		final int SYM_COMMENTSTART  =   5;  // 'Comment Start'
		final int SYM_MINUS         =   6;  // '-'
		final int SYM_MINUSMINUS    =   7;  // '--'
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
		final int SYM_COMMA         =  18;  // ','
		final int SYM_DOT           =  19;  // '.'
		final int SYM_DIV           =  20;  // '/'
		final int SYM_DIVEQ         =  21;  // '/='
		final int SYM_COLON         =  22;  // ':'
		final int SYM_SEMI          =  23;  // ';'
		final int SYM_QUESTION      =  24;  // '?'
		final int SYM_LBRACKET      =  25;  // '['
		final int SYM_RBRACKET      =  26;  // ']'
		final int SYM_CARET         =  27;  // '^'
		final int SYM_CARETEQ       =  28;  // '^='
		final int SYM_LBRACE        =  29;  // '{'
		final int SYM_PIPE          =  30;  // '|'
		final int SYM_PIPEPIPE      =  31;  // '||'
		final int SYM_PIPEEQ        =  32;  // '|='
		final int SYM_RBRACE        =  33;  // '}'
		final int SYM_TILDE         =  34;  // '~'
		final int SYM_PLUS          =  35;  // '+'
		final int SYM_PLUSPLUS      =  36;  // '++'
		final int SYM_PLUSEQ        =  37;  // '+='
		final int SYM_LT            =  38;  // '<'
		final int SYM_LTLT          =  39;  // '<<'
		final int SYM_LTLTEQ        =  40;  // '<<='
		final int SYM_LTEQ          =  41;  // '<='
		final int SYM_EQ            =  42;  // '='
		final int SYM_MINUSEQ       =  43;  // '-='
		final int SYM_EQEQ          =  44;  // '=='
		final int SYM_GT            =  45;  // '>'
		final int SYM_MINUSGT       =  46;  // '->'
		final int SYM_GTEQ          =  47;  // '>='
		final int SYM_GTGT          =  48;  // '>>'
		final int SYM_GTGTEQ        =  49;  // '>>='
		final int SYM_AUTO          =  50;  // auto
		final int SYM_BREAK         =  51;  // break
		final int SYM_CASE          =  52;  // case
		final int SYM_CHAR          =  53;  // char
		final int SYM_CHARLITERAL   =  54;  // CharLiteral
		final int SYM_CONST         =  55;  // const
		final int SYM_CONTINUE      =  56;  // continue
		final int SYM_DECLITERAL    =  57;  // DecLiteral
		final int SYM_DEFAULT       =  58;  // default
		final int SYM_DO            =  59;  // do
		final int SYM_DOUBLE        =  60;  // double
		final int SYM_ELSE          =  61;  // else
		final int SYM_ENUM          =  62;  // enum
		final int SYM_EXTERN        =  63;  // extern
		final int SYM_FLOAT         =  64;  // float
		final int SYM_FLOATLITERAL  =  65;  // FloatLiteral
		final int SYM_FOR           =  66;  // for
		final int SYM_GOTO          =  67;  // goto
		final int SYM_HEXLITERAL    =  68;  // HexLiteral
		final int SYM_ID            =  69;  // Id
		final int SYM_IF            =  70;  // if
		final int SYM_INT           =  71;  // int
		final int SYM_LONG          =  72;  // long
		final int SYM_OCTLITERAL    =  73;  // OctLiteral
		final int SYM_REGISTER      =  74;  // register
		final int SYM_RETURN        =  75;  // return
		final int SYM_SHORT         =  76;  // short
		final int SYM_SIGNED        =  77;  // signed
		final int SYM_SIZEOF        =  78;  // sizeof
		final int SYM_STATIC        =  79;  // static
		final int SYM_STRINGLITERAL =  80;  // StringLiteral
		final int SYM_STRUCT        =  81;  // struct
		final int SYM_SWITCH        =  82;  // switch
		final int SYM_TYPEDEF       =  83;  // typedef
		final int SYM_UNION         =  84;  // union
		final int SYM_UNSIGNED      =  85;  // unsigned
		final int SYM_VOID          =  86;  // void
		final int SYM_VOLATILE      =  87;  // volatile
		final int SYM_WHILE         =  88;  // while
		final int SYM_ARG           =  89;  // <Arg>
		final int SYM_ARRAY         =  90;  // <Array>
		final int SYM_BASE          =  91;  // <Base>
		final int SYM_BLOCK         =  92;  // <Block>
		final int SYM_CASESTMS      =  93;  // <Case Stms>
		final int SYM_DECL          =  94;  // <Decl>
		final int SYM_DECLS         =  95;  // <Decls>
		final int SYM_ENUMDECL      =  96;  // <Enum Decl>
		final int SYM_ENUMDEF       =  97;  // <Enum Def>
		final int SYM_ENUMVAL       =  98;  // <Enum Val>
		final int SYM_EXPR          =  99;  // <Expr>
		final int SYM_FUNCDECL      = 100;  // <Func Decl>
		final int SYM_FUNCID        = 101;  // <Func ID>
		final int SYM_FUNCPROTO     = 102;  // <Func Proto>
		final int SYM_IDLIST        = 103;  // <Id List>
		final int SYM_MOD           = 104;  // <Mod>
		final int SYM_NORMALSTM     = 105;  // <Normal Stm>
		final int SYM_OPADD         = 106;  // <Op Add>
		final int SYM_OPAND         = 107;  // <Op And>
		final int SYM_OPASSIGN      = 108;  // <Op Assign>
		final int SYM_OPBINAND      = 109;  // <Op BinAND>
		final int SYM_OPBINOR       = 110;  // <Op BinOR>
		final int SYM_OPBINXOR      = 111;  // <Op BinXOR>
		final int SYM_OPCOMPARE     = 112;  // <Op Compare>
		final int SYM_OPEQUATE      = 113;  // <Op Equate>
		final int SYM_OPIF          = 114;  // <Op If>
		final int SYM_OPMULT        = 115;  // <Op Mult>
		final int SYM_OPOR          = 116;  // <Op Or>
		final int SYM_OPPOINTER     = 117;  // <Op Pointer>
		final int SYM_OPSHIFT       = 118;  // <Op Shift>
		final int SYM_OPUNARY       = 119;  // <Op Unary>
		final int SYM_PARAM         = 120;  // <Param>
		final int SYM_PARAMS        = 121;  // <Params>
		final int SYM_POINTERS      = 122;  // <Pointers>
		final int SYM_SCALAR        = 123;  // <Scalar>
		final int SYM_SIGN          = 124;  // <Sign>
		final int SYM_STM           = 125;  // <Stm>
		final int SYM_STMLIST       = 126;  // <Stm List>
		final int SYM_STRUCTDECL    = 127;  // <Struct Decl>
		final int SYM_STRUCTDEF     = 128;  // <Struct Def>
		final int SYM_THENSTM       = 129;  // <Then Stm>
		final int SYM_TYPE          = 130;  // <Type>
		final int SYM_TYPEDEFDECL   = 131;  // <Typedef Decl>
		final int SYM_TYPES         = 132;  // <Types>
		final int SYM_UNIONDECL     = 133;  // <Union Decl>
		final int SYM_VALUE         = 134;  // <Value>
		final int SYM_VAR           = 135;  // <Var>
		final int SYM_VARDECL       = 136;  // <Var Decl>
		final int SYM_VARITEM       = 137;  // <Var Item>
		final int SYM_VARLIST       = 138;  // <Var List>
	};

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
		final int PROD_FUNCPROTO_LPAREN_RPAREN_SEMI3                =  11;  // <Func Proto> ::= <Func ID> '(' ')' ';'
		final int PROD_FUNCDECL_LPAREN_RPAREN                       =  12;  // <Func Decl> ::= <Func ID> '(' <Params> ')' <Block>
		final int PROD_FUNCDECL_LPAREN_RPAREN2                      =  13;  // <Func Decl> ::= <Func ID> '(' <Id List> ')' <Struct Def> <Block>
		final int PROD_FUNCDECL_LPAREN_RPAREN3                      =  14;  // <Func Decl> ::= <Func ID> '(' ')' <Block>
		final int PROD_PARAMS_COMMA                                 =  15;  // <Params> ::= <Param> ',' <Params>
		final int PROD_PARAMS                                       =  16;  // <Params> ::= <Param>
		final int PROD_PARAM_CONST_ID                               =  17;  // <Param> ::= const <Type> Id
		final int PROD_PARAM_ID                                     =  18;  // <Param> ::= <Type> Id
		final int PROD_TYPES_COMMA                                  =  19;  // <Types> ::= <Type> ',' <Types>
		final int PROD_TYPES                                        =  20;  // <Types> ::= <Type>
		final int PROD_IDLIST_ID_COMMA                              =  21;  // <Id List> ::= Id ',' <Id List>
		final int PROD_IDLIST_ID                                    =  22;  // <Id List> ::= Id
		final int PROD_FUNCID_ID                                    =  23;  // <Func ID> ::= <Type> Id
		final int PROD_FUNCID_ID2                                   =  24;  // <Func ID> ::= Id
		final int PROD_TYPEDEFDECL_TYPEDEF_ID_SEMI                  =  25;  // <Typedef Decl> ::= typedef <Type> Id ';'
		final int PROD_STRUCTDECL_STRUCT_ID_LBRACE_RBRACE_SEMI      =  26;  // <Struct Decl> ::= struct Id '{' <Struct Def> '}' ';'
		final int PROD_UNIONDECL_UNION_ID_LBRACE_RBRACE_SEMI        =  27;  // <Union Decl> ::= union Id '{' <Struct Def> '}' ';'
		final int PROD_STRUCTDEF                                    =  28;  // <Struct Def> ::= <Var Decl> <Struct Def>
		final int PROD_STRUCTDEF2                                   =  29;  // <Struct Def> ::= <Var Decl>
		final int PROD_VARDECL_SEMI                                 =  30;  // <Var Decl> ::= <Mod> <Type> <Var> <Var List> ';'
		final int PROD_VARDECL_SEMI2                                =  31;  // <Var Decl> ::= <Type> <Var> <Var List> ';'
		final int PROD_VARDECL_SEMI3                                =  32;  // <Var Decl> ::= <Mod> <Var> <Var List> ';'
		final int PROD_VAR_ID                                       =  33;  // <Var> ::= Id <Array>
		final int PROD_VAR_ID_EQ                                    =  34;  // <Var> ::= Id <Array> '=' <Op If>
		final int PROD_ARRAY_LBRACKET_RBRACKET                      =  35;  // <Array> ::= '[' <Expr> ']'
		final int PROD_ARRAY_LBRACKET_RBRACKET2                     =  36;  // <Array> ::= '[' ']'
		final int PROD_ARRAY                                        =  37;  // <Array> ::= 
		final int PROD_VARLIST_COMMA                                =  38;  // <Var List> ::= ',' <Var Item> <Var List>
		final int PROD_VARLIST                                      =  39;  // <Var List> ::= 
		final int PROD_VARITEM                                      =  40;  // <Var Item> ::= <Pointers> <Var>
		final int PROD_MOD_EXTERN                                   =  41;  // <Mod> ::= extern
		final int PROD_MOD_STATIC                                   =  42;  // <Mod> ::= static
		final int PROD_MOD_REGISTER                                 =  43;  // <Mod> ::= register
		final int PROD_MOD_AUTO                                     =  44;  // <Mod> ::= auto
		final int PROD_MOD_VOLATILE                                 =  45;  // <Mod> ::= volatile
		final int PROD_MOD_CONST                                    =  46;  // <Mod> ::= const
		final int PROD_ENUMDECL_ENUM_ID_LBRACE_RBRACE_SEMI          =  47;  // <Enum Decl> ::= enum Id '{' <Enum Def> '}' ';'
		final int PROD_ENUMDEF_COMMA                                =  48;  // <Enum Def> ::= <Enum Val> ',' <Enum Def>
		final int PROD_ENUMDEF                                      =  49;  // <Enum Def> ::= <Enum Val>
		final int PROD_ENUMVAL_ID                                   =  50;  // <Enum Val> ::= Id
		final int PROD_ENUMVAL_ID_EQ_OCTLITERAL                     =  51;  // <Enum Val> ::= Id '=' OctLiteral
		final int PROD_ENUMVAL_ID_EQ_HEXLITERAL                     =  52;  // <Enum Val> ::= Id '=' HexLiteral
		final int PROD_ENUMVAL_ID_EQ_DECLITERAL                     =  53;  // <Enum Val> ::= Id '=' DecLiteral
		final int PROD_TYPE                                         =  54;  // <Type> ::= <Base> <Pointers>
		final int PROD_BASE                                         =  55;  // <Base> ::= <Sign> <Scalar>
		final int PROD_BASE_STRUCT_ID                               =  56;  // <Base> ::= struct Id
		final int PROD_BASE_STRUCT_LBRACE_RBRACE                    =  57;  // <Base> ::= struct '{' <Struct Def> '}'
		final int PROD_BASE_UNION_ID                                =  58;  // <Base> ::= union Id
		final int PROD_BASE_UNION_LBRACE_RBRACE                     =  59;  // <Base> ::= union '{' <Struct Def> '}'
		final int PROD_BASE_ENUM_ID                                 =  60;  // <Base> ::= enum Id
		final int PROD_SIGN_SIGNED                                  =  61;  // <Sign> ::= signed
		final int PROD_SIGN_UNSIGNED                                =  62;  // <Sign> ::= unsigned
		final int PROD_SIGN                                         =  63;  // <Sign> ::= 
		final int PROD_SCALAR_CHAR                                  =  64;  // <Scalar> ::= char
		final int PROD_SCALAR_INT                                   =  65;  // <Scalar> ::= int
		final int PROD_SCALAR_SHORT                                 =  66;  // <Scalar> ::= short
		final int PROD_SCALAR_LONG                                  =  67;  // <Scalar> ::= long
		final int PROD_SCALAR_SHORT_INT                             =  68;  // <Scalar> ::= short int
		final int PROD_SCALAR_LONG_INT                              =  69;  // <Scalar> ::= long int
		final int PROD_SCALAR_FLOAT                                 =  70;  // <Scalar> ::= float
		final int PROD_SCALAR_DOUBLE                                =  71;  // <Scalar> ::= double
		final int PROD_SCALAR_VOID                                  =  72;  // <Scalar> ::= void
		final int PROD_POINTERS_TIMES                               =  73;  // <Pointers> ::= '*' <Pointers>
		final int PROD_POINTERS                                     =  74;  // <Pointers> ::= 
		final int PROD_STM                                          =  75;  // <Stm> ::= <Var Decl>
		final int PROD_STM_ID_COLON                                 =  76;  // <Stm> ::= Id ':'
		final int PROD_STM_IF_LPAREN_RPAREN                         =  77;  // <Stm> ::= if '(' <Expr> ')' <Stm>
		final int PROD_STM_IF_LPAREN_RPAREN_ELSE                    =  78;  // <Stm> ::= if '(' <Expr> ')' <Then Stm> else <Stm>
		final int PROD_STM_WHILE_LPAREN_RPAREN                      =  79;  // <Stm> ::= while '(' <Expr> ')' <Stm>
		final int PROD_STM_FOR_LPAREN_SEMI_SEMI_RPAREN              =  80;  // <Stm> ::= for '(' <Arg> ';' <Arg> ';' <Arg> ')' <Stm>
		final int PROD_STM2                                         =  81;  // <Stm> ::= <Normal Stm>
		final int PROD_THENSTM_IF_LPAREN_RPAREN_ELSE                =  82;  // <Then Stm> ::= if '(' <Expr> ')' <Then Stm> else <Then Stm>
		final int PROD_THENSTM_WHILE_LPAREN_RPAREN                  =  83;  // <Then Stm> ::= while '(' <Expr> ')' <Then Stm>
		final int PROD_THENSTM_FOR_LPAREN_SEMI_SEMI_RPAREN          =  84;  // <Then Stm> ::= for '(' <Arg> ';' <Arg> ';' <Arg> ')' <Then Stm>
		final int PROD_THENSTM                                      =  85;  // <Then Stm> ::= <Normal Stm>
		final int PROD_NORMALSTM_DO_WHILE_LPAREN_RPAREN             =  86;  // <Normal Stm> ::= do <Stm> while '(' <Expr> ')'
		final int PROD_NORMALSTM_SWITCH_LPAREN_RPAREN_LBRACE_RBRACE =  87;  // <Normal Stm> ::= switch '(' <Expr> ')' '{' <Case Stms> '}'
		final int PROD_NORMALSTM                                    =  88;  // <Normal Stm> ::= <Block>
		final int PROD_NORMALSTM_SEMI                               =  89;  // <Normal Stm> ::= <Expr> ';'
		final int PROD_NORMALSTM_GOTO_ID_SEMI                       =  90;  // <Normal Stm> ::= goto Id ';'
		final int PROD_NORMALSTM_BREAK_SEMI                         =  91;  // <Normal Stm> ::= break ';'
		final int PROD_NORMALSTM_CONTINUE_SEMI                      =  92;  // <Normal Stm> ::= continue ';'
		final int PROD_NORMALSTM_RETURN_SEMI                        =  93;  // <Normal Stm> ::= return <Expr> ';'
		final int PROD_NORMALSTM_SEMI2                              =  94;  // <Normal Stm> ::= ';'
		final int PROD_ARG                                          =  95;  // <Arg> ::= <Expr>
		final int PROD_ARG2                                         =  96;  // <Arg> ::= 
		final int PROD_CASESTMS_CASE_COLON                          =  97;  // <Case Stms> ::= case <Value> ':' <Stm List> <Case Stms>
		final int PROD_CASESTMS_DEFAULT_COLON                       =  98;  // <Case Stms> ::= default ':' <Stm List>
		final int PROD_CASESTMS                                     =  99;  // <Case Stms> ::= 
		final int PROD_BLOCK_LBRACE_RBRACE                          = 100;  // <Block> ::= '{' <Stm List> '}'
		final int PROD_STMLIST                                      = 101;  // <Stm List> ::= <Stm> <Stm List>
		final int PROD_STMLIST2                                     = 102;  // <Stm List> ::= 
		final int PROD_EXPR_COMMA                                   = 103;  // <Expr> ::= <Expr> ',' <Op Assign>
		final int PROD_EXPR                                         = 104;  // <Expr> ::= <Op Assign>
		final int PROD_OPASSIGN_EQ                                  = 105;  // <Op Assign> ::= <Op If> '=' <Op Assign>
		final int PROD_OPASSIGN_PLUSEQ                              = 106;  // <Op Assign> ::= <Op If> '+=' <Op Assign>
		final int PROD_OPASSIGN_MINUSEQ                             = 107;  // <Op Assign> ::= <Op If> '-=' <Op Assign>
		final int PROD_OPASSIGN_TIMESEQ                             = 108;  // <Op Assign> ::= <Op If> '*=' <Op Assign>
		final int PROD_OPASSIGN_DIVEQ                               = 109;  // <Op Assign> ::= <Op If> '/=' <Op Assign>
		final int PROD_OPASSIGN_CARETEQ                             = 110;  // <Op Assign> ::= <Op If> '^=' <Op Assign>
		final int PROD_OPASSIGN_AMPEQ                               = 111;  // <Op Assign> ::= <Op If> '&=' <Op Assign>
		final int PROD_OPASSIGN_PIPEEQ                              = 112;  // <Op Assign> ::= <Op If> '|=' <Op Assign>
		final int PROD_OPASSIGN_GTGTEQ                              = 113;  // <Op Assign> ::= <Op If> '>>=' <Op Assign>
		final int PROD_OPASSIGN_LTLTEQ                              = 114;  // <Op Assign> ::= <Op If> '<<=' <Op Assign>
		final int PROD_OPASSIGN                                     = 115;  // <Op Assign> ::= <Op If>
		final int PROD_OPIF_QUESTION_COLON                          = 116;  // <Op If> ::= <Op Or> '?' <Op If> ':' <Op If>
		final int PROD_OPIF                                         = 117;  // <Op If> ::= <Op Or>
		final int PROD_OPOR_PIPEPIPE                                = 118;  // <Op Or> ::= <Op Or> '||' <Op And>
		final int PROD_OPOR                                         = 119;  // <Op Or> ::= <Op And>
		final int PROD_OPAND_AMPAMP                                 = 120;  // <Op And> ::= <Op And> '&&' <Op BinOR>
		final int PROD_OPAND                                        = 121;  // <Op And> ::= <Op BinOR>
		final int PROD_OPBINOR_PIPE                                 = 122;  // <Op BinOR> ::= <Op BinOR> '|' <Op BinXOR>
		final int PROD_OPBINOR                                      = 123;  // <Op BinOR> ::= <Op BinXOR>
		final int PROD_OPBINXOR_CARET                               = 124;  // <Op BinXOR> ::= <Op BinXOR> '^' <Op BinAND>
		final int PROD_OPBINXOR                                     = 125;  // <Op BinXOR> ::= <Op BinAND>
		final int PROD_OPBINAND_AMP                                 = 126;  // <Op BinAND> ::= <Op BinAND> '&' <Op Equate>
		final int PROD_OPBINAND                                     = 127;  // <Op BinAND> ::= <Op Equate>
		final int PROD_OPEQUATE_EQEQ                                = 128;  // <Op Equate> ::= <Op Equate> '==' <Op Compare>
		final int PROD_OPEQUATE_EXCLAMEQ                            = 129;  // <Op Equate> ::= <Op Equate> '!=' <Op Compare>
		final int PROD_OPEQUATE                                     = 130;  // <Op Equate> ::= <Op Compare>
		final int PROD_OPCOMPARE_LT                                 = 131;  // <Op Compare> ::= <Op Compare> '<' <Op Shift>
		final int PROD_OPCOMPARE_GT                                 = 132;  // <Op Compare> ::= <Op Compare> '>' <Op Shift>
		final int PROD_OPCOMPARE_LTEQ                               = 133;  // <Op Compare> ::= <Op Compare> '<=' <Op Shift>
		final int PROD_OPCOMPARE_GTEQ                               = 134;  // <Op Compare> ::= <Op Compare> '>=' <Op Shift>
		final int PROD_OPCOMPARE                                    = 135;  // <Op Compare> ::= <Op Shift>
		final int PROD_OPSHIFT_LTLT                                 = 136;  // <Op Shift> ::= <Op Shift> '<<' <Op Add>
		final int PROD_OPSHIFT_GTGT                                 = 137;  // <Op Shift> ::= <Op Shift> '>>' <Op Add>
		final int PROD_OPSHIFT                                      = 138;  // <Op Shift> ::= <Op Add>
		final int PROD_OPADD_PLUS                                   = 139;  // <Op Add> ::= <Op Add> '+' <Op Mult>
		final int PROD_OPADD_MINUS                                  = 140;  // <Op Add> ::= <Op Add> '-' <Op Mult>
		final int PROD_OPADD                                        = 141;  // <Op Add> ::= <Op Mult>
		final int PROD_OPMULT_TIMES                                 = 142;  // <Op Mult> ::= <Op Mult> '*' <Op Unary>
		final int PROD_OPMULT_DIV                                   = 143;  // <Op Mult> ::= <Op Mult> '/' <Op Unary>
		final int PROD_OPMULT_PERCENT                               = 144;  // <Op Mult> ::= <Op Mult> '%' <Op Unary>
		final int PROD_OPMULT                                       = 145;  // <Op Mult> ::= <Op Unary>
		final int PROD_OPUNARY_EXCLAM                               = 146;  // <Op Unary> ::= '!' <Op Unary>
		final int PROD_OPUNARY_TILDE                                = 147;  // <Op Unary> ::= '~' <Op Unary>
		final int PROD_OPUNARY_MINUS                                = 148;  // <Op Unary> ::= '-' <Op Unary>
		final int PROD_OPUNARY_TIMES                                = 149;  // <Op Unary> ::= '*' <Op Unary>
		final int PROD_OPUNARY_AMP                                  = 150;  // <Op Unary> ::= '&' <Op Unary>
		final int PROD_OPUNARY_PLUSPLUS                             = 151;  // <Op Unary> ::= '++' <Op Unary>
		final int PROD_OPUNARY_MINUSMINUS                           = 152;  // <Op Unary> ::= '--' <Op Unary>
		final int PROD_OPUNARY_PLUSPLUS2                            = 153;  // <Op Unary> ::= <Op Pointer> '++'
		final int PROD_OPUNARY_MINUSMINUS2                          = 154;  // <Op Unary> ::= <Op Pointer> '--'
		final int PROD_OPUNARY_LPAREN_RPAREN                        = 155;  // <Op Unary> ::= '(' <Type> ')' <Op Unary>
		final int PROD_OPUNARY_SIZEOF_LPAREN_RPAREN                 = 156;  // <Op Unary> ::= sizeof '(' <Type> ')'
		final int PROD_OPUNARY_SIZEOF_LPAREN_ID_RPAREN              = 157;  // <Op Unary> ::= sizeof '(' Id <Pointers> ')'
		final int PROD_OPUNARY                                      = 158;  // <Op Unary> ::= <Op Pointer>
		final int PROD_OPPOINTER_DOT                                = 159;  // <Op Pointer> ::= <Op Pointer> '.' <Value>
		final int PROD_OPPOINTER_MINUSGT                            = 160;  // <Op Pointer> ::= <Op Pointer> '->' <Value>
		final int PROD_OPPOINTER_LBRACKET_RBRACKET                  = 161;  // <Op Pointer> ::= <Op Pointer> '[' <Expr> ']'
		final int PROD_OPPOINTER                                    = 162;  // <Op Pointer> ::= <Value>
		final int PROD_VALUE_OCTLITERAL                             = 163;  // <Value> ::= OctLiteral
		final int PROD_VALUE_HEXLITERAL                             = 164;  // <Value> ::= HexLiteral
		final int PROD_VALUE_DECLITERAL                             = 165;  // <Value> ::= DecLiteral
		final int PROD_VALUE_STRINGLITERAL                          = 166;  // <Value> ::= StringLiteral
		final int PROD_VALUE_CHARLITERAL                            = 167;  // <Value> ::= CharLiteral
		final int PROD_VALUE_FLOATLITERAL                           = 168;  // <Value> ::= FloatLiteral
		final int PROD_VALUE_ID_LPAREN_RPAREN                       = 169;  // <Value> ::= Id '(' <Expr> ')'
		final int PROD_VALUE_ID_LPAREN_RPAREN2                      = 170;  // <Value> ::= Id '(' ')'
		final int PROD_VALUE_ID                                     = 171;  // <Value> ::= Id
		final int PROD_VALUE_LPAREN_RPAREN                          = 172;  // <Value> ::= '(' <Expr> ')'
	};

	private static BufferedReader buffR;
	
	// C-specific for switch analysis
	private Stack<Integer> firstCaseWithoutBreak = new Stack<Integer>();
	
	
	// FIXME Replace by parse method
	/***************************************************************
	 * This class will run the engine, and needs a file called config.dat
	 * in the current directory. This file should contain two lines,
	 * The first should be the absolute path name to the .cgt file, the second
	 * should be the source file you wish to parse.
	 * @param args Array of arguments.
	 ***************************************************************/
	public static void main(String[] args)
	{
		
		// FIXME
		String textToParse = "d:\\SW-Produkte\\Structorizer\\tests\\Issue228\\fibonacci-1.c";
		
		CParser parser = null;
		try {
			parser = new CParser();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		
		List<Root> roots = parser.parse(textToParse, "UTF-8");
		
		System.out.println(roots.size());
	}
	
	public CParser() throws Exception {

		parser = new GOLDParser();

		try
		{
			parser.loadCompiledGrammar(compiledGrammar);
		}
		catch(ParserException parse)
		{
			System.out.println("**PARSER ERROR**\n" + parse.toString());
			System.exit(1);
		}
		catch(Exception ex)
		{
			String msg = ex.getMessage();
			if (msg == null) {
				msg = ex.toString();
			}
			System.err.println("**GRAMMAR LOAD ERROR**: " + msg);
			ex.printStackTrace(System.err);
			throw ex;
		}
	}

	/**
	 * Parses the ANSI-C source code from file _textToParse, which is supposed to be encoded
	 * with the charset _encoding, and returns a list of structograms - one for each function
	 * or program contained in the source file.
	 * Field `error' will either contain an empty string or an error message afterwards.
	 * @param _textToParse - file name of the C source.
	 * @param _encoding - name of the charset to be used for decoding
	 * @return A list containing composed diagrams (if successful, otherwise field error will contain an error description) 
	 */
	public List<Root> parse(String textToParse, String _encoding) {
	
		// create new root
		root = new Root();
		error = "";
		
		prepareTextfile(textToParse, _encoding);

		try
		{
			parser.openFile(textToParse + ".structorizer");
		}
		catch(ParserException parse)
		{
			System.out.println("**PARSER ERROR**\n" + parse.toString());
			System.exit(1);
		}

		// Rolling buffer for processed tokens as retrospective context for error messages
		// Number of empty strings = number of retained context lines 
		String[] context = {"", "", "", "", "", "", "", "", "", ""};
		int contextLine = 0;

		boolean done = false;
		int response = -1;

		while(!done)
		{
			try
			{
				response = parser.parse();
			}
			catch(ParserException parse)
			{
				System.out.println("**PARSER ERROR**\n" + parse.toString());
				System.exit(1);
			}

			Token theTok;

			switch(response)
			{
			case gpMsgTokenRead:
				/* A token was read by the parser. The Token Object can be accessed
                      through the CurrentToken() property:  Parser.CurrentToken */
				Token myTok = parser.currentToken();
				System.out.println("gpMsgTokenRead: " + (String)myTok.getData());
				// START KGU#191 2016-04-30: Issue #182 (insufficient information for error detection)
				while (parser.currentLineNumber() > contextLine)
				{
					context[(++contextLine) % context.length] = "";
				}
				context[contextLine % context.length] += ((String)myTok.getData() + " ");
				// END KGU#191 2016-04-30
				break;

			case gpMsgReduction:
				/* This message is returned when a rule was reduced by the parse engine.
                      The CurrentReduction property is assigned a Reduction object
                      containing the rule and its related tokens. You can reassign this
                      property to your own customized class. If this is not the case,
                      this message can be ignored and the Reduction object will be used
                      to store the parse tree.  */

//				switch(parser.currentReduction().getParentRule().getTableIndex())
//				{
//				case RuleConstants.PROD_DECLS:
//					//<Decls> ::= <Decl> <Decls>
//					break;
//				case RuleConstants.PROD_DECLS2:
//					//<Decls> ::= 
//					break;
//				case RuleConstants.PROD_DECL:
//					//<Decl> ::= <Func Decl>
//					break;
//				case RuleConstants.PROD_DECL2:
//					//<Decl> ::= <Func Proto>
//					break;
//				case RuleConstants.PROD_DECL3:
//					//<Decl> ::= <Struct Decl>
//					break;
//				case RuleConstants.PROD_DECL4:
//					//<Decl> ::= <Union Decl>
//					break;
//				case RuleConstants.PROD_DECL5:
//					//<Decl> ::= <Enum Decl>
//					break;
//				case RuleConstants.PROD_DECL6:
//					//<Decl> ::= <Var Decl>
//					System.out.println("PROD_DECL6: " + parser.currentReduction().getToken(0).getData());
//					break;
//				case RuleConstants.PROD_DECL7:
//					//<Decl> ::= <Typedef Decl>
//					break;
//				case RuleConstants.PROD_FUNCPROTO_LPAREN_RPAREN_SEMI:
//					//<Func Proto> ::= <Func ID> '(' <Types> ')' ';'
//					break;
//				case RuleConstants.PROD_FUNCPROTO_LPAREN_RPAREN_SEMI2:
//					//<Func Proto> ::= <Func ID> '(' <Params> ')' ';'
//					break;
//				case RuleConstants.PROD_FUNCPROTO_LPAREN_RPAREN_SEMI3:
//					//<Func Proto> ::= <Func ID> '(' ')' ';'
//					break;
//				case RuleConstants.PROD_FUNCDECL_LPAREN_RPAREN:
//					//<Func Decl> ::= <Func ID> '(' <Params> ')' <Block>
//					break;
//				case RuleConstants.PROD_FUNCDECL_LPAREN_RPAREN2:
//					//<Func Decl> ::= <Func ID> '(' <Id List> ')' <Struct Def> <Block>
//					break;
//				case RuleConstants.PROD_FUNCDECL_LPAREN_RPAREN3:
//					//<Func Decl> ::= <Func ID> '(' ')' <Block>
//					break;
//				case RuleConstants.PROD_PARAMS_COMMA:
//					//<Params> ::= <Param> ',' <Params>
//					break;
//				case RuleConstants.PROD_PARAMS:
//					//<Params> ::= <Param>
//					break;
//				case RuleConstants.PROD_PARAM_CONST_ID:
//					//<Param> ::= const <Type> Id
//					break;
//				case RuleConstants.PROD_PARAM_ID:
//					//<Param> ::= <Type> Id
//					break;
//				case RuleConstants.PROD_TYPES_COMMA:
//					//<Types> ::= <Type> ',' <Types>
//					break;
//				case RuleConstants.PROD_TYPES:
//					//<Types> ::= <Type>
//					break;
//				case RuleConstants.PROD_IDLIST_ID_COMMA:
//					//<Id List> ::= Id ',' <Id List>
//					break;
//				case RuleConstants.PROD_IDLIST_ID:
//					//<Id List> ::= Id
//					break;
//				case RuleConstants.PROD_FUNCID_ID:
//					//<Func ID> ::= <Type> Id
//					break;
//				case RuleConstants.PROD_FUNCID_ID2:
//					//<Func ID> ::= Id
//					break;
//				case RuleConstants.PROD_TYPEDEFDECL_TYPEDEF_ID_SEMI:
//					//<Typedef Decl> ::= typedef <Type> Id ';'
//					break;
//				case RuleConstants.PROD_STRUCTDECL_STRUCT_ID_LBRACE_RBRACE_SEMI:
//					//<Struct Decl> ::= struct Id '{' <Struct Def> '}' ';'
//					break;
//				case RuleConstants.PROD_UNIONDECL_UNION_ID_LBRACE_RBRACE_SEMI:
//					//<Union Decl> ::= union Id '{' <Struct Def> '}' ';'
//					break;
//				case RuleConstants.PROD_STRUCTDEF:
//					//<Struct Def> ::= <Var Decl> <Struct Def>
//					break;
//				case RuleConstants.PROD_STRUCTDEF2:
//					//<Struct Def> ::= <Var Decl>
//					break;
//				case RuleConstants.PROD_VARDECL_SEMI:
//					//<Var Decl> ::= <Mod> <Type> <Var> <Var List> ';'
//					break;
//				case RuleConstants.PROD_VARDECL_SEMI2:
//					//<Var Decl> ::= <Type> <Var> <Var List> ';'
//					break;
//				case RuleConstants.PROD_VARDECL_SEMI3:
//					//<Var Decl> ::= <Mod> <Var> <Var List> ';'
//					break;
//				case RuleConstants.PROD_VAR_ID:
//					//<Var> ::= Id <Array>
//					break;
//				case RuleConstants.PROD_VAR_ID_EQ:
//					//<Var> ::= Id <Array> '=' <Op If>
//					break;
//				case RuleConstants.PROD_ARRAY_LBRACKET_RBRACKET:
//					//<Array> ::= '[' <Expr> ']'
//					break;
//				case RuleConstants.PROD_ARRAY_LBRACKET_RBRACKET2:
//					//<Array> ::= '[' ']'
//					break;
//				case RuleConstants.PROD_ARRAY:
//					//<Array> ::= 
//					break;
//				case RuleConstants.PROD_VARLIST_COMMA:
//					//<Var List> ::= ',' <Var Item> <Var List>
//					break;
//				case RuleConstants.PROD_VARLIST:
//					//<Var List> ::= 
//					break;
//				case RuleConstants.PROD_VARITEM:
//					//<Var Item> ::= <Pointers> <Var>
//					break;
//				case RuleConstants.PROD_MOD_EXTERN:
//					//<Mod> ::= extern
//					break;
//				case RuleConstants.PROD_MOD_STATIC:
//					//<Mod> ::= static
//					break;
//				case RuleConstants.PROD_MOD_REGISTER:
//					//<Mod> ::= register
//					break;
//				case RuleConstants.PROD_MOD_AUTO:
//					//<Mod> ::= auto
//					break;
//				case RuleConstants.PROD_MOD_VOLATILE:
//					//<Mod> ::= volatile
//					break;
//				case RuleConstants.PROD_MOD_CONST:
//					//<Mod> ::= const
//					break;
//				case RuleConstants.PROD_ENUMDECL_ENUM_ID_LBRACE_RBRACE_SEMI:
//					//<Enum Decl> ::= enum Id '{' <Enum Def> '}' ';'
//					break;
//				case RuleConstants.PROD_ENUMDEF_COMMA:
//					//<Enum Def> ::= <Enum Val> ',' <Enum Def>
//					break;
//				case RuleConstants.PROD_ENUMDEF:
//					//<Enum Def> ::= <Enum Val>
//					break;
//				case RuleConstants.PROD_ENUMVAL_ID:
//					//<Enum Val> ::= Id
//					break;
//				case RuleConstants.PROD_ENUMVAL_ID_EQ_OCTLITERAL:
//					//<Enum Val> ::= Id '=' OctLiteral
//					break;
//				case RuleConstants.PROD_ENUMVAL_ID_EQ_HEXLITERAL:
//					//<Enum Val> ::= Id '=' HexLiteral
//					break;
//				case RuleConstants.PROD_ENUMVAL_ID_EQ_DECLITERAL:
//					//<Enum Val> ::= Id '=' DecLiteral
//					break;
//				case RuleConstants.PROD_TYPE:
//					//<Type> ::= <Base> <Pointers>
//					break;
//				case RuleConstants.PROD_BASE:
//					//<Base> ::= <Sign> <Scalar>
//					break;
//				case RuleConstants.PROD_BASE_STRUCT_ID:
//					//<Base> ::= struct Id
//					break;
//				case RuleConstants.PROD_BASE_STRUCT_LBRACE_RBRACE:
//					//<Base> ::= struct '{' <Struct Def> '}'
//					break;
//				case RuleConstants.PROD_BASE_UNION_ID:
//					//<Base> ::= union Id
//					break;
//				case RuleConstants.PROD_BASE_UNION_LBRACE_RBRACE:
//					//<Base> ::= union '{' <Struct Def> '}'
//					break;
//				case RuleConstants.PROD_BASE_ENUM_ID:
//					//<Base> ::= enum Id
//					break;
//				case RuleConstants.PROD_SIGN_SIGNED:
//					//<Sign> ::= signed
//					break;
//				case RuleConstants.PROD_SIGN_UNSIGNED:
//					//<Sign> ::= unsigned
//					break;
//				case RuleConstants.PROD_SIGN:
//					//<Sign> ::= 
//					break;
//				case RuleConstants.PROD_SCALAR_CHAR:
//					//<Scalar> ::= char
//					break;
//				case RuleConstants.PROD_SCALAR_INT:
//					//<Scalar> ::= int
//					break;
//				case RuleConstants.PROD_SCALAR_SHORT:
//					//<Scalar> ::= short
//					break;
//				case RuleConstants.PROD_SCALAR_LONG:
//					//<Scalar> ::= long
//					break;
//				case RuleConstants.PROD_SCALAR_SHORT_INT:
//					//<Scalar> ::= short int
//					break;
//				case RuleConstants.PROD_SCALAR_LONG_INT:
//					//<Scalar> ::= long int
//					break;
//				case RuleConstants.PROD_SCALAR_FLOAT:
//					//<Scalar> ::= float
//					break;
//				case RuleConstants.PROD_SCALAR_DOUBLE:
//					//<Scalar> ::= double
//					break;
//				case RuleConstants.PROD_SCALAR_VOID:
//					//<Scalar> ::= void
//					break;
//				case RuleConstants.PROD_POINTERS_TIMES:
//					//<Pointers> ::= '*' <Pointers>
//					break;
//				case RuleConstants.PROD_POINTERS:
//					//<Pointers> ::= 
//					break;
//				case RuleConstants.PROD_STM:
//					//<Stm> ::= <Var Decl>
//					break;
//				case RuleConstants.PROD_STM_ID_COLON:
//					//<Stm> ::= Id ':'
//					break;
//				case RuleConstants.PROD_STM_IF_LPAREN_RPAREN:
//					//<Stm> ::= if '(' <Expr> ')' <Stm>
//					break;
//				case RuleConstants.PROD_STM_IF_LPAREN_RPAREN_ELSE:
//					//<Stm> ::= if '(' <Expr> ')' <Then Stm> else <Stm>
//					break;
//				case RuleConstants.PROD_STM_WHILE_LPAREN_RPAREN:
//					//<Stm> ::= while '(' <Expr> ')' <Stm>
//					break;
//				case RuleConstants.PROD_STM_FOR_LPAREN_SEMI_SEMI_RPAREN:
//					//<Stm> ::= for '(' <Arg> ';' <Arg> ';' <Arg> ')' <Stm>
//					break;
//				case RuleConstants.PROD_STM2:
//					//<Stm> ::= <Normal Stm>
//					break;
//				case RuleConstants.PROD_THENSTM_IF_LPAREN_RPAREN_ELSE:
//					//<Then Stm> ::= if '(' <Expr> ')' <Then Stm> else <Then Stm>
//					break;
//				case RuleConstants.PROD_THENSTM_WHILE_LPAREN_RPAREN:
//					//<Then Stm> ::= while '(' <Expr> ')' <Then Stm>
//					break;
//				case RuleConstants.PROD_THENSTM_FOR_LPAREN_SEMI_SEMI_RPAREN:
//					//<Then Stm> ::= for '(' <Arg> ';' <Arg> ';' <Arg> ')' <Then Stm>
//					break;
//				case RuleConstants.PROD_THENSTM:
//					//<Then Stm> ::= <Normal Stm>
//					break;
//				case RuleConstants.PROD_NORMALSTM_DO_WHILE_LPAREN_RPAREN:
//					//<Normal Stm> ::= do <Stm> while '(' <Expr> ')'
//					break;
//				case RuleConstants.PROD_NORMALSTM_SWITCH_LPAREN_RPAREN_LBRACE_RBRACE:
//					//<Normal Stm> ::= switch '(' <Expr> ')' '{' <Case Stms> '}'
//					break;
//				case RuleConstants.PROD_NORMALSTM:
//					//<Normal Stm> ::= <Block>
//					break;
//				case RuleConstants.PROD_NORMALSTM_SEMI:
//					//<Normal Stm> ::= <Expr> ';'
//					break;
//				case RuleConstants.PROD_NORMALSTM_GOTO_ID_SEMI:
//					//<Normal Stm> ::= goto Id ';'
//					break;
//				case RuleConstants.PROD_NORMALSTM_BREAK_SEMI:
//					//<Normal Stm> ::= break ';'
//					break;
//				case RuleConstants.PROD_NORMALSTM_CONTINUE_SEMI:
//					//<Normal Stm> ::= continue ';'
//					break;
//				case RuleConstants.PROD_NORMALSTM_RETURN_SEMI:
//					//<Normal Stm> ::= return <Expr> ';'
//					break;
//				case RuleConstants.PROD_NORMALSTM_SEMI2:
//					//<Normal Stm> ::= ';'
//					break;
//				case RuleConstants.PROD_ARG:
//					//<Arg> ::= <Expr>
//					break;
//				case RuleConstants.PROD_ARG2:
//					//<Arg> ::= 
//					break;
//				case RuleConstants.PROD_CASESTMS_CASE_COLON:
//					//<Case Stms> ::= case <Value> ':' <Stm List> <Case Stms>
//					break;
//				case RuleConstants.PROD_CASESTMS_DEFAULT_COLON:
//					//<Case Stms> ::= default ':' <Stm List>
//					break;
//				case RuleConstants.PROD_CASESTMS:
//					//<Case Stms> ::= 
//					break;
//				case RuleConstants.PROD_BLOCK_LBRACE_RBRACE:
//					//<Block> ::= '{' <Stm List> '}'
//					break;
//				case RuleConstants.PROD_STMLIST:
//					//<Stm List> ::= <Stm> <Stm List>
//					break;
//				case RuleConstants.PROD_STMLIST2:
//					//<Stm List> ::= 
//					break;
//				case RuleConstants.PROD_EXPR_COMMA:
//					//<Expr> ::= <Expr> ',' <Op Assign>
//					break;
//				case RuleConstants.PROD_EXPR:
//					//<Expr> ::= <Op Assign>
//					break;
//				case RuleConstants.PROD_OPASSIGN_EQ:
//					//<Op Assign> ::= <Op If> '=' <Op Assign>
//					break;
//				case RuleConstants.PROD_OPASSIGN_PLUSEQ:
//					//<Op Assign> ::= <Op If> '+=' <Op Assign>
//					break;
//				case RuleConstants.PROD_OPASSIGN_MINUSEQ:
//					//<Op Assign> ::= <Op If> '-=' <Op Assign>
//					break;
//				case RuleConstants.PROD_OPASSIGN_TIMESEQ:
//					//<Op Assign> ::= <Op If> '*=' <Op Assign>
//					break;
//				case RuleConstants.PROD_OPASSIGN_DIVEQ:
//					//<Op Assign> ::= <Op If> '/=' <Op Assign>
//					break;
//				case RuleConstants.PROD_OPASSIGN_CARETEQ:
//					//<Op Assign> ::= <Op If> '^=' <Op Assign>
//					break;
//				case RuleConstants.PROD_OPASSIGN_AMPEQ:
//					//<Op Assign> ::= <Op If> '&=' <Op Assign>
//					break;
//				case RuleConstants.PROD_OPASSIGN_PIPEEQ:
//					//<Op Assign> ::= <Op If> '|=' <Op Assign>
//					break;
//				case RuleConstants.PROD_OPASSIGN_GTGTEQ:
//					//<Op Assign> ::= <Op If> '>>=' <Op Assign>
//					break;
//				case RuleConstants.PROD_OPASSIGN_LTLTEQ:
//					//<Op Assign> ::= <Op If> '<<=' <Op Assign>
//					break;
//				case RuleConstants.PROD_OPASSIGN:
//					//<Op Assign> ::= <Op If>
//					break;
//				case RuleConstants.PROD_OPIF_QUESTION_COLON:
//					//<Op If> ::= <Op Or> '?' <Op If> ':' <Op If>
//					break;
//				case RuleConstants.PROD_OPIF:
//					//<Op If> ::= <Op Or>
//					break;
//				case RuleConstants.PROD_OPOR_PIPEPIPE:
//					//<Op Or> ::= <Op Or> '||' <Op And>
//					break;
//				case RuleConstants.PROD_OPOR:
//					//<Op Or> ::= <Op And>
//					break;
//				case RuleConstants.PROD_OPAND_AMPAMP:
//					//<Op And> ::= <Op And> '&&' <Op BinOR>
//					break;
//				case RuleConstants.PROD_OPAND:
//					//<Op And> ::= <Op BinOR>
//					break;
//				case RuleConstants.PROD_OPBINOR_PIPE:
//					//<Op BinOR> ::= <Op BinOR> '|' <Op BinXOR>
//					break;
//				case RuleConstants.PROD_OPBINOR:
//					//<Op BinOR> ::= <Op BinXOR>
//					break;
//				case RuleConstants.PROD_OPBINXOR_CARET:
//					//<Op BinXOR> ::= <Op BinXOR> '^' <Op BinAND>
//					break;
//				case RuleConstants.PROD_OPBINXOR:
//					//<Op BinXOR> ::= <Op BinAND>
//					break;
//				case RuleConstants.PROD_OPBINAND_AMP:
//					//<Op BinAND> ::= <Op BinAND> '&' <Op Equate>
//					break;
//				case RuleConstants.PROD_OPBINAND:
//					//<Op BinAND> ::= <Op Equate>
//					break;
//				case RuleConstants.PROD_OPEQUATE_EQEQ:
//					//<Op Equate> ::= <Op Equate> '==' <Op Compare>
//					break;
//				case RuleConstants.PROD_OPEQUATE_EXCLAMEQ:
//					//<Op Equate> ::= <Op Equate> '!=' <Op Compare>
//					break;
//				case RuleConstants.PROD_OPEQUATE:
//					//<Op Equate> ::= <Op Compare>
//					break;
//				case RuleConstants.PROD_OPCOMPARE_LT:
//					//<Op Compare> ::= <Op Compare> '<' <Op Shift>
//					break;
//				case RuleConstants.PROD_OPCOMPARE_GT:
//					//<Op Compare> ::= <Op Compare> '>' <Op Shift>
//					break;
//				case RuleConstants.PROD_OPCOMPARE_LTEQ:
//					//<Op Compare> ::= <Op Compare> '<=' <Op Shift>
//					break;
//				case RuleConstants.PROD_OPCOMPARE_GTEQ:
//					//<Op Compare> ::= <Op Compare> '>=' <Op Shift>
//					break;
//				case RuleConstants.PROD_OPCOMPARE:
//					//<Op Compare> ::= <Op Shift>
//					break;
//				case RuleConstants.PROD_OPSHIFT_LTLT:
//					//<Op Shift> ::= <Op Shift> '<<' <Op Add>
//					break;
//				case RuleConstants.PROD_OPSHIFT_GTGT:
//					//<Op Shift> ::= <Op Shift> '>>' <Op Add>
//					break;
//				case RuleConstants.PROD_OPSHIFT:
//					//<Op Shift> ::= <Op Add>
//					break;
//				case RuleConstants.PROD_OPADD_PLUS:
//					//<Op Add> ::= <Op Add> '+' <Op Mult>
//					break;
//				case RuleConstants.PROD_OPADD_MINUS:
//					//<Op Add> ::= <Op Add> '-' <Op Mult>
//					break;
//				case RuleConstants.PROD_OPADD:
//					//<Op Add> ::= <Op Mult>
//					break;
//				case RuleConstants.PROD_OPMULT_TIMES:
//					//<Op Mult> ::= <Op Mult> '*' <Op Unary>
//					break;
//				case RuleConstants.PROD_OPMULT_DIV:
//					//<Op Mult> ::= <Op Mult> '/' <Op Unary>
//					break;
//				case RuleConstants.PROD_OPMULT_PERCENT:
//					//<Op Mult> ::= <Op Mult> '%' <Op Unary>
//					break;
//				case RuleConstants.PROD_OPMULT:
//					//<Op Mult> ::= <Op Unary>
//					break;
//				case RuleConstants.PROD_OPUNARY_EXCLAM:
//					//<Op Unary> ::= '!' <Op Unary>
//					break;
//				case RuleConstants.PROD_OPUNARY_TILDE:
//					//<Op Unary> ::= '~' <Op Unary>
//					break;
//				case RuleConstants.PROD_OPUNARY_MINUS:
//					//<Op Unary> ::= '-' <Op Unary>
//					break;
//				case RuleConstants.PROD_OPUNARY_TIMES:
//					//<Op Unary> ::= '*' <Op Unary>
//					break;
//				case RuleConstants.PROD_OPUNARY_AMP:
//					//<Op Unary> ::= '&' <Op Unary>
//					break;
//				case RuleConstants.PROD_OPUNARY_PLUSPLUS:
//					//<Op Unary> ::= '++' <Op Unary>
//					break;
//				case RuleConstants.PROD_OPUNARY_MINUSMINUS:
//					//<Op Unary> ::= '--' <Op Unary>
//					break;
//				case RuleConstants.PROD_OPUNARY_PLUSPLUS2:
//					//<Op Unary> ::= <Op Pointer> '++'
//					break;
//				case RuleConstants.PROD_OPUNARY_MINUSMINUS2:
//					//<Op Unary> ::= <Op Pointer> '--'
//					break;
//				case RuleConstants.PROD_OPUNARY_LPAREN_RPAREN:
//					//<Op Unary> ::= '(' <Type> ')' <Op Unary>
//					break;
//				case RuleConstants.PROD_OPUNARY_SIZEOF_LPAREN_RPAREN:
//					//<Op Unary> ::= sizeof '(' <Type> ')'
//					break;
//				case RuleConstants.PROD_OPUNARY_SIZEOF_LPAREN_ID_RPAREN:
//					//<Op Unary> ::= sizeof '(' Id <Pointers> ')'
//					break;
//				case RuleConstants.PROD_OPUNARY:
//					//<Op Unary> ::= <Op Pointer>
//					break;
//				case RuleConstants.PROD_OPPOINTER_DOT:
//					//<Op Pointer> ::= <Op Pointer> '.' <Value>
//					break;
//				case RuleConstants.PROD_OPPOINTER_MINUSGT:
//					//<Op Pointer> ::= <Op Pointer> '->' <Value>
//					break;
//				case RuleConstants.PROD_OPPOINTER_LBRACKET_RBRACKET:
//					//<Op Pointer> ::= <Op Pointer> '[' <Expr> ']'
//					break;
//				case RuleConstants.PROD_OPPOINTER:
//					//<Op Pointer> ::= <Value>
//					break;
//				case RuleConstants.PROD_VALUE_OCTLITERAL:
//					//<Value> ::= OctLiteral
//					break;
//				case RuleConstants.PROD_VALUE_HEXLITERAL:
//					//<Value> ::= HexLiteral
//					break;
//				case RuleConstants.PROD_VALUE_DECLITERAL:
//					//<Value> ::= DecLiteral
//					break;
//				case RuleConstants.PROD_VALUE_STRINGLITERAL:
//					//<Value> ::= StringLiteral
//					break;
//				case RuleConstants.PROD_VALUE_CHARLITERAL:
//					//<Value> ::= CharLiteral
//					break;
//				case RuleConstants.PROD_VALUE_FLOATLITERAL:
//					//<Value> ::= FloatLiteral
//					break;
//				case RuleConstants.PROD_VALUE_ID_LPAREN_RPAREN:
//					//<Value> ::= Id '(' <Expr> ')'
//					break;
//				case RuleConstants.PROD_VALUE_ID_LPAREN_RPAREN2:
//					//<Value> ::= Id '(' ')'
//					break;
//				case RuleConstants.PROD_VALUE_ID:
//					//<Value> ::= Id
//					break;
//				case RuleConstants.PROD_VALUE_LPAREN_RPAREN:
//					//<Value> ::= '(' <Expr> ')'
//					break;
//				}

				//Parser.Reduction = //Object you created to store the rule

				// ************************************** log file
				System.out.println("gpMsgReduction");
				Reduction myRed = parser.currentReduction();
				System.out.println(myRed.getParentRule().getText());
				// ************************************** end log

				break;

			case gpMsgAccept:
				/* The program was accepted by the parsing engine */

				buildNSD(parser.currentReduction());
				// ************************************** log file
				System.out.println("gpMsgAccept");
				// ************************************** end log

				done = true;

				break;

			case gpMsgLexicalError:
				/* Place code here to handle a illegal or unrecognized token
                           To recover, pop the token from the stack: Parser.PopInputToken */

				theTok = parser.currentToken();
				error = ("Unexpected character: " + (String)theTok.getData()+" at line "+parser.currentLineNumber());

				// ************************************** log file
				System.out.println("gpMsgLexicalError");
				// ************************************** end log

				done = true;
				
				parser.popInputToken();

				break;

			case gpMsgNotLoadedError:
				/* Load the Compiled Grammar Table file first. */

				// ************************************** log file
				System.out.println("gpMsgNotLoadedError");
				// ************************************** end log

				done = true;

				break;

			case gpMsgSyntaxError:
				/* This is a syntax error: the source has produced a token that was
                           not expected by the LALR State Machine. The expected tokens are stored
                           into the Tokens() list. To recover, push one of the
                              expected tokens onto the parser's input queue (the first in this case):
                           You should limit the number of times this type of recovery can take
                           place. */

				theTok = parser.currentToken();
				error = ("Syntax Error: " + (String)theTok.getData() + " at line "+parser.currentLineNumber());

				// ************************************** log file
				System.out.println("gpMsgSyntaxError");
				// ************************************** end log
				
				done = true;

				break;

			case gpMsgCommentError:
				/* The end of the input was reached while reading a comment.
                             This is caused by a comment that was not terminated */

				theTok = parser.currentToken();
				error = ("Comment Error: " + (String)theTok.getData() + " at line "+parser.currentLineNumber());

				// ************************************** log file
				System.out.println("gpMsgCommentError");
				// ************************************** end log

				done = true;

				break;

			case gpMsgInternalError:
				/* Something horrid happened inside the parser. You cannot recover */

				// ************************************** log file
				System.out.println("gpMsgInternalError");
				// ************************************** end log

				done = true;

				break;
			}
		}

		// START KGU#191 2016-04-30: Issue #182 - In error case append the context 
		if (!error.isEmpty())
		{
			error += "\nPreceding source context:";
			contextLine -= context.length;
			for (int line = 0; line < context.length; line++)
			{
				if (++contextLine >= 0)
				{
					error += "\n" + contextLine + ":   " + context[contextLine % context.length];
				}
			}
		}
		// END KGU#191 2016-04-30

		try
		{
			parser.closeFile();
		}
		catch(ParserException parse)
		{
			System.out.println("**PARSER ERROR**\n" + parse.toString());
			System.exit(1);
		}

		// remove the temporary file
		(new File(textToParse + ".structorizer")).delete();
		
		// START KGU#194 2016-07-07: Enh. #185/#188 - Try to convert calls to Call elements
		StringList signatures = new StringList();
		for (Root subroutine : subRoots)
		{
			if (!subroutine.isProgram)
			{
				signatures.add(subroutine.getMethodName() + "#" + subroutine.getParameterNames().count());
			}
		}
		// END KGU#194 2016-07-07
		
		// START KGU#194 2016-05-08: Bugfix #185 - face an empty program or unit vessel
		//return root;
		if (subRoots.isEmpty() || root.children.getSize() > 0)
		{
			subRoots.add(0, root);
		}
		// START KGU#194 2016-07-07: Enh. #185/#188 - Try to convert calls to Call elements
		for (Root aRoot : subRoots)
		{
			if (aRoot.getMethodName().equals("main")) {
				if (aRoot.getParameterNames().count() == 0) {
					String fileName = new File(textToParse).getName();
					if (fileName.contains(".")) {
						fileName = fileName.substring(0, fileName.indexOf('.'));
					}
					if (this.optionUpperCaseProgName) {
						fileName = fileName.toUpperCase();
					}
					aRoot.setText(fileName);
				}
				aRoot.isProgram = true;
			}
			aRoot.convertToCalls(signatures);
		}
		// END KGU#194 2016-07-07
		
		return subRoots;
	}

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
	private void prepareTextfile(String _textToParse, String _encoding)
	{
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
					if (strLine.matches(".*[^!=<>][=]\\s*?[{].*[}].*")) {
						StringList tokens = Element.splitLexically(strLine, true);
						int posAsgn = tokens.indexOf("=");
						int posLBrace = tokens.indexOf("{", posAsgn+1);
						StringList items = Element.splitExpressionList(tokens.subSequence(posLBrace+1, tokens.count()).concatenate(), ",", true);
						if (items.count() > 0 && items.get(items.count()-1).startsWith("}")) {
							strLine = tokens.subSequence(0, posLBrace-1).concatenate() +
									arrayIniFunc + items.subSequence(0, items.count()-1).concatenate(", ") +
									")" + items.get(items.count()-1).substring(1);
						}
					}
					srcCode += strLine + "\n";
				}
			}
			//Close the input stream
			in.close();

			for (Entry entry: defines.entrySet()) {
				System.out.println(Matcher.quoteReplacement((String)entry.getValue()));
				srcCode = srcCode.replaceAll("(.*?\\W)" + entry.getKey() + "(\\W.*?)", "$1"+ Matcher.quoteReplacement((String)entry.getValue()) + "$2");
			}

			Regex r = new Regex("(.*\\()\\s*?void\\s*?(\\).*)", "$1$2");
			srcCode = r.replaceAll(srcCode);
			
			//System.out.println(pasCode);

			// trim and save as new file
			OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(_textToParse+".structorizer"), "UTF-8");
			ow.write(srcCode.trim()+"\n");
			//System.out.println("==> "+filterNonAscii(pasCode.trim()+"\n"));
			ow.close();
		}
		catch (Exception e) 
		{
			System.out.println(e.getMessage());
		}	
	}

	/**
	 * Preselects the type of the initial diagram to be imported as function.
	 * @see lu.fisch.structorizer.parsers.CodeParser#initializeBuildNSD()
	 */
	@Override
	protected void initializeBuildNSD()
	{
		root.isProgram = false;	// C programs are functions, primarily
		this.optionUpperCaseProgName = Root.check(6);
	}

	@Override
	protected void buildNSD_R(Reduction _reduction, Subqueue _parentNode)
	{
		//String content = new String();
	
		if (_reduction.getTokenCount() > 0)
		{
			String ruleName = _reduction.getParentRule().name();
			int ruleId = _reduction.getParentRule().getTableIndex();
			System.out.println("buildNSD_R(" + ruleName + ", " + _parentNode.parent + ")...");

			if (
					// Assignment or procedure call?
					ruleId == RuleConstants.PROD_OPASSIGN_EQ
					||
					ruleId == RuleConstants.PROD_VALUE_ID_LPAREN_RPAREN
					||
					ruleId == RuleConstants.PROD_VALUE_ID_LPAREN_RPAREN2
					||
					ruleId == RuleConstants.PROD_VALUE_ID_LPAREN_RPAREN
					)
			{
				// Simply convet it as text and create an instruction. In case of a call
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
					type = getContent_R((Reduction)_reduction.getToken(typeIx).getData(), "");
				}
				// Now concern on the first declaration of the list
				Reduction secReduc = (Reduction)_reduction.getToken(typeIx + 1).getData();
				ruleId = secReduc.getParentRule().getTableIndex();
				String content = getContent_R(secReduc, "");
				if (ruleId == RuleConstants.PROD_VAR_ID) {
					// Simple declaration - if allowed then make them to a Pascal decl.
					if (this.optionImportVarDecl) {
						content = "var " + content + ": " + type;
						_parentNode.addElement(new Instruction(translateContent(content)));
					}
				}
				else {
					// Should be RuleConstants.PROD_VAR_ID_EQ. Now it can get tricky if arrays
					// are involved - the executor copes with lines like
					// int data[4] <- {2,5,6,3}. On the other hand, an import without the type
					// but with index brackets would leave a totally wrong semantics. So we must
					// drop both or none.
					String varName = (String)secReduc.getToken(0).getData();
					//String arrayTag = this.getContent_R((Reduction)thdReduc.getToken(1).getData(), "");
					String expr = this.getContent_R((Reduction)secReduc.getToken(3).getData(), "");
					content = translateContent(content);
					if (this.optionImportVarDecl) {
						_parentNode.addElement(new Instruction(type + " " + content));
					}
					else {
						_parentNode.addElement(new Instruction(varName + " <- " + translateContent(expr)));
					}
				}
				if (_reduction.getTokenCount() > typeIx+2) {
					secReduc = (Reduction)_reduction.getToken(typeIx + 2).getData();	// <Var List>
					ruleId = secReduc.getParentRule().getTableIndex();
					while (ruleId == RuleConstants.PROD_VARLIST_COMMA) {
						Reduction thdReduc = (Reduction)secReduc.getToken(1).getData();	// <Var Item>
						content = getContent_R(thdReduc, "");
						// Now decide whether it was initialized
						thdReduc = (Reduction)thdReduc.getToken(2).getData();	// <Var>
						if (thdReduc.getParentRule().getTableIndex() == RuleConstants.PROD_VAR_ID) {
							// Simple declaration - if allowed then make them to a Pascal decl.
							if (this.optionImportVarDecl) {
								content = "var " + content + ": " + type;
								_parentNode.addElement(new Instruction(translateContent(content)));
							}
						}
						else {
							// Should be RuleConstants.PROD_VAR_ID_EQ. Now it can get tricky if arrays
							// are involved - the executor copes with lines like
							// int data[4] <- {2,5,6,3}. On the other hand, an import without the type
							// but with index brackets would leave a totally wrong semantics. So we must
							// drop both or none.
							String varName = (String) thdReduc.getToken(0).getData();
							//String arrayTag = this.getContent_R((Reduction)thdReduc.getToken(1).getData(), "");
							String expr = this.getContent_R((Reduction)thdReduc.getToken(3).getData(), "");
							content = translateContent(content);
							if (this.optionImportVarDecl) {
								_parentNode.addElement(new Instruction(type + " " + content));
							}
							else {
								_parentNode.addElement(new Instruction(varName + " <- " + translateContent(expr)));
							}
						}
						secReduc = (Reduction)secReduc.getToken(2).getData();	// <Var List>
						ruleId = secReduc.getParentRule().getTableIndex();
					}
				}
			}
			else if (
					// JUMP instruction?
					ruleId == RuleConstants.PROD_NORMALSTM_BREAK_SEMI
					||
					ruleId == RuleConstants.PROD_NORMALSTM_RETURN_SEMI
					)
			{
				String content = getKeyword("preLeave");
				if (ruleId == RuleConstants.PROD_NORMALSTM_RETURN_SEMI) {
					content = translateContent(getKeyword("preReturn") + " " + getContent_R((Reduction)_reduction.getToken(1).getData(), ""));
				}
				//System.out.println(ruleName + ": " + content);
				_parentNode.addElement(new Jump(content.trim()));				
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
				String lval = getContent_R((Reduction)_reduction.getToken(0).getData(), "");
				String expr = getContent_R((Reduction)_reduction.getToken(2).getData(), "");
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
				String lval = getContent_R((Reduction)_reduction.getToken(lvalIx).getData(), "");
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
				Root prevRoot = root;	// Push the original root
				if (!root.getText().getText().trim().equals("???")) {
					root = new Root();	// Prepare a new root for the subroutine
					subRoots.add(root);
				}
				String content = new String();
				content = getContent_R((Reduction) _reduction.getToken(0).getData(),content);
				content += "(";
				int bodyIndex = 4;
				String params = "";
				if (ruleId == RuleConstants.PROD_FUNCDECL_LPAREN_RPAREN) {
					params = getContent_R((Reduction)_reduction.getToken(2).getData(), "");
				}
				else if (ruleId == RuleConstants.PROD_FUNCDECL_LPAREN_RPAREN2) {
					bodyIndex = 5;
					params = getContent_R((Reduction)_reduction.getToken(2).getData(), "");
				}
				else if (ruleId == RuleConstants.PROD_FUNCDECL_LPAREN_RPAREN3) {
					bodyIndex = 3;
				}
				content += params + ")";
				root.setText(content);
				if (_reduction.getToken(bodyIndex).getKind() == SymbolTypeConstants.symbolTypeNonterminal)
				{
					buildNSD_R((Reduction) _reduction.getToken(bodyIndex).getData(), root.children);
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
				content = getContent_R((Reduction) _reduction.getToken(1).getData(),content);
				While ele = new While(getKeyword("preWhile")+translateContent(content)+getKeyword("postWhile"));
				_parentNode.addElement(ele);
				
				Reduction secReduc = (Reduction) _reduction.getToken(3).getData();
				buildNSD_R(secReduc,ele.q);
			}
			else if (
					// REPEAT loop?
					ruleId == RuleConstants.PROD_NORMALSTM_DO_WHILE_LPAREN_RPAREN
					 )
			{
				String content = new String();
				content = getContent_R((Reduction) _reduction.getToken(4).getData(),content);
				// FIXME We might look for kinds of expressions with direct negation possibility,
				// e.g. PROD_OPEQUATE_EQEQ, PROD_OPEQUATE_EXCLAMEQ, PROD_OPCOMPARE_LT, PROD_OPCOMPARE_GT
				// etc. where we could try to replace the reduction by its opposite.
				Repeat ele = new Repeat(getKeyword("preRepeat") + " not (" + content + ")" + getKeyword("postRepeat"));
				_parentNode.addElement(ele);
				
				Reduction secReduc = (Reduction) _reduction.getToken(1).getData();
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
				Reduction secReduc = (Reduction) _reduction.getToken(2).getData();
				buildNSD_R(secReduc, _parentNode);
				// Mark all offsprings of the FOR loop with a (by default) yellowish colour
				_parentNode.getElement(_parentNode.getSize()-1).setColor(Element.color2);
				
				// get the second part - should be an ordinary condition
				String content = getContent_R((Reduction) _reduction.getToken(4).getData(), "");
				While ele = new While(getKeyword("preWhile")+translateContent(content)+getKeyword("postWhile"));
				// Mark all offsprings of the FOR loop with a (by default) yellowish colour
				ele.setColor(Element.color2);
				_parentNode.addElement(ele);
				
				// Get and convert the body
				secReduc = (Reduction) _reduction.getToken(8).getData();
				buildNSD_R(secReduc, ele.q);

				// get the last part of the header now and append it to the body
				secReduc = (Reduction) _reduction.getToken(6).getData();
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
				content = getContent_R((Reduction) _reduction.getToken(2).getData(),content);
				
				Alternative ele = new Alternative(getKeyword("preAlt") + translateContent(content) + getKeyword("postAlt"));
				_parentNode.addElement(ele);
				
				Reduction secReduc = (Reduction) _reduction.getToken(4).getData();
				buildNSD_R(secReduc,ele.qTrue);
				if (ruleId == RuleConstants.PROD_STM_IF_LPAREN_RPAREN_ELSE)
				{
					secReduc = (Reduction) _reduction.getToken(6).getData();
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
				Case parent = (Case) _parentNode.parent;
				String content = new String();
				int iNext = 0;	// index of the next free selector entry
				for (int i = 1; i < parent.getText().count() && iNext == 0; i++) {
					if (parent.getText().get(i).equals("??"))
					{
						iNext = i;
					}
				}
				if (iNext == 0) { iNext = parent.getText().count()-1; }
				boolean lastCaseWasEmpty = iNext > 1 && parent.qs.get(iNext-2).getSize() == 0;
				int branchIx = 2;	// token id for default branch
				// Which is the last previous branch ending with jump instruction?
				int lastCaseWithBreak = -1;
				for (int i = 0; i < iNext-1; i++) {
					int size = parent.qs.get(i).getSize();
					if (size > 0 && (parent.qs.get(i).getElement(size-1) instanceof Jump)) {
						lastCaseWithBreak = i;
					}
					else {
						break;
					}
				}
				if (ruleId == RuleConstants.PROD_CASESTMS_CASE_COLON) {
					// Get the selector constant
					String selector = getContent_R((Reduction) _reduction.getToken(1).getData(), "");
					// If the last branch was empty then just add the selector to the list
					if (lastCaseWasEmpty) {
						String selectors = parent.getText().get(iNext-1) + ", " + selector;
						parent.getText().set(iNext - 1, selectors);
					}
					else {
						parent.getText().set(iNext, selector);
					}
					branchIx = 3;
				}
				// Add the branch content
				Reduction secReduc = (Reduction) _reduction.getToken(branchIx).getData();
				Subqueue sq = (Subqueue) parent.qs.get(iNext-1);
				buildNSD_R(secReduc, sq);
						
				// append copies of the elements of the new case to all cases still not terminated
				for (int i = lastCaseWithBreak+1; i < iNext-1; i++) {
					Subqueue sq1 = parent.qs.get(i);
					for (int j = 0; j < sq.getSize(); j++) {
						Element el = sq.getElement(j).copy();	// FIXME: Need a new Id!
						sq1.addElement(el);
					}
				}
				if (ruleId == RuleConstants.PROD_CASESTMS_CASE_COLON) {
					buildNSD_R((Reduction)_reduction.getToken(branchIx+1).getData(), _parentNode);					
				}
			}
			else if (
					// CASE statement (switch)
					 ruleId == RuleConstants.PROD_NORMALSTM_SWITCH_LPAREN_RPAREN_LBRACE_RBRACE
					 )
			{
				//this.firstCaseWithoutBreak.push(0);

				String content = new String();
				// Put the discriminator into the first line of content
				content = getKeyword("preCase")+getContent_R((Reduction) _reduction.getToken(2).getData(),content)+getKeyword("postCase");

				// How many branches has the CASE element? We must count the non-empty statement lists!
				Reduction sr = (Reduction) _reduction.getToken(5).getData();
				int j = 0;
				//System.out.println(sr.getParentRule().getText());  // <<<<<<<
				while (sr.getParentRule().getTableIndex() == RuleConstants.PROD_CASESTMS_CASE_COLON)
				{
					Reduction stmList = (Reduction) sr.getToken(3).getData();
					if (stmList.getParentRule().getTableIndex() == RuleConstants.PROD_STMLIST) {
						// non-empty statement list, so we will have to set up a branch
						j++;
						content += "\n??";
					}
					sr = (Reduction) sr.getToken(4).getData();
				}

				if (sr.getParentRule().getTableIndex() == RuleConstants.PROD_CASESTMS_DEFAULT_COLON)
				{
					content += "\ndefault";
				}
				else {
					content += "\"%";
				}
				j++;

				// Pooh, the translation is risky...
				Case ele = new Case(translateContent(content));
				//ele.setText(updateContent(content));
				_parentNode.addElement(ele);

				// Create the selector branches
				Reduction secReduc = (Reduction) _reduction.getToken(5).getData();
				buildNSD_R(secReduc,(Subqueue) ele.qs.get(0));

				// In theory, all branches should end with a break instruction
				// unless they end with return or exit. Drop the break instructions now.
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
			// Block...?
			else
			{
				if (_reduction.getTokenCount()>0)
				{
					for(int i=0; i<_reduction.getTokenCount(); i++)
					{
						if (_reduction.getToken(i).getKind()==SymbolTypeConstants.symbolTypeNonterminal)
						{
							buildNSD_R((Reduction) _reduction.getToken(i).getData(),_parentNode);
						}
					}
				}
			}
		}
	}

	private String translateContent(String _content)
	{
		String output = getKeyword("output");
		String input = getKeyword("input");
		_content = _content.replaceAll(BString.breakup("printf")+"[((](.*?)[))]", output+" $1");
		_content = _content.replaceAll(BString.breakup("scanf")+"[((](.*?),[ ]*[&]?(.*?)[))]", input+" $2");
		
		//System.out.println(_content);
		
		/*
		 _content:=ReplaceEntities(_content);
		*/
		
		// Convert the pseudo function back to array initializers
		int posIni = _content.indexOf(arrayIniFunc);
		if (posIni >= 0) {
			StringList items = Element.splitExpressionList(_content.substring(posIni + arrayIniFunc.length()), ",", true);
			_content = _content.substring(0, posIni) + "{" + items.subSequence(0, items.count()-1).concatenate(", ") +
					"}" + items.get(items.count()-1).substring(1);
		}
		
		//_content = BString.replace(_content, ":="," \u2190 ");
		//_content = BString.replace(_content, " = "," <- "); already done by getContent_R()!

		return _content.trim();
	}
	
	@Override
	protected String getContent_R(Reduction _reduction, String _content)
	{
		for(int i=0; i<_reduction.getTokenCount(); i++)
		{
			Token token = _reduction.getToken(i);
			switch (token.getKind()) 
			{
			case SymbolTypeConstants.symbolTypeNonterminal:
				int ruleId = _reduction.getParentRule().getTableIndex();
				_content = getContent_R((Reduction) token.getData(), _content);	
				break;
			case SymbolTypeConstants.symbolTypeTerminal:
			{
				String toAdd = "";
				int idx = token.getTableIndex();
				switch (token.getTableIndex()) {
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
				default:
					toAdd = (String)token.getData();
					if (idx >= SymbolConstants.SYM_MINUS && idx < SymbolConstants.SYM_LPAREN
							||
							idx > SymbolConstants.SYM_RPAREN && idx < SymbolConstants.SYM_DOT
							||
							idx > SymbolConstants.SYM_DOT && idx < SymbolConstants.SYM_LBRACKET
							||
							idx > SymbolConstants.SYM_RBRACKET && idx < SymbolConstants.SYM_LBRACE
							||
							idx > SymbolConstants.SYM_RBRACE && idx <= SymbolConstants.SYM_GTEQ
							) {
						toAdd = " " + toAdd + " ";
					}
					else if (toAdd.matches("^\\w.*") && _content.matches(".*\\w$")) {
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
	
}

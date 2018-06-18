/*
    Structorizer
    A little tool which you can use to create Nassi-Schneiderman Diagrams (NSD)

    Copyright (C) 2009  Bob Fisch
    Copyright (C) 2017  StructorizerParserTemplate.pgt: Kay Gürtzig

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
 *      Description:    Class to parse an ANSI-C file and build structograms from the reduction tree.
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
 *      Kay Gürtzig     2017.04.27      Enh. #354: Bugs in procedure and expression list evaluation fixed
 *      Simon Sobisch   2017.05.23      Enh. #409: File type .h added
 *      Kay Gürtzig     2017.05.23/24   Enh. #354/#411: Pre-processor workaround for typedef significantly improved
 *      Simon Sobisch   2017.05.24      Enh. #409: Comment-aware #define analysis, with function macro approach
 *                                                 C continuation line handling
 *      Kay Gürtzig     2017.05.26      Enh. #409: #define analysis (including function macros) accomplished
 *      Kay Gürtzig     2017.05.28      Issue #409: Recursion overhead in buildNSD_R() significantly reduced. 
 *      Kay Gürtzig     2017.06.22      Enh. #420: Prepared for comment retrieval
 *      Simon Sobisch   2017.06.26      Enh. #409: Handling of C includes: preparse them for retrieving defines;
 *                                                 Minimized Pattern/Matcher Objects used by explicit using/reseting them
 *                                                 instead of using the implicit generation by replaceAll
 *      Kay Gürtzig     2017.07.01      Enh. #389: Include mechanism revised
 *      Kay Gürtzig     2017.09.30      Enh. #411: Bugfix in typedef preparation, enh. #423: struct import done
 *                                      Enh. #420: Comment delimiter specification added
 *      Kay Gürtzig     2018.06.04      Issue #533: Import of C struct definitions hadn't been converted to Structorizer syntax
 *      Kay Gürtzig     2018.06.17      Bugfix #540: replaceDefinedEntries() could get caught in an eternal loop
 *                                      Enh. #541: New option "redundantNames" to eliminate disturbing symbols or macros
 *                                      Bugfix #542: return without expr. not supported, result type void now suppressed
 *      Kay Gürtzig     2018.06.18      KGU#525: Better support for legacy function definition syntax
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

import java.awt.Color;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.Vector;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import lu.fisch.utils.StringList;

/**
 * Code import parser class of Structorizer 3.27, based on GOLDParser 5.0 for the C language.
 * This file contains grammar-specific constants and individual routines to build
 * structograms (Nassi-Shneiderman diagrams) from the parsing tree. 
 * @author Kay Gürtzig
 */
public class CParser extends CodeParser
{
	
	/** Default diagram name for an importable program diagram with global definitions */
	private static final String DEFAULT_GLOBAL_NAME = "GlobalDefinitions";
	/** Template for the generation of grammar-conform user type ids (typedef-declared) */
	private static final String USER_TYPE_ID_MASK = "user_type_%03d";
	/** Replacement pattern for the decomposition of composed typdefs (named struct def + type def) */
	private static final String TYPEDEF_DECOMP_REPLACER = "$1 $2;\ntypedef $1 $3;";
	// START KGU#407 2017-06-22: Enh. #420 
	/** rule ids representing statements, used as stoppers for comment retrieval */
	private static final int[] statementIds = new int[]{
		RuleConstants.PROD_FUNCDECL_LPAREN_RPAREN,
		RuleConstants.PROD_FUNCDECL_LPAREN_RPAREN2,
		RuleConstants.PROD_FUNCDECL_LPAREN_VOID_RPAREN,
		RuleConstants.PROD_FUNCDECL_LPAREN_RPAREN3,
		RuleConstants.PROD_VARDECL_SEMI,
		RuleConstants.PROD_VARDECL_CONST_SEMI,
		RuleConstants.PROD_VARDECL_SEMI2,
		RuleConstants.PROD_VARDECL_SEMI3,
		RuleConstants.PROD_VARDECL_CONST_SEMI2,
		RuleConstants.PROD_OPASSIGN_EQ,
		RuleConstants.PROD_OPASSIGN_PLUSEQ,
		RuleConstants.PROD_OPASSIGN_MINUSEQ,
		RuleConstants.PROD_OPASSIGN_TIMESEQ,
		RuleConstants.PROD_OPASSIGN_DIVEQ,
		RuleConstants.PROD_OPASSIGN_CARETEQ,
		RuleConstants.PROD_OPASSIGN_AMPEQ,
		RuleConstants.PROD_OPASSIGN_PIPEEQ,
		RuleConstants.PROD_OPASSIGN_GTGTEQ,
		RuleConstants.PROD_OPASSIGN_LTLTEQ,
		RuleConstants.PROD_OPUNARY_PLUSPLUS,
		RuleConstants.PROD_OPUNARY_PLUSPLUS2,
		RuleConstants.PROD_OPUNARY_MINUSMINUS,
		RuleConstants.PROD_OPUNARY_MINUSMINUS2,
		RuleConstants.PROD_STRUCTDECL_STRUCT_ID_LBRACE_RBRACE,
		RuleConstants.PROD_UNIONDECL_UNION_ID_LBRACE_RBRACE,
		RuleConstants.PROD_ENUMDECL_ENUM_ID_LBRACE_RBRACE,
		RuleConstants.PROD_TYPEDEFDECL_TYPEDEF,
		RuleConstants.PROD_NORMALSTM_BREAK_SEMI,
		RuleConstants.PROD_NORMALSTM_RETURN_SEMI,
		RuleConstants.PROD_NORMALSTM_RETURN_SEMI2,
		RuleConstants.PROD_NORMALSTM_GOTO_ID_SEMI,
		RuleConstants.PROD_NORMALSTM_CONTINUE_SEMI,
		RuleConstants.PROD_CALLID_ID_LPAREN_RPAREN,
		RuleConstants.PROD_CALLID_ID_LPAREN_RPAREN2,
		RuleConstants.PROD_STM_WHILE_LPAREN_RPAREN,
		RuleConstants.PROD_THENSTM_WHILE_LPAREN_RPAREN,
		RuleConstants.PROD_NORMALSTM_DO_WHILE_LPAREN_RPAREN,
		RuleConstants.PROD_STM_FOR_LPAREN_SEMI_SEMI_RPAREN,
		RuleConstants.PROD_THENSTM_FOR_LPAREN_SEMI_SEMI_RPAREN,
		RuleConstants.PROD_STM_IF_LPAREN_RPAREN,
		RuleConstants.PROD_STM_IF_LPAREN_RPAREN_ELSE,
		RuleConstants.PROD_CASESTMS_CASE_COLON,
		RuleConstants.PROD_CASESTMS_DEFAULT_COLON,
		RuleConstants.PROD_NORMALSTM_SWITCH_LPAREN_RPAREN_LBRACE_RBRACE
	};
	// END KGU#407 2017-06-22

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
		final String[] exts = { "c", "h" };
		return exts;
	}
	
	//------------------- Comment delimiter specification ---------------------------------
	
	// START KGU#407 2017-09-30: Enh. #420
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.parsers.CodeParser#getCommentDelimiters()
	 */
	protected String[][] getCommentDelimiters()
	{
		return new String[][]{
			{"/*", "*/"},
			{"//"}
		};
	}
	// END KGU#407 2017-09-30
	
	//------------------- Grammar table constants DON'T MODIFY! ---------------------------
	
	// Symbolic constants naming the table indices of the symbols of the grammar 
	//@SuppressWarnings("unused")
	private interface SymbolConstants 
	{
//		final int SYM_EOF           =   0;  // (EOF)
//		final int SYM_ERROR         =   1;  // (Error)
//		final int SYM_COMMENT       =   2;  // Comment
//		final int SYM_NEWLINE       =   3;  // NewLine
//		final int SYM_WHITESPACE    =   4;  // Whitespace
//		final int SYM_TIMESDIV      =   5;  // '*/'
//		final int SYM_DIVTIMES      =   6;  // '/*'
//		final int SYM_DIVDIV        =   7;  // '//'
		final int SYM_MINUS         =   8;  // '-'
//		final int SYM_MINUSMINUS    =   9;  // '--'
		final int SYM_EXCLAM        =  10;  // '!'
		final int SYM_EXCLAMEQ      =  11;  // '!='
		final int SYM_PERCENT       =  12;  // '%'
		final int SYM_AMP           =  13;  // '&'
		final int SYM_AMPAMP        =  14;  // '&&'
//		final int SYM_AMPEQ         =  15;  // '&='
//		final int SYM_LPAREN        =  16;  // '('
//		final int SYM_RPAREN        =  17;  // ')'
		final int SYM_TIMES         =  18;  // '*'
//		final int SYM_TIMESEQ       =  19;  // '*='
//		final int SYM_COMMA         =  20;  // ','
//		final int SYM_DOT           =  21;  // '.'
		final int SYM_DIV           =  22;  // '/'
//		final int SYM_DIVEQ         =  23;  // '/='
//		final int SYM_COLON         =  24;  // ':'
//		final int SYM_SEMI          =  25;  // ';'
//		final int SYM_QUESTION      =  26;  // '?'
//		final int SYM_LBRACKET      =  27;  // '['
//		final int SYM_RBRACKET      =  28;  // ']'
		final int SYM_CARET         =  29;  // '^'
//		final int SYM_CARETEQ       =  30;  // '^='
		final int SYM_LBRACE        =  31;  // '{'
		final int SYM_PIPE          =  32;  // '|'
		final int SYM_PIPEPIPE      =  33;  // '||'
//		final int SYM_PIPEEQ        =  34;  // '|='
		final int SYM_RBRACE        =  35;  // '}'
		final int SYM_TILDE         =  36;  // '~'
		final int SYM_PLUS          =  37;  // '+'
//		final int SYM_PLUSPLUS      =  38;  // '++'
//		final int SYM_PLUSEQ        =  39;  // '+='
		final int SYM_LT            =  40;  // '<'
		final int SYM_LTLT          =  41;  // '<<'
//		final int SYM_LTLTEQ        =  42;  // '<<='
		final int SYM_LTEQ          =  43;  // '<='
		final int SYM_EQ            =  44;  // '='
//		final int SYM_MINUSEQ       =  45;  // '-='
		final int SYM_EQEQ          =  46;  // '=='
		final int SYM_GT            =  47;  // '>'
//		final int SYM_MINUSGT       =  48;  // '->'
		final int SYM_GTEQ          =  49;  // '>='
		final int SYM_GTGT          =  50;  // '>>'
//		final int SYM_GTGTEQ        =  51;  // '>>='
//		final int SYM_AUTO          =  52;  // auto
//		final int SYM_BREAK         =  53;  // break
//		final int SYM_CASE          =  54;  // case
//		final int SYM_CHAR          =  55;  // char
//		final int SYM_CHARLITERAL   =  56;  // CharLiteral
//		final int SYM_CONST         =  57;  // const
//		final int SYM_CONTINUE      =  58;  // continue
		final int SYM_DECLITERAL    =  59;  // DecLiteral
//		final int SYM_DEFAULT       =  60;  // default
//		final int SYM_DO            =  61;  // do
//		final int SYM_DOUBLE        =  62;  // double
//		final int SYM_ELSE          =  63;  // else
//		final int SYM_ENUM          =  64;  // enum
//		final int SYM_EXTERN        =  65;  // extern
//		final int SYM_FLOAT         =  66;  // float
		final int SYM_FLOATLITERAL  =  67;  // FloatLiteral
//		final int SYM_FOR           =  68;  // for
//		final int SYM_GOTO          =  69;  // goto
		final int SYM_HEXLITERAL    =  70;  // HexLiteral
//		final int SYM_ID            =  71;  // Id
//		final int SYM_IF            =  72;  // if
//		final int SYM_INT           =  73;  // int
//		final int SYM_LONG          =  74;  // long
		final int SYM_OCTLITERAL    =  75;  // OctLiteral
//		final int SYM_REGISTER      =  76;  // register
//		final int SYM_RETURN        =  77;  // return
//		final int SYM_SHORT         =  78;  // short
//		final int SYM_SIGNED        =  79;  // signed
//		final int SYM_SIZEOF        =  80;  // sizeof
//		final int SYM_STATIC        =  81;  // static
		final int SYM_STRINGLITERAL =  82;  // StringLiteral
//		final int SYM_STRUCT        =  83;  // struct
//		final int SYM_SWITCH        =  84;  // switch
//		final int SYM_TYPEDEF       =  85;  // typedef
//		final int SYM_UNION         =  86;  // union
//		final int SYM_UNSIGNED      =  87;  // unsigned
		final int SYM_USERTYPEID    =  88;  // UserTypeId
//		final int SYM_VOID          =  89;  // void
//		final int SYM_VOLATILE      =  90;  // volatile
//		final int SYM_WCHAR_T       =  91;  // 'wchar_t'
//		final int SYM_WHILE         =  92;  // while
//		final int SYM_ARG           =  93;  // <Arg>
//		final int SYM_ARRAY         =  94;  // <Array>
//		final int SYM_BASE          =  95;  // <Base>
//		final int SYM_BLOCK         =  96;  // <Block>
//		final int SYM_CALLID        =  97;  // <Call Id>
//		final int SYM_CASESTMS      =  98;  // <Case Stms>
//		final int SYM_CONSTMOD      =  99;  // <ConstMod>
//		final int SYM_CONSTPOINTERS = 100;  // <ConstPointers>
//		final int SYM_CONSTTYPE     = 101;  // <ConstType>
//		final int SYM_DECL          = 102;  // <Decl>
//		final int SYM_DECLEND       = 103;  // <Decl End>
//		final int SYM_DECLSTMLIST   = 104;  // <Decl Stm List>
//		final int SYM_ENUMDECL      = 105;  // <Enum Decl>
//		final int SYM_ENUMDEF       = 106;  // <Enum Def>
//		final int SYM_ENUMVAL       = 107;  // <Enum Val>
//		final int SYM_EXPR          = 108;  // <Expr>
//		final int SYM_EXPRINI       = 109;  // <ExprIni>
//		final int SYM_EXTDECL       = 110;  // <ExtDecl>
//		final int SYM_EXTDECLS      = 111;  // <ExtDecls>
//		final int SYM_FUNCDECL      = 112;  // <Func Decl>
//		final int SYM_FUNCID        = 113;  // <Func ID>
//		final int SYM_FUNCPROTO     = 114;  // <Func Proto>
//		final int SYM_IDLIST        = 115;  // <Id List>
//		final int SYM_INITIALIZER   = 116;  // <Initializer>
//		final int SYM_MOD           = 117;  // <Mod>
//		final int SYM_NORMALSTM     = 118;  // <Normal Stm>
//		final int SYM_OPADD         = 119;  // <Op Add>
//		final int SYM_OPAND         = 120;  // <Op And>
//		final int SYM_OPASSIGN      = 121;  // <Op Assign>
//		final int SYM_OPBINAND      = 122;  // <Op BinAND>
//		final int SYM_OPBINOR       = 123;  // <Op BinOR>
//		final int SYM_OPBINXOR      = 124;  // <Op BinXOR>
//		final int SYM_OPCOMPARE     = 125;  // <Op Compare>
//		final int SYM_OPEQUATE      = 126;  // <Op Equate>
//		final int SYM_OPIF          = 127;  // <Op If>
//		final int SYM_OPMULT        = 128;  // <Op Mult>
//		final int SYM_OPOR          = 129;  // <Op Or>
//		final int SYM_OPPOINTER     = 130;  // <Op Pointer>
//		final int SYM_OPSHIFT       = 131;  // <Op Shift>
//		final int SYM_OPUNARY       = 132;  // <Op Unary>
//		final int SYM_PARAM         = 133;  // <Param>
//		final int SYM_PARAMS        = 134;  // <Params>
//		final int SYM_POINTERS      = 135;  // <Pointers>
//		final int SYM_SCALAR        = 136;  // <Scalar>
//		final int SYM_SIGN          = 137;  // <Sign>
//		final int SYM_STM           = 138;  // <Stm>
//		final int SYM_STMLIST       = 139;  // <Stm List>
//		final int SYM_STRUCTDECL    = 140;  // <Struct Decl>
//		final int SYM_STRUCTDEF     = 141;  // <Struct Def>
//		final int SYM_THENSTM       = 142;  // <Then Stm>
//		final int SYM_TYPE          = 143;  // <Type>
//		final int SYM_TYPEDEFDECL   = 144;  // <Typedef Decl>
//		final int SYM_TYPES         = 145;  // <Types>
//		final int SYM_UNIONDECL     = 146;  // <Union Decl>
//		final int SYM_VALUE         = 147;  // <Value>
//		final int SYM_VAR           = 148;  // <Var>
//		final int SYM_VARDECL       = 149;  // <Var Decl>
//		final int SYM_VARITEM       = 150;  // <Var Item>
//		final int SYM_VARLIST       = 151;  // <Var List>
	};

	// Symbolic constants naming the table indices of the grammar rules
//	@SuppressWarnings("unused")
	private interface RuleConstants
	{
//		final int PROD_EXTDECLS                                     =   0;  // <ExtDecls> ::= <ExtDecl> <ExtDecls>
//		final int PROD_EXTDECLS2                                    =   1;  // <ExtDecls> ::= 
//		final int PROD_EXTDECL                                      =   2;  // <ExtDecl> ::= <Func Decl>
//		final int PROD_EXTDECL2                                     =   3;  // <ExtDecl> ::= <Func Proto>
//		final int PROD_EXTDECL3                                     =   4;  // <ExtDecl> ::= <Decl>
//		final int PROD_DECL                                         =   5;  // <Decl> ::= <Struct Decl>
//		final int PROD_DECL2                                        =   6;  // <Decl> ::= <Union Decl>
//		final int PROD_DECL3                                        =   7;  // <Decl> ::= <Enum Decl>
//		final int PROD_DECL4                                        =   8;  // <Decl> ::= <Var Decl>
//		final int PROD_DECL5                                        =   9;  // <Decl> ::= <Typedef Decl>
//		final int PROD_FUNCPROTO_LPAREN_RPAREN_SEMI                 =  10;  // <Func Proto> ::= <Func ID> '(' <Types> ')' ';'
//		final int PROD_FUNCPROTO_LPAREN_RPAREN_SEMI2                =  11;  // <Func Proto> ::= <Func ID> '(' <Params> ')' ';'
//		final int PROD_FUNCPROTO_LPAREN_VOID_RPAREN_SEMI            =  12;  // <Func Proto> ::= <Func ID> '(' void ')' ';'
//		final int PROD_FUNCPROTO_LPAREN_RPAREN_SEMI3                =  13;  // <Func Proto> ::= <Func ID> '(' ')' ';'
		final int PROD_FUNCDECL_LPAREN_RPAREN                       =  14;  // <Func Decl> ::= <Func ID> '(' <Params> ')' <Block>
		final int PROD_FUNCDECL_LPAREN_RPAREN2                      =  15;  // <Func Decl> ::= <Func ID> '(' <Id List> ')' <Struct Def> <Block>
		final int PROD_FUNCDECL_LPAREN_VOID_RPAREN                  =  16;  // <Func Decl> ::= <Func ID> '(' void ')' <Block>
		final int PROD_FUNCDECL_LPAREN_RPAREN3                      =  17;  // <Func Decl> ::= <Func ID> '(' ')' <Block>
//		final int PROD_PARAMS_COMMA                                 =  18;  // <Params> ::= <Param> ',' <Params>
//		final int PROD_PARAMS                                       =  19;  // <Params> ::= <Param>
//		final int PROD_PARAM_ID                                     =  20;  // <Param> ::= <ConstType> Id <Array>
//		final int PROD_TYPES_COMMA                                  =  21;  // <Types> ::= <ConstType> ',' <Types>
//		final int PROD_TYPES                                        =  22;  // <Types> ::= <ConstType>
//		final int PROD_IDLIST_ID_COMMA                              =  23;  // <Id List> ::= Id ',' <Id List>
//		final int PROD_IDLIST_ID                                    =  24;  // <Id List> ::= Id
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
//		final int PROD_STRUCTDEF2                                   =  35;  // <Struct Def> ::= <Var Decl>
//		final int PROD_DECLEND_SEMI                                 =  36;  // <Decl End> ::= ';'
		final int PROD_DECLEND_SEMI2                                =  37;  // <Decl End> ::= <Var Item> <Var List> ';'
		final int PROD_VARDECL_SEMI                                 =  38;  // <Var Decl> ::= <ConstMod> <Type> <Var> <Var List> ';'
		final int PROD_VARDECL_CONST_SEMI                           =  39;  // <Var Decl> ::= <Mod> const <Type> <Var> <Var List> ';'
		final int PROD_VARDECL_SEMI2                                =  40;  // <Var Decl> ::= <ConstType> <Var> <Var List> ';'
		final int PROD_VARDECL_SEMI3                                =  41;  // <Var Decl> ::= <ConstMod> <Var> <Var List> ';'
		final int PROD_VARDECL_CONST_SEMI2                          =  42;  // <Var Decl> ::= const <Var> <Var List> ';'
		final int PROD_VAR_ID                                       =  43;  // <Var> ::= Id <Array>
		final int PROD_VAR_ID_EQ                                    =  44;  // <Var> ::= Id <Array> '=' <Initializer>
//		final int PROD_ARRAY_LBRACKET_RBRACKET                      =  45;  // <Array> ::= '[' <Expr> ']'
//		final int PROD_ARRAY_LBRACKET_RBRACKET2                     =  46;  // <Array> ::= '[' ']'
//		final int PROD_ARRAY                                        =  47;  // <Array> ::= 
		final int PROD_VARLIST_COMMA                                =  48;  // <Var List> ::= ',' <Var Item> <Var List>
//		final int PROD_VARLIST                                      =  49;  // <Var List> ::= 
		final int PROD_VARITEM                                      =  50;  // <Var Item> ::= <Pointers> <Var>
//		final int PROD_CONSTMOD_CONST                               =  51;  // <ConstMod> ::= const <Mod>
//		final int PROD_CONSTMOD                                     =  52;  // <ConstMod> ::= <Mod>
//		final int PROD_MOD_EXTERN                                   =  53;  // <Mod> ::= extern
//		final int PROD_MOD_STATIC                                   =  54;  // <Mod> ::= static
//		final int PROD_MOD_REGISTER                                 =  55;  // <Mod> ::= register
//		final int PROD_MOD_AUTO                                     =  56;  // <Mod> ::= auto
//		final int PROD_MOD_VOLATILE                                 =  57;  // <Mod> ::= volatile
		final int PROD_ENUMDECL_ENUM_ID_LBRACE_RBRACE               =  58;  // <Enum Decl> ::= enum Id '{' <Enum Def> '}' <Decl End>
//		final int PROD_ENUMDEF_COMMA                                =  59;  // <Enum Def> ::= <Enum Val> ',' <Enum Def>
//		final int PROD_ENUMDEF                                      =  60;  // <Enum Def> ::= <Enum Val>
//		final int PROD_ENUMVAL_ID                                   =  61;  // <Enum Val> ::= Id
//		final int PROD_ENUMVAL_ID_EQ_OCTLITERAL                     =  62;  // <Enum Val> ::= Id '=' OctLiteral
//		final int PROD_ENUMVAL_ID_EQ_HEXLITERAL                     =  63;  // <Enum Val> ::= Id '=' HexLiteral
//		final int PROD_ENUMVAL_ID_EQ_DECLITERAL                     =  64;  // <Enum Val> ::= Id '=' DecLiteral
		final int PROD_CONSTTYPE_CONST                              =  65;  // <ConstType> ::= const <Type>
//		final int PROD_CONSTTYPE                                    =  66;  // <ConstType> ::= <Type>
//		final int PROD_TYPE                                         =  67;  // <Type> ::= <Base> <Pointers>
//		final int PROD_BASE                                         =  68;  // <Base> ::= <Sign> <Scalar>
		final int PROD_BASE_STRUCT_ID                               =  69;  // <Base> ::= struct Id
		final int PROD_BASE_STRUCT_LBRACE_RBRACE                    =  70;  // <Base> ::= struct '{' <Struct Def> '}'
//		final int PROD_BASE_UNION_ID                                =  71;  // <Base> ::= union Id
//		final int PROD_BASE_UNION_LBRACE_RBRACE                     =  72;  // <Base> ::= union '{' <Struct Def> '}'
//		final int PROD_BASE_ENUM_ID                                 =  73;  // <Base> ::= enum Id
//		final int PROD_BASE_ENUM_LBRACE_RBRACE                      =  74;  // <Base> ::= enum '{' <Enum Def> '}'
//		final int PROD_BASE_VOID_TIMES                              =  75;  // <Base> ::= void '*'
//		final int PROD_BASE_USERTYPEID                              =  76;  // <Base> ::= UserTypeId
//		final int PROD_SIGN_SIGNED                                  =  77;  // <Sign> ::= signed
//		final int PROD_SIGN_UNSIGNED                                =  78;  // <Sign> ::= unsigned
//		final int PROD_SIGN                                         =  79;  // <Sign> ::= 
//		final int PROD_SCALAR_CHAR                                  =  80;  // <Scalar> ::= char
//		final int PROD_SCALAR_WCHAR_T                               =  81;  // <Scalar> ::= 'wchar_t'
//		final int PROD_SCALAR_INT                                   =  82;  // <Scalar> ::= int
//		final int PROD_SCALAR_SHORT                                 =  83;  // <Scalar> ::= short
//		final int PROD_SCALAR_LONG                                  =  84;  // <Scalar> ::= long
//		final int PROD_SCALAR_SHORT_INT                             =  85;  // <Scalar> ::= short int
//		final int PROD_SCALAR_LONG_INT                              =  86;  // <Scalar> ::= long int
//		final int PROD_SCALAR_LONG_LONG                             =  87;  // <Scalar> ::= long long
//		final int PROD_SCALAR_LONG_LONG_INT                         =  88;  // <Scalar> ::= long long int
//		final int PROD_SCALAR_FLOAT                                 =  89;  // <Scalar> ::= float
//		final int PROD_SCALAR_DOUBLE                                =  90;  // <Scalar> ::= double
//		final int PROD_POINTERS_TIMES                               =  91;  // <Pointers> ::= '*' <Pointers>
//		final int PROD_POINTERS_TIMES_CONST                         =  92;  // <Pointers> ::= '*' const <ConstPointers>
//		final int PROD_POINTERS                                     =  93;  // <Pointers> ::= 
//		final int PROD_CONSTPOINTERS_TIMES_CONST                    =  94;  // <ConstPointers> ::= '*' const <ConstPointers>
//		final int PROD_CONSTPOINTERS_TIMES                          =  95;  // <ConstPointers> ::= '*'
//		final int PROD_CONSTPOINTERS                                =  96;  // <ConstPointers> ::= 
		final int PROD_STM_ID_COLON                                 =  97;  // <Stm> ::= Id ':'
		final int PROD_STM_IF_LPAREN_RPAREN                         =  98;  // <Stm> ::= if '(' <Expr> ')' <Stm>
		final int PROD_STM_IF_LPAREN_RPAREN_ELSE                    =  99;  // <Stm> ::= if '(' <Expr> ')' <Then Stm> else <Stm>
		final int PROD_STM_WHILE_LPAREN_RPAREN                      = 100;  // <Stm> ::= while '(' <Expr> ')' <Stm>
		final int PROD_STM_FOR_LPAREN_SEMI_SEMI_RPAREN              = 101;  // <Stm> ::= for '(' <Arg> ';' <Arg> ';' <Arg> ')' <Stm>
//		final int PROD_STM                                          = 102;  // <Stm> ::= <Normal Stm>
//		final int PROD_THENSTM_IF_LPAREN_RPAREN_ELSE                = 103;  // <Then Stm> ::= if '(' <Expr> ')' <Then Stm> else <Then Stm>
		final int PROD_THENSTM_WHILE_LPAREN_RPAREN                  = 104;  // <Then Stm> ::= while '(' <Expr> ')' <Then Stm>
		final int PROD_THENSTM_FOR_LPAREN_SEMI_SEMI_RPAREN          = 105;  // <Then Stm> ::= for '(' <Arg> ';' <Arg> ';' <Arg> ')' <Then Stm>
//		final int PROD_THENSTM                                      = 106;  // <Then Stm> ::= <Normal Stm>
		final int PROD_NORMALSTM_DO_WHILE_LPAREN_RPAREN             = 107;  // <Normal Stm> ::= do <Stm> while '(' <Expr> ')'
		final int PROD_NORMALSTM_SWITCH_LPAREN_RPAREN_LBRACE_RBRACE = 108;  // <Normal Stm> ::= switch '(' <Expr> ')' '{' <Case Stms> '}'
//		final int PROD_NORMALSTM                                    = 109;  // <Normal Stm> ::= <Block>
//		final int PROD_NORMALSTM_SEMI                               = 110;  // <Normal Stm> ::= <Expr> ';'
		final int PROD_NORMALSTM_GOTO_ID_SEMI                       = 111;  // <Normal Stm> ::= goto Id ';'
		final int PROD_NORMALSTM_BREAK_SEMI                         = 112;  // <Normal Stm> ::= break ';'
		final int PROD_NORMALSTM_CONTINUE_SEMI                      = 113;  // <Normal Stm> ::= continue ';'
		final int PROD_NORMALSTM_RETURN_SEMI                        = 114;  // <Normal Stm> ::= return <Expr> ';'
		final int PROD_NORMALSTM_RETURN_SEMI2                       = 115;  // <Normal Stm> ::= return ';'
//		final int PROD_NORMALSTM_SEMI2                              = 116;  // <Normal Stm> ::= ';'
//		final int PROD_ARG                                          = 117;  // <Arg> ::= <Expr>
//		final int PROD_ARG2                                         = 118;  // <Arg> ::= 
		final int PROD_CASESTMS_CASE_COLON                          = 119;  // <Case Stms> ::= case <Value> ':' <Stm List> <Case Stms>
		final int PROD_CASESTMS_DEFAULT_COLON                       = 120;  // <Case Stms> ::= default ':' <Stm List>
//		final int PROD_CASESTMS                                     = 121;  // <Case Stms> ::= 
//		final int PROD_BLOCK_LBRACE_RBRACE                          = 122;  // <Block> ::= '{' <Decl Stm List> '}'
//		final int PROD_DECLSTMLIST                                  = 123;  // <Decl Stm List> ::= <Decl> <Decl Stm List>
//		final int PROD_DECLSTMLIST2                                 = 124;  // <Decl Stm List> ::= <Stm List>
		final int PROD_STMLIST                                      = 125;  // <Stm List> ::= <Stm> <Stm List>
//		final int PROD_STMLIST2                                     = 126;  // <Stm List> ::= 
//		final int PROD_INITIALIZER                                  = 127;  // <Initializer> ::= <Op If>
//		final int PROD_INITIALIZER_LBRACE_RBRACE                    = 128;  // <Initializer> ::= '{' <ExprIni> '}'
//		final int PROD_EXPR_COMMA                                   = 129;  // <Expr> ::= <Expr> ',' <Op Assign>
//		final int PROD_EXPR                                         = 130;  // <Expr> ::= <Op Assign>
//		final int PROD_EXPRINI_COMMA                                = 131;  // <ExprIni> ::= <ExprIni> ',' <Initializer>
//		final int PROD_EXPRINI                                      = 132;  // <ExprIni> ::= <Initializer>
		final int PROD_OPASSIGN_EQ                                  = 133;  // <Op Assign> ::= <Op If> '=' <Op Assign>
		final int PROD_OPASSIGN_PLUSEQ                              = 134;  // <Op Assign> ::= <Op If> '+=' <Op Assign>
		final int PROD_OPASSIGN_MINUSEQ                             = 135;  // <Op Assign> ::= <Op If> '-=' <Op Assign>
		final int PROD_OPASSIGN_TIMESEQ                             = 136;  // <Op Assign> ::= <Op If> '*=' <Op Assign>
		final int PROD_OPASSIGN_DIVEQ                               = 137;  // <Op Assign> ::= <Op If> '/=' <Op Assign>
		final int PROD_OPASSIGN_CARETEQ                             = 138;  // <Op Assign> ::= <Op If> '^=' <Op Assign>
		final int PROD_OPASSIGN_AMPEQ                               = 139;  // <Op Assign> ::= <Op If> '&=' <Op Assign>
		final int PROD_OPASSIGN_PIPEEQ                              = 140;  // <Op Assign> ::= <Op If> '|=' <Op Assign>
		final int PROD_OPASSIGN_GTGTEQ                              = 141;  // <Op Assign> ::= <Op If> '>>=' <Op Assign>
		final int PROD_OPASSIGN_LTLTEQ                              = 142;  // <Op Assign> ::= <Op If> '<<=' <Op Assign>
//		final int PROD_OPASSIGN                                     = 143;  // <Op Assign> ::= <Op If>
//		final int PROD_OPIF_QUESTION_COLON                          = 144;  // <Op If> ::= <Op Or> '?' <Op If> ':' <Op If>
//		final int PROD_OPIF                                         = 145;  // <Op If> ::= <Op Or>
//		final int PROD_OPOR_PIPEPIPE                                = 146;  // <Op Or> ::= <Op Or> '||' <Op And>
//		final int PROD_OPOR                                         = 147;  // <Op Or> ::= <Op And>
//		final int PROD_OPAND_AMPAMP                                 = 148;  // <Op And> ::= <Op And> '&&' <Op BinOR>
//		final int PROD_OPAND                                        = 149;  // <Op And> ::= <Op BinOR>
//		final int PROD_OPBINOR_PIPE                                 = 150;  // <Op BinOR> ::= <Op BinOR> '|' <Op BinXOR>
//		final int PROD_OPBINOR                                      = 151;  // <Op BinOR> ::= <Op BinXOR>
//		final int PROD_OPBINXOR_CARET                               = 152;  // <Op BinXOR> ::= <Op BinXOR> '^' <Op BinAND>
//		final int PROD_OPBINXOR                                     = 153;  // <Op BinXOR> ::= <Op BinAND>
//		final int PROD_OPBINAND_AMP                                 = 154;  // <Op BinAND> ::= <Op BinAND> '&' <Op Equate>
//		final int PROD_OPBINAND                                     = 155;  // <Op BinAND> ::= <Op Equate>
//		final int PROD_OPEQUATE_EQEQ                                = 156;  // <Op Equate> ::= <Op Equate> '==' <Op Compare>
//		final int PROD_OPEQUATE_EXCLAMEQ                            = 157;  // <Op Equate> ::= <Op Equate> '!=' <Op Compare>
//		final int PROD_OPEQUATE                                     = 158;  // <Op Equate> ::= <Op Compare>
//		final int PROD_OPCOMPARE_LT                                 = 159;  // <Op Compare> ::= <Op Compare> '<' <Op Shift>
//		final int PROD_OPCOMPARE_GT                                 = 160;  // <Op Compare> ::= <Op Compare> '>' <Op Shift>
//		final int PROD_OPCOMPARE_LTEQ                               = 161;  // <Op Compare> ::= <Op Compare> '<=' <Op Shift>
//		final int PROD_OPCOMPARE_GTEQ                               = 162;  // <Op Compare> ::= <Op Compare> '>=' <Op Shift>
//		final int PROD_OPCOMPARE                                    = 163;  // <Op Compare> ::= <Op Shift>
//		final int PROD_OPSHIFT_LTLT                                 = 164;  // <Op Shift> ::= <Op Shift> '<<' <Op Add>
//		final int PROD_OPSHIFT_GTGT                                 = 165;  // <Op Shift> ::= <Op Shift> '>>' <Op Add>
//		final int PROD_OPSHIFT                                      = 166;  // <Op Shift> ::= <Op Add>
//		final int PROD_OPADD_PLUS                                   = 167;  // <Op Add> ::= <Op Add> '+' <Op Mult>
//		final int PROD_OPADD_MINUS                                  = 168;  // <Op Add> ::= <Op Add> '-' <Op Mult>
//		final int PROD_OPADD                                        = 169;  // <Op Add> ::= <Op Mult>
//		final int PROD_OPMULT_TIMES                                 = 170;  // <Op Mult> ::= <Op Mult> '*' <Op Unary>
//		final int PROD_OPMULT_DIV                                   = 171;  // <Op Mult> ::= <Op Mult> '/' <Op Unary>
//		final int PROD_OPMULT_PERCENT                               = 172;  // <Op Mult> ::= <Op Mult> '%' <Op Unary>
//		final int PROD_OPMULT                                       = 173;  // <Op Mult> ::= <Op Unary>
//		final int PROD_OPUNARY_EXCLAM                               = 174;  // <Op Unary> ::= '!' <Op Unary>
//		final int PROD_OPUNARY_TILDE                                = 175;  // <Op Unary> ::= '~' <Op Unary>
//		final int PROD_OPUNARY_MINUS                                = 176;  // <Op Unary> ::= '-' <Op Unary>
//		final int PROD_OPUNARY_TIMES                                = 177;  // <Op Unary> ::= '*' <Op Unary>
//		final int PROD_OPUNARY_AMP                                  = 178;  // <Op Unary> ::= '&' <Op Unary>
		final int PROD_OPUNARY_PLUSPLUS                             = 179;  // <Op Unary> ::= '++' <Op Unary>
		final int PROD_OPUNARY_MINUSMINUS                           = 180;  // <Op Unary> ::= '--' <Op Unary>
		final int PROD_OPUNARY_PLUSPLUS2                            = 181;  // <Op Unary> ::= <Op Pointer> '++'
		final int PROD_OPUNARY_MINUSMINUS2                          = 182;  // <Op Unary> ::= <Op Pointer> '--'
//		final int PROD_OPUNARY_LPAREN_RPAREN                        = 183;  // <Op Unary> ::= '(' <ConstType> ')' <Op Unary>
//		final int PROD_OPUNARY_SIZEOF_LPAREN_RPAREN                 = 184;  // <Op Unary> ::= sizeof '(' <ConstType> ')'
//		final int PROD_OPUNARY_SIZEOF_LPAREN_RPAREN2                = 185;  // <Op Unary> ::= sizeof '(' <Pointers> <Op Pointer> ')'
//		final int PROD_OPUNARY                                      = 186;  // <Op Unary> ::= <Op Pointer>
//		final int PROD_OPPOINTER_DOT                                = 187;  // <Op Pointer> ::= <Op Pointer> '.' <Call Id>
//		final int PROD_OPPOINTER_MINUSGT                            = 188;  // <Op Pointer> ::= <Op Pointer> '->' <Call Id>
//		final int PROD_OPPOINTER_LBRACKET_RBRACKET                  = 189;  // <Op Pointer> ::= <Op Pointer> '[' <Expr> ']'
//		final int PROD_OPPOINTER                                    = 190;  // <Op Pointer> ::= <Value>
		final int PROD_CALLID_ID_LPAREN_RPAREN                      = 191;  // <Call Id> ::= Id '(' <Expr> ')'
		final int PROD_CALLID_ID_LPAREN_RPAREN2                     = 192;  // <Call Id> ::= Id '(' ')'
//		final int PROD_CALLID_ID                                    = 193;  // <Call Id> ::= Id
//		final int PROD_VALUE_OCTLITERAL                             = 194;  // <Value> ::= OctLiteral
//		final int PROD_VALUE_HEXLITERAL                             = 195;  // <Value> ::= HexLiteral
//		final int PROD_VALUE_DECLITERAL                             = 196;  // <Value> ::= DecLiteral
//		final int PROD_VALUE_STRINGLITERAL                          = 197;  // <Value> ::= StringLiteral
//		final int PROD_VALUE_CHARLITERAL                            = 198;  // <Value> ::= CharLiteral
//		final int PROD_VALUE_FLOATLITERAL                           = 199;  // <Value> ::= FloatLiteral
//		final int PROD_VALUE                                        = 200;  // <Value> ::= <Call Id>
//		final int PROD_VALUE_LPAREN_RPAREN                          = 201;  // <Value> ::= '(' <Expr> ')'
	};

	//---------------------------- Local Definitions ---------------------

	private static enum PreprocState {TEXT, TYPEDEF, STRUCT_UNION_ENUM, STRUCT_UNION_ENUM_ID, COMPLIST, /*ENUMLIST, STRUCTLIST,*/ TYPEID};
	private StringList typedefs = new StringList();
	// START KGU#388 2017-09-30: Enh. #423 counter for anonymous types
	private int typeCount = 0;
	// END KGU#388 2017-09-30
	
	// START KGU#376 2017-07-01: Enh. #389 - modified mechanism
	// Roots having induced global definitions, which will have to be renamed as soon as the name gets known
	//private LinkedList<Call> provisionalImportCalls = new LinkedList<Call>();
	private LinkedList<Root> importingRoots = new LinkedList<Root>();
	// END KGU#376 2017-07-01

	//------------------------------ Constructor -----------------------------
	
	/**
	 * Constructs a parser for language ANSI-C, loads the grammar as resource and
	 * specifies whether to generate the parse tree as string
	 */
	public CParser() {

	}
	
	private String ParserEncoding;
	private String ParserPath;
	
	private	final String[][] typeReplacements = new String[][] {
		{"size_t", "unsigned long"},
		{"time_t", "unsigned long"},
		// FIXME to be made configurable
		{"cob_u8_t", "unsigned int"}
	};
	
	static HashMap<String, String[]> defines = new LinkedHashMap<String, String[]>();

	private final static Pattern PTRN_VOID_CAST = Pattern.compile("(^\\s*|.*?[^\\w\\s]+\\s*)\\(\\s*void\\s*\\)(.*?)");
	private static Matcher mtchVoidCast = PTRN_VOID_CAST.matcher("");
	// START KGU#519 2018-06-17: Enh. #541
	// macro signature:  macroname ( 3 )
	private static final Pattern PTRN_MACRO_SIG = Pattern.compile("(\\w+)\\(\\s*([0-9]*)\\s*\\)");
	private static Matcher mtchMacroSig = PTRN_MACRO_SIG.matcher("");
	// END KGU#519 2018-06-17

	//----------------------------- Preprocessor -----------------------------

	/**
	 * Performs some necessary preprocessing for the text file. Actually opens the
	 * file, filters it and writes a new temporary file "Structorizer&lt;randomstring&gt;.c"
	 * or "Structorizer&lt;randomstring&gt;.h", which is then actually parsed.
	 * For the C Parser e.g. the preprocessor directives must be removed and possibly
	 * be executed (at least the defines. with #if it would get difficult).
	 * The preprocessed file will always be saved with UTF-8 encoding.
	 * @param _textToParse - name (path) of the source file
	 * @param _encoding - the expected encoding of the source file.
	 * @return The File object associated with the preprocessed source file.
	 */
	@Override
	protected File prepareTextfile(String _textToParse, String _encoding)
	{	
		this.ParserPath = null; // set after file object creation
		this.ParserEncoding	= _encoding;
		
		// START KGU#519 2018-06-17: Enh. 541 Empty the defines before general start
		defines.clear();
		// END KGU#51 2018-06-17
		
		//========================================================================!!!
		// Now introduced as plugin-defined option configuration for C
		//this.setPluginOption("typeNames", "cob_field,cob_u8_ptr,cob_call_union");
		//                  +"cob_content,cob_pic_symbol,cob_field_attr");
		//========================================================================!!!
		
		boolean parsed = false;
		
		StringBuilder srcCodeSB = new StringBuilder();
		parsed = processSourceFile(_textToParse, srcCodeSB);

		File interm = null;
		if (parsed) {
			try {
//				for (Entry<String, String> entry: defines.entrySet()) {
////					if (logFile != null) {
////						logFile.write("CParser.prepareTextfile(): " + Matcher.quoteReplacement((String)entry.getValue()) + "\n");
////					}
//					srcCode = srcCode.replaceAll("(.*?\\W)" + entry.getKey() + "(\\W.*?)", "$1"+ Matcher.quoteReplacement((String)entry.getValue()) + "$2");
//				}
			
				// Now we try to replace all type names introduced by typedef declarations
				// because the grammar doesn't cope with user-defined type ids.
				String srcCode = this.prepareTypedefs(srcCodeSB.toString(), _textToParse);
//				System.out.println(srcCode);
				
				// trim and save as new file
				interm = File.createTempFile("Structorizer", "." + getFileExtensions()[0]);
				OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(interm), "UTF-8");
				ow.write(srcCode.trim()+"\n");
				//System.out.println("==> "+filterNonAscii(srcCode.trim()+"\n"));
				ow.close();
			}
			catch (Exception e) 
			{
				System.err.println("CParser.prepareTextfile() creation of intermediate file -> " + e.getMessage());
			}
		}
		return interm;
	}

	/**
	 * Performs some necessary preprocessing for the text file. Actually opens the
	 * file, filters it places its contents into the given StringBuilder (if set).
	 * For the C Parser e.g. the preprocessor directives must be removed and possibly
	 * be executed (at least the defines. with #if it would get difficult).
	 * @param _textToParse - name (path) of the source file
	 * @param srcCodeSB - optional: StringBuilder to store the content of the preprocessing<br/>
	 * if not given only the preprocessor handling (including #defines) will be done  
	 * @return info if the preprocessing worked
	 */
	private boolean processSourceFile(String _textToParse, StringBuilder srcCodeSB) {
		
		try
		{
			File file = new File(_textToParse);
			if (this.ParserPath == null) {
				this.ParserPath = file.getAbsoluteFile().getParent() + File.separatorChar;
			}
			DataInputStream in = new DataInputStream(new FileInputStream(file));
			// START KGU#193 2016-05-04
			BufferedReader br = new BufferedReader(new InputStreamReader(in, this.ParserEncoding));
			// END KGU#193 2016-05-04
			String strLine;
			boolean inComment = false;

			// START KGU#519 2018-06-17: Enh. #541
			registerRedundantDefines(defines);
			// END KGU#519 2018-06-17
			
			//Read File Line By Line
			// Preprocessor directives are not tolerated by the grammar, so drop them or try to
			// do the #define replacements (at least roughly...) which we do
			while ((strLine = br.readLine()) != null)
			{
				String trimmedLine = strLine.trim();
				if (trimmedLine.isEmpty()) {
					if (srcCodeSB != null) {
						// no processing if we only want to check for defines
						srcCodeSB.append("\n");
					}
					continue;
				}
				
				// the grammar doesn't know about continuation markers,
				// concatenate the lines here
				if (strLine.endsWith("\\")) {
					String newlines = "";
					strLine = strLine.substring(0, strLine.length() - 1);
					String otherline = "";
					while ((otherline = br.readLine()) != null) {
						newlines += "\n";
						if (otherline.endsWith("\\")) {
							strLine += otherline.substring(0, otherline.length() - 1);
						} else {
							strLine += otherline;
							break;
						}
					}
					trimmedLine = strLine.trim();
					// add line breaks for better line counter,
					// works but looks strange in the case of errors when
					// the "preceding context" consist of 8 empty lines
					strLine += newlines;
				}
				
				// check if we are in a comment block, in this case look for the end
				boolean commentsChecked = false;
				String commentFree = "";
				String lineTail = trimmedLine;
				while (!lineTail.isEmpty() && !commentsChecked) {
					if (inComment) {
						// check if the line ends the current comment block
						int commentPos = lineTail.indexOf("*/");
						if (commentPos >= 0) {
							inComment = false;
							commentPos += 2;
							if (lineTail.length() > commentPos) {
								lineTail = " " + lineTail.substring(commentPos).trim();
							} else {
								lineTail = "";
							}
						}
						else {
							commentsChecked = true;
							lineTail = "";
						}
					}

					if (!inComment && !lineTail.isEmpty()) {
						// remove inline comments
						int commentPos = lineTail.indexOf("//");
						if (commentPos > 0) {
							lineTail = lineTail.substring(0, commentPos).trim(); 
						}
						// check if the line starts a new comment block
						commentPos = lineTail.indexOf("/*");
						if (commentPos >= 0) {
							inComment = true;
							if (commentPos > 0) {
								commentFree += " " + lineTail.substring(0, commentPos).trim();
							}
							commentPos += 2;
							if (lineTail.length() > commentPos) {
								lineTail = lineTail.substring(commentPos);
							} else {
								lineTail = "";
							}
						}
						else {
							commentsChecked = true;
						}
					}

				}
				trimmedLine = (commentFree + lineTail).trim();

				// Note: trimmedLine can be empty if we start a block comment only
				if (trimmedLine.isEmpty()) {
					if (srcCodeSB != null) {
						// no processing if we only want to check for defines
						srcCodeSB.append(strLine);
						srcCodeSB.append("\n");
					}
					continue;
				}
				// FIXME: try to take care for #if/#else/#endif, maybe depending upon an import setting
				//        likely only useful if we parse includes...
				//        and/or add a standard (dialect specific) list of defines
				if (trimmedLine.charAt(0) == '#') {
					if (srcCodeSB == null) {
						handlePreprocessorLine(trimmedLine.substring(1), defines);
						// no further processing if we only want to check for defines
						continue;
					}
					srcCodeSB.append(handlePreprocessorLine(trimmedLine.substring(1), defines));
					srcCodeSB.append(strLine);
				} else {
					if (srcCodeSB == null) {
						// no further processing if we only want to check for defines
						continue;
					}
					strLine = replaceDefinedEntries(strLine, defines);
					// The grammar doesn't cope with customer-defined type names nor library-defined ones, so we will have to
					// replace as many as possible of them in advance.
					// We cannot guess however, what's included since include files won't be available for us.
					for (String[] pair: typeReplacements) {
						String search = "(^|.*?\\W)"+Pattern.quote(pair[0])+"(\\W.*?|$)";
						if (strLine.matches(search)) {
							strLine = strLine.replaceAll(search, "$1" + pair[1] + "$2");
						}
					}
					mtchVoidCast.reset(strLine);
					if (mtchVoidCast.matches()) {
						strLine = mtchVoidCast.group(1) + mtchVoidCast.group(2);	// checkme
					}
					srcCodeSB.append(strLine);
				}
				srcCodeSB.append("\n");
			}
			//Close the input stream
			in.close();
			return true;
		}
		catch (Exception e) 
		{
			if (srcCodeSB != null) {
				System.err.println("CParser.processSourcefile() -> " + e.getMessage());
			}
			return false;
		}
	}

	// START KGU#519 2018-06-17: Enh. #541
	/**
	 * Analyses the import option "redundantNames" and derives dummy defines from
	 * them that are suited to eliminate the respective texts from the code. 
	 */
	private void registerRedundantDefines(HashMap<String, String[]> defines) {
		String namesToBeIgnored = (String)this.getPluginOption("redundantNames", null);
		if (namesToBeIgnored != null) {
			String[] macros = namesToBeIgnored.split(",");
			for (String macro: macros) {
				macro = macro.trim();
				if (mtchMacroSig.reset(macro).matches()) {
					String macroName = mtchMacroSig.group(1).trim();
					String countStr = mtchMacroSig.group(2).trim();
					int count = 1;
					try {
						if (!countStr.isEmpty()) {
							count = Integer.parseInt(countStr);
						}
						String[] pseudoArgs = new String[count+1];
						pseudoArgs[0] = "";
						for (int i = 0; i < count; i++) {
							pseudoArgs[i+1] = "arg" + i;
						}
						defines.put(macroName, pseudoArgs);
						log("Prepared to eliminate redundant macro \"" + macro + "\"\n", false);
					}
					catch (NumberFormatException ex) {
						log("*** Illegal redundant macro specification: " + macro + "\n", true);
					}
				}
				else {
					defines.put(macro, new String[]{""});
					log("Prepared to eliminate redundant symbol \"" + macro + "\"\n", false);
				}
			}
		}
	}
	// END KGU#519 2018-06-17

	// Patterns and Matchers for preprocessing
	// (reusable, otherwise these get created and compiled over and over again)
	// #define	a	b
	private static final Pattern PTRN_DEFINE = Pattern.compile("^define\\s+(\\w*)\\s+(\\S.*?)");
	// #define	a	// empty
	private static final Pattern PTRN_DEFINE_EMPTY = Pattern.compile("^define\\s+(\\w*)\\s*");
	// #define	a(b)	functionname (int b)
	// #define	a(b,c,d)	functionname (int b, char *d)	// multiple ones, some may be omitted
	// #define	a(b)	// empty
	private static final Pattern PTRN_DEFINE_FUNC = Pattern.compile("^define\\s+(\\w+)\\s*\\(([^)]+)\\)\\s+(.*)");
	// #undef	a
	private static final Pattern PTRN_UNDEF = Pattern.compile("^undef\\s+(\\w+)(.*)");
	// #undef	a
	private static final Pattern PTRN_INCLUDE = Pattern.compile("^include\\s+[<\"]([^>\"]+)[>\"]");
	// several things we can ignore: #pragma, #warning, #error, #message 
	private static final Pattern PTRN_IGNORE = Pattern.compile("^(?>pragma)|(?>warning)|(?>error)|(?>message)");
	
	private static Matcher mtchDefine = PTRN_DEFINE.matcher("");
	private static Matcher mtchDefineEmpty = PTRN_DEFINE_EMPTY.matcher("");
	private static Matcher mtchDefineFunc = PTRN_DEFINE_FUNC.matcher("");
	private static Matcher mtchUndef = PTRN_UNDEF.matcher("");
	private static Matcher mtchInclude = PTRN_INCLUDE.matcher("");
	private static Matcher mtchIgnore = PTRN_IGNORE.matcher("");

	// Patterns and Matchers for parsing / building
	// detection of a const modifier in a declaration
	private static final Pattern PTRN_CONST = Pattern.compile("(^|.*?\\s+)const(\\s+.*?|$)");

	private static Matcher mtchConst = PTRN_CONST.matcher("");

	/**
	 * Helper function for prepareTextfile to handle C preprocessor commands
	 * @param preprocessorLine	line for the preprocessor without leading '#'
	 * @param defines 
	 * @return comment string that can be used for prefixing the original source line
	 */
	private String handlePreprocessorLine(String preprocessorLine, HashMap<String, String[]> defines)
	{
		mtchDefineFunc.reset(preprocessorLine);
		if (mtchDefineFunc.matches()) {
			// #define	a1(a2,a3,a4)	stuff  ( a2 ) 
			//          1  >  2   <		>     3     <
			String symbol = mtchDefineFunc.group(1);
			String[] params = mtchDefineFunc.group(2).split(",");
			String subst = mtchDefineFunc.group(3);
			String substTab[] = new String[params.length + 1];
			substTab[0] = replaceDefinedEntries(subst, defines).trim();
			for (int i = 0; i < params.length; i++) {
				substTab[i+1] = params[i].trim();
			}
			defines.put(symbol, substTab);
			return "// preparser define (function): ";
		}

		mtchDefine.reset(preprocessorLine);
		if (mtchDefine.matches()) {
			// #define	a	b
			//          1	2
			String symbol = mtchDefine.group(1);
			String subst[] = new String[1];
			subst[0] = mtchDefine.group(2);
			subst[0] = replaceDefinedEntries(subst[0], defines).trim();
			defines.put(symbol, subst);
			return "// preparser define: ";
		}
		
		mtchUndef.reset(preprocessorLine);
		if (mtchUndef.matches()) {
			// #undef	a
			String symbol = mtchUndef.group(1);
			defines.remove(symbol);
			return "// preparser undef: ";
		}

		mtchDefineEmpty.reset(preprocessorLine);
		if (mtchDefineEmpty.matches()) {
			// #define	a
			String symbol = mtchDefineEmpty.group(1);
			String subst[] = new String[]{""};
			defines.put(symbol, subst);
			return "// preparser define: ";
		}

		mtchInclude.reset(preprocessorLine);
		if (mtchInclude.matches()) {
			// #include	"header"
			// FIXME: *MAYBE* store list of non-system includes to parse as IMPORT diagram upon request?
			String incName = mtchInclude.group(1);
			// do internal preparsing for resolving define/struct/typedef for the imported file
			// FIXME: maybe do only when set as preparser option
			// FIXME: add option to use more than the main file's path as preparser option
			if (File.separatorChar == '\\') {
				// FIXME (KGU) This doesn't seem so good an idea - usually both systems cope with '/' 
				incName = incName.replaceAll("/", "\\\\");
			} else {
				incName = incName.replaceAll("\\\\", File.separator);
			}
			
			if (processSourceFile(this.ParserPath + incName, null)) {
				return "// preparser include (parsed): ";
			} else {
				return "// preparser include (failed): ";
			}
		}

		mtchIgnore.reset(preprocessorLine);
		if (mtchIgnore.find()) {
			// #pragma, #error, #warning, #message ...
			return "// preparser instruction (ignored): ";
		}

		// #if, #ifdef, #ifndef, #else, #elif, #endif, ...
		return "// preparser instruction (not parsed!): ";
	}

	/**
	 * Detects typedef declarations in the {@code srcCode}, identifies the defined type names and replaces
	 * them throughout their definition scopes text with generic names "user_type_###" defined in the grammar
	 * such that the parse won't fail. The type name map is represented by the static variable {@link #typedefs}
	 * where the ith entry is mapped to a type id "user_type_&lt;i+1&gt;" for later backwards replacement.
	 * @param srcCode - the pre-processed source code as long string
	 * @param _textToParse - the original file name
	 * @return the source code with replaced type names
	 * @throws IOException
	 */
	private String prepareTypedefs(String srcCode, String _textToParse) throws IOException
	{
		// In a first step we gather all type names defined via typedef in a
		// StringList mapping them by their index to generic type ids being
		// defined in the grammar ("user_type_###"). It will be a rudimentary parsing
		// i.e. we don't consider anything except typedef declarations and we expect
		// a syntactically correct construct. If something strange occurs then we just
		// ignore the text until we bump into another typedef keyword.
		// In the second step we replace all identifiers occurring in the map with
		// their associated generic name, respecting the definition scope.
		
		typedefs.clear();

		Vector<Integer[]> blockRanges = new Vector<Integer[]>();
		LinkedList<String> typedefDecomposers = new LinkedList<String>();
		
		// START KGU 2017-05-26: workaround for the typeId deficiency of the grammar: allow configured global typenames
		String configuredTypeNames = (String)this.getPluginOption("typeNames", null);
		if (configuredTypeNames != null) {
			String[] typeIds = configuredTypeNames.split("(,| )");
			for (int i = 0; i < typeIds.length; i++) {
				String typeId = typeIds[i].trim();
				if (typeId.matches("^\\w+$")) {
					typedefs.add(typeId);
					blockRanges.addElement(new Integer[]{0, -1});
				}
			}
		}
		// END KGU 2017-05-26
			
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
		// Underscore must be added to word characters!
		tokenizer.wordChars('_', '_');
		
		// A regular search pattern to find and decompose type definitions with both
		// struct/union/enum id and type id like in:
		// typedef struct structId {...} typeId [, ...];
		// (This is something the used grammar doesn't cope with and so it is to be 
		// decomposed as follows for the example above:
		// struct structId {...};
		// typedef struct structId typeId [, ...];
		String typedefStructPattern = "";
		
		while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
			String word = null;
			log("[" + tokenizer.lineno() + "]: ", false);
			switch (tokenizer.ttype) {
			case StreamTokenizer.TT_EOL:
				log("**newline**\n", false);
				if (!typedefStructPattern.isEmpty()) {
					typedefStructPattern += ".*?\\v";
				}
				break;
			case StreamTokenizer.TT_NUMBER:
				log("number: " + tokenizer.nval + "\n", false);
				if (!typedefStructPattern.isEmpty()) {
					// NOTE: a non-integral number literal is rather unlikely within a type definition...
					typedefStructPattern += "\\W+[+-]?[0-9]+";
				}
				break;
			case StreamTokenizer.TT_WORD:
				word = tokenizer.sval;
				log("word: " + word + "\n", false);
				if (state == PreprocState.TYPEDEF) {
					if (word.equals("enum") || word.equals("struct") || word.equals("union")) {
						state = PreprocState.STRUCT_UNION_ENUM;
						typedefStructPattern = "typedef\\s+(" + word;
					}
					else {
						lastId = word;	// Might be the defined type id if no identifier will follow
						typedefStructPattern = "";	// ...but it's definitely no combined struct/type definition
					}
				}
				else if (state == PreprocState.TYPEID && indexDepth == 0) {
					typedefs.add(word);
					blockRanges.add(new Integer[]{tokenizer.lineno()+1, (blockStarts.isEmpty() ? -1 : blockStarts.peek())});
					if (!typedefStructPattern.isEmpty()) {
						if (typedefStructPattern.matches(".*?\\W")) {
							typedefStructPattern += "\\s*" + word;
						}
						else {
							typedefStructPattern += "\\s+" + word;
						}
					}
				}
				// START KGU 2017-05-23: Bugfix - declarations like "typedef struct structId typeId"
				else if (state == PreprocState.STRUCT_UNION_ENUM) {
					state = PreprocState.STRUCT_UNION_ENUM_ID;
					// This must be the struct/union/enum id.
					typedefStructPattern += "\\s+" + word + ")\\s*(";	// named struct/union/enum: add its id and switch to next group
				}
				else if (state == PreprocState.STRUCT_UNION_ENUM_ID) {
					// We have read the struct/union/enum id already, so this must be the first type id.
					typedefs.add(word);
					blockRanges.add(new Integer[]{tokenizer.lineno()+1, (blockStarts.isEmpty() ? -1 : blockStarts.peek())});
					typedefStructPattern = "";	// ... but it's definitely no combined struct and type definition
					state = PreprocState.TYPEID;
				}
				// END KGU 2017-05-23
				else if (word.equals("typedef")) {
					typedefLevel = blockStarts.size();
					state = PreprocState.TYPEDEF;
				}
				else if (state == PreprocState.COMPLIST && !typedefStructPattern.isEmpty()) {
					if (typedefStructPattern.matches(".*\\w") && !typedefStructPattern.endsWith("\\v")) {
						typedefStructPattern += "\\s+";
					}
					else if (typedefStructPattern.endsWith(",") || typedefStructPattern.endsWith(";")) {
						// these are typical positions for comments...
						typedefStructPattern += ".*?";
					}
					else {
						typedefStructPattern += "\\s*";
					}
					typedefStructPattern += word;
				}
				break;
			case '\'':
				log("character: '" + tokenizer.sval + "'\n", false);
				if (!typedefStructPattern.isEmpty()) {
					typedefStructPattern += Pattern.quote("'"+tokenizer.sval+"'");	// We hope that there are no parentheses inserted
				}
				break;
			case '"':
				log("string: \"" + tokenizer.sval + "\"\n", false);
				if (!typedefStructPattern.isEmpty()) {
					typedefStructPattern += Pattern.quote("\""+tokenizer.sval+"\"");	// We hope that there are no parentheses inserted
				}
				break;
			case '{':
				blockStarts.add(tokenizer.lineno());
				if (state == PreprocState.STRUCT_UNION_ENUM || state == PreprocState.STRUCT_UNION_ENUM_ID) {
					if (state == PreprocState.STRUCT_UNION_ENUM) {
						typedefStructPattern = ""; 	// We don't need a decomposition
					}
					else {
						typedefStructPattern += "\\s*\\{";
					}
					state = PreprocState.COMPLIST;
				}
				parenthStack.push('}');
				break;
			case '(':
				if (!typedefStructPattern.isEmpty()) {
					typedefStructPattern += "\\s*\\(";
				}
				parenthStack.push(')');
				break;
			case '[':	// FIXME: Handle index lists in typedefs!
				if (!typedefStructPattern.isEmpty()) {
					typedefStructPattern += "\\s*\\[";
				}
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
					if (!typedefStructPattern.isEmpty()) {
						typedefStructPattern += "\\s*\\})\\s*(";	// .. therefore open the next group
					}
					state = PreprocState.TYPEID;
				}
			case ')':
			case ']':	// Handle index lists in typedef!s
				{
					if (parenthStack.isEmpty() || tokenizer.ttype != (expected = parenthStack.pop().charValue())) {
						String errText = "**FILE PREPARATION TROUBLE** in line " + tokenizer.lineno()
						+ " of file \"" + _textToParse + "\": unmatched '" + (char)tokenizer.ttype
						+ "' (expected: '" + (expected == '\0' ? '\u25a0' : expected) + "')!";
						System.err.println(errText);
						log(errText, false);
					}
					else if (tokenizer.ttype == ']' && state == PreprocState.TYPEID) {
						indexDepth--;
						if (!typedefStructPattern.isEmpty()) {
							typedefStructPattern += "\\s*\\" + (char)tokenizer.ttype;
						}
					}
				}
					break;
			case '*':
				if (state == PreprocState.TYPEDEF) {
					state = PreprocState.TYPEID;
				}
				else if (state == PreprocState.STRUCT_UNION_ENUM_ID) {
					typedefStructPattern = "";	// Cannot be a combined definition: '*' follows immediately to the struct id
				}
				else if (!typedefStructPattern.isEmpty()) {
					typedefStructPattern += "\\s*[*]";
				}
				break;
			case ',':
				if (state == PreprocState.TYPEDEF && lastId != null) {
					typedefs.add(lastId);
					blockRanges.add(new Integer[]{tokenizer.lineno()+1, (blockStarts.isEmpty() ? -1 : blockStarts.peek())});
					if (!typedefStructPattern.isEmpty()) {
						// Type name won't be replaced within the typedef clause
						//typedefStructPattern += "\\s+" + String.format(USER_TYPE_ID_MASK, typedefs.count()) + "\\s*,";
						typedefStructPattern += "\\s+" + lastId + "\\s*,";
					}
					state = PreprocState.TYPEID;
				}
				else if (state == PreprocState.TYPEID) {
					if (!typedefStructPattern.isEmpty()) {
						typedefStructPattern += "\\s*,";
					}
				}
				break;
			case ';':
				if (state == PreprocState.TYPEDEF && lastId != null) {
					typedefs.add(lastId);
					blockRanges.add(new Integer[]{tokenizer.lineno()+1, (blockStarts.isEmpty() ? -1 : blockStarts.peek())});
					typedefStructPattern = "";
					state = PreprocState.TEXT;
				}
				else if (state == PreprocState.TYPEID) {
					if (!typedefStructPattern.isEmpty() && !typedefStructPattern.endsWith("(")) {
						typedefStructPattern += ")\\s*;";
						typedefDecomposers.add(typedefStructPattern);
					}
					typedefStructPattern = "";
					state = PreprocState.TEXT;						
				}
				else if (state == PreprocState.COMPLIST && !typedefStructPattern.isEmpty()) {
					typedefStructPattern += "\\s*;";
				}
				break;
			default:
				char tokenChar = (char)tokenizer.ttype;
				if (state == PreprocState.COMPLIST && !typedefStructPattern.isEmpty()) {
					typedefStructPattern += "\\s*" + Pattern.quote(tokenChar + "");
				}
				log("other: " + tokenChar + "\n", false);
			}
			log("", false);
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
			String subst = String.format(USER_TYPE_ID_MASK, i+1);
			this.replacedIds.put(subst, typeName);
			subst = "$1" + subst + "$3";
			for (int j = range[0]; j <= range[1]; j++) {
				if (srcLines.get(j).matches(pattern)) {
					srcLines.set(j, srcLines.get(j).replaceAll(pattern, subst));
				}
			}
		}
		srcCode = srcLines.concatenate("\n");
		
		// Now we try the impossible: to decompose compound struct/union/enum and type name definition
		for (String pattern: typedefDecomposers) {
			srcCode = srcCode.replaceAll(".*?" + pattern + ".*?", TYPEDEF_DECOMP_REPLACER);
		}

		return srcCode;
	}

	private String replaceDefinedEntries(String toReplace, HashMap<String, String[]> defines) {
		// START KGU#519 2018-06-17: Enh. #541 - The matching tends to fail if toReplace ends with newline characters
		// (The trailing newlines were appended on concatenating lines broken by end-standing backslashes to preserve line counting.)
		//if (toReplace.trim().isEmpty()) {
		//	return "";
		//}
		String nlTail = "";
		int nlPos = toReplace.indexOf('\n');
		if (nlPos >= 0) {
			nlTail = toReplace.substring(nlPos);	// Ought to contain the tail of \n characters
			toReplace = toReplace.substring(0, nlPos);
		}
		if (toReplace.trim().isEmpty()) {
			return nlTail;	// Preserve line count...
		}
		// END KGU#519 2018-06-17
		//log("CParser.replaceDefinedEntries(): " + Matcher.quoteReplacement((String)entry.getValue().toString()) + "\n", false);
		for (Entry<String, String[]> entry: defines.entrySet()) {
			
			// FIXME: doesn't work if entry is at start/end of toReplace 
			
			
			if (entry.getValue().length > 1) {
				//          key<val[0]>     <   val[1]   >
				// #define	a1(a2,a3,a4)	stuff (  a2  )
				// key  ( text1, text2, text3 )	--->	stuff (  text1  )
				// #define	a1(a2,a3,a4)
				// key  ( text1, text2, text3 )	--->
				// #define	a1(a2,a3,a4)	a2
				// key  ( text1, text2, text3 )	--->	text1
				// #define	a1(a2,a3,a4)	some text
				// key  ( text1, text2, text3 )	--->	some text
				/* FIXME: 
				 * The trouble here is that text1, text2 etc. might also contain parentheses, so may the following text.
				 * The result of the replacement would then be a total desaster
				 */
				Matcher matcher = Pattern.compile("(^|.*?\\W)" + entry.getKey() + "(\\s*)\\((.*)\\)(.*?)").matcher("");
				//while (toReplace.matches("(^|.*?\\W)" + entry.getKey() + "\\s*\\(.*\\).*?")) {
				while (matcher.reset(toReplace).matches()) {
					if (entry.getValue()[0].isEmpty()) {
						//toReplace = toReplace.replaceAll("(^|.*?\\W)" + entry.getKey() + "(\\s*)\\((.*)\\)(.*?)", "$1$2$4");
						toReplace = matcher.replaceAll("$1$2$4");
					} else {
						// The greedy quantifier inside the parentheses ensures that we get to the rightmost closing parenthesis
						//String argsRaw = toReplace.replaceFirst("(^|.*?\\W)" + entry.getKey() + "(\\s*)\\((.*)\\)(.*)", "$3");
						String argsRaw = matcher.group(3);
						// Now we split the balanced substring (up to the first unexpected closing parenthesis) syntactically
						// (The unmatched tail of argsRaw will be re-appended later)
						StringList args = Element.splitExpressionList(argsRaw, ",");
						// We test whether argument and parameter count match
						if (args.count() != entry.getValue().length - 1) {
							// FIXME: function-like define doesn't match arg count
							log("CParser.replaceDefinedEntries() cannot apply function macro\n\t"
									// START KGU#522 2018-06-17: Bugfix #540 reconstruction of the macro
									//+ entry.getKey() + entry.getValue().toString() + "\n\tdue to arg count diffs:\n\t"
									+ entry.getKey() + "(" + (new StringList(entry.getValue())).concatenate(", ", 1) + ")"
									+ "\n\tdue to arg count diffs:\n\t"
									// END KGU#522 2018-06-07							
									+ toReplace + "\n", true);
							// START KGU#522 2018-06-17: Bugfix #540 (emergency exit from a threatening eternal loop)
							break;
							// END KGU#522 2018-06-07
						}
						else {
							HashMap<String, String> argMap = new HashMap<String, String>();
							// Lest the substitutions should interfere with one another we first split the string for all parameters
							// KGU#522: But not this way!
							StringList parts = StringList.getNew(entry.getValue()[0]);
							for (int i = 0; i < args.count(); i++) {
								String param = entry.getValue()[i+1];
								argMap.put(param, args.get(i));
								parts = StringList.explodeWithDelimiter(parts, param);
								// START KGU#522 2018-06-17: Bugfix #540 - we must recompose identifiers
								parts.removeAll("");
								int pos = -1;
								while ((pos = parts.indexOf(param, pos+1)) >= 0) {
									if (pos > 0 && parts.get(pos-1).matches(".*?\\w")) {
										parts.set(pos-1, parts.get(pos-1)+param);
										parts.remove(pos--);
									}
									if (pos+1 < parts.count() && parts.get(pos+1).matches("\\w.*?")) {
										parts.set(pos, parts.get(pos) + parts.get(pos+1));
										parts.remove(pos+1);
									}
								}
								// END KGU#522 2018-06-17
							}
							// Now we have all parts separated and walk through the StringList, substituting the parameter names
							for (int i = 0; i < parts.count(); i++) {
								String part = parts.get(i);
								if (!part.isEmpty() && argMap.containsKey(part)) {
									parts.set(i, argMap.get(part));
								}
							}
							// Now we correct possible matching defects
							StringList argsPlusTail = Element.splitExpressionList(argsRaw, ",", true);
							if (argsPlusTail.count() > args.count()) {
								String tail = argsPlusTail.get(args.count()).trim();
								// With high probability tail stars with a closing parenthesis, which has to be dropped if so
								// whereas the consumed parenthesis at the end has to be restored.
								if (tail.startsWith(")")) {
									tail = tail.substring(1) + ")";
								}
								parts.add(tail);
							}
							toReplace = toReplace.replaceFirst("(^|.*?\\W)" + entry.getKey() + "(\\s*)\\((.*)\\)(.*)",
									"$1" + Matcher.quoteReplacement(parts.concatenate()) + "$4");
						}
					}
				}
			} else {
				// from: #define	a	b, b can also be empty
				toReplace = toReplace.replaceAll("(^|.*?\\W)" + entry.getKey() + "(\\W.*?)",
						"$1" + Matcher.quoteReplacement((String) entry.getValue()[0]) + "$2");
			}
		}
		// START KGU#519 2018-06-17: Enh. #541 - To preserve line counting, we restore the temporarily cropped newlines
		//return toReplace;
		return toReplace + nlTail;
		// END KGU#519 2018-06-17
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
		root.setProgram(false);	// C programs are functions, primarily
		this.optionUpperCaseProgName = Root.check(6);
		// START KGU#407 207-06-22: Enh. #420: Configure the lookup table for comment retrieval
		this.registerStatementRuleIds(statementIds);
		// END KGU#407 2017-06-11
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
			getLogger().log(Level.CONFIG, "Rule {0}, {1}", new Object[]{rule, _parentNode.parent});
			log("buildNSD_R(" + rule + ", " + _parentNode.parent + ")...\n", true);
			
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
				else {
					arguments = new StringList();
				}
				if (procName.equals("exit")) {
					content = getKeywordOrDefault("preExit", "exit");
					if (arguments.count() > 0) {
						content += arguments.get(0);
						// START KGU#407 2017-06-20: Enh. #420 - comments already here
						//_parentNode.addElement(new Jump(content));
						_parentNode.addElement(this.equipWithSourceComment(new Jump(content), _reduction));
						// END KGU#407 2017-06-22
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
					// START KGU#407 2017-06-20: Enh. #420 - comments already here
					//_parentNode.addElement(new Instruction(getContent_R(_reduction, content)));
					_parentNode.addElement(this.equipWithSourceComment(new Instruction(getContent_R(_reduction, content)), _reduction));
					// END KGU#407 2017-06-22
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
				// START KGU#407 2017-06-20: Enh. #420 - comments already here
				//_parentNode.addElement(new Instruction(translateContent(content)));
				_parentNode.addElement(this.equipWithSourceComment(new Instruction(translateContent(content)), _reduction));
				// END KGU#407 2017-06-22
			}
			else if (
					// Variable declaration with or without initialization?
					ruleName.equals("<Var Decl>")	// var declaration			
					)
			{
				// If declaration import is allowed then we make an instruction in
				// Pascal syntax out of it.
				Subqueue parentNode = _parentNode;
				// Just a container for the type to be returned
				StringList tmpTypes = new StringList();
				String rootName = root.getMethodName();
				boolean isGlobal = rootName.equals("???");
				if (isGlobal) {
					if (this.globalRoot == null) {
						this.globalRoot = root;
//								this.globalRoot.setText("global");
//								subRoots.add(this.globalRoot);
					}
					parentNode = this.globalRoot.children;
				}
				// START KGU#388 2017-09-30: Enh. #423
				boolean isStruct = processTypes(_reduction, ruleId, parentNode, isGlobal, tmpTypes, true);
				// END KGU#388 2017-09-30
				String type = tmpTypes.concatenate();
				// START KGU#407 2017-06-22: Enh.#420 grope for possible souce comments
				String comment = this.retrieveComment(_reduction);
				// END KGU#407 2017-06-22
				// Now concern on the first declaration of the list
				Reduction secReduc = _reduction.get(_reduction.size() - 3).asReduction();
				buildDeclOrAssignment(secReduc, type, parentNode, comment, isStruct);
				// START KGU#376 2017-09-30: Enh. #389
				// CHECKME!
				if (isGlobal && root != globalRoot && !importingRoots.contains(root)) {
					importingRoots.add(root);
				}
				// END KGU#376 2017-09-30
//				if (_reduction.size() > typeIx+2) {
					log("\tanalyzing <Var List> ...\n", false);
//					secReduc = _reduction.get(typeIx + 2).asReduction();	// <Var List>
					secReduc = _reduction.get(_reduction.size()-2).asReduction();	// <Var List>
					ruleId = secReduc.getParent().getTableIndex();
					while (ruleId == RuleConstants.PROD_VARLIST_COMMA) {
						Reduction thdReduc = secReduc.get(1).asReduction();	// <Var Item>
						// Get the pointers part
						String pointers = getContent_R(thdReduc.get(0).asReduction(), "");
						// Delegate the sub-reduction <Var>
						buildDeclOrAssignment(secReduc.get(1).asReduction(), type+pointers, parentNode, comment, isStruct);
						// Get the list tail
						secReduc = secReduc.get(2).asReduction();	// <Var List>
						ruleId = secReduc.getParent().getTableIndex();
					}
					log("\t<Var List> done.\n", false);
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
				// START KGU#407 2017-06-22: Enh. #420
				String comment = this.retrieveComment(_reduction);
				// END KGU#407 2017-06-22
				int insertAt = parentNode.getSize();
				// START KGU#388 2017-09-30: Enh. #423 Now struct types supported in Structorizer
				boolean isValid = false;
				if (ruleId == RuleConstants.PROD_STRUCTDECL_STRUCT_ID_LBRACE_RBRACE) {
					// <Struct Decl> ::= struct Id '{' <Struct Def> '}' <Decl End>
					String structName = _reduction.get(1).asString();
					// START KGU#517 2018-06-04: Bugfix #??? - we must analyse recursively and convert syntax!
					//String components = this.getContent_R(_reduction.get(3).asReduction(), "").replace("struct ", "").trim();
					//if (components.endsWith(";")) components = components.substring(0, components.length()-1);
					//content = "type " + structName + " = struct{" + components + "}";
					StringList components = getCompsFromStructDef(_reduction.get(3).asReduction());
					content = "type " + structName + " = struct{" + components.concatenate(";\\\n") + "}";
					// END KGU#517 2018-06-04
					isValid = true;
				}
				// END KGU#388 2017-09-30
				if (ruleId == RuleConstants.PROD_TYPEDEFDECL_TYPEDEF) {
					// <Typedef Decl> ::= typedef <Var Decl>
					StringList typeNames = new StringList();
					StringList typeRanges = new StringList();
					StringList typePointers = new StringList();
					Reduction varDecl = _reduction.get(1).asReduction();	// <Var Decl>
					int varRuleId = varDecl.getParent().getTableIndex(); 
					int varIx = 0;
					switch (varRuleId) {
					case RuleConstants.PROD_VARDECL_SEMI:
						// <Var Decl> ::= <ConstMod> <Type> <Var> <Var List> ';'
						varIx = 2;
						break;
					case RuleConstants.PROD_VARDECL_CONST_SEMI:
						// <Var Decl> ::= <Mod> const <Type> <Var> <Var List> ';'
						varIx = 3;
						break;
					default:
						// <Var Decl> ::= <ConstType> <Var> <Var List> ';'
						// <Var Decl> ::= <ConstMod> <Var> <Var List> ';'
						// <Var Decl> ::= const <Var> <Var List> ';'
						varIx = 1;
					}
					// <Var> ::= Id <Array> [ '=' <Initializer> ]
					String type = varDecl.get(varIx).asReduction().get(0).asString().trim();
					String range = this.getContent_R(varDecl.get(varIx).asReduction().get(1).asReduction(), "").trim();
					// We want a non-array type at the top
					boolean hadPlainId = false;
					typeNames.add(type);
					typePointers.add("");
					if (range.isEmpty()) {
						hadPlainId = true;
					}
					else { 
						range = "array " + range + " of ";
					}
					typeRanges.add(range);
					Reduction varRed = varDecl.get(varIx + 1).asReduction();	// <Var List>
					while (varRed.getParent().getTableIndex() == RuleConstants.PROD_VARLIST_COMMA) {
						// <Var List> ::= ',' <Var Item> <Var List>
						Reduction varItem = varRed.get(1).asReduction();
						// <Var Item> ::= <Pointers> <Var>
						String pointers = this.getContent_R(varItem.get(0).asReduction(), "").trim(); 
						type = varItem.get(1).asReduction().get(0).asString();
						range = this.getContent_R(varItem.get(1).asReduction().get(1).asReduction(), "").trim();
						if (range.isEmpty() && pointers.isEmpty() && !hadPlainId) {
							typeNames.insert(type, 0);
							typeRanges.insert("", 0);
							typePointers.insert("", 0);
							hadPlainId = true;
						}
						else {
							if (!range.isEmpty()) {
								range = "array " + range + " of ";
							}
							typeNames.add(type);
							typeRanges.add(range);
							typePointers.add(pointers);
						}
						
						varRed = varRed.get(2).asReduction();
					}
					StringList tmpTypes = new StringList();
					if (hadPlainId) {
						// The first type name may be used for an anonymous struct 
						tmpTypes = typeNames;
					}
					int nTypesBefore = tmpTypes.count();
					// If the number of types in tmpTypes doesn't increment then the first type was consumed already
					this.processTypes(varDecl, varRuleId, parentNode, isGlobal, tmpTypes, false);
					int diff = tmpTypes.count() - nTypesBefore;
					if (nTypesBefore == 0) {
						tmpTypes.add(typeNames);
					}
					content = "";
					String type0 = tmpTypes.get(0);
					for (int i = 1; i < tmpTypes.count(); i++) {
						content += (i > 0 ? "\n" : ":") + "type " + tmpTypes.get(i) + " = " + typeRanges.get(i-diff) + type0 + typePointers.get(i-diff);
					}
					insertAt = parentNode.getSize();
				}
				else {
					Reduction varDecl = _reduction.get(5).asReduction();
					// Does it contain variable declarations?
					if (varDecl.getParent().getTableIndex() == RuleConstants.PROD_DECLEND_SEMI2) {
						String type = content.substring(0, content.indexOf("{")).trim();
						this.buildDeclOrAssignment(varDecl.get(0).asReduction(), type, _parentNode, comment, false);
						varDecl = varDecl.get(1).asReduction();
						while (varDecl.getParent().getTableIndex() == RuleConstants.PROD_VARLIST_COMMA) {
							this.buildDeclOrAssignment(varDecl.get(1).asReduction(), type, _parentNode, comment, false);
							varDecl = varDecl.get(2).asReduction();
						}
						content = this.getContent_R(_reduction.get(3).asReduction(), type + " {") + "}";
					}
				}
				if (!content.trim().isEmpty()) {
					Instruction decl = new Instruction(StringList.explode(translateContent(content), "\n"));
					decl.disabled = !isValid;
					if (isGlobal) {
						decl.setColor(colorGlobal);
						// START KGU#376 2017-09-30: Enh. #389
						// CHECKME!
						if (root != globalRoot && !importingRoots.contains(root)) {
							importingRoots.add(root);
						}
						// END KGU#376 2017-09-30
					}
					parentNode.insertElementAt(this.equipWithSourceComment(decl, _reduction), insertAt);
				}
			}
			else if (
					// BREAK instruction
					ruleId == RuleConstants.PROD_NORMALSTM_BREAK_SEMI
					)
			{
				String content = getKeyword("preLeave");
				// START KGU#407 2017-06-20: Enh. #420 - comments already here
				//_parentNode.addElement(new Jump(content.trim()));								
				_parentNode.addElement(this.equipWithSourceComment(new Jump(content.trim()), _reduction));
				// END KGU#407 2017-06-22
			}
			else if (
					// RETURN instruction
					ruleId == RuleConstants.PROD_NORMALSTM_RETURN_SEMI
					// START KGU#523 2018-06-17: Bugfix #542
					||
					ruleId == RuleConstants.PROD_NORMALSTM_RETURN_SEMI2
					// END KGU#523 2018-06-17
					)
			{
				// START KGU#523 2018-06-17: Bugfix #542
				//String content = translateContent(getKeyword("preReturn") + 
				//		" " + getContent_R(_reduction.get(1).asReduction(), ""));
				String content = getKeyword("preReturn");
				if (ruleId == RuleConstants.PROD_NORMALSTM_RETURN_SEMI) { 
					content += " " + translateContent(getContent_R(_reduction.get(1).asReduction(), ""));
				}
				// END KGU#523 2018-06-17
				// START KGU#407 2017-06-20: Enh. #420 - comments already here
				//_parentNode.addElement(new Jump(content.trim()));								
				_parentNode.addElement(this.equipWithSourceComment(new Jump(content.trim()), _reduction));
				// END KGU#407 2017-06-22
			}
			else if (
					// GOTO instruction
					ruleId == RuleConstants.PROD_NORMALSTM_GOTO_ID_SEMI
					)
			{
				String content = _reduction.get(0).asString() + " " + _reduction.get(1).asString();
				Jump el = new Jump(content.trim());
				// START KGU#407 2017-06-20: Enh. #420 - comments already here
				//el.setComment("FIXME: Goto is not supported in structured algorithms!");
				this.equipWithSourceComment(el, _reduction);
				el.getComment().add("FIXME: Goto is not supported in structured algorithms!");
				// END KGU#407 2017-06-22
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
				// START KGU#407 2017-06-20: Enh. #420 - comments already here
				//el.setComment("FIXME: Goto instructions are not supported in structured algorithms!");
				this.equipWithSourceComment(el, _reduction);
				el.getComment().add("FIXME: Goto insructions are not supported in structured algorithms!");
				// END KGU#407 2017-06-22
				_parentNode.addElement(el);
			}
			else if (
					// Continue instruction
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
					// START KGU#407 2017-06-20: Enh. #420 - comments already here
					this.equipWithSourceComment(el, _reduction);
					// END KGU#407 2017-06-22
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
				// START KGU#407 2017-06-20: Enh. #420 - comments already here
				//_parentNode.addElement(new Instruction(translateContent(content)));
				_parentNode.addElement(this.equipWithSourceComment(new Instruction(translateContent(content)), _reduction));
				// END KGU#407 2017-06-22
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
				// START KGU#407 2017-06-20: Enh. #420 - comments already here
				//_parentNode.addElement(new Instruction(translateContent(content)));
				_parentNode.addElement(this.equipWithSourceComment(new Instruction(translateContent(content)), _reduction));
				// END KGU#407 2017-06-22
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
				root.setProgram(false);
				subRoots.add(root);
				// START KGU#376 2017-09-30: Enh. #389
				if (prevRoot.getMethodName().equals("???") && prevRoot.children.getSize() > 0) {
					// We must have inserted some global stuff, so assume a dependency...
					this.importingRoots.add(root);
				}
				// END KGU#376 2017-09-30
				String content = new String();
				// Is there a type specification different from void?
				if (typeIx >= 0) {
					Token typeToken = secReduc.get(typeIx);
					if (typeToken.getType() == SymbolType.CONTENT) {
						content += typeToken.asString() + " ";
					}
					else {
						content = getContent_R(secReduc.get(typeIx).asReduction(), content).trim() + " ";
					}
					content = content.replace("const ", "");
				}
				// START KGU#523 2018-06-17: Bugfix #542 - result type void should be suppressed
				if (content.trim().equals("void")) {
					content = "";
				}
				// END KGU#523 2018-06-17
				content += funcName + "(";
				int bodyIndex = 4;
				String params = "";
				switch (ruleId) {
				case RuleConstants.PROD_FUNCDECL_LPAREN_RPAREN2:
					//params = getparamsFromStructDecl(_reduction.get(4).asReduction());
					bodyIndex = 5;
					// START KGU#525 2018-06-18: Don't throw the type information away...
					//params = getCompsFromStructDef(_reduction.get(4).asReduction()).concatenate("; ");
					params = getContent_R(_reduction.get(2).asReduction(), "");
					{
						StringList paramDecls = getCompsFromStructDef(_reduction.get(4).asReduction());
						StringList paramNames = StringList.explode(params, ",");
						// Sort the parameter declarations according to the arg list (just in case...)
						if (paramDecls.count() == paramNames.count()) {
							StringList paramsOrdered = new StringList();
							for (int p = 0; p < paramNames.count(); p++) {
								Matcher pm = Pattern.compile("(^|.*?\\W)" + paramNames.get(p).trim() + ":.*").matcher("");
								for (int q = 0; q < paramDecls.count(); q++) {
									String pd = paramDecls.get(q);
									if (pm.reset(pd).matches()) {
										paramsOrdered.add(pd);
										break;
									}
								}
								if (paramsOrdered.count() < p+1) {
									paramsOrdered.add(paramNames.get(p));
								}
							}
							params = paramsOrdered.concatenate("; ");
						}
					}
					break;
					// END KGU#525 2018-06-18
				case RuleConstants.PROD_FUNCDECL_LPAREN_RPAREN:
					params = getContent_R(_reduction.get(2).asReduction(), "");
					break;
				case RuleConstants.PROD_FUNCDECL_LPAREN_RPAREN3:
					bodyIndex = 3;
					break;
				}
				content += params + ")";
				root.setText(content);
				// START KGU#407 2017-06-20: Enh. #420 - comments already here
				this.equipWithSourceComment(root, _reduction);
				// END KGU#407 2017-06-22
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
				// START KGU#407 2017-06-20: Enh. #420 - comments already here
				this.equipWithSourceComment(ele, _reduction);
				// END KGU#407 2017-06-22
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
				// START KGU#407 2017-06-20: Enh. #420 - comments already here
				this.equipWithSourceComment(ele, _reduction);
				// END KGU#407 2017-06-22
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
				_parentNode.getElement(_parentNode.getSize()-1).setColor(colorMisc);
				
				// get the second part - should be an ordinary condition
				String content = getContent_R(_reduction.get(4).asReduction(), "");
				While ele = new While((getKeyword("preWhile").trim() + " " + translateContent(content) + " " + getKeyword("postWhile").trim()).trim());
				// START KGU#407 2017-06-20: Enh. #420 - comments already here
				this.equipWithSourceComment(ele, _reduction);
				// END KGU#407 2017-06-22
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
				// START KGU#407 2017-06-20: Enh. #420 - comments already here
				this.equipWithSourceComment(ele, _reduction);
				// END KGU#407 2017-06-22
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
			// START KGU#412 2017-05-28: Frequent stack overflows on large sources was observed
			else if (ruleId == RuleConstants.PROD_STMLIST)
			{
				// We can easily reduce recursion overhead since the crucial <Stm List> rule is
				// right-recursive. We don't even need auxiliary data structures.
				while (_reduction.size() > 0) {
					this.buildNSD_R(_reduction.get(0).asReduction(), _parentNode);
					_reduction = _reduction.get(1).asReduction();
				}
			}
			// END KGU#412 2017-05-28
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
	 * Processes type specifications for a variable / constant declaration or a
	 * type definition (argument {@code _declaringVars} indicates which of both).
	 * If an anonymous struct description is found then a type definition object
	 * will be inserted either with a generic name (if {@code _typeList} is empty)
	 * or with the first element of {@code _typeList} as name. Except in the latter
	 * case (type definition with given name created) the name of the found type
	 * will be inserted at the beginning of {@code _typeList}.
	 * If {@code _isGlobal} is true and a type definition is to be created then
	 * a dependency of the current root to the global diagram is established in
	 * {@code this.importingRoots}.
	 * @param _reduction - current {@link Reduction} object
	 * @param _ruleId - table id of the production rule
	 * @param _subqueue - the {@link Subqueue} to which elements are to be added
	 * @param _isGlobal - whether the type / variable is a global one
	 * @param _typeList - a container for type names, both for input and output 
	 * @param _declaringVars - whether this is used by a variable/constant declaration (type definition otherwise)
	 * @return a logical value indicating whether the current type is a structured one
	 */
	protected boolean processTypes(Reduction _reduction, int _ruleId, Subqueue _subqueue, boolean _isGlobal,
			StringList _typeList, boolean _declaringVars) {
		boolean isStruct = false;
		String type = "int";
		boolean isConstant = false;
		boolean addType = true;
		int typeIx = -1;	// token index of the type description
		switch (_ruleId) {
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
		if (typeIx >= 0) {
			Reduction typeRed = _reduction.get(typeIx).asReduction();
			type = getContent_R(typeRed, "").trim();
			mtchConst.reset(type);
			if (mtchConst.matches()) {
				type = mtchConst.replaceAll("$1$2").trim();
				isConstant = true;
			}
			// START KGU#388 2017-09-30: Enh. #423 Support for struct types and their references
			if (type.contains("struct")) {
				switch (_ruleId) {
				case RuleConstants.PROD_VARDECL_SEMI:
				case RuleConstants.PROD_VARDECL_CONST_SEMI:
					typeRed = typeRed.get(0).asReduction();
					break;
				case RuleConstants.PROD_VARDECL_SEMI2:
					if (typeRed.getParent().getTableIndex() == RuleConstants.PROD_CONSTTYPE_CONST) {
						typeRed = typeRed.get(1).asReduction().get(0).asReduction();	
					}
					else {
						typeRed = typeRed.get(0).asReduction();
					}					
					break;
				}
				switch (typeRed.getParent().getTableIndex())
				{
				case RuleConstants.PROD_BASE_STRUCT_ID:
					type = type.replace("struct ", "").trim();
					isStruct = true;
					break;
				case RuleConstants.PROD_BASE_STRUCT_LBRACE_RBRACE:
					// We have an anonymous type here - if we didn't obtain a type name, we'll create a new one
					{
						if (_typeList.count() == 0) {
							type = String.format("AnonStruct%1$03d", typeCount++);
						}
						else {
							type = _typeList.get(0);
							addType = false;
						}
						// START KGU#517 2018-06-04: Bugfix #??? - we must analyse recursively and convert syntax!
						//String components = this.getContent_R(typeRed.get(2).asReduction(), "").replace("struct ", "").trim();
						//if (components.endsWith(";")) components = components.substring(0, components.length()-1);
						//String content = "type " + type + " = struct{" + components + "}";
						StringList components = getCompsFromStructDef(typeRed.get(2).asReduction());
						String content = "type " + type + " = struct{" + components.concatenate(";\\\n") + "}";
						// END KGU#517 2018-06-04
						Instruction typeDef = new Instruction(this.translateContent(content));
						if (addType && _declaringVars) {
							typeDef.setComment("Automatically created from anonymous struct in following declaration");
						}
						else {
							this.equipWithSourceComment(typeDef, _reduction);
						}
						if (_isGlobal) {
							typeDef.setColor(colorGlobal);
							// START KGU#376 2017-09-30: Enh. #389
							// CHECKME!
							if (root != globalRoot && !importingRoots.contains(root)) {
								importingRoots.add(root);
							}
							// END KGU#376 2017-09-30
						}
						_subqueue.addElement(typeDef);
						isStruct = true;
					}
					break;
				default:
					// Something strange or more complicated might go on - put a remark to the log
					this.log(this.getContent_R(_reduction,
							"Unclear occurrence of \"struct\" detected, consider reporting this issue to http://structorizer.fisch.lu:\n"),
							false);
				}
			}
			// END KGU#388 2017-09-30
		}
		if (isConstant && _declaringVars) {
			type = "const " + type;
		}
		if (addType) {
			_typeList.insert(type, 0);
		}
		return isStruct;
	}

	// START KGU#517 2018-06-04: Bugfix ???
	/**
	 * Is to extract the struct component declarations from a struct definition and
	 * to convert them into Structorizer (Pascal-like) syntax.
	 * Also useful as helper method for the analysis of the very old C function declaration
	 * syntax:<br/>
	 * {@code <Type> <Func ID> '(' <Id List> ')' <Struct Def> <Block>}
	 * @param _structDef - the {@link Reduction} representing a {@code <Struct Decl>} rule.
	 * @return the component declaration strings in Structorizer syntax
	 */
	private StringList getCompsFromStructDef(Reduction _structDef) {
		//String components = this.getContent_R(_structDef, "").replace("struct ", "").trim();
		//if (components.endsWith(";")) components = components.substring(0, components.length()-1);
		StringList components = new StringList();
		do {
			Reduction varDecl = _structDef;
			if (_structDef.getParent().getTableIndex() == RuleConstants.PROD_STRUCTDEF) {
				varDecl = _structDef.get(0).asReduction();
				_structDef = _structDef.get(1).asReduction();
			}
			else {
				_structDef = null;
			}
			int nameIx = varDecl.size() - 3;
			String type = "int";
			if (varDecl.getParent().getTableIndex() < RuleConstants.PROD_VARDECL_SEMI3) {
				type = getContent_R(varDecl.get(nameIx -1).asReduction(), "");
			}
			Reduction varList = varDecl.get(nameIx+1).asReduction();
			StringList compNames = new StringList();
			Reduction varRed = varDecl.get(nameIx).asReduction();
			String name = varRed.get(0).asString();
			String index = getContent_R(varRed.get(1).asReduction(), "").trim();
			if (!index.isEmpty()) {
				if (index.equals("[]")) {
					index = "";
				}
				components.add(name + ": array" + index + " of " + type);
			}
			else {
				compNames.add(name);
				while (varList.size() > 0) {
					varRed = varList.get(1).asReduction();
					String pointers = getContent_R(varRed.get(0).asReduction(), "").trim();
					name = varRed.get(1).asReduction().get(0).asString();
					index = getContent_R(varRed.get(1).asReduction().get(1).asReduction(), "").trim();
					if (!index.isEmpty() || !pointers.isEmpty()) {
						if (compNames.count() > 0) {
							components.add(compNames.concatenate(", ") + ": " + type);
							compNames.clear();
						}
						if (index.equals("[]")) {
							index = "array of ";
						}
						else if (!index.isEmpty()) {
							index = "array " + index + " of ";
						}
						components.add(name + ": " + index + type + pointers);
					}
					else {
						compNames.add(name);
					}
					varList = varList.get(2).asReduction();
				}
				if (compNames.count() > 0) {
					components.add(compNames.concatenate(", ") + ": " + type);
				}
			}
		} while (_structDef != null);
		return components;
	}
	// END KGU#517 2018-06-04
	
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
	 * @param _comment - a retrieved source code comment to be placed inthe element or null
	 * @param _forceDecl TODO
	 */
	private void buildDeclOrAssignment(Reduction _reduc, String _type, Subqueue _parentNode, String _comment, boolean _forceDecl)
	{
		boolean isConstant = _type != null && _type.startsWith("const ");
		int ruleId = _reduc.getParent().getTableIndex();
		String content = getContent_R(_reduc, "");	// Default???
		if (ruleId == RuleConstants.PROD_VAR_ID) {
			log("\ttrying PROD_VAR_ID ...\n", false);
			// Simple declaration - if allowed then make it to a Pascal decl.
			if (this.optionImportVarDecl || _forceDecl) {
				content = "var " + content + ": " + _type;
				Element instr = new Instruction(translateContent(content));
				// START KGU#407 2017-06-22: Enh. #420
				if (_comment != null) {
					instr.setComment(_comment);
				}
				// END KGU#407 2017-06-22
				if (_parentNode.parent instanceof Root && ((Root)_parentNode.parent).getMethodName().equals("???")) {
					// START KGU#407 2017-06-22: Enh. #420
					//instr.setComment("globally declared!");
					instr.getComment().add("Globally declared!");
					// END KGU#407 2017-06-22
					instr.setColor(colorGlobal);
					// FIXME
					if (root != _parentNode.parent && !this.importingRoots.contains(root)) {
						this.importingRoots.add(root);
						((Root)_parentNode.parent).addToIncludeList((Root)_parentNode.parent);
					}
				}
				else {
					instr.setColor(colorDecl);	// local declarations with a smooth green
				}
				_parentNode.addElement(instr);
			}
		}
		else if (ruleId == RuleConstants.PROD_VARITEM) {
			log("\ttrying PROD_VARITEM ...\n", false);
			// This should be the <Pointers> token...
			String ptype = this.getContent_R(_reduc.get(0).asReduction(), _type);
			// .. and this is assumed to be the <Var> token
			buildDeclOrAssignment(_reduc.get(1).asReduction(), ptype, _parentNode, _comment, _forceDecl);
		}
		else if (ruleId == RuleConstants.PROD_VAR_ID_EQ) {
			// assignment
			log("\ttrying PROD_VAR_ID_EQ ...\n", false);
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
			// START KGU#407 2017-06-22: Enh. #420
			if (_comment != null) {
				instr.setComment(_comment);
			}
			// END KGU#407 2017-06-22
			if (_parentNode.parent instanceof Root && ((Root)_parentNode.parent).getMethodName().equals("???")) {
				// START KGU#407 2017-06-22: Enh. #420
				//instr.setComment("globally declared!");
				instr.getComment().add("Globally declared!");
				// END KGU#407 2017-06-22
				instr.setColor(colorGlobal);
			}
			_parentNode.addElement(instr);
		}
		log("\tfallen back with rule " + ruleId + " (" + _reduc.getParent().toString() + ")\n", false);
	}
	
	/**
	 * Converts a rule of type PROD_NORMALSTM_SWITCH_LPAREN_RPAREN_LBRACE_RBRACE into the
	 * skeleton of a Case element. The case branches will be handled separately
	 * @param _reduction - Reduction rule of a switch instruction
	 * @param _parentNode - the Subqueue this Case element is to be appended to
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
		// START KGU#407 2017-06-20: Enh. #420 - comments already here
		this.equipWithSourceComment(ele, _reduction);
		// END KGU#407 2017-06-22
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
					newExprList.add(_args.subSequence(i, _args.count()).concatenate(", "));
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
		// START KGU#407 2017-06-20: Enh. #420 - comments already here
		//_parentNode.addElement(new Instruction(content.trim()));
		_parentNode.addElement(this.equipWithSourceComment(new Instruction(content.trim()), _reduction));
		// END KGU#407 2017-06-22
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
		// START KGU#407 2017-06-20: Enh. #420 - comments already here
		//_parentNode.addElement(new Instruction(content.trim()));
		_parentNode.addElement(this.equipWithSourceComment(new Instruction(content.trim()), _reduction));
		// END KGU#407 2017-06-22
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
		// If we haden't to replace some things here, we might as well
		// have used token.asString(), it seems (instead of passing the
		// token as reduction to this method).
		for(int i=0; i<_reduction.size(); i++)
		{
			Token token = _reduction.get(i);
			switch (token.getType()) 
			{
			case NON_TERMINAL:
				_content = getContent_R(token.asReduction(), _content);
				// START KGU 2017-05-27: There may be strings split over several lines... 
				{
					// Real (unescaped) newlines shouldn't occur within expressions otherwise
					StringList parts = StringList.explode(_content, "\n");
					if (parts.count() > 1) {
						_content = "";
						for (i = 0; i < parts.count(); i++) {
							String sep = " ";	// By default we will just put a space charcter ther
							if (_content.endsWith("\"") && parts.get(i).trim().startsWith("\"")) {
								// In this case we may concatenate the strings (which is the meaning)
								sep = " + ";
							}
							_content += sep + parts.get(i).trim();
						}
						_content = _content.trim();
					}
				}
				// END KGU 2017-05-27
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
					// NOTE: The missing of a break instruction is intended here!
					// START KGU 2017-05-26: We must of course restore the original type name
				case SymbolConstants.SYM_USERTYPEID:
						toAdd = this.undoIdReplacements(toAdd);
					// END KGU 2017-05-26
					// NOTE: The missing of a break instruction is intended here!
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
		if (ruleHead.equals("<Value>") || ruleHead.equals("<Call Id>")) {
			exprList.add(getContent_R(_reduc, ""));
		}
		else while (ruleHead.equals("<Expr>") || ruleHead.equals("<ExprIni>")) {
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
			fileName = fileName.replaceAll("(.*?)[^A-Za-z0-9_](.*?)", "$1_$2");
			if (aRoot.getParameterNames().count() > 0) {
				String header = aRoot.getText().getText();
				header = header.replaceFirst("(.*?)main([((].*)", "$1" + fileName + "$2");
				aRoot.setText(header);
			}
			else {
				aRoot.setText(fileName);
				aRoot.setProgram(true);
			}
			// Are there some global definitions to be imported?
			if (this.globalRoot != null && this.globalRoot.children.getSize() > 0 && this.globalRoot != aRoot) {
				String oldName = this.globalRoot.getMethodName();
				String inclName = fileName + "Globals";
				this.globalRoot.setText(inclName);
				// START KGU#376 2017-05-17: Enh. #389 now we have an appropriate diagram type
				//this.globalRoot.setProgram(true);
				this.globalRoot.setInclude();
				// END KGU#376 2017-05-17
				// START KGU#376 2017-07-01: Enh. #389 - now register global includable with Root
//				for (Call provCall: this.provisionalImportCalls) {
//					provCall.setText(provCall.getText().get(0).replace(DEFAULT_GLOBAL_NAME, inclName).replace("???", inclName));
//				}
//				this.provisionalImportCalls.clear();
				for (Root impRoot: this.importingRoots) {
					if (impRoot.includeList != null) {
						int n = impRoot.includeList.replaceAll(oldName, inclName);
						n += impRoot.includeList.replaceAll(DEFAULT_GLOBAL_NAME, inclName);
						n += impRoot.includeList.replaceAll("???", inclName);
						if (n > 1) {
							impRoot.includeList.removeAll(inclName);
							impRoot.includeList.add(inclName);
						}
					}
					else {
						impRoot.includeList = StringList.getNew(inclName);
					}
				}
				this.importingRoots.clear();
				// END KGU#376 2017-07-01
			}
		}
		// START KGU#376 2017-04-11: enh. #389 import mechanism for globals
//		if (this.globalRoot != null && this.globalRoot != aRoot) {
//			String globalName = this.globalRoot.getMethodName();
//			// START KGU#376 2017-07-01: Enh. #389 - modified mechanism
////			Call importCall = new Call(getKeywordOrDefault("preImport", "import") + " " + (globalName.equals("???") ? DEFAULT_GLOBAL_NAME : globalName));
////			importCall.setColor(colorGlobal);
////			aRoot.children.insertElementAt(importCall, 0);
////			if (globalName.equals("???")) {
////				this.provisionalImportCalls.add(importCall);
////			}
//			if (aRoot.includeList == null) {
//				aRoot.includeList = StringList.getNew(globalName);
//			}
//			else {
//				aRoot.includeList.addIfNew(globalName);
//			}
//			if (!this.importingRoots.contains(aRoot)) {
//				this.importingRoots.add(aRoot);
//			}
//			// END KGU#376 2017-07-01
//		}
		// END KGU#376 2017-04-11
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.parsers.CodeParser#postProcess(java.lang.String)
	 */
	protected void subclassPostProcess(String textToParse)
	{
		// May there was no main function but global definitions
		if (this.globalRoot != null && this.globalRoot.children.getSize() > 0) {
			String globalName = this.globalRoot.getMethodName();
			if (globalName.equals("???")) {
				// FIXME: Shouldn't we also derive the name from the filename as in the method above?
				this.globalRoot.setText(globalName = DEFAULT_GLOBAL_NAME);
			}
			// START KGU#376 2017-05-23: Enh. #389 now we have an appropriate diagram type
			//this.globalRoot.setProgram(true);
			this.globalRoot.setInclude();
			// END KGU#376 2017-05-23
			// Check again that we haven't forgotten to update any include list
			for (Root dependent: this.importingRoots) {
				if (dependent.includeList == null) {
					dependent.includeList = StringList.getNew(globalName);
				}
				else {
					dependent.includeList.removeAll("???");
					dependent.includeList.addIfNew(globalName);
				}
			}
			this.importingRoots.clear();
		}
	}

}

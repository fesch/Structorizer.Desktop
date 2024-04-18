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
 *      Description:    Class to parse an ANSI-C99 file and build structograms from the reduction tree.
 *
 ******************************************************************************************************
 *
 *      Revision List (Template File!)
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.03.02      First Issue for Iden GOLDEngine
 *      Kay Gürtzig     2017.03.11      Parameter annotations and some comments corrected, indentation unified
 *      Kay Gürtzig     2018.03.26      Imports revised
 *
 ******************************************************************************************************
 *
 *      Revision List (this parser)
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2018-03-26      First Issue (generated with GOLDprog.exe)
 *      Kay Gürtzig     2018-04-12      RuleConstants updated to corrected grammar (version 1.1)
 *      Kay Gürtzig     2018-06-18      Bugfix #540: replaceDefinedEntries() could get caught in an eternal loop
 *                                      Enh. #541: New option "redundantNames" to eliminate disturbing symbols or macros
 *      Kay Gürtzig     2018-06-19      File decomposed and inheritance changed
 *      Kay Gürtzig     2018-06-20      Most algorithmic structures implemented, bugfixes #545, #546 integrated
 *      Kay Gürtzig     2018-06-23      Function definitions, struct definitions, and struct initializers
 *      Kay Gürtzig     2018-07-10      Precaution against incomplete FOR loops (index error on colouring parts)
 *                                      Provisional enum type import as constant definition sequence.
 *      Kay Gürtzig     2019-02-13      Bugfix #678: Array declarations hadn't been imported properly
 *      Kay Gürtzig     2019-02-28      Bugfix #690 - workaround for struct types in function headers
 *      Kay Gürtzig     2019-03-01      Bugfix #692 - failed constant recognition
 *      Kay Gürtzig     2019-03-29      KGU#702: Index range exception in method getPointers() fixed.
 *      Kay Gürtzig     2019-11-18      Enh. #739: Direct enum type import
 *      Kay Gürtzig     2020-03-09      Issue #835: Revised mechanism for the insertion of optional structure keywords
 *      Kay Gürtzig     2023-09-27      Bugfix #1089.2-4 three flaws on typedef imports
 *      Kay Gürtzig     2023-09-28      Issue #1091: Correct handling of alias, enum, and array type definitions
 *      Kay Gürtzig     2023-09-29      Bugfix #678: Unwanted side-effect on pointer types mended
 *      Kay Gürtzig     2024-03-17      Bugfix #1141: Measures against stack overflow in buildNSD_R()
 *      Kay Gürtzig     2024-04-17/18   Bugfix #1163: Import of non-trivial switch structures improved
 *      Kay Gürtzig     2024-04-18      Bugfix #1164: Adapted to new grammar version (1.6)
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.creativewidgetworks.goldparser.engine.*;
import com.creativewidgetworks.goldparser.engine.enums.SymbolType;

import lu.fisch.structorizer.elements.Alternative;
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
import lu.fisch.structorizer.elements.TypeMapEntry;
import lu.fisch.structorizer.elements.While;
import lu.fisch.utils.StringList;

/**
 * Code import parser class of Structorizer 3.27, based on GOLDParser 5.0 for the ANSI-C99 language.
 * This file contains grammar-specific constants and individual routines to build
 * structograms (Nassi-Shneiderman diagrams) from the parsing tree. 
 * @author Kay Gürtzig
 */
public class C99Parser extends CPreParser
{

	/** rule ids representing statements, used as stoppers for comment retrieval (enh. #420) */
	private static final int[] statementIds = new int[]{
			/* TODO: Fill in the RuleConstants members of those productions that are
			 * to be associated with comments found in their syntax subtrees or their
			 * immediate environment. */
			RuleConstants.PROD_FUNCTIONDEF,
			RuleConstants.PROD_POSTFIXEXP_LPAREN_RPAREN,
			RuleConstants.PROD_POSTFIXEXP_LPAREN_RPAREN2,
			RuleConstants.PROD_POSTFIXEXP_PLUSPLUS,
			RuleConstants.PROD_POSTFIXEXP_MINUSMINUS,
			RuleConstants.PROD_UNARYEXP_PLUSPLUS,
			RuleConstants.PROD_UNARYEXP_PLUSPLUS,
			RuleConstants.PROD_DECLARATION_SEMI,
			RuleConstants.PROD_DECLARATION_SEMI2,
			RuleConstants.PROD_LABELLEDSTMT_IDENTIFIER_COLON,
			RuleConstants.PROD_SELECTIONSTMT_IF_LPAREN_RPAREN,
			RuleConstants.PROD_SELECTIONSTMT_IF_LPAREN_RPAREN_ELSE,
			RuleConstants.PROD_SELECTIONSTMT_SWITCH_LPAREN_RPAREN_LBRACE_RBRACE,
			RuleConstants.PROD_CASESTMTS_CASE_COLON,
			RuleConstants.PROD_CASESTMTS_DEFAULT_COLON,
			//RuleConstants.PROD_EXPRESSIONSTMT_SEMI,
			//RuleConstants.PROD_EXPRESSIONSTMT_SEMI2,
			RuleConstants.PROD_ASSIGNEXP,
			RuleConstants.PROD_JUMPSTMT_GOTO_IDENTIFIER_SEMI,
			RuleConstants.PROD_JUMPSTMT_CONTINUE_SEMI,
			RuleConstants.PROD_JUMPSTMT_BREAK_SEMI,
			RuleConstants.PROD_JUMPSTMT_RETURN_SEMI,
			RuleConstants.PROD_ITERATIONSTMT_WHILE_LPAREN_RPAREN,
			RuleConstants.PROD_ITERATIONSTMT_DO_WHILE_LPAREN_RPAREN_SEMI,
			RuleConstants.PROD_ITERATIONSTMT_FOR_LPAREN_SEMI_SEMI_RPAREN,
			RuleConstants.PROD_ITERATIONSTMT_FOR_LPAREN_SEMI_RPAREN
	};

	//---------------------- Grammar specification ---------------------------

	@Override
	protected final String getCompiledGrammar()
	{
		return "C-ANSI99.egt";
	}
	
	@Override
	protected final String getGrammarTableName()
	{
		return "ANSI-C99";
	}

	//------------------------------ Constructor -----------------------------

	/**
	 * Constructs a parser for language ANSI-C99, loads the grammar as resource and
	 * specifies whether to generate the parse tree as string
	 */
	public C99Parser() {
	}

	//---------------------- File Filter configuration ---------------------------
	
	@Override
	public String getDialogTitle() {
		return "ANSI-C99";
	}

	@Override
	protected String getFileDescription() {
		return "ANSI-C99 Source Files";
	}

	//---------------------- Grammar table constants DON'T MODIFY! ---------------------------

	// Symbolic constants naming the table indices of the symbols of the grammar 
	//@SuppressWarnings("unused")
	private interface SymbolConstants 
	{
//		final int SYM_EOF               =   0;  // (EOF)
//		final int SYM_ERROR             =   1;  // (Error)
//		final int SYM_COMMENT           =   2;  // Comment
//		final int SYM_NEWLINE           =   3;  // NewLine
//		final int SYM_WHITESPACE        =   4;  // Whitespace
//		final int SYM_TIMESDIV          =   5;  // '*/'
//		final int SYM_DIVTIMES          =   6;  // '/*'
//		final int SYM_DIVDIV            =   7;  // '//'
		final int SYM_MINUS             =   8;  // '-'
//		final int SYM_MINUSMINUS        =   9;  // '--'
		final int SYM_EXCLAM            =  10;  // '!'
		final int SYM_EXCLAMEQ          =  11;  // '!='
		final int SYM_PERCENT           =  12;  // '%'
//		final int SYM_PERCENTEQ         =  13;  // '%='
		final int SYM_AMP               =  14;  // '&'
		final int SYM_AMPAMP            =  15;  // '&&'
//		final int SYM_AMPEQ             =  16;  // '&='
//		final int SYM_LPAREN            =  17;  // '('
//		final int SYM_RPAREN            =  18;  // ')'
		final int SYM_TIMES             =  19;  // '*'
//		final int SYM_TIMESEQ           =  20;  // '*='
//		final int SYM_COMMA             =  21;  // ','
//		final int SYM_DOT               =  22;  // '.'
//		final int SYM_DOTDOTDOT         =  23;  // '...'
		final int SYM_DIV               =  24;  // '/'
//		final int SYM_DIVEQ             =  25;  // '/='
//		final int SYM_COLON             =  26;  // ':'
//		final int SYM_SEMI              =  27;  // ';'
//		final int SYM_QUESTION          =  28;  // '?'
//		final int SYM_LBRACKET          =  29;  // '['
//		final int SYM_RBRACKET          =  30;  // ']'
		final int SYM_CARET             =  31;  // '^'
//		final int SYM_CARETEQ           =  32;  // '^='
//		final int SYM__BOOL             =  33;  // '_Bool'
//		final int SYM__COMPLEX          =  34;  // '_Complex'
		final int SYM_LBRACE            =  35;  // '{'
		final int SYM_PIPE              =  36;  // '|'
		final int SYM_PIPEPIPE          =  37;  // '||'
//		final int SYM_PIPEEQ            =  38;  // '|='
		final int SYM_RBRACE            =  39;  // '}'
		final int SYM_TILDE             =  40;  // '~'
		final int SYM_PLUS              =  41;  // '+'
//		final int SYM_PLUSPLUS          =  42;  // '++'
//		final int SYM_PLUSEQ            =  43;  // '+='
		final int SYM_LT                =  44;  // '<'
		final int SYM_LTLT              =  45;  // '<<'
//		final int SYM_LTLTEQ            =  46;  // '<<='
		final int SYM_LTEQ              =  47;  // '<='
		final int SYM_EQ                =  48;  // '='
//		final int SYM_MINUSEQ           =  49;  // '-='
		final int SYM_EQEQ              =  50;  // '=='
		final int SYM_GT                =  51;  // '>'
//		final int SYM_MINUSGT           =  52;  // '->'
		final int SYM_GTEQ              =  53;  // '>='
		final int SYM_GTGT              =  54;  // '>>'
//		final int SYM_GTGTEQ            =  55;  // '>>='
//		final int SYM_AUTO              =  56;  // auto
//		final int SYM_BREAK             =  57;  // break
//		final int SYM_CASE              =  58;  // case
//		final int SYM_CHAR              =  59;  // char
//		final int SYM_CHARLITERAL       =  60;  // CharLiteral
//		final int SYM_CONST             =  61;  // const
//		final int SYM_CONTINUE          =  62;  // continue
		final int SYM_DECLITERAL        =  63;  // DecLiteral
//		final int SYM_DEFAULT           =  64;  // default
//		final int SYM_DO                =  65;  // do
//		final int SYM_DOUBLE            =  66;  // double
//		final int SYM_ELSE              =  67;  // else
//		final int SYM_ENUM              =  68;  // enum
//		final int SYM_EXTERN            =  69;  // extern
//		final int SYM_FLOAT             =  70;  // float
		final int SYM_FLOATLITERAL      =  71;  // FloatLiteral
//		final int SYM_FOR               =  72;  // for
//		final int SYM_GOTO              =  73;  // goto
		final int SYM_HEXLITERAL        =  74;  // HexLiteral
//		final int SYM_IDENTIFIER        =  75;  // Identifier
//		final int SYM_IF                =  76;  // if
//		final int SYM_INLINE            =  77;  // inline
//		final int SYM_INT               =  78;  // int
//		final int SYM_LONG              =  79;  // long
		final int SYM_OCTLITERAL        =  80;  // OctLiteral
//		final int SYM_REGISTER          =  81;  // register
//		final int SYM_RESTRICT          =  82;  // restrict
//		final int SYM_RETURN            =  83;  // return
//		final int SYM_SHORT             =  84;  // short
//		final int SYM_SIGNED            =  85;  // signed
//		final int SYM_SIZEOF            =  86;  // sizeof
//		final int SYM_STATIC            =  87;  // static
		final int SYM_STRINGLITERAL     =  88;  // StringLiteral
//		final int SYM_STRUCT            =  89;  // struct
//		final int SYM_SWITCH            =  90;  // switch
//		final int SYM_TYPEDEF           =  91;  // typedef
//		final int SYM_UNION             =  92;  // union
//		final int SYM_UNSIGNED          =  93;  // unsigned
		final int SYM_USERTYPEID        =  94;  // UserTypeId
//		final int SYM_VOID              =  95;  // void
//		final int SYM_VOLATILE          =  96;  // volatile
//		final int SYM_WCHAR_T           =  97;  // 'wchar_t'
//		final int SYM_WHILE             =  98;  // while
//		final int SYM_ABSTRACTDECL      =  99;  // <Abstract Decl>
//		final int SYM_ADDEXP            = 100;  // <Add Exp>
//		final int SYM_ANDEXP            = 101;  // <And Exp>
//		final int SYM_ARGEXPLIST        = 102;  // <ArgExpList>
//		final int SYM_ASSIGNEXP         = 103;  // <Assign Exp>
//		final int SYM_ASSIGNOP          = 104;  // <Assign Op>
//		final int SYM_BLOCKITEM         = 105;  // <BlockItem>
//		final int SYM_BLOCKITEMLIST     = 106;  // <BlockItemList>
//		final int SYM_CASESTMTS         = 107;  // <Case Stmts>
//		final int SYM_CASTEXP           = 108;  // <Cast Exp>
//		final int SYM_COMPOUNDSTMT      = 109;  // <Compound Stmt>
//		final int SYM_CONDEXP           = 110;  // <Cond Exp>
//		final int SYM_CONSTANTEXP       = 111;  // <Constant Exp>
//		final int SYM_DECLSPECIFIERS    = 112;  // <Decl Specifiers>
//		final int SYM_DECLSPECS         = 113;  // <Decl Specs>
//		final int SYM_DECLARATION       = 114;  // <Declaration>
//		final int SYM_DECLARATIONLIST   = 115;  // <DeclarationList>
//		final int SYM_DECLARATOR        = 116;  // <Declarator>
//		final int SYM_DECLLISTOPT       = 117;  // <DeclListOpt>
//		final int SYM_DESIGNATION       = 118;  // <Designation>
//		final int SYM_DESIGNATOR        = 119;  // <Designator>
//		final int SYM_DESIGNATORLIST    = 120;  // <DesignatorList>
//		final int SYM_DIRABSTRDECLOPT   = 121;  // <DirAbstrDeclOpt>
//		final int SYM_DIRECTABSTRDECL   = 122;  // <Direct Abstr Decl>
//		final int SYM_DIRECTDECL        = 123;  // <Direct Decl>
//		final int SYM_ENUMERATOR        = 124;  // <Enumerator>
//		final int SYM_ENUMERATORSPEC    = 125;  // <Enumerator Spec>
//		final int SYM_ENUMLIST          = 126;  // <EnumList>
//		final int SYM_EQUATEXP          = 127;  // <Equat Exp>
//		final int SYM_EXCLOREXP         = 128;  // <ExclOr Exp>
//		final int SYM_EXPRESSION        = 129;  // <Expression>
//		final int SYM_EXPRESSIONSTMT    = 130;  // <Expression Stmt>
//		final int SYM_EXPROPT           = 131;  // <ExprOpt>
//		final int SYM_EXTERNALDECL      = 132;  // <External Decl>
//		final int SYM_FUNCTIONDEF       = 133;  // <Function Def>
//		final int SYM_IDENTIFIERLIST    = 134;  // <IdentifierList>
//		final int SYM_IDLISTOPT         = 135;  // <IdListOpt>
//		final int SYM_INITDECLARATOR    = 136;  // <Init Declarator>
//		final int SYM_INITDECLLIST      = 137;  // <InitDeclList>
//		final int SYM_INITIALIZER       = 138;  // <Initializer>
//		final int SYM_INITIALIZERLIST   = 139;  // <InitializerList>
//		final int SYM_ITERATIONSTMT     = 140;  // <Iteration Stmt>
//		final int SYM_JUMPSTMT          = 141;  // <Jump Stmt>
//		final int SYM_LABELLEDSTMT      = 142;  // <Labelled Stmt>
//		final int SYM_LITERAL           = 143;  // <Literal>
//		final int SYM_LOGANDEXP         = 144;  // <LogAnd Exp>
//		final int SYM_LOGOREXP          = 145;  // <LogOr Exp>
//		final int SYM_MULTEXP           = 146;  // <Mult Exp>
//		final int SYM_OREXP             = 147;  // <Or Exp>
//		final int SYM_PARAMETERDECL     = 148;  // <Parameter Decl>
//		final int SYM_PARAMETERLIST     = 149;  // <ParameterList>
//		final int SYM_PARAMTYPELIST     = 150;  // <ParamTypeList>
//		final int SYM_POINTER           = 151;  // <Pointer>
//		final int SYM_POSTFIXEXP        = 152;  // <Postfix Exp>
//		final int SYM_RELATEXP          = 153;  // <Relat Exp>
//		final int SYM_SELECTIONSTMT     = 154;  // <Selection Stmt>
//		final int SYM_SHIFTEXP          = 155;  // <Shift Exp>
//		final int SYM_SPECQUALLIST      = 156;  // <SpecQualList>
//		final int SYM_SPECQUALS         = 157;  // <SpecQuals>
//		final int SYM_STATEMENT         = 158;  // <Statement>
//		final int SYM_STMTLIST          = 159;  // <StmtList>
//		final int SYM_STORAGECLASS      = 160;  // <Storage Class>
//		final int SYM_STRUCTDECL        = 161;  // <Struct Decl>
//		final int SYM_STRUCTDECLARATION = 162;  // <Struct Declaration>
//		final int SYM_STRUCTDECLLIST    = 163;  // <StructDeclList>
//		final int SYM_STRUCTDECLNLIST   = 164;  // <StructDeclnList>
//		final int SYM_STRUCTORUNION     = 165;  // <StructOrUnion>
//		final int SYM_STRUCTORUNIONSPEC = 166;  // <StructOrUnion Spec>
//		final int SYM_TRANSLATIONUNIT   = 167;  // <Translation Unit>
//		final int SYM_TYPEQUALIFIER     = 168;  // <Type Qualifier>
//		final int SYM_TYPESPECIFIER     = 169;  // <Type Specifier>
//		final int SYM_TYPEDEFNAME       = 170;  // <Typedef Name>
//		final int SYM_TYPENAME          = 171;  // <Typename>
//		final int SYM_TYPEQUALLIST      = 172;  // <TypeQualList>
//		final int SYM_TYPEQUALSOPT      = 173;  // <TypeQualsOpt>
//		final int SYM_UNARYEXP          = 174;  // <Unary Exp>
//		final int SYM_UNARYOP           = 175;  // <Unary Op>
//		final int SYM_VALUE             = 176;  // <Value>
	};

	// Symbolic constants naming the table indices of the grammar rules
	//@SuppressWarnings("unused")
	private interface RuleConstants
	{
		final int PROD_DECLARATION_SEMI                                   =   0;  // <Declaration> ::= <Decl Specifiers> <InitDeclList> ';'
		final int PROD_DECLARATION_SEMI2                                  =   1;  // <Declaration> ::= <Decl Specifiers> ';'
		final int PROD_DECLSPECIFIERS                                     =   2;  // <Decl Specifiers> ::= <Storage Class> <Decl Specs>
		final int PROD_DECLSPECIFIERS2                                    =   3;  // <Decl Specifiers> ::= <Type Specifier> <Decl Specs>
		final int PROD_DECLSPECIFIERS3                                    =   4;  // <Decl Specifiers> ::= <Type Qualifier> <Decl Specs>
//		final int PROD_DECLSPECIFIERS_INLINE                              =   5;  // <Decl Specifiers> ::= inline <Decl Specs>
//		final int PROD_DECLSPECS                                          =   6;  // <Decl Specs> ::= <Decl Specifiers>
//		final int PROD_DECLSPECS2                                         =   7;  // <Decl Specs> ::= 
		final int PROD_INITDECLLIST_COMMA                                 =   8;  // <InitDeclList> ::= <InitDeclList> ',' <Init Declarator>
//		final int PROD_INITDECLLIST                                       =   9;  // <InitDeclList> ::= <Init Declarator>
		final int PROD_INITDECLARATOR_EQ                                  =  10;  // <Init Declarator> ::= <Declarator> '=' <Initializer>
//		final int PROD_INITDECLARATOR                                     =  11;  // <Init Declarator> ::= <Declarator>
//		final int PROD_STORAGECLASS_TYPEDEF                               =  12;  // <Storage Class> ::= typedef
//		final int PROD_STORAGECLASS_EXTERN                                =  13;  // <Storage Class> ::= extern
//		final int PROD_STORAGECLASS_STATIC                                =  14;  // <Storage Class> ::= static
//		final int PROD_STORAGECLASS_AUTO                                  =  15;  // <Storage Class> ::= auto
//		final int PROD_STORAGECLASS_REGISTER                              =  16;  // <Storage Class> ::= register
		final int PROD_TYPESPECIFIER_VOID                                 =  17;  // <Type Specifier> ::= void
//		final int PROD_TYPESPECIFIER_CHAR                                 =  18;  // <Type Specifier> ::= char
//		final int PROD_TYPESPECIFIER_WCHAR_T                              =  19;  // <Type Specifier> ::= 'wchar_t'
//		final int PROD_TYPESPECIFIER_SHORT                                =  20;  // <Type Specifier> ::= short
//		final int PROD_TYPESPECIFIER_INT                                  =  21;  // <Type Specifier> ::= int
//		final int PROD_TYPESPECIFIER_LONG                                 =  22;  // <Type Specifier> ::= long
//		final int PROD_TYPESPECIFIER_FLOAT                                =  23;  // <Type Specifier> ::= float
//		final int PROD_TYPESPECIFIER_DOUBLE                               =  24;  // <Type Specifier> ::= double
//		final int PROD_TYPESPECIFIER_SIGNED                               =  25;  // <Type Specifier> ::= signed
//		final int PROD_TYPESPECIFIER_UNSIGNED                             =  26;  // <Type Specifier> ::= unsigned
//		final int PROD_TYPESPECIFIER__BOOL                                =  27;  // <Type Specifier> ::= '_Bool'
		final int PROD_TYPESPECIFIER__COMPLEX                             =  28;  // <Type Specifier> ::= '_Complex'
		final int PROD_TYPESPECIFIER                                      =  29;  // <Type Specifier> ::= <StructOrUnion Spec>
		final int PROD_TYPESPECIFIER2                                     =  30;  // <Type Specifier> ::= <Enumerator Spec>
		final int PROD_TYPESPECIFIER3                                     =  31;  // <Type Specifier> ::= <Typedef Name>
		final int PROD_STRUCTORUNIONSPEC_IDENTIFIER_LBRACE_RBRACE         =  32;  // <StructOrUnion Spec> ::= <StructOrUnion> Identifier '{' <StructDeclnList> '}'
		final int PROD_STRUCTORUNIONSPEC_LBRACE_RBRACE                    =  33;  // <StructOrUnion Spec> ::= <StructOrUnion> '{' <StructDeclnList> '}'
		final int PROD_STRUCTORUNIONSPEC_IDENTIFIER                       =  34;  // <StructOrUnion Spec> ::= <StructOrUnion> Identifier
//		final int PROD_STRUCTORUNION_STRUCT                               =  35;  // <StructOrUnion> ::= struct
//		final int PROD_STRUCTORUNION_UNION                                =  36;  // <StructOrUnion> ::= union
//		final int PROD_STRUCTDECLNLIST                                    =  37;  // <StructDeclnList> ::= <StructDeclnList> <Struct Declaration>
//		final int PROD_STRUCTDECLNLIST2                                   =  38;  // <StructDeclnList> ::= <Struct Declaration>
//		final int PROD_STRUCTDECLARATION_SEMI                             =  39;  // <Struct Declaration> ::= <SpecQualList> <StructDeclList> ';'
//		final int PROD_SPECQUALLIST                                       =  40;  // <SpecQualList> ::= <Type Specifier> <SpecQuals>
//		final int PROD_SPECQUALLIST2                                      =  41;  // <SpecQualList> ::= <Type Qualifier> <SpecQuals>
//		final int PROD_SPECQUALS                                          =  42;  // <SpecQuals> ::= <SpecQualList>
//		final int PROD_SPECQUALS2                                         =  43;  // <SpecQuals> ::= 
		final int PROD_STRUCTDECLLIST_COMMA                               =  44;  // <StructDeclList> ::= <StructDeclList> ',' <Struct Decl>
//		final int PROD_STRUCTDECLLIST                                     =  45;  // <StructDeclList> ::= <Struct Decl>
//		final int PROD_STRUCTDECL_COLON                                   =  46;  // <Struct Decl> ::= <Declarator> ':' <Constant Exp>
//		final int PROD_STRUCTDECL                                         =  47;  // <Struct Decl> ::= <Declarator>
//		final int PROD_STRUCTDECL_COLON2                                  =  48;  // <Struct Decl> ::= ':' <Constant Exp>
		final int PROD_ENUMERATORSPEC_ENUM_IDENTIFIER_LBRACE_RBRACE       =  49;  // <Enumerator Spec> ::= enum Identifier '{' <EnumList> '}'
		final int PROD_ENUMERATORSPEC_ENUM_IDENTIFIER_LBRACE_COMMA_RBRACE =  50;  // <Enumerator Spec> ::= enum Identifier '{' <EnumList> ',' '}'
		final int PROD_ENUMERATORSPEC_ENUM_LBRACE_RBRACE                  =  51;  // <Enumerator Spec> ::= enum '{' <EnumList> '}'
		final int PROD_ENUMERATORSPEC_ENUM_LBRACE_COMMA_RBRACE            =  52;  // <Enumerator Spec> ::= enum '{' <EnumList> ',' '}'
		final int PROD_ENUMERATORSPEC_ENUM_IDENTIFIER                     =  53;  // <Enumerator Spec> ::= enum Identifier
		final int PROD_ENUMLIST_COMMA                                     =  54;  // <EnumList> ::= <EnumList> ',' <Enumerator>
//		final int PROD_ENUMLIST                                           =  55;  // <EnumList> ::= <Enumerator>
		final int PROD_ENUMERATOR_IDENTIFIER_EQ                           =  56;  // <Enumerator> ::= Identifier '=' <Constant Exp>
//		final int PROD_ENUMERATOR_IDENTIFIER                              =  57;  // <Enumerator> ::= Identifier
//		final int PROD_TYPEQUALIFIER_CONST                                =  58;  // <Type Qualifier> ::= const
//		final int PROD_TYPEQUALIFIER_RESTRICT                             =  59;  // <Type Qualifier> ::= restrict
//		final int PROD_TYPEQUALIFIER_VOLATILE                             =  60;  // <Type Qualifier> ::= volatile
		final int PROD_DECLARATOR                                         =  61;  // <Declarator> ::= <Pointer> <Direct Decl>
//		final int PROD_DECLARATOR2                                        =  62;  // <Declarator> ::= <Direct Decl>
		final int PROD_DIRECTDECL_IDENTIFIER                              =  63;  // <Direct Decl> ::= Identifier
		final int PROD_DIRECTDECL_LPAREN_RPAREN                           =  64;  // <Direct Decl> ::= '(' <Declarator> ')'
		final int PROD_DIRECTDECL_LBRACKET_RBRACKET                       =  65;  // <Direct Decl> ::= <Direct Decl> '[' <TypeQualsOpt> <Assign Exp> ']'
		final int PROD_DIRECTDECL_LBRACKET_TIMES_RBRACKET                 =  66;  // <Direct Decl> ::= <Direct Decl> '[' <TypeQualsOpt> '*' ']'
		final int PROD_DIRECTDECL_LBRACKET_STATIC_RBRACKET                =  67;  // <Direct Decl> ::= <Direct Decl> '[' <TypeQualList> static <Assign Exp> ']'
		final int PROD_DIRECTDECL_LBRACKET_RBRACKET2                      =  68;  // <Direct Decl> ::= <Direct Decl> '[' <TypeQualsOpt> ']'
		final int PROD_DIRECTDECL_LBRACKET_STATIC_RBRACKET2               =  69;  // <Direct Decl> ::= <Direct Decl> '[' static <TypeQualsOpt> <Assign Exp> ']'
		final int PROD_DIRECTDECL_LPAREN_RPAREN2                          =  70;  // <Direct Decl> ::= <Direct Decl> '(' <ParamTypeList> ')'
		final int PROD_DIRECTDECL_LPAREN_RPAREN3                          =  71;  // <Direct Decl> ::= <Direct Decl> '(' <IdListOpt> ')'
		final int PROD_POINTER_TIMES                                      =  72;  // <Pointer> ::= '*' <TypeQualList> <Pointer>
		final int PROD_POINTER_TIMES2                                     =  73;  // <Pointer> ::= '*' <TypeQualList>
		final int PROD_POINTER_TIMES3                                     =  74;  // <Pointer> ::= '*' <Pointer>
//		final int PROD_POINTER_TIMES4                                     =  75;  // <Pointer> ::= '*'
//		final int PROD_TYPEQUALLIST                                       =  76;  // <TypeQualList> ::= <Type Qualifier>
//		final int PROD_TYPEQUALLIST2                                      =  77;  // <TypeQualList> ::= <TypeQualList> <Type Qualifier>
//		final int PROD_TYPEQUALSOPT                                       =  78;  // <TypeQualsOpt> ::= <TypeQualList>
//		final int PROD_TYPEQUALSOPT2                                      =  79;  // <TypeQualsOpt> ::= 
		final int PROD_PARAMTYPELIST_COMMA_DOTDOTDOT                      =  80;  // <ParamTypeList> ::= <ParameterList> ',' '...'
//		final int PROD_PARAMTYPELIST                                      =  81;  // <ParamTypeList> ::= <ParameterList>
		final int PROD_PARAMETERLIST_COMMA                                =  82;  // <ParameterList> ::= <ParameterList> ',' <Parameter Decl>
//		final int PROD_PARAMETERLIST                                      =  83;  // <ParameterList> ::= <Parameter Decl>
//		final int PROD_PARAMETERDECL                                      =  84;  // <Parameter Decl> ::= <Decl Specifiers> <Declarator>
//		final int PROD_PARAMETERDECL2                                     =  85;  // <Parameter Decl> ::= <Decl Specifiers> <Abstract Decl>
//		final int PROD_PARAMETERDECL3                                     =  86;  // <Parameter Decl> ::= <Decl Specifiers>
		final int PROD_IDENTIFIERLIST_COMMA_IDENTIFIER                    =  87;  // <IdentifierList> ::= <IdentifierList> ',' Identifier
		final int PROD_IDENTIFIERLIST_IDENTIFIER                          =  88;  // <IdentifierList> ::= Identifier
//		final int PROD_IDLISTOPT                                          =  89;  // <IdListOpt> ::= <IdentifierList>
		final int PROD_IDLISTOPT2                                         =  90;  // <IdListOpt> ::= 
//		final int PROD_TYPENAME                                           =  91;  // <Typename> ::= <SpecQualList> <Abstract Decl>
//		final int PROD_TYPENAME2                                          =  92;  // <Typename> ::= <SpecQualList>
		final int PROD_ABSTRACTDECL                                       =  93;  // <Abstract Decl> ::= <Pointer> <Direct Abstr Decl>
//		final int PROD_ABSTRACTDECL2                                      =  94;  // <Abstract Decl> ::= <Pointer>
//		final int PROD_ABSTRACTDECL3                                      =  95;  // <Abstract Decl> ::= <Direct Abstr Decl>
		final int PROD_DIRECTABSTRDECL_LPAREN_RPAREN                      =  96;  // <Direct Abstr Decl> ::= '(' <Abstract Decl> ')'
		final int PROD_DIRECTABSTRDECL_LBRACKET_RBRACKET                  =  97;  // <Direct Abstr Decl> ::= <DirAbstrDeclOpt> '[' <TypeQualsOpt> <Assign Exp> ']'
		final int PROD_DIRECTABSTRDECL_LBRACKET_TIMES_RBRACKET            =  98;  // <Direct Abstr Decl> ::= <DirAbstrDeclOpt> '[' '*' ']'
		final int PROD_DIRECTABSTRDECL_LBRACKET_STATIC_RBRACKET           =  99;  // <Direct Abstr Decl> ::= <DirAbstrDeclOpt> '[' <TypeQualList> static <Assign Exp> ']'
		final int PROD_DIRECTABSTRDECL_LBRACKET_RBRACKET2                 = 100;  // <Direct Abstr Decl> ::= <DirAbstrDeclOpt> '[' <TypeQualsOpt> ']'
		final int PROD_DIRECTABSTRDECL_LBRACKET_STATIC_RBRACKET2          = 101;  // <Direct Abstr Decl> ::= <DirAbstrDeclOpt> '[' static <TypeQualsOpt> <Assign Exp> ']'
		final int PROD_DIRECTABSTRDECL_LPAREN_RPAREN2                     = 102;  // <Direct Abstr Decl> ::= <DirAbstrDeclOpt> '(' <ParamTypeList> ')'
		final int PROD_DIRECTABSTRDECL_LPAREN_RPAREN3                     = 103;  // <Direct Abstr Decl> ::= <DirAbstrDeclOpt> '(' ')'
//		final int PROD_DIRABSTRDECLOPT                                    = 104;  // <DirAbstrDeclOpt> ::= <Direct Abstr Decl>
//		final int PROD_DIRABSTRDECLOPT2                                   = 105;  // <DirAbstrDeclOpt> ::= 
		final int PROD_TYPEDEFNAME_USERTYPEID                             = 106;  // <Typedef Name> ::= UserTypeId
//		final int PROD_INITIALIZER                                        = 107;  // <Initializer> ::= <Assign Exp>
//		final int PROD_INITIALIZER_LBRACE_RBRACE                          = 108;  // <Initializer> ::= '{' <InitializerList> '}'
//		final int PROD_INITIALIZER_LBRACE_COMMA_RBRACE                    = 109;  // <Initializer> ::= '{' <InitializerList> ',' '}'
//		final int PROD_INITIALIZERLIST_COMMA                              = 110;  // <InitializerList> ::= <InitializerList> ',' <Designation>
//		final int PROD_INITIALIZERLIST                                    = 111;  // <InitializerList> ::= <Designation>
//		final int PROD_DESIGNATION_EQ                                     = 112;  // <Designation> ::= <DesignatorList> '=' <Initializer>
//		final int PROD_DESIGNATION                                        = 113;  // <Designation> ::= <Initializer>
//		final int PROD_DESIGNATORLIST                                     = 114;  // <DesignatorList> ::= <DesignatorList> <Designator>
//		final int PROD_DESIGNATORLIST2                                    = 115;  // <DesignatorList> ::= <Designator>
//		final int PROD_DESIGNATOR_LBRACKET_RBRACKET                       = 116;  // <Designator> ::= '[' <Constant Exp> ']'
//		final int PROD_DESIGNATOR_DOT_IDENTIFIER                          = 117;  // <Designator> ::= '.' Identifier
//		final int PROD_STATEMENT                                          = 118;  // <Statement> ::= <Labelled Stmt>
//		final int PROD_STATEMENT2                                         = 119;  // <Statement> ::= <Compound Stmt>
//		final int PROD_STATEMENT3                                         = 120;  // <Statement> ::= <Expression Stmt>
//		final int PROD_STATEMENT4                                         = 121;  // <Statement> ::= <Selection Stmt>
//		final int PROD_STATEMENT5                                         = 122;  // <Statement> ::= <Iteration Stmt>
//		final int PROD_STATEMENT6                                         = 123;  // <Statement> ::= <Jump Stmt>
		final int PROD_LABELLEDSTMT_IDENTIFIER_COLON                      = 124;  // <Labelled Stmt> ::= Identifier ':' <Statement>
		final int PROD_COMPOUNDSTMT_LBRACE_RBRACE                         = 125;  // <Compound Stmt> ::= '{' <BlockItemList> '}'
//		final int PROD_COMPOUNDSTMT_LBRACE_RBRACE2                        = 126;  // <Compound Stmt> ::= '{' '}'
		final int PROD_BLOCKITEMLIST                                      = 127;  // <BlockItemList> ::= <BlockItemList> <BlockItem>
//		final int PROD_BLOCKITEMLIST2                                     = 128;  // <BlockItemList> ::= <BlockItem>
//		final int PROD_BLOCKITEM                                          = 129;  // <BlockItem> ::= <Declaration>
//		final int PROD_BLOCKITEM2                                         = 130;  // <BlockItem> ::= <Statement>
//		final int PROD_EXPRESSIONSTMT_SEMI                                = 131;  // <Expression Stmt> ::= <Expression> ';'
//		final int PROD_EXPRESSIONSTMT_SEMI2                               = 132;  // <Expression Stmt> ::= ';'
		final int PROD_SELECTIONSTMT_IF_LPAREN_RPAREN                     = 133;  // <Selection Stmt> ::= if '(' <Expression> ')' <Statement>
		final int PROD_SELECTIONSTMT_IF_LPAREN_RPAREN_ELSE                = 134;  // <Selection Stmt> ::= if '(' <Expression> ')' <Statement> else <Statement>
		final int PROD_SELECTIONSTMT_SWITCH_LPAREN_RPAREN_LBRACE_RBRACE   = 135;  // <Selection Stmt> ::= switch '(' <Expression> ')' '{' <Case Stmts> '}'
		final int PROD_CASESTMTS_CASE_COLON                               = 136;  // <Case Stmts> ::= case <Cond Exp> ':' <StmtList> <Case Stmts>
		final int PROD_CASESTMTS_DEFAULT_COLON                            = 137;  // <Case Stmts> ::= default ':' <StmtList>
//		final int PROD_CASESTMTS                                          = 138;  // <Case Stmts> ::= 
		final int PROD_STMTLIST                                           = 139;  // <StmtList> ::= <Statement> <StmtList>
//		final int PROD_STMTLIST2                                          = 140;  // <StmtList> ::= 
		final int PROD_ITERATIONSTMT_WHILE_LPAREN_RPAREN                  = 141;  // <Iteration Stmt> ::= while '(' <Expression> ')' <Statement>
		final int PROD_ITERATIONSTMT_DO_WHILE_LPAREN_RPAREN_SEMI          = 142;  // <Iteration Stmt> ::= do <Statement> while '(' <Expression> ')' ';'
		final int PROD_ITERATIONSTMT_FOR_LPAREN_SEMI_SEMI_RPAREN          = 143;  // <Iteration Stmt> ::= for '(' <ExprOpt> ';' <ExprOpt> ';' <ExprOpt> ')' <Statement>
		final int PROD_ITERATIONSTMT_FOR_LPAREN_SEMI_RPAREN               = 144;  // <Iteration Stmt> ::= for '(' <Declaration> <ExprOpt> ';' <ExprOpt> ')' <Statement>
		final int PROD_JUMPSTMT_GOTO_IDENTIFIER_SEMI                      = 145;  // <Jump Stmt> ::= goto Identifier ';'
		final int PROD_JUMPSTMT_CONTINUE_SEMI                             = 146;  // <Jump Stmt> ::= continue ';'
		final int PROD_JUMPSTMT_BREAK_SEMI                                = 147;  // <Jump Stmt> ::= break ';'
		final int PROD_JUMPSTMT_RETURN_SEMI                               = 148;  // <Jump Stmt> ::= return <ExprOpt> ';'
//		final int PROD_TRANSLATIONUNIT                                    = 149;  // <Translation Unit> ::= <External Decl>
		final int PROD_TRANSLATIONUNIT2                                   = 150;  // <Translation Unit> ::= <Translation Unit> <External Decl>
//		final int PROD_EXTERNALDECL                                       = 151;  // <External Decl> ::= <Function Def>
//		final int PROD_EXTERNALDECL2                                      = 152;  // <External Decl> ::= <Declaration>
		final int PROD_FUNCTIONDEF                                        = 153;  // <Function Def> ::= <Decl Specifiers> <Declarator> <DeclListOpt> <Compound Stmt>
		final int PROD_DECLARATIONLIST                                    = 154;  // <DeclarationList> ::= <DeclarationList> <Declaration>
//		final int PROD_DECLARATIONLIST2                                   = 155;  // <DeclarationList> ::= <Declaration>
//		final int PROD_DECLLISTOPT                                        = 156;  // <DeclListOpt> ::= <DeclarationList>
//		final int PROD_DECLLISTOPT2                                       = 157;  // <DeclListOpt> ::= 
//		final int PROD_EXPRESSION_COMMA                                   = 158;  // <Expression> ::= <Expression> ',' <Assign Exp>
//		final int PROD_EXPRESSION                                         = 159;  // <Expression> ::= <Assign Exp>
		final int PROD_ASSIGNEXP                                          = 160;  // <Assign Exp> ::= <Unary Exp> <Assign Op> <Assign Exp>
//		final int PROD_ASSIGNEXP2                                         = 161;  // <Assign Exp> ::= <Cond Exp>
		final int PROD_ASSIGNOP_EQ                                        = 162;  // <Assign Op> ::= '='
//		final int PROD_ASSIGNOP_TIMESEQ                                   = 163;  // <Assign Op> ::= '*='
//		final int PROD_ASSIGNOP_DIVEQ                                     = 164;  // <Assign Op> ::= '/='
//		final int PROD_ASSIGNOP_PERCENTEQ                                 = 165;  // <Assign Op> ::= '%='
		final int PROD_ASSIGNOP_PLUSEQ                                    = 166;  // <Assign Op> ::= '+='
		final int PROD_ASSIGNOP_MINUSEQ                                   = 167;  // <Assign Op> ::= '-='
//		final int PROD_ASSIGNOP_LTLTEQ                                    = 168;  // <Assign Op> ::= '<<='
//		final int PROD_ASSIGNOP_GTGTEQ                                    = 169;  // <Assign Op> ::= '>>='
//		final int PROD_ASSIGNOP_AMPEQ                                     = 170;  // <Assign Op> ::= '&='
//		final int PROD_ASSIGNOP_CARETEQ                                   = 171;  // <Assign Op> ::= '^='
//		final int PROD_ASSIGNOP_PIPEEQ                                    = 172;  // <Assign Op> ::= '|='
//		final int PROD_CONDEXP_QUESTION_COLON                             = 173;  // <Cond Exp> ::= <LogOr Exp> '?' <Expression> ':' <Cond Exp>
//		final int PROD_CONDEXP                                            = 174;  // <Cond Exp> ::= <LogOr Exp>
//		final int PROD_LOGOREXP_PIPEPIPE                                  = 175;  // <LogOr Exp> ::= <LogOr Exp> '||' <LogAnd Exp>
//		final int PROD_LOGOREXP                                           = 176;  // <LogOr Exp> ::= <LogAnd Exp>
//		final int PROD_LOGANDEXP_AMPAMP                                   = 177;  // <LogAnd Exp> ::= <LogAnd Exp> '&&' <Or Exp>
//		final int PROD_LOGANDEXP                                          = 178;  // <LogAnd Exp> ::= <Or Exp>
//		final int PROD_OREXP_PIPE                                         = 179;  // <Or Exp> ::= <Or Exp> '|' <ExclOr Exp>
//		final int PROD_OREXP                                              = 180;  // <Or Exp> ::= <ExclOr Exp>
//		final int PROD_EXCLOREXP_CARET                                    = 181;  // <ExclOr Exp> ::= <ExclOr Exp> '^' <And Exp>
//		final int PROD_EXCLOREXP                                          = 182;  // <ExclOr Exp> ::= <And Exp>
//		final int PROD_ANDEXP_AMP                                         = 183;  // <And Exp> ::= <And Exp> '&' <Equat Exp>
//		final int PROD_ANDEXP                                             = 184;  // <And Exp> ::= <Equat Exp>
//		final int PROD_EQUATEXP_EQEQ                                      = 185;  // <Equat Exp> ::= <Equat Exp> '==' <Relat Exp>
//		final int PROD_EQUATEXP_EXCLAMEQ                                  = 186;  // <Equat Exp> ::= <Equat Exp> '!=' <Relat Exp>
//		final int PROD_EQUATEXP                                           = 187;  // <Equat Exp> ::= <Relat Exp>
		final int PROD_RELATEXP_GT                                        = 188;  // <Relat Exp> ::= <Relat Exp> '>' <Shift Exp>
		final int PROD_RELATEXP_LT                                        = 189;  // <Relat Exp> ::= <Relat Exp> '<' <Shift Exp>
		final int PROD_RELATEXP_LTEQ                                      = 190;  // <Relat Exp> ::= <Relat Exp> '<=' <Shift Exp>
		final int PROD_RELATEXP_GTEQ                                      = 191;  // <Relat Exp> ::= <Relat Exp> '>=' <Shift Exp>
//		final int PROD_RELATEXP                                           = 192;  // <Relat Exp> ::= <Shift Exp>
//		final int PROD_SHIFTEXP_LTLT                                      = 193;  // <Shift Exp> ::= <Shift Exp> '<<' <Add Exp>
//		final int PROD_SHIFTEXP_GTGT                                      = 194;  // <Shift Exp> ::= <Shift Exp> '>>' <Add Exp>
//		final int PROD_SHIFTEXP                                           = 195;  // <Shift Exp> ::= <Add Exp>
		final int PROD_ADDEXP_PLUS                                        = 196;  // <Add Exp> ::= <Add Exp> '+' <Mult Exp>
		final int PROD_ADDEXP_MINUS                                       = 197;  // <Add Exp> ::= <Add Exp> '-' <Mult Exp>
//		final int PROD_ADDEXP                                             = 198;  // <Add Exp> ::= <Mult Exp>
//		final int PROD_MULTEXP_TIMES                                      = 199;  // <Mult Exp> ::= <Mult Exp> '*' <Cast Exp>
//		final int PROD_MULTEXP_DIV                                        = 200;  // <Mult Exp> ::= <Mult Exp> '/' <Cast Exp>
//		final int PROD_MULTEXP_PERCENT                                    = 201;  // <Mult Exp> ::= <Mult Exp> '%' <Cast Exp>
//		final int PROD_MULTEXP                                            = 202;  // <Mult Exp> ::= <Cast Exp>
//		final int PROD_POSTFIXEXP                                         = 203;  // <Postfix Exp> ::= <Value>
//		final int PROD_POSTFIXEXP_LBRACKET_RBRACKET                       = 204;  // <Postfix Exp> ::= <Postfix Exp> '[' <Expression> ']'
		final int PROD_POSTFIXEXP_LPAREN_RPAREN                           = 205;  // <Postfix Exp> ::= <Postfix Exp> '(' <ArgExpList> ')'
		final int PROD_POSTFIXEXP_LPAREN_RPAREN2                          = 206;  // <Postfix Exp> ::= <Postfix Exp> '(' ')'
//		final int PROD_POSTFIXEXP_DOT_IDENTIFIER                          = 207;  // <Postfix Exp> ::= <Postfix Exp> '.' Identifier
//		final int PROD_POSTFIXEXP_MINUSGT_IDENTIFIER                      = 208;  // <Postfix Exp> ::= <Postfix Exp> '->' Identifier
		final int PROD_POSTFIXEXP_PLUSPLUS                                = 209;  // <Postfix Exp> ::= <Postfix Exp> '++'
		final int PROD_POSTFIXEXP_MINUSMINUS                              = 210;  // <Postfix Exp> ::= <Postfix Exp> '--'
//		final int PROD_POSTFIXEXP_LPAREN_RPAREN_LBRACE_RBRACE             = 211;  // <Postfix Exp> ::= '(' <Typename> ')' '{' <InitializerList> '}'
//		final int PROD_POSTFIXEXP_LPAREN_RPAREN_LBRACE_COMMA_RBRACE       = 212;  // <Postfix Exp> ::= '(' <Typename> ')' '{' <InitializerList> ',' '}'
//		final int PROD_ARGEXPLIST                                         = 213;  // <ArgExpList> ::= <Assign Exp>
//		final int PROD_ARGEXPLIST_COMMA                                   = 214;  // <ArgExpList> ::= <ArgExpList> ',' <Assign Exp>
//		final int PROD_UNARYEXP                                           = 215;  // <Unary Exp> ::= <Postfix Exp>
		final int PROD_UNARYEXP_PLUSPLUS                                  = 216;  // <Unary Exp> ::= '++' <Unary Exp>
		final int PROD_UNARYEXP_MINUSMINUS                                = 217;  // <Unary Exp> ::= '--' <Unary Exp>
//		final int PROD_UNARYEXP2                                          = 218;  // <Unary Exp> ::= <Unary Op> <Cast Exp>
//		final int PROD_UNARYEXP_SIZEOF                                    = 219;  // <Unary Exp> ::= sizeof <Unary Exp>
//		final int PROD_UNARYEXP_SIZEOF_LPAREN_RPAREN                      = 220;  // <Unary Exp> ::= sizeof '(' <Typename> ')'
//		final int PROD_UNARYOP_AMP                                        = 221;  // <Unary Op> ::= '&'
//		final int PROD_UNARYOP_TIMES                                      = 222;  // <Unary Op> ::= '*'
//		final int PROD_UNARYOP_PLUS                                       = 223;  // <Unary Op> ::= '+'
//		final int PROD_UNARYOP_MINUS                                      = 224;  // <Unary Op> ::= '-'
//		final int PROD_UNARYOP_TILDE                                      = 225;  // <Unary Op> ::= '~'
//		final int PROD_UNARYOP_EXCLAM                                     = 226;  // <Unary Op> ::= '!'
//		final int PROD_CASTEXP                                            = 227;  // <Cast Exp> ::= <Unary Exp>
//		final int PROD_CASTEXP_LPAREN_RPAREN                              = 228;  // <Cast Exp> ::= '(' <Typename> ')' <Cast Exp>
		final int PROD_VALUE_IDENTIFIER                                   = 229;  // <Value> ::= Identifier
//		final int PROD_VALUE                                              = 230;  // <Value> ::= <Literal>
//		final int PROD_VALUE_LPAREN_RPAREN                                = 231;  // <Value> ::= '(' <Expression> ')'
//		final int PROD_LITERAL_DECLITERAL                                 = 232;  // <Literal> ::= DecLiteral
//		final int PROD_LITERAL_OCTLITERAL                                 = 233;  // <Literal> ::= OctLiteral
//		final int PROD_LITERAL_HEXLITERAL                                 = 234;  // <Literal> ::= HexLiteral
//		final int PROD_LITERAL_FLOATLITERAL                               = 235;  // <Literal> ::= FloatLiteral
//		final int PROD_LITERAL_STRINGLITERAL                              = 236;  // <Literal> ::= StringLiteral
//		final int PROD_LITERAL_CHARLITERAL                                = 237;  // <Literal> ::= CharLiteral
//		final int PROD_CONSTANTEXP                                        = 238;  // <Constant Exp> ::= <Cond Exp>
//		final int PROD_EXPROPT                                            = 239;  // <ExprOpt> ::= <Expression>
		final int PROD_EXPROPT2                                           = 240;  // <ExprOpt> ::= 
	};

	//---------------------- Build methods for structograms ---------------------------

	// START KGU#1153 2024-04-17: Bugfix #1163
	/** Registers all generated Jumps from found breaks in switch cases */
	private final HashSet<Jump> switchBreaks = new HashSet<Jump>();
	// END KGU#1153 2024-04-07
	
	private final Matcher MATCH_PTR_DECL = Pattern.compile("(\\s*([*]\\s*)+)(.+)").matcher("");
	
	/**
	 * Global type map - e.g. for the conversion of struct initializers.
	 * This isn't clean but more reliable and efficient than trying to retrieve the
	 * type info from the several incomplete Roots.
	 */
	private final HashMap<String, TypeMapEntry> typeMap = new HashMap<String, TypeMapEntry>();
	
	/**
	 * Preselects the type of the initial diagram to be imported as function.
	 * @see CodeParser#initializeBuildNSD()
	 */
	@Override
	protected void initializeBuildNSD() throws ParserCancelled
	{
		root.setProgram(false);	// C programs are functions, primarily
		this.optionUpperCaseProgName = Root.check(6);
		// Enh. #420: Configure the lookup table for comment retrieval
		this.registerStatementRuleIds(statementIds);
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.parsers.CodeParser#buildNSD_R(com.creativewidgetworks.goldparser.engine.Reduction, lu.fisch.structorizer.elements.Subqueue)
	 */
	@Override
	protected void buildNSD_R(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled
	{
		//String content = new String();
	
		if (_reduction.size() > 0)
		{
			String rule = _reduction.getParent().toString();
			//String ruleHead = _reduction.getParent().getHead().toString();
			int ruleId = _reduction.getParent().getTableIndex();
			getLogger().log(Level.CONFIG, "Rule {0}, {1}", new Object[]{rule, _parentNode.parent});
			log("buildNSD_R(" + rule + ", " + _parentNode.parent + ")...\n", true);

			if (
					// Function definition?
					ruleId == RuleConstants.PROD_FUNCTIONDEF
					)
			{
				buildFunctionDecl(_reduction);				
			}
			else if (
					// Procedure call?
					(ruleId == RuleConstants.PROD_POSTFIXEXP_LPAREN_RPAREN
					||
					ruleId == RuleConstants.PROD_POSTFIXEXP_LPAREN_RPAREN2)
					&& 
					_reduction.get(0).getType() == SymbolType.NON_TERMINAL
					&& _reduction.get(0).asReduction().getParent().getTableIndex() == RuleConstants.PROD_VALUE_IDENTIFIER
					)
			{
				String content = "";
				String procName = _reduction.get(0).asReduction().get(0).asString();
				StringList arguments = null;
				if (ruleId == RuleConstants.PROD_POSTFIXEXP_LPAREN_RPAREN) {
					arguments = this.getExpressionList(_reduction.get(2).asReduction());
				}
				else {
					arguments = new StringList();
				}
				if (procName.equals("exit")) {
					content = getKeywordOrDefault("preExit", "exit");
					if (arguments.count() > 0) {
						content += arguments.get(0);
						_parentNode.addElement(this.equipWithSourceComment(new Jump(content), _reduction));
					}
				}
				else if (procName.equals("printf") || procName.equals("printf_s") || procName.equals("puts") && arguments.count() == 1)
				{
					buildOutput(_reduction, procName, arguments, _parentNode);
				}
				else if (procName.equals("scanf") || procName.equals("scanf_s") || procName.equals("gets") && arguments.count() == 1){
					buildInput(_reduction, procName, arguments, _parentNode);
				}
				// START KGU#652 2019-02-13: Issue #679 - Support more input and output functions
				else if ((procName.equals("fprintf") || procName.equals("fprintf_s")) && arguments.count() >= 2 && arguments.get(0).equals("stdout")) {
					buildOutput(_reduction, "printf", arguments.subSequence(1, arguments.count()), _parentNode);
				}
				else if ((procName.equals("fscanf") || procName.equals("fscanf_s")) && arguments.count() >= 2 && arguments.get(0).equals("stdin")){
					buildInput(_reduction, procName.substring(1), arguments.subSequence(1, arguments.count()), _parentNode);
				}
				else if (procName.equals("fputs") && arguments.count() == 2 && arguments.get(1).equals("stdout")) {
					buildOutput(_reduction, "puts", arguments.subSequence(0, 1), _parentNode);
				}
				else if (procName.equals("fgets") && arguments.count() == 3 && arguments.get(2).equals("stdin")) {
					buildInput(_reduction, "gets", arguments.subSequence(0, 1), _parentNode);
				}
				// END KGU#652 2019-02-13
				else if (!convertBuiltInRoutines(_reduction, procName, arguments, _parentNode)) {
					_parentNode.addElement(this.equipWithSourceComment(new Instruction(getContent_R(_reduction, content)), _reduction));
				}
			}
			else if (
					// Assignment?
					ruleId == RuleConstants.PROD_ASSIGNEXP
					)
			{
				// FIXME: What if this is part of a declaration?
				// Simply convert it as text and create an instruction. In case of an
				// external function call we'll try to transmute it after all subroutines
				// will have been built.
				String var = getContent_R(_reduction.get(0).asReduction(), "").trim();
				Token opToken = _reduction.get(1).asReduction().get(0);
				String opAsStr = opToken.asString();
				String content = var + " <- ";
				if (!opAsStr.equals("=")) {
					String opr = opAsStr.substring(0, opAsStr.length()-1);
					content += var + " " + (opr.equals("%") ? " mod " : opr) ;
				}
				content = getContent_R(_reduction.get(2).asReduction(), content).trim();
				//System.out.println(ruleName + ": " + content);
				// In case of a variable declaration get rid of the trailing semicolon
				//if (content.endsWith(";")) {
				//	content = content.substring(0, content.length() - 1).trim();
				//}
				_parentNode.addElement(this.equipWithSourceComment(new Instruction(translateContent(content)), _reduction));
			}
			else if (
					// Autoincrement / autodecrement (i++, i--, ++i, --i)
					ruleId == RuleConstants.PROD_POSTFIXEXP_PLUSPLUS
					||
					ruleId == RuleConstants.PROD_POSTFIXEXP_MINUSMINUS
					||
					ruleId == RuleConstants.PROD_UNARYEXP_PLUSPLUS
					||
					ruleId == RuleConstants.PROD_UNARYEXP_MINUSMINUS
					)
			{
				// Token index of the variable (CAUTION: There could be a more complex operand!)
				int lvalIx = (ruleId >= RuleConstants.PROD_UNARYEXP_PLUSPLUS) ? 1 : 0;
				// Variable name
				String lval = getContent_R(_reduction.get(lvalIx).asReduction(), "");
				// Operator + or - ?
				String opr = (ruleId == RuleConstants.PROD_UNARYEXP_PLUSPLUS || ruleId == RuleConstants.PROD_POSTFIXEXP_PLUSPLUS) ? " + " : " - ";
				String content = lval + " <- " + lval + opr + "1";
				_parentNode.addElement(this.equipWithSourceComment(new Instruction(translateContent(content)), _reduction));
			}
			else if (
					// Variable declaration with or without initialization? Might also be a typedef though!
					ruleId == RuleConstants.PROD_DECLARATION_SEMI
					// <Declaration> ::= <Decl Specifiers> <InitDeclList> ';'
					||
					ruleId == RuleConstants.PROD_DECLARATION_SEMI2
					// <Declaration> ::= <Decl Specifiers> ';'
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
				boolean isTypedef = processTypes(_reduction, ruleId, parentNode, isGlobal, tmpTypes, true);
				String type = tmpTypes.concatenate();
				// START KGU#407 2017-06-22: Enh.#420 grope for possible source comments
				String comment = this.retrieveComment(_reduction);
				// END KGU#407 2017-06-22
				Reduction declReduc = _reduction.get(1).asReduction();
				// Now concern on the declarations of the list (FIXME: test for last argument is too vague)
				// START KGU#545 2018-07-05: There might still be pointer symbols!
				//buildDeclsOrAssignments(declReduc, type, parentNode, comment, tmpTypes.contains("struct"));
				if (declReduc != null) {
					int declIx = declReduc.getParent().getTableIndex();
					String ptrs = "";
					while (declIx == RuleConstants.PROD_DECLARATOR) {
						ptrs += " *";
						declReduc = declReduc.get(1).asReduction();
						declIx = declReduc.getParent().getTableIndex();
					}
					// We don't want to produce variable declarations from function prototypes!
					if (declIx != RuleConstants.PROD_DIRECTDECL_LPAREN_RPAREN2 && declIx != RuleConstants.PROD_DIRECTDECL_LPAREN_RPAREN3) {
						buildDeclsOrAssignments(declReduc, type + ptrs, parentNode, comment, tmpTypes.contains("struct"), isTypedef);
					}
				}
				// END KGU#545 2018-07-05
				// CHECKME!
				if (isGlobal && root != globalRoot && !importingRoots.contains(root)) {
					importingRoots.add(root);
				}
			}
			else if (
					// Labelled instruction?
					ruleId == RuleConstants.PROD_LABELLEDSTMT_IDENTIFIER_COLON
					)
			{
				// <Labelled Stmt> ::= Identifier ':' <Statement>
				String content = _reduction.get(0).asString() + ":";
				Instruction el = new Instruction(content);
				el.setColor(Color.RED);	// will only be seen if the user enables the element
				el.setDisabled(true);
				this.equipWithSourceComment(el, _reduction);
				el.getComment().add("FIXME: Goto instructions are not supported in structured algorithms!");
				_parentNode.addElement(el);
				// Label is done, now parse the actual statement
				// FIXME: Can't we avoid recursion here?
				buildNSD_R(_reduction.get(2).asReduction(), _parentNode);
			}
			else if (
					// BREAK instruction
					ruleId == RuleConstants.PROD_JUMPSTMT_BREAK_SEMI
					)
			{
				String content = getKeyword("preLeave");
				// START KGU#1153 2024-04-17: Bugfix #1163 check context
				//_parentNode.addElement(this.equipWithSourceComment(new Jump(content.trim()), _reduction));
				Jump leave = new Jump(content.trim());
				this.equipWithSourceComment(leave, _reduction);
				Element parent = _parentNode;
				while (parent != null) {
					if (parent instanceof ILoop) {
						// We are inside a loop context, so this is a valid leave
						break;
					}
					else if (parent instanceof Case) {
						// This is meant to end a Case branch and should vanish
						leave.setColor(Color.RED);
						leave.comment.add("TODO: Restructure this CASE branch for clean end.");
						// Register this kind of Jump for final restructuring attempts
						switchBreaks.add(leave);
						break;
					}
					parent = parent.parent;
				}
				_parentNode.addElement(leave);
				// END KGU#1153 2024-04-17
			}
			else if (
					// RETURN instruction
					ruleId == RuleConstants.PROD_JUMPSTMT_RETURN_SEMI
					)
			{
				String content = getKeyword("preReturn");
				Reduction exprRed = _reduction.get(1).asReduction();
				if (exprRed.getParent().getTableIndex() != RuleConstants.PROD_EXPROPT2) { 
					content += " " + translateContent(getContent_R(exprRed, ""));
				}
				_parentNode.addElement(this.equipWithSourceComment(new Jump(content.trim()), _reduction));
			}
			else if (
					// GOTO instruction
					ruleId == RuleConstants.PROD_JUMPSTMT_GOTO_IDENTIFIER_SEMI
					)
			{
				String content = _reduction.get(0).asString() + " " + _reduction.get(1).asString();
				Jump el = new Jump(content.trim());
				this.equipWithSourceComment(el, _reduction);
				el.getComment().add("FIXME: Goto is not supported in structured algorithms!");
				el.setColor(Color.RED);
				_parentNode.addElement(el);				
			}
			else if (
					// WHILE loop?
					ruleId == RuleConstants.PROD_ITERATIONSTMT_WHILE_LPAREN_RPAREN
					 )
			{
				String content = new String();
				content = getContent_R(_reduction.get(2).asReduction(), content);
				// STARTG KGU#822 2020-03-09: Issue #835
				//While ele = new While((getKeyword("preWhile").trim() + " " + translateContent(content) + " " + getKeyword("postWhile").trim()).trim());
				While ele = new While((getOptKeyword("preWhile", false, true)
						+ translateContent(content)
						+ getOptKeyword("postWhile", true, false)).trim());
				// END KGU#822 2020-03-09
				// START KGU#407 2017-06-20: Enh. #420 - comments already here
				this.equipWithSourceComment(ele, _reduction);
				// END KGU#407 2017-06-22
				_parentNode.addElement(ele);
				
				Reduction secReduc = _reduction.get(4).asReduction();
				buildNSD_R(secReduc, ele.q);
			}
			else if (
					// REPEAT loop?
					ruleId == RuleConstants.PROD_ITERATIONSTMT_DO_WHILE_LPAREN_RPAREN_SEMI
					 )
			{
				String content = new String();
				content = getContent_R(_reduction.get(4).asReduction(), content);
				// FIXME We might look for kinds of expressions with direct negation possibility,
				// e.g. PROD_OPEQUATE_EQEQ, PROD_OPEQUATE_EXCLAMEQ, PROD_OPCOMPARE_LT, PROD_OPCOMPARE_GT
				// etc. where we could try to replace the reduction by its opposite.
				// START KGU#822 2020-03-09: Issue #835
				//Repeat ele = new Repeat((getKeyword("preRepeat").trim() + " not (" + content + ") " + getKeyword("postRepeat").trim()).trim());
				Repeat ele = new Repeat((getOptKeyword("preRepeat", false, true)
						+ Element.negateCondition(content)
						+ getOptKeyword("postRepeat", true, false)).trim());
				// END KGU#822 2020-03-09
				// START KGU#407 2017-06-20: Enh. #420 - comments already here
				this.equipWithSourceComment(ele, _reduction);
				// END KGU#407 2017-06-22
				_parentNode.addElement(ele);
				
				Reduction secReduc = _reduction.get(1).asReduction();
				buildNSD_R(secReduc, ele.q);
			}
			else if (
					// FOR loop?
					 ruleId == RuleConstants.PROD_ITERATIONSTMT_FOR_LPAREN_SEMI_SEMI_RPAREN
					 ||
					 ruleId == RuleConstants.PROD_ITERATIONSTMT_FOR_LPAREN_SEMI_RPAREN
					 )
			{
				// <Iteration Stmt> ::= for '(' <ExprOpt> ';' <ExprOpt> ';' <ExprOpt> ')' <Statement>
				// <Iteration Stmt> ::= for '(' <Declaration> <ExprOpt> ';' <ExprOpt> ')' <Statement>
				
				// get the second header part index
				int condIx = 4;
				if (ruleId == RuleConstants.PROD_ITERATIONSTMT_FOR_LPAREN_SEMI_RPAREN) {
					condIx = 3;
				}
				Subqueue body = null;
				// First try to produce an actual FOR loop.
				Element ele = this.checkAndMakeFor(_reduction.get(2), _reduction.get(condIx), _reduction.get(condIx + 2));
				int oldSize = _parentNode.getSize();
				
				if (ele != null) {
					body = ((For)ele).getBody();
				}
				else {
					// The fallback approach now is always to build WHILE loops here

					// get first part - should be an assignment...
					// We make a separate instruction out of it
					Reduction secReduc = _reduction.get(2).asReduction();
					buildNSD_R(secReduc, _parentNode);
					// Mark all offsprings of the FOR loop with a (by default) yellowish colour
					// (maybe the initialization part was empty, though!)
					for (int i = oldSize; i < _parentNode.getSize(); i++) {
						_parentNode.getElement(i).setColor(COLOR_MISC);
					}

					// get the second part - should be an ordinary condition
					String content = getContent_R(_reduction.get(condIx).asReduction(), "");
					if (content.trim().isEmpty()) {
						Forever loop = new Forever();
						ele = loop;
						body = loop.getBody();
					}
					else {
						// START KGU#822 2020-03-09: Issue #835
						//While loop = new While((getKeyword("preWhile").trim() + " " + translateContent(content) + " " + getKeyword("postWhile").trim()).trim());
						While loop = new While((getOptKeyword("preWhile", false, true)
								+ translateContent(content)
								+ getOptKeyword("postWhile", true, false)).trim());
						// END KGU#822 2020-03-09
						ele = loop;
						body = loop.getBody();
					}
					// Mark all offsprings of the FOR loop with a (by default) yellowish colour
					ele.setColor(COLOR_MISC);
				}
				
				this.equipWithSourceComment(ele, _reduction);
				_parentNode.addElement(ele);
				
				// Get and convert the body
				Reduction bodyRed = _reduction.get(condIx + 4).asReduction();
				buildNSD_R(bodyRed, body);

				if (!(ele instanceof For)) {
					// get the last part of the header now and append it to the body
					oldSize = body.getSize();	// Maybe (though little likely) the increment part is empty
					buildNSD_R(_reduction.get(condIx + 2).asReduction(), body);
					// Mark all offsprings of the FOR loop with a (by default) yellowish colour
					for (int i = oldSize; i < body.getSize(); i++) {
						body.getElement(i).setColor(COLOR_MISC);
					}
				}
			}
			else if (
					// Alternative?
					ruleId == RuleConstants.PROD_SELECTIONSTMT_IF_LPAREN_RPAREN
					||
					ruleId == RuleConstants.PROD_SELECTIONSTMT_IF_LPAREN_RPAREN_ELSE
					)
			{
				// <Selection Stmt>  ::= if '(' <Expression> ')' <Statement>
				// <Selection Stmt>  ::= if '(' <Expression> ')' <Statement> else <Statement>
				String content = getContent_R(_reduction.get(2).asReduction(), "");
				Alternative alt = new Alternative(content);
				this.equipWithSourceComment(alt, _reduction);
				_parentNode.addElement(alt);
				buildNSD_R(_reduction.get(4).asReduction(), alt.qTrue);
				if (_reduction.size() >= 7) {
					buildNSD_R(_reduction.get(6).asReduction(), alt.qFalse);
				}
			}
			else if (
					// CASE branch?
					ruleId == RuleConstants.PROD_CASESTMTS_CASE_COLON
					||
					ruleId == RuleConstants.PROD_CASESTMTS_DEFAULT_COLON
					)
			{
				// <Case Stmts> ::= case <Cond Exp> ':' <StmtList> <Case Stmts>
				// <Case Stmts> ::= default ':' <StmtList>
				buildCaseBranch(_reduction, ruleId, (Case) _parentNode.parent);
			}
			else if (
					// Case selection?
					ruleId == RuleConstants.PROD_SELECTIONSTMT_SWITCH_LPAREN_RPAREN_LBRACE_RBRACE
					)
			{
				// <Selection Stmt>  ::= switch '(' <Expression> ')' '{' <Case Stmts> '}'
				buildCase(_reduction, _parentNode); 
			}
			// Frequent stack overflows on large sources was observed
			else if (ruleId == RuleConstants.PROD_STMTLIST)
			{
				// We can easily reduce recursion overhead since the crucial <Stm List> rule is
				// right-recursive. We don't even need auxiliary data structures.
				while (_reduction.size() > 0) {
					this.buildNSD_R(_reduction.get(0).asReduction(), _parentNode);
					_reduction = _reduction.get(1).asReduction();
				}
			}
			
			// TODO add the handling of further instruction types here...
			
			// START KGU#1130 2024-03-17: Bugfix #1141 Measures against stack overflow risk
			else if (ruleId == RuleConstants.PROD_BLOCKITEMLIST
					|| ruleId == RuleConstants.PROD_TRANSLATIONUNIT2) {
				// <BlockItemList> ::= <BlockItemList> <BlockItem>
				// <Translation Unit> ::= <Translation Unit> <External Decl>
				int loopId = ruleId;
				Stack<Reduction> redStack = new Stack<Reduction>();
				do {
					redStack.push(_reduction.get(1).asReduction());
					_reduction = _reduction.get(0).asReduction();
					ruleId = _reduction.getParent().getTableIndex();
				} while (ruleId == loopId);
				buildNSD_R(_reduction, _parentNode);
				while (!redStack.isEmpty()) {
					buildNSD_R(redStack.pop(), _parentNode);
				}
			}
			// END KGU#1130 2024-03-17
			else 
			{
				if (_reduction.size() > 0)
				{
					for(int i = 0; i < _reduction.size(); i++)
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
	 * Processes a function definition from the given {@code _reduction}
	 * 
	 * @param _reduction - the {@link Reduction} of the parser
	 * @throws ParserCancelled when the user aborted the import
	 */
	private void buildFunctionDecl(Reduction _reduction) throws ParserCancelled {
		// <Function Def> ::= <Decl Specifiers> <Declarator> <DeclListOpt> <Compound Stmt>
		// Find out the name of the function
		Reduction secReduc = _reduction.get(1).asReduction();
		String content = new String();
		boolean weird = false;
		//int secRuleId = secReduc.getParent().getTableIndex();
		StringList prefix = new StringList();
		StringList suffix = new StringList();
		StringList pascal = new StringList();
		String funcId = this.getDeclarator(secReduc, prefix, suffix, pascal, null, _reduction.get(2).asReduction());
		//System.out.println(prefix.concatenate(" ") + funcId + suffix.concatenate());
		//System.out.println(pascal.concatenate());
//		int nPointers = 0;
//		// Drop redundant parentheses
//		while (secRuleId == RuleConstants.PROD_DECLARATOR || secRuleId == RuleConstants.PROD_DIRECTDECL_LPAREN_RPAREN) {
//			if (secRuleId == RuleConstants.PROD_DECLARATOR) {
//				nPointers++;
//			}
//			secReduc = secReduc.get(1).asReduction();
//			secRuleId = secReduc.getParent().getTableIndex();
//		} 
//		String funcName = null;
//		Reduction paramReduc = null;
//		switch (secRuleId) {
//		case RuleConstants.PROD_DIRECTDECL_IDENTIFIER:
//			funcName = getContent_R(secReduc, "");
//			break;
//		case RuleConstants.PROD_DIRECTDECL_LPAREN_RPAREN2:
//		case RuleConstants.PROD_DIRECTDECL_LPAREN_RPAREN3:
//			funcName = getContent_R(secReduc.get(0).asReduction(), "");
//			paramReduc = secReduc.get(2).asReduction();
//			break;
//		default: 
//			// Something weird like an array of functions or the like
//			content = getContent_R(_reduction, "");
//			weird = true;
//		}
		Root prevRoot = root;	// Cache the original root
		root = new Root();	// Prepare a new root for the (sub)routine
		root.setProgram(false);
		addRoot(root);
		// If the previous root was global and had collected elements then make the new root a potential importer
		if (prevRoot.getMethodName().equals("???") && prevRoot.children.getSize() > 0 && !this.importingRoots.contains(root)) {
			// We must have inserted some global stuff, so assume a dependency...
			this.importingRoots.add(root);
		}
		if (!weird) {
			// Is there a type specification different from void?
			Token typeToken = _reduction.get(0);
			if (typeToken.getType() == SymbolType.CONTENT) {
				content += typeToken.asString() + " ";
			}
			else {
				// FIXME: We might need a more intelligent type analysis
				content = getContent_R(typeToken.asReduction(), "").trim() + " ";
				content = content.replaceAll("(^|.*\\W)static(\\s+.*)", "$1$2");
				content = content.replaceAll("(^|.*\\W)const(\\s+.*)", "$1$2");
				// START KGU#668 2019-02-28: Workaround #690 to get rid of struct keywords here
				content = content.replaceAll("(^|.*\\W)struct(\\s+.*)", "$1$2");
				// END KGU#668 2019-02-28
			}
			content += prefix.concatenate(" ").trim();
			content = content.trim();
			// Result type "void" should be suppressed
			if (content.equals("void")) {
				content = "";
			}
			//content += funcId + "(";
			//String params = this.getParamList(paramReduc, null, true, _reduction.get(2).asReduction());
//			if (paramReduc != null) {
//				StringList paramList = new StringList();
//				String ellipse = "";
//				int ruleId = paramReduc.getParent().getTableIndex(); 
//				if (ruleId == RuleConstants.PROD_PARAMTYPELIST_COMMA_DOTDOTDOT) {
//					ellipse = ", ...";
//					paramReduc = paramReduc.get(0).asReduction();
//					ruleId = paramReduc.getParent().getTableIndex();
//				}
//				switch (ruleId) {
//				case RuleConstants.PROD_IDLISTOPT2:
//					// Empty argument list
//					break;
//				case RuleConstants.PROD_IDENTIFIERLIST_IDENTIFIER:	// FIXME does is work for this rule?
//				case RuleConstants.PROD_IDENTIFIERLIST_COMMA_IDENTIFIER:
//					// Ancient function definition: type foo(a, b, c) type1 a; type2 b; type3 c; {...}
//					params = getContent_R(paramReduc, "");
//					{
//						StringList paramDecls = getDeclsFromDeclList(_reduction.get(2).asReduction());
//						StringList paramNames = StringList.explode(params, ",");
//						// Sort the parameter declarations according to the arg list (just in case...)
//						if (paramDecls.count() == paramNames.count()) {
//							StringList paramsOrdered = new StringList();
//							for (int p = 0; p < paramNames.count(); p++) {
//								Matcher pm = Pattern.compile("(^|.*?\\W)" + paramNames.get(p).trim() + ":.*").matcher("");
//								for (int q = 0; q < paramDecls.count(); q++) {
//									String pd = paramDecls.get(q);
//									if (pm.reset(pd).matches()) {
//										paramsOrdered.add(pd);
//										break;
//									}
//								}
//								if (paramsOrdered.count() < p+1) {
//									paramsOrdered.add(paramNames.get(p));
//								}
//							}
//							params = paramsOrdered.concatenate("; ");
//						}
//					}
//					break;
//				case RuleConstants.PROD_PARAMETERLIST_COMMA:
//					// More than one parameter
//					do {
//						String param = getContent_R(paramReduc.get(2).asReduction(), "");
//						paramReduc = paramReduc.get(0).asReduction();
//						ruleId = paramReduc.getParent().getTableIndex();
//						paramList.add(param);
//					} while (ruleId == RuleConstants.PROD_PARAMETERLIST_COMMA);
//					// no break here!
//				default: // Should be a <Parameter Decl>
//					paramList.add(getContent_R(paramReduc, ""));
//					params = paramList.reverse().concatenate(", ") + ellipse;
//					break;
//				}
//			}
//			if (params.trim().equals("void")) {
//				params = "";
//			}
//			content += params + ")";
			content += " " + funcId;
			String params = suffix.concatenate().trim();
			if (params.equals("(void)")) {
				params = "()";
			}
			content += params;
		}
		root.setText(content.trim());
		this.equipWithSourceComment(root, _reduction);
		if (weird) {
			root.comment.add("UNSUPPORTED SHAPE OF FUNCTION/PROCEDURE HEADER!");
		}
		Reduction bodyRed = _reduction.get(3).asReduction();
		if (bodyRed.getParent().getTableIndex() == RuleConstants.PROD_COMPOUNDSTMT_LBRACE_RBRACE)
		{
			buildNSD_R(bodyRed.get(1).asReduction(), root.children);
		}
		// Restore the original root
		root = prevRoot;
	}
	
	/**
	 * Is to extract the declarations from an old-style parameter declaration list
	 * ({@code <DeclarationList>}) and to convert them into Structorizer-compatible
	 * syntax (Pascal style).
	 * @param _declRed - the {@link Reduction} representing a {@code <Struct Decl>} rule.
	 * @return {@link StringList} of the declaration strings in Structorizer syntax
	 * @throws ParserCancelled when the user aborted the import
	 */
	private StringList getDeclsFromDeclList(Reduction _declRed) throws ParserCancelled
	{
		// FIXME!
		StringList decls = new StringList();
		while (_declRed != null && _declRed.size() > 0) {
			int ruleId = _declRed.getParent().getTableIndex();
			Reduction varDecl = _declRed;
			if (ruleId == RuleConstants.PROD_DECLARATIONLIST) {
				varDecl = _declRed.get(1).asReduction();
				_declRed = _declRed.get(0).asReduction();
				ruleId = _declRed.getParent().getTableIndex();
			}
			else {
				_declRed = null;
			}
//			int nameIx = varDecl.size() - 3;
			String type = "";
			if (varDecl.get(0).getType() == SymbolType.CONTENT) {
				type = varDecl.get(0).asString();
			}
			else {
				type = getTypeSpec(varDecl.get(0).asReduction());
			}
//			StringList compNames = new StringList();
			// FIXME May there be declaration groups? May type specifiers disrupt a group here?
			//decls.add(getContent_R(varDecl.get(1).asReduction(), type));
			decls.add(this.buildDeclOrAssignment(varDecl.get(1).asReduction(), type, null, null, true, false));
//			Reduction varRed = varDecl.get(1).asReduction();
//			String name = varRed.get(0).asString();
//			String index = getContent_R(varRed.get(1).asReduction(), "").trim();
//			if (!index.isEmpty()) {
//				if (index.equals("[]")) {
//					index = "";
//				}
//				components.add(name + ": array" + index + " of " + type);
//			}
//			else {
//				compNames.add(name);
//				while (varList.size() > 0) {
//					varRed = varList.get(1).asReduction();
//					String pointers = getContent_R(varRed.get(0).asReduction(), "").trim();
//					name = varRed.get(1).asReduction().get(0).asString();
//					index = getContent_R(varRed.get(1).asReduction().get(1).asReduction(), "").trim();
//					if (!index.isEmpty() || !pointers.isEmpty()) {
//						if (compNames.count() > 0) {
//							components.add(compNames.concatenate(", ") + ": " + type);
//							compNames.clear();
//						}
//						if (index.equals("[]")) {
//							index = "array of ";
//						}
//						else if (!index.isEmpty()) {
//							index = "array " + index + " of ";
//						}
//						components.add(name + ": " + index + type + pointers);
//					}
//					else {
//						compNames.add(name);
//					}
//					varList = varList.get(2).asReduction();
//				}
//				if (compNames.count() > 0) {
//					components.add(compNames.concatenate(", ") + ": " + type);
//				}
//			}
		}
		return decls;
	}
	
	/**
	 * 
	 * @param _declSpecRed
	 * @return
	 * @throws ParserCancelled when the user aborted the import
	 */
	private String getTypeSpec(Reduction _declSpecRed) throws ParserCancelled {
		// FIXME: Drop superfluous stuff
		StringList parts = new StringList();
		while (_declSpecRed.getParent().getHead().toString().equals("<Decl Specifiers>")) {
			Reduction partRed = _declSpecRed.get(0).asReduction();
			String part = getContent_R(partRed, "").trim();
			switch(_declSpecRed.getParent().getTableIndex()) {
			case RuleConstants.PROD_DECLSPECIFIERS:
				// <Decl Specifiers> ::= <Storage Class> <Decl Specs>
			case RuleConstants.PROD_DECLSPECIFIERS3:
				// <Decl Specifiers> ::= <Type Qualifier> <Decl Specs>
				if (part.equals("typedef") || part.equals("const")) {
					parts.add(part);
				}
				break;
			case RuleConstants.PROD_DECLSPECIFIERS2:
				// <Decl Specifiers> ::= <Type Specifier> <Decl Specs>
				parts.add(part);
				break;
			}
			_declSpecRed = _declSpecRed.get(1).asReduction();
		}
		return parts.concatenate(" ");
	}
	
	/**
	 * Converts a rule of type PROD_NORMALSTM_SWITCH_LPAREN_RPAREN_LBRACE_RBRACE
	 * into the skeleton of a Case element. The case branches will be handled
	 * separately
	 * 
	 * @param _reduction - Reduction rule of a switch instruction
	 * @param _parentNode - the Subqueue this Case element is to be appended to
	 * 
	 * @throws ParserCancelled
	 * 
	 * @see {@link #buildCaseBranch(Reduction, int, Case)}
	 */
	private void buildCase(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled
	{
		// Rule: <Selection Stmt> ::= switch '(' <Expression> ')' '{' <Case Stmts> '}
		//
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
		// We should wipe off end-standing breaks, however, before we copy.
		String content = new String();
		// Put the discriminator into the first line of content
		// START KGU#822 2020-03-09: Issue #835
		//content = getKeyword("preCase")+getContent_R(_reduction.get(2).asReduction(), content)+getKeyword("postCase");
		content = getOptKeyword("preCase", false, true)
				+ getContent_R(_reduction.get(2).asReduction(), content)
				+ getOptKeyword("postCase", true, false);
		// END KGU#822 2020-03-09

		// How many branches has the CASE element? We must count the non-empty statement lists!
		Reduction sr = _reduction.get(5).asReduction();	// <Case Stmts>
		int j = 0;
		//System.out.println(sr.getParentRule().getText());  // <<<<<<<
		while (sr.getParent().getTableIndex() == RuleConstants.PROD_CASESTMTS_CASE_COLON)
		{
			// sr: <Case Stmts> ::= case <Cond Exp> ':' <StmtList> <Case Stmts>
			Reduction stmList = sr.get(3).asReduction();	// <StmtList>
			if (stmList.getParent().getTableIndex() == RuleConstants.PROD_STMTLIST) {
				// non-empty statement list, so we will have to set up a branch
				j++;
				content += "\n??";
			}
			sr = sr.get(4).asReduction();	// <Case Stmts>
		}

		if (sr.getParent().getTableIndex() == RuleConstants.PROD_CASESTMTS_DEFAULT_COLON)
		{
			// <Case Stmts> ::= default ':' <StmtList>
			content += "\ndefault";
		}
		else {
			// <Case Stmts> ::=
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

		// Actually create the selector branches
		Reduction secReduc = _reduction.get(5).asReduction();	// <Case Stmts>
		buildNSD_R(secReduc, (Subqueue) ele.qs.get(0));

		// DEBUG: Is it possible that j differs from ele.qs.size()
		if (j != ele.qs.size()) {
			System.out.println("Counted branch n° differs from created");
		}
		// Now it's time to clean and combine the branches
		// START KGU#1153 2024-04-18: Bugfix #1163: prepare combination
		boolean[] closedBranches = new boolean[ele.qs.size()];
		// END KGU#1153 2024-04-18
		// In theory, all branches should end with a break instruction
		// unless they end with return or exit. Drop the break instructions
		// (and only these) now.
		for (int i = 0; i < ele.qs.size(); i++) {
			Subqueue sq = ele.qs.get(i);
			int size = sq.getSize();
			// START KGU#1153 2024-04-18: Bugfix #1163
			closedBranches[i] = false;
			// END KGU#1153 2024-04-18
			if (size > 0) {
				Element el = sq.getElement(size-1);
				// START KGU#1153 2024-04-18: Bugfix #1163
				//if (el instanceof Jump && ((Jump)el).isLeave()) {
				//	sq.removeElement(size-1);
				//}
				if (el instanceof Jump) {
					if (switchBreaks.contains(el)) {
						sq.removeElement(size-1);
						switchBreaks.remove(el);
					}
					closedBranches[i] = true;
				}
				else if (!el.mayPassControl() && (el instanceof Alternative || el instanceof Case)) {
					this.effaceTerminalSwitchBreaks(el);
					closedBranches[i] = true;
				}
				else if (!sq.isReachable(size-1, false, null)) {
					closedBranches[i] = true;
				}
				// END KGU#1153 2024-04-18
			}
		}

		// START KGU#1153 2024-04-18: Bugfix #1163
		// From last to first, for all open branches, copy the respective
		// successor branch to its end
		for (int i = ele.qs.size()-2; i >= 0; i--) {
			if (!closedBranches[i]) {
				Subqueue sq0 = ele.qs.get(i);
				Subqueue sq1 = ele.qs.get(i+1);
				// TODO: Here we might decide between merging and copying
				for (int k = 0; k < sq1.getSize(); k++) {
					Element el = sq1.getElement(k).copy();	// FIXME: Need a new Id!
					sq0.addElement(el);
				}
			}
		}
		
		resolveConditionalSwitchBreaks(ele);
		// END KGU#1153 2024-04-18
		
		// cut off default, if possible
		if (((Subqueue) ele.qs.get(j-1)).getSize()==0)
		{
			ele.getText().set(ele.getText().count()-1,"%");
		}

	}

	// START KGU#1153 2024-04-18: Bugfix #1163 Address some non-trivial constellations
	/**
	 * Resolves constellations where a conditioned switch break (illegal in Structorizer)
	 * is placed at the end of one branch of an Alternative that is a direct element of
	 * one of the Case branches. Accesses and possibly modifies {@link #switchBreaks}.
	 *
	 * @param _case - the owning {@link Case} element
	 */
	private void resolveConditionalSwitchBreaks(Case _case) {
		/* In the following we try to find some further breaks that can be resolved
		 * We concentrate on those that end one of the branches of an Alternative
		 * which is an immediate member of a Case branch subqueue. In this case
		 * we can append (move) all subsequent elements to the end of the other
		 * branch of the Alternative, remove the break and possibly swap the
		 * Alternative branches if the break had resided in the T branch and this
		 * became empty.
		 * If more than one of such constellations happen to occur within the same
		 * Case branch then it i important to start with the last of them, otherwise
		 * the conditions for its recognition would be spoiled.
		 * Therefore we first sort all of these constallations by their indices within
		 * their respective branches.
		 */
		// First find out the maximum length of all branches.
		int maxLen = 0, len;
		for (int i = 0; i < _case.qs.size(); i++) {
			if ((len = _case.qs.get(i).getSize()) > maxLen) {
				maxLen = len;
			}
		}
		@SuppressWarnings("unchecked")
		HashSet<Jump>[] removedBreaks = new HashSet[maxLen];
		for (int i = 0; i < maxLen; i++) {
			removedBreaks[i] = new HashSet<Jump>();
		}
		// Now register all relevant break elements with the index of the Alternative
		for (Jump leave: switchBreaks) {
			Subqueue sq0, sq1;
			Alternative alt;
			if (leave.parent instanceof Subqueue
					&& (sq0 = (Subqueue)leave.parent).parent instanceof Alternative
					&& (alt = (Alternative)sq0.parent).parent instanceof Subqueue
					&& sq0.getElement(sq0.getSize()-1) == leave
					&& (sq1 = (Subqueue)alt.parent).parent == _case) {
				int ix = sq1.getIndexOf(alt);
				removedBreaks[ix].add(leave);
			}
		}
		// Now we rearrange the Alternatives and their subsequent elements to get a clean branch
		for (int i = maxLen - 1; i >= 0; i--) {
			for (Jump leave: removedBreaks[i]) {
				Subqueue sq0 = (Subqueue)leave.parent;
				Alternative alt = (Alternative)sq0.parent;
				Subqueue sq = (Subqueue)alt.parent;
				// Remove the break
				sq0.removeElement(sq0.getSize()-1);
				switchBreaks.remove(leave);	// Registration no longer needed
				Subqueue sq1 = (alt.qTrue == sq0) ? alt.qFalse : alt.qTrue;
				if (sq0.getSize() == 0 && sq0 == alt.qTrue) {
					// Swap Alternative branches and invert condition
					alt.qTrue = sq1;
					alt.qFalse = sq0;
					alt.setText(Element.negateCondition(alt.getUnbrokenText().concatenate()));
				}
				// Append the subsequent elements to the opposite alt branch
				for (int k = i + 1; k < sq.getSize(); k++) {
					sq1.addElement(sq.getElement(k));
				}
				// Remove the subsequent elements from the case branch
				for (int k = sq.getSize() - 1; k > i; k--) {
					sq.removeElement(k);
				}
			}
		}
	}
	// END KGU#1153 2024-04-18

	/**
	 * Constructs one or more Case branches from the given {@link Reduction}
	 * {@code _reduction} into the {@link Case} element {@code _case}.
	 * 
	 * @param _reduction - a {@code <Case Stmts>} Reduction
	 * @param _ruleId - the related production table index
	 * @param _case - the "owning" {@link Case} object
	 * @throws ParserCancelled if the user happened to abort the import
	 */
	private void buildCaseBranch(Reduction _reduction, int _ruleId, Case _case) throws ParserCancelled
	{
		// Relevant grammar rules:
		// <Case Stmts> ::= case <Selector> ':' <StmtList> <Case Stmts>
		// <Case Stmts> ::= default ':' <StmtList>

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
		if (_ruleId == RuleConstants.PROD_CASESTMTS_CASE_COLON) {
			// <Case Stmts> ::= case <Cond Expr> ':' <StmtList> <Case Stmts>
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
			stmListIx = 3;	// <StmtList> index for explicit branch
		}
		// Add the branch content
		Reduction secReduc = _reduction.get(stmListIx).asReduction();
		Subqueue sq = (Subqueue) _case.qs.get(iNext-1);
		// Fill the branch with the instructions (if there are any)
		buildNSD_R(secReduc, sq);
				
		// START KGU#1153 2024-04-18: Bugfix #1163
		/* We got into a conflict here: It does not make sense to copy
		 * un-cleaned branches. On the other hand, we should not have
		 * removed the final leave element if we want to use it for the
		 * detection of closed branches. So the only sensible way is to
		 * postpone the copying to buildCase()
		 */
		// Which is the last branch ending with jump instruction?
		//int lastClosedBranch = -1;
		//for (int i = iNext-2; i >= 0; i--) {
		//	int size = _case.qs.get(i).getSize();
		//	if (size > 0 && (!_case.qs.get(i).getElement(size-1).mayPassControl())) {
		//		lastClosedBranch = i;
		//		break;
		//	}
		//}
		//
		// Append copies of the elements of the new case to all cases still not terminated
		//for (int i = lastClosedBranch+1; i < iNext-1; i++) {
		//	Subqueue sq1 = _case.qs.get(i);
		//	for (int j = 0; j < sq.getSize(); j++) {
		//		Element el = sq.getElement(j).copy();	// FIXME: Need a new Id!
		//		sq1.addElement(el);
		//	}
		//}
		// END KGU#1153 2024-04-18
		
		// If this is an explicit case branch then the last token holds the subsequent branches
		if (_ruleId == RuleConstants.PROD_CASESTMTS_CASE_COLON) {
			// We may pass an arbitrary subqueue, the case branch rule goes up to the Case element anyway
			buildNSD_R(_reduction.get(stmListIx+1).asReduction(), _case.qs.get(0));
		}
		
	}
	
	// START KGU#1153 2024-04-17: Bugfix #1163
	/**
	 * Recursively eliminates all {@link #switchBreaks} elements that are placed
	 * at the end of some branch of the forking element {@code forkEl}.
	 * 
	 * @param forkEl - either an {@link Alternative} or a {@link Case}
	 */
	private void effaceTerminalSwitchBreaks(Element forkEl)
	{
		if (forkEl instanceof Alternative) {
			removeTerminalSwitchBreaks(((Alternative)forkEl).qTrue);
			removeTerminalSwitchBreaks(((Alternative)forkEl).qFalse);
		}
		else if (forkEl instanceof Case) {
			for (int i = 0; i < ((Case)forkEl).qs.size(); i++) {
				removeTerminalSwitchBreaks(((Case)forkEl).qs.get(i));
			}
		}
	}
	/**
	 * Recursively eliminates all {@link #switchBreaks} elements that are placed
	 * at the end of Subqueue {@code sq}.
	 * 
	 * @param sq - a subqueue the end of which is to be cleaned
	 */
	private void removeTerminalSwitchBreaks(Subqueue sq) {
		if (sq.getSize() > 0) {
			Element lastEl = sq.getElement(sq.getSize()-1);
			if (lastEl instanceof Jump && switchBreaks.contains(lastEl)) {
				sq.removeElement(sq.getSize()-1);
				switchBreaks.remove(lastEl);
			}
			else if (lastEl instanceof Alternative || lastEl instanceof Case) {
				effaceTerminalSwitchBreaks(lastEl);
			}
		}
	}
	// END KGU#1153 2024-04-17

	@Override
	protected String[] checkForIncr(Token incrToken) throws ParserCancelled
	{
		String[] parts = null;
		if (incrToken.getType() == SymbolType.NON_TERMINAL) {
			// Should be some kind of <ExprOpt>
			Reduction incrRed = incrToken.asReduction();
			if (incrRed.size() > 0) {
				Token nxtToken;
				int ruleId = incrRed.getParent().getTableIndex();
				switch (ruleId) {
				case RuleConstants.PROD_POSTFIXEXP_PLUSPLUS:
				case RuleConstants.PROD_POSTFIXEXP_MINUSMINUS:
					nxtToken = incrRed.get(0);
					if (nxtToken.getType() == SymbolType.NON_TERMINAL && nxtToken.asReduction().getParent().getTableIndex() == RuleConstants.PROD_VALUE_IDENTIFIER) {
						parts = new String[3];
						parts[0] = nxtToken.asReduction().get(0).asString();
						parts[1] = (ruleId == RuleConstants.PROD_POSTFIXEXP_PLUSPLUS) ? "+" : "-";
						parts[2] = "1";
					}
					break;
				case RuleConstants.PROD_UNARYEXP_PLUSPLUS:
				case RuleConstants.PROD_UNARYEXP_MINUSMINUS:
					nxtToken = incrRed.get(1);
					if (nxtToken.getType() == SymbolType.NON_TERMINAL && nxtToken.asReduction().getParent().getTableIndex() == RuleConstants.PROD_VALUE_IDENTIFIER) {
						parts = new String[3];
						parts[0] = nxtToken.asReduction().get(0).asString();
						parts[1] = (ruleId == RuleConstants.PROD_UNARYEXP_PLUSPLUS) ? "+" : "-";
						parts[2] = "1";
					}
					break;
				case RuleConstants.PROD_ASSIGNEXP:
					// The first part must be an id
					nxtToken = incrRed.get(0);
					if (nxtToken.getType() == SymbolType.NON_TERMINAL 
							&& nxtToken.asReduction().getParent().getTableIndex() == RuleConstants.PROD_VALUE_IDENTIFIER) {
						parts = new String[3];
						parts[0] = nxtToken.asReduction().get(0).asString();
						// Now identify the operator
						nxtToken = incrRed.get(1);
						switch (nxtToken.asReduction().getParent().getTableIndex()) {
						case RuleConstants.PROD_ASSIGNOP_EQ: 
						{
							// Now we must have an addition or subtraction on the right side
							// and its first operand must be identical to parts[0]
							Reduction rightRed = incrRed.get(2).asReduction();
							ruleId = rightRed.getParent().getTableIndex();
							if ((ruleId == RuleConstants.PROD_ADDEXP_PLUS || ruleId == RuleConstants.PROD_ADDEXP_MINUS)
									&& this.getContent_R(rightRed.get(0).asReduction(), "").trim().equals(parts[0])) {
								parts[1] = (ruleId == RuleConstants.PROD_ADDEXP_PLUS) ? "+" : "-";
								nxtToken = rightRed.get(2);
							}
							else {
								parts = null;
							}
						} // case RuleConstants.PROD_ASSIGNOP_EQ
						break;
						case RuleConstants.PROD_ASSIGNOP_PLUSEQ:
							parts[1] = "+";
							nxtToken = incrRed.get(2);
							break;
						case RuleConstants.PROD_ASSIGNOP_MINUSEQ:
							parts[1] = "-";
							nxtToken = incrRed.get(2);
							break;
						default:
							// Not allowed
							parts = null;
						}
						if (parts != null) {
							// Now check the last operand - must be an int literal
							String opdStr = this.getContent_R(nxtToken.asReduction(), "").trim();
							try {
								int opd = Integer.parseInt(opdStr);
								if (opd < 0) {
									opdStr = Integer.toString(-opd);
									parts[1] = (parts[1].equals("+")) ? "-" : "+";
								}
								parts[2] = opdStr;
							}
							catch (NumberFormatException ex) {
								parts = null;
							}
						} // if (parts != null)
						break;
					} // if (nxtToken.getType() == SymbolType.NON_TERMINAL && ...)
				} // switch (ruleId)
			} // if (incrRed.size() > 0)
		} // if (incrToken.getType() == SymbolType.NON_TERMINAL)
		return parts;
	}

	@Override
	protected String checkForCond(Token condToken, String id, boolean upward) throws ParserCancelled
	{
		String lastVal = null;
		if (condToken.getType() == SymbolType.NON_TERMINAL) {
			// Should be some kind of <ExprOpt>
			Reduction condRed = condToken.asReduction();
			int ruleId = condRed.getParent().getTableIndex();
			if ((upward && (ruleId == RuleConstants.PROD_RELATEXP_LT || ruleId == RuleConstants.PROD_RELATEXP_LTEQ)
					|| !upward && (ruleId == RuleConstants.PROD_RELATEXP_GT || ruleId == RuleConstants.PROD_RELATEXP_GTEQ))
					&& this.getContent_R(condRed.get(0).asReduction(), "").trim().equals(id)) {
				lastVal = this.getContent_R(condRed.get(2).asReduction(), "");
				if (ruleId == RuleConstants.PROD_RELATEXP_LT) {
					lastVal += " - 1";
				}
				else if (ruleId == RuleConstants.PROD_RELATEXP_GT) {
					lastVal += " + 1";
				}
			}
		}		
		return lastVal;
	}

	@Override
	protected String checkForInit(Token initToken, String id) throws ParserCancelled
	{
		String firstVal = null;
		Reduction initRed = null;
		if (initToken.getType() == SymbolType.NON_TERMINAL && (initRed = initToken.asReduction()).size() > 0) {
			// Now there are two cases: <ExprOpt> or <Declarator>, first we try <ExprOpt>
			int ruleId = initRed.getParent().getTableIndex();
			if (ruleId == RuleConstants.PROD_ASSIGNEXP
					&& initRed.get(1).asReduction().getParent().getTableIndex() == RuleConstants.PROD_ASSIGNOP_EQ) {
				if (this.getContent_R(initRed.get(0).asReduction(), "").trim().equals(id)) {
					firstVal = this.getContent_R(initRed.get(2).asReduction(), "");
				}
			}
			else if (ruleId == RuleConstants.PROD_DECLARATION_SEMI 
					&& initRed.get(1).asReduction().getParent().getTableIndex() != RuleConstants.PROD_INITDECLLIST_COMMA){
				initRed = initRed.get(1).asReduction();
				ruleId = initRed.getParent().getTableIndex();
				if (ruleId == RuleConstants.PROD_INITDECLARATOR_EQ &&
					id.equals(this.getDeclarator(initRed.get(0).asReduction(), null, null, null, null, null))) {
					firstVal = this.getContent_R(initRed.get(2).asReduction(), "");
				}
				//this.analyseDeclaration(_reduction, _pascalType, _parentNode, _forceDecl, _something)
			}			
		}
		return firstVal;
	}


	/**
	 * Converts a declaration list and creates a sequence of {@link Instruction} elements into
	 * {@code _parentNode if given}.  
	 * @param _reduction - an {@code <InitDeclList>} rule, may be left-recursive
	 * @param _type - the common data type as string
	 * @param _parentNode - the {@link Subqueue} the built Instruction is to be appended to or null
	 * @param _comment - a retrieved source code comment to be placed in the element or null
	 * @param _forceDecl - if a declaration must be produced (e.g. in case of a struct type)
	 * @param _asTypeDef - if a type definition is to be created
	 * @return - the {@link StringList} of declarations
	 * @throws ParserCancelled 
	 */
	private StringList buildDeclsOrAssignments(Reduction _reduction, String _type, Subqueue _parentNode, String _comment,
			boolean _forceDecl, boolean _asTypeDef) throws ParserCancelled {
		// FIXME!
		log("\tanalyzing <InitDeclList> ...\n", false);
		StringList declns = new StringList();
		// Resolve the left recursion
		LinkedList<Reduction> decls = new LinkedList<Reduction>();
		while (_reduction != null) {
			if (_reduction.getParent().getTableIndex() == RuleConstants.PROD_INITDECLLIST_COMMA) {
				// <InitDeclList> ::= <InitDeclList> ',' <Init Declarator>
				decls.addFirst(_reduction.get(2).asReduction());
				_reduction = _reduction.get(0).asReduction();
			}
			else {
				decls.addFirst(_reduction);
				_reduction = null;
			}
		}
		// Now derive the declarations
		for (Reduction red: decls) {
			declns.add(buildDeclOrAssignment(red, _type, _parentNode, _comment, _forceDecl, _asTypeDef));
		}
		log("\t<InitDeclList> done.\n", false);
		return declns;
	}

	private static final Matcher MTCHR_EXTERN = Pattern.compile("(^|.*\\W)extern(\\s+)(.*)").matcher("");
	private static final Matcher MTCHR_STATIC = Pattern.compile("(^|.*\\W)static(\\s+)(.*)").matcher("");
	private static final Matcher MTCHR_REGISTER = Pattern.compile("(^|.*\\W)register(\\s+)(.*)").matcher("");
	
	/**
	 * Converts a rule with head {@code <Init Declarator>} (as part of a declaration) and casts it
	 * into an Instruction element added to {@code _parentNode} if given.  
	 * @param _reduc - the Reduction object (PROD_VAR_ID or PROD_VAR_ID_EQ)
	 * @param _type - the data type as string
	 * @param _parentNode - the {@link Subqueue} the built Instruction is to be appended to or null
	 * @param _comment - a retrieved source code comment to be placed in the element or null
	 * @param _forceDecl - if a declaration must be produced (e.g. in case of a struct type)
	 * @param _asTypeDef - if a type definition is to be created
	 * @return the built declaration or assignment
	 * @throws ParserCancelled 
	 */
	private String buildDeclOrAssignment(Reduction _reduc, String _type, Subqueue _parentNode, String _comment, boolean _forceDecl, boolean _asTypeDef) throws ParserCancelled
	{
		boolean isGlobal = false;
		StringList extraComments = new StringList();
		if (MTCHR_EXTERN.reset(_type).matches()) {
			extraComments.add("extern");
			_type = MTCHR_EXTERN.replaceAll("$1$2$3");
			isGlobal = true;
		}
		if (MTCHR_STATIC.reset(_type).matches()) {
			extraComments.add("static");
			_type = MTCHR_STATIC.replaceAll("$1$2$3");
			isGlobal = true;
		}
		if (MTCHR_REGISTER.reset(_type).matches()) {
			extraComments.add("register");
			_type = MTCHR_REGISTER.replaceAll("$1$2$3");
		}
		if (_type.equals("extern") || _type.equals("static")) {
			System.out.println("C99Parser(1489): extern or static found"); 
		}
		boolean isConstant = _type != null && _type.startsWith("const ");	// Is it sure that const will be at the beginning?
		int ruleId = _reduc.getParent().getTableIndex();
		String content = getContent_R(_reduc, "");	// Default (and for easier testing)
		String id = "";
		// FIXME: There may be pointer symbols on the left. Does this make sense?
		String expr = null;
		if (isConstant) {
			_type = _type.substring("const ".length());
		}
		if (ruleId == RuleConstants.PROD_INITDECLARATOR_EQ) {
			// Initialized declaration
			log("\ttrying <Declarator> '=' <Initializer> ...\n", false);
			content = this.getContent_R(_reduc.get(0).asReduction(), "");	// Default
			// START KGU#651 2019-02-13: Bugfix #678: Check for array declarations
			//id = this.getDeclarator(_reduc.get(0).asReduction(), null, null, null, _parentNode, null);
			//expr = this.getContent_R(_reduc.get(2).asReduction(), "").trim();
			StringList pascalType = null;
			if (_type != null && content.contains("[") && content.endsWith("]")) {
				pascalType = new StringList();
			}
			id = this.getDeclarator(_reduc.get(0).asReduction(), null, null, pascalType, _parentNode, null);
			expr = this.getContent_R(_reduc.get(2).asReduction(), "").trim();
			if (pascalType != null && !pascalType.isEmpty() && pascalType.get(0).contains("array")) {
				if (_type.equals("char") && expr.startsWith("\"") && expr.endsWith("\"")) {
					if (_comment == null) {
						_comment = "(original declaration: char " + content + ")";
					}
					else {
						_comment += "\n(original declaration: char " + content + ")";
					}
					_type = "string";
				}
				else {
					_type = pascalType.getLongString() + _type;
				}
			}
			// END KGU#651 2019-02-13
			_reduc = _reduc.get(0).asReduction();
			ruleId = _reduc.getParent().getTableIndex();
		}
		else {
			// Simple declaration - if allowed then make it to a Pascal decl.
			log("\ttrying <Declarator> ...\n", false);
			// START KGU#651 2019-02-13: Bugfix #678 - array declarations hadn't been imported properly
			//id = this.getDeclarator(_reduc, null, null, null, _parentNode, null);
			StringList asPascal = new StringList();
			id = this.getDeclarator(_reduc, null, null, asPascal, _parentNode, null);
			// START KGU#651/KGU#1080 2023-09-29: Bugfix #678,#1089 Don't insert both pascal and C pointer symbols
			//if (!asPascal.isEmpty()) {
			if (!asPascal.isEmpty() && (ruleId != RuleConstants.PROD_DECLARATOR || !_forceDecl && !this.optionImportVarDecl)) {
			// END KGU#651/KGU#1080 2023-09-29
				_type = asPascal.getLongString() + " " + _type;
			}
			// END KGU#651 2019-02-13
		}
		_forceDecl = this.optionImportVarDecl || _forceDecl;
		TypeMapEntry typeEntry = typeMap.get(":" + _type.trim());
		boolean isStruct = typeEntry != null && typeEntry.isRecord();
		if (_forceDecl || isStruct) {
			if (ruleId == RuleConstants.PROD_DECLARATOR) {
				log("\ttrying <Pointer> <Direct Decl> ...\n", false);
				// This should be the <Pointers> token...
				_type = this.getContent_R(_reduc.get(0).asReduction(), _type);
				//id = this.getDeclarator(_reduc, null, null, null, _parentNode, null);
			}
			if (_asTypeDef) {
				content = "type " + id + " = " + _type;
				// START KGU#1080b 2023-09-27: Bugfix #1089.2 Avoid redundant entries
				if (id.equals(_type)) {
					content = "";
				}
				// END KGU#1080b 2023-09-27
			}
			else if (isConstant) {
				content = "const " + id + ": " + _type;
			}
			else if (_parentNode == null) {
				// This seems to be for a component list
				content = id + ": " + _type;
			}
			else {
				content = "var " + id + ": " + _type;
			}
		}
		if (_forceDecl || isStruct || expr != null) {
			if (expr != null) {
				if (isStruct && expr.startsWith("{") && expr.endsWith("}")) {
					expr = convertStructInitializer(_type, expr, typeEntry);
				}
				content += " <- " + expr;
			}
			// START KGU#1080b 2023-09-27: Bugfix #1089.2 Suppress empty elements
			//if (_parentNode != null) {
			if (_parentNode != null && !content.isEmpty()) {
			// END KGU#1080b 2023-09-27
				Element instr = new Instruction(translateContent(content));
				if (_comment != null) {
					instr.setComment(_comment);
				}
				if (!extraComments.isEmpty()) {
					instr.getComment().add(extraComments.concatenate(" + "));
				}
				if (_parentNode.parent instanceof Root && ((Root)_parentNode.parent).getMethodName().equals("???")) {
					if (!_asTypeDef) {
						instr.getComment().add("Globally declared!");
						instr.setColor(COLOR_GLOBAL);
					}
					// FIXME
					if (root != _parentNode.parent && !this.importingRoots.contains(root)) {
						this.importingRoots.add(root);
						(root).addToIncludeList((Root)_parentNode.parent);
					}
				}
				else if (isGlobal && this.globalRoot != null) {
					_parentNode = this.globalRoot.children;
					if (root != this.globalRoot && !this.importingRoots.contains(root)) {
						this.importingRoots.add(root);
						root.addToIncludeList(this.globalRoot);						
					}
				}
				else if (expr == null && !_asTypeDef) {
					instr.setColor(COLOR_DECL);	// local declarations with a smooth green
				}
				// START KGU#1080/KGU#1081 2023-09-28: Bugfix #1089/#1091 We must register aliases
				//if (_asTypeDef && expr != null) {
				//	instr.setColor(Color.RED);
				//}
				if (_asTypeDef) {
					if (expr == null) {
						instr.updateTypeMap(typeMap);
					}
					else {
						instr.setColor(Color.RED);
					}
				}
				// END KGU#1080/KGU#1081 2023-09-28
				// Constant colour has priority
				if (isConstant && !_asTypeDef) {
					instr.setColor(COLOR_CONST);
				}
				_parentNode.addElement(instr);
			}
		}
//		else {
//			// assignment
//			log("\ttrying <Declarator> '=' <Initializer> ...\n", false);
//			// Should be RuleConstants.PROD_VAR_ID_EQ. Now it can get tricky if arrays
//			// are involved - remember this is a declaration rule!
//			// The executor now copes with lines like int data[4] <- {2,5,6,3}. On the
//			// other hand, an import without the type but with index brackets would
//			// induce a totally wrong semantics. So we must drop both or none.
//			// Without declaration however, the parser won't accept initializers
//			// anymore - which is sound with ANSI C.
//			// Don't be afraid of multidimensional arrays. The grammar doesn't accept
//			// multiple indices in a declaration (only in assignments or as expression).
//			String varName = _reduc.get(0).asString();
//			//String arrayTag = this.getContent_R((Reduction)thdReduc.getToken(1).getData(), "");
//			String expr = this.getContent_R(_reduc.get(3).asReduction(), "");
//			content = translateContent(content);
//			Element instr = null;
//			if (this.optionImportVarDecl || _forceDecl) {
//				instr = new Instruction(translateContent(_type) + " " + content);
//				if (isConstant) {
//					instr.setColor(colorConst);
//				}
//			}
//			else {
//				instr = new Instruction((isConstant ? "const " : "") + varName + " <- " + translateContent(expr));
//				if (isConstant) {
//					instr.setColor(colorConst);
//				}
//			}
//			// START KGU#407 2017-06-22: Enh. #420
//			if (_comment != null) {
//				instr.setComment(_comment);
//			}
//			// END KGU#407 2017-06-22
//			if (_parentNode.parent instanceof Root && ((Root)_parentNode.parent).getMethodName().equals("???")) {
//				// START KGU#407 2017-06-22: Enh. #420
//				//instr.setComment("globally declared!");
//				instr.getComment().add("Globally declared!");
//				// END KGU#407 2017-06-22
//				instr.setColor(colorGlobal);
//			}
//			_parentNode.addElement(instr);
//		}
		log("\tFallen back with rule " + ruleId + " (" + _reduc.getParent().toString() + ")\n", false);
		return content;
	}
	
	/**
	 * Collects all type name parts (including "const") in _typeSpecs and returns whether
	 * "typedef" was among the storage class specifiers
	 * @param _reduction - a {@code <Decl Specifiers>} rule is expected
	 * @param _typeSpecs - a {@link StringList} to be filled
	 * @param _parentNode - a {@link Subqueue} possible implicit type definitions should go to, may be null
	 * @param _initDeclRed - if a declarator / initializer is following, then their reduction
	 * @return true if "typedef" was among the storage class specifiers
	 * @throws ParserCancelled 
	 */
	private boolean getDeclSpecifiers(Reduction _reduction, StringList _typeSpecs, Subqueue _parentNode, Reduction _initDeclRed) throws ParserCancelled
	{
		boolean isTypedef = false;
		//boolean isStruct = false;
		int ruleId = _reduction.getParent().getTableIndex();
		while (_reduction.getParent().getHead().toString().equals("<Decl Specifiers>")) {
			Token prefix = _reduction.get(0);	// May be <Storage Class> or <Type Specifier> or "inline"
			switch (ruleId) {
			case RuleConstants.PROD_DECLSPECIFIERS:
				// <Decl Specifiers> ::= <Storage Class> <Decl Specs>
				if (prefix.getType() == SymbolType.NON_TERMINAL) {
					String prefixStr = getContent_R(prefix.asReduction(), "");
					if (prefixStr.equals("typedef")) {
						isTypedef = true;
					}
					else if (!prefixStr.equals("auto") && !isTypedef) {
						// We should also add "extern" and "static" as they use to be important
						_typeSpecs.add(prefixStr);
					}
				}
				break;
			case RuleConstants.PROD_DECLSPECIFIERS3:
				// <Decl Specifiers> ::= <Type Qualifier> <Decl Specs>
				// START KGU#670 2019-03-01: Bugfix #692 Wrong test let const detection fail
				//if (prefix.asString().equals("const")) {
				if (prefix.asString().equals("[const]")) {
				// END KGU#670 2019-03-01
					_typeSpecs.add("const");
				}
				break;
			case RuleConstants.PROD_DECLSPECIFIERS2:
				// <Decl Specifiers> ::= <Type Specifier> <Decl Specs>
				if (prefix.getType() == SymbolType.NON_TERMINAL) {
					int prefixId = prefix.asReduction().getParent().getTableIndex();
					switch (prefixId) {
					case RuleConstants.PROD_TYPESPECIFIER:	// rather unlikely (represented by one of the following)
						// <Type Specifier> ::= <StructOrUnion Spec>
					case RuleConstants.PROD_STRUCTORUNIONSPEC_IDENTIFIER_LBRACE_RBRACE:
						// <StructOrUnion Spec> ::= <StructOrUnion> Identifier '{' <StructDeclnList> '}'
					case RuleConstants.PROD_STRUCTORUNIONSPEC_LBRACE_RBRACE:
						// <StructOrUnion Spec> ::= <StructOrUnion> '{' <StructDeclnList> '}'
					case RuleConstants.PROD_STRUCTORUNIONSPEC_IDENTIFIER:
						// <StructOrUnion Spec> ::= <StructOrUnion> Identifier
					{
						String type = null;
						Reduction structRed = prefix.asReduction();
						if (structRed.size() == 2) {
							// <StructOrUnion> Identifier
							// Skip the "struct" or "union" keyword if the type is known, otherwise put the keyword
							type = structRed.get(1).asString();
							if (!this.typeMap.containsKey(":"+type)) {
								type = getContent_R(structRed.get(0).asReduction(), "") + " " + type;
							}
						}
						else {
							String array = "";
							String ptrs = "";
							if (structRed.size() == 4) {
								// <StructOrUnion> '{' <StructDeclnList> '}'
								// It is actually totally ambiguous, in which of the reductions the identifier occurs! 
								if (isTypedef && _initDeclRed != null || !(type = getContent_R(_reduction.get(1).asReduction(), "").trim()).isEmpty()) {
									// FIXME: We must separate indices and pointers
									if (_initDeclRed != null) {
										// START KGU#1080d 2023-09-27 Bugfix #1089.4 Substitute only if unique
										//type = getContent_R(_initDeclRed, "").trim();
										type = String.format("AnonStruct%1$03d", typeCount++);
										if (_initDeclRed.getParent().getTableIndex() != RuleConstants.PROD_INITDECLLIST_COMMA) {
											type = getContent_R(_initDeclRed, "").trim();
										}
										// END KGU#1080.d 2023-09--27
									}
									if (MATCH_PTR_DECL.reset(type).matches()) {
										ptrs = MATCH_PTR_DECL.group(1).trim();
										type = MATCH_PTR_DECL.group(3).trim();
									}
									int pos = type.indexOf('[');
									if (pos > 0) {
										array = "array " + type.substring(pos) + " of ";
										type = type.substring(0, pos).trim();
									}
								}
								else {
									type = String.format("AnonStruct%1$03d", typeCount++);
								}
							}
							else {
								// <StructOrUnion> Identifier '{' <StructDeclnList> '}'
								// FIXME: Is this a NON_TERMINAL, such that we should use getContent_R()?
								type = structRed.get(1).asString();
							}
							StringList components = getCompsFromStructDef(structRed.get(structRed.size()-2).asReduction());
							// compose and define type
							components.insert("type " + type + " = " + array + ptrs + "struct{\\", 0);
							String lastComp = components.get(components.count()-1);
							components.set(components.count()-1, lastComp.replace('\\', '}'));
							Instruction typedef = new Instruction(components);
							if (_parentNode != null) {
								if (isTypedef) {
									this.equipWithSourceComment(typedef, _reduction);
								}
								_parentNode.addElement(typedef);
							}
							typedef.updateTypeMap(typeMap);
						}
						//isStruct = true;
						_typeSpecs.add(type);
					}
						break;
					case RuleConstants.PROD_TYPESPECIFIER2:	// rather unlikely (represented by one of the following)
						// <Type Specifier> ::= <Enumerator Spec>
					case RuleConstants.PROD_ENUMERATORSPEC_ENUM_IDENTIFIER_LBRACE_RBRACE:
						// <Enumerator Spec> ::= enum Identifier '{' <EnumList> '}'
					case RuleConstants.PROD_ENUMERATORSPEC_ENUM_IDENTIFIER_LBRACE_COMMA_RBRACE:
						// <Enumerator Spec> ::= enum Identifier '{' <EnumList> ',' '}'
					case RuleConstants.PROD_ENUMERATORSPEC_ENUM_LBRACE_RBRACE:
						// <Enumerator Spec> ::= enum '{' <EnumList> '}'
					case RuleConstants.PROD_ENUMERATORSPEC_ENUM_LBRACE_COMMA_RBRACE:
						// <Enumerator Spec> ::= enum '{' <EnumList> ',' '}'
					{
						// actual enum type support
						String typeName = null;
						int ixEnum = 3;
						if (prefixId == RuleConstants.PROD_ENUMERATORSPEC_ENUM_LBRACE_COMMA_RBRACE
								|| prefixId == RuleConstants.PROD_ENUMERATORSPEC_ENUM_LBRACE_RBRACE) {
							ixEnum = 2;
						}
						else {
							typeName = prefix.asReduction().get(1).asString();
						}
						Reduction redEnumL = prefix.asReduction().get(ixEnum).asReduction();
						StringList names = new StringList();
						StringList values = new StringList();
						while (redEnumL != null) {
							Reduction redEnum = redEnumL;
							if (redEnumL.getParent().getTableIndex() == RuleConstants.PROD_ENUMLIST_COMMA) {
								redEnum = redEnumL.get(2).asReduction();
								redEnumL = redEnumL.get(0).asReduction();
							}
							else {
								redEnumL = null;
							}
							if (redEnum.getParent().getTableIndex() == RuleConstants.PROD_ENUMERATOR_IDENTIFIER_EQ) {
								names.add(redEnum.get(0).asString());
								values.add(this.getContent_R(redEnum.get(2).asReduction(), ""));
							}
							else {
								names.add(this.getContent_R(redEnum, ""));
								values.add("");
							}
						}
						if (names.count() > 0 && _parentNode != null) {
							names = names.reverse();
							values = values.reverse();
							//int val = 0;
							//String baseVal = "";
							for (int i = 0; i < names.count(); i++) {
								// START KGU#542 2019-11-18: Enh. #739 - true enum type import
								//if (!values.get(i).isEmpty()) {
								//	baseVal = values.get(i);
								//	try {
								//		val = Integer.parseInt(baseVal);
								//		baseVal = "";	// If the value was an int literal, we don't need an expr. string
								//	}
								//	catch (NumberFormatException ex) {
								//		val = 0;
								//	}
								//}
								//names.set(i, "const " + names.get(i) + " <- " + baseVal + (baseVal.isEmpty() ? "" : " + ") + val);
								//val++;
								String valStr = values.get(i).trim();
								if (!valStr.isEmpty()) {
									names.set(i, names.get(i) + " = " + valStr);
								}
								// END KGU#542 2019-11-18
							}
							// START KGU#542 2019-11-18: Enh. #739
							//Instruction enumDef = new Instruction(names);
							String sepa = ", ";
							// FIXME: Tune the threshold if necessary
							if (names.count() > 10) {
								sepa = ",\\\n";
							}
							// START KGU#1080b 2023-09-27: Bugfix #1089.2 Try to fetch the typeid
							if (typeName == null) {
								// enum '{' <EnumList> [','] '}'
								// It is actually rather ambiguous, in which of the reductions the identifier occurs!
								if (isTypedef && _initDeclRed != null || !(typeName = getContent_R(_reduction.get(1).asReduction(), "").trim()).isEmpty()) {
									// We must separate indices and pointers
									if (_initDeclRed != null) {
										if (_initDeclRed.getParent().getTableIndex() != RuleConstants.PROD_INITDECLLIST_COMMA) {
											typeName = getContent_R(_initDeclRed, "").trim();
										}
										else {
											// More than one defined typeids - don't substitute.
											typeName = "[";	// Makes it invalid without causing NullPointerException
										}
									}
									if (typeName.indexOf('[') >= 0
											|| MATCH_PTR_DECL.reset(typeName).matches()) {
										typeName = null;
									}
								}
							}
							// END KGU#1080b 2023-09-27
							if (typeName == null) {
								typeName = "Enum" + Math.abs(System.nanoTime());
							}
							Instruction enumDef = new Instruction(
									StringList.explode("type " + typeName + " = enum{" + names.concatenate(sepa) + "}", "\n"));
							// END KGU#542 2019-11-18
							this.equipWithSourceComment(enumDef, prefix.asReduction());
							//if (typeName != null) {
							//	enumDef.getComment().add("Enumeration type " + typeName);
							//}
							enumDef.setColor(COLOR_CONST);
							_parentNode.addElement(enumDef);
							// START KGU#1080/KGU#1081 2023-09-28: Bugfix #1089/#1091
							enumDef.updateTypeMap(typeMap);
							// END KGU#1080/KGU#1089
						}
						// START KGU#1080c 2023-09-27: Bugfix #739,#1089.3 Defective enum type support
						//_typeSpecs.add("int");
						if (typeName == null) {
							_typeSpecs.add("int");	// Shouldn't happen anymore
						}
						else {
							_typeSpecs.add(typeName);
						}
						// END KGU#1080c 2023-09-27
					}	
						break;
					case RuleConstants.PROD_ENUMERATORSPEC_ENUM_IDENTIFIER:
						// <Enumerator Spec> ::= enum Identifier
						// START KGU#1080c 2023-09-27: Bugfix #739,#1089.3 Defective enum type support
						//_typeSpecs.add("int");
					{
						String typeid = prefix.asReduction().get(1).asString();
						_typeSpecs.add(typeid);
					}
						// END KGU#1080c
						break;
					case RuleConstants.PROD_TYPEDEFNAME_USERTYPEID:
						// <Typedef Name> ::= UserTypeId
					case RuleConstants.PROD_TYPESPECIFIER3:
						// <Type Specifier> ::= <Typedef Name>
					{
						String typeid = getContent_R(prefix.asReduction(), "").trim();
						_typeSpecs.add(typeid);// Produce typedef here? No, maybe a configured external type
					}
						break;
					default:
						if (prefixId >= RuleConstants.PROD_TYPESPECIFIER_VOID && prefixId <= RuleConstants.PROD_TYPESPECIFIER__COMPLEX) {
							_typeSpecs.add(getContent_R(prefix.asReduction(), "").trim());
						}
						else {
							// FIXME Debug print?
							System.out.println("C99Parser.getDeclSpecifiers() default - Type specifier: " + prefix.asReduction().getParent().getTableIndex());
						}
					}
				}
				break;
			default:;
			}
			_reduction = _reduction.get(1).asReduction();
			ruleId = _reduction.getParent().getTableIndex();
		}
		//System.out.println("getDeclSpecifiers(): " + _typeSpecs.concatenate(", "));
		return isTypedef;
	}
	
	/**
	 * Analyses the given {@link Reduction}, isolates and returns the declared identifier
	 * and provides the structure in original C syntax ({@code _pointer}, {@code _arrays})
	 * and in Pascal syntax {@code _asPascal}.
	 * @param _reduction - {@link Reduction} of type {@code <Declarator>} or {@code <Abstract Declarator>}
	 * @param _pointers - a {@link StringList} to be filled with the prefix (left of the identifier)
	 * @param _arrays - a {@link StringList} to be filled with the postfix (right of the identifier)
	 * @param _asPascal - a {@link StringList} to be filled with a Pascal like type specification
	 * @param _parentNode - a {@link Subqueue} to append possible type definitions to 
	 * @param _declListRed - possibly a reduction representing the outer context (i.e. {@code <DelarationList>})
	 * @return the isolated identifier or null of there is none oder if it's ambiguous.
	 * @throws ParserCancelled 
	 */
	String getDeclarator(Reduction _reduction, StringList _pointers, StringList _arrays, StringList _asPascal, Subqueue _parentNode, Reduction _declListRed) throws ParserCancelled
	{
		// FIXME: This may be a function signature! So we should support the respective structure --> _asPascal?
		// <Declarator> ::= <Pointer> <Direct Decl>   or
		// <Declarator> ::= <Direct Decl>
		String name = null;	// The declared identifier
		int ruleId = _reduction.getParent().getTableIndex();
		if (ruleId == RuleConstants.PROD_DECLARATOR || ruleId == RuleConstants.PROD_ABSTRACTDECL) {
			// Analyse <Pointer>
			if (_pointers != null || _asPascal != null) {
				getPointers(_reduction, _pointers, _asPascal);
			}
			// Now the pointers are done, concentrate on the declarator
			_reduction = _reduction.get(1).asReduction();
			ruleId = _reduction.getParent().getTableIndex();
		}
		// Now process the <Direct Decl>
		name = getDirectDecl(_reduction, _pointers, _arrays, _asPascal, _parentNode, _declListRed);
		return name;
	}

	/**
	 * Processes a {@code <Direct Decl>} or {@code <Direct Abstr Decl>} rule, extracts the declared name and
	 * fills the given {@link StringList}s with the surrounding text fragments.
	 * @param _reduction - a reduction dreived from of either {@code <Direct Decl>} or {@code <Direct Abstr Decl>}
	 * @param _pointers - the {@link StringList} intended for the prefix of the name
	 * @param _arrays - the {@link StringList} intended for the postfix of the name (index ranges, arg lists)
	 * @param _asPascal - a {@link StringList} intended to accumulate a Pascal-like notation
	 * @param _parentNode - if given, the {@link Subqueue} to add required type definitions
	 * @param _declListRed - {@link Reduction} for the declarator list or null
	 * @return the declared name (if any)
	 * @throws ParserCancelled 
	 */
	private String getDirectDecl(Reduction _reduction, StringList _pointers, StringList _arrays, StringList _asPascal,
			Subqueue _parentNode, Reduction _declListRed) throws ParserCancelled {
		String name = "";
		int ruleId = _reduction.getParent().getTableIndex();
		String nestedType = null;
		int ixDim = -1;
		String indexRange = "";
		switch (ruleId) {
		case RuleConstants.PROD_DIRECTDECL_IDENTIFIER:
			name = getContent_R(_reduction, "");
			break;
		case RuleConstants.PROD_DIRECTDECL_LPAREN_RPAREN:
			// <Direct Abstr Decl> ::= '(' <Abstract Decl> ')'
		case RuleConstants.PROD_DIRECTABSTRDECL_LPAREN_RPAREN:
			// <Direct Decl> ::= '(' <Declarator> ')'
			// Now in Pascal style, this should force us to define the type as far as possible
			if (_parentNode != null && _asPascal != null) {
				nestedType = String.format("AnonType%1$03d", typeCount++);
				String content = "type " + nestedType + " = " + _asPascal.concatenate(" ");
				_parentNode.addElement(new Instruction(content));
				_asPascal.clear();
				_asPascal.add(nestedType);
			}
			if (_pointers != null && _arrays != null) {
				_pointers.add("(");
				_arrays.insert(")", 0);
			}					
			name = getDeclarator(_reduction.get(1).asReduction(), _pointers, _arrays, _asPascal, _parentNode, null);
			break;
		case RuleConstants.PROD_DIRECTDECL_LBRACKET_STATIC_RBRACKET:
		case RuleConstants.PROD_DIRECTDECL_LBRACKET_STATIC_RBRACKET2:
		case RuleConstants.PROD_DIRECTABSTRDECL_LBRACKET_STATIC_RBRACKET:
		case RuleConstants.PROD_DIRECTABSTRDECL_LBRACKET_STATIC_RBRACKET2:
			ixDim += 1;	// Total: ixDim = 4
		case RuleConstants.PROD_DIRECTDECL_LBRACKET_RBRACKET:
		case RuleConstants.PROD_DIRECTABSTRDECL_LBRACKET_RBRACKET:
			// START KGU#651 2019-02-13: Bugfix #678
			//ixDim += 3;
			ixDim += 4;	// Total: ixDim = 3 (without static) or 4 (with static)
			// END KGU#651 2019-02-13
		case RuleConstants.PROD_DIRECTDECL_LBRACKET_TIMES_RBRACKET:
		case RuleConstants.PROD_DIRECTDECL_LBRACKET_RBRACKET2:
		case RuleConstants.PROD_DIRECTABSTRDECL_LBRACKET_TIMES_RBRACKET:
		case RuleConstants.PROD_DIRECTABSTRDECL_LBRACKET_RBRACKET2:
			if (ixDim > -1) {
				Token dimToken = _reduction.get(ixDim);
				// START KGU#651 2019-02-13: Bugfix #678
				//if (dimToken.getType() != SymbolType.NON_TERMINAL) {
				if (dimToken.getType() == SymbolType.NON_TERMINAL) {
				// END KGU#651 2019-02-13
					indexRange = "[" + getContent_R(dimToken.asReduction(), "") +"]";
				}
				else if (dimToken.asReduction().size() == 0) {
					indexRange = "";
				}
				else {
					indexRange = "[" + dimToken.asString() + "]";
				}
			}
			if (_arrays != null) {
				_arrays.insert((indexRange == null ? "[]" : indexRange), 0);
			}
			if (_asPascal != null) {
				_asPascal.insert("array " + (indexRange == null ? "" : indexRange + " ") + "of ", 0);
			}
			name = getDirectDecl(_reduction.get(0).asReduction(), _pointers, _arrays, _asPascal, _parentNode, _declListRed);
			break;
		case RuleConstants.PROD_DIRECTDECL_LPAREN_RPAREN2: 
		case RuleConstants.PROD_DIRECTDECL_LPAREN_RPAREN3: 
		case RuleConstants.PROD_DIRECTABSTRDECL_LPAREN_RPAREN2: 
		case RuleConstants.PROD_DIRECTABSTRDECL_LPAREN_RPAREN3:
		{
			String params = "";
			if (ruleId != RuleConstants.PROD_DIRECTABSTRDECL_LPAREN_RPAREN3) {
				params = this.getParamList(_reduction.get(2).asReduction(), _parentNode, _asPascal != null, _declListRed).trim();
			}
			if (_arrays != null) {
				_arrays.insert("(" + params + ")", 0);
			}
			if (_asPascal != null) {
				_asPascal.insert(": ", 0);	// Marker for function id
				_asPascal.insert(")", 0);
				if (!params.isEmpty()) {
					_asPascal.insert(params, 0);
				}
				_asPascal.insert("(", 0);
			}
			name = getDirectDecl(_reduction.get(0).asReduction(), _pointers, _arrays, _asPascal, _parentNode, _declListRed);
			break;
		}			
		}
		return name;
	}

	/**
	 * Extracts and returns a parameter list from the given {@link Reduction} {@code _paramReduc}.
	 * @param _paramReduc - {@link Reduction} for head {@code <ParamTypeList>} or {@code <ParameterList>}
	 * @param _parentNode - if a {@link Subqueue} is given here then possible intermediate type definitions may be added there
	 * @param _pascalStyle - whether the parameter list is rather to look like a Pascal parameter list (may not always work) 
	 * @param _declListRed - an optional declaration list reduction with external parameter declarations to replace the mere
	 * identifier list in such a case. 
	 * @return
	 * @throws ParserCancelled 
	 */
	private String getParamList(Reduction _paramReduc, Subqueue _parentNode, boolean _pascalStyle, Reduction _declListRed) throws ParserCancelled {
		String params = ""; 
		StringList paramList = new StringList();
		String ellipse = "";
		int ruleId = _paramReduc.getParent().getTableIndex(); 
		if (ruleId == RuleConstants.PROD_PARAMTYPELIST_COMMA_DOTDOTDOT) {
			ellipse = ", ...";
			_paramReduc = _paramReduc.get(0).asReduction();
			ruleId = _paramReduc.getParent().getTableIndex();
		}
		switch (ruleId) {
		case RuleConstants.PROD_IDLISTOPT2:
			// Empty argument list
			break;
		case RuleConstants.PROD_IDENTIFIERLIST_IDENTIFIER:	// FIXME does is work for this rule?
		case RuleConstants.PROD_IDENTIFIERLIST_COMMA_IDENTIFIER:
			// Ancient function definition: type foo(a, b, c) type1 a; type2 b; type3 c; {...}
			params = getContent_R(_paramReduc, "");
			if (_declListRed != null) {
				StringList paramDecls = getDeclsFromDeclList(_declListRed);
				StringList paramNames = StringList.explode(params, ",");
				// Sort the parameter declarations according to the arg list (just in case...)
				if (paramDecls.count() == paramNames.count()) {
					for (int p = 0; p < paramNames.count(); p++) {
						Matcher pm = Pattern.compile("(^|.*?\\W)" + paramNames.get(p).trim() + ":.*").matcher("");
						for (int q = 0; q < paramDecls.count(); q++) {
							String pd = paramDecls.get(q);
							if (pm.reset(pd).matches()) {
								paramList.add(pd);
								break;
							}
						}
						if (paramList.count() < p+1) {
							paramList.add(paramNames.get(p));
						}
					}
					params = paramList.concatenate("; ");
				}
			}
			break;
		case RuleConstants.PROD_PARAMETERLIST_COMMA:
			// More than one parameter
			do {
				String param = getContent_R(_paramReduc.get(2).asReduction(), "");
				_paramReduc = _paramReduc.get(0).asReduction();
				ruleId = _paramReduc.getParent().getTableIndex();
				paramList.add(param);
			} while (ruleId == RuleConstants.PROD_PARAMETERLIST_COMMA);
			// no break here!
		default: // Should be a <Parameter Decl>
			paramList.add(getContent_R(_paramReduc, ""));
			// START KGU#668 2019-02-28: Workaround #690 - Suppress struct keywords in parameter lists (FIXME)
			for (int i = 0; i < paramList.count(); i++) {
				String param = paramList.get(i);
				if (param.startsWith("struct ")) {
					paramList.set(i, param.substring(7));
				}
			}
			// END KGU#668 2019-02-28
			params = paramList.reverse().concatenate(", ") + ellipse;
			break;
		}
		return params.trim();
	}

	/**
	 * Analyses and extracts the relevant information from a {@code <Pointer} reduction
	 * @param _reduction - a {@link Reduction} assumed to be a {@code <Pointer>} rule
	 * @param _pointers - a {@link StringList} to be filled with the relevant tokens in C order
	 * @param _asPascal - a {@link StringList} to be filled as good as possible with Pascal-like ref cascade
	 */
	private void getPointers(Reduction _reduction, StringList _pointers, StringList _asPascal) {
		Token ptrToken = _reduction.get(0);
		do {
			// There is at least one asterisk
			if (_pointers != null) {
				_pointers.add("*");
			}
			if (_asPascal != null) {
				_asPascal.insert("ref", 0);
			}
			if (ptrToken.getType() == SymbolType.CONTENT) {
				// <Pointer> ::= '*'
				ptrToken = null;
			}
			else {
				Token qualToken = null;
				switch (ptrToken.asReduction().getParent().getTableIndex()) {
				case RuleConstants.PROD_POINTER_TIMES:
					// <Pointer> ::= '*' <TypeQualList> <Pointer>
					qualToken = ptrToken.asReduction().get(1);
					ptrToken = ptrToken.asReduction().get(2);
					break;
				case RuleConstants.PROD_POINTER_TIMES2:
					// <Pointer> ::= '*' <TypeQualList>
					qualToken = ptrToken.asReduction().get(1);
					ptrToken = null;
					break;
				case RuleConstants.PROD_POINTER_TIMES3:
					// <Pointer> ::= '*' <Pointer>
					ptrToken = ptrToken.asReduction().get(1);;
					break;
				default:
						ptrToken = null;
				}
				// If there a <TypeQualList> then exhaust it
				while (qualToken != null) {
					if (qualToken.getType() == SymbolType.CONTENT && qualToken.asString().equals("const")) {
						if (_pointers != null) {
							_pointers.add("const");
						}
						// In Pascal this cannot sensibly be expressed. 
						qualToken = null;
					}
					// START KGU#702 2019-03-29: Against appearance, qualToken may be a <TypeQualList> but contain only 1 element!
					//else if (qualToken.asReduction().get(1).asString().equals("const")) {
					else if (qualToken.asReduction().size() > 1 && qualToken.asReduction().get(1).asString().equals("const")) {
					// END KGU#702 2019-03-29
						if (_pointers != null) {
							_pointers.add("const");
						}
						// In Pascal this cannot sensibly be expressed. 
						// There can't be several const tokens, others aren't of interest
						qualToken = null;
					}
					else {
						qualToken = qualToken.asReduction().get(0);
					}
				}
			}
		} while (ptrToken != null);
		// <Pointer> part done, now adjust the  
	}
	
	// New approach for a generic analysis of a Declaration, remained a fragment (superfluous?)
//	private String analyseDeclaration(Reduction _reduction, StringBuilder _pascalType, Subqueue _parentNode, boolean _forceDecl, boolean _something)
//	{
//		String initializer = "";
//		StringList array = new StringList();
//		StringList ptrs = new StringList();
//		StringList typeparts = new StringList();
//		String varName = null;
//		int ruleId = _reduction.getParent().getTableIndex();
//		if (ruleId == RuleConstants.PROD_DECLARATION_SEMI) {
//			// <Declaration> ::= <Decl Specifiers> <InitDeclList> ';'
//			// Evaluation of the <InitDeclList>
//			Reduction intDecl = _reduction.get(1).asReduction();
//		}
//		_reduction = _reduction.get(0).asReduction();
//		ruleId = _reduction.getParent().getTableIndex();
//		while (_reduction.getParent().getHead().toString().equals("<Decl Specifiers")) {
//			switch (ruleId) {
//			case RuleConstants.PROD_DECLSPECIFIERS:
//			case RuleConstants.PROD_DECLSPECIFIERS2:
//			case RuleConstants.PROD_DECLSPECIFIERS3:
//			}
//			
//		}
//		return varName;
//	}
	
	/**
	 * Processes type specifications for a variable / constant declaration or a
	 * type definition (argument {@code _declaringVars} indicates which of both).
	 * If an anonymous struct description is found then a type definition object
	 * will be inserted to {@code _subqueue} - either with a generic name (if 
	 * {@code _typeList} is empty) or with the first element of {@code _typeList}
	 * as name. Except in the latter case (type definition with given name created)
	 * the name of the found type will be inserted at the beginning of
	 * {@code _typeList}.<br/>
	 * If {@code _isGlobal} is true and a type definition is to be created then
	 * a dependency of the current {@link #root} to the global diagram is established
	 * in {@code this.importingRoots}.<br/>
	 * The trouble here is that we would like to return several things at once:
	 * a type entry, a type description, and some flags. For a recursive application,
	 * we would even need different resulting formats.
	 * @param _reduction - current {@link Reduction} object
	 * @param _ruleId - table id of the production rule
	 * @param _parentNode - the {@link Subqueue} to which elements are to be added
	 * @param _isGlobal - whether the type / variable is a global one
	 * @param _typeList - a container for type names, both for input and output 
	 * @param _declaringVars - whether this is used by a variable/constant declaration (type definition otherwise)
	 * @return a logical value indicating whether the processed rule was a type definition
	 * @throws ParserCancelled 
	 */
	protected boolean processTypes(Reduction _reduction, int _ruleId, Subqueue _parentNode, boolean _isGlobal,
			StringList _typeList, boolean _declaringVars) throws ParserCancelled
	{
		//boolean isStruct = false;
		boolean isTypedef = false;
		String type = "int";
		//boolean isConstant = false;
		boolean addType = true;
		//StringList storage = new StringList();
		//StringList specifiers = new StringList();
		//StringList qualifiers = new StringList();
		Reduction declRed = null;
		// Will a variable or type be declared / defined here?
		boolean hasDecl = _ruleId == RuleConstants.PROD_DECLARATION_SEMI;
		if (hasDecl) {
			declRed = _reduction.get(1).asReduction(); 
		}
		if (hasDecl || _ruleId == RuleConstants.PROD_DECLARATION_SEMI2) {
			_reduction = _reduction.get(0).asReduction();
			_ruleId = _reduction.getParent().getTableIndex();
		}
//		while (_reduction.getParent().getHead().toString().equals("<Decl Specifiers>")) {
//			Token prefix = _reduction.get(0);	// May be <Storage Class> or <Type Specifier> or "inline"
//			switch (_ruleId) {
//			case RuleConstants.PROD_DECLSPECIFIERS: // <Decl Specifiers> ::= <Storage Class> <Decl Specs>
//			{
//				String storageClass = (prefix.getType() == SymbolType.NON_TERMINAL) ? getContent_R(prefix.asReduction(), "") : prefix.asString();
//				if (storageClass.equals("typedef")) {
//					isTypedef = true;
//				}
//				else {
//					// We don't actually need them but may add them to the comment.
//					storage.add(storageClass);
//				}
//			}
//				break;
//			case RuleConstants.PROD_DECLSPECIFIERS2: // <Decl Specifiers> ::= <Type Specifier> <Decl Specs>
//				if (prefix.getType() == SymbolType.NON_TERMINAL) {
//					switch (prefix.asReduction().getParent().getTableIndex()) {
//					case RuleConstants.PROD_TYPESPECIFIER:	// rather unlikely (represented by one of the following)
//						// <Type Specifier> ::= <StructOrUnion Spec>
//					case RuleConstants.PROD_STRUCTORUNIONSPEC_IDENTIFIER_LBRACE_RBRACE:
//						// <StructOrUnion Spec> ::= <StructOrUnion> Identifier '{' <StructDeclnList> '}'
//					case RuleConstants.PROD_STRUCTORUNIONSPEC_LBRACE_RBRACE:
//						// <StructOrUnion Spec> ::= <StructOrUnion> '{' <StructDeclnList> '}'
//					case RuleConstants.PROD_STRUCTORUNIONSPEC_IDENTIFIER:
//						// <StructOrUnion Spec> ::= <StructOrUnion> Identifier
//					{
//						Reduction structRed = prefix.asReduction();
//						if (structRed.size() == 2) {
//							type = structRed.get(1).asString();
//							// TODO retrieve type
//						}
//						else {
//							String array = "";
//							String ptrs = "";
//							if (structRed.size() == 4) {
//								if (isTypedef && declRed == null) {
//									// FIXME: We must separate indices and pointers
//									type = getContent_R(_reduction.get(1).asReduction(), "").trim();
//									if (MATCH_PTR_DECL.reset(type).matches()) {
//										ptrs = MATCH_PTR_DECL.group(1).trim();
//										type = MATCH_PTR_DECL.group(3).trim();
//									}
//									int pos = type.indexOf('[');
//									if (pos > 0) {
//										array = "array " + type.substring(pos) + " of ";
//										type = type.substring(0, pos).trim();
//									}
//								}
//								else {
//									type = String.format("AnonStruct%1$03d", typeCount++);
//								}
//							}
//							else {
//								type = structRed.get(1).asString();
//							}
//							StringList components = getCompsFromStructDef(structRed.get(structRed.size()-2).asReduction());
//							// compose and define type
//							components.insert("type " + type + " = " + array + ptrs + "struct{\\", 0);
//							String lastComp = components.get(components.count()-1);
//							components.set(components.count()-1, lastComp.replace('\\', '}'));
//							Instruction typedef = new Instruction(components);
//							_parentNode.addElement(typedef);
//							typedef.updateTypeMap(typeMap);
//						}
//						isStruct = true;
//					}
//						break;
//					case RuleConstants.PROD_TYPESPECIFIER2:
//						// <Type Specifier> ::= <Enumerator Spec>
//						// FIXME Define the constants at least
//						break;
//					case RuleConstants.PROD_TYPEDEFNAME_USERTYPEID:
//						// <Typedef Name> ::= UserTypeId
//					case RuleConstants.PROD_TYPESPECIFIER3:
//						// <Type Specifier> ::= <Typedef Name>
//						type = getContent_R(prefix.asReduction(), "").trim();
//						break;
//					default:
//						System.out.println(prefix.asReduction().getParent().getTableIndex());	
//					}
//				}
//				else {
//					specifiers.add(prefix.toString());
//				}
//				break;
//			case RuleConstants.PROD_DECLSPECIFIERS3: // <Decl Specifiers> ::= <Type Specifier> <Decl Specs>
//				qualifiers.add(prefix.toString());
//				break;
//			default:
//			}
//			_reduction = _reduction.get(1).asReduction();
//			_ruleId = _reduction.getParent().getTableIndex();
//			
//		}
		StringList typeSpecs = new StringList();
		isTypedef = this.getDeclSpecifiers(_reduction, typeSpecs, _parentNode, declRed);
		if (typeSpecs.count() > 0) {
			type = typeSpecs.concatenate(" ");
		}
//		if (isConstant && _declaringVars) {
//			type = "const " + type;
//		}
		if (addType) {
			_typeList.insert(type, 0);
		}
		return isTypedef;
	}

	/**
	 * Is to extract the struct component declarations from a struct definition and
	 * to convert them into Structorizer (Pascal-like) syntax.
	 * @param _compListRed - the reduction representing a rule {@code <StructDeclnList>}
	 * @return The {@link StringList} of processed component groups
	 * @throws ParserCancelled 
	 */
	private StringList getCompsFromStructDef(Reduction _compListRed) throws ParserCancelled
	{
		// Resolve the left recursion non-recursively
		StringList components = new StringList();
		LinkedList<Reduction> compReds = new LinkedList<Reduction>();
		while (_compListRed.size() == 2) {
			// <StructDeclnList> ::= <StructDeclnList> <Struct Declaration>
			compReds.add(_compListRed.get(1).asReduction());
			_compListRed = _compListRed.get(0).asReduction();
		}
		// <StructDeclnList> ::= <Struct Declaration>
		compReds.add(_compListRed);
		
		for (Reduction compRed: compReds) {
			// <Struct Declaration> ::= <SpecQualList> <StructDeclList> ';'
			// Now analyse components
			// FIXME: parse type recursively - there might be anonymous structs!
			String compType = getContent_R(compRed.get(0).asReduction(), "");
			// Component names -there might be arrays among them!
			Reduction declListRed = compRed.get(1).asReduction();
			StringList declList = new StringList();
			while (declListRed.getParent().getTableIndex() == RuleConstants.PROD_STRUCTDECLLIST_COMMA) {
				// <StructDeclList> ::= <StructDeclList> ',' <Struct Decl>
				addProcessedCompDecl(compType, declListRed.get(2).asReduction(), declList, components);
				declListRed = declListRed.get(0).asReduction();
			}
			// <StructDeclList> ::= <Struct Decl>
			addProcessedCompDecl(compType, declListRed, declList, components);
			if (!declList.isEmpty()) {
				components.add(declList.reverse().concatenate(", ") + ": " + compType + ";\\");
			}
		}
		return components.reverse();
	}

	/**
	 * Processes the declarator represented by {@code _declRed} and either adds the result to {@code _declList} or
	 * to {@code _groupList}. 
	 * @param _baseType - basic type of the components of this list.
	 * @param _declRed - {@link Reduction} representing a declarator.
	 * @param _declList - {@link StringList} of processed simple declarators of the {@code _baseType}, to be enhanced.
	 * @param _groupList - {@link StringList} of component declarator groups, may be extended by e.g. array components.
	 * @throws ParserCancelled 
	 */
	private void addProcessedCompDecl(String _baseType, Reduction _declRed, StringList _declList, StringList _groupList) throws ParserCancelled {
		String compDecl = getContent_R(_declRed, "");
		int pos = compDecl.indexOf(':');	// bit field?
		if (pos >= 0) {
			compDecl = compDecl.substring(0, pos);
		}
		if (!compDecl.trim().isEmpty()) {
			String ptrs = "";
			if (MATCH_PTR_DECL.reset(compDecl).matches()) {
				ptrs = MATCH_PTR_DECL.group(1);
				compDecl = MATCH_PTR_DECL.group(3);
			}
			pos = compDecl.indexOf("[");
			String index = "";
			if (pos > 0) {
				index = "array " + compDecl.substring(pos) + " of ";
				compDecl = compDecl.substring(0, pos);
			}
			if (!ptrs.isEmpty() || !index.isEmpty()) {
				if (!_declList.isEmpty()) {
					_groupList.add(_declList.reverse().concatenate(", ") + ": " + _baseType + ";\\");
					_declList.clear();
				}
				_groupList.add(compDecl + ": " + index + _baseType + ptrs + ";\\");
			}
			else {
				_declList.add(compDecl);
			}
		}
	}

	/**
	 * Converts a detected C library function to the corresponding Structorizer
	 * built-in routine if possible.
	 * @param _reduction a rule of type &lt;Value&gt; ::= Id '(' [&lt;Expr&gt;] ')'
	 * @param procName - the already extracted routine identifier
	 * @param arguments - list of argument strings
	 * @param _parentNode - the {@link Subqueue} the derived instruction is to be appended to 
	 * @return true if a specific conversion could be applied and all is done.
	 * @throws ParserCancelled 
	 */
	private boolean convertBuiltInRoutines(Reduction _reduction, String procName, StringList arguments,
			Subqueue _parentNode) throws ParserCancelled {
		// Here we should convert certain known library functions to Structorizer built-in procedures
		// START KGU#652 2019-02-13: Issue #679
		String content = convertBuiltInRoutine(procName, arguments, false);
		if (content != null) {
			Instruction procCall = new Instruction(StringList.explode(content, "\n"));
			_parentNode.addElement(this.equipWithSourceComment(procCall, _reduction));
			return true;
		}
		// END KGU#652 2019-02-13
		return false;
	}

	// START KGU#652 2019-02-13: Issue #679: Started to convert some functions
	/**
	 * Tries to convert the function or procedure given by name {@code funcName} and parameters {@code arguments}
	 * into an equivalent built-in routine if available 
	 * @param funcName - name of the function
	 * @param arguments - arguments (as strings) of the function
	 * @param resultNeeded - whether the routine is called within an expression (this may restrict the conversion
	 * options due to assumed result type incompatibility)
	 * @return either a converted call string or null (if no built-in routine was found)
	 */
	private String convertBuiltInRoutine(String funcName, StringList arguments, boolean resultNeeded) {
		String builtin = null;
		int nArgs = arguments.count();
		if (funcName.equals("strlen") && nArgs == 1) {
			builtin = "length(" + arguments.get(0) + ")";
		}
		else if (funcName.equals("strcpy") && nArgs == 2 && !resultNeeded) {
			builtin = arguments.get(0) + " <- " + arguments.get(1);
		}
		else if (funcName.equals("strncpy") && nArgs == 3 && !resultNeeded) {
			builtin = arguments.get(0) + " <- copy(" + arguments.get(1) + ", 1, " + arguments.get(2) + ")";
		}
		else if (funcName.equals("strcat") && nArgs == 2 && !resultNeeded) {
			builtin = arguments.get(0) + " <- " + arguments.get(0) + " + " + arguments.get(1);
		}
		else if (funcName.equals("strncat") && nArgs == 3 && !resultNeeded) {
			builtin = arguments.get(0) + " <- " + arguments.get(0) + " + copy(" + arguments.get(1) + ", 1, " + arguments.get(2) + ")";
		}
		else if (funcName.equals("toupper") && nArgs == 1) {
			builtin = "uppercase(" + arguments.get(0) + ")";
		}
		else if (funcName.equals("tolower") && nArgs == 1) {
			builtin = "lowercase(" + arguments.get(0) + ")";
		}
		else if (funcName.equals("srand") && nArgs == 1 && !resultNeeded) {
			builtin = "randomize()";
		}
		return builtin;
	}
	// END KGU#652 2019-02-13

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
		// START KGU 2017-04-11
		_content = undoIdReplacements(_content);
		// END KGU 2017-04-11
		return _content.trim();
	}
	
	@Override
	protected String getContent_R(Reduction _reduction, String _content) throws ParserCancelled
	{
		if (_reduction == null) {
			System.err.println("STOP!");
			return "";
		}
		int rule_id = _reduction.getParent().getTableIndex();
		// Function call?
		// START KGU#652 2019-02-13: Issue #679
		if (rule_id ==	RuleConstants.PROD_POSTFIXEXP_LPAREN_RPAREN || rule_id == RuleConstants.PROD_POSTFIXEXP_LPAREN_RPAREN2) {
			String fnName = getContent_R(_reduction.get(0).asReduction(), "");
			StringList args = new StringList();
			if (rule_id ==	RuleConstants.PROD_POSTFIXEXP_LPAREN_RPAREN) {
				args = this.getExpressionList(_reduction.get(2).asReduction());
			}
			String builtin = this.convertBuiltInRoutine(fnName,  args, true);
			if (builtin != null) {
				return _content + builtin;
			}
		}
		// END KGU#652 2019-02-13
		for (int i = 0; i < _reduction.size(); i++)
		{
			Token token = _reduction.get(i);
			/* -------- Begin code example for text retrieval and translation -------- */
			switch (token.getType()) 
			{
			case NON_TERMINAL:
				_content = getContent_R(token.asReduction(), _content);
				// START KGU 2018-06-20: Avoid unnecessary gap between unary operator and operand
				// FIXME: repeated unary operators could inadvertently be glued, like "- -a"> "--a" 
				if (token.getName().equals("Unary Op") && _content.endsWith(" ") && !_content.trim().endsWith("not")) {
					_content = _content.substring(0, _content.length()-1);
				}
				// END KGU 2018-06-20
				// START KGU 2017-05-27: There may be strings split over several lines... 
				{
					// Real (unescaped) newlines shouldn't occur within expressions otherwise
					StringList parts = StringList.explode(_content, "\n");
					if (parts.count() > 1) {
						_content = "";
						for (i = 0; i < parts.count(); i++) {
							String sep = " ";	// By default we will just put a space character there
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
			/* -------- End code example for text retrieval and translation -------- */
		}
		
		return _content;
	}

	/**
	 * Routine will return the list of the (translated) expressions (at top level, if a tree is needed,
	 * than keep with the reduction tree.)
	 * @param _reduc - a rule with head &lt;Expr&gt; or &lt;ExprIni&gt; 
	 * @return the list of expressions as strings
	 * @throws ParserCancelled 
	 */
	private StringList getExpressionList(Reduction _reduc) throws ParserCancelled
	{
		StringList exprList = new StringList();
		String ruleHead = _reduc.getParent().getHead().toString();
		if (ruleHead.equals("<Literal>") || ruleHead.equals("<Call Id>") || ruleHead.equals("<Value>") || ruleHead.equals("<Postfix Exp>")) {
			exprList.add(getContent_R(_reduc, ""));
		}
		else while (ruleHead.equals("<ArgExpList>") || ruleHead.equals("<IdentifierList>")) {
			// Get the content from right to left to avoid recursion
			exprList.add(getContent_R(_reduc.get(_reduc.size()-1).asReduction(), ""));
			if (_reduc.size() > 1) {
				_reduc = _reduc.get(0).asReduction();
				ruleHead = _reduc.getParent().getHead().toString();
				if (!ruleHead.equals("<ArgExpList>") && !ruleHead.equals("IdenifierList")) {
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

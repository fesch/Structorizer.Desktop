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
 *      Kay Gürtzig     2018.03.26      First Issue (generated with GOLDprog.exe)
 *      Kay Gürtzig     2018.04.12      RuleConstants updated to corrected grammar (version 1.1)
 *      Kay Gürtzig     2018.06.18      Bugfix #540: replaceDefinedEntries() could get caught in an eternal loop
 *                                      Enh. #541: New option "redundantNames" to eliminate disturbing symbols or macros
 *      Kay Gürtzig     2018.06.19      File decomposed and inheritance changed
 *      Kay Gürtzig     2018.06.20      Most algorithmic structures implemented, bugfixes #545, #546 integrated
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
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.creativewidgetworks.goldparser.engine.*;
import com.creativewidgetworks.goldparser.engine.enums.SymbolType;

import lu.fisch.structorizer.elements.Alternative;
import lu.fisch.structorizer.elements.Case;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.Forever;
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
			RuleConstants.PROD_SELECTIONSTMT_SWITCH_LPAREN_RPAREN_LBRACE_RBRACE,
			RuleConstants.PROD_CASESTMTS_CASE_COLON,
			RuleConstants.PROD_CASESTMTS_DEFAULT_COLON,
			//RuleConstants.PROD_STATEMENT2,
			//RuleConstants.PROD_ASSIGNEXP
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

 	@Override
	public String[] getFileExtensions() {
		// TODO specify the usual file name extensions for ANSI-C99 source files here!";
		final String[] exts = { "c" };
		return exts;
	}

	//---------------------- Grammar table constants DON'T MODIFY! ---------------------------

	// Symbolic constants naming the table indices of the symbols of the grammar 
	//@SuppressWarnings("unused")
	private interface SymbolConstants 
	{
		final int SYM_EOF               =   0;  // (EOF)
		final int SYM_ERROR             =   1;  // (Error)
		final int SYM_COMMENT           =   2;  // Comment
		final int SYM_NEWLINE           =   3;  // NewLine
		final int SYM_WHITESPACE        =   4;  // Whitespace
		final int SYM_TIMESDIV          =   5;  // '*/'
		final int SYM_DIVTIMES          =   6;  // '/*'
		final int SYM_DIVDIV            =   7;  // '//'
		final int SYM_MINUS             =   8;  // '-'
		final int SYM_MINUSMINUS        =   9;  // '--'
		final int SYM_EXCLAM            =  10;  // '!'
		final int SYM_EXCLAMEQ          =  11;  // '!='
		final int SYM_PERCENT           =  12;  // '%'
		final int SYM_PERCENTEQ         =  13;  // '%='
		final int SYM_AMP               =  14;  // '&'
		final int SYM_AMPAMP            =  15;  // '&&'
		final int SYM_AMPEQ             =  16;  // '&='
		final int SYM_LPAREN            =  17;  // '('
		final int SYM_RPAREN            =  18;  // ')'
		final int SYM_TIMES             =  19;  // '*'
		final int SYM_TIMESEQ           =  20;  // '*='
		final int SYM_COMMA             =  21;  // ','
		final int SYM_DOT               =  22;  // '.'
		final int SYM_DOTDOTDOT         =  23;  // '...'
		final int SYM_DIV               =  24;  // '/'
		final int SYM_DIVEQ             =  25;  // '/='
		final int SYM_COLON             =  26;  // ':'
		final int SYM_SEMI              =  27;  // ';'
		final int SYM_QUESTION          =  28;  // '?'
		final int SYM_LBRACKET          =  29;  // '['
		final int SYM_RBRACKET          =  30;  // ']'
		final int SYM_CARET             =  31;  // '^'
		final int SYM_CARETEQ           =  32;  // '^='
		final int SYM__BOOL             =  33;  // '_Bool'
		final int SYM__COMPLEX          =  34;  // '_Complex'
		final int SYM_LBRACE            =  35;  // '{'
		final int SYM_PIPE              =  36;  // '|'
		final int SYM_PIPEPIPE          =  37;  // '||'
		final int SYM_PIPEEQ            =  38;  // '|='
		final int SYM_RBRACE            =  39;  // '}'
		final int SYM_TILDE             =  40;  // '~'
		final int SYM_PLUS              =  41;  // '+'
		final int SYM_PLUSPLUS          =  42;  // '++'
		final int SYM_PLUSEQ            =  43;  // '+='
		final int SYM_LT                =  44;  // '<'
		final int SYM_LTLT              =  45;  // '<<'
		final int SYM_LTLTEQ            =  46;  // '<<='
		final int SYM_LTEQ              =  47;  // '<='
		final int SYM_EQ                =  48;  // '='
		final int SYM_MINUSEQ           =  49;  // '-='
		final int SYM_EQEQ              =  50;  // '=='
		final int SYM_GT                =  51;  // '>'
		final int SYM_MINUSGT           =  52;  // '->'
		final int SYM_GTEQ              =  53;  // '>='
		final int SYM_GTGT              =  54;  // '>>'
		final int SYM_GTGTEQ            =  55;  // '>>='
		final int SYM_AUTO              =  56;  // auto
		final int SYM_BREAK             =  57;  // break
		final int SYM_CASE              =  58;  // case
		final int SYM_CHAR              =  59;  // char
		final int SYM_CHARLITERAL       =  60;  // CharLiteral
		final int SYM_CONST             =  61;  // const
		final int SYM_CONTINUE          =  62;  // continue
		final int SYM_DECLITERAL        =  63;  // DecLiteral
		final int SYM_DEFAULT           =  64;  // default
		final int SYM_DO                =  65;  // do
		final int SYM_DOUBLE            =  66;  // double
		final int SYM_ELSE              =  67;  // else
		final int SYM_ENUM              =  68;  // enum
		final int SYM_EXTERN            =  69;  // extern
		final int SYM_FLOAT             =  70;  // float
		final int SYM_FLOATLITERAL      =  71;  // FloatLiteral
		final int SYM_FOR               =  72;  // for
		final int SYM_GOTO              =  73;  // goto
		final int SYM_HEXLITERAL        =  74;  // HexLiteral
		final int SYM_IDENTIFIER        =  75;  // Identifier
		final int SYM_IF                =  76;  // if
		final int SYM_INLINE            =  77;  // inline
		final int SYM_INT               =  78;  // int
		final int SYM_LONG              =  79;  // long
		final int SYM_OCTLITERAL        =  80;  // OctLiteral
		final int SYM_REGISTER          =  81;  // register
		final int SYM_RESTRICT          =  82;  // restrict
		final int SYM_RETURN            =  83;  // return
		final int SYM_SHORT             =  84;  // short
		final int SYM_SIGNED            =  85;  // signed
		final int SYM_SIZEOF            =  86;  // sizeof
		final int SYM_STATIC            =  87;  // static
		final int SYM_STRINGLITERAL     =  88;  // StringLiteral
		final int SYM_STRUCT            =  89;  // struct
		final int SYM_SWITCH            =  90;  // switch
		final int SYM_TYPEDEF           =  91;  // typedef
		final int SYM_UNION             =  92;  // union
		final int SYM_UNSIGNED          =  93;  // unsigned
		final int SYM_USERTYPEID        =  94;  // UserTypeId
		final int SYM_VOID              =  95;  // void
		final int SYM_VOLATILE          =  96;  // volatile
		final int SYM_WCHAR_T           =  97;  // 'wchar_t'
		final int SYM_WHILE             =  98;  // while
		final int SYM_ABSTRACTDECL      =  99;  // <Abstract Decl>
		final int SYM_ADDEXP            = 100;  // <Add Exp>
		final int SYM_ANDEXP            = 101;  // <And Exp>
		final int SYM_ARGEXPLIST        = 102;  // <ArgExpList>
		final int SYM_ASSIGNEXP         = 103;  // <Assign Exp>
		final int SYM_ASSIGNOP          = 104;  // <Assign Op>
		final int SYM_BLOCKITEM         = 105;  // <BlockItem>
		final int SYM_BLOCKITEMLIST     = 106;  // <BlockItemList>
		final int SYM_CASESTMTS         = 107;  // <Case Stmts>
		final int SYM_CASTEXP           = 108;  // <Cast Exp>
		final int SYM_CONDEXP           = 109;  // <Cond Exp>
		final int SYM_CONSTANTEXP       = 110;  // <Constant Exp>
		final int SYM_DECLSPECIFIERS    = 111;  // <Decl Specifiers>
		final int SYM_DECLSPECS         = 112;  // <Decl Specs>
		final int SYM_DECLARATION       = 113;  // <Declaration>
		final int SYM_DECLARATIONLIST   = 114;  // <DeclarationList>
		final int SYM_DECLARATOR        = 115;  // <Declarator>
		final int SYM_DECLLISTOPT       = 116;  // <DeclListOpt>
		final int SYM_DESIGNATION       = 117;  // <Designation>
		final int SYM_DESIGNATOR        = 118;  // <Designator>
		final int SYM_DESIGNATORLIST    = 119;  // <DesignatorList>
		final int SYM_DIRABSTRDECLOPT   = 120;  // <DirAbstrDeclOpt>
		final int SYM_DIRECTABSTRDECL   = 121;  // <Direct Abstr Decl>
		final int SYM_DIRECTDECL        = 122;  // <Direct Decl>
		final int SYM_ENUMERATOR        = 123;  // <Enumerator>
		final int SYM_ENUMERATORSPEC    = 124;  // <Enumerator Spec>
		final int SYM_ENUMLIST          = 125;  // <EnumList>
		final int SYM_EQUATEXP          = 126;  // <Equat Exp>
		final int SYM_EXCLOREXP         = 127;  // <ExclOr Exp>
		final int SYM_EXPRESSION        = 128;  // <Expression>
		final int SYM_EXPRESSIONSTMT    = 129;  // <Expression Stmt>
		final int SYM_EXPROPT           = 130;  // <ExprOpt>
		final int SYM_EXTERNALDECL      = 131;  // <External Decl>
		final int SYM_FUNCTIONDEF       = 132;  // <Function Def>
		final int SYM_IDENTIFIERLIST    = 133;  // <IdentifierList>
		final int SYM_IDLISTOPT         = 134;  // <IdListOpt>
		final int SYM_INITDECLARATOR    = 135;  // <Init Declarator>
		final int SYM_INITDECLLIST      = 136;  // <InitDeclList>
		final int SYM_INITIALIZER       = 137;  // <Initializer>
		final int SYM_INITIALIZERLIST   = 138;  // <InitializerList>
		final int SYM_ITERATIONSTMT     = 139;  // <Iteration Stmt>
		final int SYM_JUMPSTMT          = 140;  // <Jump Stmt>
		final int SYM_LABELLEDSTMT      = 141;  // <Labelled Stmt>
		final int SYM_LITERAL           = 142;  // <Literal>
		final int SYM_LOGANDEXP         = 143;  // <LogAnd Exp>
		final int SYM_LOGOREXP          = 144;  // <LogOr Exp>
		final int SYM_MULTEXP           = 145;  // <Mult Exp>
		final int SYM_OREXP             = 146;  // <Or Exp>
		final int SYM_PARAMETERDECL     = 147;  // <Parameter Decl>
		final int SYM_PARAMETERLIST     = 148;  // <ParameterList>
		final int SYM_PARAMTYPELIST     = 149;  // <ParamTypeList>
		final int SYM_POINTER           = 150;  // <Pointer>
		final int SYM_POSTFIXEXP        = 151;  // <Postfix Exp>
		final int SYM_RELATEXP          = 152;  // <Relat Exp>
		final int SYM_SELECTIONSTMT     = 153;  // <Selection Stmt>
		final int SYM_SELECTOR          = 154;  // <Selector>
		final int SYM_SHIFTEXP          = 155;  // <Shift Exp>
		final int SYM_SPECQUALLIST      = 156;  // <SpecQualList>
		final int SYM_SPECQUALS         = 157;  // <SpecQuals>
		final int SYM_STATEMENT         = 158;  // <Statement>
		final int SYM_STMTLIST          = 159;  // <StmtList>
		final int SYM_STORAGECLASS      = 160;  // <Storage Class>
		final int SYM_STRUCTDECL        = 161;  // <Struct Decl>
		final int SYM_STRUCTDECLARATION = 162;  // <Struct Declaration>
		final int SYM_STRUCTDECLLIST    = 163;  // <StructDeclList>
		final int SYM_STRUCTDECLNLIST   = 164;  // <StructDeclnList>
		final int SYM_STRUCTORUNION     = 165;  // <StructOrUnion>
		final int SYM_STRUCTORUNIONSPEC = 166;  // <StructOrUnion Spec>
		final int SYM_TRANSLATIONUNIT   = 167;  // <Translation Unit>
		final int SYM_TYPEQUALIFIER     = 168;  // <Type Qualifier>
		final int SYM_TYPESPECIFIER     = 169;  // <Type Specifier>
		final int SYM_TYPEDEFNAME       = 170;  // <Typedef Name>
		final int SYM_TYPENAME          = 171;  // <Typename>
		final int SYM_TYPEQUALLIST      = 172;  // <TypeQualList>
		final int SYM_TYPEQUALSOPT      = 173;  // <TypeQualsOpt>
		final int SYM_UNARYEXP          = 174;  // <Unary Exp>
		final int SYM_UNARYOP           = 175;  // <Unary Op>
		final int SYM_VALUE             = 176;  // <Value>
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
		final int PROD_DECLSPECIFIERS_INLINE                              =   5;  // <Decl Specifiers> ::= inline <Decl Specs>
		final int PROD_DECLSPECS                                          =   6;  // <Decl Specs> ::= <Decl Specifiers>
		final int PROD_DECLSPECS2                                         =   7;  // <Decl Specs> ::= 
		final int PROD_INITDECLLIST_COMMA                                 =   8;  // <InitDeclList> ::= <InitDeclList> ',' <Init Declarator>
		final int PROD_INITDECLLIST                                       =   9;  // <InitDeclList> ::= <Init Declarator>
		final int PROD_INITDECLARATOR_EQ                                  =  10;  // <Init Declarator> ::= <Declarator> '=' <Initializer>
		final int PROD_INITDECLARATOR                                     =  11;  // <Init Declarator> ::= <Declarator>
		final int PROD_STORAGECLASS_TYPEDEF                               =  12;  // <Storage Class> ::= typedef
		final int PROD_STORAGECLASS_EXTERN                                =  13;  // <Storage Class> ::= extern
		final int PROD_STORAGECLASS_STATIC                                =  14;  // <Storage Class> ::= static
		final int PROD_STORAGECLASS_AUTO                                  =  15;  // <Storage Class> ::= auto
		final int PROD_STORAGECLASS_REGISTER                              =  16;  // <Storage Class> ::= register
		final int PROD_TYPESPECIFIER_VOID                                 =  17;  // <Type Specifier> ::= void
		final int PROD_TYPESPECIFIER_CHAR                                 =  18;  // <Type Specifier> ::= char
		final int PROD_TYPESPECIFIER_WCHAR_T                              =  19;  // <Type Specifier> ::= 'wchar_t'
		final int PROD_TYPESPECIFIER_SHORT                                =  20;  // <Type Specifier> ::= short
		final int PROD_TYPESPECIFIER_INT                                  =  21;  // <Type Specifier> ::= int
		final int PROD_TYPESPECIFIER_LONG                                 =  22;  // <Type Specifier> ::= long
		final int PROD_TYPESPECIFIER_FLOAT                                =  23;  // <Type Specifier> ::= float
		final int PROD_TYPESPECIFIER_DOUBLE                               =  24;  // <Type Specifier> ::= double
		final int PROD_TYPESPECIFIER_SIGNED                               =  25;  // <Type Specifier> ::= signed
		final int PROD_TYPESPECIFIER_UNSIGNED                             =  26;  // <Type Specifier> ::= unsigned
		final int PROD_TYPESPECIFIER__BOOL                                =  27;  // <Type Specifier> ::= '_Bool'
		final int PROD_TYPESPECIFIER__COMPLEX                             =  28;  // <Type Specifier> ::= '_Complex'
		final int PROD_TYPESPECIFIER                                      =  29;  // <Type Specifier> ::= <StructOrUnion Spec>
		final int PROD_TYPESPECIFIER2                                     =  30;  // <Type Specifier> ::= <Enumerator Spec>
		final int PROD_TYPESPECIFIER3                                     =  31;  // <Type Specifier> ::= <Typedef Name>
		final int PROD_STRUCTORUNIONSPEC_IDENTIFIER_LBRACE_RBRACE         =  32;  // <StructOrUnion Spec> ::= <StructOrUnion> Identifier '{' <StructDeclnList> '}'
		final int PROD_STRUCTORUNIONSPEC_LBRACE_RBRACE                    =  33;  // <StructOrUnion Spec> ::= <StructOrUnion> '{' <StructDeclnList> '}'
		final int PROD_STRUCTORUNIONSPEC_IDENTIFIER                       =  34;  // <StructOrUnion Spec> ::= <StructOrUnion> Identifier
		final int PROD_STRUCTORUNION_STRUCT                               =  35;  // <StructOrUnion> ::= struct
		final int PROD_STRUCTORUNION_UNION                                =  36;  // <StructOrUnion> ::= union
		final int PROD_STRUCTDECLNLIST                                    =  37;  // <StructDeclnList> ::= <StructDeclnList> <Struct Declaration>
		final int PROD_STRUCTDECLNLIST2                                   =  38;  // <StructDeclnList> ::= <Struct Declaration>
		final int PROD_STRUCTDECLARATION_SEMI                             =  39;  // <Struct Declaration> ::= <SpecQualList> <StructDeclList> ';'
		final int PROD_SPECQUALLIST                                       =  40;  // <SpecQualList> ::= <Type Specifier> <SpecQuals>
		final int PROD_SPECQUALLIST2                                      =  41;  // <SpecQualList> ::= <Type Qualifier> <SpecQuals>
		final int PROD_SPECQUALS                                          =  42;  // <SpecQuals> ::= <SpecQualList>
		final int PROD_SPECQUALS2                                         =  43;  // <SpecQuals> ::= 
		final int PROD_STRUCTDECLLIST_COMMA                               =  44;  // <StructDeclList> ::= <StructDeclList> ',' <Struct Decl>
		final int PROD_STRUCTDECLLIST                                     =  45;  // <StructDeclList> ::= <Struct Decl>
		final int PROD_STRUCTDECL_COLON                                   =  46;  // <Struct Decl> ::= <Declarator> ':' <Constant Exp>
		final int PROD_STRUCTDECL                                         =  47;  // <Struct Decl> ::= <Declarator>
		final int PROD_STRUCTDECL_COLON2                                  =  48;  // <Struct Decl> ::= ':' <Constant Exp>
		final int PROD_ENUMERATORSPEC_ENUM_IDENTIFIER_LBRACE_RBRACE       =  49;  // <Enumerator Spec> ::= enum Identifier '{' <EnumList> '}'
		final int PROD_ENUMERATORSPEC_ENUM_IDENTIFIER_LBRACE_COMMA_RBRACE =  50;  // <Enumerator Spec> ::= enum Identifier '{' <EnumList> ',' '}'
		final int PROD_ENUMERATORSPEC_ENUM_LBRACE_RBRACE                  =  51;  // <Enumerator Spec> ::= enum '{' <EnumList> '}'
		final int PROD_ENUMERATORSPEC_ENUM_LBRACE_COMMA_RBRACE            =  52;  // <Enumerator Spec> ::= enum '{' <EnumList> ',' '}'
		final int PROD_ENUMERATORSPEC_ENUM_IDENTIFIER                     =  53;  // <Enumerator Spec> ::= enum Identifier
		final int PROD_ENUMLIST_COMMA                                     =  54;  // <EnumList> ::= <EnumList> ',' <Enumerator>
		final int PROD_ENUMLIST                                           =  55;  // <EnumList> ::= <Enumerator>
		final int PROD_ENUMERATOR_IDENTIFIER_EQ                           =  56;  // <Enumerator> ::= Identifier '=' <Constant Exp>
		final int PROD_ENUMERATOR_IDENTIFIER                              =  57;  // <Enumerator> ::= Identifier
		final int PROD_TYPEQUALIFIER_CONST                                =  58;  // <Type Qualifier> ::= const
		final int PROD_TYPEQUALIFIER_RESTRICT                             =  59;  // <Type Qualifier> ::= restrict
		final int PROD_TYPEQUALIFIER_VOLATILE                             =  60;  // <Type Qualifier> ::= volatile
		final int PROD_DECLARATOR                                         =  61;  // <Declarator> ::= <Pointer> <Direct Decl>
		final int PROD_DECLARATOR2                                        =  62;  // <Declarator> ::= <Direct Decl>
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
		final int PROD_POINTER_TIMES4                                     =  75;  // <Pointer> ::= '*'
		final int PROD_TYPEQUALLIST                                       =  76;  // <TypeQualList> ::= <Type Qualifier>
		final int PROD_TYPEQUALLIST2                                      =  77;  // <TypeQualList> ::= <TypeQualList> <Type Qualifier>
		final int PROD_TYPEQUALSOPT                                       =  78;  // <TypeQualsOpt> ::= <TypeQualList>
		final int PROD_TYPEQUALSOPT2                                      =  79;  // <TypeQualsOpt> ::= 
		final int PROD_PARAMTYPELIST_COMMA_DOTDOTDOT                      =  80;  // <ParamTypeList> ::= <ParameterList> ',' '...'
		final int PROD_PARAMTYPELIST                                      =  81;  // <ParamTypeList> ::= <ParameterList>
		final int PROD_PARAMETERLIST_COMMA                                =  82;  // <ParameterList> ::= <ParameterList> ',' <Parameter Decl>
		final int PROD_PARAMETERLIST                                      =  83;  // <ParameterList> ::= <Parameter Decl>
		final int PROD_PARAMETERDECL                                      =  84;  // <Parameter Decl> ::= <Decl Specifiers> <Declarator>
		final int PROD_PARAMETERDECL2                                     =  85;  // <Parameter Decl> ::= <Decl Specifiers> <Abstract Decl>
		final int PROD_PARAMETERDECL3                                     =  86;  // <Parameter Decl> ::= <Decl Specifiers>
		final int PROD_IDENTIFIERLIST_COMMA_IDENTIFIER                    =  87;  // <IdentifierList> ::= <IdentifierList> ',' Identifier
		final int PROD_IDENTIFIERLIST_IDENTIFIER                          =  88;  // <IdentifierList> ::= Identifier
		final int PROD_IDLISTOPT                                          =  89;  // <IdListOpt> ::= <IdentifierList>
		final int PROD_IDLISTOPT2                                         =  90;  // <IdListOpt> ::= 
		final int PROD_TYPENAME                                           =  91;  // <Typename> ::= <SpecQualList> <Abstract Decl>
		final int PROD_TYPENAME2                                          =  92;  // <Typename> ::= <SpecQualList>
		final int PROD_ABSTRACTDECL                                       =  93;  // <Abstract Decl> ::= <Pointer> <Direct Abstr Decl>
		final int PROD_ABSTRACTDECL2                                      =  94;  // <Abstract Decl> ::= <Pointer>
		final int PROD_ABSTRACTDECL3                                      =  95;  // <Abstract Decl> ::= <Direct Abstr Decl>
		final int PROD_DIRECTABSTRDECL_LPAREN_RPAREN                      =  96;  // <Direct Abstr Decl> ::= '(' <Abstract Decl> ')'
		final int PROD_DIRECTABSTRDECL_LBRACKET_RBRACKET                  =  97;  // <Direct Abstr Decl> ::= <DirAbstrDeclOpt> '[' <TypeQualsOpt> <Assign Exp> ']'
		final int PROD_DIRECTABSTRDECL_LBRACKET_TIMES_RBRACKET            =  98;  // <Direct Abstr Decl> ::= <DirAbstrDeclOpt> '[' '*' ']'
		final int PROD_DIRECTABSTRDECL_LBRACKET_STATIC_RBRACKET           =  99;  // <Direct Abstr Decl> ::= <DirAbstrDeclOpt> '[' <TypeQualList> static <Assign Exp> ']'
		final int PROD_DIRECTABSTRDECL_LBRACKET_RBRACKET2                 = 100;  // <Direct Abstr Decl> ::= <DirAbstrDeclOpt> '[' <TypeQualsOpt> ']'
		final int PROD_DIRECTABSTRDECL_LBRACKET_STATIC_RBRACKET2          = 101;  // <Direct Abstr Decl> ::= <DirAbstrDeclOpt> '[' static <TypeQualsOpt> <Assign Exp> ']'
		final int PROD_DIRECTABSTRDECL_LPAREN_RPAREN2                     = 102;  // <Direct Abstr Decl> ::= <DirAbstrDeclOpt> '(' <ParamTypeList> ')'
		final int PROD_DIRECTABSTRDECL_LPAREN_RPAREN3                     = 103;  // <Direct Abstr Decl> ::= <DirAbstrDeclOpt> '(' ')'
		final int PROD_DIRABSTRDECLOPT                                    = 104;  // <DirAbstrDeclOpt> ::= <Direct Abstr Decl>
		final int PROD_DIRABSTRDECLOPT2                                   = 105;  // <DirAbstrDeclOpt> ::= 
		final int PROD_TYPEDEFNAME_USERTYPEID                             = 106;  // <Typedef Name> ::= UserTypeId
		final int PROD_INITIALIZER                                        = 107;  // <Initializer> ::= <Assign Exp>
		final int PROD_INITIALIZER_LBRACE_RBRACE                          = 108;  // <Initializer> ::= '{' <InitializerList> '}'
		final int PROD_INITIALIZER_LBRACE_COMMA_RBRACE                    = 109;  // <Initializer> ::= '{' <InitializerList> ',' '}'
		final int PROD_INITIALIZERLIST_COMMA                              = 110;  // <InitializerList> ::= <InitializerList> ',' <Designation>
		final int PROD_INITIALIZERLIST                                    = 111;  // <InitializerList> ::= <Designation>
		final int PROD_DESIGNATION_EQ                                     = 112;  // <Designation> ::= <DesignatorList> '=' <Initializer>
		final int PROD_DESIGNATION                                        = 113;  // <Designation> ::= <Initializer>
		final int PROD_DESIGNATORLIST                                     = 114;  // <DesignatorList> ::= <DesignatorList> <Designator>
		final int PROD_DESIGNATORLIST2                                    = 115;  // <DesignatorList> ::= <Designator>
		final int PROD_DESIGNATOR_LBRACKET_RBRACKET                       = 116;  // <Designator> ::= '[' <Constant Exp> ']'
		final int PROD_DESIGNATOR_DOT_IDENTIFIER                          = 117;  // <Designator> ::= '.' Identifier
		final int PROD_STATEMENT                                          = 118;  // <Statement> ::= <Labelled Stmt>
		final int PROD_STATEMENT_LBRACE_RBRACE                            = 119;  // <Statement> ::= '{' <BlockItemList> '}'
		final int PROD_STATEMENT2                                         = 120;  // <Statement> ::= <Expression Stmt>
		final int PROD_STATEMENT3                                         = 121;  // <Statement> ::= <Selection Stmt>
		final int PROD_STATEMENT4                                         = 122;  // <Statement> ::= <Iteration Stmt>
		final int PROD_STATEMENT5                                         = 123;  // <Statement> ::= <Jump Stmt>
		final int PROD_LABELLEDSTMT_IDENTIFIER_COLON                      = 124;  // <Labelled Stmt> ::= Identifier ':' <Statement>
		final int PROD_BLOCKITEMLIST                                      = 125;  // <BlockItemList> ::= <BlockItemList> <BlockItem>
		final int PROD_BLOCKITEMLIST2                                     = 126;  // <BlockItemList> ::= <BlockItem>
		final int PROD_BLOCKITEM                                          = 127;  // <BlockItem> ::= <Declaration>
		final int PROD_BLOCKITEM2                                         = 128;  // <BlockItem> ::= <Statement>
		final int PROD_EXPRESSIONSTMT_SEMI                                = 129;  // <Expression Stmt> ::= <Expression> ';'
		final int PROD_EXPRESSIONSTMT_SEMI2                               = 130;  // <Expression Stmt> ::= ';'
		final int PROD_SELECTIONSTMT_IF_LPAREN_RPAREN                     = 131;  // <Selection Stmt> ::= if '(' <Expression> ')' <Statement>
		final int PROD_SELECTIONSTMT_IF_LPAREN_RPAREN_ELSE                = 132;  // <Selection Stmt> ::= if '(' <Expression> ')' <Statement> else <Statement>
		final int PROD_SELECTIONSTMT_SWITCH_LPAREN_RPAREN_LBRACE_RBRACE   = 133;  // <Selection Stmt> ::= switch '(' <Expression> ')' '{' <Case Stmts> '}'
		final int PROD_CASESTMTS_CASE_COLON                               = 134;  // <Case Stmts> ::= case <Selector> ':' <StmtList> <Case Stmts>
		final int PROD_CASESTMTS_DEFAULT_COLON                            = 135;  // <Case Stmts> ::= default ':' <StmtList>
		final int PROD_CASESTMTS                                          = 136;  // <Case Stmts> ::= 
		final int PROD_SELECTOR                                           = 137;  // <Selector> ::= <Literal>
		final int PROD_SELECTOR_LPAREN_RPAREN                             = 138;  // <Selector> ::= '(' <Expression> ')'
		final int PROD_STMTLIST                                           = 139;  // <StmtList> ::= <Statement> <StmtList>
		final int PROD_STMTLIST2                                          = 140;  // <StmtList> ::= 
		final int PROD_ITERATIONSTMT_WHILE_LPAREN_RPAREN                  = 141;  // <Iteration Stmt> ::= while '(' <Expression> ')' <Statement>
		final int PROD_ITERATIONSTMT_DO_WHILE_LPAREN_RPAREN_SEMI          = 142;  // <Iteration Stmt> ::= do <Statement> while '(' <Expression> ')' ';'
		final int PROD_ITERATIONSTMT_FOR_LPAREN_SEMI_SEMI_RPAREN          = 143;  // <Iteration Stmt> ::= for '(' <ExprOpt> ';' <ExprOpt> ';' <ExprOpt> ')' <Statement>
		final int PROD_ITERATIONSTMT_FOR_LPAREN_SEMI_RPAREN               = 144;  // <Iteration Stmt> ::= for '(' <Declaration> <ExprOpt> ';' <ExprOpt> ')' <Statement>
		final int PROD_JUMPSTMT_GOTO_IDENTIFIER_SEMI                      = 145;  // <Jump Stmt> ::= goto Identifier ';'
		final int PROD_JUMPSTMT_CONTINUE_SEMI                             = 146;  // <Jump Stmt> ::= continue ';'
		final int PROD_JUMPSTMT_BREAK_SEMI                                = 147;  // <Jump Stmt> ::= break ';'
		final int PROD_JUMPSTMT_RETURN_SEMI                               = 148;  // <Jump Stmt> ::= return <ExprOpt> ';'
		final int PROD_TRANSLATIONUNIT                                    = 149;  // <Translation Unit> ::= <External Decl>
		final int PROD_TRANSLATIONUNIT2                                   = 150;  // <Translation Unit> ::= <Translation Unit> <External Decl>
		final int PROD_EXTERNALDECL                                       = 151;  // <External Decl> ::= <Function Def>
		final int PROD_EXTERNALDECL2                                      = 152;  // <External Decl> ::= <Declaration>
		final int PROD_FUNCTIONDEF_LBRACE_RBRACE                          = 153;  // <Function Def> ::= <Decl Specifiers> <Declarator> <DeclListOpt> '{' <BlockItemList> '}'
		final int PROD_DECLARATIONLIST                                    = 154;  // <DeclarationList> ::= <DeclarationList> <Declaration>
		final int PROD_DECLARATIONLIST2                                   = 155;  // <DeclarationList> ::= <Declaration>
		final int PROD_DECLLISTOPT                                        = 156;  // <DeclListOpt> ::= <DeclarationList>
		final int PROD_DECLLISTOPT2                                       = 157;  // <DeclListOpt> ::= 
		final int PROD_EXPRESSION_COMMA                                   = 158;  // <Expression> ::= <Expression> ',' <Assign Exp>
		final int PROD_EXPRESSION                                         = 159;  // <Expression> ::= <Assign Exp>
		final int PROD_ASSIGNEXP                                          = 160;  // <Assign Exp> ::= <Unary Exp> <Assign Op> <Assign Exp>
		final int PROD_ASSIGNEXP2                                         = 161;  // <Assign Exp> ::= <Cond Exp>
		final int PROD_ASSIGNOP_EQ                                        = 162;  // <Assign Op> ::= '='
		final int PROD_ASSIGNOP_TIMESEQ                                   = 163;  // <Assign Op> ::= '*='
		final int PROD_ASSIGNOP_DIVEQ                                     = 164;  // <Assign Op> ::= '/='
		final int PROD_ASSIGNOP_PERCENTEQ                                 = 165;  // <Assign Op> ::= '%='
		final int PROD_ASSIGNOP_PLUSEQ                                    = 166;  // <Assign Op> ::= '+='
		final int PROD_ASSIGNOP_MINUSEQ                                   = 167;  // <Assign Op> ::= '-='
		final int PROD_ASSIGNOP_LTLTEQ                                    = 168;  // <Assign Op> ::= '<<='
		final int PROD_ASSIGNOP_GTGTEQ                                    = 169;  // <Assign Op> ::= '>>='
		final int PROD_ASSIGNOP_AMPEQ                                     = 170;  // <Assign Op> ::= '&='
		final int PROD_ASSIGNOP_CARETEQ                                   = 171;  // <Assign Op> ::= '^='
		final int PROD_ASSIGNOP_PIPEEQ                                    = 172;  // <Assign Op> ::= '|='
		final int PROD_CONDEXP_QUESTION_COLON                             = 173;  // <Cond Exp> ::= <LogOr Exp> '?' <Expression> ':' <Cond Exp>
		final int PROD_CONDEXP                                            = 174;  // <Cond Exp> ::= <LogOr Exp>
		final int PROD_LOGOREXP_PIPEPIPE                                  = 175;  // <LogOr Exp> ::= <LogOr Exp> '||' <LogAnd Exp>
		final int PROD_LOGOREXP                                           = 176;  // <LogOr Exp> ::= <LogAnd Exp>
		final int PROD_LOGANDEXP_AMPAMP                                   = 177;  // <LogAnd Exp> ::= <LogAnd Exp> '&&' <Or Exp>
		final int PROD_LOGANDEXP                                          = 178;  // <LogAnd Exp> ::= <Or Exp>
		final int PROD_OREXP_PIPE                                         = 179;  // <Or Exp> ::= <Or Exp> '|' <ExclOr Exp>
		final int PROD_OREXP                                              = 180;  // <Or Exp> ::= <ExclOr Exp>
		final int PROD_EXCLOREXP_CARET                                    = 181;  // <ExclOr Exp> ::= <ExclOr Exp> '^' <And Exp>
		final int PROD_EXCLOREXP                                          = 182;  // <ExclOr Exp> ::= <And Exp>
		final int PROD_ANDEXP_AMP                                         = 183;  // <And Exp> ::= <And Exp> '&' <Equat Exp>
		final int PROD_ANDEXP                                             = 184;  // <And Exp> ::= <Equat Exp>
		final int PROD_EQUATEXP_EQEQ                                      = 185;  // <Equat Exp> ::= <Equat Exp> '==' <Relat Exp>
		final int PROD_EQUATEXP_EXCLAMEQ                                  = 186;  // <Equat Exp> ::= <Equat Exp> '!=' <Relat Exp>
		final int PROD_EQUATEXP                                           = 187;  // <Equat Exp> ::= <Relat Exp>
		final int PROD_RELATEXP_GT                                        = 188;  // <Relat Exp> ::= <Relat Exp> '>' <Shift Exp>
		final int PROD_RELATEXP_LT                                        = 189;  // <Relat Exp> ::= <Relat Exp> '<' <Shift Exp>
		final int PROD_RELATEXP_LTEQ                                      = 190;  // <Relat Exp> ::= <Relat Exp> '<=' <Shift Exp>
		final int PROD_RELATEXP_GTEQ                                      = 191;  // <Relat Exp> ::= <Relat Exp> '>=' <Shift Exp>
		final int PROD_RELATEXP                                           = 192;  // <Relat Exp> ::= <Shift Exp>
		final int PROD_SHIFTEXP_LTLT                                      = 193;  // <Shift Exp> ::= <Shift Exp> '<<' <Add Exp>
		final int PROD_SHIFTEXP_GTGT                                      = 194;  // <Shift Exp> ::= <Shift Exp> '>>' <Add Exp>
		final int PROD_SHIFTEXP                                           = 195;  // <Shift Exp> ::= <Add Exp>
		final int PROD_ADDEXP_PLUS                                        = 196;  // <Add Exp> ::= <Add Exp> '+' <Mult Exp>
		final int PROD_ADDEXP_MINUS                                       = 197;  // <Add Exp> ::= <Add Exp> '-' <Mult Exp>
		final int PROD_ADDEXP                                             = 198;  // <Add Exp> ::= <Mult Exp>
		final int PROD_MULTEXP_TIMES                                      = 199;  // <Mult Exp> ::= <Mult Exp> '*' <Cast Exp>
		final int PROD_MULTEXP_DIV                                        = 200;  // <Mult Exp> ::= <Mult Exp> '/' <Cast Exp>
		final int PROD_MULTEXP_PERCENT                                    = 201;  // <Mult Exp> ::= <Mult Exp> '%' <Cast Exp>
		final int PROD_MULTEXP                                            = 202;  // <Mult Exp> ::= <Cast Exp>
		final int PROD_POSTFIXEXP                                         = 203;  // <Postfix Exp> ::= <Value>
		final int PROD_POSTFIXEXP_LBRACKET_RBRACKET                       = 204;  // <Postfix Exp> ::= <Postfix Exp> '[' <Expression> ']'
		final int PROD_POSTFIXEXP_LPAREN_RPAREN                           = 205;  // <Postfix Exp> ::= <Postfix Exp> '(' <ArgExpList> ')'
		final int PROD_POSTFIXEXP_LPAREN_RPAREN2                          = 206;  // <Postfix Exp> ::= <Postfix Exp> '(' ')'
		final int PROD_POSTFIXEXP_DOT_IDENTIFIER                          = 207;  // <Postfix Exp> ::= <Postfix Exp> '.' Identifier
		final int PROD_POSTFIXEXP_MINUSGT_IDENTIFIER                      = 208;  // <Postfix Exp> ::= <Postfix Exp> '->' Identifier
		final int PROD_POSTFIXEXP_PLUSPLUS                                = 209;  // <Postfix Exp> ::= <Postfix Exp> '++'
		final int PROD_POSTFIXEXP_MINUSMINUS                              = 210;  // <Postfix Exp> ::= <Postfix Exp> '--'
		final int PROD_POSTFIXEXP_LPAREN_RPAREN_LBRACE_RBRACE             = 211;  // <Postfix Exp> ::= '(' <Typename> ')' '{' <InitializerList> '}'
		final int PROD_POSTFIXEXP_LPAREN_RPAREN_LBRACE_COMMA_RBRACE       = 212;  // <Postfix Exp> ::= '(' <Typename> ')' '{' <InitializerList> ',' '}'
		final int PROD_ARGEXPLIST                                         = 213;  // <ArgExpList> ::= <Assign Exp>
		final int PROD_ARGEXPLIST_COMMA                                   = 214;  // <ArgExpList> ::= <ArgExpList> ',' <Assign Exp>
		final int PROD_UNARYEXP                                           = 215;  // <Unary Exp> ::= <Postfix Exp>
		final int PROD_UNARYEXP_PLUSPLUS                                  = 216;  // <Unary Exp> ::= '++' <Unary Exp>
		final int PROD_UNARYEXP_MINUSMINUS                                = 217;  // <Unary Exp> ::= '--' <Unary Exp>
		final int PROD_UNARYEXP2                                          = 218;  // <Unary Exp> ::= <Unary Op> <Cast Exp>
		final int PROD_UNARYEXP_SIZEOF                                    = 219;  // <Unary Exp> ::= sizeof <Unary Exp>
		final int PROD_UNARYEXP_SIZEOF_LPAREN_RPAREN                      = 220;  // <Unary Exp> ::= sizeof '(' <Typename> ')'
		final int PROD_UNARYOP_AMP                                        = 221;  // <Unary Op> ::= '&'
		final int PROD_UNARYOP_TIMES                                      = 222;  // <Unary Op> ::= '*'
		final int PROD_UNARYOP_PLUS                                       = 223;  // <Unary Op> ::= '+'
		final int PROD_UNARYOP_MINUS                                      = 224;  // <Unary Op> ::= '-'
		final int PROD_UNARYOP_TILDE                                      = 225;  // <Unary Op> ::= '~'
		final int PROD_UNARYOP_EXCLAM                                     = 226;  // <Unary Op> ::= '!'
		final int PROD_CASTEXP                                            = 227;  // <Cast Exp> ::= <Unary Exp>
		final int PROD_CASTEXP_LPAREN_RPAREN                              = 228;  // <Cast Exp> ::= '(' <Typename> ')' <Cast Exp>
		final int PROD_VALUE_IDENTIFIER                                   = 229;  // <Value> ::= Identifier
		final int PROD_VALUE                                              = 230;  // <Value> ::= <Literal>
		final int PROD_VALUE_LPAREN_RPAREN                                = 231;  // <Value> ::= '(' <Expression> ')'
		final int PROD_LITERAL_DECLITERAL                                 = 232;  // <Literal> ::= DecLiteral
		final int PROD_LITERAL_OCTLITERAL                                 = 233;  // <Literal> ::= OctLiteral
		final int PROD_LITERAL_HEXLITERAL                                 = 234;  // <Literal> ::= HexLiteral
		final int PROD_LITERAL_FLOATLITERAL                               = 235;  // <Literal> ::= FloatLiteral
		final int PROD_LITERAL_STRINGLITERAL                              = 236;  // <Literal> ::= StringLiteral
		final int PROD_LITERAL_CHARLITERAL                                = 237;  // <Literal> ::= CharLiteral
		final int PROD_CONSTANTEXP                                        = 238;  // <Constant Exp> ::= <Cond Exp>
		final int PROD_EXPROPT                                            = 239;  // <ExprOpt> ::= <Expression>
		final int PROD_EXPROPT2                                           = 240;  // <ExprOpt> ::= 
	};

	//---------------------- Build methods for structograms ---------------------------

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
	protected void initializeBuildNSD()
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
	protected void buildNSD_R(Reduction _reduction, Subqueue _parentNode)
	{
		//String content = new String();
	
		if (_reduction.size() > 0)
		{
			String rule = _reduction.getParent().toString();
			String ruleHead = _reduction.getParent().getHead().toString();
			int ruleId = _reduction.getParent().getTableIndex();
			getLogger().log(Level.CONFIG, "Rule {0}, {1}", new Object[]{rule, _parentNode.parent});
			log("buildNSD_R(" + rule + ", " + _parentNode.parent + ")...\n", true);

			if (
					// Function definition?
					ruleId == RuleConstants.PROD_FUNCTIONDEF_LBRACE_RBRACE
					)
			{
				buildFunction(_reduction);				
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
				else if (procName.equals("printf") || procName.equals("puts") && arguments.count() == 1)
				{
					buildOutput(_reduction, procName, arguments, _parentNode);
				}
				else if (procName.equals("scanf") || procName.equals("gets") && arguments.count() == 1){
					buildInput(_reduction, procName, arguments, _parentNode);
				}
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
					// FIXME: What about RuleConstants.PROD_DECLARATION_SEMI2?
					||
					ruleId == RuleConstants.PROD_DECLARATION_SEMI2
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
				if (!isTypedef) {
					String type = tmpTypes.concatenate();
					// START KGU#407 2017-06-22: Enh.#420 grope for possible source comments
					String comment = this.retrieveComment(_reduction);
					// END KGU#407 2017-06-22
					Reduction declReduc = _reduction.get(1).asReduction();
					// Now concern on the declarations of the list (FIXME: test for last argument is too vague) 
					buildDeclsOrAssignments(declReduc, type, parentNode, comment, tmpTypes.contains("struct"));
				}
				// CHECKME!
				if (isGlobal && root != globalRoot && !importingRoots.contains(root)) {
					importingRoots.add(root);
				}
			}
			else if (
					// Labeled instruction?
					ruleId == RuleConstants.PROD_LABELLEDSTMT_IDENTIFIER_COLON
					)
			{
				// <Labelled Stmt> ::= Identifier ':' <Statement>
				String content = _reduction.get(0).asString() + ":";
				Instruction el = new Instruction(content);
				el.setColor(Color.RED);	// will only be seen if the user enables the element
				el.disabled = true;
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
				_parentNode.addElement(this.equipWithSourceComment(new Jump(content.trim()), _reduction));
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
				While ele = new While((getKeyword("preWhile").trim() + " " + translateContent(content) + " " + getKeyword("postWhile").trim()).trim());
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
					 ruleId == RuleConstants.PROD_ITERATIONSTMT_FOR_LPAREN_SEMI_SEMI_RPAREN
					 ||
					 ruleId == RuleConstants.PROD_ITERATIONSTMT_FOR_LPAREN_SEMI_RPAREN
					 )
			{
				// <Iteration Stmt> ::= for '(' <ExprOpt> ';' <ExprOpt> ';' <ExprOpt> ')' <Statement>
				// <Iteration Stmt> ::= for '(' <Declaration> <ExprOpt> ';' <ExprOpt> ')' <Statement>
				// The easiest (and default) approach is always to build WHILE loops here
				// Only in very few cases which are difficult to verify, a FOR loop might
				// be built: The first part must be a single assignment, the variable of
				// which must occur in a comparison in part 2 and in a simple incrementation
				// or decrementation in part3. The next trouble: The incrementation/decremention
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
				int condIx = 4;
				if (ruleId == RuleConstants.PROD_ITERATIONSTMT_FOR_LPAREN_SEMI_RPAREN) {
					condIx = 3;
				}
				String content = getContent_R(_reduction.get(condIx).asReduction(), "");
				Subqueue body = null;
				Element ele = null;
				if (content.trim().isEmpty()) {
					Forever loop = new Forever();
					ele = loop;
					body = loop.getBody();
				}
				else {
					While loop = new While((getKeyword("preWhile").trim() + " " + translateContent(content) + " " + getKeyword("postWhile").trim()).trim());
					ele = loop;
					body = loop.getBody();
				}
				this.equipWithSourceComment(ele, _reduction);
				// Mark all offsprings of the FOR loop with a (by default) yellowish colour
				ele.setColor(colorMisc);
				_parentNode.addElement(ele);
				
				// Get and convert the body
				secReduc = _reduction.get(condIx + 4).asReduction();
				buildNSD_R(secReduc, body);

				// get the last part of the header now and append it to the body
				secReduc = _reduction.get(condIx + 2).asReduction();
				// Problem is that it is typically a simple operator expression,
				// e.g. i++ or --i, so it won't be recognized as statement unless we
				/// impose some extra status
				buildNSD_R(secReduc, body);
				// Mark all offsprings of the FOR loop with a (by default) yellowish colour
				body.getElement(body.getSize()-1).setColor(colorMisc);

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
	 * Processes a function definition from the given {@code _reduction}
	 * @param _reduction - the {@link Reduction} of the parser
	 */
	private void buildFunction(Reduction _reduction) {
		// <Function Def> ::= <Decl Specifiers> <Declarator> <DeclListOpt> '{' <BlockItemList> '}'
		// Find out the name of the function
		Reduction secReduc = _reduction.get(1).asReduction();
		String content = new String();
		boolean weird = false;
		int secRuleId = secReduc.getParent().getTableIndex();
		int nPointers = 0;
		// Drop redundant parentheses
		while (secRuleId == RuleConstants.PROD_DECLARATOR || secRuleId == RuleConstants.PROD_DIRECTDECL_LPAREN_RPAREN) {
			if (secRuleId == RuleConstants.PROD_DECLARATOR) {
				nPointers++;
				secReduc = secReduc.get(1).asReduction();
				secRuleId = secReduc.getParent().getTableIndex();
			}
			secReduc = secReduc.get(1).asReduction();
			secRuleId = secReduc.getParent().getTableIndex();
		} 
		String funcName = null;
		Reduction paramReduc = null;
		switch (secRuleId) {
		case RuleConstants.PROD_DIRECTDECL_IDENTIFIER:
			funcName = getContent_R(secReduc, "");
			break;
		case RuleConstants.PROD_DIRECTDECL_LPAREN_RPAREN2:
		case RuleConstants.PROD_DIRECTDECL_LPAREN_RPAREN3:
			funcName = getContent_R(secReduc.get(0).asReduction(), "");
			paramReduc = secReduc.get(2).asReduction();
			break;
		default: 
			// Something weird like an array of functions or the like
			content = getContent_R(_reduction, "");
			weird = true;
		}
		Root prevRoot = root;	// Cache the original root
		root = new Root();	// Prepare a new root for the (sub)routine
		root.setProgram(false);
		subRoots.add(root);
		// If the previous root was global and had collected elements then make the new root a potential importer
		if (prevRoot.getMethodName().equals("???") && prevRoot.children.getSize() > 0) {
			// We must have inserted some global stuff, so assume a dependency...
			this.importingRoots.add(root);
		}
		// Is there a type specification different from void?
		if (!weird) {
			Token typeToken = _reduction.get(0);
			if (typeToken.getType() == SymbolType.CONTENT) {
				content += typeToken.asString() + " ";
			}
			else {
				// FIXME: We might need a more intelligent type analysis
				content = getContent_R(typeToken.asReduction(), "").trim() + " ";
			}
			content = content.replace("const ", "");
			// Result type void should be suppressed
			if (content.trim().equals("void")) {
				content = "";
			}
			content += funcName + "(";
			String params = "";
			if (paramReduc != null) {
				StringList paramList = new StringList();
				String ellipse = "";
				int ruleId =paramReduc.getParent().getTableIndex(); 
				if (ruleId == RuleConstants.PROD_PARAMTYPELIST_COMMA_DOTDOTDOT) {
					ellipse = ", ...";
					paramReduc = paramReduc.get(0).asReduction();
				}
				switch (ruleId) {
				case RuleConstants.PROD_IDLISTOPT2:
					// Empty argument list
					break;
				case RuleConstants.PROD_IDENTIFIERLIST_IDENTIFIER:	// FIXME does is work for this rule?
				case RuleConstants.PROD_IDENTIFIERLIST_COMMA_IDENTIFIER:
					// Ancient function definition: type foo(a, b, c) type1 a; type2 b; type3 c; {...}
					params = getContent_R(paramReduc, "");
					{
						StringList paramDecls = getDeclsFromDeclList(_reduction.get(2).asReduction());
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
				case RuleConstants.PROD_PARAMETERLIST_COMMA:
					// More than one parameter
					do {
						String param = getContent_R(paramReduc.get(2).asReduction(), "");
						paramReduc = paramReduc.get(0).asReduction();
						ruleId = paramReduc.getParent().getTableIndex();
						paramList.add(param);
					} while (ruleId == RuleConstants.PROD_PARAMETERLIST_COMMA);
					// no break here!
				default: // Should be a <Parameter Decl>
					paramList.add(getContent_R(paramReduc, ""));
					params = paramList.reverse().concatenate(", ") + ellipse;
					break;
				}
			}
			if (params.trim().equals("void")) {
				params = "";
			}
			content += params + ")";
		}
		root.setText(content);
		this.equipWithSourceComment(root, _reduction);
		if (_reduction.get(4).getType() == SymbolType.NON_TERMINAL)
		{
			buildNSD_R(_reduction.get(4).asReduction(), root.children);
		}
		// Restore the original root
		root = prevRoot;
	}
	
	/**
	 * Is to extract the declarations from an old-style parameter declaration list
	 * ({@code <DeclarationList>}) and to convert them into Structorizer-compatible
	 * syntax.
	 * @param _declRed - the {@link Reduction} representing a {@code <Struct Decl>} rule.
	 * @return {@link StringList} of the declaration strings in Structorizer syntax
	 */
	StringList getDeclsFromDeclList(Reduction _declRed)
	{
		StringList decls = new StringList();
		int ruleId = _declRed.getParent().getTableIndex();
		do {
			Reduction varDecl = _declRed;
			if (ruleId == RuleConstants.PROD_DECLARATIONLIST) {
				varDecl = _declRed.get(1).asReduction();
				_declRed = _declRed.get(0).asReduction();
			}
			else {
				_declRed = null;
			}
			int nameIx = varDecl.size() - 3;
			String type = getContent_R(varDecl.get(0).asReduction(), "");
			StringList compNames = new StringList();
			// FIXME
			decls.add(getContent_R(varDecl.get(1).asReduction(), type));
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
		} while (_declRed != null);
		return decls;
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
		while (sr.getParent().getTableIndex() == RuleConstants.PROD_CASESTMTS_CASE_COLON)
		{
			Reduction stmList = (Reduction) sr.get(3).getData();
			if (stmList.getParent().getTableIndex() == RuleConstants.PROD_STMTLIST) {
				// non-empty statement list, so we will have to set up a branch
				j++;
				content += "\n??";
			}
			sr = sr.get(4).asReduction();
		}

		if (sr.getParent().getTableIndex() == RuleConstants.PROD_CASESTMTS_DEFAULT_COLON)
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
		if (_ruleId == RuleConstants.PROD_CASESTMTS_CASE_COLON) {
			// <Case Stmts> ::= case <Selector> ':' <StmtList> <Case Stmts>
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
		if (_ruleId == RuleConstants.PROD_CASESTMTS_CASE_COLON) {
			// We may pass an arbitrary subqueue, the case branch rule goes up to the Case element anyway
			buildNSD_R(_reduction.get(stmListIx+1).asReduction(), _case.qs.get(0));					
		}
		
	}

	/**
	 * Converts a declaration list into a sequence of {@link Instruction} elements.  
	 * @param _reduction - an {@code <InitDeclList>} rule, may be left-recursive
	 * @param _type - the common data type as string
	 * @param _parentNode - the {@link Subqueue} the built Instruction is to be appended to
	 * @param _comment - a retrieved source code comment to be placed in the element or null
	 * @param _forceDecl - if a declaration must be produced (e.g. in case of a struct type) 
	 */
	private void buildDeclsOrAssignments(Reduction _reduction, String _type, Subqueue _parentNode, String _comment,
			boolean _forceDecl) {
		log("\tanalyzing <InitDeclList> ...\n", false);
		// Resolve the left recursion
		LinkedList<Reduction> decls = new LinkedList<Reduction>();
		while (_reduction  != null) {
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
			buildDeclOrAssignment(red, _type, _parentNode, _comment, _forceDecl);
		}
		log("\t<InitDeclList> done.\n", false);
	}

	/**
	 * Converts a rule with head &lt;Init Declarator&gt; (as part of a declaration) into an
	 * Instruction element.  
	 * @param _reduc - the Reduction object (PROD_VAR_ID or PROD_VAR_ID_EQ)
	 * @param _type - the data type as string
	 * @param _parentNode - the {@link Subqueue} the built Instruction is to be appended to
	 * @param _comment - a retrieved source code comment to be placed in the element or null
	 * @param _forceDecl - if a declaration must be produced (e.g. in case of a struct type)
	 */
	private void buildDeclOrAssignment(Reduction _reduc, String _type, Subqueue _parentNode, String _comment, boolean _forceDecl)
	{
		boolean isConstant = _type != null && _type.startsWith("const ");	// Is it sure that const will be at the beginning?
		int ruleId = _reduc.getParent().getTableIndex();
		String content = getContent_R(_reduc, "");	// Default???
		String expr = null;
		if (ruleId == RuleConstants.PROD_INITDECLARATOR_EQ) {
			log("\ttrying <Declarator> '=' <Initializer> ...\n", false);
			content = this.getContent_R(_reduc.get(0).asReduction(), "");
			expr = this.getContent_R(_reduc.get(2).asReduction(), "");
			_reduc = _reduc.get(0).asReduction();
		}
		else {
			log("\ttrying <Declarator> ...\n", false);
			// Simple declaration - if allowed then make it to a Pascal decl.
			_forceDecl = this.optionImportVarDecl || _forceDecl;
		}
		if (_forceDecl) {
			if (ruleId == RuleConstants.PROD_DECLARATOR) {
				log("\ttrying <Pointer> <Direct Decl> ...\n", false);
				// This should be the <Pointers> token...
				_type = this.getContent_R(_reduc.get(0).asReduction(), _type);
			}
			if (isConstant) {
				content = "const " + content + ": " + _type.substring("const ".length());
			}
			else {
				content = "var " + content + ": " + _type;
			}
		}
		if (_forceDecl || expr != null) {
			if (expr != null) {
				content += "<- " + expr;
			}
			Element instr = new Instruction(translateContent(content));
			if (_comment != null) {
				instr.setComment(_comment);
			}
			if (_parentNode.parent instanceof Root && ((Root)_parentNode.parent).getMethodName().equals("???")) {
				instr.getComment().add("Globally declared!");
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
			// Constant colour has priority
			if (isConstant) {
				instr.setColor(colorConst);
			}
			_parentNode.addElement(instr);
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
		log("\tfallen back with rule " + ruleId + " (" + _reduc.getParent().toString() + ")\n", false);
	}
	
	/**
	 * Processes type specifications for a variable / constant declaration or a
	 * type definition (argument {@code _declaringVars} indicates which of both).
	 * If an anonymous struct description is found then a type definition object
	 * will be inserted to {@code _subqueue} - either with a generic name (if 
	 * {@code _typeList} is empty) or with the first element of {@code _typeList}
	 * as name. Except in the latter case (type definition with given name created)
	 * the name of the found type will be inserted at the beginning of
	 * {@code _typeList}.
	 * If {@code _isGlobal} is true and a type definition is to be created then
	 * a dependency of the current {@link #root} to the global diagram is established
	 * in {@code this.importingRoots}.
	 * The trouble here is that we would like to return several things at once:
	 * a type entry, a type description, and some flags. For a recursive application,
	 * we would even need different resulting formats.
	 * @param _reduction - current {@link Reduction} object
	 * @param _ruleId - table id of the production rule
	 * @param _subqueue - the {@link Subqueue} to which elements are to be added
	 * @param _isGlobal - whether the type / variable is a global one
	 * @param _typeList - a container for type names, both for input and output 
	 * @param _declaringVars - whether this is used by a variable/constant declaration (type definition otherwise)
	 * @return a logical value indicating whether the processed rule was a type definition
	 */
	protected boolean processTypes(Reduction _reduction, int _ruleId, Subqueue _parentNode, boolean _isGlobal,
			StringList _typeList, boolean _declaringVars)
	{
		boolean isStruct = false;
		boolean isTypedef = false;
		String type = "int";
		boolean isConstant = false;
		boolean addType = true;
		StringList storage = new StringList();
		StringList specifiers = new StringList();
		StringList qualifiers = new StringList();
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
		while (_reduction.getParent().getHead().toString().equals("<Decl Specifiers>")) {
			Token prefix = _reduction.get(0);
			switch (_ruleId) {
			case RuleConstants.PROD_DECLSPECIFIERS: // <Decl Specifiers> ::= <Storage Class> <Decl Specs>
				storage.add(prefix.toString());
				break;
			case RuleConstants.PROD_DECLSPECIFIERS2: // <Decl Specifiers> ::= <Type Specifier> <Decl Specs>
				if (prefix.getType() == SymbolType.NON_TERMINAL) {
					switch (prefix.asReduction().getParent().getTableIndex()) {
					case RuleConstants.PROD_TYPESPECIFIER:	// rather unlikely (represented by one of the following)
					case RuleConstants.PROD_STRUCTORUNIONSPEC_IDENTIFIER_LBRACE_RBRACE:
					case RuleConstants.PROD_STRUCTORUNIONSPEC_LBRACE_RBRACE:
					case RuleConstants.PROD_STRUCTORUNIONSPEC_IDENTIFIER:
					{
						Reduction structRed = prefix.asReduction();
						if (structRed.size() == 2) {
							type = structRed.get(1).asString();
							// TODO retrieve type
						}
						else {
							if (structRed.size() == 4) {
								type = String.format("AnonStruct%1$03d", typeCount++);
							}
							else {
								type = structRed.get(1).asString();
							}
							StringList components = getCompsFromStructDef(structRed.get(structRed.size()-2).asReduction());
							// compose and define type
							components.insert("type " + type + " = struct{\\", 0);
							components.add("}");
							Instruction typedef = new Instruction(components);
							_parentNode.addElement(typedef);
							typedef.updateTypeMap(typeMap);
						}
						isStruct = true;
					}
						break;
					case RuleConstants.PROD_TYPESPECIFIER2:
						break;
					case RuleConstants.PROD_TYPESPECIFIER3:
						break;
					}
				}
				else {
					specifiers.add(prefix.toString());
				}
				break;
			case RuleConstants.PROD_DECLSPECIFIERS3: // <Decl Specifiers> ::= <Type Specifier> <Decl Specs>
				qualifiers.add(prefix.toString());
				break;
			default:
			}
			_reduction = _reduction.get(1).asReduction();
			_ruleId = _reduction.getParent().getTableIndex();
			
		}
		if (isConstant && _declaringVars) {
			type = "const " + type;
		}
		if (addType) {
			_typeList.insert(type, 0);
		}
		return isTypedef;
	}

	/**
	 * Is to extract the struct component declarations from a struct definition and
	 * to convert them into Structorizer (Pascal-like) syntax.
	 * @param _compListRed - the reduction representing the component list
	 * @return The {@link StringList} of component groups
	 */
	private StringList getCompsFromStructDef(Reduction _compListRed)
	{
		// Resolve the left recursion non-recursively
		StringList components = new StringList();
		LinkedList<Reduction> compReds = new LinkedList<Reduction>();
		while (_compListRed.size() == 2) {
			compReds.addFirst(_compListRed.get(1).asReduction());
			_compListRed = _compListRed.get(0).asReduction();
		}
		compReds.addFirst(_compListRed);
		for (Reduction compRed: compReds) {
			// <Struct Declaration> ::= <SpecQualList> <StructDeclList> ';'
			System.out.println(getContent_R(compRed, ""));
			// TODO: Now analyse components (recursively)
			String compType = getContent_R(compRed.get(0).asReduction(), "");
			Reduction declListRed = compRed.get(1).asReduction();
			StringList declList = new StringList();
			while (declListRed.getParent().getTableIndex() == RuleConstants.PROD_STRUCTDECLLIST_COMMA) {
				declList.add(getContent_R(declListRed.get(2).asReduction(), ""));
				declListRed = declListRed.get(0).asReduction();
			}
			declList.add(getContent_R(declListRed, ""));
			components.add(declList.reverse().concatenate(", ") + ": " + compType + ";\\");
		}
		return components;
	}

	/**
	 * Converts a detected C library function to the corresponding Structorizer
	 * built-in routine if possible.
	 * @param _reduction a rule of type &lt;Value&gt; ::= Id '(' [&lt;Expr&gt;] ')'
	 * @param procName - the already extracted routine identifier
	 * @param arguments - list of argument strings
	 * @param _parentNode - the {@link Subqueue} the derived instruction is to be appended to 
	 * @return true if a specific conversion could be applied and all is done.
	 */
	private boolean convertBuiltInRoutines(Reduction _reduction, String procName, StringList arguments,
			Subqueue _parentNode) {
		// TODO Here we should convert certain known library functions to Structorizer built-in procedures
		return false;
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
	 */
	private StringList getExpressionList(Reduction _reduc)
	{
		StringList exprList = new StringList();
		String ruleHead = _reduc.getParent().getHead().toString();
		if (ruleHead.equals("<Literal>") || ruleHead.equals("<Call Id>")) {
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

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
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import lu.fisch.utils.StringList;

/**
 * Code import parser class of Structorizer 3.27, based on GOLDParser 5.0 for the ANSI-C99 language.
 * This file contains grammar-specific constants and individual routines to build
 * structograms (Nassi-Shneiderman diagrams) from the parsing tree. 
 * @author Kay Gürtzig
 */
public class C99Parser extends CodeParser
{

	/** Default diagram name for an importable program diagram with global definitions */
	private static final String DEFAULT_GLOBAL_NAME = "GlobalDefinitions";
	/** Template for the generation of grammar-conform user type ids (typedef-declared) */
	private static final String USER_TYPE_ID_MASK = "user_type_%03d";
	/** Replacement pattern for the decomposition of composed typdefs (named struct def + type def) */
	private static final String TYPEDEF_DECOMP_REPLACER = "$1 $2;\ntypedef $1 $3;";
	/** rule ids representing statements, used as stoppers for comment retrieval (enh. #420) */
	private static final int[] statementIds = new int[]{
			/* TODO: Fill in the RuleConstants members of those productions that are
			 * to be associated with comments found in their syntax subtrees or their
			 * immediate environment. */
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

	/**
	 * If this flag is set then program names derived from file name will be made uppercase
	 * This default will be initialized in consistency with the Analyser check 
	 */
	private boolean optionUpperCaseProgName = false;

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

	//------------------- Comment delimiter specification ---------------------------------
	
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

	//---------------------- Grammar table constants DON'T MODIFY! ---------------------------

	// Symbolic constants naming the table indices of the symbols of the grammar 
	@SuppressWarnings("unused")
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
		final int SYM__BOOL             =  32;  // '_Bool'
		final int SYM__COMPLEX          =  33;  // '_Complex'
		final int SYM_LBRACE            =  34;  // '{'
		final int SYM_PIPE              =  35;  // '|'
		final int SYM_PIPEPIPE          =  36;  // '||'
		final int SYM_PIPEEQ            =  37;  // '|='
		final int SYM_RBRACE            =  38;  // '}'
		final int SYM_TILDE             =  39;  // '~'
		final int SYM_PLUS              =  40;  // '+'
		final int SYM_PLUSPLUS          =  41;  // '++'
		final int SYM_PLUSEQ            =  42;  // '+='
		final int SYM_LT                =  43;  // '<'
		final int SYM_LTLT              =  44;  // '<<'
		final int SYM_LTLTEQ            =  45;  // '<<='
		final int SYM_LTEQ              =  46;  // '<='
		final int SYM_EQ                =  47;  // '='
		final int SYM_MINUSEQ           =  48;  // '-='
		final int SYM_EQEQ              =  49;  // '=='
		final int SYM_GT                =  50;  // '>'
		final int SYM_MINUSGT           =  51;  // '->'
		final int SYM_GTEQ              =  52;  // '>='
		final int SYM_GTGT              =  53;  // '>>'
		final int SYM_GTGTEQ            =  54;  // '>>='
		final int SYM_AUTO              =  55;  // auto
		final int SYM_BREAK             =  56;  // break
		final int SYM_CASE              =  57;  // case
		final int SYM_CHAR              =  58;  // char
		final int SYM_CHARLITERAL       =  59;  // CharLiteral
		final int SYM_CONST             =  60;  // const
		final int SYM_CONTINUE          =  61;  // continue
		final int SYM_DECLITERAL        =  62;  // DecLiteral
		final int SYM_DEFAULT           =  63;  // default
		final int SYM_DO                =  64;  // do
		final int SYM_DOUBLE            =  65;  // double
		final int SYM_ELSE              =  66;  // else
		final int SYM_ENUM              =  67;  // enum
		final int SYM_EXTERN            =  68;  // extern
		final int SYM_FLOAT             =  69;  // float
		final int SYM_FLOATLITERAL      =  70;  // FloatLiteral
		final int SYM_FOR               =  71;  // for
		final int SYM_GOTO              =  72;  // goto
		final int SYM_HEXLITERAL        =  73;  // HexLiteral
		final int SYM_IDENTIFIER        =  74;  // Identifier
		final int SYM_IF                =  75;  // if
		final int SYM_INLINE            =  76;  // inline
		final int SYM_INT               =  77;  // int
		final int SYM_LITERAL           =  78;  // Literal
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
		final int SYM_LITERAL2          = 142;  // <Literal>
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
	@SuppressWarnings("unused")
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
		final int PROD_SELECTOR_LITERAL                                   = 137;  // <Selector> ::= Literal
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
		final int PROD_ASSIGNOP_CARET                                     = 171;  // <Assign Op> ::= '^'
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

	private String ParserEncoding;
	private String ParserPath;
	
	/** Fix list of common preprocesssor-defined type names for convenience */
	private	final String[][] typeReplacements = new String[][] {
		{"size_t", "unsigned long"},
		{"time_t", "unsigned long"},
		{"ptrdiff_t", "unsigned long"}
	};
	
	static HashMap<String, String[]> defines = new LinkedHashMap<String, String[]>();

	final static Pattern PTRN_VOID_CAST = Pattern.compile("(^\\s*|.*?[^\\w\\s]+\\s*)\\(\\s*void\\s*\\)(.*?)");
	static Matcher mtchVoidCast = PTRN_VOID_CAST.matcher("");

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
	// multiple things we can ignore: #pragma, #warning, #error, #message 
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
				return "// preparser include: ";
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
		if (toReplace.trim().isEmpty()) {
			return "";
		}
		//log("CParser.replaceDefinedEntries(): " + Matcher.quoteReplacement((String)entry.getValue().toString()) + "\n", false);
		for (Entry<String, String[]> entry: defines.entrySet()) {
			
			// FIXME: doesn't work if entry is at start/end of toReplace 
			
			
			if (entry.getValue().length > 1) {
				//          key<val[0]>     <   val[1]   >
				// #define	a1(a2,a3,a4)	stuff (  a2  )
				// key  (  text1, text2, text3 )	--->	stuff (  text1  )
				// #define	a1(a2,a3,a4)
				// key  (  text1, text2, text3 )	--->
				// #define	a1(a2,a3,a4)	a2
				// key  (  text1, text2, text3 )	--->	text1
				// #define	a1(a2,a3,a4)	some text
				// key  (  text1, text2, text3 )	--->	some text
				// The trouble here is that text1, text2 etc. might also contain parentheses, so may the following text.
				// The result of the replacement would then be a total desaster
				while (toReplace.matches("(^|.*?\\W)" + entry.getKey() + "\\s*\\(.*\\).*?")) {
					if (entry.getValue()[0].isEmpty()) {
						toReplace = toReplace.replaceAll("(^|.*?\\W)" + entry.getKey() + "(\\s*)\\((.*)\\)(.*?)", "$1$2$4");
					} else {
						// The greedy quantifier inside the parentheses ensures that we get to the rightmost closing parenthesis
						String argsRaw = toReplace.replaceFirst("(^|.*?\\W)" + entry.getKey() + "(\\s*)\\((.*)\\)(.*)", "$3");
						// Now we split the balanced substring (up to the first unexpected closing parenthesis) syntactically
						// (The unmatched tail of argsRaw will be re-appended later)
						StringList args = Element.splitExpressionList(argsRaw, ",");
						// We test whether argument and parameter count match
						if (args.count() != entry.getValue().length - 1) {
							// FIXME: function-like define doesn't match arg count
							log("CParser.replaceDefinedEntries() cannot apply function macro\n\t"
									+ entry.getKey() + entry.getValue().toString() + "\n\tdue to arg count diffs:\n\t"
									+ toReplace + "\n", true);
						}
						else {
							HashMap<String, String> argMap = new HashMap<String, String>();
							// Lest the substitutions should interfere with one another we first split the string for all parameters
							StringList parts = StringList.getNew(entry.getValue()[0]); 
							for (int i = 0; i < args.count(); i++) {
								String param = entry.getValue()[i+1];
								argMap.put(param, args.get(i));
								parts = StringList.explodeWithDelimiter(parts, param);
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
				toReplace = toReplace.replaceAll("(.*?\\W)" + entry.getKey() + "(\\W.*?)",
						"$1" + Matcher.quoteReplacement((String) entry.getValue()[0]) + "$2");
			}
		}
		return toReplace;
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
//				// Simply convert it as text and create an instruction. In case of a call
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
//			/* -------- End code example for tree analysis and build -------- */
//			// Block...?
//			else
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

	@Override
	protected void subclassUpdateRoot(Root root, String sourceFileName) {
		// TODO Auto-generated method stub
		
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

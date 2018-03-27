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
		return "ANSI-C99.egt";
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
		final int SYM_WHILE             =  97;  // while
		final int SYM_ABSTRACTDECL      =  98;  // <Abstract Decl>
		final int SYM_ADDEXP            =  99;  // <Add Exp>
		final int SYM_ANDEXP            = 100;  // <And Exp>
		final int SYM_ARGEXPLIST        = 101;  // <ArgExpList>
		final int SYM_ASSIGNEXP         = 102;  // <Assign Exp>
		final int SYM_ASSIGNOP          = 103;  // <Assign Op>
		final int SYM_BLOCKITEM         = 104;  // <BlockItem>
		final int SYM_BLOCKITEMLIST     = 105;  // <BlockItemList>
		final int SYM_CASESTMTS         = 106;  // <Case Stmts>
		final int SYM_CASTEXP           = 107;  // <Cast Exp>
		final int SYM_CONDEXP           = 108;  // <Cond Exp>
		final int SYM_CONSTANTEXP       = 109;  // <Constant Exp>
		final int SYM_DECLSPECIFIERS    = 110;  // <Decl Specifiers>
		final int SYM_DECLSPECS         = 111;  // <Decl Specs>
		final int SYM_DECLARATION       = 112;  // <Declaration>
		final int SYM_DECLARATIONLIST   = 113;  // <DeclarationList>
		final int SYM_DECLARATOR        = 114;  // <Declarator>
		final int SYM_DECLLISTOPT       = 115;  // <DeclListOpt>
		final int SYM_DESIGNATION       = 116;  // <Designation>
		final int SYM_DESIGNATOR        = 117;  // <Designator>
		final int SYM_DESIGNATORLIST    = 118;  // <DesignatorList>
		final int SYM_DIRABSTRDECLOPT   = 119;  // <DirAbstrDeclOpt>
		final int SYM_DIRECTABSTRDECL   = 120;  // <Direct Abstr Decl>
		final int SYM_DIRECTDECL        = 121;  // <Direct Decl>
		final int SYM_ENUMERATOR        = 122;  // <Enumerator>
		final int SYM_ENUMERATORSPEC    = 123;  // <Enumerator Spec>
		final int SYM_ENUMLIST          = 124;  // <EnumList>
		final int SYM_EQUATEXP          = 125;  // <Equat Exp>
		final int SYM_EXCLOREXP         = 126;  // <ExclOr Exp>
		final int SYM_EXPRESSION        = 127;  // <Expression>
		final int SYM_EXPRESSIONSTMT    = 128;  // <Expression Stmt>
		final int SYM_EXPROPT           = 129;  // <ExprOpt>
		final int SYM_EXTERNALDECL      = 130;  // <External Decl>
		final int SYM_FUNCTIONDEF       = 131;  // <Function Def>
		final int SYM_IDENTIFIERLIST    = 132;  // <IdentifierList>
		final int SYM_IDLISTOPT         = 133;  // <IdListOpt>
		final int SYM_INITDECLARATOR    = 134;  // <Init Declarator>
		final int SYM_INITDECLLIST      = 135;  // <InitDeclList>
		final int SYM_INITIALIZER       = 136;  // <Initializer>
		final int SYM_INITIALIZERLIST   = 137;  // <InitializerList>
		final int SYM_ITERATIONSTMT     = 138;  // <Iteration Stmt>
		final int SYM_JUMPSTMT          = 139;  // <Jump Stmt>
		final int SYM_LABELLEDSTMT      = 140;  // <Labelled Stmt>
		final int SYM_LITERAL2          = 141;  // <Literal>
		final int SYM_LOGANDEXP         = 142;  // <LogAnd Exp>
		final int SYM_LOGOREXP          = 143;  // <LogOr Exp>
		final int SYM_MULTEXP           = 144;  // <Mult Exp>
		final int SYM_OREXP             = 145;  // <Or Exp>
		final int SYM_PARAMETERDECL     = 146;  // <Parameter Decl>
		final int SYM_PARAMETERLIST     = 147;  // <ParameterList>
		final int SYM_PARAMTYPELIST     = 148;  // <ParamTypeList>
		final int SYM_POINTER           = 149;  // <Pointer>
		final int SYM_POSTFIXEXP        = 150;  // <Postfix Exp>
		final int SYM_RELATEXP          = 151;  // <Relat Exp>
		final int SYM_SELECTIONSTMT     = 152;  // <Selection Stmt>
		final int SYM_SELECTOR          = 153;  // <Selector>
		final int SYM_SHIFTEXP          = 154;  // <Shift Exp>
		final int SYM_SPECQUALLIST      = 155;  // <SpecQualList>
		final int SYM_SPECQUALS         = 156;  // <SpecQuals>
		final int SYM_STATEMENT         = 157;  // <Statement>
		final int SYM_STMTLIST          = 158;  // <StmtList>
		final int SYM_STORAGECLASS      = 159;  // <Storage Class>
		final int SYM_STRUCTDECL        = 160;  // <Struct Decl>
		final int SYM_STRUCTDECLARATION = 161;  // <Struct Declaration>
		final int SYM_STRUCTDECLLIST    = 162;  // <StructDeclList>
		final int SYM_STRUCTDECLNLIST   = 163;  // <StructDeclnList>
		final int SYM_STRUCTORUNION     = 164;  // <StructOrUnion>
		final int SYM_STRUCTORUNIONSPEC = 165;  // <StructOrUnion Spec>
		final int SYM_TRANSLATIONUNIT   = 166;  // <Translation Unit>
		final int SYM_TYPEQUALIFIER     = 167;  // <Type Qualifier>
		final int SYM_TYPESPECIFIER     = 168;  // <Type Specifier>
		final int SYM_TYPEDEFNAME       = 169;  // <Typedef Name>
		final int SYM_TYPENAME          = 170;  // <Typename>
		final int SYM_TYPEQUALLIST      = 171;  // <TypeQualList>
		final int SYM_TYPEQUALSOPT      = 172;  // <TypeQualsOpt>
		final int SYM_UNARYEXP          = 173;  // <Unary Exp>
		final int SYM_UNARYOP           = 174;  // <Unary Op>
		final int SYM_VALUE             = 175;  // <Value>
	};

	// Symbolic constants naming the table indices of the grammar rules
	@SuppressWarnings("unused")
	private interface RuleConstants
	{
		final int PROD_DECLARATION_SEMI                                   =   0;  // <Declaration> ::= <Decl Specifiers> <InitDeclList> ';'
		final int PROD_DECLSPECIFIERS                                     =   1;  // <Decl Specifiers> ::= <Storage Class> <Decl Specs>
		final int PROD_DECLSPECIFIERS2                                    =   2;  // <Decl Specifiers> ::= <Type Specifier> <Decl Specs>
		final int PROD_DECLSPECIFIERS3                                    =   3;  // <Decl Specifiers> ::= <Type Qualifier> <Decl Specs>
		final int PROD_DECLSPECIFIERS_INLINE                              =   4;  // <Decl Specifiers> ::= inline <Decl Specs>
		final int PROD_DECLSPECS                                          =   5;  // <Decl Specs> ::= <Decl Specifiers>
		final int PROD_DECLSPECS2                                         =   6;  // <Decl Specs> ::= 
		final int PROD_INITDECLLIST_COMMA                                 =   7;  // <InitDeclList> ::= <InitDeclList> ',' <Init Declarator>
		final int PROD_INITDECLLIST                                       =   8;  // <InitDeclList> ::= <Init Declarator>
		final int PROD_INITDECLARATOR_EQ                                  =   9;  // <Init Declarator> ::= <Declarator> '=' <Initializer>
		final int PROD_INITDECLARATOR                                     =  10;  // <Init Declarator> ::= <Declarator>
		final int PROD_STORAGECLASS_TYPEDEF                               =  11;  // <Storage Class> ::= typedef
		final int PROD_STORAGECLASS_EXTERN                                =  12;  // <Storage Class> ::= extern
		final int PROD_STORAGECLASS_STATIC                                =  13;  // <Storage Class> ::= static
		final int PROD_STORAGECLASS_AUTO                                  =  14;  // <Storage Class> ::= auto
		final int PROD_STORAGECLASS_REGISTER                              =  15;  // <Storage Class> ::= register
		final int PROD_TYPESPECIFIER_VOID                                 =  16;  // <Type Specifier> ::= void
		final int PROD_TYPESPECIFIER_CHAR                                 =  17;  // <Type Specifier> ::= char
		final int PROD_TYPESPECIFIER_SHORT                                =  18;  // <Type Specifier> ::= short
		final int PROD_TYPESPECIFIER_INT                                  =  19;  // <Type Specifier> ::= int
		final int PROD_TYPESPECIFIER_LONG                                 =  20;  // <Type Specifier> ::= long
		final int PROD_TYPESPECIFIER_FLOAT                                =  21;  // <Type Specifier> ::= float
		final int PROD_TYPESPECIFIER_DOUBLE                               =  22;  // <Type Specifier> ::= double
		final int PROD_TYPESPECIFIER_SIGNED                               =  23;  // <Type Specifier> ::= signed
		final int PROD_TYPESPECIFIER_UNSIGNED                             =  24;  // <Type Specifier> ::= unsigned
		final int PROD_TYPESPECIFIER__BOOL                                =  25;  // <Type Specifier> ::= '_Bool'
		final int PROD_TYPESPECIFIER__COMPLEX                             =  26;  // <Type Specifier> ::= '_Complex'
		final int PROD_TYPESPECIFIER                                      =  27;  // <Type Specifier> ::= <StructOrUnion Spec>
		final int PROD_TYPESPECIFIER2                                     =  28;  // <Type Specifier> ::= <Enumerator Spec>
		final int PROD_TYPESPECIFIER3                                     =  29;  // <Type Specifier> ::= <Typedef Name>
		final int PROD_STRUCTORUNIONSPEC_IDENTIFIER_LBRACE_RBRACE         =  30;  // <StructOrUnion Spec> ::= <StructOrUnion> Identifier '{' <StructDeclnList> '}'
		final int PROD_STRUCTORUNIONSPEC_LBRACE_RBRACE                    =  31;  // <StructOrUnion Spec> ::= <StructOrUnion> '{' <StructDeclnList> '}'
		final int PROD_STRUCTORUNIONSPEC_IDENTIFIER                       =  32;  // <StructOrUnion Spec> ::= <StructOrUnion> Identifier
		final int PROD_STRUCTORUNION_STRUCT                               =  33;  // <StructOrUnion> ::= struct
		final int PROD_STRUCTORUNION_UNION                                =  34;  // <StructOrUnion> ::= union
		final int PROD_STRUCTDECLNLIST_COMMA                              =  35;  // <StructDeclnList> ::= <StructDeclnList> ',' <Struct Declaration>
		final int PROD_STRUCTDECLNLIST                                    =  36;  // <StructDeclnList> ::= <Struct Declaration>
		final int PROD_STRUCTDECLARATION_SEMI                             =  37;  // <Struct Declaration> ::= <SpecQualList> <StructDeclList> ';'
		final int PROD_SPECQUALLIST                                       =  38;  // <SpecQualList> ::= <Type Specifier> <SpecQuals>
		final int PROD_SPECQUALLIST2                                      =  39;  // <SpecQualList> ::= <Type Qualifier> <SpecQuals>
		final int PROD_SPECQUALS                                          =  40;  // <SpecQuals> ::= <SpecQualList>
		final int PROD_SPECQUALS2                                         =  41;  // <SpecQuals> ::= 
		final int PROD_STRUCTDECLLIST_COMMA                               =  42;  // <StructDeclList> ::= <StructDeclList> ',' <Struct Decl>
		final int PROD_STRUCTDECLLIST                                     =  43;  // <StructDeclList> ::= <Struct Decl>
		final int PROD_STRUCTDECL_COLON                                   =  44;  // <Struct Decl> ::= <Declarator> ':' <Constant Exp>
		final int PROD_STRUCTDECL                                         =  45;  // <Struct Decl> ::= <Declarator>
		final int PROD_STRUCTDECL_COLON2                                  =  46;  // <Struct Decl> ::= ':' <Constant Exp>
		final int PROD_ENUMERATORSPEC_ENUM_IDENTIFIER_LBRACE_RBRACE       =  47;  // <Enumerator Spec> ::= enum Identifier '{' <EnumList> '}'
		final int PROD_ENUMERATORSPEC_ENUM_IDENTIFIER_LBRACE_COMMA_RBRACE =  48;  // <Enumerator Spec> ::= enum Identifier '{' <EnumList> ',' '}'
		final int PROD_ENUMERATORSPEC_ENUM_LBRACE_RBRACE                  =  49;  // <Enumerator Spec> ::= enum '{' <EnumList> '}'
		final int PROD_ENUMERATORSPEC_ENUM_LBRACE_COMMA_RBRACE            =  50;  // <Enumerator Spec> ::= enum '{' <EnumList> ',' '}'
		final int PROD_ENUMERATORSPEC_ENUM_IDENTIFIER                     =  51;  // <Enumerator Spec> ::= enum Identifier
		final int PROD_ENUMLIST_COMMA                                     =  52;  // <EnumList> ::= <EnumList> ',' <Enumerator>
		final int PROD_ENUMLIST                                           =  53;  // <EnumList> ::= <Enumerator>
		final int PROD_ENUMERATOR_IDENTIFIER_EQ                           =  54;  // <Enumerator> ::= Identifier '=' <Constant Exp>
		final int PROD_ENUMERATOR_IDENTIFIER                              =  55;  // <Enumerator> ::= Identifier
		final int PROD_TYPEQUALIFIER_CONST                                =  56;  // <Type Qualifier> ::= const
		final int PROD_TYPEQUALIFIER_RESTRICT                             =  57;  // <Type Qualifier> ::= restrict
		final int PROD_TYPEQUALIFIER_VOLATILE                             =  58;  // <Type Qualifier> ::= volatile
		final int PROD_DECLARATOR                                         =  59;  // <Declarator> ::= <Pointer> <Direct Decl>
		final int PROD_DECLARATOR2                                        =  60;  // <Declarator> ::= <Direct Decl>
		final int PROD_DIRECTDECL_IDENTIFIER                              =  61;  // <Direct Decl> ::= Identifier
		final int PROD_DIRECTDECL_LPAREN_RPAREN                           =  62;  // <Direct Decl> ::= '(' <Declarator> ')'
		final int PROD_DIRECTDECL_LBRACKET_RBRACKET                       =  63;  // <Direct Decl> ::= <Direct Decl> '[' <TypeQualsOpt> <Assign Exp> ']'
		final int PROD_DIRECTDECL_LBRACKET_TIMES_RBRACKET                 =  64;  // <Direct Decl> ::= <Direct Decl> '[' <TypeQualsOpt> '*' ']'
		final int PROD_DIRECTDECL_LBRACKET_STATIC_RBRACKET                =  65;  // <Direct Decl> ::= <Direct Decl> '[' <TypeQualList> static <Assign Exp> ']'
		final int PROD_DIRECTDECL_LBRACKET_RBRACKET2                      =  66;  // <Direct Decl> ::= <Direct Decl> '[' <TypeQualsOpt> ']'
		final int PROD_DIRECTDECL_LBRACKET_STATIC_RBRACKET2               =  67;  // <Direct Decl> ::= <Direct Decl> '[' static <TypeQualsOpt> <Assign Exp> ']'
		final int PROD_DIRECTDECL_LPAREN_RPAREN2                          =  68;  // <Direct Decl> ::= <Direct Decl> '(' <ParamTypeList> ')'
		final int PROD_DIRECTDECL_LPAREN_RPAREN3                          =  69;  // <Direct Decl> ::= <Direct Decl> '(' <IdListOpt> ')'
		final int PROD_POINTER_TIMES                                      =  70;  // <Pointer> ::= '*' <TypeQualList> <Pointer>
		final int PROD_POINTER_TIMES2                                     =  71;  // <Pointer> ::= '*' <TypeQualList>
		final int PROD_POINTER_TIMES3                                     =  72;  // <Pointer> ::= '*' <Pointer>
		final int PROD_POINTER_TIMES4                                     =  73;  // <Pointer> ::= '*'
		final int PROD_TYPEQUALLIST                                       =  74;  // <TypeQualList> ::= <Type Qualifier>
		final int PROD_TYPEQUALLIST2                                      =  75;  // <TypeQualList> ::= <TypeQualList> <Type Qualifier>
		final int PROD_TYPEQUALSOPT                                       =  76;  // <TypeQualsOpt> ::= <TypeQualList>
		final int PROD_TYPEQUALSOPT2                                      =  77;  // <TypeQualsOpt> ::= 
		final int PROD_PARAMTYPELIST_COMMA_DOTDOTDOT                      =  78;  // <ParamTypeList> ::= <ParameterList> ',' '...'
		final int PROD_PARAMTYPELIST                                      =  79;  // <ParamTypeList> ::= <ParameterList>
		final int PROD_PARAMETERLIST_COMMA                                =  80;  // <ParameterList> ::= <ParameterList> ',' <Parameter Decl>
		final int PROD_PARAMETERLIST                                      =  81;  // <ParameterList> ::= <Parameter Decl>
		final int PROD_PARAMETERDECL                                      =  82;  // <Parameter Decl> ::= <Decl Specifiers> <Declarator>
		final int PROD_PARAMETERDECL2                                     =  83;  // <Parameter Decl> ::= <Decl Specifiers> <Abstract Decl>
		final int PROD_PARAMETERDECL3                                     =  84;  // <Parameter Decl> ::= <Decl Specifiers>
		final int PROD_IDENTIFIERLIST_COMMA_IDENTIFIER                    =  85;  // <IdentifierList> ::= <IdentifierList> ',' Identifier
		final int PROD_IDENTIFIERLIST_IDENTIFIER                          =  86;  // <IdentifierList> ::= Identifier
		final int PROD_IDLISTOPT                                          =  87;  // <IdListOpt> ::= <IdentifierList>
		final int PROD_IDLISTOPT2                                         =  88;  // <IdListOpt> ::= 
		final int PROD_TYPENAME                                           =  89;  // <Typename> ::= <SpecQualList> <Abstract Decl>
		final int PROD_TYPENAME2                                          =  90;  // <Typename> ::= <SpecQualList>
		final int PROD_ABSTRACTDECL                                       =  91;  // <Abstract Decl> ::= <Pointer> <Direct Abstr Decl>
		final int PROD_ABSTRACTDECL2                                      =  92;  // <Abstract Decl> ::= <Pointer>
		final int PROD_ABSTRACTDECL3                                      =  93;  // <Abstract Decl> ::= <Direct Abstr Decl>
		final int PROD_DIRECTABSTRDECL_LPAREN_RPAREN                      =  94;  // <Direct Abstr Decl> ::= '(' <Abstract Decl> ')'
		final int PROD_DIRECTABSTRDECL_LBRACKET_RBRACKET                  =  95;  // <Direct Abstr Decl> ::= <DirAbstrDeclOpt> '[' <TypeQualsOpt> <Assign Exp> ']'
		final int PROD_DIRECTABSTRDECL_LBRACKET_TIMES_RBRACKET            =  96;  // <Direct Abstr Decl> ::= <DirAbstrDeclOpt> '[' '*' ']'
		final int PROD_DIRECTABSTRDECL_LBRACKET_STATIC_RBRACKET           =  97;  // <Direct Abstr Decl> ::= <DirAbstrDeclOpt> '[' <TypeQualList> static <Assign Exp> ']'
		final int PROD_DIRECTABSTRDECL_LBRACKET_RBRACKET2                 =  98;  // <Direct Abstr Decl> ::= <DirAbstrDeclOpt> '[' <TypeQualsOpt> ']'
		final int PROD_DIRECTABSTRDECL_LBRACKET_STATIC_RBRACKET2          =  99;  // <Direct Abstr Decl> ::= <DirAbstrDeclOpt> '[' static <TypeQualsOpt> <Assign Exp> ']'
		final int PROD_DIRECTABSTRDECL_LPAREN_RPAREN2                     = 100;  // <Direct Abstr Decl> ::= <DirAbstrDeclOpt> '(' <ParamTypeList> ')'
		final int PROD_DIRECTABSTRDECL_LPAREN_RPAREN3                     = 101;  // <Direct Abstr Decl> ::= <DirAbstrDeclOpt> '(' ')'
		final int PROD_DIRABSTRDECLOPT                                    = 102;  // <DirAbstrDeclOpt> ::= <Direct Abstr Decl>
		final int PROD_DIRABSTRDECLOPT2                                   = 103;  // <DirAbstrDeclOpt> ::= 
		final int PROD_TYPEDEFNAME_USERTYPEID                             = 104;  // <Typedef Name> ::= UserTypeId
		final int PROD_INITIALIZER                                        = 105;  // <Initializer> ::= <Assign Exp>
		final int PROD_INITIALIZER_LBRACE_RBRACE                          = 106;  // <Initializer> ::= '{' <InitializerList> '}'
		final int PROD_INITIALIZER_LBRACE_COMMA_RBRACE                    = 107;  // <Initializer> ::= '{' <InitializerList> ',' '}'
		final int PROD_INITIALIZERLIST_COMMA                              = 108;  // <InitializerList> ::= <InitializerList> ',' <Designation>
		final int PROD_INITIALIZERLIST                                    = 109;  // <InitializerList> ::= <Designation>
		final int PROD_DESIGNATION_EQ                                     = 110;  // <Designation> ::= <DesignatorList> '=' <Initializer>
		final int PROD_DESIGNATION                                        = 111;  // <Designation> ::= <Initializer>
		final int PROD_DESIGNATORLIST                                     = 112;  // <DesignatorList> ::= <DesignatorList> <Designator>
		final int PROD_DESIGNATORLIST2                                    = 113;  // <DesignatorList> ::= <Designator>
		final int PROD_DESIGNATOR_LBRACKET_RBRACKET                       = 114;  // <Designator> ::= '[' <Constant Exp> ']'
		final int PROD_DESIGNATOR_DOT_IDENTIFIER                          = 115;  // <Designator> ::= '.' Identifier
		final int PROD_STATEMENT                                          = 116;  // <Statement> ::= <Labelled Stmt>
		final int PROD_STATEMENT_LBRACE_RBRACE                            = 117;  // <Statement> ::= '{' <BlockItemList> '}'
		final int PROD_STATEMENT2                                         = 118;  // <Statement> ::= <Expression Stmt>
		final int PROD_STATEMENT3                                         = 119;  // <Statement> ::= <Selection Stmt>
		final int PROD_STATEMENT4                                         = 120;  // <Statement> ::= <Iteration Stmt>
		final int PROD_STATEMENT5                                         = 121;  // <Statement> ::= <Jump Stmt>
		final int PROD_LABELLEDSTMT_IDENTIFIER_COLON                      = 122;  // <Labelled Stmt> ::= Identifier ':' <Statement>
		final int PROD_BLOCKITEMLIST                                      = 123;  // <BlockItemList> ::= <BlockItemList> <BlockItem>
		final int PROD_BLOCKITEMLIST2                                     = 124;  // <BlockItemList> ::= <BlockItem>
		final int PROD_BLOCKITEM                                          = 125;  // <BlockItem> ::= <Declaration>
		final int PROD_BLOCKITEM2                                         = 126;  // <BlockItem> ::= <Statement>
		final int PROD_EXPRESSIONSTMT_SEMI                                = 127;  // <Expression Stmt> ::= <Expression> ';'
		final int PROD_EXPRESSIONSTMT_SEMI2                               = 128;  // <Expression Stmt> ::= ';'
		final int PROD_SELECTIONSTMT_IF_LPAREN_RPAREN                     = 129;  // <Selection Stmt> ::= if '(' <Expression> ')' <Statement>
		final int PROD_SELECTIONSTMT_IF_LPAREN_RPAREN_ELSE                = 130;  // <Selection Stmt> ::= if '(' <Expression> ')' <Statement> else <Statement>
		final int PROD_SELECTIONSTMT_SWITCH_LPAREN_RPAREN_LBRACE_RBRACE   = 131;  // <Selection Stmt> ::= switch '(' <Expression> ')' '{' <Case Stmts> '}'
		final int PROD_CASESTMTS_CASE_COLON                               = 132;  // <Case Stmts> ::= case <Selector> ':' <StmtList> <Case Stmts>
		final int PROD_CASESTMTS_DEFAULT_COLON                            = 133;  // <Case Stmts> ::= default ':' <StmtList>
		final int PROD_CASESTMTS                                          = 134;  // <Case Stmts> ::= 
		final int PROD_SELECTOR_LITERAL                                   = 135;  // <Selector> ::= Literal
		final int PROD_SELECTOR_LPAREN_RPAREN                             = 136;  // <Selector> ::= '(' <Expression> ')'
		final int PROD_STMTLIST                                           = 137;  // <StmtList> ::= <Statement> <StmtList>
		final int PROD_STMTLIST2                                          = 138;  // <StmtList> ::= 
		final int PROD_ITERATIONSTMT_WHILE_LPAREN_RPAREN                  = 139;  // <Iteration Stmt> ::= while '(' <Expression> ')' <Statement>
		final int PROD_ITERATIONSTMT_DO_WHILE_LPAREN_RPAREN_SEMI          = 140;  // <Iteration Stmt> ::= do <Statement> while '(' <Expression> ')' ';'
		final int PROD_ITERATIONSTMT_FOR_LPAREN_SEMI_SEMI_RPAREN          = 141;  // <Iteration Stmt> ::= for '(' <ExprOpt> ';' <ExprOpt> ';' <ExprOpt> ')' <Statement>
		final int PROD_ITERATIONSTMT_FOR_LPAREN_SEMI_RPAREN               = 142;  // <Iteration Stmt> ::= for '(' <Declaration> <ExprOpt> ';' <ExprOpt> ')' <Statement>
		final int PROD_JUMPSTMT_GOTO_IDENTIFIER_SEMI                      = 143;  // <Jump Stmt> ::= goto Identifier ';'
		final int PROD_JUMPSTMT_CONTINUE_SEMI                             = 144;  // <Jump Stmt> ::= continue ';'
		final int PROD_JUMPSTMT_BREAK_SEMI                                = 145;  // <Jump Stmt> ::= break ';'
		final int PROD_JUMPSTMT_RETURN_SEMI                               = 146;  // <Jump Stmt> ::= return <ExprOpt> ';'
		final int PROD_TRANSLATIONUNIT                                    = 147;  // <Translation Unit> ::= <External Decl>
		final int PROD_TRANSLATIONUNIT2                                   = 148;  // <Translation Unit> ::= <Translation Unit> <External Decl>
		final int PROD_EXTERNALDECL                                       = 149;  // <External Decl> ::= <Function Def>
		final int PROD_EXTERNALDECL2                                      = 150;  // <External Decl> ::= <Declaration>
		final int PROD_FUNCTIONDEF_LBRACE_RBRACE                          = 151;  // <Function Def> ::= <Decl Specifiers> <Declarator> <DeclListOpt> '{' <BlockItemList> '}'
		final int PROD_DECLARATIONLIST                                    = 152;  // <DeclarationList> ::= <DeclarationList> <Declaration>
		final int PROD_DECLARATIONLIST2                                   = 153;  // <DeclarationList> ::= <Declaration>
		final int PROD_DECLLISTOPT                                        = 154;  // <DeclListOpt> ::= <DeclarationList>
		final int PROD_DECLLISTOPT2                                       = 155;  // <DeclListOpt> ::= 
		final int PROD_EXPRESSION_COMMA                                   = 156;  // <Expression> ::= <Expression> ',' <Assign Exp>
		final int PROD_EXPRESSION                                         = 157;  // <Expression> ::= <Assign Exp>
		final int PROD_ASSIGNEXP                                          = 158;  // <Assign Exp> ::= <Unary Exp> <Assign Op> <Assign Exp>
		final int PROD_ASSIGNEXP2                                         = 159;  // <Assign Exp> ::= <Cond Exp>
		final int PROD_ASSIGNOP_EQ                                        = 160;  // <Assign Op> ::= '='
		final int PROD_ASSIGNOP_TIMESEQ                                   = 161;  // <Assign Op> ::= '*='
		final int PROD_ASSIGNOP_DIVEQ                                     = 162;  // <Assign Op> ::= '/='
		final int PROD_ASSIGNOP_PERCENTEQ                                 = 163;  // <Assign Op> ::= '%='
		final int PROD_ASSIGNOP_PLUSEQ                                    = 164;  // <Assign Op> ::= '+='
		final int PROD_ASSIGNOP_MINUSEQ                                   = 165;  // <Assign Op> ::= '-='
		final int PROD_ASSIGNOP_LTLTEQ                                    = 166;  // <Assign Op> ::= '<<='
		final int PROD_ASSIGNOP_GTGTEQ                                    = 167;  // <Assign Op> ::= '>>='
		final int PROD_ASSIGNOP_AMPEQ                                     = 168;  // <Assign Op> ::= '&='
		final int PROD_ASSIGNOP_CARET                                     = 169;  // <Assign Op> ::= '^'
		final int PROD_ASSIGNOP_PIPEEQ                                    = 170;  // <Assign Op> ::= '|='
		final int PROD_CONDEXP_QUESTION_COLON                             = 171;  // <Cond Exp> ::= <LogOr Exp> '?' <Expression> ':' <Cond Exp>
		final int PROD_CONDEXP                                            = 172;  // <Cond Exp> ::= <LogOr Exp>
		final int PROD_LOGOREXP_PIPEPIPE                                  = 173;  // <LogOr Exp> ::= <LogOr Exp> '||' <LogAnd Exp>
		final int PROD_LOGOREXP                                           = 174;  // <LogOr Exp> ::= <LogAnd Exp>
		final int PROD_LOGANDEXP_AMPAMP                                   = 175;  // <LogAnd Exp> ::= <LogAnd Exp> '&&' <Or Exp>
		final int PROD_LOGANDEXP                                          = 176;  // <LogAnd Exp> ::= <Or Exp>
		final int PROD_OREXP_PIPE                                         = 177;  // <Or Exp> ::= <Or Exp> '|' <ExclOr Exp>
		final int PROD_OREXP                                              = 178;  // <Or Exp> ::= <ExclOr Exp>
		final int PROD_EXCLOREXP_CARET                                    = 179;  // <ExclOr Exp> ::= <ExclOr Exp> '^' <And Exp>
		final int PROD_EXCLOREXP                                          = 180;  // <ExclOr Exp> ::= <And Exp>
		final int PROD_ANDEXP_AMP                                         = 181;  // <And Exp> ::= <And Exp> '&' <Equat Exp>
		final int PROD_ANDEXP                                             = 182;  // <And Exp> ::= <Equat Exp>
		final int PROD_EQUATEXP_EQEQ                                      = 183;  // <Equat Exp> ::= <Equat Exp> '==' <Relat Exp>
		final int PROD_EQUATEXP_EXCLAMEQ                                  = 184;  // <Equat Exp> ::= <Equat Exp> '!=' <Relat Exp>
		final int PROD_EQUATEXP                                           = 185;  // <Equat Exp> ::= <Relat Exp>
		final int PROD_RELATEXP_GT                                        = 186;  // <Relat Exp> ::= <Relat Exp> '>' <Shift Exp>
		final int PROD_RELATEXP_LT                                        = 187;  // <Relat Exp> ::= <Relat Exp> '<' <Shift Exp>
		final int PROD_RELATEXP_LTEQ                                      = 188;  // <Relat Exp> ::= <Relat Exp> '<=' <Shift Exp>
		final int PROD_RELATEXP_GTEQ                                      = 189;  // <Relat Exp> ::= <Relat Exp> '>=' <Shift Exp>
		final int PROD_RELATEXP                                           = 190;  // <Relat Exp> ::= <Shift Exp>
		final int PROD_SHIFTEXP_LTLT                                      = 191;  // <Shift Exp> ::= <Shift Exp> '<<' <Add Exp>
		final int PROD_SHIFTEXP_GTGT                                      = 192;  // <Shift Exp> ::= <Shift Exp> '>>' <Add Exp>
		final int PROD_SHIFTEXP                                           = 193;  // <Shift Exp> ::= <Add Exp>
		final int PROD_ADDEXP_PLUS                                        = 194;  // <Add Exp> ::= <Add Exp> '+' <Mult Exp>
		final int PROD_ADDEXP_MINUS                                       = 195;  // <Add Exp> ::= <Add Exp> '-' <Mult Exp>
		final int PROD_ADDEXP                                             = 196;  // <Add Exp> ::= <Mult Exp>
		final int PROD_MULTEXP_TIMES                                      = 197;  // <Mult Exp> ::= <Mult Exp> '*' <Cast Exp>
		final int PROD_MULTEXP_DIV                                        = 198;  // <Mult Exp> ::= <Mult Exp> '/' <Cast Exp>
		final int PROD_MULTEXP_PERCENT                                    = 199;  // <Mult Exp> ::= <Mult Exp> '%' <Cast Exp>
		final int PROD_MULTEXP                                            = 200;  // <Mult Exp> ::= <Cast Exp>
		final int PROD_POSTFIXEXP                                         = 201;  // <Postfix Exp> ::= <Value>
		final int PROD_POSTFIXEXP_LBRACKET_RBRACKET                       = 202;  // <Postfix Exp> ::= <Postfix Exp> '[' <Expression> ']'
		final int PROD_POSTFIXEXP_LPAREN_RPAREN                           = 203;  // <Postfix Exp> ::= <Postfix Exp> '(' <ArgExpList> ')'
		final int PROD_POSTFIXEXP_DOT_IDENTIFIER                          = 204;  // <Postfix Exp> ::= <Postfix Exp> '.' Identifier
		final int PROD_POSTFIXEXP_MINUSGT_IDENTIFIER                      = 205;  // <Postfix Exp> ::= <Postfix Exp> '->' Identifier
		final int PROD_POSTFIXEXP_PLUSPLUS                                = 206;  // <Postfix Exp> ::= <Postfix Exp> '++'
		final int PROD_POSTFIXEXP_MINUSMINUS                              = 207;  // <Postfix Exp> ::= <Postfix Exp> '--'
		final int PROD_POSTFIXEXP_LPAREN_RPAREN_LBRACE_RBRACE             = 208;  // <Postfix Exp> ::= '(' <Typename> ')' '{' <InitializerList> '}'
		final int PROD_POSTFIXEXP_LPAREN_RPAREN_LBRACE_COMMA_RBRACE       = 209;  // <Postfix Exp> ::= '(' <Typename> ')' '{' <InitializerList> ',' '}'
		final int PROD_ARGEXPLIST                                         = 210;  // <ArgExpList> ::= <Assign Exp>
		final int PROD_ARGEXPLIST_COMMA                                   = 211;  // <ArgExpList> ::= <ArgExpList> ',' <Assign Exp>
		final int PROD_UNARYEXP                                           = 212;  // <Unary Exp> ::= <Postfix Exp>
		final int PROD_UNARYEXP_PLUSPLUS                                  = 213;  // <Unary Exp> ::= '++' <Unary Exp>
		final int PROD_UNARYEXP_MINUSMINUS                                = 214;  // <Unary Exp> ::= '--' <Unary Exp>
		final int PROD_UNARYEXP2                                          = 215;  // <Unary Exp> ::= <Unary Op> <Cast Exp>
		final int PROD_UNARYEXP_SIZEOF                                    = 216;  // <Unary Exp> ::= sizeof <Unary Exp>
		final int PROD_UNARYEXP_SIZEOF_LPAREN_RPAREN                      = 217;  // <Unary Exp> ::= sizeof '(' <Typename> ')'
		final int PROD_UNARYOP_AMP                                        = 218;  // <Unary Op> ::= '&'
		final int PROD_UNARYOP_TIMES                                      = 219;  // <Unary Op> ::= '*'
		final int PROD_UNARYOP_PLUS                                       = 220;  // <Unary Op> ::= '+'
		final int PROD_UNARYOP_MINUS                                      = 221;  // <Unary Op> ::= '-'
		final int PROD_UNARYOP_TILDE                                      = 222;  // <Unary Op> ::= '~'
		final int PROD_UNARYOP_EXCLAM                                     = 223;  // <Unary Op> ::= '!'
		final int PROD_CASTEXP                                            = 224;  // <Cast Exp> ::= <Unary Exp>
		final int PROD_CASTEXP_LPAREN_RPAREN                              = 225;  // <Cast Exp> ::= '(' <Typename> ')' <Cast Exp>
		final int PROD_VALUE_IDENTIFIER                                   = 226;  // <Value> ::= Identifier
		final int PROD_VALUE                                              = 227;  // <Value> ::= <Literal>
		final int PROD_VALUE_LPAREN_RPAREN                                = 228;  // <Value> ::= '(' <Expression> ')'
		final int PROD_LITERAL_DECLITERAL                                 = 229;  // <Literal> ::= DecLiteral
		final int PROD_LITERAL_OCTLITERAL                                 = 230;  // <Literal> ::= OctLiteral
		final int PROD_LITERAL_HEXLITERAL                                 = 231;  // <Literal> ::= HexLiteral
		final int PROD_LITERAL_FLOATLITERAL                               = 232;  // <Literal> ::= FloatLiteral
		final int PROD_LITERAL_STRINGLITERAL                              = 233;  // <Literal> ::= StringLiteral
		final int PROD_LITERAL_CHARLITERAL                                = 234;  // <Literal> ::= CharLiteral
		final int PROD_CONSTANTEXP                                        = 235;  // <Constant Exp> ::= <Cond Exp>
		final int PROD_EXPROPT                                            = 236;  // <ExprOpt> ::= <Expression>
		final int PROD_EXPROPT2                                           = 237;  // <ExprOpt> ::= 
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
	
	private	final String[][] typeReplacements = new String[][] {
		{"size_t", "unsigned long"},
		{"time_t", "unsigned long"},
		// FIXME: to be made configurable
		{"cob_u8_t", "unsigned int"}
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

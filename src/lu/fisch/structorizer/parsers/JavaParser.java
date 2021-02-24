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

import java.awt.Color;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Class to parse a Java SE 8 file and build structograms from the reduction tree.
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
 *      Kay Gürtzig     2018.06.30      Enh. #553: hooks for possible thread cancellation inserted.
 *
 ******************************************************************************************************
 *
 *      Revision List (this parser)
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2021-02-16      First Issue (generated with GOLDprog.exe)
 *      Kay Gürtzig     2021-02-23      Functionally complete implementation
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
import java.util.Stack;
import java.util.logging.Level;

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
import lu.fisch.structorizer.elements.Try;
import lu.fisch.structorizer.elements.While;
import lu.fisch.utils.StringList;

/**
 * Code import parser class of Structorizer 3.27, based on GOLDParser 5.0 for the Java SE 8 language.
 * This file contains grammar-specific constants and individual routines to build
 * structograms (Nassi-Shneiderman diagrams) from the parsing tree. 
 * @author Kay Gürtzig
 */
public class JavaParser extends CodeParser
{

	/** Rule ids representing statements, used as stoppers for comment retrieval (enh. #420) */
	private static final int[] statementIds = new int[]{
			/* TODO: Fill in the RuleConstants members of those productions that are
			 * to be associated with comments found in their syntax subtrees or their
			 * immediate environment. */
			RuleConstants.PROD_NORMALCLASSDECLARATION,
			RuleConstants.PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER,
			RuleConstants.PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER2,
			RuleConstants.PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER3,
			RuleConstants.PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER4,
			RuleConstants.PROD_LOCALCLASSDECLARATION,
			RuleConstants.PROD_FIELDDECLARATION_SEMI,
			RuleConstants.PROD_FIELDDECLARATION_SEMI2,
			RuleConstants.PROD_METHODDECLARATION,
			RuleConstants.PROD_CONSTRUCTORDECLARATION,
			RuleConstants.PROD_CONSTRUCTORDECLARATION2,
			RuleConstants.PROD_CONSTRUCTORDECLARATION3,
			RuleConstants.PROD_CONSTRUCTORDECLARATION4,
			RuleConstants.PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER,
			RuleConstants.PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER2,
			RuleConstants.PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER3,
			RuleConstants.PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER4,
			RuleConstants.PROD_ENUMDECLARATION_ENUM_IDENTIFIER,
			RuleConstants.PROD_ENUMDECLARATION_ENUM_IDENTIFIER2,
			RuleConstants.PROD_STATICINITIALIZER_STATIC,
			RuleConstants.PROD_LOCALVARIABLEDECLARATION_FINAL,
			RuleConstants.PROD_LOCALVARIABLEDECLARATION,
			RuleConstants.PROD_IFTHENSTATEMENT_IF_LPAREN_RPAREN,
			RuleConstants.PROD_IFTHENELSESTATEMENT_IF_LPAREN_RPAREN_ELSE,
			RuleConstants.PROD_IFTHENELSESTATEMENTNOSHORTIF_IF_LPAREN_RPAREN_ELSE,
			RuleConstants.PROD_SWITCHSTATEMENT_SWITCH_LPAREN_RPAREN,
			RuleConstants.PROD_BASICFORSTATEMENT_FOR_LPAREN_SEMI_SEMI_RPAREN,
			RuleConstants.PROD_BASICFORSTATEMENTNOSHORTIF_FOR_LPAREN_SEMI_SEMI_RPAREN,
			RuleConstants.PROD_ENHANCEDFORSTATEMENT_FOR_LPAREN_COLON_RPAREN,
			RuleConstants.PROD_ENHANCEDFORSTATEMENTNOSHORTIF_FOR_LPAREN_COLON_RPAREN,
			RuleConstants.PROD_ENHANCEDFORSTATEMENT_FOR_LPAREN_FINAL_COLON_RPAREN,
			RuleConstants.PROD_ENHANCEDFORSTATEMENTNOSHORTIF_FOR_LPAREN_FINAL_COLON_RPAREN,
			RuleConstants.PROD_SWITCHLABEL_DEFAULT_COLON,
			RuleConstants.PROD_WHILESTATEMENT_WHILE_LPAREN_RPAREN,
			RuleConstants.PROD_WHILESTATEMENTNOSHORTIF_WHILE_LPAREN_RPAREN,
			RuleConstants.PROD_DOSTATEMENT_DO_WHILE_LPAREN_RPAREN_SEMI,
			RuleConstants.PROD_BREAKSTATEMENT_BREAK_IDENTIFIER_SEMI,
			RuleConstants.PROD_BREAKSTATEMENT_BREAK_SEMI,
			RuleConstants.PROD_CONTINUESTATEMENT_CONTINUE_IDENTIFIER_SEMI,
			RuleConstants.PROD_CONTINUESTATEMENT_CONTINUE_SEMI,
			RuleConstants.PROD_RETURNSTATEMENT_RETURN_SEMI,
			RuleConstants.PROD_RETURNSTATEMENT_RETURN_SEMI2,
			RuleConstants.PROD_THROWSTATEMENT_THROW_SEMI,
			RuleConstants.PROD_SYNCHRONIZEDSTATEMENT_SYNCHRONIZED_LPAREN_RPAREN,
			RuleConstants.PROD_LABELEDSTATEMENT_IDENTIFIER_COLON,
			RuleConstants.PROD_LABELEDSTATEMENTNOSHORTIF_IDENTIFIER_COLON,
			RuleConstants.PROD_EXPRESSIONSTATEMENT_SEMI,
			RuleConstants.PROD_LABELEDSTATEMENT_IDENTIFIER_COLON,
			RuleConstants.PROD_LABELEDSTATEMENTNOSHORTIF_IDENTIFIER_COLON,
			RuleConstants.PROD_EXPRESSIONSTATEMENT_SEMI,
	};
	
	//---------------------- Grammar specification ---------------------------

	@Override
	protected final String getCompiledGrammar()
	{
		return "JavaSE8.egt";
	}
	
	@Override
	protected final String getGrammarTableName()
	{
		return "Java SE 8";
	}

	/**
	 * If this flag is set then program names derived from file name will be made uppercase
	 * This default will be initialized in consistency with the Analyser check 
	 */
	private boolean optionUpperCaseProgName = false;

	//------------------------------ Constructor -----------------------------

	/**
	 * Constructs a parser for language Java SE 8, loads the grammar as resource and
	 * specifies whether to generate the parse tree as string
	 */
	public JavaParser() {
	}

	//---------------------- File Filter configuration ---------------------------
	
	@Override
	public String getDialogTitle() {
		return "Java SE 8";
	}

	@Override
	protected String getFileDescription() {
		return "Java SE 8 Source Files";
	}

 	@Override
	public String[] getFileExtensions() {
		// TODO specify the usual file name extensions for Java SE 8 source files here!";
		final String[] exts = { "java" };
		return exts;
	}

	@Override
	protected String[][] getCommentDelimiters() {
		return new String[][] {
			{"/*", "*/"},
			{"//"}
		};
	}

	//---------------------- Grammar table constants DON'T MODIFY! ---------------------------

	// Symbolic constants naming the table indices of the symbols of the grammar 
	@SuppressWarnings("unused")
	private interface SymbolConstants 
	{
		final int SYM_EOF                                  =   0;  // (EOF)
		final int SYM_ERROR                                =   1;  // (Error)
		final int SYM_COMMENT                              =   2;  // Comment
		final int SYM_NEWLINE                              =   3;  // NewLine
		final int SYM_WHITESPACE                           =   4;  // Whitespace
		final int SYM_TIMESDIV                             =   5;  // '*/'
		final int SYM_DIVTIMES                             =   6;  // '/*'
		final int SYM_DIVDIV                               =   7;  // '//'
		final int SYM_MINUS                                =   8;  // '-'
		final int SYM_MINUSMINUS                           =   9;  // '--'
		final int SYM_EXCLAM                               =  10;  // '!'
		final int SYM_EXCLAMEQ                             =  11;  // '!='
		final int SYM_PERCENT                              =  12;  // '%'
		final int SYM_PERCENTEQ                            =  13;  // '%='
		final int SYM_AMP                                  =  14;  // '&'
		final int SYM_AMPAMP                               =  15;  // '&&'
		final int SYM_AMPEQ                                =  16;  // '&='
		final int SYM_LPAREN                               =  17;  // '('
		final int SYM_RPAREN                               =  18;  // ')'
		final int SYM_TIMES                                =  19;  // '*'
		final int SYM_TIMESEQ                              =  20;  // '*='
		final int SYM_COMMA                                =  21;  // ','
		final int SYM_DOT                                  =  22;  // '.'
		final int SYM_DIV                                  =  23;  // '/'
		final int SYM_DIVEQ                                =  24;  // '/='
		final int SYM_COLON                                =  25;  // ':'
		final int SYM_SEMI                                 =  26;  // ';'
		final int SYM_QUESTION                             =  27;  // '?'
		final int SYM_AT                                   =  28;  // '@'
		final int SYM_LBRACKET                             =  29;  // '['
		final int SYM_RBRACKET                             =  30;  // ']'
		final int SYM_CARET                                =  31;  // '^'
		final int SYM_CARETEQ                              =  32;  // '^='
		final int SYM_LBRACE                               =  33;  // '{'
		final int SYM_PIPE                                 =  34;  // '|'
		final int SYM_PIPEPIPE                             =  35;  // '||'
		final int SYM_PIPEEQ                               =  36;  // '|='
		final int SYM_RBRACE                               =  37;  // '}'
		final int SYM_TILDE                                =  38;  // '~'
		final int SYM_PLUS                                 =  39;  // '+'
		final int SYM_PLUSPLUS                             =  40;  // '++'
		final int SYM_PLUSEQ                               =  41;  // '+='
		final int SYM_LT                                   =  42;  // '<'
		final int SYM_LTLT                                 =  43;  // '<<'
		final int SYM_LTLTEQ                               =  44;  // '<<='
		final int SYM_LTEQ                                 =  45;  // '<='
		final int SYM_EQ                                   =  46;  // '='
		final int SYM_MINUSEQ                              =  47;  // '-='
		final int SYM_EQEQ                                 =  48;  // '=='
		final int SYM_GT                                   =  49;  // '>'
		final int SYM_GTEQ                                 =  50;  // '>='
		final int SYM_GTGT                                 =  51;  // '>>'
		final int SYM_GTGTEQ                               =  52;  // '>>='
		final int SYM_GTGTGT                               =  53;  // '>>>'
		final int SYM_GTGTGTEQ                             =  54;  // '>>>='
		final int SYM_ABSTRACT                             =  55;  // abstract
		final int SYM_BOOLEAN                              =  56;  // boolean
		final int SYM_BOOLEANLITERAL                       =  57;  // BooleanLiteral
		final int SYM_BREAK                                =  58;  // break
		final int SYM_BYTE                                 =  59;  // byte
		final int SYM_CASE                                 =  60;  // case
		final int SYM_CATCH                                =  61;  // catch
		final int SYM_CHAR                                 =  62;  // char
		final int SYM_CLASS                                =  63;  // class
		final int SYM_CONTINUE                             =  64;  // continue
		final int SYM_DEFAULT                              =  65;  // default
		final int SYM_DO                                   =  66;  // do
		final int SYM_DOUBLE                               =  67;  // double
		final int SYM_ELLIPSIS                             =  68;  // Ellipsis
		final int SYM_ELSE                                 =  69;  // else
		final int SYM_ENUM                                 =  70;  // enum
		final int SYM_EXTENDS                              =  71;  // extends
		final int SYM_FINAL                                =  72;  // final
		final int SYM_FINALLY                              =  73;  // finally
		final int SYM_FLOAT                                =  74;  // float
		final int SYM_FLOATINGPOINTLITERAL                 =  75;  // FloatingPointLiteral
		final int SYM_FLOATINGPOINTLITERALEXPONENT         =  76;  // FloatingPointLiteralExponent
		final int SYM_FOR                                  =  77;  // for
		final int SYM_HEXESCAPECHARLITERAL                 =  78;  // HexEscapeCharLiteral
		final int SYM_HEXINTEGERLITERAL                    =  79;  // HexIntegerLiteral
		final int SYM_IDENTIFIER                           =  80;  // Identifier
		final int SYM_IF                                   =  81;  // if
		final int SYM_IMPLEMENTS                           =  82;  // implements
		final int SYM_IMPORT                               =  83;  // import
		final int SYM_INDIRECTCHARLITERAL                  =  84;  // IndirectCharLiteral
		final int SYM_INSTANCEOF                           =  85;  // instanceof
		final int SYM_INT                                  =  86;  // int
		final int SYM_INTERFACE                            =  87;  // interface
		final int SYM_LONG                                 =  88;  // long
		final int SYM_NATIVE                               =  89;  // native
		final int SYM_NEW                                  =  90;  // new
		final int SYM_NULLLITERAL                          =  91;  // NullLiteral
		final int SYM_OCTALESCAPECHARLITERAL               =  92;  // OctalEscapeCharLiteral
		final int SYM_OCTALINTEGERLITERAL                  =  93;  // OctalIntegerLiteral
		final int SYM_PACKAGE                              =  94;  // package
		final int SYM_PRIVATE                              =  95;  // private
		final int SYM_PROTECTED                            =  96;  // protected
		final int SYM_PUBLIC                               =  97;  // public
		final int SYM_RETURN                               =  98;  // return
		final int SYM_SHORT                                =  99;  // short
		final int SYM_STANDARDESCAPECHARLITERAL            = 100;  // StandardEscapeCharLiteral
		final int SYM_STARTWITHNOZERODECIMALINTEGERLITERAL = 101;  // StartWithNoZeroDecimalIntegerLiteral
		final int SYM_STARTWITHZERODECIMALINTEGERLITERAL   = 102;  // StartWithZeroDecimalIntegerLiteral
		final int SYM_STATIC                               = 103;  // static
		final int SYM_STRICTFP                             = 104;  // strictfp
		final int SYM_STRINGLITERAL                        = 105;  // StringLiteral
		final int SYM_SUPER                                = 106;  // super
		final int SYM_SWITCH                               = 107;  // switch
		final int SYM_SYNCHRONIZED                         = 108;  // synchronized
		final int SYM_THIS                                 = 109;  // this
		final int SYM_THROW                                = 110;  // throw
		final int SYM_THROWS                               = 111;  // throws
		final int SYM_TRANSIENT                            = 112;  // transient
		final int SYM_TRY                                  = 113;  // try
		final int SYM_VOID                                 = 114;  // void
		final int SYM_VOLATILE                             = 115;  // volatile
		final int SYM_WHILE                                = 116;  // while
		final int SYM_ADDITIONALBOUNDOPT                   = 117;  // <AdditionalBoundOpt>
		final int SYM_ADDITIVEEXPRESSION                   = 118;  // <AdditiveExpression>
		final int SYM_ANDEXPRESSION                        = 119;  // <AndExpression>
		final int SYM_ANNOTATION                           = 120;  // <Annotation>
		final int SYM_ANNOTATIONS                          = 121;  // <Annotations>
		final int SYM_ARGUMENTLIST                         = 122;  // <ArgumentList>
		final int SYM_ARRAYACCESS                          = 123;  // <ArrayAccess>
		final int SYM_ARRAYCREATIONEXPRESSION              = 124;  // <ArrayCreationExpression>
		final int SYM_ARRAYINITIALIZER                     = 125;  // <ArrayInitializer>
		final int SYM_ARRAYTYPE                            = 126;  // <ArrayType>
		final int SYM_ASSIGNMENT                           = 127;  // <Assignment>
		final int SYM_ASSIGNMENTEXPRESSION                 = 128;  // <AssignmentExpression>
		final int SYM_ASSIGNMENTOPERATOR                   = 129;  // <AssignmentOperator>
		final int SYM_BASICFORSTATEMENT                    = 130;  // <BasicForStatement>
		final int SYM_BASICFORSTATEMENTNOSHORTIF           = 131;  // <BasicForStatementNoShortIf>
		final int SYM_BLOCK                                = 132;  // <Block>
		final int SYM_BLOCKSTATEMENT                       = 133;  // <BlockStatement>
		final int SYM_BLOCKSTATEMENTS                      = 134;  // <BlockStatements>
		final int SYM_BREAKSTATEMENT                       = 135;  // <BreakStatement>
		final int SYM_CASTEXPRESSION                       = 136;  // <CastExpression>
		final int SYM_CATCHCLAUSE                          = 137;  // <CatchClause>
		final int SYM_CATCHES                              = 138;  // <Catches>
		final int SYM_CHARACTERLITERAL                     = 139;  // <CharacterLiteral>
		final int SYM_CLASSBODY                            = 140;  // <ClassBody>
		final int SYM_CLASSBODYDECLARATION                 = 141;  // <ClassBodyDeclaration>
		final int SYM_CLASSBODYDECLARATIONS                = 142;  // <ClassBodyDeclarations>
		final int SYM_CLASSBODYOPT                         = 143;  // <ClassBodyOpt>
		final int SYM_CLASSDECLARATION                     = 144;  // <ClassDeclaration>
		final int SYM_CLASSINSTANCECREATIONEXPRESSION      = 145;  // <ClassInstanceCreationExpression>
		final int SYM_CLASSMEMBERDECLARATION               = 146;  // <ClassMemberDeclaration>
		final int SYM_CLASSORINTERFACETYPE                 = 147;  // <ClassOrInterfaceType>
		final int SYM_CLASSTYPE                            = 148;  // <ClassType>
		final int SYM_CLASSTYPELIST                        = 149;  // <ClassTypeList>
		final int SYM_COMPILATIONUNIT                      = 150;  // <CompilationUnit>
		final int SYM_CONDITIONALANDEXPRESSION             = 151;  // <ConditionalAndExpression>
		final int SYM_CONDITIONALEXPRESSION                = 152;  // <ConditionalExpression>
		final int SYM_CONDITIONALOREXPRESSION              = 153;  // <ConditionalOrExpression>
		final int SYM_CONSTANTDECLARATION                  = 154;  // <ConstantDeclaration>
		final int SYM_CONSTANTEXPRESSION                   = 155;  // <ConstantExpression>
		final int SYM_CONSTRUCTORBODY                      = 156;  // <ConstructorBody>
		final int SYM_CONSTRUCTORDECLARATION               = 157;  // <ConstructorDeclaration>
		final int SYM_CONSTRUCTORDECLARATOR                = 158;  // <ConstructorDeclarator>
		final int SYM_CONTINUESTATEMENT                    = 159;  // <ContinueStatement>
		final int SYM_DECIMALINTEGERLITERAL                = 160;  // <DecimalIntegerLiteral>
		final int SYM_DIMEXPR                              = 161;  // <DimExpr>
		final int SYM_DIMEXPRS                             = 162;  // <DimExprs>
		final int SYM_DIMS                                 = 163;  // <Dims>
		final int SYM_DOSTATEMENT                          = 164;  // <DoStatement>
		final int SYM_ELEMENTVALUE                         = 165;  // <ElementValue>
		final int SYM_ELEMENTVALUEARRAYINITIALIZER         = 166;  // <ElementValueArrayInitializer>
		final int SYM_ELEMENTVALUEPAIR                     = 167;  // <ElementValuePair>
		final int SYM_ELEMENTVALUEPAIRS                    = 168;  // <ElementValuePairs>
		final int SYM_ELEMENTVALUES                        = 169;  // <ElementValues>
		final int SYM_EMPTYSTATEMENT                       = 170;  // <EmptyStatement>
		final int SYM_ENHANCEDFORSTATEMENT                 = 171;  // <EnhancedForStatement>
		final int SYM_ENHANCEDFORSTATEMENTNOSHORTIF        = 172;  // <EnhancedForStatementNoShortIf>
		final int SYM_ENUMBODY                             = 173;  // <EnumBody>
		final int SYM_ENUMBODYDECLARATIONSOPT              = 174;  // <EnumBodyDeclarationsOpt>
		final int SYM_ENUMCONSTANT                         = 175;  // <EnumConstant>
		final int SYM_ENUMCONSTANTS                        = 176;  // <EnumConstants>
		final int SYM_ENUMDECLARATION                      = 177;  // <EnumDeclaration>
		final int SYM_EQUALITYEXPRESSION                   = 178;  // <EqualityExpression>
		final int SYM_EXCLUSIVEOREXPRESSION                = 179;  // <ExclusiveOrExpression>
		final int SYM_EXPLICITCONSTRUCTORINVOCATION        = 180;  // <ExplicitConstructorInvocation>
		final int SYM_EXPRESSION                           = 181;  // <Expression>
		final int SYM_EXPRESSIONOPT                        = 182;  // <ExpressionOpt>
		final int SYM_EXPRESSIONSTATEMENT                  = 183;  // <ExpressionStatement>
		final int SYM_EXTENDSINTERFACES                    = 184;  // <ExtendsInterfaces>
		final int SYM_FIELDACCESS                          = 185;  // <FieldAccess>
		final int SYM_FIELDDECLARATION                     = 186;  // <FieldDeclaration>
		final int SYM_FINALLY2                             = 187;  // <Finally>
		final int SYM_FLOATINGPOINTTYPE                    = 188;  // <FloatingPointType>
		final int SYM_FLOATPOINTLITERAL                    = 189;  // <FloatPointLiteral>
		final int SYM_FORINITOPT                           = 190;  // <ForInitOpt>
		final int SYM_FORMALPARAMETER                      = 191;  // <FormalParameter>
		final int SYM_FORMALPARAMETERLIST                  = 192;  // <FormalParameterList>
		final int SYM_FORSTATEMENT                         = 193;  // <ForStatement>
		final int SYM_FORSTATEMENTNOSHORTIF                = 194;  // <ForStatementNoShortIf>
		final int SYM_FORUPDATEOPT                         = 195;  // <ForUpdateOpt>
		final int SYM_IFTHENELSESTATEMENT                  = 196;  // <IfThenElseStatement>
		final int SYM_IFTHENELSESTATEMENTNOSHORTIF         = 197;  // <IfThenElseStatementNoShortIf>
		final int SYM_IFTHENSTATEMENT                      = 198;  // <IfThenStatement>
		final int SYM_IMPORTDECLARATION                    = 199;  // <ImportDeclaration>
		final int SYM_IMPORTDECLARATIONS                   = 200;  // <ImportDeclarations>
		final int SYM_INCLUSIVEOREXPRESSION                = 201;  // <InclusiveOrExpression>
		final int SYM_INSTANCEINITIALIZER                  = 202;  // <InstanceInitializer>
		final int SYM_INTEGERLITERAL                       = 203;  // <IntegerLiteral>
		final int SYM_INTEGRALTYPE                         = 204;  // <IntegralType>
		final int SYM_INTERFACEBODY                        = 205;  // <InterfaceBody>
		final int SYM_INTERFACEDECLARATION                 = 206;  // <InterfaceDeclaration>
		final int SYM_INTERFACEMEMBERDECLARATION           = 207;  // <InterfaceMemberDeclaration>
		final int SYM_INTERFACEMEMBERDECLARATIONS          = 208;  // <InterfaceMemberDeclarations>
		final int SYM_INTERFACES                           = 209;  // <Interfaces>
		final int SYM_INTERFACETYPE                        = 210;  // <InterfaceType>
		final int SYM_INTERFACETYPELIST                    = 211;  // <InterfaceTypeList>
		final int SYM_LABELEDSTATEMENT                     = 212;  // <LabeledStatement>
		final int SYM_LABELEDSTATEMENTNOSHORTIF            = 213;  // <LabeledStatementNoShortIf>
		final int SYM_LASTFORMALPARAMETER                  = 214;  // <LastFormalParameter>
		final int SYM_LEFTHANDSIDE                         = 215;  // <LeftHandSide>
		final int SYM_LITERAL                              = 216;  // <Literal>
		final int SYM_LOCALCLASSDECLARATION                = 217;  // <LocalClassDeclaration>
		final int SYM_LOCALCLASSMODIFIERS                  = 218;  // <LocalClassModifiers>
		final int SYM_LOCALVARIABLEDECLARATION             = 219;  // <LocalVariableDeclaration>
		final int SYM_LOCALVARIABLEDECLARATIONSTATEMENT    = 220;  // <LocalVariableDeclarationStatement>
		final int SYM_MARKERANNOTATION                     = 221;  // <MarkerAnnotation>
		final int SYM_METHODBODY                           = 222;  // <MethodBody>
		final int SYM_METHODDECLARATION                    = 223;  // <MethodDeclaration>
		final int SYM_METHODDECLARATOR                     = 224;  // <MethodDeclarator>
		final int SYM_METHODHEADER                         = 225;  // <MethodHeader>
		final int SYM_METHODINVOCATION                     = 226;  // <MethodInvocation>
		final int SYM_MODIFIER                             = 227;  // <Modifier>
		final int SYM_MODIFIERS                            = 228;  // <Modifiers>
		final int SYM_MULTIPLICATIVEEXPRESSION             = 229;  // <MultiplicativeExpression>
		final int SYM_NAME                                 = 230;  // <Name>
		final int SYM_NORMALANNOTATION                     = 231;  // <NormalAnnotation>
		final int SYM_NORMALCLASSDECLARATION               = 232;  // <NormalClassDeclaration>
		final int SYM_NUMERICTYPE                          = 233;  // <NumericType>
		final int SYM_PACKAGEDECLARATION                   = 234;  // <PackageDeclaration>
		final int SYM_POSTDECREMENTEXPRESSION              = 235;  // <PostDecrementExpression>
		final int SYM_POSTFIXEXPRESSION                    = 236;  // <PostfixExpression>
		final int SYM_POSTINCREMENTEXPRESSION              = 237;  // <PostIncrementExpression>
		final int SYM_PREDECREMENTEXPRESSION               = 238;  // <PreDecrementExpression>
		final int SYM_PREINCREMENTEXPRESSION               = 239;  // <PreIncrementExpression>
		final int SYM_PRIMARY                              = 240;  // <Primary>
		final int SYM_PRIMARYNONEWARRAY                    = 241;  // <PrimaryNoNewArray>
		final int SYM_PRIMITIVETYPE                        = 242;  // <PrimitiveType>
		final int SYM_PURECLASSDECLARATION                 = 243;  // <PureClassDeclaration>
		final int SYM_QUALIFIEDNAME                        = 244;  // <QualifiedName>
		final int SYM_QUALPREFIXOPT                        = 245;  // <QualPrefixOpt>
		final int SYM_RECEIVERPARAMETER                    = 246;  // <ReceiverParameter>
		final int SYM_REFERENCETYPE                        = 247;  // <ReferenceType>
		final int SYM_RELATIONALEXPRESSION                 = 248;  // <RelationalExpression>
		final int SYM_RESOURCE                             = 249;  // <Resource>
		final int SYM_RESOURCES                            = 250;  // <Resources>
		final int SYM_RESOURCESPECIFICATION                = 251;  // <ResourceSpecification>
		final int SYM_RETURNSTATEMENT                      = 252;  // <ReturnStatement>
		final int SYM_SHIFTEXPRESSION                      = 253;  // <ShiftExpression>
		final int SYM_SIMPLENAME                           = 254;  // <SimpleName>
		final int SYM_SINGLEELEMENTANNOTATION              = 255;  // <SingleElementAnnotation>
		final int SYM_SINGLESTATICIMPORTDECLARATION        = 256;  // <SingleStaticImportDeclaration>
		final int SYM_SINGLETYPEIMPORTDECLARATION          = 257;  // <SingleTypeImportDeclaration>
		final int SYM_STATEMENT                            = 258;  // <Statement>
		final int SYM_STATEMENTEXPRESSION                  = 259;  // <StatementExpression>
		final int SYM_STATEMENTEXPRESSIONLIST              = 260;  // <StatementExpressionList>
		final int SYM_STATEMENTNOSHORTIF                   = 261;  // <StatementNoShortIf>
		final int SYM_STATEMENTWITHOUTTRAILINGSUBSTATEMENT = 262;  // <StatementWithoutTrailingSubstatement>
		final int SYM_STATICIMPORTONDEMANDDECLARATION      = 263;  // <StaticImportOnDemandDeclaration>
		final int SYM_STATICINITIALIZER                    = 264;  // <StaticInitializer>
		final int SYM_SUPER2                               = 265;  // <Super>
		final int SYM_SWITCHBLOCK                          = 266;  // <SwitchBlock>
		final int SYM_SWITCHBLOCKSTATEMENTGROUP            = 267;  // <SwitchBlockStatementGroup>
		final int SYM_SWITCHBLOCKSTATEMENTGROUPS           = 268;  // <SwitchBlockStatementGroups>
		final int SYM_SWITCHLABEL                          = 269;  // <SwitchLabel>
		final int SYM_SWITCHLABELS                         = 270;  // <SwitchLabels>
		final int SYM_SWITCHSTATEMENT                      = 271;  // <SwitchStatement>
		final int SYM_SYNCHRONIZEDSTATEMENT                = 272;  // <SynchronizedStatement>
		final int SYM_THROWS2                              = 273;  // <Throws>
		final int SYM_THROWSTATEMENT                       = 274;  // <ThrowStatement>
		final int SYM_TRYSTATEMENT                         = 275;  // <TryStatement>
		final int SYM_TYPE                                 = 276;  // <Type>
		final int SYM_TYPEARGUMENT                         = 277;  // <TypeArgument>
		final int SYM_TYPEARGUMENTS                        = 278;  // <TypeArguments>
		final int SYM_TYPEBOUNDOPT                         = 279;  // <TypeBoundOpt>
		final int SYM_TYPEDECLARATION                      = 280;  // <TypeDeclaration>
		final int SYM_TYPEDECLARATIONS                     = 281;  // <TypeDeclarations>
		final int SYM_TYPEIMPORTONDEMANDDECLARATION        = 282;  // <TypeImportOnDemandDeclaration>
		final int SYM_TYPENAME                             = 283;  // <TypeName>
		final int SYM_TYPEPARAMETER                        = 284;  // <TypeParameter>
		final int SYM_TYPEPARAMETERS                       = 285;  // <TypeParameters>
		final int SYM_TYPEPARAMETERSOPT                    = 286;  // <TypeParametersOpt>
		final int SYM_TYPEVARIABLE                         = 287;  // <TypeVariable>
		final int SYM_UNARYEXPRESSION                      = 288;  // <UnaryExpression>
		final int SYM_UNARYEXPRESSIONNOTPLUSMINUS          = 289;  // <UnaryExpressionNotPlusMinus>
		final int SYM_VARIABLEDECLARATOR                   = 290;  // <VariableDeclarator>
		final int SYM_VARIABLEDECLARATORID                 = 291;  // <VariableDeclaratorId>
		final int SYM_VARIABLEDECLARATORS                  = 292;  // <VariableDeclarators>
		final int SYM_VARIABLEINITIALIZER                  = 293;  // <VariableInitializer>
		final int SYM_VARIABLEINITIALIZERS                 = 294;  // <VariableInitializers>
		final int SYM_WHILESTATEMENT                       = 295;  // <WhileStatement>
		final int SYM_WHILESTATEMENTNOSHORTIF              = 296;  // <WhileStatementNoShortIf>
		final int SYM_WILDCARD                             = 297;  // <Wildcard>
		final int SYM_WILDCARDBOUNDSOPT                    = 298;  // <WildcardBoundsOpt>
	};

	// Symbolic constants naming the table indices of the grammar rules
//	@SuppressWarnings("unused")
	private interface RuleConstants
	{
//		final int PROD_CHARACTERLITERAL_INDIRECTCHARLITERAL                         =   0;  // <CharacterLiteral> ::= IndirectCharLiteral
//		final int PROD_CHARACTERLITERAL_STANDARDESCAPECHARLITERAL                   =   1;  // <CharacterLiteral> ::= StandardEscapeCharLiteral
//		final int PROD_CHARACTERLITERAL_OCTALESCAPECHARLITERAL                      =   2;  // <CharacterLiteral> ::= OctalEscapeCharLiteral
//		final int PROD_CHARACTERLITERAL_HEXESCAPECHARLITERAL                        =   3;  // <CharacterLiteral> ::= HexEscapeCharLiteral
//		final int PROD_DECIMALINTEGERLITERAL_STARTWITHZERODECIMALINTEGERLITERAL     =   4;  // <DecimalIntegerLiteral> ::= StartWithZeroDecimalIntegerLiteral
//		final int PROD_DECIMALINTEGERLITERAL_STARTWITHNOZERODECIMALINTEGERLITERAL   =   5;  // <DecimalIntegerLiteral> ::= StartWithNoZeroDecimalIntegerLiteral
//		final int PROD_FLOATPOINTLITERAL_FLOATINGPOINTLITERAL                       =   6;  // <FloatPointLiteral> ::= FloatingPointLiteral
//		final int PROD_FLOATPOINTLITERAL_FLOATINGPOINTLITERALEXPONENT               =   7;  // <FloatPointLiteral> ::= FloatingPointLiteralExponent
//		final int PROD_INTEGERLITERAL                                               =   8;  // <IntegerLiteral> ::= <DecimalIntegerLiteral>
//		final int PROD_INTEGERLITERAL_HEXINTEGERLITERAL                             =   9;  // <IntegerLiteral> ::= HexIntegerLiteral
//		final int PROD_INTEGERLITERAL_OCTALINTEGERLITERAL                           =  10;  // <IntegerLiteral> ::= OctalIntegerLiteral
//		final int PROD_LITERAL                                                      =  11;  // <Literal> ::= <IntegerLiteral>
//		final int PROD_LITERAL2                                                     =  12;  // <Literal> ::= <FloatPointLiteral>
//		final int PROD_LITERAL_BOOLEANLITERAL                                       =  13;  // <Literal> ::= BooleanLiteral
//		final int PROD_LITERAL3                                                     =  14;  // <Literal> ::= <CharacterLiteral>
//		final int PROD_LITERAL_STRINGLITERAL                                        =  15;  // <Literal> ::= StringLiteral
//		final int PROD_LITERAL_NULLLITERAL                                          =  16;  // <Literal> ::= NullLiteral
//		final int PROD_ANNOTATION                                                   =  17;  // <Annotation> ::= <NormalAnnotation>
//		final int PROD_ANNOTATION2                                                  =  18;  // <Annotation> ::= <MarkerAnnotation>
//		final int PROD_ANNOTATION3                                                  =  19;  // <Annotation> ::= <SingleElementAnnotation>
//		final int PROD_NORMALANNOTATION_AT_LPAREN_RPAREN                            =  20;  // <NormalAnnotation> ::= '@' <TypeName> '(' <ElementValuePairs> ')'
//		final int PROD_NORMALANNOTATION_AT_LPAREN_RPAREN2                           =  21;  // <NormalAnnotation> ::= '@' <TypeName> '(' ')'
//		final int PROD_ELEMENTVALUEPAIRS                                            =  22;  // <ElementValuePairs> ::= <ElementValuePair>
//		final int PROD_ELEMENTVALUEPAIRS_COMMA                                      =  23;  // <ElementValuePairs> ::= <ElementValuePairs> ',' <ElementValuePair>
//		final int PROD_ELEMENTVALUEPAIR_IDENTIFIER_EQ                               =  24;  // <ElementValuePair> ::= Identifier '=' <ElementValue>
//		final int PROD_ELEMENTVALUE                                                 =  25;  // <ElementValue> ::= <ConditionalExpression>
//		final int PROD_ELEMENTVALUE2                                                =  26;  // <ElementValue> ::= <ElementValueArrayInitializer>
//		final int PROD_ELEMENTVALUE3                                                =  27;  // <ElementValue> ::= <Annotation>
//		final int PROD_ELEMENTVALUEARRAYINITIALIZER_LBRACE_RBRACE                   =  28;  // <ElementValueArrayInitializer> ::= '{' <ElementValues> '}'
//		final int PROD_ELEMENTVALUEARRAYINITIALIZER_LBRACE_COMMA_RBRACE             =  29;  // <ElementValueArrayInitializer> ::= '{' <ElementValues> ',' '}'
//		final int PROD_ELEMENTVALUEARRAYINITIALIZER_LBRACE_COMMA_RBRACE2            =  30;  // <ElementValueArrayInitializer> ::= '{' ',' '}'
//		final int PROD_ELEMENTVALUEARRAYINITIALIZER_LBRACE_RBRACE2                  =  31;  // <ElementValueArrayInitializer> ::= '{' '}'
//		final int PROD_ELEMENTVALUES                                                =  32;  // <ElementValues> ::= <ElementValue>
//		final int PROD_ELEMENTVALUES_COMMA                                          =  33;  // <ElementValues> ::= <ElementValues> ',' <ElementValue>
//		final int PROD_MARKERANNOTATION_AT                                          =  34;  // <MarkerAnnotation> ::= '@' <TypeName>
//		final int PROD_SINGLEELEMENTANNOTATION_AT_LPAREN_RPAREN                     =  35;  // <SingleElementAnnotation> ::= '@' <TypeName> '(' <ElementValue> ')'
//		final int PROD_TYPENAME                                                     =  36;  // <TypeName> ::= <Name>
//		final int PROD_ANNOTATIONS                                                  =  37;  // <Annotations> ::= <Annotation> <Annotations>
//		final int PROD_ANNOTATIONS2                                                 =  38;  // <Annotations> ::= 
//		final int PROD_TYPE                                                         =  39;  // <Type> ::= <PrimitiveType>
//		final int PROD_TYPE2                                                        =  40;  // <Type> ::= <ReferenceType>
//		final int PROD_PRIMITIVETYPE                                                =  41;  // <PrimitiveType> ::= <NumericType>
//		final int PROD_PRIMITIVETYPE_BOOLEAN                                        =  42;  // <PrimitiveType> ::= boolean
//		final int PROD_NUMERICTYPE                                                  =  43;  // <NumericType> ::= <IntegralType>
//		final int PROD_NUMERICTYPE2                                                 =  44;  // <NumericType> ::= <FloatingPointType>
//		final int PROD_INTEGRALTYPE_BYTE                                            =  45;  // <IntegralType> ::= byte
//		final int PROD_INTEGRALTYPE_SHORT                                           =  46;  // <IntegralType> ::= short
//		final int PROD_INTEGRALTYPE_INT                                             =  47;  // <IntegralType> ::= int
//		final int PROD_INTEGRALTYPE_LONG                                            =  48;  // <IntegralType> ::= long
//		final int PROD_INTEGRALTYPE_CHAR                                            =  49;  // <IntegralType> ::= char
//		final int PROD_FLOATINGPOINTTYPE_FLOAT                                      =  50;  // <FloatingPointType> ::= float
//		final int PROD_FLOATINGPOINTTYPE_DOUBLE                                     =  51;  // <FloatingPointType> ::= double
//		final int PROD_REFERENCETYPE                                                =  52;  // <ReferenceType> ::= <ClassOrInterfaceType>
//		final int PROD_REFERENCETYPE2                                               =  53;  // <ReferenceType> ::= <ArrayType>
//		final int PROD_CLASSORINTERFACETYPE_LT_GT                                   =  54;  // <ClassOrInterfaceType> ::= <Name> '<' <TypeArguments> '>'
//		final int PROD_CLASSORINTERFACETYPE                                         =  55;  // <ClassOrInterfaceType> ::= <Name>
//		final int PROD_CLASSTYPE                                                    =  56;  // <ClassType> ::= <ClassOrInterfaceType>
//		final int PROD_INTERFACETYPE                                                =  57;  // <InterfaceType> ::= <ClassOrInterfaceType>
//		final int PROD_TYPEVARIABLE_IDENTIFIER                                      =  58;  // <TypeVariable> ::= <Annotations> Identifier
//		final int PROD_ARRAYTYPE                                                    =  59;  // <ArrayType> ::= <PrimitiveType> <Dims>
//		final int PROD_ARRAYTYPE2                                                   =  60;  // <ArrayType> ::= <Name> <Dims>
//		final int PROD_NAME                                                         =  61;  // <Name> ::= <SimpleName>
//		final int PROD_NAME2                                                        =  62;  // <Name> ::= <QualifiedName>
		final int PROD_SIMPLENAME_IDENTIFIER                                        =  63;  // <SimpleName> ::= Identifier
//		final int PROD_QUALIFIEDNAME_DOT_IDENTIFIER                                 =  64;  // <QualifiedName> ::= <Name> '.' Identifier
//		final int PROD_TYPEARGUMENTS                                                =  65;  // <TypeArguments> ::= <TypeArgument>
//		final int PROD_TYPEARGUMENTS_COMMA                                          =  66;  // <TypeArguments> ::= <TypeArguments> ',' <TypeArgument>
//		final int PROD_TYPEARGUMENT                                                 =  67;  // <TypeArgument> ::= <ReferenceType>
//		final int PROD_TYPEARGUMENT2                                                =  68;  // <TypeArgument> ::= <Wildcard>
//		final int PROD_WILDCARD_QUESTION                                            =  69;  // <Wildcard> ::= <Annotations> '?' <WildcardBoundsOpt>
//		final int PROD_WILDCARDBOUNDSOPT_EXTENDS                                    =  70;  // <WildcardBoundsOpt> ::= extends <ReferenceType>
//		final int PROD_WILDCARDBOUNDSOPT_SUPER                                      =  71;  // <WildcardBoundsOpt> ::= super <ReferenceType>
//		final int PROD_WILDCARDBOUNDSOPT                                            =  72;  // <WildcardBoundsOpt> ::= 
//		final int PROD_TYPEPARAMETER_IDENTIFIER                                     =  73;  // <TypeParameter> ::= <Annotations> Identifier <TypeBoundOpt>
//		final int PROD_TYPEBOUNDOPT_EXTENDS                                         =  74;  // <TypeBoundOpt> ::= extends <TypeVariable>
//		final int PROD_TYPEBOUNDOPT_EXTENDS2                                        =  75;  // <TypeBoundOpt> ::= extends <ClassOrInterfaceType> <AdditionalBoundOpt>
//		final int PROD_ADDITIONALBOUNDOPT_AMP                                       =  76;  // <AdditionalBoundOpt> ::= '&' <InterfaceType>
//		final int PROD_ADDITIONALBOUNDOPT                                           =  77;  // <AdditionalBoundOpt> ::= 
//		final int PROD_COMPILATIONUNIT                                              =  78;  // <CompilationUnit> ::= <PackageDeclaration> <ImportDeclarations> <TypeDeclarations>
//		final int PROD_COMPILATIONUNIT2                                             =  79;  // <CompilationUnit> ::= <PackageDeclaration> <ImportDeclarations>
//		final int PROD_COMPILATIONUNIT3                                             =  80;  // <CompilationUnit> ::= <PackageDeclaration> <TypeDeclarations>
//		final int PROD_COMPILATIONUNIT4                                             =  81;  // <CompilationUnit> ::= <PackageDeclaration>
//		final int PROD_COMPILATIONUNIT5                                             =  82;  // <CompilationUnit> ::= <ImportDeclarations> <TypeDeclarations>
//		final int PROD_COMPILATIONUNIT6                                             =  83;  // <CompilationUnit> ::= <ImportDeclarations>
//		final int PROD_COMPILATIONUNIT7                                             =  84;  // <CompilationUnit> ::= <TypeDeclarations>
//		final int PROD_COMPILATIONUNIT8                                             =  85;  // <CompilationUnit> ::= 
//		final int PROD_IMPORTDECLARATIONS                                           =  86;  // <ImportDeclarations> ::= <ImportDeclaration>
//		final int PROD_IMPORTDECLARATIONS2                                          =  87;  // <ImportDeclarations> ::= <ImportDeclarations> <ImportDeclaration>
//		final int PROD_TYPEDECLARATIONS                                             =  88;  // <TypeDeclarations> ::= <TypeDeclaration>
//		final int PROD_TYPEDECLARATIONS2                                            =  89;  // <TypeDeclarations> ::= <TypeDeclarations> <TypeDeclaration>
		final int PROD_PACKAGEDECLARATION_PACKAGE_SEMI                              =  90;  // <PackageDeclaration> ::= package <Name> ';'
//		final int PROD_IMPORTDECLARATION                                            =  91;  // <ImportDeclaration> ::= <SingleTypeImportDeclaration>
//		final int PROD_IMPORTDECLARATION2                                           =  92;  // <ImportDeclaration> ::= <TypeImportOnDemandDeclaration>
//		final int PROD_IMPORTDECLARATION3                                           =  93;  // <ImportDeclaration> ::= <SingleStaticImportDeclaration>
//		final int PROD_IMPORTDECLARATION4                                           =  94;  // <ImportDeclaration> ::= <StaticImportOnDemandDeclaration>
		final int PROD_SINGLETYPEIMPORTDECLARATION_IMPORT_SEMI                      =  95;  // <SingleTypeImportDeclaration> ::= import <Name> ';'
		final int PROD_TYPEIMPORTONDEMANDDECLARATION_IMPORT_DOT_TIMES_SEMI          =  96;  // <TypeImportOnDemandDeclaration> ::= import <Name> '.' '*' ';'
		final int PROD_SINGLESTATICIMPORTDECLARATION_IMPORT_STATIC_SEMI             =  97;  // <SingleStaticImportDeclaration> ::= import static <Name> ';'
		final int PROD_STATICIMPORTONDEMANDDECLARATION_IMPORT_STATIC_DOT_TIMES_SEMI =  98;  // <StaticImportOnDemandDeclaration> ::= import static <Name> '.' '*' ';'
//		final int PROD_TYPEDECLARATION                                              =  99;  // <TypeDeclaration> ::= <ClassDeclaration>
//		final int PROD_TYPEDECLARATION2                                             = 100;  // <TypeDeclaration> ::= <InterfaceDeclaration>
//		final int PROD_TYPEDECLARATION_SEMI                                         = 101;  // <TypeDeclaration> ::= ';'
//		final int PROD_MODIFIERS                                                    = 102;  // <Modifiers> ::= <Modifier>
//		final int PROD_MODIFIERS2                                                   = 103;  // <Modifiers> ::= <Modifiers> <Modifier>
//		final int PROD_MODIFIER_PUBLIC                                              = 104;  // <Modifier> ::= public
//		final int PROD_MODIFIER_PROTECTED                                           = 105;  // <Modifier> ::= protected
//		final int PROD_MODIFIER_PRIVATE                                             = 106;  // <Modifier> ::= private
//		final int PROD_MODIFIER_STATIC                                              = 107;  // <Modifier> ::= static
//		final int PROD_MODIFIER_ABSTRACT                                            = 108;  // <Modifier> ::= abstract
//		final int PROD_MODIFIER_FINAL                                               = 109;  // <Modifier> ::= final
//		final int PROD_MODIFIER_NATIVE                                              = 110;  // <Modifier> ::= native
//		final int PROD_MODIFIER_SYNCHRONIZED                                        = 111;  // <Modifier> ::= synchronized
//		final int PROD_MODIFIER_TRANSIENT                                           = 112;  // <Modifier> ::= transient
//		final int PROD_MODIFIER_VOLATILE                                            = 113;  // <Modifier> ::= volatile
//		final int PROD_MODIFIER_DEFAULT                                             = 114;  // <Modifier> ::= default
//		final int PROD_MODIFIER_STRICTFP                                            = 115;  // <Modifier> ::= strictfp
//		final int PROD_CLASSDECLARATION                                             = 116;  // <ClassDeclaration> ::= <Annotations> <NormalClassDeclaration>
//		final int PROD_CLASSDECLARATION2                                            = 117;  // <ClassDeclaration> ::= <Annotations> <EnumDeclaration>
		final int PROD_NORMALCLASSDECLARATION                                       = 118;  // <NormalClassDeclaration> ::= <Modifiers> <PureClassDeclaration>
//		final int PROD_NORMALCLASSDECLARATION2                                      = 119;  // <NormalClassDeclaration> ::= <PureClassDeclaration>
		final int PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER                        = 120;  // <PureClassDeclaration> ::= class Identifier <TypeParametersOpt> <Super> <Interfaces> <ClassBody>
		final int PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER2                       = 121;  // <PureClassDeclaration> ::= class Identifier <TypeParametersOpt> <Super> <ClassBody>
		final int PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER3                       = 122;  // <PureClassDeclaration> ::= class Identifier <TypeParametersOpt> <Interfaces> <ClassBody>
		final int PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER4                       = 123;  // <PureClassDeclaration> ::= class Identifier <TypeParametersOpt> <ClassBody>
//		final int PROD_TYPEPARAMETERSOPT_LT_GT                                      = 124;  // <TypeParametersOpt> ::= '<' <TypeParameters> '>'
//		final int PROD_TYPEPARAMETERSOPT                                            = 125;  // <TypeParametersOpt> ::= 
//		final int PROD_TYPEPARAMETERS                                               = 126;  // <TypeParameters> ::= <TypeParameter>
//		final int PROD_TYPEPARAMETERS_COMMA                                         = 127;  // <TypeParameters> ::= <TypeParameters> ',' <TypeParameter>
//		final int PROD_SUPER_EXTENDS                                                = 128;  // <Super> ::= extends <ClassType>
//		final int PROD_INTERFACES_IMPLEMENTS                                        = 129;  // <Interfaces> ::= implements <InterfaceTypeList>
//		final int PROD_INTERFACETYPELIST                                            = 130;  // <InterfaceTypeList> ::= <InterfaceType>
//		final int PROD_INTERFACETYPELIST_COMMA                                      = 131;  // <InterfaceTypeList> ::= <InterfaceTypeList> ',' <InterfaceType>
		final int PROD_ENUMDECLARATION_ENUM_IDENTIFIER                              = 132;  // <EnumDeclaration> ::= <Modifiers> enum Identifier <Interfaces> <EnumBody>
		final int PROD_ENUMDECLARATION_ENUM_IDENTIFIER2                             = 133;  // <EnumDeclaration> ::= <Modifiers> enum Identifier <EnumBody>
//		final int PROD_ENUMBODY_LBRACE_COMMA_RBRACE                                 = 134;  // <EnumBody> ::= '{' <EnumConstants> ',' <EnumBodyDeclarationsOpt> '}'
//		final int PROD_ENUMBODY_LBRACE_RBRACE                                       = 135;  // <EnumBody> ::= '{' <EnumConstants> <EnumBodyDeclarationsOpt> '}'
//		final int PROD_ENUMBODYDECLARATIONSOPT_SEMI                                 = 136;  // <EnumBodyDeclarationsOpt> ::= ';' <ClassBodyDeclarations>
//		final int PROD_ENUMBODYDECLARATIONSOPT                                      = 137;  // <EnumBodyDeclarationsOpt> ::= 
//		final int PROD_ENUMCONSTANTS                                                = 138;  // <EnumConstants> ::= <EnumConstant>
		final int PROD_ENUMCONSTANTS_COMMA                                          = 139;  // <EnumConstants> ::= <EnumConstants> ',' <EnumConstant>
//		final int PROD_ENUMCONSTANT_IDENTIFIER_LPAREN_RPAREN                        = 140;  // <EnumConstant> ::= <Annotations> Identifier '(' <ArgumentList> ')' <ClassBodyOpt>
		final int PROD_ENUMCONSTANT_IDENTIFIER                                      = 141;  // <EnumConstant> ::= <Annotations> Identifier <ClassBodyOpt>
//		final int PROD_CLASSBODYOPT                                                 = 142;  // <ClassBodyOpt> ::= <ClassBody>
//		final int PROD_CLASSBODYOPT2                                                = 143;  // <ClassBodyOpt> ::= 
		final int PROD_CLASSBODY_LBRACE_RBRACE                                      = 144;  // <ClassBody> ::= '{' <ClassBodyDeclarations> '}'
//		final int PROD_CLASSBODY_LBRACE_RBRACE2                                     = 145;  // <ClassBody> ::= '{' '}'
//		final int PROD_CLASSBODYDECLARATIONS                                        = 146;  // <ClassBodyDeclarations> ::= <ClassBodyDeclaration>
//		final int PROD_CLASSBODYDECLARATIONS2                                       = 147;  // <ClassBodyDeclarations> ::= <ClassBodyDeclarations> <ClassBodyDeclaration>
//		final int PROD_CLASSBODYDECLARATION                                         = 148;  // <ClassBodyDeclaration> ::= <ClassMemberDeclaration>
//		final int PROD_CLASSBODYDECLARATION2                                        = 149;  // <ClassBodyDeclaration> ::= <InstanceInitializer>
//		final int PROD_CLASSBODYDECLARATION3                                        = 150;  // <ClassBodyDeclaration> ::= <StaticInitializer>
//		final int PROD_CLASSBODYDECLARATION4                                        = 151;  // <ClassBodyDeclaration> ::= <ConstructorDeclaration>
//		final int PROD_CLASSMEMBERDECLARATION                                       = 152;  // <ClassMemberDeclaration> ::= <FieldDeclaration>
//		final int PROD_CLASSMEMBERDECLARATION2                                      = 153;  // <ClassMemberDeclaration> ::= <MethodDeclaration>
//		final int PROD_CLASSMEMBERDECLARATION3                                      = 154;  // <ClassMemberDeclaration> ::= <ClassDeclaration>
//		final int PROD_CLASSMEMBERDECLARATION4                                      = 155;  // <ClassMemberDeclaration> ::= <InterfaceDeclaration>
//		final int PROD_CLASSMEMBERDECLARATION_SEMI                                  = 156;  // <ClassMemberDeclaration> ::= ';'
		final int PROD_FIELDDECLARATION_SEMI                                        = 157;  // <FieldDeclaration> ::= <Annotations> <Modifiers> <Type> <VariableDeclarators> ';'
		final int PROD_FIELDDECLARATION_SEMI2                                       = 158;  // <FieldDeclaration> ::= <Annotations> <Type> <VariableDeclarators> ';'
//		final int PROD_VARIABLEDECLARATORS                                          = 159;  // <VariableDeclarators> ::= <VariableDeclarator>
		final int PROD_VARIABLEDECLARATORS_COMMA                                    = 160;  // <VariableDeclarators> ::= <VariableDeclarators> ',' <VariableDeclarator>
//		final int PROD_VARIABLEDECLARATOR                                           = 161;  // <VariableDeclarator> ::= <VariableDeclaratorId>
		final int PROD_VARIABLEDECLARATOR_EQ                                        = 162;  // <VariableDeclarator> ::= <VariableDeclaratorId> '=' <VariableInitializer>
//		final int PROD_VARIABLEDECLARATORID_IDENTIFIER                              = 163;  // <VariableDeclaratorId> ::= Identifier
//		final int PROD_VARIABLEDECLARATORID_IDENTIFIER2                             = 164;  // <VariableDeclaratorId> ::= Identifier <Dims>
//		final int PROD_VARIABLEINITIALIZER                                          = 165;  // <VariableInitializer> ::= <Expression>
//		final int PROD_VARIABLEINITIALIZER2                                         = 166;  // <VariableInitializer> ::= <ArrayInitializer>
		final int PROD_METHODDECLARATION                                            = 167;  // <MethodDeclaration> ::= <Annotations> <MethodHeader> <MethodBody>
		final int PROD_METHODHEADER                                                 = 168;  // <MethodHeader> ::= <Modifiers> <Type> <MethodDeclarator> <Throws>
		final int PROD_METHODHEADER2                                                = 169;  // <MethodHeader> ::= <Modifiers> <Type> <MethodDeclarator>
//		final int PROD_METHODHEADER3                                                = 170;  // <MethodHeader> ::= <Type> <MethodDeclarator> <Throws>
//		final int PROD_METHODHEADER4                                                = 171;  // <MethodHeader> ::= <Type> <MethodDeclarator>
		final int PROD_METHODHEADER_VOID                                            = 172;  // <MethodHeader> ::= <Modifiers> void <MethodDeclarator> <Throws>
		final int PROD_METHODHEADER_VOID2                                           = 173;  // <MethodHeader> ::= <Modifiers> void <MethodDeclarator>
//		final int PROD_METHODHEADER_VOID3                                           = 174;  // <MethodHeader> ::= void <MethodDeclarator> <Throws>
//		final int PROD_METHODHEADER_VOID4                                           = 175;  // <MethodHeader> ::= void <MethodDeclarator>
		final int PROD_METHODDECLARATOR_IDENTIFIER_LPAREN_RPAREN                    = 176;  // <MethodDeclarator> ::= Identifier '(' <FormalParameterList> ')'
//		final int PROD_METHODDECLARATOR_IDENTIFIER_LPAREN_RPAREN2                   = 177;  // <MethodDeclarator> ::= Identifier '(' ')'
		final int PROD_METHODDECLARATOR_IDENTIFIER_LPAREN_RPAREN3                   = 178;  // <MethodDeclarator> ::= Identifier '(' <FormalParameterList> ')' <Dims>
		final int PROD_METHODDECLARATOR_IDENTIFIER_LPAREN_RPAREN4                   = 179;  // <MethodDeclarator> ::= Identifier '(' ')' <Dims>
//		final int PROD_FORMALPARAMETERLIST                                          = 180;  // <FormalParameterList> ::= <FormalParameter>
		final int PROD_FORMALPARAMETERLIST_COMMA                                    = 181;  // <FormalParameterList> ::= <FormalParameterList> ',' <FormalParameter>
//		final int PROD_FORMALPARAMETER                                              = 182;  // <FormalParameter> ::= <Type> <VariableDeclaratorId>
		final int PROD_FORMALPARAMETER_FINAL                                        = 183;  // <FormalParameter> ::= final <Type> <VariableDeclaratorId>
//		final int PROD_FORMALPARAMETER2                                             = 184;  // <FormalParameter> ::= <ReceiverParameter>
//		final int PROD_FORMALPARAMETER3                                             = 185;  // <FormalParameter> ::= <LastFormalParameter>
		final int PROD_LASTFORMALPARAMETER_ELLIPSIS                                 = 186;  // <LastFormalParameter> ::= <Type> Ellipsis <VariableDeclaratorId>
//		final int PROD_RECEIVERPARAMETER_THIS                                       = 187;  // <ReceiverParameter> ::= <Type> <QualPrefixOpt> this
//		final int PROD_QUALPREFIXOPT_IDENTIFIER_DOT                                 = 188;  // <QualPrefixOpt> ::= Identifier '.' <QualPrefixOpt>
//		final int PROD_QUALPREFIXOPT                                                = 189;  // <QualPrefixOpt> ::= 
//		final int PROD_THROWS_THROWS                                                = 190;  // <Throws> ::= throws <ClassTypeList>
//		final int PROD_CLASSTYPELIST                                                = 191;  // <ClassTypeList> ::= <ClassType>
//		final int PROD_CLASSTYPELIST_COMMA                                          = 192;  // <ClassTypeList> ::= <ClassTypeList> ',' <ClassType>
//		final int PROD_METHODBODY                                                   = 193;  // <MethodBody> ::= <Block>
//		final int PROD_METHODBODY_SEMI                                              = 194;  // <MethodBody> ::= ';'
//		final int PROD_INSTANCEINITIALIZER                                          = 195;  // <InstanceInitializer> ::= <Annotations> <Block>
		final int PROD_STATICINITIALIZER_STATIC                                     = 196;  // <StaticInitializer> ::= <Annotations> static <Block>
		final int PROD_CONSTRUCTORDECLARATION                                       = 197;  // <ConstructorDeclaration> ::= <Annotations> <Modifiers> <ConstructorDeclarator> <Throws> <ConstructorBody>
		final int PROD_CONSTRUCTORDECLARATION2                                      = 198;  // <ConstructorDeclaration> ::= <Annotations> <Modifiers> <ConstructorDeclarator> <ConstructorBody>
		final int PROD_CONSTRUCTORDECLARATION3                                      = 199;  // <ConstructorDeclaration> ::= <Annotations> <ConstructorDeclarator> <Throws> <ConstructorBody>
		final int PROD_CONSTRUCTORDECLARATION4                                      = 200;  // <ConstructorDeclaration> ::= <Annotations> <ConstructorDeclarator> <ConstructorBody>
		final int PROD_CONSTRUCTORDECLARATOR_LPAREN_RPAREN                          = 201;  // <ConstructorDeclarator> ::= <SimpleName> '(' <FormalParameterList> ')'
		final int PROD_CONSTRUCTORDECLARATOR_LPAREN_RPAREN2                         = 202;  // <ConstructorDeclarator> ::= <SimpleName> '(' ')'
//		final int PROD_CONSTRUCTORBODY_LBRACE_RBRACE                                = 203;  // <ConstructorBody> ::= '{' <ExplicitConstructorInvocation> <BlockStatements> '}'
//		final int PROD_CONSTRUCTORBODY_LBRACE_RBRACE2                               = 204;  // <ConstructorBody> ::= '{' <ExplicitConstructorInvocation> '}'
//		final int PROD_CONSTRUCTORBODY_LBRACE_RBRACE3                               = 205;  // <ConstructorBody> ::= '{' <BlockStatements> '}'
//		final int PROD_CONSTRUCTORBODY_LBRACE_RBRACE4                               = 206;  // <ConstructorBody> ::= '{' '}'
		final int PROD_EXPLICITCONSTRUCTORINVOCATION_THIS_LPAREN_RPAREN_SEMI        = 207;  // <ExplicitConstructorInvocation> ::= this '(' <ArgumentList> ')' ';'
		final int PROD_EXPLICITCONSTRUCTORINVOCATION_THIS_LPAREN_RPAREN_SEMI2       = 208;  // <ExplicitConstructorInvocation> ::= this '(' ')' ';'
		final int PROD_EXPLICITCONSTRUCTORINVOCATION_SUPER_LPAREN_RPAREN_SEMI       = 209;  // <ExplicitConstructorInvocation> ::= super '(' <ArgumentList> ')' ';'
		final int PROD_EXPLICITCONSTRUCTORINVOCATION_SUPER_LPAREN_RPAREN_SEMI2      = 210;  // <ExplicitConstructorInvocation> ::= super '(' ')' ';'
		final int PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER                    = 211;  // <InterfaceDeclaration> ::= <Annotations> <Modifiers> interface Identifier <TypeParametersOpt> <ExtendsInterfaces> <InterfaceBody>
		final int PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER2                   = 212;  // <InterfaceDeclaration> ::= <Annotations> <Modifiers> interface Identifier <TypeParametersOpt> <InterfaceBody>
		final int PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER3                   = 213;  // <InterfaceDeclaration> ::= <Annotations> interface Identifier <TypeParametersOpt> <ExtendsInterfaces> <InterfaceBody>
		final int PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER4                   = 214;  // <InterfaceDeclaration> ::= <Annotations> interface Identifier <TypeParametersOpt> <InterfaceBody>
//		final int PROD_EXTENDSINTERFACES_EXTENDS                                    = 215;  // <ExtendsInterfaces> ::= extends <InterfaceType>
//		final int PROD_EXTENDSINTERFACES_COMMA                                      = 216;  // <ExtendsInterfaces> ::= <ExtendsInterfaces> ',' <InterfaceType>
//		final int PROD_INTERFACEBODY_LBRACE_RBRACE                                  = 217;  // <InterfaceBody> ::= '{' <InterfaceMemberDeclarations> '}'
//		final int PROD_INTERFACEBODY_LBRACE_RBRACE2                                 = 218;  // <InterfaceBody> ::= '{' '}'
//		final int PROD_INTERFACEMEMBERDECLARATIONS                                  = 219;  // <InterfaceMemberDeclarations> ::= <InterfaceMemberDeclaration>
//		final int PROD_INTERFACEMEMBERDECLARATIONS2                                 = 220;  // <InterfaceMemberDeclarations> ::= <InterfaceMemberDeclarations> <InterfaceMemberDeclaration>
//		final int PROD_INTERFACEMEMBERDECLARATION                                   = 221;  // <InterfaceMemberDeclaration> ::= <ConstantDeclaration>
//		final int PROD_INTERFACEMEMBERDECLARATION2                                  = 222;  // <InterfaceMemberDeclaration> ::= <MethodDeclaration>
//		final int PROD_INTERFACEMEMBERDECLARATION3                                  = 223;  // <InterfaceMemberDeclaration> ::= <ClassDeclaration>
//		final int PROD_INTERFACEMEMBERDECLARATION4                                  = 224;  // <InterfaceMemberDeclaration> ::= <InterfaceDeclaration>
//		final int PROD_CONSTANTDECLARATION                                          = 225;  // <ConstantDeclaration> ::= <FieldDeclaration>
//		final int PROD_ARRAYINITIALIZER_LBRACE_COMMA_RBRACE                         = 226;  // <ArrayInitializer> ::= '{' <VariableInitializers> ',' '}'
		final int PROD_ARRAYINITIALIZER_LBRACE_RBRACE                               = 227;  // <ArrayInitializer> ::= '{' <VariableInitializers> '}'
//		final int PROD_ARRAYINITIALIZER_LBRACE_COMMA_RBRACE2                        = 228;  // <ArrayInitializer> ::= '{' ',' '}'
//		final int PROD_ARRAYINITIALIZER_LBRACE_RBRACE2                              = 229;  // <ArrayInitializer> ::= '{' '}'
//		final int PROD_VARIABLEINITIALIZERS                                         = 230;  // <VariableInitializers> ::= <VariableInitializer>
		final int PROD_VARIABLEINITIALIZERS_COMMA                                   = 231;  // <VariableInitializers> ::= <VariableInitializers> ',' <VariableInitializer>
//		final int PROD_BLOCK_LBRACE_RBRACE                                          = 232;  // <Block> ::= '{' <BlockStatements> '}'
//		final int PROD_BLOCK_LBRACE_RBRACE2                                         = 233;  // <Block> ::= '{' '}'
//		final int PROD_BLOCKSTATEMENTS                                              = 234;  // <BlockStatements> ::= <BlockStatement>
//		final int PROD_BLOCKSTATEMENTS2                                             = 235;  // <BlockStatements> ::= <BlockStatements> <BlockStatement>
//		final int PROD_BLOCKSTATEMENT                                               = 236;  // <BlockStatement> ::= <LocalVariableDeclarationStatement>
//		final int PROD_BLOCKSTATEMENT2                                              = 237;  // <BlockStatement> ::= <LocalClassDeclaration>
//		final int PROD_BLOCKSTATEMENT3                                              = 238;  // <BlockStatement> ::= <Statement>
//		final int PROD_LOCALVARIABLEDECLARATIONSTATEMENT_SEMI                       = 239;  // <LocalVariableDeclarationStatement> ::= <LocalVariableDeclaration> ';'
		final int PROD_LOCALVARIABLEDECLARATION_FINAL                               = 240;  // <LocalVariableDeclaration> ::= final <Type> <VariableDeclarators>
		final int PROD_LOCALVARIABLEDECLARATION                                     = 241;  // <LocalVariableDeclaration> ::= <Type> <VariableDeclarators>
		final int PROD_LOCALCLASSDECLARATION                                        = 242;  // <LocalClassDeclaration> ::= <LocalClassModifiers> <PureClassDeclaration>
//		final int PROD_LOCALCLASSDECLARATION2                                       = 243;  // <LocalClassDeclaration> ::= <PureClassDeclaration>
//		final int PROD_LOCALCLASSMODIFIERS_ABSTRACT                                 = 244;  // <LocalClassModifiers> ::= abstract
//		final int PROD_LOCALCLASSMODIFIERS_FINAL                                    = 245;  // <LocalClassModifiers> ::= final
//		final int PROD_STATEMENT                                                    = 246;  // <Statement> ::= <StatementWithoutTrailingSubstatement>
//		final int PROD_STATEMENT2                                                   = 247;  // <Statement> ::= <LabeledStatement>
//		final int PROD_STATEMENT3                                                   = 248;  // <Statement> ::= <IfThenStatement>
//		final int PROD_STATEMENT4                                                   = 249;  // <Statement> ::= <IfThenElseStatement>
//		final int PROD_STATEMENT5                                                   = 250;  // <Statement> ::= <WhileStatement>
//		final int PROD_STATEMENT6                                                   = 251;  // <Statement> ::= <ForStatement>
//		final int PROD_STATEMENTNOSHORTIF                                           = 252;  // <StatementNoShortIf> ::= <StatementWithoutTrailingSubstatement>
//		final int PROD_STATEMENTNOSHORTIF2                                          = 253;  // <StatementNoShortIf> ::= <LabeledStatementNoShortIf>
//		final int PROD_STATEMENTNOSHORTIF3                                          = 254;  // <StatementNoShortIf> ::= <IfThenElseStatementNoShortIf>
//		final int PROD_STATEMENTNOSHORTIF4                                          = 255;  // <StatementNoShortIf> ::= <WhileStatementNoShortIf>
//		final int PROD_STATEMENTNOSHORTIF5                                          = 256;  // <StatementNoShortIf> ::= <ForStatementNoShortIf>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT                         = 257;  // <StatementWithoutTrailingSubstatement> ::= <Block>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT2                        = 258;  // <StatementWithoutTrailingSubstatement> ::= <EmptyStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT3                        = 259;  // <StatementWithoutTrailingSubstatement> ::= <ExpressionStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT4                        = 260;  // <StatementWithoutTrailingSubstatement> ::= <SwitchStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT5                        = 261;  // <StatementWithoutTrailingSubstatement> ::= <DoStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT6                        = 262;  // <StatementWithoutTrailingSubstatement> ::= <BreakStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT7                        = 263;  // <StatementWithoutTrailingSubstatement> ::= <ContinueStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT8                        = 264;  // <StatementWithoutTrailingSubstatement> ::= <ReturnStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT9                        = 265;  // <StatementWithoutTrailingSubstatement> ::= <SynchronizedStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT10                       = 266;  // <StatementWithoutTrailingSubstatement> ::= <ThrowStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT11                       = 267;  // <StatementWithoutTrailingSubstatement> ::= <TryStatement>
//		final int PROD_EMPTYSTATEMENT_SEMI                                          = 268;  // <EmptyStatement> ::= ';'
		final int PROD_LABELEDSTATEMENT_IDENTIFIER_COLON                            = 269;  // <LabeledStatement> ::= Identifier ':' <Statement>
		final int PROD_LABELEDSTATEMENTNOSHORTIF_IDENTIFIER_COLON                   = 270;  // <LabeledStatementNoShortIf> ::= Identifier ':' <StatementNoShortIf>
		final int PROD_EXPRESSIONSTATEMENT_SEMI                                     = 271;  // <ExpressionStatement> ::= <StatementExpression> ';'
//		final int PROD_STATEMENTEXPRESSION                                          = 272;  // <StatementExpression> ::= <Assignment>
//		final int PROD_STATEMENTEXPRESSION2                                         = 273;  // <StatementExpression> ::= <PreIncrementExpression>
//		final int PROD_STATEMENTEXPRESSION3                                         = 274;  // <StatementExpression> ::= <PreDecrementExpression>
//		final int PROD_STATEMENTEXPRESSION4                                         = 275;  // <StatementExpression> ::= <PostIncrementExpression>
//		final int PROD_STATEMENTEXPRESSION5                                         = 276;  // <StatementExpression> ::= <PostDecrementExpression>
//		final int PROD_STATEMENTEXPRESSION6                                         = 277;  // <StatementExpression> ::= <MethodInvocation>
//		final int PROD_STATEMENTEXPRESSION7                                         = 278;  // <StatementExpression> ::= <ClassInstanceCreationExpression>
		final int PROD_IFTHENSTATEMENT_IF_LPAREN_RPAREN                             = 279;  // <IfThenStatement> ::= if '(' <Expression> ')' <Statement>
		final int PROD_IFTHENELSESTATEMENT_IF_LPAREN_RPAREN_ELSE                    = 280;  // <IfThenElseStatement> ::= if '(' <Expression> ')' <StatementNoShortIf> else <Statement>
		final int PROD_IFTHENELSESTATEMENTNOSHORTIF_IF_LPAREN_RPAREN_ELSE           = 281;  // <IfThenElseStatementNoShortIf> ::= if '(' <Expression> ')' <StatementNoShortIf> else <StatementNoShortIf>
		final int PROD_SWITCHSTATEMENT_SWITCH_LPAREN_RPAREN                         = 282;  // <SwitchStatement> ::= switch '(' <Expression> ')' <SwitchBlock>
		final int PROD_SWITCHBLOCK_LBRACE_RBRACE                                    = 283;  // <SwitchBlock> ::= '{' <SwitchBlockStatementGroups> <SwitchLabels> '}'
//		final int PROD_SWITCHBLOCK_LBRACE_RBRACE2                                   = 284;  // <SwitchBlock> ::= '{' <SwitchBlockStatementGroups> '}'
		final int PROD_SWITCHBLOCK_LBRACE_RBRACE3                                   = 285;  // <SwitchBlock> ::= '{' <SwitchLabels> '}'
		final int PROD_SWITCHBLOCK_LBRACE_RBRACE4                                   = 286;  // <SwitchBlock> ::= '{' '}'
//		final int PROD_SWITCHBLOCKSTATEMENTGROUPS                                   = 287;  // <SwitchBlockStatementGroups> ::= <SwitchBlockStatementGroup>
		final int PROD_SWITCHBLOCKSTATEMENTGROUPS2                                  = 288;  // <SwitchBlockStatementGroups> ::= <SwitchBlockStatementGroups> <SwitchBlockStatementGroup>
//		final int PROD_SWITCHBLOCKSTATEMENTGROUP                                    = 289;  // <SwitchBlockStatementGroup> ::= <SwitchLabels> <BlockStatements>
//		final int PROD_SWITCHLABELS                                                 = 290;  // <SwitchLabels> ::= <SwitchLabel>
		final int PROD_SWITCHLABELS2                                                = 291;  // <SwitchLabels> ::= <SwitchLabels> <SwitchLabel>
//		final int PROD_SWITCHLABEL_CASE_COLON                                       = 292;  // <SwitchLabel> ::= case <ConstantExpression> ':'
		final int PROD_SWITCHLABEL_DEFAULT_COLON                                    = 293;  // <SwitchLabel> ::= default ':'
		final int PROD_WHILESTATEMENT_WHILE_LPAREN_RPAREN                           = 294;  // <WhileStatement> ::= while '(' <Expression> ')' <Statement>
		final int PROD_WHILESTATEMENTNOSHORTIF_WHILE_LPAREN_RPAREN                  = 295;  // <WhileStatementNoShortIf> ::= while '(' <Expression> ')' <StatementNoShortIf>
		final int PROD_DOSTATEMENT_DO_WHILE_LPAREN_RPAREN_SEMI                      = 296;  // <DoStatement> ::= do <Statement> while '(' <Expression> ')' ';'
//		final int PROD_FORSTATEMENT                                                 = 297;  // <ForStatement> ::= <BasicForStatement>
//		final int PROD_FORSTATEMENT2                                                = 298;  // <ForStatement> ::= <EnhancedForStatement>
		final int PROD_BASICFORSTATEMENT_FOR_LPAREN_SEMI_SEMI_RPAREN                = 299;  // <BasicForStatement> ::= for '(' <ForInitOpt> ';' <ExpressionOpt> ';' <ForUpdateOpt> ')' <Statement>
//		final int PROD_EXPRESSIONOPT                                                = 300;  // <ExpressionOpt> ::= <Expression>
//		final int PROD_EXPRESSIONOPT2                                               = 301;  // <ExpressionOpt> ::= 
//		final int PROD_FORSTATEMENTNOSHORTIF                                        = 302;  // <ForStatementNoShortIf> ::= <BasicForStatementNoShortIf>
//		final int PROD_FORSTATEMENTNOSHORTIF2                                       = 303;  // <ForStatementNoShortIf> ::= <EnhancedForStatementNoShortIf>
		final int PROD_BASICFORSTATEMENTNOSHORTIF_FOR_LPAREN_SEMI_SEMI_RPAREN       = 304;  // <BasicForStatementNoShortIf> ::= for '(' <ForInitOpt> ';' <ExpressionOpt> ';' <ForUpdateOpt> ')' <StatementNoShortIf>
//		final int PROD_FORINITOPT                                                   = 305;  // <ForInitOpt> ::= <StatementExpressionList>
//		final int PROD_FORINITOPT2                                                  = 306;  // <ForInitOpt> ::= <LocalVariableDeclaration>
//		final int PROD_FORINITOPT3                                                  = 307;  // <ForInitOpt> ::= 
//		final int PROD_FORUPDATEOPT                                                 = 308;  // <ForUpdateOpt> ::= <StatementExpressionList>
//		final int PROD_FORUPDATEOPT2                                                = 309;  // <ForUpdateOpt> ::= 
		final int PROD_ENHANCEDFORSTATEMENT_FOR_LPAREN_FINAL_COLON_RPAREN           = 310;  // <EnhancedForStatement> ::= for '(' final <Type> <VariableDeclaratorId> ':' <Expression> ')' <Statement>
		final int PROD_ENHANCEDFORSTATEMENT_FOR_LPAREN_COLON_RPAREN                 = 311;  // <EnhancedForStatement> ::= for '(' <Type> <VariableDeclaratorId> ':' <Expression> ')' <Statement>
		final int PROD_ENHANCEDFORSTATEMENTNOSHORTIF_FOR_LPAREN_FINAL_COLON_RPAREN  = 312;  // <EnhancedForStatementNoShortIf> ::= for '(' final <Type> <VariableDeclaratorId> ':' <Expression> ')' <StatementNoShortIf>
		final int PROD_ENHANCEDFORSTATEMENTNOSHORTIF_FOR_LPAREN_COLON_RPAREN        = 313;  // <EnhancedForStatementNoShortIf> ::= for '(' <Type> <VariableDeclaratorId> ':' <Expression> ')' <StatementNoShortIf>
//		final int PROD_STATEMENTEXPRESSIONLIST                                      = 314;  // <StatementExpressionList> ::= <StatementExpression>
//		final int PROD_STATEMENTEXPRESSIONLIST_COMMA                                = 315;  // <StatementExpressionList> ::= <StatementExpressionList> ',' <StatementExpression>
		final int PROD_BREAKSTATEMENT_BREAK_IDENTIFIER_SEMI                         = 316;  // <BreakStatement> ::= break Identifier ';'
		final int PROD_BREAKSTATEMENT_BREAK_SEMI                                    = 317;  // <BreakStatement> ::= break ';'
		final int PROD_CONTINUESTATEMENT_CONTINUE_IDENTIFIER_SEMI                   = 318;  // <ContinueStatement> ::= continue Identifier ';'
		final int PROD_CONTINUESTATEMENT_CONTINUE_SEMI                              = 319;  // <ContinueStatement> ::= continue ';'
		final int PROD_RETURNSTATEMENT_RETURN_SEMI                                  = 320;  // <ReturnStatement> ::= return <Expression> ';'
		final int PROD_RETURNSTATEMENT_RETURN_SEMI2                                 = 321;  // <ReturnStatement> ::= return ';'
		final int PROD_THROWSTATEMENT_THROW_SEMI                                    = 322;  // <ThrowStatement> ::= throw <Expression> ';'
		final int PROD_SYNCHRONIZEDSTATEMENT_SYNCHRONIZED_LPAREN_RPAREN             = 323;  // <SynchronizedStatement> ::= synchronized '(' <Expression> ')' <Block>
		final int PROD_TRYSTATEMENT_TRY                                             = 324;  // <TryStatement> ::= try <Block> <Catches>
		final int PROD_TRYSTATEMENT_TRY2                                            = 325;  // <TryStatement> ::= try <Block> <Catches> <Finally>
		final int PROD_TRYSTATEMENT_TRY3                                            = 326;  // <TryStatement> ::= try <Block> <Finally>
		final int PROD_TRYSTATEMENT_TRY4                                            = 327;  // <TryStatement> ::= try <ResourceSpecification> <Block>
		final int PROD_TRYSTATEMENT_TRY5                                            = 328;  // <TryStatement> ::= try <ResourceSpecification> <Block> <Catches>
		final int PROD_TRYSTATEMENT_TRY6                                            = 329;  // <TryStatement> ::= try <ResourceSpecification> <Block> <Catches> <Finally>
		final int PROD_TRYSTATEMENT_TRY7                                            = 330;  // <TryStatement> ::= try <ResourceSpecification> <Block> <Finally>
//		final int PROD_CATCHES                                                      = 331;  // <Catches> ::= <CatchClause>
		final int PROD_CATCHES2                                                     = 332;  // <Catches> ::= <Catches> <CatchClause>
//		final int PROD_CATCHCLAUSE_CATCH_LPAREN_RPAREN                              = 333;  // <CatchClause> ::= catch '(' <FormalParameter> ')' <Block>
//		final int PROD_FINALLY_FINALLY                                              = 334;  // <Finally> ::= finally <Block>
//		final int PROD_RESOURCESPECIFICATION_LPAREN_RPAREN                          = 335;  // <ResourceSpecification> ::= '(' <Resources> ')'
//		final int PROD_RESOURCESPECIFICATION_LPAREN_SEMI_RPAREN                     = 336;  // <ResourceSpecification> ::= '(' <Resources> ';' ')'
//		final int PROD_RESOURCES                                                    = 337;  // <Resources> ::= <Resource>
		final int PROD_RESOURCES_SEMI                                               = 338;  // <Resources> ::= <Resources> ';' <Resource>
//		final int PROD_RESOURCE_EQ                                                  = 339;  // <Resource> ::= <Type> <VariableDeclaratorId> '=' <Expression>
		final int PROD_RESOURCE_FINAL_EQ                                            = 340;  // <Resource> ::= final <Type> <VariableDeclaratorId> '=' <Expression>
//		final int PROD_PRIMARY                                                      = 341;  // <Primary> ::= <PrimaryNoNewArray>
//		final int PROD_PRIMARY2                                                     = 342;  // <Primary> ::= <ArrayCreationExpression>
//		final int PROD_PRIMARYNONEWARRAY                                            = 343;  // <PrimaryNoNewArray> ::= <Literal>
//		final int PROD_PRIMARYNONEWARRAY_THIS                                       = 344;  // <PrimaryNoNewArray> ::= this
		final int PROD_PRIMARYNONEWARRAY_LPAREN_RPAREN                              = 345;  // <PrimaryNoNewArray> ::= '(' <Expression> ')'
//		final int PROD_PRIMARYNONEWARRAY2                                           = 346;  // <PrimaryNoNewArray> ::= <ClassInstanceCreationExpression>
//		final int PROD_PRIMARYNONEWARRAY3                                           = 347;  // <PrimaryNoNewArray> ::= <FieldAccess>
//		final int PROD_PRIMARYNONEWARRAY4                                           = 348;  // <PrimaryNoNewArray> ::= <MethodInvocation>
//		final int PROD_PRIMARYNONEWARRAY5                                           = 349;  // <PrimaryNoNewArray> ::= <ArrayAccess>
		final int PROD_CLASSINSTANCECREATIONEXPRESSION_NEW_LPAREN_RPAREN            = 350;  // <ClassInstanceCreationExpression> ::= new <ClassType> '(' <ArgumentList> ')'
		final int PROD_CLASSINSTANCECREATIONEXPRESSION_NEW_LPAREN_RPAREN2           = 351;  // <ClassInstanceCreationExpression> ::= new <ClassType> '(' ')'
//		final int PROD_CLASSINSTANCECREATIONEXPRESSION_NEW_LPAREN_RPAREN3           = 352;  // <ClassInstanceCreationExpression> ::= new <ClassType> '(' <ArgumentList> ')' <ClassBody>
//		final int PROD_CLASSINSTANCECREATIONEXPRESSION_NEW_LPAREN_RPAREN4           = 353;  // <ClassInstanceCreationExpression> ::= new <ClassType> '(' ')' <ClassBody>
//		final int PROD_ARGUMENTLIST                                                 = 354;  // <ArgumentList> ::= <Expression>
		final int PROD_ARGUMENTLIST_COMMA                                           = 355;  // <ArgumentList> ::= <ArgumentList> ',' <Expression>
		final int PROD_ARRAYCREATIONEXPRESSION_NEW                                  = 356;  // <ArrayCreationExpression> ::= new <PrimitiveType> <DimExprs> <Dims>
		final int PROD_ARRAYCREATIONEXPRESSION_NEW2                                 = 357;  // <ArrayCreationExpression> ::= new <PrimitiveType> <DimExprs>
		final int PROD_ARRAYCREATIONEXPRESSION_NEW3                                 = 358;  // <ArrayCreationExpression> ::= new <ClassOrInterfaceType> <DimExprs> <Dims>
		final int PROD_ARRAYCREATIONEXPRESSION_NEW4                                 = 359;  // <ArrayCreationExpression> ::= new <ClassOrInterfaceType> <DimExprs>
		final int PROD_ARRAYCREATIONEXPRESSION_NEW5                                 = 360;  // <ArrayCreationExpression> ::= new <PrimitiveType> <Dims> <ArrayInitializer>
		final int PROD_ARRAYCREATIONEXPRESSION_NEW6                                 = 361;  // <ArrayCreationExpression> ::= new <ClassOrInterfaceType> <Dims> <ArrayInitializer>
//		final int PROD_DIMEXPRS                                                     = 362;  // <DimExprs> ::= <DimExpr>
//		final int PROD_DIMEXPRS2                                                    = 363;  // <DimExprs> ::= <DimExprs> <DimExpr>
//		final int PROD_DIMEXPR_LBRACKET_RBRACKET                                    = 364;  // <DimExpr> ::= '[' <Expression> ']'
//		final int PROD_DIMS_LBRACKET_RBRACKET                                       = 365;  // <Dims> ::= '[' ']'
//		final int PROD_DIMS_LBRACKET_RBRACKET2                                      = 366;  // <Dims> ::= <Dims> '[' ']'
		final int PROD_FIELDACCESS_DOT_IDENTIFIER                                   = 367;  // <FieldAccess> ::= <Primary> '.' Identifier
		final int PROD_FIELDACCESS_SUPER_DOT_IDENTIFIER                             = 368;  // <FieldAccess> ::= super '.' Identifier
		final int PROD_METHODINVOCATION_LPAREN_RPAREN                               = 369;  // <MethodInvocation> ::= <Name> '(' <ArgumentList> ')'
		final int PROD_METHODINVOCATION_LPAREN_RPAREN2                              = 370;  // <MethodInvocation> ::= <Name> '(' ')'
		final int PROD_METHODINVOCATION_DOT_IDENTIFIER_LPAREN_RPAREN                = 371;  // <MethodInvocation> ::= <Primary> '.' Identifier '(' <ArgumentList> ')'
		final int PROD_METHODINVOCATION_DOT_IDENTIFIER_LPAREN_RPAREN2               = 372;  // <MethodInvocation> ::= <Primary> '.' Identifier '(' ')'
		final int PROD_METHODINVOCATION_SUPER_DOT_IDENTIFIER_LPAREN_RPAREN          = 373;  // <MethodInvocation> ::= super '.' Identifier '(' <ArgumentList> ')'
		final int PROD_METHODINVOCATION_SUPER_DOT_IDENTIFIER_LPAREN_RPAREN2         = 374;  // <MethodInvocation> ::= super '.' Identifier '(' ')'
		final int PROD_ARRAYACCESS_LBRACKET_RBRACKET                                = 375;  // <ArrayAccess> ::= <Name> '[' <Expression> ']'
		final int PROD_ARRAYACCESS_LBRACKET_RBRACKET2                               = 376;  // <ArrayAccess> ::= <PrimaryNoNewArray> '[' <Expression> ']'
//		final int PROD_POSTFIXEXPRESSION                                            = 377;  // <PostfixExpression> ::= <Primary>
//		final int PROD_POSTFIXEXPRESSION2                                           = 378;  // <PostfixExpression> ::= <Name>
//		final int PROD_POSTFIXEXPRESSION3                                           = 379;  // <PostfixExpression> ::= <PostIncrementExpression>
//		final int PROD_POSTFIXEXPRESSION4                                           = 380;  // <PostfixExpression> ::= <PostDecrementExpression>
		final int PROD_POSTINCREMENTEXPRESSION_PLUSPLUS                             = 381;  // <PostIncrementExpression> ::= <PostfixExpression> '++'
		final int PROD_POSTDECREMENTEXPRESSION_MINUSMINUS                           = 382;  // <PostDecrementExpression> ::= <PostfixExpression> '--'
//		final int PROD_UNARYEXPRESSION                                              = 383;  // <UnaryExpression> ::= <PreIncrementExpression>
//		final int PROD_UNARYEXPRESSION2                                             = 384;  // <UnaryExpression> ::= <PreDecrementExpression>
		final int PROD_UNARYEXPRESSION_PLUS                                         = 385;  // <UnaryExpression> ::= '+' <UnaryExpression>
		final int PROD_UNARYEXPRESSION_MINUS                                        = 386;  // <UnaryExpression> ::= '-' <UnaryExpression>
//		final int PROD_UNARYEXPRESSION3                                             = 387;  // <UnaryExpression> ::= <UnaryExpressionNotPlusMinus>
		final int PROD_PREINCREMENTEXPRESSION_PLUSPLUS                              = 388;  // <PreIncrementExpression> ::= '++' <UnaryExpression>
		final int PROD_PREDECREMENTEXPRESSION_MINUSMINUS                            = 389;  // <PreDecrementExpression> ::= '--' <UnaryExpression>
//		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS                                  = 390;  // <UnaryExpressionNotPlusMinus> ::= <PostfixExpression>
		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS_TILDE                            = 391;  // <UnaryExpressionNotPlusMinus> ::= '~' <UnaryExpression>
		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS_EXCLAM                           = 392;  // <UnaryExpressionNotPlusMinus> ::= '!' <UnaryExpression>
//		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS2                                 = 393;  // <UnaryExpressionNotPlusMinus> ::= <CastExpression>
		final int PROD_CASTEXPRESSION_LPAREN_RPAREN                                 = 394;  // <CastExpression> ::= '(' <PrimitiveType> <Dims> ')' <UnaryExpression>
		final int PROD_CASTEXPRESSION_LPAREN_RPAREN2                                = 395;  // <CastExpression> ::= '(' <PrimitiveType> ')' <UnaryExpression>
		final int PROD_CASTEXPRESSION_LPAREN_RPAREN3                                = 396;  // <CastExpression> ::= '(' <Expression> ')' <UnaryExpressionNotPlusMinus>
		final int PROD_CASTEXPRESSION_LPAREN_RPAREN4                                = 397;  // <CastExpression> ::= '(' <Name> <Dims> ')' <UnaryExpressionNotPlusMinus>
//		final int PROD_MULTIPLICATIVEEXPRESSION                                     = 398;  // <MultiplicativeExpression> ::= <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_TIMES                               = 399;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> '*' <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_DIV                                 = 400;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> '/' <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_PERCENT                             = 401;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> '%' <UnaryExpression>
//		final int PROD_ADDITIVEEXPRESSION                                           = 402;  // <AdditiveExpression> ::= <MultiplicativeExpression>
		final int PROD_ADDITIVEEXPRESSION_PLUS                                      = 403;  // <AdditiveExpression> ::= <AdditiveExpression> '+' <MultiplicativeExpression>
		final int PROD_ADDITIVEEXPRESSION_MINUS                                     = 404;  // <AdditiveExpression> ::= <AdditiveExpression> '-' <MultiplicativeExpression>
//		final int PROD_SHIFTEXPRESSION                                              = 405;  // <ShiftExpression> ::= <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_LTLT                                         = 406;  // <ShiftExpression> ::= <ShiftExpression> '<<' <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_GTGT                                         = 407;  // <ShiftExpression> ::= <ShiftExpression> '>>' <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_GTGTGT                                       = 408;  // <ShiftExpression> ::= <ShiftExpression> '>>>' <AdditiveExpression>
//		final int PROD_RELATIONALEXPRESSION                                         = 409;  // <RelationalExpression> ::= <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_LT                                      = 410;  // <RelationalExpression> ::= <RelationalExpression> '<' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_GT                                      = 411;  // <RelationalExpression> ::= <RelationalExpression> '>' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_LTEQ                                    = 412;  // <RelationalExpression> ::= <RelationalExpression> '<=' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_GTEQ                                    = 413;  // <RelationalExpression> ::= <RelationalExpression> '>=' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_INSTANCEOF                              = 414;  // <RelationalExpression> ::= <RelationalExpression> instanceof <ReferenceType>
//		final int PROD_EQUALITYEXPRESSION                                           = 415;  // <EqualityExpression> ::= <RelationalExpression>
		final int PROD_EQUALITYEXPRESSION_EQEQ                                      = 416;  // <EqualityExpression> ::= <EqualityExpression> '==' <RelationalExpression>
		final int PROD_EQUALITYEXPRESSION_EXCLAMEQ                                  = 417;  // <EqualityExpression> ::= <EqualityExpression> '!=' <RelationalExpression>
//		final int PROD_ANDEXPRESSION                                                = 418;  // <AndExpression> ::= <EqualityExpression>
		final int PROD_ANDEXPRESSION_AMP                                            = 419;  // <AndExpression> ::= <AndExpression> '&' <EqualityExpression>
//		final int PROD_EXCLUSIVEOREXPRESSION                                        = 420;  // <ExclusiveOrExpression> ::= <AndExpression>
		final int PROD_EXCLUSIVEOREXPRESSION_CARET                                  = 421;  // <ExclusiveOrExpression> ::= <ExclusiveOrExpression> '^' <AndExpression>
//		final int PROD_INCLUSIVEOREXPRESSION                                        = 422;  // <InclusiveOrExpression> ::= <ExclusiveOrExpression>
		final int PROD_INCLUSIVEOREXPRESSION_PIPE                                   = 423;  // <InclusiveOrExpression> ::= <InclusiveOrExpression> '|' <ExclusiveOrExpression>
//		final int PROD_CONDITIONALANDEXPRESSION                                     = 424;  // <ConditionalAndExpression> ::= <InclusiveOrExpression>
		final int PROD_CONDITIONALANDEXPRESSION_AMPAMP                              = 425;  // <ConditionalAndExpression> ::= <ConditionalAndExpression> '&&' <InclusiveOrExpression>
//		final int PROD_CONDITIONALOREXPRESSION                                      = 426;  // <ConditionalOrExpression> ::= <ConditionalAndExpression>
		final int PROD_CONDITIONALOREXPRESSION_PIPEPIPE                             = 427;  // <ConditionalOrExpression> ::= <ConditionalOrExpression> '||' <ConditionalAndExpression>
//		final int PROD_CONDITIONALEXPRESSION                                        = 428;  // <ConditionalExpression> ::= <ConditionalOrExpression>
		final int PROD_CONDITIONALEXPRESSION_QUESTION_COLON                         = 429;  // <ConditionalExpression> ::= <ConditionalOrExpression> '?' <Expression> ':' <ConditionalExpression>
//		final int PROD_ASSIGNMENTEXPRESSION                                         = 430;  // <AssignmentExpression> ::= <ConditionalExpression>
//		final int PROD_ASSIGNMENTEXPRESSION2                                        = 431;  // <AssignmentExpression> ::= <Assignment>
		final int PROD_ASSIGNMENT                                                   = 432;  // <Assignment> ::= <LeftHandSide> <AssignmentOperator> <AssignmentExpression>
//		final int PROD_LEFTHANDSIDE                                                 = 433;  // <LeftHandSide> ::= <Name>
//		final int PROD_LEFTHANDSIDE2                                                = 434;  // <LeftHandSide> ::= <FieldAccess>
//		final int PROD_LEFTHANDSIDE3                                                = 435;  // <LeftHandSide> ::= <ArrayAccess>
		final int PROD_ASSIGNMENTOPERATOR_EQ                                        = 436;  // <AssignmentOperator> ::= '='
//		final int PROD_ASSIGNMENTOPERATOR_TIMESEQ                                   = 437;  // <AssignmentOperator> ::= '*='
//		final int PROD_ASSIGNMENTOPERATOR_DIVEQ                                     = 438;  // <AssignmentOperator> ::= '/='
//		final int PROD_ASSIGNMENTOPERATOR_PERCENTEQ                                 = 439;  // <AssignmentOperator> ::= '%='
		final int PROD_ASSIGNMENTOPERATOR_PLUSEQ                                    = 440;  // <AssignmentOperator> ::= '+='
		final int PROD_ASSIGNMENTOPERATOR_MINUSEQ                                   = 441;  // <AssignmentOperator> ::= '-='
//		final int PROD_ASSIGNMENTOPERATOR_LTLTEQ                                    = 442;  // <AssignmentOperator> ::= '<<='
//		final int PROD_ASSIGNMENTOPERATOR_GTGTEQ                                    = 443;  // <AssignmentOperator> ::= '>>='
//		final int PROD_ASSIGNMENTOPERATOR_GTGTGTEQ                                  = 444;  // <AssignmentOperator> ::= '>>>='
//		final int PROD_ASSIGNMENTOPERATOR_AMPEQ                                     = 445;  // <AssignmentOperator> ::= '&='
//		final int PROD_ASSIGNMENTOPERATOR_CARETEQ                                   = 446;  // <AssignmentOperator> ::= '^='
//		final int PROD_ASSIGNMENTOPERATOR_PIPEEQ                                    = 447;  // <AssignmentOperator> ::= '|='
//		final int PROD_EXPRESSION                                                   = 448;  // <Expression> ::= <AssignmentExpression>
//		final int PROD_CONSTANTEXPRESSION                                           = 449;  // <ConstantExpression> ::= <Expression>
	};

	//----------------------------- Preprocessor -----------------------------
	
	static final StringList CLASS_LITERAL = StringList.explodeWithDelimiter(".class", ".");

	/**
	 * Performs some necessary preprocessing for the text file. Actually opens the
	 * file, filters it and writes a new temporary file "Structorizer&lt;random&gt;.{defaultExt}",
	 * which is then actually parsed.<br/>
	 * The preprocessed file will always be saved with UTF-8 encoding.<br/>
	 * NOTE: For interactive mode, there should be frequent tests with either
	 * {@link #isCancelled()} or {@link #doStandardCancelActionIfRequested()} whether
	 * the parser thread was asked to stop. If so, then a return or an exception are
	 * recommended in order to respond to the cancel request. 
	 * @param _textToParse - name (path) of the source file
	 * @param _encoding - the expected encoding of the source file.
	 * @return The File object associated with the preprocessed source file.
	 */
	@Override
	protected File prepareTextfile(String _textToParse, String _encoding) throws ParserCancelled
	{
		/* Not sensibly achievable (too complicated syntactical analysis to find the
		 * correct place for the class definition insertion (after the last import
		 * and before the outer class or interface definition: The possibly recursive
		 * annotations, which may be interwoven with comments, are the problem.
		 */
		
		//boolean wrapInClass = (Boolean)this.getPluginOption("wrap_in_dummy_class", false);
		
		/*
		 * Another well-known problem is that the closing angular brackets
		 * of type parameters or arguments clash together and are then mistaken
		 * as a shift operator. So we may have to separate them since it is
		 * beyond the power of our grammar to solve this.
		 * Unfortunately it is neither feasible to tell these occurrences
		 * from actual operator symbols ">>" or ">>>" with a simple heuristics.
		 * So the only approach would be to offer it interactively. But this
		 * would not be sustainable, i.e. the user would have to be asked again
		 * and again if another error occurred. Hence it is way simpler and more
		 * effective to let the user manually modify the source code.
		 */

		File interm = null;
		try
		{
			File file = new File(_textToParse);
			DataInputStream in = new DataInputStream(new FileInputStream(file));
			// START KGU#193 2016-05-04
			BufferedReader br = new BufferedReader(new InputStreamReader(in, _encoding));
			// END KGU#193 2016-05-04
			StringBuilder srcCode = new StringBuilder();
			try {
				String strLine;
//				boolean withinComment = false;	// Registers a block comment context
//				boolean inDeclaration = false;	// e.g. package or import declaration
//				boolean mayBeClassDeclaration = false;
				//Read File Line By Line
				while ((strLine = br.readLine()) != null)   
				{
					checkCancelled();
					
					// TODO The following checks should better be done without line restriction
//					if (wrapInClass) {
//						int len = strLine.length();
//						for (int i = 0; i < len; i++) {
//							char ch = strLine.charAt(i);
//							char ch1 = '\0';
//							if (i+1 < len) {
//								ch1 = strLine.charAt(i+1);
//							}
//							if (withinComment) {
//								// The only thing to do here is to check for the end of the comment
//								if (ch == '*' && ch1 == '/') {
//									withinComment = false;
//									i++;
//								}
//							}
//							else switch (ch) {
//							case '/':
//								// Likely to be a comment
//								if (ch1 == '*') {
//									withinComment = true;
//									i++;
//								}
//								else if (ch1 == '/') {
//									i = len;
//								}
//								break;
//							case 'p':
//								// Might be "package" or "public" etc.
//								if (!inDeclaration) {
//									String tail = strLine.substring(i);
//									if (tail.equals("package") || tail.startsWith("package ")) {
//										inDeclaration = true;
//										i += "package".length();
//									}
//									else {
//										/* TODO Check the possible modifiers for a class
//										 * or interface declaration unless we are already
//										 * within a class or interface declaration
//										 */
//									}
//								}
//								break;
//							case '@':
//								// Seems to be an annotation
//								// TODO Find its end: tokenize? What about comments then?
//							}
//						}
//					}
					
					/* We have to replace "class" as a component identifier
					 * as in "Logger.getLogger(XYZClass.class.getName())"
					 */
					if (strLine.contains("class")) {
						// Tokenization is to make sure that we don't substitute in wrong places
						StringList tokens = Element.splitLexically(strLine, true);
						int ixClass = -1;
						boolean replaced = false;
						while ((ixClass = tokens.indexOf(CLASS_LITERAL, 0, true)) >= 0) {
							tokens.set(ixClass+1, "c_l_a_s_s");
							replaced = true;
						}
						if (replaced) {
							strLine = tokens.concatenate();
						}
					}
					srcCode.append(strLine + "\n");
				}
				//Close the input stream
			}
			finally {
				in.close();
			}

			//System.out.println(srcCode);

			// trim and save as new file
			checkCancelled();
			interm = File.createTempFile("Structorizer", "." + getFileExtensions()[0]);
			
			try (OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(interm), "UTF-8")) {
				ow.write(srcCode.toString().trim()+"\n");
			}
		}
		catch (Exception e) 
		{
			getLogger().log(Level.WARNING, this.getClass().getSimpleName() + ".prepareTextfile()", e);
			//System.err.println(this.getClass().getSimpleName() + ".prepareTextfile() -> " + e.getMessage());
			e.printStackTrace();	
		}
		return interm;
	}

	//---------------------- Build methods for structograms ---------------------------

	/** Caches the declared package of the outer class */
	private String packageStr = null;
	/** Caches the import directives */
	private StringList imports = null;
	/** Represents the field definitions of the hierarchical class context*/
	private Stack<Root> includables = null;
	/** Represents the hierarchical class context*/
	private Stack<Root> classes = null;
	
	private boolean optionTranslate = false;
	
	/** Maps the labels of labelled statements to the respective first element of the statement */
	private HashMap<String, Element> labels = new HashMap<String, Element>();
	
	/** Maps Java operator symbols to preferred Structorizer symbols */
	private static final HashMap<String, String> operatorMap = new HashMap<String, String>();
	static {
		operatorMap.put("!", "not");
		operatorMap.put("%", "mod");
		operatorMap.put("&&", "and");
		operatorMap.put("||", "or");
		operatorMap.put("<<", "shl");
		operatorMap.put(">>", "shr");
		operatorMap.put("=", "<-");
		operatorMap.put("!=", "<>");
	}
	
	/* (non-Javadoc)
	 * @see CodeParser#initializeBuildNSD()
	 */
	@Override
	protected void initializeBuildNSD() throws ParserCancelled
	{
		// TODO insert initializations for the build phase if necessary ...
		packageStr = null;
		imports = new StringList();
		includables = new Stack<Root>();
		classes = new Stack<Root>();
		addRoot(root);
		
		optionTranslate = (Boolean)this.getPluginOption("convert_syntax", false);
		
		// START KGU#407 2018-03-26: Enh. #420: Configure the lookup table for comment retrieval
		this.registerStatementRuleIds(statementIds);
		// END KGU#407 2018-06-26
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.parsers.CodeParser#buildNSD_R(com.creativewidgetworks.goldparser.engine.Reduction, lu.fisch.structorizer.elements.Subqueue)
	 */
	@Override
	protected void buildNSD_R(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled
	{
		//String content = new String();
		// START KGU#537 2018-07-01: Enh. #553
		checkCancelled();
		// END KGU#537 2018-07-01
		if (_reduction.size() > 0)
		{
			//String rule = _reduction.getParent().toString();
			//String ruleHead = _reduction.getParent().getHead().toString();
			int ruleId = _reduction.getParent().getTableIndex();
			//System.out.println("buildNSD_R(" + rule + ", " + _parentNode.parent + ")...");
			// START KGU#537 2018-06-30: Enh. #553
			checkCancelled();
			// END KGU#537 2018-06-30

			switch (ruleId) {
			/* -------- Begin code example for tree analysis and build -------- */
//			// Assignment or procedure call?
//			case RuleConstants.PROD_OPASSIGN_EQ:
//			case RuleConstants.PROD_VALUE_ID_LPAREN_RPAREN:
//			case RuleConstants.PROD_VALUE_ID_LPAREN_RPAREN2:
//			case RuleConstants.PROD_VALUE_ID_LPAREN_RPAREN:
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
//			break;
			/* -------- End code example for tree analysis and build -------- */
			case RuleConstants.PROD_PACKAGEDECLARATION_PACKAGE_SEMI:
				// <PackageDeclaration> ::= package <Name> ';'
				packageStr = this.getContent_R(_reduction.get(1).asReduction(), "");
				break;
				
			case RuleConstants.PROD_SINGLETYPEIMPORTDECLARATION_IMPORT_SEMI:
			case RuleConstants.PROD_TYPEIMPORTONDEMANDDECLARATION_IMPORT_DOT_TIMES_SEMI:
			{
				imports.add(this.getContent_R(_reduction.get(1).asReduction(), ""));
			}
			break;
			
			case RuleConstants.PROD_SINGLESTATICIMPORTDECLARATION_IMPORT_STATIC_SEMI:
			case RuleConstants.PROD_STATICIMPORTONDEMANDDECLARATION_IMPORT_STATIC_DOT_TIMES_SEMI:
			{
				imports.add(this.getContent_R(_reduction.get(2).asReduction(), ""));
			}
			break;
			
			case RuleConstants.PROD_NORMALCLASSDECLARATION:
			case RuleConstants.PROD_LOCALCLASSDECLARATION:
			case RuleConstants.PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER:
			case RuleConstants.PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER2:
			case RuleConstants.PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER3:
			case RuleConstants.PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER4:
			case RuleConstants.PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER:
			case RuleConstants.PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER2:
			case RuleConstants.PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER3:
			case RuleConstants.PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER4:
			{
				// <NormalClassDeclaration> ::= <Modifiers> <PureClassDeclaration>
				// <LocalClassDeclaration> ::= <LocalClassModifiers> <PureClassDeclaration>
				// <PureClassDeclaration> ::= class Identifier <TypeParametersOpt> <Super> <Interfaces> <ClassBody>
				// <PureClassDeclaration> ::= class Identifier <TypeParametersOpt> <Super> <ClassBody>
				// <PureClassDeclaration> ::= class Identifier <TypeParametersOpt> <Interfaces> <ClassBody>
				// <PureClassDeclaration> ::= class Identifier <TypeParametersOpt> <ClassBody>
				// <InterfaceDeclaration> ::= <Annotations> <Modifiers> interface Identifier <TypeParametersOpt> <ExtendsInterfaces> <InterfaceBody>
				// <InterfaceDeclaration> ::= <Annotations> <Modifiers> interface Identifier <TypeParametersOpt> <InterfaceBody>
				// <InterfaceDeclaration> ::= <Annotations> interface Identifier <TypeParametersOpt> <ExtendsInterfaces> <InterfaceBody>
				// <InterfaceDeclaration> ::= <Annotations> interface Identifier <TypeParametersOpt> <InterfaceBody>
				// Fetch comment and append modifiers
				String modifiers = "";
				int ixName = 1;
				if (ruleId == RuleConstants.PROD_NORMALCLASSDECLARATION
						|| ruleId == RuleConstants.PROD_LOCALCLASSDECLARATION) {
					// Get modifiers (in order to append them to the comment)
					modifiers = this.getContent_R(_reduction.get(0));
					_reduction = _reduction.get(1).asReduction();
					ruleId = _reduction.getParent().getTableIndex();
				}
				else if (ruleId == RuleConstants.PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER
						|| ruleId == RuleConstants.PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER2) {
					modifiers = this.getContent_R(_reduction.get(1));
					ixName = 3;
				}
				else if (ruleId == RuleConstants.PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER3
						|| ruleId == RuleConstants.PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER4) {
					ixName = 2;
				}
				// Get the name
				String name = _reduction.get(ixName).asString();
				String category = _reduction.get(ixName-1).asString(); // "class" or "interface"
				String qualifier = packageStr;
				Root classRoot = root;
				if (!this.classes.isEmpty()) {
					qualifier = this.classes.peek().getQualifiedName();
					classRoot = new Root();
					this.addRoot(classRoot);
				}
				classes.push(classRoot);
				classRoot.setNamespace(qualifier);
				
				Root incl = new Root();
				incl.setInclude();
				incl.setNamespace(qualifier);
				this.addRoot(incl);
				if (!includables.isEmpty()) {
					incl.addToIncludeList(includables.peek());
				}
				includables.push(incl);
				classRoot.setText(name);
				incl.setText(name + "_Fields");
				classRoot.addToIncludeList(incl);
				
				this.equipWithSourceComment(classRoot, _reduction);
				// Get type parameters (if any)
				String typePars = this.getContent_R(_reduction.get(2));
				int ixBody = ixName + 2;
				// Get the inheritance
				String inh = "";
				if (ruleId != RuleConstants.PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER4
						&& ruleId != RuleConstants.PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER2
						&& ruleId != RuleConstants.PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER4) {
					inh = this.getContent_R(_reduction.get(ixBody++).asReduction(), inh);
					if (ruleId == RuleConstants.PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER) {
						inh = this.getContent_R(_reduction.get(ixBody++).asReduction(), inh);
					}
				}
				if (this.classes.size() == 1 && packageStr != null) {
					classRoot.comment.add("==== package: " + packageStr);
					if (!imports.isEmpty()) {
						imports.insert("==== imports:", 0);
						incl.comment.add(imports);
					}
				}
				if (this.classes.size() > 1) {
					classRoot.comment.add(category.toUpperCase() + " in class " + qualifier);
				}
				classRoot.getComment().add((modifiers + " " + category).trim());
				if (!typePars.trim().isEmpty()) {
					classRoot.getComment().add("==== type parameters: " + typePars);
				}
				// Now descend into the body
				this.buildNSD_R(_reduction.get(ixBody).asReduction(), _parentNode);
				this.classes.pop();
				this.includables.pop();
			}
			break;
			
			case RuleConstants.PROD_ENUMDECLARATION_ENUM_IDENTIFIER:
			case RuleConstants.PROD_ENUMDECLARATION_ENUM_IDENTIFIER2:
			{
				// <EnumDeclaration> ::= <Modifiers> enum Identifier <Interfaces> <EnumBody>
				// <EnumDeclaration> ::= <Modifiers> enum Identifier <EnumBody>
				String modifiers = this.getContent_R(_reduction.get(0));
				String name = this.getContent_R(_reduction.get(2));
				Reduction redBody = _reduction.get(_reduction.size()-1).asReduction();
				// PROD_ENUMBODY_LBRACE_COMMA_RBRACE: <EnumBody> ::= '{' <EnumConstants> ',' <EnumBodyDeclarationsOpt> '}'
				// PROD_ENUMBODY_LBRACE_RBRACE:       <EnumBody> ::= '{' <EnumConstants> <EnumBodyDeclarationsOpt> '}'
				Reduction redConstants = redBody.get(1).asReduction();
				// PROD_ENUMCONSTANTS:       <EnumConstants> ::= <EnumConstant>
				// PROD_ENUMCONSTANTS_COMMA: <EnumConstants> ::= <EnumConstants> ',' <EnumConstant>
				boolean isClass = false;
				final StringList EMPTY_SL = new StringList();
				StringList itemNames = new StringList();
				Stack<StringList> itemValues = new Stack<StringList>();
				HashMap<String, Reduction> classBodies = new HashMap<String, Reduction>();
				do {
					Reduction redConst = redConstants;
					// Prepare the next loop pass
					if (redConstants.getParent().getTableIndex() == RuleConstants.PROD_ENUMCONSTANTS_COMMA) {
						redConst = redConstants.get(2).asReduction();
						redConstants = redConstants.get(0).asReduction();
					}
					else {
						redConstants = null;
					}
					// Now process the item...
					// PROD_ENUMCONSTANT_IDENTIFIER_LPAREN_RPAREN: <EnumConstant> ::= <Annotations> Identifier '(' <ArgumentList> ')' <ClassBodyOpt>
					// PROD_ENUMCONSTANT_IDENTIFIER:               <EnumConstant> ::= <Annotations> Identifier <ClassBodyOpt>
					String itemName = redConst.get(1).asString();
					if (redConst.getParent().getTableIndex() == RuleConstants.PROD_ENUMCONSTANT_IDENTIFIER) {
						itemNames.add(itemName);
						itemValues.push(EMPTY_SL); // consecutive value
					}
					else {
						// Form the constructor call
						isClass = true;
						StringList value = new StringList();
						this.processArguments(redConst.get(3), StringList.getNew(name), value);
						itemNames.add(itemName);
						itemValues.push(value);
					}
					Reduction redItemBody = redConst.get(redConst.size()-1).asReduction();
					if (redItemBody != null && redItemBody.getParent().getTableIndex() == 
							RuleConstants.PROD_CLASSBODY_LBRACE_RBRACE) {
						// There is a non-empty class body
						isClass = true;
						classBodies.put(itemName, redItemBody);
					}
				} while (redConstants != null);

				// Now we have to decide among a mere type definition or a class declaration
				Reduction redDecls = redBody.get(redBody.size()-2).asReduction();
				if (isClass || redDecls != null && redDecls.size() > 0) {
					// We will have to define a member class
					Root enumRoot = root;
					Root enumIncl = new Root();
					enumIncl.setInclude();
					enumIncl.setText(name + "_Enum");
					String qualifier = packageStr;
					if (!classes.isEmpty()) {
						enumRoot = new Root();
						qualifier = classes.peek().getQualifiedName();
						enumIncl.addToIncludeList(includables.peek());
					}
					enumRoot.setText(name);
					enumRoot.setNamespace(qualifier);
					enumIncl.setNamespace(qualifier);
					enumRoot.addToIncludeList(enumIncl);
					this.equipWithSourceComment(enumRoot, _reduction);
					this.equipWithSourceComment(enumIncl, _reduction);
					enumRoot.comment.add(modifiers);
					if (ruleId == RuleConstants.PROD_ENUMDECLARATION_ENUM_IDENTIFIER) {
						enumIncl.comment.add("==== " + this.getContent_R(_reduction.get(3)));
					}
					addRoot(enumRoot);
					addRoot(enumIncl);
					if (redDecls != null && redDecls.size() > 0) {
						classes.push(enumRoot);
						includables.push(enumIncl);
						this.buildNSD_R(redDecls.get(1).asReduction(), enumRoot.children);
						classes.pop();
						includables.pop();
					}
					int itemOffset = 0;
					String prevValue = null;
					for (int i = itemNames.count()-1; i > 0; i--) {
						String itemName = itemNames.get(i);
						StringList exprs = itemValues.pop();
						String value = Integer.toString(itemOffset);
						if (exprs.isEmpty()) {
							if (prevValue != null) {
								value = prevValue + " + " + value;
							}
						}
						else if (exprs.count() == 1) {
							value = prevValue = exprs.get(0);
							itemOffset = 0;
						}
						else {
							// Put all preparatory assignments as initialisation code
							enumIncl.children.addElement(
									new Instruction(exprs.subSequence(0, exprs.count()-1)));
							value = prevValue = exprs.get(exprs.count()-1);
							itemOffset = 0;
						}
						enumIncl.children.addElement(
								new Instruction("const " + itemName + " <- " + value));
						itemOffset++;
						if (classBodies.containsKey(itemName)) {
							// Produce a subclass for the specific item
							Root itemBody = new Root();
							itemBody.setText(itemName);
							Root itemIncl = new Root();
							itemIncl.setText(itemName + "_Fields");
							itemBody.setNamespace(qualifier + "." + name);
							itemIncl.setNamespace(qualifier + "." + name);
							itemBody.setComment("Specific CLASS for enum item "
									+ itemBody.getNamespace() + "." + itemName);
							itemIncl.addToIncludeList(includables.peek());
							itemBody.addToIncludeList(itemIncl);
							addRoot(itemBody);
							addRoot(itemIncl);
							classes.push(itemBody);
							includables.push(itemIncl);
							this.buildNSD_R(classBodies.get(itemName), itemBody.children);
							classes.pop();
							includables.pop();
						}
					}
				}
				else {
					// This is going to be a type definition
					// Users may break the lines at their preference afterwards...
					Instruction ele = new Instruction("type " + name + " = enum{"
							+ itemNames.reverse().concatenate(", ") + "}");
					this.equipWithSourceComment(ele, _reduction);
					ele.comment.add(modifiers);
					includables.peek().children.addElement(ele);
				}
				
			}
			break;
			
			case RuleConstants.PROD_FIELDDECLARATION_SEMI:
			case RuleConstants.PROD_FIELDDECLARATION_SEMI2:
			{
				// <FieldDeclaration> ::= <Annotations> <Modifiers> <Type> <VariableDeclarators> ';'
				// <FieldDeclaration> ::= <Annotations> <Type> <VariableDeclarators> ';'
				String modifiers = null;
				boolean allConstant = false;
				int ixType = 1;
				if (ruleId == RuleConstants.PROD_FIELDDECLARATION_SEMI) {
					modifiers = this.getContent_R(_reduction.get(1));
					if (modifiers.contains("final")) {
						allConstant = true;
					}
					ixType++;
				}
				String type = this.translateType(_reduction.get(ixType));
				StringList decls = processVarDeclarators(_reduction.get(ixType+1), type, allConstant);
				Element ele = this.equipWithSourceComment(new Instruction(decls), _reduction);
				ele.comment.add("FIELD in class " + classes.peek().getQualifiedName());
				ele.setColor(colorGlobal);
				if (allConstant) {
					ele.setColor(colorConst);
				}
				if (modifiers != null) {
					ele.comment.add(modifiers);
				}
				includables.peek().children.addElement(ele);
			}
			break;
			
			case RuleConstants.PROD_STATICINITIALIZER_STATIC:
				// <StaticInitializer> ::= <Annotations> static <Block>
				this.buildNSD_R(_reduction.get(2).asReduction(), includables.peek().children);
				break;
			
			case RuleConstants.PROD_METHODDECLARATION:
			case RuleConstants.PROD_CONSTRUCTORDECLARATION:
			case RuleConstants.PROD_CONSTRUCTORDECLARATION2:
			case RuleConstants.PROD_CONSTRUCTORDECLARATION3:
			case RuleConstants.PROD_CONSTRUCTORDECLARATION4:
			{
				// <MethodDeclaration> ::= <Annotations> <MethodHeader> <MethodBody>
				// <ConstructorDeclaration> ::= <Annotations> <Modifiers> <ConstructorDeclarator> <Throws> <ConstructorBody>
				// <ConstructorDeclaration> ::= <Annotations> <Modifiers> <ConstructorDeclarator> <ConstructorBody>
				// <ConstructorDeclaration> ::= <Annotations> <ConstructorDeclarator> <Throws> <ConstructorBody>
				// <ConstructorDeclaration> ::= <Annotations> <ConstructorDeclarator> <ConstructorBody>
				// Ignore the annotations (or add them as comment?)
				int ixHeader = 1;
				Root subRoot = new Root();
				this.equipWithSourceComment(subRoot, _reduction);
				String qualifier = classes.peek().getQualifiedName();
				subRoot.setNamespace(qualifier);
				subRoot.comment.add((ruleId == RuleConstants.PROD_METHODDECLARATION ? "METHOD" : "CONSTRUCTOR")
						+ " for class " + qualifier);
				if (ruleId == RuleConstants.PROD_CONSTRUCTORDECLARATION
						|| ruleId == RuleConstants.PROD_CONSTRUCTORDECLARATION2) {
					// Add the modifiers to the comment
					subRoot.getComment().add(this.getContent_R(_reduction.get(1)));
					ixHeader++;
				}
				if (ruleId == RuleConstants.PROD_CONSTRUCTORDECLARATION
						|| ruleId == RuleConstants.PROD_CONSTRUCTORDECLARATION3) {
					// Add the throws clause to the comment
					subRoot.getComment().add("==== " + this.getContent_R(_reduction.get(ixHeader + 1)));
				}
				Root targetRoot = subRoot;
				// Extract the method header
				boolean isMain = this.applyMethodHeader(
						_reduction.get(ixHeader).asReduction(), subRoot);
				if (isMain) {
					// Add the body elements to the class Root instead
					targetRoot = classes.peek();
					// Prepare a disabled instruction element showing the declaration
					Instruction decl = new Instruction(subRoot.getText());
					decl.setComment(subRoot.getComment());
					decl.setColor(colorDecl);
					decl.disabled = true;
					// Append the declaration
					targetRoot.children.addElement(decl);
				}
				else {
					// In any other case just add the method as is to the pool
					subRoot.addToIncludeList(includables.peek());
					addRoot(subRoot);
				}
				// Now build the method body
				this.buildNSD_R(_reduction.get(2).asReduction(), targetRoot.children);
			}
			break;
	
			//case RuleConstants.PROD_LOCALVARIABLEDECLARATIONSTATEMENT_SEMI:
			case RuleConstants.PROD_LOCALVARIABLEDECLARATION_FINAL:
			case RuleConstants.PROD_LOCALVARIABLEDECLARATION:
			{
				// <LocalVariableDeclarationStatement> ::= <LocalVariableDeclaration> ';'
				// <LocalVariableDeclaration> ::= final <Type> <VariableDeclarators>
				// <LocalVariableDeclaration> ::= <Type> <VariableDeclarators>
				boolean isConst = ruleId == RuleConstants.PROD_LOCALVARIABLEDECLARATION_FINAL;
				String type = this.translateType(_reduction.get(isConst ? 1 : 0));
				StringList vars = this.processVarDeclarators(_reduction.get(isConst ? 2 : 1), type, isConst);
				Instruction ele = new Instruction(vars);
				if (isConst) {
					ele.setColor(colorConst);
				}
				_parentNode.addElement(this.equipWithSourceComment(ele, _reduction));
			}
			break;
			
			case RuleConstants.PROD_IFTHENSTATEMENT_IF_LPAREN_RPAREN:
			case RuleConstants.PROD_IFTHENELSESTATEMENT_IF_LPAREN_RPAREN_ELSE:
			case RuleConstants.PROD_IFTHENELSESTATEMENTNOSHORTIF_IF_LPAREN_RPAREN_ELSE:
			{
				// <IfThenStatement> ::= if '(' <Expression> ')' <Statement>
				// <IfThenElseStatement> ::= if '(' <Expression> ')' <StatementNoShortIf> else <Statement>
				// <IfThenElseStatementNoShortIf> ::= if '(' <Expression> ')' <StatementNoShortIf> else <StatementNoShortIf>
				StringList cond = this.decomposeExpression(_reduction.get(2), false, false);
				int ixLast = cond.count()-1; // index of the last line of the condition (if there are more than 1)
				if (ixLast > 0) {
					Instruction prep = new Instruction(cond.subSequence(0, ixLast));
					prep.setColor(colorMisc);
					_parentNode.addElement(prep);
				}
				Alternative alt = new Alternative(cond.get(ixLast));
				this.equipWithSourceComment(alt, _reduction);
				if (ixLast > 0) {
					alt.setColor(colorMisc);
				}
				_parentNode.addElement(alt);
				buildNSD_R(_reduction.get(4).asReduction(), alt.qTrue);
				if (_reduction.size() > 6) {
					buildNSD_R(_reduction.get(6).asReduction(), alt.qFalse);
				}
			}
			break;
			
			case RuleConstants.PROD_SWITCHSTATEMENT_SWITCH_LPAREN_RPAREN:
			{
				// <SwitchStatement> ::= switch '(' <Expression> ')' <SwitchBlock>
				buildCase(_reduction, _parentNode);
			}
			break;
			
			case RuleConstants.PROD_BASICFORSTATEMENT_FOR_LPAREN_SEMI_SEMI_RPAREN:
			case RuleConstants.PROD_BASICFORSTATEMENTNOSHORTIF_FOR_LPAREN_SEMI_SEMI_RPAREN:
			{
				// <BasicForStatement> ::= for '(' <ForInitOpt> ';' <ExpressionOpt> ';' <ForUpdateOpt> ')' <Statement>
				// <BasicForStatementNoShortIf> ::= for '(' <ForInitOpt> ';' <ExpressionOpt> ';' <ForUpdateOpt> ')' <StatementNoShortIf>
				Subqueue body = null;
				int oldSize = _parentNode.getSize();
				Element ele = this.checkAndMakeFor(_reduction.get(2), _reduction.get(4), _reduction.get(6));
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
						_parentNode.getElement(i).setColor(colorMisc);
					}

					// get the second part - should be an ordinary condition
					String content = getContent_R(_reduction.get(4));
					if (content.trim().isEmpty()) {
						Forever loop = new Forever();
						ele = loop;
						body = loop.getBody();
					}
					else {
						StringList cond = this.decomposeExpression(_reduction.get(4), false, false);
						int ixLast = cond.count() - 1;	// index of the last line (might be more than 1)
						if (cond.count() > 1) {
							Instruction prep = new Instruction(cond.subSequence(0, ixLast));
							prep.setColor(colorMisc);
							_parentNode.addElement(prep);
						}
						While loop = new While((getOptKeyword("preWhile", false, true)
								+ translateContent(cond.get(ixLast))
								+ getOptKeyword("postWhile", true, false)).trim());
						ele = loop;
						body = loop.getBody();
					}
					// Mark all offsprings of the FOR loop with a (by default) yellowish colour
					ele.setColor(colorMisc);
				}
				
				this.equipWithSourceComment(ele, _reduction);
				_parentNode.addElement(ele);
				
				// Get and convert the body
				Reduction bodyRed = _reduction.get(8).asReduction();
				buildNSD_R(bodyRed, body);

				if (!(ele instanceof For)) {
					// get the last part of the header now and append it to the body
					oldSize = body.getSize();	// Maybe (though little likely) the increment part is empty
					buildNSD_R(_reduction.get(6).asReduction(), body);
					// Mark all offsprings of the FOR loop with a (by default) yellowish colour
					for (int i = oldSize; i < body.getSize(); i++) {
						body.getElement(i).setColor(colorMisc);
					}
				}
			}
			break;
			
			case RuleConstants.PROD_ENHANCEDFORSTATEMENT_FOR_LPAREN_COLON_RPAREN:
			case RuleConstants.PROD_ENHANCEDFORSTATEMENT_FOR_LPAREN_FINAL_COLON_RPAREN:
			case RuleConstants.PROD_ENHANCEDFORSTATEMENTNOSHORTIF_FOR_LPAREN_COLON_RPAREN:
			case RuleConstants.PROD_ENHANCEDFORSTATEMENTNOSHORTIF_FOR_LPAREN_FINAL_COLON_RPAREN:
			{
				// <EnhancedForStatement> ::= for '(' <Type> <VariableDeclaratorId> ':' <Expression> ')' <Statement>
				// <EnhancedForStatement> ::= for '(' final <Type> <VariableDeclaratorId> ':' <Expression> ')' <Statement>
				// <EnhancedForStatementNoShortIf> ::= for '(' <Type> <VariableDeclaratorId> ':' <Expression> ')' <StatementNoShortIf>
				// <EnhancedForStatementNoShortIf> ::= for '(' final <Type> <VariableDeclaratorId> ':' <Expression> ')' <StatementNoShortIf>
				int ixType = 2;
				if (ruleId == RuleConstants.PROD_ENHANCEDFORSTATEMENT_FOR_LPAREN_FINAL_COLON_RPAREN ||
						ruleId == RuleConstants.PROD_ENHANCEDFORSTATEMENTNOSHORTIF_FOR_LPAREN_FINAL_COLON_RPAREN) {
					ixType++;
				}
				// What do we do with the type? We might insert a declaration and disable it
				String type = this.translateType(_reduction.get(ixType));
				String loopVar = this.getContent_R(_reduction.get(ixType + 1));
				Instruction instr = new Instruction("var " + loopVar + ": " + type);
				instr.disabled = true;
				instr.setColor(colorMisc);
				_parentNode.addElement(instr);
				//String valList = this.translateContent(this.getContent_R(_reduction.get(ixType + 3)));
				StringList valList = decomposeExpression(_reduction.get(ixType + 3), false, false);
				int ixLast = valList.count() - 1;
				if (ixLast > 0) {
					Instruction prep = new Instruction(valList.subSequence(0, ixLast));
					prep.setColor(colorMisc);
					_parentNode.addElement(prep);
				}
				Element forIn = this.equipWithSourceComment(new For(loopVar, valList.get(ixLast)), _reduction);
				_parentNode.addElement(forIn);
				this.buildNSD_R(_reduction.get(ixType + 5).asReduction(), ((For)forIn).getBody());
			}
			break;
			
			case RuleConstants.PROD_WHILESTATEMENT_WHILE_LPAREN_RPAREN:
			case RuleConstants.PROD_WHILESTATEMENTNOSHORTIF_WHILE_LPAREN_RPAREN:
			{
				// <WhileStatement> ::= while '(' <Expression> ')' <Statement>
				// <WhileStatementNoShortIf> ::= while '(' <Expression> ')' <StatementNoShortIf>
				StringList cond = this.decomposeExpression(_reduction.get(2), false, false);
				int ixLast = cond.count() - 1;
				if (ixLast > 0) {
					Instruction prep = new Instruction(cond.subSequence(0, ixLast));
					_parentNode.addElement(prep);
				}
				While loop = (While)this.equipWithSourceComment(
						new While(getOptKeyword("preWhile", false, true)
								+ this.translateContent(cond.get(ixLast))
								+ getOptKeyword("postWhile", true, false)),
						_reduction);
				_parentNode.addElement(loop);
				this.buildNSD_R(_reduction.get(4).asReduction(), loop.getBody());
			}
			break;
			
			case RuleConstants.PROD_DOSTATEMENT_DO_WHILE_LPAREN_RPAREN_SEMI:
			{
				// <DoStatement> ::= do <Statement> while '(' <Expression> ')' ';'
				StringList cond = this.decomposeExpression(_reduction.get(4), false, false);
				int ixLast = cond.count() - 1;
				String cond0 = Element.negateCondition(cond.get(ixLast));
				Repeat loop = (Repeat)this.equipWithSourceComment(
						new Repeat(getOptKeyword("preRepeat", false, true)
								+ cond0
								+ getOptKeyword("posRepeat", true, false)),
						_reduction);
				_parentNode.addElement(loop);
				this.buildNSD_R(_reduction.get(1).asReduction(), loop.getBody());
				if (ixLast > 0) {
					Instruction prep = new Instruction(cond.subSequence(0, ixLast));
					prep.setColor(colorMisc);
					loop.getBody().addElement(prep);
				}
			}
			break;
			
			case RuleConstants.PROD_TRYSTATEMENT_TRY:
			case RuleConstants.PROD_TRYSTATEMENT_TRY2:
			case RuleConstants.PROD_TRYSTATEMENT_TRY3:
			case RuleConstants.PROD_TRYSTATEMENT_TRY4:
			case RuleConstants.PROD_TRYSTATEMENT_TRY5:
			case RuleConstants.PROD_TRYSTATEMENT_TRY6:
			case RuleConstants.PROD_TRYSTATEMENT_TRY7:
			{
				// <TryStatement> ::= try <Block> <Catches>
				// <TryStatement> ::= try <Block> <Catches> <Finally>
				// <TryStatement> ::= try <Block> <Finally>
				// <TryStatement> ::= try <ResourceSpecification> <Block>
				// <TryStatement> ::= try <ResourceSpecification> <Block> <Catches>
				// <TryStatement> ::= try <ResourceSpecification> <Block> <Catches> <Finally>
				// <TryStatement> ::= try <ResourceSpecification> <Block> <Finally>
				int ixBlock = 1;
				Try ele = new Try("exception");	// Just a temporary text
				if (ruleId >= RuleConstants.PROD_TRYSTATEMENT_TRY4) {
					/* Insert an Instruction to declare the resources (and assign them null)
					 * and prepare the specific instruction texts for Try block and Finally block
					 */
					Reduction redRscs = _reduction.get(1).asReduction().get(1).asReduction();
					// PROD_RESOURCES:      <Resources> ::= <Resource>
					// PROD_RESOURCES_SEMI: <Resources> ::= <Resources> ';' <Resource>
					StringList textRscDecls = new StringList();	// declaration lines in reverse order
					StringList textRscInits = new StringList();	// acquisition lines in reverse order
					do {
						Reduction redRsc = redRscs;
						if (redRscs.getParent().getTableIndex() == RuleConstants.PROD_RESOURCES_SEMI) {
							redRsc = redRscs.get(2).asReduction();
							redRscs = redRscs.get(0).asReduction();
						}
						else {
							redRscs = null;
						}
						// PROD_RESOURCE_EQ:       <Resource> ::= <Type> <VariableDeclaratorId> '=' <Expression>
						// PROD_RESOURCE_FINAL_EQ: <Resource> ::= final <Type> <VariableDeclaratorId> '=' <Expression>
						boolean isConst = redRsc.getParent().getTableIndex() == RuleConstants.PROD_RESOURCE_FINAL_EQ;
						int ixType = 0;
						if (isConst) {
							ixType++;
						}
						
						// Get the variable id of the resource
						String rscId = this.getContent_R(redRsc.get(ixType + 1));
						
						// Prepare a declaration entry for it
						textRscDecls.add("var " + rscId + ": " + this.translateType(redRsc.get(ixType)) + " <- null");
						
						// Now prepare the actual resource request (all in reverse order)
						StringList rscExprs = new StringList();
						int ixLast = rscExprs.count() - 1;
						textRscInits.add(rscId + " <- " + rscExprs.get(ixLast));
						textRscInits.add(rscExprs.subSequence(0, ixLast).reverse());
						
						// Now create a disposal instruction for the FINALLY block (reverse order is perfect)
						Alternative finAlt = new Alternative(
								this.getOptKeyword("preAlt", false, true) + rscId + " <> null");
						finAlt.setComment("FIXME: The comparison with null may have to be replaced!");
						Instruction disp = new Instruction("dispose(" + rscId + ")");
						disp.setComment("FIXME: Find the correct way to dispose the resource!");
						disp.setColor(colorMisc);
						finAlt.qTrue.addElement(disp);
						finAlt.setColor(colorMisc);
					} while (redRscs != null);
					
					// Insert the resource declaration block before the TRY element
					Instruction decls = new Instruction(textRscDecls.reverse());
					decls.setColor(colorMisc);
					decls.setComment("FIXME: The initialisation with null is Java-specific");
					_parentNode.addElement(decls);
					ele.setColor(colorMisc);
					
					// Put the combined resource acquisition instruction into the TRY block
					ele.qTry.addElement(new Instruction(textRscInits.reverse()));
					
					ixBlock++;
				}
				this.buildNSD_R(_reduction.get(ixBlock).asReduction(), ele.qTry);
				if (ruleId != RuleConstants.PROD_TRYSTATEMENT_TRY3
						&& ruleId != RuleConstants.PROD_TRYSTATEMENT_TRY4
						&& ruleId != RuleConstants.PROD_TRYSTATEMENT_TRY7) {
					// Get the catch clauses and combine them sensibly
					// PROD_CATCHES:  <Catches> ::= <CatchClause>
					// PROD_CATCHES2: <Catches> ::= <Catches> <CatchClause>
					// PROD_CATCHCLAUSE_CATCH_LPAREN_RPAREN:
					//                <CatchClause> ::= catch '(' <FormalParameter> ')' <Block>
					Reduction redCatches = _reduction.get(ixBlock+1).asReduction();
					if (redCatches.getParent().getTableIndex() != RuleConstants.PROD_CATCHES2) {
						// A single clause - no Case necessary
						String[] param = this.getFormalParameter(redCatches.get(2).asReduction());
						// Set the actual catch variable
						ele.setText(param[0]);
						ele.comment.add(param[1]);
						this.buildNSD_R(redCatches.get(4).asReduction(), ele.qCatch);
					}
					else {
						// Get all catch clauses and build a Case element from them
						// FIXME there should be a built-in function to retrieve the type name
						StringList distr = StringList.getNew("exception instanceof");
						Stack<Subqueue> handlers = new Stack<Subqueue>();
						do {
							Reduction redClause = redCatches;
							if (redCatches.getParent().getTableIndex() == RuleConstants.PROD_CATCHES2) {
								redClause = redCatches.get(1).asReduction();
								redCatches = redCatches.get(0).asReduction();
							}
							else {
								redCatches = null;
							}
							String[] param = this.getFormalParameter(redClause);
							// FIXME the built-in function to retrieve the type name should be applied here too
							distr.insert(param[1], 1);
							Subqueue sq = new Subqueue();
							// This would require a type cast, actually...
							sq.addElement(new Instruction(param[0] + " <- exception"));
							this.buildNSD_R(redClause.get(4).asReduction(), sq);
							handlers.push(sq);
						} while(redCatches != null);
						distr.add("%");
						Case catchCase = new Case(distr);
						int ixBranch = 0;
						while (!handlers.isEmpty()) {
							Subqueue sq = handlers.pop();
							catchCase.qs.set(ixBranch++, sq);
							sq.parent = catchCase;
						}
					}
				}
				int ixFinally = 0;
				if (ruleId == RuleConstants.PROD_TRYSTATEMENT_TRY2) {
					ixFinally = 3;
				}
				else if (ruleId == RuleConstants.PROD_TRYSTATEMENT_TRY3) {
					ixFinally = 2;
				}
				if (ixFinally > 0) {
					// <Finally> ::= finally <Block>
					this.buildNSD_R(_reduction.get(ixFinally).asReduction().get(1).asReduction(), ele.qFinally);
				}
				_parentNode.addElement(this.equipWithSourceComment(ele, _reduction));
			}
			break;
			
			case RuleConstants.PROD_LABELEDSTATEMENT_IDENTIFIER_COLON:
			case RuleConstants.PROD_LABELEDSTATEMENTNOSHORTIF_IDENTIFIER_COLON:
			{
				// <LabeledStatement> ::= Identifier ':' <Statement>
				// <LabeledStatementNoShortIf> ::= Identifier ':' <StatementNoShortIf>
				// We will just place a disabled Instruction showing the label, and map it
				String label = this.getContent_R(_reduction.get(0));
				Instruction ele = new Instruction(label + ":");
				ele.setColor(Color.RED);
				ele.disabled = true;
				_parentNode.addElement(ele);
				labels.put(label, ele);
				this.buildNSD_R(_reduction.get(2).asReduction(), _parentNode);
			}
			break;
			
			case RuleConstants.PROD_BREAKSTATEMENT_BREAK_IDENTIFIER_SEMI:
			{
				// <BreakStatement> ::= break Identifier ';'
				// FIXME: Should we try the count the loop levels in order to construct a "leave n"?
				String label = this.getContent_R(_reduction.get(0));
				Element labelledEle = labels.get(label);
				Element parent = _parentNode.parent;
				Jump ele = null;
				if (labelledEle != null && parent instanceof ILoop) {
					Subqueue target = (Subqueue)labelledEle.parent;
					int nLevels = 1;
					while ((parent = parent.parent) != null
							&& (parent = parent.parent) instanceof ILoop
							&& parent.parent != target) {
						nLevels++;
					}
					if (parent != null && parent.parent == target) {
						int ixLabel = target.getIndexOf(labelledEle);
						if (ixLabel + 1 < target.getSize() && target.getElement(ixLabel + 1) instanceof ILoop
								// Could be a decomposed For loop...
								|| ixLabel + 2 < target.getSize() && target.getElement(ixLabel + 2) instanceof While) {
							ele = new Jump(getKeyword("preLeave") + " " + Integer.toString(nLevels));
							this.equipWithSourceComment(ele, _reduction);
						}
					}
				}
				if (ele == null) {
					ele = new Jump(getContent_R(_reduction, ""));
					this.equipWithSourceComment(ele, _reduction);
					ele.setColor(Color.RED);
					ele.comment.add("An outer loop with label \"" + label + "\" was not found!");
				}
				_parentNode.addElement(ele);
			}
			break;
			
			case RuleConstants.PROD_BREAKSTATEMENT_BREAK_SEMI:
				// <BreakStatement> ::= break ';'
			{
				Jump jmp = new Jump(this.getOptKeyword("preLeave", false, false));
				this.equipWithSourceComment(jmp, _reduction);
				_parentNode.addElement(jmp);
			}
			break;
			
			case RuleConstants.PROD_CONTINUESTATEMENT_CONTINUE_IDENTIFIER_SEMI:
			case RuleConstants.PROD_CONTINUESTATEMENT_CONTINUE_SEMI:
				// <ContinueStatement> ::= continue Identifier ';'
				// <ContinueStatement> ::= continue ';'
			{
				Instruction ele = new Instruction(this.getContent_R(_reduction, ""));
				this.equipWithSourceComment(ele, _reduction);
				ele.setColor(Color.RED);
				ele.disabled = true;
				ele.comment.add("Continue statements are NOT SUPPORTED in Structorizer! Try to circumvent it.");
				_parentNode.addElement(ele);
			}
			break;
				
			case RuleConstants.PROD_RETURNSTATEMENT_RETURN_SEMI:
			case RuleConstants.PROD_RETURNSTATEMENT_RETURN_SEMI2:
			{
				// <ReturnStatement> ::= return <Expression> ';'
				// <ReturnStatement> ::= return ';'
				String text = getKeyword("preReturn");
				if (ruleId == RuleConstants.PROD_RETURNSTATEMENT_RETURN_SEMI) {
					// FIXME: Face an expression decomposition!
					text += " " + this.getContent_R(_reduction.get(1));
				}
				Jump jmp = new Jump(text);
				this.equipWithSourceComment(jmp, _reduction);
				_parentNode.addElement(jmp);
			}
			break;
			
			case RuleConstants.PROD_THROWSTATEMENT_THROW_SEMI:
			{
				// <ThrowStatement> ::= throw <Expression> ';'
				String text = getKeyword("preThrow");
				// FIXME: Face an expression decomposition!
				text += " " + this.getContent_R(_reduction.get(1));
				Jump jmp = new Jump(text);
				this.equipWithSourceComment(jmp, _reduction);
				_parentNode.addElement(jmp);
				
			}
			break;
			
			case RuleConstants.PROD_EXPLICITCONSTRUCTORINVOCATION_THIS_LPAREN_RPAREN_SEMI:
			case RuleConstants.PROD_EXPLICITCONSTRUCTORINVOCATION_THIS_LPAREN_RPAREN_SEMI2:
			case RuleConstants.PROD_EXPLICITCONSTRUCTORINVOCATION_SUPER_LPAREN_RPAREN_SEMI:
			case RuleConstants.PROD_EXPLICITCONSTRUCTORINVOCATION_SUPER_LPAREN_RPAREN_SEMI2:
			{
				// <ExplicitConstructorInvocation> ::= this '(' <ArgumentList> ')' ';'
				// <ExplicitConstructorInvocation> ::= this '(' ')' ';'
				// <ExplicitConstructorInvocation> ::= super '(' <ArgumentList> ')' ';'
				// <ExplicitConstructorInvocation> ::= super '(' ')' ';'
				StringList exprs = new StringList();
				StringList invocation = StringList.getNew(_reduction.get(0).asString());
				if (_reduction.size() > 3) {
					this.processArguments(_reduction.get(2), invocation, exprs);
					Instruction prep = new Instruction(exprs);
					this.equipWithSourceComment(prep, _reduction);
					_parentNode.addElement(prep);
				}
				else {
					Instruction constr = new Instruction(invocation);
					this.equipWithSourceComment(constr, _reduction);
					_parentNode.addElement(constr);
				}
			}
			break;
			
			case RuleConstants.PROD_SYNCHRONIZEDSTATEMENT_SYNCHRONIZED_LPAREN_RPAREN:
			{
				// <SynchronizedStatement> ::= synchronized '(' <Expression> ')' <Block>
				/* The only sensible thing here is to create an obvious block context
				 * with some comment. We will use a FOR loop of one single pass
				 */
				For ele = new For("synchronized", "0", "0", 1);
				this.equipWithSourceComment(ele, _reduction);
				ele.comment.add("synchronized (" + this.getContent_R(_reduction.get(2)) + ")");
				ele.setColor(colorConst);
				_parentNode.addElement(ele);
				this.buildNSD_R(_reduction.get(4).asReduction(), ele.q);
			}
			break;
			
			case RuleConstants.PROD_EXPRESSIONSTATEMENT_SEMI:
			{
				// <ExpressionStatement> ::= <StatementExpression> ';'
				//
				// <StatementExpression> ::= <Assignment>
				// <StatementExpression> ::= <PreIncrementExpression>
				// <StatementExpression> ::= <PreDecrementExpression>
				// <StatementExpression> ::= <PostIncrementExpression>
				// <StatementExpression> ::= <PostDecrementExpression>
				// <StatementExpression> ::= <MethodInvocation>
				// <StatementExpression> ::= <ClassInstanceCreationExpression>
				StringList exprLines = decomposeExpression(_reduction.get(0), true, false);
				// Separate all method invocations
				int i= 0;
				boolean commentDone = false;
				Instruction ele = null;
				while (exprLines.count() > 0 && i < exprLines.count()) {
					String line = exprLines.get(i);
					if (Instruction.isFunctionCall(line) || Instruction.isProcedureCall(line)) {
						if (i > 0) {
							ele = new Instruction(exprLines.subSequence(0, i));
							if (!commentDone) {
								this.equipWithSourceComment(ele, _reduction);
							}
							_parentNode.addElement(ele);
						}
						ele = new Instruction(line);
						if (!commentDone) {
							this.equipWithSourceComment(ele, _reduction);
						}
						_parentNode.addElement(ele);
						exprLines.remove(0, i+1);
						i = 0;
					}
					else {
						i++;
					}
				}
				// Are some lines left? Then make an instruction from it
				if (!exprLines.isEmpty()) {
					ele = new Instruction(exprLines);
					if (!commentDone) {
						this.equipWithSourceComment(ele, _reduction);
					}
					_parentNode.addElement(ele);
				}
			}
			break;
			
			default:
				if (_reduction.size() > 0)
				{
					for (int i=0; i<_reduction.size(); i++)
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
	 * Analyses the expression tree represented by {@code exprToken} and decomposes
	 * the expression such that embedded implicit or explicit assignments are extracted
	 * to an own preceding line, combined assignment operator expressions are also
	 * decomposed, e.g. {@code <var> += <expr>} to {@code <var> <- <var> + <expr>}.
	 * @param isStatement - whether the expression represents a statement
	 * @param leftHandSide - indicates whether the expression represents an assignment
	 * target expression.
	 * @param redExpr - the top-level expression {@link Reduction}.
	 * @return a {@code StringList} each elements of which contain an expression, all
	 * but the very last one are necessarily expression statements.
	 * @throws ParserCancelled 
	 */
	private StringList decomposeExpression(Token exprToken, boolean isStatement, boolean leftHandSide) throws ParserCancelled {
		// We expect redExpr to represent one of the following
		// <Assignment>
		// <PreIncrementExpression>
		// <PreDecrementExpression>
		// <PostIncrementExpression>
		// <PostDecrementExpression>
		// <MethodInvocation>
		// <ClassInstanceCreationExpression>
		// <PrimaryNoNewArray> ::= '(' <Expression> ')'
		// <ArrayAccess>
		// <RecordAccess>
		// <Name>
		StringList exprs = new StringList();
		if (exprToken.getType() == SymbolType.NON_TERMINAL) {
			Reduction exprRed = exprToken.asReduction();
			switch (exprRed.getParent().getTableIndex()) {
			case RuleConstants.PROD_ASSIGNMENT:
			{
				// <Assignment> ::= <LeftHandSide> <AssignmentOperator> <AssignmentExpression>
				exprs = decomposeExpression(exprRed.get(2), false, false);
				String value = exprs.get(exprs.count()-1);
				exprs.remove(exprs.count()-1);
				
				StringList lhs = decomposeExpression(exprRed.get(0), false, true);
				String target = lhs.get(lhs.count() -1);
				exprs.add(lhs.subSequence(0, lhs.count()-1));

				if (exprRed.get(1).asReduction().getParent().getTableIndex() == RuleConstants.PROD_ASSIGNMENTOPERATOR_EQ) {
					exprs.add(target + " <- " + value);
				}
				else {
					// Decompose the combined assignment
					String opr = getContent_R(exprRed.get(1)).trim();
					exprs.add(target + " <- " + target + " " +
							opr.substring(0, opr.length()-1) + " " + value);
				}
				if (!isStatement) {
					exprs.add(target);
				}
			}
			break;
				
			case RuleConstants.PROD_PREINCREMENTEXPRESSION_PLUSPLUS:
			case RuleConstants.PROD_PREDECREMENTEXPRESSION_MINUSMINUS:
			{
				// <PreIncrementExpression> ::= '++' <UnaryExpression>
				// <PreDecrementExpression> ::= '--' <UnaryExpression>
				StringList lhs = decomposeExpression(exprRed.get(1), false, true);
				String target = lhs.get(lhs.count() -1);
				String opr = exprRed.get(0).asString().substring(1);
				exprs.add(lhs.subSequence(0, lhs.count()-1));
				exprs.add(target + " <- " + target + " " + opr + " 1");
				if (!isStatement) {
					exprs.add(target);
				}
			}
			break;
			
			case RuleConstants.PROD_POSTINCREMENTEXPRESSION_PLUSPLUS:
			case RuleConstants.PROD_POSTDECREMENTEXPRESSION_MINUSMINUS:
			{
				// <PostIncrementExpression> ::= <PostfixExpression> '++'
				// <PostDecrementExpression> ::= <PostfixExpression> '--'
				StringList lhs = decomposeExpression(exprRed.get(0), false, true);
				String target = lhs.get(lhs.count() -1);
				String opr = exprRed.get(1).asString().substring(1);
				exprs.add(lhs.subSequence(0, lhs.count()-1));
				String tempName = "temp" + Integer.toHexString(exprRed.hashCode());
				exprs.add(tempName + " <- " + target);
				exprs.add(target + " <- " + target + " " + opr + " 1");
				if (!isStatement) {
					exprs.add(tempName);
				}
			}
			break;
			
			case RuleConstants.PROD_METHODINVOCATION_LPAREN_RPAREN:
			case RuleConstants.PROD_METHODINVOCATION_DOT_IDENTIFIER_LPAREN_RPAREN:
			case RuleConstants.PROD_METHODINVOCATION_SUPER_DOT_IDENTIFIER_LPAREN_RPAREN:
			case RuleConstants.PROD_CLASSINSTANCECREATIONEXPRESSION_NEW_LPAREN_RPAREN:
			{
				// <MethodInvocation> ::= <Name> '(' <ArgumentList> ')'
				// <MethodInvocation> ::= <Primary> '.' Identifier '(' <ArgumentList> ')'
				// <MethodInvocation> ::= super '.' Identifier '(' <ArgumentList> ')'
				// <ClassInstanceCreationExpression> ::= new <ClassType> '(' <ArgumentList> ')'
				StringList result = new StringList();
				for (int i = 0; i < exprRed.size() - 3; i++) {
					result.add(this.getContent_R(exprRed.get(i)));
				}
				Token argListToken = exprRed.get(exprRed.size()-2);
				processArguments(argListToken, result, exprs);
			}
			break;

			case RuleConstants.PROD_METHODINVOCATION_LPAREN_RPAREN2:
			case RuleConstants.PROD_METHODINVOCATION_SUPER_DOT_IDENTIFIER_LPAREN_RPAREN2:
			case RuleConstants.PROD_CLASSINSTANCECREATIONEXPRESSION_NEW_LPAREN_RPAREN2:
			{
				// <MethodInvocation> ::= <Name> '(' ')'
				// <MethodInvocation> ::= super '.' Identifier '(' ')'
				// <ClassInstanceCreationExpression> ::= new <ClassType> '(' ')'
				
				// Just leave it as is
				exprs.add(getContent_R(exprRed, ""));
			}
			break;
			
			case RuleConstants.PROD_METHODINVOCATION_DOT_IDENTIFIER_LPAREN_RPAREN2:
			{
				// <MethodInvocation> ::= <Primary> '.' Identifier '(' ')'
				exprs.add(decomposeExpression(exprRed.get(0), false, false));
				int ixLast = exprs.count()-1;
				exprs.set(ixLast, exprs.get(ixLast) + "." + getContent_R(exprRed.get(2)) + "()");
			}
			break;
			
			case RuleConstants.PROD_PRIMARYNONEWARRAY_LPAREN_RPAREN:
			{
				// <PrimaryNoNewArray> ::= '(' <Expression> ')'
				exprs.add(decomposeExpression(exprRed.get(1), false, false));
				int ixLast = exprs.count() - 1;
				exprs.set(ixLast, "(" + exprs.get(ixLast) + ")");
			}
			break;
			
			case RuleConstants.PROD_ARRAYACCESS_LBRACKET_RBRACKET:
			case RuleConstants.PROD_ARRAYACCESS_LBRACKET_RBRACKET2:
			{
				// <ArrayAccess> ::= <Name> '[' <Expression> ']'
				// <ArrayAccess> ::= <PrimaryNoNewArray> '[' <Expression> ']'
				
				exprs = decomposeExpression(exprRed.get(0), false, false);
				StringList indexExprs = decomposeExpression(exprRed.get(2), false, false);
				String target = exprs.get(exprs.count()-1);
				exprs.remove(exprs.count()-1);
				String index = indexExprs.get(indexExprs.count()-1);
				exprs.add(indexExprs.subSequence(0, indexExprs.count()-1));
				exprs.add(target + "[" + index + "]");
			}
			break;
			
			case RuleConstants.PROD_FIELDACCESS_DOT_IDENTIFIER:
			case RuleConstants.PROD_FIELDACCESS_SUPER_DOT_IDENTIFIER:
			{
				// <FieldAccess> ::= <Primary> '.' Identifier
				// <FieldAccess> ::= 'super' '.' Identifier
				exprs = decomposeExpression(exprRed.get(0), false, false);
				int ixLast = exprs.count()-1;
				exprs.set(ixLast, exprs.get(ixLast) + "."
						+ exprRed.get(2).asString().replace("c_l_a_s_s", "class"));
			}
			break;

			case RuleConstants.PROD_UNARYEXPRESSION_PLUS:
			case RuleConstants.PROD_UNARYEXPRESSION_MINUS:
			case RuleConstants.PROD_UNARYEXPRESSIONNOTPLUSMINUS_TILDE:
			case RuleConstants.PROD_UNARYEXPRESSIONNOTPLUSMINUS_EXCLAM:

				// <UnaryExpression> ::= '+' <UnaryExpression>
				// <UnaryExpression> ::= '-' <UnaryExpression>
				// <UnaryExpressionNotPlusMinus> ::= '~' <UnaryExpression>
				// <UnaryExpressionNotPlusMinus> ::= '!' <UnaryExpression>
			{
				String opr = exprRed.get(0).asString();
				exprs = decomposeExpression(exprRed.get(1), false, false);
				int ixLast = exprs.count()-1;
				exprs.set(ixLast, translateOperator(opr) + exprs.get(ixLast));
			}
			break;
			
			case RuleConstants.PROD_CONDITIONALEXPRESSION_QUESTION_COLON:
			{
				// <ConditionalExpression> ::= <ConditionalOrExpression> '?' <Expression> ':' <ConditionalExpression>
				// FIXME: Here we haven't a really good chance to replace it by a statement
				exprs = decomposeExpression(exprRed.get(0), false, false);
				StringList exprsTrue = decomposeExpression(exprRed.get(2), false, false);
				StringList exprsFalse = decomposeExpression(exprRed.get(4), false, false);
				String result = exprs.get(exprs.count()-1);
				exprs.remove(exprs.count()-1);
				exprs.add(exprsTrue.subSequence(0, exprsTrue.count()-1));
				exprs.add(exprsFalse.subSequence(0, exprsFalse.count()-1));
				exprs.add(result + " ? " + exprsTrue.get(exprsTrue.count()-1) + " : " + exprsFalse.get(exprsFalse.count()-1));
			}
			break;
			
			// Now we can hanöde all binary expressions the same way
			case RuleConstants.PROD_CONDITIONALOREXPRESSION_PIPEPIPE:
			case RuleConstants.PROD_CONDITIONALANDEXPRESSION_AMPAMP:
			case RuleConstants.PROD_INCLUSIVEOREXPRESSION_PIPE:
			case RuleConstants.PROD_EXCLUSIVEOREXPRESSION_CARET:
			case RuleConstants.PROD_ANDEXPRESSION_AMP:
			case RuleConstants.PROD_EQUALITYEXPRESSION_EQEQ:
			case RuleConstants.PROD_EQUALITYEXPRESSION_EXCLAMEQ:
			case RuleConstants.PROD_RELATIONALEXPRESSION_GT:
			case RuleConstants.PROD_RELATIONALEXPRESSION_GTEQ:
			case RuleConstants.PROD_RELATIONALEXPRESSION_LT:
			case RuleConstants.PROD_RELATIONALEXPRESSION_LTEQ:
			case RuleConstants.PROD_RELATIONALEXPRESSION_INSTANCEOF:
			case RuleConstants.PROD_SHIFTEXPRESSION_GTGT:
			case RuleConstants.PROD_SHIFTEXPRESSION_GTGTGT:
			case RuleConstants.PROD_SHIFTEXPRESSION_LTLT:
			case RuleConstants.PROD_ADDITIVEEXPRESSION_PLUS:
			case RuleConstants.PROD_ADDITIVEEXPRESSION_MINUS:
			case RuleConstants.PROD_MULTIPLICATIVEEXPRESSION_TIMES:
			case RuleConstants.PROD_MULTIPLICATIVEEXPRESSION_DIV:
			case RuleConstants.PROD_MULTIPLICATIVEEXPRESSION_PERCENT:
			{
				// <ConditionalOrExpression> ::= <ConditionalOrExpression> '||' <ConditionalAndExpression>
				// <ConditionalAndExpression> ::= <ConditionalAndExpression> '&&' <InclusiveOrExpression>
				// <InclusiveOrExpression> ::= <InclusiveOrExpression> '|' <ExclusiveOrExpression>
				// <ExclusiveOrExpression> ::= <ExclusiveOrExpression> '^' <AndExpression>
				// <AndExpression> ::= <AndExpression> '&' <EqualityExpression>
				// <EqualityExpression> ::= <EqualityExpression> '==' <RelationalExpression>
				// <EqualityExpression> ::= <EqualityExpression> '!=' <RelationalExpression>
				// <RelationalExpression> ::= <RelationalExpression> '>' <ShiftExpression>
				// <RelationalExpression> ::= <RelationalExpression> '>=' <ShiftExpression>
				// <RelationalExpression> ::= <RelationalExpression> '<' <ShiftExpression>
				// <RelationalExpression> ::= <RelationalExpression> '<=' <ShiftExpression>
				// <RelationalExpression> ::= <RelationalExpression> instanceof <ReferenceType>
				// <ShiftExpression> ::= <ShiftExpression> '>>' <AdditiveExpression>
				// <ShiftExpression> ::= <ShiftExpression> '>>>' <AdditiveExpression>
				// <ShiftExpression> ::= <ShiftExpression> '<<' <AdditiveExpression>
				// <AdditiveExpression> ::= <AdditiveExpression> '+' <MultiplicativeExpression>
				// <AdditiveExpression> ::= <AdditiveExpression> '-' <MultiplicativeExpression>
				// <MultiplicativeExpression> ::= <MultiplicativeExpression> '*' <UnaryExpression>
				// <MultiplicativeExpression> ::= <MultiplicativeExpression> '/' <UnaryExpression>
				// <MultiplicativeExpression> ::= <MultiplicativeExpression> '%' <UnaryExpression>
				String opr = translateOperator(exprRed.get(1).asString());
				exprs = decomposeExpression(exprRed.get(0), false, false);
				StringList exprsRight = decomposeExpression(exprRed.get(2), false, false);
				String left = exprs.get(exprs.count()-1);
				String right = exprsRight.get(exprsRight.count()-1);
				exprs.remove(exprs.count()-1);
				exprs.add(exprsRight.subSequence(0, exprsRight.count()-1));
				exprs.add(left + " " + opr + " " + right);
			}
			break;
			
			case RuleConstants.PROD_ARRAYCREATIONEXPRESSION_NEW:
			case RuleConstants.PROD_ARRAYCREATIONEXPRESSION_NEW2:
			case RuleConstants.PROD_ARRAYCREATIONEXPRESSION_NEW3:
			case RuleConstants.PROD_ARRAYCREATIONEXPRESSION_NEW4:
			{
				// <ArrayCreationExpression> ::= new <PrimitiveType> <DimExprs> <Dims>
				// <ArrayCreationExpression> ::= new <PrimitiveType> <DimExprs>
				// <ArrayCreationExpression> ::= new <ClassOrInterfaceType> <DimExprs> <Dims>
				// <ArrayCreationExpression> ::= new <ClassOrInterfaceType> <DimExprs>
				/* FIXME: Here it gets somewhat difficult to say what to do:
				 * We might assign an element with the highest index such that
				 * an array of the requested size gets created (and filled with 0),
				 * but this would hardly be helpful in case of multidimensional
				 * arrays.
				 * Hence, for now we leave it just as is
				 */
				exprs.add(getContent_R(exprToken));
			}
			break;
			
			case RuleConstants.PROD_ARRAYCREATIONEXPRESSION_NEW5:
			case RuleConstants.PROD_ARRAYCREATIONEXPRESSION_NEW6:
			{
				// <ArrayCreationExpression> ::= new <PrimitiveType> <Dims> <ArrayInitializer>
				// <ArrayCreationExpression> ::= new <ClassOrInterfaceType> <Dims> <ArrayInitializer>
				Reduction redInit = exprRed.get(exprRed.size()-1).asReduction();
				// PROD_ARRAYINITIALIZER_LBRACE_COMMA_RBRACE:
				//                     <ArrayInitializer> ::= '{' <VariableInitializers> ',' '}'
				// PROD_ARRAYINITIALIZER_LBRACE_RBRACE:
				//                     <ArrayInitializer> ::= '{' <VariableInitializers> '}'
				// PROD_ARRAYINITIALIZER_LBRACE_COMMA_RBRACE2:
				//                     <ArrayInitializer> ::= '{' ',' '}'
				// PROD_ARRAYINITIALIZER_LBRACE_RBRACE2:
				//                     <ArrayInitializer> ::= '{' '}'
				Stack<StringList> initExprs = new Stack<StringList>();
				if (redInit.getParent().getTableIndex() <= RuleConstants.PROD_ARRAYINITIALIZER_LBRACE_RBRACE) {
					Token tokenVals = redInit.get(1);
					// PROD_VARIABLEINITIALIZERS:       <VariableInitializers> ::= <VariableInitializer>
					// PROD_VARIABLEINITIALIZERS_COMMA: <VariableInitializers> ::= <VariableInitializers> ',' <VariableInitializer>
					do {
						Token tokenVal = tokenVals;
						if (tokenVals.asReduction() != null
								&& (tokenVals).asReduction().getParent().getTableIndex() == RuleConstants.PROD_VARIABLEINITIALIZERS_COMMA) {
							tokenVal = tokenVals.asReduction().get(2);
							tokenVals = tokenVals.asReduction().get(0);
						}
						else {
							tokenVals = null;
						}
						initExprs.push(this.decomposeExpression(tokenVal, false, false));
					} while (tokenVals != null);
				}
				StringList result = StringList.getNew("{");
				while (!initExprs.isEmpty()) {
					StringList valExprs = initExprs.pop();
					int ixLast = valExprs.count()-1;
					exprs.add(valExprs.subSequence(0, ixLast));
					result.add(valExprs.get(ixLast));
					result.add(", ");
				}
				if (result.get(result.count()-1).equals(", ")) {
					result.set(result.count()-1, "}");
				}
				else {
					result.add("}");
				}
				exprs.add(result.concatenate());
			}
			break;
			
			case RuleConstants.PROD_CASTEXPRESSION_LPAREN_RPAREN:
			case RuleConstants.PROD_CASTEXPRESSION_LPAREN_RPAREN2:
			case RuleConstants.PROD_CASTEXPRESSION_LPAREN_RPAREN3:
			case RuleConstants.PROD_CASTEXPRESSION_LPAREN_RPAREN4:
			{
				// <CastExpression> ::= '(' <PrimitiveType> <Dims> ')' <UnaryExpression>
				// <CastExpression> ::= '(' <PrimitiveType> ')' <UnaryExpression>
				// <CastExpression> ::= '(' <Expression> ')' <UnaryExpressionNotPlusMinus>
				// <CastExpression> ::= '(' <Name> <Dims> ')' <UnaryExpressionNotPlusMinus>

				/* We don't have a sensible casting mechanism in Structorizer.
				 * Just leave as is in a way
				 */
				String cast = "(" + this.getContent_R(exprRed.get(1));
				if (exprRed.size() > 4) {
					cast += this.getContent_R(exprRed.get(2));
				}
				cast += ")";
				exprs.add(this.decomposeExpression(exprRed.get(exprRed.size()-1), false, false));
				int ixLast = exprs.count()-1;
				exprs.set(ixLast, cast + exprs.get(ixLast));
			}
			break;
				
			default:
				exprs.add(this.getContent_R(exprToken));
			
			}
			
		}
		else {
			exprs.add(exprToken.asString());
		}
		return exprs;
	}

	/**
	 * Helper method for the expression conversion
	 * @param argListToken - the {@link Token} representing the list of method call
	 *      arguments (inside the parentheses, rule &lt;ArgeumentList&gt;).
	 * @param prefix - A {@link StringList} representing the tokens of the method
	 *      reference (e.g. a qualified name) 
	 * @param exprs - A {@link StringList} to add the preparatory assignments and the
	 *      resulting method call to.
	 * @throws ParserCancelled
	 */
	private void processArguments(Token argListToken, StringList prefix, StringList exprs) throws ParserCancelled {
		Stack<StringList> argExprs = new Stack<StringList>();
		do {
			Token argToken = argListToken;
			Reduction argListRed = argListToken.asReduction();
			// PROD_ARGUMENTLIST:       <ArgumentList> ::= <Expression>
			// PROD_ARGUMENTLIST_COMMA: <ArgumentList> ::= <ArgumentList> ',' <Expression>
			if (argListRed != null
					&& argListRed.getParent().getTableIndex() == RuleConstants.PROD_ARGUMENTLIST_COMMA) {
				argToken = argListRed.get(2);
				argListToken = argListRed.get(0);
			}
			else {
				argListToken = null;
			}
			argExprs.push(decomposeExpression(argToken, false, false));
		} while (argListToken != null);
		prefix.add("(");
		while (!argExprs.isEmpty()) {
			StringList arg = argExprs.pop();
			int ixLast = arg.count() - 1;
			// All but the last elements are the preparation assignments
			exprs.add(arg.subSequence(0, ixLast));
			// The last element is the assignable value
			prefix.add(arg.get(ixLast));
			prefix.add(", ");
		}
		// Replace the last (redundant) comma by the closing parenthesis
		prefix.set(prefix.count()-1, ")");
		exprs.add(prefix.concatenate());
	}

	/**
	 * Extracts the method header from {@link Reduction} {@code _reduction}, prepares
	 * it for Structorizer and applies the information to {@code _subRoot}.<br/>
	 * The method comment is assumed to have been attached already.
	 * @param asReduction - the {@code <MethodHeader>} reduction
	 * @param subRoot - the method diagram to be equipped with a header
	 * @returns {@code true} if the method represents either the "main" method;
	 *       {@code false} otherwise
	 * @throws ParserCancelled 
	 */
	private boolean applyMethodHeader(Reduction _reduction, Root _subRoot) throws ParserCancelled
	{
		// PROD_METHODHEADER:       <MethodHeader> ::= <Modifiers> <Type> <MethodDeclarator> <Throws>
		// PROD_METHODHEADER2:      <MethodHeader> ::= <Modifiers> <Type> <MethodDeclarator>
		// PROD_METHODHEADER3:      <MethodHeader> ::= <Type> <MethodDeclarator> <Throws>
		// PROD_METHODHEADER4:      <MethodHeader> ::= <Type> <MethodDeclarator>
		// PROD_METHODHEADER_VOID:  <MethodHeader> ::= <Modifiers> void <MethodDeclarator> <Throws>
		// PROD_METHODHEADER_VOID2: <MethodHeader> ::= <Modifiers> void <MethodDeclarator>
		// PROD_METHODHEADER_VOID3: <MethodHeader> ::= void <MethodDeclarator> <Throws>
		// PROD_METHODHEADER_VOID4: <MethodHeader> ::= void <MethodDeclarator>
		// PROD_CONSTRUCTORDECLARATOR_LPAREN_RPAREN:  <ConstructorDeclarator> ::= <SimpleName> '(' <FormalParameterList> ')'
		// PROD_CONSTRUCTORDECLARATOR_LPAREN_RPAREN2: <ConstructorDeclarator> ::= <SimpleName> '(' ')'
		int ixType = 0;
		int ruleId = _reduction.getParent().getTableIndex();
		if (ruleId == RuleConstants.PROD_METHODHEADER
				|| ruleId == RuleConstants.PROD_METHODHEADER2
				|| ruleId == RuleConstants.PROD_METHODHEADER_VOID
				|| ruleId == RuleConstants.PROD_METHODHEADER_VOID2) {
			// Add the modifiers to the comment
			String modifiers = this.getContent_R(_reduction.get(0));
			_subRoot.comment.add(modifiers);
			ixType++;
		}
		String resultType = null;	// Constructor
		if (ruleId != RuleConstants.PROD_CONSTRUCTORDECLARATOR_LPAREN_RPAREN
				&& ruleId != RuleConstants.PROD_CONSTRUCTORDECLARATOR_LPAREN_RPAREN2) {
			resultType = translateType(_reduction.get(ixType));
		}
		if (resultType != null && _reduction.size() > ixType + 2) {
			// Add the throws clause to the comment
			_subRoot.comment.add("==== " + this.getContent_R(_reduction.get(ixType + 2)));
		}
		// get the name and arguments
		Reduction redDecl = _reduction;	// In case of a constructor, this is already what we need
		if (resultType != null) {
			redDecl = _reduction.get(ixType + 1).asReduction();
			ruleId = redDecl.getParent().getTableIndex();
		}
		// PROD_METHODDECLARATOR_IDENTIFIER_LPAREN_RPAREN:
		//          <MethodDeclarator> ::= Identifier '(' <FormalParameterList> ')'
		// PROD_METHODDECLARATOR_IDENTIFIER_LPAREN_RPAREN2:
		//          <MethodDeclarator> ::= Identifier '(' ')'
		// PROD_METHODDECLARATOR_IDENTIFIER_LPAREN_RPAREN3:
		//          <MethodDeclarator> ::= Identifier '(' <FormalParameterList> ')' <Dims>
		// PROD_METHODDECLARATOR_IDENTIFIER_LPAREN_RPAREN4:
		//          <MethodDeclarator> ::= Identifier '(' ')' <Dims>
		// PROD_CONSTRUCTORDECLARATOR_LPAREN_RPAREN:
		//          <ConstructorDeclarator> ::= <SimpleName> '(' <FormalParameterList> ')'
		// PROD_CONSTRUCTORDECLARATOR_LPAREN_RPAREN2:
		//          <ConstructorDeclarator> ::= <SimpleName> '(' ')'
		if (ruleId == RuleConstants.PROD_METHODDECLARATOR_IDENTIFIER_LPAREN_RPAREN3
				|| ruleId == RuleConstants.PROD_METHODDECLARATOR_IDENTIFIER_LPAREN_RPAREN4) {
			// Add these brackets to the type
			String extraDims = this.getContent_R(redDecl.get(1));
			if (resultType != null) {
				resultType += extraDims;
			}
		}
		String name = getContent_R(redDecl.get(0));
		StringList argList = new StringList();
		if (ruleId == RuleConstants.PROD_METHODDECLARATOR_IDENTIFIER_LPAREN_RPAREN
				|| ruleId == RuleConstants.PROD_METHODDECLARATOR_IDENTIFIER_LPAREN_RPAREN3
				|| ruleId == RuleConstants.PROD_CONSTRUCTORDECLARATOR_LPAREN_RPAREN) {
			// There are arguments - so retrieve them
			addFormalParameters(redDecl.get(2).asReduction(), argList);
		}
		if (name.equals("main") && argList.count() == 1) {
			_subRoot.setText(name);
			_subRoot.setProgram(true);
		}
		else {
			// TODO use a syntax-preference-specific separator
			String header = name + "(" + argList.concatenate("; ") + ")";
			if (resultType != null && !resultType.equals("void")) {
				header += ": " + resultType;
			}
			_subRoot.setText(header);
			_subRoot.setProgram(false);
		}
		return name.equals("main");
	}

	/**
	 * Converts the formal parameter list and appends the formal parameter declarations
	 * to the {@link StringList} {@code argList}
	 * @param redFPL - the {@link Reduction} representing the formal parameter list
	 * @param argList - A {@link StringList} the argument declarations are to be added to
	 * @throws ParserCancelled if the user aborted the import
	 */
	private void addFormalParameters(Reduction redFPL, StringList argList) throws ParserCancelled {
		// PROD_FORMALPARAMETERLIST:       <FormalParameterList> ::= <FormalParameter>
		// PROD_FORMALPARAMETERLIST_COMMA: <FormalParameterList> ::= <FormalParameterList> ',' <FormalParameter>
		do {
			Reduction redFP = redFPL;
			if (redFPL.getParent().getTableIndex() == RuleConstants.PROD_FORMALPARAMETERLIST_COMMA) {
				redFP = redFPL.get(2).asReduction();
				redFPL = redFPL.get(0).asReduction();
			}
			else {
				redFPL = null;
			}
			String[] param = getFormalParameter(redFP);
			argList.insert(composeDeclarationString(param), 0);
		} while (redFPL != null);
	}
	
	/**
	 * Composes a declaration string from the given declaration components
	 * @param declComponents - an array of [0] name, [1] type description, [2] "const" or ""
	 * @return the composed declaration according to the syntax preferences
	 */
	String composeDeclarationString(String[] declComponents) {
		// TODO provisionally we just return a Pascal-like form
		String decl = declComponents[2] + " " + declComponents[0] + ": " + declComponents[1];
		return decl.trim();
	}

	/**
	 * Derive a single formal parameter from the given {@link Reduction} and returns a
	 * suited Strung describing its declaration in a Structorizer-compatible way.
	 * @param redFP - the {@link Reduction} to be analysed
	 * @return an array of [0] name, [1] type description, and [2] "const" (if to be applied)
	 * @throws ParserCancelled 
	 */
	private String[] getFormalParameter(Reduction redFP) throws ParserCancelled {
		// PROD_FORMALPARAMETER:        <FormalParameter> ::= <Type> <VariableDeclaratorId>
		// PROD_FORMALPARAMETER_FINAL:  <FormalParameter> ::= final <Type> <VariableDeclaratorId>
		// PROD_RECEIVERPARAMETER_THIS: <ReceiverParameter> ::= <Type> <QualPrefixOpt> this
		// PROD_LASTFORMALPARAMETER_Ellipsis: <LastFormalParameter> ::= <Type> <Annotations> '...' <VariableDeclaratorId>
		int ruleId = redFP.getParent().getTableIndex();
		int ixType = 0;
		int ixVar = 1;
		boolean isConst = ruleId == RuleConstants.PROD_FORMALPARAMETER_FINAL;
		if (isConst) {
			ixType++;
			ixVar++;
		}
		else if(ruleId == RuleConstants.PROD_LASTFORMALPARAMETER_ELLIPSIS) {
			ixVar = 3;
		}
		String type = this.translateType(redFP.get(ixType));
		String varId = this.getContent_R(redFP.get(ixVar));
		if (ixVar == 3) {
			/* The ellipsis is practically the same as an array dimension
			 * ... unless Structorizer allows variable argument number as well some day
			 */
			type += "[]";
		}
		return new String[] {
				varId,
				type,
				isConst ? "const" : ""
		};
	}

	/**
	 * Returns processed variable declarations as StringList
	 * @param token - {@link Token} representing <VariableDeclarators> 
	 * @param typeDescr TODO
	 * @param asConst TODO
	 * @return {@link StringList} of variable declarations in order of occurrence
	 * @throws ParserCancelled 
	 */
	private StringList processVarDeclarators(Token token, String typeDescr, boolean asConst) throws ParserCancelled {
		// <VariableDeclarators> ::= <VariableDeclarators> ',' <VariableDeclarator>
		// <VariableDeclarators> ::= <VariableDeclarator>
		StringList declarators = new StringList();
		do {
			Token declToken = token;
			if (token.getType() == SymbolType.NON_TERMINAL
					&& token.asReduction().getParent().getTableIndex() == RuleConstants.PROD_VARIABLEDECLARATORS_COMMA) {
				declToken = token.asReduction().get(2);
				token = token.asReduction().get(0);
			}
			else {
				token = null;
			}
			Token declToken0 = declToken;
			String valStr = null;
			if (declToken.asReduction() != null
					&& declToken.asReduction().getParent().getTableIndex() == RuleConstants.PROD_VARIABLEDECLARATOR_EQ) {
				declToken0 = declToken.asReduction().get(0);
				valStr = this.translateContent(this.getContent_R(declToken.asReduction().get(2)));
			}
			String var = this.getContent_R(declToken0);
			// FIXME: Consider desired syntax
			if (typeDescr != null) {
				if (optionTranslate) {
					var += ": " + typeDescr;
				}
				else {
					var = typeDescr + " " + var;
				}
			}
			if (asConst) {
				var = "const " + var;
			}
			else if (optionTranslate) {
				var = "var " + var;
			}
			if (valStr != null) {
				var += " <- " + valStr;
			}
			declarators.add(var);
		} while (token != null);
		return declarators.reverse();
	}

	/**
	 * Checks the three header zones (given by the {@link Token}s) of a C {@code for} loop being
	 * imported and decides by means of methods {@link #checkForInit(Token, String)},
	 * {@link #checkForCond(Token, String, boolean)}, and {@link #checkForIncr(Token)}
	 * whether a Structorizer {@link For} loop may be formed from them. If so returns the {@link For}
	 * element for further processing, otherwise returns {@code null}.<br/>
	 * Subclasses must overwrite methods {@link #checkForInit(Token, String)},
	 * {@link #checkForCond(Token, String, boolean)}, and {@link #checkForIncr(Token)} in a
	 * grammar-dependent way. The versions of {@link CPreParser} return just null and thus avert
	 * the generation of a {@link For} element.
	 * @param initToken - {@link Token} representing the first header zone 
	 * @param condToken - {@link Token} representing the second header zone
	 * @param incrToken - {@link Token} representing the third header zone
	 * @return either a {@link For} element or null.
	 * @see #checkForInit(Token, String)
	 * @see #checkForCond(Token, String, boolean)
	 * @see #checkForIncr(Token)
	 */
	protected final For checkAndMakeFor(Token initToken, Token condToken, Token incrToken) throws ParserCancelled
	{
		For loop = null;
		String[] idAndIncr = this.checkForIncr(incrToken);
		if (idAndIncr != null) {
			String id = idAndIncr[0];
			String last = this.checkForCond(condToken, id, idAndIncr[1].equals("+"));
			if (last != null) {
				String first = this.checkForInit(initToken, id);
				if (first != null) {
					int incr = Integer.parseUnsignedInt(idAndIncr[2]);
					if (idAndIncr[1].equals("-")) {
						incr = -incr;
					}
					loop = new For(id, first, last, incr);
				}
			}
		}
		return loop;
	}

	/**
	 * Checks the increment zone of a Java {@code for} loop given by {@link #Token} {@code incrToken} 
	 * whether the instruction is suited for a Structorizer FOR loop.<br/>
	 * This is assumed in exactly the following cases:<br/>
	 * 1. {@code <id>++}, {@code ++<id>}, {@code <id> += <intlit>}, or {@code <id> = <id> + <intlit>}<br/>  
	 * 2. {@code <id>--}, {@code --<id>}, {@code <id> -= <intlit>}, or {@code <id> = <id> - <intlit>}  
	 * @param incrToken - the token representing the third zone of a {@code for} loop header
	 * @return null or a string array of: [0] the id, [1] '+' or '-', [2] the int literal of the increment/decrement
	 * @see #checkAndMakeFor(Token, Token, Token)
	 * @see #checkForCond(Token, String, boolean)
	 * @see #checkForInit(Token, String)
	 */
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
				case RuleConstants.PROD_POSTINCREMENTEXPRESSION_PLUSPLUS:
				case RuleConstants.PROD_POSTDECREMENTEXPRESSION_MINUSMINUS:
					nxtToken = incrRed.get(0);
					if (nxtToken.getType() == SymbolType.NON_TERMINAL && nxtToken.asReduction().getParent().getTableIndex() == RuleConstants.PROD_SIMPLENAME_IDENTIFIER) {
						parts = new String[3];
						parts[0] = nxtToken.asReduction().get(0).asString();
						parts[1] = (ruleId == RuleConstants.PROD_POSTINCREMENTEXPRESSION_PLUSPLUS) ? "+" : "-";
						parts[2] = "1";
					}
					break;
				case RuleConstants.PROD_PREINCREMENTEXPRESSION_PLUSPLUS:
				case RuleConstants.PROD_PREDECREMENTEXPRESSION_MINUSMINUS:
					nxtToken = incrRed.get(1);
					if (nxtToken.getType() == SymbolType.NON_TERMINAL && nxtToken.asReduction().getParent().getTableIndex() == RuleConstants.PROD_SIMPLENAME_IDENTIFIER) {
						parts = new String[3];
						parts[0] = nxtToken.asReduction().get(0).asString();
						parts[1] = (ruleId == RuleConstants.PROD_PREINCREMENTEXPRESSION_PLUSPLUS) ? "+" : "-";
						parts[2] = "1";
					}
					break;
				case RuleConstants.PROD_ASSIGNMENT:
					// <Assignment> ::= <LeftHandSide> <AssignmentOperator> <AssignmentExpression>
					// The first part must be an id
					nxtToken = incrRed.get(0);
					if (nxtToken.getType() == SymbolType.NON_TERMINAL 
							&& nxtToken.asReduction().getParent().getTableIndex() == RuleConstants.PROD_SIMPLENAME_IDENTIFIER) {
						parts = new String[3];
						parts[0] = nxtToken.asReduction().get(0).asString();
						// Now identify the operator
						nxtToken = incrRed.get(1);
						switch (nxtToken.asReduction().getParent().getTableIndex()) {
						case RuleConstants.PROD_ASSIGNMENTOPERATOR_EQ: 
						{
							// Now we must have an addition or subtraction on the right side
							// and its first operand must be identical to parts[0]
							Reduction rightRed = incrRed.get(2).asReduction();
							ruleId = rightRed.getParent().getTableIndex();
							if ((ruleId == RuleConstants.PROD_ADDITIVEEXPRESSION_PLUS || ruleId == RuleConstants.PROD_ADDITIVEEXPRESSION_MINUS)
									&& this.getContent_R(rightRed.get(0)).trim().equals(parts[0])) {
								parts[1] = (ruleId == RuleConstants.PROD_ADDITIVEEXPRESSION_PLUS) ? "+" : "-";
								nxtToken = rightRed.get(2);
							}
							else {
								parts = null;
							}
						} // case RuleConstants.PROD_ASSIGNOP_EQ
						break;
						case RuleConstants.PROD_ASSIGNMENTOPERATOR_PLUSEQ:
							parts[1] = "+";
							nxtToken = incrRed.get(2);
							break;
						case RuleConstants.PROD_ASSIGNMENTOPERATOR_MINUSEQ:
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

	/**
	 * Checks the condition zone of a Java {@code for} loop given by {@link #Token} {@code incrToken} 
	 * whether the expression is suited for a Structorizer FOR loop.<br/>
	 * Only the following cases are accepted where the expression {@code <expr>} must not contain
	 * any comparison or logical operator like {@code &&}, {@code ||}, and {@code !}:<br/>
	 * 1. {@code <id> < <expr>} or {@code <id> <= <expr>} where {@code <id>} is the given {@code <id>}
	 * and {@code upward} is true<br/>  
	 * 2. {@code <id> > <expr>} or {@code <id> >= <expr>} where {@code <id>} is the given {@code <id>}
	 * and {@code upward} is false  
	 * @param condToken - the token representing the second zone of a {@code for} loop header
	 * @param id - the expected loop variable id to be tested
	 * @param upward - whether there is an increment or decrement
	 * @return the end value of the Structorizer counting FOR loop if suited, null otherwise
	 * @see #checkAndMakeFor(Token, Token, Token)
	 * @see #checkForIncr(Token)
	 * @see #checkForInit(Token, String)
	 */
	protected String checkForCond(Token condToken, String id, boolean upward) throws ParserCancelled
	{
		String lastVal = null;
		if (condToken.getType() == SymbolType.NON_TERMINAL) {
			// Should be some kind of <ExprOpt>
			Reduction condRed = condToken.asReduction();
			int ruleId = condRed.getParent().getTableIndex();
			if ((upward && (ruleId == RuleConstants.PROD_RELATIONALEXPRESSION_LT || ruleId == RuleConstants.PROD_RELATIONALEXPRESSION_LTEQ)
					|| !upward && (ruleId == RuleConstants.PROD_RELATIONALEXPRESSION_GT || ruleId == RuleConstants.PROD_RELATIONALEXPRESSION_GTEQ))
					&& this.getContent_R(condRed.get(0).asReduction(), "").trim().equals(id)) {
				lastVal = this.getContent_R(condRed.get(2).asReduction(), "");
				if (ruleId == RuleConstants.PROD_RELATIONALEXPRESSION_LT) {
					lastVal += " - 1";
				}
				else if (ruleId == RuleConstants.PROD_RELATIONALEXPRESSION_GT) {
					lastVal += " + 1";
				}
			}
		}		
		return lastVal;
	}
	
	/**
	 * Checks the initialization zone of a Java {@code for} loop given by {@link #Token} {@code initToken} 
	 * whether the statement is suited for a Structorizer FOR loop.<br/>
	 * Only the following cases are accepted where the expression {@code <expr>} must not be composed
	 * of several instructions:<br/>
	 * 1. {@code <id> = <expr>} or<br/>
	 * 2. {@code <type> <id> = <expr>}<br/>
	 * where {@code <id>} is the given {@code <id>}. 
	 * @param initToken - the token representing the first zone of a {@code for} loop header
	 * @param id - the expected loop variable id to be tested
	 * @return the start value expression or null if the {@code id} doesn't match or the staeement isn't suited
	 * @see #checkAndMakeFor(Token, Token, Token)
	 * @see #checkForIncr(Token)
	 * @see #checkForCond(Token, String, boolean)
	 */
	protected String checkForInit(Token initToken, String id) throws ParserCancelled
	{
		String firstVal = null;
		Reduction initRed = null;
		if (initToken.getType() == SymbolType.NON_TERMINAL && (initRed = initToken.asReduction()).size() > 0) {
			// Now there are two cases: <ExprOpt> or <Declarator>, first we try <ExprOpt>
			int ruleId = initRed.getParent().getTableIndex();
			if (ruleId == RuleConstants.PROD_ASSIGNMENT
					&& initRed.get(1).asReduction().getParent().getTableIndex() == RuleConstants.PROD_ASSIGNMENTOPERATOR_EQ) {
				if (this.getContent_R(initRed.get(0).asReduction(), "").trim().equals(id)) {
					firstVal = this.getContent_R(initRed.get(2).asReduction(), "");
				}
			}
			else if (ruleId == RuleConstants.PROD_LOCALVARIABLEDECLARATION_FINAL || ruleId == RuleConstants.PROD_LOCALVARIABLEDECLARATION) {
				int ixDecl = (ruleId == RuleConstants.PROD_LOCALVARIABLEDECLARATION_FINAL) ? 2 : 1;
				if ((ruleId = (initRed = initRed.get(ixDecl).asReduction()).getParent().getTableIndex()) != RuleConstants.PROD_VARIABLEDECLARATORS_COMMA){
					if (ruleId == RuleConstants.PROD_VARIABLEDECLARATOR_EQ &&
							id.equals(this.getContent_R(initRed.get(0)).trim())) {
						firstVal = this.getContent_R(initRed.get(2));
					}
				}
				//this.analyseDeclaration(_reduction, _pascalType, _parentNode, _forceDecl, _something)
			}
		}
		return firstVal;
	}

	/**
	 * Converts a rule of type PROD_SWITCHSTATEMENT_SWITCH_LPAREN_RPAREN into the
	 * skeleton of a Case element. The case branches will be handled separately
	 * @param _reduction - Reduction rule of a switch instruction
	 * @param _parentNode - the Subqueue this Case element is to be appended to
	 * @throws ParserCancelled 
	 */
	private void buildCase(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled
	{
		// <SwitchStatement> ::= switch '(' <Expression> ')' <SwitchBlock>
		// Put the discriminator into the first line of content
		String content = translateContent(getContent_R(_reduction.get(2)));

		// How many branches has the CASE element? We must count the non-empty statement lists!
		Reduction sr = _reduction.get(4).asReduction();
		int ruleId = sr.getParent().getTableIndex();
		if (ruleId == RuleConstants.PROD_SWITCHBLOCK_LBRACE_RBRACE4 ||
				ruleId == RuleConstants.PROD_SWITCHBLOCK_LBRACE_RBRACE3) {
			// <SwitchBlock> ::= '{' '}'
			/* Empty switch block! This means the only possible purpose is a side effect
			 * of the discriminator evaluation! So we might produce a dummy assignment
			 */
			String var = "switch" + Integer.toHexString(_reduction.hashCode());
			Instruction instr = new Instruction(var + " <- " + content);
			this.equipWithSourceComment(instr, _reduction);
			instr.getComment().add("This was a switch instruction with empty body!");
			instr.setColor(colorMisc);
			_parentNode.addElement(instr);
			return;
		}
		// Now we have one of:
		// <SwitchBlock> ::= '{' <SwitchBlockStatementGroups> <SwitchLabels> '}'
		// <SwitchBlock> ::= '{' <SwitchBlockStatementGroups> '}'
		content = getOptKeyword("preCase", false, true)
				+ content
				+ getOptKeyword("postCase", true, false).trim();
		StringList text = StringList.getNew(content);
		Stack<Subqueue> branches = new Stack<Subqueue>();
		// Add some pro-forma branch
		text.add("%");
		branches.add(new Subqueue());
		boolean hasDefault = false;
		if (ruleId == RuleConstants.PROD_SWITCHBLOCK_LBRACE_RBRACE) {
			// <SwitchBlock> ::= '{' <SwitchBlockStatementGroups> <SwitchLabels> '}'
			// Get the trailing switch labels (there is an empty branch, though)
			StringList tailLabels = extractCaseLabels(sr.get(2).asReduction());
			hasDefault = tailLabels.contains("default");
			if (hasDefault) {
				text.set(1, "default");
				tailLabels.clear();	// The other labels are necessarily redundant
			}
			if (!tailLabels.isEmpty()) {
				// This is an unnecessary empty branch, but let it be
				text.insert(tailLabels.concatenate(", "), 1);
				branches.add(new Subqueue());
			}
		}
		sr = sr.get(1).asReduction();
		ruleId = sr.getParent().getTableIndex();
		// Now we retrieve the branches from last to first
		while (ruleId == RuleConstants.PROD_SWITCHBLOCKSTATEMENTGROUPS2)
		{
			// <SwitchBlockStatementGroups> ::= <SwitchBlockStatementGroups> <SwitchBlockStatementGroup>
			hasDefault = addCaseBranch(sr.get(1).asReduction(), text, branches, hasDefault);
			sr = sr.get(0).asReduction();
			ruleId = sr.getParent().getTableIndex();
		}
		addCaseBranch(sr, text, branches, hasDefault);

		Case ele = new Case(text);
		this.equipWithSourceComment(ele, _reduction);
		_parentNode.addElement(ele);

		/* Now adjust the case branches
		 * In theory, all branches should end with a break instruction
		 * unless they end with return or exit. Drop the break instructions
		 * (and only these) now.
		 * If there is non of them, however, then we must append a copy of
		 * the following branch(es).
		 */
		int ixAppendTo = ele.qs.size();
		for (int i = 0; i < ele.qs.size() && !branches.isEmpty(); i++) {
			Subqueue sq = branches.pop();
			ele.qs.set(i, sq);
			sq.parent = ele;
			int size = sq.getSize();
			if (size > 0) {
				Element el = sq.getElement(size-1);
				boolean jumps = el instanceof Jump;
				if (jumps && ((Jump)el).isLeave()) {
					// remove the break instruction! All others remain
					sq.removeElement(--size);
				}
				// In case we had unclosed previous branches copy the content
				for (int j = ixAppendTo; j < i; j++) {
					for (int k = 0; k < size; k++) {
						ele.qs.get(j).addElement(sq.getElement(k).copy());
					}
				}
				if (jumps) {
					// Disable copy
					ixAppendTo = ele.qs.size();
				}
				else if (ixAppendTo > i) {
					// Prepare copy for next branches
					ixAppendTo = i;
				}
			}
		}
	}

	/**
	 * Evaluates the case block represented by the passed in reduction {@code stmGroup},
	 * inserts the labels into {@code text} and pushes the block onto {@code branches}.
	 * @param stmGroup - {@link Reduction} representing a <SwitchBlockStatementGroup> rule
	 * @param text - The Case text in progress - new entries are to be inserted at position 1
	 * @param branches - a stack of provisional {@link Subqueues} to form the branches
	 * @param hasDefault - whether there had been an (empty) default branch
	 * @return whether there is a default branch now
	 * @throws ParserCancelled if the user has aborted the import
	 */
	private boolean addCaseBranch(Reduction stmGroup, StringList text, Stack<Subqueue> branches, boolean hasDefault)
			throws ParserCancelled {
		// <SwitchBlockStatementGroup> ::= <SwitchLabels> <BlockStatements>
		StringList caseLabels = extractCaseLabels(stmGroup.get(0).asReduction());
		if (!hasDefault && branches.size() == 1 
				&& (hasDefault = caseLabels.contains("default"))) {
			// Replace the "%" line
			text.set(1, "default");
			caseLabels.clear();
		}
		else {
			caseLabels.removeAll("default");	// Just in case
			if (!caseLabels.isEmpty()) {
				text.insert(caseLabels.concatenate(", "), 1);
			}
			else {
				text.insert("???", 1);
			}
			branches.push(new Subqueue());
		}
		this.buildNSD_R(stmGroup.get(1).asReduction(), branches.peek());
		return hasDefault;
	}

	/**
	 * Extracts the case labels from the given {@code token} that is representing
	 * a {@code <SwitchLabels>} node or sub-node.
	 * @param token - represents a single {@code <SwitchLabel>} or a {@code <SwitchLabels>}
	 *    sequence
	 * @return a {@link StringList} of the pure case selectors (without "case" and colon)
	 * @throws ParserCancelled 
	 */
	private StringList extractCaseLabels(Reduction redLabels) throws ParserCancelled {
		// PROD_SWITCHLABELS:  <SwitchLabels> ::= <SwitchLabel>
		// PROD_SWITCHLABELS2: <SwitchLabels> ::= <SwitchLabels> <SwitchLabel>
		StringList labels = new StringList();
		while (redLabels != null) {
			Reduction redLabel1 = redLabels;
			if (redLabels.getParent().getTableIndex() == RuleConstants.PROD_SWITCHLABELS2) {
				redLabel1 = redLabels.get(1).asReduction();
				redLabels = redLabels.get(0).asReduction();
			}
			else {
				redLabels = null;
			}
			// PROD_SWITCHLABEL_CASE_COLON:    <SwitchLabel> ::= case <ConstantExpression> ':'
			// PROD_SWITCHLABEL_DEFAULT_COLON: <SwitchLabel> ::= default ':'
			if (redLabel1.getParent().getTableIndex() == RuleConstants.PROD_SWITCHLABEL_DEFAULT_COLON) {
				labels.add("default");
			}
			else {
				labels.add(this.translateContent(this.getContent_R(redLabel1.get(1))));
			}
		}
		return labels.reverse();
	}

	/**
	 * @param token
	 * @return
	 * @throws ParserCancelled 
	 */
	private String translateType(Token token) throws ParserCancelled {
		// TODO Auto-generated method stub
		return getContent_R(token);
	}
	
	private String translateOperator(String opr)
	{
		// We will always translate the operators - as they can be displayed in C mode.
		if (operatorMap.containsKey(opr)) {
			opr = operatorMap.get(opr);
		}
		return opr;
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
		StringList outputTokens = StringList.explodeWithDelimiter("System.out.println", ".");
		// An input conversion is not feasible.
		//String input = getKeyword("input");
		
		StringList tokens = Element.splitLexically(_content, true);
		int ix = -1;
		while ((ix = tokens.indexOf(outputTokens, 0, true)) >= 0) {
			tokens.remove(ix, ix + outputTokens.count());
			tokens.insert(output, ix);
		}
		
		tokens.removeAll(StringList.explodeWithDelimiter("Math.", "."), true);

		return _content.trim();
	}
	
	/**
	 * Convenience method for the string content retrieval from a {@link Token}
	 * that may be either represent a content symbol or a {@link Reduction}.
	 * @param _token - the {@link Token} the content is to be appended to
	 *        {@code _content}
	 * @return the content string (may be empty in case of noise)
	 * @throws ParserCancelled
	 */
	private String getContent_R(Token _token) throws ParserCancelled
	{
		if (_token.getType() == SymbolType.NON_TERMINAL) {
			return getContent_R(_token.asReduction(), "");
		}
		else if (_token.getType() == SymbolType.CONTENT) {
			return _token.asString();
		}
		return "";
	}
	
	@Override
	protected String getContent_R(Reduction _reduction, String _content) throws ParserCancelled
	{
		// START KGU#537 2018-07-01: Enh. #553
		checkCancelled();
		// END KGU#537 2018-07-01
		for (int i = 0; i < _reduction.size(); i++)
		{
			Token token = _reduction.get(i);
			/* -------- Begin code example for text retrieval and translation -------- */
			switch (token.getType()) 
			{
			case NON_TERMINAL:
				//int ruleId = _reduction.getParent().getTableIndex();
				_content = getContent_R(token.asReduction(), _content);	
				break;
			case CONTENT:
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
				case SymbolConstants.SYM_DECIMALINTEGERLITERAL:
				case SymbolConstants.SYM_HEXINTEGERLITERAL:
				case SymbolConstants.SYM_OCTALINTEGERLITERAL:
				case SymbolConstants.SYM_FLOATINGPOINTLITERAL:
					// Remove type-specific suffixes
					if (toAdd.matches(".*?[lL]+")) {
						toAdd = toAdd.replaceAll("(.*?)[lL]+", "$1");
					}
					else if (idx == SymbolConstants.SYM_FLOATINGPOINTLITERAL && toAdd.matches(".+?[fF]")) {
						toAdd = toAdd.replaceAll("(.+?)[fF]", "$1");
					}
					// NOTE: The missing of a break instruction is intended here!
				default:
					if (toAdd.matches("^\\w.*") && _content.matches(".*\\w$") || _content.matches(".*[,;]$")) {
						_content += " ";
					}
					_content += toAdd;
				}
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
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.parsers.CodeParser#subclassUpdateRoot(lu.fisch.structorizer.elements.Root, java.lang.String)
	 */
	@Override
	protected boolean subclassUpdateRoot(Root aRoot, String sourceFileName) throws ParserCancelled
	{
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
		return false;
	}

}

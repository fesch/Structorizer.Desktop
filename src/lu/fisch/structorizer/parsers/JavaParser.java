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
import java.util.Vector;
import java.util.logging.Level;
import java.util.regex.Matcher;

import com.creativewidgetworks.goldparser.engine.*;
import com.creativewidgetworks.goldparser.engine.enums.SymbolType;

import lu.fisch.structorizer.elements.Alternative;
import lu.fisch.structorizer.elements.Case;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.Forever;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.elements.Jump;
import lu.fisch.structorizer.elements.Repeat;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.structorizer.elements.While;
import lu.fisch.structorizer.parsers.CodeParser.ParserCancelled;
import lu.fisch.utils.BString;
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
			RuleConstants.PROD_BASICFORSTATEMENT_FOR_LPAREN_SEMI_SEMI_RPAREN,
			RuleConstants.PROD_BASICFORSTATEMENTNOSHORTIF_FOR_LPAREN_SEMI_SEMI_RPAREN,
			RuleConstants.PROD_ENHANCEDFORSTATEMENT_FOR_LPAREN_COLON_RPAREN,
			RuleConstants.PROD_ENHANCEDFORSTATEMENTNOSHORTIF_FOR_LPAREN_COLON_RPAREN,
			RuleConstants.PROD_ENHANCEDFORSTATEMENT_FOR_LPAREN_FINAL_COLON_RPAREN,
			RuleConstants.PROD_ENHANCEDFORSTATEMENTNOSHORTIF_FOR_LPAREN_FINAL_COLON_RPAREN,
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
		final int SYM_ELSE                                 =  68;  // else
		final int SYM_ENUM                                 =  69;  // enum
		final int SYM_EXTENDS                              =  70;  // extends
		final int SYM_FINAL                                =  71;  // final
		final int SYM_FINALLY                              =  72;  // finally
		final int SYM_FLOAT                                =  73;  // float
		final int SYM_FLOATINGPOINTLITERAL                 =  74;  // FloatingPointLiteral
		final int SYM_FLOATINGPOINTLITERALEXPONENT         =  75;  // FloatingPointLiteralExponent
		final int SYM_FOR                                  =  76;  // for
		final int SYM_HEXESCAPECHARLITERAL                 =  77;  // HexEscapeCharLiteral
		final int SYM_HEXINTEGERLITERAL                    =  78;  // HexIntegerLiteral
		final int SYM_IDENTIFIER                           =  79;  // Identifier
		final int SYM_IF                                   =  80;  // if
		final int SYM_IMPLEMENTS                           =  81;  // implements
		final int SYM_IMPORT                               =  82;  // import
		final int SYM_INDIRECTCHARLITERAL                  =  83;  // IndirectCharLiteral
		final int SYM_INSTANCEOF                           =  84;  // instanceof
		final int SYM_INT                                  =  85;  // int
		final int SYM_INTERFACE                            =  86;  // interface
		final int SYM_LONG                                 =  87;  // long
		final int SYM_NATIVE                               =  88;  // native
		final int SYM_NEW                                  =  89;  // new
		final int SYM_NULLLITERAL                          =  90;  // NullLiteral
		final int SYM_OCTALESCAPECHARLITERAL               =  91;  // OctalEscapeCharLiteral
		final int SYM_OCTALINTEGERLITERAL                  =  92;  // OctalIntegerLiteral
		final int SYM_PACKAGE                              =  93;  // package
		final int SYM_PRIVATE                              =  94;  // private
		final int SYM_PROTECTED                            =  95;  // protected
		final int SYM_PUBLIC                               =  96;  // public
		final int SYM_RETURN                               =  97;  // return
		final int SYM_SHORT                                =  98;  // short
		final int SYM_STANDARDESCAPECHARLITERAL            =  99;  // StandardEscapeCharLiteral
		final int SYM_STARTWITHNOZERODECIMALINTEGERLITERAL = 100;  // StartWithNoZeroDecimalIntegerLiteral
		final int SYM_STARTWITHZERODECIMALINTEGERLITERAL   = 101;  // StartWithZeroDecimalIntegerLiteral
		final int SYM_STATIC                               = 102;  // static
		final int SYM_STRICTFP                             = 103;  // strictfp
		final int SYM_STRINGLITERAL                        = 104;  // StringLiteral
		final int SYM_SUPER                                = 105;  // super
		final int SYM_SWITCH                               = 106;  // switch
		final int SYM_SYNCHRONIZED                         = 107;  // synchronized
		final int SYM_THIS                                 = 108;  // this
		final int SYM_THROW                                = 109;  // throw
		final int SYM_THROWS                               = 110;  // throws
		final int SYM_TRANSIENT                            = 111;  // transient
		final int SYM_TRY                                  = 112;  // try
		final int SYM_VOID                                 = 113;  // void
		final int SYM_VOLATILE                             = 114;  // volatile
		final int SYM_WHILE                                = 115;  // while
		final int SYM_ADDITIONALBOUNDOPT                   = 116;  // <AdditionalBoundOpt>
		final int SYM_ADDITIVEEXPRESSION                   = 117;  // <AdditiveExpression>
		final int SYM_ANDEXPRESSION                        = 118;  // <AndExpression>
		final int SYM_ANNOTATION                           = 119;  // <Annotation>
		final int SYM_ANNOTATIONS                          = 120;  // <Annotations>
		final int SYM_ARGUMENTLIST                         = 121;  // <ArgumentList>
		final int SYM_ARRAYACCESS                          = 122;  // <ArrayAccess>
		final int SYM_ARRAYCREATIONEXPRESSION              = 123;  // <ArrayCreationExpression>
		final int SYM_ARRAYINITIALIZER                     = 124;  // <ArrayInitializer>
		final int SYM_ARRAYTYPE                            = 125;  // <ArrayType>
		final int SYM_ASSIGNMENT                           = 126;  // <Assignment>
		final int SYM_ASSIGNMENTEXPRESSION                 = 127;  // <AssignmentExpression>
		final int SYM_ASSIGNMENTOPERATOR                   = 128;  // <AssignmentOperator>
		final int SYM_BASICFORSTATEMENT                    = 129;  // <BasicForStatement>
		final int SYM_BASICFORSTATEMENTNOSHORTIF           = 130;  // <BasicForStatementNoShortIf>
		final int SYM_BLOCK                                = 131;  // <Block>
		final int SYM_BLOCKSTATEMENT                       = 132;  // <BlockStatement>
		final int SYM_BLOCKSTATEMENTS                      = 133;  // <BlockStatements>
		final int SYM_BREAKSTATEMENT                       = 134;  // <BreakStatement>
		final int SYM_CASTEXPRESSION                       = 135;  // <CastExpression>
		final int SYM_CATCHCLAUSE                          = 136;  // <CatchClause>
		final int SYM_CATCHES                              = 137;  // <Catches>
		final int SYM_CHARACTERLITERAL                     = 138;  // <CharacterLiteral>
		final int SYM_CLASSBODY                            = 139;  // <ClassBody>
		final int SYM_CLASSBODYDECLARATION                 = 140;  // <ClassBodyDeclaration>
		final int SYM_CLASSBODYDECLARATIONS                = 141;  // <ClassBodyDeclarations>
		final int SYM_CLASSBODYOPT                         = 142;  // <ClassBodyOpt>
		final int SYM_CLASSDECLARATION                     = 143;  // <ClassDeclaration>
		final int SYM_CLASSINSTANCECREATIONEXPRESSION      = 144;  // <ClassInstanceCreationExpression>
		final int SYM_CLASSMEMBERDECLARATION               = 145;  // <ClassMemberDeclaration>
		final int SYM_CLASSORINTERFACETYPE                 = 146;  // <ClassOrInterfaceType>
		final int SYM_CLASSTYPE                            = 147;  // <ClassType>
		final int SYM_CLASSTYPELIST                        = 148;  // <ClassTypeList>
		final int SYM_COMPILATIONUNIT                      = 149;  // <CompilationUnit>
		final int SYM_CONDITIONALANDEXPRESSION             = 150;  // <ConditionalAndExpression>
		final int SYM_CONDITIONALEXPRESSION                = 151;  // <ConditionalExpression>
		final int SYM_CONDITIONALOREXPRESSION              = 152;  // <ConditionalOrExpression>
		final int SYM_CONSTANTDECLARATION                  = 153;  // <ConstantDeclaration>
		final int SYM_CONSTANTEXPRESSION                   = 154;  // <ConstantExpression>
		final int SYM_CONSTRUCTORBODY                      = 155;  // <ConstructorBody>
		final int SYM_CONSTRUCTORDECLARATION               = 156;  // <ConstructorDeclaration>
		final int SYM_CONSTRUCTORDECLARATOR                = 157;  // <ConstructorDeclarator>
		final int SYM_CONTINUESTATEMENT                    = 158;  // <ContinueStatement>
		final int SYM_DECIMALINTEGERLITERAL                = 159;  // <DecimalIntegerLiteral>
		final int SYM_DIMEXPR                              = 160;  // <DimExpr>
		final int SYM_DIMEXPRS                             = 161;  // <DimExprs>
		final int SYM_DIMS                                 = 162;  // <Dims>
		final int SYM_DOSTATEMENT                          = 163;  // <DoStatement>
		final int SYM_ELEMENTVALUE                         = 164;  // <ElementValue>
		final int SYM_ELEMENTVALUEARRAYINITIALIZER         = 165;  // <ElementValueArrayInitializer>
		final int SYM_ELEMENTVALUEPAIR                     = 166;  // <ElementValuePair>
		final int SYM_ELEMENTVALUEPAIRS                    = 167;  // <ElementValuePairs>
		final int SYM_ELEMENTVALUES                        = 168;  // <ElementValues>
		final int SYM_EMPTYSTATEMENT                       = 169;  // <EmptyStatement>
		final int SYM_ENHANCEDFORSTATEMENT                 = 170;  // <EnhancedForStatement>
		final int SYM_ENHANCEDFORSTATEMENTNOSHORTIF        = 171;  // <EnhancedForStatementNoShortIf>
		final int SYM_ENUMBODY                             = 172;  // <EnumBody>
		final int SYM_ENUMBODYDECLARATIONSOPT              = 173;  // <EnumBodyDeclarationsOpt>
		final int SYM_ENUMCONSTANT                         = 174;  // <EnumConstant>
		final int SYM_ENUMCONSTANTS                        = 175;  // <EnumConstants>
		final int SYM_ENUMDECLARATION                      = 176;  // <EnumDeclaration>
		final int SYM_EQUALITYEXPRESSION                   = 177;  // <EqualityExpression>
		final int SYM_EXCLUSIVEOREXPRESSION                = 178;  // <ExclusiveOrExpression>
		final int SYM_EXPLICITCONSTRUCTORINVOCATION        = 179;  // <ExplicitConstructorInvocation>
		final int SYM_EXPRESSION                           = 180;  // <Expression>
		final int SYM_EXPRESSIONOPT                        = 181;  // <ExpressionOpt>
		final int SYM_EXPRESSIONSTATEMENT                  = 182;  // <ExpressionStatement>
		final int SYM_EXTENDSINTERFACES                    = 183;  // <ExtendsInterfaces>
		final int SYM_FIELDACCESS                          = 184;  // <FieldAccess>
		final int SYM_FIELDDECLARATION                     = 185;  // <FieldDeclaration>
		final int SYM_FINALLY2                             = 186;  // <Finally>
		final int SYM_FLOATINGPOINTTYPE                    = 187;  // <FloatingPointType>
		final int SYM_FLOATPOINTLITERAL                    = 188;  // <FloatPointLiteral>
		final int SYM_FORINITOPT                           = 189;  // <ForInitOpt>
		final int SYM_FORMALPARAMETER                      = 190;  // <FormalParameter>
		final int SYM_FORMALPARAMETERLIST                  = 191;  // <FormalParameterList>
		final int SYM_FORSTATEMENT                         = 192;  // <ForStatement>
		final int SYM_FORSTATEMENTNOSHORTIF                = 193;  // <ForStatementNoShortIf>
		final int SYM_FORUPDATEOPT                         = 194;  // <ForUpdateOpt>
		final int SYM_IFTHENELSESTATEMENT                  = 195;  // <IfThenElseStatement>
		final int SYM_IFTHENELSESTATEMENTNOSHORTIF         = 196;  // <IfThenElseStatementNoShortIf>
		final int SYM_IFTHENSTATEMENT                      = 197;  // <IfThenStatement>
		final int SYM_IMPORTDECLARATION                    = 198;  // <ImportDeclaration>
		final int SYM_IMPORTDECLARATIONS                   = 199;  // <ImportDeclarations>
		final int SYM_INCLUSIVEOREXPRESSION                = 200;  // <InclusiveOrExpression>
		final int SYM_INTEGERLITERAL                       = 201;  // <IntegerLiteral>
		final int SYM_INTEGRALTYPE                         = 202;  // <IntegralType>
		final int SYM_INTERFACEBODY                        = 203;  // <InterfaceBody>
		final int SYM_INTERFACEDECLARATION                 = 204;  // <InterfaceDeclaration>
		final int SYM_INTERFACEMEMBERDECLARATION           = 205;  // <InterfaceMemberDeclaration>
		final int SYM_INTERFACEMEMBERDECLARATIONS          = 206;  // <InterfaceMemberDeclarations>
		final int SYM_INTERFACES                           = 207;  // <Interfaces>
		final int SYM_INTERFACETYPE                        = 208;  // <InterfaceType>
		final int SYM_INTERFACETYPELIST                    = 209;  // <InterfaceTypeList>
		final int SYM_LABELEDSTATEMENT                     = 210;  // <LabeledStatement>
		final int SYM_LABELEDSTATEMENTNOSHORTIF            = 211;  // <LabeledStatementNoShortIf>
		final int SYM_LEFTHANDSIDE                         = 212;  // <LeftHandSide>
		final int SYM_LITERAL                              = 213;  // <Literal>
		final int SYM_LOCALCLASSDECLARATION                = 214;  // <LocalClassDeclaration>
		final int SYM_LOCALCLASSMODIFIERS                  = 215;  // <LocalClassModifiers>
		final int SYM_LOCALVARIABLEDECLARATION             = 216;  // <LocalVariableDeclaration>
		final int SYM_LOCALVARIABLEDECLARATIONSTATEMENT    = 217;  // <LocalVariableDeclarationStatement>
		final int SYM_MARKERANNOTATION                     = 218;  // <MarkerAnnotation>
		final int SYM_METHODBODY                           = 219;  // <MethodBody>
		final int SYM_METHODDECLARATION                    = 220;  // <MethodDeclaration>
		final int SYM_METHODDECLARATOR                     = 221;  // <MethodDeclarator>
		final int SYM_METHODHEADER                         = 222;  // <MethodHeader>
		final int SYM_METHODINVOCATION                     = 223;  // <MethodInvocation>
		final int SYM_MODIFIER                             = 224;  // <Modifier>
		final int SYM_MODIFIERS                            = 225;  // <Modifiers>
		final int SYM_MULTIPLICATIVEEXPRESSION             = 226;  // <MultiplicativeExpression>
		final int SYM_NAME                                 = 227;  // <Name>
		final int SYM_NORMALANNOTATION                     = 228;  // <NormalAnnotation>
		final int SYM_NORMALCLASSDECLARATION               = 229;  // <NormalClassDeclaration>
		final int SYM_NUMERICTYPE                          = 230;  // <NumericType>
		final int SYM_PACKAGEDECLARATION                   = 231;  // <PackageDeclaration>
		final int SYM_POSTDECREMENTEXPRESSION              = 232;  // <PostDecrementExpression>
		final int SYM_POSTFIXEXPRESSION                    = 233;  // <PostfixExpression>
		final int SYM_POSTINCREMENTEXPRESSION              = 234;  // <PostIncrementExpression>
		final int SYM_PREDECREMENTEXPRESSION               = 235;  // <PreDecrementExpression>
		final int SYM_PREINCREMENTEXPRESSION               = 236;  // <PreIncrementExpression>
		final int SYM_PRIMARY                              = 237;  // <Primary>
		final int SYM_PRIMARYNONEWARRAY                    = 238;  // <PrimaryNoNewArray>
		final int SYM_PRIMITIVETYPE                        = 239;  // <PrimitiveType>
		final int SYM_PURECLASSDECLARATION                 = 240;  // <PureClassDeclaration>
		final int SYM_QUALIFIEDNAME                        = 241;  // <QualifiedName>
		final int SYM_REFERENCETYPE                        = 242;  // <ReferenceType>
		final int SYM_RELATIONALEXPRESSION                 = 243;  // <RelationalExpression>
		final int SYM_RETURNSTATEMENT                      = 244;  // <ReturnStatement>
		final int SYM_SHIFTEXPRESSION                      = 245;  // <ShiftExpression>
		final int SYM_SIMPLENAME                           = 246;  // <SimpleName>
		final int SYM_SINGLEELEMENTANNOTATION              = 247;  // <SingleElementAnnotation>
		final int SYM_SINGLESTATICIMPORTDECLARATION        = 248;  // <SingleStaticImportDeclaration>
		final int SYM_SINGLETYPEIMPORTDECLARATION          = 249;  // <SingleTypeImportDeclaration>
		final int SYM_STATEMENT                            = 250;  // <Statement>
		final int SYM_STATEMENTEXPRESSION                  = 251;  // <StatementExpression>
		final int SYM_STATEMENTEXPRESSIONLIST              = 252;  // <StatementExpressionList>
		final int SYM_STATEMENTNOSHORTIF                   = 253;  // <StatementNoShortIf>
		final int SYM_STATEMENTWITHOUTTRAILINGSUBSTATEMENT = 254;  // <StatementWithoutTrailingSubstatement>
		final int SYM_STATICIMPORTONDEMANDDECLARATION      = 255;  // <StaticImportOnDemandDeclaration>
		final int SYM_STATICINITIALIZER                    = 256;  // <StaticInitializer>
		final int SYM_SUPER2                               = 257;  // <Super>
		final int SYM_SWITCHBLOCK                          = 258;  // <SwitchBlock>
		final int SYM_SWITCHBLOCKSTATEMENTGROUP            = 259;  // <SwitchBlockStatementGroup>
		final int SYM_SWITCHBLOCKSTATEMENTGROUPS           = 260;  // <SwitchBlockStatementGroups>
		final int SYM_SWITCHLABEL                          = 261;  // <SwitchLabel>
		final int SYM_SWITCHLABELS                         = 262;  // <SwitchLabels>
		final int SYM_SWITCHSTATEMENT                      = 263;  // <SwitchStatement>
		final int SYM_SYNCHRONIZEDSTATEMENT                = 264;  // <SynchronizedStatement>
		final int SYM_THROWS2                              = 265;  // <Throws>
		final int SYM_THROWSTATEMENT                       = 266;  // <ThrowStatement>
		final int SYM_TRYSTATEMENT                         = 267;  // <TryStatement>
		final int SYM_TYPE                                 = 268;  // <Type>
		final int SYM_TYPEARGUMENT                         = 269;  // <TypeArgument>
		final int SYM_TYPEARGUMENTS                        = 270;  // <TypeArguments>
		final int SYM_TYPEARGUMENTSOPT                     = 271;  // <TypeArgumentsOpt>
		final int SYM_TYPEBOUNDOPT                         = 272;  // <TypeBoundOpt>
		final int SYM_TYPEDECLARATION                      = 273;  // <TypeDeclaration>
		final int SYM_TYPEDECLARATIONS                     = 274;  // <TypeDeclarations>
		final int SYM_TYPEIMPORTONDEMANDDECLARATION        = 275;  // <TypeImportOnDemandDeclaration>
		final int SYM_TYPENAME                             = 276;  // <TypeName>
		final int SYM_TYPEPARAMETER                        = 277;  // <TypeParameter>
		final int SYM_TYPEPARAMETERS                       = 278;  // <TypeParameters>
		final int SYM_TYPEPARAMETERSOPT                    = 279;  // <TypeParametersOpt>
		final int SYM_TYPEVARIABLE                         = 280;  // <TypeVariable>
		final int SYM_UNARYEXPRESSION                      = 281;  // <UnaryExpression>
		final int SYM_UNARYEXPRESSIONNOTPLUSMINUS          = 282;  // <UnaryExpressionNotPlusMinus>
		final int SYM_VARIABLEDECLARATOR                   = 283;  // <VariableDeclarator>
		final int SYM_VARIABLEDECLARATORID                 = 284;  // <VariableDeclaratorId>
		final int SYM_VARIABLEDECLARATORS                  = 285;  // <VariableDeclarators>
		final int SYM_VARIABLEINITIALIZER                  = 286;  // <VariableInitializer>
		final int SYM_VARIABLEINITIALIZERS                 = 287;  // <VariableInitializers>
		final int SYM_WHILESTATEMENT                       = 288;  // <WhileStatement>
		final int SYM_WHILESTATEMENTNOSHORTIF              = 289;  // <WhileStatementNoShortIf>
		final int SYM_WILDCARD                             = 290;  // <Wildcard>
		final int SYM_WILDCARDBOUNDSOPT                    = 291;  // <WildcardBoundsOpt>
	};

	// Symbolic constants naming the table indices of the grammar rules
	@SuppressWarnings("unused")
	private interface RuleConstants
	{
		final int PROD_CHARACTERLITERAL_INDIRECTCHARLITERAL                         =   0;  // <CharacterLiteral> ::= IndirectCharLiteral
		final int PROD_CHARACTERLITERAL_STANDARDESCAPECHARLITERAL                   =   1;  // <CharacterLiteral> ::= StandardEscapeCharLiteral
		final int PROD_CHARACTERLITERAL_OCTALESCAPECHARLITERAL                      =   2;  // <CharacterLiteral> ::= OctalEscapeCharLiteral
		final int PROD_CHARACTERLITERAL_HEXESCAPECHARLITERAL                        =   3;  // <CharacterLiteral> ::= HexEscapeCharLiteral
		final int PROD_DECIMALINTEGERLITERAL_STARTWITHZERODECIMALINTEGERLITERAL     =   4;  // <DecimalIntegerLiteral> ::= StartWithZeroDecimalIntegerLiteral
		final int PROD_DECIMALINTEGERLITERAL_STARTWITHNOZERODECIMALINTEGERLITERAL   =   5;  // <DecimalIntegerLiteral> ::= StartWithNoZeroDecimalIntegerLiteral
		final int PROD_FLOATPOINTLITERAL_FLOATINGPOINTLITERAL                       =   6;  // <FloatPointLiteral> ::= FloatingPointLiteral
		final int PROD_FLOATPOINTLITERAL_FLOATINGPOINTLITERALEXPONENT               =   7;  // <FloatPointLiteral> ::= FloatingPointLiteralExponent
		final int PROD_INTEGERLITERAL                                               =   8;  // <IntegerLiteral> ::= <DecimalIntegerLiteral>
		final int PROD_INTEGERLITERAL_HEXINTEGERLITERAL                             =   9;  // <IntegerLiteral> ::= HexIntegerLiteral
		final int PROD_INTEGERLITERAL_OCTALINTEGERLITERAL                           =  10;  // <IntegerLiteral> ::= OctalIntegerLiteral
		final int PROD_LITERAL                                                      =  11;  // <Literal> ::= <IntegerLiteral>
		final int PROD_LITERAL2                                                     =  12;  // <Literal> ::= <FloatPointLiteral>
		final int PROD_LITERAL_BOOLEANLITERAL                                       =  13;  // <Literal> ::= BooleanLiteral
		final int PROD_LITERAL3                                                     =  14;  // <Literal> ::= <CharacterLiteral>
		final int PROD_LITERAL_STRINGLITERAL                                        =  15;  // <Literal> ::= StringLiteral
		final int PROD_LITERAL_NULLLITERAL                                          =  16;  // <Literal> ::= NullLiteral
		final int PROD_ANNOTATION                                                   =  17;  // <Annotation> ::= <NormalAnnotation>
		final int PROD_ANNOTATION2                                                  =  18;  // <Annotation> ::= <MarkerAnnotation>
		final int PROD_ANNOTATION3                                                  =  19;  // <Annotation> ::= <SingleElementAnnotation>
		final int PROD_NORMALANNOTATION_AT_LPAREN_RPAREN                            =  20;  // <NormalAnnotation> ::= '@' <TypeName> '(' <ElementValuePairs> ')'
		final int PROD_ELEMENTVALUEPAIRS                                            =  21;  // <ElementValuePairs> ::= <ElementValuePair>
		final int PROD_ELEMENTVALUEPAIRS_COMMA                                      =  22;  // <ElementValuePairs> ::= <ElementValuePairs> ',' <ElementValuePair>
		final int PROD_ELEMENTVALUEPAIR_IDENTIFIER_EQ                               =  23;  // <ElementValuePair> ::= Identifier '=' <ElementValue>
		final int PROD_ELEMENTVALUE                                                 =  24;  // <ElementValue> ::= <ConditionalExpression>
		final int PROD_ELEMENTVALUE2                                                =  25;  // <ElementValue> ::= <ElementValueArrayInitializer>
		final int PROD_ELEMENTVALUE3                                                =  26;  // <ElementValue> ::= <Annotation>
		final int PROD_ELEMENTVALUEARRAYINITIALIZER_LBRACE_RBRACE                   =  27;  // <ElementValueArrayInitializer> ::= '{' <ElementValues> '}'
		final int PROD_ELEMENTVALUEARRAYINITIALIZER_LBRACE_COMMA_RBRACE             =  28;  // <ElementValueArrayInitializer> ::= '{' <ElementValues> ',' '}'
		final int PROD_ELEMENTVALUEARRAYINITIALIZER_LBRACE_COMMA_RBRACE2            =  29;  // <ElementValueArrayInitializer> ::= '{' ',' '}'
		final int PROD_ELEMENTVALUEARRAYINITIALIZER_LBRACE_RBRACE2                  =  30;  // <ElementValueArrayInitializer> ::= '{' '}'
		final int PROD_ELEMENTVALUES                                                =  31;  // <ElementValues> ::= <ElementValue>
		final int PROD_ELEMENTVALUES_COMMA                                          =  32;  // <ElementValues> ::= <ElementValues> ',' <ElementValue>
		final int PROD_MARKERANNOTATION_AT                                          =  33;  // <MarkerAnnotation> ::= '@' <TypeName>
		final int PROD_SINGLEELEMENTANNOTATION_AT_LPAREN_RPAREN                     =  34;  // <SingleElementAnnotation> ::= '@' <TypeName> '(' <ElementValue> ')'
		final int PROD_TYPENAME                                                     =  35;  // <TypeName> ::= <Name>
		final int PROD_ANNOTATIONS                                                  =  36;  // <Annotations> ::= <Annotation> <Annotations>
		final int PROD_ANNOTATIONS2                                                 =  37;  // <Annotations> ::= 
		final int PROD_TYPE                                                         =  38;  // <Type> ::= <PrimitiveType>
		final int PROD_TYPE2                                                        =  39;  // <Type> ::= <ReferenceType>
		final int PROD_PRIMITIVETYPE                                                =  40;  // <PrimitiveType> ::= <Annotations> <NumericType>
		final int PROD_PRIMITIVETYPE_BOOLEAN                                        =  41;  // <PrimitiveType> ::= <Annotations> boolean
		final int PROD_NUMERICTYPE                                                  =  42;  // <NumericType> ::= <IntegralType>
		final int PROD_NUMERICTYPE2                                                 =  43;  // <NumericType> ::= <FloatingPointType>
		final int PROD_INTEGRALTYPE_BYTE                                            =  44;  // <IntegralType> ::= byte
		final int PROD_INTEGRALTYPE_SHORT                                           =  45;  // <IntegralType> ::= short
		final int PROD_INTEGRALTYPE_INT                                             =  46;  // <IntegralType> ::= int
		final int PROD_INTEGRALTYPE_LONG                                            =  47;  // <IntegralType> ::= long
		final int PROD_INTEGRALTYPE_CHAR                                            =  48;  // <IntegralType> ::= char
		final int PROD_FLOATINGPOINTTYPE_FLOAT                                      =  49;  // <FloatingPointType> ::= float
		final int PROD_FLOATINGPOINTTYPE_DOUBLE                                     =  50;  // <FloatingPointType> ::= double
		final int PROD_REFERENCETYPE                                                =  51;  // <ReferenceType> ::= <ClassOrInterfaceType>
		final int PROD_REFERENCETYPE2                                               =  52;  // <ReferenceType> ::= <ArrayType>
		final int PROD_CLASSORINTERFACETYPE                                         =  53;  // <ClassOrInterfaceType> ::= <Name> <TypeArgumentsOpt>
		final int PROD_CLASSTYPE                                                    =  54;  // <ClassType> ::= <ClassOrInterfaceType>
		final int PROD_INTERFACETYPE                                                =  55;  // <InterfaceType> ::= <ClassOrInterfaceType>
		final int PROD_TYPEVARIABLE_IDENTIFIER                                      =  56;  // <TypeVariable> ::= <Annotations> Identifier
		final int PROD_ARRAYTYPE                                                    =  57;  // <ArrayType> ::= <PrimitiveType> <Dims>
		final int PROD_ARRAYTYPE2                                                   =  58;  // <ArrayType> ::= <Name> <Dims>
		final int PROD_NAME                                                         =  59;  // <Name> ::= <SimpleName>
		final int PROD_NAME2                                                        =  60;  // <Name> ::= <QualifiedName>
		final int PROD_SIMPLENAME_IDENTIFIER                                        =  61;  // <SimpleName> ::= Identifier
		final int PROD_QUALIFIEDNAME_DOT_IDENTIFIER                                 =  62;  // <QualifiedName> ::= <Name> '.' Identifier
		final int PROD_TYPEARGUMENTSOPT_LT_GT                                       =  63;  // <TypeArgumentsOpt> ::= '<' <TypeArguments> '>'
		final int PROD_TYPEARGUMENTSOPT                                             =  64;  // <TypeArgumentsOpt> ::= 
		final int PROD_TYPEARGUMENTS                                                =  65;  // <TypeArguments> ::= <TypeArgument>
		final int PROD_TYPEARGUMENTS_COMMA                                          =  66;  // <TypeArguments> ::= <TypeArguments> ',' <TypeArgument>
		final int PROD_TYPEARGUMENT                                                 =  67;  // <TypeArgument> ::= <ReferenceType>
		final int PROD_TYPEARGUMENT2                                                =  68;  // <TypeArgument> ::= <Wildcard>
		final int PROD_WILDCARD_QUESTION                                            =  69;  // <Wildcard> ::= <Annotations> '?' <WildcardBoundsOpt>
		final int PROD_WILDCARDBOUNDSOPT_EXTENDS                                    =  70;  // <WildcardBoundsOpt> ::= extends <ReferenceType>
		final int PROD_WILDCARDBOUNDSOPT_SUPER                                      =  71;  // <WildcardBoundsOpt> ::= super <ReferenceType>
		final int PROD_WILDCARDBOUNDSOPT                                            =  72;  // <WildcardBoundsOpt> ::= 
		final int PROD_TYPEPARAMETER_IDENTIFIER                                     =  73;  // <TypeParameter> ::= <Annotations> Identifier <TypeBoundOpt>
		final int PROD_TYPEBOUNDOPT_EXTENDS                                         =  74;  // <TypeBoundOpt> ::= extends <TypeVariable>
		final int PROD_TYPEBOUNDOPT_EXTENDS2                                        =  75;  // <TypeBoundOpt> ::= extends <ClassOrInterfaceType> <AdditionalBoundOpt>
		final int PROD_ADDITIONALBOUNDOPT_AMP                                       =  76;  // <AdditionalBoundOpt> ::= '&' <InterfaceType>
		final int PROD_ADDITIONALBOUNDOPT                                           =  77;  // <AdditionalBoundOpt> ::= 
		final int PROD_COMPILATIONUNIT                                              =  78;  // <CompilationUnit> ::= <PackageDeclaration> <ImportDeclarations> <TypeDeclarations>
		final int PROD_COMPILATIONUNIT2                                             =  79;  // <CompilationUnit> ::= <PackageDeclaration> <ImportDeclarations>
		final int PROD_COMPILATIONUNIT3                                             =  80;  // <CompilationUnit> ::= <PackageDeclaration> <TypeDeclarations>
		final int PROD_COMPILATIONUNIT4                                             =  81;  // <CompilationUnit> ::= <PackageDeclaration>
		final int PROD_COMPILATIONUNIT5                                             =  82;  // <CompilationUnit> ::= <ImportDeclarations> <TypeDeclarations>
		final int PROD_COMPILATIONUNIT6                                             =  83;  // <CompilationUnit> ::= <ImportDeclarations>
		final int PROD_COMPILATIONUNIT7                                             =  84;  // <CompilationUnit> ::= <TypeDeclarations>
		final int PROD_COMPILATIONUNIT8                                             =  85;  // <CompilationUnit> ::= 
		final int PROD_IMPORTDECLARATIONS                                           =  86;  // <ImportDeclarations> ::= <ImportDeclaration>
		final int PROD_IMPORTDECLARATIONS2                                          =  87;  // <ImportDeclarations> ::= <ImportDeclarations> <ImportDeclaration>
		final int PROD_TYPEDECLARATIONS                                             =  88;  // <TypeDeclarations> ::= <TypeDeclaration>
		final int PROD_TYPEDECLARATIONS2                                            =  89;  // <TypeDeclarations> ::= <TypeDeclarations> <TypeDeclaration>
		final int PROD_PACKAGEDECLARATION_PACKAGE_SEMI                              =  90;  // <PackageDeclaration> ::= package <Name> ';'
		final int PROD_IMPORTDECLARATION                                            =  91;  // <ImportDeclaration> ::= <SingleTypeImportDeclaration>
		final int PROD_IMPORTDECLARATION2                                           =  92;  // <ImportDeclaration> ::= <TypeImportOnDemandDeclaration>
		final int PROD_IMPORTDECLARATION3                                           =  93;  // <ImportDeclaration> ::= <SingleStaticImportDeclaration>
		final int PROD_IMPORTDECLARATION4                                           =  94;  // <ImportDeclaration> ::= <StaticImportOnDemandDeclaration>
		final int PROD_SINGLETYPEIMPORTDECLARATION_IMPORT_SEMI                      =  95;  // <SingleTypeImportDeclaration> ::= import <Name> ';'
		final int PROD_TYPEIMPORTONDEMANDDECLARATION_IMPORT_DOT_TIMES_SEMI          =  96;  // <TypeImportOnDemandDeclaration> ::= import <Name> '.' '*' ';'
		final int PROD_SINGLESTATICIMPORTDECLARATION_IMPORT_STATIC_SEMI             =  97;  // <SingleStaticImportDeclaration> ::= import static <Name> ';'
		final int PROD_STATICIMPORTONDEMANDDECLARATION_IMPORT_STATIC_DOT_TIMES_SEMI =  98;  // <StaticImportOnDemandDeclaration> ::= import static <Name> '.' '*' ';'
		final int PROD_TYPEDECLARATION                                              =  99;  // <TypeDeclaration> ::= <ClassDeclaration>
		final int PROD_TYPEDECLARATION2                                             = 100;  // <TypeDeclaration> ::= <InterfaceDeclaration>
		final int PROD_TYPEDECLARATION_SEMI                                         = 101;  // <TypeDeclaration> ::= ';'
		final int PROD_MODIFIERS                                                    = 102;  // <Modifiers> ::= <Modifier>
		final int PROD_MODIFIERS2                                                   = 103;  // <Modifiers> ::= <Modifiers> <Modifier>
		final int PROD_MODIFIER_PUBLIC                                              = 104;  // <Modifier> ::= public
		final int PROD_MODIFIER_PROTECTED                                           = 105;  // <Modifier> ::= protected
		final int PROD_MODIFIER_PRIVATE                                             = 106;  // <Modifier> ::= private
		final int PROD_MODIFIER_STATIC                                              = 107;  // <Modifier> ::= static
		final int PROD_MODIFIER_ABSTRACT                                            = 108;  // <Modifier> ::= abstract
		final int PROD_MODIFIER_FINAL                                               = 109;  // <Modifier> ::= final
		final int PROD_MODIFIER_NATIVE                                              = 110;  // <Modifier> ::= native
		final int PROD_MODIFIER_SYNCHRONIZED                                        = 111;  // <Modifier> ::= synchronized
		final int PROD_MODIFIER_TRANSIENT                                           = 112;  // <Modifier> ::= transient
		final int PROD_MODIFIER_VOLATILE                                            = 113;  // <Modifier> ::= volatile
		final int PROD_MODIFIER_DEFAULT                                             = 114;  // <Modifier> ::= default
		final int PROD_MODIFIER_STRICTFP                                            = 115;  // <Modifier> ::= strictfp
		final int PROD_CLASSDECLARATION                                             = 116;  // <ClassDeclaration> ::= <Annotations> <NormalClassDeclaration>
		final int PROD_CLASSDECLARATION2                                            = 117;  // <ClassDeclaration> ::= <Annotations> <EnumDeclaration>
		final int PROD_NORMALCLASSDECLARATION                                       = 118;  // <NormalClassDeclaration> ::= <Modifiers> <PureClassDeclaration>
		final int PROD_NORMALCLASSDECLARATION2                                      = 119;  // <NormalClassDeclaration> ::= <PureClassDeclaration>
		final int PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER                        = 120;  // <PureClassDeclaration> ::= class Identifier <TypeParametersOpt> <Super> <Interfaces> <ClassBody>
		final int PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER2                       = 121;  // <PureClassDeclaration> ::= class Identifier <TypeParametersOpt> <Super> <ClassBody>
		final int PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER3                       = 122;  // <PureClassDeclaration> ::= class Identifier <TypeParametersOpt> <Interfaces> <ClassBody>
		final int PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER4                       = 123;  // <PureClassDeclaration> ::= class Identifier <TypeParametersOpt> <ClassBody>
		final int PROD_TYPEPARAMETERSOPT_LT_GT                                      = 124;  // <TypeParametersOpt> ::= '<' <TypeParameters> '>'
		final int PROD_TYPEPARAMETERSOPT                                            = 125;  // <TypeParametersOpt> ::= 
		final int PROD_TYPEPARAMETERS                                               = 126;  // <TypeParameters> ::= <TypeParameter>
		final int PROD_TYPEPARAMETERS_COMMA                                         = 127;  // <TypeParameters> ::= <TypeParameters> ',' <TypeParameter>
		final int PROD_SUPER_EXTENDS                                                = 128;  // <Super> ::= extends <ClassType>
		final int PROD_INTERFACES_IMPLEMENTS                                        = 129;  // <Interfaces> ::= implements <InterfaceTypeList>
		final int PROD_INTERFACETYPELIST                                            = 130;  // <InterfaceTypeList> ::= <InterfaceType>
		final int PROD_INTERFACETYPELIST_COMMA                                      = 131;  // <InterfaceTypeList> ::= <InterfaceTypeList> ',' <InterfaceType>
		final int PROD_ENUMDECLARATION_ENUM_IDENTIFIER                              = 132;  // <EnumDeclaration> ::= <Modifiers> enum Identifier <Interfaces> <EnumBody>
		final int PROD_ENUMDECLARATION_ENUM_IDENTIFIER2                             = 133;  // <EnumDeclaration> ::= <Modifiers> enum Identifier <EnumBody>
		final int PROD_ENUMBODY_LBRACE_COMMA_RBRACE                                 = 134;  // <EnumBody> ::= '{' <EnumConstants> ',' <EnumBodyDeclarationsOpt> '}'
		final int PROD_ENUMBODY_LBRACE_RBRACE                                       = 135;  // <EnumBody> ::= '{' <EnumConstants> <EnumBodyDeclarationsOpt> '}'
		final int PROD_ENUMBODYDECLARATIONSOPT_SEMI                                 = 136;  // <EnumBodyDeclarationsOpt> ::= ';' <ClassBodyDeclarations>
		final int PROD_ENUMBODYDECLARATIONSOPT                                      = 137;  // <EnumBodyDeclarationsOpt> ::= 
		final int PROD_ENUMCONSTANTS                                                = 138;  // <EnumConstants> ::= <EnumConstant>
		final int PROD_ENUMCONSTANTS_COMMA                                          = 139;  // <EnumConstants> ::= <EnumConstants> ',' <EnumConstant>
		final int PROD_ENUMCONSTANT_IDENTIFIER_LPAREN_RPAREN                        = 140;  // <EnumConstant> ::= <Annotations> Identifier '(' <ArgumentList> ')' <ClassBodyOpt>
		final int PROD_ENUMCONSTANT_IDENTIFIER                                      = 141;  // <EnumConstant> ::= <Annotations> Identifier <ClassBodyOpt>
		final int PROD_CLASSBODYOPT                                                 = 142;  // <ClassBodyOpt> ::= <ClassBody>
		final int PROD_CLASSBODYOPT2                                                = 143;  // <ClassBodyOpt> ::= 
		final int PROD_CLASSBODY_LBRACE_RBRACE                                      = 144;  // <ClassBody> ::= '{' <ClassBodyDeclarations> '}'
		final int PROD_CLASSBODY_LBRACE_RBRACE2                                     = 145;  // <ClassBody> ::= '{' '}'
		final int PROD_CLASSBODYDECLARATIONS                                        = 146;  // <ClassBodyDeclarations> ::= <ClassBodyDeclaration>
		final int PROD_CLASSBODYDECLARATIONS2                                       = 147;  // <ClassBodyDeclarations> ::= <ClassBodyDeclarations> <ClassBodyDeclaration>
		final int PROD_CLASSBODYDECLARATION                                         = 148;  // <ClassBodyDeclaration> ::= <ClassMemberDeclaration>
		final int PROD_CLASSBODYDECLARATION2                                        = 149;  // <ClassBodyDeclaration> ::= <StaticInitializer>
		final int PROD_CLASSBODYDECLARATION3                                        = 150;  // <ClassBodyDeclaration> ::= <ConstructorDeclaration>
		final int PROD_CLASSMEMBERDECLARATION                                       = 151;  // <ClassMemberDeclaration> ::= <FieldDeclaration>
		final int PROD_CLASSMEMBERDECLARATION2                                      = 152;  // <ClassMemberDeclaration> ::= <MethodDeclaration>
		final int PROD_CLASSMEMBERDECLARATION3                                      = 153;  // <ClassMemberDeclaration> ::= <ClassDeclaration>
		final int PROD_CLASSMEMBERDECLARATION4                                      = 154;  // <ClassMemberDeclaration> ::= <InterfaceDeclaration>
		final int PROD_CLASSMEMBERDECLARATION_SEMI                                  = 155;  // <ClassMemberDeclaration> ::= ';'
		final int PROD_FIELDDECLARATION_SEMI                                        = 156;  // <FieldDeclaration> ::= <Annotations> <Modifiers> <Type> <VariableDeclarators> ';'
		final int PROD_FIELDDECLARATION_SEMI2                                       = 157;  // <FieldDeclaration> ::= <Annotations> <Type> <VariableDeclarators> ';'
		final int PROD_VARIABLEDECLARATORS                                          = 158;  // <VariableDeclarators> ::= <VariableDeclarator>
		final int PROD_VARIABLEDECLARATORS_COMMA                                    = 159;  // <VariableDeclarators> ::= <VariableDeclarators> ',' <VariableDeclarator>
		final int PROD_VARIABLEDECLARATOR                                           = 160;  // <VariableDeclarator> ::= <VariableDeclaratorId>
		final int PROD_VARIABLEDECLARATOR_EQ                                        = 161;  // <VariableDeclarator> ::= <VariableDeclaratorId> '=' <VariableInitializer>
		final int PROD_VARIABLEDECLARATORID_IDENTIFIER                              = 162;  // <VariableDeclaratorId> ::= Identifier
		final int PROD_VARIABLEDECLARATORID_LBRACKET_RBRACKET                       = 163;  // <VariableDeclaratorId> ::= <VariableDeclaratorId> '[' ']'
		final int PROD_VARIABLEINITIALIZER                                          = 164;  // <VariableInitializer> ::= <Expression>
		final int PROD_VARIABLEINITIALIZER2                                         = 165;  // <VariableInitializer> ::= <ArrayInitializer>
		final int PROD_METHODDECLARATION                                            = 166;  // <MethodDeclaration> ::= <Annotations> <MethodHeader> <MethodBody>
		final int PROD_METHODHEADER                                                 = 167;  // <MethodHeader> ::= <Modifiers> <Type> <MethodDeclarator> <Throws>
		final int PROD_METHODHEADER2                                                = 168;  // <MethodHeader> ::= <Modifiers> <Type> <MethodDeclarator>
		final int PROD_METHODHEADER3                                                = 169;  // <MethodHeader> ::= <Type> <MethodDeclarator> <Throws>
		final int PROD_METHODHEADER4                                                = 170;  // <MethodHeader> ::= <Type> <MethodDeclarator>
		final int PROD_METHODHEADER_VOID                                            = 171;  // <MethodHeader> ::= <Modifiers> void <MethodDeclarator> <Throws>
		final int PROD_METHODHEADER_VOID2                                           = 172;  // <MethodHeader> ::= <Modifiers> void <MethodDeclarator>
		final int PROD_METHODHEADER_VOID3                                           = 173;  // <MethodHeader> ::= void <MethodDeclarator> <Throws>
		final int PROD_METHODHEADER_VOID4                                           = 174;  // <MethodHeader> ::= void <MethodDeclarator>
		final int PROD_METHODDECLARATOR_IDENTIFIER_LPAREN_RPAREN                    = 175;  // <MethodDeclarator> ::= Identifier '(' <FormalParameterList> ')'
		final int PROD_METHODDECLARATOR_IDENTIFIER_LPAREN_RPAREN2                   = 176;  // <MethodDeclarator> ::= Identifier '(' ')'
		final int PROD_METHODDECLARATOR_LBRACKET_RBRACKET                           = 177;  // <MethodDeclarator> ::= <MethodDeclarator> '[' ']'
		final int PROD_FORMALPARAMETERLIST                                          = 178;  // <FormalParameterList> ::= <FormalParameter>
		final int PROD_FORMALPARAMETERLIST_COMMA                                    = 179;  // <FormalParameterList> ::= <FormalParameterList> ',' <FormalParameter>
		final int PROD_FORMALPARAMETER                                              = 180;  // <FormalParameter> ::= <Type> <VariableDeclaratorId>
		final int PROD_THROWS_THROWS                                                = 181;  // <Throws> ::= throws <ClassTypeList>
		final int PROD_CLASSTYPELIST                                                = 182;  // <ClassTypeList> ::= <ClassType>
		final int PROD_CLASSTYPELIST_COMMA                                          = 183;  // <ClassTypeList> ::= <ClassTypeList> ',' <ClassType>
		final int PROD_METHODBODY                                                   = 184;  // <MethodBody> ::= <Block>
		final int PROD_METHODBODY_SEMI                                              = 185;  // <MethodBody> ::= ';'
		final int PROD_STATICINITIALIZER_STATIC                                     = 186;  // <StaticInitializer> ::= <Annotations> static <Block>
		final int PROD_CONSTRUCTORDECLARATION                                       = 187;  // <ConstructorDeclaration> ::= <Annotations> <Modifiers> <ConstructorDeclarator> <Throws> <ConstructorBody>
		final int PROD_CONSTRUCTORDECLARATION2                                      = 188;  // <ConstructorDeclaration> ::= <Annotations> <Modifiers> <ConstructorDeclarator> <ConstructorBody>
		final int PROD_CONSTRUCTORDECLARATION3                                      = 189;  // <ConstructorDeclaration> ::= <Annotations> <ConstructorDeclarator> <Throws> <ConstructorBody>
		final int PROD_CONSTRUCTORDECLARATION4                                      = 190;  // <ConstructorDeclaration> ::= <Annotations> <ConstructorDeclarator> <ConstructorBody>
		final int PROD_CONSTRUCTORDECLARATOR_LPAREN_RPAREN                          = 191;  // <ConstructorDeclarator> ::= <SimpleName> '(' <FormalParameterList> ')'
		final int PROD_CONSTRUCTORDECLARATOR_LPAREN_RPAREN2                         = 192;  // <ConstructorDeclarator> ::= <SimpleName> '(' ')'
		final int PROD_CONSTRUCTORBODY_LBRACE_RBRACE                                = 193;  // <ConstructorBody> ::= '{' <ExplicitConstructorInvocation> <BlockStatements> '}'
		final int PROD_CONSTRUCTORBODY_LBRACE_RBRACE2                               = 194;  // <ConstructorBody> ::= '{' <ExplicitConstructorInvocation> '}'
		final int PROD_CONSTRUCTORBODY_LBRACE_RBRACE3                               = 195;  // <ConstructorBody> ::= '{' <BlockStatements> '}'
		final int PROD_CONSTRUCTORBODY_LBRACE_RBRACE4                               = 196;  // <ConstructorBody> ::= '{' '}'
		final int PROD_EXPLICITCONSTRUCTORINVOCATION_THIS_LPAREN_RPAREN_SEMI        = 197;  // <ExplicitConstructorInvocation> ::= this '(' <ArgumentList> ')' ';'
		final int PROD_EXPLICITCONSTRUCTORINVOCATION_THIS_LPAREN_RPAREN_SEMI2       = 198;  // <ExplicitConstructorInvocation> ::= this '(' ')' ';'
		final int PROD_EXPLICITCONSTRUCTORINVOCATION_SUPER_LPAREN_RPAREN_SEMI       = 199;  // <ExplicitConstructorInvocation> ::= super '(' <ArgumentList> ')' ';'
		final int PROD_EXPLICITCONSTRUCTORINVOCATION_SUPER_LPAREN_RPAREN_SEMI2      = 200;  // <ExplicitConstructorInvocation> ::= super '(' ')' ';'
		final int PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER                    = 201;  // <InterfaceDeclaration> ::= <Annotations> <Modifiers> interface Identifier <TypeParametersOpt> <ExtendsInterfaces> <InterfaceBody>
		final int PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER2                   = 202;  // <InterfaceDeclaration> ::= <Annotations> <Modifiers> interface Identifier <TypeParametersOpt> <InterfaceBody>
		final int PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER3                   = 203;  // <InterfaceDeclaration> ::= <Annotations> interface Identifier <TypeParametersOpt> <ExtendsInterfaces> <InterfaceBody>
		final int PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER4                   = 204;  // <InterfaceDeclaration> ::= <Annotations> interface Identifier <TypeParametersOpt> <InterfaceBody>
		final int PROD_EXTENDSINTERFACES_EXTENDS                                    = 205;  // <ExtendsInterfaces> ::= extends <InterfaceType>
		final int PROD_EXTENDSINTERFACES_COMMA                                      = 206;  // <ExtendsInterfaces> ::= <ExtendsInterfaces> ',' <InterfaceType>
		final int PROD_INTERFACEBODY_LBRACE_RBRACE                                  = 207;  // <InterfaceBody> ::= '{' <InterfaceMemberDeclarations> '}'
		final int PROD_INTERFACEBODY_LBRACE_RBRACE2                                 = 208;  // <InterfaceBody> ::= '{' '}'
		final int PROD_INTERFACEMEMBERDECLARATIONS                                  = 209;  // <InterfaceMemberDeclarations> ::= <InterfaceMemberDeclaration>
		final int PROD_INTERFACEMEMBERDECLARATIONS2                                 = 210;  // <InterfaceMemberDeclarations> ::= <InterfaceMemberDeclarations> <InterfaceMemberDeclaration>
		final int PROD_INTERFACEMEMBERDECLARATION                                   = 211;  // <InterfaceMemberDeclaration> ::= <ConstantDeclaration>
		final int PROD_INTERFACEMEMBERDECLARATION2                                  = 212;  // <InterfaceMemberDeclaration> ::= <MethodDeclaration>
		final int PROD_INTERFACEMEMBERDECLARATION3                                  = 213;  // <InterfaceMemberDeclaration> ::= <ClassDeclaration>
		final int PROD_INTERFACEMEMBERDECLARATION4                                  = 214;  // <InterfaceMemberDeclaration> ::= <InterfaceDeclaration>
		final int PROD_CONSTANTDECLARATION                                          = 215;  // <ConstantDeclaration> ::= <FieldDeclaration>
		final int PROD_ARRAYINITIALIZER_LBRACE_COMMA_RBRACE                         = 216;  // <ArrayInitializer> ::= '{' <VariableInitializers> ',' '}'
		final int PROD_ARRAYINITIALIZER_LBRACE_RBRACE                               = 217;  // <ArrayInitializer> ::= '{' <VariableInitializers> '}'
		final int PROD_ARRAYINITIALIZER_LBRACE_COMMA_RBRACE2                        = 218;  // <ArrayInitializer> ::= '{' ',' '}'
		final int PROD_ARRAYINITIALIZER_LBRACE_RBRACE2                              = 219;  // <ArrayInitializer> ::= '{' '}'
		final int PROD_VARIABLEINITIALIZERS                                         = 220;  // <VariableInitializers> ::= <VariableInitializer>
		final int PROD_VARIABLEINITIALIZERS_COMMA                                   = 221;  // <VariableInitializers> ::= <VariableInitializers> ',' <VariableInitializer>
		final int PROD_BLOCK_LBRACE_RBRACE                                          = 222;  // <Block> ::= '{' <BlockStatements> '}'
		final int PROD_BLOCK_LBRACE_RBRACE2                                         = 223;  // <Block> ::= '{' '}'
		final int PROD_BLOCKSTATEMENTS                                              = 224;  // <BlockStatements> ::= <BlockStatement>
		final int PROD_BLOCKSTATEMENTS2                                             = 225;  // <BlockStatements> ::= <BlockStatements> <BlockStatement>
		final int PROD_BLOCKSTATEMENT                                               = 226;  // <BlockStatement> ::= <LocalVariableDeclarationStatement>
		final int PROD_BLOCKSTATEMENT2                                              = 227;  // <BlockStatement> ::= <LocalClassDeclaration>
		final int PROD_BLOCKSTATEMENT3                                              = 228;  // <BlockStatement> ::= <Statement>
		final int PROD_LOCALVARIABLEDECLARATIONSTATEMENT_SEMI                       = 229;  // <LocalVariableDeclarationStatement> ::= <LocalVariableDeclaration> ';'
		final int PROD_LOCALVARIABLEDECLARATION_FINAL                               = 230;  // <LocalVariableDeclaration> ::= final <Type> <VariableDeclarators>
		final int PROD_LOCALVARIABLEDECLARATION                                     = 231;  // <LocalVariableDeclaration> ::= <Type> <VariableDeclarators>
		final int PROD_LOCALCLASSDECLARATION                                        = 232;  // <LocalClassDeclaration> ::= <LocalClassModifiers> <PureClassDeclaration>
		final int PROD_LOCALCLASSDECLARATION2                                       = 233;  // <LocalClassDeclaration> ::= <PureClassDeclaration>
		final int PROD_LOCALCLASSMODIFIERS_ABSTRACT                                 = 234;  // <LocalClassModifiers> ::= abstract
		final int PROD_LOCALCLASSMODIFIERS_FINAL                                    = 235;  // <LocalClassModifiers> ::= final
		final int PROD_STATEMENT                                                    = 236;  // <Statement> ::= <StatementWithoutTrailingSubstatement>
		final int PROD_STATEMENT2                                                   = 237;  // <Statement> ::= <LabeledStatement>
		final int PROD_STATEMENT3                                                   = 238;  // <Statement> ::= <IfThenStatement>
		final int PROD_STATEMENT4                                                   = 239;  // <Statement> ::= <IfThenElseStatement>
		final int PROD_STATEMENT5                                                   = 240;  // <Statement> ::= <WhileStatement>
		final int PROD_STATEMENT6                                                   = 241;  // <Statement> ::= <ForStatement>
		final int PROD_STATEMENTNOSHORTIF                                           = 242;  // <StatementNoShortIf> ::= <StatementWithoutTrailingSubstatement>
		final int PROD_STATEMENTNOSHORTIF2                                          = 243;  // <StatementNoShortIf> ::= <LabeledStatementNoShortIf>
		final int PROD_STATEMENTNOSHORTIF3                                          = 244;  // <StatementNoShortIf> ::= <IfThenElseStatementNoShortIf>
		final int PROD_STATEMENTNOSHORTIF4                                          = 245;  // <StatementNoShortIf> ::= <WhileStatementNoShortIf>
		final int PROD_STATEMENTNOSHORTIF5                                          = 246;  // <StatementNoShortIf> ::= <ForStatementNoShortIf>
		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT                         = 247;  // <StatementWithoutTrailingSubstatement> ::= <Block>
		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT2                        = 248;  // <StatementWithoutTrailingSubstatement> ::= <EmptyStatement>
		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT3                        = 249;  // <StatementWithoutTrailingSubstatement> ::= <ExpressionStatement>
		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT4                        = 250;  // <StatementWithoutTrailingSubstatement> ::= <SwitchStatement>
		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT5                        = 251;  // <StatementWithoutTrailingSubstatement> ::= <DoStatement>
		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT6                        = 252;  // <StatementWithoutTrailingSubstatement> ::= <BreakStatement>
		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT7                        = 253;  // <StatementWithoutTrailingSubstatement> ::= <ContinueStatement>
		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT8                        = 254;  // <StatementWithoutTrailingSubstatement> ::= <ReturnStatement>
		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT9                        = 255;  // <StatementWithoutTrailingSubstatement> ::= <SynchronizedStatement>
		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT10                       = 256;  // <StatementWithoutTrailingSubstatement> ::= <ThrowStatement>
		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT11                       = 257;  // <StatementWithoutTrailingSubstatement> ::= <TryStatement>
		final int PROD_EMPTYSTATEMENT_SEMI                                          = 258;  // <EmptyStatement> ::= ';'
		final int PROD_LABELEDSTATEMENT_IDENTIFIER_COLON                            = 259;  // <LabeledStatement> ::= Identifier ':' <Statement>
		final int PROD_LABELEDSTATEMENTNOSHORTIF_IDENTIFIER_COLON                   = 260;  // <LabeledStatementNoShortIf> ::= Identifier ':' <StatementNoShortIf>
		final int PROD_EXPRESSIONSTATEMENT_SEMI                                     = 261;  // <ExpressionStatement> ::= <StatementExpression> ';'
		final int PROD_STATEMENTEXPRESSION                                          = 262;  // <StatementExpression> ::= <Assignment>
		final int PROD_STATEMENTEXPRESSION2                                         = 263;  // <StatementExpression> ::= <PreIncrementExpression>
		final int PROD_STATEMENTEXPRESSION3                                         = 264;  // <StatementExpression> ::= <PreDecrementExpression>
		final int PROD_STATEMENTEXPRESSION4                                         = 265;  // <StatementExpression> ::= <PostIncrementExpression>
		final int PROD_STATEMENTEXPRESSION5                                         = 266;  // <StatementExpression> ::= <PostDecrementExpression>
		final int PROD_STATEMENTEXPRESSION6                                         = 267;  // <StatementExpression> ::= <MethodInvocation>
		final int PROD_STATEMENTEXPRESSION7                                         = 268;  // <StatementExpression> ::= <ClassInstanceCreationExpression>
		final int PROD_IFTHENSTATEMENT_IF_LPAREN_RPAREN                             = 269;  // <IfThenStatement> ::= if '(' <Expression> ')' <Statement>
		final int PROD_IFTHENELSESTATEMENT_IF_LPAREN_RPAREN_ELSE                    = 270;  // <IfThenElseStatement> ::= if '(' <Expression> ')' <StatementNoShortIf> else <Statement>
		final int PROD_IFTHENELSESTATEMENTNOSHORTIF_IF_LPAREN_RPAREN_ELSE           = 271;  // <IfThenElseStatementNoShortIf> ::= if '(' <Expression> ')' <StatementNoShortIf> else <StatementNoShortIf>
		final int PROD_SWITCHSTATEMENT_SWITCH_LPAREN_RPAREN                         = 272;  // <SwitchStatement> ::= switch '(' <Expression> ')' <SwitchBlock>
		final int PROD_SWITCHBLOCK_LBRACE_RBRACE                                    = 273;  // <SwitchBlock> ::= '{' <SwitchBlockStatementGroups> <SwitchLabels> '}'
		final int PROD_SWITCHBLOCK_LBRACE_RBRACE2                                   = 274;  // <SwitchBlock> ::= '{' <SwitchBlockStatementGroups> '}'
		final int PROD_SWITCHBLOCK_LBRACE_RBRACE3                                   = 275;  // <SwitchBlock> ::= '{' <SwitchLabels> '}'
		final int PROD_SWITCHBLOCK_LBRACE_RBRACE4                                   = 276;  // <SwitchBlock> ::= '{' '}'
		final int PROD_SWITCHBLOCKSTATEMENTGROUPS                                   = 277;  // <SwitchBlockStatementGroups> ::= <SwitchBlockStatementGroup>
		final int PROD_SWITCHBLOCKSTATEMENTGROUPS2                                  = 278;  // <SwitchBlockStatementGroups> ::= <SwitchBlockStatementGroups> <SwitchBlockStatementGroup>
		final int PROD_SWITCHBLOCKSTATEMENTGROUP                                    = 279;  // <SwitchBlockStatementGroup> ::= <SwitchLabels> <BlockStatements>
		final int PROD_SWITCHLABELS                                                 = 280;  // <SwitchLabels> ::= <SwitchLabel>
		final int PROD_SWITCHLABELS2                                                = 281;  // <SwitchLabels> ::= <SwitchLabels> <SwitchLabel>
		final int PROD_SWITCHLABEL_CASE_COLON                                       = 282;  // <SwitchLabel> ::= case <ConstantExpression> ':'
		final int PROD_SWITCHLABEL_DEFAULT_COLON                                    = 283;  // <SwitchLabel> ::= default ':'
		final int PROD_WHILESTATEMENT_WHILE_LPAREN_RPAREN                           = 284;  // <WhileStatement> ::= while '(' <Expression> ')' <Statement>
		final int PROD_WHILESTATEMENTNOSHORTIF_WHILE_LPAREN_RPAREN                  = 285;  // <WhileStatementNoShortIf> ::= while '(' <Expression> ')' <StatementNoShortIf>
		final int PROD_DOSTATEMENT_DO_WHILE_LPAREN_RPAREN_SEMI                      = 286;  // <DoStatement> ::= do <Statement> while '(' <Expression> ')' ';'
		final int PROD_FORSTATEMENT                                                 = 287;  // <ForStatement> ::= <BasicForStatement>
		final int PROD_FORSTATEMENT2                                                = 288;  // <ForStatement> ::= <EnhancedForStatement>
		final int PROD_BASICFORSTATEMENT_FOR_LPAREN_SEMI_SEMI_RPAREN                = 289;  // <BasicForStatement> ::= for '(' <ForInitOpt> ';' <ExpressionOpt> ';' <ForUpdateOpt> ')' <Statement>
		final int PROD_EXPRESSIONOPT                                                = 290;  // <ExpressionOpt> ::= <Expression>
		final int PROD_EXPRESSIONOPT2                                               = 291;  // <ExpressionOpt> ::= 
		final int PROD_FORSTATEMENTNOSHORTIF                                        = 292;  // <ForStatementNoShortIf> ::= <BasicForStatementNoShortIf>
		final int PROD_FORSTATEMENTNOSHORTIF2                                       = 293;  // <ForStatementNoShortIf> ::= <EnhancedForStatementNoShortIf>
		final int PROD_BASICFORSTATEMENTNOSHORTIF_FOR_LPAREN_SEMI_SEMI_RPAREN       = 294;  // <BasicForStatementNoShortIf> ::= for '(' <ForInitOpt> ';' <ExpressionOpt> ';' <ForUpdateOpt> ')' <StatementNoShortIf>
		final int PROD_FORINITOPT                                                   = 295;  // <ForInitOpt> ::= <StatementExpressionList>
		final int PROD_FORINITOPT2                                                  = 296;  // <ForInitOpt> ::= <LocalVariableDeclaration>
		final int PROD_FORINITOPT3                                                  = 297;  // <ForInitOpt> ::= 
		final int PROD_FORUPDATEOPT                                                 = 298;  // <ForUpdateOpt> ::= <StatementExpressionList>
		final int PROD_FORUPDATEOPT2                                                = 299;  // <ForUpdateOpt> ::= 
		final int PROD_ENHANCEDFORSTATEMENT_FOR_LPAREN_FINAL_COLON_RPAREN           = 300;  // <EnhancedForStatement> ::= for '(' final <Type> <VariableDeclaratorId> ':' <Expression> ')' <Statement>
		final int PROD_ENHANCEDFORSTATEMENT_FOR_LPAREN_COLON_RPAREN                 = 301;  // <EnhancedForStatement> ::= for '(' <Type> <VariableDeclaratorId> ':' <Expression> ')' <Statement>
		final int PROD_ENHANCEDFORSTATEMENTNOSHORTIF_FOR_LPAREN_FINAL_COLON_RPAREN  = 302;  // <EnhancedForStatementNoShortIf> ::= for '(' final <Type> <VariableDeclaratorId> ':' <Expression> ')' <StatementNoShortIf>
		final int PROD_ENHANCEDFORSTATEMENTNOSHORTIF_FOR_LPAREN_COLON_RPAREN        = 303;  // <EnhancedForStatementNoShortIf> ::= for '(' <Type> <VariableDeclaratorId> ':' <Expression> ')' <StatementNoShortIf>
		final int PROD_STATEMENTEXPRESSIONLIST                                      = 304;  // <StatementExpressionList> ::= <StatementExpression>
		final int PROD_STATEMENTEXPRESSIONLIST_COMMA                                = 305;  // <StatementExpressionList> ::= <StatementExpressionList> ',' <StatementExpression>
		final int PROD_BREAKSTATEMENT_BREAK_IDENTIFIER_SEMI                         = 306;  // <BreakStatement> ::= break Identifier ';'
		final int PROD_BREAKSTATEMENT_BREAK_SEMI                                    = 307;  // <BreakStatement> ::= break ';'
		final int PROD_CONTINUESTATEMENT_CONTINUE_IDENTIFIER_SEMI                   = 308;  // <ContinueStatement> ::= continue Identifier ';'
		final int PROD_CONTINUESTATEMENT_CONTINUE_SEMI                              = 309;  // <ContinueStatement> ::= continue ';'
		final int PROD_RETURNSTATEMENT_RETURN_SEMI                                  = 310;  // <ReturnStatement> ::= return <Expression> ';'
		final int PROD_RETURNSTATEMENT_RETURN_SEMI2                                 = 311;  // <ReturnStatement> ::= return ';'
		final int PROD_THROWSTATEMENT_THROW_SEMI                                    = 312;  // <ThrowStatement> ::= throw <Expression> ';'
		final int PROD_SYNCHRONIZEDSTATEMENT_SYNCHRONIZED_LPAREN_RPAREN             = 313;  // <SynchronizedStatement> ::= synchronized '(' <Expression> ')' <Block>
		final int PROD_TRYSTATEMENT_TRY                                             = 314;  // <TryStatement> ::= try <Block> <Catches>
		final int PROD_TRYSTATEMENT_TRY2                                            = 315;  // <TryStatement> ::= try <Block> <Catches> <Finally>
		final int PROD_TRYSTATEMENT_TRY3                                            = 316;  // <TryStatement> ::= try <Block> <Finally>
		final int PROD_CATCHES                                                      = 317;  // <Catches> ::= <CatchClause>
		final int PROD_CATCHES2                                                     = 318;  // <Catches> ::= <Catches> <CatchClause>
		final int PROD_CATCHCLAUSE_CATCH_LPAREN_RPAREN                              = 319;  // <CatchClause> ::= catch '(' <FormalParameter> ')' <Block>
		final int PROD_FINALLY_FINALLY                                              = 320;  // <Finally> ::= finally <Block>
		final int PROD_PRIMARY                                                      = 321;  // <Primary> ::= <PrimaryNoNewArray>
		final int PROD_PRIMARY2                                                     = 322;  // <Primary> ::= <ArrayCreationExpression>
		final int PROD_PRIMARYNONEWARRAY                                            = 323;  // <PrimaryNoNewArray> ::= <Literal>
		final int PROD_PRIMARYNONEWARRAY_THIS                                       = 324;  // <PrimaryNoNewArray> ::= this
		final int PROD_PRIMARYNONEWARRAY_LPAREN_RPAREN                              = 325;  // <PrimaryNoNewArray> ::= '(' <Expression> ')'
		final int PROD_PRIMARYNONEWARRAY2                                           = 326;  // <PrimaryNoNewArray> ::= <ClassInstanceCreationExpression>
		final int PROD_PRIMARYNONEWARRAY3                                           = 327;  // <PrimaryNoNewArray> ::= <FieldAccess>
		final int PROD_PRIMARYNONEWARRAY4                                           = 328;  // <PrimaryNoNewArray> ::= <MethodInvocation>
		final int PROD_PRIMARYNONEWARRAY5                                           = 329;  // <PrimaryNoNewArray> ::= <ArrayAccess>
		final int PROD_CLASSINSTANCECREATIONEXPRESSION_NEW_LPAREN_RPAREN            = 330;  // <ClassInstanceCreationExpression> ::= new <ClassType> '(' <ArgumentList> ')'
		final int PROD_CLASSINSTANCECREATIONEXPRESSION_NEW_LPAREN_RPAREN2           = 331;  // <ClassInstanceCreationExpression> ::= new <ClassType> '(' ')'
		final int PROD_ARGUMENTLIST                                                 = 332;  // <ArgumentList> ::= <Expression>
		final int PROD_ARGUMENTLIST_COMMA                                           = 333;  // <ArgumentList> ::= <ArgumentList> ',' <Expression>
		final int PROD_ARRAYCREATIONEXPRESSION_NEW                                  = 334;  // <ArrayCreationExpression> ::= new <PrimitiveType> <DimExprs> <Dims>
		final int PROD_ARRAYCREATIONEXPRESSION_NEW2                                 = 335;  // <ArrayCreationExpression> ::= new <PrimitiveType> <DimExprs>
		final int PROD_ARRAYCREATIONEXPRESSION_NEW3                                 = 336;  // <ArrayCreationExpression> ::= new <ClassOrInterfaceType> <DimExprs> <Dims>
		final int PROD_ARRAYCREATIONEXPRESSION_NEW4                                 = 337;  // <ArrayCreationExpression> ::= new <ClassOrInterfaceType> <DimExprs>
		final int PROD_ARRAYCREATIONEXPRESSION_NEW5                                 = 338;  // <ArrayCreationExpression> ::= new <PrimitiveType> <Dims> <ArrayInitializer>
		final int PROD_ARRAYCREATIONEXPRESSION_NEW6                                 = 339;  // <ArrayCreationExpression> ::= new <ClassOrInterfaceType> <Dims> <ArrayInitializer>
		final int PROD_DIMEXPRS                                                     = 340;  // <DimExprs> ::= <DimExpr>
		final int PROD_DIMEXPRS2                                                    = 341;  // <DimExprs> ::= <DimExprs> <DimExpr>
		final int PROD_DIMEXPR_LBRACKET_RBRACKET                                    = 342;  // <DimExpr> ::= '[' <Expression> ']'
		final int PROD_DIMS_LBRACKET_RBRACKET                                       = 343;  // <Dims> ::= '[' ']'
		final int PROD_DIMS_LBRACKET_RBRACKET2                                      = 344;  // <Dims> ::= <Dims> '[' ']'
		final int PROD_FIELDACCESS_DOT_IDENTIFIER                                   = 345;  // <FieldAccess> ::= <Primary> '.' Identifier
		final int PROD_FIELDACCESS_SUPER_DOT_IDENTIFIER                             = 346;  // <FieldAccess> ::= super '.' Identifier
		final int PROD_METHODINVOCATION_LPAREN_RPAREN                               = 347;  // <MethodInvocation> ::= <Name> '(' <ArgumentList> ')'
		final int PROD_METHODINVOCATION_LPAREN_RPAREN2                              = 348;  // <MethodInvocation> ::= <Name> '(' ')'
		final int PROD_METHODINVOCATION_DOT_IDENTIFIER_LPAREN_RPAREN                = 349;  // <MethodInvocation> ::= <Primary> '.' Identifier '(' <ArgumentList> ')'
		final int PROD_METHODINVOCATION_DOT_IDENTIFIER_LPAREN_RPAREN2               = 350;  // <MethodInvocation> ::= <Primary> '.' Identifier '(' ')'
		final int PROD_METHODINVOCATION_SUPER_DOT_IDENTIFIER_LPAREN_RPAREN          = 351;  // <MethodInvocation> ::= super '.' Identifier '(' <ArgumentList> ')'
		final int PROD_METHODINVOCATION_SUPER_DOT_IDENTIFIER_LPAREN_RPAREN2         = 352;  // <MethodInvocation> ::= super '.' Identifier '(' ')'
		final int PROD_ARRAYACCESS_LBRACKET_RBRACKET                                = 353;  // <ArrayAccess> ::= <Name> '[' <Expression> ']'
		final int PROD_ARRAYACCESS_LBRACKET_RBRACKET2                               = 354;  // <ArrayAccess> ::= <PrimaryNoNewArray> '[' <Expression> ']'
		final int PROD_POSTFIXEXPRESSION                                            = 355;  // <PostfixExpression> ::= <Primary>
		final int PROD_POSTFIXEXPRESSION2                                           = 356;  // <PostfixExpression> ::= <Name>
		final int PROD_POSTFIXEXPRESSION3                                           = 357;  // <PostfixExpression> ::= <PostIncrementExpression>
		final int PROD_POSTFIXEXPRESSION4                                           = 358;  // <PostfixExpression> ::= <PostDecrementExpression>
		final int PROD_POSTINCREMENTEXPRESSION_PLUSPLUS                             = 359;  // <PostIncrementExpression> ::= <PostfixExpression> '++'
		final int PROD_POSTDECREMENTEXPRESSION_MINUSMINUS                           = 360;  // <PostDecrementExpression> ::= <PostfixExpression> '--'
		final int PROD_UNARYEXPRESSION                                              = 361;  // <UnaryExpression> ::= <PreIncrementExpression>
		final int PROD_UNARYEXPRESSION2                                             = 362;  // <UnaryExpression> ::= <PreDecrementExpression>
		final int PROD_UNARYEXPRESSION_PLUS                                         = 363;  // <UnaryExpression> ::= '+' <UnaryExpression>
		final int PROD_UNARYEXPRESSION_MINUS                                        = 364;  // <UnaryExpression> ::= '-' <UnaryExpression>
		final int PROD_UNARYEXPRESSION3                                             = 365;  // <UnaryExpression> ::= <UnaryExpressionNotPlusMinus>
		final int PROD_PREINCREMENTEXPRESSION_PLUSPLUS                              = 366;  // <PreIncrementExpression> ::= '++' <UnaryExpression>
		final int PROD_PREDECREMENTEXPRESSION_MINUSMINUS                            = 367;  // <PreDecrementExpression> ::= '--' <UnaryExpression>
		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS                                  = 368;  // <UnaryExpressionNotPlusMinus> ::= <PostfixExpression>
		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS_TILDE                            = 369;  // <UnaryExpressionNotPlusMinus> ::= '~' <UnaryExpression>
		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS_EXCLAM                           = 370;  // <UnaryExpressionNotPlusMinus> ::= '!' <UnaryExpression>
		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS2                                 = 371;  // <UnaryExpressionNotPlusMinus> ::= <CastExpression>
		final int PROD_CASTEXPRESSION_LPAREN_RPAREN                                 = 372;  // <CastExpression> ::= '(' <PrimitiveType> <Dims> ')' <UnaryExpression>
		final int PROD_CASTEXPRESSION_LPAREN_RPAREN2                                = 373;  // <CastExpression> ::= '(' <PrimitiveType> ')' <UnaryExpression>
		final int PROD_CASTEXPRESSION_LPAREN_RPAREN3                                = 374;  // <CastExpression> ::= '(' <Expression> ')' <UnaryExpressionNotPlusMinus>
		final int PROD_CASTEXPRESSION_LPAREN_RPAREN4                                = 375;  // <CastExpression> ::= '(' <Name> <Dims> ')' <UnaryExpressionNotPlusMinus>
		final int PROD_MULTIPLICATIVEEXPRESSION                                     = 376;  // <MultiplicativeExpression> ::= <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_TIMES                               = 377;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> '*' <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_DIV                                 = 378;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> '/' <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_PERCENT                             = 379;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> '%' <UnaryExpression>
		final int PROD_ADDITIVEEXPRESSION                                           = 380;  // <AdditiveExpression> ::= <MultiplicativeExpression>
		final int PROD_ADDITIVEEXPRESSION_PLUS                                      = 381;  // <AdditiveExpression> ::= <AdditiveExpression> '+' <MultiplicativeExpression>
		final int PROD_ADDITIVEEXPRESSION_MINUS                                     = 382;  // <AdditiveExpression> ::= <AdditiveExpression> '-' <MultiplicativeExpression>
		final int PROD_SHIFTEXPRESSION                                              = 383;  // <ShiftExpression> ::= <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_LTLT                                         = 384;  // <ShiftExpression> ::= <ShiftExpression> '<<' <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_GTGT                                         = 385;  // <ShiftExpression> ::= <ShiftExpression> '>>' <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_GTGTGT                                       = 386;  // <ShiftExpression> ::= <ShiftExpression> '>>>' <AdditiveExpression>
		final int PROD_RELATIONALEXPRESSION                                         = 387;  // <RelationalExpression> ::= <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_LT                                      = 388;  // <RelationalExpression> ::= <RelationalExpression> '<' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_GT                                      = 389;  // <RelationalExpression> ::= <RelationalExpression> '>' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_LTEQ                                    = 390;  // <RelationalExpression> ::= <RelationalExpression> '<=' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_GTEQ                                    = 391;  // <RelationalExpression> ::= <RelationalExpression> '>=' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_INSTANCEOF                              = 392;  // <RelationalExpression> ::= <RelationalExpression> instanceof <ReferenceType>
		final int PROD_EQUALITYEXPRESSION                                           = 393;  // <EqualityExpression> ::= <RelationalExpression>
		final int PROD_EQUALITYEXPRESSION_EQEQ                                      = 394;  // <EqualityExpression> ::= <EqualityExpression> '==' <RelationalExpression>
		final int PROD_EQUALITYEXPRESSION_EXCLAMEQ                                  = 395;  // <EqualityExpression> ::= <EqualityExpression> '!=' <RelationalExpression>
		final int PROD_ANDEXPRESSION                                                = 396;  // <AndExpression> ::= <EqualityExpression>
		final int PROD_ANDEXPRESSION_AMP                                            = 397;  // <AndExpression> ::= <AndExpression> '&' <EqualityExpression>
		final int PROD_EXCLUSIVEOREXPRESSION                                        = 398;  // <ExclusiveOrExpression> ::= <AndExpression>
		final int PROD_EXCLUSIVEOREXPRESSION_CARET                                  = 399;  // <ExclusiveOrExpression> ::= <ExclusiveOrExpression> '^' <AndExpression>
		final int PROD_INCLUSIVEOREXPRESSION                                        = 400;  // <InclusiveOrExpression> ::= <ExclusiveOrExpression>
		final int PROD_INCLUSIVEOREXPRESSION_PIPE                                   = 401;  // <InclusiveOrExpression> ::= <InclusiveOrExpression> '|' <ExclusiveOrExpression>
		final int PROD_CONDITIONALANDEXPRESSION                                     = 402;  // <ConditionalAndExpression> ::= <InclusiveOrExpression>
		final int PROD_CONDITIONALANDEXPRESSION_AMPAMP                              = 403;  // <ConditionalAndExpression> ::= <ConditionalAndExpression> '&&' <InclusiveOrExpression>
		final int PROD_CONDITIONALOREXPRESSION                                      = 404;  // <ConditionalOrExpression> ::= <ConditionalAndExpression>
		final int PROD_CONDITIONALOREXPRESSION_PIPEPIPE                             = 405;  // <ConditionalOrExpression> ::= <ConditionalOrExpression> '||' <ConditionalAndExpression>
		final int PROD_CONDITIONALEXPRESSION                                        = 406;  // <ConditionalExpression> ::= <ConditionalOrExpression>
		final int PROD_CONDITIONALEXPRESSION_QUESTION_COLON                         = 407;  // <ConditionalExpression> ::= <ConditionalOrExpression> '?' <Expression> ':' <ConditionalExpression>
		final int PROD_ASSIGNMENTEXPRESSION                                         = 408;  // <AssignmentExpression> ::= <ConditionalExpression>
		final int PROD_ASSIGNMENTEXPRESSION2                                        = 409;  // <AssignmentExpression> ::= <Assignment>
		final int PROD_ASSIGNMENT                                                   = 410;  // <Assignment> ::= <LeftHandSide> <AssignmentOperator> <AssignmentExpression>
		final int PROD_LEFTHANDSIDE                                                 = 411;  // <LeftHandSide> ::= <Name>
		final int PROD_LEFTHANDSIDE2                                                = 412;  // <LeftHandSide> ::= <FieldAccess>
		final int PROD_LEFTHANDSIDE3                                                = 413;  // <LeftHandSide> ::= <ArrayAccess>
		final int PROD_ASSIGNMENTOPERATOR_EQ                                        = 414;  // <AssignmentOperator> ::= '='
		final int PROD_ASSIGNMENTOPERATOR_TIMESEQ                                   = 415;  // <AssignmentOperator> ::= '*='
		final int PROD_ASSIGNMENTOPERATOR_DIVEQ                                     = 416;  // <AssignmentOperator> ::= '/='
		final int PROD_ASSIGNMENTOPERATOR_PERCENTEQ                                 = 417;  // <AssignmentOperator> ::= '%='
		final int PROD_ASSIGNMENTOPERATOR_PLUSEQ                                    = 418;  // <AssignmentOperator> ::= '+='
		final int PROD_ASSIGNMENTOPERATOR_MINUSEQ                                   = 419;  // <AssignmentOperator> ::= '-='
		final int PROD_ASSIGNMENTOPERATOR_LTLTEQ                                    = 420;  // <AssignmentOperator> ::= '<<='
		final int PROD_ASSIGNMENTOPERATOR_GTGTEQ                                    = 421;  // <AssignmentOperator> ::= '>>='
		final int PROD_ASSIGNMENTOPERATOR_GTGTGTEQ                                  = 422;  // <AssignmentOperator> ::= '>>>='
		final int PROD_ASSIGNMENTOPERATOR_AMPEQ                                     = 423;  // <AssignmentOperator> ::= '&='
		final int PROD_ASSIGNMENTOPERATOR_CARETEQ                                   = 424;  // <AssignmentOperator> ::= '^='
		final int PROD_ASSIGNMENTOPERATOR_PIPEEQ                                    = 425;  // <AssignmentOperator> ::= '|='
		final int PROD_EXPRESSION                                                   = 426;  // <Expression> ::= <AssignmentExpression>
		final int PROD_CONSTANTEXPRESSION                                           = 427;  // <ConstantExpression> ::= <Expression>
	};

	//----------------------------- Preprocessor -----------------------------

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
		boolean wrapInClass = (Boolean)this.getPluginOption("wrap_in_dummy_class", false);
		
		File interm = null;
		try
		{
			File file = new File(_textToParse);
			DataInputStream in = new DataInputStream(new FileInputStream(file));
			// START KGU#193 2016-05-04
			BufferedReader br = new BufferedReader(new InputStreamReader(in, _encoding));
			// END KGU#193 2016-05-04
			String srcCode = new String();
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
					srcCode += strLine + "\n";
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
				ow.write(srcCode.trim()+"\n");
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

	private String packageStr = null;
	private StringList imports = null;
	private Stack<Root> includables = null;
	private Stack<Root> classes = null;
	
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
			String rule = _reduction.getParent().toString();
			String ruleHead = _reduction.getParent().getHead().toString();
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
				packageStr = this.getContent_R(_reduction.get(1).asReduction(), "package ");
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
			case RuleConstants.PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER:
			case RuleConstants.PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER2:
			case RuleConstants.PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER3:
			case RuleConstants.PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER4:
			{
				// <NormalClassDeclaration> ::= <Modifiers> <PureClassDeclaration>
				// <PureClassDeclaration> ::= class Identifier <TypeParametersOpt> <Super> <Interfaces> <ClassBody>
				// <PureClassDeclaration> ::= class Identifier <TypeParametersOpt> <Super> <ClassBody>
				// <PureClassDeclaration> ::= class Identifier <TypeParametersOpt> <Interfaces> <ClassBody>
				// <PureClassDeclaration> ::= class Identifier <TypeParametersOpt> <ClassBody>
				// Fetch comment and append modifiers
				String modifiers = "";
				if (ruleId == RuleConstants.PROD_NORMALCLASSDECLARATION) {
					// Get modifiers (in order to append them to the comment)
					modifiers = this.getContent_R(_reduction.get(0));
					_reduction = _reduction.get(1).asReduction();
					ruleId = _reduction.getParent().getTableIndex();
				}
				// Get the name
				String name = _reduction.get(1).asString();
				Root classRoot = root;
				if (!this.classes.isEmpty()) {
					classRoot = new Root();
					this.addRoot(classRoot);
				}
				classes.push(classRoot);
				
				Root incl = new Root();
				incl.setInclude();
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
				int ixBody = 3;
				// Get the inheritance
				String inh = "";
				if (ruleId != RuleConstants.PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER4) {
					ixBody++;
					inh = this.getContent_R(_reduction.get(3).asReduction(), inh);
					if (ruleId == RuleConstants.PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER) {
						ixBody++;
						inh = this.getContent_R(_reduction.get(4).asReduction(), inh);
					}
				}
				if (this.classes.size() == 1 && packageStr != null) {
					classRoot.getComment().add("==== " + packageStr);
					if (!imports.isEmpty()) {
						imports.insert("==== imports:", 0);
						incl.getComment().add(imports);
					}
				}
				classRoot.getComment().add((modifiers + " class").trim());
				if (!typePars.trim().isEmpty()) {
					classRoot.getComment().add("==== type parameters: " + typePars);
				}
				// Now descend into the body
				this.buildNSD_R(_reduction.get(ixBody).asReduction(), _parentNode);
				this.classes.pop();
				this.includables.pop();
			}
			break;
			case RuleConstants.PROD_FIELDDECLARATION_SEMI:
			case RuleConstants.PROD_FIELDDECLARATION_SEMI2:
			{
				// <FieldDeclaration> ::= <Annotations> <Modifiers> <Type> <VariableDeclarators> ';'
				// <FieldDeclaration> ::= <Annotations> <Type> <VariableDeclarators> ';'
				String modifiers = null;
				int ixDecls = 2;
				if (ruleId == RuleConstants.PROD_FIELDDECLARATION_SEMI) {
					modifiers = this.getContent_R(_reduction.get(1));
					ixDecls++;
				}
				// TODO: Compose the variable declarations
				StringList decls = processVarDeclarators(_reduction.get(ixDecls));
				Element ele = this.equipWithSourceComment(new Instruction(decls), _reduction);
				if (modifiers != null) {
					ele.getComment().add(modifiers);
				}
				includables.peek().children.addElement(ele);
			}
			break;
			case RuleConstants.PROD_METHODDECLARATION:
			{
				// <MethodDeclaration> ::= <Annotations> <MethodHeader> <MethodBody>
				// Ignore the annotations (or add them as comment?)
				Root subRoot = new Root();
				this.equipWithSourceComment(subRoot, _reduction);
				Root targetRoot = subRoot;
				// Extract the method header
				boolean isStdConstructorOrMain = this.applyMethodHeader(
						_reduction.get(1).asReduction(), subRoot, classes.peek().getMethodName());
				if (isStdConstructorOrMain) {
					/* If it is the standard constructor add the body to the includable
					 * FIXME: Is this acceptable if there are other constructors?
					 */
					targetRoot = includables.peek();
					if (subRoot.isProgram()) {
						// Must be "Main" -> add the body elements to the class Root instead
						targetRoot = classes.peek();
					}
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
					addRoot(subRoot);
				}
				// Now build the method body
				this.buildNSD_R(_reduction.get(2).asReduction(), targetRoot.children);
			}
			break;
			case RuleConstants.PROD_IFTHENSTATEMENT_IF_LPAREN_RPAREN:
			case RuleConstants.PROD_IFTHENELSESTATEMENT_IF_LPAREN_RPAREN_ELSE:
			case RuleConstants.PROD_IFTHENELSESTATEMENTNOSHORTIF_IF_LPAREN_RPAREN_ELSE:
			{
				// <IfThenStatement> ::= if '(' <Expression> ')' <Statement>
				// <IfThenElseStatement> ::= if '(' <Expression> ')' <StatementNoShortIf> else <Statement>
				// <IfThenElseStatementNoShortIf> ::= if '(' <Expression> ')' <StatementNoShortIf> else <StatementNoShortIf>
				String cond = getContent_R(_reduction.get(2));
				Alternative alt = new Alternative(cond);
				this.equipWithSourceComment(alt, _reduction);
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
						While loop = new While((getOptKeyword("preWhile", false, true)
								+ translateContent(content)
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
				String valList = this.translateContent(this.getContent_R(_reduction.get(ixType + 3)));
				Element forIn = this.equipWithSourceComment(new For(loopVar, valList), _reduction);
				_parentNode.addElement(forIn);
				this.buildNSD_R(_reduction.get(ixType + 5).asReduction(), ((For)forIn).getBody());
			}
			break;
			case RuleConstants.PROD_WHILESTATEMENT_WHILE_LPAREN_RPAREN:
			case RuleConstants.PROD_WHILESTATEMENTNOSHORTIF_WHILE_LPAREN_RPAREN:
			{
				// <WhileStatement> ::= while '(' <Expression> ')' <Statement>
				// <WhileStatementNoShortIf> ::= while '(' <Expression> ')' <StatementNoShortIf>
				String cond = this.getContent_R(_reduction.get(2));
				While loop = (While)this.equipWithSourceComment(
						new While(getOptKeyword("preWhile", false, true)
								+ this.translateContent(cond)
								+ getOptKeyword("postWhile", true, false)),
						_reduction);
				_parentNode.addElement(loop);
				this.buildNSD_R(_reduction.get(4).asReduction(), loop.getBody());
			}
			break;
			case RuleConstants.PROD_DOSTATEMENT_DO_WHILE_LPAREN_RPAREN_SEMI:
			{
				// <DoStatement> ::= do <Statement> while '(' <Expression> ')' ';'
				String cond = Element.negateCondition(this.getContent_R(_reduction.get(4)));
				Repeat loop = (Repeat)this.equipWithSourceComment(
						new Repeat(getOptKeyword("preRepeat", false, true)
								+ this.translateContent(cond)
								+ getOptKeyword("posRepeat", true, false)),
						_reduction);
				_parentNode.addElement(loop);
				this.buildNSD_R(_reduction.get(1).asReduction(), loop.getBody());
			}
			break;
			default:
				if (_reduction.size()>0)
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
	 * Extracts the methos header from {@link Reduction} {@code _reduction}, prepares
	 * it for Structorizer and applies the information to {@code _subRoot}.<br/>
	 * The method comment is assumed to have been attached already.
	 * @param asReduction - the {@code <MethodHeader>} reduction
	 * @param subRoot - the method diagram to be equipped with a header
	 * @returns {@code true} if the method represents either a constructor for class
	 * {@code _className} or the "Main" method; {@code false} otherwise
	 * @throws ParserCancelled 
	 */
	private boolean applyMethodHeader(Reduction _reduction, Root _subRoot, String _className) throws ParserCancelled
	{
		// PROD_METHODHEADER:  <MethodHeader> ::= <Modifiers> <Type> <MethodDeclarator> <Throws>
		// PROD_METHODHEADER2: <MethodHeader> ::= <Modifiers> <Type> <MethodDeclarator>
		// PROD_METHODHEADER3: <MethodHeader> ::= <Type> <MethodDeclarator> <Throws>
		// PROD_METHODHEADER4: <MethodHeader> ::= <Type> <MethodDeclarator>
		// PROD_METHODHEADER_VOID:  <MethodHeader> ::= <Modifiers> void <MethodDeclarator> <Throws>
		// PROD_METHODHEADER_VOID2: <MethodHeader> ::= <Modifiers> void <MethodDeclarator>
		// PROD_METHODHEADER_VOID3: <MethodHeader> ::= void <MethodDeclarator> <Throws>
		// PROD_METHODHEADER_VOID4: <MethodHeader> ::= void <MethodDeclarator>
		int ixType = 0;
		int ruleId = _reduction.getParent().getTableIndex();
		if (ruleId == RuleConstants.PROD_METHODHEADER
				|| ruleId == RuleConstants.PROD_METHODHEADER2
				|| ruleId == RuleConstants.PROD_METHODHEADER_VOID
				|| ruleId == RuleConstants.PROD_METHODHEADER_VOID2) {
			String modifiers = this.getContent_R(_reduction.get(0));
			if (!modifiers.trim().isEmpty()) {
				_subRoot.getComment().add(modifiers);
			}
			ixType++;
		}
		String resultType = null;
		if (ruleId == RuleConstants.PROD_METHODHEADER
				|| ruleId == RuleConstants.PROD_METHODHEADER2
				|| ruleId == RuleConstants.PROD_METHODHEADER3
				|| ruleId == RuleConstants.PROD_METHODHEADER4) {
			resultType = translateType(_reduction.get(ixType));
		}
		// TODO get the name and arguments
		String name = "???";
		int nArgs = 0;
//		final int PROD_METHODDECLARATOR_IDENTIFIER_LPAREN_RPAREN                    = 175;  // <MethodDeclarator> ::= Identifier '(' <FormalParameterList> ')'
//		final int PROD_METHODDECLARATOR_IDENTIFIER_LPAREN_RPAREN2                   = 176;  // <MethodDeclarator> ::= Identifier '(' ')'
//		final int PROD_METHODDECLARATOR_LBRACKET_RBRACKET                           = 177;  // <MethodDeclarator> ::= <MethodDeclarator> '[' ']'
		
		if (name.equals("Main") && nArgs == 0) {
			_subRoot.setProgram(true);
		}
		if (_reduction.size() > ixType + 2) {
			// Add the throws clause to the comment
			_subRoot.getComment().add("==== " + this.getContent_R(_reduction.get(ixType + 2)));
		}
		return name.equals(_className) || name.equals("Main");
	}

	/**
	 * @param token
	 * @return
	 */
	private String translateType(Token token) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param token
	 * @return
	 */
	private StringList processVarDeclarators(Token token) {
		// <VariableDeclarators> ::= <VariableDeclarators> ',' <VariableDeclarator>
		// <VariableDeclarators> ::= <VariableDeclarator>
		StringList declarators = new StringList();
		if (token.getType() == SymbolType.NON_TERMINAL) {
			Reduction declRed = token.asReduction();
			int ruleId = declRed.getParent().getTableIndex();
			while (declRed != null) {
				
				if (ruleId == RuleConstants.PROD_VARIABLEDECLARATORS_COMMA) {
					// FIXME!!!
				}
			}
		}
		return declarators;
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
		
		// TODO
		/* -------- Begin code example for C code import -------- */
//		_content = _content.replaceAll(BString.breakup("printf")+"[ ((](.*?)[))]", output+"$1");
//		_content = _content.replaceAll(BString.breakup("scanf")+"[ ((](.*?),[ ]*[&]?(.*?)[))]", input+"$2");
		/* -------- End code example for C code import -------- */
		
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
	
	/**
	 * Convenience method for the string content retrieval from a {@link Token}
	 * that may be either reprsent a content symbol or a {@link Reduction}.
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
			StringList tailLabels = extractCaseLabels(sr.get(2));
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
		StringList caseLabels = extractCaseLabels(stmGroup.get(0));
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
	 * @param token
	 * @return
	 */
	private StringList extractCaseLabels(Token token) {
		// TODO Auto-generated method stub
		return null;
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
									&& this.getContent_R(rightRed.get(0).asReduction(), "").trim().equals(parts[0])) {
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
							initRed.get(0).asReduction().getParent().getTableIndex() != RuleConstants.PROD_VARIABLEDECLARATORID_LBRACKET_RBRACKET &&
							id.equals(this.getContent_R(initRed.get(0).asReduction(), "").trim())) {
						firstVal = this.getContent_R(initRed.get(2).asReduction(), "");
					}
				}
				//this.analyseDeclaration(_reduction, _pascalType, _parentNode, _forceDecl, _something)
			}
		}
		return firstVal;
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

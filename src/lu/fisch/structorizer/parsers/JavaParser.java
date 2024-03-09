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
 *      Kay Gürtzig     2017-03-02      First Issue for Iden GOLDEngine
 *      Kay Gürtzig     2017-03-11      Parameter annotations and some comments corrected, indentation unified
 *      Kay Gürtzig     2018-03-26      Imports revised
 *      Kay Gürtzig     2018-06-30      Enh. #553: hooks for possible thread cancellation inserted.
 *      Kay Gürtzig     2021-02-17      Some updates to interface changes and e.g. date formatting 
 *
 ******************************************************************************************************
 *
 *      Revision List (this parser)
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2021-02-16      First Issue (generated with GOLDprog.exe)
 *      Kay Gürtzig     2021-02-23      Functionally complete implementation
 *      Kay Gürtzig     2021-02-26      Revision of the class and method handling, minor fixes
 *      Kay Gürtzig     2021-02-27      Supporting method hooks for ProcessingParser inserted.
 *      Kay Gürtzig     2021-03-04      Bugfix #955 ".class" replacement in file preparation didn't work,
 *                                      Multi-catch clauses let the parser fail (and must be converted)
 *                                      Issue #956 omit declarations if optionImportVarDecl is false,
 *                                      don't decompose expressions if optionTranslate is off
 *      Kay Gürtzig     2021-03-05      Bugfix #959: Grammar tweaked to let Processing converters pass;
 *                                      bugfix #961: The conversion of output instructions had not worked
 *      Kay Gürtzig     2021-03-06      Bugfix #962: Constructor bodies had not been imported,
 *                                      KGU#961: Array initialiser conversion in declarations improved
 *      Kay Gürtzig     2023-11-08      Bugfix #1110 method translateContent() returned the argument instead of the result
 *      Kay Gürtzig     2024-03-08      KGU#1117: Missing backward replacement of c_l_a_s_s in one case mended.
 *      Kay Gürtzig     2024-03-09      Issue #1131: Handling of anonymous inner class instantiations
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
import java.util.Stack;
import java.util.logging.Level;

import com.creativewidgetworks.goldparser.engine.*;
import com.creativewidgetworks.goldparser.engine.enums.SymbolType;

import lu.fisch.structorizer.elements.Alternative;
import lu.fisch.structorizer.elements.Call;
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
import lu.fisch.structorizer.executor.Function;
import lu.fisch.utils.StringList;

/**
 * Code import parser class of Structorizer 3.27, based on GOLDParser 5.0 for the Java SE 8 language.
 * This file contains grammar-specific constants and individual routines to build
 * structograms (Nassi-Shneiderman diagrams) from the parsing tree. 
 * @author Kay Gürtzig
 */
public class JavaParser extends CodeParser
{

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
	//@SuppressWarnings("unused")
	private interface SymbolConstants 
	{
//		final int SYM_EOF                                  =   0;  // (EOF)
//		final int SYM_ERROR                                =   1;  // (Error)
//		final int SYM_COMMENT                              =   2;  // Comment
//		final int SYM_NEWLINE                              =   3;  // NewLine
//		final int SYM_WHITESPACE                           =   4;  // Whitespace
//		final int SYM_TIMESDIV                             =   5;  // '*/'
//		final int SYM_DIVTIMES                             =   6;  // '/*'
//		final int SYM_DIVDIV                               =   7;  // '//'
		final int SYM_MINUS                                =   8;  // '-'
//		final int SYM_MINUSMINUS                           =   9;  // '--'
		final int SYM_EXCLAM                               =  10;  // '!'
		final int SYM_EXCLAMEQ                             =  11;  // '!='
		final int SYM_PERCENT                              =  12;  // '%'
//		final int SYM_PERCENTEQ                            =  13;  // '%='
		final int SYM_AMP                                  =  14;  // '&'
		final int SYM_AMPAMP                               =  15;  // '&&'
//		final int SYM_AMPEQ                                =  16;  // '&='
//		final int SYM_LPAREN                               =  17;  // '('
//		final int SYM_RPAREN                               =  18;  // ')'
		final int SYM_TIMES                                =  19;  // '*'
//		final int SYM_TIMESEQ                              =  20;  // '*='
//		final int SYM_COMMA                                =  21;  // ','
//		final int SYM_DOT                                  =  22;  // '.'
		final int SYM_DIV                                  =  23;  // '/'
//		final int SYM_DIVEQ                                =  24;  // '/='
//		final int SYM_COLON                                =  25;  // ':'
//		final int SYM_SEMI                                 =  26;  // ';'
//		final int SYM_QUESTION                             =  27;  // '?'
//		final int SYM_AT                                   =  28;  // '@'
//		final int SYM_LBRACKET                             =  29;  // '['
//		final int SYM_RBRACKET                             =  30;  // ']'
		final int SYM_CARET                                =  31;  // '^'
//		final int SYM_CARETEQ                              =  32;  // '^='
		final int SYM_LBRACE                               =  33;  // '{'
		final int SYM_PIPE                                 =  34;  // '|'
		final int SYM_PIPEPIPE                             =  35;  // '||'
//		final int SYM_PIPEEQ                               =  36;  // '|='
		final int SYM_RBRACE                               =  37;  // '}'
		final int SYM_TILDE                                =  38;  // '~'
		final int SYM_PLUS                                 =  39;  // '+'
//		final int SYM_PLUSPLUS                             =  40;  // '++'
//		final int SYM_PLUSEQ                               =  41;  // '+='
		final int SYM_LT                                   =  42;  // '<'
		final int SYM_LTLT                                 =  43;  // '<<'
//		final int SYM_LTLTEQ                               =  44;  // '<<='
		final int SYM_LTEQ                                 =  45;  // '<='
		final int SYM_EQ                                   =  46;  // '='
//		final int SYM_MINUSEQ                              =  47;  // '-='
		final int SYM_EQEQ                                 =  48;  // '=='
		final int SYM_GT                                   =  49;  // '>'
		final int SYM_GTEQ                                 =  50;  // '>='
		final int SYM_GTGT                                 =  51;  // '>>'
//		final int SYM_GTGTEQ                               =  52;  // '>>='
//		final int SYM_GTGTGT                               =  53;  // '>>>'
//		final int SYM_GTGTGTEQ                             =  54;  // '>>>='
//		final int SYM_ABSTRACT                             =  55;  // abstract
//		final int SYM_BINARY                               =  56;  // binary
//		final int SYM_BOOLEAN                              =  57;  // boolean
//		final int SYM_BOOLEANLITERAL                       =  58;  // BooleanLiteral
//		final int SYM_BREAK                                =  59;  // break
//		final int SYM_BYTE                                 =  60;  // byte
//		final int SYM_CASE                                 =  61;  // case
//		final int SYM_CATCH                                =  62;  // catch
//		final int SYM_CHAR                                 =  63;  // char
//		final int SYM_CLASS                                =  64;  // class
//		final int SYM_CONTINUE                             =  65;  // continue
//		final int SYM_DEFAULT                              =  66;  // default
//		final int SYM_DO                                   =  67;  // do
//		final int SYM_DOUBLE                               =  68;  // double
//		final int SYM_ELLIPSIS                             =  69;  // Ellipsis
//		final int SYM_ELSE                                 =  70;  // else
//		final int SYM_ENUM                                 =  71;  // enum
//		final int SYM_EXTENDS                              =  72;  // extends
//		final int SYM_FINAL                                =  73;  // final
//		final int SYM_FINALLY                              =  74;  // finally
//		final int SYM_FLOAT                                =  75;  // float
		final int SYM_FLOATINGPOINTLITERAL                 =  76;  // FloatingPointLiteral
//		final int SYM_FLOATINGPOINTLITERALEXPONENT         =  77;  // FloatingPointLiteralExponent
//		final int SYM_FOR                                  =  78;  // for
//		final int SYM_HEX                                  =  79;  // hex
//		final int SYM_HEXESCAPECHARLITERAL                 =  80;  // HexEscapeCharLiteral
		final int SYM_HEXINTEGERLITERAL                    =  81;  // HexIntegerLiteral
//		final int SYM_IDENTIFIER                           =  82;  // Identifier
//		final int SYM_IF                                   =  83;  // if
//		final int SYM_IMPLEMENTS                           =  84;  // implements
//		final int SYM_IMPORT                               =  85;  // import
//		final int SYM_INDIRECTCHARLITERAL                  =  86;  // IndirectCharLiteral
//		final int SYM_INSTANCEOF                           =  87;  // instanceof
//		final int SYM_INT                                  =  88;  // int
//		final int SYM_INTERFACE                            =  89;  // interface
//		final int SYM_LONG                                 =  90;  // long
//		final int SYM_NATIVE                               =  91;  // native
//		final int SYM_NEW                                  =  92;  // new
//		final int SYM_NULLLITERAL                          =  93;  // NullLiteral
//		final int SYM_OCTALESCAPECHARLITERAL               =  94;  // OctalEscapeCharLiteral
		final int SYM_OCTALINTEGERLITERAL                  =  95;  // OctalIntegerLiteral
//		final int SYM_PACKAGE                              =  96;  // package
//		final int SYM_PRIVATE                              =  97;  // private
//		final int SYM_PROTECTED                            =  98;  // protected
//		final int SYM_PUBLIC                               =  99;  // public
//		final int SYM_RETURN                               = 100;  // return
//		final int SYM_SHORT                                = 101;  // short
//		final int SYM_STANDARDESCAPECHARLITERAL            = 102;  // StandardEscapeCharLiteral
//		final int SYM_STARTWITHNOZERODECIMALINTEGERLITERAL = 103;  // StartWithNoZeroDecimalIntegerLiteral
//		final int SYM_STARTWITHZERODECIMALINTEGERLITERAL   = 104;  // StartWithZeroDecimalIntegerLiteral
//		final int SYM_STATIC                               = 105;  // static
//		final int SYM_STR                                  = 106;  // str
//		final int SYM_STRICTFP                             = 107;  // strictfp
		final int SYM_STRINGLITERAL                        = 108;  // StringLiteral
//		final int SYM_SUPER                                = 109;  // super
//		final int SYM_SWITCH                               = 110;  // switch
//		final int SYM_SYNCHRONIZED                         = 111;  // synchronized
//		final int SYM_THIS                                 = 112;  // this
//		final int SYM_THROW                                = 113;  // throw
//		final int SYM_THROWS                               = 114;  // throws
//		final int SYM_TRANSIENT                            = 115;  // transient
//		final int SYM_TRY                                  = 116;  // try
//		final int SYM_UNBINARY                             = 117;  // unbinary
//		final int SYM_UNHEX                                = 118;  // unhex
//		final int SYM_VOID                                 = 119;  // void
//		final int SYM_VOLATILE                             = 120;  // volatile
//		final int SYM_WEBCOLORLITERAL                      = 121;  // WebColorLiteral
//		final int SYM_WHILE                                = 122;  // while
//		final int SYM_ADDITIONALBOUNDOPT                   = 123;  // <AdditionalBoundOpt>
//		final int SYM_ADDITIVEEXPRESSION                   = 124;  // <AdditiveExpression>
//		final int SYM_ANDEXPRESSION                        = 125;  // <AndExpression>
//		final int SYM_ANNOTATION                           = 126;  // <Annotation>
//		final int SYM_ANNOTATIONS                          = 127;  // <Annotations>
//		final int SYM_ARGUMENTLIST                         = 128;  // <ArgumentList>
//		final int SYM_ARRAYACCESS                          = 129;  // <ArrayAccess>
//		final int SYM_ARRAYCREATIONEXPRESSION              = 130;  // <ArrayCreationExpression>
//		final int SYM_ARRAYINITIALIZER                     = 131;  // <ArrayInitializer>
//		final int SYM_ARRAYTYPE                            = 132;  // <ArrayType>
//		final int SYM_ASSIGNMENT                           = 133;  // <Assignment>
//		final int SYM_ASSIGNMENTEXPRESSION                 = 134;  // <AssignmentExpression>
//		final int SYM_ASSIGNMENTOPERATOR                   = 135;  // <AssignmentOperator>
//		final int SYM_BASICFORSTATEMENT                    = 136;  // <BasicForStatement>
//		final int SYM_BASICFORSTATEMENTNOSHORTIF           = 137;  // <BasicForStatementNoShortIf>
//		final int SYM_BLOCK                                = 138;  // <Block>
//		final int SYM_BLOCKSTATEMENT                       = 139;  // <BlockStatement>
//		final int SYM_BLOCKSTATEMENTS                      = 140;  // <BlockStatements>
//		final int SYM_BREAKSTATEMENT                       = 141;  // <BreakStatement>
//		final int SYM_CASTEXPRESSION                       = 142;  // <CastExpression>
//		final int SYM_CATCHCLAUSE                          = 143;  // <CatchClause>
//		final int SYM_CATCHES                              = 144;  // <Catches>
//		final int SYM_CATCHFORMALPARAMETER                 = 145;  // <CatchFormalParameter>
//		final int SYM_CATCHTYPE                            = 146;  // <CatchType>
//		final int SYM_CHARACTERLITERAL                     = 147;  // <CharacterLiteral>
//		final int SYM_CLASSBODY                            = 148;  // <ClassBody>
//		final int SYM_CLASSBODYDECLARATION                 = 149;  // <ClassBodyDeclaration>
//		final int SYM_CLASSBODYDECLARATIONS                = 150;  // <ClassBodyDeclarations>
//		final int SYM_CLASSBODYOPT                         = 151;  // <ClassBodyOpt>
//		final int SYM_CLASSDECLARATION                     = 152;  // <ClassDeclaration>
//		final int SYM_CLASSINSTANCECREATIONEXPRESSION      = 153;  // <ClassInstanceCreationExpression>
//		final int SYM_CLASSMEMBERDECLARATION               = 154;  // <ClassMemberDeclaration>
//		final int SYM_CLASSORINTERFACETYPE                 = 155;  // <ClassOrInterfaceType>
//		final int SYM_CLASSTYPE                            = 156;  // <ClassType>
//		final int SYM_CLASSTYPELIST                        = 157;  // <ClassTypeList>
//		final int SYM_COMPILATIONUNIT                      = 158;  // <CompilationUnit>
//		final int SYM_CONDITIONALANDEXPRESSION             = 159;  // <ConditionalAndExpression>
//		final int SYM_CONDITIONALEXPRESSION                = 160;  // <ConditionalExpression>
//		final int SYM_CONDITIONALOREXPRESSION              = 161;  // <ConditionalOrExpression>
//		final int SYM_CONSTANTDECLARATION                  = 162;  // <ConstantDeclaration>
//		final int SYM_CONSTANTEXPRESSION                   = 163;  // <ConstantExpression>
//		final int SYM_CONSTRUCTORBODY                      = 164;  // <ConstructorBody>
//		final int SYM_CONSTRUCTORDECLARATION               = 165;  // <ConstructorDeclaration>
//		final int SYM_CONSTRUCTORDECLARATOR                = 166;  // <ConstructorDeclarator>
//		final int SYM_CONTINUESTATEMENT                    = 167;  // <ContinueStatement>
		final int SYM_DECIMALINTEGERLITERAL                = 168;  // <DecimalIntegerLiteral>
//		final int SYM_DIMEXPR                              = 169;  // <DimExpr>
//		final int SYM_DIMEXPRS                             = 170;  // <DimExprs>
//		final int SYM_DIMS                                 = 171;  // <Dims>
//		final int SYM_DOSTATEMENT                          = 172;  // <DoStatement>
//		final int SYM_ELEMENTVALUE                         = 173;  // <ElementValue>
//		final int SYM_ELEMENTVALUEARRAYINITIALIZER         = 174;  // <ElementValueArrayInitializer>
//		final int SYM_ELEMENTVALUEPAIR                     = 175;  // <ElementValuePair>
//		final int SYM_ELEMENTVALUEPAIRS                    = 176;  // <ElementValuePairs>
//		final int SYM_ELEMENTVALUES                        = 177;  // <ElementValues>
//		final int SYM_EMPTYSTATEMENT                       = 178;  // <EmptyStatement>
//		final int SYM_ENHANCEDFORSTATEMENT                 = 179;  // <EnhancedForStatement>
//		final int SYM_ENHANCEDFORSTATEMENTNOSHORTIF        = 180;  // <EnhancedForStatementNoShortIf>
//		final int SYM_ENUMBODY                             = 181;  // <EnumBody>
//		final int SYM_ENUMBODYDECLARATIONSOPT              = 182;  // <EnumBodyDeclarationsOpt>
//		final int SYM_ENUMCONSTANT                         = 183;  // <EnumConstant>
//		final int SYM_ENUMCONSTANTS                        = 184;  // <EnumConstants>
//		final int SYM_ENUMDECLARATION                      = 185;  // <EnumDeclaration>
//		final int SYM_EQUALITYEXPRESSION                   = 186;  // <EqualityExpression>
//		final int SYM_EXCLUSIVEOREXPRESSION                = 187;  // <ExclusiveOrExpression>
//		final int SYM_EXPLICITCONSTRUCTORINVOCATION        = 188;  // <ExplicitConstructorInvocation>
//		final int SYM_EXPRESSION                           = 189;  // <Expression>
//		final int SYM_EXPRESSIONOPT                        = 190;  // <ExpressionOpt>
//		final int SYM_EXPRESSIONSTATEMENT                  = 191;  // <ExpressionStatement>
//		final int SYM_EXTENDSINTERFACES                    = 192;  // <ExtendsInterfaces>
//		final int SYM_FIELDACCESS                          = 193;  // <FieldAccess>
//		final int SYM_FIELDDECLARATION                     = 194;  // <FieldDeclaration>
//		final int SYM_FINALLY2                             = 195;  // <Finally>
//		final int SYM_FLOATINGPOINTTYPE                    = 196;  // <FloatingPointType>
//		final int SYM_FLOATPOINTLITERAL                    = 197;  // <FloatPointLiteral>
//		final int SYM_FORINITOPT                           = 198;  // <ForInitOpt>
//		final int SYM_FORMALPARAMETER                      = 199;  // <FormalParameter>
//		final int SYM_FORMALPARAMETERLIST                  = 200;  // <FormalParameterList>
//		final int SYM_FORSTATEMENT                         = 201;  // <ForStatement>
//		final int SYM_FORSTATEMENTNOSHORTIF                = 202;  // <ForStatementNoShortIf>
//		final int SYM_FORUPDATEOPT                         = 203;  // <ForUpdateOpt>
//		final int SYM_IFTHENELSESTATEMENT                  = 204;  // <IfThenElseStatement>
//		final int SYM_IFTHENELSESTATEMENTNOSHORTIF         = 205;  // <IfThenElseStatementNoShortIf>
//		final int SYM_IFTHENSTATEMENT                      = 206;  // <IfThenStatement>
//		final int SYM_IMPORTDECLARATION                    = 207;  // <ImportDeclaration>
//		final int SYM_IMPORTDECLARATIONS                   = 208;  // <ImportDeclarations>
//		final int SYM_INCLUSIVEOREXPRESSION                = 209;  // <InclusiveOrExpression>
//		final int SYM_INSTANCEINITIALIZER                  = 210;  // <InstanceInitializer>
//		final int SYM_INTEGERLITERAL                       = 211;  // <IntegerLiteral>
//		final int SYM_INTEGRALTYPE                         = 212;  // <IntegralType>
//		final int SYM_INTERFACEBODY                        = 213;  // <InterfaceBody>
//		final int SYM_INTERFACEDECLARATION                 = 214;  // <InterfaceDeclaration>
//		final int SYM_INTERFACEMEMBERDECLARATION           = 215;  // <InterfaceMemberDeclaration>
//		final int SYM_INTERFACEMEMBERDECLARATIONS          = 216;  // <InterfaceMemberDeclarations>
//		final int SYM_INTERFACES                           = 217;  // <Interfaces>
//		final int SYM_INTERFACETYPE                        = 218;  // <InterfaceType>
//		final int SYM_INTERFACETYPELIST                    = 219;  // <InterfaceTypeList>
//		final int SYM_LABELEDSTATEMENT                     = 220;  // <LabeledStatement>
//		final int SYM_LABELEDSTATEMENTNOSHORTIF            = 221;  // <LabeledStatementNoShortIf>
//		final int SYM_LASTFORMALPARAMETER                  = 222;  // <LastFormalParameter>
//		final int SYM_LEFTHANDSIDE                         = 223;  // <LeftHandSide>
//		final int SYM_LITERAL                              = 224;  // <Literal>
//		final int SYM_LOCALCLASSDECLARATION                = 225;  // <LocalClassDeclaration>
//		final int SYM_LOCALCLASSMODIFIERS                  = 226;  // <LocalClassModifiers>
//		final int SYM_LOCALVARIABLEDECLARATION             = 227;  // <LocalVariableDeclaration>
//		final int SYM_LOCALVARIABLEDECLARATIONSTATEMENT    = 228;  // <LocalVariableDeclarationStatement>
//		final int SYM_MARKERANNOTATION                     = 229;  // <MarkerAnnotation>
//		final int SYM_METHODBODY                           = 230;  // <MethodBody>
//		final int SYM_METHODDECLARATION                    = 231;  // <MethodDeclaration>
//		final int SYM_METHODDECLARATOR                     = 232;  // <MethodDeclarator>
//		final int SYM_METHODHEADER                         = 233;  // <MethodHeader>
//		final int SYM_METHODINVOCATION                     = 234;  // <MethodInvocation>
//		final int SYM_MODIFIER                             = 235;  // <Modifier>
//		final int SYM_MODIFIERS                            = 236;  // <Modifiers>
//		final int SYM_MULTIPLICATIVEEXPRESSION             = 237;  // <MultiplicativeExpression>
//		final int SYM_NAME                                 = 238;  // <Name>
//		final int SYM_NORMALANNOTATION                     = 239;  // <NormalAnnotation>
//		final int SYM_NORMALCLASSDECLARATION               = 240;  // <NormalClassDeclaration>
//		final int SYM_NUMERICTYPE                          = 241;  // <NumericType>
//		final int SYM_PACKAGEDECLARATION                   = 242;  // <PackageDeclaration>
//		final int SYM_POSTDECREMENTEXPRESSION              = 243;  // <PostDecrementExpression>
//		final int SYM_POSTFIXEXPRESSION                    = 244;  // <PostfixExpression>
//		final int SYM_POSTINCREMENTEXPRESSION              = 245;  // <PostIncrementExpression>
//		final int SYM_PREDECREMENTEXPRESSION               = 246;  // <PreDecrementExpression>
//		final int SYM_PREINCREMENTEXPRESSION               = 247;  // <PreIncrementExpression>
//		final int SYM_PRIMARY                              = 248;  // <Primary>
//		final int SYM_PRIMARYNONEWARRAY                    = 249;  // <PrimaryNoNewArray>
//		final int SYM_PRIMITIVETYPE                        = 250;  // <PrimitiveType>
//		final int SYM_PROCESSINGTYPECONVERSION             = 251;  // <ProcessingTypeConversion>
//		final int SYM_PURECLASSDECLARATION                 = 252;  // <PureClassDeclaration>
//		final int SYM_QUALIFIEDNAME                        = 253;  // <QualifiedName>
//		final int SYM_QUALPREFIXOPT                        = 254;  // <QualPrefixOpt>
//		final int SYM_RECEIVERPARAMETER                    = 255;  // <ReceiverParameter>
//		final int SYM_REFERENCETYPE                        = 256;  // <ReferenceType>
//		final int SYM_RELATIONALEXPRESSION                 = 257;  // <RelationalExpression>
//		final int SYM_RESOURCE                             = 258;  // <Resource>
//		final int SYM_RESOURCES                            = 259;  // <Resources>
//		final int SYM_RESOURCESPECIFICATION                = 260;  // <ResourceSpecification>
//		final int SYM_RETURNSTATEMENT                      = 261;  // <ReturnStatement>
//		final int SYM_SHIFTEXPRESSION                      = 262;  // <ShiftExpression>
//		final int SYM_SIMPLENAME                           = 263;  // <SimpleName>
//		final int SYM_SINGLEELEMENTANNOTATION              = 264;  // <SingleElementAnnotation>
//		final int SYM_SINGLESTATICIMPORTDECLARATION        = 265;  // <SingleStaticImportDeclaration>
//		final int SYM_SINGLETYPEIMPORTDECLARATION          = 266;  // <SingleTypeImportDeclaration>
//		final int SYM_STATEMENT                            = 267;  // <Statement>
//		final int SYM_STATEMENTEXPRESSION                  = 268;  // <StatementExpression>
//		final int SYM_STATEMENTEXPRESSIONLIST              = 269;  // <StatementExpressionList>
//		final int SYM_STATEMENTNOSHORTIF                   = 270;  // <StatementNoShortIf>
//		final int SYM_STATEMENTWITHOUTTRAILINGSUBSTATEMENT = 271;  // <StatementWithoutTrailingSubstatement>
//		final int SYM_STATICIMPORTONDEMANDDECLARATION      = 272;  // <StaticImportOnDemandDeclaration>
//		final int SYM_STATICINITIALIZER                    = 273;  // <StaticInitializer>
//		final int SYM_SUPER2                               = 274;  // <Super>
//		final int SYM_SWITCHBLOCK                          = 275;  // <SwitchBlock>
//		final int SYM_SWITCHBLOCKSTATEMENTGROUP            = 276;  // <SwitchBlockStatementGroup>
//		final int SYM_SWITCHBLOCKSTATEMENTGROUPS           = 277;  // <SwitchBlockStatementGroups>
//		final int SYM_SWITCHLABEL                          = 278;  // <SwitchLabel>
//		final int SYM_SWITCHLABELS                         = 279;  // <SwitchLabels>
//		final int SYM_SWITCHSTATEMENT                      = 280;  // <SwitchStatement>
//		final int SYM_SYNCHRONIZEDSTATEMENT                = 281;  // <SynchronizedStatement>
//		final int SYM_THROWS2                              = 282;  // <Throws>
//		final int SYM_THROWSTATEMENT                       = 283;  // <ThrowStatement>
//		final int SYM_TRYSTATEMENT                         = 284;  // <TryStatement>
//		final int SYM_TYPE                                 = 285;  // <Type>
//		final int SYM_TYPEARGUMENT                         = 286;  // <TypeArgument>
//		final int SYM_TYPEARGUMENTS                        = 287;  // <TypeArguments>
//		final int SYM_TYPEBOUNDOPT                         = 288;  // <TypeBoundOpt>
//		final int SYM_TYPEDECLARATION                      = 289;  // <TypeDeclaration>
//		final int SYM_TYPEDECLARATIONS                     = 290;  // <TypeDeclarations>
//		final int SYM_TYPEIMPORTONDEMANDDECLARATION        = 291;  // <TypeImportOnDemandDeclaration>
//		final int SYM_TYPENAME                             = 292;  // <TypeName>
//		final int SYM_TYPEPARAMETER                        = 293;  // <TypeParameter>
//		final int SYM_TYPEPARAMETERS                       = 294;  // <TypeParameters>
//		final int SYM_TYPEPARAMETERSOPT                    = 295;  // <TypeParametersOpt>
//		final int SYM_TYPEVARIABLE                         = 296;  // <TypeVariable>
//		final int SYM_UNARYEXPRESSION                      = 297;  // <UnaryExpression>
//		final int SYM_UNARYEXPRESSIONNOTPLUSMINUS          = 298;  // <UnaryExpressionNotPlusMinus>
//		final int SYM_VARIABLEDECLARATOR                   = 299;  // <VariableDeclarator>
//		final int SYM_VARIABLEDECLARATORID                 = 300;  // <VariableDeclaratorId>
//		final int SYM_VARIABLEDECLARATORS                  = 301;  // <VariableDeclarators>
//		final int SYM_VARIABLEINITIALIZER                  = 302;  // <VariableInitializer>
//		final int SYM_VARIABLEINITIALIZERS                 = 303;  // <VariableInitializers>
//		final int SYM_WHILESTATEMENT                       = 304;  // <WhileStatement>
//		final int SYM_WHILESTATEMENTNOSHORTIF              = 305;  // <WhileStatementNoShortIf>
//		final int SYM_WILDCARD                             = 306;  // <Wildcard>
//		final int SYM_WILDCARDBOUNDSOPT                    = 307;  // <WildcardBoundsOpt>
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
//		final int PROD_LITERAL_WEBCOLORLITERAL                                      =  17;  // <Literal> ::= WebColorLiteral
//		final int PROD_ANNOTATION                                                   =  18;  // <Annotation> ::= <NormalAnnotation>
//		final int PROD_ANNOTATION2                                                  =  19;  // <Annotation> ::= <MarkerAnnotation>
//		final int PROD_ANNOTATION3                                                  =  20;  // <Annotation> ::= <SingleElementAnnotation>
//		final int PROD_NORMALANNOTATION_AT_LPAREN_RPAREN                            =  21;  // <NormalAnnotation> ::= '@' <TypeName> '(' <ElementValuePairs> ')'
//		final int PROD_NORMALANNOTATION_AT_LPAREN_RPAREN2                           =  22;  // <NormalAnnotation> ::= '@' <TypeName> '(' ')'
//		final int PROD_ELEMENTVALUEPAIRS                                            =  23;  // <ElementValuePairs> ::= <ElementValuePair>
//		final int PROD_ELEMENTVALUEPAIRS_COMMA                                      =  24;  // <ElementValuePairs> ::= <ElementValuePairs> ',' <ElementValuePair>
//		final int PROD_ELEMENTVALUEPAIR_IDENTIFIER_EQ                               =  25;  // <ElementValuePair> ::= Identifier '=' <ElementValue>
//		final int PROD_ELEMENTVALUE                                                 =  26;  // <ElementValue> ::= <ConditionalExpression>
//		final int PROD_ELEMENTVALUE2                                                =  27;  // <ElementValue> ::= <ElementValueArrayInitializer>
//		final int PROD_ELEMENTVALUE3                                                =  28;  // <ElementValue> ::= <Annotation>
//		final int PROD_ELEMENTVALUEARRAYINITIALIZER_LBRACE_RBRACE                   =  29;  // <ElementValueArrayInitializer> ::= '{' <ElementValues> '}'
//		final int PROD_ELEMENTVALUEARRAYINITIALIZER_LBRACE_COMMA_RBRACE             =  30;  // <ElementValueArrayInitializer> ::= '{' <ElementValues> ',' '}'
//		final int PROD_ELEMENTVALUEARRAYINITIALIZER_LBRACE_COMMA_RBRACE2            =  31;  // <ElementValueArrayInitializer> ::= '{' ',' '}'
//		final int PROD_ELEMENTVALUEARRAYINITIALIZER_LBRACE_RBRACE2                  =  32;  // <ElementValueArrayInitializer> ::= '{' '}'
//		final int PROD_ELEMENTVALUES                                                =  33;  // <ElementValues> ::= <ElementValue>
//		final int PROD_ELEMENTVALUES_COMMA                                          =  34;  // <ElementValues> ::= <ElementValues> ',' <ElementValue>
//		final int PROD_MARKERANNOTATION_AT                                          =  35;  // <MarkerAnnotation> ::= '@' <TypeName>
//		final int PROD_SINGLEELEMENTANNOTATION_AT_LPAREN_RPAREN                     =  36;  // <SingleElementAnnotation> ::= '@' <TypeName> '(' <ElementValue> ')'
//		final int PROD_TYPENAME                                                     =  37;  // <TypeName> ::= <Name>
//		final int PROD_ANNOTATIONS                                                  =  38;  // <Annotations> ::= <Annotation> <Annotations>
//		final int PROD_ANNOTATIONS2                                                 =  39;  // <Annotations> ::= 
//		final int PROD_TYPE                                                         =  40;  // <Type> ::= <PrimitiveType>
//		final int PROD_TYPE2                                                        =  41;  // <Type> ::= <ReferenceType>
//		final int PROD_PRIMITIVETYPE                                                =  42;  // <PrimitiveType> ::= <NumericType>
//		final int PROD_PRIMITIVETYPE_BOOLEAN                                        =  43;  // <PrimitiveType> ::= boolean
//		final int PROD_NUMERICTYPE                                                  =  44;  // <NumericType> ::= <IntegralType>
//		final int PROD_NUMERICTYPE2                                                 =  45;  // <NumericType> ::= <FloatingPointType>
//		final int PROD_INTEGRALTYPE_BYTE                                            =  46;  // <IntegralType> ::= byte
//		final int PROD_INTEGRALTYPE_SHORT                                           =  47;  // <IntegralType> ::= short
//		final int PROD_INTEGRALTYPE_INT                                             =  48;  // <IntegralType> ::= int
//		final int PROD_INTEGRALTYPE_LONG                                            =  49;  // <IntegralType> ::= long
//		final int PROD_INTEGRALTYPE_CHAR                                            =  50;  // <IntegralType> ::= char
//		final int PROD_FLOATINGPOINTTYPE_FLOAT                                      =  51;  // <FloatingPointType> ::= float
//		final int PROD_FLOATINGPOINTTYPE_DOUBLE                                     =  52;  // <FloatingPointType> ::= double
//		final int PROD_REFERENCETYPE                                                =  53;  // <ReferenceType> ::= <ClassOrInterfaceType>
//		final int PROD_REFERENCETYPE2                                               =  54;  // <ReferenceType> ::= <ArrayType>
//		final int PROD_CLASSORINTERFACETYPE_LT_GT                                   =  55;  // <ClassOrInterfaceType> ::= <Name> '<' <TypeArguments> '>'
//		final int PROD_CLASSORINTERFACETYPE                                         =  56;  // <ClassOrInterfaceType> ::= <Name>
//		final int PROD_CLASSTYPE                                                    =  57;  // <ClassType> ::= <ClassOrInterfaceType>
//		final int PROD_INTERFACETYPE                                                =  58;  // <InterfaceType> ::= <ClassOrInterfaceType>
		final int PROD_TYPEVARIABLE_IDENTIFIER                                      =  59;  // <TypeVariable> ::= <Annotations> Identifier
		final int PROD_ARRAYTYPE                                                    =  60;  // <ArrayType> ::= <PrimitiveType> <Dims>
		final int PROD_ARRAYTYPE2                                                   =  61;  // <ArrayType> ::= <Name> <Dims>
//		final int PROD_NAME                                                         =  62;  // <Name> ::= <SimpleName>
//		final int PROD_NAME2                                                        =  63;  // <Name> ::= <QualifiedName>
		final int PROD_SIMPLENAME_IDENTIFIER                                        =  64;  // <SimpleName> ::= Identifier
//		final int PROD_QUALIFIEDNAME_DOT_IDENTIFIER                                 =  65;  // <QualifiedName> ::= <Name> '.' Identifier
//		final int PROD_TYPEARGUMENTS                                                =  66;  // <TypeArguments> ::= <TypeArgument>
//		final int PROD_TYPEARGUMENTS_COMMA                                          =  67;  // <TypeArguments> ::= <TypeArguments> ',' <TypeArgument>
//		final int PROD_TYPEARGUMENT                                                 =  68;  // <TypeArgument> ::= <ReferenceType>
//		final int PROD_TYPEARGUMENT2                                                =  69;  // <TypeArgument> ::= <Wildcard>
//		final int PROD_WILDCARD_QUESTION                                            =  70;  // <Wildcard> ::= <Annotations> '?' <WildcardBoundsOpt>
//		final int PROD_WILDCARDBOUNDSOPT_EXTENDS                                    =  71;  // <WildcardBoundsOpt> ::= extends <ReferenceType>
//		final int PROD_WILDCARDBOUNDSOPT_SUPER                                      =  72;  // <WildcardBoundsOpt> ::= super <ReferenceType>
//		final int PROD_WILDCARDBOUNDSOPT                                            =  73;  // <WildcardBoundsOpt> ::= 
//		final int PROD_TYPEPARAMETER_IDENTIFIER                                     =  74;  // <TypeParameter> ::= <Annotations> Identifier <TypeBoundOpt>
//		final int PROD_TYPEBOUNDOPT_EXTENDS                                         =  75;  // <TypeBoundOpt> ::= extends <TypeVariable>
//		final int PROD_TYPEBOUNDOPT_EXTENDS2                                        =  76;  // <TypeBoundOpt> ::= extends <ClassOrInterfaceType> <AdditionalBoundOpt>
//		final int PROD_ADDITIONALBOUNDOPT_AMP                                       =  77;  // <AdditionalBoundOpt> ::= '&' <InterfaceType>
//		final int PROD_ADDITIONALBOUNDOPT                                           =  78;  // <AdditionalBoundOpt> ::= 
//		final int PROD_COMPILATIONUNIT                                              =  79;  // <CompilationUnit> ::= <PackageDeclaration> <ImportDeclarations> <TypeDeclarations>
//		final int PROD_COMPILATIONUNIT2                                             =  80;  // <CompilationUnit> ::= <PackageDeclaration> <ImportDeclarations>
//		final int PROD_COMPILATIONUNIT3                                             =  81;  // <CompilationUnit> ::= <PackageDeclaration> <TypeDeclarations>
//		final int PROD_COMPILATIONUNIT4                                             =  82;  // <CompilationUnit> ::= <PackageDeclaration>
//		final int PROD_COMPILATIONUNIT5                                             =  83;  // <CompilationUnit> ::= <ImportDeclarations> <TypeDeclarations>
//		final int PROD_COMPILATIONUNIT6                                             =  84;  // <CompilationUnit> ::= <ImportDeclarations>
//		final int PROD_COMPILATIONUNIT7                                             =  85;  // <CompilationUnit> ::= <TypeDeclarations>
//		final int PROD_COMPILATIONUNIT8                                             =  86;  // <CompilationUnit> ::= 
//		final int PROD_IMPORTDECLARATIONS                                           =  87;  // <ImportDeclarations> ::= <ImportDeclaration>
//		final int PROD_IMPORTDECLARATIONS2                                          =  88;  // <ImportDeclarations> ::= <ImportDeclarations> <ImportDeclaration>
//		final int PROD_TYPEDECLARATIONS                                             =  89;  // <TypeDeclarations> ::= <TypeDeclaration>
//		final int PROD_TYPEDECLARATIONS2                                            =  90;  // <TypeDeclarations> ::= <TypeDeclarations> <TypeDeclaration>
		final int PROD_PACKAGEDECLARATION_PACKAGE_SEMI                              =  91;  // <PackageDeclaration> ::= package <Name> ';'
//		final int PROD_IMPORTDECLARATION                                            =  92;  // <ImportDeclaration> ::= <SingleTypeImportDeclaration>
//		final int PROD_IMPORTDECLARATION2                                           =  93;  // <ImportDeclaration> ::= <TypeImportOnDemandDeclaration>
//		final int PROD_IMPORTDECLARATION3                                           =  94;  // <ImportDeclaration> ::= <SingleStaticImportDeclaration>
//		final int PROD_IMPORTDECLARATION4                                           =  95;  // <ImportDeclaration> ::= <StaticImportOnDemandDeclaration>
		final int PROD_SINGLETYPEIMPORTDECLARATION_IMPORT_SEMI                      =  96;  // <SingleTypeImportDeclaration> ::= import <Name> ';'
		final int PROD_TYPEIMPORTONDEMANDDECLARATION_IMPORT_DOT_TIMES_SEMI          =  97;  // <TypeImportOnDemandDeclaration> ::= import <Name> '.' '*' ';'
		final int PROD_SINGLESTATICIMPORTDECLARATION_IMPORT_STATIC_SEMI             =  98;  // <SingleStaticImportDeclaration> ::= import static <Name> ';'
		final int PROD_STATICIMPORTONDEMANDDECLARATION_IMPORT_STATIC_DOT_TIMES_SEMI =  99;  // <StaticImportOnDemandDeclaration> ::= import static <Name> '.' '*' ';'
//		final int PROD_TYPEDECLARATION                                              = 100;  // <TypeDeclaration> ::= <ClassDeclaration>
//		final int PROD_TYPEDECLARATION2                                             = 101;  // <TypeDeclaration> ::= <InterfaceDeclaration>
//		final int PROD_TYPEDECLARATION_SEMI                                         = 102;  // <TypeDeclaration> ::= ';'
//		final int PROD_MODIFIERS                                                    = 103;  // <Modifiers> ::= <Modifier>
//		final int PROD_MODIFIERS2                                                   = 104;  // <Modifiers> ::= <Modifiers> <Modifier>
//		final int PROD_MODIFIER_PUBLIC                                              = 105;  // <Modifier> ::= public
//		final int PROD_MODIFIER_PROTECTED                                           = 106;  // <Modifier> ::= protected
//		final int PROD_MODIFIER_PRIVATE                                             = 107;  // <Modifier> ::= private
//		final int PROD_MODIFIER_STATIC                                              = 108;  // <Modifier> ::= static
//		final int PROD_MODIFIER_ABSTRACT                                            = 109;  // <Modifier> ::= abstract
//		final int PROD_MODIFIER_FINAL                                               = 110;  // <Modifier> ::= final
//		final int PROD_MODIFIER_NATIVE                                              = 111;  // <Modifier> ::= native
//		final int PROD_MODIFIER_SYNCHRONIZED                                        = 112;  // <Modifier> ::= synchronized
//		final int PROD_MODIFIER_TRANSIENT                                           = 113;  // <Modifier> ::= transient
//		final int PROD_MODIFIER_VOLATILE                                            = 114;  // <Modifier> ::= volatile
//		final int PROD_MODIFIER_DEFAULT                                             = 115;  // <Modifier> ::= default
//		final int PROD_MODIFIER_STRICTFP                                            = 116;  // <Modifier> ::= strictfp
//		final int PROD_CLASSDECLARATION                                             = 117;  // <ClassDeclaration> ::= <Annotations> <NormalClassDeclaration>
//		final int PROD_CLASSDECLARATION2                                            = 118;  // <ClassDeclaration> ::= <Annotations> <EnumDeclaration>
		final int PROD_NORMALCLASSDECLARATION                                       = 119;  // <NormalClassDeclaration> ::= <Modifiers> <PureClassDeclaration>
//		final int PROD_NORMALCLASSDECLARATION2                                      = 120;  // <NormalClassDeclaration> ::= <PureClassDeclaration>
		final int PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER                        = 121;  // <PureClassDeclaration> ::= class Identifier <TypeParametersOpt> <Super> <Interfaces> <ClassBody>
		final int PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER2                       = 122;  // <PureClassDeclaration> ::= class Identifier <TypeParametersOpt> <Super> <ClassBody>
		final int PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER3                       = 123;  // <PureClassDeclaration> ::= class Identifier <TypeParametersOpt> <Interfaces> <ClassBody>
		final int PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER4                       = 124;  // <PureClassDeclaration> ::= class Identifier <TypeParametersOpt> <ClassBody>
//		final int PROD_TYPEPARAMETERSOPT_LT_GT                                      = 125;  // <TypeParametersOpt> ::= '<' <TypeParameters> '>'
//		final int PROD_TYPEPARAMETERSOPT                                            = 126;  // <TypeParametersOpt> ::= 
//		final int PROD_TYPEPARAMETERS                                               = 127;  // <TypeParameters> ::= <TypeParameter>
//		final int PROD_TYPEPARAMETERS_COMMA                                         = 128;  // <TypeParameters> ::= <TypeParameters> ',' <TypeParameter>
//		final int PROD_SUPER_EXTENDS                                                = 129;  // <Super> ::= extends <ClassType>
//		final int PROD_INTERFACES_IMPLEMENTS                                        = 130;  // <Interfaces> ::= implements <InterfaceTypeList>
//		final int PROD_INTERFACETYPELIST                                            = 131;  // <InterfaceTypeList> ::= <InterfaceType>
//		final int PROD_INTERFACETYPELIST_COMMA                                      = 132;  // <InterfaceTypeList> ::= <InterfaceTypeList> ',' <InterfaceType>
		final int PROD_ENUMDECLARATION_ENUM_IDENTIFIER                              = 133;  // <EnumDeclaration> ::= <Modifiers> enum Identifier <Interfaces> <EnumBody>
		final int PROD_ENUMDECLARATION_ENUM_IDENTIFIER2                             = 134;  // <EnumDeclaration> ::= <Modifiers> enum Identifier <EnumBody>
//		final int PROD_ENUMBODY_LBRACE_COMMA_RBRACE                                 = 135;  // <EnumBody> ::= '{' <EnumConstants> ',' <EnumBodyDeclarationsOpt> '}'
//		final int PROD_ENUMBODY_LBRACE_RBRACE                                       = 136;  // <EnumBody> ::= '{' <EnumConstants> <EnumBodyDeclarationsOpt> '}'
//		final int PROD_ENUMBODYDECLARATIONSOPT_SEMI                                 = 137;  // <EnumBodyDeclarationsOpt> ::= ';' <ClassBodyDeclarations>
//		final int PROD_ENUMBODYDECLARATIONSOPT                                      = 138;  // <EnumBodyDeclarationsOpt> ::= 
//		final int PROD_ENUMCONSTANTS                                                = 139;  // <EnumConstants> ::= <EnumConstant>
		final int PROD_ENUMCONSTANTS_COMMA                                          = 140;  // <EnumConstants> ::= <EnumConstants> ',' <EnumConstant>
		final int PROD_ENUMCONSTANT_IDENTIFIER_LPAREN_RPAREN                        = 141;  // <EnumConstant> ::= <Annotations> Identifier '(' <ArgumentList> ')' <ClassBodyOpt>
		final int PROD_ENUMCONSTANT_IDENTIFIER                                      = 142;  // <EnumConstant> ::= <Annotations> Identifier <ClassBodyOpt>
//		final int PROD_CLASSBODYOPT                                                 = 143;  // <ClassBodyOpt> ::= <ClassBody>
//		final int PROD_CLASSBODYOPT2                                                = 144;  // <ClassBodyOpt> ::= 
		final int PROD_CLASSBODY_LBRACE_RBRACE                                      = 145;  // <ClassBody> ::= '{' <ClassBodyDeclarations> '}'
//		final int PROD_CLASSBODY_LBRACE_RBRACE2                                     = 146;  // <ClassBody> ::= '{' '}'
//		final int PROD_CLASSBODYDECLARATIONS                                        = 147;  // <ClassBodyDeclarations> ::= <ClassBodyDeclaration>
//		final int PROD_CLASSBODYDECLARATIONS2                                       = 148;  // <ClassBodyDeclarations> ::= <ClassBodyDeclarations> <ClassBodyDeclaration>
//		final int PROD_CLASSBODYDECLARATION                                         = 149;  // <ClassBodyDeclaration> ::= <ClassMemberDeclaration>
//		final int PROD_CLASSBODYDECLARATION2                                        = 150;  // <ClassBodyDeclaration> ::= <InstanceInitializer>
//		final int PROD_CLASSBODYDECLARATION3                                        = 151;  // <ClassBodyDeclaration> ::= <StaticInitializer>
//		final int PROD_CLASSBODYDECLARATION4                                        = 152;  // <ClassBodyDeclaration> ::= <ConstructorDeclaration>
//		final int PROD_CLASSMEMBERDECLARATION                                       = 153;  // <ClassMemberDeclaration> ::= <FieldDeclaration>
//		final int PROD_CLASSMEMBERDECLARATION2                                      = 154;  // <ClassMemberDeclaration> ::= <MethodDeclaration>
//		final int PROD_CLASSMEMBERDECLARATION3                                      = 155;  // <ClassMemberDeclaration> ::= <ClassDeclaration>
//		final int PROD_CLASSMEMBERDECLARATION4                                      = 156;  // <ClassMemberDeclaration> ::= <InterfaceDeclaration>
//		final int PROD_CLASSMEMBERDECLARATION_SEMI                                  = 157;  // <ClassMemberDeclaration> ::= ';'
		final int PROD_FIELDDECLARATION_SEMI                                        = 158;  // <FieldDeclaration> ::= <Annotations> <Modifiers> <Type> <VariableDeclarators> ';'
		final int PROD_FIELDDECLARATION_SEMI2                                       = 159;  // <FieldDeclaration> ::= <Annotations> <Type> <VariableDeclarators> ';'
//		final int PROD_VARIABLEDECLARATORS                                          = 160;  // <VariableDeclarators> ::= <VariableDeclarator>
		final int PROD_VARIABLEDECLARATORS_COMMA                                    = 161;  // <VariableDeclarators> ::= <VariableDeclarators> ',' <VariableDeclarator>
//		final int PROD_VARIABLEDECLARATOR                                           = 162;  // <VariableDeclarator> ::= <VariableDeclaratorId>
		final int PROD_VARIABLEDECLARATOR_EQ                                        = 163;  // <VariableDeclarator> ::= <VariableDeclaratorId> '=' <VariableInitializer>
//		final int PROD_VARIABLEDECLARATORID_IDENTIFIER                              = 164;  // <VariableDeclaratorId> ::= Identifier
//		final int PROD_VARIABLEDECLARATORID_IDENTIFIER2                             = 165;  // <VariableDeclaratorId> ::= Identifier <Dims>
//		final int PROD_VARIABLEINITIALIZER                                          = 166;  // <VariableInitializer> ::= <Expression>
//		final int PROD_VARIABLEINITIALIZER2                                         = 167;  // <VariableInitializer> ::= <ArrayInitializer>
		final int PROD_METHODDECLARATION                                            = 168;  // <MethodDeclaration> ::= <Annotations> <MethodHeader> <MethodBody>
		final int PROD_METHODHEADER                                                 = 169;  // <MethodHeader> ::= <Modifiers> <Type> <MethodDeclarator> <Throws>
		final int PROD_METHODHEADER2                                                = 170;  // <MethodHeader> ::= <Modifiers> <Type> <MethodDeclarator>
//		final int PROD_METHODHEADER3                                                = 171;  // <MethodHeader> ::= <Type> <MethodDeclarator> <Throws>
//		final int PROD_METHODHEADER4                                                = 172;  // <MethodHeader> ::= <Type> <MethodDeclarator>
		final int PROD_METHODHEADER_VOID                                            = 173;  // <MethodHeader> ::= <Modifiers> void <MethodDeclarator> <Throws>
		final int PROD_METHODHEADER_VOID2                                           = 174;  // <MethodHeader> ::= <Modifiers> void <MethodDeclarator>
//		final int PROD_METHODHEADER_VOID3                                           = 175;  // <MethodHeader> ::= void <MethodDeclarator> <Throws>
//		final int PROD_METHODHEADER_VOID4                                           = 176;  // <MethodHeader> ::= void <MethodDeclarator>
		final int PROD_METHODDECLARATOR_IDENTIFIER_LPAREN_RPAREN                    = 177;  // <MethodDeclarator> ::= Identifier '(' <FormalParameterList> ')'
//		final int PROD_METHODDECLARATOR_IDENTIFIER_LPAREN_RPAREN2                   = 178;  // <MethodDeclarator> ::= Identifier '(' ')'
		final int PROD_METHODDECLARATOR_IDENTIFIER_LPAREN_RPAREN3                   = 179;  // <MethodDeclarator> ::= Identifier '(' <FormalParameterList> ')' <Dims>
		final int PROD_METHODDECLARATOR_IDENTIFIER_LPAREN_RPAREN4                   = 180;  // <MethodDeclarator> ::= Identifier '(' ')' <Dims>
//		final int PROD_FORMALPARAMETERLIST                                          = 181;  // <FormalParameterList> ::= <FormalParameter>
		final int PROD_FORMALPARAMETERLIST_COMMA                                    = 182;  // <FormalParameterList> ::= <FormalParameterList> ',' <FormalParameter>
//		final int PROD_FORMALPARAMETER                                              = 183;  // <FormalParameter> ::= <Type> <VariableDeclaratorId>
		final int PROD_FORMALPARAMETER_FINAL                                        = 184;  // <FormalParameter> ::= final <Type> <VariableDeclaratorId>
//		final int PROD_FORMALPARAMETER2                                             = 185;  // <FormalParameter> ::= <ReceiverParameter>
//		final int PROD_FORMALPARAMETER3                                             = 186;  // <FormalParameter> ::= <LastFormalParameter>
		final int PROD_LASTFORMALPARAMETER_ELLIPSIS                                 = 187;  // <LastFormalParameter> ::= <Type> Ellipsis <VariableDeclaratorId>
//		final int PROD_RECEIVERPARAMETER_THIS                                       = 188;  // <ReceiverParameter> ::= <Type> <QualPrefixOpt> this
//		final int PROD_QUALPREFIXOPT_IDENTIFIER_DOT                                 = 189;  // <QualPrefixOpt> ::= Identifier '.' <QualPrefixOpt>
//		final int PROD_QUALPREFIXOPT                                                = 190;  // <QualPrefixOpt> ::= 
//		final int PROD_THROWS_THROWS                                                = 191;  // <Throws> ::= throws <ClassTypeList>
//		final int PROD_CLASSTYPELIST                                                = 192;  // <ClassTypeList> ::= <ClassType>
//		final int PROD_CLASSTYPELIST_COMMA                                          = 193;  // <ClassTypeList> ::= <ClassTypeList> ',' <ClassType>
//		final int PROD_METHODBODY                                                   = 194;  // <MethodBody> ::= <Block>
//		final int PROD_METHODBODY_SEMI                                              = 195;  // <MethodBody> ::= ';'
//		final int PROD_INSTANCEINITIALIZER                                          = 196;  // <InstanceInitializer> ::= <Annotations> <Block>
		final int PROD_STATICINITIALIZER_STATIC                                     = 197;  // <StaticInitializer> ::= <Annotations> static <Block>
		final int PROD_CONSTRUCTORDECLARATION                                       = 198;  // <ConstructorDeclaration> ::= <Annotations> <Modifiers> <ConstructorDeclarator> <Throws> <ConstructorBody>
		final int PROD_CONSTRUCTORDECLARATION2                                      = 199;  // <ConstructorDeclaration> ::= <Annotations> <Modifiers> <ConstructorDeclarator> <ConstructorBody>
		final int PROD_CONSTRUCTORDECLARATION3                                      = 200;  // <ConstructorDeclaration> ::= <Annotations> <ConstructorDeclarator> <Throws> <ConstructorBody>
		final int PROD_CONSTRUCTORDECLARATION4                                      = 201;  // <ConstructorDeclaration> ::= <Annotations> <ConstructorDeclarator> <ConstructorBody>
		final int PROD_CONSTRUCTORDECLARATOR_LPAREN_RPAREN                          = 202;  // <ConstructorDeclarator> ::= <SimpleName> '(' <FormalParameterList> ')'
		final int PROD_CONSTRUCTORDECLARATOR_LPAREN_RPAREN2                         = 203;  // <ConstructorDeclarator> ::= <SimpleName> '(' ')'
//		final int PROD_CONSTRUCTORBODY_LBRACE_RBRACE                                = 204;  // <ConstructorBody> ::= '{' <ExplicitConstructorInvocation> <BlockStatements> '}'
//		final int PROD_CONSTRUCTORBODY_LBRACE_RBRACE2                               = 205;  // <ConstructorBody> ::= '{' <ExplicitConstructorInvocation> '}'
//		final int PROD_CONSTRUCTORBODY_LBRACE_RBRACE3                               = 206;  // <ConstructorBody> ::= '{' <BlockStatements> '}'
//		final int PROD_CONSTRUCTORBODY_LBRACE_RBRACE4                               = 207;  // <ConstructorBody> ::= '{' '}'
		final int PROD_EXPLICITCONSTRUCTORINVOCATION_THIS_LPAREN_RPAREN_SEMI        = 208;  // <ExplicitConstructorInvocation> ::= this '(' <ArgumentList> ')' ';'
		final int PROD_EXPLICITCONSTRUCTORINVOCATION_THIS_LPAREN_RPAREN_SEMI2       = 209;  // <ExplicitConstructorInvocation> ::= this '(' ')' ';'
		final int PROD_EXPLICITCONSTRUCTORINVOCATION_SUPER_LPAREN_RPAREN_SEMI       = 210;  // <ExplicitConstructorInvocation> ::= super '(' <ArgumentList> ')' ';'
		final int PROD_EXPLICITCONSTRUCTORINVOCATION_SUPER_LPAREN_RPAREN_SEMI2      = 211;  // <ExplicitConstructorInvocation> ::= super '(' ')' ';'
		final int PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER                    = 212;  // <InterfaceDeclaration> ::= <Annotations> <Modifiers> interface Identifier <TypeParametersOpt> <ExtendsInterfaces> <InterfaceBody>
		final int PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER2                   = 213;  // <InterfaceDeclaration> ::= <Annotations> <Modifiers> interface Identifier <TypeParametersOpt> <InterfaceBody>
		final int PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER3                   = 214;  // <InterfaceDeclaration> ::= <Annotations> interface Identifier <TypeParametersOpt> <ExtendsInterfaces> <InterfaceBody>
		final int PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER4                   = 215;  // <InterfaceDeclaration> ::= <Annotations> interface Identifier <TypeParametersOpt> <InterfaceBody>
//		final int PROD_EXTENDSINTERFACES_EXTENDS                                    = 216;  // <ExtendsInterfaces> ::= extends <InterfaceType>
//		final int PROD_EXTENDSINTERFACES_COMMA                                      = 217;  // <ExtendsInterfaces> ::= <ExtendsInterfaces> ',' <InterfaceType>
//		final int PROD_INTERFACEBODY_LBRACE_RBRACE                                  = 218;  // <InterfaceBody> ::= '{' <InterfaceMemberDeclarations> '}'
//		final int PROD_INTERFACEBODY_LBRACE_RBRACE2                                 = 219;  // <InterfaceBody> ::= '{' '}'
//		final int PROD_INTERFACEMEMBERDECLARATIONS                                  = 220;  // <InterfaceMemberDeclarations> ::= <InterfaceMemberDeclaration>
//		final int PROD_INTERFACEMEMBERDECLARATIONS2                                 = 221;  // <InterfaceMemberDeclarations> ::= <InterfaceMemberDeclarations> <InterfaceMemberDeclaration>
//		final int PROD_INTERFACEMEMBERDECLARATION                                   = 222;  // <InterfaceMemberDeclaration> ::= <ConstantDeclaration>
//		final int PROD_INTERFACEMEMBERDECLARATION2                                  = 223;  // <InterfaceMemberDeclaration> ::= <MethodDeclaration>
//		final int PROD_INTERFACEMEMBERDECLARATION3                                  = 224;  // <InterfaceMemberDeclaration> ::= <ClassDeclaration>
//		final int PROD_INTERFACEMEMBERDECLARATION4                                  = 225;  // <InterfaceMemberDeclaration> ::= <InterfaceDeclaration>
//		final int PROD_CONSTANTDECLARATION                                          = 226;  // <ConstantDeclaration> ::= <FieldDeclaration>
//		final int PROD_ARRAYINITIALIZER_LBRACE_COMMA_RBRACE                         = 227;  // <ArrayInitializer> ::= '{' <VariableInitializers> ',' '}'
		final int PROD_ARRAYINITIALIZER_LBRACE_RBRACE                               = 228;  // <ArrayInitializer> ::= '{' <VariableInitializers> '}'
//		final int PROD_ARRAYINITIALIZER_LBRACE_COMMA_RBRACE2                        = 229;  // <ArrayInitializer> ::= '{' ',' '}'
//		final int PROD_ARRAYINITIALIZER_LBRACE_RBRACE2                              = 230;  // <ArrayInitializer> ::= '{' '}'
//		final int PROD_VARIABLEINITIALIZERS                                         = 231;  // <VariableInitializers> ::= <VariableInitializer>
		final int PROD_VARIABLEINITIALIZERS_COMMA                                   = 232;  // <VariableInitializers> ::= <VariableInitializers> ',' <VariableInitializer>
//		final int PROD_BLOCK_LBRACE_RBRACE                                          = 233;  // <Block> ::= '{' <BlockStatements> '}'
//		final int PROD_BLOCK_LBRACE_RBRACE2                                         = 234;  // <Block> ::= '{' '}'
//		final int PROD_BLOCKSTATEMENTS                                              = 235;  // <BlockStatements> ::= <BlockStatement>
//		final int PROD_BLOCKSTATEMENTS2                                             = 236;  // <BlockStatements> ::= <BlockStatements> <BlockStatement>
//		final int PROD_BLOCKSTATEMENT                                               = 237;  // <BlockStatement> ::= <LocalVariableDeclarationStatement>
//		final int PROD_BLOCKSTATEMENT2                                              = 238;  // <BlockStatement> ::= <LocalClassDeclaration>
//		final int PROD_BLOCKSTATEMENT3                                              = 239;  // <BlockStatement> ::= <Statement>
//		final int PROD_LOCALVARIABLEDECLARATIONSTATEMENT_SEMI                       = 240;  // <LocalVariableDeclarationStatement> ::= <LocalVariableDeclaration> ';'
		final int PROD_LOCALVARIABLEDECLARATION_FINAL                               = 241;  // <LocalVariableDeclaration> ::= final <Type> <VariableDeclarators>
		final int PROD_LOCALVARIABLEDECLARATION                                     = 242;  // <LocalVariableDeclaration> ::= <Type> <VariableDeclarators>
		final int PROD_LOCALCLASSDECLARATION                                        = 243;  // <LocalClassDeclaration> ::= <LocalClassModifiers> <PureClassDeclaration>
//		final int PROD_LOCALCLASSDECLARATION2                                       = 244;  // <LocalClassDeclaration> ::= <PureClassDeclaration>
//		final int PROD_LOCALCLASSMODIFIERS_ABSTRACT                                 = 245;  // <LocalClassModifiers> ::= abstract
//		final int PROD_LOCALCLASSMODIFIERS_FINAL                                    = 246;  // <LocalClassModifiers> ::= final
//		final int PROD_STATEMENT                                                    = 247;  // <Statement> ::= <StatementWithoutTrailingSubstatement>
//		final int PROD_STATEMENT2                                                   = 248;  // <Statement> ::= <LabeledStatement>
//		final int PROD_STATEMENT3                                                   = 249;  // <Statement> ::= <IfThenStatement>
//		final int PROD_STATEMENT4                                                   = 250;  // <Statement> ::= <IfThenElseStatement>
//		final int PROD_STATEMENT5                                                   = 251;  // <Statement> ::= <WhileStatement>
//		final int PROD_STATEMENT6                                                   = 252;  // <Statement> ::= <ForStatement>
//		final int PROD_STATEMENTNOSHORTIF                                           = 253;  // <StatementNoShortIf> ::= <StatementWithoutTrailingSubstatement>
//		final int PROD_STATEMENTNOSHORTIF2                                          = 254;  // <StatementNoShortIf> ::= <LabeledStatementNoShortIf>
//		final int PROD_STATEMENTNOSHORTIF3                                          = 255;  // <StatementNoShortIf> ::= <IfThenElseStatementNoShortIf>
//		final int PROD_STATEMENTNOSHORTIF4                                          = 256;  // <StatementNoShortIf> ::= <WhileStatementNoShortIf>
//		final int PROD_STATEMENTNOSHORTIF5                                          = 257;  // <StatementNoShortIf> ::= <ForStatementNoShortIf>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT                         = 258;  // <StatementWithoutTrailingSubstatement> ::= <Block>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT2                        = 259;  // <StatementWithoutTrailingSubstatement> ::= <EmptyStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT3                        = 260;  // <StatementWithoutTrailingSubstatement> ::= <ExpressionStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT4                        = 261;  // <StatementWithoutTrailingSubstatement> ::= <SwitchStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT5                        = 262;  // <StatementWithoutTrailingSubstatement> ::= <DoStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT6                        = 263;  // <StatementWithoutTrailingSubstatement> ::= <BreakStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT7                        = 264;  // <StatementWithoutTrailingSubstatement> ::= <ContinueStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT8                        = 265;  // <StatementWithoutTrailingSubstatement> ::= <ReturnStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT9                        = 266;  // <StatementWithoutTrailingSubstatement> ::= <SynchronizedStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT10                       = 267;  // <StatementWithoutTrailingSubstatement> ::= <ThrowStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT11                       = 268;  // <StatementWithoutTrailingSubstatement> ::= <TryStatement>
//		final int PROD_EMPTYSTATEMENT_SEMI                                          = 269;  // <EmptyStatement> ::= ';'
		final int PROD_LABELEDSTATEMENT_IDENTIFIER_COLON                            = 270;  // <LabeledStatement> ::= Identifier ':' <Statement>
		final int PROD_LABELEDSTATEMENTNOSHORTIF_IDENTIFIER_COLON                   = 271;  // <LabeledStatementNoShortIf> ::= Identifier ':' <StatementNoShortIf>
		final int PROD_EXPRESSIONSTATEMENT_SEMI                                     = 272;  // <ExpressionStatement> ::= <StatementExpression> ';'
//		final int PROD_STATEMENTEXPRESSION                                          = 273;  // <StatementExpression> ::= <Assignment>
//		final int PROD_STATEMENTEXPRESSION2                                         = 274;  // <StatementExpression> ::= <PreIncrementExpression>
//		final int PROD_STATEMENTEXPRESSION3                                         = 275;  // <StatementExpression> ::= <PreDecrementExpression>
//		final int PROD_STATEMENTEXPRESSION4                                         = 276;  // <StatementExpression> ::= <PostIncrementExpression>
//		final int PROD_STATEMENTEXPRESSION5                                         = 277;  // <StatementExpression> ::= <PostDecrementExpression>
//		final int PROD_STATEMENTEXPRESSION6                                         = 278;  // <StatementExpression> ::= <MethodInvocation>
//		final int PROD_STATEMENTEXPRESSION7                                         = 279;  // <StatementExpression> ::= <ClassInstanceCreationExpression>
		final int PROD_IFTHENSTATEMENT_IF_LPAREN_RPAREN                             = 280;  // <IfThenStatement> ::= if '(' <Expression> ')' <Statement>
		final int PROD_IFTHENELSESTATEMENT_IF_LPAREN_RPAREN_ELSE                    = 281;  // <IfThenElseStatement> ::= if '(' <Expression> ')' <StatementNoShortIf> else <Statement>
		final int PROD_IFTHENELSESTATEMENTNOSHORTIF_IF_LPAREN_RPAREN_ELSE           = 282;  // <IfThenElseStatementNoShortIf> ::= if '(' <Expression> ')' <StatementNoShortIf> else <StatementNoShortIf>
		final int PROD_SWITCHSTATEMENT_SWITCH_LPAREN_RPAREN                         = 283;  // <SwitchStatement> ::= switch '(' <Expression> ')' <SwitchBlock>
		final int PROD_SWITCHBLOCK_LBRACE_RBRACE                                    = 284;  // <SwitchBlock> ::= '{' <SwitchBlockStatementGroups> <SwitchLabels> '}'
//		final int PROD_SWITCHBLOCK_LBRACE_RBRACE2                                   = 285;  // <SwitchBlock> ::= '{' <SwitchBlockStatementGroups> '}'
		final int PROD_SWITCHBLOCK_LBRACE_RBRACE3                                   = 286;  // <SwitchBlock> ::= '{' <SwitchLabels> '}'
		final int PROD_SWITCHBLOCK_LBRACE_RBRACE4                                   = 287;  // <SwitchBlock> ::= '{' '}'
//		final int PROD_SWITCHBLOCKSTATEMENTGROUPS                                   = 288;  // <SwitchBlockStatementGroups> ::= <SwitchBlockStatementGroup>
		final int PROD_SWITCHBLOCKSTATEMENTGROUPS2                                  = 289;  // <SwitchBlockStatementGroups> ::= <SwitchBlockStatementGroups> <SwitchBlockStatementGroup>
//		final int PROD_SWITCHBLOCKSTATEMENTGROUP                                    = 290;  // <SwitchBlockStatementGroup> ::= <SwitchLabels> <BlockStatements>
//		final int PROD_SWITCHLABELS                                                 = 291;  // <SwitchLabels> ::= <SwitchLabel>
		final int PROD_SWITCHLABELS2                                                = 292;  // <SwitchLabels> ::= <SwitchLabels> <SwitchLabel>
//		final int PROD_SWITCHLABEL_CASE_COLON                                       = 293;  // <SwitchLabel> ::= case <ConstantExpression> ':'
		final int PROD_SWITCHLABEL_DEFAULT_COLON                                    = 294;  // <SwitchLabel> ::= default ':'
		final int PROD_WHILESTATEMENT_WHILE_LPAREN_RPAREN                           = 295;  // <WhileStatement> ::= while '(' <Expression> ')' <Statement>
		final int PROD_WHILESTATEMENTNOSHORTIF_WHILE_LPAREN_RPAREN                  = 296;  // <WhileStatementNoShortIf> ::= while '(' <Expression> ')' <StatementNoShortIf>
		final int PROD_DOSTATEMENT_DO_WHILE_LPAREN_RPAREN_SEMI                      = 297;  // <DoStatement> ::= do <Statement> while '(' <Expression> ')' ';'
//		final int PROD_FORSTATEMENT                                                 = 298;  // <ForStatement> ::= <BasicForStatement>
//		final int PROD_FORSTATEMENT2                                                = 299;  // <ForStatement> ::= <EnhancedForStatement>
		final int PROD_BASICFORSTATEMENT_FOR_LPAREN_SEMI_SEMI_RPAREN                = 300;  // <BasicForStatement> ::= for '(' <ForInitOpt> ';' <ExpressionOpt> ';' <ForUpdateOpt> ')' <Statement>
//		final int PROD_EXPRESSIONOPT                                                = 301;  // <ExpressionOpt> ::= <Expression>
//		final int PROD_EXPRESSIONOPT2                                               = 302;  // <ExpressionOpt> ::= 
//		final int PROD_FORSTATEMENTNOSHORTIF                                        = 303;  // <ForStatementNoShortIf> ::= <BasicForStatementNoShortIf>
//		final int PROD_FORSTATEMENTNOSHORTIF2                                       = 304;  // <ForStatementNoShortIf> ::= <EnhancedForStatementNoShortIf>
		final int PROD_BASICFORSTATEMENTNOSHORTIF_FOR_LPAREN_SEMI_SEMI_RPAREN       = 305;  // <BasicForStatementNoShortIf> ::= for '(' <ForInitOpt> ';' <ExpressionOpt> ';' <ForUpdateOpt> ')' <StatementNoShortIf>
//		final int PROD_FORINITOPT                                                   = 306;  // <ForInitOpt> ::= <StatementExpressionList>
//		final int PROD_FORINITOPT2                                                  = 307;  // <ForInitOpt> ::= <LocalVariableDeclaration>
//		final int PROD_FORINITOPT3                                                  = 308;  // <ForInitOpt> ::= 
//		final int PROD_FORUPDATEOPT                                                 = 309;  // <ForUpdateOpt> ::= <StatementExpressionList>
//		final int PROD_FORUPDATEOPT2                                                = 310;  // <ForUpdateOpt> ::= 
		final int PROD_ENHANCEDFORSTATEMENT_FOR_LPAREN_FINAL_COLON_RPAREN           = 311;  // <EnhancedForStatement> ::= for '(' final <Type> <VariableDeclaratorId> ':' <Expression> ')' <Statement>
		final int PROD_ENHANCEDFORSTATEMENT_FOR_LPAREN_COLON_RPAREN                 = 312;  // <EnhancedForStatement> ::= for '(' <Type> <VariableDeclaratorId> ':' <Expression> ')' <Statement>
		final int PROD_ENHANCEDFORSTATEMENTNOSHORTIF_FOR_LPAREN_FINAL_COLON_RPAREN  = 313;  // <EnhancedForStatementNoShortIf> ::= for '(' final <Type> <VariableDeclaratorId> ':' <Expression> ')' <StatementNoShortIf>
		final int PROD_ENHANCEDFORSTATEMENTNOSHORTIF_FOR_LPAREN_COLON_RPAREN        = 314;  // <EnhancedForStatementNoShortIf> ::= for '(' <Type> <VariableDeclaratorId> ':' <Expression> ')' <StatementNoShortIf>
//		final int PROD_STATEMENTEXPRESSIONLIST                                      = 315;  // <StatementExpressionList> ::= <StatementExpression>
//		final int PROD_STATEMENTEXPRESSIONLIST_COMMA                                = 316;  // <StatementExpressionList> ::= <StatementExpressionList> ',' <StatementExpression>
		final int PROD_BREAKSTATEMENT_BREAK_IDENTIFIER_SEMI                         = 317;  // <BreakStatement> ::= break Identifier ';'
		final int PROD_BREAKSTATEMENT_BREAK_SEMI                                    = 318;  // <BreakStatement> ::= break ';'
		final int PROD_CONTINUESTATEMENT_CONTINUE_IDENTIFIER_SEMI                   = 319;  // <ContinueStatement> ::= continue Identifier ';'
		final int PROD_CONTINUESTATEMENT_CONTINUE_SEMI                              = 320;  // <ContinueStatement> ::= continue ';'
		final int PROD_RETURNSTATEMENT_RETURN_SEMI                                  = 321;  // <ReturnStatement> ::= return <Expression> ';'
		final int PROD_RETURNSTATEMENT_RETURN_SEMI2                                 = 322;  // <ReturnStatement> ::= return ';'
		final int PROD_THROWSTATEMENT_THROW_SEMI                                    = 323;  // <ThrowStatement> ::= throw <Expression> ';'
		final int PROD_SYNCHRONIZEDSTATEMENT_SYNCHRONIZED_LPAREN_RPAREN             = 324;  // <SynchronizedStatement> ::= synchronized '(' <Expression> ')' <Block>
		final int PROD_TRYSTATEMENT_TRY                                             = 325;  // <TryStatement> ::= try <Block> <Catches>
		final int PROD_TRYSTATEMENT_TRY2                                            = 326;  // <TryStatement> ::= try <Block> <Catches> <Finally>
		final int PROD_TRYSTATEMENT_TRY3                                            = 327;  // <TryStatement> ::= try <Block> <Finally>
		final int PROD_TRYSTATEMENT_TRY4                                            = 328;  // <TryStatement> ::= try <ResourceSpecification> <Block>
		final int PROD_TRYSTATEMENT_TRY5                                            = 329;  // <TryStatement> ::= try <ResourceSpecification> <Block> <Catches>
		final int PROD_TRYSTATEMENT_TRY6                                            = 330;  // <TryStatement> ::= try <ResourceSpecification> <Block> <Catches> <Finally>
		final int PROD_TRYSTATEMENT_TRY7                                            = 331;  // <TryStatement> ::= try <ResourceSpecification> <Block> <Finally>
//		final int PROD_CATCHES                                                      = 332;  // <Catches> ::= <CatchClause>
		final int PROD_CATCHES2                                                     = 333;  // <Catches> ::= <Catches> <CatchClause>
//		final int PROD_CATCHCLAUSE_CATCH_LPAREN_RPAREN                              = 334;  // <CatchClause> ::= catch '(' <CatchFormalParameter> ')' <Block>
		final int PROD_CATCHFORMALPARAMETER_FINAL                                   = 335;  // <CatchFormalParameter> ::= final <CatchType> <VariableDeclaratorId>
//		final int PROD_CATCHFORMALPARAMETER                                         = 336;  // <CatchFormalParameter> ::= <CatchType> <VariableDeclaratorId>
//		final int PROD_CATCHTYPE                                                    = 337;  // <CatchType> ::= <ClassType>
		final int PROD_CATCHTYPE_PIPE                                               = 338;  // <CatchType> ::= <CatchType> '|' <ClassType>
//		final int PROD_FINALLY_FINALLY                                              = 339;  // <Finally> ::= finally <Block>
//		final int PROD_RESOURCESPECIFICATION_LPAREN_RPAREN                          = 340;  // <ResourceSpecification> ::= '(' <Resources> ')'
//		final int PROD_RESOURCESPECIFICATION_LPAREN_SEMI_RPAREN                     = 341;  // <ResourceSpecification> ::= '(' <Resources> ';' ')'
//		final int PROD_RESOURCES                                                    = 342;  // <Resources> ::= <Resource>
		final int PROD_RESOURCES_SEMI                                               = 343;  // <Resources> ::= <Resources> ';' <Resource>
//		final int PROD_RESOURCE_EQ                                                  = 344;  // <Resource> ::= <Type> <VariableDeclaratorId> '=' <Expression>
		final int PROD_RESOURCE_FINAL_EQ                                            = 345;  // <Resource> ::= final <Type> <VariableDeclaratorId> '=' <Expression>
//		final int PROD_PRIMARY                                                      = 346;  // <Primary> ::= <PrimaryNoNewArray>
//		final int PROD_PRIMARY2                                                     = 347;  // <Primary> ::= <ArrayCreationExpression>
//		final int PROD_PRIMARYNONEWARRAY                                            = 348;  // <PrimaryNoNewArray> ::= <Literal>
//		final int PROD_PRIMARYNONEWARRAY_THIS                                       = 349;  // <PrimaryNoNewArray> ::= this
		final int PROD_PRIMARYNONEWARRAY_LPAREN_RPAREN                              = 350;  // <PrimaryNoNewArray> ::= '(' <Expression> ')'
//		final int PROD_PRIMARYNONEWARRAY2                                           = 351;  // <PrimaryNoNewArray> ::= <ClassInstanceCreationExpression>
//		final int PROD_PRIMARYNONEWARRAY3                                           = 352;  // <PrimaryNoNewArray> ::= <FieldAccess>
//		final int PROD_PRIMARYNONEWARRAY4                                           = 353;  // <PrimaryNoNewArray> ::= <MethodInvocation>
//		final int PROD_PRIMARYNONEWARRAY5                                           = 354;  // <PrimaryNoNewArray> ::= <ArrayAccess>
//		final int PROD_PRIMARYNONEWARRAY6                                           = 355;  // <PrimaryNoNewArray> ::= <ProcessingTypeConversion>
		final int PROD_CLASSINSTANCECREATIONEXPRESSION_NEW_LPAREN_RPAREN            = 356;  // <ClassInstanceCreationExpression> ::= new <ClassType> '(' <ArgumentList> ')'
		final int PROD_CLASSINSTANCECREATIONEXPRESSION_NEW_LPAREN_RPAREN2           = 357;  // <ClassInstanceCreationExpression> ::= new <ClassType> '(' ')'
		final int PROD_CLASSINSTANCECREATIONEXPRESSION_NEW_LPAREN_RPAREN3           = 358;  // <ClassInstanceCreationExpression> ::= new <ClassType> '(' <ArgumentList> ')' <ClassBody>
		final int PROD_CLASSINSTANCECREATIONEXPRESSION_NEW_LPAREN_RPAREN4           = 359;  // <ClassInstanceCreationExpression> ::= new <ClassType> '(' ')' <ClassBody>
//		final int PROD_ARGUMENTLIST                                                 = 360;  // <ArgumentList> ::= <Expression>
		final int PROD_ARGUMENTLIST_COMMA                                           = 361;  // <ArgumentList> ::= <ArgumentList> ',' <Expression>
		final int PROD_ARRAYCREATIONEXPRESSION_NEW                                  = 362;  // <ArrayCreationExpression> ::= new <PrimitiveType> <DimExprs> <Dims>
		final int PROD_ARRAYCREATIONEXPRESSION_NEW2                                 = 363;  // <ArrayCreationExpression> ::= new <PrimitiveType> <DimExprs>
		final int PROD_ARRAYCREATIONEXPRESSION_NEW3                                 = 364;  // <ArrayCreationExpression> ::= new <ClassOrInterfaceType> <DimExprs> <Dims>
		final int PROD_ARRAYCREATIONEXPRESSION_NEW4                                 = 365;  // <ArrayCreationExpression> ::= new <ClassOrInterfaceType> <DimExprs>
		final int PROD_ARRAYCREATIONEXPRESSION_NEW5                                 = 366;  // <ArrayCreationExpression> ::= new <PrimitiveType> <Dims> <ArrayInitializer>
		final int PROD_ARRAYCREATIONEXPRESSION_NEW6                                 = 367;  // <ArrayCreationExpression> ::= new <ClassOrInterfaceType> <Dims> <ArrayInitializer>
//		final int PROD_DIMEXPRS                                                     = 368;  // <DimExprs> ::= <DimExpr>
//		final int PROD_DIMEXPRS2                                                    = 369;  // <DimExprs> ::= <DimExprs> <DimExpr>
//		final int PROD_DIMEXPR_LBRACKET_RBRACKET                                    = 370;  // <DimExpr> ::= '[' <Expression> ']'
//		final int PROD_DIMS_LBRACKET_RBRACKET                                       = 371;  // <Dims> ::= '[' ']'
//		final int PROD_DIMS_LBRACKET_RBRACKET2                                      = 372;  // <Dims> ::= <Dims> '[' ']'
		final int PROD_FIELDACCESS_DOT_IDENTIFIER                                   = 373;  // <FieldAccess> ::= <Primary> '.' Identifier
		final int PROD_FIELDACCESS_SUPER_DOT_IDENTIFIER                             = 374;  // <FieldAccess> ::= super '.' Identifier
		final int PROD_METHODINVOCATION_LPAREN_RPAREN                               = 375;  // <MethodInvocation> ::= <Name> '(' <ArgumentList> ')'
		final int PROD_METHODINVOCATION_LPAREN_RPAREN2                              = 376;  // <MethodInvocation> ::= <Name> '(' ')'
		final int PROD_METHODINVOCATION_DOT_IDENTIFIER_LPAREN_RPAREN                = 377;  // <MethodInvocation> ::= <Primary> '.' Identifier '(' <ArgumentList> ')'
		final int PROD_METHODINVOCATION_DOT_IDENTIFIER_LPAREN_RPAREN2               = 378;  // <MethodInvocation> ::= <Primary> '.' Identifier '(' ')'
		final int PROD_METHODINVOCATION_SUPER_DOT_IDENTIFIER_LPAREN_RPAREN          = 379;  // <MethodInvocation> ::= super '.' Identifier '(' <ArgumentList> ')'
		final int PROD_METHODINVOCATION_SUPER_DOT_IDENTIFIER_LPAREN_RPAREN2         = 380;  // <MethodInvocation> ::= super '.' Identifier '(' ')'
		final int PROD_PROCESSINGTYPECONVERSION_BINARY_LPAREN_RPAREN                = 381;  // <ProcessingTypeConversion> ::= binary '(' <Expression> ')'
		final int PROD_PROCESSINGTYPECONVERSION_HEX_LPAREN_RPAREN                   = 382;  // <ProcessingTypeConversion> ::= hex '(' <Expression> ')'
		final int PROD_PROCESSINGTYPECONVERSION_UNBINARY_LPAREN_RPAREN              = 383;  // <ProcessingTypeConversion> ::= unbinary '(' <Expression> ')'
		final int PROD_PROCESSINGTYPECONVERSION_UNHEX_LPAREN_RPAREN                 = 384;  // <ProcessingTypeConversion> ::= unhex '(' <Expression> ')'
		final int PROD_PROCESSINGTYPECONVERSION_INT_LPAREN_RPAREN                   = 385;  // <ProcessingTypeConversion> ::= int '(' <Expression> ')'
		final int PROD_PROCESSINGTYPECONVERSION_BYTE_LPAREN_RPAREN                  = 386;  // <ProcessingTypeConversion> ::= byte '(' <Expression> ')'
		final int PROD_PROCESSINGTYPECONVERSION_CHAR_LPAREN_RPAREN                  = 387;  // <ProcessingTypeConversion> ::= char '(' <Expression> ')'
		final int PROD_PROCESSINGTYPECONVERSION_STR_LPAREN_RPAREN                   = 388;  // <ProcessingTypeConversion> ::= str '(' <Expression> ')'
		final int PROD_PROCESSINGTYPECONVERSION_FLOAT_LPAREN_RPAREN                 = 389;  // <ProcessingTypeConversion> ::= float '(' <Expression> ')'
		final int PROD_PROCESSINGTYPECONVERSION_BOOLEAN_LPAREN_RPAREN               = 390;  // <ProcessingTypeConversion> ::= boolean '(' <Expression> ')'
		final int PROD_ARRAYACCESS_LBRACKET_RBRACKET                                = 391;  // <ArrayAccess> ::= <Name> '[' <Expression> ']'
		final int PROD_ARRAYACCESS_LBRACKET_RBRACKET2                               = 392;  // <ArrayAccess> ::= <PrimaryNoNewArray> '[' <Expression> ']'
//		final int PROD_POSTFIXEXPRESSION                                            = 393;  // <PostfixExpression> ::= <Primary>
//		final int PROD_POSTFIXEXPRESSION2                                           = 394;  // <PostfixExpression> ::= <Name>
//		final int PROD_POSTFIXEXPRESSION3                                           = 395;  // <PostfixExpression> ::= <PostIncrementExpression>
//		final int PROD_POSTFIXEXPRESSION4                                           = 396;  // <PostfixExpression> ::= <PostDecrementExpression>
		final int PROD_POSTINCREMENTEXPRESSION_PLUSPLUS                             = 397;  // <PostIncrementExpression> ::= <PostfixExpression> '++'
		final int PROD_POSTDECREMENTEXPRESSION_MINUSMINUS                           = 398;  // <PostDecrementExpression> ::= <PostfixExpression> '--'
//		final int PROD_UNARYEXPRESSION                                              = 399;  // <UnaryExpression> ::= <PreIncrementExpression>
//		final int PROD_UNARYEXPRESSION2                                             = 400;  // <UnaryExpression> ::= <PreDecrementExpression>
		final int PROD_UNARYEXPRESSION_PLUS                                         = 401;  // <UnaryExpression> ::= '+' <UnaryExpression>
		final int PROD_UNARYEXPRESSION_MINUS                                        = 402;  // <UnaryExpression> ::= '-' <UnaryExpression>
//		final int PROD_UNARYEXPRESSION3                                             = 403;  // <UnaryExpression> ::= <UnaryExpressionNotPlusMinus>
		final int PROD_PREINCREMENTEXPRESSION_PLUSPLUS                              = 404;  // <PreIncrementExpression> ::= '++' <UnaryExpression>
		final int PROD_PREDECREMENTEXPRESSION_MINUSMINUS                            = 405;  // <PreDecrementExpression> ::= '--' <UnaryExpression>
//		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS                                  = 406;  // <UnaryExpressionNotPlusMinus> ::= <PostfixExpression>
		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS_TILDE                            = 407;  // <UnaryExpressionNotPlusMinus> ::= '~' <UnaryExpression>
		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS_EXCLAM                           = 408;  // <UnaryExpressionNotPlusMinus> ::= '!' <UnaryExpression>
//		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS2                                 = 409;  // <UnaryExpressionNotPlusMinus> ::= <CastExpression>
		final int PROD_CASTEXPRESSION_LPAREN_RPAREN                                 = 410;  // <CastExpression> ::= '(' <PrimitiveType> <Dims> ')' <UnaryExpression>
		final int PROD_CASTEXPRESSION_LPAREN_RPAREN2                                = 411;  // <CastExpression> ::= '(' <PrimitiveType> ')' <UnaryExpression>
		final int PROD_CASTEXPRESSION_LPAREN_RPAREN3                                = 412;  // <CastExpression> ::= '(' <Expression> ')' <UnaryExpressionNotPlusMinus>
		final int PROD_CASTEXPRESSION_LPAREN_RPAREN4                                = 413;  // <CastExpression> ::= '(' <Name> <Dims> ')' <UnaryExpressionNotPlusMinus>
//		final int PROD_MULTIPLICATIVEEXPRESSION                                     = 414;  // <MultiplicativeExpression> ::= <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_TIMES                               = 415;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> '*' <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_DIV                                 = 416;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> '/' <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_PERCENT                             = 417;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> '%' <UnaryExpression>
//		final int PROD_ADDITIVEEXPRESSION                                           = 418;  // <AdditiveExpression> ::= <MultiplicativeExpression>
		final int PROD_ADDITIVEEXPRESSION_PLUS                                      = 419;  // <AdditiveExpression> ::= <AdditiveExpression> '+' <MultiplicativeExpression>
		final int PROD_ADDITIVEEXPRESSION_MINUS                                     = 420;  // <AdditiveExpression> ::= <AdditiveExpression> '-' <MultiplicativeExpression>
//		final int PROD_SHIFTEXPRESSION                                              = 421;  // <ShiftExpression> ::= <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_LTLT                                         = 422;  // <ShiftExpression> ::= <ShiftExpression> '<<' <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_GTGT                                         = 423;  // <ShiftExpression> ::= <ShiftExpression> '>>' <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_GTGTGT                                       = 424;  // <ShiftExpression> ::= <ShiftExpression> '>>>' <AdditiveExpression>
//		final int PROD_RELATIONALEXPRESSION                                         = 425;  // <RelationalExpression> ::= <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_LT                                      = 426;  // <RelationalExpression> ::= <RelationalExpression> '<' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_GT                                      = 427;  // <RelationalExpression> ::= <RelationalExpression> '>' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_LTEQ                                    = 428;  // <RelationalExpression> ::= <RelationalExpression> '<=' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_GTEQ                                    = 429;  // <RelationalExpression> ::= <RelationalExpression> '>=' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_INSTANCEOF                              = 430;  // <RelationalExpression> ::= <RelationalExpression> instanceof <ReferenceType>
//		final int PROD_EQUALITYEXPRESSION                                           = 431;  // <EqualityExpression> ::= <RelationalExpression>
		final int PROD_EQUALITYEXPRESSION_EQEQ                                      = 432;  // <EqualityExpression> ::= <EqualityExpression> '==' <RelationalExpression>
		final int PROD_EQUALITYEXPRESSION_EXCLAMEQ                                  = 433;  // <EqualityExpression> ::= <EqualityExpression> '!=' <RelationalExpression>
//		final int PROD_ANDEXPRESSION                                                = 434;  // <AndExpression> ::= <EqualityExpression>
		final int PROD_ANDEXPRESSION_AMP                                            = 435;  // <AndExpression> ::= <AndExpression> '&' <EqualityExpression>
//		final int PROD_EXCLUSIVEOREXPRESSION                                        = 436;  // <ExclusiveOrExpression> ::= <AndExpression>
		final int PROD_EXCLUSIVEOREXPRESSION_CARET                                  = 437;  // <ExclusiveOrExpression> ::= <ExclusiveOrExpression> '^' <AndExpression>
//		final int PROD_INCLUSIVEOREXPRESSION                                        = 438;  // <InclusiveOrExpression> ::= <ExclusiveOrExpression>
		final int PROD_INCLUSIVEOREXPRESSION_PIPE                                   = 439;  // <InclusiveOrExpression> ::= <InclusiveOrExpression> '|' <ExclusiveOrExpression>
//		final int PROD_CONDITIONALANDEXPRESSION                                     = 440;  // <ConditionalAndExpression> ::= <InclusiveOrExpression>
		final int PROD_CONDITIONALANDEXPRESSION_AMPAMP                              = 441;  // <ConditionalAndExpression> ::= <ConditionalAndExpression> '&&' <InclusiveOrExpression>
//		final int PROD_CONDITIONALOREXPRESSION                                      = 442;  // <ConditionalOrExpression> ::= <ConditionalAndExpression>
		final int PROD_CONDITIONALOREXPRESSION_PIPEPIPE                             = 443;  // <ConditionalOrExpression> ::= <ConditionalOrExpression> '||' <ConditionalAndExpression>
//		final int PROD_CONDITIONALEXPRESSION                                        = 444;  // <ConditionalExpression> ::= <ConditionalOrExpression>
		final int PROD_CONDITIONALEXPRESSION_QUESTION_COLON                         = 445;  // <ConditionalExpression> ::= <ConditionalOrExpression> '?' <Expression> ':' <ConditionalExpression>
//		final int PROD_ASSIGNMENTEXPRESSION                                         = 446;  // <AssignmentExpression> ::= <ConditionalExpression>
//		final int PROD_ASSIGNMENTEXPRESSION2                                        = 447;  // <AssignmentExpression> ::= <Assignment>
		final int PROD_ASSIGNMENT                                                   = 448;  // <Assignment> ::= <LeftHandSide> <AssignmentOperator> <AssignmentExpression>
//		final int PROD_LEFTHANDSIDE                                                 = 449;  // <LeftHandSide> ::= <Name>
//		final int PROD_LEFTHANDSIDE2                                                = 450;  // <LeftHandSide> ::= <FieldAccess>
//		final int PROD_LEFTHANDSIDE3                                                = 451;  // <LeftHandSide> ::= <ArrayAccess>
		final int PROD_ASSIGNMENTOPERATOR_EQ                                        = 452;  // <AssignmentOperator> ::= '='
//		final int PROD_ASSIGNMENTOPERATOR_TIMESEQ                                   = 453;  // <AssignmentOperator> ::= '*='
//		final int PROD_ASSIGNMENTOPERATOR_DIVEQ                                     = 454;  // <AssignmentOperator> ::= '/='
//		final int PROD_ASSIGNMENTOPERATOR_PERCENTEQ                                 = 455;  // <AssignmentOperator> ::= '%='
		final int PROD_ASSIGNMENTOPERATOR_PLUSEQ                                    = 456;  // <AssignmentOperator> ::= '+='
		final int PROD_ASSIGNMENTOPERATOR_MINUSEQ                                   = 457;  // <AssignmentOperator> ::= '-='
//		final int PROD_ASSIGNMENTOPERATOR_LTLTEQ                                    = 458;  // <AssignmentOperator> ::= '<<='
//		final int PROD_ASSIGNMENTOPERATOR_GTGTEQ                                    = 459;  // <AssignmentOperator> ::= '>>='
//		final int PROD_ASSIGNMENTOPERATOR_GTGTGTEQ                                  = 460;  // <AssignmentOperator> ::= '>>>='
//		final int PROD_ASSIGNMENTOPERATOR_AMPEQ                                     = 461;  // <AssignmentOperator> ::= '&='
//		final int PROD_ASSIGNMENTOPERATOR_CARETEQ                                   = 462;  // <AssignmentOperator> ::= '^='
//		final int PROD_ASSIGNMENTOPERATOR_PIPEEQ                                    = 463;  // <AssignmentOperator> ::= '|='
//		final int PROD_EXPRESSION                                                   = 464;  // <Expression> ::= <AssignmentExpression>
//		final int PROD_CONSTANTEXPRESSION                                           = 465;  // <ConstantExpression> ::= <Expression>
	};

	//----------------------------- Comment configuration -----------------------------

	/** Rule ids representing statements, used as stoppers for comment retrieval (enh. #420) */
	private static final int[] statementIds = new int[]{
			/* RuleConstants members of those productions that are
			 * to be associated with comments found in their syntax subtrees or their
			 * immediate environment. */
			RuleConstants.PROD_NORMALCLASSDECLARATION,
			RuleConstants.PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER,
			RuleConstants.PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER2,
			RuleConstants.PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER3,
			RuleConstants.PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER4,
			RuleConstants.PROD_LOCALCLASSDECLARATION,
			RuleConstants.PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER,
			RuleConstants.PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER2,
			RuleConstants.PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER3,
			RuleConstants.PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER4,
			RuleConstants.PROD_ENUMDECLARATION_ENUM_IDENTIFIER,
			RuleConstants.PROD_ENUMDECLARATION_ENUM_IDENTIFIER2,
			RuleConstants.PROD_ENUMCONSTANTS_COMMA,
			RuleConstants.PROD_ENUMCONSTANT_IDENTIFIER_LPAREN_RPAREN,
			RuleConstants.PROD_ENUMCONSTANT_IDENTIFIER,
			RuleConstants.PROD_FIELDDECLARATION_SEMI,
			RuleConstants.PROD_FIELDDECLARATION_SEMI2,
			RuleConstants.PROD_METHODDECLARATION,
			RuleConstants.PROD_CONSTRUCTORDECLARATION,
			RuleConstants.PROD_CONSTRUCTORDECLARATION2,
			RuleConstants.PROD_CONSTRUCTORDECLARATION3,
			RuleConstants.PROD_CONSTRUCTORDECLARATION4,
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
			// START KGU#953 2021-03-04: Issue #955: Comments for Try statements had not been gathered
			RuleConstants.PROD_TRYSTATEMENT_TRY,
			RuleConstants.PROD_TRYSTATEMENT_TRY2,
			RuleConstants.PROD_TRYSTATEMENT_TRY3,
			RuleConstants.PROD_TRYSTATEMENT_TRY4,
			RuleConstants.PROD_TRYSTATEMENT_TRY5,
			RuleConstants.PROD_TRYSTATEMENT_TRY6,
			RuleConstants.PROD_TRYSTATEMENT_TRY7,
			// END KGU#953 2021-03-04
			// START KGU#957 2021-03-05: Bugfix #959
			RuleConstants.PROD_PROCESSINGTYPECONVERSION_BINARY_LPAREN_RPAREN,
			RuleConstants.PROD_PROCESSINGTYPECONVERSION_HEX_LPAREN_RPAREN,
			RuleConstants.PROD_PROCESSINGTYPECONVERSION_UNBINARY_LPAREN_RPAREN,
			RuleConstants.PROD_PROCESSINGTYPECONVERSION_UNHEX_LPAREN_RPAREN,
			RuleConstants.PROD_PROCESSINGTYPECONVERSION_INT_LPAREN_RPAREN,
			RuleConstants.PROD_PROCESSINGTYPECONVERSION_BYTE_LPAREN_RPAREN,
			RuleConstants.PROD_PROCESSINGTYPECONVERSION_CHAR_LPAREN_RPAREN,
			RuleConstants.PROD_PROCESSINGTYPECONVERSION_STR_LPAREN_RPAREN,
			RuleConstants.PROD_PROCESSINGTYPECONVERSION_FLOAT_LPAREN_RPAREN,
			RuleConstants.PROD_PROCESSINGTYPECONVERSION_BOOLEAN_LPAREN_RPAREN,
			// END KGU#957 2021-03-05
	};
	
	//----------------------------- Preprocessor -----------------------------
	
	// START KGU#953 2021-03-04: Bugfix #955 The original approach seemed clever but left an empty string at start
	//static final StringList CLASS_LITERAL = StringList.explodeWithDelimiter(".class", ".");
	static final StringList CLASS_LITERAL = StringList.explode(".:class", ":");
	// END KGU#953 2021-03-04

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
			
			StringBuilder srcCode = new StringBuilder();
			try (DataInputStream in = new DataInputStream(new FileInputStream(file));
					// START KGU#193 2016-05-04
					BufferedReader br = new BufferedReader(new InputStreamReader(in, _encoding));
					// END KGU#193 2016-05-04
					) {
				String strLine;
				// Filter BOM sequence?
				boolean first = "UTF-8".equals(_encoding);
				//Read File Line By Line
				while ((strLine = br.readLine()) != null)
				{
					checkCancelled();
					
					if (first && strLine.length() >= 1) {
						/* Cut off the BOM sequence (0xEF, 0xBB, 0xBF) if there is any
						 * (As code point, it is represented as 0xfeff, interestingly)
						 */
						if (strLine.codePointAt(0) == 0xfeff) {
							strLine = strLine.substring(1);
						}
					}
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
					first = false;
				}
			}

			//System.out.println(srcCode);
			doExtraPreparations(srcCode, file);

			// trim and save as new file
			checkCancelled();
			interm = File.createTempFile("Structorizer", "." + getFileExtensions()[0]);
			
			try (OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(interm), "UTF-8")) {
				ow.write(srcCode.toString().trim());
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

	/**
	 * Allows subclasses to do some extra preparations to the preprocessed
	 * content given as {@code _srcCode}, considering the source file {@code _file}
	 * @param _srcCode - the pre-processed file content
	 * @param _file - the file object for the source file (e.g. to fetch the name from)
	 * @throws ParserCancelled 
	 */
	protected void doExtraPreparations(StringBuilder _srcCode, File _file) throws ParserCancelled {
		// We don't have to do anything here
	}

	//---------------------- Build methods for structograms ---------------------------

	/** Caches the declared package of the outer class */
	private String packageStr = null;
	/** Caches the import directives */
	private StringList imports = null;
	/** Represents the class definitions in the hierarchical class context */
	private Stack<Root> includables = null;
	
	/** Holds the value of the Java-specific import option "convert_syntax" */
	private boolean optionConvertSyntax = false;
	// START KGU#1117 2024-03-09: Issue #1131 Handle anonymous inne class instantiations properly
	/** Holds the value of the Java-specific import option "dissect_anon_inner_class" */
	private boolean optionDissectInnerClass = true;
	// END KGU#1117 2024-03-09
	
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
	
	/**
	 * Subclassable configuration method. Decides whether methods of the top-level
	 * class or interface are to be qualified or not.
	 * @return {@code true} if methods on the top-level class hierarchy are to be
	 * equipped with namespace prefix, {@code false} otherwise.
	 */
	protected boolean qualifyTopLevelMethods()
	{
		return true;
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
		root.setInclude();
		addRoot(root);
		// Add dummy loops in order to gather fields and method signatures (to be dissolved in the end)
		root.children.addElement(new Forever());
		root.children.addElement(new Forever());
		
		optionConvertSyntax = (Boolean)this.getPluginOption("convert_syntax", false);
		// START KGU#1117 2024-03-09: Issue #1131 Allow to construct diagrams from anonymous classes
		optionDissectInnerClass = (Boolean)this.getPluginOption("dissect_anon_inner_class", true);
		// END KGU#1117 2024-03-09
		
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
				Reduction redClass = _reduction;
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
				if (!this.includables.isEmpty()) {
					qualifier = this.includables.peek().getQualifiedName();
					classRoot = new Root();
					classRoot.setInclude();
					classRoot.addToIncludeList(includables.peek());
					// Add temporary dummy loops in order to gather fields and method signatures
					classRoot.children.addElement(new Forever());
					classRoot.children.addElement(new Forever());
					this.addRoot(classRoot);
				}
				includables.push(classRoot);
				classRoot.setNamespace(qualifier);
				
				classRoot.setText(name);
				
				this.equipWithSourceComment(classRoot, redClass);
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
				if (this.includables.size() == 1 && packageStr != null) {
					classRoot.comment.add("==== package: " + packageStr);
					if (!imports.isEmpty()) {
						imports.insert("==== imports:", 0);
						classRoot.comment.add(imports);
					}
				}
				classRoot.comment.insert(category.toUpperCase()
						+ (this.includables.size() > 1 ? " in class " + qualifier : ""), 0);
				classRoot.getComment().add((modifiers + " " + category).trim());
				if (!typePars.trim().isEmpty()) {
					classRoot.comment.add("==== type parameters: " + typePars);
				}
				if (!inh.isEmpty()) {
					classRoot.comment.add("==== " + inh);
				}
				// Now descend into the body
				this.buildNSD_R(_reduction.get(ixBody).asReduction(), classRoot.children);
				// Dissolve the field and method containers
				dissolveDummyContainers(classRoot);
				this.includables.pop();
			}
			break;
			
			case RuleConstants.PROD_ENUMDECLARATION_ENUM_IDENTIFIER:
			case RuleConstants.PROD_ENUMDECLARATION_ENUM_IDENTIFIER2:
			{
				buildEnumeratorDefinition(_reduction, ruleId);
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
				ele.comment.add("FIELD in class " + includables.peek().getQualifiedName());
				ele.setColor(colorGlobal);
				if (allConstant) {
					ele.setColor(colorConst);
				}
				if (modifiers != null) {
					ele.comment.add(modifiers);
				}
				((Forever)_parentNode.getElement(0)).getBody().addElement(ele);
			}
			break;
			
			case RuleConstants.PROD_STATICINITIALIZER_STATIC:
				// <StaticInitializer> ::= <Annotations> static <Block>
				this.buildNSD_R(_reduction.get(2).asReduction(), _parentNode);
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
				int ixBody = 2;
				Root subRoot = new Root();
				this.equipWithSourceComment(subRoot, _reduction);
				String qualifier = includables.peek().getQualifiedName();
				if (includables.size() > 1 || qualifyTopLevelMethods()) {
					subRoot.setNamespace(qualifier);
				}
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
				// Extract the method header
				this.applyMethodHeader(_reduction.get(ixHeader).asReduction(), subRoot);
					// Add the body elements to the class Root instead
				Root classRoot = includables.peek();
				if (optionImportVarDecl) { // KGU#951 2021-03-04: Issue #956 consider import option
					// Prepare a disabled instruction element showing the declaration
					Call decl = new Call(subRoot.getText());
					decl.isMethodDeclaration = true;
					decl.setComment(subRoot.getComment());
					decl.setColor(colorDecl);
					// Append the declaration
					((Forever)classRoot.children.getElement(1)).getBody().addElement(decl);
				}
				// Add the method as is to the pool
				subRoot.addToIncludeList(includables.peek());
				addRoot(subRoot);
				// Now build the method body
				// START KGU#960 2021-03-06: Bugfix #962 The boy of constructors wasn't always imported
				//this.buildNSD_R(_reduction.get(2).asReduction(), subRoot.children);
				switch (ruleId) {
				case RuleConstants.PROD_CONSTRUCTORDECLARATION:
					ixBody++;	// No break here!
				case RuleConstants.PROD_CONSTRUCTORDECLARATION2:
				case RuleConstants.PROD_CONSTRUCTORDECLARATION3:
					ixBody++;
				}
				this.buildNSD_R(_reduction.get(ixBody).asReduction(), subRoot.children);
				// END KGU#960 2021-03-06
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
				// START KGU#951 2021-03-04: Issue #956 consider import option
				if (!optionImportVarDecl && !isConst) {
					for (int i = vars.count()-1; i >= 0; i--) {
						if (!Instruction.isAssignment(vars.get(i))) {
							vars.remove(i);
						}
					}
				}
				if (vars.isEmpty()) {
					break;
				}
				// END KGU#951 2021-03-04
				Instruction ele = new Instruction(vars);
				if (isConst) {
					ele.setColor(colorConst);
				}
				else {
					ele.setColor(colorDecl);
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
				if (optionImportVarDecl) {	// KGU#951 2021-03-04 issue #956
					Instruction instr = new Instruction("var " + loopVar + ": " + type);
					instr.setDisabled(true);
					instr.setColor(colorMisc);
					_parentNode.addElement(instr);
				}
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
					//                <CatchClause> ::= catch '(' <CatchFormalParameter> ')' <Block>
					Reduction redCatches = _reduction.get(ixBlock+1).asReduction();
					if (redCatches.getParent().getTableIndex() != RuleConstants.PROD_CATCHES2) {
						// A single clause - possibly no Case necessary, but might be multi-catch clause
						// START KGU#952 2021-03-04: Bugfix #955/2
						//String[] param = this.getFormalParameter(redCatches.get(2).asReduction());
						//ele.setText(param[0]);
						//ele.comment.add(param[1]);
						//this.buildNSD_R(redCatches.get(4).asReduction(), ele.qCatch);
						StringList param = this.getCatchParameter(redCatches.get(2).asReduction());
						// Set the actual catch variable
						ele.setText(param.get(0));
						ele.comment.add(param.get(1));
						if (param.count() == 3) {
							this.buildNSD_R(redCatches.get(4).asReduction(), ele.qCatch);
						}
						else {
							Case catchCase = new Case(param.get(0) + " instanceof\n" + param.concatenate(", ", 2) + "\ndefault");
							ele.qCatch.addElement(catchCase);
							this.buildNSD_R(redCatches.get(4).asReduction(), catchCase.qs.get(0));
						}
						// END KGU#953 2021-03-04
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
							// START KGU#953 2021-03-04: Bugfix #955
							//String[] param = this.getFormalParameter(redClause);
							//distr.insert(param[1], 1);
							//Subqueue sq = new Subqueue();
							// This would require a type cast, actually...
							//sq.addElement(new Instruction(param[0] + " <- exception"));
							StringList param = this.getCatchParameter(redClause.get(2).asReduction());
							// FIXME the built-in function to retrieve the type name should be applied here too
							distr.insert(param.concatenate(", ", 2), 1);	// The types as selectors
							Subqueue sq = new Subqueue();
							// This would require a type cast, actually...
							sq.addElement(new Instruction((param.get(1) + " " + param.get(0) + " <- exception").trim()));
							// END KGU#953 2021-03-04
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
						// START KGU#953 2021-03-04: Bugfix #955 Forgotten to place the Case element
						ele.qCatch.addElement(catchCase);
						// END KGU#953 2021-03-04
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
				ele.setDisabled(true);
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
				ele.setDisabled(true);
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
				int i = 0;
				boolean commentDone = false;
				Instruction ele = null;
				while (exprLines.count() > 0 && i < exprLines.count()) {
					String line = exprLines.get(i);
					// START KGU#959 2021-03-05 Bugfix #961
					//if (Instruction.isFunctionCall(line) || Instruction.isProcedureCall(line)) {
					if (Instruction.isFunctionCall(line, true)
							|| Instruction.isProcedureCall(line, true)) {
					// END KGU#959 2021-03-05
						if (i > 0) {
							// Make an instruction for the preceding lines (if there are any)
							ele = new Instruction(exprLines.subSequence(0, i));
							if (!commentDone) {
								this.equipWithSourceComment(ele, _reduction);
							}
							_parentNode.addElement(ele);
						}
						/* Now care for the method invocation line itself. Also
						 * check whether it might be some specific action like
						 * System.exit() or System.out.println()
						 */
						// START KGU#959 2021-03-05: Issue #961: give subclasses a chance for own conversions
						if (line.startsWith("System.exit(")) {
							ele = new Jump(getKeyword("preExit") + " "
									+ line.substring("System.exit(".length(), line.length()-1));
						}
						else if (line.startsWith("System.out.println(")) {
							ele = new Instruction(getKeyword("output") + " "
									+ line.substring("System.out.println(".length(), line.length()-1));
						}
						else if (line.startsWith("System.out.print(")) {
							ele = new Instruction(getKeyword("output") + " "
									+ line.substring("System.out.print(".length(), line.length()-1));
						}
						//else {
						if ((ele = convertInvocation(line)) == null) {
						// END KGU#959 2021-03-05
							ele = new Instruction(line);
						}
						if (!commentDone) {
							this.equipWithSourceComment(ele, _reduction);
						}
						_parentNode.addElement(ele);
						// Remove the processed lines from the list and reset i
						exprLines.remove(0, i+1);
						i = 0;
					}
					else {
						// No special treatment -> just advance to the next line
						i++;
					}
				}
				/* Are some lines left? Then make an instruction from it (there can't
				 * be method invocations among them, as we have scrutinised all lines)
				 */
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

	// START KGU#959 2021-03-06: Issue #961 extracted from decomposeExpression() for overloading
	/**
	 * Checks whether the passed-in instruction line (which must adhere to a
	 * method invocation with or without assigned result, where the assignment
	 * symbol if contained is expected to be "<-") represents some built-in
	 * function or command, e.g. an output instruction, and if so converts it
	 * accordingly. If it is notzhing specific then just returns {@code null}.
	 * @param line - an built instruction line with call syntax (qualified names
	 * possible)
	 * @return a representing {@link Element} or {@code null}
	 */
	protected Instruction convertInvocation(String line)
	{
		Instruction ele = null;
		if (line.startsWith("System.exit(")) {
			ele = new Jump(getKeyword("preExit") + " "
					+ line.substring("System.exit(".length(), line.length()-1));
		}
		else if (line.startsWith("System.out.println(")) {
			ele = new Instruction(getKeyword("output") + " "
					+ line.substring("System.out.println(".length(), line.length()-1));
		}
		else if (line.startsWith("System.out.print(")) {
			ele = new Instruction(getKeyword("output") + " "
					+ line.substring("System.out.print(".length(), line.length()-1));
		}		
		return ele;
	}
	// END KGU#959 2021-03-05
	
	/**
	 * @param classRoot
	 */
	private void dissolveDummyContainers(Root classRoot) {
		for (int i = 1; i >= 0; i--) {
			if (classRoot.children.getSize() > i
					&& classRoot.children.getElement(i) instanceof Forever) {
				Subqueue body = ((Forever)classRoot.children.getElement(i)).getBody();
				for (int j = body.getSize() - 1; j >= 0; j--) {
					classRoot.children.insertElementAt(body.getElement(j), i+1);
				}
				classRoot.children.removeElement(i);
			}
		}
	}

	/**
	 * Derives an enumerator definition, either as a type definition or as a class
	 * from the given {@link Reduction} {@code _reduction}
	 * @param _reduction - The {@link Reduction} meant to represent an {@code  <EnumDeclaration>}
	 * rule.
	 * @param _ruleId - the actual table index of the rule
	 * @throws ParserCancelled if the user happened to abort the parsing process
	 */
	private void buildEnumeratorDefinition(Reduction _reduction, int _ruleId) throws ParserCancelled {
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
		StringList itemComments = new StringList();
		StringList itemNames = new StringList();
		Stack<StringList> itemValues = new Stack<StringList>();
		HashMap<String, Reduction> classBodies = new HashMap<String, Reduction>();
		String thisListComment = null;	// comment for the current item from previous list rule
		do {
			//System.out.println(redConstants.getParent().toString());
			Reduction redConst = redConstants;
			String nextListComment = this.retrieveComment(redConstants);
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
				itemValues.push(EMPTY_SL); // consecutive value
			}
			else {
				// Form the constructor call
				isClass = true;
				StringList value = new StringList();
				this.processArguments(redConst.get(3), StringList.getNew(name), value);
				itemValues.push(value);
			}
			itemNames.add(itemName);
			String itemComment = this.retrieveComment(redConst);
			if (itemComment == null || itemComment.isEmpty()) {
				itemComment = thisListComment;
			}
			itemComments.add(itemComment == null ? "" : itemComment);
			Reduction redItemBody = redConst.get(redConst.size()-1).asReduction();
			if (redItemBody != null && redItemBody.getParent().getTableIndex() == 
					RuleConstants.PROD_CLASSBODY_LBRACE_RBRACE) {
				// There is a non-empty class body
				isClass = true;
				classBodies.put(itemName, redItemBody);
			}
			thisListComment = nextListComment;
		} while (redConstants != null);

		// Now we have to decide among a mere type definition or a class declaration
		Reduction redDecls = redBody.get(redBody.size()-2).asReduction();
		if (isClass || redDecls != null && redDecls.size() > 0) {
			// We will have to define a member class
			Root enumRoot = root;
			String qualifier = packageStr;
			if (!includables.isEmpty()) {
				enumRoot = new Root();
				enumRoot.setInclude();
				// Add temporary dummy loops in order to gather fields and method signatures
				enumRoot.children.addElement(new Forever());
				enumRoot.children.addElement(new Forever());
				qualifier = includables.peek().getQualifiedName();
				enumRoot.addToIncludeList(includables.peek());
				addRoot(enumRoot);
			}
			enumRoot.setText(name);
			enumRoot.setNamespace(qualifier);
			this.equipWithSourceComment(enumRoot, _reduction);
			enumRoot.comment.add(modifiers);
			if (_ruleId == RuleConstants.PROD_ENUMDECLARATION_ENUM_IDENTIFIER) {
				enumRoot.comment.add("==== " + this.getContent_R(_reduction.get(3)));
			}
			if (redDecls != null && redDecls.size() > 0) {
				includables.push(enumRoot);
				this.buildNSD_R(redDecls.get(1).asReduction(), enumRoot.children);
				includables.pop();
			}
			int itemOffset = 0;
			String prevValue = null;
			Subqueue container = ((Forever)enumRoot.children.getElement(0)).getBody();
			for (int i = itemNames.count()-1; i >= 0; i--) {
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
					container.addElement(
							new Instruction(exprs.subSequence(0, exprs.count()-1)));
					value = prevValue = exprs.get(exprs.count()-1);
					itemOffset = 0;
				}
				Instruction itemDecl = new Instruction("const " + itemName + " <- " + value);
				itemDecl.setComment(itemComments.get(i));
				itemDecl.setColor(colorConst);
				container.addElement(itemDecl);
				itemOffset++;
				if (classBodies.containsKey(itemName)) {
					// Produce a subclass for the specific item
					Root itemBody = new Root();
					itemBody.setText(itemName);
					itemBody.setNamespace(qualifier + "." + name);
					itemBody.setInclude();
					itemBody.setComment("Specific CLASS for enum item "
							+ itemBody.getNamespace() + "." + itemName);
					itemBody.addToIncludeList(includables.peek());
					addRoot(itemBody);
					includables.push(itemBody);
					this.buildNSD_R(classBodies.get(itemName), itemBody.children);
					includables.pop();
				}
			}
			this.dissolveDummyContainers(enumRoot);
		}
		else {
			// This is going to be a type definition
			// If there are item comments, then make them unambiguous
			for (int i = 0; i < itemNames.count(); i++) {
				String itemComment = itemComments.get(i);
				String itemName = itemNames.get(i);
				if (!itemComment.isEmpty() && !itemComment.contains(itemName)) {
					itemComments.set(i, itemName + ": " + itemComment);
				}
			}
			// Users may break the lines at their preference afterwards...
			Instruction ele = new Instruction("type " + name + " = enum{"
					+ itemNames.reverse().concatenate(", ") + "}");
			Root targetRoot = root;
			if (!includables.isEmpty()) {
				targetRoot = includables.peek();
				this.equipWithSourceComment(ele, _reduction);
			}
			else {
				// We are on the outermost level
				root.setText(name);
				this.equipWithSourceComment(root, _reduction);
			}
			((Forever)targetRoot.children.getElement(0)).getBody().addElement(ele);
			ele.comment.add(itemComments.reverse());
			ele.comment.add(modifiers);
		}
	}
	
	/**
	 * Analyses the expression tree represented by {@code exprToken} and decomposes
	 * the expression such that embedded implicit or explicit assignments are extracted
	 * to an own preceding line, combined assignment operator expressions are also
	 * decomposed, e.g. {@code <var> += <expr>} to {@code <var> <- <var> + <expr>}.
	 * 
	 * @param exprToken - the {@link Token} representing the top-level expression.
	 * @param isStatement - whether the expression represents a statement
	 * @param leftHandSide - indicates whether the expression represents an assignment
	 *    target expression.
	 * @param optInstNameSubst - optionally a name substitution pair (in case of an
	 *    anonymous inner class instantiation
	 * 
	 * @return a {@code StringList} each elements of which contain an expression, all
	 *    but the very last one are necessarily expression statements.
	 * @throws ParserCancelled 
	 */
	protected StringList decomposeExpression(Token exprToken, boolean isStatement, boolean leftHandSide)
			 throws ParserCancelled {
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
			int ruleIx = exprRed.getParent().getTableIndex();
			switch (ruleIx) {
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
				// START KGU#955 2021-03-04: Issue #956 The decomposition should only be done if optionTranslate is false
				else if (!optionConvertSyntax) {
					// Leave the expression as is
					exprs.add(target + getContent_R(exprRed.get(1)) + value);
				}
				// END KGU#955 2021-03-04
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
				// START KGU#955 2021-03-04: Issue #956 Decompose only if optionTranslate is false
				//exprs.add(target + " <- " + target + " " + opr + " 1");
				if (optionConvertSyntax) {
					exprs.add(target + " <- " + target + " " + opr + " 1");
				}
				else {
					exprs.add(opr + opr + target);
				}
				// END KGU#955 2021-03-04
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
				// START KGU#955 2021-03-04: Issue #956 Decompose only if optionTranslate is false
				//String tempName = "temp" + Integer.toHexString(exprRed.hashCode());
				//exprs.add(tempName + " <- " + target);
				//exprs.add(target + " <- " + target + " " + opr + " 1");
				//if (!isStatement) {
				//	exprs.add(tempName);
				//}
				if (optionConvertSyntax) {
					String tempName = "temp" + Integer.toHexString(exprRed.hashCode());
					if (!isStatement) {
						exprs.add(tempName + " <- " + target);
					}
					exprs.add(target + " <- " + target + " " + opr + " 1");
					if (!isStatement) {
						exprs.add(tempName);
					}
				}
				else {
					exprs.add(target + opr + opr);
				}
				// END KGU#955 2021-03-04
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
					result.add(this.getContent_R(exprRed.get(i)).replace("c_l_a_s_s", "class"));
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
				exprs.add(getContent_R(exprRed, "").replace("c_l_a_s_s", "class"));
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
			
			// START KGU#957 2021-03-05: Bugfix #959 Specific "Processing" language elements
			case RuleConstants.PROD_PROCESSINGTYPECONVERSION_BINARY_LPAREN_RPAREN:
			case RuleConstants.PROD_PROCESSINGTYPECONVERSION_HEX_LPAREN_RPAREN:
			case RuleConstants.PROD_PROCESSINGTYPECONVERSION_UNBINARY_LPAREN_RPAREN:
			case RuleConstants.PROD_PROCESSINGTYPECONVERSION_UNHEX_LPAREN_RPAREN:
			case RuleConstants.PROD_PROCESSINGTYPECONVERSION_INT_LPAREN_RPAREN:
			case RuleConstants.PROD_PROCESSINGTYPECONVERSION_BYTE_LPAREN_RPAREN:
			case RuleConstants.PROD_PROCESSINGTYPECONVERSION_CHAR_LPAREN_RPAREN:
			case RuleConstants.PROD_PROCESSINGTYPECONVERSION_STR_LPAREN_RPAREN:
			case RuleConstants.PROD_PROCESSINGTYPECONVERSION_FLOAT_LPAREN_RPAREN:
			case RuleConstants.PROD_PROCESSINGTYPECONVERSION_BOOLEAN_LPAREN_RPAREN:
			{
				// <ProcessingTypeConversion> ::= binary '(' <Expression> ')'
				// <ProcessingTypeConversion> ::= hex '(' <Expression> ')'
				// <ProcessingTypeConversion> ::= unbinary '(' <Expression> ')'
				// <ProcessingTypeConversion> ::= unhex '(' <Expression> ')'
				// <ProcessingTypeConversion> ::= int '(' <Expression> ')'
				// <ProcessingTypeConversion> ::= byte '(' <Expression> ')'
				// <ProcessingTypeConversion> ::= char '(' <Expression> ')'
				// <ProcessingTypeConversion> ::= str '(' <Expression> ')'
				// <ProcessingTypeConversion> ::= float '(' <Expression> ')'
				// <ProcessingTypeConversion> ::= boolean '(' <Expression> ')'
				exprs.add(decomposeProcessingTypeConversion(exprRed));
			}
			break;
			// END KGU#957 2021-03-05
	
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
				String opr = translateOperator(exprRed.get(0).asString());
				exprs = decomposeExpression(exprRed.get(1), false, false);
				int ixLast = exprs.count()-1;
				if (Function.testIdentifier(opr, true, null)) {
					opr += " ";
				}
				exprs.set(ixLast, opr + exprs.get(ixLast));
			}
			break;
			
			case RuleConstants.PROD_CONDITIONALEXPRESSION_QUESTION_COLON:
			{
				// <ConditionalExpression> ::= <ConditionalOrExpression> '?' <Expression> ':' <ConditionalExpression>
				/* Here we haven't a really good chance to replace it by a statement,
				 * but Structorizer may cope with its execution in simple contexts
				 */
				exprs = decomposeExpression(exprRed.get(0), false, false);
				StringList exprsTrue = decomposeExpression(exprRed.get(2), false, false);
				StringList exprsFalse = decomposeExpression(exprRed.get(4), false, false);
				int ixLast = exprs.count() -1;
				int ixLastT = exprsTrue.count() - 1;
				int ixLastF = exprsFalse.count() - 1;
				String result = exprs.get(ixLast);
				exprs.remove(ixLast);
				exprs.add(exprsTrue.subSequence(0, ixLastT));
				exprs.add(exprsFalse.subSequence(0, ixLastF));
				exprs.add("(" + result + ") ? " + exprsTrue.get(ixLastT) + " : " + exprsFalse.get(ixLastF));
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
				/* FIXME:
				 * Here it gets somewhat difficult to say what to do:
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
				
				// For Structorizer, we just concentrate on the <ArrayInitializer>
				String prefix = "";
				if (!optionConvertSyntax) {
					prefix = "new " + translateType(exprRed.get(1)) + this.getContent_R(exprRed.get(2));
				}
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
				StringList result = StringList.getNew(prefix + "{");
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
				String cast = "(" + this.translateType(exprRed.get(1));
				if (exprRed.size() > 4) {
					String dims = this.getContent_R(exprRed.get(2));
					if (this.optionConvertSyntax) {
						for (int i = 0; i < dims.length()/2; i++) {
							cast = "array of " + cast;
						}
					}
					else {
						cast += dims;
					}
				}
				cast += ")";
				exprs.add(this.decomposeExpression(exprRed.get(exprRed.size()-1), false, false));
				int ixLast = exprs.count()-1;
				exprs.set(ixLast, cast + exprs.get(ixLast));
			}
			break;
			// START KGU#1117 2024-03-09: Issue #1131: Handle anonymous inner classes
			// ATTENTION: THIS CASE MUST REMAIN AT LAST POSITION BEFORE DEFAULT!
			case RuleConstants.PROD_CLASSINSTANCECREATIONEXPRESSION_NEW_LPAREN_RPAREN3:
			case RuleConstants.PROD_CLASSINSTANCECREATIONEXPRESSION_NEW_LPAREN_RPAREN4:
				// <ClassInstanceCreationExpression> ::= new <ClassType> '(' <ArgumentList> ')' <ClassBody>
				// <ClassInstanceCreationExpression> ::= new <ClassType> '(' ')' <ClassBody>
				if (this.optionDissectInnerClass) {
					String className = this.deriveAnonInnerClass(exprRed);
					StringList result = new StringList();
					result.add("new");
					result.add(className);
					if (ruleIx == RuleConstants.PROD_CLASSINSTANCECREATIONEXPRESSION_NEW_LPAREN_RPAREN3) {
						Token argListToken = exprRed.get(3);
						processArguments(argListToken, result, exprs);
					}
					else {
						exprs.add(result.concatenate(null) + "()");
					}
					break;
				}
				// No break; here (or an else branch with the content of default would have to be added)!
			// END KGU#1117 2024-03-09
			default:
				// START KGU#1117 2024-03-08: Some expressions slipped through without replacement
				//exprs.add(this.getContent_R(exprToken));
				exprs.add(this.getContent_R(exprToken).replace(".c_l_a_s_s", ".class"));
				// END KGU#1117 2024-03-08
			}
			
		}
		else {
			exprs.add(exprToken.asString());
		}
		return exprs;
	}

	// START KGU#1117 2024-03-09: Issue #1131 Handle anonymous inner classes
	/**
	 * Creates the diagrams defining an anonymous inner class and its methods and
	 * returns a name mapping from the instantiated super class to the made-up
	 * generic class name of the anonymous class.
	 * 
	 * @param instCreaRed - the instance creation expression {@link Reduction}
	 * @return the made-up name for the instantiated anonymous inner class.
	 * @throws ParserCancelled 
	 */
	private String deriveAnonInnerClass(Reduction instCreaRed) throws ParserCancelled {
		// <ClassInstanceCreationExpression> ::= new <ClassType> '(' <ArgumentList> ')' <ClassBody>
		// <ClassInstanceCreationExpression> ::= new <ClassType> '(' ')' <ClassBody>
		// FIXME Try to delegate as much as possible to a submethod shared with ClassDeclaration section
		String className0 = getContent_R(instCreaRed.get(1).asReduction(), "");
		String qualifier = packageStr;
		Root classRoot = root;
		if (!this.includables.isEmpty()) {
			// (Should always be the case here)
			qualifier = this.includables.peek().getQualifiedName();
			classRoot = new Root();
			classRoot.setInclude();
			classRoot.addToIncludeList(includables.peek());
			// Add temporary dummy loops in order to gather fields and method signatures
			classRoot.children.addElement(new Forever());
			classRoot.children.addElement(new Forever());
			this.addRoot(classRoot);
		}
		includables.push(classRoot);
		classRoot.setNamespace(qualifier);
		String className = className0 + "_" + Integer.toHexString(classRoot.hashCode());
		classRoot.setText(className);
		// FIXME: Is this necessary here?
		if (this.includables.size() == 1 && packageStr != null) {
			classRoot.comment.add("==== package: " + packageStr);
			if (!imports.isEmpty()) {
				imports.insert("==== imports:", 0);
				classRoot.comment.add(imports);
			}
		}
		classRoot.comment.insert("CLASS"
				+ (this.includables.size() > 1 ? " in class " + qualifier : ""), 0);
		classRoot.getComment().add(("Anonymous inner class").trim());
		classRoot.comment.add("==== extends or implements " + className0);
		
		// Now descend into the body
		int ixBody = 4;
		if (instCreaRed.getParent().getTableIndex() == RuleConstants.PROD_CLASSINSTANCECREATIONEXPRESSION_NEW_LPAREN_RPAREN3) {
			ixBody++;
		}
		this.buildNSD_R(instCreaRed.get(ixBody).asReduction(), classRoot.children);
		// Dissolve the field and method containers
		dissolveDummyContainers(classRoot);
		this.includables.pop();
		return className;
	}
	// END KGU#1117 2024-03-09

	// START KGU#957 2021-03-05: Issue #959 - Processing conversion function handling
	/**
	 * Processes a conversion function of the "Processing" language. The JavaParser base
	 * code won't do anything here.
	 * 
	 * @param exprRed - a {@code <ProcessingTypeConversion>} reduction
	 * @return a StringList containing the necessary sequence of Structorizer instructions
	 * and expressions to achieve the same effect.
	 * @throws ParserCancelled if the user aborted the process
	 */
	protected StringList decomposeProcessingTypeConversion(Reduction exprRed) throws ParserCancelled
	{
		// Should not occur in Java
		return new StringList();
	}
	// END KGU#957 2021-03-05

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
	 * suited String describing its declaration in a Structorizer-compatible way.
	 * @param redFP - the {@link Reduction} to be analysed
	 * @return an array of [0] name, [1] type description, and [2] "const" (if to be
	 *   applied, otherwise "")
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

	// START KGU#953 2021-03-04: Bugfix #955/2 Multi-Catch clauses had not been supported
	/**
	 * Extracts one or more formal parameters from the given {@link Reduction} and returns a
	 * suited String describing its declaration in a Structorizer-compatible way.
	 * @param redCFP - the {@link Reduction} to be analysed
	 * @return a StringList of 0. name, 1. "const" or "", 2. ... n-1. type descriptions
	 * @throws ParserCancelled 
	 */
	private StringList getCatchParameter(Reduction redCFP) throws ParserCancelled {
		// PROD_CATCHFORMALPARAMETER_FINAL <CatchFormalParameter> ::= final <CatchType> <VariableDeclaratorId>
		// PROD_CATCHFORMALPARAMETER       <CatchFormalParameter> ::= <CatchType> <VariableDeclaratorId>
		StringList result = new StringList();
		int ruleId = redCFP.getParent().getTableIndex();
		int ixTypes = 0;
		int ixVar = 1;
		boolean isConst = ruleId == RuleConstants.PROD_CATCHFORMALPARAMETER_FINAL;
		if (isConst) {
			ixTypes++;
			ixVar++;
		}
		Token tokCTs = redCFP.get(ixTypes);
		// PROD_CATCHTYPE      <CatchType> ::= <ClassType>
		// PROD_CATCHTYPE_PIPE <CatchType> ::= <CatchType> '|' <ClassType>
		do {
			Token tokCT = tokCTs;
			Reduction redCT = tokCTs.asReduction();
			if (redCT != null && redCT.getParent().getTableIndex() == RuleConstants.PROD_CATCHTYPE_PIPE) {
				tokCTs = redCT.get(0);
				tokCT = redCT.get(2);
			}
			else {
				tokCTs = null;
			}
			result.add(translateType(tokCT));
		} while (tokCTs != null);
		result.add(isConst ? "const" : "");
		result.add(getContent_R(redCFP.get(ixVar)));
		return result.reverse();
	}

	/**
	 * Returns processed variable declarations as StringList
	 * @param token - {@link Token} representing <VariableDeclarators> 
	 * @param typeDescr - the parsed type description in the syntactically preferred syntax
	 * @param asConst - whether the declaration is to be a constant (or a variable otherwise)
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
				// In a declaration context we can hardly decompose the expression into several lines
				valStr = this.translateContent(this.getContent_R(declToken.asReduction().get(2)));
			}
			String var = this.getContent_R(declToken0);
			// FIXME: Consider desired syntax
			if (typeDescr != null) {
				if (optionConvertSyntax) {
					var += ": " + typeDescr;
				}
				else {
					var = typeDescr + " " + var;
				}
			}
			if (asConst) {
				var = "const " + var;
			}
			else if (optionConvertSyntax) {
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
	 * @return either a {@link For} element or {@code null}.
	 * 
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
	 * Checks whether the increment zone of a Java {@code for} loop given by {@link #Token}
	 * {@code incrToken} is suited for a Structorizer FOR loop.<br/>
	 * This is assumed in exactly the following cases:
	 * <ol>
	 * <li>{@code <id>++}, {@code ++<id>}, {@code <id> += <intlit>}, or {@code <id> = <id> + <intlit>}</li>  
	 * <li>{@code <id>--}, {@code --<id>}, {@code <id> -= <intlit>}, or {@code <id> = <id> - <intlit>}</li>
	 * </ol>
	 * @param incrToken - the token representing the third zone of a {@code for} loop header
	 * @return null or a string array of: [0] the id, [1] '+' or '-', [2] the {@code int} literal
	 *     of the increment/decrement
	 * 
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
	 * @return the end value of the Structorizer counting FOR loop if suited, {@code null} otherwise
	 * 
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
	 * Checks the initialization zone of a Java {@code for} loop given by
	 * {@link #Token} {@code initToken} whether the statement is suited for
	 * a Structorizer FOR loop.<br/>
	 * Only the following cases are accepted where the expression {@code <expr>}
	 * must not be composed of several instructions:
	 * <ol>
	 * <li>{@code <id> = <expr>} or</li>
	 * <li>{@code <type> <id> = <expr>}</li>
	 * </ol>
	 * where {@code <id>} is the given {@code id}.
	 * 
	 * @param initToken - the token representing the first zone of a {@code for}
	 *    loop header
	 * @param id - the expected loop variable id to be tested
	 * @return the start value expression, or {@code null} if the {@code id}
	 *    doesn't match or the statement isn't suited
	 * 
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
	 * 
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
	 * Tries to translate the Java type represented by {@code token} according
	 * to the syntactic preferences held in {@link #optionConvertSyntax}, i.e. either
	 * to a documented Structorizer type description syntax (rather Pascal-like)
	 * or not at all.
	 * @param token - the {@link Token} representing a {@code <Type>} rule or symbol
	 * @return an appropriate type description
	 * @throws ParserCancelled 
	 */
	private String translateType(Token token) throws ParserCancelled {
		if (optionConvertSyntax) {
			Reduction red = token.asReduction();
			if (red != null) {
				int ruleId = red.getParent().getTableIndex();
				switch (ruleId) {
				case RuleConstants.PROD_TYPEVARIABLE_IDENTIFIER:
				{
					// <TypeVariable> ::= <Annotations> Identifier
					// Ignore the annotations
					return getContent_R(red.get(1));
				}
				
				case RuleConstants.PROD_ARRAYTYPE:
				case RuleConstants.PROD_ARRAYTYPE2:
				{
					// <ArrayType> ::= <PrimitiveType> <Dims>
					// <ArrayType> ::= <Name> <Dims>
					String type = this.translateType(red.get(0));
					String dims = this.getContent_R(red.get(1));
					for (int i = 0; i < dims.length()/2; i++) {
						type = "array of " + type;
					}
					return type;
				}
				
				default:
					return getContent_R(token);
				}
			}
		}
		return getContent_R(token);
	}
	
	/**
	 * Given the Java operator symbol {@code opr}, either lets it pass as is (if
	 * {@link #optionConvertSyntax} is {@code false}) or returns a Structorizer-
	 * preferred, more verbose (or, say Pascal-like) operator symbol.<br/>
	 * Just applies {@link #operatorMap}, actually.<br/>
	 * <b>WARNING:</b> Avoid to apply this method twice to the same operator!<br/>
	 * The result will not be padded!
	 * @param opr - a Java operator symbol
	 * @return an operator symbol possibly better suited for Structorizer
	 */
	private String translateOperator(String opr)
	{
		/* We will always translate the operators - as they can be displayed in C mode.
		 * but most of the operator translation is already done in decomposeExpression().
		 */
		if (operatorMap.containsKey(opr)) {
			opr = operatorMap.get(opr);
		}
		return opr;
	}

	/**
	 * Helper method to retrieve and compose the text of the given reduction,
	 * combine it with previously assembled string _content and adapt it to
	 * syntactical conventions of Structorizer. Finally return the text phrase.
	 * 
	 * @param _content - A string already assembled, may be used as prefix,
	 *    ignored or combined in another way 
	 * @return composed and translated text.
	 */
	protected String translateContent(String _content)
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
		
		tokens.removeAll(StringList.explode("Math,.", ","), true);

		// START KGU#1098 2023-11-08: Bugfix #1110 Methodwas ineffective
		//return _content.trim();
		return tokens.concatenate(null).trim();
		// END KGU#1098 2023-11-08
	}
	
	/**
	 * Convenience method for the string content retrieval from a {@link Token}
	 * that may be either represent a content symbol or a {@link Reduction}.
	 * @param _token - the {@link Token} the content is to be appended to
	 *        {@code _content}
	 * @return the content string (may be empty in case of noise)
	 * @throws ParserCancelled
	 */
	protected String getContent_R(Token _token) throws ParserCancelled
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
		// START KGU#961 2021-03-06: Specific handling for array initialisations
		int ruleId = _reduction.getParent().getTableIndex();
		if (optionConvertSyntax && (ruleId == RuleConstants.PROD_ARRAYCREATIONEXPRESSION_NEW5
				|| ruleId == RuleConstants.PROD_ARRAYCREATIONEXPRESSION_NEW6)) {
			// <ArrayCreationExpression> ::= new <PrimitiveType> <Dims> <ArrayInitializer>
			// <ArrayCreationExpression> ::= new <ClassOrInterfaceType> <Dims> <ArrayInitializer>
			// Skip the formal creation prefix and concentrate on the actual initialiser
			_reduction = _reduction.get(3).asReduction();
		}
		// END KGU#961 2021-03-06
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

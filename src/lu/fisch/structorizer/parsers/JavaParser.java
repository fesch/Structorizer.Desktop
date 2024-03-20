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
 *      Kay Gürtzig     2024-03-12      Bugfix #1136 Heuristic approaches to circumvent three known problems
 *                                      with type parameters (particularly considering angular brackets),
 *                                      Bugfix #1137: Workaround for usage of "this" like a component name.
 *      Kay Gürtzig     2024-03-17      Issues #1131, #1137: Proper id replacement solution ensuring
 *                                      consistent id restoration via CodeParser.undoIdReplacaments();
 *                                      bugfix #1141: Measures against stack overflow in buildNSD_R()
 *                                      bugfix #1142: assert statements hadn't been supported.
 *      Kay Gürtzig     2024-03-18      Bugfix #1143: {@code <EnumDeclaration>} unduly required modifiers,
 *                                      bugfix #1145: Crash with more than 1 class / interface on top level
 *      Kay Gürtzig     2024-03-20      Bugfix #1150: RuleConstants adapted to new grammar version 0.9
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
//		final int SYM_ASSERT                               =  56;  // assert
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
//		final int SYM_HEXESCAPECHARLITERAL                 =  79;  // HexEscapeCharLiteral
		final int SYM_HEXINTEGERLITERAL                    =  80;  // HexIntegerLiteral
		final int SYM_IDENTIFIER                           =  81;  // Identifier
//		final int SYM_IF                                   =  82;  // if
//		final int SYM_IMPLEMENTS                           =  83;  // implements
//		final int SYM_IMPORT                               =  84;  // import
//		final int SYM_INDIRECTCHARLITERAL                  =  85;  // IndirectCharLiteral
//		final int SYM_INSTANCEOF                           =  86;  // instanceof
//		final int SYM_INT                                  =  87;  // int
//		final int SYM_INTERFACE                            =  88;  // interface
//		final int SYM_LONG                                 =  89;  // long
//		final int SYM_NATIVE                               =  90;  // native
//		final int SYM_NEW                                  =  91;  // new
//		final int SYM_NULLLITERAL                          =  92;  // NullLiteral
//		final int SYM_OCTALESCAPECHARLITERAL               =  93;  // OctalEscapeCharLiteral
		final int SYM_OCTALINTEGERLITERAL                  =  94;  // OctalIntegerLiteral
//		final int SYM_PACKAGE                              =  95;  // package
//		final int SYM_PRIVATE                              =  96;  // private
//		final int SYM_PROTECTED                            =  97;  // protected
//		final int SYM_PUBLIC                               =  98;  // public
//		final int SYM_RETURN                               =  99;  // return
//		final int SYM_SHORT                                = 100;  // short
//		final int SYM_STANDARDESCAPECHARLITERAL            = 101;  // StandardEscapeCharLiteral
//		final int SYM_STARTWITHNOZERODECIMALINTEGERLITERAL = 102;  // StartWithNoZeroDecimalIntegerLiteral
//		final int SYM_STARTWITHZERODECIMALINTEGERLITERAL   = 103;  // StartWithZeroDecimalIntegerLiteral
//		final int SYM_STATIC                               = 104;  // static
//		final int SYM_STRICTFP                             = 105;  // strictfp
		final int SYM_STRINGLITERAL                        = 106;  // StringLiteral
//		final int SYM_SUPER                                = 107;  // super
//		final int SYM_SWITCH                               = 108;  // switch
//		final int SYM_SYNCHRONIZED                         = 109;  // synchronized
//		final int SYM_THIS                                 = 110;  // this
//		final int SYM_THROW                                = 111;  // throw
//		final int SYM_THROWS                               = 112;  // throws
//		final int SYM_TRANSIENT                            = 113;  // transient
//		final int SYM_TRY                                  = 114;  // try
//		final int SYM_VOID                                 = 115;  // void
//		final int SYM_VOLATILE                             = 116;  // volatile
//		final int SYM_WEBCOLORLITERAL                      = 117;  // WebColorLiteral
//		final int SYM_WHILE                                = 118;  // while
//		final int SYM_ADDITIONALBOUNDOPT                   = 119;  // <AdditionalBoundOpt>
//		final int SYM_ADDITIVEEXPRESSION                   = 120;  // <AdditiveExpression>
//		final int SYM_ANDEXPRESSION                        = 121;  // <AndExpression>
//		final int SYM_ANNOTATION                           = 122;  // <Annotation>
//		final int SYM_ANNOTATIONS                          = 123;  // <Annotations>
//		final int SYM_ARGUMENTLIST                         = 124;  // <ArgumentList>
//		final int SYM_ARRAYACCESS                          = 125;  // <ArrayAccess>
//		final int SYM_ARRAYCREATIONEXPRESSION              = 126;  // <ArrayCreationExpression>
//		final int SYM_ARRAYINITIALIZER                     = 127;  // <ArrayInitializer>
//		final int SYM_ARRAYTYPE                            = 128;  // <ArrayType>
//		final int SYM_ASSERTMESSAGEOPT                     = 129;  // <AssertMessageOpt>
//		final int SYM_ASSERTSTATEMENT                      = 130;  // <AssertStatement>
//		final int SYM_ASSIGNMENT                           = 131;  // <Assignment>
//		final int SYM_ASSIGNMENTEXPRESSION                 = 132;  // <AssignmentExpression>
//		final int SYM_ASSIGNMENTOPERATOR                   = 133;  // <AssignmentOperator>
//		final int SYM_BASICFORSTATEMENT                    = 134;  // <BasicForStatement>
//		final int SYM_BASICFORSTATEMENTNOSHORTIF           = 135;  // <BasicForStatementNoShortIf>
//		final int SYM_BLOCK                                = 136;  // <Block>
//		final int SYM_BLOCKSTATEMENT                       = 137;  // <BlockStatement>
//		final int SYM_BLOCKSTATEMENTS                      = 138;  // <BlockStatements>
//		final int SYM_BREAKSTATEMENT                       = 139;  // <BreakStatement>
//		final int SYM_CASTEXPRESSION                       = 140;  // <CastExpression>
//		final int SYM_CATCHCLAUSE                          = 141;  // <CatchClause>
//		final int SYM_CATCHES                              = 142;  // <Catches>
//		final int SYM_CATCHFORMALPARAMETER                 = 143;  // <CatchFormalParameter>
//		final int SYM_CATCHTYPE                            = 144;  // <CatchType>
//		final int SYM_CHARACTERLITERAL                     = 145;  // <CharacterLiteral>
//		final int SYM_CLASSBODY                            = 146;  // <ClassBody>
//		final int SYM_CLASSBODYDECLARATION                 = 147;  // <ClassBodyDeclaration>
//		final int SYM_CLASSBODYDECLARATIONS                = 148;  // <ClassBodyDeclarations>
//		final int SYM_CLASSBODYOPT                         = 149;  // <ClassBodyOpt>
//		final int SYM_CLASSDECLARATION                     = 150;  // <ClassDeclaration>
//		final int SYM_CLASSINSTANCECREATIONEXPRESSION      = 151;  // <ClassInstanceCreationExpression>
//		final int SYM_CLASSMEMBERDECLARATION               = 152;  // <ClassMemberDeclaration>
//		final int SYM_CLASSORINTERFACETYPE                 = 153;  // <ClassOrInterfaceType>
//		final int SYM_CLASSTYPE                            = 154;  // <ClassType>
//		final int SYM_CLASSTYPELIST                        = 155;  // <ClassTypeList>
//		final int SYM_COMPILATIONUNIT                      = 156;  // <CompilationUnit>
//		final int SYM_CONDITIONALANDEXPRESSION             = 157;  // <ConditionalAndExpression>
//		final int SYM_CONDITIONALEXPRESSION                = 158;  // <ConditionalExpression>
//		final int SYM_CONDITIONALOREXPRESSION              = 159;  // <ConditionalOrExpression>
//		final int SYM_CONSTANTDECLARATION                  = 160;  // <ConstantDeclaration>
//		final int SYM_CONSTANTEXPRESSION                   = 161;  // <ConstantExpression>
//		final int SYM_CONSTRUCTORBODY                      = 162;  // <ConstructorBody>
//		final int SYM_CONSTRUCTORDECLARATION               = 163;  // <ConstructorDeclaration>
//		final int SYM_CONSTRUCTORDECLARATOR                = 164;  // <ConstructorDeclarator>
//		final int SYM_CONTINUESTATEMENT                    = 165;  // <ContinueStatement>
		final int SYM_DECIMALINTEGERLITERAL                = 166;  // <DecimalIntegerLiteral>
//		final int SYM_DIMEXPR                              = 167;  // <DimExpr>
//		final int SYM_DIMEXPRS                             = 168;  // <DimExprs>
//		final int SYM_DIMS                                 = 169;  // <Dims>
//		final int SYM_DOSTATEMENT                          = 170;  // <DoStatement>
//		final int SYM_ELEMENTVALUE                         = 171;  // <ElementValue>
//		final int SYM_ELEMENTVALUEARRAYINITIALIZER         = 172;  // <ElementValueArrayInitializer>
//		final int SYM_ELEMENTVALUEPAIR                     = 173;  // <ElementValuePair>
//		final int SYM_ELEMENTVALUEPAIRS                    = 174;  // <ElementValuePairs>
//		final int SYM_ELEMENTVALUES                        = 175;  // <ElementValues>
//		final int SYM_EMPTYSTATEMENT                       = 176;  // <EmptyStatement>
//		final int SYM_ENHANCEDFORSTATEMENT                 = 177;  // <EnhancedForStatement>
//		final int SYM_ENHANCEDFORSTATEMENTNOSHORTIF        = 178;  // <EnhancedForStatementNoShortIf>
//		final int SYM_ENUMBODY                             = 179;  // <EnumBody>
//		final int SYM_ENUMBODYDECLARATIONSOPT              = 180;  // <EnumBodyDeclarationsOpt>
//		final int SYM_ENUMCONSTANT                         = 181;  // <EnumConstant>
//		final int SYM_ENUMCONSTANTS                        = 182;  // <EnumConstants>
//		final int SYM_ENUMDECLARATION                      = 183;  // <EnumDeclaration>
//		final int SYM_EQUALITYEXPRESSION                   = 184;  // <EqualityExpression>
//		final int SYM_EXCLUSIVEOREXPRESSION                = 185;  // <ExclusiveOrExpression>
//		final int SYM_EXPLICITCONSTRUCTORINVOCATION        = 186;  // <ExplicitConstructorInvocation>
//		final int SYM_EXPRESSION                           = 187;  // <Expression>
//		final int SYM_EXPRESSIONOPT                        = 188;  // <ExpressionOpt>
//		final int SYM_EXPRESSIONSTATEMENT                  = 189;  // <ExpressionStatement>
//		final int SYM_EXTENDSINTERFACES                    = 190;  // <ExtendsInterfaces>
//		final int SYM_FIELDACCESS                          = 191;  // <FieldAccess>
//		final int SYM_FIELDDECLARATION                     = 192;  // <FieldDeclaration>
//		final int SYM_FINALLY2                             = 193;  // <Finally>
//		final int SYM_FLOATINGPOINTTYPE                    = 194;  // <FloatingPointType>
//		final int SYM_FLOATPOINTLITERAL                    = 195;  // <FloatPointLiteral>
//		final int SYM_FORINITOPT                           = 196;  // <ForInitOpt>
//		final int SYM_FORMALPARAMETER                      = 197;  // <FormalParameter>
//		final int SYM_FORMALPARAMETERLIST                  = 198;  // <FormalParameterList>
//		final int SYM_FORSTATEMENT                         = 199;  // <ForStatement>
//		final int SYM_FORSTATEMENTNOSHORTIF                = 200;  // <ForStatementNoShortIf>
//		final int SYM_FORUPDATEOPT                         = 201;  // <ForUpdateOpt>
//		final int SYM_IFTHENELSESTATEMENT                  = 202;  // <IfThenElseStatement>
//		final int SYM_IFTHENELSESTATEMENTNOSHORTIF         = 203;  // <IfThenElseStatementNoShortIf>
//		final int SYM_IFTHENSTATEMENT                      = 204;  // <IfThenStatement>
//		final int SYM_IMPORTDECLARATION                    = 205;  // <ImportDeclaration>
//		final int SYM_IMPORTDECLARATIONS                   = 206;  // <ImportDeclarations>
//		final int SYM_INCLUSIVEOREXPRESSION                = 207;  // <InclusiveOrExpression>
//		final int SYM_INSTANCEINITIALIZER                  = 208;  // <InstanceInitializer>
//		final int SYM_INTEGERLITERAL                       = 209;  // <IntegerLiteral>
//		final int SYM_INTEGRALTYPE                         = 210;  // <IntegralType>
//		final int SYM_INTERFACEBODY                        = 211;  // <InterfaceBody>
//		final int SYM_INTERFACEDECLARATION                 = 212;  // <InterfaceDeclaration>
//		final int SYM_INTERFACEMEMBERDECLARATION           = 213;  // <InterfaceMemberDeclaration>
//		final int SYM_INTERFACEMEMBERDECLARATIONS          = 214;  // <InterfaceMemberDeclarations>
//		final int SYM_INTERFACES                           = 215;  // <Interfaces>
//		final int SYM_INTERFACETYPE                        = 216;  // <InterfaceType>
//		final int SYM_INTERFACETYPELIST                    = 217;  // <InterfaceTypeList>
//		final int SYM_LABELEDSTATEMENT                     = 218;  // <LabeledStatement>
//		final int SYM_LABELEDSTATEMENTNOSHORTIF            = 219;  // <LabeledStatementNoShortIf>
//		final int SYM_LASTFORMALPARAMETER                  = 220;  // <LastFormalParameter>
//		final int SYM_LEFTHANDSIDE                         = 221;  // <LeftHandSide>
//		final int SYM_LITERAL                              = 222;  // <Literal>
//		final int SYM_LOCALCLASSDECLARATION                = 223;  // <LocalClassDeclaration>
//		final int SYM_LOCALCLASSMODIFIERS                  = 224;  // <LocalClassModifiers>
//		final int SYM_LOCALVARIABLEDECLARATION             = 225;  // <LocalVariableDeclaration>
//		final int SYM_LOCALVARIABLEDECLARATIONSTATEMENT    = 226;  // <LocalVariableDeclarationStatement>
//		final int SYM_MARKERANNOTATION                     = 227;  // <MarkerAnnotation>
//		final int SYM_METHODBODY                           = 228;  // <MethodBody>
//		final int SYM_METHODDECLARATION                    = 229;  // <MethodDeclaration>
//		final int SYM_METHODDECLARATOR                     = 230;  // <MethodDeclarator>
//		final int SYM_METHODHEADER                         = 231;  // <MethodHeader>
//		final int SYM_METHODINVOCATION                     = 232;  // <MethodInvocation>
//		final int SYM_MODIFIER                             = 233;  // <Modifier>
//		final int SYM_MODIFIERS                            = 234;  // <Modifiers>
//		final int SYM_MODIFIERSOPT                         = 235;  // <ModifiersOpt>
//		final int SYM_MULTIPLICATIVEEXPRESSION             = 236;  // <MultiplicativeExpression>
//		final int SYM_NAME                                 = 237;  // <Name>
//		final int SYM_NORMALANNOTATION                     = 238;  // <NormalAnnotation>
//		final int SYM_NORMALCLASSDECLARATION               = 239;  // <NormalClassDeclaration>
//		final int SYM_NUMERICTYPE                          = 240;  // <NumericType>
//		final int SYM_PACKAGEDECLARATION                   = 241;  // <PackageDeclaration>
//		final int SYM_POSTDECREMENTEXPRESSION              = 242;  // <PostDecrementExpression>
//		final int SYM_POSTFIXEXPRESSION                    = 243;  // <PostfixExpression>
//		final int SYM_POSTINCREMENTEXPRESSION              = 244;  // <PostIncrementExpression>
//		final int SYM_PREDECREMENTEXPRESSION               = 245;  // <PreDecrementExpression>
//		final int SYM_PREINCREMENTEXPRESSION               = 246;  // <PreIncrementExpression>
//		final int SYM_PRIMARY                              = 247;  // <Primary>
//		final int SYM_PRIMARYNONEWARRAY                    = 248;  // <PrimaryNoNewArray>
//		final int SYM_PRIMITIVETYPE                        = 249;  // <PrimitiveType>
//		final int SYM_PROCESSINGTYPECONVERSION             = 250;  // <ProcessingTypeConversion>
//		final int SYM_PURECLASSDECLARATION                 = 251;  // <PureClassDeclaration>
//		final int SYM_QUALIFIEDNAME                        = 252;  // <QualifiedName>
//		final int SYM_QUALPREFIXOPT                        = 253;  // <QualPrefixOpt>
//		final int SYM_RECEIVERPARAMETER                    = 254;  // <ReceiverParameter>
//		final int SYM_REFERENCETYPE                        = 255;  // <ReferenceType>
//		final int SYM_RELATIONALEXPRESSION                 = 256;  // <RelationalExpression>
//		final int SYM_RESOURCE                             = 257;  // <Resource>
//		final int SYM_RESOURCES                            = 258;  // <Resources>
//		final int SYM_RESOURCESPECIFICATION                = 259;  // <ResourceSpecification>
//		final int SYM_RETURNSTATEMENT                      = 260;  // <ReturnStatement>
//		final int SYM_SHIFTEXPRESSION                      = 261;  // <ShiftExpression>
//		final int SYM_SIMPLENAME                           = 262;  // <SimpleName>
//		final int SYM_SINGLEELEMENTANNOTATION              = 263;  // <SingleElementAnnotation>
//		final int SYM_SINGLESTATICIMPORTDECLARATION        = 264;  // <SingleStaticImportDeclaration>
//		final int SYM_SINGLETYPEIMPORTDECLARATION          = 265;  // <SingleTypeImportDeclaration>
//		final int SYM_STATEMENT                            = 266;  // <Statement>
//		final int SYM_STATEMENTEXPRESSION                  = 267;  // <StatementExpression>
//		final int SYM_STATEMENTEXPRESSIONLIST              = 268;  // <StatementExpressionList>
//		final int SYM_STATEMENTNOSHORTIF                   = 269;  // <StatementNoShortIf>
//		final int SYM_STATEMENTWITHOUTTRAILINGSUBSTATEMENT = 270;  // <StatementWithoutTrailingSubstatement>
//		final int SYM_STATICIMPORTONDEMANDDECLARATION      = 271;  // <StaticImportOnDemandDeclaration>
//		final int SYM_STATICINITIALIZER                    = 272;  // <StaticInitializer>
//		final int SYM_SUPER2                               = 273;  // <Super>
//		final int SYM_SWITCHBLOCK                          = 274;  // <SwitchBlock>
//		final int SYM_SWITCHBLOCKSTATEMENTGROUP            = 275;  // <SwitchBlockStatementGroup>
//		final int SYM_SWITCHBLOCKSTATEMENTGROUPS           = 276;  // <SwitchBlockStatementGroups>
//		final int SYM_SWITCHLABEL                          = 277;  // <SwitchLabel>
//		final int SYM_SWITCHLABELS                         = 278;  // <SwitchLabels>
//		final int SYM_SWITCHSTATEMENT                      = 279;  // <SwitchStatement>
//		final int SYM_SYNCHRONIZEDSTATEMENT                = 280;  // <SynchronizedStatement>
//		final int SYM_THROWS2                              = 281;  // <Throws>
//		final int SYM_THROWSTATEMENT                       = 282;  // <ThrowStatement>
//		final int SYM_TRYSTATEMENT                         = 283;  // <TryStatement>
//		final int SYM_TYPE                                 = 284;  // <Type>
//		final int SYM_TYPEARGUMENT                         = 285;  // <TypeArgument>
//		final int SYM_TYPEARGUMENTS                        = 286;  // <TypeArguments>
//		final int SYM_TYPEBOUNDOPT                         = 287;  // <TypeBoundOpt>
//		final int SYM_TYPEDECLARATION                      = 288;  // <TypeDeclaration>
//		final int SYM_TYPEDECLARATIONS                     = 289;  // <TypeDeclarations>
//		final int SYM_TYPEIMPORTONDEMANDDECLARATION        = 290;  // <TypeImportOnDemandDeclaration>
//		final int SYM_TYPENAME                             = 291;  // <TypeName>
//		final int SYM_TYPEPARAMETER                        = 292;  // <TypeParameter>
//		final int SYM_TYPEPARAMETERS                       = 293;  // <TypeParameters>
//		final int SYM_TYPEPARAMETERSOPT                    = 294;  // <TypeParametersOpt>
//		final int SYM_TYPEVARIABLE                         = 295;  // <TypeVariable>
//		final int SYM_UNARYEXPRESSION                      = 296;  // <UnaryExpression>
//		final int SYM_UNARYEXPRESSIONNOTPLUSMINUS          = 297;  // <UnaryExpressionNotPlusMinus>
//		final int SYM_VARIABLEDECLARATOR                   = 298;  // <VariableDeclarator>
//		final int SYM_VARIABLEDECLARATORID                 = 299;  // <VariableDeclaratorId>
//		final int SYM_VARIABLEDECLARATORS                  = 300;  // <VariableDeclarators>
//		final int SYM_VARIABLEINITIALIZER                  = 301;  // <VariableInitializer>
//		final int SYM_VARIABLEINITIALIZERS                 = 302;  // <VariableInitializers>
//		final int SYM_WHILESTATEMENT                       = 303;  // <WhileStatement>
//		final int SYM_WHILESTATEMENTNOSHORTIF              = 304;  // <WhileStatementNoShortIf>
//		final int SYM_WILDCARD                             = 305;  // <Wildcard>
//		final int SYM_WILDCARDBOUNDSOPT                    = 306;  // <WildcardBoundsOpt>
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
//		final int PROD_TYPEBOUNDOPT                                                 =  77;  // <TypeBoundOpt> ::= 
//		final int PROD_ADDITIONALBOUNDOPT_AMP                                       =  78;  // <AdditionalBoundOpt> ::= '&' <InterfaceType>
//		final int PROD_ADDITIONALBOUNDOPT                                           =  79;  // <AdditionalBoundOpt> ::= 
//		final int PROD_COMPILATIONUNIT                                              =  80;  // <CompilationUnit> ::= <PackageDeclaration> <ImportDeclarations> <TypeDeclarations>
//		final int PROD_COMPILATIONUNIT2                                             =  81;  // <CompilationUnit> ::= <PackageDeclaration> <ImportDeclarations>
//		final int PROD_COMPILATIONUNIT3                                             =  82;  // <CompilationUnit> ::= <PackageDeclaration> <TypeDeclarations>
//		final int PROD_COMPILATIONUNIT4                                             =  83;  // <CompilationUnit> ::= <PackageDeclaration>
//		final int PROD_COMPILATIONUNIT5                                             =  84;  // <CompilationUnit> ::= <ImportDeclarations> <TypeDeclarations>
//		final int PROD_COMPILATIONUNIT6                                             =  85;  // <CompilationUnit> ::= <ImportDeclarations>
//		final int PROD_COMPILATIONUNIT7                                             =  86;  // <CompilationUnit> ::= <TypeDeclarations>
//		final int PROD_COMPILATIONUNIT8                                             =  87;  // <CompilationUnit> ::= 
//		final int PROD_IMPORTDECLARATIONS                                           =  88;  // <ImportDeclarations> ::= <ImportDeclaration>
//		final int PROD_IMPORTDECLARATIONS2                                          =  89;  // <ImportDeclarations> ::= <ImportDeclarations> <ImportDeclaration>
//		final int PROD_TYPEDECLARATIONS                                             =  90;  // <TypeDeclarations> ::= <TypeDeclaration>
//		final int PROD_TYPEDECLARATIONS2                                            =  91;  // <TypeDeclarations> ::= <TypeDeclarations> <TypeDeclaration>
		final int PROD_PACKAGEDECLARATION_PACKAGE_SEMI                              =  92;  // <PackageDeclaration> ::= package <Name> ';'
//		final int PROD_IMPORTDECLARATION                                            =  93;  // <ImportDeclaration> ::= <SingleTypeImportDeclaration>
//		final int PROD_IMPORTDECLARATION2                                           =  94;  // <ImportDeclaration> ::= <TypeImportOnDemandDeclaration>
//		final int PROD_IMPORTDECLARATION3                                           =  95;  // <ImportDeclaration> ::= <SingleStaticImportDeclaration>
//		final int PROD_IMPORTDECLARATION4                                           =  96;  // <ImportDeclaration> ::= <StaticImportOnDemandDeclaration>
		final int PROD_SINGLETYPEIMPORTDECLARATION_IMPORT_SEMI                      =  97;  // <SingleTypeImportDeclaration> ::= import <Name> ';'
		final int PROD_TYPEIMPORTONDEMANDDECLARATION_IMPORT_DOT_TIMES_SEMI          =  98;  // <TypeImportOnDemandDeclaration> ::= import <Name> '.' '*' ';'
		final int PROD_SINGLESTATICIMPORTDECLARATION_IMPORT_STATIC_SEMI             =  99;  // <SingleStaticImportDeclaration> ::= import static <Name> ';'
		final int PROD_STATICIMPORTONDEMANDDECLARATION_IMPORT_STATIC_DOT_TIMES_SEMI = 100;  // <StaticImportOnDemandDeclaration> ::= import static <Name> '.' '*' ';'
//		final int PROD_TYPEDECLARATION                                              = 101;  // <TypeDeclaration> ::= <ClassDeclaration>
//		final int PROD_TYPEDECLARATION2                                             = 102;  // <TypeDeclaration> ::= <InterfaceDeclaration>
//		final int PROD_TYPEDECLARATION_SEMI                                         = 103;  // <TypeDeclaration> ::= ';'
//		final int PROD_MODIFIERS                                                    = 104;  // <Modifiers> ::= <Modifier>
//		final int PROD_MODIFIERS2                                                   = 105;  // <Modifiers> ::= <Modifiers> <Modifier>
//		final int PROD_MODIFIERSOPT                                                 = 106;  // <ModifiersOpt> ::= <Modifiers>
//		final int PROD_MODIFIERSOPT2                                                = 107;  // <ModifiersOpt> ::= 
//		final int PROD_MODIFIER_PUBLIC                                              = 108;  // <Modifier> ::= public
//		final int PROD_MODIFIER_PROTECTED                                           = 109;  // <Modifier> ::= protected
//		final int PROD_MODIFIER_PRIVATE                                             = 110;  // <Modifier> ::= private
//		final int PROD_MODIFIER_STATIC                                              = 111;  // <Modifier> ::= static
//		final int PROD_MODIFIER_ABSTRACT                                            = 112;  // <Modifier> ::= abstract
//		final int PROD_MODIFIER_FINAL                                               = 113;  // <Modifier> ::= final
//		final int PROD_MODIFIER_NATIVE                                              = 114;  // <Modifier> ::= native
//		final int PROD_MODIFIER_SYNCHRONIZED                                        = 115;  // <Modifier> ::= synchronized
//		final int PROD_MODIFIER_TRANSIENT                                           = 116;  // <Modifier> ::= transient
//		final int PROD_MODIFIER_VOLATILE                                            = 117;  // <Modifier> ::= volatile
//		final int PROD_MODIFIER_DEFAULT                                             = 118;  // <Modifier> ::= default
//		final int PROD_MODIFIER_STRICTFP                                            = 119;  // <Modifier> ::= strictfp
//		final int PROD_CLASSDECLARATION                                             = 120;  // <ClassDeclaration> ::= <Annotations> <NormalClassDeclaration>
//		final int PROD_CLASSDECLARATION2                                            = 121;  // <ClassDeclaration> ::= <Annotations> <EnumDeclaration>
		final int PROD_NORMALCLASSDECLARATION                                       = 122;  // <NormalClassDeclaration> ::= <Modifiers> <PureClassDeclaration>
//		final int PROD_NORMALCLASSDECLARATION2                                      = 123;  // <NormalClassDeclaration> ::= <PureClassDeclaration>
		final int PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER                        = 124;  // <PureClassDeclaration> ::= class Identifier <TypeParametersOpt> <Super> <Interfaces> <ClassBody>
		final int PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER2                       = 125;  // <PureClassDeclaration> ::= class Identifier <TypeParametersOpt> <Super> <ClassBody>
		final int PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER3                       = 126;  // <PureClassDeclaration> ::= class Identifier <TypeParametersOpt> <Interfaces> <ClassBody>
		final int PROD_PURECLASSDECLARATION_CLASS_IDENTIFIER4                       = 127;  // <PureClassDeclaration> ::= class Identifier <TypeParametersOpt> <ClassBody>
//		final int PROD_TYPEPARAMETERSOPT_LT_GT                                      = 128;  // <TypeParametersOpt> ::= '<' <TypeParameters> '>'
//		final int PROD_TYPEPARAMETERSOPT                                            = 129;  // <TypeParametersOpt> ::= 
//		final int PROD_TYPEPARAMETERS                                               = 130;  // <TypeParameters> ::= <TypeParameter>
//		final int PROD_TYPEPARAMETERS_COMMA                                         = 131;  // <TypeParameters> ::= <TypeParameters> ',' <TypeParameter>
//		final int PROD_SUPER_EXTENDS                                                = 132;  // <Super> ::= extends <ClassType>
//		final int PROD_INTERFACES_IMPLEMENTS                                        = 133;  // <Interfaces> ::= implements <InterfaceTypeList>
//		final int PROD_INTERFACETYPELIST                                            = 134;  // <InterfaceTypeList> ::= <InterfaceType>
//		final int PROD_INTERFACETYPELIST_COMMA                                      = 135;  // <InterfaceTypeList> ::= <InterfaceTypeList> ',' <InterfaceType>
		final int PROD_ENUMDECLARATION_ENUM_IDENTIFIER                              = 136;  // <EnumDeclaration> ::= <ModifiersOpt> enum Identifier <Interfaces> <EnumBody>
		final int PROD_ENUMDECLARATION_ENUM_IDENTIFIER2                             = 137;  // <EnumDeclaration> ::= <ModifiersOpt> enum Identifier <EnumBody>
//		final int PROD_ENUMBODY_LBRACE_COMMA_RBRACE                                 = 138;  // <EnumBody> ::= '{' <EnumConstants> ',' <EnumBodyDeclarationsOpt> '}'
//		final int PROD_ENUMBODY_LBRACE_RBRACE                                       = 139;  // <EnumBody> ::= '{' <EnumConstants> <EnumBodyDeclarationsOpt> '}'
//		final int PROD_ENUMBODYDECLARATIONSOPT_SEMI                                 = 140;  // <EnumBodyDeclarationsOpt> ::= ';' <ClassBodyDeclarations>
//		final int PROD_ENUMBODYDECLARATIONSOPT                                      = 141;  // <EnumBodyDeclarationsOpt> ::= 
//		final int PROD_ENUMCONSTANTS                                                = 142;  // <EnumConstants> ::= <EnumConstant>
		final int PROD_ENUMCONSTANTS_COMMA                                          = 143;  // <EnumConstants> ::= <EnumConstants> ',' <EnumConstant>
		final int PROD_ENUMCONSTANT_IDENTIFIER_LPAREN_RPAREN                        = 144;  // <EnumConstant> ::= <Annotations> Identifier '(' <ArgumentList> ')' <ClassBodyOpt>
		final int PROD_ENUMCONSTANT_IDENTIFIER                                      = 145;  // <EnumConstant> ::= <Annotations> Identifier <ClassBodyOpt>
//		final int PROD_CLASSBODYOPT                                                 = 146;  // <ClassBodyOpt> ::= <ClassBody>
//		final int PROD_CLASSBODYOPT2                                                = 147;  // <ClassBodyOpt> ::= 
		final int PROD_CLASSBODY_LBRACE_RBRACE                                      = 148;  // <ClassBody> ::= '{' <ClassBodyDeclarations> '}'
//		final int PROD_CLASSBODY_LBRACE_RBRACE2                                     = 149;  // <ClassBody> ::= '{' '}'
//		final int PROD_CLASSBODYDECLARATIONS                                        = 150;  // <ClassBodyDeclarations> ::= <ClassBodyDeclaration>
		final int PROD_CLASSBODYDECLARATIONS2                                       = 151;  // <ClassBodyDeclarations> ::= <ClassBodyDeclarations> <ClassBodyDeclaration>
//		final int PROD_CLASSBODYDECLARATION                                         = 152;  // <ClassBodyDeclaration> ::= <ClassMemberDeclaration>
//		final int PROD_CLASSBODYDECLARATION2                                        = 153;  // <ClassBodyDeclaration> ::= <InstanceInitializer>
//		final int PROD_CLASSBODYDECLARATION3                                        = 154;  // <ClassBodyDeclaration> ::= <StaticInitializer>
//		final int PROD_CLASSBODYDECLARATION4                                        = 155;  // <ClassBodyDeclaration> ::= <ConstructorDeclaration>
//		final int PROD_CLASSMEMBERDECLARATION                                       = 156;  // <ClassMemberDeclaration> ::= <FieldDeclaration>
//		final int PROD_CLASSMEMBERDECLARATION2                                      = 157;  // <ClassMemberDeclaration> ::= <MethodDeclaration>
//		final int PROD_CLASSMEMBERDECLARATION3                                      = 158;  // <ClassMemberDeclaration> ::= <ClassDeclaration>
//		final int PROD_CLASSMEMBERDECLARATION4                                      = 159;  // <ClassMemberDeclaration> ::= <InterfaceDeclaration>
//		final int PROD_CLASSMEMBERDECLARATION_SEMI                                  = 160;  // <ClassMemberDeclaration> ::= ';'
		final int PROD_FIELDDECLARATION_SEMI                                        = 161;  // <FieldDeclaration> ::= <Annotations> <Modifiers> <Type> <VariableDeclarators> ';'
		final int PROD_FIELDDECLARATION_SEMI2                                       = 162;  // <FieldDeclaration> ::= <Annotations> <Type> <VariableDeclarators> ';'
//		final int PROD_VARIABLEDECLARATORS                                          = 163;  // <VariableDeclarators> ::= <VariableDeclarator>
		final int PROD_VARIABLEDECLARATORS_COMMA                                    = 164;  // <VariableDeclarators> ::= <VariableDeclarators> ',' <VariableDeclarator>
//		final int PROD_VARIABLEDECLARATOR                                           = 165;  // <VariableDeclarator> ::= <VariableDeclaratorId>
		final int PROD_VARIABLEDECLARATOR_EQ                                        = 166;  // <VariableDeclarator> ::= <VariableDeclaratorId> '=' <VariableInitializer>
//		final int PROD_VARIABLEDECLARATORID_IDENTIFIER                              = 167;  // <VariableDeclaratorId> ::= Identifier
//		final int PROD_VARIABLEDECLARATORID_IDENTIFIER2                             = 168;  // <VariableDeclaratorId> ::= Identifier <Dims>
//		final int PROD_VARIABLEINITIALIZER                                          = 169;  // <VariableInitializer> ::= <Expression>
//		final int PROD_VARIABLEINITIALIZER2                                         = 170;  // <VariableInitializer> ::= <ArrayInitializer>
		final int PROD_METHODDECLARATION                                            = 171;  // <MethodDeclaration> ::= <Annotations> <MethodHeader> <MethodBody>
		final int PROD_METHODHEADER                                                 = 172;  // <MethodHeader> ::= <Modifiers> <Type> <MethodDeclarator> <Throws>
		final int PROD_METHODHEADER2                                                = 173;  // <MethodHeader> ::= <Modifiers> <Type> <MethodDeclarator>
//		final int PROD_METHODHEADER3                                                = 174;  // <MethodHeader> ::= <Type> <MethodDeclarator> <Throws>
//		final int PROD_METHODHEADER4                                                = 175;  // <MethodHeader> ::= <Type> <MethodDeclarator>
		final int PROD_METHODHEADER_VOID                                            = 176;  // <MethodHeader> ::= <Modifiers> void <MethodDeclarator> <Throws>
		final int PROD_METHODHEADER_VOID2                                           = 177;  // <MethodHeader> ::= <Modifiers> void <MethodDeclarator>
//		final int PROD_METHODHEADER_VOID3                                           = 178;  // <MethodHeader> ::= void <MethodDeclarator> <Throws>
//		final int PROD_METHODHEADER_VOID4                                           = 179;  // <MethodHeader> ::= void <MethodDeclarator>
		final int PROD_METHODDECLARATOR_IDENTIFIER_LPAREN_RPAREN                    = 180;  // <MethodDeclarator> ::= Identifier '(' <FormalParameterList> ')'
//		final int PROD_METHODDECLARATOR_IDENTIFIER_LPAREN_RPAREN2                   = 181;  // <MethodDeclarator> ::= Identifier '(' ')'
		final int PROD_METHODDECLARATOR_IDENTIFIER_LPAREN_RPAREN3                   = 182;  // <MethodDeclarator> ::= Identifier '(' <FormalParameterList> ')' <Dims>
		final int PROD_METHODDECLARATOR_IDENTIFIER_LPAREN_RPAREN4                   = 183;  // <MethodDeclarator> ::= Identifier '(' ')' <Dims>
//		final int PROD_FORMALPARAMETERLIST                                          = 184;  // <FormalParameterList> ::= <FormalParameter>
		final int PROD_FORMALPARAMETERLIST_COMMA                                    = 185;  // <FormalParameterList> ::= <FormalParameterList> ',' <FormalParameter>
//		final int PROD_FORMALPARAMETER                                              = 186;  // <FormalParameter> ::= <Type> <VariableDeclaratorId>
		final int PROD_FORMALPARAMETER_FINAL                                        = 187;  // <FormalParameter> ::= final <Type> <VariableDeclaratorId>
//		final int PROD_FORMALPARAMETER2                                             = 188;  // <FormalParameter> ::= <ReceiverParameter>
//		final int PROD_FORMALPARAMETER3                                             = 189;  // <FormalParameter> ::= <LastFormalParameter>
		final int PROD_LASTFORMALPARAMETER_ELLIPSIS                                 = 190;  // <LastFormalParameter> ::= <Type> Ellipsis <VariableDeclaratorId>
//		final int PROD_RECEIVERPARAMETER_THIS                                       = 191;  // <ReceiverParameter> ::= <Type> <QualPrefixOpt> this
//		final int PROD_QUALPREFIXOPT_IDENTIFIER_DOT                                 = 192;  // <QualPrefixOpt> ::= Identifier '.' <QualPrefixOpt>
//		final int PROD_QUALPREFIXOPT                                                = 193;  // <QualPrefixOpt> ::= 
//		final int PROD_THROWS_THROWS                                                = 194;  // <Throws> ::= throws <ClassTypeList>
//		final int PROD_CLASSTYPELIST                                                = 195;  // <ClassTypeList> ::= <ClassType>
//		final int PROD_CLASSTYPELIST_COMMA                                          = 196;  // <ClassTypeList> ::= <ClassTypeList> ',' <ClassType>
//		final int PROD_METHODBODY                                                   = 197;  // <MethodBody> ::= <Block>
//		final int PROD_METHODBODY_SEMI                                              = 198;  // <MethodBody> ::= ';'
//		final int PROD_INSTANCEINITIALIZER                                          = 199;  // <InstanceInitializer> ::= <Annotations> <Block>
		final int PROD_STATICINITIALIZER_STATIC                                     = 200;  // <StaticInitializer> ::= <Annotations> static <Block>
		final int PROD_CONSTRUCTORDECLARATION                                       = 201;  // <ConstructorDeclaration> ::= <Annotations> <Modifiers> <ConstructorDeclarator> <Throws> <ConstructorBody>
		final int PROD_CONSTRUCTORDECLARATION2                                      = 202;  // <ConstructorDeclaration> ::= <Annotations> <Modifiers> <ConstructorDeclarator> <ConstructorBody>
		final int PROD_CONSTRUCTORDECLARATION3                                      = 203;  // <ConstructorDeclaration> ::= <Annotations> <ConstructorDeclarator> <Throws> <ConstructorBody>
		final int PROD_CONSTRUCTORDECLARATION4                                      = 204;  // <ConstructorDeclaration> ::= <Annotations> <ConstructorDeclarator> <ConstructorBody>
		final int PROD_CONSTRUCTORDECLARATOR_LPAREN_RPAREN                          = 205;  // <ConstructorDeclarator> ::= <SimpleName> '(' <FormalParameterList> ')'
		final int PROD_CONSTRUCTORDECLARATOR_LPAREN_RPAREN2                         = 206;  // <ConstructorDeclarator> ::= <SimpleName> '(' ')'
//		final int PROD_CONSTRUCTORBODY_LBRACE_RBRACE                                = 207;  // <ConstructorBody> ::= '{' <ExplicitConstructorInvocation> <BlockStatements> '}'
//		final int PROD_CONSTRUCTORBODY_LBRACE_RBRACE2                               = 208;  // <ConstructorBody> ::= '{' <ExplicitConstructorInvocation> '}'
//		final int PROD_CONSTRUCTORBODY_LBRACE_RBRACE3                               = 209;  // <ConstructorBody> ::= '{' <BlockStatements> '}'
//		final int PROD_CONSTRUCTORBODY_LBRACE_RBRACE4                               = 210;  // <ConstructorBody> ::= '{' '}'
		final int PROD_EXPLICITCONSTRUCTORINVOCATION_THIS_LPAREN_RPAREN_SEMI        = 211;  // <ExplicitConstructorInvocation> ::= this '(' <ArgumentList> ')' ';'
		final int PROD_EXPLICITCONSTRUCTORINVOCATION_THIS_LPAREN_RPAREN_SEMI2       = 212;  // <ExplicitConstructorInvocation> ::= this '(' ')' ';'
		final int PROD_EXPLICITCONSTRUCTORINVOCATION_SUPER_LPAREN_RPAREN_SEMI       = 213;  // <ExplicitConstructorInvocation> ::= super '(' <ArgumentList> ')' ';'
		final int PROD_EXPLICITCONSTRUCTORINVOCATION_SUPER_LPAREN_RPAREN_SEMI2      = 214;  // <ExplicitConstructorInvocation> ::= super '(' ')' ';'
		final int PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER                    = 215;  // <InterfaceDeclaration> ::= <Annotations> <Modifiers> interface Identifier <TypeParametersOpt> <ExtendsInterfaces> <InterfaceBody>
		final int PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER2                   = 216;  // <InterfaceDeclaration> ::= <Annotations> <Modifiers> interface Identifier <TypeParametersOpt> <InterfaceBody>
		final int PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER3                   = 217;  // <InterfaceDeclaration> ::= <Annotations> interface Identifier <TypeParametersOpt> <ExtendsInterfaces> <InterfaceBody>
		final int PROD_INTERFACEDECLARATION_INTERFACE_IDENTIFIER4                   = 218;  // <InterfaceDeclaration> ::= <Annotations> interface Identifier <TypeParametersOpt> <InterfaceBody>
//		final int PROD_EXTENDSINTERFACES_EXTENDS                                    = 219;  // <ExtendsInterfaces> ::= extends <InterfaceType>
//		final int PROD_EXTENDSINTERFACES_COMMA                                      = 220;  // <ExtendsInterfaces> ::= <ExtendsInterfaces> ',' <InterfaceType>
//		final int PROD_INTERFACEBODY_LBRACE_RBRACE                                  = 221;  // <InterfaceBody> ::= '{' <InterfaceMemberDeclarations> '}'
//		final int PROD_INTERFACEBODY_LBRACE_RBRACE2                                 = 222;  // <InterfaceBody> ::= '{' '}'
//		final int PROD_INTERFACEMEMBERDECLARATIONS                                  = 223;  // <InterfaceMemberDeclarations> ::= <InterfaceMemberDeclaration>
//		final int PROD_INTERFACEMEMBERDECLARATIONS2                                 = 224;  // <InterfaceMemberDeclarations> ::= <InterfaceMemberDeclarations> <InterfaceMemberDeclaration>
//		final int PROD_INTERFACEMEMBERDECLARATION                                   = 225;  // <InterfaceMemberDeclaration> ::= <ConstantDeclaration>
//		final int PROD_INTERFACEMEMBERDECLARATION2                                  = 226;  // <InterfaceMemberDeclaration> ::= <MethodDeclaration>
//		final int PROD_INTERFACEMEMBERDECLARATION3                                  = 227;  // <InterfaceMemberDeclaration> ::= <ClassDeclaration>
//		final int PROD_INTERFACEMEMBERDECLARATION4                                  = 228;  // <InterfaceMemberDeclaration> ::= <InterfaceDeclaration>
//		final int PROD_CONSTANTDECLARATION                                          = 229;  // <ConstantDeclaration> ::= <FieldDeclaration>
//		final int PROD_ARRAYINITIALIZER_LBRACE_COMMA_RBRACE                         = 230;  // <ArrayInitializer> ::= '{' <VariableInitializers> ',' '}'
		final int PROD_ARRAYINITIALIZER_LBRACE_RBRACE                               = 231;  // <ArrayInitializer> ::= '{' <VariableInitializers> '}'
//		final int PROD_ARRAYINITIALIZER_LBRACE_COMMA_RBRACE2                        = 232;  // <ArrayInitializer> ::= '{' ',' '}'
//		final int PROD_ARRAYINITIALIZER_LBRACE_RBRACE2                              = 233;  // <ArrayInitializer> ::= '{' '}'
//		final int PROD_VARIABLEINITIALIZERS                                         = 234;  // <VariableInitializers> ::= <VariableInitializer>
		final int PROD_VARIABLEINITIALIZERS_COMMA                                   = 235;  // <VariableInitializers> ::= <VariableInitializers> ',' <VariableInitializer>
//		final int PROD_BLOCK_LBRACE_RBRACE                                          = 236;  // <Block> ::= '{' <BlockStatements> '}'
//		final int PROD_BLOCK_LBRACE_RBRACE2                                         = 237;  // <Block> ::= '{' '}'
//		final int PROD_BLOCKSTATEMENTS                                              = 238;  // <BlockStatements> ::= <BlockStatement>
		final int PROD_BLOCKSTATEMENTS2                                             = 239;  // <BlockStatements> ::= <BlockStatements> <BlockStatement>
//		final int PROD_BLOCKSTATEMENT                                               = 240;  // <BlockStatement> ::= <LocalVariableDeclarationStatement>
//		final int PROD_BLOCKSTATEMENT2                                              = 241;  // <BlockStatement> ::= <LocalClassDeclaration>
//		final int PROD_BLOCKSTATEMENT3                                              = 242;  // <BlockStatement> ::= <Statement>
//		final int PROD_LOCALVARIABLEDECLARATIONSTATEMENT_SEMI                       = 243;  // <LocalVariableDeclarationStatement> ::= <LocalVariableDeclaration> ';'
		final int PROD_LOCALVARIABLEDECLARATION_FINAL                               = 244;  // <LocalVariableDeclaration> ::= final <Type> <VariableDeclarators>
		final int PROD_LOCALVARIABLEDECLARATION                                     = 245;  // <LocalVariableDeclaration> ::= <Type> <VariableDeclarators>
		final int PROD_LOCALCLASSDECLARATION                                        = 246;  // <LocalClassDeclaration> ::= <LocalClassModifiers> <PureClassDeclaration>
//		final int PROD_LOCALCLASSDECLARATION2                                       = 247;  // <LocalClassDeclaration> ::= <PureClassDeclaration>
//		final int PROD_LOCALCLASSMODIFIERS_ABSTRACT                                 = 248;  // <LocalClassModifiers> ::= abstract
//		final int PROD_LOCALCLASSMODIFIERS_FINAL                                    = 249;  // <LocalClassModifiers> ::= final
//		final int PROD_STATEMENT                                                    = 250;  // <Statement> ::= <StatementWithoutTrailingSubstatement>
//		final int PROD_STATEMENT2                                                   = 251;  // <Statement> ::= <LabeledStatement>
//		final int PROD_STATEMENT3                                                   = 252;  // <Statement> ::= <IfThenStatement>
//		final int PROD_STATEMENT4                                                   = 253;  // <Statement> ::= <IfThenElseStatement>
//		final int PROD_STATEMENT5                                                   = 254;  // <Statement> ::= <WhileStatement>
//		final int PROD_STATEMENT6                                                   = 255;  // <Statement> ::= <ForStatement>
//		final int PROD_STATEMENTNOSHORTIF                                           = 256;  // <StatementNoShortIf> ::= <StatementWithoutTrailingSubstatement>
//		final int PROD_STATEMENTNOSHORTIF2                                          = 257;  // <StatementNoShortIf> ::= <LabeledStatementNoShortIf>
//		final int PROD_STATEMENTNOSHORTIF3                                          = 258;  // <StatementNoShortIf> ::= <IfThenElseStatementNoShortIf>
//		final int PROD_STATEMENTNOSHORTIF4                                          = 259;  // <StatementNoShortIf> ::= <WhileStatementNoShortIf>
//		final int PROD_STATEMENTNOSHORTIF5                                          = 260;  // <StatementNoShortIf> ::= <ForStatementNoShortIf>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT                         = 261;  // <StatementWithoutTrailingSubstatement> ::= <Block>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT2                        = 262;  // <StatementWithoutTrailingSubstatement> ::= <EmptyStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT3                        = 263;  // <StatementWithoutTrailingSubstatement> ::= <ExpressionStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT4                        = 264;  // <StatementWithoutTrailingSubstatement> ::= <SwitchStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT5                        = 265;  // <StatementWithoutTrailingSubstatement> ::= <DoStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT6                        = 266;  // <StatementWithoutTrailingSubstatement> ::= <BreakStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT7                        = 267;  // <StatementWithoutTrailingSubstatement> ::= <ContinueStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT8                        = 268;  // <StatementWithoutTrailingSubstatement> ::= <ReturnStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT9                        = 269;  // <StatementWithoutTrailingSubstatement> ::= <SynchronizedStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT10                       = 270;  // <StatementWithoutTrailingSubstatement> ::= <ThrowStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT11                       = 271;  // <StatementWithoutTrailingSubstatement> ::= <TryStatement>
//		final int PROD_STATEMENTWITHOUTTRAILINGSUBSTATEMENT12                       = 272;  // <StatementWithoutTrailingSubstatement> ::= <AssertStatement>
//		final int PROD_EMPTYSTATEMENT_SEMI                                          = 273;  // <EmptyStatement> ::= ';'
		final int PROD_LABELEDSTATEMENT_IDENTIFIER_COLON                            = 274;  // <LabeledStatement> ::= Identifier ':' <Statement>
		final int PROD_LABELEDSTATEMENTNOSHORTIF_IDENTIFIER_COLON                   = 275;  // <LabeledStatementNoShortIf> ::= Identifier ':' <StatementNoShortIf>
		final int PROD_EXPRESSIONSTATEMENT_SEMI                                     = 276;  // <ExpressionStatement> ::= <StatementExpression> ';'
//		final int PROD_STATEMENTEXPRESSION                                          = 277;  // <StatementExpression> ::= <Assignment>
//		final int PROD_STATEMENTEXPRESSION2                                         = 278;  // <StatementExpression> ::= <PreIncrementExpression>
//		final int PROD_STATEMENTEXPRESSION3                                         = 279;  // <StatementExpression> ::= <PreDecrementExpression>
//		final int PROD_STATEMENTEXPRESSION4                                         = 280;  // <StatementExpression> ::= <PostIncrementExpression>
//		final int PROD_STATEMENTEXPRESSION5                                         = 281;  // <StatementExpression> ::= <PostDecrementExpression>
//		final int PROD_STATEMENTEXPRESSION6                                         = 282;  // <StatementExpression> ::= <MethodInvocation>
//		final int PROD_STATEMENTEXPRESSION7                                         = 283;  // <StatementExpression> ::= <ClassInstanceCreationExpression>
		final int PROD_IFTHENSTATEMENT_IF_LPAREN_RPAREN                             = 284;  // <IfThenStatement> ::= if '(' <Expression> ')' <Statement>
		final int PROD_IFTHENELSESTATEMENT_IF_LPAREN_RPAREN_ELSE                    = 285;  // <IfThenElseStatement> ::= if '(' <Expression> ')' <StatementNoShortIf> else <Statement>
		final int PROD_IFTHENELSESTATEMENTNOSHORTIF_IF_LPAREN_RPAREN_ELSE           = 286;  // <IfThenElseStatementNoShortIf> ::= if '(' <Expression> ')' <StatementNoShortIf> else <StatementNoShortIf>
		final int PROD_SWITCHSTATEMENT_SWITCH_LPAREN_RPAREN                         = 287;  // <SwitchStatement> ::= switch '(' <Expression> ')' <SwitchBlock>
		final int PROD_SWITCHBLOCK_LBRACE_RBRACE                                    = 288;  // <SwitchBlock> ::= '{' <SwitchBlockStatementGroups> <SwitchLabels> '}'
//		final int PROD_SWITCHBLOCK_LBRACE_RBRACE2                                   = 289;  // <SwitchBlock> ::= '{' <SwitchBlockStatementGroups> '}'
		final int PROD_SWITCHBLOCK_LBRACE_RBRACE3                                   = 290;  // <SwitchBlock> ::= '{' <SwitchLabels> '}'
		final int PROD_SWITCHBLOCK_LBRACE_RBRACE4                                   = 291;  // <SwitchBlock> ::= '{' '}'
//		final int PROD_SWITCHBLOCKSTATEMENTGROUPS                                   = 292;  // <SwitchBlockStatementGroups> ::= <SwitchBlockStatementGroup>
		final int PROD_SWITCHBLOCKSTATEMENTGROUPS2                                  = 293;  // <SwitchBlockStatementGroups> ::= <SwitchBlockStatementGroups> <SwitchBlockStatementGroup>
//		final int PROD_SWITCHBLOCKSTATEMENTGROUP                                    = 294;  // <SwitchBlockStatementGroup> ::= <SwitchLabels> <BlockStatements>
//		final int PROD_SWITCHLABELS                                                 = 295;  // <SwitchLabels> ::= <SwitchLabel>
		final int PROD_SWITCHLABELS2                                                = 296;  // <SwitchLabels> ::= <SwitchLabels> <SwitchLabel>
//		final int PROD_SWITCHLABEL_CASE_COLON                                       = 297;  // <SwitchLabel> ::= case <ConstantExpression> ':'
		final int PROD_SWITCHLABEL_DEFAULT_COLON                                    = 298;  // <SwitchLabel> ::= default ':'
		final int PROD_WHILESTATEMENT_WHILE_LPAREN_RPAREN                           = 299;  // <WhileStatement> ::= while '(' <Expression> ')' <Statement>
		final int PROD_WHILESTATEMENTNOSHORTIF_WHILE_LPAREN_RPAREN                  = 300;  // <WhileStatementNoShortIf> ::= while '(' <Expression> ')' <StatementNoShortIf>
		final int PROD_DOSTATEMENT_DO_WHILE_LPAREN_RPAREN_SEMI                      = 301;  // <DoStatement> ::= do <Statement> while '(' <Expression> ')' ';'
//		final int PROD_FORSTATEMENT                                                 = 302;  // <ForStatement> ::= <BasicForStatement>
//		final int PROD_FORSTATEMENT2                                                = 303;  // <ForStatement> ::= <EnhancedForStatement>
		final int PROD_BASICFORSTATEMENT_FOR_LPAREN_SEMI_SEMI_RPAREN                = 304;  // <BasicForStatement> ::= for '(' <ForInitOpt> ';' <ExpressionOpt> ';' <ForUpdateOpt> ')' <Statement>
//		final int PROD_EXPRESSIONOPT                                                = 305;  // <ExpressionOpt> ::= <Expression>
//		final int PROD_EXPRESSIONOPT2                                               = 306;  // <ExpressionOpt> ::= 
//		final int PROD_FORSTATEMENTNOSHORTIF                                        = 307;  // <ForStatementNoShortIf> ::= <BasicForStatementNoShortIf>
//		final int PROD_FORSTATEMENTNOSHORTIF2                                       = 308;  // <ForStatementNoShortIf> ::= <EnhancedForStatementNoShortIf>
		final int PROD_BASICFORSTATEMENTNOSHORTIF_FOR_LPAREN_SEMI_SEMI_RPAREN       = 309;  // <BasicForStatementNoShortIf> ::= for '(' <ForInitOpt> ';' <ExpressionOpt> ';' <ForUpdateOpt> ')' <StatementNoShortIf>
//		final int PROD_FORINITOPT                                                   = 310;  // <ForInitOpt> ::= <StatementExpressionList>
//		final int PROD_FORINITOPT2                                                  = 311;  // <ForInitOpt> ::= <LocalVariableDeclaration>
//		final int PROD_FORINITOPT3                                                  = 312;  // <ForInitOpt> ::= 
//		final int PROD_FORUPDATEOPT                                                 = 313;  // <ForUpdateOpt> ::= <StatementExpressionList>
//		final int PROD_FORUPDATEOPT2                                                = 314;  // <ForUpdateOpt> ::= 
		final int PROD_ENHANCEDFORSTATEMENT_FOR_LPAREN_FINAL_COLON_RPAREN           = 315;  // <EnhancedForStatement> ::= for '(' final <Type> <VariableDeclaratorId> ':' <Expression> ')' <Statement>
		final int PROD_ENHANCEDFORSTATEMENT_FOR_LPAREN_COLON_RPAREN                 = 316;  // <EnhancedForStatement> ::= for '(' <Type> <VariableDeclaratorId> ':' <Expression> ')' <Statement>
		final int PROD_ENHANCEDFORSTATEMENTNOSHORTIF_FOR_LPAREN_FINAL_COLON_RPAREN  = 317;  // <EnhancedForStatementNoShortIf> ::= for '(' final <Type> <VariableDeclaratorId> ':' <Expression> ')' <StatementNoShortIf>
		final int PROD_ENHANCEDFORSTATEMENTNOSHORTIF_FOR_LPAREN_COLON_RPAREN        = 318;  // <EnhancedForStatementNoShortIf> ::= for '(' <Type> <VariableDeclaratorId> ':' <Expression> ')' <StatementNoShortIf>
//		final int PROD_STATEMENTEXPRESSIONLIST                                      = 319;  // <StatementExpressionList> ::= <StatementExpression>
//		final int PROD_STATEMENTEXPRESSIONLIST_COMMA                                = 320;  // <StatementExpressionList> ::= <StatementExpressionList> ',' <StatementExpression>
		final int PROD_BREAKSTATEMENT_BREAK_IDENTIFIER_SEMI                         = 321;  // <BreakStatement> ::= break Identifier ';'
		final int PROD_BREAKSTATEMENT_BREAK_SEMI                                    = 322;  // <BreakStatement> ::= break ';'
		final int PROD_CONTINUESTATEMENT_CONTINUE_IDENTIFIER_SEMI                   = 323;  // <ContinueStatement> ::= continue Identifier ';'
		final int PROD_CONTINUESTATEMENT_CONTINUE_SEMI                              = 324;  // <ContinueStatement> ::= continue ';'
		final int PROD_RETURNSTATEMENT_RETURN_SEMI                                  = 325;  // <ReturnStatement> ::= return <Expression> ';'
		final int PROD_RETURNSTATEMENT_RETURN_SEMI2                                 = 326;  // <ReturnStatement> ::= return ';'
		final int PROD_THROWSTATEMENT_THROW_SEMI                                    = 327;  // <ThrowStatement> ::= throw <Expression> ';'
		final int PROD_SYNCHRONIZEDSTATEMENT_SYNCHRONIZED_LPAREN_RPAREN             = 328;  // <SynchronizedStatement> ::= synchronized '(' <Expression> ')' <Block>
		final int PROD_TRYSTATEMENT_TRY                                             = 329;  // <TryStatement> ::= try <Block> <Catches>
		final int PROD_TRYSTATEMENT_TRY2                                            = 330;  // <TryStatement> ::= try <Block> <Catches> <Finally>
		final int PROD_TRYSTATEMENT_TRY3                                            = 331;  // <TryStatement> ::= try <Block> <Finally>
		final int PROD_TRYSTATEMENT_TRY4                                            = 332;  // <TryStatement> ::= try <ResourceSpecification> <Block>
		final int PROD_TRYSTATEMENT_TRY5                                            = 333;  // <TryStatement> ::= try <ResourceSpecification> <Block> <Catches>
		final int PROD_TRYSTATEMENT_TRY6                                            = 334;  // <TryStatement> ::= try <ResourceSpecification> <Block> <Catches> <Finally>
		final int PROD_TRYSTATEMENT_TRY7                                            = 335;  // <TryStatement> ::= try <ResourceSpecification> <Block> <Finally>
//		final int PROD_CATCHES                                                      = 336;  // <Catches> ::= <CatchClause>
		final int PROD_CATCHES2                                                     = 337;  // <Catches> ::= <Catches> <CatchClause>
//		final int PROD_CATCHCLAUSE_CATCH_LPAREN_RPAREN                              = 338;  // <CatchClause> ::= catch '(' <CatchFormalParameter> ')' <Block>
		final int PROD_CATCHFORMALPARAMETER_FINAL                                   = 339;  // <CatchFormalParameter> ::= final <CatchType> <VariableDeclaratorId>
//		final int PROD_CATCHFORMALPARAMETER                                         = 340;  // <CatchFormalParameter> ::= <CatchType> <VariableDeclaratorId>
//		final int PROD_CATCHTYPE                                                    = 341;  // <CatchType> ::= <ClassType>
		final int PROD_CATCHTYPE_PIPE                                               = 342;  // <CatchType> ::= <CatchType> '|' <ClassType>
//		final int PROD_FINALLY_FINALLY                                              = 343;  // <Finally> ::= finally <Block>
		final int PROD_ASSERTSTATEMENT_ASSERT                                       = 344;  // <AssertStatement> ::= assert <Expression> <AssertMessageOpt>
//		final int PROD_ASSERTMESSAGEOPT_COLON                                       = 345;  // <AssertMessageOpt> ::= ':' <Expression>
//		final int PROD_ASSERTMESSAGEOPT                                             = 346;  // <AssertMessageOpt> ::= 
//		final int PROD_RESOURCESPECIFICATION_LPAREN_RPAREN                          = 347;  // <ResourceSpecification> ::= '(' <Resources> ')'
//		final int PROD_RESOURCESPECIFICATION_LPAREN_SEMI_RPAREN                     = 348;  // <ResourceSpecification> ::= '(' <Resources> ';' ')'
//		final int PROD_RESOURCES                                                    = 349;  // <Resources> ::= <Resource>
		final int PROD_RESOURCES_SEMI                                               = 350;  // <Resources> ::= <Resources> ';' <Resource>
//		final int PROD_RESOURCE_EQ                                                  = 351;  // <Resource> ::= <Type> <VariableDeclaratorId> '=' <Expression>
		final int PROD_RESOURCE_FINAL_EQ                                            = 352;  // <Resource> ::= final <Type> <VariableDeclaratorId> '=' <Expression>
//		final int PROD_PRIMARY                                                      = 353;  // <Primary> ::= <PrimaryNoNewArray>
//		final int PROD_PRIMARY2                                                     = 354;  // <Primary> ::= <ArrayCreationExpression>
//		final int PROD_PRIMARYNONEWARRAY                                            = 355;  // <PrimaryNoNewArray> ::= <Literal>
//		final int PROD_PRIMARYNONEWARRAY_THIS                                       = 356;  // <PrimaryNoNewArray> ::= this
		final int PROD_PRIMARYNONEWARRAY_LPAREN_RPAREN                              = 357;  // <PrimaryNoNewArray> ::= '(' <Expression> ')'
//		final int PROD_PRIMARYNONEWARRAY2                                           = 358;  // <PrimaryNoNewArray> ::= <ClassInstanceCreationExpression>
//		final int PROD_PRIMARYNONEWARRAY3                                           = 359;  // <PrimaryNoNewArray> ::= <FieldAccess>
//		final int PROD_PRIMARYNONEWARRAY4                                           = 360;  // <PrimaryNoNewArray> ::= <MethodInvocation>
//		final int PROD_PRIMARYNONEWARRAY5                                           = 361;  // <PrimaryNoNewArray> ::= <ArrayAccess>
//		final int PROD_PRIMARYNONEWARRAY6                                           = 362;  // <PrimaryNoNewArray> ::= <ProcessingTypeConversion>
		final int PROD_CLASSINSTANCECREATIONEXPRESSION_NEW_LPAREN_RPAREN            = 363;  // <ClassInstanceCreationExpression> ::= new <ClassType> '(' <ArgumentList> ')'
		final int PROD_CLASSINSTANCECREATIONEXPRESSION_NEW_LPAREN_RPAREN2           = 364;  // <ClassInstanceCreationExpression> ::= new <ClassType> '(' ')'
		final int PROD_CLASSINSTANCECREATIONEXPRESSION_NEW_LPAREN_RPAREN3           = 365;  // <ClassInstanceCreationExpression> ::= new <ClassType> '(' <ArgumentList> ')' <ClassBody>
		final int PROD_CLASSINSTANCECREATIONEXPRESSION_NEW_LPAREN_RPAREN4           = 366;  // <ClassInstanceCreationExpression> ::= new <ClassType> '(' ')' <ClassBody>
//		final int PROD_ARGUMENTLIST                                                 = 367;  // <ArgumentList> ::= <Expression>
		final int PROD_ARGUMENTLIST_COMMA                                           = 368;  // <ArgumentList> ::= <ArgumentList> ',' <Expression>
		final int PROD_ARRAYCREATIONEXPRESSION_NEW                                  = 369;  // <ArrayCreationExpression> ::= new <PrimitiveType> <DimExprs> <Dims>
		final int PROD_ARRAYCREATIONEXPRESSION_NEW2                                 = 370;  // <ArrayCreationExpression> ::= new <PrimitiveType> <DimExprs>
		final int PROD_ARRAYCREATIONEXPRESSION_NEW3                                 = 371;  // <ArrayCreationExpression> ::= new <ClassOrInterfaceType> <DimExprs> <Dims>
		final int PROD_ARRAYCREATIONEXPRESSION_NEW4                                 = 372;  // <ArrayCreationExpression> ::= new <ClassOrInterfaceType> <DimExprs>
		final int PROD_ARRAYCREATIONEXPRESSION_NEW5                                 = 373;  // <ArrayCreationExpression> ::= new <PrimitiveType> <Dims> <ArrayInitializer>
		final int PROD_ARRAYCREATIONEXPRESSION_NEW6                                 = 374;  // <ArrayCreationExpression> ::= new <ClassOrInterfaceType> <Dims> <ArrayInitializer>
//		final int PROD_DIMEXPRS                                                     = 375;  // <DimExprs> ::= <DimExpr>
//		final int PROD_DIMEXPRS2                                                    = 376;  // <DimExprs> ::= <DimExprs> <DimExpr>
//		final int PROD_DIMEXPR_LBRACKET_RBRACKET                                    = 377;  // <DimExpr> ::= '[' <Expression> ']'
//		final int PROD_DIMS_LBRACKET_RBRACKET                                       = 378;  // <Dims> ::= '[' ']'
//		final int PROD_DIMS_LBRACKET_RBRACKET2                                      = 379;  // <Dims> ::= <Dims> '[' ']'
		final int PROD_FIELDACCESS_DOT_IDENTIFIER                                   = 380;  // <FieldAccess> ::= <Primary> '.' Identifier
		final int PROD_FIELDACCESS_SUPER_DOT_IDENTIFIER                             = 381;  // <FieldAccess> ::= super '.' Identifier
		final int PROD_METHODINVOCATION_LPAREN_RPAREN                               = 382;  // <MethodInvocation> ::= <Name> '(' <ArgumentList> ')'
		final int PROD_METHODINVOCATION_LPAREN_RPAREN2                              = 383;  // <MethodInvocation> ::= <Name> '(' ')'
		final int PROD_METHODINVOCATION_DOT_IDENTIFIER_LPAREN_RPAREN                = 384;  // <MethodInvocation> ::= <Primary> '.' Identifier '(' <ArgumentList> ')'
		final int PROD_METHODINVOCATION_DOT_IDENTIFIER_LPAREN_RPAREN2               = 385;  // <MethodInvocation> ::= <Primary> '.' Identifier '(' ')'
		final int PROD_METHODINVOCATION_SUPER_DOT_IDENTIFIER_LPAREN_RPAREN          = 386;  // <MethodInvocation> ::= super '.' Identifier '(' <ArgumentList> ')'
		final int PROD_METHODINVOCATION_SUPER_DOT_IDENTIFIER_LPAREN_RPAREN2         = 387;  // <MethodInvocation> ::= super '.' Identifier '(' ')'
		final int PROD_PROCESSINGTYPECONVERSION_INT_LPAREN_RPAREN                   = 388;  // <ProcessingTypeConversion> ::= int '(' <Expression> ')'
		final int PROD_PROCESSINGTYPECONVERSION_BYTE_LPAREN_RPAREN                  = 389;  // <ProcessingTypeConversion> ::= byte '(' <Expression> ')'
		final int PROD_PROCESSINGTYPECONVERSION_CHAR_LPAREN_RPAREN                  = 390;  // <ProcessingTypeConversion> ::= char '(' <Expression> ')'
		final int PROD_PROCESSINGTYPECONVERSION_FLOAT_LPAREN_RPAREN                 = 391;  // <ProcessingTypeConversion> ::= float '(' <Expression> ')'
		final int PROD_PROCESSINGTYPECONVERSION_BOOLEAN_LPAREN_RPAREN               = 392;  // <ProcessingTypeConversion> ::= boolean '(' <Expression> ')'
		final int PROD_ARRAYACCESS_LBRACKET_RBRACKET                                = 393;  // <ArrayAccess> ::= <Name> '[' <Expression> ']'
		final int PROD_ARRAYACCESS_LBRACKET_RBRACKET2                               = 394;  // <ArrayAccess> ::= <PrimaryNoNewArray> '[' <Expression> ']'
//		final int PROD_POSTFIXEXPRESSION                                            = 395;  // <PostfixExpression> ::= <Primary>
//		final int PROD_POSTFIXEXPRESSION2                                           = 396;  // <PostfixExpression> ::= <Name>
//		final int PROD_POSTFIXEXPRESSION3                                           = 397;  // <PostfixExpression> ::= <PostIncrementExpression>
//		final int PROD_POSTFIXEXPRESSION4                                           = 398;  // <PostfixExpression> ::= <PostDecrementExpression>
		final int PROD_POSTINCREMENTEXPRESSION_PLUSPLUS                             = 399;  // <PostIncrementExpression> ::= <PostfixExpression> '++'
		final int PROD_POSTDECREMENTEXPRESSION_MINUSMINUS                           = 400;  // <PostDecrementExpression> ::= <PostfixExpression> '--'
//		final int PROD_UNARYEXPRESSION                                              = 401;  // <UnaryExpression> ::= <PreIncrementExpression>
//		final int PROD_UNARYEXPRESSION2                                             = 402;  // <UnaryExpression> ::= <PreDecrementExpression>
		final int PROD_UNARYEXPRESSION_PLUS                                         = 403;  // <UnaryExpression> ::= '+' <UnaryExpression>
		final int PROD_UNARYEXPRESSION_MINUS                                        = 404;  // <UnaryExpression> ::= '-' <UnaryExpression>
//		final int PROD_UNARYEXPRESSION3                                             = 405;  // <UnaryExpression> ::= <UnaryExpressionNotPlusMinus>
		final int PROD_PREINCREMENTEXPRESSION_PLUSPLUS                              = 406;  // <PreIncrementExpression> ::= '++' <UnaryExpression>
		final int PROD_PREDECREMENTEXPRESSION_MINUSMINUS                            = 407;  // <PreDecrementExpression> ::= '--' <UnaryExpression>
//		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS                                  = 408;  // <UnaryExpressionNotPlusMinus> ::= <PostfixExpression>
		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS_TILDE                            = 409;  // <UnaryExpressionNotPlusMinus> ::= '~' <UnaryExpression>
		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS_EXCLAM                           = 410;  // <UnaryExpressionNotPlusMinus> ::= '!' <UnaryExpression>
//		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS2                                 = 411;  // <UnaryExpressionNotPlusMinus> ::= <CastExpression>
		final int PROD_CASTEXPRESSION_LPAREN_RPAREN                                 = 412;  // <CastExpression> ::= '(' <PrimitiveType> <Dims> ')' <UnaryExpression>
		final int PROD_CASTEXPRESSION_LPAREN_RPAREN2                                = 413;  // <CastExpression> ::= '(' <PrimitiveType> ')' <UnaryExpression>
		final int PROD_CASTEXPRESSION_LPAREN_RPAREN3                                = 414;  // <CastExpression> ::= '(' <Expression> ')' <UnaryExpressionNotPlusMinus>
		final int PROD_CASTEXPRESSION_LPAREN_RPAREN4                                = 415;  // <CastExpression> ::= '(' <Name> <Dims> ')' <UnaryExpressionNotPlusMinus>
//		final int PROD_MULTIPLICATIVEEXPRESSION                                     = 416;  // <MultiplicativeExpression> ::= <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_TIMES                               = 417;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> '*' <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_DIV                                 = 418;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> '/' <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_PERCENT                             = 419;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> '%' <UnaryExpression>
//		final int PROD_ADDITIVEEXPRESSION                                           = 420;  // <AdditiveExpression> ::= <MultiplicativeExpression>
		final int PROD_ADDITIVEEXPRESSION_PLUS                                      = 421;  // <AdditiveExpression> ::= <AdditiveExpression> '+' <MultiplicativeExpression>
		final int PROD_ADDITIVEEXPRESSION_MINUS                                     = 422;  // <AdditiveExpression> ::= <AdditiveExpression> '-' <MultiplicativeExpression>
//		final int PROD_SHIFTEXPRESSION                                              = 423;  // <ShiftExpression> ::= <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_LTLT                                         = 424;  // <ShiftExpression> ::= <ShiftExpression> '<<' <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_GTGT                                         = 425;  // <ShiftExpression> ::= <ShiftExpression> '>>' <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_GTGTGT                                       = 426;  // <ShiftExpression> ::= <ShiftExpression> '>>>' <AdditiveExpression>
//		final int PROD_RELATIONALEXPRESSION                                         = 427;  // <RelationalExpression> ::= <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_LT                                      = 428;  // <RelationalExpression> ::= <RelationalExpression> '<' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_GT                                      = 429;  // <RelationalExpression> ::= <RelationalExpression> '>' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_LTEQ                                    = 430;  // <RelationalExpression> ::= <RelationalExpression> '<=' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_GTEQ                                    = 431;  // <RelationalExpression> ::= <RelationalExpression> '>=' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_INSTANCEOF                              = 432;  // <RelationalExpression> ::= <RelationalExpression> instanceof <ReferenceType>
//		final int PROD_EQUALITYEXPRESSION                                           = 433;  // <EqualityExpression> ::= <RelationalExpression>
		final int PROD_EQUALITYEXPRESSION_EQEQ                                      = 434;  // <EqualityExpression> ::= <EqualityExpression> '==' <RelationalExpression>
		final int PROD_EQUALITYEXPRESSION_EXCLAMEQ                                  = 435;  // <EqualityExpression> ::= <EqualityExpression> '!=' <RelationalExpression>
//		final int PROD_ANDEXPRESSION                                                = 436;  // <AndExpression> ::= <EqualityExpression>
		final int PROD_ANDEXPRESSION_AMP                                            = 437;  // <AndExpression> ::= <AndExpression> '&' <EqualityExpression>
//		final int PROD_EXCLUSIVEOREXPRESSION                                        = 438;  // <ExclusiveOrExpression> ::= <AndExpression>
		final int PROD_EXCLUSIVEOREXPRESSION_CARET                                  = 439;  // <ExclusiveOrExpression> ::= <ExclusiveOrExpression> '^' <AndExpression>
//		final int PROD_INCLUSIVEOREXPRESSION                                        = 440;  // <InclusiveOrExpression> ::= <ExclusiveOrExpression>
		final int PROD_INCLUSIVEOREXPRESSION_PIPE                                   = 441;  // <InclusiveOrExpression> ::= <InclusiveOrExpression> '|' <ExclusiveOrExpression>
//		final int PROD_CONDITIONALANDEXPRESSION                                     = 442;  // <ConditionalAndExpression> ::= <InclusiveOrExpression>
		final int PROD_CONDITIONALANDEXPRESSION_AMPAMP                              = 443;  // <ConditionalAndExpression> ::= <ConditionalAndExpression> '&&' <InclusiveOrExpression>
//		final int PROD_CONDITIONALOREXPRESSION                                      = 444;  // <ConditionalOrExpression> ::= <ConditionalAndExpression>
		final int PROD_CONDITIONALOREXPRESSION_PIPEPIPE                             = 445;  // <ConditionalOrExpression> ::= <ConditionalOrExpression> '||' <ConditionalAndExpression>
//		final int PROD_CONDITIONALEXPRESSION                                        = 446;  // <ConditionalExpression> ::= <ConditionalOrExpression>
		final int PROD_CONDITIONALEXPRESSION_QUESTION_COLON                         = 447;  // <ConditionalExpression> ::= <ConditionalOrExpression> '?' <Expression> ':' <ConditionalExpression>
//		final int PROD_ASSIGNMENTEXPRESSION                                         = 448;  // <AssignmentExpression> ::= <ConditionalExpression>
//		final int PROD_ASSIGNMENTEXPRESSION2                                        = 449;  // <AssignmentExpression> ::= <Assignment>
		final int PROD_ASSIGNMENT                                                   = 450;  // <Assignment> ::= <LeftHandSide> <AssignmentOperator> <AssignmentExpression>
//		final int PROD_LEFTHANDSIDE                                                 = 451;  // <LeftHandSide> ::= <Name>
//		final int PROD_LEFTHANDSIDE2                                                = 452;  // <LeftHandSide> ::= <FieldAccess>
//		final int PROD_LEFTHANDSIDE3                                                = 453;  // <LeftHandSide> ::= <ArrayAccess>
		final int PROD_ASSIGNMENTOPERATOR_EQ                                        = 454;  // <AssignmentOperator> ::= '='
//		final int PROD_ASSIGNMENTOPERATOR_TIMESEQ                                   = 455;  // <AssignmentOperator> ::= '*='
//		final int PROD_ASSIGNMENTOPERATOR_DIVEQ                                     = 456;  // <AssignmentOperator> ::= '/='
//		final int PROD_ASSIGNMENTOPERATOR_PERCENTEQ                                 = 457;  // <AssignmentOperator> ::= '%='
		final int PROD_ASSIGNMENTOPERATOR_PLUSEQ                                    = 458;  // <AssignmentOperator> ::= '+='
		final int PROD_ASSIGNMENTOPERATOR_MINUSEQ                                   = 459;  // <AssignmentOperator> ::= '-='
//		final int PROD_ASSIGNMENTOPERATOR_LTLTEQ                                    = 460;  // <AssignmentOperator> ::= '<<='
//		final int PROD_ASSIGNMENTOPERATOR_GTGTEQ                                    = 461;  // <AssignmentOperator> ::= '>>='
//		final int PROD_ASSIGNMENTOPERATOR_GTGTGTEQ                                  = 462;  // <AssignmentOperator> ::= '>>>='
//		final int PROD_ASSIGNMENTOPERATOR_AMPEQ                                     = 463;  // <AssignmentOperator> ::= '&='
//		final int PROD_ASSIGNMENTOPERATOR_CARETEQ                                   = 464;  // <AssignmentOperator> ::= '^='
//		final int PROD_ASSIGNMENTOPERATOR_PIPEEQ                                    = 465;  // <AssignmentOperator> ::= '|='
//		final int PROD_EXPRESSION                                                   = 466;  // <Expression> ::= <AssignmentExpression>
//		final int PROD_CONSTANTEXPRESSION                                           = 467;  // <ConstantExpression> ::= <Expression>
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
			// START KGU#1131 2024-03-17: Bugfix #1142 assert statement hadn't been considered
			RuleConstants.PROD_ASSERTSTATEMENT_ASSERT,
			// END KGU#1131 2024-03-17
			// START KGU#957 2021-03-05: Bugfix #959
			// START KGU#1120 2024-03-11: Bugfix #1135 Unnecessary rules caused ID conflicts
			//RuleConstants.PROD_PROCESSINGTYPECONVERSION_BINARY_LPAREN_RPAREN,
			//RuleConstants.PROD_PROCESSINGTYPECONVERSION_HEX_LPAREN_RPAREN,
			//RuleConstants.PROD_PROCESSINGTYPECONVERSION_UNBINARY_LPAREN_RPAREN,
			//RuleConstants.PROD_PROCESSINGTYPECONVERSION_UNHEX_LPAREN_RPAREN,
			// END KGU#1120 2024-03-11
			RuleConstants.PROD_PROCESSINGTYPECONVERSION_INT_LPAREN_RPAREN,
			RuleConstants.PROD_PROCESSINGTYPECONVERSION_BYTE_LPAREN_RPAREN,
			RuleConstants.PROD_PROCESSINGTYPECONVERSION_CHAR_LPAREN_RPAREN,
			// START KGU#1120 2024-03-11: Bugfix #1135 Unnecessary rules caused ID conflicts
			//RuleConstants.PROD_PROCESSINGTYPECONVERSION_STR_LPAREN_RPAREN,
			// END KGU#1120 2024-03-11
			RuleConstants.PROD_PROCESSINGTYPECONVERSION_FLOAT_LPAREN_RPAREN,
			RuleConstants.PROD_PROCESSINGTYPECONVERSION_BOOLEAN_LPAREN_RPAREN,
			// END KGU#957 2021-03-05
	};
	
	//----------------------------- Preprocessor -----------------------------
	
	// START KGU#953 2021-03-04: Bugfix #955 The original approach seemed clever but left an empty string at start
	//static final StringList CLASS_LITERAL = StringList.explodeWithDelimiter(".class", ".");
	// START KGU#1123 2024-03-12/18: Bugfix #1137 Similar workaround for ".this" necessary
	//static final StringList CLASS_LITERAL = StringList.explode(".:class", ":");
	/** Contains pairs of keywords possibly used like component names and their temporary replacements */
	static final String[][] KEYWORDS_AS_COMPNAMES= new String[][] {
		{"class", "c_l_a_s_s"},
		{"this", "t_h_i_s"},
		//{"new", "n_e_w"} // This had to be solved in the grammar (as a qualified new operator)
	};
	// END KGU#1123 2024-03-12/18
	// END KGU#953 2021-03-04
	
	// START KGU#1122 2024-03-12: Bugfix #1136 Temporary replacements for certain angular brackets
	/** A String array with temporary substitutes for '<' (at [0]), ',' (at [1]), '>' (at [2]),
	 * '[' (at [3]), and ']' (at [4]) within type castings */
	static final String[] ANG_BRACK_SUBST = {"íí", "îî", "ìì", "úú", "ùù"};
	// END KGU#1122 2024-03-12

	/**
	 * Performs some necessary preprocessing for the text file. Actually opens the
	 * file, filters it and writes a new temporary file "Structorizer&lt;random&gt;.{defaultExt}",
	 * which is then actually parsed.<br/>
	 * The preprocessed file will always be saved with UTF-8 encoding.<br/>
	 * NOTE: For interactive mode, there should be frequent tests with either
	 * {@link #isCancelled()} or {@link #doStandardCancelActionIfRequested()} whether
	 * the parser thread was asked to stop. If so, then a return or an exception are
	 * recommended in order to respond to the cancel request.
	 * 
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
		 * The grammar does not cope with type arguments in type casting, as e.g. in
		 * Vector<String> strings = (Vector<String>)getLines("blabla\nhoho\nmore");
		 * We want to provide a workaround.
		 * Another well-known problem is that the closing angular brackets
		 * of type parameters or arguments clash together and are then mistaken
		 * as a shift operator. So we have to separate them since it is
		 * beyond the power of our grammar to solve this.
		 */
		final Matcher castMatcher = Pattern.compile(".*[>]\\s*[)].*").matcher("");
		final Matcher arrayDeclMatcher = Pattern.compile(".*[>]\\s*\\[\\s*\\].*").matcher("");
		
		// START KGU#1122 2024-03-12: Bugfix #1136 New option to separate angular type parameter brackets
		boolean separateAngularBrackets = (Boolean)this.getPluginOption("separate_angular_brackets", true);
		// END KGU#1122 2024-03-12

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
					// START KGU#1123 2024-03-12: Bugfix #1137 ".this" may also cause trouble
					//if (strLine.contains("class")) {
					boolean containsKeyword = false;
					for (String[] keywordPair: KEYWORDS_AS_COMPNAMES) {
						if (strLine.contains(keywordPair[0])) {
							containsKeyword = true;
							break;
						}
					}
					if (containsKeyword) {
					// END KGU#1123 2024-03-12
						// Tokenization is to make sure that we don't substitute in wrong places
						StringList tokens = Element.splitLexically(strLine, true);
						boolean replaced = false;
						// START KGU#1123 2024-03-12/18: Bugfix #1137 ".this" and ".new" may also cause trouble
						//int ixKeyword = -1;
						//while ((ixKeyword = tokens.indexOf(CLASS_LITERAL, 0, true)) >= 0) {
						//	tokens.set(ixKeyword+1, "c_l_a_s_s");
						//	// START KGU#1117/KGU#1123 2024-03-17: Issues #1131, #1137 Proper solution
						//	replacedIds.putIfAbsent("c_l_a_s_s", "class");
						//	// END KGU#1117/KGU#1123 2024-03-17
						//	replaced = true;
						//}
						for (String[] keywordPair: KEYWORDS_AS_COMPNAMES) {
							int ixKeyword = -1;
							StringList keyTokens = new StringList(new String[]{".", keywordPair[0]});
							while ((ixKeyword = tokens.indexOf(keyTokens, 0, true)) >= 0) {
								tokens.set(ixKeyword+1, keywordPair[1]);
								replacedIds.putIfAbsent(keywordPair[1], keywordPair[0]);
								replaced = true;
							}
						}
						// END KGU#1123 2024-03-12
						if (replaced) {
							strLine = tokens.concatenate();
						}
					}
					// START KGU#1122 2024-03-12: Bugfix #1136
					castMatcher.reset(strLine);
					arrayDeclMatcher.reset(strLine);
					if (castMatcher.matches() || arrayDeclMatcher.matches()
							|| strLine.contains(">>") || strLine.contains("?>") || strLine.contains("<>")) {
						boolean replaced = false;
						StringList tokens = Element.splitLexically(strLine, true);
						// Initially decompose all ">>" tokens into ">", ">" ...
						int posAngBr = -1;
						while ((posAngBr = tokens.indexOf(">>", posAngBr+1)) >= 0) {
							tokens.set(posAngBr, ">");
							tokens.insert(">", ++posAngBr);
							// This is no modification w.r.t. the string representation
						}
						// START KGU#1122 2024-03-18: Bugfix #1136 can only be an empty type param list
						// ... get rid of "<>" tokens ...
						if (tokens.removeAll("<>") > 0) {
							replaced = true;
						}
						// END KGU#1122 2024-03-18
						// ... and remove all token sequences "<", "?", ">" and "<", ">".
						posAngBr = -1;
						while ((posAngBr = tokens.indexOf(">", posAngBr+1)) >= 0) {
							int pos = posAngBr - 1;
							while (pos > 0 && tokens.get(pos).isBlank()) pos--;
							if (pos > 0 && tokens.get(pos).equals("?")) {
								// Look for preceding "<" in order to verify "<", "?", ">"
								while (--pos >= 0 && tokens.get(pos).isBlank());
								if (pos >= 0 && tokens.get(pos).equals("<")) {
									// Found token sequence "<", "?", ">", so efface it
									tokens.remove(pos, posAngBr+1);
									replaced = true;
									posAngBr = pos;
								}
							}
							// START KGU#1122 2024-03-18: Bugfix #1136
							else if (pos > 0 && tokens.get(pos).equals("<")) {
								// Found token sequence "<", ">", which can only be an empty type param list
								tokens.remove(pos, posAngBr+1);
								replaced = true;
								posAngBr = pos;
							}
							// END KGU#1122 2024-03-18
						}
						
						// Now we first look for type casts or array specs with type arguments and modify them
						replaced = transformParameterisedTypes(tokens) || replaced;
						// Second, we look for clashing closing angular brackets.
						if (separateAngularBrackets) {
							replaced = separateAngularTypeBrackets(tokens) || replaced;
						}
						if (replaced) {
							strLine = tokens.concatenate();
						}
					}
					// END KGU#1122 2024-03-12
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

	// START KGU#1122 2024-03-17: Bugfix #1136 Methods to solve angular bracket trouble
	/**
	 * Derives a single pseudo identifier from parameterized types with casting
	 * parentheses or in array specifications, e.g.<br/>
	 * {@code (Vector<Integer>)expression} &rarr; {@code (VectorííIntegerìì)expression}<br/>
	 * {@code JComboBox<String>[]} &rarr; {@code JComboBoxííIntegerìì[]}<br/>
	 * If a replacement takes place adds its backward mapping to {@link CodeParser#replacedIds}.
	 * 
	 * @param tokens - a tokenized line to be transformed in the described way
	 *    at all matching places. May be modified (then returns {@code true}).
	 * @return {@code true} if {@code tokens} was modified and a related entry is member
	 *    of {@link CodeParser#replacedIds}, {@code false} otherwise.
	 */
	private boolean transformParameterisedTypes(StringList tokens) {
		boolean replaced = false;
		int posAngBr = -1;
		while ((posAngBr = tokens.indexOf(">", posAngBr+1)) >= 0) {
			// Go ahead and look for a closing parenthesis or an opening bracket...
			int posPar1 = posAngBr + 1;
			while (posPar1 < tokens.count() && tokens.get(posPar1).isBlank()) posPar1++;
			/* If there is a closing parenthesis then we walk backwards along
			 * possible type constructs in order to replace all angular brackets
			 * by "íí" and "ìì", respectively until we reach the opening parenthesis.
			 * but it's a little more tricky as there might be lists of type parameters
			 * and some of the types might be array types, e.g.
			 * (HashMap<String, Object[]>). So, for ',', '[', and ']' substítutions
			 * are also necessary. The major question here is, how precise our syntax
			 * check has to be.
			 */
			boolean isCast = false;
			if (posPar1 < tokens.count() && ((isCast = tokens.get(posPar1).equals(")")) || tokens.get(posPar1).equals("["))) {
				/* First make sure the construct is complete within this line
				 * We must only find identifiers, '.', ',', '[]', '<', and '>'
				 * (and possibly comments!?) between the parentheses...
				 * We start with a rather rough backwards search and do a more
				 * precise syntax check in forward direction after that.
				 */
				int posPar0 = posAngBr - 1;
				int angCount = 1;
				int posId = -1;
				while (posPar0 >= 0 && !tokens.get(posPar0).equals("(") && angCount >= 0) {
					String token = tokens.get(posPar0);
					if (token.isBlank()) {
						// We want to produce a single pseudo identifier, so remove all blanks
						tokens.remove(posPar0);
						posPar1--;
						posAngBr--;
						posId--;
					}
					else if (token.equals(">")) {
						angCount++;
					}
					else if (token.equals("<")) {
						angCount--;
						// There must be an identifier before the opening '<'
						posId = posPar0 - 1;
						while (angCount >= 0 && posId >= 0 && tokens.get(posId).isBlank()) posId--;
						if (posId < 0 || !Function.testIdentifier(tokens.get(posId), false, "$")) {
							break;
						}
						else if (!isCast && angCount == 0) {
							posPar0 = posId;
							break;
						}
					}
					else if (!Function.testIdentifier(token, false, "$")
							&& !(token.length() == 1 && "[],.".contains(token))) {
						break;
					}
					posPar0--;
				}
				if (posPar0 >= 0 && (!isCast || tokens.get(posPar0).equals("(")) && angCount == 0
						&& isTypeSpecificationList(tokens.subSequence(posId, posAngBr+1))) {
					String origSequence = tokens.concatenate("", posId, posAngBr+1);
					// We should have a dense token sequence now and produce a pseudo-identifier
					tokens.replaceAllBetween("<", ANG_BRACK_SUBST[0], true, posPar0+1, posPar1);
					tokens.replaceAllBetween(">", ANG_BRACK_SUBST[2], true, posPar0+1, posPar1);
					tokens.replaceAllBetween(",", ANG_BRACK_SUBST[1], true, posPar0+1, posPar1);
					tokens.replaceAllBetween("[", ANG_BRACK_SUBST[3], true, posPar0+1, posPar1);
					tokens.replaceAllBetween("]", ANG_BRACK_SUBST[4], true, posPar0+1, posPar1);
					// Ensure the operator symbols will be restored after the parsing
					this.replacedIds.putIfAbsent(tokens.concatenate("", posId, posAngBr+1), origSequence);
					replaced = true;
				}
			}
		}
		return replaced;
	}
	// END KGU#1122 2024-03-17

	// START KGU#1122 2024-03-12: Bugfix #1136 Methods to solve angular bracket trouble
	/**
	 * Tries to separate closing angular brackets of nested type parmeters as
	 * in {@code HashMap<String, ArrayList<String[]>>} within the given token
	 * list {@code tokens} by blanks in order to avoid a parser error because
	 * of the mistaken assumption, {@code >>} formed a shift operator.
	 * 
	 * @param tokens - a tokenized source code line, where ">>" and ">>>" tokens
	 *    must already have been split into two or three consecutive tokens ">".
	 * @return {@code true} if the given {@code tokens} were modified, {@code false}
	 *    otherwise.
	 */
	private boolean separateAngularTypeBrackets(StringList tokens) {
		boolean replaced = false;
		int posAngBr = -1;
		while ((posAngBr = tokens.indexOf(">", posAngBr + 1)) >= 0) {
			if (posAngBr < tokens.count() - 1 && tokens.get(posAngBr+1).equals(">")) {
				// Again, roughly scan backwards for an opening angular bracket
				int angCount = 1;
				int posAngBr0 = posAngBr - 1;
				while (posAngBr0 >= 0 && angCount > 0) {
					String token = tokens.get(posAngBr0);
					if (token.equals(">")) {
						angCount++;
					}
					else if (token.equals("<") && --angCount == 0) {
						if (isTypeSpecificationList(tokens.subSequence(posAngBr0 + 1, posAngBr))) {
							// Separate the two closing angular brackets
							tokens.insert(" ", ++posAngBr);
							replaced = true;
						}
						break;
					}
					else if (!token.isBlank()
							&& !Function.testIdentifier(token, false, "$")
							&& !(token.length() == 1 && "[],.".contains(token))) {
						break;
					}
					posAngBr0--;
				}
			}
		}
		return replaced;
	}

	/**
	 * Verifies the syntax of (possibly nested) type specifications like
	 * {@code HashMap<String, Vector<String[]>>, List<Double>}
	 * 
	 * @param subSequence - token sequence assumed to specify a comma-separated
	 *    list of (possibly parameterised) types.
	 * @return {@code true} if the given token list does not obviously violate
	 *    the expected syntax of a comma-separated list of (possibly parameterised)
	 *    type specifiers.
	 */
	private boolean isTypeSpecificationList(StringList subSequence) {
		// TODO (by now the parsing in transformParameterisedTypes() was good enough)
		//System.out.println("(Not) checking type list \"" + subSequence + "\" in JavaParser.isTypeSpecification()");
		return true;
	}
	// END KGU#1122 2024-03-12
	
	/**
	 * Allows subclasses to do some extra preparations to the preprocessed
	 * content given as {@code _srcCode}, considering the source file {@code _file}
	 * 
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
		root.children.addElement(new Forever());	// For types
		root.children.addElement(new Forever());	// not need at outermost level
		
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
				
				/*
				 * What situations do we have to face?
				 * - if this.includables is empty then we are at the outermost level, but
				 *    it is not necessarily the first class/interface at this level
				 *    (i.e. currentRoot is not necessarily = this.root)
				 * - currentRoot == this.root then this is either the first (currentRoot empty)
				 *    or the second class (curentRoot not empty) at the outermost level.
				 * - currentRoot is an Includable - then we are defining an inner class and
				 *    we will add to this.includables
				 * - currentRoot is no Includable but a subroutine then we are defining a
				 *    local class within the method block. We will add to includables and
				 *    have currentRoot including this new Root.
				 */
				
				// START KGU#1134 2024-03-18: Bugfix #1145 we better distinguish context
				boolean isLocal = ruleId == RuleConstants.PROD_LOCALCLASSDECLARATION;
				// END KGU#1134 2024-03-18
				// Fetch comment and append modifiers
				String modifiers = "";
				int ixName = 1;
				Reduction redClass = _reduction;
				if (ruleId == RuleConstants.PROD_NORMALCLASSDECLARATION
						|| isLocal) {
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
				// START KGU#1134 2024-03-18: Bugfix #1145
				//Root classRoot = root;
				Root classRoot = Element.getRoot(_parentNode);
				Root outerClass = null;
				// END KGU#1134 2024-03-18
				if (!this.includables.isEmpty()) {
					// START KGU#1134 2024-03-18: Bugfix #1145
					//qualifier = this.includables.peek().getQualifiedName();
					qualifier = (outerClass = this.includables.peek()).getQualifiedName();
				}
				if (outerClass != null || !isEmptyClassRoot(classRoot)) {
					Root currentRoot = classRoot;
				// END KGU#1134 2024-03-18
					classRoot = new Root();
					classRoot.setInclude();
					if (outerClass != null) {
						// In case of a global class shall we actually establish this?
						classRoot.addToIncludeList(outerClass);
					}
					// START KGU#1134 2024-03-18: Bugfix #1145
					if (!currentRoot.isInclude()) {
						currentRoot.addToIncludeList(name);
					}
					// END KGU#1134 2024-03-18
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
				// START KGU#1134 2024-03-18: Bugfix# 1154 We must not this for the outermost diagram now
				// Dissolve the field and method containers
				//dissolveDummyContainers(classRoot);
				// END KGU#1134 2024-03-18
				this.includables.pop();
			}
			break;
			
			case RuleConstants.PROD_ENUMDECLARATION_ENUM_IDENTIFIER:
			case RuleConstants.PROD_ENUMDECLARATION_ENUM_IDENTIFIER2:
			{
				// START KGU#1134 2024-03-18: Bugfix #1145 Trouble with multiple classes on top level
				//buildEnumeratorDefinition(_reduction, ruleId);
				buildEnumeratorDefinition(_reduction, ruleId, _parentNode);
				// eND KGU#1134 2024-03-18
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
				ele.setColor(COLOR_GLOBAL);
				if (allConstant) {
					ele.setColor(COLOR_CONST);
				}
				if (modifiers != null) {
					ele.comment.add(modifiers);
				}
				// FIXME #1145 We must cope with more than one class per file
				((Forever)_parentNode.getElement(0)).getBody().addElement(ele);
				// END KGU#1134 2024-03-18
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
					decl.setColor(COLOR_DECL);
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
					ele.setColor(COLOR_CONST);
				}
				else {
					ele.setColor(COLOR_DECL);
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
					prep.setColor(COLOR_MISC);
					_parentNode.addElement(prep);
				}
				Alternative alt = new Alternative(cond.get(ixLast));
				this.equipWithSourceComment(alt, _reduction);
				if (ixLast > 0) {
					alt.setColor(COLOR_MISC);
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
						_parentNode.getElement(i).setColor(COLOR_MISC);
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
							prep.setColor(COLOR_MISC);
							_parentNode.addElement(prep);
						}
						While loop = new While((getOptKeyword("preWhile", false, true)
								+ translateContent(cond.get(ixLast))
								+ getOptKeyword("postWhile", true, false)).trim());
						ele = loop;
						body = loop.getBody();
					}
					// Mark all offsprings of the FOR loop with a (by default) yellowish colour
					ele.setColor(COLOR_MISC);
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
						body.getElement(i).setColor(COLOR_MISC);
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
					instr.setColor(COLOR_MISC);
					_parentNode.addElement(instr);
				}
				//String valList = this.translateContent(this.getContent_R(_reduction.get(ixType + 3)));
				StringList valList = decomposeExpression(_reduction.get(ixType + 3), false, false);
				int ixLast = valList.count() - 1;
				if (ixLast > 0) {
					Instruction prep = new Instruction(valList.subSequence(0, ixLast));
					prep.setColor(COLOR_MISC);
					_parentNode.addElement(prep);
				}
				Element forIn = this.equipWithSourceComment(new For(loopVar, valList.get(ixLast)), _reduction);
				_parentNode.addElement(forIn);
				// Build the loop body
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
				// Build the loop body
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
				// Build the loop body
				this.buildNSD_R(_reduction.get(1).asReduction(), loop.getBody());
				// Did the condition contain some assignments or other side effects?
				if (ixLast > 0) {
					// Yes, create an element for the preparation instructions and append it to the body
					Instruction prep = new Instruction(cond.subSequence(0, ixLast));
					prep.setColor(COLOR_MISC);
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
						disp.setColor(COLOR_MISC);
						finAlt.qTrue.addElement(disp);
						finAlt.setColor(COLOR_MISC);
					} while (redRscs != null);
					
					// Insert the resource declaration block before the TRY element
					Instruction decls = new Instruction(textRscDecls.reverse());
					decls.setColor(COLOR_MISC);
					decls.setComment("FIXME: The initialisation with null is Java-specific");
					_parentNode.addElement(decls);
					ele.setColor(COLOR_MISC);
					
					// Put the combined resource acquisition instruction into the TRY block
					ele.qTry.addElement(new Instruction(textRscInits.reverse()));
					
					ixBlock++;
				}
				// Build the protected TRY block
				this.buildNSD_R(_reduction.get(ixBlock).asReduction(), ele.qTry);
				// Care for the CATCH block(s)
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
				// Check if there is a FINALLY block (and where)
				int ixFinally = 0;
				if (ruleId == RuleConstants.PROD_TRYSTATEMENT_TRY2) {
					ixFinally = 3;
				}
				else if (ruleId == RuleConstants.PROD_TRYSTATEMENT_TRY3) {
					ixFinally = 2;
				}
				if (ixFinally > 0) {
					// <Finally> ::= finally <Block>
					// Build the FINALLY block
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
			
			// START KGU#1131 2024-03-17: Bugfix #1142 assert statements hadn't been catered for
			case RuleConstants.PROD_ASSERTSTATEMENT_ASSERT:
			{
				// <AssertStatement> ::= assert <Expression> <AssertMessageOpt>
				String cond = this.getContent_R(_reduction.get(1));
				Alternative alt = new Alternative(Element.negateCondition(cond));
				this.equipWithSourceComment(alt, _reduction);
				String message = "\"Assertion failed: " + cond + "\"";
				if (_reduction.get(2).asReduction().size() > 0) {
					// <AssertMessageOpt> ::= ':' <Expression>
					message = this.getContent_R(_reduction.get(2).asReduction().get(1));
				}
				Jump jmp = new Jump(getKeyword("preThrow") + " " + message);
				alt.qTrue.addElement(jmp);
				_parentNode.addElement(alt);
			}
			break;
			// END KGU#1131 2024-03-17
			
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
				ele.setColor(COLOR_CONST);
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
			
			// TODO add the handling of further instruction types here...
			
			// START KGU#1130 2024-03-17: Bugfix #1141 Measures against stack overflow risk
			case RuleConstants.PROD_CLASSBODYDECLARATIONS2:
			case RuleConstants.PROD_BLOCKSTATEMENTS2:
			{
				// <ClassBodyDeclarations> ::= <ClassBodyDeclarations> <ClassBodyDeclaration>
				// <BlockStatements> ::= <BlockStatements> <BlockStatement>
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
			break;
			// END KGU#1130 2024-03-17
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

	// START KGU#1134 2024-03-18: Bugfix #1145 Trouble with more than one class at top level
	/**
	 * Checks whether the given Root is an "empty" Includable for a class definition.
	 * 
	 * @param classRoot - the potential diagram to place a new class representation
	 * @return {@code true} if {@code classRoot} is an Includable containing exactly two
	 *     empty Forever loops, {@code false} otherwise
	 */
	private boolean isEmptyClassRoot(Root classRoot) {
		Element ele;
		return classRoot.isInclude()
				&& classRoot.children.getSize() == 2
				&& ((ele = classRoot.children.getElement(0)) instanceof Forever)
				&& ((Forever)ele).getBody().getSize() == 0
				&& ((ele = classRoot.children.getElement(1)) instanceof Forever)
				&& ((Forever)ele).getBody().getSize() == 0;
	}
	// END KGU#1134 2024-03-18

	// START KGU#959 2021-03-06: Issue #961 extracted from decomposeExpression() for overloading
	/**
	 * Checks whether the passed-in instruction line (which must adhere to a
	 * method invocation with or without assigned result, where the assignment
	 * symbol if contained is expected to be "<-") represents some built-in
	 * function or command, e.g. an output instruction, and if so converts it
	 * accordingly. If it is nothing specific then just returns {@code null}.
	 * 
	 * @param line - a built instruction line with call syntax (qualified names
	 *    possible)
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
	 * For the composition of Includables representing classes the field and
	 * method delaration have been collected in two dummy Forever loops. These
	 * are here deconstructed and their contents merged into the top subqueue.
	 * 
	 * @param classRoot - the Includable diagram representing a declared class
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
	 * 
	 * @param _reduction - The {@link Reduction} meant to represent an {@code  <EnumDeclaration>}
	 * rule.
	 * @param _ruleId - the actual table index of the rule
	 * @param _parentNode - the target {@link Subqueue}
	 * @throws ParserCancelled if the user happened to abort the parsing process
	 */
	private void buildEnumeratorDefinition(Reduction _reduction, int _ruleId, Subqueue _parentNode) throws ParserCancelled {
		// <EnumDeclaration> ::= <ModifiersOpt> enum Identifier <Interfaces> <EnumBody>
		// <EnumDeclaration> ::= <ModifiersOpt> enum Identifier <EnumBody>
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
			// START KGU#1134 2024-03-18: Bugfix #1145 We might be within a method
			Root currentRoot = Element.getRoot(_parentNode);
			// END KGU#1134 2024-03-18
			Root enumRoot = root;
			String qualifier = packageStr;
			// START KGU#1134 2024-03-18: Bugfix #1145 more than one top-level classes caused error
			//if (!includables.isEmpty()) {
			if (!includables.isEmpty() || !this.isEmptyClassRoot(enumRoot)) {
			// END KGU#1134 2024-03-18
				enumRoot = new Root();
				enumRoot.setInclude();
				// Add temporary dummy loops in order to gather fields and method signatures
				enumRoot.children.addElement(new Forever());
				enumRoot.children.addElement(new Forever());
				// START KGU#1134 2024-03-18: Bugfix #1145 more than one top-level classes caused error
				//qualifier = includables.peek().getQualifiedName();
				//enumRoot.addToIncludeList(includables.peek());
				if (!includables.isEmpty()) {
					qualifier = includables.peek().getQualifiedName();
					enumRoot.addToIncludeList(includables.peek());
					if (!currentRoot.isInclude()) {
						currentRoot.addToIncludeList(enumRoot);
					}
				}
				// END KGU#1134 2024-03-18
				addRoot(enumRoot);
			}
			enumRoot.setText(name);
			enumRoot.setNamespace(qualifier);
			this.equipWithSourceComment(enumRoot, _reduction);
			// START KGU#1132 2024-03-18: Bugfix #1143 enum does not need modifiers
			//enumRoot.comment.add(modifiers);
			if (!modifiers.isBlank()) {
				enumRoot.comment.add(modifiers);
			}
			// END KGU#1132 2024-03-18
			// Provide information about inherited interfaces if given
			if (_ruleId == RuleConstants.PROD_ENUMDECLARATION_ENUM_IDENTIFIER) {
				 // <EnumDeclaration> ::= <ModifiersOpt> enum Identifier <Interfaces> <EnumBody>
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
				itemDecl.setColor(COLOR_CONST);
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
			// START KGU#1134 2024-03-18: Bugfix #1145 Postpone this to the postprocessing
			//this.dissolveDummyContainers(enumRoot);
			// END KGU#1134 2024-03-18
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
			// START KGU#1134 2024-03-18: Bugfix #1145 we may be in different contexts
			//Root targetRoot = root;
			//if (!includables.isEmpty()) {
			//	targetRoot = includables.peek();
			//	this.equipWithSourceComment(ele, _reduction);
			//}
			//else {
			//	// We are on the outermost level
			//	root.setText(name);
			//	this.equipWithSourceComment(root, _reduction);
			//}
			Root targetRoot = Element.getRoot(_parentNode);
			if (targetRoot == root && this.isEmptyClassRoot(targetRoot)) {
				root.setText(name);
			}
			//((Forever)targetRoot.children.getElement(0)).getBody().addElement(ele);			
			// START KGU#1134 2024-03-18: Bugfix #1145 We may not be on a class level but in a method
			if (targetRoot.isInclude()) {
				((Forever)targetRoot.children.getElement(0)).getBody().addElement(ele);
			}
			else {
				_parentNode.addElement(ele);
			}
			// END KGU#1134 2024-03-18
			this.equipWithSourceComment(ele, _reduction);
			ele.comment.add(itemComments.reverse());
			// START KGU#1132 2024-03-18: Bugfix #1143 enum does not need modifiers
			//ele.comment.add(modifiers);
			if (!modifiers.isBlank()) {
				ele.comment.add(modifiers);
			}
			// END KGUU#1132 2024-03-18
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
					// START KGU#1123 2024-03-17: Bugfix #1137 Delegated to getContent_R()
					//result.add(this.getContent_R(exprRed.get(i)).replace("c_l_a_s_s", "class"));
					result.add(this.getContent_R(exprRed.get(i)));
					// END KGU#1123 2024-03-17
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
				// START KGU#1123 2024-03-17: Bugfix #1137 Delegated to getContent_R()
				//exprs.add(getContent_R(exprRed, "").replace("c_l_a_s_s", "class"));
				exprs.add(getContent_R(exprRed, ""));
				// END KGU#1123 2024-03-17
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
			// START KGU#1120 2024-03-11: Bugfix #1135 Unnecessary rules caused ID conflicts
			//case RuleConstants.PROD_PROCESSINGTYPECONVERSION_BINARY_LPAREN_RPAREN:
			//case RuleConstants.PROD_PROCESSINGTYPECONVERSION_HEX_LPAREN_RPAREN:
			//case RuleConstants.PROD_PROCESSINGTYPECONVERSION_UNBINARY_LPAREN_RPAREN:
			//case RuleConstants.PROD_PROCESSINGTYPECONVERSION_UNHEX_LPAREN_RPAREN:
			// END KGU#1120 2024-03-11
			case RuleConstants.PROD_PROCESSINGTYPECONVERSION_INT_LPAREN_RPAREN:
			case RuleConstants.PROD_PROCESSINGTYPECONVERSION_BYTE_LPAREN_RPAREN:
			case RuleConstants.PROD_PROCESSINGTYPECONVERSION_CHAR_LPAREN_RPAREN:
			// START KGU#1120 2024-03-11: Bugfix #1135 Unnecessary rules caused ID conflicts
			//case RuleConstants.PROD_PROCESSINGTYPECONVERSION_STR_LPAREN_RPAREN:
			// END KGU#1120 2024-03-11
			case RuleConstants.PROD_PROCESSINGTYPECONVERSION_FLOAT_LPAREN_RPAREN:
			case RuleConstants.PROD_PROCESSINGTYPECONVERSION_BOOLEAN_LPAREN_RPAREN:
			{
				// <ProcessingTypeConversion> ::= int '(' <Expression> ')'
				// <ProcessingTypeConversion> ::= byte '(' <Expression> ')'
				// <ProcessingTypeConversion> ::= char '(' <Expression> ')'
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
						// START KGU#1123 2024-03-17: Bugfix #1137 Delegated to getConten_R()
						//+ exprRed.get(2).asString().replace("c_l_a_s_s", "class"));
						+ exprRed.get(2).asString());
						// END KGU#1123 2024-03-17
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
				exprs.add(this.getContent_R(exprToken));
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
		if (!this.includables.isEmpty()) {
			// (Should always be the case here)
			qualifier = this.includables.peek().getQualifiedName();
		}
		Root classRoot = new Root();
		classRoot.setInclude();
		classRoot.addToIncludeList(includables.peek());
		// Add temporary dummy loops in order to gather fields and method signatures
		classRoot.children.addElement(new Forever());
		classRoot.children.addElement(new Forever());
		this.addRoot(classRoot);
		includables.push(classRoot);
		classRoot.setNamespace(qualifier);
		String className = className0 + "_" + Integer.toHexString(classRoot.hashCode());
		classRoot.setText(className);
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
			instr.setColor(COLOR_MISC);
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
	 * 
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
	 * 
	 * @param _token - the {@link Token} the content is to be appended to
	 *    {@code _content}
	 * @return the content string (may be empty in case of noise)
	 * @throws ParserCancelled
	 */
	protected String getContent_R(Token _token) throws ParserCancelled
	{
		if (_token.getType() == SymbolType.NON_TERMINAL) {
			return getContent_R(_token.asReduction(), "");
		}
		else if (_token.getType() == SymbolType.CONTENT) {
			// START KGU#1122 2024-03-17: Bugfix #1136 revert preprocessing substitutions
			//return _token.asString();
			//return _token.asString().replace(ANG_BRACK_SUBST[0], "<").replace(ANG_BRACK_SUBST[2], ">")
			//		.replace(ANG_BRACK_SUBST[1], ",")
			//		.replace(ANG_BRACK_SUBST[3], "[").replace(ANG_BRACK_SUBST[4], "]");
			return undoIdReplacements(_token.asString());
			// END KGU#1122 2024-03-17
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
					// START KGU#1117/KGU#1123 2024-03-17: Issues #1131, #1137 Proper solution
					if (idx == SymbolConstants.SYM_IDENTIFIER) {
						toAdd = undoIdReplacements(toAdd);
					}
					// END KGU#1117/KGU#1123 2024-03-17
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
		// START KGU#1134 2024-03-18: Bugfix #1145
		if (aRoot.isInclude()) {
			this.dissolveDummyContainers(aRoot);
		}
		// END KGU#1134 2024-03-18
		return false;
	}

}

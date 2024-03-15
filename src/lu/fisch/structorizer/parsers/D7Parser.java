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

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    Class to parse a Pascal file.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Bob Fisch       2008-01-06      First Issue
 *      Bob Fisch       2008-05-02      Added filter for (* ... *) comment filtering
 *      Kay Gürtzig     2015-10-20      New setting stepFor (KGU#3, to be made configurable!)
 *      Kay Gürtzig     2016-03-20      New settings preForIn and postForIn added (KGU#61, #84/#135)
 *      Kay Gürtzig     2016-03-25      KGU#163: New static method getAllPropeties() added for Analyser
 *                                      KGU#165: New option ignoreCase
 *      Kay Gürtzig     2016-04-04      KGU#165: Default for ignoreCase changed to true
 *      Kay Gürtzig     2016-04-30      Issue #182 (KGU#191): More information on error exit in parse()
 *      Kay Gürtzig     2016-05-02      Issue #184 / Enh. #10: Flaws in FOR loop import (KGU#192)
 *      Kay Gürtzig     2016-05-04      KGU#194: Bugfix - parse() now with Charset argument
 *      Kay Gürtzig     2016-05-05/09   Issue #185: Import now copes with units and multiple routines per file
 *      Kay Gürtzig     2016-07-07      Enh. #185: Identification of Calls improved on parsing
 *      Kay Gürtzig     2016-09-25      Method getPropertyMap() added for more generic keyword handling (Enh. #253)
 *      Bob Fisch       2016-11-03      Bugfix #278 (NoSuchMethodError) in loadFromIni()
 *      Kay Gürtzig     2016-11-06      Bugfix #279: New methods keywordSet(), getKeywordOrDefault() etc.
 *      Kay Gürtzig     2016-11-08      Bugfix #281/#282 in method setKeyword() (Java 1.8 method HashMap.replace())
 *      Kay Gürtzig     2017-01-06      Issue #327: French default parser keywords replaced by English ones
 *      Kay Gürtzig     2017-03-04      Enh. #354: Inheritance to CodeParser introduced, Structorizer keyword
 *                                      configuration moved to superclass CodeParser.
 *      Kay Gürtzig     2017.03.08      Modified for GOLDParser 5.0, also required to convert (* *) comments
 *      Kay Gürtzig     2017.03.26      Fix #357: New temp file mechanism for the prepared text file
 *      Kay Gürtzig     2017.03.29      Enh. #368: Evaluation of constant definitions and var declarations enabled
 *      Kay Gürtzig     2017.03.31      Enh. #388: new constants concept in Structorizer supported
 *      Kay Gürtzig     2017.06.22      Enh. #420: Prepared for comment retrieval
 *      Kay Gürtzig     2017.09.22      Enh. #388 + #423: Import of types and structured initializers (var/const) fixed
 *                                      Enh. #389 Import of Units and program declarations to includables
 *      Kay Gürtzig     2018.07.11      Enh. #558: Provisional enumeration type import (as constant defs.), un-used
 *                                      rule constants disabled
 *      Kay Gürtzig     2018.09.17      Issue #594 Last remnants of com.stevesoft.pat.Regex replaced
 *      Kay Gürtzig     2018.09.28      Bugfix #613: Include relations with empty includables should be eliminated;
 *                                      Bugfix #614: Redundant result assignments in function diagrams removed
 *                                      Workaround #615: Replace comment delimiters (* *) with { } in preparation phase
 *      Kay Gürtzig     2019-03-23      Enh. #56: Import of Try and Raise instructions implemented.
 *      Kay Gürtzig     2019-11-19      Enh. #739: Genuine enumeration type import (revision of #558)
 *      Kay Gürtzig     2020-03-08      Bugfix #833: Parameter parentheses ensured, superfluous Includable suppressed
 *      Kay Gürtzig     2020-03-09      Bugfix #835: Structure preference keywords must not be glued to expressions
 *      Kay Gürtzig     2020-04-12      Bugfix #847 ensured that ids like 'false', 'true', and built-in functions be lowercase
 *      Kay Gürtzig     2020-04-24      Bugfix #861/2 duplicate procedure comments prevented (was revealed by
 *                                      by a modification of the block comment in PasGenerator.
 *      Kay Gürtzig     2021-02-15/16   Unicode import enabled after eliminating a grammar ambiguity, comment
 *                                      processing no longer necessary, either, hence '\n' substitution also dropped
 *      Kay Gürtzig     2021-10-03      Mechanism to ensure case-sensitive matching of result variables with function name
 *      Kay Gürtzig     2022-12-21      Deprecation annotation added to filterNonAscii()
 *      Kay Gürtzig     2024-03-14      Issue #1084 RuleConstants updated to new grammar version 1.6
 *      Kay Gürtzig     2024-03-15      Issue #1084 Substantial achievements to import ObjectPascal / Delphi code
 *
 ******************************************************************************************************
 *
 *      Comment:		While setting up this class (v1.0), I had a deep look at the following package:
 *
 *     Licensed Material - Property of Matthew Hawkins (hawkini@myrealbox.com)
 *     GOLDParser - code ported from VB - Author Devin Cook. All rights reserved.
 *     Modifications to this code are allowed as it is a helper class to use the engine.
 *     Source File:    AppleSample.java<br>
 *     Author:         Matthew Hawkins<br>
 *     Description:    A Sample class, takes in a set of files and runs the GOLDParser
 *					   engine on them.<br>
 *
 ******************************************************************************************************///

import java.io.*;

import com.creativewidgetworks.goldparser.engine.*;
import com.creativewidgetworks.goldparser.engine.enums.SymbolType;

import lu.fisch.structorizer.elements.Alternative;
import lu.fisch.structorizer.elements.Call;
import lu.fisch.structorizer.elements.Case;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.Forever;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.elements.Jump;
import lu.fisch.structorizer.elements.Repeat;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.structorizer.elements.Try;
import lu.fisch.structorizer.elements.While;
import lu.fisch.utils.BString;
import lu.fisch.utils.StringList;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;

/**
 * Code import parser class of Structorizer 3.27, based on GOLDParser 5.0 for the ObjectPascal, Pascal
 * or Delphi 7 language.
 * This file contains grammar-specific constants and individual routines to build
 * structograms (Nassi-Shneiderman diagrams) from the parsing tree. 
 * @author Kay Gürtzig
 */
public class D7Parser extends CodeParser
{
	/** Default diagram name for an importable program diagram with global definitions */
	private static final String DEFAULT_GLOBAL_SUFFIX = "Globals";

 	/**
 	 * Class to parse a Pascal (or Delphi 7, more precisely) file, generating a Structogram.
 	 * @author Bob Fisch
 	 */
 	public D7Parser() {
 	}

	//---------------------- File Filter configuration ---------------------------
	
	@Override
	public String getDialogTitle() {
		return "Pascal";
	}

	//@Override
	protected String getFileDescription() {
		return "Pascal Source Files";
	}

	//@Override
	public String[] getFileExtensions() {
		final String[] exts = { "pas", "dpr", "lpr" };
		return exts;
	}

	//------------------- Comment delimiter specification ---------------------------------
	
	// START KGU#407 2017-09-30: Enh. #420
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.parsers.CodeParser#getCommentDelimiters()
	 */
	@Override
	protected String[][] getCommentDelimiters()
	{
		return new String[][]{
			{"(*", "*)"},
			{"{", "}"},
			{"//"}
		};
	}
	// END KGU#407 2017-09-30
	
	//---------------------- Grammar specification ---------------------------
	
	@Override
	protected final String getCompiledGrammar()
	{
		// START KGU#387 2021-02-15: Issue #939 Eventually succeeded to generate a new parser table
		//return "D7Grammar.cgt";
		return "D7Grammar.egt";
		// END KGU#387 2021-02-15
	}
	
	@Override
	protected final String getGrammarTableName()
	{
		return "ObjectPascal";
	}
	
	//------------------- Grammar table constants DON'T MODIFY! ---------------------------
	
	// START KGU#358 2017-03-29: Enh. #368 - rule constants inserted to facilitate build phase
	// START KGU#387 2021-02-15: Issue #939 Grammar revised (disambiguated)
	// Symbolic constants naming the table indices of the grammar rules
	private interface RuleConstants
	{
//		final int PROD_LCONST_DECLITERAL                           =   0;  // <LCONST> ::= DecLiteral
//		final int PROD_ICONST_DECLITERAL                           =   1;  // <ICONST> ::= DecLiteral
//		final int PROD_ICONST_HEXLITERAL                           =   2;  // <ICONST> ::= HexLiteral
//		final int PROD_RCONST_FLOATLITERAL                         =   3;  // <RCONST> ::= FloatLiteral
//		final int PROD_RCONST_REALLITERAL                          =   4;  // <RCONST> ::= RealLiteral
//		final int PROD_SCONST_STRINGLITERAL                        =   5;  // <SCONST> ::= StringLiteral
//		final int PROD_SCONST_CARET_ID                             =   6;  // <SCONST> ::= '^' id
//		final int PROD_UNITIDLIST_COMMA                            =   7;  // <UnitIdList> ::= <UnitIdList> ',' <FieldDesignator>
//		final int PROD_UNITIDLIST                                  =   8;  // <UnitIdList> ::= <FieldDesignator>
//		final int PROD_IDLIST_COMMA                                =   9;  // <IdList> ::= <IdList> ',' <RefId>
//		final int PROD_IDLIST                                      =  10;  // <IdList> ::= <RefId>
//		final int PROD_LABELID_ID                                  =  11;  // <LabelId> ::= id
//		final int PROD_TYPEID_ID                                   =  12;  // <TypeId> ::= id
//		final int PROD_TYPEID_NAME                                 =  13;  // <TypeId> ::= NAME
//		final int PROD_TYPENAME                                    =  14;  // <TypeName> ::= <TypeId>
//		final int PROD_TYPENAME_ID_DOT                             =  15;  // <TypeName> ::= id '.' <RefId>
//		final int PROD_TYPELIST                                    =  16;  // <TypeList> ::= <TypeName>
//		final int PROD_TYPELIST_COMMA                              =  17;  // <TypeList> ::= <TypeList> ',' <TypeName>
		final int PROD_REFID_ID                                    =  18;  // <RefId> ::= id
//		final int PROD_REFID_AT                                    =  19;  // <RefId> ::= AT
//		final int PROD_REFID_ON                                    =  20;  // <RefId> ::= ON
//		final int PROD_REFID_READ                                  =  21;  // <RefId> ::= READ
//		final int PROD_REFID_WRITE                                 =  22;  // <RefId> ::= WRITE
//		final int PROD_REFID_READLN                                =  23;  // <RefId> ::= READLN
//		final int PROD_REFID_WRITELN                               =  24;  // <RefId> ::= WRITELN
//		final int PROD_REFID_NAME                                  =  25;  // <RefId> ::= NAME
//		final int PROD_REFID_INDEX                                 =  26;  // <RefId> ::= INDEX
//		final int PROD_REFID_VIRTUAL                               =  27;  // <RefId> ::= VIRTUAL
//		final int PROD_REFID_ABSOLUTE                              =  28;  // <RefId> ::= ABSOLUTE
//		final int PROD_REFID_MESSAGE                               =  29;  // <RefId> ::= MESSAGE
//		final int PROD_REFID_DEFAULT                               =  30;  // <RefId> ::= DEFAULT
//		final int PROD_REFID_OVERRIDE                              =  31;  // <RefId> ::= OVERRIDE
//		final int PROD_REFID_ABSTRACT                              =  32;  // <RefId> ::= ABSTRACT
//		final int PROD_REFID_DISPID                                =  33;  // <RefId> ::= DISPID
//		final int PROD_REFID_REINTRODUCE                           =  34;  // <RefId> ::= REINTRODUCE
//		final int PROD_REFID_REGISTER                              =  35;  // <RefId> ::= REGISTER
//		final int PROD_REFID_PASCAL                                =  36;  // <RefId> ::= PASCAL
//		final int PROD_REFID_CDECL                                 =  37;  // <RefId> ::= CDECL
//		final int PROD_REFID_STDCALL                               =  38;  // <RefId> ::= STDCALL
//		final int PROD_REFID_SAFECALL                              =  39;  // <RefId> ::= SAFECALL
//		final int PROD_REFID_STRING                                =  40;  // <RefId> ::= STRING
//		final int PROD_REFID_WIDESTRING                            =  41;  // <RefId> ::= WIDESTRING
//		final int PROD_REFID_ANSISTRING                            =  42;  // <RefId> ::= ANSISTRING
//		final int PROD_REFID_VARIANT                               =  43;  // <RefId> ::= VARIANT
//		final int PROD_REFID_OLEVARIANT                            =  44;  // <RefId> ::= OLEVARIANT
//		final int PROD_REFID_READONLY                              =  45;  // <RefId> ::= READONLY
//		final int PROD_REFID_IMPLEMENTS                            =  46;  // <RefId> ::= IMPLEMENTS
//		final int PROD_REFID_NODEFAULT                             =  47;  // <RefId> ::= NODEFAULT
//		final int PROD_REFID_STORED                                =  48;  // <RefId> ::= STORED
//		final int PROD_REFID_OVERLOAD                              =  49;  // <RefId> ::= OVERLOAD
//		final int PROD_REFID_LOCAL                                 =  50;  // <RefId> ::= LOCAL
//		final int PROD_REFID_VARARGS                               =  51;  // <RefId> ::= VARARGS
//		final int PROD_REFID_FORWARD                               =  52;  // <RefId> ::= FORWARD
//		final int PROD_REFID_CONTAINS                              =  53;  // <RefId> ::= CONTAINS
//		final int PROD_REFID_PACKAGE                               =  54;  // <RefId> ::= PACKAGE
//		final int PROD_REFID_REQUIRES                              =  55;  // <RefId> ::= REQUIRES
//		final int PROD_REFID_LIBRARY                               =  56;  // <RefId> ::= LIBRARY
//		final int PROD_REFID_IMPORT                                =  57;  // <RefId> ::= IMPORT
//		final int PROD_REFID_EXPORT                                =  58;  // <RefId> ::= EXPORT
//		final int PROD_REFID_PLATFORM                              =  59;  // <RefId> ::= PLATFORM
//		final int PROD_REFID_DEPRECATED                            =  60;  // <RefId> ::= DEPRECATED
//		final int PROD_REFID_EXTERNAL                              =  61;  // <RefId> ::= EXTERNAL
//		final int PROD_FIELDDESIGNATOR                             =  62;  // <FieldDesignator> ::= <RefId>
//		final int PROD_FIELDDESIGNATOR_DOT                         =  63;  // <FieldDesignator> ::= <FieldDesignator> '.' <RefId>
//		final int PROD_OBJECTPASCAL                                =  64;  // <ObjectPascal> ::= <Program>
//		final int PROD_OBJECTPASCAL2                               =  65;  // <ObjectPascal> ::= <Unit>
//		final int PROD_OBJECTPASCAL3                               =  66;  // <ObjectPascal> ::= <Package>
//		final int PROD_OBJECTPASCAL4                               =  67;  // <ObjectPascal> ::= <Library>
//		final int PROD_PROGRAM_DOT                                 =  68;  // <Program> ::= <ProgHeader> <OptUsesSection> <Block> '.'
//		final int PROD_PROGHEADER_PROGRAM_SEMI                     =  69;  // <ProgHeader> ::= PROGRAM <RefId> <OptProgParamList> ';'
//		final int PROD_OPTPROGPARAMLIST_LPAREN_RPAREN              =  70;  // <OptProgParamList> ::= '(' <IdList> ')'
//		final int PROD_OPTPROGPARAMLIST                            =  71;  // <OptProgParamList> ::= 
//		final int PROD_UNIT_DOT                                    =  72;  // <Unit> ::= <UnitHeader> <InterfaceSection> <ImplementationSection> <InitSection> '.'
//		final int PROD_UNITHEADER_UNIT_SEMI                        =  73;  // <UnitHeader> ::= UNIT <FieldDesignator> <OptPortDirectives> ';'
//		final int PROD_PACKAGE_END_DOT                             =  74;  // <Package> ::= <PackageHeader> <OptRequiresClause> <OptContainsClause> END '.'
		final int PROD_PACKAGEHEADER_PACKAGE_SEMI                  =  75;  // <PackageHeader> ::= PACKAGE <RefId> ';'
		final int PROD_OPTREQUIRESCLAUSE_REQUIRES_SEMI             =  76;  // <OptRequiresClause> ::= REQUIRES <IdList> ';'
//		final int PROD_OPTREQUIRESCLAUSE                           =  77;  // <OptRequiresClause> ::= 
		final int PROD_OPTCONTAINSCLAUSE_CONTAINS_SEMI             =  78;  // <OptContainsClause> ::= CONTAINS <IdList> ';'
//		final int PROD_OPTCONTAINSCLAUSE                           =  79;  // <OptContainsClause> ::= 
//		final int PROD_LIBRARYHEADER_LIBRARY_SEMI                  =  80;  // <LibraryHeader> ::= LIBRARY <RefId> ';'
//		final int PROD_LIBRARY_DOT                                 =  81;  // <Library> ::= <LibraryHeader> <OptUsesSection> <Block> '.'
//		final int PROD_INTERFACESECTION_INTERFACE                  =  82;  // <InterfaceSection> ::= INTERFACE <OptUsesSection> <OptExportDeclList>
//		final int PROD_OPTUSESSECTION                              =  83;  // <OptUsesSection> ::= <UsesSection>
//		final int PROD_OPTUSESSECTION2                             =  84;  // <OptUsesSection> ::= 
//		final int PROD_USESCLAUSE_USES_SEMI                        =  85;  // <UsesClause> ::= USES <UnitIdList> ';'
//		final int PROD_USESCLAUSE_SYNERROR                         =  86;  // <UsesClause> ::= SynError
//		final int PROD_USESSECTION                                 =  87;  // <UsesSection> ::= <UsesClause>
//		final int PROD_USESSECTION2                                =  88;  // <UsesSection> ::= <UsesSection> <UsesClause>
//		final int PROD_OPTEXPORTDECLLIST                           =  89;  // <OptExportDeclList> ::= <ExportDeclList>
//		final int PROD_OPTEXPORTDECLLIST2                          =  90;  // <OptExportDeclList> ::= 
		final int PROD_EXPORTDECLLIST                              =  91;  // <ExportDeclList> ::= <ExportDeclItem>
		final int PROD_EXPORTDECLLIST2                             =  92;  // <ExportDeclList> ::= <ExportDeclList> <ExportDeclItem>
		final int PROD_EXPORTDECLITEM                              =  93;  // <ExportDeclItem> ::= <ConstSection>
		final int PROD_EXPORTDECLITEM2                             =  94;  // <ExportDeclItem> ::= <TypeSection>
		final int PROD_EXPORTDECLITEM3                             =  95;  // <ExportDeclItem> ::= <VarSection>
		final int PROD_EXPORTDECLITEM4                             =  96;  // <ExportDeclItem> ::= <CallSection>
		final int PROD_EXPORTDECLITEM_FORWARD_SEMI                 =  97;  // <ExportDeclItem> ::= <CallSection> FORWARD ';'
		final int PROD_CALLSECTION                                 =  98;  // <CallSection> ::= <ProcHeading>
		final int PROD_CALLSECTION2                                =  99;  // <CallSection> ::= <FuncHeading>
		final int PROD_IMPLEMENTATIONSECTION_IMPLEMENTATION        = 100;  // <ImplementationSection> ::= IMPLEMENTATION <OptUsesSection> <OptDeclSection> <OptExportBlock>
		final int PROD_INITSECTION_INITIALIZATION_END              = 101;  // <InitSection> ::= INITIALIZATION <StmtList> END
		final int PROD_INITSECTION_INITIALIZATION_FINALIZATION_END = 102;  // <InitSection> ::= INITIALIZATION <StmtList> FINALIZATION <StmtList> END
		final int PROD_INITSECTION                                 = 103;  // <InitSection> ::= <CompoundStmt>
		final int PROD_INITSECTION_END                             = 104;  // <InitSection> ::= END
//		final int PROD_BLOCK                                       = 105;  // <Block> ::= <OptDeclSection> <OptExportBlock> <CompoundStmt> <OptExportBlock>
//		final int PROD_OPTEXPORTBLOCK                              = 106;  // <OptExportBlock> ::= <ExportStmt>
//		final int PROD_OPTEXPORTBLOCK2                             = 107;  // <OptExportBlock> ::= <OptExportBlock> <ExportStmt>
//		final int PROD_OPTEXPORTBLOCK3                             = 108;  // <OptExportBlock> ::= 
//		final int PROD_EXPORTSTMT_EXPORTS_SEMI                     = 109;  // <ExportStmt> ::= EXPORTS <ExportList> ';'
//		final int PROD_EXPORTLIST                                  = 110;  // <ExportList> ::= <ExportItem>
//		final int PROD_EXPORTLIST_COMMA                            = 111;  // <ExportList> ::= <ExportList> ',' <ExportItem>
//		final int PROD_EXPORTITEM_ID                               = 112;  // <ExportItem> ::= id
//		final int PROD_EXPORTITEM_ID_NAME_APOST_APOST              = 113;  // <ExportItem> ::= id NAME '' <ConstExpr> ''
//		final int PROD_EXPORTITEM_ID_INDEX_APOST_APOST             = 114;  // <ExportItem> ::= id INDEX '' <ConstExpr> ''
//		final int PROD_EXPORTITEM_NAME_APOST_APOST                 = 115;  // <ExportItem> ::= NAME '' <ConstExpr> ''
//		final int PROD_EXPORTITEM_INDEX_APOST_APOST                = 116;  // <ExportItem> ::= INDEX '' <ConstExpr> ''
//		final int PROD_OPTDECLSECTION                              = 117;  // <OptDeclSection> ::= <DeclSection>
//		final int PROD_OPTDECLSECTION2                             = 118;  // <OptDeclSection> ::= 
//		final int PROD_DECLSECTION                                 = 119;  // <DeclSection> ::= <DeclItem>
//		final int PROD_DECLSECTION2                                = 120;  // <DeclSection> ::= <DeclSection> <DeclItem>
//		final int PROD_DECLITEM                                    = 121;  // <DeclItem> ::= <LabelSection>
//		final int PROD_DECLITEM2                                   = 122;  // <DeclItem> ::= <ConstSection>
//		final int PROD_DECLITEM3                                   = 123;  // <DeclItem> ::= <TypeSection>
//		final int PROD_DECLITEM4                                   = 124;  // <DeclItem> ::= <VarSection>
//		final int PROD_DECLITEM5                                   = 125;  // <DeclItem> ::= <ProcedureDeclSection>
//		final int PROD_DECLITEM_SYNERROR                           = 126;  // <DeclItem> ::= SynError
//		final int PROD_LABELSECTION_LABEL_SEMI                     = 127;  // <LabelSection> ::= LABEL <LabelList> ';'
//		final int PROD_LABELLIST                                   = 128;  // <LabelList> ::= <LabelId>
//		final int PROD_LABELLIST_COMMA                             = 129;  // <LabelList> ::= <LabelList> ',' <LabelId>
//		final int PROD_CONSTSECTION_CONST                          = 130;  // <ConstSection> ::= CONST <ConstantDeclList>
//		final int PROD_CONSTSECTION_RESOURCESTRING                 = 131;  // <ConstSection> ::= RESOURCESTRING <ConstantDeclList>
//		final int PROD_CONSTANTDECLLIST                            = 132;  // <ConstantDeclList> ::= <ConstantDecl>
//		final int PROD_CONSTANTDECLLIST2                           = 133;  // <ConstantDeclList> ::= <ConstantDeclList> <ConstantDecl>
		final int PROD_CONSTANTDECL_EQ_SEMI                        = 134;  // <ConstantDecl> ::= <RefId> '=' <ConstExpr> <OptPortDirectives> ';'
		final int PROD_CONSTANTDECL_COLON_EQ_SEMI                  = 135;  // <ConstantDecl> ::= <RefId> ':' <Type> '=' <TypedConstant> <OptPortDirectives> ';'
//		final int PROD_CONSTANTDECL_SYNERROR_SEMI                  = 136;  // <ConstantDecl> ::= SynError ';'
//		final int PROD_TYPEDCONSTANT                               = 137;  // <TypedConstant> ::= <ConstExpr>
//		final int PROD_TYPEDCONSTANT2                              = 138;  // <TypedConstant> ::= <ArrayConstant>
//		final int PROD_TYPEDCONSTANT3                              = 139;  // <TypedConstant> ::= <RecordConstant>
		final int PROD_ARRAYCONSTANT_LPAREN_RPAREN                 = 140;  // <ArrayConstant> ::= '(' <TypedConstList> ')'
		final int PROD_RECORDCONSTANT_LPAREN_RPAREN                = 141;  // <RecordConstant> ::= '(' <RecordFieldConstList> ')'
		final int PROD_RECORDCONSTANT_LPAREN_SEMI_RPAREN           = 142;  // <RecordConstant> ::= '(' <RecordFieldConstList> ';' ')'
//		final int PROD_RECORDCONSTANT_LPAREN_RPAREN2               = 143;  // <RecordConstant> ::= '(' ')'
//		final int PROD_RECORDFIELDCONSTLIST                        = 144;  // <RecordFieldConstList> ::= <RecordFieldConstant>
//		final int PROD_RECORDFIELDCONSTLIST_SEMI                   = 145;  // <RecordFieldConstList> ::= <RecordFieldConstList> ';' <RecordFieldConstant>
//		final int PROD_RECORDFIELDCONSTANT_COLON                   = 146;  // <RecordFieldConstant> ::= <RefId> ':' <TypedConstant>
//		final int PROD_TYPEDCONSTLIST                              = 147;  // <TypedConstList> ::= <TypedConstant>
//		final int PROD_TYPEDCONSTLIST_COMMA                        = 148;  // <TypedConstList> ::= <TypedConstList> ',' <TypedConstant>
//		final int PROD_TYPESECTION_TYPE                            = 149;  // <TypeSection> ::= TYPE <TypeDeclList>
//		final int PROD_TYPEDECLLIST                                = 150;  // <TypeDeclList> ::= <TypeDecl>
//		final int PROD_TYPEDECLLIST2                               = 151;  // <TypeDeclList> ::= <TypeDeclList> <TypeDecl>
		final int PROD_TYPEDECL_EQ                                 = 152;  // <TypeDecl> ::= <TypeId> '=' <TypeSpec>
//		final int PROD_TYPEDECL_SYNERROR_SEMI                      = 153;  // <TypeDecl> ::= SynError ';'
		final int PROD_TYPESPEC_SEMI                               = 154;  // <TypeSpec> ::= <GenericType> ';'
		final int PROD_TYPESPEC_SEMI2                              = 155;  // <TypeSpec> ::= <RestrictedType> <OptPortDirectives> ';'
//		final int PROD_TYPESPEC_SEMI3                              = 156;  // <TypeSpec> ::= <CallType> ';'
//		final int PROD_TYPESPEC_SEMI_SEMI                          = 157;  // <TypeSpec> ::= <CallType> ';' <CallConventions> ';'
//		final int PROD_TYPESPEC_SYNERROR_SEMI                      = 158;  // <TypeSpec> ::= SynError ';'
//		final int PROD_TYPE                                        = 159;  // <Type> ::= <GenericType>
//		final int PROD_TYPE2                                       = 160;  // <Type> ::= <CallType>
//		final int PROD_TYPEREF                                     = 161;  // <TypeRef> ::= <TypeName>
//		final int PROD_TYPEREF2                                    = 162;  // <TypeRef> ::= <StringType>
//		final int PROD_TYPEREF3                                    = 163;  // <TypeRef> ::= <VariantType>
//		final int PROD_GENERICTYPE                                 = 164;  // <GenericType> ::= <TypeName>
//		final int PROD_GENERICTYPE2                                = 165;  // <GenericType> ::= <StringType>
//		final int PROD_GENERICTYPE3                                = 166;  // <GenericType> ::= <VariantType>
//		final int PROD_GENERICTYPE4                                = 167;  // <GenericType> ::= <SubrangeType>
//		final int PROD_GENERICTYPE5                                = 168;  // <GenericType> ::= <EnumType>
//		final int PROD_GENERICTYPE6                                = 169;  // <GenericType> ::= <StructType>
//		final int PROD_GENERICTYPE7                                = 170;  // <GenericType> ::= <PointerType>
//		final int PROD_GENERICTYPE8                                = 171;  // <GenericType> ::= <ClassRefType>
//		final int PROD_GENERICTYPE9                                = 172;  // <GenericType> ::= <ClonedType>
//		final int PROD_CLONEDTYPE_TYPE                             = 173;  // <ClonedType> ::= TYPE <TypeRef>
//		final int PROD_STRINGTYPE_STRING                           = 174;  // <StringType> ::= STRING
//		final int PROD_STRINGTYPE_ANSISTRING                       = 175;  // <StringType> ::= ANSISTRING
//		final int PROD_STRINGTYPE_WIDESTRING                       = 176;  // <StringType> ::= WIDESTRING
//		final int PROD_STRINGTYPE_STRING_LBRACKET_RBRACKET         = 177;  // <StringType> ::= STRING '[' <ConstExpr> ']'
//		final int PROD_VARIANTTYPE_VARIANT                         = 178;  // <VariantType> ::= VARIANT
//		final int PROD_VARIANTTYPE_OLEVARIANT                      = 179;  // <VariantType> ::= OLEVARIANT
//		final int PROD_ORDINALTYPE                                 = 180;  // <OrdinalType> ::= <SubrangeType>
//		final int PROD_ORDINALTYPE2                                = 181;  // <OrdinalType> ::= <EnumType>
//		final int PROD_ORDINALTYPE3                                = 182;  // <OrdinalType> ::= <TypeName>
//		final int PROD_SUBRANGETYPE_DOTDOT                         = 183;  // <SubrangeType> ::= <ConstOrdExpr> '..' <ConstOrdExpr>
		final int PROD_ENUMTYPE_LPAREN_COMMA_RPAREN                = 184;  // <EnumType> ::= '(' <EnumId> ',' <EnumList> ')'
//		final int PROD_ENUMLIST                                    = 185;  // <EnumList> ::= <EnumId>
		final int PROD_ENUMLIST_COMMA                              = 186;  // <EnumList> ::= <EnumList> ',' <EnumId>
//		final int PROD_ENUMID                                      = 187;  // <EnumId> ::= <RefId>
		final int PROD_ENUMID_EQ                                   = 188;  // <EnumId> ::= <RefId> '=' <ConstExpr>
//		final int PROD_OPTPACKED_PACKED                            = 189;  // <OptPacked> ::= PACKED
//		final int PROD_OPTPACKED                                   = 190;  // <OptPacked> ::= 
//		final int PROD_STRUCTTYPE                                  = 191;  // <StructType> ::= <ArrayType>
//		final int PROD_STRUCTTYPE2                                 = 192;  // <StructType> ::= <SetType>
//		final int PROD_STRUCTTYPE3                                 = 193;  // <StructType> ::= <FileType>
//		final int PROD_STRUCTTYPE4                                 = 194;  // <StructType> ::= <RecType>
		final int PROD_ARRAYTYPE_ARRAY_LBRACKET_RBRACKET_OF        = 195;  // <ArrayType> ::= <OptPacked> ARRAY '[' <OrdinalTypeList> ']' OF <Type>
		final int PROD_ARRAYTYPE_ARRAY_OF_CONST                    = 196;  // <ArrayType> ::= <OptPacked> ARRAY OF CONST
		final int PROD_ARRAYTYPE_ARRAY_OF                          = 197;  // <ArrayType> ::= <OptPacked> ARRAY OF <Type>
//		final int PROD_ORDINALTYPELIST                             = 198;  // <OrdinalTypeList> ::= <OrdinalType>
//		final int PROD_ORDINALTYPELIST_COMMA                       = 199;  // <OrdinalTypeList> ::= <OrdinalTypeList> ',' <OrdinalType>
		final int PROD_RECTYPE_RECORD_END                          = 200;  // <RecType> ::= <OptPacked> RECORD <RecFieldList> END <OptPortDirectives>
//		final int PROD_RECFIELDLIST                                = 201;  // <RecFieldList> ::= 
//		final int PROD_RECFIELDLIST2                               = 202;  // <RecFieldList> ::= <RecField1>
//		final int PROD_RECFIELDLIST3                               = 203;  // <RecFieldList> ::= <RecField2>
//		final int PROD_RECFIELDLIST_SEMI                           = 204;  // <RecFieldList> ::= <RecField1> ';' <RecFieldList>
//		final int PROD_RECFIELDLIST_SEMI2                          = 205;  // <RecFieldList> ::= <RecField2> ';' <RecFieldList>
//		final int PROD_RECFIELDLIST_SEMI3                          = 206;  // <RecFieldList> ::= <RecField2> ';' <CallConvention>
//		final int PROD_RECFIELDLIST_SEMI_SEMI                      = 207;  // <RecFieldList> ::= <RecField2> ';' <CallConvention> ';' <RecFieldList>
//		final int PROD_RECFIELDLIST_CASE_OF                        = 208;  // <RecFieldList> ::= CASE <Selector> OF <RecVariantList>
//		final int PROD_RECVARIANTLIST                              = 209;  // <RecVariantList> ::= 
//		final int PROD_RECVARIANTLIST2                             = 210;  // <RecVariantList> ::= <RecVariant>
//		final int PROD_RECVARIANTLIST_SEMI                         = 211;  // <RecVariantList> ::= <RecVariant> ';' <RecVariantList>
//		final int PROD_RECFIELD1_COLON                             = 212;  // <RecField1> ::= <IdList> ':' <GenericType> <OptPortDirectives>
//		final int PROD_RECFIELD2_COLON                             = 213;  // <RecField2> ::= <IdList> ':' <CallType>
//		final int PROD_RECVARIANT_COLON_LPAREN_RPAREN              = 214;  // <RecVariant> ::= <ConstExprList> ':' '(' <RecFieldList> ')'
		final int PROD_SELECTOR_COLON                              = 215;  // <Selector> ::= <RefId> ':' <TypeName>
//		final int PROD_SELECTOR                                    = 216;  // <Selector> ::= <TypeName>
//		final int PROD_SETTYPE_SET_OF                              = 217;  // <SetType> ::= <OptPacked> SET OF <OrdinalType>
//		final int PROD_FILETYPE_FILE_OF                            = 218;  // <FileType> ::= <OptPacked> FILE OF <TypeRef>
//		final int PROD_FILETYPE_FILE                               = 219;  // <FileType> ::= FILE
//		final int PROD_POINTERTYPE_CARET                           = 220;  // <PointerType> ::= '^' <TypeRef>
//		final int PROD_CALLTYPE_PROCEDURE                          = 221;  // <CallType> ::= PROCEDURE <OptFormalParms> <OptCallConventions>
//		final int PROD_CALLTYPE_PROCEDURE_OF_OBJECT                = 222;  // <CallType> ::= PROCEDURE <OptFormalParms> OF OBJECT <OptCallConventions>
//		final int PROD_CALLTYPE_FUNCTION_COLON                     = 223;  // <CallType> ::= FUNCTION <OptFormalParms> ':' <ResultType> <OptCallConventions>
//		final int PROD_CALLTYPE_FUNCTION_COLON_OF_OBJECT           = 224;  // <CallType> ::= FUNCTION <OptFormalParms> ':' <ResultType> OF OBJECT <OptCallConventions>
//		final int PROD_RESTRICTEDTYPE                              = 225;  // <RestrictedType> ::= <ObjectType>
//		final int PROD_RESTRICTEDTYPE2                             = 226;  // <RestrictedType> ::= <ClassType>
//		final int PROD_RESTRICTEDTYPE3                             = 227;  // <RestrictedType> ::= <InterfaceType>
//		final int PROD_OBJECTTYPE_OBJECT_END                       = 228;  // <ObjectType> ::= <OptPacked> OBJECT <OptObjectHeritage> <ObjectMemberList> END
		final int PROD_CLASSTYPE_CLASS_END                         = 229;  // <ClassType> ::= CLASS <OptClassHeritage> <ClassMemberList> END
//		final int PROD_CLASSTYPE_CLASS                             = 230;  // <ClassType> ::= CLASS <OptClassHeritage>
//		final int PROD_CLASSREFTYPE_CLASS_OF                       = 231;  // <ClassRefType> ::= CLASS OF <TypeName>
//		final int PROD_INTERFACETYPE_INTERFACE_END                 = 232;  // <InterfaceType> ::= INTERFACE <OptClassHeritage> <OptClassGUID> <OptClassMethodList> END
//		final int PROD_INTERFACETYPE_DISPINTERFACE_END             = 233;  // <InterfaceType> ::= DISPINTERFACE <OptClassHeritage> <OptClassGUID> <OptClassMethodList> END
//		final int PROD_INTERFACETYPE_INTERFACE                     = 234;  // <InterfaceType> ::= INTERFACE
//		final int PROD_INTERFACETYPE_DISPINTERFACE                 = 235;  // <InterfaceType> ::= DISPINTERFACE
//		final int PROD_OPTOBJECTHERITAGE_LPAREN_RPAREN             = 236;  // <OptObjectHeritage> ::= '(' <TypeName> ')'
//		final int PROD_OPTOBJECTHERITAGE                           = 237;  // <OptObjectHeritage> ::= 
//		final int PROD_OPTCLASSHERITAGE_LPAREN_RPAREN              = 238;  // <OptClassHeritage> ::= '(' <TypeList> ')'
//		final int PROD_OPTCLASSHERITAGE                            = 239;  // <OptClassHeritage> ::= 
//		final int PROD_OPTCLASSGUID_LBRACKET_RBRACKET              = 240;  // <OptClassGUID> ::= '[' <ConstStrExpr> ']'
//		final int PROD_OPTCLASSGUID                                = 241;  // <OptClassGUID> ::= 
//		final int PROD_OBJECTMEMBERLIST                            = 242;  // <ObjectMemberList> ::= <OptFieldList> <OptObjectMethodList>
//		final int PROD_OBJECTMEMBERLIST_PUBLIC                     = 243;  // <ObjectMemberList> ::= <ObjectMemberList> PUBLIC <OptFieldList> <OptObjectMethodList>
//		final int PROD_OBJECTMEMBERLIST_PRIVATE                    = 244;  // <ObjectMemberList> ::= <ObjectMemberList> PRIVATE <OptFieldList> <OptObjectMethodList>
//		final int PROD_OBJECTMEMBERLIST_PROTECTED                  = 245;  // <ObjectMemberList> ::= <ObjectMemberList> PROTECTED <OptFieldList> <OptObjectMethodList>
		final int PROD_CLASSMEMBERLIST                             = 246;  // <ClassMemberList> ::= <OptFieldList> <OptClassMethodList>
//		final int PROD_CLASSMEMBERLIST_PUBLIC                      = 247;  // <ClassMemberList> ::= <ClassMemberList> PUBLIC <OptFieldList> <OptClassMethodList>
//		final int PROD_CLASSMEMBERLIST_PRIVATE                     = 248;  // <ClassMemberList> ::= <ClassMemberList> PRIVATE <OptFieldList> <OptClassMethodList>
//		final int PROD_CLASSMEMBERLIST_PROTECTED                   = 249;  // <ClassMemberList> ::= <ClassMemberList> PROTECTED <OptFieldList> <OptClassMethodList>
//		final int PROD_CLASSMEMBERLIST_PUBLISHED                   = 250;  // <ClassMemberList> ::= <ClassMemberList> PUBLISHED <OptFieldList> <OptClassMethodList>
//		final int PROD_OPTFIELDLIST                                = 251;  // <OptFieldList> ::= <FieldList>
//		final int PROD_OPTFIELDLIST2                               = 252;  // <OptFieldList> ::= 
//		final int PROD_OPTOBJECTMETHODLIST                         = 253;  // <OptObjectMethodList> ::= <ObjectMethodList>
//		final int PROD_OPTOBJECTMETHODLIST2                        = 254;  // <OptObjectMethodList> ::= 
//		final int PROD_OPTCLASSMETHODLIST                          = 255;  // <OptClassMethodList> ::= <ClassMethodList>
//		final int PROD_OPTCLASSMETHODLIST2                         = 256;  // <OptClassMethodList> ::= 
//		final int PROD_FIELDLIST                                   = 257;  // <FieldList> ::= <FieldSpec>
		final int PROD_FIELDLIST2                                  = 258;  // <FieldList> ::= <FieldList> <FieldSpec>
//		final int PROD_OBJECTMETHODLIST                            = 259;  // <ObjectMethodList> ::= <ObjectMethodSpec>
//		final int PROD_OBJECTMETHODLIST2                           = 260;  // <ObjectMethodList> ::= <ObjectMethodList> <ObjectMethodSpec>
//		final int PROD_CLASSMETHODLIST                             = 261;  // <ClassMethodList> ::= <ClassMethodSpec>
		final int PROD_CLASSMETHODLIST2                            = 262;  // <ClassMethodList> ::= <ClassMethodList> <ClassMethodSpec>
		final int PROD_FIELDSPEC_COLON_SEMI                        = 263;  // <FieldSpec> ::= <IdList> ':' <Type> <OptPortDirectives> ';'
//		final int PROD_FIELDSPEC_SYNERROR_SEMI                     = 264;  // <FieldSpec> ::= SynError ';'
//		final int PROD_OBJECTMETHODSPEC                            = 265;  // <ObjectMethodSpec> ::= <MethodSpec> <OptMethodDirectives>
//		final int PROD_OBJECTMETHODSPEC2                           = 266;  // <ObjectMethodSpec> ::= <PropertySpec> <OptPropertyDirectives>
//		final int PROD_OBJECTMETHODSPEC_SYNERROR                   = 267;  // <ObjectMethodSpec> ::= SynError
		final int PROD_CLASSMETHODSPEC                             = 268;  // <ClassMethodSpec> ::= <MethodSpec> <OptMethodDirectives>
		final int PROD_CLASSMETHODSPEC2                            = 269;  // <ClassMethodSpec> ::= <ResolutionSpec> <OptMethodDirectives>
		final int PROD_CLASSMETHODSPEC_CLASS                       = 270;  // <ClassMethodSpec> ::= CLASS <ProcSpec> <OptMethodDirectives>
		final int PROD_CLASSMETHODSPEC_CLASS2                      = 271;  // <ClassMethodSpec> ::= CLASS <FuncSpec> <OptMethodDirectives>
		final int PROD_CLASSMETHODSPEC3                            = 272;  // <ClassMethodSpec> ::= <PropertySpec> <OptPropertyDirectives>
//		final int PROD_CLASSMETHODSPEC_SYNERROR                    = 273;  // <ClassMethodSpec> ::= SynError
//		final int PROD_METHODSPEC                                  = 274;  // <MethodSpec> ::= <ConstructorSpec>
//		final int PROD_METHODSPEC2                                 = 275;  // <MethodSpec> ::= <DestructorSpec>
//		final int PROD_METHODSPEC3                                 = 276;  // <MethodSpec> ::= <ProcSpec>
//		final int PROD_METHODSPEC4                                 = 277;  // <MethodSpec> ::= <FuncSpec>
		final int PROD_CONSTRUCTORSPEC_CONSTRUCTOR_SEMI            = 278;  // <ConstructorSpec> ::= CONSTRUCTOR <RefId> <OptFormalParms> ';'
		final int PROD_DESTRUCTORSPEC_DESTRUCTOR_SEMI              = 279;  // <DestructorSpec> ::= DESTRUCTOR <RefId> <OptFormalParms> ';'
		final int PROD_PROCSPEC_PROCEDURE_SEMI                     = 280;  // <ProcSpec> ::= PROCEDURE <RefId> <OptFormalParms> <OptCallConventions> ';'
		final int PROD_FUNCSPEC_FUNCTION_COLON_SEMI                = 281;  // <FuncSpec> ::= FUNCTION <RefId> <OptFormalParms> ':' <ResultType> <OptCallConventions> ';'
//		final int PROD_RESOLUTIONSPEC_PROCEDURE_DOT_EQ_SEMI        = 282;  // <ResolutionSpec> ::= PROCEDURE <RefId> '.' <RefId> '=' <RefId> ';'
//		final int PROD_RESOLUTIONSPEC_FUNCTION_DOT_EQ_SEMI         = 283;  // <ResolutionSpec> ::= FUNCTION <RefId> '.' <RefId> '=' <RefId> ';'
//		final int PROD_PROPERTYSPEC_PROPERTY_SEMI                  = 284;  // <PropertySpec> ::= PROPERTY <PropertyDecl> <OptPropSpecifiers> ';'
//		final int PROD_PROPERTYDECL_COLON                          = 285;  // <PropertyDecl> ::= <RefId> ':' <TypeRef>
//		final int PROD_PROPERTYDECL_LBRACKET_RBRACKET_COLON        = 286;  // <PropertyDecl> ::= <RefId> '[' <IndexList> ']' ':' <TypeRef>
//		final int PROD_PROPERTYDECL                                = 287;  // <PropertyDecl> ::= <RefId>
//		final int PROD_INDEXLIST                                   = 288;  // <IndexList> ::= <IndexDecl>
//		final int PROD_INDEXLIST_SEMI                              = 289;  // <IndexList> ::= <IndexList> ';' <IndexDecl>
//		final int PROD_INDEXDECL                                   = 290;  // <IndexDecl> ::= <IdDecl>
//		final int PROD_INDEXDECL_CONST                             = 291;  // <IndexDecl> ::= CONST <IdDecl>
//		final int PROD_IDDECL_COLON                                = 292;  // <IdDecl> ::= <IdList> ':' <Type>
//		final int PROD_OPTPROPSPECIFIERS                           = 293;  // <OptPropSpecifiers> ::= <PropertySpecifiers>
//		final int PROD_OPTPROPSPECIFIERS2                          = 294;  // <OptPropSpecifiers> ::= 
//		final int PROD_PROPERTYSPECIFIERS                          = 295;  // <PropertySpecifiers> ::= <PropertySpecifier>
//		final int PROD_PROPERTYSPECIFIERS2                         = 296;  // <PropertySpecifiers> ::= <PropertySpecifiers> <PropertySpecifier>
//		final int PROD_PROPERTYSPECIFIER_INDEX                     = 297;  // <PropertySpecifier> ::= INDEX <ConstExpr>
//		final int PROD_PROPERTYSPECIFIER_READ                      = 298;  // <PropertySpecifier> ::= READ <FieldDesignator>
//		final int PROD_PROPERTYSPECIFIER_WRITE                     = 299;  // <PropertySpecifier> ::= WRITE <FieldDesignator>
//		final int PROD_PROPERTYSPECIFIER_STORED                    = 300;  // <PropertySpecifier> ::= STORED <FieldDesignator>
//		final int PROD_PROPERTYSPECIFIER_DEFAULT                   = 301;  // <PropertySpecifier> ::= DEFAULT <ConstExpr>
//		final int PROD_PROPERTYSPECIFIER_NODEFAULT                 = 302;  // <PropertySpecifier> ::= NODEFAULT
//		final int PROD_PROPERTYSPECIFIER_WRITEONLY                 = 303;  // <PropertySpecifier> ::= WRITEONLY
//		final int PROD_PROPERTYSPECIFIER_READONLY                  = 304;  // <PropertySpecifier> ::= READONLY
//		final int PROD_PROPERTYSPECIFIER_DISPID                    = 305;  // <PropertySpecifier> ::= DISPID <ConstExpr>
//		final int PROD_PROPERTYSPECIFIER                           = 306;  // <PropertySpecifier> ::= <ImplementsSpecifier>
//		final int PROD_IMPLEMENTSSPECIFIER_IMPLEMENTS              = 307;  // <ImplementsSpecifier> ::= IMPLEMENTS <TypeRef>
//		final int PROD_IMPLEMENTSSPECIFIER_COMMA                   = 308;  // <ImplementsSpecifier> ::= <ImplementsSpecifier> ',' <TypeRef>
//		final int PROD_VARSECTION_VAR                              = 309;  // <VarSection> ::= VAR <VarDeclList>
//		final int PROD_VARSECTION_THREADVAR                        = 310;  // <VarSection> ::= THREADVAR <ThreadVarDeclList>
//		final int PROD_VARDECLLIST                                 = 311;  // <VarDeclList> ::= <VarDecl>
//		final int PROD_VARDECLLIST2                                = 312;  // <VarDeclList> ::= <VarDeclList> <VarDecl>
		final int PROD_VARDECL_COLON_SEMI                          = 313;  // <VarDecl> ::= <IdList> ':' <Type> <OptAbsoluteClause> <OptPortDirectives> ';'
		final int PROD_VARDECL_COLON_EQ_SEMI                       = 314;  // <VarDecl> ::= <IdList> ':' <Type> '=' <TypedConstant> <OptPortDirectives> ';'
		final int PROD_VARDECL_COLON                               = 315;  // <VarDecl> ::= <IdList> ':' <TypeSpec>
		final int PROD_VARDECL_SYNERROR_SEMI                       = 316;  // <VarDecl> ::= SynError ';'
//		final int PROD_THREADVARDECLLIST                           = 317;  // <ThreadVarDeclList> ::= <ThreadVarDecl>
//		final int PROD_THREADVARDECLLIST2                          = 318;  // <ThreadVarDeclList> ::= <ThreadVarDeclList> <ThreadVarDecl>
//		final int PROD_THREADVARDECL_COLON                         = 319;  // <ThreadVarDecl> ::= <IdList> ':' <TypeSpec>
//		final int PROD_THREADVARDECL_SYNERROR_SEMI                 = 320;  // <ThreadVarDecl> ::= SynError ';'
//		final int PROD_OPTABSOLUTECLAUSE_ABSOLUTE                  = 321;  // <OptAbsoluteClause> ::= ABSOLUTE <RefId>
//		final int PROD_OPTABSOLUTECLAUSE                           = 322;  // <OptAbsoluteClause> ::= 
//		final int PROD_CONSTEXPR                                   = 323;  // <ConstExpr> ::= <Expr>
//		final int PROD_CONSTORDEXPR                                = 324;  // <ConstOrdExpr> ::= <AddExpr>
//		final int PROD_CONSTSTREXPR                                = 325;  // <ConstStrExpr> ::= <AddExpr>
//		final int PROD_EXPR                                        = 326;  // <Expr> ::= <AddExpr>
//		final int PROD_EXPR2                                       = 327;  // <Expr> ::= <AddExpr> <RelOp> <AddExpr>
//		final int PROD_EXPR_SYNERROR                               = 328;  // <Expr> ::= SynError
//		final int PROD_ADDEXPR                                     = 329;  // <AddExpr> ::= <MulExpr>
//		final int PROD_ADDEXPR2                                    = 330;  // <AddExpr> ::= <AddExpr> <AddOp> <MulExpr>
//		final int PROD_MULEXPR                                     = 331;  // <MulExpr> ::= <Factor>
//		final int PROD_MULEXPR2                                    = 332;  // <MulExpr> ::= <MulExpr> <MulOp> <Factor>
//		final int PROD_FACTOR_NIL                                  = 333;  // <Factor> ::= NIL
//		final int PROD_FACTOR                                      = 334;  // <Factor> ::= <ICONST>
//		final int PROD_FACTOR2                                     = 335;  // <Factor> ::= <RCONST>
//		final int PROD_FACTOR3                                     = 336;  // <Factor> ::= <SCONST>
//		final int PROD_FACTOR4                                     = 337;  // <Factor> ::= <Designator>
//		final int PROD_FACTOR5                                     = 338;  // <Factor> ::= <SetConstructor>
//		final int PROD_FACTOR_AT                                   = 339;  // <Factor> ::= '@' <Designator>
//		final int PROD_FACTOR_AT_AT                                = 340;  // <Factor> ::= '@' '@' <Designator>
//		final int PROD_FACTOR_LPAREN_RPAREN                        = 341;  // <Factor> ::= '(' <Expr> ')'
//		final int PROD_FACTOR_LPAREN_RPAREN_CARET                  = 342;  // <Factor> ::= '(' <Expr> ')' '^'
//		final int PROD_FACTOR_PLUS                                 = 343;  // <Factor> ::= '+' <Factor>
//		final int PROD_FACTOR_MINUS                                = 344;  // <Factor> ::= '-' <Factor>
//		final int PROD_FACTOR_NOT                                  = 345;  // <Factor> ::= NOT <Factor>
//		final int PROD_DESIGNATOR                                  = 346;  // <Designator> ::= <FieldDesignator>
//		final int PROD_DESIGNATOR_DOT                              = 347;  // <Designator> ::= <Designator> '.' <FieldDesignator>
//		final int PROD_DESIGNATOR_CARET                            = 348;  // <Designator> ::= <Designator> '^'
//		final int PROD_DESIGNATOR_LBRACKET_RBRACKET                = 349;  // <Designator> ::= <Designator> '[' <ExprList> ']'
//		final int PROD_DESIGNATOR_LPAREN_RPAREN                    = 350;  // <Designator> ::= <Designator> '(' <ExprList> ')'
//		final int PROD_DESIGNATOR_LPAREN_RPAREN2                   = 351;  // <Designator> ::= <Designator> '(' ')'
//		final int PROD_DESIGNATOR_AS                               = 352;  // <Designator> ::= <Designator> AS <TypeRef>
//		final int PROD_DESIGNATOR_LPAREN_RPAREN3                   = 353;  // <Designator> ::= '(' <Designator> ')'
//		final int PROD_DESIGNATOR_INHERITED                        = 354;  // <Designator> ::= INHERITED <Designator>
//		final int PROD_ASNOP_COLONEQ                               = 355;  // <AsnOp> ::= ':='
//		final int PROD_ASNOP_PLUSEQ                                = 356;  // <AsnOp> ::= '+='
//		final int PROD_ASNOP_MINUSEQ                               = 357;  // <AsnOp> ::= '-='
//		final int PROD_ASNOP_TIMESEQ                               = 358;  // <AsnOp> ::= '*='
//		final int PROD_ASNOP_DIVEQ                                 = 359;  // <AsnOp> ::= '/='
//		final int PROD_RELOP_EQ                                    = 360;  // <RelOp> ::= '='
//		final int PROD_RELOP_GT                                    = 361;  // <RelOp> ::= '>'
//		final int PROD_RELOP_LT                                    = 362;  // <RelOp> ::= '<'
//		final int PROD_RELOP_LTEQ                                  = 363;  // <RelOp> ::= '<='
//		final int PROD_RELOP_GTEQ                                  = 364;  // <RelOp> ::= '>='
//		final int PROD_RELOP_LTGT                                  = 365;  // <RelOp> ::= '<>'
//		final int PROD_RELOP_IN                                    = 366;  // <RelOp> ::= IN
//		final int PROD_RELOP_IS                                    = 367;  // <RelOp> ::= IS
//		final int PROD_RELOP_AS                                    = 368;  // <RelOp> ::= AS
//		final int PROD_ADDOP_PLUS                                  = 369;  // <AddOp> ::= '+'
//		final int PROD_ADDOP_MINUS                                 = 370;  // <AddOp> ::= '-'
//		final int PROD_ADDOP_OR                                    = 371;  // <AddOp> ::= OR
//		final int PROD_ADDOP_XOR                                   = 372;  // <AddOp> ::= XOR
//		final int PROD_MULOP_TIMES                                 = 373;  // <MulOp> ::= '*'
//		final int PROD_MULOP_DIV                                   = 374;  // <MulOp> ::= '/'
//		final int PROD_MULOP_DIV2                                  = 375;  // <MulOp> ::= DIV
//		final int PROD_MULOP_MOD                                   = 376;  // <MulOp> ::= MOD
//		final int PROD_MULOP_AND                                   = 377;  // <MulOp> ::= AND
//		final int PROD_MULOP_SHL                                   = 378;  // <MulOp> ::= SHL
//		final int PROD_MULOP_SHR                                   = 379;  // <MulOp> ::= SHR
//		final int PROD_SETCONSTRUCTOR_LBRACKET_RBRACKET            = 380;  // <SetConstructor> ::= '[' <SetElementList> ']'
//		final int PROD_SETCONSTRUCTOR_LBRACKET_RBRACKET2           = 381;  // <SetConstructor> ::= '[' ']'
//		final int PROD_SETELEMENTLIST                              = 382;  // <SetElementList> ::= <SetElement>
//		final int PROD_SETELEMENTLIST_COMMA                        = 383;  // <SetElementList> ::= <SetElementList> ',' <SetElement>
//		final int PROD_SETELEMENT                                  = 384;  // <SetElement> ::= <Expr>
//		final int PROD_SETELEMENT_DOTDOT                           = 385;  // <SetElement> ::= <Expr> '..' <Expr>
//		final int PROD_EXPRLIST                                    = 386;  // <ExprList> ::= <Expr>
//		final int PROD_EXPRLIST_COMMA                              = 387;  // <ExprList> ::= <ExprList> ',' <Expr>
//		final int PROD_FMTEXPR                                     = 388;  // <FmtExpr> ::= <Expr>
//		final int PROD_FMTEXPR_COLON                               = 389;  // <FmtExpr> ::= <Expr> ':' <Expr>
//		final int PROD_FMTEXPR_COLON_COLON                         = 390;  // <FmtExpr> ::= <Expr> ':' <Expr> ':' <Expr>
//		final int PROD_FMTEXPRLIST                                 = 391;  // <FmtExprList> ::= <FmtExpr>
//		final int PROD_FMTEXPRLIST_COMMA                           = 392;  // <FmtExprList> ::= <FmtExprList> ',' <FmtExpr>
//		final int PROD_CONSTEXPRLIST                               = 393;  // <ConstExprList> ::= <ConstExpr>
//		final int PROD_CONSTEXPRLIST_COMMA                         = 394;  // <ConstExprList> ::= <ConstExprList> ',' <ConstExpr>
//		final int PROD_STMTLIST                                    = 395;  // <StmtList> ::= <Statement>
//		final int PROD_STMTLIST_SEMI                               = 396;  // <StmtList> ::= <StmtList> ';' <Statement>
		final int PROD_STATEMENT                                   = 397;  // <Statement> ::= <Label> <Statement>
		final int PROD_STATEMENT2                                  = 398;  // <Statement> ::= <AssignmentStmt>
		final int PROD_STATEMENT3                                  = 399;  // <Statement> ::= <CallStmt>
		final int PROD_STATEMENT4                                  = 400;  // <Statement> ::= <GotoStatement>
		final int PROD_STATEMENT5                                  = 401;  // <Statement> ::= <CompoundStmt>
		final int PROD_STATEMENT6                                  = 402;  // <Statement> ::= <IfStatement>
		final int PROD_STATEMENT7                                  = 403;  // <Statement> ::= <CaseStatement>
		final int PROD_STATEMENT8                                  = 404;  // <Statement> ::= <ForStatement>
		final int PROD_STATEMENT9                                  = 405;  // <Statement> ::= <WhileStatement>
		final int PROD_STATEMENT10                                 = 406;  // <Statement> ::= <RepeatStatement>
		final int PROD_STATEMENT11                                 = 407;  // <Statement> ::= <WithStatement>
		final int PROD_STATEMENT12                                 = 408;  // <Statement> ::= <TryFinallyStmt>
		final int PROD_STATEMENT13                                 = 409;  // <Statement> ::= <TryExceptStmt>
		final int PROD_STATEMENT14                                 = 410;  // <Statement> ::= <RaiseStmt>
		final int PROD_STATEMENT15                                 = 411;  // <Statement> ::= <AssemblerStmt>
//		final int PROD_STATEMENT_SYNERROR                          = 412;  // <Statement> ::= SynError
//		final int PROD_STATEMENT16                                 = 413;  // <Statement> ::= 
//		final int PROD_LABEL_COLON                                 = 414;  // <Label> ::= <LCONST> ':'
//		final int PROD_LABEL_COLON2                                = 415;  // <Label> ::= <LabelId> ':'
		final int PROD_ASSIGNMENTSTMT                              = 416;  // <AssignmentStmt> ::= <Designator> <AsnOp> <Expr>
		final int PROD_ASSIGNMENTSTMT_AT_COLONEQ                   = 417;  // <AssignmentStmt> ::= '@' <RefId> ':=' <Factor>
		final int PROD_CALLSTMT                                    = 418;  // <CallStmt> ::= <Designator>
		final int PROD_CALLSTMT_WRITE_LPAREN_RPAREN                = 419;  // <CallStmt> ::= WRITE '(' <FmtExprList> ')'
		final int PROD_CALLSTMT_WRITELN_LPAREN_RPAREN              = 420;  // <CallStmt> ::= WRITELN '(' <FmtExprList> ')'
		final int PROD_CALLSTMT_INHERITED                          = 421;  // <CallStmt> ::= INHERITED
		final int PROD_GOTOSTATEMENT_GOTO                          = 422;  // <GotoStatement> ::= GOTO <LCONST>
		final int PROD_GOTOSTATEMENT_GOTO2                         = 423;  // <GotoStatement> ::= GOTO <RefId>
		final int PROD_COMPOUNDSTMT_BEGIN_END                      = 424;  // <CompoundStmt> ::= BEGIN <StmtList> END
		final int PROD_IFSTATEMENT_IF_THEN_ELSE                    = 425;  // <IfStatement> ::= IF <Expr> THEN <Statement> ELSE <Statement>
		final int PROD_IFSTATEMENT_IF_THEN                         = 426;  // <IfStatement> ::= IF <Expr> THEN <Statement>
		final int PROD_IFSTATEMENT_IF_SYNERROR_THEN                = 427;  // <IfStatement> ::= IF SynError THEN <Statement>
		final int PROD_CASESTATEMENT_CASE_OF_END                   = 428;  // <CaseStatement> ::= CASE <Expr> OF <CaseList> <OtherWise> END
		final int PROD_FORSTATEMENT_FOR_COLONEQ_DO                 = 429;  // <ForStatement> ::= FOR <RefId> ':=' <Expr> <Dir> <Expr> DO <Statement>
//		final int PROD_DIR_TO                                      = 430;  // <Dir> ::= TO
//		final int PROD_DIR_DOWNTO                                  = 431;  // <Dir> ::= DOWNTO
		final int PROD_WHILESTATEMENT_WHILE_DO                     = 432;  // <WhileStatement> ::= WHILE <Expr> DO <Statement>
		final int PROD_WITHSTATEMENT_WITH_DO                       = 433;  // <WithStatement> ::= WITH <DesignatorList> DO <Statement>
//		final int PROD_DESIGNATORLIST                              = 434;  // <DesignatorList> ::= <Designator>
//		final int PROD_DESIGNATORLIST_COMMA                        = 435;  // <DesignatorList> ::= <DesignatorList> ',' <Designator>
		final int PROD_REPEATSTATEMENT_REPEAT_UNTIL                = 436;  // <RepeatStatement> ::= REPEAT <StmtList> UNTIL <Expr>
		final int PROD_ASSEMBLERSTMT_ASM_END                       = 437;  // <AssemblerStmt> ::= ASM <AsmLanguage> END
//		final int PROD_OTHERWISE_OTHERWISE                         = 438;  // <OtherWise> ::= OTHERWISE <StmtList>
//		final int PROD_OTHERWISE_ELSE                              = 439;  // <OtherWise> ::= ELSE <StmtList>
//		final int PROD_OTHERWISE                                   = 440;  // <OtherWise> ::= 
//		final int PROD_CASELIST                                    = 441;  // <CaseList> ::= <CaseSelector>
//		final int PROD_CASELIST_SEMI                               = 442;  // <CaseList> ::= <CaseList> ';' <CaseSelector>
//		final int PROD_CASELIST_SEMI2                              = 443;  // <CaseList> ::= <CaseList> ';'
//		final int PROD_CASESELECTOR_COLON                          = 444;  // <CaseSelector> ::= <CaseLabels> ':' <Statement>
//		final int PROD_CASELABELS                                  = 445;  // <CaseLabels> ::= <CaseLabel>
//		final int PROD_CASELABELS_COMMA                            = 446;  // <CaseLabels> ::= <CaseLabels> ',' <CaseLabel>
//		final int PROD_CASELABEL                                   = 447;  // <CaseLabel> ::= <ConstExpr>
//		final int PROD_CASELABEL_DOTDOT                            = 448;  // <CaseLabel> ::= <ConstExpr> '..' <ConstExpr>
		final int PROD_RAISESTMT_RAISE_SYNERROR                    = 449;  // <RaiseStmt> ::= RAISE SynError
		final int PROD_RAISESTMT_RAISE                             = 450;  // <RaiseStmt> ::= RAISE <OptExceptInstance>
		final int PROD_RAISESTMT_RAISE_AT                          = 451;  // <RaiseStmt> ::= RAISE <OptExceptInstance> AT <Address>
		final int PROD_TRYFINALLYSTMT_TRY_FINALLY_END              = 452;  // <TryFinallyStmt> ::= TRY <StmtList> FINALLY <StmtList> END
		final int PROD_TRYEXCEPTSTMT_TRY_EXCEPT_END                = 453;  // <TryExceptStmt> ::= TRY <StmtList> EXCEPT <ExceptionBlock> <OptExceptionElse> END
//		final int PROD_EXCEPTIONBLOCK                              = 454;  // <ExceptionBlock> ::= <ExceptionStmt>
		final int PROD_EXCEPTIONBLOCK_SEMI                         = 455;  // <ExceptionBlock> ::= <ExceptionBlock> ';' <ExceptionStmt>
		final int PROD_EXCEPTIONSTMT_ON_DO                         = 456;  // <ExceptionStmt> ::= ON <Selector> DO <Statement>
//		final int PROD_EXCEPTIONSTMT                               = 457;  // <ExceptionStmt> ::= <Statement>
		final int PROD_OPTEXCEPTIONELSE_ELSE                       = 458;  // <OptExceptionElse> ::= ELSE <StmtList>
//		final int PROD_OPTEXCEPTIONELSE                            = 459;  // <OptExceptionElse> ::= 
//		final int PROD_OPTEXCEPTINSTANCE                           = 460;  // <OptExceptInstance> ::= <Designator>
//		final int PROD_OPTEXCEPTINSTANCE2                          = 461;  // <OptExceptInstance> ::= 
//		final int PROD_ADDRESS                                     = 462;  // <Address> ::= <Designator>
//		final int PROD_OPTSEMI_SEMI                                = 463;  // <OptSemi> ::= ';'
//		final int PROD_OPTSEMI                                     = 464;  // <OptSemi> ::= 
//		final int PROD_PROCEDUREDECLSECTION                        = 465;  // <ProcedureDeclSection> ::= <ProcedureDecl>
//		final int PROD_PROCEDUREDECLSECTION2                       = 466;  // <ProcedureDeclSection> ::= <FunctionDecl>
//		final int PROD_PROCEDUREDECLSECTION3                       = 467;  // <ProcedureDeclSection> ::= <MethodDecl>
//		final int PROD_PROCEDUREDECL                               = 468;  // <ProcedureDecl> ::= <ProcHeading> <CallBody> <OptSemi>
//		final int PROD_FUNCTIONDECL                                = 469;  // <FunctionDecl> ::= <FuncHeading> <CallBody> <OptSemi>
//		final int PROD_METHODDECL                                  = 470;  // <MethodDecl> ::= <MethHeading> <CallBody> <OptSemi>
//		final int PROD_PROCHEADING_PROCEDURE_SEMI                  = 471;  // <ProcHeading> ::= PROCEDURE <RefId> <OptFormalParms> <OptCallSpecifiers> ';'
		final int PROD_PROCHEADING                                 = 472;  // <ProcHeading> ::= <ProcHeading> <CallDirectives> <OptSemi>
//		final int PROD_FUNCHEADING_FUNCTION_COLON_SEMI             = 473;  // <FuncHeading> ::= FUNCTION <RefId> <OptFormalParms> ':' <ResultType> <OptCallSpecifiers> ';'
		final int PROD_FUNCHEADING_FUNCTION_SEMI                   = 474;  // <FuncHeading> ::= FUNCTION <RefId> ';'
		final int PROD_FUNCHEADING                                 = 475;  // <FuncHeading> ::= <FuncHeading> <CallDirectives> <OptSemi>
		final int PROD_METHHEADING_PROCEDURE_DOT_SEMI              = 476;  // <MethHeading> ::= PROCEDURE <RefId> '.' <RefId> <OptFormalParms> <OptCallSpecifiers> ';'
		final int PROD_METHHEADING_FUNCTION_DOT_COLON_SEMI         = 477;  // <MethHeading> ::= FUNCTION <RefId> '.' <RefId> <OptFormalParms> ':' <ResultType> <OptCallSpecifiers> ';'
		final int PROD_METHHEADING_FUNCTION_DOT_SEMI               = 478;  // <MethHeading> ::= FUNCTION <RefId> '.' <RefId> ';'
		final int PROD_METHHEADING_CONSTRUCTOR_DOT_SEMI            = 479;  // <MethHeading> ::= CONSTRUCTOR <RefId> '.' <RefId> <OptFormalParms> <OptCallSpecifiers> ';'
		final int PROD_METHHEADING_DESTRUCTOR_DOT_SEMI             = 480;  // <MethHeading> ::= DESTRUCTOR <RefId> '.' <RefId> <OptFormalParms> <OptCallSpecifiers> ';'
		final int PROD_METHHEADING_CLASS_PROCEDURE_DOT_SEMI        = 481;  // <MethHeading> ::= CLASS PROCEDURE <RefId> '.' <RefId> <OptFormalParms> <OptCallSpecifiers> ';'
		final int PROD_METHHEADING_CLASS_FUNCTION_DOT_COLON_SEMI   = 482;  // <MethHeading> ::= CLASS FUNCTION <RefId> '.' <RefId> <OptFormalParms> ':' <ResultType> <OptCallSpecifiers> ';'
		final int PROD_METHHEADING_SEMI                            = 483;  // <MethHeading> ::= <MethHeading> <CallDirectives> ';'
//		final int PROD_RESULTTYPE                                  = 484;  // <ResultType> ::= <TypeRef>
//		final int PROD_OPTFORMALPARMS_LPAREN_RPAREN                = 485;  // <OptFormalParms> ::= '(' <FormalParmList> ')'
//		final int PROD_OPTFORMALPARMS_LPAREN_RPAREN2               = 486;  // <OptFormalParms> ::= '(' ')'
//		final int PROD_OPTFORMALPARMS                              = 487;  // <OptFormalParms> ::= 
//		final int PROD_FORMALPARMLIST                              = 488;  // <FormalParmList> ::= <FormalParm>
//		final int PROD_FORMALPARMLIST_SEMI                         = 489;  // <FormalParmList> ::= <FormalParmList> ';' <FormalParm>
//		final int PROD_FORMALPARM                                  = 490;  // <FormalParm> ::= <Parameter>
//		final int PROD_FORMALPARM_CONST                            = 491;  // <FormalParm> ::= CONST <Parameter>
//		final int PROD_FORMALPARM_VAR                              = 492;  // <FormalParm> ::= VAR <Parameter>
//		final int PROD_FORMALPARM_OUT                              = 493;  // <FormalParm> ::= OUT <Parameter>
//		final int PROD_PARAMETER                                   = 494;  // <Parameter> ::= <IdList>
//		final int PROD_PARAMETER_COLON                             = 495;  // <Parameter> ::= <IdList> ':' <ParmType>
//		final int PROD_PARAMETER_COLON_EQ                          = 496;  // <Parameter> ::= <IdList> ':' <TypeRef> '=' <ConstExpr>
//		final int PROD_PARMTYPE                                    = 497;  // <ParmType> ::= <TypeRef>
//		final int PROD_PARMTYPE_ARRAY_OF                           = 498;  // <ParmType> ::= ARRAY OF <TypeRef>
//		final int PROD_PARMTYPE_ARRAY_OF_CONST                     = 499;  // <ParmType> ::= ARRAY OF CONST
//		final int PROD_PARMTYPE_FILE                               = 500;  // <ParmType> ::= FILE
//		final int PROD_CALLBODY                                    = 501;  // <CallBody> ::= <OptDeclSection> <CompoundStmt>
//		final int PROD_CALLBODY2                                   = 502;  // <CallBody> ::= <OptDeclSection> <AssemblerStmt>
//		final int PROD_CALLBODY3                                   = 503;  // <CallBody> ::= <ExternalDeclaration>
//		final int PROD_CALLBODY_FORWARD                            = 504;  // <CallBody> ::= FORWARD
//		final int PROD_OPTPORTDIRECTIVES                           = 505;  // <OptPortDirectives> ::= <PortDirectives>
//		final int PROD_OPTPORTDIRECTIVES2                          = 506;  // <OptPortDirectives> ::= 
//		final int PROD_PORTDIRECTIVES                              = 507;  // <PortDirectives> ::= <PortDirective>
//		final int PROD_PORTDIRECTIVES2                             = 508;  // <PortDirectives> ::= <PortDirectives> <PortDirective>
//		final int PROD_PORTDIRECTIVE_PLATFORM                      = 509;  // <PortDirective> ::= PLATFORM
//		final int PROD_PORTDIRECTIVE_PLATFORM_EQ                   = 510;  // <PortDirective> ::= PLATFORM '=' <ConstExpr>
//		final int PROD_PORTDIRECTIVE_DEPRECATED                    = 511;  // <PortDirective> ::= DEPRECATED
//		final int PROD_PORTDIRECTIVE_LIBRARY                       = 512;  // <PortDirective> ::= LIBRARY
//		final int PROD_OPTMETHODDIRECTIVES                         = 513;  // <OptMethodDirectives> ::= <MethodDirectives>
//		final int PROD_OPTMETHODDIRECTIVES_SEMI                    = 514;  // <OptMethodDirectives> ::= <OptMethodDirectives> <PortDirective> ';'
//		final int PROD_OPTMETHODDIRECTIVES2                        = 515;  // <OptMethodDirectives> ::= 
//		final int PROD_METHODDIRECTIVES_SEMI                       = 516;  // <MethodDirectives> ::= <MethodDirective> ';'
//		final int PROD_METHODDIRECTIVES_SEMI2                      = 517;  // <MethodDirectives> ::= <MethodDirectives> <MethodDirective> ';'
//		final int PROD_METHODDIRECTIVE_VIRTUAL                     = 518;  // <MethodDirective> ::= VIRTUAL
//		final int PROD_METHODDIRECTIVE_VIRTUAL2                    = 519;  // <MethodDirective> ::= VIRTUAL <ConstExpr>
//		final int PROD_METHODDIRECTIVE_DYNAMIC                     = 520;  // <MethodDirective> ::= DYNAMIC
//		final int PROD_METHODDIRECTIVE_OVERRIDE                    = 521;  // <MethodDirective> ::= OVERRIDE
//		final int PROD_METHODDIRECTIVE_ABSTRACT                    = 522;  // <MethodDirective> ::= ABSTRACT
//		final int PROD_METHODDIRECTIVE_MESSAGE                     = 523;  // <MethodDirective> ::= MESSAGE <ConstExpr>
//		final int PROD_METHODDIRECTIVE_OVERLOAD                    = 524;  // <MethodDirective> ::= OVERLOAD
//		final int PROD_METHODDIRECTIVE_REINTRODUCE                 = 525;  // <MethodDirective> ::= REINTRODUCE
//		final int PROD_METHODDIRECTIVE_DISPID                      = 526;  // <MethodDirective> ::= DISPID <ConstExpr>
//		final int PROD_METHODDIRECTIVE                             = 527;  // <MethodDirective> ::= <CallConvention>
//		final int PROD_OPTPROPERTYDIRECTIVES_SEMI                  = 528;  // <OptPropertyDirectives> ::= <PropertyDirective> ';'
//		final int PROD_OPTPROPERTYDIRECTIVES_SEMI2                 = 529;  // <OptPropertyDirectives> ::= <OptPropertyDirectives> <PortDirective> ';'
//		final int PROD_OPTPROPERTYDIRECTIVES                       = 530;  // <OptPropertyDirectives> ::= 
//		final int PROD_PROPERTYDIRECTIVE_DEFAULT                   = 531;  // <PropertyDirective> ::= DEFAULT
//		final int PROD_EXTERNALDECLARATION_EXTERNAL                = 532;  // <ExternalDeclaration> ::= EXTERNAL
//		final int PROD_EXTERNALDECLARATION_EXTERNAL2               = 533;  // <ExternalDeclaration> ::= EXTERNAL <ConstStrExpr>
//		final int PROD_EXTERNALDECLARATION_EXTERNAL_NAME           = 534;  // <ExternalDeclaration> ::= EXTERNAL <ConstStrExpr> NAME <ConstStrExpr>
//		final int PROD_CALLDIRECTIVES                              = 535;  // <CallDirectives> ::= <CallDirective>
//		final int PROD_CALLDIRECTIVES2                             = 536;  // <CallDirectives> ::= <CallDirectives> <CallDirective>
//		final int PROD_CALLDIRECTIVE                               = 537;  // <CallDirective> ::= <CallConvention>
//		final int PROD_CALLDIRECTIVE2                              = 538;  // <CallDirective> ::= <CallObsolete>
//		final int PROD_CALLDIRECTIVE3                              = 539;  // <CallDirective> ::= <PortDirective>
//		final int PROD_CALLDIRECTIVE_VARARGS                       = 540;  // <CallDirective> ::= VARARGS
//		final int PROD_CALLDIRECTIVE_LOCAL                         = 541;  // <CallDirective> ::= LOCAL
//		final int PROD_CALLDIRECTIVE4                              = 542;  // <CallDirective> ::= <SCONST>
//		final int PROD_CALLDIRECTIVE_OVERLOAD                      = 543;  // <CallDirective> ::= OVERLOAD
//		final int PROD_OPTCALLSPECIFIERS                           = 544;  // <OptCallSpecifiers> ::= <CallSpecifier>
//		final int PROD_OPTCALLSPECIFIERS2                          = 545;  // <OptCallSpecifiers> ::= <OptCallSpecifiers> <CallSpecifier>
//		final int PROD_OPTCALLSPECIFIERS3                          = 546;  // <OptCallSpecifiers> ::= 
//		final int PROD_CALLSPECIFIER                               = 547;  // <CallSpecifier> ::= <CallConvention>
//		final int PROD_CALLSPECIFIER2                              = 548;  // <CallSpecifier> ::= <CallObsolete>
//		final int PROD_CALLCONVENTIONS                             = 549;  // <CallConventions> ::= <CallConvention>
//		final int PROD_CALLCONVENTIONS2                            = 550;  // <CallConventions> ::= <CallConventions> <CallConvention>
//		final int PROD_OPTCALLCONVENTIONS                          = 551;  // <OptCallConventions> ::= <CallConvention>
//		final int PROD_OPTCALLCONVENTIONS2                         = 552;  // <OptCallConventions> ::= <OptCallConventions> <CallConvention>
//		final int PROD_OPTCALLCONVENTIONS3                         = 553;  // <OptCallConventions> ::= 
//		final int PROD_CALLCONVENTION_REGISTER                     = 554;  // <CallConvention> ::= REGISTER
//		final int PROD_CALLCONVENTION_PASCAL                       = 555;  // <CallConvention> ::= PASCAL
//		final int PROD_CALLCONVENTION_CDECL                        = 556;  // <CallConvention> ::= CDECL
//		final int PROD_CALLCONVENTION_STDCALL                      = 557;  // <CallConvention> ::= STDCALL
//		final int PROD_CALLCONVENTION_SAFECALL                     = 558;  // <CallConvention> ::= SAFECALL
//		final int PROD_CALLOBSOLETE_INLINE                         = 559;  // <CallObsolete> ::= INLINE
//		final int PROD_CALLOBSOLETE_ASSEMBLER                      = 560;  // <CallObsolete> ::= ASSEMBLER
//		final int PROD_CALLOBSOLETE_NEAR                           = 561;  // <CallObsolete> ::= NEAR
//		final int PROD_CALLOBSOLETE_FAR                            = 562;  // <CallObsolete> ::= FAR
//		final int PROD_CALLOBSOLETE_EXPORT                         = 563;  // <CallObsolete> ::= EXPORT
//		final int PROD_ASMLANGUAGE                                 = 564;  // <AsmLanguage> ::= <AsmInstruction>
//		final int PROD_ASMLANGUAGE2                                = 565;  // <AsmLanguage> ::= <AsmLanguage> <AsmInstruction>
//		final int PROD_ASMINSTRUCTION                              = 566;  // <AsmInstruction> ::= <AsmItem>
//		final int PROD_ASMINSTRUCTION2                             = 567;  // <AsmInstruction> ::= <AsmInstruction> <AsmItem>
//		final int PROD_ASMINSTRUCTION_COMMA                        = 568;  // <AsmInstruction> ::= <AsmInstruction> ',' <AsmItem>
//		final int PROD_ASMINSTRUCTION_SEMI                         = 569;  // <AsmInstruction> ::= <AsmInstruction> ';'
//		final int PROD_ASMITEM                                     = 570;  // <AsmItem> ::= <AsmLabel>
//		final int PROD_ASMITEM2                                    = 571;  // <AsmItem> ::= <AsmExpr>
//		final int PROD_ASMLABEL_COLON                              = 572;  // <AsmLabel> ::= <AsmLocal> ':'
//		final int PROD_ASMLABEL_COLON2                             = 573;  // <AsmLabel> ::= <AsmId> ':'
//		final int PROD_ASMEXPR                                     = 574;  // <AsmExpr> ::= <AsmFactor>
//		final int PROD_ASMEXPR_MINUS                               = 575;  // <AsmExpr> ::= '-' <AsmFactor>
//		final int PROD_ASMEXPR_PLUS                                = 576;  // <AsmExpr> ::= <AsmExpr> '+' <AsmFactor>
//		final int PROD_ASMEXPR_TIMES                               = 577;  // <AsmExpr> ::= <AsmExpr> '*' <AsmFactor>
//		final int PROD_ASMEXPR_MINUS2                              = 578;  // <AsmExpr> ::= <AsmExpr> '-' <AsmFactor>
//		final int PROD_ASMEXPR_DOT                                 = 579;  // <AsmExpr> ::= <AsmExpr> '.' <AsmFactor>
//		final int PROD_ASMEXPR_LBRACKET_RBRACKET                   = 580;  // <AsmExpr> ::= '[' <AsmExpr> ']'
//		final int PROD_ASMEXPR_LPAREN_RPAREN                       = 581;  // <AsmExpr> ::= '(' <AsmExpr> ')'
//		final int PROD_ASMEXPR_SYNERROR                            = 582;  // <AsmExpr> ::= SynError
//		final int PROD_ASMFACTOR                                   = 583;  // <AsmFactor> ::= <AsmId>
//		final int PROD_ASMFACTOR2                                  = 584;  // <AsmFactor> ::= <AsmLocal>
//		final int PROD_ASMFACTOR3                                  = 585;  // <AsmFactor> ::= <ICONST>
//		final int PROD_ASMFACTOR4                                  = 586;  // <AsmFactor> ::= <RCONST>
//		final int PROD_ASMFACTOR5                                  = 587;  // <AsmFactor> ::= <SCONST>
//		final int PROD_ASMID                                       = 588;  // <AsmId> ::= <RefId>
//		final int PROD_ASMID_AMP                                   = 589;  // <AsmId> ::= '&' <RefId>
//		final int PROD_ASMID_REPEAT                                = 590;  // <AsmId> ::= REPEAT
//		final int PROD_ASMID_WHILE                                 = 591;  // <AsmId> ::= WHILE
//		final int PROD_ASMID_IF                                    = 592;  // <AsmId> ::= IF
//		final int PROD_ASMID_AND                                   = 593;  // <AsmId> ::= AND
//		final int PROD_ASMID_OR                                    = 594;  // <AsmId> ::= OR
//		final int PROD_ASMID_XOR                                   = 595;  // <AsmId> ::= XOR
//		final int PROD_ASMID_SHR                                   = 596;  // <AsmId> ::= SHR
//		final int PROD_ASMID_SHL                                   = 597;  // <AsmId> ::= SHL
//		final int PROD_ASMID_DIV                                   = 598;  // <AsmId> ::= DIV
//		final int PROD_ASMID_NOT                                   = 599;  // <AsmId> ::= NOT
//		final int PROD_ASMLOCAL_AT                                 = 600;  // <AsmLocal> ::= '@' <LCONST>
//		final int PROD_ASMLOCAL_AT2                                = 601;  // <AsmLocal> ::= '@' <AsmId>
//		final int PROD_ASMLOCAL_AT3                                = 602;  // <AsmLocal> ::= '@' <AsmLocal>
//		final int PROD_ASMLOCAL_AT_END                             = 603;  // <AsmLocal> ::= '@' END
	};
	// END KGU#358 2017-03-29

	// START KGU#407 2017-06-22: Enh. #420 - rule ids representing statements, used as stoppers for comment rerieval
	private static final int[] statementIds = new int[]{
			RuleConstants.PROD_PACKAGEHEADER_PACKAGE_SEMI,
			RuleConstants.PROD_OPTREQUIRESCLAUSE_REQUIRES_SEMI,
			RuleConstants.PROD_OPTCONTAINSCLAUSE_CONTAINS_SEMI,
			RuleConstants.PROD_EXPORTDECLLIST,
			RuleConstants.PROD_EXPORTDECLLIST2,
			RuleConstants.PROD_EXPORTDECLITEM,
			RuleConstants.PROD_EXPORTDECLITEM2,
			RuleConstants.PROD_EXPORTDECLITEM3,
			RuleConstants.PROD_EXPORTDECLITEM4,
			RuleConstants.PROD_EXPORTDECLITEM_FORWARD_SEMI,
			RuleConstants.PROD_CALLSECTION,
			RuleConstants.PROD_CALLSECTION2,
			// START KGU#1073 224-03-14: Issue #1084 ObjectPascal import
			RuleConstants.PROD_CLASSMETHODSPEC,
			RuleConstants.PROD_CLASSMETHODSPEC2,
			RuleConstants.PROD_CLASSMETHODSPEC_CLASS,
			RuleConstants.PROD_CLASSMETHODSPEC_CLASS2,
			RuleConstants.PROD_FIELDSPEC_COLON_SEMI,
			// END KGU#1073 2024-03-14
			RuleConstants.PROD_IMPLEMENTATIONSECTION_IMPLEMENTATION,
			RuleConstants.PROD_INITSECTION_INITIALIZATION_END,
			RuleConstants.PROD_INITSECTION_INITIALIZATION_FINALIZATION_END,
			RuleConstants.PROD_INITSECTION,
			RuleConstants.PROD_INITSECTION_END,
			RuleConstants.PROD_STATEMENT,
			RuleConstants.PROD_STATEMENT2,
			RuleConstants.PROD_STATEMENT3,
			RuleConstants.PROD_STATEMENT4,
			RuleConstants.PROD_STATEMENT5,
			RuleConstants.PROD_STATEMENT6,
			RuleConstants.PROD_STATEMENT7,
			RuleConstants.PROD_STATEMENT8,
			RuleConstants.PROD_STATEMENT9,
			RuleConstants.PROD_STATEMENT10,
			RuleConstants.PROD_STATEMENT11,
			RuleConstants.PROD_STATEMENT12,
			RuleConstants.PROD_STATEMENT13,
			RuleConstants.PROD_STATEMENT14,
			RuleConstants.PROD_STATEMENT15,
			RuleConstants.PROD_ASSIGNMENTSTMT,
			RuleConstants.PROD_ASSIGNMENTSTMT_AT_COLONEQ,
			RuleConstants.PROD_CALLSTMT,
			RuleConstants.PROD_CALLSTMT_WRITE_LPAREN_RPAREN,
			RuleConstants.PROD_CALLSTMT_WRITELN_LPAREN_RPAREN,
			RuleConstants.PROD_CALLSTMT_INHERITED,
			RuleConstants.PROD_GOTOSTATEMENT_GOTO,
			RuleConstants.PROD_GOTOSTATEMENT_GOTO2,
			RuleConstants.PROD_COMPOUNDSTMT_BEGIN_END,
			RuleConstants.PROD_IFSTATEMENT_IF_THEN_ELSE,
			RuleConstants.PROD_IFSTATEMENT_IF_THEN,
			RuleConstants.PROD_IFSTATEMENT_IF_SYNERROR_THEN,
			RuleConstants.PROD_CASESTATEMENT_CASE_OF_END,
			RuleConstants.PROD_FORSTATEMENT_FOR_COLONEQ_DO,
			RuleConstants.PROD_WHILESTATEMENT_WHILE_DO,
			RuleConstants.PROD_WITHSTATEMENT_WITH_DO,
			RuleConstants.PROD_REPEATSTATEMENT_REPEAT_UNTIL,
			RuleConstants.PROD_ASSEMBLERSTMT_ASM_END,
			RuleConstants.PROD_RAISESTMT_RAISE_SYNERROR,
			RuleConstants.PROD_RAISESTMT_RAISE,
			RuleConstants.PROD_RAISESTMT_RAISE_AT,
			RuleConstants.PROD_OPTEXCEPTIONELSE_ELSE,
	};
	// END KGU#407 2017-06-22
	// END KGU#387 2021-02-15

	// START KGU#843 2020-04-11: Bugfix #847 - We must convert all operator names to lower-case
	/** Production rule heads designating operator symbols */
	private static final StringList OPR_RULE_HEADS = StringList.explode("<MulOp>,<RelOp>,<AddOp>", ",");
	/** Identifiers belonging to literals or functions the names of which will only be accepted in lowercase by Structorizer */
	private static final StringList NAMES_TO_LOWER = StringList.explode("FALSE,TRUE,CHR,ORD,POS,COPY,DELETE,INSERT,UPPERCASE,LOWERCASE", ",");
	// END KGU#843 2020-04-11

// START KGU#387 2021-02-16: Issue #939 workaround no longer necessary
//    // START KGU#575 2018-09-17: Issue #594 - replace obsolete 3rd-party regex library
//    /** Matcher for temporary newline surrogates */
//    private static final java.util.regex.Matcher NEWLINE_MATCHER = java.util.regex.Pattern.compile("(.*?)[\u2190](.*?)").matcher("");
//    // END KGU#575 2018-09-17
// END KGU#387 2021-02-16

	// START KGU#354 2017-03-04: Now inherited from CodeParser
	//Root root = null;
	// START KGU#194 2016-05-08: Bugfix #185
	// We may obtain a collection of Roots (unit or program with subroutines)!
	//private List<Root> subRoots = new LinkedList<Root>();
	// END KGU#194 2016-05-08
	//
	//public String error = new String();
	// END KGU#354 2017-03-04
	
	// START KGU#194 2016-05-08: Bugfix #185 - if being a unit we must retain its name
	private String unitName = null;
	// END KGU#194 2016-05-08
	
	// START KGU#991 2021-10-03: Issue #991 Ensure exact spelling of result variables
	/**
	 * Holds the function name within a function declaration to adopt its exact case-
	 * aware spelling for the result variable.
	 */
	private String functionName = null;
	// END KGU#991 2021-10-03
	
	// START KGU#821 2020-03-07: Issue #833 We must process parameterless routines
	/** List of the names of detected parameterless routines (in order to add parentheses) */
	private StringList paramlessRoutineNames = new StringList();
	
	/** Registers all occurring USES clauses in order to add them to the comments of imported {@link Root}s. */
	private StringList usesClauses = new StringList();
	// END KGU#821 2020-03-07

	// START KGU#1073 2024-03-14: Issue #1084 Handle classes properly
	/** Represents the class definitions in the hierarchical class context */
	private ArrayList<Root> includables = null;
	// END KGU#1073 2024-03-14

	/**
	 * Removes all non-Ascii characters from the given string {@code inString}
	 * 
	 * @param inString - the string to be filtered
	 * @return - the filter result
	 * @deprecated No longer needed for Pascal import - if it turns out to be for others
	 *     then it should go to {@link CodeParser} instead.
	 */
	@Deprecated
	public String filterNonAscii(String inString) 
	{
		// Create the encoder and decoder for the character encoding
		Charset charset = Charset.forName("ASCII");
		//Charset charset = Charset.forName("ISO-8859-1");
		CharsetDecoder decoder = charset.newDecoder();
		CharsetEncoder encoder = charset.newEncoder();
		// This line is the key to removing "unmappable" characters.
		encoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
		String result = inString;
		
		try 
		{
			// Convert a string to bytes in a ByteBuffer
			ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(inString));
			
			// Convert bytes in a ByteBuffer to a character ByteBuffer and then to a string.
			CharBuffer cbuf = decoder.decode(bbuf);
			result = cbuf.toString();
		} 
		catch (CharacterCodingException cce) 
		{
			getLogger().log(Level.WARNING, "Exception during character encoding/decoding: {0}", cce.getMessage());
		}
		
		return result;	
	}
	

	// START KGU#354 2017-03-03: Enh. #354 - generalized import mechanism
	@Override
	protected File prepareTextfile(String _textToParse, String _encoding) throws ParserCancelled, FilePreparationException
	{
		File interm = null;
		try
		{
			String pasCode = new String();
			DataInputStream in = new DataInputStream(new FileInputStream(_textToParse));
			// START KGU#193 2016-05-04
			BufferedReader br = new BufferedReader(new InputStreamReader(in, _encoding));
			// END KGU#193 2016-05-04
			try {
				String strLine;
				// START KGU#589 2018-09-28: Workaround #615 - KGU#387 2021-02-15 obsolete.
				// First item means within "(* *)", second item means within "{ }"
				//boolean[] inComments = {false, false};
				// END KGU#589 2018-09-28
				//Read File Line By Line
				while ((strLine = br.readLine()) != null)   
				{
					// START KGU#537 2018-07-01: Enh. #553 Make parser interruptible
					checkCancelled();
					// END KGU#537 2018-07-01

					// START KGU#387/KGU#589 2021-02-15: #939 Workaround #615 now obsolete
					// START KGU#589 2018-09-28: Workaround #615
					//strLine = unifyComments(strLine, inComments);
					// END KGU#589 2018-09-28
					// add no ending because of comment filter
					//pasCode += strLine+"\u2190";
					pasCode+=strLine+"\n";
					// END KGU#387/KGU#589 2021-02-15
				}
			}
			finally {
				//Close the input stream
				in.close();
			}

			// START KGU#195 2016-05-04: Issue #185 - Workaround for mere subroutines
			pasCode = embedSubroutineDeclaration(pasCode);
			// END KGU#195 2016-05-04

// START KGU#387 2021-02-16: Issue #939 obsolete stuff disabled
//			// reset correct endings
//			// START KGU#575 2018-09-17: Issue #594 - replacing obsolete 3rd-party regex library
//			//Regex r = new Regex("(.*?)[\u2190](.*?)","$1\n$2"); 
//			//pasCode = r.replaceAll(pasCode);
//			pasCode = NEWLINE_MATCHER.reset(pasCode).replaceAll("$1\n$2");
//			// END KGU#575 2018-09-17
//			// START KGU#354 2017-03-07: Workaround for missing second comment delimiter pair in GOLDParser 5.0
////			pasCode = pasCode.replaceAll("(.*?)(\\(\\*)(.*?)(\\*\\))(.*?)", "$1\\{$3\\}$5");
//			// END KGU#354 2017-03-07
//
//			//System.out.println(pasCode);
// END KGU#387 2021-02-16

			// trim and save as new file
			//interm = new File(_textToParse + ".structorizer");
			interm = File.createTempFile("Structorizer", ".pas");
			// START KGU#387 2021-02-16: Issue #939 - Eventually we may get rid of this filtering
			//OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(interm), "ISO-8859-1");
			//try {
			//	ow.write(filterNonAscii(pasCode.trim()+"\n"));
			//}
			//finally {
			//	ow.close();
			//}
			try (OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(interm), _encoding)) {
				ow.write(pasCode.trim() +"\n");
			}
			// END KGU#387 2021-02-16
		}
		catch (Exception e) 
		{
			getLogger().severe(e.getMessage());
		}	
		return interm;
	}
	// END KGU#354 2017-03-03
	
//	// START KGU589 2018-09-28: Issue #615 - KGU#387 2021-02-16: No longer needed (Issue #939)
//	/**
//	 * Replaces all comment delimiters of kind (* *) by pairs of curly braces (and therefore closing
//	 * braces within (* *) former comments with "[brace-close]" to avoid comment corruption as comments
//	 * of same kind cannot be nested.
//	 * FIXME: This method is only a workaround while the grammar doesn't cope with (* *) comments.  
//	 * @param pasLine - the original Pascal line
//	 * @param withinComments - a pair of flags: [0] for inside (* *), [1] for inside { }.
//	 * @return the Pascal line with unified comments (length may have changed)
//	 */
//	private String unifyComments(String pasLine, boolean[] withinComments) {
//		// Let's do a quick test first
//		if (withinComments[0] && (pasLine.contains("*)") || (pasLine.contains("}")))
//				|| !withinComments[0] && pasLine.contains("(*")) {
//			// Now we must have a closer look to exclude false positives in string constants
//			StringBuilder sb = new StringBuilder();
//			boolean inString = false;
//			int len = pasLine.length();
//			int i = 0;
//			while (i < len) {
//				char c = pasLine.charAt(i);
//				if (!withinComments[0] && !withinComments[1]) {
//					if (c == '\'') {
//						inString = !inString;
//					}
//					else if (c == '{') {
//						withinComments[1] = true;
//					}
//					else if (!inString && c == '(' && i+1 < len && pasLine.charAt(i+1) == '*') {
//						withinComments[0] = true;
//						c = '{';
//						i++;
//					}
//				}
//				else if (withinComments[1]) {
//					if (c == '}') {
//						withinComments[1] = false;
//					}
//				}
//				// So we must be within a comment of type (* *)
//				else if (c == '*' && i+1 < len && pasLine.charAt(i+1) == ')') {
//					withinComments[0] = false;
//					c = '}';
//					i++;
//				}
//				else if (c == '}') {
//					// This might cause trouble as it would shorten the comment!
//					// so replace it by something irrelevant within a comment
//					sb.append("[brace-close");
//					c = ']';
//				}
//				sb.append(c);
//				i++;
//			}
//			pasLine = sb.toString();
//		}
//		return pasLine;
//	}
//	// END KGU#589 2018-09-28

	// START KGU#195 2016-05-04: Issue #185 - Workaround for mere subroutines
	private String embedSubroutineDeclaration(String _pasCode) throws ParserCancelled
	{
		// Find the first non-empty line where line ends are encoded as "\u2190"
		boolean headerFound = false;
		int pos = -1;
		int lineEnd = -1;
		// START KGU#387 2021-02-16: Issue #939 line end workaround no longer necessary
		//while (!headerFound && (lineEnd = _pasCode.indexOf("\u2190", pos+1)) >= 0)
		while (!headerFound && (lineEnd = _pasCode.indexOf("\n", pos+1)) >= 0)
		// END KGU#387 2021-02-16
		{
			// START KGU#537 2018-07-01: Enh. #553
			checkCancelled();
			// END KGU#537 2018-07-01
			String line = _pasCode.substring(pos+1, lineEnd).toLowerCase();
			pos = lineEnd;
			// If the file contains a program or unit then we leave it as is
			// for the moment...
			if (line.startsWith("program") || line.startsWith("unit"))
			{
				headerFound = true;
			}
			else if (line.startsWith("function") ||
					 line.startsWith("procedure"))
			{
				// embed the declaration in a dummy program definition as
				// workaround
				headerFound = true;
				// START KGU#387 2021-02-16: Issue #939 line end workaround no longer necessary
				//_pasCode = "program dummy;" + "\u2190"
				//		+ _pasCode + "\u2190"
				//		+ "begin" + "\u2190"
				//		+ "end." + "\u2190";
				_pasCode = "program dummy;" + "\n"
						+ _pasCode + "\n"
						+ "begin" + "\n"
						+ "end." + "\n";
				// END KGU#387 2021-02-16
			}
		}
		return _pasCode;
	}
	// END KGU#195 2016-05-04

	// START KGU#586 2018-09-28: Bugfix #613 - modified include mechanism
	// Include lists should be filled immediately - the includables might remain empty
	/** Holds the {@link Root}s including the unit globals (if there are any) */
	protected LinkedList<Root> includerList = new LinkedList<Root>();
	// END KGU#586 2018-09-28

	// START KGU#1073 2024-03-15: Issue #1084 we must distinguish INTERFACE context
	/** Indicates whether or not we are inside an INTERFACE section */
	private boolean withinInterface = false;
	// END KGU#1073 2024-03-15

	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.parsers.CodeParser#initializeBuildNSD()
	 */
	@Override
	protected void initializeBuildNSD() throws ParserCancelled
	{
		// START KGU#194 2016-05-08: Bugfix #185
		unitName = null;
		// END KGU#194 2016-05-08
		// START KGU#1073 2024-03-14: Issue #1084 Allow ObjectPascal import
		includables = new ArrayList<Root>();
		// END KGU#1073 2024-03-14

		// START KGU#407 207-06-22: Enh. #420: Configure the lookup table for comment retrieval
		this.registerStatementRuleIds(statementIds);
		// END KGU#407 2017-06-11
	}
	
	@Override
	protected void buildNSD_R(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled
	{
		// START KGU#537 2018-07-01: Enh. #553
		checkCancelled();
		// END KGU#537 2018-07-01
		String content = new String();
	
		if (_reduction.size() > 0)
		{
			String ruleHead = _reduction.getParent().getHead().toString();
			int ruleId = _reduction.getParent().getTableIndex();

			//System.out.println(ruleHead);
			if (
				ruleHead.equals("<RefId>")
				||
				ruleHead.equals("<CallStmt>")
				||
				ruleHead.equals("<Designator>")
				||
				ruleHead.equals("<AssignmentStmt>")
				// START KGU#1073 2024-03-15: Issue #1084: ObjectPascal import
				||
				ruleHead.equals("<FieldDesignator>")
				// END KGU#1073 2024-03-15
			   )
			{
				content = getContent_R(_reduction, "");
				//System.out.println(ruleHead + ": " + content);
				// START KGU#407 2017-06-20: Enh. #420 - comments already here
				//_parentNode.addElement(new Instruction(translateContent(content)));
				// START KGU#821 2020-03-08: Issue #833 - check parameterless routine calls
				//_parentNode.addElement(this.equipWithSourceComment(new Instruction(translateContent(content)), _reduction));
				_parentNode.addElement(this.equipWithSourceComment(new Instruction(translateContentWithCalls(content)), _reduction));
				// END KGU#407 2017-06-22
			}
			// START KGU#358 2017-03-29: Enh. #368
			else if (
					ruleHead.equals("<VarDecl>")
					&&
					ruleId != RuleConstants.PROD_VARDECL_SYNERROR_SEMI
					||
					ruleHead.equals("<ConstantDecl>")
					&&
					_reduction.get(0).asReduction().getParent().getTableIndex() == RuleConstants.PROD_REFID_ID
					)
			{
				String idList = getContent_R(_reduction.get(0).asReduction(), "");
				String[] ids = idList.split(",");
				String value = "";
				String type = "";
				Instruction instr = null;
				switch (ruleId)
				{
				case RuleConstants.PROD_VARDECL_COLON_EQ_SEMI:
				case RuleConstants.PROD_CONSTANTDECL_COLON_EQ_SEMI:
					{
						boolean isConstant = ruleId == RuleConstants.PROD_CONSTANTDECL_COLON_EQ_SEMI;
						// So called typed constant - this is always to be made an instruction
						// START KGU#375/KGU#388 2017-09-22: Enh. #388, #423 constants and record support
						String prefix = ruleId == RuleConstants.PROD_VARDECL_COLON_EQ_SEMI ? "var" : "const";
						// FIXME: Handle type specifiers here (for type ids it's ok)!
						type = getContent_R(_reduction.get(2).asReduction(), "");
						// FIXME: Decompose the value of structured initializers!
						Reduction valRed = _reduction.get(4).asReduction(); 
						switch (valRed.getParent().getTableIndex()) {
						case RuleConstants.PROD_ARRAYCONSTANT_LPAREN_RPAREN:
							value = getContent_R(valRed.get(1).asReduction(), " <- {") + "}";
							{
								StringList valueTokens = Element.splitLexically(value, true);
								valueTokens.replaceAll(",", ", ");
								value = valueTokens.concatenate();
							}
							break;
						case RuleConstants.PROD_RECORDCONSTANT_LPAREN_RPAREN:
						case RuleConstants.PROD_RECORDCONSTANT_LPAREN_SEMI_RPAREN:
							value = getContent_R(valRed.get(1).asReduction(), " <- " + type + "{") + "}";
							{
								StringList valueTokens = Element.splitLexically(value, true);
								valueTokens.replaceAll(":", ": ");
								valueTokens.replaceAll(";", "; ");
								value = valueTokens.concatenate();
							}
							break;
						default:
							value = getContent_R(valRed, " <- ");
						}
						if (!this.optionImportVarDecl || isConstant) {
							// FIXME: Handle type specifiers here (for type ids it's ok)!
							type = ": " + type;
						}
						else {
							type = "";
						}
						StringList lines = new StringList();
						for (int i = 0; i < ids.length; i++) {
							// We replace 'value' by ids[0] in lines > 0
							lines.add(prefix + " " + ids[i] + type + value);
							value = " <- " + ids[0];
						}
						instr = new Instruction(StringList.explode(translateContent(lines.getText()), "\n"));
						// START KGU#407 2017-06-20: Enh. #420 - comments already here
						//asgnmt.setComment("constant!");
						this.equipWithSourceComment(instr, _reduction);
						//asgnmt.getComment().add("Constant!");
						// END KGU#407 2017-06-22
						if (isConstant) {
							instr.setColor(colorConst);
						}
						_parentNode.addElement(instr);
					}
					break;
				case RuleConstants.PROD_VARDECL_COLON_SEMI:
				case RuleConstants.PROD_VARDECL_COLON:
					// Uninitialized variable declarations
					if (this.optionImportVarDecl) {
						Reduction typeRule = _reduction.get(2).asReduction();
						if (ruleId == RuleConstants.PROD_VARDECL_COLON) {
							typeRule = typeRule.get(0).asReduction();
						}
						content = getContent_R(typeRule, "var " + idList + ": ");
						instr = new Instruction(translateContent(content));
						// START KGU#407 2017-06-20: Enh. #420 - comments already here
						this.equipWithSourceComment(instr, _reduction);
						// END KGU#407 2017-06-22
						instr.setColor(colorDecl);
						_parentNode.addElement(instr);
					}
					break;
				case RuleConstants.PROD_CONSTANTDECL_EQ_SEMI:
					content = getContent_R(_reduction.get(2).asReduction(), "const " + idList + " <- ");
					instr = new Instruction(translateContent(content));
					// START KGU#407 2017-06-20: Enh. #420 - comments already here
					//asgnmt.setComment("constant!");
					this.equipWithSourceComment(instr, _reduction);
					//asgnmt.getComment().add("Constant!");
					// END KGU#407 2017-06-22
					instr.setColor(colorConst);
					_parentNode.addElement(instr);
					break;
				default:;
				}
			}
			// END KGU#358 2017-03-29
			// START KGU#388 2017-09-22: Enh. #423 accept record type definitions
			else if (
					ruleId == RuleConstants.PROD_TYPEDECL_EQ
					&&
					// <TypeSpec> ::= <GenericType> ';'
					_reduction.get(2).asReduction().getParent().getTableIndex() == RuleConstants.PROD_TYPESPEC_SEMI
					)
			{
				// <TypeDecl> ::= <TypeId> '=' <TypeSpec>
				// with <TypeSpec> ::= <GenericType> ';'
				boolean isEnum = false;
				String typeName = this.getContent_R(_reduction.get(0).asReduction(), "type ") + " = ";
				Reduction secRed = _reduction.get(2).asReduction().get(0).asReduction();
				int subRuleId = secRed.getParent().getTableIndex();
				switch (subRuleId) {
				case RuleConstants.PROD_RECTYPE_RECORD_END:
					// FIXME: It might be necessary recursively to decompose the type specifier if nested
					content = this.getContent_R(secRed.get(2).asReduction(), typeName + "record{") + "}";
					content = content.replace(":", ": ").replace(";}", "}").replace(";", "; ");
					break;
				case RuleConstants.PROD_ARRAYTYPE_ARRAY_LBRACKET_RBRACKET_OF:
				case RuleConstants.PROD_ARRAYTYPE_ARRAY_OF_CONST:
				case RuleConstants.PROD_ARRAYTYPE_ARRAY_OF:
					content = this.getContent_R(secRed, typeName);
					break;
				// START KGU#542 2018-07-11: Enh. #558 - provisional enum type support
				// START KGU#387 2021-02-15: Issue #939 - Modified enum type definition
				//case RuleConstants.PROD_ENUMTYPE_LPAREN_RPAREN:
				//case RuleConstants.PROD_ENUMTYPE_LPAREN_RPAREN2:
				//	content = importEnumType(secRed.get(1).asReduction(), typeName);
				case RuleConstants.PROD_ENUMTYPE_LPAREN_COMMA_RPAREN:
					content = importEnumType(secRed, typeName);
				// END KGU#387 2021-02-15
					isEnum = true;
					break;
				// END KGU#542 2018-07-11
				default:
					break;
				}
				if (!content.isEmpty()) {
					// FIXME: Could be global if outside of functions! --> global Root --> includable
					Element def = this.equipWithSourceComment(new Instruction(translateContent(content)), _reduction);
					_parentNode.addElement(def);
					if (isEnum) {
						def.setColor(colorConst);
						// START KGU#542 2019-11-19: Enh. #739 - No longer needed, now genuine enum type import
						//def.getComment().add("Enumerator type " + typeName);
						// END KGU#542 2019-11-19
					}
				}
			}
			// END KGU#388 2017-09-22
			// START KGU#1073 2024-03-24: Issue #1084 Handle class types properly
			else if (
					ruleId == RuleConstants.PROD_TYPEDECL_EQ
					&&
					// <TypeSpec> ::= <RestrictedType> <OptPortDirectives> ';'
					_reduction.get(2).asReduction().getParent().getTableIndex() == RuleConstants.PROD_TYPESPEC_SEMI2
					)
			{
				// <TypeDecl> ::= <TypeId> '=' <TypeSpec>
				// with <TypeSpec> ::= <RestrictedType> <OptPortDirectives> ';'
				/* Now we have to produce both a record type definition with the public components
				 * and an includable at the same time
				 */
				String comment1 = this.retrieveComment(_reduction);
				StringList comment = new StringList();
				String typeName = this.getContent_R(_reduction.get(0).asReduction(), "");
				Reduction secRed = _reduction.get(2).asReduction().get(0).asReduction();
				if (comment1 != null && !comment1.isBlank()) {
					comment.add(comment1);
				}
				ruleId = secRed.getParent().getTableIndex();
				switch (ruleId) {
				case RuleConstants.PROD_CLASSTYPE_CLASS_END:
					// <ClassType> ::= CLASS <OptClassHeritage> <ClassMemberList>
					StringBuilder sb = new StringBuilder();
					sb.append("type ");
					sb.append(typeName);
					sb.append(" = record {");	// FIXME We might introduce a class type
					String heritage = "";
					if (secRed.get(1).asReduction().size() > 0) {
						heritage = this.getContent_R(secRed.get(1).asReduction().get(1).asReduction(), "");
					}
					comment.add("CLASS");
					buildClassDefinition(typeName, heritage, secRed.get(2).asReduction(), sb, comment);
					sb.append("}");
					content = sb.toString();
					break;
				//case RuleConstants.PROD_OBJECTTYPE_OBJECT_END: TODO?
				//case RuleConstants.PROD_CLASSTYPE_CLASS: TODO?
				//case RuleConstants.PROD_CLASSREFTYPE_CLASS_OF: TODO?
				//case RuleConstants.PROD_INTERFACETYPE_INTERFACE_END: TODO?
				//case RuleConstants.PROD_INTERFACETYPE_DISPINTERFACE: TODO?
				}
				// The definition must be added even if it does not contain any (public) fields
				/* FIXME It might be an ideas, however, to introduce a new type category "class"
				 * as an extension of record/struct, which should hold public method entries next
				 * to the public fields. Proposed syntax for the methods would be like subroutine
				 * headers:
				 * type Classtype = class {
				 *     field1: type1;
				 *     field2: type2;
				 *     procmethod(arg1, arg2, arg3);
				 *     funcmethod(arg1, arg2): type3
				 * }
				 */
				Element def = new Instruction(translateContent(content));
				def.setComment(comment);
				_parentNode.addElement(def);
			}
			// END KGU#1073 2024-03-14
			else if (
					 ruleHead.equals("<UsesClause>")
			// START KGU#821 2020-03-08: Issue #833 Register uses clauses to add them as comments to all Roots
			//		 // START KGU#358 2017-03-29: Enh. #368 we do no longer ignore const/var declarations
			//		 //||
			//		 //ruleHead.equals("<VarSection>")
			//		 //||
			//		 //ruleHead.equals("<ConstSection>")
			//		 //||
			//		 //ruleHead.equals("<TypeSection>")
			//		 ||
					 ) {
				String usesClause = this.getContent_R(_reduction, "");
				this.usesClauses.add(usesClause);
			}
			// START KGU#1073 2024-03-15: Issue #1084 to ignore <InterfaceSection> and <InitSection> was outright wrong
			//else if (
			//// END KGU#821 2020-03-08
			//		 ruleHead.equals("<LabelSection>")
			//		 // END KGU#358 2017-03-29
			//		 // START KGU#194 2016-05-08: Bugfix #185
			//		 // UNIT Interface section can be ignored, all contained routines
			//		 // must be converted from the implementation section
			//		 ||
			//		 ruleHead.equals("<InterfaceSection>")
			//		 ||
			//		 ruleHead.equals("<InitSection>")
			//		 // END KGU#194 2016-05-08
			//		 )
			//{
			//	// This is just to skip these sections
			//}
			else if (ruleHead.equals("<LabelSection>")) {
				// Ignore this section
			}
			else if (ruleHead.equals("<InterfaceSection>")) {
				// Process the uses clause
				this.buildNSD_R(_reduction.get(1).asReduction(), _parentNode);
				// Process the actual export declarations
				this.withinInterface = true;
				this.buildNSD_R(_reduction.get(2).asReduction(), _parentNode);
				this.withinInterface = false;
			}
			// END KGU#1073 2024-03-15
			// START KGU#1073 2024-03-15: Issue 
			// START KGU#194 2016-05-08: Bugfix #185 - we must handle unit headers
			else if (
					ruleHead.equals("<UnitHeader>")
					 )
			{
				unitName = getContent_R(_reduction.get(1).asReduction(), "");
				// START KGU#407 2018-09-28: Enh. #420 - comments already here
				String comment = this.retrieveComment(_reduction);
				if (comment != null && !comment.isBlank()) {
					root.getComment().add(StringList.explode(comment, "\n"));
				}
				// END KGU#407 2018-09-28
			}
			else if (
					// <ProcedureDecl> ::= <ProcHeading> <CallBody> <OptSemi>
					ruleHead.equals("<ProcedureDecl>")
					||
					// <FunctionDecl> ::= <FuncHeading> <CallBody> <OptSemi>
					ruleHead.equals("<FunctionDecl>")
					||
					// <MethodDecl> ::= <MethHeading> <CallBody> <OptSemi>
					ruleHead.equals("<MethodDecl>")
					)
			{
				Root prevRoot = root;	// Push the original root
				// If this root is top level and a program diagram then there may only have been declarations
				// so far. And these may be global. So transfer them to a new includable diagram and act as if
				// it were a unit.
				if (unitName == null && this.getSubRootCount() == 0 && prevRoot.isProgram()) {
					Root includable = new Root();
					String progName = prevRoot.getMethodName();
					unitName = progName;
					includable.setText(unitName + DEFAULT_GLOBAL_SUFFIX);
					includable.setComment("Global declarations for program " + progName);
					for (int i = 0; i < prevRoot.children.getSize(); i++) {
						Element elem = prevRoot.children.getElement(i);
						elem.parent = includable.children;
						includable.children.addElement(elem);
					}
					includable.setInclude(true);
					prevRoot.children.removeElements();
					prevRoot.addToIncludeList(unitName + DEFAULT_GLOBAL_SUFFIX);
					// START KGU#586 2018-09-28: Bugfix #613 - Register the inclusion, allowing the postprocess to check it
					this.includerList.add(prevRoot);
					// END KGU#586 2018-09-28
					this.addRoot(includable);
				}
				root = new Root();	// Prepare a new root for the subroutine
				// START KGU#407 2017-06-20: Enh. #420 - comments already here
				this.equipWithSourceComment(root, _reduction);
				// END KGU#407 2017-06-22
				this.addRoot(root);
				for (int i = 0; i < _reduction.size(); i++)
				{
					if (_reduction.get(i).getType() == SymbolType.NON_TERMINAL)
					{
						buildNSD_R(_reduction.get(i).asReduction(), root.children);
					}
				}
				// Restore the original root
				// START KGU#376 2017-09-22: Enh. #389
				// FIXME transfer declarations to a global includable if prevRoot 
				
				// END KGU#376 2017-09-22
				// START KGU#991 2021-10-03: Issue #991 Uncache the function name on leaving definition context
				functionName = null;
				// END KGU#991 2021-10-03
				root = prevRoot;
			}
			// END KGU#194 2016-05-08
			else if (
					 ruleHead.equals("<ProgHeader>")
					 )
			{
				content=new String();
				content=getContent_R(_reduction.get(1).asReduction(), content);
				root.setText(translateContent(content));
				// START KGU#407 2017-06-20: Enh. #420 - comments already here
				String comment = this.retrieveComment(_reduction);
				if (comment != null) {
					root.getComment().add(StringList.explode(comment, "\n"));
				}
				// END KGU#407 2017-06-22
			}
			else if (
					ruleHead.equals("<ProcHeading>")
					||
					ruleHead.equals("<FuncHeading>")
					// START KGU#1073 2024-03-15: Issue #1084 Handle method definitions as well
					||
					ruleHead.equals("<MethHeading>")
					// END KGU#1073 2024-03-15
					 )
			{
				if (!this.withinInterface) {
					processRoutineHeading(_reduction);
				}
			}
			else if (
					 ruleHead.equals("<WhileStatement>")
					 )
			{
				content = new String();
				content = getContent_R(_reduction.get(1).asReduction(), content);
				// START KGU#8222020-03-09: Issue #835
				//While ele = new While(getKeyword("preWhile")+translateContent(content)+getKeyword("postWhile"));
				While ele = new While(getOptKeyword("preWhile", false, true)
						+ translateContent(content)
						+ getOptKeyword("postWhile", true, false));
				// END KGU#822 2020-03-09
				// START KGU#407 2017-06-20: Enh. #420 - comments already here
				this.equipWithSourceComment(ele, _reduction);
				// END KGU#407 2017-06-22
				_parentNode.addElement(ele);
				
				Reduction secReduc = _reduction.get(3).asReduction();
				buildNSD_R(secReduc,ele.q);
			}
			else if (
					 ruleHead.equals("<RepeatStatement>")
					 )
			{
				content = new String();
				content = getContent_R(_reduction.get(3).asReduction(), content);
				// START KGU#822 2020-03-09: Issue #835
				//Repeat ele = new Repeat(getKeyword("preRepeat")+translateContent(content)+getKeyword("postRepeat"));
				Repeat ele = new Repeat(getOptKeyword("preRepeat", false, true)
						+ translateContent(content)
						+ getOptKeyword("postRepeat", true, false));
				// END KGU#822 2020-03-09
				// START KGU#407 2017-06-20: Enh. #420 - comments already here
				this.equipWithSourceComment(ele, _reduction);
				// END KGU#407 2017-06-22
				_parentNode.addElement(ele);
				
				Reduction secReduc = _reduction.get(1).asReduction();
				buildNSD_R(secReduc,ele.q);
			}
			else if (
					 ruleHead.equals("<ForStatement>")
					 )
			{
				content = new String();
				content = getContent_R(_reduction.get(1).asReduction(), content);
				content += ":=";
				content = getContent_R(_reduction.get(3).asReduction(), content);
				content += " ";
				content += getKeyword("postFor");
				content += " ";
				content = getContent_R(_reduction.get(5).asReduction(), content);
				// START KGU#3 2016-05-02: Enh. #10 Token 4 contains the information whether it's to or downto
				if (getContent_R(_reduction.get(4).asReduction(), "").equals("downto"))
				{
					content += " " + getKeyword("stepFor") + " -1";
				}
				// END KGU#3 2016-05-02
				// START KGU 2016-05-02: This worked only if preFor ended with space
				//For ele = new For(preFor+updateContent(content));
				For ele = new For(getKeyword("preFor").trim() + " " + translateContent(content));
				// END KGU 2016-05-02
				// START KGU#407 2017-06-20: Enh. #420 - comments already here
				this.equipWithSourceComment(ele, _reduction);
				// END KGU#407 2017-06-22
				_parentNode.addElement(ele);
				
				// Get and convert the body
				Reduction secReduc = _reduction.get(7).asReduction();
				buildNSD_R(secReduc,ele.q);
			}
			else if (
					 ruleHead.equals("<IfStatement>")
					 )
			{
				content = new String();
				content = getContent_R(_reduction.get(1).asReduction(), content);
				
				// START KGU#822 2020-03-09: Issue #835
				//Alternative ele = new Alternative(getKeyword("preAlt") + translateContent(content)+getKeyword("postAlt"));
				Alternative ele = new Alternative(getOptKeyword("preAlt", false, true)
						+ translateContent(content)
						+ getOptKeyword("postAlt", true,false));
				// END KGU#822 2020-03-09
				// START KGU#407 2017-06-20: Enh. #420 - comments already here
				this.equipWithSourceComment(ele, _reduction);
				// END KGU#407 2017-06-22
				_parentNode.addElement(ele);
				
				Reduction secReduc = _reduction.get(3).asReduction();
				buildNSD_R(secReduc,ele.qTrue);
				if(_reduction.size()>=5)
				{
					secReduc = _reduction.get(5).asReduction();
					buildNSD_R(secReduc,ele.qFalse);
				}
			}
			else if (
					 ruleHead.equals("<CaseSelector>")
					 )
			{
				content = new String();
				content = getContent_R(_reduction.get(0).asReduction(), content);
				
				// sich am parent (CASE) dat nächst fräit Element
				boolean found = false;
				for (int i=0; i<((Case) _parentNode.parent).getText().count(); i++)
				{
					if (((Case) _parentNode.parent).getText().get(i).equals("??") && found==false)
					{
						((Case) _parentNode.parent).getText().set(i,content);
						found=true;
						
						Reduction secReduc = _reduction.get(2).asReduction();
						buildNSD_R(secReduc,(Subqueue) ((Case) _parentNode.parent).qs.get(i-1));
					}
				}

			}
			else if (
					 ruleHead.equals("<CaseStatement>")
					 )
			{
				content = "";
				// START KGU#822 2020-03-09: Issue #835
				//content = getKeyword("preCase") + getContent_R(_reduction.get(1).asReduction(), content) + getKeyword("postCase");
				content = getOptKeyword("preCase", false, true)
						+ getContent_R(_reduction.get(1).asReduction(), content)
						+ getOptKeyword("postCase", true, false);
				// END KGU#822 2020-03-09
				// am content steet elo hei den "test" dran
				
				// Wéivill Elementer sinn am CASE dran?
				Reduction sr = _reduction.get(3).asReduction();
				int j = 0;
				//System.out.println(sr.getParent().getText());  // <<<<<<<
				while (sr.getParent().getHead().toString().equals("<CaseList>"))
				{
					j++;
					content += "\n??";
					if (sr.size()>=1)
					{
						sr = sr.get(0).asReduction();
					}
					else
					{
						break;
					}
				}
				
				if ( j>0) 
				{
					j++;
					content += "\nelse";
				}

				Case ele = new Case(translateContent(content));
				//ele.setText(translateContent(content));	// What was this good for? It had just been done!
				// START KGU#407 2017-06-20: Enh. #420 - comments already here
				this.equipWithSourceComment(ele, _reduction);
				// END KGU#407 2017-06-22
				_parentNode.addElement(ele);

				// déi eenzel Elementer siche goen
				Reduction secReduc = _reduction.get(3).asReduction();
				buildNSD_R(secReduc,(Subqueue) ele.qs.get(0));
				// den "otherwise"
				secReduc = _reduction.get(4).asReduction();
				buildNSD_R(secReduc,(Subqueue) ele.qs.get(j-1));
				
				// cut off else, if possible
				if (((Subqueue) ele.qs.get(j-1)).getSize()==0)
				{
					ele.getText().set(ele.getText().count()-1,"%");
				}

				/*
				 content:='';
				 getContent_R(R.Tokens[1].Reduction,content);
				 // am content steet elo hei den "test" dran
				 
				 // Wéivill Elementer sinn am CASE dran?
				 sr:=r.Tokens[3].Reduction;
				 j:=0;
				 while(sr.ParentRule.Name='<CaseList>') do
				 begin
				   j:=j+1;
				   content:=content+#13+'??';
				   if(sr.TokenCount>=1) then sr:=sr.Tokens[0].Reduction
				   else break;
				 end;
				 
				 if(j>0) then
				 begin
				 inc(j);
				 content:=content+#13+'else';
				 
				 ele:=ParentNode.AddCase(updateContent(content));
				 (ele as BCase).setTextLines(content);
				 
				 // déi enzel Elementer siche goen
				 DrawNSD_R(R.Tokens[3].Reduction,((ele as BCase).qs[0] as BSubqueue));
				 // den "otherwise"
				 DrawNSD_R(R.Tokens[4].Reduction,((ele as BCase).qs[j-1] as BSubqueue));
				 end;
				 */
			}
			// START KGU#686 2019-03-22: Enh. #56
			else if (ruleHead.equals("<RaiseStmt>")) {
//				switch (ruleId) {
//				case RuleConstants.PROD_RAISESTMT_RAISE_SYNERROR:	// <RaiseStmt> ::= RAISE SynError
//				case RuleConstants.PROD_RAISESTMT_RAISE:			// <RaiseStmt> ::= RAISE <OptExceptInstance>
//				case RuleConstants.PROD_RAISESTMT_RAISE_AT:			// <RaiseStmt> ::= RAISE <OptExceptInstance> AT <Address>
//				}
				Jump raise = new Jump(this.getContent_R(_reduction.get(1).asReduction(), getKeywordOrDefault("preThrow", "throw")));
				_parentNode.addElement(this.equipWithSourceComment(raise, _reduction));
			}
			else if (
					ruleId == RuleConstants.PROD_TRYEXCEPTSTMT_TRY_EXCEPT_END
					||
					ruleId == RuleConstants.PROD_TRYFINALLYSTMT_TRY_FINALLY_END
					)
			{
				Reduction secReduc = _reduction.get(1).asReduction();	// try block
				Try ele = new Try("");
				buildNSD_R(secReduc, ele.qTry);
				secReduc = _reduction.get(3).asReduction();
				int secRuleId = secReduc.getParent().getTableIndex();
				switch (ruleId) {
				case RuleConstants.PROD_TRYEXCEPTSTMT_TRY_EXCEPT_END: {
					// <TryExceptStmt> ::= TRY <StmtList> EXCEPT <ExceptionBlock> <OptExceptionElse> END
					String exVarName = null;	// exception variable
					Reduction elseReduc = _reduction.get(4).asReduction();
					int elseRuleId = elseReduc.getParent().getTableIndex();
					if (secRuleId == RuleConstants.PROD_EXCEPTIONBLOCK_SEMI
							|| elseRuleId == RuleConstants.PROD_OPTEXCEPTIONELSE_ELSE) {
						// Create a case structure with minimum content
						// START KGU#822 2020-03-09: Issue #835
						//Case select = new Case((getKeyword("preCase") + " ??? " + getKeyword("postCase")).trim() + "\n!!\nelse");
						Case select = new Case((getOptKeyword("preCase", false, true) + "???" + getOptKeyword("postCase", true, false)).trim() + "\n!!\nelse");
						// END KGU#821 2020-03-09
						select.setComment("FIXME: This case selection just reflects the exception type discrimination\nIt won't be executable in this form.");
						while (secRuleId == RuleConstants.PROD_EXCEPTIONBLOCK_SEMI) {
							exVarName = insertExceptionBlock(select, secReduc.get(2).asReduction(), exVarName);
							secReduc = secReduc.get(0).asReduction();
							secRuleId = secReduc.getParent().getTableIndex();
						}
						exVarName = insertExceptionBlock(select, secReduc, exVarName);
						if (elseRuleId == RuleConstants.PROD_OPTEXCEPTIONELSE_ELSE) {
							// <OptExceptionElse> ::= ELSE <StmtList>
							buildNSD_R(elseReduc, select.qs.lastElement());
						}
						else {
							select.qs.lastElement().addElement(new Jump(getKeywordOrDefault("preThrow", "throw")));
						}
						ele.qCatch.addElement(select);
						if (exVarName != null) {
							select.getText().set(0, select.getText().get(0).replace("???", exVarName));
						}
					}
					else {
						// Can only be a single exception block
						if (secRuleId == RuleConstants.PROD_EXCEPTIONSTMT_ON_DO) {
							// <ExceptionStmt> ::= ON <Selector> DO <Statement>
							if (secReduc.get(1).getType() == SymbolType.NON_TERMINAL
									&& secReduc.get(1).asReduction().getParent().getTableIndex() == RuleConstants.PROD_SELECTOR_COLON) {
								exVarName = getContent_R(secReduc.get(1).asReduction().get(0).asReduction(), "");
							}
							buildNSD_R(secReduc.get(3).asReduction(), ele.qCatch);
						}
					}
					if (exVarName != null) {
						ele.setText(exVarName);
					}
				}
				break;
				case RuleConstants.PROD_TRYFINALLYSTMT_TRY_FINALLY_END: {
					// <TryFinallyStmt> ::= TRY <StmtList> FINALLY <StmtList> END
					// Possibly we may combine this Try element with an encapsulated one
					Try encapsulated = null;
					if (ele.qTry.getSize() == 1 && ele.qTry.getElement(0) instanceof Try && (encapsulated = (Try)ele.qTry.getElement(0)).qFinally.getSize() == 0) {
						ele = encapsulated;
					}
					else {
						ele.qCatch.addElement(new Jump(getKeywordOrDefault("preThrow", "throw")));
					}
					buildNSD_R(secReduc, ele.qFinally);					
				}
				}
				_parentNode.addElement(this.equipWithSourceComment(ele, _reduction));
			}
			// END KGU#686 2019-03-22
			else
			{
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

	// START KGU#1073 2024-03-14: Issue #1084
	/**
	 * Processes a the routine heading that is supposed to become the text of the
	 * current {@link root}.<br/>
	 * <b>Beware:</b> Shall not be invoked if the method header is just some kind of
	 * forward declaration!
	 * 
	 * @param _reduction - the current routine header reduction, expected are:
	 *    {@code <ProcHeading>}, {@code <FuncHeading>} and {@code <MethHeading>}
	 * @throws ParserCancelled
	 */
	private void processRoutineHeading(Reduction _reduction) throws ParserCancelled {
		String ruleHead = _reduction.getParent().getHead().toString();
		int ruleId = _reduction.getParent().getTableIndex();
		String content;
		content = "";
		// Get the routine name
		// START KGU#1073 2024-03-14: Issue #1084 Handle method definitions as well
		//content = getContent_R(_reduction.get(1).asReduction(), content);
		while (ruleId == RuleConstants.PROD_METHHEADING_SEMI
				|| ruleId == RuleConstants.PROD_PROCHEADING
				|| ruleId == RuleConstants.PROD_FUNCHEADING) {
			// <MethHeading> ::= <MethHeading> <CallDirectives> ';'
			// <ProcHeading> ::= <ProcHeading> <CallDirectives> <OptSemi>
			// <FuncHeading> ::= <FuncHeading> <CallDirectives> <OptSemi>
			// Just ignore the <CallDirectives> here (should ideally be put to the comments)
			_reduction = _reduction.get(0).asReduction();
			ruleHead = _reduction.getParent().getHead().toString();
			ruleId = _reduction.getParent().getTableIndex();
		}
		boolean isFunc = ruleHead.equals("<FuncHeading>")
				|| ruleId == RuleConstants.PROD_METHHEADING_CLASS_FUNCTION_DOT_COLON_SEMI
				|| ruleId == RuleConstants.PROD_METHHEADING_FUNCTION_DOT_COLON_SEMI
				|| ruleId == RuleConstants.PROD_METHHEADING_FUNCTION_DOT_SEMI;
		int ixName = 1;
		switch (ruleId) {
		case RuleConstants.PROD_METHHEADING_CLASS_FUNCTION_DOT_COLON_SEMI:
		case RuleConstants.PROD_METHHEADING_CLASS_PROCEDURE_DOT_SEMI:
			ixName++;
		case RuleConstants.PROD_METHHEADING_CONSTRUCTOR_DOT_SEMI:
		case RuleConstants.PROD_METHHEADING_DESTRUCTOR_DOT_SEMI:
		case RuleConstants.PROD_METHHEADING_FUNCTION_DOT_COLON_SEMI:
		case RuleConstants.PROD_METHHEADING_FUNCTION_DOT_SEMI:
		case RuleConstants.PROD_METHHEADING_PROCEDURE_DOT_SEMI:
			ixName += 2;
			String className = getContent_R(_reduction.get(ixName - 2).asReduction(), "");
			root.addToIncludeList(className);
			if (unitName != null) {
				root.setNamespace(unitName + "." + className);
			}
			else {
				root.setNamespace(className);
			}
		}
		content = getContent_R(_reduction.get(ixName).asReduction(), content);
		// END KGU#1073 2024-03-14
		
		// START KGU#991 2021-10-03: Issue #991 Cache the exact name spelling to coerce the result variable
		// START KGU#1073 2024-03-14: Issue #1084 Handle method definitions properly
		//if (ruleHead.equals("<FuncHeading>")) {
		if (isFunc) {
		// END KGU#1073 2024-03-14
			functionName = content.trim();
			// This will be cleared at the end of <FunctionDecl>
		}
		// END KGU#991 2021-10-03
		
		// Check the parameter list
		// START KGU#1073 2024-03-14: Issue #1084 Handle method definitions properly
		//Reduction secReduc = _reduction.get(2).asReduction();
		if (ruleId != RuleConstants.PROD_FUNCHEADING_FUNCTION_SEMI
				&& ruleId != RuleConstants.PROD_METHHEADING_FUNCTION_DOT_SEMI) {
			Reduction secReduc = _reduction.get(ixName + 1).asReduction();
		// END KGU#1073 2024-03-14
			if (secReduc.size() != 0)
			{
				// Append the parameter list
				content = getContent_R(secReduc, content);
			}
			// START KGU#821 2020-03-08 Issue #833 - parameterless routine must get parentheses
			else {
				// No parameter list -> ensure parentheses
				paramlessRoutineNames.addIfNew(content);
				content += "()";
			}
			// END KGU#821 2020-03-08
		
			// START KGU#1073 2024-03-14: Issue #1084
			//if (ruleHead.equals("<FuncHeading>"))
			//{
			//	secReduc = _reduction.get(4).asReduction();
			if (isFunc)
			{
				secReduc = _reduction.get(ixName + 3).asReduction();
			// END KGU#1073 2024-03-14
				if (secReduc.size() > 0)
				{
					content += ": ";
					content = getContent_R(secReduc,content);
				}
			}
			
		// START KGU#1073 2024-03-14: Issue #1084
		}
		else {
			// No parameter list -> ensure parentheses
			paramlessRoutineNames.addIfNew(content);
			content += "()";
			// No result type specification, either
			isFunc = false;
		}
		// END KGU#1073 2024-03-14
		
		content = content.replace(";", "; ");
		content = content.replace(";  ", "; ");
		root.setText(translateContent(content));
		root.setProgram(false);
		// END KGU#1073 2024-03-14
		// START KGU#194 2016-05-08: Bugfix #185 - be aware of unit context
		if (unitName != null)
		{
			// START KGU#376 2017-09-22: Enh. #389 - the unit will be an includable now
			//root.setComment("(UNIT " + unitName + ")");
			root.addToIncludeList(unitName + DEFAULT_GLOBAL_SUFFIX);
			// START KGU#586 2018-09-28: Bugfix #613 - register the established include relation
			this.includerList.add(root);
			// END KGU#586 2018-09-28
			// END KGU#376 2017-09-22
		}
		// END KGU#194 2016-05-08
		// START KGU#407 2017-06-20: Enh. #420 - comments already here
		String comment = this.retrieveComment(_reduction);
		// START KGU#860 2020-04-24: Bugfix #861/2 Precaution didn't work if newlines are contained
		//if (comment != null && !root.getComment().contains(comment)) {
		//	root.getComment().add(StringList.explode(comment, "\n"));
		//}
		if (comment != null && !comment.trim().isEmpty()) {
			StringList commentLines = StringList.explode(comment, "\n");
			if (root.getComment().indexOf(commentLines, 0, true) < 0) {
				root.getComment().add(commentLines);
			}
		}
		// END KGU#960 2020-06-20
		// END KGU#407 2017-06-22
	}
	
	/**
	 * Extracts the members of a class definition with name {@code _className} from
	 * the given {@code <ClassMemberList>} {@link Reduction} {@code _memberListRed},
	 * appending all public attribute declarations like record component declarations
	 * to the passed in {@link StringBuilder} {@code _sb}, which is used to form a
	 * record type definition.
	 * 
	 * @param _className - name of the class to be produced
	 * @param _ancestors - String containing the comma-separated list of parent classes
	 * @param _memberListRed - the root reduction of the member definitions
	 * @param _sb - a StringBuilder to gather the member definitions
	 * @param _comment - the extendable class comment
	 * @throws ParserCancelled 
	 */
	private void buildClassDefinition(String _className, String _ancestors, Reduction _memberListRed,
			StringBuilder _sb, StringList _comment) throws ParserCancelled {
		// Does it contain members or parent includes??
		boolean isNeeded = false;
		StringList parents = StringList.explode(_ancestors.replace(" ", ""), ",");
		Root classRoot = new Root();
		if (!_ancestors.isBlank()) {
			_comment.add("==== inherits from " + _ancestors);
		}
		classRoot.setInclude();
		String qualifier = "";
		boolean doesInclude = false;
		// FIXME not sensible...
		if (root.isInclude()) {
			String rootName = root.getMethodName();
			if (!rootName.equals(this.unitName) && !rootName.equals(unitName + DEFAULT_GLOBAL_SUFFIX)) {
				qualifier = root.getQualifiedName();
			}
			classRoot.addToIncludeList(root);
			isNeeded = true;
			doesInclude = true;
		}
		else if (root.includeList != null) {
			classRoot.includeList = new StringList();
			classRoot.includeList.addIfNew(root.includeList);
			doesInclude = true;
		}
		for (Root incl: this.includables) {
			String qualName = incl.getQualifiedName();
			String name = incl.getMethodName();
			if (parents.contains(name) || parents.contains(qualName)) {
				classRoot.addToIncludeList(incl);
				doesInclude = true;
			}
		}
		if (doesInclude) {
			this.includerList.add(classRoot);
		}
 		// Add temporary dummy loops in order to gather fields and method signatures
		classRoot.children.addElement(new Forever());
		classRoot.children.addElement(new Forever());
		//int ixFields = 0, ixMethods = 0;
		classRoot.setText(_className);	
		classRoot.setNamespace(qualifier);
		
		String accessLevel = "PUBLIC";
		boolean hasConstructor = false, hasDestructor = false;
		while (_memberListRed != null) {
			int ruleId = _memberListRed.getParent().getTableIndex();
			Reduction fieldListRed, methodListRed;
			if (ruleId != RuleConstants.PROD_CLASSMEMBERLIST) {
				fieldListRed = _memberListRed.get(2).asReduction();
				methodListRed = _memberListRed.get(3).asReduction();
				accessLevel = _memberListRed.get(1).asString().toUpperCase();
				_memberListRed = _memberListRed.get(0).asReduction();
			}
			else {
				fieldListRed = _memberListRed.get(0).asReduction();
				methodListRed = _memberListRed.get(1).asReduction();
				_memberListRed = null;
			}
			// Care for the field list <OptFieldList>
			StringList fieldDefs = new StringList();
			if (fieldListRed.size() > 0) {
				do {
					// Get a field specification and add it to fieldDefs and classRoot.children.get(0)
					Reduction fieldSpecRed = fieldListRed;
					if (fieldListRed.getParent().getTableIndex() == RuleConstants.PROD_FIELDLIST2) {
						fieldSpecRed = fieldListRed.get(1).asReduction();
						fieldListRed = fieldListRed.get(0).asReduction();
					}
					else {
						fieldListRed = null;
					}
					String fieldDef = getContent_R(fieldSpecRed, "").trim();
					if (fieldDef.endsWith(";")) {
						fieldDef = fieldDef.substring(0, fieldDef.length()-1);
					}
					if (accessLevel.equals("PUBLIC") || accessLevel.equals("PUBLISHED")) {
						fieldDefs.add(fieldDef + ";");
					}
					Element decl = equipWithSourceComment(new Instruction("var " + fieldDef), fieldSpecRed);
					decl.comment.add("FIELD in class " + classRoot.getQualifiedName());
					decl.comment.add(accessLevel);
					((Forever)classRoot.children.getElement(0)).getBody().insertElementAt(decl, 0);
				} while (fieldListRed != null);
			}
			
			// Care for the method list <OptClassMethodList>
			if (methodListRed.size() > 0) {
				do {
					// Get a method specification and add it as dummy call to classRoot.children.get(1)
					Reduction methSpecRed = methodListRed.get(0).asReduction();
					if (methodListRed.getParent().getTableIndex() == RuleConstants.PROD_CLASSMETHODLIST2) {
						methSpecRed = methodListRed.get(1).asReduction();
						methodListRed = methodListRed.get(0).asReduction();
					}
					else {
						methodListRed = null;
					}
					int ixSpec = 0;
					String resultType = "";
					String methKind = "";
					ruleId = methSpecRed.getParent().getTableIndex();
					switch (ruleId) {
					case RuleConstants.PROD_CLASSMETHODSPEC_CLASS:
					case RuleConstants.PROD_CLASSMETHODSPEC_CLASS2:
						methKind = "CLASS ";
						ixSpec ++;
					case RuleConstants.PROD_CLASSMETHODSPEC:
						methSpecRed = methSpecRed.get(ixSpec).asReduction();
						ruleId = methSpecRed.getParent().getTableIndex();
					case RuleConstants.PROD_PROCSPEC_PROCEDURE_SEMI:
					case RuleConstants.PROD_FUNCSPEC_FUNCTION_COLON_SEMI:
					case RuleConstants.PROD_CONSTRUCTORSPEC_CONSTRUCTOR_SEMI:
					case RuleConstants.PROD_DESTRUCTORSPEC_DESTRUCTOR_SEMI:
						if (ruleId == RuleConstants.PROD_FUNCSPEC_FUNCTION_COLON_SEMI) {
							resultType = getContent_R(methSpecRed.get(4).asReduction(), ": ");
						}
						methKind += "METHOD";
						String keyword = methSpecRed.get(0).asString().toUpperCase();
						String methName = getContent_R(methSpecRed.get(1).asReduction(), "");
						String params = getContent_R(methSpecRed.get(2).asReduction(), "");
						if (params.isBlank()) {
							paramlessRoutineNames.addIfNew(methName);
							params = "()";
						}
						if (keyword.equals("CONSTRUCTOR")) {
							hasConstructor = true;
							methKind = keyword;
						}
						else if (keyword.equals("DESTRUCTOR")) {
							hasDestructor = true;
							methKind = keyword;
						}
						String signature = methName + params + resultType;
						if (accessLevel.equals("PUBLIC")) {
							_comment.add(keyword + " " + signature);
						}
						Element decl = equipWithSourceComment(new Call(signature), methSpecRed);
						decl.comment.add(methKind + " for class " + classRoot.getQualifiedName());
						decl.comment.add(accessLevel);
						((Call)decl).isMethodDeclaration = true;
						((Forever)classRoot.children.getElement(1)).getBody().insertElementAt(decl, 0);
						break;
					case RuleConstants.PROD_CLASSMETHODSPEC2:
					case RuleConstants.PROD_CLASSMETHODSPEC3:
					}
				} while (methodListRed != null);
			}
//			ixFields = ((Forever)classRoot.children.getElement(0)).getBody().getSize();
//			ixMethods = ((Forever)classRoot.children.getElement(1)).getBody().getSize();
			for (int i = fieldDefs.count() - 1; i >= 0; i--) {
				_sb.append("\\n");
				_sb.append(fieldDefs.get(i));
			}
		}
		if (!hasConstructor) {
			this.paramlessRoutineNames.addIfNew("Create");
		}
		if (!hasDestructor) {
			this.paramlessRoutineNames.addIfNew("Destroy");
		}
		dissolveDummyContainers(classRoot);
		if (isNeeded || classRoot.children.getSize() > 0) {
			this.addRoot(classRoot);
			includables.add(classRoot);
			classRoot.setComment(_comment);
		}
	}

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
	// END KGU#1073 2024-03-14

	
	/**
	 * Inserts a new exception block from {@code _exStmtReduction} at first free
	 * branch position in {@link Case} element {@code _case}.<br/>
	 * If {@code _varName} hadn't been set (i.e. is null) and the exception block
	 * has an own variable name specified then this name will be returned, otherwise
	 * null.<br/>
	 * The exception type will be converted into a string and used as selector
	 * expression.
	 * @param _case - the exception selection
	 * @param _exStmtReduction - a reduction of type {@code <ExceptionStmt>}
	 * @param _varName - a possible former exception variable name
	 * @return then new exception variable name if {@code _varName} was null and the
	 * exception statement header declared a variable.
	 * @throws ParserCancelled if the user cancelled the import meanwhile
	 */
	private String insertExceptionBlock(Case _case, Reduction _exStmtReduction, String _varName) throws ParserCancelled {
		if (_exStmtReduction.size() == 0) return null;
		String exVarName = null;
		int ixStmtList = 0;
		String type = "";
		if (_exStmtReduction.getParent().getTableIndex() == RuleConstants.PROD_EXCEPTIONSTMT_ON_DO) {
			// <ExceptionStmt> ::= ON <Selector> DO <Statement>
			ixStmtList = 3;
			if (_exStmtReduction.get(1).getType() == SymbolType.NON_TERMINAL) {
				// The selector is composed
				int ixType = 0;	// Index of the type specification
				Reduction selReduc = _exStmtReduction.get(1).asReduction();
				if (selReduc.getParent().getTableIndex() == RuleConstants.PROD_SELECTOR_COLON) {
					exVarName = getContent_R(selReduc.get(0).asReduction(), "");
					ixType = 2;					
				}
				if (selReduc.get(ixType).getType() == SymbolType.NON_TERMINAL) {
					type = getContent_R(selReduc.get(ixType).asReduction(), "");
				}
				else {
					type = selReduc.get(ixType).asString();
				}
			}
			else {
				type = _exStmtReduction.get(1).asString();
			}
		}
		if (!(type.startsWith("\"") && type.endsWith("\"") || type.startsWith("'") && type.endsWith("'"))) {
			type = "\"" + type + "\"";
		}
		// Now extract the statements
		int branchNo = _case.getText().indexOf("!!") - 1;
		Subqueue branch;
		if (branchNo < 0) {
			_case.qs.insertElementAt(new Subqueue(), 0);
			branchNo = 0;
			(branch = _case.qs.get(0)).parent = _case;
			_case.getText().insert(type, 1);
		}
		else {
			_case.getText().set(branchNo+1, type);
			branch = _case.qs.get(branchNo);
		}
		if (exVarName != null && _varName != null && !exVarName.equals(_varName)) {
			branch.addElement(new Instruction(exVarName + " <- " + _varName));
		}
		buildNSD_R(_exStmtReduction.get(ixStmtList).asReduction(), branch);
		if (_varName != null) {
			exVarName = null;
		}
		return exVarName;
	}

	// START KGU#542 2018-07-11: Enh. #558 - provisional enum type support
	/**
	 * Returns the complete instruction element content for an enumeration type
	 * definition represented by the {@link Reduction} {@code redEnumL}
	 * @param redEnumL - the enumeration reduction to be analysed
	 * @param typeName - actually more than just the type name but "type &lt;typename&gt; = ".
	 * @return the text content for the {@link Instruction} element to be created
	 * @throws ParserCancelled in case of an interactive user abort
	 */
	private String importEnumType(Reduction redEnumType, String typeName) throws ParserCancelled {
		// <EnumType> ::= '(' <EnumId> ',' <EnumList> ')'
		StringList names = new StringList();
		StringList values = new StringList();
		// START KGU#387 2021-02-15: Issue #939 Handle the first item (1st argument had been redEnumL before)
		Reduction redEnum = redEnumType.get(1).asReduction();
		if (redEnum.getParent().getTableIndex() == RuleConstants.PROD_ENUMID_EQ) {
			names.add(this.getContent_R(redEnum.get(0).asReduction(), ""));
			values.add(this.getContent_R(redEnum.get(2).asReduction(), ""));
		}
		else {
			names.add(this.getContent_R(redEnum, ""));
			values.add("");
		}
		Reduction redEnumL = redEnumType.get(3).asReduction();
		// END KGU#387 2021-02-15 The following loop could now be a do-while loop but what why bother...
		while (redEnumL != null) {
			redEnum = redEnumL;
			if (redEnumL.getParent().getTableIndex() == RuleConstants.PROD_ENUMLIST_COMMA) {
				redEnum = redEnumL.get(2).asReduction();
				redEnumL = redEnumL.get(0).asReduction();
			}
			else {
				redEnumL = null;
			}
			if (redEnum.getParent().getTableIndex() == RuleConstants.PROD_ENUMID_EQ) {
				names.add(this.getContent_R(redEnum.get(0).asReduction(), ""));
				values.add(this.getContent_R(redEnum.get(2).asReduction(), ""));
			}
			else {
				names.add(this.getContent_R(redEnum, ""));
				values.add("");
			}
		}
		if (names.count() > 0) {
			names = names.reverse();
			values = values.reverse();
			//int val = 0;
			//String baseVal = "";
			for (int i = 0; i < names.count(); i++) {
				// START KGU#542 2019-11-19: Enh. #739 - true enum type import
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
				// END KGU#542 2019-11-19
			}
		}
		// START KGU#542 2019-11-18: Enh. #739
		//return names.concatenate("\n");
		String sepa = ", ";
		// FIXME: Tune the threshold if necessary
		if (names.count() > 10) {
			sepa = ",\\\n";
		}
		return typeName + "enum{" + names.concatenate(sepa) + "}";
		// END KGU#542 2019-11-19
	}
	// END KGU#542 2018-07-11

	private String translateContent(String _content)
	{
		/* Fucking Regex class -> No need to use it, because Java implements a ***working*** version!
		Regex r;
	
		r = new Regex(BString.breakup("write")+"[((](.*?)[))]",output+" $1"); _content=r.replaceAll(_content);
		r = new Regex(BString.breakup("writeln")+"[((](.*?)[))]",output+" $1"); _content=r.replaceAll(_content);
		r = new Regex(BString.breakup("writeln")+"(.*?)",output+" $1"); _content=r.replaceAll(_content);
		r = new Regex(BString.breakup("write")+"(.*?)",output+" $1"); _content=r.replaceAll(_content);
		r = new Regex(BString.breakup("read")+"[((](.*?)[))]",input+" $1"); _content=r.replaceAll(_content);
		r = new Regex(BString.breakup("readln")+"[((](.*?)[))]",input+" $1"); _content=r.replaceAll(_content);
		r = new Regex(BString.breakup("readln")+"(.*?)",input+" $1"); _content=r.replaceAll(_content);
		r = new Regex(BString.breakup("read")+"(.*?)",input+" $1"); _content=r.replaceAll(_content);*/

		String output = getKeyword("output");
		String input = getKeyword("input");
		_content = _content.replaceAll(BString.breakup("write", true)+"[((](.*?)[))]", output+" $1");
		_content = _content.replaceAll(BString.breakup("writeln", true)+"[((](.*?)[))]", output+" $1");
		_content = _content.replaceAll(BString.breakup("writeln", true)+"(.*?)", output+" $1");
		_content = _content.replaceAll(BString.breakup("write", true)+"(.*?)", output+" $1");
		_content = _content.replaceAll(BString.breakup("read", true)+"[((](.*?)[))]", input+" $1");
		_content = _content.replaceAll(BString.breakup("readln", true)+"[((](.*?)[))]", input+" $1");
		_content = _content.replaceAll(BString.breakup("readln", true)+"(.*?)", input+" $1");
		_content = _content.replaceAll(BString.breakup("read", true)+"(.*?)", input+" $1");
		
		//System.out.println(_content);
		
		/*
		 _content:=ReplaceEntities(_content);
		*/
		
		//_content = BString.replace(_content, ":="," \u2190 ");
		_content = _content.replace(":="," <- ");

		return _content.trim();
	}
	
	// START KGU#821 2020-03-08: Issue #833 - Identify and modify references to parameterless routines
	private String translateContentWithCalls(String _content)
	{
		String translated = this.translateContent(_content);
		StringList tokens = Element.splitLexically(translated, true);
		int posAsgn = tokens.indexOf("<-");
		for (int i = 0; i < paramlessRoutineNames.count(); i++) {
			String routineName = paramlessRoutineNames.get(i);
			int pos = posAsgn;
			while ((pos = tokens.indexOf(routineName, pos+1)) >= 0) {
				int parPos = pos;
				while (parPos < tokens.count() && tokens.get(++parPos).trim().isEmpty());
				if (parPos >= tokens.count() || !tokens.get(parPos).equals("(")) {
					tokens.set(pos, routineName + "()");
				}
			}
		}
		return tokens.concatenate();
	}
	// END KGU#821 2020-03-08
	
	protected String getContent_R(Reduction _reduction, String _content) throws ParserCancelled
	{
		// START KGU#537 2018-07-01: Enh. #553
		checkCancelled();
		// END KGU#537 2018-07-01
		for(int i = 0; i < _reduction.size(); i++)
		{
			switch (_reduction.get(i).getType()) 
			{
			case NON_TERMINAL:
				_content = getContent_R(_reduction.get(i).asReduction(), _content);	
				break;
			case CONTENT:
			{
				String tokenData = _reduction.get(i).asString();
				// START KGU 2016-05-08: Avoid keyword concatenation
				boolean tokenIsId = !tokenData.isEmpty() && Character.isJavaIdentifierStart(tokenData.charAt(0));
				// END KGU 2016-05-08
				// START KGU#843 2020-04-11: Bugfix #847: Executor does not cope with upper-case letters in operators
				//if (tokenData.trim().equalsIgnoreCase("mod") ||
				//		// START KGU#192 2016-05-02: There are more operators to be considered...
				//		tokenData.trim().equalsIgnoreCase("shl") ||
				//		tokenData.trim().equalsIgnoreCase("shr") ||
				//		// END KGU#192 2016-05-02
				//		tokenData.trim().equalsIgnoreCase("div"))
				String ruleHead = _reduction.getParent().getHead().toString();
				// START KGU#991 2021-10-03 Issue #991 Inside a function definition coerce result var spelling
				//if (tokenIsId && ruleHead.equals("<RefId>") && NAMES_TO_LOWER.contains(tokenData, false)) {
				//	tokenData = tokenData.toLowerCase();
				//}
				if (tokenIsId && ruleHead.equals("<RefId>")) {
					if (NAMES_TO_LOWER.contains(tokenData, false)) {
						tokenData = tokenData.toLowerCase();
					}
					else if (functionName != null && functionName.equalsIgnoreCase(tokenData)) {
						// Make sure that function name and result variable exactly match
						tokenData = functionName;
					}
				}
				// END KGU#991 2021-10-03
				if (OPR_RULE_HEADS.contains(ruleHead))
				// END KGU#843 2020-04-11
				{
					_content += " " + tokenData + " ";
				}
				// START KGU 2016-05-08: Avoid keyword concatenation
				else if (
						tokenIsId
						&&
						!_content.isEmpty()
						&&
						Character.isJavaIdentifierPart(_content.charAt(_content.length()-1))
						)
				{
					_content += " " + tokenData;
				}
				// END KGU 2016-05-08
				else
				{
					_content += tokenData;
				}
			}
			break;
			default:
				break;
			}
		}
		
		_content = BString.replaceInsensitive(_content,")and(",") and (");
		_content = BString.replaceInsensitive(_content,")or(",") or (");
		_content = BString.replaceInsensitive(_content,"array [","array [");
		_content = BString.replaceInsensitive(_content,"]of ","] of ");
		
		return _content;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.parsers.CodeParser#subclassUpdateRoot(lu.fisch.structorizer.elements.Root, java.lang.String)
	 */
	@Override
	protected boolean subclassUpdateRoot(Root root, String sourceFileName) throws ParserCancelled {
		// START KGU#821 2020-03-08: Issue #833 - Need a chance to get rid of superfluous diagrams
		boolean isSuperfluous = false;
		getLogger().config(root.getSignatureString(false, false));
		if (unitName != null && root.isProgram() && root.getMethodName().equals("???")) {
			root.setText(unitName + DEFAULT_GLOBAL_SUFFIX);
			root.setInclude(true);
			root.getComment().insert("(UNIT " + unitName + ")", 0);
		}
		// START KGU#821 2020-03-08: Issue #833
		if (!usesClauses.isEmpty()) {
			root.getComment().add(usesClauses);
		}
		// END KGU#821 2020-03-08
		// START KGU#586 2018-09-28: Bugfix #613 check the established inclusions for necessity
		if (root.isInclude() && root.children.getSize() == 0) {
			StringList comment = root.getComment();
			for (Root includer: this.includerList) {
				// Give the unit comment a chance to survive if there is only a single dependent
				if (includer.removeFromIncludeList(root)
						&& !comment.isEmpty() && this.includerList.size() == 1) {
					includer.comment.add(comment);
				}
			}
			isSuperfluous = true;
		}
		// END KGU#586 2018-09-28
		// START KGU#587 2018-09-28: Issue #614 - remove redundant result instruction
		else if (root.isSubroutine() && root.children.getSize() > 1) {
			int ixLast = root.children.getSize() - 1;
			Element lastEl = root.children.getElement(ixLast);
			if (lastEl.getClass().getSimpleName().equals("Instruction") &&
					lastEl.getText().getText().matches("\\s*" + root.getMethodName() + "\\s*<-\\s*" + BString.breakup("result", true) + "\\s*")) {
				// An end-standing instruction "<function-name> <- result" is redundant, so remove it.
				root.children.removeElement(ixLast);
			}
		}
		// END KGU#587 2018-09-28
		// START KGU#821 2020-03-08: Issue #833 see above
		return isSuperfluous;
		// END KGU#821 2020-03-08
	}

	
}

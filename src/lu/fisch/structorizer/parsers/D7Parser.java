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
 *      Bob Fisch       2008.01.06      First Issue
 *      Bob Fisch       2008.05.02      Added filter for (* ... *) comment filtering
 *      Kay Gürtzig     2015.10.20      New setting stepFor (KGU#3, to be made configurable!)
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
import lu.fisch.structorizer.elements.Case;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.elements.Repeat;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Subqueue;
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

import com.stevesoft.pat.Regex;

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

	// START KGU 2018-03-21
	protected final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
	protected org.slf4j.Logger getLogger()
	{
		return this.logger;
	}
	// END KGU 2018-03-21
	
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
			{"{", "}"}
		};
	}
	// END KGU#407 2017-09-30
	
	//---------------------- Grammar specification ---------------------------
	
	@Override
	protected final String getCompiledGrammar()
	{
		return "D7Grammar.cgt";
	}
	
	@Override
	protected final String getGrammarTableName()
	{
		return "ObjectPascal";
	}
	
	//------------------- Grammar table constants DON'T MODIFY! ---------------------------
	
	// START KGU#358 2017-03-29: Enh. #368 - rule constants inserted to facilitate build phase
	// Symbolic constants naming the table indices of the grammar rules
	@SuppressWarnings("unused")
    private interface RuleConstants
    {
       final int PROD_LCONST_DECLITERAL                           =   0;  // <LCONST> ::= DecLiteral
       final int PROD_ICONST_DECLITERAL                           =   1;  // <ICONST> ::= DecLiteral
       final int PROD_ICONST_HEXLITERAL                           =   2;  // <ICONST> ::= HexLiteral
       final int PROD_RCONST_FLOATLITERAL                         =   3;  // <RCONST> ::= FloatLiteral
       final int PROD_RCONST_REALLITERAL                          =   4;  // <RCONST> ::= RealLiteral
       final int PROD_SCONST_STRINGLITERAL                        =   5;  // <SCONST> ::= StringLiteral
       final int PROD_SCONST_CARET_ID                             =   6;  // <SCONST> ::= '^' id
       final int PROD_IDLIST_COMMA                                =   7;  // <IdList> ::= <IdList> ',' <RefId>
       final int PROD_IDLIST                                      =   8;  // <IdList> ::= <RefId>
       final int PROD_LABELID_ID                                  =   9;  // <LabelId> ::= id
       final int PROD_TYPEID_ID                                   =  10;  // <TypeId> ::= id
       final int PROD_TYPEID_NAME                                 =  11;  // <TypeId> ::= NAME
       final int PROD_TYPENAME                                    =  12;  // <TypeName> ::= <TypeId>
       final int PROD_TYPENAME_ID_DOT                             =  13;  // <TypeName> ::= id '.' <RefId>
       final int PROD_TYPELIST                                    =  14;  // <TypeList> ::= <TypeName>
       final int PROD_TYPELIST_COMMA                              =  15;  // <TypeList> ::= <TypeList> ',' <TypeName>
       final int PROD_REFID_ID                                    =  16;  // <RefId> ::= id
       final int PROD_REFID_AT                                    =  17;  // <RefId> ::= AT
       final int PROD_REFID_ON                                    =  18;  // <RefId> ::= ON
       final int PROD_REFID_READ                                  =  19;  // <RefId> ::= READ
       final int PROD_REFID_WRITE                                 =  20;  // <RefId> ::= WRITE
       final int PROD_REFID_READLN                                =  21;  // <RefId> ::= READLN
       final int PROD_REFID_WRITELN                               =  22;  // <RefId> ::= WRITELN
       final int PROD_REFID_NAME                                  =  23;  // <RefId> ::= NAME
       final int PROD_REFID_INDEX                                 =  24;  // <RefId> ::= INDEX
       final int PROD_REFID_VIRTUAL                               =  25;  // <RefId> ::= VIRTUAL
       final int PROD_REFID_ABSOLUTE                              =  26;  // <RefId> ::= ABSOLUTE
       final int PROD_REFID_MESSAGE                               =  27;  // <RefId> ::= MESSAGE
       final int PROD_REFID_DEFAULT                               =  28;  // <RefId> ::= DEFAULT
       final int PROD_REFID_OVERRIDE                              =  29;  // <RefId> ::= OVERRIDE
       final int PROD_REFID_ABSTRACT                              =  30;  // <RefId> ::= ABSTRACT
       final int PROD_REFID_DISPID                                =  31;  // <RefId> ::= DISPID
       final int PROD_REFID_REINTRODUCE                           =  32;  // <RefId> ::= REINTRODUCE
       final int PROD_REFID_REGISTER                              =  33;  // <RefId> ::= REGISTER
       final int PROD_REFID_PASCAL                                =  34;  // <RefId> ::= PASCAL
       final int PROD_REFID_CDECL                                 =  35;  // <RefId> ::= CDECL
       final int PROD_REFID_STDCALL                               =  36;  // <RefId> ::= STDCALL
       final int PROD_REFID_SAFECALL                              =  37;  // <RefId> ::= SAFECALL
       final int PROD_REFID_STRING                                =  38;  // <RefId> ::= STRING
       final int PROD_REFID_WIDESTRING                            =  39;  // <RefId> ::= WIDESTRING
       final int PROD_REFID_ANSISTRING                            =  40;  // <RefId> ::= ANSISTRING
       final int PROD_REFID_VARIANT                               =  41;  // <RefId> ::= VARIANT
       final int PROD_REFID_OLEVARIANT                            =  42;  // <RefId> ::= OLEVARIANT
       final int PROD_REFID_READONLY                              =  43;  // <RefId> ::= READONLY
       final int PROD_REFID_IMPLEMENTS                            =  44;  // <RefId> ::= IMPLEMENTS
       final int PROD_REFID_NODEFAULT                             =  45;  // <RefId> ::= NODEFAULT
       final int PROD_REFID_STORED                                =  46;  // <RefId> ::= STORED
       final int PROD_REFID_OVERLOAD                              =  47;  // <RefId> ::= OVERLOAD
       final int PROD_REFID_LOCAL                                 =  48;  // <RefId> ::= LOCAL
       final int PROD_REFID_VARARGS                               =  49;  // <RefId> ::= VARARGS
       final int PROD_REFID_FORWARD                               =  50;  // <RefId> ::= FORWARD
       final int PROD_REFID_CONTAINS                              =  51;  // <RefId> ::= CONTAINS
       final int PROD_REFID_PACKAGE                               =  52;  // <RefId> ::= PACKAGE
       final int PROD_REFID_REQUIRES                              =  53;  // <RefId> ::= REQUIRES
       final int PROD_REFID_LIBRARY                               =  54;  // <RefId> ::= LIBRARY
       final int PROD_REFID_IMPORT                                =  55;  // <RefId> ::= IMPORT
       final int PROD_REFID_EXPORT                                =  56;  // <RefId> ::= EXPORT
       final int PROD_REFID_PLATFORM                              =  57;  // <RefId> ::= PLATFORM
       final int PROD_REFID_DEPRECATED                            =  58;  // <RefId> ::= DEPRECATED
       final int PROD_REFID_EXTERNAL                              =  59;  // <RefId> ::= EXTERNAL
       final int PROD_FIELDDESIGNATOR                             =  60;  // <FieldDesignator> ::= <RefId>
       final int PROD_FIELDDESIGNATOR_DOT                         =  61;  // <FieldDesignator> ::= <FieldDesignator> '.' <RefId>
       final int PROD_OBJECTPASCAL                                =  62;  // <ObjectPascal> ::= <Program>
       final int PROD_OBJECTPASCAL2                               =  63;  // <ObjectPascal> ::= <Unit>
       final int PROD_OBJECTPASCAL3                               =  64;  // <ObjectPascal> ::= <Package>
       final int PROD_OBJECTPASCAL4                               =  65;  // <ObjectPascal> ::= <Library>
       final int PROD_PROGRAM_DOT                                 =  66;  // <Program> ::= <ProgHeader> <OptUsesSection> <Block> '.'
       final int PROD_PROGHEADER_PROGRAM_SEMI                     =  67;  // <ProgHeader> ::= PROGRAM <RefId> <OptProgParamList> ';'
       final int PROD_OPTPROGPARAMLIST_LPAREN_RPAREN              =  68;  // <OptProgParamList> ::= '(' <IdList> ')'
       final int PROD_OPTPROGPARAMLIST                            =  69;  // <OptProgParamList> ::= 
       final int PROD_UNIT_DOT                                    =  70;  // <Unit> ::= <UnitHeader> <InterfaceSection> <ImplementationSection> <InitSection> '.'
       final int PROD_UNITHEADER_UNIT_SEMI                        =  71;  // <UnitHeader> ::= UNIT <RefId> <OptPortDirectives> ';'
       final int PROD_PACKAGE_END_DOT                             =  72;  // <Package> ::= <PackageHeader> <OptRequiresClause> <OptContainsClause> END '.'
       final int PROD_PACKAGEHEADER_PACKAGE_SEMI                  =  73;  // <PackageHeader> ::= PACKAGE <RefId> ';'
       final int PROD_OPTREQUIRESCLAUSE_REQUIRES_SEMI             =  74;  // <OptRequiresClause> ::= REQUIRES <IdList> ';'
       final int PROD_OPTREQUIRESCLAUSE                           =  75;  // <OptRequiresClause> ::= 
       final int PROD_OPTCONTAINSCLAUSE_CONTAINS_SEMI             =  76;  // <OptContainsClause> ::= CONTAINS <IdList> ';'
       final int PROD_OPTCONTAINSCLAUSE                           =  77;  // <OptContainsClause> ::= 
       final int PROD_LIBRARYHEADER_LIBRARY_SEMI                  =  78;  // <LibraryHeader> ::= LIBRARY <RefId> ';'
       final int PROD_LIBRARY_DOT                                 =  79;  // <Library> ::= <LibraryHeader> <OptUsesSection> <Block> '.'
       final int PROD_INTERFACESECTION_INTERFACE                  =  80;  // <InterfaceSection> ::= INTERFACE <OptUsesSection> <OptExportDeclList>
       final int PROD_OPTUSESSECTION                              =  81;  // <OptUsesSection> ::= <UsesSection>
       final int PROD_OPTUSESSECTION2                             =  82;  // <OptUsesSection> ::= 
       final int PROD_USESCLAUSE_USES_SEMI                        =  83;  // <UsesClause> ::= USES <IdList> ';'
       final int PROD_USESCLAUSE_SYNERROR                         =  84;  // <UsesClause> ::= SynError
       final int PROD_USESSECTION                                 =  85;  // <UsesSection> ::= <UsesClause>
       final int PROD_USESSECTION2                                =  86;  // <UsesSection> ::= <UsesSection> <UsesClause>
       final int PROD_OPTEXPORTDECLLIST                           =  87;  // <OptExportDeclList> ::= <ExportDeclList>
       final int PROD_OPTEXPORTDECLLIST2                          =  88;  // <OptExportDeclList> ::= 
       final int PROD_EXPORTDECLLIST                              =  89;  // <ExportDeclList> ::= <ExportDeclItem>
       final int PROD_EXPORTDECLLIST2                             =  90;  // <ExportDeclList> ::= <ExportDeclList> <ExportDeclItem>
       final int PROD_EXPORTDECLITEM                              =  91;  // <ExportDeclItem> ::= <ConstSection>
       final int PROD_EXPORTDECLITEM2                             =  92;  // <ExportDeclItem> ::= <TypeSection>
       final int PROD_EXPORTDECLITEM3                             =  93;  // <ExportDeclItem> ::= <VarSection>
       final int PROD_EXPORTDECLITEM4                             =  94;  // <ExportDeclItem> ::= <CallSection>
       final int PROD_EXPORTDECLITEM_FORWARD_SEMI                 =  95;  // <ExportDeclItem> ::= <CallSection> FORWARD ';'
       final int PROD_CALLSECTION                                 =  96;  // <CallSection> ::= <ProcHeading>
       final int PROD_CALLSECTION2                                =  97;  // <CallSection> ::= <FuncHeading>
       final int PROD_IMPLEMENTATIONSECTION_IMPLEMENTATION        =  98;  // <ImplementationSection> ::= IMPLEMENTATION <OptUsesSection> <OptDeclSection> <OptExportBlock>
       final int PROD_INITSECTION_INITIALIZATION_END              =  99;  // <InitSection> ::= INITIALIZATION <StmtList> END
       final int PROD_INITSECTION_INITIALIZATION_FINALIZATION_END = 100;  // <InitSection> ::= INITIALIZATION <StmtList> FINALIZATION <StmtList> END
       final int PROD_INITSECTION                                 = 101;  // <InitSection> ::= <CompoundStmt>
       final int PROD_INITSECTION_END                             = 102;  // <InitSection> ::= END
       final int PROD_BLOCK                                       = 103;  // <Block> ::= <OptDeclSection> <OptExportBlock> <CompoundStmt> <OptExportBlock>
       final int PROD_OPTEXPORTBLOCK                              = 104;  // <OptExportBlock> ::= <ExportStmt>
       final int PROD_OPTEXPORTBLOCK2                             = 105;  // <OptExportBlock> ::= <OptExportBlock> <ExportStmt>
       final int PROD_OPTEXPORTBLOCK3                             = 106;  // <OptExportBlock> ::= 
       final int PROD_EXPORTSTMT_EXPORTS_SEMI                     = 107;  // <ExportStmt> ::= EXPORTS <ExportList> ';'
       final int PROD_EXPORTLIST                                  = 108;  // <ExportList> ::= <ExportItem>
       final int PROD_EXPORTLIST_COMMA                            = 109;  // <ExportList> ::= <ExportList> ',' <ExportItem>
       final int PROD_EXPORTITEM_ID                               = 110;  // <ExportItem> ::= id
       final int PROD_EXPORTITEM_ID_NAME_APOST_APOST              = 111;  // <ExportItem> ::= id NAME '' <ConstExpr> ''
       final int PROD_EXPORTITEM_ID_INDEX_APOST_APOST             = 112;  // <ExportItem> ::= id INDEX '' <ConstExpr> ''
       final int PROD_EXPORTITEM_NAME_APOST_APOST                 = 113;  // <ExportItem> ::= NAME '' <ConstExpr> ''
       final int PROD_EXPORTITEM_INDEX_APOST_APOST                = 114;  // <ExportItem> ::= INDEX '' <ConstExpr> ''
       final int PROD_OPTDECLSECTION                              = 115;  // <OptDeclSection> ::= <DeclSection>
       final int PROD_OPTDECLSECTION2                             = 116;  // <OptDeclSection> ::= 
       final int PROD_DECLSECTION                                 = 117;  // <DeclSection> ::= <DeclItem>
       final int PROD_DECLSECTION2                                = 118;  // <DeclSection> ::= <DeclSection> <DeclItem>
       final int PROD_DECLITEM                                    = 119;  // <DeclItem> ::= <LabelSection>
       final int PROD_DECLITEM2                                   = 120;  // <DeclItem> ::= <ConstSection>
       final int PROD_DECLITEM3                                   = 121;  // <DeclItem> ::= <TypeSection>
       final int PROD_DECLITEM4                                   = 122;  // <DeclItem> ::= <VarSection>
       final int PROD_DECLITEM5                                   = 123;  // <DeclItem> ::= <ProcedureDeclSection>
       final int PROD_DECLITEM_SYNERROR                           = 124;  // <DeclItem> ::= SynError
       final int PROD_LABELSECTION_LABEL_SEMI                     = 125;  // <LabelSection> ::= LABEL <LabelList> ';'
       final int PROD_LABELLIST                                   = 126;  // <LabelList> ::= <LabelId>
       final int PROD_LABELLIST_COMMA                             = 127;  // <LabelList> ::= <LabelList> ',' <LabelId>
       final int PROD_CONSTSECTION_CONST                          = 128;  // <ConstSection> ::= CONST <ConstantDeclList>
       final int PROD_CONSTSECTION_RESOURCESTRING                 = 129;  // <ConstSection> ::= RESOURCESTRING <ConstantDeclList>
       final int PROD_CONSTANTDECLLIST                            = 130;  // <ConstantDeclList> ::= <ConstantDecl>
       final int PROD_CONSTANTDECLLIST2                           = 131;  // <ConstantDeclList> ::= <ConstantDeclList> <ConstantDecl>
       final int PROD_CONSTANTDECL_EQ_SEMI                        = 132;  // <ConstantDecl> ::= <RefId> '=' <ConstExpr> <OptPortDirectives> ';'
       final int PROD_CONSTANTDECL_COLON_EQ_SEMI                  = 133;  // <ConstantDecl> ::= <RefId> ':' <Type> '=' <TypedConstant> <OptPortDirectives> ';'
       final int PROD_CONSTANTDECL_SYNERROR_SEMI                  = 134;  // <ConstantDecl> ::= SynError ';'
       final int PROD_TYPEDCONSTANT                               = 135;  // <TypedConstant> ::= <ConstExpr>
       final int PROD_TYPEDCONSTANT2                              = 136;  // <TypedConstant> ::= <ArrayConstant>
       final int PROD_TYPEDCONSTANT3                              = 137;  // <TypedConstant> ::= <RecordConstant>
       final int PROD_ARRAYCONSTANT_LPAREN_RPAREN                 = 138;  // <ArrayConstant> ::= '(' <TypedConstList> ')'
       final int PROD_RECORDCONSTANT_LPAREN_RPAREN                = 139;  // <RecordConstant> ::= '(' <RecordFieldConstList> ')'
       final int PROD_RECORDCONSTANT_LPAREN_SEMI_RPAREN           = 140;  // <RecordConstant> ::= '(' <RecordFieldConstList> ';' ')'
       final int PROD_RECORDCONSTANT_LPAREN_RPAREN2               = 141;  // <RecordConstant> ::= '(' ')'
       final int PROD_RECORDFIELDCONSTLIST                        = 142;  // <RecordFieldConstList> ::= <RecordFieldConstant>
       final int PROD_RECORDFIELDCONSTLIST_SEMI                   = 143;  // <RecordFieldConstList> ::= <RecordFieldConstList> ';' <RecordFieldConstant>
       final int PROD_RECORDFIELDCONSTANT_COLON                   = 144;  // <RecordFieldConstant> ::= <RefId> ':' <TypedConstant>
       final int PROD_TYPEDCONSTLIST                              = 145;  // <TypedConstList> ::= <TypedConstant>
       final int PROD_TYPEDCONSTLIST_COMMA                        = 146;  // <TypedConstList> ::= <TypedConstList> ',' <TypedConstant>
       final int PROD_TYPESECTION_TYPE                            = 147;  // <TypeSection> ::= TYPE <TypeDeclList>
       final int PROD_TYPEDECLLIST                                = 148;  // <TypeDeclList> ::= <TypeDecl>
       final int PROD_TYPEDECLLIST2                               = 149;  // <TypeDeclList> ::= <TypeDeclList> <TypeDecl>
       final int PROD_TYPEDECL_EQ                                 = 150;  // <TypeDecl> ::= <TypeId> '=' <TypeSpec>
       final int PROD_TYPEDECL_SYNERROR_SEMI                      = 151;  // <TypeDecl> ::= SynError ';'
       final int PROD_TYPESPEC_SEMI                               = 152;  // <TypeSpec> ::= <GenericType> ';'
       final int PROD_TYPESPEC_SEMI2                              = 153;  // <TypeSpec> ::= <RestrictedType> <OptPortDirectives> ';'
       final int PROD_TYPESPEC_SEMI3                              = 154;  // <TypeSpec> ::= <CallType> ';'
       final int PROD_TYPESPEC_SEMI_SEMI                          = 155;  // <TypeSpec> ::= <CallType> ';' <CallConventions> ';'
       final int PROD_TYPESPEC_SYNERROR_SEMI                      = 156;  // <TypeSpec> ::= SynError ';'
       final int PROD_TYPE                                        = 157;  // <Type> ::= <GenericType>
       final int PROD_TYPE2                                       = 158;  // <Type> ::= <CallType>
       final int PROD_TYPEREF                                     = 159;  // <TypeRef> ::= <TypeName>
       final int PROD_TYPEREF2                                    = 160;  // <TypeRef> ::= <StringType>
       final int PROD_TYPEREF3                                    = 161;  // <TypeRef> ::= <VariantType>
       final int PROD_GENERICTYPE                                 = 162;  // <GenericType> ::= <TypeName>
       final int PROD_GENERICTYPE2                                = 163;  // <GenericType> ::= <StringType>
       final int PROD_GENERICTYPE3                                = 164;  // <GenericType> ::= <VariantType>
       final int PROD_GENERICTYPE4                                = 165;  // <GenericType> ::= <SubrangeType>
       final int PROD_GENERICTYPE5                                = 166;  // <GenericType> ::= <EnumType>
       final int PROD_GENERICTYPE6                                = 167;  // <GenericType> ::= <StructType>
       final int PROD_GENERICTYPE7                                = 168;  // <GenericType> ::= <PointerType>
       final int PROD_GENERICTYPE8                                = 169;  // <GenericType> ::= <ClassRefType>
       final int PROD_GENERICTYPE9                                = 170;  // <GenericType> ::= <ClonedType>
       final int PROD_CLONEDTYPE_TYPE                             = 171;  // <ClonedType> ::= TYPE <TypeRef>
       final int PROD_STRINGTYPE_STRING                           = 172;  // <StringType> ::= STRING
       final int PROD_STRINGTYPE_ANSISTRING                       = 173;  // <StringType> ::= ANSISTRING
       final int PROD_STRINGTYPE_WIDESTRING                       = 174;  // <StringType> ::= WIDESTRING
       final int PROD_STRINGTYPE_STRING_LBRACKET_RBRACKET         = 175;  // <StringType> ::= STRING '[' <ConstExpr> ']'
       final int PROD_VARIANTTYPE_VARIANT                         = 176;  // <VariantType> ::= VARIANT
       final int PROD_VARIANTTYPE_OLEVARIANT                      = 177;  // <VariantType> ::= OLEVARIANT
       final int PROD_ORDINALTYPE                                 = 178;  // <OrdinalType> ::= <SubrangeType>
       final int PROD_ORDINALTYPE2                                = 179;  // <OrdinalType> ::= <EnumType>
       final int PROD_ORDINALTYPE3                                = 180;  // <OrdinalType> ::= <TypeName>
       final int PROD_SUBRANGETYPE_DOTDOT                         = 181;  // <SubrangeType> ::= <ConstOrdExpr> '..' <ConstOrdExpr>
       final int PROD_SUBRANGETYPE_SYNERROR                       = 182;  // <SubrangeType> ::= <ConstOrdExpr> SynError
       final int PROD_SUBRANGETYPE_LPAREN_RPAREN_DOTDOT           = 183;  // <SubrangeType> ::= '(' <RefId> ')' '..' <ConstOrdExpr>
       final int PROD_ENUMTYPE_LPAREN_RPAREN                      = 184;  // <EnumType> ::= '(' <EnumList> ')'
       final int PROD_ENUMTYPE_LPAREN_RPAREN2                     = 185;  // <EnumType> ::= '(' <RefId> ')'
       final int PROD_ENUMLIST                                    = 186;  // <EnumList> ::= <EnumId>
       final int PROD_ENUMLIST_COMMA                              = 187;  // <EnumList> ::= <EnumList> ',' <EnumId>
       final int PROD_ENUMID                                      = 188;  // <EnumId> ::= <RefId>
       final int PROD_ENUMID_EQ                                   = 189;  // <EnumId> ::= <RefId> '=' <ConstExpr>
       final int PROD_OPTPACKED_PACKED                            = 190;  // <OptPacked> ::= PACKED
       final int PROD_OPTPACKED                                   = 191;  // <OptPacked> ::= 
       final int PROD_STRUCTTYPE                                  = 192;  // <StructType> ::= <ArrayType>
       final int PROD_STRUCTTYPE2                                 = 193;  // <StructType> ::= <SetType>
       final int PROD_STRUCTTYPE3                                 = 194;  // <StructType> ::= <FileType>
       final int PROD_STRUCTTYPE4                                 = 195;  // <StructType> ::= <RecType>
       final int PROD_ARRAYTYPE_ARRAY_LBRACKET_RBRACKET_OF        = 196;  // <ArrayType> ::= <OptPacked> ARRAY '[' <OrdinalTypeList> ']' OF <Type>
       final int PROD_ARRAYTYPE_ARRAY_OF_CONST                    = 197;  // <ArrayType> ::= <OptPacked> ARRAY OF CONST
       final int PROD_ARRAYTYPE_ARRAY_OF                          = 198;  // <ArrayType> ::= <OptPacked> ARRAY OF <Type>
       final int PROD_ORDINALTYPELIST                             = 199;  // <OrdinalTypeList> ::= <OrdinalType>
       final int PROD_ORDINALTYPELIST_COMMA                       = 200;  // <OrdinalTypeList> ::= <OrdinalTypeList> ',' <OrdinalType>
       final int PROD_RECTYPE_RECORD_END                          = 201;  // <RecType> ::= <OptPacked> RECORD <RecFieldList> END <OptPortDirectives>
       final int PROD_RECFIELDLIST                                = 202;  // <RecFieldList> ::= 
       final int PROD_RECFIELDLIST2                               = 203;  // <RecFieldList> ::= <RecField1>
       final int PROD_RECFIELDLIST3                               = 204;  // <RecFieldList> ::= <RecField2>
       final int PROD_RECFIELDLIST_SEMI                           = 205;  // <RecFieldList> ::= <RecField1> ';' <RecFieldList>
       final int PROD_RECFIELDLIST_SEMI2                          = 206;  // <RecFieldList> ::= <RecField2> ';' <RecFieldList>
       final int PROD_RECFIELDLIST_SEMI3                          = 207;  // <RecFieldList> ::= <RecField2> ';' <CallConvention>
       final int PROD_RECFIELDLIST_SEMI_SEMI                      = 208;  // <RecFieldList> ::= <RecField2> ';' <CallConvention> ';' <RecFieldList>
       final int PROD_RECFIELDLIST_CASE_OF                        = 209;  // <RecFieldList> ::= CASE <Selector> OF <RecVariantList>
       final int PROD_RECVARIANTLIST                              = 210;  // <RecVariantList> ::= 
       final int PROD_RECVARIANTLIST2                             = 211;  // <RecVariantList> ::= <RecVariant>
       final int PROD_RECVARIANTLIST_SEMI                         = 212;  // <RecVariantList> ::= <RecVariant> ';' <RecVariantList>
       final int PROD_RECFIELD1_COLON                             = 213;  // <RecField1> ::= <IdList> ':' <GenericType> <OptPortDirectives>
       final int PROD_RECFIELD2_COLON                             = 214;  // <RecField2> ::= <IdList> ':' <CallType>
       final int PROD_RECVARIANT_COLON_LPAREN_RPAREN              = 215;  // <RecVariant> ::= <ConstExprList> ':' '(' <RecFieldList> ')'
       final int PROD_SELECTOR_COLON                              = 216;  // <Selector> ::= <RefId> ':' <TypeName>
       final int PROD_SELECTOR                                    = 217;  // <Selector> ::= <TypeName>
       final int PROD_SETTYPE_SET_OF                              = 218;  // <SetType> ::= <OptPacked> SET OF <OrdinalType>
       final int PROD_FILETYPE_FILE_OF                            = 219;  // <FileType> ::= <OptPacked> FILE OF <TypeRef>
       final int PROD_FILETYPE_FILE                               = 220;  // <FileType> ::= FILE
       final int PROD_POINTERTYPE_CARET                           = 221;  // <PointerType> ::= '^' <TypeRef>
       final int PROD_CALLTYPE_PROCEDURE                          = 222;  // <CallType> ::= PROCEDURE <OptFormalParms> <OptCallConventions>
       final int PROD_CALLTYPE_PROCEDURE_OF_OBJECT                = 223;  // <CallType> ::= PROCEDURE <OptFormalParms> OF OBJECT <OptCallConventions>
       final int PROD_CALLTYPE_FUNCTION_COLON                     = 224;  // <CallType> ::= FUNCTION <OptFormalParms> ':' <ResultType> <OptCallConventions>
       final int PROD_CALLTYPE_FUNCTION_COLON_OF_OBJECT           = 225;  // <CallType> ::= FUNCTION <OptFormalParms> ':' <ResultType> OF OBJECT <OptCallConventions>
       final int PROD_RESTRICTEDTYPE                              = 226;  // <RestrictedType> ::= <ObjectType>
       final int PROD_RESTRICTEDTYPE2                             = 227;  // <RestrictedType> ::= <ClassType>
       final int PROD_RESTRICTEDTYPE3                             = 228;  // <RestrictedType> ::= <InterfaceType>
       final int PROD_OBJECTTYPE_OBJECT_END                       = 229;  // <ObjectType> ::= <OptPacked> OBJECT <OptObjectHeritage> <ObjectMemberList> END
       final int PROD_CLASSTYPE_CLASS_END                         = 230;  // <ClassType> ::= CLASS <OptClassHeritage> <ClassMemberList> END
       final int PROD_CLASSTYPE_CLASS                             = 231;  // <ClassType> ::= CLASS <OptClassHeritage>
       final int PROD_CLASSREFTYPE_CLASS_OF                       = 232;  // <ClassRefType> ::= CLASS OF <TypeName>
       final int PROD_INTERFACETYPE_INTERFACE_END                 = 233;  // <InterfaceType> ::= INTERFACE <OptClassHeritage> <OptClassGUID> <OptClassMethodList> END
       final int PROD_INTERFACETYPE_DISPINTERFACE_END             = 234;  // <InterfaceType> ::= DISPINTERFACE <OptClassHeritage> <OptClassGUID> <OptClassMethodList> END
       final int PROD_INTERFACETYPE_INTERFACE                     = 235;  // <InterfaceType> ::= INTERFACE
       final int PROD_INTERFACETYPE_DISPINTERFACE                 = 236;  // <InterfaceType> ::= DISPINTERFACE
       final int PROD_OPTOBJECTHERITAGE_LPAREN_RPAREN             = 237;  // <OptObjectHeritage> ::= '(' <TypeName> ')'
       final int PROD_OPTOBJECTHERITAGE                           = 238;  // <OptObjectHeritage> ::= 
       final int PROD_OPTCLASSHERITAGE_LPAREN_RPAREN              = 239;  // <OptClassHeritage> ::= '(' <TypeList> ')'
       final int PROD_OPTCLASSHERITAGE                            = 240;  // <OptClassHeritage> ::= 
       final int PROD_OPTCLASSGUID_LBRACKET_RBRACKET              = 241;  // <OptClassGUID> ::= '[' <ConstStrExpr> ']'
       final int PROD_OPTCLASSGUID                                = 242;  // <OptClassGUID> ::= 
       final int PROD_OBJECTMEMBERLIST                            = 243;  // <ObjectMemberList> ::= <OptFieldList> <OptObjectMethodList>
       final int PROD_OBJECTMEMBERLIST_PUBLIC                     = 244;  // <ObjectMemberList> ::= <ObjectMemberList> PUBLIC <OptFieldList> <OptObjectMethodList>
       final int PROD_OBJECTMEMBERLIST_PRIVATE                    = 245;  // <ObjectMemberList> ::= <ObjectMemberList> PRIVATE <OptFieldList> <OptObjectMethodList>
       final int PROD_OBJECTMEMBERLIST_PROTECTED                  = 246;  // <ObjectMemberList> ::= <ObjectMemberList> PROTECTED <OptFieldList> <OptObjectMethodList>
       final int PROD_CLASSMEMBERLIST                             = 247;  // <ClassMemberList> ::= <OptFieldList> <OptClassMethodList>
       final int PROD_CLASSMEMBERLIST_PUBLIC                      = 248;  // <ClassMemberList> ::= <ClassMemberList> PUBLIC <OptFieldList> <OptClassMethodList>
       final int PROD_CLASSMEMBERLIST_PRIVATE                     = 249;  // <ClassMemberList> ::= <ClassMemberList> PRIVATE <OptFieldList> <OptClassMethodList>
       final int PROD_CLASSMEMBERLIST_PROTECTED                   = 250;  // <ClassMemberList> ::= <ClassMemberList> PROTECTED <OptFieldList> <OptClassMethodList>
       final int PROD_CLASSMEMBERLIST_PUBLISHED                   = 251;  // <ClassMemberList> ::= <ClassMemberList> PUBLISHED <OptFieldList> <OptClassMethodList>
       final int PROD_OPTFIELDLIST                                = 252;  // <OptFieldList> ::= <FieldList>
       final int PROD_OPTFIELDLIST2                               = 253;  // <OptFieldList> ::= 
       final int PROD_OPTOBJECTMETHODLIST                         = 254;  // <OptObjectMethodList> ::= <ObjectMethodList>
       final int PROD_OPTOBJECTMETHODLIST2                        = 255;  // <OptObjectMethodList> ::= 
       final int PROD_OPTCLASSMETHODLIST                          = 256;  // <OptClassMethodList> ::= <ClassMethodList>
       final int PROD_OPTCLASSMETHODLIST2                         = 257;  // <OptClassMethodList> ::= 
       final int PROD_FIELDLIST                                   = 258;  // <FieldList> ::= <FieldSpec>
       final int PROD_FIELDLIST2                                  = 259;  // <FieldList> ::= <FieldList> <FieldSpec>
       final int PROD_OBJECTMETHODLIST                            = 260;  // <ObjectMethodList> ::= <ObjectMethodSpec>
       final int PROD_OBJECTMETHODLIST2                           = 261;  // <ObjectMethodList> ::= <ObjectMethodList> <ObjectMethodSpec>
       final int PROD_CLASSMETHODLIST                             = 262;  // <ClassMethodList> ::= <ClassMethodSpec>
       final int PROD_CLASSMETHODLIST2                            = 263;  // <ClassMethodList> ::= <ClassMethodList> <ClassMethodSpec>
       final int PROD_FIELDSPEC_COLON_SEMI                        = 264;  // <FieldSpec> ::= <IdList> ':' <Type> <OptPortDirectives> ';'
       final int PROD_FIELDSPEC_SYNERROR_SEMI                     = 265;  // <FieldSpec> ::= SynError ';'
       final int PROD_OBJECTMETHODSPEC                            = 266;  // <ObjectMethodSpec> ::= <MethodSpec> <OptMethodDirectives>
       final int PROD_OBJECTMETHODSPEC2                           = 267;  // <ObjectMethodSpec> ::= <PropertySpec> <OptPropertyDirectives>
       final int PROD_OBJECTMETHODSPEC_SYNERROR                   = 268;  // <ObjectMethodSpec> ::= SynError
       final int PROD_CLASSMETHODSPEC                             = 269;  // <ClassMethodSpec> ::= <MethodSpec> <OptMethodDirectives>
       final int PROD_CLASSMETHODSPEC2                            = 270;  // <ClassMethodSpec> ::= <ResolutionSpec> <OptMethodDirectives>
       final int PROD_CLASSMETHODSPEC_CLASS                       = 271;  // <ClassMethodSpec> ::= CLASS <ProcSpec> <OptMethodDirectives>
       final int PROD_CLASSMETHODSPEC_CLASS2                      = 272;  // <ClassMethodSpec> ::= CLASS <FuncSpec> <OptMethodDirectives>
       final int PROD_CLASSMETHODSPEC3                            = 273;  // <ClassMethodSpec> ::= <PropertySpec> <OptPropertyDirectives>
       final int PROD_CLASSMETHODSPEC_SYNERROR                    = 274;  // <ClassMethodSpec> ::= SynError
       final int PROD_METHODSPEC                                  = 275;  // <MethodSpec> ::= <ConstructorSpec>
       final int PROD_METHODSPEC2                                 = 276;  // <MethodSpec> ::= <DestructorSpec>
       final int PROD_METHODSPEC3                                 = 277;  // <MethodSpec> ::= <ProcSpec>
       final int PROD_METHODSPEC4                                 = 278;  // <MethodSpec> ::= <FuncSpec>
       final int PROD_CONSTRUCTORSPEC_CONSTRUCTOR_SEMI            = 279;  // <ConstructorSpec> ::= CONSTRUCTOR <RefId> <OptFormalParms> ';'
       final int PROD_DESTRUCTORSPEC_DESTRUCTOR_SEMI              = 280;  // <DestructorSpec> ::= DESTRUCTOR <RefId> <OptFormalParms> ';'
       final int PROD_PROCSPEC_PROCEDURE_SEMI                     = 281;  // <ProcSpec> ::= PROCEDURE <RefId> <OptFormalParms> <OptCallConventions> ';'
       final int PROD_FUNCSPEC_FUNCTION_COLON_SEMI                = 282;  // <FuncSpec> ::= FUNCTION <RefId> <OptFormalParms> ':' <ResultType> <OptCallConventions> ';'
       final int PROD_RESOLUTIONSPEC_PROCEDURE_DOT_EQ_SEMI        = 283;  // <ResolutionSpec> ::= PROCEDURE <RefId> '.' <RefId> '=' <RefId> ';'
       final int PROD_RESOLUTIONSPEC_FUNCTION_DOT_EQ_SEMI         = 284;  // <ResolutionSpec> ::= FUNCTION <RefId> '.' <RefId> '=' <RefId> ';'
       final int PROD_PROPERTYSPEC_PROPERTY_SEMI                  = 285;  // <PropertySpec> ::= PROPERTY <PropertyDecl> <OptPropSpecifiers> ';'
       final int PROD_PROPERTYDECL_COLON                          = 286;  // <PropertyDecl> ::= <RefId> ':' <TypeRef>
       final int PROD_PROPERTYDECL_LBRACKET_RBRACKET_COLON        = 287;  // <PropertyDecl> ::= <RefId> '[' <IndexList> ']' ':' <TypeRef>
       final int PROD_PROPERTYDECL                                = 288;  // <PropertyDecl> ::= <RefId>
       final int PROD_INDEXLIST                                   = 289;  // <IndexList> ::= <IndexDecl>
       final int PROD_INDEXLIST_SEMI                              = 290;  // <IndexList> ::= <IndexList> ';' <IndexDecl>
       final int PROD_INDEXDECL                                   = 291;  // <IndexDecl> ::= <IdDecl>
       final int PROD_INDEXDECL_CONST                             = 292;  // <IndexDecl> ::= CONST <IdDecl>
       final int PROD_IDDECL_COLON                                = 293;  // <IdDecl> ::= <IdList> ':' <Type>
       final int PROD_OPTPROPSPECIFIERS                           = 294;  // <OptPropSpecifiers> ::= <PropertySpecifiers>
       final int PROD_OPTPROPSPECIFIERS2                          = 295;  // <OptPropSpecifiers> ::= 
       final int PROD_PROPERTYSPECIFIERS                          = 296;  // <PropertySpecifiers> ::= <PropertySpecifier>
       final int PROD_PROPERTYSPECIFIERS2                         = 297;  // <PropertySpecifiers> ::= <PropertySpecifiers> <PropertySpecifier>
       final int PROD_PROPERTYSPECIFIER_INDEX                     = 298;  // <PropertySpecifier> ::= INDEX <ConstExpr>
       final int PROD_PROPERTYSPECIFIER_READ                      = 299;  // <PropertySpecifier> ::= READ <FieldDesignator>
       final int PROD_PROPERTYSPECIFIER_WRITE                     = 300;  // <PropertySpecifier> ::= WRITE <FieldDesignator>
       final int PROD_PROPERTYSPECIFIER_STORED                    = 301;  // <PropertySpecifier> ::= STORED <FieldDesignator>
       final int PROD_PROPERTYSPECIFIER_DEFAULT                   = 302;  // <PropertySpecifier> ::= DEFAULT <ConstExpr>
       final int PROD_PROPERTYSPECIFIER_NODEFAULT                 = 303;  // <PropertySpecifier> ::= NODEFAULT
       final int PROD_PROPERTYSPECIFIER_WRITEONLY                 = 304;  // <PropertySpecifier> ::= WRITEONLY
       final int PROD_PROPERTYSPECIFIER_READONLY                  = 305;  // <PropertySpecifier> ::= READONLY
       final int PROD_PROPERTYSPECIFIER_DISPID                    = 306;  // <PropertySpecifier> ::= DISPID <ConstExpr>
       final int PROD_PROPERTYSPECIFIER                           = 307;  // <PropertySpecifier> ::= <ImplementsSpecifier>
       final int PROD_IMPLEMENTSSPECIFIER_IMPLEMENTS              = 308;  // <ImplementsSpecifier> ::= IMPLEMENTS <TypeRef>
       final int PROD_IMPLEMENTSSPECIFIER_COMMA                   = 309;  // <ImplementsSpecifier> ::= <ImplementsSpecifier> ',' <TypeRef>
       final int PROD_VARSECTION_VAR                              = 310;  // <VarSection> ::= VAR <VarDeclList>
       final int PROD_VARSECTION_THREADVAR                        = 311;  // <VarSection> ::= THREADVAR <ThreadVarDeclList>
       final int PROD_VARDECLLIST                                 = 312;  // <VarDeclList> ::= <VarDecl>
       final int PROD_VARDECLLIST2                                = 313;  // <VarDeclList> ::= <VarDeclList> <VarDecl>
       final int PROD_VARDECL_COLON_SEMI                          = 314;  // <VarDecl> ::= <IdList> ':' <Type> <OptAbsoluteClause> <OptPortDirectives> ';'
       final int PROD_VARDECL_COLON_EQ_SEMI                       = 315;  // <VarDecl> ::= <IdList> ':' <Type> '=' <TypedConstant> <OptPortDirectives> ';'
       final int PROD_VARDECL_COLON                               = 316;  // <VarDecl> ::= <IdList> ':' <TypeSpec>
       final int PROD_VARDECL_SYNERROR_SEMI                       = 317;  // <VarDecl> ::= SynError ';'
       final int PROD_THREADVARDECLLIST                           = 318;  // <ThreadVarDeclList> ::= <ThreadVarDecl>
       final int PROD_THREADVARDECLLIST2                          = 319;  // <ThreadVarDeclList> ::= <ThreadVarDeclList> <ThreadVarDecl>
       final int PROD_THREADVARDECL_COLON                         = 320;  // <ThreadVarDecl> ::= <IdList> ':' <TypeSpec>
       final int PROD_THREADVARDECL_SYNERROR_SEMI                 = 321;  // <ThreadVarDecl> ::= SynError ';'
       final int PROD_OPTABSOLUTECLAUSE_ABSOLUTE                  = 322;  // <OptAbsoluteClause> ::= ABSOLUTE <RefId>
       final int PROD_OPTABSOLUTECLAUSE                           = 323;  // <OptAbsoluteClause> ::= 
       final int PROD_CONSTEXPR                                   = 324;  // <ConstExpr> ::= <Expr>
       final int PROD_CONSTORDEXPR                                = 325;  // <ConstOrdExpr> ::= <AddExpr>
       final int PROD_CONSTSTREXPR                                = 326;  // <ConstStrExpr> ::= <AddExpr>
       final int PROD_EXPR                                        = 327;  // <Expr> ::= <AddExpr>
       final int PROD_EXPR2                                       = 328;  // <Expr> ::= <AddExpr> <RelOp> <AddExpr>
       final int PROD_EXPR_SYNERROR                               = 329;  // <Expr> ::= SynError
       final int PROD_ADDEXPR                                     = 330;  // <AddExpr> ::= <MulExpr>
       final int PROD_ADDEXPR2                                    = 331;  // <AddExpr> ::= <AddExpr> <AddOp> <MulExpr>
       final int PROD_MULEXPR                                     = 332;  // <MulExpr> ::= <Factor>
       final int PROD_MULEXPR2                                    = 333;  // <MulExpr> ::= <MulExpr> <MulOp> <Factor>
       final int PROD_FACTOR_NIL                                  = 334;  // <Factor> ::= NIL
       final int PROD_FACTOR                                      = 335;  // <Factor> ::= <ICONST>
       final int PROD_FACTOR2                                     = 336;  // <Factor> ::= <RCONST>
       final int PROD_FACTOR3                                     = 337;  // <Factor> ::= <SCONST>
       final int PROD_FACTOR4                                     = 338;  // <Factor> ::= <Designator>
       final int PROD_FACTOR5                                     = 339;  // <Factor> ::= <SetConstructor>
       final int PROD_FACTOR_AT                                   = 340;  // <Factor> ::= '@' <Designator>
       final int PROD_FACTOR_AT_AT                                = 341;  // <Factor> ::= '@' '@' <Designator>
       final int PROD_FACTOR_LPAREN_RPAREN                        = 342;  // <Factor> ::= '(' <Expr> ')'
       final int PROD_FACTOR_LPAREN_RPAREN_CARET                  = 343;  // <Factor> ::= '(' <Expr> ')' '^'
       final int PROD_FACTOR_PLUS                                 = 344;  // <Factor> ::= '+' <Factor>
       final int PROD_FACTOR_MINUS                                = 345;  // <Factor> ::= '-' <Factor>
       final int PROD_FACTOR_NOT                                  = 346;  // <Factor> ::= NOT <Factor>
       final int PROD_DESIGNATOR                                  = 347;  // <Designator> ::= <FieldDesignator>
       final int PROD_DESIGNATOR_DOT                              = 348;  // <Designator> ::= <Designator> '.' <FieldDesignator>
       final int PROD_DESIGNATOR_CARET                            = 349;  // <Designator> ::= <Designator> '^'
       final int PROD_DESIGNATOR_LBRACKET_RBRACKET                = 350;  // <Designator> ::= <Designator> '[' <ExprList> ']'
       final int PROD_DESIGNATOR_LPAREN_RPAREN                    = 351;  // <Designator> ::= <Designator> '(' <ExprList> ')'
       final int PROD_DESIGNATOR_LPAREN_RPAREN2                   = 352;  // <Designator> ::= <Designator> '(' ')'
       final int PROD_DESIGNATOR_AS                               = 353;  // <Designator> ::= <Designator> AS <TypeRef>
       final int PROD_DESIGNATOR_LPAREN_RPAREN3                   = 354;  // <Designator> ::= '(' <Designator> ')'
       final int PROD_DESIGNATOR_INHERITED                        = 355;  // <Designator> ::= INHERITED <Designator>
       final int PROD_ASNOP_COLONEQ                               = 356;  // <AsnOp> ::= ':='
       final int PROD_ASNOP_PLUSEQ                                = 357;  // <AsnOp> ::= '+='
       final int PROD_ASNOP_MINUSEQ                               = 358;  // <AsnOp> ::= '-='
       final int PROD_ASNOP_TIMESEQ                               = 359;  // <AsnOp> ::= '*='
       final int PROD_ASNOP_DIVEQ                                 = 360;  // <AsnOp> ::= '/='
       final int PROD_RELOP_EQ                                    = 361;  // <RelOp> ::= '='
       final int PROD_RELOP_GT                                    = 362;  // <RelOp> ::= '>'
       final int PROD_RELOP_LT                                    = 363;  // <RelOp> ::= '<'
       final int PROD_RELOP_LTEQ                                  = 364;  // <RelOp> ::= '<='
       final int PROD_RELOP_GTEQ                                  = 365;  // <RelOp> ::= '>='
       final int PROD_RELOP_LTGT                                  = 366;  // <RelOp> ::= '<>'
       final int PROD_RELOP_IN                                    = 367;  // <RelOp> ::= IN
       final int PROD_RELOP_IS                                    = 368;  // <RelOp> ::= IS
       final int PROD_RELOP_AS                                    = 369;  // <RelOp> ::= AS
       final int PROD_ADDOP_PLUS                                  = 370;  // <AddOp> ::= '+'
       final int PROD_ADDOP_MINUS                                 = 371;  // <AddOp> ::= '-'
       final int PROD_ADDOP_OR                                    = 372;  // <AddOp> ::= OR
       final int PROD_ADDOP_XOR                                   = 373;  // <AddOp> ::= XOR
       final int PROD_MULOP_TIMES                                 = 374;  // <MulOp> ::= '*'
       final int PROD_MULOP_DIV                                   = 375;  // <MulOp> ::= '/'
       final int PROD_MULOP_DIV2                                  = 376;  // <MulOp> ::= DIV
       final int PROD_MULOP_MOD                                   = 377;  // <MulOp> ::= MOD
       final int PROD_MULOP_AND                                   = 378;  // <MulOp> ::= AND
       final int PROD_MULOP_SHL                                   = 379;  // <MulOp> ::= SHL
       final int PROD_MULOP_SHR                                   = 380;  // <MulOp> ::= SHR
       final int PROD_SETCONSTRUCTOR_LBRACKET_RBRACKET            = 381;  // <SetConstructor> ::= '[' <SetElementList> ']'
       final int PROD_SETCONSTRUCTOR_LBRACKET_RBRACKET2           = 382;  // <SetConstructor> ::= '[' ']'
       final int PROD_SETELEMENTLIST                              = 383;  // <SetElementList> ::= <SetElement>
       final int PROD_SETELEMENTLIST_COMMA                        = 384;  // <SetElementList> ::= <SetElementList> ',' <SetElement>
       final int PROD_SETELEMENT                                  = 385;  // <SetElement> ::= <Expr>
       final int PROD_SETELEMENT_DOTDOT                           = 386;  // <SetElement> ::= <Expr> '..' <Expr>
       final int PROD_EXPRLIST                                    = 387;  // <ExprList> ::= <Expr>
       final int PROD_EXPRLIST_COMMA                              = 388;  // <ExprList> ::= <ExprList> ',' <Expr>
       final int PROD_FMTEXPR                                     = 389;  // <FmtExpr> ::= <Expr>
       final int PROD_FMTEXPR_COLON                               = 390;  // <FmtExpr> ::= <Expr> ':' <Expr>
       final int PROD_FMTEXPR_COLON_COLON                         = 391;  // <FmtExpr> ::= <Expr> ':' <Expr> ':' <Expr>
       final int PROD_FMTEXPRLIST                                 = 392;  // <FmtExprList> ::= <FmtExpr>
       final int PROD_FMTEXPRLIST_COMMA                           = 393;  // <FmtExprList> ::= <FmtExprList> ',' <FmtExpr>
       final int PROD_CONSTEXPRLIST                               = 394;  // <ConstExprList> ::= <ConstExpr>
       final int PROD_CONSTEXPRLIST_COMMA                         = 395;  // <ConstExprList> ::= <ConstExprList> ',' <ConstExpr>
       final int PROD_STMTLIST                                    = 396;  // <StmtList> ::= <Statement>
       final int PROD_STMTLIST_SEMI                               = 397;  // <StmtList> ::= <StmtList> ';' <Statement>
       final int PROD_STATEMENT                                   = 398;  // <Statement> ::= <Label> <Statement>
       final int PROD_STATEMENT2                                  = 399;  // <Statement> ::= <AssignmentStmt>
       final int PROD_STATEMENT3                                  = 400;  // <Statement> ::= <CallStmt>
       final int PROD_STATEMENT4                                  = 401;  // <Statement> ::= <GotoStatement>
       final int PROD_STATEMENT5                                  = 402;  // <Statement> ::= <CompoundStmt>
       final int PROD_STATEMENT6                                  = 403;  // <Statement> ::= <IfStatement>
       final int PROD_STATEMENT7                                  = 404;  // <Statement> ::= <CaseStatement>
       final int PROD_STATEMENT8                                  = 405;  // <Statement> ::= <ForStatement>
       final int PROD_STATEMENT9                                  = 406;  // <Statement> ::= <WhileStatement>
       final int PROD_STATEMENT10                                 = 407;  // <Statement> ::= <RepeatStatement>
       final int PROD_STATEMENT11                                 = 408;  // <Statement> ::= <WithStatement>
       final int PROD_STATEMENT12                                 = 409;  // <Statement> ::= <TryFinallyStmt>
       final int PROD_STATEMENT13                                 = 410;  // <Statement> ::= <TryExceptStmt>
       final int PROD_STATEMENT14                                 = 411;  // <Statement> ::= <RaiseStmt>
       final int PROD_STATEMENT15                                 = 412;  // <Statement> ::= <AssemblerStmt>
       final int PROD_STATEMENT_SYNERROR                          = 413;  // <Statement> ::= SynError
       final int PROD_STATEMENT16                                 = 414;  // <Statement> ::= 
       final int PROD_LABEL_COLON                                 = 415;  // <Label> ::= <LCONST> ':'
       final int PROD_LABEL_COLON2                                = 416;  // <Label> ::= <LabelId> ':'
       final int PROD_ASSIGNMENTSTMT                              = 417;  // <AssignmentStmt> ::= <Designator> <AsnOp> <Expr>
       final int PROD_ASSIGNMENTSTMT_AT_COLONEQ                   = 418;  // <AssignmentStmt> ::= '@' <RefId> ':=' <Factor>
       final int PROD_CALLSTMT                                    = 419;  // <CallStmt> ::= <Designator>
       final int PROD_CALLSTMT_WRITE_LPAREN_RPAREN                = 420;  // <CallStmt> ::= WRITE '(' <FmtExprList> ')'
       final int PROD_CALLSTMT_WRITELN_LPAREN_RPAREN              = 421;  // <CallStmt> ::= WRITELN '(' <FmtExprList> ')'
       final int PROD_CALLSTMT_INHERITED                          = 422;  // <CallStmt> ::= INHERITED
       final int PROD_GOTOSTATEMENT_GOTO                          = 423;  // <GotoStatement> ::= GOTO <LCONST>
       final int PROD_GOTOSTATEMENT_GOTO2                         = 424;  // <GotoStatement> ::= GOTO <RefId>
       final int PROD_COMPOUNDSTMT_BEGIN_END                      = 425;  // <CompoundStmt> ::= BEGIN <StmtList> END
       final int PROD_IFSTATEMENT_IF_THEN_ELSE                    = 426;  // <IfStatement> ::= IF <Expr> THEN <Statement> ELSE <Statement>
       final int PROD_IFSTATEMENT_IF_THEN                         = 427;  // <IfStatement> ::= IF <Expr> THEN <Statement>
       final int PROD_IFSTATEMENT_IF_SYNERROR_THEN                = 428;  // <IfStatement> ::= IF SynError THEN <Statement>
       final int PROD_CASESTATEMENT_CASE_OF_END                   = 429;  // <CaseStatement> ::= CASE <Expr> OF <CaseList> <OtherWise> END
       final int PROD_FORSTATEMENT_FOR_COLONEQ_DO                 = 430;  // <ForStatement> ::= FOR <RefId> ':=' <Expr> <Dir> <Expr> DO <Statement>
       final int PROD_DIR_TO                                      = 431;  // <Dir> ::= TO
       final int PROD_DIR_DOWNTO                                  = 432;  // <Dir> ::= DOWNTO
       final int PROD_WHILESTATEMENT_WHILE_DO                     = 433;  // <WhileStatement> ::= WHILE <Expr> DO <Statement>
       final int PROD_WITHSTATEMENT_WITH_DO                       = 434;  // <WithStatement> ::= WITH <DesignatorList> DO <Statement>
       final int PROD_DESIGNATORLIST                              = 435;  // <DesignatorList> ::= <Designator>
       final int PROD_DESIGNATORLIST_COMMA                        = 436;  // <DesignatorList> ::= <DesignatorList> ',' <Designator>
       final int PROD_REPEATSTATEMENT_REPEAT_UNTIL                = 437;  // <RepeatStatement> ::= REPEAT <StmtList> UNTIL <Expr>
       final int PROD_ASSEMBLERSTMT_ASM_END                       = 438;  // <AssemblerStmt> ::= ASM <AsmLanguage> END
       final int PROD_OTHERWISE_OTHERWISE                         = 439;  // <OtherWise> ::= OTHERWISE <StmtList>
       final int PROD_OTHERWISE_ELSE                              = 440;  // <OtherWise> ::= ELSE <StmtList>
       final int PROD_OTHERWISE                                   = 441;  // <OtherWise> ::= 
       final int PROD_CASELIST                                    = 442;  // <CaseList> ::= <CaseSelector>
       final int PROD_CASELIST_SEMI                               = 443;  // <CaseList> ::= <CaseList> ';' <CaseSelector>
       final int PROD_CASELIST_SEMI2                              = 444;  // <CaseList> ::= <CaseList> ';'
       final int PROD_CASESELECTOR_COLON                          = 445;  // <CaseSelector> ::= <CaseLabels> ':' <Statement>
       final int PROD_CASELABELS                                  = 446;  // <CaseLabels> ::= <CaseLabel>
       final int PROD_CASELABELS_COMMA                            = 447;  // <CaseLabels> ::= <CaseLabels> ',' <CaseLabel>
       final int PROD_CASELABEL                                   = 448;  // <CaseLabel> ::= <ConstExpr>
       final int PROD_CASELABEL_DOTDOT                            = 449;  // <CaseLabel> ::= <ConstExpr> '..' <ConstExpr>
       final int PROD_RAISESTMT_RAISE_SYNERROR                    = 450;  // <RaiseStmt> ::= RAISE SynError
       final int PROD_RAISESTMT_RAISE                             = 451;  // <RaiseStmt> ::= RAISE <OptExceptInstance>
       final int PROD_RAISESTMT_RAISE_AT                          = 452;  // <RaiseStmt> ::= RAISE <OptExceptInstance> AT <Address>
       final int PROD_TRYFINALLYSTMT_TRY_FINALLY_END              = 453;  // <TryFinallyStmt> ::= TRY <StmtList> FINALLY <StmtList> END
       final int PROD_TRYEXCEPTSTMT_TRY_EXCEPT_END                = 454;  // <TryExceptStmt> ::= TRY <StmtList> EXCEPT <ExceptionBlock> <OptExceptionElse> END
       final int PROD_EXCEPTIONBLOCK                              = 455;  // <ExceptionBlock> ::= <ExceptionStmt>
       final int PROD_EXCEPTIONBLOCK_SEMI                         = 456;  // <ExceptionBlock> ::= <ExceptionBlock> ';' <ExceptionStmt>
       final int PROD_EXCEPTIONSTMT_ON_DO                         = 457;  // <ExceptionStmt> ::= ON <Selector> DO <Statement>
       final int PROD_EXCEPTIONSTMT                               = 458;  // <ExceptionStmt> ::= <Statement>
       final int PROD_OPTEXCEPTIONELSE_ELSE                       = 459;  // <OptExceptionElse> ::= ELSE <StmtList>
       final int PROD_OPTEXCEPTIONELSE                            = 460;  // <OptExceptionElse> ::= 
       final int PROD_OPTEXCEPTINSTANCE                           = 461;  // <OptExceptInstance> ::= <Designator>
       final int PROD_OPTEXCEPTINSTANCE2                          = 462;  // <OptExceptInstance> ::= 
       final int PROD_ADDRESS                                     = 463;  // <Address> ::= <Designator>
       final int PROD_OPTSEMI_SEMI                                = 464;  // <OptSemi> ::= ';'
       final int PROD_OPTSEMI                                     = 465;  // <OptSemi> ::= 
       final int PROD_PROCEDUREDECLSECTION                        = 466;  // <ProcedureDeclSection> ::= <ProcedureDecl>
       final int PROD_PROCEDUREDECLSECTION2                       = 467;  // <ProcedureDeclSection> ::= <FunctionDecl>
       final int PROD_PROCEDUREDECLSECTION3                       = 468;  // <ProcedureDeclSection> ::= <MethodDecl>
       final int PROD_PROCEDUREDECL                               = 469;  // <ProcedureDecl> ::= <ProcHeading> <CallBody> <OptSemi>
       final int PROD_FUNCTIONDECL                                = 470;  // <FunctionDecl> ::= <FuncHeading> <CallBody> <OptSemi>
       final int PROD_METHODDECL                                  = 471;  // <MethodDecl> ::= <MethHeading> <CallBody> <OptSemi>
       final int PROD_PROCHEADING_PROCEDURE_SEMI                  = 472;  // <ProcHeading> ::= PROCEDURE <RefId> <OptFormalParms> <OptCallSpecifiers> ';'
       final int PROD_PROCHEADING                                 = 473;  // <ProcHeading> ::= <ProcHeading> <CallDirectives> <OptSemi>
       final int PROD_FUNCHEADING_FUNCTION_COLON_SEMI             = 474;  // <FuncHeading> ::= FUNCTION <RefId> <OptFormalParms> ':' <ResultType> <OptCallSpecifiers> ';'
       final int PROD_FUNCHEADING_FUNCTION_SEMI                   = 475;  // <FuncHeading> ::= FUNCTION <RefId> ';'
       final int PROD_FUNCHEADING                                 = 476;  // <FuncHeading> ::= <FuncHeading> <CallDirectives> <OptSemi>
       final int PROD_METHHEADING_PROCEDURE_DOT_SEMI              = 477;  // <MethHeading> ::= PROCEDURE <RefId> '.' <RefId> <OptFormalParms> <OptCallSpecifiers> ';'
       final int PROD_METHHEADING_FUNCTION_DOT_COLON_SEMI         = 478;  // <MethHeading> ::= FUNCTION <RefId> '.' <RefId> <OptFormalParms> ':' <ResultType> <OptCallSpecifiers> ';'
       final int PROD_METHHEADING_FUNCTION_DOT_SEMI               = 479;  // <MethHeading> ::= FUNCTION <RefId> '.' <RefId> ';'
       final int PROD_METHHEADING_CONSTRUCTOR_DOT_SEMI            = 480;  // <MethHeading> ::= CONSTRUCTOR <RefId> '.' <RefId> <OptFormalParms> <OptCallSpecifiers> ';'
       final int PROD_METHHEADING_DESTRUCTOR_DOT_SEMI             = 481;  // <MethHeading> ::= DESTRUCTOR <RefId> '.' <RefId> <OptFormalParms> <OptCallSpecifiers> ';'
       final int PROD_METHHEADING_CLASS_PROCEDURE_DOT_SEMI        = 482;  // <MethHeading> ::= CLASS PROCEDURE <RefId> '.' <RefId> <OptFormalParms> <OptCallSpecifiers> ';'
       final int PROD_METHHEADING_CLASS_FUNCTION_DOT_COLON_SEMI   = 483;  // <MethHeading> ::= CLASS FUNCTION <RefId> '.' <RefId> <OptFormalParms> ':' <ResultType> <OptCallSpecifiers> ';'
       final int PROD_METHHEADING_SEMI                            = 484;  // <MethHeading> ::= <MethHeading> <CallDirectives> ';'
       final int PROD_RESULTTYPE                                  = 485;  // <ResultType> ::= <TypeRef>
       final int PROD_OPTFORMALPARMS_LPAREN_RPAREN                = 486;  // <OptFormalParms> ::= '(' <FormalParmList> ')'
       final int PROD_OPTFORMALPARMS_LPAREN_RPAREN2               = 487;  // <OptFormalParms> ::= '(' ')'
       final int PROD_OPTFORMALPARMS                              = 488;  // <OptFormalParms> ::= 
       final int PROD_FORMALPARMLIST                              = 489;  // <FormalParmList> ::= <FormalParm>
       final int PROD_FORMALPARMLIST_SEMI                         = 490;  // <FormalParmList> ::= <FormalParmList> ';' <FormalParm>
       final int PROD_FORMALPARM                                  = 491;  // <FormalParm> ::= <Parameter>
       final int PROD_FORMALPARM_CONST                            = 492;  // <FormalParm> ::= CONST <Parameter>
       final int PROD_FORMALPARM_VAR                              = 493;  // <FormalParm> ::= VAR <Parameter>
       final int PROD_FORMALPARM_OUT                              = 494;  // <FormalParm> ::= OUT <Parameter>
       final int PROD_PARAMETER                                   = 495;  // <Parameter> ::= <IdList>
       final int PROD_PARAMETER_COLON                             = 496;  // <Parameter> ::= <IdList> ':' <ParmType>
       final int PROD_PARAMETER_COLON_EQ                          = 497;  // <Parameter> ::= <IdList> ':' <TypeRef> '=' <ConstExpr>
       final int PROD_PARMTYPE                                    = 498;  // <ParmType> ::= <TypeRef>
       final int PROD_PARMTYPE_ARRAY_OF                           = 499;  // <ParmType> ::= ARRAY OF <TypeRef>
       final int PROD_PARMTYPE_ARRAY_OF_CONST                     = 500;  // <ParmType> ::= ARRAY OF CONST
       final int PROD_PARMTYPE_FILE                               = 501;  // <ParmType> ::= FILE
       final int PROD_CALLBODY                                    = 502;  // <CallBody> ::= <OptDeclSection> <CompoundStmt>
       final int PROD_CALLBODY2                                   = 503;  // <CallBody> ::= <OptDeclSection> <AssemblerStmt>
       final int PROD_CALLBODY3                                   = 504;  // <CallBody> ::= <ExternalDeclaration>
       final int PROD_CALLBODY_FORWARD                            = 505;  // <CallBody> ::= FORWARD
       final int PROD_OPTPORTDIRECTIVES                           = 506;  // <OptPortDirectives> ::= <PortDirectives>
       final int PROD_OPTPORTDIRECTIVES2                          = 507;  // <OptPortDirectives> ::= 
       final int PROD_PORTDIRECTIVES                              = 508;  // <PortDirectives> ::= <PortDirective>
       final int PROD_PORTDIRECTIVES2                             = 509;  // <PortDirectives> ::= <PortDirectives> <PortDirective>
       final int PROD_PORTDIRECTIVE_PLATFORM                      = 510;  // <PortDirective> ::= PLATFORM
       final int PROD_PORTDIRECTIVE_PLATFORM_EQ                   = 511;  // <PortDirective> ::= PLATFORM '=' <ConstExpr>
       final int PROD_PORTDIRECTIVE_DEPRECATED                    = 512;  // <PortDirective> ::= DEPRECATED
       final int PROD_PORTDIRECTIVE_LIBRARY                       = 513;  // <PortDirective> ::= LIBRARY
       final int PROD_OPTMETHODDIRECTIVES                         = 514;  // <OptMethodDirectives> ::= <MethodDirectives>
       final int PROD_OPTMETHODDIRECTIVES_SEMI                    = 515;  // <OptMethodDirectives> ::= <OptMethodDirectives> <PortDirective> ';'
       final int PROD_OPTMETHODDIRECTIVES2                        = 516;  // <OptMethodDirectives> ::= 
       final int PROD_METHODDIRECTIVES_SEMI                       = 517;  // <MethodDirectives> ::= <MethodDirective> ';'
       final int PROD_METHODDIRECTIVES_SEMI2                      = 518;  // <MethodDirectives> ::= <MethodDirectives> <MethodDirective> ';'
       final int PROD_METHODDIRECTIVE_VIRTUAL                     = 519;  // <MethodDirective> ::= VIRTUAL
       final int PROD_METHODDIRECTIVE_VIRTUAL2                    = 520;  // <MethodDirective> ::= VIRTUAL <ConstExpr>
       final int PROD_METHODDIRECTIVE_DYNAMIC                     = 521;  // <MethodDirective> ::= DYNAMIC
       final int PROD_METHODDIRECTIVE_OVERRIDE                    = 522;  // <MethodDirective> ::= OVERRIDE
       final int PROD_METHODDIRECTIVE_ABSTRACT                    = 523;  // <MethodDirective> ::= ABSTRACT
       final int PROD_METHODDIRECTIVE_MESSAGE                     = 524;  // <MethodDirective> ::= MESSAGE <ConstExpr>
       final int PROD_METHODDIRECTIVE_OVERLOAD                    = 525;  // <MethodDirective> ::= OVERLOAD
       final int PROD_METHODDIRECTIVE_REINTRODUCE                 = 526;  // <MethodDirective> ::= REINTRODUCE
       final int PROD_METHODDIRECTIVE_DISPID                      = 527;  // <MethodDirective> ::= DISPID <ConstExpr>
       final int PROD_METHODDIRECTIVE                             = 528;  // <MethodDirective> ::= <CallConvention>
       final int PROD_OPTPROPERTYDIRECTIVES_SEMI                  = 529;  // <OptPropertyDirectives> ::= <PropertyDirective> ';'
       final int PROD_OPTPROPERTYDIRECTIVES_SEMI2                 = 530;  // <OptPropertyDirectives> ::= <OptPropertyDirectives> <PortDirective> ';'
       final int PROD_OPTPROPERTYDIRECTIVES                       = 531;  // <OptPropertyDirectives> ::= 
       final int PROD_PROPERTYDIRECTIVE_DEFAULT                   = 532;  // <PropertyDirective> ::= DEFAULT
       final int PROD_EXTERNALDECLARATION_EXTERNAL                = 533;  // <ExternalDeclaration> ::= EXTERNAL
       final int PROD_EXTERNALDECLARATION_EXTERNAL2               = 534;  // <ExternalDeclaration> ::= EXTERNAL <ConstStrExpr>
       final int PROD_EXTERNALDECLARATION_EXTERNAL_NAME           = 535;  // <ExternalDeclaration> ::= EXTERNAL <ConstStrExpr> NAME <ConstStrExpr>
       final int PROD_CALLDIRECTIVES                              = 536;  // <CallDirectives> ::= <CallDirective>
       final int PROD_CALLDIRECTIVES2                             = 537;  // <CallDirectives> ::= <CallDirectives> <CallDirective>
       final int PROD_CALLDIRECTIVE                               = 538;  // <CallDirective> ::= <CallConvention>
       final int PROD_CALLDIRECTIVE2                              = 539;  // <CallDirective> ::= <CallObsolete>
       final int PROD_CALLDIRECTIVE3                              = 540;  // <CallDirective> ::= <PortDirective>
       final int PROD_CALLDIRECTIVE_VARARGS                       = 541;  // <CallDirective> ::= VARARGS
       final int PROD_CALLDIRECTIVE_LOCAL                         = 542;  // <CallDirective> ::= LOCAL
       final int PROD_CALLDIRECTIVE4                              = 543;  // <CallDirective> ::= <SCONST>
       final int PROD_CALLDIRECTIVE_OVERLOAD                      = 544;  // <CallDirective> ::= OVERLOAD
       final int PROD_OPTCALLSPECIFIERS                           = 545;  // <OptCallSpecifiers> ::= <CallSpecifier>
       final int PROD_OPTCALLSPECIFIERS2                          = 546;  // <OptCallSpecifiers> ::= <OptCallSpecifiers> <CallSpecifier>
       final int PROD_OPTCALLSPECIFIERS3                          = 547;  // <OptCallSpecifiers> ::= 
       final int PROD_CALLSPECIFIER                               = 548;  // <CallSpecifier> ::= <CallConvention>
       final int PROD_CALLSPECIFIER2                              = 549;  // <CallSpecifier> ::= <CallObsolete>
       final int PROD_CALLCONVENTIONS                             = 550;  // <CallConventions> ::= <CallConvention>
       final int PROD_CALLCONVENTIONS2                            = 551;  // <CallConventions> ::= <CallConventions> <CallConvention>
       final int PROD_OPTCALLCONVENTIONS                          = 552;  // <OptCallConventions> ::= <CallConvention>
       final int PROD_OPTCALLCONVENTIONS2                         = 553;  // <OptCallConventions> ::= <OptCallConventions> <CallConvention>
       final int PROD_OPTCALLCONVENTIONS3                         = 554;  // <OptCallConventions> ::= 
       final int PROD_CALLCONVENTION_REGISTER                     = 555;  // <CallConvention> ::= REGISTER
       final int PROD_CALLCONVENTION_PASCAL                       = 556;  // <CallConvention> ::= PASCAL
       final int PROD_CALLCONVENTION_CDECL                        = 557;  // <CallConvention> ::= CDECL
       final int PROD_CALLCONVENTION_STDCALL                      = 558;  // <CallConvention> ::= STDCALL
       final int PROD_CALLCONVENTION_SAFECALL                     = 559;  // <CallConvention> ::= SAFECALL
       final int PROD_CALLOBSOLETE_INLINE                         = 560;  // <CallObsolete> ::= INLINE
       final int PROD_CALLOBSOLETE_ASSEMBLER                      = 561;  // <CallObsolete> ::= ASSEMBLER
       final int PROD_CALLOBSOLETE_NEAR                           = 562;  // <CallObsolete> ::= NEAR
       final int PROD_CALLOBSOLETE_FAR                            = 563;  // <CallObsolete> ::= FAR
       final int PROD_CALLOBSOLETE_EXPORT                         = 564;  // <CallObsolete> ::= EXPORT
       final int PROD_ASMLANGUAGE                                 = 565;  // <AsmLanguage> ::= <AsmInstruction>
       final int PROD_ASMLANGUAGE2                                = 566;  // <AsmLanguage> ::= <AsmLanguage> <AsmInstruction>
       final int PROD_ASMINSTRUCTION                              = 567;  // <AsmInstruction> ::= <AsmItem>
       final int PROD_ASMINSTRUCTION2                             = 568;  // <AsmInstruction> ::= <AsmInstruction> <AsmItem>
       final int PROD_ASMINSTRUCTION_COMMA                        = 569;  // <AsmInstruction> ::= <AsmInstruction> ',' <AsmItem>
       final int PROD_ASMINSTRUCTION_SEMI                         = 570;  // <AsmInstruction> ::= <AsmInstruction> ';'
       final int PROD_ASMITEM                                     = 571;  // <AsmItem> ::= <AsmLabel>
       final int PROD_ASMITEM2                                    = 572;  // <AsmItem> ::= <AsmExpr>
       final int PROD_ASMLABEL_COLON                              = 573;  // <AsmLabel> ::= <AsmLocal> ':'
       final int PROD_ASMLABEL_COLON2                             = 574;  // <AsmLabel> ::= <AsmId> ':'
       final int PROD_ASMEXPR                                     = 575;  // <AsmExpr> ::= <AsmFactor>
       final int PROD_ASMEXPR_MINUS                               = 576;  // <AsmExpr> ::= '-' <AsmFactor>
       final int PROD_ASMEXPR_PLUS                                = 577;  // <AsmExpr> ::= <AsmExpr> '+' <AsmFactor>
       final int PROD_ASMEXPR_TIMES                               = 578;  // <AsmExpr> ::= <AsmExpr> '*' <AsmFactor>
       final int PROD_ASMEXPR_MINUS2                              = 579;  // <AsmExpr> ::= <AsmExpr> '-' <AsmFactor>
       final int PROD_ASMEXPR_DOT                                 = 580;  // <AsmExpr> ::= <AsmExpr> '.' <AsmFactor>
       final int PROD_ASMEXPR_LBRACKET_RBRACKET                   = 581;  // <AsmExpr> ::= '[' <AsmExpr> ']'
       final int PROD_ASMEXPR_LPAREN_RPAREN                       = 582;  // <AsmExpr> ::= '(' <AsmExpr> ')'
       final int PROD_ASMEXPR_SYNERROR                            = 583;  // <AsmExpr> ::= SynError
       final int PROD_ASMFACTOR                                   = 584;  // <AsmFactor> ::= <AsmId>
       final int PROD_ASMFACTOR2                                  = 585;  // <AsmFactor> ::= <AsmLocal>
       final int PROD_ASMFACTOR3                                  = 586;  // <AsmFactor> ::= <ICONST>
       final int PROD_ASMFACTOR4                                  = 587;  // <AsmFactor> ::= <RCONST>
       final int PROD_ASMFACTOR5                                  = 588;  // <AsmFactor> ::= <SCONST>
       final int PROD_ASMID                                       = 589;  // <AsmId> ::= <RefId>
       final int PROD_ASMID_AMP                                   = 590;  // <AsmId> ::= '&' <RefId>
       final int PROD_ASMID_REPEAT                                = 591;  // <AsmId> ::= REPEAT
       final int PROD_ASMID_WHILE                                 = 592;  // <AsmId> ::= WHILE
       final int PROD_ASMID_IF                                    = 593;  // <AsmId> ::= IF
       final int PROD_ASMID_AND                                   = 594;  // <AsmId> ::= AND
       final int PROD_ASMID_OR                                    = 595;  // <AsmId> ::= OR
       final int PROD_ASMID_XOR                                   = 596;  // <AsmId> ::= XOR
       final int PROD_ASMID_SHR                                   = 597;  // <AsmId> ::= SHR
       final int PROD_ASMID_SHL                                   = 598;  // <AsmId> ::= SHL
       final int PROD_ASMID_DIV                                   = 599;  // <AsmId> ::= DIV
       final int PROD_ASMID_NOT                                   = 600;  // <AsmId> ::= NOT
       final int PROD_ASMLOCAL_AT                                 = 601;  // <AsmLocal> ::= '@' <LCONST>
       final int PROD_ASMLOCAL_AT2                                = 602;  // <AsmLocal> ::= '@' <AsmId>
       final int PROD_ASMLOCAL_AT3                                = 603;  // <AsmLocal> ::= '@' <AsmLocal>
       final int PROD_ASMLOCAL_AT_END                             = 604;  // <AsmLocal> ::= '@' END
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
			getLogger().error("Exception during character encoding/decoding: {}", cce.getMessage());
		}
		
		return result;	
	}
	

	// START KGU#354 2017-03-03: Enh. #354 - generalized import mechanism
	@Override
	protected File prepareTextfile(String _textToParse, String _encoding)
	{
		File interm = null;
		try
		{
			DataInputStream in = new DataInputStream(new FileInputStream(_textToParse));
			// START KGU#193 2016-05-04
			BufferedReader br = new BufferedReader(new InputStreamReader(in, _encoding));
			// END KGU#193 2016-05-04
			String strLine;
			String pasCode = new String();
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   
			{
				// add no ending because of comment filter
				pasCode += strLine+"\u2190";
				//pasCode+=strLine+"\n";
			}
			//Close the input stream
			in.close();

			// filter out comments (KGU: Why? The GOLDParser can do it itself)
//			Regex r = new Regex("(.*?)[(][*](.*?)[*][)](.*?)","$1$3"); 
//			pasCode=r.replaceAll(pasCode);
//			r = new Regex("(.*?)[{](.*?)[}](.*?)","$1$3"); 
//			pasCode = r.replaceAll(pasCode);

			// START KGU#195 2016-05-04: Issue #185 - Workaround for mere subroutines
			pasCode = embedSubroutineDeclaration(pasCode);
			// END KGU#195 2016-05-04

			// reset correct endings
			Regex r = new Regex("(.*?)[\u2190](.*?)","$1\n$2"); 
			pasCode = r.replaceAll(pasCode);
			// START KGU#354 2017-03-07: Workaround for missing second commet delimiter pair in GOLDParser 5.0
//			pasCode = pasCode.replaceAll("(.*?)(\\(\\*)(.*?)(\\*\\))(.*?)", "$1\\{$3\\}$5");
			// END KGU#354 2017-03-07

			//System.out.println(pasCode);

			// trim and save as new file
			//interm = new File(_textToParse + ".structorizer");
			interm = File.createTempFile("Structorizer", ".pas");
			OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(interm), "ISO-8859-1");
			ow.write(filterNonAscii(pasCode.trim()+"\n"));
			//System.out.println("==> "+filterNonAscii(pasCode.trim()+"\n"));
			ow.close();
		}
		catch (Exception e) 
		{
			getLogger().error(e.getMessage());
		}	
		return interm;
	}
	// END KGU#354 2017-03-03
	
	// START KGU#195 2016-05-04: Issue #185 - Workaround for mere subroutines
	private String embedSubroutineDeclaration(String _pasCode)
	{
		// Find the first non-empty line where line ends are encoded as "\u2190"
		boolean headerFound = false;
		int pos = -1;
		int lineEnd = -1;
		while (!headerFound && (lineEnd = _pasCode.indexOf("\u2190", pos+1)) >= 0)
		{
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
				_pasCode = "program dummy;" + "\u2190"
						+ _pasCode + "\u2190"
						+ "begin" + "\u2190"
						+ "end." + "\u2190";
			}
		}
		return _pasCode;
	}
	// END KGU#195 2016-05-04

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.parsers.CodeParser#initializeBuildNSD()
	 */
	@Override
	protected void initializeBuildNSD()
	{
		// START KGU#194 2016-05-08: Bugfix #185
		unitName = null;
		// END KGU#194 2016-05-08
		// START KGU#407 207-06-22: Enh. #420: Configure the lookup table for comment retrieval
		this.registerStatementRuleIds(statementIds);
		// END KGU#407 2017-06-11
	}
	
	protected void buildNSD_R(Reduction _reduction, Subqueue _parentNode)
	{
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
			   )
			{
				content=new String();
				content=getContent_R(_reduction,content);
				//System.out.println(ruleHead + ": " + content);
				// START KGU#407 2017-06-20: Enh. #420 - comments already here
				//_parentNode.addElement(new Instruction(translateContent(content)));
				_parentNode.addElement(this.equipWithSourceComment(new Instruction(translateContent(content)), _reduction));
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
					_reduction.get(2).asReduction().getParent().getTableIndex() == RuleConstants.PROD_TYPESPEC_SEMI
					)
			{
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
				default:
					break;
				}
				if (!content.isEmpty()) {
					// FIXME: Could be global if outside of functions! --> global Root --> includable
					_parentNode.addElement(this.equipWithSourceComment(new Instruction(translateContent(content)), _reduction));
				}
			}
			// END KGU#388 2017-09-22
			else if (
					 ruleHead.equals("<UsesClause>")
					 // START KGU#358 2017-03-29: Enh. #368 we do no longer ignore const/var declarations
					 //||
					 //ruleHead.equals("<VarSection>")
					 //||
					 //ruleHead.equals("<ConstSection>")
					 //||
					 //ruleHead.equals("<TypeSection>")
					 ||
					 ruleHead.equals("<LabelSection>")
					 // END KGU#358 2017-03-29
					 // START KGU#194 2016-05-08: Bugfix #185
					 // UNIT Interface section can be ignored, all contained routines
					 // must be converted from the implementation section
					 ||
					 ruleHead.equals("<InterfaceSection>")
					 ||
					 ruleHead.equals("<InitSection>")
					 // END KGU#194 2016-05-08
					 )
			{
				// This is just to skip these sections
			}
			// START KGU#194 2016-05-08: Bugfix #185 - we must handle unit headers
			else if (
					ruleHead.equals("<UnitHeader>")
					 )
			{
				unitName = getContent_R((Reduction) _reduction.get(1).getData(), "");
			}
			else if (
					ruleHead.equals("<ProcedureDecl>")
					||
					ruleHead.equals("<FunctionDecl>")
					||
					ruleHead.equals("<MethodDecl>")
					)
			{
				Root prevRoot = root;	// Push the original root
				// If root this is top level and a program diagram then there may only have been declarations
				// so far. And these may be global. So transfer them to a new includable diagram and act as if
				// it were a unit.
				if (unitName == null && subRoots.isEmpty() && prevRoot.isProgram()) {
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
					includable.setInclude();
					prevRoot.children.removeElements();
					prevRoot.includeList = StringList.getNew(unitName + DEFAULT_GLOBAL_SUFFIX);
					subRoots.add(includable);
				}
				root = new Root();	// Prepare a new root for the subroutine
				// START KGU#407 2017-06-20: Enh. #420 - comments already here
				this.equipWithSourceComment(root, _reduction);
				// END KGU#407 2017-06-22
				subRoots.add(root);
				for (int i=0; i < _reduction.size(); i++)
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
					 )
			{
				content = new String();
				content = getContent_R(_reduction.get(1).asReduction(), content);
				
				Reduction secReduc = _reduction.get(2).asReduction();
				if (secReduc.size()!=0)
				{
					content = getContent_R(secReduc,content);
				}
				
				if (ruleHead.equals("<FuncHeading>"))
				{
					secReduc = _reduction.get(4).asReduction();
					if (secReduc.size() > 0)
					{
						content += ": ";
						content = getContent_R(secReduc,content);
					}
				}
				
				content = content.replace(";", "; ");
				content = content.replace(";  ", "; ");
				root.setText(translateContent(content));
				root.setProgram(false);
				// START KGU#194 2016-05-08: Bugfix #185 - be aware of unit context
				if (unitName != null)
				{
					// START KGU#376 2017-09-22: Enh. #389 - the unit will be an includable now
					//root.setComment("(UNIT " + unitName + ")");
					if (root.includeList == null) {
						root.includeList = new StringList();
					}
					root.includeList.addIfNew(unitName + DEFAULT_GLOBAL_SUFFIX);
					// END KGU#376 2017-09-22
				}
				// END KGU#194 2016-05-08
				// START KGU#407 2017-06-20: Enh. #420 - comments already here
				String comment = this.retrieveComment(_reduction);
				if (comment != null && !root.getComment().contains(comment)) {
					root.getComment().add(StringList.explode(comment, "\n"));
				}
				// END KGU#407 2017-06-22
			}
			else if (
					 ruleHead.equals("<WhileStatement>")
					 )
			{
				content = new String();
				content = getContent_R(_reduction.get(1).asReduction(), content);
				While ele = new While(getKeyword("preWhile")+translateContent(content)+getKeyword("postWhile"));
				// START KGU#407 2017-06-20: Enh. #420 - comments already here
				this.equipWithSourceComment(ele, _reduction);
				// END KGU#407 2017-06-22
				_parentNode.addElement(ele);
				
				Reduction secReduc = (Reduction) _reduction.get(3).getData();
				buildNSD_R(secReduc,ele.q);
			}
			else if (
					 ruleHead.equals("<RepeatStatement>")
					 )
			{
				content = new String();
				content = getContent_R(_reduction.get(3).asReduction(), content);
				Repeat ele = new Repeat(getKeyword("preRepeat")+translateContent(content)+getKeyword("postRepeat"));
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
				
				Alternative ele = new Alternative(getKeyword("preAlt") + translateContent(content)+getKeyword("postAlt"));
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
						
						Reduction secReduc = (Reduction) _reduction.get(2).getData();
						buildNSD_R(secReduc,(Subqueue) ((Case) _parentNode.parent).qs.get(i-1));
					}
				}

			}
			else if (
					 ruleHead.equals("<CaseStatement>")
					 )
			{
				content = new String();
				content = getKeyword("preCase")+getContent_R(_reduction.get(1).asReduction(),content)+getKeyword("postCase");
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
				ele.setText(translateContent(content));
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
		_content = _content.replaceAll(BString.breakup("write")+"[((](.*?)[))]", output+" $1");
		_content = _content.replaceAll(BString.breakup("writeln")+"[((](.*?)[))]", output+" $1");
		_content = _content.replaceAll(BString.breakup("writeln")+"(.*?)", output+" $1");
		_content = _content.replaceAll(BString.breakup("write")+"(.*?)", output+" $1");
		_content = _content.replaceAll(BString.breakup("read")+"[((](.*?)[))]", input+" $1");
		_content = _content.replaceAll(BString.breakup("readln")+"[((](.*?)[))]", input+" $1");
		_content = _content.replaceAll(BString.breakup("readln")+"(.*?)", input+" $1");
		_content = _content.replaceAll(BString.breakup("read")+"(.*?)", input+" $1");
		
		//System.out.println(_content);
		
		/*
		 _content:=ReplaceEntities(_content);
		*/
		
		//_content = BString.replace(_content, ":="," \u2190 ");
		_content = _content.replace(":="," <- ");

		return _content.trim();
	}
	
	protected String getContent_R(Reduction _reduction, String _content)
	{
//		if (_reduction.size()>0)
//		{
			for(int i=0;i<_reduction.size();i++)
			{
				switch (_reduction.get(i).getType()) 
				{
					case NON_TERMINAL:
						_content = getContent_R((Reduction) _reduction.get(i).getData(), _content);	
						break;
					case CONTENT:
						{
							String tokenData = (String) _reduction.get(i).getData();
							// START KGU 2016-05-08: Avoid keyword concatenation
							boolean tokenIsId = !tokenData.isEmpty() && Character.isJavaIdentifierStart(tokenData.charAt(0));
							// END KGU 2016-05-08
							if (tokenData.trim().equalsIgnoreCase("mod") ||
									// START KGU#192 2016-05-02: There are more operators to be considered...
									tokenData.trim().equalsIgnoreCase("shl") ||
									tokenData.trim().equalsIgnoreCase("shr") ||
									// END KGU#192 2016-05-02
									tokenData.trim().equalsIgnoreCase("div"))
							{
								_content += " " + tokenData + " ";
								// START KGU 2016-05-08: Avoid keyword concatenation
								tokenIsId = false;
								// END KGU 2016-05-08
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
//		}
//		else
//		{
//			// ?
//			// _content:=_content+trim(R.ParentRule.Text)
//		}
		
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
	protected void subclassUpdateRoot(Root root, String sourceFileName) {
		getLogger().trace(root.getSignatureString(false));
		if (unitName != null && root.isProgram() && root.getMethodName().equals("???")) {
			root.setText(unitName + DEFAULT_GLOBAL_SUFFIX);
			root.setInclude();
			root.getComment().insert("(UNIT " + unitName + ")", 0);
		}
	}

}

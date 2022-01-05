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

package lu.fisch.structorizer.generators;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Class to parse a Structorizer line for the restricted syntax supported by
 *                      the core ArmGenerator.
 *
 ******************************************************************************************************
 *
 *      Revision List (this parser)
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2021-11-10      First Issue (generated with GOLDprog.exe)
 *      Kay Gürtzig     2021-11-17      Bugfix #1020: Preparation of Instruction lines with return syntax
 *                                      ensured, keyword replacement in Jump elements improved
 *
 ******************************************************************************************************
 *
 *     Comment:		
 *
 ******************************************************************************************************/

import java.util.logging.Level;
import java.util.logging.Logger;

import com.creativewidgetworks.goldparser.engine.*;

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.Jump;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.structorizer.parsers.AuParser;
import lu.fisch.structorizer.parsers.CodeParser;
import lu.fisch.utils.StringList;

/**
 * Syntax checker class for ArmGenerator, based on GOLDParser 5.0 for the StructorizerArmLine
 * language, which represents a deliberate subset of the language ArmGenerator actually copes
 * with.
 * @author Kay Gürtzig
 */
public class ArmLineParser implements GeneratorSyntaxChecker
{
	private Logger logger;
	/** @return the standard Java logger for this class */
	protected Logger getLogger()
	{
		if (this.logger == null) {
			this.logger = Logger.getLogger(getClass().getName());
		}
		return this.logger;
	}

	/**
	 * The generic LALR(1) parser providing the parse tree
	 */
	protected AuParser parser;

	/**
	 * String field holding the message of error occurred during parsing or build phase
	 * for later evaluation (empty if there was no error)
	 * @see #exception
	 */
	public String error;

	// START KGU#604 2018-10-29: Enh. #627
	/**
	 * An exception object having caused the failing of the parsing process and may be
	 * extracted to the clipboard for certain errors. 
	 * @see #error
	 */
	public Exception exception;
	// END KGU#604 2018-10-29

	//---------------------- Grammar specification ---------------------------

	/**
	 * Is to provide the file name of the compiled grammar the parser class is made for
	 * @return a grammar file name retrievable as resource (a cgt or egt file).
	 */
	protected final String getCompiledGrammar()
	{
		return "StructorizerArmLine.egt";
	}
	
	/**
	 * Is to return the internal name of the grammar table as given in the grammar file
	 * parameters
	 * @return Name string as specified in the grammar file header
	 */
	protected final String getGrammarTableName()
	{
		return "StructorizerArmLine";
	}

	//------------------------------ Constructor -----------------------------

	/**
	 * Constructs a parser for language StructorizerArmLine, loads the grammar as resource and
	 * specifies whether to generate the parse tree as string
	 */
	public ArmLineParser() {
		try {
			// AuParser is a Structorizer subclass of GOLDParser (Au = gold)
			parser = new AuParser(
					getClass().getResourceAsStream(getCompiledGrammar()),
					getGrammarTableName(),
					true,
					null);
		}
		catch (Exception ex) {
			getLogger().log(Level.WARNING, "Failed to get an AuParser", ex);
		}
	}

	//---------------------- Grammar table constants DON'T MODIFY! ---------------------------

	// Symbolic constants naming the table indices of the symbols of the grammar 
//	@SuppressWarnings("unused")
//	private interface SymbolConstants 
//	{
//		final int SYM_EOF                                  =   0;  // (EOF)
//		final int SYM_ERROR                                =   1;  // (Error)
//		final int SYM_WHITESPACE                           =   2;  // Whitespace
//		final int SYM_MINUS                                =   3;  // '-'
//		final int SYM_EXCLAM                               =   4;  // '!'
//		final int SYM_EXCLAMEQ                             =   5;  // '!='
//		final int SYM_AMP                                  =   6;  // '&'
//		final int SYM_AMPAMP                               =   7;  // '&&'
//		final int SYM_LPAREN                               =   8;  // '('
//		final int SYM_RPAREN                               =   9;  // ')'
//		final int SYM_TIMES                                =  10;  // '*'
//		final int SYM_COMMA                                =  11;  // ','
//		final int SYM_LBRACKET                             =  12;  // '['
//		final int SYM_RBRACKET                             =  13;  // ']'
//		final int SYM_LBRACE                               =  14;  // '{'
//		final int SYM_PIPE                                 =  15;  // '|'
//		final int SYM_PIPEPIPE                             =  16;  // '||'
//		final int SYM_RBRACE                               =  17;  // '}'
//		final int SYM_PLUS                                 =  18;  // '+'
//		final int SYM_LT                                   =  19;  // '<'
//		final int SYM_LTMINUS                              =  20;  // '<-'
//		final int SYM_LTEQ                                 =  21;  // '<='
//		final int SYM_EQEQ                                 =  22;  // '=='
//		final int SYM_GT                                   =  23;  // '>'
//		final int SYM_GTEQ                                 =  24;  // '>='
//		final int SYM_ADDRESS                              =  25;  // address
//		final int SYM_BINARYINTEGERLITERAL                 =  26;  // BinaryIntegerLiteral
//		final int SYM_BOOLEANLITERAL                       =  27;  // BooleanLiteral
//		final int SYM_BYTE                                 =  28;  // byte
//		final int SYM_CALLPREFIX                           =  29;  // CallPrefix
//		final int SYM_CASEPREFIX                           =  30;  // CasePrefix
//		final int SYM_CATCHPREFIX                          =  31;  // CatchPrefix
//		final int SYM_CONDPREFIX                           =  32;  // CondPrefix
//		final int SYM_EXITKEY                              =  33;  // ExitKey
//		final int SYM_FORINKEY                             =  34;  // ForInKey
//		final int SYM_FORKEY                               =  35;  // ForKey
//		final int SYM_HEXESCAPECHARLITERAL                 =  36;  // HexEscapeCharLiteral
//		final int SYM_HEXINTEGERLITERAL                    =  37;  // HexIntegerLiteral
//		final int SYM_HWORD                                =  38;  // hword
//		final int SYM_IDENTIFIER                           =  39;  // Identifier
//		final int SYM_INDIRECTCHARLITERAL                  =  40;  // IndirectCharLiteral
//		final int SYM_INDIRIZZO                            =  41;  // indirizzo
//		final int SYM_INKEY                                =  42;  // InKey
//		final int SYM_INPUTKEY                             =  43;  // InputKey
//		final int SYM_LEAVEKEY                             =  44;  // LeaveKey
//		final int SYM_MEMORIA                              =  45;  // memoria
//		final int SYM_MEMORY                               =  46;  // memory
//		final int SYM_OCTA                                 =  47;  // octa
//		final int SYM_OCTALESCAPECHARLITERAL               =  48;  // OctalEscapeCharLiteral
//		final int SYM_OCTALINTEGERLITERAL                  =  49;  // OctalIntegerLiteral
//		final int SYM_OUTPUTKEY                            =  50;  // OutputKey
//		final int SYM_QUAD                                 =  51;  // quad
//		final int SYM_RETURNKEY                            =  52;  // ReturnKey
//		final int SYM_SELECTORPREFIX                       =  53;  // SelectorPrefix
//		final int SYM_STANDARDESCAPECHARLITERAL            =  54;  // StandardEscapeCharLiteral
//		final int SYM_STARTWITHNOZERODECIMALINTEGERLITERAL =  55;  // StartWithNoZeroDecimalIntegerLiteral
//		final int SYM_STARTWITHZERODECIMALINTEGERLITERAL   =  56;  // StartWithZeroDecimalIntegerLiteral
//		final int SYM_STEPKEY                              =  57;  // StepKey
//		final int SYM_STRINGLITERAL                        =  58;  // StringLiteral
//		final int SYM_THROWKEY                             =  59;  // ThrowKey
//		final int SYM_TOKEY                                =  60;  // ToKey
//		final int SYM_WORD                                 =  61;  // word
//		final int SYM_ADDRESSFUNCTION                      =  62;  // <AddressFunction>
//		final int SYM_ADDRESSKEY                           =  63;  // <AddressKey>
//		final int SYM_ANDEXTENSION                         =  64;  // <AndExtension>
//		final int SYM_ARRAYACCESS                          =  65;  // <ArrayAccess>
//		final int SYM_ARRAYASSIGNMENT                      =  66;  // <ArrayAssignment>
//		final int SYM_ARRAYINITIALISATION                  =  67;  // <ArrayInitialisation>
//		final int SYM_ARRAYINITIALIZER                     =  68;  // <ArrayInitializer>
//		final int SYM_ASSIGNMENT                           =  69;  // <Assignment>
//		final int SYM_ASSIGNMENTOPERATOR                   =  70;  // <AssignmentOperator>
//		final int SYM_BASETYPE                             =  71;  // <BaseType>
//		final int SYM_BINARYOPERATIONOPT                   =  72;  // <BinaryOperationOpt>
//		final int SYM_BINARYOPERATOR                       =  73;  // <BinaryOperator>
//		final int SYM_CALL                                 =  74;  // <Call>
//		final int SYM_CASEDISCRIMINATOR                    =  75;  // <CaseDiscriminator>
//		final int SYM_CASESELECTORS                        =  76;  // <CaseSelectors>
//		final int SYM_CATCHCLAUSE                          =  77;  // <CatchClause>
//		final int SYM_CHARACTERLITERAL                     =  78;  // <CharacterLiteral>
//		final int SYM_COMPARISONOPERATOR                   =  79;  // <ComparisonOperator>
//		final int SYM_CONDITION                            =  80;  // <Condition>
//		final int SYM_CONDITIONALEXPRESSION                =  81;  // <ConditionalExpression>
//		final int SYM_DECIMALINTEGERLITERAL                =  82;  // <DecimalIntegerLiteral>
//		final int SYM_ELEMENTLINE                          =  83;  // <ElementLine>
//		final int SYM_FORHEADER                            =  84;  // <ForHeader>
//		final int SYM_FORINHEADER                          =  85;  // <ForInHeader>
//		final int SYM_INPUTINSTRUCTION                     =  86;  // <InputInstruction>
//		final int SYM_INTEGERLITERAL                       =  87;  // <IntegerLiteral>
//		final int SYM_JUMP                                 =  88;  // <Jump>
//		final int SYM_MEMORYACCESS                         =  89;  // <MemoryAccess>
//		final int SYM_MEMORYKEY                            =  90;  // <MemoryKey>
//		final int SYM_MEMORYSTORE                          =  91;  // <MemoryStore>
//		final int SYM_NEGATIVEDECIMALLITERAL               =  92;  // <NegativeDecimalLiteral>
//		final int SYM_OFFSETOPT                            =  93;  // <OffsetOpt>
//		final int SYM_OPERAND                              =  94;  // <Operand>
//		final int SYM_OPERANDLIST                          =  95;  // <OperandList>
//		final int SYM_OREXTENSION                          =  96;  // <OrExtension>
//		final int SYM_OUTPUTINSTRUCTION                    =  97;  // <OutputInstruction>
//		final int SYM_RELATIONALEXPRESSION                 =  98;  // <RelationalExpression>
//		final int SYM_RIGHTHANDSIDE                        =  99;  // <RightHandSide>
//		final int SYM_ROUTINEINVOCATION                    = 100;  // <RoutineInvocation>
//		final int SYM_SCALARLITERAL                        = 101;  // <ScalarLiteral>
//		final int SYM_SCALARLITERALLIST                    = 102;  // <ScalarLiteralList>
//		final int SYM_SIGNEDOPERAND                        = 103;  // <SignedOperand>
//		final int SYM_STEPCLAUSE                           = 104;  // <StepClause>
//		final int SYM_VALUELIST                            = 105;  // <ValueList>
//		final int SYM_VARIABLENAMELIST                     = 106;  // <VariableNameList>
//		final int SYM_VARIABLENAMER                        = 107;  // <VariableNameR>
//	};

	// Symbolic constants naming the table indices of the grammar rules
//	@SuppressWarnings("unused")
//	private interface RuleConstants
//	{
//		final int PROD_ELEMENTLINE                                                =   0;  // <ElementLine> ::= <Assignment>
//		final int PROD_ELEMENTLINE2                                               =   1;  // <ElementLine> ::= <InputInstruction>
//		final int PROD_ELEMENTLINE3                                               =   2;  // <ElementLine> ::= <OutputInstruction>
//		final int PROD_ELEMENTLINE4                                               =   3;  // <ElementLine> ::= <ArrayInitialisation>
//		final int PROD_ELEMENTLINE5                                               =   4;  // <ElementLine> ::= <ForHeader>
//		final int PROD_ELEMENTLINE6                                               =   5;  // <ElementLine> ::= <ForInHeader>
//		final int PROD_ELEMENTLINE7                                               =   6;  // <ElementLine> ::= <Jump>
//		final int PROD_ELEMENTLINE8                                               =   7;  // <ElementLine> ::= <Call>
//		final int PROD_ELEMENTLINE9                                               =   8;  // <ElementLine> ::= <CatchClause>
//		final int PROD_ELEMENTLINE10                                              =   9;  // <ElementLine> ::= <CaseDiscriminator>
//		final int PROD_ELEMENTLINE11                                              =  10;  // <ElementLine> ::= <CaseSelectors>
//		final int PROD_ELEMENTLINE12                                              =  11;  // <ElementLine> ::= <Condition>
//		final int PROD_CHARACTERLITERAL_INDIRECTCHARLITERAL                       =  12;  // <CharacterLiteral> ::= IndirectCharLiteral
//		final int PROD_CHARACTERLITERAL_STANDARDESCAPECHARLITERAL                 =  13;  // <CharacterLiteral> ::= StandardEscapeCharLiteral
//		final int PROD_CHARACTERLITERAL_OCTALESCAPECHARLITERAL                    =  14;  // <CharacterLiteral> ::= OctalEscapeCharLiteral
//		final int PROD_CHARACTERLITERAL_HEXESCAPECHARLITERAL                      =  15;  // <CharacterLiteral> ::= HexEscapeCharLiteral
//		final int PROD_DECIMALINTEGERLITERAL_STARTWITHZERODECIMALINTEGERLITERAL   =  16;  // <DecimalIntegerLiteral> ::= StartWithZeroDecimalIntegerLiteral
//		final int PROD_DECIMALINTEGERLITERAL_STARTWITHNOZERODECIMALINTEGERLITERAL =  17;  // <DecimalIntegerLiteral> ::= StartWithNoZeroDecimalIntegerLiteral
//		final int PROD_NEGATIVEDECIMALLITERAL_MINUS                               =  18;  // <NegativeDecimalLiteral> ::= '-' <DecimalIntegerLiteral>
//		final int PROD_INTEGERLITERAL                                             =  19;  // <IntegerLiteral> ::= <DecimalIntegerLiteral>
//		final int PROD_INTEGERLITERAL_HEXINTEGERLITERAL                           =  20;  // <IntegerLiteral> ::= HexIntegerLiteral
//		final int PROD_INTEGERLITERAL_OCTALINTEGERLITERAL                         =  21;  // <IntegerLiteral> ::= OctalIntegerLiteral
//		final int PROD_INTEGERLITERAL_BINARYINTEGERLITERAL                        =  22;  // <IntegerLiteral> ::= BinaryIntegerLiteral
//		final int PROD_SCALARLITERAL                                              =  23;  // <ScalarLiteral> ::= <IntegerLiteral>
//		final int PROD_SCALARLITERAL_BOOLEANLITERAL                               =  24;  // <ScalarLiteral> ::= BooleanLiteral
//		final int PROD_SCALARLITERAL2                                             =  25;  // <ScalarLiteral> ::= <CharacterLiteral>
//		final int PROD_SCALARLITERALLIST                                          =  26;  // <ScalarLiteralList> ::= <ScalarLiteral>
//		final int PROD_SCALARLITERALLIST2                                         =  27;  // <ScalarLiteralList> ::= <NegativeDecimalLiteral>
//		final int PROD_SCALARLITERALLIST_COMMA                                    =  28;  // <ScalarLiteralList> ::= <ScalarLiteralList> ',' <ScalarLiteral>
//		final int PROD_SCALARLITERALLIST_COMMA2                                   =  29;  // <ScalarLiteralList> ::= <ScalarLiteralList> ',' <NegativeDecimalLiteral>
//		final int PROD_VARIABLENAMER_IDENTIFIER                                   =  30;  // <VariableNameR> ::= Identifier
//		final int PROD_VARIABLENAMELIST                                           =  31;  // <VariableNameList> ::= <VariableNameR>
//		final int PROD_VARIABLENAMELIST_COMMA                                     =  32;  // <VariableNameList> ::= <VariableNameList> ',' <VariableNameR>
//		final int PROD_RELATIONALEXPRESSION                                       =  33;  // <RelationalExpression> ::= <VariableNameR>
//		final int PROD_RELATIONALEXPRESSION_EXCLAM                                =  34;  // <RelationalExpression> ::= '!' <VariableNameR>
//		final int PROD_RELATIONALEXPRESSION2                                      =  35;  // <RelationalExpression> ::= <VariableNameR> <ComparisonOperator> <Operand>
//		final int PROD_CONDITIONALEXPRESSION                                      =  36;  // <ConditionalExpression> ::= <RelationalExpression>
//		final int PROD_CONDITIONALEXPRESSION2                                     =  37;  // <ConditionalExpression> ::= <RelationalExpression> <AndExtension>
//		final int PROD_CONDITIONALEXPRESSION3                                     =  38;  // <ConditionalExpression> ::= <RelationalExpression> <OrExtension>
//		final int PROD_ANDEXTENSION_AMPAMP                                        =  39;  // <AndExtension> ::= '&&' <RelationalExpression>
//		final int PROD_ANDEXTENSION_AMPAMP2                                       =  40;  // <AndExtension> ::= '&&' <RelationalExpression> <AndExtension>
//		final int PROD_OREXTENSION_PIPEPIPE                                       =  41;  // <OrExtension> ::= '||' <RelationalExpression>
//		final int PROD_OREXTENSION_PIPEPIPE2                                      =  42;  // <OrExtension> ::= '||' <RelationalExpression> <OrExtension>
//		final int PROD_OPERAND                                                    =  43;  // <Operand> ::= <VariableNameR>
//		final int PROD_OPERAND2                                                   =  44;  // <Operand> ::= <ScalarLiteral>
//		final int PROD_SIGNEDOPERAND                                              =  45;  // <SignedOperand> ::= <Operand>
//		final int PROD_SIGNEDOPERAND2                                             =  46;  // <SignedOperand> ::= <NegativeDecimalLiteral>
//		final int PROD_OPERANDLIST                                                =  47;  // <OperandList> ::= <SignedOperand>
//		final int PROD_OPERANDLIST_COMMA                                          =  48;  // <OperandList> ::= <OperandList> ',' <SignedOperand>
//		final int PROD_ROUTINEINVOCATION_IDENTIFIER_LPAREN_RPAREN                 =  49;  // <RoutineInvocation> ::= Identifier '(' ')'
//		final int PROD_ROUTINEINVOCATION_IDENTIFIER_LPAREN_RPAREN2                =  50;  // <RoutineInvocation> ::= Identifier '(' <OperandList> ')'
//		final int PROD_ADDRESSFUNCTION_LPAREN_RPAREN                              =  51;  // <AddressFunction> ::= <AddressKey> '(' <VariableNameR> ')'
//		final int PROD_ADDRESSKEY_ADDRESS                                         =  52;  // <AddressKey> ::= address
//		final int PROD_ADDRESSKEY_INDIRIZZO                                       =  53;  // <AddressKey> ::= indirizzo
//		final int PROD_MEMORYACCESS_LBRACKET_RBRACKET                             =  54;  // <MemoryAccess> ::= <MemoryKey> '[' <VariableNameR> <OffsetOpt> ']'
//		final int PROD_MEMORYKEY_MEMORY                                           =  55;  // <MemoryKey> ::= memory
//		final int PROD_MEMORYKEY_MEMORIA                                          =  56;  // <MemoryKey> ::= memoria
//		final int PROD_OFFSETOPT_PLUS                                             =  57;  // <OffsetOpt> ::= '+' <Operand>
//		final int PROD_OFFSETOPT                                                  =  58;  // <OffsetOpt> ::= 
//		final int PROD_ARRAYACCESS_LBRACKET_RBRACKET                              =  59;  // <ArrayAccess> ::= <VariableNameR> '[' <Operand> ']'
//		final int PROD_BINARYOPERATIONOPT                                         =  60;  // <BinaryOperationOpt> ::= <BinaryOperator> <Operand>
//		final int PROD_BINARYOPERATIONOPT2                                        =  61;  // <BinaryOperationOpt> ::= 
//		final int PROD_BINARYOPERATOR_MINUS                                       =  62;  // <BinaryOperator> ::= '-'
//		final int PROD_BINARYOPERATOR_PLUS                                        =  63;  // <BinaryOperator> ::= '+'
//		final int PROD_BINARYOPERATOR_TIMES                                       =  64;  // <BinaryOperator> ::= '*'
//		final int PROD_BINARYOPERATOR_AMP                                         =  65;  // <BinaryOperator> ::= '&'
//		final int PROD_BINARYOPERATOR_PIPE                                        =  66;  // <BinaryOperator> ::= '|'
//		final int PROD_BINARYOPERATOR_AMPAMP                                      =  67;  // <BinaryOperator> ::= '&&'
//		final int PROD_BINARYOPERATOR_PIPEPIPE                                    =  68;  // <BinaryOperator> ::= '||'
//		final int PROD_ASSIGNMENTOPERATOR_LTMINUS                                 =  69;  // <AssignmentOperator> ::= '<-'
//		final int PROD_COMPARISONOPERATOR_EQEQ                                    =  70;  // <ComparisonOperator> ::= '=='
//		final int PROD_COMPARISONOPERATOR_EXCLAMEQ                                =  71;  // <ComparisonOperator> ::= '!='
//		final int PROD_COMPARISONOPERATOR_LT                                      =  72;  // <ComparisonOperator> ::= '<'
//		final int PROD_COMPARISONOPERATOR_GT                                      =  73;  // <ComparisonOperator> ::= '>'
//		final int PROD_COMPARISONOPERATOR_LTEQ                                    =  74;  // <ComparisonOperator> ::= '<='
//		final int PROD_COMPARISONOPERATOR_GTEQ                                    =  75;  // <ComparisonOperator> ::= '>='
//		final int PROD_ARRAYINITIALIZER_LBRACE_RBRACE                             =  76;  // <ArrayInitializer> ::= '{' '}'
//		final int PROD_ARRAYINITIALIZER_LBRACE_RBRACE2                            =  77;  // <ArrayInitializer> ::= '{' <ScalarLiteralList> '}'
//		final int PROD_ASSIGNMENT                                                 =  78;  // <Assignment> ::= <VariableNameR> <AssignmentOperator> <RightHandSide>
//		final int PROD_ASSIGNMENT2                                                =  79;  // <Assignment> ::= <ArrayAssignment>
//		final int PROD_ASSIGNMENT3                                                =  80;  // <Assignment> ::= <MemoryStore>
//		final int PROD_RIGHTHANDSIDE                                              =  81;  // <RightHandSide> ::= <ScalarLiteral>
//		final int PROD_RIGHTHANDSIDE2                                             =  82;  // <RightHandSide> ::= <NegativeDecimalLiteral>
//		final int PROD_RIGHTHANDSIDE_STRINGLITERAL                                =  83;  // <RightHandSide> ::= StringLiteral
//		final int PROD_RIGHTHANDSIDE3                                             =  84;  // <RightHandSide> ::= <VariableNameR> <BinaryOperationOpt>
//		final int PROD_RIGHTHANDSIDE4                                             =  85;  // <RightHandSide> ::= <AddressFunction>
//		final int PROD_RIGHTHANDSIDE5                                             =  86;  // <RightHandSide> ::= <MemoryAccess>
//		final int PROD_RIGHTHANDSIDE6                                             =  87;  // <RightHandSide> ::= <ArrayAccess>
//		final int PROD_RIGHTHANDSIDE7                                             =  88;  // <RightHandSide> ::= <ArrayInitializer>
//		final int PROD_INPUTINSTRUCTION_INPUTKEY                                  =  89;  // <InputInstruction> ::= InputKey <VariableNameList>
//		final int PROD_OUTPUTINSTRUCTION_OUTPUTKEY                                =  90;  // <OutputInstruction> ::= OutputKey <OperandList>
//		final int PROD_ARRAYINITIALISATION_LBRACKET_RBRACKET                      =  91;  // <ArrayInitialisation> ::= <BaseType> '[' ']' <VariableNameR> <AssignmentOperator> <ArrayInitializer>
//		final int PROD_BASETYPE_BYTE                                              =  92;  // <BaseType> ::= byte
//		final int PROD_BASETYPE_HWORD                                             =  93;  // <BaseType> ::= hword
//		final int PROD_BASETYPE_WORD                                              =  94;  // <BaseType> ::= word
//		final int PROD_BASETYPE_QUAD                                              =  95;  // <BaseType> ::= quad
//		final int PROD_BASETYPE_OCTA                                              =  96;  // <BaseType> ::= octa
//		final int PROD_ARRAYASSIGNMENT                                            =  97;  // <ArrayAssignment> ::= <ArrayAccess> <AssignmentOperator> <VariableNameR>
//		final int PROD_MEMORYSTORE                                                =  98;  // <MemoryStore> ::= <MemoryAccess> <AssignmentOperator> <VariableNameR>
//		final int PROD_CONDITION_CONDPREFIX                                       =  99;  // <Condition> ::= CondPrefix <ConditionalExpression>
//		final int PROD_FORHEADER_FORKEY_TOKEY                                     = 100;  // <ForHeader> ::= ForKey <VariableNameR> <AssignmentOperator> <Operand> ToKey <Operand> <StepClause>
//		final int PROD_STEPCLAUSE_STEPKEY                                         = 101;  // <StepClause> ::= StepKey <IntegerLiteral>
//		final int PROD_STEPCLAUSE_STEPKEY2                                        = 102;  // <StepClause> ::= StepKey <NegativeDecimalLiteral>
//		final int PROD_STEPCLAUSE                                                 = 103;  // <StepClause> ::= 
//		final int PROD_FORINHEADER_FORINKEY_INKEY                                 = 104;  // <ForInHeader> ::= ForInKey <VariableNameR> InKey <ValueList>
//		final int PROD_VALUELIST                                                  = 105;  // <ValueList> ::= <ArrayInitializer>
//		final int PROD_VALUELIST2                                                 = 106;  // <ValueList> ::= <VariableNameR>
//		final int PROD_JUMP_RETURNKEY                                             = 107;  // <Jump> ::= ReturnKey
//		final int PROD_JUMP_RETURNKEY2                                            = 108;  // <Jump> ::= ReturnKey <Operand>
//		final int PROD_JUMP_EXITKEY                                               = 109;  // <Jump> ::= ExitKey
//		final int PROD_JUMP_EXITKEY2                                              = 110;  // <Jump> ::= ExitKey <Operand>
//		final int PROD_JUMP_LEAVEKEY                                              = 111;  // <Jump> ::= LeaveKey
//		final int PROD_JUMP_LEAVEKEY2                                             = 112;  // <Jump> ::= LeaveKey <DecimalIntegerLiteral>
//		final int PROD_JUMP_THROWKEY_STRINGLITERAL                                = 113;  // <Jump> ::= ThrowKey StringLiteral
//		final int PROD_JUMP_THROWKEY                                              = 114;  // <Jump> ::= ThrowKey <VariableNameR>
//		final int PROD_CALL_CALLPREFIX                                            = 115;  // <Call> ::= CallPrefix <RoutineInvocation>
//		final int PROD_CALL_CALLPREFIX2                                           = 116;  // <Call> ::= CallPrefix <VariableNameR> <AssignmentOperator> <RoutineInvocation>
//		final int PROD_CATCHCLAUSE_CATCHPREFIX                                    = 117;  // <CatchClause> ::= CatchPrefix <VariableNameR>
//		final int PROD_CASEDISCRIMINATOR_CASEPREFIX                               = 118;  // <CaseDiscriminator> ::= CasePrefix <VariableNameR> <BinaryOperationOpt>
//		final int PROD_CASESELECTORS_SELECTORPREFIX                               = 119;  // <CaseSelectors> ::= SelectorPrefix <ScalarLiteralList>
//	};

	//----------------------------- Preprocessor -----------------------------
	
	private String preprocessLine(String line, Element owner, int lineNo)
	{
		Subqueue sq;
		String className = owner.getClass().getSimpleName();
		if (className.equals("Call")) {
			line = "§CALL§ " + line;
		}
		else if (className.equals("Alternative")
				|| className.equals("While")
				|| className.equals("Repeat")) {
			line = "§COND§ " + line;
		}
		else if (className.equals("Case")) {
			line = (lineNo == 0 ? "§CASE§ " : "§SELECT§ ") + line;
		}
		else if (className.equals("Jump")) {
			if (line.trim().isEmpty()) {
				line = "§LEAVE§";
			}
			else {
				StringList tokens = Element.splitLexically(line, false);
				String[] keys = {"preReturn", "preLeave", "preExit", "preThrow"};
				for (String key: keys) {
					String keyWord = CodeParser.getKeyword(key);
					// START KGU#1017 2021-11-17: Issue #1020 Consider case and non-tokens
					//if (line.startsWith(keyWord)) {
					StringList splitKey = Element.splitLexically(keyWord, false);
					if (tokens.indexOf(splitKey, 0, !CodeParser.ignoreCase) == 0) {
					// END KGU#1017 2021-11-17
						line = "§" + key.substring(3).toUpperCase() + "§"
								+ line.substring(keyWord.length());
						break;
					}
				}
			}
		}
		else if (className.equals("Try")) {
			line = "§CATCH§" + line;
		}
		else {
			StringList tokens = Element.splitLexically(line, false);
			if (owner instanceof For) {
				String[] keys;
				String[] markers;
				if (((For)owner).isForInLoop()) {
					keys = new String[]{"preForIn", "postForIn"};
					markers = new String[]{"§FOREACH§", "§IN§"};
				}
				else {
					keys = new String[]{"preFor", "postFor", "stepFor"};
					markers = new String[]{"§FOR§", "§TO§", "§STEP§"};
				}
				for (int i = 0; i < keys.length; i++) {
					String keyWord = CodeParser.getKeyword(keys[i]);
					StringList splitKey = Element.splitLexically(keyWord, false);
					int posKey = tokens.indexOf(splitKey, 0, !CodeParser.ignoreCase);
					if (posKey >= 0) {
						tokens.remove(posKey+1, posKey + splitKey.count());
						tokens.set(posKey, markers[i]);
					}
				}
			}
			// START KGU#1017 2021-11-17: Issue #1020 Accept return lines in Instructions
			else if (className.equals("Instruction") && Jump.isReturn(line)
					&& lineNo == owner.getUnbrokenText().count()-1
					&& owner.parent instanceof Subqueue
					&& (sq = (Subqueue)owner.parent).getElement(sq.getSize()-1) == owner
					&& sq.parent instanceof Root
					&& ((Root)sq.parent).isSubroutine()) {
				StringList splitKey = Element.splitLexically(CodeParser.getKeyword("preReturn"), false);
				tokens.remove(1, splitKey.count());
				tokens.set(0, "§RETURN§");
			}
			// END KGU#1017 2021-11-17
			else {
				String[] keys = {"input", "output"};
				for (String key: keys) {
					StringList splitKey = Element.splitLexically(CodeParser.getKeyword(key), false);
					if (tokens.indexOf(splitKey, 0, !CodeParser.ignoreCase) == 0) {
						tokens.remove(1, splitKey.count());
						tokens.set(0, "§" + key.toUpperCase() + "§");
						break;
					}
				}
			}
			line = tokens.concatenate();
		}
		return line;
	}

	//----------------------------- Postprocessor ----------------------------

	private String undoReplacements(String line) {
		line = line.replace("§COND§", "")
				.replace("§CATCH§", "")
				.replace("§CALL§", "")
				.replace("§CASE§", "")
				.replace("§SELECT§", "")
				.replace("§INPUT§", CodeParser.getKeyword("input"))
				.replace("§OUTPUT§", CodeParser.getKeyword("output"))
				.replace("§FOR§", CodeParser.getKeyword("preFor"))
				.replace("§TO§", CodeParser.getKeyword("postFor"))
				.replace("§STEP§", CodeParser.getKeyword("stepFor"))
				.replace("§FOREACH§", CodeParser.getKeyword("preForIn"))
				.replace("§IN§", CodeParser.getKeyword("postForIn"))
				.replace("§RETURN§", CodeParser.getKeyword("preReturn"))
				.replace("§LEAVE§", CodeParser.getKeyword("preLeave"))
				.replace("§EXIT§", CodeParser.getKeyword("preExit"))
				.replace("§THROW§", CodeParser.getKeyword("preThrow"));
		return line.trim();
	}
	
	//---------------------------- Actual Parsing ----------------------------

	@Override
	public String checkSyntax(String _lineToParse, Element _element, int _lineNo) {
		String trouble = null;
		_lineToParse = preprocessLine(_lineToParse, _element, _lineNo);
		
		if (!parse(_lineToParse)) {
			trouble = error;
		}
		return trouble;
	}
	
	/**
	 * Parses the preprocessed element line {@code _lineToParse} in order to
	 * check its syntax according to the minimalist ARM-compatible instruction set.<br/>
	 * Field {@link #error} will either contain an empty string or an error message
	 * afterwards.<br/>
	 * Field {@link #exception} will either contain {@code null} or a caught
	 * exception.
	 * 
	 * @param _lineToParse - the (preprocessed) Element line to be checked for accepted syntax
	 *        of low-level ARM generator mode.
	 * @return {@code true} if the parsing ended without error, {@code false} if a
	 *        syntax error was detected by the parser
	 *
	 * @throws Exception - if something unexpected happened
	 */
	public boolean parse(String _lineToParse)
	{
		boolean isSyntaxError = false;
		try {
			if (parser == null) {
				// AuParser is a Structorizer subclass of GOLDParser (Au = gold)
				parser = new AuParser(
						getClass().getResourceAsStream(getCompiledGrammar()),
						getGrammarTableName(),
						true,
						null);
			}
			error = "";
			exception = null;

			try {
				boolean parsedWithoutError = parser.parseSourceStatements(_lineToParse);

				// Either execute the code or print any error message
				if (parsedWithoutError) {
					// ************************************** log file
					getLogger().info("Parsing complete.");	// System logging
					// ************************************** end log
					//buildLine(parser.getCurrentReduction());
				} else {
					isSyntaxError = true;
					error = parser.getErrorMessage() + " in line «$»";
				}
			}
			catch (ParserException e) {
				error = "**PARSER ERROR** in line «" + _lineToParse + "»:\n" + e.getMessage();
				getLogger().log(Level.WARNING, error, e);
				exception = e;
			}

			// START KGU#191 2016-04-30: Issue #182 - In error case append the context
			if (isSyntaxError)
			{
				Position pos = parser.getCurrentPosition();
				int colNo = pos.getColumn() - 1;
				if (_lineToParse.length() >= colNo) {
					_lineToParse = _lineToParse.substring(0, colNo) + "► " + _lineToParse.substring(colNo);
				}
				error = error.replace("$", undoReplacements(_lineToParse));
				// ************************************** log file
				getLogger().warning("Parsing failed.");	// System logging
				// ************************************** end log
			}
			// END KGU#191 2016-04-30

			// START KGU#537 2018-06-30: Enh. #553

		}
		catch (Exception ex) {
			error = "**PARSER ERROR** Grammar not found!";
			exception = ex;
		}
		// END KGU#537 2018-07-01
		return !isSyntaxError;
	}

	@Override
	public Exception getParsingException() {
		return exception;
	}

}

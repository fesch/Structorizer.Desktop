/*
    This file is part of Structorizer.

    Structorizer is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Structorizer is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 ***********************************************************************

    OBERON Source Code Generator

    Copyright (C) 2008 Klaus-Peter Reimers

    This file has been released under the terms of the GNU General
    Public License as published by the Free Software Foundation.

 */

package lu.fisch.structorizer.generators;

/******************************************************************************************************
 *
 *      Author:         Klaus-Peter Reimers
 *
 *      Description:    This class generates Oberon code.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author                  Date            Description
 *      ------                  ----            -----------
 *      Klaus-Peter Reimers     2008-01-08      First Issue
 *      Bob Fisch				2008-01-08      Modified "private String transform(String _input)"
 *      Bob Fisch				2008-04-12      Added "Fields" section for generator to be used as plugin
 *      Bob Fisch				2008-08-14      Added declaration output. A comment line in the root element
 *                                              with a "#" is ignored. All other lines are written to the code.
 *      Bob Fisch               2011-11-07      Fixed an issue while doing replacements
 *      Kay Gürtzig             2014-11-10      Operator conversion modified (see comment)
 *      Kay Gürtzig             2014-11-16      Operator conversion corrected (see comment)
 *      Kay Gürtzig             2014-12-02      Additional replacement of long assignment operator "<--" by "<-"
 *      Kay Gürtzig             2015-10-18      Indentation issue fixed and comment generation revised
 *      Kay Gürtzig             2015-12-21      Bugfix #41/#68/#69 (= KGU#93)
 *      Kay Gürtzig             2016-01-16      KGU#109: Bugfix #61 - handling of type names in assignments
 *                                              Enh. #84 + Bugfix #112 (KGU#141): Assignment export revised
 *      Kay Gürtzig             2016-03-23      Enh. #84: Support for FOR-IN loops (KGU#61)
 *      Kay Gürtzig             2016-04-03      KGU#150 Support for CHR and ORD and other built-in functions
 *      Kay Gürtzig             2016-04-29      Bugfix #144 suppressTransformation mode didn't fully work
 *      Kay Gürtzig             2016-07-20      Enh. #160 (subroutines involved) implemented
 *      Kay Gürtzig             2016-08-10      Bugfix #227 (Modules = main programs have to end with full stop)
 *      Kay Gürtzig             2016-08-12      Two tiny embellishments
 *      Kay Gürtzig             2016-08-12      Enh. #231: Additions for Analyser checks 18 and 19 (variable name collisions)
 *      Kay Gürtzig             2016-09-25      Enh. #253: CodeParser.keywordMap refactoring done 
 *      Kay Gürtzig             2016-10-14      Enh. #270: Handling of disabled elements (code.add(...) --> addCode(..))
 *      Kay Gürtzig             2016-10-15      Enh. #271: Support for input instructions with prompt string,
 *                                              Issue #227: In obvious cases (literals) output procedure names inserted.
 *      Kay Gürtzig             2016-10-16      Enh. #274: Colour info for Turtleizer procedures added
 *      Kay Gürtzig             2016-12-22      Issue #227: input and output usage more routine-specific
 *      Kay Gürtzig             2016-01-30      Enh. #335, bugfix #337: More sophisticated type treatment
 *      Kay Gürtzig             2017-02-27      Enh. #346: Insertion mechanism for user-specific include directives
 *      Kay Gürtzig             2017-03-15      Bugfix #382: FOR-IN loop value list items hadn't been transformed 
 *      Kay Gürtzig             2017-05-16      Enh. #372: Export of copyright information
 *      Kay Gürtzig             2017-10-24      Enh. #389, #423: Export strategy for includables and records
 *      Kay Gürtzig             2017-11-02      Issue #447: Line continuation in Alternative and Case elements supported
 *      Kay Gürtzig             2018-03-13      Bugfix #259,#335,#520,#521: Mode suppressTransform enforced for declarations
 *      Kay Gürtzig             2018-07-22      Enh. #563 (simplified record initializers), bugfix #564 (array initializers)
 *      Kay Gürtzig             2019-02-14      Enh. #680: Support for input instructions with several variables
 *      Kay Gürtzig             2019-03-08      Enh. #385: Support for optional parameters (by argument extension in the Call)
 *      Kay Gürtzig             2019-03-13      Enh. #696: All references to Arranger replaced by routinePool
 *      Kay Gürtzig             2019-03-28      Enh. #657: Retrieval for called subroutines now with group filter
 *      Kay Gürtzig             2019-03-30      Issue #696: Type retrieval had to consider an alternative pool
 *      Kay Gürtzig             2019-11-11      Bugfix #773: Mere declarations at top level exported, declarations
 *                                              with incomplete type no longer outcommented but marked with FIXME! comments
 *      Kay Gürtzig             2019-11-12      Enh. #775: Slightly more type-sensitive input instruction handling
 *      Kay Gürtzig             2019-11-13      Bugfix #776: Mere global declarations (from includables must not be repeated
 *                                              as local declarations in subroutines where the variables get assigned
 *      Kay Gürtzig             2019-11-14      Bugfix #779 Correct handling of input and output in case of program diagrams
 *      Kay Gürtzig             2019-11-14      Issue #780: Definitions and calls of parameterless procedures omit parentheses
 *      Kay Gürtzig             2019-11-21      Enh. #739: enum type inference for FOR-IN loops and output
 *      Kay Gürtzig             2019-02-15      Issue #814: Unidentified parameter type marker changed: (*type?*) --> ???
 *      Kay Gürtzig             2020-03-17      Bugfix #838: Generator include mechanism was buggy.
 *      Kay Gürtzig             2020-03-18      Issues #828/#836 Group export enabled, includables had been wrongly handled
 *                                              bugfix #839, fix for issue #780 revised
 *      Kay Gürtzig             2020-03-26      Bugfix KGU#833: The function name got declared as variable without need.
 *
 ******************************************************************************************************
 *
 *      Comment:		Based on "PasGenerator.java" from Bob Fisch
 *      
 *      2015.12.21 - Bugfix #41/#68/#69 (Kay Gürtzig)
 *      - Operator replacement had induced unwanted padding and string literal modifications
 *      - new subclassable method transformTokens() for all token-based replacements 
 *      
 *      2015.10.18 - Bugfix / Code revision (Kay Gürtzig)
 *      - Indentation had worked in an exponential way (duplicated every level: _indent+_indent)
 *      - Interface of comment insertion methods modified
 *      
 *      2014.11.16 - Bugfix / Enhancements
 *      - operator conversion had to be adjusted to comply with Oberon2 syntax
 *      - case structure wasn't properly exported
 *      - comment export inserted
 *
 *      2014.11.10 - Enhancement
 *      - Conversion of C-style logical operators to the Pascal-like ones added
 *      - assignment operator conversion now preserves or ensures surrounding spaces
 *
 ******************************************************************************************************///

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.regex.Matcher;

import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.generators.Generator.TryCatchSupportLevel;

public class OberonGenerator extends Generator {
	
	/************ Fields ***********************/
	protected String getDialogTitle()
	{
		return "Export Oberon Code ...";
	}
	
	protected String getFileDescription()
	{
		return "Oberon Source Code";
	}
	
	protected String getIndent()
	{
		return "  ";
	}
	
	protected String[] getFileExtensions()
	{
		String[] exts = {"Mod"};
		return exts;
	}

    // START KGU 2015-10-18: New pseudo field
    @Override
    protected String commentSymbolLeft()
    {
    	return "(*";
    }

    @Override
    protected String commentSymbolRight()
    {
    	return "*)";
    }
    // END KGU 2015-10-18
	
	// START KGU#78 2015-12-18: Enh. #23 We must know whether to create labels for simple breaks
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#supportsSimpleBreak()
	 */
	@Override
	protected boolean breakMatchesCase()
	{
		return true;
	}
	// END KGU#78 2015-12-18

	// START KGU#371 2019-03-07: Enh. #385
	/**
	 * @return The level of subroutine overloading support in the target language
	 */
	@Override
	protected OverloadingLevel getOverloadingLevel() {
		return OverloadingLevel.OL_NO_OVERLOADING;
	}
	// END KGU#371 2019-03-07

	// START KGU#686 2019-03-18: Enh. #56
	/**
	 * Subclassable method to specify the degree of availability of a try-catch-finally
	 * construction in the target language.
	 * @return a {@link TryCatchSupportLevel} value
	 */
	protected TryCatchSupportLevel getTryCatchLevel()
	{
		return TryCatchSupportLevel.TC_NO_TRY;
	}
	// END KGU#686 2019-03-18

//	// START KGU 2016-08-12: Enh. #231 - information for analyser - obsolete since 3.27
//    private static final String[] reservedWords = new String[]{
//    	"ARRAY", "BEGIN", "BY", "CASE", "CONST", "DIV", "DO", "ELSE", "ELSIF", "END", "EXIT",
//    	"FOR", "IF", "IMPORT", "IN", "IS", "LOOP", "MOD", "MODULE", "NIL", "OF", "OR",
//    	"POINTER", "PROCEDURE", "RECORD", "REPEAT", "RETURN", "THEN", "TO", "TYPE",
//    	"UNTIL", "VAR", "WHILE", "WITH",
//    	"BOOLEAN", "CHAR", "FALSE", "HALT", "INTEGER", "LONG", "LONGINT", "LONGREAL",
//    	"NEW", "REAL", "SET", "SHORT", "SHORTINT", "TRUE"
//    };
//	public String[] getReservedWords()
//	{
//		return reservedWords;
//	}
//	public boolean isCaseSignificant()
//	{
//		return true;
//	}
//	// END KGU 2016-08-12
	
	// START KGU#351 2017-02-26: Enh. #346 - include / import / uses config
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getIncludePattern()
	 */
	@Override
	protected String getIncludePattern()
	{
		return "IMPORT %%;";
	}
	// END KGU#351 2017-02-26

	// START KGU#815/KGU#824 2020-03-18: Enh. #828, bugfix #836
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generatesClass()
	 */
	@Override
	protected boolean allowsMixedModule()
	{
		return true;
	}
	// END KGU#815/KGU#836 2020-03-18
	
	/************ Code Generation **************/

	// START KGU#332 2017-01-30: Enh. #335
	private Map<String,TypeMapEntry> typeMap;
	// END KGU#332 2017-01-30

	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getInputReplacer(boolean)
	 */
	@Override
	// START KGU#281 2016-10-15: Enh. #271
	//protected String getInputReplacer()
	//{
	//	return "In.TYPE($1)";
	//}
	protected String getInputReplacer(boolean withPrompt)
	{
		if (withPrompt) {
			return "Out.String($1); In.TYPE($2)";
		}
		return "In.TYPE($1)";
	}
	// END KGU#281 2016-10-15

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getOutputReplacer()
	 */
	@Override
	protected String getOutputReplacer()
	{
		// START KGU#236 2016-10-16: Enh. #227 - we needed a substitution marker to accomplish identified routines
		//return "Out.TYPE($1)";
		return "Out.TYPE($1%LEN%)";
		// END KGU#236 2016-10-16
	}

	// START KGU#815 2020-03-26: Enh. #828
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#appendComment(lu.fisch.utils.StringList, java.lang.String)
	 */
	@Override
	protected void appendComment(StringList _sl, String _indent)
	{
		if (!_sl.getLongString().trim().isEmpty()) {
			if (_sl.count() == 1 && !_sl.get(0).contains("\n")) {
				this.appendComment(_sl.get(0), _indent);
			}
			else {
				this.appendBlockComment(_sl, _indent, this.commentSymbolLeft(), " * ", " " + this.commentSymbolRight());
			}
		}
	}
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#insertComment(lu.fisch.utils.StringList, java.lang.String, int)
	 */
	@Override
	protected int insertComment(StringList _sl, String _indent, int _atLine)
	{
		if (_sl.getLongString().trim().isEmpty()) {
			return 0;
		}
		else if (_sl.count() == 1 && !_sl.get(0).contains("\n")) {
			return this.insertComment(_sl.get(0), _indent, _atLine);
		}
		return this.insertBlockComment(_sl, _indent, this.commentSymbolLeft(), " * ", " " + this.commentSymbolRight(), _atLine);
	}
	// END KGU#815 2020-03-26
	
	// START KGU#16 2015-11-30
	/**
	 * Transforms type identifier into the target language (as far as possible)
	 * @param _type - a string potentially meaning a datatype (or null)
	 * @param _default - a default string returned if _type happens to be null
	 * @return a type identifier (or the unchanged _type value if matching failed)
	 */
	protected String transformType(String _type, String _default) {
		if (_type == null)
			_type = _default;
		else {
			_type = _type.trim();
			if (_type.equalsIgnoreCase("long") ||
					_type.equalsIgnoreCase("unsigned long")) _type = "LONGINT";
			else if (_type.equalsIgnoreCase("int") ||
					_type.equalsIgnoreCase("integer") ||
					_type.equalsIgnoreCase("unsigned") ||
					_type.equalsIgnoreCase("unsigned int")) _type = "INTEGER";
			else if (_type.equalsIgnoreCase("short") ||
					_type.equalsIgnoreCase("unsigned short") ||
					_type.equalsIgnoreCase("unsigned char")) _type = "SHORTINT";
			else if (_type.equalsIgnoreCase("char") ||
					_type.equalsIgnoreCase("character")) _type = "CHAR";
			else if (_type.equalsIgnoreCase("float") ||
					_type.equalsIgnoreCase("single") ||
					_type.equalsIgnoreCase("real")) _type = "REAL";
			else if (_type.equalsIgnoreCase("double") ||
					_type.equalsIgnoreCase("longreal")) _type = "LONGREAL";
			else if (_type.equalsIgnoreCase("bool") ||
					_type.equalsIgnoreCase("boolean")) _type = "BOOLEAN";
			else if (_type.equalsIgnoreCase("string")) _type = "ARRAY 100 OF CHAR"; // may be too short but how can we guess?
			// To be continued if required...
			// START KGU#332 2017-01-30: Enh. #335
			_type = _type.replace("array", "ARRAY");
			_type = _type.replace(" of ", " OF ");
			String pattern = "(.*)ARRAY\\s*?\\[\\s*[0-9]+\\s*[.][.][.]?\\s*([0-9]+)\\s*\\]\\s*OF\\s*(.*)";
			if (_type.matches(pattern)) {
				String upperIndex = _type.replaceFirst(pattern, "$2");
				int nElements = Integer.parseInt(upperIndex) + 1;
				String elementType = _type.replaceFirst(pattern,  "$3").trim();
				_type = _type.replaceFirst(pattern, "$1ARRAY " + nElements +" OF ") + transformType(elementType, elementType);
			}
			else if (_type.matches(pattern = "ARRAY\\s*OF\\s*(.*)")) {
				String elementType = _type.replaceFirst(pattern, "$1").trim();
				_type = "ARRAY OF " + transformType(elementType, elementType);
			}
			// END KGU#332 2017-01-30
		}
		return _type;
	}
	// END KGU#16 2015-11-30	

	// START KGU#140/KGU#388 2017-10-24: Enh. #113, #423
	/**
	 * Creates a type description suited for Oberon code from the given TypeMapEntry {@code typeInfo}
	 * @param typeInfo - the defining or derived TypeMapInfo of the type 
	 * @return a String suited as Pascal type description in declarations etc. 
	 */
	@Override
	protected String transformTypeFromEntry(TypeMapEntry typeInfo, TypeMapEntry definingWithin) {
		// Record type descriptions won't usually occur here (rather names)
		String _typeDescr;
//		String canonType = typeInfo.getTypes().get(0);
		String canonType = typeInfo.getCanonicalType(true, true);
		int nLevels = canonType.lastIndexOf('@')+1;
		String elType = (canonType.substring(nLevels)).trim();
		elType = transformType(elType, "(*???*)");
		_typeDescr = "";
		for (int i = 0; i < nLevels; i++) {
			_typeDescr += "ARRAY ";
			int maxIndex = typeInfo.getMaxIndex(i);
			if (maxIndex >= 0) {
				_typeDescr += (maxIndex + 1) + " ";
			}
			_typeDescr += "OF ";
		}
		_typeDescr += elType;
		return _typeDescr;
	}
	// END KGU#140/KGU#388 2017-10-24

	// START KGU#93 2015-12-21: Bugfix #41/#68/#69
//	/**
//	 * Transforms assignments in the given intermediate-language code line.
//	 * Replaces "<-" by ":=" here
//	 * @param _interm - a code line in intermediate syntax
//	 * @return transformed string
//	 */
//	@Deprecated
//	protected String transformAssignment(String _interm)
//	{
//		return _interm.replace(" <- ", " := ");
//	}
	// END KGU#18/KGU#23 2015-11-01

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformTokens(lu.fisch.utils.StringList)
	 */
	@Override
	protected String transformTokens(StringList tokens)
	{
		tokens.replaceAll("==", "=");
		tokens.replaceAll("!=","#");
		// C and Pascal division operators
		tokens.replaceAll("div","DIV");
		tokens.replaceAll("%"," MOD ");
		// logical operators required transformation, too
		tokens.replaceAll("&&","&");
		tokens.replaceAll("||"," OR ");
		tokens.replaceAll("!","~");
		tokens.replaceAll("<-", ":=");
		// START KGU#150 2016-04-03: Support for ord and chr function and capitalization
		String[] functionNames = {"abs", "inc", "dec", "chr", "ord", "uppercase"};
		for (int i = 0; i < functionNames.length; i++)
		{
			int pos = -1;
			while ((pos = tokens.indexOf(functionNames[i], pos+1)) >= 0 &&
					pos+1 < tokens.count() &&
					tokens.get(pos+1).equals("("))
			{
				tokens.set(pos, functionNames[i].toUpperCase());
			}
		}
		tokens.replaceAll("UPPERCASE", "CAP");
		// END KGU#15ß 2016-04-03
		String result = tokens.concatenate();
		// We now shrink superfluous padding - this may affect string literals, though!
		result = result.replace("  ", " ");
		result = result.replace("  ", " ");	// twice to catch odd-numbered space sequences, too
		return result;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transform(java.lang.String, boolean)
	 */
	@Override
	protected String transform(String _input, boolean _doInputOutput)
	{
		// START KGU#18/KGU#23 2015-11-02
		String transline = super.transform(_input, _doInputOutput);
		// END KGU#18/KGU#23 2015-11-02

// START KGU#93 205-12-21: No longer needed (Bugfix #41/#68/#69)
//		// START KGU 2014-11-16: Comparison operator had to be converted properly first
//		_input=BString.replace(_input," == "," = ");
//		_input=BString.replace(_input," != "," # ");
//		_input=BString.replace(_input," <> "," # ");
//		// C and Pascal division operators
//		_input=BString.replace(_input," div "," DIV ");
//		_input=BString.replace(_input," % "," MOD ");
//		// logical operators required transformation, too
//		_input=BString.replace(_input," && "," & ");
//		_input=BString.replace(_input," || "," OR ");
//		_input=BString.replace(_input," ! "," ~ ");
//		_input=BString.replace(_input,"!"," ~ ");
//		// END KGU 2014-11-16
// END KGU#93 2015-12-21
		// START KGU#162 2016-04-29: Bugfix for enh. #144
		if (!this.suppressTransformation)
		{
		// END KGU#162 2016-04-29
			int asgnPos = transline.indexOf(":=");
			// START KGU#109/KGU#141 2016-01-16: Bugfix #61,#112 - suppress type specifications
			if (asgnPos >= 0)
			{
				String varName = transline.substring(0, asgnPos).trim();
				String expr = transline.substring(asgnPos+2).trim();
				String[] typeNameIndex = this.lValueToTypeNameIndexComp(varName);
				varName = typeNameIndex[1];
				String index = typeNameIndex[2];
				if (!index.isEmpty())
				{
					varName = varName + "["+index+"]";
				}
				// START KGU#388 2017-09-27: Enh. #423 Add found qualifiers (always at end??)
				varName += typeNameIndex[3];
				// END KGU#388 2017-09-27: Enh. #423
				transline = varName + " := " + expr;
			}
			// END KGU#109/KGU#141 2016-01-16
		// START KGU#162 2016-04-29: Bugfix for enh. #144
		}
		// END KGU#162 2016-04-29

		return transline.trim();
	}
	

	// START KGU#61 2016-03-23: New for enh. #84
	private void insertDeclaration(String _category, String text, int _maxIndent)
	{
		int posDecl = -1;
		String seekIndent = "";
		while (posDecl < 0 && seekIndent.length() < _maxIndent)
		{
			posDecl = code.indexOf(seekIndent + _category);
			seekIndent += this.getIndent();
		}
		// START KGU#332 2017-01-30: Enh. #335 Enables const declarations
		if (posDecl < 0 && _category.equals("CONST")) {
			seekIndent = "";
			while (posDecl < 0 && seekIndent.length() < _maxIndent)
			{
				posDecl = code.indexOf(seekIndent + "VAR");
				if (posDecl >= 0) {
					insertCode("", posDecl);
					insertCode(seekIndent + _category, posDecl);
				}
				seekIndent += this.getIndent();
			}
		}
		// END KGU#332 2017-01-30
		insertCode(seekIndent + text, posDecl + 1);
	}
	// END KGU#61 2016-03-23

	
	protected void generateCode(Instruction _inst, String _indent)
	{
		// START KGU 2015-10-18: The "export instructions as comments" configuration had been ignored here
//		insertComment(_inst, _indent);
//		for(int i=0;i<_inst.getText().count();i++)
//		{
//			code.add(_indent+transform(_inst.getText().get(i))+";");
//		}
		if (!appendAsComment(_inst, _indent)) {
			
			boolean isDisabled = _inst.isDisabled();
			
			appendComment(_inst, _indent);

			String outputKey = CodeParser.getKeyword("output");
			StringList lines = _inst.getUnbrokenText();
			for (int i=0; i<lines.count(); i++)
			{
				// START KGU#101/KGU#108 2015-12-20 Issue #51/#54
				//code.add(_indent+transform(_inst.getText().get(i))+";");
				String line = lines.get(i);
				// START KGU#236 2016-08-10: Issue #227: Simplification by delegation
//				String matcherInput = "^" + getKeywordPattern(CodeParser.input);
//				String matcherOutput = "^" + getKeywordPattern(CodeParser.output);
//				if (Character.isJavaIdentifierPart(CodeParser.input.charAt(CodeParser.input.length()-1))) { matcherInput += "[ ]"; }
//				if (Character.isJavaIdentifierPart(CodeParser.output.charAt(CodeParser.output.length()-1))) { matcherOutput += "[ ]"; }
//				boolean isInput = (line.trim()+" ").matches(matcherInput + "(.*)");			// only non-empty input instructions relevant  
//				boolean isOutput = (line.trim()+" ").matches(matcherOutput + "(.*)"); 	// also empty output instructions relevant
				// END KGU#236 2016-08-10
				if (Instruction.isInput(line))
				{
					// START KGU#236 2016-08-10: Issue #227 - moved to the algorithm start now
					//code.add(_indent + "In.Open;");
					// END KGU#236 2016-08-10
					// START KGU#281 2016-10-15: Enh. #271
					//if (line.substring(inputKey.length()).trim().isEmpty())
					//{
					//	addCode("In.Char(dummyInputChar);", _indent, isDisabled);
					//}
					//else
					//{	
					//	insertComment("TODO: Replace \"TYPE\" by the the actual In procedure name for this type!", _indent);
					//	addCode(transform(line) + ";", _indent, isDisabled);
					//}
					// START KGU#653 2019-02-14: Enh. #680 - there may be several variables in an input instruction since 3.29-03
					//String transf = transform(line).replace("In.TYPE()", "In.Char(dummyInputChar)") + ";";
					//if (transf.contains("In.TYPE(")) {
					//	insertComment("TODO: Replace \"TYPE\" by the the actual In procedure name for this type!", _indent);
					//}
					//addCode(transf, _indent, isDisabled);
					StringList inputItems = Instruction.getInputItems(line);
					if (inputItems.count() > 2) {
						String inputKey = CodeParser.getKeyword("input");
						String prompt = inputItems.get(0);
						if (!prompt.isEmpty()) {
							generateTypeSpecificOutput(prompt, _indent, isDisabled, outputKey);
						}
						//appendComment("TODO: Replace \"TYPE\" by the the actual In procedure name for the respective type!", _indent);
						for (int j = 1; j < inputItems.count(); j++) {
							String inputItem = inputItems.get(j);
							// START KGU#761 2019-11-12: Enh. #775 - slightly more intelligence is feasible here
							//addCode(transform(inputKey + " \"" + inputItem + "\" " + inputItem), _indent, isDisabled);
							addCode(specifyInputMethod(_indent, inputItem, transform(inputKey + " \"" + inputItem + "\" " + inputItem)), _indent, isDisabled);
							// END KGU#761 2019-11-12
						}
					}
					else {
						String transf = transform(line).replace("In.TYPE()", "In.Char(dummyInputChar)") + ";";
						if (transf.contains("In.TYPE(")) {
							// START KGU#761 2019-11-12: Enh. #775 - slightly more intelligence is feasible here
							//appendComment("TODO: Replace \"TYPE\" by the the actual In procedure name for this type!", _indent);
							transf = specifyInputMethod(_indent, inputItems.get(1), transf);
							// END KGU#761 2019-11-12
						}
						addCode(transf, _indent, isDisabled);
					}
					// END KGU#653 2019-02-14
					// END KGU#281 2016-10-15
				}
				else if (Instruction.isOutput(line))
				{
					StringList expressions = Element.splitExpressionList(line.substring(outputKey.length()).trim(), ",");
					// Produce an output instruction for every expression (according to type)
					for (int j = 0; j < expressions.count(); j++)
					{
						// START KGU#236 2016-10-15: Issue #227 - For literals, we can of course determine the type...
						//addCode(transform(outputKey + " " + expressions.get(j)) + ";", _indent, isDisabled);
						generateTypeSpecificOutput(expressions.get(j), _indent, isDisabled, outputKey);
						// END KGU#236 2016-10-15
					}
					addCode("Out.Ln;", _indent, isDisabled);
				}
				// START KGU#388 2017-10-24: Enh. #423 suppress type definitions here
				//else
				// START KGU#504 2018-03-13: Bugfix #520, #521 - consider transformation suppression
				//else if (!Instruction.isTypeDefinition(line))
				else if (this.suppressTransformation || !Instruction.isTypeDefinition(line))
				// START KGU#504 2018-03-13: Bugfix #520, #521 - consider transformation suppression
				// END KGU#388 2017-10-24
				{
					// START KGU#100/#KGU#141 2016-01-16: Enh. #84 + Bugfix #112 - array handling
					//code.add(_indent + transform(line) + ";");
					boolean isConstant = line.toLowerCase().startsWith("const ");
					String transline = transform(line);
					int asgnPos = transline.indexOf(":=");
					boolean isComplexInit = false;
					// START KGU#100 2016-01-16: Enh. #84 - resolve array initialisation
					// START KGU#504 2018-03-13: Bugfix #520, #521
					//if (asgnPos >= 0 && transline.contains("{") && transline.contains("}"))
					if (!this.suppressTransformation && asgnPos >= 0 && transline.contains("{") && transline.contains("}"))
					// END KGU#504 2018-03-13
					{
						String varName = transline.substring(0, asgnPos).trim();
						String expr = transline.substring(asgnPos+":=".length()).trim();
						int bracePos = expr.indexOf("{");
						isComplexInit = bracePos == 0 && expr.endsWith("}");
						if (isComplexInit)
						{
							// START KGU#560 2018-07-22: Bugfix #564
							if (Instruction.isDeclaration(line) && varName.contains("[")) {
								varName = varName.substring(0, varName.indexOf('['));
							}
							// END KGU#56 2018-07-22
							// START KGU#100 2017-10-24: Enh. #84
//							StringList elements = Element.splitExpressionList(
//									expr.substring(1, expr.length()-1), ",");
//							for (int el = 0; el < elements.count(); el++)
//							{
//								addCode(varName + "[" + el + "] := " + elements.get(el) + ";",
//										_indent, isDisabled);
//							}
							StringList elements = Element.splitExpressionList(
									expr.substring(1, expr.length()-1), ",");
							this.generateArrayInit(varName, elements, _indent, isDisabled);
							// END KGU#100 2017-10-24
						}
						// START KGU#388 2017-10-24: Enh. #423 cope with record initializers
						else if (bracePos > 0 && expr.endsWith("}")
								&& Function.testIdentifier(expr.substring(0, bracePos), null))
						{
							isComplexInit = true;
							// START KGU#559 2018-07-22: Enh. #563
							//this.generateRecordInit(varName, expr, _indent, isConstant, isDisabled);
							this.generateRecordInit(varName, expr, _indent, isConstant, isDisabled, typeMap.get(":"+expr.substring(0, bracePos)));
							// END KGU#559 2018-07-22
						}
						// END KGU#388 2017-10-24
					}
					// non-complex constants will already have been defined
					if (!isComplexInit && !isConstant)
					{
						// START KGU#277/KGU#284 2016-10-13/16: Enh. #270 + Enh. #274
						//code.add(_indent + transline + ";");
						transline += ";";
						if (Instruction.isTurtleizerMove(line)) {
							transline += " " + this.commentSymbolLeft() + " color = " + _inst.getHexColor() + " " + this.commentSymbolRight();
						}
						// START KGU 2017-01-31: return must be capitalized here
						transline = transline.replaceFirst("^" + BString.breakup(CodeParser.getKeywordOrDefault("preReturn", "return")) + "($|\\W+.*)", "RETURN$1");
						// END KGU 2017-01-31
						// START KGU#261/KGU#504 2018-03-13: Enh. #259/#335, bugfix #521
						//addCode(transline, _indent, isDisabled);
						if (!this.suppressTransformation && transline.matches("^(var|dim) .*")) {
							if (asgnPos > 0) {
								// First remove the "var" or "dim" key word
								String separator = transline.startsWith("var") ? ":" : " as ";
								transline = transline.substring(4);
								int posColon = transline.substring(0, asgnPos).indexOf(separator);
								if (posColon > 0) {
									transline = transline.substring(0, posColon) + transline.substring(asgnPos);
								}
							}
							else {
								// No initialization - so ignore it here
								// (the declaration will appear in the var section)
								transline = null;
							}
						}
						if (transline != null) {
							addCode(transline, _indent, isDisabled);
						}
						// END KGU#261/KGU#504 2018-03-13
						// END KGU#277/KGU#284 2016-10-13
					}
					// END KGU#100 2016-01-16
				}
				// END KGU#101/KGU#108 2015-12-20
			}

		}
		// END KGU 2015-10-18
	}
	
	// START KGU#761 2019-11-12: Enh. #775 - slightly more intelligence is feasible here
	/**
	 * Tries to replace the placeholder `TYPE' in the transformed default input phrase {@code transf}
	 * by a type-specific actual method name for the type of the variable given by {@code varName}.
	 * @param _indent - current indentation level
	 * @param varName - input variable string (might of course be an array element or a record component)
	 * @param transf - the general transformed input instruction, presumably with placeholder "In.TYPE(..."
	 * @return a revised (and more specific) input instruction
	 */
	private String specifyInputMethod(String _indent, String varName, String transf) {
		boolean done = false;
		TypeMapEntry type = this.typeMap.get(varName);
		if (type != null) {
			String typeName = type.getCanonicalType(true, true);
			if (typeName != null && !typeName.trim().isEmpty() && !typeName.contains("???")) {
				typeName = this.transformType(typeName, "TYPE");
				done = true;
				if (typeName.equals("INTEGER") || typeName.equals("SHORTINT")) {
					transf = transf.replace("In.TYPE(", "In.Int(");
				}
				else if (typeName.equals("LONGINT")) {
					transf = transf.replace("In.TYPE(", "In.LongInt(");
				}
				else if (typeName.equals("CHAR")) {
					transf = transf.replace("In.TYPE(", "In.Char(");
				}
				else if (typeName.endsWith("REAL")) {
					transf = transf.replace("In.TYPE(", "In.Real(");
				}
				else if (typeName.equals("ARRAY 100 OF CHAR")) {
					transf = transf.replace("In.TYPE(", "In.String(");
				}
				else {
					done = false;
				}
			}
		}
		if (!done) {
			appendComment("TODO: Replace \"TYPE\" by the the actual In procedure name for this type!", _indent);
		}
		return transf;
	}
	// END KGU#761 2019-11-12

	// START KGU#236 2016-10-15: Issue #227 - For literals, we can of course determine the type...
	/**
	 * Generates an Oberon output command for expression {@code expr}
	 * @param _expression - the expression the value of which is to be output
	 * @param _indent - current source line indentation (as string)
	 * @param _isDisabled - whether the element is disabled
	 * @param _outputKey -
	 */
	protected void generateTypeSpecificOutput(String _expression, String _indent, boolean _isDisabled,
			String _outputKey) {
		String procName = "";
		String length = "";
		try {
			Double.parseDouble(_expression);
			procName = "Real";
			length = ", 10";
		}
		catch (NumberFormatException ex) {}
		try {
			Integer.parseInt(_expression);
			procName = "Int";
			length = ", 10";
		}
		catch (NumberFormatException ex) {}
		if (procName.isEmpty() && (_expression.startsWith("\"") || _expression.startsWith("'"))
				&& Element.splitLexically(_expression, true).count() == 1) {
			procName = "String";
		}
		// START KGU#332 2017-01-30: Enh. #335 Identify variable types if possible
		if (procName.isEmpty() && Function.testIdentifier(_expression, ".")) {
			// START KGU#388 2017-10-24: Enh. 423
			//TypeMapEntry typeInfo = typeMap.get(expr);
			String[] nameParts = _expression.split("[.]");
			String topVar = nameParts[0];
			TypeMapEntry typeInfo = typeMap.get(topVar);
			int level = 1;
			while (typeInfo != null && level < nameParts.length) {
				if (typeInfo.isRecord()) {
					LinkedHashMap<String, TypeMapEntry> compInfo = typeInfo.getComponentInfo(false);
					if (compInfo != null) {
						typeInfo = compInfo.get(nameParts[level++]);
					}
					else {
						typeInfo = null;
					}
				}
				else {
					typeInfo = null;
				}
			}
			// END KGU#388 2017-10-24
			if (typeInfo != null) {
				StringList types = this.getTransformedTypes(typeInfo, false);
				if (types.count() == 1) {
					String type = types.get(0);
					// START KGU#542 2019-11-21: Enh. #739 - also accept enum type here
					//if (type.equals("INTEGER") || type.equals("LONGINT") || type.equals("SHORTINT")) {
					if (typeInfo.isEnum() || type.equals("INTEGER") || type.equals("LONGINT") || type.equals("SHORTINT")) {
					// END KGU#542 2019-11-231
						procName = "Int";
						length = ", 10";
					}
					else if (type.equals("REAL") || type.equals("LONGREAL")) {
						procName = "Real";
						length = ", 10";										
					}
					else if (type.equalsIgnoreCase("STRING") || type.matches("ARRAY(\\s\\d+)? OF CHAR")) {
						procName = "String";
					}
					else if (type.equals("CHAR")) {
						procName = "Char";
					}
				}
			}
		}
		// END KGU#332 2017-01-30
		String codeLine = transform(_outputKey + " " + _expression).replace("%LEN%", length) + ";";
		if (!procName.isEmpty()) {
			codeLine = codeLine.replace("Out.TYPE(", "Out."+procName+"(");
		}
		else {
			appendComment("TODO: Replace \"TYPE\" by the the actual Out procedure name for this type and add a length argument where needed!", _indent);
		}
		addCode(codeLine, _indent, _isDisabled);
	}
	// END KGU#236 2016-10-15
	
	// START KGU#388 2017-10-23: Enh. #423 (copied from PasGenerator)
	/**
	 * Appends the code for an array initialisation of variable {@code _varName} from
	 * the pre-transformed expression {@code _expr}.
	 * @param _varName - name of the variable to be initialized
	 * @param _elemExprs - transformed initializer
	 * @param _indent - current indentation string
	 * @param _isDisabled - whether the source element is disabled (means to comment out the code)
	 */
	private void generateArrayInit(String _varName, StringList _elements, String _indent, boolean _isDisabled) {
		appendComment("Hint: Automatically decomposed array initialization", _indent);
		if (_varName.matches("\\w+\\[.*\\]")) {
			_varName = _varName.replace("]", ", ");
		}
		else {
			_varName = _varName + "[";
		}
		for (int ix = 0; ix < _elements.count(); ix++)
		{
			// START KGU#560 2018-07-22: Bugfix #564
			//addCode(_varName + ix + "] := " + 
			//		_elements.get(ix) + ";",
			//		_indent, _isDisabled);
			generateAssignment(_varName + ix + "]", _elements.get(ix), _indent, _isDisabled);
			// END KGU#560 2018-07-22
		}
	}

	/**
	 * Appends the code for a record initialisation of variable {@code _varName} from
	 * the pre-transformed expression {@code _expr}.
	 * @param _varName - name of the variable to be initialized
	 * @param _expr - transformed initializer
	 * @param _indent - current indentation string
	 * @param _forConstant - whether this initializer is needed for a constant (a variable otherwise)
	 * @param _isDisabled - whether the source element is disabled (means to comment out the code)
	 * @param _recType - used for component name retrieval if the initializer omits them (may be null)
	 */
	private void generateRecordInit(String _varName, String _expr, String _indent, boolean _forConstant, boolean _isDisabled, TypeMapEntry _recType)
	{
		// START KGU#559 2018-07-22: Enh. #563
		//HashMap<String, String> components = Instruction.splitRecordInitializer(_expr);
		HashMap<String, String> components = Instruction.splitRecordInitializer(_expr, _recType, false);
		// END KGU#559 2018-07-22
		if (_forConstant) {
			appendComment("Note: " + _varName + " was meant to be a record CONSTANT...", _indent);
		}
		for (Entry<String, String> comp: components.entrySet())
		{
			String compName = comp.getKey();
			if (!compName.startsWith("§")) {
				// START KGU#560 2018-07-22: Enh. #564 - on occasion of #563, we fix recursive initializers, too
				//addCode(_varName + "." + compName + " := " + comp.getValue() + ";",
				//		_indent, _isDisabled);
				generateAssignment(_varName + "." + compName, comp.getValue(), _indent, _isDisabled);
				// END KGU#560 2018-07-22
			}
		}
	}
	// END KGU#388 2017-10-24
   
	// START KGU#560 2018-07-22: Bugfix #564 Array initializers have to be decomposed if not occurring in a declaration
	/**
	 * Generates code that decomposes possible initializers into a series of separate assignments if
	 * there no compact translation, otherwise just adds appropriate transformed code.
	 * @param _target - the left side of the assignment (without modifiers!)
	 * @param _expr - the expression in Structorizer syntax
	 * @param _indent - current indentation level (as String)
	 * @param _isDisabled - indicates whether the code is o be commented out
	 */
    private void generateAssignment(String _target, String _expr, String _indent, boolean _isDisabled) {
		if (_expr.contains("{") && _expr.endsWith("}")) {
			StringList pureExprTokens = Element.splitLexically(_expr, true);
			pureExprTokens.removeAll(" ");
			int posBrace = pureExprTokens.indexOf("{");
			if (pureExprTokens.count() >= 3 && posBrace <= 1) {
				if (posBrace == 1 && Function.testIdentifier(pureExprTokens.get(0), null)) {
					// Record initializer
					String typeName = pureExprTokens.get(0);							
					TypeMapEntry recType = this.typeMap.get(":"+typeName);
					this.generateRecordInit(_target, _expr, _indent, false, _isDisabled, recType);
				}
				else {
					// Array initializer
					StringList items = Element.splitExpressionList(pureExprTokens.subSequence(1, pureExprTokens.count()-1), ",", true);
					this.generateArrayInit(_target, items.subSequence(0, items.count()-1), _indent, _isDisabled);
				}
			}
			else {
				addCode(_target + " := " + _expr + ";", _indent, _isDisabled);
			}
		}
		else {
			addCode(_target + " := " + _expr + ";", _indent, _isDisabled);
		}
	}
	// END KGU#560 2018-07-22

	protected void generateCode(Alternative _alt, String _indent)
	{
		boolean isDisabled = _alt.isDisabled();
		// START KGU 2014-11-16
		appendComment(_alt, _indent);
		// END KGU 2014-11-16
		// START KGU#453 2017-11-02: Issue #447
		//addCode("IF "+ transform(_alt.getText().getLongString()) + " THEN",
		addCode("IF "+ transform(_alt.getUnbrokenText().getLongString()) + " THEN",
		// END KGU#453 2017-1102
				_indent, isDisabled);
		generateCode(_alt.qTrue, _indent+this.getIndent());
		if (_alt.qFalse.getSize()!=0)
		{
			addCode("END", _indent, isDisabled);
			addCode("ELSE", _indent, isDisabled);
			generateCode(_alt.qFalse, _indent+this.getIndent());
		}
		addCode("END;", _indent, isDisabled);
	}

	protected void generateCode(Case _case, String _indent)
	{
		boolean isDisabled = _case.isDisabled();
		// START KGU 2014-11-16
		appendComment(_case, _indent);
		// END KGU 2014-11-16
		// START KGU#453 2017-11-02: Issue #447
		//addCode("CASE "+transform(_case.getText().get(0))+" OF", _indent, isDisabled);
		StringList unbrokenText = _case.getUnbrokenText();
		addCode("CASE " + transform(unbrokenText.get(0)) + " OF", _indent, isDisabled);
		// END KGU#453 2017-11-02
		
		for (int i=0; i<_case.qs.size()-1; i++)
		{
			// START KGU#453 2017-11-02: Issue #447
			//addCode(this.getIndent() + _case.getText().get(i+1).trim() + ":",
			addCode(this.getIndent() + unbrokenText.get(i+1).trim() + ":",
			// END KGU#453 2017-11-02
					_indent, isDisabled);
			generateCode((Subqueue) _case.qs.get(i),_indent+this.getIndent());
			// START KGU 2014-11-16: Wrong case separator replaced
			//code.add(_indent+"END;");
			addCode("|", _indent, isDisabled);
			// END KGU 2014-11-16
		}
		
		// START KGU#453 2017-11-02: Issue #447
		//if(!_case.getText().get(_case.qs.size()).trim().equals("%"))
		if (!unbrokenText.get(_case.qs.size()).trim().equals("%"))
		// END KGU#453 2017-11-02
		{
			addCode("ELSE", _indent+this.getIndent(), isDisabled);
			generateCode((Subqueue) _case.qs.get(_case.qs.size()-1),_indent+this.getIndent()+this.getIndent());
		}
		// START KGU 2014-11-16: Wrong indentation mended
		//code.add(_indent+this.getIndent()+"END;");
		addCode("END;", _indent, isDisabled);
		// END KGU 2014-11-16
	}
	
	protected void generateCode(For _for, String _indent)
	{
		// START KGU 2014-11-16
		appendComment(_for, _indent);
		// END KGU 2014-11-16

		// START KGU#61 2016-03-23: Enh. 84
		if (_for.isForInLoop() && generateForInCode(_for, _indent))
		{
			// All done
			return;
		}
		// END KGU#61 2016-03-23

		boolean isDisabled = _for.isDisabled();

		// START KGU#3 2015-11-02: New reliable loop parameter mechanism
		//code.add(_indent+"FOR "+BString.replace(transform(_for.getText().getText()),"\n","")+" DO");
		int step = _for.getStepConst();
		String incr = (step == 1) ? "" : " BY "+ step;
		addCode("FOR " + _for.getCounterVar() + " := " + transform(_for.getStartValue(), false) +
				" TO " + transform(_for.getEndValue(), false) + incr +" DO",
				_indent, isDisabled);
		// END KGU#3 2015-11-02
		generateCode(_for.q,_indent+this.getIndent());
		addCode("END;", _indent, isDisabled);
	}

	// START KGU#61 2016-03-23: Enh. #84 - Support for FOR-IN loops
	/**
	 * We try our very best to create a working loop from a FOR-IN construct
	 * This will only work, however, if we can get reliable information about
	 * the size of the value list, which won't be the case if we obtain it e.g.
	 * via a variable.
	 * @param _for - the element to be exported
	 * @param _indent - the current indentation level
	 * @return true iff the method created some loop code (sensible or not)
	 */
	protected boolean generateForInCode(For _for, String _indent)
	{
		boolean done = false;
		String var = _for.getCounterVar();
		StringList items = this.extractForInListItems(_for);
		// Create some generic and unique variable names
		String postfix = Integer.toHexString(_for.hashCode());
		String arrayName = "array" + postfix;
		String indexName = "index" + postfix;
		String itemType = "FIXME_" + postfix;
		boolean isDisabled = _for.isDisabled();

		if (items != null)
		{
			// Good question is: how do we guess the element type and what do we
			// do if items are heterogenous? We will just try five types: boolean,
			// common enum type, integer, real and string, where we can only test literals.
			// If none of them match then we add a TODO comment.
			int nItems = items.count();
			// START KGU#542 2019-11-21: Enh. #739
			String allEnum = "";
			// END KGU#542 2019-11-21
			boolean allBoolean = true;
			boolean allInt = true;
			boolean allReal = true;
			boolean allString = true;
			for (int i = 0; i < nItems; i++)
			{
				String item = items.get(i);
				// START KGU#542 2019-11-21: Enh. #739
				TypeMapEntry tme = this.typeMap.get(item);
				// END KGU#542 2019-11-21
				if (allBoolean)
				{
					if (!item.equalsIgnoreCase("true") && !item.equalsIgnoreCase("false"))
					{
						allBoolean = false;
					}
				}
				if (allInt)
				{
					try {
						Integer.parseInt(item);
					}
					catch (NumberFormatException ex)
					{
						// START KGU#542 2019-11-21: Enh. #739 enum type support - it might be an enumerator constant
						//allInt = false;
						allInt = tme !=  null && tme.isEnum();
						// END KGU#542 2019-11-21
					}
				}
				if (allReal)
				{
					try {
						Double.parseDouble(item);
					}
					catch (NumberFormatException ex)
					{
						allReal = false;
					}
				}
				if (allString)
				{
					allString = item.startsWith("\"") && item.endsWith("\"") &&
							!item.substring(1, item.length()-1).contains("\"") ||
							item.startsWith("\'") && item.endsWith("\'") &&
							!item.substring(1, item.length()-1).contains("\'");
				}
				// START KGU#542 2019-11-21: Enh. #739 support for enumerator types
				if (allEnum != null)
				{
					if (tme != null && tme.isEnum()) {
						if (allEnum.isEmpty()) {
							allEnum = tme.typeName;
						}
						else if (!allEnum.equals(tme.typeName)) {
							allEnum = null;	// Game over for enumerator (different enumerators)
						}
					}
					else {
						// Obviously no enumerator constant
						allEnum = null;
					}
				}
				// END KGU#542 2019-11-21
			}
			
			if (allBoolean) itemType = "BOOLEAN";
			// START KGU#542 2019-11-21: Enh. #739
			else if (allEnum != null && !allEnum.isEmpty()) itemType = allEnum;
			// END KGU#542 2019-11-21
			else if (allInt) itemType = "INTEGER";
			else if (allReal) itemType = "REAL";
			else if (allString) itemType = "ARRAY 100 OF CHAR";
			else {
				// We do a dummy type definition
				this.appendComment("TODO: Specify an appropriate element type for the array!", _indent);
			}

			// Insert the array and index declarations
			insertDeclaration("VAR", arrayName + ": " + "ARRAY " + 
					nItems + " OF " + itemType + ";", _indent.length());
			insertDeclaration("VAR", indexName + ": INTEGER;",
					_indent.length());

			// Now we create code to fill the array with the enumerated values
			for (int i = 0; i < nItems; i++)
			{
				// START KGU#369 2017-03-15: Bugfix #382 item transformation had been missing
				//addCode(arrayName + "[" + i + "] := " + items.get(i) + ";",
				addCode(arrayName + "[" + i + "] := " + transform(items.get(i)) + ";",
				// END KGU#369 2017-03-15
						_indent, isDisabled);
			}
			
			// Creation of the loop header
			addCode("FOR " + indexName + " := 0 TO " + (nItems-1) + " DO", _indent, isDisabled);

			// Creation of the loop body
			addCode(var + " := " + arrayName + "[" + indexName + "];", _indent+this.getIndent(), isDisabled);
			generateCode(_for.q, _indent+this.getIndent());
			addCode("END;", _indent, isDisabled);

			done = true;
		}
		else
		{
			String valueList = _for.getValueList();
			// Fortunately, there is a predefined function LEN in Oberon that makes it possible
			// to convert this in to a COUNTER loop. We just need a generic index variable
			if (Function.isFunction(valueList))
			{
				// For performance reasons, it wouldn't be so good an idea to call the function all the way again
				this.insertDeclaration("VAR", arrayName + ": ARRAY OF " + itemType + ";", _indent.length());
				addCode(arrayName + " := " + valueList, _indent, isDisabled);
				valueList = arrayName;
			}
			insertDeclaration("VAR", indexName + ": INTEGER;", _indent.length());
			// Creation of the loop header
			addCode("FOR " + indexName + " := 0 TO LEN(" + transform(valueList, false) + ")-1 DO",
					_indent, isDisabled);
			addCode(var + " := " + valueList + "[" + indexName + "];",
					_indent+this.getIndent(), isDisabled);
			// Add the loop body as is
			generateCode(_for.q, _indent + this.getIndent());
			addCode("END;", _indent, isDisabled);

			done = true;
		}
		return done;
	}
	// END KGU#61 2016-03-23

	protected void generateCode(While _while, String _indent)
	{
		boolean isDisabled = _while.isDisabled();
		// START KGU 2014-11-16
		appendComment(_while, _indent);
		// END KGU 2014-11-16
//		addCode("WHILE "+BString.replace(transform(_while.getUnbrokenText().getText()),"\n","")+" DO",
//				_indent, isDisabled);
		addCode("WHILE " + transform(_while.getUnbrokenText().getLongString()) + " DO",
				_indent, isDisabled);
		generateCode(_while.q, _indent + this.getIndent());
		addCode("END;", _indent, isDisabled);
	}
	
	protected void generateCode(Repeat _repeat, String _indent)
	{
		boolean isDisabled = _repeat.isDisabled();
		// START KGU 2014-11-16
		appendComment(_repeat, _indent);
		// END KGU 2014-11-16
		addCode("REPEAT", _indent, isDisabled);
		generateCode(_repeat.q,_indent+this.getIndent());
//		addCode("UNTIL "+BString.replace(transform(_repeat.getUnbrokenText().getText()),"\n","")+";",
//				_indent, isDisabled);
		addCode("UNTIL " + transform(_repeat.getUnbrokenText().getLongString()) + ";",
				_indent, isDisabled);
	}
	
	protected void generateCode(Forever _forever, String _indent)
	{
		boolean isDisabled = _forever.isDisabled();
		// START KGU 2014-11-16
		appendComment(_forever, _indent);
		// END KGU 2014-11-16
		addCode("LOOP", _indent, isDisabled);
		generateCode(_forever.q,_indent+this.getIndent());
		addCode("END;", _indent, isDisabled);
	}
	
	protected void generateCode(Call _call, String _indent)
	{
		boolean isDisabled = _call.isDisabled();
		// START KGU 2014-11-16
		appendComment(_call, _indent);
		// END KGU 2014-11-16
		Root owningRoot = Element.getRoot(_call);
		StringList lines = _call.getUnbrokenText();
		for (int i = 0; i < lines.count(); i++)
		{
			// START KGU#371 2019-03-08: Enh. #385 Support for declared optional arguments
			//addCode(transform(lines.get(i))+";", _indent, isDisabled);
			String line = lines.get(i);
			// START KGU#766 2019-11-14: Issue #780 Omit empty parentheses on procedure calls.
			boolean isFctCall = Instruction.isAssignment(line);
			// END KGU#766 2019-11-14
			if (i == 0 && this.getOverloadingLevel() == OverloadingLevel.OL_NO_OVERLOADING && (routinePool != null) && line.endsWith(")")) {
				Function call = _call.getCalledRoutine();
				java.util.Vector<Root> callCandidates = routinePool.findRoutinesBySignature(call.getName(), call.paramCount(), owningRoot);
				if (!callCandidates.isEmpty()) {
					// FIXME We'll just fetch the very first one for now...
					Root called = callCandidates.get(0);
					StringList defaults = new StringList();
					called.collectParameters(null, null, defaults);
					if (defaults.count() > call.paramCount()) {
						// We just insert the list of default values for the missing arguments
						line = line.substring(0, line.length()-1) + (call.paramCount() > 0 ? ", " : "") + 
								defaults.subSequence(call.paramCount(), defaults.count()).concatenate(", ") + ")";
					}
					// START KGU#766 2019-11-14: Issue #780 Check if it might yet be a function
					if (called.getResultType() != null) {
						isFctCall = true;
					}
					// END KGU#766 2019-11-14
				}
			}
			// START KGU#766 2019-11-14: Issue #780 Omit empty parentheses on procedure calls.
			if (!isFctCall && line.endsWith("()")) {
				line = line.substring(0, line.length()-2);
			}
			// END KGU#766 2019-11-14
			addCode(transform(line)+";", _indent, isDisabled);
			// END KGU#371 2019-03-08
		}
	}
	
	protected void generateCode(Jump _jump, String _indent)
	{
		boolean isDisabled = _jump.isDisabled();
		// START KGU 2014-11-16
		appendComment(_jump, _indent);
		// END KGU 2014-11-16

		// START KGU#74/KGU#78 2016-01-17: actual jump handling
		//for(int i=0;i<_jump.getText().count();i++)
		//{
		//	code.add(_indent+transform(_jump.getText().get(i))+";");
		//}
		// Only EXIT (= break) and RETURN exist, no further jump allowed
		boolean isEmpty = true;

		StringList lines = _jump.getUnbrokenText();
		for (int i = 0; isEmpty && i < lines.count(); i++) {
			String line = transform(lines.get(i)).trim();
			if (!line.isEmpty())
			{
				isEmpty = false;
			}
			// START KGU#74/KGU#78 2015-11-30: More sophisticated jump handling
			//code.add(_indent + line + ";");
			String preReturn = CodeParser.getKeywordOrDefault("preReturn", "return");
			String preExit   = CodeParser.getKeywordOrDefault("preExit", "exit");
			String preLeave  = CodeParser.getKeywordOrDefault("preLeave", "leave");
			if (line.matches(Matcher.quoteReplacement(preReturn)+"([\\W].*|$)"))
			{
				addCode("RETURN " + line.substring(preReturn.length()).trim() + ";",
						_indent, isDisabled);
			}
			else if (line.matches(Matcher.quoteReplacement(preExit)+"([\\W].*|$)"))
			{
				appendComment("FIXME: Find a solution to exit the program here!", _indent);
				appendComment(line, _indent);
			}
			else if (line.matches(Matcher.quoteReplacement(preLeave)+"([\\W].*|$)"))
			{
				String argument = line.substring(preLeave.length()).trim();
				if (!argument.isEmpty() && !argument.equals("1"))
				{
					appendComment("FIXME: No multi-level EXIT in OBERON; reformulate your loops to leave " + argument + " levels!", _indent);
					appendComment(line, _indent);
				}
				code.add(_indent + "EXIT;");
			}
			else if (!isEmpty)
			{
				appendComment("FIXME: jump/exit instruction of unrecognised kind!", _indent);
				appendComment(line, _indent);
			}
		}
		if (isEmpty) {
			addCode("EXIT;", _indent , isDisabled);
		}
		// END KGU#74/KGU#78 2016-01-17
	}
	
	// START KGU#47 2015-12-20: Offer at least a sequential execution (which is one legal execution order)
	protected void generateCode(Parallel _para, String _indent)
	{
		boolean isDisabled = _para.isDisabled();
		appendComment(_para, _indent);

		addCode("", "", isDisabled);
		appendComment("==========================================================", _indent);
		appendComment("================= START PARALLEL SECTION =================", _indent);
		appendComment("==========================================================", _indent);
		appendComment("TODO: add the necessary code to run the threads concurrently", _indent);
		addCode("BEGIN", _indent, isDisabled);

		for (int i = 0; i < _para.qs.size(); i++) {
			addCode("", "", isDisabled);
			appendComment("----------------- START THREAD " + i + " -----------------", _indent + this.getIndent());
			addCode("BEGIN", _indent + this.getIndent(), isDisabled);
			generateCode((Subqueue) _para.qs.get(i), _indent + this.getIndent() + this.getIndent());
			addCode("END;", _indent + this.getIndent(), isDisabled);
			appendComment("------------------ END THREAD " + i + " ------------------", _indent + this.getIndent());
			addCode("", "", isDisabled);
		}

		addCode("END;", _indent, isDisabled);
		appendComment("==========================================================", _indent);
		appendComment("================== END PARALLEL SECTION ==================", _indent);
		appendComment("==========================================================", _indent);
		addCode("", "", isDisabled);
	}
	// END KGU#47 2015-12-20

//	protected void generateCode(Subqueue _subqueue, String _indent)
//	{
//		// code.add(_indent+"");
//		for(int i=0;i<_subqueue.getSize();i++)
//		{
//			generateCode((Element) _subqueue.getElement(i),_indent);
//		}
//		// code.add(_indent+"");
//	}
	
	// START KGU 2015-12-20: Decomposition according to super class Generator
//	public String generateCode(Root _root, String _indent)
//	{
//		String pr = "MODULE";
//		String modname = _root.getText().get(0);
//		if(_root.isProgram==false) {pr="PROCEDURE";}
//		
//		code.add(pr+" "+modname+";");
//		addSepaLine();
//
//		// Add comments and/or declarations to the program (Bob)
//		for(int i=0;i<_root.getComment().count();i++)
//		{
//			if(!_root.getComment().get(i).startsWith("#"))
//			{
//				code.add(_root.getComment().get(i));
//			}
//			// START KGU 2014-11-16: Don't get the comments get lost
//			else {
//				insertComment(_root.getComment().get(i).substring(1), "");
//			}
//			// END KGU 2014-11-16
//			
//		}
//		
//		//code.add("// declare your variables here");
//		addSepaLine();
//		code.add("BEGIN");
//		generateCode(_root.children,_indent+this.getIndent());
//		code.add("END "+modname+".");
//		
//		return code.getText();
//	}
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateHeader(lu.fisch.structorizer.elements.Root, java.lang.String, java.lang.String, lu.fisch.utils.StringList, lu.fisch.utils.StringList, java.lang.String)
	 */
	@Override
	protected String generateHeader(Root _root, String _indent, String _procName,
			StringList _paramNames, StringList _paramTypes, String _resultType, boolean _public)
	{
		String header = (_root.isProgram() ? "MODULE " : "PROCEDURE ") + _procName;
		// START KGU#765 2019-11-14: Enh. 346, bugfix #779 Avoid duplicate IMPORTs
		if (topLevel) {
			if (this.hasInput()) generatorIncludes.add("In");
			if (this.hasOutput()) generatorIncludes.add("Out");
		}
		// END KGU#765 2019-11-14
		if (!_root.isProgram())
		{
			// FIXME: How to handle includable diagrams?
			// START KGU#236 2016-08-10: Issue #227 - create a MODULE context
			if (topLevel /*&& this.optionExportSubroutines()*/)	// FIXME: Why this restriction to subroutine mode here?
			{
				// Though the MODULE name is to be the same as the file name
				// (or vice versa),
				// we must not allow non-identifier characters. so convert
				// all characters that are neither letters nor digits into 
				// underscores.
				code.add(_indent + "MODULE " + getModuleName() + ";");
				appendComment("Generated by Structorizer " + Element.E_VERSION, _indent);
				addSepaLine();

				// START KGU#351 2017-02-26: Enh. #346
				this.appendUserIncludes(_indent);
				// END KGU#351 2017-02-26
				// START KGU#826 2020-03-17: Bugfix #838
				//if (this.hasInput() || this.hasOutput())
				//{
				//	StringList ioModules = new StringList();
				//	if (this.hasInput()) ioModules.add("In");
				//	if (this.hasOutput()) ioModules.add("Out");
				//	code.add(_indent + "IMPORT " + ioModules.concatenate(", ") + ";");
				//	addSepaLine();
				//}
				this.appendGeneratorIncludes(_indent, false);
				// END KGU#826 2020-03-17
				if (this.hasEmptyInput(_root))
				{
					// FIXME This might me misplaced as another VAR declaration block might follow
					code.add(_indent + "VAR");
					code.add(_indent + this.getIndent() + "dummyInputChar: CHAR;	" +
							this.commentSymbolLeft() + " for void input " + this.commentSymbolRight());
					addSepaLine();
				}
				subroutineIndent = _indent;
				subroutineInsertionLine = code.count();
			}
			// END KGU#236 2016-08-10

			if (_public) {
				header += "*";	// Marked for export as default
			}
			String lastType = "";
			//header += "(";
			String paramList = "";
			int nParams = _paramNames.count();
			for (int p = 0; p < nParams; p++) {
				// START KGU#800 2020-02-15: Type name surrogate unified to ???
				//String type = transformType(_paramTypes.get(p), "(*type?*)");
				String type = transformType(_paramTypes.get(p), "???");
				// END KGU#800 2020-02-15
				// START KGU#140 2017-01-31; Enh. #113 - array conversion in argument list
				//if (p == 0) {
				//	header += "(";
				//}
				//else if (type.equals("(*type?*)") || !type.equals(lastType)) {
				if (p > 0) {
					// START KGU#800 2020-02-15: Type name surrogate unified to ???
					//if (type.equals("(*type?*)") || !type.equals(lastType)) {
					if (type.equals("???") || !type.equals(lastType)) {
					// END KGU#800 2020-02-15
				// END KGU#140 2017-01-31
						paramList += ": " + lastType + "; ";
						// START KGU#332 2017-01-31: Enh. #335 Improved type support
						if (type.contains("ARRAY") && !_paramNames.get(p).trim().startsWith("VAR ")) {
							paramList += "VAR ";
						}
						// END KGU#332 2017-01-31
					}
					else {
						paramList += ", ";
					}
				// START KGU#140 2017-01-31; Enh. #113 - array conversion in argument list
				}
				// END KGU#140 2017-01-31
				paramList += _paramNames.get(p).trim();
				if (p+1 == nParams) {
					//header += ": " + type + ")";
					paramList += ": " + type;
				}
				lastType = type;
			}
			//header += ")";
			if (!_root.isInclude())
			{
				// START KGU#766 2019-11-14: Issue #780
				//header += "(" + paramList + ")";
				if (!paramList.isEmpty()) {
					header += "(" + paramList + ")";
				}
				// END KGU#766 2019-11-14
				// START KGU#332 2017-01-31: Enh. #335
				//header += ": " + transformType(_resultType, "");
				if (_resultType != null || this.returns || this.isFunctionNameSet || this.isResultSet) {
					String oberonType = transformType(_resultType, "???");
					if (oberonType.contains("ARRAY")) {
						appendComment("TODO: Oberon doesn't permit to return arrays - pass the result in a different way!", _indent);					
					}
					header += ": " + oberonType;
				}
				// END KGI#332 2017-01-31
			}
		}
		
		// START KGU#815/KGU#824 2020-03-18: Enh. #828, bugfix #836 - An includable has never a procedure heading
		//code.add(_indent + header + ";");
		if (!_root.isInclude()) {
			code.add(_indent + header + ";");
		}
		// END KGU#815/KGU#824 2020-03-18
		
		// START KGU#61 2016-03-23: For a module, we will import In and Out
		// START KGU#236 2016-08-10: Issue #227 - only if needed now
		//if (_root.isProgram)
		//{
		//	code.add(_indent + "IMPORT In, Out");	// Later, this could be done on demand
		//}
		// START KGU#765 2019-11-14: Bugfix #779
		//if (_root.isProgram() && (this.hasInput(_root) || this.hasOutput(_root)))
		//{
		//	StringList ioModules = new StringList();
		//	if (this.hasInput(_root)) ioModules.add("In");
		//	if (this.hasOutput(_root)) ioModules.add("Out");
		//	code.add(_indent + "IMPORT " + ioModules.concatenate(", ") + ";");
		//}
		if (_root.isProgram())
		{
			appendComment("Generated by Structorizer " + Element.E_VERSION, _indent);
			this.appendUserIncludes(_indent);
			// START KGU#826 2020-03-17: Bugfix #838
			//if (this.hasInput() || this.hasOutput()) {
			//	StringList ioModules = new StringList();
			//	if (this.hasInput()) ioModules.add("In");
			//	if (this.hasOutput()) ioModules.add("Out");
			//	code.add(_indent + "IMPORT " + ioModules.concatenate(", ") + ";");
			//	addSepaLine();
			//}
			this.appendGeneratorIncludes(_indent, false);
			// END KGU#826 2020-03-17
		}
		// END KGU#765 2019-11-14
		// END KGU#236 2016-08-10
		// END KGU#61 2016-03-23

		// START KGU 2015-12-20: Don't understand what this was meant to achieve
		// Add comments and/or declarations to the program (Bob)
//		for(int i=0; i<_root.getComment().count(); i++)
//		{
//			if(!_root.getComment().get(i).startsWith("#"))
//			{
//				code.add(_indent + _root.getComment().get(i));
//			}
//			// START KGU 2014-11-16: Don't let the comments get lost
//			else {
//				insertComment(_root.getComment().get(i).substring(1), _indent);
//			}
//			// END KGU 2014-11-16
//		}
		// START KGU 2016-01-16: No need to create an empty comment
		//insertBlockComment(_root.getComment(), _indent, this.commentSymbolLeft(),
		//		" * ", " " + this.commentSymbolRight());
		if (!_root.getComment().getLongString().trim().isEmpty())
		{
			appendBlockComment(_root.getComment(), _indent, this.commentSymbolLeft(),
					" * ", " " + this.commentSymbolRight());
		}
		if (topLevel)
		{
			// START KGU#363 2017-05-16: Enh. #372
			appendCopyright(_root, _indent, true);
			// END KGU#363 2017-05-16
		}
		// END KGU 2016-01-16
		// END KGU 2015-12-20

		return _indent;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generatePreamble(lu.fisch.structorizer.elements.Root, java.lang.String, lu.fisch.utils.StringList)
	 */
	@Override
	protected String generatePreamble(Root _root, String _indent, StringList varNames)
	{
		String indentPlusOne = _indent + this.getIndent();
		// START KGU#261 2017-01-30: Enh. #259: Insert actual declarations if possible
		// START KGU#676 2019-03-30: Enh. #696 special pool in case of batch export
		//typeMap = _root.getTypeInfo();
		typeMap = _root.getTypeInfo(routinePool);
		// END KGU#676 2019-03-30
		// END KGU#261 2017-01-30
		// START KGU#388 2017-10-24: Enh. #423
		//if (varNames.count() > 0) {
		//	code.add(_indent + "VAR");
		//	insertComment("TODO: Check and accomplish local variable declarations:", indentPlusOne);
		//}
		//for (int v = 0; v < varNames.count(); v++) {
		//   // START KGU#332 2017-01-30: Enh. #335: Insert actual declarations if possible
		//	//insertComment(varNames.get(v), indentPlusOne);
		//	String varName = varNames.get(v);
		//	TypeMapEntry typeInfo = typeMap.get(varName); 
		//	StringList types = null;
		//	if (typeInfo != null) {
		//		 types = getTransformedTypes(typeInfo, false);
		//	}
		//	if (types != null && types.count() == 1) {
		//		String type = resolveArrayType(typeInfo, types.get(0));
		//		if (type.contains("???")) {
		//			insertComment(varName + ": " + type + ";", _indent + this.getIndent());
		//		}
		//		else {
		//			code.add(_indent + this.getIndent() + varName + ": " + type + ";");
		//		}
		//	}
		//	else {
		//		insertComment(varName, _indent + this.getIndent());
		//	}
		//	// END KGU#332 2017-01-30
		//}
		// START KGU#815/KGU#824 2020-03-18: Enh. #828, bugfix #836 result no longer needed
		//StringList complexConsts = new StringList();
		//Root[] includes = this.generateDeclarations(_root, _indent, varNames, complexConsts);
		this.generateDeclarations(_root, _indent, varNames);
		// END KGU#815/KGU#824 2020-03-18
		// END KGU#388 2017-10-24
		
		// START KGU#178 2016-07-20: Enh. #160 (subroutine export integration)
		// START KGU#815 2020-03-17: Enh. #828 - the mode restriction didn't seem to make sense
		//if (topLevel && _root.isProgram() && this.optionExportSubroutines())
		if (topLevel && _root.isProgram())
		// END KGU#815 2020-03-17
		{
			addSepaLine();
			subroutineIndent = _indent;
			subroutineInsertionLine = code.count();
			// START KGU#815/KGU#824 2020-03-19: Enh. #828, bugfix #836
			libraryInsertionLine = code.count();
			// END KGU#815/KGU#824 2020-03-19
		}
		// END KGU#178 2016-07-20
		
		// START KGU#815/KGU#824 2020-03-18: Enh. #828, bugfix #836
		//code.add(_indent + "BEGIN");
		if (!_root.isInclude()) {
			code.add(_indent + "BEGIN");
		}
		// END KGU#815/KGU#824 2020-03-18
		// START KGU#236 2016-08-10: Issue #227
		// START KGU#815 2020-03-17: Enh. #828 - the mode restriction does not make sense anymore
		//boolean isProcModule = _root.isSubroutine() && this.optionExportSubroutines();
		boolean isProcModule = !_root.isProgram();
		// END KGU#815 2020-03-17
		// START KGU#765 2019-11-14: Bugfix #779
		//if (topLevel && this.hasInput(_root) && !isProcModule)
		if (topLevel && this.hasInput() && !isProcModule)
		// END KGU#765 2019-11-14
		{
			code.add(indentPlusOne + "In.Open;");
		}
		// START KGU#765 2019-11-14: Bugfix #779
		//if (topLevel && this.hasOutput(_root) && !isProcModule)
		if (topLevel && this.hasOutput() && !isProcModule)
		// END KGU#765 2019-11-14
		{
			code.add(indentPlusOne + "Out.Open;");	// This is optional, actually
		}
		// END KGU#236 2016-08-10
		// START KGU#376 2017-10-24: Enh. #389 - code of includes is to be produced here
		/* FIXME (#828, #836): This is not true if the includes are imported from another module
		 * and what about public procedures or functions in a library - ther it ought to go to the
		 * module initialisation
		 */
		if (_root.isProgram()) {
			// START KGU#815/KGU#824 2020-03-18: Enh. #828, bugfix #836
			//for (Root incl: includes) {
			//	generateCode(incl.children, _indent + this.getIndent());
			//}
			this.appendGlobalInitialisations(_root, indentPlusOne);
			// END KGU#815/KGU#824 2020-03-18
		}
		// END KGU#376 2017-10-24
		return indentPlusOne;
	}
	
	// START KGU#376/KGU#388 2017-10-24: nh. #389, #423 (copied from PasGenerator)
	/**
	 * Appends the const, type, and var declarations for the referred includable roots
	 * and - possibly - {@code _root} itself to the code, as far as they haven't been
	 * generated already.<br/>
	 * Note:<br/>
	 * The declarations of referred includables are only appended if we are at top level.<br/>
	 * The declarations of {@code _root} itself are suppressed if {@code _varNames} is
	 * null - in this case it is assumed that we are in the IMPLEMENTATION part of a UNIT
	 * outside of any function.
	 * @param _root - the currently processed diagram (usually at top level)
	 * @param _indent - the indentation string of the current nesting level
	 * @param _varNames - list of variable names if this is within preamble, otherwise null
	 * @return topologically sorted array of included Roots.
	 */
	// START KGU#815/KGU#824 2020-03-19: Enh. #828, bugfix #836 last argument not needed externally
	//protected Root[] generateDeclarations(Root _root, String _indent, StringList _varNames, StringList complexConsts) {
	protected Root[] generateDeclarations(Root _root, String _indent, StringList _varNames) {
		/* A StringList being filled with the names of those structured
		 * constants that cannot be converted to structured Oberon constants but are to
		 * be deconstructed as mere variables in the body. */
		StringList complexConsts = new StringList();
	// END KGU#815/KGU#824 2020-03-19
		Root[] includes = new Root[]{};
		boolean introPlaced = false;	// Has the CONST keyword already been written?
		if (topLevel) {
			includes = includedRoots.toArray(includes);
			for (Root incl: includes) {
				// START KGU#815/KGU#824 20202-03-18: Enh. #828, bugfix #836
				//if (incl != _root) {
				if (incl != _root && (importedLibRoots == null || !importedLibRoots.contains(incl))) {
				// END KGU#815/KGU#824 2020-03-18
					introPlaced = generateConstDefs(incl, _indent, complexConsts, introPlaced);
				}
			}
		}
		// START KGU#388 2017-09-19: Enh. #423 record type definitions introduced
		// START KGU#375 2017-04-12: Enh. #388 now passed to generatePreamble
		// START KGU#504 2018-03-13: Bugfix #520, #521
		//if (_varNames != null) {
		if (_varNames != null && (!this.suppressTransformation || _root.isInclude())) {
		// END KGU#504 2018-03-13
			generateConstDefs(_root, _indent, complexConsts, introPlaced);
		}
		
		// START KGU#376 2017-09-21: Enh. #389 Concentrate all included definitions here
		introPlaced = false;	// Has the TYPE keyword already been written?
		for (Root incl: includes) {
			// START KGU#815/KGU#824 20202-03-18: Enh. #828, bugfix #836
			//if (incl != _root) {
			if (incl != _root && (importedLibRoots == null || !importedLibRoots.contains(incl))) {
			// END KGU#815/KGU#824 2020-03-18
				introPlaced = generateTypeDefs(incl, _indent, introPlaced);
			}
		}
		// START KGU#388 2017-09-19: Enh. #423 record type definitions introduced
		// START KGU#504 2018-03-13: Bugfix #520, #521
		//if (_varNames != null) {
		if (_varNames != null && (!this.suppressTransformation || _root.isInclude())) {
		// END KGU#504 2018-03-13
			introPlaced = generateTypeDefs(_root, _indent, introPlaced);
		}
		// END KGU#388 2017-09-19
		
		if (!this.structuredInitialisations.isEmpty()) {
			// Was there a type definition inbetween?
			if (introPlaced) {
				code.add(_indent + "const");
			}
			// START KGU#375 2017-09-20: Enh. #388 initialization of structured constants AFTER type definitions
			// (Only if structured constants are allowed, which is not the case in Oberon 2)
			for (Root incl: includes) {
				// START KGU#815/KGU#824 20202-03-18: Enh. #828, bugfix #836
				//if (incl != _root) {
				if (incl != _root && (importedLibRoots == null || !importedLibRoots.contains(incl))) {
				// END KGU#815/KGU#824 2020-03-18
					this.appendPostponedInitialisations(incl, _indent + this.getIndent());
				}
			}
			// START KGU#504 2018-03-13: Bugfix #520, #521
			//if (_varNames != null) {
			if (_varNames != null && (!this.suppressTransformation || _root.isInclude())) {
			// END KGU#504 2018-03-13
				this.appendPostponedInitialisations(_root, _indent + this.getIndent());
			}
			// END KGU#375 2017-09-20
			addSepaLine();
		}
		
		introPlaced = false;	// Has the TYPE keyword already been written?
		// We must not repeat global declaration on local level, the loop won't be executed on sub-levels
		// because `includes' was only filled at top level (see above)
		for (Root incl: includes) {
			// START KGU#815/KGU#824 20202-03-18: Enh. #828, bugfix #836
			//if (incl != _root) {
			if (incl != _root && (importedLibRoots == null || !importedLibRoots.contains(incl))) {
			// END KGU#815/KGU#824 2020-03-18
				introPlaced = generateVarDecls(incl, _indent, incl.retrieveVarNames(), complexConsts, introPlaced);
			}
		}
		// START KGU#504 2018-03-13: Bugfix #520, #521
		//if (_varNames != null) {
		if (_varNames != null && (!this.suppressTransformation || _root.isInclude())) {
		// END KGU#504 2018-03-13
			// START KGU#762 2019-11-13: Bugfix #776 - we must not repeat mere declarations from Includables here
			//introPlaced = generateVarDecls(_root, _indent, _varNames, _complexConsts, introPlaced);
			StringList ownVarNames = _varNames.copy();
			if (!topLevel && _root.includeList != null) {
				for (Root incl: includedRoots) {
					// Because of recursiveness of declaration retrieval, we may restrict to the
					// directly included diagrams, this reduces the risk of eliminating variable
					// names that are not included but locally defined.
					if (_root.includeList.contains(incl.getMethodName())) {
						// START KGU#833 2020-03-26: Avoid the function name being declared unless it was used for result
						//StringList declNames = incl.getMereDeclarationNames();
						StringList declNames = incl.getMereDeclarationNames(true);
						// END KGU#833 2020-03-26
						for (int i = 0; i < declNames.count(); i++) {
							ownVarNames.removeAll(declNames.get(i));
						}
					}
				}
			}
			introPlaced = generateVarDecls(_root, _indent, ownVarNames, complexConsts, introPlaced);
			// END KGU#762 2019-11-13
		}
		// END KGU#375 2017-04-12
		// START KGU#759 2019-11-11: Bugfix #773 - Specific care for merely declared (uninitialized) variables
		if (topLevel) {
			// START KGU#833 2020-03-26: Avoid the function name being declared unless it was used for result
			//generateVarDecls(_root, _indent, _root.getMereDeclarationNames(), new StringList(), introPlaced);
			generateVarDecls(_root, _indent, _root.getMereDeclarationNames(this.isFunctionNameSet), new StringList(), introPlaced);
			// END KGU#833 2020-03-26
		}
		// END KGU#759 2019-11-11
		return includes;
	}

	/**
	 * Adds constant definitions for all non-complex constants in {@code _root.constants}.
	 * @param _root - originating Root
	 * @param _indent - current indentation level (as String)
	 * @param _complexConsts - a list of constants of array or record structure to be postponed
	 * @param _sectionBegun - whether the CONST section had already been introduced by keyword CONST
	 * @return true if CONST section has been introduced (no matter whether before or here)
	 */
	protected boolean generateConstDefs(Root _root, String _indent, StringList _complexConsts, boolean _sectionBegun) {
		if (!_root.constants.isEmpty()) {
			String indentPlus1 = _indent + this.getIndent();
			// START KGU#815/KGU#824 2020-03-18: Enh. #828, bugfix #836
			String glob = "";
			if (topLevel && _root.isInclude() && this.importedLibRoots == null) {
				glob = "*";
			}
			// END KGU#815/KGU#824 2020-03-18
			// _root.constants is expected to be a LinkedHashMap, such that topological
			// ordering should not be necessary
			for (Entry<String, String> constEntry: _root.constants.entrySet()) {
				String constName = constEntry.getKey();
				// We must make sure that the constant hasn't been included from a diagram
				// already handled at top level.
				if (wasDefHandled(_root, constName, true)) {
					continue;
				}
				// START KGU#452 2019-11-17: Enh. #739 Skip enumerator values - they are to be handled in a type definition
				//String expr = transform(constEntry.getValue());
				String expr = constEntry.getValue();
				if (expr != null && expr.startsWith(":")) {
					continue;
				}
				expr = transform(expr);
				// END KGU#452 2019-11-17
				// START KGU#676 2019-03-30: Enh. #696 special pool in case of batch export
				//TypeMapEntry constType = _root.getTypeInfo().get(constEntry.getKey()); 
				TypeMapEntry constType = _root.getTypeInfo(routinePool).get(constEntry.getKey()); 
				// END KGU#676 2019-03-30
				if (constType == null || (!constType.isArray() && !constType.isRecord())) {
					if (!_sectionBegun) {
						code.add(_indent + "CONST");
						//insertComment("TODO: check and accomplish constant definitions", indentPlus1);
						_sectionBegun = true;
					}
					// START KGU#424 2017-09-25
					appendDeclComment(_root, indentPlus1, constName);
					// END KGU#424 2017-09-25
					// START KGU#815/KGU#824 2020-03-18: Enh. #828, bugfix #836
					//code.add(indentPlus1 + constEntry.getKey() + " = " + expr + ";");
					code.add(indentPlus1 + constName + glob + " = " + expr + ";");
					// END KGU#815/KGU#824 2020-03-18
				}
				else {
					_complexConsts.add(constName);
				}
			}
			addSepaLine();
		}
		return _sectionBegun;
	}

	/**
	 * Adds type definitions for all types in {@code _root.getTypeInfo()}.
	 * @param _root - originating Root
	 * @param _indent - current indentation level (as String)
	 * @param _sectionBegun - whether the TYPE section had already been introduced by keyword CONST
	 * @return true if TYPE section has been introduced (no matter whether before or here)
	 */
	protected boolean generateTypeDefs(Root _root, String _indent, boolean _sectionBegun) {
		String indentPlus1 = _indent + this.getIndent();
		String indentPlus2 = indentPlus1 + this.getIndent();
		String indentPlus3 = indentPlus2 + this.getIndent();
		// START KGU#676 2019-03-30: Enh. #696 special pool in case of batch export
		//for (Entry<String, TypeMapEntry> typeEntry: _root.getTypeInfo().entrySet()) {
		for (Entry<String, TypeMapEntry> typeEntry: _root.getTypeInfo(routinePool).entrySet()) {
		// END KGU#676 2019-03-30
			String key = typeEntry.getKey();
			if (key.startsWith(":") /*&& typeEntry.getValue().isDeclaredWithin(_root)*/) {
				if (wasDefHandled(_root, key, true)) {
					continue;
				}
				// START KGU#815/KGU#824 2020-03-18: Enh. #828, bugfix #836
				String typeName = key.substring(1);
				if (topLevel && _root.isInclude() && this.importedLibRoots == null) {
					typeName += "*";	// Mark the type as global (this is only a vague heuristics, though)
				}
				// END KGU#815/KGU#824 2020-03-18
				if (!_sectionBegun) {
					code.add(_indent + "TYPE");
					_sectionBegun = true;
				}
				appendDeclComment(_root, indentPlus1, key);
				TypeMapEntry type = typeEntry.getValue();
				if (type.isRecord()) {
					String lastLine = indentPlus1 + typeName + " = RECORD";
					code.add(lastLine);
					for (Entry<String, TypeMapEntry> compEntry: type.getComponentInfo(false).entrySet()) {
						lastLine = indentPlus3 + compEntry.getKey() + ":\t" + transformTypeFromEntry(compEntry.getValue(), null) + ";";
						code.add(lastLine);
					}
					if (lastLine.endsWith(";")) {
						code.set(code.count()-1, lastLine.substring(0, lastLine.length()-1));
					}
					code.add(indentPlus2 + "END;");
				}
				// START KGU#542 2019-11-17: Enh. #739
				else if (type.isEnum()) {
					code.add(indentPlus1 + typeName + " = ENUM");
					StringList items = type.getEnumerationInfo();
					for (int i = 0; i < items.count(); i++) {
						// FIXME: We might have to transform the value...
						code.add(indentPlus3 + items.get(i) + (i < items.count()-1 ? "," : ""));
					}
					code.add(indentPlus2 + "END;");
				}
				// END KGU#542 2019-11-17
				else {
					code.add(indentPlus1 + typeName + " = " + this.transformTypeFromEntry(type, null) + ";");					
				}
				addSepaLine();
			}
		}
		return _sectionBegun;
	}

	/**
	 * Adds declarations for the variables and complex constants in {@code _varNames} from
	 * the given {@link Root} {@code _root}.  
	 * @param _root - the owning {@link Root}
	 * @param _indent - the current indentation (as string)
	 * @param _varNames - list of occurring variable names
	 * @param _complexConsts - list of constants with non-scalar type
	 * @param _sectionBegun - whether the introducing keyword for this declaration section has already been placed
	 * @return whether the type section has been opened.
	 */
	protected boolean generateVarDecls(Root _root, String _indent, StringList _varNames, StringList _complexConsts, boolean _sectionBegun) {
		String indentPlus1 = _indent + this.getIndent();
		// Insert actual declarations if possible
		//HashMap<String, TypeMapEntry> typeMap = _root.getTypeInfo();	// 2017-01-30: Became obsolete by field
		// START KGU#236 2016-08-10: Issue #227: Declare this variable only if needed
		//code.add(indentPlusOne + "dummyInputChar: Char;	" +
		//		this.commentSymbolLeft() + " for void input " + this.commentSymbolRight());
		// START KGU#815 2020-03-17: Enh. #828 - the mode restriction didn't mssem to make sense
		//boolean isProcModule = _root.isSubroutine() && this.optionExportSubroutines();
		boolean isProcModule = _root.isSubroutine();
		// END KGU#815 2020-03-17
		if (topLevel && this.hasEmptyInput(_root) && !isProcModule)
		{
			if (!_sectionBegun) {
				code.add(_indent + "VAR");
				_sectionBegun = true;
			}
			code.add(indentPlus1 + "dummyInputChar: Char;	" +
					this.commentSymbolLeft() + " for void input " + this.commentSymbolRight());
		}
		// END KGU#236 2016-08-10
		// START KGU#815/KGU#824 2020-03-18: Enh. #828, bugfix #836
		String glob = "";
		if (topLevel && _root.isInclude() && importedLibRoots == null) {
			glob = "*";
		}
		// END KGU#815/KGU#824 2020-03-18
		for (int v = 0; v < _varNames.count(); v++) {
			String varName = _varNames.get(v);
			// constants have already been defined
			boolean isComplexConst = _complexConsts.contains(varName);
			if (_root.constants.containsKey(varName) && !isComplexConst) {
				continue;
			}
			if (wasDefHandled(_root, varName, true)) {
				continue;
			}
			if (!_sectionBegun) {
				code.add(_indent + "VAR");
				appendComment("TODO: check and accomplish variable declarations", _indent + this.getIndent());
				_sectionBegun = true;
			}
			appendDeclComment(_root, indentPlus1, varName);
			TypeMapEntry typeInfo = typeMap.get(varName); 
			StringList types = null;
			if (typeInfo != null) {
				types = getTransformedTypes(typeInfo, true);
			}
			if (types != null && types.count() == 1) {
				String type = this.resolveArrayType(typeInfo, types.get(0));
				String comment = "";
				if (type.contains("???")) {
					// START KGU#759 2019-11-11: Issue #733 - it only irritates the user to outcomment this - the code isn't compilable anyway
					//appendComment(varName + ": " + type + ";", indentPlus1);
					comment = "\t" + this.commentSymbolLeft() + " FIXME! " + this.commentSymbolRight();
					// END KGU#759 2019-11-11
				}
				//else {
				if (isComplexConst) {
					varName = this.commentSymbolLeft() + "const" + this.commentSymbolRight() + " " + varName;
				}
				// START KGU#815/KGU#824 2020-03-18: Enh. #828, bugfix #836
				//addCode(varName + ": " + type + ";" + comment, indentPlus1, false);
				addCode(varName + glob + ": " + type + ";" + comment, indentPlus1, false);
				// END KGU#815/KGU#824 2020-03-18
				//}
			}
			else {
				// START KGU#759 2019-11-11: Issue #773 - it only irritates the user to outcomment this - the code isn't compilable anyway
				//appendComment(varName + ": ???;", indentPlus1);
				// START KGU#815/KGU#824 2020-03-18: Enh. #828, bugfix #836
				//addCode(varName + ": ???;\t" + this.commentSymbolLeft() + " FIXME! " + this.commentSymbolRight(), indentPlus1, false);
				addCode(varName + glob + ": ???;\t" + this.commentSymbolLeft() + " FIXME! " + this.commentSymbolRight(), indentPlus1, false);
				// END KGU#815/KGU#824 2020-03-18
				// END KGU#759 2019-11-11
			}
		}
		return _sectionBegun;
	}

	// START KGU#375/KGU#376/KGU#388 2017-09-20: Enh. #388, #389, #423 
	// Unlikely that this code will ever be active in Oberon
	private void appendPostponedInitialisations(Root _root, String _indent) {
		StringList initLines = this.structuredInitialisations.get(_root);
		if (initLines != null) {
			for (int i = 0; i < initLines.count(); i++) {
				code.add(_indent + initLines.get(i));
			}
			// The same initialisations must not be inserted another time somewhere else!
			this.structuredInitialisations.remove(_root);
		}
	}
	// END KGU#375/KGU#376/KGU#388 2017-09-20

	/**
	 * With help of the respective {@code typeInfo}, resolves array markers in the
	 * transformed type description {@code typeStr}.
	 * @param typeInfo - the {@link TypeMapEntry} corresponding with {@code typeStr}
	 * @param typeStr - a pre-transformed canonical type description or name
	 * @return the Pascal-conform type description with resolved array levels.
	 */
	protected String resolveArrayType(TypeMapEntry typeInfo, String typeStr) {
		int level = 0;
		String prefix = "";
		while (typeStr.startsWith("@")) {
			// It's an array, so get its index range
			int maxIndex = typeInfo.getMaxIndex(level++);
			String nElements = "";
			if (maxIndex > 0) {
				nElements = " " + (maxIndex+1);
			}
			prefix += "ARRAY" + nElements + " OF ";
			typeStr = typeStr.substring(1);
		}
		typeStr = prefix + typeStr;
		return typeStr;
	}

	// START KGU#815/KGU#824 2020-03-18: Enh. #828, bugfix #826
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateBody(lu.fisch.structorizer.elements.Root, java.lang.String)
	 */
	@Override
	protected boolean generateBody(Root _root, String _indent)
	{
		if (_root.isInclude()) {
			return false;
		}
		return super.generateBody(_root, _indent);
	}
	// START KGU#815/KGU#824 2020-03-18

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateResult(lu.fisch.structorizer.elements.Root, java.lang.String, boolean, lu.fisch.utils.StringList)
	 */
	@Override
	protected String generateResult(Root _root, String _indent, boolean alwaysReturns, StringList varNames)
	{
		if ((this.returns || _root.getResultType() != null || isFunctionNameSet || isResultSet) && !alwaysReturns) {
			String result = "0";
			if (isFunctionNameSet) {
				result = _root.getMethodName();
			} else if (isResultSet) {
				int vx = varNames.indexOf("result", false);
				result = varNames.get(vx);
			}
			//addSepaLine();
			code.add(_indent + this.getIndent() + "RETURN " + result + ";");
		}
		return _indent;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateFooter(lu.fisch.structorizer.elements.Root, java.lang.String)
	 */
	@Override
	protected void generateFooter(Root _root, String _indent)
	{
		// Method block close
		// START KGU#236 2016-08-10: Bugfix #227
		//code.add(_indent + "END " + _root.getMethodName() + ";");
		// START KGU#815/KGU#824 2020-02-18: Enh. #828, bugfix #836
		//code.add(_indent + "END " + _root.getMethodName() + (_root.isProgram() ? "." : ";"));
		if (!_root.isInclude()) {
			code.add(_indent + "END " + _root.getMethodName() + (_root.isProgram() ? "." : ";"));
		}
		// END KGU#815/KGU#824 2020-03-18
		// END KGU#236 2016-08-10
		// START KGU#178 2016-07-20: Enh. #160 - separate the routines
		if (!topLevel)
		{
			addSepaLine();
		}
		// END KGU#178 2016-07-20
		// START KGU#236 2016-08-10: Issue #227 - create an additional MODULE context
		// FIXME: An include diagram should be handled in yet another way (separate file)...
		else if (!_root.isProgram() /*&& this.optionExportSubroutines()*/)
		{
			// We are at top level here!
			// START KGU#815/KGU#824 2020-03-19: Enh. #828, bugfix #836
			libraryInsertionLine = code.count();
			// END KGU#815/KGU#824 2020-03-19

			// Additionally append an empty module body as initialization code if we export a
			// potential bunch of routines (module without program)
			addSepaLine();
			code.add(_indent + "BEGIN");
			// FIXME: We should make sure that subroutines don't open the streams again
			if (this.hasInput())
			{
				code.add(_indent + this.getIndent() + "In.Open;");
			}
			if (this.hasOutput())
			{
				code.add(_indent + this.getIndent() + "Out.Open;");	// This is optional, actually
			}
			// START KGU#816/KGU#824 2020-03-18: Enh. #828, bugfix #837
			String indentPlusOne = _indent + this.getIndent();
			this.appendGlobalInitialisations(_root, indentPlusOne);
			if (_root.isInclude() && !includedRoots.contains(_root)) {
				Queue<Root> origIncludes = includedRoots;
				try {
					includedRoots = new LinkedList<Root>();
					includedRoots.add(_root);
					this.appendGlobalInitialisations(_root, indentPlusOne);
				}
				finally {
					includedRoots = origIncludes;
				}
			}
			// END KGU#816/KGU#824 2020-03-18
			code.add(_indent + "END " + this.getModuleName() + ".");
		}
		// END KGU#236 2016-08-10

		super.generateFooter(_root, _indent);
	}
	// END KGU 2015-12-20
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getModuleName()
	 */
	@Override
	protected String getModuleName()
	{
		String unitName = "";
		for (int i = 0; i < pureFilename.length(); i++)
		{
			char ch = pureFilename.charAt(i);
			if (!Character.isAlphabetic(ch) && !Character.isDigit(ch))
			{
				ch = '_';
			}
			unitName += ch;
		}
		return unitName;
	}

}

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

/*
 ******************************************************************************************************
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
 *      Klaus-Peter Reimers     2008.01.08      First Issue
 *      Bob Fisch				2008.01.08      Modified "private String transform(String _input)"
 *      Bob Fisch				2008.04.12      Added "Fields" section for generator to be used as plugin
 *      Bob Fisch				2008.08.14      Added declaration output. A comment line in the root element
 *                                              with a "#" is ignored. All other lines are written to the code.
 *      Bob Fisch               2011.11.07      Fixed an issue while doing replacements
 *      Kay Gürtzig             2014.11.10      Operator conversion modified (see comment)
 *      Kay Gürtzig             2014.11.16      Operator conversion corrected (see comment)
 *      Kay Gürtzig             2014.12.02      Additional replacement of long assignment operator "<--" by "<-"
 *      Kay Gürtzig             2015.10.18      Indentation issue fixed and comment generation revised
 *      Kay Gürtzig             2015.12.21      Bugfix #41/#68/#69 (= KGU#93)
 *      Kay Gürtzig             2016.01.16      KGU#109: Bugfix #61 - handling of type names in assignments
 *                                              Enh. #84 + Bugfix #112 (KGU#141): Assignment export revised
 *      Kay Gürtzig             2016.03.23      Enh. #84: Support for FOR-IN loops (KGU#61)
 *      Kay Gürtzig             2016-04-03      KGU#150 Support for CHR and ORD and other built-in functions
 *      Kay Gürtzig             2016.04.29      Bugfix #144 suppressTransformation mode didn't fully work
 *      Kay Gürtzig             2016.07.20      Enh. #160 (subroutines involved) implemented
 *      Kay Gürtzig             2016.08.10      Bugfix #227 (Modules = main programs have to end with full stop)
 *      Kay Gürtzig             2016.08.12      Two tiny embellishments
 *      Kay Gürtzig             2016.08.12      Enh. #231: Additions for Analyser checks 18 and 19 (variable name collisions)
 *      Kay Gürtzig             2016.09.25      Enh. #253: D7Parser.keywordMap refactoring done 
 *      Kay Gürtzig             2016.10.14      Enh. #270: Handling of disabled elements (code.add(...) --> addCode(..))
 *      Kay Gürtzig             2016.10.15      Enh. #271: Support for input instructions with prompt string,
 *                                              Issue #227: In obvious cases (literals) output procedure names inserted.
 *      Kay Gürtzig             2016.10.16      Enh. #274: Colour info for Turtleizer procedures added
 *      Kay Gürtzig             2016.12.22      Issue #227: input and output usage more routine-specific
 *      Kay Gürtzig             2016.01.30      Enh. #335, bugfix #337: More sophisticated type treatment
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
 ******************************************************************************************************
 */

import java.util.Map;
import java.util.regex.Matcher;

import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.executor.Function;

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

	// START KGU 2016-08-12: Enh. #231 - information for analyser
    private static final String[] reservedWords = new String[]{
    	"ARRAY", "BEGIN", "BY", "CASE", "CONST", "DIV", "DO", "ELSE", "ELSIF", "END", "EXIT",
    	"FOR", "IF", "IMPORT", "IN", "IS", "LOOP", "MOD", "MODULE", "NIL", "OF", "OR",
    	"POINTER", "PROCEDURE", "RECORD", "REPEAT", "RETURN", "THEN", "TO", "TYPE",
    	"UNTIL", "VAR", "WHILE", "WITH",
    	"BOOLEAN", "CHAR", "FALSE", "HALT", "INTEGER", "LONG", "LONGINT", "LONGREAL",
    	"NEW", "REAL", "SET", "SHORT", "SHORTINT", "TRUE"
    };
	public String[] getReservedWords()
	{
		return reservedWords;
	}
	public boolean isCaseSignificant()
	{
		return true;
	}
	// END KGU 2016-08-12
	
	// START KGU#332 2017-01-30: Enh. #335
	private Map<String,TypeMapEntry> typeMap;
	// END KGU#332 2017-01-30
    
    /************ Code Generation **************/

	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/**
	 * A pattern how to embed the variable (right-hand side of an input instruction)
	 * into the target code
	 * @param withPrompt - is a prompt string to be considered?
	 * @return a regex replacement pattern, e.g. "$1 = (new Scanner(System.in)).nextLine();"
	 */
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

	/**
	 * A pattern how to embed the expression (right-hand side of an output instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "System.out.println($1);"
	 */
	protected String getOutputReplacer()
	{
		// START KGU#236 2016-10-16: Enh. #227 - we needed a substitution marker to accomplish identified routines
		//return "Out.TYPE($1)";
		return "Out.TYPE($1%LEN%)";
		// END KGU#236 2016-10-16
	}

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
				String upperIndex = _type.replaceAll(pattern, "$2");
				int nElements = Integer.parseInt(upperIndex) + 1;
				String elementType = _type.replaceAll(pattern,  "$3").trim();
				_type = _type.replaceAll(pattern, "$1ARRAY " + nElements +" OF ") + transformType(elementType, elementType);
			}
			else if (_type.matches(pattern = "ARRAY\\s*OF\\s*(.*)")) {
				String elementType = _type.replaceAll(pattern,  "$1").trim();
				_type = "ARRAY OF " + transformType(elementType, elementType);
			}
			// END KGU#332 2017-01-30
		}
		return _type;
	}
	// END KGU#16 2015-11-30	

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
				String[] typeNameIndex = this.lValueToTypeNameIndex(varName);
				varName = typeNameIndex[1];
				String index = typeNameIndex[2];
				if (!index.isEmpty())
				{
					varName = varName + "["+index+"]";
				}
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
					code.insert("", posDecl);
					code.insert(seekIndent + _category, posDecl);					
				}
				seekIndent += this.getIndent();
			}
		}
		// END KGU#332 2017-01-30
		code.insert(seekIndent + text, posDecl + 1);
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
		if (!insertAsComment(_inst, _indent)) {
			
			boolean isDisabled = _inst.isDisabled();
			
			insertComment(_inst, _indent);

			String outputKey = D7Parser.getKeyword("output");
			for (int i=0; i<_inst.getText().count(); i++)
			{
				// START KGU#101/KGU#108 2015-12-20 Issue #51/#54
				//code.add(_indent+transform(_inst.getText().get(i))+";");
				String line = _inst.getText().get(i);
				// START KGU#236 2016-08-10: Issue #227: Simplification by delegation
//				String matcherInput = "^" + getKeywordPattern(D7Parser.input);
//				String matcherOutput = "^" + getKeywordPattern(D7Parser.output);
//				if (Character.isJavaIdentifierPart(D7Parser.input.charAt(D7Parser.input.length()-1))) { matcherInput += "[ ]"; }
//				if (Character.isJavaIdentifierPart(D7Parser.output.charAt(D7Parser.output.length()-1))) { matcherOutput += "[ ]"; }
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
					String transf = transform(line).replace("In.TYPE()", "In.Char(dummyInputChar)") + ";";
					if (transf.contains("In.TYPE(")) {
						insertComment("TODO: Replace \"TYPE\" by the the actual In procedure name for this type!", _indent);
					}
					addCode(transf, _indent, isDisabled);
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
						String procName = "";
						String length = "";
						String expr = expressions.get(j);
						try {
							Double.parseDouble(expr);
							procName = "Real";
							length = ", 10";
						}
						catch (NumberFormatException ex) {}
						try {
							Integer.parseInt(expr);
							procName = "Int";
							length = ", 10";
						}
						catch (NumberFormatException ex) {}
						if (procName.isEmpty() && (expr.startsWith("\"") || expr.startsWith("'"))
								&& Element.splitLexically(expr, true).count() == 1) {
							procName = "String";
						}
						// START KGU#332 2017-01-30: Enh. #335 Identify variable types if possible
						if (procName.isEmpty()) {
							TypeMapEntry typeInfo = typeMap.get(expr);
							if (typeInfo != null) {
								StringList types = this.getTransformedTypes(typeInfo);
								if (types.count() == 1) {
									String type = types.get(0);
									if (type.equals("INTEGER") || type.equals("LONGINT") || type.equals("SHORTINT")) {
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
						String codeLine = transform(outputKey + " " + expressions.get(j)).replace("%LEN%", length) + ";";
						if (!procName.isEmpty()) {
							codeLine = codeLine.replace("Out.TYPE(", "Out."+procName+"(");
						}
						else {
							insertComment("TODO: Replace \"TYPE\" by the the actual Out procedure name for this type and add a length argument where needed!", _indent);
						}
						addCode(codeLine, _indent, isDisabled);
						// END KGU#236 2016-10-15
					}
					addCode("Out.Ln;", _indent, isDisabled);
				}
				else
				{
					// START KGU#100/#KGU#141 2016-01-16: Enh. #84 + Bugfix #112 - array handling
					//code.add(_indent + transform(line) + ";");
					String transline = transform(line);
					int asgnPos = transline.indexOf(":=");
					boolean isArrayInit = false;
					// START KGU#100 2016-01-16: Enh. #84 - resolve array initialisation
					if (asgnPos >= 0 && transline.contains("{") && transline.contains("}"))
					{
						String varName = transline.substring(0, asgnPos).trim();
						String expr = transline.substring(asgnPos+":=".length()).trim();
						isArrayInit = expr.startsWith("{") && expr.endsWith("}");
						if (isArrayInit)
						{
							StringList elements = Element.splitExpressionList(
									expr.substring(1, expr.length()-1), ",");
							for (int el = 0; el < elements.count(); el++)
							{
								addCode(varName + "[" + el + "] := " + elements.get(el) + ";",
										_indent, isDisabled);
							}
						}
						
					}
					if (!isArrayInit)
					{
						// START KGU#277/KGU#284 2016-10-13/16: Enh. #270 + Enh. #274
						//code.add(_indent + transline + ";");
						transline += ";";
						if (Instruction.isTurtleizerMove(line)) {
							transline += " " + this.commentSymbolLeft() + " color = " + _inst.getHexColor() + " " + this.commentSymbolRight();
						}
						// START KGU 2017-01-31: return must be capitalized here
						transline = transline.replaceFirst("^" + BString.breakup(D7Parser.getKeywordOrDefault("preReturn", "return")) + "($|\\W+.*)", "RETURN$1");
						// END KGU 2017-01-31
						addCode(transline, _indent, isDisabled);
						// END KGU#277/KGU#284 2016-10-13
					}
					// END KGU#100 2016-01-16
				}
				// END KGU#101/KGU#108 2015-12-20
			}

		}
		// END KGU 2015-10-18
	}
	
	protected void generateCode(Alternative _alt, String _indent)
	{
		boolean isDisabled = _alt.isDisabled();
        // START KGU 2014-11-16
        insertComment(_alt, _indent);
        // END KGU 2014-11-16
		addCode("IF "+ transform(_alt.getText().getLongString()) + " THEN",
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
        insertComment(_case, _indent);
        // END KGU 2014-11-16
		addCode("CASE "+transform(_case.getText().get(0))+" OF", _indent, isDisabled);
		
		for (int i=0; i<_case.qs.size()-1; i++)
		{
			addCode(this.getIndent() + _case.getText().get(i+1).trim() + ":",
					_indent, isDisabled);
			generateCode((Subqueue) _case.qs.get(i),_indent+this.getIndent());
			// START KGU 2014-11-16: Wrong case separator replaced
			//code.add(_indent+"END;");
			addCode("|", _indent, isDisabled);
			// END KGU 2014-11-16
		}
		
		if(!_case.getText().get(_case.qs.size()).trim().equals("%"))
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
        insertComment(_for, _indent);
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
			// do if items are heterogenous? We will just try four types: boolean,
			// integer, real and string, where we can only test literals.
			// If none of them match then we add a TODO comment.
			int nItems = items.count();
			boolean allBoolean = true;
			boolean allInt = true;
			boolean allReal = true;
			boolean allString = true;
			for (int i = 0; i < nItems; i++)
			{
				String item = items.get(i);
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
						allInt = false;
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
			}
			
			if (allBoolean) itemType = "BOOLEAN";
			else if (allInt) itemType = "INTEGER";
			else if (allReal) itemType = "REAL";
			else if (allString) itemType = "string";
			else {
				// We do a dummy type definition
				this.insertComment("TODO: Specify an appropriate element type for the array!", _indent);
			}

			// Insert the array and index declarations
			insertDeclaration("VAR", arrayName + ": " + "ARRAY " + 
					nItems + " OF " + itemType + ";", _indent.length());
			insertDeclaration("VAR", indexName + ": INTEGER;",
					_indent.length());

			// Now we create code to fill the array with the enumerated values
			for (int i = 0; i < nItems; i++)
			{
				addCode(arrayName + "[" + i + "] := " + items.get(i) + ";",
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
        insertComment(_while, _indent);
        // END KGU 2014-11-16
		addCode("WHILE "+BString.replace(transform(_while.getText().getText()),"\n","")+" DO",
				_indent, isDisabled);
		generateCode(_while.q, _indent + this.getIndent());
		addCode("END;", _indent, isDisabled);
	}
	
	protected void generateCode(Repeat _repeat, String _indent)
	{
		boolean isDisabled = _repeat.isDisabled();
        // START KGU 2014-11-16
        insertComment(_repeat, _indent);
        // END KGU 2014-11-16
		addCode("REPEAT", _indent, isDisabled);
		generateCode(_repeat.q,_indent+this.getIndent());
		addCode("UNTIL "+BString.replace(transform(_repeat.getText().getText()),"\n","")+";",
				_indent, isDisabled);
	}
	
	protected void generateCode(Forever _forever, String _indent)
	{
		boolean isDisabled = _forever.isDisabled();
        // START KGU 2014-11-16
        insertComment(_forever, _indent);
        // END KGU 2014-11-16
		addCode("LOOP", _indent, isDisabled);
		generateCode(_forever.q,_indent+this.getIndent());
		addCode("END;", _indent, isDisabled);
	}
	
	protected void generateCode(Call _call, String _indent)
	{
		boolean isDisabled = _call.isDisabled();
        // START KGU 2014-11-16
        insertComment(_call, _indent);
        // END KGU 2014-11-16
		for(int i=0;i<_call.getText().count();i++)
		{
			addCode(transform(_call.getText().get(i))+";", _indent, isDisabled);
		}
	}
	
	protected void generateCode(Jump _jump, String _indent)
	{
		boolean isDisabled = _jump.isDisabled();
        // START KGU 2014-11-16
        insertComment(_jump, _indent);
        // END KGU 2014-11-16
        
		// START KGU#74/KGU#78 2016-01-17: actual jump handling
		//for(int i=0;i<_jump.getText().count();i++)
		//{
		//	code.add(_indent+transform(_jump.getText().get(i))+";");
		//}
        // Only EXIT (= break) and RETURN exist, no further jump allowed
        boolean isEmpty = true;

        StringList lines = _jump.getText();
        for (int i = 0; isEmpty && i < lines.count(); i++) {
        	String line = transform(lines.get(i)).trim();
        	if (!line.isEmpty())
        	{
        		isEmpty = false;
        	}
        	// START KGU#74/KGU#78 2015-11-30: More sophisticated jump handling
        	//code.add(_indent + line + ";");
        	String preReturn = D7Parser.getKeywordOrDefault("preReturn", "return");
        	String preExit   = D7Parser.getKeywordOrDefault("preExit", "exit");
        	String preLeave  = D7Parser.getKeywordOrDefault("preLeave", "leave");
        	if (line.matches(Matcher.quoteReplacement(preReturn)+"([\\W].*|$)"))
        	{
        		addCode("RETURN " + line.substring(preReturn.length()).trim() + ";",
        				_indent, isDisabled);
        	}
        	else if (line.matches(Matcher.quoteReplacement(preExit)+"([\\W].*|$)"))
        	{
        		insertComment("FIXME: Find a solution to exit the program here!", _indent);
        		insertComment(line, _indent);
        	}
        	else if (line.matches(Matcher.quoteReplacement(preLeave)+"([\\W].*|$)"))
        	{
        		String argument = line.substring(preLeave.length()).trim();
        		if (!argument.isEmpty() && !argument.equals("1"))
        		{
        			insertComment("FIXME: No multi-level EXIT in OBERON; reformulate your loops to leave " + argument + " levels!", _indent);
        			insertComment(line, _indent);
        		}
    			code.add(_indent + "EXIT;");
        	}
        	else if (!isEmpty)
        	{
        		insertComment("FIXME: jump/exit instruction of unrecognised kind!", _indent);
        		insertComment(line, _indent);
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
		insertComment(_para, _indent);

		addCode("", "", isDisabled);
		insertComment("==========================================================", _indent);
		insertComment("================= START PARALLEL SECTION =================", _indent);
		insertComment("==========================================================", _indent);
		insertComment("TODO: add the necessary code to run the threads concurrently", _indent);
		addCode("BEGIN", _indent, isDisabled);

		for (int i = 0; i < _para.qs.size(); i++) {
			addCode("", "", isDisabled);
			insertComment("----------------- START THREAD " + i + " -----------------", _indent + this.getIndent());
			addCode("BEGIN", _indent + this.getIndent(), isDisabled);
			generateCode((Subqueue) _para.qs.get(i), _indent + this.getIndent() + this.getIndent());
			addCode("END;", _indent + this.getIndent(), isDisabled);
			insertComment("------------------ END THREAD " + i + " ------------------", _indent + this.getIndent());
			addCode("", "", isDisabled);
		}

		addCode("END;", _indent, isDisabled);
		insertComment("==========================================================", _indent);
		insertComment("================== END PARALLEL SECTION ==================", _indent);
		insertComment("==========================================================", _indent);
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
//		code.add("");
//
//		// Add comments and/or declarations to the program (Bob)
//		for(int i=0;i<_root.getComment().count();i++)
//		{
//			if(!_root.getComment().get(i).startsWith("#"))
//			{
//				code.add(_root.getComment().get(i));
//			}
//	        // START KGU 2014-11-16: Don't get the comments get lost
//			else {
//				insertComment(_root.getComment().get(i).substring(1), "");
//			}
//	        // END KGU 2014-11-16
//			
//		}
//		
//		//code.add("// declare your variables here");
//		code.add("");
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
			StringList _paramNames, StringList _paramTypes, String _resultType)
	{
		String header = (_root.isProgram ? "MODULE " : "PROCEDURE ") + _procName;
		if (!_root.isProgram)
		{
        	// START KGU#236 2016-08-10: Issue #227 - create a MODULE context
        	if (topLevel && this.optionExportSubroutines())
        	{
        		// Though the MODULE name is to be the same as the file name
        		// (or vice versa),
        		// we must not allow non-identifier characters. so convert
        		// all characters that are neither letters nor digits into 
        		// underscores.
        		String moduleName = "";
        		for (int i = 0; i < pureFilename.length(); i++)
        		{
        			char ch = pureFilename.charAt(i);
        			if (!Character.isAlphabetic(ch) && !Character.isDigit(ch))
        			{
        				ch = '_';
        			}
        			moduleName += ch;
        		}
        		code.add(_indent + "MODULE " + moduleName + ";");
        		code.add(_indent);
        		
        		if (this.hasInput() || this.hasOutput())
       			{
        			StringList ioModules = new StringList();
        			if (this.hasInput()) ioModules.add("In");
        			if (this.hasOutput()) ioModules.add("Out");
        			code.add(_indent + "IMPORT " + ioModules.concatenate(", ") + ";");
            		code.add(_indent);
       			}
        		if (this.hasEmptyInput(_root))
        		{
        			code.add(_indent + "VAR");
        			code.add(_indent + this.getIndent() + "dummyInputChar: Char;	" +
        					this.commentSymbolLeft() + " for void input " + this.commentSymbolRight());
        			code.add(_indent);
        		}
    			subroutineIndent = _indent;
    			subroutineInsertionLine = code.count();
        	}
        	// END KGU#236 2016-08-10
        	
			header += "*";	// Marked for export as default
			String lastType = "";
			header += "(";
			int nParams = _paramNames.count();
			for (int p = 0; p < nParams; p++) {
				String type = transformType(_paramTypes.get(p), "(*type?*)");
				//if (p == 0) {
				//	header += "(";
				//}
				//else if (type.equals("(*type?*)") || !type.equals(lastType)) {
				if (p > 0 && type.equals("(*type?*)") || !type.equals(lastType)) {
					header += ": " + lastType + "; ";
					// START KGU#332 2017-01-31: Enh. #335 Improved type support
					if (type.contains("ARRAY") && !_paramNames.get(p).trim().startsWith("VAR ")) {
						header += "VAR ";
					}
					// END KGU#332 2017-01-31
				}
				else {
					header += ", ";
				}
				header += _paramNames.get(p).trim();
				if (p+1 == nParams) {
					//header += ": " + type + ")";
					header += ": " + type;
				}
				lastType = type;
			}
			header += ")";
			if (_resultType != null || this.returns || this.isFunctionNameSet || this.isResultSet)
			{
				// START KGU#332 2017-01-31: Enh. #335
				//header += ": " + transformType(_resultType, "");
				String oberonType = transformType(_resultType, "");
				if (oberonType.contains("ARRAY")) {
					insertComment("TODO: Oberon doesn't permit to return arrays - pass the result in a different way!", _indent);					
				}
				header += ": " + oberonType;
				// END KGI#332 2017-01-31
			}
		}
		
		code.add(_indent + header + ";");
		// START KGU#61 2016-03-23: For a module, we will import In and Out
		// START KGU#236 2016-08-10: Issue #227 - only if needed now
		//if (_root.isProgram)
		//{
		//	code.add(_indent + "IMPORT In, Out");	// Later, this could be done on demand
		//}
		if (_root.isProgram && (this.hasInput(_root) || this.hasOutput(_root)))
		{
  			StringList ioModules = new StringList();
			if (this.hasInput(_root)) ioModules.add("In");
			if (this.hasOutput(_root)) ioModules.add("Out");
			code.add(_indent + "IMPORT " + ioModules.concatenate(", ") + ";");
		}
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
//	        // START KGU 2014-11-16: Don't let the comments get lost
//			else {
//				insertComment(_root.getComment().get(i).substring(1), _indent);
//			}
//	        // END KGU 2014-11-16
//		}
		// START KGU 2016-01-16: No need to create an empty comment
		//insertBlockComment(_root.getComment(), _indent, this.commentSymbolLeft(),
		//		" * ", " " + this.commentSymbolRight());
		if (!_root.getComment().getLongString().trim().isEmpty())
		{
			insertBlockComment(_root.getComment(), _indent, this.commentSymbolLeft(),
					" * ", " " + this.commentSymbolRight());
		}
		if (topLevel)
		{
			insertComment("Generated by Structorizer " + Element.E_VERSION, _indent);
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
		code.add(_indent + "VAR");
		insertComment("TODO: Check and accomplish local variable declarations:", indentPlusOne);
		// START KGU#236 2016-08-10: Issue #227: Declare this variable only if needed
		//code.add(indentPlusOne + "dummyInputChar: Char;	" +
		//		this.commentSymbolLeft() + " for void input " + this.commentSymbolRight());
		boolean isProcModule = !_root.isProgram && this.optionExportSubroutines();
		if (topLevel && this.hasEmptyInput(_root) && !isProcModule)
		{
			code.add(indentPlusOne + "dummyInputChar: Char;	" +
					this.commentSymbolLeft() + " for void input " + this.commentSymbolRight());
		}
		// END KGU#236 2016-08-10
        // START KGU#261 2017-01-30: Enh. #259: Insert actual declarations if possible
		typeMap = _root.getTypeInfo();
		// END KGU#261 2017-01-30
		for (int v = 0; v < varNames.count(); v++) {
	        // START KGU#332 2017-01-30: Enh. #335: Insert actual declarations if possible
			//insertComment(varNames.get(v), indentPlusOne);
			String varName = varNames.get(v);
			TypeMapEntry typeInfo = typeMap.get(varName); 
			StringList types = null;
			if (typeInfo != null) {
				 types = getTransformedTypes(typeInfo);
			}
			if (types != null && types.count() == 1) {
				String type = types.get(0);
				int level = 0;
				String prefix = "";
				while (type.startsWith("@")) {
					// It's an array, so get its index range
					int maxIndex = typeInfo.getMaxIndex(level++);
					String nElements = "";
					if (maxIndex > 0) {
						nElements = " " + (maxIndex+1);
					}
					prefix += "ARRAY" + nElements + " OF ";
					type = type.substring(1);
				}
				type = prefix + type;
				if (type.contains("???")) {
					insertComment(varName + ": " + type + ";", _indent + this.getIndent());
				}
				else {
					code.add(_indent + this.getIndent() + varName + ": " + type + ";");
				}
			}
			else {
				insertComment(varName, _indent + this.getIndent());
			}
			// END KGU#332 2017-01-30
		}
		
		// START KGU#178 2016-07-20: Enh. #160 (subroutine export integration)
		if (topLevel && _root.isProgram && this.optionExportSubroutines())
		{
			code.add(_indent);
			subroutineIndent = _indent;
			subroutineInsertionLine = code.count();
		}
		// END KGU#178 2016-07-20
		
		code.add(_indent + "BEGIN");
		// START KGU#236 2016-08-10: Issue #227
		if (topLevel && this.hasInput(_root) && !isProcModule)
		{
			code.add(_indent + this.getIndent() + "In.Open;");
		}
		if (topLevel && this.hasOutput(_root) && !isProcModule)
		{
			code.add(_indent + this.getIndent() + "Out.Open;");	// This is optional, actually
		}
		// END KGU#236 2016-08-10
		return indentPlusOne;
	}

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
			//code.add(_indent);
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
		code.add(_indent + "END " + _root.getMethodName() + (_root.isProgram ? "." : ";"));
		// END KGU#236 2016-08-10
		// START KGU#178 2016-07-20: Enh. #160 - separate the routines
		if (!topLevel)
		{
			code.add(_indent);
		}
		// END KGU#178 2016-07-20
    	// START KGU#236 2016-08-10: Issue #227 - create an additional MODULE context
		else if (!_root.isProgram && this.optionExportSubroutines())
    	{
			// Additionally append an empty module body if we export a
			// potential bunch of routines
			code.add(_indent);
			code.add(_indent + "BEGIN");
			if (this.hasInput())
			{
				code.add(_indent + this.getIndent() + "In.Open;");
			}
			if (this.hasOutput())
			{
				code.add(_indent + this.getIndent() + "Out.Open;");	// This is optional, actually
			}
      		String moduleName = "";
    		for (int i = 0; i < pureFilename.length(); i++)
    		{
    			char ch = pureFilename.charAt(i);
    			if (!Character.isAlphabetic(ch) && !Character.isDigit(ch))
    			{
    				ch = '_';
    			}
    			moduleName += ch;
    		}
			code.add(_indent + "END " + moduleName + ".");
    	}
		// END KGU#236 2016-08-10

		super.generateFooter(_root, _indent);
	}
	// END KGU 2015-12-20
	
}

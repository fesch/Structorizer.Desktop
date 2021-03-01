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

    Perl Source Code Generator

    Copyright (C) 2008 Jan Peter Klippel

    This file has been released under the terms of the GNU Lesser General
    Public License as published by the Free Software Foundation.

    http://www.gnu.org/licenses/lgpl.html

 */

package lu.fisch.structorizer.generators;

/******************************************************************************************************
 *
 *      Author:         Jan Peter Klippel
 *
 *      Description:    Perl Source Code Generator
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author              Date        Description
 *      ------              ----        -----------
 *      Jan Peter Klippel   2008-04-11  First Issue
 *      Bob Fisch           2008-04-12  Added "Fields" section for generator to be used as plugin
 *      Bob Fisch           2009-01-18  Corrected the FOR-loop
 *      Bob Fisch           2011-11-07  Fixed an issue while doing replacements
 *      Kay Gürtzig         2014-12-02  Additional replacement of operator "<--" by "<-"
 *      Kay Gürtzig         2015-10-18  Indentation and comment insertion revised
 *      Kay Gürtzig         2015-11-02  Reorganisation of the transformation, input/output corrected
 *      Kay Gürtzig         2015-11-02  Variable detection and renaming introduced (KGU#62)
 *                                      Code generation for Case elements (KGU#15) and For
 *                                      loops (KGU#3) revised
 *      Kay Gürtzig         2015-12-12  Bugfix #57 (KGU#103) endless loops / flaws on variable prefixing
 *      Kay Gürtzig         2015-12-17  Enh. #23 (KGU#78) jump generation revised; Root generation
 *                                      decomposed according to Generator.generateCode(Root, String);
 *                                      Enh. KGU#47: Dummy implementation for Parallel element
 *                                      Fixes in FOR and REPEAT export
 *      Kay Gürtzig         2015-12-21  Bugfix #41/#68/#69 (= KGU#93)
 *      Kay Gürtzig         2015-12-21  Bugfix #51 (= KGU#108) Didn't cope with empty input / output
 *      Kay Gürtzig         2016-03-22  Enh. #84 (= KGU#61) varNames now inherited, FOR-IN loop support
 *      Kay Gürtzig         2016-03-23  Enh. #84: Support for FOREACH loops (KGU#61)
 *      Kay Gürtzig         2016-04-01  Enh. #144: Care for the new export option suppressing content conversion
 *      Kay Gürtzig         2016-07-20  Enh. #160: Option to involve subroutines implemented (=KGU#178) 
 *      Kay Gürtzig         2016-08-12  Enh. #231: Additions for Analyser checks 18 and 19 (variable name collisions)
 *      Kay Gürtzig         2016-09-25  Enh. #253: CodeParser.keywordMap refactoring done. 
 *      Kay Gürtzig         2016-10-14  Enh. #270: Handling of disabled elements (code.add(...) --> addCode(..))
 *      Kay Gürtzig         2016-10-15  Enh. #271: Support for input instructions with prompt
 *      Kay Gürtzig         2016-10-16  Enh. #274: Colour info for Turtleizer procedures added
 *      Kay Gürtzig         2016-12-01  Bugfix #301: More precise check for parenthesis enclosing of log. conditions
 *      Kay Gürtzig         2016-12-30  Bugfix KGU#62: Result variable hadn't been prefixed in return instruction
 *      Kay Gürtzig         2017-01-04  Enh. #314: Approach to translate the File API
 *      Kay Gürtzig         2017-02-25  Enh. #348: Parallel sections translated with threading module
 *      Kay Gürtzig         2017-02-26  KGU#352: Variable prefixing revised w.r.t. arrays and references
 *      Kay Gürtzig         2017-02-27  Enh. #346: Insertion mechanism for user-specific include directives
 *      Kay Gürtzig         2017-05-16  Enh. #372: Export of copyright information
 *      Kay Gürtzig         2017-05-24  Bugfix #412: The hash codes used to generate unique identifiers could get negative
 *      Kay Gürtzig         2017-11-02  Issue #447: Line continuation in Case elements supported
 *      Kay Gürtzig         2019-02-15  Enh. #680: Support for input instructions with several variables
 *      Kay Gürtzig         2019-03-08  Enh. #385: Support for parameter default values
 *      Kay Gürtzig         2019-03-21  Enh. #56: Export of Try elements implemented
 *      Kay Gürtzig         2019-03-30  Issue #696: Type retrieval had to consider an alternative pool
 *      Kay Gürtzig         2019-11-19  Issues #423, #739: Support for struct and enum types (begun, continued 2019-12-01)
 *      Kay Gürtzig         2019-11-21  Enh. #423, #739: Enumerator stuff as well as record initializer handling revised
 *      Kay Gürtzig         2019-11-28  Issue #388: "use constant" approach withdrawn (except for enums), wrong lval references avoided
 *      Kay Gürtzig         2019-12-03  Bugfix #793: var initializations like "var v: type <- val" were incorrectly translated
 *      Kay Gürtzig         2020-03-20  Enh. #828, bugfix #836: Prepared for group and improved batch export
 *      Kay Gürtzig         2020-03-23  Bugfix #840 Conditions for code transformation w.r.t File API modified
 *      Kay Gürtzig         2021-02-03  Issue #920: Transformation for "Infinity" literal
 *
 ******************************************************************************************************
 *
 *      Comment:		LGPL license (http://www.gnu.org/licenses/lgpl.html).
 *      
 *      2019-11-28 Issue #388 (KGU#375)
 *      - A temporary solution for constants (via use constant) had to be withdrawn because these constants cannot be scoped
 *        and don't behave like readonly variables, they can hardly be used with function calls or the like.
 *
 ******************************************************************************************************///

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lu.fisch.structorizer.elements.Alternative;
import lu.fisch.structorizer.elements.Call;
import lu.fisch.structorizer.elements.Case;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.Forever;
import lu.fisch.structorizer.elements.ILoop;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.elements.Jump;
import lu.fisch.structorizer.elements.Parallel;
import lu.fisch.structorizer.elements.Repeat;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.structorizer.elements.Try;
import lu.fisch.structorizer.elements.TypeMapEntry;
import lu.fisch.structorizer.elements.While;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.generators.Generator.TryCatchSupportLevel;
import lu.fisch.structorizer.parsers.CodeParser;
import lu.fisch.utils.StringList;

public class PerlGenerator extends Generator {
	
	// START KGU#61 2016-03-22: Now provided by Generator class
	// (KGU 2015-11-02) We must know all variable names in order to prefix them with '$'.
	//StringList varNames = new StringList();
	// END KGU#61 2016-03-22

	/************ Fields ***********************/
	protected String getDialogTitle()
	{
		return "Export Perl Code ...";
	}
	
	protected String getFileDescription()
	{
		return "Perl Source Code";
	}
	
	protected String getIndent()
	{
		return "    ";	// KGU 2015-12-17: Changed from " " to "    " (www.tutorialspoint.com/perl/perl_coding_standard.html)
	}
	
	protected String[] getFileExtensions()
	{
		String[] exts = {"pl", "pm"};
		return exts;
	}
	
	// START KGU 2015-10-18: New pseudo field
	@Override
	protected String commentSymbolLeft()
	{
		return "#";
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
	 * @return either {@link TryCatchSupportLevel#TC_NO_TRY} or {@link TryCatchSupportLevel#TC_TRY_CATCH},
	 * or {@link TryCatchSupportLevel#TC_TRY_CATCH_FINALLY}
	 */
	protected TryCatchSupportLevel getTryCatchLevel()
	{
		return TryCatchSupportLevel.TC_TRY_CATCH;
	}
	// END KGU#686 2019-03-18

	//	// START KGU 2016-08-12: Enh. #231 - information for analyser - obsolete since 3.27
//	private static final String[] reservedWords = new String[]{
//		"and", "cmp", "continue", "do",
//		"else", "elsif", "eq", "exp",
//		"for", "foreach", "ge", "gt",
//		"if", "le", "lock", "lt", "ne", "no", "or",
//		"package", "qq", "qr", "qw", "qx", 
//		"sub", "tr", "unless", "until", "while", "xor"
//		};
//	public String[] getReservedWords()
//	{
//		return reservedWords;
//	}
//	public boolean isCaseSignificant()
//	{
//		return false;
//	}
//	// END KGU 2016-08-12
	
	// START KGU#351 2017-02-26: Enh. #346 - include / import / uses config
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getIncludePattern()
	 */
	@Override
	protected String getIncludePattern()
	{
		return "use %;";
	}
	// END KGU#351 2017-02-26

	/************ Code Generation **************/
	
	private final Matcher varMatcher = Pattern.compile("^\\\\?[@$]?[$]?[A-Za-z_]\\w*$").matcher("");
	// START KGU#311 2017-01-04: Enh. #314 File API analysis
	private StringList fileVars = new StringList();
	// END KGU#311 2017-01-04
	
	// START KGU#352 2017-02-26
	private StringList paramNames = new StringList();
	private HashMap<String, TypeMapEntry> typeMap = null;
	private boolean isWithinCall = false;
	// END KGU#352 2017-02-26

	// START KGU#542 2019-11-20: Enh. #739 - Support enum types
	/** Currently exported {@link Root} object. */
	private Root root;
	// END KGU#542 2019-11-20

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
	//	// START KGU#108 2015-12-22: Bugfix #51
	//	//return "$1 = <STDIN>";
	//	return "$1 = <STDIN>; chomp $1";
	//	// END KGU#108 2015-12-22
	//}
	protected String getInputReplacer(boolean withPrompt)
	{
		if (withPrompt) {
			return "print $1; chomp($2 = <STDIN>)";
		}
		return "chomp($1 = <STDIN>)";
	}
	// END KGU#281 2016-10-15

	/**
	 * A pattern how to embed the expression (right-hand side of an output instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "System.out.println($1);"
	 */
	protected String getOutputReplacer()
	{
		// START KGU#103 2015-12-12: Bugfix #57 - Too few backslashes - were consumed by the regex replacement 
		//return "print $1, \"\\n\"";
		return "print $1, \"\\n\"";
		// END KGU#103 2015-12-12
	}

	// START KGU#93 2015-12-21 Bugfix #41/#68/#69
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformTokens(lu.fisch.utils.StringList)
	 */
	@Override
	protected String transformTokens(StringList tokens)
	{
		// START KGU#920 2021-02-03: Issue #920 Handle Infinity literal
		tokens.replaceAll("Infinity", "\"inf\"");
		// END KGU#920 2021-02-03
		// START KGU#388/KGU#542 2019-11-19: Enh. #423, #739 transferred the stuff from transform(String) hitherto
		// Manipulate a condensed token list copy simultaneously (makes it easier to locate neighbouring tokens)
		StringList denseTokens = tokens.copy();
		denseTokens.removeAll(" ");	// Condense
		// Now transform all array and record initializers
		// To go from right to left should ensure we advance from the innermost to the outermost brace
		int posBraceL = tokens.lastIndexOf("{");
		int posBrace0L = denseTokens.lastIndexOf("{");
		while (posBraceL > 0) {
			int posBrace0R = denseTokens.indexOf("}", posBrace0L + 1);
			int posBraceR = tokens.indexOf("}", posBraceL + 1);
			TypeMapEntry type = null;
			if ((type = this.typeMap.get(denseTokens.get(posBrace0L-1))) != null && type.isRecord()) {
				// Transform the condensed record initializer
				StringList rInit = this.transformRecordInit(denseTokens.concatenate(null, posBrace0L-1, posBrace0R+1), type);
				tokens.remove(posBraceL-1, posBraceR+1);
				tokens.insert(rInit, posBraceL-1);
				// Now do the analogous thing for the condensed token list
				denseTokens.remove(posBrace0L-1, posBrace0R+1);
				denseTokens.insert(rInit, posBrace0L-1);;
			}
			// The other case of '{' ... '}' (assumed to be a record initializer) can be ignored here,
			// since it is sufficient to have the braces replaced by parentheses, which will be done below
			posBraceL = tokens.lastIndexOf("{", posBraceL-1);
			posBrace0L = denseTokens.lastIndexOf("{", posBrace0L + 1);
		}
		// END KGU#388/KGU#542 2019-11-19
		// START KGU#62/KGU#103 2015-12-12: Bugfix #57 - We must work based on a lexical analysis
		int posAsgn = tokens.indexOf("<-");
		for (int i = 0; i < varNames.count(); i++)
		{
			String varName = varNames.get(i);
			// Is it an enumeration constant? Then don't prefix it
			String constVal = root.constants.get(varName);
			if (constVal != null && constVal.startsWith(":") && constVal.contains("€")) {
				//tokens.replaceAll(varName, constVal.substring(1, constVal.indexOf('€')) + '_' + varName);
				continue;
			}
			//System.out.println("Looking for " + varName + "...");	// FIXME (KGU): Remove after Test!
			//_input = _input.replaceAll("(.*?[^\\$])" + varName + "([\\W$].*?)", "$1" + "\\$" + varName + "$2");
			// START KGU#352 2017-02-26: Different approaches for arrays and references
			//tokens.replaceAll(varName, "$"+varName);
			TypeMapEntry typeEntry = this.typeMap.get(varName);
			if (typeEntry != null && (typeEntry.isArray() || typeEntry.isRecord())) {
				String refPrefix = "";
				String prefix = typeEntry.isArray() ? "@" : "$";
				int pos = -1;
				int pos0 = -1;
				if (this.paramNames.contains(varName)) {
					refPrefix = "$";	// dereference the variable
				}
				while ((pos = tokens.indexOf(varName)) >= 0) {
					// Array element access?
					pos0 = denseTokens.indexOf(varName, pos0+1);
					if (pos0+3 < denseTokens.count() && denseTokens.get(pos0+1).equals("[")) {
						tokens.set(pos, "$" + refPrefix + varName);
					}
					else if (this.isWithinCall && pos > posAsgn) {
						// To pass an array or record to a subroutine we must use a reference
						// FIXME: for a component or the like the reference would have to be applied to all ("\($foo->bar)")
						tokens.set(pos, "\\" + prefix + refPrefix + varName);
					}
					else {
						tokens.set(pos, prefix + refPrefix + varName);
					}
				}
			}
			else {
				// START KGU#388 2019-11-28: Enh. #423 - avoid to replace component names!
				//tokens.replaceAll(varName, "$"+varName);
				int pos = -1, pos0 = -1;
				while ((pos = tokens.indexOf(varName, pos+1)) >= 0) {
					pos0 = denseTokens.indexOf(varName, pos0+1);
					if (pos0 == 0 || !denseTokens.get(pos0-1).equals(".")) {
						tokens.set(pos, "$"+varName);
					}
				}
				// END KGU#388 2019-11-28
			}
			// END KGU#352 2017-02-26
		}
		// END KGU#62/KGU#103 2015-12-12
		// START KGU#375 2019-11-28: Issue #388 - a "const" keyword must not remain here
		tokens.removeAll("const");
		// END KGU#375 2019-11-28
		// START KGU 2017-02-26
		tokens.replaceAll("random", "rand");
		// END KGU 2017-02-26
		tokens.replaceAll("div", "/");
		tokens.replaceAll("<-", "=");
		// START KGU#61 2016-03-23: Enh. #84 - prepare array literals
		tokens.replaceAll("{", "(");
		tokens.replaceAll("}", ")");
		// END KGU#61 2016-03-23
		for (int i = 1; i < tokens.count()-1; i++) {
			if (tokens.get(i).equals(".")) {
				// Handle possible component access
				int jL = i-1;
				String pre = tokens.get(jL).trim();
				while (pre.isEmpty() && jL > 0) {
					pre = tokens.get(--jL).trim();
				}
				int jR = i+1;
				String post = tokens.get(jR).trim();
				while (post.isEmpty() && jR < tokens.count()-1) {
					post = tokens.get(++jR).trim();
				}
				// START KGU#388 2019-11-29: Issue #423 - there are a lot of combinable prefixes
				//if ((pre.equals("]") || Function.testIdentifier(pre, null) || pre.startsWith("$") && Function.testIdentifier(pre.substring(1), null))
				if ((pre.equals("]") || varMatcher.reset(pre).matches())
				// END KGU#388 2019-11-29
						&& Function.testIdentifier(post, false, null)) {
					tokens.remove(i+1, jR);
					tokens.set(i, "->");
					tokens.remove(++jL, i);
					i -= (i - jL);
				}
			}
		}
		return tokens.concatenate(null);
	}
	// END KGU#93 2015-12-21
	
	// END KGU#18/KGU#23 2015-11-01
	
	// START KGU#61 2016-03-23: Enh. #84 (Array/List support)
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transform(java.lang.String)
	 */
	@Override
	protected String transform(String _input)
	{
		// Set array variable name prefix in assignments with array initialiser
		// START KGU#162 2016-04-01: Enh. #144 - hands off in "no conversion" mode
		if (!this.suppressTransformation)
		{
		// END KGU#162 2016-04-01
			_input = Element.unifyOperators(_input);
		// START KGU#162 2016-04-01: Enh. #144 - hands off in "no conversion" mode
		}
		// END KGU#162 2016-04-01

		return super.transform(_input).trim();
	}
	// END KGU#61 2016-03-23
	
	// START KGU#108 2015-12-22: Bugfix #51
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformInput(java.lang.String)
	 */
	@Override
	protected String transformInput(String _interm)
	{
		String transformed = super.transformInput(_interm);
		if (transformed.startsWith(" = <STDIN>"))
		{
			transformed = "my $dummy = <STDIN>";
		}
		// START KGU#281 2016-10-16: Enh. #271
		else
		{
			transformed = transformed.replace(";  = <STDIN>; chomp", "; my $dummy = <STDIN>");
		}
		// END KGU#281 2016-10-16
		return transformed;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformInput(java.lang.String)
	 */
	@Override
	protected String transformOutput(String _interm)
	{
		String transformed = super.transformOutput(_interm);
		if (transformed.startsWith("print , "))
		{
			transformed = transformed.replace("print , ", "print ");
		}
		return transformed;
	}
	// END KGU#108 2015-12-22
	
	// START KGU#78 2015-12-17: Enh. #23 (jump translation)
	// Places a label with empty instruction into the code if elem is an exited loop
	protected void appendLabel(Element elem, String _indent)
	{
		if (elem instanceof ILoop && this.jumpTable.containsKey(elem)) {
			addCode(this.labelBaseName + this.jumpTable.get(elem) + ": ;",
					_indent, elem.isDisabled(false));
		}
	}
	// END KGU#78 2015-12-17

	// START KGU#388/KGU#542 2019-11-19: Enh. #423, #739
	private void generateTypeDef(Root _root, String _typeName, TypeMapEntry _type, String _indent, boolean _disabled) {
		// TODO Auto-generated method stub
		String indentPlus1 = _indent + this.getIndent();
		if (_type.isEnum()) {
			// FIXME: This was a misconception, type name will not be needed
			//addCode("use enum " + _typeName + "(" + _type.getEnumerationInfo().concatenate(" ") + ");", _indent, _disabled);
			//addCode("use enum qw(:" + _typeName + "_ " + _type.getEnumerationInfo().concatenate(" ") + ");", _indent, _disabled);
			addCode("use enum qw(" + _type.getEnumerationInfo().concatenate(" ") + ");", _indent, _disabled);
		}
		else if (_type.isRecord()) {
			// FIXME Should we use Class::Struct or simply hashtables? Can the latter define a named type?
			addCode("struct (" + _typeName + " => {", _indent, _disabled);
			for (Entry<String, TypeMapEntry> compEntry: _type.getComponentInfo(true).entrySet()) {
				String compTypeSymbol = "$";
				TypeMapEntry compType = compEntry.getValue();
				if (compType.isRecord()) {
					compTypeSymbol = compType.typeName;
				}
				else if (compType.isArray()) {
					compTypeSymbol = "@";
				}
				// enum types are basically int, so '$' is okay
				addCode(compEntry.getKey() + " => '" + compTypeSymbol + "',", indentPlus1, _disabled);
			}
			addCode(")};", _indent, _disabled);
		}
	}
	// END KGU#388/KGU#542 2019-11-19
	
	// START KGU#388 2019-11-19: Enh. #423 - support for record types
	/**
	 * Transforms the record initializer into an adequate Perl code.
	 * @param _recordValue - the record initializer according to Structorizer syntax
	 * @param _typeEntry - used to interpret a simplified record initializer (may be null)
	 * @return a string representing an adequate Perl code for the initialisation. May contain
	 * indentation and newline characters
	 */
	protected StringList transformRecordInit(String _recordValue, TypeMapEntry _typeEntry)
	{
		StringList result = new StringList();
		result.add(_typeEntry.typeName + "->new(\n");
		HashMap<String, String> comps = Instruction.splitRecordInitializer(_recordValue, _typeEntry, false);
		for (Entry<String, String> comp: comps.entrySet()) {
			String compName = comp.getKey();
			String compVal = comp.getValue();
			if (!compName.startsWith("§") && compVal != null) {
				result.add("\t" + compName);
				result.add(" => ");
				result.add(compVal);
				result.add(",\n");
			}
		}
		result.add(");\n");
		return result;
	}
	// END KGU#388 2019-11-19


	protected void generateCode(Instruction _inst, String _indent) {

		if (!appendAsComment(_inst, _indent))
		{
			boolean isDisabled = _inst.isDisabled(false);
			appendComment(_inst, _indent);
			Root root = Element.getRoot(_inst);

			StringList lines = _inst.getUnbrokenText();
			for (int i = 0; i < lines.count(); i++)
			{
				String line = lines.get(i);
				boolean isAsgn = Instruction.isAssignment(line);
				boolean isDecl = Instruction.isDeclaration(line);
				// START KGU#653 2019-02-15: Enh. #680 - input with several items...
				StringList inputItems = Instruction.getInputItems(line);
				if (inputItems != null && inputItems.count() > 2) {
					String inputKey = CodeParser.getKeyword("input");
					String prompt = inputItems.get(0);
					if (!prompt.isEmpty()) {
						addCode(transform(CodeParser.getKeyword("output") + " " + prompt), _indent, isDisabled);
					}
					for (int j = 1; j < inputItems.count(); j++) {
						String item = inputItems.get(j);
						String transf = transform(inputKey + " \"" + item + ": \" " + item);
						if (!transf.endsWith(";")) { transf += ";"; }
						addCode(transf, _indent, isDisabled);
					}
					continue;
				}
				// END KGU#653 219-02-15
				// START KGU#388/KGU#542 2019-11-19: Enh. #423, #739
				else if (Instruction.isTypeDefinition(line)) {
					String typeName = line.substring(line.indexOf("type")+4, line.indexOf("=")).trim();
					TypeMapEntry type = this.typeMap.get(":" + typeName);
					this.generateTypeDef(root, typeName, type, _indent, isDisabled);
					continue;
				}
				else if (isDecl && !isAsgn) {
					// Declarations will have been handled in the preamble
					if (!_inst.getComment().trim().isEmpty()) {
						appendComment(line, _indent);
					}
					continue;
				}
				// END KGU#388/KGU#542 2019-11-19

				String text = null;
				if (isAsgn) {
					StringList tokens = Element.splitLexically(line, true);
					tokens.removeAll(" ");
					Element.unifyOperators(tokens, true);
					int posAsgn = tokens.indexOf("<-");
					String var = Instruction.getAssignedVarname(tokens.subSequence(0, posAsgn), true);
					StringList expr = tokens.subSequence(posAsgn+1, tokens.count());
					if (Function.testIdentifier(var, false, null) && expr.get(0).equals("{") && expr.get(expr.count()-1).equals("}")) {
						text = "@" + var + " = " + transform(expr.concatenate(null));
					}
					// START KGU#787 2019-12-03: Bugfix #793 - variable declaration parts remained
					else if (isDecl) {
						// Wipe off all declaratory parts
						line = var + " <- " + expr.concatenate(null);
					}
					// END KGU#787 2019-12-03
				}
				if (text == null) {
					text = transform(line);
				}
				if (!text.endsWith(";")) { text += ";"; }
				// START KGU#311 2017-01-04: Enh. #314 - steer the user through the File API implications
				//if (this.usesFileAPI) {	// KGU#832 2020-03-23: Bugfix #840 - transform even in disabled case
					if (text.contains("fileOpen(")) {
						String pattern = "(.*?)\\s*=\\s*fileOpen\\((.*)\\)(.*);";
						if (text.matches(pattern)) {
							String varName = text.replaceAll(pattern, "$1").trim();
							fileVars.addIfNew(varName);
							text = text.replaceAll(pattern, "open($1, \\\"<\\\", $2)$3 or die \\\"Failed to open $2\\\";");
							text = text.replaceAll("(.*or die \\\"Failed to open )\\\"(.*)\\\"(\\\";)", "$1\\\\\\\"$2\\\\\\\"$3");
						}
						else if (!isDisabled) {
							this.appendComment("TODO FileAPI: Replace the fileOpen call by something like «open(my $fHandle, \"<\", \"filename\") or die \"error message\";»", _indent);
						}
					}
					if (text.contains("fileCreate(")) {
						String pattern = "(.*?)\\s*=\\s*fileCreate\\((.*)\\)(.*);";
						if (text.matches(pattern)) {
							String varName = text.replaceAll(pattern, "$1").trim();
							fileVars.addIfNew(varName);
							text = text.replaceAll(pattern, "open($1, \\\">\\\", $2)$3 or die \\\"Failed to create $2\\\";");
							text = text.replaceAll("(.*or die \\\"Failed to create )\\\"(.*)\\\"(\\\";)", "$1\\\\\\\"$2\\\\\\\"$3");
						}
						else if (!isDisabled) {
							this.appendComment("TODO FileAPI: Replace the fileCreate call by something like «open(my $fHandle, \">\", \"filename\") or die \"error message\";»", _indent);
						}
					}
					if (text.contains("fileAppend(")) {
						String pattern = "(.*?)\\s*=\\s*fileAppend\\((.*)\\)(.*);";
						if (text.matches(pattern)) {
							String varName = text.replaceAll(pattern, "$1").trim();
							fileVars.addIfNew(varName);
							text = text.replaceAll(pattern, "open($1, \\\">>\\\", $2)$3 or die \\\"Failed to append to $2\\\";");
							text = text.replaceAll("(.*or die \\\"Failed to append to )\\\"(.*)\\\"(\\\";)", "$1\\\\\\\"$2\\\\\\\"$3");
						}
						else if (!isDisabled) {
							this.appendComment("TODO FileAPI: Replace the fileAppend call by something like «open(my $fHandle, \">>\", \"filename\") or die \"error message\";»", _indent);
						}
					}
					if (text.contains("fileRead(") || text.contains("fileReadInt(") || text.contains("fileReadDouble(") || text.contains("fileReadLine(")) {
						String fctName = text.replaceAll(".*?fileRead(\\w*)\\(.*", "$1");
						String pattern = "(.*?)\\s*=\\s*fileRead\\w*\\((.*)\\)(.*)";
						if (text.matches(pattern)) {
							if (!fctName.equals("Line")) {
								this.appendComment("TODO FileAPI: Originally this was a fileRead" + fctName + " call, so ensure to obtain the right thing!", _indent);							
							}
							text = text.replaceAll(pattern, "$1 = <$2> $3");
						}
						else if (!isDisabled) {
							this.appendComment("TODO FileAPI: Replace the fileRead" + fctName + " call by something like «<$fileHandle>\";»", _indent);							
						}
					}
					if (text.contains("fileReadChar(")) {
						String pattern = "(.*?)\\s*=\\s*fileReadChar\\((.*)\\)(.*)";
						if (text.matches(pattern)) {
							text = text.replaceAll(pattern, "$1 = gect\\($2\\)$3");
						}
						else if (!isDisabled) {
							this.appendComment("TODO FileAPI: Replace the fileReadChar* call by something like «getc($fileHandle)\";»", _indent);
						}
					}
					if (text.contains("fileWrite(")) {
						String pattern = "(.*?)fileWrite\\(\\s*(.*)\\s*,\\s*(.*)\\s*\\)(.*)";
						if (text.matches(pattern)) {
							text = text.replaceAll(pattern, "print $2 $3 $4");
						}
						else if (!isDisabled) {
							this.appendComment("TODO FileAPI: Replace the fileWrite call by something like «print $fileHandle value;»", _indent);
						}
					}
					if (text.contains("fileWriteLine(")) {
						String pattern = "(.*?)fileWriteLine\\(\\s*(.*)\\s*,\\s*(.*)\\s*\\)(.*)";
						if (text.matches(pattern)) {
							text = text.replaceAll(pattern, "print $2 $3; print $2 \\\"\\\\n\\\" $4");
						}
						else if (!isDisabled) {
							this.appendComment("TODO FileAPI: Replace the fileWriteLine call by something like «print $fileHandle value; print $fileHandle \"\\n\";»", _indent);
						}
					}
					if (text.contains("fileClose(")) {
						text = text.replace("fileClose(", "close(");
					}
				//}
				// END KGU#311 2017-01-04
				
				// START KGU#277/KGU#284 2016-10-13/16: Enh. #270 + Enh. #274
				//code.add(_indent + text);
				if (Instruction.isTurtleizerMove(line)) {
					text += " " + this.commentSymbolLeft() + " color = " + _inst.getHexColor();
				}
				addCode(text.replace("\t", this.getIndent()), _indent, isDisabled);
				// END KGU#277/KGU#284 2016-10-13
			}
		}

	}
	
	protected void generateCode(Alternative _alt, String _indent) {
		
		boolean isDisabled = _alt.isDisabled(false);
		
		addCode("", "", isDisabled);

		appendComment(_alt, _indent);

		// START KGU#162 2016-04-01: Enh. #144 new restrictive export mode
		//code.add(_indent+"if ( "+BString.replace(transform(_alt.getText().getText()),"\n","").trim()+" ) {");
		String condition = transform(_alt.getUnbrokenText().getLongString()).trim();
		// START KGU#311 2017-01-04: Enh. #314 - steer the user through the File API implications
		if (this.usesFileAPI && !isDisabled) {
			if (condition.contains("fileEOF(")) {
				this.appendComment("TODO FileAPI: Replace the fileEOF test by something like «<DATA>» in combination with «$_» for the next fileRead", _indent);
			}
			else {
				for (int k = 0; k < this.fileVars.count(); k++) {
					if (condition.contains(this.fileVars.get(k))) {
						this.appendComment("TODO FileAPI: Consider replacing / dropping this now inappropriate file test.", _indent);						
					}
				}
			}
		}
		// END KGU#311 2017-01-04
		// START KGU#301 2016-12-01: Bugfix #301
		//if (!this.suppressTransformation || !(condition.startsWith("(") && condition.endsWith(")")))
		if (!this.suppressTransformation && !isParenthesized(condition))
		// END KGU#301 2016-12-01
		{
			condition = "( " + condition + " )";
		}
		addCode("if " + condition + " {", _indent, isDisabled);
		generateCode(_alt.qTrue,_indent+this.getIndent());
		
		if(_alt.qFalse.getSize()!=0) {
			
			addCode("}", _indent, isDisabled);
			addCode("else {", _indent, isDisabled);			
			generateCode(_alt.qFalse,_indent+this.getIndent());
			
		}
		
		addCode("}", _indent, isDisabled);
		addCode("", "", isDisabled);
		
	}
	
	protected void generateCode(Case _case, String _indent) {
		
		boolean isDisabled = _case.isDisabled(false);
		
		addCode("", "", isDisabled);

		appendComment(_case, _indent);

		// Since Perl release 5.8.0, switch is a standard module...
		// START KGU#162 2016-04-01: Enh. #144 new restrictive export mode
		//code.add(_indent+"switch ( "+transform(_case.getText().get(0))+" ) {");
		// START KGU#453 2017-11-02: Issue #447
		//String discriminator = transform(_case.getText().get(0));
		StringList unbrokenText = _case.getUnbrokenText();
		String discriminator = transform(unbrokenText.get(0));
		// END KGU#453 2017-11-02
		// START KGU#301 2016-12-01: Bugfix #301
		//if (!this.suppressTransformation || !(selector.startsWith("(") && selector.endsWith(")")))
		if (!this.suppressTransformation || !isParenthesized(discriminator))
		// END KGU#301 2016-12-01
		{
			discriminator = "( " + discriminator + " )";			
		}
		addCode("switch " + discriminator + " {", _indent, isDisabled);
		// END KGU#162 2016-04-01
		
		for (int i=0; i<_case.qs.size()-1; i++)
		{
			addCode("", "", isDisabled);
			// START KGU#15 2015-11-02: Support multiple constants per branch
			//code.add(_indent+this.getIndent()+"case ("+_case.getText().get(i+1).trim()+") {");
			// START KGU#453 2017-11-02: Issue #447
			//String selectors = _case.getText().get(i+1).trim();
			String selectors = unbrokenText.get(i+1).trim();
			// END KGU#453 2017-11-02
			if (Element.splitExpressionList(selectors, ",").count() > 1)	// Is it an enumeration of values? 
			{
				selectors = "[" + selectors + "]";
			}
			else
			{
				selectors = "(" + selectors + ")";
			}
			addCode("case " + selectors +" {", _indent + this.getIndent(), isDisabled);
			// END KGU#15 2015-11-02
			//code.add(_indent+_indent.substring(0,1)+_indent.substring(0,1)+"begin");
			generateCode((Subqueue) _case.qs.get(i), _indent + this.getIndent() + this.getIndent());
			addCode("}", _indent + this.getIndent(), isDisabled);
		}
		
		// START KGU#453 2017-11-02: Issue #447
		//if(!_case.getText().get(_case.qs.size()).trim().equals("%"))
		if (!unbrokenText.get(_case.qs.size()).trim().equals("%"))
		// END KGU#453 2017-11-02
		{

			addCode("", "", isDisabled);
			addCode("else {", _indent + this.getIndent(), isDisabled);
			generateCode((Subqueue) _case.qs.get(_case.qs.size()-1), _indent + this.getIndent() + this.getIndent());
			addCode("}", _indent + this.getIndent(), isDisabled);
		}
		addCode("}", _indent, isDisabled);
		addCode("", "", isDisabled);
		
	}
	
	
	protected void generateCode(For _for, String _indent) {
		
		boolean isDisabled = _for.isDisabled(false);
		
		addCode("", "", isDisabled);
		
		appendComment(_for, _indent);

		String var = _for.getCounterVar();
		// START KGU#162 2016-04-01: Enh. #144 new restrictive export mode
		if (!var.startsWith("$"))
		{
			var = "$" + var;
		}
		// END KGU#162 2016-04-01
		// START KGU#61 2016-03-23: Enh. 84 - FOREACH support
		if (_for.isForInLoop())
		{
			String valueList = _for.getValueList();
			StringList items = this.extractForInListItems(_for);
			if (items != null)
			{
				valueList = "@array" + Integer.toHexString(_for.hashCode());
				addCode("my " + valueList + " = (" + transform(items.concatenate(", "), false) + ")",
						_indent, isDisabled);
			}
			else
			{
				valueList = transform(valueList, false);
				if (!this.suppressTransformation && valueList.startsWith("$"))
				{
					valueList = "@" + valueList.substring(1);
				}
			}
			// START KGU#162 2016-04-01: Enh. #144 new restrictive export mode
			//code.add(_indent + "foreach $"+ var + " (" + valueList + ") {");
			addCode("foreach "+ var + " (" + valueList + ") {", _indent, isDisabled);
			// END KGU#162
		}
		else
		{
			// END KGU#61 2016-03-23
			int step = _for.getStepConst();
			String compOp = (step > 0) ? " <= " : " >= ";
			// START KGU#162 2016-04-01: Enh. #144 var syntax already handled 
			//String increment = "$" + var + " += (" + step + ")";
			//code.add(_indent + "for ($" +
			//	var + " = " + transform(_for.getStartValue(), false) + "; $" +
			//		var + compOp + transform(_for.getEndValue(), false) + "; " +
			//		increment +
			//		") {");
			String increment = var + " += (" + step + ")";
			addCode("for (" +
					var + " = " + transform(_for.getStartValue(), false) + "; " +
					var + compOp + transform(_for.getEndValue(), false) + "; " +
					increment +
					") {", _indent, isDisabled);
			// END KGU#162 2016-04-01
			// START KGU#61 2016-03-23: Enh. 84 - FOREACH support (part 2)
		}
		// END KGU#61 2016-03-23
		// END KGU#3 2015-11-02
		generateCode(_for.q, _indent+this.getIndent());
		addCode("}", _indent, isDisabled);
		// START KGU#78 2015-12-17: Enh. #23 Put a trailing label if this is a jump target
		appendLabel(_for, _indent);
		// END KGU#78 2015-12-17
		addCode("", "", isDisabled);
	
	}
	
	protected void generateCode(While _while, String _indent) {
		
		boolean isDisabled = _while.isDisabled(false);
		
		addCode("", "", isDisabled);
		appendComment(_while, _indent);
		// START KGU#162 2016-04-01: Enh. #144 new restrictive export mode
		//code.add(_indent+"while ("+BString.replace(transform(_while.getText().getText()),"\n","").trim()+") {");
		String condition = transform(_while.getUnbrokenText().getLongString()).trim();
		// START KGU#311 2017-01-04: Enh. #314 - steer the user through the File API implications
		if (this.usesFileAPI && !isDisabled) {
			if (condition.contains("fileEOF(")) {
				this.appendComment("TODO FileAPI: Replace the fileEOF test by something like «<DATA>» in combination with «$_» for the next fileRead", _indent);
			}
		}
		// END KGU#311 2017-01-04
		// START KGU#301 2016-12-01: Bugfix #301
		//if (!this.suppressTransformation || !(condition.startsWith("(") && condition.endsWith(")")))
		if (!this.suppressTransformation && !isParenthesized(condition))
		// END KGU#301 2016-12-01
		{
			condition = "( " + condition + " )";
		}
		addCode("while " + condition + " {", _indent, isDisabled);
		// END KGU#162 2016-04-01
		generateCode(_while.q, _indent+this.getIndent());
		addCode("}", _indent, isDisabled);
		// START KGU#78 2015-12-17: Enh. #23 Put a trailing label if this is a jump target
		appendLabel(_while, _indent);
		// END KGU#78 2015-12-17
		addCode("", "", isDisabled);
		
	}

	
	protected void generateCode(Repeat _repeat, String _indent) {
		
		boolean isDisabled = _repeat.isDisabled(false);
		
		addCode("", "", isDisabled);

		appendComment(_repeat, _indent);

		addCode("do {", _indent, isDisabled);
		generateCode(_repeat.q,_indent+this.getIndent());
		// START KGU#162 2016-04-01: Enh. #144 new restrictive export mode
		//code.add(_indent+"} while (!("+BString.replace(transform(_repeat.getText().getText()),"\n","").trim()+")) {");
		String condition = transform(_repeat.getUnbrokenText().getLongString()).trim();
		// START KGU#311 2017-01-04: Enh. #314 - steer the user through the File API implications
		if (this.usesFileAPI && !isDisabled) {
			if (condition.contains("fileEOF(")) {
				this.appendComment("TODO FileAPI: Replace the fileEOF test by something like «<DATA>» in combination with «$_» for the next fileRead", _indent);
			}
		}
		// END KGU#311 2017-01-04
		// START KGU#301 2016-12-01: Bugfix #301
		//if (!this.suppressTransformation || !(condition.startsWith("(") && condition.endsWith(")")))
		if (!this.suppressTransformation && !isParenthesized(condition))
		// END KGU#301 2016-12-01
		{
			condition = "( " + condition + " )";
		}
		addCode("} while (!" + condition + ");", _indent, isDisabled);
		// END KGU#162 2016-04-01
		// START KGU#78 2015-12-17: Enh. #23 Put a trailing label if this is a jump target
		appendLabel(_repeat, _indent);
		// END KGU#78 2015-12-17
		addCode("", "", isDisabled);
		
	}

	protected void generateCode(Forever _forever, String _indent) {
		
		boolean isDisabled = _forever.isDisabled(false);
		
		addCode("", "", isDisabled);

		appendComment(_forever, _indent);

		addCode("while (1) {", _indent, isDisabled);		
		generateCode(_forever.q, _indent+this.getIndent());
		addCode("}", _indent, isDisabled);
		// START KGU#78 2015-12-17: Enh. #23 Put a trailing label if this is a jump target
		appendLabel(_forever, _indent);
		// END KGU#78 2015-12-17
		addCode("", "", isDisabled);
		
	}
	
	protected void generateCode(Call _call, String _indent) {
		if(!appendAsComment(_call, _indent))
		{
			boolean isDisabled = _call.isDisabled(false);

			appendComment(_call, _indent);

			// START KGU#352 2017-02-26: Handle arrays as arguments appropriately
			this.isWithinCall = true;
			// END KGU#352 2017-02-26
			StringList lines = _call.getUnbrokenText();
			for (int i=0; i<lines.count(); i++)
			{
				// FIXME: Arrays must be passed as reference, i.e. "\@arr" or "\@$para"
				addCode(transform(lines.get(i)) + ";", _indent, isDisabled);
			}
			// START KGU#352 2017-02-26: Handle arrays as arguments appropriately
			this.isWithinCall = false;
			// END KGU#352 2017-02-26			
		}
	}
	
	protected void generateCode(Jump _jump, String _indent) {
		if(!appendAsComment(_jump, _indent))
// START KGU#78 2015-12-17: Block braces had been missing! Enh. #23 - jump support
//			insertComment(_jump, _indent);
//			for(int i=0;i<_jump.getText().count();i++)
//			{
//				code.add(_indent+transform(_jump.getText().get(i))+";");
//			}
		{
			boolean isDisabled = _jump.isDisabled(false);
			// In case of an empty text generate a break instruction by default.
			boolean isEmpty = true;
			
			StringList lines = _jump.getUnbrokenText();
			String preReturn = CodeParser.getKeywordOrDefault("preReturn", "return");
			String preExit   = CodeParser.getKeywordOrDefault("preExit", "exit");
			String preLeave  = CodeParser.getKeywordOrDefault("preLeave", "leave");
			// START KGU#686 2019-03-21: Enh. #56
			String preThrow  = CodeParser.getKeywordOrDefault("preThrow", "throw");
			// END KGU#686 2019-03-21
			for (int i = 0; isEmpty && i < lines.count(); i++) {
				String line = transform(lines.get(i)).trim();
				if (!line.isEmpty())
				{
					isEmpty = false;
				}
				// START KGU#74/KGU#78 2015-11-30: More sophisticated jump handling
				//code.add(_indent + line + ";");
				if (Jump.isReturn(line))
				{
					addCode("return " + line.substring(preReturn.length()).trim() + ";",
							_indent, isDisabled);
				}
				else if (Jump.isExit(line))
				{
					addCode("exit(" + line.substring(preExit.length()).trim() + ");",
							_indent, isDisabled);
				}
				// START KGU#686 2019-03-21: Enh. #56
				else if (Jump.isThrow(line))
				{
					addCode("die " + line.substring(preThrow.length()).trim() + ";",
					_indent, isDisabled);
				}
				// END KGU#686 2019-03-21
				// Has it already been matched with a loop? Then syntax must have been okay...
				else if (this.jumpTable.containsKey(_jump))
				{
					Integer ref = this.jumpTable.get(_jump);
					String label = this.labelBaseName + ref;
					if (ref.intValue() < 0)
					{
						appendComment("FIXME: Structorizer detected this illegal jump attempt:", _indent);
						appendComment(line, _indent);
						label = "__ERROR__";
					}
					addCode("goto " + label + ";", _indent, isDisabled);
				}
				else if (line.matches(Matcher.quoteReplacement(preLeave)+"([\\W].*|$)"))
				{
					// Strange case: neither matched nor rejected - how can this happen?
					// Try with an ordinary break instruction and a funny comment
					addCode("last;\t" + this.commentSymbolLeft() + " FIXME: Dubious occurrence of 'last' instruction!",
							_indent, isDisabled);
				}
				else if (!isEmpty)
				{
					appendComment("FIXME: jump/exit instruction of unrecognised kind!", _indent);
					appendComment(line, _indent);
				}
				// END KGU#74/KGU#78 2015-11-30
			}
			if (isEmpty) {
				addCode("last;", _indent, isDisabled);
			}
			
		}
// END KGU#78 2015-12-17
			
	}

	// START KGU#47 2015-12-17: Offer at least a sequential execution (which is one legal execution order)
	protected void generateCode(Parallel _para, String _indent)
	{
		boolean isDisabled = _para.isDisabled(false);
		Root root = Element.getRoot(_para);
		int nThreads = _para.qs.size();
		StringList[] asgndVars = new StringList[nThreads];
		String indentPlusOne = _indent + this.getIndent();
		String indentPlusTwo = indentPlusOne + this.getIndent();
		String suffix = Integer.toHexString(_para.hashCode());
				
		// START KGU 2014-11-16
		appendComment(_para, _indent);
		// END KGU 2014-11-16

		addCode("", "", isDisabled);
		appendComment("==========================================================", _indent);
		appendComment("================= START PARALLEL SECTION =================", _indent);
		appendComment("==========================================================", _indent);
		appendComment("Requires at least Perl 5.8 and version threads 2.07", _indent);
		addCode("{", _indent, isDisabled);

		for (int i = 0; i < _para.qs.size(); i++) {
			addCode("", "", isDisabled);
			appendComment("----------------- START THREAD " + i + " -----------------", indentPlusOne);
			asgndVars[i] = root.getVarNames(_para.qs.get(i), false).reverse();
			boolean hasResults = asgndVars[i].count() > 0;
			StringList usedVars = root.getUsedVarNames(_para.qs.get(i), false, false).reverse();
			for (int v = 0; v < asgndVars[i].count(); v++) {
				usedVars.removeAll(asgndVars[i].get(v));
			}
			String threadVar = "$thr" + suffix + "_" + i;
			if (hasResults) {
				// Define the thread in list context such that we may obtain more results
				threadVar = "(" + threadVar + ")";
			}
			addCode("my " + threadVar + " = threads->create(sub {", indentPlusOne, isDisabled);
			for (int v = 0; v < usedVars.count(); v++) {
				addCode("my $" + usedVars.get(v) + " = $_[" + v + "];", indentPlusTwo, isDisabled);				
			}
			generateCode((Subqueue) _para.qs.get(i), indentPlusTwo);
			if (hasResults) {
				this.isWithinCall = true;				
				//addCode("return ($" + asgndVars[i].concatenate(", $") + ");", indentPlusTwo, isDisabled);
				addCode("return (" + this.transform(asgndVars[i].concatenate(", ")) + ");", indentPlusTwo, isDisabled);
				this.isWithinCall = false;
			}
			String argList = usedVars.concatenate(", ").trim();
			if (!argList.isEmpty()) {
				this.isWithinCall = true;
				argList = ", (" + this.transform(argList) + ")";
				this.isWithinCall = false;
			}
			addCode("}" + argList + ");", indentPlusOne, isDisabled);
			addCode("", "", isDisabled);
		}

		for (int i = 0; i < _para.qs.size(); i++) {
			addCode("", "", isDisabled);
			appendComment("----------------- AWAIT THREAD " + i + " -----------------", indentPlusOne);
			String resultVars = asgndVars[i].concatenate(", $").trim();
			if (!resultVars.isEmpty()) {
				resultVars = "($" + resultVars + ") = ";
			}
			addCode(resultVars + "$thr" + suffix + "_" + i + "->join();", indentPlusOne, isDisabled);
			addCode("", "", isDisabled);
		}

		addCode("}", _indent, isDisabled);
		appendComment("==========================================================", _indent);
		appendComment("================== END PARALLEL SECTION ==================", _indent);
		appendComment("==========================================================", _indent);
		addCode("", "", isDisabled);
	}
	// END KGU#47 2015-12-17
	
	// START KGU#686 2019-03-21: Enh. #56
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateCode(lu.fisch.structorizer.elements.Try, java.lang.String)
	 */
	@Override
	protected void generateCode(Try _try, String _indent)
	{
		String indent1 = _indent + this.getIndent();
		boolean isDisabled = _try.isDisabled(false);
		this.appendAsComment(_try, _indent);
		this.addCode("eval {", _indent, isDisabled);
		if (_try.qFinally.getSize() > 0) {
			this.addCode("my $final" + Integer.toHexString(_try.hashCode()) + " = finally {", indent1, isDisabled);
			this.generateCode(_try.qFinally, indent1 + this.getIndent());
			this.addCode("};", indent1, isDisabled);
		}
		this.generateCode(_try.qTry, indent1);
		this.addCode("};", _indent, isDisabled);
		String exName = _try.getExceptionVarName();
		if (exName != null && !exName.isEmpty()) {
			exName = "ex" + Integer.toHexString(_try.hashCode());
		}
		this.addCode("if (my $" + exName + " = $@) {", _indent, isDisabled);
		this.generateCode(_try.qCatch, indent1);
		this.addCode("};", _indent, isDisabled);
	}
	// END KGU#686 2019-03-21
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateHeader(lu.fisch.structorizer.elements.Root, java.lang.String, java.lang.String, lu.fisch.utils.StringList, lu.fisch.utils.StringList, java.lang.String)
	 */
	@Override
	protected String generateHeader(Root _root, String _indent, String _procName,
			StringList _paramNames, StringList _paramTypes, String _resultType, boolean _public)
	{
		String indent = _indent;
		// START KGU#542 2019-11-20: Enh. #739 - Support enum types
		this.root = _root;
		// END KGU#542 2019-11-20
		// START KGU#352 2017-02-26: Cache transform-relevant information 
		this.paramNames = _paramNames;
		// START KGU#676 2019-03-30: Enh. #696 special pool in case of batch export
		//this.typeMap = _root.getTypeInfo();
		this.typeMap = _root.getTypeInfo(routinePool);
		// END KGU#676 2019-03-30
		// END KGU#352 2017-02-26
		
		// END KGU#352 2017-02-26
		// START KGU#178 2016-07-20: Enh. #160 - don't add this if it's not at top level
		//code.add(_indent + "#!/usr/bin/perl");
		//insertComment("Generated by Structorizer " + Element.E_VERSION, _indent);
		//insertComment("", _indent);
		if (topLevel)
		{
			code.add(_indent + "#!/usr/bin/perl");
			appendComment("Generated by Structorizer " + Element.E_VERSION, _indent);
			// START KGU#363 2017-05-16: Enh. #372
			appendCopyright(_root, _indent, true);
			// END KGU#363 2017-05-16
			//if (_root.isProgram) {
			generatorIncludes.add("strict");
			generatorIncludes.add("warnings");
			// START KGU#388 2019-11-19: Enh. #423 - Support for record types
			generatorIncludes.add("Class::Struct");
			// END KGU#388 2019-11-19
			//}
			// START KGU#348 2017-02-25: Enh. #348: Support for Parallel elements
			if (this.hasParallels) {
				generatorIncludes.add("threads");
				generatorIncludes.add("threads::shared");
			}
			// END KGU#348 2017-02-25
			addSepaLine();
			this.appendGeneratorIncludes(_indent, false);
			// END KGU#686 2019-03-21
			// START KGU#351 2017-02-26: Enh. #346
			this.appendUserIncludes(_indent);
			// END KGU#351 2017-02-26
			// START KGU#311 2017-01-04: Enh. #314 Desperate approach to sell the File API...
			if (this.usesFileAPI) {
				addSepaLine();
				this.appendComment("TODO: This algorithm made use of the Structorizer File API,", _indent);
				this.appendComment("      which cannot not be translated completely.", _indent);
				this.appendComment("      Watch out for \"TODO FileAPI\" comments and try to adapt", _indent);
				this.appendComment("      the code according to the recommendations.", _indent);
				this.appendComment("      See e.g. http://perldoc.perl.org/perlopentut.html", _indent);
				addSepaLine();
			}
			// END KGU#311 2017-01-04
			// START KGU#686 2019-03-21: Enh. #56 We better prepare for finally actions
			if (this.hasTryBlocks) {
				// This approach was taken from http://wiki.c2.com/?ExceptionHandlingInPerl
				// see addFinallyPackage(String)
				addCode("sub finally (&) { Finally->new(@_) }", _indent, false);
			}
			// END KGU#66 2019-03-21
			subroutineInsertionLine = code.count();
		}
		else
		{
			addSepaLine();
		}
		// END KGU#178 2016-07-20
		appendComment(_root, _indent);
		// FIXME: What to do with includable diagrams? 
		if( _root.isSubroutine() ) {
			code.add(_indent + "sub " + _procName + " {");
			indent = _indent + this.getIndent();
			// START KGU#371 2019-03-08: Enh. #385 support optional arguments
			//for (int p = 0; p < _paramNames.count(); p++) {
			//	code.add(indent + "my $" + _paramNames.get(p).trim() + " = $_[" + p + "];");
			//}
			int minArgs = _root.getMinParameterCount();
			StringList argDefaults = _root.getParameterDefaults();
			for (int p = 0; p < minArgs; p++)
			{
				code.add(indent + "my $" + _paramNames.get(p).trim() + " = $_[" + p + "];");
			}
			for (int p = minArgs; p < _paramNames.count(); p++)
			{
				code.add(indent + "if (defined $_[" + p + "]) {");
				code.add(indent + this.getIndent() + "my $" + _paramNames.get(p).trim() + " = $_[" + p + "];");
				code.add(indent + "} else {");
				code.add(indent + this.getIndent() + "my $" + _paramNames.get(p).trim() + " = " + transform(argDefaults.get(p)));
				code.add(indent + "}");
			}
			// END KGU#371 2019-03-08
		}
	
		addSepaLine();
		
		return indent;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generatePreamble(lu.fisch.structorizer.elements.Root, java.lang.String, lu.fisch.utils.StringList)
	 */
	@Override
	protected String generatePreamble(Root _root, String _indent, StringList _varNames)
	{
		// Ensure all variables be declared
		// START KGU#352 2017-02-26: This must also be done for programs!
		//if (!_root.isProgram) {
		//	for (int v = 0; v < _varNames.count(); v++) {
		//		code.add(_indent + "my $" + _varNames.get(v) + ";");	// FIXME (KGU) What about lists?
		//	}
		//}
		// START KGU#375/KGU#542 2019-11-19: Enh. #388, #739 - Support for constants (withdrawn for inconsistency)
		//StringList paramNames = _root.getParameterNames();
		//for (Entry<String, String> constEntry: _root.constants.entrySet()) {
		//	String constName = constEntry.getKey();
		//	String constValue = constEntry.getValue();
		//	// Skip arguments and enumeration items
		//	if (!paramNames.contains(constName) && !constValue.startsWith(":")) {
		//		addCode("use constant " + constName + " => " + transform(constValue) + ";", _indent, false);
		//	}
		//}
		// END KGU#375 2019-11-19
		for (int v = 0; v < _varNames.count(); v++) {
			String varName = _varNames.get(v);
			TypeMapEntry typeEntry = this.typeMap.get(varName);
			// START KGU#375/KGU#542 2019-12-01: Enh. #388, #739 - Don't declare enum constants here!
			//String prefix = (typeEntry != null && typeEntry.isArray()) ? "@" : "$";
			//code.add(_indent + "my " + prefix + varName + ";");
			String constVal = _root.constants.get(varName);
			if (constVal == null || !constVal.startsWith(":")) {
				String prefix = (typeEntry != null && typeEntry.isArray()) ? "@" : "$";
				code.add(_indent + "my " + prefix + varName + ";");
			}
			// END KGU#375/KGU#542 2019-11-19
		}
		// END KGU#352 2017-02-26
		addSepaLine();
		// START KGU 2015-11-02: Now fetch all variable names from the entire diagram
		varNames = _root.retrieveVarNames(); // in contrast to super we need the parameter names included again.
		// END KGU 2015-11-02
		return _indent;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateResult(lu.fisch.structorizer.elements.Root, java.lang.String, boolean, lu.fisch.utils.StringList)
	 */
	@Override
	protected String generateResult(Root _root, String _indent, boolean alwaysReturns, StringList varNames)
	{
		if (_root.isSubroutine() && (returns || _root.getResultType() != null || isFunctionNameSet || isResultSet) && !alwaysReturns)
		{
			String result = "";
			if (isFunctionNameSet)
			{
				// START KGU#62 2016-12-30: Bugfix #57
				//result = _root.getMethodName();
				result = "$" + _root.getMethodName();
				// END KGU#62 2016-12-30
			}
			else if (isResultSet)
			{
				int vx = varNames.indexOf("result", false);
				result = varNames.get(vx);
				// START KGU#62 2016-12-30: Bugfix
				if (!result.startsWith("$")) {
					result = "$" + result;
				}
				// END KGU#62 2016-12-30
			}
			// START KGU#62 2017-02-26: Bugfix #57
			if (result.isEmpty()) {
				result = "0";
			}
			else if(!result.startsWith("$") && !result.startsWith("@")) {
				String prefix = "$";
				TypeMapEntry typeEntry = this.typeMap.get(result);
				if (typeEntry != null && typeEntry.isArray()) {
					if (!this.paramNames.contains(result)) {
						prefix = "@";
					}
					else {
						prefix = "@$";
					}
				}
				result = prefix + result;
			}
			// END KGU#62 2017-02-26
			addSepaLine();
			code.add(_indent + "return " + result + ";");
		}
		return _indent;
	}
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateFooter(lu.fisch.structorizer.elements.Root, java.lang.String)
	 */
	@Override
	protected void generateFooter(Root _root, String _indent)
	{
		if (_root.isSubroutine()) code.add(_indent + "}");		
		// START KGU#686 2019-03-21: Enh. #56 We better prepare for "finally" actions
		if (this.topLevel && this.hasTryBlocks) {
			this.addFinallyPackage(_indent);
		}
		// END KGU#66 2019-03-21
		// START KGU#815/KGU#824 2020-03-20: Enh. #828, bugfix #836
		if (topLevel) {
			libraryInsertionLine = code.count();
		}
		// END KGU#815/KGU#824 2020-03-20
	}
	// END KGU#78 2015-12-17
	
	// START KGU#686 2019-03-21: Enh. #56
	/**
	 * This defines a "Finally" package to allow sensible export of {@code finally} blocks
	 * using RAII.<br/>
	 * Source for this solution: <a href="http://wiki.c2.com/?ExceptionHandlingInPerl">
	 * http://wiki.c2.com/?ExceptionHandlingInPerl</a>
	 * @param _indent - current indentation
	 */
	private void addFinallyPackage(String _indent)
	{
		addSepaLine();
		appendComment("----------------------------------------------------------------------", _indent);
		appendComment(" Finally class, introduced to handle finally blocks via RAII", _indent);
		appendComment("----------------------------------------------------------------------", _indent);
		addCode("package Finally;", _indent, false);
		addCode("sub new {", _indent, false);
		addCode("my ($class, $code) = @_;", _indent + this.getIndent(), false);
		addCode("bless {code => $code}, $class;", _indent + this.getIndent(), false);
		addCode("}", _indent, false);
		addCode("sub DESTROY { my ($self) = @_; $self->{code}->() }", _indent, false);
	}
	// END KGU#686 2019-03-21
}

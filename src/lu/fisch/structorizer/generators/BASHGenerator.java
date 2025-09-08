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

    BASH Source Code Generator

    Copyright (C) 2008 Markus Grundner

    This file has been released under the terms of the GNU Lesser General
    Public License as published by the Free Software Foundation.

    http://www.gnu.org/licenses/lgpl.html

 */

package lu.fisch.structorizer.generators;

/******************************************************************************************************
 *
 *      Author:         Markus Grundner
 *
 *      Description:    BASH Source Code Generator
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author				Date			Description
 *      ------				----			-----------
 *      Markus Grundner     2008-06-01		First Issue based on KSHGenerator from Jan Peter Kippel
 *      Bob Fisch           2011-11-07      Fixed an issue while doing replacements
 *      Kay Gürtzig         2014-11-16      Bugfixes in operator conversion and enhancements (see comments)
 *      Kay Gürtzig         2015-10-18      Indentation logic and comment insertion revised
 *                                          generateCode(For, String) and generateCode(Root, String) modified
 *      Kay Gürtzig         2015-11-02      transform methods re-organised (KGU#18/KGU#23) using subclassing,
 *                                          Pattern list syntax in Case Elements corrected (KGU#15).
 *                                          Bugfix KGU#60 (Repeat loop was incorrectly translated).
 *      Kay Gürtzig         2015-12-19      Enh. #23 (KGU#78): Jump translation implemented
 *      Kay Gürtzig         2015-12-21      Bugfix #41/#68/#69 (= KGU#93): String literals were spoiled
 *      Kay Gürtzig         2015-12-22      Bugfix #71 (= KGU#114): Text transformation didn't work
 *      Kay Gürtzig         2016-01-08      Bugfix #96 (= KGU#129): Variable names handled properly,
 *                                          KGU#132: Logical expressions (conditions) put into ((  )).
 *      Kay Gürtzig         2016-03-22      Enh. #84/#135 (= KGU#61): Support for FOR-IN loops
 *      Kay Gürtzig         2016-03-24      Bugfix #92/#135 (= KGU#161) Input variables were prefixed
 *      Kay Gürtzig         2016-03-29      KGU#164: Bugfix #138 Function call expression revised (in transformTokens())
 *                                          #135 Array and expression support improved (with thanks to R. Schmidt)
 *      Kay Gürtzig         2016-03-31      Enh. #144 - content conversion may be switched off
 *      Kay Gürtzig         2016-04-05      Enh. #153 - Export of Parallel elements had been missing
 *      Kay Gürtzig         2016-04-05      KGU#150 - provisional support for chr and ord function
 *      Kay Gürtzig         2016-07-20      Enh. #160: Option to involve subroutines implemented (=KGU#178) 
 *      Kay Gürtzig         2016-08-12      Enh. #231: Additions for Analyser checks 18 and 19 (identifier collisions)
 *      Kay Gürtzig         2016-09-01      Issue #234: ord and chr function code generated only if needed and allowed
 *      Kay Gürtzig         2016-09-21      Bugfix #247: Forever loops were exported with a defective condition.
 *      Kay Gürtzig         2016-10-14      Enh. #270: Handling of disabled elements (code.add(...) --> addCode(..))
 *      Kay Gürtzig         2016-10-15      Enh. #271: Support for input with prompt
 *      Kay Gürtzig         2016-10-16      Enh. #274: Colour info for Turtleizer procedures added
 *      Kay Gürtzig         2016-11-06      Issue #279: Method HashMap.getOrDefault() replaced
 *      Kay Gürtzig         2017-01-05      Enh. #314: File API TODO comments added  
 *      Kay Gürtzig         2017-02-27      Enh. #346: Insertion mechanism for user-specific include directives
 *      Kay Gürtzig         2017-04-18      Bugfix #386: Algorithmically empty Subqueues must produce a ':' line
 *      Kay Gürtzig         2017-05-05      Issue #396: function calls should better be enclosed in $(...) than in back ticks
 *      Kay Gürtzig         2017-05-16      Enh. #372: Export of copyright information
 *      Kay Gürtzig         2017-05-19      Issue #237: Expression transformation heuristics improved
 *      Kay Gürtzig         2017-10-05      Enh. #423: First incomplete approach to handle record variables
 *      Kay Gürtzig         2017-10-24      Enh. #423: Record variable handling accomplished for release 3.27
 *      Kay Gürtzig         2017-11-02      Issue #447: Line continuation in Alternative and Case elements supported
 *      Kay Gürtzig         2019-02-15      Enh. #680: Support for input instructions with several variables
 *      Kay Gürtzig         2019-03-08      Enh. #385: Optional function arguments with defaults
 *      Kay Gürtzig         2019-03-30      Issue #696: Type retrieval had to consider an alternative pool
 *      Kay Gürtzig         2019-09-27      Enh. #738: Support for code preview map on Root level
 *      Kay Gürttig         2019-10-15      Bugfix #765: Private field typeMap had to be made protected
 *      Kay Gürtzig         2019-11-08      Bugfix #769: Undercomplex selector list splitting in CASE generation mended
 *      Kay Gürtzig         2019-11-24      Bugfix #783 - Workaround for record initializers without known type
 *      Kay Gürtzig         2019-11-24      Bugfix #784 - Suppression of mere declarations and fix in transformExpression()
 *      Kay Gürtzig         2019-12-01      Enh. #739: Support for enum types, $() around calls removed, array decl subclassable
 *      Kay Gürtzig         2020-02-16/24   Issue #816: Function calls and value return mechanism revised
 *      Kay Gürtzig         2020-02-17/18   Issue #816: Efforts to label local variables in routines appropriately
 *      Kay Gürtzig         2020-02-18      Enh. #388: Support for constants
 *      Kay Gürtzig         2020-02-20/24   Issues #816, #821: More sophisticated approach to pass records/arrays (see comment)
 *      Kay Gürtzig         2020-02-21      Bugfix #824: The condition of the Repeat loop wasn't negated,
 *                                          several absurd bugs in finishCondition() fixed
 *      Kay Gürtzig         2020-03-18      Bugfix #839: sticky returns flag mended
 *      Kay Gürtzig         2020-03-23      Issue #840: Adaptations w.r.t. disabled elements using File API
 *      Kay Gürtzig         2020-03-27/29   Enh. #828: Modifications tpo supporet group export
 *      Kay Gürtzig         2021-02-03      Issue #920: Transformation for "Infinity" literal
 *      Kay Gürtzig         2021-10-03      Issue #990: Precautions against wrong result type associations
 *      Kay Gürtzig         2021-11-02      Bugfix #1014: Declarations in C and Java style hadn't been processed correctly
 *      Kay Gürtzig         2022-08-23      Issue #1068: transformIndexLists() inserted in transformTokens()
 *      Kay Gürtzig         2023-10-16      Bugfx #1096: transformTokens revised for mixed C / Java declarations
 *      Kay Gürtzig         2023-11-08      Bugfix #1109: Code generation for throw suppressed
 *      Kay Gürtzig         2025-07-03      Bugfix #447: Potential bug for Case elements with broken lines fixed
 *      Kay Gürtzig         2025-08-17      Bugfix #1207: Wrong results on output instructions with expression list
 *      Kay Gürtzig         2025-08-19      Bugfix #1207: output in case of arrays etc. contained temporary auxiliary stuff
 *      Kay Gürtzig         2025-08-20/28   Bugfix #1210: Option suppressTransformation wasn't consequently respected
 *      Kay Gürtzig         2025-09-06      Issue #1148: opportunity to use elif in IF chains now implemented;
 *                                          bugfix #1210: any workaround for value return from functions and on
 *                                          CALLs disabled in case suppressTransformation;
 *                                          bugfix #1222.1: Wrong indentation of non-default CASE branches.
 *      Kay Gürtzig         2025-09-07      Issue #1223: First approach to implement generateCode(Try, String)
 *      Kay Gürtzig         2025-09-08      Issue #1223: generateCode(Try, String) accomplished (with finally
 *                                          and throw.
 *
 ******************************************************************************************************
 *
 *      Comment:		LGPL license (http://www.gnu.org/licenses/lgpl.html).
 *      
 *      2020-02-20 Passing arrays and records into and out of routines (Kay Gürtzig)
 *      - There is no easy way to copy arrays and records, and in most cases these are not even desired
 *      - ksh and bash 4.* allow a name reference (local -n / typeset -n / declare -n) that can be used
 *        to pass an array orc record (= associate array) into a function but it requires that the argument
 *        is a variable (this is in mostly the case, particularly as Structorizer doesn't support nested
 *        CALLs, but does not cover component or array element access - though nested data structures are
 *        not supported by the shells anyway) and then to pass the un-dollared name of the array/record.
 *      - The pass an array or record out of a routine is more tricky. As we converted value return to
 *        a mechanism using a uniquely named generic global variable, we must copy the actual value into
 *        this global value in general, which can be done via "${array[&commat;]}" in case of an array and
 *        requires a loop in case of a record. A global name reference (declare -ng) wouldn't make sense
 *        as it would usually refer to a local name. The folowing paradigms for the return of arrays and
 *        records are tested for viability and implemented as far as possible:
 *        a) Returning an array:
 *        	routineA() {
 *        		...
 *        		declare -ag result0815=("${array[@]}")
 *        	}
 *          CALL:
 *        	routineA ...
 *        	declare -a res=("${result0815[@]}")
 *        b) Returning a record:
 *        	routineR() {
 *        		...
 *        		declare -ag result4711keys=("${!record[@]}")
 *        		declare -ag result4711values=("${record[@]}")
 *        	}
 *          CALL:
 *        	routineR ...
 *        	declare -A res
 *        	for index4711 in ${!result4711keys[@]}; do
 *        		res[${result4711keys[$index4711]}]="result4711values[$index4711]}"
 *        	done
 *        For putting array or record variables into an array or record initializer, an
 *        executable reconstruction command will be created at runtime and placed instead.
 *        In the exported code, these artefacts look like "$(typeset -p varname)".
 *      
 *      2016-04-05 - Enhancement #153 (Kay Gürtzig / Rolf Schmidt)
 *      - Parallel elements hat just been ignored by previous versions Now an easy way could be
 *        implemented. It's working rather well, provided that the commands within the
 *        branches are convertible. Delivered with version 3.24-06. 
 *      
 *      2016-03-21/22 - Enhancement #84/#135 (Kay Gürtzig / Rolf Schmidt)
 *      - Besides the working (but rather rarely used) C-like "three-expression" FOR loop, FOR-IN loops
 *        were to be enabled in a consistent way, i.e. the syntax must also be accepted by Editors and
 *        Executor as well as other code generators.
 *      - This generator copes with value lists of the following types:
 *        {item1, item2, item3} --> item1 item2 item3
 *        item1, item2, item3 -->   item1 item2 item3
 *        {val1..val2} and {val1..val2..step} would be left as is but not explicitly created
 *      
 *      2015-12-21 - Bugfix #41/#68/#69 (Kay Gürtzig)
 *      - Operator replacement had induced unwanted padding and string literal modifications
 *      - new subclassable method transformTokens() for all token-based replacements 
 *      
 *      2015-11-02 - Code revision / enhancements (Kay Gürtzig)
 *      - Most of the transform stuff delegated to Element and Generator (KGU#18/KGU23)
 *      - Enhancement #10 (KGU#3): FOR loops now provide themselves more reliable loop parameters  
 *      - Case enabled to combine several constants/patterns in one branch (KGU#15)
 *      - The Repeat loop had been implememed in an incorrect way  
 *      
 *      2015-10-18 - Bugfixes (KGU#53, KGU#30)
 *      - Conversion of functions improved by producing headers according to BASH syntax
 *      - Conversion of For loops slightly improved (not robust, may still fail with complex expressions as loop parameters
 *      
 *      2014-11-16 - Bugfixes / Enhancement
 *      - conversion of Pascal-like logical operators "and", "or", and "not" supported 
 *      - conversion of comparison and operators accomplished
 *      - comment export introduced 
 *
 ******************************************************************************************************///

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lu.fisch.structorizer.elements.Alternative;
import lu.fisch.structorizer.elements.Call;
import lu.fisch.structorizer.elements.Case;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.Forever;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.elements.Jump;
import lu.fisch.structorizer.elements.Parallel;
import lu.fisch.structorizer.elements.Repeat;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.structorizer.elements.Try;
import lu.fisch.structorizer.elements.TypeMapEntry;
import lu.fisch.structorizer.elements.While;
import lu.fisch.structorizer.executor.Executor;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.generators.Generator.TryCatchSupportLevel;
import lu.fisch.structorizer.parsers.CodeParser;
import lu.fisch.utils.StringList;


public class BASHGenerator extends Generator {
	
	// START KGU#61 2016-03-22: Now provided by Generator class
	// Bugfix #96 (KGU#129, 2015-01-08): We must know all variable names to prefix them with '$'.
	//StringList varNames = new StringList();
	// END KGU#6 2016-03-22

	/************ Fields ***********************/
	@Override
	protected String getDialogTitle()
	{
		return "Export BASH Code ...";
	}
	
	@Override
	protected String getFileDescription()
	{
		return "BASH Source Code";
	}
	
	@Override
	protected String getIndent()
	{
		// START KGU#1204 2025-09-06: Issue #1148 A more noticeable indentation would be helpful
		//return " ";
		return "    ";
		// END KGU#1204 2025-09-06
	}
	
	@Override
	protected String[] getFileExtensions()
	{
		String[] exts = {"sh"};
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
		return false;
	}
	// END KGU#78 2015-12-18

	// START KGU#351 2017-02-26: Enh. #346 - include / import / uses config
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getIncludePattern()
	 */
	@Override
	protected String getIncludePattern()
	{
		return ". %";
	}
	// END KGU#351 2017-02-26

	// START KGU#371 2019-03-07: Enh. #385
	/**
	 * @return The level of subroutine overloading support in the target language
	 */
	@Override
	protected OverloadingLevel getOverloadingLevel() {
		return OverloadingLevel.OL_DEFAULT_ARGUMENTS;
	}
	// END KGU#371 2019-03-07

	// START KGU#686 2019-03-18: Enh. #56
	@Override
	protected TryCatchSupportLevel getTryCatchLevel()
	{
		/* The only theoretical approach coming near an exception handling
		 * would require to entangle the tried commands with && but this
		 * doesn't work recursively. FIXME */
		return TryCatchSupportLevel.TC_NO_TRY;
	}
	// END KGU#686 2019-03-18

	// START KGU#241 2016-09-01: Issue #234: names of certain occurring functions detected by checkElementInformation()
	protected StringList occurringFunctions = new StringList();
	// END KGU#241 2015-09-01

//	// START KGU 2016-08-12: Enh. #231 - information for analyser - obsolete since 3.27 
//    private static final String[] reservedWords = new String[]{
//		"if", "then", "else", "elif", "fi",
//		"select", "case", "in", "esac",
//		"for", "do", "done",
//		"while", "until",
//		"function", "return"};
//	public String[] getReservedWords()
//	{
//		return reservedWords;
//	}
//	public boolean isCaseSignificant()
//	{
//		return true;
//	}
//	// END KGU 2016-08-12
	
	/************ Code Generation **************/
	
	/** Currently exported {@link Root} */
	protected Root root = null;
	
	/** Matcher for simple variable access consisting of a dollar sign and an identifier within braces */
	protected static final Matcher VAR_ACCESS_MATCHER = Pattern.compile("[$]\\{[A-Za-z][A-Za-z0-9_]*\\}").matcher("");

	/** Name of an auxiliary function to copy associative arrays */
	private static final String FN_COPY_ASSOC_ARRAY = "auxCopyAssocArray";

	/** Set of comparison operators requiring arithmetic evaluation if placed among numeric operands */
	private final Set<String> compOprs = new HashSet<String>(
			Arrays.asList(new String[]{/*"==",*/ "<", ">", "<=", ">=", /*"!=", "<>"*/}));

	// START KGU#1206 2025-09-08: Enh. #1223 Gather all Try elements
	/** A collection of contained Try elements needed for code generation */
	private HashSet<Try> tryElememts = new HashSet<Try>();
	/** Retains whether a Jump element of flavour throw is contained in the export */
	private boolean hasThrows = false;
	// END KGU#1206 2025-09-08
	
	// START KGU#815 2020-03-27: Enh. #828 export of group modules
	/**
	 * Method converts some generic module name into a generator-specific include file name or
	 * module name for the import / use clause.<br/>
	 * To be used before adding a generic name to {@link #generatorIncludes}.
	 * This version adds a ".sh" suffix (more precisely the first filename extension method
	 * {@link #getFileExtensions()} provides). 
	 * @see #getIncludePattern()
	 * @see #appendGeneratorIncludes(String)
	 * @see #prepareUserIncludeItem(String)
	 * @param _includeName a generic (language-independent) string for the generator include configuration
	 * @return the converted string as to be actually added to {@link #generatorIncludes}
	 */
	protected String prepareGeneratorIncludeItem(String _includeName)
	{
		return _includeName + "." + this.getFileExtensions()[0];
	}
	// END KGU#815 2020-03-27
	
	// START KGU#542 2019-12-01: Enh. #739 enumeration type support - configuration for subclasses
	/** @return the shell-specific declarator for enumeration constants (e.g. {@code "declare -ri "} for bash) */
	protected String getEnumDeclarator()
	{
		return "declare -ri ";
	}
	
	/** @param isConst - whether the respective variable is to be declared read-only
	 * @return the shell-specific declarator for array variables (e.g. {@code "declare -a "} for bash) */
	protected String getArrayDeclarator(boolean isConst)
	{
		return isConst ? "declare -ar " : "declare -a ";
	}

	/** @param isConst - whether the respective variable is to be declared read-only
	 * @return the shell-specific declarator for associative arrays (maps, e.g. {@code "declare -A "} for bash) */
	protected String getAssocDeclarator(boolean isConst)
	{
		return isConst ? "declare -Ar " : "declare -A ";
	}
	// END KGU#542 2019-12-01
	
	// START KGU#803/KGU#806 2020-02-18: Issues #388, #816
	/**
	 * @return the shell-specific declarator for read-only variables
	 */
	protected String getConstDeclarator()
	{
		return "declare -r ";
	}

	/**
	 * @param isConst - whether the reference is to be readonly
	 * @return a declarator prefix for a name reference
	 */
	protected String getNameRefDeclarator(boolean isConst)
	{
		if (isConst) {
			return "declare -nr ";
		}
		return "declare -n ";
	}

	/**
	 * @param isConst - whether the subject to be declared is a constant
	 * @param type - a {@link TypeMapEntry} if available for the subject, otherwise null
	 * @return the shell-specific declarator for general local variables
	 */
	protected String getLocalDeclarator(boolean isConst, TypeMapEntry type)
	{
		if (type != null) {
			if (type.isRecord()) {
				return this.getAssocDeclarator(isConst);
			}
			else if (type.isArray()) {
				return this.getArrayDeclarator(isConst);
			}
			String typeName = type.getCanonicalType(true, true);
			if (typeName.equals("int")) {
				return "declare -i" + (isConst ? "r" : "") + " ";
			}
		}
		if (isConst) {
			return this.getConstDeclarator();
		}
		return "local ";
	}

	/**
	 * Returns an array assignment among array variables. A possible declarator (like
	 * {@code declare -a} in bash or {@code set -A} in ksh will be put as prefix if
	 * {@code tgtVar} is given, with the respective options according to the flags
	 * {@code asConstant}, {@code as Global}.<br/>
	 * If {@code tgtVar} is null then only the right-hand side of the assigment (an
	 * expression) will be returned).
	 * @param tgtVar - the target array variable (left-hand side) of the assignment or null
	 * @param srcVar - the source array variable (right-hand side) of the assignment
	 * @param getKeys - whether rather the keys/indices are to be collected
	 * @param asConstant - whether a read-only declaratioin is to be effectuated 
	 * @param asGlobal - whether an export declaration is to be effectuated
	 * @return the assignment string with declarator or just the rval without declarator
	 * (in this case the flags {@code asConstant} and {@code asGlobal} will be ignored.)
	 * @see #getArrayDeclarator(boolean)
	 */
	protected String makeArrayCopyAssignment(String tgtVar, String srcVar, boolean getKeys, boolean asConstant, boolean asGlobal)
	{
		String prefix = "";
		if (tgtVar != null) {
			prefix = this.getArrayDeclarator(asConstant);
			if (asGlobal) {
				prefix += "-g ";
			}
			prefix += tgtVar + this.getArrayInitOperator();
		}
		return prefix + "(\"${" + (getKeys ? "!" : "") + srcVar + "[@]}\")";
	}
	
	/**
	 * @return the operator symbol for array initialization
	 */
	protected String getArrayInitOperator()
	{
		return "=";
	}
	// END KGU#803/#806 2020-02-18
	
	// START KGU#753 2019-10-15: Bugfix #765 had to be made protected, since KSHGenerator must initialize it as well. 
	//private HashMap<String, TypeMapEntry> typeMap = null;
	protected HashMap<String, TypeMapEntry> typeMap = null;
	// END KGU#753 2019-10-15 
	
	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getInputReplacer(boolean)
	 */
	// START KGU#281 2016-10-15: Enh. #271 (support for input with prompt)
	//protected String getInputReplacer()
	//{
	//	return "read $1";
	//}
	@Override
	protected String getInputReplacer(boolean withPrompt)
	{
		if (withPrompt) {
			return "echo -n $1 ; read $2";
		}
		return "read $1";
	}
	// END KGU#281 2016-10-15

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getOutputReplacer()
	 */
	@Override
	protected String getOutputReplacer()
	{
		return "echo $1";
	}

	// START KGU#93 2015-12-21: Bugfix #41/#68/#69
//	/**
//	 * Transforms assignments in the given intermediate-language code line.
//	 * Replaces "<-" by "="
//	 * @param _interm - a code line in intermediate syntax
//	 * @return transformed string
//	 */
//	protected String transformAssignment(String _interm)
//	{
//		return _interm.replace(" <- ", "=");
//	}

	// START KGU#150/KGU#241 2016-09-01: Issue #234 - smarter handling of ord and chr functions
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#checkElementInformation(lu.fisch.structorizer.elements.Element)
	 */
	@Override
	protected boolean checkElementInformation(Element _ele)
	{
		StringList tokens = Element.splitLexically(_ele.getText().getText(), true);
		String[] functionNames = {"ord", "chr"};
		for (int i = 0; i < functionNames.length; i++)
		{
			if (!occurringFunctions.contains(functionNames[i])) {
				int pos = -1;
				while ((pos = tokens.indexOf(functionNames[i], pos+1)) >= 0 &&
						pos+1 < tokens.count() &&
						tokens.get(pos+1).equals("("))
				{
					occurringFunctions.add(functionNames[i]);
					break;	
				}
			}
		}
		// START KGU#1206 2025-09-08: Issue #1223 Support error handling code
		if (_ele instanceof Try) {
			this.tryElememts.add((Try)_ele);
		}
		else if (_ele instanceof Jump && ((Jump)_ele).isThrow()) {
			this.hasThrows = true;
		}
		// END KGU#1206 2025-09-08
		
		return super.checkElementInformation(_ele);
	}
	// END KGU#150/KGU#241 2016-09-01
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformTokens(lu.fisch.utils.StringList)
	 */
	@Override
	protected String transformTokens(StringList tokens)
	{
		// START KGU#803 2020-02-18: Enh. #388 (constant handling)
		boolean isConst = false;
		String declarator = "";
		String varName = "";
		String origExpr = "";
		// END KGU#803 2020-02-18
		// START KGU#1061 2022-08-23: Issue #1068
		transformIndexLists(tokens);
		// END KGU#1061 2022-08-23
		// START KGU#920 2021-02-03: Issue #920 Handle Infinity literal
		// https://unix.stackexchange.com/questions/24721/how-to-compare-to-floating-point-number-in-a-shell-script
		tokens.replaceAll("Infinity", "INF");
		// END KGU#920 2021-02-03
		// Trim the tokens at both ends (just for sure)
		tokens = tokens.trim();
		// START KGU#129 2016-01-08: Bugfix #96 - variable name processing
		// We must of course identify variable names and prefix them with $ unless being an lvalue
		int posAsgnOpr = tokens.indexOf("<-");
		// START KGU#388 2017-10-24: Enh. #335, #389, #423
		if (posAsgnOpr > 0) {
			// FIXME: Consider using lValueToTypeNameIndexComp(String) rather than reinventing all
			String token0 = tokens.get(0);
			// START KGU#803 2020-02-18: Enh. #388 - handling of constants
			if (isConst = token0.equalsIgnoreCase("const")) {
				declarator = this.getConstDeclarator();
			}
			// END KGU#803 2020-02-18
			int posColon = -1;
			if (token0.equalsIgnoreCase("var") || isConst) {
				tokens.remove(0);
				posAsgnOpr--;
				posColon = tokens.indexOf(":");
			}
			else if (token0.equalsIgnoreCase("dim")) {
				tokens.remove(0);
				posAsgnOpr--;
				posColon = tokens.indexOf("as", false);
			}
			if (posColon > 0 && posColon < posAsgnOpr) {
				tokens.remove(posColon, posAsgnOpr);
				posAsgnOpr = posColon;
			}
			// START KGU#1009 2021-11-02: Bugfix #1014 We must consider C-/Java-style declarations
			else {
				/* Assumption: the type description ends with a name or a closing bracket
				 * followed by an identifier
				 */
				int pos = posAsgnOpr - 1;
				int posVar = -1;
				boolean wasId = false;
				while (pos >= 0) {
					String token = tokens.get(pos);
					if (!token.trim().isEmpty()) {
						boolean isId = Function.testIdentifier(token, false, null);
						if (wasId && (isId || token.equals("]"))) {
							tokens.remove(0, posVar);
							posAsgnOpr -= posVar;
							// START KGU#1090 2023-10-15: Bugfix #1096
							// We must now also get rid of index stuff beyond the variable
							tokens.remove(1, posAsgnOpr);
							posAsgnOpr = 1;
							// END KGU#1090 2023-10-15
							break;
						}
						if (wasId = isId) {
							posVar = pos;
						}
					}
					pos--;
				}
			}
			// END KGU#1009 2021-10-02
			// START KGU#388 2017-10-05: Enh. #423
			int posDot = -1;
			while ((posDot = tokens.indexOf(".", posDot+1)) > 0 && posDot + 1 < posAsgnOpr) {
				if (Function.testIdentifier(tokens.get(posDot+1), false, null))
				{
					// FIXME: Handle multi-level record access! We might also check type
					tokens.set(posDot - 1, tokens.get(posDot-1) + "[" + tokens.get(posDot+1) + "]");
					tokens.remove(posDot, posDot+2);
					posAsgnOpr -= 2;
				}
			}
			// END KGU#388 2017-10-05
			origExpr = tokens.concatenate(null, posAsgnOpr + 1).trim();
		}
		// END KGU#388 2017-10-24
		// START KGU#161 2016-03-24: Bugfix #135/#92 - variables in read instructions must not be prefixed!
		if (tokens.contains(CodeParser.getKeyword("input")))
		{
			// Hide the text from the replacement, except for occurrences as index
			posAsgnOpr = tokens.count();
		}
		// END KGU#161 2016-03-24
		// START KGU#61 2016-03-21: Enh. #84/#135
		if (posAsgnOpr < 0 && !CodeParser.getKeyword("postForIn").trim().isEmpty()) posAsgnOpr = tokens.indexOf(CodeParser.getKeyword("postForIn"));
		// END KGU#61 2016-03-21
		// If there is an array variable left of the assignment symbol, check the index 
		int posBracket1 = tokens.indexOf("[");
		int posBracket2 = -1;
		if (posBracket1 >= 0 && posBracket1 < posAsgnOpr) posBracket2 = tokens.lastIndexOf("]", posAsgnOpr-1);
		for (int i = 0; i < varNames.count(); i++)
		{
			String var = varNames.get(i);
			//System.out.println("Looking for " + varName + "...");	// FIXME (KGU): Remove after Test!
			//_input = _input.replaceAll("(.*?[^\\$])" + varName + "([\\W$].*?)", "$1" + "\\$" + varName + "$2");
			// Transform the expression right of the assignment symbol
			transformVariableAccess(var, tokens, posAsgnOpr+1, tokens.count());
			// Transform the index expression on the left side of the assignment symbol
			transformVariableAccess(var, tokens, posBracket1+1, posBracket2+1);
		}

		// Position of the assignment operator may have changed now
		posAsgnOpr = tokens.indexOf("<-");
		// END KGU#96 2016-01-08
		// FIXME (KGU): Function calls, math expressions etc. will have to be put into brackets etc. pp.
		tokens.replaceAll("div", "/");
		tokens.replaceAllCi("false", "0");
		tokens.replaceAllCi("true", "1");
		// START KGU#164 2016-03-29: Bugfix #138 - function calls weren't handled
		//return tokens.concatenate();
		String lval = "";
		if (posAsgnOpr > 0)
		{
			// Separate lval and assignment operator from the expression tokens
			varName = tokens.concatenate("", 0, posAsgnOpr).trim();
			lval += varName + "=";
			tokens = tokens.subSequence(posAsgnOpr+1, tokens.count());
			// START KGU#803 2020-02-18: Issues #388, #816
			// We don't know whether we might process a call here, so better don't set handled entry
			if (Function.testIdentifier(varName, false, null) && !this.wasDefHandled(root, varName, false)
					&& root.isSubroutine()) {
				declarator = this.getLocalDeclarator(isConst, typeMap.get(varName));
			}
			// END KGU#803 2020-02-18
		}
		else if (tokens.count() > 0)
		{
			// Since keywords have already been replaced by super.transform(String), this is quite fine
			// 
			String[] keywords = CodeParser.getAllProperties();
			boolean startsWithKeyword = false;
			String firstToken = tokens.get(0);
			for (int kwi = 0; !startsWithKeyword && kwi < keywords.length; kwi++)
			{
				if (firstToken.equals(keywords[kwi]))
				{
					lval = firstToken + " ";
					tokens.delete(0);
					startsWithKeyword = true;
				}
			}
		}
		// Trim the tokens (at front)
		tokens = tokens.trim();
		// Re-combine the rval expression to a string 
		String expr = tokens.concatenate();
		// If the expression is a function call, then convert it to shell syntax
		// (i.e. drop the parentheses and dissolve the argument list)
		Function fct = new Function(expr);
		// START KGU#388 2017-10-24: Enh. #423
		HashMap<String, String> recordIni = null;
		// END KGU#388 2017-10-24
		if (fct.isFunction())
		{
			// START KGU#405 2017-05-19: Bugfix #237 - was too simple an analysis
			//expr = fct.getName();
			//for (int p = 0; p < fct.paramCount(); p++)
			//{
			//	String param = fct.getParam(p);
			//	if (param.matches("(.*?)(-|[+*/%])(.*?)"))
			//	{
			//		param = "$(( " + param + " ))";
			//	}
			//	else if (param.contains(" "))
			//	{
			//		param = "\"" + param + "\"";
			//	}
			//	expr += (" " + param);
			//}
			expr = transformExpression(fct);
			// END KGU#405 2017-05-19
			//if (posAsgnOpr > 0)
			boolean isRoutine = this.routinePool != null 
					&& !this.routinePool.findRoutinesBySignature(fct.getName(), fct.paramCount(), null, false).isEmpty();
			// START KGU#803 2020-02-16: Issue #816 don't assign the result of functions directly
			//if (posAsgnOpr > 0 && !isRoutine)
			// END KGU 2019-12-01
			//{
			//	expr = "$(" + expr + ")";
			//}
			if (posAsgnOpr > 0) {
				// KGU 2019-12-01: An evaluation should not apply for subroutines!
				if (isRoutine) {
					lval = "";	// Assignment is to be handled by the CALL element afterwards
					declarator = "";
				}
				else {
					/* FIXME This would just collect all (console) output of the function */
					expr = "$( " + expr + " )";
				}
			}
			// END KGU#803 2020-02-16
		}
		// FIXME (KGU 2019-12-01) this looks too simplistic
		else if (expr.startsWith("{") && expr.endsWith("}") && posAsgnOpr > 0)
		{
			// Array initializer
			declarator = this.getArrayDeclarator(isConst);
			boolean isAssignment = lval.endsWith("=");
			if (isAssignment) {
				lval = lval.substring(0, lval.length()-1) + this.getArrayInitOperator();
			}
			StringList items = Element.splitExpressionList(expr.substring(1, expr.length()-1), ",");
			// START KGU#405 2017-05-19: Bugfix #237 - was too simple an analysis
			for (int i = 0; i < items.count(); i++) {
				items.set(i, transformExpression(items.get(i), true, false));
			}
			// END KGU#405 2017-05-19
			// START KGU#803 2020-02-20: Issue #816, #423
			//expr = "(" + items.getLongString() + ")";
			expr = items.getLongString();
			if (!(isAssignment && this.getArrayInitOperator().equals(" ") /* indicator for ksh */)) {
				expr = "(" + expr + ")";
			}
			// END KGU#803 2020-02-20
		}
		// START KGU#388 2017-10-24: Enh. #423
		else if (tokens.count() > 2 && Function.testIdentifier(tokens.get(0), false, null)
				&& tokens.get(1).equals("{") && expr.endsWith("}")
				// START KGU#559 2018-07-20: Enh. #  Try to fetch sufficient type info
				//&& (recordIni = Element.splitRecordInitializer(expr, null)) != null) {
				&& (recordIni = Element.splitRecordInitializer(expr, this.typeMap.get(":"+tokens.get(0)), false)) != null) {
				// END KGU#559 2018-07-20
			// Record initializer
			// START KGU#388 2019-11-28: Bugfix #423 - record initializations must not be separated from the declaration
			declarator = this.getAssocDeclarator(false);	// Repetition of declaration doesn't cause harm
			// END KGU#388 2019-11-28
			StringBuilder sb = new StringBuilder(15 * recordIni.size());
			String sepa = "(";
			for (Entry<String, String> entry: recordIni.entrySet()) {
				String key = entry.getKey();
				if (!key.startsWith("§")) {
					// START KGU#807 2020-02-21 Bugfix #821 - we must do specific expression transformation
					//sb.append(sepa + '[' + key + "]=" + entry.getValue());
					sb.append(sepa + '[' + key + "]=" + this.transformExpression(entry.getValue(), true, false));
					// END KGU#807 2020-02-21
					sepa = " ";
				}
			}
			// START KGU#771 2019-11-24: Bugfix #783 - fallback for the case of missing struct info
			//sb.append(")");
			//expr = sb.toString();
			// If the type info was available or didn't provide any content then leave expr as is
			if (sb.length() > 0) {
				sb.append(")");
				expr = sb.toString();
			}
			// END KGU#771 2019-11-24
		}
		// END KGU#388 2017-10-24
		// The following is a very rough and vague heuristics to support arithmetic expressions 
		else if ( !(expr.startsWith("(") && expr.endsWith(")")
				|| expr.startsWith("`") && expr.endsWith("`")
				|| expr.startsWith("'") && expr.endsWith("'")
				|| expr.startsWith("\"") && expr.endsWith("\"")
				|| expr.startsWith("[[") && expr.endsWith("]]")))
		{
			if (expr.matches(".*?[+*/%-].*?"))
			{
				// START KGU#405 2017-05-19: Issue #237
				//expr = "(( " + expr + " ))";
				//if (posAsgnOpr > 0)
				//{
				//	expr = "$" + expr;
				//}
				expr = transformExpression(expr, posAsgnOpr > 0, false);
				// END KGU#405 2017-05-19
			}
			// START KGU 2016-03-31: Issue #135+#144 - quoting wasn't actually helpful
//			else if (expr.contains(" "))
//			{
//				expr = "\"" + expr + "\"";
//			}
			// END KGU 2016-03-31
			// START KGU#807 2020-02-24: Issue #821 Care for result assignments from complex variable
			// FIXME: This is of course a very limited partial workaround and should be revised with #800
			if (varNames.contains(origExpr) && root.isSubroutine()
					&& (varName.equalsIgnoreCase("result") || varName.equals(root.getMethodName()))) {
				TypeMapEntry type1 = typeMap.get(varName);
				TypeMapEntry type2 = typeMap.get(origExpr);
				if (type1 != null && type2 != null) {
					if (type1.isArray() && type2.isArray()) {
						expr = makeArrayCopyAssignment(null, origExpr, false, false, false);
					}
					else if (type1.isRecord() && type2.isRecord()) {
						// Now this is getting tricky as we'd need to copy the associative array in one line
						lval = varName + "; ";
						expr = FN_COPY_ASSOC_ARRAY + " " + varName + " " + origExpr;
					}
				}
			}
			// END KGU#807 2020-02-24
		}
		if (!declarator.isEmpty() && Function.testIdentifier(varName, false, null)) {
			wasDefHandled(root, varName, true);	// Set the handled flag
		}
		return declarator + lval + expr;
		// END KGU#164 2016-03-29
	}
	// END KGU#93 2015-12-21

	// END KGU#18/KGU#23 2015-11-01
	
	// START KGU#405 2017-05-19: Issue #237
	/**
	 * Does some specific transformation for expressions, possibly being used as
	 * arguments of a routine CALL, in which case array and record variables have
	 * to be passed by name, such that "dollaring" must be undone.
	 * @param exprTokens - the lexically split expression, may contain blanks
	 * @param isAssigned - if the expression is used as right term of an assignment
	 * @param asArgument - if the expression is an argument for a routine CALL
	 * @return the transformed expression
	 */
	protected String transformExpression(StringList exprTokens, boolean isAssigned, boolean asArgument)
	{
		// FIXME: Check the operands - they must be literals (type detectable),
		// variables (consult typeMap), built-in functions (type known), or
		// recursively constructed expressions themselves (analyse recursively)
		// This static type check should be implemented as static method on Element
		// but needs access to the typeMap of the current Root.
		boolean isArithm =
				exprTokens.contains("+") ||
				exprTokens.contains("-") ||
				exprTokens.contains("*") ||
				exprTokens.contains("/") ||
				exprTokens.contains("%");
		// Avoid recursive enclosing in $(...)
		if (isArithm) {
			exprTokens.insert(((isAssigned || asArgument) ? "$(( " : "(( "), 0);
			exprTokens.add(" ))");
		}
		// START KGU#772 2019-11-24: Bugfix #784 - avoid redundant enclosing with $(...)
		//else if (isAssigned) {
		//	exprTokens.insert("$(", 0);
		//	exprTokens.add(")");
		//}
		// START KGU#807 2020-02-20: Bugfix #821 - assigned or not doesn't seem to play a role here
//		else if (isAssigned) {
		else {
		// END KGU#807 2020-02-20
			String varName = null;
			boolean isVarAccess =
					exprTokens.count() == 4 &&
					exprTokens.get(0).equals("$") &&
					exprTokens.get(1).equals("{") &&
					this.varNames.contains(varName = exprTokens.get(2)) &&
					exprTokens.get(3).equals("}") ||
					exprTokens.count() == 1 &&
					VAR_ACCESS_MATCHER.reset(exprTokens.get(0)).matches() &&
					this.varNames.contains(varName = exprTokens.get(0).substring(2, exprTokens.get(0).length()-1));
			if (isVarAccess) {
				// START KGU#803 2020-02-19: Issue #816
				//exprTokens.insert("\"", 0);
				//exprTokens.add("\"");
				TypeMapEntry typeEntry = this.typeMap.get(varName);
				if (typeEntry != null && (typeEntry.isArray() || typeEntry.isRecord())) {
					// We have to prepare a name reference, so undo the "dollaring"
					exprTokens.clear();
					exprTokens.add(varName);
					// START KGU#807 2020-02-20: Issue #821
					if (!asArgument) {
						/* This is a rather desperate approach to conserve as much information
						 * as possible - we produce a reconstruction command, though with the
						 * original variable name, which is a rather poor idea
						 */
						exprTokens.add(")\"");
						exprTokens.insert("\"$(typeset -p ", 0);
					}
					// END KGU#807 2020-02-20
				}
				else {
					exprTokens.insert("\"", 0);
					exprTokens.add("\"");
				}
				// END KGU#803 2020-02-19
			}
			// KGU#807 2020-02-20 Bugfix #821 disabled as it is nonsense in most cases, especially for literals
			//else {
			//	exprTokens.insert("$( ", 0);
			//	exprTokens.add(" )");
			//}
			// KGU#807 2020-02-20
		}
		// END KGU#772 2019-11-24
		return exprTokens.concatenate();
	}
	protected String transformExpression(String expr, boolean isAssigned, boolean asArgument)
	{
		if (Function.isFunction(expr, false)) {
			// It cannot be a diagram call here, so it must be some built-in function
			expr = this.transformExpression(new Function(expr));
			if (isAssigned || asArgument) {
				expr = "$( " + expr + " )";
			}
		}
		else {
			expr = transformExpression(Element.splitLexically(expr, true), isAssigned, asArgument);
		}
		return expr;
	}
	protected String transformExpression(Function fct)
	{
		String expr = fct.getName();
		for (int p = 0; p < fct.paramCount(); p++)
		{
			String param = fct.getParam(p);
			param = this.transformExpression(param, false, true);
			expr += (" " + param);
		}
		return expr;
	}
	/**
	 * Tries to find out whether the condition might involve a numerical comparison and
	 * encloses it with (( )) in this case, otherwise with [[ ]].
	 * @param condition the transformed condition
	 * @return the enclosed condition
	 */
	private String finishCondition(String condition) {
		// KGU#811 2020-02-21: Bugfix #824 - many absurd bugs found and fixed
		if (!this.suppressTransformation && !(condition.trim().matches("^\\(\\(.*?\\)\\)$")))
		{
			StringList condTokens = Element.splitLexically(condition, true);
			condTokens.removeAll(" ");
			boolean isNumber = false;
			for (int i = 1; !isNumber && i < condTokens.count()-1; i++) {
				String token = condTokens.get(i);
				// Check for comparison operator
				if (compOprs.contains(token)) {
					// FIXME this is too vague again
					int k = i-1;
					// fetch some operand-like token to the left
					String leftOpnd = condTokens.get(k);
					while ((leftOpnd.equals(")") || leftOpnd.equals("}"))
							&& k > 0) {
						leftOpnd = condTokens.get(--k);
					}
					// fetch some operand-like token to the right
					k = i+1;
					String rightOpnd = condTokens.get(k);
					while ((rightOpnd.isEmpty() || rightOpnd.equals("$") || rightOpnd.equals("(") || rightOpnd.equals("{"))
							&& k < condTokens.count()-1) {
						rightOpnd = condTokens.get(++k);
					}
					String typeLeft = Element.identifyExprType(typeMap, leftOpnd, true);
					String typeRight = Element.identifyExprType(typeMap, rightOpnd, true);
					if (!(typeLeft.isEmpty() && typeRight.isEmpty())
							&& (typeLeft.equals("int") || typeLeft.equals("double") || typeLeft.isEmpty())
							&& (typeRight.equals("int") || typeRight.equals("double") || typeRight.isEmpty())) {
						isNumber = true;
					}
				}
			}
			if (isNumber) {
				condition = "(( " + condition + " ))";
			}
			else {
				condition = "[[ " + condition + " ]]";
			}
		}
		return condition;
	}
	// END KGU#405 2017-05-10
	
	// START KGU#167 2016-03-30: Enh. #135 Array support
	protected void transformVariableAccess(String _varName, StringList _tokens, int _start, int _end)
	{
		int pos = _start-1;
		while ((pos = _tokens.indexOf(_varName, pos+1)) >= 0 && pos < _end)
		{
			int posNext = pos+1;
			while (posNext < _end && _tokens.get(posNext).trim().isEmpty()) posNext++;
			String nextToken = _tokens.get(posNext); 
			if (nextToken.equals("["))
			{
				_tokens.set(pos, "${" + _varName);
				// index brackets follow, so remove the blanks
				for (int i = 0; i < posNext - pos-1; i++)
				{
					_tokens.delete(pos+1);
					_end--;
				}
				// find the corresponding closing bracket
				int depth = 1;
				for (posNext = pos+2; depth > 0 && posNext < _end; posNext++)
				{
					String token = _tokens.get(posNext);
					if (token.equals("["))
					{
						depth++;
					}
					else if (token.equals("]"))
					{
						if (--depth <= 0)
						{
							_tokens.set(posNext, "]}");
						}
					}
				}
			}
			// START KGU#388 2017-10-05: Enh. #423 (record export)
			else if (nextToken.equals(".") && posNext+1 < _end && Function.testIdentifier(_tokens.get(posNext+1), false, null))
			{
				// FIXME: Handle multi-level record access! We might also check type
				_tokens.set(pos, "${" + _varName + "[" + _tokens.get(posNext+1) + "]}");
				_tokens.remove(posNext, posNext+2);
				_end -= 2;
			}
			// END KGU#388 2017-10-05
			else
			{
				_tokens.set(pos, "${" + _varName + "}");
			}
		}
	}

	// END KGU#167 2016-03-30

	// START KGU#101 2015-12-22: Enh. #54 - handling of multiple expressions
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformInput(java.lang.String)
	 */
	@Override
	protected String transformOutput(String _interm)
	{
		// START KGU#1193 2025-08-20: Bugfix #1210 We must not ignore this mode
		if (this.suppressTransformation) {
			return super.transformOutput(_interm);
		}
		// END KGU#1193 2025-08-20
		String output = CodeParser.getKeyword("output").trim();
		if (_interm.matches("^" + output + "[ ](.*?)"))
		{
			StringList expressions = 
					Element.splitExpressionList(_interm.substring(output.length()), ",");
			expressions.removeAll(" ");
			// START KGU#1190 2025-08-17: Bugfix #1207 We must convert the expressions separately
			String dummyVar = "dummy" + Integer.toHexString(this.hashCode());
			for (int i = 0; i < expressions.count(); i++) {
				String expr = this.transform(dummyVar + " <- " + expressions.get(i), false);
				// START KGU#1190 2025-08-19: Bugfix #1207 in case of arrays etc. there may be a prefix
				//if (expr.startsWith(dummyVar + "=")) {
				//	expr = expr.substring(dummyVar.length()+1);
				//}
				int posDummy = expr.indexOf(dummyVar + "=");
				if (posDummy >= 0) {
					String prefix = expr.substring(0, posDummy).trim();
					if (prefix.isEmpty() || this.getAssignmentPrefixes().contains(prefix)) {
						expr = expr.substring(posDummy + dummyVar.length()+1);
					}
				}
				// END KGU#1190 2025-08-19
				expressions.set(i,  expr);
			}
			// END KGU#1190 2025-08-17
			_interm = output + " " + expressions.getLongString();
		}
		
		String transformed = super.transformOutput(_interm);
		// START KGU#1190 2025-08-17: Bugfix #1207 "print" seems impossible here
		//if (transformed.startsWith("print , "))
		//{
		//	transformed = transformed.replace("print , ", "print ");
		//}
		// END KGU#1190 2025-08-17
		return transformed;
	}
	// END KGU#101 2015-12-22
	
	// START KGU#1190 2025-08-19: Bugfix #1207 Shell-specific configuration
	/**
	 * @return a StringList of shell-specific assignment prefixes like "local",
	 *     "typeset"
	 */
	protected StringList getAssignmentPrefixes() {
		return StringList.explode("local", ",");
	}
	// END KGU#1190 2025-08-19

	// START KGU#18/KGU#23 2015-11-02: Most of the stuff became obsolete by subclassing
	@Override
	// START KGU#1190 2025-08-17: Bugfix #1207 More appropriate overriding
	//protected String transform(String _input)
	//{
	//	String intermed = super.transform(_input);
	protected String transform(String _input, boolean _doInputOutput)
	{
		String intermed = super.transform(_input, _doInputOutput);
	// END KGU##1190 2025-08-17
		
		// START KGU#162 2016-03-31: Enh. #144
		if (!this.suppressTransformation)
		{
		// END KGU#162 2016-03-31
		
			// START KGU 2014-11-16 Support for Pascal-style operators		
			intermed = intermed.replace(" div ", " / ");
			// END KGU 2014-11-06
			
		// START KGU#1193 2025-08-28: Issue #1210 More precise rules
		}
		// END KGU#1193 2025-08-28
			
		// START KGU#78 2015-12-19: Enh. #23: We only have to ensure the correct keywords
		// START KGU#288 2016-11-06: Issue #279 - some JREs don't know method getOrDefault()
		//String preLeave = CodeParser.keywordMap.getOrDefault("preLeave","").trim();
		//String preReturn = CodeParser.keywordMap.getOrDefault("preReturn","").trim();
		//String preExit = CodeParser.keywordMap.getOrDefault("preExit","").trim();
		String preLeave = CodeParser.getKeywordOrDefault("preLeave","leave").trim();
		String preReturn = CodeParser.getKeywordOrDefault("preReturn","return").trim();
		String preExit = CodeParser.getKeywordOrDefault("preExit","exit").trim();
		// END KGU#288 2016-11-06
		if (intermed.matches("^" + Matcher.quoteReplacement(preLeave) + "(\\W.*|$)"))
		{
			intermed = "break " + intermed.substring(preLeave.length());
		}
		else if (intermed.matches("^" + Matcher.quoteReplacement(preReturn) + "(\\W.*|$)"))
		{
			// FIXME KGU#803 2020-02-16: Issue #816 - should only be reached within a main or includable diagram now
			intermed = "return " + intermed.substring(preReturn.length());
		}
		else if (intermed.matches("^" + Matcher.quoteReplacement(preExit) + "(\\W.*|$)"))
		{
			intermed = "exit " + intermed.substring(preExit.length());
		} 
		// END KGU#78 2015-12-19
			
		// START KGU#162 2016-03-31: Enh. #144 | KGU#1193 2025-08-28: Issue #1210 reverted
		//}
		// END KGU#162 2016-03-31
		

		// START KGU#114 2015-12-22: Bugfix #71
		//return _input.trim();
		return intermed.trim();
		// END KGU#114 2015-12-22
	}
	
	/* (non-Javadoc)
	 * Generates a ":" line if the Subqueue contains only empty instructions
	 * @see lu.fisch.structorizer.generators.Generator#generateCode(lu.fisch.structorizer.elements.Subqueue, java.lang.String)
	 */
	@Override
	protected void generateCode(Subqueue _subqueue, String _indent)
	{
		super.generateCode(_subqueue, _indent);
		if (_subqueue.isNoOp()) {
			addCode(":", _indent, _subqueue.isDisabled(false));
		}
		// START KGU#1206 2025-09-07: Issue #1223 Ugly workaround
		else if (!getLineEnd(_subqueue).isEmpty()) {
			// If the sequence forms a conjunction we must add true to complete the syntax
			addCode("true", _indent, _subqueue.isDisabled(false));
		}
		// END KGU#1206-09-07
	}

	@Override
	protected void generateCode(Instruction _inst, String _indent) {
		
		if (!appendAsComment(_inst, _indent)) {
			// START KGU#1206 2025-09-07: Issue #1223 Try mechanism
			String lineEnd = this.getLineEnd(_inst);
			// END KGU#1206 2025-09-07
			// START KGU 2014-11-16
			appendComment(_inst, _indent);
			boolean disabled = _inst.isDisabled(false);
			// END KGU 2014-11-16
			StringList text = _inst.getUnbrokenText(); 
			// START KGU#803 2020-02-16: Issue #816
			String preReturn = CodeParser.getKeywordOrDefault("preReturn","return").trim();
			// END KGU#803 2020-02-16
			// START KGU#1190 2025-08-17: Bugfix #1207
			String preOutput = CodeParser.getKeywordOrDefault("preOutput","OUTPUT").trim();
			// END KGU#1190 2025-08-17
			int nLines = text.count();
			for (int i = 0; i < nLines; i++) {
				// START KGU#277/KGU#284 2016-10-13/16: Enh. #270 + Enh. #274
				//code.add(_indent + transform(_inst.getText().get(i)));
				String line = text.get(i);
				// START KGU#1193 2025-08-20: Bugfix #1210 We must not transform if transformation is suppressed
				if (!this.suppressTransformation) {
				// END KGU#1193 2025-08-20
					// START KGU#653 2019-02-15: Enh. #680 - special treatment for multi-variable input instructions
					StringList inputItems = Instruction.getInputItems(line);
					// START KGU#803 2020-02-17: Issue #816 ensure local declaration where necessary
					if (inputItems != null) {
						for (int j = 1; j < inputItems.count(); j++) {
							String target = inputItems.get(j);
							int cutPos = Math.min((target+".").indexOf("."), (target+"[").indexOf("["));
							if (Function.testIdentifier(target.substring(cutPos), false, null)
									&& !this.wasDefHandled(root, target, !disabled)
									&& root.isSubroutine()) {
								// START KGU#1206 2025-09-07: Issue #1223
								//addCode(getLocalDeclarator(false, typeMap.get(target)) + target, _indent, disabled);
								addCode(getLocalDeclarator(false, typeMap.get(target)) + target + lineEnd, _indent, disabled);
								// END KGU#1206 2025-09-07
							}
						}
						if (inputItems.count() > 2) {
							String prompt = inputItems.get(0);
							if (!prompt.isEmpty()) {
								// START KGU#1206 2025-09-07: Issue #1223
								//addCode(transform(CodeParser.getKeyword("output") + " " + prompt), _indent, disabled);
								addCode(transform(CodeParser.getKeyword("output") + " " + prompt) + lineEnd, _indent, disabled);
								// END KGU#1206 2025-09-07
							}
							for (int j = 1; j < inputItems.count(); j++) {
								String item = transform(inputItems.get(j) + " <-");
								int posEq = item.lastIndexOf("=");
								if (posEq > 0) {
									item = item.substring(0, posEq);
								}
								inputItems.set(j, item);
							}
							// START KGU#1206 2025-09-07: Issue #1223
							//addCode(this.getInputReplacer(false).replace("$1", inputItems.concatenate(" ", 1)), _indent, disabled);
							addCode(this.getInputReplacer(false).replace("$1", inputItems.concatenate(" ", 1)) + lineEnd, _indent, disabled);
							// END KGU#1206 2025-09-07
							continue;
						}
					}
					// END KGU#803 2020-02-17
					// END KGU#653 2019-02-15
					// START KGU#388/KGU#772 2017-10-24/2019-11-24: Enh. #423/bugfix #784 ignore type definitions and mere variable declarations
					//if (Instruction.isTypeDefinition(line)) {
					if (Instruction.isMereDeclaration(line)) {
						// local declaration should have been handled by generateCode(Root)
						continue;
					}
					// START KGU#803 2020-02-16: Issue #816 A return has to be handled specifically
					if (root.isSubroutine() && (line.matches("^" + Matcher.quoteReplacement(preReturn) + "(\\W.*|$)"))) {
						String expr = line.substring(preReturn.length()).trim();
						// START KGU#1205 2025-09-06: Issue #1210 no workaround with suppressTransformation!
						if (suppressTransformation) {
							addCode("return " + expr, _indent, disabled);
							// Further lines are hardly interesting (not reachable)...
							disabled = true;
							continue;
						}
						// END KGU#1205 2025-09-05
						// START KGU#1206 2025-09-07: Issue #1223 Ugly workaround
						//generateResultVariables(expr, _indent, disabled);
						String indent1 = _indent;
						if (!lineEnd.isBlank()) {
							addCode("{", _indent, disabled);
							indent1 = _indent+this.getIndent();
						}
						generateResultVariables(expr, indent1, disabled);
						if (!lineEnd.isBlank()) {
							addCode("true }", _indent, disabled);
						}
						// END KGU#1206 2025-09-07
						// In case of an endstanding return we don't need a formal return command
						if (i < nLines-1 || root.children.getElement(root.children.getSize()-1) != _inst) {
							addCode("return 0", _indent, disabled);
						}
						continue;
					}
					// END KGU#803 2020-02-16
					// END KGU#388/KGU#772 2017-10-24/2019-11-24
					// START KGU#1190 2025-08-17: Bugfix #1207: We must handle output explicitly
					if (unifyKeywords(Element.splitLexically(line, true)).indexOf(preOutput) == 0) {
						String transf = this.transformOutput(line);
						// START KGU#1206 2025-09-07: Issue #1223
						//addCode(transf, _indent, disabled);
						addCode(transf + lineEnd, _indent, disabled);
						// END KGU#1206 2025-09-07
						continue;
					}
					// END KGU#1190 2025-08-17
				// START KGU#1193 2025-08-20: Bugfix #1210
				}
				// END KGU#1193 2025-08-20
				String codeLine = transform(line);
				// START KGU#1206 2025-09-07: Issue #1223
				if (!codeLine.isBlank()) {
					codeLine += lineEnd;
				}
				// END KGU#1206 2025-09-07
				/* FIXME KGU#803 2020-02-16: Issue #816 - we should mark local variables as local
				 * This requires to check whether line is an assignment, that the target variable
				 * is not a parameter or input variable (how to declare these?) and it hasn't been
				 * assigned or input before. Then "local " could be put as prefix to codeLine.
				 */
				// START KGU#311 2017-01-05: Enh. #314: We should at least put some File API remarks
				if (this.usesFileAPI && !disabled) {
					for (int j = 0; j < Executor.fileAPI_names.length; j++) {
						if (line.contains(Executor.fileAPI_names[j] + "(")) {
							appendComment("TODO File API: Replace the \"" + Executor.fileAPI_names[j] + "\" call by an appropriate shell construct", _indent);
							break;
						}
					}
				}
				// END KGU#311 2017-01-05
				if (Instruction.isTurtleizerMove(line)) {
					codeLine += " " + this.commentSymbolLeft() + " color = " + _inst.getHexColor();
				}
				// START KGU#383 2017-04-18: Bugfix #386 - suppress sole empty line
				//addCode(codeLine, _indent, disabled);
				if (!codeLine.trim().isEmpty() || nLines > 1) {
					addCode(codeLine, _indent, disabled);
				}
				// END KGU#383 2017-04-18
				// END KGU#277/KGU#284 2016-10-13
			}
		}

	}

	/**
	 * Auxiliary method for transforming result mechanism into some sensible shell code.
	 * @param expr - the transformed result expression
	 * @param _indent - the current indentation string
	 * @param disabled - whether the element is disabled, i.e. the code is to be commented out
	 */
	private void generateResultVariables(String expr, String _indent, boolean disabled) {
		String resultName = "result" + Integer.toHexString(root.hashCode());
		// Check for array or record
		String varName = expr;
		if (varName.startsWith("${") && varName.endsWith("}")) {
			varName = varName.substring(2, varName.length()-1);
		}
		else if (varName.startsWith("$")) {
			varName = varName.substring(1);
		}
		if (varNames.contains(varName)) {
			TypeMapEntry typeEntry = this.typeMap.get(varName);
			String resultType = root.getResultType();
			TypeMapEntry resTypeEntry = null;
			boolean isArray = typeEntry != null && typeEntry.isArray()
					|| resultType != null && (resultType.startsWith("@") || resultType.startsWith("array "));
			boolean isRecord = typeEntry != null && typeEntry.isRecord();
			if (resultType != null && !resultType.trim().isEmpty()
					&& (resTypeEntry = this.typeMap.get(":"+resultType)) != null) {
				if (resTypeEntry.isArray()) {
					isArray = true;
				}
				else if (resTypeEntry.isRecord()) {
					isRecord = true;
				}
			}
			if (isArray) {
				addCode(makeArrayCopyAssignment(resultName, varName, false, false, true), _indent, disabled);
				return;
			}
			else if (isRecord) {
				addCode(makeArrayCopyAssignment(resultName+"keys", varName, true, false, true), _indent, disabled);
				addCode(makeArrayCopyAssignment(resultName+"values", varName, false, false, true), _indent, disabled);
				return;
			}
		}
		String codeLine = transform(resultName + " <- " + expr);
		// START KGU#1193 2025-09-02: Bugfix #1210 We may not always expect a translation...
		//addCode(resultName + codeLine.substring(codeLine.indexOf("=")), _indent, disabled);
		int posAsgn = codeLine.indexOf("=");
		if (posAsgn > 0) {
			expr = codeLine.substring(posAsgn);
		}
		else {
			// Maybe mode suppressTransformation
			expr = "=" + expr;
		}
		addCode(resultName + expr, _indent, disabled);
		// END KGU#1193 2025-09-02
	}

	@Override
	protected void generateCode(Alternative _alt, String _indent) {
		
		boolean disabled = _alt.isDisabled(false);
		this.addSepaLine();
		// START KGU 2014-11-16
		appendComment(_alt, _indent);
		// END KGU 2014-11-16
		// START KGU#132 2016-01-08: Bugfix #96 - approach with C-like syntax
		//code.add(_indent+"if "+BString.replace(transform(_alt.getText().getText()),"\n","").trim());
		// START KGU#132 2016-03-24: Bugfix #96/#135 second approach with [[ ]] instead of (( ))
		//code.add(_indent+"if (( "+BString.replace(transform(_alt.getText().getText()),"\n","").trim() + " ))");
		// START KGU#453 2017-11-02: Issue #447
		//String condition = transform(_alt.getText().getLongString()).trim();
		String condition = transform(_alt.getUnbrokenText().getLongString()).trim();
		// END KGU#453 2017-11-02
		// START KGU#311 2017-01-05: Enh. #314: We should at least put some File API remarks
		if (this.usesFileAPI && !disabled) {
			for (int j = 0; j < Executor.fileAPI_names.length; j++) {
				if (condition.contains(Executor.fileAPI_names[j] + "(")) {
					appendComment("TODO File API: Replace the \"" + Executor.fileAPI_names[j] + "\" call by an appropriate shell construct", _indent);
					break;
				}
			}
		}
		// END KGU#311 2017-01-05
		// START KGU#277 2016-10-13: Enh. #270
		//code.add(_indent + "if " + condition);
		addCode("if " + finishCondition(condition), _indent, disabled);
		// END KGU#277 2016-10-13
		// END KGU#132 2016-03-24
		// END KGU#131 2016-01-08
		// START KGU#277 2016-10-13: Enh. #270
		//code.add(_indent+"then");
		addCode("then", _indent, disabled);
		// END KGU#277 2016-10-13
		generateCode(_alt.qTrue, _indent+this.getIndent());
		
		// START KGU#1204 2025-09-06: Issue #1148 We ought to make use of the ELSIF if possible
		Element ele = null;
		// We must cater for the code mapping of the chained sub-alternatives
		Stack<Element> processedAlts = new Stack<Element>();
		Stack<Integer> storedLineNos = new Stack<Integer>();
		while (_alt.qFalse.getSize() == 1 
				&& (ele = _alt.qFalse.getElement(0)) instanceof Alternative) {
			_alt = (Alternative)ele;
			// We must care for the code mapping explicitly here since we circumvent generateCode()
			markElementStart(_alt, _indent, processedAlts, storedLineNos);
			appendComment(_alt, _indent);
			condition = transform(_alt.getUnbrokenText().getLongString()).trim();
			// START KGU#311 2017-01-05: Enh. #314: We should at least put some File API remarks
			if (this.usesFileAPI && !disabled) {
				for (int j = 0; j < Executor.fileAPI_names.length; j++) {
					if (condition.contains(Executor.fileAPI_names[j] + "(")) {
						appendComment("TODO File API: Replace the \"" + Executor.fileAPI_names[j] + "\" call by an appropriate shell construct", _indent);
						break;
					}
				}
			}
			// END KGU#311 2017-01-05
			addCode("elif "+ finishCondition(condition),
					_indent, ele.isDisabled(false));
			addCode("then",
					_indent, ele.isDisabled(false));
			generateCode(_alt.qTrue, _indent+this.getIndent());
		}
		// END KGU#1204 2025-09-06
		
		if (_alt.qFalse.getSize() != 0) {
			
			// START KGU#277 2016-10-13: Enh. #270
			//code.add(_indent+"");
			//code.add(_indent+"else");
			// START KGU#1204 2025-09-06: Issue #1148 Unnecessary blank line dropped
			//if (!code.get(code.count()-1).trim().isEmpty()) {
			//	addCode("", "", disabled);
			//}
			// END KGU#1204 2025-09-06
			addCode("else", _indent, disabled);			
			// END KGU#277 2016-10-13
			generateCode(_alt.qFalse,_indent+this.getIndent());
			
		}
		
		// START KGU#277 2016-10-13: Enh. #270
		//code.add(_indent+"fi");
		//addSepaLine();
		// START KGU#1206 2025-09-07: Issue #1223 Try mechanism
		//addCode("fi", _indent, disabled);
		addCode("fi" + this.getLineEnd(_alt), _indent, disabled);
		// END KGU#1206 2025-09-07
		addCode("", "", disabled);
		// END KGU#277 2016-10-13

		// START KGU#1204 2025-09-06: Issue #1148 Accomplish the code map for the processed child alternatives
		markElementEnds(processedAlts, storedLineNos);
		// END KGU#1204 2025-09-06
	
	}
	
	@Override
	protected void generateCode(Case _case, String _indent) {
		
		boolean disabled = _case.isDisabled(false);
		addSepaLine();
		// START KGU 2014-11-16
		appendComment(_case, _indent);
		// END KGU 2014-11-16
		// START KGU#277 2016-10-14: Enh. #270
		//code.add(_indent+"case "+transform(_case.getText().get(0))+" in");
		// START KGU#453 2017-11-02: Issue #447
		//addCode("case "+transform(_case.getText().get(0))+" in", _indent, disabled);
		StringList unbrokenText = _case.getUnbrokenText();
		addCode("case "+transform(unbrokenText.get(0))+" in", _indent, disabled);
		// END KGU#453 2017-11-02
		// END KGU#277 2016-10-14
		
		for (int i=0; i<_case.qs.size()-1; i++)
		{
			// START KGU#277 2016-10-14: Enh. #270
			//code.add("");
			//code.add(_indent + this.getIndent() + _case.getText().get(i+1).trim().replace(",", "|") + ")");
			addCode("", "", disabled);
			// START KGU#453 2017-11-02: Issue #447
			//addCode(this.getIndent() + _case.getText().get(i+1).trim().replace(",", "|") + ")", _indent, disabled);
			// START KGU#755 2019-11-08: Bugfix #769 - more precise splitting necessary
			//addCode(this.getIndent() + unbrokenText.get(i+1).trim().replace(",", "|") + ")", _indent, disabled);
			StringList items = Element.splitExpressionList(unbrokenText.get(i+1).trim(), ",");
			addCode(items.concatenate("|") + ")", _indent+this.getIndent(), disabled);
			// END KGU#755 2019-11-08
			// END KGU#453 2017-11-02
			// END KGU#277 2016-10-14
			// START KGU#1207 2025-09-06: Bugfix #1222.1 Indentation was one level too deep
			//generateCode((Subqueue) _case.qs.get(i), _indent+this.getIndent()+this.getIndent()+this.getIndent());
			generateCode((Subqueue) _case.qs.get(i), _indent+this.getIndent()+this.getIndent());
			// END KGU#1207 2025-09-06
			addCode(";;", _indent+this.getIndent(), disabled);
		}
		
		// START KGU#1178 2025-07-03: Bugfix #447 This might have gone wrong with broken lines...
		//if(!_case.getText().get(_case.qs.size()).trim().equals("%"))
		if (!unbrokenText.get(_case.qs.size()).trim().equals("%"))
		// END KGU#1178 2025-07-03
		{
			addCode("", "", disabled);
			addCode("*)", _indent+this.getIndent(), disabled);
			generateCode((Subqueue) _case.qs.get(_case.qs.size()-1),_indent+this.getIndent()+this.getIndent());
			addCode(";;", _indent+this.getIndent(), disabled);
		}
		// START KGU#1206 2025-09-07: Issue #1223 Try mechanism
		//addCode("esac", _indent, disabled);
		addCode("esac" + this.getLineEnd(_case), _indent, disabled);
		// END KGU#1206 2025-09-07
		addCode("", "", disabled);
	}
	
	@Override
	protected void generateCode(For _for, String _indent) {

		// START KGU#277 2016-10-13: Enh. #270
		boolean disabled = _for.isDisabled(false); 
		// END KGU#277 2016-10-13
		if (code.count() > 0 && !code.get(code.count()-1).trim().isEmpty()) {
			addCode("", "", disabled);
		}
		// START KGU 2014-11-16
		appendComment(_for, _indent);
		// END KGU 2014-11-16
		// START KGU#30 2015-10-18: This resulted in nonsense if the algorithm was a real counting loop
		// We now use C-like syntax: for ((var = sval; var < eval; var=var+incr)) ...
		// START KGU#3 2015-11-02: And now we have a competent splitting mechanism...
		String counterStr = _for.getCounterVar();
		//START KGU#61 2016-03-21: Enh. #84/#135 - FOR-IN support
		if (_for.isForInLoop())
		{
			String valueList = _for.getValueList();
			if (!this.suppressTransformation)
			{
				StringList items = null;
				// Convert an array initializer to a space-separated sequence
				if (valueList.startsWith("{") && valueList.endsWith("}") &&
						!valueList.contains(".."))	// Preserve ranges like {3..18} or {1..200..2}
				{
					items = Element.splitExpressionList(
							valueList.substring(1, valueList.length()-1), ",");
				}
				// Convert a comma-separated list to a space-separated sequence
				else if (valueList.contains(","))
				{
					items = Element.splitExpressionList(valueList, ",");				
				}
				if (items != null)
				{
					valueList = transform(items.getLongString());
				}
				else if (varNames.contains(valueList))
				{
					// Must be an array variable
					valueList = "\"${" + valueList + "[@]}\"";
				}
				else
				{
					valueList = transform(valueList);
				}
			}
			// START KGU#277 2016-10-13: Enh. #270
			//code.add(_indent + "for " + counterStr + " in " + valueList);
			addCode("for " + counterStr + " in " + valueList, _indent, disabled);
			// END KGU#277 2016-10-13
		}
		// START KGU#1193 2025-08-28: Bugfix #1210: Too optimistic a conclusion
		//else // traditional COUNTER loop
		else if (_for.style == For.ForLoopStyle.COUNTER)
		// END KGU#1193 2025-08-28
		{
		// END KGU#61 2016-03-21
			String startValueStr = _for.getStartValue();
			String endValueStr = _for.getEndValue();
			// START KGU#129 2016-01-08: Bugfix #96: Expressions must be transformed
			// START KGU#1193 2025-08-28: Issue #1210 Respect mode suppressTransformation
			//startValueStr = transform(startValueStr);
			//endValueStr = transform(endValueStr);
			if (!suppressTransformation) {
				startValueStr = transform(startValueStr);
				endValueStr = transform(endValueStr);
			}
			// END KGU#1193 2025-08-28
			// END KGU#129 2016-01-08
			int stepValue = _for.getStepConst();
			String incrStr = counterStr + "++";
			if (stepValue == -1) {
				incrStr = counterStr + "--";
			}
			else if (stepValue != 1) {
				// START KGU#129 2016-01-08: Bugfix #96 - prefix variables
				incrStr = "(( " + counterStr + "=$" + counterStr + "+(" + stepValue + ") ))";
			}
			// END KGU#3 2015-11-02
			// START KGU#277 2016-10-13: Enh. #270
			//code.add(_indent+"for (( "+counterStr+"="+startValueStr+"; "+
			//		counterStr + ((stepValue > 0) ? "<=" : ">=") + endValueStr + "; " +
			//		incrStr + " ))");
			addCode("for (( "+counterStr+"="+startValueStr+"; "+
					counterStr + ((stepValue > 0) ? "<=" : ">=") + endValueStr + "; " +
					incrStr + " ))", _indent, disabled);
			// END KGU#277 2016-10-13
			// END KGU#30 2015-10-18
		// START KGU#61 2016-03-21: Enh. #84/#135 (continued)
		}
		// END KGU#61 2016-03-21
		// START KGU#1193 2025-08-28: Bugfix #1210: Too optimistic a conclusion
		else // unspecific FREETEXT loop
		{
			String loopText = _for.getUnbrokenText().getLongString().trim();
			if (!suppressTransformation) {
				loopText = transform(loopText);
			}
			addCode(loopText, _indent, disabled);
		}
		// END KGU#1193 2025-08-28
		// START KGU#277 2016-10-14: Enh. #270
		//code.add(_indent+"do");
		//generateCode(_for.q,_indent+this.getIndent());
		//code.add(_indent+"done");	
		//code.add("");
		addCode("do", _indent, disabled);
		generateCode(_for.getBody(),_indent+this.getIndent());
		// START KGU#1206 2025-09-07: Issue #1223 Try mechanism
		//addCode("done", _indent, disabled);
		addCode("done" + this.getLineEnd(_for), _indent, disabled);
		// END KGU#1206 2025-09-07
		addCode("", "", disabled);
		// END KGU#277 2016-10-14

	}
	
	@Override
	protected void generateCode(While _while, String _indent) {
		
		// START KGU#277 2016-10-14: Enh. #270
		boolean disabled = _while.isDisabled(false);
		// END KGU#277 2016-10-14
		if (code.count() > 0 && !code.get(code.count()-1).trim().isEmpty()) {
			addCode("", "", disabled);
		}
		// START KGU 2014-11-16
		appendComment(_while, _indent);
		// END KGU 2014-11-16
		// START KGU#132 2016-01-08: Bugfix #96 first approach with C-like syntax (( ))
		//code.add(_indent+"while " + transform(_while.getText().getLongString()));
		// START KGU#132 2016-03-24: Bugfix #96/#135 second approach with [[ ]] instead of (( ))
		//code.add(_indent+"while (( " + transform(_while.getText().getLongString()) + " ))");
		// START KGU#132/KGU#162 2016-03-31: Bugfix #96 + Enh. #144
		//code.add(_indent+"while [[ " + transform(_while.getText().getLongString()).trim() + " ]]");
		String condition = transform(_while.getUnbrokenText().getLongString()).trim();
		// START KGU#311 2017-01-05: Enh. #314: We should at least put some File API remarks
		if (this.usesFileAPI && !disabled) {
			for (int j = 0; j < Executor.fileAPI_names.length; j++) {
				if (condition.contains(Executor.fileAPI_names[j] + "(")) {
					appendComment("TODO File API: Replace the \"" + Executor.fileAPI_names[j] + "\" call by an appropriate shell construct", _indent);
					break;
				}
			}
		}
		// END KGU#311 2017-01-05
		// START KGU#277 2016-10-14: Enh. #270
		//code.add(_indent + "while " + condition);
		addCode("while " + this.finishCondition(condition), _indent, disabled);
		// END KGU#277 2016-10-14
		// END KGU#132/KGU#144 2016-03-31
		// END KGU#132 2016-03-24
		// END KGU#132 2016-01-08
		// START KGU#277 2016-10-14: Enh. #270
		//code.add(_indent+"do");
		//generateCode(_while.q,_indent+this.getIndent());
		//code.add(_indent+"done");
		//code.add("");
		addCode("do", _indent, disabled);
		generateCode(_while.getBody(),_indent+this.getIndent());
		// START KGU#1206 2025-09-07: Issue #1223 Try mechanism
		//addCode("done", _indent, disabled);
		addCode("done" + this.getLineEnd(_while), _indent, disabled);
		// END KGU#1206 2025-09-07
		addCode("", "", disabled);
		// END KGU#277 2016-10-14
		
	}
	
	@Override
	protected void generateCode(Repeat _repeat, String _indent) {
		
		// START KGU#277 2016-10-14: Enh. #270
		boolean disabled = _repeat.isDisabled(false);
		// END KGU#277 2016-10-14
		if (code.count() > 0 && !code.get(code.count()-1).trim().isEmpty()) {
			addCode("", "", disabled);
		}
		// START KGU 2014-11-16
		appendComment(_repeat, _indent);
		// END KGU 2014-11-16
		// START KGU#812 2020-02-21: Bugfix #824 - we do no longer duplicate the body
		//appendComment("NOTE: This is an automatically inserted copy of the loop body below.", _indent);
		//generateCode(_repeat.q, _indent);
		appendComment("NOTE: Represents a REPEAT UNTIL loop, see conditional break at the end.", _indent);
		// END KGU#812 2020-02-21
		// START KGU#811 2020-02-21: Bugfix #824? We had forgotten to invert the condition
		//String condition = transform(_repeat.getUnbrokenText().getLongString()).trim();
		String condition = Element.negateCondition(_repeat.getUnbrokenText().getLongString().trim());
		condition = transform(condition);
		// END KGU#811 2020-02-21
		// START KGU#811 2020-02-201: Bugfix #824 - put a conditional exit to the end instead
		//addCode("while " + this.finishCondition(condition), _indent, disabled);
		addCode("while :", _indent, disabled);
		// END KGU#811 2020-02-21
		addCode("do", _indent, disabled);
		generateCode(_repeat.getBody(), _indent + this.getIndent());
		// START KGU#311 2017-01-05: Enh. #314: We should at least put some File API remarks
		if (this.usesFileAPI && !disabled) {
			for (int j = 0; j < Executor.fileAPI_names.length; j++) {
				if (condition.contains(Executor.fileAPI_names[j] + "(")) {
					appendComment("TODO File API: Replace the \"" + Executor.fileAPI_names[j] + "\" call by an appropriate shell construct", _indent);
					break;
				}
			}
		}
		// END KGU#311 2017-01-05
		// START KGU#811 2020-02-21: Bugfix #824
		addCode(this.finishCondition(condition) + " || break", _indent + getIndent(), disabled);
		// END KGU#811 2020-02-21
		// START KGU#1206 2025-09-07: Issue #1223 Try mechanism
		//addCode("done", _indent, disabled);
		addCode("done" + this.getLineEnd(_repeat), _indent, disabled);
		// END KGU#1206 2025-09-07
		addCode("", "", disabled);
		
	}

	@Override
	protected void generateCode(Forever _forever, String _indent) {
		
		// START KGU#277 2016-10-14: Enh. #270
		//code.add("");
		boolean disabled = _forever.isDisabled(false);
		if (code.count() > 0 && !code.get(code.count()-1).trim().isEmpty()) {
			addCode("", "", disabled);
		}
		// END KGU#277 2016-10-14
		// START KGU 2014-11-16
		appendComment(_forever, _indent);
		// END KGU 2014-11-16
		// START KGU#277 2016-10-14: Enh. #270
		//code.add(_indent + "while [ 1 ]");
		//code.add(_indent + "do");
		//generateCode(_forever.q, _indent + this.getIndent());
		//code.add(_indent + "done");
		//code.add("");
		addCode("while :", _indent, disabled);
		addCode("do", _indent, disabled);
		generateCode(_forever.getBody(), _indent + this.getIndent());
		// START KGU#1206 2025-09-07: Issue #1223 Try mechanism
		//addCode("done", _indent, disabled);
		addCode("done" + this.getLineEnd(_forever), _indent, disabled);
		// END KGU#1206 2025-09-07
		addCode("", "", disabled);
		// END KGU#277 2016-10-14
		
	}
	
	@Override
	protected void generateCode(Call _call, String _indent) {
		if (!appendAsComment(_call, _indent)) {
			// START KGU 2014-11-16
			appendComment(_call, _indent);
			// END KGU 2014-11-16
			// START KGU#1206 2025-09-07: Issue #1223 Try mechanism
			String lineEnd = this.getLineEnd(_call);
			// END KGU#1206 2025-09-07
			// START KGU#277 2016-10-14: Enh. #270
			boolean disabled = _call.isDisabled(false);
			// END KGU#277 2016-10-14
			StringList callText = _call.getUnbrokenText();
			for (int i = 0; i < callText.count(); i++)
			{
				String line = callText.get(i);
				// START KGU#277 2016-10-14: Enh. #270
				//code.add(_indent+transform(_call.getText().get(i)));
				// START KGU#1206 2025-09-07: Issue #1223 Try mechanism
				//addCode(transform(line), _indent, disabled);
				addCode(transform(line) + lineEnd, _indent, disabled);
				// END KGU#1206 2025-09-07
				// END KGU#277 2016-10-14
				// START KGU#1205 2025-09-06: Bugfix #1210 Avoid all workarounds on suppressTransformation
				if (suppressTransformation) {
					// We assume the user wrote shell-compatible code, so it's all done...
					continue;
				}
				// END KGU#1205 2025-09-06
				// START KGU#803 2020-02-16: Issue #816
				Function fct = _call.getCalledRoutine();
				if (fct != null && fct.isFunction() && Instruction.isAssignment(line)) {
					if (this.routinePool != null) {
						Root routine = null;
						Vector<Root> cands = this.routinePool.findRoutinesBySignature(fct.getName(), fct.paramCount(), root, false);
						if (cands.size() >= 1) {
							routine = cands.firstElement();
							String routineId = Integer.toHexString(routine.hashCode());
							String resultName = "result" + routineId;
							String source = "${" + resultName + "}";
							StringList tokens = Element.splitLexically(line, true);
							tokens.removeAll(" ");
							Element.unifyOperators(tokens, true);
							String target = Instruction.getAssignedVarname(tokens, true);
							boolean done = false;
							if (target != null && this.varNames.contains(target)) {
								String resultType = routine.getResultType();
								TypeMapEntry resTypeEntry = this.typeMap.get(target);
								boolean isArray = (resTypeEntry != null && resTypeEntry.isArray());
								boolean isRecord = (resTypeEntry != null && resTypeEntry.isRecord());
								if (resTypeEntry == null && resultType != null && !resultType.trim().isEmpty()) {
									resTypeEntry = this.typeMap.get(":" + resultType);
									if (resTypeEntry != null && resTypeEntry.isArray()
											|| resultType.startsWith("@") || resultType.startsWith("array ")) {
										isArray = true;
									}
									if (resTypeEntry != null && resTypeEntry.isRecord()
											|| resultType.startsWith("$")) {
										isRecord = true;
									}
								}
								if (isArray) {
									// START KGU#1206 2025-09-07: Issue #1223 Try mechanism
									//addCode(makeArrayCopyAssignment(target, resultName, false, root.constants.containsKey(target), false), _indent, disabled);
									addCode(makeArrayCopyAssignment(target, resultName, false, root.constants.containsKey(target), false) + lineEnd,
											_indent, disabled);
									// END KGU#1206 2025-09-07
									wasDefHandled(root, target, true); 	// Set the handled flag
									done = true;
								}
								else if (isRecord) {
									if (!wasDefHandled(root, target, true)) {
										// We must ignore if target is a constant, otherwise we couldn't fill it
										// START KGU#1206 2025-09-07: Issue #1223 Try mechanism
										//addCode(this.getAssocDeclarator(false) + target, _indent, disabled);
										addCode(this.getAssocDeclarator(false) + target + lineEnd,
												_indent, disabled);
										// END KGU#1206 2025-09-07
									}
									addCode("for index" + routineId + " in \"${" + source + "keys[@]}\"; do", _indent, disabled);
									addCode(target + "[${" + resultName + "keys[$index" + routineId + "]}]=${" + resultName + "values[$index" + routineId + "]}", _indent+this.getIndent(), disabled);
									// START KGU#1206 2025-09-07: Issue #1223 Try mechanism
									//addCode("done", _indent, disabled);
									addCode("done" + lineEnd, _indent, disabled);
									// END KGU#1206 2025-09-07
									done = true;
								}
							}
							if (!done) {
								// START KGU#1206 2025-09-07: Issue #1223 Try mechanism
								//addCode(transform(tokens.concatenate(null, 0, tokens.indexOf("<-")+1) + source),
								//		_indent, disabled);
								addCode(transform(tokens.concatenate(null, 0, tokens.indexOf("<-")+1) + source) + lineEnd,
										_indent, disabled);
								// END KGU#1206 2025-09-07
							}
						}
					}
				}
				// END KGU#803 2020-02-16
			}
		}
	}
	
	@Override
	protected void generateCode(Jump _jump, String _indent) {
		if(!appendAsComment(_jump, _indent)) {
			// START KGU 2014-11-16
			appendComment(_jump, _indent);
			// END KGU 2014-11-16
			// START KGU#277 2016-10-14: Enh. #270
			boolean disabled = _jump.isDisabled(false);
			// END KGU#277 2016-10-14
			// START KGU#803 2020-02-16: Issue #816
			String preReturn = CodeParser.getKeywordOrDefault("preReturn","return").trim();
			// END KGU#803 2020-02-16
			StringList jumpText = _jump.getUnbrokenText();
			for (int i = 0; i < jumpText.count(); i++)
			{
				String line = jumpText.get(i);
				// FIXME (KGU 2016-03-25): Handle the kinds of exiting jumps!
				// START KGU#803 2020-02-16: Issue #816
				if (root.isSubroutine() && (line.matches("^" + Matcher.quoteReplacement(preReturn) + "(\\W.*|$)"))) {
					String expr = line.substring(preReturn.length()).trim();
					// START KGU#1205 2025-09-06: Issue #1210 no workaround with suppressTransformation!
					if (suppressTransformation) {
						addCode("return " + expr, _indent, disabled);
						// Further lines are hardly interesting (not reachable)...
						break;
					}
					// END KGU#1205 2025-09-05
					generateResultVariables(expr, _indent, disabled);
					// In case of an endstanding return we don't need a formal return command
					if (i < jumpText.count()-1 || root.children.getElement(root.children.getSize()-1) != _jump) {
						addCode("return 0", _indent, disabled);
					}
				}
				// START KGU#1102 2023-11-07: Bugfix #1109 There is such thing as try/catch/throw in bash
				else if (Jump.isThrow(line)) {
					// START KGU#1206 2025-09-08: Enh. #1223 We must either fail or return
					//appendComment(line + " (FIXME!)", _indent);
					appendComment(line, _indent);
					if (findEnclosingTry(_jump) != null) {
						// The context will be &&-ed such that failing leads up to the catch
						addCode("false", _indent, disabled);
					}
					else if (root.isSubroutine()) {
						// In this case we just return an arbitrary non-zero value
						addCode("return 42", _indent, disabled);
					}
					else {
						// The only remaining opportunity is to exit
						addCode("exit 42", _indent, disabled);						
					}
					// END KGU#1206 2025-09-08
				}
				// END KGU#1102 2023-11-07
				else
				// END KGU#803 2020-02-16
				// START KGU#277 2016-10-14: Enh. #270
				//code.add(_indent+transform(_jump.getText().get(i)));
				addCode(transform(line), _indent, disabled);
				// END KGU#277 2016-10-14
			}
		}
	}
	
	// START KGU#174 2016-04-05: Issue #153 - export had been missing
	@Override
	protected void generateCode(Parallel _para, String _indent)
	{
		// START KGU#277 2016-10-14: Enh. #270
		boolean disabled = _para.isDisabled(false);
		// END KGU#277 2016-10-14
		appendComment(_para, _indent);
		appendComment("==========================================================", _indent);
		appendComment("================= START PARALLEL SECTION =================", _indent);
		appendComment("==========================================================", _indent);
		// START KGU#1206 2025-09-07: Issue #1223  Approach to translate TRY elements
		String lineEnd = this.getLineEnd(_para);
		String indent0 = _indent;
		if (!lineEnd.isBlank()) {
			// This is a nested Try!
			addCode("{", _indent, disabled);
			indent0 += this.getIndent();
		}
		// END KGU#1206 2025-09-07
		String indent1 = indent0 + this.getIndent();
		String varName = "pids" + Integer.toHexString(_para.hashCode());
		// START KGU#277 2016-10-14: Enh. #270
		//code.add(_indent + varName + "=\"\"");
		addCode(varName + "=\"\"", indent0 , disabled);
		// END KGU#277 2016-10-14
		for (Subqueue q : _para.qs)
		{
			// START KGU#277 2016-10-14: Enh. #270
			//code.add(_indent + "(");
			//generateCode(q, indent1);
			//code.add(_indent + ") &");
			//code.add(_indent + varName + "=\"${" + varName + "} $!\"");
			addCode("(", indent0, disabled);
			generateCode(q, indent1);
			addCode(") &", indent0, disabled);
			addCode(varName + "=\"${" + varName + "} $!\"", indent0, disabled);
			// END KGU#277 2016-10-14
		}
		// START KGU#277 2016-10-14: Enh. #270
		addCode("wait ${" + varName + "}", indent0, disabled);
		// END KGU#277 2016-10-14
		// START KGU#1206 2025-09-07: Issue #1223 Approach to translate TRY elements
		if (!lineEnd.isBlank()) {
			addCode("}" + lineEnd, _indent, disabled);			
		}
		// END KGU#1206 2025-09-07
		appendComment("==========================================================", _indent);
		appendComment("================== END PARALLEL SECTION ==================", _indent);
		appendComment("==========================================================", _indent);
	}
	// END KGU#174 2016-04-05
	
	// START KGU#1206 2025-09-07: Issue #1223 Approach to translate TRY elements
	@Override
	public void generateCode(Try _try, String _indent)
	{
		// That is what we want to achieve:
		//trap finallyCode EXIT
		//{ # try
		//
		//    command_1 &&
		//    command_2 &&
		//    ...
		//    command_n &&
		//    # save your output &&
		//    true
		//} || { # catch
		//    # save log for exception
		//    command_x1
		//    ...
		//    command_xm
		//}
		//trap - EXIT
		//
		// where finallyCode is a function that contains qFinally code
		boolean disabled = _try.isDisabled(false);
		String exceptName = _try.getUnbrokenText().getLongString().trim();
		String lineEnd = this.getLineEnd(_try);
		String indent0 = _indent;
		if (!lineEnd.isBlank()) {
			// This is a nested Try!
			addCode("{", _indent, disabled);
			indent0 += this.getIndent();
		}
		String indent1 = indent0 + this.getIndent();
		String suffix = Integer.toHexString(_try.hashCode());
		String finallyName = "finally" + suffix;
		String trapVar = "trap" + suffix;
		if (!_try.qFinally.isNoOp()) {
			// Cache the former state of trap EXIT
			// trap -p EXIT either yields a string "trap -- command EXIT" or nothing
			addCode(trapVar + "=$( trap -p EXIT )", _indent, disabled);
			// Replace the empty string by "-" or extract the command
			addCode("if [ -z \"$"+trapVar+"\" ] ; then " + trapVar + "=\"-\"; else "
			+ trapVar + "=${"+trapVar+":8}; "+ trapVar + "=${" + trapVar + "% *}; fi",
			_indent, disabled);
			addCode("if [ \"${"+trapVar+":0:1}\" = \"'\" ] ; then "
			+ trapVar + "=${" + trapVar + ":1} ; "
			+ trapVar + "=${" + trapVar + "%\\'*}; fi",
			_indent, disabled);
			// Now establish the new EXIT trap for this Try
			addCode("trap \"" + finallyName + " trapped\" EXIT", _indent, disabled);
		}
		// Generate the try section
		addCode("{ " + this.commentSymbolLeft() + " try", indent0, disabled);
		generateCode(_try.qTry, indent1);
		// Connect the catch section
		addCode("} || { " + this.commentSymbolLeft() +
				" catch " + exceptName,
				indent0, disabled);
		if (!suppressTransformation) {
			// Assign the result status to the exception variable
			addCode(transform(exceptName) + "=$?", indent1, disabled);
		}
		// Generate the actual catch code
		generateCode(_try.qCatch, indent1);
		if (!_try.qFinally.isNoOp()) {
			// Append the finally precautions
			addCode("}", indent0, disabled);
			if (!lineEnd.isEmpty()) {
				// Cache the result status of try+catch
				addCode("try"+suffix + "=$?" + lineEnd, indent0, disabled);
			}
			// Restore the previous trap situation
			addCode("trap \"${" + trapVar + "}\" EXIT", indent0, disabled);
			addCode("{ " + this.commentSymbolLeft() + " finally", indent0, disabled);
			// Generate the actual finally function call
			addCode(finallyName + " okay", indent1, disabled);
		}
		addCode("}", indent0, disabled);
		if (!lineEnd.isBlank()) {
			addCode("}" + lineEnd, _indent, disabled);
		}
	}

	/**
	 * Searches for a closest {@link Try} element such that the given element
	 * {@code _ele} is statically part of the substructure of the try section
	 * of which. (In case of nested Try structures the method will return the
	 * innermost one. Will not return Try elements containing {@code _ele} in
	 * the catch or finally section.
	 * 
	 * @param _ele - an element that is assumed to be part of the substructure
	 *     of a try block.
	 * @return ether the statically enclosing Try element or {@code null}.
	 */
	protected Try findEnclosingTry(Element _ele)
	{
		Element parent = _ele.parent;
		while (parent != null && !(parent instanceof Try)) {
			_ele = parent;
			parent = _ele.parent;
		}
		if (parent instanceof Try && _ele == ((Try)parent).qTry) {
			return (Try)parent;
		}
		return null;
	}
	
	/**
	 * Finds out whether the given _element is part of a try block and if so
	 * returns an AND operator (between commands) as line end. Otherwise
	 * returns an empty string.
	 * 
	 * @param _ele - an arbitrary element that might be part of the substructure
	 *     of a try block.
	 * @return either " &&" or "".
	 */
	protected String getLineEnd(Element _ele)
	{
		String lineEnd = "";
		if (findEnclosingTry(_ele) != null) {
			lineEnd = " &&";
		}
		return lineEnd;
	}
	// END KGU#1206 2025-09-07

	// TODO: Decompose this - Result mechanism is missing!
	@Override
	public String generateCode(Root _root, String _indent, boolean _public) {
		root = _root;
		// START KGU#405 2017-05-19: Issue #237
		// START KGU#676 2019-03-30: Enh. #696 special pool in case of batch export
		//typeMap = _root.getTypeInfo();
		typeMap = _root.getTypeInfo(routinePool);
		// END KGU#676 2019-03-30
		// START KGU#803 2020-02-16: Issue #816
		// START KGU#828 2020-03-18: Bugfix #839: Some fields had been forgotten to reset
		this.returns = false;
		// END KGU#828 2020-03-18
		boolean alwaysReturns = mapJumps(_root.children);
		// START KGU#990 2021-10-03: Bugfix #990
		_root.returnsValue = this.returns;
		// END KGU#990 2021-10-03
		boolean isSubroutine = _root.isSubroutine();
		// END KGU#803 2020-02-16
		// START KGU#705 2019-09-23: Enh. #738
		int line0 = code.count();
		if (codeMap!= null) {
			// register the triple of start line no, end line no, and indentation depth
			// (tab chars count as 1 char for the text positioning!)
			codeMap.put(_root, new int[]{line0, line0, _indent.length()});
		}
		// END KGU#705 2019-09-23
		// END KGU#405 2017-05-19
		if (topLevel)
		{
			// START KGU#815 2020-03-27: Enh. #828 group export
			if (this.isLibraryModule()) {
				this.appendScissorLine(true, this.pureFilename + "." + this.getFileExtensions()[0]);
			}
			// END KGU#815 2020-03-27
			code.add("#!/bin/bash");
			// START KGU#815 2020-03-27: Enh. #828
			this.appendGeneratorIncludes("", false);
			// END KGU#815 2020-03-27
			// START KGU#351 2017-02-26: Enh. #346
			this.appendUserIncludes("");
			// END KGU#351 2017-02-26
		}
		addSepaLine();

		// START KGU 2014-11-16
		appendComment(_root, _indent);
		// END KGU 2014-11-16
		String indent = _indent;
		if (topLevel)
		{
			appendComment("(generated by Structorizer " + Element.E_VERSION + ")", indent);
			// START KGU#363 2017-05-16: Enh. #372
			appendCopyright(_root, _indent, true);
			// END KGU#363 2017-05-16

			// START KGU#311/KGU#150/KGU#241 2017-01-05/2016-09-01/2020-02-24: Issues #314, #234
			appendAuxiliaryCode(_indent);
			
			subroutineInsertionLine = code.count();
		}
		// START KGU#815 2020-03-26: Enh. #828
		else if (_public) {
			appendCopyright(_root, _indent, false);
		}
		// END KGU#815 2020-03-26
		
		if (isSubroutine) {
			// FIXME (KGU#1193) Is this structural info or should we just append the text as is?
			// START KGU#53 2015-10-18: Shell functions get their arguments via $1, $2 etc.
			//code.add(_root.getText().get(0)+" () {");
			String header = _root.getMethodName() + "()";
			// START KGU#803 2020-02-18: Issue #816 - make sure declarations make variables local
			//addCode(header + " {", "", false);
			addCode("function " + header + " {", "", false);
			// END KGU#803 2020-02-18
			indent = indent + this.getIndent();
			generateArgAssignments(_root, indent);
			// END KGU#53 2015-10-18
		} else {				
			addSepaLine();
		}
		
		addSepaLine();
		// START KGU#129 2016-01-08: Bugfix #96 - Now fetch all variable names from the entire diagram
		varNames = _root.retrieveVarNames();
		appendComment("TODO: Check and revise the syntax of all expressions!", indent);
		addSepaLine();
		// END KGU#129 2016-01-08
		
		// START KGU#1193 2025-08-28: Issue #1210 Mind suppressTransformation
		if (!suppressTransformation) {
		// END KGU#1193 2025-05-28
			// START KGU#542 2019-12-01: Enh. #739 - support for enumeration types
			for (Entry<String, TypeMapEntry> typeEntry: typeMap.entrySet()) {
				TypeMapEntry type = typeEntry.getValue();
				if (typeEntry.getKey().startsWith(":") && type != null && type.isEnum()) {
					appendEnumeratorDef(type, indent);
				}
			}
			// END KGU#542 2019-12-01
			// START KGU#389 2017-10-23: Enh. #423 declare records as associative arrays
			generateDeclarations(indent);
			// END KGU#389 2017-10-23
		// START KGU#1193 2025-08-28: Issue #1210 see above
		}
		// END KGU#1193 2025-08-28
		
		// START KGU#803 2020-02-18: Issue #816
		if (isSubroutine) {
			this.isResultSet = varNames.contains("result", false);
			this.isFunctionNameSet = varNames.contains(_root.getMethodName());
		}
		// END KGU#803 2020-02-18
		
		generateBody(_root, indent);
		
		// START KGU#803 2020-02-16: Issue #816
		generateResult(_root, indent, alwaysReturns, varNames);
		// END KGU#803 2020-02-16

		if (_root.isSubroutine()) {
			code.add("}");
		}
		
		// START KGU#815/KGU#824 2020-03-19: Enh. #828, bugfix #836
		if (topLevel) {
			libraryInsertionLine = code.count();
		}
		// END KGU#815/KGU#824 2020-03-19

		// START KGU#705 2019-09-23: Enh. #738
		if (codeMap != null) {
			// Update the end line no relative to the start line no
			codeMap.get(_root)[1] += (code.count() - line0);
		}
		// END KGU#705 2019-09-23

		return code.getText();
		
	}

	/**
	 * @param _indent
	 */
	protected void appendAuxiliaryCode(String _indent) {
		String indentPlus1 = _indent + getIndent();
		String indentPlus2 = indentPlus1 + getIndent();
		// START KGU#311 2017-01-05: Enh. #314: We should at least put some File API remarks
		if (this.usesFileAPI) {
			appendComment("TODO The exported algorithms made use of the Structorizer File API.", _indent);
			appendComment("     Unfortunately there are no comparable constructs in shell", _indent);
			appendComment("     syntax for automatic conversion.", _indent);
			appendComment("     The respective lines are marked with a TODO File API comment.", _indent);
			appendComment("     You might try something like \"echo value >> filename\" for output", _indent);
			appendComment("     or \"while ... do ... read var ... done < filename\" for input.", _indent);
		}
		// END KGU#311 2017-01-05
		// START KGU#150/KGU#241 2017-01-05: Issue #234 - Provisional support for chr and ord functions
		boolean builtInAdded = false;
		if (!this.suppressTransformation)
		{
			if (occurringFunctions.contains("chr"))
			{
				addSepaLine();
				appendComment("chr() - converts decimal value to its ASCII character representation", _indent);
				code.add(_indent + "chr() {");
				code.add(indentPlus1 + "printf \\\\$(printf '%03o' $1)");
				code.add(_indent + "}");
				builtInAdded = true;
			}
			if (occurringFunctions.contains("ord"))
			{
				addSepaLine();
				appendComment("ord() - converts ASCII character to its decimal value", _indent);
				code.add(_indent + "ord() {");
				code.add(indentPlus1 + "printf '%d' \"'$1\"");
				code.add(_indent + "}");
				builtInAdded = true;
			}
			// START KGU#803/KGU#807 2020-02-24: Issues #816, #821 - provide a routine to copy associative arrays
			for (TypeMapEntry type: typeMap.values()) {
				if (type.isRecord()) {
					addSepaLine();
					appendComment(FN_COPY_ASSOC_ARRAY + "() - copies an associative array via name references", _indent);
					addCode("auxCopyAssocArray() {", _indent, false);
					addCode(this.getNameRefDeclarator(false) + "target=$1", indentPlus1, false);
					addCode(this.getNameRefDeclarator(false) + "source=$2", indentPlus1, false);
					addCode(this.getLocalDeclarator(false, null) + "key", indentPlus1, false);
					addCode("for key in \"${!source[@]}\"; do", indentPlus1, false);
					addCode("target[$key]=\"${source[$key]}\"", indentPlus2, false);					
					addCode("done", indentPlus1, false);
					addCode("}", _indent, false);
					builtInAdded = true;
					break;
				}
			}
			// END KGU#803/KGU#807 2020-02-24
		}
		// END KGU#150/KGU#241 2017-01-05
		// START KGU#1206 2025-09-08: Enh. #1223 Support for Try/Throw
		// If a user employs finally then it is intended to be translated
		for (Try _try: this.tryElememts) {
			if (!_try.qFinally.isNoOp()) {
				addSepaLine();
				String fName = "finally" + Integer.toHexString(_try.hashCode());
				boolean isDisabled = _try.isDisabled(false);
				String indent1Plus1 = _indent+this.getIndent();
				addCode("function " + fName + "()", _indent, isDisabled);
				addCode("{", _indent, isDisabled);
				addCode("exitCode=$?", indent1Plus1, isDisabled);
				addCode("arg1=$1", indent1Plus1, isDisabled);
				generateCode(_try.qFinally, indent1Plus1);
				addCode("if [ \"$arg1\" = trapped ]", indent1Plus1, isDisabled);
				addCode("then", indent1Plus1, isDisabled);
				addCode("exit ${exitCode}", indent1Plus1+this.getIndent(), isDisabled);
				addCode("fi", indent1Plus1, isDisabled);
				addCode("}", _indent, isDisabled);
				builtInAdded = true;
			}
		}
		if (builtInAdded) addSepaLine();
		// END KGU#1206 2025-09-08
	}
	
	/**
	 * Generates declarations for the variables and constants of {@link #root} as
	 * far as needed.
	 * If {@link #root} is a subroutine then its variables wil be declared as local
	 * @param _indent - current indentation string
	 */
	public void generateDeclarations(String _indent) {
		boolean isSubroutine = root.isSubroutine();
		// FIXME: We should only do so if they won't get initialized
		for (int i = 0; i < varNames.count(); i++) {
			String varName = varNames.get(i);
			boolean isConst = root.constants.containsKey(varName);
			TypeMapEntry typeEntry = typeMap.get(varName);
			// START KGU#803 2020-02-18: Issue #816
			//if (typeEntry != null && typeEntry.isRecord()) {
			//	addCode(this.getAssocDeclarator() + varName, declIndent, false);
			//}
			if (!this.wasDefHandled(root, varName, false)) {
				if (typeEntry != null && typeEntry.isRecord()) {
					// The declare command makes the declaration local by default
					addCode(this.getAssocDeclarator(false) + varName, _indent, false);
					this.wasDefHandled(root, varName, true);
				}
				// Don't declare constants here as they must be 
				else if (isSubroutine && !isConst) {
					addCode(getLocalDeclarator(false, typeEntry) + varName, _indent, false);
					this.wasDefHandled(root, varName, true);
				}
			}
			// END KGU#803 2020-02-17
		}
	}

	// START KGU#803 2020-02-18: Issue #816 - facilitate subclassing
	/**
	 * Generates the parameter declarations and assignments
	 * @param _root - the (function) diagram being exported
	 * @param indent - the appropriate indentation level
	 */
	protected void generateArgAssignments(Root _root, String indent) {
		StringList paraNames = _root.getParameterNames();
		// START KGU#371 2019-03-08: Enh. #385 support optional arguments
		//for (int i = 0; i < paraNames.count(); i++)
		//{
		//	code.add(indent + paraNames.get(i) + "=$" + (i+1));
		//}
		int minArgs = _root.getMinParameterCount();
		StringList argDefaults = _root.getParameterDefaults();
		for (int i = 0; i < minArgs; i++)
		{
			// START KGU#803 2020-02-18: Issue #816
			//code.add(indent + paraNames.get(i) + "=$" + (i+1));
			String parName = paraNames.get(i);
			TypeMapEntry typeEntry = typeMap.get(parName);
			boolean isConst = _root.constants.containsKey(parName);
			if (typeEntry != null && (typeEntry.isArray() || typeEntry.isRecord())) {
				addCode(this.getNameRefDeclarator(isConst) + parName + "=" + argVar(i),
							indent, false);
			}
			else {
				String modifier = getLocalDeclarator(isConst, typeEntry);
				addCode(modifier + parName + "=" + argVar(i), indent, false);
			}
			this.setDefHandled(_root.getSignatureString(false, false), parName);
			// END KGU#803 2020-02-18
		}
		for (int i = minArgs; i < paraNames.count(); i++)
		{
			// START KGU#803 2020-02-18: Issue #816
			String parName = paraNames.get(i);
			TypeMapEntry typeEntry = typeMap.get(parName);
			boolean isConst = _root.constants.containsKey(parName);
			String modifier = getLocalDeclarator(isConst, typeEntry);
			// END KGU#803 2020-02-18
			addCode("if [ $# -lt " + (i+1) + " ]", indent, false);
			addCode("then", indent, false);
			// START KGU#803 2020-02-18: Issue #816
			//code.add(indent + this.getIndent() + paraNames.get(i) + "=" + transform(argDefaults.get(i)));
			addCode(modifier + parName + "=" + transform(argDefaults.get(i)), indent + this.getIndent(), false);
			// END KGU#803 2020-02-18
			addCode("else", indent, false);
			// START KGU#803 2020-02-18: Issue #816
			if (typeEntry != null && (typeEntry.isArray() || typeEntry.isRecord())) {
				modifier = this.getNameRefDeclarator(isConst);
			}
			addCode(modifier + parName + "=" + argVar(i), indent + this.getIndent(), false);
			this.setDefHandled(_root.getSignatureString(false, false), paraNames.get(i));
			// END KGU#803 2020-02-18
			addCode("fi", indent, false);
		}
		// END KGU#371 2019-03-08
	}
	
	/** @return the (index+1)th argument access notation, e.g. $2 for index 1 or ${13} for index 12 */
	private String argVar(int index)
	{
		index++;
		if (index < 10) {
			return "$" + Integer.toString(index);
		}
		return "${" + Integer.toString(index) + "}";
	}
	// END KGU#803 2020-02-18

	// START KGU#542 2019-12-01: Enh. #739 support for enumeration types
	/**
	 * Generates a shell equivalent for an enumeration type by declaring the
	 * respective set of read-only integer variables.
	 * @param _type - the {@link TpyeMapEntry} of the enumeration type
	 * @param _indent - the current indentation string
	 */
	protected void appendEnumeratorDef(TypeMapEntry _type, String _indent) {
		StringList enumItems = _type.getEnumerationInfo();
		appendComment("START enumeration type " + _type.typeName, _indent);
		// In vintage BASIC, we will just generate separate variable definitions
		int offset = 0;
		String lastVal = "";
		for (int i = 0; i < enumItems.count(); i++) {
			String[] itemSpec = enumItems.get(i).split("=", 2);
			if (itemSpec.length > 1) {
				lastVal = itemSpec[1].trim();
				offset = 0;
				try {
					int code = Integer.parseUnsignedInt(lastVal);
					lastVal = "";
					offset = code;
				}
				catch (NumberFormatException ex) {}
			}
			addCode(this.getEnumDeclarator() + itemSpec[0] + "=" + transform(lastVal) + (lastVal.isEmpty() ? "" : "+") + offset, _indent, false);
			offset++;
		}
		appendComment("END enumeration type "+ _type.typeName, _indent);
	}
	// END KGU#542 2019-12-01

	// START KGU#803 2020-02-16: Issue #816 - specific return mechanism via a global variable required
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateResult(lu.fisch.structorizer.elements.Root, java.lang.String, boolean, lu.fisch.utils.StringList)
	 */
	@Override
	protected String generateResult(Root _root, String _indent, boolean alwaysReturns, StringList varNames)
	{
		// START KGU#1193 2025-08-20: Bugfix #1210
		//if (_root.isSubroutine())
		if (_root.isSubroutine() && ! this.suppressTransformation)
		// END KGU#1193 2025-08-20
		{
			if (!alwaysReturns)
			{
				String varName = _root.getMethodName();	// for Pascal style result
				if (isResultSet && !isFunctionNameSet) {
					// retrieve the exact spelling of the used result variable
					int vx = varNames.indexOf("result", false);	// case-ignorant search
					varName = varNames.get(vx);
				}
				if (isResultSet || isFunctionNameSet) {
					//code.add(_indent + "result" + Integer.toHexString(_root.hashCode()) + "=" + varName);
					generateResultVariables(varName, _indent, false);
				}
			}
			// Otherwise the Jump generator method will have done all what's needed
		}
		return _indent;
	}
	// END KGU#803 2020-02-16

}



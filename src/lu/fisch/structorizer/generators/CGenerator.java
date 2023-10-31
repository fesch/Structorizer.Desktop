/*
    Structorizer
    A little tool which you can use to create Nassi-Schneiderman Diagrams (NSD)

    Copyright (C) 2009, 2020  Bob Fisch

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
 *      Author:         Bob Fisch
 *
 *      Description:    This class generates ANSI C code.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author                  Date            Description
 *      ------                  ----            -----------
 *      Bob Fisch               2008-11-17      First Issue
 *      Gunter Schillebeeckx    2009-08-10      Bugfixes (see comment)
 *      Bob Fisch               2009-08-17      Bugfixes (see comment)
 *      Bob Fisch               2010-08-30      Different fixes asked by Kay Gürtzig
 *                                              and Peter Ehrlich
 *      Kay Gürtzig             2010-09-10      Bugfixes and cosmetics (see comment)
 *      Bob Fisch               2011-11-07      Fixed an issue while doing replacements
 *      Kay Gürtzig             2014-11-06      Support for logical Pascal operators added
 *      Kay Gürtzig             2014-11-16      Bugfixes in operator conversion
 *      Kay Gürtzig             2015-10-18      Indentation and comment mechanisms revised, bugfix
 *      Kay Gürtzig             2015-10-21      New generator now supports multiple-case branches
 *      Kay Gürtzig             2015-11-01      Language transforming reorganised, FOR loop revision
 *      Kay Gürtzig             2015-11-10      Bugfixes KGU#71 (switch default), KGU#72 (div operators)
 *      Kay Gürtzig             2015-11-10      Code style option optionBlockBraceNextLine() added,
 *                                              bugfix/enhancement #22 (KGU#74 jump and return handling)
 *      Kay Gürtzig             2015-12-13      Bugfix #51 (=KGU#108): Cope with empty input and output
 *      Kay Gürtzig             2015-12-21      Adaptations for Bugfix #41/#68/#69 (=KGU#93)
 *      Kay Gürtzig             2016-01-15      Bugfix #64 (exit instruction was exported without ';')
 *      Kay Gürtzig             2016-01-15      Issue #61/#107: improved handling of typed variables 
 *      Kay Gürtzig             2016-03-16      Enh. #84: Minimum support for FOR-IN loops (KGU#61) 
 *      Kay Gürtzig             2016-04-01      Enh. #144: Export option to suppress content conversion 
 *      Kay Gürtzig             2016-04-03      Enh. KGU#150: ord and chr functions converted (raw approach)
 *      Kay Gürtzig             2016-07-20      Enh. #160: Option to involve subroutines implemented (=KGU#178)
 *      Kay Gürtzig             2016-08-10      Issue #227: <stdio.h> and TODOs only included if needed 
 *      Kay Gürtzig             2016-08-12      Enh. #231: Additions for Analyser checks 18 and 19 (variable name collisions)
 *      Kay Gürtzig             2016-09-25      Enh. #253: CodeParser.keywordMap refactored 
 *      Kay Gürtzig             2016-10-14      Enh. 270: Handling of disabled elements (code.add(...) --> addCode(..))
 *      Kay Gürtzig             2016-10-15      Enh. 271: Support for input instructions with prompt
 *      Kay Gürtzig             2016-10-16      Enh. #274: Colour info for Turtleizer procedures added
 *      Kay Gürtzig             2016-12-01      Bugfix #301: More sophisticated test for condition enclosing by parentheses
 *      Kay Gürtzig             2016-12-22      Enh. #314: Support for File API
 *      Kay Gürtzig             2017-01-26      Enh. #259/#335: Type retrieval and improved declaration support 
 *      Kay Gürtzig             2017-01-31      Enh. #113: Array parameter transformation
 *      Kay Gürtzig             2017-02-06      Minor corrections in generateJump(), String delimiter conversion (#343)
 *      Kay Gürtzig             2017-02-27      Enh. #346: Insertion mechanism for user-specific include directives
 *      Kay Gürtzig             2017-03-05      Bugfix #365: Fundamental revision of generateForInCode(), see comment.
 *      Kay Gürtzig             2017-03-15      Bugfix #181/#382: String delimiter transformation didn't work 
 *      Kay Gürtzig             2017-03-15      Issue #346: Insertion mechanism was misplaced (depended on others)
 *      Kay Gürtzig             2017-03-30      Issue #365: FOR-IN loop code generation revised again
 *      Kay Gürtzig             2017-04-12      Enh. #388: Handling of constants
 *      Kay Gürtzig             2017-04-13      Enh. #389: Preparation for subclass-dependent handling of import CALLs
 *      Kay Gürtzig             2017-04-14      Bugfix #394: Export of Jump elements (esp. leave) revised
 *      Kay Gürtzig             2017-05-16      Enh. #372: Export of copyright information
 *      Kay Gürtzig             2017-09-26      Enh. #389/#423: Export with includable diagrams (as global definitions)
 *      Kay Gürtzig             2017-09-30      Enh. #423: struct export fixed.
 *      Kay Gürtzig             2017-11-02      Issue #447: Line continuation in Alternative and Case elements supported
 *      Kay Gürtzig             2017-11-06      Issue #453: Modifications for string type and input and output instructions
 *      Kay Gürtzig             2018-03-13      Bugfix #520,#521: Mode suppressTransform enforced for declarations
 *      Kay Gürtzig             2018-07-21      Enh. #563, Bugfix #564: Smarter record initializers / array initializer defects
 *      Kay Gürtzig             2018-10-30      bool type no longer converted to int, instead #include <stdbool.h> generated
 *      Kay Gürtzig             2019-01-21      Bugfix #669: Export of some FOR-In loops produced structurally defective code
 *      Kay Gürtzig             2019-02-14      Enh. #680: Support for input instructions with several variables
 *      Kay Gürtzig             2019-03-07      Enh. #385: Support for optional parameters (by argument extension in the Call)
 *      Kay Gürtzig             2019-03-13      Enh. #696: All references to Arranger replaced by routinePool
 *      Kay Gürtzig             2019-03-18      Enh. #56: Export of try-catch-finally blocks
 *      Kay Gürtzig             2019-03-28      Enh. #657: Retrieval for subroutines now with group filter
 *      Kay Gürtzig             2019-03-30      Issue #696: Type retrieval had to consider an alternative pool
 *      Kay Gürtzig             2019-09-24/25   Bugfix #752: Declarations in Calls are to be handled, workaround for type defects
 *      Kay Gürtzig             2019-10-02      Enh. #721: New hooks for Jacascript in declaration handling
 *      Kay Gürtzig             2019-10-03      Bugfix #756: Transformation damage on expressions containing "<-" and brackets
 *      Kay Gürtzig             2019-11-08      Bugfix #769: Undercomplex selector list splitting in CASE generation mended
 *      Kay Gürtzig             2019-11-12      Bugfix #752: Outcommenting of incomplete declarations ended
 *      Kay Gürtzig             2019-11-17      Enh. #739: Modifications for support of enum type definitions
 *      Kay Gürtzig             2019-11-24      Bugfix #783: Defective record initializers were simply skipped without trace
 *      Kay Gürtzig             2019-11-30      Bugfix #782: Handling of global/local declarations mended
 *      Kay Gürtzig             2019-12-02      KGU#784 Type descriptor transformation improved.
 *      Kay Gürtzig             2020-02-10      Bugfix #808: For initialised declarations, operator unification was forgotten.
 *      Kay Gürtzig             2020-02-11      Bugfix #806: Mechanism to derive rudimentary format strings for printf/scanf
 *      Kay Gürtzig             2020-02-15      Issue #814: An empty parameter list should be translated into ...(void)
 *      Kay Gürtzig             2020-03-17      Enh. #828: New configuration method prepareGeneratorIncludeItem()
 *      Kay Gürtzig             2020-03-20/27   Enh. #828, bugfix #836: Group export implemented, batch export improved
 *      Kay Gürtzig             2020-03-23      Issues #828, #840: Revisions w.r.t. the File API
 *      Kay Gürtzig             2020-04-22      Bugfix #854: Deterministic topological order of type definitions ensured
 *                                              Enh. #855: New configurable default array size considered
 *      Kay Gürtzig             2020-10-16      Bugfix #873: Type definition handling was compromised by bugfix #808
 *      Kay Gürtzig             2020-10-16      Bugfix #874: Nullpointer exception on Calls with non-ASCII letters in name
 *      Kay Gürtzig             2021-02-03      Issue #920: Transformation for "Infinity" literal
 *      Kay Gürtzig             2021-10-01      Bugfix #989: No expression translation in EXIT elements to C, C++, etc.
 *      Kay Gürtzig             2021-10-03      Bugfix #990: Made-up result types on exported procedures
 *                                              Bugfix #993: Wrong handling of constant parameters
 *      Kay Gürtzig             2021-12-05      Bugfix #1024: Precautions against defective record initializers
 *      Kay Gürtzig             2022-08-23      Issue #1068: transformIndexLists() inserted into transformTokens(),
 *                                              transformOrGenerateArrayInit() mended (mutilated empty initialisers)
 *      Kay Gürtzig             2022-09-29      Bugfix #1073: Call comments had always been duplicated
 *      Kay Gürtzig             2023-09-28      Bugfix #1092: Sensible export of alias type definitions enabled
 *      Kay Gürtzig             2023-10-04      Bugfix #1093 Undue final return 0 on function diagrams
 *      Kay Gürtzig             2023-10-12      Issue #980: Cope with multi-variable declarations
 *      Kay Gürtzig             2023-10-15      Bugfix #1096 Handles complicated C-/Java-style declarations
 *      Kay Gürtzig             2023-10-17      Bugfix #1099: Constants defined by an external routine call no longer moved
 *                                              to top (to change execution order could severely compromise the algorithm!)
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *      2017-03-05 - Bugfix #365 (Kay Gürtzig)
 *      - Improved FOR-IN loop export applying the now available typeMap information.
 *      - generic names no longer with constant suffix but with loop-specific hash code, allowing global distinction
 *      - generic type definitions now global (old ANSI C didn't support local type definitions (relevant for reimport)
 *
 *      2016-04-01 - Enh. #144 (Kay Gürtzig)
 *      - A new export option suppresses conversion of text content and restricts the export
 *        more or less to the mere control structure generation.
 *        
 *      2015-12-21 - Bugfix #41/#68/#69 (Kay Gürtzig)
 *      - Operator replacement had induced unwanted padding and string literal modifications
 *      - new subclassable method transformTokens() for all token-based replacements 
 *      
 *      2015-11-29 - enhancement #23: Sensible handling of Jump elements (break / return / exit)
 *      - return instructions and assignments to variables named "result" or like the function
 *        are registered, such that return instructions may be generated on demand
 *      - "leave" jumps will generate break or goto instructions
 *      - exit instructions are produced as well.
 *      - new methods insertBlockHeading() and insertBlockTail() facilitate code style variation and
 *        subclassing w.r.t. multi-level jump instructions.
 *      
 *      2015-11-01 - Code revision / enhancements
 *      - Most of the transform stuff delegated to Element and Generator (KGU#18/KGU23)
 *      - Enhancement #10 (KGU#3): FOR loops themselves now provide more reliable loop parameters  
 *      
 *      2015-10-21 - Enhancement KGU#15: Case element with comma-separated constant list per branch
 *      
 *      2015-10-18 - Bugfixes and modificatons (Kay Gürtzig)
 *      - Bugfix: The export option "export instructions as comments" had been ignored before
 *      - An empty Jump element will now be translated into a break; instruction by default.
 *      - Comment method signature simplified
 *      - Indentation mechanism revised
 *      
 *      2014-11-16 - Bugfixes (Kay Gürtzig)
 *      - conversion of comparison and logical operators had still been flawed
 *      - comment generation unified by new inherited generic method insertComment 
 *      
 *      2014-11-06 - Enhancement (Kay Gürtzig)
 *      - logical operators "and", "or", and "not" supported 
 *      
 *      2010-09-10 - Bugfixes (Kay Gürtzig)
 *      - conditions for automatic bracket insertion for "while", "switch", "if" corrected
 *      - case keyword inserted for the branches of "switch"
 *      - function header and return statement for non-program diagram export adjusted
 *      - "cosmetic" changes to the block ends of "switch" and "do while" 
 *      
 *      2010-08-30
 *      - replaced standard I/O by the correct versions for C (not Pascal ;-P))
 *      - comments are put into code as well
 *      - code transformations (copied from Java)
 *
 *      2009-08-17 - Bugfixes
 *      - added automatic brackets for "while", "switch" & "if"
 *      - in the "repeat": "not" => "!"
 *      - pascal operator convertion
 *
 *      2009-08-10 - Bugfixes
 *      - Mistyping of the keyword "switch" in CASE statement
 *      - Mistyping of brackets in IF statement
 *      - Implementation of FOR loop
 *      - Indent replaced from 2 spaces to TAB-character (TAB configurable in IDE)
 *
 ******************************************************************************************************///

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Level;

import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.executor.Function;

public class CGenerator extends Generator {

	
	/************ Fields ***********************/
	@Override
	protected String getDialogTitle() {
		return "Export ANSI C ...";
	}

	@Override
	protected String getFileDescription() {
		return "ANSI C Source Code";
	}

	@Override
	protected String getIndent() {
		return "\t";
	}

	@Override
	protected String[] getFileExtensions() {
		String[] exts = { "c" };
		return exts;
	}

	// START KGU 2015-10-18: New pseudo field
	@Override
	protected String commentSymbolLeft() {
		// In ANSI C99, line comments are already allowed
		return "//";
	}
	// END KGU 2015-10-18
	
	// START KGU#371 2019-03-07: Enh. #385
	/**
	 * @return The level of subroutine overloading support in the target language
	 */
	@Override
	protected OverloadingLevel getOverloadingLevel() {
		return OverloadingLevel.OL_NO_OVERLOADING;
	}
	// END KGU#371 2019-03-07
	


// START KGU#16 2015-12-18: Moved to Generator.java	and made an ExportOptionDialoge option
//	// START KGU#16 2015-11-29: Code style option for opening brace placement
//	protected boolean optionBlockBraceNextLine() {
//		// (KGU 2015-11-29): Should become an ExportOptionDialoge option
//		return true;
//	}
//	// END KGU#16 2015-11-29
// END KGU#16 2015-12-18
	
	// START KGU#261/#332 2017-01-27: Enh. #259/#335
	protected HashMap<String, TypeMapEntry> typeMap;
	// END KGU#261/#332 2017-01-27
	
	// START KGU#16/KGU#74 2015-11-30: Unification of block generation (configurable)
	/**
	 * This subclassable method is used for insertBlockHeading()
	 * @return Indicates where labels for multi-level loop exit jumps are to be placed
	 * (in C, C++, C# after the loop, in Java at the beginning of the loop). 
	 */
	protected boolean isLabelAtLoopStart()
	{
		return false;
	}
	
	/**
	 * Instruction to be used to leave an outer loop (subclassable)
	 * A label string is supposed to be appended without parentheses.
	 * @return a string containing the respective reserved word
	 */
	protected String getMultiLevelLeaveInstr()
	{
		return "goto";
	}
	
	// See also appendExitInstr(int, String)
	// END KGU#16/KGU#74 2015-11-30

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

//	// START KGU 2016-08-12: Enh. #231 - information for analyser - obsolete since 3.27
//    private static final String[] reservedWords = new String[]{
//		"auto", "break", "case", "char", "const", "continue",
//		"default", "do", "double", "else", "enum", "extern",
//		"float", "for", "goto", "if", "int", "long",
//		"register", "return",
//		"short", "signed", "sizeof", "static", "struct", "switch",
//		"typedef", "union", "unsigned", "void", "volatile", "while"};
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
		return "#include %";
	}
	// END KGU#351 2017-02-26

	/************ Code Generation **************/

	// START KGU#311/KGU#828 2020-03-22: Enh. #314, #828
	protected static final String FILE_API_CLASS_NAME = "StructorizerFileAPI";
	// END KGU#311/KGU#828 2020-03-22

	// START KGU#815 2020-03-20: Enh. #828
	/** Holds the name of a required library module during group export of depending modules */
	protected String libModuleName = null;
	// END KGU#815 2020-03-20
	
	// START KGU#686 2019-03-18: Enh. #56
	/** Contains the declaration or name of the caught exception of the closest surrounding try block */
	protected String caughtException = null;
	
	/**
	 * Subclassable method to specify the degree of availability of a try-catch-finally
	 * construction in the target language.
	 * @return a {@link TryCatchSupportLevel} value
	 * @see #appendCatchHeading(Try, String)
	 */
	protected TryCatchSupportLevel getTryCatchLevel()
	{
		return TryCatchSupportLevel.TC_NO_TRY;
	}
	// END KGU#686 2019-03-18

	// START KGU#560 2018-07-22 Bugfix #564
	/** @return whether the element number is to be given in array type specifiers */
	protected boolean wantsSizeInArrayType()
	{
		return true;
	}
	// END KGU#560 2018-07-22
	
	// START KGU#784 2019-12-02
	/** @return whether the index range for array declarations is to be appended to the element type (otherwise to the variable name) */
	protected boolean arrayBracketsAtTypeName()
	{
		return false;
	}
	// END KGU#784 2019-12-02

	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getInputReplacer(boolean)
	 */
	// START KGU#281 2016-10-15: Enh. #271
	//protected String getInputReplacer() {
	//	return "scanf(\"\", &$1)";
	//}
	@Override
	protected String getInputReplacer(boolean withPrompt) {
		if (withPrompt) {
			// START KGU#794 2020-02-11: Issue #806
			//return "printf($1); scanf(\"TODO: specify format\", &$2)";
			return "printf($1); scanf($2)";
			// END KGU#794 2020-02-11
		}
		// START KGU#794 2020-02-11: Issue #806
		//return "scanf(\"TODO: specify format\", &$1)";
		return "scanf($1)";
		// END KGU#794 2020-02-11
	}
	// END KGU#281 2016-10-15

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getOutputReplacer()
	 */
	@Override
	protected String getOutputReplacer() {
		return "printf(\"TODO: specify format\\n\", $1)";
	}

	// START KGU#815/KGU#824 2020-03-19: Enh. #828, bugfix #836
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#insertPrototype(lu.fisch.structorizer.elements.Root, java.lang.String, boolean, int)
	 */
	@Override
	protected int insertPrototype(Root _root, String _indent, boolean _withComment, int _atLine)
	{
		final String CONST_PREFIX = "const ";
		int lines = 0;	// Result value = number of inserted lines
		String fnHeader = "int main(void)";
		boolean returnsArray = false;
		if (!_root.isProgram()) {
			// This is explicitly also to be done for Includables (resulting in an initialisation function)
			String fnName = _root.getMethodName();
			StringList paramNames = new StringList();
			StringList paramTypes = new StringList();
			_root.collectParameters(paramNames, paramTypes, null);
			// START KGU#990 2021-10-02: Bugfix #990 _root is not necessarily the current Root
			StringList vars = _root.getVarNames();
			// END KGU#990 2021-10-02
			fnHeader = transformTypeWithLookup(_root.getResultType(),
					// START KGU#990 2021-10-02: Bugfix #990 These values could be from a different root
					//((this.returns || this.isResultSet || this.isFunctionNameSet) ? "int" : "void"));
					((_root.returnsValue == Boolean.TRUE || vars.contains("result", false) || vars.contains(fnName)) ? "int" : "void"));
					// END KGU#990 2021-10-02
			// START KGU#140 2017-01-31: Enh. #113 - improved type recognition and transformation
			returnsArray = fnHeader.toLowerCase().contains("array") || fnHeader.contains("]");
			if (returnsArray) {
				fnHeader = transformArrayDeclaration(fnHeader, "");
			}
			// END KGU#140 2017-01-31
			fnHeader += " " + fnName + "(";
			for (int p = 0; p < paramNames.count(); p++) {
				if (p > 0) { fnHeader += ", "; }
				// START KGU#140 2017-01-31: Enh. #113: Proper conversion of array types
				//fnHeader += (transformType(_paramTypes.get(p), "/*type?*/") + " " + 
				//		_paramNames.get(p)).trim();
				// START KGU#993 2021-10-03: Bugfix #993 wrong handling of constant parameters
				//fnHeader += transformArrayDeclaration(
				//		transformTypeWithLookup(paramTypes.get(p), "???").trim(),
				//		paramNames.get(p));
				String pType = paramTypes.get(p);
				if (pType != null && pType.startsWith(CONST_PREFIX)) {
					fnHeader += CONST_PREFIX;
					pType = pType.substring(CONST_PREFIX.length());
				}
				fnHeader += transformArrayDeclaration(
						transformTypeWithLookup(pType, "???").trim(),
						paramNames.get(p));
				// END KGU#993 2021-10-03
				// END KGU#140 2017-01-31
			}
			// START KGU#800 2020-02-15: Issue #814
			if (paramNames.isEmpty()) {
				fnHeader += "void";
			}
			// END KGU#800 2020-02-15
			fnHeader += ")";
		}
		if (_withComment) {
			insertCode("", _atLine);
			lines += 1 + insertComment(_root, _indent, _atLine + 1);
			/* If we are in the library module then the comment is only placed in the header file,
			 * so it is to be a mere prototype
			 */
			if (this.isLibraryModule()) {
				if (topLevel) {
					lines += insertSepaLine("", _atLine + lines);
					lines += insertComment("Initialisation function for this library.", _indent, _atLine + lines);
				}
				fnHeader += ";";
			}
		}
		if (_root.isSubroutine()) {
			lines += insertComment("TODO: Revise the return type and declare the parameters.", _indent, _atLine + lines);
			// START KGU#140 2017-01-31: Enh. #113
			if (returnsArray) {	// Tries to return an array
				lines += insertComment("      C does not permit to return arrays - find an other way to pass the result!",
						_indent, _atLine + lines);
			}
			// END KGU#140 2017-01-31
		}
		insertCode(fnHeader, _atLine + lines);
		return lines + 1;
	}
	// END KGU #815/KGU#824 2020-03-19

	// START KGU#351 2017-02-26: Enh. #346 - include / import / uses config
	/**
	 * Method pre-processes an include file name for the #include
	 * clause. This version surrounds a string not enclosed in angular
	 * brackets by quotes.
	 * @param _includeFileName a string from the user include configuration
	 * @return the preprocessed string as to be actually inserted
	 */
	protected String prepareUserIncludeItem(String _includeFileName)
	{
		boolean isQuoted = _includeFileName.startsWith("<") && _includeFileName.endsWith(">")
				|| _includeFileName.startsWith("\"") && _includeFileName.endsWith("\"");
		if (!isQuoted) {
			_includeFileName = "\"" + _includeFileName + "\"";
		}
		return _includeFileName;
	}
	// END KGU#351 2017-02-26
	// START KGU#815/KGU#826 2020-03-17: Enh. #828, bugfix #836
	/**
	 * Method converts some generic module name into a generator-specific include file name or
	 * module name for the import / use clause.<br/>
	 * To be used before adding a generic name to {@link #generatorIncludes}.
	 * This version adds a ".h" suffix if there was neither {@code <...>} nor {@code "..."} around
	 * it and it it didn't end with ".h". Will then call {@link #prepareUserIncludeItem(String)}. 
	 * @see #getIncludePattern()
	 * @see #appendGeneratorIncludes(String)
	 * @see #prepareUserIncludeItem(String)
	 * @param _includeName a generic (language-independent) string for the generator include configuration
	 * @return the converted string as to be actually added to {@link #generatorIncludes}
	 */
	protected String prepareGeneratorIncludeItem(String _includeName)
	{
		_includeName = _includeName.trim();
		boolean isQuoted = _includeName.startsWith("<") && _includeName.endsWith(">")
				|| _includeName.startsWith("\"") && _includeName.endsWith("\"");
		if (!isQuoted && !_includeName.endsWith(".h")) {
			_includeName = this.prepareUserIncludeItem(_includeName + ".h");
		}
		return _includeName;
	}
	// END KGU#815/KGU#826 2020-03-17

	// START KGU#16/#47 2015-11-30
	/**
	 * Instruction to create a language-specific exit instruction (subclassable)
	 * The exit code will be passed to the generated code.
	 */
	protected void appendExitInstr(String _exitCode, String _indent, boolean isDisabled)
	{
		// START KGU 2016-01-15: Bugfix #64 (reformulated) semicolon was missing
		//code.add(_indent + "exit(" + _exitCode + ")");
		addCode("exit(" + _exitCode + ");", _indent, isDisabled);
		// END KGU 2016-01-15
	}
	// END KGU#16/#47 2015-11-30

	// START KGU#93 2015-12-21: Bugfix #41/#68/#69
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformTokens(lu.fisch.utils.StringList)
	 */
	@Override
	protected String transformTokens(StringList tokens)
	{
		// START KGU#1061 2022-08-23: Issue #1068
		transformIndexLists(tokens);
		// END KGU#1061 2022-08-23
		// START KGU#920 2021-02-03: Issue #920 Handle Infinity literal
		tokens.replaceAll("Infinity", "INFINITY");
		// END KGU#920 2021-02-03
		tokens.replaceAll("div", "/");
		tokens.replaceAll("<-", "=");
		// START KGU#150 2016-04-03: Handle Pascal ord and chr function
		int pos = - 1;
		while ((pos = tokens.indexOf("ord", pos+1)) >= 0 && pos+1 < tokens.count() && tokens.get(pos+1).equals("("))
		{
			tokens.set(pos, "(int)");
		}
		pos = -1;
		while ((pos = tokens.indexOf("chr", pos+1)) >= 0 && pos+1 < tokens.count() && tokens.get(pos+1).equals("("))
		{
			tokens.set(pos, "(char)");
		}
		// END KGU#150 2016-04-03
		// START KGU#311 2016-12-22: Enh. #314 - Structorizer file API support
		// KGU#832 2020-03-23: Bugfix #840 Even in case of disabled File API elements the code should be transformed
		//if (this.usesFileAPI) {
		transformFileAPITokens(tokens);
		//}
		// END KGU#311 2016-12-22
		// START KGU#342 2017-02-07: Bugfix #343
		for (int i = 0; i < tokens.count(); i++) {
			String token = tokens.get(i);
			int tokenLen = token.length();
			// START KGU#190 2017-03-15: Bugfix #181/#382 - String delimiter conversion had failed
			//if (tokenLen >= 2 && (token.startsWith("'") && token.endsWith("\"") || token.startsWith("\"") && token.endsWith("'"))) {
			if (tokenLen >= 2 && (token.startsWith("'") && token.endsWith("'") || token.startsWith("\"") && token.endsWith("\""))) {
			// END KGU#190 2017-03-15
				char delim = token.charAt(0);
				String internal = token.substring(1, tokenLen-1);
				// Escape all unescaped double quotes
				pos = -1;
				while ((pos = internal.indexOf("\"", pos+1)) >= 0) {
					if (pos == 0 || internal.charAt(pos-1) != '\\') {
						internal = internal.substring(0, pos) + "\\\"" + internal.substring(pos+1);
						pos++;
					}
				}
				if (!(tokenLen == 3 || tokenLen == 4 && token.charAt(1) == '\\')) {
					delim = '\"';
				}
				tokens.set(i, delim + internal + delim);
			}
		}
		// END KGU#342 2017-02-07
		return tokens.concatenate().trim();
	}
	// END KGU#93 2015-12-21

	// END KGU#18/KGU#23 2015-11-01

	// START KGU#311 2016-12-22: Enh. #314 - Structorizer file API support
	/**
	 * Subclassable submethod of transformTokens(), designed to do specific replacements or manipulations
	 * with the subroutine names of the File API. It is called after all other token transformations are done
	 * (immediately before re-concatenation).
	 * This does some C-specific stuff here prefixing fileRead with pointer operators and a dummy type casting,
	 * so subclasses should better overwrite it.
	 * @param tokens
	 */
	protected void transformFileAPITokens(StringList tokens)
	{
		int pos = -1;
		while ((pos = tokens.indexOf("fileRead", pos+1)) >= 0 && pos+1 < tokens.count() && tokens.get(pos+1).equals("("))
		{
			tokens.set(pos, "*(/*type?*/*)fileRead");
		}
	}
	// END KGU#311 2016-12-22
	
// START KGU#18/KGU#23 2015-11-01: Obsolete    
//    public static String transform(String _input)
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transform(java.lang.String, boolean)
	 */
	@Override
	protected String transform(String _input, boolean _doInputOutput)
	{
		// START KGU#162 2016-04-01: Enh. #144
		if (!this.suppressTransformation)
		{
		// END KGU#162 2016-04-01
			// START KGU#109/KGU#141 2016-01-16: Bugfix #61,#107,#112
			_input = Element.unifyOperators(_input);
			int asgnPos = _input.indexOf("<-");
			if (asgnPos > 0)
			{
				// START KGU#739 2019-10-03: Bugfix #756 we must avoid false positives...
				//String lval = _input.substring(0, asgnPos).trim();
				//String expr = _input.substring(asgnPos + "<-".length()).trim();
				StringList tokens = Element.splitLexically(_input, true);
				if ((asgnPos = tokens.indexOf("<-")) > 0) {
					String lval = tokens.concatenate("", 0, asgnPos);
					String expr = tokens.concatenate("", asgnPos+1).trim();
				// END KGU#739 2019-10-03
					String[] typeNameIndex = this.lValueToTypeNameIndexComp(lval);
					String index = typeNameIndex[2];
					_input = (typeNameIndex[0] + " " + typeNameIndex[1] + 
							(index.isEmpty() ? "" : "["+index+"]") + 
							// START KGU#388 2017-09-27: Enh. #423
							typeNameIndex[3] +
							// END KGU#388 2017-09-27: Enh. #423
							" <- " + expr).trim();
				// START KGU#739 2019-10-03: Bugfix #756 part 2
				}
				// END KGU#739 2019-10-03
			}
			// END KGU#109/KGU#141 2016-01-16
		// START KGU#162 2016-04-01: Enh. #144
		}
		// END KGU#162 2016-04-01
		
		_input = super.transform(_input, _doInputOutput);

		// START KGU#108 2015-12-13: Bugfix #51: Cope with empty input and output
		if (_doInputOutput) {
			// START KGU#794 2020-02-11: Issue #806 - more intelligent format string handling
			//_input = _input.replace("scanf(\"TODO: specify format\", &)", "getchar()");
			//_input = _input.replace("printf(\"TODO: specify format\", ); ", "");
			_input = _input.replace("scanf()", "getchar()").replace("§$§$§", "\"");
			//_input = _input.replace("printf(\"TODO: specify format\\n\", )", "printf(\"\\n\")");
			if (_input.startsWith("printf(\"TODO: specify format\\n\",")) {
				// Decompose the output items and try to derive sensible format specifiers
				StringList exprs = Element.splitExpressionList(_input.substring("printf(\"TODO: specify format\\n\",".length()), ",", true);
				if (exprs.isEmpty()) {
					_input = "printf(\"\\n\")";
				}
				else {
					String tail = exprs.get(exprs.count() - 1);
					exprs.remove(exprs.count()-1);
					exprs.removeAll("");
					_input = "printf(\"";
					for (int i = 0; i < exprs.count(); i++) {
						String expr = exprs.get(i);
						StringList tokens = Element.splitLexically(expr, true);
						tokens.removeAll(" ");
						String fSpec = "?";
						if (tokens.count() == 1) {
							String token = tokens.get(0);
							if (token.startsWith("\"") && token.endsWith("\"")) {
								fSpec = "s";
							}
							else if (token.startsWith("'") && token.endsWith("'")) {
								int len = token.length();
								if (len == 3 || len == 4 && token.charAt(1) == '\\') {
									fSpec = "c";
								}
								else {
									fSpec = "s";
								}
							}
							else if (token.equals("false") || token.equals("true")) {
								fSpec = "d";
							}
							else if (typeMap.containsKey(token)) {
								TypeMapEntry type = typeMap.get(token);
								String typeName = transformTypeFromEntry(type, null, true);
								if (type.isEnum()) {
									fSpec = "d";
								}
								else if (typeName != null) {
									if (typeName.endsWith("int") || typeName.endsWith("short") || typeName.endsWith("long")) {
										fSpec = "d";
									}
									else if (typeName.equals("double") || typeName.equals("float")) {
										fSpec = "g";
									}
									else if (typeName.equals("char*")) {
										fSpec = "s";
									}
									else if (typeName.equals("char")) {
										fSpec = "c";
									}
								}
							}
							else {
								try {
									Double.parseDouble(token);
									fSpec = "g";
									Integer.parseInt(token);
									fSpec = "d";
								}
								catch (NumberFormatException ex) {}
							}
						}
						_input += "%" + fSpec;
					}
					_input +="\\n\"" + (exprs.isEmpty() ? "" : ", ")
							+ exprs.concatenate(", ") + tail;
				}
			}
			// END KGU#794 2020-02-11
		}
		// END KGU#108 2015-12-13

		return _input.trim();
	}

	// START KGU#16 2015-11-29
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformTypeString(java.lang.String, java.lang.String)
	 * see also: transformType(java.lang.String, java.lang.String)
	 */
	@Override
	protected String transformType(String _type, String _default) {
		if (_type == null) {
			_type = _default;
		}
		// START KGU 2017-04-12: We must not generally flatten the case (consider user types!)
		//_type = _type.toLowerCase();
		//_type = _type.replace("integer", "int");
		//_type = _type.replace("real", "double");
		//_type = _type.replace("boolean", "int");
		//_type = _type.replace("boole", "int");
		//_type = _type.replace("character", "char");
		_type = _type.replaceAll("(^|.*\\W)(I" + BString.breakup("nt", true) + ")($|\\W.*)", "$1int$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("integer", true) + ")($|\\W.*)", "$1int$3");
		_type = _type.replaceAll("(^|.*\\W)(L" + BString.breakup("ong", true) + ")($|\\W.*)", "$1long$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("longint", true) + ")($|\\W.*)", "$1long$3");
		_type = _type.replaceAll("(^|.*\\W)(D" + BString.breakup("ouble", true) + ")($|\\W.*)", "$1double$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("real", true) + ")($|\\W.*)", "$1double$3");
		_type = _type.replaceAll("(^|.*\\W)(F" + BString.breakup("loat", true) + ")($|\\W.*)", "$1float$3");
		// START KGU#607 2018-10-30: We may insert "#include <stdbool.h>", so bool can be used
		//_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("boolean") + ")($|\\W.*)", "$1int$3");
		//_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("boole") + ")($|\\W.*)", "$1int$3");
		//_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("bool") + ")($|\\W.*)", "$1int$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("boolean", true) + ")($|\\W.*)", "$1bool$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("boole", true) + ")($|\\W.*)", "$1bool$3");
		// END KGU#607 2018-10-30
		_type = _type.replaceAll("(^|.*\\W)(C" + BString.breakup("har", true) + ")($|\\W.*)", "$1char$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("character", true) + ")($|\\W.*)", "$1char$3");
		// END KGU 2017-04-12
		// START KGU#332 2017-01-30: Enh. #335 - more sophisticated type info
		if (this.getClass().getSimpleName().equals("CGenerator")) {
			// START KGU#854 2020-04-22: Enh. #855 default string length
			//_type = _type.replace("string", "char*");
			//_type = _type.replace("String", "char*");
			/*
			 * It wouldn't make much sense to define the string type as char[<defaultStringLength>] as in most
			 * cases we are better off with the pointer type, e.g. fo arguments, assignments and even return
			 * values, so we don't make use of the default value introduced with version 3.30.08 here.
			 */
			_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("String", true) + ")($|\\W.*)", "$1char*$3");
			// END KGU#854 2020-04-22
		}
		// END KGU#332 2017-01-30
		return _type;
	}
	// END KGU#16 2015-11-29
	
	// START KGU#388 2017-09-29: Enh. #423
	protected String transformTypeWithLookup(String _type, String _default) {
		TypeMapEntry typeInfo = this.typeMap.get(":" + _type);
		// The typeInfo might be an alias, in this case no specific measures are necessary
		// START KGU#542 2019-11-17: Enh. #739
		//if (typeInfo != null && typeInfo.isRecord() && _type.equals(typeInfo.typeName)) {
		//	_type = this.transformRecordTypeRef(typeInfo.typeName, false);
		//}
		if (typeInfo != null && (typeInfo.isRecord() || typeInfo.isEnum()) && _type.equals(typeInfo.typeName)) {
			if (typeInfo.isRecord()) {
				_type = this.transformRecordTypeRef(typeInfo.typeName, false);
			}
			else {
				_type = this.transformEnumTypeRef(typeInfo.typeName);
			}
		}
		// END KGU#542 2019-11-17
		else {
			_type = transformType(_type, _default);
		}
		return _type;
	}
	// END KGU#388 2017-09-29



	// START KGU#140 2017-01-31: Enh. #113: Advanced array transformation
	protected String transformArrayDeclaration(String _typeDescr, String _varName)
	{
		String decl = "";
		if (_typeDescr.toLowerCase().startsWith("array") || _typeDescr.endsWith("]")) {
			// TypeMapEntries are really good at analysing array definitions
			TypeMapEntry typeInfo = new TypeMapEntry(_typeDescr, null, null, null, 0, false, true);
			String canonType = typeInfo.getTypes().get(0);
			decl = this.makeArrayDeclaration(canonType, _varName, typeInfo).trim();
		}
		else {
			// START KGU#711 2019-09-30: Enh. #721 - for Javascript we should allow to substitute the type
			//decl = (_typeDescr + " " + _varName).trim();
			decl = this.composeTypeAndNameForDecl(_typeDescr, _varName).trim();
			// END KGU#711 2019-09-30
		}
		return decl;
	}
	// END KGU#140 2017-01-31
	
	// START KGU#388 2017-09-26: Enh. #423 struct type support
	/**
	 * Returns a target-language expression replacing the Structorizer record
	 * initializer - as far as it can be handled within one line
	 * @param constValue - the Structorizer record initializer
	 * @param typeInfo - the TypeMapEntry describing the record type
	 * @return the equivalent target code as expression string
	 */
	protected String transformRecordInit(String constValue, TypeMapEntry typeInfo) {
		// START KGU#559 2018-07-20: Enh. #563 - smarter initializer evaluation
		//HashMap<String, String> comps = Instruction.splitRecordInitializer(constValue);
		HashMap<String, String> comps = Instruction.splitRecordInitializer(constValue, typeInfo, false);
		// END KGU#559 2018-07-20
		LinkedHashMap<String, TypeMapEntry> compInfo = typeInfo.getComponentInfo(true);
		StringBuilder recordInit = new StringBuilder("{");
		boolean isFirst = true;
		for (Entry<String, TypeMapEntry> compEntry: compInfo.entrySet()) {
			String compName = compEntry.getKey();
			// START KGU#1021 2021-12-05: Bugfix #1024 Instruction might be defective
			//String compVal = comps.get(compName);
			String compVal = null;
			if (comps != null) {
				compVal = comps.get(compName);
			}
			// END KGU#1021 2021-12-05
			if (isFirst) {
				isFirst = false;
			}
			else {
				recordInit.append(", ");
			}
			if (!compName.startsWith("§")) {
				if (compVal == null) {
					recordInit.append("0 /*undef.*/");
				}
				else if (compEntry.getValue().isRecord()) {
					recordInit.append(transformRecordInit(compVal, compEntry.getValue()));
				}
				else {
					recordInit.append(transform(compVal));
				}
			}
		}
		recordInit.append("}");
		return recordInit.toString();
	}
	// END KGU#388 2017-09-26

	protected void appendBlockHeading(Element elem, String _headingText, String _indent)
	{
		boolean isDisabled = elem.isDisabled(false);
		if (elem instanceof ILoop && this.jumpTable.containsKey(elem) && this.isLabelAtLoopStart())  
		{
				_headingText = this.labelBaseName + this.jumpTable.get(elem) + ": " + _headingText;
		}
		if (!this.optionBlockBraceNextLine())
		{
			addCode(_headingText + " {", _indent, isDisabled);
		}
		else
		{
			addCode(_headingText, _indent, isDisabled);
			addCode("{", _indent, isDisabled);
		}
	}

	protected void appendBlockTail(Element elem, String _tailText, String _indent)
	{
		boolean isDisabled = elem.isDisabled(false);
		if (_tailText == null) {
			addCode("}", _indent, isDisabled);
		}
		else {
			addCode("} " + _tailText + ";", _indent, isDisabled);
		}
		
		if (elem instanceof ILoop && this.jumpTable.containsKey(elem) && !this.isLabelAtLoopStart()) {
			addCode(this.labelBaseName + this.jumpTable.get(elem) + ": ;", _indent, isDisabled);
		}
	}
	// END KGU#74 2015-11-30
	
	// START KGU#332 2017-01-27: Enh. #335
	/**
	 * States whether constant definitions or variable declarations may occur anywhere in
	 * the code or only at block beginning.<b/>
	 * Note that this option only applies to explicit declarations in e.g. Instruction or
	 * Call elements; in case of a mere first assignment somewhere we could not be sure that
	 * a declaration in this place would cover all occurrences (e.g., if introduced in
	 * several branches of an Alternative or Case element).
	 * @return true if declarations may be mixed among instructions
	 */
	protected boolean isInternalDeclarationAllowed()
	{
		return false;
	}
	// END KGU#332 2017-01-27
	
	// START KGU#388 2017-09-26: Enh. #423
	/**
	 * Creates a type description suited for C code from the given TypeMapEntry {@code typeInfo}
	 * The returned type description will have to be split before the first
	 * occurring opening bracket in order to place the variable or type name there.
	 * 
	 * @param typeInfo - the defining or derived TypeMapInfo of the type 
	 * @param definingWithin - a possible outer type context
	 * @param preferName - whether the type name is to be preferred over the structure
	 * @return a String suited as C type description in declarations etc. 
	 */
	@Override
	// START KGU#1082 2023-09-28: Bugfix #1092 Sensible handling of alias types
	//protected String transformTypeFromEntry(TypeMapEntry typeInfo, TypeMapEntry definingWithin) {
	protected String transformTypeFromEntry(TypeMapEntry typeInfo, TypeMapEntry definingWithin, boolean preferName) {
	// END KGU#1082 2023-09-28
		// Record type description won't usually occur (rather names)
		String _typeDescr;
//		String canonType = typeInfo.getTypes().get(0);
		// START KGU#1082 2023-09-28: Bugfix #1092 Sensible handling of alias types
		//String canonType = typeInfo.getCanonicalType(true, true);
		String canonType = typeInfo.getCanonicalType(true, preferName);
		// END KGU#1082 2023-09-28
		int nLevels = canonType.lastIndexOf('@')+1;
		String elType = (canonType.substring(nLevels)).trim();
		TypeMapEntry elTypeInfo = typeInfo;
		if (nLevels > 0 && typeInfo.getTypeMap() != null && typeInfo.getTypeMap().containsKey(":"+elType)) {
			elTypeInfo = typeInfo.getTypeMap().get(":" + elType);
		}
		if (elTypeInfo.isRecord() && elType.equals(elTypeInfo.typeName)) {
			elType = transformRecordTypeRef(elType, elTypeInfo == definingWithin);
		}
		// START KGU#542 2019-11-17: Enh. #739 - support for enum types
		else if (elTypeInfo.isEnum() && elType.equals(elTypeInfo.typeName)) {
			elType = transformEnumTypeRef(elType);
		}
		// END KGU#542 2019-11-17
		else {
			elType = transformType(elType, "???");
		}
		_typeDescr = elType;
		for (int i = 0; i < nLevels; i++) {
			_typeDescr += "[";
			if (this.wantsSizeInArrayType()) {
				int minIndex = typeInfo.getMinIndex(i);
				int maxIndex = typeInfo.getMaxIndex(i);
				// START KGU#854 2020-04-22: Enh. #855
				if (maxIndex < 0) {
					maxIndex = this.optionDefaultArraySize() - 1;
				}
				// END KGU#854 2020-04-22
				int indexRange = maxIndex+1 - minIndex;
				// We try a workaround for negative minIndex...
				if (indexRange > maxIndex + 1) {
					maxIndex = indexRange - 1;
				}
				if (maxIndex >= 0) {
					_typeDescr += Integer.toString(maxIndex+1);
				}
			}
			_typeDescr += "]";
		}
		return _typeDescr;
	}

	/**
	 * Special adaptation of record type name references in C-like languages, e.g. C
	 * adds a prefix "struct" wherever it is used. C++ doesn't need to, Java and C#
	 * don't, so the inheriting classes must override this.
	 * @param structName - name of the structured type
	 * @param isRecursive - if used defining this very type
	 * @return the prepared reference string
	 */
	protected String transformRecordTypeRef(String structName, boolean isRecursive) {
		return "struct " + structName + (isRecursive ? " * " : "");
	}
	
	// START KGU#542 2019-11-17: Enh. #739
	/**
	 * Special adaptation of enum type name references in C-like languages, e.g. C
	 * adds a prefix "enum" wherever it is used. C++ doesn't need to, Java and C#
	 * don't, so the inheriting classes must override this.
	 * @param enumName - name of the structured type
	 * @return the prepared reference string
	 */
	protected String transformEnumTypeRef(String enumName) {
		return "enum " + enumName;
	}
	// END KGU#542 2019-11-17

	/**
	 * Adds the type definitions for all types in {@code _root.getTypeInfo()}.
	 * @param _root - originating Root
	 * @param _indent - current indentation level (as String)
	 */
	protected void generateTypeDefs(Root _root, String _indent) {
		// START KGU#676 2019-03-30: Enh. #696 special pool in case of batch export
		//for (Entry<String, TypeMapEntry> typeEntry: _root.getTypeInfo().entrySet()) {
		for (Entry<String, TypeMapEntry> typeEntry: _root.getTypeInfo(routinePool).entrySet()) {
		// END KGU#676 2019-03-30
			String typeKey = typeEntry.getKey();
			if (typeKey.startsWith(":")) {
				generateTypeDef(_root, typeKey.substring(1), typeEntry.getValue(), _indent, false);
			}
		}
	}

	/**
	 * Appends a typedef or struct definition for the type passed in by {@code _typeEnry}
	 * if it hadn't been defined globally or in the preamble before.
	 * @param _root - the originating Root
	 * @param _typeName - the designated name for the type to be defined
	 * @param _type - the type map entry the definition for which is requested here
	 * @param _indent - the current indentation
	 * @param _asComment - if the type definition is only to be added as comment (disabled)
	 */
	protected void generateTypeDef(Root _root, String _typeName, TypeMapEntry _type, String _indent, boolean _asComment) {
		String typeKey = ":" + _typeName;
		if (this.wasDefHandled(_root, typeKey, true)) {
			return;
		}
		// START KGU#1082 2023-09-28: Bugfix #1092 sensible support for alias types
		if (_type.typeName != null && !_typeName.equals(_type.typeName)) {
			// Seems to be an alias - so make sure the referred type is defined
			generateTypeDef(_root, _type.typeName, _type, _indent, _asComment);
			String prefix = "";
			if ("CGenerator".equals(this.getClass().getSimpleName())) {
				if (_type.isRecord()) {
					prefix = "struct ";
				}
				else if (_type.isEnum()) {
					prefix = "enum ";
				}
			}
			addCode("typedef " + prefix + _type.typeName + " " + _typeName + ";",
					_indent, _asComment);
			return;
		}
		// END KGU#1082 2023-09-28
		String indentPlus1 = _indent + this.getIndent();
		appendDeclComment(_root, _indent, typeKey);
		if (_type.isRecord()) {
			addCode("struct " + _type.typeName + " {", _indent, _asComment);
			for (Entry<String, TypeMapEntry> compEntry: _type.getComponentInfo(false).entrySet()) {
				// START KGU#837 2020-03-30: Inconsistent (not recursive) approach to place an array length correctly
				//addCode(transformTypeFromEntry(compEntry.getValue(), _type) + "\t" + compEntry.getKey() + ";",
				//		indentPlus1, _asComment);
				TypeMapEntry compType = compEntry.getValue();
				String transType = transformTypeFromEntry(compType, _type, true).trim();
				int posBrack0 = -1, posBrack1 = -1;
				String bracks = "";
				if (compType.isArray() && transType != null
						&& !this.arrayBracketsAtTypeName()
						&& (posBrack0 = transType.indexOf("[")) > 0
						&& (posBrack1 = transType.lastIndexOf("]")) > posBrack0
						&& posBrack1 + 1 == transType.length()) {
					bracks = transType.substring(posBrack0, posBrack1+1);
					transType = transType.substring(0, posBrack0);
				}
				addCode(transType + "\t" + compEntry.getKey() + bracks + ";", indentPlus1, _asComment);
				// END KGU#837 2020-03-30
			}
			addCode("};", _indent, _asComment);
		}
		// START KGU#542 2019-11-17: Enh. #739
		else if (_type.isEnum()) {
			StringList items = _type.getEnumerationInfo();
			String itemList = items.concatenate(", ");
			if (itemList.length() > 70) {
				addCode("enum " + _type.typeName + "{", _indent, _asComment);
				for (int i = 0; i < items.count(); i++) {
					// FIXME: We might have to transform the value...
					addCode(items.get(i) + (i < items.count() -1 ? "," : ""), indentPlus1, _asComment);
				}
				addCode("};", _indent, _asComment);
			}
			else {
				addCode("enum " + _type.typeName + "{" + itemList + "};", _indent, _asComment);
			}
		}
		// END KGU#542 2019-11-17
		else {
			// START KGU#1082 2023-09-28: Bugfix #1092 Sensible handling of alias types
			//addCode("typedef " + this.transformTypeFromEntry(_type, null, true) + " " + _typeName + ";",
			addCode("typedef " + this.transformTypeFromEntry(_type, null, !_typeName.equals(_type.typeName)) + " " + _typeName + ";",
			// END KGU#1082 2023-09-28
					_indent, _asComment);
		}
	}
	// END KGU#388 2017-09-26

	@Override
	protected void generateCode(Instruction _inst, String _indent) {

		if (!appendAsComment(_inst, _indent)) {

			// START KGU#424 2017-09-26: Avoid the comment here if the element contains mere declarations
			//insertComment(_inst, _indent);
			boolean commentInserted = false;
			// END KGU#424 2017-09-26

			StringList lines = _inst.getUnbrokenText();
			for (int i = 0; i < lines.count(); i++) {
				// FIXME: We must distinguish for every line:
				// 1. assignment
				// 1.1 with declaration (mind record initializer!)
				// 1.1.1 as constant
				// 1.1.2 as variable
				// 1.2 without declaration 
				// 1.2.1 with record or array initializer
				// 1.2.2 without record initializer
				// 2. mere declaration
				// 2.1 as constant
				// 2.2 as variable
				// 3. type definition
				// 4. Input / output
				// START KGU#277/KGU#284 2016-10-13/16: Enh. #270 + Enh. #274
				//code.add(_indent + transform(lines.get(i)) + ";");
				// START KGU#504 2018-03-13: Bugfix #520/#521
				//String line = _inst.getText().get(i);
				String line = lines.get(i);
				// END KGU#504 2018-03-13
				// START KGU#261/KGU#332 2017-01-26: Enh. #259/#335
				//String codeLine = transform(line) + ";";
				//addCode(codeLine, _indent, isDisabled);
				// Things will get easier and more precise with tokenization
				// (which must be done based on the original line)
				commentInserted = generateInstructionLine(_inst, _indent, commentInserted, line);
				// END KGU#277/KGU#284 2016-10-13
			}

		}
		
	}

	// START KGU#730 2019-09-24: Bugfix #752 Outsourced from generateCode(Instruction, String) because also needed for Calls
	/**
	 * Generates the code for the given {@code Instruction} line {@code _line}.
	 * 
	 * @param _inst - the Instruction (or Call) element
	 * @param _indent - current indentation
	 * @param _commentInserted - whether the element comment had already been inserted
	 * @param _line - the current instruction line
	 * @return {@code true} if the element comment will have been inserted when this
	 *     routine is being left
	 */
	protected boolean generateInstructionLine(Instruction _inst, String _indent, boolean _commentInserted, String _line) {
		// Cases to be distinguished and handled:
		// 1. assignment
		// 1.1 with declaration (mind record initializer!)
		// 1.1.1 as constant
		// 1.1.2 as variable
		// 1.2 without declaration 
		// 1.2.1 with record or array initializer
		// 1.2.2 without record initializer
		// 2. mere declaration
		// 2.1 as constant
		// 2.2 as variable
		// 3. type definition
		// 4. Input / output
		boolean isDisabled = _inst.isDisabled(false); 
		StringList tokens = Element.splitLexically(_line.trim(), true);
		// START KGU#796 2020-02-10: Bugfix #808
		Element.unifyOperators(tokens, false);
		// END KGU#796 2020-02-10
		StringList pureTokens = tokens.copy();	// will not contain separating space
		StringList exprTokens = null;	// Tokens of the expression in case of an assignment
		StringList pureExprTokens = null;	// as before, will not contain separating space
		pureTokens.removeAll(" ");
		String expr = null;	// Original expression
		int posAsgn = tokens.indexOf("<-");
		if (posAsgn < 0) {
			posAsgn = tokens.count();
		}
		else {
			exprTokens = tokens.subSequence(posAsgn + 1, tokens.count());
			pureExprTokens = pureTokens.subSequence(pureTokens.indexOf("<-")+1, pureTokens.count());
		}
		String codeLine = null;
		String varName = Instruction.getAssignedVarname(pureTokens, false);
		boolean isDecl = Instruction.isDeclaration(_line);
		//exprTokens.removeAll(" ");
		if (!this.suppressTransformation && (isDecl || exprTokens != null)) {
			// Cases 1 or 2
			// If there is an initialization then it must at least be generated
			// as assignment.
			// With declaration styles other than than C-like, this requires
			// cutting out the type specification together with the
			// specific keywords and separators ("var"+":" / "dim"+"as").
			// With C-style initializations, however, it depends on whether
			// code-internal declarations are allowed (C++, C#, Java) or not
			// (pure C): If allowed then we may just convert it as is, otherwise
			// we must cut off the type specification (i.e. all text preceding the
			// variable name).
			Root root = Element.getRoot(_inst);
			StringList paramNames = root.getParameterNames();
			// START KGU#375 2017-04-12: Enh. #388 special treatment of constants
			if (pureTokens.get(0).equals("const")) {
				// Cases 1.1.1 or 2.1
				if (!this.isInternalDeclarationAllowed()) {
					return _commentInserted;
				}
				// We try to enrich or accomplish defective type information
				if (root.constants.get(varName) != null) {
					this.appendDeclaration(root, varName, _indent, true);
					// KGU#424: Avoid the comment here if the element contains mere declarations
					return true;
				}	
			}
			// END KGU#375 2017-04-12
			if (isDecl && (this.isInternalDeclarationAllowed() || exprTokens != null)) {
				// cases 1.1.2 or 2.2
				if (tokens.get(0).equalsIgnoreCase("var") || tokens.get(0).equalsIgnoreCase("dim")) {
					// Case 1.1.2a/b or 2.2a/b (Pascal/BASIC declaration)
					String separator = tokens.get(0).equalsIgnoreCase("dim") ? "as" : ":";
					int posColon = tokens.indexOf(separator, 2, false);
					// Declaration well-formed?
					if (posColon > 0) {
						// Compose the lval without type
						codeLine = transform(tokens.subSequence(1, posColon).concatenate().trim());
						if (this.isInternalDeclarationAllowed()) {
						// START KGU#1089 2023-10-12: Issue #980 Face a list of variables
							//// START KGU#711 2019-10-01: Enh. #721 Precaution for Javascript
							//if (exprTokens == null && wasDefHandled(root, varName, false)) {
							//	return commentInserted;
							//}
							//// END KGU#711 2019-10-01
							//// Insert the type description
							//String type = tokens.subSequence(posColon+1, posAsgn).concatenate().trim();
							//// START KGU#561 2018-07-21: Bugfix #564
							////codeLine = transform(transformType(type, "")) + " " + codeLine;
							//type = transformType(type, "");
							//codeLine = this.transformArrayDeclaration(type, codeLine);
							//// END KGU#561 2018-07-21
							String type = tokens.subSequence(posColon+1, posAsgn).concatenate().trim();
							StringList declItems = Element.splitExpressionList(tokens.subSequence(1, posColon), ",", false);
							for (int i = 0; i < declItems.count(); i++) {
								String declItem = declItems.get(i).trim();
								// FIXME there could be asterisks and more!
								int posBrack = declItem.indexOf('[');
								String brackets = "";
								if (posBrack >= 0) {
									brackets = declItem.substring(posBrack);
									declItem = declItem.substring(0, posBrack).trim();
								}
								// START KGU#711 2019-10-01: Enh. #721 Precaution for Javascript
								if (declItem.isEmpty() || exprTokens == null && wasDefHandled(root, declItem, false)) {
									codeLine = null;	// If this was the last loop cycle then ensure nothing gets coded.
									continue;
								}
								// END KGU#711 2019-10-01
								StringList dims = new StringList();
								while (brackets.startsWith("[")) {
									StringList ranges = Element.splitExpressionList(brackets.substring(1), ",", true);
									dims.add(ranges.subSequence(0, ranges.count()-1));
									brackets = ranges.get(ranges.count()-1).trim();
									if (brackets.startsWith("]")) {
										brackets = brackets.substring(1).trim();
									}
								}
								String type1 = type;
								if (!dims.isEmpty()) {
									type1 = "array [ " + dims.concatenate(",") + " ] of " + type;
								}
								// Insert the type description
								// START KGU#561 2018-07-21: Bugfix #564
								//codeLine = transform(transformType(type, "")) + " " + codeLine;
								type1 = transformType(type1, "");
								codeLine = this.transformArrayDeclaration(type1, declItem);
								// END KGU#561 2018-07-21
								if (!_commentInserted) {
									appendComment(_inst, _indent);
									_commentInserted = true;
								}
								if (exprTokens == null || declItems.count() > 1) {
									addCode(codeLine + ";", _indent, isDisabled);
									wasDefHandled(root, declItem, true);
									codeLine = null;
								}
							}
							// END KGU#1089 2023-10-12: Issue #980
						}
					}
				}
				else {
					// Case 1.1.2c or 2.2c (2.2c not needed if internal declarations not allowed)
					// Must be C-style declaration
					if (this.isInternalDeclarationAllowed()) {
						// Case 2.2c (allowed) or 1.1.2c
						// START KGU#711 2019-10-01: Enh. #721 Avoid nonsense declarations in Javascript
						// START KGU#1089 2023-10-16: Bugfix #980 Handle multi-variable declarations sensibly
						//if (exprTokens == null && this.wasDefHandled(root, varName, false)) {
						//	return _commentInserted;
						//}
						//String declVar = varName;
						StringList declVars = null;
						if (varName != null) {
							declVars = StringList.getNew(varName);
						}
						else {
							// Apparently many declared variables, ambiguous assignment
							declVars = Instruction.getDeclaredVariables(pureTokens);
							if (declVars.count() > 1) {
								exprTokens = null;
							}
						}
						int posVar0 = 0;
						StringList declZones = new StringList();
						for (int i = 0; i < declVars.count(); i++) {
							String declVar = declVars.get(i);
							// END KGU#1089 2023-10-16
							if (exprTokens == null && declVar != null
									&& this.wasDefHandled(root, declVar, false)) {
								return _commentInserted;
							}
							// END KGU#711 2019-10-01
							// START KGU#560 2018-07-22: Bugfix #564
							//codeLine = transform(tokens.subSequence(0, posAsgn).concatenate().trim());
							TypeMapEntry type = this.typeMap.get(declVar);
							if (type != null && type.isArray()) {
								String canonType = type.getCanonicalType(true, false);
								codeLine = this.makeArrayDeclaration(this.transformType(canonType, "int"), declVar, type);
							}
							else {
								// START KGU#711 2019-09-30: Enh. #721: Consider Javascript
								// Combine type and variable as is
								//codeLine = transform(tokens.subSequence(0, posAsgn).concatenate().trim());
								int posVar = tokens.indexOf(declVar);
								if (i == 0) {
									posVar0 = posVar;
									declZones = Element.splitExpressionList(tokens.subSequence(posVar0, posAsgn), ",", true);
								}
								StringList typeStr = tokens.subSequence(0, posVar0);
								int posLBrack = declZones.get(i).indexOf('[');
								int posRBrack = declZones.get(i).lastIndexOf(']');
								if (!typeStr.isEmpty() && posLBrack > 0 && posRBrack > posLBrack) {
									typeStr.insert("array "
											+ declZones.get(i).substring(posLBrack, posRBrack+1) + " of ", 0);
								}
								else if (typeStr.isEmpty() && exprTokens != null) {
									declVar = declZones.get(i);
								}
								codeLine = this.composeTypeAndNameForDecl(
										typeStr.concatenate(null).trim(),
										declVar.trim());
								codeLine = transform(codeLine);
								// END KGU#711 2019-09-30
							}
							// END KGU#560 2018-07-22
							addCode(codeLine + ";", _indent, isDisabled);
							codeLine = null;
						}
					}
					else if (exprTokens != null) {
						// Case 1.1.2c (2.2c not allowed)
						// Cut out leading type specification
						int posVar = tokens.indexOf(varName);
						// START KGU#560 2018-07-21: Bugfix #564 In case of an array declaration we must wipe off the array stuff
						//codeLine = transform(tokens.subSequence(posVar, posAsgn).concatenate().trim());
						int posEnd = tokens.indexOf("[", posVar+1);
						if (!isDecl || posEnd < 0 || posEnd > posAsgn) {
							posEnd = posAsgn;
						}
						codeLine = transform(tokens.subSequence(posVar, posEnd).concatenate().trim());
						// END KGU#560 2018-07-21
					}
//							// START KGU#375 2017-04-13: Enh. #388
//							//codeLine = transform(tokens.concatenate().trim());
//							else if (tokens.get(0).equals("const")) {
//								// We try to enrich or accomplish defective type information
//								Root root = Element.getRoot(_inst);
//								if (root.constants.get(varName) != null) {
//									this.insertDeclaration(root, varName, _indent, true);
//									// START KGU#424 2017-09-26: Avoid the comment here if the element contains mere declarations
//									commentInserted = true;
//									// END KGU#424 2017-09-26
//									continue;
//								}
//							}
				}
			}
			else if (!isDecl && exprTokens != null) {
				// Case 1.2
				// Combine variable access as is
				codeLine = transform(tokens.subSequence(0, posAsgn).concatenate()).trim();
				// START KGU#767 2019-11-30: Bugfix #782 maybe we must introduce a postponed declaration here
				if (varName != null
						&& Function.testIdentifier(varName, false, null)
						&& codeLine.indexOf(varName) + varName.length() == codeLine.length()
						&& !paramNames.contains(varName)
						&& !this.wasDefHandled(root, varName, false)) {
					TypeMapEntry type = this.typeMap.get(varName);
					String typeName = "???";
					if (type != null) {
						typeName = transformTypeFromEntry(type, null, true);
						if (type.isRecord()) {
							isDecl = true;
						}
						// START KGU#784 2019-12-02
						else if (type.isArray() && !this.arrayBracketsAtTypeName()) {
							int posBrack = typeName.indexOf("[");
							if (posBrack > 0) {
								codeLine += typeName.substring(posBrack);
								typeName = typeName.substring(0, posBrack);
							}
						}
						// END KGU#784 2019-12-02
					}
					codeLine = typeName + " " + codeLine;
					this.setDefHandled(root.getSignatureString(false, false), varName);
				}
				// END KGU#767 2019-11-30
			}
			// Now we care for a possible assignment
			if (codeLine != null && exprTokens != null && pureExprTokens.count() > 0) {
				// START KGU#560 2018-07-21: Bugfix #564 - several problems with array initializers
				int posBrace = pureExprTokens.indexOf("{");
				if (posBrace >= 0 && posBrace <= 1 && pureExprTokens.get(pureExprTokens.count()-1).equals("}")) {
					// Case 1.1 or 1.2.1 (either array or record initializer)
					if (posBrace == 1 && pureExprTokens.count() >= 3 && Function.testIdentifier(pureExprTokens.get(0), false, null)) {
						String typeName = pureExprTokens.get(0);
						TypeMapEntry recType = this.typeMap.get(":"+typeName);
						if (isDecl && this.isInternalDeclarationAllowed() && recType != null) {
							// transforms the Structorizer record initializer into a C-conform one
							expr = this.transformRecordInit(exprTokens.concatenate().trim(), recType);
						}
						else {
							// In this case it's either no declaration or the declaration has already been generated
							// at the block beginning
							if (!_commentInserted) {
								appendComment(_inst, _indent);
								_commentInserted = true;
							}
							// FIXME: Possibly codeLine (the lval string) might be too much as first argument
							// START KGU#559 2018-07-20: Enh. #563
							//this.generateRecordInit(codeLine, pureExprTokens.concatenate(), _indent, isDisabled, null);
							this.generateRecordInit(codeLine, pureExprTokens.concatenate(), _indent, isDisabled, recType);
							// END KGU#559 2018-07-20
							return _commentInserted;
						}
					}
					else {
						StringList items = Element.splitExpressionList(pureExprTokens.subSequence(1, pureExprTokens.count()), ",", true);
						String elemType = null;
						TypeMapEntry arrType = this.typeMap.get(varName);
						if (arrType != null && arrType.isArray()) {
							elemType = arrType.getCanonicalType(true, false);
							if (elemType != null && elemType.startsWith("@")) {
								elemType = elemType.substring(1);
							}
							// START KGU#784 2019-12-02: varName is only part of the left side, there may be indices, so reduce the type if so
							int posIdx = codeLine.indexOf(varName) + varName.length();
							String indices = codeLine.substring(posIdx).trim();
							while (elemType.startsWith("@") && indices.startsWith("[")) {
								elemType = elemType.substring(1);
								StringList indexList = Element.splitExpressionList(indices.substring(1), ",", true);
								indexList.remove(0); // Drop first index expression (has already been handled)
								// Are there perhaps more indices within the same bracket pair (comma-separated list)?
								while (indexList.count() > 1 && elemType.startsWith("@")) {
									indexList.remove(0);
									elemType = elemType.substring(1);
								}
								if (indexList.isEmpty()) {
									indices = "";
								}
								else if (indexList.get(0).trim().startsWith("]")) {
									// This should be the tail
									indices = indexList.get(0).substring(1);
								}
							}
							// END KGU#784 2019-12-02
						}
						expr = this.transformOrGenerateArrayInit(codeLine, items.subSequence(0, items.count()-1), _indent, isDisabled, elemType, isDecl);
						if (expr == null) {
							return _commentInserted;
						}
					}
				}
				// END KGU#560 2018-07-21
				else {
					expr = this.transform(exprTokens.concatenate()).trim();
				}
			}
			if (expr != null) {
				// In this case codeLine must be different from null
				codeLine += " = " + expr;
			}
		} // if (!this.suppressTransformation && (isDecl || exprTokens != null))
		// START KGU#388 2017-09-25: Enh. #423
		else if (!this.suppressTransformation && Instruction.isTypeDefinition(_line, typeMap)) {
			// Attention! The following condition must not be combined with the above one! 
			if (this.isInternalDeclarationAllowed()) {
				tokens.removeAll(" ");
				// START KGU#878 2020-10-16: Bugfix #873 - collateral damage of bugfix #808 mended
				//int posEqu = tokens.indexOf("=");
				int posEqu = tokens.indexOf("==");
				// END KGU#878 2020-10-16
				String typeName = null;
				if (posEqu == 2) {
					typeName = tokens.get(1);
				}
				TypeMapEntry type = this.typeMap.get(":" + typeName);
				Root root = Element.getRoot(_inst);
				if (type != null) {
					this.generateTypeDef(root, typeName, type, _indent, isDisabled);
					_commentInserted = true;
					// CodeLine is not filled because the code has already been generated
				}
				else {
					// Hardly a recognizable type definition, just put it as is...
					codeLine = "typedef " + transform(tokens.concatenate(" ", posEqu + 1)) + " " + typeName;
				}
			}
		}
		// END KGU#388 2017-09-25
		else {
			// All other cases (e.g. input, output)
			// START KGU#653 2019-02-14: Enh. #680 - care for multi-variable input lines
			//codeLine = transform(line);
			StringList inputItems = Instruction.getInputItems(_line);
			if (inputItems == null || !generateMultipleInput(inputItems, _indent, isDisabled, _commentInserted ? null : _inst.getComment())) {
				codeLine = transform(_line);
			}
			else {
				codeLine = null;
			}
			// END KGU#653 2019-02-14
		}
		// Now append the codeLine in case it was composed and not already appended
		if (codeLine != null) {
			String lineEnd = ";";
			if (Instruction.isTurtleizerMove(_line)) {
				codeLine = this.enhanceWithColor(codeLine, _inst);
				lineEnd = "";	// codeLine already contains a line end in this case
			}
			// START KGU#424 2017-09-26: Avoid the comment here if the element contains mere declarations
			if (!_commentInserted) {
				appendComment(_inst, _indent);
				_commentInserted = true;
			}
			// END KGU#424 2017-09-26
			// START KGU#794 2020-02-11: Issue #806
			if (codeLine.startsWith("printf(")) {
				this.appendComment("TODO: check format specifiers, replace all '?'!", _indent);
			}
			// END KGU#794 2020-02-11
			addCode(codeLine + lineEnd, _indent, isDisabled);
		}
		// END KGU#261 2017-01-26
		return _commentInserted;
	}

	// START KGU#653 2019-02-14: Enh. #680 - auxiliary methods for handling multi-item input instructions
	/**
	 * Generates a series of code lines representing a decomposed multi-item input instruction
	 * @param _inputItems - list of input items (first the prompt string, then the lvar expressions)
	 * @param _indent - current code indentation
	 * @param _isDisabled - whether the containing instruction is disabled (i.e. is to be commented out)
	 * @param _comment - the instruction comment (if not exported already)
	 * @return true is all was done here, false if no code had been expressed
	 */
	private boolean generateMultipleInput(StringList _inputItems, String _indent, boolean _isDisabled, StringList _comment) {
		boolean done = false;
		if (this.getClass().getSimpleName().equals("CGenerator")) {
			this.appendComment("TODO: check format specifiers, replace all '?'!", _indent);
		}
		// START KGU#794 2020-02-13: Issue #806
		//if (_inputItems.count() > 2) {
		if (_inputItems.count() > 2 || this.getClass().getSimpleName().equals("CGenerator") && _inputItems.count() == 2) {
		// END KGU#794 2020-02-13
			String inputKey = CodeParser.getKeyword("input") + " ";
			String prompt = _inputItems.get(0);
			if (!prompt.isEmpty()) {
				prompt += " ";
			}
			_inputItems.remove(0);
			if (_comment != null) {
				this.appendComment(_comment, _indent);
			}
			String targetList = composeInputItems(_inputItems);
			if (targetList != null) {
				// Multiple-item conversion available, so create a single line
				_inputItems.clear();
				_inputItems.add(targetList);
			}
			else if (!prompt.isEmpty()) {
				addCode(transform(CodeParser.getKeyword("output") + " " + prompt), _indent, _isDisabled);
			}
			for (int i = 0; i < _inputItems.count(); i++) {
				if (targetList == null) {
					prompt = "\"" + _inputItems.get(i) + ": \" ";
				}
				String codeLine = transform(inputKey + prompt + _inputItems.get(i));
				addCode(codeLine + ";", _indent, _isDisabled);
			}
			done = true;
		}
		return done;
	}
	/**
	 * Subclassable method possibly to obtain a suited transformed argument list string for the given series of
	 * input items (i.e. expressions designating an input target variable each) to be inserted in the input replacer
	 * returned by {@link #getInputReplacer(boolean)}, this allowing to generate a single input instruction only.<br/>
	 * This instance concatenates all elements with commas and address operators and inserts a draft format string
	 * at the beginning (as needed for {@code scanf)).
	 * @param _inputVarItems - {@link StringList} of variable descriptions for input
	 * @return either a syntactically converted combined string with suited operator or separator symbols, or null.
	 */
	@Override
	protected String composeInputItems(StringList _inputVarItems)
	{
		// START KGU#794 2020-02-11: Issue #806 - more intelligent format preparation
		//return _inputVarItems.concatenate(", &");
		StringList format = new StringList();
		StringList items = new StringList();
		for (int i = 0; i < _inputVarItems.count(); i++) {
			String varItem = _inputVarItems.get(i);
			String[] formatAndItem = this.makeScanfFormatVarPair(varItem);
			format.add(formatAndItem[0]);
			items.add(formatAndItem[1]);
		}
		items.insert("§$§$§" + format.concatenate(" ") + "§$§$§", 0);
		return items.concatenate(", ");
		// END KGU#794 2020-02-11
	}
	// END KGU#653 2019-02-14
	
	// START KGU#794 2020-02-11: Issue #806
	private String[] makeScanfFormatVarPair(String inputItem)
	{
		String[] pair = new String[] {
				"%?",
				"&" + inputItem
		};
		TypeMapEntry type = this.typeMap.get(inputItem);
		if (type != null) {
			String typeName = this.transformTypeFromEntry(type, null, true);
			if (typeName.equals("char*")) {
				pair[0] = "%s";
				pair[1] = inputItem;	// No address operator!
			}
			else {
				if (typeName.startsWith("unsigned")) {
					pair[0] ="%u";
				}
				else if (typeName.endsWith("int") || typeName.endsWith("long") || typeName.endsWith("short")) {
					pair[0] = "%i";
				}
				else if (typeName.equalsIgnoreCase("bool")) {
					pair[0] = "%d";
				}
				else if (typeName.endsWith("double")) {
					pair[0] = "%lg";
				}
				else if (typeName.equals("float")) {
					pair[0] = "%g";
				}
				else if (typeName.endsWith("char")) {
					pair[0] = "%c1";
				}
			}
		}
		return pair;
	}
	// END KGU#794 2020-02-11

	protected String enhanceWithColor(String _codeLine, Instruction _inst) {
		return _codeLine + "; " + this.commentSymbolLeft() + " color = " + _inst.getHexColor();
	}

	@Override
	protected void generateCode(Alternative _alt, String _indent) {
		
		appendComment(_alt, _indent);
		
		// START KGU#453 2017-11-02: Issue #447
		//String condition = transform(_alt.getText().getLongString(), false).trim();
		String condition = transform(_alt.getUnbrokenText().getLongString(), false).trim();
		// END KGU#453 2017-11-02
		// START KGU#301 2016-12-01: Bugfix #301
		//if (!condition.startsWith("(") || !condition.endsWith(")"))
		if (!isParenthesized(condition))
		// END KGU#301 2016-12-01
			condition = "(" + condition + ")";
		
		appendBlockHeading(_alt, "if " + condition, _indent);
		generateCode(_alt.qTrue, _indent + this.getIndent());
		appendBlockTail(_alt, null, _indent);

		if (_alt.qFalse.getSize() != 0) {
			appendBlockHeading(_alt, "else", _indent);
			generateCode(_alt.qFalse, _indent + this.getIndent());
			appendBlockTail(_alt, null, _indent);
		}
	}

	@Override
	protected void generateCode(Case _case, String _indent) {
		
		boolean isDisabled = _case.isDisabled(false);
		appendComment(_case, _indent);
		
		// START KGU#453 2017-11-02: Issue #447
		//StringList lines = _case.getText();
		StringList lines = _case.getUnbrokenText();
		// END KGU#453 2017-11-02
		String condition = transform(lines.get(0), false);
		// START KGU#301 2016-12-01: Bugfix #301
		//if (!condition.startsWith("(") || !condition.endsWith(")")) {
		if (!isParenthesized(condition)) {
		// END KGU#301 2016-12-01
			condition = "(" + condition + ")";
		}

		appendBlockHeading(_case, "switch " + condition, _indent);

		for (int i = 0; i < _case.qs.size() - 1; i++) {
			// START KGU#15 2015-10-21: Support for multiple constants per
			// branch
			// START KGU#755 2019-11-08: Bugfix #769 - more precise splitting necessary
			//StringList constants = StringList.explode(lines.get(i + 1), ",");
			StringList constants = Element.splitExpressionList(lines.get(i + 1), ",");
			// END KGU#755 2019-11-08
			for (int j = 0; j < constants.count(); j++) {
				addCode("case " + constants.get(j).trim() + ":", _indent, isDisabled);
			}
			// END KGU#15 2015-10-21
			// START KGU#380 2017-04-14: Bugfix #394 - Avoid redundant break instructions
			//generateCode((Subqueue) _case.qs.get(i), _indent + this.getIndent());
			//addCode(this.getIndent() + "break;", _indent, isDisabled);
			Subqueue sq = _case.qs.get(i);
			generateCode(sq, _indent + this.getIndent());
			Element lastEl = null;
			for (int j = sq.getSize() - 1; lastEl == null && j >= 0; j--) {
				if ((lastEl = sq.getElement(j)).isDisabled(true)) {
					lastEl = null;
				}
			}
			Integer label = null;
			if (lastEl == null || !(lastEl instanceof Jump) || (label = this.jumpTable.get(lastEl)) != null && label == -1) {
				addCode(this.getIndent() + "break;", _indent, isDisabled);
			}
			// END KGU#380 2017-04-14
		}

		if (!lines.get(_case.qs.size()).trim().equals("%")) {
			addCode("default:", _indent, isDisabled);
			Subqueue squeue = (Subqueue) _case.qs.get(_case.qs.size() - 1);
			generateCode(squeue, _indent + this.getIndent());
			// START KGU#71 2015-11-10: For an empty default branch, at least a
			// semicolon is required
			if (squeue.getSize() == 0) {
				addCode(this.getIndent() + ";", _indent, isDisabled);
			}
			// END KGU#71 2015-11-10
		}
		
		appendBlockTail(_case, null, _indent);
	}

	// END KGU#18/#23 2015-10-20

	@Override
	protected void generateCode(For _for, String _indent) {

		appendComment(_for, _indent);
		
		// START KGU#61 2016-03-22: Enh. #84 - Support for FOR-IN loops
		if (_for.isForInLoop())
		{
			// There aren't many ideas how to implement this here in general,
			// but subclasses may have better chances to do so.
			if (generateForInCode(_for, _indent)) return;
		}
		// END KGU#61 2016-03-22

		String var = _for.getCounterVar();
		String decl = "";
		// START KGU#376 2017-09-27: Enh. #389
		if (this.isInternalDeclarationAllowed() && !wasDefHandled(Element.getRoot(_for), var, false)) {
			// We just insert a loop-local declaration
			decl = "int ";
		}
		// END KGU#376 2017-09-27
		int step = _for.getStepConst();
		String compOp = (step > 0) ? " <= " : " >= ";
		String increment = var + " += (" + step + ")";
		// START KGU#934 2021-02-13: Bugfix #935: NullpointerException
		//appendBlockHeading(_for, "for (" + decl + var + " = "
		//		+ transform(_for.getStartValue(), false) + "; " + var + compOp
		//		+ transform(_for.getEndValue(), false) + "; " + increment + ")",
		//		_indent);
		String header = transform(_for.getUnbrokenText().getLongString());
		if (_for.style == For.ForLoopStyle.COUNTER) {
			header = "for (" + decl + var + " = "
					+ transform(_for.getStartValue(), false) + "; "
					+ var + compOp
					+ transform(_for.getEndValue(), false) + "; "
					+ increment + ")";
		}
		else {
			appendComment("TODO: No automatic FOR loop conversion found!", _indent);
		}
		appendBlockHeading(_for, header, _indent);
		// END KGU#934 2021-02-13

		generateCode(_for.q, _indent + this.getIndent());

		appendBlockTail(_for, null, _indent);

	}
	
	// START KGU#61 2016-03-22: Enh. #84 - Support for FOR-IN loops
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
		String valueList = _for.getValueList();
		TypeMapEntry typeInfo = this.typeMap.get(valueList);
		StringList items = this.extractForInListItems(_for);
		String itemVar = var;
		String itemType = "";
		String nameSuffix = Integer.toHexString(_for.hashCode());
		String arrayName = "array" + nameSuffix;
		String indexName = "index" + nameSuffix;
		String indent = _indent + this.getIndent();
		String startValStr = "0";
		String endValStr = "???";
		boolean isDisabled = _for.isDisabled(false);
		// START KGU#640 2019-01-21: Bugfix #669
		boolean isLoopConverted = false;
		// END KGU#640 2019-01-21
		if (items != null)
		{
			// Good question is: how do we guess the element type and what do we
			// do if items are heterogeneous? We will make use of the typeMap and
			// hope to get sensible information. Otherwise we add a TODO comment.
			int nItems = items.count();
			boolean allChar = true;	// KGU#782 2019-12-02: We now also detect char elements
			boolean allInt = true;
			boolean allDouble = true;
			boolean allString = true;
			StringList itemTypes = new StringList();
			for (int i = 0; i < nItems; i++)
			{
				String item = items.get(i);
				String type = Element.identifyExprType(this.typeMap, item, false);
				itemTypes.add(this.transformType(type, "int"));
				if (!type.equals("char")) {
					allChar = false;
				}
				if (!type.equals("int") && !type.equals("boolean") && !type.equals("char")) {
					allInt = false;
				}
				// START KGU#355 2017-03-30: #365 - allow type conversion
				//if (!type.equals("double")) {
				if (!type.equals("int") && !type.equals("boolean") && !type.equals("double")) {
				// END KGU#355 2017-03-30
					allDouble = false;
				}
				if (!type.equals("String") && !type.equals("char")) {
					allString = false;
				}
			}
			if (allChar) itemType = "char";
			else if (allInt) itemType = "int";
			else if (allDouble) itemType = "double";
			else if (allString) itemType = "char*";
			String arrayLiteral = "{" + items.concatenate(", ") + "}";

			// Start an extra block to encapsulate the additional definitions
			addCode("{", _indent, isDisabled);
			
			if (itemType.isEmpty())
			{
				if (nItems <= 1) {
					itemType = "int";	// the default...
				}
				else {
					itemType = "union ItemTyp" + nameSuffix;
					// We create a dummy type definition
					String typeDef = itemType + " {";
					// START KGU#355 2017-03-30: #365 - initializers needs selectors
					// and we overwrite the array literal
					arrayLiteral = "{";
					// END KGU#355 2017-03-30
					for (int i = 0; i < nItems; i++) {
						typeDef += itemTypes.get(i) + " comp" + i + "; ";
						// START KGU#355 2017-03-30: #365 - initializers needs selectors
						if (i > 0) arrayLiteral += ", ";
						arrayLiteral += ".comp" + i + "<-" + items.get(i);
						// END KGU#355 2017-03-30
					}
					// START KGU#355 2017-03-30: #365 - initializers needs selectors
					//typeDef += "}";
					typeDef = typeDef.trim() + "};";
					arrayLiteral += "}";
					// END KGU#355 2017-03-30
					// START KGU#355 2017-03-30: #365 - it was not correct that types must be defined globally
					//this.addGlobalTypeDef(typeDef, "TODO: Define a sensible 'ItemType' for the loop further down", isDisabled);
					this.addCode(typeDef, indent, isDisabled);
					// END KGU#355 2017-03-30
					this.appendComment("TODO: Prepare the elements of the array according to defined type (or conversely).", indent);
				}
			}
			// We define a fixed array here
			addCode(itemType + " " + arrayName +  "[" + nItems + "] = "
					+ transform(arrayLiteral, false) + ";", indent, isDisabled);
			
			endValStr = Integer.toString(nItems);
			// START KGU#640 2019-01-21: Bugfix #669
			isLoopConverted = true;
			// END KGU#640 2019-01-21
		}
		else if (typeInfo != null && typeInfo.isArray()) {
			String limitName = "count" + nameSuffix;
			StringList typeDecls = getTransformedTypes(typeInfo, false);
			if (typeDecls.count() == 1) {
				itemType = typeDecls.get(0).substring(1);
				int lastAt = itemType.lastIndexOf('@');
				if (lastAt >= 0) {
					itemType = itemType.substring(lastAt+1);
					for (int i = 0; i <= lastAt; i++) {
						itemVar += "[]";
					}
				}
			}
			startValStr = Integer.toString(Math.max(0, typeInfo.getMinIndex(0)));
			int endVal = typeInfo.getMaxIndex(0);
			if (endVal > -1) {
				endValStr = Integer.toString(endVal + 1);
			}
			arrayName = valueList;
			
			// Start an extra block to encapsulate the additional definitions
			addCode("{", _indent, isDisabled);

			if (endValStr.equals("???")) {
				this.appendComment("TODO: Find out and fill in the number of elements of the array " + valueList + " here!", _indent);
			}
			addCode("int " + limitName + " = " + endValStr +";", indent, isDisabled);

			endValStr = limitName;
			// START KGU#640 2019-01-21: Bugfix #669
			isLoopConverted = true;
			// END KGU#640 2019-01-21
		}
		// START KGU#640 2019-01-21: Bugfix #669 - There could as well be a string as type but no items
		else if (typeInfo != null && typeInfo.getCanonicalType(true, true).equalsIgnoreCase("string")) {
			// Just a dummy block to be compatible with other branches
			addCode("{", _indent, isDisabled);
			endValStr = "strlen(" + valueList + ")";
			arrayName = valueList;
			isLoopConverted = true;
		}
		// END KGU#640 2019-01-21
		
		// START KGU#640 2019-01-21: Bugfix #669
		//if (items != null || typeInfo != null) {
		if (isLoopConverted) {
		// END KGU#640 2019-01-21
			
			// Definition of the loop index variable
			addCode("int " + indexName + ";", indent, isDisabled);

			// Creation of the loop header
			appendBlockHeading(
					_for, "for (" + indexName + " = " + startValStr + "; " +
					indexName + " < " + endValStr + "; " + indexName + "++)",
					indent);
			
			// Assignment of a single item to the given variable
			if (itemType.startsWith("union ")) {
				this.appendComment("TODO: Extract the value from the appropriate component here and care for type conversion!", _indent);
			}
			// Well, this is local to the loop, so it won't cause trouble with an automatic declaration in the outer context
			addCode(this.getIndent() + (itemType + " " + itemVar + " = " +
					arrayName + "[" + indexName + "];").trim(), indent, isDisabled);

			// Add the loop body as is
			generateCode(_for.q, indent + this.getIndent());

			// Accomplish the loop
			appendBlockTail(_for, null, indent);

			// Close the extra block
			addCode("}", _indent, isDisabled);
			done = true;
		}
		else
		{
			// END KGU#355 2017-03-05
			// We have no strategy here, no idea how to find out the number and type of elements,
			// no idea how to iterate the members, so we leave it similar to C# and just add a TODO comment...
			this.appendComment("TODO: Rewrite this loop (there was no way to convert this automatically)", _indent);

			// Creation of the loop header
			appendBlockHeading(_for, "foreach (" + var + " in " + transform(valueList, false) + ")", _indent);
			// Add the loop body as is
			generateCode(_for.q, _indent + this.getIndent());
			// Accomplish the loop
			appendBlockTail(_for, null, _indent);
			
			done = true;
		}
		return done;
	}
	// END KGU#61 2016-03-22

	@Override
	protected void generateCode(While _while, String _indent) {
		
		appendComment(_while, _indent);
		

		String condition = transform(_while.getText().getLongString(), false)
				.trim();
		// START KGU#301 2016-12-01: Bugfix #301
		//if (!condition.startsWith("(") || !condition.endsWith(")")) {
		if (!isParenthesized(condition)) {
		// END KGU#301 2016-12-01
			condition = "(" + condition + ")";
		}

		appendBlockHeading(_while, "while " + condition, _indent);

		generateCode(_while.q, _indent + this.getIndent());

		appendBlockTail(_while, null, _indent);

	}

	@Override
	protected void generateCode(Repeat _repeat, String _indent) {
		
		appendComment(_repeat, _indent);

		appendBlockHeading(_repeat, "do", _indent);

		generateCode(_repeat.q, _indent + this.getIndent());

		// START KGU#301 2016-12-01: Bugfix #301
		//insertBlockTail(_repeat, "while (!(" 
		//		+ transform(_repeat.getText().getLongString()).trim() + "))", _indent);
		// START KGU#811 2020-02-21: Use existing base technology
		//String condition = transform(_repeat.getText().getLongString()).trim();
		//if (!isParenthesized(condition)) {
		//	condition = "(" + condition + ")";
		//}
		//appendBlockTail(_repeat, "while (!" + condition + ")", _indent);
		String condition = Element.negateCondition(_repeat.getUnbrokenText().getLongString().trim());
		appendBlockTail(_repeat, "while (" + transform(condition) + ")", _indent);
		// END KGU#811 2020-02-21
		// END KGU#301 2016-12-01
	}

	@Override
	protected void generateCode(Forever _forever, String _indent) {
		
		appendComment(_forever, _indent);

		appendBlockHeading(_forever, "while (true)", _indent);

		generateCode(_forever.q, _indent + this.getIndent());

		appendBlockTail(_forever, null, _indent);
	}

	@Override
	protected void generateCode(Call _call, String _indent) {
 
		if (!appendAsComment(_call, _indent)) {

			boolean commentInserted = false;

			boolean isDisabled = _call.isDisabled(false);

			// START KGU#1065 2022-09-29: Bugfix #1073 Case comments occurred twice
			//appendComment(_call, _indent);
			// END KGU#1065 2022-09-29
			// In theory, here should be only one line, but we better be prepared...
			StringList lines = _call.getUnbrokenText();
			Root owningRoot = Element.getRoot(_call);
			for (int i = 0; i < lines.count(); i++) {
				String line = lines.get(i).trim();
//				// START KGU#376 2017-04-13: Enh. #389 handle import calls - withdrawn here
//				if (!isDisabled && Call.isImportCall(lines.get(i))) {
//					generateImportCode(_call, line, _indent);
//				}
//				else
//				// END KGU#376 2017-04-13
				// START KGU#371 2019-03-07: Enh. #385 Support for declared optional arguments
				boolean mustHealDefaults = line.endsWith(")") && this.getOverloadingLevel() == OverloadingLevel.OL_NO_OVERLOADING;
				if ((routinePool != null) && (mustHealDefaults || this.importedLibRoots != null)) {
					Function call = _call.getCalledRoutine(i);
					// START KGU#877 2020-10-16: Bugfix #874 name extraction may fail (e.g. non-ASCII letters)
					if (call != null) {
					// END KGU#877 2020-10-16
						java.util.Vector<Root> callCandidates = routinePool.findRoutinesBySignature(call.getName(), call.paramCount(), owningRoot, false);
						if (!callCandidates.isEmpty()) {
							// FIXME We'll just fetch the very first one for now...
							Root called = callCandidates.get(0);
							if (mustHealDefaults) {
								StringList defaults = new StringList();
								called.collectParameters(null, null, defaults);
								if (defaults.count() > call.paramCount()) {
									// We just add the list of default values for the missing arguments
									line = line.substring(0, line.length()-1) + (call.paramCount() > 0 ? ", " : "") + 
											defaults.subSequence(call.paramCount(), defaults.count()).concatenate(", ") + ")";
								}
							}
							// START KGU#815 2020-03-26: Enh. #828 we have to cope with class methods from a foreign library
							if (this.importedLibRoots != null && this.importedLibRoots.contains(called)) {
								StringList tokens = Element.splitLexically(line, true);
								Element.unifyOperators(tokens, true);
								int posAsgn = tokens.indexOf("<-");
								int posCall = tokens.indexOf(call.getName(), posAsgn+1);
								tokens.set(posCall, this.makeLibCallName(call.getName()));
								line = tokens.concatenate();
							}
							// END KGU#815 2020-03-26
						}
					// START KGU#877 2020-10-16: Bugfix #874 name extraction may fail (e.g. non-ASCII letters)
					}
					// END KGU#877 2020-10-16
				}
				// END KGU#371 2019-03-07
				// Input or Output should not occur here
				// START KGU#730 2019-09-24: Bugfix #752 ... but declarations (even const definitions) could occur!
//				addCode(transform(line, false) + ";", _indent, isDisabled);
				if (Instruction.isAssignment(line)) {
					commentInserted = generateInstructionLine(_call, _indent, commentInserted, line);
				}
				else {
					// START KGU#1065 2022-09-29: Bugfix #1073 Case comments had occurred twice
					if (!commentInserted) {
						appendComment(_call, _indent);
						commentInserted = true;
					}
					// END KGU#1065 2022-09-29
					addCode(transform(line, false) + ";", _indent, isDisabled);
				}
				// END KGU#730 2019-09-24
			}
		}
		
	}

	// START KGU#815 2020-03-26: Enh. #828 support for library references
	/**
	 * Returns the language-specifically qualified name for the call of routine {@code name}
	 * from the external library {@link #libModuleName}.
	 * @param name - pure (unqualified) routine name
	 * @return the transformed name
	 */
	protected String makeLibCallName(String name) {
		return name;
	}
	// END KGU#815 2020-03-26

	@Override
	protected void generateCode(Jump _jump, String _indent)
	{
		// START KGU 2015-10-18: The "export instructions as comments"
		// configuration had been ignored here
		// insertComment(_jump, _indent);
		// for(int i=0;i<_jump.getText().count();i++)
		// {
		// code.add(_indent+transform(_jump.getText().get(i))+";");
		// }
		if (!appendAsComment(_jump, _indent)) {
			
			boolean isDisabled = _jump.isDisabled(false);

			appendComment(_jump, _indent);

			// START KGU#380 2017-04-14: Bugfix #394 Done in another way now
			// KGU 2015-10-18: In case of an empty text generate a break
			// instruction by default.
			//boolean isEmpty = true;
			// END KGU#380 207-04-14
			
			StringList lines = _jump.getText();
			boolean isEmpty = lines.getLongString().trim().isEmpty();
			String preReturn = CodeParser.getKeywordOrDefault("preReturn", "return").trim();
			String preExit   = CodeParser.getKeywordOrDefault("preExit", "exit").trim();
			// START KGU#380 2017-04-14: Bugfix #394 - We don't consider superfluous lines anymore
			//String preLeave  = CodeParser.getKeywordOrDefault("preLeave", "leave").trim();
			//String preReturnMatch = Matcher.quoteReplacement(preReturn)+"([\\W].*|$)";
			//String preExitMatch   = Matcher.quoteReplacement(preExit)+"([\\W].*|$)";
			//String preLeaveMatch  = Matcher.quoteReplacement(preLeave)+"([\\W].*|$)";
			//for (int i = 0; isEmpty && i < lines.count(); i++) {
			//	String line = transform(lines.get(i)).trim();
			//	if (!line.isEmpty())
			//	{
			//		isEmpty = false;
			//	}
			String line = "";
			if (!isEmpty) {
				line = lines.get(0).trim();
			}
				// START KGU#74/KGU#78 2015-11-30: More sophisticated jump handling
				//code.add(_indent + line + ";");
				//if (line.matches(preReturnMatch))
				if (_jump.isReturn())
				{
					// START KGU#989 2021-10-01: Bugfix #989 missing expression translation
					//addCode("return " + line.substring(preReturn.length()).trim() + ";",
					addCode("return " + transform(line.substring(preReturn.length()).trim()) + ";",
					// END KGU#989 2021-10-01
							_indent, isDisabled);
				}
				//else if (line.matches(preExitMatch))
				else if (_jump.isExit())
				{
					// START KGU#989 2021-10-01: Bugfix #989 missing expression translation
					//appendExitInstr(line.substring(preExit.length()).trim(), _indent, isDisabled);
					appendExitInstr(transform(line.substring(preExit.length()).trim()), _indent, isDisabled);
					// END KGU#989 2021-10-01
				}
				// START KGU#686 2019-03-20: Enh. #56 Throw has to be implemented
				else if (_jump.isThrow() && this.getTryCatchLevel() != TryCatchSupportLevel.TC_NO_TRY) {
					// START KGU#989 2021-10-01: Bugfix #989 missing expression translation
					//this.generateThrowWith(line.substring(
					//		CodeParser.getKeywordOrDefault("preThrow", "throw").length()).trim(), _indent, isDisabled);
					this.generateThrowWith(transform(line.substring(
							CodeParser.getKeywordOrDefault("preThrow", "throw").length()).trim()), _indent, isDisabled);
					// END KGU#989 2021-10-01
				}
				// END KGU#686 2019-03-20
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
					addCode(this.getMultiLevelLeaveInstr() + " " + label + ";", _indent, isDisabled);
				}
				//else if (line.matches(preLeaveMatch))
				else if (_jump.isLeave())
				{
					// START KGU 2017-02-06: The "funny comment" was irritating and dubious itself
					// Seems to be an ordinary one-level break without need to concoct a jump statement
					// (Are there also strange cases - neither matched nor rejected? And how could this happen?)
					//addCode("break;\t// FIXME: Dubious occurrence of break instruction!", _indent, isDisabled);
					addCode("break;", _indent, isDisabled);
					// END KGU 2017-02-06
				}
				else if (!isEmpty)
				{
					appendComment("FIXME: jump/exit instruction of unrecognised kind!", _indent);
					appendComment(line, _indent);
				}
				// END KGU#74/KGU#78 2015-11-30
			}
//			if (isEmpty) {
//				addCode("break;", _indent, isDisabled);
//			}
//			// END KGU 2015-10-18
//		}
		// END KGU#380 207-04-14
	}

	/**
	 * This method is to be overridden by the subclasses to append a suited throw
	 * instruction from string {@code _thrown} as taken from the Jump element text
	 * line (after the "preThrow" keyword).<br/>
	 * If the throw occurs with in a catch block then field {@link #caughtException}
	 * will contain the exception to be rethrown if {@code _thrown} is empty.
	 * @param _thrown - the text line tail after the "preThrow" keyword.
	 * @param _indent - the current indentation
	 * @param _asComment - whether the throw instruction is to be exported as comment
	 */
	protected void generateThrowWith(String _thrown, String _indent, boolean _asComment) {
	}

	// START KGU#47 2015-11-30: Offer at least a sequential execution (which is one legal execution order)
	protected void generateCode(Parallel _para, String _indent)
	{

		boolean isDisabled = _para.isDisabled(false);
		appendComment(_para, _indent);

		addCode("", "", isDisabled);
		appendComment("==========================================================", _indent);
		appendComment("================= START PARALLEL SECTION =================", _indent);
		appendComment("==========================================================", _indent);
		appendComment("TODO: add the necessary code to run the threads concurrently", _indent);
		addCode("{", _indent, isDisabled);

		for (int i = 0; i < _para.qs.size(); i++) {
			addCode("", "", isDisabled);
			appendComment("----------------- START THREAD " + i + " -----------------", _indent + this.getIndent());
			addCode("{", _indent + this.getIndent(), isDisabled);
			generateCode((Subqueue) _para.qs.get(i), _indent + this.getIndent() + this.getIndent());
			addCode("}", _indent + this.getIndent(), isDisabled);
			appendComment("------------------ END THREAD " + i + " ------------------", _indent + this.getIndent());
			addCode("", "", isDisabled);
		}

		addCode("}", _indent, isDisabled);
		appendComment("==========================================================", _indent);
		appendComment("================== END PARALLEL SECTION ==================", _indent);
		appendComment("==========================================================", _indent);
		addCode("", "", isDisabled);
	}
	// END KGU#47 2015-11-30
	
	// START KGU#686 2019-03-18: Enh. #56
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateCode(lu.fisch.structorizer.elements.Try, java.lang.String)
	 */
	@Override
	protected void generateCode(Try _try, String _indent)
	{

		boolean isDisabled = _try.isDisabled(false);
		appendComment(_try, _indent);
	
		TryCatchSupportLevel trySupport = this.getTryCatchLevel();
		if (trySupport == TryCatchSupportLevel.TC_NO_TRY) {
			this.appendComment("TODO: Find an equivalent for this non-supported try / catch block!", _indent);
		}
		// We will temporarily modify the disabled status depending on the language capabilities
		_try.setDisabled(isDisabled || trySupport == TryCatchSupportLevel.TC_NO_TRY);
		try {
			this.appendBlockHeading(_try, "try", _indent);
			_try.setDisabled(isDisabled);

			generateCode(_try.qTry, _indent + this.getIndent());

			_try.setDisabled(isDisabled || trySupport == TryCatchSupportLevel.TC_NO_TRY);
			this.appendBlockTail(_try, null, _indent);
			String caught = this.caughtException;
			this.appendCatchHeading(_try, _indent);

			// If try/catch isn't supported then the entire catch block is to be disabled
			generateCode(_try.qCatch, _indent + this.getIndent());

			this.caughtException = caught;
			this.appendBlockTail(_try, null, _indent);
			if (_try.qFinally.getSize() > 0) {
				_try.setDisabled(isDisabled || trySupport != TryCatchSupportLevel.TC_TRY_CATCH_FINALLY);
				this.appendBlockHeading(_try, "finally", _indent);
				_try.setDisabled(isDisabled);

				generateCode(_try.qFinally, _indent + this.getIndent());

				_try.setDisabled(isDisabled || trySupport != TryCatchSupportLevel.TC_TRY_CATCH_FINALLY);
				this.appendBlockTail(_try, null, _indent);
			}
		}
		finally {
			// Restore the original disabled status
			_try.setDisabled(isDisabled);
		}
	}

	/**
	 * Generates the catch block header with the language-specific variable
	 * declarations etc.<br/>
	 * This base method just generates a header with a char array variable
	 * declaration, to be subclassed therefore.<br/>
	 * Whether the header gets exported uncommented depends on the current
	 * value of {@code _try.disabled}.
	 * @param _try - the {@link Try} element
	 * @param _indent - the current indentation (outside the block)
	 * @see #getTryCatchLevel()
	 */
	protected void appendCatchHeading(Try _try, String _indent) {
		String varName = _try.getExceptionVarName();
		String head = "catch (...)";
		if (varName != null && !varName.isEmpty()) {
			head = "catch(char " + varName + "[])";
		}
		this.appendBlockHeading(_try, head, _indent);
	}
	// END KGU#686 2019-03-18

	/**
	 * Composes the heading for the program or function according to the
	 * C language specification.
	 * @param _root - The diagram root
	 * @param _indent - the initial indentation string
	 * @param _procName - the procedure name
	 * @param paramNames - list of the argument names
	 * @param paramTypes - list of corresponding type names (possibly null) 
	 * @param resultType - result type name (possibly null)
	 * @return the default indentation string for the subsequent stuff
	 */
	@Override
	protected String generateHeader(Root _root, String _indent, String _procName,
			StringList _paramNames, StringList _paramTypes, String _resultType, boolean _public)
	{
		// START KGU#178 2016-07-20: Enh. #160
		if (!topLevel)
		{
			addSepaLine();
		}
		// END KGU#178 2016-07-20
		// START KGU#815/KGU#824 2020-03-20: Enh. #828, bugfix #836
		else if (this.isLibraryModule()) {
			appendScissorLine(true, this.pureFilename + ".h");
			String guardName = deriveIncludeGuardName(this.pureFilename + ".h");
			code.add("#ifndef " + guardName);
			code.add("#define " + guardName);
		}
		// START KGU#852 2020-04-22: Bugfix #854 - we must ensure topological order on export
		//this.typeMap = new HashMap<String, TypeMapEntry>(_root.getTypeInfo(routinePool));
		this.typeMap = new LinkedHashMap<String, TypeMapEntry>(_root.getTypeInfo(routinePool));
		// END KGU#852 2020-04-22
		// KGU#815/KGU#824 2020-03-20

		String pr = "program";
		if (_root.isSubroutine()) {
			pr = "function";
		} else if (_root.isInclude()) {
			pr = "includable";
		}
		appendComment(pr + " " + _root.getText().get(0), _indent);
		// START KGU#178 2016-07-20: Enh. #160
		if (topLevel)
		{
		// END KGU#178 2016-07-20
			// START KGU#815/KGU#824 2020-03-23: Enh. #828, bugfix #836
			if (this.usesFileAPI && (this.isLibraryModule() || this.importedLibRoots != null)) {
				generatorIncludes.addIfNew("FileAPI.h");
			}
			// END KGU#815/KGU#824 2020-03-23
			appendComment("Generated by Structorizer " + Element.E_VERSION, _indent);
			// START KGU#363 2017-05-16: Enh. #372
			appendCopyright(_root, _indent, true);
			// END KGU#363 2017-05-16
			addSepaLine();
			// START KGU#236 2016-08-10: Issue #227
			//code.add("#include <stdio.h>");
			//addSepaLine();
			if (this.hasInput() || this.hasOutput() || this.usesFileAPI)
			{
				code.add("#define _CRT_SECURE_NO_WARNINGS");	// VisualStudio precaution 
				this.generatorIncludes.addIfNew("<stdio.h>");
				// START KGU#815/KGU#824 2020-03-23: Enh. #828, bugfix #836
				//if (this.usesFileAPI) {
				if (this.usesFileAPI && !generatorIncludes.contains("FileAPI.h")) {
				// END KGU#815/KGU#824 2020-03-23
					this.generatorIncludes.addIfNew("<stdlib.h>");
					this.generatorIncludes.addIfNew("<string.h>");
					this.generatorIncludes.addIfNew("<errno.h>");
				}
			}
			// START KGU#607 2018-10-30: Issue 346
			this.generatorIncludes.addIfNew("<stdbool.h>");
			// END KGU#607 2018-10-30
			this.appendGeneratorIncludes("", false);
			addSepaLine();
			// START KGU#351 2017-02-26: Enh. #346 / KGU#3512017-03-17 had been mis-placed
			this.appendUserIncludes("");
			// START KGU#446 2017-10-27: Enh. #441
			this.includeInsertionLine = code.count();
			// END KGU#446 2017-10-27
			addSepaLine();
			// END KGU#351 2017-02-26
			// START KGU#376 2017-09-26: Enh. #389 - definitions from all included diagrams will follow
			appendGlobalDefinitions(_root, _indent, false);
			// END KGU#376 2017-09-26
			// START KGU#815/KGU#824 2020-03-20: Enh. #828, bugfix #836
			interfaceInsertionLine = code.count();
			if (this.isLibraryModule()) {
				addSepaLine();
				this.insertPrototype(_root, _indent, true, code.count());
				interfaceInsertionLine = code.count();
				addSepaLine();
				code.add("#endif /*" + this.deriveIncludeGuardName(this.pureFilename + ".h") + "*/");
				addSepaLine();
				appendScissorLine(true, this.pureFilename + "." + this.getFileExtensions()[0]);
				addSepaLine();
				appendComment("Generated by Structorizer " + Element.E_VERSION, _indent);
				appendCopyright(_root, _indent, true);
				addSepaLine();
				addCode("#include \"" + this.pureFilename + ".h\"", _indent, false);
				addSepaLine();
			}
			// END KGU#815/KGU#824 2020-03-20
			// END KGU#236 2016-08-10
		// START KGU#178 2016-07-20: Enh. #160
			subroutineInsertionLine = code.count();
			subroutineIndent = _indent;
			
			// START KGU#311 2016-12-22: Enh. #314 - insert File API routines if necessary
			// KGU#815/KGU#824 2020-03-20: Enh. #828, bugfix #836: Decomposed for group export!
			//if (this.usesFileAPI) {
			if (this.usesFileAPI && !generatorIncludes.contains("FileAPI.h")) {
			// END KGU#815/KGU#824 2020-03-20
				this.insertFileAPI("c");
			}
			// END KGU#311 2016-12-22
		}
		// END KGU#178 2016-07-20
		// START KGU#815 2020-03-26: Enh. #828
		else if (_public) {
			appendCopyright(_root, _indent, false);
		}
		// END KGU#815 2020-03-26

		// START KGU#815/KGU#824 2020-03-20: Enh. #828, bugfix #836 This call replaces the function header code
		//appendComment(_root, _indent);
		//if (_root.isProgram()) {
		//	code.add("int main(void)");
		//}
		//else {
		//	// Compose the function header
		//	// START KGU#676 2019-03-30: Enh. #696 special pool in case of batch export
		//	//this.typeMap = new HashMap<String, TypeMapEntry>(_root.getTypeInfo());
		//	this.typeMap = new HashMap<String, TypeMapEntry>(_root.getTypeInfo(routinePool));
		//	// END KGU#676 2019-03-30
		//}
		if (subroutineInsertionLine == code.count()) {
			code.add("");
		}
		this.insertPrototype(_root, _indent, !this.isLibraryModule(), code.count());
		// END KGU#815/KGU#824 2020-03-20
		code.add(_indent + "{");
		
		// START KGU#376 2017-09-26: Enh. #389 - add the initialization code of the includables
		// START KGU#815/KGU#824/KGU#834 2020-03-26: Moved to generateBody(Root, _indent)
		//appendGlobalInitialisations(_indent + this.getIndent());
		// END KGU#815/KGU#824 2020-03-20
		// END KGU#376 2017-09-26
		
		return _indent + this.getIndent();
	}

	// START KGU#815 2020-03-20: New auxiliary method for enh.#828
	/**
	 * Just derives a C preprocessor symbol from given {@code headerFilename} in
	 * order to form an include guard
	 * @param headerFileName - name of the header file
	 * @return the appropriate {@code #define} symbol.
	 */
	protected String deriveIncludeGuardName(String headerFileName) {
		char[] guard = headerFileName.toUpperCase().toCharArray();
		for (int i = 0; i < guard.length; i++) {
			if (!Character.isDigit(guard[i]) && (guard[i] < 'A' || guard[i] > 'Z')) {
				guard[i] = '_';
			}
		}
		return String.copyValueOf(guard);
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#appendGlobalInitialisations(java.lang.String)
	 */
	@Override
	protected void appendGlobalInitialisations(Root _root, String _indent) {
		if (topLevel && this.libModuleName != null) {
			// Multi-module export with a common library - insert its initialization function call
			appendGlobalInitialisationsLib(_indent);
		}
		else {
			super.appendGlobalInitialisations(_root, _indent);
		}
	}
	
	// START KGU#834 2020-03-26: Mechanism to ensure one-time initialisation
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#makeStaticInitFlagDeclaration(lu.fisch.structorizer.elements.Root)
	 */
	@Override
	protected String makeStaticInitFlagDeclaration(Root incl, boolean inGlobalDecl) {
		if (!inGlobalDecl) {
			return "static bool " + this.getInitFlagName(incl) + " = false;";
		}
		return null;
	}
	// END KGU#834 2020-03-26

	// START KGU#815 2020-03-20: Enh. #828
	/**
	 * Special handling for the global initializations in case these were outsourced to
	 * an external library {@link #libModuleName}.
	 * @param _indent - current indentation
	 * @see #appendGlobalInitialisations(Root, String)
	 */
	protected void appendGlobalInitialisationsLib(String _indent) {
		// We simply call the global initialisation function of the library
		addCode(this.libModuleName + "();", _indent, false);
	}
	// END KGU#815 2020-03-20

	/**
	 * Generates some preamble (i.e. comments, language declaration section etc.)
	 * and adds it to this.code.
	 * @param _root - the diagram root element
	 * @param _indent - the current indentation string
	 * @param varNames - list of variable names introduced inside the body
	 */
	@Override
	protected String generatePreamble(Root _root, String _indent, StringList varNames)
	{
		appendComment("TODO: Check and accomplish variable declarations:", _indent);
		// START KGU#261/KGU#332 2017-01-26: Enh. #259/#335: Add actual declarations if possible
		// START KGU#504 2018-03-13: Bugfix #520, #521: only add declarations if conversion is allowed
		//insertDefinitions(_root, _indent, varNames, false);
		if (!this.suppressTransformation) {
			appendDefinitions(_root, _indent, varNames, false);
		}
		// END KGU#504 2018-03-13
		// END KGU#261/KGU#332 2017-01-26
		// START KGU#332 2017-01-30: Decomposed to ease sub-classing
		generateIOComment(_root, _indent);
		// END KGU#332 2017-01-30
		addSepaLine();
		return _indent;
	}

	// START KGU#376 2017-09-26: Enh #389 - declaration stuff condensed to a method
	/**
	 * Appends constant, type, and variable definitions for the passed-in {@link Root} {@code _root}.<br/>
	 * @param _root - the diagram the declarations and definitions of are to be added
	 * @param _indent - the proper indentation as String
	 * @param _varNames - optionally the StringList of the variable names to be declared (my be null)
	 * @param _force - true means that the addition is forced even if option {@link #isInternalDeclarationAllowed()} is set 
	 */
	protected void appendDefinitions(Root _root, String _indent, StringList _varNames, boolean _force) {
		// TODO: structured constants must be defined after the type definitions (see PasGenerator)!
		int lastLine = code.count();
		// START KGU#375 2017-04-12: Enh. #388 - we want to add new information but this is not to have an impact on _root 
		//this.typeMap = _root.getTypeInfo();
		// START KGU#676 2019-03-30: Enh. #696 special pool in case of batch export
		//this.typeMap = new HashMap<String, TypeMapEntry>(_root.getTypeInfo());
		// START KGU#852 2020-04-22: Bugfix #854 - we must ensure topological order on export
		//this.typeMap = new HashMap<String, TypeMapEntry>(_root.getTypeInfo(routinePool));
		HashMap<String, TypeMapEntry> oldTypeMap = typeMap;
		this.typeMap = new LinkedHashMap<String, TypeMapEntry>(_root.getTypeInfo(routinePool));
		// END KGU#852 2020-04-22
		// END KGU#676 2019-03-30
		// END KGU#375 2017-04-12
		// END KGU#261/KGU#332 2017-01-16
		// START KGU#993 2021-10-03: Bugfix #993 constant parameters must not be defined here again!
		StringList parNames = _root.getParameterNames();
		// END KGU#993 2021-10-03
		// START KGU#375 2017-04-12: Enh. #388 special treatment of constants
		for (String constName: _root.constants.keySet()) {
			// START KGU#993 2021-10-03: Bugfix #993 constant parameters must not be defined here again!
			//appendDeclaration(_root, constName, _indent, _force || !this.isInternalDeclarationAllowed());
			if (!parNames.contains(constName)) {
				appendDeclaration(_root, constName, _indent, _force || !this.isInternalDeclarationAllowed());
			}
			// END KGU#993 2021-10-03
		}
		// END KGU#375 2017-04-12
		// START KGU#388 2017-09-26: Enh. #423 Place the necessary type definitions here
		if (_force || !this.isInternalDeclarationAllowed()) {
			this.generateTypeDefs(_root, _indent);
		}
		// END KGU#388 2017-09-26
		// START KGU 2015-11-30: List the variables to be declared (This will include merely declared variables!)
		for (int v = 0; v < _varNames.count(); v++) {
			// START KGU#261/#332 2017-01-26: Enh. #259/#335: Add actual declarations if possible
			//insertComment(varNames.get(v), _indent);
			String varName = _varNames.get(v);
			if (!_root.constants.containsKey(varName)) {
				appendDeclaration(_root, varName, _indent, _force || !this.isInternalDeclarationAllowed());
			}
			// END KGU#261/KGU#332 2017-01-16
		}
		// END KGU 2015-11-30
		// START KGU#376 2017-09-28: Enh. #423 - Specific care for merely declared (uninitialized) variables
		if (_root.isInclude()) {
			for (String id: this.typeMap.keySet()) {
				if (!id.startsWith(":") && !_varNames.contains(id)) {
					appendDeclaration(_root, id, _indent, _force || !this.isInternalDeclarationAllowed());
				}
			}
		}
		// END KGU#376 2017-09-28
		if (code.count() > lastLine) {
			addSepaLine();
		}
		// START KGU#852 2020-04-22: make sure the original type map is restored
		if (oldTypeMap != null) {
			typeMap = oldTypeMap;
		}
		// END KGU#852 2020-04-22
	}
	// END KGU#376 2017-09-26
	
	// START KGU#375 2017-04-12: Enh. #388 common preparation of constants and variables
	/**
	 * Appends a definition or declaration, respectively, for constant or variable {@code _name}
	 * to {@code this.code}. If {@code _name} represents a constant, which is checked via {@link Root}
	 * {@code _root}, then its definition is introduced.
	 * @param _root - the owning diagram
	 * @param _name - the identifier of the variable or constant
	 * @param _indent - the current indentation (as String)
	 * @param _fullDecl - whether the declaration is to be forced in full format
	 */
	protected void appendDeclaration(Root _root, String _name, String _indent, boolean _fullDecl)
	{
		// START KGU#376 2017-09-26: Enh. #389
		if (wasDefHandled(_root, _name, false)) {
			return;
		}
		// END KGU#376 2017-09-26
		TypeMapEntry typeInfo = typeMap.get(_name);
		StringList types = null;
		String constValue = _root.constants.get(_name);
		// START KGU#542 2019-11-17: Enh. #739 Don't add enumerator constant definitions
		if (constValue != null && constValue.startsWith(":")) {
			return;	// If the value string starts with a colon then it originates in an enumeration type.
		}
		// END KGU#542 2019-11-17
		// START KGU#1092 2023-10-17: Bugfix #1099 We shouldn't simply shift around an external call
		if (!_fullDecl && constValue != null && Function.isFunction(constValue, true)) {
			Function call = new Function(constValue);
			if (this.routinePool != null) {
				java.util.Vector<Root> callCandidates = routinePool.findRoutinesBySignature(
						call.getName(), call.paramCount(), _root, false);
				if (!callCandidates.isEmpty()) {
					// Better leave it for now...
					return;
				}
			}
		}
		// END KGU#1092 2023-10-17
		String transfConst = transformType("const", "");
		if (typeInfo != null) {
			// START KGU#388 2017-09-30: Enh. #423
			//types = getTransformedTypes(typeInfo, true);
			if (typeInfo.isRecord()) {
				types = StringList.getNew(this.transformRecordTypeRef(typeInfo.typeName, false));
			}
			// START KGU#542 2019-11-17: Enh. #739 - support for enum types
			else if (typeInfo.isEnum()) {
				types = StringList.getNew(this.transformEnumTypeRef(typeInfo.typeName));
			}
			// END KGU#542 2019-11-17
			else {
				types = getTransformedTypes(typeInfo, true);
			}
			// END KGU#388 2017-09-30
		}
		// START KGU#375 2017-04-12: Enh. #388: Might be an imported constant
		// FIXME (KGU 2017-09-30): It should be extremely unlikely now that there isn't a typeMap entry
		// START KGU#730 2019-09-25: workaround #752 - unfortunately a typeMap entry may be deficient (from subroutine)
		//else if (constValue != null) {
		if (constValue != null && (types == null || types.isEmpty())) {
		// END KGU#730 2019-09-25
			getLogger().log(Level.WARNING, "appendDeclaration({0}, {1}, ...): MISSING TYPE MAP ENTRY FOR THIS CONSTANT!",
					new Object[]{_root, _name});
			// This is likely to fail if constValue is an external function call
			String type = Element.identifyExprType(typeMap, constValue, false);
			if (!type.isEmpty()) {
				types = StringList.getNew(transformType(type, "int"));
				// We place a faked workaround entry
				typeMap.put(_name, new TypeMapEntry(type, null, null, _root, 0, true, false));
			}
			// START KGU#730 2019-09-25: Bugfix #752 - we must provide something lest the definition should go lost
			else if (_fullDecl) {
				types = StringList.getNew("???");
			}
			// END KGU#730 2019-09-25
		}
		// END KGU#375 2017-04-12
		// If the type is unambiguous and has no C-style declaration or may not be
		// declared between instructions then add the declaration here
		if (types != null && types.count() == 1 && 
				// FIXME: Replace isCStyleDeclared() with isDeclared()?
				// FIXME (#619) What we may want to know here is if there is an explicit declaration in some element
				//(typeInfo != null && !typeInfo.isCStyleDeclaredAt(null) || _fullDecl)) {			
				(typeInfo != null && !typeInfo.isDeclaredWithin(null) || _fullDecl)) {			
			String decl = types.get(0).trim();
			// START KGU#375 2017-04-12: Enh. #388 - types.get(0) doesn't contain anything more than e.g. "const"?
			if (decl.equals(transfConst) && constValue != null) {
				// The actual type spec is missing but we try to extract it from the value
				decl += " " + Element.identifyExprType(typeMap, constValue, false);
				decl = decl.trim();
			}
			// END KGU#375 2017-04-12
			if (decl.startsWith("@")) {
				decl = makeArrayDeclaration(decl, _name, typeInfo);
			}
			else {
				// START KGU#711 2019-09-30: Enh. #721 Javascript generator doesn't want type names
				//decl = decl + " " + _name;
				decl = this.composeTypeAndNameForDecl(decl, _name);
				// END KGU#711 2019-09-30
			}
			// START KGU#375 2017-04-12: Enh. #388 support for constant definitions
			if (_root.constants.containsKey(_name)) {
				if (!decl.contains(transfConst + " ")) {
					decl = transfConst + " " + decl;
				}
				if (constValue != null) {
					// START KGU#388 2017-09-26: Enh. #423
					//decl += " = " + transform(constValue);
					if (constValue.contains("{") && constValue.endsWith("}") && typeInfo != null && typeInfo.isRecord()) {
						constValue = transformRecordInit(constValue, typeInfo);
					}
					else {
						constValue = transform(constValue);
					}
					decl += " = " + constValue;
					// END KGU#388 2017-09-26
				}
			}
			// END KGU#375 2017-04-12
			// START KGU#388 2017-09-27: Enh. #423
			if (typeInfo != null && typeInfo.isNamed()) {
				this.generateTypeDef(_root, typeInfo.typeName, typeInfo, _indent, false);
			}
			// END KGU#388 2017-09-27
			// START KGU#424 2017-09-26: Ensure the declaration comment doesn't get lost
			appendDeclComment(_root, _indent, _name);
			// Just ensure that the declaration is registered
			setDefHandled(_root.getSignatureString(false, false), _name);
			// END KGU#424 2017-09-26
			if (decl.contains("???")) {
				// START #730 2019-11-12: Issue #752 don't comment it out, a missing declaration is a syntax error anyway
				//appendComment(decl + ";", _indent);
				addCode(decl + ";", _indent, false);
				// END KGU#730 2019-11-12
			}
			else {
				// START KGU#501 2018-02-22: Bugfix #517 In Java, C++, or C# we may need modifiers here
				//code.add(_indent + decl + ";");
				code.add(_indent + this.getModifiers(_root, _name) + decl + ";");
				// END KGU#501 2018-02-22
			}
		}
		// Add a comment if there is no type info or internal declaration is not allowed
		else if (types == null || _fullDecl){
			String typeName = "???";
			// START KGU#771 2019-11-24: Bugfix #783
			if (types != null) {
				typeName = types.get(0) + "???";
			}
			// END KGU#771 2019-11-24
			// START #730 2019-11-12: Issue #752 don't comment it out, a missing declaration is a syntax error anyway
			//appendComment(_name + ";", _indent);
			addCode(typeName + " " + _name + ";", _indent, false);
			// END KGU#730 2019-11-12
			// START KGU#424 2017-09-26: Ensure the declaration comment doesn't get lost
			setDefHandled(_root.getSignatureString(false, false), _name);
			// END KGU#424 2017-09-26
		}
		// END KGU#261/KGU#332 2017-01-16
	}
	// END KGU#375 2017-04-12

	// START KGU#711 2019-09-30: Enh. #721 - Allows subclasses a different composition
	/**
	 * Just composes given type designator {@code _type} and variable name {@code _name}
	 * for a declaration.
	 * @param _type - type designator
	 * @param _name - variable name
	 * @return the composed string (usually concatenated via blank)
	 */
	protected String composeTypeAndNameForDecl(String _type, String _name) {
		return _type + " " + _name;
	}

	// START KGU#501 2018-02-22: Bugfix #517
	/**
	 * Returns modifiers to be placed in front of the declaration of {@code _name} for the
	 * diagram {@code _root}.<br/>
	 * Method is intended to be overridden by sub-classes. If the result is non-empty then
	 * it ought to be padded at the end.
	 * @param _root - the originating {@link Root} of the entity {@code _name}
	 * @param _name - the identifier  
	 * @return a sequence of appropriate modifiers like "private static " or an empty string
	 */
	protected String getModifiers(Root _root, String _name) {
		return "";
	}
	// END KGU#501 2018-02-22

	// START KGU#388 2017-09-26: Enh. #423
	/**
	 * Generates code that decomposes a record initializer into separate component assignments if
	 * necessary or converts it into the appropriate target language.
	 * @param _lValue - the left side of the assignment (without modifiers!)
	 * @param _recordValue - the record initializer according to Structorizer syntax
	 * @param _indent - current indentation level (as String)
	 * @param _isDisabled - indicates whether the code is o be commented out
	 * @param _typeEntry - used to interpret a simplified record initializer (may be null)
	 */
	// START KGU#559 2018-07-20: Enh. #563
	//protected void generateRecordInit(String _lValue, String _recordValue, String _indent, boolean _isDisabled) {
	//	HashMap<String, String> comps = Instruction.splitRecordInitializer(_recordValue, null);
	protected void generateRecordInit(String _lValue, String _recordValue, String _indent, boolean _isDisabled, TypeMapEntry _typeEntry)
	{
		// START KGU#771 2019-11-24: Bugfix #783 In case of an unknown record type we should at least write the original content
		if (_typeEntry == null) {
			addCode(_lValue + " = " + _recordValue + ";\t" + this.commentSymbolLeft() + " FIXME: missing type information for struct! " + this.commentSymbolRight(),
					_indent, false);
			return;
		}
		// END KGU#771 2019-11-24
		HashMap<String, String> comps = Instruction.splitRecordInitializer(_recordValue, _typeEntry, false);
	// END KGU#559 2018-07-20
		// START KGU#1021 2021-12-05: Bugfix #1024 Instruction might be defective
		if (comps == null) {
			appendComment("ERROR: defective record initializer in diagram:", _indent);
			appendComment(_recordValue, _indent);
			return;
		}
		// END KGU#1021 2021-12-05
		for (Entry<String, String> comp: comps.entrySet()) {
			String compName = comp.getKey();
			String compVal = comp.getValue();
			if (!compName.startsWith("§") && compVal != null) {
				// START KGU#560 2018-07-21: Enh. #564 - on occasion of #563, we fix recursive initializers, too
				//addCode(transform(_lValue + "." + compName + " <- " + compVal) + ";", _indent, _isDisabled);
				generateAssignment(_lValue + "." + compName, compVal, _indent, _isDisabled);
				// END KGU#560 2018-07-21
			}
		}
	}
	// END KGU#388 2017-09-26

	// START KGU#560 2018-07-21: Bugfix #564 Array initializers have to be decomposed if not occurring in a declaration
	/**
	 * Generates code that decomposes possible initializers into a series of separate
	 * assignments if there is no compact translation, otherwise just adds appropriate
	 * transformed code.
	 * 
	 * @param _lValue - the left side of the assignment (without modifiers!)
	 * @param _expr - the expression in Structorizer syntax
	 * @param _indent - current indentation level (as String)
	 * @param _isDisabled - indicates whether the code is o be commented out
	 * 
	 * @see #transformOrGenerateArrayInit(String, StringList, String, boolean, String, boolean)
	 */
	protected void generateAssignment(String _lValue, String _expr, String _indent, boolean _isDisabled) {
		if (_expr.contains("{") && _expr.endsWith("}")) {
			StringList pureExprTokens = Element.splitLexically(_expr, true);
			pureExprTokens.removeAll(" ");
			int posBrace = pureExprTokens.indexOf("{");
			if (pureExprTokens.count() >= 3 && posBrace <= 1) {
				if (posBrace == 1 && Function.testIdentifier(pureExprTokens.get(0), false, null)) {
					// Record initializer
					String typeName = pureExprTokens.get(0);
					TypeMapEntry recType = this.typeMap.get(":"+typeName);
					this.generateRecordInit(_lValue, _expr, _indent, _isDisabled, recType);
					return;
				}
				else {
					// Array initializer
					StringList items = Element.splitExpressionList(pureExprTokens.subSequence(1, pureExprTokens.count()-1), ",", true);
					// START KGU#732 2019-10-03: Issue #755
					//this.generateArrayInit(_lValue, items.subSequence(0, items.count()-1), _indent, _isDisabled, null, false);
					_expr = this.transformOrGenerateArrayInit(_lValue, items.subSequence(0, items.count()-1), _indent, _isDisabled, null, false);
					if (_expr != null) {
						addCode(transform(_lValue) + " = " + _expr + ";", _indent, _isDisabled);
					}
					// END KGU#732 2019-10-03
					return;
				}
			}
		}
		// FIXME Array initializers must be handled recursively!
		addCode(transform(_lValue + " <- " + _expr) + ";", _indent, _isDisabled);
	}
	
	/**
	 * Either composes and returns a syntax-conform array initializer expression
	 * (if possible and allowed) or directly generates code that decomposes an array
	 * initializer into a series of element assignments if there is no compact
	 * translation. In the latter case {@code null} will be returned.
	 * 
	 * @param _lValue - the left side of the assignment (without modifiers!), i.e.
	 *    the array name
	 * @param _arrayItems - the {@link StringList} of element expressions to be
	 *    assigned (in index order)
	 * @param _indent - the current indentation level
	 * @param _isDisabled - whether the code is commented out
	 * @param _elemType - the {@link TypeMapEntry} of the element type is available
	 * @param _isDecl - if this is part of a declaration (i.e. a true initialization)
	 * @return either the transformed array initializer string, or {@code null}
	 *    (in the latter case the code was already generated)
	 */
	protected String transformOrGenerateArrayInit(String _lValue, StringList _arrayItems, String _indent, boolean _isDisabled, String _elemType, boolean _isDecl)
	{
		int nItems = _arrayItems.count();
		// START KGU#1061 2022-08-23: Issue #1068
		// If the last item is an empty string then drop it
		if (nItems > 0 && _arrayItems.get(nItems-1).isBlank()) {
			_arrayItems.remove(--nItems);
		}
		// END KGU#1061 2022-08-23
		if (_isDecl && this.isInternalDeclarationAllowed()) {
			// START KGU#732 2019-10-03: Bugfix #755 we have to care for recursive transformation
			//return this.transform("{" + _arrayItems.concatenate(", ") + "}");
			StringBuilder arrIni = new StringBuilder();
			// START KGU#1061 2022-08-23 Bugfix #1068 With a empty list the brace was missing
			//String sepa = "{";
			arrIni.append("{");
			// END KGU#1061 2022-08-23
			for (int i = 0; i < nItems; i++) {
				// START KGU#1061 2022-08-23 Bugfix #1068 With a empty list the brace was missing
				//arrIni.append(sepa);
				//arrIni.append(transform(_arrayItems.get(i)));
				//sepa = ", ";
				if (i > 0) {
					arrIni.append(", ");
				}
				arrIni.append(transform(_arrayItems.get(i)));
				// END KGU#1061 2022-08-23
			}
			arrIni.append('}');
			return arrIni.toString();
			// END KGU#732 2019-10-03
		}
		for (int i = 0; i < nItems; i++) {
			// initializers must be handled recursively!
			generateAssignment(_lValue + "[" + i + "]", _arrayItems.get(i), _indent, _isDisabled);
		}
		return null;
	}
	// END KGU#560 2018-07-21

	// START KGU#332 2017-01-30: Decomposition of generatePreamble() to ease sub-classing
	/**
	 * Returns the language-specific array declarator (element type description plus name plus
	 * index range specifiers) in the respective order.
	 * @param _canonType - canonicalized type string to extract the array element type.
	 * @param _varName - name of the array variable to be declared
	 * @param _typeInfo - the type map entry for the array variable (to retrieve index ranges)
	 * @return the transformed declarator
	 */
	protected String makeArrayDeclaration(String _canonType, String _varName, TypeMapEntry _typeInfo)
	{
		int nLevels = _canonType.lastIndexOf('@')+1;
		// START KGU#388 2023-10-17: Enh. #423 Care for recursivity
		//String _elementType = (_canonType.substring(nLevels) + " " + _varName).trim();
		String _elementType = _canonType.substring(nLevels);
		String typeStr = (this.transformTypeWithLookup(_elementType, _elementType) + " " + _varName).trim();
		// END KGU#388 2023-10-17
		for (int i = 0; i < nLevels; i++) {
			int maxIndex = _typeInfo.getMaxIndex(i);
			// START KGU#854 2020-04-22: Enh. #855
			if (maxIndex < 0) {
				maxIndex = this.optionDefaultArraySize() - 1;
			}
			// END KGU#854 2020-04-22
			typeStr += "[" + (maxIndex >= 0 ? Integer.toString(maxIndex+1) : (i == 0 ? "" : "/*???*/") ) + "]";
		}
		return typeStr;
	}
	
	protected void generateIOComment(Root _root, String _indent)
	{
		// START KGU#236 2016-08-10: Issue #227 - don't express this information if not needed
		if (this.hasInput(_root)) {
		// END KGU#236 2016-08-10
			addSepaLine();
			appendComment("TODO:", _indent);
			appendComment(
					"For any input using the 'scanf' function you need to fill the first argument.",
					_indent);
			appendComment(
					"http://en.wikipedia.org/wiki/Scanf#Format_string_specifications",
					_indent);
		// START KGU#236 2016-08-10: Issue #227
		}
		if (this.hasOutput(_root)) {
		// END KGU#236 2016-08-10
		addSepaLine();
		appendComment("TODO:", _indent);
		appendComment(
				"For any output using the 'printf' function you need to fill the first argument:",
				_indent);
		appendComment(
				"http://en.wikipedia.org/wiki/Printf#printf_format_placeholders",
				_indent);
		// START KGU#236 2016-08-10: Issue #227
		}
		// END KGU#236 2016-08-10	
	}
	// START KGU#332 2017-01-30
	
	// START KGU#815/KGU#824/KGU#834 2020-03-26: Enh. #828, bugfix #826
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateBody(lu.fisch.structorizer.elements.Root, java.lang.String)
	 */
	@Override
	protected boolean generateBody(Root _root, String _indent)
	{
		String indentBody = _indent;
		if (topLevel && this.isLibraryModule() && _root.isInclude()) {
			// This is the initialization code for the library
			addCode("static bool initDone_" + this.getModuleName() + " = false;", _indent, false);
			String cond = "if (!initDone_"  + this.getModuleName() + ")";
			if (!this.optionBlockBraceNextLine()) {
				addCode(cond + " {", _indent, false);
			}
			else {
				addCode(cond, _indent, false);
				addCode("{", _indent, false);
			}
			indentBody += this.getIndent();			
		}
		// END KGU#815/KGU#824 2020-03-20
		
		// START KGU#376 2017-09-26: Enh. #389 - add the initialization code of the includables
		appendGlobalInitialisations(_root, indentBody);
		// END KGU#376 2017-09-26
		
		boolean done = super.generateBody(_root, indentBody);
		
		if (!indentBody.equals(_indent)) {
			addCode("initDone_" + this.pureFilename + " = true;", indentBody, false);
			addCode("}", _indent, false);
		}
		return done;
	}
	// START KGU#815/KGU#824/KGU#834 2020-03-26
	
	/**
	 * Creates the appropriate code for returning a required result and adds it
	 * (after the algorithm code of the body) to this.code)
	 * @param _root - the diagram root element
	 * @param _indent - the current indentation string
	 * @param alwaysReturns - whether all paths of the body already force a return
	 * @param varNames - names of all assigned variables
	 */
	@Override
	protected String generateResult(Root _root, String _indent, boolean alwaysReturns, StringList varNames)
	{
		if (_root.isProgram() && !alwaysReturns)
		{
			addSepaLine();
			code.add(_indent + "return 0;");
		}
		else if (_root.isSubroutine() &&
				(returns || _root.getResultType() != null || isFunctionNameSet || isResultSet) && !alwaysReturns)
		{
			String result = "0";
			if (isFunctionNameSet)
			{
				result = _root.getMethodName();
			}
			else if (isResultSet)
			{
				int vx = varNames.indexOf("result", false);
				result = varNames.get(vx);
			}
			// START KGU#1084 2023-10-04: Bugfix #1093 Don't invent an undue return statement here
			else {
				return _indent;
			}
			// END KGU#1084 2023-10-24
			addSepaLine();
			code.add(_indent + "return " + result + ";");
		}
		return _indent;
	}
	
	/**
	 * Method is to finish up after the text additions of the diagram, i.e. to close open blocks etc. 
	 * @param _root 
	 * @param _indent
	 */
	@Override
	protected void generateFooter(Root _root, String _indent)
	{
		code.add(_indent + "}");
		// START KGU#815/KGU#824 2020-03-20: Enh. #828, bugfix #836
		if (topLevel) {
			libraryInsertionLine = code.count();
		}
		// END KGU#815/KGU#824 2020-03-20
	}

	// START KGU#815 2020-03-20: Enh. #828 - Expanded support for code export of arrangement groups
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateModule(java.util.Vector, java.util.TreeMap, boolean, java.util.Vector, java.lang.String)
	 */
	@Override
	protected boolean generateModule(Vector<Root> _roots, TreeMap<Root, SubTopoSortEntry> _dependencyTree, boolean _batchMode, Vector<Root> _entryPoints, String _libName) {
		// We must cache this library name for the global initializations
		this.libModuleName = _libName;
		boolean needsFileAPI = super.generateModule(_roots, _dependencyTree, _batchMode, _entryPoints, _libName);
		this.libModuleName = null;
		return needsFileAPI;
	}
	// END KGU#815 2020-03-20
	
	// START KGU#311/KGU#815/KGU#824 2020-03-23: Enh. #314, #828, issue #836
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#copyFileAPIResources(java.lang.String)
	 */
	@Override
	protected boolean copyFileAPIResources(String _filePath)
	{
		/* If importedLibRoots is not null then we had a multi-module export,
		 * this function will only be called if at least one of the modules required
		 * the file API, so all requiring modules will have included "FileAPI.h".
		 * Now we simply have to make sure it gets provided.
		 */
		boolean isDone = true;	// By default nothing is to be done and that is okay
		if (this.importedLibRoots != null) {
			isDone = copyFileAPIResource("c", null, _filePath);
			isDone = copyFileAPIResource("h", null, _filePath) && isDone;
		}
		return isDone;
	}
	// END KGU#311/KGU#815/KGU#824 2020-03-23
}

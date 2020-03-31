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

package lu.fisch.structorizer.generators;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    This class generates Pascal code.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author              Date            Description
 *      ------              ----            -----------
 *      Bob Fisch           2007-12-27      First Issue
 *      Bob Fisch           2008-04-12      Added "Fields" section for generator to be used as plugin
 *      Bob Fisch           2008-11-17      Added Freepascal extensions
 *      Bob Fisch           2009-08-17      Bugfixes (see comment)
 *      Bob Fisch           2011-11-07      Fixed an issue while doing replacements
 *      Dirk Wilhelmi       2012-10-11      Added comments export
 *      Kay Gürtzig         2014-11-10      Conversion of C-like logical operators
 *      Kay Gürtzig         2014-11-16      Conversion of C-like comparison operator, comment export
 *      Kay Gürtzig         2014-12-02      Additional replacement of long assignment operator "<--" by "<-"
 *      Kay Gürtzig         2015-10-18      Comment generation and indentation revised
 *      Kay Gürtzig         2015-11-30      Enh. #23: Jump generation modified, KGU#47: Parallel generation
 *                                          added, Root generation fundamentally redesigned (decomposed)  
 *      Bob Fisch           2015-12-10      Bugfix #50 --> grep & export function parameter types
 *      Kay Gürtzig         2015-12-20      Bugfix #22 (KGU#74): Correct return mechanisms even with
 *                                          return instructions not placed in Jump elements
 *      Kay Gürtzig         2015-12-21      Bugfix #41/#68/#69 (= KG#93)
 *      Kay Gürtzig         2016-01-14      Enh. #84: array initialisation expressions decomposed (= KG#100)
 *      Kay Gürtzig         2016-01-17      Bugfix #61/#112 - handling of type names in assignments (KGU#109/KGU#141)
 *                                          KGU#142: Bugfix for enh. #23 - empty Jumps weren't translated
 *      Kay Gürtzig         2016-03-16      Enh. #84: Minimum support for FOR-IN loops (KGU#61) 
 *      Kay Gürtzig         2016-03-31      Enh. #144 - content conversion may be switched off
 *      Kay Gürtzig         2016-04-30      Bugfix #181 - delimiters of string literals weren't converted (KGU#190)
 *      Kay Gürtzig         2016-05-05      Bugfix #51 - empty writeln instruction must not have parentheses 
 *      Kay Gürtzig         2016-07-20      Enh. #160 - optional export of called subroutines implemented
 *      Kay Gürtzig         2016-08-12      Enh. #231: Additions for Analyser checks 18 and 19 (variable name collisions)
 *      Kay Gürtzig         2016-09-25      Enh. #253: CodeParser.keywordMap refactoring done 
 *      Kay Gürtzig         2016-10-14      Enh. #270: Handling of disabled elements (code.add(...) --> addCode(..))
 *      Kay Gürtzig         2016-10-15      Enh. #271: Support for input instructions with prompt
 *      Kay Gürtzig         2016-10-16      Enh. #274: Colour info for Turtleizer procedures added
 *      Kay Gürtzig         2016-12-26      Enh. #314: Makeshift additions to support the File API
 *      Kay Gürtzig         2017-01-30      Enh. #259/#335: Type retrieval and improved declaration support
 *                                          Bugfix #337: Defective export of 2d assignments like a[i] <- {foo, bar} mended
 *      Kay Gürtzig         2017-01-31      Enh. #113: Array parameter transformation
 *      Kay Gürtzig         2017-02-01      Enh. #84: indexBase constant mechanism for array initializers disabled
 *      Kay Gürtzig         2017-02-27      Enh. #346: Insertion mechanism for user-specific include directives
 *      Kay Gürtzig         2017-03-15      Bugfix #382: FOR-IN loop value list items hadn't been transformed
 *      Kay Gürtzig         2017-04-12      Enh. #388: Support for export of constant definitions added 
 *      Kay Gürtzig         2017-05-16      Enh. #372: Export of copyright information
 *      Kay Gürtzig         2017-09-19      Enh. #423: Export of record types
 *      Kay Gürtzig         2017-09-21      Enh. #388, #389: Export strategy for Includables and structured constants
 *      Kay Gürtzig         2017-09-25      Enh. #388, #423: Positioning of declaration comments revised
 *      Kay Gürtzig         2017-11-02      Issue #447: Line continuation in Case elements supported
 *      Kay Gürtzig         2018-03-13      Bugfix #520,#521: Mode suppressTransform enforced for declarations
 *      Kay Gürtzig         2018-07-20      Enh. #563: support for simplified record initializers
 *      Kay Gürtzig         2018-07-22      Bugfix #564: defects with nested record/array initializers mended
 *      Kay Gürtzig         2018-10-05      Bugfix #619: Undue declaration of function result variable dropped
 *      Kay Gürtzig         2019-03-20      Enh. #56: Export of Try elements and of Jump elements with throw flavour
 *      Kay Gürtzig         2019-03-30      Issue #696: Type retrieval had to consider an alternative pool
 *      Kay Gürtzig         2019-10-01      Issue #754: Instruction with "return" and UNIT name for code preview fixed
 *      Kay Gürtzig         2019-11-08      Bugfix #772: NullPointerExceptions in code preview, due to late typeMap init
 *      Kay Gürtzig         2019-11-11      Bugfix #773: Mere declarations at top level exported, incomplete
 *                                          declarations (defective type) no longer as comment but with FIXME! marker
 *      Kay Gürtzig         2019-11-13      Bugfix #776: Mere global declarations (from includables must not be repeated
 *                                          as local declarations in subroutines where the variables get assigned
 *      Kay Gürtzig         2019-11-21      Enh. #739: enum type inference for FOR-IN loops
 *      Kay Gürtzig         2019-11-29      Bugfix 787: multiple global type definitions (as many times as includables involved)
 *      Kay Gürtzig         2019-02-15      Issue #814: Unidentified parameter type marker changed: {type?} --> ???
 *      Kay Gürtzig         2020-03-17/30   Enh. #828: Modification in generatePreamble(), insertion lines
 *                                          corrected
 *
 ******************************************************************************************************
 *
 *      Comments:
 *      
 *      2020-03-22 - File API Issues #314 + #828 + #836 (Kay Gürtzig)
 *      - The introduction of arrangement group export with enh. #828 has further complicated the File API
 *        handling (enh. #314), where code preview and batch code export have also to be considered.
 *        Basically there are three different approaches:
 *        1. Program export (single file): type definitions (section 1) and procedure definitions (section 2)
 *        2. Subroutine export (single UNIT): both types and procedures may be copied into the IMPLEMENTATION
 *        3. Library UNIT export: If only library routines need the file API (internally) then approach 2
 *           could be adopted, but if dependent modules require the fileAPI as well then it will be more
 *           sensible to copy the entire fileAPI resource (UNIT) and reference it with a USING clause in the
 *           both the library and the requiring library client modules (won't cause harm to have redundant
 *           USES clauses). We ensure that the File API is copied by providing a conditioned implementation
 *           of copyFileAPIResources
 *      
 *      2015-12-21 - Bugfix #41/#68/#69 (Kay Gürtzig)
 *      - Operator replacement had induced unwanted padding and string literal modifications
 *      - new subclassable method transformTokens() for all token-based replacements 
 *      
 *      2015-11-01 - Code revision / enhancements
 *      - Most of the transform stuff delegated to Element and Generator (KGU#18/KGU23)
 *      - Enhancement #10 (KGU#3): FOR loops now provide more reliable loop parameters detection  
 *
 *      2015-10-18
 *      - Indentation increment with +_indent.substring(0,1) worked only for single-character indentation units
 *      - Interface of comment insertion methods modified
 *
 *      2014-11-16 - Bugfix / Enhancement
 *      - Conversion of C-style unequality operator had to be added
 *      - Comments are now exported, too
 *       
 *      2014-11-10 - Enhancement
 *      - Conversion of C-style logical operators to the Pascal-like ones added
 *      - assignment operator conversion now preserves or ensures surrounding spaces
 *
 *      2009-08-17 - Bugfixes
 *      - added automatic brackets for "while", "switch", "repeat" & "if"
 *
 ******************************************************************************************************///

import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.generators.Generator.TryCatchSupportLevel;


public class PasGenerator extends Generator 
{
	
	/** The method name of root */
	protected String procName = "";
	
	/************ Fields ***********************/
    @Override
    protected String getDialogTitle()
    {
            return "Export Pascal Code ...";
    }

    @Override
    protected String getFileDescription()
    {
            return "Pascal / Delphi Source Code";
    }

    @Override
    protected String getIndent()
    {
            return "  ";
    }

    @Override
    protected String[] getFileExtensions()
    {
            String[] exts = {"pas", "dpr", "pp", "lpr"};
            return exts;
    }

    // START KGU 2015-10-18: New pseudo field
    @Override
    protected String commentSymbolLeft()
    {
    	return "{";
    }

    @Override
    protected String commentSymbolRight()
    {
    	return "}";
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

	// START KGU#371 2019-03-07: Enh. #385
	/**
	 * @return The level of subroutine overloading support in the target language
	 */
	@Override
	protected OverloadingLevel getOverloadingLevel() {
		// This might be disputable, but Delphi, FreePascal etc. support default arguments
		return OverloadingLevel.OL_DEFAULT_ARGUMENTS;
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
		return TryCatchSupportLevel.TC_TRY_CATCH_FINALLY;
	}
	// END KGU#686 2019-03-18

//	// START KGU 2016-08-12: Enh. #231 - information for analyser - obsolete since 3.27
//    private static final String[] reservedWords = new String[]{
//		"and", "array", "begin",
//		"case", "const", "div", "do", "downto",
//		"else", "end", "file", "for", "function", "goto",
//		"if", "in", "label", "mod", "nil", "not", "of", "or",
//		"packed", "procedure", "program", "record", "repeat",
//		"set", "shl", "shr", "then", "to", "type",
//		"until", "var", "while", "with"
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
		return "uses %%;";
	}
	// END KGU#351 2017-02-26

	// START KGU#311 2016-12-26: Enh.#314 File API support
	/** Names of the three FileAPI functions to open a file for a) reading, b) writing, c) appending */
	private static final String[] openAPINames = {"fileOpen", "fileCreate", "fileAppend"};
	/** Names of the three Pascal procedures to open a file for a) reading, b) writing, c) appending */
	private static final String[] openProcNames = {"open", "rewrite", "append"};
	/** Gathers all file variable names (triggered by the detection of a file open routine call) found in the code */
	private StringList fileVarNames = new StringList();
	// END KGU#311 2016-12-26
	// START KGU#311/KGU#828 2020-03-22: Enh. #314, #828
	private static final String FILE_API_UNIT_NAME = "StructorizerFileAPI";
	// END KGU#311/KGU#828 2020-03-22
	
	/************ Code Generation **************/
	
	// START KGU#559/KGU#560 2018-07-22: Enh. #563, bugfix #564
	private Map<String,TypeMapEntry> typeMap;
	// END KGU#559/KGU#560 2018-07-22

	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getInputReplacer(boolean)
	 */
	@Override
	// START KGU#281 2016-10-15: Enh. #271
	//protected String getInputReplacer()
	//{
	//	return "readln($1)";
	//}
	protected String getInputReplacer(boolean withPrompt)
	{
		if (withPrompt) {
			return "write($1); readln($2)";
		}
		return "readln($1)";
	}
	// END KGU#281 2016-10-15

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getOutputReplacer()
	 */
	@Override
	protected String getOutputReplacer()
	{
		return "writeln($1)";
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
				this.appendBlockComment(_sl, _indent, this.commentSymbolLeft(), "* ", this.commentSymbolRight());
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
		return this.insertBlockComment(_sl, _indent, this.commentSymbolLeft(), "* ", this.commentSymbolRight(), _atLine);
	}
	// END KGU#815 2020-03-26

	// START KGU#815/KGU#824 2020-03-19: Enh. #828, bugfix #836
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#insertPrototype(lu.fisch.structorizer.elements.Root, java.lang.String, boolean, int)
	 */
	@Override
	protected int insertPrototype(Root _root, String _indent, boolean _withComment, int _atLine)
	{
		String signature = _root.getMethodName();
		StringList paramNames = new StringList();
		StringList paramTypes = new StringList();
		_root.collectParameters(paramNames, paramTypes, null);
		String resultType = _root.getResultType();
		String pr = "program";
		if (!_root.isProgram()) {
			pr = "function";
			// Compose the function header
			signature += "(";
			//insertComment("TODO: declare the parameters and specify the result type!", _indent);
			// START KGU#371 2019-03-08: Enh. #385
			int minArgs = _root.getMinParameterCount();
			StringList argDefaults = _root.getParameterDefaults();
			// END KGU#371 2019-03-08
			for (int p = 0; p < paramNames.count(); p++) {
				signature += ((p > 0) ? "; " : "");
				// START KGU#800 2020-02-15: Type name surrogate unified to ???
				//signature += (_paramNames.get(p) + ": " + transformType(_paramTypes.get(p), "{type?}")).trim();
				signature += (paramNames.get(p) + ": " + transformType(paramTypes.get(p), "???")).trim();
				// END KGU#800 2020-02-15
				// START KGU#371 2019-03-08: Enh. #385
				if (p >= minArgs) {
					signature += " = " + transform(argDefaults.get(p));
				}
				// END KGU#371 2019-03-08
			}
			signature += ")";
			if (resultType != null || this.returns || this.isResultSet || this.isFunctionNameSet)
			{
				resultType = transformType(_root.getResultType(), "Integer");
				signature += ": " + resultType;
			}
			else 
			{
				pr = "procedure";
			}
		}
		signature = pr + " " + signature + ";";
		int lines = 0;
		if (_withComment) {
			insertCode("", _atLine);
			lines += 1 + insertComment(_root, _indent, _atLine + 1);
		}
		insertCode(signature, _atLine + lines);
		return lines + 1;
	}
	// END KGU #815/KGU#824 2020-03-19

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
			if (_type.equalsIgnoreCase("long")) _type = "Longint";
			else if (_type.equalsIgnoreCase("int")) _type = "Longint";
			else if (_type.equalsIgnoreCase("integer")) _type = "Longint";
			else if (_type.equalsIgnoreCase("float")) _type = "Single";
			else if (_type.equalsIgnoreCase("real")) _type = "Single";
			else if (_type.equalsIgnoreCase("double")) _type = "Double";
			else if (_type.equalsIgnoreCase("longreal")) _type = "Double";
			else if (_type.equalsIgnoreCase("unsigned short")) _type = "Word";
			else if (_type.equalsIgnoreCase("short")) _type = "Smallint";
			else if (_type.equalsIgnoreCase("shortint")) _type = "Smallint";
			else if (_type.equalsIgnoreCase("unsigned int")) _type = "Cardinal";
			else if (_type.equalsIgnoreCase("unsigned long")) _type = "Cardinal";
			else if (_type.equalsIgnoreCase("bool")) _type = "Boolean";
			// START KGU 2017-04-12: cope with C strings here
			else if (_type.equalsIgnoreCase("char*")) _type = "String";
			else if (_type.equalsIgnoreCase("char *")) _type = "String";
			// END KGU 2017-04-12
			// START KGU#140 2017-01-31: Enh. #113
			//else if (_type.toLowerCase().startsWith("array")) {
			//	String lower = _type.toLowerCase();
			//	String elType = lower.replaceAll("^array.*?of (.*)", "$1");
			//	if (!elType.trim().isEmpty()) {
			//		_type = lower.replaceAll("^(array.*?of ).*", "$1") + transformType(elType, elType);
			//	}
			//}
			else if (!_type.equalsIgnoreCase("array")) {
				// "array" without element type is a pathologic case that might drive us into stack overflow!
				_type = transformArrayDeclaration(_type);
			}
			// END KGU#140 2017-01-31
			// To be continued if required...
		}
		return _type;
	}
	// END KGU#16 2015-11-30	

	// START KGU#140 2017-01-31: Enh. #113: Advanced array transformation
	protected String transformArrayDeclaration(String _typeDescr)
	{
		if (_typeDescr.toLowerCase().startsWith("array") || _typeDescr.endsWith("]")) {
			// TypeMapEntries are really good at analysing array definitions
			TypeMapEntry typeInfo = new TypeMapEntry(_typeDescr, null, null, null, 0, false, true);
			_typeDescr = transformTypeFromEntry(typeInfo, null);
		}
		return _typeDescr;
	}

	/**
	 * Creates a type description suited for Pascal code from the given TypeMapEntry {@code typeInfo}
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
			_typeDescr += "array ";
			int minIndex = typeInfo.getMinIndex(i);
			int maxIndex = typeInfo.getMaxIndex(i);
			if (maxIndex >= minIndex) {
				_typeDescr += "[" + minIndex + ".." + maxIndex + "] ";
			}
			_typeDescr += "of ";
		}
		_typeDescr += elType;
		return _typeDescr;
	}
	// END KGU#140 2017-01-31
	
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

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformTokens(lu.fisch.utils.StringList)
	 */
	@Override
	protected String transformTokens(StringList tokens)
	{
		// First get rid of superfluous spaces
		int pos = -1;
		StringList doubleBlank = StringList.explode(" \n ", "\n");
		while ((pos = tokens.indexOf(doubleBlank, 0, true)) >= 0)
		{
			tokens.delete(pos);	// Get rid of one of the blanks
		}
		// On inserting operator keywords we better make sure them being padded
		// (lest neighbouring identifiers be glued to them on concatenating)
		// The correct way would of course be to add blank tokens where needed
		// but this seemed too expensive here.
        tokens.replaceAll("==", "=");
        tokens.replaceAll("!=","<>");
        tokens.replaceAll("%"," mod ");
        tokens.replaceAll("&&"," and");
        tokens.replaceAll("||"," or ");
        tokens.replaceAll("!"," not ");
        tokens.replaceAll("&"," and");
        tokens.replaceAll("|"," or ");
        tokens.replaceAll("~"," not ");
        tokens.replaceAll("<<"," shl ");
        tokens.replaceAll(">>"," shr ");
		tokens.replaceAll("<-", ":=");
		// START KGU#311 2016-12-26: Enh. #314 - Support for File API
		//if (this.usesFileAPI) {	// KGU#832 2020-03-23: Bugfix #840 transform even for disabled elements
			tokens.replaceAll("fileWrite", "write");
			tokens.replaceAll("fileWriteLine", "writeln");
			tokens.replaceAll("fileEOF", "eof");
			tokens.replaceAll("fileClose", "closeFile");
		//}
		// END KGU#311 2016-12-26
		// START KGU#190 2016-04-30: Bugfix #181 - String delimiters must be converted to '
		for (int i = 0; i < tokens.count(); i++)
		{
			String token = tokens.get(i);
			if (token.length() > 1 && token.startsWith("\"") && token.endsWith("\""))
			{
				// Seems to be a string, hence modify it
				// Replace all internal apostrophes by double apostrophes
				token = token.replace("'", "''");
				// Now replace the outer delimiters
				tokens.set(i, "'" + token.substring(1, token.length()-1) + "'");
			}
		}
		// END KGU#190 2016-04-30
		String result = tokens.concatenate();
		// We now shrink superfluous padding - this may affect string literals, though!
		result = result.replace("  ", " ");
		result = result.replace("  ", " ");	// twice to catch odd-numbered space sequences, too
		return result;
	}
	// END KGU#93 2015-12-21
	// END KGU#18/KGU#23 2015-11-01
    

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transform(java.lang.String)
	 */
	@Override
	protected String transform(String _input)
	{
		String transline = super.transform(_input);

		// START KGU#109/KGU#141 2016-01-16: Bugfix #61,#112 - suppress type specifications
		// (This must work both in Instruction and Call elements)
		int asgnPos = transline.indexOf(":=");
		if (asgnPos > 0)
		{
			String varName = transline.substring(0, asgnPos).trim();
			String expr = transline.substring(asgnPos + ":=".length()).trim();
			String[] typeNameIndex = this.lValueToTypeNameIndexComp(varName);
			varName = typeNameIndex[1];
			String index = typeNameIndex[2];
			if (!index.isEmpty())
			{
				varName += "["+index+"]";
			}
			// START KGU#388 2017-09-27: Enh. #423
			varName += typeNameIndex[3];
			// END KGU#388 2017-09-27: Enh. #423
			transline = varName + " := " + expr;
		}
		// END KGU#109/KGU#141 2016-01-16
		
		// START KGU#195 2016-05-05: Bugfix #51 (handling of empty output instructions)
		if (transline.startsWith("writeln()"))
		{
			transline = "writeln" + transline.substring("writeln()".length());
		}
		// END KGU#195 2016-05-05
		
		return transline.trim(); 
    }
	
	// START KGU#61 2016-03-23: New for enh. #84 (FOREACH loop support)
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
		if (posDecl < 0 && _category.equals("const")) {
			seekIndent = "";
			while (posDecl < 0 && seekIndent.length() < _maxIndent)
			{
				posDecl = code.indexOf(seekIndent + "var");
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

	@Override
	protected void generateCode(Instruction _inst, String _indent)
	{
		if (!appendAsComment(_inst, _indent)) {
			
			boolean isDisabled = _inst.isDisabled();

			// START KGU#424 2017-09-25: Avoid the comment here if the element contains mere declarations
			//insertComment(_inst, _indent);
			boolean commentInserted = false;
			// END KGU#424 2017-09-25

			String preReturn = CodeParser.getKeywordOrDefault("preReturn", "return");
			Pattern preReturnMatch = Pattern.compile(getKeywordPattern(preReturn)+"([\\W].*|$)");
			StringList lines = _inst.getUnbrokenText();
			// START KGU#424 2017-09-25: Put the comment if the element doesn't contain anything else
			if (lines.getLongString().trim().isEmpty()) {
				appendComment(_inst, _indent);
				commentInserted = true;
			}
			// END KGU#424 2017-09-25
			for (int i=0; i<lines.count(); i++)
			{
				// START KGU#74 2015-12-20: Bug #22 There might be a return outside of a Jump element, handle it!
				//code.add(_indent+transform(_inst.getText().get(i))+";");
				String line = lines.get(i).trim();
				if (preReturnMatch.matcher(line).matches())
				{
					String argument = line.substring(preReturn.length()).trim();
					if (!argument.isEmpty())
					{
						// START KGU#424 2017-09-25: Put the comment on substantial content
						if (!commentInserted) {
							appendComment(_inst, _indent);
							commentInserted = true;
						}
						// END KGU#424 2017-09-25
						addCode(this.procName + " := " + transform(argument) + ";",
								_indent, isDisabled); 
					}
					Subqueue sq = (_inst.parent == null) ? null : (Subqueue)_inst.parent;
					if (sq == null || !(sq.parent instanceof Root) || sq.getIndexOf(_inst) != sq.getSize()-1 ||
							i+1 < lines.count())
					{
						// START KGU#424 2017-09-25: Put the comment on substantial content
						if (!commentInserted) {
							appendComment(_inst, _indent);
							commentInserted = true;
						}
						// END KGU#424 2017-09-25
						addCode("exit;", _indent, isDisabled);
					}
					// START KGU#737 2019-10-01: Issue #754
					continue;	// There is nothing more t be done about this line!
					// END KGU#737 2019-10-01
				}
				// START KGU#375 2107-09-21: Enh. #388 constant definitions must not be generated here (preamble stuff)
				//else	// no return
				// START KGU#504 2018-03-13: Bugfix #520, #521 - consider transformation suppression
				//if (!Instruction.isTypeDefinition(line, null) && !line.toLowerCase().startsWith("const "))
				if (this.suppressTransformation || !Instruction.isTypeDefinition(line, null) && !line.toLowerCase().startsWith("const "))
				// END KGU#504 2018-03-13
				// END KGU#375 2017-09-21
				{
					// START KGU#100 2016-01-14: Enh. #84 - resolve array initialisation
					// The crux is: we don't know the index range!
					// So we'll invent an index base variable easy to be modified in code
					//code.add(_indent + transform(line) + ";");
					String transline = transform(line);
					int asgnPos = transline.indexOf(":=");
					boolean isArrayOrRecordInit = false;
					if (asgnPos > 0 && transline.contains("{") && transline.contains("}"))
					{
						String varName = transline.substring(0, asgnPos).trim();
						String expr = transline.substring(asgnPos+2).trim();
						int posBrace = expr.indexOf("{");
						// START KGU#424 2017-09-25: Put the comment on substantial content
						if (!commentInserted) {
							appendComment(_inst, _indent);
							commentInserted = true;
						}
						// END KGU#424 2017-09-25
						// START KGU#504 2018-03-13 A: Bugfix #520, #521
						if (!this.suppressTransformation) {
						// END KGU#504 2018-03-13 A
							String potTypeName = expr.substring(0,  posBrace);
							isArrayOrRecordInit = posBrace == 0 && expr.endsWith("}");	// only true at this moment on array init
							if (isArrayOrRecordInit)
							{
								// START KGU#560 2018-07-22: Bugfix #564 - with C-like declarations, the index range must be wiped off
								if (varName.contains("[") && Instruction.isDeclaration(line)) {
									varName = varName.substring(0, varName.indexOf('['));
								}
								// END KGU#560 2018-07-22
								generateArrayInit(varName, expr, _indent, null, isDisabled);
							}
							else if (posBrace > 0 && Function.testIdentifier(potTypeName, ".") && expr.endsWith("}"))
							{
								// START KGU#559 2018-07-20: Enh. #563 - smarter record initializer interpretation
								//generateRecordInit(varName, expr, _indent, false, isDisabled, null);
								TypeMapEntry recType = typeMap.get(":" + potTypeName);
								generateRecordInit(varName, expr, _indent, false, isDisabled, recType);
								// END KGU#559 2018-07-20
								isArrayOrRecordInit = true;
							}
						// START KGU#504 2018-03-13 B: Bugfix #520, #521
						}
						// END KGU#504 2018-03-13 B
					}
					if (!isArrayOrRecordInit)
					{
						// START KGU#311 2016-12-26: Enh. #314 - File API support
						// If FileAPI is used then this isn't supposed to be suppressed on export
						// START KGU#832 2020-03-22: Bugfix #840 even on disabled File API elements transform the text
						//if (this.usesFileAPI && asgnPos > 0) {
						if (asgnPos > 0) {
						// END KGU#832 202-03-23
							boolean doneFileAPI = false;
							String expr = transline.substring(asgnPos+1).trim();
							String var = transline.substring(0, asgnPos).trim();
							int posBracket = var.indexOf("[");
							for (int k = 0; !doneFileAPI && k < openAPINames.length; k++) {
								int posOpen = expr.indexOf(openAPINames[k] + "(");
								if (posOpen >= 0) {
									if (posBracket > 0) {
										fileVarNames.addIfNew(var.substring(0, posBracket));
									}
									else {
										fileVarNames.addIfNew(var);
									}
									StringList args = Element.splitExpressionList(expr.substring(posOpen + openAPINames[k].length()+1), ",");
									transline = "assign(" + var + ", " + args.get(0) + "); " + openProcNames[k] + "(" + var + ")";
									doneFileAPI = true;
								}
							}
						}
						// END KGU#311 2016-12-26
						// START KGU#277/KGU#284 2016-10-13/16: Enh. #270 + Enh. #274
						//code.add(_indent + transline + ";");
						transline += ";";
						if (Instruction.isTurtleizerMove(line)) {
							transline += " " + this.commentSymbolLeft() + " color = " + _inst.getHexColor() + " " + this.commentSymbolRight();
						}
						// START KGU#261 2017-01-26: Enh. #259/#335
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
							// START KGU#424 2017-09-25: Put the comment on substantial content
							if (!commentInserted) {
								appendComment(_inst, _indent);
								commentInserted = true;
							}
							// END KGU#424 2017-09-25
							addCode(transline, _indent, isDisabled);
						}
						// END KGU#261 2017-01-26
						// END KGU#277/KGU#284 2016-10-13
					}
					// END KGU#100 2016-01-14
				}
				// END KGU#74 2015-12-20
			}

		}
    }

	/**
	 * Appends the code for an array initialisation of variable {@code _varName} from
	 * the pre-transformed expression {@code _expr}.
	 * @param _varName - name of the variable to be initialized
	 * @param _expr - transformed initializer
	 * @param _indent - current indentation string
	 * @param _constType - in case of a constant the array type description (otherwise null)
	 * @param _isDisabled - whether the source element is disabled (means to comment out the code)
	 */
	private void generateArrayInit(String _varName, String _expr, String _indent, String _constType, boolean _isDisabled) {
		StringList elements = Element.splitExpressionList(
				_expr.substring(1, _expr.length()-1), ",");
		if (_constType != null) {
			addCode(_varName + ": " + _constType + " = (" + elements.concatenate(", ") + ");", _indent, _isDisabled);
		}
		else {
			// In order to be consistent with possible index access
			// at other positions in code, we use the standard Java
			// index range here (though in Pascal indexing usually 
			// starts with 1 but may vary widely). We solve the problem
			// by providing a configurable start index constant
			//insertComment("TODO: Check indexBase value (automatically generated)", _indent);
			appendComment("Hint: Automatically decomposed array initialization", _indent);
			// START KGU#332 2017-01-30: We must be better prepared for two-dimensional arrays
			//insertDeclaration("var", "indexBase_" + varName + ": Integer = 0;",
			//		_indent.length());
			//for (int el = 0; el < elements.count(); el++)
			//{
			//	addCode(varName + "[indexBase_" + varName + " + " + el + "] := " + 
			//			elements.get(el) + ";",
			//			_indent, isDisabled);
			//}
			//String baseName = varName;
			if (_varName.matches("\\w+\\[.*\\]")) {
				//baseName = varName.replaceAll("(\\w.*)\\[(.*)\\]", "$1_$2");
				_varName = _varName.replace("]", ", ");
			}
			else {
				_varName = _varName + "[";
			}
			//insertDeclaration("const", "indexBase_" + baseName + " = 0;",
			//		_indent.length());
			for (int ix = 0; ix < elements.count(); ix++)
			{
				// START KGU#560 2018-07-22: Bugfix #564 - initializers must be handled recursively!
				//addCode(_varName /*+ "indexBase_" + baseName + " + "*/ + ix + "] := " + 
				//		_elements.get(ix) + ";",
				//		_indent, _isDisabled);
				generateAssignment(_varName + ix + "]", elements.get(ix), _indent, _isDisabled);
			}
			// END KGU#332 2017-01-30
		}
	}
	// START KGU#388 2017-09-20: Enh. #423
	/**
	 * Appends the code for a record initialisation of variable {@code _varName} from
	 * the pre-transformed expression {@code _expr}.
	 * @param _varName - name of the variable to be initialized
	 * @param _expr - transformed initializer
	 * @param _indent - current indentation string
	 * @param _forConstant - whether this initializer is needed for a constant (a variable otherwise)
	 * @param _isDisabled - whether the source element is disabled (means to comment out the code)
	 * @param _typeEntry - used for component name retrieval if the initializer omits them (may be null)
	 */
	// START KGU#559 2018-07-20: Enh. #563 - loosened record initializer syntax
	//private void generateRecordInit(String _varName, String _expr, String _indent, boolean _forConstant, boolean _isDisabled)
	//{
	//	HashMap<String, String> components = Instruction.splitRecordInitializer(_expr);
	private void generateRecordInit(String _varName, String _expr, String _indent, boolean _forConstant, boolean _isDisabled, TypeMapEntry _typeEntry)
	{
		HashMap<String, String> components = Instruction.splitRecordInitializer(_expr, _typeEntry, false);
	// END KGU#559 2018-07-20
		if (_forConstant) {
			String typeName = components.get("§TYPENAME§");
			String indentPlus1 = _indent + this.getIndent();
			String indentPlus2 = indentPlus1 + this.getIndent();
			addCode(_varName + ": " + typeName + " = (", _indent, _isDisabled);
			for (Entry<String, String> comp: components.entrySet())
			{
				String compName = comp.getKey();
				if (!compName.startsWith("§")) {
					addCode(comp.getKey() + ":\t" + comp.getValue() + ";",
							indentPlus2, _isDisabled);
				}
			}
			addCode(");", indentPlus1, _isDisabled);
		}
		else {
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
	}
	// END KGU#388 2017-09-20
   
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
					this.generateArrayInit(_target, _expr, _indent, null, _isDisabled);
				}
			}
			else {
				addCode(_target + " := " + transform(_expr) + ";", _indent, _isDisabled);
			}
		}
		else {
			addCode(_target + " := " + transform(_expr) + ";", _indent, _isDisabled);
		}
	}
	// END KGU#560 2018-07-22

	@Override
    protected void generateCode(Alternative _alt, String _indent)
    {
    	boolean isDisabled = _alt.isDisabled();

    	// START KGU 2014-11-16
    	appendComment(_alt, _indent);
    	// END KGU 2014-11-16

    	//String condition = BString.replace(transform(_alt.getText().getText()),"\n","").trim();
    	String condition = transform(_alt.getUnbrokenText().getLongString()).trim();
    	// START KGU#311 2016-12-26: Enh. #314 File API support
    	if (this.usesFileAPI) {
    		StringList tokens = Element.splitLexically(condition, true);
    		for (int i = 0; i < this.fileVarNames.count(); i++) {
    			if (tokens.contains(this.fileVarNames.get(i))) {
    				this.appendComment("TODO: Consider replacing this file test using IOResult!", _indent);
    			}
    		}
    	}
    	// END KGU#311 2016-12-26
    	if(!condition.startsWith("(") && !condition.endsWith(")")) condition="("+condition+")";

    	addCode("if "+condition+" then", _indent, isDisabled);
    	addCode("begin", _indent, isDisabled);
    	generateCode(_alt.qTrue,_indent+this.getIndent());
    	if(_alt.qFalse.getSize()!=0)
    	{
    		addCode("end", _indent, isDisabled);
    		addCode("else", _indent, isDisabled);
    		addCode("begin", _indent, isDisabled);
    		generateCode(_alt.qFalse,_indent+this.getIndent());
    	}
    	addCode("end;", _indent, isDisabled);
    }

    @Override
    protected void generateCode(Case _case, String _indent)
    {
    	boolean isDisabled = _case.isDisabled();

    	// START KGU 2014-11-16
    	appendComment(_case, _indent);
    	// END KGU 2014-11-16

    	// START KGU#453 2017-11-02: Issue #447
    	//String condition = transform(_case.getText().get(0));
    	StringList unbrokenText = _case.getUnbrokenText();
    	String condition = transform(unbrokenText.get(0));
    	// END KGU#453 2017-11-02
    	if (!condition.startsWith("(") && !condition.endsWith(")")) {
    		condition = "("+condition+")";
    	}

    	addCode("case "+condition+" of", _indent, isDisabled);

    	for (int i = 0; i < _case.qs.size()-1; i++)
    	{
    		// START KGU#453 2017-11-02: Issue #447
    		//addCode(_case.getText().get(i+1).trim()+":", _indent+this.getIndent(), isDisabled);
    		addCode(unbrokenText.get(i+1).trim()+":", _indent+this.getIndent(), isDisabled);
    		// END KGU#453 2017-11-02
    		addCode("begin", _indent+this.getIndent()+this.getIndent(), isDisabled);
    		generateCode(_case.qs.get(i),_indent+this.getIndent()+this.getIndent()+this.getIndent());
    		addCode("end;", _indent+this.getIndent()+this.getIndent(), isDisabled);
    	}

    	// START KGU#453 2017-11-02: Issue #447
    	//if(!_case.getText().get(_case.qs.size()).trim().equals("%"))
    	if(!unbrokenText.get(_case.qs.size()).trim().equals("%"))
    	// END KGU#453 2017-11-02
    	{
    		addCode("else", _indent+this.getIndent(), isDisabled);
    		generateCode(_case.qs.get(_case.qs.size()-1), _indent+this.getIndent()+this.getIndent());
    	}
    	addCode("end;", _indent, isDisabled);
    }

    @Override
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
    	//code.add(_indent+"for "+BString.replace(transform(_for.getText().getText()),"\n","").trim()+" do");
    	//code.add(_indent + "begin");
    	//generateCode(_for.q, _indent+this.getIndent());
    	String counter = _for.getCounterVar();
    	int step = _for.getStepConst();
    	if (Math.abs(step) == 1)
    	{
    		// We may employ a For loop
    		String incr = (step == 1) ? " to " : " downto ";
    		addCode("for " + counter + " := " + transform(_for.getStartValue(), false) +
    				incr + transform(_for.getEndValue(), false) + " do",
    				_indent, isDisabled);
    	}
    	else
    	{
    		// While loop required
    		addCode(counter + " := " + transform(_for.getStartValue(), false),
    				_indent, isDisabled);
    		addCode("while " + counter + ((step > 0) ? " <= " : " >= ") + transform(_for.getEndValue(), false) + " do",
    				_indent, isDisabled);
    	}
    	addCode("begin", _indent, isDisabled);
    	generateCode(_for.q, _indent+this.getIndent());
    	if (Math.abs(step) != 1)
    	{
    		addCode(counter + " := " + counter + ((step > 0) ? " + " : " ") + step,
    				_indent + this.getIndent(), isDisabled); 
    	}
    	// END KGU#3 2015-11-02
    	addCode("end;", _indent, isDisabled);

    	// START KGU#74 2015-11-30: The following instruction is goto target
    	if (this.jumpTable.containsKey(_for))
    	{
    		addCode("StructorizerLabel_" + this.jumpTable.get(_for).intValue() + ": ;",
    				_indent, isDisabled);
    	}
    	// END KGU 2015-11-30
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
		boolean isDisabled = _for.isDisabled();
		String var = _for.getCounterVar();
		StringList items = this.extractForInListItems(_for);
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
			
			// Create some generic and unique variable names
			String postfix = Integer.toHexString(_for.hashCode());
			String arrayName = "array" + postfix;
			String indexName = "index" + postfix;

			String itemType = "";
			if (allBoolean) itemType = "boolean";
			// START KGU#542 2019-11-21: Enh. #739
			else if (allEnum != null && !allEnum.isEmpty()) itemType = allEnum;
			// END KGU#542 2019-11-21
			else if (allInt) itemType = "integer";
			else if (allReal) itemType = "real";
			else if (allString) itemType = "string";
			else {
				itemType = "FIXME_" + postfix;
				// We do a dummy type definition
				this.appendComment("TODO: Specify an appropriate element type for the array!", _indent);
			}

			// Insert the array and index declarations
			String range = "1.." + items.count();
			insertDeclaration("var", arrayName + ": " + "array [" + 
					range + "] of " + itemType + ";", _indent.length());
			insertDeclaration("var", indexName + ": " + range + ";",
					_indent.length());

			// Now we create code to fill the array with the enumerated values
			for (int i = 0; i < nItems; i++)
			{
				// START KGU#369 2017-03-15: Bugfix #382 item transformation had been missing
				//addCode(arrayName + "[" + (i+1) + "] := " + items.get(i) + ";",
				addCode(arrayName + "[" + (i+1) + "] := " + transform(items.get(i)) + ";",
				// END KGU#369 2017-03-15
						_indent, isDisabled);
			}
			
			// Creation of the loop header
			addCode("for " + indexName + " := 1 to " + nItems + " do",
					_indent, isDisabled);

			// Creation of the loop body
			addCode("begin", _indent, isDisabled);
			addCode(var + " := " + arrayName + "[" + indexName + "];",
					_indent+this.getIndent(), isDisabled);
			generateCode(_for.q, _indent+this.getIndent());
			addCode("end;", _indent, isDisabled);

			done = true;
		}
		else
		{
			String valueList = _for.getValueList();
			// We have no strategy here, no idea how to find out the number and type of elements,
			// no idea how to iterate the members, so we leave it similar to Delphi and just add a TODO comment...
			this.appendComment("TODO: Rewrite this loop (there was no way to convert this automatically)", _indent);

			// Creation of the loop header
			addCode("for " + var + " in " + transform(valueList, false) + " do",
					_indent, isDisabled);
			// Add the loop body as is
			addCode("begin", _indent, isDisabled);
			generateCode(_for.q, _indent + this.getIndent());
			addCode("end;", _indent, isDisabled);
			
			done = true;
		}
		return done;
	}
	// END KGU#61 2016-03-23

	@Override
	protected void generateCode(While _while, String _indent)
	{
		boolean isDisabled = _while.isDisabled();
		// START KGU 2014-11-16
		appendComment(_while, _indent);
		// END KGU 2014-11-16

		//String condition = BString.replace(transform(_while.getUnbrokenText().getText()),"\n","").trim();
		String condition = transform(_while.getUnbrokenText().getLongString()).trim();
		if(!condition.startsWith("(") && !condition.endsWith(")")) condition="("+condition+")";

		addCode("while "+condition+" do", _indent, isDisabled);
		addCode("begin", _indent, isDisabled);
		generateCode(_while.q,_indent+this.getIndent());
		addCode("end;", _indent, isDisabled);

		// START KGU#74 2015-11-30: The following instruction is goto target
		if (this.jumpTable.containsKey(_while))
		{
			addCode("StructorizerLabel_" + this.jumpTable.get(_while).intValue() + ": ;",
					_indent, isDisabled);
		}
		// END KGU 2015-11-30
	}

	@Override
	protected void generateCode(Repeat _repeat, String _indent)
	{
		boolean isDisabled = _repeat.isDisabled();
		// START KGU 2014-11-16
		appendComment(_repeat, _indent);
		// END KGU 2014-11-16

		//String condition = BString.replace(transform(_repeat.getUnbrokenText().getText()),"\n","").trim();
		String condition = transform(_repeat.getUnbrokenText().getLongString()).trim();
		if(!condition.startsWith("(") && !condition.endsWith(")")) condition="("+condition+")";

		addCode("repeat", _indent, isDisabled);
		generateCode(_repeat.q,_indent+this.getIndent());
		addCode(_indent+"until "+condition+";", _indent, isDisabled);

		// START KGU#74 2015-11-30: The following instruction is goto target
		if (this.jumpTable.containsKey(_repeat))
		{
			addCode("StructorizerLabel_" + this.jumpTable.get(_repeat).intValue() + ": ;",
					_indent, isDisabled);
		}
		// END KGU 2015-11-30
	}

	@Override
	protected void generateCode(Forever _forever, String _indent)
	{
		boolean isDisabled = _forever.isDisabled();
		// START KGU 2014-11-16
		appendComment(_forever, _indent);
		// END KGU 2014-11-16

		addCode("while (true) do", _indent, isDisabled);
		addCode("begin", _indent, isDisabled);
		generateCode(_forever.q,_indent+this.getIndent());
		addCode("end;", _indent, isDisabled);

		// START KGU#74 2015-11-30: The following instruction is goto target
		if (this.jumpTable.containsKey(_forever))
		{
			addCode("StructorizerLabel_" + this.jumpTable.get(_forever).intValue() + ": ;",
					_indent, isDisabled);
		}
		// END KGU 2015-11-30
	}

	@Override
	protected void generateCode(Call _call, String _indent)
	{
		boolean isDisabled = _call.isDisabled();
		// START KGU 2014-11-16
		appendComment(_call, _indent);
		// END KGU 2014-11-16

		StringList lines = _call.getUnbrokenText();
		for(int i=0;i<lines.count();i++)
		{
			addCode(transform(lines.get(i))+";", _indent, isDisabled);
		}
	}

	@Override
	protected void generateCode(Jump _jump, String _indent)
	{
		if (!appendAsComment(_jump, _indent)) {
			
			boolean isDisabled = _jump.isDisabled();

			appendComment(_jump, _indent);

			// KGU 2015-11-30: In Pascal, there is no break and no goto,
			// so empty Jumps won't be allowed
			// We will just have to translate exit into halt and return into exit
			boolean isEmpty = true;
			
			StringList lines = _jump.getUnbrokenText();
			// START KGU#142 2016-01-17: fixes Enh. #23 The following code had been
			// misplaced inside the text line loop, it belongs to the top (no further
			// analysis required):
			// Has it already been matched with a loop? Then syntax must have been okay...
			if (this.jumpTable.containsKey(_jump))
			{
				Integer ref = this.jumpTable.get(_jump);
				String label = "StructorizerLabel_" + ref;
				if (ref.intValue() < 0)
				{
					appendComment("FIXME: Structorizer detected this illegal jump attempt:", _indent);
					appendComment(lines.getLongString(), _indent);
					label = "__ERROR__";
				}
				else
				{
					appendComment("WARNING: Most Pascal compilers don't support jump instructions!", _indent);					
				}
				addCode("goto" + " " + label + ";", _indent, isDisabled);
			}
			else
			{
			// END KGU#142 2016-01-17
				String preReturn = CodeParser.getKeywordOrDefault("preReturn", "return");
				String preExit   = CodeParser.getKeywordOrDefault("preExit", "exit");
				String preThrow  = CodeParser.getKeywordOrDefault("preThrow", "throw");
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
						String argument = line.substring(preReturn.length()).trim();
						if (!argument.isEmpty())
						{
							addCode(this.procName + " := " + argument + ";", _indent, isDisabled); 
						}
						// START KGU 2016-01-17: Omit the exit if this is the last instruction of the diagram
						//code.add(_indent + "exit;");
						if (i < lines.count()-1 || !((_jump.parent).parent instanceof Root))
						{
							addCode("exit;", _indent, isDisabled);
						}
						// END KGU 2016-01-17
					}
					else if (Jump.isExit(line))
					{
						String argument = line.substring(preExit.length()).trim();
						if (!argument.isEmpty()) { argument = "(" + argument + ")"; }
						addCode("halt" + argument + ";", _indent, isDisabled);
					}
					// START KGU#686 2019-03-20: Enh. #56
					else if (Jump.isThrow(line)) {
						String what = line.substring(preThrow.length()).trim();
						addCode("raise Exception.Create(" + what + ");", _indent, isDisabled);
					}
					// END KGU#686 2019-03-20
					else if (!isEmpty)
					{
						appendComment("FIXME: Structorizer detected the following illegal jump attempt:", _indent);
						appendComment(line, _indent);
					}
					// END KGU#74/KGU#78 2015-11-30
				}
				if (isEmpty) {
					appendComment("FIXME: An empty jump was found here! Cannot be translated to " +
							this.getFileDescription(), _indent);
				}
			// START KGU#142 2016-01-17: Bugfix for enh. #23 (continued)
			}
			// END KGU#142 2016-01-17
		}
    }

	// START KGU#47 2015-11-30: Offer at least a sequential execution (which is one legal execution order)
	protected void generateCode(Parallel _para, String _indent)
	{
		boolean isDisabled = _para.isDisabled();
		
		// START KGU 2014-11-16
		appendComment(_para, _indent);
		// END KGU 2014-11-16

		addCode("", "", isDisabled);
		appendComment("==========================================================", _indent);
		appendComment("================= START PARALLEL SECTION =================", _indent);
		appendComment("==========================================================", _indent);
		appendComment("TODO: add the necessary code to run the threads concurrently", _indent);
		addCode("begin", _indent, isDisabled);

		for (int i = 0; i < _para.qs.size(); i++) {
			addCode("", "", isDisabled);
			appendComment("----------------- START THREAD " + i + " -----------------", _indent + this.getIndent());
			addCode("begin", _indent + this.getIndent(), isDisabled);
			generateCode((Subqueue) _para.qs.get(i), _indent + this.getIndent() + this.getIndent());
			addCode("end;", _indent + this.getIndent(), isDisabled);
			appendComment("------------------ END THREAD " + i + " ------------------", _indent + this.getIndent());
			addCode("", "", isDisabled);
		}

		addCode("end;", _indent, isDisabled);
		appendComment("==========================================================", _indent);
		appendComment("================== END PARALLEL SECTION ==================", _indent);
		appendComment("==========================================================", _indent);
		addCode("", "", isDisabled);
	}
	// END KGU#47 2015-11-30

	// START KGU#686 2019-03-20: Enh. #56
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateCode(lu.fisch.structorizer.elements.Try, java.lang.String)
	 */
	@Override
	protected void generateCode(Try _try, String _indent)
	{
		boolean isDisabled = _try.isDisabled();
		
		appendComment(_try, _indent);

		// Both try-except and try-finally blocks exist, but not in combination, so we must nest them if necessary
		String indent0 = _indent;
		
		this.addCode("try", _indent, isDisabled);

		if (_try.qFinally.getSize() > 0) {
			indent0 += this.getIndent();
			// Inner try instruction
			this.addCode("try", indent0, isDisabled);
		}
		String indent1 = indent0 + this.getIndent();
		
		this.generateCode(_try.qTry, indent1);
		
		//indent1 = _indent + this.getIndent();
		this.addCode("except", indent0, isDisabled);
		String exName = _try.getExceptionVarName();
		if (exName != null && !exName.isEmpty()) {
			this.addCode("on Ex : Exception do", indent1, isDisabled);
		}
		else {
			this.addCode("on Exception do", indent1, isDisabled);			
		}
		this.addCode("begin", indent1, isDisabled);
		String indent2 = indent1 + this.getIndent();
		if (exName != null && !exName.isEmpty()) {
			this.appendComment("FIXME: Ensure a declaration for variable " + exName + " (String) in the VAR block!", indent2);
			this.addCode(exName + " := Ex.Message;", indent2, isDisabled);
		}
		generateCode(_try.qCatch, indent2);
		this.addCode("end;", indent1, isDisabled);
		
		if (_try.qFinally.getSize() > 0) {
			this.addCode("end;", indent0, isDisabled);

			this.addCode("finally", _indent, isDisabled);
			
			this.generateCode(_try.qFinally, indent0);
		}

		this.addCode("end;", _indent, isDisabled);	// Close the (outer) try block 
	}
	// END KGU#686 2019-03-20

	// START KGU#74 2015-11-30 
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateHeader(lu.fisch.structorizer.elements.Root, java.lang.String, java.lang.String, lu.fisch.utils.StringList, lu.fisch.utils.StringList, java.lang.String)
	 */
	@Override
	protected String generateHeader(Root _root, String _indent, String _procName,
			StringList _paramNames, StringList _paramTypes, String _resultType, boolean _public)
	{
		this.procName = _procName;	// Needed for value return mechanisms

		if (topLevel)
		{
			// START KGU#311/KGU#815/KGU#836 2020-03-22: Enh. #314, #828, bugfix #836
			if (this.usesFileAPI && (this.isLibraryModule() || this.importedLibRoots != null)) {
				this.generatorIncludes.addIfNew(FILE_API_UNIT_NAME);
			}
			// END KGU#311/KGU#815/KGU#836 2020-03-22
			if (_root.isProgram()) {
				code.add(_indent + "program " + _procName + ";");				
			}
			else {
				// START KGU#194 2016-07-20: Bugfix #185 - Though the UNIT name is to be the same as the file name
				// (or vice versa),
				// we must not allow non-identifier characters. so convert all characters that are neither letters
				// nor digits into underscores.
				//code.add(_indent + "UNIT " + pureFilename + ";");
				String unitName = this.getModuleName();
				if (unitName.isEmpty()) {
					unitName = _root.proposeFileName().toUpperCase();
					if (unitName.contains("-")) {
						unitName = unitName.substring(0, unitName.indexOf('-'));
					}
				}
				code.add(_indent + "UNIT " + unitName + ";");
				// END KGU#194 2016-07-20
			}
			appendComment("Generated by Structorizer " + Element.E_VERSION, _indent);
			// START KGU#363 2017-05-16: Enh. #372
			appendCopyright(_root, _indent, true);
			// END KGU#363 2017-05-16
			if (!_root.isSubroutine() && !_root.getComment().getLongString().trim().isEmpty()) {
				addSepaLine();
				this.appendComment(_root, _indent);
			}
			addSepaLine();
			// START KGU#351 2017-02-26: Enh. #346
			if (_root.isProgram()) {
				// For non-programs this is done in another branch
				if (this.appendUserIncludes(_indent) > 0
						|| this.appendGeneratorIncludes(_indent, false) > 0) {
					addSepaLine();
				}
				// FIXME (#389): If Includables are to form separate UNITs then they are to be referenced here, too
			}
			// END KGU#351 2017-02-26
		}

		// START KGU#757 2019-11-08: Bugfix #772 - had been in generatePreamble, which might cause NullPointerExceptions here
		typeMap = _root.getTypeInfo(routinePool);
		// END KGU#757 2019-11-08

		if (!_root.isProgram()) {

			// START KGU#194 2016-05-07: Bugfix #185 - create a unit context
			if (topLevel)
			{

				addSepaLine();
				code.add(_indent + "INTERFACE");
				// START KGU#351 2017-02-26: Enh. #346
				this.appendUserIncludes(_indent);
				// END KGU#351 2017-02-26
				// START KGU#815/KGU#824 2020-03-19: Enh. #828, bugfix #836 had been missing
				this.appendGeneratorIncludes(_indent, false);
				// END KGU#815/KGU#824 2020-03-19
				addSepaLine();
				// START KGU#194/KGU#376 2017-09-22: Bugfix #185, Enh. #389 - the function header shall have the comment
				if (!_root.isInclude() && _public) {
					appendComment(_root, _indent);
				}
				// END KGU#194/KGU#376 2017-09-22

				// START KGU#815 2020-03-16: Enh. #828
				//code.add(_indent + pr + " " + signature + ";");
				if (_root.isInclude() || this.isLibraryModule()) {
					// We must place all declarations in the interface if we are creating a library
					this.generateDeclarations(_root, _indent, this.varNames);
				}
				
				this.interfaceInsertionLine = code.count();
				if (_public && _root.isSubroutine()) {
					this.insertPrototype(_root, _indent, true, code.count());
				}
				// END KGU#815 2020-03-16
				addSepaLine();
				code.add(_indent + "IMPLEMENTATION");

				// START KGU#388 2017-09-21: Enh. #423
				// START KGU#815 2020-03-16: Enh. #828
				generateDeclarations(_root, _indent, null);
				//if (!_root.isInclude() && !this.isLibraryModule()) {
				//	this.generateDeclarations(_root, _indent, null);
				//}
				// END KGU#388 2017-09-21

				// START KGU#178 2016-07-20: Enh. #160 - insert called subroutines here
				subroutineInsertionLine = code.count();
				subroutineIndent = _indent;
				// END KGU#178 2016-07-20

				// START KGU#311 2016-12-26: Enh. #314
				if (this.usesFileAPI) {
					// START KGU#815/KGU#836 2020-03-22: Enh. #828, bugfix #836 turend out to be too simple
					//this.insertFileAPI("pas");
					//addSepaLine();
					if (!this.generatorIncludes.contains(FILE_API_UNIT_NAME)) {
						// The type definitions will already have been inserted in generateDeclarations()
						this.insertFileAPI("pas", 2);	// The routine implementations
						addSepaLine();
					}
					// END KGU#815/KGU#836 2020-03-22
				}
				// END KGU#311 2016-12-26

				addSepaLine();
			}
			// END KGU#194 2016-05-07
			// START KGU#815 2020-03-16: Enh. #828 group export requires interface with more than one signature
			else if (_public) {
				// FIXME This can't be sensible, not at least at this position!
				//StringList complexConsts = new StringList();
				//int codeEnd = code.count();
				//generateDeclarations(_root, subroutineIndent, null, complexConsts);
				//// Now move the added code to the subroutine insertion line
				//StringList addedCode = code.subSequence(codeEnd, code.count());
				//code.remove(codeEnd, code.count());
				//for (int i = 0; i < addedCode.count(); i++) {
				//	insertCode(addedCode.get(i), this.subroutineInsertionLine);
				//}
			}

			// Now write the routine signature into the IMPLEMENTATION section
			// START KGU#376 2017-09-21: Enh. #389
			//code.add(_indent + pr + " " + signature + ";");
			if (_root.isSubroutine()) {
				// For public routines the comment is already placed in the INTERFACE
				insertPrototype(_root, _indent, !_public, code.count());
			}
			// END KGU#376 2017-09-21
		}

		if (this.labelCount > 0)
		{
			// Declare the used labels
			addSepaLine();
			code.add(_indent + "label");
			for (int lb = 0; lb < this.labelCount; lb++)
			{
				code.add(_indent + this.getIndent() + "StructorizerLabel_" + lb + ";");
			}
		}

		// START KGU#311 2016-12-26: Enh. #314
		// START KGU#815/KGUZ#824 2020-03-22: Enh. #828, bugfix #836 group export may require a separate UNIT
		//if (topLevel && _root.isProgram() && this.usesFileAPI) {
		if (topLevel && _root.isProgram() && this.usesFileAPI && !this.generatorIncludes.contains(FILE_API_UNIT_NAME)) {
		// END KGU#815/KGUZ#824 2020-03-22: Enh. #828, bugfix #836 group export may require a separate UNIT
			this.insertFileAPI("pas", code.count(), _indent, 1);
		}
		// END KGU#311 2016-12-26

		addSepaLine();
		// START KGU#375 2017-04-12: Enh. #388 now passed to generatePreamble
		//code.add(_indent + "var");
		// END KGU#375 2017-04-12

		return _indent;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generatePreamble(lu.fisch.structorizer.elements.Root, java.lang.String, lu.fisch.utils.StringList)
	 */
	@Override
	protected String generatePreamble(Root _root, String _indent, StringList _varNames)
	{
		// START KGU#261 2017-01-30: Enh. #259: Insert actual declarations if possible
		// START KGU#676 2019-03-30: Enh. #696 special pool in case of batch export
		//typeMap = _root.getTypeInfo();
		//typeMap = _root.getTypeInfo(routinePool); // KGU#757 2019-11-08 Bugfix #772 - moved to generateHeader()
		// END KGU#676 2019-03-30
		// END KGU#261 2017-01-30
		// START KGU#388 2017-09-20: Enh. #423
		//StringList complexConsts = new StringList();	// KGU#815/KGU#824 2020-03-19: No longer needed 
		// END KGU#388 2017-09-20

		// START KGU#376 2017-09-21: Enh. #389 Concentrate all included definitions here
		// START KGU#815/KGU#824 2020-03-19: Enh. #828, bugfix #836
		//Root[] includes = generateDeclarations(_root, _indent, _varNames, complexConsts);
		Root[] includes = new Root[]{};
		if (!(topLevel && (_root.isInclude() || this.isLibraryModule()))) {
			includes = generateDeclarations(_root, _indent, _varNames);
		}
		else if (topLevel) {
			includes = this.includedRoots.toArray(includes);
		}
		// END KGU#815/KGU#824 2020-03-19
		// END KGU#376 2017-09-21
		
		// START KGU#178 2016-07-20: Enh. #160
		// START KGU#376 2017-09-21: Enh. #389: Special care for an Includable at top level
		//if (topLevel && _root.isProgram() && this.optionExportSubroutines())
		// START KGU#815 2020-03-17: Enh. #828 - the mode restriction seemed useless
		//if (topLevel && !_root.isSubroutine() && this.optionExportSubroutines())
		if (topLevel && !_root.isSubroutine())
		// END KGU#815 2020-03-17
		// END KGU#376 2017-09-21
		{
			subroutineInsertionLine = code.count();
			subroutineIndent = _indent;
			// START KGU#311 2016-12-26: Enh. #314
			// START KGU#815/KGU#824 2020-03-22: Enh. #828, bugfix #836
			//if (this.usesFileAPI) {
			if (this.usesFileAPI && !generatorIncludes.contains(FILE_API_UNIT_NAME)) {
			// END KGU#815/KGU#824 2020-03-22
				this.insertFileAPI("pas", 2);
			}
			// END KGU#311 2016-12-26
			addSepaLine();
		}
		// END KGU#178 2016-07-20

		// START KGU#376 2017-09-21: Enh. #389 Includables cannot have an own body
		//code.add(_indent + "begin");
		if (!_root.isInclude()) {
			addSepaLine();
			code.add(_indent + "begin");
			// START KGU#376 2017-09-21: Enh. #389 - code of includes is to be produced here
			if (_root.isProgram()) {
				for (Root incl: includes) {
					generateCode(incl.children, _indent + this.getIndent());
				}
			}
			// END KGU#376 2017-09-21
		}
		else {
			if (code.count() == subroutineInsertionLine) {
				code.add("");	// Make sure the subroutines are inserted before library entry points
			}
			libraryInsertionLine = code.count();
			addSepaLine();
			code.add(_indent + "BEGIN");
		}
		// END KGU#376 2017-09-21

		// START KGU#375 2017-09-20: Enh. #388 Workaround if structured constants aren't allowed
		//for (Root incl: includes) {
		//	if (incl != _root) {
		//		this.insertPostponedInitialisations(incl, _indent + this.getIndent());
		//	}
		//}
		//this.insertPostponedInitialisations(_root, _indent + this.getIndent());
		// END KGU#375 2017-09-20

		// START KGU#376 2017-09-21: Enh. #389 Includables cannot have an own body
		return _indent + this.getIndent();
	}

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
	 * @param _complexConsts - a StringList being filled with the names of those structured
	 * constants that cannot be converted to structured Pascal constants but are to
	 * be deconstructed as mere variables in the body (shouldn't be used anymore).
	 * @return array of the included {@link Root}s
	 */
	// START KGU#815/KGU#824 2020-03-19: Enh. #828, bugfix #836 last argument not needed externally
	//protected Root[] generateDeclarations(Root _root, String _indent, StringList _varNames, StringList complexConsts) {
	protected Root[] generateDeclarations(Root _root, String _indent, StringList _varNames) {
		/* A StringList being filled with the names of those structured
		 * constants that cannot be converted to structured Pascal constants but are to
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
		// START KGU#815/KGU#836 2020-03-22: Enh. #828, bugfix #836
		if (topLevel && this.usesFileAPI && !generatorIncludes.contains(FILE_API_UNIT_NAME) && _varNames == null) {
			this.insertFileAPI("pas", code.count(), _indent, 1);
			introPlaced = true;	// Includes the TYPE keyword
		}
		// END KGU#815/KGU#836 2020-03-22
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
			// (Only if structured constants are allowed, which is the case in most newer Pascal dialects)
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
		
		introPlaced = false;	// Has the VAR keyword already been written?
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
			// START KGU#762 2019-11-13: Bugfix #776 - we must not repeat mere decalartions from Includables here
			//introPlaced = generateVarDecls(_root, _indent, _varNames, _complexConsts, introPlaced);
			StringList ownVarNames = _varNames.copy();
			if (!topLevel && _root.includeList != null) {
				for (Root incl: includedRoots) {
					// Because of recursiveness of declaration retrieval, we may restrict to the
					// directly included diagrams, this reduces the risk of eliminating variable
					// names that are not included but locally defined.
					if (_root.includeList.contains(incl.getMethodName())) {
						StringList declNames = incl.getMereDeclarationNames(true);
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
			generateVarDecls(_root, _indent, _root.getMereDeclarationNames(true), new StringList(), introPlaced);
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
			// _root.constants is expected to be a LinkedHashMap, such that topological
			// ordering should not be necessary
			for (Entry<String, String> constEntry: _root.constants.entrySet()) {
				String constName = constEntry.getKey();
				// We must make sure that the constant hasn't been included from a diagram
				// already handled at top level.
				if (wasDefHandled(_root, constName, true)) {
					continue;
				}
				// START KGU#388 2017-09-19: Enh. #423 Modern Pascal allows structured constants
				//code.add(indentPlus1 + constEntry.getKey() + " = " + transform(constEntry.getValue()) + ";");
				// START KGU#452 2019-11-17: Enh. #739 Skip enumerator values - they are to be handled in a type definition
				//String expr = transform(constEntry.getValue());
				//String expr = transform(constEntry.getValue());
				String expr = constEntry.getValue();
				if (expr != null && expr.startsWith(":")) {
					continue;
				}
				expr = transform(expr);
				// END KGU#452 2019-11-17
				TypeMapEntry constType = typeMap.get(constEntry.getKey()); 
				if (constType == null || (!constType.isArray() && !constType.isRecord())) {
					if (!_sectionBegun) {
						code.add(_indent + "const");
						//insertComment("TODO: check and accomplish constant definitions", indentPlus1);
						_sectionBegun = true;
					}
					// START KGU#424 2017-09-25
					appendDeclComment(_root, indentPlus1, constName);
					// END KGU#424 2017-09-25
					code.add(indentPlus1 + constEntry.getKey() + " = " + expr + ";");
				}
				else {
					StringList generatedInit = null;
					int lineNo = code.count();
					if (expr.endsWith("}")) {
						// Seems to be an initializer
						// START KGU#424 2017-09-25
						appendDeclComment(_root, "", constName);
						// END KGU#424 2017-09-25
						if (constType.isArray()) {
							generateArrayInit(constEntry.getKey(), expr, "", transformTypeFromEntry(constType, null), false);
						}
						else {
							generateRecordInit(constEntry.getKey(), expr, "", true, false, constType);
						}
						generatedInit = code.subSequence(lineNo, code.count());
						code.remove(lineNo, code.count());
					}
					else {
						// May be the assignment of e.g. another constant of the same type
						generatedInit = StringList.getNew(constEntry.getKey() + " = " + expr + ";");
					}
					StringList structuredInits = this.structuredInitialisations.get(_root);
					// Note: This effectively modifies an entry of attribute this.structuredInitialisations!
					if (structuredInits != null) {
						structuredInits.add("");
						structuredInits.add(generatedInit);
					}
					else {
						this.structuredInitialisations.put(_root, generatedInit);
					}
					// Only needed if structured constant definitions aren't allowed
					//_complexConsts.add(constEntry.getKey());
				}
				// END KGU#388 2017-09-19
			}
			addSepaLine();
		}
		return _sectionBegun;
	}

	/**
	 * Adds type definitions for all types in {@link #typeMap}.
	 * @param _root - originating Root
	 * @param _indent - current indentation level (as String)
	 * @param _sectionBegun - whether the TYPE section had already been introduced by keyword CONST
	 * @return true if TYPE section has been introduced (no matter whether before or here)
	 */
	protected boolean generateTypeDefs(Root _root, String _indent, boolean _sectionBegun) {
		String indentPlus1 = _indent + this.getIndent();
		String indentPlus2 = indentPlus1 + this.getIndent();
		String indentPlus3 = indentPlus2 + this.getIndent();
		// START KGU#774 2019-11-29: Bugfix #787 - On top level, any Includable declared any types known to main
		//for (Entry<String, TypeMapEntry> typeEntry: typeMap.entrySet()) {
		for (Entry<String, TypeMapEntry> typeEntry: _root.getTypeInfo(routinePool).entrySet()) {
		// END KGU#774 2019-11-29
			String key = typeEntry.getKey();
			if (key.startsWith(":") /*&& typeEntry.getValue().isDeclaredWithin(_root)*/) {
				if (wasDefHandled(_root, key, true)) {
					continue;
				}
				if (!_sectionBegun) {
					code.add(_indent + "type");
					_sectionBegun = true;
				}
				// START KGU#424 2017-09-25
				appendDeclComment(_root, indentPlus1, key);
				// END KGU#424 2017-09-25
				TypeMapEntry type = typeEntry.getValue();
				if (type.isRecord()) {
					code.add(indentPlus1 + key.substring(1) + " = RECORD");
					for (Entry<String, TypeMapEntry> compEntry: type.getComponentInfo(false).entrySet()) {
						code.add(indentPlus3 + compEntry.getKey() + ":\t" + transformTypeFromEntry(compEntry.getValue(), null) + ";");
					}
					code.add(indentPlus2 + "END;");
				}
				// START KGU#542 2019-11-17: Enh. #739
				else if (type.isEnum()) {
					StringList items = type.getEnumerationInfo();
					String itemList = items.concatenate(", ");
					if (itemList.length() > 70) {
						code.add(indentPlus1 + key.substring(1) + " = (");
						for (int i = 0; i < items.count(); i++) {
							// FIXME: We might have to transform the value...
							code.add(indentPlus3 + items.get(i) + (i < items.count()-1 ? "," : ""));
						}
						code.add(indentPlus2 + ");");
					}
					else {
						code.add(indentPlus1 + key.substring(1) + " = (" + itemList + ");");
					}
				}
				// END KGU#542 2019-11-17
				else {
					code.add(indentPlus1 + key.substring(1) + " = " + this.transformTypeFromEntry(type, null) + ";");					
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
		// START KGU#261 2017-01-26: Enh. #259: Insert actual declarations if possible
		//HashMap<String, TypeMapEntry> typeMap = _root.getTypeInfo();	// KGU 2018-07-22: became obsolete by new field typeMap
		// END KGU#261 2017-01-16
		// START KGU#593 2018-10-05: Bugfix #619 - the function result variable must not be declared (again) here!
		String functionName = null;
		// START KGU 2020-03-25: This seemed to be too slacky
		//if (_root.getResultType() != null) {
		if (_root.getResultType() != null || this.returns || this.isResultSet || this.isFunctionNameSet) {
		// END KGU 2020-03-25
			functionName = _root.getMethodName();
		}
		// END KGU#593 2018-10-05
		for (int v = 0; v < _varNames.count(); v++) {
			// START KGU#261 2017-01-26: Enh. #259: Insert actual declarations if possible
			//insertComment(_varNames.get(v), _indent + this.getIndent());
			String varName = _varNames.get(v);
			// START KGU#593 2018-10-05: Bugfix #619 - the function result variable must not be declared (again) here!
			if (functionName != null && varName.equals(functionName)) {
				continue;
			}
			// END KGU#593 2018-10-05
			// START KGU#375 2017-04-12: Enh. #388 constants have already been defined
			boolean isComplexConst = _complexConsts.contains(varName);
			if (_root.constants.containsKey(varName) && !isComplexConst) {
				continue;
			}
			// END KGU#375 2017-04-12
			if (wasDefHandled(_root, varName, true)) {
				continue;
			}
			if (!_sectionBegun) {
				code.add(_indent + "var");
				//insertComment("TODO: check and accomplish variable declarations", _indent + this.getIndent());
				_sectionBegun = true;
			}
			// START KGU#424 2017-09-25
			appendDeclComment(_root, indentPlus1, varName);
			// END KGU#424 2017-09-25
			TypeMapEntry typeInfo = typeMap.get(varName); 
			StringList types = null;
			if (typeInfo != null) {
				// START KGU#388 2017-09-19: Enh. #423
				//types = getTransformedTypes(typeInfo);
				types = getTransformedTypes(typeInfo, true);
				// END KGU#388 2017-09-19
			}
			if (types != null && types.count() == 1) {
				String type = types.get(0);
				String prefix = "";
				int level = 0;
				while (type.startsWith("@")) {
					// It's an array, so get its index range
					int minIndex = typeInfo.getMinIndex(level);
					int maxIndex = typeInfo.getMaxIndex(level++);
					String indexRange = "";
					if (maxIndex > 0) {
						indexRange = "[" + minIndex +
								".." + maxIndex + "] ";
					}
					prefix += "array " + indexRange + "of ";
					type = type.substring(1);
				}
				type = prefix + type;
				String comment = "";
				if (type.contains("???")) {
					// START KGU#759 2019-12-02: Issue #773 - it only irritates the user to outcomment this - the code isn't compilable anyway
					//appendComment(varName + ": " + type + ";", indentPlus1);
					comment = "\t" + this.commentSymbolLeft() + " FIXME! " + this.commentSymbolRight();
					// END KGU#759 2019-12-02
				}
				//else {
				if (isComplexConst) {
					varName = this.commentSymbolLeft() + "const" + this.commentSymbolRight() + " " + varName;
				}
				addCode(varName + ": " + type + ";" + comment, indentPlus1, false);
				//}
			}
			else {
				// START KGU#759 2019-11-11: Issue #773 - it only irritates the user to outcomment this - the code isn't compilable anyway
				//appendComment(varName, indentPlus1);
				addCode(varName + ": ???;\t" + this.commentSymbolLeft() + " FIXME! " + this.commentSymbolRight(), indentPlus1, false);
				// END KGU#759 2019-11-11
			}
			// END KGU#261 2017-01-16
		// START KGU#375 2017-04-12: Enh. #388
		}
		// END KGU#375 2017-04-12
		return _sectionBegun;
	}

	// START KGU#375/KGU#376/KGU#388 2017-09-20: Enh. #388, #389, #423 
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

	// START KGU#74 2015-12-20: Enh. #22: We must achieve a correct value assignment to the function name
	@Override
	protected String generateResult(Root _root, String _indent, boolean alwaysReturns, StringList varNames)
	{
		if (_root.isSubroutine())
		{
			String varName = "";
			if (isResultSet && !isFunctionNameSet && !alwaysReturns)
			{
				int vx = varNames.indexOf("result", false);
				varName = varNames.get(vx);
				addSepaLine();
				appendComment("Automatically inserted to ensure Pascal value return. May be dropped on Structorizer reimport.", _indent);
				code.add(_indent + this.getIndent() + _root.getMethodName() + " := " + varName + ";");
			}
		}
		return _indent;
	}
	// END KGU#74 2015-12-20

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateFooter(lu.fisch.structorizer.elements.Root, java.lang.String)
	 */
	@Override
	protected void generateFooter(Root _root, String _indent)
	{
		// START KGU#194 2016-05-07: Bugfix #185 - create a unit context
		if (_root.isSubroutine()) {
			addSepaLine();
			code.add(_indent + "end;");
			if (topLevel)
			{
				addSepaLine();
				libraryInsertionLine = code.count();
				code.add("");	// addSepaLine() wouldn't help here
				code.add(_indent + "BEGIN");
				// START KGU#376 2017-09-21: Enh. #389 - code of includes is to be produced here
				// START KGU#815/KGU#824 2020-03-18: Enh. #828, bugfix #836
				//while (!includedRoots.isEmpty()) {
				//	generateCode(includedRoots.remove().children, _indent + this.getIndent());
				//}
				this.appendGlobalInitialisations(_indent + this.getIndent());
				// END KGU#815/KGU#824 2020-03-18
				// END KGU#376 2017-09-21
				code.add(_indent + "END.");
			}
		}
		// START KGU#376 2017-09-21: Enh. #389
		else if (_root.isInclude()) {
			// START KGU#815 2020-03-30: Enh. #828
			if (topLevel) {
				this.appendGlobalInitialisations(_indent + this.getIndent());
			}
			// END KGU#815 2020-03-30
			code.add(_indent + "END.");
		}
		// END KGU#376 2017-09-12
		else
		// END KGU#194 2016-05-07
		{
			code.add(_indent + "end.");
		}
	}
	// END KGU#74 2015-11-30
	
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
	
	// START KGU#311/KGU#815 2020-03-22: Enh. #314 + enh. #828
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#copyFileAPIResources(java.lang.String)
	 */
	@Override
	protected boolean copyFileAPIResources(String _filePath)
	{
		/* If importedLibRoots is not null then we had a multi-module export,
		 * this function will only be called if at least one of the modules required
		 * the file API, so all requiring modules will have put "StructorizerFileAPI"
		 * to their USES list. Now we have to make sure it gets provided.
		 */
		return this.importedLibRoots == null || copyFileAPIResource("pas", FILE_API_UNIT_NAME+".pas", _filePath);
	}
	// END KGU#311/KGU#815 2020-03-22

}

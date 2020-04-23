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
 *      Author:         Kay Gürtzig
 *
 *      Description:    This class generates C++ code (mainly based on ANSI C code except for IO).
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2010-08-31      First Issue
 *      Kay Gürtzig     2015-11-01      Adaptations to new decomposed preprocessing
 *      Kay Gürtzig     2015-11-30      Jump mechanisms (KGU#78) and root export revised 
 *      Kay Gürtzig     2015-12-11      Enh. #54 (KGU#101): Support for output expression lists
 *      Kay Gürtzig     2015-12-13      Bugfix #51 (=KGU#108): Cope with empty input and output
 *      Kay Gürtzig     2016-01-14      Type conversion of C overridden (KGU#16)
 *      Kay Gürtzig     2016-03-23      Enh. #84: Support for FOR-IN loops (KGU#61)
 *      Kay Gürtzig     2016-08-10      Issue #227: <iostream> only included if needed 
 *      Kay Gürtzig     2016-08-12      Enh. #231: Additions for Analyser checks 18 and 19 (variable name collisions)
 *      Kay Gürtzig     2016-09-25      Enh. #253: CodeParser.keywordMap refactoring done 
 *      Kay Gürtzig     2016-10-15      Enh. #271: Support for input instructions with prompt
 *      Kay Gürtzig     2016-11-08      Collateral damage of #271 to getOutputReplacer() mended
 *      Kay Gürtzig     2016-12-25      Enh. #314: Support for File API added.
 *      Kay Gürtzig     2017-01-05      Enh. #314: File API intervention in transformTokens modified
 *      Kay Gürtzig     2017-01-30      Enh. #259/#335: Type retrieval and improved declaration support 
 *      Kay Gürtzig     2017-01-31      Enh. #113: Array parameter transformation
 *      Kay Gürtzig     2017-02-21      Enh. #348: Parallel sections translated with <thread> library
 *      Kay Gürtzig     2017-02-27      Enh. #346: Insertion mechanism for user-specific include directives
 *      Kay Gürtzig     2017-04-12      Issue #335: transformType() revised and isInternalDeclarationAllowed() corrected
 *      Kay Gürtzig     2017-05-16      Enh. #372: Export of copyright information
 *      Kay Gürtzig     2017-05-24      Bugfix: name suffix for Parallel elements now hexadecimal (could otherwise be negative)
 *      Kay Gürtzig     2017-09-27      Enh. #423: Handling of struct definitions and access
 *      Kay Gürtzig     2018-02-22      Bugfix #517: Declarations/initializations from includables weren't handled correctly 
 *      Kay Gürtzig     2019-02-14      Enh. #680: Support for input instructions with several variables
 *      Kay Gürtzig     2019-03-08      Enh. #385: Support for parameter default values
 *      Kay Gürtzig     2019-03-30      Issue #696: Type retrieval had to consider an alternative pool
 *      Kay Gürtzig     2019-10-18      Enh. #739: Support for enum types
 *      Kay Gürtzig     2019-12-02      KGU#784 Defective type descriptions in argument lists and thread function operators
 *      Kay Gürtzig     2020-03-20/23   Enh. #828 bugfix #836: Group export implemented, batch export improved
 *      Kay Gürtzig     2020-03-23      Issue #840: Adaptations w.r.t. disabled elements using File API
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      2015.11.01 Simplified, drastically reduced to CGenerator as parent class
 *
 *      2010.08.31 Initial version
 *      - root handling overridden - still too much copied code w.r.t. CGenerator, should be
 *        parameterized
 *
 ******************************************************************************************************///

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.IElementVisitor;
import lu.fisch.structorizer.elements.Parallel;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.structorizer.elements.Try;
import lu.fisch.structorizer.elements.TypeMapEntry;
import lu.fisch.structorizer.executor.Executor;
import lu.fisch.structorizer.generators.Generator.TryCatchSupportLevel;
import lu.fisch.structorizer.parsers.CodeParser;
import lu.fisch.utils.BString;
import lu.fisch.utils.StringList;

public class CPlusPlusGenerator extends CGenerator {

    /************ Fields ***********************/
    protected String getDialogTitle()
    {
            return "Export C++ ...";
    }

    protected String getFileDescription()
    {
            return "C++ Source Code";
    }

    protected String[] getFileExtensions()
    {
            String[] exts = {"cpp"};
            return exts;
    }

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
	/**
	 * Subclassable method to specify the degree of availability of a try-catch-finally
	 * construction in the target language.
	 * @return a {@link TryCatchSupportLevel} value
	 * @see #appendCatchHeading(Try, String)
	 */
	protected TryCatchSupportLevel getTryCatchLevel()
	{
		return TryCatchSupportLevel.TC_TRY_CATCH;
	}
	// END KGU#686 2019-03-18

//	// START KGU 2016-08-12: Enh. #231 - information for analyser - obsolete since 3.27
//    private static final String[] reservedWords = new String[]{
//		"auto", "break", "case", "char", "const", "continue",
//		"default", "do", "double", "else", "enum", "extern",
//		"float", "for", "goto", "if", "int", "long",
//		"register", "return",
//		"short", "signed", "sizeof", "static", "struct", "switch",
//		"typedef", "union", "unsigned", "void", "volatile", "while",
//		"asm", "bool", "catch", "calss", "const_cast", "delete", "dynamic_cast",
//		"explicit", "false", "friend", "inline", "mutable", "namespace", "new", "nullptr",
//		"operator", "private", "public", "protected", "reinterpret_cast",
//		"static_cast", "template", "this", "throw", "true", "try", "typeid", "typename",
//		"using", "virtual", "wchar_t"};
//	public String[] getReservedWords()
//	{
//		return reservedWords;
//	}
//	// END KGU 2016-08-12

	/************ Code Generation **************/

	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#getInputReplacer(boolean)
	 */
	@Override
	// START KGU#281 2016-10-15: Enh. #271
	//protected String getInputReplacer()
	//{
	//	return "std::cin >> $1";
	//}
	protected String getInputReplacer(boolean withPrompt)
	{
		if (withPrompt) {
			return "std::cout << $1; std::cin >> $2";
		}
		return "std::cin >> $1";
	}
	// END KGU#281 2016-10-15

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#getOutputReplacer()
	 */
	@Override
	protected String getOutputReplacer()
	{
		return "std::cout << $1 << std::endl";
	}
	
	// START KGU#815/KGU#824 2020-03-21: Enh. #828, bugfix #836
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#insertPrototype(lu.fisch.structorizer.elements.Root, java.lang.String, boolean, int)
	 */
	@Override
	protected int insertPrototype(Root _root, String _indent, boolean _withComment, int _atLine)
	{
		int lines = 0; 	// Result value = number of inserted lines
		String fnHeader = "int main(void)";
		boolean returnsArray = false;
		if (!_root.isProgram()) {
			// This is explicitly also to be done for Includables (resulting in an initialisation function)
			String fnName = _root.getMethodName();
			StringList paramNames = new StringList();
			StringList paramTypes = new StringList();
			_root.collectParameters(paramNames, paramTypes, null);
			// Start with the result type
			fnHeader = transformType(_root.getResultType(),
					((returns || isResultSet || isFunctionNameSet) ? "int" : "void"));
			// START KGU#140 2017-01-31: Enh. #113 - improved type recognition and transformation
			returnsArray = fnHeader.toLowerCase().contains("array") || fnHeader.contains("]");
			if (returnsArray) {
				fnHeader = transformArrayDeclaration(fnHeader, "");
			}
			// END KGU#140 2017-01-31
			// START KGU#371 2019-03-07: Enh. #385 Care for default values
			StringList defaultVals = _root.getParameterDefaults();
			// END KGU#371 2019-03-07
			fnHeader += " " + fnName + "(";
			for (int p = 0; p < paramNames.count(); p++) {
				if (p > 0) { fnHeader += ", "; }
				// START KGU#140 2017-01-31: Enh. #113: Proper conversion of array types
				//fnHeader += (transformType(_paramTypes.get(p), "/*type?*/") + " " + 
				//		_paramNames.get(p)).trim();
				fnHeader += transformArrayDeclaration(transformType(paramTypes.get(p), "???").trim(), paramNames.get(p));
				// START KGU#371 2019-03-07: Enh. #385
				String defVal = defaultVals.get(p);
				if (defVal != null) {
					fnHeader += " = " + transform(defVal);
				}
				// END KGU#371 2019-03-07
				// END KGU#140 2017-01-31
			}
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
			if (returnsArray) {
				lines += insertComment("      C++ may not permit to return arrays like this - find an other way to pass the result!",
						_indent, _atLine + lines);
			}
			// END KGU#140 2017-01-31
		}
		insertCode(fnHeader, _atLine + lines);

		return lines + 1;
	}
	// END KGU#815/KGU#824 2020-03-21

	// START KGU#101 2015-12-11: Enhancement #54: Cope with output expression lists
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transform(java.lang.String)
	 */
	@Override
	protected String transform(String _input)
	{
		// START KGU#101 2015-12-11: Enh. #54 - support lists of expressions
		String outputKey = CodeParser.getKeyword("output").trim();
		if (_input.matches("^" + getKeywordPattern(outputKey) + "[ ](.*?)"))
		{
			StringList expressions = 
					Element.splitExpressionList(_input.substring(outputKey.length()), ",");
			_input = outputKey + " " + expressions.concatenate(" << ");
		}
		// END KGU#101 2015-12-11
		
		_input = super.transform(_input);
		
		// START KGU#108 2015-12-13: Bugfix #51: Cope with empty input and output
		// START KGU#281 2016-10-15: Enh. #271 - needed a more precise mechanism
		//if (_input.equals("std::cin >>")) _input = "getchar()";	// FIXME Better solution required
		if (_input.endsWith("std::cin >>")) {
			_input = _input.replace("std::cin >>", "getchar()");	// FIXME Better solution required
		}
		// END KGU#281 2016-10-15
		_input = _input.replace("<<  <<", "<<");
		// END KGU#108 2015-12-13

		return _input.trim();
	}
	// END KGU#101 2015-12-11
	
	// START KGU#311 2016-12-25/2017-01-05: Enh. #314: Replace all API names by prefixed ones
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#transformFileAPITokens(lu.fisch.utils.StringList)
	 */
	@Override
	protected void transformFileAPITokens(StringList tokens)
	{
		for (int i = 0; i < Executor.fileAPI_names.length; i++) {
			tokens.replaceAll(Executor.fileAPI_names[i], FILE_API_CLASS_NAME + "::" + Executor.fileAPI_names[i]);
		}
	}
	// END KGU#311 2016-12-25/2017-01-05
	
	// START KGU#16 2016-01-14
	@Override
	protected String transformType(String _type, String _default) {
		if (_type == null)
			_type = _default;
		// START KGU 2017-04-12: We must not generally flatten the case (consider user types!)
		//_type = _type.toLowerCase();
		//_type = _type.replace("integer", "int");
		//_type = _type.replace("real", "double");
		//_type = _type.replace("boolean", "bool");
		//_type = _type.replace("boole", "bool");
		//_type = _type.replace("character", "char");
		//_type = _type.replace("String", "string");
		//_type = _type.replace("char[]", "string");
		_type = _type.replaceAll("(^|.*\\W)(I" + BString.breakup("nt", true) + ")($|\\W.*)", "$1int$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("integer", true) + ")($|\\W.*)", "$1int$3");
		_type = _type.replaceAll("(^|.*\\W)(L" + BString.breakup("ong", true) + ")($|\\W.*)", "$1long$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("longint", true) + ")($|\\W.*)", "$1long$3");
		_type = _type.replaceAll("(^|.*\\W)(D" + BString.breakup("ouble", true) + ")($|\\W.*)", "$1double$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("real", true) + ")($|\\W.*)", "$1double$3");
		_type = _type.replaceAll("(^|.*\\W)(F" + BString.breakup("loat", true) + ")($|\\W.*)", "$1float$3");
		_type = _type.replaceAll("(^|.*\\W)" + BString.breakup("boolean", true) + "($|\\W.*)", "bool");
		_type = _type.replaceAll("(^|.*\\W)" + BString.breakup("boole", true) + "($|\\W.*)", "bool");
		_type = _type.replaceAll("(^|.*\\W)(B" + BString.breakup("ool", true) + ")($|\\W.*)", "$1bool$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("String", true) + ")($|\\W.*)", "$1string$3");
		_type = _type.replaceAll("(^|.*\\W)(C" + BString.breakup("har", true) + ")($|\\W.*)", "$1char$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("character", true) + ")($|\\W.*)", "$1char$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("char", true) + "\\[\\])($|\\W.*)", "$1string$3");
		// END KGU 2017-04-12
		return _type;
	}
	// END KGU#16 2016-01-14
	
	// START KGU#332 2017-04-12: Enh. #335
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#isInternalDeclarationAllowed()
	 */
	@Override
	protected boolean isInternalDeclarationAllowed()
	{
		// START KGU#501 2018-02-22: Bugfix #517
		//return true;
		return !isInitializingIncludes();
		// END KGU#501 2018-02-22
	}
	// END KGU#332 2017-04-12

	// START KGU#653 2019-02-14: Enh. #680
	/**
	 * Subclassable method possibly to obtain a suited transformed argument list string for the given series of
	 * input items (i.e. expressions designating an input target variable each) to be inserted in the input replacer
	 * returned by {@link #getInputReplacer(boolean)}, this allowing to generate a single input instruction only.<br/>
	 * This instance concatenates all elements with ">>" operators (as needed for std:::cin).
	 * @param _inputVarItems - {@link StringList} of variable descriptions for input
	 * @return either a syntactically converted combined string with suited operator or separator symbols, or null.
	 */
	@Override
	protected String composeInputItems(StringList _inputVarItems)
	{
		return _inputVarItems.concatenate(" >> ");
	}
	// END KGU#653 2019-02-14

// START KGU#542 2019-11-17: Enh. #739: This code was identical to that of super
//	// START KGU#388 2017-09-27: Enh.#423
//	/**
//	 * Adds a typedef for the type passed in by {@code _typeEnry} if it hadn't been defined
//	 * globally or in the preamble before.
//	 * @param _root - the originating Root
//	 * @param _type - the type map entry the definition for which is requested here
//	 * @param _indent - the current indentation
//	 * @param _asComment - if the type deinition is only to be added as comment (disabled)
//	 */
//	@Override
//	protected void generateTypeDef(Root _root, String _typeName, TypeMapEntry _type, String _indent, boolean _asComment) {
//		String typeKey = ":" + _typeName;
//		if (this.wasDefHandled(_root, typeKey, true)) {
//			return;
//		}
//		appendDeclComment(_root, _indent, typeKey);
//		if (_type.isRecord()) {
//			String indentPlus1 = _indent + this.getIndent();
//			addCode("struct " + _typeName + " {", _indent, _asComment);
//			for (Entry<String, TypeMapEntry> compEntry: _type.getComponentInfo(false).entrySet()) {
//				addCode(transformTypeFromEntry(compEntry.getValue(), _type) + "\t" + compEntry.getKey() + ";",
//						indentPlus1, _asComment);
//			}
//			addCode("};", _indent, _asComment);
//		}
//		else {
//			addCode("typedef " + this.transformTypeFromEntry(_type, null) + " " + _typeName + ";",
//					_indent, _asComment);					
//		}
//	}
//	// END KGU#388 2017-09-27
// END KGU#542 2019-11-17

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
		// We simply use the range-based loop of C++11 (should be long enough established)
		boolean done = false;
		String var = _for.getCounterVar();
		String valueList = _for.getValueList();
		StringList items = this.extractForInListItems(_for);
		if (items != null)
		{
			valueList = "{" + items.concatenate(", ") + "}";
		}

		// Creation of the loop header
		appendBlockHeading(_for, "for (auto " + var + " : " +
				transform(valueList, false) + ")", _indent);

		// Add the loop body as is
		generateCode(_for.q, _indent + this.getIndent());

		// Accomplish the loop
		appendBlockTail(_for, null, _indent);

		done = true;

		return done;
	}
	// END KGU#61 2016-03-22
	
	// START KGU#47/KGU#348 2017-02-21: Enh. #348 - Offer a C++11 solution with class std::thread
	@Override
	protected void generateCode(Parallel _para, String _indent)
	{

		boolean isDisabled = _para.isDisabled();
		Root root = Element.getRoot(_para);
		String indentPlusOne = _indent + this.getIndent();
		String suffix = Integer.toHexString(_para.hashCode());

		appendComment(_para, _indent);

		addCode("", "", isDisabled);
		appendComment("Parallel section", _indent);
		addCode("{", _indent, isDisabled);

		for (int i = 0; i < _para.qs.size(); i++) {
			Subqueue sq = _para.qs.get(i);
			String threadVar = "thr" + suffix + "_" + i;
			String threadFunc = "ThrFunc" + suffix + "_" + i;
			String threadFuncInst = threadFunc.toLowerCase();
			StringList used = root.getUsedVarNames(sq, false, false).reverse();
			StringList asgnd = root.getVarNames(sq, false, false).reverse();
			// We use all occurring variables as arguments, so we need the difference of
			// used and assigned variables to form the ordered union of both (first the
			// merely consumed variables, then those that might get modified).
			for (int v = 0; v < asgnd.count(); v++) {
				used.removeAll(asgnd.get(v));
			}
			String args = asgnd.concatenate(", ").trim();
			if (asgnd.count() > 0) { args = "(" + args + ")"; }
			addCode(threadFunc  + " " + threadFuncInst + args + ";", indentPlusOne, isDisabled);
			args = used.concatenate(", ").trim();
			addCode("std::thread " + threadVar + "(" + threadFuncInst + (args.isEmpty() ? "" : ", ") + args + ");", indentPlusOne, isDisabled);
			addCode("", _indent, isDisabled);
		}

		for (int i = 0; i < _para.qs.size(); i++) {
			String threadVar = "thr" + suffix + "_" + i;
			addCode(threadVar + ".join();", indentPlusOne, isDisabled);
		}

		addCode("}", _indent, isDisabled);
		addCode("", "", isDisabled);
	}

	// Adds class definitions for function objects to be used by the threads
	private void generateParallelThreadFunctions(Root _root, String _indent)
	{
		String indentPlusOne = _indent + this.getIndent();
		String indentPlusTwo = indentPlusOne + this.getIndent();
		final LinkedList<Parallel> containedParallels = new LinkedList<Parallel>();
		_root.traverse(new IElementVisitor() {
			@Override
			public boolean visitPreOrder(Element _ele) {
				return true;
			}
			@Override
			public boolean visitPostOrder(Element _ele) {
				if (_ele instanceof Parallel) {
					containedParallels.addLast((Parallel)_ele);
				}
				return true;
			}
		});
		for (Parallel par: containedParallels) {
			boolean isDisabled = par.isDisabled();
			String functNameBase = "ThrFunc" + Integer.toHexString(par.hashCode()) + "_";
			Root root = Element.getRoot(par);
			// START KGU#676 2019-03-30: Enh. #696 special pool in case of batch export
			//HashMap<String, TypeMapEntry> typeMap = root.getTypeInfo();
			HashMap<String, TypeMapEntry> typeMap = root.getTypeInfo(routinePool);
			// END KGU#676 2019-03-30
			int i = 0;
			// We still don't care for synchronisation, mutual exclusion etc.
			for (Subqueue sq: par.qs) {
				// Variables assigned here will be made reference members
				StringList setVars = root.getVarNames(sq, false).reverse();
				// Variables used here (without being assigned) will be made reference arguments
				StringList usedVars = root.getUsedVarNames(sq, false, false).reverse();
				String initList = "";
				for (int v = 0; v < setVars.count(); v++) {
					String varName = setVars.get(v);
					usedVars.removeAll(varName);
					initList += ", " + varName + "(" + varName + ")";
				}
				if (!initList.isEmpty()) {
					initList = ":" + initList.substring(1);
				}
				addCode("class " + functNameBase + i + "{", _indent, isDisabled);
				addCode("public:", _indent, isDisabled);
				// Member variables (all references!)
				String argList = this.makeArgList(setVars, typeMap);
				if (setVars.count() > 0) {
					String[] argDecls = argList.split(", ");
					for (String decl: argDecls) {
						addCode(decl + ";", indentPlusOne, isDisabled);
					}
					// Constructor
					addCode(functNameBase + i + "(" + argList + ") " + initList + "{}", indentPlusOne, isDisabled);
				}
				// Function operator
				argList = "(" + this.makeArgList(usedVars, typeMap) + ")";
				addCode("void operator()" + argList + " {", indentPlusOne, isDisabled);
				generateCode(sq, indentPlusTwo);
				addCode("}", indentPlusOne, isDisabled);
				addCode("};", _indent, isDisabled);
				addSepaLine();
				i++;
			}
		}
	}
	
	/**
	 * Generates an argument list for a worker thread routine as branch of a parallel section.
	 * Types for the variable names in {@code varNames} are retrieved from {@code typeMap}. If
	 * no associated type can be identified then a comment {@code "type?"} will be inserted.
	 * @param varNames - list of variable names to be passed in
	 * @param typeMap - maps variable names and type names to type specifications
	 * @return a list of argument declarations
	 */
	private String makeArgList(StringList varNames, HashMap<String, TypeMapEntry> typeMap)
	{
		String argList = "";
		for (int v = 0; v < varNames.count(); v++) {
			String varName = varNames.get(v);
			TypeMapEntry typeEntry = typeMap.get(varName);
			String typeSpec = "???";
			boolean isArray = false;
			if (typeEntry != null) {
				isArray = typeEntry.isArray();
				StringList typeSpecs = this.getTransformedTypes(typeEntry, false);
				if (typeSpecs.count() == 1) {
					// START KGU#784 2019-12-02
					//typeSpec = typeSpecs.get(0);
					typeSpec = this.transformTypeFromEntry(typeEntry, null);
					int posBrack = typeSpec.indexOf("[");
					if (posBrack > 0) {
						varName += typeSpec.substring(posBrack);
						typeSpec = typeSpec.substring(0, posBrack);
					}
					// END KGU#784 2019-12-02
				}
			}
			argList += (v > 0 ? ", " : "") + typeSpec + (isArray ? " " : "& ") + varName;
		}
		return argList;
	}
	// END KGU#47/KGU#348 2017-02-21
	
	// START KGU#686 2019-03-18: Enh. #56
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#makeExceptionFrom(java.lang.String)
	 */
	@Override
	protected void generateThrowWith(String _thrown, String _indent, boolean _asComment) {
		// If it isn't a rethrow then fake some text
		if (_thrown.isEmpty() && this.caughtException == null) {
			_thrown = "new string(\"unspecified error\")";
		}
		addCode(("throw " + _thrown).trim() + ";", _indent, _asComment);
	}
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#appendCatchHeading(lu.fisch.structorizer.elements.Try, java.lang.String)
	 */
	@Override
	protected void appendCatchHeading(Try _try, String _indent) {
		String varName = _try.getExceptionVarName();
		String head = "catch (...)";
		if (varName != null && !varName.isEmpty()) {
			head = "catch(string " + varName + ")";
		}
		this.appendBlockHeading(_try, head, _indent);
		this.caughtException = head;	// No matter what is contains but it must not be null
	}
	// END KGU#686 2019-03-18

	
// KGU#74 (2015-11-30): Now we only override some of the decomposed methods below
//    @Override
//    public String generateCode(Root _root, String _indent)
//    {
//        ...
//        return code.getText();
//    }
    
	/**
	 * Composes the heading for the program or function according to the
	 * C language specification.
	 * @param _root - The diagram root
	 * @param _indent - the initial indentation string
	 * @param _procName - the procedure name
	 * @param _paramNames - list of the argument names
	 * @param _paramTypes - list of corresponding type names (possibly null) 
	 * @param _resultType - result type name (possibly null)
	 * @param _public - whether the resulting routine is to be public
	 * @return the default indentation string for the subsequent stuff
	 */
	@Override
	protected String generateHeader(Root _root, String _indent, String _procName,
			StringList _paramNames, StringList _paramTypes, String _resultType, boolean _public)
	{
		// START KGU#852 2020-04-22: Since method appendDeclarations() does not overwrite typeMap anymore, we must set it
		this.typeMap = new LinkedHashMap<String, TypeMapEntry>(_root.getTypeInfo(routinePool));
		// END KGU#852 2020-04-22
		// START KGU#178 2016-07-20: Enh. #160
		if (topLevel)
		{
			// START KGU#815/KGU#824 2020-03-20: Enh. #828, bugfix #836
			if (this.isLibraryModule()) {
				appendScissorLine(true, this.pureFilename + ".h");
				String guardName = this.deriveIncludeGuardName(pureFilename + ".h");
				code.add("#pragma once");	// For the friends of Visual Studio ...
				code.add("#ifndef " + guardName);
				code.add("#define " + guardName);
			}
			// END KGU#815/KGU#824 2020-03-20: Enh. #828, bugfix #836
			appendComment("Generated by Structorizer " + Element.E_VERSION, _indent);
			// START KGU#363 2017-05-16: Enh. #372
			appendCopyright(_root, _indent, true);
			// END KGU#363 2017-05-16
			// START KGU#236 2016-08-10: Issue #227
			generatorIncludes.add("<string>");
			//code.add("#include <iostream>");
			// START KGU#236 2016-12-22: Issue #227: root-specific analysis needed
			//if (this.hasInput && this.hasOutput)
			if (this.hasInput() || this.hasOutput())
			// END KGU#236 2016-12-22
			{
				this.generatorIncludes.add("<iostream>");
			}
			// END KGU#236 2016-08-10
			// START KGU#348 2017-02-21: Enh. #348 Parallel support
			if (this.hasParallels) {
				this.generatorIncludes.add("<thread>");
			}
			this.appendGeneratorIncludes("", false);
			// END KGU#348 2017-02-21
			// START KGU#351 2017-02-26: Enh. #346
			this.appendUserIncludes("");
			// END KGU#351 2017-02-26
			code.add(_indent + "using std::string;");
			// START KGU#376 2017-09-27: Enh. #389 - definitions from all included diagrams will follow
			appendGlobalDefinitions(_root, _indent, true);
			// END KGU#376 2017-09-27
			// START KGU#311 2016-12-22: Enh. #314 - support for file API
			if (this.usesFileAPI) {
				// Adds the #include directive
				this.insertFileAPI("cpp", code.count(), "", 0);
			}
			// END KGU#311 2016-12-22

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

			subroutineInsertionLine = code.count();
			subroutineIndent = _indent;
			addSepaLine();
		}
		else
		{
			addSepaLine();
		}
		// END KGU#178 2016-07-20
		
		// START KGU#815 2020-03-20: Enh. #828
		// add comment
		//appendComment(_root, _indent);
		// END KGU#815 2020-03-20

		String pr = "program";
		if (_root.isSubroutine()) {
			pr = "function";
		} else if (_root.isInclude()) {
			pr = "includable";
		}
		appendComment(pr + " " + _root.getText().get(0), _indent);

		// START KGU#815 2020-03-20: Enh. #828 - signature code extracted to insertPrototype()
		this.insertPrototype(_root, _indent, !this.isLibraryModule(), code.count());
		// END KGU#815 2020-03-20
		// START KGU#815 2020-03-26: Enh. #828
		if (!topLevel && _public) {
			appendCopyright(_root, _indent, false);
		}
		// END KGU#815 2020-03-26
		
		code.add("{");

		// START KGU#376 2017-09-28: Enh. #389 - add the initialization code of the includables
		// START KGU#815/KGU#824/KGU#834 2020-03-26: Moved to generateBody(Root, _indent)
		//appendGlobalInitialisations(_indent + this.getIndent());
		// END KGU#815/KGU#824/KGU#834 2020-03-26
		// END KGU#376 2017-09-28

		return _indent + this.getIndent();
	}

// START KGU#332 2017-01-30: Method decomposed - no need to override it anymore
	@Override
	protected String generatePreamble(Root _root, String _indent, StringList varNames)
	{
		// START KGU#348 2017-02-21: Enh. #348 - Actual translation of Parallel sections
		this.generateParallelThreadFunctions(_root, _indent);
		// END KGU#348 2017-02-21
		return super.generatePreamble(_root, _indent, varNames);
	}
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#transformRecordTypeRef(java.lang.String, boolean)
	 */
	@Override
	protected String transformRecordTypeRef(String structName, boolean isRecursive) {
		return structName + (isRecursive ? " * " : "");
	}

	// START KGU#542 2019-11-17: Enh. #739
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#transformEnumTypeRef(java.lang.String)
	 */
	@Override
	protected String transformEnumTypeRef(String enumName) {
		return enumName;
	}
	// END KGU#542 2019-11-17

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#generateIOComment(lu.fisch.structorizer.elements.Root, java.lang.String)
	 */
	@Override
	protected void generateIOComment(Root _root, String _indent)
	{
		// Don't write anything
	}
// END KGU#332 2017-01-30
	
	// START KGU#311 2016-12-24: Enh. #314
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#copyFileAPIResources(java.lang.String)
	 */
	@Override
	protected boolean copyFileAPIResources(String _filePath)
	{
		boolean isDone1 = copyFileAPIResource("hpp", FILE_API_CLASS_NAME + ".h", _filePath);
		boolean isDone2 = copyFileAPIResource("cpp", FILE_API_CLASS_NAME +  ".cpp", _filePath);		
		return isDone1 && isDone2;
	}
	// END KGU#311 2016-12-24

}

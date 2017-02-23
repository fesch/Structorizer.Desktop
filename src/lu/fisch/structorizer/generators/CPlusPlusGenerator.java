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
 *      Kay Gürtzig     2010.08.31      First Issue
 *      Kay Gürtzig     2015.11.01      Adaptations to new decomposed preprocessing
 *      Kay Gürtzig     2015.11.30      Jump mechanisms (KGU#78) and root export revised 
 *      Kay Gürtzig     2015.12.11      Enh. #54 (KGU#101): Support for output expression lists
 *      Kay Gürtzig     2015.12.13      Bugfix #51 (=KGU#108): Cope with empty input and output
 *      Kay Gürtzig     2016.01.14      Type conversion of C overridden (KGU#16)
 *      Kay Gürtzig     2016.03.23      Enh. #84: Support for FOR-IN loops (KGU#61)
 *      Kay Gürtzig     2016.08.10      Issue #227: <iostream> only included if needed 
 *      Kay Gürtzig     2016.08.12      Enh. #231: Additions for Analyser checks 18 and 19 (variable name collisions)
 *      Kay Gürtzig     2016.09.25      Enh. #253: D7Parser.keywordMap refactoring done 
 *      Kay Gürtzig     2016.10.15      Enh. #271: Support for input instructions with prompt
 *      Kay Gürtzig     2016.11.08      Collateral damage of #271 to getOutputReplacer() mended
 *      Kay Gürtzig     2016.12.25      Enh. #314: Support for File API added.
 *      Kay Gürtzig     2017.01.05      Enh. #314: File API intervention in transformTokens modified
 *      Kay Gürtzig     2017.01.30      Enh. #259/#335: Type retrieval and improved declaration support 
 *      Kay Gürtzig     2017.01.31      Enh. #113: Array parameter transformation
 *      Kay Gürtzig     2017.02.21      Enh. #348: Parallel sections translated with <thread> library
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
import java.util.LinkedList;

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.IElementVisitor;
import lu.fisch.structorizer.elements.Parallel;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.structorizer.elements.TypeMapEntry;
import lu.fisch.structorizer.executor.Executor;
import lu.fisch.structorizer.parsers.D7Parser;
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

	// START KGU 2016-08-12: Enh. #231 - information for analyser
    private static final String[] reservedWords = new String[]{
		"auto", "break", "case", "char", "const", "continue",
		"default", "do", "double", "else", "enum", "extern",
		"float", "for", "goto", "if", "int", "long",
		"register", "return",
		"short", "signed", "sizeof", "static", "struct", "switch",
		"typedef", "union", "unsigned", "void", "volatile", "while",
		"asm", "bool", "catch", "calss", "const_cast", "delete", "dynamic_cast",
		"explicit", "false", "friend", "inline", "mutable", "namespace", "new", "nullptr",
		"operator", "private", "public", "protected", "reinterpret_cast",
		"staic_cast", "template", "this", "throw", "true", "try", "typeid", "typename",
		"using", "virtual", "wchar_t"};
	public String[] getReservedWords()
	{
		return reservedWords;
	}
	// END KGU 2016-08-12

	/************ Code Generation **************/
	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/**
	 * A pattern how to embed the variable (right-hand side of an input instruction)
	 * into the target code
	 * @param withPrompt - is a prompt string to be considered?
	 * @return a regex replacement pattern, e.g. "$1 = (new Scanner(System.in)).nextLine()"
	 */
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

	/**
	 * A pattern how to embed the expression (right-hand side of an output instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "System.out.println($1)"
	 */
	@Override
	protected String getOutputReplacer()
	{
		return "std::cout << $1 << std::endl";
	}


	// START KGU#101 2015-12-11: Enhancement #54: Cope with output expression lists
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transform(java.lang.String)
	 */
	@Override
	protected String transform(String _input)
	{
		// START KGU#101 2015-12-11: Enh. #54 - support lists of expressions
		String outputKey = D7Parser.getKeyword("output").trim();
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
			tokens.replaceAll(Executor.fileAPI_names[i], "StructorizerFileAPI::" + Executor.fileAPI_names[i]);
		}
	}
	// END KGU#311 2016-12-25/2017-01-05
	
	// START KGU#16 2016-01-14
	@Override
	protected String transformType(String _type, String _default) {
		if (_type == null)
			_type = _default;
		_type = _type.toLowerCase();
		_type = _type.replace("integer", "int");
		_type = _type.replace("real", "double");
		_type = _type.replace("boolean", "bool");
		_type = _type.replace("boole", "bool");
		_type = _type.replace("character", "char");
		_type = _type.replace("String", "string");
		_type = _type.replace("char[]", "string");
		return _type;
	}
	// END KGU#16 2016-01-14

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
		insertBlockHeading(_for, "for (auto " + var + " : " +
				transform(valueList, false) + ")", _indent);

		// Add the loop body as is
		generateCode(_for.q, _indent + this.getIndent());

		// Accomplish the loop
		insertBlockTail(_for, null, _indent);

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

		insertComment(_para, _indent);

		addCode("", "", isDisabled);
		insertComment("Parallel section", _indent);
		addCode("{", _indent, isDisabled);

		for (int i = 0; i < _para.qs.size(); i++) {
			Subqueue sq = _para.qs.get(i);
			String threadVar = "thr" + _para.hashCode() + "_" + i;
			String threadFunc = "ThrFunc" + _para.hashCode() + "_" + i;
			String threadFuncInst = threadFunc.toLowerCase();
			StringList used = root.getUsedVarNames(sq, false, false).reverse();
			StringList asgnd = root.getVarNames(sq, false, false).reverse();
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
			String threadVar = "thr" + _para.hashCode() + "_" + i;
			addCode(threadVar + ".join();", indentPlusOne, isDisabled);
		}

		addCode("}", _indent, isDisabled);
		addCode("", "", isDisabled);
	}

	// Inserts class definitions for function objects to be used by the threads
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
			String functNameBase = "ThrFunc" + par.hashCode() + "_";
			Root root = Element.getRoot(par);
			HashMap<String, TypeMapEntry> typeMap = root.getTypeInfo();
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
				code.add(_indent);
				i++;
			}
		}
	}
	
	private String makeArgList(StringList varNames, HashMap<String, TypeMapEntry> typeMap)
	{
		String argList = "";
		for (int v = 0; v < varNames.count(); v++) {
			String varName = varNames.get(v);
			TypeMapEntry typeEntry = typeMap.get(varName);
			String typeSpec = "/*type?*/";
			boolean isArray = false;
			if (typeEntry != null) {
				isArray = typeEntry.isArray();
				StringList typeSpecs = this.getTransformedTypes(typeEntry);
				if (typeSpecs.count() == 1) {
					typeSpec = typeSpecs.get(0);
				}
			}
			argList += (v > 0 ? ", " : "") + typeSpec + (isArray ? " " : "& ") + varName;
		}
		return argList;
	}
	// END KGU#47/KGU#348 2017-02-21
	
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
	 * @param paramNames - list of the argument names
	 * @param paramTypes - list of corresponding type names (possibly null) 
	 * @param resultType - result type name (possibly null)
	 * @return the default indentation string for the subsequent stuff
	 */
	@Override
	protected String generateHeader(Root _root, String _indent, String _procName,
			StringList _paramNames, StringList _paramTypes, String _resultType)
	{
		// START KGU#178 2016-07-20: Enh. #160
		if (topLevel)
		{
			insertComment("Generated by Structorizer " + Element.E_VERSION, _indent);
			// START KGU#236 2016-08-10: Issue #227
	        //code.add("#include <iostream>");
			// START KGU#236 2016-12-22: Issue #227: root-specific analysis needed
			//if (this.hasInput && this.hasOutput)
			if (this.hasInput() || this.hasOutput())
			// END KGU#236 2016-12-22
			{
				code.add("#include <iostream>");
			}
	        // END KGU#236 2016-08-10
			// START KGU#348 2017-02-21: Enh. #348 Parallel support
			if (this.hasParallels) {
				code.add("#include <thread>");
			}
			// END KGU#348 2017-02-21
			// START KGU#311 2016-12-22: Enh. #314 - support for file API
			if (this.usesFileAPI) {
		        this.insertFileAPI("cpp", code.count(), "", 0);
			}
			// END KGU#311 2016-12-22
	        subroutineInsertionLine = code.count();
	        subroutineIndent = _indent;
	        code.add("");
		}
		else
		{
			code.add("");
		}
		// END KGU#178 2016-07-20
		
        // add comment
    	insertComment(_root, _indent);

        String pr = _root.isProgram ? "program" : "function";
        insertComment(pr + " " + _root.getText().get(0), _indent);
        
        if (_root.isProgram)
        	code.add(_indent + "int main(void)");
        else {
        	// Start with the result type
			String fnHeader = transformType(_resultType,
					((returns || isResultSet || isFunctionNameSet) ? "int" : "void"));
			// START KGU#140 2017-01-31: Enh. #113 - improved type recognition and transformation
			boolean returnsArray = fnHeader.toLowerCase().contains("array") || fnHeader.contains("]");
			if (returnsArray) {
				fnHeader = transformArrayDeclaration(fnHeader, "");
			}
			// END KGU#140 2017-01-31
			fnHeader += " " + _procName + "(";
			for (int p = 0; p < _paramNames.count(); p++) {
				if (p > 0) { fnHeader += ", "; }
				// START KGU#140 2017-01-31: Enh. #113: Proper conversion of array types
				//fnHeader += (transformType(_paramTypes.get(p), "/*type?*/") + " " + 
				//		_paramNames.get(p)).trim();
				fnHeader += transformArrayDeclaration(transformType(_paramTypes.get(p), "/*type?*/").trim(), _paramNames.get(p));
				// END KGU#140 2017-01-31
			}
			fnHeader += ")";
			// END KGU 2015-11-29
			// START KGU#140 2017-01-31: Enh. #113
			if (returnsArray) {
				insertComment("      C++ may not permit to return arrays like this - find an other way to pass the result!", _indent);
			}
			// END KGU#140 2017-01-31
            insertComment("TODO Revise the return type and declare the parameters!", _indent);
            
        	code.add(fnHeader);
        }
		
		code.add("{");
		// START KGU#348 2017-02-21: Enh. #348 - Actual translation of Parallel sections
		this.generateParallelThreadFunctions(_root, _indent + this.getIndent());
		// END KGU#348 2017-02-21
		
		return _indent + this.getIndent();
	}

// START KGU#332 2017-01-30: Method decomposed - no need to override it anymore
//	/**
//	 * Generates some preamble (i.e. comments, language declaration section etc.)
//	 * and adds it to this.code.
//	 * @param _root - the diagram root element
//	 * @param _indent - the current indentation string
//	 * @param varNames - list of variable names introduced inside the body
//	 */
//	@Override
//	protected String generatePreamble(Root _root, String _indent, StringList varNames)
//	{
//		code.add(_indent);
//		insertComment("TODO: declare your variables here:", _indent);
//        // START KGU 2015-11-30: List the variables to be declared
//		for (int v = 0; v < varNames.count(); v++) {
//			insertComment(varNames.get(v), _indent);
//		}
//		// END KGU 2015-11-30
//		code.add(_indent);
//		return _indent;
//	}
	
	@Override
	protected void generateIOComment(Root _root, String _indent)
	{
		// Don't write anything
	}
// END KGU#332 2017-01-30
    
	// START KGU#311 2016-12-24: Enh. #314
	protected boolean copyFileAPIResources(String _filePath)
	{
		boolean isDone1 = copyFileAPIResource("hpp", "StructorizerFileAPI.h", _filePath);
		boolean isDone2 = copyFileAPIResource("cpp", "StructorizerFileAPI.cpp", _filePath);		
		return isDone1 && isDone2;
	}
	// END KGU#311 2016-12-24


}

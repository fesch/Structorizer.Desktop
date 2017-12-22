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
 *      Description:    This class generates C# code.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author         			Date			Description
 *      ------					----            -----------
 *      Bob Fisch               2008.11.17      First Issue
 *      Gunter Schillebeeckx    2010.08.07      C# Generator starting from C Generator & Java Generator
 *      Kay Gürtzig             2010.09.10      Bugfixes and cosmetics (see comment)
 *      Bob Fisch               2011.11.07      Fixed an issue while doing replacements
 *      Kay Gürtzig             2014.11.06      Support for logical Pascal operators added
 *      Kay Gürtzig             2014.11.16      Bugfixes and enhancements (see comment)
 *      Kay Gürtzig             2014.12.02      Additional replacement of long assignment operator "<--" by "<-"
 *      Kay Gürtzig             2015.10.18      Indentation fixed, comment insertion interface modified
 *      Kay Gürtzig             2015.11.01      Inheritance changed and unnecessary overridings disabled
 *      Kay Gürtzig             2015.11.30      Sensible handling of return and exit/break instructions
 *                                              (issue #22 = KGU#47)
 *      Kay Gürtzig             2016.03.23      Enh. #84: Support for FOR-IN loops (KGU#61) 
 *      Kay Gürtzig             2016-07-20      Enh. #160: Option to involve subroutines implemented (=KGU#178),
 *                                              brace balance in non-program files fixed  
 *      Kay Gürtzig             2016.08.12      Enh. #231: Additions for Analyser checks 18 and 19 (variable name collisions) 
 *      Kay Gürtzig             2016.10.14      Enh. #270: Handling of disabled elements (code.add(...) --> addCode(..))
 *      Kay Gürtzig             2016.10.15      Enh. #271: Support for input instructions with prompt
 *      Kay Gürtzig             2017.01.04      Bugfix #322: input and output code generation fixed 
 *      Kay Gürtzig             2017.01.30      Enh. #259/#335: Type retrieval and improved declaration support 
 *      Kay Gürtzig             2017.01.31      Enh. #113: Array parameter transformation
 *      Kay Gürtzig             2017.02.24      Enh. #348: Parallel sections translated with System.Threading
 *      Kay Gürtzig             2017.02.27      Enh. #346: Insertion mechanism for user-specific include directives
 *      Kay Gürtzig             2017.04.14      Enh. #335: Method isInternalDeclarationAllowed() duly overridden
 *      Kay Gürtzig             2017.05.16      Bugfix #51: Export of empty input instructions produced " = Console.ReadLine();"
 *      Kay Gürtzig             2017.05.16      Enh. #372: Export of copyright information
 *      Kay Gürtzig             2017.05.24      Bugfix: hashCode as suffix could get negative, therefore now hex string used
 *      Kay Gürtzig             2017.09.28      Enh. #389, #423: Update for record types and includable diagrams
 *      Kay Gürtzig             2017.12.22      KGU#482: Autodoc comment style changed from /**... to ///...
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *      2017-02-24 - Issue #348
 *      - The generator now translates Parallel sections in two phases:
 *        1. For each of the branches of a Parallel element a Worker class is generated, named Worker<id>_<i>
 *           where <id> is the hash code of the Parallel element and <i> is current branch number.
 *           The Worker class has all variables assigned to in the branch as public members and all variables
 *           merely used in the branch (without being assigned) as private members.
 *           The constructor initializes all private members via respective arguments. The public members are
 *           not automatically initialized.
 *           Method DoWork is the thread start method and obtains no argument. It contains the translated
 *           algorithm of the Parallel branch.
 *           The generated worker classes are placed within the program class before the Main method (if the top-level
 *           routine is a program).
 *        2. The Parallel element itself is setup as follows. Every branch is represented by:
 *           a) The declaration of a worker class instance;
 *           b) the declaration of a thread instance with the DoWork method as thread start delegate;
 *           c) the call of the Start() method of the thread.
 *           The Parallel element is terminated as follows:
 *           d) for every thrad the Join() method is called to wait for the termination of all threads.
 *           e) for every thread, all public members are assigned to the local variables of the same name. 
 *           
 *      2015-11-30 - Bugfix / enhancement #22 (KGU#47) <Kay Gürtzig>
 *      - The generator now checks in advance mechanisms of value return and premature exits in order
 *        to generate appropriate instructions
 *      - Also the analysis of routine arguments and return types was improved
 *      
 *      2015-11-01 - Code revision / enhancements <Kay Gürtzig>
 *      - Inheritance changed to CGenerator because most of the stuff is very similar.
 *      - Enhancement #10 (KGU#3): FOR loops now provide themselves more reliable loop parameters 
 *      - Enhancement KGU#15: Support for the gathering of several case values in CASE instructions
 *
 *      2015.10.18 - Bugfix
 *      - Indentation wasn't done properly (_indent+this.getIndent() works only for single-character indents)
 *      
 *      2014.11.16 - Bugfixes / Enhancements
 *      - conversion of comparison and logical operators had still been flawed
 *      - element comment export added
 *      
 *      2014.11.06 - Enhancement (Kay Gürtzig)
 *      - Pascal-style logical operators "and", "or", and "not" supported 
 *      
 *      2010.09.10 - Bugfixes
 *      - Code generator for the Case structure (switch) had missed to add the case keywords
 *      - Comparison and assignment operator conversion was incomplete
 *      - Missing parentheses around negated condition of "do while" added
 *      - logical flaw in the automatic addition of brackets for "if", "while", and "switch" mended
 *      - "cosmetic" changes to the block ends of "switch" and "do while" 
 *      		
 *      2010.08.07 - Bugfixes
 *      - none
 *
 ******************************************************************************************************///

import lu.fisch.utils.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.executor.Executor;
import lu.fisch.structorizer.parsers.CodeParser;


public class CSharpGenerator extends CGenerator 
{

	/************ Fields ***********************/
	protected String getDialogTitle()
	{
		return "Export C# ...";
	}

	protected String getFileDescription()
	{
		return "C# Source Code";
	}

	protected String[] getFileExtensions()
	{
		String[] exts = {"cs"};
		return exts;
	}

//	// START KGU 2016-08-12: Enh. #231 - information for analyser - obsolete since 3.27
//    private static final String[] reservedWords = new String[]{
//		"abstract", "as", "base", "bool", "break", "byte",
//		"case", "catch", "char", "checked", "class", "const", "continue",
//		"decimal", "default", "delegate", "do", "double",
//		"else", "enum", "event", "explicit", "extern",
//		"false", "finally", "fixed", "float", "for", "foreach", "goto",
//		"if", "implicit", "in", "int", "interface", "internal", "is",
//		"lock", "long", "namespace", "new", "null",
//		"object", "operator", "out", "override", "params", "private", "public",
//		"readonly", "ref", "return", "sbyte", "sealed", "short", "sizeof",
//		"stackalloc", "static", "string", "struct", "switch",
//		"this", "throw", "true", "try", "typeof",
//		"uint", "ulong", "unchecked", "unsafe", "ushort", "using",
//		"virtual", "void", "volatile", "while"};
//	public String[] getReservedWords()
//	{
//		return reservedWords;
//	}
//	// END KGU 2016-08-12
	
	// START KGU#348 2017-02-24: Enh. #348: Support for Parallel section translation
	private int subClassInsertionLine = 0;
	// END KGU#348 2017-02-24
	
	// START KGU#351 2017-02-26: Enh. #346 - include / import / uses config
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getIncludePattern()
	 */
	@Override
	protected String getIncludePattern()
	{
		return "using %;";
	}
	// END KGU#351 2017-02-26

	/************ Code Generation **************/

	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/**
	 * A pattern how to embed the variable (right-hand side of an input instruction)
	 * into the target code
	 * @param withPrompt - is a prompt string to be considered?
	 * @return a regex replacement pattern, e.g. "$1 = (new Scanner(System.in)).nextLine();"
	 */
	// START KGU#281 2016-10-15: Enh. #271 (support for input with prompt)
	//protected String getInputReplacer()
	//{
	//	return "Console.ReadLine($1)";
	//}
	protected String getInputReplacer(boolean withPrompt)
	{
		// START KGU##321 2017-01-04: Bugfix #322 had produced wrong syntax
		//if (withPrompt) {
		//	return "Console.Write($1); Console.ReadLine($2)";
		//}
		//return "Console.ReadLine($1)";
		if (withPrompt) {
			return "Console.Write($1); $2 = Console.ReadLine()";
		}
		return "$1 = Console.ReadLine()";
		// END KGU#321 2017-01-04
	}
	// END KGU#281 2016-10-15

	/**
	 * A pattern how to embed the expression (right-hand side of an output instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "System.out.println($1);"
	 */
	protected String getOutputReplacer()
	{
		return "Console.WriteLine($1)";
	}

	// START KGU#399 2017-05-16: Bugfix #51
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformInput(java.lang.String)
	 */
	protected String transformInput(String _interm)
	{
		String transf = super.transformInput(_interm);
		if (transf.trim().startsWith("= ")) {
			transf = transf.trim().substring(2);
		}
		return transf;
	}
	// END KGU#399 2017-05-16

	// START KGU#321 2017-01-04: Bugfix #322 - we must split the argument list
	/**
	 * Detects whether the given code line starts with the configured output keystring
	 * and if so replaces it according to the regex pattern provided by getOutputReplacer()
	 * @param _interm - a code line in intermediate syntax
	 * @return transformed output instruction or _interm unchanged
	 */
	@Override
	protected String transformOutput(String _interm)
	{
		String subst = getOutputReplacer();
		String subst0 = subst.replaceAll("Line", "");
		// Between the input keyword and the variable name there MUST be some blank...
		String keyword = CodeParser.getKeyword("output").trim();
		if (!keyword.isEmpty() && _interm.startsWith(keyword))
		{
			String matcher = Matcher.quoteReplacement(keyword);
			if (Character.isJavaIdentifierPart(keyword.charAt(keyword.length()-1)))
			{
				matcher = matcher + "[ ]";
			}

			// Start - BFI (#51 - Allow empty output instructions)
			if(!_interm.matches("^" + matcher + "(.*)"))
			{
				_interm += " ";
			}
			// End - BFI (#51)
			
			String argstr = _interm.replaceFirst("^" + matcher + "(.*)", "$1");
			StringList args = Element.splitExpressionList(argstr, ",");
			String result = "";
			for (int i = 0; i < args.count()-1; i++) {
				result += subst0.replace("$1", args.get(i).trim()) + "; ";
			}
			if (args.count() > 1) { 
				_interm = result + subst.replace("$1", args.get(args.count()-1));
			}
			else {
				_interm = _interm.replaceFirst("^" + matcher + "(.*)", subst);
			}
		}
		return _interm;
	}
	// END KGU#321 2017-01-04

	// START KGU#311 2017-01-05: Enh. #314 Don't do what the parent does.
	@Override
	protected void transformFileAPITokens(StringList tokens)
	{
		for (int i = 0; i < Executor.fileAPI_names.length; i++) {
			tokens.replaceAll(Executor.fileAPI_names[i], "StructorizerFileAPI." + Executor.fileAPI_names[i]);
		}
	}
	// END KGU#311 2017-01-05

	// START KGU#351 2017-02-26: Enh. #346 - include / import / uses config
	/**
	 * Method preprocesses an include file name for the #include
	 * clause. This version surrounds a string not enclosed in angular
	 * brackets by quotes.
	 * @param _includeFileName a string from the user include configuration
	 * @return the preprocessed string as to be actually inserted
	 */
	protected String prepareIncludeItem(String _includeFileName)
	{
		return _includeFileName;
	}
	// END KGU#351 2017-02-26

	// START KGU#16/#47 2015-11-30
	/**
	 * Instruction to create a language-specific exit instruction (subclassable)
	 * The exit code will be passed to the generated code.
	 */
	@Override
	protected void insertExitInstr(String _exitCode, String _indent, boolean isDisabled)
	{
		Jump dummy = new Jump();
		insertBlockHeading(dummy, "if (System.Windows.Forms.Application.MessageLoop)", _indent); 
		insertComment("WinForms app", _indent + this.getIndent());
		addCode(this.getIndent() + "System.Windows.Forms.Application.Exit();", _indent, isDisabled);
		insertBlockTail(dummy, null, _indent);

		insertBlockHeading(dummy, "else", _indent); 
		insertComment("Console app", _indent + this.getIndent());
		addCode(this.getIndent() + "System.Environment.Exit(" + _exitCode + ");", _indent, isDisabled);
		insertBlockTail(dummy, null, _indent);
	}
	// END KGU#16/#47 2015-11-30

	// START KGU#332 2017-04-14: Enh. #335
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#isInternalDeclarationAllowed()
	 */
	@Override
	protected boolean isInternalDeclarationAllowed()
	{
		return true;
	}
	// END KGU#332 2017-04-14

	// START KGU#388 2017-09-28: Enh. #423
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#transformRecordInit(java.lang.String, lu.fisch.structorizer.elements.TypeMapEntry)
	 */
	@Override
	protected String transformRecordInit(String constValue, TypeMapEntry typeInfo) {
		// This is practically identical to Java
		HashMap<String, String> comps = Instruction.splitRecordInitializer(constValue);
		LinkedHashMap<String, TypeMapEntry> compInfo = typeInfo.getComponentInfo(true);
		String recordInit = "new " + typeInfo.typeName + "(";
		boolean isFirst = true;
		for (Entry<String, TypeMapEntry> compEntry: compInfo.entrySet()) {
			String compName = compEntry.getKey();
			String compVal = comps.get(compName);
			if (isFirst) {
				isFirst = false;
			}
			else {
				recordInit += ", ";
			}
			if (!compName.startsWith("§")) {
				if (compVal == null) {
					recordInit += "null";
				}
				else if (compEntry.getValue().isRecord()) {
					recordInit += transformRecordInit(compVal, compEntry.getValue());
				}
				else {
					recordInit += transform(compVal);
				}
			}
		}
		recordInit += ")";
		return recordInit;
	}

	/**
	 * Generates code that either allows direct assignment or decomposes the record
	 * initializer into separate component assignments
	 * @param _lValue - the left side of the assignment (without modifiers!)
	 * @param _recordValue - the record initializier according to Structorizer syntax
	 * @param _indent - current indentation level (as String)
	 * @param _isDisabled - indicates whether the code is o be commented out
	 */
	protected void generateRecordInit(String _lValue, String _recordValue, String _indent, boolean _isDisabled) {
		// This is practically identical to Java
		HashMap<String, String> comps = Instruction.splitRecordInitializer(_recordValue);
		String typeName = comps.get("§TYPENAME§");
		TypeMapEntry recordType = null;
		if (typeName == null || (recordType = typeMap.get(":"+typeName)) == null || !recordType.isRecord()) {
			// Just decompose it (requires that the target variable has been initialized before).
			super.generateRecordInit(_lValue, _recordValue, _indent, _isDisabled);
		}
		// This way has the particular advantage not to fail with an uninitialized variable (important for Java!). 
		addCode(_lValue + " = " + this.transformRecordInit(_recordValue, recordType) + ";", _indent, _isDisabled);
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#generateTypeDef(lu.fisch.structorizer.elements.Root, java.lang.String, lu.fisch.structorizer.elements.TypeMapEntry, java.lang.String, boolean)
	 */
	@Override
	protected void generateTypeDef(Root _root, String _typeName, TypeMapEntry _type, String _indent, boolean _asComment) {
		String typeKey = ":" + _typeName;
		if (this.wasDefHandled(_root, typeKey, true)) {
			return;
		}
		insertDeclComment(_root, _indent, typeKey);
		if (_type.isRecord()) {
			String indentPlus1 = _indent + this.getIndent();
			String indentPlus2 = indentPlus1 + this.getIndent();
			addCode("public struct " + _typeName + "{", _indent, _asComment);
			boolean isFirst = true;
			StringBuffer constructor = new StringBuffer();
			StringList constrBody = new StringList();
			constructor.append("public " + _typeName + "(");
			for (Entry<String, TypeMapEntry> compEntry: _type.getComponentInfo(false).entrySet()) {
				String compName = compEntry.getKey();
				String typeStr = transformTypeFromEntry(compEntry.getValue(), null);
				addCode("public " + typeStr + "\t" + compName + ";",
						indentPlus1, _asComment);
				if (!isFirst) constructor.append(", ");
				constructor.append(typeStr + " p_" + compName);
				constrBody.add(compName + " = p_" + compName + ";");
				isFirst = false;
			}
			constructor.append(")");
			addCode(constructor.toString(), indentPlus1, _asComment);
			addCode("{", indentPlus1, _asComment);
			for (int i = 0; i < constrBody.count(); i++) {
				addCode(constrBody.get(i), indentPlus2, _asComment);
			}
			addCode("}", indentPlus1, _asComment);
			addCode("};", _indent, _asComment);
		}
		else {
			// FIXME: What do we here in C#? This must be placed at another position
			addCode("using "  + _typeName + " = " + this.transformTypeFromEntry(_type, null) + ";",
					_indent, true);
		}
	}
	// END KGU#388 2017-09-28

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
		boolean isDisabled = _for.isDisabled();
		// We simply use the range-based loop of Java (as far as possible)
		String var = _for.getCounterVar();
		String valueList = _for.getValueList();
		StringList items = this.extractForInListItems(_for);
		String indent = _indent;
		String itemType = null;
		if (items != null)
		{
			valueList = "{" + items.concatenate(", ") + "}";
			// Good question is: how do we guess the element type and what do we
			// do if items are heterogeneous? We will just try four ways: int,
			// double, String, and derived type name. If none of them match we use
			// Object and add a TODO comment.
			int nItems = items.count();
			boolean allInt = true;
			boolean allDouble = true;
			boolean allString = true;
			// START KGU#388 2017-09-28: Enh. #423
			boolean allCommon = true;
			String commonType = null;
			// END KGU#388 2017-09-28
			for (int i = 0; i < nItems; i++)
			{
				String item = items.get(i);
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
				if (allDouble)
				{
					try {
						Double.parseDouble(item);
					}
					catch (NumberFormatException ex)
					{
						allDouble = false;
					}
				}
				if (allString)
				{
					allString = item.startsWith("\"") && item.endsWith("\"") &&
							!item.substring(1, item.length()-1).contains("\"");
				}
			}
			// START KGU#388 2017-09-28: Enh. #423
			//if (allInt) itemType = "int";
			if (allCommon) itemType = commonType;
			else if (allInt) itemType = "int";
			// END KGU#388 2017-09-28
			else if (allDouble) itemType = "double";
			else if (allString) itemType = "char*";
			String arrayName = "array20160322";
			
			// Extra block to encapsulate the additional variable declarations
			addCode("{", _indent , isDisabled);
			indent += this.getIndent();
			
			if (itemType == null)
			{
				itemType = "object";
				this.insertComment("TODO: Find a more specific item type than object and/or prepare the elements of the array", indent);
				
			}
			addCode(itemType + "[] " + arrayName + " = " + transform(valueList, false) + ";", indent, isDisabled);
			
			valueList = arrayName;
		}
		else
		{
			// START KGU#388 2017-09-28 #423
			//itemType = "Object";
			//this.insertComment("TODO: Select a more sensible item type than Object and/or prepare the elements of the array", indent);
			TypeMapEntry listType = this.typeMap.get(valueList);
			if (listType != null && listType.isArray() && (itemType = listType.getCanonicalType(true, false)) != null
					&& itemType.startsWith("@"))
			{
				itemType = this.transformType(itemType.substring(1), "Object");	
			}
			else {
				itemType = "Object";
				this.insertComment("TODO: Select a more sensible item type than Object and/or prepare the elements of the array", indent);
			}
			// END KGU#388 2017-09-28
			valueList = transform(valueList, false);
		}

		// Creation of the loop header
		insertBlockHeading(_for, "foreach (" + itemType + " " + var + " in " +	valueList + ")", indent);

		// Add the loop body as is
		generateCode(_for.q, indent + this.getIndent());

		// Accomplish the loop
		insertBlockTail(_for, null, indent);

		if (items != null)
		{
			addCode("}", _indent, isDisabled);
		}
		
		return true;
	}
	// END KGU#61 2016-03-22

	// START KGU#47/KGU#348 2017-02-24: Enh. #348 - Offer a C# solution with class Thread
	@Override
	protected void generateCode(Parallel _para, String _indent)
	{

		boolean isDisabled = _para.isDisabled();
		Root root = Element.getRoot(_para);
		String indentPlusOne = _indent + this.getIndent();
		String suffix = Integer.toHexString(_para.hashCode());

		insertComment(_para, _indent);

		addCode("", "", isDisabled);
		insertComment("==========================================================", _indent);
		insertComment("================= START PARALLEL SECTION =================", _indent);
		insertComment("==========================================================", _indent);
		addCode("{", _indent, isDisabled);
		
		StringList[] asgndVars = new StringList[_para.qs.size()];

		for (int i = 0; i < _para.qs.size(); i++) {
			Subqueue sq = _para.qs.get(i);
			String threadVar = "thr" + suffix + "_" + i;
			String worker = "Worker" + suffix + "_" + i;
			String workerInst = worker.toLowerCase();
			StringList usedVars = root.getUsedVarNames(sq, false, false).reverse();
			asgndVars[i] = root.getVarNames(sq, false, false).reverse();
			for (int v = 0; v < asgndVars[i].count(); v++) {
				usedVars.removeAll(asgndVars[i].get(v));
			}
			
			String args = "(" + usedVars.concatenate(", ").trim() + ")";
			addCode(worker  + " " + workerInst + " = new " + worker + args + ";", indentPlusOne, isDisabled);
			addCode("Thread " + threadVar + " = new Thread(" + workerInst + ".DoWork" + ");", indentPlusOne, isDisabled);
			addCode(threadVar + ".Start();", indentPlusOne, isDisabled);
			addCode("", _indent, isDisabled);
		}

		for (int i = 0; i < _para.qs.size(); i++) {
			String threadVar = "thr" + suffix + "_" + i;
			addCode(threadVar + ".Join();", indentPlusOne, isDisabled);
		}
		
		for (int i = 0; i < _para.qs.size(); i++) {
			for (int j = 0; j < asgndVars[i].count(); j++) {
				String workerInst = "worker" + suffix + "_" + i;
				String varName = asgndVars[i].get(j);
				addCode(varName + " = " + workerInst + "." + varName + ";", indentPlusOne, isDisabled);
			}
		}

		addCode("}", _indent, isDisabled);
		insertComment("==========================================================", _indent);
		insertComment("================== END PARALLEL SECTION ==================", _indent);
		insertComment("==========================================================", _indent);
		addCode("", "", isDisabled);
	}

	// Inserts class definitions for workers to be used by the threads to this.subClassDefinitions
	private StringList generateParallelThreadWorkers(Root _root, String _indent)
	{
		StringList codeBefore = this.code;
		StringList workerDefinitions = new StringList();
		this.code = workerDefinitions;
		try {
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
			if (!containedParallels.isEmpty()) {
				insertComment("=========== START PARALLEL WORKER DEFINITIONS ============", _indent);
			}
			for (Parallel par: containedParallels) {
				boolean isDisabled = par.isDisabled();
				String workerNameBase = "Worker" + Integer.toHexString(par.hashCode()) + "_";
				Root root = Element.getRoot(par);
				HashMap<String, TypeMapEntry> typeMap = root.getTypeInfo();
				int i = 0;
				// We still don't care for synchronisation, mutual exclusion etc.
				for (Subqueue sq: par.qs) {
					String worker = workerNameBase + i;
					// Variables assigned here will be made public members
					StringList setVars = root.getVarNames(sq, false).reverse();
					// Variables used here (without being assigned) will be made private members and constructor arguments
					StringList usedVars = root.getUsedVarNames(sq, false, false).reverse();
					for (int v = 0; v < setVars.count(); v++) {
						String varName = setVars.get(v);
						usedVars.removeAll(varName);
					}
					if (i > 0) {
						code.add(_indent);
					}
					addCode("class " + worker + "{", _indent, isDisabled);
					if (setVars.count() > 0 || usedVars.count() > 0) {
						insertComment("TODO: Check and accomplish the member declarations here", indentPlusOne);
					}
					if (setVars.count() > 0) {
						insertComment("TODO: Maybe you must care for an initialization of the public members, too", indentPlusOne);
					}
					StringList argList = this.makeArgList(setVars, typeMap);
					for (int j = 0; j < argList.count(); j++) {
						addCode("public " + argList.get(j) + ";", indentPlusOne, isDisabled);
					}
					argList = this.makeArgList(usedVars, typeMap);
					for (int j = 0; j < argList.count(); j++) {
						addCode("private " + argList.get(j) + ";", indentPlusOne, isDisabled);
					}
					// Constructor
					addCode("public " + worker + "(" + argList.concatenate(", ") + ")", indentPlusOne, isDisabled);
					addCode("{", indentPlusOne, isDisabled);
					for (int j = 0; j < usedVars.count(); j++) {
						String memberName = usedVars.get(j);
						addCode("this." + memberName + " = " + memberName + ";", indentPlusTwo, isDisabled);
					}
					addCode("}", indentPlusOne, isDisabled);
					// Work method
					addCode("public void DoWork()", indentPlusOne, isDisabled);
					addCode("{", indentPlusOne, isDisabled);
					generateCode(sq, indentPlusTwo);
					addCode("}", indentPlusOne, isDisabled);
					addCode("};", _indent, isDisabled);
					i++;
				}
			}
			if (!containedParallels.isEmpty()) {
				insertComment("============ END PARALLEL WORKER DEFINITIONS =============", _indent);
			}
			code.add(_indent);
		}
		finally {
			this.code = codeBefore;
		}
		return workerDefinitions;
	}
	
	private StringList makeArgList(StringList varNames, HashMap<String, TypeMapEntry> typeMap)
	{
		StringList argList = new StringList();
		for (int v = 0; v < varNames.count(); v++) {
			String varName = varNames.get(v);
			TypeMapEntry typeEntry = typeMap.get(varName);
			String typeSpec = "/*type?*/";
			if (typeEntry != null) {
				StringList typeSpecs = this.getTransformedTypes(typeEntry, false);
				if (typeSpecs.count() == 1) {
					typeSpec = typeSpecs.get(0);
				}
			}
			argList.add(typeSpec + " " + varName);
		}
		return argList;
	}
	// END KGU#47/KGU#348 2017-02-24

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
		String indentPlus1 = _indent + this.getIndent();
		String indentPlus2 = indentPlus1 + this.getIndent();
		// START KGU#178 2016-07-20: Enh. #160
		if (topLevel)
		{
			insertComment("Generated by Structorizer " + Element.E_VERSION, _indent);
			// START KGU#363 2017-05-16: Enh. #372
			insertCopyright(_root, _indent, true);
			// END KGU#363 2017-05-16
			// START KGU#376 2017-09-28: Enh. #389 - definitions from all included diagrams will follow
			if (!_root.isProgram()) {
				insertGlobalDefinitions(_root, indentPlus1, true);
			}
			// END KGU#376 2017-09-28
			code.add("");
			subroutineInsertionLine = code.count();	// default position for subroutines
			subroutineIndent = _indent;
		}
		else
		{
			code.add("");
		}
		// END KGU#178 2016-07-20
		
		if (_root.isProgram()) {
			code.add(_indent + "using System;");
			// START KGU#348 2017-02-24: Enh. #348
			if (this.hasParallels) {
				code.add(_indent + "using System.Threading;");
			}
			// END KGU#348 2017-02-24
			// STARTB KGU#351 2017-02-26: Enh. #346
			this.insertUserIncludes(_indent);
			// END KGU#351 2017-02-26
			code.add(_indent + "");
			// START KGU 2015-10-18
			insertBlockComment(_root.getComment(), _indent, "/// <summary>", "/// ", "/// </summary>");
			// END KGU 2015-10-18

			insertBlockHeading(_root, "public class "+ _procName, _indent);
			code.add(_indent);
			// START KGU#348 2017-02-24: Enh.#348
			this.subClassInsertionLine = code.count();
			// END KGU#348 2017-02-24
			// START KGU#311 2017-01-05: Enh. #314 File API
			if (this.usesFileAPI) {
				this.insertFileAPI("cs", code.count(), _indent, 0);
				code.add(_indent);
			}
			// END KU#311 2017-01-05
			// START KGU#376 2017-09-28: Enh. #389 - definitions from all included diagrams will follow
			//insertComment("TODO Declare and initialise class variables here", indentPlus1);
			insertGlobalDefinitions(_root, indentPlus1, true);
			// END KGU#376 2017-09-28
			code.add(_indent);
			code.add(indentPlus1 + "/// <param name=\"args\"> array of command line arguments </param>");
			insertBlockHeading(_root, "public static void Main(string[] args)", indentPlus1);
			code.add("");
		}
		else {
			// START KGU#311 2017-01-05: Enh. #314 File API
			if (this.topLevel && this.usesFileAPI) {
				this.insertFileAPI("cs", code.count(), _indent, 0);
				code.add(indentPlus1);
			}
			// END KU#311 2017-01-05
			// START KGU#348 2017-02-24: Enh.#348
			if (this.topLevel) {
				this.subClassInsertionLine = code.count();
			}
			// END KGU#348 2017-02-24
			insertBlockComment(_root.getComment(), indentPlus1, "/// <summary>", "/// ", "/// </summary>");
			for (String param: _paramNames.toArray()) {
				code.add(indentPlus1 + "/// <param name=\"" + param + "\"> TODO </param>");
			}
			if (_resultType != null || this.returns || this.isFunctionNameSet || this.isResultSet)
			{
				code.add(indentPlus1 + "/// <return> TODO </return>");
				_resultType = transformType(_resultType, "int");
				// START KGU#140 2017-01-31: Enh. #113 - Converts possible array notations
				_resultType = transformArrayDeclaration(_resultType, "");
				// END KGU#140 2017-01-31
			}
			else
			{
				_resultType = "void";
			}
			// START KGU#178 2016-07-20: Enh. #160 - insert called subroutines as private
			//String fnHeader = "public static " + _resultType + " " + _procName + "(";
			String fnHeader = (topLevel ? "public" : "private") + " static "
					+ _resultType + " " + _procName + "(";
			// END KGU#178 2016-07-20
			for (int p = 0; p < _paramNames.count(); p++) {
				if (p > 0) { fnHeader += ", "; }
				// START KGU#140 2017-01-31: Enh. #113: Proper conversion of array types
				//fnHeader += (transformType(_paramTypes.get(p), "/*type?*/") + " " + 
				//		_paramNames.get(p)).trim();
				fnHeader += transformArrayDeclaration(transformType(_paramTypes.get(p), "/*type?*/").trim(), _paramNames.get(p));
				// END KGU#140 2017-01-31
			}
			fnHeader += ")";
			insertBlockHeading(_root, fnHeader, indentPlus1);
		}

		// START KGU#376 2017-09-26: Enh. #389 - insert the initialization code of the includables
		insertGlobalInitialisations(indentPlus2);
		// END KGU#376 2017-09-26

		// START KGU#348 2017-02-24: Enh. #348 - Actual translation of Parallel sections
		StringList workers = this.generateParallelThreadWorkers(_root, indentPlus1);
		boolean moveSubroutineInsertions = this.subroutineInsertionLine > this.subClassInsertionLine;
		for (int i = 0; i < workers.count(); i++) {
			this.code.insert(workers.get(i), this.subClassInsertionLine++);
			if (moveSubroutineInsertions) this.subroutineInsertionLine++;
		}
		// END KGU#348 2017-02-24
		
		return indentPlus2;
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
//		code.add("");
//		// Variable declaration proposals (now with all used variables listed)
//		insertComment("TODO: Declare local variables here:", _indent);
//		for (int v = 0; v < varNames.count(); v++)
//		{
//			insertComment(varNames.get(v), _indent);
//		}
//		code.add("");
//		return _indent;
//	}
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#transformRecordTypeRef(java.lang.String, boolean)
	 */
	@Override
	protected String transformRecordTypeRef(String structName, boolean isRecursive) {
		return structName;
	}

	@Override
	protected String makeArrayDeclaration(String _elementType, String _varName, TypeMapEntry typeInfo)
	{
		while (_elementType.startsWith("@")) {
			_elementType = _elementType.substring(1) + "[]";
		}
		return (_elementType + " " + _varName).trim(); 
	}
	@Override
	protected void generateIOComment(Root _root, String _indent)
	{
		// START KGU#236 2016-12-22: Issue #227
		if (this.hasInput(_root)) {
			code.add(_indent);
			insertComment("TODO: You may have to modify input instructions,", _indent);			
			insertComment("      possibly by enclosing Console.ReadLine() calls with Parse methods", _indent);
			insertComment("      according to the variable type, e.g. \"i = int.Parse(Console.ReadLine());\".", _indent);			
		}
		// END KGU#236 2016-12-22
	}
// END KGU#332 2017-01-30

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
		if ((returns || _root.getResultType() != null || isFunctionNameSet || isResultSet) && !alwaysReturns)
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
			code.add(_indent);
			code.add(_indent + "return " + result + ";");
		}
		return _indent;
	}

	// START KGU 2015-12-15: Method block must be closed as well
	/**
	 * Method is to finish up after the text insertions of the diagram, i.e. to close open blocks etc. 
	 * @param _root - the diagram root element 
	 * @param _indent - the current indentation string
	 */
	@Override
	protected void generateFooter(Root _root, String _indent)
	{
		// Method block close
		super.generateFooter(_root, _indent + this.getIndent());

		if (_root.isProgram())
		{
			// START KGU#178 2016-07-20: Enh. #160
			// Modify the subroutine insertion position
			subroutineInsertionLine = code.count();
			// END KGU#178 2016-07-20
			
			// Close class block
			code.add("");
			code.add(_indent + "}");
		}
	}
	// END KGU 2015-12-15
    	
}

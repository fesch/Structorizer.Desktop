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
 *      Author                  Date            Description
 *      ------                  ----            -----------
 *      Bob Fisch               2008-11-17      First Issue
 *      Gunter Schillebeeckx    2010-08-07      C# Generator starting from C Generator & Java Generator
 *      Kay Gürtzig             2010-09-10      Bugfixes and cosmetics (see comment)
 *      Bob Fisch               2011-11-07      Fixed an issue while doing replacements
 *      Kay Gürtzig             2014-11-06      Support for logical Pascal operators added
 *      Kay Gürtzig             2014-11-16      Bugfixes and enhancements (see comment)
 *      Kay Gürtzig             2014-12-02      Additional replacement of long assignment operator "<--" by "<-"
 *      Kay Gürtzig             2015-10-18      Indentation fixed, comment insertion interface modified
 *      Kay Gürtzig             2015-11-01      Inheritance changed and unnecessary overridings disabled
 *      Kay Gürtzig             2015-11-30      Sensible handling of return and exit/break instructions
 *                                              (issue #22 = KGU#47)
 *      Kay Gürtzig             2016-03-23      Enh. #84: Support for FOR-IN loops (KGU#61) 
 *      Kay Gürtzig             2016-07-20      Enh. #160: Option to involve subroutines implemented (=KGU#178),
 *                                              brace balance in non-program files fixed  
 *      Kay Gürtzig             2016-08-12      Enh. #231: Additions for Analyser checks 18 and 19 (variable name collisions) 
 *      Kay Gürtzig             2016-10-14      Enh. #270: Handling of disabled elements (code.add(...) --> addCode(..))
 *      Kay Gürtzig             2016-10-15      Enh. #271: Support for input instructions with prompt
 *      Kay Gürtzig             2017-01-04      Bugfix #322: input and output code generation fixed 
 *      Kay Gürtzig             2017-01-30      Enh. #259/#335: Type retrieval and improved declaration support 
 *      Kay Gürtzig             2017-01-31      Enh. #113: Array parameter transformation
 *      Kay Gürtzig             2017-02-24      Enh. #348: Parallel sections translated with System.Threading
 *      Kay Gürtzig             2017-02-27      Enh. #346: Insertion mechanism for user-specific include directives
 *      Kay Gürtzig             2017-04-14      Enh. #335: Method isInternalDeclarationAllowed() duly overridden
 *      Kay Gürtzig             2017-05-16      Bugfix #51: Export of empty input instructions produced " = Console.ReadLine();"
 *      Kay Gürtzig             2017-05-16      Enh. #372: Export of copyright information
 *      Kay Gürtzig             2017-05-24      Bugfix: hashCode as suffix could get negative, therefore now hex string used
 *      Kay Gürtzig             2017-09-28      Enh. #389, #423: Update for record types and includable diagrams
 *      Kay Gürtzig             2017-12-22      Issue #496: Autodoc comment style changed from /**... to ///...
 *      Kay Gürtzig             2018-02-22      Bugfix #517: Declarations/initializations from includables weren't handled correctly 
 *      Kay Gürtzig             2018-07-21      Ebh. #563 (smarter record initializers), bugfix #564 (array initializer trouble)
 *      Kay Gürtzig             2019-02-14      Enh. #680: Support for input instructions with several variables
 *      Kay Gürtzig             2019-03-08      Enh. #385: Support for parameter default values
 *      Kay Gürtzig             2019-03-20      Enh. #56: Export of Try elements and support of throw Jumps
 *      Kay Gürtzig             2019-03-30      Issue #696: Type retrieval had to consider an alternative pool
 *      Kay Gürtzig             2019-10-02      Bugfix #755: Defective conversion of For-In loops with explicit array initializer
 *      Kay Gürtzig             2019-10-03      Bugfix #755: Further provisional fixes for nested Array initializers
 *      Kay Gürtzig             2020-02-15      KGU#801: Correction in generateParallelThreadWorkers() (had inflated the header)
 *      Kay Gürtzig             2020-03-17      Enh. #828: New configuration method prepareGeneratorIncludeItem()
 *      Kay Gürtzig             2020-03-27      Enh. #828: Group export support accomplished
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
 *      2015-10-18 - Bugfix
 *      - Indentation wasn't done properly (_indent+this.getIndent() works only for single-character indents)
 *      
 *      2014-11-16 - Bugfixes / Enhancements
 *      - conversion of comparison and logical operators had still been flawed
 *      - element comment export added
 *      
 *      2014-11-06 - Enhancement (Kay Gürtzig)
 *      - Pascal-style logical operators "and", "or", and "not" supported 
 *      
 *      2010-09-10 - Bugfixes
 *      - Code generator for the Case structure (switch) had missed to add the case keywords
 *      - Comparison and assignment operator conversion was incomplete
 *      - Missing parentheses around negated condition of "do while" added
 *      - logical flaw in the automatic addition of brackets for "if", "while", and "switch" mended
 *      - "cosmetic" changes to the block ends of "switch" and "do while" 
 *      		
 *      2010-08-07 - Bugfixes
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
import lu.fisch.structorizer.generators.Generator.TryCatchSupportLevel;
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
		return TryCatchSupportLevel.TC_TRY_CATCH_FINALLY;
	}
	// END KGU#686 2019-03-18

//	// START KGU 2016-08-12: Enh. #231 - information for analyser - obsolete since 3.27
//	private static final String[] reservedWords = new String[]{
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

	// START KGU#560 2018-07-22 Bugfix #564
	@Override
	protected boolean wantsSizeInArrayType()
	{
		return false;
	}
	// END KGU#560 2018-07-22

	@Override
	protected boolean arrayBracketsAtTypeName()
	{
		return true;
	}

	// START KGU#815/KGU#824 2020-03-19: Enh. #828, bugfix #836
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generatesClass()
	 */
	@Override
	protected boolean allowsMixedModule()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#insertPrototype(lu.fisch.structorizer.elements.Root, java.lang.String, boolean, int)
	 */
	@Override
	protected int insertPrototype(Root _root, String _indent, boolean _withComment, int _atLine)
	{
		// We don't need prototypes
		return 0;
	}
	// END KGU#815/KGU#824 2020-03-19
	

	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#getInputReplacer(boolean)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#getOutputReplacer()
	 */
	@Override
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
			tokens.replaceAll(Executor.fileAPI_names[i], FILE_API_CLASS_NAME + "." + Executor.fileAPI_names[i]);
		}
	}
	// END KGU#311 2017-01-05

	// START KGU#351 2017-02-26: Enh. #346 - include / import / uses config
	/**
	 * Method preprocesses an include file name for the #include
	 * clause. This version does nothing.
	 * @param _includeFileName a string from the user include configuration
	 * @return the preprocessed string as to be actually inserted
	 */
	protected String prepareUserIncludeItem(String _includeFileName)
	{
		return _includeFileName;
	}
	// END KGU#351 2017-02-26
	// START KGU#815/KGU#826 2020-03-17: Enh. #828, bugfix #836
	/**
	 * Method converts some generic module name into a generator-specific include file name or
	 * module name for the import / use clause.<br/>
	 * To be used before adding a generic name to {@link #generatorIncludes}.
	 * This version does not do anything. 
	 * @see #getIncludePattern()
	 * @see #appendGeneratorIncludes(String)
	 * @see #prepareUserIncludeItem(String)
	 * @param _includeName a generic (language-independent) string for the generator include configuration
	 * @return the converted string as to be actually added to {@link #generatorIncludes}
	 */
	protected String prepareGeneratorIncludeItem(String _includeName)
	{
		return _includeName;
	}
	// END KGU#815/KGU#826 2020-03-17

	// START KGU#16/#47 2015-11-30
	/**
	 * Instruction to create a language-specific exit instruction (subclassable)
	 * The exit code will be passed to the generated code.
	 */
	@Override
	protected void appendExitInstr(String _exitCode, String _indent, boolean isDisabled)
	{
		Jump dummy = new Jump();
		appendBlockHeading(dummy, "if (System.Windows.Forms.Application.MessageLoop)", _indent); 
		appendComment("WinForms app", _indent + this.getIndent());
		addCode(this.getIndent() + "System.Windows.Forms.Application.Exit();", _indent, isDisabled);
		appendBlockTail(dummy, null, _indent);

		appendBlockHeading(dummy, "else", _indent); 
		appendComment("Console app", _indent + this.getIndent());
		addCode(this.getIndent() + "System.Environment.Exit(" + _exitCode + ");", _indent, isDisabled);
		appendBlockTail(dummy, null, _indent);
	}
	// END KGU#16/#47 2015-11-30

	// START KGU#332 2017-04-14: Enh. #335
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
	// END KGU#332 2017-04-14

	// START KGU#784 2019-12-02
	@Override
	protected String transformType(String _type, String _default)
	{
		if (_type != null && (_type.equals("String") || _type.equals("Object"))) {
			_type = _type.toLowerCase();
		}
		return super.transformType(_type, _default);
	}
	// END KGU#784 2019-12-02

	
	// START KGU#388 2017-09-28: Enh. #423
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#transformRecordInit(java.lang.String, lu.fisch.structorizer.elements.TypeMapEntry)
	 */
	@Override
	protected String transformRecordInit(String constValue, TypeMapEntry typeInfo) {
		// This is practically identical to Java
		// START KGU#559 2018-07-20: Enh. #563 - smarter record initialization
		//HashMap<String, String> comps = Instruction.splitRecordInitializer(constValue);
		HashMap<String, String> comps = Instruction.splitRecordInitializer(constValue, typeInfo, false);
		// END KGU#559 2018-07-20
		LinkedHashMap<String, TypeMapEntry> compInfo = typeInfo.getComponentInfo(true);
		String recordInit = "new " + typeInfo.typeName + "(";
		boolean isFirst = true;
		for (Entry<String, TypeMapEntry> compEntry: compInfo.entrySet()) {
			String compName = compEntry.getKey();
			TypeMapEntry compType = compEntry.getValue();
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
				else if (compType != null && compType.isRecord()) {
					recordInit += transformRecordInit(compVal, compType);
				}
				// START KGU#561 2018-07-21: Bugfix #564
				else if (compType != null && compType.isArray() && compVal.startsWith("{") && compVal.endsWith("}")) {
					String elemType = compType.getCanonicalType(true, false).substring(1);
					recordInit += "new " + this.transformType(elemType, "object") + "[]" + compVal;
				}
				// END KGU#561 2018-07-21
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
	 * @param _recordValue - the record initializer according to Structorizer syntax
	 * @param _indent - current indentation level (as String)
	 * @param _isDisabled - indicates whether the code is o be commented out
	 * @param _typeEntry - an existing {@link TyeMapEntry} for the assumed record type (or null)
	 */
	protected void generateRecordInit(String _lValue, String _recordValue, String _indent, boolean _isDisabled, TypeMapEntry _typeEntry) {
		// This is practically identical to Java
		// START KGU#559 2018-07-21: Enh. #563 - Radically revised
		if (_typeEntry == null || !_typeEntry.isRecord()) {
			// Just decompose it (requires that the target variable has been initialized before).
			super.generateRecordInit(_lValue, _recordValue, _indent, _isDisabled, _typeEntry);
		}
		else {
			// This way has the particular advantage not to fail with an uninitialized variable (important for Java!). 
			addCode(_lValue + " = " + this.transformRecordInit(_recordValue, _typeEntry) + ";", _indent, _isDisabled);
		}
		// END KGU#559 2018-07-21
	}
	
	// START KGU#560 2018-07-21: Issue #563 Array initializers have to be decomposed if not occurring in a declaration
	/**
	 * Generates code that decomposes an array initializer into a series of element assignments if there no
	 * compact translation.
	 * @param _lValue - the left side of the assignment (without modifiers!), i.e. the array name
	 * @param _arrayItems - the {@link StringList} of element expressions to be assigned (in index order)
	 * @param _indent - the current indentation level
	 * @param _isDisabled - whether the code is commented out
	 * @param _elemType - the {@link TypeMapEntry} of the element type is available
	 * @param _isDecl - if this is part of a declaration (i.e. a true initialization)
	 */
	protected String transformOrGenerateArrayInit(String _lValue, StringList _arrayItems, String _indent, boolean _isDisabled, String _elemType, boolean _isDecl)
	{
		// START KGU#732 2019-10-03: Bugfix #755 - The operator new is always to be used.
		//if (_isDecl) {
		//	return this.transform("{" + _arrayItems.concatenate(", ") + "}");
		//}
		//else if (_elemType != null) {
		//	return "new " + this.transformType(_elemType, "object") + "[]{" + _arrayItems.concatenate(", ") + "}";
		//}
		//else {
		//	super.generateArrayInit(_lValue, _arrayItems, _indent, _isDisabled, null, false);
		//}
		//return null;
		// The C-like initializer
		String initializerC = super.transformOrGenerateArrayInit(_lValue, _arrayItems, _indent, _isDisabled, _elemType, true);
		if (initializerC != null) {
			return "new " + this.transformType(_elemType, "object") + "[]" + initializerC;
		}
		return null;
		// END KGU#732 2019-10-03
	}
	// END KGU#560 2018-07-21


	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#generateTypeDef(lu.fisch.structorizer.elements.Root, java.lang.String, lu.fisch.structorizer.elements.TypeMapEntry, java.lang.String, boolean)
	 */
	@Override
	protected void generateTypeDef(Root _root, String _typeName, TypeMapEntry _type, String _indent, boolean _asComment) {
		String typeKey = ":" + _typeName;
		if (this.wasDefHandled(_root, typeKey, true)) {
			return;
		}
		appendDeclComment(_root, _indent, typeKey);
		if (_type.isRecord()) {
			String indentPlus1 = _indent + this.getIndent();
			String indentPlus2 = indentPlus1 + this.getIndent();
			addCode((_root.isInclude() ? "public " : "") + "struct " + _typeName + "{", _indent, _asComment);
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

	// START KGU#653 2019-02-14: Enh. #680
	/**
	 * Subclassable method possibly to obtain a suited transformed argument list string for the given series of
	 * input items (i.e. expressions designating an input target variable each) to be inserted in the input replacer
	 * returned by {@link #getInputReplacer(boolean)}, this allowing to generate a single input instruction only.<br/>
	 * This instance just returns null (forcing the generate method to produce consecutive lines).
	 * @param _inputVarItems - {@link StringList} of variable descriptions for input
	 * @return either a syntactically converted combined string with suited operator or separator symbols, or null.
	 */
	@Override
	protected String composeInputItems(StringList _inputVarItems)
	{
		return null;
	}
	// END KGU#653 2019-02-14

	// START KGU#815 2020-03-26: Enh. #828 support for library references
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#makeLibCallName(java.lang.String)
	 */
	@Override
	protected String makeLibCallName(String name) {
		return this.libModuleName + "." + name;
	}
	// END KGU#815 2020-03-26

	// START KGU#61 2016-03-22: Enh. #84 - Support for FOR-IN loops
	/**
	 * We try our very best to create a working loop from a FOR-IN construct.
	 * @param _for - the element to be exported
	 * @param _indent - the current indentation level
	 * @return true iff the method created some loop code (sensible or not)
	 */
	protected boolean generateForInCode(For _for, String _indent)
	{
		// We simply use the range-based loop of Java (as far as possible)
		String var = _for.getCounterVar();
		String valueList = _for.getValueList();
		StringList items = this.extractForInListItems(_for);
		String indent = _indent;
		String itemType = null;
		if (items != null)
		{
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
				// START KGU#388 2019-10-02: Enh. #423 (had been forgotten in 2017)
				if (allCommon)
				{
					String itType = Element.identifyExprType(this.typeMap, item, true);
					if (i == 0) {
						commonType = itType;
					}
					if (!commonType.equals(itType)) {
						allCommon = false;
					}
				}
				// END KGU#388 2019-10-02
				// START KGU#732 2019-10-02: Bugfix #755 - transformation of the items is necessary
				items.set(i, transform(item));
				// END KGU#732 2019-10-02
			}
			valueList = "{" + items.concatenate(", ") + "}";
			// START KGU#388 2017-09-28: Enh. #423
			//if (allInt) itemType = "int";
			if (allCommon) itemType = commonType;
			else if (allInt) itemType = "int";
			// END KGU#388 2017-09-28
			else if (allDouble) itemType = "double";
			else if (allString) itemType = "char*";
			// START KGU#732 2019-10-02: Bugfix #755 part 1 - no need to define an extra variable, initializer was wrong
			//String arrayName = "array20160322";
			//
			//addCode("{", _indent , isDisabled);
			//indent += this.getIndent();
			// END KGU#732 2019-10-02
			
			if (itemType == null)
			{
				itemType = "object";
				this.appendComment("TODO: Find a more specific item type than object and/or prepare the elements of the array", indent);
				
			}
			// START KGU#732 2019-10-02: Bugfix #755 part 2
			//addCode(itemType + "[] " + arrayName + " = " + transform(valueList, false) + ";", indent, isDisabled);
			//
			//valueList = arrayName;
			valueList = "new " + itemType + "[]" + valueList;
			// END KGU#732 2019-10-02
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
				itemType = this.transformType(itemType.substring(1), "object");	
			}
			else {
				itemType = "Object";
				this.appendComment("TODO: Select a more sensible item type than object", indent);
				this.appendComment("      and/or prepare the elements of the array.", indent);
			}
			// END KGU#388 2017-09-28
			valueList = transform(valueList, false);
		}

		// Creation of the loop header
		appendBlockHeading(_for, "foreach (" + itemType + " " + var + " in " +	valueList + ")", indent);

		// Add the loop body as is
		generateCode(_for.q, indent + this.getIndent());

		// Accomplish the loop
		appendBlockTail(_for, null, indent);

		// START KGU#732 2019-10-02: Bugfix #755 part 3
		//if (items != null)
		//{
		//	addCode("}", _indent, isDisabled);
		//}
		// END KGU#732 2019-10-02
		
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

		appendComment(_para, _indent);

		addCode("", "", isDisabled);
		appendComment("==========================================================", _indent);
		appendComment("================= START PARALLEL SECTION =================", _indent);
		appendComment("==========================================================", _indent);
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
		appendComment("==========================================================", _indent);
		appendComment("================== END PARALLEL SECTION ==================", _indent);
		appendComment("==========================================================", _indent);
		addCode("", "", isDisabled);
	}

	// Adds class definitions for workers to be used by the threads to this.subClassDefinitions
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
				appendComment("=========== START PARALLEL WORKER DEFINITIONS ============", _indent);
			}
			for (Parallel par: containedParallels) {
				boolean isDisabled = par.isDisabled();
				String workerNameBase = "Worker" + Integer.toHexString(par.hashCode()) + "_";
				Root root = Element.getRoot(par);
				// START KGU#676 2019-03-30: Enh. #696 special pool in case of batch export
				//HashMap<String, TypeMapEntry> typeMap = root.getTypeInfo();
				HashMap<String, TypeMapEntry> typeMap = root.getTypeInfo(routinePool);
				// END KGU#676 2019-03-30
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
						addSepaLine();
					}
					addCode("class " + worker + "{", _indent, isDisabled);
					if (setVars.count() > 0 || usedVars.count() > 0) {
						appendComment("TODO: Check and accomplish the member declarations here", indentPlusOne);
					}
					if (setVars.count() > 0) {
						appendComment("TODO: Maybe you must care for an initialization of the public members, too", indentPlusOne);
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
				appendComment("============ END PARALLEL WORKER DEFINITIONS =============", _indent);
				addSepaLine();
			}
		}
		finally {
			this.code = codeBefore;
		}
		return workerDefinitions;
	}
	
	/**
	 * Generates an argument list for a worker thread routine as branch of a parallel section.
	 * Types for the variable names in {@code varNames} are retrieved from {@code typeMap}. If
	 * no associated type can be identified then a comment {@code "type?"} will be inserted.
	 * @param varNames - list of variable names to be passed in
	 * @param typeMap - maps variable names and type names to type specifications
	 * @return a list of argument declarations
	 */
	private StringList makeArgList(StringList varNames, HashMap<String, TypeMapEntry> typeMap)
	{
		StringList argList = new StringList();
		for (int v = 0; v < varNames.count(); v++) {
			String varName = varNames.get(v);
			TypeMapEntry typeEntry = typeMap.get(varName);
			String typeSpec = "???";
			if (typeEntry != null) {
				StringList typeSpecs = this.getTransformedTypes(typeEntry, false);
				if (typeSpecs.count() == 1) {
					// START KGU#784 2019-12-02
					//typeSpec = typeSpecs.get(0);
					typeSpec = this.transformTypeFromEntry(typeEntry, null);
					// END KGU#784 2019-12-02
				}
			}
			argList.add(typeSpec + " " + varName);
		}
		return argList;
	}
	// END KGU#47/KGU#348 2017-02-24

	// START KGU#686 2019-03-18: Enh. #56
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#makeExceptionFrom(java.lang.String)
	 */
	@Override
	protected void generateThrowWith(String _thrown, String _indent, boolean _asComment) {
		// If it isn't a rethrow then fake some text (a rethrow doesn't require an argument)
		boolean warn = false;
		if (_thrown.isEmpty() && this.caughtException == null) {
			_thrown = "new System.Exception(\"unspecified error\")";
			warn = true;
		}
		else if (!_thrown.isEmpty()) {
			// _thrown is supposed to be a string expression...
			_thrown = "new System.Exception(" + _thrown + ")";
			warn = true;
		}
		if (warn) {
			appendComment("FIXME: You should replace System.Exception by an own subclass!", _indent);
		}
		// In case of an empty argument with non-null caughtException we assume a rethrow
		addCode(("throw " + _thrown).trim() + ";", _indent, _asComment);
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#appendCatchHeading(lu.fisch.structorizer.elements.Try, java.lang.String)
	 */
	@Override
	protected void appendCatchHeading(Try _try, String _indent) {
		
		boolean isDisabled = _try.isDisabled();
		String varName = _try.getExceptionVarName();
		String head = "catch ()";
		String exName = "ex" + Integer.toHexString(_try.hashCode());
		if (varName != null && !varName.isEmpty()) {
			head = "catch(Exception " + exName + ")";
		}
		this.appendBlockHeading(_try, head, _indent);
		if (exName != null) {
			this.addCode("string " + varName + " = " + exName + ".ToString()", _indent + this.getIndent(), isDisabled);
		}
		this.caughtException = exName;
	}
	// END KGU#686 2019-03-18

	/**
	 * Composes the heading for the program or function according to the
	 * C language specification.
	 * @param _root - The diagram root
	 * @param _indent - the initial indentation string
	 * @param _procName - the procedure name
	 * @param _paramNames - list of the argument names
	 * @param _paramTypes - list of corresponding type names (possibly null) 
	 * @param _resultType - result type name (possibly null)
	 * @param _public - whether the resulting method is to be public
	 * @return the default indentation string for the subsequent stuff
	 */
	@Override
	protected String generateHeader(Root _root, String _indent, String _procName,
			StringList _paramNames, StringList _paramTypes, String _resultType, boolean _public)
	{
		String indentPlus1 = _indent + this.getIndent();
		String indentPlus2 = indentPlus1 + this.getIndent();
		// START KGU#178 2016-07-20: Enh. #160
		if (topLevel)
		{
			// START KGU#815/KGU#824 2020-03-25: Enh. #828, bugfix #836
			if (this.usesFileAPI && (this.isLibraryModule() || this.importedLibRoots != null)) {
				/* In case of a library we will rather work with a copied FileAPI file than
				 * with copied code, so ensure the using clause for the namespace
				 */
				generatorIncludes.addIfNew("FileAPI.CS");
			}
			// END KGU#815/KGU#824 2020-03-25
			appendComment("Generated by Structorizer " + Element.E_VERSION, _indent);
			// START KGU#363 2017-05-16: Enh. #372
			appendCopyright(_root, _indent, true);
			// END KGU#363 2017-05-16
			// START KGU#376 2017-09-28: Enh. #389 - definitions from all included diagrams will follow
			if (!_root.isProgram()) {
				appendGlobalDefinitions(_root, indentPlus1, true);
			}
			// END KGU#376 2017-09-28
			addSepaLine();
			subroutineInsertionLine = code.count();	// default position for subroutines
			subroutineIndent = _indent;
		}
		else
		{
			addSepaLine();
		}
		// END KGU#178 2016-07-20
		
		// START KGU#815 2020-03-26: Enh. #828 group export may produce libraries
		//if (_root.isProgram()) {
		if (_root.isProgram() || topLevel && this.isLibraryModule()) {
		// END KGU#815 2020-03-26
			this.generatorIncludes.add("System");
			// START KGU#348 2017-02-24: Enh. #348
			if (this.hasParallels) {
				this.generatorIncludes.add("System.Threading");
			}
			// END KGU#348 2017-02-24
			this.appendGeneratorIncludes(_indent, false);
			addSepaLine();
			// STARTB KGU#351 2017-02-26: Enh. #346
			this.appendUserIncludes(_indent);
			// END KGU#351 2017-02-26
			addSepaLine();
			// START KGU 2015-10-18
			appendBlockComment(_root.getComment(), _indent, "/// <summary>", "/// ", "/// </summary>");
			// END KGU 2015-10-18

			appendBlockHeading(_root, "public class "+ _procName, _indent);
			addSepaLine();
			// START KGU#348 2017-02-24: Enh.#348
			this.subClassInsertionLine = code.count();
			// END KGU#348 2017-02-24
			// START KGU#311 2017-01-05: Enh. #314 File API
			if (this.usesFileAPI && !generatorIncludes.contains("FileAPI.CS")) {
				this.insertFileAPI("cs", code.count(), _indent, 0);
				addSepaLine();
			}
			// END KU#311 2017-01-05
			// START KGU#376 2017-09-28: Enh. #389 - definitions from all included diagrams will follow
			//insertComment("TODO Declare and initialise class variables here", indentPlus1);
			appendGlobalDefinitions(_root, indentPlus1, true);
			// END KGU#376 2017-09-28
			addSepaLine();
			// START KGU#815 2020-03-26: Enh. #828
			//code.add(indentPlus1 + "/// <param name=\"args\"> array of command line arguments </param>");
			//appendBlockHeading(_root, "public static void Main(string[] args)", indentPlus1);
			if (_root.isProgram()) {
				code.add(indentPlus1 + "/// <param name=\"args\"> array of command line arguments </param>");
				appendBlockHeading(_root, "public static void Main(string[] args)", indentPlus1);
			}
			else {
				// Obviously it is the library initialization routine
				appendBlockHeading(_root, "public static void " + this.getInitRoutineName(_root) + "()", indentPlus1);
			}
			// END KGU#815 2020-03-26
			addSepaLine();
		}
		else {
			// Not at top level or some subroutine ...
			
			// START KGU#311 2017-01-05: Enh. #314 File API
			// START KGU#815 2020-03-26: Enh. #828 - in case of an involved library we will share the copied file
			//if (this.topLevel && this.usesFileAPI) {
			if (this.topLevel && this.usesFileAPI && this.importedLibRoots == null) {
			// END KGU#815 2020-03-26
				this.insertFileAPI("cs", code.count(), _indent, 0);
				addSepaLine();
			}
			// END KU#311 2017-01-05
			// START KGU#348 2017-02-24: Enh.#348
			if (this.topLevel) {
				// START KGU#815 2020-03-26: Enh. #828
				if (this.importedLibRoots != null) {
					appendBlockHeading(_root, "public class "+ _procName, _indent);					
				}
				// END KGU#815 2020-03-26
				this.subClassInsertionLine = code.count();
			}
			// END KGU#348 2017-02-24
			appendBlockComment(_root.getComment(), indentPlus1, "/// <summary>", "/// ", "/// </summary>");
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
			String fnHeader = ((topLevel || _public) ? "public" : "private") + " static "
					+ _resultType + " " + _procName + "(";
			// END KGU#178 2016-07-20
			// START KGU#371 2019-03-07: Enh. #385 Care for default values
			StringList defaultVals = _root.getParameterDefaults();
			// END KGU#371 2019-03-07
			for (int p = 0; p < _paramNames.count(); p++) {
				if (p > 0) { fnHeader += ", "; }
				// START KGU#140 2017-01-31: Enh. #113: Proper conversion of array types
				//fnHeader += (transformType(_paramTypes.get(p), "/*type?*/") + " " + 
				//		_paramNames.get(p)).trim();
				fnHeader += transformArrayDeclaration(transformType(_paramTypes.get(p), "???").trim(), _paramNames.get(p));
				// END KGU#140 2017-01-31
				// START KGU#371 2019-03-07: Enh. #385
				String defVal = defaultVals.get(p);
				if (defVal != null) {
					fnHeader += " = " + transform(defVal);
				}
				// END KGU#371 2019-03-07
			}
			fnHeader += ")";
			appendBlockHeading(_root, fnHeader, indentPlus1);
		}

		// START KGU#376 2017-09-26: Enh. #389 - add the initialization code of the includables
		// START KGU#815 2020-03-27: Enh. #828 now done in generateBody()
		//appendGlobalInitialisations(indentPlus2);
		// END KGU#815 2020-03-27
		// END KGU#376 2017-09-26

		// START KGU#348 2017-02-24: Enh. #348 - Actual translation of Parallel sections
		StringList workers = this.generateParallelThreadWorkers(_root, indentPlus1);
		for (int i = 0; i < workers.count(); i++) {
			insertCode(workers.get(i), this.subClassInsertionLine);
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
//		addSepaLine();
//		// Variable declaration proposals (now with all used variables listed)
//		insertComment("TODO: Declare local variables here:", _indent);
//		for (int v = 0; v < varNames.count(); v++)
//		{
//			insertComment(varNames.get(v), _indent);
//		}
//		addSepaLine();
//		return _indent;
//	}
	
	// START KGU#501 2018-02-22: Bugfix #517
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#getModifiers(lu.fisch.structorizer.elements.Root, java.lang.String)
	 */
	@Override
	protected String getModifiers(Root _root, String _name) {
		if (_root.isInclude()) {
			return "private static ";
		}
		return "";
	}
	// END KGU#501 2018-02-22

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
		String sepa = " ";
		if (_elementType.startsWith("@")) {
			_elementType = _elementType.substring(1) + "[";
			sepa = "] ";
		}
		while (_elementType.startsWith("@")) {
			_elementType = _elementType.substring(1) + ",";
		}
		return (transformType(_elementType, _elementType) + sepa + _varName).trim(); 
	}
	@Override
	protected void generateIOComment(Root _root, String _indent)
	{
		// START KGU#236 2016-12-22: Issue #227
		if (this.hasInput(_root)) {
			addSepaLine();
			appendComment("TODO: You may have to modify input instructions,", _indent);
			appendComment("      possibly by enclosing Console.ReadLine() calls with", _indent);
			appendComment("      Parse methods according to the variable type, e.g.:", _indent);
			appendComment("         i = int.Parse(Console.ReadLine());", _indent);
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
			addSepaLine();
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

		// START KGU#815/KGU#824 2020-03-19: Enh. #828, bugfix #836
		if (topLevel) {
			libraryInsertionLine = code.count();
		}
		// END KGU#815/KGU#824 2020-03-19

		// If we declared a class in generateHeader then we must close the block here
		if (_root.isProgram() || topLevel && (this.isLibraryModule() || this.importedLibRoots != null))
		{
			// START KGU#178 2016-07-20: Enh. #160
			// Modify the subroutine insertion position
			subroutineInsertionLine = code.count();
			// END KGU#178 2016-07-20
			
			// Close class block
			addSepaLine();
			code.add(_indent + "}");
		}
	}
	// END KGU 2015-12-15

	// START KGU#815 2020-03-26: Enh. #828 - group export, for libraries better copy the FileAPI file than the content
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#updateLineMarkers(int, int)
	 */
	@Override
	protected void updateLineMarkers(int atLine, int nLines) {
		super.updateLineMarkers(atLine, nLines);
		if (this.subClassInsertionLine >= atLine) {
			this.subClassInsertionLine += nLines;
		}
	}
	
	/**
	 * Special handling for the global initializations in case these were outsourced to
	 * an external library {@link #libModuleName}. (The inherited method would suggest a
	 * constructor call but then we would have to care for an instantiation, certainly as
	 * singleton, rather than relying on static methods.
	 * @param _indent - current indentation
	 * @see #appendGlobalInitialisations(String)
	 */
	protected void appendGlobalInitialisationsLib(String _indent) {
		// We simply call the global initialisation function of the library
		addCode("initialize_" + this.libModuleName + "();", _indent, false);
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#copyFileAPIResources(java.lang.String)
	 */
	@Override
	protected boolean copyFileAPIResources(String _filePath)
	{
		/* If importedLibRoots is not null then we had a multi-module export,
		 * this function will only be called if at least one of the modules required
		 * the file API, so all requiring modules will be using "FileAPI.CS".
		 * Now we simply have to make sure it gets provided.
		 */
		if (this.importedLibRoots != null) {
			return copyFileAPIResource("cs", FILE_API_CLASS_NAME + ".cs", _filePath);
		}
		return true;	// By default, nothing is to be done and that is okay
	}
	// END KGU#815 2020-03-26
}

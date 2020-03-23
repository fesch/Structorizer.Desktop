/*
    Structorizer
    A little tool which you can use to create Nassi-Shneiderman Diagrams (NSD)

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
 *      Description:    This class generates Javascript code
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2019-05-21      First Issue (#721)
 *      Kay Gürtzig     2019-09-30      Array and record initializer handling added...
 *      Kay Gürtzig     2019-10-03      ... and improved (still not clean - we need a new recursive approach)
 *      Kay Gürtzig     2019-11-24      Bugfix #783 - Workaround for record initializers without known type
 *      Kay Gürtzig     2020-02-11      Bugfix #810 - multiple-input instruction export wasn't properly configured
 *
 ******************************************************************************************************
 *
 *      Comment: See e.g. https://www.w3schools.com/js/default.asp
 *      
 *
 ******************************************************************************************************///

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Try;
import lu.fisch.structorizer.elements.TypeMapEntry;
import lu.fisch.utils.StringList;

/**
 * @author Kay Gürtzig
 * Javascript generator for Structorizer (requested by A. Brusinsky)
 */
public class JsGenerator extends CGenerator {

	private Root currentRoot = null;
	
	/**
	 * Generates a blank Javascript code generator
	 */
	public JsGenerator() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getDialogTitle()
	 */
	@Override
	protected String getDialogTitle() {
		return "Export Javascript ...";
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getFileDescription()
	 */
	@Override
	protected String getFileDescription() {
		return "Javascript Code";
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getFileExtensions()
	 */
	@Override
	protected String[] getFileExtensions() {
		String[] exts = {"js"};
		return exts;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getIndent()
	 */
	@Override
	protected String getIndent() {
		return "\t";
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#commentSymbolLeft()
	 */
	@Override
	protected String commentSymbolLeft() {
		return "//";
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#isInternalDeclarationAllowed()
	 */
	@Override
	protected boolean isInternalDeclarationAllowed()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#getTryCatchLevel()
	 */
	@Override
	protected TryCatchSupportLevel getTryCatchLevel()
	{
		return TryCatchSupportLevel.TC_TRY_CATCH_FINALLY;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getInputReplacer(boolean)
	 */
	@Override
	protected String getInputReplacer(boolean withPrompt) {
		if (withPrompt) {
			return "$2 = prompt($1)";
		}
		return "$1 = prompt(String($1))";
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getOutputReplacer()
	 */
	@Override
	protected String getOutputReplacer() {
		return "document.write(($1) + \"<br/>\")";
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#breakMatchesCase()
	 */
	@Override
	protected boolean breakMatchesCase() {
		return true;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getIncludePattern()
	 */
	@Override
	protected String getIncludePattern() {
		// TODO Auto-generated method stub
		return "import %;";
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getOverloadingLevel()
	 */
	@Override
	protected OverloadingLevel getOverloadingLevel() {
		return OverloadingLevel.OL_DEFAULT_ARGUMENTS;	// Both overloading and default arguments (ES6/ES2015)
	}

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
		StringList transItems = new StringList();
		for (int i = 0; i < _arrayItems.count(); i++) {
			transItems.add(this.transform(_arrayItems.get(i), false));
		}
		return "[" + transItems.concatenate(", ") + "]";
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#transformRecordInit(java.lang.String, lu.fisch.structorizer.elements.TypeMapEntry)
	 */
	@Override
	protected String transformRecordInit(String constValue, TypeMapEntry typeInfo) {
		// START KGU#559 2018-07-20: Enh. #563 - smarter initializer evaluation
		//HashMap<String, String> comps = Instruction.splitRecordInitializer(constValue);
		// START KGU#771 2019 11-24: Bugfix #783 - precaution against unknown type
		//HashMap<String, String> comps = Instruction.splitRecordInitializer(constValue, typeInfo);
		HashMap<String, String> comps = Instruction.splitRecordInitializer(constValue, typeInfo, true);
		// END KGU#771 2019-11-24
		// END KGU#559 2018-07-20
		LinkedHashMap<String, TypeMapEntry> compInfo;
		if (typeInfo != null) {
			compInfo = typeInfo.getComponentInfo(true);
		}
		else {
			compInfo = new LinkedHashMap<String, TypeMapEntry>();
			for (String key: comps.keySet()) {
				compInfo.put(key, null);
			}
		}
		StringBuilder recordInit = new StringBuilder("{");
		boolean isFirst = true;
		for (Entry<String, TypeMapEntry> compEntry: compInfo.entrySet()) {
			String compName = compEntry.getKey();
			String compVal = comps.get(compName);
			TypeMapEntry compType = compEntry.getValue();
			if (!compName.startsWith("§") && compVal != null) {
				if (isFirst) {
					isFirst = false;
				}
				else {
					recordInit.append(", ");
				}
				recordInit.append(compName + ":");
				if (compType != null && compType.isRecord()) {
					recordInit.append(transformRecordInit(compVal, compType));
				}
				// START KGU#732 2019-10-03: Bugfix #755 FIXME - nasty workaround
				// START KGU#771 2019-11-24: Bugfix #783 Caused a NullPointer exception on missing type info
				//else if (compType.isArray() && compVal.startsWith("{") && compVal.endsWith("}")) {
				else if (compType != null && compType.isArray() && compVal.startsWith("{") && compVal.endsWith("}")) {
				// END KGU#771 2019-11-24
					StringList items = Element.splitExpressionList(compVal.substring(1), ",", true);
					items.delete(items.count()-1);
					for (int i = 0; i < items.count(); i++) {
						items.set(i, transform(items.get(i)));
					}
					recordInit.append("[");
					recordInit.append(items.concatenate(", ", 0));
					recordInit.append("]");
				}
				// END KGU#561 2018-07-21
				else {
					recordInit.append(transform(compVal, false));
				}
			}
		}
		recordInit.append("}");
		return recordInit.toString();
	}
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#makeArrayDeclaration(java.lang.String, java.lang.String, lu.fisch.structorizer.elements.TypeMapEntry)
	 */
	@Override
	protected String makeArrayDeclaration(String _elementType, String _varName, TypeMapEntry typeInfo)
	{
		return ("var " + _varName).trim(); 
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#generateRecordInit(java.lang.String, java.lang.String, java.lang.String, boolean, lu.fisch.structorizer.elements.TypeMapEntry)
	 */
	@Override
	protected void generateRecordInit(String _lValue, String _recordValue, String _indent, boolean _isDisabled, TypeMapEntry _typeEntry)
	{
		String compVal = transformRecordInit(_recordValue, _typeEntry);
		addCode(_lValue + " = " + compVal, _indent, _isDisabled);
	}
	
	
	/**
	 * We suppress type definitions here and replace them by a comment line.
	 * @see lu.fisch.structorizer.generators.CGenerator#generateTypeDef(lu.fisch.structorizer.elements.Root, java.lang.String, lu.fisch.structorizer.elements.TypeMapEntry, java.lang.String, boolean)
	 */
	@Override
	protected void generateTypeDef(Root _root, String _typeName, TypeMapEntry _type, String _indent, boolean _asComment) {
		String typeKey = ":" + _typeName;
		if (this.wasDefHandled(_root, typeKey, true)) {
			return;
		}
		appendDeclComment(_root, _indent, typeKey);
		appendComment("type " + _typeName + ": " + _type.getCanonicalType(false, false).replace("$", "object"), _indent);
	}
	
	// START KGU#653/KGU#797 2020-02-11: Enh. #680, bugfix #810
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
	// END KGU#653/KGU#797 2020-02-11

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#generateInstructionLine(lu.fisch.structorizer.elements.Instruction, java.lang.String, boolean, java.lang.String)
	 */
	@Override
	protected boolean generateInstructionLine(Instruction _inst, String _indent, boolean commentInserted, String line)
	{
		// Don't do anything with type definitions
		if (Instruction.isTypeDefinition(line, typeMap)) {
			return false;
		}
		return super.generateInstructionLine(_inst, _indent, commentInserted, line);
	}
	
	/**
	 * We try our very best to create a working loop from a FOR-IN construct.
	 * @param _for - the element to be exported
	 * @param _indent - the current indentation level
	 * @return true iff the method created some loop code (sensible or not)
	 */
	protected boolean generateForInCode(For _for, String _indent)
	{
		String var = _for.getCounterVar();
		String valueList = _for.getValueList();
		StringList items = this.extractForInListItems(_for);
		
		if (items != null) {
			for (int i = 0; i < items.count(); i++) {
				items.set(i,  transform(items.get(i), false));
			}
			valueList = "[" + items.concatenate(", ") + "]";
		}
		
		// Creation of the loop header
		appendBlockHeading(_for, "for (const " + var + " of " +	valueList + ")", _indent);

		// Add the loop body as is
		generateCode(_for.q, _indent + this.getIndent());

		// Accomplish the loop
		appendBlockTail(_for, null, _indent);

		return true;
	}

	@Override
	protected String composeTypeAndNameForDecl(String _type, String _name) {
		String prefix = "";
		if (!this.wasDefHandled(currentRoot, _name, true)) {
			prefix = "var ";
		}
		return prefix + _name;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#makeExceptionFrom(java.lang.String)
	 */
	@Override
	protected void generateThrowWith(String _thrown, String _indent, boolean _asComment) {
		// If it isn't a rethrow then fake some text
		if (_thrown.isEmpty()) {
			if (this.caughtException == null) {
				_thrown = "new Error(\"unspecified error\")";
			}
			else {
				_thrown = this.caughtException;
			}
		}
		addCode (("throw " + _thrown).trim() + ";", _indent, _asComment);
	}
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.JavaGenerator#insertCatchHeading(lu.fisch.structorizer.elements.Try, java.lang.String)
	 */
	@Override
	protected void appendCatchHeading(Try _try, String _indent) {
		boolean isDisabled = _try.isDisabled();
		String varName = _try.getExceptionVarName();
		String exName = "ex" + Integer.toHexString(_try.hashCode());;
		String head = "catch (" + exName + ")";
		this.appendBlockHeading(_try, head, _indent);
		if (varName != null && !varName.isEmpty()) {
			this.addCode(varName + " = " + exName + ".message", _indent + this.getIndent(), isDisabled);
		}
		this.caughtException = exName;
	}
	
	
	/**
	 * Composes the heading for the program or function according to the
	 * C language specification.
	 * @param _root - The diagram root
	 * @param _indent - the initial indentation string
	 * @param _procName - the procedure name
	 * @param _paramNames - list of the argument names
	 * @param _paramTypes - list of corresponding type names (possibly null) 
	 * @param _resultType - result type name (possibly null)
	 * @param _public - whether the resulting functions are to be public
	 * @return the default indentation string for the subsequent stuff
	 */
	@Override
	protected String generateHeader(Root _root, String _indent, String _procName,
			StringList _paramNames, StringList _paramTypes, String _resultType, boolean _public)
	{
		currentRoot = _root;
		if (topLevel)
		{
			code.add("<script>");
		}
		else {
			addSepaLine();
		}
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
			appendComment("Generated by Structorizer " + Element.E_VERSION, _indent);
			// START KGU#363 2017-05-16: Enh. #372
			appendCopyright(_root, _indent, true);
			// END KGU#363 2017-05-16
			addSepaLine();
//			if (this.usesFileAPI) {
//				this.generatorIncludes.add("<stlib.h>");
//				this.generatorIncludes.add("<string.h>");
//				this.generatorIncludes.add("<errno.h>");
//			}
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
			// END KGU#236 2016-08-10
			// START KGU#178 2016-07-20: Enh. #160
			subroutineInsertionLine = code.count();
			subroutineIndent = _indent;
			
			// START KGU#311 2016-12-22: Enh. #314 - insert File API routines if necessary
//			if (this.usesFileAPI) {
//				this.insertFileAPI("js");
//			}
			// END KGU#311 2016-12-22
		}
		// END KGU#178 2016-07-20

		appendComment(_root, _indent);
		if (_root.isSubroutine()) {
			//this.typeMap = new HashMap<String, TypeMapEntry>(_root.getTypeInfo(routinePool));
			String fnHeader = "function " + _procName + "(";
			for (int p = 0; p < _paramNames.count(); p++) {
				if (p > 0) { fnHeader += ", "; }
				fnHeader += _paramNames.get(p);
			}
			fnHeader += ") {";
			code.add(_indent + fnHeader);
			_indent += this.getIndent();
		}
		
		// START KGU#376 2017-09-26: Enh. #389 - insert the initialization code of the includables
		appendGlobalInitialisations(_indent);
		// END KGU#376 2017-09-26
		
		return _indent;
	}

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
		if (!this.suppressTransformation) {
			appendDefinitions(_root, _indent, varNames, false);
		}
		if (this.typeMap == null) {
			this.typeMap = new HashMap<String, TypeMapEntry>(_root.getTypeInfo(routinePool));
		}
		//generateIOComment(_root, _indent);
		addSepaLine();
		return _indent;
	}

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
		if (wasDefHandled(_root, _name, false)) {
			return;
		}
		String constValue = _root.getConstValueString(_name);
		String decl = "var " + _name;
		if (_root.constants.containsKey(_name) && constValue != null) {
			decl = "const " + _name;
			// FIXME
			TypeMapEntry typeInfo = typeMap.get(_name);
					if (constValue.contains("{") && constValue.endsWith("}") && typeInfo != null && typeInfo.isRecord()) {
						constValue = transformRecordInit(constValue, typeInfo);
					}
					else {
						constValue = transform(constValue);
					}
			decl += " = " + constValue;
		}
		appendDeclComment(_root, _indent, _name);
		setDefHandled(_root.getSignatureString(false), _name);
		code.add(_indent + this.getModifiers(_root, _name) + decl + ";");
		// Add a comment if there is no type info or internal declaration is not allowed
		setDefHandled(_root.getSignatureString(false), _name);
		// END KGU#424 2017-09-26
	}

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
		if (_root.isSubroutine() &&
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
			addSepaLine();
			code.add(_indent + "return " + result + ";");
		}
		return _indent;
	}

	// START KGU 2015-12-15: Method block must be closed as well
	/**
	 * Method is to finish up after the text insertions of the diagram, i.e. to close open blocks etc. 
	 * @param _root 
	 * @param _indent
	 */
	@Override
	protected void generateFooter(Root _root, String _indent)
	{
		if (_root.isSubroutine()) {
			code.add(_indent + "}");
		}

		if (topLevel) {
			// START KGU#815/KGU#824 2020-03-19: Enh. #828, bugfix #836
			libraryInsertionLine = code.count();
			// END KGU#815/KGU#824 2020-03-19
			code.add("</script>");
		}
//		if (topLevel && this.usesFileAPI) {
//			this.insertFileAPI("java");
//		}

//		if (topLevel && this.usesTurtleizer) {
//			// START KGU#563 2018-07-26: Issue #566
//			//code.insert(this.commentSymbolLeft() + " TODO: Download the turtle package from http://structorizer.fisch.lu and put it into this project", this.includeInsertionLine++);
//			code.insert(this.commentSymbolLeft() + " TODO: Download the turtle package from " + Element.E_HOME_PAGE + " and put it into this project", this.includeInsertionLine++);
//			// END KGU#563 2018-07-26
//			code.insert((_root.isSubroutine() ? this.commentSymbolLeft() : "") + "import lu.fisch.turtle.adapters.Turtleizer;", this.includeInsertionLine);
//		}
	}

	
}

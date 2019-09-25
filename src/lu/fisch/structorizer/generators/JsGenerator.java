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
 *
 ******************************************************************************************************
 *
 *      Comment: See e.g. https://www.w3schools.com/js/default.asp
 *      
 *
 ******************************************************************************************************///


import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Try;
import lu.fisch.utils.StringList;

/**
 * @author Kay Gürtzig
 * Javascript generator for Structorizer (requested by A. Brusinsky)
 */
public class JsGenerator extends CGenerator {

	/**
	 * 
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
	 * @param paramNames - list of the argument names
	 * @param paramTypes - list of corresponding type names (possibly null) 
	 * @param resultType - result type name (possibly null)
	 * @return the default indentation string for the subsequent stuff
	 */
	@Override
	protected String generateHeader(Root _root, String _indent, String _procName,
			StringList _paramNames, StringList _paramTypes, String _resultType)
	{
		if (!topLevel)
		{
			code.add("");					
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
			code.add("");
//			if (this.usesFileAPI) {
//				this.generatorIncludes.add("<stlib.h>");
//				this.generatorIncludes.add("<string.h>");
//				this.generatorIncludes.add("<errno.h>");
//			}
			this.appendGeneratorIncludes("", false);
			code.add("");
			// START KGU#351 2017-02-26: Enh. #346 / KGU#3512017-03-17 had been mis-placed
			this.appendUserIncludes("");
			// START KGU#446 2017-10-27: Enh. #441
			this.includeInsertionLine = code.count();
			// END KGU#446 2017-10-27
			code.add("");
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
		if (!_root.isProgram()) {
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
		//generateIOComment(_root, _indent);
		code.add(_indent);
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
		String constValue = _root.constants.get(_name);
		String decl = "var " + _name;
		if (_root.constants.containsKey(_name) && constValue != null) {
			decl = "const " + _name;
//					if (constValue.contains("{") && constValue.endsWith("}") && typeInfo != null && typeInfo.isRecord()) {
//						constValue = transformRecordInit(constValue, typeInfo);
//					}
//					else {
//						constValue = transform(constValue);
//					}
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
			code.add(_indent);
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
		if (!_root.isProgram()) {
			code.add(_indent + "}");		
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

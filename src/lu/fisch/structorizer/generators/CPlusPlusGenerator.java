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

/*
 ******************************************************************************************************
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
 ******************************************************************************************************
 */

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.Root;
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
		return "std::cout << $1";
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
			if (this.hasInput && this.hasOutput)
			{
				code.add("#include <iostream>");
			}
	        // END KGU#236 2016-08-10
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
			String fnHeader = transformType(_resultType,
					((returns || isResultSet || isFunctionNameSet) ? "int" : "void"));
			fnHeader += " " + _procName + "(";
			for (int p = 0; p < _paramNames.count(); p++) {
				if (p > 0)
					fnHeader += ", ";
				fnHeader += (transformType(_paramTypes.get(p), "/*type?*/") + " " +
					_paramNames.get(p)).trim();
			}
			fnHeader += ")";
			// END KGU 2015-11-29
            insertComment("TODO Revise the return type and declare the parameters!", _indent);
            
        	code.add(fnHeader);
        }
		
		code.add("{");
		
		return _indent + this.getIndent();
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
		code.add(_indent);
		insertComment("TODO: declare your variables here:", _indent);
        // START KGU 2015-11-30: List the variables to be declared
		for (int v = 0; v < varNames.count(); v++) {
			insertComment(varNames.get(v), _indent);
		}
		// END KGU 2015-11-30
		code.add(_indent);
		return _indent;
	}
    


}

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
 *      Description:    Abstract class for any code generator.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------          ----			-----------
 *      Bob Fisch       2007.12.27		First Issue
 *      Bob Fisch       2008.04.12		Plugin Interface
 *      Kay Gürtzig     2014.11.16		comment generation revised (see comment below)
 *      Kay Gürtzig     2015.10.18		File name proposal in exportCode(Root, File, Frame) delegated to Root
 *      Kay Gürtzig     2015.11.01		transform methods reorganised (KGU#18/KGU23) using subclassing
 *      Kay Gürtzig     2015.11.30		General preprocessing for generateCode(Root, String) (KGU#47)
 *      Bob Fisch       2015.12.10		Bugfix #51: when input identifier is alone, it was not converted
 *      Kay Gürtzig     2015.12.18		Enh #66, #67: New export options
 *      Kay Gürtzig     2015-12-21      Bugfix #41/#68/#69 (= KGU#93) avoid padding and string literal impact
 *      Kay Gürtzig     2015.12.22		Slight performance improvement in transform()
 *      Kay Gürtzig     2016-01-16      KGU#141: New generic method lValueToTypeNameIndex introduced for Issue #112
 *      Kay Gürtzig     2016-03-22      KGU#61/KGU#129: varNames now basic field for all subclasses
 *
 ******************************************************************************************************
 *
 *      Comment:		
 *      2015.11.30 - Decomposition of generateRoot() and divers preprocessing provided for subclasses
 *      - method mapJumps fills hashTable jumpTable mapping (Jump and Loop elements to connecting codes)
 *      - parameter names and types as well as functio name and type are preprocessed
 *      - result mechanisms are also analysed

 *      2014.11.16 - Enhancement
 *      - method insertComment renamed to insertAsComment (as it inserts the instruction text!)
 *      - overloaded method insertComment added to export the actual element comment
 *      
 ******************************************************************************************************///

import java.awt.Frame;
import java.io.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;

import javax.swing.*;

import com.stevesoft.pat.Regex;

import lu.fisch.utils.*;
import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.gui.ExportOptionDialoge;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.parsers.D7Parser;


public abstract class Generator extends javax.swing.filechooser.FileFilter
{
	/************ Fields ***********************/
	private ExportOptionDialoge eod = null;
	protected StringList code = new StringList();

	// START KGU#74 2015-11-29: Sound handling of Jumps requires some tracking
	protected boolean returns = false; // Explicit return instructions occurred?
	protected boolean alwaysReturns = false; // Do all paths involve a return instruction?
	protected boolean isResultSet = false; // Assignment to variable named "result"?
	protected boolean isFunctionNameSet = false; // Assignment to variable named like function?
	protected int labelCount = 0; // unique count for generated labels
	protected String labelBaseName = "StructorizerLabel_";
	// maps loops and Jump elements to label counts (neg. number means illegal jump target)
	protected Hashtable<Element, Integer> jumpTable = new Hashtable<Element, Integer>();
	// END KGU#74 2015-11-29

	// START KGU#129/KGU#61 2016-03-22: Bugfix #96 / Enh. #84 Now important for most generators
	// Some generators must prefix variables, for some generators it's important for FOR-IN loops
	protected StringList varNames = new StringList();
	// END KGU#129/KGU#61 2015-01-22
	// START KGU  2016-03-29: For keyword detection improvement
	private Vector<StringList> splitKeywords = new Vector<StringList>();
	// END KGU 2016-03-29

	
	/************ Abstract Methods *************/
	protected abstract String getDialogTitle();
	protected abstract String getFileDescription();
	protected abstract String getIndent();
	protected abstract String[] getFileExtensions();
	// START KGU 2015-10-18: It seemed sensible to store the comment specification permanently
	/**
	 * @return left comment delimiter, e.g. "/*", "//", "(*", or "{"
	 */
	protected abstract String commentSymbolLeft();
	/**
	 * @return right comment delimiter if required, e.g. "*\/", "}", "*)"
	 */
	protected String commentSymbolRight() { return ""; }
	// END KGU 2015-10-18
	
	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/**
	 * A pattern how to embed the variable (right-hand side of an input instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "$1 = (new Scanner(System.in)).nextLine();"
	 */
	protected abstract String getInputReplacer();

	/**
	 * A pattern how to embed the expression (right-hand side of an output instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "System.out.println($1);"
	 */
	protected abstract String getOutputReplacer();
	// END KGU#18/KGU#23 2015-11-01
	
	// START KGU#78 2015-12-18: Enh. #23 We must know whether to create labels for simple breaks
	/**
	 * Specifies whether an instruction to leave the innermost enclosing loop (like "break;" in C)
	 * is available in the target language AND ALSO BREAKS CASE SELECTIONS (switch constructs).
	 * 
	 * @return true if and only if there is such an instruction
	 */
	protected abstract boolean breakMatchesCase();
	// END KGU#78 2015-12-18
	
	/************ Code Generation **************/
	
	// START KGU#16 2015-12-18: Enh. #66 - Code style option for opening brace placement
	protected boolean optionBlockBraceNextLine() {
		return (!eod.bracesCheckBox.isSelected());
	}
	// END KGU#16 2015-12-18	
	
	// START KGU#113 2015-12-18: Enh. #67 - Line numbering for BASIC export
	protected boolean optionBasicLineNumbering() {
		return (eod.lineNumbersCheckBox.isSelected());
	}
	// END KGU#113 2015-12-18	
	
	// KGU 2014-11-16: Method renamed (formerly: insertComment)
	// START KGU 2015-11-18: Method parameter list reduced by a comment symbol configuration
	/**
	 * Inserts the text of _element as comments into the code, using delimiters this.commentSymbolLeft
	 * and this.commentSymbolRight (if given) to enclose the comment lines, with indentation _indent 
	 * @param _element current NSD element
	 * @param _indent indentation string
	 */
	protected boolean insertAsComment(Element _element, String _indent)
	{
		if(eod.commentsCheckBox.isSelected()) {
			insertComment(_element.getText(), _indent);
			return true;
		}
		return false;
	}

	/**
	 * Inserts the comment part of _element into the code, using delimiters this.commentSymbolLeft
	 * and this.commentSymbolRight (if given) to enclose the comment lines, with indentation _indent
	 * @param _element current NSD element
	 * @param _indent indentation string
	 */
	protected void insertComment(Element _element, String _indent)
	{
		this.insertComment(_element.getComment(), _indent);
	}

	/**
	 * Inserts all lines of the given StringList as a series of single comment lines to the exported code
	 * @param _text - the text to be inserted as comment
	 * @param _indent - indentation string
	 */
	protected void insertComment(String _text, String _indent)
	{
		String[] lines = _text.split("\n");
		for (int i = 0; i < lines.length; i++)
		{
			code.add(_indent + commentSymbolLeft() + " " + lines[i] + " " + commentSymbolRight());
		}
	}

	/**
	 * Inserts all lines of the given StringList as a series of single comment lines to the exported code
	 * @param _sl - the text to be inserted as comment
	 * @param _indent - indentation string
	 */
	protected void insertComment(StringList _sl, String _indent)
	{
		for (int i = 0; i < _sl.count(); i++)
		{
        	// The following splitting is just to avoid empty comment lines and broken
        	// comment lines (though the latter shouldn't be possible here)
        	String commentLine = _sl.get(i);
        	// Skip an initial empty comment line
       		if (i > 0 || !commentLine.isEmpty()) {
       			insertComment(commentLine, _indent);
       		}
		}
	}
	
	/**
	 * Inserts a multi-line comment with configurable comment delimiters for the staring line, the
	 * continuation lines, and the trailing line.
	 * @param _sl - the StringList to be written as commment
	 * @param _indent - the basic indentation 
	 * @param _start - comment symbol for the leading comment line (e.g. "/**", if null then this is omitted)
	 * @param _cont - comment symbol for the continuation lines
	 * @param _end - comment symbol for trailing line (e.g. " *\/", if null then no trailing line is generated)
	 */
	protected void insertBlockComment(StringList _sl, String _indent, String _start, String _cont, String _end)
	{
		if (_start != null)
		{
			code.add(_indent + _start);
		}
		for (int i = 0; i < _sl.count(); i++)
		{
        	// The following splitting is just to avoid empty comment lines and broken
        	// comment lines (though the latter shouldn't be possible here)
        	String commentLine = _sl.get(i);
        	// Skip an initial empty comment line
       		if (i > 0 || !commentLine.isEmpty()) {
       			code.add(_indent + _cont + commentLine);
       		}
		}
		if (_end != null)
		{
			code.add(_indent + _end);
		}
	}
	// END KGU 2015-10-18

	/**
	 * Overridable general text transformation routine, performing the following steps:
	 * 1. Eliminates parser preference keywords listed below and unifies all operators
	 *    @see lu.fisch.Structorizer.elements.Element#unifyOperators(java.lang.String)
	 *         preAlt, preCase, preWhile, preRepeat,
	 *         postAlt, postCase, postWhile, postRepeat;
	 * 2. Tokenizes the result, processes the tokens by an overridable method
	 *    transformTokens(StringList), and re-concatenates the result;
	 * 3. Transforms Input and Output lines according to regular replacement expressions defined
	 *    by getInputReplacer() and getOutPutReplacer, respectively. This is done by overridable
	 *    methods transformInput(String) and transformOutput(), respectively.
	 *    This is only done if _input starts with one of the configured Input and Output keywords 
	 * @param _input a line or the concatenated lines of an Element's text
	 * @return the transformed line (target language line)
	 */
	protected String transform(String _input)
	{
		return transform(_input, true);
	}

	/**
	 * Overridable general text transformation routine, performing the following steps:
	 * 1. Eliminates parser preference keywords listed below and unifies all operators
	 *         preAlt, preCase, preWhile, preRepeat,
	 *         postAlt, postCase, postWhile, postRepeat;
	 * 2. Tokenizes the result, processes the tokens by an overridable method
	 *    transformTokens(StringList), and re-concatenates the result;
	 * 3. Transforms Input and Output lines if _doInput and/or _doOutput are true, respectively
	 *    This is only done if _input starts with one of the configured Input and Output keywords 
	 * @see lu.fisch.Structorizer.elements.Element#unifyOperators(java.lang.String)
	 * @param _input - a line or the concatenated lines of an Element's text
	 * @param _doInputOutput - whether the third transforms are to be performed
	 * @return the transformed line (target language line)
	 */
	protected String transform(String _input, boolean _doInputOutput)
	{

		// General operator unification and dummy keyword elimination
		// START KGU#93 2015-12-21: Bugfix #41/#68/#69
//		_input = Element.transformIntermediate(_input);
//
//		// assignment transformation
//		_input = transformAssignment(_input);
		
		StringList tokens = Element.transformIntermediate(_input);
		// START KGU 2016-03-29: Unify all parser keywords
		String[] keywords = D7Parser.getAllProperties();
		for (int kw = 0; kw < keywords.length; kw++)
		{    				
			if (keywords[kw].trim().length() > 0)
			{
				StringList keyTokens = this.splitKeywords.elementAt(kw);
				int keyLength = keyTokens.count();
				int pos = -1;
				while ((pos = tokens.indexOf(keyTokens, pos + 1, !D7Parser.ignoreCase)) >= 0)
				{
					tokens.set(pos, keywords[kw]);
					for (int j=1; j < keyLength; j++)
					{
						tokens.delete(pos+1);
					}
				}
			}
		}
		// END KGU 2016-03-29
		String transformed = transformTokens(tokens);
		// END KGU#93 2015-12-21

		if (_doInputOutput)
		{
			// START KGU 2015-12-22: Avoid unnecessary transformation attempts
			//// input instruction transformation
			//transformed = transformInput(transformed);
			//// output instruction transformation
			//transformed = transformOutput(transformed);
			if (transformed.indexOf(D7Parser.input.trim()) >= 0)
			{
				transformed = transformInput(transformed);
			}
			if (transformed.indexOf(D7Parser.output.trim()) >= 0)
			{
				transformed = transformOutput(transformed);
			}
			// END KGU 2015-12-22
		}

		return transformed.trim();
	}
	
	// START KGU#93 2015-12-21: Bugfix #41/#68/#69
	/**
	 * Transforms operators and other tokens from the given intermediate
	 * language into tokens of the target language.
	 * OVERRIDE this! (Method just returns the reconcatentated tokens)
	 * @param _interm - a code line in intermediate syntax
	 * @return transformed string
	 */
	protected String transformTokens(StringList tokens)
	{
		return tokens.concatenate();
	}
	
//	/**
//	 * Transforms assignments in the given intermediate-language code line.
//	 * OVERRIDE this! (Method just returns _interm without changes)
//	 * @param _interm - a code line in intermediate syntax
//	 * @return transformed string
//	 */
//	@Deprecated
//	protected String transformAssignment(String _interm)
//	{
//		return _interm;
//	}
	// END KGU#93 2015-12-21
	
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
		return _type;
	}
	// END KGU#1 2015-11-30	
	
	/**
	 * Detects whether the given code line starts with the configured input keystring
	 * and if so replaces it according to the regex pattern provided by getInputReplacer()
	 * @param _interm - a code line in intermediate syntax
	 * @return transformed input instruction or _interm unchanged
	 */
	protected String transformInput(String _interm)
	{
		String subst = getInputReplacer();
		// Between the input keyword and the variable name there MUST be some blank...
		String keyword = D7Parser.input.trim();
		if (!keyword.isEmpty() && _interm.startsWith(keyword))
		{
			String matcher = Matcher.quoteReplacement(keyword);
			if (Character.isJavaIdentifierPart(keyword.charAt(keyword.length()-1)))
			{
				matcher = matcher + "[ ]";
			}
                        
			// Start - BFI (#51 - Allow empty input instructions)
			if(!_interm.matches("^" + matcher + "(.*)"))
			{
				_interm += " ";
			}
			// End - BFI (#51)
			
			_interm = _interm.replaceFirst("^" + matcher + "(.*)", subst);
		}
		return _interm;
	}

	/**
	 * Detects whether the given code line starts with the configured output keystring
	 * and if so replaces it according to the regex pattern provided by getOutputReplacer()
	 * @param _interm - a code line in intermediate syntax
	 * @return transformed output instruction or _interm unchanged
	 */
	protected String transformOutput(String _interm)
	{
		String subst = getOutputReplacer();
		// Between the input keyword and the variable name there MUST be some blank...
		String keyword = D7Parser.output.trim();
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
			
			_interm = _interm.replaceFirst("^" + matcher + "(.*)", subst);
		}
		return _interm;
	}
	// END KGU#18/KGU#23 2015-11-01
	

	// START KGU#74 2015-11-30
	/**
	 * We do a recursive analysis for loops, returns and jumps of type "leave"
	 * to be able to place equivalent goto instructions and their target labels
	 * on demand.
	 * Maps Jump instructions and Loops (as potential jump targets) to unique
	 * numbers used for the creation of unambiguous goto or break labels.
	 * 
	 * The mapping is gathered in this.jumpTable.
	 * If a return instruction with value is encountered, this.returns will be set true
	 *   
	 * @param _squeue - instruction sequence to be analysed 
	 * @return true iff there is no execution path without a value returned.
	 */
	protected boolean mapJumps(Subqueue _squeue)
	{
		boolean surelyReturns = false;
		Iterator<Element> iter = _squeue.getIterator();
		while (iter.hasNext() && !surelyReturns)
		{
			Element elem = iter.next();
			// If we detect a Jump element of type leave then we detect its target
			// and label both
			if (elem instanceof Jump)
			{
				String jumpText = elem.getText().getLongString().trim();
				if (jumpText.matches(Matcher.quoteReplacement(D7Parser.preReturn) + "([\\W].*|$)"))
				{
					boolean hasResult = !jumpText.substring(D7Parser.preReturn.length()).trim().isEmpty();
					if (hasResult) this.returns = true;
					// Further investigation would be done in vain - the remaining sequence is redundant
					return hasResult;
				}
				else if (jumpText.matches(Matcher.quoteReplacement(D7Parser.preExit) + "([\\W].*|$)"))
				{
					// Doesn't return a regular result but we won't get to the end, so a default return is
					// not required, we handle this as if a result would have been returned.
					surelyReturns = true;
				}
				// Get the number of requested exit levels
				int levelsUp = 0;
				if (jumpText.isEmpty())
				{
					levelsUp = 1;
				}
				else if (jumpText.matches(Matcher.quoteReplacement(D7Parser.preLeave) + "([\\W].*|$)"))
				{
					levelsUp = 1;
					try {
						levelsUp = Integer.parseInt(jumpText.substring(D7Parser.preLeave.length()).trim());
					}
					catch (NumberFormatException ex)
					{
						System.out.println("Unsuited leave argument in Element \"" + jumpText + "\"");
					}
				}
				// Try to find the target loop
				// START KGU#78 2015-12-18: Enh. #23 specific handling only required if there is a break instruction
				//boolean simpleBreak = levelsUp == 1;	// For special handling of Case context
				// Simple break instructions usually require special handling of Case context
				boolean simpleBreak = levelsUp == 1 && this.breakMatchesCase();
				// END KGU#78 2015-12-18
				Element parent = elem.parent;
				while (parent != null && !(parent instanceof Parallel) && levelsUp > 0)
				{
					if (parent instanceof ILoop)
					{
						if (--levelsUp == 0 && !simpleBreak)	// Target reached?
						{
							// Is target loop already associated with a label?
							Integer label = this.jumpTable.get(parent);
							if (label == null)
							{
								// If not then associate it with a label
								label = this.labelCount++;
								this.jumpTable.put(parent, label);
							}
							this.jumpTable.put(elem, label);
						}
					}
					else if (parent instanceof Case)
					{
						// If we were within a selection (switch) then we must use "goto" to get out
						simpleBreak = false;
					}
					parent = parent.parent;
				}
				if (levelsUp > 0)
				{
					// Target couldn't be found, so mark the jump with an error marker
					this.jumpTable.put(elem, -1);
				}
				else {
					// After an unconditional jump, the remaining instructions are redundant
					return surelyReturns;
				}
			}
			// No jump: then only recursively descend
			else if (elem instanceof Alternative)
			{
				boolean willReturnT = mapJumps(((Alternative)elem).qTrue);
				boolean willReturnF = mapJumps(((Alternative)elem).qFalse);
				surelyReturns = willReturnT && willReturnF;
			}
			else if (elem instanceof Case)
			{
				boolean willReturn = false;
				for (int i = 0; i < ((Case)elem).qs.size(); i++)
				{
					boolean caseReturns = mapJumps(((Case)elem).qs.elementAt(i));
					willReturn = willReturn && caseReturns;
				}
				if (willReturn) surelyReturns = true;
			}
			else if (elem instanceof ILoop)	// While, Repeat, For, Forever
			{
				if (mapJumps(((ILoop)elem).getBody())) surelyReturns = true;
			}
			else if (elem instanceof Parallel)
			{
				// There is no regular return out of a parallel thread...
				for (int i = 0; i < ((Parallel)elem).qs.size(); i++)
				{
					mapJumps(((Parallel)elem).qs.elementAt(i));
				}
			}
			else if (elem instanceof Instruction)
			{
				StringList text = elem.getText();
				for (int i = 0; i < text.count(); i++)
				{
					String line = text.get(i);
					if (line.matches(Matcher.quoteReplacement(D7Parser.preReturn) + "([\\W].*|$)"))
					{
						boolean hasResult = !line.substring(D7Parser.preReturn.length()).trim().isEmpty();
						if (hasResult)
						{
							this.returns = true;
							// Further investigation would be done in vain - the remaining sequence is redundant
							surelyReturns = true;
						}
					}
				}
				
			}
		}
		return surelyReturns;
	}
	
	
	// START KGU#109/KGU#141 2016-01-16: New for ease of fixing #61 and #112
	/**
	 * Decomposes the left-hand side of an assignment passed in as _lval
	 * into three strings:
	 * [0] - type specification (a sequence of tokens, may be empty)
	 * [1] - variable name (a single token supposed to be the identifier)
	 * [2] - index expression (if _lval is an indexed variable, else empty)
	 * @param _lval a string found on the left-hand side of an assignment operator
	 * @return String array of [0] type, [1] name, [2] index; all but [1] may be empty
	 */
	protected String[] lValueToTypeNameIndex(String _lval)
	{
		// Avoid too much nonsense on indexed variables
    	Regex r = new Regex("(.*?)[\\[](.*?)[\\]](.*?)","$1 $3");
    	String name = r.replaceAll(_lval);
		String type = "";
		// Check Pascal and BASIC style of type specifications
		int subPos = name.indexOf(":");
		if (subPos > 0)
		{
			type = name.substring(subPos + 1).trim() + " ";
			name = name.substring(0, subPos).trim();
		}
		else if ((subPos = name.indexOf(" as ")) > 0)
		{
			type = name.substring(subPos + " as ".length()).trim() + " ";
			name = name.substring(0, subPos).trim();
		}
		// Now split the assumed name to check C-style type specifications
		StringList nameParts = StringList.explode(name, " ");
		if (type.isEmpty() || nameParts.count() > 1)
		{
			type = nameParts.concatenate(" ", 0, nameParts.count()-1).trim() + " ";
		}
		name = nameParts.get(nameParts.count()-1);
		//r = new Regex("(.*?)[\\[](.*?)[\\]](.*?)","$2");
		String index = "";
		
		if ((subPos = _lval.indexOf('[')) >= 0 && _lval.indexOf(']', subPos+1) >= 0)
		{
			index = _lval.replaceAll("(.*?)[\\[](.*?)[\\]](.*?)","$2").trim();
		}
		String[] typeNameIndex = {type, name, index};
		return typeNameIndex;
	}
	// END KGU#109/KGU#141 2016-01-16
	
	// START KGU#61 2016-03-23: Enh. #84 (FOR-IN loop infrastructure)
	protected StringList extractForInListItems(For _for)
	{
		String valueList = _for.getValueList();
		StringList items = null;
		boolean isComplexObject = (new Function(valueList)).isFunction() || this.varNames.contains(valueList);
		if (valueList.startsWith("{") && valueList.endsWith("}"))
		{
			items = Element.splitExpressionList(valueList.substring(1, valueList.length()-1), ",");
		}
		else if (valueList.contains(","))
		{
			items = Element.splitExpressionList(valueList, ",");
		}
		else if (!isComplexObject && valueList.contains(" "))
		{
			items = Element.splitExpressionList(valueList, " ");
		}
		return items;
	}
	// END KGU#61 2016-03-23
 	
    protected void generateCode(Instruction _inst, String _indent)
	{
            //
	}
	
	protected void generateCode(Alternative _alt, String _indent)
	{
		// code.add(_indent+"");
		generateCode(_alt.qTrue,_indent+this.getIndent());
		// code.add(_indent+"");
		generateCode(_alt.qFalse,_indent+this.getIndent());
		// code.add(_indent+"");
	}

	protected void generateCode(Case _case, String _indent)
	{
		// code.add(_indent+"");
		for(int i=0; i < _case.qs.size(); i++)
		{
			// code.add(_indent+"");
			generateCode((Subqueue) _case.qs.get(i), _indent+this.getIndent());
			// code.add(_indent+"");
		}
		// code.add(_indent+"");
	}

	protected void generateCode(For _for, String _indent)
	{
		// code.add(_indent+"");
		generateCode(_for.q, _indent + this.getIndent());
		// code.add(_indent+"");
	}

	protected void generateCode(While _while, String _indent)
	{
		// code.add(_indent+"");
		generateCode(_while.q, _indent + this.getIndent());
		// code.add(_indent+"");
	}

	protected void generateCode(Repeat _repeat, String _indent)
	{
		// code.add(_indent+"");
		generateCode(_repeat.q, _indent + this.getIndent());
		// code.add(_indent+"");
	}

	protected void generateCode(Forever _forever, String _indent)
	{
		// code.add(_indent+"");
		generateCode(_forever.q, _indent + this.getIndent());
		// code.add(_indent+"");
	}
	
	protected void generateCode(Call _call, String _indent)
	{
		// code.add(_indent+"");
	}

	protected void generateCode(Jump _jump, String _indent)
	{
		// code.add(_indent+"");
	}

	protected void generateCode(Parallel _para, String _indent)
	{
		// code.add(_indent+"");
	}

	protected void generateCode(Element _ele, String _indent)
	{
		if(_ele.getClass().getSimpleName().equals("Instruction"))
		{
			generateCode((Instruction) _ele, _indent);
		}
		else if(_ele.getClass().getSimpleName().equals("Alternative"))
		{
			generateCode((Alternative) _ele,_indent);
		}
		else if(_ele.getClass().getSimpleName().equals("Case"))
		{
			generateCode((Case) _ele,_indent);
		}
		else if(_ele.getClass().getSimpleName().equals("Parallel"))
		{
			generateCode((Parallel) _ele,_indent);
		}
		else if(_ele.getClass().getSimpleName().equals("For"))
		{
			generateCode((For) _ele, _indent);
		}
		else if(_ele.getClass().getSimpleName().equals("While"))
		{
			generateCode((While) _ele,_indent);
		}
		else if(_ele.getClass().getSimpleName().equals("Repeat"))
		{
			generateCode((Repeat) _ele,_indent);
		}
		else if(_ele.getClass().getSimpleName().equals("Forever"))
		{
			generateCode((Forever) _ele,_indent);
		}
		else if(_ele.getClass().getSimpleName().equals("Call"))
		{
			generateCode((Call) _ele,_indent);
		}
		else if(_ele.getClass().getSimpleName().equals("Jump"))
		{
			generateCode((Jump) _ele,_indent);
		}
	}
	
	protected void generateCode(Subqueue _subqueue, String _indent)
	{
		// code.add(_indent+"");
		for(int i=0;i<_subqueue.getSize();i++)
		{
			generateCode((Element) _subqueue.getElement(i),_indent);
		}
		// code.add(_indent+"");
	}

	/******** Public Methods *************/

	public String generateCode(Root _root, String _indent)
	{
		// START KGU#74 2015-11-30: General preprocessing phase 1
		// Code analysis and Header analysis
		String procName = _root.getMethodName();
		boolean alwaysReturns = mapJumps(_root.children);
		StringList paramNames = new StringList();
		StringList paramTypes = new StringList();
		_root.collectParameters(paramNames, paramTypes);
		String resultType = _root.getResultType();
		// START KGU#61/KGU#129 2016-03-22: Now common field for all generator classes
		//StringList varNames = _root.getVarNames(_root, false, true);	// FIXME: FOR loop vars are missing
		this.varNames = _root.getVarNames(_root, false, true);	// FIXME: FOR loop vars are missing
		// END KGU#61/KGU#129
		this.isResultSet = varNames.contains("result", false);
		this.isFunctionNameSet = varNames.contains(procName);
		
		String preaIndent = generateHeader(_root, _indent, procName, paramNames, paramTypes, resultType);
		String bodyIndent = generatePreamble(_root, preaIndent, varNames);
		// END KGU#74 2015-11-30
		
		// code.add("");
		generateCode(_root.children, bodyIndent);
		// code.add("");
		
		// START KGU#74 2015-11-30: Result preprocessing
		generateResult(_root, preaIndent, alwaysReturns, varNames);
		generateFooter(_root, _indent);
		// END KGU#74 2015-11-30

		return code.getText();
	}
	
	// Just dummy implementations to be overridden by subclasses
	/**
	 * Composes the heading for the program or function according to the
	 * syntactic rules of the target language and adds it to this.code.
	 * @param _root - The diagram root element
	 * @param _indent - the initial indentation string
	 * @param _procName - the procedure name
	 * @param _paramNames - list of the argument names
	 * @param _paramTypes - list of corresponding type names (possibly null) 
	 * @param _resultType - result type name (possibly null)
	 * @return the default indentation string for the subsequent stuff
	 */
	protected String generateHeader(Root _root, String _indent, String _procName,
			StringList _paramNames, StringList _paramTypes, String _resultType)
	{
		return _indent + this.getIndent();
	}
	/**
	 * Generates some preamble (i.e. comments, language declaration section etc.)
	 * and adds it to this.code.
	 * @param _root - the diagram root element
	 * @param _indent - the current indentation string
	 * @param varNames - list of variable names introduced inside the body
	 */
	protected String generatePreamble(Root _root, String _indent, StringList _varNames)
	{
		return _indent;
	}
	/**
	 * Creates the appropriate code for returning a required result and adds it
	 * (after the algorithm code of the body) to this.code)
	 * @param _root - the diagram root element
	 * @param _indent - the current indentation string
	 * @param alwaysReturns - whether all paths of the body already force a return
	 * @param varNames - names of all assigned variables
	 */
	protected String generateResult(Root _root, String _indent, boolean alwaysReturns, StringList varNames)
	{
		return _indent;
	}
	/**
	 * Method is to finish up after the text insertions of the diagram, i.e. to close an open block. 
	 * @param _root - the diagram root element 
	 * @param _indent - the current indentation string
	 */
	protected void generateFooter(Root _root, String _indent)
	{
		
	}
	// END KGU#74 2015-11-30
	
	public void exportCode(Root _root, File _currentDirectory, Frame frame)
	{
		try
		{
			Ini ini = Ini.getInstance();
			ini.load();
			eod = new ExportOptionDialoge(frame);	// FIXME (KGU) What do we need this hidden dialog for?
			if(ini.getProperty("genExportComments","0").equals("true"))
				eod.commentsCheckBox.setSelected(true);
			else 
				eod.commentsCheckBox.setSelected(false);
			// START KGU#16/KGU#113 2015-12-18: Enh. #66, #67
			eod.bracesCheckBox.setSelected(ini.getProperty("genExportBraces", "0").equals("true"));
			eod.lineNumbersCheckBox.setSelected(ini.getProperty("genExportLineNumbers", "0").equals("true"));
			// END KGU#16/KGU#113 2015-12-18
		} 
		catch (FileNotFoundException ex)
		{
			ex.printStackTrace();
		} 
		catch (IOException ex)
		{
			ex.printStackTrace();
		}

		JFileChooser dlgSave = new JFileChooser();
		dlgSave.setDialogTitle(getDialogTitle());

		// set directory
		if(_root.getFile()!=null)
		{
			dlgSave.setCurrentDirectory(_root.getFile());
		}
		else
		{
			dlgSave.setCurrentDirectory(_currentDirectory);
		}

		// propose name
		// START KGU 2015-10-18: Root has got a mechanism for this!
		//		String nsdName = _root.getText().get(0);
		//		nsdName.replace(':', '_');
		//		if(nsdName.indexOf(" (")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf(" ("));}
		//		if(nsdName.indexOf("(")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf("("));}
		String nsdName = _root.getMethodName();
		// END KGU 2015-10-18
		dlgSave.setSelectedFile(new File(nsdName));

		dlgSave.addChoosableFileFilter((javax.swing.filechooser.FileFilter) this);
		int result = dlgSave.showSaveDialog(frame);

		/***** file_exists check here!
		 if(file.exists())
		 {
		 JOptionPane.showMessageDialog(null,file);
		 int response = JOptionPane.showConfirmDialog (null,
		 "Overwrite existing file?","Confirm Overwrite",
		 JOptionPane.OK_CANCEL_OPTION,
		 JOptionPane.QUESTION_MESSAGE);
		 if (response == JOptionPane.CANCEL_OPTION)
		 {
		 return;
		 }
		 else
		 */
		String filename = new String();

		boolean saveIt = true;

		if (result == JFileChooser.APPROVE_OPTION) 
		{
			filename=dlgSave.getSelectedFile().getAbsoluteFile().toString();
			if(!isOK(filename))
			{
				filename+="."+getFileExtensions()[0];
			}
		}
		else
		{
			saveIt = false;
		}

		//System.out.println(filename);

		if (saveIt == true) 
		{
			File file = new File(filename);
			boolean writeDown = true;

			if(file.exists())
			{
				int response = JOptionPane.showConfirmDialog (null,
						"Overwrite existing file?","Confirm Overwrite",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);
				if (response == JOptionPane.NO_OPTION)
				{
					writeDown=false;
				}
			}
			if(writeDown==true)
			{

				// START KGU 2016-03-29: Pre-processed match patterns for better identification of complicated keywords
		    	this.splitKeywords.clear();
		    	String[] keywords = D7Parser.getAllProperties();
		    	for (int k = 0; k < keywords.length; k++)
		    	{
		    		this.splitKeywords.add(Element.splitLexically(keywords[k], false));
		    	}
				// END KGU 2016-03-29

		    	try
				{
					// START KGU 2015-10-18: This didn't make much sense: Why first insert characters that will be replaced afterwards?
					// (And with possibly any such characters that had not been there for indentation!)
					//    String code = BString.replace(generateCode(_root,"\t"),"\t",getIndent());
					String code = generateCode(_root, "");
					// END KGU 2015-10-18

					BTextfile outp = new BTextfile(filename);
					outp.rewrite();
					outp.write(code);
					outp.close();
				}
				catch(Exception e)
				{
					JOptionPane.showMessageDialog(null,"Error while saving the file!\n" + e.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	} 
	
	/******* FileFilter Extension *********/
	protected boolean isOK(String _filename)
	{
		boolean res = false;
		// START KGU 2016-01-16: Didn't work for mixed-case extensions like ".Mod" - and it was inefficient
//		if(getExtension(_filename)!=null)
//		{
//			for(int i =0; i<getFileExtensions().length; i++)
//			{
//				res = res || (getExtension(_filename).equals(getFileExtensions()[i]));
//			}
//		}
		String ext = getExtension(_filename); 
		if (ext != null)
		{
			for (int i =0; i<getFileExtensions().length; i++)
			{
				res = res || (ext.equalsIgnoreCase(getFileExtensions()[i]));
			}
		}
		// END KGU 2016-01-16
		return res;
	}
	
	private static String getExtension(String s) 
	{
		String ext = null;
		int i = s.lastIndexOf('.');
		
		if (i > 0 &&  i < s.length() - 1) 
		{
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}
	
	private static String getExtension(File f) 
	{
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		
		if (i > 0 &&  i < s.length() - 1) 
		{
			ext = s.substring(i+1).toLowerCase();
		}
		
		return ext;
	}
	
	public String getDescription() 
	{
        return getFileDescription();
    }
	
    public boolean accept(File f) 
	{
        if (f.isDirectory()) 
		{
            return true;
        }
		
        String extension = getExtension(f);
        if (extension != null) 
		{
            return isOK(f.getName());
		}
		
        return false;
    }
	

	/******* Constructor ******************/

	public Generator()
	{
	}
	
}

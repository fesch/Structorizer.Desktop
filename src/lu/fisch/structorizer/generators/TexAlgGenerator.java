/*
    Structorizer
    A little tool which you can use to create Nassi-Shneiderman Diagrams (NSD)

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
 *      Author:         Kay Gürtzig
 *
 *      Description:    Generator class for several LaTeX pseudocode modules
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2021-06-08      First Issue on behalf of enhancement request #953
 *      Kay Gürtzig     2022-08-23      Structorizer version inserted as LaTeX comment
 *      Kay Gürtzig     2025-07-03      Bugfix #1195: disabled check unified (--> isDisabled(true)),
 *                                      missing Override annotations added
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *      2021-06-07 (Kay Gürtzig)
 *      - Support for further, alternative algorithm/pseudocode packages for LaTeX might be added
 *        on demand.
 *
 ******************************************************************************************************///

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import lu.fisch.structorizer.elements.Alternative;
import lu.fisch.structorizer.elements.Call;
import lu.fisch.structorizer.elements.Case;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.Forever;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.elements.Jump;
import lu.fisch.structorizer.elements.Parallel;
import lu.fisch.structorizer.elements.Param;
import lu.fisch.structorizer.elements.Repeat;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.structorizer.elements.Try;
import lu.fisch.structorizer.elements.While;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.parsers.CodeParser;
import lu.fisch.utils.StringList;

/**
 * Configurable code generator for several pseudocode / algorithm modules for LaTeX (on behalf of
 * enhancement request #953 by Karl-Heinz Becker), derived from TexGenerator (which produces StrukTeX
 * code).
 * @author Kay Gürtzig
 */
public class TexAlgGenerator extends Generator {
	
	/**
	 * Defines the names of the supported algorithm/pseudocode packages
	 * for LaTeX.
	 * Each String array contains definitions in the order of the enumerator
	 * PackageCommands.
	 */
	private static final String[] PACKAGE_NAMES = new String[] {
		// algorithmicx
		"algpseudocode",
		// algorithmic
		"algorithmic",
		// algorithm2e
		"algorithm2e",
		// pseudocode
		"pseudocode"
	};
	
	@Override
	protected String getDialogTitle() {
		return "Export LaTeX pseudocode/algo ...";
	}

	@Override
	protected String getFileDescription() {
		return "LaTeX Algorithmic Code";
	}

	@Override
	protected String[] getFileExtensions() {
		String[] exts = {"tex"};
		return exts;
	}

	@Override
	protected String getIndent() {
		return "  ";
	}

	@Override
	protected String commentSymbolLeft() {
		switch (packageIndex) {
		case 0:
			return "\\State \\Comment{";
		case 1:
			return "\\STATE \\COMMENT{";
		case 2:
			return "\\tcc{";
		case 3:
			return "\\COMMENT{";
		default:
			return "%";	// TeX line comment symbol
		}
	}

	@Override
	protected String commentSymbolRight() {
		switch (packageIndex) {
		case 0:
		case 1:
		case 2:
			return "}";
		case 3:
			return "}\\\\";
		default:
			return "";
		}
	}

	/*=========== Code Generation ============*/
	
	/**
	 * Specifies the start and end LaTeX commands for: MAIN, PROCEDURE, FUNCTION per
	 * algorithm package.<br/>
	 * Indices: <ol>
	 * <li>package</li>
	 * <li>diagram type + start/end</li>
	 * </ol>
	 */
	private static final String[][] ROUTINE_MACROS = new String[][] {
		// algorithmicx
		{"\\Procedure{%1}{ }", "\\EndProcedure",
			"\\Procedure{%1}{%2}", "\\EndProcedure",
			"\\Function{%1}{%2}", "\\EndFunction",
			"\\Procedure{%1}{ }", "\\EndProcedure"},
		// algorithmic
		{"", "", "", "", "", ""},
		// algorithm2e
		{"\\Prog{\\FuncSty{%1}}{", "}",
			"\\Proc{\\FuncSty{%1(}\\ArgSty{%2}\\FuncSty{)}}{", "}",
			"\\Func{\\FuncSty{%1(}\\ArgSty{%2}\\FuncSty{)}:%3}{", "}",
			"\\Incl{\\FuncSty{%1}}{", "}"},
		// pseudocode
		{"\\MAIN", "\\ENDMAIN",
			"\\PROCEDURE{%1}{%2}", "\\ENDPROCEDURE",
			"\\PROCEDURE{%1}{%2}", "\\ENDPROCEDURE",
			"", ""},
	};
	
	/**
	 * Specifies specific algorithm2e environments for programs, procedures,
	 * functions, and includables<br/>
	 * Indices: <ol>
	 * <li>package</li>
	 * <li>position (start/end)</li>
	 * </ol>
	 */
	private static final String[] ENVIRONMENTS2E = new String[] {
		"\\begin{algorithm}", "\\end{algorithm}",
		"\\begin{procedure}", "\\end{procedure}",
		"\\begin{function}", "\\end{function}",
		"\\begin{algorithm}", "\\end{algorithm}"
	};
	
	/**
	 * Specifies the start and end LaTeX commands for blocks or null by
	 * algorithm package. Both elements may be {@code null} if explicit
	 * blocks aren't needed.<br/>
	 * Indices: <ol>
	 * <li>package</li>
	 * <li>position (start/end)</li>
	 * </ol>
	 */
	private static final String[][] BLOCK_MACROS = new String[][] {
		// algorithmicx
		{null, null},
		// algorithmic
		{null, null},
		// algorithm2e
		{"\\Begin{", "}"},
		// pseudocode
		{"\\BEGIN", "\\END\\\\"}
	};
	
	/**
	 * Specifies the command (if existent) to insert an instruction
	 * by algorithm package<br/>
	 * Indices: <ol>
	 * <li>package</li>
	 * <li>command (normal/output)</li>
	 * </ol>
	 */
	private static final String[][] INSTR_MACRO = new String[][] {
		// algorithmicx
		{"\\State \\(%1\\)%2", "\\State \\(%1\\)%2"},
		// algorithmic
		{"\\STATE \\(%1\\)%2", "%1%2"},
		// algorithm2e
		{"\\(%1\\)\\;", "%1"},
		// pseudocode
		{"%1%2\\\\", "%1%2\\\\"}
	};
	
	/**
	 * Specifies the LaTeX commands for Alternatives: IF, ENDIF/null, ELSE, ELSEIF by
	 * algorithm package.<br/>
	 * If the second element is null then multi-line branches must be enclosed
	 * in blocks, if the 3rd element is null then in case of a complete alternative
	 * (both branches non-empty) the 5th and 6th element are to be used instead of the
	 * first and third ones.<br/>
	 * Indices: <ol>
	 * <li>package</li>
	 * <li>command (IF/ENDIF/ELSE/ELSIF)</li>
	 * </ol>
	 */
	private static final String[][] IF_MACROS = new String[][] {
		// algorithmicx
		{"\\If{\\(%1\\)}%C",       "\\EndIf", "\\Else", "\\Elsif{\\(%1\\)}%C"},
		// algorithmic
		{"\\IF{\\(%1\\)}%C",       "\\ENDIF", "\\ELSE", "\\ELSIF{\\(%1\\)}%C"},
		// algorithm2e 
		{"\\If{\\(%1\\)}{",        "}",       null,     "\\ElseIf{\\(%1\\)}", "\\eIf{\\(%1\\)}{", "}{"},
		// pseudocode
		{"\\IF %1 \\THEN",   null,      "\\ELSE", "\\ELSEIF %1 \\THEN"},
	};
	
	/**
	 * Specifies the LaTeX commands for Multi-way selections: CASE, ENDCASE,
	 * SELECTOR, ENDSELECTOR, OTHER by algorithm package.<br/>
	 * If the first element is null then the structure must be decomposed into an
	 * if-elsif chain (no further element may be expected then).<br/>
	 * Indices: <ol>
	 * <li>package</li>
	 * <li>command (CASE/ENDCASE/SELECTOR/ENDSELECTOR/OTHER)</li>
	 * </ol>
	 */
	private static final String[][] CASE_MACROS = new String[][] {
		// algorithmicx
		{"\\Case{%1}%C",    "\\EndCase", "\\Selector{%1}", "\\EndSelector", "\\Other",  "\\EndOther"},
		// algorithmic
		{null},
		// algorithm2e
		{"\\Switch{%1}{", "}",            "\\Case{%1}{",    "}",             "\\Other{", "}"},
		// pseudocode
		{null},
	};
	
	/**
	 * Specifies the LaTeX commands for FOR loops: FOR, FOR parameterized, ENDFOR,
	 * FORALL, ENDFORALL by algorithm package.<br/>
	 * If the first element is null then the FOR loop requires parameterized form,
	 * if the 3rd and 5th element are {@code null} then non-atomic bodies must be
	 * blocked.<br/>
	 * Indices: <ol>
	 * <li>package</li>
	 * <li>command (FOR compact/FOR parameterized/ENDFOR/FORALL/ENDFORALL)</li>
	 * </ol>
	 */
	private static final String[][] FOR_MACROS = new String[][] {
		// algorithmicx
		{"\\For{\\(%1\\)}%C", "\\For{\\(%1 \\gets %2\\) \\textbf{to} \\(%3\\) \\textbf{by} \\(%4\\)}", "\\EndFor", "\\ForAll{\\(%1 \\in %2\\)}%C", "\\EndFor"},
		// algorithmic
		{"\\FOR{\\(%1\\)}%C", "\\FOR{\\(%1 \\gets %2\\) \\TO \\(%3\\) \\textbf{by} \\(%4\\)}", "\\ENDFOR", "\\FORALL{\\(%1 \\in %2\\)}%C", "\\ENDFOR"},
		// algorithm2e
		{"\\For{\\(%1\\)}{", "\\For{\\(%1\\leftarrow %2\\) \\KwTo \\(%3\\) \\textbf{by} \\(%4\\)}{", "}", "\\ForEach{\\(%1 \\in %2\\)}{", "}"},
		// pseudocode
		{"\\FOR %1 \\DO", "\\FOR %1 \\gets %2 %5 %3 %6 \\DO", null, "\\FOREACH %1 \\in %2 \\DO", null},
	};
	
	/**
	 * Specifies the LaTeX commands for WHILE loops: WHILE, ENDWHILE by algorithm
	 * package.<br/>
	 * If the 2nd element is {@code null} then non-atomic bodies must be blocked.<br/>
	 * Indices: <ol>
	 * <li>package</li>
	 * <li>command (WHILE/ENDWHILE)</li>
	 * </ol>
	 */
	private static final String[][] WHILE_MACROS = new String[][] {
		// algorithmicx
		{"\\While{\\(%1\\)}%C", "\\EndWhile"},
		// algorithmic
		{"\\WHILE{\\(%1\\)}%C",  "\\ENDWHILE"},
		// algorithm2e
		{"\\While{\\(%1\\)}{", "}"},
		// pseudocode
		{"\\WHILE %1 \\DO", null},
	};

	/**
	 * Specifies the LaTeX commands for REPEAT loops: REPEAT, ENDREPEAT by
	 * algorithm package.<br/>
	 * If the 2nd element is {@code null} then non-atomic bodies must be
	 * blocked.<br/>
	 * Indices: <ol>
	 * <li>package</li>
	 * <li>command (REPEAT/ENDREPEAT)</li>
	 * </ol>
	 */
	private static final String[][] REPEAT_MACROS = new String[][] {
		// algorithmicx
		{"\\Repeat%C", "\\Until{\\(%1\\)}%2"},
		// algorithmic
		{"\\REPEAT%C",  "\\UNTIL{\\(%1\\)}%2"},
		// algorithm2e
		{"\\Repeat{\\(%1\\)}{", "}"},
		// pseudocode
		{"\\REPEAT", "\\UNTIL %1%2\\\\"},
	};

	/**
	 * Specifies the LaTeX commands for the eternal loops: LOOP, ENDLOOP by
	 * algorithm package.<br/>
	 * If the 2nd element is {@code null} then non-atomic bodies must be
	 * blocked.<br/>
	 * Indices: <ol>
	 * <li>package</li>
	 * <li>command (LOOP/ENDLOOP)</li>
	 * </ol>
	 */
	private static final String[][] LOOP_MACROS = new String[][] {
		// algorithmicx
		{"\\Loop%C", "\\EndLoop"},
		// algorithmic
		{"\\LOOP%C",  "\\ENDLOOP"},
		// algorithm2e
		{"\\While{\\textbf{true}}{", "}"},
		// pseudocode
		{"\\WHILE \\TRUE", null},
	};

	/**
	 * Specifies the macro (if existent) to markup a call by algorithm package<br/>
	 * Index is package
	 */
	private static final String[] CALL_MACROS = new String[] {
		// algorithmicx
		"\\Call{%1}{%2}",
		// algorithmic
		null,
		// algorithm2e
		"\\Fn%1(%2)",
		// pseudocode
		"\\CALL{%1}{%2}",
	};
	
	/**
	 * Specifies the LaTeX commands for the Jump types: LEAVE, RETURN, EXIT, THROW by
	 * algorithm package.<br/>
	 * Indices: <ol>
	 * <li>package</li>
	 * <li>command (LEAVE/RETURN/EXIT/THROW)</li>
	 * </ol>
	 */
	private static final String[][] JUMP_MACROS = new String[][] {
		// algorithmicx
		{"\\State \\textbf{%1} \\(%2\\)%3", "\\State \\textbf{%1} \\(%2\\)%3", "\\State \\textbf{%1} \\(%2\\)%3", "\\State \\textbf{%1} \\(%2\\)%3"},
		// algorithmic
		{"\\STATE \\textbf{%1} \\(%2\\)%3", "\\RETURN\\(%2\\)%3", "\\STATE \\textbf{%1} \\(%2\\)%3", "\\STATE \\textbf{%1} \\(%2\\)%3"},
		// algorithm2e
		{"\\preLeave{\\(%2\\)}\\)\\;", "\\Return{\\(%2\\)}%3", "\\preExit{\\(%2\\)}\\;", "\\preThrow{\\(%2\\)}\\;"},
		// pseudocode
		{"\\textbf{%1}\\ %2%3\\\\", "\\RETURN{%2}%3\\\\", "\\EXIT %2%3\\\\", "\\textbf{%1}\\ %2%3\\\\"}
	};

	/**
	 * Specifies the LaTeX commands for Parallel sections: PARALLEL, ENDPARALLEL,
	 * THREAD, ENDTHREAD by algorithm package.<br/>
	 * If the 2nd element is null then non-atomic bodies must be blocked.<br/>
	 * <li>package</li>
	 * <li>command (PARALLEL/ENDPARALLEL/THREAD/ENDTHREAD)</li>
	 * </ol>
	 */
	private static final String[][] PARALLEL_MACROS = new String[][] {
		// algorithmicx
		{"\\Para%C", "\\EndPara", "\\Thread %1", "\\EndThread %1"},
		// algorithmic (we abuse the BODY environment to achieve indentation)
		{"\\STATE \\textbf{parallel} %C \\BODY",  "\\ENDBODY \\STATE \\textbf{end parallel}", "\\STATE \\textbf{thread} %1 \\BODY",  "\\ENDBODY \\STATE \\textbf{end thread} %1"},
		// algorithm2e
		{"\\Parallel{", "}", "\\Thread{%1}{", "}"},
		// pseudocode
		{"\\textbf{parallel}\\BEGIN", "\\END\\\\", "\\textbf{thread}\\ %1\\BEGIN", "\\END\\\\"}
	};

	/**
	 * Specifies the LaTeX commands for Try blocks: TRY, ENDTRY, CATCH, ENDCATCH,
	 * FINALLY, ENDFINALLY by algorithm package.<br/>
	 * If the 2nd element is null then non-atomic bodies must be blocked.<br/>
	 * <li>package</li>
	 * <li>command (TRY/ENDTRY/CATCH/ENDCATCH/FINALLY/ENDFINALLY)</li>
	 * </ol>
	 */
	private static final String[][] TRY_MACROS = new String[][] {
		// algorithmicx
		{"\\Try", "\\EndTry", "\\Catch %1", "\\EndCatch %1", "\\Finally", "\\EndFinally"},
		// algorithmic (we abuse the BODY environment to achieve indentation)
		{"\\STATE \\textbf{try} %C \\BODY",  "\\ENDBODY \\STATE \\textbf{end try}",
			"\\STATE \\textbf{catch} (\\(%1\\)) \\BODY",  "\\ENDBODY \\STATE \\textbf{end catch}",
			"\\STATE \\textbf{finally} \\BODY",  "\\ENDBODY \\STATE \\textbf{end finally}"},
		// algorithm2e
		{"\\Try{", "}", "\\Catch{%1}{", "}", "\\Finally{", "}"},
		// pseudocode
		{"\\textbf{try} \\BEGIN", "\\END\\\\", "\\textbf{catch}\\ (%1)\\BEGIN", "\\END\\\\", "\\textbf{finally} \\BEGIN", "\\END\\\\"}
	};

	/**
	 * May provide package-specific definitions or customisations to be inserted
	 * at the beginning of the document.<br/>
	 * Index is package number
	 */
	private static final String[] PACKAGE_CUSTOMIZATIONS = new String[] {
			// algorithmicx
			"\\algblockdefx[CASE]{Case}{EndCase}\n"
			+ "  [1]{\\textbf{case} \\(#1\\) \\textbf{of}}\n"
			+ "  {\\textbf{end\\ case}}\n"
			+ "\\algblockdefx[SELECT]{Selector}{EndSelector}\n"
			+ "  [1]{#1\\textbf{: begin}}\n"
			+ "  {\\textbf{end}}\n"
			+ "\\algblockdefx[OTHER]{Other}{EndOther}\n"
			+ "  {\\textbf{otherwise: begin}}\n"
			+ "  {\\textbf{end}}\n"
			+ "\\algblockdefx[TRY]{Try}{EndTry}\n"
			+ "  {\\textbf{try}}\n"
			+ "  {\\textbf{end\\ try}}\n"
			+ "\\algblockdefx[CATCH]{Catch}{EndCatch}\n"
			+ "  [1]{\\textbf{catch} (#1)}\n"
			+ "  {\\textbf{end\\ catch}}"
			+ "\\algblockdefx[FINALLY]{Finally}{EndFinally}\n"
			+ "  {\\textbf{finally}}\n"
			+ "  {\\textbf{end\\ finally}}\n"
			+ "\\algblockdefx[PARALLEL]{Para}{EndPara}\n"
			+ "  {\\textbf{parallel}}\n"
			+ "  {\\textbf{end\\ parallel}}\n"
			+ "\\algblockdefx[THREAD]{Thread}{EndThread}\n"
			+ "  [1]{\\textbf{thread} #1}\n"
			+ "  [1]{\\textbf{end\\ thread} #1}\n"
			+ "\\algblockdefx[DECLARATION]{Decl}{EndDecl}\n"
			+ "  [1][]{\\textbf{#1}}\n"
			+ "  {}\n",
			// algorithmic
			null,
			// algorithm2e (will be accomplished dynamically)
			"\\SetKwInput{Input}{input}\n"
			+ "\\SetKwInput{Output}{output}\n"
			+ "\\SetKwBlock{Parallel}{parallel}{end}\n"
			+ "\\SetKwFor{Thread}{thread}{:}{end}\n"
			+ "\\SetKwBlock{Try}{try}{end}\n"
			+ "\\SetKwFor{Catch}{catch (}{)}{end}\n"
			+ "\\SetKwBlock{Finally}{finally}{end}\n"
			+ "\\SetKwProg{Prog}{Program}{ }{end}\n"
			+ "\\SetKwProg{Proc}{Procedure}{ }{end}\n"
			+ "\\SetKwProg{Func}{Function}{ }{end}\n"
			+ "\\SetKwProg{Incl}{Includable}{ }{end}\n",
			// pseudocode
			null
	};

	/** Index of the chosen algorithmic / pseudocode package */
	private int packageIndex = 0;
	
	/**
	 * @return the index of the preferred LateX package for algorithm export
	 */
	private int optionTargetPackage()
	{
		Object packageName = this.getPluginOption("package", "algorithmicx");
		for (int i = 1; i < PACKAGE_NAMES.length; i++) {
			if (PACKAGE_NAMES[i].equals(packageName)) {
				return i;
			}
		}
		return 0;
	}
	/**
	 * @return the preferred line numbering interval
	 */
	private int optionNumberingInterval()
	{
		return (int)this.getPluginOption("numberingInterval", 0);
	}

	/**
	 * @return {@code true} if ';' is to be appended to every instruction
	 */
	private boolean optionAppendSemicolon()
	{
		return (boolean)this.getPluginOption("appendSemicolon", false);
	}
	
	private void updatePackageCustomizations()
	{
		if (packageIndex == 2 /* "algorithm2e" */) {
			for (String key: new String[] {"preLeave", "preExit", "preThrow"}) {
				String keyword = CodeParser.getKeyword(key);
				if (keyword != null) {
					// We transform the keyword as a string...
					keyword = transform("\"" + keyword + "\"");
					// ... but this requires us to cut off the "\\)\"{}" prefix and "\"{}\\(" suffix
					addCode(String.format("\\SetKw{%s}{%s}", key, keyword.substring(5, keyword.length() - 5)), "", false);
				}
			}
		}
	}
	
	/**
	 * Produces an in-line comment for the current statement if {@code elem}'s
	 * comment comprises exactly one line of comment to be substituted for "%C"
	 * in the statement macro.
	 * @param elem - an {@link Element}
	 * @returns the string to be replaced for %C
	 */
	private String makeInLineComment(Element elem)
	{
		StringList comment = elem.getComment();
		if (comment.count() == 1 && !comment.get(0).trim().isEmpty()) {
			String comment0 = transformText(comment.get(0));
			switch (packageIndex) {
			case 0 /* algorithmicx */:
				return "\\Comment{" + comment0 + "}";
			case 1 /* algorithmic */:
				if (elem instanceof Instruction || elem instanceof Parallel || elem instanceof Try) {
					return "\\COMMENT{" + comment0 + "}";
				}
				else {
					return "[" + comment0 + "]";
				}
			case 3 /* pseudocode */:
				return "\\COMMENT{" + comment0 + "}";				
			default:
				return "";
			}
		}
		return "";
	}

	@Override
	protected void appendComment(String _text, String _indent)
	{
		String[] lines = _text.split("\n");
		String nl = "";
		if (packageIndex == 3 /* pseudocode*/
				&& !code.isEmpty() && code.get(code.count()-1).trim().equals("\\END")) {
			nl = "\\\\ ";
		}
		for (int i = 0; i < lines.length; i++)
		{
			code.add(_indent + nl + commentSymbolLeft() + " "
					+ transformText(lines[i])
					+ " " + commentSymbolRight());
			nl = "";
		}
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getInputReplacer(boolean)
	 */
	@Override
	protected String getInputReplacer(boolean withPrompt)
	{
		switch (packageIndex) {
		case 1 /* algorithmic */:
			return "\\STATE \\textbf{input} \\($1\\)";
		case 2 /* algorith2e */:
			return "\\Input{\\($1\\)}";
		case 3 /* pseudocode */:
			return "\\)input\\($1";
		}
		return "\\)input\\(($1)";
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getOutputReplacer()
	 */
	@Override
	protected String getOutputReplacer()
	{
		switch (packageIndex) {
		case 1 /* algorithmic */:
			return "\\PRINT\\($1\\)";
		case 2 /* algorithm2e */:
			return "\\Output{\\($1\\)}";	// Might also be \\KwOut{\\($1\\)}}
		case 3 /* pseudocode */:
			return "\\OUTPUT{$1}";
		default:
			return "\\)print\\(($1)";
		}
	}

	@Override
	protected boolean breakMatchesCase() {
		return true;
	}

	@Override
	protected String getIncludePattern() {
		return "\\usepackage{%}";
	}

	@Override
	protected OverloadingLevel getOverloadingLevel() {
		// Simply pass the stuff as is...
		return OverloadingLevel.OL_DEFAULT_ARGUMENTS;
	}

	@Override
	protected TryCatchSupportLevel getTryCatchLevel() {
		return TryCatchSupportLevel.TC_NO_TRY;
	}

	/**
	 * Transforms operators and other tokens from the given intermediate
	 * language into tokens of the target language and returns the result
	 * as string.<br/>
	 * This method is called by {@link #transform(String, boolean)} but may
	 * also be used elsewhere for a specific token list.
	 * @see #transform(String, boolean)
	 * @see #transformInput(String)
	 * @see #transformOutput(String)
	 * @see #transformType(String, String)
	 * @see #suppressTransformation
	 * @param _interm - a code line in intermediate syntax
	 * @return transformed string
	 */
	@Override
	protected String transformTokens(StringList tokens)
	{
		tokens.replaceAll("Infinity", "\\infty");
		tokens.replaceAll("{", "\\{");
		tokens.replaceAll("}", "\\}");
		tokens.replaceAll("%", "\\bmod");
		tokens.replaceAll("div", "\\oprdiv");
		tokens.replaceAll("shl", "\\oprshl");
		tokens.replaceAll("shr", "\\oprshr");
		if (packageIndex == 1 /* algorithmic */ || packageIndex == 3 /* pseudocode */) {
			tokens.replaceAll("true", "\\TRUE");
			tokens.replaceAll("false", "\\FALSE");
			tokens.replaceAll("!", " \\NOT");
			tokens.replaceAll("&&", " \\AND");
			tokens.replaceAll("||", " \\OR");
		}
		else {
			tokens.replaceAll("&&", "\\wedge");
			tokens.replaceAll("||", "\\vee");
		}
		tokens.replaceAll("==", "=");
		tokens.replaceAll("!=", "\\neq");
		tokens.replaceAll("<=", "\\leq");
		tokens.replaceAll(">=", "\\geq");
		tokens.replaceAll("\\", "\\backslash{}");
		tokens.replaceAll("~", "\\~{}");
		tokens.replaceAll("&", "\\&");
		if (packageIndex == 1 /* algorithmic */) {
			tokens.replaceAll("^", "\\XOR");
		}
		else {
			tokens.replaceAll("^", "\\^{}");
		}
		String[] keywords = CodeParser.getAllProperties();
		HashSet<String> keys = new HashSet<String>(keywords.length);
		for (String keyword: keywords) {
			keys.add(keyword);
		}
		for (int i = 0; i < tokens.count(); i++) {
			String token = tokens.get(i);
			int len = token.length();
			if (token.equals("<-") || token.equals(":=")) {
				switch (packageIndex) {
				case 0 /* algorithmicx */:
				case 1 /* algorithmic */:
				case 3 /* pseudocode */:
					token = "\\gets";
					break;
				default:
					token = "\\leftarrow{}";
					break;
				}
				if (i+1 < tokens.count() && !tokens.get(i+1).trim().isEmpty()) {
					token += " ";
				}
				tokens.set(i, token);
			}
			// Cut strings out of inline math mode and disarm quotes
			// START KGU#974 2021-05-12: Bugfix #975 we must disarm backslashes as well
			else if (len >= 2
					&& (token.charAt(0) == '"' && token.charAt(len-1) == '"' 
					|| token.charAt(0) == '\'' && token.charAt(len-1) == '\'')) {
				tokens.set(i, "\\)"
					// Replacements within string literals
					+ transformStringContent(token)
					// There may be more symbols to be escaped...
					+ "\\(");
			}
			// END KGU#974 2021-05-12
//			else if (keys.contains(token)) {
//				tokens.set(i, "\\)\\pKey{" + token + "}\\(");
//			}
			else if (token.contains("^")) {
				tokens.set(i, token.replace("^", "\\textasciicircum{}"));
			}
		}
		tokens.removeAll(" ");
		return tokens.concatenate(null);
	}

	@Override
	protected String transform(String _input)
	{
		_input = super.transform(_input, true);
		// Escape underscores and blanks
		_input = _input.replace("_", "\\_");
		_input = _input.replace(" ","\\ ");
		
		// Special German characters (UTF-8 -> LaTeX)
		_input = _input.replace("\u00F6","\"o");
		_input = _input.replace("\u00D6","\"O");
		_input = _input.replace("\u00E4","\"a");
		_input = _input.replace("\u00C4","\"A");
		_input = _input.replace("\u00FC","\"u");
		_input = _input.replace("\u00DC","\"U");
		_input = _input.replace("\u00E9","\"e");
		_input = _input.replace("\u00CB","\"E");
		_input = _input.replace("\u00DF","\\ss{}");

		return _input;
	}
	
	/**
	 * Transforms mere text (or string literal contents) to LaTeX syntax
	 * @param text - the source string
	 * @return the LaTeX-compatible text
	 */
	private String transformStringContent(String text)
	{
		return text.replace("{", "‽{")
		.replace("}", "‽}")
		.replace("\\", "\\textbackslash{}")
		.replace("|", "\\textbar{}")
		.replace("\"", "\"{}")
		.replace("'", "'{}")
		.replace("´", "\\textasciiacute{}")
		.replace("`", "\\textasciigrave{}")
		.replace("^", "\\textasciicircum{}")
		.replace("~", "\\textasciitilde{}")
		.replace("<", "\\textless")
		.replace(">", "\\textgreater")
		.replace("&", "\\&")
		.replace("#", "\\#")
		.replace("°", "\\textdegree")
		.replace("%", "\\%")
		.replace("$", "\\$")
		.replace("→", "\\textrightarrow{}")
		.replace("←", "\\textleftarrow{}")
		.replace("‽{", "\\{")
		.replace("‽}", "\\}");
	}
	
	/**
	 * 
	 * @param _input
	 * @return
	 */
	private String transformText(String _input)
	{
		_input = transformStringContent(_input);
		_input = _input
				// Escape underscores and blanks
				.replace("_", "\\_")
				// Special German characters (UTF-8 -> LaTeX)
				.replace("\u00F6","\"o")
				.replace("\u00D6","\"O")
				.replace("\u00E4","\"a")
				.replace("\u00C4","\"A")
				.replace("\u00FC","\"u")
				.replace("\u00DC","\"U")
				.replace("\u00E9","\"e")
				.replace("\u00CB","\"E")
				.replace("\u00DF","\\ss{}");
		return _input;
	}
	
	/**
	 * Adds a block start or block macro with given indentation to the en
	 * of {@link #code};
	 * @param _indent - requested indentation
	 * @param start - whether the start macro (or the end macro) is requested
	 */
	private void appendBlockMacro(String _indent, boolean start)
	{
		String blockMacro = BLOCK_MACROS[packageIndex][start ? 0 : 1];
		if (blockMacro != null) {
			addCode(blockMacro, _indent, false);
		}
	}
	
	/**
	 * Tests whether the given {@link Element} {@code _el} may be expressed
	 * within one line (needed for the decision of blocking necessity)
	 * @param _el - the element to be scrutinised
	 * @return {@code true} if a single line of code is sufficient to show
	 * the element (or its header if it is a composed one).
	 */
	private boolean isAtomic(Element _el) {
		if (_el instanceof Subqueue) {
			Subqueue sq = (Subqueue)_el;
			if (sq.getSize() == 0) {
				return true;
			}
			else if (sq.getSize() > 1) {
				return false;
			}
			return isAtomic(sq.getElement(0));
		}
		return _el.getUnbrokenText().count() + _el.getComment().count() <= 1;
	}
	
	@Override
	protected void generateCode(Instruction _inst, String _indent)
	{
		if (!_inst.isDisabled(true)) {
			Subqueue sq = (Subqueue)_inst.parent;
			boolean isLastEl = sq == null || sq.getIndexOf(_inst)+1 == sq.getSize();
			if (packageIndex > 1 || _inst.getComment().count() > 1) {
				appendComment(_inst, _indent);
			}
			StringList lines = _inst.getUnbrokenText();
			String semi = optionAppendSemicolon() ? ";" : "";
			for (int i = 0; i < lines.count(); i++)
			{
				String line = lines.get(i);
				String instrCommand = INSTR_MACRO[packageIndex][0];
				if (packageIndex == 0 /* algorithmicx*/ && Instruction.isTypeDefinition(line)) {
					addCode("\\Decl{type:}", _indent, false);
					// get the type name
					StringList tokens = Element.splitLexically(line, true);
					tokens.removeAll(" ");
					String typeName = tokens.get(1);
					addCode(instrCommand
							.replace("%1", typeName + " = " + transform(tokens.concatenate(" ", 3)))
							.replace("%2", semi),
							_indent+this.getIndent(), false);
					addCode("\\EndDecl", _indent, false);
				}
				else if (Jump.isReturn(line) && sq != null && sq.parent instanceof Root
						&& isLastEl) {
					String keyword = CodeParser.getKeyword("preReturn");
					line = line.substring(keyword.length()).trim();
					addCode(JUMP_MACROS[packageIndex][1]
							.replace("%1", transformText(keyword))
							.replace("%2", line)
							.replace("%3", semi),
							_indent, false);
					
				}
				else {
					if (Instruction.isOutput(line) || Instruction.isInput(line)) {
						// We must not use a \STATE prefix in this case!
						instrCommand = INSTR_MACRO[packageIndex][1];
					}
					addCode(instrCommand
							.replace("%1", transform(line))
							.replace("%2", semi),
							_indent, false);
				}
				if (packageIndex <= 1 && i == 0 && _inst.comment.count() == 1) {
					addCode(makeInLineComment(_inst), _indent, false);
				}
			}
		}
	}

	@Override
	protected void generateCode(Alternative _alt, String _indent)
	{
		if (!_alt.isDisabled(true)) {
			if (packageIndex > 1 || _alt.getComment().count() > 1) {
				appendComment(_alt, _indent);
			}
			String indentPlus1 = _indent + this.getIndent();
			StringList condLines = _alt.getCuteText();
			boolean needsBlocking = IF_MACROS[packageIndex][1] == null;
			int ifIndex = 0;
			int elseIndex = 2;
			if (IF_MACROS[packageIndex][elseIndex] == null && _alt.qFalse.getSize() > 0) {
				ifIndex = 4;
				elseIndex = 5;
			}
			boolean atomic = isAtomic(_alt.qTrue);
			addCode(IF_MACROS[packageIndex][ifIndex]
					.replace("%1", transform(condLines.getLongString()))
					.replace("%C", makeInLineComment(_alt)), _indent, false);
			if (needsBlocking && !atomic) {
				appendBlockMacro(_indent, true);
			}
			generateCode(_alt.qTrue, indentPlus1);
			if (needsBlocking && !atomic) {
				appendBlockMacro(_indent, false);
			}
			if (_alt.qFalse.getSize() > 0)
			{
				addCode(IF_MACROS[packageIndex][elseIndex], _indent, false);
				if (needsBlocking && !(atomic = isAtomic(_alt.qFalse))) {
					appendBlockMacro(_indent, true);
				}
				generateCode(_alt.qFalse, indentPlus1);
				if (needsBlocking && !atomic) {
					appendBlockMacro(_indent, false);
				}
			}
			if (!needsBlocking) {
				addCode(IF_MACROS[packageIndex][1], _indent, false);
			}
		}
	}

	@Override
	protected void generateCode(Case _case, String _indent)
	{
		if (!_case.isDisabled(true)) {
			String indentPlus1 = _indent + this.getIndent();
			String indentPlus2 = indentPlus1 + this.getIndent();
			StringList caseText = _case.getUnbrokenText();
			if (packageIndex > 1 || _case.getComment().count() > 1) {
				appendComment(_case, _indent);
			}
			int nBranches = _case.qs.size();
			boolean hasDefaultBranch = !caseText.get(nBranches).trim().equals("%");
			String expr = caseText.get(0);
			if (CASE_MACROS[packageIndex][0] != null) {
				// There is a set of appropriate macros we can use
				addCode(CASE_MACROS[packageIndex][0]
						.replace("%1", transform(expr))
						.replace("%C", makeInLineComment(_case)), _indent, false);
				for(int i = 0; i < nBranches-1; i++)
				{
					Subqueue branch = _case.qs.get(i);
					addCode(CASE_MACROS[packageIndex][2]
							.replace("%1", transform(caseText.get(i+1).trim())), indentPlus1, false);
					generateCode((Subqueue)branch, indentPlus2);
					addCode(CASE_MACROS[packageIndex][3], indentPlus1, false);
				}
				if (hasDefaultBranch)
				{
					addCode(CASE_MACROS[packageIndex][4], indentPlus1, false);
					generateCode((Subqueue) _case.qs.get(nBranches-1), indentPlus2);
					addCode(CASE_MACROS[packageIndex][5], indentPlus1, false);
				}
				addCode(CASE_MACROS[packageIndex][1], _indent, false);
			}
			else {
				// Insert a variable if the discriminator expression is complex
				String var = expr;
				if (!Function.testIdentifier(expr, false, null)) {
					String semi = optionAppendSemicolon() ? ";" : "";
					var = "discr" + Integer.toHexString(_case.hashCode());
					addCode(INSTR_MACRO[packageIndex][0].replace("%1", var + " <- " + expr)
							.replace("%2", semi),
							_indent, false);
				}
				boolean needsBlocking = IF_MACROS[packageIndex][1] == null;
				boolean atomic = false;
				for(int i = 0; i < nBranches-1; i++)
				{
					Subqueue branch = _case.qs.get(i);
					/* Convert a comma-separated selector list into a logical condition
					 * by means of a disjunction of equality checks, e.g. for var = "a"
					 * and selector = "12, 23+5, -9, abs(b)" the resulting condition
					 * will be: "a = 12 or a = 23+5 or a = -9 or a = abs(b)".
					 */
					StringList exprs = Element.splitExpressionList(caseText.get(i+1).trim(), ",");
					String cond = var + " = " + exprs.concatenate(" or " + var + " = ");
					addCode(IF_MACROS[packageIndex][i == 0 ? 0 : 3]
							.replace("%1", transform(cond))
							.replace("%C", makeInLineComment(_case)), _indent, false);
					if (needsBlocking && !(atomic = isAtomic(branch))) {
						appendBlockMacro(_indent, true);
					}
					generateCode((Subqueue)branch, indentPlus1);
					if (needsBlocking && !atomic) {
						appendBlockMacro(_indent, false);
					}
				}
				if (hasDefaultBranch)
				{
					Subqueue branch = _case.qs.get(nBranches-1);
					addCode(IF_MACROS[packageIndex][2], _indent, false);
					if (needsBlocking && !(atomic = isAtomic(branch))) {
						appendBlockMacro(_indent, true);
					}
					generateCode((Subqueue)branch, indentPlus1);
					if (needsBlocking && !atomic) {
						appendBlockMacro(_indent, false);
					}
				}
				if (!needsBlocking) {
					addCode(IF_MACROS[packageIndex][1], _indent, false);
				}
			}
		}
	}

	@Override
	protected void generateCode(For _for, String _indent)
	{
		if (!_for.isDisabled(true)) {
			if (packageIndex > 1 || _for.getComment().count() > 1) {
				appendComment(_for, _indent);
			}
			// Generate header
			int endIndex = 2;
			if (_for.isForInLoop()) {
				endIndex = 4;
				StringList items = this.extractForInListItems(_for);
				String valueList = "";
				if (items == null) {
					valueList = _for.getValueList();
				}
				else {
					valueList = items.concatenate(", ");
					if (items.count() != 1 || !isStringLiteral(items.get(0)) && !Function.testIdentifier(items.get(0), false, null)) {
						valueList = "\\{" + transform(valueList) + "\\}";
					}
					else {
						valueList = "\\ " + transform(valueList);
					}
				}
				addCode(FOR_MACROS[packageIndex][3]
						.replace("%1", _for.getCounterVar())
						.replace("%2", valueList)
						.replace("%C", makeInLineComment(_for)), _indent, false);
			}
			else if (_for.style == For.ForLoopStyle.COUNTER && FOR_MACROS[packageIndex][1] != null) {
				Integer step = null;
				try {
					step = _for.getStepConst();
				}
				catch (NumberFormatException exc) {}
				addCode(FOR_MACROS[packageIndex][1]
						.replace("%1", transform(_for.getCounterVar()))
						.replace("%2", transform(_for.getStartValue()))
						.replace("%3", transform(_for.getEndValue()))
						.replace("%4", transform(_for.getStepString()))
						.replace("%5", step != null && step < 0 ? "\\DOWNTO" : "\\TO")
						.replace("%6", step != null && Math.abs(step) == 1 ? "" : _for.getStepString())
						.replace("%C", makeInLineComment(_for)),
						_indent, false);
			}
			else {
				// FREE_TEXT
				addCode(FOR_MACROS[packageIndex][0]
						.replace("%1", transform(_for.getUnbrokenText().getLongString()))
						.replace("%C", makeInLineComment(_for)), _indent, false);
			}
			// Generate body
			boolean needsBlocking = FOR_MACROS[packageIndex][endIndex] == null;
			boolean atomic = false;
			if (needsBlocking && !(atomic = isAtomic(_for.getBody()))) {
				appendBlockMacro(_indent, true);
			}
			generateCode(_for.getBody(), _indent + this.getIndent());
			if (needsBlocking) {
				if (!atomic) {
					appendBlockMacro(_indent, false);
				}
			}
			else {
				addCode(FOR_MACROS[packageIndex][endIndex], _indent, false);
			}
		}
	}
	
	/** tests whether the given value list item {@code _item} is a string literal */
	private boolean isStringLiteral(String _item) {
		return _item.length() >= 2 && _item.charAt(0) == '"' && _item.charAt(_item.length()-1) == '"';
	}

	@Override
	protected void generateCode(While _while, String _indent)
	{
		if (!_while.isDisabled(true)) {
			if (packageIndex > 1 || _while.getComment().count() > 1) {
				appendComment(_while, _indent);
			}
			addCode(WHILE_MACROS[packageIndex][0]
					.replace("%1", transform(_while.getUnbrokenText().getLongString()))
					.replace("%C", makeInLineComment(_while)), _indent, false);
			boolean needsBlocking = WHILE_MACROS[packageIndex][1] == null;
			boolean atomic = false;
			if (needsBlocking && !(atomic = isAtomic(_while.getBody()))) {
				appendBlockMacro(_indent, true);
			}
			generateCode(_while.getBody(), _indent + this.getIndent());
			if (needsBlocking && !atomic) {
				appendBlockMacro(_indent, false);
			}
			if (!needsBlocking) {
				addCode(WHILE_MACROS[packageIndex][1], _indent, false);
			}
		}
	}
	
	@Override
	protected void generateCode(Repeat _repeat, String _indent)
	{
		if (!_repeat.isDisabled(true)) {
			if (packageIndex > 1 || _repeat.getComment().count() > 1) {
				appendComment(_repeat, _indent);
			}
			String semi = optionAppendSemicolon() ? ";" : "";
			/* Most packages put the condition to the second macro but some put
			 * it to the start macro, so we try to replace it on both
			 */
			String cond = transform(_repeat.getUnbrokenText().getLongString());
			addCode(REPEAT_MACROS[packageIndex][0]
					.replace("%1", cond)
					.replace("%C", makeInLineComment(_repeat)), _indent, false);
			generateCode(_repeat.getBody(), _indent + this.getIndent());
			addCode(REPEAT_MACROS[packageIndex][1]
					.replace("%1", cond).replace("%2", semi), _indent, false);
		}
	}
	
	@Override
	protected void generateCode(Forever _forever, String _indent)
	{
		if (!_forever.isDisabled(true)) {
			if (packageIndex > 1 || _forever.getComment().count() > 1) {
				appendComment(_forever, _indent);
			}
			boolean needsBlocking = LOOP_MACROS[packageIndex][1] == null;
			boolean atomic = false;
			addCode(LOOP_MACROS[packageIndex][0]
					.replace("%C", makeInLineComment(_forever)), _indent, false);
			if (needsBlocking && !(atomic = isAtomic(_forever.getBody()))) {
				appendBlockMacro(_indent, true);
			}
			generateCode(_forever.getBody(), _indent + this.getIndent());
			if (needsBlocking && !atomic) {
				appendBlockMacro(_indent, false);
			}
			if (!needsBlocking) {
				addCode(LOOP_MACROS[packageIndex][1], _indent, false);
			}
		}
	}

	@Override
	protected void generateCode(Call _call, String _indent)
	{
		String callMacro = CALL_MACROS[packageIndex];
		if (callMacro == null) {
			generateCode((Instruction)_call, _indent);
			return;
		}
		String semi = optionAppendSemicolon() ? ";" : "";
		if (!_call.isDisabled(false)) {
			if (packageIndex > 1 || _call.getComment().count() > 1) {
				appendComment(_call, _indent);
			}
			StringList lines = _call.getUnbrokenText();
			String instrMacro = INSTR_MACRO[packageIndex][0];
			for (int i = 0; i < lines.count(); i++)
			{
				String line = lines.get(i);
				StringList tokens = Element.splitLexically(line, true);
				Element.unifyOperators(tokens, true);
				int posAsgnOpr = tokens.indexOf("<-");
				StringList left = null;
				if (posAsgnOpr >=0 ) {
					left = tokens.subSequence(0, posAsgnOpr+1);
					tokens.remove(0, posAsgnOpr+1);
					tokens.trim();
				}
				int posPar1 = tokens.indexOf("(");
				int posPar2 = tokens.lastIndexOf(")");
				if (posPar1 >= 0 && posPar2 > posPar1) {
					String name = tokens.concatenate("", 0, posPar1);
					if (packageIndex == 2 /* algorithm2e */) {
						name = name.replace("_", "");
					}
					line = callMacro.replace("%1", transformText(name))
							.replace("%2", transform(tokens.concatenate("", posPar1+1, posPar2)));
					if (left != null) {
						line = transform(left.concatenate()) + line;
					}
				}
				else {
					line = transform(line);
				}
				addCode(instrMacro.replace("%1", line)
						.replace("%2", semi),
						_indent, false);
				if (packageIndex <= 1 && i == 0 && _call.getComment().count() == 1) {
					addCode(makeInLineComment(_call), _indent, false);
				}
			}
		}
	}
	
	@Override
	protected void generateCode(Jump _jump, String _indent)
	{
		if (!_jump.isDisabled(true)) {
			if (packageIndex > 1 || _jump.getComment().count() > 1) {
				appendComment(_jump, _indent);
			}
			StringList lines = _jump.getUnbrokenText();
			String semi = optionAppendSemicolon() ? ";" : "";
			// START KGU#78 2015-12-19: Enh. #23: We now distinguish exit and return boxes
			//code.add(_indent+"\\assign{\\("+transform(_jump.getText().get(i))+"\\)}");
			if (lines.count() == 0 || lines.getText().trim().isEmpty())
			{
				addCode(JUMP_MACROS[packageIndex][0]
						.replace("%1", transformText(CodeParser.getKeyword("preLeave")))
						.replace("%2", "")
						.replace("%3", semi), _indent, false);
				if (packageIndex <= 1 && _jump.getComment().count() == 1) {
					addCode(makeInLineComment(_jump), _indent, false);
				}
			}
			else
			{
				for(int i = 0; i < lines.count(); i++)
				{
					String line = lines.get(i).trim();
					int index = 0;
					String keyword = CodeParser.getKeyword("preLeave");
					if (Jump.isReturn(line)) {
						index = 1;
						keyword = CodeParser.getKeyword("preReturn");
					}
					else if (Jump.isExit(line)) {
						index = 2;
						keyword = CodeParser.getKeyword("preExit");
					}
					else if (Jump.isThrow(line)) {
						index = 3;
						keyword = CodeParser.getKeyword("preThrow");
					}
					else if (!Jump.isLeave(line)) {
						keyword = "";
					}
					line = line.substring(keyword.length()).trim();
					addCode(JUMP_MACROS[packageIndex][index]
							.replace("%1", transformText(keyword))
							.replace("%2", transform(line, false))
							.replace("%3", semi),
							_indent, false);
					if (packageIndex <= 1 && i == 0 && _jump.getComment().count() == 1) {
						addCode(makeInLineComment(_jump), _indent, false);
					}
					if (!keyword.isEmpty()) {
						break;	// Further instructions are not reachable anyway
					}
				}
			}
		}
	}
	
	@Override
	protected void generateCode(Parallel _para, String _indent)
	{
		// Ignore it if there are no threads or if the element is disabled
		if (!_para.qs.isEmpty() && !_para.isDisabled(true))
		{
			if (packageIndex > 1 || _para.getComment().count() > 1) {
				appendComment(_para, _indent);
			}
			String indentPlus1 = _indent + getIndent();
			String indentPlus2 = indentPlus1 + getIndent();
			// FIXME Indentation may not work properly for the LaTeX result
			addCode(PARALLEL_MACROS[packageIndex][0]
					.replace("%C", makeInLineComment(_para)), _indent, false);
			boolean needsBlocking = PARALLEL_MACROS[packageIndex][3] == null;
			boolean atomic = false;
			for (int q = 0; q < _para.qs.size(); q++)
			{
				String threadNo = Integer.toString(q+1);
				addCode(PARALLEL_MACROS[packageIndex][2]
						.replace("%1", threadNo), indentPlus1, false);
				if (needsBlocking && !(atomic = isAtomic(_para.qs.get(q)))) {
					this.appendBlockMacro(indentPlus1, true);
				}
				generateCode(_para.qs.get(q), indentPlus2);
				if (needsBlocking) {
					if (!atomic) {
						this.appendBlockMacro(indentPlus1, false);
					}
				} else {
					addCode(PARALLEL_MACROS[packageIndex][3]
							.replace("%1", threadNo), indentPlus1, false);
				}
			}
			addCode(PARALLEL_MACROS[packageIndex][1], _indent, false);		
		}
	}
	
	@Override
	protected void generateCode(Try _try, String _indent)
	{
		if (!_try.isDisabled(true)) {
			if (packageIndex > 1 || _try.getComment().count() > 1) {
				appendComment(_try, _indent);
			}
			String text = _try.getUnbrokenText().getLongString();
			String indentPlus1 = _indent + getIndent();
			boolean needsBlocking = TRY_MACROS[packageIndex][1] == null;
			boolean atomic = false;
			addCode(TRY_MACROS[packageIndex][0]
					.replace("%C", makeInLineComment(_try)), _indent, false);
			if (needsBlocking && !(atomic = isAtomic(_try.qTry))) {
				this.appendBlockMacro(_indent, true);
			}
			generateCode(_try.qTry, indentPlus1);
			if (needsBlocking) {
				if (!atomic) {
					this.appendBlockMacro(_indent, false);
				}
			} else {
				addCode(TRY_MACROS[packageIndex][1], _indent, false);
			}
			
			needsBlocking = TRY_MACROS[packageIndex][3] == null;
			addCode(TRY_MACROS[packageIndex][2]
					.replace("%1", text), _indent, false);
			if (needsBlocking && !(atomic = isAtomic(_try.qCatch))) {
				this.appendBlockMacro(_indent, true);
			}
			generateCode(_try.qCatch, indentPlus1);
			if (needsBlocking) {
				if (!atomic) {
					this.appendBlockMacro(_indent, false);
				}
			} else {
				addCode(TRY_MACROS[packageIndex][3], _indent, false);
			}
			
			if (_try.qFinally != null && _try.qFinally.getSize() > 0) {
				needsBlocking = TRY_MACROS[packageIndex][5] == null;
				addCode(TRY_MACROS[packageIndex][4], _indent, false);
				if (needsBlocking && !(atomic = isAtomic(_try.qFinally))) {
					this.appendBlockMacro(_indent, true);
				}
				generateCode(_try.qFinally, indentPlus1);
				if (needsBlocking) {
					if (!atomic) {
						this.appendBlockMacro(_indent, false);
					}
				} else {
					addCode(TRY_MACROS[packageIndex][5], _indent, false);
				}
			}
		}
	}
	
	@Override
	public String generateCode(Root _root, String _indent, boolean _public)
	{
		int line0 = code.count();
		if (codeMap!= null) {
			// register the triple of start line no, end line no, and indentation depth
			// (tab chars count as 1 char for the text positioning!)
			codeMap.put(_root, new int[]{line0, line0, _indent.length()});
		}

		if (topLevel)
		{
			packageIndex = this.optionTargetPackage();
			String packageName = PACKAGE_NAMES[packageIndex];
			addCode("\\documentclass[a4paper,10pt]{article}", "", false);
			addCode("", "", false);
			if (packageIndex <= 1) {
				addCode("\\usepackage{algorithm}", "", false);
			}
			String options = packageIndex == 2 ? "[inoutnumbered]" : "";
			addCode("\\usepackage" + options + "{" + packageName + "}", "", false);
			// FIXME: We might use the packages utc, utf8x
			addCode("\\usepackage{ngerman}", "", false);
			addCode("\\usepackage{amsmath}", "", false);
			// Issue #497 - there might also be usepackage additions
			this.appendUserIncludes("");
			addCode("", "", false);
			addCode("\\DeclareMathOperator{\\oprdiv}{div}", "", false);
			addCode("\\DeclareMathOperator{\\oprshl}{shl}", "", false);
			addCode("\\DeclareMathOperator{\\oprshr}{shr}", "", false);
			String extraDefinitions = PACKAGE_CUSTOMIZATIONS[this.packageIndex];
			if (extraDefinitions != null) {
				for (String line: extraDefinitions.split("\n", -1)) {
					addCode(line, "", false);
				}
			}
			updatePackageCustomizations();
			if (packageIndex == 2 /* algorithm2e */) {
				addCode(optionAppendSemicolon() ? "\\PrintSemicolon" : "\\DontPrintSemicolon", "", false);
			}
			File file = _root.getFile();
			addCode("\\title{Structorizer LaTeX pseudocode Export"
					+ (file != null ? " of " + transformText(file.getName()) : "")
					+ "}", "", false);
			if (this.optionExportLicenseInfo()) {
				addCode("% Structorizer version " + Element.E_VERSION, "", false);
				addCode("\\author{" + transformText(_root.getAuthor()) + "}", "", false);
			} else {
				addCode("\\author{Structorizer " + Element.E_VERSION + "}","", false);
			}
			addCode("\\date{" + DateFormat.getDateInstance().format(new Date()) + "}", "", false);
			addCode("", "", false);
			addCode("\\begin{document}", "", false);
			if (packageIndex == 2 /* algorithm2e */ && this.optionNumberingInterval() > 0) {
				addCode("\\LinesNumbered", "", false);
			}
			subroutineInsertionLine = code.count();
		}
		addCode("", "", false);
		
		// Start the environment
		String argString = _root.getParameterNames().concatenate(", ");
		String resultType = _root.getResultType();
		int commandIndex = 0;	// for main
		if (_root.isSubroutine()) {
			commandIndex = 1;
			if (resultType != null && !"void".equals(resultType) || this.returns || this.isResultSet || this.isFunctionNameSet) {
				commandIndex++;
			}
			if (packageIndex == 1 /* algorithmic */) {
				addCode("\\floatname{algorithm}{" + (commandIndex == 1 ? "Procedure" : "Function") + "}",
						"", false);
			}
		}
		else if (packageIndex == 1 /* algorithmic */) {
			addCode("\\floatname{algorithm}{" + (_root.isProgram() ? "Program" : "Includable") + "}",
					"", false);
		}
		else if (_root.isInclude()) {
			commandIndex = 3;
		}
		commandIndex *= 2;
		switch (packageIndex) {
		case 0 /* algorithmicx */:
		case 1 /* algorithmic */:
		{
			addCode("\\begin{algorithm}", "", false);
			String caption = _root.getSignatureString(false, true);
			if (packageIndex == 1 /* algorithmic */) {
				caption = _root.getMethodName() + "(" + argString +")";
			}
			addCode("\\caption{" + transformText(caption) + "}", "", false);
			addCode("\\begin{algorithmic}[" + this.optionNumberingInterval() + "]", "", false);
			addCode(ROUTINE_MACROS[packageIndex][commandIndex].replace("%1", transform(_root.getMethodName())).replace("%2",argString), "", false);
			this.appendComment(_root, "");
			generateParameterDecl(_root, this.getIndent());
			break;
		}
		case 2 /* algorithm2e */:
		{
			String caption = _root.getMethodName();
			if (_root.isSubroutine()) {
				caption += "(" + transformText(argString) + ")";
			}
			addCode(ENVIRONMENTS2E[commandIndex], "", false);
			addCode("\\caption{" + transformText(caption) + "}", "", false);
			// Declare the names of all called routines
			HashSet<String> routineNames = new HashSet<>();
			for (Call cl: _root.collectCalls()) {
				Function called = cl.getCalledRoutine();
				if (called != null && called.isFunction()) {
					routineNames.add(called.getName());
				}
			}
			for (String name: routineNames) {
				addCode("\\SetKwFunction{Fn" + name.replace("_", "") + "}{"
						+ transformText(name) + "}", "", false);
			}
			this.appendComment(_root, "");
			addCode(ROUTINE_MACROS[packageIndex][commandIndex]
					.replace("%1", transformText(_root.getMethodName()))
					.replace("%2", transformText(argString))
					.replace("%3", resultType == null ? "?" : transformText(resultType)), "", false);
			generateParameterDecl(_root, this.getIndent());
			break;
		}
		case 3 /* pseudocode */:
		{
			addCode("\\begin{pseudocode}{%1}{%2 }"
					.replace("%1", transformText(_root.getMethodName()))
					.replace("%2", transformText(argString)), "", false);
			addCode("\\label{" + transformText(_root.getMethodName()) + "}", "", false);
			this.appendComment(_root, "");
			addCode(ROUTINE_MACROS[packageIndex][commandIndex].replace("%1", transform(_root.getMethodName())).replace("%2",argString), "", false);
			break;
		}
		default:
			appendComment("FIXME: algorithm package \"" + this.getPluginOption("package", "???")
							+ "\" not supported!", "");
		}
		
		// Actual algorithm
		generateCode(_root.children, this.getIndent());
		
		// end the environment
		addCode(ROUTINE_MACROS[packageIndex][commandIndex+1], "", false);
		switch (packageIndex) {
		case 0 /* algorithmicx */:
		case 1 /* algorithmic */:
		{
			if (packageIndex <= 1 /* algorithmicx or algorithmic */) {
				addCode("\\end{algorithmic}", "", false);
			}
			addCode("\\end{algorithm}", "", false);
			break;
		}
		case 2 /* algorithm2e */:
		{
			addCode(ENVIRONMENTS2E[commandIndex+1], "", false);
			break;
		}
		case 3 /* pseudocode */:
		{
			addCode("\\end{pseudocode}", "", false);
			break;
		}
		}
		addCode("", "", false);
		if (topLevel)
		{
			if (this.optionExportSubroutines()) {
				while (!this.includedRoots.isEmpty()) {
					Root incl = this.includedRoots.remove();
					if (incl != _root && (importedLibRoots == null || !importedLibRoots.contains(incl))) {
						this.appendDefinitions(incl, _indent, null, true);
					}
				}
			}
			
			this.libraryInsertionLine = code.count();
			addSepaLine();
			
			addCode("\\end{document}", "", false);
		}
		
		if (codeMap != null) {
			// Update the end line no relative to the start line no
			codeMap.get(_root)[1] += (code.count() - line0);
		}

		return code.getText();
	}

	/**
	 * Creates a dummy element declaring all arguments in case of a function diagram
	 * @param _root - the diagram
	 * @param _indent - the current indentation
	 */
	private void generateParameterDecl(Root _root, String _indent) {
		boolean hasIncludes = _root.includeList != null && _root.includeList.count() > 0;
		if (_root.isSubroutine() || hasIncludes) {
			String indent2 = _indent + this.getIndent();
			String resType = _root.getResultType();
			ArrayList<Param> params = _root.getParams();
			if (!params.isEmpty() || resType != null || hasIncludes) {
				switch (packageIndex) {
				case 0 /* algorithmicx */:
					if (!params.isEmpty()) {
						addCode("\\Decl{Parameters:}", _indent, false);
						for (Param param: params) {
							String type = param.getType(true);
							addCode("\\State " + transform(param.getName()) +
									": " + (type == null ? "?" : transform(param.getType(true))), indent2, false);
						}
						addCode("\\EndDecl", _indent, false);
					}
					if (resType != null) {
						addCode("\\Decl{Result type:}", _indent, false);
						addCode("\\State " + resType, indent2, false);
						addCode("\\EndDecl", _indent, false);
					}
					if (hasIncludes) {
						addCode("\\Decl{Includes:}", _indent, false);
						addCode("\\State " + _root.includeList.concatenate(", "), indent2, false);
						addCode("\\EndDecl", _indent, false);
					}
					break;
				case 2 /* algorithm2e */:
					for (Param param: params) {
						// This is somewhat diverted (as the documentation suggests its use for commenting)
						String type = param.getType(true);
						addCode("\\KwData{\\(" + transform(param.getName(), false) +
								"\\): " + (type == null ? "?" : transformText(param.getType(true))) + "}", "", false);
					}
					if (resType != null) {
						// This is somewhat diverted (as the documentation suggests its use for describing)
						addCode("\\KwResult{" + transformText(resType) + "}", "", false);
					}
					break;
				}	
			}
		}
	}
	// END KGU#483 2017-12-30

	// START KGU#483 2018-01-02: Enh. #389 + issue #497
	@Override
	protected void appendDefinitions(Root _root, String _indent, StringList _varNames, boolean _force) {
		// Just generate the entire diagram...
		boolean wasTopLevel = topLevel;
		try {
			topLevel = false;
			generateCode(_root, _indent, false);
		}
		finally {
			topLevel = wasTopLevel;
		}
	}
	// END KGU#483 2018-01-02

	// START KGU#815 2020-04-03: Enh. #828
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generatesClass()
	 */
	@Override
	protected boolean allowsMixedModule()
	{
		return true;
	}
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#max1MainPerModule()
	 */
	@Override
	protected boolean max1MainPerModule()
	{
		return false;
	}
	// END KGU#815 2020-04-03
}

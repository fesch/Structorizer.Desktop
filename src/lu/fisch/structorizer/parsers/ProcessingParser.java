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

package lu.fisch.structorizer.parsers;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Parser class for the "Processing" language (http://processing.org)
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2021-02-27      First Issue (on behalf of enhancement request #932)
 *      Kay Gürtzig     2021-03-04      Issue #957 file preparation now cares for import declarations
 *      Kay Gürtzig     2021-03-05      Bugfix #959: Support for Processing conversion functions,
 *                                      Issue #960: Processing system variables automatically initialised,
 *                                      way more standard constants defined (see comment);
 *                                      bugfix #961: The conversion of output instructions had not worked
 *      Kay Gürtzig     2021-03-08      Issue #964 The central draw() loop shall not be inserted if draw() was
 *                                      not defined.
 *      Kay Gürtzig     2021-05-12      Issue #932: Processing standard definitions separated from main
 *      Kay Gürtzig     2021-05-12      Bugfix #974: A severe and an annoying typo in the ProcessingStandardDefinitions
 *      Kay Gürtzig     2023-11-08      Bugfix #1110 method translateContent() returned the argument instead of the result
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      Basically uses the JavaParser with some specific modifications, does not try to make sense
 *      of application-specific functions like loop(), noLoop(), push(), pop() etc.
 *      
 *      2021-03-05 Kay Gürtzig (issue #960)
 *      - constant definitions taken from:
 *        https://github.com/processing/processing/blob/master/core/src/processing/core/PConstants.java
 *
 ******************************************************************************************************///

import java.io.File;

import com.creativewidgetworks.goldparser.engine.Reduction;

import lu.fisch.structorizer.elements.Call;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.Forever;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.elements.Jump;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.utils.StringList;

/**
 * JavaParser offspring to import "Processing" code (see http://processing.org"), which is basically
 * Java but without outer class as CompilationUnit root and with a lot of drawing-specific functions
 * Introduced on behalf of enhancement request #932 (Karl-Heinz Becker)
 * @author Kay Gürtzig
 * @version 3.31
 */
public class ProcessingParser extends JavaParser {

	//------------------------------ Constructor -----------------------------
	/**
	 * Constructs a parser for language "Processing", loads the grammar as resource and
	 * specifies whether to generate the parse tree as string
	 */
	public ProcessingParser() {
	}

	//---------------------- File Filter configuration ---------------------------
	
	@Override
	public String getDialogTitle() {
		return "Processing";
	}

	@Override
	protected String getFileDescription() {
		return "'Processing' Source Files";
	}

	@Override
	public String[] getFileExtensions() {
		final String[] exts = { "pde" };
		return exts;
	}

	//----------------------------- Preprocessor -----------------------------

	/**
	 * The file name to be used as program name
	 */
	private String progName = null;
	
	@Override
	protected void doExtraPreparations(StringBuilder _srcCode, File _file) throws ParserCancelled
	{
		progName = _file.getName();
		if (progName.contains(".")) {
			progName = progName.substring(0, progName.indexOf('.'));
		}
		progName = Root.getMethodName(progName, Root.DiagramType.DT_MAIN, true);
		// START KGU#954 2021-03-04: Issue #957 Skip import lines before inserting the definition
		//_srcCode.insert(0, "public class " + progName + "Processing {\n");
		int posIns = 0;
		if (_srcCode.indexOf("import") >= 0) {
			int posNextLine = -1;
			int posLine = posNextLine + 1;
			boolean inComment = false;
			boolean inImportClause = false;
			int length = _srcCode.length();
			boolean isNoise = true;
			while (isNoise && posLine < length && (posNextLine = _srcCode.indexOf("\n", posLine)) >= 0) {
				char[] lineChars = new char[posNextLine - posLine];
				_srcCode.getChars(posLine, posNextLine, lineChars, 0);
				String line = new String(lineChars);
				for (int i = 0; i < lineChars.length; i++) {
					char ch = lineChars[i];
					if (inComment) {
						if (ch == '*' && i+1 < lineChars.length && lineChars[i+1] == '/') {
							inComment = false;
							i++;
						}
					}
					else if (inImportClause && ch == ';') {
						/* The syntactic consistency of the import clause is not our business
						 * here (we'll leave this to the parser), we just grope for the ending
						 * semicolon
						 */
						inImportClause = false;
					}
					else if (ch == '/' && i+1 < lineChars.length) {
						if (lineChars[i+1] == '/') {
							// Line comment - skip to next line
							break;
						}
						else if (lineChars[i+1] == '*') {
							// Block comment starts here
							inComment = true;
							i++;
						}
					}
					else if (!inImportClause && !Character.isWhitespace(ch)) {
						if (!line.substring(i).startsWith("import") || i + 6 >= lineChars.length
								|| !Character.isWhitespace(lineChars[i + 6])) {
							// Okay this is something else ...
							if (i > 0) {
								// insert a newline here if necessary ...
								posLine += i;
								_srcCode.insert(posLine++, "\n");
							}
							isNoise = false;
							posIns = posLine;
							// ... and leave the loop
							break;
						}
						// May we dare to skip the line? No, it might still open a comment block
						inImportClause = true;
						i += 6;
					}
				} // for (int i = 0; i < lineChars.length; i++)
				posLine = posNextLine + 1;
				this.checkCancelled();
			} // while (isNoise && ...)
		}
		_srcCode.insert(posIns, "public class " + progName + "Processing {\n");
		// END KGU#954 2021-03-04
		_srcCode.append("}\n");
	}

	//---------------------- Build methods for structograms ---------------------------
	
	// START KGU#962 2021-03-08: Issue #964 Detect the definition of the central draw() method
	private boolean hasDrawMethod = false;
	
	protected void addRoot(Root newRoot)
	{
		if (newRoot.isSubroutine() && newRoot.getQualifiedName().equals("draw")) {
			hasDrawMethod = true;
		}
		super.addRoot(newRoot);
	}
	// END KGU#962 2021-03-08

	@Override
	protected boolean qualifyTopLevelMethods()
	{
		return false;
	}
	
	/* (non-Javadoc)
	 * @see CodeParser#initializeBuildNSD()
	 */
	@Override
	protected void initializeBuildNSD() throws ParserCancelled
	{
		super.initializeBuildNSD();
		Root mainRoot = new Root();
		mainRoot.setText(progName);
		mainRoot.children.addElement(new Call("setup()"));
		// START KGU#962 2021-03-08: Issue #964 Now done afterwards when we know that a draw method exists
		//Forever mainLoop = new Forever();
		//mainLoop.getBody().addElement(new Call("draw()"));
		//mainRoot.children.addElement(mainLoop);
		// END KGU#962 2021-03-08
		mainRoot.addToIncludeList(progName + "Processing");
		addRoot(mainRoot);
	}
	
	// START KGU#957 2021-03-05: Issue #959 - Processing conversion function handling
	/**
	 * Decomposes a conversion function, i.e. the expression to be converted.
	 * @param exprRed - a {@code <ProcessingTypeConversion>} reduction
	 * @return a StringList containing the necessary sequence of Structorizer instructions
	 * and expressions to achieve the same effect.
	 * @throws ParserCancelled if the user aborted the import process
	 */
	protected StringList decomposeProcessingTypeConversion(Reduction exprRed) throws ParserCancelled
	{
		// <ProcessingTypeConversion> ::= binary '(' <Expression> ')'
		// <ProcessingTypeConversion> ::= hex '(' <Expression> ')'
		// <ProcessingTypeConversion> ::= unbinary '(' <Expression> ')'
		// <ProcessingTypeConversion> ::= unhex '(' <Expression> ')'
		// <ProcessingTypeConversion> ::= int '(' <Expression> ')'
		// <ProcessingTypeConversion> ::= byte '(' <Expression> ')'
		// <ProcessingTypeConversion> ::= char '(' <Expression> ')'
		// <ProcessingTypeConversion> ::= str '(' <Expression> ')'
		// <ProcessingTypeConversion> ::= float '(' <Expression> ')'
		// <ProcessingTypeConversion> ::= boolean '(' <Expression> ')'
		StringList exprs = this.decomposeExpression(exprRed.get(2), false, false);
		int ixLast = exprs.count() - 1;
		/* As a first approach we just put the original name and don't try some smart
		 * reinterpretation: A simple casting will not usually do, and Structorizer
		 * does not even devour a casting expression, anyway...
		 */
		exprs.set(ixLast, this.getContent_R(exprRed.get(0)) + "(" + exprs.get(ixLast) + ")");
		return exprs;
	}
	// END KGU#957 2021-03-05

	// START KGU#959 2021-03-06: Issue #961 extracted from decomposeExpression() for overloading
	/**
	 * Checks whether the passed-in instruction line (which must adhere to a
	 * method invocation with or without assigned result, where the assignment
	 * symbol if contained is expected to be "<-") represents some built-in
	 * function or command, e.g. an output instruction, and if so converts it
	 * accordingly. If it is notzhing specific then just returns {@code null}.
	 * @param line - an built instruction line with call syntax (qualified names
	 * possible)
	 * @return a representing {@link Element} or {@code null}
	 */
	protected Instruction convertInvocation(String line)
	{
		Instruction ele = null;
		if (line.startsWith("exit(")) {
			ele = new Jump(getKeyword("preExit") + " "
					+ line.substring("exit(".length(), line.length()-1));
		}
		else if (line.startsWith("println(")) {
			ele = new Instruction(getKeyword("output") + " "
					+ line.substring("println(".length(), line.length()-1));
		}
		else if (line.startsWith("print(")) {
			ele = new Instruction(getKeyword("output") + " "
					+ line.substring("print(".length(), line.length()-1));
		}		
		return ele;
	}
	// END KGU#959 2021-03-05

	@Override
	protected String translateContent(String _content)
	{
		String output = getKeyword("output");
		String[] outputTokens = new String[] {"println", "printArray", "print"};
		// An input conversion is not feasible.
		//String input = getKeyword("input");
		
		StringList tokens = Element.splitLexically(_content, true);
		for (int i = 0; i < outputTokens.length; i++) {
			tokens.replaceAll(outputTokens[i], output);
		}

		// Rather not necessary, but who knows...
		tokens.removeAll(StringList.explode("Math,.", ","), true);

		// START KGU#1098 2023-11-08: Bugfix #1110 Methodwas ineffective
		//return _content.trim();
		return tokens.concatenate(null).trim();
		// END KGU#1098 2023-11-08
	}
	

	//------------------------- Postprocessor ---------------------------

	private boolean stdDefinitionsIncluded = false;
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.parsers.CodeParser#subclassUpdateRoot(lu.fisch.structorizer.elements.Root, java.lang.String)
	 */
	@Override
	protected boolean subclassUpdateRoot(Root root, String sourceFileName) throws ParserCancelled
	{
		// START KGU#1134 2024-03-18: Bugfix #1145 Defective handling of multiple classes at top level
		super.subclassUpdateRoot(root, sourceFileName);
		// END KGU#1134 2024-03-18
		if (root.isInclude() && root.getMethodName().equals(progName + "Processing")) {
			// START KGU#970 2021-05-12: Issue #932 Content moved to subclassPostProcess 
			// Define some important Processing constants
//			final String mathConstants = 
//					"const PI <- " + Float.toString((float)Math.PI) + "\n" +
//					"const HALF_PI <- " + Float.toString((float)Math.PI/2) + "\n" +
//					"const QUARTER_PI <- " + Float.toString((float)Math.PI/4) + "\n" +
//					"const TWO_PI <- " + Float.toString((float)Math.PI*2) + "\n" +
//					"const TAU <- TWO_PI\n" +
//					"const DEG_TO_RAD <- PI/180.0\n" +
//					"const RAD_TO_DEG <- 1/DEG_TO_RAD\n";
//			// START KGU#958 2021-03-05: Issue #960
//			//Instruction defs = new Instruction("type ColorMode = enum{RGB, HSB}");
//			final String keyConstants =
//					"const BACKSPACE <- char(8)\n" +
//					"const TAB <- char(9)\n" +
//					"const ENTER <- char(10)\n" +
//					"const RETURN <- char(13)\n" +
//					"const ESC <- char(27)\n" +
//					"const DELETE <- char(127)";
//			final String strokeConstants =
//					"const SQUARE <- 1 << 0\n" +
//					"const ROUND <- 1 << 1\n" +
//					"const PROJECT <- 1 << 2\n" +
//					"const MITER <- 1 << 3\n" +
//					"const BEVEL <- 1 << 4\n";
//			final String blendModeConstants = 
//					"const REPLACE <- 0\n" +
//					"const BLEND <- 1 << 0\n" +
//					"const ADD <- 1 << 1\n" +
//					"const SUBTRACT <- 1 << 2\n" +
//					"const LIGHTEST <- 1 << 3\n" +
//					"const DARKEST <- 1 << 4\n" +
//					"const DIFFERENCE <- 1 << 5\n" +
//					"const EXCLUSION <- 1 << 6\n" +
//					"const MULTIPLY <- 1 << 7\n" +
//					"const SCREEN <- 1 << 8\n" +
//					"const OVERLAY <- 1 << 9\n" +
//					"const HARD_LIGHT <- 1 << 10\n" +
//					"const SOFT_LIGHT <- 1 << 11\n" +
//					"const DODGE <- 1 << 12\n" +
//					"const BURN <- 1 << 13\n";
//			final String rendererConstants =
//					"const JAVA2D <- \"processing.awt.PGraphicsJava2D\"\n"
//					+ "const P2D <- \"processing.awt.PGraphics2D\"\n"
//					+ "const P3D <- \"processing.awt.PGraphics3D\"\n"
//					+ "const FX2D <- \"processing.awt.PGraphicsFX2D\"\n"
//					+ "const PDF <- \"processing.awt.PGraphicsPDF\"\n"
//					+ "const SVG <- \"processing.awt.PGraphicsSVG\"\n"
//					+ "const DXF <- \"processing.awt.RawDXF\"";
//			final String shapeEnumerator =
//					"type Shapes = enum{\\\n"
//					+ "GROUP,\\\n"
//					+ "POINT = 2, POINTS, LINE, LINES,\\\n"
//					+ "TRIANGLE = 8, TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN,\\\n"
//					+ "QUAD = 16, QUADS, QUAD_STRIP,\\\n"
//					+ "POLYGON = 20, PATH,\\\n"
//					+ "RECT = 30, ELLIPSE, ARC,\\\n"
//					+ "SPHERE = 40, BOX,"
//					+ "LINE_STRIP = 50, LINE_LOOP\\\n"
//					+ "}";
//			final String systemVariables1 = 
//					"var width: int <- 100\n" +
//					"var height: int <- 100\n" +
//					"var pixelWidth: int <- width\n" +
//					"var pixelHeight: int <- height\n" +
//					"var frameCount: int <- 0\n" +
//					"var frameRate: int <- 60";
//			final String systemVariables2 = 
//					"var key: char <- '\0'\n" +
//					"var keyPressed: boolean <- false\n" +
//					"var keyCode: KeyCode <- NONE\n";
//			Instruction defs = new Instruction(systemVariables2);
//			defs.setColor(colorGlobal);
//			defs.setComment("Processing system variables initialization, part 2");
//			root.children.insertElementAt(defs, 0);
//			defs = new Instruction(systemVariables1);
//			defs.setColor(colorGlobal);
//			defs.setComment("Processing system variables initialization, part 1");
//			root.children.insertElementAt(defs, 0);
//			defs = new Instruction("type KeyCodes = enum{NONE, SHIFT = 16, CONTROL, ALT, LEFT = 37, RIGHT = 39, DOWN = 40, UP = 224}");
//			defs.setColor(colorConst);
//			defs.setComment("Processing standard key code enumerator");
//			root.children.insertElementAt(defs, 0);
//			defs = new Instruction("type FileTypes = enum{TIFF, TARGA, JPEG, GIF}");
//			defs.setColor(colorConst);
//			defs.setComment("Processing standard file type enumerator");
//			root.children.insertElementAt(defs, 0);
//			defs = new Instruction("type Lighting = enum{AMBIENT, DIRECTIONAL, SPOT}");
//			defs.setColor(colorConst);
//			defs.setComment("Processing standard lighting enumerator");
//			root.children.insertElementAt(defs, 0);
//			defs = new Instruction("type VertAlignmentModes = enum{BASELINE, TOP, BOTTOM}");
//			defs.setColor(colorConst);
//			defs.setComment("Processing standard vertical alignment mode enumerator");
//			root.children.insertElementAt(defs, 0);
//			defs = new Instruction("type ArcModes = enum{CHORD = 2, PIE}");
//			defs.setColor(colorConst);
//			defs.setComment("Processing standard arc drawing mode enumerator");
//			root.children.insertElementAt(defs, 0);
//			defs = new Instruction("type ShapeModes = enum{CORNER, CORNERS, RADIUS, CENTER, DIAMETER = CENTER}");
//			defs.setColor(colorConst);
//			defs.setComment("Processing standard shape drawing mode enumerator");
//			root.children.insertElementAt(defs, 0);
//			defs = new Instruction(shapeEnumerator);
//			defs.setColor(colorConst);
//			defs.setComment("Processing standard shape type enumerator");
//			root.children.insertElementAt(defs, 0);
//			defs = new Instruction("type ColorMode = enum{RGB, ARGB, HSB, ALPHA}");
//			// END KGU#958 2021-03-05
//			defs.setColor(colorConst);
//			defs.setComment("Processing standard color mode enumerator");
//			root.children.insertElementAt(defs, 0);
//			// START KGU#958 2021-03-05: Issue #960
//			defs = new Instruction(rendererConstants);
//			defs.setColor(colorConst);
//			defs.setComment("Processing standard renderer constants");
//			root.children.insertElementAt(defs, 0);
//			defs = new Instruction(blendModeConstants);
//			defs.setColor(colorConst);
//			defs.setComment("Processing standard blend mode constants");
//			root.children.insertElementAt(defs, 0);
//			defs = new Instruction(strokeConstants);
//			defs.setColor(colorConst);
//			defs.setComment("Processing standard stroke constants");
//			root.children.insertElementAt(defs, 0);
//			defs = new Instruction(keyConstants);
//			defs.setColor(colorConst);
//			defs.setComment("Processing standard key constants");
//			root.children.insertElementAt(defs, 0);
//			// END KGU#958 2021-03-05
//			defs = new Instruction(mathConstants);
//			defs.setColor(colorConst);
//			defs.setComment("Processing standard Math constants");
//			root.children.insertElementAt(defs, 0);
			root.addToIncludeList("ProcessingStandardDefinitions");
			stdDefinitionsIncluded = true;
			// END KGU#970 2021-05-12
			// This must not be invoked here because setup() includes this diagram (#947)
			//root.children.addElement(new Call("setup()"));
		}
		// START KGU#962 2021-03-08: Issue #964 Add draw loop if a draw method exists
		else if (root.isProgram() && root.getMethodName().equals(progName) && this.hasDrawMethod) {
			Forever mainLoop = new Forever();
			mainLoop.getBody().addElement(new Call("draw()"));
			root.children.addElement(mainLoop);
		}
		// END KGU#962 2021-03-08
		return false;
	}

	// START KGU#970 2021-05-12: Standard definitions now as isolated Includable
	@Override
	protected void subclassPostProcess(String textToParse) throws ParserCancelled
	{
		// Define some important Processing constants
		final String mathConstants = 
				"const PI <- " + Float.toString((float)Math.PI) + "\n" +
				"const HALF_PI <- " + Float.toString((float)Math.PI/2) + "\n" +
				"const QUARTER_PI <- " + Float.toString((float)Math.PI/4) + "\n" +
				"const TWO_PI <- " + Float.toString((float)Math.PI*2) + "\n" +
				"const TAU <- TWO_PI\n" +
				"const DEG_TO_RAD <- PI/180.0\n" +
				"const RAD_TO_DEG <- 1/DEG_TO_RAD";
		// START KGU#958 2021-03-05: Issue #960
		//Instruction defs = new Instruction("type ColorMode = enum{RGB, HSB}");
		final String keyConstants =
				// START KGU#973 2021-05-12: Bugfix #974 - char(...) replaced by chr(...)
				"const BACKSPACE <- chr(8)\n" +
				"const TAB <- chr(9)\n" +
				"const ENTER <- chr(10)\n" +
				"const RETURN <- chr(13)\n" +
				"const ESC <- chr(27)\n" +
				"const DELETE <- chr(127)";
				// END KGU#973 2021-05-12
		final String strokeConstants =
				"const SQUARE <- 1 << 0\n" +
				"const ROUND <- 1 << 1\n" +
				"const PROJECT <- 1 << 2\n" +
				"const MITER <- 1 << 3\n" +
				"const BEVEL <- 1 << 4";
		final String blendModeConstants = 
				"const REPLACE <- 0\n" +
				"const BLEND <- 1 << 0\n" +
				"const ADD <- 1 << 1\n" +
				"const SUBTRACT <- 1 << 2\n" +
				"const LIGHTEST <- 1 << 3\n" +
				"const DARKEST <- 1 << 4\n" +
				"const DIFFERENCE <- 1 << 5\n" +
				"const EXCLUSION <- 1 << 6\n" +
				"const MULTIPLY <- 1 << 7\n" +
				"const SCREEN <- 1 << 8\n" +
				"const OVERLAY <- 1 << 9\n" +
				"const HARD_LIGHT <- 1 << 10\n" +
				"const SOFT_LIGHT <- 1 << 11\n" +
				"const DODGE <- 1 << 12\n" +
				"const BURN <- 1 << 13";
		final String rendererConstants =
				"const JAVA2D <- \"processing.awt.PGraphicsJava2D\"\n"
				+ "const P2D <- \"processing.awt.PGraphics2D\"\n"
				+ "const P3D <- \"processing.awt.PGraphics3D\"\n"
				+ "const FX2D <- \"processing.awt.PGraphicsFX2D\"\n"
				+ "const PDF <- \"processing.awt.PGraphicsPDF\"\n"
				+ "const SVG <- \"processing.awt.PGraphicsSVG\"\n"
				+ "const DXF <- \"processing.awt.RawDXF\"";
		final String shapeEnumerator =
				"type Shapes = enum{\\\n"
				+ "GROUP,\\\n"
				+ "POINT = 2, POINTS, LINE, LINES,\\\n"
				+ "TRIANGLE = 8, TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN,\\\n"
				+ "QUAD = 16, QUADS, QUAD_STRIP,\\\n"
				+ "POLYGON = 20, PATH,\\\n"
				+ "RECT = 30, ELLIPSE, ARC,\\\n"
				+ "SPHERE = 40, BOX,"
				+ "LINE_STRIP = 50, LINE_LOOP\\\n"
				+ "}";
		final String systemVariables1 = 
				"var width: int <- 100\n" +
				"var height: int <- 100\n" +
				"var pixelWidth: int <- width\n" +
				"var pixelHeight: int <- height\n" +
				"var frameCount: int <- 0\n" +
				"var frameRate: int <- 60";
		final String systemVariables2 = 
				// START KGU#973 2021-05-12: Bugfix #974
				//"var key: char <- '\0'\n" +
				"var key: char <- chr(0)\n" +
				// END KGU#973 2021-05-12
				"var keyPressed: boolean <- false\n" +
				"var keyCode: KeyCode <- NONE";
		
		if (stdDefinitionsIncluded) {
			Root stdDefs = new Root();
			stdDefs.setText("ProcessingStandardDefinitions");
			stdDefs.setInclude();
			Instruction defs = new Instruction(systemVariables2);
			defs.setColor(COLOR_GLOBAL);
			defs.setComment("Processing system variables initialization, part 2");
			stdDefs.children.insertElementAt(defs, 0);
			defs = new Instruction(systemVariables1);
			defs.setColor(COLOR_GLOBAL);
			defs.setComment("Processing system variables initialization, part 1");
			stdDefs.children.insertElementAt(defs, 0);
			defs = new Instruction("type KeyCodes = enum{NONE, SHIFT = 16, CONTROL, ALT, LEFT = 37, RIGHT = 39, DOWN = 40, UP = 224}");
			defs.setColor(COLOR_CONST);
			defs.setComment("Processing standard key code enumerator");
			stdDefs.children.insertElementAt(defs, 0);
			defs = new Instruction("type FileTypes = enum{TIFF, TARGA, JPEG, GIF}");
			defs.setColor(COLOR_CONST);
			defs.setComment("Processing standard file type enumerator");
			stdDefs.children.insertElementAt(defs, 0);
			defs = new Instruction("type Lighting = enum{AMBIENT, DIRECTIONAL, SPOT}");
			defs.setColor(COLOR_CONST);
			defs.setComment("Processing standard lighting enumerator");
			stdDefs.children.insertElementAt(defs, 0);
			defs = new Instruction("type VertAlignmentModes = enum{BASELINE, TOP, BOTTOM}");
			defs.setColor(COLOR_CONST);
			defs.setComment("Processing standard vertical alignment mode enumerator");
			stdDefs.children.insertElementAt(defs, 0);
			defs = new Instruction("type ArcModes = enum{CHORD = 2, PIE}");
			defs.setColor(COLOR_CONST);
			defs.setComment("Processing standard arc drawing mode enumerator");
			stdDefs.children.insertElementAt(defs, 0);
			defs = new Instruction("type ShapeModes = enum{CORNER, CORNERS, RADIUS, CENTER, DIAMETER = CENTER}");
			defs.setColor(COLOR_CONST);
			defs.setComment("Processing standard shape drawing mode enumerator");
			stdDefs.children.insertElementAt(defs, 0);
			defs = new Instruction(shapeEnumerator);
			defs.setColor(COLOR_CONST);
			defs.setComment("Processing standard shape type enumerator");
			stdDefs.children.insertElementAt(defs, 0);
			defs = new Instruction("type ColorMode = enum{RGB, ARGB, HSB, ALPHA}");
			// END KGU#958 2021-03-05
			defs.setColor(COLOR_CONST);
			defs.setComment("Processing standard color mode enumerator");
			stdDefs.children.insertElementAt(defs, 0);
			// START KGU#958 2021-03-05: Issue #960
			defs = new Instruction(rendererConstants);
			defs.setColor(COLOR_CONST);
			defs.setComment("Processing standard renderer constants");
			stdDefs.children.insertElementAt(defs, 0);
			defs = new Instruction(blendModeConstants);
			defs.setColor(COLOR_CONST);
			defs.setComment("Processing standard blend mode constants");
			stdDefs.children.insertElementAt(defs, 0);
			defs = new Instruction(strokeConstants);
			defs.setColor(COLOR_CONST);
			defs.setComment("Processing standard stroke constants");
			stdDefs.children.insertElementAt(defs, 0);
			defs = new Instruction(keyConstants);
			defs.setColor(COLOR_CONST);
			defs.setComment("Processing standard key constants");
			stdDefs.children.insertElementAt(defs, 0);
			// END KGU#958 2021-03-05
			defs = new Instruction(mathConstants);
			defs.setColor(COLOR_CONST);
			defs.setComment("Processing standard Math constants");
			stdDefs.children.insertElementAt(defs, 0);
			this.addRoot(stdDefs);
		}
	}
	// END KGU#970 2021-05-12
	
}

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
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      Basically uses the JavaParser with some specific modifications, does not try to make sense
 *      of application-specific functions like loop(), noLoop(), push(), pop() etc.
 *
 ******************************************************************************************************///

import java.io.File;

import lu.fisch.structorizer.elements.Call;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.Forever;
import lu.fisch.structorizer.elements.Instruction;
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
	protected void doExtraPreparations(StringBuilder _srcCode, File _file) {
		progName = _file.getName();
		if (progName.contains(".")) {
			progName = progName.substring(0, progName.indexOf('.'));
		}
		progName = Root.getMethodName(progName, Root.DiagramType.DT_MAIN, true);
		_srcCode.insert(0, "public class " + progName + "Processing {\n");
		_srcCode.append("}\n");
	}

	//---------------------- Build methods for structograms ---------------------------

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
		Forever mainLoop = new Forever();
		mainLoop.getBody().addElement(new Call("draw()"));
		mainRoot.children.addElement(mainLoop);
		mainRoot.addToIncludeList(progName + "Processing");
		addRoot(mainRoot);
	}
	
	/**
	 * Helper method to retrieve and compose the text of the given reduction, combine it with previously
	 * assembled string _content and adapt it to syntactical conventions of Structorizer. Finally return
	 * the text phrase.
	 * @param _content - A string already assembled, may be used as prefix, ignored or combined in another
	 * way 
	 * @return composed and translated text.
	 */
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
		tokens.removeAll(StringList.explodeWithDelimiter("Math.", "."), true);

		return _content.trim();
	}
	

	//------------------------- Postprocessor ---------------------------

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.parsers.CodeParser#subclassUpdateRoot(lu.fisch.structorizer.elements.Root, java.lang.String)
	 */
	@Override
	protected boolean subclassUpdateRoot(Root root, String sourceFileName) throws ParserCancelled
	{
		if (root.isInclude() && root.getMethodName().equals(progName + "Processing")) {
			// Define some important Processing constants
			final String mathConstants = 
					"const PI <- " + Double.toString(Math.PI) + "\n" +
					"const HALF_PI <- " + Double.toString(Math.PI/2) + "\n" +
					"const QUARTER_PI <- " + Double.toString(Math.PI/4) + "\n" +
					"const TWO_PI <- " + Double.toString(Math.PI*2) + "\n" +
					"const TAU <- TWO_PI";
			Instruction defs = new Instruction("type ColorMode = enum{RGB, HSB}");
			defs.setColor(colorConst);
			defs.setComment("Processing standard enumerator");
			root.children.insertElementAt(defs, 0);
			defs = new Instruction(mathConstants);
			defs.setColor(colorConst);
			defs.setComment("Processing standard Math constants");
			root.children.insertElementAt(defs, 0);
			// This must not be in invoked here because setup() includes diagram (#947)
			//root.children.addElement(new Call("setup()"));
		}
		return false;
	}

}

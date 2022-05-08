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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.filechooser.FileFilter;

import org.xml.sax.SAXException;

import lu.fisch.structorizer.elements.Alternative;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.ILoop;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.elements.Repeat;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.structorizer.elements.While;
import lu.fisch.utils.StringList;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Class SbdImporter for importing sbide files as diagrams
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2022-05-07      First Issue to implement #1032
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

/**
 * Importer class for sbd files from editor www.sbide.de
 * 
 * @author Kay Gürtzig
 */
public class SbdImporter implements INSDImporter {

	/**
	 * StringList of supported sbde file names
	 */
	private static final StringList SBIDE_TYPES = StringList.explode("int,float,char,vint,vfloat,vchar", ",");
	
	private static final String ARRAY_INDEX_WARNING = "Caution: Indexing of the array may be biased!";
	
	/**
	 * Holds the names of variables that were declared as arrays
	 */
	private StringList arrayVariables = new StringList();
	
	/**
	 * 
	 */
	public SbdImporter() {
	}

	@Override
	public String getDialogTitle() {
		return "sbide";
	}

	@Override
	public String getFileDescription() {
		return "sbide files";
	}

	@Override
	public String[] getFileExtensions() {
		final String[] exts = { "sbd" };
		return exts;
	}

	@Override
	public FileFilter getFileFilter() {
		return new javax.swing.filechooser.FileFilter() {

			/* (non-Javadoc)
			 * @see javax.swing.filechooser.FileFilter#getDescription()
			 */
			@Override
			public final String getDescription() 
			{
				return getFileDescription();
			}

			/* (non-Javadoc)
			 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
			 */
			@Override
			public final boolean accept(File f) 
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

		};
	}


	@Override
	public Root parse(String _filename) throws SAXException, IOException {
		Root root = null;
		URL url = new URL(_filename);
		File file = new File(url.getPath());
		try {
			String content = "";
			content = new String(Files.readAllBytes(file.toPath()));
			StringList tokens = Element.splitLexically(content, true).trim();
			int posBrace = -1;
			if (tokens.get(0).equals("{") && (posBrace = tokens.indexOf("}", 1)) > 0) {
				root = new Root();
				root.setText(tokens.concatenate("", 1, posBrace).trim());
				tokens.remove(0, posBrace+1);
				while (!tokens.isEmpty() && tokens.get(0).equals("{")) {
					String unexpected = parseElement(tokens, root.children, file);
					if (unexpected != null) {
						throw new IOException("Inconsistent file content (\"" + unexpected + "\" instead of expected '{').");
					};
				}
			}
		}
		catch (IOException ex) {
			throw ex;
		}
		catch (Exception ex) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error on importing file " + _filename, ex);
			throw ex;
		}
		if (root != null && !root.isEmpty()) {
			root.origin += " / " + this.getClass().getSimpleName() + ": \"" + file + "\"";
			root.setChanged(false);
		}
		return root;
	}
	
	/**
	 * Recursively parses the next brace block from the given {@code tokens},
	 * derives the respective Element from it, appends it to the {@link Subqueue}
	 * {@code node} and removes the consumed token sequence from {@code tokens}.
	 * 
	 * @param tokens - Remainder of the tokenized file content (will be shortened)
	 * @param node - the {@link Subqueue} to append the parsed element to
	 * @param filepath - path of the sbide file to be imported
	 * @return Either {@code null} (if parsing succeeded) or the unexpected token
	 */
	private String parseElement(StringList tokens, Subqueue node, File file) {
		int posBrace = tokens.indexOf("}");
		if (!tokens.get(0).equals("{") || posBrace < 1) {
			// Syntax error
			return tokens.get(0);
		}
		if (posBrace == 1) {
			// empty instruction (may occur as loop body)
			tokens.remove(0, posBrace+1);
			return null;
		}
		String typeStr = tokens.get(1);
		try {
			int typeCode = Integer.parseInt(typeStr);
			switch (typeCode) {
			case 1:	// Instruction
			{
				StringList content = tokens.subSequence(3, posBrace);
				content.replaceAll("∧", "and");
				content.replaceAll("∨", "or");
				content.replaceInElements("↵", "\\n");
				content.replaceAllBetween("Ausgabe", CodeParser.getKeyword("output"), true, 0, 1);
				content.replaceAllBetween("output", CodeParser.getKeyword("output"), true, 0, 1);
				content.replaceAllBetween("Eingabe", CodeParser.getKeyword("input"), true, 0, 1);
				content.replaceAllBetween("input", CodeParser.getKeyword("input"), true, 0, 1);
				Instruction instr = new Instruction(content.concatenate(""));
				if (refersToArray(content)) {
					instr.setComment(ARRAY_INDEX_WARNING);
				}
				node.addElement(instr);
			}
				break;
			case 2:	// Alternative
			{
				posBrace = tokens.indexOf("{", 1); // start of first branch
				StringList cond = tokens.subSequence(3, posBrace);
				cond.replaceAll("∧", "and");
				cond.replaceAll("∨", "or");
				cond.replaceInElements("↵", "\\n");
				Alternative alt = new Alternative(cond.concatenate(""));
				if (refersToArray(cond)) {
					alt.setComment(ARRAY_INDEX_WARNING);
				}
				node.addElement(alt);
				tokens.remove(0, posBrace+1);
				while (!tokens.isEmpty() && tokens.get(0).equals("{")) {
					String unexpected = parseElement(tokens, alt.qTrue, file);
					if (unexpected != null) {
						return unexpected;
					};
				}
				if (tokens.isEmpty()) {
					return "EOF";
				}
				if (!tokens.get(0).equals("}")) {
					// True branch not properly closed
					return tokens.get(0);
				}
				tokens.remove(0);
				if (tokens.isEmpty()) {
					return "EOF";
				}
				if (tokens.get(0).equals("{")) { // start of second branch
					tokens.remove(0);
					while (!tokens.isEmpty() && tokens.get(0).equals("{")) {
						String unexpected = parseElement(tokens, alt.qFalse, file);
						if (unexpected != null) {
							return unexpected;
						};
					}
					if (tokens.isEmpty()) {
						return "EOF";
					}
					if (!tokens.get(0).equals("}")) {
						// False branch not properly closed
						return tokens.get(0);
					}
					tokens.remove(0);
				}
				if (tokens.isEmpty()) {
					return "EOF";
				}
				if (!tokens.get(0).equals("}")) {
					// Alternative not properly closed
					return tokens.get(0);
				}
				posBrace = 0;
			}
				break;
			case 3:	// Case (not implemented in sbide, unlikely to occur therefore)
				break;
			case 4:	// While
			case 5:	// Repeat
			{
				ILoop loop = null;
				posBrace = tokens.indexOf("{", 1); // start of body
				StringList cond = tokens.subSequence(3, posBrace);
				cond.replaceAll("∧", "and");
				cond.replaceAll("∨", "or");
				cond.replaceInElements("↵", "\\n");
				String condStr = cond.concatenate("");
				if (typeCode == 4) {
					loop = new While(condStr);
				}
				else {
					loop = new Repeat(Element.negateCondition(condStr));
				}
				node.addElement((Element)loop);
				if (refersToArray(cond)) {
					node.getElement(node.getSize()-1).setComment(ARRAY_INDEX_WARNING);
				}
				
				tokens.remove(0, posBrace);	// Eliminate condition tokens
				// There is no brace pair enclosing the body (unless it is empty)
				while (!tokens.isEmpty() && tokens.get(0).equals("{")) {
					String unexpected = parseElement(tokens, loop.getBody(), file);
					if (unexpected != null) {
						return unexpected;
					};
				}
				if (tokens.isEmpty()) {
					return "EOF";
				}
				if (!tokens.get(0).equals("}")) {
					// Loop not properly closed
					return tokens.get(0);
				}
				posBrace = 0;;
			}
				break;
			default:	// Unknown code - ignore the element
				Logger.getLogger(getClass().getName()).log(Level.WARNING,
						"Unknown element type " + typeStr + " in file " + file.getPath());
			}
			tokens.remove(0, posBrace+1);
		}
		catch (NumberFormatException ex) {
			if (posBrace == 6 && tokens.get(2).isBlank() && tokens.get(4).isBlank()) {
				// Seems to be a declaration
				StringBuilder sb = new StringBuilder();
				sb.append("var ");
				int typeIx = SBIDE_TYPES.indexOf(typeStr);
				if (typeIx >= SBIDE_TYPES.count()/2) {
					// Seems to be a vector, cut off the "v" prefix and get the size
					typeStr = "array[1 .. " + tokens.get(5) + "] of " + typeStr.substring(1);
					arrayVariables.addIfNew(tokens.get(3));
				}
				sb.append(tokens.get(3));	// Variable name
				sb.append(": ");
				sb.append(typeStr);			// data type description
				node.addElement(new Instruction(sb.toString()));
			}
			else if (posBrace < 0) {
				// Syntax error
				return "{" + typeStr;
			}
			// remove the brace block
			tokens.remove(0, posBrace+1);
		}
		return null;
	}

	/**
	 * Checks whether the expression in the token list might refer to an array
	 * variable oder some array element access.
	 * 
	 * @param tokens - the tokenised element text
	 * @return {@code true} iff the token list contains the name of a variable that
	 * was declared as array or a pair of brackets.
	 */
	private boolean refersToArray(StringList tokens) {
		boolean hasBrackets = tokens.contains("[") && tokens.contains("]");
		for (int i = 0; i < arrayVariables.count(); i++) {
			if (tokens.contains(arrayVariables.get(i)) && hasBrackets) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Internal check for acceptable input files. The default implementation just
	 * compares the filename extension with the extensions configured in and
	 * provided by {@link #getFileExtensions()}. Helper method for method 
	 * {@link #accept(File)}.
	 * @param _filename
	 * @return true if the import file is formally welcome. 
	 */
	protected final boolean isOK(String _filename)
	{
		boolean res = false;
		String ext = getExtension(_filename); 
		if (ext != null)
		{
			for (int i = 0; i < getFileExtensions().length; i++)
			{
				res = res || (ext.equalsIgnoreCase(getFileExtensions()[i]));
			}
		}
		return res;
	}

	private static final String getExtension(File f) 
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
	
	private static final String getExtension(String s) 
	{
		String ext = null;
		int i = s.lastIndexOf('.');
		
		if (i > 0 &&  i < s.length() - 1) 
		{
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}
	
}

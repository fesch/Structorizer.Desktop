/*
    Structorizer
    A little tool which you can use to create Nassi-Schneiderman Diagrams (NSD)

    Copyright (C) 2009  Bob Fisch
    Copyright (C) 2017  StructorizerParserTemplate.pgt: Kay Gürtzig

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
 *      Description:    Inheritance class for alternative C parsers, handles the common pre-processing.
 *
 ******************************************************************************************************
 *
 *      Revision List (Template File!)
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.03.02      First Issue for Iden GOLDEngine
 *      Kay Gürtzig     2017.03.11      Parameter annotations and some comments corrected, indentation unified
 *      Kay Gürtzig     2018.03.26      Imports revised
 *
 ******************************************************************************************************
 *
 *      Revision List (this parser)
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2018.06.19      First Issue (derived from the common parts of CParser and C99Parser)
 *
 ******************************************************************************************************
 *
 *     Comment:		
 *
 ******************************************************************************************************/

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.creativewidgetworks.goldparser.engine.Reduction;

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.structorizer.elements.TypeMapEntry;
import lu.fisch.utils.StringList;

/**
 * Code import parser class of Structorizer 3.27, based on GOLDParser 5.0 for the ANSI-C99 language.
 * This file contains grammar-specific constants and individual routines to build
 * structograms (Nassi-Shneiderman diagrams) from the parsing tree. 
 * @author Kay Gürtzig
 */
public abstract class CPreParser extends CodeParser
{

	/** Default diagram name for an importable program diagram with global definitions */
	protected static final String DEFAULT_GLOBAL_NAME = "GlobalDefinitions";
	/** Template for the generation of grammar-conform user type ids (typedef-declared) */
	private static final String USER_TYPE_ID_MASK = "user_type_%03d";
	/** Replacement pattern for the decomposition of composed typdefs (named struct def + type def) */
	private static final String TYPEDEF_DECOMP_REPLACER = "$1 $2;\ntypedef $1 $3;";

	//---------------------- Grammar specification ---------------------------

	/**
	 * If this flag is set then program names derived from file name will be made uppercase
	 * This default will be initialized in consistency with the Analyser check 
	 */
	protected boolean optionUpperCaseProgName = false;

	//------------------------------ Constructor -----------------------------

	/**
	 * Constructs a parser for language ANSI-C99, loads the grammar as resource and
	 * specifies whether to generate the parse tree as string
	 */
	public CPreParser() {
	}

	//---------------------- File Filter configuration ---------------------------
	
 	@Override
	public String[] getFileExtensions() {
		final String[] exts = { "c", "h" };
		return exts;
	}

	//------------------- Comment delimiter specification ---------------------------------
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.parsers.CodeParser#getCommentDelimiters()
	 */
	protected String[][] getCommentDelimiters()
	{
		return new String[][]{
			{"/*", "*/"},
			{"//"}
		};
	}


	//---------------------------- Local Definitions ---------------------

	private static enum PreprocState {TEXT, TYPEDEF, STRUCT_UNION_ENUM, STRUCT_UNION_ENUM_ID, COMPLIST, /*ENUMLIST, STRUCTLIST,*/ TYPEID};
	private StringList typedefs = new StringList();
	// START KGU#388 2017-09-30: Enh. #423 counter for anonymous types
	protected int typeCount = 0;
	// END KGU#388 2017-09-30
	
	// START KGU#376 2017-07-01: Enh. #389 - modified mechanism
	// Roots having induced global definitions, which will have to be renamed as soon as the name gets known
	//private LinkedList<Call> provisionalImportCalls = new LinkedList<Call>();
	protected LinkedList<Root> importingRoots = new LinkedList<Root>();
	// END KGU#376 2017-07-01

	private String ParserEncoding;
	private String ParserPath;
	
	/** Fix list of common preprocesssor-defined type names for convenience */
	private	final String[][] typeReplacements = new String[][] {
		{"size_t", "unsigned long"},
		{"time_t", "unsigned long"},
		{"ptrdiff_t", "unsigned long"}
	};
	
	static HashMap<String, String[]> defines = new LinkedHashMap<String, String[]>();

	final static Pattern PTRN_VOID_CAST = Pattern.compile("(^\\s*|.*?[^\\w\\s]+\\s*)\\(\\s*void\\s*\\)(.*?)");
	static Matcher mtchVoidCast = PTRN_VOID_CAST.matcher("");
	// START KGU#519 2018-06-17: Enh. #541
	// macro signature:  macroname ( 3 )
	private static final Pattern PTRN_MACRO_SIG = Pattern.compile("(\\w+)\\(\\s*([0-9]*)\\s*\\)");
	private static Matcher mtchMacroSig = PTRN_MACRO_SIG.matcher("");
	// END KGU#519 2018-06-17

	//----------------------------- Preprocessor -----------------------------

	/**
	 * Performs some necessary preprocessing for the text file. Actually opens the
	 * file, filters it and writes a new temporary file "Structorizer&lt;randomstring&gt;.c"
	 * or "Structorizer&lt;randomstring&gt;.h", which is then actually parsed.
	 * For the C Parser e.g. the preprocessor directives must be removed and possibly
	 * be executed (at least the defines. with #if it would get difficult).
	 * The preprocessed file will always be saved with UTF-8 encoding.
	 * @param _textToParse - name (path) of the source file
	 * @param _encoding - the expected encoding of the source file.
	 * @return The File object associated with the preprocessed source file.
	 */
	@Override
	protected File prepareTextfile(String _textToParse, String _encoding)
	{	
		this.ParserPath = null; // set after file object creation
		this.ParserEncoding	= _encoding;
		
		// START KGU#519 2018-06-17: Enh. 541 Empty the defines before general start
		defines.clear();
		// END KGU#519 2018-06-17

		//========================================================================!!!
		// Now introduced as plugin-defined option configuration for C
		//this.setPluginOption("typeNames", "cob_field,cob_u8_ptr,cob_call_union");
		//                  +"cob_content,cob_pic_symbol,cob_field_attr");
		//========================================================================!!!
		
		boolean parsed = false;
		
		StringBuilder srcCodeSB = new StringBuilder();
		parsed = processSourceFile(_textToParse, srcCodeSB);

		File interm = null;
		if (parsed) {
			try {
//				for (Entry<String, String> entry: defines.entrySet()) {
////					if (logFile != null) {
////						logFile.write("CParser.prepareTextfile(): " + Matcher.quoteReplacement((String)entry.getValue()) + "\n");
////					}
//					srcCode = srcCode.replaceAll("(.*?\\W)" + entry.getKey() + "(\\W.*?)", "$1"+ Matcher.quoteReplacement((String)entry.getValue()) + "$2");
//				}
			
				// Now we try to replace all type names introduced by typedef declarations
				// because the grammar doesn't cope with user-defined type ids.
				String srcCode = this.prepareTypedefs(srcCodeSB.toString(), _textToParse);
//				System.out.println(srcCode);
				
				// trim and save as new file
				interm = File.createTempFile("Structorizer", "." + getFileExtensions()[0]);
				OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(interm), "UTF-8");
				ow.write(srcCode.trim()+"\n");
				//System.out.println("==> "+filterNonAscii(srcCode.trim()+"\n"));
				ow.close();
			}
			catch (Exception e) 
			{
				System.err.println("CParser.prepareTextfile() creation of intermediate file -> " + e.getMessage());
			}
		}
		return interm;
	}

	/**
	 * Performs some necessary preprocessing for the text file. Actually opens the
	 * file, filters it places its contents into the given StringBuilder (if set).
	 * For the C Parser e.g. the preprocessor directives must be removed and possibly
	 * be executed (at least the defines. with #if it would get difficult).
	 * @param _textToParse - name (path) of the source file
	 * @param srcCodeSB - optional: StringBuilder to store the content of the preprocessing<br/>
	 * if not given only the preprocessor handling (including #defines) will be done  
	 * @return info if the preprocessing worked
	 */
	private boolean processSourceFile(String _textToParse, StringBuilder srcCodeSB) {
		
		try
		{
			File file = new File(_textToParse);
			if (this.ParserPath == null) {
				this.ParserPath = file.getAbsoluteFile().getParent() + File.separatorChar;
			}
			DataInputStream in = new DataInputStream(new FileInputStream(file));
			// START KGU#193 2016-05-04
			BufferedReader br = new BufferedReader(new InputStreamReader(in, this.ParserEncoding));
			// END KGU#193 2016-05-04
			String strLine;
			boolean inComment = false;

			// START KGU#519 2018-06-17: Enh. #541
			registerRedundantDefines(defines);
			// END KGU#519 2018-06-17
			
			//Read File Line By Line
			// Preprocessor directives are not tolerated by the grammar, so drop them or try to
			// do the #define replacements (at least roughly...) which we do
			while ((strLine = br.readLine()) != null)
			{
				String trimmedLine = strLine.trim();
				if (trimmedLine.isEmpty()) {
					if (srcCodeSB != null) {
						// no processing if we only want to check for defines
						srcCodeSB.append("\n");
					}
					continue;
				}
				
				// the grammar doesn't know about continuation markers,
				// concatenate the lines here
				if (strLine.endsWith("\\")) {
					String newlines = "";
					strLine = strLine.substring(0, strLine.length() - 1);
					String otherline = "";
					while ((otherline = br.readLine()) != null) {
						newlines += "\n";
						if (otherline.endsWith("\\")) {
							strLine += otherline.substring(0, otherline.length() - 1);
						} else {
							strLine += otherline;
							break;
						}
					}
					trimmedLine = strLine.trim();
					// add line breaks for better line counter,
					// works but looks strange in the case of errors when
					// the "preceding context" consist of 8 empty lines
					strLine += newlines;
				}
				
				// check if we are in a comment block, in this case look for the end
				boolean commentsChecked = false;
				String commentFree = "";
				String lineTail = trimmedLine;
				while (!lineTail.isEmpty() && !commentsChecked) {
					if (inComment) {
						// check if the line ends the current comment block
						int commentPos = lineTail.indexOf("*/");
						if (commentPos >= 0) {
							inComment = false;
							commentPos += 2;
							if (lineTail.length() > commentPos) {
								lineTail = " " + lineTail.substring(commentPos).trim();
							} else {
								lineTail = "";
							}
						}
						else {
							commentsChecked = true;
							lineTail = "";
						}
					}

					if (!inComment && !lineTail.isEmpty()) {
						// remove inline comments
						int commentPos = lineTail.indexOf("//");
						if (commentPos > 0) {
							lineTail = lineTail.substring(0, commentPos).trim(); 
						}
						// check if the line starts a new comment block
						commentPos = lineTail.indexOf("/*");
						if (commentPos >= 0) {
							inComment = true;
							if (commentPos > 0) {
								commentFree += " " + lineTail.substring(0, commentPos).trim();
							}
							commentPos += 2;
							if (lineTail.length() > commentPos) {
								lineTail = lineTail.substring(commentPos);
							} else {
								lineTail = "";
							}
						}
						else {
							commentsChecked = true;
						}
					}

				}
				trimmedLine = (commentFree + lineTail).trim();

				// Note: trimmedLine can be empty if we start a block comment only
				if (trimmedLine.isEmpty()) {
					if (srcCodeSB != null) {
						// no processing if we only want to check for defines
						srcCodeSB.append(strLine);
						srcCodeSB.append("\n");
					}
					continue;
				}
				// FIXME: try to take care for #if/#else/#endif, maybe depending upon an import setting
				//        likely only useful if we parse includes...
				//        and/or add a standard (dialect specific) list of defines
				if (trimmedLine.charAt(0) == '#') {
					if (srcCodeSB == null) {
						handlePreprocessorLine(trimmedLine.substring(1), defines);
						// no further processing if we only want to check for defines
						continue;
					}
					srcCodeSB.append(handlePreprocessorLine(trimmedLine.substring(1), defines));
					srcCodeSB.append(strLine);
				} else {
					if (srcCodeSB == null) {
						// no further processing if we only want to check for defines
						continue;
					}
					strLine = replaceDefinedEntries(strLine, defines);
					// The grammar doesn't cope with customer-defined type names nor library-defined ones, so we will have to
					// replace as many as possible of them in advance.
					// We cannot guess however, what's included since include files won't be available for us.
					for (String[] pair: typeReplacements) {
						String search = "(^|.*?\\W)"+Pattern.quote(pair[0])+"(\\W.*?|$)";
						if (strLine.matches(search)) {
							strLine = strLine.replaceAll(search, "$1" + pair[1] + "$2");
						}
					}
					mtchVoidCast.reset(strLine);
					if (mtchVoidCast.matches()) {
						strLine = mtchVoidCast.group(1) + mtchVoidCast.group(2);	// checkme
					}
					srcCodeSB.append(strLine);
				}
				srcCodeSB.append("\n");
			}
			//Close the input stream
			in.close();
			return true;
		}
		catch (Exception e) 
		{
			if (srcCodeSB != null) {
				System.err.println("CParser.processSourcefile() -> " + e.getMessage());
			}
			return false;
		}
	}

	// START KGU#519 2018-06-17: Enh. #541
	/**
	 * Analyses the import option "redundantNames" and derives dummy defines from
	 * them that are suited to eliminate the respective texts from the code. 
	 */
	private void registerRedundantDefines(HashMap<String, String[]> defines) {
		String namesToBeIgnored = (String)this.getPluginOption("redundantNames", null);
		if (namesToBeIgnored != null) {
			String[] macros = namesToBeIgnored.split(",");
			for (String macro: macros) {
				macro = macro.trim();
				if (mtchMacroSig.reset(macro).matches()) {
					String macroName = mtchMacroSig.group(1).trim();
					String countStr = mtchMacroSig.group(2).trim();
					int count = 1;
					try {
						if (!countStr.isEmpty()) {
							count = Integer.parseInt(countStr);
						}
						String[] pseudoArgs = new String[count+1];
						pseudoArgs[0] = "";
						for (int i = 0; i < count; i++) {
							pseudoArgs[i+1] = "arg" + i;
						}
						defines.put(macroName, pseudoArgs);
						log("Prepared to eliminate redundant macro \"" + macro + "\"\n", false);
					}
					catch (NumberFormatException ex) {
						log("*** Illegal redundant macro specification: " + macro + "\n", true);
					}
				}
				else {
					defines.put(macro, new String[]{""});
					log("Prepared to eliminate redundant symbol \"" + macro + "\"\n", false);
				}
			}
		}
	}
	// END KGU#519 2018-06-17

	// Patterns and Matchers for preprocessing
	// (reusable, otherwise these get created and compiled over and over again)
	// #define	a	b
	private static final Pattern PTRN_DEFINE = Pattern.compile("^define\\s+(\\w*)\\s+(\\S.*?)");
	// #define	a	// empty
	private static final Pattern PTRN_DEFINE_EMPTY = Pattern.compile("^define\\s+(\\w*)\\s*");
	// #define	a(b)	functionname (int b)
	// #define	a(b,c,d)	functionname (int b, char *d)	// multiple ones, some may be omitted
	// #define	a(b)	// empty
	private static final Pattern PTRN_DEFINE_FUNC = Pattern.compile("^define\\s+(\\w+)\\s*\\(([^)]+)\\)\\s+(.*)");
	// #undef	a
	private static final Pattern PTRN_UNDEF = Pattern.compile("^undef\\s+(\\w+)(.*)");
	// #undef	a
	private static final Pattern PTRN_INCLUDE = Pattern.compile("^include\\s+[<\"]([^>\"]+)[>\"]");
	// several things we can ignore: #pragma, #warning, #error, #message 
	private static final Pattern PTRN_IGNORE = Pattern.compile("^(?>pragma)|(?>warning)|(?>error)|(?>message)");
	
	private static Matcher mtchDefine = PTRN_DEFINE.matcher("");
	private static Matcher mtchDefineEmpty = PTRN_DEFINE_EMPTY.matcher("");
	private static Matcher mtchDefineFunc = PTRN_DEFINE_FUNC.matcher("");
	private static Matcher mtchUndef = PTRN_UNDEF.matcher("");
	private static Matcher mtchInclude = PTRN_INCLUDE.matcher("");
	private static Matcher mtchIgnore = PTRN_IGNORE.matcher("");

	// Patterns and Matchers for parsing / building
	// detection of a const modifier in a declaration
	private static final Pattern PTRN_CONST = Pattern.compile("(^|.*?\\s+)const(\\s+.*?|$)");

	protected static Matcher mtchConst = PTRN_CONST.matcher("");

	/**
	 * Helper function for prepareTextfile to handle C preprocessor commands
	 * @param preprocessorLine	line for the preprocessor without leading '#'
	 * @param defines 
	 * @return comment string that can be used for prefixing the original source line
	 */
	private String handlePreprocessorLine(String preprocessorLine, HashMap<String, String[]> defines)
	{
		mtchDefineFunc.reset(preprocessorLine);
		if (mtchDefineFunc.matches()) {
			// #define	a1(a2,a3,a4)	stuff  ( a2 ) 
			//          1  >  2   <		>     3     <
			String symbol = mtchDefineFunc.group(1);
			String[] params = mtchDefineFunc.group(2).split(",");
			String subst = mtchDefineFunc.group(3);
			String substTab[] = new String[params.length + 1];
			substTab[0] = replaceDefinedEntries(subst, defines).trim();
			for (int i = 0; i < params.length; i++) {
				substTab[i+1] = params[i].trim();
			}
			defines.put(symbol, substTab);
			return "// preparser define (function): ";
		}

		mtchDefine.reset(preprocessorLine);
		if (mtchDefine.matches()) {
			// #define	a	b
			//          1	2
			String symbol = mtchDefine.group(1);
			String subst[] = new String[1];
			subst[0] = mtchDefine.group(2);
			subst[0] = replaceDefinedEntries(subst[0], defines).trim();
			defines.put(symbol, subst);
			return "// preparser define: ";
		}
		
		mtchUndef.reset(preprocessorLine);
		if (mtchUndef.matches()) {
			// #undef	a
			String symbol = mtchUndef.group(1);
			defines.remove(symbol);
			return "// preparser undef: ";
		}

		mtchDefineEmpty.reset(preprocessorLine);
		if (mtchDefineEmpty.matches()) {
			// #define	a
			String symbol = mtchDefineEmpty.group(1);
			String subst[] = new String[]{""};
			defines.put(symbol, subst);
			return "// preparser define: ";
		}

		mtchInclude.reset(preprocessorLine);
		if (mtchInclude.matches()) {
			// #include	"header"
			// FIXME: *MAYBE* store list of non-system includes to parse as IMPORT diagram upon request?
			String incName = mtchInclude.group(1);
			// do internal preparsing for resolving define/struct/typedef for the imported file
			// FIXME: maybe do only when set as preparser option
			// FIXME: add option to use more than the main file's path as preparser option
			if (File.separatorChar == '\\') {
				// FIXME (KGU) This doesn't seem so good an idea - usually both systems cope with '/' 
				incName = incName.replaceAll("/", "\\\\");
			} else {
				incName = incName.replaceAll("\\\\", File.separator);
			}
			
			if (processSourceFile(this.ParserPath + incName, null)) {
				return "// preparser include (parsed): ";
			} else {
				return "// preparser include (failed): ";
			}
		}

		mtchIgnore.reset(preprocessorLine);
		if (mtchIgnore.find()) {
			// #pragma, #error, #warning, #message ...
			return "// preparser instruction (ignored): ";
		}

		// #if, #ifdef, #ifndef, #else, #elif, #endif, ...
		return "// preparser instruction (not parsed!): ";
	}

	/**
	 * Detects typedef declarations in the {@code srcCode}, identifies the defined type names and replaces
	 * them throughout their definition scopes text with generic names "user_type_###" defined in the grammar
	 * such that the parse won't fail. The type name map is represented by the static variable {@link #typedefs}
	 * where the ith entry is mapped to a type id "user_type_&lt;i+1&gt;" for later backwards replacement.
	 * @param srcCode - the pre-processed source code as long string
	 * @param _textToParse - the original file name
	 * @return the source code with replaced type names
	 * @throws IOException
	 */
	private String prepareTypedefs(String srcCode, String _textToParse) throws IOException
	{
		// In a first step we gather all type names defined via typedef in a
		// StringList mapping them by their index to generic type ids being
		// defined in the grammar ("user_type_###"). It will be a rudimentary parsing
		// i.e. we don't consider anything except typedef declarations and we expect
		// a syntactically correct construct. If something strange occurs then we just
		// ignore the text until we bump into another typedef keyword.
		// In the second step we replace all identifiers occurring in the map with
		// their associated generic name, respecting the definition scope.
		
		typedefs.clear();

		Vector<Integer[]> blockRanges = new Vector<Integer[]>();
		LinkedList<String> typedefDecomposers = new LinkedList<String>();
		
		// START KGU 2017-05-26: workaround for the typeId deficiency of the grammar: allow configured global typenames
		String configuredTypeNames = (String)this.getPluginOption("typeNames", null);
		if (configuredTypeNames != null) {
			String[] typeIds = configuredTypeNames.split("(,| )");
			for (int i = 0; i < typeIds.length; i++) {
				String typeId = typeIds[i].trim();
				if (typeId.matches("^\\w+$")) {
					typedefs.add(typeId);
					blockRanges.addElement(new Integer[]{0, -1});
				}
			}
		}
		// END KGU 2017-05-26
			
		Stack<Character> parenthStack = new Stack<Character>();
		Stack<Integer> blockStarts = new Stack<Integer>();
		int blockStartLine = -1;
		int typedefLevel = -1;
		int indexDepth = 0;
		PreprocState state = PreprocState.TEXT;
		String lastId = null;
		char expected = '\0';
		
		StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(srcCode));
		tokenizer.quoteChar('"');
		tokenizer.quoteChar('\'');
		tokenizer.slashStarComments(true);
		tokenizer.slashSlashComments(true);
		tokenizer.parseNumbers();
		tokenizer.eolIsSignificant(true);
		// Underscore must be added to word characters!
		tokenizer.wordChars('_', '_');
		
		// A regular search pattern to find and decompose type definitions with both
		// struct/union/enum id and type id like in:
		// typedef struct structId {...} typeId [, ...];
		// (This is something the used grammar doesn't cope with and so it is to be 
		// decomposed as follows for the example above:
		// struct structId {...};
		// typedef struct structId typeId [, ...];
		String typedefStructPattern = "";
		
		while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
			String word = null;
			log("[" + tokenizer.lineno() + "]: ", false);
			switch (tokenizer.ttype) {
			case StreamTokenizer.TT_EOL:
				log("**newline**\n", false);
				if (!typedefStructPattern.isEmpty()) {
					typedefStructPattern += ".*?\\v";
				}
				break;
			case StreamTokenizer.TT_NUMBER:
				log("number: " + tokenizer.nval + "\n", false);
				if (!typedefStructPattern.isEmpty()) {
					// NOTE: a non-integral number literal is rather unlikely within a type definition...
					typedefStructPattern += "\\W+[+-]?[0-9]+";
				}
				break;
			case StreamTokenizer.TT_WORD:
				word = tokenizer.sval;
				log("word: " + word + "\n", false);
				if (state == PreprocState.TYPEDEF) {
					if (word.equals("enum") || word.equals("struct") || word.equals("union")) {
						state = PreprocState.STRUCT_UNION_ENUM;
						typedefStructPattern = "typedef\\s+(" + word;
					}
					else {
						lastId = word;	// Might be the defined type id if no identifier will follow
						typedefStructPattern = "";	// ...but it's definitely no combined struct/type definition
					}
				}
				else if (state == PreprocState.TYPEID && indexDepth == 0) {
					typedefs.add(word);
					blockRanges.add(new Integer[]{tokenizer.lineno()+1, (blockStarts.isEmpty() ? -1 : blockStarts.peek())});
					if (!typedefStructPattern.isEmpty()) {
						if (typedefStructPattern.matches(".*?\\W")) {
							typedefStructPattern += "\\s*" + word;
						}
						else {
							typedefStructPattern += "\\s+" + word;
						}
					}
				}
				// START KGU 2017-05-23: Bugfix - declarations like "typedef struct structId typeId"
				else if (state == PreprocState.STRUCT_UNION_ENUM) {
					state = PreprocState.STRUCT_UNION_ENUM_ID;
					// This must be the struct/union/enum id.
					typedefStructPattern += "\\s+" + word + ")\\s*(";	// named struct/union/enum: add its id and switch to next group
				}
				else if (state == PreprocState.STRUCT_UNION_ENUM_ID) {
					// We have read the struct/union/enum id already, so this must be the first type id.
					typedefs.add(word);
					blockRanges.add(new Integer[]{tokenizer.lineno()+1, (blockStarts.isEmpty() ? -1 : blockStarts.peek())});
					typedefStructPattern = "";	// ... but it's definitely no combined struct and type definition
					state = PreprocState.TYPEID;
				}
				// END KGU 2017-05-23
				else if (word.equals("typedef")) {
					typedefLevel = blockStarts.size();
					state = PreprocState.TYPEDEF;
				}
				else if (state == PreprocState.COMPLIST && !typedefStructPattern.isEmpty()) {
					if (typedefStructPattern.matches(".*\\w") && !typedefStructPattern.endsWith("\\v")) {
						typedefStructPattern += "\\s+";
					}
					else if (typedefStructPattern.endsWith(",") || typedefStructPattern.endsWith(";")) {
						// these are typical positions for comments...
						typedefStructPattern += ".*?";
					}
					else {
						typedefStructPattern += "\\s*";
					}
					typedefStructPattern += word;
				}
				break;
			case '\'':
				log("character: '" + tokenizer.sval + "'\n", false);
				if (!typedefStructPattern.isEmpty()) {
					typedefStructPattern += Pattern.quote("'"+tokenizer.sval+"'");	// We hope that there are no parentheses inserted
				}
				break;
			case '"':
				log("string: \"" + tokenizer.sval + "\"\n", false);
				if (!typedefStructPattern.isEmpty()) {
					typedefStructPattern += Pattern.quote("\""+tokenizer.sval+"\"");	// We hope that there are no parentheses inserted
				}
				break;
			case '{':
				blockStarts.add(tokenizer.lineno());
				if (state == PreprocState.STRUCT_UNION_ENUM || state == PreprocState.STRUCT_UNION_ENUM_ID) {
					if (state == PreprocState.STRUCT_UNION_ENUM) {
						typedefStructPattern = ""; 	// We don't need a decomposition
					}
					else {
						typedefStructPattern += "\\s*\\{";
					}
					state = PreprocState.COMPLIST;
				}
				parenthStack.push('}');
				break;
			case '(':
				if (!typedefStructPattern.isEmpty()) {
					typedefStructPattern += "\\s*\\(";
				}
				parenthStack.push(')');
				break;
			case '[':	// FIXME: Handle index lists in typedefs!
				if (!typedefStructPattern.isEmpty()) {
					typedefStructPattern += "\\s*\\[";
				}
				if (state == PreprocState.TYPEID) {
					indexDepth++;
				}
				parenthStack.push(']');
				break;
			case '}':
				blockStartLine = blockStarts.pop();
				// Store the start and current line no as block range if there are typedefs on this block level
				int blockEndLine = tokenizer.lineno();
				Integer[] entry;
				for (int i = blockRanges.size()-1; i >= 0 && (entry = blockRanges.get(i))[1] >= blockStartLine; i--) {
					if (entry[1] == blockStartLine && entry[1] < entry[0]) {
						entry[1] = blockEndLine;
					}
				}
				if (state == PreprocState.COMPLIST && typedefLevel == blockStarts.size()) {
					// After the closing brace, type ids are expected to follow
					if (!typedefStructPattern.isEmpty()) {
						typedefStructPattern += "\\s*\\})\\s*(";	// .. therefore open the next group
					}
					state = PreprocState.TYPEID;
				}
			case ')':
			case ']':	// Handle index lists in typedef!s
				{
					if (parenthStack.isEmpty() || tokenizer.ttype != (expected = parenthStack.pop().charValue())) {
						String errText = "**FILE PREPARATION TROUBLE** in line " + tokenizer.lineno()
						+ " of file \"" + _textToParse + "\": unmatched '" + (char)tokenizer.ttype
						+ "' (expected: '" + (expected == '\0' ? '\u25a0' : expected) + "')!";
						System.err.println(errText);
						log(errText, false);
					}
					else if (tokenizer.ttype == ']' && state == PreprocState.TYPEID) {
						indexDepth--;
						if (!typedefStructPattern.isEmpty()) {
							typedefStructPattern += "\\s*\\" + (char)tokenizer.ttype;
						}
					}
				}
					break;
			case '*':
				if (state == PreprocState.TYPEDEF) {
					state = PreprocState.TYPEID;
				}
				else if (state == PreprocState.STRUCT_UNION_ENUM_ID) {
					typedefStructPattern = "";	// Cannot be a combined definition: '*' follows immediately to the struct id
				}
				else if (!typedefStructPattern.isEmpty()) {
					typedefStructPattern += "\\s*[*]";
				}
				break;
			case ',':
				if (state == PreprocState.TYPEDEF && lastId != null) {
					typedefs.add(lastId);
					blockRanges.add(new Integer[]{tokenizer.lineno()+1, (blockStarts.isEmpty() ? -1 : blockStarts.peek())});
					if (!typedefStructPattern.isEmpty()) {
						// Type name won't be replaced within the typedef clause
						//typedefStructPattern += "\\s+" + String.format(USER_TYPE_ID_MASK, typedefs.count()) + "\\s*,";
						typedefStructPattern += "\\s+" + lastId + "\\s*,";
					}
					state = PreprocState.TYPEID;
				}
				else if (state == PreprocState.TYPEID) {
					if (!typedefStructPattern.isEmpty()) {
						typedefStructPattern += "\\s*,";
					}
				}
				break;
			case ';':
				if (state == PreprocState.TYPEDEF && lastId != null) {
					typedefs.add(lastId);
					blockRanges.add(new Integer[]{tokenizer.lineno()+1, (blockStarts.isEmpty() ? -1 : blockStarts.peek())});
					typedefStructPattern = "";
					state = PreprocState.TEXT;
				}
				else if (state == PreprocState.TYPEID) {
					if (!typedefStructPattern.isEmpty() && !typedefStructPattern.endsWith("(")) {
						typedefStructPattern += ")\\s*;";
						typedefDecomposers.add(typedefStructPattern);
					}
					typedefStructPattern = "";
					state = PreprocState.TEXT;						
				}
				else if (state == PreprocState.COMPLIST && !typedefStructPattern.isEmpty()) {
					typedefStructPattern += "\\s*;";
				}
				break;
			default:
				char tokenChar = (char)tokenizer.ttype;
				if (state == PreprocState.COMPLIST && !typedefStructPattern.isEmpty()) {
					typedefStructPattern += "\\s*" + Pattern.quote(tokenChar + "");
				}
				log("other: " + tokenChar + "\n", false);
			}
			log("", false);
		}
		StringList srcLines = StringList.explode(srcCode, "\n");
		// Now we replace the detected user-specific type names by the respective generic ones.
		for (int i = 0; i < typedefs.count(); i++) {
			String typeName = typedefs.get(i);
			Integer[] range = blockRanges.get(i);
			// Global range?
			if (range[1] < 0) {
				range[1] = srcLines.count()-1;
			}
			String pattern = "(^|.*?\\W)("+typeName+")(\\W.*?|$)";
			String subst = String.format(USER_TYPE_ID_MASK, i+1);
			this.replacedIds.put(subst, typeName);
			subst = "$1" + subst + "$3";
			for (int j = range[0]; j <= range[1]; j++) {
				if (srcLines.get(j).matches(pattern)) {
					srcLines.set(j, srcLines.get(j).replaceAll(pattern, subst));
				}
			}
		}
		srcCode = srcLines.concatenate("\n");
		
		// Now we try the impossible: to decompose compound struct/union/enum and type name definition
		for (String pattern: typedefDecomposers) {
			srcCode = srcCode.replaceAll(".*?" + pattern + ".*?", TYPEDEF_DECOMP_REPLACER);
		}

		return srcCode;
	}

	/**
	 * Part of the file preprocessing, is to replace all occurrences of any of the keys
	 * of string map {@code defines} by their corresponding values within the target
	 * string {@code toReplace}. 
	 * @param toReplace - The string representation of (a part of) the input file.
	 * @param defines - maps certain defined identifiers to more acceptable other ones.
	 * @return The resulting string.
	 */
	private String replaceDefinedEntries(String toReplace, HashMap<String, String[]> defines) {
		// START KGU#519 2018-06-17: Enh. #541 - The matching tends to fail if toReplace ends with newline characters
		// (The trailing newlines were appended on concatenating lines broken by end-standing backslashes to preserve line counting.)
		//if (toReplace.trim().isEmpty()) {
		//	return "";
		//}
		String nlTail = "";
		int nlPos = toReplace.indexOf('\n');
		if (nlPos >= 0) {
			nlTail = toReplace.substring(nlPos);	// Ought to contain the tail of \n characters
			toReplace = toReplace.substring(0, nlPos);
		}
		if (toReplace.trim().isEmpty()) {
			return nlTail;	// Preserve line count...
		}
		// END KGU#519 2018-06-17
		//log("CParser.replaceDefinedEntries(): " + Matcher.quoteReplacement((String)entry.getValue().toString()) + "\n", false);
		for (Entry<String, String[]> entry: defines.entrySet()) {
			
			// FIXME: doesn't work if entry is at start/end of toReplace 
			
			
			if (entry.getValue().length > 1) {
				//          key<val[0]>     <   val[1]   >
				// #define	a1(a2,a3,a4)	stuff (  a2  )
				// key  ( text1, text2, text3 )	--->	stuff (  text1  )
				// #define	a1(a2,a3,a4)
				// key  ( text1, text2, text3 )	--->
				// #define	a1(a2,a3,a4)	a2
				// key  ( text1, text2, text3 )	--->	text1
				// #define	a1(a2,a3,a4)	some text
				// key  ( text1, text2, text3 )	--->	some text
				/* FIXME: 
				 * The trouble here is that text1, text2 etc. might also contain parentheses, so may the following text.
				 * The result of the replacement would then be a total desaster
				 */
				Matcher matcher = Pattern.compile("(^|.*?\\W)" + entry.getKey() + "(\\s*)\\((.*)\\)(.*?)").matcher("");
				//while (toReplace.matches("(^|.*?\\W)" + entry.getKey() + "\\s*\\(.*\\).*?")) {
				while (matcher.reset(toReplace).matches()) {
					if (entry.getValue()[0].isEmpty()) {
						//toReplace = toReplace.replaceAll("(^|.*?\\W)" + entry.getKey() + "(\\s*)\\((.*)\\)(.*?)", "$1$2$4");
						toReplace = matcher.replaceAll("$1$2$4");
					} else {
						// The greedy quantifier inside the parentheses ensures that we get to the rightmost closing parenthesis
						//String argsRaw = toReplace.replaceFirst("(^|.*?\\W)" + entry.getKey() + "(\\s*)\\((.*)\\)(.*)", "$3");
						String argsRaw = matcher.group(3);
						// Now we split the balanced substring (up to the first unexpected closing parenthesis) syntactically
						// (The unmatched tail of argsRaw will be re-appended later)
						StringList args = Element.splitExpressionList(argsRaw, ",");
						// We test whether argument and parameter count match
						if (args.count() != entry.getValue().length - 1) {
							// FIXME: function-like define doesn't match arg count
							log("CParser.replaceDefinedEntries() cannot apply function macro\n\t"
									// START KGU#522 2018-06-17: Bugfix #540 reconstruction of the macro
									//+ entry.getKey() + entry.getValue().toString() + "\n\tdue to arg count diffs:\n\t"
									+ entry.getKey() + "(" + (new StringList(entry.getValue())).concatenate(", ", 1) + ")"
									+ "\n\tdue to arg count diffs:\n\t"
									// END KGU#522 2018-06-07							
									+ toReplace + "\n", true);
							// START KGU#522 2018-06-17: Bugfix #540 (emergency exit from a threatening eternal loop)
							break;
							// END KGU#522 2018-06-07
						}
						else {
							HashMap<String, String> argMap = new HashMap<String, String>();
							// Lest the substitutions should interfere with one another we first split the string for all parameters
							StringList parts = StringList.getNew(entry.getValue()[0]); 
							for (int i = 0; i < args.count(); i++) {
								String param = entry.getValue()[i+1];
								argMap.put(param, args.get(i));
								parts = StringList.explodeWithDelimiter(parts, param);
								// START KGU#522 2018-06-17: Bugfix #540 - we must recompose identifiers
								parts.removeAll("");
								int pos = -1;
								while ((pos = parts.indexOf(param, pos+1)) >= 0) {
									if (pos > 0 && parts.get(pos-1).matches(".*?\\w")) {
										parts.set(pos-1, parts.get(pos-1)+param);
										parts.remove(pos--);
									}
									if (pos+1 < parts.count() && parts.get(pos+1).matches("\\w.*?")) {
										parts.set(pos, parts.get(pos) + parts.get(pos+1));
										parts.remove(pos+1);
									}
								}
								// END KGU#522 2018-06-17
							}
							// Now we have all parts separated and walk through the StringList, substituting the parameter names
							for (int i = 0; i < parts.count(); i++) {
								String part = parts.get(i);
								if (!part.isEmpty() && argMap.containsKey(part)) {
									parts.set(i, argMap.get(part));
								}
							}
							// Now we correct possible matching defects
							StringList argsPlusTail = Element.splitExpressionList(argsRaw, ",", true);
							if (argsPlusTail.count() > args.count()) {
								String tail = argsPlusTail.get(args.count()).trim();
								// With high probability tail stars with a closing parenthesis, which has to be dropped if so
								// whereas the consumed parenthesis at the end has to be restored.
								if (tail.startsWith(")")) {
									tail = tail.substring(1) + ")";
								}
								parts.add(tail);
							}
							// This pattern differs in the last group from matcher (greedy <-> non-greedy)
							toReplace = toReplace.replaceFirst("(^|.*?\\W)" + entry.getKey() + "(\\s*)\\((.*)\\)(.*)",
									"$1" + Matcher.quoteReplacement(parts.concatenate()) + "$4");
						}
					}
				}
			} else {
				// from: #define	a	b, b can also be empty
				toReplace = toReplace.replaceAll("(^|.*?\\W)" + entry.getKey() + "(\\W.*?)",
						"$1" + Matcher.quoteReplacement((String) entry.getValue()[0]) + "$2");
			}
		}
		// START KGU#519 2018-06-17: Enh. #541 - To preserve line counting, we restore the temporarily cropped newlines
		//return toReplace;
		return toReplace + nlTail;
		// END KGU#519 2018-06-17
	}

	//---------------------- Build helpers for structograms ---------------------------

	/**
	 * Dummy Root for global definitions (will be put to main or the only function if
	 * there aren't more depending Roots)
	 */
	protected Root globalRoot = null;
	
	/**
	 * Creates an output instruction from the given arguments {@code _args} and adds it to
	 * the {@code _parentNode}.
	 * @param _reduction - the responsible rule
	 * @param _name - the name of the encountered input function (e.g. "scanf")
	 * @param _args - the argument expressions
	 * @param _parentNode - the {@link Subqueue} the output instruction is to be added to. 
	 */
	protected void buildInput(Reduction _reduction, String _name, StringList _args, Subqueue _parentNode) {
		//content = content.replaceAll(BString.breakup("scanf")+"[ ((](.*?),[ ]*[&]?(.*?)[))]", input+" $2");
		String content = getKeyword("input");
		if (_args != null) {
			if (_name.equals("scanf")) {
				// Forget the format string
				if (_args.count() > 0) {
					_args.remove(0);
				}
				for (int i = 0; i < _args.count(); i++) {
					String varItem = _args.get(i).trim();
					if (varItem.startsWith("&")) {
						_args.set(i, varItem.substring(1));
					}
				}
			}
			content += _args.concatenate(", ");
		}
		// START KGU#407 2017-06-20: Enh. #420 - comments already here
		//_parentNode.addElement(new Instruction(content.trim()));
		_parentNode.addElement(this.equipWithSourceComment(new Instruction(content.trim()), _reduction));
		// END KGU#407 2017-06-22
	}

	/**
	 * Creates an output instruction from the given arguments {@code _args} and adds it to
	 * the {@code _parentNode}.
	 * @param _reduction - the responsible rule
	 * @param _name - the name of the encountered output function (e.g. "printf")
	 * @param _args - the argument expressions
	 * @param _parentNode - the {@link Subqueue} the output instruction is to be added to. 
	 */
	protected void buildOutput(Reduction _reduction, String _name, StringList _args, Subqueue _parentNode) {
		//content = content.replaceAll(BString.breakup("printf")+"[ ((](.*?)[))]", output+" $1");
		String content = getKeyword("output") + " ";
		if (_args != null) {
			int nExpr = _args.count();
			// Find the format mask
			if (nExpr > 1 && _name.equals("printf") && _args.get(0).matches("^[\"].*[\"]$")) {
				// We try to split the string by the "%" signs which is of course dirty
				// Unfortunately, we can't use split because it eats empty chunks
				StringList newExprList = new StringList();
				String formatStr = _args.get(0);
				int posPerc = -1;
				formatStr = formatStr.substring(1, formatStr.length()-1);
				int i = 1;
				while ((posPerc = formatStr.indexOf('%')) >= 0 && i < _args.count()) {
					newExprList.add('"' + formatStr.substring(0, posPerc) + '"');
					formatStr = formatStr.substring(posPerc+1).replaceFirst(".*?[idxucsefg](.*)", "$1");
					newExprList.add(_args.get(i++));
				}
				if (!formatStr.isEmpty()) {
					newExprList.add('"' + formatStr + '"');
				}
				if (i < _args.count()) {
					newExprList.add(_args.subSequence(i, _args.count()).concatenate(", "));
				}
				_args = newExprList;
			}
			else {
				// Drop an end-standing newline since Structorizer produces a newline automatically
				String last = _args.get(nExpr - 1);
				if (last.equals("\"\n\"")) {
					_args.remove(--nExpr);
				}
				else if (last.endsWith("\n\"")) {
					_args.set(nExpr-1, last.substring(0, last.length()-2) + '"');
				}
			}
			content += _args.concatenate(", ");
		}
		// START KGU#407 2017-06-20: Enh. #420 - comments already here
		//_parentNode.addElement(new Instruction(content.trim()));
		_parentNode.addElement(this.equipWithSourceComment(new Instruction(content.trim()), _reduction));
		// END KGU#407 2017-06-22
	}

	/**
	 * Converts a C initializer expression for a struct type to a corresponding
	 * record initializer for Structorizer.
	 * This method may not be applicable if the type info isn't available
	 * @param _typeName - Name of the detected struct type
	 * @param _expr - the initialiser expression to be converted
	 * @param _typeEntry - the type entry corresponding to {@code _typeName}
	 * @return the converted initializer
	 */
	protected String convertStructInitializer(String _typeName, String _expr, TypeMapEntry _typeEntry) {
		StringList parts = Element.splitExpressionList(_expr.substring(1), ",", true);
		LinkedHashMap<String, TypeMapEntry> compInfo = _typeEntry.getComponentInfo(false);
		if (parts.count() > 1 && compInfo.size() >= parts.count() - 1) {
			int ix = 0;
			_expr = _typeName + "{";
			for (Entry<String, TypeMapEntry> comp: compInfo.entrySet()) {
				String part = parts.get(ix).trim();
				// Check for recursive structure initializers
				TypeMapEntry compType = comp.getValue();
				if (part.startsWith("{") && part.endsWith("}") &&
						compType != null && compType.isRecord() && compType.isNamed()) {
					part = convertStructInitializer(compType.typeName, part, compType);
				}
				_expr += comp.getKey() + ": " + part;
				if (++ix >= parts.count()-1) {
					_expr += parts.get(parts.count()-1);
					break;
				}
				else {
					_expr += ", ";
				}
			}
		}
		return _expr;
	}
	
	//------------------------- Postprocessor ---------------------------

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.parsers.CodeParser#subclassUpdateRoot(lu.fisch.structorizer.elements.Root, java.lang.String)
	 */
	@Override
	protected void subclassUpdateRoot(Root aRoot, String textToParse) {
		if (aRoot.getMethodName().equals("main")) {
			String fileName = new File(textToParse).getName();
			if (fileName.contains(".")) {
				fileName = fileName.substring(0, fileName.indexOf('.'));
			}
			if (this.optionUpperCaseProgName) {
				fileName = fileName.toUpperCase();
			}
			fileName = fileName.replaceAll("(.*?)[^A-Za-z0-9_](.*?)", "$1_$2");
			if (aRoot.getParameterNames().count() > 0) {
				String header = aRoot.getText().getText();
				header = header.replaceFirst("(.*?)main([((].*)", "$1" + fileName + "$2");
				aRoot.setText(header);
			}
			else {
				aRoot.setText(fileName);
				aRoot.setProgram(true);
			}
			// Are there some global definitions to be imported?
			if (this.globalRoot != null && this.globalRoot.children.getSize() > 0 && this.globalRoot != aRoot) {
				String oldName = this.globalRoot.getMethodName();
				String inclName = fileName + "Globals";
				this.globalRoot.setText(inclName);
				// START KGU#376 2017-05-17: Enh. #389 now we have an appropriate diagram type
				//this.globalRoot.setProgram(true);
				this.globalRoot.setInclude();
				// END KGU#376 2017-05-17
				// START KGU#376 2017-07-01: Enh. #389 - now register global includable with Root
//				for (Call provCall: this.provisionalImportCalls) {
//					provCall.setText(provCall.getText().get(0).replace(DEFAULT_GLOBAL_NAME, inclName).replace("???", inclName));
//				}
//				this.provisionalImportCalls.clear();
				for (Root impRoot: this.importingRoots) {
					if (impRoot.includeList != null) {
						int n = impRoot.includeList.replaceAll(oldName, inclName);
						n += impRoot.includeList.replaceAll(DEFAULT_GLOBAL_NAME, inclName);
						n += impRoot.includeList.replaceAll("???", inclName);
						if (n > 1) {
							impRoot.includeList.removeAll(inclName);
							impRoot.includeList.add(inclName);
						}
					}
					else {
						impRoot.includeList = StringList.getNew(inclName);
					}
				}
				this.importingRoots.clear();
				// END KGU#376 2017-07-01
			}
		}
		// START KGU#376 2017-04-11: enh. #389 import mechanism for globals
//		if (this.globalRoot != null && this.globalRoot != aRoot) {
//			String globalName = this.globalRoot.getMethodName();
//			// START KGU#376 2017-07-01: Enh. #389 - modified mechanism
////			Call importCall = new Call(getKeywordOrDefault("preImport", "import") + " " + (globalName.equals("???") ? DEFAULT_GLOBAL_NAME : globalName));
////			importCall.setColor(colorGlobal);
////			aRoot.children.insertElementAt(importCall, 0);
////			if (globalName.equals("???")) {
////				this.provisionalImportCalls.add(importCall);
////			}
//			if (aRoot.includeList == null) {
//				aRoot.includeList = StringList.getNew(globalName);
//			}
//			else {
//				aRoot.includeList.addIfNew(globalName);
//			}
//			if (!this.importingRoots.contains(aRoot)) {
//				this.importingRoots.add(aRoot);
//			}
//			// END KGU#376 2017-07-01
//		}
		// END KGU#376 2017-04-11
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.parsers.CodeParser#postProcess(java.lang.String)
	 */
	protected void subclassPostProcess(String textToParse)
	{
		// May there was no main function but global definitions
		if (this.globalRoot != null && this.globalRoot.children.getSize() > 0) {
			String globalName = this.globalRoot.getMethodName();
			if (globalName.equals("???")) {
				// FIXME: Shouldn't we also derive the name from the filename as in the method above?
				this.globalRoot.setText(globalName = DEFAULT_GLOBAL_NAME);
			}
			// START KGU#376 2017-05-23: Enh. #389 now we have an appropriate diagram type
			//this.globalRoot.setProgram(true);
			this.globalRoot.setInclude();
			// END KGU#376 2017-05-23
			// Check again that we haven't forgotten to update any include list
			for (Root dependent: this.importingRoots) {
				if (dependent.includeList == null) {
					dependent.includeList = StringList.getNew(globalName);
				}
				else {
					dependent.includeList.removeAll("???");
					dependent.includeList.addIfNew(globalName);
				}
			}
			this.importingRoots.clear();
		}
	}


}

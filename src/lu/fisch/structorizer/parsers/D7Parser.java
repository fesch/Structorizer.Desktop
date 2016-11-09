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

package lu.fisch.structorizer.parsers;

/*
 ******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    Class to parse a Pascal file.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Bob Fisch       2008.01.06      First Issue
 *      Bob Fisch       2008.05.02      Added filter for (* ... *) comment filtering
 *      Kay Gürtzig     2015.10.20      New setting stepFor (KGU#3, to be made configurable!)
 *      Kay Gürtzig     2016-03-20      New settings preForIn and postForIn added (KGU#61, #84/#135)
 *      Kay Gürtzig     2016-03-25      KGU#163: New static method getAllPropeties() added for Analyser
 *                                      KGU#165: New option ignoreCase
 *      Kay Gürtzig     2016-04-04      KGU#165: Default for ignoreCase changed to true
 *      Kay Gürtzig     2016-04-30      Issue #182 (KGU#191): More information on error exit in parse()
 *      Kay Gürtzig     2016-05-02      Issue #184 / Enh. #10: Flaws in FOR loop import (KGU#192)
 *      Kay Gürtzig     2016-05-04      KGU#194: Bugfix - parse() now with Charset argument
 *      Kay Gürtzig     2016-05-05/09   Issue #185: Import now copes with units and multiple routines per file
 *      Kay Gürtzig     2016-07-07      Enh. #185: Identification of Calls improved on parsing
 *      Kay Gürtzig     2016-09-25      Method getPropertyMap() added for more generic keyword handling (Enh. #253)
 *      Bob Fisch       2016-11-03      Bugfix #278 (NoSuchMethodError) in loadFromIni()
 *      Kay Gürtzig     2016-11-06      Bugfix #279: New methods keywordSet(), getKeywordOrDefault() etc.
 *      Kay Gürtzig     2016-11-08      Bugfix #281/#282 in method setKeyword() (Java 1.8 method HashMap.replace())
 *
 ******************************************************************************************************
 *
 *      Comment:		While setting up this class, I had a deep look at the following package:
 *
 *     Licensed Material - Property of Matthew Hawkins (hawkini@myrealbox.com)
 *     GOLDParser - code ported from VB - Author Devin Cook. All rights reserved.
 *     Modifications to this code are allowed as it is a helper class to use the engine.
 *     Source File:    AppleSample.java<br>
 *     Author:         Matthew Hawkins<br>
 *     Description:    A Sample class, takes in a set of files and runs the GOLDParser
 *					   engine on them.<br>
 *
 ******************************************************************************************************/

import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import goldengine.java.*;
import lu.fisch.utils.*;
import lu.fisch.structorizer.io.*;
import lu.fisch.structorizer.elements.*;

import com.stevesoft.pat.*;  //http://www.javaregex.com/

/**
 * Class to parse a Pascal (or Delphi 7, more precisely) file, generating a Structogram.
 * Also (ab)used for some fundamental settings of Structorizer - which will have to be
 * decomposed as soon as more languages (e.g. OBERON) are elected for import to Structorizer.
 * @author Bob Fisch
 */
public class D7Parser implements GPMessageConstants
{
	// START KGU#165 2016-03-25: Once and for all: It should be a transparent choice, ...
	/**
	 * whether or not the keywords are to be handled in a case-independent way
	 */
	public static boolean ignoreCase = true;
	// END KGU#165 2016-03-25
	
	// START KGU#288 2016-11-06: Issue #279: Access limited to private, compensated by new methods
	//public static final HashMap<String, String> keywordMap = new LinkedHashMap<String, String>();
	private static final HashMap<String, String> keywordMap = new LinkedHashMap<String, String>();
	// END KGU#288 2016-11-06
	static {
		keywordMap.put("preAlt",     "");
		keywordMap.put("postAlt",    "");
		keywordMap.put("preCase",    "");
		keywordMap.put("postCase",   "");
		keywordMap.put("preFor",     "for");
		keywordMap.put("postFor",    "to");
		keywordMap.put("stepFor",    "step");
		keywordMap.put("preForIn",   "foreach");
		keywordMap.put("postForIn",  "in");
		keywordMap.put("preWhile",   "while");
		keywordMap.put("postWhile",  "");
		keywordMap.put("preRepeat",  "until");
		keywordMap.put("postRepeat", "");
		keywordMap.put("preLeave",   "leave");
		keywordMap.put("preReturn",  "return");
		keywordMap.put("preExit",    "exit");
		keywordMap.put("input",      "read");
		keywordMap.put("output",     "write");
	}
	
//	public static String preAlt = "";
//	public static String postAlt = "";
//	public static String preCase = "";
//	public static String postCase = "";
//	public static String preFor = "for ";
//	public static String postFor = "to";
//	// START KGU#3/KGU#18/KGU#23 2015-10-20
//	// TODO Must the code below (esp. DrawNSD_R) be adapted? Or isn't it used anymore?
//	public static String stepFor = " step ";	// For consistent analysis of FOR loops
//	// END KGU#3/KGU#18/KGU#23 2015-10-20;
//	// START KGU#61 2016-03-20: Enh. #84/#135 - support and distinguish FOR-IN loops
//	public static String preForIn = "for ";	// This may be equal to preFor!
//	public static String postForIn = " in ";
//	// END KGU#61 2016-03-20
//	public static String preWhile = "while ";
//	public static String postWhile = "";
//	public static String preRepeat = "until ";
//	public static String postRepeat = "";
//	public static String input = "read ";
//	public static String output = "write ";
//	
//	// START KGU#78 2015-11-27: Configurable keywords for Jump types prepared
//	public static String preLeave = "leave";
//	public static String preReturn = "return";
//	public static String preExit = "exit";
//	// END KGU#78 2015-11-27
	
	// NOTE: Don't forget to add new keywords to getAllProperties()!

	private String compiledGrammar = null;
	Root root = null;
	// START KGU#194 2016-05-08: Bugfix #185
	// We may obtain a collection of Roots (unit or program with subroutines)!
	private List<Root> subRoots = new LinkedList<Root>();
	// END KGU#194 2016-05-08
	
	public String error = new String();
	
	GOLDParser parser = null;
	
	// START KGU#194 2016-05-08: Bugfix #185 - if being a unit we must retain its name
	private String unitName = null;
	// END KGU#194 2016-05-08
	
	public D7Parser(String _compiledGrammar)
	{
		compiledGrammar = _compiledGrammar;
		// create new parser
		parser = new GOLDParser();
		parser.setTrimReductions(true);
		
		
		// load the grammar	
		try
        {
            parser.loadCompiledGrammar(_compiledGrammar);
        }
        catch(ParserException parse)
        {
            System.out.println("**PARSER ERROR**\n" + parse.toString());
            System.exit(1);
        }
		
		loadFromINI();
	}
	
	
	public String filterNonAscii(String inString) 
	{
		// Create the encoder and decoder for the character encoding
		Charset charset = Charset.forName("ASCII");
		//Charset charset = Charset.forName("ISO-8859-1");
		CharsetDecoder decoder = charset.newDecoder();
		CharsetEncoder encoder = charset.newEncoder();
		// This line is the key to removing "unmappable" characters.
		encoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
		String result = inString;
		
		try 
		{
			// Convert a string to bytes in a ByteBuffer
			ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(inString));
			
			// Convert bytes in a ByteBuffer to a character ByteBuffer and then to a string.
			CharBuffer cbuf = decoder.decode(bbuf);
			result = cbuf.toString();
		} 
		catch (CharacterCodingException cce) 
		{
			String errorMessage = "Exception during character encoding/decoding: " + cce.getMessage();
			System.out.println(errorMessage);	
		}
		
		return result;	
	}
	
	// START KGU#193 2016-05-04
	/**
	 * Parses the Pascal source code from file _textToParse and returns an equivalent
	 * structogram.
	 * Field `error' will either contain an empty string or an error message afterwards.
	 * For backward compatibility reasons, character encoding ISO-8859-1 is assumed.
	 * @param _textToParse - file name of the Pascal source.
	 * @return The composed diagram (if parsing was successful, otherwise field error will contain an error description) 
	 */
	public Root parse(String _textToParse)
	{
		return parse(_textToParse, "ISO-8859-1").get(0);
	}

	/**
	 * Parses the Pascal source code from file _textToParse, which is supposed to be encoded
	 * with the charset _encoding, and returns an equivalent structogram.
	 * Field `error' will either contain an empty string or an error message afterwards.
	 * @param _textToParse - file name of the Pascal source.
	 * @param _encoding - name of the charset to be used for decoding
	 * @return The composed diagram (if parsing was successful, otherwise field error will contain an error description) 
	 */
	public List<Root> parse(String _textToParse, String _encoding)
	// END KGU#193 2016-05-04
	{
		// create new root
		root = new Root();
		error = "";

		// prepare textfile
		try
		{
			DataInputStream in = new DataInputStream(new FileInputStream(_textToParse));
			// START KGU#193 2016-05-04
			BufferedReader br = new BufferedReader(new InputStreamReader(in, _encoding));
			// END KGU#193 2016-05-04
			String strLine;
			String pasCode = new String();
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   
			{
				// add no ending because of comment filter
				pasCode += strLine+"\u2190";
				//pasCode+=strLine+"\n";
			}
			//Close the input stream
			in.close();

			// filter out comments (KGU: Why? The GOLDParser can do it itself)
			Regex r = new Regex("(.*?)[(][*](.*?)[*][)](.*?)","$1$3"); 
			pasCode=r.replaceAll(pasCode);
			r = new Regex("(.*?)[{](.*?)[}](.*?)","$1$3"); 
			pasCode = r.replaceAll(pasCode);

			// START KGU#195 2016-05-04: Issue #185 - Workaround for mere subroutines
			pasCode = embedSubroutineDeclaration(pasCode);
			// END KGU#195 2016-05-04

			// reset correct endings
			r = new Regex("(.*?)[\u2190](.*?)","$1\n$2"); 
			pasCode = r.replaceAll(pasCode);

			//System.out.println(pasCode);

			// trim and save as new file
			OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(_textToParse+".structorizer"), "ISO-8859-1");
			ow.write(filterNonAscii(pasCode.trim()+"\n"));
			//System.out.println("==> "+filterNonAscii(pasCode.trim()+"\n"));
			ow.close();
		}
		catch (Exception e) 
		{
			System.out.println(e.getMessage());
		}

		// try to load the compiled grammar and
		// load the text to parse
		try
		{
			//parser.loadCompiledGrammar(compiledGrammar);
			parser.openFile(_textToParse+".structorizer");
		}
		catch(ParserException parse)
		{
			System.out.println("**PARSER ERROR**\n" + parse.toString());
			System.exit(1);
		}

		// init some vars
		boolean done = false;
		int response = -1;
		// START KGU#191 2016-04-30: Issue #182 (insufficient information for error detection)
		// Rolling buffer for processed tokens as retrospective context for error messages
		// Number of empty strings = number of retained context lines 
		String[] context = {"", "", "", "", "", "", "", "", "", ""};
		int contextLine = 0;
		// END KGU#191 2016-04-30

		// start parsing
		while (!done)
		{
			try
			{
				//System.out.println("- Parse: before");
				response = parser.parse();
				//System.out.println("- Parse: after");
			}
			catch(ParserException parse)
			{
				System.err.println("**PARSER ERROR**\n" + parse.toString());
				System.exit(1);
			}

			//System.out.println("===> "+response);
			Token theTok = null;
			switch(response)
			{
			case gpMsgLexicalError:
				//System.err.println("gpMsgLexicalError");
				// START KGU#191 2016-04-30: Issue #182 
				theTok = parser.currentToken();
				error = ("Unexpected character: " + (String)theTok.getData()+" at line "+parser.currentLineNumber());
				// END KGU#191 2016-04-30
				parser.popInputToken();
				done = true;
				break;

			case gpMsgSyntaxError:
				theTok = parser.currentToken();
				error = ("Token not expected: " + (String)theTok.getData()+" at line "+parser.currentLineNumber());

				//System.out.println("gpMsgSyntaxError");
				done = true;
				break;

			case gpMsgReduction:
				//System.out.println("gpMsgReduction");
				//Reduction myRed = parser.currentReduction();
				//System.out.println(myRed.getParentRule().getText());
				break;

			case gpMsgAccept:
				//System.out.println("Source OK");
				DrawNSD(parser.currentReduction());
				done = true;
				break;

			case gpMsgTokenRead:
				//System.out.println("gpMsgTokenRead");
				Token myTok = parser.currentToken();
				//System.out.println((String)myTok.getData());
				while (parser.currentLineNumber() > contextLine)
				{
					context[(++contextLine) % context.length] = "";
				}
				context[contextLine % context.length] += ((String)myTok.getData() + " ");
				break;

			case gpMsgNotLoadedError:
				System.err.println("gpMsgNotLoadedError");
				done = true;
				break;

			case gpMsgCommentError:
				System.err.println("gpMsgCommentError");
				done = true;
				break;

			case gpMsgInternalError:
				System.err.println("gpMsgInternalError");
				done = true;
				break;

			default:
				//System.out.println("default");
				break;

			}
		}
		//System.out.println("---- done ----");

		// START KGU#191 2016-04-30: Issue #182 - In error case append the context 
		if (!error.isEmpty())
		{
			error += "\nPreceding source context:";
			contextLine -= context.length;
			for (int line = 0; line < context.length; line++)
			{
				if (++contextLine >= 0)
				{
					error += "\n" + contextLine + ":   " + context[contextLine % context.length];
				}
			}
		}
		// END KGU#191 2016-04-30

		try
		{
			parser.closeFile();
		}
		catch(ParserException parsex)
		{
			System.err.println("**PARSER ERROR**\n" + parsex.toString());
			System.exit(1);
		}

		//remove the temporary file
		(new File(_textToParse+".structorizer")).delete();

		// START KGU#194 2016-07-07: Enh. #185/#188 - Try to convert calls to Call elements
		StringList signatures = new StringList();
		for (Root subroutine : subRoots)
		{
			if (!subroutine.isProgram)
			{
				signatures.add(subroutine.getMethodName() + "#" + subroutine.getParameterNames().count());
			}
		}
		// END KGU#194 2016-07-07
		
		// START KGU#194 2016-05-08: Bugfix #185 - face an empty program or unit vessel
		//return root;
		if (subRoots.isEmpty() || root.children.getSize() > 0)
		{
			subRoots.add(0, root);
		}
		// START KGU#194 2016-07-07: Enh. #185/#188 - Try to convert calls to Call elements
		for (Root aRoot : subRoots)
		{
			aRoot.convertToCalls(signatures);
		}
		// END KGU#194 2016-07-07
		return subRoots;
		// END KGU#194 2016-05-08
	}
	
	// START KGU#195 2016-05-04: Issue #185 - Workaround for mere subroutines
	private String embedSubroutineDeclaration(String _pasCode)
	{
		// Find the first non-empty line where line ends are encoded as "\u2190"
		boolean headerFound = false;
		int pos = -1;
		int lineEnd = -1;
		while (!headerFound && (lineEnd = _pasCode.indexOf("\u2190", pos+1)) >= 0)
		{
			String line = _pasCode.substring(pos+1, lineEnd).toLowerCase();
			pos = lineEnd;
			// If the file contains a program or unit then we leave it as is
			// for the moment...
			if (line.startsWith("program") || line.startsWith("unit"))
			{
				headerFound = true;
			}
			else if (line.startsWith("function") ||
					 line.startsWith("procedure"))
			{
				// embed the declaration in a dummy program definition as
				// workaround
				headerFound = true;
				_pasCode = "program dummy;" + "\u2190"
						+ _pasCode + "\u2190"
						+ "begin" + "\u2190"
						+ "end." + "\u2190";
			}
		}
		return _pasCode;
	}
	// END KGU#195 2016-05-04

	private void DrawNSD(Reduction _reduction)
	{
		root.isProgram=true;
		// START KGU#194 2016-05-08: Bugfix #185
		unitName = null;
		// END KGU#194 2016-05-08
		DrawNSD_R(_reduction, root.children);
	}
	
	private void DrawNSD_R(Reduction _reduction, Subqueue _parentNode)
	{
		String content = new String();
	
		if (_reduction.getTokenCount()>0)
		{
			String ruleName = _reduction.getParentRule().name();
			//System.out.println(ruleName);
			if ( 
				ruleName.equals("<RefId>")
				||
				ruleName.equals("<CallStmt>")
				||
				ruleName.equals("<Designator>")
				||
				ruleName.equals("<AssignmentStmt>")
			   )
			{
				content=new String();
				content=getContent_R(_reduction,content);
				//System.out.println(ruleName + ": " + content);
				_parentNode.addElement(new Instruction(updateContent(content)));
			}
			else if (
					 ruleName.equals("<UsesClause>")
					 ||
					 ruleName.equals("<VarSection>")
					 ||
					 ruleName.equals("<ConstSection>")
					 // START KGU#194 2016-05-08: Bugfix #185
					 // UNIT Interface section can be ignored, all contained routines
					 // must be converted from the implementation section
					 ||
					 ruleName.equals("<InterfaceSection>")
					 ||
					 ruleName.equals("<InitSection>")
					 // END KGU#194 2016-05-08
					 )
			{
			}
			// START KGU#194 2016-05-08: Bugfix #185 - we must handle unit headers
			else if (
					ruleName.equals("<UnitHeader>")
					 )
			{
				unitName = getContent_R((Reduction) _reduction.getToken(1).getData(), "");
			}
			else if (
					ruleName.equals("<ProcedureDecl>")
					||
					ruleName.equals("<FunctionDecl>")
					||
					ruleName.equals("<MethodDecl>")
					)
			{
				Root prevRoot = root;	// Push the original root
				root = new Root();	// Prepare a new root for the subroutine
				subRoots.add(root);
				for (int i=0; i < _reduction.getTokenCount(); i++)
				{
					if (_reduction.getToken(i).getKind() == SymbolTypeConstants.symbolTypeNonterminal)
					{
						DrawNSD_R((Reduction) _reduction.getToken(i).getData(), root.children);
					}
				}
				// Restore the original root
				root = prevRoot;
			}
			// END KGU#194 2016-05-08
			else if (
					 ruleName.equals("<ProgHeader>")
					 )
			{
				content=new String();
				content=getContent_R((Reduction) _reduction.getToken(1).getData(),content);
				root.setText(updateContent(content));
			}
			else if (
					 ruleName.equals("<ProcHeading>")
					 )
			{
				content = new String();
				content = getContent_R((Reduction) _reduction.getToken(1).getData(),content);
				
				Reduction secReduc = (Reduction) _reduction.getToken(2).getData();
				if (secReduc.getTokenCount()!=0)
				{
					content = getContent_R(secReduc,content);
				}
				
				content = BString.replaceInsensitive(content,";","; ");
				content = BString.replaceInsensitive(content,";  ","; ");
				root.setText(updateContent(content));
				root.isProgram=false;
				// START KGU#194 2016-05-08: Bugfix #185 - be aware of unit context
				if (unitName != null)
				{
					root.setComment("(UNIT " + unitName + ")");
				}
				// END KGU#194 2016-05-08
			}
			else if (
					 ruleName.equals("<FuncHeading>")
					 )
			{
				content = new String();
				content = getContent_R((Reduction) _reduction.getToken(1).getData(),content);
				
				Reduction secReduc = (Reduction) _reduction.getToken(2).getData();
				if (secReduc.getTokenCount()!=0)
				{
					content = getContent_R(secReduc,content);
				}
				
				secReduc = (Reduction) _reduction.getToken(4).getData();
				if (secReduc.getTokenCount()!=0)
				{
					content += ": ";
					content = getContent_R(secReduc,content);
				}
				
				content = BString.replaceInsensitive(content,";","; ");
				content = BString.replaceInsensitive(content,";  ","; ");
				root.setText(updateContent(content));
				root.isProgram = false;
				// START KGU#194 2016-05-08: Bugfix #185 - be aware of unit context
				if (unitName != null)
				{
					root.setComment("(UNIT " + unitName + ")");
				}
				// END KGU#194 2016-05-08
			}
			else if (
					 ruleName.equals("<WhileStatement>")
					 )
			{
				content = new String();
				content = getContent_R((Reduction) _reduction.getToken(1).getData(),content);
				While ele = new While(keywordMap.get("preWhile")+updateContent(content)+keywordMap.get("postWhile"));
				_parentNode.addElement(ele);
				
				Reduction secReduc = (Reduction) _reduction.getToken(3).getData();
				DrawNSD_R(secReduc,ele.q);
			}
			else if (
					 ruleName.equals("<RepeatStatement>")
					 )
			{
				content = new String();
				content = getContent_R((Reduction) _reduction.getToken(3).getData(),content);
				Repeat ele = new Repeat(keywordMap.get("preRepeat")+updateContent(content)+keywordMap.get("postRepeat"));
				_parentNode.addElement(ele);
				
				Reduction secReduc = (Reduction) _reduction.getToken(1).getData();
				DrawNSD_R(secReduc,ele.q);
			}
			else if (
					 ruleName.equals("<ForStatement>")
					 )
			{
				content = new String();
				content = getContent_R((Reduction) _reduction.getToken(1).getData(),content);
				content += ":=";
				content = getContent_R((Reduction) _reduction.getToken(3).getData(),content);
				content += " ";
				content += keywordMap.get("postFor");
				content += " ";
				content = getContent_R((Reduction) _reduction.getToken(5).getData(),content);
				// START KGU#3 2016-05-02: Enh. #10 Token 4 contains the information whether it's to or downto
				if (getContent_R((Reduction) _reduction.getToken(4).getData(), "").equals("downto"))
				{
					content += " " + keywordMap.get("stepFor") + " -1";
				}
				// END KGU#3 2016-05-02
				// START KGU 2016-05-02: This worked only if preFor ended with space
				//For ele = new For(preFor+updateContent(content));
				For ele = new For(keywordMap.get("preFor").trim() + " " + updateContent(content));
				// END KGU 2016-05-02
				_parentNode.addElement(ele);
				
				// Get and convert the body
				Reduction secReduc = (Reduction) _reduction.getToken(7).getData();
				DrawNSD_R(secReduc,ele.q);
			}
			else if (
					 ruleName.equals("<IfStatement>")
					 )
			{
				content = new String();
				content = getContent_R((Reduction) _reduction.getToken(1).getData(),content);
				
				Alternative ele = new Alternative(keywordMap.get("preAlt") + updateContent(content)+keywordMap.get("postAlt"));
				_parentNode.addElement(ele);
				
				Reduction secReduc = (Reduction) _reduction.getToken(3).getData();
				DrawNSD_R(secReduc,ele.qTrue);
				if(_reduction.getTokenCount()>=5)
				{
					secReduc = (Reduction) _reduction.getToken(5).getData();
					DrawNSD_R(secReduc,ele.qFalse);
				}
			}
			else if (
					 ruleName.equals("<CaseSelector>")
					 )
			{
				content = new String();
				content = getContent_R((Reduction) _reduction.getToken(0).getData(),content);
				
				// sich am parent (CASE) dat nächst fräit Element
				boolean found = false;
				for (int i=0; i<((Case) _parentNode.parent).getText().count(); i++)
				{
					if (((Case) _parentNode.parent).getText().get(i).equals("??") && found==false)
					{
						((Case) _parentNode.parent).getText().set(i,content);
						found=true;
						
						Reduction secReduc = (Reduction) _reduction.getToken(2).getData();
						DrawNSD_R(secReduc,(Subqueue) ((Case) _parentNode.parent).qs.get(i-1));
					}
				}

				/*
				 content:='';
				 getContent_R(R.Tokens[0].Reduction,content);
				 
				 // sich am parent (CASE) dat nächst fräit Element
				 found:=false;
				 for i:=0 to (parentnode.parent as BCase).text.Count-1 do
				 begin
				 if((parentnode.parent as BCase).text[i]='??')and(found=false) then
				 begin
				 (parentnode.parent as BCase).text[i]:=content;
				 found:=true;
				 DrawNSD_R(R.Tokens[2].Reduction,((parentnode.parent as BCase).qs[i-1] as BSubqueue));
				 end;
				 end;
				*/
			}
			else if (
					 ruleName.equals("<CaseStatement>")
					 )
			{
				content = new String();
				content = keywordMap.get("preCase")+getContent_R((Reduction) _reduction.getToken(1).getData(),content)+keywordMap.get("postCase");
				// am content steet elo hei den "test" dran
				
				// Wéivill Elementer sinn am CASE dran?
				Reduction sr = (Reduction) _reduction.getToken(3).getData();
				int j = 0;
				//System.out.println(sr.getParentRule().getText());  // <<<<<<<
				while (sr.getParentRule().name().equals("<CaseList>"))
				{
					j++;
					content += "\n??";
					if (sr.getTokenCount()>=1)
					{
						sr = (Reduction) sr.getToken(0).getData();
					}
					else
					{
						break;
					}
				}
				
				if ( j>0) 
				{
					j++;
					content += "\nelse";
				}

				Case ele = new Case(updateContent(content));
				ele.setText(updateContent(content));
				_parentNode.addElement(ele);

				// déi eenzel Elementer siche goen
				Reduction secReduc = (Reduction) _reduction.getToken(3).getData();
				DrawNSD_R(secReduc,(Subqueue) ele.qs.get(0));
				// den "otherwise"
				secReduc = (Reduction) _reduction.getToken(4).getData();
				DrawNSD_R(secReduc,(Subqueue) ele.qs.get(j-1));
				
				// cut off else, if possible
				if (((Subqueue) ele.qs.get(j-1)).getSize()==0)
				{
					ele.getText().set(ele.getText().count()-1,"%");
				}

				/*
				 content:='';
				 getContent_R(R.Tokens[1].Reduction,content);
				 // am content steet elo hei den "test" dran
				 
				 // Wéivill Elementer sinn am CASE dran?
				 sr:=r.Tokens[3].Reduction;
				 j:=0;
				 while(sr.ParentRule.Name='<CaseList>') do
				 begin
				   j:=j+1;
				   content:=content+#13+'??';
				   if(sr.TokenCount>=1) then sr:=sr.Tokens[0].Reduction
				   else break;
				 end;
				 
				 if(j>0) then
				 begin
				 inc(j);
				 content:=content+#13+'else';
				 
				 ele:=ParentNode.AddCase(updateContent(content));
				 (ele as BCase).setTextLines(content);
				 
				 // déi enzel Elementer siche goen
				 DrawNSD_R(R.Tokens[3].Reduction,((ele as BCase).qs[0] as BSubqueue));
				 // den "otherwise"
				 DrawNSD_R(R.Tokens[4].Reduction,((ele as BCase).qs[j-1] as BSubqueue));
				 end;
				 */
			}
			else
			{
				if (_reduction.getTokenCount()>0)
				{
					for(int i=0; i<_reduction.getTokenCount(); i++)
					{
						if (_reduction.getToken(i).getKind()==SymbolTypeConstants.symbolTypeNonterminal)
						{
							DrawNSD_R((Reduction) _reduction.getToken(i).getData(),_parentNode);
						}
					}
				}
			}
			
		}
	}
	
	private String updateContent(String _content)
	{
		/* Fucking Regex class -> No need to use it, becaue Java implements a ***working*** version!
		Regex r;
	
		r = new Regex(BString.breakup("write")+"[((](.*?)[))]",output+" $1"); _content=r.replaceAll(_content);
		r = new Regex(BString.breakup("writeln")+"[((](.*?)[))]",output+" $1"); _content=r.replaceAll(_content);
		r = new Regex(BString.breakup("writeln")+"(.*?)",output+" $1"); _content=r.replaceAll(_content);
		r = new Regex(BString.breakup("write")+"(.*?)",output+" $1"); _content=r.replaceAll(_content);
		r = new Regex(BString.breakup("read")+"[((](.*?)[))]",input+" $1"); _content=r.replaceAll(_content);
		r = new Regex(BString.breakup("readln")+"[((](.*?)[))]",input+" $1"); _content=r.replaceAll(_content);
		r = new Regex(BString.breakup("readln")+"(.*?)",input+" $1"); _content=r.replaceAll(_content);
		r = new Regex(BString.breakup("read")+"(.*?)",input+" $1"); _content=r.replaceAll(_content);*/

		String output = keywordMap.get("output");
		String input = keywordMap.get("input");
		_content = _content.replaceAll(BString.breakup("write")+"[((](.*?)[))]", output+" $1");
		_content = _content.replaceAll(BString.breakup("writeln")+"[((](.*?)[))]", output+" $1");
		_content = _content.replaceAll(BString.breakup("writeln")+"(.*?)", output+" $1");
		_content = _content.replaceAll(BString.breakup("write")+"(.*?)", output+" $1");
		_content = _content.replaceAll(BString.breakup("read")+"[((](.*?)[))]", input+" $1");
		_content = _content.replaceAll(BString.breakup("readln")+"[((](.*?)[))]", input+" $1");
		_content = _content.replaceAll(BString.breakup("readln")+"(.*?)", input+" $1");
		_content = _content.replaceAll(BString.breakup("read")+"(.*?)", input+" $1");
		
		//System.out.println(_content);
		
		/*
		 _content:=ReplaceEntities(_content);
		*/
		
		//_content = BString.replace(_content, ":="," \u2190 ");
		_content = BString.replace(_content, ":="," <- ");

		return _content.trim();
	}
	
	private String getContent_R(Reduction _reduction, String _content)
	{
//		if (_reduction.getTokenCount()>0)
//		{
			for(int i=0;i<_reduction.getTokenCount();i++)
			{
				switch (_reduction.getToken(i).getKind()) 
				{
					case SymbolTypeConstants.symbolTypeNonterminal:
						_content = getContent_R((Reduction) _reduction.getToken(i).getData(), _content);	
						break;
					case SymbolTypeConstants.symbolTypeTerminal:
						{
							String tokenData = (String) _reduction.getToken(i).getData();
							// START KGU 2016-05-08: Avoid keyword concatenation
							boolean tokenIsId = !tokenData.isEmpty() && Character.isJavaIdentifierStart(tokenData.charAt(0));
							// END KGU 2016-05-08
							if (tokenData.trim().equalsIgnoreCase("mod") ||
									// START KGU#192 2016-05-02: There are more operators to be considered...
									tokenData.trim().equalsIgnoreCase("shl") ||
									tokenData.trim().equalsIgnoreCase("shr") ||
									// END KGU#192 2016-05-02
									tokenData.trim().equalsIgnoreCase("div"))
							{
								_content += " " + tokenData + " ";
								// START KGU 2016-05-08: Avoid keyword concatenation
								tokenIsId = false;
								// END KGU 2016-05-08
							}
							// START KGU 2016-05-08: Avoid keyword concatenation
							else if (
									tokenIsId
									&&
									!_content.isEmpty()
									&&
									Character.isJavaIdentifierPart(_content.charAt(_content.length()-1))
									)
							{
								_content += " " + tokenData;
							}
							// END KGU 2016-05-08
							else
							{
								_content += tokenData;
							}
						}
						break;
					default:
						break;
				}
			}
//		}
//		else
//		{
//			// ?
//			// _content:=_content+trim(R.ParentRule.Text)
//		}
		
		_content = BString.replaceInsensitive(_content,")and(",") and (");
		_content = BString.replaceInsensitive(_content,")or(",") or (");
		
		return _content;
	}
	
	/************************
	 * static things
	 ************************/
	
	public static void loadFromINI()
	{
		final HashMap<String, String> defaultKeys = new HashMap<String, String>();
		defaultKeys.put("ParserPreFor", "pour ");
		defaultKeys.put("ParserPostFor", "\u00E0");
		defaultKeys.put("ParserStepFor", ", pas = ");
		defaultKeys.put("ParserPreForIn", "pour ");
		defaultKeys.put("ParserPostForIn", " en ");
		defaultKeys.put("ParserPreWhile", "tant que ");
		defaultKeys.put("ParserPreRepeat", "jusqu'\u00E0 ");
		defaultKeys.put("ParserPreLeave", "leave");
		defaultKeys.put("ParserPreReturn", "return");
		defaultKeys.put("ParserPreExit", "exit");
		defaultKeys.put("ParserInput", "lire ");
		defaultKeys.put("ParserOutput", "\u00E9crire");
		
		try
		{
			Ini ini = Ini.getInstance();
			ini.load();

			// elements
			// START KGU#258 2016-09-25: Code redesign for enh. #253
//			preAlt = ini.getProperty("ParserPreAlt","");
//			postAlt = ini.getProperty("ParserPostAlt","");
//			preCase = ini.getProperty("ParserPreCase","");
//			postCase = ini.getProperty("ParserPostCase","");
//			preFor = ini.getProperty("ParserPreFor","pour ");
//			postFor = ini.getProperty("ParserPostFor","\u00E0");
//			// START KGU#3 2015-11-08: Enhancement #10
//			stepFor = ini.getProperty("ParserStepFor", ", pas = ");
//			// END KGU#3 2015-11-08
//			// START KGU#61 2016-03-20: Enh. #84/#135 - support and distinguish FOR-IN loops
//			preForIn = ini.getProperty("ParserPreForIn","pour ");
//			postForIn = ini.getProperty("ParserPostForIn"," en ");
//			// END KGU#61 2016-03-20
//			preWhile = ini.getProperty("ParserPreWhile","tant que ");
//			postWhile = ini.getProperty("ParserPostWhile","");
//			preRepeat = ini.getProperty("ParserPreRepeat","jusqu'\u00E0 ");
//			postRepeat = ini.getProperty("ParserPostRepeat","");
//    		// START KGU#78 2016-03-25: Enh. #23 - Jump configurability introduced
//			preLeave = ini.getProperty("ParserPreLeave", "leave");
//			preReturn = ini.getProperty("ParserPreReturn", "return");
//			preExit = ini.getProperty("ParserPreExit", "exit");
//    		// END KGU#78 2016-03-25
//			input = ini.getProperty("ParserInput","lire");
//			output = ini.getProperty("ParserOutput","\u00E9crire");
			for (String key: keywordMap.keySet())
			{
				String propertyName = "Parser" + Character.toUpperCase(key.charAt(0)) + key.substring(1);
                                if(defaultKeys.containsKey(propertyName))
                                {
                                    keywordMap.put(key, ini.getProperty(propertyName, defaultKeys.get(propertyName)));
                                }
                                else
                                {
                                    keywordMap.put(key, ini.getProperty(propertyName, ""));
                                }
			}
			
			// END KGU#258 2016-09-25
			// START KGU#165 2016-03-25: Enhancement configurable case awareness
			ignoreCase = ini.getProperty("ParserIgnoreCase", "true").equalsIgnoreCase("true");
			// END KGU#3 2016-03-25
			
		}
		catch (Exception e) 
		{
			System.out.println(e);
		}
	}
	
	public static void saveToINI()
	{
		try
		{
			Ini ini = Ini.getInstance();
			ini.load();			// elements
			// START KGU#258 2016-09-25: Code redesign for enh. #253			
//			ini.setProperty("ParserPreAlt",preAlt);
//			ini.setProperty("ParserPostAlt",postAlt);
//			ini.setProperty("ParserPreCase",preCase);
//			ini.setProperty("ParserPostCase",postCase);
//			ini.setProperty("ParserPreFor",preFor);
//			ini.setProperty("ParserPostFor",postFor);
//			// START KGU#3 2015-11-08: Enhancement #10
//			ini.setProperty("ParserStepFor",stepFor);
//			// END KGU#3 2015-11-08
//			// START KGU#61 2016-03-20: Enh. #84/#135 - support and distinguish FOR-IN loops
//			ini.setProperty("ParserPreForIn",preForIn);
//			ini.setProperty("ParserPostForIn",postForIn);
//			// END KGU#61 2016-03-20
//			ini.setProperty("ParserPreWhile",preWhile);
//			ini.setProperty("ParserPostWhile",postWhile);
//			ini.setProperty("ParserPreRepeat",preRepeat);
//			ini.setProperty("ParserPostRepeat",postRepeat);
//    		// START KGU#78 2016-03-25: Enh. #23 - Jump configurability introduced
//			ini.setProperty("ParserPreLeave", preLeave);
//			ini.setProperty("ParserPreReturn", preReturn);
//			ini.setProperty("ParserPreExit", preExit);
//    		// END KGU#78 2016-03-25
//			
//			ini.setProperty("ParserInput",input);
//			ini.setProperty("ParserOutput",output);
//			// START KGU#165 2016-03-25: Enhancement 
//			ini.setProperty("ParserIgnoreCase",Boolean.toString(ignoreCase));
//			// END KGU#3 2016-03-25
			for (Map.Entry<String, String> entry: getPropertyMap(true).entrySet())
			{
				String propertyName = "Parser" + Character.toUpperCase(entry.getKey().charAt(0)) + entry.getKey().substring(1);
				ini.setProperty(propertyName, entry.getValue());
			}
			// END KGU#258 2016-09-25
			
			ini.save();
		}
		catch (Exception e) 
		{
			System.out.println(e);
		}
	}
	
	// START KGU#163 2016-03-25: For syntax analysis purposes
	/**
	 * Returns the complete set of configurable parser keywords for Elements 
	 * @return array of current keyword strings
	 */
	public static String[] getAllProperties()
	{
		String[] props = new String[]{};
		return keywordMap.values().toArray(props);
	}
	// END KGU#163 2016-03-25
	
	// START KGU#258 2016-09-25: Enh. #253 (temporary workaround for the needed Hashmap)
	/**
	 * Returns a Hashmap mapping parser preference labels like "preAlt" to the
	 * configured parser preference keywords.
	 * @param includeAuxiliary - whether or not non-keyword settings (like "ignoreCase") are to be included
	 * @return the hash table with the current settings
	 */
	public static final HashMap<String, String> getPropertyMap(boolean includeAuxiliary)
	{
		HashMap<String, String> keywords = keywordMap;
		if (includeAuxiliary)
		{
			keywords = new HashMap<String,String>(keywordMap);
			// The following information may be important for a correct search
			keywords.put("ignoreCase",  Boolean.toString(ignoreCase));
		}
		return keywords;
	}
	// END KGU#258 2016-09-25
	
	// START KGU#288 2016-11-06: New methods to facilitate bugfix #278, #279
	/**
	 * Returns the set of the parser preference names
	 * @return
	 */
	public static Set<String> keywordSet()
	{
		return keywordMap.keySet();
	}
	
	/**
	 * Returns the cached keyword for parser preference _key or null
	 * @param _key - the name of the requested parser preference
	 * @return the cached keyword or null
	 */
	public static String getKeyword(String _key)
	{
		return keywordMap.get(_key);
	}
	
	/**
	 * Returns the cached keyword for parser preference _key or the given _defaultVal if no
	 * entry or only an empty entry is found for _key.
	 * @param _key - the name of the requested parser preference
	 * @param _defaultVal - a default keyword to be returned if there is no non-empty cached value
	 * @return the cached or default keyword
	 */
	public static String getKeywordOrDefault(String _key, String _defaultVal)
	{
		// This method circumvents the use of the Java 8 method:
		//return keywordMap.getOrDefault(_key, _defaultVal);
		String keyword = keywordMap.get(_key);
		if (keyword == null || keyword.isEmpty()) {
			keyword = _defaultVal;
		}
		return keyword;
	}
	
	/**
	 * Replaces the cached parser preference _key with the new keyword _keyword for this session.
	 * Note:
	 * 1. This does NOT influence the Ini file!
	 * 2. Only for existing keys a new mapping may be set 
	 * @param _key - name of the parser preference
	 * @param _keyword - new value of the parser preference or null
	 */
	public static void setKeyword(String _key, String _keyword)
	{
		if (_keyword == null) {
			_keyword = "";
		}
		// Bugfix #281/#282
                if (keywordMap.containsKey(_key)) {
			keywordMap.put(_key, _keyword);
		}
	}
	// END KGU#288 2016-11-06

}

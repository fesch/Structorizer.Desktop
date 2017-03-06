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

/******************************************************************************************************
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
 *      Kay Gürtzig     2017-01-06      Issue #327: French default parser keywords replaced by English ones
 *      Kay Gürtzig     2017-03-04      Enh. #354: Inheritance to CodeParser introduced, Structorizer keyword
 *                                      configuration moved to superclass CodeParser.
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
 ******************************************************************************************************///

import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.LinkedList;
import java.util.List;

import goldengine.java.*;
import lu.fisch.utils.*;
import lu.fisch.structorizer.elements.*;

import com.stevesoft.pat.*;  //http://www.javaregex.com/

/**
 * Class to parse a Pascal (or Delphi 7, more precisely) file, generating a Structogram.
 * Also (ab)used for some fundamental settings of Structorizer - which will have to be
 * decomposed as soon as more languages (e.g. OBERON) are elected for import to Structorizer.
 * @author Bob Fisch
 */
// START KGU#354 2017-03-04: Enh. 354 - inheritance changed
//public class D7Parser implements GPMessageConstants
public class D7Parser extends CodeParser implements GPMessageConstants
// END KGU#354 2017-03-04
{
	//@Override
	public String getDialogTitle() {
		return "Pascal";
	}

	//@Override
	protected String getFileDescription() {
		return "Pascal Source Files";
	}

	//@Override
	protected String[] getFileExtensions() {
		final String[] exts = { "pas", "dpr", "lpr" };
		return exts;
	}

	private final String compiledGrammar = "D7Grammar.cgt";
	
	// START KGU#354 2017-03-04: Now inherited from CodeParser
	//Root root = null;
	// START KGU#194 2016-05-08: Bugfix #185
	// We may obtain a collection of Roots (unit or program with subroutines)!
	//private List<Root> subRoots = new LinkedList<Root>();
	// END KGU#194 2016-05-08
	//
	//public String error = new String();
	// END KGU#354 2017-03-04
	
	GOLDParser parser = null;
	
	// START KGU#194 2016-05-08: Bugfix #185 - if being a unit we must retain its name
	private String unitName = null;
	// END KGU#194 2016-05-08
	
	// START KGU#354 2017-03-04: Enh. #354 - signature changed, grammar now hard-coded
	//public D7Parser(String _compiledGrammar)
	public D7Parser()
	// END KGU#354 2017-03-04
	{
		//compiledGrammar = _compiledGrammar;
		// create new parser
		parser = new GOLDParser();
		parser.setTrimReductions(true);
		
		
		// load the grammar	
		try
        {
            parser.loadCompiledGrammar(compiledGrammar);
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
	 * with the charset _encoding, and returns a list of structograms - one for each function
	 * or program contained in the source file.
	 * Field `error' will either contain an empty string or an error message afterwards.
	 * @param _textToParse - file name of the Pascal source.
	 * @param _encoding - name of the charset to be used for decoding
	 * @return A list containing composed diagrams (if successful, otherwise field error will contain an error description) 
	 */
	public List<Root> parse(String _textToParse, String _encoding)
	// END KGU#193 2016-05-04
	{
		// create new root
		root = new Root();
		error = "";

		// prepare textfile
		// START KGU#354 2017-03-03: Enh. #354 - delegated to submethod
		prepareTextfile(_textToParse, _encoding);
		// END KGU#354 2017-03-03

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
				buildNSD(parser.currentReduction());
				done = true;
				break;

			case gpMsgTokenRead:
				//System.out.println("gpMsgTokenRead");
				Token myTok = parser.currentToken();
				//System.out.println((String)myTok.getData());
				// START KGU#191 2016-04-30: Issue #182 (insufficient information for error detection)
				while (parser.currentLineNumber() > contextLine)
				{
					context[(++contextLine) % context.length] = "";
				}
				context[contextLine % context.length] += ((String)myTok.getData() + " ");
				// END KGU#191 2016-04-30
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

	// START KGU#354 2017-03-03: Enh. #354 - generalized import mechanism
	private void prepareTextfile(String _textToParse, String _encoding)
	{
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
	}
	// END KGU#354 2017-03-03
	
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

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.parsers.CodeParser#initializeBuildNSD()
	 */
	@Override
	protected void initializeBuildNSD()
	{
		// START KGU#194 2016-05-08: Bugfix #185
		unitName = null;
		// END KGU#194 2016-05-08
	}
	
	@Override
	protected void buildNSD_R(Reduction _reduction, Subqueue _parentNode)
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
				_parentNode.addElement(new Instruction(translateContent(content)));
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
						buildNSD_R((Reduction) _reduction.getToken(i).getData(), root.children);
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
				root.setText(translateContent(content));
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
				root.setText(translateContent(content));
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
				root.setText(translateContent(content));
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
				While ele = new While(getKeyword("preWhile")+translateContent(content)+getKeyword("postWhile"));
				_parentNode.addElement(ele);
				
				Reduction secReduc = (Reduction) _reduction.getToken(3).getData();
				buildNSD_R(secReduc,ele.q);
			}
			else if (
					 ruleName.equals("<RepeatStatement>")
					 )
			{
				content = new String();
				content = getContent_R((Reduction) _reduction.getToken(3).getData(),content);
				Repeat ele = new Repeat(getKeyword("preRepeat")+translateContent(content)+getKeyword("postRepeat"));
				_parentNode.addElement(ele);
				
				Reduction secReduc = (Reduction) _reduction.getToken(1).getData();
				buildNSD_R(secReduc,ele.q);
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
				content += getKeyword("postFor");
				content += " ";
				content = getContent_R((Reduction) _reduction.getToken(5).getData(),content);
				// START KGU#3 2016-05-02: Enh. #10 Token 4 contains the information whether it's to or downto
				if (getContent_R((Reduction) _reduction.getToken(4).getData(), "").equals("downto"))
				{
					content += " " + getKeyword("stepFor") + " -1";
				}
				// END KGU#3 2016-05-02
				// START KGU 2016-05-02: This worked only if preFor ended with space
				//For ele = new For(preFor+updateContent(content));
				For ele = new For(getKeyword("preFor").trim() + " " + translateContent(content));
				// END KGU 2016-05-02
				_parentNode.addElement(ele);
				
				// Get and convert the body
				Reduction secReduc = (Reduction) _reduction.getToken(7).getData();
				buildNSD_R(secReduc,ele.q);
			}
			else if (
					 ruleName.equals("<IfStatement>")
					 )
			{
				content = new String();
				content = getContent_R((Reduction) _reduction.getToken(1).getData(),content);
				
				Alternative ele = new Alternative(getKeyword("preAlt") + translateContent(content)+getKeyword("postAlt"));
				_parentNode.addElement(ele);
				
				Reduction secReduc = (Reduction) _reduction.getToken(3).getData();
				buildNSD_R(secReduc,ele.qTrue);
				if(_reduction.getTokenCount()>=5)
				{
					secReduc = (Reduction) _reduction.getToken(5).getData();
					buildNSD_R(secReduc,ele.qFalse);
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
						buildNSD_R(secReduc,(Subqueue) ((Case) _parentNode.parent).qs.get(i-1));
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
				content = getKeyword("preCase")+getContent_R((Reduction) _reduction.getToken(1).getData(),content)+getKeyword("postCase");
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

				Case ele = new Case(translateContent(content));
				ele.setText(translateContent(content));
				_parentNode.addElement(ele);

				// déi eenzel Elementer siche goen
				Reduction secReduc = (Reduction) _reduction.getToken(3).getData();
				buildNSD_R(secReduc,(Subqueue) ele.qs.get(0));
				// den "otherwise"
				secReduc = (Reduction) _reduction.getToken(4).getData();
				buildNSD_R(secReduc,(Subqueue) ele.qs.get(j-1));
				
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
							buildNSD_R((Reduction) _reduction.getToken(i).getData(),_parentNode);
						}
					}
				}
			}
			
		}
	}
	
	private String translateContent(String _content)
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

		String output = getKeyword("output");
		String input = getKeyword("input");
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
	
	protected String getContent_R(Reduction _reduction, String _content)
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
	

}

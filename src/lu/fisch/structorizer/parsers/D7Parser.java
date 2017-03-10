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
 *      Kay Gürtzig     2017.03.08      Modified for GOLDParser 5.0, also required to convert (* *) comments
 *
 ******************************************************************************************************
 *
 *      Comment:		While setting up this class (v1.0), I had a deep look at the following package:
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.regex.Matcher;

import com.creativewidgetworks.goldparser.parser.*;
import com.creativewidgetworks.goldparser.engine.*;
import com.creativewidgetworks.goldparser.engine.enums.SymbolType;

import lu.fisch.structorizer.elements.Alternative;
import lu.fisch.structorizer.elements.Case;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.elements.Jump;
import lu.fisch.structorizer.elements.Repeat;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.structorizer.elements.While;
import lu.fisch.utils.BString;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

import com.creativewidgetworks.goldparser.engine.ParserException;
import com.creativewidgetworks.goldparser.parser.GOLDParser;
import com.stevesoft.pat.Regex;

/**
 * Code import parser class of Structorizer 3.27, based on GOLDParser 5.0 for the ObjectPascal, Pascal
 * or Delphi 7 language.
 * This file contains grammar-specific constants and individual routines to build
 * structograms (Nassi-Shneiderman diagrams) from the parsing tree. 
 * @author Kay Gürtzig
 */
public class D7Parser extends CodeParser
{

 	/**
 	 * Class to parse a Pascal (or Delphi 7, more precisely) file, generating a Structogram.
 	 * @author Bob Fisch
 	 */
 	public D7Parser() {
 	}

	@Override
	public String getDialogTitle() {
		return "Pascal";
	}

	//@Override
	protected String getFileDescription() {
		return "Pascal Source Files";
	}

	//@Override
	public String[] getFileExtensions() {
		final String[] exts = { "pas", "dpr", "lpr" };
		return exts;
	}

	@Override
	protected final String getCompiledGrammar()
	{
		return "D7Grammar.cgt";
	}
	
	@Override
	protected final String getGrammarTableName()
	{
		return "ObjectPascal";
	}
	
	// START KGU#354 2017-03-04: Now inherited from CodeParser
	//Root root = null;
	// START KGU#194 2016-05-08: Bugfix #185
	// We may obtain a collection of Roots (unit or program with subroutines)!
	//private List<Root> subRoots = new LinkedList<Root>();
	// END KGU#194 2016-05-08
	//
	//public String error = new String();
	// END KGU#354 2017-03-04
	
	// START KGU#194 2016-05-08: Bugfix #185 - if being a unit we must retain its name
	private String unitName = null;
	// END KGU#194 2016-05-08
	
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
	

	// START KGU#354 2017-03-03: Enh. #354 - generalized import mechanism
	@Override
	protected File prepareTextfile(String _textToParse, String _encoding)
	{
		File interm = null;
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
//			Regex r = new Regex("(.*?)[(][*](.*?)[*][)](.*?)","$1$3"); 
//			pasCode=r.replaceAll(pasCode);
//			r = new Regex("(.*?)[{](.*?)[}](.*?)","$1$3"); 
//			pasCode = r.replaceAll(pasCode);

			// START KGU#195 2016-05-04: Issue #185 - Workaround for mere subroutines
			pasCode = embedSubroutineDeclaration(pasCode);
			// END KGU#195 2016-05-04

			// reset correct endings
			Regex r = new Regex("(.*?)[\u2190](.*?)","$1\n$2"); 
			pasCode = r.replaceAll(pasCode);
			// START KGU#354 2017-03-07: Workaround for missing second commet delimiter pair in GOLDParser 5.0
//			pasCode = pasCode.replaceAll("(.*?)(\\(\\*)(.*?)(\\*\\))(.*?)", "$1\\{$3\\}$5");
			// END KGU#354 2017-03-07

			//System.out.println(pasCode);

			// trim and save as new file
			interm = new File(_textToParse + ".structorizer");
			OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(interm), "ISO-8859-1");
			ow.write(filterNonAscii(pasCode.trim()+"\n"));
			//System.out.println("==> "+filterNonAscii(pasCode.trim()+"\n"));
			ow.close();
		}
		catch (Exception e) 
		{
			System.out.println(e.getMessage());
		}	
		return interm;
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
	
	protected void buildNSD_R(Reduction _reduction, Subqueue _parentNode)
	{
		String content = new String();
	
		if (_reduction.size()>0)
		{
			String ruleHead = _reduction.getParent().getHead().toString();
			//System.out.println(ruleHead);
			if ( 
				ruleHead.equals("<RefId>")
				||
				ruleHead.equals("<CallStmt>")
				||
				ruleHead.equals("<Designator>")
				||
				ruleHead.equals("<AssignmentStmt>")
			   )
			{
				content=new String();
				content=getContent_R(_reduction,content);
				//System.out.println(ruleHead + ": " + content);
				_parentNode.addElement(new Instruction(translateContent(content)));
			}
			else if (
					 ruleHead.equals("<UsesClause>")
					 ||
					 ruleHead.equals("<VarSection>")
					 ||
					 ruleHead.equals("<ConstSection>")
					 // START KGU#194 2016-05-08: Bugfix #185
					 // UNIT Interface section can be ignored, all contained routines
					 // must be converted from the implementation section
					 ||
					 ruleHead.equals("<InterfaceSection>")
					 ||
					 ruleHead.equals("<InitSection>")
					 // END KGU#194 2016-05-08
					 )
			{
			}
			// START KGU#194 2016-05-08: Bugfix #185 - we must handle unit headers
			else if (
					ruleHead.equals("<UnitHeader>")
					 )
			{
				unitName = getContent_R((Reduction) _reduction.get(1).getData(), "");
			}
			else if (
					ruleHead.equals("<ProcedureDecl>")
					||
					ruleHead.equals("<FunctionDecl>")
					||
					ruleHead.equals("<MethodDecl>")
					)
			{
				Root prevRoot = root;	// Push the original root
				root = new Root();	// Prepare a new root for the subroutine
				subRoots.add(root);
				for (int i=0; i < _reduction.size(); i++)
				{
					if (_reduction.get(i).getType() == SymbolType.NON_TERMINAL)
					{
						buildNSD_R(_reduction.get(i).asReduction(), root.children);
					}
				}
				// Restore the original root
				root = prevRoot;
			}
			// END KGU#194 2016-05-08
			else if (
					 ruleHead.equals("<ProgHeader>")
					 )
			{
				content=new String();
				content=getContent_R(_reduction.get(1).asReduction(), content);
				root.setText(translateContent(content));
			}
			else if (
					 ruleHead.equals("<ProcHeading>")
					 )
			{
				content = new String();
				content = getContent_R(_reduction.get(1).asReduction(),content);
				
				Reduction secReduc = _reduction.get(2).asReduction();
				if (secReduc.size()!=0)
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
					 ruleHead.equals("<FuncHeading>")
					 )
			{
				content = new String();
				content = getContent_R(_reduction.get(1).asReduction(), content);
				
				Reduction secReduc = _reduction.get(2).asReduction();
				if (secReduc.size()!=0)
				{
					content = getContent_R(secReduc,content);
				}
				
				secReduc = _reduction.get(4).asReduction();
				if (secReduc.size()!=0)
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
					 ruleHead.equals("<WhileStatement>")
					 )
			{
				content = new String();
				content = getContent_R(_reduction.get(1).asReduction(), content);
				While ele = new While(getKeyword("preWhile")+translateContent(content)+getKeyword("postWhile"));
				_parentNode.addElement(ele);
				
				Reduction secReduc = (Reduction) _reduction.get(3).getData();
				buildNSD_R(secReduc,ele.q);
			}
			else if (
					 ruleHead.equals("<RepeatStatement>")
					 )
			{
				content = new String();
				content = getContent_R(_reduction.get(3).asReduction(), content);
				Repeat ele = new Repeat(getKeyword("preRepeat")+translateContent(content)+getKeyword("postRepeat"));
				_parentNode.addElement(ele);
				
				Reduction secReduc = _reduction.get(1).asReduction();
				buildNSD_R(secReduc,ele.q);
			}
			else if (
					 ruleHead.equals("<ForStatement>")
					 )
			{
				content = new String();
				content = getContent_R(_reduction.get(1).asReduction(), content);
				content += ":=";
				content = getContent_R(_reduction.get(3).asReduction(), content);
				content += " ";
				content += getKeyword("postFor");
				content += " ";
				content = getContent_R(_reduction.get(5).asReduction(), content);
				// START KGU#3 2016-05-02: Enh. #10 Token 4 contains the information whether it's to or downto
				if (getContent_R(_reduction.get(4).asReduction(), "").equals("downto"))
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
				Reduction secReduc = _reduction.get(7).asReduction();
				buildNSD_R(secReduc,ele.q);
			}
			else if (
					 ruleHead.equals("<IfStatement>")
					 )
			{
				content = new String();
				content = getContent_R(_reduction.get(1).asReduction(), content);
				
				Alternative ele = new Alternative(getKeyword("preAlt") + translateContent(content)+getKeyword("postAlt"));
				_parentNode.addElement(ele);
				
				Reduction secReduc = _reduction.get(3).asReduction();
				buildNSD_R(secReduc,ele.qTrue);
				if(_reduction.size()>=5)
				{
					secReduc = _reduction.get(5).asReduction();
					buildNSD_R(secReduc,ele.qFalse);
				}
			}
			else if (
					 ruleHead.equals("<CaseSelector>")
					 )
			{
				content = new String();
				content = getContent_R(_reduction.get(0).asReduction(), content);
				
				// sich am parent (CASE) dat nächst fräit Element
				boolean found = false;
				for (int i=0; i<((Case) _parentNode.parent).getText().count(); i++)
				{
					if (((Case) _parentNode.parent).getText().get(i).equals("??") && found==false)
					{
						((Case) _parentNode.parent).getText().set(i,content);
						found=true;
						
						Reduction secReduc = (Reduction) _reduction.get(2).getData();
						buildNSD_R(secReduc,(Subqueue) ((Case) _parentNode.parent).qs.get(i-1));
					}
				}

			}
			else if (
					 ruleHead.equals("<CaseStatement>")
					 )
			{
				content = new String();
				content = getKeyword("preCase")+getContent_R(_reduction.get(1).asReduction(),content)+getKeyword("postCase");
				// am content steet elo hei den "test" dran
				
				// Wéivill Elementer sinn am CASE dran?
				Reduction sr = _reduction.get(3).asReduction();
				int j = 0;
				//System.out.println(sr.getParent().getText());  // <<<<<<<
				while (sr.getParent().getHead().toString().equals("<CaseList>"))
				{
					j++;
					content += "\n??";
					if (sr.size()>=1)
					{
						sr = sr.get(0).asReduction();
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
				Reduction secReduc = _reduction.get(3).asReduction();
				buildNSD_R(secReduc,(Subqueue) ele.qs.get(0));
				// den "otherwise"
				secReduc = _reduction.get(4).asReduction();
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
				if (_reduction.size()>0)
				{
					for(int i=0; i<_reduction.size(); i++)
					{
						if (_reduction.get(i).getType()==SymbolType.NON_TERMINAL)
						{
							buildNSD_R(_reduction.get(i).asReduction(), _parentNode);
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
//		if (_reduction.size()>0)
//		{
			for(int i=0;i<_reduction.size();i++)
			{
				switch (_reduction.get(i).getType()) 
				{
					case NON_TERMINAL:
						_content = getContent_R((Reduction) _reduction.get(i).getData(), _content);	
						break;
					case CONTENT:
						{
							String tokenData = (String) _reduction.get(i).getData();
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

	@Override
	protected void subclassUpdateRoot(Root root, String sourceFileName) {
		// TODO Auto-generated method stub
		
	}

}

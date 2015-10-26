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
 *      Description:    Class to parse a pascal file.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------		----			-----------
 *      Bob Fisch       2008.01.06              First Issue
 *      Bob Fisch       2008.05.02              Added filter for (* ... *) comment filtering
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

import goldengine.java.*;

import lu.fisch.utils.*;
import lu.fisch.structorizer.io.*;
import lu.fisch.structorizer.elements.*;

import com.stevesoft.pat.*;  //http://www.javaregex.com/

 
public class D7Parser implements GPMessageConstants
{
	public static String preAlt = "";
	public static String postAlt = "";
	public static String preCase = "";
	public static String postCase = "";
	public static String preFor = "for ";
	public static String postFor = "to";
	public static String preWhile = "while ";
	public static String postWhile = "";
	public static String preRepeat = "until ";
	public static String postRepeat = "";
	public static String input = "read ";
	public static String output = "write ";
	

	private String compiledGrammar = null;
	Root root = null;
	
	public String error = new String();
	
	GOLDParser parser = null;
	
	public D7Parser(String _compiledGrammar)
	{
		compiledGrammar=_compiledGrammar;
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
	
	public Root parse(String _textToParse)
	{
		// create new root
		root = new Root();
		error = "";
		

		// prepare textfile
		try
		{
			DataInputStream in = new DataInputStream(new FileInputStream(_textToParse));
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "ISO-8859-1"));
			String strLine;
			String pasCode = new String();
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   
			{
				// add no ending because of comment filter
				pasCode+=strLine+"\u2190";
				//pasCode+=strLine+"\n";
			}
			//Close the input stream
			in.close();
			
			// filter out comments
			Regex r = new Regex("(.*?)[(][*](.*?)[*][)](.*?)","$1$3"); 
			pasCode=r.replaceAll(pasCode);
			r = new Regex("(.*?)[{](.*?)[}](.*?)","$1$3"); 
			pasCode=r.replaceAll(pasCode);
			
			// reset correct endings
			r = new Regex("(.*?)[\u2190](.*?)","$1\n$2"); 
			pasCode=r.replaceAll(pasCode);
						
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
		
		// stsart parsing
        while(!done)
        {
            try
            {
				//System.out.println("- Parse: before");
            	response = parser.parse();
				//System.out.println("- Parse: after");
            }
            catch(ParserException parse)
            {
                System.out.println("**PARSER ERROR**\n" + parse.toString());
                System.exit(1);
            }
			
			//System.out.println("===> "+response);
			
            switch(response)
            {
                case gpMsgLexicalError:
                    System.out.println("gpMsgLexicalError");
                    parser.popInputToken();
					done = true;
                    break;
					
                case gpMsgSyntaxError:
                    Token theTok = parser.currentToken();
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
                    break;
					
                case gpMsgNotLoadedError:
                    System.out.println("gpMsgNotLoadedError");
                    done = true;
                    break;
					
                case gpMsgCommentError:
                    System.out.println("gpMsgCommentError");
                    done = true;
					break;
					
                case gpMsgInternalError:
                    System.out.println("gpMsgInternalError");
                    done = true;
                    break;
				
				default:
					//System.out.println("default");
					break;
					
            }
        }
		//System.out.println("---- done ----");
		
        try
        {
        	parser.closeFile();
        }
        catch(ParserException parse)
        {
            System.out.println("**PARSER ERROR**\n" + parse.toString());
            System.exit(1);
        }
		
		//remove the temporary file
		(new File(_textToParse+".structorizer")).delete();
		
		return root;
	}
	
	private void DrawNSD(Reduction _reduction)
	{
		root.isProgram=true;
		DrawNSD_R(_reduction, root.children);
	}
	
	private void DrawNSD_R(Reduction _reduction, Subqueue _parentNode)
	{
		String content = new String();
	
		if (_reduction.getTokenCount()>0)
		{
			if ( 
				_reduction.getParentRule().name().equals("<RefId>")
				||
				_reduction.getParentRule().name().equals("<CallStmt>")
				||
				_reduction.getParentRule().name().equals("<Designator>")
				||
				_reduction.getParentRule().name().equals("<AssignmentStmt>")
			   )
			{
				content=new String();
				content=getContent_R(_reduction,content);
				//System.out.println(content);
				_parentNode.addElement(new Instruction(updateContent(content)));
			}
			else if (
					 _reduction.getParentRule().name().equals("<UsesClause>")
					 ||
					 _reduction.getParentRule().name().equals("<VarSection>")
					 ||
					 _reduction.getParentRule().name().equals("<ConstSection>")
					 )
			{
			}
			else if (
					 _reduction.getParentRule().name().equals("<ProgHeader>")
					 )
			{
				content=new String();
				content=getContent_R((Reduction) _reduction.getToken(1).getData(),content);
				root.setText(updateContent(content));
			}
			else if (
					 _reduction.getParentRule().name().equals("<ProcHeading>")
					 )
			{
				content=new String();
				content=getContent_R((Reduction) _reduction.getToken(1).getData(),content);
				
				Reduction secReduc = (Reduction) _reduction.getToken(2).getData();
				if (secReduc.getTokenCount()!=0)
				{
					content=getContent_R(secReduc,content);
				}
				
				content = BString.replaceInsensitive(content,";","; ");
				content = BString.replaceInsensitive(content,";  ","; ");
				root.setText(updateContent(content));
				root.isProgram=false;
			}
			else if (
					 _reduction.getParentRule().name().equals("<FuncHeading>")
					 )
			{
				content=new String();
				content=getContent_R((Reduction) _reduction.getToken(1).getData(),content);
				
				Reduction secReduc = (Reduction) _reduction.getToken(2).getData();
				if (secReduc.getTokenCount()!=0)
				{
					content=getContent_R(secReduc,content);
				}
				
				secReduc = (Reduction) _reduction.getToken(4).getData();
				if (secReduc.getTokenCount()!=0)
				{
					content+=": ";
					content=getContent_R(secReduc,content);
				}
				
				content = BString.replaceInsensitive(content,";","; ");
				content = BString.replaceInsensitive(content,";  ","; ");
				root.setText(updateContent(content));
				root.isProgram=false;
			}
			else if (
					 _reduction.getParentRule().name().equals("<WhileStatement>")
					 )
			{
				content=new String();
				content=getContent_R((Reduction) _reduction.getToken(1).getData(),content);
				While ele = new While(preWhile+updateContent(content)+postWhile);
				_parentNode.addElement(ele);
				
				Reduction secReduc = (Reduction) _reduction.getToken(3).getData();
				DrawNSD_R(secReduc,ele.q);
			}
			else if (
					 _reduction.getParentRule().name().equals("<RepeatStatement>")
					 )
			{
				content=new String();
				content=getContent_R((Reduction) _reduction.getToken(3).getData(),content);
				Repeat ele = new Repeat(preRepeat+updateContent(content)+postRepeat);
				_parentNode.addElement(ele);
				
				Reduction secReduc = (Reduction) _reduction.getToken(1).getData();
				DrawNSD_R(secReduc,ele.q);
			}
			else if (
					 _reduction.getParentRule().name().equals("<ForStatement>")
					 )
			{
				content=new String();
				content=getContent_R((Reduction) _reduction.getToken(1).getData(),content);
				content+=":=";
				content=getContent_R((Reduction) _reduction.getToken(3).getData(),content);
				content+="  ";
				content+=postFor;
				content+="  ";
				content=getContent_R((Reduction) _reduction.getToken(5).getData(),content);
				
				For ele = new For(preFor+updateContent(content));
				_parentNode.addElement(ele);
				
				Reduction secReduc = (Reduction) _reduction.getToken(7).getData();
				DrawNSD_R(secReduc,ele.q);
			}
			else if (
					 _reduction.getParentRule().name().equals("<IfStatement>")
					 )
			{
				content=new String();
				content=getContent_R((Reduction) _reduction.getToken(1).getData(),content);
				
				Alternative ele = new Alternative(preAlt+updateContent(content)+postAlt);
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
					 _reduction.getParentRule().name().equals("<CaseSelector>")
					 )
			{
				content=new String();
				content=getContent_R((Reduction) _reduction.getToken(0).getData(),content);
				
				// sich am parent (CASE) dat nächst fräit Element
				boolean found = false;
				for(int i=0;i<((Case) _parentNode.parent).getText().count();i++)
				{
					if(((Case) _parentNode.parent).getText().get(i).equals("??") && found==false)
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
					 _reduction.getParentRule().name().equals("<CaseStatement>")
					 )
			{
				content=new String();
				content=preCase+getContent_R((Reduction) _reduction.getToken(1).getData(),content)+postCase;
				// am content steet elo hei den "test" dran
				
				// Wéivill Elementer sinn am CASE dran?
				Reduction sr = (Reduction) _reduction.getToken(3).getData();
				int j=0;
				//System.out.println(sr.getParentRule().getText());  // <<<<<<<
				while(sr.getParentRule().name().equals("<CaseList>"))
				{
					  j++;
					  content+="\n??";
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
					content+="\nelse";
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
					for(int i=0;i<_reduction.getTokenCount();i++)
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

		_content=_content.replaceAll(BString.breakup("write")+"[((](.*?)[))]",output+" $1");
		_content=_content.replaceAll(BString.breakup("writeln")+"[((](.*?)[))]",output+" $1");
		_content=_content.replaceAll(BString.breakup("writeln")+"(.*?)",output+" $1");
		_content=_content.replaceAll(BString.breakup("write")+"(.*?)",output+" $1");
		_content=_content.replaceAll(BString.breakup("read")+"[((](.*?)[))]",input+" $1");
		_content=_content.replaceAll(BString.breakup("readln")+"[((](.*?)[))]",input+" $1");
		_content=_content.replaceAll(BString.breakup("readln")+"(.*?)",input+" $1");
		_content=_content.replaceAll(BString.breakup("read")+"(.*?)",input+" $1");
		
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
		if (_reduction.getTokenCount()>0)
		{
			for(int i=0;i<_reduction.getTokenCount();i++)
			{
				switch (_reduction.getToken(i).getKind()) 
				{
					case SymbolTypeConstants.symbolTypeNonterminal:
						_content=getContent_R((Reduction) _reduction.getToken(i).getData(), _content);	
						break;
					case SymbolTypeConstants.symbolTypeTerminal:
						if (((String) _reduction.getToken(i).getData()).trim().equals("mod") ||
						    ((String) _reduction.getToken(i).getData()).trim().equals("div"))
							{
								_content+=" "+(String) _reduction.getToken(i).getData()+" ";
							}
							else
							{
								_content+=(String) _reduction.getToken(i).getData();
							}
						break;
					default:
						break;
				}
			}
		}
		else
		{
			// ?
			// _content:=_content+trim(R.ParentRule.Text)
		}
		
		_content = BString.replaceInsensitive(_content,")and(",") and (");
		_content = BString.replaceInsensitive(_content,")or(",") or (");
		
		return _content;
	}
	
	/************************
	 * static things
	 ************************/
	
	public static void loadFromINI()
	{
		try
		{
			Ini ini = Ini.getInstance();
			ini.load();

			// elements
			preAlt=ini.getProperty("ParserPreAlt","");
			postAlt=ini.getProperty("ParserPostAlt","");
			preCase=ini.getProperty("ParserPreCase","");
			postCase=ini.getProperty("ParserPostCase","");
			preFor=ini.getProperty("ParserPreFor","pour ");
			postFor=ini.getProperty("ParserPostFor","\u00E0");
			preWhile=ini.getProperty("ParserPreWhile","tant que ");
			postWhile=ini.getProperty("ParserPostWhile","");
			preRepeat=ini.getProperty("ParserPreRepeat","jusqu'\u00E0 ");
			postRepeat=ini.getProperty("ParserPostRepeat","");
			input=ini.getProperty("ParserInput","lire");
			output=ini.getProperty("ParserOutput","\u00E9crire");
			
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
			ini.setProperty("ParserPreAlt",preAlt);
			ini.setProperty("ParserPostAlt",postAlt);
			ini.setProperty("ParserPreCase",preCase);
			ini.setProperty("ParserPostCase",postCase);
			ini.setProperty("ParserPreFor",preFor);
			ini.setProperty("ParserPostFor",postFor);
			ini.setProperty("ParserPreWhile",preWhile);
			ini.setProperty("ParserPostWhile",postWhile);
			ini.setProperty("ParserPreRepeat",preRepeat);
			ini.setProperty("ParserPostRepeat",postRepeat);
			ini.setProperty("ParserInput",input);
			ini.setProperty("ParserOutput",output);
			
			ini.save();
		}
		catch (Exception e) 
		{
			System.out.println(e);
		}
	}

}
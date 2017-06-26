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

package lu.fisch.utils;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    Class to manage a textfile.
 *						Aims to work like working with a textfile in Delphi.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2003.05.10      First Issue
 *		Bob Fisch		2007.12.10		Moved to another package for Structorizer
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************/

import java.io.*;
import lu.fisch.utils.*;

public class BTextfile
{


 /** The outputwrites */
  private OutputStreamWriter myfile_out;
 /** The inputwrites */
  private InputStreamReader myfile_in;
 /** The filename */
  private String filename = new String();
 /** The file-end-check */
  private boolean fileend = false;
 /** The line-end-check */
  private boolean lineend = false;
 /** A temporary variable to the last read byte */
  private String lastbyte = new String();

 /**
  * Returns TRUE if end-of-file reached
  *@return TRUE if end-of-file reached
  */
  public boolean eof()
  {
   return fileend;
  }

 /**
  * Returns TRUE if end-of-line reached
  *@return TRUE if end-of-line reached
  */
  public boolean eol()
  {
   return lineend;
  }

 /**
  * Contructor of the BTextfile object
  *@param filename The file to be assigned to
  */
  public BTextfile(String filename)
  {
   this.filename=filename;
   fileend=false;
  }

	/**
	 * Opens the file for writing (in the given Charset) and overwrites any
	 * existing file with the same name
	 * @param _code - The name of a supported Charset
	 */
	public void rewrite(String _code) throws IOException
	{
		myfile_out=new OutputStreamWriter(new FileOutputStream(filename), _code);
		fileend=false;
	}
	
	/**
	 * Opens the file for writing and overwrites any existing file with the same name
	 * Uses charset "ISO-8859-1". (For a different encoding use BTextfile.write(_code);)
	 */
	public void rewrite() throws IOException
	{
		myfile_out=new OutputStreamWriter(new FileOutputStream(filename), "ISO-8859-1");
		fileend=false;
	}
	
	/**
  * Opens the file for reading
  */
  public void reset() throws IOException
  {
   myfile_in=new InputStreamReader(new FileInputStream(filename));
   fileend=false;
   read();
  }

 /**
  * Opens the file for writing and appends text to any existing file with the same name
  */
  public void append() throws IOException
  {
   myfile_out=new OutputStreamWriter(new FileOutputStream(filename,true));
  }

 /**
  * Closes and saves the file
  */
  public void close() throws IOException
  {
   if (myfile_out != null) {myfile_out.close();}
   if (myfile_in  != null) {myfile_in.close();}
   fileend=true;
  }

 /**
  * Writes to the file
  *@param mystring String to be written to the file
  */
  public void write(String mystring) throws IOException
  {
   myfile_out.write(mystring,0,mystring.length());
  }

 /**
  * Writes to the file and adds end-of-line symbol
  *@param mystring String to be written to the file
  */
  public void writeln(String mystring) throws IOException
  {
   myfile_out.write(mystring+"\r\n",0,mystring.length()+2);
  }

 /**
  * Reads one caracter (byte) from the file
  *@return mystring The string that has been read
  */
  public String read() throws IOException
  {
   if (eof()==false)
   {
    Integer ibuf = new Integer(myfile_in.read());
    byte[] bbuf = new byte[1];
    bbuf[0] = ibuf.byteValue();

    if ((bbuf[0]==10)||(bbuf[0]==13)) {lineend=true;myfile_in.read();}
     else {lineend=false;}
    if ((bbuf[0]==-1)) {fileend=true;}
     else {fileend=false;}

    String sbuf = lastbyte;
    lastbyte = new String(bbuf);

    return sbuf;
   }
   else
   {
    return "";
   }
  }

 /**
  * Reads a single line from the file but cuts blanks at the beginning and the end
  *@return The trimmed string that has been read
  */
  public String readlnDataonly() throws IOException
  {
   String str = readln();
   return str.trim();
  }

 /**
  * Reads from the file a single line
  *@return mystring The string that has been read
  */
  public String readln() throws IOException
  {
   String sbuf = new String();

   if (eof()==false)
   {
    String cbuf = new String();
    do
    {
    cbuf = read();
     if ((cbuf.equals("\n")==false)&&(cbuf.equals("\r")==false))
     {
      sbuf=sbuf.concat(cbuf);
     }
    }
    while ((eof()==false)&&(eol()==false));

   }
   return sbuf;
  }
}

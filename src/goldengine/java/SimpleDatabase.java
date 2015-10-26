package goldengine.java;

import java.io.*;
import java.util.*;

/*
 * Licensed Material - Property of Matthew Hawkins (hawkini@barclays.net)
 *
 * GOLDParser - code ported from VB - Author Devin Cook. All rights reserved.
 *
 * No modifications to this code are allowed without the permission of the author.
 */
/**-------------------------------------------------------------------------------------------<br>
 *
 *      Source File:    SimpleDatabase.java<br>
 *
 *      Author:         Devin Cook, Matthew Hawkins<br>
 *
 *      Description:    A class written to open a binary cgt file (written in VB), and read its
 *                      contents for simple database records.<br>
 *
 *
 *-------------------------------------------------------------------------------------------<br>
 *
 *      Revision List<br>
 *<pre>
 *      Author          Version   Date         Description
 *      ------          -------   ----------   -----------
 *      MPH             1.0                    First Issue
 *      MPH             1.1                    Bug fixed where getNextRecord would always be false.
 *      Rick Blommers   1.3       2005/01/26   Added some code to the Databaseloader: "The number of multiblocks wasn't handled correctly"
 *		Bob Fisch		1.4		  2008/01/04   Added support to read grammar file from JAR-Ressource	
 *
 *</pre><br>
 *-------------------------------------------------------------------------------------------<br>
 *
 *      IMPORT: java.io, java.util<br>
 *
 *-------------------------------------------------------------------------------------------<br>
 */
public class SimpleDatabase implements EntryContentConstants
{
    //private File database = null;
    private boolean bFileOpen = false;
    private int fileNumber = 0;
    private BufferedInputStream buff = null;
    private String fileType = "";
    private final int RecordContentMulti = 77;

    private int entryReadPosition = 0;
    private Vector entryList = null;

    /***************************************************************
     *
     * retrieve
     *
     * This method will retrieve a set of integers that have been
     * read by the SimpleDatabase file.
     * @param numParams The number of records to return.
     * @return The int array holds the integers associated with the
     *          record.
     ***************************************************************/
    public int[] retrieve(int numParams)
    {

        int n = 0;
        int[] returnArr = new int[numParams];

        while(n < numParams)
        {
            Integer intA = (Integer)entryList.elementAt(entryReadPosition);
            returnArr[n] = intA.intValue();
            entryReadPosition++;
            n++;
        }

        return returnArr;
    }

    /***************************************************************
     *
     * retrieveDone
     *
     * This method checks to see if there no many fields or records
     * to be read.
     * @return True if there are no many fields to retrieve, false
     *          if there is.
     ***************************************************************/
    public boolean retrieveDone()
    {
        return !(entryReadPosition < entryList.size());
    }

    /***************************************************************
     *
     * retrieveNext
     *
     * This method will retrieve an Object that has been read in
     * by the SimpleDatabase. It could be a String, Boolean etc.
     * @return The next field.
     ***************************************************************/
    public Object retrieveNext()
    {
        Object returnObj;
        if(!retrieveDone())
        {
            returnObj = entryList.elementAt(entryReadPosition);
            entryReadPosition++;
            return returnObj;
        }
        else
        {
            return null;
        }
    }

    /***************************************************************
     *
     * openFile
     *
     * This method will open the file for reading.
     * @param fileName the absolute pathname of the file to read.
     * @return Will return true if the file was opened, false
     *          if there was an IOException, or if there was an
     *          invalid file header.
     * @throws ParserException Thrown if there is a problem with the stream.
     ***************************************************************/
    public boolean openFile(String fileName) throws ParserException
    {
        boolean retBol = false;

        if(bFileOpen)
        {
            fileNumber = 0;
        }
		
		//fileName="D7Grammar.cgt";

        //database = new File(getClass().getResource(fileName).getFile());
        //database = new File(fileName);
		//System.out.println(database.length());
		
		
        //if(database.exists())
		if(this.getClass().getResourceAsStream(fileName)!=null)
        {
            bFileOpen = true;
            try
            {
				//buff = new BufferedInputStream(new FileInputStream(database));
				//System.out.println("Opening database");
				buff = new BufferedInputStream(getClass().getResourceAsStream(fileName));
				
				//System.out.println("AVAIL: "+buff.available());
				//buff = new BufferedInputStream(this.getClass().getResourceAsStream(fileName));
			}
            catch(Exception ioex)
            {
				System.out.println(ioex.getMessage());
                bFileOpen = false;
                return false;
            }

            try
            {
                retBol = hasValidHeader();
            }
            catch(IOException ioex)
            {
                throw new ParserException("Error in Database stream - Header Check <SD.hVH()>.");
            }

            return retBol;
        }
        else
        {
			System.out.println("Can't find grammar file: "+fileName);
            bFileOpen = false;
            return false;
        }
    }

    /***************************************************************
     *
     * closeFile
     *
     * This method will close the file and set the Buffer to null.
     * @throws ParserException If there was a problem closing the file.
     ***************************************************************/
    public void closeFile() throws ParserException
    {
        try
        {
            buff.close();
        }
        catch(IOException ioex)
        {
            throw new ParserException("Error in Database stream - File Closure <SD.cF()>.");
        }

        buff = null;
    }

    // this method is inaccessible. It will check the file header of
    // the file opened, and return true if it agrees with the file
    // type already set up, false if not.
    private boolean hasValidHeader() throws IOException
    {
        int Char1 = -1;
        int Char2 = -1;
        String str = "";
        boolean done = false;
        char t;

        do
        {
            Char1 = buff.read();
            Char2 = buff.read();

            if((Char1 == 0) & (Char2 == 0))
            {
                done = true;
            }
            else
            {
                if(Char1 == 0)
                {
                    t = (char)Char2;
                    str += t;
                }
                else
                {
                    t = (char)Char1;
                    str += t;
                }
            }
        } while(!done);

        if(str.equals(fileType))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /***************************************************************
     *
     * setFileType
     *
     * This method will identify what file can be read by the
     * simple database.
     * @param newFileType The file type to read.
     ***************************************************************/
    public void setFileType(String newFileType)
    {
        fileType = newFileType;
    }

    /***************************************************************
     *
     * getFileType
     *
     * This method will return what file type this Database instance
     * can read.
     * @return The file type.
     ***************************************************************/
    public String getFileType()
    {
        return fileType;
    }

    /***************************************************************
     *
     * done
     *
     * This method will check to see if we have reached the end of the
     * file.
     * @return True if the end of the file has been reached, false if
     *              not.
     * @throws ParserException If there was a problem with reading the
     *              stream.
     ***************************************************************/
    public boolean done() throws ParserException
    {
        boolean done = false;
        /*int t1, t2;

        try
        {
            //if(bFileOpen & database.canRead())
			System.out.println("AVAIL: "+buff.available());

			if(bFileOpen && buff.available()!=0)
            {
                buff.mark(5);

                t1 = buff.read();
                t2 = buff.read();

                if(t1 == -1)
                {
                    done = true;
                }

                if(t2 == -1)
                {
                    done = true;
                }

                //return false;
                done = false;
            }

            buff.reset();
        }
        catch(IOException ioex)
        {
			System.out.println(ioex.getMessage());
            throw new ParserException("Error in Database stream - EOF Check <SD.done()>.");
        }/**/
		
		try
        {
			done=(buff.available()==0);
		}
        catch(IOException ioex)
        {
			System.out.println(ioex.getMessage());
            throw new ParserException("Error in Database stream - EOF Check <SD.done()>.");
        }

        return done;
    }

    /***************************************************************
     *
     * getNextRecord #ver1.1#
     *
     * This method will read the file for the next record, and place
     * each field in the Vector to be retrieved later.
     * <p>
     * ver1.1 Found bug where it would always return false.
     * @return True if there was no problems getting the next record,
     *              and false if there was.
     * @throws ParserException If there was some problem with the stream.
     ***************************************************************/
    public boolean getNextRecord() throws ParserException
    {
        boolean found = false;
        boolean retBol = false;
        int id, count;

        try
        {
            //if(bFileOpen & database.canRead())
			if(bFileOpen && buff.available()!=0)
            {
				//System.out.println("AVAIL: "+buff.available());

                int t1 = 0;
                while(!found)
                {
                    t1 = buff.read();
					//System.out.println("T1: "+t1);

                    if(t1 != 0)
                    {
                        found = true;
                    }
                }

                found = false;

                id = t1;
                t1 = 0;
                clear();
                switch(id)
                {
                    case RecordContentMulti:
                        count = buff.read();
                        t1 = buff.read();

                        //MODIFIED 2005/01/26: Rick Blommers
                        //... bug in the Databaseloader of the Java Parsing engine
                        //... the number of multiblocks wasn't handled correctly:
                        count = (t1<<8) + count;
                        //END MODIFIED CODE

                        for(int i=0; i<count; i++)
                        {
                            readEntry();
                        }
                        entryReadPosition = 0;
                        retBol = true;
                        break;

                    default:
                        retBol = false;
                }
            }
            else
            {
                retBol = false;
            }
        }
        catch(IOException ioex)
        {
            throw new ParserException("Error in Database stream - Getting Next Record <SD.gNR()>.");
        }

        return retBol;
    }

    // this method is inaccesible. It will read a portion of the
    // database file and create a new Object or an integer depending
    // on what the next field is, which is identified by the number of
    // the byte - which itself is given in the EntryContentConstants
    // interface. Please do NOT edit this method! Nasty things will
    // happen.
    private void readEntry() throws IOException
    {
        Integer intA;
        int t1 = 0, t2 = 0;
        boolean found = false;

        while(!found)
        {
            t1 = buff.read();
            if(t1 != 0)
            {
                found = true;
            }
        }

        switch(t1)
        {
            case entryContentEmpty:
                // do nuffin, There are no additional bytes
                Object empty = new Object();
                entryList.addElement(empty);
                break;

            case entryContentInteger:
                // a very complicated piece of code, to construct
                // an integer from two bytes. It means constructing
                // a binary representation in two Strings, then
                // calculating the binary value and storing it as a
                // integer. Very nasty. Very messy. Do NOT edit!
                byte b1 = (byte)buff.read();
                byte b2 = (byte)buff.read();

                String binar = "";
                if((b1 < 0) & (b2 == 0))
                {
                    String tmpStr = Integer.toBinaryString((int)b1);
                    binar = tmpStr.substring(tmpStr.length() - 8);
                }
                else
                {
                    if(b1 < 0)
                    {
                        String tmpStr = Integer.toBinaryString((int)b2);
                        binar = tmpStr;
                        tmpStr = Integer.toBinaryString((int)b1);
                        binar += tmpStr.substring(tmpStr.length() - 8);
                    }
                    else
                    {
                        if(b2 == 0)
                        {
                            binar = Integer.toBinaryString((int)b1);
                        }
                        else
                        {
                            binar = Integer.toBinaryString((int)b1);
                            int len = binar.length();
                            int leadingZeros = 8 - len;
                            for(int i=0; i<leadingZeros; i++)
                            {
                                binar = "0" + binar;
                            }

                            binar = Integer.toBinaryString((int)b2) + binar;
                        }
                    }
                }

                int realInt = 0;
                int multiplier = 1;

                for(int i = (binar.length()-1); i>=0; i--)
                {
                    if(binar.charAt(i) == '1')
                    {
                        realInt += multiplier;
                    }
                    multiplier = multiplier * 2;
                }
                intA = new Integer(realInt);
                entryList.addElement(intA);
                break;

            case entryContentString:
                boolean done = false;
                String str = "";

                do
                {
                    int c1 = buff.read();
                    int c2 = buff.read();
                    if(c1 == 0)
                    {
                        done = true;
                    }
                    else
                    {
                        char ch = (char)c1;
                        str += ch;
                    }
                } while(!done);

                entryList.addElement(str);
                break;

            case entryContentBoolean:
                Boolean bool;
                int boolI = buff.read();
                if(boolI == 1)
                {
                    bool = new Boolean(true);
                }
                else
                {
                    bool = new Boolean(false);
                }
                entryList.addElement(bool);
                break;

            case entryContentByte:
                int byte1 = buff.read();
                intA = new Integer(byte1);
                entryList.addElement(intA);
                break;
        }
    }

    /***************************************************************
     *
     * clear
     *
     * This method will reset all the currently read fields.
     ***************************************************************/
    public void clear()
    {
        entryReadPosition = 0;
        entryList = new Vector();
    }
}
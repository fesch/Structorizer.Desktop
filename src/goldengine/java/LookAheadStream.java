package goldengine.java;

import java.io.*;

/*
 * Licensed Material - Property of Matthew Hawkins (hawkini@barclays.net)
 *
 * GOLDParser - code ported from VB - Author Devin Cook. All rights reserved.
 *
 * No modifications to this code are allowed without the permission of the author.
 */
/**-------------------------------------------------------------------------------------------<br>
 *
 *      Source File:    LookAheadStream.java<br>
 *
 *      Author:         Matthew Hawkins<br>
 *
 *      Description:    A stream implementation that uses a PushbackReader, to read the source
 *                      file character by character, and will place characters on a char buffer.
 *                      This char buffer is then used when a token needs to be read.<br>
 *
 *
 *-------------------------------------------------------------------------------------------<br>
 *
 *      Revision List<br>
 *<pre>
 *      Author          Version   Date           Description
 *      ------          -------   -----------    -----------
 *      MPH             1.0                      First Issue
 *      MPH             1.1                      Fixed a bug with discarding a line for comments
 *      Nat Ayewah      1.3       2005/05/20     Sometimes the JVM would convert CRLF to \n in a single char: "I noticed that the character read was the good-old-fashioned '\n'... I've modified this function in LookAheadStream.java"
 *

 *</pre><br>
 *
 *-------------------------------------------------------------------------------------------<br>
 *
 *      IMPORT: java.io<br>
 *
 *-------------------------------------------------------------------------------------------<br>
 */
public class LookAheadStream
{
    private PushbackReader buffR;
    private char[] unreadme;
    private final int kMaxBufferSize = 1024;
    private int multiplier = 2;
    private int bufferPos;

    /***************************************************************
     *
     * openFile
     *
     * This method will open the source file to read.
     * @param <parameter name> <parameter description>
     * @return True if the file was opened. It will not return false,
     *          as an exception should be thrown beforehand.
     * @throws ParserException If the PushbackReader was not initialised.
     ***************************************************************/
    public boolean openFile(String file) throws ParserException
    {
        try
        {
            buffR = new PushbackReader(new FileReader(new File(file)));
            unreadme = new char[kMaxBufferSize];
            bufferPos = 0;
        }
        catch(IOException ioex)
        {
            throw new ParserException("Source file could not be opened.");
        }

        return true;
    }

    /***************************************************************
     *
     * closeFile
     *
     * This method will close the source file.
     * @return True if the file was closed. It will not return false,
     *          as an exception should be thrown beforehand.
     * @throws ParserException if the file could not be closed.
     ***************************************************************/
    public boolean closeFile() throws ParserException
    {
        try
        {
            buffR.close();
        }
        catch(IOException ioex)
        {
            throw new ParserException("Source file could not be closed.");
        }

        return true;
    }

    // this method is not accessible. It doubles the size of the
    // available array acting as a character buffer, copying the
    // contents of the old buffer to the new one.
    private void doubleArray()
    {
        int size = kMaxBufferSize * multiplier;
        char[] unreadmeDouble = new char[size];
        multiplier *= 2;
        for(int i=0; i<size / multiplier; i++)
        {
            unreadmeDouble[i] = unreadme[i];
        }

        unreadme = unreadmeDouble;
    }

    // this method is not accessible. It will get rid of read
    // characters which are not needed anymore.
    private void unreadChars(int size)
    {
        for(int i=0; i<(bufferPos - size); i++)
        {
            unreadme[i] = unreadme[size+i];
        }
        bufferPos = 0;
    }

    /***************************************************************
     *
     * done
     *
     * This method checks the next character to see if it is the end
     * of the file. If it is not, it will use the functionality of the
     * PushbackReader to push the read character back onto the stream.
     * @return True if it is the end of file next, false if not.
     * @throws ParserException if the stream could not be read.
     ***************************************************************/
    public boolean done() throws ParserException
    {
        try
        {
            int pushR = buffR.read();

            if(pushR == -1)
            {
                return true;
            }

            buffR.unread(pushR);
        }
        catch(IOException ioex)
        {
            throw new ParserException("Error on PushBack stream - EOF Check <LAS.done()>.");
        }

        return false;
    }

    /***************************************************************
     *
     * nextCharNotWhitespace
     *
     * A quick checker method which checks to see if the next
     * character is whitespace.
     * @return True if it is not whitespace, false if it is.
     * @throws ParserException if the stream could not be read.
     ***************************************************************/
    public boolean nextCharNotWhitespace() throws ParserException
    {
        boolean retBool;

        try
        {
            int pushR = buffR.read();
            char check = (char)pushR;

            if(Character.isWhitespace(check))
            {
                retBool = false;
            }
            else
            {
                retBool = true;
            }

            buffR.unread(pushR);
        }
        catch(IOException ioex)
        {
            throw new ParserException("Error on PushBack stream - Whitespace Check <LAS.ncnws()>.");
        }

        return retBool;
    }

    /***************************************************************
     *
     * nextChar
     *
     * This method will read one character (int) from the stream.
     * @return It will return the character as a String, unless
     *          it is the end of string, where it will return the
     *          character represented by the int "2".
     * @throws ParserException if the stream could not be read.
     ***************************************************************/
    public String nextChar() throws ParserException
    {
        String returnStr;

        if(!done())
        {
            int pushR;
            try
            {
                pushR = buffR.read();
            }
            catch(IOException ioex)
            {
                throw new ParserException("Error on PushBack stream - Next Char <LAS.nextChar()>.");
            }
            char ch = (char)pushR;

            if(bufferPos == kMaxBufferSize)
            {
                doubleArray();
            }

            unreadme[bufferPos] = ch;
            bufferPos++;

            returnStr = "" + ch;
        }
        else
        {
            char EOF = (char)2;
            return "" + EOF;
        }

        return returnStr;
    }

    /***************************************************************
     *
     * read
     *
     * This method will return a String of length <code>size</code>.
     * The String is contained in the buffer of read characters.
     * @param size The number of characters to read from the buffer.
     * @return The String created from the buffer.
     * @throws ParserException if the stream could not be read.
     ***************************************************************/
    public String read(int size) throws ParserException
    {
        try
        {
            // push back the last char read back onto the stream
            int pushR = (int)unreadme[bufferPos-1];
            buffR.unread(pushR);
            if(pushR == 65535)
            {
                // cripes - its the end of the file! we shouldn't of pushed
                // it back onto the stream - re-read it!
                buffR.read();
            }
        }
        catch(IOException ioex)
        {
            throw new ParserException("Error on PushBack stream - Read <LAS.read(int)>.");
        }

        String text = "";
        for(int i=0; i<size; i++)
        {
            char useBuf = unreadme[i];
            text += useBuf;
        }

        unreadChars(size);

        return text;
    }

    /***************************************************************
     *
     * readLine #ver1.1#
     *
     * This method will read characters from the buffer until a line
     * feed or carriage return is found. This means characters should
     * be read using <code>nextChar</code> first to place them on the
     * buffer. This method had a bug where the end of the line could not
     * be found.
     * @return The String of characters in the buffer.
     * @throws ParserException if the stream could not be read.
     ***************************************************************/
    public String readLine() throws ParserException
    {
        int charSpace = 0, cr = 0;
        boolean endReached = false;
        boolean crPresent = false;
        String text = "", ch;

        while(!endReached & !done())
        {
            ch = nextChar();
            charSpace++;

            if((ch.charAt(0) == '\f') | (ch.charAt(0) == '\r'))
            {
                ch = nextChar();
                if((ch.charAt(0) == '\f') | (ch.charAt(0) == '\r'))
                {
                    crPresent = true;
                    charSpace++;
                }
                endReached = true;
            }
            //MODIFIED 2005/05/20: Nat Ayewah
            //Sometimes the JVM would convert CRLF to \n in a single char
            //"I noticed that the character read was the good-old-fashioned '\n'.
            //(This was on a PC.) I've modified this function in LookAheadStream.java
            else if(ch.charAt(0) == '\n')
            {
                endReached = true;
            }
            //END OF MODIFIED CODE
            else
            {
                text += ch;
            }
        }

        unreadChars(charSpace);

        return text;
    }
}
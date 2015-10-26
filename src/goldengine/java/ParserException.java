package goldengine.java;

/*
 * Licensed Material - Property of Matthew Hawkins (hawkini@barclays.net)
 *
 * GOLDParser - code ported from VB - Author Devin Cook. All rights reserved.
 *
 * No modifications to this code are allowed without the permission of the author.
 */
/**-------------------------------------------------------------------------------------------<br>
 *
 *      Source File:    ParserException.java<br>
 *
 *      Author:         Matthew Hawkins<br>
 *
 *      Description:    A specialised Exception class that will be thrown in all cases when
 *						a normal exception would of been thrown.<br>
 *
 *
 *-------------------------------------------------------------------------------------------<br>
 *
 *      Revision List<br>
 *<pre>
 *      Author          Version         Description
 *      ------          -------         -----------
 *      MPH             1.0             First Issue</pre><br>
 *
 *-------------------------------------------------------------------------------------------<br>
 *
 *      IMPORT: NONE<br>
 *
 *-------------------------------------------------------------------------------------------<br>
 */
public class ParserException extends Exception
{
    /***************************************************************
 	 *
 	 * ParserException
 	 *
 	 * The default constructor gives a default message.
 	 ***************************************************************/
    public ParserException()
    {
        super("A fault ");
    }

    /***************************************************************
 	 *
 	 * ParserException
 	 *
 	 * This constructor gives a user defined message.
 	 ***************************************************************/
    public ParserException(String text)
    {
        super(text);
    }
}
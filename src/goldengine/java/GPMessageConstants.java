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
 *      Source File:    GPMessageConstants.java<br>
 *
 *      Author:         Matthew Hawkins<br>
 *
 *      Description:    A set of constants associated with telling the user what the parsing
 *						engine has done.<br>
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
public interface GPMessageConstants
{
    /** A new token is read */
    final int gpMsgTokenRead       = 201;
    /** A rule is reduced */
    final int gpMsgReduction       = 202;
	/** Grammar complete */
    final int gpMsgAccept          = 203;
    /** No grammar is loaded */
    final int gpMsgNotLoadedError  = 204;
    /** Token not recognized */
    final int gpMsgLexicalError    = 205;
    /** Token is not expected */
    final int gpMsgSyntaxError     = 206;
    /** Reached the end of the file - mostly due to being stuck in comment mode */
    final int gpMsgCommentError    = 207;
    /** Something is wrong, very wrong */
    final int gpMsgInternalError   = 208;
}
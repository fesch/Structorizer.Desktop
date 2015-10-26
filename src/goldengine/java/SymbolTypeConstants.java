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
 *      Source File:    SymbolTypeConstants.java<br>
 *
 *      Author:         Matthew Hawkins<br>
 *
 *      Description:    A set of constants associated with what type a symbol is.
 *						Do NOT change these numbers!<br>
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
public interface SymbolTypeConstants
{
    /** Normal nonterminal */
	final int symbolTypeNonterminal   = 0;
    /** Normal terminal */
    final int symbolTypeTerminal      = 1;
    /** Type of terminal */
    final int symbolTypeWhitespace    = 2;
    /** End character (EOF) */
    final int symbolTypeEnd           = 3;
    /** Comment start */
    final int symbolTypeCommentStart  = 4;
    /** Comment end */
    final int symbolTypeCommentEnd    = 5;
    /** Comment line */
    final int symbolTypeCommentLine   = 6;
     /** Error symbol */
    final int symbolTypeError         = 7;
}
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
 *      Source File:    ParseResultConstants.java<br>
 *
 *      Author:         Matthew Hawkins<br>
 *
 *      Description:    Set of Constants associated with what Action should be performed after a parse.
 *						Do NOT change the numbers!<br>
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
public interface ParseResultConstants
{
    final int parseResultAccept 		   = 301;
    final int parseResultShift 			   = 302;
    final int parseResultReduceNormal 	   = 303;
    final int parseResultReduceEliminated  = 304;
	final int parseResultSyntaxError 	   = 305;
    final int parseResultInternalError     = 306;
}
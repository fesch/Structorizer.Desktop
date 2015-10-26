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
 *      Source File:    ActionConstants.java<br>
 *
 *      Author:         Matthew Hawkins<br>
 *
 *      Description:    Set of Constants associated with what Action should be performed.
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
public interface ActionConstants
{
    /** Shift a symbol and goto a state */
    final int actionShift  = 1;
	/** Reduce by a specified rule */
    final int actionReduce = 2;
	/** Goto to a state on reduction */
    final int actionGoto   = 3;
	/** Input successfully parsed */
    final int actionAccept = 4;
	/** Programmars see this often! */
    final int actionError  = 5;
}
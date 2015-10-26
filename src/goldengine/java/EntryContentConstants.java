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
 *      Source File:    EntryContentConstants.java<br>
 *
 *      Author:         Matthew Hawkins<br>
 *
 *      Description:    Set of Constants associated with the Compiled Grammar Table file records.
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
public interface EntryContentConstants
{
    /** Defined as E */
    final int entryContentEmpty    = 69; 
	/** Defined as I - Signed, 2 byte */
    final int entryContentInteger  = 73;  
	/** Defined as S - Unicode format */
    final int entryContentString   = 83; 
	/** Defined as B - 1 byte, Value is 0 or 1 */
    final int entryContentBoolean  = 66;
    /** Defined as b */
    final int entryContentByte     = 98;
}
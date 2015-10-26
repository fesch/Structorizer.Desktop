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
 *      Source File:    RecordIDConstants.java<br>
 *
 *      Author:         Matthew Hawkins<br>
 *
 *      Description:    A set of constants associated with the type of records in the cgt file.
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
public interface RecordIDConstants
{
    /** Defined as P */
    final int recordIdParameters   = 80;
    /** Defined as T */
	final int recordIdTableCounts  = 84;
    /** Defined as I */
	final int recordIdInitial      = 73;
    /** Defined as S */
	final int recordIdSymbols      = 83;
    /** Defined as C */
	final int recordIdCharSets     = 67;
    /** Defined as R */
	final int recordIdRules        = 82;
    /** Defined as D */
	final int recordIdDFAStates    = 68;
    /** Defined as L */
	final int recordIdLRTables     = 76;
    /** Defined as ! */
	final int recordIdComment      = 33;
}
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
 *      Source File:    FAEdge.java<br>
 *
 *      Author:         Devin Cook, Matthew Hawkins<br>
 *
 *      Description:    Represents an edge for the DFA state.<br>
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
public class FAEdge
{
    private String characters;
    private int targetIndex;

    /***************************************************************
 	 *
 	 * getChars
     *
 	 * Returns the set of characters associated with this edge
 	 * @return The set of characters associated with this edge
 	 ***************************************************************/
    public String getChars() { return characters; }

    /***************************************************************
 	 *
 	 * getTargetIndex
 	 *
 	 * Returns the index of the edge in the FAState
 	 * @return The index of the edge in the FAState
 	 ***************************************************************/
    public int getTargetIndex() { return targetIndex; }

    /***************************************************************
 	 *
 	 * setChars
 	 *
 	 * Sets the characters of this edge to the String passed in.
 	 * @param newChars The new set of characters.
 	 ***************************************************************/
    public void setChars(String newChars) { characters = newChars; }

    /***************************************************************
 	 *
 	 * setTargetIndex
 	 *
 	 * Sets the index in the FAState to the number passed in.
 	 * @param newIn The new target index.
 	 ***************************************************************/
    public void setTargetIndex(int newIn) { targetIndex = newIn; }

    /***************************************************************
 	 *
 	 * contains
 	 *
 	 * Returns True if the characters are in the String passed in,
     * false if not.
 	 * @return Boolean
     * @param Char The String (one character long) to check.
 	 ***************************************************************/
    public boolean contains(String Char)
    {
        return (characters.regionMatches(0, Char, 0, 1));
    }
}
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
 *      Source File:    VariableType.java<br>
 *
 *      Author:         Devin Cook, Matthew Hawkins<br>
 *
 *      Description:    A VariableType holds information specific to this grammar, from the author
 *						to whether or not it is case sensitive.<br>
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
public class VariableType
{
    private String name;
    private boolean visible;
    private String value;
    private String comment;

    /***************************************************************
 	 *
 	 * VariableType
 	 *
 	 * The constructor creates a new Variable.
 	 * @param theName The name of the variable.
     * @param theValue The value of the variable.
     * @param theComment The comment associated with this variable.
     * @param isVisible True if it public, false if not.
 	 ***************************************************************/
    public VariableType(String theName, String theValue, String theComment, boolean isVisible)
    {
        name = theName;
        value = theValue;
        comment = theComment;
        visible = isVisible;
    }

    /***************************************************************
 	 *
 	 * getName
 	 *
 	 * This method will get the name of this variable.
 	 * @return The name of this variable.
 	 ***************************************************************/
    public String getName() { return name; }

    /***************************************************************
 	 *
 	 * getValue
 	 *
 	 * This method will get the value of this variable.
 	 * @return The value of this variable.
 	 ***************************************************************/
    public String getValue() { return value; }

    /***************************************************************
 	 *
 	 * getComment
 	 *
 	 * This method will get the comment of this variable.
 	 * @return The comment of this variable.
 	 ***************************************************************/
    public String getComment() { return comment; }

    /***************************************************************
 	 *
 	 * getVisible
 	 *
 	 * This method will get whether or not this variable is public.
 	 * @return True if it is visible, false if not.
 	 ***************************************************************/
    public boolean getVisible() { return visible; }

    /***************************************************************
 	 *
 	 * setName
 	 *
 	 * This method will set the name of this variable to that passed in.
 	 * @param newName The new name of the variable.
 	 ***************************************************************/
    public void setName(String newName) { name = newName; }

    /***************************************************************
 	 *
 	 * setValue
 	 *
 	 * This method will set the value of this variable to that passed in.
 	 * @param newValue The new value of the variable.
 	 ***************************************************************/
    public void setValue(String newValue) { value = newValue; }

    /***************************************************************
 	 *
 	 * setComment
 	 *
 	 * This method will set the comment of this variable to that passed in.
 	 * @param newComment The new comment of the variable.
 	 ***************************************************************/
    public void setComment(String newComment) { comment = newComment; }
	 
    /***************************************************************
 	 *
 	 * setVisible
 	 *
 	 * This method will set whether or not this variable is visible.
 	 * @param isVisible True if it is visible, false if not.
 	 ***************************************************************/
    public void setVisible(boolean isVisible) { visible = isVisible; }
}
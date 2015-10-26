package goldengine.java;

import java.util.*;

/*
 * Licensed Material - Property of Matthew Hawkins (hawkini@barclays.net)
 *
 * GOLDParser - code ported from VB - Author Devin Cook. All rights reserved.
 *
 * No modifications to this code are allowed without the permission of the author.
 */
/**-------------------------------------------------------------------------------------------<br>
 *
 *      Source File:    VariableList.java<br>
 *
 *      Author:         Devin Cook, Matthew Hawkins<br>
 *
 *      Description:    A holding class for VariableTypes, with special methods needed that a
 *						Vector can not fulfil.<br>
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
 *      IMPORT: java.util<br>
 *
 *-------------------------------------------------------------------------------------------<br>
 */
public class VariableList
{
    private Vector memberList = new Vector();
    private int memberCount = 0;

    /***************************************************************
 	 *
 	 * add
 	 *
 	 * This method will add a new variable to the list. It will do this
     * if and only if there is not an equivalent variable already in
     * the list. If there is not, it will create a new <code>
     * VariableType</code>.
 	 * @param name The name of the variable.
     * @param value The value of the variable.
     * @param comment Any associated comment for the variable.
 	 * @return Will return true if the variable was not found, false
     *              if it was.
 	 ***************************************************************/
    public boolean add(String name, String value, String comment)
    {
        int n = 0;
        boolean found = false;

        while((n < memberCount) & !found)
        {
            VariableType varType = (VariableType)memberList.elementAt(n);
            found = varType.getName().equals(name);
            n++;
        }

        if(!found)
        {
            memberCount++;
            VariableType varT = new VariableType(name, value, comment, true);
            memberList.addElement(varT);
        }

        return !found;
    }

    /***************************************************************
 	 *
 	 * clearValues
 	 *
 	 * This method will set the name of each variable to "".
 	 ***************************************************************/
    public void clearValues()
    {
        Enumeration enumA = memberList.elements();
        while(enumA.hasMoreElements())
        {
            VariableType varType = (VariableType)enumA.nextElement();
            varType.setName("");
        }
    }

    /***************************************************************
 	 *
 	 * count
 	 *
 	 * This method returns the current number of variables.
 	 * @return The number of variables in the list.
 	 ***************************************************************/
    public int count()
    {
        return memberCount;
    }

    /***************************************************************
 	 *
 	 * name
 	 *
 	 * Return the name of the Variable at the specified index. It will
     * do this if and only if the index is not less than 0, and if
     * the index is less than the current number of variables.
 	 * @param index The index of the variable to check.
 	 * @return The name of the variable at the specified index.
 	 ***************************************************************/
    public String name(int index)
    {
        if((index >= 0) & (index < memberCount))
        {
            VariableType varType = (VariableType)memberList.elementAt(index);
            return varType.getName();
        }

        return null;
    }

    /***************************************************************
 	 *
 	 * setValue
 	 *
 	 * This method sets the value of a variable of the same name
     * as that specified.
 	 * @param name The name of the variable to set its value.
     * @param value The new value to set.
 	 ***************************************************************/
    public void setValue(String name, String value)
    {
        int index = variableIndex(name);

        if((index >= 0) & (index < memberCount))
        {
	        VariableType varType = (VariableType)memberList.elementAt(index);
    	    varType.setValue(value);
        }
    }

    /***************************************************************
 	 *
 	 * getValue
 	 *
 	 * This method will return the value of the variable with the same
     * name as that specified.
 	 * @param name The name of the variable wanted.
 	 * @return The value of the variable specified.
 	 ***************************************************************/
    public String getValue(String name)
    {
        int index = variableIndex(name);

        if((index >= 0) & (index < memberCount))
        {
            VariableType varType = (VariableType)memberList.elementAt(index);
            return varType.getValue();
        }

        return null;
    }

    /***************************************************************
 	 *
 	 * variableIndex
 	 *
 	 * This method will return the index number of a variable
     * that has the same name as that specified.
 	 * @param name The name of the variable to get its index.
 	 * @return The index in the list of the variable.
 	 ***************************************************************/
    public int variableIndex(String name)
    {
        int index = -1;
        int n = 0;

        while((n < memberCount) & (index == -1))
        {
            VariableType varType = (VariableType)memberList.elementAt(n);
            if(varType.getName().equals(name))
            {
                index = n;
            }
            n++;
        }

        return index;
    }
}
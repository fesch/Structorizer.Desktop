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
 *      Source File:    SymbolList.java<br>
 *
 *      Author:         Devin Cook, Matthew Hawkins<br>
 *
 *      Description:    A holding class for Symbols, with special methods needed that a Vector
 *						can not fulfil.<br>
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
public class SymbolList
{
    private Vector memberList = new Vector();
    private int memberCount = 0;

    /***************************************************************
 	 *
 	 * reDim
 	 *
 	 * This is the equivalent of the ReDim method in VB. It will
     * resize the Vector to the new size passed in.
 	 * @param newSize The new size required.
 	 ***************************************************************/
    public void reDim(int newSize)
    {
        memberCount = newSize;
        memberList.setSize(newSize);
    }

    /***************************************************************
 	 *
 	 * clear
 	 *
 	 * This method empties the list.
 	 ***************************************************************/
    public void clear()
    {
        memberList.clear();
        memberCount = 0;
    }

    /***************************************************************
 	 *
 	 * count
 	 *
 	 * This method will return the number of entries in the SymbolList.
 	 * @return The current number of symbols.
 	 ***************************************************************/
    public int count()
    {
        return memberCount;
    }

    /***************************************************************
 	 *
 	 * getMember
 	 *
 	 * This method will return the Symbol at the specified index. It
     * will do this if and only if the index is not less than 0, and
     * if the index is less than the current number of symbols.
 	 * @param index The index of the Symbol wanted.
 	 * @return The symbol at the specified index, or null if the index
     *              is invalid.
 	 ***************************************************************/
    public Symbol getMember(int index)
    {
        if((index >= 0) & (index < memberCount))
        {
            return (Symbol)memberList.elementAt(index);
        }

        return null;
    }

    /***************************************************************
 	 *
 	 * getMember
 	 *
 	 * This method will return the Symbol that has an equivalent
     * name in the list.
 	 * @param name The name of the Symbol wanted in the list.
 	 * @return The Symbol with the same name of that passed in.
 	 ***************************************************************/
    public Symbol getMember(String name)
    {
        Enumeration enum1 = memberList.elements();
        while(enum1.hasMoreElements())
        {
            Symbol tmp = (Symbol)enum1.nextElement();
            if(tmp.getName().equals(name))
            {
                return tmp;
            }
        }
        return null;
    }

    /***************************************************************
 	 *
 	 * setMember
 	 *
 	 * This method will set the element at the specified index
     * to the Symbol passed in. It will do this if and only if
     * the index is not less than 0, and if the index is less than
     * the current member count.
 	 * @param index The index to set the Symbol to.
     * @param obj The Symbol to place in the SymbolList.
 	 ***************************************************************/
    public void setMember(int index, Symbol obj)
    {
        if((index >= 0) & (index < memberCount))
        {
            memberList.setElementAt(obj, index);
        }
    }

    /***************************************************************
 	 *
 	 * add
 	 *
 	 * This method adds a symbol to the end of the list.
 	 * @param newItem The Symbol to add.
 	 * @return The index in the list at which the symbol was added.
 	 ***************************************************************/
    public int add(Symbol newItem)
    {
        memberCount++;
        memberList.addElement(newItem);

        return (memberCount - 1);
    }
}
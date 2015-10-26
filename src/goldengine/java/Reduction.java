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
 *      Source File:    Reduction.java<br>
 *
 *      Author:         Devin Cook, Matthew Hawkins<br>
 *
 *      Description:    A representation of a Reduction. An instance of this class will hold
 *						the resulting parse tree once created, and if the source file has been
 *						accepted.<br>
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
public class Reduction
{
    private Vector pTokens = new Vector();
    private int pTokenCount;
    private Rule pParentRule;
    private int pTag;

    /***************************************************************
 	 *
 	 * setTokenCount
 	 *
 	 * This method implicitly sets the number of tokens in this
     * Reduction. If the value is 0 or less, then we clear the tokens
     * in this reduction and set the number of tokens to 0.
 	 * @param value The number of tokens in this reduction.
 	 ***************************************************************/
    public void setTokenCount(int value)
    {
        if(value < 1)
        {
            pTokens.clear();
            pTokenCount = 0;
        }
        else
        {
            pTokenCount = value;
            pTokens.setSize(value);
            for(int i=0; i<value; i++)
            {
                pTokens.addElement(new Object());
            }
        }
    }

    /***************************************************************
 	 *
 	 * getTokenCount
 	 *
 	 * This method returns the number of tokens.
 	 * @return The number of tokens
 	 ***************************************************************/
    public int getTokenCount() { return pTokenCount; }

    /***************************************************************
 	 *
 	 * getParentRule
 	 *
 	 * This method returns the rule associated with this Reduction.
 	 * @return The rule associated with this Reduction.
 	 ***************************************************************/
    public Rule getParentRule() { return pParentRule; }

    /***************************************************************
 	 *
 	 * getTag
 	 *
 	 * Will return the tag associated with this Reduction.
 	 * @return The tag associated with this Reduction.
 	 ***************************************************************/
    public int getTag() { return pTag; }

    /***************************************************************
 	 *
 	 * setParentRule
 	 *
 	 * Will set the Rule of this Reduction to the one passed in.
 	 * @param newRule The parent Rule of this Reduction.
 	 ***************************************************************/
    public void setParentRule(Rule newRule) { pParentRule = newRule; }

    /***************************************************************
 	 *
 	 * setTag
 	 *
 	 * Will set the tag of this Reduction to that passed in.
 	 * @param value The value of the tag.
 	 ***************************************************************/
    public void setTag(int value) { pTag = value; }

    /***************************************************************
 	 *
 	 * getToken
 	 *
 	 * Will retrieve a Token at the specified index. The index
     * specified must be equal or greater than 0 and less than
     * the current number of Tokens.
 	 * @param index The index of the token in this Reduction.
 	 * @return The Token at the specified index.
 	 ***************************************************************/
    public Token getToken(int index)
    {
        if((index >= 0) & (index < pTokenCount))
        {
            return (Token)pTokens.elementAt(index);
        }
        else
        {
            return null;
        }
    }

    /***************************************************************
 	 *
 	 * setToken
 	 *
 	 * Will place a Token at the specified index. It will only do this
     * if the index is greater or equal to 0, and less than the
     * token count.
 	 * @param index The index to place the token at.
     * @param value The token to set at the index.
 	 ***************************************************************/
    public void setToken(int index, Token value)
    {
        if((index >= 0) & (index < pTokenCount))
        {
            pTokens.setElementAt(value, index);
        }
    }
}
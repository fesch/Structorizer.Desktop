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
 *      Source File:    Token.java<br>
 *
 *      Author:         Devin Cook, Matthew Hawkins<br>
 *
 *      Description:    A representation of a token associated with this grammar.<br>
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
public class Token
{
    private int pState;
	private Object pData;
	private Symbol pParentSymbol;

    /***************************************************************
 	 *
 	 * Token
 	 *
 	 * The constructor initialises the data of this Token.
 	 ***************************************************************/
    public Token()
    {
        pData = null;
    }

    /***************************************************************
 	 *
 	 * getState
 	 *
 	 * This method will get the State of this Token.
 	 * @return The State of this Token.
 	 ***************************************************************/
    public int getState()      { return pState; }

    /***************************************************************
 	 *
 	 * getKind
 	 *
 	 * This method will get the kind of this Token. This is contained
     * in the parent symbol, and defined in SymbolTypeConstants.
 	 * @return The kind of this Token.
 	 ***************************************************************/
    public int getKind()       { return pParentSymbol.getKind(); }

    /***************************************************************
 	 *
 	 * getTableIndex
 	 *
 	 * This method will get the table index of this Token.
 	 * @return The table index of this Token.
 	 ***************************************************************/
    public int getTableIndex() { return pParentSymbol.getTableIndex(); }

    /***************************************************************
 	 *
 	 * getData
 	 *
 	 * This method will get the data of this Token.
 	 * @return The data of this Token.
 	 ***************************************************************/
    public Object getData()    { return pData; }

    /***************************************************************
 	 *
 	 * getText
 	 *
 	 * This method will get the text of this Token. This is the
     * text in the parent symbol getText() method.
 	 * @return The text of this Token.
 	 ***************************************************************/
    public String getText()    { return pParentSymbol.getText(); }

    /***************************************************************
 	 *
 	 * getName
 	 *
 	 * This method will get the name of this Token. This is the
     * name of the parent symbol in the getName() method.
 	 * @return The name of this Token.
 	 ***************************************************************/
    public String getName()    { return pParentSymbol.getName(); }

    /***************************************************************
 	 *
 	 * getPSymbol
 	 *
 	 * This method will get the parent symbol of this Token.
 	 * @return The parent symbol of this Token.
 	 ***************************************************************/
    public Symbol getPSymbol() { return pParentSymbol; }

    /***************************************************************
 	 *
 	 * setState
 	 *
 	 * This method will set the state of this token to that passed in.
 	 * @param newState The new state of the token.
 	 ***************************************************************/
    public void setState(int newState) { pState = newState; }

    /***************************************************************
 	 *
 	 * setData
 	 *
 	 * This method will set the data of this token to that passed in.
 	 * @param value The new data of the token.
 	 ***************************************************************/
    public void setData(Object value)  { pData = value; }

    /***************************************************************
 	 *
 	 * setParentSymbol
 	 *
 	 * This method will set the parent symbol of this token to that passed in.
 	 * @param theSymbol The new parent symbol of the token.
 	 ***************************************************************/
    public void setParentSymbol(Symbol theSymbol) { pParentSymbol = theSymbol; }
}
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
 *      Source File:    LRAction.java<br>
 *
 *      Author:         Devin Cook, Matthew Hawkins<br>
 *
 *      Description:    An LALR Action.<br>
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
public class LRAction
{
    private Symbol pSymbol;

    /***************************************************************
 	 *
 	 * actionConstant
 	 *
 	 * This is the action that should be taken for this instance.
 	 ***************************************************************/
    public int actionConstant;

    /***************************************************************
 	 *
 	 * value
 	 *
 	 * The value of the action.
 	 ***************************************************************/
    public int value;      //shift to state, reduce rule, goto state

    /***************************************************************
 	 *
 	 * setSymbol
 	 *
 	 * This method will set the symbol of this action.
 	 * @param sym The symbol to set for this action.
 	 ***************************************************************/
    public void setSymbol(Symbol sym) { pSymbol = sym; }

    /***************************************************************
 	 *
 	 * getSymbolIndex
 	 *
 	 * This method will return the index of the smybol in the symbol
     * table.
 	 * @return The index of the smybol in the symbol table.
 	 ***************************************************************/
    public int getSymbolIndex() { return pSymbol.getTableIndex(); }

    /***************************************************************
 	 *
 	 * getSymbol
 	 *
 	 * This method will return the symbol associated with this action.
 	 * @return The symbol associated with this action.
 	 ***************************************************************/
    public Symbol getSymbol() { return pSymbol; }
}
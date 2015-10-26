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
 *      Source File:    Symbol.java<br>
 *
 *      Author:         Devin Cook, Matthew Hawkins<br>
 *
 *      Description:    A representation of a symbol associated with this grammar.<br>
 *
 *
 *-------------------------------------------------------------------------------------------<br>
 *
 *      Revision List<br>
 *<pre>
 *      Author          Version         Description
 *      ------          -------         -----------
 *      MPH             1.0             First Issue
 *      MPH             1.1             Added isEqual method</pre><br>
 *
 *-------------------------------------------------------------------------------------------<br>
 *
 *      IMPORT: NONE<br>
 *
 *-------------------------------------------------------------------------------------------<br>
 */
public class Symbol implements SymbolTypeConstants
{
    private String pName;
	private String pPattern;
    private int pKind;
	private boolean pVariableLength;
    private int pTableIndex;
    private final String kQuotedChars = "|-+*?()[]{}<>!";

    /***************************************************************
 	 *
 	 * Symbol
 	 *
 	 * The constructor simply initialises the table index.
 	 ***************************************************************/
    public Symbol()
    {
        pTableIndex = -1;
    }

    /***************************************************************
 	 *
 	 * getName
 	 *
 	 * This method gets the name of the symbol.
 	 * @return The name of the symbol.
 	 ***************************************************************/
    public String getName()    { return pName; }

    /***************************************************************
 	 *
 	 * getKind
 	 *
 	 * This method gets the kind of symbol (defined in SymbolTypeConstants).
 	 * @return The kind of symbol.
 	 ***************************************************************/
    public int getKind()       { return pKind; }

    /***************************************************************
 	 *
 	 * getTableIndex
 	 *
 	 * This method gets the table index of this symbol.
 	 * @return The table index of this symbol.
 	 ***************************************************************/
    public int getTableIndex() { return pTableIndex; }

    /***************************************************************
 	 *
 	 * setName
 	 *
 	 * This method sets the name of the symbol.
 	 * @param newName The name of the symbol.
 	 ***************************************************************/
    public void setName(String newName)   { pName = newName; }

    /***************************************************************
 	 *
 	 * setKind
 	 *
 	 * This method sets the kind of the symbol (defined in SymbolTypeConstants).
 	 * @param newKind <parameter description>
 	 ***************************************************************/
    public void setKind(int newKind)      { pKind = newKind; }

    /***************************************************************
 	 *
 	 * setTableIndex
 	 *
 	 * This method sets the table index of the symbol.
 	 * @param newTab The kind of symbol.
 	 ***************************************************************/
    public void setTableIndex(int newTab) { pTableIndex = newTab; }

    /***************************************************************
 	 *
 	 * getText
 	 *
 	 * This method will create a text representation of this Symbol.
     * What text is returned depends on the kind of Symbol.
     * If it is a Non-Terminal, angular brackets are placed before
     * and after, if it is a Terminal, then it is formatted.
     * Everything else is placed in parenthesis.
 	 * @return The String representation of this Symbol.
 	 ***************************************************************/
    public String getText()
    {
        String str;

        switch(pKind)
        {
            case symbolTypeNonterminal:
                str = "<" + pName + ">";
                break;

            case symbolTypeTerminal:
                str = patternFormat(pName);
                break;

            default:
                str = "(" + pName + ")";
        }

        return str;
    }

    // this method is not accessible. It will create a formatted String
    // from that passed in.
    private String patternFormat(String source)
    {
        String result = "", ch;
        int in34 = 34;
        char ch34 = (char)in34;

        for(int i=0; i<source.length(); i++)
        {
            ch = "" + source.charAt(i);

            if(ch == "'")
            {
                ch = "''";
            }
            else
            {
                if(ch.regionMatches(true, 0, kQuotedChars, 0, 1) | ch.charAt(0) == ch34)
                {
                    ch = "'" + ch + "'";
                }
            }

            result += ch;
        }

        return result;
    }

    /***************************************************************
 	 *
 	 * isEqual #ver1.1#
 	 *
 	 * This method will check equality of two Symbols - this and the one passed in.
     * @param other The symbol to check against this one.
 	 * @return True if it is equal, false if not.
 	 ***************************************************************/
    public boolean isEqual(Symbol other)
    {
        if((pName.equals(other.getName())) & (pKind == other.getKind()))
        {
            return true;
        }

        return false;
    }
}
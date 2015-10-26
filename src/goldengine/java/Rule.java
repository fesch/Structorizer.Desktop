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
 *      Source File:    Rule.java<br>
 *
 *      Author:         Devin Cook, Matthew Hawkins<br>
 *
 *      Description:    A representation of a rule associated with this grammar.<br>
 *
 *
 *-------------------------------------------------------------------------------------------<br>
 *
 *      Revision List<br>
 *<pre>
 *      Author          Version         Description
 *      ------          -------         -----------
 *      MPH             1.0             First Issue
 *      MPH             1.1             Added the TrimReductions property and required logic</pre><br>
 *
 *-------------------------------------------------------------------------------------------<br>
 *
 *      IMPORT: NONE<br>
 *
 *-------------------------------------------------------------------------------------------<br>
 */
public class Rule implements SymbolTypeConstants
{
    private Symbol pRuleNonterminal;
	private SymbolList pRuleSymbols; //This consist the body of the rule
	private int pTableIndex;

    /***************************************************************
 	 *
 	 * Rule
 	 *
 	 * Constructor: This constructor initialises this rule and creates
     * a new SymbolList and the table index.
 	 ***************************************************************/
    public Rule()
    {
        pRuleSymbols = new SymbolList();
        pTableIndex = -1;
    }

    /***************************************************************
 	 *
 	 * getTableIndex
 	 *
 	 * Will return what index the table index in this Rule is at.
 	 * @return The table index.
 	 ***************************************************************/
    public int getTableIndex() { return pTableIndex; }

    /***************************************************************
 	 *
 	 * getSymbolCount
 	 *
 	 * Will return how many symbols are contained in this Rule.
 	 * @return The number of symbols.
 	 ***************************************************************/
    public int getSymbolCount() { return pRuleSymbols.count(); }

    /***************************************************************
 	 *
 	 * getRuleNonTerminal
 	 *
 	 * Will return the Non-Terminal Symbol associated with this Rule.
 	 * @return The Non-Terminal of this Rule.
 	 ***************************************************************/
    public Symbol getRuleNonTerminal() { return pRuleNonterminal; }

    /***************************************************************
 	 *
 	 * setTableIndex
 	 *
 	 * Will set the current table index to that passed in.
 	 * @param index The table index wanted.
 	 ***************************************************************/
    public void setTableIndex(int index) { pTableIndex = index; }

    /***************************************************************
 	 *
 	 * setRuleNonTerminal
 	 *
 	 * Will setup the Non-Terminal symbol to that passed in.
 	 * @param nonTerminal The Non-Terminal Symbol.
 	 ***************************************************************/
    public void setRuleNonTerminal(Symbol nonTerminal)
		{ pRuleNonterminal = nonTerminal; }

    /***************************************************************
 	 *
 	 * getSymbols
 	 *
 	 * Will return the Symbol at the index specified. It will do this
     * if and only if the index is not less than 0 and greater than
     * the symbol count.
 	 * @param index The index of the symbol wanted.
 	 * @return The symbol at the specified index.
 	 ***************************************************************/
    public Symbol getSymbols(int index)
    {
        if((index >= 0) & (index < pRuleSymbols.count()))
        {
            return pRuleSymbols.getMember(index);
        }

        return null;
    }

    /***************************************************************
 	 *
 	 * name
 	 *
 	 * Will return a String consisting of the Rules name. This
     * is in the format <code>"< 'name of non-terminal' >"</code>.
 	 * @return The String representing this Rule,
 	 ***************************************************************/
    public String name()
    {
        return "<" + pRuleNonterminal.getName() + ">";
    }

    /***************************************************************
 	 *
 	 * definition
 	 *
 	 * This method will return the right hand side of
     * the rule, It does this by concatenating all the Symbols in the Symbol list.
 	 * @return The String representing the definition of this Rule.
 	 ***************************************************************/
    public String definition()
    {
        String str = "";

        for(int i = 0; i < pRuleSymbols.count(); i++)
        {
            str += pRuleSymbols.getMember(i).getText() + " ";
        }

        return str.trim();
    }

    /***************************************************************
 	 *
 	 * addItem
 	 *
 	 * This method will add a symbol to the Symbol list.
 	 * @param item The Symbol to add.
 	 ***************************************************************/
    public void addItem(Symbol item)
    {
        pRuleSymbols.add(item);
    }

    /***************************************************************
 	 *
 	 * getText
 	 *
 	 * This method uses the method name() and definiton() to create
     * a String representing the entirety of this Rule.
 	 * @return The entire Rule in readable format.
 	 ***************************************************************/
    public String getText()
    {
        return name() + " ::= " + definition();
    }

    /***************************************************************
 	 *
 	 * containsOneNonTerminal #ver1.1#
 	 *
 	 * This method will check to see if the rule contains a non terminal.
 	 * @return True if it does contain one non terminal, false if not.
 	 ***************************************************************/
    public boolean containsOneNonTerminal()
    {
        if(pRuleSymbols.count() == 1)
        {
            if(pRuleSymbols.getMember(0).getKind() == symbolTypeNonterminal)
            {
                return true;
            }
        }

        return false;
    }

    /***************************************************************
 	 *
 	 * isEqual #ver1.1#
 	 *
 	 * This method will check equality of two Rules - this and the one passed in.
     * @param secondRule The rule to check against this one.
 	 * @return True if it is equal, false if not.
 	 ***************************************************************/
    public boolean isEqual(Rule secondRule)
    {
        boolean equal = false;
        int n = 0;

        if((pRuleSymbols.count() == secondRule.getSymbolCount()) &
           (pRuleNonterminal.isEqual(secondRule.getRuleNonTerminal())))
        {
            equal = true;
            while(equal & (n < pRuleSymbols.count()))
            {
                equal = pRuleSymbols.getMember(n).isEqual(secondRule.getSymbols(n));
                n++;
            }
        }

        return equal;
    }
}
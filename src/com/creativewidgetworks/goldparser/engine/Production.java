package com.creativewidgetworks.goldparser.engine;

import com.creativewidgetworks.goldparser.engine.enums.SymbolType;

/**
 * Production 
 *
 * This class is used to represent the logical structures of the grammar. Productions consist
 * of a head containing a nonterminal followed by a series of both nonterminals and terminals.
 *
 * <br>Dependencies:
 * <ul> 
 * <li>{@link Symbol}</li>
 * <li>{@link SymbolType}</li>
 * </ul>
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0
 */
public class Production {
    private Symbol head;
    private SymbolList handle;
    private int tableIndex;
   
    public Production(Symbol head, int tableIndex) {
        this.head = head;
        this.tableIndex = tableIndex;
        this.handle = new SymbolList();
    }

    public Symbol getHead() {
        return head;
    }

    public SymbolList getHandle() {
        if (handle == null) {
            handle = new SymbolList();
        }
        return handle;
    }

    public int getTableIndex() {
        return tableIndex;
    }

    public void setHead(Symbol head) {
        this.head = head;
    }

    public void setHandle(SymbolList handle) {
        this.handle = handle;
    }

    public void setTableIndex(int tableIndex) {
        this.tableIndex = tableIndex;
    }

    /*----------------------------------------------------------------------------*/

    public boolean containsOneNonTerminal() {
        if (getHandle().size() == 1) {
            return handle.get(0).getType().equals(SymbolType.NON_TERMINAL);
        }
        return false;
    }
    
    /*----------------------------------------------------------------------------*/

    @Override
    public String toString() {
        return getHead() + " ::= " + getHandle();
    }
    
}
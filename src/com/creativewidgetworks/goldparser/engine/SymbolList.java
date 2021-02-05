
package com.creativewidgetworks.goldparser.engine;

import java.util.ArrayList;

/**
 * SymbolList 
 *
 * This class manages a list of Symbols.

 * <br>Dependencies:
 * <ul> 
 * <li>{@link Symbol}</li>
 * </ul>
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0
 */
@SuppressWarnings("serial")
public class SymbolList extends ArrayList<Symbol> {
    
    // Symbol keys for comment processing
    public static final String SYMBOL_COMMENT       = "COMMENT";
    public static final String SYMBOL_COMMENT_BLOCK = "COMMENT_BLOCK";
    public static final String SYMBOL_COMMENT_LINE  = "COMMENT_LINE";
    
    
    public SymbolList() {
        // default constructor
    }
    
    /**
     * Constructor that establishes the size of the list and creates placeholder
     * objects so the list can be accessed in a "random" fashion when setting
     * items.
     * @param size
     */    
    public SymbolList(int size) {
        super(size);
        for (int i = 0; i < size; i++) {
            add(null);
        }
    }
    
    /*----------------------------------------------------------------------------*/

    /**
     * Searches the list of symbols for the matching name
     * @param name of symbol to find. case insensitive
     * @return Symbol or null if not found
     */
    public Symbol findByName(String name) {
        for (Symbol symbol : this) {
            if (symbol.name.equalsIgnoreCase(name)) {
                return symbol;
            }
        }
        return null;
    }
    
    /*----------------------------------------------------------------------------*/
    
    @Override 
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size(); i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(get(i));
        }
        return sb.toString();
    }

}
package com.creativewidgetworks.goldparser.engine;

import com.creativewidgetworks.goldparser.engine.enums.SymbolType;

/**
 * Symbol  
 * 
 * This class is used to store the symbol data used by the Deterministic
 * Finite Automata (DFA) and LALR Parser. Symbols can be either
 * terminals (which represent a class of tokens - such as identifiers) or
 * nonterminals (which represent the rules and structures of the grammar).
 * 
 * Terminal symbols fall into several categories for use by the GOLD Parser
 * Engine which are contained in the SymbolType enumeration class.
 * 
 * <br>Dependencies:
 * <ul> 
 * <li>{@link SymbolType}</li>
 * <li>{@link Group}</li>
 * </ul>
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0 
 */
public class Symbol {
    protected String name;
    protected SymbolType type;
    protected int tableIndex;
    protected Group group;

    public Symbol() {
        //
    }
    
    public Symbol(String name, SymbolType type, int tableIndex) {
        this.name = name;
        this.type = type;
        this.tableIndex = tableIndex;
    }
    
    public Group getGroup() {
        return group;
    }
    
    public String getName() {
        return name;
    }

    public SymbolType getType() {
        return type;
    }

    public int getTableIndex() {
        return tableIndex;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public void setType(SymbolType type) {
        this.type = type;
    }

    public void setTableIndex(int tableIndex) {
        this.tableIndex = tableIndex;
    }

    /*----------------------------------------------------------------------------*/

    @Override
    public String toString() {
        return toString(false);
    }
    
    public String toString(boolean alwaysDelimitTerminals) {
        StringBuilder sb = new StringBuilder();
        
        if (type != null) {
            switch (type) {
                case NON_TERMINAL:
                    sb.append("<").append(name).append(">");
                    break;
                
                case CONTENT:
                    sb.append(literalFormat(name, alwaysDelimitTerminals));
                    break;
                
                default:
                    sb.append("(").append(name).append(")");
                    break;                
            }
        } else {
            sb.append("<not initialized>");
        }

        return sb.toString();
    }
    
    /*----------------------------------------------------------------------------*/

    private String literalFormat(String source, boolean forceDelimiter) {
        if (source == null) {
            return "null";
        } else if (source.equals("'")) {
            return "''";
        } else {
            // Quote anything other than identifiers 
            if (!forceDelimiter) {
                forceDelimiter = source.length() == 0 || !Character.isLetter(source.charAt(0));
                if (!forceDelimiter) {
                    for (int i = 1; !forceDelimiter && i < source.length(); i++) {
                        char c = source.charAt(i);
                        forceDelimiter = !(Character.isLetter(c) || c == '.' || c == '_' || c == '-');
                    }                    
                }
            }
            return forceDelimiter ? "'" + source + "'" : source;
        }
    }
    
}
package com.creativewidgetworks.goldparser.engine;

import java.util.ArrayList;

import com.creativewidgetworks.goldparser.engine.enums.LRActionType;

/**
 * LRState
 * This object maintains a list of LRAction instance and provides
 * the ability to locate a LRAction by its Symbol
 *
 * <br>Dependencies:
 * <ul> 
 * <li>{@link LRAction}</li>
 * <li>{@link Symbol}</li>
 * </ul>
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0
 */
@SuppressWarnings("serial")
public class LRState extends ArrayList<LRAction> {
    
    public static final int INITIAL_STATE = 0;
    
    public static final LRAction LRACTION_UNDEFINED = new LRAction(new Symbol(), LRActionType.UNDEFINED, -1);

    /**
     * Default constructor
     */
    public LRState() {
        // default constructor
    }
    
    /**
     * Constructor that establishes the size of the list and creates placeholder
     * objects so the list can be accessed in a "random" fashion when setting
     * items.
     * @param size
     */
    public LRState(int size) {
        super(size);
        for (int i = 0; i < size; i++) {
            add(null);
        }        
    }
    
    /*----------------------------------------------------------------------------*/

    /**
     * Returns the LRAction that contains symbol 
     * @param symbol to use when searching the LRAction list
     * @return LRAction containing symbol or null if not found
     */
    public LRAction find(Symbol symbol) {
        LRAction result = LRACTION_UNDEFINED;
        
        if (symbol != null) {
            for (int i = 0; i < size(); i++) {
                LRAction action = get(i);
                if (action.getSymbol().tableIndex == symbol.tableIndex) {
                    result = action;
                    break;
                }
            }       
        }
        
        return result;
    }

}

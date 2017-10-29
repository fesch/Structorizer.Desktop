package com.creativewidgetworks.goldparser.engine;

import java.util.ArrayList;

/**
 * LRStateList
 *
 * Manages a list of LRState objects.
 *
 * <br>Dependencies:
 * <ul> 
 * <li>{@link LRState}</li>
 * </ul>
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0 
 */
public class LRStateList extends ArrayList<LRState> {

    private int initialState;
    
    /**
     * Constructor that establishes the size of the list and creates placeholder
     * objects so the list can be accessed in a "random" fashion when setting
     * items.
     * @param size
     */    
    public LRStateList(int size) {
        super(size);
        initialState = 0;
        for (int i = 0; i < size; i++) {
            add(null);
        }
    }    
    
    public int getInitialState() {
        return initialState;
    }
    
    public void setInitialState(int initialState) {
        this.initialState = initialState;
    }
}

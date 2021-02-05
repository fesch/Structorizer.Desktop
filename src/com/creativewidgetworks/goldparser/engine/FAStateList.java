package com.creativewidgetworks.goldparser.engine;

import java.util.ArrayList;

/**
 * FAStateList
 *
 * Manages a list of FAState instances. 
 *
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
public class FAStateList extends ArrayList<FAState> {
    private int initialState;
    private Symbol errorSymbol;
    
    public FAStateList(int size) {
        super(size);
        for (int i = 0; i < size; i++) {
            add(null);
        }
    }

    public int getInitialState() {
        return initialState;
    }
    
    public Symbol getErrorSymbol() {
        return errorSymbol;
    }
    
    public void setInitialState(int initialState) {
        this.initialState = initialState;
    }
    
    public void setErrorSymbol(Symbol errorSymbol) {
        this.errorSymbol = errorSymbol;
    }
}

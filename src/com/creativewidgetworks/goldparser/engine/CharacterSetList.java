package com.creativewidgetworks.goldparser.engine;

import java.util.ArrayList;

/**
 * CharacterSetList
 *
 * Manages a list of CharacterSet objects.
 *
 * <br>Dependencies:
 * <ul>
 * <li>{@link CharacterSet}</li>
 * </ul>
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0 
 */
@SuppressWarnings("serial")
public class CharacterSetList extends ArrayList<CharacterSet> {

    /**
     * Constructor that establishes the size of the list and creates placeholder
     * objects so the list can be accessed in a "random" fashion when setting
     * items.
     * @param size
     */    
    public CharacterSetList(int size) {
        super(size);
        for (int i = 0; i < size; i++) {
            add(null);
        }
    }    
}

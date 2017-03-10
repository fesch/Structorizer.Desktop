package com.creativewidgetworks.goldparser.engine;

import java.util.ArrayList;

/**
 * GroupList
 *
 * Manages a list of Group objects.
 *
 * <br>Dependencies:
 * <ul> 
 * <li>{@link Group}</li>
 * </ul>
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0 
 */
public class GroupList extends ArrayList<Group> {

    public GroupList() {
        // default constructor
        super();
    }
    
    /**
     * Constructor that establishes the size of the list and creates placeholder
     * objects so the list can be accessed in a "random" fashion when setting
     * items.
     * @param size
     */    
    public GroupList(int size) {
        super(size);
        for (int i = 0; i < size; i++) {
            add(null);
        }
    }    
}

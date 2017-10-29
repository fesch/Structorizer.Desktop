package com.creativewidgetworks.goldparser.engine;

import java.util.ArrayList;

/**
 * CharacterSet 
 *
 * Manages a list of CharacterRange objects.
 *
 * <br>Dependencies:
 * <ul>
 * <li>{@link CharacterRange}</li>
 * </ul>
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0 
 */
public class CharacterSet extends ArrayList<CharacterRange> {

    /**
     * Default constructor
     */
    public CharacterSet() {
        // default constructor
    }
    
    /**
     * Constructor that establishes the size of the list and creates placeholder
     * objects so the list can be accessed in a "random" fashion when setting
     * items.
     * @param size
     */    
    public CharacterSet(int size) {
        super(size);
        for (int i = 0; i < size; i++) {
            add(null);
        }
    }    
    
    /*----------------------------------------------------------------------------*/

    /**
    * This procedure searches the set to determine if the CharCode is in one
    * of the ranges and therefore, the set.
    * 
    * The number of ranges in any given set are relatively small - rarely 
    * exceeding 10 total. As a result, a simple linear search is sufficient 
    * rather than a binary search. In fact, a binary search overhead might
    * slow down the search!

    * @param charCode the search character
    * @return true if the character is in the set.
    */
    public boolean contains(int charCode) {
        boolean found = false;
        for (int i = 0; !found && i < size(); i++) {
            CharacterRange range = get(i);
            if (range.characterSet != null) {
                // Version 1 CGT
                found = range.characterSet.contains(String.valueOf((char)charCode));
            } else {
                // Version 5 EGT
                found = charCode >= range.start && charCode <= range.end;
            }
        }
        return found;
    }

}

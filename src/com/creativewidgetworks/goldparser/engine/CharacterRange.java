package com.creativewidgetworks.goldparser.engine;


/**
 * CharacterRange 
 *
 * Represents offsets of a character range.
 *
 * <br>Dependencies: None
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0 
 */
public class CharacterRange {
    int start;
    int end;
    String characterSet;

    /**
     * Constructor for the V1 CGT format
     * @param characterSet set of characters valid for the edge
     */
    public CharacterRange(String characterSet) {
        this.characterSet = characterSet;
    }
    
    /**
     * Constructor for the V2 EGT format
     * @param start character value
     * @param end character value
     */
    public CharacterRange(int start, int end) {
        this.start = start;
        this.end = end;
    }
    
    public String getCharacters() {
        return characterSet;
    }
    
    public int getStart() {
        return start;
    }
    
    public int getEnd() {
        return end;
    }
    
}

package com.creativewidgetworks.goldparser.engine;

/**
 * FAEdge - This class is used to represent an edge
 * 
 * Each state in the Determinstic Finite Automata contains multiple edges which
 * link to other states in the automata.
 *
 * <br>Dependencies: None
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0 
 */
public class FAEdge {
    private CharacterSet chars;
    private int target;

    public FAEdge(CharacterSet chars, int target) {
        this.chars = chars;
        this.target = target;
    }

    public CharacterSet getChars() {
        return chars;
    }

    public int getTarget() {
        return target;
    }

    public void setChars(CharacterSet chars) {
        this.chars = chars;
    }

    public void setTarget(int target) {
        this.target = target;
    }

}
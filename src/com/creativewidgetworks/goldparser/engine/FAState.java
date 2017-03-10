package com.creativewidgetworks.goldparser.engine;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * FAState 
 *
 * Represents a state in the Deterministic Finite Automata which is used by the tokenizer.
 *
 * <br>Dependencies:
 * <ul> 
 * <li>{@link FAEdge}</li>
 * <li>{@link Symbol}</li>
 * </ul>
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0 
 */
public class FAState {

    private List<FAEdge> edges;
    public Symbol accept;

    public FAState() {
        // default constructor
    }
    
    public FAState(Symbol symbol) {
        edges = new LinkedList<FAEdge>();
        this.accept = symbol;
    }

    public Symbol getAccept() {
        return accept;
    }
    
    public List<FAEdge> getEdges() {
        if (edges == null) {
            edges = new ArrayList<FAEdge>();
        }
        return edges;
    }
}

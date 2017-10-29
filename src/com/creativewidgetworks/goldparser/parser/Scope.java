package com.creativewidgetworks.goldparser.parser;

import java.util.Map;
import java.util.TreeMap;

/**
 * Scope 
 *
 * Represents a scope of a program including variables local to the 
 * scope and the parent that contains this scope.
 *
 * <br>Dependencies:
 * <ul> 
 * <li>{@link Variable}</li>
 * </ul>
 *
 * @author Ralph Iden (http://www.creativewidgetworks.com)
 * @version 5.0.0 
 */
public class Scope {
    private final String name;
    private Scope parent;
    private Map<String, Variable> variables;

    public static final String GLOBAL_SCOPE = "GLOBAL";

    public Scope() {
        this(GLOBAL_SCOPE);
    }

    public Scope(String scopeName) {
        name = scopeName;
    }

    public Scope(String scopeName, Scope parentScope) {
        this(scopeName);
        parent = parentScope;
    }

    /**
     * Get the scope's name
     * @return the name of the scope
     */
    public String getName() {
        return name;
    }

    /**
     * Get the parent scope
     * @return the parent scope or null if the root (GLOBAL) scope has been reached
     */
    public Scope getParent() {
        return parent;
    }

    /**
     * Retrieve the map containing the variables in use by the scope.
     * @return Map<String, Variable> of all the variables in use.
     */
    public Map<String, Variable> getVariables() {
        if (variables == null) {
            variables = new TreeMap<String, Variable>();
        }
        return variables;
    }
   
}
package com.creativewidgetworks.goldparser.engine;

import java.util.ArrayList;

import com.creativewidgetworks.goldparser.parser.Variable;

/**
 * Reduction 
 *
 * This class is used by the engine to hold a reduced rule. A reduction contains 
 * a list of Tokens corresponding to the the rule it represents. This class is 
 * important since it is used to store the actual source program parsed by the Engine

 * <br>Dependencies:
 * <ul> 
 * <li>{@link Production}</li>
 * <li>{@link Token}</li>
 * </ul>
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0
 */
@SuppressWarnings("serial")
public class Reduction extends ArrayList<Token> {
    private Production parent;
    private Variable value;
    
    public Reduction() {
        // default constructor
    }
    
    /**
     * Constructor that establishes the size of the list and creates placeholder
     * objects so the list can be accessed in a "random" fashion when setting
     * items.
     * @param size
     */    
    public Reduction(int size) {
        super(size);
        for (int i = 0; i < size; i++) {
            add(null);
        }
    }
    
    /*----------------------------------------------------------------------------*/

    /**
     * This method is called when the parsed program tree is executed. Rule handler
     * classes override this method as necessary to implement code generation or
     * execution strategies
     * @throws ParserException
     */
    public void execute() throws ParserException {
        // Base implementation does nothing, override as necessary
    }
    
    /*----------------------------------------------------------------------------*/
    
    public Production getParent() {
        return parent;
    }

    public void setParent(Production parent) {
        this.parent = parent;
    }

    public Variable getValue() {
        return value;
    }

    public void setValue(Variable value) {
        this.value = value;
    }
    
}
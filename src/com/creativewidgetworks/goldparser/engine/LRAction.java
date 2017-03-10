package com.creativewidgetworks.goldparser.engine;

import com.creativewidgetworks.goldparser.engine.enums.LRActionType;

/**
 * LRAction 
 *
 * <br>Dependencies:
 * <ul> 
 * <li>{@link LRActionType}</li>
 * <li>{@link Symbol}</li>
 * </ul>
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0
 */
public class LRAction {
    private final Symbol symbol;
    private final LRActionType type;
    private final int value;

    public LRAction(Symbol symbol, LRActionType type, int value) {
        this.symbol = symbol;
        this.type = type;
        this.value = value;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public LRActionType getType() {
        return type;
    }

    public int getValue() {
        return value;
    }
}
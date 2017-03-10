package com.creativewidgetworks.goldparser.engine.enums;

/**
 * LRActionType 
 *
 * LRAction type enumeration
 *
 * <br>Dependencies: None
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0 
 */
public enum LRActionType {
    UNDEFINED   (-1),
    SHIFT       (1),     // Shift a symbol and goto a state
    REDUCE      (2),     // Reduce by a specified rule
    GOTO        (3),     // Goto a state on reduction
    ACCEPT      (4),     // Input successfully parsed
    ERROR       (5);     // Programmers see this often!

    private final int enumCode;

    LRActionType(int code) {
        this.enumCode = code;
    }

    public int getCode() {
        return enumCode;
    }

    public static LRActionType getLRActionType(int code) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].enumCode == code) {
                return values()[i];
            }
        }
        return UNDEFINED;
    }
}

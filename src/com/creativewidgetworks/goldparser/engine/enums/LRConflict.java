package com.creativewidgetworks.goldparser.engine.enums;

/**
 * LRConflict 
 *
 * LRConflict type enumeration
 *
 * <br>Dependencies: None
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0 
 */
public enum LRConflict {
    UNDEFINED     (-1),
    SHIFT_SHIFT   (1),  // Never happens
    SHIFT_REDUCE  (2),
    REDUCE_REDUCE (3),
    ACCEPT_REDUCE (4),  // Never happens with this implementation
    NONE          (5);

    private final int enumCode;

    LRConflict(int code) {
        this.enumCode = code;
    }

    public int getCode() {
        return enumCode;
    }

    public static LRConflict getLRConflict(int code) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].enumCode == code) {
                return values()[i];
            }
        }
        return UNDEFINED;
    }
}

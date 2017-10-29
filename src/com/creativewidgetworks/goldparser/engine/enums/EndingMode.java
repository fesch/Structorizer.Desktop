package com.creativewidgetworks.goldparser.engine.enums;

/**
 * EndingMode 
 *
 * Group ending mode type enumeration
 *
 * <br>Dependencies: None
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0 
 */
public enum EndingMode {
    OPEN      (0),
    CLOSED    (1),
    UNDEFINED (-1);
    
    private final int enumCode;

    EndingMode(int code) {
        this.enumCode = code;
    }

    public int getCode() {
        return enumCode;
    }

    public static EndingMode getEndingMode(int code) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].enumCode == code) {
                return values()[i];
            }
        }
        return UNDEFINED;
    }

}

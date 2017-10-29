package com.creativewidgetworks.goldparser.engine.enums;

/**
 * AdvanceMode 
 *
 * Group advance mode type enumeration
 *
 * <br>Dependencies: None
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0 
 */
public enum AdvanceMode {
    TOKEN     (0),
    CHARACTER (1),
    UNDEFINED (-1);
    
    private final int enumCode;

    AdvanceMode(int code) {
        this.enumCode = code;
    }

    public int getCode() {
        return enumCode;
    }

    public static AdvanceMode getAdvanceMode(int code) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].enumCode == code) {
                return values()[i];
            }
        }
        return UNDEFINED;
    }

}

package com.creativewidgetworks.goldparser.engine.enums;

/**
 * EntryType 
 *
 * CGT data type enumeration
 *
 * <br>Dependencies: None
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0 
 */
public enum EntryType {
    UNDEFINED   (-1),
    ERROR       (0),    
    BOOLEAN     (66),   // B 1 byte 0=false, 1=true
    EMPTY       (69),   // E
    UINT16      (73),   // I unsigned 16 bit integer
    STRING      (83),   // S Unicode
    BYTE        (98);   // b

    private final int enumCode;

    EntryType(int code) {
        this.enumCode = code;
    }

    public int getCode() {
        return enumCode;
    }

    public static EntryType getEntryType(int code) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].enumCode == code) {
                return values()[i];
            }
        }
        return UNDEFINED;
    }

}


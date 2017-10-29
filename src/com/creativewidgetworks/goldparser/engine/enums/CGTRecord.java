package com.creativewidgetworks.goldparser.engine.enums;

/**
 * CGTRecord 
 *
 * CGT record type enumeration
 *
 * <br>Dependencies: None
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0 
 */
public enum CGTRecord {
    INITIALSTATES (73),  // I
    SYMBOL        (83),  // S
    RULE          (82),  // R Rule/related productions
    DFASTATE      (68),  // D
    LRSTATE       (76),  // L
    PARAMETER     (80),  // P (version 1 parameter block)
    PROPERTY      (112), // p (version 5 key/value property)
    CHARSET       (67),  // C (version 1 character sets)
    CHARRANGES    (99),  // c (version 5 character range)
    GROUP         (103), // g
    GROUPNESTING  (110), // n
    COUNTS        (84),  // T Table counts
    COUNTS5       (116), // t Table counts
    
    UNDEFINED     (-1);
    
    private final int enumCode;

    CGTRecord(int code) {
        this.enumCode = code;
    }

    public int getCode() {
        return enumCode;
    }

    public static CGTRecord getCGTRecord(int code) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].enumCode == code) {
                return values()[i];
            }
        }
        return UNDEFINED;
    }

}

package com.creativewidgetworks.goldparser.engine.enums;

/**
 * SymbolType 
 *
 * <br>Dependencies: None
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0 
 */
public enum SymbolType {
    UNDEFINED     (-1),
    NON_TERMINAL  (0),   // Nonterminal
    CONTENT       (1),   // Passed to the parser
    NOISE         (2),   // Ignored by the parser
    END           (3),   // End character (EOF)
    GROUP_START   (4),   // Group start
    GROUP_END     (5),   // Group end
    COMMENT_LINE  (6),   // Note COMMENT_LINE is deprecated starting at V5.
    ERROR         (7);   // Error symbol

    private final int enumCode;

    SymbolType(int code) {
        this.enumCode = code;
    }

    public int getCode() {
        return enumCode;
    }

    public static SymbolType getSymbolType(int code) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].enumCode == code) {
                return values()[i];
            }
        }
        return UNDEFINED;
    }
}

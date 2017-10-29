package com.creativewidgetworks.goldparser.engine.enums;

/**
 * ParseResult 
 *
 * <br>Dependencies: None
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0 
 */
public enum ParseResult {
    UNDEFINED         (-1),
    ACCEPT            (1),
    SHIFT             (2),
    REDUCE_NORMAL     (3),
    REDUCE_ELIMINATED (4),  // Trim
    SYNTAX_ERROR      (5),
    INTERNAL_ERROR    (6);

    private final int enumCode;

    ParseResult(int code) {
        this.enumCode = code;
    }

    public int getCode() {
        return enumCode;
    }

    public static ParseResult getParseResult(int code) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].enumCode == code) {
                return values()[i];
            }
        }
        return UNDEFINED;
    }
}

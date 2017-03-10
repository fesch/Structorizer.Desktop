package com.creativewidgetworks.goldparser.engine.enums;

/**
 * ParseMessage 
 *
 * <br>Dependencies: None
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0 
 */
public enum ParseMessage {
    UNDEFINED        (-1),
    TOKEN_READ       (0),  // A new token is read
    REDUCTION        (1),  // A production is reduced
    ACCEPT           (2),  // Parse of grammar is complete
    NOT_LOADED_ERROR (3),  // The tables are not loaded
    LEXICAL_ERROR    (4),  // Token is not recognized
    SYNTAX_ERROR     (5),  // Token is not expected
    GROUP_ERROR      (6),  // Reached the end of the file inside a block
    INTERNAL_ERROR   (7),; // Something is wrong, very wrong

    private final int enumCode;

    ParseMessage(int code) {
        this.enumCode = code;
    }

    public int getCode() {
        return enumCode;
    }

    public static ParseMessage getParseMessage(int code) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].enumCode == code) {
                return values()[i];
            }
        }
        return UNDEFINED;
    }
}

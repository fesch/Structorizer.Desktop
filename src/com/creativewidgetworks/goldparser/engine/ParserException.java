package com.creativewidgetworks.goldparser.engine;

import com.creativewidgetworks.goldparser.util.FormatHelper;

/**
 * ParseException
 *
 * This class implements a GOLD Parser specific exception.
 *
 * <br>Dependencies: None
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0 
 */
public class ParserException extends RuntimeException {
    
    public ParserException() {
        super();
    }

    public ParserException(Throwable throwable) {
        super(throwable);
    }
    
    public ParserException(String msg) {
        super(FormatHelper.formatMessage("messages", msg));
    }

    public ParserException(String msg, Throwable throwable) {
        super(FormatHelper.formatMessage("messages", msg), throwable);
    }
}

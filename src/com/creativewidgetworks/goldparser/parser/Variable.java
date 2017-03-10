package com.creativewidgetworks.goldparser.parser;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Variable 
 *
 * Represents a program variable or interim result. Methods are provided to return the
 * variable as a specified object to avoid external casting.
 *
 * <br>Dependencies: None
 *
 * @author Ralph Iden (http://www.creativewidgetworks.com)
 * @version 5.0.0 
 */
public class Variable {
    private static final String TRUE  = "1,true,";
    private static final String FALSE = "0,false,";
    
    final Object value;

    public Variable(Object theValue) {
        value = theValue;
    }

    public Object asObject() {
        return value;
    }

    public Boolean asBoolean() {
        Boolean b = null;
        if (value != null) {
            if (value instanceof String) {
                String str = ((String)value).toLowerCase() + ",";
                if (TRUE.contains(str)) {
                    b = Boolean.TRUE;
                } else if (FALSE.contains(str)) {
                    b = Boolean.FALSE;
                }
            } else if (value instanceof Boolean) {
                b = (Boolean)value;
            } 
        }
        return b;
    }

    public boolean asBool() {
        Boolean b = asBoolean();
        return b != null ? b.booleanValue() : false;
    }

    public double asDouble() {
        BigDecimal bd = asNumber();
        return bd != null ? bd.doubleValue() : Double.NaN;
    }

    public int asInt() {
        BigDecimal bd = asNumber();
        return bd != null ? bd.intValue() : Integer.MIN_VALUE;
    }

    public BigDecimal asNumber() {
        BigDecimal bd = null;
        if (value != null) {
            if (value instanceof String) {
                try {
                    bd = new BigDecimal((String)value);
                } catch (NumberFormatException nfe) {
                    // null will be returned as type can't be coerced
                }
            } else if (value instanceof BigDecimal) {
                bd = (BigDecimal)value;
            } 
        }
        return bd;
    }

    public String asString() {
        return value != null && value instanceof String ? value.toString() : null;
    }

    public Timestamp asTimestamp() {
        Timestamp ts = null;
        if (value != null) {
            if (value instanceof String) {
                try {
                    ts = Timestamp.valueOf((String)value);
                } catch (IllegalArgumentException iae) {
                    // null will be returned as type can't be coerced
                }
            } else if (value instanceof Timestamp) {
                ts = (Timestamp)value;
            } 
        }        
        
        return ts;
    }
    
    public String toString() {
        return value != null ? value.toString() : "";
    }
}

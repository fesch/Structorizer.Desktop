package com.creativewidgetworks.goldparser.engine;

import com.creativewidgetworks.goldparser.engine.enums.EntryType;

/**
 * Entry  
 * Container for an entry read from a CGT stream.
 *
 * <br>Dependencies:
 * <ul> 
 * <li>{@link EntryType}</li>
 * </ul>
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0 
 */
public class Entry {
    private EntryType type;
    private Object value;

    public Entry() {
        type = EntryType.UNDEFINED;
    }
    
    public Entry(EntryType type, Object value) {
        this.type = type;
        this.value = value;
    }
    
    public EntryType getType() {
        return type;
    }
    
    public Object getValue() {
        return value;
    }
    
    public void setType(EntryType type) {
        this.type = type;
    }

    public void setValue(Object value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return type.name() + ": " + getValue();
    }
}

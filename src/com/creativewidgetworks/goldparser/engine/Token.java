package com.creativewidgetworks.goldparser.engine;

import com.creativewidgetworks.goldparser.engine.enums.SymbolType;

/**
 * Token  
 * 
 * While the Symbol represents a class of terminals and nonterminals, the
 * Token represents an individual piece of information.
 * 
 * <br>Dependencies:
 * <ul> 
 * <li>{@link Position}</li>
 * <li>{@link Symbol}</li>
 * </ul>
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0 
 */
public class Token extends Symbol {
    private int state;
    private Object data;
    private Position position;
    
    private StringBuilder text;

    public Token() {
        super();
        this.state = LRState.INITIAL_STATE;
    }

    /**
     * This constructor is used for the unit tests for the Rules processor
     * @param data 
     */
    public Token(Object data) {
        this(new Symbol(data == null ? "" : data.toString(), SymbolType.CONTENT, 0), data);
    }

    public Token(Symbol symbol, Object data) {
        this(symbol, data, null);
    }
    
    public Token(Symbol symbol, Object data, Position position) {
        this();
        this.data = data;
        this.name = symbol.name;
        this.type = symbol.type;
        this.tableIndex = symbol.tableIndex;
        this.position = position;
        if (data != null) {
            appendData(data.toString());
        }
    }
    
    public void appendData(String moreData) {
        if (text == null) {
            text = new StringBuilder();
        }
        text.append(moreData);
    }
    
    public Reduction asReduction() {
        return data instanceof Reduction ? (Reduction)data : null;
    }
    
    public String asString() {
        return text != null ? text.toString() : data == null ? "" : data.toString();    
    }
    
    public int getState() {
        return state;
    }

    public Object getData() {
        return data;
    }

    public Position getPosition() {
        return position;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
    
    public void setSymbol(Symbol symbol) {
        setGroup(symbol.getGroup());
        setName(symbol.getName());
        setType(symbol.getType());
        setTableIndex(symbol.getTableIndex());  
    }
    
}
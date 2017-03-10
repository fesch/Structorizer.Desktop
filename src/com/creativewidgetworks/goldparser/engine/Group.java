package com.creativewidgetworks.goldparser.engine;

import java.util.ArrayList;
import java.util.List;

import com.creativewidgetworks.goldparser.engine.enums.AdvanceMode;
import com.creativewidgetworks.goldparser.engine.enums.EndingMode;

/**
 * Group  
 * Container for a group of symbols.
 *
 * <br>Dependencies:
 * <ul>
 * <li>{@link Symbol}</li>
 * </ul>
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0 
 */
public class Group {
    private String name;
    private Symbol container;
    private Symbol start;
    private Symbol end;
    private AdvanceMode advance;
    private EndingMode ending;
    private int tableIndex;
    private List<Integer> nesting;

    public Group() {
        advance = AdvanceMode.CHARACTER;
        ending = EndingMode.CLOSED;
        nesting = new ArrayList<Integer>();
    }
    
    public String getName() {
        return name;
    }
    
    public Symbol getContainer() {
        return container;
    }
    
    public Symbol getStart() {
        return start;
    }
    
    public Symbol getEnd() {
        return end;
    }
    
    public AdvanceMode getAdvanceMode() {
        return advance;
    }
    
    public EndingMode getEndingMode() {
        return ending;
    }
    
    public int getIndex() {
        return tableIndex;
    }

    public List<Integer> getNesting() {
        return nesting;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setContainer(Symbol container) {
        this.container = container;
    }
    
    public void setStart(Symbol start) {
        this.start = start;
    }
    
    public void setEnd(Symbol end) {
        this.end = end;
    }

    public void setAdvanceMode(AdvanceMode mode) {
        this.advance = mode;
    }
    
    public void setEndingMode(EndingMode mode) {
        this.ending = mode;
    }
    
    public void setIndex(int tableIndex) {
        this.tableIndex = tableIndex;
    }

    public void setNesting(List<Integer>nesting) {
        this.nesting = nesting;
    }    
    
}

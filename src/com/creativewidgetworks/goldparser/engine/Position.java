package com.creativewidgetworks.goldparser.engine;

/**
 * Position 
 *
 * Holds a line and column number pair.
 * 
 * <br>Dependencies: None
 *
 * @author Devin Cook (http://www.DevinCook.com/GOLDParser)
 * @author Ralph Iden (http://www.creativewidgetworks.com), port to Java
 * @version 5.0.0
 */
public class Position {
    private int line;
    private int column;

    public Position(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public Position(Position position) {
        this.line = position.line;
        this.column = position.column;
    }

    /** increment current position by 1 line, reset column to 1 */
    public void incrementLine() {
        this.line++;
        this.column = 1;
    }

    /** increment current position by 1 column */
    public void incrementColumn() {
        this.column++;
    }

    // START SSO 2017-06-26 - Added as convenience for Parser.consumeBuffer(int) fix
    /** increment current position by given lines, reset column to 1 */
    public void incrementLine(int lines) {
        this.line += lines;
        this.column = 1;
    }
    
    /** increment current position by given columns */
    public void incrementColumn(int columns) {
        this.column += columns;
    }
    // END SSO 2017-06-26
    
    public void set(Position newPosition) {
        if (newPosition != null) {
            this.line = newPosition.getLine();
            this.column = newPosition.getColumn();
        }
    }
  
    public int getColumn() {
        return column;
    }    
    
    public String getColumnAsString() {
        return String.valueOf(column);
    }
    
    public int getLine() {
        return line;
    }
    
    public String getLineAsString() {
        return String.valueOf(line);
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" [").append(getLineAsString()).append(",").append(getColumnAsString()).append("]");
        return sb.toString();
    }
}
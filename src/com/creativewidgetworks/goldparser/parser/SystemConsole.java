package com.creativewidgetworks.goldparser.parser;

import java.io.IOException;

import com.creativewidgetworks.goldparser.util.ConsoleDriver;

/**
 * SystemConsole 
 *
 * Concrete implementation of the ConsoleDriver interface that provides
 * support for reading and writing to a console. Used by Display/Print
 * rule processors to gather from and display data to the user via
 * the system console.
 *
 * <br>Dependencies:
 * <ul>
 * <li>{@link ConsoleDriver}</li>
 * </ul>
 *
 * @author Ralph Iden (http://www.creativewidgetworks.com)
 * @version 5.0.0 
 */
public class SystemConsole implements ConsoleDriver {

    /**
     * Read zero or more characters from StdIn.  The input is terminated when
     * the user types <CR>.
     * @return data from the user
     */
    public String read() {
        StringBuilder sb = new StringBuilder();
        try {
            char chr = (char)System.in.read();
            while (chr != 13) {
                sb.append(chr);
                chr = (char)System.in.read();
            }
            
            // Read the LF
            System.in.read();
            
        } catch (IOException e) {
            // nothing that we can do about this so ignore
        }
        return sb.toString();
    }

    /*----------------------------------------------------------------------------*/

    /**
     * Write string data to stdout.
     * @param data to write.
     */
    public void write(String data) {
        System.out.print(data);    
    }


}

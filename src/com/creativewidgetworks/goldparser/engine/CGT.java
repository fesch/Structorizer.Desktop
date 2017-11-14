package com.creativewidgetworks.goldparser.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.creativewidgetworks.goldparser.engine.enums.EntryType;

/**
 * CGTReader 
 *
 * Reads data from the CGT stream.
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
public class CGT {
    public static final int RECORD_CONTENT_MULTI = 77;  // M
        
    private boolean eofReached;
    
    private InputStream is;
    
    private int entriesRead;
    private int entryCount;
    private String header;
    
    public CGT() {
        eofReached = true;
    }
    
    /*----------------------------------------------------------------------------*/
    
    /**
     * Read a null terminated string from the stream
     * @return String input stream
     * @throws IOException
     */
    private String rawReadCString() throws IOException {
        StringBuilder sb = new StringBuilder();
        
        char c = (char)rawReadUInt16();
        while (!atEOF() && c != 0) {
            sb.append(c);
            c = (char)rawReadUInt16();
        }
        
        return sb.toString();
    }
    
    /*----------------------------------------------------------------------------*/
    
    /**
     * Read a 16 bit unsigned integer from the stream in little endian form.
     * @return unsigned integer from the input stream
     * @throws IOException
     */
    private int rawReadUInt16() throws IOException {
        int b0 = readByte();
        int b1 = readByte();
        return (b1 << 8) + b0;
    }
    
    /*----------------------------------------------------------------------------*/

    private int readByte() throws IOException {
        int i = is.read();
        eofReached = i == -1;
        return i & 0xff;
    }
    
    /*----------------------------------------------------------------------------*/

    public Entry retrieveEntry() throws IOException {
        Entry result = new Entry();
        
        if (entriesRead < entryCount) {
            entriesRead++;
            int entryType = readByte();
            switch (EntryType.getEntryType(entryType)) {
                case BOOLEAN:
                    int b = readByte();
                    result.setType(EntryType.BOOLEAN);
                    result.setValue(b == 1 ? Boolean.TRUE : Boolean.FALSE);
                    break;
                    
                case BYTE:
                    result.setType(EntryType.BYTE);
                    result.setValue(Integer.valueOf(readByte()));
                    break;
                    
                case EMPTY:
                    result.setType(EntryType.EMPTY);
                    result.setValue("");
                    break;
                    
                case ERROR:
                    result.setType(EntryType.ERROR);
                    result.setValue("");
                    break;
                    
                case STRING:
                    result.setType(EntryType.STRING);
                    result.setValue(rawReadCString());
                    break;
                    
                case UINT16:
                    result.setType(EntryType.UINT16);
                    result.setValue(Integer.valueOf(rawReadUInt16()));
                    break;
                    
                case UNDEFINED:
                    result.setType(EntryType.UNDEFINED);
                    result.setValue(String.valueOf(entryType));
                    break;
            }
        } else {
            result.setType(EntryType.EMPTY);
            result.setValue("");
        }
        
        return result;
    }

    /*----------------------------------------------------------------------------*/

    private Object retrieveEntry(EntryType type) throws IOException {
        Entry entry = retrieveEntry();
        if (entry.getType().equals(type)) {
            return entry.getValue();
        }
        throw new IOException("Invalid entry type. Expected " + type.name() + ", but got " + entry.getType().name());
    }
    
    /*----------------------------------------------------------------------------*/
    /*----------------------------------------------------------------------------*/

    /**
     * Reports is the end of file has been reached in the stream
     * @return true if all data has been read.
     */
    public boolean atEOF() {
        return eofReached;
    }
    
    /*----------------------------------------------------------------------------*/
    
    /**
     * Closes the inputstream being processed
     */
    public void close() {
        // START SSO 2017-06-26
        if (is != null) {
        // END SSO 2017-06-26
           try {
               is.close();
           } catch (Throwable t) {
               // okay, nothing to do
           }
           
           is = null;
        // START SSO 2017-06-26
        }
        // END SSO 2017-06-26
        eofReached = true;
    }
    
    /*----------------------------------------------------------------------------*/

    public boolean isRecordComplete() {
        return entriesRead >= entryCount;
    }
    
    public int getEntryCount() {
        return entryCount;
    }
    
    public int getEntriesRead() {
        return entriesRead;
    }
    
    public String getHeader() {
        return header;
    }
    
    /*----------------------------------------------------------------------------*/

    /**
     * Reads the stream and positions to the start of the next record.
     * @return true if positioned to the start of the next record.
     * @throws IOException
     */
    public boolean getNextRecord() throws IOException {
        // Finish the current record
        while (entriesRead < entryCount) {
            retrieveEntry();
        }
        
        // Start next record
        boolean result;
        int id = readByte();
        if (id == RECORD_CONTENT_MULTI) {
            entriesRead = 0;
            entryCount = rawReadUInt16();
            result = true;
        } else {
            result = false;
        }
        
        return result;
    }
    
    /*----------------------------------------------------------------------------*/

    /**
     * Open a CGT (compiled grammar table) file for processing
     * @param file to open
     * @throws IOException
     */
    public void open(File file) throws IOException {
        if (file == null) {
            throw new IOException("File null");
        }
        open(new FileInputStream(file));
    }
    
    /*----------------------------------------------------------------------------*/
    
    /**
     * Open a CGT (compiled grammar table) stream for processing
     * @param input the {@link InputStream} to process
     * @throws IOException
     */    
    public void open(InputStream input) throws IOException {
        close();
        is = input;
        entryCount = 0;
        entriesRead = 0;
        if (is != null) {
            header = rawReadCString();
        } else {
            throw new IOException("InputStream null");
        }
    }
    
    /*----------------------------------------------------------------------------*/

    /**
     * Read a boolean from the input stream
     * @return boolean
     * @throws IOException
     */
    public boolean retrieveBoolean() throws IOException {
        return ((Boolean)retrieveEntry(EntryType.BOOLEAN)).booleanValue();
    }

    /*----------------------------------------------------------------------------*/
    
    /**
     * Read a byte from the input stream
     * @return byte
     * @throws IOException
     */
    public int retrieveByte() throws IOException {
        return ((Integer)retrieveEntry(EntryType.BYTE)).intValue();
    }

    /*----------------------------------------------------------------------------*/
    
    /**
     * Read an integer from the input stream
     * @return int
     * @throws IOException
     */
    public int retrieveInteger() throws IOException {
        return ((Integer)retrieveEntry(EntryType.UINT16)).intValue();
    }

    /*----------------------------------------------------------------------------*/
    
    /**
     * Read a String object from the input stream
     * @return String
     * @throws IOException
     */
    public String retrieveString() throws IOException {
        return (String)retrieveEntry(EntryType.STRING);
    }
}
package com.creativewidgetworks.goldparser.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Extractor {
    public static final String LINE   = "//=========================================================================";
    public static final String BEGINS = "//RULE HANDLER CLASS BEGINS ===============================================";
    public static final String ENDS   = "//RULE HANDLER CLASS ENDS =================================================";
    public static final String MAIN   = "// MAIN PROGRAM SHELL BEGINS ==============================================";
    
    public static final String CLASS_MARKER = "public class ";
    
    private static final int STATUS_UNDEFINED = -2;
    private static final int STATUS_ERROR     = -1;
    private static final int STATUS_EOF       = 0;
    private static final int STATUS_HANDLER   = 1;
    private static final int STATUS_MAIN      = 2;
    
    private int offset;

    private String srcFile;
    private String outputFolder;
    
    private List<String> lines;

    public Extractor(String srcFile, String outputFolder) {
        this.srcFile = srcFile;
        this.outputFolder = outputFolder;
    }

    public String getSrcFile() {
        return srcFile;
    }

    public void setSrcFile(String srcFile) {
        this.srcFile = srcFile;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    /*----------------------------------------------------------------------------*/

    private void closeNoThrow(Closeable ioObject) {
        try {
            ioObject.close();
        } catch (IOException ioe) {
            // Nothing we can really do, except perhaps log an error
        }
    }
    
    /*----------------------------------------------------------------------------*/
    
    private int moveToStartOfBlock() {
        int status = STATUS_ERROR;
        
        // Move to the start of a block separator
        while (offset < lines.size() && !lines.get(offset++).equals(LINE)) { /* empty block intended */ }
    
        // At this point, we should be at the beginning of a rule handler, main program block, or end of file
        if (offset < lines.size()) {
            String line = lines.get(offset);
            if (line.equals(MAIN)) {
                offset++;
                status = STATUS_MAIN;
            } else if (line.equals(BEGINS)) {
                offset++;
                status = STATUS_HANDLER;
            } else {
                status = STATUS_ERROR;
            }
        } else {
            status = STATUS_EOF;
        }        
        
        // Eat blank lines
        while (offset < lines.size() && lines.get(offset++).trim().length() == 0) { /* empty block intended */ }
        
        return status;
    }
    
    /*----------------------------------------------------------------------------*/
    
    private void writeHandler() {
        String className = null;
        List<String> srcLines = new ArrayList<String>();
        
        // Build list of source lines and extract class name
        while (offset < lines.size() && !lines.get(offset).equals(LINE)) { 
            String line = lines.get(offset++);
            if (line.startsWith(CLASS_MARKER)) {
                className = line.substring(0, line.indexOf(' ', CLASS_MARKER.length())).replace(CLASS_MARKER, "");
            }
            srcLines.add(line);
        }        
        
        // Clean up any blank lines at the top and bottom
        while (srcLines.get(0).trim().length() == 0) {
            srcLines.remove(0);
        }
        while (srcLines.get(srcLines.size()-1).trim().length() == 0) {
            srcLines.remove(srcLines.size()-1);
        }
        
        // Should be at the start of the HANDLER END block, eat those lines
        if (offset < lines.size()) {
            offset += 3;
        }
        
        // Write the handler class file
        FileOutputStream fos = null;
        File outFile = new File(getOutputFolder(), className + ".java");
        try {
            fos = new FileOutputStream(outFile);
            for (String line : srcLines) {
                fos.write(line.getBytes("utf-8"));
                fos.write(System.getProperty("line.separator").getBytes("utf-8"));
            }
        } catch (IOException ioe) {
            System.out.println("Unable to write handler file (" + outFile.getAbsolutePath() + "): " + ioe.getMessage());
        } finally {
            closeNoThrow(fos);
        }        
    }
    
    /*----------------------------------------------------------------------------*/
        
    private void writeMain() {
        String className = null;
        List<String> srcLines = new ArrayList<String>();
        
        // Build list of source lines and extract class name
        while (offset < lines.size() && !lines.get(offset).equals(LINE)) { 
            String line = lines.get(offset++);
            if (line.startsWith(CLASS_MARKER)) {
                className = line.substring(0, line.indexOf(' ', CLASS_MARKER.length())).replace(CLASS_MARKER, "");
            }
            srcLines.add(line);
        }        
        
        // Clean up any blank lines at the top and bottom
        while (srcLines.get(0).trim().length() == 0) {
            srcLines.remove(0);
        }
        while (srcLines.get(srcLines.size()-1).trim().length() == 0) {
            srcLines.remove(srcLines.size()-1);
        }
        
        // Should be at the start of the HANDLER END block, eat those lines
        if (offset < lines.size()) {
            offset += 3;
        }
        
        // Write the handler class file
        FileOutputStream fos = null;
        File outFile = new File(getOutputFolder(), className + ".java");
        try {
            fos = new FileOutputStream(outFile);
            for (String line : srcLines) {
                fos.write(line.getBytes("utf-8"));
                fos.write(System.getProperty("line.separator").getBytes("utf-8"));
            }
        } catch (IOException ioe) {
            System.out.println("Unable to write main file (" + outFile.getAbsolutePath() + "): " + ioe.getMessage());
        } finally {
            closeNoThrow(fos);
        }        
    }
    
    /*----------------------------------------------------------------------------*/
    
    public void extract() {
        offset = 0;
        lines = new ArrayList<String>();

        // Load the lines from the java template source file
        BufferedReader rdr = null;
        try {
            rdr = new BufferedReader(new InputStreamReader(new FileInputStream(getSrcFile())));
            String line  = rdr.readLine();
            while (line != null) {
                line = rdr.readLine();
                if (line != null) {
                    lines.add(line);
                }
            } 
        } catch (IOException ioe) {
            System.out.println("Unable to read source file " + getSrcFile() + ": " + ioe.getMessage());
        } finally {
            closeNoThrow(rdr);
        }
        
        // Create the output directory chain, if necessary
        new File(getOutputFolder()).mkdirs();
        
        // Process the lines
        int status = STATUS_UNDEFINED;
        while (status != STATUS_EOF && status != STATUS_ERROR) {
            status = moveToStartOfBlock();
            if (status == STATUS_HANDLER) {
                writeHandler();
            } else if (status == STATUS_MAIN) {
                writeMain();
            }
        }
    }
    
    /*----------------------------------------------------------------------------*/
    
    private static void printHelp() {
        System.out.println("Extractor 1.00 - Extracts RuleHandlers from GOLDBuilder template");
        System.out.println("Usage: Extractor javaFile outputFolder");
        System.out.println("  javaFile is the template file containing all the rule handlers created in the GOLDBuilder");
        System.out.println("  outputFolder is the location to store the extracted rule handles and main file");
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            printHelp();
        } else {
            new Extractor(args[0], args[1]).extract();
        }
    }

}

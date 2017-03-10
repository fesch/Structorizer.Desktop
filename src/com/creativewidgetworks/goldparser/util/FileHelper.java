package com.creativewidgetworks.goldparser.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileHelper {

    public static FileInputStream toInputStream(File file) throws IOException {
        if (file == null) {
            throw new IOException(FormatHelper.formatMessage("messages", "error.cgt_missing"));
        }
        return new FileInputStream(file);
    }

}

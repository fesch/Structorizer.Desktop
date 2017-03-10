package com.creativewidgetworks.goldparser.util;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class FormatHelper {

    /**
     * Format a message with the given parameters. If the bundleName is not null then the message is
     * treated as a key in that bundle; the found resource will be used as the message to format.
     * 
     * This method provides a convenient wrapper for java.util.ResourceBundle and
     * java.text.MessageFormat operations because all exceptions are caught and a simple
     * concatenation of the original message and the parameters is returned.
     *
     * @param bundleName
     * @param message
     * @param parameters
     *
     * @return String
     */
    public static String formatMessage(String bundleName, String message, Object... parameters) {
        ResourceBundle bundle = null;
        
        if (bundleName != null && bundleName.trim().length() > 0) {
            try {
                bundle = ResourceBundle.getBundle(bundleName);
            } catch (MissingResourceException mre) {
                // Do nothing, we'll just use null
            }
        }
        
        return formatMessage(bundle, message, parameters);
    }

    /**
     * Format a message with the given parameters. If the bundle is not null then the message is
     * treated as a key in that bundle; the found resource will be used as the message to format.
     * 
     * This method provides a convenient wrapper for java.util.ResourceBundle and
     * java.text.MessageFormat operations because all exceptions are caught and a simple
     * concatenation of the original message and the parameters is returned.
     *
     * @param bundle
     * @param message
     * @param parameters
     *
     * @return String
     */
    public static String formatMessage(ResourceBundle bundle, String message, Object... parameters) {
        String formattedMessage = message;
        
        if (message != null) {
            // Lookup message in resource bundle, if applicable
            if (bundle != null) {
                try {
                    formattedMessage = bundle.getString(message);
                } catch (MissingResourceException mre) {
                    // Do nothing, we'll just use the message itself
                }
            }
            
            // Format message with parameters, if applicable
            if (parameters != null) {
                try {
                    formattedMessage = MessageFormat.format(formattedMessage, parameters);
                } catch(IllegalArgumentException iae) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < parameters.length; i++) {
                        sb.append("; ");
                        sb.append(parameters[i]);
                    }
                    formattedMessage += sb.toString();
                }
            }
        }
        
        return formattedMessage;
    }
    
}    

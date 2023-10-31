/*
    Structorizer
    A little tool which you can use to create Nassi-Schneiderman Diagrams (NSD)

    Copyright (C) 2009  Bob Fisch

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or any
    later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 * ==========================================================================

    This class was derived from com.github.jjYBdx4IL.utils.awt.Desktop
    (Copyright (C) 2016 jjYBdx4IL (https://github.com/jjYBdx4IL)), which was
    licensed under the Apache License, Version 2.0 (the "License");
    You may obtain a copy of that License at
 
           http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 
    Reductions for Structorizer done by codemanyak (https://github.com/codemanyak):
    - Log disabled
    - System detection in openSystemSpecific simplified
 */

package lu.fisch.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

// START codemanyak 2016-09-17: Disabled
//import org.apache.commons.lang3.SystemUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
// END codemanyak 2016-09-17

/**
 * Class is similar to java.awt.Desktop but provides some workarounds
 * for lacking integration with certain Linux distributions for launching
 * a browser, an editor, or some other standard application for given
 * files.<br/>
 * In contrast to java.awt.Desktop it is not a singleton but provides static
 * methods only.
 * @author jjYBdx4IL
 * @author codemanyak
 */
public class Desktop {

    //private static final Logger log = LoggerFactory.getLogger(Desktop.class);

    public static boolean browse(URI uri) {

        if (browseDESKTOP(uri)) return true;

        if (openSystemSpecific(uri.toString())) return true;

        // START codemanyak 2016-09-17: Disabled
        //log.warn(String.format("failed to browse %s", uri));
        // END codemanyak 2016-09-17

        return false;
    }


    public static boolean open(File file) {

        if (openDESKTOP(file)) return true;

        if (openSystemSpecific(file.getPath())) return true;

        // START codemanyak 2016-09-17: Disabled
        //log.warn(String.format("failed to open %s", file.getAbsolutePath()));
        // END codemanyak 2016-09-17

        return false;
    }


    public static boolean edit(File file) {

        if (editDESKTOP(file)) return true;

        if (openSystemSpecific(file.getPath())) return true;

        // START codemanyak 2016-09-17: Disabled
        //log.warn(String.format("failed to edit %s", file.getAbsolutePath()));
        // END codemanyak 2016-09-17

        return false;
    }


    private static boolean openSystemSpecific(String what) {

        // START codemanyak 2016-09-17: Reduced to a simpler autonomous test
        //if (SystemUtils.IS_OS_LINUX) {
        String sys = System.getProperty("os.name").toLowerCase();
        // Try to guess the window manager from environmemt variables
        if (sys.contains("linux")) {
        // END codemanyak 2016-09-17
            if (isXDG()) {
                if (runCommand("xdg-open", "%s", what)) return true;
            }
            if (isKDE()) {
                if (runCommand("kde-open", "%s", what)) return true;
            }
            if (isGNOME()) {
                if (runCommand("gnome-open", "%s", what)) return true;
            }
            // Finally, try blindly all three commands
            // START KGU 2016-09-18: Bugfix #245 - XDG had been forgotten
            if (runCommand("xdg-open", "%s", what)) return true;
            // END KGU 2016-09-18
            if (runCommand("kde-open", "%s", what)) return true;
            if (runCommand("gnome-open", "%s", what)) return true;
        }

        // START codemanyak 2016-09-17: Reduced to a simpler autonomous test
        //if (SystemUtils.IS_OS_MAC) {
        if (sys.contains("mac")) {
        // END codemanyak 2016-09-17
            if (runCommand("open", "%s", what)) return true;
        }

        // START codemanyak 2016-09-17: Reduced to a simpler autonomous test
        //if (SystemUtils.IS_OS_WINDOWS) {
        if (sys.contains("win")) {
        // END codemanyak 2016-09-17
            if (runCommand("explorer", "%s", what)) return true;
        }

        return false;
    }


    private static boolean browseDESKTOP(URI uri) {

    	try {
            if (!java.awt.Desktop.isDesktopSupported()) {
                // START codemanyak 2016-09-17: Disabled
                //log.debug("Platform is not supported.");
                // END codemanyak 2016-09-17
                return false;
            }

            if (!java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                // START codemanyak 2016-09-17: Disabled
                //log.debug("BROWSE is not supported.");
                // END codemanyak 2016-09-17
                return false;
            }

            // START codemanyak 2016-09-17: Disabled
            //log.info("Trying to use Desktop.getDesktop().browse() with " + uri.toString());
            // END codemanyak 2016-09-17
            java.awt.Desktop.getDesktop().browse(uri);

            return true;
        } catch (Throwable t) {
            // START codemanyak 2016-09-17: Disabled
            //log.error("Error using desktop browse.", t);
            // END codemanyak 2016-09-17
            return false;
        }
    }


    private static boolean openDESKTOP(File file) {
        try {
            if (!java.awt.Desktop.isDesktopSupported()) {
                // START codemanyak 2016-09-17: Disabled
                //log.debug("Platform is not supported.");
                // END codemanyak 2016-09-17
                return false;
            }

            if (!java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.OPEN)) {
                // START codemanyak 2016-09-17: Disabled
                //log.debug("OPEN is not supported.");
                // END codemanyak 2016-09-17
                return false;
            }

            // START codemanyak 2016-09-17: Disabled
            //log.info("Trying to use Desktop.getDesktop().open() with " + file.toString());
            // END codemanyak 2016-09-17
            java.awt.Desktop.getDesktop().open(file);

            return true;
        } catch (Throwable t) {
            // START codemanyak 2016-09-17: Disabled
            //log.error("Error using desktop open.", t);
            // END codemanyak 2016-09-17
            return false;
        }
    }


    private static boolean editDESKTOP(File file) {
        try {
            if (!java.awt.Desktop.isDesktopSupported()) {
                // START codemanyak 2016-09-17: Disabled
                //log.debug("Platform is not supported.");
                // END codemanyak 2016-09-17
                return false;
            }

            if (!java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.EDIT)) {
                // START codemanyak 2016-09-17: Disabled
                //log.debug("EDIT is not supported.");
                // END codemanyak 2016-09-17
                return false;
            }

            // START codemanyak 2016-09-17: Disabled
            //log.info("Trying to use Desktop.getDesktop().edit() with " + file);
            // END codemanyak 2016-09-17
            java.awt.Desktop.getDesktop().edit(file);

            return true;
        } catch (Throwable t) {
            // START codemanyak 2016-09-17: Disabled
            //log.error("Error using desktop edit.", t);
            // END codemanyak 2016-09-17
            return false;
        }
    }


    private static boolean runCommand(String command, String args, String file) {

        // START codemanyak 2016-09-17: Disabled
        //log.info("Trying to exec:\n   cmd = " + command + "\n   args = " + args + "\n   %s = " + file);
        // END codemanyak 2016-09-17

        String[] parts = prepareCommand(command, args, file);

        try {
            Process p = Runtime.getRuntime().exec(parts);

            if (p == null) return false;

            try {
                int retval = p.exitValue();
                if (retval == 0) {
                    // START codemanyak 2016-09-17: Disabled
                    //log.error("Process ended immediately.");
                    // END codemanyak 2016-09-17
                    return false;
                } else {
                    // START codemanyak 2016-09-17: Disabled
                    //log.error("Process crashed.");
                    // END codemanyak 2016-09-17
                    return false;
                }
            } catch (IllegalThreadStateException itse) {
                // START codemanyak 2016-09-17: Disabled
                //log.error("Process is running.");
                // END codemanyak 2016-09-17
                return true;
            }
        } catch (IOException e) {
            // START codemanyak 2016-09-17: Disabled
            //log.error("Error running command.", e);
            // END codemanyak 2016-09-17
            return false;
        }
    }


    private static String[] prepareCommand(String command, String args, String file) {

        List<String> parts = new ArrayList<String>();
        parts.add(command);

        if (args != null) {
            for (String s : args.split(" ")) {
                s = String.format(s, file); // put in the filename thing

                parts.add(s.trim());
            }
        }

        return parts.toArray(new String[parts.size()]);
    }

    private static boolean isXDG() {
        String xdgSessionId = System.getenv("XDG_SESSION_ID");
        return xdgSessionId != null && !xdgSessionId.isEmpty();
    }

    private static boolean isGNOME() {
        String gdmSession = System.getenv("GDMSESSION");
        return gdmSession != null && gdmSession.toLowerCase().contains("gnome");
    }

    private static boolean isKDE() {
        String gdmSession = System.getenv("GDMSESSION");
        // START KGU#250 2016-09-19: Issue #245
        //return gdmSession != null && gdmSession.toLowerCase().contains("kde");
        String kdeSessionUid = System.getenv("KDE_SESSION_UID");
        return (gdmSession != null && gdmSession.toLowerCase().contains("kde"))
                 || (kdeSessionUid != null && !kdeSessionUid.isEmpty());
        // END KGU#250 2016-09-19
    }
}

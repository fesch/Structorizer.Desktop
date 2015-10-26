//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
import java.io.File;

/** This class is just like FileRegex, except that its accept method
 only returns true if the file matching the pattern is a directory.*/
public class DirFileRegex extends FileRegex {
    public DirFileRegex() { dirflag = DIR; }
    public DirFileRegex(String fp) { super(fp); dirflag = DIR; }
    public static String[] list(String f) {
        return list(f,DIR);
    }
}

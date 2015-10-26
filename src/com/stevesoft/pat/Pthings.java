//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
import java.util.*;

/** 
Things that need to be kept track of during a
 match.  Passed along with Pattern.matchInternal. */
public class Pthings {
    /** The current text we are attempting to match. */
    public StringLike src;
    /** Whether we should ignore the case of letters in
        this match. */
    public boolean ignoreCase;
    public boolean mFlag;
    /** The mask to use when dontMatchInQuotes is set. */
    public BitSet cbits;
    /** Used to keep track of backreferences. */
    //public Hashtable marks;
    public int[] marks;
    public int nMarks;
    /** Used to set the behavior of "."  By default, it
        now fails to match the '\n' character. */
    public boolean dotDoesntMatchCR;
    /** Determine if Skipped strings need to be checked. */
    public boolean no_check;
    int lastPos;
}

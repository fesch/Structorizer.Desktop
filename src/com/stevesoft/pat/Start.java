//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
import java.util.Hashtable;

/** The '^' or the '\A' Pattern, matches the start of a string. */
class Start extends Pattern {
    boolean retIsStart;
    Start(boolean b) { retIsStart = b; }
    public int matchInternal(int pos,Pthings pt) {
        if(retIsStart
	&& pt.mFlag
	&& pos > 0 && pt.src.charAt(pos-1)=='\n')
            return nextMatch(pos,pt);
        if(pos == 0) return nextMatch(pos,pt);
        return -1;
    }
    public String toString() {
        if(retIsStart)
            return "^"+nextString();
        else
            return "\\A"+nextString();
    }
    public patInt maxChars() { return new patInt(0); }
    Pattern clone1(Hashtable h) { return new Start(retIsStart); }
};

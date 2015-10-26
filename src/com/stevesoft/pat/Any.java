//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
import java.util.Hashtable;

/** This is the '.' character in a Pattern.  It
 matches any character. */
class Any extends Pattern {
    public int matchInternal(int pos,Pthings pt) {
        if(pos < pt.src.length())
            if(pt.dotDoesntMatchCR) {
                if(pt.src.charAt(pos) != '\n')
                    return nextMatch(pos+1,pt);
            } else return nextMatch(pos+1,pt);
        return -1;
    }
    public String toString() {
        return "."+nextString();
    }
    public patInt minChars() { return new patInt(1); }
    public patInt maxChars() { return new patInt(1); }
    public Pattern clone1(Hashtable h) { return new Any(); }
};

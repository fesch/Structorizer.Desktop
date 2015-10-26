//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
import java.util.Hashtable;

/** This class implements the word boundary pattern element: \b. */
class Boundary extends Pattern {
    public String toString() {
        return "\\b"+nextString();
    }
    boolean isAChar(char c) {
        if(c >= 'a' && c <= 'z')
            return true;
        if(c >= 'A' && c <= 'Z')
            return true;
        if(c >= '0' && c <= '9')
            return true;
        if(c == '_')
            return true;
        return false;
    }
    boolean matchLeft(int pos,Pthings pt) {
        if(pos <= 0)
            return true;
        if(isAChar(pt.src.charAt(pos))
                && isAChar(pt.src.charAt(pos-1)))
            return false;
        return true;
    }
    boolean matchRight(int pos,Pthings pt) {
        if(pos < 0) return false;
        if(pos+1 >= pt.src.length())
            return true;
        if(isAChar(pt.src.charAt(pos))
                && isAChar(pt.src.charAt(pos+1)))
            return false;
        return true;
    }
    public int matchInternal(int pos,Pthings pt) {
        if(matchRight(pos-1,pt) || matchLeft(pos,pt))
            return nextMatch(pos,pt);
        return -1;
    }
    public patInt maxChars() { return new patInt(0); }
    public Pattern clone1(Hashtable h) { return new Boundary(); }
};

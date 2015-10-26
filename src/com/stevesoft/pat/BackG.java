//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
import java.util.Hashtable;

/** This class represents the \G pattern element. */
class BackG extends Pattern {
    char c,altc,altc2;
    int mask;
    public BackG() {
    }
    public int matchInternal(int pos,Pthings pt) {
        return pos==pt.lastPos ? nextMatch(pos,pt) : -1;
    }
    public String toString() {
        return "\\G"+nextString();
    }
    public patInt minChars() { return new patInt(1); }
    public patInt maxChars() { return new patInt(1); }
    Pattern clone1(Hashtable h) { return new BackG(); }
}

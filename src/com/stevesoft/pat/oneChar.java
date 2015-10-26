//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
import java.util.Hashtable;

/** This class matches a single character. */
class oneChar extends Pattern {
    char c,altc,altc2;
    int mask;
    public oneChar(char ci) {
        c = ci;
        char cu,cl,ct;
        cu = CaseMgr.toUpperCase(c);
        cl = CaseMgr.toLowerCase(c);
        ct = CaseMgr.toTitleCase(c);
        if(c == cu) {
            altc = cl;
            altc2 = ct;
        } else if(c == cl) {
            altc = cu;
            altc2 = ct;
        } else {
            altc = cl;
            altc2 = cu;
        }
        mask = c & altc & altc2;
    }
    public int matchInternal(int pos,Pthings pt) {
        char p;
        int ret=-1;
        if (pos < pt.src.length() && !Masked(pos,pt)
                && ((p=pt.src.charAt(pos))==c ||
                (pt.ignoreCase&& (p==altc||p==altc2) ) ))
            ret = nextMatch(pos+1,pt);
        return ret;
    }
    public String toString() {
        return protect(""+c,PROTECT_THESE,ESC)+nextString();
    }
    public patInt minChars() { return new patInt(1); }
    public patInt maxChars() { return new patInt(1); }
    Pattern clone1(Hashtable h) { return new oneChar(c); }
};

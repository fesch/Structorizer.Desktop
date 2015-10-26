//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
import java.util.Hashtable;

/** Implements the (?&lt;number) Pattern, where number is
 an integer telling us how far to back up in the Pattern.
 Not in perl 5. */
class Skipped extends Pattern {
    String s;
    Skipped(String s) { this.s = s; }
    public String toString() { return s+nextString(); }
    public int matchInternal(int pos,Pthings pt) {
        //if(pt.no_check || s.regionMatches(pt.ignoreCase,0,pt.src,pos,s.length()))
        if(pt.no_check || CaseMgr.regionMatches(s,pt.ignoreCase,0,pt.src,pos,s.length()))
            return nextMatch(pos+s.length(),pt);
        return -1;
    }
    public patInt minChars() { return new patInt(s.length()); }
    public patInt maxChars() { return new patInt(s.length()); }
    Pattern clone1(Hashtable h) { return new Skipped(s); }
};

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
 Not in perl 5. It also allows (?&gt;number). */
class Backup extends Pattern {
    int bk;
    Backup(int ii) { bk = ii; }
    public String toString() {
        return "(?" + (bk < 0 ? ">" + (-bk) : "<" + bk) + ")" + nextString();
    }
    public int matchInternal(int pos,Pthings pt) {
        if(pos < bk) return -1;
        return nextMatch(pos-bk,pt);
    }
    public patInt minChars() { return new patInt(-bk); }
    public patInt maxChars() { return new patInt(-bk); }
    public Pattern clone1(Hashtable h) { return new Backup(bk); }
};

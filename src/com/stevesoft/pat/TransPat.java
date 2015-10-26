//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

import java.util.*;

/** This class is used to implement the Transformer
    @see com.stevesoft.pat.Transform
    */
class TransPat extends Pattern {
    Regex[] ra = new Regex[10];
    int ra_len = 0;
    int pn = -1;
    public String toString() {
        return "(?#TransPat)";
    }

    TransPat() {}

    int lastMatchedTo = -1;
    public int matchInternal(int pos,Pthings pt) {
        for(int i=0;i<ra_len;i++) {
            pt.ignoreCase = ra[i].ignoreCase;
            pt.mFlag = ra[i].mFlag;
            pt.dotDoesntMatchCR = ra[i].dotDoesntMatchCR;
            int r = ra[i].thePattern.matchInternal(pos,pt);
            if(r >= 0) {
                pn = i;
                return r;
            }
        }
        pn = -1;
        return -1;
    }
}

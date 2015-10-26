//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
import java.util.Hashtable;

/** This class is needed internally to make backtracking work
  * correctly in user-defined patterns.
  */
class CustomEndpoint extends Pattern {
    Custom c;
    CustomEndpoint(Custom cm) { c = cm; }
    public int matchInternal(int pos,Pthings pt) {
        int npos = c.v.validate(pt.src,c.start,pos);
        if(npos >= 0) 
            return nextMatch(npos,pt);
        return -1;
    }
    public String toString() { return ""; }
    Pattern clone1(Hashtable h) {
        return new CustomEndpoint((Custom)c.clone(h));
    }
}

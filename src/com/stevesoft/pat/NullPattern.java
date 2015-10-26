//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
import java.util.Hashtable;

/** This pattern matches nothing -- it is found in patterns
  * like (hello|world|) where a zero-length subelement occurs.
  */
class NullPattern extends Pattern {
    public String toString() { return nextString(); }
    public int matchInternal(int p,Pthings pt) {
        return nextMatch(p,pt);
    }
    public patInt maxChars() { return new patInt(0); }
    Pattern clone1(Hashtable h) { return new NullPattern(); }
}

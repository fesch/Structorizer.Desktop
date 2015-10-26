//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
import java.util.Hashtable;

/** Provides the ability to match a backreference from within
  * a Pattern.
  */
class BackMatch extends Pattern {
    int id;
    BackMatch(int id) { this.id = id; }
    public String toString() { return "\\"+(id)+nextString(); }
    public int matchInternal(int pos,Pthings p) {
        int i1 = p.marks[id];
        int i2 = p.marks[id+p.nMarks];
        int imax = i2-i1;
        if(i1<0||imax < 0||pos+imax>p.src.length()) return -1;
        int ns = p.src.length()-pos;
        if(imax < ns) ns = imax;
        for(int i=0;i<ns;i++) {
            if(p.src.charAt(i+i1) != p.src.charAt(pos+i))
                return -1;
        }
        return nextMatch(pos+imax,p);
    }
    Pattern clone1(Hashtable h) { return new BackMatch(id); }
}


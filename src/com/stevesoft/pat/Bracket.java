//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
import java.util.Vector;
import java.util.Hashtable;

/** The Bracket is a form of the Or class,
 implements the pattern element [  ]. */
class Bracket extends Or {
    boolean neg;
    Bracket(boolean n) { neg = n; }
    String leftForm() {
        if(neg)
            return "[^";
        else
            return "[";
    }
    String rightForm() { return "]"; }
    String sepForm() { return ""; }
    public int matchInternal(int pos,Pthings pt) {
        if(pos >= pt.src.length()) return -1;
        int r = super.matchInternal(pos,pt);
        if((neg && r<0)||(!neg && r>=0))
            return nextMatch(pos+1,pt);
        return -1;
    }
    public patInt minChars() { return new patInt(1); }
    public patInt maxChars() { return new patInt(1); }

    public Or addOr(Pattern p) {
        pv = null;
        v.addElement(p);
        p.setParent(null);
        return this;
    }
    public Pattern clone1(Hashtable h) {
        Bracket b = new Bracket(neg);
        b.v = new Vector();
        for(int i=0;i<v.size();i++)
            b.v.addElement( ((Pattern)v.elementAt(i)).clone1(h) );
        return b;
    }
};

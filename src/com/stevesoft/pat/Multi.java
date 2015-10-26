//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
import java.util.Hashtable;

/** Matches any number of instances of sub Pattern
 this was the hardest method to write.  It implements
 '+', '*', '?', "{0,10}", "{5,}", "{5}", etc.
 @see pat.Multi_stage2
 @see pat.MultiMin
 */
class Multi extends PatternSub {
    patInt a,b;
    public patInt minChars() { return a.mul(p.countMinChars()); }
    public patInt maxChars() { return b.mul(p.countMaxChars()); }
    Pattern p;
    Multi_stage2 st2;
    public boolean matchFewest = false;
    /**
        @param a The fewest number of times the sub pattern can match.
        @param b The maximum number of times the sub pattern can match.
        @param p The sub pattern.
        @see Multi_stage2
        @see MultiMin
        */
    public Multi(patInt a,patInt b,Pattern p) throws RegSyntax {
        this.a = a;
        this.b = b;
        this.p = p;
        st2 = new Multi_stage2(a,b,p);
        st2.parent = this;
        sub = st2.sub;
    }
    public String toString() {
        st2.matchFewest = matchFewest;
        return st2.toString();
    }
    public int matchInternal(int pos,Pthings pt) {
        try {
            st2 = new Multi_stage2(a,b,p);
        } catch(RegSyntax r__) {}
        st2.matchFewest = matchFewest;
        st2.parent = this;
        return st2.matchInternal(pos,pt);
    }
    public Pattern clone1(Hashtable h) {
        try {
            Multi m = new Multi(a,b,((Pattern)p).clone(h));
            m.matchFewest = matchFewest;
            return m;
        } catch(RegSyntax rs) {
            return null;
        }
    }
};

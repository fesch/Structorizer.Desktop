//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
import java.util.Hashtable;

/** If Multi were not split into a second stage, then
 a nested Multi would try to re-use the same count
 variable and the whole thing would break. */
class Multi_stage2 extends PatternSub {
    Pattern nextRet;
    patInt count;
    patInt matchMin,matchMax;
    public boolean matchFewest = false;
    public String toString() {
        String ret = "";
        ret += sub.toString();
        ret += "{"+matchMin+","+matchMax+"}";
        if(matchFewest) ret += "?";
        ret += parent.nextString();
        return ret;
    }
    Multi_stage2(patInt a,patInt b,Pattern p) throws RegSyntax {
        if(p == null) RegSyntaxError.endItAll(
                "Multiple match of Null pattern requested.");
        sub = p;
        nextRet = this;
        sub.setParent(this);
        matchMin = a;
        matchMax = b;
        count = new patInt(0);
        // we must have b > a > -1 for this
        // to make sense.
        if(!a.lessEq(b))
            //throw new BadMultiArgs();
            RegSyntaxError.endItAll("Bad Multi Args: "+a+">"+b);
        patInt i = new patInt(-1);
        if(a.lessEq(i))
            //throw new BadMultiArgs();
            RegSyntaxError.endItAll("Bad Multi Args: "+a+"< 0");
    }
    public Pattern getNext() {
        return nextRet;
    }
    int pos_old = -1;
    public int matchInternal(int pos,Pthings pt) {
        sub.setParent(this);

        int canUse = -1;

        // check for some forms of infinite recursion...
        if(pos_old >= 0 && pos == pos_old) {
            return -1;
        }
        pos_old = pos;

        if(matchMin.lessEq(count))
            canUse = pos;
        if(!count.lessEq(matchMax) || pos > pt.src.length())
            return -1;

        if((matchFewest||count.equals(matchMax)) && canUse >= 0) {
            Pattern n = super.getNext();
            if(n == null)
                return canUse;
            int ret = testMatch(n,pos,pt);
            if(ret >= 0) {
               return ret;
            }
            else canUse = -1;
        }

        count.inc();
        try {
            if(count.lessEq(matchMax)) {
                int r = testMatch(sub,pos,pt);
                if(r >= 0)
                    return r;
            }
        } finally { count.dec(); }

        if(!matchFewest && canUse >= 0) {
            Pattern n = super.getNext();
            if(n == null)
                return canUse;
            int ret = testMatch(n,pos,pt);
            return ret;
        } else return canUse;
    }
    public Pattern clone1(Hashtable h) {
        try {
            Multi_stage2 m = new Multi_stage2(matchMin,matchMax,sub.clone(h));
            m.matchFewest = matchFewest;
            return m;
        } catch(RegSyntax rs) {
            return null;
        }
    }
};

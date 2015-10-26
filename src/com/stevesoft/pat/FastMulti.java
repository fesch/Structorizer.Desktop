//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
import java.util.Hashtable;

/** A special case of Multi, implemented when minChars().equals(maxChars()),
  * and some other conditions spelled out in RegOpt.safe4fm "Safe for
  * FastMulti."  It avoids stack growth problems as well as being slightly
  * faster.
  */
class FastMulti extends PatternSub {
    patInt fewestMatches,mostMatches;
    public patInt minChars() {
        return sub.countMinChars().mul(fewestMatches);
    }
    public patInt maxChars() {
        return sub.countMaxChars().mul(mostMatches);
    }
    public boolean matchFewest = false;

    FastMulti(patInt a,patInt b,Pattern p) throws RegSyntax {
        if(p == null) RegSyntaxError.endItAll("Null length pattern "+
                "followed by *, +, or other Multi.");
        fewestMatches = a;
        mostMatches = b;
        sub = p;
        step = p.countMinChars().intValue();
        sub.setParent(null);
    }
    public String toString() {
        return sub.toString()+"{"
            +fewestMatches+","+mostMatches+"}"+
            (matchFewest ? "?" : "")+"(?# <= fast multi)"+
            nextString();
    }
    int step = -1;
    public int matchInternal(int pos,Pthings pt) {
        int m=-1;
        int i=pos;
        int endstr = pt.src.length()-step;
        patInt matches = new patInt(0);
        if(matchFewest) {
            if(fewestMatches.lessEq(matches)) {
                int ii = nextMatch(i,pt);
                if(ii >= 0) return ii;
            }
            while(i >= 0 && i <= endstr) {
                i=sub.matchInternal(i,pt);
                if(i >= 0) {
                    matches.inc();
                    if(fewestMatches.lessEq(matches)) {
                        int ii = nextMatch(i,pt);
                        if(ii >= 0) return ii;
                    }
                    if(matches.equals(mostMatches))
                        return -1;
                }
            }
            return -1;
        }
        int nMatches = 0;
        while(fewestMatches.intValue() > nMatches) {
            i=sub.matchInternal(i,pt);
            if(i >= 0)
                nMatches++;
            else
                return -1;
        }
        m=i;
        if(mostMatches.finite()) {
            while(nMatches < mostMatches.intValue()) {
                i = sub.matchInternal(i,pt);
                if(i>=0) {
                    m=i;
                    nMatches++;
                } else break;
            }
        } else {
            while(true) {
                i = sub.matchInternal(i,pt);
                if(i>=0) {
                    m=i;
                    nMatches++;
                } else break;
            }
        }
        while(m >= pos) {
            int r=nextMatch(m,pt);
            if(r >= 0) return r;
            m -= step;
            nMatches--;
            if(nMatches < fewestMatches.intValue())
                return -1;
        }
        return -1;
    }
    public Pattern clone1(Hashtable h) {
        try {
            FastMulti fm = new FastMulti(fewestMatches,mostMatches,sub.clone(h));
            fm.matchFewest = matchFewest;
            return fm;
        } catch(RegSyntax rs) {
            return null;
        }
    }
}

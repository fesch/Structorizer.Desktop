//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

import java.util.*;

/** Uses table lookup to match [] type constructs, but
    only if it can use a lookup table 256 bits in size.
    It is impractical to make a table if it is too large.
    */
public class FastBracket extends Bracket {
    int min, max;
    BitSet bs;
    FastBracket(boolean n) { super(n); }
    // This routine can optimize a bracket, possibly
    // it will replace it with a FastBracket.
    static Bracket process(Bracket b,boolean ignc) {
        Vector v = b.v;
        b.pv = null;
        try {

            // Expand out the vector to make separate
            // entries for other cases if ignoreCase is
            // turned on.
            Vector nv = v;
            if(ignc) {
                nv = new Vector();
                for(int i=0;i<v.size();i++) {
                    Pattern p = (Pattern)v.elementAt(i);
                    nv.addElement(p);
                    if(p instanceof oneChar) {
                        oneChar oc = (oneChar)p;
                        nv.addElement(new oneChar(oc.altc));
                    } else if(p instanceof Range) {
                        Range ra = (Range)p;
                        nv.addElement(new Range(ra.altlo,ra.althi));
                    }
                }
            }
            v = nv;

            // Bubble sort, make sure elements
            // are in order.  This will allow us
            // to merge them.
            for(int i=0;i<v.size()-1;i++) {
                for(int j=0;j<v.size()-1;j++) {
                    char c1 = getl(v.elementAt(j));
                    char c2 = getl(v.elementAt(j+1));
                    if(c2 < c1) {
                        Object o = v.elementAt(j);
                        v.setElementAt(v.elementAt(j+1),j);
                        v.setElementAt(o,j+1);
                    }
                }
            }

            nv = new Vector();
            // merge -- remove overlaps
            Pattern p = (Pattern)v.elementAt(0);
            nv.addElement(p);
            for(int i=1;i<v.size();i++) {
                if(geth(p)+1 >= getl(v.elementAt(i))) {
                    Pattern p2 = (Pattern)v.elementAt(i);
                    char lo = min(getl(p),getl(p2));
                    char hi = max(geth(p),geth(p2));
                    nv.setElementAt(p=mkelem(lo,hi),nv.size()-1);
                } else {
                    p = (Pattern)v.elementAt(i);
                    nv.addElement(p);
                }
            }

            b.v = v = nv;
        } catch(RegSyntax e) {
            e.printStackTrace();
        }

        // We don't want these things to be empty.
        Vector negv = neg(v);
        if(v.size()==1) return b;
        if(negv.size()==1) {
            b.v = negv;
            b.neg = !b.neg;
            return b;
        }

        // Now consider if we can make a FastBracket.
        // Uses a BitSet to do a lookup.
        FastBracket fb = newbrack(v,b.neg);
        if(fb == null)
            fb = newbrack(negv,!b.neg);
        if(fb != null) {
            fb.parent = b.parent;
            fb.next = b.next;
            return fb;
        }

        // return the normal Bracket.
        return b;
    }

    // Build a FastBracket and set bits.  If this can't
    // be done, return null.
    final static FastBracket newbrack(Vector v,boolean neg) {
        FastBracket fb = new FastBracket(neg);
        fb.v = v;
        if(v.size()==0) return null;
        fb.min = getl(v.elementAt(0));
        fb.max = geth(v.elementAt(v.size()-1));
        if(fb.max-fb.min <= 256) {
            fb.bs = new BitSet(fb.max-fb.min+1);
            for(int i=0;i<v.size();i++) {
                Object o = v.elementAt(i);
                int min0 = getl(o)-fb.min;
                int max0 = geth(o)-fb.min;
                for(int j=min0;j<=max0;j++)
                    fb.bs.set(j);
            }
            return fb;
        }
        return null;
    }

    // Negate a sorted Vector.  Applying this
    // operation twice should yield the same Vector
    // back.
    final static Vector neg(Vector v) {
        try {
            Vector nv = new Vector();
            if(v.size()==0) {
                nv.addElement(new Range((char)0,(char)65535));
                return nv;
            }
            int p0 = getl(v.elementAt(0));
            if(p0!=0)
                nv.addElement(mkelem((char)0,(char)(p0-1) ));
            for(int i=0;i<v.size()-1;i++) {
                int hi = getl(v.elementAt(i+1))-1;
                int lo = geth(v.elementAt(i))+1;
                nv.addElement(mkelem((char)lo,(char)hi));
            }
            int pN = geth(v.lastElement());
            if(pN != 65535)
                nv.addElement(mkelem((char)(pN+1),(char)65535));
            return nv;
        } catch(RegSyntax rs) {
            return null;
        }
    }
    // Make either a Range or oneChar Object, depending on which
    // is appropriate.
    final static Pattern mkelem(char lo,char hi) throws RegSyntax {
        return lo==hi ? (Pattern)(new oneChar(lo)) : (Pattern)(new Range(lo,hi));
    }
    static final char min(char a,char b) {
        return a<b ? a : b;
    }
    static final char max(char a,char b) {
        return a>b ? a : b;
    }

    // getl -- get lower value of Range object,
    // or get value of oneChar object.
    final static char getl(Object o) {
        Pattern p = (Pattern)o;
        if(p instanceof Range)
            return ((Range)p).lo;
        return ((oneChar)p).c;
    }
    // geth -- get higher value of Range object,
    // or get value of oneChar object.
    final static char geth(Object o) {
        Pattern p = (Pattern)o;
        if(p instanceof Range)
            return ((Range)p).hi;
        return ((oneChar)p).c;
    }

    // This is the easy part!
    public int matchInternal(int pos,Pthings pt) {
        if(pos >= pt.src.length() || Masked(pos,pt)) return -1;
        char c = pt.src.charAt(pos);
        return (neg ^ (c >= min && c <= max && bs.get(c-min)) ) ?
            nextMatch(pos+1,pt) : -1;
    }
}

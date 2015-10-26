//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

import java.util.*;

/** This class implements the (?: ... ) extended Pattern.
 It provides a base class from which we derive the
 [ ... ], ( ... ), (?! ... ), and (?= ... ) patterns. */
class Or extends Pattern {
    Vector v;
    Pattern[] pv = null;
    Or() { v = new Vector(); }
    String leftForm() { return "(?:"; }
    String rightForm() { return ")"; }
    String sepForm() { return "|"; }
    public Or addOr(Pattern p) {
        pv = null;
        v.addElement(p);
        p.setParent(this);
        return this;
    }
    public String toString() {
        int i;
        StringBuffer sb = new StringBuffer();
        sb.append(leftForm());
        if(v.size()>0)
            sb.append( ((Pattern)v.elementAt(0)).toString() );
        for(i=1;i<v.size();i++) {
            sb.append(sepForm());
            sb.append( ((Pattern)v.elementAt(i)).toString() );
        }
        sb.append(rightForm());
        sb.append(nextString());
        return sb.toString();
    }
    public int matchInternal(int pos,Pthings pt) {
        if(pv == null) {
            pv = new Pattern[v.size()];
            v.copyInto(pv);
        }
        for(int i=0;i<v.size();i++) {
            Pattern p = pv[i];//(Pattern)v.elementAt(i);
            int r = p.matchInternal(pos,pt);
            if(r >= 0)
                return r;
        }
        return -1;
    }
    public patInt minChars() {
        if(v.size()==0) return new patInt(0);
        patInt m = ((Pattern)v.elementAt(0)).countMinChars();
        for(int i=1;i<v.size();i++) {
            Pattern p = (Pattern)v.elementAt(i);
            m.mineq(p.countMinChars());
        }
        return m;
    }
    public patInt maxChars() {
        if(v.size()==0) return new patInt(0);
        patInt m = ((Pattern)v.elementAt(0)).countMaxChars();
        for(int i=1;i<v.size();i++) {
            Pattern p = (Pattern)v.elementAt(i);
            m.maxeq(p.countMaxChars());
        }
        return m;
    }
    Pattern clone1(Hashtable h) {
        Or o=new Or();
        h.put(this,o);
        h.put(o,o);
        for(int i=0;i<v.size();i++)
            o.v.addElement( ((Pattern)v.elementAt(i)).clone(h) );
        return o;
    }
};

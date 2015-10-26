//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
import java.util.*;
import java.io.*;

/** This class is just like oneChar, but doesn't worry about case. */
class FastChar extends oneChar {
    FastChar(char c) { super(c); }
    public int matchInternal(int p,Pthings pt) {
        return (p < pt.src.length()
            && pt.src.charAt(p)==c) ?
            nextMatch(p+1,pt) : -1;
    }
    Pattern clone1(Hashtable h) {
        return new FastChar(c);
    }
}

/** This class is a hashtable keyed by Character
  * Objects.  It is used to match things of the
  * form (?:a..|b..|c..|..) match with greater efficiency --
  * by using a Hashtable that indexes into the group
  * of patterns.
  */
class Branch extends Pattern {
    Hashtable h = new Hashtable();
    // We need to keep track of the order
    // of the keys -- if we don't then
    // recompiling the output of toString
    // may produce errors by re-ordering
    // ()'s and changing the id number of
    // the backreference associated with
    // a subpattern.
    Vector keys = new Vector();
    Branch() {}
    Pattern clone1(Hashtable x) {
        Branch b = new Branch();
        b.keys = (Vector)keys.clone();
        x.put(this,b);
        x.put(b,b);

        for(int i=0;i<keys.size();i++) {
            Pattern p = (Pattern)h.get(keys.elementAt(i));
            b.h.put(keys.elementAt(i),p.clone(x));
        }
        return b;
    }

    // this function eliminates Branches with 0 or 1 elements.
    final Pattern reduce(boolean ignoreCase,boolean dontMinQ) {
        if(h.size()==1) {
            Enumeration e = h.keys();
            Character c = (Character)e.nextElement();
            Pattern oc;
            if(ignoreCase||dontMinQ)
                oc=new oneChar(c.charValue());
            else oc=new FastChar(c.charValue());
            oc.next = (Pattern)h.get(c);
            oc.add(next);
            return oc;
        } else if(h.size()==0) return null;
        return this;
    }
    public patInt maxChars() {
        Enumeration e = h.keys();
        patInt count = new patInt(0);
        while(e.hasMoreElements()) {
            Object key = e.nextElement();
            Pattern pa = (Pattern)h.get(key);
            patInt pi = pa.maxChars();
            pi.inc();
            count.maxeq(pi);
        }
        return count;
    }
    public patInt minChars() {
        Enumeration e = h.keys();
        patInt count = new patInt(0);
        while(e.hasMoreElements()) {
            Object key = e.nextElement();
            Pattern pa = (Pattern)h.get(key);
            patInt pi = pa.minChars();
            pi.inc();
            count.mineq(pi);
        }
        return count;
    }

    // adds a oneChar object to this Branch
    void addc(oneChar o,boolean ignoreCase,boolean dontMinQ) {
        Pattern n = o.next;
        if(n == null)
            n = new NullPattern();
        else
            n = RegOpt.opt(n,ignoreCase,dontMinQ);
        n.setParent(this);
        set(new Character(o.c),n,ignoreCase,dontMinQ);
        if(ignoreCase) {
            if(o.c != o.altc)
                set(new Character(o.altc),n,ignoreCase,dontMinQ);
            if(o.c != o.altc2 && o.altc != o.altc2)
                set(new Character(o.altc2),n,ignoreCase,dontMinQ);
        }
    }
    void set(Character c,Pattern n,boolean igc,boolean dontMinQ) {
        Pattern p = (Pattern)h.get(c);
        next = null;
        // This letter is not yet used in the Branch object.
        // We need to add it.
        if(p==null) {
            if(n instanceof Or) {
                // A NullPattern is prepended to an Or
                // to prevent confusing this object.
                // For example: (boo|bug) => (b(?:oo|ug))
                // during this process.  However, we
                // want (b(?:oo|ell)|bug)
                NullPattern np = new NullPattern();
                np.add(n);
                h.put(c,np);
            } else {
                h.put(c,n);
            }
            // Make sure we remember the order things were
            // added into the Branch object so that we can
            // properly convert it to a String.
            keys.addElement(c);
        } else if(p instanceof Or) {
            ((Or)p).addOr(n);
        } else if(p instanceof oneChar && n instanceof oneChar
                && ((oneChar)p).c != ((oneChar)n).c) {
            Branch b = new Branch();
            b.addc((oneChar)p,igc,dontMinQ);
            b.addc((oneChar)n,igc,dontMinQ);
            h.put(c,b);
            b.setParent(this);
        } else if(p instanceof Branch && n instanceof oneChar) {
            ((Branch)p).addc((oneChar)n,igc,dontMinQ);
            n.setParent(p);
        } else {
            // Create an Or object to receive the variety
            // of branches in the pattern if the current letter
            // is matched.  We do not attempt to make these
            // sub-branches into a Branch object yet.
            Or o = new Or();
            o.setParent(this);

            // Remove NullPattern from p -- it's no longer needed.
            if(p instanceof NullPattern
                    && p.parent == null && p.next != null) {
                o.addOr(p.next);
            } else {
                o.addOr(p);
            }
            o.addOr(n);

            Pattern optpat = RegOpt.opt(o,igc,dontMinQ);
            h.put(c,optpat);
            optpat.setParent(this);
        }
    }
    public String toString() {
        StringBuffer sb = new StringBuffer();
        // should protect this...
        sb.append("(?:(?#branch)");// Hashtable)");
        for(int i=0;i<keys.size();i++) {
            Character c = (Character)keys.elementAt(i);
            sb.append(c);
            sb.append(h.get(c));
            if(i+1<keys.size())
                sb.append("|");
        }
        sb.append(")");
        sb.append(nextString());
        return sb.toString();
    }
    public int matchInternal(int pos,Pthings pt) {
        if(pos >= pt.src.length()) return -1;
        Pattern n = (Pattern)h.get(new Character(pt.src.charAt(pos)));
        if(n == null) return -1;
        if(pt.cbits != null && pt.cbits.get(pos)) return -1;
        return n.matchInternal(pos+1,pt);
    }
}

/** This is just a place to put the optimizing function.
    It is never instantiated as an Object.  It just sorts
    through the RegOpt looking for things it can change
    and make faster. */
public class RegOpt {
    static Pattern opt(Pattern p,boolean ignoreCase,
        boolean dontMinQ) {
        if(p == null) return p;
        if(p instanceof Bracket) {
            Bracket b = (Bracket)p;
            // FastBracket is the only special
            // optimized class to have its own
            // source file.
            p = FastBracket.process(b,ignoreCase);
            //if(!(p instanceof FastBracket)
            //p = Switch.process(b,ignoreCase);
            p.next = b.next;
            p.parent = b.parent;
        } else if(p instanceof oneChar && !ignoreCase
                && !dontMinQ) {
            oneChar o = (oneChar)p;
            p = new FastChar(o.c);
            p.next = o.next;
            p.parent = o.parent;
        } else if(p instanceof Or
                && ((Or)p).leftForm().equals("(?:")
                && ((Or)p).v.size()==1) { // Eliminate this Or Object.
            Or o = (Or)p;
            p = (Pattern)o.v.elementAt(0);
            p.setParent(null);
            p = RegOpt.opt(p,ignoreCase,dontMinQ);
            p.add(o.next);
        } else if(p instanceof Or) {
            Or o = (Or)p;
            o.pv = null;
            Vector v = o.v;
            o.v = new Vector();
            Branch b = new Branch();
            b.parent = o.parent;
            for(int i=0;i<v.size();i++) {
                Pattern pp = (Pattern)v.elementAt(i);
                // We want to have at least two oneChar's in
                // the Or Object to consider making a Branch.
                if(pp instanceof oneChar && (b.h.size()>=1 ||
                        (i+1<v.size() && v.elementAt(i+1) instanceof oneChar)))
                    b.addc((oneChar)pp,ignoreCase,dontMinQ);
                else {
                    if(b.keys.size() > 0) {
                        Pattern p2 = (Pattern)b.reduce(ignoreCase,dontMinQ);
                        if(p2 != null) {
                            o.addOr(p2);
                            b = new Branch();
                            b.parent = o.parent;
                        }
                    }
                    o.addOr(opt(pp,ignoreCase,dontMinQ));
                }
            }
            if(b.keys.size()>0) {
                Pattern p2=(Pattern)b.reduce(ignoreCase,dontMinQ);
                if(p2 != null)
                    o.addOr(p2);
            }
            if(o.v.size()==1
                    && o.leftForm().equals("(?:")) { // Eliminate Or Object
                p = (Pattern)o.v.elementAt(0);
                p.setParent(null);
                p = RegOpt.opt(p,ignoreCase,dontMinQ);
                p.add(o.next);
            }
        } else if(p instanceof FastMulti) {
            PatternSub ps = (PatternSub)p;
            ps.sub = RegOpt.opt(ps.sub,ignoreCase,dontMinQ);
        } else if(p instanceof Multi && safe4fm( ((PatternSub)p).sub )) {
            Multi m = (Multi)p;
            FastMulti fm = null;
            try {
                fm = new FastMulti(m.a,m.b,
                    opt(m.sub,ignoreCase,dontMinQ));
            } catch(RegSyntax rs) {}
            fm.parent = m.parent;
            fm.matchFewest = m.matchFewest;
            fm.next = m.next;
            p = fm;
        }
        if(p.next != null)
            p.next = opt(p.next,ignoreCase,dontMinQ);
        return p;
    }
    final static boolean safe4fm(Pattern x) {
        while(x != null) {
            if(x instanceof Bracket)
                ;
            else if(x instanceof Range)
                ;
            else if(x instanceof oneChar)
                ;
            else if(x instanceof Any)
                ;
            else if(x instanceof Custom
                    && ((Custom)x).v instanceof UniValidator)
                ;
            else if(x instanceof Or) {
                Or o = (Or)x;
                if(!o.leftForm().equals("(?:"))
                    return false;
                patInt lo = o.countMinChars();
                patInt hi = o.countMaxChars();
                if(!lo.equals(hi))
                    return false;
                for(int i=0;i<o.v.size();i++)
                    if(!safe4fm((Pattern)o.v.elementAt(i)) )
                        return false;
            } else return false;
            x = x.next;
        }
        return true;
    }
    /*
    public static void setParents(Regex r) {
      setParents(r.thePattern,null);
    }
    static void setParents(Pattern p,Pattern x) {
      if(p instanceof PatternSub && !(p instanceof FastMulti)
      && !(p instanceof DotMulti))
        RegOpt.setParents( ((PatternSub)p).sub, p);
      else if(p instanceof Or && !(p instanceof Bracket)) {
        Or o = (Or)p;
        for(int i=0;i<o.v.size();i++)
          RegOpt.setParents((Pattern)o.v.elementAt(i),o);
      } else if(p instanceof Branch) {
        Branch b = (Branch)p;
        Enumeration e = b.h.keys();
        while(e.hasMoreElements()) {
          Object o = e.nextElement();
          RegOpt.setParents( (Pattern)b.h.get(o), b);
        }
      }
      if(p.next == null)
        p.parent = x;
      else {
        p.parent = null;
        RegOpt.setParents(p.next,x);
      }
    }*/
}

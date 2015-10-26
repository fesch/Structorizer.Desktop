//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
import java.util.*;
/**
        Shareware: package pat
   <a href="copyright.html">Copyright 2001, Steven R. Brandt</a>
*/ /**
Class Pattern is the base class on which all the other pattern
elements are built. */

public abstract class Pattern {
    /** The ESC character, the user can provide his own value
            for the escape character through regex.esc */
    public final static char ESC = '\\';
    final static String PROTECT_THESE = "[]{}(),$,-\"^.";

    /** The interal match function, it must be provided by any
        class which wishes to extend Pattern. */
    public abstract int matchInternal(int i,Pthings p);
    public abstract String toString();

    // Class Pattern is a singly linked list
    // chained together by member next.  The member
    // parent is used so that sub patterns can access
    // the chain they are branching from.
    Pattern next=null,parent=null;

    /** This gets the next element of a Pattern that
            we wish to match.  If we are at the end of a
            subchain of patterns, it will return us to the
            parent chain. */
    public Pattern getNext() {
        return next != null ? next :
        (parent == null ? null : parent.getNext());
    }
    /** Call this method if you have a pattern element that
            takes a sub pattern (such as Or), and
            after you have added a sub pattern to the current
            pattern element. */
    public void setParent(Pattern p) {
        if(next != null) next.setParent(p);
        else parent = p;
    }
    /** This determines if the remainder of a Pattern
            matches.  Type "return nextMatch" from within
            matchInternal if the current
            Pattern matches.  Otherwise, return a -1.*/
    public int nextMatch(int i,Pthings pt) {
        Pattern p = getNext();
        /*if(p == null) return i;
                return p.matchInternal(i,pt);*/
        return p==null ? i : p.matchInternal(i,pt);
    }
    /** This is a toString() for the remainder
            of the Pattern elements after this one.
            use this when overriding toString(). Called from
            within toString(). */
    public String nextString() {
        if(next == null) return "";
        return next.toString();
    }

    /** a method to detect whether char c is in String s */
    final static boolean inString(char c,String s) {
        int i;
        for(i=0;i<s.length();i++)
            if(s.charAt(i)==c)
                return true;
        return false;
    }

    /** A method to create a string that protects the characters
            listed in PROTECT_THESE by prepending the esc character.
            The esc character itself is automatically protected. */
    final static
        String protect(String s,String PROTECT_THESE,char esc) {
        int i;
        StringBuffer sb = new StringBuffer();
        String p = PROTECT_THESE+esc;
        for(i=0;i<s.length();i++) {
            char c = s.charAt(i);
            if(inString(c,p))
                sb.append(esc);
            sb.append(c);
        }
        return sb.toString();
    }

    /** This can be used to perform a match test from
            within class Pattern. */
    public int match(StringLike s,Pthings pt) {
        return matchAt(s,0,pt);
    }
    /** This can be used to perform a match test from
            within class Pattern. */
    public int matchAt(StringLike s,int i,Pthings pt) {
        pt.src = s;
        int r = matchInternal(i,pt);
        if(r < 0) return -1;
        mfrom = r<i ? r+1 : i;
        return r<i ? i-r-1 : r-i;
    }
    int mfrom=0;

    // Detect masked characters
    final boolean Masked(int i,Pthings pt) {
        return pt.cbits == null ? false : pt.cbits.get(i);
    }

    /** add a Pattern to the singly-linked Pattern chain. */
    public Pattern add(Pattern p) {
        if(next == null) {
            if(p==null) return this;
            next = p;
            p.parent = parent;
            parent = null;
        } else next.add(p);
        return this;
    }
    /** The minimum number of characters which
        this pattern element can match. */
    public patInt minChars() { return new patInt(0); }
    /** The maximum number of characters which
        this pattern element can match. */
    public patInt maxChars() { return new patInf(); }
    /** return minimum number of characters in pattern */
    public final patInt countMinChars() {
        Pattern p = this;
        patInt sum = new patInt(0);
        while(p != null) {
            sum.pluseq(p.minChars());
            p = p.next;
        }
        return sum;
    }
    /** return maximum number of characters in pattern */
    public final patInt countMaxChars() {
        Pattern p = this;
        patInt sum = new patInt(0);
        while(p != null) {
            sum.pluseq(p.maxChars());
            p = p.next;
        }
        return sum;
    }

    // This method is only needed by Multi_stage2 so far...
    // the reason is that it may try something else after a
    // match succeeds.  OrMark will only record the last thing
    // tried in marks, so we need to backup the result of the
    // last successful match and restore it if the next one
    // does not succeed.
    final int testMatch(Pattern p,int pos,Pthings pt) {
        int[] tab = null;
        if(pt.marks != null) try {
                tab = new int[pt.marks.length];
                for(int i=0;i<tab.length;i++)
                    tab[i] = pt.marks[i];
            } catch(Throwable t) {}
        int ret = p.matchInternal(pos,pt);
        if(ret < 0) pt.marks = tab;
        return ret;
    }

    /** Clones this pattern elements without cloning others in the
        linked list. */
    Pattern clone1(Hashtable h) {
        throw new Error("No such method as clone1 for "+getClass().getName());
    }
    Pattern clone(Hashtable h) {
        Pattern p = (Pattern)h.get(this);
        if(p != null) {
            return p;
        }
        p=clone1(h);
        if(p==null)throw new Error("Null from clone1!");
        h.put(this,p);
        h.put(p,p);
        if(next != null) p.next = next.clone(h);
        if(parent != null) p.parent = parent.clone(h);
        return p;
    }
    public boolean equals(Object o) {
        return o == this;
    }
};


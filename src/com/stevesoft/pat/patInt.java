//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

/** This is just an integer that can have infinite value.
    It is used internally to implement the *, and + parts
    of regular expressions.
*/
public class patInt {
    int i;
    boolean inf;
    /** Initialize to zero. */
    public patInt() { i = 0; inf = false; }
    /** Initialize to the value of init. */
    public patInt(int init) { i = init; inf = false; }
    /** Initialize to the value of p. */
    public patInt(patInt p) { i = p.i; inf = p.inf; }
    /** set this int to infinity. */
    public void setInf(boolean b) {
        inf = b;
        if(b) i = Integer.MAX_VALUE;
    }
    /** Increment the value of this by 1. */
    public final void inc() {
        if(!inf) i++;
    }
    /** Decrement the value of this by 1. */
    public final void dec() {
        if(!inf) i--;
    }
    /** Test to see if this is less than or equal to j. */
    public final boolean lessEq(patInt j) { /*
                if(inf) return false;
                if(j.inf) return true;
                return i <= j.i; */
        return !inf && (j.inf || i <= j.i);
    }
    /** Test to see if two patterns are equal. */
    public final boolean equals(patInt j) {
        return !j.inf && !inf && i==j.i;
    }
    /** Formats the pattern as a String.  Contrary to
         what you might expect, infinity is formatted as "" */
    final public String toString() {
        if(inf) return "";
        else return ""+i;
    }
    /** This would be operator+=(patInt) if I were programming
         in C++. */
    public final patInt pluseq(patInt p) {
        if(inf||p.inf) setInf(true);
        else i += p.i;
        return this;
    }
    /** Returns a patInt with value equal to the product
         of the value of p and this. */
    public final patInt mul(patInt p) {
        if(inf||p.inf) return new patInf();
        return new patInt(i*p.i);
    }
    /** If the argument p has a smaller value than this,
         then set this Object equal to p. */
    public final patInt mineq(patInt p) {
        if(p.inf) return this;
        if(inf) i = p.i;
        else if(p.i < i) i = p.i;
        setInf(false);
        return this;
    }
    /** If the argument p has a greater than this,
         then set this object equal to p. */
    public final patInt maxeq(patInt p) {
        if(inf || p.inf) { setInf(true); return this; }
        if(p.i > i) i = p.i;
        return this;
    }
    /** Tests to see if this represents an infinite quantity. */
    public boolean finite() { return !inf; }
    /** Converts to a patInt to an int.  Infinity is
         mapped Integer.MAX_VALUE;
        */
    public int intValue() { return inf ? Integer.MAX_VALUE : i; }
};

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
This class is used to store a result from Regex */
public class RegRes implements Cloneable {
    protected int[] marks = null;
    protected boolean didMatch_ = false;
    protected StringLike src=null;

    /** Obtain the text String that was matched against. */
    public String getString() { return src.toString(); }
    /** Obtain the source StringLike object. */
    public StringLike getStringLike() { return src; }
    protected int charsMatched_=0,matchFrom_=0,numSubs_=0;
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("match="+matchedFrom()+":"+charsMatched());
        if(!didMatch()) return sb.toString();
        for(int i=0;i<numSubs();i++) {
            int n = i + 1;
            sb.append(" sub("+n+")="+matchedFrom(n)+
                ":"+charsMatched(n));
        }
        return sb.toString();
    }
    public RegRes() {}
    public RegRes(RegRes r) {
      copyOutOf(r);
    }
    public void copyOutOf(RegRes r) {
        if(r.marks == null)
            marks = null;
        else try {
                //marks = (Hashtable)r.marks.clone();
                marks = new int[r.marks.length];
                for(int i=0;i<marks.length;i++)
                    marks[i]=r.marks[i];
                //marks = (int[])r.marks.clone();
            } catch (Throwable t) {}
        didMatch_ = r.didMatch_;
        src = r.src;
        charsMatched_ = r.charsMatched_;
        matchFrom_ = r.matchFrom_;
        numSubs_ = r.numSubs_;
    }
    public Object clone() { return new RegRes(this); }
    public boolean equals(RegRes r) {
        if(charsMatched_!=r.charsMatched_
                || matchFrom_   !=r.matchFrom_
                || didMatch_    !=r.didMatch_
                || numSubs_     !=r.numSubs_
                || !src.unwrap().equals(r.src.unwrap()))
            return false;
        if(marks==null && r.marks!=null)
            return false;
        if(marks!=null && r.marks==null)
            return false;
        for(int i=1;i<=numSubs_;i++) {
            if(matchedFrom(i) != r.matchedFrom(i))
                return false;
            else if(charsMatched(i) != r.charsMatched(i))
                return false;
        }
        return true;
    }
    /** Obtains the match if successful, null otherwise.*/
    public String stringMatched() {
        int mf=matchedFrom(), cm = charsMatched();
        return !didMatch_ || mf<0 || cm<0 ? null :
        src.substring(mf,mf+cm);
    }
    /** Obtains the position backreference number i begins to match, or
         -1 if backreference i was not matched. */
    public int matchedFrom(int i) {
        if(marks==null||i>numSubs_) return -1;
        //Integer in=(Integer)marks.get("left"+i);
        //return in == null ? -1 : in.intValue();
        return marks[i];
    }
    /** Obtains the number of characters matched by backreference i, or
         -1 if backreference i was not matched. */
    public int charsMatched(int i) {
        if(marks==null||i>numSubs_||!didMatch_) return -1;
        //Integer in = (Integer)marks.get("right"+i);
        //int i2 = in==null ? -1 : in.intValue();
        int mf = matchedFrom(i);
        return mf < 0 ? -1 : marks[i+numSubs_]-matchedFrom(i);
    }
    /** This is either equal to matchedFrom(i)+charsMatched(i) if the match
        was successful, or -1 if it was not. */
    public int matchedTo(int i) {
        if(marks==null||i>numSubs_||!didMatch_) return -1;
        return marks[i+numSubs_];
    }
    /** Obtains a substring matching the nth set
                of parenthesis from the pattern. See
                numSubs(void), or null if the nth backrefence did
                not match. */
    public String stringMatched(int i) {
        int mf = matchedFrom(i), cm = charsMatched(i);
        return !didMatch_ || mf<0 || cm<0 ? null :
        src.substring(mf,mf+cm);
    }
    /** This returns the part of the string that preceeds the match,
         or null if the match failed.*/
    public String left() {
        int mf = matchedFrom();
        return !didMatch_ || (mf<0) ? null : src.substring(0,mf);
    }
    /** This returns the part of the string that follows the ith
                backreference, or null if the backreference did not match. */
    public String left(int i) {
        int mf = matchedFrom(i);
        return !didMatch_ || (mf<0) ? null : src.substring(0,mf);
    }
    /** This returns the part of the string that follows the match,
         or null if the backreference did not match.*/
    public String right() {
        int mf = matchedFrom(), cm = charsMatched();
        return !didMatch_ || mf<0 || cm<0 ? null : src.substring(mf+
            cm,src.length());
    }
    /** This returns the string to the right of the ith backreference,
         or null if the backreference did not match. */
    public String right(int i) {
        int mf = matchedFrom(i), cm = charsMatched(i);
        return !didMatch_ || mf<0 || cm<0 ? null :
        src.substring(mf+cm,src.length());
    }
    /** After a successful match, this returns the location of
                the first matching character, or -1 if the match failed.*/
    public int matchedFrom() { return !didMatch_ ? -1 : matchFrom_; }
    /** After a successful match, this returns the number of
                characters in the match, or -1 if the match failed. */
    public int charsMatched() { return !didMatch_||matchFrom_<0 ? -1 : charsMatched_; }
    /** This is matchedFrom()+charsMatched() after a successful match,
        or -1 otherwise. */
    public int matchedTo() { return !didMatch_ ? -1 : matchFrom_+charsMatched_;}
    /** This returns the number of
                backreferences (parenthesis) in the pattern,
                i.e. the pattern "(ab)" has
                one, the pattern "(a)(b)" has two, etc. */
    public int numSubs() { return numSubs_; }
    /** Contains true if the last match was successful. */
    public boolean didMatch() { return didMatch_; }

    /** An older name for matchedFrom. */
    public int matchFrom() { return matchedFrom(); }
    /** An older name for stringMatched(). */
    public String substring() { return stringMatched(); }
    /** An older name for matchedFrom. */
    public int matchFrom(int i) { return matchedFrom(i); }
    /** An older name for stringMatched. */
    public String substring(int i) { return stringMatched(i); }
}

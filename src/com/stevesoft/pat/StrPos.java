//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
/** 
        Shareware: package pat
   <a href="copyright.html">Copyright 2001, Steven R. Brandt</a>
*/ /**
StrPos is used internally by regex to parse the regular expression. */
public class StrPos {
    String s;
    int pos;
    /** Return the position in the string pointed to */
    public int pos() { return pos; }

    /** This contains the escape character, which is \ by default. */
    public char esc=Pattern.ESC;
    char c;
    /** Returns the current, possibly escaped, character. */
    public char thisChar() { return c; }

    boolean dontMatch,eos;

    /** tell whether we are at end of string */
    public boolean eos() { return eos; }
    /** initialize a StrPos from another StrPos. */
    public StrPos(StrPos sp) {
        dup(sp);
    }
    /** copy a StrPos from sp to this. */
    public void dup(StrPos sp) {
        s = sp.s;
        pos = sp.pos;
        c = sp.c;
        dontMatch = sp.dontMatch;
        eos = sp.eos;
    }
    /** Initialize a StrPos by giving it a String, and a
         position within the String. */
    public StrPos(String s,int pos) {
        this.s=s;
        this.pos=pos-1;
        inc();
    }
    /** Advance the place where StrPos points within the String.
         Counts a backslash as part of the next character. */
    public StrPos inc() {
        pos++;
        if(pos >= s.length()) {
            eos = true;
            return this;
        }
        eos = false;
        c = s.charAt(pos);
        if(c == esc && pos+1<s.length()) {
            pos++;
            c = s.charAt(pos);
            if(c != esc)
                dontMatch = true;
            else
                dontMatch = false;
        } else
            dontMatch = false;
        return this;
    }
    /** Compare the (possibly escaped) character
         pointed to by StrPos.  Return true if they are the
         same, but lways return if character pointed to is escaped. */
    public boolean match(char ch) {
        if(dontMatch || eos) return false;
        return c == ch;
    }
    /** As match, but only matches if the character is escaped. */
    public boolean escMatch(char ch) {
        if(!dontMatch || eos) return false;
        return c == ch;
    }

    /** Returns true if the current
        character is escaped (preceeded by "\"). */
    public boolean escaped() { return dontMatch; }
    /** Increment the string pointer by each character in
         <pre>st</pre> that matches a non-escaped
         character. */
    public boolean incMatch(String st) {
        StrPos sp = new StrPos(this);
        int i;
        for(i=0;i<st.length();i++) {
            if(!sp.match(st.charAt(i)) )
                return false;
            sp.inc();
        }
        dup(sp);
        return true;
    }
    /** Read in an integer. */
    public patInt getPatInt() {
        patInt pi = null;
        if(incMatch("inf"))
            return new patInf();
        int i,cnt=0;
        StrPos sp = new StrPos(this);
        for(i=0;!sp.eos && sp.c >= '0' && sp.c <= '9';i++) {
            cnt = 10*cnt+sp.c-'0';
            sp.inc();
        }
        if(i==0) return null;
        dup(sp);
        return new patInt(cnt);
    }
    /** get the string that we are processing. */
    public String getString() { return s; }
};

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
The RegexTokenizer is similar to the StringTokenizer class
provided with java, but allows one to tokenize using
regular expressions, rather than a simple list of characters.
Tokens are any strings between the supplied regular expression,
as well as any backreferences (things in parenthesis)
contained within the regular expression. */
public class RegexTokenizer implements Enumeration {
    String toParse;
    Regex r;
    int count = 0;
    Vector v = new Vector();
    Vector vi = new Vector();
    int pos=0;

    int offset = 1;
    void getMore() {
        String s = r.right();
        if(r.searchFrom(toParse,pos)) {
            v.addElement(r.left().substring(pos));
            vi.addElement(new Integer(r.matchFrom()+
                r.charsMatched()));
            for(int i=0;i<r.numSubs();i++)
                if(r.substring() != null) {
                    v.addElement(r.substring(i+offset));
                    vi.addElement(
                        new Integer(r.matchFrom(i+offset)+
                        r.charsMatched(i+offset)));
                }
            pos = r.matchFrom()+r.charsMatched();
        } else if(s != null) v.addElement(s);
    }

    /** Initialize the tokenizer with a string of text and a pattern */
    public RegexTokenizer(String txt,String ptrn) {
        toParse = txt;
        r = new Regex(ptrn);
        offset = r.BackRefOffset;
        getMore();
    }
    /** Initialize the tokenizer with a Regex object. */
    public RegexTokenizer(String txt,Regex r) {
        toParse = txt;
        this.r = r;
        offset = r.BackRefOffset;
        getMore();
    }
    /** This should always be cast to a String, as in StringTokenizer,
         and as in StringTokenizer one can do this by calling
         nextString(). */
    public Object nextElement() {
        if(count >= v.size()) getMore();
        return v.elementAt(count++);
    }
    /** This is the equivalent (String)nextElement(). */
    public String nextToken() { return (String)nextElement(); }
    /** This asks for the next token, and changes the pattern
         being used at the same time. */
    public String nextToken(String newpat) {
        try { r.compile(newpat); } catch (RegSyntax r_) {}
        return nextToken(r);
    }
    /** This asks for the next token, and changes the pattern
         being used at the same time. */
    public String nextToken(Regex nr) {
        r = nr;
        if(vi.size() > count) {
            pos = ((Integer)vi.elementAt(count)).intValue();
            v.setSize(count);
            vi.setSize(count);
        }
        getMore();
        return nextToken();
    }
    /** Tells whether there are more tokens in the pattern. */
    public boolean hasMoreElements() {
        if(count >= v.size()) getMore();
        return count < v.size();
    }
    /** Tells whether there are more tokens in the pattern, but
         in the fashion of StringTokenizer. */
    public boolean hasMoreTokens() { return hasMoreElements(); }
    /** Determines the # of remaining tokens */
    public int countTokens() {
        int old_pos=pos,_count=count;
        while(hasMoreTokens())
            nextToken();
        count=_count;
        return v.size()-count;
    }
    /** Returns all tokens in the String */
    public String[] allTokens() {
        countTokens();
        String[] ret = new String[v.size()];
        v.copyInto(ret);
        return ret;
    }
};

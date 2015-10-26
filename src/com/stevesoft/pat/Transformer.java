//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

import java.util.Vector;
import com.stevesoft.pat.wrap.StringWrap;

/** Replacement rule used by the Transformer.
    @see com.stevesoft.pat.Transformer
    */
class TransRepRule extends ReplaceRule {
    Transformer t;
    TransRepRule(Transformer t) { this.t = t; }
    public String toString1() { return ""; }
    public Object clone1() { return new TransRepRule(t); }
    public void apply(StringBufferLike sb,RegRes rr) {
        // get the ReplaceRule of the Regex that matched.
        next = t.tp.ra[t.tp.pn].getReplaceRule();
    }
}

/** Sometimes you want to replace a whole bunch of things
  that might occur within a single line of text.  One efficient
  way to do this, both in terms of performance and programming
  ease, is with Transformer. The Transformer contains an array
  of Regex's and uses the Regex that matches earliest within the
  text to do the replacing, if two Regex's match at the same
  time it uses the one put in the Transformer first.
  <p>
  This feature can be used to prevent transformations from
  occurring in certain regions. For example, if I add the rule
  s'//.*'$&' and then add the
  rule s/hello/goodbye/ the Transformer will replace "hello"
  with "goodbye" except when it occurs inside a double-slash
  style of comment.   The transformation on the comment goes first,
  does nothing, and precludes transformation on the same region
  of text as the s/hello/goodbye/ rule.
  <p>
  So far, at least, this class does not have the capability of
  turning into a giant robot :-)
  */
public class Transformer {
    TransPat tp;
    Regex rp = new Regex();
    boolean auto_optimize;

    /** Get a replacer to that works with the current Regex.
     @see com.stevesoft.pat.Replacer
     */
    public Replacer getReplacer() { return rp.getReplacer(); }

    /** Instantiate a new Transformer object. */
    public Transformer(boolean auto) {
        auto_optimize = auto;
        tp = new TransPat();
        rp.setReplaceRule(new TransRepRule(this));
        rp.thePattern = tp;
    }

    /** Add a new Regex to the set of Regex's. */
    public void add(Regex r) {
        if(auto_optimize) r.optimize();
        tp.ra[tp.ra_len++] = r;
        if(tp.ra.length==tp.ra_len) {
            Regex[] ra2 = new Regex[tp.ra_len+10];
            for(int i=0;i<tp.ra_len;i++)
                ra2[i] = tp.ra[i];
            tp.ra = ra2;
        }
        rp.numSubs_ = r.numSubs_ > rp.numSubs_ ? r.numSubs_ : rp.numSubs_;
    }

    /** Returns the number of Regex's in this Transformer. */
    public int patterns() { return tp.ra_len; }

    /** Get the Regex at position i in this Transformer. */
    public Regex getRegexAt(int i) {
        if(i >= tp.ra_len)
            throw new ArrayIndexOutOfBoundsException("i="+i+">="+patterns());
        if(i < 0)
            throw new ArrayIndexOutOfBoundsException("i="+i+"< 0");
        return tp.ra[i];
    }
    /** Set the Regex at position i in this Transformer. */
    public void setRegexAt(Regex rx,int i) {
        if(i >= tp.ra_len)
            throw new ArrayIndexOutOfBoundsException("i="+i+">="+patterns());
        if(i < 0)
            throw new ArrayIndexOutOfBoundsException("i="+i+"< 0");
        tp.ra[i] = rx;
    }

    /** Add a new Regex by calling Regex.perlCode
        @see com.stevesoft.pat.Regex#perlCode(java.lang.String)
        */
    public void add(String rs) {
        Regex r = Regex.perlCode(rs);
        if(r == null) throw new NullPointerException("bad pattern to Regex.perlCode: "+rs);
        add(r);
    }
    /** Add an array of Strings (which will be converted to
        Regex's via the Regex.perlCode method.
        @see com.stevesoft.pat.Regex#perlCode(java.lang.String)
        */
    public void add(String[] array) {
        for(int i=0;i<array.length;i++)
            add(array[i]);
    }
    /** Replace all matches in the current String. */
    public String replaceAll(String s) {
        return dorep(s,0,s.length());
    }
    public StringLike replaceAll(StringLike s) {
        return dorep(s,0,s.length());
    }
    /** Replace all matching patterns beginning at position start. */
    public String replaceAllFrom(String s,int start) {
        return dorep(s,start,s.length());
    }
    /** Replace all matching patterns beginning between the positions
        start and end inclusive. */
    public String replaceAllRegion(String s,int start,int end) {
        return dorep(s,start,end);
    }

    Replacer repr = new Replacer();
    final StringLike dorep(StringLike s,int start,int end) {
        StringLike tfmd = repr.replaceAllRegion(s,rp,start,end);
        tp.lastMatchedTo = repr.lastMatchedTo;
        return tfmd;
    }
    final String dorep(String s,int start,int end) {
        return dorep(new StringWrap(s),start,end).toString();
    }

    /** Replace the first matching pattern in String s. */
    public String replaceFirst(String s) {
        return dorep(s,0,s.length());
    }
    /** Replace the first matching pattern after position start in
        String s. */
    public String replaceFirstFrom(String s,int start) {
        return dorep(s,start,s.length());
    }
    /** Replace the first matching pattern that begins between
        start and end inclusive. */
    public String replaceFirstRegion(String s,int start,int end) {
        return dorep(s,start,end);
    }
}

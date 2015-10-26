//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

import java.util.*;
import com.stevesoft.pat.wrap.StringWrap;

/** Internally used class. */
class RegHolder {
    Regex me = null;
    RegHolder prev = null;
}

/** Internally used class.
  * @see CodeRule
  */
class CodeVal {
    int pos;
    char code;
    CodeVal(int p,char c) {
        pos = p;
        code = c;
    }
    public String toString() {
        return "("+pos+","+code+")";
    }
}

/**
  To use this class, first use either the getReplacer() method from
  Transformer or Regex.  You can then use replaceAll, replaceFirst,
  etc. methods on the Replacer in the same way that you can from
  either of those two classes.
  <p>
  The only potential difference between using the methods of
  Replacer to do the replacing is that Replacer remembers changes
  to the replacing object between calls to replaceAll, replaceFirst
  etc.  For details, see the example file
  <a href="http://javaregex.com/code/trans3.java.html">trans3.java</a>.
  @see com.stevesoft.pat.Transformer
  @see com.stevesoft.pat.Regex
*/
public class Replacer {
    boolean first;

    /** Instantiate a new Replacer. */
    public Replacer() {}

    public StringLike replaceFirstRegion(String s,Regex r,
        int start,int end) {
        return replaceFirstRegion(new StringWrap(s),r,start,end);
    }
    /** This method replaces the first occurence of the Regex in the
        String starting with position pos
        according to the Replacer rule of this object. */
    public StringLike replaceFirstRegion(StringLike s,Regex r,
        int start,int end) {
        first = true;
        rh.me = r;
        rh.prev = null;
        return dorep(s,start,end);
    }
    public StringLike replaceFirst(StringLike s) {
        return replaceFirstRegion(s,0,s.length());
    }
    public StringLike replaceFirstFrom(StringLike s,int start) {
        return replaceFirstRegion(s,start,s.length());
    }
    public StringLike replaceFirstRegion(StringLike s,int start,int end) {
        first = true;
        return dorep(s,start,end);
    }

    RegHolder rh = new RegHolder();

    public StringLike replaceAllRegion(String s,Regex r,
        int start, int end) {
        return replaceAllRegion(new StringWrap(s),r,start,end);
    }
    /** This method replaces all occurences of the Regex in the
        String starting with postition pos
        according to the Replacer rule of this object. */
    public StringLike replaceAllRegion(StringLike s,Regex r,
        int start,int end) {
        first = false;
        // reset
        rh.me = r;
        rh.prev = null;
        return dorep(s,start,end);
    }
    public StringLike replaceAll(StringLike s) {
        return replaceAllRegion(s,0,s.length());
    }
    public StringLike replaceAllFrom(StringLike s,int start) {
        return replaceAllRegion(s,start,s.length());
    }
    public StringLike replaceAllRegion(StringLike s,int start,int end) {
        first = false;
        return dorep(s,start,end);
    }

    public String replaceAll(String s) {
        return replaceAllRegion(new StringWrap(s),0,s.length()).toString();
    }
    public String replaceAllFrom(String s,int start) {
        return replaceAllRegion(new StringWrap(s),start,s.length()).toString();
    }
    public String replaceAllRegion(String s,int start,int end) {
        first = false;
        return dorep(new StringWrap(s),start,end).toString();
    }

    final public boolean isSpecial(ReplaceRule x) {
        while(x != null) {
            if(x instanceof SpecialRule
                    || (x instanceof RuleHolder && ((RuleHolder)x).held instanceof SpecialRule))
                return true;
            x = x.next;
        }
        return false;
    }
    final public void apply1(RegRes rr) {
        rr.charsMatched_++;
        apply(rr,null);
        rr.charsMatched_--;
    }

    final StringLike dorep(StringLike s,int start,int end) {
        StringLike ret = s;
        want_more_text = false;
        lastMatchedTo = 0;
        if(rh.me == null)
            throw new NullPointerException("Replacer has null Regex pointer");
        if(rh.me._search(s,start,end)) {
            int rmn = rh.me.matchedTo();
            if(rh.me.charsMatched()==0 && !isSpecial(rh.me.getReplaceRule())) {
                apply1(rh.me);
                rmn++;
            }
            apply(rh.me);
            if(!first)
                for(int i=rmn;
                        !want_more_text && rh.me._search(s,i,end);i=rmn) {
                    rmn = rh.me.matchedTo();
                    if(rh.me.charsMatched()==0) {
                        if(!isSpecial(rh.me.getReplaceRule()))
                            apply1(rh.me);
                        rmn++;
                    }
                    apply(rh.me);
                }
            ret = finish();
            ret = ret == null ? s : ret;
        }
        return ret;
    }

    StringBufferLike sb = null;
    StringLike src = null;
    int pos = 0;
    /** This method allows you to apply the results of several
        matches in a sequence to modify a String of text.  Each
        call in the sequence must operate on the same piece of
        text and the matchedFrom() of each RegRes given to this
        method must be greater in value than the preceeding
        RegRes's matchedTo() value.
        */
    public void apply(RegRes r,ReplaceRule rp) {
        if(rp==null ||(rp.next == null && rp instanceof AmpersandRule))
            return;
        if(r.didMatch()) {
            if(src == null)
                src = r.getStringLike();
	    if(sb == null)
                sb = new StringBufferLike(src.newStringBufferLike());
            int rmf = r.matchedFrom();
            for(int ii=pos;ii<rmf;ii++)
              sb.append(src.charAt(ii));

            Vector v = new Vector();
            for(ReplaceRule x=rp;x != null;x=x.next) {
                x.apply(sb,r);
                if(x instanceof SpecialRule) {
                    if(x instanceof WantMoreTextReplaceRule
                            && want_more_text_enable)
                        want_more_text = true;
                    else if(x instanceof PushRule) {
                        RegHolder rh2 = new RegHolder();
                        rh2.me = ( (PushRule)x ).NewRule;
                        rh2.prev = rh;
                        rh = rh2;
                    } else if(x instanceof PopRule) {
                        if(rh.prev != null)
                            rh = rh.prev;
                    } else if(x instanceof ChangeRule) {
                        rh.me = ( (ChangeRule) x).NewRule;
                    }
                }
            }
            if(!want_more_text)
                pos = r.matchedTo();
        }
    }
    boolean want_more_text = false, want_more_text_enable = false;
    public boolean WantMoreText() { return want_more_text; }
    /** Another form of apply, it is the same as
        apply(r,r.getReplaceRule()). */
    public void apply(Regex r) { apply(r,r.getReplaceRule()); }

    /** This finishes the replacement, appending the right() part of
        the last RegRes given to substitute(RegRes).  After this method
        is called, the Replace object is reset to perform another
        substitution. If no RegRes objects with a true didMatch are
        applied, this returns null. */
    public StringLike finish() {
        if(src==null)
            return null;
        //sb.append(src.substring(pos,src.length()));
        int s_end = src.length();
        for(int ii=pos;ii<s_end;ii++)
          sb.append(src.charAt(ii));
        src = null;
        lastMatchedTo = pos;
        pos = 0;
        StringLike retstr = sb.toStringLike();
        sb = null;
        return retstr;
    }
    int lastMatchedTo = 0;
    public Object clone() {
        Replacer r = new Replacer();
        r.first = first;
        r.src = src;
        r.sb = sb;
        r.pos = pos;
        r.lastMatchedTo = lastMatchedTo;
        r.want_more_text = want_more_text;
        r.want_more_text_enable = want_more_text_enable;
        r.rh.me = rh.me;
        r.rh.prev = rh.prev;
        return r;
    }
    public int lastMatchedTo() { return lastMatchedTo; }
    public Regex getRegex() {
      return rh.me;
    }
    public void setSource(StringLike sl) {
      src = sl;
    }
    public void setBuffer(StringBufferLike sbl) {
      sb = sbl;
    }
    public void setPos(int pos) {
      this.pos = pos;
    }
}

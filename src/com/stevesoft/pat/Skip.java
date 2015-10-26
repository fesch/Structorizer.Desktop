//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

/** This class is used internally to search ahead for some
    optimized Regex objects.  It searches within a String
    for occrences of a given String -- like a more flexible
    version of String.indexOf.
    @see com.stevesoft.pat.Skip2
    @see com.stevesoft.pat.SkipBMH
    */
public class Skip {
    static int mkmask(int c) {
        char x = (char)c;
        return ~( CaseMgr.toUpperCase(x) |
            CaseMgr.toLowerCase(x) |
            CaseMgr.toTitleCase(x));
    }
    static { int x = Regex.BackRefOffset; }
    String src;
    int c,mask;
    int offset;
    boolean ign,m1;
    /** Examine a Regex to determine what String it will
        attempt to skip to when searching for patterns.
        Return -1 if we aren't doing this. */
    public static String string(Regex r) {
        return r.skipper == null ? null : r.skipper.src;
    }
    /** Determine the offset of the String within the pattern
        that we are skipping to. Return -1 if we aren't doing
        this.  */
    public static int offset(Regex r) {
        return r.skipper == null ? -1 : r.skipper.offset;
    }
    /** Initialize, give it a String to search for, tell it
        whether or not to ignoreCase, and what the offset is
        of the String within the String to be searched. */
    public Skip(String s,boolean ign,int o) {
        src = s;
        c = s.charAt(0);
        if(ign) {
            mask = mkmask(c);
        } else mask = 0;
        offset = o;
        this.ign = ign;
        m1 = (s.length()==1);
    }
    /** The same as find(s,0,s.length()) */
    public final int find(StringLike s) {
        return find(s,0,s.length());
    }
    static final int min(int a,int b) { return a<b ? a : b; }
    /** Searches a given region of text beginning at position start
        and ending at position end for the skip object. */
    public int find(StringLike s,int start,int end) {
        if(start > end) return -1;
        start += offset;
        int vend = min(s.length()-1,end+offset);
        if(mask != c) {
            for(int i=start;i<=vend;i++)
                if(0 == (s.charAt(i) & mask))
                    //if(m1||s.regionMatches(ign,i,src,0,src.length()) )
                    if(m1||CaseMgr.regionMatches(s,ign,i,src,0,src.length()) )
                        return i-offset;
        } else {
            for(int i=start;i<=vend;i++)
                if(c == s.charAt(i))
                    //if(m1||s.regionMatches(ign,i,src,0,src.length()) )
                    if(m1||CaseMgr.regionMatches(s,ign,i,src,0,src.length()) )
                        return i-offset;
        }
        return -1;
    }
    static Skip findSkip(Regex r) {
        return findSkip(r.thePattern,r.ignoreCase,!r.dontMatchInQuotes);
    }
    // look for things that can be skipped
    static Skip findSkip(Pattern p,boolean ignoreCase,boolean trnc) {
        StringBuffer sb = new StringBuffer();
        Skip subsk = null;
        int offset = 0;
        int skipc = -1,skipoff=0;
        for(;p != null;p = p.next) {
            if(p instanceof oneChar) {
                skipc = ((oneChar)p).c;
                skipoff = offset;
            }
            if(p instanceof oneChar && p.next instanceof oneChar) {
                Pattern psav = p;
                sb.append(((oneChar)p).c);
                while(p.next instanceof oneChar) {
                    sb.append(((oneChar)p.next).c);
                    p = p.next;
                }
                String st = sb.toString();
                char c0 = st.charAt(0), c1 = st.charAt(1);
                Skip sk=null;
                if(st.length()>2)
                    sk = new SkipBMH(st,ignoreCase,offset);
                else
                    sk = new Skip2(st,ignoreCase,offset);
                if(trnc && st.length()>2) { // chop out a whole string...
                    psav.next = new Skipped(st.substring(1));
                    psav.next.next = p.next;
                    psav.next.parent = p.parent;
                }
                return sk;
            } else if(p instanceof Or && ((Or)p).v.size()==1
                    && !((Or)p).leftForm().equals("(?!")
                    && null != (subsk=
                    findSkip( (Pattern)((Or)p).v.elementAt(0),ignoreCase,trnc) )) {
                subsk.offset += offset;
                return subsk;
            } else if(p.minChars().equals(p.maxChars())) {
                offset += p.minChars().intValue();
            } else return skipc < 0 ? null :
                new Skip(""+(char)skipc,ignoreCase,skipoff);
        }
        return null;
    }
}

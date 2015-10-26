//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

import com.stevesoft.pat.wrap.StringWrap;

/** Like Skip, but implements a
    <a href="http://www.dcc.uchile.cl/~rbaeza/handbook/algs/7/713b.srch.p.html">
    Boyer-Moore-Horspool</a> type search
    method that has been modified to be more like a "T-search" (see
    the Michael Tamm''s article in <i>C'T, magazin fuer computer und technic</i>, August 97
    p 292).  Yet another important source of information for me was
    the <a href="http://www.go2net.com/people/paulp/deep/1997/05/14/">
    Deep Magic</a> article on string searching.  As of this writing, I can
    beat String's indexOf method in many cases.
    @see com.stevesoft.pat.Skip
    @see com.stevesoft.pat.Skip2
    */
public class SkipBMH extends Skip {
    // This number could be 256, but I think it's
    // big enough.  Note, it must be a power of 2.
    final int MAX_CHAR = 64;
    final char[] skip = new char[MAX_CHAR];
    int sm1;
    int jump_ahead = 0;
    char uc,lc,tc,x;
    final boolean exact(char c) {
        return (ign && anyc(c))||c==x;
    }
    final boolean anyc(char c) {
        return c==uc||c==lc||c==tc;
    }
    public SkipBMH(String pt,boolean ign) { this(pt,ign,0); }
    public SkipBMH(String pt) { this(pt,false,0); }
    public SkipBMH(String pt,boolean ign,int offset) {
        super(pt,ign,offset);
        for(int k=0;k<MAX_CHAR;k++)
            skip[k] = (char)src.length();

        sm1 = src.length()-1;
        x = src.charAt(sm1);
        uc=CaseMgr.toUpperCase(x);
        lc=CaseMgr.toLowerCase(x);
        tc=CaseMgr.toTitleCase(x);

        // We don't really want 65536 long arrays in skip[],
        // so we mask of the higher bits.  This can be combined
        // with ignore case, so accounting for upper
        // case costs us nothing extra.
        for(int k=0;k<src.length()-1;k++) {
            char x_ = src.charAt(k);
            if(ign) {
                char uc_ = CaseMgr.toUpperCase(x_);
                char lc_ = CaseMgr.toLowerCase(x_);
                char tc_ = CaseMgr.toTitleCase(x_);
                skip[uc_ & (MAX_CHAR-1)]=(char)(src.length()-k-1);
                skip[lc_ & (MAX_CHAR-1)]=(char)(src.length()-k-1);
                skip[tc_ & (MAX_CHAR-1)]=(char)(src.length()-k-1);
            } else
                skip[x_ & (MAX_CHAR-1)] = (char)(src.length()-k-1);
        }

        // This trick can be found in the July issue of
        // C-T magazine.  This makes the method a type of
        // "T-search."
        jump_ahead = src.length()-1;
        for(int k=0;k<src.length()-1;k++) {
            char y=src.charAt(sm1-k-1);
            if(exact(y)) {
                jump_ahead = k;
                break;
            }
        }
    }
    /** Set to true if you only want to compare two of the
        characters in the String. */
    final public int searchRegion(String s,int start,int end) {
        return find(s,start,end);
    }
    final public int searchFrom(String s,int start) {
        return find(s,start,s.length());
    }
    final public int search(String s) { return find(s,0,s.length()); }
    public int find(String s,int start,int end) {
        start += offset+sm1;
        int vend = min(s.length()-1,end+sm1+offset),k;
        int vend1 = vend-jump_ahead;
        if(ign) {
            for(k=start; k <= vend1;k += skip[s.charAt(k) & (MAX_CHAR-1)] ) {
                // table look-up is expensive, avoid it if possible
                if( anyc(s.charAt(k)) ) {
                    if(CaseMgr.regionMatches(src,ign,0,s,k-sm1,sm1))
                        return k-sm1-offset;
                    k += jump_ahead;
                }
            }
            for(; k <= vend;k += skip[s.charAt(k) & (MAX_CHAR-1)] ) {
                // table look-up is expensive, avoid it if possible
                if( anyc(s.charAt(k)) ) {
                    if(CaseMgr.regionMatches(src,ign,0,s,k-sm1,sm1))
                        return k-sm1-offset;
                    k += jump_ahead;
                    if(k > vend) return -1;
                }
            }
        } else {
            for(k=start; k <= vend1;k += skip[s.charAt(k) & (MAX_CHAR-1)] ) {
                // table look-up is expensive, avoid it if possible
                if( x==s.charAt(k) ) {
                    //if(src.regionMatches(0,s,k-sm1,sm1))
                    if(CaseMgr.regionMatches(src,false,0,s,k-sm1,sm1))
                        return k-sm1-offset;
                    k += jump_ahead;
                }
            }
            for(; k <= vend;k += skip[s.charAt(k) & (MAX_CHAR-1)] ) {
                // table look-up is expensive, avoid it if possible
                if( x==s.charAt(k) ) {
                    //if(src.regionMatches(0,s,k-sm1,sm1))
                    if(CaseMgr.regionMatches(src,false,0,s,k-sm1,sm1))
                        return k-sm1-offset;
                    k += jump_ahead;
                    if(k > vend) return -1;
                }
            }
        }

        return -1;
    }
    public int find(StringLike s,int start,int end) {
        if(s instanceof StringWrap)
          return find(s.toString(),start,end);
        start += offset+sm1;
        int vend = min(s.length()-1,end+sm1+offset),k;
        int vend1 = vend-jump_ahead;
        if(ign) {
            for(k=start; k <= vend1;k += skip[s.charAt(k) & (MAX_CHAR-1)] ) {
                // table look-up is expensive, avoid it if possible
                if( anyc(s.charAt(k)) ) {
                    if(CaseMgr.regionMatches(src,ign,0,s,k-sm1,sm1))
                        return k-sm1-offset;
                    k += jump_ahead;
                }
            }
            for(; k <= vend;k += skip[s.charAt(k) & (MAX_CHAR-1)] ) {
                // table look-up is expensive, avoid it if possible
                if( anyc(s.charAt(k)) ) {
                    if(CaseMgr.regionMatches(src,ign,0,s,k-sm1,sm1))
                        return k-sm1-offset;
                    k += jump_ahead;
                    if(k > vend) return -1;
                }
            }
        } else {
            for(k=start; k <= vend1;k += skip[s.charAt(k) & (MAX_CHAR-1)] ) {
                // table look-up is expensive, avoid it if possible
                if( x==s.charAt(k) ) {
                    //if(src.regionMatches(0,s,k-sm1,sm1))
                    if(CaseMgr.regionMatches(src,false,0,s,k-sm1,sm1))
                        return k-sm1-offset;
                    k += jump_ahead;
                }
            }
            for(; k <= vend;k += skip[s.charAt(k) & (MAX_CHAR-1)] ) {
                // table look-up is expensive, avoid it if possible
                if( x==s.charAt(k) ) {
                    //if(src.regionMatches(0,s,k-sm1,sm1))
                    if(CaseMgr.regionMatches(src,false,0,s,k-sm1,sm1))
                        return k-sm1-offset;
                    k += jump_ahead;
                    if(k > vend) return -1;
                }
            }
        }

        return -1;
    }
}

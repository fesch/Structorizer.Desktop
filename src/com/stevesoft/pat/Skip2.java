//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

/** This is the same as Skip, except it needs a minimum
    of two characters in the initializing String.
    @see com.stevesoft.pat.Skip
    @see com.stevesoft.pat.SkipBMH
    */
public class Skip2 extends Skip {
    int c1,mask1;
    public Skip2(String s,boolean ign,int offset) {
        super(s,ign,offset);
        c1 = s.charAt(1);
        m1 = 2==s.length();
        if(ign) {
            mask1=mkmask(c1);
        } else
            mask1 = 0;
    }
    public int find(StringLike s,int start,int end) {
        if(start > end) return -1;
        start += offset;
        int vend = min(s.length()-2,end+offset);
        for(int i=start;i<=vend;i++)
            if(0 == (s.charAt(i)&mask) && 0 == (s.charAt(i+1)&mask1)) {
                //if(m1||s.regionMatches(ign,i,src,0,src.length()) )
                if(m1||CaseMgr.regionMatches(s,ign,i,src,0,src.length()) )
                    return i-offset;
            }
        return -1;
    }
}

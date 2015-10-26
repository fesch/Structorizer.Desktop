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
This class only exists to store data needed during the
compilation of a regular expression. */
public class Rthings {
    /** The numeric identity of the next () to be encountered
         while compiling the pattern. */
    public int val=Regex.BackRefOffset;
    /** Needed in case (?i) is encountered, to pass back the
         message that ignoreCase should be set. */
    public boolean ignoreCase;
    /** Needed in case (?Q) is encountered, to pass back the
         message that dontMatchInQuotes should be set. */
    public boolean dontMatchInQuotes;
    public boolean optimizeMe = false;
    public boolean noBackRefs = false;
    public int parenLevel = 0;
    boolean gFlag = false, mFlag = false, sFlag = false;
    Pattern p;
    Or o;
    Rthings(Regex r) {
        ignoreCase = r.ignoreCase;
        dontMatchInQuotes = r.dontMatchInQuotes;
    }
    void set(Regex r) {
        r.gFlag = gFlag;
	r.mFlag = mFlag;
	r.sFlag = sFlag;
        r.ignoreCase = ignoreCase;
        r.dontMatchInQuotes = dontMatchInQuotes;
        if(optimizeMe) r.optimize();
    }
};

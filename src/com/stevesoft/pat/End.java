//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
import java.util.Hashtable;

//class AddToEnd extends RegSyntax {};

/** Compiles the '$' or the '\Z' Pattern.  It is
 an error to have further Pattern elements after
 '\Z'.  It is the end of the String. */
class End extends Pattern {
    boolean retIsEnd;
    End(boolean b) { retIsEnd = b; }
    public int matchInternal(int pos,Pthings pt) {
        if(retIsEnd && pt.mFlag && pos < pt.src.length()) {
            if(pt.src.charAt(pos)=='\n') {
                return nextMatch(pos,pt);
	    }
	}
        if(pt.src.length() == pos)
            return nextMatch(pos,pt);
	else if(pos<pt.src.length())
	    // Access the next character...
	    // this is crucial to making 
	    // RegexReader work.
	    pt.src.charAt(pos);
        return -1;
    }
    public String toString() {
        if(retIsEnd)
            return "$";
        else
            return "\\Z";
    }
    public patInt maxChars() { return new patInt(1); }
    public Pattern clone1(Hashtable h) { return new End(retIsEnd); }
};

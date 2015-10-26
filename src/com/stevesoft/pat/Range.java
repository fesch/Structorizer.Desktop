//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
import java.util.Hashtable;

/** Thrown when one encounters things like [z-a] */
class BadRangeArgs extends RegSyntax {};

/** Implments a subelement (ranges) of the [] pattern element.
For example, [a-z023] is implemented using a range and tree oneChar
classes.
@see Bracket
@see oneChar
*/
class Range extends Pattern {
    char lo,hi,altlo,althi;
    boolean printBrackets = false;
    public String toString() {
        String s=protect(""+lo,PROTECT_THESE,ESC)+"-"
            +protect(""+hi,PROTECT_THESE,ESC);
        if(!printBrackets)
            return s;
        return "["+s+"]";
    }
    Range(char loi,char hii) throws RegSyntax {
        lo = loi; hi = hii;
        oneChar o = null;
        if(lo >= hi)
            //throw new BadRangeArgs();
            RegSyntaxError.endItAll("Badly formed []'s : "+lo+" >= "+hi);
        o = new oneChar(lo);
        altlo = o.altc;
        o = new oneChar(hi);
        althi = o.altc;
    }
    public int matchInternal(int pos,Pthings pt) {
        if(pos >= pt.src.length()) return -1;
        if(Masked(pos,pt)) return -1;
        char c = pt.src.charAt(pos);
        if(lo <= c && c <= hi ||
                (pt.ignoreCase && (altlo <= c && c <= althi)))
            return nextMatch(pos+1,pt);
        return -1;
    }
    public patInt minChars() { return new patInt(1); }
    public patInt maxChars() { return new patInt(1); }
    public Pattern clone1(Hashtable h) {
        try {
            Range r = new Range(lo,hi);
            r.printBrackets = printBrackets;
            return r;
        } catch(RegSyntax rs) {
            return null;
        }
    }
};

//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

/** The apply method of this ReplaceRule simply appends the text
    it was initialized with to the StringBufferLike.
    @see com.stevesoft.pat.ReplaceRule
    */
public class StringRule extends ReplaceRule {
    String s;
    public StringRule(String s) { this.s = s; }
    public void apply(StringBufferLike sb,RegRes res) {
        sb.append(s);
    }
    public String toString1() { return s; }
    public Object clone1() { return new StringRule(s); }
}

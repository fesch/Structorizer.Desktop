//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

/** This method implements the pattern elements $1, $2, etc in
    a substitution rule. The apply(StringBufferLike sb,RegRes rr) method of this ReplaceRule
    simply appends the contents of rr.stringMatched(n), where n is
    the integer supplied to the constructor. */
public class BackRefRule extends ReplaceRule {
    int n;
    public BackRefRule(int n) { this.n = n; }
    public void apply(StringBufferLike sb,RegRes res) {
        String x = res.stringMatched(n);
        sb.append(x == null ? "" : x);
    }
    public String toString1() { return "$"+n; }
    public Object clone1() { return new BackRefRule(n); }
}

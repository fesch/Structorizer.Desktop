//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

/** The apply(StringBufferLike sb,RegRes res) method of this derivation
    of ReplaceRule appends the contents of res.left() to the StringBuffer
    sb.
    @see com.stevesoft.pat.ReplaceRule
    */
public class LeftRule extends ReplaceRule {
    public LeftRule() {}
    public void apply(StringBufferLike sb,RegRes res) {
        sb.append(res.left());
    }
    public String toString1() { return "$`"; }
}

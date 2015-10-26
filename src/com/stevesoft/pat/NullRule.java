//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

/** The apply(StringBufferLike sb,RegRes res) method of this derivation
    of ReplaceRule appends nothing to the contents of the StringBuffer sb.
    @see com.stevesoft.pat.ReplaceRule
    */
public class NullRule extends ReplaceRule {
    public NullRule() {}
    public void apply(StringBufferLike sb,RegRes res) {
    }
    public String toString1() { return ""; }
}

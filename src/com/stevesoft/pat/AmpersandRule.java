//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

/** This implements the $& element of the second argument to
  * Regex.
  * @see com.stevesoft.pat.ReplaceRule
  */
public final class AmpersandRule extends ReplaceRule {
    public AmpersandRule() {}
    public void apply(StringBufferLike sb,RegRes res) {
        sb.append(res.stringMatched());
    }
    public String toString1() { return "$&"; }
}

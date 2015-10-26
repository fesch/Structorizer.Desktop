//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

/** Triggers the reading of another line of text, allowing a longer
    pattern to match -- for details see
    <a href="http://javaregex.com/code/WantMore.java.html">WantMore.java</a>.
    */
public class WantMoreTextReplaceRule extends SpecialRule {
    public WantMoreTextReplaceRule() {}
    public void apply(StringBufferLike sb,RegRes res) {
    }
    public String toString1() { return "${WANT_MORE_TEXT}"; }
}

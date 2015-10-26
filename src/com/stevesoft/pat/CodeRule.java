//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

/** Implements the rules for \U, \L, \E, \Q in substitutions. */
public final class CodeRule extends SpecialRule {
    char c = 'E';
    public CodeRule() {}
    public CodeRule(char c) {
        this.c = c;
    }
    public void apply(StringBufferLike sb,RegRes res) {
      sb.setMode(c);
    }
    public String toString1() { return "\\"+c; }
}

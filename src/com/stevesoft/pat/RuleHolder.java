//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
/** This class is used internally. */
class RuleHolder extends ReplaceRule {
    ReplaceRule held = null;
    RuleHolder() {}
    RuleHolder(ReplaceRule h) { held = h; }
    public Object clone1() { return new RuleHolder(held); }
    public String toString1() { return held.toString1(); }
    public void apply(StringBufferLike sb,RegRes rr) {
        held.apply(sb,rr);
    }
    public ReplaceRule arg(String s) { return new RuleHolder(held.arg(s)); }
}

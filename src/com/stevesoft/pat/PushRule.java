//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

/** See the example file
    <a href="http://javaregex.com/code/trans3.java.html">trans3.java</a> for
    further examples of how this is used.  You will probably not
    want to call it directly. */
public class PushRule extends SpecialRule {
    Regex NewRule;
    public PushRule(PushRule p) { NewRule = p.NewRule; }
    public PushRule(String nm,Regex rr) { name=nm; NewRule = rr; }
    public PushRule(String nm,Transformer tr) { name = nm; NewRule = tr.rp; }
    public Object clone1() { return new PushRule(this); }
    public String String1() { return "${+"+name+"}"; }
    public void apply(StringBufferLike sbl,RegRes rr) {}
}

//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

/** This class implements user defined special replacement rules
  * of the form ${=name}. See <a href="http://javaregex.com/code/trans2.java.html">trans2.java</a>
  * and <a href="http://javaregex.com/code/trans2a.java.html">trans2a.java</a>.
  */
public class ChangeRule extends SpecialRule {
    Regex NewRule;
    public ChangeRule(ChangeRule c) { NewRule=c.NewRule; }
    public ChangeRule(String nm,Regex rr) { name=nm; NewRule = rr; }
    public ChangeRule(String nm,Transformer tr) { name=nm; NewRule = tr.rp; }
    public Object clone1() { return new ChangeRule(this); }
    public String toString1() { return "${="+name+"}"; }
    public void apply(StringBufferLike sb,RegRes rr) {}
}

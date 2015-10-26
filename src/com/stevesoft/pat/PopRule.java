//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

/** Implements substitution rule $POP. See the example
  * file <a href="http://javaregex.com/code/trans3.java.html">trans3.html</a>.
  */
public class PopRule extends SpecialRule {
    public PopRule() {}
    public String toString1() { return "${POP}"; }
}

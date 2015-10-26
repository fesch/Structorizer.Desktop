//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
import java.util.Hashtable;

/** Simple custom patterns.  See
    <a href="http://javaregex.com/code/deriv2.java.html">deriv2.java</a>
    and <a href="http://javaregex.com/code/deriv3.java.html">deriv3.java</a>
    in the test directory.
    @see com.stevesoft.pat.CustomEndpoint
 */
class Custom extends PatternSub {
    String select;
    Validator v;
    int start;
    Custom(String s) {
        select = s;
        v = (Validator)Regex.validators.get(s);
    }
    public int matchInternal(int pos,Pthings pt) {
        start = pos;
        return sub.matchInternal(pos,pt);
    }
    public String toString() {
        String a = v.argsave == null ? "" : ":"+v.argsave;
        return "(??"+select+a+")"+nextString();
    }
    public patInt minChars() { return v.minChars(); }
    public patInt maxChars() { return v.maxChars(); }
    Pattern clone1(Hashtable h) {
        Custom c = new Custom(select);
        h.put(c,c);
        h.put(this,c);
        c.sub = sub.clone(h);
        return c;
    }
}

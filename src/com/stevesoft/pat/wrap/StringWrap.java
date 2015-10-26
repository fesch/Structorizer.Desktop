//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat.wrap;

import com.stevesoft.pat.*;

/** A basic wrapper for the String object. Regex does
    not search String directly any longer, it searches StringLike. */
public class StringWrap implements StringLike {
    String s;
    public StringWrap(String s) {
      this.s = s;
    }
    public String toString() { return s; }
    public char charAt(int i) { return s.charAt(i); }
    public int length() { return s.length(); }
    public String substring(int i1,int i2) {
        return s.substring(i1,i2);
    }
    public Object unwrap() { return s; }
    public BasicStringBufferLike newStringBufferLike() {
      return new StringBufferWrap();
    }

    public int indexOf(char c) {
      return s.indexOf(c);
    }
}

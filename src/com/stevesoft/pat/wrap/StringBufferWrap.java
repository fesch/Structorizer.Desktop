//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat.wrap;

import com.stevesoft.pat.*;

/** This provides a wrapper for StringBuffer to
    capture the output of a replacement. */
public class StringBufferWrap
    implements BasicStringBufferLike
  {
  StringBuffer sb = new StringBuffer();
  public void append(char c) {
    sb.append(c);
  }
  public void append(String s) {
    sb.append(s);
  }
  public int length() {
    return sb.length();
  }
  public String toString() {
    return sb.toString();
  }
  public StringLike toStringLike() {
    return new StringWrap(sb.toString());
  }
  public Object unwrap() {
    return sb;
  }
}

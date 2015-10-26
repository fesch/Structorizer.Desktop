//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

import java.io.*;

/** This class allows you to match on a partial string.
    If the allowOverRun flag is true, then the
    length() method returns a number 1 larger than
    is actually contained by the class.
    <p>
    If one attempts to access the last character as
    follows:
    <pre>
    StringBuffer sb = ...;
    ...
    PartialBuffer pb = new PartialBuffer(sb);
    char c = pb.charAt(pb.length()-1);
    </pre>
    then two things happen.  First, a zero is returned
    into the variable c.  Second, the overRun flag is
    set to "true."  Accessing data beyond the end of
    the buffer is considered an "overRun" of the data.
    <p>
    This can be helpful in determining whether more
    characters are required for a match to occur, as
    the pseudo-code below illustrates.
    <pre>
    int i = ...;
    Regex r = new Regex("some pattern");
    pb.allowOverRun = true;
    pb.overRun = true;
    boolean result = r.matchAt(pb,i);
    if(pb.overRun) {
      // The result of the match is not relevant, regardless
      // of whether result is true or false.  We need to
      // append more data to the buffer and try again.
      ....
      sb.append(more data);
    }
    </pre>
    */
class PartialBuffer implements StringLike {
  int off;
  public boolean allowOverRun = true;
  public boolean overRun = false;
  StringBuffer sb;
  PartialBuffer(StringBuffer sb) {
    this.sb = sb;
  }
  public char charAt(int n) {
    n += off;
    if(n == sb.length()) {
      overRun = true;
      return 0;
    }
    return sb.charAt(n);
  }
  public int length() {
    return allowOverRun ? sb.length()+1 : sb.length();
  }
  public int indexOf(char c) {
    for(int i=0;i<sb.length();i++)
      if(sb.charAt(i)==c)
        return i;
    return -1;
  }
  public Object unwrap() { return sb; }
  public String substring(int i1,int i2) {
    StringBuffer sb = new StringBuffer(i2-i1);
    for(int i=i1;i<i2;i++)
      sb.append(charAt(i));
    return sb.toString();
  }
  /** Just returns null. */
  public BasicStringBufferLike newStringBufferLike() {
    return null;
  }
}

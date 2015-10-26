package//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
com.stevesoft.pat.wrap;

import com.stevesoft.pat.*;

/** Allows the outcome of a replaceAll() or replaceFirst()
    to be an array of characters rather than a String.
    */
public class CharArrayBufferWrap
  implements BasicStringBufferLike
  {
  StringBuffer sb = new StringBuffer();
  public void append(char c) {
    sb.append(c);
  }
  public void append(String s) {
    sb.append(s);
  }
  public StringLike toStringLike() {
    char[] ca = new char[sb.length()];
    for(int i=0;i<ca.length;i++)
      ca[i] = sb.charAt(i);
    return new CharArrayWrap(ca);
  }
  public int length() { return sb.length(); }
  public String toString() {
    return sb.toString();
  }
  public Object unwrap() {
    return sb;
  }
}

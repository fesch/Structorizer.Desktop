//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat.wrap;

import com.stevesoft.pat.*;
import java.io.*;

/** Allows the outcome of a replaceAll() or replaceFirst()
    to be directed to a Writer rather than a String.
    <p>
    The method toStringLike() cannot work, however.
    This means that the return value of replaceAll() will
    be null if this Object is used as the StringBufferLike.*/
public class WriterWrap
  implements BasicStringBufferLike
  {
  Writer w;
  public WriterWrap(Writer w) {
    this.w = w;
  }
  public void append(char c) {
    try {
      w.write((int)c);
    } catch(IOException ioe) {}
  }
  public void append(String s) {
    try {
      w.write(s);
    } catch(IOException ioe) {}
  }

  /** This operation can't really be done. */
  public StringLike toStringLike() {
    return null;
  }

  public Object unwrap() {
    return w;
  }
}

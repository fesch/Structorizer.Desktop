package//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
com.stevesoft.pat;

import com.stevesoft.pat.*;

/** A tool that is used to make the \E, \U, \L, and \Q
    elements of a substitution. */
public class StringBufferLike implements BasicStringBufferLike {
  BasicStringBufferLike sbl;
  public StringBufferLike(BasicStringBufferLike sbl) {
    this.sbl = sbl;
  }
  char mode = 'E', altMode = ' ';
  public StringLike toStringLike() {
    return sbl.toStringLike();
  }
  public String toString() {
    return sbl.toString();
  }
  public void append(char c) {
    
    switch(mode) {
    case 'u':
      mode = altMode;
      altMode = ' ';
    case 'U':
      sbl.append(CaseMgr.toUpperCase(c));
      break;
    case 'l':
      mode = altMode;
      altMode = ' ';
    case 'L':
      sbl.append(CaseMgr.toLowerCase(c));
      break;
    case 'Q':
      if((c >= 'a' && c <= 'z')
      || (c >= 'A' && c <= 'Z')
      || (c >= '0' && c <= '9'))
        ;
      else
        sbl.append('\\');
    default:
      sbl.append(c);
      break;
    }
  }
  public void append(String s) {
    for(int i=0;i<s.length();i++)
      append(s.charAt(i));
  }
  public void setMode(char c) {
    if(c == 'u' || c == 'l')
      if(altMode == ' ') altMode = mode;
    mode = c;
  }
  public Object unwrap() {
    return sbl.unwrap();
  }
}

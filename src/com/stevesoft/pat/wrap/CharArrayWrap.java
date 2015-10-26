//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat.wrap;

import com.stevesoft.pat.*;

/** This provides a wrapper for a char array so that
    it can be searched by Regex. */
public class CharArrayWrap implements StringLike {
    char[] ca;
    public char[] getCharArray() { return ca; }
    public CharArrayWrap(char[] ca) { this.ca = ca; }
    public String toString() {
      return new String(ca);
    }
    public char charAt(int i) { return ca[i]; }
    public int length() { return ca.length; }
    public String substring(int i1,int i2) {
        StringBuffer sb = new StringBuffer();
        for(int i=i1;i<i2;i++)
            sb.append(ca[i]);
        return sb.toString();
    }
    public Object unwrap() { return ca; }
    public BasicStringBufferLike newStringBufferLike() {
      return new CharArrayBufferWrap();
    }
    public int indexOf(char c) {
      for(int i=0;i<ca.length;i++)
        if(ca[i] == c)
          return i;
      return -1;
    }
}

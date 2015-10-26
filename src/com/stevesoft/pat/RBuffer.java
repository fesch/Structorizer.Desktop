//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

import java.io.*;
import com.stevesoft.pat.wrap.*;

/** This class is used internally by RegexReader to
    store blocks of data. */
class RBuffer {
  boolean done = false;
  StringBuffer sb;
  int pos,epos;
  RBuffer next;
  RBuffer() {}
  RBuffer(StringBuffer sb) { this.sb = sb; }
  public String toString() {
    return "sb="+sb.toString().replace('\n',' ')+
      " pos="+pos+" epos="+epos+
      " sb.length()="+sb.length()+
      "\n"+sp(pos+3)+"^"+sp(epos-pos-1)+"^";
  }
  String sp(int n) {
    if(n<=0)
      return "";
    StringBuffer sb = new StringBuffer(n);
    for(int i=0;i<n;i++)
      sb.append(' ');
    return sb.toString();
  }
}

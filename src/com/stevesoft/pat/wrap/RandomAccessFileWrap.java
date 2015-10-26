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

/** Provides a wrapper for a RandomAccessFile so that it
    can be searched by Regex. */
public class RandomAccessFileWrap implements StringLike {

    long offset = 0;
    public void setOffset(long o) {
      offset = o;
      i0 = iend = 0;
    }
    public long getOffset() {
      return offset;
    }
    RandomAccessFile raf;
    int i0=0,iend=0;
    byte[] buf = new byte[1024];

    public int getBufferSize() {
      return buf.length;
    }

    public void setBufferSize(int bs) {
      buf = new byte[bs];
      i0 = iend = 0;
    }

    public RandomAccessFileWrap(String file) throws IOException {
        this.raf = new RandomAccessFile(file,"r");
    }
    public RandomAccessFileWrap(RandomAccessFile raf) {
        this.raf = raf;
    }

    public char charAt(int i) {
        if(i >= i0 && i < iend)
            return (char)buf[i-i0];

        try {
            i0 = i-5;
            //if(i0+offset<0) i0=(int)(-offset);
            if(i0<0) i0=0;
            raf.seek(i0+offset);
            iend = i0+raf.read(buf,0,buf.length);

            if(i >= i0 && i < iend)
                return (char)buf[i-i0];
        } catch(Throwable t) {}

        throw new ArrayIndexOutOfBoundsException("Out of bounds for file:"+
          " i="+i+
          ", Final Buffer: i0="+i0+
          " iend="+iend);
    }

    public String toString() { throw new Error("Not implemented"); }
    public int length() {
      try {
        long len = raf.length()-offset;
        if(len > Integer.MAX_VALUE)
          return Integer.MAX_VALUE;
        return (int)len;
      } catch(IOException ioe) {
        return 0;
      }
    }
    public String substring(int i1,int i2) {
        StringBuffer sb = new StringBuffer();
        for(int i=i1;i<i2;i++)
            sb.append(charAt(i));
        return sb.toString();
    }
    public Object unwrap() { return raf; }

    public static void main(String[] files) throws IOException {
      for(int i=0;i<files.length;i++) {
        RandomAccessFileWrap fw =
          new RandomAccessFileWrap(new RandomAccessFile(files[i],"r"));
        Regex r = new Regex("toString\\(\\) *(?@{})");
        r.setGFlag(true);
        r.optimize();
        System.out.print(files[i]+" ");
        int j=0;
        do {
          if(r.searchFrom(fw,j)) {
            System.out.println("Matched at index: "+
             r.matchedFrom());
            j=r.matchedTo();
          } else
            System.out.println("not found");
          System.out.println(r.stringMatched());
        } while(r.didMatch());
      }
    }

    public BasicStringBufferLike newStringBufferLike() {
      return new StringBufferWrap();
    }

    public int indexOf(char c) {
      for(int i=0;i<length();i++)
        if(charAt(i)==c)
          return i;
      return -1;
    }
}

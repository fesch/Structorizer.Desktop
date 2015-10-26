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

/** This class allows you to replace the text in strings
    as you read them in.  Be careful what you do with
    this freedom... using Regex.perlCode("s{.*}{x}s")
    as your pattern will result in loading the entire
    contents of the Reader into memory.
    */
public class RegexReader extends Reader {
  RBuffer rb = new RBuffer(new StringBuffer());
  PartialBuffer wrap = new PartialBuffer(rb.sb);
  boolean moreToRead = true;
  Reader r;
  Replacer rp;

  // the buffer size
  int nmax = 2*1024;

  public RegexReader(Regex rex,Reader r) {
    this.r = r;
    rp = rex.getReplacer();
  }
  public RegexReader(Transformer tex,Reader r) {
    this.r = r;
    rp = tex.getReplacer();
  }
  public void reset() throws IOException {
    r.reset();
    rb = new RBuffer(new StringBuffer());
    wrap = new PartialBuffer(rb.sb);
    moreToRead = true;
  }
  void readData() throws IOException {
    int c;
    int n = 0;
    while( (c = r.read()) != -1) {
      rb.sb.append((char)c);
      if(n++ > nmax)
        break;
    }
    if(c == -1 && n == 0) {
      moreToRead = false;
      wrap.allowOverRun = false;
    }
  }
  void getMoreData() throws IOException {
    while(rb.pos >= rb.epos) {
      wrap.overRun = false;
      if(rb.next != null) {
	rb = rb.next;
      } else if(rb.done) {
        break;
      } else if(rb.epos >= rb.sb.length()
	     && rb.epos > nmax) {
	rb.pos = 1;
	rb.epos = 1;
	rb.sb.setLength(1);
	readData();
      } else if(rb.epos >= rb.sb.length()
	     && moreToRead) {
        readData();
      } else if(rp.getRegex().matchAt(wrap,rb.epos)) {
	if(wrap.overRun) {
	  readData();
	} else {
	  StringBufferWrap sbw = new StringBufferWrap();
	  StringBufferLike sbl = new StringBufferLike(sbw);
	  /*
          ReplaceRule rr = rex.getReplaceRule();
	  while(rr != null) {
	    rr.apply(sbl,rex);
	    rr = rr.next;
	  }
	  */
	  Regex rex = rp.getRegex();
	  int npos = rex.matchedTo();
	  rp.setBuffer(sbl);
	  rp.setSource(wrap);
	  rp.setPos(npos);
	  rp.apply(rex,rex.getReplaceRule());
	  int opos = rb.epos;
	  RBuffer rb2 = new RBuffer((StringBuffer)sbw.unwrap());
	  rb2.epos = rb2.sb.length();
	  RBuffer rb3 = new RBuffer(rb.sb);

	  rb.next = rb2;
	  rb2.next = rb3;

	  if(npos == opos) {
	    rb3.epos = npos+1;
	    if(rb3.epos > rb3.sb.length()) {
	      if(rb.pos >= rb.epos)
	        rb = rb.next;
	      rb3.pos = rb3.epos = 0;
	      rb3.done = true;
	      //break;
	    }
            rb3.pos = npos;
	  } else {
	    rb3.pos = rb3.epos = npos;
	  }

        }
      } else {
	if(wrap.overRun) {
	  readData();
        } else if(rb.epos<rb.sb.length()) {
	  rb.epos++;
        } else {
	  break;
	}
      }
    }
  }
  public int read() throws IOException {
    if(rb.pos >= rb.epos) {
      getMoreData();
      if(rb.pos >= rb.epos)
        return -1;
    }
    //System.out.println(rb);
    return rb.sb.charAt(rb.pos++);
  }
  public int read(char[] buf,int off,int len)
    throws IOException
  {
    int c = -1;
    int end = off+len;
    for(int i=off;i<end;i++) {
      c = read();
      if(c < 0) {
	if(i == off)
	  return -1;
        return i-off;
      }
      buf[i] = (char)c;
    }
    return len;
  }
  public void close()
    throws IOException
  {
    r.close();
  }

  public boolean markSupported() { return false; }

  /** Get the size of the working buffer.
      The current buffer may be larger if
      the pattern demands it. */
  public int getBufferSize() {
    return nmax;
  }
  /** Set the size of the working buffer.
      The current buffer may be larger if
      the pattern demands it. */
  public void setBufferSize(int n) {
    nmax = n;
  }

  int max_lines = 2;
  /** This function no longer serves any purpose.
      @deprecated
      */
  public int getMaxLines() { return max_lines; }
  /** This function no longer serves any purpose.
      @deprecated
      */
  public void setMaxLines(int ml) { max_lines = ml; }

  char EOLchar = '\n';
  /** This function no longer serves any purpose.
      @deprecated
      */
  public char getEOLchar() {
    return EOLchar;
  }
  /** This function no longer serves any purpose.
      @deprecated
      */
  public void setEOLchar(char c) {
    EOLchar = c;
  }

  public long skip(long d) throws IOException {
    // This is probably inefficient, I just did it
    // this way to avoid possible bugs.
    long n = 0;
    while(n<d && read() != -1)
      n++;
    return n;
  }

  /*
  static void test(String re,String inp,int n) throws Exception {
    Reader r = new StringReader(inp);
    r = new BufferedReader(r);
    Regex rex = Regex.perlCode(re);
    String res1 = rex.replaceAll(inp);
    int c = -1;
    StringBuffer sb = new StringBuffer();
    RegexReader rr = new RegexReader(rex,r);
    rr.setBufferSize(n);
    while( (c = rr.read()) != -1)
      sb.append((char)c);
    String res2 = sb.toString();
    if(!res1.equals(res2)) {
      System.out.println("nmax="+n);
      System.out.println("re="+re);
      System.out.println("inp="+inp);
      System.out.println("res1="+res1);
      System.out.println("res2="+res2);
      System.exit(255);
    }
  }
  public static void main(String[] args) throws Exception {
    for(int n=6;n<15;n++) {
      test("s/x/y/","-----x123456789",n);
      test("s/x/y/","x123456789",n);
      test("s/x/y/","-----x",n);
      test("s/x.*?x/y/",".xx..x..x...x...x....x....x",n);
      test("s/x.*x/[$&]/","--x........x--xx",n);
      test("s/x.*x/[$&]/","--x........x------",n);
      test("s/.$/a/m","bb\nbbb\nbbbb\nbbbbb\nbbbbbb\nbbbbbbbbbbbb",n);
      test("s/.$/a/","123",n);
      test("s/.$/a/","bb\nbbb\nbbbb\nbbbbb\nbbbbbb\nbb",n);
      test("s/^./a/","bb\nbbb\nbbbb\nbbbbb\nbbbbbb\nbb",n);
      test("s/$/a/","bbb",n);
      test("s/^/a/","bbb",n);
      test("s/^/a/","",n);
      test("s{.*}{N}","xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",n);
      test("s/.{0,7}/y/","AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",n);
      test("s/x/$&/","xxx",n);
    }
    System.out.println("Success!!!");
  }
  */
}

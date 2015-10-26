//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

/** This class provides a method for parsing the "s/.../.../" constructs
of Regex.perlCode.
@see Regex#perlCode
*/
class parsePerl {
    final static char close(char c) {
        // This switch statement does not behave
        // properly when compiled with jdk1.1.5
        // and the -O flag.
        /*
        switch(c) {
        case '[':
          return ']';
        case '(':
          return ')';
        case '{':
          return '}';
        }
        return c;*/
	if(c == '<')
	    return '>';
        if(c == '[')
            return ']';
        if(c == '(')
            return ')';
        if(c == '{')
            return '}';
        return c;
    }

    final public static String codify(String s,boolean keepbs) {
        return codify(s,0,s.length(),keepbs);
    }
    final public static String codify(String s,int i0,int iN,boolean keepbs) {
        StringBuffer sb = new StringBuffer();
        boolean ucmode = false, lcmode = false, litmode = false;
        boolean uc1 = false, lc1 = false;
        boolean modified = false;
        for(int i=i0;i<iN;i++) {
            char c = s.charAt(i);
            boolean mf = true, app=true;
            if(c=='\\') {
                app=false;
                i++;
                if(i<s.length()) {
                    char c2 = s.charAt(i);
                    switch(c2) {
                    case 'Q':
                        litmode = true;
                        break;
                    case 'U':
                        ucmode = true;
                        break;
                    case 'L':
                        lcmode = true;
                        break;
                    case 'u':
                        uc1 = true;
                        break;
                    case 'l':
                        lc1 = true;
                        break;
                    case 'E':
                        uc1=lc1=ucmode=lcmode=litmode=false;
                        break;
                    default:
                        if(keepbs)
                            sb.append('\\');
                        c=c2;
                        if(keepbs) mf = false;
                        app = true;
                        break;
                    }
                    modified |= mf;
                }
            }
            if(app) {
                if(lc1) {
                    c=lc(c);
                    lc1=false;
                } else if(uc1) {
                    c=uc(c);
                    uc1=false;
                } else if(ucmode) {
                    c=uc(c);
                } else if(lcmode) {
                    c=lc(c);
                }
                if(litmode && needbs(c))
                    sb.append('\\');
                sb.append(c);
            }
        }
        return modified ? sb.toString() : s;
    }
    final static char uc(char c) {
        return CaseMgr.toUpperCase(c);
    }
    final static char lc(char c) {
        return CaseMgr.toLowerCase(c);
    }
    final static boolean needbs(char c) {
        if(c >= 'a' && c <= 'z')
            return false;
        if(c >= 'A' && c <= 'Z')
            return false;
        if(c >= '0' && c <= '9')
            return false;
        if(c == '_')
            return false;
        return true;
    }
    final static Regex parse(String s) {
        boolean igncase = false, optim = false, gFlag = false;
        boolean sFlag = false, mFlag = false, xFlag = false;

        StringBuffer s1 = new StringBuffer();
        StringBuffer s2 = new StringBuffer();
        int i=0,count=0;
        char mode,delim='/',cdelim='/';
        if(s.length() >= 3 && s.charAt(0)=='s') {
            mode = 's';
            delim = s.charAt(1);
            cdelim = close(delim);
            i=2;
        } else if(s.length() >= 2 && s.charAt(0)=='m') {
            mode = 'm';
            delim = s.charAt(1);
            cdelim = close(delim);
            i=2;
        } else if(s.length() >= 1 && s.charAt(0)=='/') {
            mode = 'm';
            i=1;
        } else {
            try {
                RegSyntaxError.endItAll(
		    "Regex.perlCode should be of the "+
                    "form s/// or m// or //");
            } catch(RegSyntax rs) {}
            return null;
        }
        for(;i<s.length();i++) {
            if(s.charAt(i)=='\\') {
                s1.append('\\');
                i++;
            } else if(s.charAt(i)==cdelim && count==0) {
                i++;
                break;
            } else if(s.charAt(i)==delim && cdelim != delim) {
                count++;
            } else if(s.charAt(i)==cdelim && cdelim != delim) {
                count--;
            }
            s1.append(s.charAt(i));
        }
        if(mode=='s' && cdelim != delim) {
            while(i<s.length() && Prop.isWhite(s.charAt(i)) )
                i++;
            if(i>=s.length()) {
                try {
                    RegSyntaxError.endItAll(""+mode+delim+" needs "+cdelim);
                } catch(RegSyntax rs) {}
                return null;
            }
            cdelim = close(delim = s.charAt(i));
            i++;
        }
        count=0;
        if(mode=='s') {
            for(;i<s.length();i++) {
                if(s.charAt(i)=='\\') {
                    s2.append('\\');
                    i++;
                } else if(s.charAt(i)==cdelim && count==0) {
                    i++;
                    break;
                } else if(s.charAt(i)==delim && cdelim != delim) {
                    count++;
                } else if(s.charAt(i)==cdelim && cdelim != delim) {
                    count--;
                }
                s2.append(s.charAt(i));
            }
        }
        for(;i<s.length();i++) {
            char c = s.charAt(i);
            switch(c) {
	    case 'x':
	        xFlag = true;
		break;
            case 'i':
                igncase = true;
                break;
            case 'o':
                optim = true;
                break;
            case 's':
                sFlag = true;
                break;
            case 'm':
		mFlag = true;
                break;
            case 'g':
                gFlag = true;
                break;
            default:
                // syntax error!
                try {
                    RegSyntaxError.endItAll("Illegal flag to pattern: "+c);
                } catch(RegSyntax rs) {}
                return null;
            }
        }
        Regex r = new Regex();
        try {
            String pat=s1.toString(),reprul=s2.toString();
	    if(xFlag) {
	      pat = strip(pat);
	      reprul = strip(reprul);
	    }
            r.compile(pat);
            r.ignoreCase |= igncase;
            r.gFlag |= gFlag;
            r.sFlag |= sFlag;
	    r.mFlag |= mFlag;
            if(optim) r.optimize();
	    if(delim=='\'')
              r.setReplaceRule(new StringRule(reprul));
	    else
              r.setReplaceRule(ReplaceRule.perlCode(reprul));
        } catch(RegSyntax rs) {
            r = null;
        }
        return r;
    }
    static String strip(String s) {
      StringBuffer sb = new StringBuffer();
      for(int i=0;i<s.length();i++) {
        char c = s.charAt(i);
	if(Prop.isWhite(c)) {
	  ;
	} else if(c == '#') {
	  i++;
	  while(i<s.length()) {
	    if(s.charAt(i) == '\n')
	      break;
	    i++;
	  }
        } else if(c == '\\') {
	  sb.append(c);
	  sb.append(s.charAt(++i));
	} else
	  sb.append(c);
      }
      return sb.toString();
    }
}

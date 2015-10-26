//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
import java.util.*;
import java.io.*;
import com.stevesoft.pat.wrap.StringWrap;


/** Matches a Unicode punctuation character. */
class UnicodePunct extends UniValidator {
    public int validate(StringLike s,int from,int to) {
        return from<s.length() && Prop.isPunct(s.charAt(from)) ? to : -1;
    }
}

/** Matches a Unicode white space character. */
class UnicodeWhite extends UniValidator {
    public int validate(StringLike s,int from,int to) {
        return from<s.length() && Prop.isWhite(s.charAt(from)) ? to : -1;
    }
}

/** Matches a character that is not a Unicode punctuation
  * character.
  */
class NUnicodePunct extends UniValidator {
    public int validate(StringLike s,int from,int to) {
        return from<s.length() && !Prop.isPunct(s.charAt(from)) ? to : -1;
    }
}

/** Matches a character that is not a
  * Unicode white space character.
  */
class NUnicodeWhite extends UniValidator {
    public int validate(StringLike s,int from,int to) {
        return from<s.length() && !Prop.isWhite(s.charAt(from)) ? to : -1;
    }
}

/** Matches a Unicode word character: an alphanumeric or underscore. */
class UnicodeW extends UniValidator {
    public int validate(StringLike s,int from,int to) {
        if(from >= s.length()) return -1;
        char c = s.charAt(from);
        return (Prop.isAlphabetic(c)||Prop.isDecimalDigit(c)||c=='_') ? to : -1;
    }
}

/** Matches a character that is not a Unicode alphanumeric or underscore. */
class NUnicodeW extends UniValidator {
    public int validate(StringLike s,int from,int to) {
        if(from >= s.length()) return -1;
        char c = s.charAt(from);
        return !(Prop.isAlphabetic(c)||Prop.isDecimalDigit(c)||c=='_') ? to : -1;
    }
}

/** Matches a Unicode decimal digit. */
class UnicodeDigit extends UniValidator {
    public int validate(StringLike s,int from,int to) {
        return from<s.length() && Prop.isDecimalDigit(s.charAt(from)) ? to : -1;
    }
}
/** Matches a character that is not a Unicode digit.*/
class NUnicodeDigit extends UniValidator {
    public int validate(StringLike s,int from,int to) {
        return from<s.length() && !Prop.isDecimalDigit(s.charAt(from)) ? to : -1;
    }
}

/** Matches a Unicode math character. */
class UnicodeMath extends UniValidator {
    public int validate(StringLike s,int from,int to) {
        return from<s.length() && Prop.isMath(s.charAt(from)) ? to : -1;
    }
}
/** Matches a non-math Unicode character. */
class NUnicodeMath extends UniValidator {
    public int validate(StringLike s,int from,int to) {
        return from<s.length() && !Prop.isMath(s.charAt(from)) ? to : -1;
    }
}

/** Matches a Unicode currency symbol. */
class UnicodeCurrency extends UniValidator {
    public int validate(StringLike s,int from,int to) {
        return from<s.length() && Prop.isCurrency(s.charAt(from)) ? to : -1;
    }
}
/** Matches a non-currency symbol Unicode character. */
class NUnicodeCurrency extends UniValidator {
    public int validate(StringLike s,int from,int to) {
        return from<s.length() && !Prop.isCurrency(s.charAt(from)) ? to : -1;
    }
}

/** Matches a Unicode alphabetic character. */
class UnicodeAlpha extends UniValidator {
    public int validate(StringLike s,int from,int to) {
        return from<s.length() && Prop.isAlphabetic(s.charAt(from)) ? to : -1;
    }
}

/** Matches a non-alphabetic Unicode character. */
class NUnicodeAlpha extends UniValidator {
    public int validate(StringLike s,int from,int to) {
        return from<s.length() && !Prop.isAlphabetic(s.charAt(from)) ? to : -1;
    }
}

/** Matches an upper case Unicode character. */
class UnicodeUpper extends UniValidator {
  public int validate(StringLike s,int from,int to) {
    return from<s.length() && isUpper(s.charAt(from)) ? to : -1;
  }
  final boolean isUpper(char c) {
    return c == CaseMgr.toUpperCase(c) && c != CaseMgr.toLowerCase(c);
  }
}

/** Matches an upper case Unicode character. */
class UnicodeLower extends UniValidator {
  public int validate(StringLike s,int from,int to) {
    return from<s.length() && isLower(s.charAt(from)) ? to : -1;
  }
  final boolean isLower(char c) {
    return c != CaseMgr.toUpperCase(c) && c == CaseMgr.toLowerCase(c);
  }
}

/**
Regex provides the parser which constructs the linked list of
Pattern classes from a String.
<p>
For the purpose of this documentation, the fact that java interprets the
backslash will be ignored.  In practice, however, you will need a
double backslash to obtain a string that contains a single backslash
character.  Thus, the example pattern "\b" should really be typed
as "\\b" inside java code.
<p>
Note that Regex is part of package "com.stevesoft.pat".
To use it, simply import
com.stevesoft.pat.Regex at the top of your file.
<p>
Regex is made with a constructor that takes a String that defines
the regular expression.  Thus, for example
<pre>
      Regex r = new Regex("[a-c]*");
</pre>
matches any number of characters so long as the are 'a', 'b', or 'c').
<p>
To attempt to match the Pattern to a given string, you can use either
the search(String) member function, or the matchAt(String,int position)
member function.  These functions return a boolean which tells you
whether or not the thing worked, and sets the methods "charsMatched()"
and "matchedFrom()" in the Regex object appropriately.
<p>
The portion of the string before the match can be obtained by the
left() member, and the portion after the match can be obtained
by the right() member.
<p>
Essentially, this package implements a syntax that is very much
like the perl 5 regular expression syntax.

Longer example:
<pre>
        Regex r = new Regex("x(a|b)y");
        r.matchAt("xay",0);
        System.out.println("sub = "+r.stringMatched(1));
</pre>
The above would print "sub = a".
<pre>
        r.left() // would return "x"
        r.right() // would return "y"
</pre>
<p>
Differences between this package and perl5:<br>
The extended Pattern for setting flags, is now supported,
but the flags are different.  "(?i)" tells the pattern to
ignore case, "(?Q)" sets the "dontMatchInQuotes" flag, and
"(?iQ)" sets them both.  You can change the escape character.
The pattern <pre>(?e=#)#d+</pre> is the same as <pre>\d+</pre>,
but note that the sequence <pre>(?e=#)</pre> <b>must</b> occur
at the very beginning of the pattern.  There may be other small
differences as well.  I will either make my package conform
or note them as I become aware of them.
<p>
This package supports additional patterns not in perl5:
<center>
<table border=1>
<tr><td>(?@())</td><td>Group</td><td>This matches all characters between
the '(' character and the balancing ')' character.  Thus, it will
match "()" as well as "(())".  The balancing characters are
arbitrary, thus (?@{}) matches on "{}" and "{{}}".</td>
<tr><td>(?&lt1)</td><td>Backup</td><td>Moves the pointer backwards within the text.
This allows you to make a "look behind."  It fails if it
attempts to move to a position before the beginning of the string.
"x(?&lt1)" is equivalent to "(?=x)".  The number, 1 in this example,
is the number of characters to move backwards.</td>
</table>
</center>
</dl>
@author Steven R. Brandt
@version package com.stevesoft.pat, release 1.5.3
@see Pattern
*/
public class Regex extends RegRes implements FilenameFilter {
    /** BackRefOffset gives the identity number of the first
        pattern.  Version 1.0 used zero, version 1.1 uses 1 to be
        more compatible with perl. */
    static int BackRefOffset = 1;
    private static Pattern none = new NoPattern();
    Pattern thePattern = none;
    patInt minMatch = new patInt(0);

    static Hashtable validators = new Hashtable();
    static {
        define("p","(?>1)",new UnicodePunct());
        define("P","(?>1)",new NUnicodePunct());
        define("s","(?>1)",new UnicodeWhite());
        define("S","(?>1)",new NUnicodeWhite());
        define("w","(?>1)",new UnicodeW());
        define("W","(?>1)",new NUnicodeW());
        define("d","(?>1)",new UnicodeDigit());
        define("D","(?>1)",new NUnicodeDigit());
        define("m","(?>1)",new UnicodeMath());
        define("M","(?>1)",new NUnicodeMath());
        define("c","(?>1)",new UnicodeCurrency());
        define("C","(?>1)",new NUnicodeCurrency());
        define("a","(?>1)",new UnicodeAlpha());
        define("A","(?>1)",new NUnicodeAlpha());
        define("uc","(?>1)",new UnicodeUpper());
        define("lc","(?>1)",new UnicodeLower());
    }

    /** Set the dontMatch in quotes flag. */
    public void setDontMatchInQuotes(boolean b) {
      dontMatchInQuotes = b;
    }
    /** Find out if the dontMatchInQuotes flag is enabled. */
    public boolean getDontMatchInQuotes() {
      return dontMatchInQuotes;
    }
    boolean dontMatchInQuotes = false;

    /** Set the state of the ignoreCase flag.  If set to true, then
        the pattern matcher will ignore case when searching for a
        match. */
    public void setIgnoreCase(boolean b) {
        ignoreCase = b;
    }
    /** Get the state of the ignoreCase flag.  Returns true if we
        are ignoring the case of the pattern, false otherwise. */
    public boolean getIgnoreCase() {
        return ignoreCase;
    }
    boolean ignoreCase = false;
    
    static boolean defaultMFlag = false;
    /** Set the default value of the m flag.  If it
        is set to true, then the MFlag will be on
	for any regex search executed. */
    public static void setDefaultMFlag(boolean mFlag) {
      defaultMFlag = mFlag;
    }
    /** Get the default value of the m flag.  If it
        is set to true, then the MFlag will be on
	for any regex search executed. */
    public static boolean getDefaultMFlag() {
      return defaultMFlag;
    }

    /** Initializes the object without a Pattern. To supply a Pattern
        use compile(String s).
         @see com.stevesoft.pat.Regex#compile(java.lang.String)
                 */
    public Regex() {}
    /** Create and compile a Regex, but do not throw any exceptions.
        If you wish to have exceptions thrown for syntax errors,
        you must use the Regex(void) constructor to create the
        Regex object, and then call the compile method.  Therefore, you
        should only call this method when you know your pattern is right.
        I will probably become more like
         @see com.stevesoft.pat.Regex#search(java.lang.String)
         @see com.stevesoft.pat.Regex#compile(java.lang.String)
         */
    public Regex(String s) {
        try {
            compile(s);
        } catch(RegSyntax rs) {}
    }

    ReplaceRule rep = null;
    /** Create and compile both a Regex and a ReplaceRule.
        @see com.stevesoft.pat.ReplaceRule
	@see com.stevesoft.pat.Regex#compile(java.lang.String)
        */
    public Regex(String s,String rp) {
        this(s);
        rep = ReplaceRule.perlCode(rp);
    }
    /** Create and compile a Regex, but give it the ReplaceRule
        specified.  This allows the user finer control of the
        Replacement process, if that is desired.
        @see com.stevesoft.pat.ReplaceRule
	@see com.stevesoft.pat.Regex#compile(java.lang.String)
        */
    public Regex(String s,ReplaceRule rp) {
        this(s);
        rep = rp;
    }

    /** Change the ReplaceRule of this Regex by compiling
        a new one using String rp. */
    public void setReplaceRule(String rp) {
        rep = ReplaceRule.perlCode(rp);
        repr = null; // Clear Replacer history
    }

    /** Change the ReplaceRule of this Regex to rp. */
    public void setReplaceRule(ReplaceRule rp) {
        rep = rp;
    }
    /** Test to see if a custom defined rule exists.
        @see com.stevesoft.pat#define(java.lang.String,java.lang.String,Validator)
        */
    public static boolean isDefined(String nm) {
        return validators.get(nm) != null;
    }
    /** Removes a custom defined rule.
        @see com.stevesoft.pat#define(java.lang.String,java.lang.String,Validator)
        */
    public static void undefine(String nm) {
        validators.remove(nm);
    }
    /** Defines a method to create a new rule.  See test/deriv2.java
        and test/deriv3.java for examples of how to use it. */
    public static void define(String nm,String pat,Validator v) {
        v.pattern = pat;
        validators.put(nm,v);
    }
    /** Defines a shorthand for a pattern.  The pattern will be
        invoked by a string that has the form "(??"+nm+")".
        */
    public static void define(String nm,String pat) {
        validators.put(nm,pat);
    }

    /** Get the current ReplaceRule. */
    public ReplaceRule getReplaceRule() { return rep; }

    Replacer repr = null;
    final Replacer _getReplacer() {
        return repr==null ? repr=new Replacer() : repr;
    }
    public Replacer getReplacer() {
        if(repr == null)
            repr = new Replacer();
        repr.rh.me = this;
        repr.rh.prev = null;
        return repr;
    }
    /** Replace the first occurence of this pattern in String s
        according to the ReplaceRule.
        @see com.stevesoft.pat.ReplaceRule
        @see com.stevesoft.pat.Regex#getReplaceRule()
        */
    public String replaceFirst(String s) {
        return _getReplacer().replaceFirstRegion(s,this,0,s.length()).toString();
    }
    /** Replace the first occurence of this pattern in String s
        beginning with position pos according to the ReplaceRule.
        @see com.stevesoft.pat.ReplaceRule
        @see com.stevesoft.pat.Regex#getReplaceRule()
        */
    public String replaceFirstFrom(String s,int pos) {
        return _getReplacer().replaceFirstRegion(s,this,pos,s.length()).toString();
    }
    /** Replace the first occurence of this pattern in String s
        beginning with position start and ending with end
        according to the ReplaceRule.
        @see com.stevesoft.pat.ReplaceRule
        @see com.stevesoft.pat.Regex#getReplaceRule()
        */
    public String replaceFirstRegion(String s,int start,int end) {
        return _getReplacer().replaceFirstRegion(s,this,start,end).toString();
    }

    /** Replace all occurences of this pattern in String s
        according to the ReplaceRule.
        @see com.stevesoft.pat.ReplaceRule
        @see com.stevesoft.pat.Regex#getReplaceRule()
        */
    public String replaceAll(String s) {
        return _getReplacer().replaceAllRegion(s,this,0,s.length()).toString();
    }
    public StringLike replaceAll(StringLike s) {
        return _getReplacer().replaceAllRegion(s,this,0,s.length());
    }
    /** Replace all occurences of this pattern in String s
        beginning with position pos according to the ReplaceRule.
        @see com.stevesoft.pat.ReplaceRule
        @see com.stevesoft.pat.Regex#getReplaceRule()
        */
    public String replaceAllFrom(String s,int pos) {
        return _getReplacer().replaceAllRegion(s,this,pos,s.length()).toString();
    }
    /** Replace all occurences of this pattern in String s
        beginning with position start and ending with end
        according to the ReplaceRule.
        @see com.stevesoft.pat.ReplaceRule
        @see com.stevesoft.pat.Regex#getReplaceRule()
        */
    public String replaceAllRegion(String s,int start,int end) {
        return _getReplacer().replaceAllRegion(s,this,start,end).toString();
    }


    /** Essentially clones the Regex object */
    public Regex(Regex r) {
        super((RegRes)r);
        dontMatchInQuotes = r.dontMatchInQuotes;
        esc = r.esc;
        ignoreCase = r.ignoreCase;
        gFlag = r.gFlag;
        if(r.rep==null)
            rep = null;
        else
            rep = (ReplaceRule)r.rep.clone();
        /* try {
            compile(r.toString());
        } catch(RegSyntax r_) {} */
        thePattern = r.thePattern.clone(new Hashtable());
        minMatch = r.minMatch;
        skipper = r.skipper;
    }

    /** By default,
                the escape character is the backslash, but you can
                make it anything you want by setting this variable. */
    public char esc = Pattern.ESC;
    /** This method compiles a regular expression, making it
         possible to call the search or matchAt methods.
                @exception com.stevesoft.pat.RegSyntax
                is thrown if a syntax error is encountered
		in the pattern.
                 For example, "x{3,1}" or "*a" are not valid
		 patterns.
                @see com.stevesoft.pat.Regex#search
                @see com.stevesoft.pat.Regex#matchAt
                */
    public void compile(String prepat) throws RegSyntax {
        String postpat = parsePerl.codify(prepat,true);
        String pat = postpat==null ? prepat : postpat;
        minMatch = null;
        ignoreCase = false;
        dontMatchInQuotes = false;
        Rthings mk = new Rthings(this);
        int offset = mk.val;
        String newpat = pat;
        thePattern = none;
        p = null;
        or = null;
        minMatch = new patInt(0);
        StrPos sp = new StrPos(pat,0);
        if(sp.incMatch("(?e=")) {
            char newEsc = sp.c;
            sp.inc();
            if(sp.match(')'))
                newpat = reEscape(pat.substring(6),
                    newEsc,Pattern.ESC);
        } else if(esc != Pattern.ESC)
            newpat = reEscape(pat,esc,Pattern.ESC);
        thePattern = _compile(newpat,mk);
        numSubs_ = mk.val-offset;
        mk.set(this);
    }

    /*  If a Regex is compared against a Regex, a check is
        done to see that the patterns are equal as well as
        the most recent match.  If a Regex is compare with
        a RegRes, only the result of the most recent match
        is compared. */
    public boolean equals(Object o) {
        if(o instanceof Regex) {
            if(toString().equals(o.toString()))
                return super.equals(o);
            else
                return false;
        } else return super.equals(o);
    }

    /** A clone by any other name would smell as sweet. */
    public Object clone() {
        return new Regex(this);
    }
    /** Return a clone of the underlying RegRes object. */
    public RegRes result() {
      return (RegRes)super.clone();
    }

    // prep sets global variables of class
    // Pattern so that it can access them
    // during an attempt at a match
    Pthings pt = new Pthings();
    final Pthings prep(StringLike s) {
	//if(gFlag)
          pt.lastPos = matchedTo();
        if(pt.lastPos < 0) pt.lastPos = 0;
        if( (s==null ? null : s.unwrap()) != (src==null ? null : s.unwrap()) )
          pt.lastPos = 0;
        src = s;
        pt.dotDoesntMatchCR=dotDoesntMatchCR && (!sFlag);
	pt.mFlag = (mFlag | defaultMFlag);
        pt.ignoreCase = ignoreCase;
        pt.no_check = false;
        if(pt.marks != null)
            for(int i=0;i<pt.marks.length;i++)
                pt.marks[i]=-1;
	pt.marks = null;
        pt.nMarks = numSubs_;
        pt.src = s;
        if(dontMatchInQuotes)
            setCbits(s,pt);
        else
            pt.cbits = null;
        return pt;
    }
    /** Attempt to match a Pattern beginning
        at a specified location within the string.
        @see com.stevesoft.pat.Regex#search
        */
    public boolean matchAt(String s,int start_pos) {
        return _search(s,start_pos,start_pos);
    }
    /** Attempt to match a Pattern beginning
        at a specified location within the StringLike.
        @see com.stevesoft.pat.Regex#search
        */
    public boolean matchAt(StringLike s,int start_pos) {
        return _search(s,start_pos,start_pos);
    }


    /** Search through a String for the first
        occurrence of a match.
        @see com.stevesoft.pat.Regex#searchFrom
        @see com.stevesoft.pat.Regex#matchAt
        */
    public boolean search(String s) {
        if(s==null)
            throw new NullPointerException("Null String Given to Regex.search");
        return _search(s,0,s.length());
    }
    public boolean search(StringLike sl) {
        if(sl==null)
            throw new NullPointerException("Null StringLike Given to Regex.search");
        return _search(sl,0,sl.length());
    }
    public boolean reverseSearch(String s) {
        if(s==null)
            throw new NullPointerException("Null String Given to Regex.reverseSearch");
        return _reverseSearch(s,0,s.length());
    }
    public boolean reverseSearch(StringLike sl) {
        if(sl==null)
            throw new NullPointerException("Null StringLike Given to Regex.reverseSearch");
        return _reverseSearch(sl,0,sl.length());
    }
    /** Search through a String for the first
                occurence of a match, but start at position <pre>start</pre>*/
    public boolean searchFrom(String s,int start) {
        if(s==null)
            throw new NullPointerException("Null String Given to Regex.searchFrom");
        return _search(s,start,s.length());
    }
    public boolean searchFrom(StringLike s,int start) {
        if(s==null)
            throw new NullPointerException("Null String Given to Regex.searchFrom");
        return _search(s,start,s.length());
    }
    /** Search through a region of a String
        for the first occurence of a match. */
    public boolean searchRegion(String s,int start,int end) {
        if(s==null)
            throw new NullPointerException("Null String Given to Regex.searchRegion");
        return _search(s,start,end);
    }
    /** Set this to change the default behavior of the "." pattern.
                By default it now matches perl's behavior and fails to
                match the '\n' character. */
    public static boolean dotDoesntMatchCR = true;
    StringLike gFlags;
    int gFlagto = 0;
    boolean gFlag = false;
    /** Set the 'g' flag */
    public void setGFlag(boolean b) {
      gFlag = b;
    }
    /** Get the state of the 'g' flag. */
    public boolean getGFlag() {
      return gFlag;
    }
    boolean sFlag = false;
    /** Get the state of the sFlag */
    public boolean getSFlag() {
      return sFlag;
    }
    boolean mFlag = false;
    /** Get the state of the sFlag */
    public boolean getMFlag() {
      return mFlag;
    }

    final boolean _search(String s,int start,int end) {
        return _search(new StringWrap(s),start,end);
    }
    final boolean _search(StringLike s,int start,int end) {
        if(gFlag && gFlagto > 0 && gFlags!=null && s.unwrap()==gFlags.unwrap())
            start = gFlagto;
        gFlags = null;

        Pthings pt=prep(s);

        int up = (minMatch == null ? end : end-minMatch.i);

        if(up < start && end >= start) up = start;

        if(skipper == null) {
            for(int i=start;i<=up;i++) {
                charsMatched_ = thePattern.matchAt(s,i,pt);
                if(charsMatched_ >= 0) {
                    matchFrom_ = thePattern.mfrom;
                    marks = pt.marks;
                    gFlagto = matchFrom_+charsMatched_;
                    gFlags = s;
                    return didMatch_=true;
                }
            }
        } else {
            pt.no_check = true;
            for(int i=start;i<=up;i++) {
                i = skipper.find(src,i,up);
                if(i<0) {
                    charsMatched_ = matchFrom_ = -1;
                    return didMatch_ = false;
                }
                charsMatched_ = thePattern.matchAt(s,i,pt);
                if(charsMatched_ >= 0) {
                    matchFrom_ = thePattern.mfrom;
                    marks = pt.marks;
                    gFlagto = matchFrom_+charsMatched_;
                    gFlags = s;
                    return didMatch_=true;
                }
            }
        }
        return didMatch_=false;
    }
    /*final boolean _search(LongStringLike s,long start,long end) {
        if(gFlag && gFlagto > 0 && s==gFlags)
            start = gFlagto;
        gFlags = null;

        Pthings pt=prep(s);

        int up = end;//(minMatch == null ? end : end-minMatch.i);

        if(up < start && end >= start) up = start;

        if(skipper == null) {
            for(long i=start;i<=up;i++) {
                charsMatched_ = thePattern.matchAt(s,i,pt);
                if(charsMatched_ >= 0) {
                    matchFrom_ = thePattern.mfrom;
                    marks = pt.marks;
                    gFlagto = matchFrom_+charsMatched_;
                    return didMatch_=true;
                }
            }
        } else {
            pt.no_check = true;
            for(long i=start;i<=up;i++) {
                i = skipper.find(src,i,up);
                if(i<0) {
                    charsMatched_ = matchFrom_ = -1;
                    return didMatch_ = false;
                }
                charsMatched_ = thePattern.matchAt(s,i,pt);
                if(charsMatched_ >= 0) {
                    matchFrom_ = thePattern.mfrom;
                    marks = pt.marks;
                    gFlagto = matchFrom_+charsMatched_;
                    gFlags = s;
                    return didMatch_=true;
                } else {
                  i = s.adjustIndex(i);
                  up = s.adjustEnd(i);
                }
            }
        }
        return didMatch_=false;
    }*/

    boolean _reverseSearch(String s,int start,int end) {
        return _reverseSearch(new StringWrap(s),start,end);
    }
    boolean _reverseSearch(StringLike s,int start,int end) {
        if(gFlag && gFlagto > 0 && s.unwrap()==gFlags.unwrap())
            end = gFlagto;
        gFlags = null;
        Pthings pt=prep(s);
        for(int i=end;i>=start;i--) {
            charsMatched_ = thePattern.matchAt(s,i,pt);
            if(charsMatched_ >= 0) {
                matchFrom_ = thePattern.mfrom;
                marks = pt.marks;
                gFlagto = matchFrom_-1;
                gFlags = s;
                return didMatch_=true;
            }
        }
        return didMatch_=false;
    }

    // This routine sets the cbits variable
    // of class Pattern.  Cbits is true for
    // the bit corresponding to a character inside
    // a set of quotes.
    static StringLike lasts=null;
    static BitSet lastbs=null;
    static void setCbits(StringLike s,Pthings pt) {
        if(s == lasts) {
            pt.cbits = lastbs;
            return;
        }
        BitSet bs = new BitSet(s.length());
        char qc = ' ';
        boolean setBit = false;
        for(int i=0;i<s.length();i++) {
            if(setBit) bs.set(i);
            char c = s.charAt(i);
            if(!setBit && c == '"') {
                qc = c;
                setBit = true;
                bs.set(i);
            } else if(!setBit && c == '\'') {
                qc = c;
                setBit = true;
                bs.set(i);
            } else if(setBit && c == qc) {
                setBit = false;
            } else if(setBit && c == '\\' && i+1<s.length()) {
                i++;
                if(setBit) bs.set(i);
            }
        }
        pt.cbits = lastbs = bs;
        lasts = s;
    }

    // Wanted user to over-ride this in alpha version,
    // but it wasn't really necessary because of this trick:
    Regex newRegex() {
        try {
            return (Regex)getClass().newInstance();
        } catch(InstantiationException ie) {
            return null;
        } catch(IllegalAccessException iae) {
            return null;
        }
    }
    /** Only needed for creating your own extensions of
         Regex.  This method adds the next Pattern in the chain
         of patterns or sets the Pattern if it is the first call. */
    protected void add(Pattern p2) {
        if(p == null)
            p = p2;
        else {
            p.add(p2);
            p2 = p;
        }
    }

    /** You only need to use this method if you are creating
        your own extentions to Regex.
        compile1 compiles one Pattern element, it can be
        over-ridden to allow the Regex compiler to understand
        new syntax.  See deriv.java for an example.  This routine
        is the heart of class Regex. Rthings has one integer
        member called intValue, it is used to keep track of the number
        of ()'s in the Pattern.
        @exception com.stevesoft.pat.RegSyntax is thrown when a nonsensensical
        pattern is supplied.  For example, a pattern beginning
        with *. */
    protected void compile1(StrPos sp,Rthings mk) throws RegSyntax {
        if(sp.match('[')) {
            sp.inc();
            add(matchBracket(sp));
        } else if(sp.match('|')) {
            if(or == null)
                or = new Or();
            if(p == null) p=new NullPattern();
            or.addOr(p);
            p = null;
        } else if(sp.incMatch("(?<")) {
            patInt i = sp.getPatInt();
            if(i==null) RegSyntaxError.endItAll("No int after (?<");
            add(new Backup(i.intValue()));
            if(!sp.match(')')) RegSyntaxError.endItAll("No ) after (?<");
        } else if(sp.incMatch("(?>")) {
            patInt i = sp.getPatInt();
            if(i==null) RegSyntaxError.endItAll("No int after (?>");
            add(new Backup(-i.intValue()));
            if(!sp.match(')')) RegSyntaxError.endItAll("No ) after (?<");
        } else if(sp.incMatch("(?@")) {
            char op = sp.c;
            sp.inc();
            char cl = sp.c;
            sp.inc();
            if(!sp.match(')'))
                RegSyntaxError.endItAll(
                    "(?@ does not have closing paren");
            add(new Group(op,cl));
        } else if(sp.incMatch("(?#")) {
            while(!sp.match(')'))
                sp.inc();
        } else if(sp.dontMatch && sp.c == 'w') {
            Regex r = new Regex();
            //r._compile("[a-zA-Z0-9_]",mk);
            //add(new Goop("\\w",r.thePattern));
            Bracket b = new Bracket(false);
            b.addOr(new Range('a','z'));
            b.addOr(new Range('A','Z'));
            b.addOr(new Range('0','9'));
            b.addOr(new oneChar('_'));
            add(b);
        } else if(sp.dontMatch && sp.c == 'G') {
            add(new BackG());
        } else if(sp.dontMatch && sp.c == 's') {
            //Regex r = new Regex();
            //r._compile("[ \t\n\r\b]",mk);
            //add(new Goop("\\s",r.thePattern));
            Bracket b = new Bracket(false);
            b.addOr(new oneChar((char)32));
            b.addOr(new Range((char)8,(char)10));
            b.addOr(new oneChar((char)13));
            add(b);
        } else if(sp.dontMatch && sp.c == 'd') {
            Regex r = new Regex();
            //r._compile("[0-9]",mk);
            //add(new Goop("\\d",r.thePattern));
            Range digit = new Range('0','9');
            digit.printBrackets = true;
            add(digit);
        } else if(sp.dontMatch && sp.c == 'W') {
            Regex r = new Regex();
            //r._compile("[^a-zA-Z0-9_]",mk);
            //add(new Goop("\\W",r.thePattern));
            Bracket b = new Bracket(true);
            b.addOr(new Range('a','z'));
            b.addOr(new Range('A','Z'));
            b.addOr(new Range('0','9'));
            b.addOr(new oneChar('_'));
            add(b);
        } else if(sp.dontMatch && sp.c == 'S') {
            //Regex r = new Regex();
            //r._compile("[^ \t\n\r\b]",mk);
            //add(new Goop("\\S",r.thePattern));
            Bracket b = new Bracket(true);
            b.addOr(new oneChar((char)32));
            b.addOr(new Range((char)8,(char)10));
            b.addOr(new oneChar((char)13));
            add(b);
        } else if(sp.dontMatch && sp.c == 'D') {
            //Regex r = new Regex();
            //r._compile("[^0-9]",mk);
            //add(new Goop("\\D",r.thePattern));
            Bracket b = new Bracket(true);
            b.addOr(new Range('0','9'));
            add(b);
        } else if(sp.dontMatch && sp.c == 'B') {
            Regex r = new Regex();
            r._compile("(?!"+back_slash+"b)",mk);
            add(r.thePattern);
	} else if(isOctalString(sp)) {
	    int d = sp.c - '0';
	    sp.inc();
	    d = 8*d + sp.c - '0';
	    StrPos sp2 = new StrPos(sp);
	    sp2.inc();
	    if(isOctalDigit(sp2,false)) {
	      sp.inc();
	      d = 8*d + sp.c - '0';
	    }
	    add(new oneChar((char)d));
        } else if(sp.dontMatch && sp.c >= '1' && sp.c <= '9') {
            int iv = sp.c-'0';
            StrPos s2 = new StrPos(sp);
            s2.inc();
            if(!s2.dontMatch && s2.c >= '0' && s2.c <= '9') {
                iv = 10*iv+(s2.c-'0');
                sp.inc();
            }
            add(new BackMatch(iv));
        } else if(sp.dontMatch && sp.c == 'b') {
            add(new Boundary());
        } else if(sp.match('\b')) {
            add(new Boundary());
        } else if(sp.match('$')) {
            add(new End(true));
        } else if(sp.dontMatch && sp.c == 'Z') {
            add(new End(false));
        } else if(sp.match('.')) {
            add(new Any());
        } else if(sp.incMatch("(??")) {
            StringBuffer sb = new StringBuffer();
            StringBuffer sb2 = new StringBuffer();
            while(!sp.match(')') && !sp.match(':')) {
                sb.append(sp.c);
                sp.inc();
            }
            if(sp.incMatch(":")) {
                while(!sp.match(')')) {
                    sb2.append(sp.c);
                    sp.inc();
                }
            }
            String sbs = sb.toString();
            if(validators.get(sbs) instanceof String) {
                String pat = (String)validators.get(sbs);
                Regex r = newRegex();
		Rthings rth = new Rthings(this);
		rth.noBackRefs = true;
                r._compile(pat,rth);
                add(r.thePattern);
            } else {
                Custom cm = new Custom(sb.toString());
                if(cm.v != null) {
                    Validator v2 = cm.v.arg(sb2.toString());
                    if(v2 != null) {
                        v2.argsave = sb2.toString();
                        String p = cm.v.pattern;
                        cm.v = v2;
                        v2.pattern = p;
                    }
                    Regex r = newRegex();
		    Rthings rth = new Rthings(this);
		    rth.noBackRefs = true;
                    r._compile(cm.v.pattern,rth);
                    cm.sub = r.thePattern;
                    cm.sub.add(new CustomEndpoint(cm));
                    cm.sub.setParent(cm);
                    add(cm);
                }
            }
        } else if(sp.match('(')) {
            mk.parenLevel++;
            Regex r = newRegex();
            // r.or = new Or();
            sp.inc();
            if(sp.incMatch("?:")) {
                r.or = new Or();
            } else if(sp.incMatch("?=")) {
                r.or = new lookAhead(false);
            } else if(sp.incMatch("?!")) {
                r.or = new lookAhead(true);
            } else if(sp.match('?')) {
                sp.inc();
                do {
                    if(sp.c=='i')mk.ignoreCase = true;
                    if(sp.c=='Q')mk.dontMatchInQuotes = true;
                    if(sp.c=='o')mk.optimizeMe = true;
                    if(sp.c=='g')mk.gFlag = true;
                    if(sp.c=='s')mk.sFlag = true;
		    if(sp.c=='m')mk.mFlag = true;
                    sp.inc();
                } while(!sp.match(')') && !sp.eos);
                r = null;
                mk.parenLevel--;
                if(sp.eos) //throw new RegSyntax
                    RegSyntaxError.endItAll("Unclosed ()");
            } else { // just ordinary parenthesis
                r.or = mk.noBackRefs ? new Or() : new OrMark(mk.val++);
            }
            if(r != null) add(r._compile(sp,mk));
        } else if(sp.match('^')) {
            add(new Start(true));
        } else if(sp.dontMatch && sp.c=='A') {
            add(new Start(false));
        } else if(sp.match('*')) {
            addMulti(new patInt(0),new patInf());
        } else if(sp.match('+')) {
            addMulti(new patInt(1),new patInf());
        } else if(sp.match('?')) {
            addMulti(new patInt(0),new patInt(1));
        } else if(sp.match('{')) {
            boolean bad = false;
            StrPos sp2 = new StrPos(sp);
            StringBuffer sb = new StringBuffer();
            sp.inc();
            patInt i1 = sp.getPatInt();
            patInt i2 = null;
            if(sp.match('}')) {
                i2 = i1;
            } else {
                if(!sp.match(','))/*
                    RegSyntaxError.endItAll(
                       "String \"{"+i2+
                       "\" should be followed with , or }");*/
                    bad = true;
                sp.inc();
                if(sp.match('}'))
                    i2 = new patInf();
                else
                    i2 = sp.getPatInt();
            }
            if(i1 == null || i2 == null) /*
                                throw new RegSyntax("Badly formatted Multi: "
                                +"{"+i1+","+i2+"}"); */ bad = true;
            if(bad) {
                sp.dup(sp2);
                add(new oneChar(sp.c));
            } else
                addMulti(i1,i2);
	} else if(sp.escMatch('x') && next2Hex(sp)) { 
	    sp.inc();
	    int d = getHexDigit(sp);
	    sp.inc();
            d = 16*d + getHexDigit(sp);
	    add(new oneChar((char)d));
	} else if(sp.escMatch('c')) {
	    sp.inc();
	    if(sp.c < Ctrl.cmap.length)
	      add(new oneChar(Ctrl.cmap[sp.c]));
	    else
	      add(new oneChar(sp.c));
	} else if(sp.escMatch('f')) {
	    add(new oneChar((char)12));
	} else if(sp.escMatch('a')) {
	    add(new oneChar((char)7));
	} else if(sp.escMatch('t')) {
	    add(new oneChar('\t'));
        } else if(sp.escMatch('n')) {
            add(new oneChar('\n'));
        } else if(sp.escMatch('r')) {
            add(new oneChar('\r'));
        } else if(sp.escMatch('b')) {
            add(new oneChar('\b'));
        } else if(sp.escMatch('e')) {
            add(new oneChar((char)27));
        } else {
            add(new oneChar(sp.c));
            if(sp.match(')'))
                RegSyntaxError.endItAll("Unmatched right paren in pattern");
        }
    }

    // compiles all Pattern elements, internal method
    private Pattern _compile(String pat,Rthings mk) throws RegSyntax {
        minMatch = null;
        sFlag = mFlag = ignoreCase = gFlag = false;
        StrPos sp = new StrPos(pat,0);
        thePattern = _compile(sp,mk);
        pt.marks = null;
        return thePattern;
    }

    Pattern p = null;
    Or or = null;
    Pattern _compile(StrPos sp,Rthings mk) throws RegSyntax {
        while(!(sp.eos || (or != null && sp.match(')')) )) {
            compile1(sp,mk);
            sp.inc();
        }
        if(sp.match(')')) mk.parenLevel--;
        else if(sp.eos && mk.parenLevel != 0) {
            RegSyntaxError.endItAll("Unclosed Parenthesis! lvl="+mk.parenLevel);
        } if(or != null) {
            if(p == null) p = new NullPattern();
            or.addOr(p);
            return or;
        }
        return p==null ? new NullPattern() : p;
    }

    // add a multi object to the end of the chain
    // which applies to the last object
    void addMulti(patInt i1,patInt i2) throws RegSyntax {
        Pattern last,last2;
        for(last = p;last != null && last.next != null;last=last.next)
            ;
        if(last == null || last == p)
            last2 = null;
        else
            for(last2 = p;last2.next != last;last2=last2.next)
                ;
        if(last instanceof Multi && i1.intValue()==0 &&
                i2.intValue()==1)
            ((Multi)last).matchFewest = true;
        else if(last instanceof FastMulti && i1.intValue()==0 &&
                i2.intValue()==1)
            ((FastMulti)last).matchFewest = true;
        else if(last instanceof DotMulti && i1.intValue()==0 &&
                i2.intValue()==1)
            ((DotMulti)last).matchFewest = true;
	else if(last instanceof Multi
	     || last instanceof DotMulti
	     || last instanceof FastMulti)
	    throw new RegSyntax("Syntax error.");
        else if(last2 == null)
            p = mkMulti(i1,i2,p);
        else
            last2.next = mkMulti(i1,i2,last);
    }
    final static Pattern mkMulti(patInt lo,patInt hi,Pattern p) throws RegSyntax {
        if(p instanceof Any && p.next == null)
            return (Pattern)new DotMulti(lo,hi);
        return RegOpt.safe4fm(p) ? (Pattern)new FastMulti(lo,hi,p) :
        (Pattern)new Multi(lo,hi,p);
    }
    // process the bracket operator
    Pattern matchBracket(StrPos sp) throws RegSyntax {
        Bracket ret;
        if(sp.match('^')) {
            ret = new Bracket(true);
            sp.inc();
        } else
            ret = new Bracket(false);
        if(sp.match(']'))
            //throw new RegSyntax
            RegSyntaxError.endItAll("Unmatched []");

        while(!sp.eos && !sp.match(']')) {
            StrPos s1 = new StrPos(sp);
            s1.inc();
            StrPos s1_ = new StrPos(s1);
            s1_.inc();
            if(s1.match('-') && !s1_.match(']')) {
                StrPos s2 = new StrPos(s1);
                s2.inc();
                if(!s2.eos)
                    ret.addOr(new Range(sp.c,s2.c));
                sp.inc();
                sp.inc();
            } else if(sp.escMatch('Q')) {
                sp.inc();
                while(!sp.escMatch('E')) {
                    ret.addOr(new oneChar(sp.c));
                    sp.inc();
                }
            } else if(sp.escMatch('d')) {
                ret.addOr(new Range('0','9'));
            } else if(sp.escMatch('s')) {
                ret.addOr(new oneChar((char)32));
                ret.addOr(new Range((char)8,(char)10));
                ret.addOr(new oneChar((char)13));
            } else if(sp.escMatch('w')) {
                ret.addOr(new Range('a','z'));
                ret.addOr(new Range('A','Z'));
                ret.addOr(new Range('0','9'));
                ret.addOr(new oneChar('_'));
            } else if(sp.escMatch('D')) {
                ret.addOr(new Range((char)0,(char)47));
                ret.addOr(new Range((char)58,(char)65535));
            } else if(sp.escMatch('S')) {
                ret.addOr(new Range((char)0,(char)7));
                ret.addOr(new Range((char)11,(char)12));
                ret.addOr(new Range((char)14,(char)31));
                ret.addOr(new Range((char)33,(char)65535));
            } else if(sp.escMatch('W')) {
                ret.addOr(new Range((char)0,(char)64));
                ret.addOr(new Range((char)91,(char)94));
                ret.addOr(new oneChar((char)96));
                ret.addOr(new Range((char)123,(char)65535));
	    } else if(sp.escMatch('x') && next2Hex(sp)) { 
	        sp.inc();
	        int d = getHexDigit(sp);
	        sp.inc();
                d = 16*d + getHexDigit(sp);
	        ret.addOr(new oneChar((char)d));
	    } else if(sp.escMatch('a')) {
	        ret.addOr(new oneChar((char)7));
	    } else if(sp.escMatch('f')) {
	        ret.addOr(new oneChar((char)12));
	    } else if(sp.escMatch('e')) {
	        ret.addOr(new oneChar((char)27));
	    } else if(sp.escMatch('n')) {
	        ret.addOr(new oneChar('\n'));
	    } else if(sp.escMatch('t')) {
	        ret.addOr(new oneChar('\t'));
	    } else if(sp.escMatch('r')) {
	        ret.addOr(new oneChar('\r'));
	    } else if(sp.escMatch('c')) {
	        sp.inc();
	        if(sp.c < Ctrl.cmap.length)
	          ret.addOr(new oneChar(Ctrl.cmap[sp.c]));
	        else
	          ret.addOr(new oneChar(sp.c));
	    } else if(isOctalString(sp)) {
	        int d = sp.c - '0';
	        sp.inc();
	        d = 8*d + sp.c - '0';
	        StrPos sp2 = new StrPos(sp);
	        sp2.inc();
	        if(isOctalDigit(sp2,false)) {
	          sp.inc();
	          d = 8*d + sp.c - '0';
	        }
	        ret.addOr(new oneChar((char)d));
            } else
                ret.addOr(new oneChar(sp.c));
            sp.inc();
        }
        return ret;
    }

    /** Converts the stored Pattern to a String -- this is a
          decompile.  Note that \t and \n will really print out here,
          Not just the two character representations.
          Also be prepared to see some strange output if your characters
          are not printable. */
    public String toString() {
        if( false && thePattern == null )
            return "";
        else {
            StringBuffer sb = new StringBuffer();
            if(esc != Pattern.ESC) {
                sb.append("(?e=");
                sb.append(esc);
                sb.append(")");
            }
            if(gFlag
	    ||mFlag
	    ||!dotDoesntMatchCR
	    ||sFlag
	    ||ignoreCase
	    ||dontMatchInQuotes
	    ||optimized()) {
                sb.append("(?");
                if(ignoreCase)sb.append("i");
		if(mFlag)sb.append("m");
		if(sFlag||!dotDoesntMatchCR)sb.append("s");
                if(dontMatchInQuotes)sb.append("Q");
                if(optimized())sb.append("o");
                if(gFlag)sb.append("g");
                sb.append(")");
            }
            String patstr = thePattern.toString();
            if(esc != Pattern.ESC)
                patstr = reEscape(patstr,Pattern.ESC,esc);
            sb.append(patstr);
            return sb.toString();
        }
    }
    // Re-escape Pattern, allows us to use a different escape
    // character.
    static String reEscape(String s,char oldEsc,char newEsc) {
        if(oldEsc == newEsc) return s;
        int i;
        StringBuffer sb = new StringBuffer();
        for(i=0;i<s.length();i++) {
            if(s.charAt(i)==oldEsc && i+1 < s.length()) {
                if(s.charAt(i+1)==oldEsc) {
                    sb.append(oldEsc);
                } else {
                    sb.append(newEsc);
                    sb.append(s.charAt(i+1));
                }
                i++;
            } else if(s.charAt(i)==newEsc) {
                sb.append(newEsc);
                sb.append(newEsc);
            } else {
                sb.append(s.charAt(i));
            }
        }
        return sb.toString();
    }
    /** This method implements FilenameFilter, allowing one
         to use a Regex to search through a directory using File.list.
         There is a FileRegex now that does this better.
         @see com.stevesoft.pat.FileRegex
        */
    public boolean accept(File dir,String s) {
        return search(s);
    }
    /** The version of this package */
    final static public String version() {
        return "lgpl release 1.5.3";
    }
    /** Once this method is called, the state of variables
        ignoreCase and dontMatchInQuotes should not be changed as the
        results will be unpredictable.  However,
        search and matchAt will run more quickly.  Note that you
        can check to see if the pattern has been optimized by calling
        the optimized() method.<p>This method will attempt to rewrite
        your pattern in a way that makes it faster (not all patterns
        execute at the same speed).  In general, "(?: ... )" will be
        faster than "( ... )" so if you don't need the backreference,
        you should group using the former pattern.<p>It will also
        introduce new pattern elements that you can't get to otherwise,
        for example if you have a large table of strings, i.e. the
        months of the year "(January|February|...)" optimize() will make
        a Hashtable that takes it to the next appropriate pattern
        element -- eliminating the need for a linear search.
        @see com.stevesoft.pat.Regex#optimized
        @see com.stevesoft.pat.Regex#ignoreCase
        @see com.stevesoft.pat.Regex#dontMatchInQuotes
        @see com.stevesoft.pat.Regex#matchAt
        @see com.stevesoft.pat.Regex#search
        */
    public void optimize() {
        if(optimized()||thePattern==null) return;
        minMatch = new patInt(0);//thePattern.countMinChars();
        thePattern = RegOpt.opt(thePattern,ignoreCase,
            dontMatchInQuotes);
        skipper = Skip.findSkip(this);
        //RegOpt.setParents(this);
        return;
    }
    Skip skipper;
    /** This function returns true if the optimize method has
         been called. */
    public boolean optimized() {
        return minMatch != null;
    }

    /** A bit of syntactic surgar for those who want to make
        their code look more perl-like.  To use this initialize
        your Regex object by saying:
        <pre>
        Regex r1 = Regex.perlCode("s/hello/goodbye/");
        Regex r2 = Regex.perlCode("s'fish'frog'i");
        Regex r3 = Regex.perlCode("m'hello');
        </pre>
        The i for ignoreCase is supported in
        this syntax, as well as m, s, and x.  The g flat
	is a bit of a special case.<p>
	If you wish to replace all occurences of a pattern, you
        do not put a 'g' in the perlCode, but call Regex's
        replaceAll method.<p>
	If you wish to simply
        and only do a search for r2's pattern, you can do this
        by calling the searchFrom method method repeatedly, or
	by calling search repeatedly if the g flag is set.
        <p>
        Note: Currently perlCode does <em>not</em>
	support the (?e=#) syntax for
        changing the escape character.
    */

    public static Regex perlCode(String s) {
        // this file is big enough, see parsePerl.java
        // for this function.
        return parsePerl.parse(s);
    }
    static final char back_slash = '\\';

    /** Checks to see if there are only literal and no special
        pattern elements in this Regex. */
    public boolean isLiteral() {
        Pattern x = thePattern;
        while(x != null) {
            if(x instanceof oneChar)
                ;
            else if(x instanceof Skipped)
                ;
            else
                return false;
            x = x.next;
        }
        return true;
    }

    /** You only need to know about this if you are inventing
        your own pattern elements. */
    public patInt countMinChars() { return thePattern.countMinChars(); }
    /** You only need to know about this if you are inventing
        your own pattern elements. */
    public patInt countMaxChars() { return thePattern.countMaxChars(); }

    boolean isHexDigit(StrPos sp) {
      boolean r =
        !sp.eos && !sp.dontMatch
        && ((sp.c>='0'&&sp.c<='9')
	  ||(sp.c>='a'&&sp.c<='f')
	  ||(sp.c>='A'&&sp.c<='F'));
      return r;
    }
    boolean isOctalDigit(StrPos sp,boolean first) {
      boolean r =
        !sp.eos && !(first^sp.dontMatch)
        && sp.c>='0'&&sp.c<='7';
      return r;
    }
    int getHexDigit(StrPos sp) {
      if(sp.c >= '0' && sp.c <= '9')
        return sp.c - '0';
      if(sp.c >= 'a' && sp.c <= 'f')
        return sp.c - 'a' + 10;
      return sp.c - 'A' + 10;
    }
    boolean next2Hex(StrPos sp) {
      StrPos sp2 = new StrPos(sp);
      sp2.inc();
      if(!isHexDigit(sp2))
        return false;
      sp2.inc();
      if(!isHexDigit(sp2))
        return false;
      return true;
    }
    boolean isOctalString(StrPos sp) {
      if(!isOctalDigit(sp,true))
        return false;
      StrPos sp2 = new StrPos(sp);
      sp2.inc();
      if(!isOctalDigit(sp2,false))
        return false;
      return true;
    }
}

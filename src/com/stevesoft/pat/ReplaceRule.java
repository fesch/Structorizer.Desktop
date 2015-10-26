//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

import java.util.*;

/** ReplaceRule is a singly linked list of Objects which describe how
    to replace the matched portion of a String.  The only member method
    that you absolutely need to define to use this class is apply(StringBuffer,RegRes) --
    although you may want define toString1() and clone1() (if you are
    unhappy with the default methods) that are needed by
    the clone() or toString() methods on this class.
    During the replacement process, each ReplaceRule tells the replacer
    what to add to StringBuffer and uses the contents of the Regular
    expression result to get the information it needs to
    do this.  Here is an <a href="http://javaregex.com/code/fancy.java.html">example</a>

    @see com.stevesoft.pat.NullRule
    @see com.stevesoft.pat.AmpersandRule
    @see com.stevesoft.pat.BackRefRule
    @see com.stevesoft.pat.LeftRule
    @see com.stevesoft.pat.RightRule
    @see com.stevesoft.pat.StringRule
    */
public abstract class ReplaceRule {
    /** points to the next ReplaceRule in the linked list. */
    protected ReplaceRule next = null;
    /** This function appends to the StringBufferLike the text you want
        to replaced the portion of the String last matched. */
    public abstract void apply(StringBufferLike sb,RegRes r);

    /** A rule describing how to clone only the current ReplaceRule,
        and none of the others in this linked list.  It is called by
        clone() for each item in the list. */
    public Object clone1() {
        return new RuleHolder(this);
    }
    public final Object clone() {
        ReplaceRule x = (ReplaceRule)clone1();
        ReplaceRule xsav = x;
        ReplaceRule y = this;
        while(y.next != null) {
            x.next = (ReplaceRule)y.next.clone1();
            x.name = y.name;
            x = x.next;
            y = y.next;
        }
        return xsav;
    }
    static ReplaceRule add(ReplaceRule head,ReplaceRule adding) {
        if(head == null)
            return head = adding;
        head.addRule(adding);
        return head;
    }
    public ReplaceRule add(ReplaceRule adding) {
        return add(this,adding);
    }
    /** Add another ReplaceRule to the linked list. */
    public void addRule(ReplaceRule r) {
        if(next == null) next = r;
        else next.addRule(r);
    }
    static Regex getvar = null;
    final static Regex getv() {
        // Thanks to Michael Jimenez for pointing out the need
        // to clone getvar rather than simply returning it.
        // Previously this was not thread safe.
        //if(getvar != null) return getvar;
        if(getvar != null) return (Regex)getvar.clone();
        getvar=
            new Regex(
	    "(?:\\\\(\\d+)|"+ // ref 1
	    "\\$(?:"+
	      "(\\d+)|"+ // ref 2
	      "(\\w+)|"+ // ref 3
              "([&'`])|"+ // ref 4
	      "\\{(?:(\\d+)|"+ // ref 5
	        "([^\n}\\\\]+))}"+ // ref 6
	      ")|"+ 
	    "\\\\([nrbtaef])|"+ // ref 7
	    "\\\\c([\u0000-\uFFFF])|"+ // ref 8
	    "\\\\x([A-Fa-f0-9]{2})|"+ // ref 9
	    "\\\\([\u0000-\uFFFF])"+ // ref 10
	    ")");
        getvar.optimize();
        return getvar;
    }
    /** Compile a ReplaceRule using the text that would go between
        the second and third /'s in a typical substitution pattern
        in Perl: s/ ... / <i>The argument to ReplaceRule.perlCode</i> /.
        */
    public static ReplaceRule perlCode(String s) {
        //String sav_backGs = Regex.backGs;
        //int sav_backGto = Regex.backGto;
        try {
            int mf = 0, mt = 0;
            Regex gv = getv();
            ReplaceRule head = null;
            Object tmp = null;
            while(gv.searchFrom(s,mt)) {
                int off=Regex.BackRefOffset-1;
                mf = gv.matchedFrom();
                if(mf > mt)
                    head=add(head,
                        new StringRule(s.substring(mt,mf)));
                String var = null;
                if((var=gv.stringMatched(1+off)) != null
                        || (var=gv.stringMatched(2+off)) != null
                        || (var=gv.stringMatched(5+off)) != null) {
                    int d=0;
                    for(int i=0;i<var.length();i++)
                        d = 8*d+( var.charAt(i)-'0' );
		    if(var.length() == 1)
                      head=add(head,new BackRefRule(d));
		    else
		      head=new StringRule(""+(char)d);
                } else if(
                        (var=gv.stringMatched(10+off)) != null) {
                    if("QELlUu".indexOf(var) >= 0)
                        head=add(head,new CodeRule(var.charAt(0)) );
                    else
                        head=add(head,new StringRule(var) );
                } else if(
                        (var=gv.stringMatched(3+off)) != null
                        || (var=gv.stringMatched(4+off)) != null
                        || (var=gv.stringMatched(6+off)) != null) {
                    String arg = "";
                    int pc;
                    if((pc=var.indexOf(':')) > 0) {
                        arg = var.substring(pc+1);
                        var = var.substring(0,pc);
                    }
                    if(var.equals("&")||var.equals("MATCH")) {
                        head=add(head,new AmpersandRule());
                    } else if(var.equals("`")||var.equals("PREMATCH")) {
                        head=add(head,new LeftRule());
                    } else if(var.equals("'")||var.equals("POSTMATCH")) {
                        head=add(head,new RightRule());
                    } else if(var.equals("WANT_MORE_TEXT")) {
                        head=add(head,new WantMoreTextReplaceRule());
                    } else if(var.equals("POP")) {
                        head=add(head,new PopRule());
                    } else if(var.startsWith("+") && (tmp=defs.get(var.substring(1))) != null) {
                        if(tmp instanceof Regex)
                            head=add(head,new PushRule(var.substring(1),(Regex)tmp));
                        else if(tmp instanceof Transformer)
                            head=add(head,new PushRule(var.substring(1),(Transformer)tmp));
                        else head=add(head,new StringRule("${"+var+"}"));
                    } else if(var.startsWith("=") && (tmp=defs.get(var.substring(1))) != null) {
                        if(tmp instanceof Regex)
                            head=add(head,new ChangeRule(var.substring(1),(Regex)tmp));
                        else if(tmp instanceof Transformer)
                            head=add(head,new ChangeRule(var.substring(1),(Transformer)tmp));
                        else head=add(head,new StringRule("${"+var+"}"));
                    } else if( (tmp=defs.get(var)) != null) {
                        if(tmp instanceof ReplaceRule) {
                            ReplaceRule alt = ((ReplaceRule)tmp).arg(arg);
                            if(alt == null) alt = ((ReplaceRule)tmp);
                            head=add(head,(ReplaceRule)(alt.clone()));
                        }
                    } else // can't figure out how to transform this thing...
                        head=add(head,new StringRule("${"+var+"}"));
                } else if(
		  (var = gv.stringMatched(7+off)) != null) {
		  char c = var.charAt(0);
		  if(c == 'n')
		    head=add(head,new StringRule("\n"));
		  else if(c == 't')
		    head=add(head,new StringRule("\t"));
		  else if(c == 'r')
		    head=add(head,new StringRule("\r"));
		  else if(c == 'b')
		    head=add(head,new StringRule("\r"));
		  else if(c == 'a')
		    head=add(head,new StringRule(""+(char)7));
		  else if(c == 'e')
		    head=add(head,new StringRule(""+(char)27));
		  else if(c == 'f')
		    head=add(head,new StringRule(""+(char)12));
		} else if(
		  (var = gv.stringMatched(8+off)) != null) {
		  char c = var.charAt(0);
		  if(c < Ctrl.cmap.length)
		    c = Ctrl.cmap[c];
		  head=add(head,new StringRule(""+c));
		} else if(
		  (var = gv.stringMatched(9+off)) != null) {
		  int d =
		    16*getHexDigit(var.charAt(0))+
		    getHexDigit(var.charAt(1));
		  head=add(head,new StringRule(""+(char)d));
		}
                mt = gv.matchedTo();
            }
            if(mt <= s.length())
                head=add(head,new StringRule(s.substring(mt)));
            return head;
        } finally {
            //Regex.backGs = sav_backGs;
            //Regex.backGto = sav_backGto;
        }
    }
    static Hashtable defs = new Hashtable();
    public static boolean isDefined(String s) { return defs.get(s) != null; }
    public static void define(String s,Regex r) { defs.put(s,r); }
    public static void define(String s,ReplaceRule r) {
        defs.put(s,r);
        r.name = s;
    }
    String name = getClass().getName();
    public static void define(String s,Transformer t) { defs.put(s,t); }
    public static void undefine(String s) { defs.remove(s); }
    /** This tells how to convert just the current element (and none
        of the other items in the linked list) to a String. This
        method is called by toString() for each item in the linked
        list. */
    public String toString1() {
        return "${"+name+"}";
    }
    /** Convert to a String. */
    public final String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(toString1());
        ReplaceRule rr = this.next;
        while(rr != null) {
            sb.append(rr.toString1());
            rr = rr.next;
        }
        return sb.toString();
    }
    /** Modified the behavior of a ReplaceRule by supplying
        an argument.  If a ReplaceRule named "foo" is defined
        and the pattern "s/x/${foo:5}/" is given to Regex.perlCode,
        then the "foo" the definition of "foo" will be retrieved
        and arg("5") will be called.  If the result is non-null,
        that is the ReplaceRule that will be used.  If the result
        is null, then the pattern works just as if it were
        "s/x/${foo}/".
        @see com.stevesoft.pat.Validator#arg(java.lang.String)
        */
    public ReplaceRule arg(String s) { return null; }
    static int getHexDigit(char c) {
      if(c >= '0' && c <= '9')
        return c - '0';
      if(c >= 'a' && c <= 'f')
        return c - 'a'+10;
      return c - 'A'+10;
    }
}

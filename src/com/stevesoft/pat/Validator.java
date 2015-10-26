//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

/** This class makes it easy to create your own patterns
and integrate them into Regex.  For more detail, see the
example file <a href="http://javaregex.com/code/deriv2.java.html">deriv2.java</a> or
<a href="http://javaregex.com/code/deriv3.java.html">deriv3.java</a>. */

public class Validator {
    String argsave = null;
    String pattern = ".";
    /**
    This method does extra checking on a matched section of
    a String beginning at position start and ending at end.
    The idea is that you can do extra checking with this
    that you don't know how to do with a standard Regex.

    If this method is successful, it returns the location
    of the end of this pattern element -- that may be the
    value end provided or some other value.  A negative
    value signifies that a match failure.
    
    By default, this method just returns end and thus
    does nothing.
    @see com.stevesoft.pat.Regex#define(java.lang.String,java.lang.String,com.stevesoft.pat.Validator)
    */
    public int validate(StringLike src,int start,int end) {
        return end;
    }
    /* This method allows you to modify the behavior of this
    validator by making a new Validator object.  If a Validator
    named "foo" is defined, then the pattern "{??foo:bar}" will
    cause Regex to first get the Validator given to Regex.define
    and then to call its arg method with the string "bar".
    If this method returns a null (the default) you get the same
    behavior as the pattern "{??foo}" would supply. */
    public Validator arg(String s) { return null; }

    /** For optimization it is helpful, but not necessary, that
    you define the minimum number of characters this validator
    will allow to match.  To do this 
    return new patInt(number) where number is the smallest
    number of characters that can match. */
    public patInt minChars() { return new patInt(0); }

    /** For optimization it is helpful, but not necessary, that
    you define the maximum number of characters this validator
    will allow to match.  To do this either
    return new patInt(number), or new patInf() if an infinite
    number of characters may match. */
    public patInt maxChars() { return new patInf(); }
}

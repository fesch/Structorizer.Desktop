//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

/*
        Shareware: package pat
   <a href="copyright.html">Copyright 2001, Steven R. Brandt</a>
*/
/**
This type of syntax error is thrown whenever a syntax error
 is encountered in the pattern. It may not be caught directly, as
 it is not in the throws clause of any method.  To detect it, catch
 Throwable, and use instanceof to see if it is a RegSyntax. */
public class RegSyntax extends Exception {
    RegSyntax() {}
    RegSyntax(String msg) {
        super(msg);
    }
};

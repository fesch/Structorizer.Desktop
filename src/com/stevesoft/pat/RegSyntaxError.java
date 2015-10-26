//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

/** When enabled, this class is thrown instead of
    the normal RegSyntax.  Thus, enabling of this
    class will make your debugging easier -- but
    if you leave it on and forget to catch RegSyntaxError
    a user-supplied pattern could generate a
    RegSyntaxError that will kill your application.

    I strongly recommend turning this flag on, however,
    as I think it is more likely to help than to hurt
    your programming efforts.
    */
public class RegSyntaxError extends Error {
    public static boolean RegSyntaxErrorEnabled = false;
    public RegSyntaxError() {}
    public RegSyntaxError(String s) { super(s); }
    final static void endItAll(String s) throws RegSyntax {
        if(RegSyntaxErrorEnabled) throw new RegSyntaxError(s);
        throw new RegSyntax(s);
    }
}

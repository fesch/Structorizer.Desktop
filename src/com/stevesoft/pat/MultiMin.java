//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

/** This extension of multi is the version of multi
 which wants to match the fewest number of characters.
 It implements the *? type of syntax. */
class MultiMin extends Multi {
    MultiMin(patInt i1,patInt i2,Pattern p) throws RegSyntax {
        super(i1,i2,p);
        matchFewest = true;
    }
};

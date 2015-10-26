//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

/** This provides a base class for all the Unicode character
  * matching rules.
  */
class UniValidator extends Validator {
    public patInt minChars() { return new patInt(1); }
    public patInt maxChars() { return new patInt(1); }
}

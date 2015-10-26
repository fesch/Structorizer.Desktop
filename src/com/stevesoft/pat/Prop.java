//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

/** Get Unicode properties for a character.  See
<a href="http://unicode.org">http://unicode.org</a>. */
public class Prop {
    /** Is this a "Decimal Digit" according to Unicode? */
    public final static boolean isDecimalDigit(char c) {
      if(Bits.decimal_digit == null)
        Bits.decimal_digit_f();
      return Bits.decimal_digit.get(c);
    }
    /** Is this a "Alphabetic" according to Unicode? */
    public final static boolean isAlphabetic(char c) {
      if(Bits.letter == null)
        Bits.letter_f();
      return Bits.letter.get(c);
    }
    /** Is this a "Math" according to Unicode? */
    public final static boolean isMath(char c) {
      if(Bits.math == null)
        Bits.math_f();
      return Bits.math.get(c);
    }

    /** Is this a "Currency" according to Unicode? */
    public final static boolean isCurrency(char c) {
      if(Bits.currency == null)
        Bits.currency_f();
      return Bits.currency.get(c);
    }

    /** Is c a white space character according to Unicode? */
    public final static boolean isWhite(char c) {
      if(Bits.white == null)
        Bits.white_f();
      return Bits.white.get(c);
    }

    /** Is c a punctuation character according to Unicode? */
    public final static boolean isPunct(char c) {
      if(Bits.punct == null)
        Bits.punct_f();
      return Bits.punct.get(c);
    }
}

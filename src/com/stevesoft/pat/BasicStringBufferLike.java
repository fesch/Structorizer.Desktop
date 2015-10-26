package//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
com.stevesoft.pat;

/** An abstraction of the StringBuffer which only
    implements a subset of StringBuffer's methods.
    */
public interface BasicStringBufferLike {
  public void append(char c);
  public void append(String s);
  public StringLike toStringLike();
  public Object unwrap();
}

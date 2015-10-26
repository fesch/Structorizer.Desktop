package//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
com.stevesoft.pat;
import java.util.*;

/** The idea behind this class is simply to eliminate the need for
  * testing to see if Regex.thePattern is null.  Every instruction
  * we can eliminate from _search will help.
  */
public class NoPattern extends Pattern {
    public String toString() { return "(?e=#)[^#d#D]"; }
    public int matchInternal(int i,Pthings p) { return -1; }
    Pattern clone1(Hashtable h) { return new NoPattern(); }
}

package//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
com.stevesoft.pat;

/** Package pat can search anything that implements this
    interface.  Package pat assumes the following:
    <ul>
    <li>The StringLike object will not change.  Calls to
        charAt(int) will not vary with time.
    <li>The length of the object being searched is known
        before the search begins and does not vary with time.
    </ul>
    Note that searching String is probably faster than searching
    other objects, so searching String is still preferred if
    possible.
*/
public interface StringLike {
    public char charAt(int i);
    public String toString();
    public int length();
    public String substring(int i1,int i2);
    /** Obtain the underlying object, be it a String, char[],
        RandomAccessFile, whatever. */
    public Object unwrap();
    /** By default, the result is put in a String or char[]
        when a replace is done.  If you wish to save the result
        in some other StringBufferLike then you can do this
        by implementing this method, or over-riding it's behavior
        from an existing class. */
    public BasicStringBufferLike newStringBufferLike();
    public int indexOf(char c);
}

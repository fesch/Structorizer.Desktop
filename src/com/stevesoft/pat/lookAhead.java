//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
import java.util.Hashtable;

/** Implements "(?=  )" and "(?! )" */
class lookAhead extends Or {
    boolean reverse;
    lookAhead(boolean b) { reverse = b; }
    public Pattern getNext() { return null; }
    public int nextMatch(int pos,Pthings pt) {
        Pattern p = super.getNext();
        if(p != null) return p.matchInternal(pos,pt);
        else return pos;
    }
    public int matchInternal(int pos,Pthings pt) {
        if(super.matchInternal(pos,pt) >= 0) {
            if(reverse) return -1;
            else return nextMatch(pos,pt);
        } else {
            if(reverse) return nextMatch(pos,pt);
            else return -1;
        }
    }
    String leftForm() {
        if(reverse)
            return "(?!";
        else
            return "(?=";
    }
    public patInt minChars() { return new patInt(0); }
    public patInt maxChars() { return new patInt(0); }
    Pattern clone1(Hashtable h) {
        lookAhead la=new lookAhead(reverse);
        h.put(this,la);
        h.put(la,la);
        for(int i=0;i<v.size();i++)
            la.v.addElement( ((Pattern)v.elementAt(i)).clone(h) );
        return la;
    }
}

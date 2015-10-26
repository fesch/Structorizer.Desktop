//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
import java.util.Hashtable;
import java.util.Vector;

/** Implements the parenthesis pattern subelement.*/
class OrMark extends Or {
    SubMark sm = new SubMark();
    int id;
    OrMark(int i) {
        sm.om = this;
        id = i;
    }
    String leftForm() { return "("; }
    public Pattern getNext() {
        return sm;
    }
    public int matchInternal(int pos,Pthings pt) {
        sm.next = super.getNext();
        if(pt.marks == null) {
            int n2 = 2 * pt.nMarks+2;
            pt.marks = new int[n2];
            for(int i=0;i<n2;i++)
                pt.marks[i] = -1;
        }
        pt.marks[id] = pos;
        int ret = super.matchInternal(pos,pt);
        if(ret < 0)
            pt.marks[id] = -1;
        else if(pt.marks[id] > pt.marks[id+pt.nMarks]) {
            int swap = pt.marks[id];
            pt.marks[id] = pt.marks[id+pt.nMarks]+1;
            pt.marks[id+pt.nMarks] = swap+1;
        }
        return ret;
    }
    public Pattern clone1(Hashtable h) {
        OrMark om = new OrMark(id);
        h.put(om,om);
        h.put(this,om);
        for(int i=0;i<v.size();i++)
            om.v.addElement( ((Pattern)v.elementAt(i)).clone(h) );
        return om;
    }
};

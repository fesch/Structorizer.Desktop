//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

/** OrMark and SubMark together implement ( ... ) */
class SubMark extends Pattern {
    int end_pos,start_pos;
    OrMark om;
    public String toString() { return ""; }
    public int matchInternal(int i,Pthings pt) {
        pt.marks[om.id+pt.nMarks] = i;
        int ret=nextMatch(i,pt);
        if(ret < 0)
            pt.marks[om.id+pt.nMarks] = -1;
        return ret;
    }
}

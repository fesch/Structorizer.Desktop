//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
import java.util.Hashtable;

/** This class implements the (?@<>) syntax that matches
 a balanced parenthesis.  Not in perl 5. */
class Group extends Pattern {
    char op,cl;
    Group(char opi,char cli) {
        op = opi;
        cl = cli;
    }
    public int matchInternal(int pos,Pthings pt) {
        int i,count=1;
        if(pos < pt.src.length())
            if(!Masked(pos,pt) && pt.src.charAt(pos) != op)
                return -1;
        for(i=pos+1;i<pt.src.length();i++) {
            char c = pt.src.charAt(i);
            boolean b = !Masked(i,pt);
            if(b && c == ESC) {
                i++;
            } else {
                if(b && c == cl) count--;
                if(count == 0) return nextMatch(i+1,pt);
                if(b && c == op) count++;
            }
        }
        return -1;
    }
    public String toString() {
        return "(?@"+op+cl+")"+nextString();
    }
    public patInt minChars() { return new patInt(2); }
    Pattern clone1(Hashtable h) { return new Group(op,cl); }
};

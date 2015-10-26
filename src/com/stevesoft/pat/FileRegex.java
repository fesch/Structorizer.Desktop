//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;
import java.io.*;
import java.util.*;

/** This class is a different form of Regex designed to work more
 like the file matching utility of a Unix shell.  It is implemented
 by some simple string transformations:
 <center>
 <table border=1>
 <tr> <td> FileRegex </td> <td> Regex </td>
 <tr> <td> * </td><td> .* </td>
 <tr> <td> . </td><td> \. </td>
 <tr> <td> { </td><td> (?: </td>
 <tr> <td> {?! </td><td> (?! </td>
 <tr> <td> {?= </td><td> (?= </td>
 <tr> <td> {?? </td><td> (?? </td>
 <tr> <td> } </td><td> ) </td>
 <tr> <td> ? </td><td> . </td>
 <tr> <td> {,} </td><td> (|) </td>
 </table>
 </center>
 Note that a FileRegex pattern always ends with the Regex
 pattern element "$".  If you like to experiment, try making
 FileRegex's and then printing them out.  The toString() method
 does a decompile of the pattern to a standard Regex.  Here are
 some more complete examples:
 <center>
 <table border=3>
 <tr> <td> FileRegex </td><td> Regex </td>
 <tr> <td>*.java </td><td> .*\.java$ </td>
 <tr> <td>*.{java,html} </td><td> .*\.(java|html)$ </td>
 <tr> <td> foo.[chC] </td><td> foo.[chC]$ </td>
 </table>
 </center>
 */
public class FileRegex extends Regex {
    /** Build an unitialized FileRegex. */
    public FileRegex() { dirflag=EITHER; }
    /** Build a FileRegex form String s. */
    public FileRegex(String s) {
        super(s);
        dirflag = EITHER;
    }
    /** Compile a new pattern.
        Throws @exception com.stevesoft.pat.RegSyntax for
        nonsensical patterns like "[9-0]+" just as Regex does.
        @see com.stevesoft.pat#compile(java.lang.String)
        */
    public void compile(String s) throws RegSyntax {
	String npat = toFileRegex(s);
        super.compile(npat);
        if(File.separatorChar == '\\') // MS-DOS
            ignoreCase = true;
    }
    /** This is the method required by FileNameFilter.
       To get a listing of files in the current directory
         ending in .java, do this:
        <pre>
        File dot = new File(".");
        FileRegex java_files = new FileRegex("*.java");
        String[] file_list = dot.list(java_files);
        </pre>
        */
    public boolean accept(File dir,String s) {
        if(dirflag != EITHER) {
            File f = new File(s);
            if(f.isDirectory() && dirflag == NONDIR)
                return false;
            if(!f.isDirectory() && dirflag == DIR)
                return false;
        }
        return matchAt(s,0);
    }
    int dirflag = 0;
    final static int EITHER=0,DIR=1,NONDIR=2;

    /** Provides an alternative to File.list -- this
        separates its argument according to File.pathSeparator.
        To each path, it splits off a directory -- all characters
        up to and including the first instance of File.separator --
        and a file pattern -- the part that comes after the directory.
        It then produces a list of all the pattern matches on all
        the paths.  Thus "*.java:../*.java" would produce a list of
        all the java files in this directory and in the ".." directory
        on a Unix machine.  "*.java;..\\*.java" would do the same thing
        on a Dos machine. */
    public static String[] list(String f) {
        return list(f,EITHER);
    }
    static String[] list(String f,int df) {
        //return list_(f,new FileRegex());
        StringTokenizer st = new StringTokenizer(f,File.pathSeparator);
        Vector v = new Vector();
        while(st.hasMoreTokens()) {
            String path = st.nextToken();
            list1(path,v,df,true);
        }
        String[] sa = new String[v.size()];
        v.copyInto(sa);
        return sa;
    }
    final static Regex root=new Regex(File.separatorChar=='/' ?
        "/$" : "(?:.:|)\\\\$");
    static void list1(String path,Vector v,int df,boolean rec) {
	// if path looks like a/b/c/ or d:\ then add .
        if(root.matchAt(path,0)) {
            v.addElement(path+".");
            return;
        }
        File f = new File(path);
        if(f.getParent() != null && rec) {
            Vector v2 = new Vector();
            list1(f.getParent(),v2,DIR,true);
            for(int i=0;i<v2.size();i++) {
                String path2 = ((String)v2.elementAt(i))+
                    File.separator+f.getName();
                list1(path2,v,df,false);
            }
        } else {
            File base = new File(path);

            String dir_s = base.getParent();
            if(dir_s==null) dir_s=".";
            File dir = new File(dir_s);

            FileRegex fr = new FileRegex(base.getName());
            if(fr.isLiteral()) {
                v.addElement(dir_s+File.separator+base.getName());
                return;
            }
            fr.dirflag = df;
            String[] sa = dir.list(fr);
            if(sa == null) return;
            for(int i=0;i<sa.length;i++) {
                v.addElement(dir_s+File.separator+sa[i]);
            }
        }
    }

    /** This method takes a file regular expression, and translates it
            into the type of pattern used by a normal Regex. */
    public static String toFileRegex(String s) {
        StrPos sp = new StrPos(s,0);
        StringBuffer sb = new StringBuffer();
        if(sp.incMatch("{?e=")) {
            char e = sp.thisChar();
            sp.inc();
            if(sp.incMatch("}")) {
                sb.append("(?e="+e+")^");
            } else {
                sb.append("^(?e=");
            }
            sp.esc = e;
        }
        int ParenLvl = 0;
        while(!sp.eos()) {
	    if(File.separatorChar == '\\') {
	      if(sp.escaped())
	        sb.append("\\\\");
	      sp.dontMatch = false;
	    }
            if(sp.incMatch("?"))
                sb.append(".");
            else if(sp.incMatch(".")) {
                sb.append(sp.esc);
                sb.append('.');
            } else if(sp.incMatch("{??")) {
                sb.append("(??");
                ParenLvl++;
                // allow negative lookahead to work
            } else if(sp.incMatch("{?!")) {
                sb.append("(?!");
                ParenLvl++;
                // allow positive lookahead to work
            } else if(sp.incMatch("{?=")) {
                sb.append("(?=");
                ParenLvl++;
            } else if(sp.incMatch("{")) {
                sb.append("(?:");
                ParenLvl++;
            } else if(sp.incMatch("}")) {
                sb.append(')');
                ParenLvl--;
            } else if(ParenLvl != 0 && sp.incMatch(","))
                sb.append('|');
            else if(sp.incMatch("*"))
                sb.append(".*");
            else {
                sb.append(sp.thisChar());
                sp.inc();
            }
        }
        sb.append("$");
        return sb.toString();
    }
    public boolean isLiteral() {
        Pattern x = thePattern;
        while(x != null && !(x instanceof End)) {
            if(x instanceof oneChar)
                ;
            else if(x instanceof Skipped)
                ;
            else return false;
            x = x.next;
        }
        return true;
    }
}

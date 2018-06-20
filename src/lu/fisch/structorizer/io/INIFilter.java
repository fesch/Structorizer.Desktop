package lu.fisch.structorizer.io;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    Input filter for Structorizer ini files.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.27      First Issue
 *      Kay Gürtzig     2018.06.08      Inheritance changed
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.io.File;

public class INIFilter extends ExtFileFilter {
	
    public static boolean isKnown(String _filename)
    {
            return
            (getExtension(_filename).equals("ini"))
            ;
    }

//    public static String getExtension(String s)
//    {
//            String ext = null;
//            int i = s.lastIndexOf('.');
//
//            if (i > 0 &&  i < s.length() - 1)
//            {
//                    ext = s.substring(i+1).toLowerCase();
//            }
//            return ext;
//    }
//
//    public static String getExtension(File f)
//    {
//            String ext = null;
//            String s = f.getName();
//            int i = s.lastIndexOf('.');
//
//            if (i > 0 &&  i < s.length() - 1)
//            {
//                    ext = s.substring(i+1).toLowerCase();
//            }
//
//            return ext;
//    }

    @Override
    public String getDescription()
    {
        return "Configuration File";
    }
	
    @Override
    public boolean accept(File f) 
	{
        if (f.isDirectory()) 
		{
            return true;
        }
		
//        String extension = getExtension(f);
//        if (extension != null) 
//		{
            return isKnown(f.getName());
//		}
//		
//        return false;
    }
	
}

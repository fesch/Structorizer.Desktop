/*
    Structorizer
    A little tool which you can use to create Nassi-Schneiderman Diagrams (NSD)

    Copyright (C) 2009  Bob Fisch

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or any
    later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package lu.fisch.structorizer.generators;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    Abstract class for any code generator.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.27		First Issue
 *      Bob Fisch       2008.04.12		Plugin Interface
 *      Kay Gürtzig     2014.11.16		comment generation revised (see comment below)
 *
 ******************************************************************************************************
 *
 *      Comment:		
 *      2014.11.16 - Enhancement
 *      - method insertComment renamed to insertAsComment (as it inserts the instruction text!)
 *      - overloaded method insertComment added to export the actual element comment
 *      
 ******************************************************************************************************///

import java.awt.Frame;
import java.io.*;

import javax.swing.*;

import lu.fisch.utils.*;
import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.gui.ExportOptionDialoge;
import lu.fisch.structorizer.io.Ini;


public abstract class Generator extends javax.swing.filechooser.FileFilter
{
        private ExportOptionDialoge eod = null;
	protected StringList code = new StringList();

	/************ Fields ***********************/
	protected abstract String getDialogTitle();
	protected abstract String getFileDescription();
	protected abstract String getIndent();
	protected abstract String[] getFileExtensions();
	
	
	/************ Code Generation **************/
	// KGU 2014-11-16: Method renamed (formerly: insertComment)
	protected boolean insertAsComment(Element _element, String _indent, String _symbol)
	{
		if(eod.commentsCheckBox.isSelected())
		{
			for(int i=0;i<_element.getText().count();i++)
			{
				code.add(_indent+_symbol+_element.getText().get(i));
			}
			return true;
		}
		return false;
	}
    
	// START KGU 2014-11-16:
	/**
	 * Inserts the comment part of _element into the code, using delimiters _symbolLeft
	 * and _symbolRight (if given) to enclose the comment lines, with indentation _indent 
	 * @param _element current NSD element
	 * @param _indent indentation string
	 * @oaram _symbolLeft left comment delimiter, e.g. "/*", "//", "(*", or "{"
	 * @param _symbolRight right comment delimiter if required, e.g. "}", "*)"
	 */
	protected void insertComment(Element _element, String _indent, String _symbolLeft, String _symbolRight)
	{
		if (_symbolRight == null) {
			_symbolRight = "";
		}
        for(int i = 0; i < _element.getComment().count(); i++)
        {
        	// The following splitting is just to avoid empty comment lines and broken
        	// comment lines (though the latter shouldn't be possible here)
        	String commentLine = _element.getComment().get(i);
        	// Skip an initial empty comment line
       		if (i > 0 || !commentLine.isEmpty()) {
       			code.add(_indent + _symbolLeft + commentLine + _symbolRight);
       		}
        }
	}
	
	/**
	 * Inserts the comment part of _element into the code, using line comment symbol
	 * _symbol with indentation _indent 
	 * @param _element current NSD element
	 * @param _indent indentation string
	 * @oaram _symbol language-dependent line comment symbol, e.g. "//" or "#"
	 */
	protected void insertComment(Element _element, String _indent, String _symbol)
	{
		this.insertComment(_element, _indent, _symbol, null);
	}
	// END KGU 2014-11-16
        
	protected void generateCode(Instruction _inst, String _indent)
	{
            //
	}
	
	protected void generateCode(Alternative _alt, String _indent)
	{
		// code.add(_indent+"");
		generateCode(_alt.qTrue,_indent+_indent.substring(0,1));
		// code.add(_indent+"");
		generateCode(_alt.qFalse,_indent+_indent.substring(0,1));
		// code.add(_indent+"");
	}

	protected void generateCode(Case _case, String _indent)
	{
		// code.add(_indent+"");
		for(int i=0;i<_case.qs.size();i++)
		{
			// code.add(_indent+"");
			generateCode((Subqueue) _case.qs.get(i),_indent+_indent.substring(0,1));
			// code.add(_indent+"");
		}
		// code.add(_indent+"");
	}

	protected void generateCode(For _for, String _indent)
	{
		// code.add(_indent+"");
		generateCode(_for.q,_indent+_indent.substring(0,1));
		// code.add(_indent+"");
	}

	protected void generateCode(While _while, String _indent)
	{
		// code.add(_indent+"");
		generateCode(_while.q,_indent+_indent.substring(0,1));
		// code.add(_indent+"");
	}

	protected void generateCode(Repeat _repeat, String _indent)
	{
		// code.add(_indent+"");
		generateCode(_repeat.q,_indent+_indent.substring(0,1));
		// code.add(_indent+"");
	}

	protected void generateCode(Forever _forever, String _indent)
	{
		// code.add(_indent+"");
		generateCode(_forever.q,_indent+_indent.substring(0,1));
		// code.add(_indent+"");
	}
	
	protected void generateCode(Call _call, String _indent)
	{
		// code.add(_indent+"");
	}

	protected void generateCode(Jump _jump, String _indent)
	{
		// code.add(_indent+"");
	}

	protected void generateCode(Parallel _para, String _indent)
	{
		// code.add(_indent+"");
	}

	protected void generateCode(Element _ele, String _indent)
	{
		if(_ele.getClass().getSimpleName().equals("Instruction"))
		{
			generateCode((Instruction) _ele,_indent);
		}
		else if(_ele.getClass().getSimpleName().equals("Alternative"))
		{
			generateCode((Alternative) _ele,_indent);
		}
		else if(_ele.getClass().getSimpleName().equals("Case"))
		{
			generateCode((Case) _ele,_indent);
		}
		else if(_ele.getClass().getSimpleName().equals("Parallel"))
		{
			generateCode((Parallel) _ele,_indent);
		}
		else if(_ele.getClass().getSimpleName().equals("For"))
		{
			generateCode((For) _ele,_indent);
		}
		else if(_ele.getClass().getSimpleName().equals("While"))
		{
			generateCode((While) _ele,_indent);
		}
		else if(_ele.getClass().getSimpleName().equals("Repeat"))
		{
			generateCode((Repeat) _ele,_indent);
		}
		else if(_ele.getClass().getSimpleName().equals("Forever"))
		{
			generateCode((Forever) _ele,_indent);
		}
		else if(_ele.getClass().getSimpleName().equals("Call"))
		{
			generateCode((Call) _ele,_indent);
		}
		else if(_ele.getClass().getSimpleName().equals("Jump"))
		{
			generateCode((Jump) _ele,_indent);
		}
	}
	
	protected void generateCode(Subqueue _subqueue, String _indent)
	{
		// code.add(_indent+"");
		for(int i=0;i<_subqueue.children.size();i++)
		{
			generateCode((Element) _subqueue.children.get(i),_indent);
		}
		// code.add(_indent+"");
	}

	/******** Public Methods *************/

	public String generateCode(Root _root, String _indent)
	{
		// code.add("");
		generateCode(_root.children,_indent+_indent.substring(0,1));
		// code.add("");

		return code.getText();
	}
	
	public void exportCode(Root _root, File _currentDirectory, Frame frame)
	{
                try
                {
                    Ini ini = Ini.getInstance();
                    ini.load();
                    eod = new ExportOptionDialoge(frame);
                    if(ini.getProperty("genExportComments","0").equals("true"))
                        eod.commentsCheckBox.setSelected(true);
                    else 
                        eod.commentsCheckBox.setSelected(false);
                } 
                catch (FileNotFoundException ex)
                {
                    ex.printStackTrace();
                } 
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            
                JFileChooser dlgSave = new JFileChooser();
		dlgSave.setDialogTitle(getDialogTitle());
		
		// set directory
		if(_root.getFile()!=null)
		{
			dlgSave.setCurrentDirectory(_root.getFile());
		}
		else
		{
			dlgSave.setCurrentDirectory(_currentDirectory);
		}
		
		// propose name
		String nsdName = _root.getText().get(0);
		nsdName.replace(':', '_');
		if(nsdName.indexOf(" (")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf(" ("));}
		if(nsdName.indexOf("(")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf("("));}
		dlgSave.setSelectedFile(new File(nsdName));
		
		dlgSave.addChoosableFileFilter((javax.swing.filechooser.FileFilter) this);
		int result = dlgSave.showSaveDialog(frame);
		
		/***** file_exists check here!
		 if(file.exists())
		 {
		 JOptionPane.showMessageDialog(null,file);
		 int response = JOptionPane.showConfirmDialog (null,
		 "Overwrite existing file?","Confirm Overwrite",
		 JOptionPane.OK_CANCEL_OPTION,
		 JOptionPane.QUESTION_MESSAGE);
		 if (response == JOptionPane.CANCEL_OPTION)
		 {
		 return;
		 }
		 else
		 */
		String filename = new String();
		
		boolean saveIt = true;
                
		if (result == JFileChooser.APPROVE_OPTION) 
		{
			filename=dlgSave.getSelectedFile().getAbsoluteFile().toString();
			if(!isOK(filename))
			{
				filename+="."+getFileExtensions()[0];
			}
		}
		else
		{
			saveIt = false;
		}
		
		//System.out.println(filename);
		
		if (saveIt == true) 
		{
			File file = new File(filename);
                        boolean writeDown = true;

                        if(file.exists())
			{
                            int response = JOptionPane.showConfirmDialog (null,
                                            "Overwrite existing file?","Confirm Overwrite",
                                            JOptionPane.YES_NO_OPTION,
                                            JOptionPane.QUESTION_MESSAGE);
                            if (response == JOptionPane.NO_OPTION)
                            {
				writeDown=false;
                            }
                        }
                        if(writeDown==true)
                        {

                            try
                            {
                                    String code = BString.replace(generateCode(_root,"\t"),"\t",getIndent());

                                    BTextfile outp = new BTextfile(filename);
                                    outp.rewrite();
                                    outp.write(code);
                                    outp.close();
                            }
                            catch(Exception e)
                            {
                                    JOptionPane.showOptionDialog(null,"Error while saving the file!\n"+e.getMessage(),"Error",JOptionPane.OK_OPTION,JOptionPane.ERROR_MESSAGE,null,null,null);
                            }
                        }
		}
	} 
	
	/******* FileFilter Extension *********/
	protected boolean isOK(String _filename)
	{
		boolean res = false;
		if(getExtension(_filename)!=null)
		{
			for(int i =0; i<getFileExtensions().length; i++)
			{
				res = res || (getExtension(_filename).equals(getFileExtensions()[i]));
			}
		}
		return res;
	}
	
	private static String getExtension(String s) 
	{
		String ext = null;
		int i = s.lastIndexOf('.');
		
		if (i > 0 &&  i < s.length() - 1) 
		{
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}
	
	private static String getExtension(File f) 
	{
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		
		if (i > 0 &&  i < s.length() - 1) 
		{
			ext = s.substring(i+1).toLowerCase();
		}
		
		return ext;
	}
	
	public String getDescription() 
	{
        return getFileDescription();
    }
	
    public boolean accept(File f) 
	{
        if (f.isDirectory()) 
		{
            return true;
        }
		
        String extension = getExtension(f);
        if (extension != null) 
		{
            return isOK(f.getName());
		}
		
        return false;
    }
	

	/******* Constructor ******************/

	public Generator()
	{
	}
	
}

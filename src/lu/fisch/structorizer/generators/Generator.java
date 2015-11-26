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
 *      Kay Gürtzig     2015.10.18		File name proposal in exportCode(Root, File, Frame) delegated to Root
 *      Kay Gürtzig     2015.11.01		transform methods reorganised (KGU#18/KGU23) using subclassing
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
import java.util.regex.Matcher;

import javax.swing.*;

import com.stevesoft.pat.Regex;

import lu.fisch.utils.*;
import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.gui.ExportOptionDialoge;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.parsers.D7Parser;


public abstract class Generator extends javax.swing.filechooser.FileFilter
{
	/************ Fields ***********************/
	private ExportOptionDialoge eod = null;
	protected StringList code = new StringList();

	/************ Abstract Methods *************/
	protected abstract String getDialogTitle();
	protected abstract String getFileDescription();
	protected abstract String getIndent();
	protected abstract String[] getFileExtensions();
	// START KGU 2015-10-18: It seemed sensible to store the comment specification permanently
	/**
	 * @return left comment delimiter, e.g. "/*", "//", "(*", or "{"
	 */
	protected abstract String commentSymbolLeft();
	/**
	 * @return right comment delimiter if required, e.g. "*\/", "}", "*)"
	 */
	protected String commentSymbolRight() { return ""; }
	// END KGU 2015-10-18
	
	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/**
	 * A pattern how to embed the variable (right-hand side of an input instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "$1 = (new Scanner(System.in)).nextLine();"
	 */
	protected abstract String getInputReplacer();

	/**
	 * A pattern how to embed the expression (right-hand side of an output instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "System.out.println($1);"
	 */
	protected abstract String getOutputReplacer();
	// END KGU#18/KGU#23 2015-11-01
	
	/************ Code Generation **************/
	
	// KGU 2014-11-16: Method renamed (formerly: insertComment)
	// START KGU 2015-11-18: Method parameter list reduced by a comment symbol configuration
	/**
	 * Inserts the text of _element as comments into the code, using delimiters this.commentSymbolLeft
	 * and this.commentSymbolRight (if given) to enclose the comment lines, with indentation _indent 
	 * @param _element current NSD element
	 * @param _indent indentation string
	 */
	protected boolean insertAsComment(Element _element, String _indent)
	{
		if(eod.commentsCheckBox.isSelected()) {
			insertComment(_element.getText(), _indent);
			return true;
		}
		return false;
	}

	/**
	 * Inserts the comment part of _element into the code, using delimiters this.commentSymbolLeft
	 * and this.commentSymbolRight (if given) to enclose the comment lines, with indentation _indent
	 * @param _element current NSD element
	 * @param _indent indentation string
	 */
	protected void insertComment(Element _element, String _indent)
	{
		this.insertComment(_element.getComment(), _indent);
	}

	/**
	 * Inserts all lines of the given StringList as a series of single comment lines to the exported code
	 * @param _text - the text to be inserted as comment
	 * @param _indent - indentation string
	 */
	protected void insertComment(String _text, String _indent)
	{
		String[] lines = _text.split("\n");
		for (int i = 0; i < lines.length; i++)
		{
			code.add(_indent + commentSymbolLeft() + " " + lines[i] + " " + commentSymbolRight());
		}
	}

	/**
	 * Inserts all lines of the given StringList as a series of single comment lines to the exported code
	 * @param _sl - the text to be inserted as comment
	 * @param _indent - indentation string
	 */
	protected void insertComment(StringList _sl, String _indent)
	{
		for (int i = 0; i < _sl.count(); i++)
		{
        	// The following splitting is just to avoid empty comment lines and broken
        	// comment lines (though the latter shouldn't be possible here)
        	String commentLine = _sl.get(i);
        	// Skip an initial empty comment line
       		if (i > 0 || !commentLine.isEmpty()) {
       			insertComment(commentLine, _indent);
       		}
		}
	}
	
	protected void insertBlockComment(StringList _sl, String _indent, String _start, String _cont, String _end)
	{
		if (_start != null)
		{
			code.add(_indent + _start);
		}
		for (int i = 0; i < _sl.count(); i++)
		{
        	// The following splitting is just to avoid empty comment lines and broken
        	// comment lines (though the latter shouldn't be possible here)
        	String commentLine = _sl.get(i);
        	// Skip an initial empty comment line
       		if (i > 0 || !commentLine.isEmpty()) {
       			code.add(_indent + _cont + commentLine);
       		}
		}
		if (_end != null)
		{
			code.add(_indent + _end);
		}
	}
	// END KGU 2015-10-18

	/**
	 * Overridable general text transformation routine, performing the following steps:
	 * 1. Eliminates parser preference keywords listed below and unifies all operators
	 *    @see lu.fisch.Structorizer.elements.Element#unifyOperators(java.lang.String)
	 *         preAlt, preCase, preWhile, preRepeat,
	 *         postAlt, postCase, postWhile, postRepeat
	 * 2. Replaces assignments by a call of overridable method transformAssignment(String)
	 * 3. Transforms Input and Output lines according to regular replacement expressions defined
	 *    by getInputReplacer() and getOutPutReplacer, respectively. This is done by overridable
	 *    methods transformInput(String) and transformOutput(), respectively.
	 *    This is only done if _input starts with one of the configured Input and Output keywords 
	 * @param _input a line or the concatenated lines of an Element's text
	 * @return the transformed line (target language line)
	 */
	protected String transform(String _input)
	{
		return transform(_input, true);
	}

	/**
	 * Overridable general text transformation routine, performing the following steps:
	 * 1. Eliminates parser preference keywords listed below and unifies all operators
	 *    @see lu.fisch.Structorizer.elements.Element#unifyOperators(java.lang.String)
	 *         preAlt, preCase, preWhile, preRepeat,
	 *         postAlt, postCase, postWhile, postRepeat
	 * 2. Replaces assignments by a call of overridable method transformAssignment(String)
	 * 3. Transforms Input and Output lines if _doInput and/or _doOutput are true, respectively
	 *    This is only done if _input starts with one of the configured Input and Output keywords 
	 * @param _input - a line or the concatenated lines of an Element's text
	 * @param _doInputOutput - whether the third transforms are to be performed
	 * @return the transformed line (target language line)
	 */
	protected String transform(String _input, boolean _doInputOutput)
	{

		// General operator unification and dummy keyword elimination
		_input = Element.transformIntermediate(_input);

		// assignment transformation
		_input = transformAssignment(_input);

		if (_doInputOutput)
		{
			// input instruction transformation
			_input = transformInput(_input);

			// output instruction transformation
			_input = transformOutput(_input);
		}

		return _input.trim();
	}
	
	/**
	 * Transforms assignments in the given intermediate-language code line.
	 * OVERRIDE this! (Method just returns _interm without changes)
	 * @param _interm - a code line in intermediate syntax
	 * @return transformed string
	 */
	protected String transformAssignment(String _interm)
	{
		return _interm;
	}
	
	/**
	 * Detects whether the given code line starts with the configured input keystring
	 * and if so replaces it according to the regex pattern provided by getInputReplacer()
	 * @param _interm - a code line in intermediate syntax
	 * @return transformed input instruction or _interm unchanged
	 */
	protected String transformInput(String _interm)
	{
		String subst = getInputReplacer();
		// Between the input keyword and the variable name there MUST be some blank...
		String keyword = D7Parser.input.trim();
		if (!keyword.isEmpty() && _interm.startsWith(keyword))
		{
			String matcher = Matcher.quoteReplacement(keyword);
			if (Character.isJavaIdentifierPart(keyword.charAt(keyword.length()-1)))
			{
				matcher = matcher + "[ ]";
			}
			_interm = _interm.replaceFirst("^" + matcher + "(.*)", subst);
		}
		return _interm;
	}

	/**
	 * Detects whether the given code line starts with the configured output keystring
	 * and if so replaces it according to the regex pattern provided by getOutputReplacer()
	 * @param _interm - a code line in intermediate syntax
	 * @return transformed output instruction or _interm unchanged
	 */
	protected String transformOutput(String _interm)
	{
		String subst = getOutputReplacer();
		// Between the input keyword and the variable name there MUST be some blank...
		String keyword = D7Parser.output.trim();
		if (!keyword.isEmpty() && _interm.startsWith(keyword))
		{
			String matcher = Matcher.quoteReplacement(keyword);
			if (Character.isJavaIdentifierPart(keyword.charAt(keyword.length()-1)))
			{
				matcher = matcher + "[ ]";
			}
			_interm = _interm.replaceFirst("^" + matcher + "(.*)", subst);
		}
		return _interm;
	}
	// END KGU#18/KGU#23 2015-11-01

 	
    protected void generateCode(Instruction _inst, String _indent)
	{
            //
	}
	
	protected void generateCode(Alternative _alt, String _indent)
	{
		// code.add(_indent+"");
		generateCode(_alt.qTrue,_indent+this.getIndent());
		// code.add(_indent+"");
		generateCode(_alt.qFalse,_indent+this.getIndent());
		// code.add(_indent+"");
	}

	protected void generateCode(Case _case, String _indent)
	{
		// code.add(_indent+"");
		for(int i=0; i < _case.qs.size(); i++)
		{
			// code.add(_indent+"");
			generateCode((Subqueue) _case.qs.get(i), _indent+this.getIndent());
			// code.add(_indent+"");
		}
		// code.add(_indent+"");
	}

	protected void generateCode(For _for, String _indent)
	{
		// code.add(_indent+"");
		generateCode(_for.q, _indent + this.getIndent());
		// code.add(_indent+"");
	}

	protected void generateCode(While _while, String _indent)
	{
		// code.add(_indent+"");
		generateCode(_while.q, _indent + this.getIndent());
		// code.add(_indent+"");
	}

	protected void generateCode(Repeat _repeat, String _indent)
	{
		// code.add(_indent+"");
		generateCode(_repeat.q, _indent + this.getIndent());
		// code.add(_indent+"");
	}

	protected void generateCode(Forever _forever, String _indent)
	{
		// code.add(_indent+"");
		generateCode(_forever.q, _indent + this.getIndent());
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
			generateCode((Instruction) _ele, _indent);
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
			generateCode((For) _ele, _indent);
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
		for(int i=0;i<_subqueue.getSize();i++)
		{
			generateCode((Element) _subqueue.getElement(i),_indent);
		}
		// code.add(_indent+"");
	}

	/******** Public Methods *************/

	public String generateCode(Root _root, String _indent)
	{
		// code.add("");
		generateCode(_root.children,_indent+this.getIndent());
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
		// START KGU 2015-10-18: Root has got a mechanism for this!
//		String nsdName = _root.getText().get(0);
//		nsdName.replace(':', '_');
//		if(nsdName.indexOf(" (")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf(" ("));}
//		if(nsdName.indexOf("(")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf("("));}
		String nsdName = _root.getMethodName();
		// END KGU 2015-10-18
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
                            	// START KGU 2015-10-18: This didn't make much sense: Why first insert characters that will be replaced afterwards?
                            	// (And with possibly any such characters that had not been there for indentation!)
                                //    String code = BString.replace(generateCode(_root,"\t"),"\t",getIndent());
                            	String code = generateCode(_root, "");
                            	// END KGU 2015-10-18

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

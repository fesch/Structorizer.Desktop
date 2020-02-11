/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lu.fisch.structorizer.generators;

import java.awt.Frame;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import lu.fisch.structorizer.archivar.IRoutinePool;
import lu.fisch.structorizer.elements.Root;

/**
 * OBSOLETE APPROACH TO SOLVE CHARACTER ENCODING WITHIN A SPECIFIC GENERATOR
 * @author robertfisch
 */
public class OberonGeneratorUTF8 extends OberonGenerator
{
	public File exportCode(Root _root, File _currentDirectory, Frame frame, IRoutinePool _routinePool)
	{
		File exportDir = _currentDirectory;
		boolean saveIt = true;

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

		String filename = new String();


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
			exportDir = file.getParentFile();
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
                                    String code = generateCode(_root,"\t").replace("\t",getIndent());

                                    FileOutputStream fos = new FileOutputStream(filename);
                                    Writer out = new OutputStreamWriter(fos, "UTF-8");
                                    out.write(code);
                                    out.close();
                            }
                            catch(Exception e)
                            {
                                    JOptionPane.showOptionDialog(null,"Error while saving the file!\n"+e.getMessage(),"Error",JOptionPane.OK_OPTION,JOptionPane.ERROR_MESSAGE,null,null,null);
                            }
                        }
		}
		return exportDir;
	}
}

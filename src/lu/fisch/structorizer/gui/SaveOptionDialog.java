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

package lu.fisch.structorizer.gui;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Dialog for configuration of diagram saving options (enhancement #310)
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2016.12.15      First Issue for enh. #310
 *      Kay Gürtzig     2017.03.12      Enh. #372 (name attribute choosable)
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.io.LicFilter;
import lu.fisch.structorizer.locales.LangDialog;

/**
 * Dialog for the configuration of diagram saving options
 * @author Kay Gürtzig
 *
 */
@SuppressWarnings("serial")
public class SaveOptionDialog extends LangDialog implements ActionListener, WindowListener {
	
    public boolean goOn = false;
    private Frame frame;

	public SaveOptionDialog()
	{
	    initComponents();
	    setModal(true);
	}
	
	public SaveOptionDialog(Frame frame)
	{
		this.frame = frame;
	    initComponents();
	    setModal(true);
	    setLocationRelativeTo(frame);
	}

	/**
	 * This method is called from within the constructor to
	 * initialize the form.
	 */
	//@SuppressWarnings("unchecked")
	private void initComponents() {

	    pnlTop = new javax.swing.JPanel();
	    pnlButtons = new javax.swing.JPanel();
	    pnlOptions = new javax.swing.JPanel();
	    pnlWrapper = new javax.swing.JPanel();
	    pnlAutoSave = new javax.swing.JPanel();
	    pnlBackup = new javax.swing.JPanel();
		// START KGU#363 2017-03-12: Enh. #372 Author name field
		pnlFileInfo = new javax.swing.JPanel();
		// END KGU#363 2017-03-12
	    lbIntro = new javax.swing.JLabel();
	    btnOk = new javax.swing.JButton();

		chkAutoSaveExecute = new javax.swing.JCheckBox("Auto-save during execution?");
		chkAutoSaveClose  = new javax.swing.JCheckBox("Auto-save when going to be closed?");
		chkBackupFile = new javax.swing.JCheckBox("Create backup file on re-saving?") ;
		// START KGU#363 2017-03-12: Enh. #372 Author name field
		lblAuthorName = new javax.swing.JLabel("Author name");
		txtAuthorName = new javax.swing.JTextField();
		btnLicenseFile = new javax.swing.JButton("License file") ;
		btnLicenseFile.addActionListener(this);
		cbLicenseFile = new javax.swing.JComboBox<String>();
		cbLicenseFile.setEditable(true);
		// END KGU#363 2017-03-12

	    setTitle("Preferences for Saving ...");

	    org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(pnlTop);
	    pnlTop.setLayout(jPanel1Layout);
	    jPanel1Layout.setHorizontalGroup(
	        jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
	        .add(0, 0, Short.MAX_VALUE)
	    );
	    jPanel1Layout.setVerticalGroup(
	        jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
	        .add(0, 0, Short.MAX_VALUE)
	    );


	    lbIntro.setText("Please select the options you want to activate ...");

	    btnOk.setText("OK");
	    btnOk.addActionListener(this);

	    Container content = getContentPane();
	    content.setLayout(new BorderLayout());
	    
	    pnlTop.setLayout(new GridLayout(1,1,4,4));
	    pnlTop.setBorder(new EmptyBorder(12,12,0,12));
	    pnlTop.add(lbIntro);
	    
	    pnlAutoSave.setBorder(new TitledBorder("Auto-save options"));
	    pnlAutoSave.setLayout(new GridLayout(0, 1, 0, 1));
	    pnlAutoSave.add(this.chkAutoSaveExecute);
	    pnlAutoSave.add(this.chkAutoSaveClose);
	    
	    pnlBackup.setBorder(new TitledBorder("Backup options"));
	    pnlBackup.setLayout(new GridLayout(0, 1, 0, 1));
	    pnlBackup.add(this.chkBackupFile);
	    
		// START KGU#363 2017-03-12: Enh. #372 Author name field
		pnlFileInfo.setBorder(new TitledBorder("File info defaults"));
		pnlFileInfo.setLayout(new GridLayout(0, 2, 1, 1));
		pnlFileInfo.add(this.lblAuthorName);
		pnlFileInfo.add(this.txtAuthorName);
		pnlFileInfo.add(this.btnLicenseFile);
		pnlFileInfo.add(this.cbLicenseFile);
		// END KGU#363 2017-03-12

	    pnlOptions.setLayout(new GridLayout(0,1,4,4));
	    pnlOptions.setBorder(new EmptyBorder(12,12,12,12));
	    pnlOptions.add(pnlAutoSave, BorderLayout.CENTER);
	    pnlOptions.add(pnlBackup, BorderLayout.CENTER);
		// START KGU#363 2017-03-12: Enh. #372 Author name field
	    pnlOptions.add(pnlFileInfo, BorderLayout.CENTER);
		// END KGU#363 2017-03-12
	    //pnlOptions.add(pnlPreference, BorderLayout.CENTER);
	    
	    pnlButtons.setLayout(new BorderLayout());
	    pnlButtons.setBorder(new EmptyBorder(12,12,12,12));
	    pnlButtons.add(btnOk, BorderLayout.EAST);
	    
	    pnlWrapper.add(pnlOptions);
	    
	    content.add(pnlTop, BorderLayout.NORTH);
	    content.add(pnlWrapper, BorderLayout.CENTER);
	    content.add(pnlButtons, BorderLayout.SOUTH);

		try
		{
			File dir = getLicenseDirectory();
		    File[] licFiles = dir.listFiles(new LicFilter());
		    String prefix = LicFilter.getNamePrefix();
		    String ext = "." + LicFilter.acceptedExtension();
			for (File f: licFiles) {
				String fname = f.getName();
				this.cbLicenseFile.addItem(fname.substring(prefix.length(), fname.lastIndexOf(ext)));
			}
			
		} catch (Error e)
		{
			System.err.println("SaveOptionDialog: " + e.getMessage());
		} catch (Exception e)
		{
			System.err.println("SaveOptionDialog: " + e.getMessage());
		}

		pack();
	    
	    
	}
	
	/**
	 * Returns a File object for the license directory (which is identical to the
	 * ini directory)
	 * @return the File object of the directory for license files
	 */
	private File getLicenseDirectory()
	{
		return Ini.getIniDirectory();
	}


//    /**
//     * @param args the command line arguments
//     */
//    public static void main(String args[])
//    {
//        /* Set the Nimbus look and feel */
//        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
//         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
//         */
//        try
//        {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
//            {
//                if ("Nimbus".equals(info.getName()))
//                {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex)
//        {
//            java.util.logging.Logger.getLogger(SaveOptionDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex)
//        {
//            java.util.logging.Logger.getLogger(SaveOptionDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex)
//        {
//            java.util.logging.Logger.getLogger(SaveOptionDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex)
//        {
//            java.util.logging.Logger.getLogger(SaveOptionDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable()
//        {
//
//            public void run()
//            {
//                new SaveOptionDialog().setVisible(true);
//            }
//        });
//    }

    // Variables declaration
	private LicenseEditor editor = null;
	private javax.swing.JPanel pnlTop;
	private javax.swing.JPanel pnlButtons;
	private javax.swing.JPanel pnlOptions;
	private javax.swing.JPanel pnlWrapper;
	public javax.swing.JPanel pnlAutoSave;
	public javax.swing.JPanel pnlBackup;
	// START KGU#363 2017-03-12: Enh. #372 Author name field
	public javax.swing.JPanel pnlFileInfo;
	// END KGU#363 2017-03-12
	public javax.swing.JButton btnOk;
	public javax.swing.JLabel lbIntro;
	public javax.swing.JCheckBox chkAutoSaveExecute;
	public javax.swing.JCheckBox chkAutoSaveClose;
	public javax.swing.JCheckBox chkBackupFile;
	// START KGU#363 2017-03-12: Enh. #372 Author name field
	public javax.swing.JLabel lblAuthorName;
	public javax.swing.JTextField txtAuthorName;
	public javax.swing.JButton btnLicenseFile;
	public javax.swing.JComboBox<String> cbLicenseFile;
	public static LangTextHolder msgNoFile = new LangTextHolder("No file name selected or entered!");
	public static LangTextHolder msgCantEdit = new LangTextHolder("Cannot open an editor for the selected file!");
	// END KGU#363 2017-03-12

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == this.btnLicenseFile) {
			String prefix = LicFilter.getNamePrefix();
			String ext = "." + LicFilter.acceptedExtension();
			String licName = (String)this.cbLicenseFile.getSelectedItem();
			File licDir = this.getLicenseDirectory();
			if (licName == null || licName.trim().isEmpty()) {
				JOptionPane.showMessageDialog(this, msgNoFile.getText());
				return;
			}
			File licFile = new File(licDir.getAbsolutePath() + File.separator + prefix + licName + ext);
			try {
				licFile.createNewFile();
			} catch (IOException e) {
				System.out.println("SaveOptionDialog: " + e.getMessage());
			}
			editor = new LicenseEditor(frame, licFile);
			editor.addWindowListener(this);
			editor.setVisible(true);
		}
		else if (evt.getSource() == this.btnOk) {
	        goOn = true;
	        this.setVisible(false);
		}
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent evt) {
		Object source = evt.getSource();
		if (source instanceof LicenseEditor) {
			this.cbLicenseFile.setSelectedIndex(-1);
			this.cbLicenseFile.removeAllItems();
			try
			{
				File dir = getLicenseDirectory();
				File[] licFiles = dir.listFiles(new LicFilter());
				String prefix = LicFilter.getNamePrefix();
				String ext = "." + LicFilter.acceptedExtension();
				for (File f: licFiles) {
					String fname = f.getName();
					this.cbLicenseFile.addItem(fname.substring(prefix.length(), fname.lastIndexOf(ext)));
				}
			} catch (Error ex)
			{
				System.err.println("SaveOptionDialog: " + ex.getMessage());
			} catch (Exception ex)
			{
				System.err.println("SaveOptionDialog: " + ex.getMessage());
			}
			String licName = ((LicenseEditor)source).getLicenseName(true); 
			if (!licName.equals("???")) {
				this.cbLicenseFile.setSelectedItem(licName);
			}
		}
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent evt) {
		Object source = evt.getSource();
		//System.out.println(source + " opened...");
		if (source instanceof LicenseEditor) {
			LicenseEditor led = (LicenseEditor)source;
			led.setSize(LicenseEditor.PREFERRED_WIDTH, LicenseEditor.PREFERRED_HEIGHT);
		}
	}
	
}


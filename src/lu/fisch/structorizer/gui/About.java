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

/**
 ******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    This is the "about" dialog window.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007-12-29      First Issue
 *      Kay Gürtzig     2016-11-02      Issue #81: Scaling as workaround for lacking DPI awareness
 *      Kay Gürtzig     2016-11-09      Issue #81: Scale factor no longer rounded.
 *      Kay Gürtzig     2017-01-09      Bugfix #330: Scaling done by GUIScaler
 *      Kay Gürtzig     2018-03-21      Logger introduced, two file reading sequences extracted to method readTextFile()
 *      Kay Gürtzig     2018-07-30      Bugfix #571 - about -> license also showed the changelog.txt
 *      Kay Gürtzig     2018-10-08      Issue #620: a fourth tab "Paths" added.
 *      Kay Gürtzig     2019-08-01      Issue #733: Correct representation of the installation directory (URL to UTF-8)
 *      Kay Gürtzig     2019-08-07      Enh. #741: Paths tab now shows the complete ini file path, not just the dir path
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      - 2007.12.29 Robert Fisch
 *      	I used JFormDesigner to design this window graphically.
 *
 ******************************************************************************************************///

/*
 * Created by JFormDesigner on Sat Dec 29 21:36:58 CET 2007
 */

import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.locales.LangDialog;
import lu.fisch.structorizer.locales.LangEvent;
import lu.fisch.structorizer.locales.LangEventListener;
import lu.fisch.structorizer.locales.LangTextHolder;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import lu.fisch.structorizer.elements.*;

@SuppressWarnings("serial")
// START KGU#595/KGU#596 2018-10-08: Issue #620 - inheritance change fot more diagnostic transparency
//public class About extends LangDialog implements ActionListener, KeyListener
public class About extends LangDialog implements ActionListener, KeyListener, LangEventListener
// END KGU#595/KGU#596 2018-10-8
{
	// START KGU 2018-03-21
	private static final String CHANGELOG_FILE = "changelog.txt";
	private static final String LICENSE_FILE = "license.txt";
	public static final Logger logger = Logger.getLogger(About.class.getName());
	// END KGU 2018-03-21
	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Robert Fisch
	protected JPanel dialogPane;
	protected JPanel contentPanel;
	protected JPanel pnlLeft;
	protected JPanel pnlTop;
	protected JPanel panel2;
	protected JLabel lblName;
	protected JLabel lblVersion;
	protected JLabel label2;
	protected JTabbedPane pnlTabbed;
	protected JScrollPane scrollPane1;
	protected JTextPane txtThanks;
	protected JScrollPane scrollPane2;
	protected JTextPane txtChangelog;
	protected JScrollPane scrollPane3;
	protected JTextPane txtLicense;
	// START KGU#595 2018-10-08: Issue #620
	protected JScrollPane scrollPane4;
	protected JTextPane txtPaths;
	// START KGU#722 2019-08-07: Enh. #741
	//public LangTextHolder msgPaths = new LangTextHolder("Ini folder:\n%1\n\nLog folder:\n%2\n\nInstallation path:\n%3\n");
	public LangTextHolder msgPaths = new LangTextHolder("Ini file:\n%1\n\nLog folder:\n%2\n\nInstallation path:\n%3\n");
	// END KGU#722 2019-08-07
	// END KGU#595 2018-10-08
	protected JPanel buttonBar;
	protected JButton btnOK;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	
	public void create()
	{
		// set window title
		setTitle("About");
		// set layout (OS default)
		setLayout(null);
		// set windows size
		// START KGU#287 2016-11-02: Issue #81 (DPI awareness workaround)
		//setSize(650, 400);
		double scaleFactor = Double.valueOf(Ini.getInstance().getProperty("scaleFactor","1"));
		setSize((int)(650 * scaleFactor), (int)(400 * scaleFactor));
		// END KGU#287 2016-11-02
		// show form
		setVisible(false);
		// set action to perform if closed
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// set icon

		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Robert Fisch
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		pnlLeft = new JPanel();
		pnlTop = new JPanel();
		panel2 = new JPanel();
		lblName = new JLabel();
		lblVersion = new JLabel();
		label2 = new JLabel();
		pnlTabbed = new JTabbedPane();
		scrollPane1 = new JScrollPane();
		txtThanks = new JTextPane();
		scrollPane2 = new JScrollPane();
		scrollPane3 = new JScrollPane();
		txtChangelog = new JTextPane();
		txtLicense = new JTextPane();
		// START KGU#595 2018-10-08: Issue #620
		scrollPane4 = new JScrollPane();
		txtPaths = new JTextPane();
		// END KGU#595 2018-10-08
		buttonBar = new JPanel();
		btnOK = new JButton();

		txtChangelog.setFont(new Font("Courier",Font.PLAIN,(int)(10*scaleFactor)));
		txtLicense.setFont(new Font("Courier",Font.PLAIN,(int)(12*scaleFactor)));
		
		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		
		//======== dialogPane ========
		{
			int border = (int)(12 * scaleFactor);
			dialogPane.setBorder(new EmptyBorder(border, border, border, border));
			
			// JFormDesigner evaluation mark
			/*
			dialogPane.setBorder(new javax.swing.border.CompoundBorder(
																	   new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0),
																										   "JFormDesigner Evaluation", javax.swing.border.TitledBorder.CENTER,
																										   javax.swing.border.TitledBorder.BOTTOM, new java.awt.Font("Dialog", java.awt.Font.BOLD, 12),
																										   java.awt.Color.red), dialogPane.getBorder())); dialogPane.addPropertyChangeListener(new java.beans.PropertyChangeListener(){public void propertyChange(java.beans.PropertyChangeEvent e){if("border".equals(e.getPropertyName()))throw new RuntimeException();}});
			*/
			dialogPane.setLayout(new BorderLayout());
			
			//======== contentPanel ========
			{
				border = (int)(5 * scaleFactor);
				contentPanel.setLayout(new BorderLayout(border, border));
				
				//======== pnlLeft ========
				{
					pnlLeft.setLayout(new BorderLayout());
				}
				contentPanel.add(pnlLeft, BorderLayout.WEST);
				
				//======== pnlTop ========
				{
					pnlTop.setLayout(new BorderLayout());
					
					//======== panel2 ========
					{
						panel2.setLayout(new BorderLayout());
						
						//---- lblName ----
						lblName.setText("Structorizer");
						lblName.setFont(lblName.getFont().deriveFont(lblName.getFont().getStyle() | Font.BOLD, lblName.getFont().getSize() + 10f));
						panel2.add(lblName, BorderLayout.NORTH);
						
						//---- lblVersion ----
						lblVersion.setText("Version 3.00");
						panel2.add(lblVersion, BorderLayout.CENTER);
					}
					pnlTop.add(panel2, BorderLayout.CENTER);
					
					//---- label2 ----
					label2.setIcon(IconLoader.icoNSD48);
					border = (int)(8 * scaleFactor);
					label2.setBorder(BorderFactory.createEmptyBorder(border,border,border,border));
					pnlTop.add(label2, BorderLayout.WEST);
				}
				contentPanel.add(pnlTop, BorderLayout.NORTH);
				
				//======== pnlTabbed ========
				{
					//pnlTabbed.setSelectedIndex(0);
					pnlTabbed.setTabPlacement(SwingConstants.BOTTOM);
					
					//======== scrollPane1 ========
					{
						scrollPane1.setViewportView(txtThanks);
					}
					pnlTabbed.addTab("Implicated Persons", scrollPane1);
					
					
					//======== scrollPane2 ========
					{
						scrollPane2.setViewportView(txtChangelog);
					}
					pnlTabbed.addTab("Changelog", scrollPane2);

					//======== scrollPane3 ========
					{
						scrollPane3.setViewportView(txtLicense);
					}
					pnlTabbed.addTab("License", scrollPane3);

					// START KGU#595 2018-10-08: Issue #620
					//======== scrollPane4 ========
					{
						scrollPane4.setViewportView(txtPaths);
					}
					pnlTabbed.addTab("Paths", scrollPane4);
					// END KGU#595 2018-10-08
				}
				contentPanel.add(pnlTabbed, BorderLayout.CENTER);
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);
			
			//======== buttonBar ========
			{
				buttonBar.setBorder(new EmptyBorder((int)(12 * scaleFactor), 0, 0, 0));
				buttonBar.setLayout(new GridBagLayout());
				((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, (int)(80*scaleFactor)};
				((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0};
				
				//---- okButton ----
				btnOK.setText("OK");
				btnOK.addActionListener(this);
				buttonBar.add(btnOK, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
															   GridBagConstraints.CENTER, GridBagConstraints.BOTH,
															   new Insets(0, 0, 0, 0), 0, 0));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);

        // START KGU#287 2017-01-09: Issues #81/#330 GUI scaling
        GUIScaler.rescaleComponents(this);
        // END KGU#287 2017-01-09
        		
		//pack();	// Don't pack here (would minimize all tabs)!
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents

		btnOK.addKeyListener(this);
		txtThanks.addKeyListener(this);
		txtChangelog.addKeyListener(this);
		txtLicense.addKeyListener(this);
		// START KGU#595 2018-10-08: Issue #620
		txtPaths.addKeyListener(this);
		// END KGU#595 2018-10-08
		pnlTabbed.addKeyListener(this);
		addKeyListener(this);
		
		txtThanks.setEditable(false);
		txtChangelog.setEditable(false);
		txtLicense.setEditable(false);
		// START KGU#595 2018-10-08: Issue #620
		txtPaths.setEditable(false);
		// END KGU#595 2018-10-08
		
		
		txtChangelog.setText(readTextFile(CHANGELOG_FILE));

		txtLicense.setText(readTextFile(LICENSE_FILE));

		txtThanks.setText(Element.E_THANKS);
		txtThanks.setCaretPosition(0);
		
		// START KGU#595 2018-10-08: Issue #620
		updatePaths();
		msgPaths.addLangEventListener(this);
		// END KGU#595 2018-10-08
		
		//txtChangelog.setText(Element.E_CHANGELOG);
		txtChangelog.setCaretPosition(0);

		txtLicense.setCaretPosition(0);
		
		lblVersion.setText("Version "+Element.E_VERSION);
		
		// KGU 2018-08-01 Failed attempt to force flushing of the log file
//		Logger lgr = logger;
//		lgr.log(Level.INFO, "Trying to flush this now...");
//		while (lgr != null && lgr.getHandlers().length == 0) {
//			lgr = lgr.getParent();
//			System.out.print("^");
//		}
//		System.out.println("");
//		if (lgr != null) {
//			for (Handler hdlr: lgr.getHandlers()) {
//				System.out.println("Flushing " + hdlr + " ...");
//				hdlr.flush();
//			}
//		}
//		while (lgr != null) {
//			lgr = lgr.getParent();
//			if (lgr != null) {
//				if (lgr.getHandlers().length > 0) {
//					System.out.print("#");
//				}
//				else {
//					System.out.print("^");
//				}
//			}
//		}
	}

	// START KGU#595 2018-10-08: Issue #620 - more transparency for diagnosis
	/**
	 * Sets (or updates) the content of {@link #txtPaths}.
	 */
	private void updatePaths() {
		File prodDir = Ini.getInstallDirectory();
		txtPaths.setText(msgPaths.getText().
				// START KGU#722 2019-08-07: Enh. #741
				//replace("%1", Ini.getIniDirectory().getAbsolutePath()).
				replace("%1", Ini.getInstance().getIniFile().getAbsolutePath()).
				// END KGU#722 2019-08-07
				replace("%2", new File(System.getProperty("java.util.logging.config.file", "???")).getParent()).
				replace("%3", prodDir.getAbsolutePath()));
		txtPaths.setCaretPosition(0);
	}
	// END KGU#595 2018-10-08

	/**
	 * Reads the file given by the resource name {@code fileName} and returns its
	 * text content as string.
	 * @param fileName - the relative (local) name of the text file
	 * @return the text content if any
	 */
	private String readTextFile(String fileName) {
		StringBuilder input = new StringBuilder();
		BufferedReader in = null;
		try
		{
			// KGU#567 2018-07-30: Bugfix #571 - argument has to be used instead of hard-coded name.
			in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(fileName), "UTF8"));
			String str;
			while ((str = in.readLine()) != null)
			{
				input.append(str + "\n");
			}
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "Cannot read " + fileName, e);
		}
		finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {}
		}
		return input.toString();
	}
	
	// listen to actions
	public void actionPerformed(ActionEvent event)
	{
		setVisible(false);
	}
	
	public void keyTyped(KeyEvent kevt) 
	{
    }
	
	public void keyPressed(KeyEvent e) 
	{
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			setVisible(false);
		}
		else if(e.getKeyCode() == KeyEvent.VK_ENTER && (e.isShiftDown() || e.isControlDown()))
		{
			setVisible(false);
		}
    }
	
	public void keyReleased(KeyEvent ke)
	{
	} 
	

	public About(Frame owner)
	{
		super(owner);
		setPacking(false);
		this.setModal(true);
		create();
	}

	// START KGU#595/KGU#596 2018-10-08: Issue #620
	@Override
	public void LangChanged(LangEvent evt) {
		if (evt.getSource() == msgPaths) {
			updatePaths();
		}
	}
	// END KGU#595/KGU#596 2018-10-08

}

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
 *      Author:         Bob Fisch
 *
 *      Description:    This is the font chooser dialog.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007-12-31      First Issue
 *      Kay Gürtzig     2016-10-11      Minimum font size 2 dropped from the sizes array.
 *      Kay Gürtzig     2016-11-02      Issue #81: Scaling Factor considered (DPI awareness workarond)
 *      Kay Gürtzig     2016-11-09      Issue #81: Scaling factor no longer rounded, ensured to be >= 1
 *      Kay Gürtzig     2017-05-09      Issue #400: commit field OK introduced, keyListener at all controls
 *      Kay Gürtzig     2018-09-10      Issue #508: displays current size in label if not in list, re-pack after font change
 *      Kay Gürtzig     2018-12-19      Bugfix #650: Fix #508 had to be revised for Java functionality changes
 *      Kay Gürtzig     2021-01-26      Issue #400: keyListener applied to cbFixPadding
 *
 ******************************************************************************************************
 *
 *      Comment:		While setting up this class, I had a deep look at the following package:
 *
 * 		*********************************************************	
 *		* Package: ZoeloeSoft.projects.JFontChooser		
 *		* Id: JFontChooser.java		 	
 * 		* Date: 23:39 19/04/2004	
 *	 	* Creator: Tim Eeckhaut				
 *		* Alias: zoeloeboeloe					
 * 		* Company: ZoeloeSoft				
 *	 	* Website: http://users.pandora.be/ZoeloeSoft	
 *		*********************************************************		
 *
 ******************************************************************************************************///


import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.locales.LangDialog;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/*
 * Created by JFormDesigner on Mon Dec 31 12:44:23 CET 2007
 * No longer maintained with JFormDesigner, but manually!
 */

/**
 * @author Robert Fisch
 */
@SuppressWarnings("serial")
public class FontChooser extends LangDialog
{
	// START KGU#393 2017-05-09: Issue #400 - indicate whether changes are committed
	public boolean OK = false;
	// END KGU#393 2017-05-09
	
	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Robert Fisch
	protected JPanel pnlChooser;
	protected JPanel contentPanel;
	protected JLabel lblTest;
	protected JPanel pnlName;
	protected JLabel lblName;
	protected JScrollPane scrollPane1;
	protected JList<String> lsNames;
	protected JPanel pnlSize;
	protected JLabel lblSize;
	// START KGU#494 2018-09-10: Issue #508
	protected JLabel lblSizeValue;
	// END KGU#494 2018-09-10
	protected JScrollPane scrollPane2;
	protected JList<String> lsSizes;
	protected JPanel buttonBar;
	protected JButton btnOK;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	// START KGU#494 2018-09-10: Issue #508
	protected JCheckBox cbFixPadding;
	protected int fontSize = 12;	// Caches a non-selectable font size
	private boolean offerPadding = false; 
	// END KGU#494 2018-09-10
	
	private String[] sizes = new String[] { "4","6","8","10","12","14","16","18","20","22","24","30","36","48","72" };

	/*
	public FontChooser() {
		super();
		setModal(true);
		initComponents();
	}*/
	
	public FontChooser(Frame owner) {
		this(owner, false);
	}

	public FontChooser(Frame owner, boolean withPaddingControl) {
		super(owner);
		offerPadding = withPaddingControl;
		setModal(true);
		initComponents();
	}
	
	/*public FontChooser(Dialog owner) {
		super(owner);
		initComponents();
	}*/

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Robert Fisch
		pnlChooser = new JPanel();
		contentPanel = new JPanel();
		lblTest = new JLabel();
		pnlName = new JPanel();
		lblName = new JLabel();
		scrollPane1 = new JScrollPane();
		lsNames = new JList<String>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
		pnlSize = new JPanel();
		lblSize = new JLabel();
		// START KGU#494 2018-09-10: Issue #508
		lblSizeValue = new JLabel();
		// END KGU#494 2018-09-10
		
		scrollPane2 = new JScrollPane();
		lsSizes = new JList<String>(sizes);
		buttonBar = new JPanel();
		btnOK = new JButton();
		
		// START KGU#287 2016-11-02: Issue #81 (DPI awareness workaround)
		double scaleFactor = Double.valueOf(Ini.getInstance().getProperty("scaleFactor","1"));
		if (scaleFactor < 1) scaleFactor = 1.0;
		// END KGU#287 2016-11-02

		//======== fontChooser ========
		{
			this.setResizable(false);	// This also suppresses the dialog icon holding the size menu
			this.setTitle("Font");
			Container fontChooserContentPane = getContentPane();
			fontChooserContentPane.setLayout(new BorderLayout());

			//======== FontChooser ========
			{
				
				// START KGU#287 2016-11-02: Issue #81 (DPI awareness workaround)
				//pnlChooser.setBorder(new EmptyBorder(12, 12, 12, 12));
				int border = (int)(12 * scaleFactor);
				pnlChooser.setBorder(new EmptyBorder(border, border, border, border));
				// END KGU#287 2016-11-02

				// JFormDesigner evaluation mark
				/*FontChooser.setBorder(new javax.swing.border.CompoundBorder(
					new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0),
						"JFormDesigner Evaluation", javax.swing.border.TitledBorder.CENTER,
						javax.swing.border.TitledBorder.BOTTOM, new java.awt.Font("Dialog", java.awt.Font.BOLD, 12),
						java.awt.Color.red), FontChooser.getBorder())); FontChooser.addPropertyChangeListener(new java.beans.PropertyChangeListener(){public void propertyChange(java.beans.PropertyChangeEvent e){if("border".equals(e.getPropertyName()))throw new RuntimeException();}});
				 */
				pnlChooser.setLayout(new BorderLayout());

				//======== contentPanel ========
				{
					// START KGU#287 2016-11-02: Issue #81 (DPI awareness workaround)
					//contentPanel.setLayout(new BorderLayout(8, 8));
					border = (int)(8 * scaleFactor);
					contentPanel.setLayout(new BorderLayout(border, border));
					// END KGU#287 2016-11-02

					//---- lblTest ----
					lblTest.setText("Test: Structorizer (symbols: [\u2190 - \u2205 - \u2260 - \u2264 - \u2265])");
					contentPanel.add(lblTest, BorderLayout.SOUTH);

					//======== pnlName ========
					{
						pnlName.setPreferredSize(new Dimension((int)(250*scaleFactor), (int)(150*scaleFactor)));
						pnlName.setLayout(new BorderLayout(border, border));

						//---- lblName ----
						lblName.setText("Name");
						pnlName.add(lblName, BorderLayout.NORTH);

						//======== scrollPane1 ========
						{
							scrollPane1.setViewportView(lsNames);
						}
						pnlName.add(scrollPane1, BorderLayout.CENTER);
					}
					contentPanel.add(pnlName, BorderLayout.WEST);

					//======== pnlSize ========
					{
						pnlSize.setPreferredSize(new Dimension((int)(100*scaleFactor), (int)(150*scaleFactor)));
						pnlSize.setLayout(new BorderLayout(border, border));

						//---- lblSize ----
						lblSize.setText("Size");
						// START KGU#494 2018-09-10: Issue #508
						//pnlSize.add(lblSize, BorderLayout.NORTH);
						JPanel pnlSizeSub = new JPanel();
						pnlSizeSub.setLayout(new BorderLayout());
						pnlSizeSub.add(lblSize, BorderLayout.LINE_START);
						pnlSizeSub.add(lblSizeValue, BorderLayout.CENTER);
						pnlSize.add(pnlSizeSub, BorderLayout.NORTH);
						// END KGU#494 2018-09-10

						//======== scrollPane2 ========
						{
							scrollPane2.setViewportView(lsSizes);
						}
						pnlSize.add(scrollPane2, BorderLayout.CENTER);
					}
					contentPanel.add(pnlSize, BorderLayout.CENTER);
				}
				pnlChooser.add(contentPanel, BorderLayout.CENTER);

				//======== buttonBar ========
				{
					// START KGU#287 2016-11-02: Issue #81 (DPI awareness workaround)
					//buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
					buttonBar.setBorder(new EmptyBorder((int)(12*scaleFactor), 0, 0, 0));
					// END KGU#287 2016-11-02
					buttonBar.setLayout(new GridBagLayout());
					((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, (int)(80*scaleFactor)};
					((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0};

					// START KGU#494 2018-09-10: Issue #508 - Allow fix padding (legacy mode)
					//---- cbFixPadding ----
					cbFixPadding = new JCheckBox("Fixed (font-independent) padding");
					if (offerPadding) {
						buttonBar.add(cbFixPadding, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 0, 0), 0, 0));
					}
					// END KGU#494 2018-09-10
					//---- btnOK ----
					btnOK.setText("OK");
					buttonBar.add(btnOK, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
					// START KGU#287 2017-01-09: Issues #81/#330 GUI scaling
					GUIScaler.rescaleComponents(buttonBar);
					// END KGU#287 2017-01-09
				}
				pnlChooser.add(buttonBar, BorderLayout.SOUTH);
			}
			fontChooserContentPane.add(pnlChooser, BorderLayout.CENTER);
			
			this.pack();
			this.setLocationRelativeTo(this.getOwner());
		}
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
		
		// BOB thinks
		
		// add the LIST-listeners
		ListSelectionListener listListener = new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{ 
				Font font = getCurrentFont();
				lblTest.setFont(font); 
				// START KGU#494 2018-09-10: Issue #508 (symbol test could become invisible on enlarging the font)
				if (lsSizes.getSelectedIndex() < 0) {
					lblSizeValue.setText(" (" + font.getSize() + ") ");
				}
				else {
					lblSizeValue.setText(" ");
				}
				pack();
				// END KGU#494 2018-09-10
			}
		};
		lsSizes.addListSelectionListener(listListener);
		lsNames.addListSelectionListener(listListener);
		
		// add the KEY-listeners
		KeyListener keyListener = new KeyListener()
		{
			public void keyPressed(KeyEvent e) 
			{
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					setVisible(false);
				}
				else if(e.getKeyCode() == KeyEvent.VK_ENTER && (e.isShiftDown() || e.isControlDown()))
				{
					// START KGU#393 2017-05-09: Issue #400
					OK = true;
					// END KGU#393 2017-05-09		
					setVisible(false);
				}
			}
			
			public void keyReleased(KeyEvent ke) {} 
			public void keyTyped(KeyEvent kevt) {}
		};
		lsSizes.addKeyListener(keyListener);
		lsNames.addKeyListener(keyListener);
		btnOK.addKeyListener(keyListener);
		// START KGU#393/KGU#494 2021-01-26: Issues #400, #508
		cbFixPadding.addKeyListener(keyListener);
		// END KGU#393/KGU#494 2021-01-26
		
		// add the ACTION-listeners
		ActionListener actionListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				// START KGU#393 2017-05-09: Issue #400
				OK = true;
				// END KGU#393 2017-05-09		
				setVisible(false);
			}
		};
		btnOK.addActionListener(actionListener);
	}
	
	public Font getCurrentFont()
	{	
		// Provide default font (Helvetica 12 is usually both available and suited)
		// START KGU#494 2018-09-10: Issue #508
		//Font font = new Font("Helvetica", Font.PLAIN, 12);
		Font font = new Font("Helvetica", Font.PLAIN, fontSize);
		// END KGU#494 2018-09-10
		
		try
		{
			String fontFamily = (String)lsNames.getSelectedValue();
			// START KGU#494 2018-09-10: Issue #508	We don't want to lose the former size if selection is void
			//int fontSize = Integer.parseInt((String)lsSizes.getSelectedValue());
			if (lsSizes.getSelectedIndex() >= 0) {
				fontSize = Integer.parseInt((String)lsSizes.getSelectedValue());
			}
			// END KGU#494 2018-09-10
			
			int fontType = Font.PLAIN;
			
			//if (cbBold.isSelected()) fontType += Font.BOLD;
			//if (cbItalic.isSelected()) fontType += Font.ITALIC;
			
			font = new Font(fontFamily, fontType, fontSize);		
		}
		catch (Exception e) 
		{
		}
	
		return font;
	}
	
	public void setFont(Font font)
	{
		if (font == null) font = lblTest.getFont();
		
		lsNames.setSelectedValue(font.getName(), true);
		lsNames.ensureIndexIsVisible(lsNames.getSelectedIndex());
		// START KGU#494 2018-12-19: Issue #508 (previous workaround doesn't work anymore for Java > 1.8)
		//lsSizes.setSelectedValue("" + font.getSize(), true);
		// Ensure the current font size is in the item list
		int currentFontSize = font.getSize();
		int indexToInsert = sizes.length;
		for (int i = 0; i < sizes.length; i++) {
			int itemVal = Integer.parseInt(sizes[i]);
			if (currentFontSize == itemVal) {
				indexToInsert = -1;
				break;
			}
			else if (currentFontSize < Integer.parseInt(sizes[i])) {
				indexToInsert = i;
				break;
			}
		}
		if (indexToInsert >= 0) {
			String[] newSizes = new String[sizes.length+1];
			for (int j = 0; j < indexToInsert; j++) {
				newSizes[j] = sizes[j];
			}
			newSizes[indexToInsert] = Integer.toString(currentFontSize);
			for (int j = indexToInsert; j < sizes.length; j++) {
				newSizes[j+1] = sizes[j];
			}
			lsSizes.setListData(newSizes);
		}
		lsSizes.setSelectedValue("" + currentFontSize, true);
		// END KGU#494 2018-12-19
		lsSizes.ensureIndexIsVisible(lsSizes.getSelectedIndex());
		// START KGU#619 2018-12-19: Bugfix #650 - Old workaround should no longer be necessary
//		// START KGU#494 2018-09-10: Issue #508 (font size is not necessarily in the choice list)
//		if (lsSizes.getSelectedIndex() < 0) {
//			// (in this case the listSelectionListener won't work)
//			fontSize = currentFontSize;
//			lblSizeValue.setText(" (" + fontSize + ") ");
//			lblTest.setFont(font);
//			pack();
//		}
//		// END KGU#494 2018-09-10
		// END KGU#619 2018-12-19
		
		//cbBold.setSelected(font.isBold());
		//cbItalic.setSelected(font.isItalic());
	}
	
	// START KGU#494 2018-09-10: Issue #508
	public boolean getFixPadding()
	{
		return this.cbFixPadding.isSelected();
	}

	public void setFixPadding(boolean fixPadding)
	{
		this.cbFixPadding.setSelected(fixPadding);
	}
	// END KGU#494 2018-09-10
	
}

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
 *      Description:    Class providing static methods for GUI scaling.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.01.09      First Issue
 *      Kay Gürtzig     2017.10.15      Scaling for JTree rows added
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.table.JTableHeader;

import lu.fisch.structorizer.io.Ini;

/**
 * @author kay
 *
 */
public class GUIScaler {
	
	// Order of the ToggleButton icons cached here is:
	// 0 - unselected CheckBox icon
	// 1 - selected CheckBox icon
	// 2 - unselected RadioButton icon
	// 3 - selected RadioButton icon
	private static ImageIcon[] toggleIcons = new ImageIcon[]{null, null, null, null};
	private static LookAndFeelInfo[] lafis = UIManager.getInstalledLookAndFeels();
	private static LookAndFeel laf = null;
	
	public static String getSizeVariant(double scaleFactor)
	{
		String sizeVariant = null;
		if (scaleFactor < 0.6) {
			sizeVariant = "mini";
		}
		else if (scaleFactor < 1.0) {
			sizeVariant = "small";
		}
		else if (scaleFactor > 1.0 && scaleFactor < 2.0) {
			sizeVariant = "large";
		}
		return sizeVariant;
	}
	
	public static void rescaleComponents(Container cont)
	{
		double scale = Double.parseDouble(Ini.getInstance().getProperty("scaleFactor", "1"));
		String sizeVariant = getSizeVariant(scale);
		if (sizeVariant != null) {
			rescaleComponents(cont, sizeVariant);
		}
		else if (scale >= 2.0) {
			rescaleComponents(cont, scale);
		}
	}
	
	private static void rescaleComponents(Container cont, String sizeVariant)
	{
		if (cont instanceof JComponent) {
//			System.out.println(cont + " --> " + sizeVariant);
			((JComponent)cont).putClientProperty("JComponent.sizeVariant", sizeVariant);
			if (cont instanceof JMenu) {
//				System.out.println("===============================================");
//				System.out.println(((JMenu)cont).getText());
//				System.out.println("===============================================");
				for (int i = 0; i < ((JMenu)cont).getItemCount(); i++) {
					rescaleComponents((Container)((JMenu)cont).getItem(i), sizeVariant);
				}
			}
			for (Component comp: ((Container) cont).getComponents()) {
				rescaleComponents((Container)comp, sizeVariant);
			}
		}
	}

	private static void rescaleComponents(Container cont, double scaleFactor)
	{
		laf = UIManager.getLookAndFeel();
		String otherLaf = null;
		boolean isNimbus = laf != null && laf.getName().equalsIgnoreCase("nimbus");
		if (isNimbus) {
			for (LookAndFeelInfo lafi: lafis) {
				if (!lafi.getName().equalsIgnoreCase("nimbus")) {
					// Any other than "Nimbus" would do if "Metal" isn't available
					otherLaf = lafi.getClassName();
					if (lafi.getName().equalsIgnoreCase("metal")) {
						// Metal is preferred for the Checkbox icon thing
						break;
					}
				}
			}
		}
		rescaleComponents(cont, scaleFactor, otherLaf);
	}

	private static void rescaleComponents(Container cont, double scaleFactor, String alternativeLaf)
	{
		for (Component comp: cont.getComponents()) {
			// KGU#287 2017-01-07: Bugfix #330: With L&F "Nimbus" the icon replacement would hide selected status
			// (it seems to suppress the standard Painters of the L&F)
			if (comp instanceof JToggleButton /*comp instanceof JCheckBox || comp instanceof JRadioButton*/) {
				if (alternativeLaf != null) {
					comp.setFont(UIManager.getFont("CheckBox.font"));
					try {
						UIManager.setLookAndFeel(alternativeLaf);
					} catch (Exception e) {
						System.err.println("GUIScaler.scaleComponents(cont, " + scaleFactor + ", " + alternativeLaf + "): " + e.toString());
					}
				}
				try {
					((JToggleButton)comp).setIcon(scaleToggleIcon((JToggleButton)comp, false));
					((JToggleButton)comp).setSelectedIcon(scaleToggleIcon((JToggleButton)comp, true));
				}
				catch (Exception ex) {
					System.err.println(ex);
					ex.printStackTrace(System.err);
				}
				if (alternativeLaf != null) {
					try {
						UIManager.setLookAndFeel(laf);
					} catch (Exception e) {
						System.err.println("GUIScaler.scaleComponents(): " + e.toString());
					}
				}
			}
			// START KGU#324 2017-10-15: Enh. #415
			else if (comp instanceof JTree && scaleFactor > 1) {
				((JTree)comp).setRowHeight((int)(((JTree)comp).getRowHeight() * scaleFactor));
			}
			// END KGU#324 2017-10-15
			else if (alternativeLaf != null) {
				if (comp instanceof JLabel) {
					comp.setFont(UIManager.getFont("Label.font"));
				}
				else if (comp instanceof JButton) {
					comp.setFont(UIManager.getFont("Button.font"));
				}
				else if (comp instanceof JTabbedPane) {
					comp.setFont(UIManager.getFont("TabbedPane.font"));	
				}
				else if (comp instanceof JTableHeader) {
					comp.setFont(UIManager.getFont("TableHeader.font"));	
				}
			}
			// FIXME: We also need a solution for JCheckBoxMenuItems...
			if (comp instanceof Container) {
				rescaleComponents((Container)comp, scaleFactor, alternativeLaf);
			}
		}
	}

    // START KGU#287 2016-11-11: Issue #81 (DPI-awareness workaround)
    /**
     * Returns a scaled checkbox icon for JCheckbox/JRadioButton/JTogleButton
     * toggleButton in mode given by selected (checked or unchecked)
     * @param toggleButton - the toggle button the icon is requested for
     * @param selected - the kind of icon (true = selected, false = unselected)
     * @return an ImageIcon scaled to the font size of the checkbox or the scale factor
     */
    static ImageIcon scaleToggleIcon(JToggleButton toggleButton, boolean selected)
    {
    	return scaleToggleIcon(toggleButton, selected, true);
    }
    // END KGU#287-11-11
    
    // START KGU#287 2017-01-11: Issue #81 (DPI-awareness workaround)
    /**
     * Returns a scaled checkbox icon for JCheckbox/JRadioButton/JTogleButton
     * toggleButton in mode given by selected (checked or unchecked)
     * @param toggleButton - the toggle button the icon is requested for
     * @param selected - the kind of icon (true = selected, false = unselected)
     * @param useCache - whether an internal toggleIcon cache is to be used or not
     * @return an ImageIcon scaled to the font size of the checkbox or the scale factor
     */
    static ImageIcon scaleToggleIcon(JToggleButton toggleButton, boolean selected, boolean useCache)
        {
    	final String[] propertyNames = new String[]{"CheckBox", "RadioButton", "ToggleButton"};
    	
    	int buttonType = 2;	// JToggleButton
    	if (toggleButton instanceof JCheckBox) {
    		buttonType = 0;
    	}
    	else if (toggleButton instanceof JRadioButton) {
    		buttonType = 1;
    	}
		Icon boxIcon = selected ? toggleButton.getSelectedIcon() : toggleButton.getIcon();
		int iconIndex = buttonType * 2 + (selected ? 1 : 0);
		boolean hadBoxIcon = boxIcon != null;
    	if (useCache && iconIndex < toggleIcons.length && toggleIcons[iconIndex] != null) {
    		return toggleIcons[iconIndex];	// Return pre-computed icon
    	}
    	
    	boolean prevState = toggleButton.isSelected();
    	FontMetrics boxFontMetrics = toggleButton.getFontMetrics(toggleButton.getFont());
    	if (boxIcon == null) {
    		if (buttonType < 2) {
    			String propertyName = propertyNames[buttonType];
    			boxIcon = UIManager.getIcon(propertyName + ".icon");
    		}
    		else if (selected) {
    			boxIcon = toggleButton.getIcon();
    			hadBoxIcon = boxIcon != null;
    		}
    	}
    	int type = BufferedImage.TYPE_INT_ARGB;
    	BufferedImage boxImage = new BufferedImage(
    			boxIcon.getIconWidth(), boxIcon.getIconHeight(), type);
    	Graphics2D g2 = boxImage.createGraphics();

    	// In case of a RadioButton we cannot temporarily unselect it to paint
    	// the unselected RadioButton image. We would rather have to select another
    	// RadioButtobof the same group to achieve this. But it's difficult to
    	// identify the ButtonGroup. So we just look for another RadioButton with
    	// the needed selection state underthe same parent as a surrogate.
		JRadioButton surrogate = null;
		if (toggleButton instanceof JRadioButton) {
			if (prevState == selected) {
				// It may be its own surrogate
				surrogate = (JRadioButton)toggleButton;
			}
			else {
				Container parent = toggleButton.getParent();
				for (Component cmp: parent.getComponents()) {
					if (cmp instanceof JRadioButton && ((JToggleButton)cmp).isSelected() == selected) {
						// Okay, we found a suited peer
						surrogate = (JRadioButton)cmp;
						break;
					}
				}
			}
		}
		if (surrogate == null) {
    		toggleButton.setSelected(selected);
		}

		try {
    		boxIcon.paintIcon(((surrogate != null) ? surrogate : toggleButton), g2, 0, 0);
    	}
    	finally {
    		g2.dispose();
    	}
    	ImageIcon newBoxIcon = new ImageIcon(boxImage);
    	Image finalBoxImage = newBoxIcon.getImage().getScaledInstance(
    			boxFontMetrics.getHeight(), boxFontMetrics.getHeight(), Image.SCALE_SMOOTH
    			);
    	if (surrogate == null) {
    		toggleButton.setSelected(prevState);
    	}
    	
    	ImageIcon scaledIcon = new ImageIcon(finalBoxImage);

    	if (useCache && !hadBoxIcon && iconIndex < toggleIcons.length) {
    		toggleIcons[iconIndex] = scaledIcon;
    	}
    	return scaledIcon;
    }
    // END KGU#287 2017-01-11

	// START KGU#287 2016-11-01: Issue #81 (DPI awareness, 2017-01-09 moved hitherto from Mainform)
	static void scaleDefaultFontSize(double factor) {

		// START KGU#287 2017-01-09: Bugfix #330
		//if (factor <= 1) return;
		if (factor < 2.0) return;	// In this case a sizeVariant will be used
		// END KGU#287 2017-01-09
		
		Set<Object> keySet = UIManager.getLookAndFeelDefaults().keySet();
		Object[] keys = keySet.toArray(new Object[keySet.size()]);

		for (Object key : keys) {

			if (key != null && key.toString().toLowerCase().contains("font")) {

				Font font = UIManager.getDefaults().getFont(key);
				if (font != null) {
					int size = font.getSize();
					//System.out.println(key + ": " + size);
					// Vague attempt to prevent the font size being exponentially enlarged by repeated calls...
					if (size < 18 && factor > 1) {
						font = font.deriveFont((float)(size * factor));
						UIManager.put(key, font);
					}
				}

			}

		}

	}
	// END KGU#287 2016-11-01
	

}

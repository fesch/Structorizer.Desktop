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
 *      Kay Gürtzig     2017-01-09      First Issue to address #81
 *      Kay Gürtzig     2017-10-15      Scaling for JTree rows added
 *      Kay Gürtzig     2018-03-21      Console output replaced with logging mechanism
 *      Kay Gürtzig     2019-02-06      Issue #670: JTree row height fix for sizeVariant
 *      Kay Gürtzig     2020-12-28      Issue #895: Turtleizer popup menu hadn't properly reacted with Nimbus
 *      Kay Gürtzig     2021-01-27      New static method getScreenScale()
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
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
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
 * Helper class for the DPI awareness workaround, provides static methods for GUI scaling
 * @author Kay Gürtzig
 */
public class GUIScaler {
	
	// START KGU#287 2021-01-27: Issue #81
	private static Float screenScale = null;
	// END KGU#287 2021-01-27
	
	// START KGU#484 2018-03-21: Issue #463
	public static final Logger logger = Logger.getLogger(GUIScaler.class.getName());
	// END KGU#484 2018-03-21
	/**
	 * Order of the ToggleButton icons cached here is:<br/>
	 * 0 - unselected CheckBox icon<br/>
	 * 1 - selected CheckBox icon<br/>
	 * 2 - unselected RadioButton icon<br/>
	 * 3 - selected RadioButton icon
	 */
	private static ImageIcon[] toggleIcons = new ImageIcon[]{null, null, null, null};
	/** Array of info records for all installed Look & Feel variants */ 
	private static LookAndFeelInfo[] lafis = UIManager.getInstalledLookAndFeels();
	/** Currently selected Look & Feel */
	private static LookAndFeel laf = null;
	
	/**
	 * Tries to derive a size variant string roughly corresponding to the {@code scaleFactor}
	 * if the latter is small (below 2). If no related scale factor is found returns null.
	 * @param scaleFactor
	 * @return If acceptable a string among "mini", "small", and "large". Otherwise null.
	 */
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
	
	// START KGU 2021-01-27: Issue #81
	/**
	 * Service method to retrieve a sensible system dpi factor
	 * @return a normed DPI scale factor between 1.0f and 2.0f in 0.25f steps
	 */
	public static float getScreenScale()
	{
		if (screenScale == null) {
			/* 
			 * Found on https://github.com/bulenkov/iconloader/blob/master/src/com/bulenkov/iconloader/util/UIUtil.java
			 */
			int dpi = 96;
			try {
				dpi = Toolkit.getDefaultToolkit().getScreenResolution();
			} catch (HeadlessException e) {
			}
			screenScale = 1f;
			if (dpi < 120) screenScale = 1f;
			else if (dpi < 144) screenScale = 1.25f;
			else if (dpi < 168) screenScale = 1.5f;
			else if (dpi < 192) screenScale = 1.75f;
			else screenScale = 2f;
		}
		return screenScale;
	}
	// END KGU 2021-01-26
	
	/**
	 * Recursively rescales all {@link Component}s in container {@code cont} that
	 * can be adapted to the given relative size.
	 * @param cont - the owning container
	 */
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
			// START KGU#324/KGU#642 2019-02-06: Issues #415, #670/2
			else if (cont instanceof JTree && IconLoader.getIcon(0) != null) {
				((JTree)cont).setRowHeight(IconLoader.getIcon(0).getIconHeight());
			}
			// END KGU#324/KGU#642 2019-02-06
		}
		if (cont != null) {
			for (Component comp: cont.getComponents()) {
				if (comp instanceof Container) {
					rescaleComponents((Container)comp, sizeVariant);
				}
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
				// FIXME: check screenScale and the current icon size - maybe there isn't anything to do
				if (alternativeLaf != null) {
					comp.setFont(UIManager.getFont("CheckBox.font"));
					try {
						UIManager.setLookAndFeel(alternativeLaf);
					} catch (Exception e) {
						logger.log(Level.WARNING, "scaleComponents(cont, {0}, {1}): {3}",
								new Object[]{scaleFactor, alternativeLaf, e.toString()});
					}
				}
				try {
					((JToggleButton)comp).setIcon(scaleToggleIcon((JToggleButton)comp, false));
					((JToggleButton)comp).setSelectedIcon(scaleToggleIcon((JToggleButton)comp, true));
				}
				catch (Exception ex) {
					// START KGU#484 2018-03-21: Issue #463
					//System.err.println(ex);
					//ex.printStackTrace(System.err);
					logger.log(Level.WARNING, "Error on scaling/setting toggle icons", ex);
					// END KGU#484 2018-03-21
				}
				if (alternativeLaf != null) {
					try {
						UIManager.setLookAndFeel(laf);
					} catch (Exception e) {
						// START KGU#484 2018-03-21: Issue #463
						System.err.println("GUIScaler.scaleComponents(): " + e.toString());
						logger.log(Level.WARNING, "Intended Look & Feel cannot be set!", e);
						// END KGU#484 2018-03-21
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
				// START KGU#894 2020-12-28: Bugfix #895 (popup menu in TurtleFrame wasn't properly scaled)
				else if (comp instanceof JMenuItem) {
					comp.setFont(UIManager.getFont("MenuItem.font"));
				}
				// END KGU#894 2020-12-28
			}
			if (comp instanceof Container) {
				rescaleComponents((Container)comp, scaleFactor, alternativeLaf);
			}
		}
		// FIXME: We also need a solution for JCheckBoxMenuItems...
//		if (cont instanceof JMenu) {
//			for (Component comp: ((JMenu)cont).getMenuComponents()) {
//				if (comp instanceof JCheckBoxMenuItem) {
//					/* We cannot do a lot as the LaF draws the checkbox
//					 * via software within LaF-specific Factory classes
//					 * which do not even implement a common interface...
//					 */
//				}
//			}
//		}
	}

	// START KGU#287 2016-11-11: Issue #81 (DPI-awareness workaround)
	/**
	 * Returns a scaled checkbox icon for {@link JToggleButton} {@code toggleButton}
	 * (may also be a {@link JCheckbox} or {@link JRadioButton}) in mode given by
	 * {@code selected} (checked or unchecked)
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
	 * Returns a scaled checkbox icon for {@link JToggleButton} {@code toggleButton}
	 * (may also be a {@link JCheckbox} or {@link JRadioButton}) in mode given by
	 * {@code selected} (checked or unchecked)
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
		// RadioButton of the same group to achieve this. But it's difficult to
		// identify the ButtonGroup. So we just look for another RadioButton with
		// the needed selection state under the same parent as a surrogate.
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
			if (key.toString().toLowerCase().contains("font")) {
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

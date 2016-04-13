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
 *      Description:    This is the color preferences dialog.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.31      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:		I used JFormDesigner to desin this window graphically.
 *
 ******************************************************************************************************///

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

/*
 * Created by JFormDesigner on Mon Dec 31 20:03:51 CET 2007
 */



/**
 * @author Robert Fisch
 */
@SuppressWarnings("serial")
public class Colors extends LangDialog {
	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Robert Fisch
	protected JPanel dialogPane;
	protected JPanel contentPanel;
	protected JLabel lblColor0;
	protected JPanel color0;
	protected JLabel lblColor1;
	protected JPanel color1;
	protected JLabel lblColor2;
	protected JPanel color2;
	protected JLabel lblColor3;
	protected JPanel color3;
	protected JLabel lblColor4;
	protected JPanel color4;
	protected JLabel lblColor5;
	protected JPanel color5;
	protected JLabel lblColor6;
	protected JPanel color6;
	protected JLabel lblColor7;
	protected JPanel color7;
	protected JLabel lblColor8;
	protected JPanel color8;
	protected JLabel lblColor9;
	protected JPanel color9;
	protected JPanel buttonBar;
	protected JButton btnOK;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

        private Frame frame = null;

	/*public Colors()
	{
		super();
		setModal(true);
		initComponents();
	}*/

	public Colors(Frame owner) 
	{
		super(owner);
                this.frame = owner;
                setModal(true);
		initComponents();
	}
	
	/*public Colors(Dialog owner) {
		super(owner);
		initComponents();
	}*/

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Robert Fisch
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		lblColor0 = new JLabel();
		color0 = new JPanel();
		lblColor1 = new JLabel();
		color1 = new JPanel();
		lblColor2 = new JLabel();
		color2 = new JPanel();
		lblColor3 = new JLabel();
		color3 = new JPanel();
		lblColor4 = new JLabel();
		color4 = new JPanel();
		lblColor5 = new JLabel();
		color5 = new JPanel();
		lblColor6 = new JLabel();
		color6 = new JPanel();
		lblColor7 = new JLabel();
		color7 = new JPanel();
		lblColor8 = new JLabel();
		color8 = new JPanel();
		lblColor9 = new JLabel();
		color9 = new JPanel();
		buttonBar = new JPanel();
		btnOK = new JButton();

		//======== this ========
		setResizable(false);
		setTitle("Color Preferences");
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));

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
				contentPanel.setLayout(new GridLayout(10, 2, 8, 8));

				//---- lblColor0 ----
				lblColor0.setText("Color 0");
				contentPanel.add(lblColor0);

				//======== color0 ========
				{
					color0.setBackground(Color.white);
					color0.setBorder(new LineBorder(Color.black));
					color0.setPreferredSize(new Dimension(100, 7));
					color0.setLayout(new BorderLayout());
				}
				contentPanel.add(color0);

				//---- lblColor1 ----
				lblColor1.setText("Color 1");
				contentPanel.add(lblColor1);

				//======== color1 ========
				{
					color1.setBackground(new Color(255, 204, 204));
					color1.setBorder(new LineBorder(Color.black));
					color1.setLayout(new BorderLayout());
				}
				contentPanel.add(color1);

				//---- lblColor2 ----
				lblColor2.setText("Color 2");
				contentPanel.add(lblColor2);

				//======== color2 ========
				{
					color2.setBackground(new Color(255, 255, 153));
					color2.setBorder(new LineBorder(Color.black));
					color2.setLayout(new BorderLayout());
				}
				contentPanel.add(color2);

				//---- lblColor3 ----
				lblColor3.setText("Color 3");
				contentPanel.add(lblColor3);

				//======== color3 ========
				{
					color3.setBackground(new Color(153, 255, 153));
					color3.setBorder(new LineBorder(Color.black));
					color3.setLayout(new BorderLayout());
				}
				contentPanel.add(color3);

				//---- lblColor4 ----
				lblColor4.setText("Color 4");
				contentPanel.add(lblColor4);

				//======== color4 ========
				{
					color4.setBackground(new Color(153, 204, 255));
					color4.setBorder(new LineBorder(Color.black));
					color4.setLayout(new BorderLayout());
				}
				contentPanel.add(color4);

				//---- lblColor5 ----
				lblColor5.setText("Color 5");
				contentPanel.add(lblColor5);

				//======== color5 ========
				{
					color5.setBackground(new Color(153, 153, 255));
					color5.setBorder(new LineBorder(Color.black));
					color5.setLayout(new BorderLayout());
				}
				contentPanel.add(color5);

				//---- lblColor6 ----
				lblColor6.setText("Color 6");
				contentPanel.add(lblColor6);

				//======== color6 ========
				{
					color6.setBackground(new Color(255, 153, 255));
					color6.setBorder(new LineBorder(Color.black));
					color6.setLayout(new BorderLayout());
				}
				contentPanel.add(color6);

				//---- lblColor7 ----
				lblColor7.setText("Color 7");
				contentPanel.add(lblColor7);

				//======== color7 ========
				{
					color7.setBackground(new Color(204, 204, 204));
					color7.setBorder(new LineBorder(Color.black));
					color7.setLayout(new BorderLayout());
				}
				contentPanel.add(color7);

				//---- lblColor8 ----
				lblColor8.setText("Color 8");
				contentPanel.add(lblColor8);

				//======== color8 ========
				{
					color8.setBackground(new Color(255, 153, 102));
					color8.setBorder(new LineBorder(Color.black));
					color8.setLayout(new BorderLayout());
				}
				contentPanel.add(color8);

				//---- lblColor9 ----
				lblColor9.setText("Color 9");
				contentPanel.add(lblColor9);

				//======== color9 ========
				{
					color9.setBackground(new Color(153, 102, 255));
					color9.setBorder(new LineBorder(Color.black));
					color9.setLayout(new BorderLayout());
				}
				contentPanel.add(color9);
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
				buttonBar.setLayout(new GridBagLayout());
				((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 80};
				((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0};

				//---- btnOK ----
				btnOK.setText("OK");
				buttonBar.add(btnOK, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
		
		// Bob-thinks
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
					setVisible(false);
				}
			}
			
			public void keyReleased(KeyEvent ke) {} 
			public void keyTyped(KeyEvent kevt) {}
		};
		btnOK.addKeyListener(keyListener);
		
		// add the ACTION-listeners
		ActionListener actionListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				setVisible(false);
			}
		};
		btnOK.addActionListener(actionListener);

                final Frame fframe = this.frame;
                MouseListener mouseListener = new MouseListener()
		{
			public void mouseClicked(MouseEvent e) 
			{
				ColorChooser chooser = new ColorChooser(fframe);
				Point p = getLocationOnScreen();
				chooser.setLocation(Math.round(p.x+(getWidth()-chooser.getWidth())/2), 
										Math.round(p.y+(getHeight()-chooser.getHeight())/2));
				
				chooser.setColor(((JPanel) e.getSource()).getBackground());
				chooser.setLang(langFile);
				
				if(chooser.execute()==true)
				{
					((JPanel) e.getSource()).setBackground(chooser.getColor());
				}
			}
			
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
		};
		color0.addMouseListener(mouseListener);
		color1.addMouseListener(mouseListener);
		color2.addMouseListener(mouseListener);
		color3.addMouseListener(mouseListener);
		color4.addMouseListener(mouseListener);
		color5.addMouseListener(mouseListener);
		color6.addMouseListener(mouseListener);
		color7.addMouseListener(mouseListener);
		color8.addMouseListener(mouseListener);
		color9.addMouseListener(mouseListener);
	}

	
}

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
 *      Description:    This is GUI scale preview and preselect dialog.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.01.11      First Issue
 *      Kay Gürtzig     2017.05.09      Issue #400: keyListener at all controls, initial focus to spinner
 *      Kay Gürtzig     2018.02.06      Issue #4/#81: Icon scaling preview adapted 
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.LookAndFeel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.locales.LangDialog;

/**
 * @author codemanyak
 *
 */
@SuppressWarnings("serial")
public class GUIScaleChooser extends LangDialog implements ChangeListener {

	protected JPanel pnlChooser = new JPanel();
	protected JPanel contentPanel = new JPanel();
	protected JPanel pnlComment = new JPanel();
	protected JPanel pnlTest = new JPanel();
	protected JLabel lblSpinner = new JLabel("Scale factor");
	protected JPanel pnlSpinner = new JPanel();
	protected JLabel lblDummy = new JLabel("");
	protected JSpinner spnScale = null;
	protected JTextArea txtComment = new JTextArea("This is just a preview. A hot scaling of the entire application isn't possible.\nThe scale factor will only be active on next Structorizer start.");
	protected JLabel lblIcon = new JLabel("Icon Preview");
	protected JLabel lblTest = new JLabel("Font Preview");
	protected JCheckBox chkTest = new JCheckBox("CheckBox Preview");
	protected JPanel buttonBar = new JPanel();
	protected JButton btnOK = new JButton("OK");

	public GUIScaleChooser()
	{
		initComponents();
		setModal(true);
	}

	public GUIScaleChooser(Frame owner)
	{
		super(owner);
		setModal(true);
		initComponents();
	}
	
	private void initComponents() {
		double scaleFactor = Element.E_NEXT_SCALE_FACTOR;
		SpinnerModel spnModel = new SpinnerNumberModel(1.0, 0.5, 5.0, 0.5);
		spnScale = new JSpinner(spnModel);
		lblIcon.setIcon(IconLoader.getIcon(0));
		
		if (scaleFactor < 0.5) scaleFactor = 0.5;

		//======== ScaleChooser ========
		{
			this.setTitle("GUI Scale");
			Container scaleChooserContentPane = getContentPane();
			scaleChooserContentPane.setLayout(new BorderLayout());

			//======== scaleChooser Pane ========
			{
				int border = (int)(12 * scaleFactor);
				pnlChooser.setBorder(new EmptyBorder(border, border, border, border));

				pnlChooser.setLayout(new BorderLayout());

				//======== contentPanel ========
				{
					border = (int)(8 * scaleFactor);
					contentPanel.setLayout(new BorderLayout(border, border));
					// END KGU#287 2016-11-02

					//======== pnlComment ========
					{
						pnlComment.setLayout(new GridLayout(1,1));
						pnlComment.add(txtComment);
						txtComment.putClientProperty("JComponent.sizeVariant", "large");
						txtComment.setEditable(false);
						txtComment.setFont(lblDummy.getFont());
					}
					GUIScaler.rescaleComponents(pnlComment);
					contentPanel.add(pnlComment, BorderLayout.NORTH);

					//======== pnlSpinner ========
					{
						lblSpinner.putClientProperty("JComponent.sizeVariant", "large");
						spnScale.putClientProperty("JComponent.sizeVariant", "large");

						GridBagLayout gbSpinner = new GridBagLayout();
				        GridBagConstraints gbcSpinner = new GridBagConstraints();
				        border = (int)(5 * scaleFactor);
				        gbcSpinner.insets = new Insets(0, 0, border, border);
				        pnlSpinner.setLayout(gbSpinner);
				        
						JPanel pnlAux = new JPanel();
						pnlAux.setLayout(new GridLayout(1,2));
						((GridLayout)pnlAux.getLayout()).setHgap(border);
						pnlAux.add(lblSpinner);
						pnlAux.add(spnScale);

						gbcSpinner.gridx = 1;
				        gbcSpinner.gridy = 1;
				        gbcSpinner.gridwidth = 1;
				        gbcSpinner.gridheight = 1;
				        gbcSpinner.fill = GridBagConstraints.NONE;
				        gbcSpinner.weightx = 1;
				        gbcSpinner.weighty = 1;
				        gbcSpinner.anchor = GridBagConstraints.NORTHWEST;
				        gbSpinner.setConstraints(pnlAux, gbcSpinner);
						pnlSpinner.add(pnlAux);

				        gbcSpinner.gridx = 2;
				        gbcSpinner.fill = GridBagConstraints.REMAINDER;
				        gbcSpinner.gridwidth = 100;
				        //gbcSpinner.anchor = GridBagConstraints.NORTHWEST;
				        gbSpinner.setConstraints(lblDummy, gbcSpinner);
						pnlSpinner.add(lblDummy);
						
						spnScale.addChangeListener(this);
					}
					GUIScaler.rescaleComponents(pnlSpinner);
					contentPanel.add(pnlSpinner, BorderLayout.CENTER);
		
					//======== pnlTest ========
					{
						pnlTest.setLayout(new GridLayout(0, 1));
						pnlTest.add(lblTest);
						pnlTest.add(lblIcon);
						pnlTest.add(chkTest);
					}
					contentPanel.add(pnlTest, BorderLayout.SOUTH);
					
				}
				pnlChooser.add(contentPanel, BorderLayout.CENTER);

				//======== buttonBar ========
				{
					buttonBar.setBorder(new EmptyBorder((int)(12*scaleFactor), 0, 0, 0));
					buttonBar.setLayout(new GridBagLayout());
					((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, (int)(80*scaleFactor)};
					((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0};

					//---- btnOK ----
					btnOK.setText("OK");
					buttonBar.add(btnOK, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					GUIScaler.rescaleComponents(buttonBar);
				}
				pnlChooser.add(buttonBar, BorderLayout.SOUTH);
		
			}
			scaleChooserContentPane.add(pnlChooser, BorderLayout.CENTER);
			
			spnScale.setValue(scaleFactor);
			
			this.pack();
			//this.setLocationRelativeTo(this.getOwner());

			// add the KEY-listener
			KeyListener keyListener = new KeyListener()
			{
				public void keyPressed(KeyEvent e) 
				{
					if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
					{
						setVisible(false);
					}
					else if(e.getKeyCode() == KeyEvent.VK_ENTER)
					{
						Element.E_NEXT_SCALE_FACTOR = (Double)spnScale.getValue();
						setVisible(false);
					}
				}
				
				public void keyReleased(KeyEvent ke) {} 
				public void keyTyped(KeyEvent kevt) {}
			};
			btnOK.addKeyListener(keyListener);
			((JSpinner.DefaultEditor)spnScale.getEditor()).getTextField().addKeyListener(keyListener);
			// START KGU#393 2017-05-09: Issue #400 - involve all controls
			txtComment.addKeyListener(keyListener);
			this.chkTest.addKeyListener(keyListener);
			spnScale.requestFocusInWindow();
			// END KGU#393 2017-05-09
			
			// add the ACTION-listeners
			ActionListener actionListener = new ActionListener()
			{
				public void actionPerformed(ActionEvent event)
				{
					Element.E_NEXT_SCALE_FACTOR = (Double)spnScale.getValue();
					setVisible(false);
				}
			};
			btnOK.addActionListener(actionListener);
		}
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	@Override
	public void stateChanged(ChangeEvent evt) {
		SpinnerModel numberModel = spnScale.getModel();
		double scaleFactor = (Double)numberModel.getValue();
		String sizeVariant = GUIScaler.getSizeVariant(scaleFactor);
		
		Font font = UIManager.getLookAndFeelDefaults().getFont("Label.font");
		if (sizeVariant == null) {
			font = font.deriveFont((float)(font.getSize() * scaleFactor));
		}
		lblTest.setFont(font);
		lblIcon.setFont(font);
		
		font = UIManager.getLookAndFeelDefaults().getFont("CheckBox.font");
		if (sizeVariant == null) {
			font = font.deriveFont((float)(font.getSize() * scaleFactor));
		}
		chkTest.setFont(font);
		
		if (scaleFactor == 1.0) {
			sizeVariant = "regular";
		}
		
		if (sizeVariant != null) {
			chkTest.setIcon(null);
			chkTest.setSelectedIcon(null);
			lblTest.putClientProperty("JComponent.sizeVariant", sizeVariant);
			lblIcon.putClientProperty("JComponent.sizeVariant", sizeVariant);
			chkTest.putClientProperty("JComponent.sizeVariant", sizeVariant);
		}
		else {
			String surrLaf = null;
			LookAndFeel currLaf = UIManager.getLookAndFeel();
			if (currLaf.getName().equalsIgnoreCase("nimbus")) {
				for (LookAndFeelInfo lafi: UIManager.getInstalledLookAndFeels()) {
					if (!lafi.getName().equalsIgnoreCase("nimbus")) {
						surrLaf = lafi.getClassName();
						if (lafi.getName().equalsIgnoreCase("metal")) {
							break;	// Preferred look and feel
						}
					}
				}
				if (surrLaf != null) {
					try {
						UIManager.setLookAndFeel(surrLaf);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			chkTest.setIcon(GUIScaler.scaleToggleIcon(chkTest, false, false));
			chkTest.setSelectedIcon(GUIScaler.scaleToggleIcon(chkTest, true, false));
			if (surrLaf != null) {
				try {
					UIManager.setLookAndFeel(currLaf);
				} catch (UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}
			}
		}

		scaleFactor = Math.max(1.0, scaleFactor);
		// START KGU#486 2018-02-06:Issue #4
		//URL myUrl = IconLoader.getURI("icons/074_nsd.png");
		//ImageIcon ii = new ImageIcon(myUrl);
		//int w = (int)(scaleFactor * ii.getIconWidth());
		//int h = (int)(scaleFactor * ii.getIconHeight());
		//lblIcon.setIcon(IconLoader.scaleTo(ii, w, h));
		double scale = Double.parseDouble(Ini.getInstance().getProperty("scaleFactor", "1"));
		lblIcon.setIcon(IconLoader.getIconImage("000_structorizer.png", scaleFactor / scale));
		// END KGU#486 2018-02-06
        
        pack();
	}
	
    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try
        {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
            {
                if ("Metal".equals(info.getName()))
                {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex)
        {
            java.util.logging.Logger.getLogger(ExportOptionDialoge.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(ExportOptionDialoge.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(ExportOptionDialoge.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(ExportOptionDialoge.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable()
        {

            public void run()
            {
                new GUIScaleChooser().setVisible(true);
            }
        });
    }
}

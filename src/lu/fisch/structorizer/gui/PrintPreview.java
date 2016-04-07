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
 *      Description:    This class is a special speedbutton.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2008.01.27      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:		Differents parts of code have been copied from different forums on the net.
 *
 ******************************************************************************************************///
 
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.print.*;

import javax.swing.*;
import javax.swing.border.*;

/**
 * @author Bob Fisch
 */
@SuppressWarnings("serial")
public class PrintPreview extends LangDialog implements Runnable{

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Bob Fisch
	protected JPanel dialogPane;
	protected JPanel contentPanel;
	protected JScrollPane scrPreview;
	protected JPanel buttonBar;
	protected JComboBox<String> m_cbScale;
	protected JButton btnOrientation;
	protected JButton btnOK;
	protected JButton btnCancel;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	
	// Bob
	protected int m_wPage;
	protected int m_hPage;
	protected Printable m_target;
	protected PreviewContainer m_preview;
	protected PageFormat pp_pf = null;
	

	public PrintPreview(Frame frame, Printable target) {
		super(frame);
		m_target = target;
		initComponents();
	}
	
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Bob Fisch
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		scrPreview = new JScrollPane();
		buttonBar = new JPanel();
		m_cbScale = new JComboBox<String>();
		btnOrientation = new JButton();
		btnOK = new JButton();
		btnCancel = new JButton();

                btnOrientation.setVisible(false);

		//======== this ========
		setTitle("Print Preview");
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));

			// JFormDesigner evaluation mark
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setLayout(new BorderLayout());
				contentPanel.add(scrPreview, BorderLayout.CENTER);
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
				buttonBar.setLayout(new GridBagLayout());
				((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 0, 85, 80};
				((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 0.0};
				buttonBar.add(m_cbScale, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- btnOrientation ----
				btnOrientation.setText("Toggle Orientation");
				buttonBar.add(btnOrientation, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- btnOK ----
				btnOK.setText("OK");
				buttonBar.add(btnOK, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- btnCancel ----
				btnCancel.setText("Cancel");
				buttonBar.add(btnCancel, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
		
		// Bob
		setModal(true);
		setSize(680,400);
		
		// add the KEY-listeners
		KeyListener keyListener = new KeyListener()
		{
			public void keyPressed(KeyEvent e) 
			{
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					setVisible(false);
				}
			}
			
			public void keyReleased(KeyEvent ke) {} 
			public void keyTyped(KeyEvent kevt) {}
		};
		btnCancel.addKeyListener(keyListener);
		btnOrientation.addKeyListener(keyListener);
		this.addKeyListener(keyListener);
		
		// OK button
		ActionListener lst = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					// Use default printer, no dialog
					PrinterJob prnJob = PrinterJob.getPrinterJob();
					prnJob.setPrintable(m_target,pp_pf);
					if (prnJob.printDialog()) 
					{
						setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						prnJob.print();
						setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						dispose();
					}
				}
				catch (PrinterException ex)
				{
					ex.printStackTrace();
					System.err.println("Printing error: "+ex.toString());
				}
			}
		};
		btnOK.addActionListener(lst);
		
		// add the KEY-listeners 2
		KeyListener keyListenerPrint = new KeyListener()
		{
			public void keyPressed(KeyEvent e) 
			{
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					setVisible(false);
				}
				else if(e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					try
					{
						PrinterJob prnJob = PrinterJob.getPrinterJob();
						prnJob.setPrintable(m_target,pp_pf);
						if (prnJob.printDialog()) 
						{
							setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
							prnJob.print();
							setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
							dispose();
						}
					}
					catch (PrinterException ex)
					{
						ex.printStackTrace();
						System.err.println("Printing error: "+ex.toString());
					}
				}
			}
			
			public void keyReleased(KeyEvent ke) {} 
			public void keyTyped(KeyEvent kevt) {}
		};
		btnOK.addKeyListener(keyListenerPrint);
		
		
		// Cancel button
		lst = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		};
		btnCancel.addActionListener(lst);
		
		// scale list
		m_cbScale.addItem("10 %");
		m_cbScale.addItem("25 %");
		m_cbScale.addItem("50 %");
		m_cbScale.addItem("75 %");
		m_cbScale.addItem("100 %");
		m_cbScale.setSelectedIndex(4);
		lst = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Thread runner = new Thread(PrintPreview.this);
				runner.start();
			}
		};
		m_cbScale.addActionListener(lst);
		
		// orientation
		ActionListener orient = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (pp_pf.getOrientation() == PageFormat.PORTRAIT) 
				{
					pp_pf.setOrientation(PageFormat.LANDSCAPE);
				} 
				else 
				{
					pp_pf.setOrientation(PageFormat.PORTRAIT);
				}
				Thread runner = new Thread(PrintPreview.this);
				runner.start();
			}
		};
		btnOrientation.addActionListener(orient);
		
		
		// preview
		m_preview = new PreviewContainer();
		
		PrinterJob prnJob = PrinterJob.getPrinterJob();
		pp_pf = prnJob.defaultPage();
		if (pp_pf.getHeight() == 0 || pp_pf.getWidth() == 0)
		{
			System.err.println("Unable to determine default page size");
			return;
		}
                
                m_wPage = (int)(pp_pf.getWidth());
                m_hPage = (int)(pp_pf.getHeight());

                int scale = 100;
		int w = (int)(m_wPage * scale / 100);
		int h = (int)(m_hPage * scale / 100);
		
		int pageIndex = 0;
		try
		{
			while (true)
			{
				BufferedImage img = new BufferedImage(m_wPage, m_hPage, BufferedImage.TYPE_INT_RGB);
				Graphics g = img.getGraphics();
				g.setColor(Color.white);
				g.fillRect(0, 0, m_wPage, m_hPage);
				if (m_target.print(g, pp_pf, pageIndex) != Printable.PAGE_EXISTS)
					break;
				PagePreview pp = new PagePreview(w, h, img);
				m_preview.add(pp);
				pageIndex++;
			}
		}
		catch (PrinterException e)
		{
			e.printStackTrace();
			System.err.println("Printing error: "+e.toString());
		}
		m_preview.addKeyListener(keyListener);

		scrPreview.setViewportView(m_preview);

		btnOrientation.setFocusable(false);
		m_cbScale.setFocusable(false);
		btnOK.requestFocus(true);
	}
	
	// runnable interface
	public void run()
	{
                m_wPage = (int)(pp_pf.getWidth());
                m_hPage = (int)(pp_pf.getHeight());
		
		String str = m_cbScale.getSelectedItem().toString();
		if (str.endsWith("%"))
			str = str.substring(0, str.length() - 1);
		str = str.trim();
		int scale = 0;
		try
		{
			scale = Integer.parseInt(str);
		}
		catch (NumberFormatException ex)
		{
			return;
		}
		int w = (int)(m_wPage * scale / 100);
		int h = (int)(m_hPage * scale / 100);
		
		m_preview = new PreviewContainer();
		
		int pageIndex = 0;
		try
		{
			while (true)
			{
				BufferedImage img = new BufferedImage(m_wPage, m_hPage, BufferedImage.TYPE_INT_RGB);
				Graphics g = img.getGraphics();
				g.setColor(Color.white);
				g.fillRect(0, 0, m_wPage, m_hPage);
				if (m_target.print(g, pp_pf, pageIndex) != Printable.PAGE_EXISTS)
					break;
				PagePreview pp = new PagePreview(w, h, img);
				m_preview.add(pp);
				pageIndex++;
			}
		}
		catch (PrinterException e)
		{
			e.printStackTrace();
			System.err.println("Printing error: "+e.toString());
		}	
		
		scrPreview.setViewportView(m_preview);
		System.gc();
				
		/*
		Component[] comps = m_preview.getComponents();
		for (int k = 0; k < comps.length; k++)
		{
			if (!(comps[k] instanceof PagePreview))
				continue;
			PagePreview pp = (PagePreview) comps[k];
			pp.setScaledSize(w, h);
		}
		m_preview.doLayout();
		m_preview.getParent().getParent().validate();
		*/
	}
	
	
	// sub classes
	class PreviewContainer extends JPanel
        {
            protected int H_GAP = 16;
            protected int V_GAP = 10;

            @Override
            public Dimension getPreferredSize()
            {
                    int n = getComponentCount();
                    if (n == 0)
                            return new Dimension(H_GAP, V_GAP);
                    Component comp = getComponent(0);
                    Dimension dc = comp.getPreferredSize();
                    int w = dc.width;
                    int h = dc.height;

                    Dimension dp = getParent().getSize();
                    int nCol = Math.max((dp.width - H_GAP) / (w + H_GAP), 1);
                    int nRow = n / nCol;
                    if (nRow * nCol < n)
                            nRow++;

                    int ww = nCol * (w + H_GAP) + H_GAP;
                    int hh = nRow * (h + V_GAP) + V_GAP;
                    Insets ins = getInsets();
                    return new Dimension(ww + ins.left + ins.right, hh + ins.top + ins.bottom);
            }

            @Override
            public Dimension getMaximumSize()
            {
                    return getPreferredSize();
            }

            @Override
            public Dimension getMinimumSize()
            {
                    return getPreferredSize();
            }

            @Override
            public void doLayout()
            {
                    Insets ins = getInsets();
                    int x = ins.left + H_GAP;
                    int y = ins.top + V_GAP;

                    int n = getComponentCount();
                    if (n == 0)
                            return;
                    Component comp = getComponent(0);
                    Dimension dc = comp.getPreferredSize();
                    int w = dc.width;
                    int h = dc.height;

                    Dimension dp = getParent().getSize();
                    int nCol = Math.max((dp.width - H_GAP) / (w + H_GAP), 1);
                    int nRow = n / nCol;
                    if (nRow * nCol < n)
                            nRow++;

                    int index = 0;
                    for (int k = 0; k < nRow; k++)
                    {
                            for (int m = 0; m < nCol; m++)
                            {
                                    if (index >= n)
                                            return;
                                    comp = getComponent(index++);
                                    comp.setBounds(x, y, w, h);
                                    x += w + H_GAP;
                            }
                            y += h + V_GAP;
                            x = ins.left + H_GAP;
                    }
            }
        }
	
	class PagePreview extends JPanel
        {
            protected int m_w;
            protected int m_h;
            protected Image m_source;
            protected Image m_img;

            public PagePreview(int w, int h, Image source)
            {
                    m_w = w;
                    m_h = h;
                    m_source = source;
                    m_img = m_source.getScaledInstance(m_w, m_h, Image.SCALE_SMOOTH);
                    m_img.flush();
                    setBackground(Color.white);
                    setBorder(new MatteBorder(1, 1, 2, 2, Color.black));
            }

            public void setScaledSize(int w, int h)
            {
                    m_w = w;
                    m_h = h;
                    m_img = m_source.getScaledInstance(m_w, m_h, Image.SCALE_SMOOTH);
                    repaint();
            }

            @Override
            public Dimension getPreferredSize()
            {
                    Insets ins = getInsets();
                    return new Dimension(m_w + ins.left + ins.right, m_h + ins.top + ins.bottom);
            }

            @Override
            public Dimension getMaximumSize()
            {
                    return getPreferredSize();
            }

            @Override
            public Dimension getMinimumSize()
            {
                    return getPreferredSize();
            }

            @Override
            public void paint(Graphics g)
            {
                    g.setColor(getBackground());
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.drawImage(m_img, 0, 0, this);
                    paintBorder(g);
            }
        }

}

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
/*
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.awt.print.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;


public class PrintPreview extends JDialog implements Runnable
	{
		protected JScrollPane displayArea;
		protected int m_wPage;
		protected int m_hPage;
		protected int width;
		protected int height;
		protected Printable m_target;
		protected JComboBox m_cbScale;
		protected PreviewContainer m_preview;
		protected PageFormat pp_pf = null;
		protected JButton formatButton;
		protected JButton shrinkButton;
		private static final int INCH = 72;
		private static boolean bScallToFitOnePage = false;
		protected JButton bt;
		
		protected void getThePreviewPages()
		{
			m_wPage = (int)(pp_pf.getWidth());
			m_hPage = (int)(pp_pf.getHeight());
			int scale = getDisplayScale();
			width = (int)Math.ceil(m_wPage*scale/100);
			height = (int)Math.ceil(m_hPage*scale/100);
			
			int sX = width/m_wPage;
			int sY = height/m_hPage;
			int sca = Math.min(sX,sY);
			
			int pageIndex = 0;
			try {
				while (true) {
					BufferedImage img = new BufferedImage(m_wPage, m_hPage,
														  BufferedImage.TYPE_INT_RGB);
					Graphics g = img.getGraphics();
					g.setColor(Color.white);
					g.fillRect(0, 0, m_wPage, m_hPage);
					if (bScallToFitOnePage) 
					{
						Graphics2D gg = (Graphics2D) g;
						m_target.print(g, pp_pf, -1);
						PagePreview pp = new PagePreview(width, height, img);
						m_preview.add(pp);
						break;
					} 
					else
					{
						if (m_target.print(g, pp_pf, pageIndex) !=  Printable.PAGE_EXISTS) break;
						PagePreview pp = new PagePreview(width, height, img);
						m_preview.add(pp);
						pageIndex++;
					}
				}
			} catch (OutOfMemoryError om) {
				JOptionPane.showMessageDialog(this,
											  "image is too big that run out of memory.", "Print Preview",
											  JOptionPane.INFORMATION_MESSAGE);
			}
			catch (PrinterException e) {
				e.printStackTrace();
				System.err.println("Printing error: "+e.toString());
			}
		}
		
		protected void previewThePages(int orientation)
		{
			//if (displayArea != null) displayArea.setVisible(false);
			
			m_preview = new PreviewContainer();
			
			getThePreviewPages();
			
			displayArea = new JScrollPane(m_preview);
			getContentPane().add(displayArea, BorderLayout.CENTER);
			//setVisible(true);
			System.gc();
		}
		
		
		protected void createButtons(JToolBar tb, boolean shrink) {
			bt = new JButton("Print");
			ActionListener lst = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						PrinterJob prnJob = PrinterJob.getPrinterJob();
						prnJob.setPrintable(m_target, pp_pf);
						if (prnJob.printDialog()) {
							setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
							prnJob.print();
							setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						}
						dispose();
					}
					catch (PrinterException ex) {
						ex.printStackTrace();
						System.err.println("Printing error: "+ex.toString());
					}
				}
			};
			bt.addActionListener(lst);
			bt.setAlignmentY(0.5f);
			bt.setMargin(new Insets(4,6,4,6));
			tb.add(bt);
			
			if (pp_pf.getOrientation() == PageFormat.PORTRAIT)
				formatButton = new JButton("Landscape");
			else
				formatButton = new JButton("Portrait");
			
			lst = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (pp_pf.getOrientation() == PageFormat.PORTRAIT) {
						pp_pf.setOrientation(PageFormat.LANDSCAPE);
						previewThePages(PageFormat.LANDSCAPE);
						formatButton.setText("Portrait");
					} else {
						pp_pf.setOrientation(PageFormat.PORTRAIT);
						previewThePages(PageFormat.PORTRAIT);
						formatButton.setText("Landscape");
					}
				}
			};
			formatButton.addActionListener(lst);
			formatButton.setAlignmentY(0.5f);
			formatButton.setMargin(new Insets(4,6,4,6));
			tb.add(formatButton);
			
			if (shrink) {
				shrinkButton = new JButton("Shrink to fit");
				
				lst = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						bScallToFitOnePage = !bScallToFitOnePage;
						previewThePages(pp_pf.getOrientation());
					}
				};
				shrinkButton.addActionListener(lst);
				shrinkButton.setAlignmentY(0.5f);
				shrinkButton.setMargin(new Insets(4,6,4,6));
				tb.add(shrinkButton);
			}
			
			bt = new JButton("Close");
			lst = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					dispose();
				}
			};
			bt.addActionListener(lst);
			bt.setAlignmentY(0.5f);
			bt.setMargin(new Insets(4,6,4,6));
			tb.add(bt);
		}
		
		public int getDisplayScale() {
			String str = m_cbScale.getSelectedItem().toString();
			if (str.endsWith("%")) str = str.substring(0, str.length()-1);
			str = str.trim();
			int scale = 0;
			try { scale = Integer.parseInt(str); }
            catch (NumberFormatException ex) { return 25; }
			return scale ;
		}
		
		public PrintPreview(Printable target) {
			this(target, "Print Preview", false);
		}
		
		public PrintPreview(Printable target, String title) {
			this(target, title, false);
		}
		
		public PrintPreview(Printable target, String title, boolean shrink) {
			super();
			setModal(true);
			bScallToFitOnePage = false;  // reset to default
			PrinterJob prnJob = PrinterJob.getPrinterJob();
			pp_pf = prnJob.defaultPage();
			if (pp_pf.getHeight()==0 || pp_pf.getWidth()==0) {
				System.err.println("Unable to determine default page size");
				return;
			}
			setSize(600, 400);
			m_target = target;
			
			displayArea = null;
			m_preview = null;
			
			JToolBar tb = new JToolBar();
			createButtons(tb, shrink);
			
			String[] scales = { "10 %", "25 %", "50 %", "100 %" };
			m_cbScale = new JComboBox(scales);
			m_cbScale.setSelectedIndex(1);
			ActionListener lst = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Thread runner = new Thread(PrintPreview.this);
					runner.start();
				}
			};
			m_cbScale.addActionListener(lst);
			m_cbScale.setMaximumSize(m_cbScale.getPreferredSize());
			m_cbScale.setEditable(true);
			tb.addSeparator();
			tb.add(m_cbScale);
			getContentPane().add(tb, BorderLayout.NORTH);
			
			//    previewThePages(PageFormat.PORTRAIT);
			previewThePages(pp_pf.getOrientation());
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setVisible(false);
			
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
			bt.addKeyListener(keyListener);		
			this.addKeyListener(keyListener);
			m_cbScale.addKeyListener(keyListener);		
		}
		
		public void run() {
			int scale = getDisplayScale();
			width = (int)(m_wPage*scale/100);
			height = (int)(m_hPage*scale/100);
			
			Component[] comps = m_preview.getComponents();
			for (int k=0; k<comps.length; k++) {
				if (!(comps[k] instanceof PagePreview))
					continue;
				PagePreview pp = (PagePreview)comps[k];
				pp.setScaledSize(width, height);
			}
			m_preview.doLayout();
			m_preview.getParent().getParent().validate();
		}
		
		class PreviewContainer extends JPanel
			{
				protected int H_GAP = 16;
				protected int V_GAP = 10;
				
				public Dimension getPreferredSize() {
					int n = getComponentCount();
					if (n == 0)
						return new Dimension(H_GAP, V_GAP);
					Component comp = getComponent(0);
					Dimension dc = comp.getPreferredSize();
					int w = dc.width;
					int h = dc.height;
					
					Dimension dp = getParent().getSize();
					int nCol = Math.max((dp.width-H_GAP)/(w+H_GAP), 1);
					int nRow = n/nCol;
					if (nRow*nCol < n)
						nRow++;
					
					int ww = nCol*(w+H_GAP) + H_GAP;
					int hh = nRow*(h+V_GAP) + V_GAP;
					Insets ins = getInsets();
					return new Dimension(ww+ins.left+ins.right,
										 hh+ins.top+ins.bottom);
				}
				
				public Dimension getMaximumSize() {
					return getPreferredSize();
				}
				
				public Dimension getMinimumSize() {
					return getPreferredSize();
				}
				
				public void doLayout() {
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
					int nCol = Math.max((dp.width-H_GAP)/(w+H_GAP), 1);
					int nRow = n/nCol;
					if (nRow*nCol < n)
						nRow++;
					
					int index = 0;
					for (int k = 0; k<nRow; k++) {
						for (int m = 0; m<nCol; m++) {
							if (index >= n)
								return;
							comp = getComponent(index++);
							comp.setBounds(x, y, w, h);
							x += w+H_GAP;
						}
						y += h+V_GAP;
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
				
				public PagePreview(int w, int h, Image source) {
					m_w = w;
					m_h = h;
					m_source= source;
					m_img = m_source.getScaledInstance(m_w, m_h, Image.SCALE_SMOOTH);
					m_img.flush();
					setBackground(Color.white);
					setBorder(new MatteBorder(1, 1, 2, 2, Color.black));
				}
				
				public void setScaledSize(int w, int h) {
					m_w = w;
					m_h = h;
					m_img = m_source.getScaledInstance(m_w, m_h, Image.SCALE_SMOOTH);
					repaint();
				}
				
				public Dimension getPreferredSize() {
					Insets ins = getInsets();
					return new Dimension(m_w+ins.left+ins.right,  m_h+ins.top+ins.bottom);
				}
				
				public Dimension getMaximumSize() {
					return getPreferredSize();
				}
				
				public Dimension getMinimumSize() {
					return getPreferredSize();
				}
				
				public void paint(Graphics g) {
					g.setColor(getBackground());
					g.fillRect(0, 0, getWidth(), getHeight());
					g.drawImage(m_img, 0, 0, this);
					paintBorder(g);
				}
			}
	}
/**/

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.print.*;

import javax.swing.*;
import javax.swing.border.*;

@SuppressWarnings("serial")
public class PrintPreviewOld extends JDialog
	{
        protected int m_wPage;
        protected int m_hPage;
        protected Printable m_target;
        protected JComboBox<String> m_cbScale;
        protected PreviewContainer m_preview;

		
        public PrintPreviewOld(Printable target, String title)
        {
			super();
			setModal(true);
			setSize(600, 400);
			m_target = target;
			
			JToolBar tb = new JToolBar();
			JButton bt = new JButton("Print", IconLoader.ico041);
			ActionListener lst = new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						// Use default printer, no dialog
						PrinterJob prnJob = PrinterJob.getPrinterJob();
						if (prnJob.printDialog()) 
						{
							prnJob.setPrintable(m_target);
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
			bt.addActionListener(lst);
			bt.setAlignmentY(0.5f);
			bt.setMargin(new Insets(4, 6, 4, 6));
			tb.add(bt);
			
			bt = new JButton("Close");
			lst = new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					dispose();
				}
			};
			bt.addActionListener(lst);
			bt.setAlignmentY(0.5f);
			bt.setMargin(new Insets(2, 6, 2, 6));
			tb.add(bt);
			
			//String[] scales = { "10 %", "25 %", "50 %", "100 %" };
			//m_cbScale = new JComboBox(scales);
			lst = new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					Thread runner = new Thread()
					{
						public void run()
						{
							String str = m_cbScale.getSelectedItem(). toString();
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
						}
					};
					runner.start();
				}
			};
			m_cbScale.addActionListener(lst);
			m_cbScale.setMaximumSize(m_cbScale.getPreferredSize());
			m_cbScale.setEditable(true);
			tb.addSeparator();
			tb.add(m_cbScale);
			getContentPane().add(tb, BorderLayout.NORTH);
			
			m_preview = new PreviewContainer();
			
			PrinterJob prnJob = PrinterJob.getPrinterJob();
			PageFormat pageFormat = prnJob.defaultPage();
			if (pageFormat.getHeight() == 0 || pageFormat.getWidth() == 0)
			{
				System.err.println("Unable to determine default page size");
				return;
			}
			m_wPage = (int)(pageFormat.getWidth());
			m_hPage = (int)(pageFormat.getHeight());
			int scale = 10;
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
					if (target.print(g, pageFormat, pageIndex) != Printable.PAGE_EXISTS)
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
			
			JScrollPane ps = new JScrollPane(m_preview);
			getContentPane().add(ps, BorderLayout.CENTER);
			
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setVisible(false);
        }
		
        class PreviewContainer extends JPanel
			{
                protected int H_GAP = 16;
                protected int V_GAP = 10;
				
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
				
                public Dimension getMaximumSize()
                {
					return getPreferredSize();
                }
				
                public Dimension getMinimumSize()
                {
					return getPreferredSize();
                }
				
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
				
                public Dimension getPreferredSize()
                {
					Insets ins = getInsets();
					return new Dimension(m_w + ins.left + ins.right, m_h + ins.top + ins.bottom);
                }
				
                public Dimension getMaximumSize()
                {
					return getPreferredSize();
                }
				
                public Dimension getMinimumSize()
                {
					return getPreferredSize();
                }
				
                public void paint(Graphics g)
                {
					g.setColor(getBackground());
					g.fillRect(0, 0, getWidth(), getHeight());
					g.drawImage(m_img, 0, 0, this);
					paintBorder(g);
                }
			}
	}
/**/	
	
/*
//
//  PrintPreview.java
//  Structorizer
//
//  Created by Robert Fisch on 1/25/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//

import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.print.*;
import java.awt.image.*;

public class PrintPreview extends JPanel implements ActionListener{
	static final double INITIAL_SCALE_DIVISOR = 1.5; // scale factor == 1 / 1.5
	
	Component targetComponent;
	PageFormat pageFormat = new PageFormat();
	double xScaleDivisor = INITIAL_SCALE_DIVISOR;
	double yScaleDivisor = INITIAL_SCALE_DIVISOR;
	BufferedImage pcImage;
	
	JPanel hold = new JPanel();
	PreviewPage prp;
	
	ButtonGroup pf = new ButtonGroup();
	JRadioButton pf1;
	JRadioButton pf2;
	JLabel xsl = new JLabel("Xscale div by:", JLabel.LEFT);
	JLabel ysl = new JLabel("Yscale div by:", JLabel.LEFT);
	JButton ftp = new JButton("Fit to Page");
	JCheckBox cp = new JCheckBox("Constrain Proportions");
	JButton preview = new JButton("PREVIEW");
	JButton print = new JButton("PRINT");
	
	JSpinner xsp, ysp;
	SpinnerNumberModel snmx, snmy;
	
	JFrame workFrame;
	
	Color bgColor = Color.white;
	
	int pcw, pch;
	double wh, hw;
	
	public PrintPreview(Component pc){
		setBackground(bgColor);
		
		targetComponent = pc;
		
		// for a JTable, we can't use simple component.paint(g) call
		// because it doesn't paint table header !!
		if (pc instanceof JTable){
			TableModel tm = ((JTable)pc).getModel();
			JTable workTable = new JTable(tm); // make pure clone
			targetComponent = getTableComponent(workTable);
		}
		
		pcImage = new BufferedImage(pcw = targetComponent.getWidth(),
									pch = targetComponent.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = pcImage.createGraphics();
		//targetComponent.paint(g);
		targetComponent.print(g);
		g.dispose();
		
		wh = (double)pcw / (double)pch;
		hw = (double)pch / (double)pcw;
		// workFrame is used in getTableComponent() method
		 / only for visualizing the table component and its header
		 //
		if (workFrame != null){ // if you don't use table clone here,
			workFrame.dispose();  // calling dispose() delete the table
		}                       // from original app window
		
		pageFormat.setOrientation(PageFormat.PORTRAIT);
		prp = new PreviewPage();
		
		snmx = new SpinnerNumberModel(1.5, 0.1, 10.0, 0.1);
		snmy = new SpinnerNumberModel(1.5, 0.1, 10.0, 0.1);
		xsp = new JSpinner(snmx);
		ysp = new JSpinner(snmy);
		
		pf1 = new JRadioButton("Portrait");
		pf1.setActionCommand("1");
		pf1.setSelected(true);
		pf2 = new JRadioButton("Landscape");
		pf2.setActionCommand("2");
		pf.add(pf1);
		pf.add(pf2);
		pf1.setBackground(bgColor);
		pf2.setBackground(bgColor);
		
		cp.setBackground(bgColor);
		
		preview.addActionListener(this);
		print.addActionListener(this);
		
		prp.setBackground(bgColor);
		hold.setBorder(BorderFactory.createLineBorder(Color.black, 2));
		hold.setBackground(bgColor);
		hold.setLayout(new GridBagLayout());
		
		GridBagConstraints c1 = new GridBagConstraints();
		
		c1.insets = new Insets(15, 45, 0, 5);
		c1 = buildConstraints(c1, 0, 0, 2, 1, 0.0, 0.0);
		hold.add(pf1, c1);
		
		c1.insets = new Insets(2, 45, 0, 5);
		c1 = buildConstraints(c1, 0, 1, 2, 1, 0.0, 0.0);
		hold.add(pf2, c1);
		
		c1.insets = new Insets(25, 5, 0, 5);
		c1 = buildConstraints(c1, 0, 2, 1, 1, 0.0, 0.0);
		hold.add(xsl, c1);
		
		c1.insets = new Insets(25, 5, 0, 35);
		c1 = buildConstraints(c1, 1, 2, 1, 1, 0.0, 0.0);
		hold.add(xsp, c1);
		
		c1.insets = new Insets(5, 5, 0, 5);
		c1 = buildConstraints(c1, 0, 3, 1, 1, 0.0, 0.0);
		hold.add(ysl, c1);
		
		c1.insets = new Insets(15, 5, 0, 35);
		c1 = buildConstraints(c1, 1, 3, 1, 1, 0.0, 0.0);
		hold.add(ysp, c1);
		
		c1.insets = new Insets(0, 25, 0, 5);
		c1 = buildConstraints(c1, 0, 4, 2, 1, 0.0, 0.0);
		hold.add(cp, c1);
		
		c1.insets = new Insets(20, 35, 0, 35);
		c1 = buildConstraints(c1, 0, 5, 2, 1, 0.0, 0.0);
		hold.add(ftp, c1);
		
		c1.insets = new Insets(25, 35, 0, 35);
		c1 = buildConstraints(c1, 0, 6, 2, 1, 0.0, 0.0);
		hold.add(preview, c1);
		
		c1.insets = new Insets(5, 35, 25, 35);
		c1 = buildConstraints(c1, 0, 7, 2, 1, 0.0, 0.0);
		hold.add(print, c1);
		
		add(hold);
		add(prp);
	}
	
	Component getTableComponent(JTable table){
		Box box = new Box(BoxLayout.Y_AXIS);
		JTableHeader jth = table.getTableHeader();
		
		Dimension dh = jth.getPreferredSize();
		Dimension dt = table.getPreferredSize();
		Dimension db = new Dimension(dh.width, dh.height + dt.height);
		box.setPreferredSize(db);
		
		jth.setBorder(new LineBorder(Color.black, 1){
					  public Insets getBorderInsets(Component c){
					  return new Insets(2, 2, 2, 2);
					  }
					  });
		
		table.setBorder(new PartialLineBorder(false, true, false, false));
		
		box.add(jth);
		box.add(table);
		
		// visualize table for getting non-zero sizes(width, height)
		workFrame = new JFrame();
		workFrame.getContentPane().add(box);
		workFrame.pack();
		workFrame.setVisible(true);
		
		return box;
	}
	
	GridBagConstraints buildConstraints(GridBagConstraints gbc, int gx, int gy,
										int gw, int gh, double wx, double wy){
		gbc.gridx = gx;
		gbc.gridy = gy;
		gbc.gridwidth = gw;
		gbc.gridheight = gh;
		gbc.weightx = wx;
		gbc.weighty = wy;
		gbc.fill = GridBagConstraints.BOTH;
		return gbc;
	}
	
	public class PreviewPage extends JPanel{
		int x1, y1, l1, h1, x2, y2;
		Image image;
		
		public PreviewPage(){
			setPreferredSize(new Dimension(460, 460));
			setBorder(BorderFactory.createLineBorder(Color.black, 2));
		}
		
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			
			// PORTRAIT
			if(pageFormat.getOrientation() == PageFormat.PORTRAIT){
				g.setColor(Color.black);
				g.drawRect(60, 10, 340, 440);
				x1 = (int)Math.rint(((double)pageFormat.getImageableX() / 72) * 40);
				y1 = (int)Math.rint(((double)pageFormat.getImageableY() / 72) * 40);
				l1 
				= (int)Math.rint(((double)pageFormat.getImageableWidth() / 72) * 40);
				h1 
				= (int)Math.rint(((double)pageFormat.getImageableHeight() / 72) * 40);
				g.setColor(Color.red);
				g.drawRect(x1 + 60, y1 + 10, l1, h1);
				// setScales(); // commenting-out suppresses too frequent paint updates
				x2 = (int)Math.rint((double)l1 / xScaleDivisor);
				y2 = (int)Math.rint(((double)l1 * hw) / yScaleDivisor);
				image = pcImage.getScaledInstance(x2, y2, Image.SCALE_AREA_AVERAGING);
				g.drawImage(image, x1 + 60, y1 + 10, this);
			}
			// LANDSCAPE
			else{
				g.setColor(Color.black);
				g.drawRect(10, 60, 440, 340);
				x1 = (int)Math.rint(((double)pageFormat.getImageableX() / 72) * 40);
				y1 = (int)Math.rint(((double)pageFormat.getImageableY() / 72) * 40);
				l1 
				= (int)Math.rint(((double)pageFormat.getImageableWidth() / 72) * 40);
				h1 
				= (int)Math.rint(((double)pageFormat.getImageableHeight() / 72) * 40);
				g.setColor(Color.red);
				g.drawRect(x1 + 10, y1 + 60, l1, h1);
				// setScales();
				x2 = (int)Math.rint((double)l1 / xScaleDivisor);
				y2 = (int)Math.rint(((double)l1 * hw) / yScaleDivisor);
				image = pcImage.getScaledInstance(x2, y2, Image.SCALE_AREA_AVERAGING);
				g.drawImage(image, x1 + 10, y1 + 60, this);
			}
		} 
	}
	
	public void actionPerformed(ActionEvent e){
		if(e.getSource() == ftp){
		}
		if(e.getSource() == preview){
			setProperties();
		}
		if(e.getSource() == print){
			doPrint();
		}
	}
	
	public void setProperties(){
		if(pf1.isSelected()){
			pageFormat.setOrientation(PageFormat.PORTRAIT);
		}
		else if(pf2.isSelected()){
			pageFormat.setOrientation(PageFormat.LANDSCAPE);
		}
		setScales();
		prp.repaint();
	}
	
	public void setScales(){
		try{
			xScaleDivisor = ((Double)xsp.getValue()).doubleValue();
			yScaleDivisor = ((Double)ysp.getValue()).doubleValue();
		}
		catch (NumberFormatException e) {
		}
	}
	
	public void doPrint(){
		PrintThis();
	}
	
	public void PrintThis(){
		PrinterJob printerJob = PrinterJob.getPrinterJob();
		Book book = new Book();
		book.append(new PrintPage(), pageFormat);
		printerJob.setPageable(book);
		boolean doPrint = printerJob.printDialog();
		if (doPrint) {
			try {
				printerJob.print();
			}
			catch (PrinterException exception) {
				System.err.println("Printing error: " + exception);
			}
		}
	}
	
	//public class PrintPage implements Printable{
	class PrintPage implements Printable{
		
		public int print(Graphics g, PageFormat format, int pageIndex) {
			Graphics2D g2D = (Graphics2D) g;
			g2D.translate(format.getImageableX (), format.getImageableY ());
			//      disableDoubleBuffering(mp);
			System.out.println("get i x " + format.getImageableX ());
			System.out.println("get i x " + format.getImageableY ());
			System.out.println("getx: " + format.getImageableWidth() );
			System.out.println("getx: " + format.getImageableHeight() );
			// scale to fill the page
			double dw = format.getImageableWidth();
			double dh = format.getImageableHeight();
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			setScales();
			double xScale = dw / (1028 / xScaleDivisor);
			double yScale = dh / (768 / yScaleDivisor);
			double scale = Math.min(xScale, yScale);
			System.out.println("" + scale);
			g2D.scale( xScale, yScale);
			targetComponent.paint(g);
			//      enableDoubleBuffering(mp);
			return Printable.PAGE_EXISTS;
		}
		
		public void disableDoubleBuffering(Component c) {
			RepaintManager currentManager = RepaintManager.currentManager(c);
			currentManager.setDoubleBufferingEnabled(false);
		}
		
		public void enableDoubleBuffering(Component c) {
			RepaintManager currentManager = RepaintManager.currentManager(c);
			currentManager.setDoubleBufferingEnabled(true);
		}
	}
	
	public static void print(Component _something)
	{
		JFrame frame2 = new JFrame();
		frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container con2 = frame2.getContentPane();

		PrintPreview pp = new PrintPreview(_something);
		con2.add(pp, BorderLayout.CENTER);
		frame2.pack();
		frame2.setVisible(true);
		frame2.toFront();
	}
	
	public static void main(String[] args){
		JFrame frame1 = new JFrame();
		frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container con1 = frame1.getContentPane();
		
		JFrame frame2 = new JFrame();
		frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container con2 = frame2.getContentPane();
		
		JTable table = new JTable(20, 7);
		JScrollPane jsp = new JScrollPane(table);
		
		con1.add(jsp, BorderLayout.CENTER);
		frame1.pack();
		frame1.setVisible(true);
		
		PrintPreview pp = new PrintPreview(table);
		con2.add(pp, BorderLayout.CENTER);
		frame2.pack();
		frame2.setVisible(true);
		frame2.toFront();
	}
}

class PartialLineBorder extends AbstractBorder{
	boolean top, left, bottom, right;
	
	public PartialLineBorder(boolean t, boolean l, boolean b, boolean r){
		top = t;
		left = l;
		bottom = b;
		right = r;
	}
	
	public boolean isBorderOpaque(){
		return true;
	}
	
	public Insets getBorderInsets(Component c){
		return new Insets(2, 2, 2, 2);
	}
	
	public void paintBorder
	(Component c, Graphics g, int x, int y, int width, int height){
		
		Graphics2D g2 = (Graphics2D)g;
		g2.setStroke(new BasicStroke(1.0f));
		
		if (top){
			g2.drawLine(x, y, x + width, y);
		}
		if (left){
			g2.drawLine(x, y, x, y + height);
		}
		if (bottom){
			g2.drawLine(x, y + height, x + width, y + height);
		}
		if (right){
			g2.drawLine(x + width, y, x + width, y + height);
		}
	}
}

*/
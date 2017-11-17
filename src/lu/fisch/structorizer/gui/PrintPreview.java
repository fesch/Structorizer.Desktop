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
 *      Description:    This class provides a somewhat configurable print preview with print function.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2008.01.27      First Issue
 *      Kay Gürtzig     2017.11.06      Enh. #456 Orientation switching reactivated, margin configuration added.
 *
 ******************************************************************************************************
 *
 *      Comment:		Several parts of code have been copied from different forums on the net.
 *
 ******************************************************************************************************///
 
import lu.fisch.structorizer.locales.LangDialog;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.print.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
	// START KGU#458 2017-11-06: Enh. #456 - provide minimum adaptability
	protected JPanel marginPanel;
	protected JLabel lblMarginX;
	protected JLabel lblMarginY;
	protected JSpinner spnMarginX;
	protected JSpinner spnMarginY;
	// END KGU#458 2017-11-06
	
	// Bob
	/** Page width in inch/72 */
	protected int m_wPage;
	/** Page height in inch/72 */
	protected int m_hPage;
	// START KGU#458 2017-11-06: Enh. #456 - provide minimum adaptability
	/** Horizontal margins in inch/72 */
	protected int m_xMargin;
	/** Vertical margins in inch/72 */
	protected int m_yMargin;
	/** A flag to prevent overlapping preview refresh */
	protected boolean allowMarginRefresh = true;
	// END KGU#458 2017-11-06
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
		
		// START KGU#458 2017-11-06: Enh. #456 - provide minimum adaptability
		marginPanel = new JPanel();
		lblMarginX = new JLabel("horiz. Margin:");
		lblMarginY = new JLabel("vert. Margin:");
		SpinnerModel spnModelX = new SpinnerNumberModel(10, 0, 72, 1);
		SpinnerModel spnModelY = new SpinnerNumberModel(10, 0, 72, 1);
		spnMarginX = new JSpinner(spnModelX);
		spnMarginY = new JSpinner(spnModelY);
		// END KGU#458 2017-11-06

		// btnOrientation.setVisible(false);

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
				
				int xPos = 0;
				
				buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
				buttonBar.setLayout(new GridBagLayout());
				// START KGU#458 2017-11-06: Enh. #456 - Fifth column for margin panel inserted
				((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 0, 0, 85, 80};
				((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 0.0, 0.0};
				// END KGU#458 2017-11-06

				buttonBar.add(m_cbScale, new GridBagConstraints(xPos++, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- btnOrientation ----
				btnOrientation.setText("Toggle Orientation");
				buttonBar.add(btnOrientation, new GridBagConstraints(xPos++, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));
				
				// START KGU#458 2017-11-06: Enh. #456 - Fifth column for margin panel inserted
				//---- marginPanel ----
				marginPanel.setLayout(new GridLayout(1, 0, 5, 0));
				marginPanel.add(lblMarginX);
				marginPanel.add(spnMarginX);
				marginPanel.add(lblMarginY);
				marginPanel.add(spnMarginY);
				buttonBar.add(marginPanel, new GridBagConstraints(xPos++, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));
				// END KGU#458 2017-11-06

				//---- btnOK ----
				btnOK.setText("OK");
				buttonBar.add(btnOK, new GridBagConstraints(xPos++, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- btnCancel ----
				btnCancel.setText("Cancel");
				buttonBar.add(btnCancel, new GridBagConstraints(xPos++, 0, 1, 1, 0.0, 0.0,
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
		
		// add the KEY listeners
		// Basic key listener for all potential key event generators - handles Esc character
		KeyListener keyListener = new KeyListener()
		{
			public void keyPressed(KeyEvent e) 
			{
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					setVisible(false);
				}
			}
			
			// Ignored events
			public void keyReleased(KeyEvent ke) {} 
			public void keyTyped(KeyEvent kevt) {}
		};
		btnCancel.addKeyListener(keyListener);
		btnOrientation.addKeyListener(keyListener);
		this.addKeyListener(keyListener);
		// START KGU#458 2017-11-06: Enh. #456 - let the spinners react to Escape
		((JSpinner.DefaultEditor)spnMarginX.getEditor()).getTextField().addKeyListener(keyListener);
		((JSpinner.DefaultEditor)spnMarginY.getEditor()).getTextField().addKeyListener(keyListener);
		// END KGU#458 2017-11-06
		
		// OK button
		ActionListener lst = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// START KGU#458 2017-11-06: Enh. #456 - Duplicate code (copy-and-paste trap) outsourced					
				print();
				// END KGU#458 2017-11-06
			}
		};
		btnOK.addActionListener(lst);
		
		// add the KEY-listeners 2
		/** Extended key listener for the OK button - handles Esc and Enter characters */
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
					// START KGU#458 2017-11-06: Enh. #456 - Duplicate code (copy-and-paste trap) outsourced					
//					try
//					{
//						PrinterJob prnJob = PrinterJob.getPrinterJob();
//						prnJob.setPrintable(m_target,pp_pf);
//						if (prnJob.printDialog()) 
//						{
//							setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//							prnJob.print();
//							setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//							dispose();
//						}
//					}
//					catch (PrinterException ex)
//					{
//						ex.printStackTrace();
//						System.err.println("Printing error: "+ex.toString());
//					}
					print();
					// END KGU#458 2017-11-06
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
				// START KGU#458 2017-11-06: Enh. #456 - Orientation switching is to swap the margins, too					
				// Exchange the margin values
				int temp = m_xMargin;
				m_xMargin = m_yMargin;
				m_yMargin = temp;
				allowMarginRefresh = false;
				spnMarginX.getModel().setValue(m_xMargin);
				spnMarginY.getModel().setValue(m_yMargin);
				allowMarginRefresh = true;
				// END KGU#458 2017-11-05
				// Now refresh the preview again
				Thread runner = new Thread(PrintPreview.this);
				runner.start();
			}
		};
		btnOrientation.addActionListener(orient);
		
		// START KGU#458 2017-11-06: Enh. #456 - spinner value changes should immediately be reflected in the preview
		// It is important that this change listener is only added after the adjustment of the spinners has been finished!
		ChangeListener marginListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				Object source = evt.getSource();
				if (source instanceof JSpinner) {
					SpinnerModel numberModel = ((JSpinner)source).getModel();
					int newMargin = (Integer)numberModel.getValue();
					if (source == spnMarginX) {
						m_xMargin = newMargin;
					}
					else {
						m_yMargin = newMargin;
					}
					if (allowMarginRefresh) {
						EventQueue.invokeLater(PrintPreview.this);
					}
				}
			}
			
		};
		// END KGU#456 2017-11-05

		// preview
		//m_preview = new PreviewContainer();	// --> moved to generatePreviewPages(int)
		
		PrinterJob prnJob = PrinterJob.getPrinterJob();
		pp_pf = prnJob.defaultPage();
		if (pp_pf.getHeight() == 0 || pp_pf.getWidth() == 0)
		{
			System.err.println("Unable to determine default page size");
			return;
		}

		m_wPage = (int)(pp_pf.getWidth());
		m_hPage = (int)(pp_pf.getHeight());
		// START KGU#458 2017-11-06: Enh. #456 - Set the spinner values from the actual default page					
		m_xMargin = (int)(pp_pf.getImageableX());
		m_yMargin = (int)(pp_pf.getImageableY());
		spnMarginX.getModel().setValue(m_xMargin);
		spnMarginY.getModel().setValue(m_yMargin);
		
		// START KGU#458 2017-11-07: Issue #456 - duplicate code consolidated
//		Paper paper = pp_pf.getPaper();
//		// Make the margins symmetric (this is good enough)
//		paper.setImageableArea(m_xMargin, m_yMargin, m_wPage - 2*m_xMargin, m_hPage-2*m_yMargin);
//		pp_pf.setPaper(paper);
//		// END KGU#458 2017-11-06
//
//		int scale = 100;
//		int w = (int)(m_wPage * scale / 100);
//		int h = (int)(m_hPage * scale / 100);
//		
//		int pageIndex = 0;
//		try
//		{
//			while (true)
//			{
//				BufferedImage img = new BufferedImage(m_wPage, m_hPage, BufferedImage.TYPE_INT_RGB);
//				Graphics g = img.getGraphics();
//				g.setColor(Color.white);
//				g.fillRect(0, 0, m_wPage, m_hPage);
//				// START KGU#458 2017-11-06: Enh. #456 - Show the margins as gray lines
//				g.setColor(Color.decode("0xD0D0D0"));
//				g.drawLine(0, m_yMargin, m_wPage, m_yMargin);
//				g.drawLine(0, m_hPage - m_yMargin, m_wPage, m_hPage - m_yMargin);
//				g.drawLine(m_xMargin, 0, m_xMargin, m_hPage);
//				g.drawLine(m_wPage - m_xMargin, 0, m_wPage - m_xMargin, m_hPage);
//				// END KGU#458 2017-11-06
//				if (m_target.print(g, pp_pf, pageIndex) != Printable.PAGE_EXISTS) {
//					break;
//				}
//				PagePreview pp = new PagePreview(w, h, img);
//				m_preview.add(pp);
//				pageIndex++;
//			}
//		}
//		catch (PrinterException e)
//		{
//			e.printStackTrace();
//			System.err.println("Printing error: "+e.toString());
//		}
		generatePreviewPages(100);
		// END KGU#458 2017-11-07
		
		// START KGU#287 2017-01-09: Issue #81 / bugfix #330: GUI scaling
		GUIScaler.rescaleComponents(this);
		// END KGU#287 2017-01-09
		
		// START KGU#458 2017-11-06: Enh. #456 - Now spinner value changes should immediately be reflected in the preview
		spnMarginX.addChangeListener(marginListener);
		spnMarginY.addChangeListener(marginListener);
		// END KGU#456 2017-11-05
		
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
		
		// START KGU#458 2017-11-07: Issue #456 duplicate code consolidated
		generatePreviewPages(scale);
		// END KGU#458 2017-11-07
		
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
		// START KGU#458 2017-11-06: Enh. #456 - Now adapt the dialog width to the paper orientation
		setSize(new Dimension(getPreferredSize().width, getSize().height));
		// END KGU#458 2017-11-06
	}

	/**
	 * Sets field {@link #m_preview} to a new {@link PreviewContainer}, generates the target pages and adds them to it. 
	 * @param scale - a rough scale factor for the entire preview (not just its content!) in percent
	 */
	protected void generatePreviewPages(int scale) {
		int w = (int)(m_wPage * scale / 100);
		int h = (int)(m_hPage * scale / 100);
		
		m_preview = new PreviewContainer();
		
		int pageIndex = 0;
		try
		{
			// START KGU#458 2017-11-06: Enh. #456 - Now adopt the margins - stunningly this is orientation-dependent
			Paper paper = pp_pf.getPaper();
			if (pp_pf.getOrientation() == PageFormat.PORTRAIT) {
				paper.setImageableArea(m_xMargin, m_yMargin, m_wPage-2*m_xMargin, m_hPage-2*m_yMargin);
			}
			else {
				paper.setImageableArea(m_yMargin, m_xMargin, m_hPage-2*m_yMargin, m_wPage - 2*m_xMargin);				
			}
			pp_pf.setPaper(paper);
			// END KGU#458 2017-11-06
			while (true)
			{
				BufferedImage img = new BufferedImage(m_wPage, m_hPage, BufferedImage.TYPE_INT_RGB);
				Graphics g = img.getGraphics();
				g.setColor(Color.white);
				g.fillRect(0, 0, m_wPage, m_hPage);
				// START KGU#458 2017-11-06: Enh. #456 - Show the margins as gray lines
				g.setColor(Color.decode("0xD0D0D0"));
				g.drawLine(0, m_yMargin, m_wPage, m_yMargin);
				g.drawLine(0, m_hPage - m_yMargin, m_wPage, m_hPage - m_yMargin);
				g.drawLine(m_xMargin, 0, m_xMargin, m_hPage);
				g.drawLine(m_wPage - m_xMargin, 0, m_wPage - m_xMargin, m_hPage);
				// END KGU#458 2017-11-06
				if (m_target.print(g, pp_pf, pageIndex) != Printable.PAGE_EXISTS) {
					break;
				}
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
	}
	
	// START KGU#458 2017-11-06: Enh. #456 - common action of both Enter key and Ok button pressing
	/** Actually prints the diagram according to the customized preview */
	protected void print()
	{
		try
		{
			// Use default printer, no dialog
			PrinterJob prnJob = PrinterJob.getPrinterJob();
                                
			// get the default page format
			PageFormat pf0 = prnJob.defaultPage();
			// clone it
			PageFormat pf1 = (PageFormat) pf0.clone();
			Paper p = pf0.getPaper();
			// set to given margins (in theory, we could just use the margins since all PageFormats here are derived
			// from prnJob.defaultPage(), but well be prepared for some proportional scaling (doesn't cause harm if
			// not necessary)
			double marginLeft = pp_pf.getImageableX()/pp_pf.getWidth();
			double marginWidth = pp_pf.getImageableWidth()/pp_pf.getWidth();
			double marginTop = pp_pf.getImageableY()/pp_pf.getHeight();
			double marginHeight = pp_pf.getImageableHeight()/pp_pf.getHeight();
			p.setImageableArea(marginLeft * pf0.getWidth(), marginTop * pf0.getHeight(), marginWidth * pf0.getWidth(), marginHeight * pf0.getHeight());
			pf1.setPaper(p);
			// Also adopt the preview orientation
			pf1.setOrientation(pp_pf.getOrientation());
			// let the printer validate it
			PageFormat pf2 = prnJob.validatePage(pf1);

			prnJob.setPrintable(m_target,pf2);
			//prnJob.setPrintable(m_target, pp_pf);
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
			System.err.println("Printing error: " + ex.toString());
		}
		
	}
	// END KGU#458 2017-11-06
	
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

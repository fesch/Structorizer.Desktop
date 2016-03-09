/*
    Structorizer :: Arranger
    A little tool which you can use to arrange Nassi-Schneiderman Diagrams (NSD)

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

package lu.fisch.structorizer.arranger;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    This class offers an opportunity to graphically arrange several NSD diagrams
 *                      within one and the same drawing area. While related to owned Structorizers, the
 *                      diagrams will fully and synchronously reflect all status changes (selection,
 *                      execution, ...)
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2009.08.18		First Issue
 *      Kay Gürtzig     2015.10.18		Transient WindowsListener added enabling Surface to have dirty diagrams saved before exit
 *      Kay Gürtzig     2015.11.17		Remove button added (issue #35 = KGU#85)
 *      Kay Gürtzig     2015.11.19		Converted into a singleton (enhancement request #9 = KGU#2)
 *      Kay Gürtzig     2015-11-24		Pin button added (issue #35, KGU#88)
 *      Kay Gürtzig     2015-11-30		Remove action now also achievable by pressing del button (issue #35, KGU#88)
 *      Kay Gürtzig     2015-12-21		Two new buttons for saving and loading arrangements (issue #62, KGU#110)
 *      Kay Gürtzig     2016-01-05		Icons for saving and loading arrangements replaced by fitting ones
 *      Kay Gürtzig     2016-03-08		Bugfix #97: Methods for drawing info invalidation added (KGU#155) 
 *      Kay Gürtzig     2016.03.08      Method clearExecutionStatus and btnSetCovered added (for Enhancement #77)
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.executor.IRoutinePool;
import lu.fisch.structorizer.gui.Mainform;

/**
 *
 * @author robertfisch
 */
public class Arranger extends javax.swing.JFrame implements WindowListener, KeyListener, IRoutinePool
{
    // START KGU#2 2015-11-19: Enh. #9 - Converted into a singleton class
    //** Creates new form Arranger */
    //public Arranger() {
    //    initComponents();
    //}
	private static Arranger mySelf = null;
    /** Returns the Arranger instance (if it is to be created then it will be as a dependent frame)
     */
	public static Arranger getInstance()
	{
		return getInstance(false);
	}
    /** Returns the Arranger instance
     * @param standalone - if true then the instance will exit on close otherwise only dispose (works only on actual creation)
     */
	public static Arranger getInstance(boolean standalone)
	{
		if (mySelf == null)
		{
			mySelf = new Arranger(standalone);
		}
		return mySelf;
	}
	
	// START KGU#155 2016-03-08: added for bugfix #97
	/**
	 * Allows to find out whether an Arranger instance is created without creating it
	 * @return true iff there is already an Arranger instance
	 */
	public static boolean hasInstance()
	{
		return mySelf != null;
	}
	// END KGU#155 2016-03-08
	
    /** Creates new form Arranger */
    private Arranger(boolean standalone) {
        initComponents();
        setDefaultCloseOperation(standalone ? JFrame.EXIT_ON_CLOSE : JFrame.DISPOSE_ON_CLOSE);
    }
    
    /**
     * Places the passed-in root at a free space on the Arranger surface if it hasn't been placed there
     * already. Relates the given frame to it if it is a Mainform instance.
     * @param root - The diagram root to be added to the Arranger
     * @param frame - potentially an associable Mainform (Structorizer)
     */
    public void addToPool(Root root, JFrame frame)
    {
    	if (frame instanceof Mainform)
    	{
    		surface.addDiagram(root, (Mainform) frame);
    	}
    	else
    	{
    		surface.addDiagram(root);
    	}
    }
    // END KGU#2 2015-11-19
	
	// START KGU#155 2016-03-08: Bugfix #97 extension
	/**
	 * Invalidates the cached prepareDraw info of all diagrams residing here
	 * (to be called on events with heavy impact on the size or shape of some
	 * Elements)
	 * @param _exceptDiagr the hash code of a lu.fisch.structorizer.gui.Diagram
	 * that is not to be invoked (to avoid recursion)
	 */
	public void resetDrawingInfo(int _exceptDiagr)
	{
		surface.resetDrawingInfo(_exceptDiagr);
	}
	// END KGU#155 2016-03-08

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolbar = new javax.swing.JToolBar();
        btnExportPNG = new javax.swing.JButton();
        btnAddDiagram = new javax.swing.JButton();
        // START KGU#85 2015-11-17
        btnRemoveDiagram = new javax.swing.JButton();
        // END KGU#85 2015-11-17
        // START KGU#88 2015-11-24
        btnPinDiagram = new javax.swing.JButton();
        // END KGU#88 2015-11-24
        // START KGU#110 2015-12-20: Enh. #62
        btnSaveArr = new javax.swing.JButton();
        btnLoadArr = new javax.swing.JButton();
        // END KGU#110 2015-12-20
        // START KGU#117 2016-03-09: Env. #77 - test coverage
        btnSetCovered = new javax.swing.JButton();
        // END KGU#117 2016-03-09

        
        surface = new lu.fisch.structorizer.arranger.Surface();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Structorizer Arranger");
        // START KGU#2 2015-11-24: Replace the Java default icon
        try
        {
        setIconImage(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/arranger48.png")).getImage());
        }
        catch (Error error)
        {
        	System.err.println(error.getMessage());
        }
        // END KGU#2 2015-11-24

        toolbar.setFloatable(false);
        toolbar.setRollover(true);

        btnExportPNG.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/032_make_bmp.png"))); // NOI18N
        btnExportPNG.setText("PNG Export");
        btnExportPNG.setFocusable(false);
        btnExportPNG.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExportPNG.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnExportPNG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportPNGActionPerformed(evt);
            }
        });
        toolbar.add(btnExportPNG);

        // START KGU#110 2015-12-20: Enh. #62
        // FIXME: Icon somewhat too little
        btnSaveArr.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/SaveFile20x20.png"))); // NOI18N
        btnSaveArr.setText("Save List");
        btnSaveArr.setFocusable(false);
        btnSaveArr.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSaveArr.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSaveArr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveArrActionPerformed(evt);
            }
        });
        toolbar.add(btnSaveArr);

        // FIXME: Icon somewhat too little
        btnLoadArr.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/OpenFile20x20.png"))); // NOI18N
        btnLoadArr.setText("Load List");
        btnLoadArr.setFocusable(false);
        btnLoadArr.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnLoadArr.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnLoadArr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoadArrActionPerformed(evt);
            }
        });
        toolbar.add(btnLoadArr);
        // END KGU#110 2015-12-20

        btnAddDiagram.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/101_diagram_new.png"))); // NOI18N
        btnAddDiagram.setText("New Diagram");
        btnAddDiagram.setFocusable(false);
        btnAddDiagram.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddDiagram.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddDiagram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddDiagramActionPerformed(evt);
            }
        });
        toolbar.add(btnAddDiagram);

        // START KGU#88 2015-11-24: Protect a diagram against replacement
        btnPinDiagram.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/pin_blue_14x20.png"))); // NOI18N
        btnPinDiagram.setText("Pin Diagram");
        btnPinDiagram.setToolTipText("Pin a diagram to make it immune against replacement.");
        btnPinDiagram.setFocusable(false);
        btnPinDiagram.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnPinDiagram.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnPinDiagram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPinDiagramActionPerformed(evt);
            }
        });
        toolbar.add(btnPinDiagram);
        // END KGU#88 2015-11-24

        // START KGU#117 2016-03-09: Enh. #77 - Mark a subroutine as test-covered
        btnSetCovered.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/setCovered20x20.png"))); // NOI18N
        btnSetCovered.setText("Set Covered");
        btnSetCovered.setToolTipText("Mark the routine diagram as test-covered for subroutine calls to it.");
        btnSetCovered.setFocusable(false);
        btnSetCovered.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSetCovered.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSetCovered.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetCoveredActionPerformed(evt);
            }
        });
        toolbar.add(btnSetCovered);
        // END KGU#117 2016-03-09

        // START KGU#85 2015-11-17: New opportunity to drop the selected diagram 
        btnRemoveDiagram.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/100_diagram_drop.png"))); // NOI18N
        btnRemoveDiagram.setText("Drop Diagram");
        btnRemoveDiagram.setFocusable(false);
        btnRemoveDiagram.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRemoveDiagram.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnRemoveDiagram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveDiagramActionPerformed(evt);
            }
        });
        toolbar.add(btnRemoveDiagram);
        // END KGU#85 2015-11-17

        getContentPane().add(toolbar, java.awt.BorderLayout.NORTH);

        surface.setBackground(new java.awt.Color(255, 255, 255));

//        org.jdesktop.layout.GroupLayout surfaceLayout = new org.jdesktop.layout.GroupLayout(surface);
//        surface.setLayout(surfaceLayout);
//        surfaceLayout.setHorizontalGroup(
//            surfaceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
//            .add(0, 420, Short.MAX_VALUE)
//        );
//        surfaceLayout.setVerticalGroup(
//            surfaceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
//            .add(0, 254, Short.MAX_VALUE)
//        );
        
        // START KGU#85 2015-11-18
        //getContentPane().add(surface, java.awt.BorderLayout.CENTER);
        scrollarea = new JScrollPane(surface);
        //scrollarea.setBackground(Color.LIGHT_GRAY);
        scrollarea.getViewport().putClientProperty("EnableWindowBlit", Boolean.TRUE);
        scrollarea.setWheelScrollingEnabled(true);
        scrollarea.setDoubleBuffered(true);
        scrollarea.setBorder(BorderFactory.createEmptyBorder());
        //scrollarea.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        //scrollarea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        //scrollarea.setViewportView(surface);
        getContentPane().add(scrollarea, java.awt.BorderLayout.CENTER);
        // END KGU#85 2015-11-18
        
        this.addKeyListener(this);

        // START KGU#49 2015-10-18: On closing the Arranger window, the dependent Mainforms must get a chance to save their stuff!
        /******************************
         * Set onClose event
         ******************************/
        addWindowListener(new WindowAdapter() 
        {  
        	@Override
        	public void windowClosing(WindowEvent e) 
        	{
        		// START KGU#2 2015-11-19: Only necessary if I am going to exit
        		if (mySelf.getDefaultCloseOperation() == JFrame.EXIT_ON_CLOSE)
        		// END KGU# 2015-11-19
        			surface.saveDiagrams();	// Allow user to save dirty diagrams
        	}  

        	@Override
        	public void windowOpened(WindowEvent e) 
        	{  
        	}  

        	@Override
        	public void windowActivated(WindowEvent e)
        	{  
        	}

        	@Override
        	public void windowGainedFocus(WindowEvent e) 
        	{  
        	}  
        });
        // END KGU#49 2015-10-18

        // START KGU#117 2016-03-09: New for Enh. #77
        this.doButtons();
        // END KGU#117 2016-03-09
        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnExportPNGActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnExportPNGActionPerformed
    {//GEN-HEADEREND:event_btnExportPNGActionPerformed
        surface.exportPNG(this);
    }//GEN-LAST:event_btnExportPNGActionPerformed

    private void btnAddDiagramActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnAddDiagramActionPerformed
    {//GEN-HEADEREND:event_btnAddDiagramActionPerformed
        surface.addDiagram(new Root());
    }//GEN-LAST:event_btnAddDiagramActionPerformed

    // START KGU#85 2015-11-17
    private void btnRemoveDiagramActionPerformed(java.awt.event.ActionEvent evt)
    {
        surface.removeDiagram();
    }
    // END KGU#85 2015-11-17

    // START KGU#88 2015-11-24
    private void btnPinDiagramActionPerformed(java.awt.event.ActionEvent evt)
    {
        surface.togglePinned();
    }
    // END KGU#88 2015-11-24

    // START KGU#110 2015-12-20: Enh. #62 Possibility to save and load arrangements
    private void btnSaveArrActionPerformed(java.awt.event.ActionEvent evt)
    {
        surface.saveArrangement(this);
    }

    private void btnLoadArrActionPerformed(java.awt.event.ActionEvent evt)
    {
        surface.loadArrangement(this);
    }
    // END KGU#110 2015-12-20

    // START KGU#117 2016-03-09: Enh. #77
    private void btnSetCoveredActionPerformed(java.awt.event.ActionEvent evt)
    {
        surface.setCovered(this);
    }
    // END KGU#88 2016-03-09

    /**
     * Starts the Arranger as application
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
            	// START KGU#2 2015-11-19: Converted into a singleton
                //new Arranger().setVisible(true);
                getInstance(true).setVisible(true);
                // END KGU#2 2015-11-19
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddDiagram;
    // START KGU#85 2015-11-17
    private javax.swing.JButton btnRemoveDiagram;
    // END KGU#85 2015-11-17
    // START KGU#88 2015-11-24
    private javax.swing.JButton btnPinDiagram;
    // END KGU#88 2015-11-24
    private javax.swing.JButton btnExportPNG;
    // START KGU#110 2015-12-20: Enh. #62 a possibility to save and load arrangements was requested
    private javax.swing.JButton btnSaveArr;
    private javax.swing.JButton btnLoadArr;
    // END KGU#110 2015-12-20
    // START KGU#117 2016-03-09: Env. #77 - test coverage
    private javax.swing.JButton btnSetCovered;
    // END KGU#117 2016-03-09
    
    private lu.fisch.structorizer.arranger.Surface surface;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables
    // START KGU#85 2015-11-18
    private JScrollPane scrollarea;
    // END KGU#85 2015-11-18

    public void windowOpened(WindowEvent e)
    {
    }

    public void windowClosing(WindowEvent e)
    {
    }

    public void windowClosed(WindowEvent e)
    {
    }

    public void windowIconified(WindowEvent e)
    {
    }

    public void windowDeiconified(WindowEvent e)
    {
    }

    public void windowActivated(WindowEvent e)
    {
        surface.repaint();
    }

    public void windowDeactivated(WindowEvent e)
    {
    }
    
    // START KGU#85 2015-11-30: For convenience, the delete button may also be used to drop a diagram now
	@Override
	public void keyPressed(KeyEvent ev) {
		// TODO Auto-generated method stub
		if (ev.getSource() == this && ev.getKeyCode() == KeyEvent.VK_DELETE)
		{
			surface.removeDiagram();
		}
	}
	// END KGU#85 2015-11-30

	@Override
	public void keyReleased(KeyEvent ev) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent ev) {
		// TODO Auto-generated method stub
	}
	// END KGU#2 2015-11-30

	// START KGU#2 2015-11-24
	@Override
	public Vector<Root> findRoutinesByName(String rootName) {
		return surface.findRoutinesByName(rootName);
	}
	@Override
	public Vector<Root> findRoutinesBySignature(String rootName, int argCount) {
		return surface.findRoutinesBySignature(rootName, argCount);
	}
	// END KGU#2 2015-11-24
	
	// START KGU#117 2016-03-08: Introduced on occasion of Enhancement #77
	@Override
	public void clearExecutionStatus()
	{
		doButtons();
		surface.clearExecutionStatus();
	}
	
	public void doButtons()
	{
		btnSetCovered.setEnabled(Element.E_TESTCOVERAGEMODE);
	}
	// END KGU#117 2016-03-08
	
}

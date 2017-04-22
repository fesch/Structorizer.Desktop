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

package lu.fisch.structorizer.executor;

/*
 ******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    This class represents the GUI controlling the execution of a diagram.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2009.05.18      First Issue
 *      Kay Gürtzig     2015.11.05      Enhancement allowing to adopt edited values from Control (KGU#68)
 *      Kay Gürtzig     2015.11.14      New controls to display the call level for enhancement #9 (KGU#2)
 *      Kay Gürtzig     2016.03.06      Enh. #77: New checkboxes for test coverage tracking mode (KGU#117)
 *      Kay Gürtzig     2016.03.13      Enh. #124 (KGU#156): Runtime data collection generalised
 *      Kay Gürtzig     2016.03.17      Enh. #133 (KGU#159): Stack trace may now be shown in paused mode
 *      Kay Gürtzig     2016.03.18      KGU#89: Extended Language translation support
 *      Kay Gürtzig     2016.03.25      Message translations now held in LangTextHolder instead of JLabel
 *      Kay Gürtzig     2016.04.12      Enh. #137: additional toggle to direct input and output to a text window
 *      Kay Gürtzig     2016.05.05      KGU#197: Further (forgotten) LangTextHolders added
 *      Kay Gürtzig     2016.07.25      Issue #201: Redesign of the GUI, new Slider listening, Call Stack button
 *      Kay Gürtzig     2016.07.27      KGU#197: More LangTextHolders for Executor error messages
 *      Kay Gürtzig     2016.08.03      KGU#89: Inheritance enhanced to improve language support (var table)
 *      Kay Gürtzig     2016.10.05      Bugfix #260: Editing of 1st column in variable table disabled.
 *      Kay Gürtzig     2016.10.07      KGU#68 (issue #15) ConcurrentHashMap replaces Object[] for variable editing
 *      Kay Gürtzig     2016.10.08      Issue #264 variable display updates caused frequent silent exceptions on rendering
 *      Kay Gürtzig     2016.11.01      Issue #81: Icon and frame size scaling ensured according to scaleFactor
 *      Kay Gürtzig     2016.11.09      Issue #81: Scale factor no longer rounded.
 *      Kay Gürtzig     2016.12.12      Issue #307: New error message msgForLoopManipulation
 *      Kay Gürtzig     2016.12.29      KGU#317 (issues #267, #315) New message for multiple subroutines
 *      Kay Gürtzig     2016.01.09      Issue #81 / bugfix #330: GUI scaling stuff outsourced to GUIScaler
 *      Kay Gürtzig     2017.03.27      Issue #356: Sensible reaction to the close button ('X') implemented
 *      Kay Gürtzig     2017.03.30      Enh. #388: Support for the display of constants
 *      Kay Gürtzig     2017.04.12      Bugfix #391: Defective button control in step mode fixed.
 *
 ******************************************************************************************************
 *
 *      Comment:  /
 *         
 ******************************************************************************************************
 */



import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import lu.fisch.structorizer.arranger.Arranger;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.RuntimeDataPresentMode;
import lu.fisch.structorizer.gui.GUIScaler;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.structorizer.gui.LangTextHolder;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.locales.LangFrame;


/**
 *
 * @author robertfisch
 */
@SuppressWarnings("serial")
public class Control extends LangFrame implements PropertyChangeListener, ItemListener { //, ILangDialog {

    /** Creates new form Control */
    public Control() {
        initComponents();
        this.setDefaultCloseOperation(Control.DO_NOTHING_ON_CLOSE);
    }

    // START KGU#375 2017-03-30: Enh. #388 Distinguished display for constants
    private class MyCellRenderer extends DefaultTableCellRenderer {

        private final Color backgroundColor = getBackground();
        private final Color constColor = Color.decode("0xFFD0FF");
        private final Color constColorSel = Color.decode("0xFF80FF");

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
        {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            String varName = model.getValueAt(row, 0).toString();
            if (Executor.getInstance().isConstant(varName)) {
            	if (isSelected) {
            		c.setBackground(constColorSel);
            	}
            	else {
            		c.setBackground(constColor);
            	}
            }
            else if (!isSelected) {
            	c.setBackground(backgroundColor);
            }
            return c;
        }
    }
    // END KGU#375 2017-03-30
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     * @param new java.awt.event.Mouse 
     */
    //@SuppressWarnings("unchecked")
    private void initComponents() {

    	// START KGU#89 2015-11-25
    	this.setIconImage(IconLoader.ico004.getImage());
    	// END KGU#89 2015-11-25
    	// START KGU#210 2016-07-25: Initialisation with min, max, and value
        //slSpeed = new javax.swing.JSlider();
        slSpeed = new javax.swing.JSlider(0, 2000, 50);
        // END KGU#210 2016-07-25
        lblSpeed = new javax.swing.JLabel();
        // START KGU#89 2015-11-25
        lblSpeedValue = new javax.swing.JLabel();
        // END KGU#89 2015-11-25
        // START KGU#160 2016-04-12: Enh. #137 - Checkbox for text window output
        chkOutputToTextWindow = new javax.swing.JCheckBox("Text Window Output");
        // END KGU#160 2016-04-12
        // START KGU#117 2016-03-06: Enh. #77 - Checkbox for Run data collection
        chkCollectRuntimeData = new javax.swing.JCheckBox("Collect Run Data");
        cbRunDataDisplay = new JComboBox<RuntimeDataPresentMode>(RuntimeDataPresentMode.values());
        // END KGU#117 2016-03-06
        btnStop = new javax.swing.JButton();
        btnPlay = new javax.swing.JButton();
        btnPause = new javax.swing.JButton();
        btnStep = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblVar = new HeaderTable();
        // START KGU#2 (#9) 2015-11-14: Additional display of subroutine call level
        // START KGU#210 2016-07-25: Fix #210 - improved usability
        //lblCallLevel = new javax.swing.JLabel(" Subroutine level:");
        btnCallStack = new javax.swing.JButton("Call stack");
        lblCallLevel = new javax.swing.JLabel("Level:");
        // END KGU#210 2016-07-25
        txtCallLevel = new javax.swing.JTextField("0");
        txtCallLevel.setEditable(false);
        // END KGU#2 (#9) 2015-11-14

        // START KGU#89/KGU#157 2016-03-18: Bugfix #131 - Prevent interference or take-over
        // These fields are just a translation support for Executor
        lbStopRunningProc = new LangTextHolder("This action is not allowed while a diagram is being executed.\nDo you want to stop the current execution?");
        lbInputValue = new LangTextHolder("Please enter a value for <%>");
        lbInputPaused = new LangTextHolder("Execution paused - you may enter the value in the variable display.");
        // END KGU#89/KGU#157 2016-03-18
        // START KGU#197 2016-05-05: Forgotten translations added
        lbInputCancelled = new LangTextHolder("Input cancelled");
        lbManuallySet = new LangTextHolder("*** Manually set: %1 <- %2 ***");
        lbEmptyLine = new LangTextHolder("empty line");
        lbReturnedResult = new LangTextHolder("Returned result");
        lbOutput = new LangTextHolder("Output");
        lbInput = new LangTextHolder("Input");
        lbAcknowledge = new LangTextHolder("Please acknowledge.");
        // END KGU#197 2016-05-05

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        // START KGU#210 2016-07-25: Issue #201 - show more details
        //slSpeed.setMajorTickSpacing(100);
        //slSpeed.setMaximum(2000);
        slSpeed.setMajorTickSpacing(500);
        slSpeed.setMinorTickSpacing(50);
        slSpeed.setPaintTicks(true);
        slSpeed.setPaintLabels(true);
        // END KGU#201 2016-07-25
        // START KGU#210 2016-07-25: Issue #201 - Cursor key movements didn't immedialtely show
        slSpeed.addChangeListener(new ChangeListener() {
        	public void stateChanged(ChangeEvent e) {
                javax.swing.JSlider source = (javax.swing.JSlider)e.getSource();
                if (!source.getValueIsAdjusting()) {
                	updateSpeed();
                }
        	}    

        });
        // END KGU#210 2016-07-25
        slSpeed.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                slSpeedMouseMoved(evt);
            }
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                slSpeedMouseDragged(evt);
            }
        });

        tblVar.addPropertyChangeListener(this);

        // START KGU#89 2015-11-25
        //lblSpeed.setText(" Delay: 50");
        lblSpeed.setText(" Delay: ");
        
        lblSpeedValue.setText("50");
        // END KGU#89 2015-11-25
        
		// START KGU#160 2016-04-12: Enh. #137 - Checkbox for text window output
        chkOutputToTextWindow.addItemListener(this);
		// END KGU#160 2016-04-12

		// START KGU#117 2016-03-06: Enh. #77 Track test coverage mode change
        chkCollectRuntimeData.addItemListener(this);
        // END KGU#117 2016-03-06
        // START KGU#165 2016-03-13: Enh. #124
        cbRunDataDisplay.addItemListener(this);
        // END KGU#156 2016-03-13

        // START KGU#287 2016-11-01: Issue #81 (DPI awareness)
        //btnStop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/executor/stop.png"))); // NOI18N
        btnStop.setIcon(IconLoader.getIconImage(getClass().getResource("/lu/fisch/structorizer/executor/stop.png"))); // NOI18N
        // END KGU#287 2016-11-01
        btnStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStopActionPerformed(evt);
            }
        });

        // START KGU#287 2016-11-01: Issue #81 (DPI awareness)
        //btnPlay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/executor/play.png"))); // NOI18N
        btnPlay.setIcon(IconLoader.getIconImage(getClass().getResource("/lu/fisch/structorizer/executor/play.png"))); // NOI18N
        // END KGU#287 2016-11-01
        btnPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPlayActionPerformed(evt);
            }
        });

        // START KGU#287 2016-11-01: Issue #81 (DPI awareness)
        //btnPause.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/executor/pause.png"))); // NOI18N
        btnPause.setIcon(IconLoader.getIconImage(getClass().getResource("/lu/fisch/structorizer/executor/pause.png"))); // NOI18N
        // END KGU#287 2016-11-01
        btnPause.setEnabled(false);
        btnPause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPauseActionPerformed(evt);
            }
        });

        // START KGU#287 2016-11-01: Issue #81 (DPI awareness)
        //btnStep.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/executor/next.png"))); // NOI18N
        btnStep.setIcon(IconLoader.getIconImage(getClass().getResource("/lu/fisch/structorizer/executor/next.png"))); // NOI18N
        // END KGU#287 2016-11-01
        btnStep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStepActionPerformed(evt);
            }
        });

        // START KGU#159 2016-03-17: New possibility to show stack trace in paused mode
        btnCallStack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCallStackActionPerformed(evt);
            }
        });
        // END KGU#159 2016-03-17

        tblVar.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Content"
            }
        ) {
            Class<?>[] types = new Class<?> [] {
                java.lang.String.class, java.lang.Object.class
            };

            public Class<?> getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
            // START KGU#269 2016-10-05: Bugfix #260 - disable editing of the name column
            @Override
            public boolean isCellEditable(int row, int column){  
                return (column==1);  
            }
            // END KGU#269 2016-10-05
       });
        jScrollPane1.setViewportView(tblVar);

        // START KGU#287 2016-11-02: Issue #81 (DPI awareness workarounds)
        double scaleFactor = Double.valueOf(Ini.getInstance().getProperty("scaleFactor","1"));
        if (scaleFactor < 1) scaleFactor = 1.0; 
        tblVar.setRowHeight((int)(tblVar.getRowHeight() * scaleFactor));
        // END KGU#2987 2016-11-02
        // START KGU#210 2016-07-25: Issue #201 - new GridBagLayout-based GUI (easier to handle)
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3,2,2,2);
        Container ctnr = getContentPane();
        ctnr.setLayout(gbl);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbl.setConstraints(lblSpeed, gbc);
        ctnr.add(lblSpeed);
        
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbl.setConstraints(lblSpeedValue, gbc);
        ctnr.add(lblSpeedValue);
        
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbl.setConstraints(slSpeed, gbc);
        ctnr.add(slSpeed);
        slSpeed.setMaximumSize(new Dimension((int)(30*scaleFactor), (int)(15*scaleFactor)));
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbl.setConstraints(chkOutputToTextWindow, gbc);
        ctnr.add(chkOutputToTextWindow);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbl.setConstraints(chkCollectRuntimeData, gbc);
        ctnr.add(chkCollectRuntimeData);
        
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbl.setConstraints(cbRunDataDisplay, gbc);
        ctnr.add(cbRunDataDisplay);
        
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbl.setConstraints(btnStop, gbc);
        ctnr.add(btnStop);
        
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbl.setConstraints(btnPlay, gbc);
        ctnr.add(btnPlay);
        
        gbc.gridx = 3;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbl.setConstraints(btnPause, gbc);
        ctnr.add(btnPause);
        
        gbc.gridx = 4;
        gbc.gridy = 4;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbl.setConstraints(btnStep, gbc);
        ctnr.add(btnStep);
        
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbl.setConstraints(btnCallStack, gbc);
        ctnr.add(btnCallStack);
        
        gbc.gridx = 3;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbl.setConstraints(lblCallLevel, gbc);
        ctnr.add(lblCallLevel);
        
        gbc.gridx = 4;
        gbc.gridy = 5;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbl.setConstraints(txtCallLevel, gbc);
        ctnr.add(txtCallLevel);
        
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0;
        gbc.weighty = 1;
        gbl.setConstraints(jScrollPane1, gbc);
        ctnr.add(jScrollPane1);
        // END KGU#210 2016-07-25
        
        // START KGU#287 2017-01-09: Issue #81 / bugfix #330 - flexible GUI scaling
        GUIScaler.rescaleComponents(this);
        SwingUtilities.updateComponentTreeUI(this);
        // END KGU#287 2017-01-09
       
        // START KGU#375 2017-03-30: Enh. #388 Distinguished display for constants
        // We must do this as late as possible, otherwise "Nimbus" tends to ignore this
        tblVar.setDefaultRenderer(Object.class, new MyCellRenderer());
        // END KGU#375 2017-03-30
        
        pack();
        
        // START KGU#287 2016-11-02: Issue #81 (DPI awareness)
        //setSize(350, 500);
        setSize((int)(350 * scaleFactor), (int)(500 * scaleFactor));
        // END KGU#287 2016-11-02

    }

    public void init()
    {
        btnStop.setEnabled(true);
        btnPlay.setEnabled(true);
        btnPause.setEnabled(false);
        btnStep.setEnabled(true);
        // START KGU#210 2016-07-25: Issue #201 - new call stack display strategy
        btnCallStack.setEnabled(false);
        // END KGU#210 2016-07-25
        // START KGU#117 2016-03-06: Enh. #77
        chkCollectRuntimeData.setEnabled(true);
        this.cbRunDataDisplay.setEnabled(chkCollectRuntimeData.isSelected());
        // END KGU#117 2016-03-06
        // empty table
        DefaultTableModel tm = (DefaultTableModel) tblVar.getModel();
        while(tm.getRowCount()>0) tm.removeRow(0);
    }

    // START KGU#210/KGU#234 2016-08-09: Issue #224 - Ensure GUI consistency and table grid visibility
    public void updateLookAndFeel()
    {
    	try {
    		SwingUtilities.updateComponentTreeUI(this);
    		// Now, this is a workaround for issue #224
    		if (!javax.swing.UIManager.getLookAndFeel().getName().equals("Nimbus"))
    		{
    			tblVar.setShowGrid(true);
    		}
    		// Make sure look and feel "Nimbus" doesn't sabotage the cell renderer setting
    		tblVar.setDefaultRenderer(Object.class, new MyCellRenderer());
    	}
    	catch (Exception ex) {}
    }
    // END KGU#210/KGU#234 2016-08-09
    
    private void btnStopActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnStopActionPerformed
    {
        Executor.getInstance().setStop(true);
        // START KGU#117 2016-03-06: Enh. #77
        chkCollectRuntimeData.setEnabled(true);
        cbRunDataDisplay.setEnabled(chkCollectRuntimeData.isSelected());
        // END KGU#117 2016-03-06
        this.setVisible(false);
    }

    private void btnPlayActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnPlayActionPerformed
    {
        btnPause.setEnabled(true);
        btnPlay.setEnabled(false);
        btnStep.setEnabled(false);
        // START KGU#210 2016-07-25: Issue #201 - new Call Stack display strategy
        btnCallStack.setEnabled(false);
        // END KGU#210 2016-07-25
        // START KGU#117 2016-03-06: Enh. #77
        chkCollectRuntimeData.setEnabled(false);
        cbRunDataDisplay.setEnabled(chkCollectRuntimeData.isSelected());
        // END KGU#117 2016-03-06
        // START KGU#68 205-11-06: Enhancement - update edited values
    	if (!varUpdates.isEmpty())
    	{
    		Executor.getInstance().adoptVarChanges(new HashMap<String, Object>(varUpdates));
    	}
    	varUpdates.clear();
    	// END KGU#68 2015-11-06
        if(Executor.getInstance().isRunning()==false)
        {
            Executor.getInstance().start(false);
        }
        else
        {
            Executor.getInstance().setPaus(false);
        }
    }
    
    // START KGU 2015-10-12: Must be possible on breakpoints
    // START KGU#379 2017-04-12: Bugfix #391 Signature change (new argument allButtons)
    //public void setButtonsForPause()
    /**
     * Enables / disables the player buttons appropriately for paused state i.e. the
     * Pause button will be disabled whereas Play, Step, and CallStack buttons will
     * be enabled. By passing false as argument, only the Pause button will be enabled,
     * the other buttons would keep their state.
     * @param allButtons - if not true then only the Pause button will be influenced
     */
    public void setButtonsForPause(boolean allButtons)
    // END KGU#379 2017-04-12
    {
        btnPause.setEnabled(false);
        // START KGU#379 2017-04-12: Bugfix #391
        if (allButtons) {
        // END KGU#379 2017-04-12
            btnPlay.setEnabled(true);
            btnStep.setEnabled(true);
            // START KGU#210 2016-07-25: Issue #201 - new Call stack display strategy
            btnCallStack.setEnabled(true);
            // END KGU#210 2016-07-25
        // START KGU#379 2017-04-12: Bugfix #391
        }
        // END KGU#379 2017-04-12
    }
    // END KGU 2015-10-12

    private void btnPauseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnPauseActionPerformed
    {
    	// START KGU 2015-10-12
//        btnPause.setEnabled(false);
//        btnPlay.setEnabled(true);
//        btnStep.setEnabled(true);
    	// START KGU#379 2017-04-12: Bugfix #391 It's sufficient just to disable the pause button for now
		setButtonsForPause(false);
		// END KGU#379 2017-04-12
    	// END KGU 2015-10-12
        Executor.getInstance().setPaus(!Executor.getInstance().getPaus());
    }

    private void btnStepActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnStepActionPerformed
    {
    	// START KGU#379 2017-04-12: Bugfix #391 - buttons weren't properly handled in step mode
    	// Buttons will be switched back in Executor.waitForNext()
    	// Attention: the pause button must not be enabled here because it has toggling effect, hence it
    	// would turn step mode into run mode if pressed during step execution!
        btnStep.setEnabled(false);
        btnPlay.setEnabled(false);
        btnCallStack.setEnabled(false);
        // END KGU#379 2017-04-12
    	// START KGU#68 2015-11-06: Enhancement - update edited values
    	if (!varUpdates.isEmpty())
    	{
    		Executor.getInstance().adoptVarChanges(new HashMap<String, Object>(varUpdates));
    		varUpdates.clear();
    	}
    	// END KGU#68 2015-11-06
        // START KGU#117 2016-03-06: Enh. #77
        chkCollectRuntimeData.setEnabled(false);
        cbRunDataDisplay.setEnabled(chkCollectRuntimeData.isSelected());
        // END KGU#117 2016-03-06
        if(Executor.getInstance().isRunning()==false)
        {
            Executor.getInstance().start(true);
        }
        else
        {
            Executor.getInstance().doStep();
        }
    }

    private void updateSpeed()
    {
        if(Executor.getInstance()!=null)
        {
            Executor.getInstance().setDelay(slSpeed.getValue());
        }
        // START KGU#89 2015-11-25
        //lblSpeed.setText(" Delay: "+slSpeed.getValue());
        lblSpeedValue.setText("" + slSpeed.getValue());
        // END KGU#89 2015-11-25
    }

    private void slSpeedMouseMoved(java.awt.event.MouseEvent evt)//GEN-FIRST:event_slSpeedMouseMoved
    {//GEN-HEADEREND:event_slSpeedMouseMoved
        updateSpeed();
    }//GEN-LAST:event_slSpeedMouseMoved

    private void slSpeedMouseDragged(java.awt.event.MouseEvent evt)//GEN-FIRST:event_slSpeedMouseDragged
    {//GEN-HEADEREND:event_slSpeedMouseDragged
        updateSpeed();
    }//GEN-LAST:event_slSpeedMouseDragged

    // START KGU#159 2016-03-17: Stack trace now permanently available on demand
    // START KGU#210 2016-07-25: Fix #201 - improved usability
    //private void txtCallLevelClicked(MouseEvent evt)
    //{
    //	if (evt.getClickCount() == 2 && Executor.getInstance().getPaus())
    //	{
    //		Executor.getInstance().showStackTrace();
    //	}
    //}
    private void btnCallStackActionPerformed(java.awt.event.ActionEvent evt)
    {
    	if (Executor.getInstance().getPaus())
    	{
    		Executor.getInstance().showStackTrace();
    	}    	
    }
    // END KGU#210 2016-07-25
    // END KGU#159 2016-03-17

    public void updateVars(Vector<Vector<Object>> vars)
    {
        tblVar.setGridColor(Color.LIGHT_GRAY);
        tblVar.setShowGrid(true);
        DefaultTableModel tm = (DefaultTableModel) tblVar.getModel();
        // empty the table
        // START KGU#68 2016-10-07: Preparation for variable editing
        varUpdates.clear();
        // END KGU#68 2016-10-07
        // START KGU#274 2016-10-08: Issue #264 Reduce the ArrayIndexOutOfBoundsException rate
        //while(tm.getRowCount()>0) tm.removeRow(0);
        //for(int i=0; i<vars.size(); i++) tm.addRow(vars.get(i));
        int nRows = tm.getRowCount();
        if (nRows > vars.size()) {
        	tm.setRowCount(vars.size());
        	nRows = vars.size();
        }
        for (int i = 0; i < nRows; i++) {
        	tm.setValueAt(vars.get(i).get(0), i, 0);
        	tm.setValueAt(vars.get(i).get(1), i, 1);
        }
        for (int i = nRows; i < vars.size(); i++) {
        	tm.addRow(vars.get(i));
        }
        // END KGU#274 2016-10-08
    }
    
    // START KGU#2 (#9) 2015-11-14: Update method for subroutine level display
    public void updateCallLevel(int level)
    {
    	this.txtCallLevel.setText(Integer.toString(level));
    }
    // END KGU#2 (#9) 2015-11-14

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnPause;
    private javax.swing.JButton btnPlay;
    private javax.swing.JButton btnStep;
    private javax.swing.JButton btnStop;
    private javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JLabel lblSpeed;
    // START KGU#89 2015-11-25: Is to ease localization (separate text and value)
    private javax.swing.JLabel lblSpeedValue;
    // END KGU#49 2015-11-25
    private javax.swing.JSlider slSpeed;
    // START KGU#160 2016-04-12: Enh. #137 - Checkbox for text window output
    public javax.swing.JCheckBox chkOutputToTextWindow;
    // END KGU#160 2016-04-12
    // START KGU#117/KGU#156 2016-03-13: Enh. #77/#124 - Checkbox fpr Test coverage mode
    public javax.swing.JCheckBox chkCollectRuntimeData;
    public javax.swing.JComboBox<RuntimeDataPresentMode> cbRunDataDisplay;
    // END KGU#117/KGU#156 2016-03-13
    private HeaderTable tblVar;
    // End of variables declaration//GEN-END:variables
    // START KGU#2 (#9) 2015-11-14: Additional display of subroutine call level
    // START KGU#210 2016-07-25: Fix #201 - A button would be more obvious to display
    public javax.swing.JButton btnCallStack;
    // END KGU#210 2016-07-25
    public javax.swing.JLabel lblCallLevel;
    public javax.swing.JTextField txtCallLevel;
    // END KGU#2 (#9) 2015-11-14
    // START KGU#89/KGU#157 2016-03-18: Bugfix #131 - Language support for Executor
    public LangTextHolder lbStopRunningProc;
    public LangTextHolder lbInputValue;
    public LangTextHolder lbInputPaused;
    // END KGU#89/KGU#157 2016-03-18
    // START KGU#197 2016-05-05: More language support
    public LangTextHolder lbInputCancelled;
    public LangTextHolder lbManuallySet;
    public LangTextHolder lbEmptyLine;
    public LangTextHolder lbReturnedResult;
    public LangTextHolder lbOutput;
    public LangTextHolder lbInput;
    public LangTextHolder lbAcknowledge;
    // START KGU 2017-04-21
    public final LangTextHolder lbOk = new LangTextHolder("OK");
    public final LangTextHolder lbPause = new LangTextHolder("Pause");
    // END KGU 2017-04-21
    // START KGU#197 2016-07-27
    public final LangTextHolder msgNoSubroutine = 
    		new LangTextHolder("A subroutine diagram \"%1\" (%2 parameters) could not be found!\nConsider starting the Arranger and place needed subroutine diagrams there first.");
 // START KGU#376 2017-04-11: Enh. #389
    // END KGU#376 2017-04-11
    public final LangTextHolder msgNoProgDiagram = 
    		new LangTextHolder("An importable program diagram \"%\" could not be found!\nConsider starting the Arranger and place the needed diagram there first.");
 // // START KGU#317 2016-12-29
    public final LangTextHolder msgAmbiguousCall =
    		new LangTextHolder("Ambiguous CALL: Different callable diagrams \"%1\" (%2 parameters) found!");
    // END KGU#317 2016-12-29
    public final LangTextHolder msgInvalidExpr =
    		new LangTextHolder("<%1> is not a correct or existing expression.");
    // START KGU#249 2016-09-17: Bugfix #246 + Issue #243
    public final LangTextHolder msgInvalidBool =
    		new LangTextHolder("<%1> is not a valid Boolean expression.");
    // END KGU#249 2016-09-17
    public final LangTextHolder msgIllFunction =
    		new LangTextHolder("<%1> is not a correct function!");
    public final LangTextHolder msgManualBreak =
    		new LangTextHolder("Manual Break!");
    public final LangTextHolder msgIllegalLeave =
    		new LangTextHolder("Illegal leave argument: %1");
    public final LangTextHolder msgWrongExit =
    		new LangTextHolder("Wrong exit value: %1");
    public final LangTextHolder msgExitCode =
    		new LangTextHolder("Program exited with code %1!");
    public final LangTextHolder msgIllegalJump =
    		new LangTextHolder("Illegal content of a Jump (i.e. exit) instruction: <%1>!");
    public final LangTextHolder msgTooManyLevels =
    		new LangTextHolder("Too many levels to leave (actual depth: %1 / specified: %2)!");
    // END KGU#197 2016-07-27
    // START KGU#247 2016-09-17: Issue #243
    public final LangTextHolder msgJumpOutParallel =
    		new LangTextHolder("Illegal attempt to jump out of a parallel thread:%Thread killed!");
    public final LangTextHolder msgTitleError =
    		new LangTextHolder("Error");
    public final LangTextHolder msgTitleParallel =
    		new LangTextHolder("Parallel Execution Problem");
    public final LangTextHolder msgTitleQuestion =
    		new LangTextHolder("Question");
    // END KGU#247 2016-09-17
    // START KGU#307 2016-12-12: Enh. #307
    public final LangTextHolder msgForLoopManipulation =
    		new LangTextHolder("Illegal attempt to manipulate the FOR loop variable «%»!");
    // END KGU#307 2016-12-12
    // START KGU#375 2017-03-30: Enh. #388
    public final LangTextHolder msgConstantRedefinition =
    		new LangTextHolder("Illegal attempt to redefine constant «%»!");
    public final LangTextHolder msgConstantArrayElement =
    		new LangTextHolder("An array element «%» may not be made a constant by assignment!");
    // END KGU#375 2017-03-30
    // START KGU#311 2016-12-18/24: Enh. #314 Error messages for File API
    public static final LangTextHolder msgInvalidFileNumberRead =
    		new LangTextHolder("Invalid file number or file not open for reading.");
    public static final LangTextHolder msgInvalidFileNumberWrite =
    		new LangTextHolder("Invalid file number or file not open for writing.");
    public static final LangTextHolder msgNoIntLiteralOnFile =
    		new LangTextHolder("No integer value readable from file!");
    public static final LangTextHolder msgNoDoubleLiteralOnFile =
    		new LangTextHolder("No floating-point value readable from file!");    
    public static final LangTextHolder msgEndOfFile =
    		new LangTextHolder("Attempt to read data past end of file!");    
    // END KGU#311 2016-12-18/24
    // START KGU#372 2017-03-27: Enh. #356
    public static final LangTextHolder msgUseStopButton =
    		new LangTextHolder("There is a running or pending execution!\nUse the STOP button to abort and close.");
    // END KGU#372 2017-03-27
    
    // START KGU#68 2015-11-06: Register variable value editing events
    private final ConcurrentMap<String, Object> varUpdates = new ConcurrentHashMap<String, Object>();

    @Override
	public void propertyChange(PropertyChangeEvent pcEv) {
		// Check if it was triggered by the termination of some editing activity (i.e. the cell editor was dropped)
    	if (pcEv.getSource() == this.tblVar && pcEv.getPropertyName().equals("tableCellEditor") && pcEv.getNewValue() == null)
    	{
    		int rowNr = tblVar.getSelectedRow();
            DefaultTableModel tm = (DefaultTableModel) tblVar.getModel();
            Object val = tm.getValueAt(rowNr, 1);
            if (val != null)
            {
            	varUpdates.put((String)tm.getValueAt(rowNr, 0), val);
            	//System.out.println(tm.getValueAt(rowNr, 0).toString() + " <- " + val.toString());
            }
    	}
		
	}
    // END KGU#68 2015-11-06

	// START KGU#117 2016-03-08: Enh. #77
	@Override
	public void itemStateChanged(ItemEvent itEv) {
		if (itEv.getSource() == this.chkCollectRuntimeData)
    	{
    		if (itEv.getStateChange() == ItemEvent.SELECTED)
    		{
    			Element.E_COLLECTRUNTIMEDATA = true;
    			this.cbRunDataDisplay.setEnabled(this.chkCollectRuntimeData.isEnabled());
        		if (Arranger.hasInstance())
        		{
        			Arranger.getInstance().redraw();
        		}
        		Executor.getInstance().redraw();
    		}
    		else if (itEv.getStateChange() == ItemEvent.DESELECTED)
    		{
    			boolean wipeTestStatus = Element.E_COLLECTRUNTIMEDATA;
    			Element.E_COLLECTRUNTIMEDATA = false;
    			this.cbRunDataDisplay.setEnabled(false);
    			if (wipeTestStatus) 
    			{
    				Executor.getInstance().clearPoolExecutionStatus();
    			}
    		}
    		if (Arranger.hasInstance())
    		{
    			Arranger.getInstance().doButtons();
    		}
    	}
    	else if (itEv.getSource() == this.cbRunDataDisplay)
    	{
    		RuntimeDataPresentMode oldShowMode = Element.E_RUNTIMEDATAPRESENTMODE;
    		Element.E_RUNTIMEDATAPRESENTMODE = (RuntimeDataPresentMode)itEv.getItem();
    		if (oldShowMode != Element.E_RUNTIMEDATAPRESENTMODE)
    		{
        		if (Arranger.hasInstance())
        		{
        			Arranger.getInstance().redraw();
        		}
        		Executor.getInstance().redraw();
    		}
    	}
	    // START KGU#160 2016-04-12: Enh. #137 - Checkbox for text window output
    	else if (itEv.getSource() == this.chkOutputToTextWindow)
    	{
    		Executor.getInstance().setOutputWindowEnabled(this.chkOutputToTextWindow.isSelected());
    	}
	    // END KGU#160 2016-04-12
	}
	// END KGU#117 2016-03-08
	
	// START KGU#372 2017-03-27: Enh. #356
	/**
	 * Allows programmatically to press the STOP button.
	 */
	public void clickStopButton()
	{
		this.btnStop.doClick();
	}
	// END KGU#372 2017-03-27
}

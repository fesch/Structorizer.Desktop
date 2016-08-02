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
 *      Description:    This the dialog that allows editing the properties of any element.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.23      First Issue
 *      Kay Gürtzig     2015.10.12      A checkbox added for breakpoint control (KGU#43)
 *      Kay Gürtzig     2015.10.14      Element-class-specific language support (KGU#42)
 *      Kay Gürtzig     2015.10.25      Hook for subclassing added to method create() (KGU#3)
 *      Kay Gürtzig     2016.04.26      Issue #165: Focus transfer reset to Tab and Shift-Tab
 *      Kay Gürtzig     2016.07.14      Enh. #180: Initial focus dependent on switchTextComment mode (KGU#169)
 *      Kay Gürtzig     2016.08.02      Enh. #215: Breakpoint trigger counts partially implemented
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.utils.StringList;


@SuppressWarnings("serial")
public class InputBox extends LangDialog implements ActionListener, KeyListener
{
    public boolean OK = false;
    
    // KGU#3 2015-11-03: Some of the controls had to be made public in order to allow language support for subclasses 
    // Buttons
    public JButton btnOK = new JButton("OK"); 
    public JButton btnCancel = new JButton("Cancel"); 
	
    // Labels
    public JLabel lblText = new JLabel("Please enter a text");
    public JLabel lblComment = new JLabel("Comment");

    // Textarea
    public JTextArea txtText = new JTextArea();
    public JTextArea txtComment = new JTextArea();

    // Scrollpanes
    protected JScrollPane scrText = new JScrollPane(txtText);
    protected JScrollPane scrComment = new JScrollPane(txtComment);
    
    // Checkbox
    // START KGU#43 2015-10-12: Additional possibility to control the breakpoint setting
    public JCheckBox chkBreakpoint = new JCheckBox("Breakpoint");
    // END KGU#43 2015-10-12
    // START KGU#213 2016-08-01: Enh. #215
    //private int prevBreakTrigger = 0;
    public LangTextHolder lblBreakText = new LangTextHolder("Break at execution count: %");
    public JLabel lblBreakTrigger = new JLabel();
    //public JTextField txtBreakTrigger = new JTextField();
    // END KGU#213 2016-08-01
    
    // START KGU 2015-10-14: Additional information for data-specific title translation
    public String elementType = new String();	// The (lower-case) class name of the element type to be edited here
    public boolean forInsertion = false;		// If this dialog is used to setup a new element (in contrast to updating an existing element)
    private boolean gotSpecificTitle = false;	// class-specific title translation already done? (prevents setTitle() from spoiling it)
    // END KGU 2015-10-14

    // START KGU#169 2016-07-14: Enh. #180: helps to enable focus control
    protected void setPreferredSize()
    {
        setSize(500, 400);   	
    }
    // END KGU#169 2016-07-14


    private void create()
    {
            // set window title
            setTitle("Content");
            // set layout (OS default)
            setLayout(null);
            // START KGU#169 2016-07-14: Enh. #180: Now done after pack() and subclassable
            // set windows size
            //setSize(500, 400);
            // END KGU#169 2016-07-14
            // show form
            setVisible(false);
            // set action to perform if closed
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            // set icon
            btnOK.addActionListener(this);
            btnCancel.addActionListener(this);

            btnOK.addKeyListener(this);
            btnCancel.addKeyListener(this);
            txtText.addKeyListener(this);
            // START KGU#186 2016-04-26: Issue #163 - tab isn't really needed within the text
            txtText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
            txtText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
            // END KGU#186 2016-04-26
            txtComment.addKeyListener(this);
            // START KGU#186 2016-04-26: Issue #163 - tab isn't really needed within the text
            txtComment.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
            txtComment.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
            // END KGU#186 2016-04-26
            // START KGU#213 2016-08-01: Enh. #215
            //txtBreakTrigger.addKeyListener(this);
            // END KGU#213 2016-08-01
            addKeyListener(this);

            Border emptyBorder = BorderFactory.createEmptyBorder(4,4,4,4);
            txtText.setBorder(emptyBorder);
            txtComment.setBorder(emptyBorder);

            JPanel pnPanel0 = new JPanel();
            GridBagLayout gbPanel0 = new GridBagLayout();
            GridBagConstraints gbcPanel0 = new GridBagConstraints();
            gbcPanel0.insets=new Insets(10,10, 0,10);
            pnPanel0.setLayout( gbPanel0 );
            
            // START KGU#3 2015-10-24: Open opportunities for subclasses
            createPanelTop(pnPanel0, gbPanel0, gbcPanel0);
            
            JPanel pnPanel1 = new JPanel();
            GridBagLayout gbPanel1 = new GridBagLayout();
            GridBagConstraints gbcPanel1 = new GridBagConstraints();
            gbcPanel1.insets=new Insets(10,10,0,10);
            pnPanel1.setLayout( gbPanel1 );
            // END KGU#3 2015-10-24
            
            gbcPanel1.gridx = 1;
            gbcPanel1.gridy = 2;
            gbcPanel1.gridwidth = 18;
            gbcPanel1.gridheight = 7;
            gbcPanel1.fill = GridBagConstraints.BOTH;
            gbcPanel1.weightx = 1;
            gbcPanel1.weighty = 1;
            gbcPanel1.anchor = GridBagConstraints.NORTH;
            gbPanel1.setConstraints( scrText, gbcPanel1 );
            pnPanel1.add( scrText );

            gbcPanel1.gridx = 1;
            gbcPanel1.gridy = 12;
            gbcPanel1.gridwidth = 18;
            gbcPanel1.gridheight = 4;
            gbcPanel1.fill = GridBagConstraints.BOTH;
            gbcPanel1.weightx = 1;
            gbcPanel1.weighty = 1;
            gbcPanel1.anchor = GridBagConstraints.NORTH;
            gbPanel1.setConstraints( scrComment, gbcPanel1 );
            pnPanel1.add( scrComment );

            // START KGU#3 2015-10-25: Moved to addCreating() - such that it may be replaced by subclasses
//            gbcPanel1.gridx = 1;
//            gbcPanel1.gridy = 1;
//            gbcPanel1.gridwidth = 18;
//            gbcPanel1.gridheight = 1;
//            gbcPanel1.fill = GridBagConstraints.BOTH;
//            gbcPanel1.weightx = 1;
//            gbcPanel1.weighty = 0;
//            gbcPanel1.anchor = GridBagConstraints.NORTH;
//            gbPanel1.setConstraints( lblText, gbcPanel1 );
//            pnPanel1.add( lblText );
            // END KGU#3 2015-10-25
            
            gbcPanel1.gridx = 1;
            gbcPanel1.gridy = 10;
            gbcPanel1.gridwidth = 18;
            gbcPanel1.gridheight = 1;
            gbcPanel1.fill = GridBagConstraints.BOTH;
            gbcPanel1.weightx = 1;
            gbcPanel1.weighty = 0;
            gbcPanel1.anchor = GridBagConstraints.NORTH;
            gbPanel1.setConstraints( lblComment, gbcPanel1 );
            pnPanel1.add( lblComment );

            gbcPanel1.gridx = 1;
            gbcPanel1.gridy = 17;
            gbcPanel1.gridwidth = 7;
            gbcPanel1.gridheight = 1;
            gbcPanel1.fill = GridBagConstraints.BOTH;
            gbcPanel1.weightx = 1;
            gbcPanel1.weighty = 0;
            gbcPanel1.anchor = GridBagConstraints.NORTH;
            gbPanel1.setConstraints( chkBreakpoint, gbcPanel1 );
            pnPanel1.add( chkBreakpoint );

            // START KGU#213 2016-08-01: Enh. #215 - conditional breakpoints
            gbcPanel1.gridx = 12;
            gbcPanel1.gridy = 17;
            gbcPanel1.gridwidth = 7;
            gbcPanel1.gridheight = 1;
            gbcPanel1.fill = GridBagConstraints.BOTH;
            gbcPanel1.weightx = 1;
            gbcPanel1.weighty = 0;
            gbcPanel1.anchor = GridBagConstraints.NORTH;
            gbPanel1.setConstraints( lblBreakTrigger, gbcPanel1 );
            pnPanel1.add( lblBreakTrigger );

//            gbcPanel1.gridx = 13;
//            gbcPanel1.gridy = 17;
//            gbcPanel1.gridwidth = 2;
//            gbcPanel1.gridheight = 1;
//            gbcPanel1.fill = GridBagConstraints.BOTH;
//            gbcPanel1.weightx = 1;
//            gbcPanel1.weighty = 0;
//            gbcPanel1.anchor = GridBagConstraints.NORTH;
//            gbPanel1.setConstraints( txtBreakTrigger, gbcPanel1 );
//            pnPanel1.add( txtBreakTrigger );
// END KGU#213 2106-08-01

            gbcPanel1.insets=new Insets(10,10,10,10);

            //createExitButtons(gridbase)
            gbcPanel1.gridx = 1;
            gbcPanel1.gridy = 18;
            gbcPanel1.gridwidth = 7;
            gbcPanel1.gridheight = 1;
            gbcPanel1.fill = GridBagConstraints.BOTH;
            gbcPanel1.weightx = 1;
            gbcPanel1.weighty = 0;
            gbcPanel1.anchor = GridBagConstraints.NORTH;
            gbPanel1.setConstraints( btnCancel, gbcPanel1 );
            pnPanel1.add( btnCancel );

            // START KGU#3 2015-10-31: The new gridx causes no difference here but fits better for InputBoxFor
            gbcPanel1.gridx = 12;
            //gbcPanel1.gridx = 8;
            // END KGU#3 2015-10-31
            gbcPanel1.gridy = 18;
            // START KGU#3 2015-10-31: The new gridwidth causes no difference here but fits better for InputBoxFor
            gbcPanel1.gridwidth = 7;
    		//gbcPanel1.gridwidth = GridBagConstraints.REMAINDER;
    		// END KGU#3 2015-10-31
            gbcPanel1.gridheight = 1;
            gbcPanel1.fill = GridBagConstraints.BOTH;
            gbcPanel1.weightx = 1;
            gbcPanel1.weighty = 0;
            gbcPanel1.anchor = GridBagConstraints.NORTH;
            gbPanel1.setConstraints( btnOK, gbcPanel1 );
            pnPanel1.add( btnOK );

            Container container = getContentPane();
            container.setLayout(new BorderLayout());
            container.add(pnPanel0,BorderLayout.NORTH);
            container.add(pnPanel1,BorderLayout.CENTER);

            // START KGU#91+KGU#169 2016-07-14: Enh. #180 (also see #39 and #142)
            this.pack();	// This makes focus control possible but must precede the size setting
            setPreferredSize();
            if (Element.E_TOGGLETC)
            {
            	txtComment.requestFocusInWindow();
            }
            else
            {
            	txtText.requestFocusInWindow();
            }
            // END KGU#91+KGU#169 2016-07-14
    }
    
    // START KGU#3 2015-10-24: Hook for subclasses
    /**
     * Subclassable method to add specific stuff to the Panel top
     * @param _panel the panel to be enhanced
     * @param _gbc the layout constraints
     * @return number of lines (y units) inserted
     */
    protected int createPanelTop(JPanel _panel, GridBagLayout _gb, GridBagConstraints _gbc)
    {
        _gbc.gridx = 1;
        _gbc.gridy = 1;
        _gbc.gridwidth = 18;
        _gbc.gridheight = 1;
        _gbc.fill = GridBagConstraints.BOTH;
        _gbc.weightx = 1;
        _gbc.weighty = 0;
        _gbc.anchor = GridBagConstraints.NORTH;
        _gb.setConstraints( lblText, _gbc );
        _panel.add( lblText );
        // Return the number of used grid lines such that the calling method may go on there
    	return 1;
    }
    // END KGU#3 2015-10-24


    // listen to actions
    public void actionPerformed(ActionEvent event)
    {
            Object source=event.getSource();

            if (source == btnOK)
            {
                    OK=true;
            }
            else if (source == btnCancel)
            {
                    OK=false;
            }
            setVisible(false);
    }

    public void keyTyped(KeyEvent kevt)
    {
    }
	
    public void keyPressed(KeyEvent e)
    {
            if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
            {
                    OK=false;
                    setVisible(false);
            }
            else if(e.getKeyCode() == KeyEvent.VK_ENTER && (e.isShiftDown() || e.isControlDown()))
            {
                    OK=true;
                    setVisible(false);
            }
    }

    public void keyReleased(KeyEvent ke)
    {
//    	// START KGU#213 2016-08-01: Enh. #215
//    	Object source = ke.getSource();
//    	if (source == txtBreakTrigger)
//    	{
//    		int cnt = 0;
//    		String triggerStr = txtBreakTrigger.getText();
//    		if (triggerStr != null)
//    		{
//    			try{
//    				cnt = Integer.parseUnsignedInt(triggerStr);
//    				this.prevBreakTrigger = cnt;
//    			}
//    			catch (Exception ex)
//    			{
//    				txtBreakTrigger.setText(Integer.toString(this.prevBreakTrigger));
//    			}
//    		}
//    	}
    }
	
    // constructors
    public InputBox(Frame owner, boolean modal)
    {
        super(owner,modal);
        create();
    }
	
    // constructors
    /*public InputBox()
    {
        super();
	this.setModal(true);
        create();
    }*/

    // START KGU#42 2015-10-14: data-specific title localisation
    /**
     * Replaces the title string by translation if keys match some internal state information
     * @see lu.fisch.structorizer.gui.LangDialog#setLangSpecific(lu.fisch.utils.StringList, java.lang.String)
     */
    @Override
	protected void setLangSpecific(StringList keys, String translation)
	{
		if (!keys.get(2).isEmpty() && keys.get(2).equalsIgnoreCase(this.elementType))
		{
			String discriminator = keys.get(3);
			if (discriminator.isEmpty() ||
					discriminator.equals("insert") && this.forInsertion ||
					discriminator.equals("update") && !this.forInsertion)
			{
				this.setTitle(translation);
				this.gotSpecificTitle = true;
			}
			if (discriminator.equalsIgnoreCase("lblText"))
			{
				this.lblText.setText(translation);
			}
		}
	}
    
    /**
     * Sets the title of the Dialog if no specific translation had already taken place
	 * @param title - the title to be displayed in the dialog's border; a null value is ignored here
     */
    @Override
    public void setTitle(String title)
    {
    	if (title != null && !this.gotSpecificTitle)
    	{
    		super.setTitle(title);
    	}
    }
    // END KGU#42 2015-10-14
    
    // START KGU#61 2016-03-21: Enh. #84 - Addition to facilitate specific handling
    /**
     * May check and ensure consistency between inserted data, control behaviour etc.
     */
    public void checkConsistency() {}
    // END KGU#61 2016-03-21
    
}

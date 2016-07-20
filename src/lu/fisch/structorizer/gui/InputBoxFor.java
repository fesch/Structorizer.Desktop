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
*      Description:    This dialog allows editing the properties of FOR elements in a more specific way.
*
******************************************************************************************************
*
*      Revision List
*
*      Author           Date			Description
*      ------			----			-----------
*      Kay Gürtzig      2015-10-12		First Issue
*      Kay Gürtzig      2015-11-01		Mutual text field update and error detection accomplished
*      Kay Gürtzig      2015-12-04		frame width increased (-> 600)
*      Kay Gürtzig      2016-03-20      Enhancement #84/#135: FOR-IN / FOREACH paradigm considered
*      Kay Gürtzig      2016-07-14      Enh. #180: Initial focus dependent on switchTextComment mode (KGU#169)
*
******************************************************************************************************
*
*      Comment:		/
*      - This editor has practically two and a half modes:
*        1. Structured editing of traditional counter loops via specific fields, text automatically composed
*        2. full text editing, editor splits it to fill the structured counter fields (strong syntax support)
*        3. full text editing, as soon as one of the keys for FOR IN loops occurs, a FOR-IN loop is assumed
*           (weak syntax support)  
*
******************************************************************************************************///

import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.parsers.D7Parser;

/**
 * @author kay
 *
 */
@SuppressWarnings("serial")
public class InputBoxFor extends InputBox implements ItemListener {
	
	protected JLabel lblVariable/* = new JLabel("Counter variable")*/;
	protected JLabel lblStartVal/* = new JLabel("Start value")*/;
	protected JLabel lblEndVal/* = new JLabel("End value")*/;
	protected JLabel lblIncr/* = new JLabel("Increment")*/;
	protected JLabel lblPreFor/* = new JLabel(D7Parser.preFor)*/;
	protected JLabel lblPostFor/* = new JLabel(D7Parser.postFor)*/;
	protected JLabel lblAsgnmt/* = new JLabel(" <- ")*/;
	protected JLabel lblStepFor/* = new JLabel(D7Parser.stepFor)*/;
	protected JLabel lblParserInfo/* = new JLabel("")*/;
	protected JTextField txtVariable/* = new JTextField(20)*/;
	protected JTextField txtStartVal/* = new JTextField(10)*/;
	protected JTextField txtEndVal/* = new JTextField(20)*/;
	protected JTextField txtIncr/* = new JTextField(10)*/;
	protected JCheckBox chkTextInput/* = new JCheckBox("Full Text Editing")*/;
	
	// START KGU#61 2016-03-20: Enh. #84/#135 - FOR-IN loop support
	protected String forInValueList = null;
	// END KGU#61 2016-03-20
	
	private int prevTxtIncrContent = 1;		// Workaround for poor behaviour of JFormattedTextField;

	/**
	 * Constructs the dedicated editor for FOR loops 
	 * @param owner
	 * @param modal
	 */
	public InputBoxFor(Frame owner, boolean modal) {
		super(owner, modal);
	}
	
    // START KGU#169 2016-07-14: Enh. #180 (see also: #39, #142) - helps to enable focus control
    protected void setPreferredSize()
    {
        setSize(600, 400);   	
    }
    // END KGU#169 2016-07-14

    
    /**
     * Subclassable method to add specific stuff to the Panel top
     * @param _panel the panel to be enhanced
     * @param pnPanel0c the layout constraints
     * @return number of lines (y cell units) inserted
     */
	protected int createPanelTop(JPanel _panel, GridBagLayout _gb, GridBagConstraints _gbc)
	{
		lblVariable = new JLabel("Counter variable");
		lblStartVal = new JLabel("Start value");
		lblEndVal = new JLabel("End value");
		lblIncr = new JLabel("Increment");
		lblPreFor = new JLabel(D7Parser.preFor);
		lblPostFor = new JLabel(D7Parser.postFor);
		lblAsgnmt = new JLabel(" <- ");
		lblStepFor = new JLabel(D7Parser.stepFor);
		lblParserInfo = new JLabel("");
		txtVariable = new JTextField(50);
		txtStartVal = new JTextField(20);
		txtEndVal = new JTextField(50);
		txtIncr = new JTextField(20);	// Width 10
		chkTextInput = new JCheckBox("Full Text Editing");

		txtVariable.addKeyListener(this);
		txtStartVal.addKeyListener(this);
		txtEndVal.addKeyListener(this);
		txtIncr.addKeyListener(this);
		chkTextInput.setSelected(false);	// TODO Will have to be set in dependence of consistency flag 
		chkTextInput.addItemListener(this);
		txtText.addKeyListener(this);

	    // START KGU#169 2016-07-14: Enh. #180 - Now delegated to setPreferredSize() to be done afterwards
		//setSize(600, 400);	// We need more width, at least on Linux
	    // END KGU#169 2016-07-14

		int lineNo = 1;

		// TODO (KGU 2015-11-01) Grid configuration halfway works under both Windows and KDE but's still not pleasant 
		
		_gbc.insets = new Insets(10, 5, 0, 5);
		
		_gbc.gridx = 2;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 1;
		_gbc.fill = GridBagConstraints.NONE;
		_gbc.anchor = GridBagConstraints.WEST;
		_gb.setConstraints(lblVariable, _gbc);
		_panel.add(lblVariable);

		_gbc.gridx = 8;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 1;
		_gbc.fill = GridBagConstraints.NONE;
		_gbc.anchor = GridBagConstraints.WEST;
		_gb.setConstraints(lblStartVal, _gbc);
		_panel.add(lblStartVal);

		_gbc.gridx = 11;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 1;
		_gbc.fill = GridBagConstraints.NONE;
		_gbc.anchor = GridBagConstraints.WEST;
		_gb.setConstraints(lblEndVal, _gbc);
		_panel.add(lblEndVal);

		_gbc.gridx = 17;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 1;
		_gbc.fill = GridBagConstraints.NONE;
		_gbc.anchor = GridBagConstraints.WEST;
		_gb.setConstraints(lblIncr, _gbc);
		_panel.add(lblIncr);

		lineNo++;

		_gbc.insets = new Insets(10, 10, 0, 5);
		
		_gbc.gridx = 1;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 1;
		_gbc.fill = GridBagConstraints.NONE;
		_gbc.anchor = GridBagConstraints.WEST;
		_gb.setConstraints(lblPreFor, _gbc);
		_panel.add(lblPreFor);

		_gbc.insets = new Insets(10, 5, 0, 5);

		_gbc.gridx = 2;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 5;
		_gbc.weightx = 20;
		_gbc.fill = GridBagConstraints.HORIZONTAL;
		_gb.setConstraints(txtVariable, _gbc);
		_panel.add(txtVariable);

		_gbc.gridx = 7;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 1;
		_gbc.fill = GridBagConstraints.NONE;
		_gbc.anchor = GridBagConstraints.WEST;
		_gb.setConstraints(lblAsgnmt, _gbc);
		_panel.add(lblAsgnmt);

		_gbc.gridx = 8;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 2;
		_gbc.weightx = 10;
		_gbc.fill = GridBagConstraints.HORIZONTAL;
		_gb.setConstraints(txtStartVal, _gbc);
		_panel.add(txtStartVal);

		_gbc.gridx = 10;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 1;
		_gbc.fill = GridBagConstraints.NONE;
		_gbc.anchor = GridBagConstraints.WEST;
		_gb.setConstraints(lblPostFor, _gbc);
		_panel.add(lblPostFor);

		_gbc.gridx = 11;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 5;
		_gbc.weightx = 20;
		_gbc.fill = GridBagConstraints.HORIZONTAL;
		_gb.setConstraints(txtEndVal, _gbc);
		_panel.add(txtEndVal);

		_gbc.gridx = 16;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 1;
		_gbc.fill = GridBagConstraints.NONE;
		_gbc.anchor = GridBagConstraints.WEST;
		_gb.setConstraints(lblStepFor, _gbc);
		_panel.add(lblStepFor);

		_gbc.insets = new Insets(10, 5, 0, 10);

		_gbc.gridx = 17;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 4;
		_gbc.weightx = 10;
		_gbc.fill = GridBagConstraints.HORIZONTAL;
		_gb.setConstraints(txtIncr, _gbc);
		_panel.add(txtIncr);
		
		lineNo++;

		_gbc.insets = new Insets(10, 10, 0, 10);

		_gbc.gridx = 1;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 9;
		_gbc.fill = GridBagConstraints.HORIZONTAL;
		_gb.setConstraints(chkTextInput, _gbc);
		_panel.add(chkTextInput);

		_gbc.gridx = 10;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = GridBagConstraints.REMAINDER;
		_gbc.fill = GridBagConstraints.HORIZONTAL;
		_gbc.anchor = GridBagConstraints.WEST;
		_gb.setConstraints(lblParserInfo, _gbc);
		_panel.add(lblParserInfo);

		return lineNo;
	}
    
    public void keyReleased(KeyEvent kevt)
    {
    	Object source = kevt.getSource();
    	if (source == txtVariable || source == txtStartVal || source == txtEndVal || source == txtIncr)
    	{
    		int incr = this.prevTxtIncrContent;
    		String incrStr = txtIncr.getText();
    		if (incrStr != null)
    		{
    			try{
    				incr = Integer.valueOf(incrStr);
    				this.prevTxtIncrContent = incr;
    			}
    			catch (Exception ex)
    			{
    				txtIncr.setText(Integer.toString(this.prevTxtIncrContent));
    			}
    		}
    		txtText.setText(For.composeForClause(txtVariable.getText(), txtStartVal.getText(), txtEndVal.getText(), incr, false));
    		// START KGU#61 2016-03-20: Enh. #84/#135 - FOR-IN loop support
    		forInValueList = null;
    		// END KGU#61 2016-03-20
    	}
    	else if (source == txtText)
    	{
    		//String text = For.unifyOperators(txtText.getText());	// Now done by splitForClause
    		String text = txtText.getText();
    		String[] forFractions = For.splitForClause(text);
    		// START KGU#61 2016-03-21: Enh. #84/#135 - check whether this is a FOR-IN loop
    		if (forFractions.length >= 6 && forFractions[5] != null)
    		{
    			txtVariable.setText(forFractions[0]);
    			forInValueList = forFractions[5];
    			checkValueList();
    			lblStartVal.setVisible(false);
    			lblEndVal.setVisible(false);
    			lblIncr.setVisible(false);
    			lblPostFor.setForeground(Color.GRAY);
    			lblAsgnmt.setText(D7Parser.postForIn);
    			lblStepFor.setForeground(Color.GRAY);			
    			txtStartVal.setText("");
    			txtEndVal.setText("");
    			txtIncr.setText("");
    		}
    		else
    		{
    			// END KGU#61 2016-03-21
    			if (forFractions.length >= 3)
    			{
    				// START KGU#61 2016-03-21: Enh. #84/#135 - Ensure traditional field visibility
    				lblStartVal.setVisible(true);
    				lblEndVal.setVisible(true);
    				lblIncr.setVisible(true);
    				lblPostFor.setForeground(Color.BLACK);
    				lblAsgnmt.setText(" <- ");
    				lblStepFor.setForeground(Color.BLACK);			
    				// END KGU#61 2016-03-21
    				txtVariable.setText(forFractions[0]);
    				txtStartVal.setText(forFractions[1]);
    				txtEndVal.setText(forFractions[2]);
    				txtIncr.setText(forFractions[3]);
    			}
    			if (forFractions[4].isEmpty() || forFractions[3].equals(forFractions[4]))
    			{
    				lblParserInfo.setText("");
    			}
    			else
    			{
    				lblParserInfo.setForeground(Color.RED);
    				lblParserInfo.setText("<" + forFractions[4] + "> is no valid integer constant");
    			}
   			// START KGU#61 2016-03-21: Enh. #84/#135 (continued)
        		forInValueList = null;
    		}
    		// END KGU#61 2016-03-21
    	}
    	super.keyReleased(kevt);
    }

	@Override
	public void itemStateChanged(ItemEvent iev) {
		if (iev.getSource() == chkTextInput)
		{
			this.enableTextFields(iev.getStateChange() == ItemEvent.SELECTED);
		}
	}
	
	// TODO: Change parameter to For.ForLoopStyle
	public void enableTextFields(boolean inputAsText)
	{
		txtVariable.setEnabled(!inputAsText);
		txtStartVal.setEnabled(!inputAsText);
		txtEndVal.setEnabled(!inputAsText);
		txtIncr.setEnabled(!inputAsText);
		// START KGU#61 2016-03-21: Enh. #84/#135 - Ensure traditional field visibility
		if (!inputAsText)
		{
			lblStartVal.setVisible(true);
			lblEndVal.setVisible(true);
			lblIncr.setVisible(true);
			lblPostFor.setForeground(Color.BLACK);
			lblAsgnmt.setText(" <- ");
			lblStepFor.setForeground(Color.BLACK);			
		}
		// END KGU#61 2016-03-21
		
		txtText.setEnabled(inputAsText);
		lblParserInfo.setVisible(inputAsText);
	}

	// START KGU#61 2016-03-21: Enh. #84 - special test for FOR-IN loop
	private void checkValueList()
	{
		lblParserInfo.setForeground(Color.BLACK);
		lblParserInfo.setText("");
		if (forInValueList.startsWith("{") && !forInValueList.endsWith("}"))
		{				
    			lblParserInfo.setForeground(Color.RED);
    			lblParserInfo.setText("Value list must end with '}'");
		}
		else if (!forInValueList.startsWith("{") && forInValueList.endsWith("}"))
		{				
			lblParserInfo.setForeground(Color.RED);
			lblParserInfo.setText("Value list should begin with '{'");
		}
		else if ((new Function(forInValueList)).isFunction())
		{
			lblParserInfo.setForeground(Color.BLUE);
			lblParserInfo.setText("Ensure the function returns an array.");			
		}
		else if (forInValueList.isEmpty())
		{
			lblParserInfo.setForeground(Color.BLUE);
			lblParserInfo.setText("Enter the value list for the loop.");			
		}
		//lblParserInfo.setForeground(Color.decode("0x007700"));
		//lblParserInfo.setText(values);
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.gui.InputBox#checkConsistency()
	 */
	@Override
	public void checkConsistency()
	{
		if (chkTextInput.isSelected())
		{
    		String text = txtText.getText();
    		String[] forFractions = For.splitForClause(text);
    		// START KGU#61 2016-03-21: Enh. #84/#135 - check whether this is a FOR-IN loop
    		if (forFractions.length >= 6 && forFractions[5] != null)
    		{
    			txtVariable.setText(forFractions[0]);
    			forInValueList = forFractions[5];
    			checkValueList();
    			lblStartVal.setVisible(false);
    			lblEndVal.setVisible(false);
    			lblIncr.setVisible(false);
    			lblPostFor.setForeground(Color.GRAY);
    			lblAsgnmt.setText(D7Parser.postForIn);
    			lblStepFor.setForeground(Color.GRAY);			
    			txtStartVal.setText("");
    			txtEndVal.setText("");
    			txtIncr.setText("");
    		}		
		}
	}
	// END KGU#61 2016-03-21

}
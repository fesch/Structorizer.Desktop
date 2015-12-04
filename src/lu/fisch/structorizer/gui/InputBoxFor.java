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
*
******************************************************************************************************
*
*      Comment:		/
*
******************************************************************************************************///

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.parsers.D7Parser;
import lu.fisch.utils.BString;

/**
 * @author kay
 *
 */
public class InputBoxFor extends InputBox implements ItemListener {
	
	protected JLabel lblVariable/* = new JLabel("Counter variable")*/;
	protected JLabel lblStartVal/* = new JLabel("Start value")*/;
	protected JLabel lblEndVal/* = new JLabel("End value")*/;
	protected JLabel lblIncr/* = new JLabel("Increment")*/;
	// START optional labels for design study 2
	protected JLabel lblPreFor/* = new JLabel(D7Parser.preFor)*/;
	protected JLabel lblPostFor/* = new JLabel(D7Parser.postFor)*/;
	protected JLabel lblAsgnmt/* = new JLabel(" <- ")*/;
	protected JLabel lblStepFor/* = new JLabel(D7Parser.stepFor)*/;
	// END optional labels
	protected JLabel lblParserWarnings/* = new JLabel("")*/;
	protected JTextField txtVariable/* = new JTextField(20)*/;
	protected JTextField txtStartVal/* = new JTextField(10)*/;
	protected JTextField txtEndVal/* = new JTextField(20)*/;
	protected JTextField txtIncr/* = new JTextField(10)*/;
	protected JCheckBox chkTextInput/* = new JCheckBox("Full Text Editing")*/;
	
	private int prevTxtIncrContent = 1;		// Workaround for poor behaviour of JFormattedTextField;

	/**
	 * Constructs the dedicated editor for FOR loops 
	 * @param owner
	 * @param modal
	 */
	public InputBoxFor(Frame owner, boolean modal) {
		super(owner, modal);
	}
	
    
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
		// START optional labels for design study two
		lblPreFor = new JLabel(D7Parser.preFor);
		lblPostFor = new JLabel(D7Parser.postFor);
		lblAsgnmt = new JLabel(" <- ");
		lblStepFor = new JLabel(D7Parser.stepFor);
		// END optional labels
		lblParserWarnings = new JLabel("");
		lblParserWarnings.setForeground(Color.RED);
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

		setSize(600, 400);	// We need more width, at least on Linux
		
		int lineNo = 1;

		// START KGU 2015-10-30: Design study 1
//		_gbc.gridx = 1;
//		_gbc.gridy = lineNo;
//		_gbc.gridheight = 1;
//		_gbc.gridwidth = 1;
//		_gbc.fill = GridBagConstraints.NONE;
//		_gbc.anchor = GridBagConstraints.WEST;
//		_gb.setConstraints(lblVariable, _gbc);
//		_panel.add(lblVariable);
//
//		_gbc.gridx = 2;
//		_gbc.gridy = lineNo;
//		_gbc.gridheight = 1;
//		_gbc.gridwidth = 6;
//		_gbc.fill = GridBagConstraints.HORIZONTAL;
//		_gb.setConstraints(txtVariable, _gbc);
//		_panel.add(txtVariable);
//
//		_gbc.gridx = 12;
//		_gbc.gridy = lineNo;
//		_gbc.gridheight = 1;
//		_gbc.gridwidth = 1;
//		_gbc.fill = GridBagConstraints.NONE;
//		_gbc.anchor = GridBagConstraints.WEST;
//		_gb.setConstraints(lblStartVal, _gbc);
//		_panel.add(lblStartVal);
//
//		_gbc.gridx = 13;
//		_gbc.gridy = lineNo;
//		_gbc.gridheight = 1;
//		_gbc.gridwidth = GridBagConstraints.REMAINDER;
//		_gbc.fill = GridBagConstraints.HORIZONTAL;
//		_gb.setConstraints(txtStartVal, _gbc);
//		_panel.add(txtStartVal);
//
//		lineNo++;
//		
//		_gbc.gridx = 1;
//		_gbc.gridy = lineNo;
//		_gbc.gridheight = 1;
//		_gbc.gridwidth = 1;
//		_gbc.fill = GridBagConstraints.NONE;
//		_gbc.anchor = GridBagConstraints.WEST;
//		_gb.setConstraints(lblEndVal, _gbc);
//		_panel.add(lblEndVal);
//
//		_gbc.gridx = 2;
//		_gbc.gridy = lineNo;
//		_gbc.gridheight = 1;
//		_gbc.gridwidth = 6;
//		_gbc.fill = GridBagConstraints.HORIZONTAL;
//		_gb.setConstraints(txtEndVal, _gbc);
//		_panel.add(txtEndVal);
//
//		_gbc.gridx = 12;
//		_gbc.gridy = lineNo;
//		_gbc.gridheight = 1;
//		_gbc.gridwidth = 1;
//		_gbc.fill = GridBagConstraints.NONE;
//		_gbc.anchor = GridBagConstraints.WEST;
//		_gb.setConstraints(lblIncr, _gbc);
//		_panel.add(lblIncr);
//
//		_gbc.gridx = 13;
//		_gbc.gridy = lineNo;
//		_gbc.gridheight = 1;
//		_gbc.gridwidth = GridBagConstraints.REMAINDER;
//		_gbc.fill = GridBagConstraints.HORIZONTAL;
//		_gb.setConstraints(txtIncr, _gbc);
//		_panel.add(txtIncr);
		// END KGU 2015-10-30: Design study 1

		// START KGU 2015-10-30: Design study 2
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
		// END KGU 2015-10-30: Design study 2
		
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
		_gb.setConstraints(lblParserWarnings, _gbc);
		_panel.add(lblParserWarnings);

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
    		txtText.setText(For.composeForClause(txtVariable.getText(), txtStartVal.getText(), txtEndVal.getText(), incr));
    	}
    	else if (source == txtText)
    	{
    		//String text = For.unifyOperators(txtText.getText());	// Now done by splitForClause
    		String text = txtText.getText();
    		String[] forFractions = For.splitForClause(text);
    		if (forFractions.length >= 3)
    		{
    			txtVariable.setText(forFractions[0]);
    			txtStartVal.setText(forFractions[1]);
    			txtEndVal.setText(forFractions[2]);
    			txtIncr.setText(forFractions[3]);
    		}
    		if (forFractions[4].isEmpty() || forFractions[3].equals(forFractions[4]))
    		{
    			lblParserWarnings.setText("");
    		}
    		else
    		{
    			lblParserWarnings.setText("<" + forFractions[4] + "> is no valid integer constant");
    		}
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
	
	public void enableTextFields(boolean inputAsText)
	{
		txtVariable.setEnabled(!inputAsText);
		txtStartVal.setEnabled(!inputAsText);
		txtEndVal.setEnabled(!inputAsText);
		txtIncr.setEnabled(!inputAsText);
		txtText.setEnabled(inputAsText);
		lblParserWarnings.setVisible(inputAsText);
	}


}
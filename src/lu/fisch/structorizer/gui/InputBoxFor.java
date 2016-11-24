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
 ******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    This dialog allows editing the properties of FOR elements in a more specific way.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date        Description
 *      ------          ----        -----------
 *      Kay Gürtzig     2015-10-12  First Issue
 *      Kay Gürtzig     2015-11-01  Mutual text field update and error detection accomplished
 *      Kay Gürtzig     2015-12-04  frame width increased (-> 600)
 *      Kay Gürtzig     2016-03-20  Enhancement #84/#135: FOR-IN / FOREACH paradigm considered
 *      Kay Gürtzig     2016-07-14  Enh. #180: Initial focus dependent on switchTextComment mode (KGU#169)
 *      Kay Gürtzig     2016-09-23  Issue #243: Message translations, more messages
 *      Kay Gürtzig     2016-09-24  Enh. #250 Partial GUI redesign - now loop style can actively be selected
 *      Kay Gürtzig     2016.11.02  Issue #81: Workaround for lacking DPI awareness
 *      Kay Gürtzig     2016.11.09  Issue #81: Scale factor no longer rounded but ensured to be >= 1
 *      Kay Gürtzig     2016.11.11  Issue #81: DPI-awareness workaround for checkboxes/radio buttons,
 *                                  Bugfix #288: Behaviour on clicking the selected one of the radio buttons fixed
 *      Kay Gürtzig     2016.11.21  Issue #284: Opportunity to scale up/down the TextField fonts by Ctrl-Numpad+/-
 *      Kay Gürtzig     2016.11.22  stepFor label mended; issue #284: Font resizing buttons added
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *      - This editor has three modes:
 *        1. Structured editing of traditional counter loops via specific fields, text automatically composed
 *        2. Structured editing as traversing loop via a variable name and a value list field, text is
 *           automatically composed
 *        3. full text editing, editor splits it to fill the structured counter fields (strong syntax support),
 *           as soon as one of the keys for FOR IN loops occurs, a FOR-IN loop is assumed and the splitting
 *           switches (weaker syntax support), and vice versa
 *      - The user may actively switch between mode 1 and 2, usually losing most of the data, though. From mode
 *        1 to mode 2 a conversion of the number sequence to a number list is attempted.   
 *
 ******************************************************************************************************
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.parsers.D7Parser;
import lu.fisch.utils.StringList;

/**
 * @author kay
 *
 */
@SuppressWarnings("serial")
public class InputBoxFor extends InputBox implements ItemListener {
	
	private static int IBF_PREFERRED_WIDTH = 600;
	private final static int MAX_ENUMERATION_ITEMS = 25;

	// START KGU#61 2016-09-23: Enh. #250 - Additional field set for FOR-IN loops
	private boolean isTraversingLoop = false;
	protected JRadioButton rbCounting;
	protected JRadioButton rbTraversing;
	// END KGU#61 2016-09-23

	protected JLabel lblVarDesignation/* = new JLabel("Counter variable")*/;
	protected JLabel lblFirstValueLabel/* = new JLabel("Start value")*/;
	protected JLabel lblEndVal/* = new JLabel("End value")*/;
	protected JLabel lblIncr/* = new JLabel("Increment")*/;
	//protected JLabel lblPreFor/* = new JLabel(D7Parser.keywordMap.get("preFor"))*/;
	protected JLabel lblPostFor/* = new JLabel(D7Parser.keywordMap.get("postFor"))*/;
	protected JLabel lblAsgnmt/* = new JLabel(" <- ")*/;
	protected JLabel lblStepFor/* = new JLabel(D7Parser.keywordMap.get("steptFor"))*/;
	protected JTextField txtParserInfo/* = new JTextField("")*/;
	protected JTextField txtVariable/* = new JTextField(20)*/;
	protected JTextField txtStartVal/* = new JTextField(10)*/;
	protected JTextField txtEndVal/* = new JTextField(20)*/;
	protected JTextField txtIncr/* = new JTextField(10)*/;
	protected JCheckBox chkTextInput/* = new JCheckBox("Full Text Editing")*/;
	// START KGU#61 2016-09-23: Enh. #250 -Additional field set for FOR-IN loops
	//protected JLabel lblPreForIn/* = new JLabel(D7Parser.keywordMap.get("preFor")In)*/;
	protected JLabel lblpostForIn/* = new JLabel(D7Parser.keywordMap.get("postFor")In)*/;
	protected JTextField txtVariableIn/* = new JTextField(20)*/;
	protected JTextField txtValueList/* = new JTextField(60)*/;	
	// END KGU#61 2016-09-23
	// START KGU#247 2016-09-23: Issue #243 Forgotten translation
	protected LangTextHolder lblVariable = new LangTextHolder("Counter variable");
	protected LangTextHolder lblTraversingVariable = new LangTextHolder("Element variable");
	protected LangTextHolder lblValueList = new LangTextHolder("Value list or array");
	protected LangTextHolder lblStartVal = new LangTextHolder("Start value");
	protected LangTextHolder msgInvalidIncrement = new LangTextHolder("<%> is no valid integer constant");
	protected LangTextHolder msgMissingBrace1 = new LangTextHolder("Value list must begin with '{'");
	protected LangTextHolder msgMissingBrace2 = new LangTextHolder("Value list must end with '}'");
	protected LangTextHolder msgSeparateWithComma = new LangTextHolder("Within braces, commas must separate values.");
	protected LangTextHolder msgEnsureReturnedArray = new LangTextHolder("Ensure the function returns an array.");
	protected LangTextHolder msgEnsureVariableIsArray = new LangTextHolder("Ensure that <%> is an array.");
	protected LangTextHolder msgEnterValueList = new LangTextHolder("Enter the value list for the loop.");
	protected LangTextHolder msgDiscardData = new LangTextHolder("Changing the loop style means to discard most of the data!%Do you really want to discard the data?");
	protected LangTextHolder msgAttention = new LangTextHolder("ATTENTION!");
	// END KGU#247 2016-09-23
	
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
    // START KGU#287 2016-11-02: Issue #81 (DPI awareness workaround)
    protected void setPreferredSize(double scaleFactor) {
        setSize((int)(IBF_PREFERRED_WIDTH * scaleFactor), (int)(400 * scaleFactor));        
    }
    // END KGU#287 2016-11-02
    // END KGU#169 2016-07-14

    
    /**
     * Subclassable method to add specific stuff to the Panel top
     * @param _panel the panel to be enhanced
     * @param pnPanel0c the layout constraints
     * @return number of lines (y cell units) inserted
     */
	protected int createPanelTop(JPanel _panel, GridBagLayout _gb, GridBagConstraints _gbc)
	{
        // START KGU#287 2016-11-02/09: Issue #81 (DPI awaeness workaround)
		double scaleFactor = Double.valueOf(Ini.getInstance().getProperty("scaleFactor","1"));
		if (scaleFactor < 1) scaleFactor = 1.0;		
        ImageIcon unselectedBox = null;
        ImageIcon selectedBox = null;
        // END KGU#287 2016-11-02/09

		lblVarDesignation = new JLabel("Counter variable");
		lblFirstValueLabel = new JLabel("Start value");
		lblEndVal = new JLabel("End value");
		lblIncr = new JLabel("Increment");
		//lblPreFor = new JLabel(D7Parser.keywordMap.get("preFor"));
		lblPostFor = new JLabel(D7Parser.getKeyword("postFor"));
		lblAsgnmt = new JLabel(" <- ");
		lblStepFor = new JLabel(D7Parser.getKeyword("stepFor"));
		txtParserInfo = new JTextField(300);
		txtParserInfo.setEditable(false);
		if (UIManager.getLookAndFeel().getName().equals("Nimbus"))
		{
			// It shall look inactive but must still show coloured text,
			// therefore we can't disable it.
			// This is a lighter gray than Color.LIGHT_GRAY
			txtParserInfo.setBackground(Color.decode("0xDEE1E5"));
		}
		txtVariable = new JTextField(50);
		txtStartVal = new JTextField(20);
		txtEndVal = new JTextField(50);
		txtIncr = new JTextField(20);	// Width 10
		chkTextInput = new JCheckBox("Full Text Editing");
		// START KGU#61 2016-09-23: Enh. #250 - Additional field set for FOR-IN loops
		//lblPreForIn = new JLabel(D7Parser.keywordMap.get("preFor")In);
		lblpostForIn = new JLabel(D7Parser.getKeyword("postForIn"));
		txtVariableIn = new JTextField(50);
		txtValueList = new JTextField(120);
		txtVariableIn.setEnabled(false);
		txtValueList.setEnabled(false);
		txtVariableIn.addKeyListener(this);
		txtValueList.addKeyListener(this);
		// END KGU#61 2016-09-23

		txtVariable.addKeyListener(this);
		txtStartVal.addKeyListener(this);
		txtEndVal.addKeyListener(this);
		txtIncr.addKeyListener(this);
		chkTextInput.setSelected(false); 
        // START KGU#287 2016-11-11: Issue #81 (DPI-awareness workaround)
		if (scaleFactor > 1) {
			unselectedBox = scaleToggleIcon(chkTextInput, false);
			selectedBox = scaleToggleIcon(chkTextInput, true);
			chkTextInput.setIcon(unselectedBox);
			chkTextInput.setSelectedIcon(selectedBox);
		}
        // END KGU#287 2016-11-11
		chkTextInput.addItemListener(this);
		txtText.addKeyListener(this);
		
        // START KGU#294 2016-11-21: Issue #284
		scalableComponents.addElement(txtVariable);
		scalableComponents.addElement(txtStartVal);
		scalableComponents.addElement(txtEndVal);
		scalableComponents.addElement(txtIncr);
		scalableComponents.addElement(txtVariableIn);
		scalableComponents.addElement(txtValueList);
		scalableComponents.addElement(lblAsgnmt);
		scalableComponents.addElement(lblPostFor);
		scalableComponents.addElement(lblStepFor);
		scalableComponents.addElement(lblpostForIn);		
        // END KGU#294 2016-11-21
		
		// START KGU#254 2016-09-24: Enh. #250 - GUI redesign
		rbCounting = new JRadioButton(D7Parser.getKeyword("preFor"));
		rbCounting.setActionCommand("FOR");
		rbCounting.setToolTipText("Select this if you want to count through a range of numbers.");
		
		rbTraversing = new JRadioButton(D7Parser.getKeyword("preForIn").isEmpty() ? D7Parser.getKeyword("postFor") : D7Parser.getKeyword("preForIn"));
		rbTraversing.setActionCommand("FOR-IN");
		rbTraversing.setToolTipText("Select this if you want to traverse all members of a collection.");

		ButtonGroup radioGroup = new ButtonGroup();
		radioGroup.add(rbCounting);
		radioGroup.add(rbTraversing);
		rbCounting.setSelected(!isTraversingLoop);
		rbTraversing.setSelected(isTraversingLoop);
        // START KGU#287 2016-11-11: Issue #81 (DPI-awareness workaround)
		if (scaleFactor > 1) {
			if (isTraversingLoop) {
				unselectedBox = scaleToggleIcon(rbCounting, false);
				selectedBox = scaleToggleIcon(rbTraversing, true);
			}
			else {
				unselectedBox = scaleToggleIcon(rbTraversing, false);
				selectedBox = scaleToggleIcon(rbCounting, true);			
			}
			rbCounting.setIcon(unselectedBox);
			rbCounting.setSelectedIcon(selectedBox);
			rbTraversing.setIcon(unselectedBox);
			rbTraversing.setSelectedIcon(selectedBox);
		}
		// END KGU#287 2016-11-11
		rbCounting.addActionListener(this);
		rbTraversing.addActionListener(this);
		// END KGU#254 2016-09-24

		int lineNo = 1;
		
		// TODO (KGU 2015-11-01) Grid configuration halfway works under both Windows and KDE but's still not pleasant 

		int border = (int)(5 * scaleFactor);
		_gbc.insets = new Insets(2*border, border, 0, border);
		
		_gbc.gridx = 2;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 1;
		_gbc.fill = GridBagConstraints.NONE;
		_gbc.anchor = GridBagConstraints.WEST;
		_gb.setConstraints(lblVarDesignation, _gbc);
		_panel.add(lblVarDesignation);

		_gbc.gridx = 8;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 1;
		_gbc.fill = GridBagConstraints.NONE;
		_gbc.anchor = GridBagConstraints.WEST;
		_gb.setConstraints(lblFirstValueLabel, _gbc);
		_panel.add(lblFirstValueLabel);

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

		_gbc.insets = new Insets(border, 2*border, 0, border);
		
		_gbc.gridx = 1;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 1;
		_gbc.fill = GridBagConstraints.NONE;
		_gbc.anchor = GridBagConstraints.WEST;
		_gb.setConstraints(rbCounting, _gbc);
		_panel.add(rbCounting);

		_gbc.insets = new Insets(border, border, 0, border);

		_gbc.gridx = 2;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 5;
		_gbc.weightx = 1;
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
		_gbc.weightx = 1;
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
		_gbc.weightx = 1;
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

		_gbc.insets = new Insets(border, border, 0, 2*border);

		_gbc.gridx = 17;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 4;
		_gbc.weightx = 1;
		_gbc.fill = GridBagConstraints.HORIZONTAL;
		_gb.setConstraints(txtIncr, _gbc);
		_panel.add(txtIncr);
		
		// START KGU#61 2016-09-23: Additional field set for FOR-IN loops
		lineNo++;
		
		_gbc.insets = new Insets(0, 2*border, 0, border);
		
		_gbc.gridx = 1;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 1;
		_gbc.fill = GridBagConstraints.NONE;
		_gbc.anchor = GridBagConstraints.WEST;
		_gb.setConstraints(rbTraversing, _gbc);
		_panel.add(rbTraversing);

		_gbc.insets = new Insets(0, border, 0, border);

		_gbc.gridx = 2;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 5;
		_gbc.weightx = 1;
		_gbc.fill = GridBagConstraints.HORIZONTAL;
		_gb.setConstraints(txtVariableIn, _gbc);
		_panel.add(txtVariableIn);

		_gbc.gridx = 7;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 1;
		_gbc.fill = GridBagConstraints.NONE;
		_gbc.anchor = GridBagConstraints.WEST;
		_gb.setConstraints(lblpostForIn, _gbc);
		_panel.add(lblpostForIn);

		_gbc.insets = new Insets(0, border, 0, 2*border);

		_gbc.gridx = 8;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = GridBagConstraints.REMAINDER;
		_gbc.weightx = 1;
		_gbc.fill = GridBagConstraints.HORIZONTAL;
		_gb.setConstraints(txtValueList, _gbc);
		_panel.add(txtValueList);

		// END KGU#61 2016-09-23

		lineNo++;

		_gbc.insets = new Insets(2*border, 2*border, 0, 2*border);

		_gbc.gridx = 1;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 5;
		_gbc.fill = GridBagConstraints.HORIZONTAL;
		_gb.setConstraints(chkTextInput, _gbc);
		_panel.add(chkTextInput);

		_gbc.insets = new Insets(2*border, border, 0, 2*border);

		_gbc.gridx = 8;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 13;
		_gbc.fill = GridBagConstraints.HORIZONTAL;
		//_gbc.anchor = GridBagConstraints.WEST;
		_gb.setConstraints(txtParserInfo, _gbc);
		_panel.add(txtParserInfo);

		return lineNo;
	}

	// listen to key events
	@Override
	public void keyReleased(KeyEvent kevt)
	{
		Object source = kevt.getSource();
		if (source == txtVariable || source == txtStartVal || source == txtEndVal || source == txtIncr)
		{
			transferCountingToText();
		}
		else if (source == txtVariableIn || source == txtValueList)
		{
			transferTraversingToText();
		}
		else if (source == txtText)
		{
			transferTextToFields();
			setVisibility();
			forceMinimumSize(txtParserInfo, false);
		}
		super.keyReleased(kevt);
	}

	// START KGU#254 2016-09-24: Enh. #250 - GUI redesign
	// listen to actions
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();

		if (source == rbCounting || source == rbTraversing) {
			// START KGU#291 2016-11-11: Bugfix #288 (no action on clicking the already selected mode!)
			//if (JOptionPane.showConfirmDialog(null, msgDiscardData.getText().replace("%", "\n\n"), msgAttention.getText(), JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
			if ((source == rbTraversing) == isTraversingLoop ||
					JOptionPane.showConfirmDialog(null, msgDiscardData.getText().replace("%", "\n\n"), msgAttention.getText(), JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
			// END KGU#291 2016-11-11
			{
				// Switch back the radio buttons ...
				setIsTraversingLoop(isTraversingLoop);
				// ... and do nothing
				return;
			}
			isTraversingLoop = event.getActionCommand().equals("FOR-IN");
			if (isTraversingLoop)
			{
				// Try to convert the iteration to an enumeration
				transferCountingToTraversing();
				transferTraversingToText();
			}
			else
			{
				txtVariable.setText(txtVariableIn.getText());
				txtStartVal.setText("0");
				txtEndVal.setText("0");
				txtIncr.setText("1");
				forInValueList = null;
				transferCountingToText();
			}
			setVisibility();
			if (isTraversingLoop) {
				forceMinimumSize(txtValueList, true);
			}
		} else {
			if (source == btnOK && isTraversingLoop)
			{
				// Make sure the appropriate variable name will be found
				txtVariable.setText(txtVariableIn.getText());
			}
			super.actionPerformed(event);
		}
	}

	// Listen to item state changes
	@Override
	public void itemStateChanged(ItemEvent iev) {
		if (iev.getSource() == chkTextInput)
		{
			this.enableTextFields(iev.getStateChange() == ItemEvent.SELECTED);
		}
	}
	
	private void transferCountingToText()
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

	private void transferTraversingToText()
	{
		txtText.setText(For.composeForInClause(txtVariableIn.getText(), forInValueList = txtValueList.getText()));
		checkValueList();
		forceMinimumSize(txtParserInfo, true);
	}

	private void transferCountingToTraversing()
	{
		String valueList = "";
		String start = txtStartVal.getText();
		String end = txtEndVal.getText();
		String incr = txtIncr.getText();
		if (!start.isEmpty() && !end.isEmpty())
		{
			try {
				int startVal = Integer.parseInt(start);
				int endVal = Integer.parseInt(end);
				int step = 1;
				if (!incr.isEmpty())
				{
					step = Integer.parseInt(incr);
				}
				if (step != 0 && (Math.abs(endVal - startVal)+1)/Math.abs(step) <= MAX_ENUMERATION_ITEMS)
				{
					boolean first = true;
					for (int val = startVal; (step > 0 && val <= endVal) || (step < 0 && val >= endVal); val += step)
					{
						if (first)
						{
							first = false; 
							valueList += val;
						}
						else
						{
							valueList += ("," + val);
						}
					}
				}
			}
			catch (NumberFormatException ex) {}    		
		}
		txtVariableIn.setText(txtVariable.getText());
		txtValueList.setText(forInValueList = "{" + valueList + "}");
		txtStartVal.setText("");
		txtEndVal.setText("");
		txtIncr.setText("1");
	}

	private void transferTextToFields()
	{
		//String text = For.unifyOperators(txtText.getText());	// Now done by splitForClause
		String text = txtText.getText();
		String[] forFractions = For.splitForClause(text);
		// START KGU#61 2016-03-21: Enh. #84/#135 - check whether this is a FOR-IN loop
		setIsTraversingLoop(forFractions.length >= 6 && forFractions[5] != null);
		if (isTraversingLoop)
		{
			txtVariableIn.setText(forFractions[0]);
			forInValueList = forFractions[5];
			txtValueList.setText(forInValueList);
			checkValueList();
			//			lblStartVal.setVisible(false);
			//			lblEndVal.setVisible(false);
			//			lblIncr.setVisible(false);
			//			lblPostFor.setForeground(Color.GRAY);
			//			lblAsgnmt.setText(D7Parser.keywordMap.get("postFor")In);
			//			lblStepFor.setForeground(Color.GRAY);
			txtVariable.setText("");
			txtStartVal.setText("");
			txtEndVal.setText("");
			txtIncr.setText("");
		}
		else
		{
		// END KGU#61 2016-03-21
			if (forFractions.length >= 3)
			{
				txtVariable.setText(forFractions[0]);
				txtStartVal.setText(forFractions[1]);
				txtEndVal.setText(forFractions[2]);
				txtIncr.setText(forFractions[3]);
			}
			if (forFractions[4].isEmpty() || forFractions[3].equals(forFractions[4]) && !forFractions[3].equals("0"))
			{
				txtParserInfo.setText("");
			}
			else
			{
				txtParserInfo.setForeground(Color.RED);
				// START KGU#247 2016-09-23: Issue #243: Forgotten translations
				//lblParserInfo.setText("<" + forFractions[4] + "> is no valid integer constant");
				txtParserInfo.setText(msgInvalidIncrement.getText().replace("%",forFractions[4]));
				// END KGU#247 2016-09-23
			}
			// START KGU#61 2016-03-21: Enh. #84/#135 (continued)
			txtVariableIn.setText("");
			forInValueList = null;
		}
		// END KGU#61 2016-03-21

	}
    
	public void enableTextFields(boolean inputAsText)
	{
		txtVariable.setEnabled(!inputAsText);
		txtStartVal.setEnabled(!inputAsText);
		txtEndVal.setEnabled(!inputAsText);
		txtIncr.setEnabled(!inputAsText);

		// START KGU#61 2016-09-23: Enh. #250 - Additional field set for FOR-IN loops
		rbCounting.setEnabled(!inputAsText);
		rbTraversing.setEnabled(!inputAsText);
		txtVariableIn.setEnabled(!inputAsText);
		txtValueList.setEnabled(!inputAsText);
		setVisibility();			
		// END KGU#61 2016-09-23
		
		txtText.setEnabled(inputAsText);
		txtParserInfo.setVisible(inputAsText || isTraversingLoop);
		forceMinimumSize(txtParserInfo, true);
	}

	// START KGU#61 2016-03-21: Enh. #84 - special test for FOR-IN loop
	private void checkValueList()
	{
		txtParserInfo.setForeground(Color.BLACK);
		txtParserInfo.setText("");
		if (forInValueList == null) forInValueList = "{}";
		boolean startsWithBrace = forInValueList.startsWith("{");
		boolean endsWithBrace = forInValueList.endsWith("}");
		if (startsWithBrace && !endsWithBrace)
		{				
    			txtParserInfo.setForeground(Color.RED);
				// START KGU#247 2016-09-23: Issue #243 - Forgotten translations
    			//lblParserInfo.setText("Value list must end with '}'");
    			txtParserInfo.setText(msgMissingBrace2.getText());
				// END KGU#247 2016-09-23
		}
		else if (!startsWithBrace && endsWithBrace)
		{				
			txtParserInfo.setForeground(Color.RED);
			// START KGU#247 2016-09-23: Issue #243 - Forgotten translations
			//lblParserInfo.setText("Value list should begin with '{'");
			txtParserInfo.setText(msgMissingBrace1.getText());
			// END KGU#247 2016-09-23
		}
		else if ((new Function(forInValueList)).isFunction())
		{
			txtParserInfo.setForeground(Color.BLUE);
			// START KGU#247 2016-09-23: Issue #243 - Forgotten translations
			//lblParserInfo.setText("Ensure the function returns an array.");			
			txtParserInfo.setText(msgEnsureReturnedArray.getText());
			// END KGU#247 2016-09-23
		}
		else if (forInValueList.isEmpty())
		{
			txtParserInfo.setForeground(Color.BLUE);
			// START KGU#247 2016-09-23: Issue #243 - Forgotten translations
			//lblParserInfo.setText("Enter the value list for the loop.");			
			txtParserInfo.setText(msgEnterValueList.getText());
			// END KGU#247 2016-09-23
		}
		// START KGU 2016-09-23
		else if (Function.testIdentifier(forInValueList, ""))
		{
			txtParserInfo.setForeground(Color.BLUE);
			txtParserInfo.setText(msgEnsureVariableIsArray.getText().replace("%", forInValueList));
		}
		else if (startsWithBrace && endsWithBrace)
		{
			StringList elements = Element.splitExpressionList(forInValueList.substring(1, forInValueList.length()-1).trim(), ",");
			for (int i = 0; i < elements.count(); i++)
			{
				if (Element.splitExpressionList(elements.get(i).trim(), " ").count() > 1)
				{
					txtParserInfo.setForeground(Color.RED);
					txtParserInfo.setText(msgSeparateWithComma.getText());
					break;
				}
			}
		}
		// END KGU 2016-09-23
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
    		setIsTraversingLoop(forFractions.length >= 6 && forFractions[5] != null);
    		if (isTraversingLoop)
    		{
    			//txtVariable.setText(forFractions[0]);
    			txtVariableIn.setText(forFractions[0]);
    			forInValueList = forFractions[5];
    			txtValueList.setText(forInValueList);
    			checkValueList();
    			
    			txtStartVal.setText("");
    			txtEndVal.setText("");
    			txtIncr.setText("");
    		}		
			setVisibility();
			if (isLoopDataConsistent())
			{
				chkTextInput.setSelected(false);
				this.enableTextFields(false);
			}
		}
	}
	// END KGU#61 2016-03-21
	
	// START KGU#61 2016-09-23: Enh. #250
	private void setVisibility()
	{
		if (isTraversingLoop)
		{
			lblVarDesignation.setText(lblTraversingVariable.getText());
			lblFirstValueLabel.setText(lblValueList.getText());
			txtParserInfo.setVisible(true);
			validate();
		}
		else
		{
			lblVarDesignation.setText(lblVariable.getText());
			lblFirstValueLabel.setText(lblStartVal.getText());
		}
		this.lblEndVal.setVisible(!isTraversingLoop);
		this.lblIncr.setVisible(!isTraversingLoop);
		//this.lblPreFor.setVisible(!isTraversingLoop);
		this.txtVariable.setVisible(!isTraversingLoop);
		this.lblAsgnmt.setVisible(!isTraversingLoop);
		this.txtStartVal.setVisible(!isTraversingLoop);
		this.lblPostFor.setVisible(!isTraversingLoop);
		this.txtEndVal.setVisible(!isTraversingLoop);
		this.lblStepFor.setVisible(!isTraversingLoop);
		this.txtIncr.setVisible(!isTraversingLoop);
		
		//this.lblPreForIn.setVisible(isTraversingLoop);
		this.txtVariableIn.setVisible(isTraversingLoop);
		this.lblpostForIn.setVisible(isTraversingLoop || rbCounting.getText().equals(rbTraversing.getText()));
		this.txtValueList.setVisible(isTraversingLoop);
	}
	
	public void setIsTraversingLoop(boolean isTraversing)
	{
		this.isTraversingLoop = isTraversing;
		if (isTraversing) {
			rbTraversing.setSelected(true);
		}
		else {
			rbCounting.setSelected(true);
		}
		setVisibility();
	}
	
	public For.ForLoopStyle identifyForLoopStyle()
	{
		return isTraversingLoop ? For.ForLoopStyle.TRAVERSAL : For.ForLoopStyle.COUNTER;
	}
	
	/**
	 * May be used to find out whether there is an equivalence between the full text
	 * content and the loop-style-specific input fields 
	 * @return true if full text and field contents correspond (according to the loop style)
	 */
	public boolean isLoopDataConsistent()
	{
		boolean isConsistent = true;
		String text = txtText.getText();
		String[] forFractions = For.splitForClause(text);
		if (isTraversingLoop != (forFractions.length >= 6 && forFractions[5] != null))
		{
			isConsistent = false;
		}
		else if (isTraversingLoop)		
		{
			//txtVariable.setText(forFractions[0]);
			isConsistent = (txtVariableIn.getText().equals(forFractions[0]))
					&& forInValueList != null && forInValueList.equals(forFractions[5]);
		}
		else {
			isConsistent = (txtVariable.getText().equals(forFractions[0]))
					&& txtStartVal.getText().equals(forFractions[1])
					&& txtEndVal.getText().equals(forFractions[2])
					&& txtIncr.getText().equals(forFractions[3]);
		}
		return isConsistent;
	}
	
	// Workaround to ensure a sufficiently wide message and value list field
	// FIXME: Usually not works before the first editing activity.
	private void forceMinimumSize(JTextField _textField, boolean _validate)
	{
		Dimension size = _textField.getMinimumSize();
		//System.out.println("size: " + size.width + " x " + size.height);
		size.width = Math.max(IBF_PREFERRED_WIDTH/2, size.width);
		_textField.setMinimumSize(isTraversingLoop ? size : null);
		if (_validate) {
			this.validate();
		}
	}
	
    /**
     * This method is called on opening after setLocale and before re-packing.
     * Replaces markers in translated texts.
     */
    @Override
    protected void adjustLangDependentComponents()
    {
    	super.adjustLangDependentComponents();
    	setVisibility();
    }

	// END KGU#61 2016-09-23
	
}
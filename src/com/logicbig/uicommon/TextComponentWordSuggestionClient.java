/*
    Copyright (C) 2016-2022 LogicBig
    
    This is source code provided in tutorial examples licensed under
    Creative Commons Attribution-SharAlike 3.0 Unported
    (https://creativecommons.org/licenses/by-sa/3.0/)
 */

package com.logicbig.uicommon;

/******************************************************************************************************
*
*      Revision List
*
*      Author          Date        Description
*      ------          ----        -----------
*      Kay Gürtzig     2022-08-19  Copied from
*                                  https://www.logicbig.com/tutorials/java-swing/text-suggestion-component.html
*                                  Modified to allow a case-ignorant matching, adopting the case of the
*                                  selected value over the user-written prefix
*
******************************************************************************************************
*
*      Comment:
*      
*
******************************************************************************************************///

import java.awt.Point;
import java.awt.geom.Rectangle2D;

import java.util.List;
import java.util.function.Function;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;

/**
 * Matches individual words instead of complete text
 * 
 * @author LogicBig (www.logicbic.com)
 */
public class TextComponentWordSuggestionClient implements SuggestionClient<JTextComponent> {

	private Function<String, List<String>> suggestionProvider;

	/**
	 * 
	 * @param suggestionProvider
	 */
	public TextComponentWordSuggestionClient(Function<String, List<String>> suggestionProvider) {
		this.suggestionProvider = suggestionProvider;
	}

	@Override
	public Point getPopupLocation(JTextComponent invoker) {
		int caretPosition = invoker.getCaretPosition();
		try {
			Rectangle2D rectangle2D = invoker.modelToView2D(caretPosition);
			return new Point((int) rectangle2D.getX(), (int) (rectangle2D.getY() + rectangle2D.getHeight()));
		} catch (BadLocationException e) {
			System.err.println(e);
		}
		return null;
	}

	@Override
	public void setSelectedText(JTextComponent tp, String selectedValue) {
		int cp = tp.getCaretPosition();
		try {
			if (cp == 0 || tp.getText(cp - 1, 1).trim().isEmpty()) {
				tp.getDocument().insertString(cp, selectedValue, null);
			} else {
				// Remark: In a specific implementation the word scanner might be a parameter
				int previousWordIndex = Utilities.getPreviousWord(tp, cp);
				String text = tp.getText(previousWordIndex, cp - previousWordIndex);
				if (selectedValue.startsWith(text)) {
					tp.getDocument().insertString(cp, selectedValue.substring(text.length()), null);
				// START KGU 2022-08-19: Adapted for case-insensitive replacement
				} else if (selectedValue.toLowerCase().startsWith(text.toLowerCase())) {
					tp.setSelectionStart(previousWordIndex);
					tp.setSelectionEnd(cp);
					tp.replaceSelection(selectedValue);
				// END KGU 2022-08-19	
				} else {
					// In case of a mismatch just append the selectedValue (???)
					tp.getDocument().insertString(cp, selectedValue, null);
				}
			}
		} catch (BadLocationException e) {
			System.err.println(e);
		}
	}

	@Override
	public List<String> getSuggestions(JTextComponent tp) {
		try {
			int cp = tp.getCaretPosition();
			if (cp != 0) {
				String text = tp.getText(cp - 1, 1);
				if (text.trim().isEmpty()) {
					return null;
				}
			}
			// Remark: In a specific implementation the word scanner might be a parameter
			int previousWordIndex = Utilities.getPreviousWord(tp, cp);
			String text = tp.getText(previousWordIndex, cp - previousWordIndex);
			return suggestionProvider.apply(text.trim());
		} catch (BadLocationException e) {
			System.err.println(e);
		}
		return null;
	}
}

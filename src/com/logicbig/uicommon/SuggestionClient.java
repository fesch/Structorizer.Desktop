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
*                                  Method comments added.
*
******************************************************************************************************
*
*      Comment:
*      
*
******************************************************************************************************///

import java.awt.Point;
import javax.swing.JComponent;
import java.util.List;

/**
 * This is a hookup interface for the {@link SuggestionDropDownDecorator}.
 * An implementation works on a specific component. This interface also
 * allows the component on how it wants to display suggestions e.g.
 * word by word or on the entire text etc.
 * 
 * @author LogicBig (www.logicbic.com)
 *
 * @param <T> a {@link JComponent} subclass
 */
public interface SuggestionClient<T extends JComponent> {

	/**
	 * Is to retrieve the view coordinates for the suggestion popup (usually
	 * beneath the caret position)
	 * 
	 * @param invoker - the decorated component
	 * @return the view-related point for the pulldown list
	 */
	Point getPopupLocation(T invoker);

	/**
	 * Is to place the chosen text suggestion {@code selectedValue} into the
	 * component {@code invoker} at the appropriate position.
	 * 
	 * @param invoker - the decorated component
	 * @param selectedValue - the chosen text insertion
	 * 
	 * @see #getSuggestions(JComponent)
	 */
	void setSelectedText(T invoker, String selectedValue);

	/**
	 * Is to derive a list of suited texts to be proposed for insertion. They
	 * usually have to match the last inserted text part as prefix but this is
	 * up to the implementing class and must coincide with 
	 * {@link #setSelectedText(JComponent, String)}
	 * 
	 * @param invoker - the decorated component
	 * @return - a list of strings fitting for a text completion
	 * 
	 * @see #setSelectedText(JComponent, String)
	 */
	List<String> getSuggestions(T invoker);

}

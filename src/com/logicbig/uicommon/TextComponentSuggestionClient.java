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
*
******************************************************************************************************
*
*      Comment:
*      
*
******************************************************************************************************///

import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.List;
import java.util.function.Function;

/**
 * Matches entire text instead of separate words
 * 
 * @author LogicBig (www.logicbic.com)
 */
public class TextComponentSuggestionClient implements SuggestionClient<JTextComponent> {

    private Function<String, List<String>> suggestionProvider;

    public TextComponentSuggestionClient(Function<String, List<String>> suggestionProvider) {
        this.suggestionProvider = suggestionProvider;
    }

    @Override
    public Point getPopupLocation(JTextComponent invoker) {
        return new Point(0, invoker.getPreferredSize().height);
    }

    @Override
    public void setSelectedText(JTextComponent invoker, String selectedValue) {
        invoker.setText(selectedValue);
    }

    @Override
    public List<String> getSuggestions(JTextComponent invoker) {
        return suggestionProvider.apply(invoker.getText().trim());
    }
}


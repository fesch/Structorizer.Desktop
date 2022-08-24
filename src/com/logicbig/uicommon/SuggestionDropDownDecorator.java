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

import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/**
 * The decorator can work with any kind of (text) component which allows
 * a user to enter or type text.<b/>
 * Its task is to suggest insertable texts (usually words) from a word list that are
 * matching a partially inserted string via a dropdown list
 * 
 * @author LogicBig (www.logicbig.com)
 *
 * @param <T> a {@link JComponent} subclass
 */
public class SuggestionDropDownDecorator<T extends JComponent> {

	private final T invoker;
	private final SuggestionClient<T> suggestionClient;
	private JPopupMenu popupMenu;
	private JList<String> listComp;
	DefaultListModel<String> listModel;
	private boolean disableTextEvent;

	public SuggestionDropDownDecorator(T invoker, SuggestionClient<T> suggestionClient) {
		this.invoker = invoker;
		this.suggestionClient = suggestionClient;
	}

	public static <C extends JComponent> void decorate(C component, SuggestionClient<C> suggestionClient) {
		SuggestionDropDownDecorator<C> d = new SuggestionDropDownDecorator<>(component, suggestionClient);
		d.init();
	}

	public void init() {
		initPopup();
		initSuggestionCompListener();
		initInvokerKeyListeners();
	}

	private void initPopup() {
		popupMenu = new JPopupMenu();
		listModel = new DefaultListModel<>();
		listComp = new JList<>(listModel);
		listComp.setBorder(BorderFactory.createEmptyBorder(0, 2, 5, 2));
		listComp.setFocusable(false);
		popupMenu.setFocusable(false);
		popupMenu.add(listComp);
	}

	private void initSuggestionCompListener() {
		if (invoker instanceof JTextComponent) {
			JTextComponent tc = (JTextComponent) invoker;
			tc.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void insertUpdate(DocumentEvent e) {
					update(e);
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					update(e);
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					update(e);
				}

				private void update(DocumentEvent e) {
					if (disableTextEvent) {
						return;
					}
					SwingUtilities.invokeLater(() -> {
						List<String> suggestions = suggestionClient.getSuggestions(invoker);
						if (suggestions != null && !suggestions.isEmpty()) {
							showPopup(suggestions);
						} else {
							popupMenu.setVisible(false);
						}
					});
				}
			});
		}//todo init invoker components other than text components
	}

	private void showPopup(List<String> suggestions) {
		listModel.clear();
		suggestions.forEach(listModel::addElement);
		Point p = suggestionClient.getPopupLocation(invoker);
		if (p == null) {
			return;
		}
		popupMenu.pack();
		listComp.setSelectedIndex(0);
		popupMenu.show(invoker, (int) p.getX(), (int) p.getY());
	}

	private void initInvokerKeyListeners() {
		//not using key inputMap cause that would override the original handling
		invoker.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					selectFromList(e);
				} else if (e.getKeyCode() == KeyEvent.VK_UP) {
					moveUp(e);
				} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					moveDown(e);
				} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					popupMenu.setVisible(false);
				}
			}
		});
	}

	private void selectFromList(KeyEvent e) {
		if (popupMenu.isVisible()) {
			int selectedIndex = listComp.getSelectedIndex();
			if (selectedIndex != -1) {
				popupMenu.setVisible(false);
				String selectedValue = listComp.getSelectedValue();
				disableTextEvent = true;
				suggestionClient.setSelectedText(invoker, selectedValue);
				disableTextEvent = false;
				e.consume();
			}
		}
	}

	private void moveDown(KeyEvent keyEvent) {
		if (popupMenu.isVisible() && listModel.getSize() > 0) {
			int selectedIndex = listComp.getSelectedIndex();
			if (selectedIndex < listModel.getSize()) {
				listComp.setSelectedIndex(selectedIndex + 1);
				keyEvent.consume();
			}
		}
	}

	private void moveUp(KeyEvent keyEvent) {
		if (popupMenu.isVisible() && listModel.getSize() > 0) {
			int selectedIndex = listComp.getSelectedIndex();
			if (selectedIndex > 0) {
				listComp.setSelectedIndex(selectedIndex - 1);
				keyEvent.consume();
			}
		}
	}
}
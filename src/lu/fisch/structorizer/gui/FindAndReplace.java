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

import java.awt.Dialog;
import java.awt.Frame;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.IElementSequence;
import lu.fisch.structorizer.elements.IElementSequence.Iterator;
import lu.fisch.structorizer.locales.LangDialog;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Find & Replace dialog for Structorizer
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.05.30      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

/**
 * @author Kay Gürtzig
 * A diaolog providing the usual tools to search a (sub) diagram for Elements matching certain string
 * patterns with the opportunity to replace the text parts by other patterns
 */
public class FindAndReplace extends LangDialog {

	/**
	 * Specifies in what information the matching is to be performed.
	 * @author kay
	 */
	public enum Wherein { TEXT_ONLY, COMMENT_ONLY, TEXT_AND_COMMENT };
	
	/**
	 * Allows to formulate sets of interesting element types
	 * @author kay
	 */
	public enum ElementType { INSTRUCTION, ALTERNATIVE, CASE, FOR, WHILE, REPEAT, FOREVER, CALL, JUMP, PARALLEL };
	
	/**
	 * Specifies the search scope
	 */
	public enum Scope { CURRENT_SELECTION, DIAGRAM, OPENED_DIAGRAMS };
	
	private Diagram diagram;
	// TODO We need:
	protected JComboBox<String> cmbSearchPattern;
	protected JComboBox<String> cmbPeplacePattern;
	protected JCheckBox chkReplace;
	protected JCheckBox chkCaseSensitive;
	protected JCheckBox chkWholeWord;	// Not sensible with regex search
	protected JCheckBox chkRegEx;
	protected JCheckBox[] chkElementTypes;
	protected JComboBox<Scope> cmbScope; // current diagram, current selection, all open diagrams (or as radio group?)
	protected JComboBox<Wherein> cmbWherein; // current diagram, current selection, all open diagrams (or as radio group?)
	protected JButton btnFind;
	protected JButton btnReplaceFind;
	protected JButton btnReplace;
	protected JButton btnReplaceAll;
	protected JScrollPane scrResults;		// Scroll pane for the search results list lstResults
	protected JList<Element> lstResults;	// Search results, to be represented by icon and first line each
	protected JTextPane txtPane;			// For the find result / preview, editable
	protected StyledDocument doc = null;	// Copy of the found element text / comment with markers
	
	/**
	 * 
	 */
	public FindAndReplace(Diagram _diagram) {
		diagram = _diagram;
		initComponents();
	}

	/**
	 * @param owner
	 */
	public FindAndReplace(Frame owner) {
		super(owner);
		initComponents();
	}

	/**
	 * @param owner
	 */
	public FindAndReplace(Dialog owner) {
		super(owner);
		initComponents();
	}

	/**
	 * @param owner
	 * @param modal
	 */
	public FindAndReplace(Frame owner, boolean modal) {
		super(owner, modal);
		// TODO Auto-generated constructor stub
	}
	
	private void initComponents()
	{
		// TODO ...
		ElementType[] types = ElementType.values();
		chkElementTypes = new JCheckBox[types.length];
		for (int i = 0; i < types.length; i++) {
			chkElementTypes[i].setText(types[i].name());
			// FIXME Save configuration in ini file? 
		}
		// ...
	}

	private LinkedList<Element> findElements(IElementSequence _scope, String _pattern, boolean _isRegex, boolean _deeply, Wherein _wherein, Set<ElementType> _types)
	{
		boolean inText = _wherein != Wherein.COMMENT_ONLY;
		boolean inComment = _wherein != Wherein.TEXT_ONLY;
		Set<String> classNames = null;
		if (_types != null) {
			classNames = new HashSet<String>();
			for (ElementType type: _types) {
				classNames.add(type.name());
			}
		}
		LinkedList<Element> elements = new LinkedList<Element>();
		Iterator iter = _scope.iterator(_deeply);
		while (iter.hasNext()) {
			Element ele = iter.next();
			if (classNames == null || classNames.contains(ele.getClass().getSimpleName().toUpperCase())) {
				String text = ele.getText().getText();
				String comment = ele.getComment().getText();
				if (_isRegex && (inText && text.matches(_pattern) || inComment && comment.matches(_pattern))
					|| !_isRegex && (inText && text.contains(_pattern) || inComment && comment.contains(_pattern))) {
					elements.add(ele);
				}
			}
		}
		return elements;
	}
	
	/**
	 * @param args - command line arguments
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

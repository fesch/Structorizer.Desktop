/*
    Structorizer
    A little tool which you can use to create Nassi-Shneiderman Diagrams (NSD)

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
package lu.fisch.structorizer.locales;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    JTree subclass with Structorizer locale support.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2019-01-011     First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

/**
 * JTree subclass with Structorizer locale support
 * @author Kay Gürtzig
 */
@SuppressWarnings("serial")
public class LangTree extends JTree {

	/**
	 * 
	 */
	public LangTree() {
		Locales.getInstance().register(this);
	}

	/**
	 * @param value
	 */
	public LangTree(Object[] value) {
		super(value);
		Locales.getInstance().register(this);
	}

	/**
	 * @param value
	 */
	public LangTree(Vector<?> value) {
		super(value);
		Locales.getInstance().register(this);
	}

	/**
	 * @param value
	 */
	public LangTree(Hashtable<?, ?> value) {
		super(value);
		Locales.getInstance().register(this);
	}

	/**
	 * @param root
	 */
	public LangTree(TreeNode root) {
		super(root);
		Locales.getInstance().register(this);
	}

	/**
	 * @param newModel
	 */
	public LangTree(TreeModel newModel) {
		super(newModel);
		Locales.getInstance().register(this);
	}

	/**
	 * @param root
	 * @param asksAllowsChildren
	 */
	public LangTree(TreeNode root, boolean asksAllowsChildren) {
		super(root, asksAllowsChildren);
		Locales.getInstance().register(this);
	}

}

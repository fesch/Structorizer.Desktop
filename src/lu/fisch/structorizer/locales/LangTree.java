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
 *      Kay Gürtzig     2019-01-11      First Issue
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
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

/**
 * JTree subclass with Structorizer locale support
 * @author Kay Gürtzig
 */
@SuppressWarnings("serial")
public class LangTree extends JTree {

	/**
	 * Returns a {@link LangTree} with a sample model.<br/>
	 * The default model used by the tree defines a leaf node as any node
	 * without children.
	 *
	 * @see DefaultTreeModel#asksAllowsChildren
	 */
	public LangTree() {
		Locales.getInstance().register(this);
	}

	/**
	 * Returns a {@link LangTree} with each element of the
	 * specified array as the
	 * child of a new root node which is not displayed.<br/>
	 * By default, the tree defines a leaf node as any node without
	 * children.
	 *
	 * @param value - an array of <code>Object</code>s
	 * @see #LangTree(Vector)
	 */
	public LangTree(Object[] value) {
		super(value);
		Locales.getInstance().register(this);
	}

	/**
	 * Returns a LangTree with each element of the specified {@link Vector}
	 * {@code value} as the child of a new root node which is not displayed.
	 * By default, the tree defines a leaf node as any node without children.
	 * 
	 * @param value - a {@link Vector}
	 * @see #LangTree(Object[])
	 */
	public LangTree(Vector<?> value) {
		super(value);
		Locales.getInstance().register(this);
	}

	/**
     * Returns a <code>LangTree</code> created from {@link Hashtable} {@code value}
     * which does not display with root.
     * Each value-half of the key/value pairs in the <code>HashTable</code>
     * becomes a child of the new root node. By default, the tree defines
     * a leaf node as any node without children.
     * 
	 * @param value - a {@link Hashtable}
	 */
	public LangTree(Hashtable<?, ?> value) {
		super(value);
		Locales.getInstance().register(this);
	}

	/**
	 * Returns a LangTree with the specified {@link TreeNode} {@code root}
	 * as its root, which displays the root node. By default, the tree
	 * defines a leaf node as any node without children.
	 * 
	 * @param root - a {@link TreeNode} object
	 * @see #LangTree(TreeNode, boolean)
	 */
	public LangTree(TreeNode root) {
		super(root);
		Locales.getInstance().register(this);
	}

	/**
	 * Returns an instance of LangTree which displays the root node
	 * -- the tree is created using the specified data model {@code newModel.}
	 * 
	 * @param newModel - the {@link TreeModel} to use as the data model
	 */
	public LangTree(TreeModel newModel) {
		super(newModel);
		Locales.getInstance().register(this);
	}

	/**
	 * Returns a LangTree with the specified {@link TreeNode} {@code root} as
	 * its root, which displays the root node and which decides whether
	 * a node is a leaf node in the specified manner.
	 * @param root - a {@link TreeNode} object
	 * @param asksAllowsChildren - if {@code false}, any node without
	 * children is a leaf node; if {@code true}, only nodes that do not
	 * allow children are leaf nodes.
	 */
	public LangTree(TreeNode root, boolean asksAllowsChildren) {
		super(root, asksAllowsChildren);
		Locales.getInstance().register(this);
	}

}

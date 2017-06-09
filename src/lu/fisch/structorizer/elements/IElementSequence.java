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

package lu.fisch.structorizer.elements;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.Vector;

//import lu.fisch.structorizer.gui.SelectedSequence;

/******************************************************************************************************
*
*      Author:         Kay Guertzig
*
*      Description:    This interface allows sequences of Elements to be handled in a unified way.
*
******************************************************************************************************
*
*      Revision List
*
*      Author           Date            Description
*      ------           ----            -----------
*      Kay Gürtzig      2015.11.23      First issue (KGU#87).
*      Kay Gürtzig      2016.10.13      Enh. #277: Method setDisabled(boolean) added
*      Kay Gürtzig      2017.05.30      Enh. #415: Equipped with a tree-capable Iterator class
*
******************************************************************************************************
*
*      Comment:		/
*
******************************************************************************************************///

/**
 * @author Kay Gürtzig
 *
 */
public interface IElementSequence {

	// START KGU#324 2017-05-30: Enh. #373, #415
	/**
	 * Iterates forward or backward through this element sequence and (in pre-order manner)
	 * its substructure if allowed to
	 * @author Kay Gürtzig
	 */
	public class Iterator implements java.util.Iterator<Element> {

		private IElementSequence top;
		private IElementSequence current;
		private Stack<Integer> positions = new Stack<Integer>();
		private boolean descend = false;
		
		private Iterator(IElementSequence _scope, boolean _descendTree)
		{
			this.top = _scope;
			this.current = _scope;
			this.descend = _descendTree;
		}
		
		@Override
		public boolean hasNext() {
 			return getNext(false) != null;
		}

		@Override
		public Element next() {
			Element next = this.getNext(true);
			if (next == null) {
				throw new NoSuchElementException("Diagram subtree exhauseted");
			}
			return next;
		}
		
		public boolean hasPrevious() {
			return this.getPrevious(false) != null;
		}

		public Element previous() {
			Element prev = this.getPrevious(true);
			if (prev == null) {
				throw new NoSuchElementException("Diagram subtree exhauseted");
			}
			return prev;
		}
		
		private Element getNext(boolean move) {
			Element next = null;
			//IElementSequence seq = current;
			int level = positions.size() - 1;
			int at = (level >= 0) ? positions.peek() : -1;
			if (at < 0 && top.getSize() > 0) {
				next = top.getElement(0);
				if (move) {
					current = top;
					positions.push(0);
				}
				return next;
			}
			Element el = current.getElement(at);
			// Try to get downwards or sidewards first
			if (descend) {
				if (el instanceof ILoop) {
					Subqueue body = ((ILoop)el).getBody();
					if (body.getSize() > 0) {
						next = body.getElement(0);
					}
				}
				else if (el instanceof Alternative) {
					if (((Alternative)el).qTrue.getSize() > 0) {
						next = ((Alternative)el).qTrue.getElement(0);
					}
					else if (((Alternative)el).qFalse.getSize() > 0) {
						next = ((Alternative)el).qFalse.getElement(0);
					}
				}
				else if (el instanceof Case || el instanceof Parallel) {
					Vector<Subqueue> subqueues = (el instanceof Case) ? ((Case)el).qs : ((Parallel)el).qs;
					for (int i = 0; next == null && i < subqueues.size(); i++) {
						if (subqueues.get(i).getSize() > 0) {
							next = subqueues.get(i).getElement(0);
						}
					}
				}
			}
			if (next != null && move) {
				current = (Subqueue)next.parent;
				positions.push(0);
			}
			// el might still be atomic, so look for a successor (tree neighbour) 
			else if (at < current.getSize()-1) {
				next = current.getElement(at+1);
				if (move) {
					positions.pop();
					positions.push(at+1);
				}
			}
			// Try to go upwards - we stop at a level where there is an un-exhausted neighbour left
			IElementSequence seq = current.getSubqueue();
			while (next == null && level > 0) {
				// Half a level up
				el = ((Subqueue)seq).parent;	// Check if the Subqueue's parent is a branching element (in this case try another branch)
				if (el instanceof Alternative && seq == ((Alternative)el).qTrue && ((Alternative)el).qFalse.getSize() > 0) {
					seq = (Subqueue)((Alternative)el).qFalse;
					next = seq.getElement(0);
					if (move) {
						while (positions.size() > level) {
							positions.pop();
						}
						current = seq;
						positions.push(0);
					}
				}
				else if (el instanceof Case || el instanceof Parallel) {
					Vector<Subqueue> subqueues = (el instanceof Case) ? ((Case)el).qs : ((Parallel)el).qs;
					// First identify the current subqueue
					boolean found = false;
					for (int i = 0; next != null && i < subqueues.size(); i++) {
						if (!found && seq == subqueues.get(i)) {
							found = true;
						}
						else if (found && subqueues.get(i).getSize() > 0) {
							seq = subqueues.get(i);
							next = current.getElement(0);
							if (move) {
								while (positions.size() > level) {
									positions.pop();
								}
								current = seq;
								positions.push(0);
							}
						}
					}
				}
				if (next == null) {
					// Entire subqueue level up
					seq = (Subqueue)((Subqueue)seq).parent.parent;
					if (seq == top.getSubqueue()) {
						seq = top;
					}
					level--;
					at = positions.get(level);
					if (at+1 < seq.getSize()) {
						next = seq.getElement(at+1);
					}
					
					if (next != null && move) {
						while (positions.size() > level) {
							positions.pop();
						}
						current = seq;
						positions.push(at+1);
					}
				}
			}
			return next;
		}

		private Element getPrevious(boolean move) {
			Element prev = null;
			//IElementSequence seq = current;
			int level = positions.size() - 1;
			int at = (level >= 0) ? positions.peek() : -1;
			if (at < 0 && top.getSize() > 0) {
				return getLastInSubtree(top, top.getSize()-1, move);
			}
			IElementSequence seq = current;
			while (level >= 0 && prev == null) {
				if (at > 0) {
					if (move) {
						positions.pop();
					}
					prev = getLastInSubtree(seq, at-1, move);
				}
				else if (level > 0) {
					// descending must have been allowed
					if (move) {
						positions.pop();
					}
					Element el = ((Subqueue)seq).parent;
					if (el instanceof Alternative && seq == ((Alternative)el).qFalse && ((Alternative)el).qTrue.getSize() > 0) {
						prev = getLastInSubtree(((Alternative)el).qTrue, ((Alternative)el).qTrue.getSize()-1, move);
					}
					else if (el instanceof Case || el instanceof Parallel) {
						boolean found = false;
						Vector<Subqueue> subqueues = (el instanceof Case) ? ((Case)el).qs : ((Parallel)el).qs;
						for (int i = subqueues.size()-1; prev == null && i >= 0; i--) {
							if (seq == subqueues.get(i)) {
								found = true;
							}
							else if (found && subqueues.get(i).getSize() > 0) {
								prev = getLastInSubtree(subqueues.get(i), subqueues.get(i).getSize()-1, move);
							}
						}
					}
					// Nothing found on the half stage? Then get an entire subqueue level up
					if (prev == null) {
						seq = (Subqueue)((Subqueue)seq).parent.parent;
						if (seq == top.getSubqueue()) {
							seq = top;
						}
						at = positions.get(level-1);
						// Now the subtree root element itself is due.
						prev = seq.getElement(at);
					}
				}
				else {
					// No level above to go to
					at = -1;
				}
				level--;
			}
			if (prev != null && move) {
				current = (Subqueue)prev.parent;
				if (current == top.getSubqueue()) {
					current = top;
				}
			}
			return prev;
		}

		private Element getLastInSubtree(IElementSequence seq, int index, boolean move) {
			// An element at position index in seq msut exist, otherwise we wouldn't have been called.
			// if it hasn't got a substructure or we aren't allow to walk it then this is already the result.
			Element el = seq.getElement(index);
			if (move) {
				current = seq;
				positions.push(index);
			}
			if (descend && !(el instanceof Instruction)) {
				if (el instanceof ILoop) {
					Subqueue body = ((ILoop)el).getBody();
					if (body.getSize() > 0) {
						el = getLastInSubtree(body, body.getSize() - 1, move);
					}
				}
				else if (el instanceof Alternative) {
					if (((Alternative)el).qFalse.getSize() > 0) {
						el = getLastInSubtree(((Alternative)el).qFalse, ((Alternative)el).qFalse.getSize()-1, move);
					}
					else if (((Alternative)el).qTrue.getSize() > 0) {
						el = getLastInSubtree(((Alternative)el).qTrue, ((Alternative)el).qTrue.getSize()-1, move);
					}
				}
				else if (el instanceof Case || el instanceof Parallel) {
					Vector<Subqueue> subqueues = (el instanceof Case) ? ((Case)el).qs : ((Parallel)el).qs;
					for (int i = subqueues.size()-1; el == null && i >= 0; i--) {
						if (subqueues.get(i).getSize() > 0) {
							el = getLastInSubtree(subqueues.get(i), subqueues.get(i).getSize()-1, move);
						}
					}
				}
			}
			return el;
		}
	
	}
	// END KGU#324 2017-05-30

	/**
	 * Returns the number of Elements held
	 * @return number of elements
	 */
	public abstract int getSize();

	public abstract int getIndexOf(Element _ele);
	
	public abstract Element getElement(int _index);
	
	/**
	 * Appends the given _element to the already held elements
	 * @param _element
	 */
	public abstract void addElement(Element _element);

	/**
	 * Inserts the given _element before child no. _where (if 0 <= _where <= this.getSize()).
	 * If _element is another IElementContainer, however, all children of _element will be
	 * inserted before the child _where, instead.
	 * @param _element - an Element to be inserted (or the children of which are to be inserted here)
	 * @param _where - index of the child, which _element (or _element's children) is to inserted before  
	 */
	public abstract void insertElementAt(Element _element, int _where);

	/**
	 * Clears this of all elements (maybe just element references) 
	 */
	public abstract void clear();

	/**
	 * Removes all elements (by default similar to clear()) 
	 */
	public abstract void removeElements();
	
	public abstract void removeElement(Element _element);
	
	public abstract void removeElement(int _index);
	
	/**
	 * Sets or removes the disabled flag from all elements
	 * @param disable - whetjer to set (true) or to remove (false) the disabled flag
	 */
	public abstract void setDisabled(boolean disable);

	/**
	 * Returns the common parenting Subqueue of all the elements contained.
	 * @return a Subqueue (may even be this!)
	 */
	public abstract Subqueue getSubqueue();
	
	// START KGU#324 2017-05-30: Enh. #373, #415
	/**
	 * Provides an iterator for the scope this IElementSequence is establishng.
	 * @param _subtree - specifies whether the iterator is to traverse in deep or shallow mode 
	 * @return the provided iterator
	 */
	public default Iterator iterator(boolean _subtree)
	{
		return new Iterator(this, _subtree);
	}
	// END KGU#324 2017-05-30
	
}

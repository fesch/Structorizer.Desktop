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
*      Kay Gürtzig      2015-11-23      First issue (KGU#87).
*      Kay Gürtzig      2016-10-13      Enh. #277: Method setDisabled(boolean) added
*      Kay Gürtzig      2017-05-30      Enh. #415: Equipped with a tree-capable Iterator class
*      Kay Gürtzig      2019-03-17      Bugfix #705: Substructure of Case and Parallel hadn't been traversed
*      Kay Gürtzig      2019-03-17      Enh. #56: New element class Try integrated
*      Kay Gürtzig      2019-10-12      Bugfix #705/2: Retrieval mistake for CASE, PARALLEL branches in getNext(boolean)
*      Kay Gürtzig      2020-05-02      Javadoc completed on occasion of issue #866
*      Kay Gürtzig      2022-07-30      Result type of removeElement(Element) changed from void to boolean
*
******************************************************************************************************
*
*      Comment:		/
*
******************************************************************************************************///

import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.Vector;

//import lu.fisch.structorizer.gui.SelectedSequence;

/**
 * Interface for linear NSD element sequences (e.g. subsequences of {@link Subqueue}s).
 * Provides typical method signatures for collections and a fully implemented specific
 * NSD tree iterator.
 * @author Kay Gürtzig
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
				throw new NoSuchElementException("Diagram subtree exhausted");
			}
			return next;
		}
		
		public boolean hasPrevious() {
			return this.getPrevious(false) != null;
		}

		public Element previous() {
			Element prev = this.getPrevious(true);
			if (prev == null) {
				throw new NoSuchElementException("Diagram subtree exhausted");
			}
			return prev;
		}
		
		/**
		 * @param move - if true then actually moves forward, otherwise only "peeks".
		 * @return the next element in the iteration or null
		 */
		private Element getNext(boolean move) {
			Element next = null;
			//IElementSequence seq = current;
			int level = positions.size() - 1;
			int at = -1;
			if (level >= 0) at = positions.peek();
			if (at < 0) {
				if (top.getSize() > 0) {
					next = top.getElement(0);
					if (move) {
						current = top;
						positions.push(0);
					}
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
				// START KGU#686 2019-03-17: Enh. #56 New element class Try
				else if (el instanceof Try) {
					if (((Try)el).qTry.getSize() > 0) {
						next = ((Try)el).qTry.getElement(0);
					}
					else if (((Try)el).qCatch.getSize() > 0) {
						next = ((Try)el).qCatch.getElement(0);
					}
					else if (((Try)el).qFinally.getSize() > 0) {
						next = ((Try)el).qFinally.getElement(0);
					}
				}
				// END KGU#686 2019-03-17
			}
			// START KGU#750 2019-10-12: Bugfix #705/2 wrong consecution in case of move = false
			//if (next != null && move) {
			//	current = (Subqueue)next.parent;
			//	positions.push(0);
			//}
			if (next != null) {
				if (move) {
					current = (Subqueue)next.parent;
					positions.push(0);
				}
			}
			// END KGU#750 2019-10-12
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
					// START KGU#688 2019-03-17: Bugfix #705 - Wrong loop condition averted search.
					//for (int i = 0; next != null && i < subqueues.size(); i++) {
					for (int i = 0; next == null && i < subqueues.size(); i++) {
					// END KGU#688 2019-03-17
						if (!found && seq == subqueues.get(i)) {
							found = true;
						}
						else if (found && subqueues.get(i).getSize() > 0) {
							seq = subqueues.get(i);
							// START KGU#750 2019-10-12: Bugfix #705/2
							//next = current.getElement(0);
							next = seq.getElement(0);
							// END KGU#750 2019-10-12
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
				// START KGU#686 2019-03-17: Enh. #56 new element class Try
				else if (el instanceof Try) {
					Subqueue[] subqueues = new Subqueue[] {((Try)el).qTry, ((Try)el).qCatch, ((Try)el).qFinally};
					boolean found = false;	// Current child subqueue identified?
					for (int i = 0; next == null && i < subqueues.length; i++) {
						if (!found && seq == subqueues[i]) {
							found = true;	// Yes, so fetch the next non-empty sister subqueue in the next cycle
						}
						else if (found && subqueues[i].getSize() > 0) {
							seq = subqueues[i];
							// START KGU#750 2019-10-12: Bugfix #705/2
							//next = current.getElement(0);
							next = seq.getElement(0);
							// END KGU#750 2019-10-12
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
				// END KGU#686 2019-03-17
				
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
				// Wrap around if in virgin state
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
					// START KGU#686 2019-03-17: Enh. #56 new element class Try
					else if (el instanceof Try) {
						boolean found = false;
						Subqueue[] subqueues = {((Try)el).qTry, ((Try)el).qCatch, ((Try)el).qFinally};
						for (int i = subqueues.length-1; prev == null && i >= 0; i--) {
							if (seq == subqueues[i]) {
								found = true;
							}
							else if (found && subqueues[i].getSize() > 0) {
								prev = getLastInSubtree(subqueues[i], subqueues[i].getSize()-1, move);
							}
						}
					}
					// END KGU#686 2019-03-17
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
				else if (el instanceof Try) {
					if (((Try)el).qFinally.getSize() > 0) {
						el = getLastInSubtree(((Try)el).qFinally, ((Try)el).qFinally.getSize()-1, move);
					}
					else if (((Try)el).qCatch.getSize() > 0) {
						el = getLastInSubtree(((Try)el).qCatch, ((Try)el).qCatch.getSize()-1, move);
					}
					else if (((Try)el).qTry.getSize() > 0) {
						el = getLastInSubtree(((Try)el).qTry, ((Try)el).qTry.getSize()-1, move);
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

	/**
	 * Tries to find {@link Element} {@code _ele} within this sequence and returns
	 * its position if found.
	 * @param _ele - the {@link Element} to be located.
	 * @return the index, or -1 if not found
	 */
	public abstract int getIndexOf(Element _ele);
	
	/**
	 * Fetches the {@link Element} at position {@code _index} if this is a
	 * valid index. Will cause an exception otherwise.
	 * @param _index - must be in interval [0, {@link #getSize()}-1]
	 * @return the requested {@link Element}
	 * @throws ArrayIndexOutOfBoundsException if the index is out of range
	 */
	public abstract Element getElement(int _index);
	
	/**
	 * Appends the given {@code _element} to the already held {@link Element}s
	 * @param _element - {@link Element} to be added.
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
	
	/**
	 * Removes the given {@link Element} {@code _element} if it was contained
	 * @param _element - the element to be removed
	 * @return {@code true} if the Element was a component of this sequence, {@code false} otherwise
	 */
	public abstract boolean removeElement(Element _element);
	
	/**
	 * Removes the {@link Element} at position {@code _index}
	 * @param _index - index of the {@link Element} to be removed
	 */
	public abstract void removeElement(int _index);
	
	/**
	 * Sets or removes the disabled flag from all elements
	 * @param disable - whether to set (true) or to remove (false) the disabled flag
	 */
	public abstract void setDisabled(boolean disable);

	/**
	 * Returns the common parenting Subqueue of all the elements contained.
	 * @return a Subqueue (may even be this!)
	 */
	public abstract Subqueue getSubqueue();
	
	// START KGU#324 2017-05-30: Enh. #373, #415
	/**
	 * Provides an iterator for the scope this IElementSequence is establishing.
	 * @param _subtree - specifies whether the iterator is to traverse in deep or shallow mode 
	 * @return the provided iterator
	 */
	public default Iterator iterator(boolean _subtree)
	{
		return new Iterator(this, _subtree);
	}
	// END KGU#324 2017-05-30
	
}

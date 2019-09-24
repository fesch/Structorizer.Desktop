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

package lu.fisch.structorizer.locales;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Light-weight helper class for LangDialog, serves as hlder for translated texts
 *                      to be used e.g. as messages but don't require a Swing class like JLabel
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Kay Gürtzig     2016.03.25      First Issue
 *      Kay Gürtzig     2017.09.29      Method toString overridden
 *      Kay Gürtzig     2018.10.08      Moved from package lu.fisch.structorizer.gui to ~.locales,
 *                                      added LangEvent support
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.util.HashSet;

/**
 * A simple String wrapper responding to the messages getText() and setText() in order
 * to serve as light-weight translation holder compatible to the language localisation
 * mechanism of LangDialog. Translations for messages and other texts not permanently
 * visible in the GUI should better be stored in an instance of this class than e,g, in a
 * hidden {@link JLabel}.<br/>
 * Objects interested in text changes of an object of this class may register as listeners
 * to {@link LangEvent}s via {@link #addLangEventListener(LangEventListener)}.
 * @author Kay Gürtzig
 */
public class LangTextHolder
{
	private String text = "";
	// START KGU#596 2018-10-08: listener API added
	/** Set of {@link LangEventListener}s with lazy initialization */
	private HashSet<LangEventListener> listeners = null;
	// START KGU#596 2018-10-08: listener API added
	public LangTextHolder() {}
	public LangTextHolder(String _text)
	{
		this.text = _text;
	}
	
	/**
	 * Replaces the held text. Method fires {@link LangEvent}s notifying all registered
	 * {@link LangEventListeners}.
	 * @param _text - The new string content
	 * @see #addLangEventListener(LangEventListener)
	 * @see #removeLangEventListener(LangEventListener)
	 */
	public void setText(String _text)
	{
		this.text = _text;
		// START KGU#596 2018-10-08: listener API added
		if (listeners != null) {
			LangEvent evt = new LangEvent(this);
			for (LangEventListener lsnr: listeners) {
				lsnr.LangChanged(evt);
			}
		}
		// END KGU#596 2018-10-08
	}
	
	/**
	 * @return the held text string
	 */
	public String getText()
	{
		return this.text;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return this.getClass().getName() + "[text=" + this.text + "]";
	}
	
	// START KGU#596 2018-10-08: New event API on occasion of issue #620
	/**
	 * Adds {@code listener} to the set of {@link LangEvent} listeners.<br/>
	 * An alternative mechanism (only available for classes extending {@link LangDialog},
	 * however) is implementing {@link LangDialog#adjustLangDependentComponents()}.
	 * @param listener - an object interested in {@link LangEvent}s
	 * @see #removeLangEventListener(LangEventListener)
	 * @see LangEventListener#LangChanged(LangEvent)
	 * @see LangDialog#adjustLangDependentComponents()
	 */
	public void addLangEventListener(LangEventListener listener)
	{
		if (listeners == null) {
			listeners = new HashSet<LangEventListener>();
		}
		listeners.add(listener);
	}
	
	/**
	 * Removes {@code listener} from the set of {@link LangEvent} listeners.
	 * @param listener - an object no longer interested in {@link LangEvent}s
	 * @return true if {@code listener} had been registered as {@link LangEventListener}
	 * @see #addLangEventListener(LangEventListener)
	 */
	public boolean removeLangEventListener(LangEventListener listener)
	{
		boolean done = false;
		if (listeners != null) {
			done = listeners.remove(listener);
		}
		return done;
	}
	// END KGU#596 2018-10-08
}

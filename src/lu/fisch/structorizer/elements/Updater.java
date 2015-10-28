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

import java.util.Vector;

/**
 *
 * @author robertfisch
 */
public interface Updater
{
    /**
     * Informs the Updater that something's changed
     * @param source
     */
    public void update(Root source);
    
    // START KGU 2015-10-17: This offers a way to keep track of a Root replacement in an associated form
    /**
     * When a registered Root has been be replaced in its Mainform, here is the method to be called in
     * order to allow the Updater to keep track of the replacement and to register updater with the new Root.
     * @param oldRoot - the Root object recently registered as update source
     * @param newRoot (optional) - the Root object having replaced oldRoot. May be null if Updater is able to retrieve newRoot itself
     */
    public void replaced(Root oldRoot, Root newRoot);
    
    /**
     * Gathers all update sources responding to the name passed in. 
     * @param rootName - a String the Root objects looked for ought to respond to as name
     * @return a collection of Root objects responding to the passed-in name
     */
    public Vector<Root> findSourcesByName(String rootName);
    // END KGU 2015-10-17
}


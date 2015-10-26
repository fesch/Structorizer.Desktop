/*
    Structorizer :: Arranger
    A little tool which you can use to arrange Nassi-Schneiderman Diagrams (NSD)

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

package lu.fisch.structorizer.arranger;

import java.awt.Point;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.gui.Mainform;

/**
 *
 * @author robertfisch
 */
public class Diagram
{
    Root root = null;
    Point point = null;
    Mainform mainform = null;

    public Diagram(Root root, Point point)
    {
        this.root=root;
        this.point=point;
    }
}

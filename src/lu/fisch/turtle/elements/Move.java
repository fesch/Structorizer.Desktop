/*
    Turtlebox

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


package lu.fisch.turtle.elements;

/******************************************************************************************************
 *
 *      Author:         Robert Fisch
 *
 *      Description:    Move - an invisible line in the Turtle graphics window
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2020-12-22      Enh. #890 method getNearestPoint(Point) implemented
 *
 ******************************************************************************************************
 *
 *      Comment: This class is completely redundant
 *
 ******************************************************************************************************///

import java.awt.Graphics2D;
import java.awt.Point;

/**
 *
 * @author robertfisch
 */
public class Move extends Element
{

    public Move(Point from, Point to)
    {
        super(from,to);
    }

    @Override
    public void draw(Graphics2D graphics)
    {
    }

    // START KGU#889 2020-12-22: Enh. #890/9 (measuring with snap)
    @Override
    public Point getNearestPoint(Point pt)
    {
        if (from.distance(pt) > to.distance(pt)) {
            return to;
        }
        return from;
    }
    // END KGU#889 2020-12-22

}

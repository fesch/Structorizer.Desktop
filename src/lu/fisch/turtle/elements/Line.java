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
 *      Description:    Line - a visible line in the Turtle graphics window
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2020-12-11      Enh. #704 API extension: draw(Graphics2D, Rectangle), getBounds()
 *                                      appendSpecificCSVInfo(StringBuilder, String)
 *      Kay Gürtzig     2020-12-22      Enh. #890 method getNearestPoint(Point, boolean) implemented
 *
 ******************************************************************************************************
 *
 *      Comment:
 *
 ******************************************************************************************************///

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

/**
 *
 * @author robertfisch
 */
public class Line extends Element
{

    public Line(Point from, Point to)
    {
        super(from,to);
    }

    public Line(Point from, Point to, Color color)
    {
        super(from,to,color);
    }

    @Override
    public void draw(Graphics2D graphics)
    {
        graphics.setColor(color);
        graphics.drawLine(from.x, from.y, to.x, to.y);
    }

    // START KGU#685 2020-12-11: Enh. #704
    protected void appendSpecificCSVInfo(StringBuilder sb, String separator)
    {
        sb.append(separator);
        sb.append(Integer.toHexString(color.getRGB()));
    }

    /**
     * @return the bounding box of this line, ensuring that no dimensions is 0
     */
    public Rectangle getBounds()
    {
        Rectangle bounds = new Rectangle(from);
        bounds.add(to);
        // We must avoid "empty" rectangles for intersection tests
        if (bounds.height == 0) {
            bounds.height = 1;
        }
        if (bounds.width == 0) {
            bounds.width = 1;
        }
        return bounds;
    }
    // END KGU#685 2020-12-11
    
    // START KGU#889 2020-12-22: Enh. #890/9 (measuring with snapping)
    @Override
    public Point getNearestPoint(Point pt, boolean inter)
    {
        if (inter) {
            // We abuse a point for the direction vector
            Point dvec = new Point(to.x - from.x, to.y - from.y);
            Point pvec = new Point(pt.x - from.x, pt.y - from.y);
            double dlen2 = (dvec.x * dvec.x + dvec.y * dvec.y);
            double param = (pvec.x * dvec.x + pvec.y * dvec.y) / dlen2;
            if (param < 0) {
                return from;
            }
            else if (param * param > dlen2) {
                return to;
            }
            Point nearest = new Point(from);
            nearest.translate((int)Math.round(param * dvec.x), (int)Math.round(param * dvec.y));
            return nearest;
        }
        else if (from.distance(pt) > to.distance(pt)) {
            return to;
        }
        return from;
    }
    // END KGU#889 2020-12-22

}

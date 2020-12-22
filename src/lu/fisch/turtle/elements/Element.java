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
 *      Description:    Move - an invisible move in the Turtle graphics window
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2020-12-11      Enh. #704 API extension: toString(),
 *                                      appendSpecificCSVInfo(StringBuilder, String)
 *      Kay Gürtzig     2020-12-13      Enh. #704 API extension: getFrom(), getTo(), getColor(),
 *      Kay Gürtzig     2020-12-22      Enh. #890 abstract method getNearestPoint(Point) added
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
 * Abstract base class for atomic parts of a TurtleBox drawing
 * @author robertfisch
 */
public abstract class Element
{
    Point from;
    Point to;
    Color color = Color.BLACK;

    public Element(Point from, Point to)
    {
        this.from = from;
        this.to = to;
    }

    public Element(Point from, Point to, Color color)
    {
        this.from = from;
        this.to = to;
        this.color = color;
    }

    /**
     * Draws this Element in the given 2D drawing environment {@code graphics}
     * @param graphics - the 2D drawing environment
     * @see #draw(Graphics2D, Rectangle)
     */
    public abstract void draw(Graphics2D graphics);

    // START KGU#685 2020-12-11: Enh. #704
    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + "(" + this.from + "," + this.to + "," + this.color + ")";
    }
    
    /**
     * Returns an appropriate string for a CSV row, using the given {@code separator}
     * @param separator - character sequence to separate the columns (usually with
     * length 1), internal default is {@code ","};
     * @return the prepared string, comprising the coordinates and a subclass-specific
     * addendum produced by {@link #appendSpecificCSVInfo(StringBuilder, String)}
     */
    public String toCSV(String separator)
    {
        StringBuilder sb = new StringBuilder();
        if (separator == null || separator.isEmpty()) {
            separator = ",";
        }
        sb.append(from.x);
        sb.append(separator);
        sb.append(from.y);
        sb.append(separator);
        sb.append(to.x);
        sb.append(separator);
        sb.append(to.y);
        appendSpecificCSVInfo(sb, separator);
        return sb.toString();
    }
    /**
     * Appends subclass-specific columns to the CSV content given by {@code sb}.
     * @param sb - String builder composing the CSV information
     * @param separator - column separator to be used
     */
    protected void appendSpecificCSVInfo(StringBuilder sb, String separator)
    {
    }
    // END KGU#685 2020-12-11
    
    // START KGU#685 2020-12-13: Enh. #704
    /** @return the start point of this element */
    public Point getFrom()
    {
        return from;
    }
    
    /** @return the end point of this element */
    public Point getTo()
    {
        return to;
    }
    
    /** @return the end color of this element */
    public Color getColor()
    {
        return color;
    }
    // END KGU#685 2020-12-13
    
    // START KGU#889 2020-12-22: Enh. #890/9 (measuring with snap)
    /**
     * Return the point of this element thatis closest to the given point
     * {@code pt}
     * @param pt - the interesting point
     * @return the point nearest to {@code pt} on this shape
     */
    public abstract Point getNearestPoint(Point pt);
    // END KGU#889 2020-12-22

}

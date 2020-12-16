/*
    Turtlebox / Structorizer

    Copyright (C) 2017  Bob Fisch

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
package lu.fisch.turtle.adapters;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Turtleizer: Adapter class for TurtleBox to be used by Java applications in the
 *                      same way as Turtleizer routines may be used within Structorizer
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.10.28      First issue for enhancement request #441 (possibility to use
 *                                      Turtleizer functionality by Java code e.g. exported from Structorizer)
 *      Kay Gürtzig     2018.01.21      Enh. #441, #443: Retrieval methods for API put deprecated
 *      Kay Gürtzig     2018.07.30      Enh. #576: New procedure clear() added to the API
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      This adapter class works with static methods and is a kind of proxy for {@link TurtleBox}.
 *      The API for employing applications is retrievable via {@link TurtleBox#getFunctionMap()} and
 *      {@link TurtleBox#getProcedureMap}.
 *
 ******************************************************************************************************///

import java.awt.Color;

import java.util.HashMap;

import lu.fisch.turtle.TurtleBox;

/**
 * Adapter class for Structorizer diagrams exported to Java code.<br/>
 * Turtleizer routines like {@code forward(100)} or {@code getX()} may easily be
 * used by calling them as static methods on this class:<br/>
 * {@code Turtleizer.forward(100);}<br/>
 * {@code int x = Turtleizer.getX();}<br/>
 * (The Structorizer Java export should already produce the code in this form.)
 * @author Kay Gürtzig
 * @version 3.27-05
 */
public class Turtleizer {
	
	private static TurtleBox turtleBox = null;
	/**
	 * Maps different Turtleizer function names (in lower-case) to the respective adapter
	 * method names, which are not of course case-ignorant. This map is needed for 
	 */
	// FIXME: This field became superfluous since TurtleBox got a light-weight constructor.
	@Deprecated
	@SuppressWarnings("serial")
	private static final HashMap<String, String> supportedRoutines = new HashMap<String, String>() {{
		put("forward", "forward");
		put("backward", "backward");
		put("fd", "fd");
		put("bk", "bk");
		put("left", "left");
		put("right", "right");
		put("rl", "left");
		put("rr", "right");
		put("penup", "penUp");
		put("pendown", "penDown");
		put("up", "penUp");
		put("down", "penDown");
		put("gotoxy", "gotoXY");
		put("gotox", "gotoX");
		put("gotoy", "gotoY");
		put("hideturtle", "hideTurtle");
		put("showturtle", "showTurtle");
		// START KGU#566 2018-07-20: Enh. #576
		put("clear", "clear");
		// END KGU#566 2018-07-20
		put("getorientation", "getOrientation");
		put("getx", "getX");
		put("gety", "getY");
	}};
	
	/**
	 * Returns a (heavy-weight) as-if singleton instance of class {@link TurtleBox}
	 * (creates it if it hadn't been there).<br/>
	 * Note: For mere API retrieval use a light-weight instance to be obtained via
	 * {@link TurtleBox#TurtleBox()}.
	 */
	private static TurtleBox getTurtleBox()
	{
		if (turtleBox == null) {
			turtleBox = new TurtleBox(500, 500);
			turtleBox.setVisible(true);
			turtleBox.setAnimationDelay(0, true);
		}
		return turtleBox;
	}
	
	/**
	 * Checks whether a routine with the {@code candidateName} is supported by Turtleizer, and if so
	 * returns the qualified method name, otherwise null. Note that {@code candidateName} will be
	 * looked up case-ignorantly.<br/>
	 * Became obsolete with Structorizer version 3.27-05, better use {@link TurtleBox#providedRoutine(String, int)}
	 * on a light-weight instance of {@link TurtleBox} (obtainable from the standard constructor,
	 * NOT via {@link #getTurtleBox()}).
	 * @param candidateName - a procedure or function identifier 
	 * @return the qualified method name or null
	 * @see TurtleBox#providedRoutine(String, int)
	 */
	@Deprecated
	public static String checkRoutine(String candidateName)
	{
		String methodName = supportedRoutines.get(candidateName.trim().toLowerCase());
		if (methodName != null) {
			methodName = "Turtleizer." + methodName;
		}
		return methodName;
	}
	
	// Drawing
	/**
	 * Moves the turtle forwards by exactly {@code pixels} within a virtual floating-point
	 * coordinate system. If pen is down, draws a line in the current pen colour.
	 * @param pixels - exact length of the translation (in current orientation).
	 * @see #backward(double)
	 * @see #forward(double, Color)
	 * @see #fd(int)
	 * @see #penDown()
	 * @see #penUp()
	 * @see #setPenColor(short, short, short)
	 */
	public static void forward(double pixels)
	{
		getTurtleBox().forward(pixels);;
	}
	/**
	 * Moves the turtle forwards by exactly {@code pixels} within a virtual floating-point
	 * coordinate system. If pen is down, draws a line in the given {@code color}.
	 * @param pixels - exact length of the translation (in current orientation).
	 * @param color - the pen colour to be used.
	 * @see #backward(double, Color)
	 * @see #forward(double)
	 * @see #fd(int, Color)
	 * @see #penDown()
	 * @see #penUp()
	 */
	public static void forward(double pixels, Color color)
	{
		getTurtleBox().forward(pixels, color);
	}
	/**
	 * Moves the turtle backwards by exactly {@code pixels} within a virtual floating-point
	 * coordinate system. If pen is down, draws a line in the current pen colour.
	 * @param pixels - exact length of the translation (in current orientation).
	 * @see #forward(double)
	 * @see #backward(double, Color)
	 * @see #bk(int)
	 * @see #penDown()
	 * @see #penUp()
	 * @see #setPenColor(short, short, short)
	 */
	public static void backward(double pixels)
	{
		getTurtleBox().backward(pixels);
	}
	/**
	 * Moves the turtle backwards by exactly {@code pixels} within a virtual floating-point
	 * coordinate system. If pen is down, draws a line in the given {@code color}.
	 * @param pixels - exact length of the translation (in current orientation).
	 * @param color - the pen colour to be used.
	 * @see #forward(double, Color)
	 * @see #backward(double)
	 * @see #bk(int, Color)
	 * @see #penDown()
	 * @see #penUp()
	 */
	public static void backward(double pixels, Color color)
	{
		getTurtleBox().backward(pixels, color);
	}
	/**
	 * Moves the turtle forwards by approximately {@code pixels} and coerces its position
	 * to the nearest pixel coordinate in an integral coordinate system. If pen is down,
	 * draws a line from the recent coordinate to the target coordinate in the current
	 * pen colour.
	 * @param pixels - integral length of the translation (in current orientation).
	 * @see #forward(double)
	 * @see #fd(int, Color)
	 * @see #bk(int)
	 * @see #penDown()
	 * @see #penUp()
	 * @see #setPenColor(short, short, short)
	 */
	public static void fd(int pixels)
	{
		getTurtleBox().fd(pixels);
	}
	/**
	 * Moves the turtle forwards by approximately {@code pixels} and coerces its position
	 * to the nearest pixel coordinate in an integral coordinate system. If pen is down,
	 * draws a line from the recent coordinate to the target coordinate in the given
	 * pen colour.
	 * @param pixels - integral length of the translation (in current orientation).
	 * @see #forward(double, Color)
	 * @see #fd(int)
	 * @see #bk(int, Color)
	 * @see #penDown()
	 * @see #penUp()
	 */
	public static void fd(int pixels, Color color)
	{
		getTurtleBox().fd(pixels, color);
	}
	/**
	 * Moves the turtle backwards by approximately {@code pixels} and coerces its position
	 * to the nearest pixel coordinate in an integral coordinate system. If pen is down,
	 * draws a line from the recent coordinate to the target coordinate in the current
	 * pen colour.
	 * @param pixels - integral length of the translation (in current orientation).
	 * @see #backward(double)
	 * @see #bk(int, Color)
	 * @see #fd(int)
	 * @see #penDown()
	 * @see #penUp()
	 * @see #setPenColor(short, short, short)
	 */
	public static void bk(int pixels)
	{
		getTurtleBox().bk(pixels);
	}
	/**
	 * Moves the turtle backwards by approximately {@code pixels} and coerces its position
	 * to the nearest pixel coordinate in an integral coordinate system. If pen is down,
	 * draws a line from the recent coordinate to the target coordinate in the given
	 * pen colour.
	 * @param pixels - integral length of the translation (in current orientation).
	 * @see #backward(double, Color)
	 * @see #bk(int)
	 * @see #fd(int, Color)
	 * @see #penDown()
	 * @see #penUp()
	 */
	public static void bk(int pixels, Color color)
	{
		getTurtleBox().bk(pixels, color);
	}
	
	// Rotation
	public static void right(double degrees)
	{
		getTurtleBox().right(degrees);
	}
	public static void left(double degrees)
	{
		getTurtleBox().left(degrees);
	}
	
	// Pen
	/** Raises the pen such that further moves won't draw lines */
	public static void penUp()
	{
		getTurtleBox().penUp();
	}
	/** Lowers the pen such that further moves will draw lines */	
	public static void penDown()
	{
		getTurtleBox().penDown();
	}
	
	// Positioning
	/** Places the turtle to the nearest integral coordinate (x,y) without drawing. */
    public static void gotoXY(double x, double y)
    {
    	getTurtleBox().gotoXY((int) x, (int) y);
    }
	/** Places the turtle to the integral pixel position (x,y) where y is the current column. */
    public static void gotoX(double x)
    {
    	getTurtleBox().gotoX((int) x);
    }
	/** Places the turtle to the integral pixel position (x,y) where x is the current roe. */
    public static void gotoY(double y)
    {
    	getTurtleBox().gotoY((int) y);
    }
    
    // Hiding
    /** Makes the turtle invisible (this does not prevent it from drawing lines) */
    public static void hideTurtle()
    {
    	getTurtleBox().hideTurtle();
    }
    /** Makes the turtle visible (this does not put the pen down if it's up!) */
    public static void showTurtle()
    {
    	getTurtleBox().showTurtle();
    }

    // Changing Color
    /**
     * Sets the background colour of the canvas according to the given RGB values.<br/>
     * Legacy method for Java exports prior to Structorizer version 3.27-05
     * @see #setBackgroundColor(short, short, short)
     */ 
    public static void setBackground(short red, short green, short blue)
    {
    	getTurtleBox().setBackgroundColor(Math.abs(red), Math.abs(green), Math.abs(blue));
    }
    /** Sets the background colour of the canvas according to the given RGB values. */
    public static void setBackgroundColor(short red, short green, short blue)
    {
    	getTurtleBox().setBackgroundColor(Math.abs(red), Math.abs(green), Math.abs(blue));
    }
    /** Sets the pen colour of the turtle according to the given RGB values. */
    public static void setPenColor(short red, short green, short blue)
    {
    	getTurtleBox().setPenColor(Math.abs(red), Math.abs(green), Math.abs(blue));
    }
    
    // Functions
    /** Returns the current turtle orientation in degrees from North */
    public static double getOrientation() {
    	return (Double)getTurtleBox().getOrientation();
    }
    /** Returns the current virtual x coordinate of the turtle (as double) */
    public static double getX() {
    	return getTurtleBox().getX();
    }
    /** Returns the current virtual y coordinate of the turtle (as double) */
    public static double getY() {
    	return getTurtleBox().getY();
    }
    // START KGU#566 2018-07-30: Enh. API function allowing the user algorithm to wipe the box
    /** Wipes the Turle canvas from all content */
    public static void clear()
    {
    	getTurtleBox().clear();
    }
    // END KGU#566 2018-07-30

}

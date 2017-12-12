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

import java.awt.Color;

import java.util.HashMap;

import lu.fisch.turtle.TurtleBox;

/**
 * Adapter class for Structorizer diagrams exported to Java code
 * @author Kay Gürtzig
 * @version 3.27
 */
public class Turtleizer {
	
	private static TurtleBox turtleBox = null;
	/** maps different Turtleizer function names in lower-case to the respective adapter method names */
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
		put("getorientation", "getOrientation");
		put("getx", "getX");
		put("gety", "getY");
	}};
	
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
	 * looked up case-ignorantly.
	 * @param candidateName - a procedure or function identifier 
	 * @return the qualified method name or null
	 */
	public static String checkRoutine(String candidateName)
	{
		String methodName = supportedRoutines.get(candidateName.trim().toLowerCase());
		if (methodName != null) {
			methodName = "Turtleizer." + methodName;
		}
		return methodName;
	}
	
	// Drawing
	public static void forward(double pixels)
	{
		getTurtleBox().forward(pixels);;
	}
	public static void forward(double pixels, Color color)
	{
		getTurtleBox().forward(pixels, color);
	}
	public static void backward(double pixels)
	{
		getTurtleBox().backward(pixels);
	}
	public static void backward(double pixels, Color color)
	{
		getTurtleBox().backward(pixels, color);
	}
	public static void fd(int pixels)
	{
		getTurtleBox().fd(pixels);
	}
	public static void fd(int pixels, Color color)
	{
		getTurtleBox().fd(pixels, color);
	}
	public static void bk(int pixels)
	{
		getTurtleBox().bk(pixels);
	}
	public static void bk(int pixels, Color color)
	{
		getTurtleBox().bk(pixels, color);
	}
	
	// Rotation
	public static void right(double degrees)
	{
		getTurtleBox().rl(-degrees);
	}
	public static void left(double degrees)
	{
		getTurtleBox().rl(degrees);
	}
	
	// Pen
	public static void penUp()
	{
		getTurtleBox().penUp();
	}
	public static void penDown()
	{
		getTurtleBox().penDown();
	}
	
	// Positioning
    public static void gotoXY(double x, double y)
    {
    	getTurtleBox().gotoXY((int) x, (int) y);
    }
    public static void gotoX(double x)
    {
    	getTurtleBox().gotoX((int) x);
    }
    public static void gotoY(double y)
    {
    	getTurtleBox().gotoY((int) y);
    }
    
    // Hiding
    public static void hideTurtle()
    {
    	getTurtleBox().hideTurtle();
    }
    public static void showTurtle()
    {
    	getTurtleBox().showTurtle();
    }

    // Changing Color
    public static void setBackground(short red, short green, short blue)
    {
    	getTurtleBox().setBackgroundColor(Math.abs(red), Math.abs(green), Math.abs(blue));
    }
    public static void setPenColor(short red, short green, short blue)
    {
    	getTurtleBox().setPenColor(Math.abs(red), Math.abs(green), Math.abs(blue));
    }
    
    // Functions
    public static double getOrientation() {
    	return (Double)getTurtleBox().getOrientation();
    }
    public static double getX() {
    	return getTurtleBox().getx();
    }
    public static double getY() {
    	return getTurtleBox().gety();
    }
}

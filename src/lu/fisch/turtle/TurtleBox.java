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

package lu.fisch.turtle;

/******************************************************************************************************
 *
 *      Author:         Robert Fisch
 *
 *      Description:    TurtleBox - a Turtle graphics window with an interface usable e.g. by Structorizer
 *                      but also (with an appropriate adapter) by arbitrary Java applications
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2015.12.10      Inheritance change for enhancement request #48
 *      Kay Gürtzig     2016.10.16      Enh. #272: exact internal position (double coordinates)
 *      Kay Gürtzig     2016.12.02      Enh. #302 Additional methods to set the pen and background color
 *      Kay Gürtzig     2017.06.29/30   Enh. #424: Inheritance extension to FunctionProvidingDiagramControl
 *                                      function map introduced, functions getX(), getY(), getOrientation() added
 *      Kay Gürtzig     2017.10.28      Enh. #443: interface FunctionProvidingDiagramControl now integrated,
 *                                      structure of function map modified, procedure map added, execution
 *                                      mechanism fundamentally revised
 *                                      Concurrency issue fixed (KGU#449).
 *      Kay Gürtzig     2018.01.16      Enh. #490: Class decomposed to allow a mere API use without realising the GUI 
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      To start the Turtleizer in an application, the following steps are recommended:
 *      	{@code TurtleBox turtleBox = new TurtleBox(<width>, <height>);}
 *			{@code turtleBox.setVisible(true);}
 *			{@code turtleBox.setAnimationDelay(0, true);}
 *      The API for employing applications is retrievable via {@link TurtleBox#getFunctionMap()} and
 *      {@link TurtleBox#getProcedureMap}.
 *
 ******************************************************************************************************///

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import lu.fisch.diagrcontrol.*;
import lu.fisch.turtle.elements.Element;
import lu.fisch.turtle.elements.Line;
import lu.fisch.turtle.elements.Move;

/**
 * TurtleBox - a Turtle controller providing an interface usable e.g. by Structorizer
 * but also (with an appropriate adapter) by arbitrary Java applications<br/>.
 * On parameterized initialization or call of any of the offered routines a graphics
 * window with a Turtle is realized.
 * To start the Turtleizer in an application, the following steps are recommended:<br/>
 * {@code TurtleBox turtleBox = new TurtleBox(<width>, <height>);}<br/>
 * {@code turtleBox.setVisible(true);}<br/>
 * {@code turtleBox.setAnimationDelay(0, true);}<br/>
 * The API for employing applications is retrievable via {@link TurtleBox#getFunctionMap()}
 * and {@link TurtleBox#getProcedureMap()}.<br/>
 * In order just to retrieve the available API without bringing up the GUI a light-weight
 * instance is obtained by
 * {@code TurtleBox turtleBox = new TurtleBox();}
 * @author robertfisch
 * @author Kay Gürtzig
 */
// START KGU#97 2015-12-10: Inheritance change for enhancement request #48
//public class TurtleBox extends JFrame implements DiagramController
@SuppressWarnings("serial")
// START KGU#480 2018-01-16: Inheritance change for enh. #490
//public class TurtleBox extends JFrame implements DelayableDiagramController
public class TurtleBox implements DelayableDiagramController
// END KGU#480 2018-01-16
// END KGU#97 2015-12-10
{
//	// START KGU#417 2017-06-29: Enh. #424 Function capability map
//	private static final HashMap<String, Class<?>[]> definedFunctions = new HashMap<String, Class<?>[]>();
//	static {
//		definedFunctions.put("getx", new Class<?>[]{Double.class});
//		definedFunctions.put("gety", new Class<?>[]{Double.class});
//		definedFunctions.put("getorientation", new Class<?>[]{Double.class});
//	}
//	// END KGU#417 2017-06-29
	// START KGU#417/KGU#448 2017-10-28: Enh. #424, #443 Function capability map
	private static final HashMap<String, Method> definedProcedures = new HashMap<String, Method>();
	static {
		try {
			definedProcedures.put("forward#1", TurtleBox.class.getMethod("forward", new Class<?>[]{Double.class}));
			definedProcedures.put("forward#2", TurtleBox.class.getMethod("forward", new Class<?>[]{Double.class, Color.class}));
			definedProcedures.put("backward#1", TurtleBox.class.getMethod("backward", new Class<?>[]{Double.class}));
			definedProcedures.put("backward#2", TurtleBox.class.getMethod("backward", new Class<?>[]{Double.class, Color.class}));
			definedProcedures.put("fd#1", TurtleBox.class.getMethod("fd", new Class<?>[]{Integer.class}));
			definedProcedures.put("fd#2", TurtleBox.class.getMethod("fd", new Class<?>[]{Integer.class, Color.class}));
			definedProcedures.put("bk#1", TurtleBox.class.getMethod("bk", new Class<?>[]{Integer.class}));
			definedProcedures.put("bk#2", TurtleBox.class.getMethod("bk", new Class<?>[]{Integer.class, Color.class}));
			definedProcedures.put("left#1", TurtleBox.class.getMethod("left", new Class<?>[]{Double.class}));
			definedProcedures.put("right#1", TurtleBox.class.getMethod("right", new Class<?>[]{Double.class}));
			definedProcedures.put("rl#1", TurtleBox.class.getMethod("left", new Class<?>[]{Double.class}));
			definedProcedures.put("rr#1", TurtleBox.class.getMethod("right", new Class<?>[]{Double.class}));
			definedProcedures.put("gotoxy#2", TurtleBox.class.getMethod("gotoXY", new Class<?>[]{Integer.class, Integer.class}));
			definedProcedures.put("gotox#1", TurtleBox.class.getMethod("gotoX", new Class<?>[]{Integer.class}));
			definedProcedures.put("gotoy#1", TurtleBox.class.getMethod("gotoY", new Class<?>[]{Integer.class}));
			definedProcedures.put("penup#0", TurtleBox.class.getMethod("penUp", (Class<?>[])null));
			definedProcedures.put("pendown#0", TurtleBox.class.getMethod("penDown", (Class<?>[])null));
			definedProcedures.put("up#0", TurtleBox.class.getMethod("penUp", (Class<?>[])null));
			definedProcedures.put("down#0", TurtleBox.class.getMethod("penDown", (Class<?>[])null));
			definedProcedures.put("hideturtle#0", TurtleBox.class.getMethod("hideTurtle", (Class<?>[])null));
			definedProcedures.put("showturtle#0", TurtleBox.class.getMethod("showTurtle", (Class<?>[])null));
			definedProcedures.put("setpencolor#3", TurtleBox.class.getMethod("setPenColor", new Class<?>[]{Integer.class, Integer.class, Integer.class}));
			definedProcedures.put("setbackground#3", TurtleBox.class.getMethod("setBackgroundColor", new Class<?>[]{Integer.class, Integer.class, Integer.class}));
		} catch (NoSuchMethodException | SecurityException ex) {
			ex.printStackTrace();
		}
	}
	private static final HashMap<String, Method> definedFunctions = new HashMap<String, Method>();
	static {
		try {
			definedFunctions.put("getx#0", TurtleBox.class.getMethod("getX", (Class<?>[])null));
			definedFunctions.put("gety#0", TurtleBox.class.getMethod("getY", (Class<?>[])null));
			definedFunctions.put("getorientation#0", TurtleBox.class.getMethod("getOrientation", (Class<?>[])null));
		} catch (NoSuchMethodException | SecurityException ex) {
			ex.printStackTrace();
		}
	}
	// END KGU#417/KGU#448 2017-10-28
	// START KGU#480 2018-01-16: Enh. #490 - frame now as attribute
	/** The GUI frame - while null, it hasn't been materialized (light-weight instance) */
	private JFrame frame = null;
	// END KGU#480 2018-01-16
    private final String TITLE = "Turtleizer";

    private Point pos;
    // START KGU#282 2016-10-16: Enh. #272
    private double posX, posY;		// exact position
    // END KGU#282 2016-10-16
    private Point home;
    private double angle = -90;		// upwards (north-bound)
    private Image image = (new ImageIcon(this.getClass().getResource("turtle.png"))).getImage();
    private boolean isPenDown = true;
    // START KGU#303 2016-12-02: Enh. #302
    //private Color penColor = Color.BLACK;
    private Color defaultPenColor = Color.BLACK;
    private Color backgroundColor = Color.WHITE;
    private Color penColor = defaultPenColor;
    // END KGU#303 2016-12-02
    private boolean turtleHidden = false;
    private int delay = 10;
    private Vector<Element> elements = new Vector<Element>();
    private JPanel panel; 

    /**
     * This constructor does NOT realize a GUI, it just creates a light-weight instance
     * for API retrieval. A call of {@link #setVisible(boolean)} or the first use of an
     * API routine from {@link #definedProcedures} or {@link #definedFunctions} will establish
     * the graphics window, though.
     */
    public TurtleBox()
    {
    	// START KGU#480 2018-01-16: Enh. #490 - This constructor no longer builds the GUI
        //init(300,300);
    	home = new Point();
    	reinit();
        // END KGU#480 2018-01-16
    }

    /**
     * Establishes a window frame with the graphics canvas for the turtle movements.
     * The turtle will be placed in the centre of he canvas.
     * @param width - the initial width of the frame in pixels
     * @param height - the initial height of the frame in pixels.
     */
    public TurtleBox(int width, int height)
    {
        init(width,height);
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {}
    
    /* (non-Javadoc)
     * @see java.awt.Component#getName()
     */
    @Override
    public String getName()
    {
    	return TITLE;
    }
    
    /**
     * Returns the contrary of the internal orientation of the turtle in degrees.
     * @return degrees (90° = North, positive sense = clockwise)
     * @see #getOrientation()
     */
    public double getAngle()
    {
        return 180+angle;
    }
    
    // START KGU#417 2017-06-29: Enh. #424
    /**
     * API function returning the "external" turtle orientation in degrees
     * in the range -180 .. 180 where<br/>
     * 0 is upwards/North (initial orientation),<br/>
     * positive sense is clockwise (right/East),
     * negative sense is counter-clockwise (left/West)
     * @return orientation in degrees.
     * @see #getAngle()
     */
    public double getOrientation() {
    	double orient = angle + 90.0;
    	while (orient > 180) { orient -= 360; }
    	while (orient < -180) { orient += 360; }
    	return -orient;
    }
    // END KGU#417 2017-06-29
    
    // START #272 2016-10-16 (KGU)
    private void setPos(Point newPos)
    {
    	pos = newPos;
    	posX = pos.getX();
    	posY = pos.getY();
    }
    
    private void setPos(double x, double y)
    {
    	posX = x;
    	posY = y;
    	pos = new Point((int)Math.round(x), (int)Math.round(y));
    }
    // END #272 2016-10-16

    /**
     * Initialises this instance establishing the window with the graphics canvas
     * and places the Turtle in the centre.
     * @param width - initial width of the frame in pixels
     * @param height - initial height of the frame in pixels
     */
    private void init(int width, int height)
    {
    	// START KGU#480 2018-01-16: Enh. #490 - care for the existence of a frame
    	if (frame == null) {
    		frame = new JFrame();
    	}
    	// END KGU#480 2018-01-16
    	panel = new JPanel()
        {
            @Override
            public void paint(Graphics graphics)
            {
                Graphics2D g = (Graphics2D) graphics;
                // set anti-aliasing rendering
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

                // clear background
                // START KGU#303 2016-12-02: Enh. #302
                //g.setColor(Color.WHITE);
                g.setColor(backgroundColor);
                // END KGU#303 2016-12-02
                g.fillRect(0,0,getWidth(),getHeight());
                // START KGU#303 2016-12-03: Enh. #302
                //g.setColor(Color.BLACK);
                g.setColor(defaultPenColor);
                // END KGU#303 2016-12-03

                // draw all elements
                // START KGU#449 2017-10-28: The use of iterators may lead to lots of
                //java.util.ConcurrentModificationException errors slowing down all.
                // So we better avoid the iterator and loop against a snapshot size
                // (which is safe because the elements Vector can't shrink during execution).
                //for (Element ele : elements)
                //{
                //    ele.draw(g);
                //}
                int nElements = elements.size();
                for (int i = 0; i < nElements; i++) {
                	elements.get(i).draw(g);
                }
                // END KGU#449 2017-10-28

                if (!turtleHidden)
                {
                	// START #272 2016-10-16
                    // fix drawing point
                    //int x = (int) Math.round(pos.x - (image.getWidth(this)/2));
                    //int y = (int) Math.round(pos.y - (image.getHeight(this)/2));
                    // fix drawing point
                    //int xRot = x+image.getWidth(this)/2;
                    //int yRot = y+image.getHeight(this)/2;
                    // apply rotation
                    //g.rotate((270-angle)/180*Math.PI,xRot,yRot);
                    // fix drawing point
                    double x = posX - (image.getWidth(this)/2);
                    double y = posY - (image.getHeight(this)/2);
                    // apply rotation
                    g.rotate((270-angle)/180*Math.PI, posX, posY);
                    // END #272 2016-10-16
                    // draw the turtle
                    g.drawImage(image,(int)Math.round(x),(int)Math.round(y),this);
                }
            }
        };

        frame.setTitle(TITLE);
        frame.setIconImage((new ImageIcon(this.getClass().getResource("turtle.png"))).getImage());

        //this.setDefaultCloseOperation(TurtleBox.EXIT_ON_CLOSE);
        //this.setDefaultCloseOperation(TurtleBox.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setBounds(0,0,width,height);
        frame.getContentPane().add(panel);
        //this.setVisible(true);
        setPos(new Point(panel.getWidth()/2,panel.getHeight()/2));
        home = new Point(panel.getWidth()/2,panel.getHeight()/2);
        panel.setDoubleBuffered(true);
        panel.repaint();
    }

    //@Override
    /**
     * Realizes the window on the screen , brings it to front and fetches the window focus.
     * @param visible
     * @see JFrame#setVisible(boolean)
     */
    public void setVisible(boolean visible)
    {
    	// START KGU#480 2018-01-16: Enh. #490 - lazy initialization
    	if (visible && frame == null) {
    		init(300, 300);
    	}
    	// END KGU#480 2018-01-16
        frame.setVisible(visible);
// START KGU#303 2016-12-03: Issue #302 - replaced by reinit() call below
//        elements.clear();
//        angle=-90;
//        // START KGU#303 2016-12-02: Enh. #302
//        backgroundColor = Color.WHITE;
//        defaultPenColor = Color.BLACK;
//        turtleHidden = false;
//        // END KGU#3032016-12-02
//        setPos(new Point(panel.getWidth()/2,panel.getHeight()/2));
// END KGU#303 2016-12-03
        if (visible) {
        	home = new Point(panel.getWidth()/2,panel.getHeight()/2);
        	// START KGU#303 2016-12-03: Issue #302 - replaces disabled code above
        	reinit();
        	// END KGU#303 2016-12-03
        	frame.paint(frame.getGraphics());
        }
    }
    
    // START KGU#303 2016-12-03: Issue #302
    /**
     * Undoes all possible impacts of a previous main diagram execution  
     */
    private void reinit()
    {
        elements.clear();
        angle = -90;
        backgroundColor = Color.WHITE;
        defaultPenColor = Color.BLACK;
        turtleHidden = false;
        setPos(home.getLocation());
        penDown();
    }
    // END KGU#303 2016-12-03
    
    private void delay()
    {
        //panel.repaint();
        // force repaint (not recommended!)
    	// START KGU#480 2018-01-16: Enh. #490 - lazy initialization
    	if (frame == null) {
    		init(300, 300);
    	}
    	// END KGU#480 2018-01-16
        if (delay!=0)
        {
            panel.paint(panel.getGraphics());
            try { Thread.sleep(delay); }
            catch (InterruptedException e) { System.out.println(e.getMessage());}
        }
        else
        {
            panel.repaint();
        }
    }

    public void fd(Integer pixels)
    {
        Point newPos = new Point(pos.x-(int) Math.round(Math.cos(angle/180*Math.PI)*pixels),
                                 pos.y+(int) Math.round(Math.sin(angle/180*Math.PI)*pixels));
        if (isPenDown)
        {
            elements.add(new Line(pos,newPos,penColor));
        }
        else
        {
            elements.add(new Move(pos,newPos));
        }
        //System.out.println("from: ("+pos.x+","+pos.y+") => to: ("+newPos.x+","+newPos.y+")");
        setPos(newPos);
        delay();
    }

    // START #272 2016-10-16 (KGU)
//    public void forward(int pixels)
//    {
//        fd(pixels);
//    }
    public void forward(Double pixels)
    {
    	double newX = posX - Math.cos(angle/180*Math.PI) * pixels;
    	double newY = posY + Math.sin(angle/180*Math.PI) * pixels;
        Point newPos = new Point((int)Math.round(newX), (int)Math.round(newY));
        if (isPenDown)
        {
            elements.add(new Line(pos, newPos, penColor));
        }
        else
        {
            elements.add(new Move(pos, newPos));
        }
        //System.out.println("from: ("+pos.x+","+pos.y+") => to: ("+newPos.x+","+newPos.y+")");
        setPos(newX, newY);
        delay();
    	
    }
    // END #272 2016-10-16

    public void bk(Integer pixels)
    {
        fd(-pixels);
    }

    // START #272 2016-10-16 (KGU)
    //public void backward(int pixels)
    //{
    //    fd(-pixels);
    //}
    public void backward(Double pixels)
    {
        forward(-pixels);
    }
    // END #272 2016-10-16
    
    // START KGU#448 2017-10-28: Enh. #443 Wrappers with color argument
    public void fd(Integer pixels, Color color)
    {
    	this.setColorNonWhite(color);
    	this.fd(pixels);
    }
    public void bk(Integer pixels, Color color)
    {
    	this.setColorNonWhite(color);
    	this.fd(-pixels);
    }
    public void forward(Double pixels, Color color)
    {
    	this.setColorNonWhite(color);
    	this.forward(pixels);
    }
    public void backward(Double pixels, Color color)
    {
    	this.setColorNonWhite(color);
    	this.forward(-pixels);
    }
    // END KGU#448 2017-10-28

    private void rl(Double degrees)
    {
        this.angle+=degrees;
        delay();
    }

    public void left(Double degrees)
    {
        rl(degrees);
    }

    private void rr(Double degrees)
    {
        rl(-degrees);
    }

    public void right(Double degrees)
    {
        rr(degrees);
    }
    
    // START KGU#303 2016-12-02: Enh. #302
    /** Non-retarded method to set the background colour directly
     * @param bgColor - the new background colour
     */
    public void setBackgroundColor(Color bgColor)
    {
    	// START KGU#480 2018-01-16: Enh. #490 - lazy initialization
    	if (frame == null) {
    		init(300, 300);
    	}
    	// END KGU#480 2018-01-16
    	backgroundColor = bgColor;
    	panel.repaint();
    }

    /** Delayed API method to set the background colour from RGB values
     * @param red
     * @param green
     * @param blue
     */
    public void setBackgroundColor(Integer red, Integer green, Integer blue)
    {
    	backgroundColor = new Color(Math.min(255, red), Math.min(255, green), Math.min(255, blue));
    	delay();
    }

    /**
     * Non-retarded method to set the default pen colour directly
     * This colour is used whenever a move is carried out with colour WHITE
     * @param bgColor - the new foreground colour
     */
    public void setPenColor(Color penColor)
    {
    	// START KGU#480 2018-01-16: Enh. #490 - lazy initialization
    	if (frame == null) {
    		init(300, 300);
    	}
    	// END KGU#480 2018-01-16
    	defaultPenColor = penColor;
    	panel.repaint();
    }

    /**
     * Delayed API method to set the default pen colour from RGB values
     * This colour is used whenever a move is carried out with colour WHITE
     * @param red
     * @param green
     * @param blue
     */
    public void setPenColor(Integer red, Integer green, Integer blue)
    {
    	defaultPenColor = new Color(Math.min(255, red), Math.min(255, green), Math.min(255, blue));
    	delay();
    }
    // END KGU#303 2016-12-02

    // KGU: Where is this method used?
    public double getAngleToHome()
    {
    	// START #272 2016-10-16 (KGU)
        //double hypo = Math.sqrt(Math.pow((pos.x-home.x), 2)+Math.pow((pos.y-home.y),2));
        //double base = (home.x-pos.x);
        //double haut = (home.y-pos.y);
        double base = (home.x-posX);
        double haut = (home.y-posY);
        double hypo = Math.sqrt(base*base + haut*haut);
        // END #272 2016-10-16

        double sinAlpha = haut/hypo;
        double cosAlpha = base/hypo;

        double alpha = Math.asin(sinAlpha);

        alpha = alpha/Math.PI*180;

        // rounding (FIXME: what for?)
        alpha = Math.round(alpha*100)/100;

        if (cosAlpha<0) // Q2 & Q3
        {
            alpha=180-alpha;
        }
        alpha =- alpha;
        alpha -= getAngle();

        while (alpha < 0)
        {
            alpha+=360;
        }
        
        while (alpha>=360)
        {
            alpha-=360;
        }

        return alpha;
    }

    /**
     * Raises the pen, i.e. subsequent moves won't leave a trace
     */
    public void penUp()
    {
        isPenDown=false;
    }

    /**
     * Pus the pen down, i.e. subsequent moves will leave a trace
     */
    public void penDown()
    {
        isPenDown=true;
    }

    /**
     * Direct positioning (without trace, without changing orientation)
     * @param x - new horizontal pixel coordinate (from left window edge)
     * @param y - new vertical pixel coordinate (from top window edge downwards)
     */
    public void gotoXY(Integer x, Integer y)
    {
        Point newPos = new Point(x,y);
        elements.add(new Move(pos,newPos));
        setPos(newPos);
        delay();
   }

    /**
     * Direct vertical positioning (without trace, without changing orientation,
     * horizontal position remains unchanged)
     * @param y - new vertical pixel coordinate (from top window edge downwards)
     */
    public void gotoY(Integer y)
    {
        gotoXY(pos.x, y);
    }

    /**
     * Direct horizontal positioning (without trace, without changing orientation,
     * vertical position remains unchanged)
     * @param x - new horizontal pixel coordinate (from left window edge)
     */
    public void gotoX(Integer x)
    {
        gotoXY(x, pos.y);
    }

    /** The turtle icon will no longer be shown, has no impact on the pen */
    public void hideTurtle()
    {
        turtleHidden=true;
        delay();
    }

    /** The turtle icon will be shown again, has no impact on the pen */
    public void showTurtle()
    {
        turtleHidden=false;
        delay();
    }

    /**
     * Directly set the current pen colour (without delay), not part of the API, does not
     * modify the default pen colour, does not avert colour WHITE.
     * @param color - the new pen colour
     * @see #setColorNonWhite(Color)
     * @see #setPenColor(Color)
     * @see #setBackground(Color)
     */
    public void setColor(Color color)
    {
        this.penColor=color;
    }

    // START KGU#448 2017-10-28: Enh. #443
    //public void setAnimationDelay(int delay)
    /* (non-Javadoc)
     * @see lu.fisch.diagrcontrol.DelayableDiagramController#setAnimationDelay(int, boolean)
     */
    public void setAnimationDelay(int delay, boolean _reinit)
    // END KGU#448 2017-10-28
    {
        if (_reinit) {
        	reinit();
        }
        this.delay=delay;
    }


    @Deprecated
    private String parseFunctionName(String str)
    {
        if (str.trim().indexOf("(")!=-1)
            return str.trim().substring(0,str.trim().indexOf("(")).trim().toLowerCase();
        else
            return null;
    }

    @Deprecated
    private String parseFunctionParam(String str, int count)
    {
    	String res = null;
    	int posParen1 = (str = str.trim()).indexOf("(");
    	if (posParen1 > -1)
    	{
    		String params = str.substring(posParen1+1, str.indexOf(")")).trim();
    		if (!params.isEmpty())
    		{
    			String[] args = params.split(",");
    			if (count < args.length) {
    				res = args[count];
    			}
    		}
    	}
    	return res;
    }

    @Deprecated
    private Double parseFunctionParamDouble(String str, int count)
    {
        String res = parseFunctionParam(str, count);
        if( res == null || res.isEmpty() ) { return 0.0; }
        return Double.valueOf(res);
    }

    @Deprecated
    public String execute(String message, Color color)
    {
        setColorNonWhite(color);
        return execute(message);
    }

	/**
	 * Sets the given color except in case of WHITE where the {@link #defaultPenColor} is used instead.
	 * Does not influence the default pen colour.
	 * @param color - the specified colour (where WHITE means default)
	 * @see #setColor(Color)
	 * @see #setPenColor(Color)
	 * @see #setBackground(Color)
	 */
	private void setColorNonWhite(Color color) {
		if(color.equals(Color.WHITE))
        {
        	// START KGU#303 2016-12-03: Enh. #302
            //this.setColor(Color.BLACK);
            this.setColor(defaultPenColor);
            // END KGU#303 2016-12-03
        }
        else this.setColor(color);
	}

	@Deprecated
    public String execute(String message)
    {
        String name = parseFunctionName(message);
        double param1 = parseFunctionParamDouble(message,0);
        double param2 = parseFunctionParamDouble(message,1);
        // START KGU#303 2016-12-02: Enh. #302
        double param3 = parseFunctionParamDouble(message,2);
        // END KGU#303 2016-12-02
        String res = new String();
        if(name!=null)
        {
           if (name.equals("init")) {
// START KGU#303 2016-12-03: Issue #302 - replaced by reinit() call
//        	   elements.clear();
//        	   angle=-90;
//        	   // START KGU#303 2016-12-02: Enh. #302
//        	   backgroundColor = Color.WHITE;
//        	   // END KGU#3032016-12-02
//        	   // START KGU#303 2016-12-03: Enh. #302
//        	   defaultPenColor = Color.BLACK;
//        	   turtleHidden = false;
//        	   // END KGU#3032016-12-03
//        	   setPos(home.getLocation());
//        	   penDown();
//        	   reinit();
// END KGU#303 2016-12-03
        	   setAnimationDelay((int) param1, true);
           }
           // START #272 2016-10-16 (KGU): Now different types (to allow to study rounding behaviour)
           //else if (name.equals("forward") || name.equals("fd")) { forward((int) param1); }
           //else if (name.equals("backward") || name.equals("bk")) { backward((int) param1); }
           else if (name.equals("forward")) { forward(param1); }
           else if (name.equals("backward")) { backward(param1); }
           else if (name.equals("fd")) { fd((int)param1); }
           else if (name.equals("bk")) { bk((int)param1); }
           // END #272 2016-10-16
           // START KGU 20141007: Wrong type casting mended (led to rotation biases)
           //else if (name.equals("left") || name.equals("rl")) { left((int) param1); }
           //else if (name.equals("right") || name.equals("rr")) { right((int) param1); }
           else if (name.equals("left") || name.equals("rl")) { left(param1); }
           else if (name.equals("right") || name.equals("rr")) { right(param1); }
           // END KGU 20141007
           else if (name.equals("penup") || name.equals("up")) { penUp(); }
           else if (name.equals("pendown") || name.equals("down")) { penDown(); }
           else if (name.equals("gotoxy")) { gotoXY((int) param1, (int) param2); }
           else if (name.equals("gotox")) { gotoX((int) param1); }
           else if (name.equals("gotoy")) { gotoY((int) param1); }
           else if (name.equals("hideturtle")) { hideTurtle(); }
           else if (name.equals("showturtle")) { showTurtle(); }
           // START KGU#303 2016-12-02: Enh. #302 - A procedure to set the backgroud colour was requested
           else if (name.equals("setbackground")) { setBackgroundColor((int)Math.abs(param1),(int)Math.abs(param2),(int)Math.abs(param3)); }
           else if (name.equals("setpencolor")) { setPenColor((int)Math.abs(param1),(int)Math.abs(param2),(int)Math.abs(param3)); }
           // END KGU#303 2016-12-02
           else { res="Procedure <"+name+"> not implemented!"; }
        }
        
        return res;
    }

    // START KGU#448 2017-10-28: Enh. #443
	/* (non-Javadoc)
	 * @see lu.fisch.diagrcontrol.DiagramController#getProcedureMap()
	 */
	@Override
	public HashMap<String, Method> getProcedureMap() {
		return definedProcedures;
	}
	// END KGU#448 2017-10-28

    // START KGU#417 2017-06-29: Enh. #424, #443 - Allow function getX(), getY(), getOrientation();
    /* (non-Javadoc)
     * @see lu.fisch.diagrcontrol.DiagramController#getFunctionMap()
     */
    public HashMap<String, Method> getFunctionMap()
    {
    	return definedFunctions;
    }
    
    // START KGU#448 2017-10-28: Enh. #417, #443 - support the generic execute method
    /**
     * Returns the current horizontal pixel coordinate.
     * @return the precise result of preceding moves, i.e. as double value
     */
    public double getX() {
    	return this.posX;
    }
    /**
     * Returns the current vertical pixel coordinate (from top downwards).
     * @return the precise result of preceding moves, i.e. as double value
     */
    public double getY() {
    	return this.posY;
    }
    // END KGU#448 2017-10-28

}

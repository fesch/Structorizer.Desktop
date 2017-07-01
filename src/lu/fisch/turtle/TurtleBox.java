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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import lu.fisch.structorizer.executor.DelayableDiagramController;
import lu.fisch.structorizer.executor.FunctionProvidingDiagramController;
import lu.fisch.turtle.elements.Element;
import lu.fisch.turtle.elements.Line;
import lu.fisch.turtle.elements.Move;
import lu.fisch.utils.StringList;

/**
 *
 * @author robertfisch
 */
// START KGU#97 2015-12-10: Inheritance change for enhancement request #48
//public class TurtleBox extends JFrame implements DiagramController
@SuppressWarnings("serial")
// START KGU#417 2017-06-30: Inheritance change for enh. #424
//public class TurtleBox extends JFrame implements DelayableDiagramController
public class TurtleBox extends JFrame implements DelayableDiagramController,  FunctionProvidingDiagramController
// END KGU#417 2017-06-30
// END KGU#97 2015-12-10
{
	// START KGU#417 2017-06-29: Enh. #424 Function capability map
	private static final HashMap<String, Class<?>[]> definedFunctions = new HashMap<String, Class<?>[]>();
	static {
		definedFunctions.put("getx", new Class<?>[]{Double.class});
		definedFunctions.put("gety", new Class<?>[]{Double.class});
		definedFunctions.put("getorientation", new Class<?>[]{Double.class});
	}
	// END KGU#417 2017-06-29
    private final String TITLE = "Turtleizer";

    private Point pos;
    // START #272 2016-10-16 (KGU)
    private double posX, posY;		// exact position
    // END #272 2016-10-16
    private Point home;
    private double angle = -90;		// upwards (north-bound)
    private Image image = (new ImageIcon(this.getClass().getResource("turtle.png"))).getImage();
    private boolean penDown = true;
    // START KGU#303 2016-12-02: Enh. #302
    //private Color penColor = Color.BLACK;
    private Color defaultPenColor = Color.BLACK;
    private Color backgroundColor = Color.WHITE;
    private Color penColor = defaultPenColor;
    // END KGU#303 2016-12-02
    private boolean turtleHidden = false;
    private int delay = 10;
    private Vector<Element> elements = new Vector<Element>();
    private JPanel panel = new JPanel()
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
            for (Element ele : elements)
            {
                ele.draw(g);
            }

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


    public TurtleBox()
    {
        init(300,300);
    }

    public TurtleBox(int width, int height)
    {
        init(width,height);
    }

    public double getAngle()
    {
        return 180+angle;
    }
    
    // START KGU#417 2017-06-29: Enh. #424
    /**
     * Returns the "external" turtle orientation in degrees in the interval
     * -180 .. 180 where<br/>
     * 0 is upwards/north (initial orientation),<br/>
     * positive is right/east,
     * negative is left/west
     * @return orientation in degrees.
     */
    private double getOrientation() {
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

    private void init(int width, int height)
    {
        this.setTitle(TITLE);
        this.setIconImage((new ImageIcon(this.getClass().getResource("turtle.png"))).getImage());

        //this.setDefaultCloseOperation(TurtleBox.EXIT_ON_CLOSE);
        //this.setDefaultCloseOperation(TurtleBox.DISPOSE_ON_CLOSE);
        this.setDefaultCloseOperation(TurtleBox.HIDE_ON_CLOSE);
        this.setBounds(0,0,width,height);
        this.getContentPane().add(panel);
        //this.setVisible(true);
        setPos(new Point(panel.getWidth()/2,panel.getHeight()/2));
        home = new Point(panel.getWidth()/2,panel.getHeight()/2);
        panel.setDoubleBuffered(true);
        panel.repaint();
    }

    @Override
    public void setVisible(boolean visible)
    {
        super.setVisible(visible);
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
        home = new Point(panel.getWidth()/2,panel.getHeight()/2);
        // START KGU#303 2016-12-03: Issue #302 - replaces disabled code above
        reinit();
        // END KGU#303 2016-12-03
        paint(this.getGraphics());
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

        if(delay!=0)
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

    public void fd(int pixels)
    {
        Point newPos = new Point(pos.x-(int) Math.round(Math.cos(angle/180*Math.PI)*pixels),
                                 pos.y+(int) Math.round(Math.sin(angle/180*Math.PI)*pixels));
        if (penDown==true)
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
    public void forward(double pixels)
    {
    	double newX = posX - Math.cos(angle/180*Math.PI) * pixels;
    	double newY = posY + Math.sin(angle/180*Math.PI) * pixels;
        Point newPos = new Point((int)Math.round(newX), (int)Math.round(newY));
        if (penDown==true)
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

    public void bk(int pixels)
    {
        fd(-pixels);
    }

    // START #272 2016-10-16 (KGU)
    //public void backward(int pixels)
    //{
    //    fd(-pixels);
    //}
    public void backward(double pixels)
    {
        forward(-pixels);
    }
    // END #272 2016-10-16

    public void rl(double angle)
    {
        this.angle+=angle;
        delay();
    }

    public void left(double angle)
    {
        rl(angle);
    }

    public void rr(double angle)
    {
        rl(-angle);
    }

    public void right(double angle)
    {
        rr(angle);
    }
    
    // START KGU#303 2016-12-02: Enh. #302
    public void setBackgroundColor(Color bgColor)
    {
    	backgroundColor = bgColor;
    	panel.repaint();
    }

    public void setBackgroundColor(int red, int green, int blue)
    {
    	backgroundColor = new Color(Math.min(255, red), Math.min(255, green), Math.min(255, blue));
    	delay();
    }

    public void setPenColor(Color penColor)
    {
    	defaultPenColor = penColor;
    	panel.repaint();
    }

    public void setPenColor(int red, int green, int blue)
    {
    	defaultPenColor = new Color(Math.min(255, red), Math.min(255, green), Math.min(255, blue));
    	delay();
    }
    // END KGU#303 2016-12-02

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

    public void penUp()
    {
        penDown=false;
    }

    public void penDown()
    {
        penDown=true;
    }

    public void gotoXY(int x, int y)
    {
        Point newPos = new Point(x,y);
        elements.add(new Move(pos,newPos));
        setPos(newPos);
        delay();
   }

    public void gotoY(int y)
    {
        gotoXY(pos.x,y);
    }

    public void gotoX(int x)
    {
        gotoXY(x,pos.y);
    }

    public void hideTurtle()
    {
        turtleHidden=true;
        delay();
    }

    public void showTurtle()
    {
        turtleHidden=false;
        delay();
    }

    public void setColor(Color color)
    {
        this.penColor=color;
    }

    public void setAnimationDelay(int delay)
    {
       this.delay=delay;
    }

    private String parseFunctionName(String str)
    {
        if (str.trim().indexOf("(")!=-1)
            return str.trim().substring(0,str.trim().indexOf("(")).trim().toLowerCase();
        else
            return null;
    }

    private String parseFunctionParam(String str, int count)
    {
        String res = null;
        if (str.trim().indexOf("(")!=-1)
        {
            String params = str.trim().substring(str.trim().indexOf("(")+1,str.trim().indexOf(")")).trim();
            if(!params.equals(""))
            {
                StringList sl = StringList.explode(params,",");
                res = sl.get(count);
            }
        }
        return res;
    }

    private Double parseFunctionParamDouble(String str, int count)
    {
        String res = parseFunctionParam(str, count);
        if( res == null || res.isEmpty() ) { return 0.0; }
        return Double.valueOf(res);
    }

    public String execute(String message, Color color)
    {
        if(color.equals(Color.WHITE))
        {
        	// START KGU#303 2016-12-03: Enh. #302
            //this.setColor(Color.BLACK);
            this.setColor(defaultPenColor);
            // END KGU#303 2016-12-03
        }
        else this.setColor(color);
        return execute(message);
    }

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
        	   reinit();
// END KGU#303 2016-12-03
        	   setAnimationDelay((int) param1);
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
           else if (name.equals("left") || name.equals("rl")) { left((double) param1); }
           else if (name.equals("right") || name.equals("rr")) { right((double) param1); }
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

    // START KGU#417 2017-06-29: Enh. #424 - Allow function getX(), getY(), getOrientation();
    public HashMap<String, Class<?>[]> getFunctionMap()
    {
    	return definedFunctions;
    }
    
    /**
     * Executes a provided function (by now only the following three
     * are supported: getX(), getY(), getOrientation()). If the function signature isn't valid or some evaluation error occurs, a {@code FunctionException} will be thrown
     * @param name - the function name
     * @param arguments - function arguments as objects (number and types must match a signature specification provided by {@link #getFunctionMap()}) 
     * @return the function's result (if valid)
     */
    @Override
    public Object execute(String name, Object[] arguments) throws FunctionException
    {
    	Object result = null;
        if (name != null && definedFunctions.containsKey(name) && arguments.length == definedFunctions.get(name).length-1)
        {
        	if (name.equals("getorientation")) {
        		result = this.getOrientation();
        	}
        	else if (name.equals("getx")) {
        		result = this.posX;
        	}
        	else if (name.equals("gety")) {
        		result = this.posY;        		
        	}
        }
        else {
        	throw new FunctionException("TurtleBox: No function <" + name + "> with " + arguments.length + " arguments defined.");
        }
        return result;
    }


}

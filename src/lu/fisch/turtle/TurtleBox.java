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
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import lu.fisch.structorizer.executor.DelayableDiagramController;
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
public class TurtleBox extends JFrame implements DelayableDiagramController
// END KGU#97 2015-12-10
{
    private final String TITLE = "Turtleizer";

    private Point pos;
    // START #272 2016-10-16 (KGU)
    private double posX, posY;		// exact position
    // END #272 2016-10-16
    private Point home;
    private double angle = -90;
    private Image image = (new ImageIcon(this.getClass().getResource("turtle.png"))).getImage();
    private boolean penDown = true;
    private Color penColor = Color.BLACK;
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
            g.setColor(Color.WHITE);
            g.fillRect(0,0,getWidth(),getHeight());
            g.setColor(Color.BLACK);

            // draw all elements
            for(Element ele : elements)
            {
                ele.draw(g);
            }

            if(turtleHidden==false)
            {
            	// START #272 2016-10-16
                // fix drawing point
                //int x = (int) Math.round(pos.x - (image.getWidth(this)/2));
                //int y = (int) Math.round(pos.y - (image.getHeight(this)/2));
                // fix drawing point
                //int xRot = x+image.getWidth(this)/2;
                //int yRot = y+image.getHeight(this)/2;
                // fix drawing point
                double x = posX - (image.getWidth(this)/2);
                double y = posY - (image.getHeight(this)/2);
                // fix rotation point
                double xRot = x + image.getWidth(this)/2;
                double yRot = y + image.getHeight(this)/2;
                // END #272 2016-10-16
                // apply rotation
                g.rotate((270-angle)/180*Math.PI,xRot,yRot);
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
        elements.clear();
        angle=-90;
        setPos(new Point(panel.getWidth()/2,panel.getHeight()/2));
        home = new Point(panel.getWidth()/2,panel.getHeight()/2);
        paint(this.getGraphics());
    }

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

        // rounding
        alpha = Math.round(alpha*100)/100;

        if (cosAlpha<0) // Q2 & Q3
        {
            alpha=180-alpha;
        }
        alpha=-alpha;
        alpha-=getAngle();

        while (alpha<0)
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
        if (str.trim().indexOf("(")!=-1)
        {
            String params = str.trim().substring(str.trim().indexOf("(")+1,str.trim().indexOf(")")).trim();
            if(!params.equals(""))
            {
                StringList sl = StringList.explode(params,",");
                return sl.get(count);
            }
            else return null;
        }
        else return null;
    }

    private Double parseFunctionParamDouble(String str, int count)
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
        if( res == null) { return 0.0; }
        else if (res.equals("")) { return 0.0; }
        else { return Double.valueOf(res); }
    }

    public String execute(String message, Color color)
    {
        if(color.equals(Color.WHITE))
        {
            this.setColor(Color.BLACK);
        }
        else this.setColor(color);
        return execute(message);
    }

    public String execute(String message)
    {
        String name = parseFunctionName(message);
        double param1 = parseFunctionParamDouble(message,0);
        double param2 = parseFunctionParamDouble(message,1);
        String res = new String();
        if(name!=null)
        {
           if (name.equals("init")) { elements.clear(); angle=-90; setPos(home.getLocation()); penDown(); setAnimationDelay((int) param1); }
           // START #272 2016-10-16 (KGU): Now different types (to allow to study rounding behaviour)
           //else if (name.equals("forward") || name.equals("fd")) { forward((int) param1); }
           //else if (name.equals("backward") || name.equals("bk")) { backward((int) param1); }
           else if (name.equalsIgnoreCase("forward")) { forward(param1); }
           else if (name.equalsIgnoreCase("backward")) { backward(param1); }
           else if (name.equalsIgnoreCase("fd")) { fd((int)param1); }
           else if (name.equalsIgnoreCase("bk")) { bk((int)param1); }
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
           else { res="Function <"+name+"> not implemented!"; }
        }
        
        return res;
    }

}

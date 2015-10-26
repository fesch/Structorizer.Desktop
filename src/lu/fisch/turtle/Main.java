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
import lu.fisch.structorizer.gui.Mainform;

/**
 *
 * @author robertfisch
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        TurtleBox turtle = new TurtleBox(500,500);
        Mainform structorizer = new Mainform();
        //structorizer.getEditor().diagram.setDiagramController(turtle);
        //structorizer.getEditor().diagram.setAnalyser(false);
        //structorizer.getEditor().diagram.setHightlightVars(false);


        //turtle.hideTurtle();
//        turtle.setAnimationDelay(10);
/*
        int rayon = 200;
        float cotes = 40;
        float angle = 360/cotes;
        double cote = Math.sqrt(2*Math.pow(rayon,2)*(1-Math.cos(angle/180*Math.PI)));

        turtle.penUp();
        turtle.left(90);
        turtle.forward(rayon);
        turtle.right(90);
        turtle.penDown();

        turtle.left(360/(2*cotes));

        for(int i = 1;i<=cotes;i++)
        {
            turtle.right(angle);
            turtle.forward((int) Math.round(cote));


            double angleTo = turtle.getAngleToHome();
            System.out.println(angle);
            turtle.left(angleTo);
            turtle.forward(rayon);
            turtle.backward(rayon);
            turtle.right(angleTo);

/*
 turtle.right(90);
            for(int j=0;j<10;j++)
            {
                turtle.setColor(Color.RED);
                turtle.forward(5);
                turtle.left(180/10);
                turtle.setColor(Color.BLACK);
            }
            turtle.right(90);
*/
       // }
         /**/


    }

}

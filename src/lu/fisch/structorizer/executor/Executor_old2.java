/*
    Structorizer
    A little tool which you can use to create Nassi-Schneiderman Diagrams (NSD)

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

package lu.fisch.structorizer.executor;

import bsh.EvalError;
import bsh.Interpreter;
import com.stevesoft.pat.Regex;
import java.awt.Color;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.gui.Diagram;
import lu.fisch.structorizer.parsers.D7Parser;
import lu.fisch.utils.BString;
import lu.fisch.utils.StringList;

/**
 *
 * @author robertfisch
 */
public class Executor_old2 implements Runnable
{

    /**
     * @param aStop the stop to set
     */
    public void setStop(boolean aStop)
    {
        synchronized(this)
        {
            stop = aStop;
            paus=false;
            step=false;
            this.notify();
        }
    }

    /**
     * @param aStep the step to set
     */
    public void doStep()
    {
        synchronized(this)
        {
            paus = false;
            step = true;
            this.notify();
        }
    }

    /**
     * @param aPaus the step to set
     */
    public void setPaus(boolean aPaus)
    {
        synchronized(this)
        {
            paus = aPaus;
            if(paus==false) step=false;
            this.notify();
        }
    }

    public boolean getPaus()
    {
        synchronized(this)
        {
            return paus;
        }
    }

    /**
     * @param delay the delay to set
     */
    public void setDelay(int aDelay)
    {
        delay = aDelay;
    }

    public boolean isRunning()
    {
        return running;
    }

    private Diagram diagram = null;
    private DiagramController diagramController = null;

    private boolean stop = false;
    private boolean step = false;
    private boolean paus = false;
    private int delay = 50;
    private boolean running = false;
    private StringList variables = new StringList();

    private Interpreter interpreter;

    boolean returned = false;

    private static Executor_old2 mySelf = null;

    private Control control = new Control();


    private Executor_old2(Diagram diagram, DiagramController diagramController)
    {
        this.diagram = diagram;
        this.diagramController = diagramController;
    }

    public static Executor_old2 getInstance(Diagram diagram, DiagramController diagramController)
    {
       if (mySelf==null)
       {
            mySelf = new Executor_old2(diagram,diagramController);
       }
       if(diagramController!=null) mySelf.diagramController=diagramController;
       if(diagram!=null) mySelf.diagram=diagram;
       mySelf.control.init();
       mySelf.control.setLocationRelativeTo(diagram);
       mySelf.control.validate();
       mySelf.control.setVisible(true);
       mySelf.control.repaint();

       return mySelf;
    }

    public static Executor_old2 getInstance()
    {
       return mySelf;
    }

    public String getExec(String cmd)
    {
        String result = "";
        if(diagramController!=null)
        {
            result = diagramController.execute(cmd);
        }
        else delay();
        if(delay!=0)
        {
            diagram.redraw();
            try { Thread.sleep(delay); }
            catch (InterruptedException e) { System.out.println(e.getMessage());}
        }
        return result;
    }

    private void delay()
    {
        if(delay!=0)
        {
            diagram.redraw();
            try { Thread.sleep(delay); }
            catch (InterruptedException e) { System.out.println(e.getMessage());}
        }
        waitForNext();
    }

    public String getExec(String cmd, Color color)
    {
        String result = "";
        if(diagramController!=null)
        {
            result = diagramController.execute(cmd,color);
        }
        else delay();
        if(delay!=0)
        {
            diagram.redraw();
            try { Thread.sleep(delay); }
            catch (InterruptedException e) { System.out.println(e.getMessage());}
        }
        return result;
    }

    private void waitForNext()
    {
        synchronized (this)
        {
            while (paus==true)
            {
                try { wait(); }
                catch (Exception e) { System.out.println(e.getMessage()); }
            }
        }
        /*
        int i = 0;
        while(paus==true)
        {
            System.out.println(i);

            try { Thread.sleep(100); }
            catch (InterruptedException e) { System.out.println(e.getMessage());}
            i++;
        }
        */

        if(step==true)
        {
           paus=true;
        }
   }

    public void start(boolean useSteps)
    {
        running=true;
        paus=useSteps;
        step=useSteps;
        stop=false;
        variables = new StringList();
        control.updateVars(new Vector<Vector<Object>>());

        Thread runner = new Thread(this, "Player");
        runner.start();
    }

   public boolean isNumneric(String input )
   {
      try
      {
         Double.parseDouble( input );
         return true;
      }
      catch( Exception e )
      {
          return false;
      }
    }

    private void setVar(String name, Object content) throws EvalError
    {
        //interpreter.set(name,content);

        if(content instanceof String)
        {
            if(!isNumneric((String) content))
            {
                content = "\""+ ((String) content) + "\"";
            }
        }

        interpreter.set(name,content);
        interpreter.eval(name+" = "+content);
        variables.addIfNew(name);

        if(delay!=0)
        {
            Vector<Vector<Object>> vars = new Vector<Vector<Object>>();
            for(int i=0;i<variables.count();i++)
            {
                Vector<Object> myVar = new Vector<Object>();
                myVar.add(variables.get(i));
                myVar.add(interpreter.get(variables.get(i)));
                vars.add(myVar);
            }
            control.updateVars(vars);
        }

    }

    public void execute()
    {
        Root root = diagram.getRoot();

        boolean analyserState = diagram.getAnalyser();
        diagram.setAnalyser(false);
        initInterpreter();
        String result = "";
        returned = false;

        StringList params = root.getParameterNames();
        params.reverse();
        for(int i=0;i<params.count();i++)
        {
            String in = params.get(i);
            String str = JOptionPane.showInputDialog(null, "Please enter a value for <"+in+">",null);
            if(str==null) 
            {
                i=params.count();
                result="Manual break!";
                break;
            }
            try
            {
                        // first add as string
                        setVar(in,str);
                        // try adding as char
                        try
                        {
                            if(str.length()==1)
                            {
                             Character strc = str.charAt(0);
                             setVar(in,strc);
                            }
                        }
                        catch(Exception e) {}
                        // try adding as double
                        try
                        {
                            double strd = Double.parseDouble(str);
                            setVar(in,strd);
                        }
                        catch(Exception e) {}
                        // finally try adding as integer
                        try
                        {
                            int stri = Integer.parseInt(str);
                            setVar(in,stri);
                        }
                        catch(Exception e) {}
            }
            catch (EvalError ex)
            {
                result=ex.getMessage();
            }
        }

        if(result.equals(""))
        {
            result = step(root);
            if(result.equals("") && stop==true)
            {
                result="Manual break!";
            }
        }
        
        diagram.redraw();
        if(!result.equals(""))
        {
            JOptionPane.showMessageDialog(diagram, result, "Error", 0);
        }
        else
        {
            if(root.isProgram==false && returned==false)
            {
                StringList posres = new StringList();
                posres.add(root.getMethodName());
                posres.add("result");
                posres.add("RESULT");
                posres.add("Result");

                try
                {
                    int i = 0;
                    while(i<posres.count() && returned==false)
                    {
                        Object n = interpreter.get(posres.get(i));
                        if(n!=null)
                        {
                            JOptionPane.showMessageDialog(diagram, n, "Returned result", 0);
                            returned=true;
                        }
                        i++;
                    }
                }
                catch (EvalError ex)
                {
                    Logger.getLogger(Executor.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            
        }
        diagram.setAnalyser(analyserState);
    }

    private void initInterpreter()
    {
        try
        {
            interpreter = new Interpreter();
            String pascalFunction;
            // random
            pascalFunction = "public int random(int max) { return (int) (Math.random()*max); }";
            interpreter.eval(pascalFunction);
            pascalFunction = "public void randomize() {  }";
            interpreter.eval(pascalFunction);
            // square
            pascalFunction = "public double sqr(Double d) { return Math.pow(d,2); }";
            interpreter.eval(pascalFunction);
            // square root
            pascalFunction = "public double sqrt(Double d) { return Math.sqrt(d); }";
            interpreter.eval(pascalFunction);
            // length of a string
            pascalFunction = "public int length(String s) { return s.length(); }";
            interpreter.eval(pascalFunction);
            // position of a substring inside another string
            pascalFunction = "public int pos(String subs, String s) { return s.indexOf(subs)+1; }";
            interpreter.eval(pascalFunction);
            pascalFunction = "public int pos(Character subs, String s) { return s.indexOf(subs)+1; }";
            interpreter.eval(pascalFunction);
            // return a substring of a string
            pascalFunction = "public String copy(String s, int start, int count) { return s.substring(start-1,start-1+count); }";
            interpreter.eval(pascalFunction);
            // delete a part of a string
            pascalFunction = "public String delete(String s, int start, int count) { return s.substring(0,start-1)+s.substring(start+count-1,s.length()); }";
            interpreter.eval(pascalFunction);
            // insert a string into anoter one
            pascalFunction = "public String insert(String what, String s, int start) { return s.substring(0,start-1)+what+s.substring(start-1,s.length()); }";
            interpreter.eval(pascalFunction);
            // string transformation
            pascalFunction = "public String lowercase(String s) { return s.toLowerCase(); }";
            interpreter.eval(pascalFunction);
            pascalFunction = "public String uppercase(String s) { return s.toUpperCase(); }";
            interpreter.eval(pascalFunction);
            pascalFunction = "public String trim(String s) { return s.trim(); }";
            interpreter.eval(pascalFunction);
        }
        catch (EvalError ex)
        {
            System.out.println(ex.getMessage());
        }
    }


    private String convert(String s)
    {
        Regex r;

        // variable assignment
        s=s.replace(":=", "<-");

        // testing
        s=s.replace("==", "=");
        s=s.replace("!=", "<>");
        s=s.replace("=", "==");
        s=s.replace("<==", "<=");
        s=s.replace(">==", ">=");
        s=s.replace("<>", "!=");

        s=s.replace(" mod ", " % ");
        s=s.replace(" div ", " / ");

        s=s.replace("cos(", "Math.cos(");
        s=s.replace("sin(", "Math.sin(");
        s=s.replace("tan(", "Math.tan(");
        // FIXME (KGU 20101014) After the previous replacements the following 3 strings will never be found!
        // (A simple swapping does NOT solve problem in any way - we need a lex scanner in advance!)
        s=s.replace("acos(", "Math.acos(");
        s=s.replace("asin(", "Math.asin(");
        s=s.replace("atan(", "Math.atan(");
        s=s.replace("abs(", "Math.abs(");
        s=s.replace("round(", "Math.round(");
        s=s.replace("min(", "Math.min(");
        s=s.replace("max(", "Math.max(");
        s=s.replace("ceil(", "Math.ceil(");
        s=s.replace("floor(", "Math.floor(");
        s=s.replace("exp(", "Math.exp(");
        s=s.replace("log(", "Math.log(");
        s=s.replace("sqrt(", "Math.sqrt(");
        s=s.replace("pow(", "Math.pow(");
        s=s.replace("toRadians(", "Math.toRadians(");
        s=s.replace("toDegrees(", "Math.toDegrees(");
        //s=s.replace("random(", "Math.random(");

        // pascal notation to access a character inside a string
 	r = new Regex("(.*)\\[(.*)\\](.*)","$1.charAt($2-1)$3");
 	r = new Regex("(.*)\\[(.*)\\](.*)","$1.substring($2-1,$2)$3");
        s=r.replaceAll(s);
        // pascal: delete
 	r = new Regex("delete\\((.*),(.*),(.*)\\)","$1=delete($1,$2,$3)");
        s=r.replaceAll(s);
        // pascal: insert
 	r = new Regex("insert\\((.*),(.*),(.*)\\)","$2=insert($1,$2,$3)");
        s=r.replaceAll(s);
        // pascal: quotes 
 	r = new Regex("([^']*?)'(([^']|'')*)'","$1\"$2\"");
        s=r.replaceAll(s);
        s=s.replace("''", "'");
        // pascal: randomize
        s=s.replace("randomize()", "randomize");
        s=s.replace("randomize", "randomize()");


        // clean up ... if needed
        s=s.replace("Math.Math.", "Math.");

        if(s.indexOf("==")>=0)
        {
            r = new Regex("(.*)==(.*)","$1");
            String left =r.replaceAll(s).trim();
            while(Function.countChar(left, '(')>Function.countChar(left,')'))
            {
                left+=')';
            }
            r = new Regex("(.*)==(.*)","$2");
            String right =r.replaceAll(s).trim();
            while(Function.countChar(right, ')')>Function.countChar(right,'('))
            {
                right='('+right;
            }
            // ---- thanks to autoboxing, we can alway use the "equals" method
            // ---- to compare things ...
            // addendum: sorry, doesn't always work.
            try
            {
                Object leftO = interpreter.eval(left);
                Object rightO = interpreter.eval(right);
                if((leftO instanceof String) || (rightO instanceof String))
                {
                    s=left+".equals("+right+")";
                }
            }
            catch (EvalError ex)
            {
                System.err.println(ex.getMessage());
            }
        }
        /*if(s.indexOf(">")>=0)
        {
            r = new Regex("(.*)>(.*)","$1");
            String left =r.replaceAll(s).trim();
            while(Function.countChar(left, '(')>Function.countChar(left,')'))
            {
                left+=')';
            }
            r = new Regex("(.*)>(.*)","$2");
            String right =r.replaceAll(s).trim();
            while(Function.countChar(right, ')')>Function.countChar(right,'('))
            {
                right='('+right;
            }
            // ---- thanks to autoboxing, we can alway use the "equals" method
            // ---- to compare things ...
            // addendum: sorry, doesn't always work.
            try
            {
                Object leftO = interpreter.eval(left);
                Object rightO = interpreter.eval(right);
                if((leftO instanceof String) || (rightO instanceof String))
                {
                    s=left+".compareTo("+right+")>0";
                }
            }
            catch (EvalError ex)
            {
                System.err.println(ex.getMessage());
            }
        }*/
        if(s.indexOf("!=")>=0)
        {
            r = new Regex("(.*)!=(.*)","$1");
            String left =r.replaceAll(s).trim();
            while(Function.countChar(left, '(')>Function.countChar(left,')'))
            {
                left+=')';
            }
            r = new Regex("(.*)!=(.*)","$2");
            String right =r.replaceAll(s).trim();
            while(Function.countChar(right, ')')>Function.countChar(right,'('))
            {
                right='('+right;
            }
            // ---- thanks to autoboxing, we can always use the "equals" method
            // ---- to compare things ...
            // addendum: sorry, doesn't always work.
            try
            {
                Object leftO = interpreter.eval(left);
                Object rightO = interpreter.eval(right);
                if((leftO instanceof String) || (rightO instanceof String))
                {
                    s="!"+left+".equals("+right+")";
                }
            }
            catch (EvalError ex)
            {
                System.err.println(ex.getMessage());
            }
        }

        //System.out.println(s);
        return s;
    }

    private String unconvert(String s)
    {
        s=s.replace("==","=");
        return s;
    }

    private String step(Element element)
    {
        String result = new String();
        if(element instanceof Root)
        {
            element.selected=true;

            int i = 0;
            getExec("init("+delay+")");

            element.waited=true;

            while(i<((Root) element).children.getSize() && result.equals("") && stop==false)
            {
                result = step(((Root) element).children.getElement(i));
                i++;
            }

            if (result.equals("")) { element.selected=false; element.waited=false; }
        }
        else if(element instanceof Instruction)
        {
            element.selected=true;
            if(delay!=0){diagram.redraw();}

            StringList sl = ((Instruction) element).getText();
            int i = 0;
            
            while(i<sl.count() && result.equals("") && stop==false)
            {
                String cmd = sl.get(i);
                //cmd=cmd.replace(":=", "<-");
                cmd=convert(cmd);
                try
                {
                    // assignment
                    if(cmd.indexOf("<-")>=0)
                    {
                        String varName = cmd.substring(0,cmd.indexOf("<-")).trim();
                        String expression = cmd.substring(cmd.indexOf("<-")+2,cmd.length()).trim();
                        cmd=cmd.replace("<-", "=");
                        // evaluate the expression
                        Object n = interpreter.eval(expression);
                        if (n==null) { result = "<"+expression+"> is not a correct or existing expression."; }
                        else { setVar(varName,n); }
                        delay();
                    }
                    // input
                    else if(cmd.indexOf(D7Parser.keywordMap.get("input"))>=0)
                    {
                        String in = cmd.substring(cmd.indexOf(D7Parser.keywordMap.get("input"))+D7Parser.keywordMap.get("input").length()).trim();
                        String str = JOptionPane.showInputDialog(null, "Please enter a value for <"+in+">",null);
                        // first add as string
                        setVar(in,str);
                        // try adding as char
                        try
                        {
                            if(str.length()==1)
                            {
                              Character strc = str.charAt(0);
                              setVar(in,strc);
                            }
                        }
                        catch(Exception e) {}
                        // try adding as double
                        try
                        {
                            double strd = Double.parseDouble(str);
                            setVar(in,strd);
                        }
                        catch(Exception e) {}
                        // finally try adding as integer
                        try
                        {
                            int stri = Integer.parseInt(str);
                            setVar(in,stri);
                        }
                        catch(Exception e) {}
                    }
                    // output
                    else if(cmd.indexOf(D7Parser.keywordMap.get("output"))>=0)
                    {
                        String out = cmd.substring(cmd.indexOf(D7Parser.keywordMap.get("output"))+D7Parser.keywordMap.get("output").length()).trim();
                        Object n = interpreter.eval(out);
                        if (n==null) { result = "<"+out+"> is not a correct or existing expression."; }
                        else { String s = unconvert(n.toString()); JOptionPane.showMessageDialog(diagram, s, "Output", 0); }
                    }
                    // return statement
                    else if(cmd.indexOf("return")>=0)
                    {
                        String out = cmd.substring(cmd.indexOf("return")+6).trim();
                        Object n = interpreter.eval(out);
                        if (n==null) { result = "<"+out+"> is not a correct or existing expression."; }
                        else { String s = unconvert(n.toString()); JOptionPane.showMessageDialog(diagram, s, "Returned result", 0); }
                        returned = true;
                    }
                    else
                    {
                        Function f = new Function(cmd);
                        if(f.isFunction())
                        {
                            if(diagramController!=null)
                            {
                                String params = new String();
                                for(int p=0;p<f.paramCount();p++)
                                {
                                    try
                                    {
                                        Object n = interpreter.eval(f.getParam(p));
                                        if (n==null) { result = "<"+f.getParam(p)+"> is not a correct or existing expression."; }
                                        else { params+=","+n.toString(); }
                                    }
                                    catch (EvalError ex)
                                    {
                                        System.out.println("PARAM: "+f.getParam(p));
                                        result=ex.getMessage();
                                    }
                                }
                                if(result.equals(""))
                                {
                                    if(f.paramCount()>0) params=params.substring(1);
                                    cmd = f.getName().toLowerCase() +"("+params+")";
                                    result = getExec(cmd, element.getColor());
                                }
                                delay();
                            }
                            else
                            {
                                interpreter.eval(cmd);
                            }
                        }
                        else
                        {
                            result = "<"+cmd+"> is not a correct function!";
                        }
                    }
                }
                catch (EvalError ex)
                {
                    result=ex.getMessage();
                }
                i++;
            }
            if (result.equals("")) element.selected=false;
        }
        else if(element instanceof Case)
        {
             try
             {
                 // select the element
                 element.selected = true;
                 if(delay!=0){diagram.redraw();}
                 // delay for this element!
                 element.waited=false;
                 delay();

                 Case c = (Case) element;
                 StringList text = c.getText();
                 String expression = text.get(0);
                 boolean done=false;
                 int last = text.count()-1;
                 if(text.get(last).trim().equals("%")) last--;
                 for(int q=1;q<=last && done==false;q++)
                 {
                     String test;
                     if(!text.get(q).contains(">=") && 
                        !text.get(q).contains("<=") && 
                        !text.get(q).contains(">") && 
                        !text.get(q).contains("<") && 
                        !text.get(q).contains("=") && 
                        !text.get(q).contains("==") && 
                        !text.get(q).contains("!="))
                     {
                         test = convert(expression+"=="+text.get(q));
                     }
                     else
                     {
                         test = convert(expression+text.get(q));
                     }

                     //System.out.println("TEST: "+test);

                     Object ne = interpreter.eval(expression);
                     //System.out.println(expression +" = "+ne.toString()+" <"+ne.getClass().getSimpleName()+">");
                     if(ne.getClass().getSimpleName().equals("Character"))
                     {
                         test=test.replace(expression,expression+".toString()");
                         //System.out.println("TEST: "+test);
                     }
                     //ne = interpreter.eval(expression);
                     //System.out.println(expression +" = "+ne.toString()+" <"+ne.getClass().getSimpleName()+">");
                     
                     
                     boolean go = false;
                     if(q==last && !text.get(text.count()-1).trim().equals("%")) go=true;
                     if(go==false)
                     {
                         Object n = interpreter.eval(test);
                         go = n.toString().equals("true");
                     }
                     if(go)
                     {
                        done=true;
                        element.waited=true;
                        int i = 0;
                        while (i < c.qs.get(q-1).getSize() && result.equals("") && stop == false)
                        {
                            result = step(c.qs.get(q-1).getElement(i));
                            i++;
                        }
                        if (result.equals(""))
                        {
                            element.selected = false;
                        }
                     }

                 }
                 if (result.equals("")) { element.selected=false; element.waited=false; }
             }
             catch (EvalError ex)
             {
                result=ex.getMessage();
             }
        }
        else if(element instanceof Alternative)
        {
            try
            {
                element.selected = true;
                if(delay!=0){diagram.redraw();}
                // delay for this element!
                element.waited=false;
                delay();

                String s = ((Alternative) element).getText().getText();
                if(!D7Parser.keywordMap.get("preAlt").equals("")){s=BString.replace(s,D7Parser.keywordMap.get("preAlt"),"");}
                if(!D7Parser.keywordMap.get("postAlt").equals("")){s=BString.replace(s,D7Parser.keywordMap.get("postAlt"),"");}
                //s=s.replace("==", "=");
                //s=s.replace("=", "==");
                //s=s.replace("<==", "<=");
                //s=s.replace(">==", ">=");
                s=convert(s);

                //System.out.println("C=  "+interpreter.get("C"));
                //System.out.println("IF: "+s);
                Object n = interpreter.eval(s);
                //System.out.println("Res= "+n);*/
                if (n==null) { result = "<"+s+"> is not a correct or existing expression."; }
                //if(getExec(s).equals("OK"))
                else if (n.toString().equals("true"))
                {
                    element.waited=true;
                    int i = 0;
                    while (i < ((Alternative) element).qTrue.getSize() && result.equals("") && stop == false)
                    {
                        result = step(((Alternative) element).qTrue.getElement(i));
                        i++;
                    }
                    if (result.equals(""))
                    {
                        element.selected = false;
                    }
                }
                else
                {
                    element.waited=true;
                    int i = 0;
                    while (i < ((Alternative) element).qFalse.getSize() && result.equals("") && stop == false)
                    {
                        result = step(((Alternative) element).qFalse.getElement(i));
                        i++;
                    }
                    if (result.equals(""))
                    {
                        element.selected = false;
                    }
                }
                if (result.equals("")) { element.selected=false; element.waited=false; }
            }
            catch (EvalError ex)
            {
                result=ex.getMessage();
            }
        }
        else if(element instanceof While)
        {
            try
            {
                element.selected = true;
                if(delay!=0){diagram.redraw();}

                String s = ((While) element).getText().getText();
         	if(!D7Parser.keywordMap.get("preWhile").equals("")){s=BString.replace(s,D7Parser.keywordMap.get("preWhile"),"");}
       		if(!D7Parser.keywordMap.get("postWhile").equals("")){s=BString.replace(s,D7Parser.keywordMap.get("postWhile"),"");}
                //s=s.replace("==", "=");
                //s=s.replace("=", "==");
                //s=s.replace("<==", "<=");
                //s=s.replace(">==", ">=");
                s=convert(s);
                //System.out.println("WHILE: "+s);

                //int cw = 0;
                Object n = interpreter.eval(s);
                if (n==null) { result = "<"+s+"> is not a correct or existing expression."; }
                //if(getExec(s).equals("OK"))
                else while (n.toString().equals("true") && result.equals("") && stop == false)
                {

                    // delay this element
                    element.waited=false;
                    delay();
                    element.waited=true;

                    int i = 0;
                    // START KGU 2010-09-14 The limitation of cw CAUSED eternal loops (rather then preventing them)
                    //while (i < ((While) element).q.children.size() && result.equals("") && stop == false && cw < 100)
                    while (i < ((While) element).q.getSize() && result.equals("") && stop == false)
                    // END KGU 2010-09-14
                    {
                        result = step(((While) element).q.getElement(i));
                        i++;
                    }
                    if (result.equals(""))
                    {
                        //cw++;
                        element.selected = true;
                    }
                    n = interpreter.eval(s);
                    if (n==null) { result = "<"+s+"> is not a correct or existing expression."; }
                }
                if (result.equals("")) { element.selected = false; element.waited=false;}
                /*if (cw > 1000000)
                {
                    element.selected = true;
                    result = "Your loop ran a million times. I think there is a problem!";
                }
                */
            }
            catch (EvalError ex)
            {
                result=ex.getMessage();
            }
        }
        else if(element instanceof Repeat)
        {
            try
            {
                element.selected = true;
                element.waited = true;
                if(delay!=0){diagram.redraw();}

                String s = ((Repeat) element).getText().getText();
                if(!D7Parser.keywordMap.get("preRepeat").equals("")){s=BString.replace(s,D7Parser.keywordMap.get("preRepeat"),"");}
                if(!D7Parser.keywordMap.get("postRepeat").equals("")){s=BString.replace(s,D7Parser.keywordMap.get("postRepeat"),"");}
                //s=s.replace("==", "=");
                //s=s.replace("=", "==");
                //s=s.replace("<==", "<=");
                //s=s.replace(">==", ">=");
                s=convert(s);
                //System.out.println("REPEAT: "+s
                
                //int cw = 0;
                Object n = null;
                do
                {
                    int i = 0;
                    // START KGU 2010-09-14 The limitation of cw CAUSED eternal loops (rather then preventing them)
                    //while (i < ((Repeat) element).q.children.size() && result.equals("") && stop == false && cw < 100)
                    while (i < ((Repeat) element).q.getSize() && result.equals("") && stop == false)
                    // END KGU 2010-09-14
                    {
                        result = step(((Repeat) element).q.getElement(i));
                        i++;
                    }

                    if (result.equals(""))
                    {
                        //cw++;
                        element.selected = true;
                    }
                    
                    n = interpreter.eval(s);
                    System.out.println(s);
                    if (n==null) { result = "<"+s+"> is not a correct or existing expression."; }

                    // delay this element
                    element.waited = false;
                    delay();
                    element.waited = true;

                }
                while (!(n.toString().equals("true") && result.equals("") && stop == false));

                if (result.equals("")) { element.selected = false; element.waited=false;}
                /*if (cw > 100)
                {
                    element.selected = true;
                    result = "Problem!";
                }*/
            }
            catch (EvalError ex)
            {
                result=ex.getMessage();
            }
        }
        else if(element instanceof For)
        {
            try
            {
                element.selected = true;
                if (delay != 0) { diagram.redraw(); }

                String str = ((For) element).getText().getText();
                // cut of the start of the expression
                if (!D7Parser.keywordMap.get("preFor").equals(""))
                {
                    str = BString.replace(str, D7Parser.keywordMap.get("preFor"), "");
                }
                // trim blanks
                str = str.trim();
                // modify the later word
                if (!D7Parser.keywordMap.get("postFor").equals(""))
                {
                    str = BString.replace(str, D7Parser.keywordMap.get("postFor"), "<=");
                }
                // do other transformations
                //str = CGenerator.transform(str);
                String counter = str.substring(0, str.indexOf("="));
                // complete

                String s = str.substring(str.indexOf("=")+1,str.indexOf("<=")).trim();
                s=convert(s);
                Object n = interpreter.eval(s);
                if (n==null) { result = "<"+s+"> is not a correct or existing expression."; }
                int ival = 0;
                if (n instanceof Integer) { ival= (Integer) n; }
                if (n instanceof Long) { ival= ((Long) n).intValue(); }
                if (n instanceof Float) { ival= ((Float) n).intValue(); }
                if (n instanceof Double) { ival= ((Double) n).intValue(); }

                s=str.substring(str.indexOf("<=") + 2, str.length()).trim();
                s=convert(s);
                n = interpreter.eval(s);
                if (n==null) { result = "<"+s+"> is not a correct or existing expression."; }
                int fval = 0;
                if (n instanceof Integer) { fval= (Integer) n; }
                if (n instanceof Long) { fval= ((Long) n).intValue(); }
                if (n instanceof Float) { fval= ((Float) n).intValue(); }
                if (n instanceof Double) { fval= ((Double) n).intValue(); }

                int cw = ival;
                while (cw <= fval && result.equals("") && stop == false)
                {
                    setVar(counter, (Integer) cw);
                    // delay for this element!
                    element.waited = false;
                    delay();
                    element.waited = true;

                    int i = 0;
                    // START KGU 2010-09-14 The limitation of cw CAUSED eternal loops (rather then preventing them)
                    //while (i < ((For) element).q.children.size() && result.equals("") && stop == false && cw < 100)
                    while (i < ((For) element).q.getSize() && result.equals("") && stop == false)
                    // END KGU 2010-09-14
                    {
                        result = step(((For) element).q.getElement(i));
                        i++;
                    }
                    cw++;
                }
                if (result.equals(""))
                {
                    element.selected = false;
                    element.waited = false;
                }
            }
            catch (EvalError ex)
            {
                result=ex.getMessage();
            }
        }
        return result;
    }

    public void run()
    {
        execute();
        running=false;
        control.setVisible(false);
    }
}

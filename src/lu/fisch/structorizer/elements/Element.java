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

package lu.fisch.structorizer.elements;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    Abstract class for all Elements.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.09      First Issue
 *      Kay Gürtzig     2014.11.11      Operator highlighting modified (sse comment)
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *      2014.10.18 / 2014.11.11
 *      - Additions for highlighting of logical operators (both C and Pascal style) in methods
 *        writeOutVariables() and getWidthOutVariables(),
 *        minor code revision respecting 2- and 3-character operator symbols
 *
 ******************************************************************************************************///


import java.awt.Color;
import java.awt.Font;


import lu.fisch.utils.*;
import lu.fisch.graphics.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.io.*;

import com.stevesoft.pat.*;  //http://www.javaregex.com/
import java.awt.Point;

public abstract class Element {
	// Program CONSTANTS
	public static String E_VERSION = "3.22-28";
	public static String E_THANKS =
	"Developed and maintained by\n"+
	" - Robert Fisch <robert.fisch@education.lu>\n"+
	"\n"+
	"Having also put his fingers into the code\n"+
        " - Kay Gürtzig <kay.guertzig@fh-erfurt.de>\n"+
	"\n"+
	"Export classes written and maintained by\n"+
	" - Oberon: Klaus-Peter Reimers <k_p_r@freenet.de>\n"+
	" - Perl: Jan Peter Klippel <structorizer@xtux.org>\n"+
	" - KSH: Jan Peter Klippel <structorizer@xtux.org>\n"+
	" - BASH: Markus Grundner <markus@praised-land.de>\n"+
	" - Java: Gunter Schillebeeckx <gunter.schillebeeckx@tsmmechelen.be>\n"+
	" - C: Gunter Schillebeeckx <gunter.schillebeeckx@tsmmechelen.be>\n"+
	" - C#: Kay Gürtzig <kay.guertzig@fh-erfurt.de>\n"+
	" - PHP: Rolf Schmidt <rolf.frogs@t-online.de>\n"+
	"\n"+
	"License setup and checking done by\n"+
	" - Marcus Radisch <radischm@googlemail.com>\n"+
	" - Stephan <clauwn@freenet.de>\n"+
	"\n"+
	"Usermanuel edited by\n"+
	" - David Morais <narutodc@hotmail.com>\n"+
	" - Praveen Kumar <praveen_sonal@yahoo.com>\n"+
	" - Jan Ollmann <bkgmjo@gmx.net>\n"+
	"\n"+
	"Translations realised by\n"+
	" - NL: Jerone <jeronevw@hotmail.com>\n"+
	" - DE: Klaus-Peter Reimers <k_p_r@freenet.de>\n"+
	" - LU: Laurent Zender <laurent.zender@hotmail.de>\n"+
	" - ES: Andres Cabrera <andrescabrera20@gmail.com>\n"+
        " - PT/BR: Theldo Cruz <cruz@pucminas.br>\n"+
        " - IT: Andrea Maiani <andreamaiani@gmail.com>\n"+
        " - CN: Wang Lei <wanglei@hollysys.com>\n"+
        " - CZ: Vladimír Vaščák <vascak@spszl.cz>\n"+
        " - RU: Юра Лебедев <elita.alegator@gmail.com>\n"+
	"\n"+
	"Different good ideas and improvements provided by\n"+
	" - Serge Marelli <serge.marelli@education.lu>\n"+
	" - T1IF1 2006/2007\n"+
	" - Gil Belling <gil.belling@education.lu>\n"+
	" - Guy Loesch <guy.loesch@education.lu>\n"+
	" - Claude Sibenaler <claude.sibenaler@education.lu>\n"+
	" - Tom Van Houdenhove <tom@vanhoudenhove.be>\n"+
	" - Sylvain Piren <sylvain.piren@education.lu>\n"+
	" - Bernhard Wiesner <bernhard.wiesner@informatik.uni-erlangen.de>\n"+
	" - Christian Fandel <christian_fandel@web.de>\n"+
	" - Sascha Meyer <harlequin2@gmx.de>\n"+
	" - Andreas Jenet <ajenet@gmx.de>\n"+
	" - Jan Peter Klippel <structorizer@xtux.org>\n"+
	                
	"\n"+
	"File dropper class by\n"+
	" - Robert W. Harder <robertharder@mac.com>\n"+
	"\n"+
	"Pascal parser (GOLDParser) engine by\n"+
	" - Matthew Hawkins <hawkini@barclays.net>\n"+
	"\n"+
	"Delphi grammar by\n"+
	" - Rob F.M. van den Brink <R.F.M.vandenBrink@hccnet.nl>\n"+
	"\n"+
	"Regular expression engine by\n"+
	" - Steven R. Brandt, <sbrandt@javaregex.com>\n"+
	"\n"+
	"Vector graphics export by\n"+
	" - FreeHEP Team <http://java.freehep.org/vectorgraphics>\n"+
	"\n"+
	"Command interpreter provided by\n"+
	" - Pat Niemeyer <pat@pat.net>\n"+
	"\n"+
	"Turtle icon designed by\n"+
	" - rainie_billybear@yahoo.com <rainiew@cass.net>\n"+
	"";
	public final static String E_CHANGELOG = "";

	// some static constants
	static int E_PADDING = 20;
	static int E_INDENT = 2;
	public static Color E_DRAWCOLOR = Color.YELLOW;
	public static Color E_COLLAPSEDCOLOR = Color.LIGHT_GRAY;
	public static Color E_WAITCOLOR = new Color(255,255,210);
	static Color E_COMMENTCOLOR = Color.LIGHT_GRAY;
	public static boolean E_VARHIGHLIGHT = false;
	public static boolean E_SHOWCOMMENTS = true;
	public static boolean E_TOGGLETC = false;
	public static boolean E_DIN = false;
	public static boolean E_ANALYSER = true;

	// some colors
	public static Color color0 = Color.decode("0xFFFFFF");
	public static Color color1 = Color.decode("0xFF8080");
	public static Color color2 = Color.decode("0xFFFF80");
	public static Color color3 = Color.decode("0x80FF80");
	public static Color color4 = Color.decode("0x80FFFF");
	public static Color color5 = Color.decode("0x0080FF");
	public static Color color6 = Color.decode("0xFF80C0");
	public static Color color7 = Color.decode("0xC0C0C0");
	public static Color color8 = Color.decode("0xFF8000");
	public static Color color9 = Color.decode("0x8080FF");

	// text "constants"
	public static String preAlt = "(?)";
	public static String preAltT = "T";
	public static String preAltF = "F";
	public static String preCase = "(?)\n?\n?\nelse";
	public static String preFor = "for ? <- ? to ?";
	public static String preWhile = "while (?)";
	public static String preRepeat = "until (?)";

	// used font
	static Font font = new Font("Helvetica", Font.PLAIN, 12);
	
        public static final String COLLAPSED =  "...";
        public static boolean altPadRight = true;

	// element attributes
	protected StringList text = new StringList();
	public StringList comment = new StringList();
        
	public boolean rotated = false;

	public Element parent = null;
	public boolean selected = false;
	public boolean waited = false;
	private Color color = Color.WHITE;

        private boolean collapsed = false;

	// used for drawing
	public Rect rect = new Rect();

	// abstract things
	public abstract Rect prepareDraw(Canvas _canvas);
	public abstract void draw(Canvas _canvas, Rect _top_left);
	public abstract Element copy();

        // draw point
        Point drawPoint = new Point(0,0);

        public StringList getCollapsedText()
        {
            StringList sl = new StringList();
            if(getText().count()>0) sl.add(getText().get(0));
            sl.add(COLLAPSED);
            return sl;
        }
        
        public Point getDrawPoint()
        {
            Element ele = this;
            while(ele.parent!=null) ele=ele.parent;
            return ele.drawPoint;
        }

        public void setDrawPoint(Point point)
        {
            Element ele = this;
            while(ele.parent!=null) ele=ele.parent;
            ele.drawPoint=point;
        }


        public Element()
	{
	}

	public Element(String _strings)
	{
		setText(_strings);
	}

	public Element(StringList _strings)
	{
		setText(_strings);
	}

	public void setText(String _text)
	{
		getText().setText(_text);
	}

	public void setText(StringList _text)
	{
		text=_text;
	}

	public StringList getText()
	{
            Root root = getRoot(this);
            if(root!=null)
            {
                if(root.isSwitchTextAndComments())
                    return comment;
                else
                    return text;
            }
            else return text;
	}

	public void setComment(String _comment)
	{
		comment.setText(_comment);
	}

	public void setComment(StringList _comment)
	{
		comment=_comment;
	}

	public StringList getComment()
	{
            Root root = getRoot(this);
            if(root!=null)
            {
                if(root.isSwitchTextAndComments())
                    return text;
                else
                    return comment;
            }
            else return comment;
	}

	public boolean getSelected()
	{
		return selected;
	}

	public void setSelected(boolean _sel)
	{
		selected=_sel;
	}

	public Color getColor()
	{
		return color;
	}

	public String getHexColor()
	{
		String rgb = Integer.toHexString(color.getRGB());
		return rgb.substring(2, rgb.length());
	}

	public static String getHexColor(Color _color)
	{
		String rgb = Integer.toHexString(_color.getRGB());
		return rgb.substring(2, rgb.length());
	}

	public void setColor(Color _color)
	{
		color = _color;
	}

	public Element selectElementByCoord(int _x, int _y)
	{
            Point pt=getDrawPoint();

            if ((rect.left-pt.x<_x)&&(_x<rect.right-pt.x)&&
                    (rect.top-pt.y<_y)&&(_y<rect.bottom-pt.y))
            {
                    return this;
            }
            else
            {
                    selected=false;
                    return null;
            }
	}

	public Element getElementByCoord(int _x, int _y)
	{
            Point pt=getDrawPoint();

            if ((rect.left-pt.x<_x)&&(_x<rect.right-pt.x)&&
                    (rect.top-pt.y<_y)&&(_y<rect.bottom-pt.y))
            {
                    return this;
            }
            else
            {
                    return null;
            }
	}

        public Rect getRect()
        {
            return new Rect(rect.left,rect.top,rect.right,rect.bottom);
        }

	public static Font getFont()
	{
		return font;
	}

	public static void setFont(Font _font)
	{
		font=_font;
	}

	/************************
	 * static things
	 ************************/

	public static void loadFromINI()
	{
		try
		{
			Ini ini = Ini.getInstance();
			ini.load();
			// elements
			preAltT=ini.getProperty("IfTrue","V");
			preAltF=ini.getProperty("IfFalse","F");
			preAlt=ini.getProperty("If","()");
			StringList sl = new StringList();
			sl.setCommaText(ini.getProperty("Case","\"?\",\"?\",\"?\",\"sinon\""));
			preCase=sl.getText();
			preFor=ini.getProperty("For","pour ? <- ? \u00E0 ?");
			preWhile=ini.getProperty("While","tant que ()");
			preRepeat=ini.getProperty("Repeat","jusqu'\u00E0 ()");
			// font
			setFont(new Font(ini.getProperty("Name","Dialog"), Font.PLAIN,Integer.valueOf(ini.getProperty("Size","12")).intValue()));
			// colors
			color0=Color.decode("0x"+ini.getProperty("color0","FFFFFF"));
			color1=Color.decode("0x"+ini.getProperty("color1","FF8080"));
			color2=Color.decode("0x"+ini.getProperty("color2","FFFF80"));
			color3=Color.decode("0x"+ini.getProperty("color3","80FF80"));
			color4=Color.decode("0x"+ini.getProperty("color4","80FFFF"));
			color5=Color.decode("0x"+ini.getProperty("color5","0080FF"));
			color6=Color.decode("0x"+ini.getProperty("color6","FF80C0"));
			color7=Color.decode("0x"+ini.getProperty("color7","C0C0C0"));
			color8=Color.decode("0x"+ini.getProperty("color8","FF8000"));
			color9=Color.decode("0x"+ini.getProperty("color9","8080FF"));
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}

	public static void saveToINI()
	{
		try
		{
			Ini ini = Ini.getInstance();
			ini.load();
			// elements
			ini.setProperty("IfTrue",preAltT);
			ini.setProperty("IfFalse",preAltF);
			ini.setProperty("If",preAlt);
			StringList sl = new StringList();
			sl.setText(preCase);
			ini.setProperty("Case",sl.getCommaText());
			ini.setProperty("For",preFor);
			ini.setProperty("While",preWhile);
			ini.setProperty("Repeat",preRepeat);
			// font
			ini.setProperty("Name",getFont().getFamily());
			ini.setProperty("Size",Integer.toString(getFont().getSize()));
			// colors
			ini.setProperty("color0", getHexColor(color0));
			ini.setProperty("color1", getHexColor(color1));
			ini.setProperty("color2", getHexColor(color2));
			ini.setProperty("color3", getHexColor(color3));
			ini.setProperty("color4", getHexColor(color4));
			ini.setProperty("color5", getHexColor(color5));
			ini.setProperty("color6", getHexColor(color6));
			ini.setProperty("color7", getHexColor(color7));
			ini.setProperty("color8", getHexColor(color8));
			ini.setProperty("color9", getHexColor(color9));

			ini.save();
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}

	public static Root getRoot(Element now)
	{
		while(now.parent!=null)
		{
			now=now.parent;
		}
                if(now instanceof Root)
                    return (Root) now;
                else
                    return null;
	}

	private String cutOut(String _s, String _by)
	{
		System.out.print(_s+" -> ");
		Regex rep = new Regex("(.*?)"+BString.breakup(_by)+"(.*?)","$1\",\""+_by+"\",\"$2");
		_s=rep.replaceAll(_s);
		System.out.println(_s);
		return _s;
	}

	public static int getWidthOutVariables(Canvas _canvas, String _text, Element _this)
	{
		// init total
		int total = 0;

		StringList parts = new StringList();
		parts.add(_text);

		StringList splits = new StringList();
		Root root = getRoot(_this);

		if(root!=null)
		{
			if (root.hightlightVars==true)
			{
                        	// split
				parts=StringList.explodeWithDelimiter(parts," ");
				parts=StringList.explodeWithDelimiter(parts,".");
				parts=StringList.explodeWithDelimiter(parts,",");
				parts=StringList.explodeWithDelimiter(parts,"(");
				parts=StringList.explodeWithDelimiter(parts,")");
				parts=StringList.explodeWithDelimiter(parts,"[");
				parts=StringList.explodeWithDelimiter(parts,"]");
				parts=StringList.explodeWithDelimiter(parts,"-");
				parts=StringList.explodeWithDelimiter(parts,"+");
				parts=StringList.explodeWithDelimiter(parts,"/");
				parts=StringList.explodeWithDelimiter(parts,"*");
				parts=StringList.explodeWithDelimiter(parts," mod ");
				parts=StringList.explodeWithDelimiter(parts," div ");
				// START KGU 2014-11-11 Should do the same with Pascal logical operators
				parts=StringList.explodeWithDelimiter(parts," and ");
				parts=StringList.explodeWithDelimiter(parts," or ");
				parts=StringList.explodeWithDelimiter(parts," xor ");
				parts=StringList.explodeWithDelimiter(parts," not ");				
				// END KGU 2014-11-11
				parts=StringList.explodeWithDelimiter(parts,">");
				parts=StringList.explodeWithDelimiter(parts,"<");
				parts=StringList.explodeWithDelimiter(parts,"=");
				parts=StringList.explodeWithDelimiter(parts,":");
				parts=StringList.explodeWithDelimiter(parts,"!");
				parts=StringList.explodeWithDelimiter(parts,"'");
				parts=StringList.explodeWithDelimiter(parts,"\"");

				parts=StringList.explodeWithDelimiter(parts,"\\");
				parts=StringList.explodeWithDelimiter(parts,"%");

				//reassamble
				int i = 0;
				while (i<parts.count())
				{
					/* KGU 2014-10-18: Redundant code disabled
					if(i<parts.count()-2)
					{
						if(parts.get(i).equals("<") && parts.get(i+1).equals("-") && parts.get(i+2).equals("-") )
						{
							parts.set(i,"<-");
							parts.delete(i+1);
							parts.delete(i+1);
						}
						else if(parts.get(i).equals("<") && parts.get(i+1).equals("-"))
						{
							parts.set(i,"<-");
							parts.delete(i+1);
						}
						else if(parts.get(i).equals(":") && parts.get(i+1).equals("="))
						{
							parts.set(i,":=");
							parts.delete(i+1);
						}
                                                else if(parts.get(i).equals("!") && parts.get(i+1).equals("="))
                                                {
                                                        parts.set(i,"!=");
                                                        parts.delete(i+1);
                                                }
                                                else if(parts.get(i).equals("<") && parts.get(i+1).equals(">"))
                                                {
                                                        parts.set(i,"<>");
                                                        parts.delete(i+1);
                                                }
					}
					else*/ if(i<parts.count()-1)
					{
						if(parts.get(i).equals("<") && parts.get(i+1).equals("-"))
						{
							parts.set(i,"<-");
							parts.delete(i+1);
							// START KGU 2014-10-18 potential three-character assignment symbol?
							if (i<parts.count()-1 && parts.get(i+1).equals("-"))
							{
								parts.delete(i+1);
							}
							// END KGU 2014-10-18
						}
						else if(parts.get(i).equals(":") && parts.get(i+1).equals("="))
						{
							parts.set(i,":=");
							parts.delete(i+1);
						}
						// FIXME KGU Why have the following ones been added? Further below they won't be handled as a unit
                                                else if(parts.get(i).equals("!") && parts.get(i+1).equals("="))
                                                {
                                                        parts.set(i,"!=");
                                                        parts.delete(i+1);
                                                }
                                                else if(parts.get(i).equals("<") && parts.get(i+1).equals(">"))
                                                {
                                                        parts.set(i,"<>");
                                                        parts.delete(i+1);
                                                }
						// START KGU 2014-10-18: Logical two-character operators should be highlighted, too ...
                                                else if(parts.get(i).equals("&") && parts.get(i+1).equals("&"))
                                                {
                                                        parts.set(i,"&&");
                                                        parts.delete(i+1);
                                                }
                                                else if(parts.get(i).equals("|") && parts.get(i+1).equals("|"))
                                                {
                                                        parts.set(i,"||");
                                                        parts.delete(i+1);
                                                }
						// END KGU 2014-10-18
					}
					i++;
				}

				// bold font
				Font boldFont = new Font(Element.font.getName(),Font.BOLD,Element.font.getSize());
				// backup the original font
				Font backupFont = _canvas.getFont();

				StringList specialSigns = new StringList();
				specialSigns.add(".");
				specialSigns.add("[");
				specialSigns.add("]");
				specialSigns.add("\u2190");
				specialSigns.add(":=");

				specialSigns.add("+");
				specialSigns.add("/");
				specialSigns.add("*");
				specialSigns.add("-");
				specialSigns.add("var");
				specialSigns.add("mod");
				specialSigns.add("div");
				specialSigns.add("<");
				specialSigns.add(">");
				specialSigns.add("=");
				specialSigns.add("!");
				// START KGU 2014-10-18
				specialSigns.add("&&");
				specialSigns.add("||");
				specialSigns.add("and");
				specialSigns.add("or");
				specialSigns.add("xor");
				specialSigns.add("not");
				// END KGU 2014-10-18

				specialSigns.add("'");
				specialSigns.add("\"");

				StringList ioSigns = new StringList();
				ioSigns.add(D7Parser.input);
				ioSigns.add(D7Parser.output);

				for(i=0;i<parts.count();i++)
				{
					String display = parts.get(i);

					display = BString.replace(display, "<--","<-");
					display = BString.replace(display, "<-","\u2190");

					if(!display.equals(""))
					{
						// if this part has to be colored
						if(root.variables.contains(display))
						{
							// set color
							_canvas.setColor(Color.decode("0x000099"));
							// set font
							_canvas.setFont(boldFont);
						}
						// if this part has to be colored with special color
						else if(specialSigns.contains(display))
						{
							// set color
							_canvas.setColor(Color.decode("0x990000"));
							// set font
							_canvas.setFont(boldFont);
						}
						// if this part has to be colored with io color
						else if(ioSigns.contains(display))
						{
							// set color
							_canvas.setColor(Color.decode("0x007700"));
							// set font
							_canvas.setFont(boldFont);
						}

                                                // add to the total
                                                total+=_canvas.stringWidth(display);

						// reset color
						_canvas.setColor(Color.BLACK);
						// reset font
						_canvas.setFont(backupFont);

					}
				}
				//System.out.println(parts.getCommaText());
			}
			else
			{
                            // add to the total
                            total+=_canvas.stringWidth(_text);
			}
		}


            // return value
            return total;
        }

	public static void writeOutVariables(Canvas _canvas, int _x, int _y, String _text, Element _this)
	{
		StringList parts = new StringList();
		parts.add(_text);

		StringList splits = new StringList();
		Root root = getRoot(_this);

		if(root!=null)
		{
			if (root.hightlightVars==true)
			{

				// split
				parts=StringList.explodeWithDelimiter(parts," ");
				parts=StringList.explodeWithDelimiter(parts,".");
				parts=StringList.explodeWithDelimiter(parts,",");
				parts=StringList.explodeWithDelimiter(parts,"(");
				parts=StringList.explodeWithDelimiter(parts,")");
				parts=StringList.explodeWithDelimiter(parts,"[");
				parts=StringList.explodeWithDelimiter(parts,"]");
				parts=StringList.explodeWithDelimiter(parts,"-");
				parts=StringList.explodeWithDelimiter(parts,"+");
				parts=StringList.explodeWithDelimiter(parts,"/");
				parts=StringList.explodeWithDelimiter(parts,"*");
				parts=StringList.explodeWithDelimiter(parts," mod ");
				parts=StringList.explodeWithDelimiter(parts," div ");
				// START KGU 2014-11-11 Should do the same with Pascal logical operators
				parts=StringList.explodeWithDelimiter(parts," and ");
				parts=StringList.explodeWithDelimiter(parts," or ");
				parts=StringList.explodeWithDelimiter(parts," xor ");
				parts=StringList.explodeWithDelimiter(parts," not ");				
				// END KGU 2014-11-11
				parts=StringList.explodeWithDelimiter(parts,">");
				parts=StringList.explodeWithDelimiter(parts,"<");
				parts=StringList.explodeWithDelimiter(parts,"=");
				parts=StringList.explodeWithDelimiter(parts,":");
				parts=StringList.explodeWithDelimiter(parts,"!");
				parts=StringList.explodeWithDelimiter(parts,"'");
				parts=StringList.explodeWithDelimiter(parts,"\"");

				parts=StringList.explodeWithDelimiter(parts,"\\");
				parts=StringList.explodeWithDelimiter(parts,"%");

				/*
				String s = parts.getCommaText();
				//s=cutOut(s,",");
				s=cutOut(s," ");
				s=cutOut(s,".");
				s=cutOut(s,"(");
				s=cutOut(s,")");
				s=cutOut(s,"[");
				s=cutOut(s,"]");
				s=cutOut(s,"-");
				s=cutOut(s,"+");
				s=cutOut(s,"/");
				s=cutOut(s,"*");
				s=cutOut(s,"mod");
				s=cutOut(s,"div");
				s=cutOut(s,"<");
				s=cutOut(s,">");
				s=cutOut(s,"=");
				s=cutOut(s,":");
				s=cutOut(s,"'");
				s=cutOut(s,"\"");
				parts.setCommaText(s);*/

				//reassamble
				int i = 0;
				while (i<parts.count())
				{
					// KGU 2014-10-18 Code redundancy reduced 
					/*
					if(i<parts.count()-2)
					{
						if(parts.get(i).equals("<") && parts.get(i+1).equals("-") && parts.get(i+2).equals("-") )
						{
							parts.set(i,"<-");
							parts.delete(i+1);
							parts.delete(i+1);
						}
						else if(parts.get(i).equals("<") && parts.get(i+1).equals("-"))
						{
							parts.set(i,"<-");
							parts.delete(i+1);
						}
						else if(parts.get(i).equals(":") && parts.get(i+1).equals("="))
						{
							parts.set(i,":=");
							parts.delete(i+1);
						}
                                                else if(parts.get(i).equals("!") && parts.get(i+1).equals("="))
                                                {
                                                        parts.set(i,"!=");
                                                        parts.delete(i+1);
                                                }
                                                else if(parts.get(i).equals("<") && parts.get(i+1).equals(">"))
                                                {
                                                        parts.set(i,"<>");
                                                        parts.delete(i+1);
                                                }
					}
					else */ if(i<parts.count()-1)
					{
						if(parts.get(i).equals("<") && parts.get(i+1).equals("-"))
						{
							parts.set(i,"<-");
							parts.delete(i+1);
							// START KGU 2014-10-18 potential three-character assignment symbol?
							if (i<parts.count()-1 && parts.get(i+1).equals("-"))
							{
								parts.delete(i+1);
							}
							// END KGU 2014-10-18
						}
						else if(parts.get(i).equals(":") && parts.get(i+1).equals("="))
						{
							parts.set(i,":=");
							parts.delete(i+1);
						}
                                                else if(parts.get(i).equals("!") && parts.get(i+1).equals("="))
                                                {
                                                        parts.set(i,"!=");
                                                        parts.delete(i+1);
                                                }
                                                else if(parts.get(i).equals("<") && parts.get(i+1).equals(">"))
                                                {
                                                        parts.set(i,"<>");
                                                        parts.delete(i+1);
                                                }
						// START KGU 2014-10-18: Logical two-character operators should be highlighted, too ...
                                                else if(parts.get(i).equals("&") && parts.get(i+1).equals("&"))
                                                {
                                                        parts.set(i,"&&");
                                                        parts.delete(i+1);
                                                }
                                                else if(parts.get(i).equals("|") && parts.get(i+1).equals("|"))
                                                {
                                                        parts.set(i,"||");
                                                        parts.delete(i+1);
                                                }
						// END KGU 2014-10-18
					}
					i++;
				}

				// bold font
				Font boldFont = new Font(Element.font.getName(),Font.BOLD,Element.font.getSize());
				// backup the original font
				Font backupFont = _canvas.getFont();

				StringList specialSigns = new StringList();
				specialSigns.add(".");
				specialSigns.add("[");
				specialSigns.add("]");
				//specialSigns.add("<-");
				//specialSigns.add("<--");
				specialSigns.add("\u2190");
				specialSigns.add(":=");

				specialSigns.add("+");
				specialSigns.add("/");
				specialSigns.add("*");
				specialSigns.add("-");
				specialSigns.add("var");
				specialSigns.add("mod");
				specialSigns.add("div");
				specialSigns.add("<");
				specialSigns.add(">");
				specialSigns.add("=");
				specialSigns.add("!");
				// START KGU 2014-10-18
				specialSigns.add("&&");
				specialSigns.add("||");
				specialSigns.add("and");
				specialSigns.add("or");
				specialSigns.add("xor");
				specialSigns.add("not");
				// END KGU 2014-10-18

				specialSigns.add("'");
				specialSigns.add("\"");

				StringList ioSigns = new StringList();
				ioSigns.add(D7Parser.input);
				ioSigns.add(D7Parser.output);

				for(i=0;i<parts.count();i++)
				{
					String display = parts.get(i);

					display = BString.replace(display, "<--","<-");
					display = BString.replace(display, "<-","\u2190");

					if(!display.equals(""))
					{
						// if this part has to be colored
						if(root.variables.contains(display))
						{
							// set color
							_canvas.setColor(Color.decode("0x000099"));
							// set font
							_canvas.setFont(boldFont);
						}
						// if this part has to be colored with special color
						else if(specialSigns.contains(display))
						{
							// set color
							_canvas.setColor(Color.decode("0x990000"));
							// set font
							_canvas.setFont(boldFont);
						}
						// if this part has to be colored with io color
						else if(ioSigns.contains(display))
						{
							// set color
							_canvas.setColor(Color.decode("0x007700"));
							// set font
							_canvas.setFont(boldFont);
						}

						// write out text
						_canvas.writeOut(_x,_y,display);

						// update width
						_x+=_canvas.stringWidth(display);

						// reset color
						_canvas.setColor(Color.BLACK);
						// reset font
						_canvas.setFont(backupFont);

					}
				}
				//System.out.println(parts.getCommaText());
			}
			else
			{
				_canvas.writeOut(_x,_y,_text);
			}
		}
	}



    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

}

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

package lu.fisch.structorizer.gui;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    This class is responsible for loading all the application icons.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.28      First Issue
 *      Kay Gürtzig     2015.10.12      New icons (103_breakpt, 104_nobreakpt) introduced
 *      Kay Gürtzig     2015.11.19      New Arranger icon (icoArr) introduced
 *      Kay Gürtzig     2016.01.03/04   Enh. #87 (=KGU#123) Basic icons for NSD element types and
 *                                      element collapsing introduced
 *      Kay Gürtzig     2016.07.06      Enh. #188: New icon 109_wand introduced for element conversion
 *      Kay Gürtzig     2016.07.22      Enh. #199: New icon 110_help introduced for online user guide activation
 *      Kay Gürtzig     2016-07-31      Enh. #128: New icon 111_c_plus_t introduced for "commments plus text" mode
 *      Kay Gürtzig     2016-08-02      Enh. #215: New icon 112_stopwatch introduced for breapoint count triggers
 *      Kay Gürtzig     2016-09-05      Generic support for locale icons
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.awt.*;
import java.awt.image.*;
import java.util.HashMap;

import javax.swing.*;

import lu.fisch.structorizer.locales.Locales;


public class IconLoader {

	private static String from = new String("");

	protected static double scaleFactor = 1;

	// Icons
	public static ImageIcon icoNSD = new ImageIcon(getURI(from+"icons/structorizer.png"));
	public static ImageIcon icoNSD48 = new ImageIcon(getURI(from+"icons/structorizer48.png"));
	
	public static ImageIcon ico001 = getIconImage(getURI(from+"icons/001_New.png"));
	public static ImageIcon ico002 = getIconImage(getURI(from+"icons/002_Open.png"));
	public static ImageIcon ico003 = getIconImage(getURI(from+"icons/003_Save.png"));
	public static ImageIcon ico004 = getIconImage(getURI(from+"icons/004_Make.png"));
	public static ImageIcon ico005 = getIconImage(getURI(from+"icons/005_Delete.png"));
	public static ImageIcon ico006 = getIconImage(getURI(from+"icons/006_update.png"));
	public static ImageIcon ico007 = getIconImage(getURI(from+"icons/007_intBefore.png"));
	public static ImageIcon ico008 = getIconImage(getURI(from+"icons/008_altBefore.png"));
	public static ImageIcon ico009 = getIconImage(getURI(from+"icons/009_forBefore.png"));
	public static ImageIcon ico010 = getIconImage(getURI(from+"icons/010_whileBefore.png"));
	public static ImageIcon ico011 = getIconImage(getURI(from+"icons/011_repeatBefore.png"));
	public static ImageIcon ico012 = getIconImage(getURI(from+"icons/012_intAfter.png"));
	public static ImageIcon ico013 = getIconImage(getURI(from+"icons/013_altAfter.png"));
	public static ImageIcon ico014 = getIconImage(getURI(from+"icons/014_forAfter.png"));
	public static ImageIcon ico015 = getIconImage(getURI(from+"icons/015_whileAfter.png"));
	public static ImageIcon ico016 = getIconImage(getURI(from+"icons/016_repeatAfter.png"));
	public static ImageIcon ico017 = getIconImage(getURI(from+"icons/017_Eye.png"));
	public static ImageIcon ico018 = getIconImage(getURI(from+"icons/018_add.png"));
	public static ImageIcon ico019 = getIconImage(getURI(from+"icons/019_Up.png"));
	public static ImageIcon ico020 = getIconImage(getURI(from+"icons/020_Down.png"));
	public static ImageIcon ico021 = getIconImage(getURI(from+"icons/021_function.png"));
	public static ImageIcon ico022 = getIconImage(getURI(from+"icons/022_program.png"));
	public static ImageIcon ico023 = getIconImage(getURI(from+"icons/023_font.png"));
	
	public static ImageIcon ico031 = getIconImage(getURI(from+"icons/031_make_copy.png"));
	public static ImageIcon ico032 = getIconImage(getURI(from+"icons/032_make_bmp.png"));
	public static ImageIcon ico033 = getIconImage(getURI(from+"icons/033_font_up.png"));
	public static ImageIcon ico034 = getIconImage(getURI(from+"icons/034_font_down.png"));
	
	public static ImageIcon ico038 = getIconImage(getURI(from+"icons/038_redo.png"));
	public static ImageIcon ico039 = getIconImage(getURI(from+"icons/039_undo.png"));
	public static ImageIcon ico040 = getIconImage(getURI(from+"icons/040_notnice.png"));
	public static ImageIcon ico041 = getIconImage(getURI(from+"icons/041_print.png"));
	public static ImageIcon ico042 = getIconImage(getURI(from+"icons/042_copy.png"));
	public static ImageIcon ico043 = getIconImage(getURI(from+"icons/043_paste.png"));
	public static ImageIcon ico044 = getIconImage(getURI(from+"icons/044_cut.png"));
	//public static ImageIcon ico045 = getIconImage(getURI(from+"icons/045_fr.png"));
	//public static ImageIcon ico046 = getIconImage(getURI(from+"icons/046_uk.png"));
	public static ImageIcon ico047 = getIconImage(getURI(from+"icons/047_casebefore.png"));
	public static ImageIcon ico048 = getIconImage(getURI(from+"icons/048_caseafter.png"));
	public static ImageIcon ico049 = getIconImage(getURI(from+"icons/049_callbefore.png"));
	public static ImageIcon ico050 = getIconImage(getURI(from+"icons/050_callafter.png"));
	//public static ImageIcon ico051 = getIconImage(getURI(from+"icons/051_nl.png"));
	public static ImageIcon ico052 = getIconImage(getURI(from+"icons/052_update.png"));
	
	public static ImageIcon ico055 = getIconImage(getURI(from+"icons/055_jumpafter.png"));
	public static ImageIcon ico056 = getIconImage(getURI(from+"icons/056_jumpbefore.png"));
	
	// START KGU#122 2016-01-03: Enhancement for collapsed elements
	public static ImageIcon ico057 = getIconImage(getURI(from+"icons/057_conv_inst.png"));
	public static ImageIcon ico058 = getIconImage(getURI(from+"icons/058_conv_call.png"));
	public static ImageIcon ico059 = getIconImage(getURI(from+"icons/059_conv_jump.png"));
	public static ImageIcon ico060 = getIconImage(getURI(from+"icons/060_conv_if.png"));
	public static ImageIcon ico061 = getIconImage(getURI(from+"icons/061_conv_for.png"));
	public static ImageIcon ico062 = getIconImage(getURI(from+"icons/062_conv_while.png"));
	public static ImageIcon ico063 = getIconImage(getURI(from+"icons/063_conv_repeat.png"));
	public static ImageIcon ico064 = getIconImage(getURI(from+"icons/064_conv_case.png"));
	// END KGU#122 2016-01-03
	
	public static ImageIcon ico074 = getIconImage(getURI(from+"icons/074_nsd.png"));
	//public static ImageIcon ico075 = getIconImage(getURI(from+"icons/075_lu.png"));
	public static ImageIcon ico076 = getIconImage(getURI(from+"icons/076_latex.png"));
	public static ImageIcon ico077 = getIconImage(getURI(from+"icons/077_bubble.png"));
	public static ImageIcon ico078 = getIconImage(getURI(from+"icons/078_java.png"));
	public static ImageIcon ico079 = getIconImage(getURI(from+"icons/079_marker.png"));
	//public static ImageIcon ico080 = getIconImage(getURI(from+"icons/080_de.png"));
	public static ImageIcon ico081 = getIconImage(getURI(from+"icons/081_pen.png"));
	public static ImageIcon ico082 = getIconImage(getURI(from+"icons/082_din.png"));
	public static ImageIcon ico083 = getIconImage(getURI(from+"icons/083_loupe.png"));
	//public static ImageIcon ico084 = getIconImage(getURI(from+"icons/084_es.png"));
	//public static ImageIcon ico085 = getIconImage(getURI(from+"icons/085_pt_br.png"));
	//public static ImageIcon ico086 = getIconImage(getURI(from+"icons/086_it.png"));
	//public static ImageIcon ico087 = getIconImage(getURI(from+"icons/087_cn.png"));
	//public static ImageIcon ico088 = getIconImage(getURI(from+"icons/088_cz.png"));

	public static ImageIcon ico089 = getIconImage(getURI(from+"icons/089_paraAfter.png"));
	public static ImageIcon ico090 = getIconImage(getURI(from+"icons/090_paraBefore.png"));
	public static ImageIcon ico091 = getIconImage(getURI(from+"icons/091_conv_para.png"));
	//public static ImageIcon ico092 = getIconImage(getURI(from+"icons/092_ru.png"));
	//public static ImageIcon ico093 = getIconImage(getURI(from+"icons/093_pl.png"));
	//public static ImageIcon ico094 = getIconImage(getURI(from+"icons/094_tw.png"));

	public static ImageIcon ico102 = getIconImage(getURI(from+"icons/102_switch.png"));
	
	// START KGU 2015-10-12: New checkpoint icon
	public static ImageIcon ico103 = getIconImage(getURI(from+"icons/103_breakpt.png"));
	public static ImageIcon ico104 = getIconImage(getURI(from+"icons/104_nobreakpt.png"));
	// END KGU 2015-10-12
	// START KGU#2 2015-11-19: Arranger icon
	public static ImageIcon ico105 = getIconImage(getURI(from+"icons/105_arranger.png"));
	// END KGU 2015-10-12
	// START KGU#123 2016-01-03/04: Enh. #87
	public static ImageIcon ico106 = getIconImage(getURI(from+"icons/106_collapse.png"));
	public static ImageIcon ico107 = getIconImage(getURI(from+"icons/107_expand.png"));
	public static ImageIcon ico108 = getIconImage(getURI(from+"icons/108_collapse_by_wheel.png"));
	// END KGU#123 2016-01-03/04
	// START KGU#199 2016-07-06: Enh. #188
	public static ImageIcon ico109 = getIconImage(getURI(from+"icons/109_wand.png"));
	// END KGU#199 2016-07-06
	// START KGU#208 2016-07-22: Enh. #199 - direct online user guide activation
	public static ImageIcon ico110 = getIconImage(getURI(from+"icons/110_help.png"));
	// END KGU#208 2016-07-22
	// START KGU#227 2016-07-31: Enh. #128 - comments plus text display
	public static ImageIcon ico111 = getIconImage(getURI(from+"icons/111_c_plus_t.png"));
	// END KGU#227 2016-07-31
	// START KGU#213 2016-08-02: Enh. #215 - breakpoint counting trigger
	public static ImageIcon ico112 = getIconImage(getURI(from+"icons/112_stopwatch.png"));
	public static ImageIcon ico113 = getIconImage(getURI(from+"icons/113_translater.png"));
	// END KGU#213 2016-08-02
	// START KGU#232 2016-08-03: Enh. #222
	//public static ImageIcon ico114 = getIconImage(getURI(from+"icons/locale_unknown.png"));
	// END KGU#232 2016-08-03

	public static ImageIcon turtle = getIconImage(getURI(from+"icons/turtle.png"));
	
	// START KGU#242 2016-09-05
	public static HashMap<String, ImageIcon> icoLocales = new HashMap<String, ImageIcon>();
	// END KGU#242 2016-09-05

        public static void setScaleFactor(double scale)
        {
            scaleFactor=scale;
            ico001 = getIconImage(getURI(from+"icons/001_New.png"));
            ico002 = getIconImage(getURI(from+"icons/002_Open.png"));
            ico003 = getIconImage(getURI(from+"icons/003_Save.png"));
            ico004 = getIconImage(getURI(from+"icons/004_Make.png"));
            ico005 = getIconImage(getURI(from+"icons/005_Delete.png"));
            ico006 = getIconImage(getURI(from+"icons/006_update.png"));
            ico007 = getIconImage(getURI(from+"icons/007_intBefore.png"));
            ico008 = getIconImage(getURI(from+"icons/008_altBefore.png"));
            ico009 = getIconImage(getURI(from+"icons/009_forBefore.png"));
            ico010 = getIconImage(getURI(from+"icons/010_whileBefore.png"));
            ico011 = getIconImage(getURI(from+"icons/011_repeatBefore.png"));
            ico012 = getIconImage(getURI(from+"icons/012_intAfter.png"));
            ico013 = getIconImage(getURI(from+"icons/013_altAfter.png"));
            ico014 = getIconImage(getURI(from+"icons/014_forAfter.png"));
            ico015 = getIconImage(getURI(from+"icons/015_whileAfter.png"));
            ico016 = getIconImage(getURI(from+"icons/016_repeatAfter.png"));
            ico017 = getIconImage(getURI(from+"icons/017_Eye.png"));
            ico018 = getIconImage(getURI(from+"icons/018_add.png"));
            ico019 = getIconImage(getURI(from+"icons/019_Up.png"));
            ico020 = getIconImage(getURI(from+"icons/020_Down.png"));
            ico021 = getIconImage(getURI(from+"icons/021_function.png"));
            ico022 = getIconImage(getURI(from+"icons/022_program.png"));
            ico023 = getIconImage(getURI(from+"icons/023_font.png"));

            ico031 = getIconImage(getURI(from+"icons/031_make_copy.png"));
            ico032 = getIconImage(getURI(from+"icons/032_make_bmp.png"));
            ico033 = getIconImage(getURI(from+"icons/033_font_up.png"));
            ico034 = getIconImage(getURI(from+"icons/034_font_down.png"));

            ico038 = getIconImage(getURI(from+"icons/038_redo.png"));
            ico039 = getIconImage(getURI(from+"icons/039_undo.png"));
            ico040 = getIconImage(getURI(from+"icons/040_notnice.png"));
            ico041 = getIconImage(getURI(from+"icons/041_print.png"));
            ico042 = getIconImage(getURI(from+"icons/042_copy.png"));
            ico043 = getIconImage(getURI(from+"icons/043_paste.png"));
            ico044 = getIconImage(getURI(from+"icons/044_cut.png"));
            //ico045 = getIconImage(getURI(from+"icons/045_fr.png"));
            //ico046 = getIconImage(getURI(from+"icons/046_uk.png"));
            ico047 = getIconImage(getURI(from+"icons/047_casebefore.png"));
            ico048 = getIconImage(getURI(from+"icons/048_caseafter.png"));
            ico049 = getIconImage(getURI(from+"icons/049_callbefore.png"));
            ico050 = getIconImage(getURI(from+"icons/050_callafter.png"));
            //ico051 = getIconImage(getURI(from+"icons/051_nl.png"));
            ico052 = getIconImage(getURI(from+"icons/052_update.png"));

            ico055 = getIconImage(getURI(from+"icons/055_jumpafter.png"));
            ico056 = getIconImage(getURI(from+"icons/056_jumpbefore.png"));

        	// START KGU#122 2016-01-03: Enhancement for collapsed elements
        	ico057 = getIconImage(getURI(from+"icons/057_conv_inst.png"));
        	ico058 = getIconImage(getURI(from+"icons/058_conv_call.png"));
        	ico059 = getIconImage(getURI(from+"icons/059_conv_jump.png"));
        	ico060 = getIconImage(getURI(from+"icons/060_conv_if.png"));
        	ico061 = getIconImage(getURI(from+"icons/061_conv_for.png"));
        	ico062 = getIconImage(getURI(from+"icons/062_conv_while.png"));
        	ico063 = getIconImage(getURI(from+"icons/063_conv_repeat.png"));
        	ico064 = getIconImage(getURI(from+"icons/064_conv_case.png"));
        	// END KGU#122 2016-01-03

        	ico074 = getIconImage(getURI(from+"icons/074_nsd.png"));
            //ico075 = getIconImage(getURI(from+"icons/075_lu.png"));
            ico076 = getIconImage(getURI(from+"icons/076_latex.png"));
            ico077 = getIconImage(getURI(from+"icons/077_bubble.png"));
            ico078 = getIconImage(getURI(from+"icons/078_java.png"));
            ico079 = getIconImage(getURI(from+"icons/079_marker.png"));
            //ico080 = getIconImage(getURI(from+"icons/080_de.png"));
            ico081 = getIconImage(getURI(from+"icons/081_pen.png"));
            ico082 = getIconImage(getURI(from+"icons/082_din.png"));
            ico083 = getIconImage(getURI(from+"icons/083_loupe.png"));
            //ico084 = getIconImage(getURI(from+"icons/084_es.png"));
            //ico085 = getIconImage(getURI(from+"icons/085_pt_br.png"));
            //ico086 = getIconImage(getURI(from+"icons/086_it.png"));
            //ico087 = getIconImage(getURI(from+"icons/087_cn.png"));
            //ico088 = getIconImage(getURI(from+"icons/088_cz.png"));
            //ico092 = getIconImage(getURI(from+"icons/092_ru.png"));
            //ico093 = getIconImage(getURI(from+"icons/093_pl.png"));
            //ico094 = getIconImage(getURI(from+"icons/094_tw.png"));

            // START KGU 2015-10-12: Whatever this might be good for...(?)
        	ico103 = getIconImage(getURI(from+"icons/103_breakpt.png"));
        	ico104 = getIconImage(getURI(from+"icons/104_nobreakpt.png"));
        	// END KGU 2015-10-12
        	// START KGU#2 2015-11-19
        	ico105 = getIconImage(getURI(from+"icons/105_arranger.png"));
        	// END KGU#2 2015-11-19
        	// START KGU#123 2016-01-03/04: Enh. #87
        	ico106 = getIconImage(getURI(from+"icons/106_collapse.png"));
        	ico107 = getIconImage(getURI(from+"icons/107_expand.png"));
        	ico108 = getIconImage(getURI(from+"icons/108_collapse_by_wheel.png"));
        	// END KGU#123 2016-01-03/04
        	// START KGU#199 2016-07-06: Enh. #188
        	ico109 = getIconImage(getURI(from+"icons/109_wand.png"));
        	// END KGU#199 2016-07-06
        	// START KGU#208 2016-07-22: Enh. #199 - direct online user guide activation
        	ico110 = getIconImage(getURI(from+"icons/110_help.png"));
        	// END KGU#208 2016-07-22
        	// START KGU#227 2016-07-31: Enh. #128 - comments plus text display mode
        	ico111 = getIconImage(getURI(from+"icons/111_c_plus_t.png"));
        	// END KGU#227 2016-07-31
        	// START KGU#213 2016-08-02: Enh. #215 - breakpoint counting trigger
        	ico112 = getIconImage(getURI(from+"icons/112_stopwatch.png"));
        	// END KGU#213 2016-08-02

        	// START KGU#232 2016-08-02: Enh. #222 - more flexible language export
        	//ico114 = getIconImage(getURI(from+"icons/locale_unknown.png"));
        	// END KGU#232 2016-08-02

        	turtle = getIconImage(getURI(from+"icons/turtle.png"));
        	
        	// START KGU#242 2016-09-05
        	for (String key: icoLocales.keySet())
        	{
        		icoLocales.put(key, getIconImage(getURI(from+"icons/locale_"+key+".png")));
        	}
        	// END KGU#242 2016-09-05
        }

        public static ImageIcon getIconImage(java.net.URL url)
        {
            ImageIcon ii = new ImageIcon(url);
            ii = scale(ii);
            return ii;
        }
        
        // START KGU 2016-09-06
        public static ImageIcon getLocaleIconImage(String localeName)
        {
        	ImageIcon ii = icoLocales.get(localeName);
        	if (ii == null && Locales.isNamedLocale(localeName))
        	{
        		ii = getIconImage(getURI(from + "icons/locale_"+localeName+".png"));
        	}
        	ii = scale(ii);
        	return ii;
        }
        // END KGU 2016-09-06

        private static ImageIcon scale(ImageIcon src)
        {
            //System.out.println(scaleFactor);
            if(scaleFactor>1)
            {
                int w = (int)(scaleFactor*src.getIconWidth());
                int h = (int)(scaleFactor*src.getIconHeight());
                int type = BufferedImage.TYPE_INT_ARGB;
                BufferedImage dst = new BufferedImage(w, h, type);
                Graphics2D g2 = dst.createGraphics();
                g2.drawImage(src.getImage(), 0, 0, w, h, null);
                g2.dispose();
                return new ImageIcon(dst);
            }
            else return src;
        }


	public static java.net.URL getURI(String _filename)
	{
		IconLoader icol = new IconLoader();
		return icol.getClass().getResource(_filename);
	}
	
            public static ImageIcon generateIcon(Color _color)
            {
                    int size = (int) (16*scaleFactor);
                    BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D graphics = (Graphics2D) image.getGraphics();
                    graphics.setColor(Color.BLACK);
                    graphics.fillRect(0,0,size,size);
                    graphics.setColor(_color);
                    graphics.fillRect(1,1,size-2,size-2);
                    return new ImageIcon(image);
            }
	
	public static void setFrom(String _from)
	{
		from=_from;
	}
}

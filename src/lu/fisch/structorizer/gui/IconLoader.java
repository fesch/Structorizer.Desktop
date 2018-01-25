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
 *      Kay Gürtzig     2016.07.31      Enh. #128: New icon 111_c_plus_t introduced for "commments plus text" mode
 *      Kay Gürtzig     2016.08.02      Enh. #215: New icon 112_stopwatch introduced for breapoint count triggers
 *      Kay Gürtzig     2016.09.05      Generic support for locale icons
 *      Kay Gürtzig     2016.09.25      Enh. #253: New icon 025_import for import configuration
 *      Kay Gürtzig     2016.10.13      Enh. #270: New icon 026_disable for inactive elements
 *      Kay Gürtzig     2016.10.16      Enh. #272: New icons 027_richTurtle and 028_poorTurtle
 *      Kay Gürtzig     2016.11.01      Issue #81: icons 089 through 091 and 113 hadn't been scaled
 *      Kay Gürtzig     2016.12.14      Enh. #305: New icon 029_index for the Arranger index 
 *      Kay Gürtzig     2017.01.05      Enh. #319: New icons 045_remove, 046_covered for Arranger index popup
 *      Kay Gürtzig     2017.01.07      Enh. #319: New icon 030_function_green for Arranger index
 *      Kay Gürtzig     2017.01.11      Enh. #81: New icon 051_scale_gui for scaling preset
 *      Kay Gürtzig     2017.03.13      Enh. #372: New icons 065_paragraph through 067_commit for license editing
 *      Kay Gürtzig     2017.03.23      Enh. #380: Icon 068 for the conversion of a sequence to a subroutine
 *      Kay Gürtzig     2017.03.28      Enh. #387: Icon 069 for "Save All" added.
 *      Kay Gürtzig     2017.04.29      Enh. #319/#389: New icon 070_program_green for Arranger index
 *      Kay Gürtzig     2017.05.16      Enh. #389: New icons 071_include, 072_include_green
 *      Kay Gürtzig     2017.06.13      Enh. #415: New icon 073_binoculars for Find & Replace dialog
 *      Kay Gürtzig     2017.10.16      Enh. #439: New icons 080_pulldown and 084_pencil introduced
 *      Kay Gürtzig     2017.12.06      Enh. #487: New icon 085_hide_decl for hiding declarations
 *      Kay Gürtzig     2017.12.11      Enh. #425: New icons 114_down and 115_up for Translator
 *      Kay Gürtzig     2018.01.04      New icon 092_SaveAs
 *      Kay Gürtzig     2018.01.18      Issue #4: New icons 032_export, 086_properties, 087_code, 088_picture
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
    // START KGU#287 2016-11-02: Issue #81 (DPI awareness workaround)
	//public static ImageIcon icoNSD48 = new ImageIcon(getURI(from+"icons/structorizer48.png"));
	public static ImageIcon icoNSD48 = getIconImage(getURI(from+"icons/structorizer48.png"));
	// END KGU#287 2016-11-02
	
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
	// START KGU#459 2017-11-19: Issue #459
	public static ImageIcon ico024 = getIconImage(getURI(from+"icons/024_smiley.png"));
	// END KGU#459 2017-11-19
	
	// START KGU#258 2016-09-25: Enh. #253
	public static ImageIcon ico025 = getIconImage(getURI(from+"icons/025_import.png"));
	// END KGU#258 2016-09-25
	// START KGU#277 2016-10-13: Enh. #270
	public static ImageIcon ico026 = getIconImage(getURI(from+"icons/026_disable.png"));
	// END KGU#277 2016-10-13
	// START KGU#282 2016-10-16: Enh. #272
	public static ImageIcon ico027 = getIconImage(getURI(from+"icons/027_richTurtle.png"));
	public static ImageIcon ico028 = getIconImage(getURI(from+"icons/028_poorTurtle.png"));
	// END KGU#282 2016-10-16
	// START KGU#305 2016-12-14: Enh. #305
	public static ImageIcon ico029 = getIconImage(getURI(from+"icons/029_index.png"));
	// END KGU#305 2016-12-14
	// START KGU#318 2017-01-07: Enh. #319
	public static ImageIcon ico030 = getIconImage(getURI(from+"icons/030_function_green.png"));
	// END KGU#318 2017-01-07

	public static ImageIcon ico031 = getIconImage(getURI(from+"icons/031_make_copy.png"));
	// START KGU#486 2018-01-18: Issue #4 (icon redesign)
	//public static ImageIcon ico032 = getIconImage(getURI(from+"icons/032_make_bmp.png"));
	public static ImageIcon ico032 = getIconImage(getURI(from+"icons/032_export.png"));
	// END KGU#486 2018-01-18
	public static ImageIcon ico033 = getIconImage(getURI(from+"icons/033_font_up.png"));
	public static ImageIcon ico034 = getIconImage(getURI(from+"icons/034_font_down.png"));
	
	public static ImageIcon ico038 = getIconImage(getURI(from+"icons/038_redo.png"));
	public static ImageIcon ico039 = getIconImage(getURI(from+"icons/039_undo.png"));
	public static ImageIcon ico040 = getIconImage(getURI(from+"icons/040_notnice.png"));
	public static ImageIcon ico041 = getIconImage(getURI(from+"icons/041_print.png"));
	public static ImageIcon ico042 = getIconImage(getURI(from+"icons/042_copy.png"));
	public static ImageIcon ico043 = getIconImage(getURI(from+"icons/043_paste.png"));
	public static ImageIcon ico044 = getIconImage(getURI(from+"icons/044_cut.png"));
	// START KGU#318 2017-01-05: Enh. #319 - new icon for Aranger index
	public static ImageIcon ico045 = getIconImage(getURI(from+"icons/045_remove.png"));
	public static ImageIcon ico046 = getIconImage(getURI(from+"icons/046_covered.png"));
	// END KGU#318 2017-01-05
	public static ImageIcon ico047 = getIconImage(getURI(from+"icons/047_casebefore.png"));
	public static ImageIcon ico048 = getIconImage(getURI(from+"icons/048_caseafter.png"));
	public static ImageIcon ico049 = getIconImage(getURI(from+"icons/049_callbefore.png"));
	public static ImageIcon ico050 = getIconImage(getURI(from+"icons/050_callafter.png"));
	// START KGU#287 2017-01-11: Issue #81/#330
	public static ImageIcon ico051 = getIconImage(getURI(from+"icons/051_scale_gui.png"));
	// END KGU#287 2017-01-11
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
	// START KGU#363 2017-03-13: Issue #372
	public static ImageIcon ico065 = getIconImage(getURI(from+"icons/065_paragraph.png"));
	public static ImageIcon ico066 = getIconImage(getURI(from+"icons/066_litterbin.png"));
	public static ImageIcon ico067 = getIconImage(getURI(from+"icons/067_commit.png"));
	// END KGU#363 2017-03-13
	// START KGU#365 2017-03-23: Issue #380
	public static ImageIcon ico068 = getIconImage(getURI(from+"icons/068_seq2sub.png"));
	// END KGU#365 2017-03-23
	// START KGU#373 2017-03-28: Issue #387
	public static ImageIcon ico069 = getIconImage(getURI(from+"icons/069_SaveAll.png"));
	// END KGU#373 2017-03-28
	// START KGU#318/KGU#376 2017-04-29: Enh. #319, #389
	public static ImageIcon ico070 = getIconImage(getURI(from+"icons/070_program_green.png"));
	public static ImageIcon ico071 = getIconImage(getURI(from+"icons/071_include.png"));
	public static ImageIcon ico072 = getIconImage(getURI(from+"icons/072_include_green.png"));
	// END KGU#318/KGU#376 2017-04-29
	// START KGU#324 2017-06-13: Enh. #415
	public static ImageIcon ico073 = getIconImage(getURI(from+"icons/073_binoculars.png"));
	// END KGU#324 2017-06-13
	
	public static ImageIcon ico074 = getIconImage(getURI(from+"icons/074_nsd.png"));
	// START KGU#456 2017-11-05: Issue #452
	public static ImageIcon ico075 = getIconImage(getURI(from+"icons/075_beginner.png"));
	// END KGU#456 2017-11-05
	public static ImageIcon ico076 = getIconImage(getURI(from+"icons/076_latex.png"));
	public static ImageIcon ico077 = getIconImage(getURI(from+"icons/077_bubble.png"));
	public static ImageIcon ico078 = getIconImage(getURI(from+"icons/078_java.png"));
	public static ImageIcon ico079 = getIconImage(getURI(from+"icons/079_marker.png"));
	public static ImageIcon ico080 = getIconImage(getURI(from+"icons/080_pulldown.png"));
	public static ImageIcon ico081 = getIconImage(getURI(from+"icons/081_pen.png"));
	public static ImageIcon ico082 = getIconImage(getURI(from+"icons/082_din.png"));
	public static ImageIcon ico083 = getIconImage(getURI(from+"icons/083_loupe.png"));
	public static ImageIcon ico084 = getIconImage(getURI(from+"icons/084_pencil.png"));
	// START KGU#477 2017-12-06: Enh. #487
	public static ImageIcon ico085 = getIconImage(getURI(from+"icons/085_hide_decl.png"));
	// END KGU#477 2017-12-06
	// START KGU#486 2018-01-18: Issue #4 (icon redesign)
	public static ImageIcon ico086 = getIconImage(getURI(from+"icons/086_properties.png"));
	public static ImageIcon ico087 = getIconImage(getURI(from+"icons/087_code.png"));
	public static ImageIcon ico088 = getIconImage(getURI(from+"icons/088_picture.png"));
	// END KGU#486 2018-01-18

	public static ImageIcon ico089 = getIconImage(getURI(from+"icons/089_paraAfter.png"));
	public static ImageIcon ico090 = getIconImage(getURI(from+"icons/090_paraBefore.png"));
	public static ImageIcon ico091 = getIconImage(getURI(from+"icons/091_conv_para.png"));
	public static ImageIcon ico092 = getIconImage(getURI(from+"icons/092_SaveAs.png"));
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
	// START KGU#418 2017-12-11: Enh. #425
	public static ImageIcon ico114 = getIconImage(getURI(from+"icons/114_down.png"));
	public static ImageIcon ico115 = getIconImage(getURI(from+"icons/115_up.png"));
	// END KGU#213 2017-12-11

	public static ImageIcon turtle = getIconImage(getURI(from+"icons/turtle.png"));
	
	// START KGU#242 2016-09-05
	public static HashMap<String, ImageIcon> icoLocales = new HashMap<String, ImageIcon>();
	// END KGU#242 2016-09-05

        public static void setScaleFactor(double scale)
        {
            scaleFactor=scale;
            // START KGU#287 2016-11-02: Issue #81 (DPI awareness workaround)
        	icoNSD48 = getIconImage(getURI(from+"icons/structorizer48.png"));
        	// END KGU#287 2016-11-02

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
        	// START KGU#459 2017-11-19: Issue #459
        	ico024 = getIconImage(getURI(from+"icons/024_smiley.png"));
        	// END KGU#459 2017-11-19

        	// START KGU#258 2016-09-25: Enh. #253
            ico025 = getIconImage(getURI(from+"icons/025_import.png"));
        	// END KGU#258 2016-09-25
        	// START KGU#277 2016-10-13: Enh. #270
        	ico026 = getIconImage(getURI(from+"icons/026_disable.png"));
        	// END KGU#277 2016-10-13
        	// START KGU#282 2016-10-16: Enh. #272
        	ico027 = getIconImage(getURI(from+"icons/027_richTurtle.png"));
        	ico028 = getIconImage(getURI(from+"icons/028_poorTurtle.png"));
        	// END KGU#282 2016-10-16
        	// START KGU#305 2016-12-14: Enh. #305
        	ico029 = getIconImage(getURI(from+"icons/029_index.png"));
        	// END KGU#305 2016-12-14
        	// START KGU#318 2017-01-07: Enh. #319
        	ico030 = getIconImage(getURI(from+"icons/030_function_green.png"));
        	// END KGU#318 2017-01-07

            ico031 = getIconImage(getURI(from+"icons/031_make_copy.png"));
        	// START KGU#486 2018-01-18: Issue #4 (icon redesign)
            //ico032 = getIconImage(getURI(from+"icons/032_make_bmp.png"));
            ico032 = getIconImage(getURI(from+"icons/032_export.png"));
        	// END KGU#486 2018-01-18
            ico033 = getIconImage(getURI(from+"icons/033_font_up.png"));
            ico034 = getIconImage(getURI(from+"icons/034_font_down.png"));

            ico038 = getIconImage(getURI(from+"icons/038_redo.png"));
            ico039 = getIconImage(getURI(from+"icons/039_undo.png"));
            ico040 = getIconImage(getURI(from+"icons/040_notnice.png"));
            ico041 = getIconImage(getURI(from+"icons/041_print.png"));
            ico042 = getIconImage(getURI(from+"icons/042_copy.png"));
            ico043 = getIconImage(getURI(from+"icons/043_paste.png"));
            ico044 = getIconImage(getURI(from+"icons/044_cut.png"));
            // START KGU#318 2017-01-05: Enh. #319 - new icons for Arranger index
            ico045 = getIconImage(getURI(from+"icons/045_remove.png"));
            ico046 = getIconImage(getURI(from+"icons/046_covered.png"));
            // END KGU#318 2017-01-05
            ico047 = getIconImage(getURI(from+"icons/047_casebefore.png"));
            ico048 = getIconImage(getURI(from+"icons/048_caseafter.png"));
            ico049 = getIconImage(getURI(from+"icons/049_callbefore.png"));
            ico050 = getIconImage(getURI(from+"icons/050_callafter.png"));
        	// START KGU#287 2017-01-11: Issue #81/#330
        	ico051 = getIconImage(getURI(from+"icons/051_scale_gui.png"));
        	// END KGU#287 2017-01-11
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
        	// START KGU#363 2017-03-13: Issue #372
        	ico065 = getIconImage(getURI(from+"icons/065_paragraph.png"));
        	ico066 = getIconImage(getURI(from+"icons/066_litterbin.png"));
        	ico067 = getIconImage(getURI(from+"icons/067_commit.png"));
        	// END KGU#363 2017-03-13
        	// START KGU#365 2017-03-23: Issue #380
        	ico068 = getIconImage(getURI(from+"icons/068_seq2sub.png"));
        	// END KGU#365 2017-03-23
        	// START KGU#373 2017-03-28: Issue #387
        	ico069 = getIconImage(getURI(from+"icons/069_SaveAll.png"));
        	// END KGU#373 2017-03-28
        	// START KGU#318/KGU#376 2017-04-29: Enh. #319, #389
        	ico070 = getIconImage(getURI(from+"icons/070_program_green.png"));
        	ico071 = getIconImage(getURI(from+"icons/071_include.png"));
        	ico072 = getIconImage(getURI(from+"icons/072_include_green.png"));
        	// END KGU#318/KGU#376 2017-04-29
        	// START KGU#324 2017-06-13: Enh. #415
        	ico073 = getIconImage(getURI(from+"icons/073_binoculars.png"));
        	// END KGU#324 2017-06-13
        	ico074 = getIconImage(getURI(from+"icons/074_nsd.png"));
        	// START KGU#456 2017-11-05: Issue #452
            ico075 = getIconImage(getURI(from+"icons/075_beginner.png"));
        	// END KGU#456 2017-11-05
            ico076 = getIconImage(getURI(from+"icons/076_latex.png"));
            ico077 = getIconImage(getURI(from+"icons/077_bubble.png"));
            ico078 = getIconImage(getURI(from+"icons/078_java.png"));
            ico079 = getIconImage(getURI(from+"icons/079_marker.png"));
            ico080 = getIconImage(getURI(from+"icons/080_pulldown.png"));
            ico081 = getIconImage(getURI(from+"icons/081_pen.png"));
            ico082 = getIconImage(getURI(from+"icons/082_din.png"));
            ico083 = getIconImage(getURI(from+"icons/083_loupe.png"));
            ico084 = getIconImage(getURI(from+"icons/084_pencil.png"));
        	// START KGU#477 2017-12-06: Enh. #487
            ico085 = getIconImage(getURI(from+"icons/085_hide_decl.png"));
        	// END KGU#477 2017-12-06
        	// START KGU#486 2018-01-18: Issue #4 (icon redesign)
            ico086 = getIconImage(getURI(from+"icons/086_properties.png"));
            ico087 = getIconImage(getURI(from+"icons/087_code.png"));
            ico088 = getIconImage(getURI(from+"icons/088_picture.png"));
        	// END KGU#486 2018-01-18: Issue #4
            
            // START KGU#287 2016-11-01: Issue #81: Scaling had been forgotten
            ico089 = getIconImage(getURI(from+"icons/089_paraAfter.png"));
            ico090 = getIconImage(getURI(from+"icons/090_paraBefore.png"));
        	ico091 = getIconImage(getURI(from+"icons/091_conv_para.png"));
            // END KGU#287 2016-11-01
            
            ico092 = getIconImage(getURI(from+"icons/092_SaveAs.png"));
            //ico093 = getIconImage(getURI(from+"icons/093_pl.png"));
            //ico094 = getIconImage(getURI(from+"icons/094_tw.png"));

            // START KGU#287 2016-11-02: Issue #81: Scaling had been forgotten
        	ico102 = getIconImage(getURI(from+"icons/102_switch.png"));
            // END KGU#287 2016-11-02            
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

            // START KGU#287 2016-11-01: Issue #81: Scaling had been forgotten
            ico113 = getIconImage(getURI(from+"icons/113_translater.png"));
            // END KGU#287 2016-11-01

        	// START KGU#418 2017-12-11: Enh. #425
        	ico114 = getIconImage(getURI(from+"icons/114_down.png"));
        	ico115 = getIconImage(getURI(from+"icons/115_up.png"));
        	// END KGU#213 2017-12-11

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
        		// Already comprises scaling...
        		ii = getIconImage(getURI(from + "icons/locale_"+localeName+".png"));
        	}
        	return ii;
        }
        // END KGU 2016-09-06

        /**
         * Returns an ImageIcon version of src, which is magnified by length factor this.scaleFactor
         * @param src - the source icon
         * @return the magnified (or diminished) icon
         */
        private static final ImageIcon scale(ImageIcon src)
        {
            //System.out.println(scaleFactor);
            if(scaleFactor>1)
            {
                int w = (int)(scaleFactor*src.getIconWidth());
                int h = (int)(scaleFactor*src.getIconHeight());
                return scaleTo(src, w, h);
            }
            else return src;
        }


        /**
         * Returns an ImageIcon version of src, which is magnified (or diminished to the
         * given width and height
         * @param src - the source icon
         * @param width - the target icon width
         * @param height - the target icon height
         * @return the magnified (or diminished) icon
         */
        public static final ImageIcon scaleTo(ImageIcon src, int width, int height)
        {
            //System.out.println(scaleFactor);
        	int type = BufferedImage.TYPE_INT_ARGB;
        	BufferedImage dst = new BufferedImage(width, height, type);
        	Graphics2D g2 = dst.createGraphics();
        	g2.drawImage(src.getImage(), 0, 0, width, height, null);
        	g2.dispose();
        	return new ImageIcon(dst);
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

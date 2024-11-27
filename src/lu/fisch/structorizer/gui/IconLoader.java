/*
    Structorizer
    A little tool which you can use to create Nassi-Shneiderman Diagrams (NSD)

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
 *      Bob Fisch       2007-12-28      First Issue
 *      Kay Gürtzig     2015-10-12      New icons (103_breakpt, 104_nobreakpt) introduced
 *      Kay Gürtzig     2015-11-19      New Arranger icon (icoArr) introduced
 *      Kay Gürtzig     2016-01-03/04   Enh. #87 (=KGU#123) Basic icons for NSD element types and
 *                                      element collapsing introduced
 *      Kay Gürtzig     2016-07-06      Enh. #188: New icon 109_wand introduced for element conversion
 *      Kay Gürtzig     2016-07-22      Enh. #199: New icon 110_help introduced for online user guide activation
 *      Kay Gürtzig     2016-07-31      Enh. #128: New icon 111_c_plus_t introduced for "commments plus text" mode
 *      Kay Gürtzig     2016-08-02      Enh. #215: New icon 112_stopwatch introduced for breapoint count triggers
 *      Kay Gürtzig     2016-09-05      Generic support for locale icons
 *      Kay Gürtzig     2016-09-25      Enh. #253: New icon 025_import for import configuration
 *      Kay Gürtzig     2016-10-13      Enh. #270: New icon 026_disable for inactive elements
 *      Kay Gürtzig     2016-10-16      Enh. #272: New icons 027_richTurtle and 028_poorTurtle
 *      Kay Gürtzig     2016-11-01      Issue #81: icons 089 through 091 and 113 hadn't been scaled
 *      Kay Gürtzig     2016-12-14      Enh. #305: New icon 029_index for the Arranger index 
 *      Kay Gürtzig     2017-01-05      Enh. #319: New icons 045_remove, 046_covered for Arranger index popup
 *      Kay Gürtzig     2017-01-07      Enh. #319: New icon 030_function_green for Arranger index
 *      Kay Gürtzig     2017-01-11      Enh. #81: New icon 051_scale_gui for scaling preset
 *      Kay Gürtzig     2017-03-13      Enh. #372: New icons 065_paragraph through 067_commit for license editing
 *      Kay Gürtzig     2017-03-23      Enh. #380: Icon 068 for the conversion of a sequence to a subroutine
 *      Kay Gürtzig     2017-03-28      Enh. #387: Icon 069 for "Save All" added.
 *      Kay Gürtzig     2017-04-29      Enh. #319/#389: New icon 070_program_green for Arranger index
 *      Kay Gürtzig     2017-05-16      Enh. #389: New icons 071_include, 072_include_green
 *      Kay Gürtzig     2017-06-13      Enh. #415: New icon 073_binoculars for Find & Replace dialog
 *      Kay Gürtzig     2017-10-16      Enh. #439: New icons 080_pulldown and 084_pencil introduced
 *      Kay Gürtzig     2017-12-06      Enh. #487: New icon 085_hide_decl for hiding declarations
 *      Kay Gürtzig     2017-12-11      Enh. #425: New icons 114_down and 115_up for Translator
 *      Kay Gürtzig     2018-01-04      New icon 092_SaveAs
 *      Kay Gürtzig     2018-01-18      Issue #4: New icons 032_export, 086_properties, 087_code, 088_picture
 *      Kay Gürtzig     2018-01-25      Issue #4: Enumerable icon fields converted into an array (ico011 --> getIcon(11))
 *      Kay Gürtzig     2018-02-06      Issue #4: Extra factor in getIconImage() for e.g. Arranger icons
 *      Kay Gürtzig     2018-02-09      Issue #4: Some icon renaming and reorganisation
 *      Kay Gürtzig     2018-02-12      Issue #4: Distinct set of FOR loop icons (no longer = forever/while)
 *      Kay Gürtzig     2018-02-14      Issue #510: Colour button icon shape changed from rectangle to circle.
 *      Kay Gürtzig     2018-06-28      Smaller element symbols for Search + Replace (###_mini_???.png)
 *      Kay Gürtzig     2018-07-17      Issue #561: New icon 055_sigma (sum symbol) added
 *      Kay Gürtzig     2018-09-18      Issue #601: More safety on icon request (dummy icon, log).
 *      Kay Gürtzig     2018-10-27      Enh. #619: New icon 056_wordwrap for line breaking menu item
 *      Kay Gürtzig     2018-12-27      Enh. #657: New icons 089_keyboard, 090_layers for Arranger popup menu added
 *      Kay Gürtzig     2019-01-01      Enh. #657: New icons 094_group through 098_groupDetach added
 *      Kay Gürtzig     2019-01-03      Enh. #657: New icons 116_groupAttach, 117_groupExpand added
 *      Kay Gürtzig     2019-01-10      Enh. #657, #662/2: variant of generateIcon(Color) with insets
 *      Kay Gürtzig     2019-01-12      Enh. #622/3: 119_rearrange added
 *      Kay Gürtzig     2020-01-20      Enh. #801: 123_help_book added
 *      Kay Gürtzig     2020-04-02      Issue #4 deprecated static field turtle removed, set the from field deprecated.
 *      Kay Gürtzig     2020-10-17      Enh. #872: 124_prog_c added
 *      Kay Gürtzig     2021-01-09/11   Enh. #910: 125_plug added, deprecated field `from' removed
 *      Kay Gürtzig     2021-01-13      126_info added to replace 017 in some places.
 *      Kay Gürtzig     2021-02-06      Enh. #915: 127_merge and 128_split added
 *      Kay Gürtzig     2021-03-18      Issue #966: Icon 081 (language) replaced
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.awt.*;
import java.awt.image.*;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

import lu.fisch.structorizer.locales.Locales;

/**
 * Class responsible for loading and scaling of all application icons.
 * 
 * @author Robert Fisch
 */
public class IconLoader {

// START KGU 2021-01-09 Eventually disabled
//	@Deprecated
//	private static String from = new String("");
// END KGU 2021-01-09

	protected static double scaleFactor = 1;
	
	// START KGU#486 2018-01-24: Issue #4 - new icon file retrieval mechanism
	private static final String[] ICON_FILES = {
			"000_structorizer.png",	// former 074_nsd.png, structorizer.png, and structorizer48.png
			"001_New.png",
			"002_Open.png",
			"003_Save.png",
			"004_Make.png",
			"005_Delete.png",
			"006_edit.png",	// Renamed
			"007_zoom_out.png",
			"008_zoom_in.png",
			"009_mouse.png",	// (#519)
			"010_mini_inst.png",	// ex "010_whileBefore.png"
			"011_mini_call.png",	// ex "011_repeatBefore.png"
			"012_mini_jump.png",	// ex "012_intAfter.png"
			"013_mini_if.png",		// ex "013_altAfter.png"
			"014_mini_forever.png",	// ex "014_foreverAfter.png"
			"015_mini_while.png",	// ex "015_whileAfter.png"
			"016_mini_repeat.png",	// ex "016_repeatAfter.png"
			"017_Eye.png",
			"018_add.png",
			"019_Up.png",
			"020_Down.png",
			"021_function.png",
			"022_program.png",
			"023_font.png",
			"024_smiley.png",	// larger than usual!
			"025_import.png",
			"026_disable.png",
			"027_richTurtle.png",
			"028_poorTurtle.png",
			"029_index.png",
			"030_function_green.png",
			"031_palette.png",
			"032_export.png",
			"033_font_up.png",
			"034_font_down.png",
			"035_realtime.png",	// not used
			"036_textfile.png",	// not used
			"037_text.png",		// not used
			"038_redo.png",
			"039_undo.png",
			"040_notnice.png",
			"041_print.png",	// or use "041_printer.png" alternatively
			"042_copy.png",
			"043_paste.png",
			"044_cut.png",
			"045_remove.png",	// used in Arranger and Arranger index
			"046_covered.png",	// for Arranger toolbar
			"047_mini_case.png",	// ex "047_casebefore.png"
			"048_mini_para.png",	// ex "048_caseafter.png",
			"049_mini_for_din.png",	// ex "049_callbefore.png"
			"050_mini_for.png",		// ex "050_callafter.png"
			"051_scale_gui.png",
			"052_update.png",
			"053_elem_for.png",	// new
			"054_tortoise.png",
			"055_sigma.png",	//"055_jumpafter.png",	// obsolete (#510)
			"056_wordwrap.png",	//"056_jumpbefore.png",	// obsolete (#510)
			"057_elem_inst.png",
			"058_elem_call.png",
			"059_elem_jump.png",
			"060_elem_if.png",
			"061_elem_forever.png",
			"062_elem_while.png",
			"063_elem_repeat.png",
			"064_elem_case.png",
			"065_paragraph.png",	// not used
			"066_litterbin.png",	// not used
			"067_commit.png",		// not used
			"068_seq2sub.png",
			"069_SaveAll.png",
			"070_program_green.png",
			"071_include.png",
			"072_include_green.png",
			"073_binoculars.png",
			"074_elem_for_din.png",	// new, replaced "074_nsd.png" --> "000_structorizer.png"
			"075_beginner.png",
			"076_latex.png",	// for StrukTeX?
			"077_bubble.png",
			"078_java.png",
			"079_marker.png",
			"080_pulldown.png",	// for value presenter
			"081_earth.png",	// 2021-03-18 #966: replaces "081_pen.png",
			"082_din.png",
			"083_loupe.png",	// Analyser
			"084_pencil.png",	// for Value Presenter
			"085_hide_decl.png",
			"086_properties.png",
			"087_code.png",		// or use "087_code1.png" alternatively
			"088_picture.png",	// export / import
			"089_keyboard.png",	//"089_paraAfter.png",	// obsolete (#510)
			"090_layers.png",	//"090_paraBefore.png",	// obsolete (#510)
			"091_elem_para.png",
			"092_SaveAs.png",
			"093_picture_export.png",	// for Arranger toolbar
			"094_group.png",	//ex "094_forBefore.png",		// obsolete (#510)
			"095_groupList.png",	//ex "095_for_dinBefore.png",	// obsolete (#510)
			"096_groupArchive.png",	//ex "096_forAfter.png",			// obsolete (#510)
			"097_groupDissolve.png",	//"097_for_dinAfter.png",		// obsolete (#510)
			"098_groupDetach.png",
			"099_pin_blue.png",	// for Arranger toolbar
			"100_diagram_drop.png", // for Arranger toolbar
			"101_diagram_new.png",	// for Arranger toolbar and Arranger index
			"102_switch.png",
			"103_breakpt.png",
			"104_nobreakpt.png",
			"105_arranger.png",
			"106_collapse.png",
			"107_expand.png",
			"108_collapse_by_wheel.png",
			"109_wand.png",
			"110_help.png",
			"111_c_plus_t.png",
			"112_stopwatch.png",
			"113_translator.png",
			"114_down.png",	// for Translator Find dialog
			"115_up.png",	// for Translator Find dialog
			"116_groupAttach.png",
			"117_groupExpand.png",
			"118_info.png",
			"119_rearrange.png",
			"120_elem_try.png",		// introduced with enh. #56
			"121_mini_try.png",		// introduced with enh. #56
			"122_exit.png",			// Introduced with 3.29-13
			"123_help_book.png",	// introduced with 3.30-05 for #801
			"124_prog_c.png",		// introduced with 3.30-11 for #872
			"125_plug.png",			// introduced with 3.30-14 for #910
			"126_info.png",			// is to replace the 017_Eye.png in the main toolbox
			"127_merge.png",		// introduced with 3.30-16 for #915
			"128_split.png"			// introduced with 3.30-16 for #915
			//"129_gnu.png"			// intended for #967 (ARM code export), but not used
	};
	
	/** Array of supported icon sizes (in pixel) */
	private static final int[] ICON_SIZES = {
			16,
			24,
			32,
			48,
			64
			// The sizes 128 and 256 are only for design purposes, they would unnecessarily inflate the JAR
	};
	
	/** Array of available numbered icons with lazy initialization (replaces ico001, ico002 etc.) */
	private static ImageIcon[] icons = null;
	// END KGU#486 2018-01-24

	// Icons
	/** A fixed-size product image for Mac or Translator */
	// START KGU#577 2018-09-18: Issue #601
	//public static ImageIcon icoNSD = new ImageIcon(getURI(from+"icons/structorizer.png"));
	public static ImageIcon icoNSD = getIconImage(getURI("icons/structorizer.png"), true);
	// END KGU#577 2018-09-18
	// START KGU#287 2016-11-02: Issue #81 (DPI awareness workaround)
	//public static ImageIcon icoNSD48 = new ImageIcon(getURI(from+"icons/structorizer48.png"));
	// START KGU#486 2018-02-06: Issue #4 (Icon redesign)
	//public static ImageIcon icoNSD48 = getIconImage(getURI(from+"icons/structorizer48.png"));
	/** A scaled product image with basic size of 48 pixels */
	public static ImageIcon icoNSD48 = getIconImage(getURI("icons_48/000_structorizer.png"));
	// END KGU#486 2018-02-06
	// END KGU#287 2016-11-02
	// START KGU#577 2018-09-17: Issue #601 - we use lazy initialization
	private static ImageIcon dummyIcon = null;
	// END KGU#577 2018-09-17
		
//	public static ImageIcon ico001 = getIconImage(getURI(from+"icons/001_New.png"));
//	public static ImageIcon ico002 = getIconImage(getURI(from+"icons/002_Open.png"));
//	public static ImageIcon ico003 = getIconImage(getURI(from+"icons/003_Save.png"));
//	public static ImageIcon ico004 = getIconImage(getURI(from+"icons/004_Make.png"));
//	public static ImageIcon ico005 = getIconImage(getURI(from+"icons/005_Delete.png"));
//	public static ImageIcon ico006 = getIconImage(getURI(from+"icons/006_update.png"));
//	public static ImageIcon ico007 = getIconImage(getURI(from+"icons/007_intBefore.png"));
//	public static ImageIcon ico008 = getIconImage(getURI(from+"icons/008_altBefore.png"));
//	public static ImageIcon ico009 = getIconImage(getURI(from+"icons/009_forBefore.png"));
//	public static ImageIcon ico010 = getIconImage(getURI(from+"icons/010_whileBefore.png"));
//	public static ImageIcon ico011 = getIconImage(getURI(from+"icons/011_repeatBefore.png"));
//	public static ImageIcon ico012 = getIconImage(getURI(from+"icons/012_intAfter.png"));
//	public static ImageIcon ico013 = getIconImage(getURI(from+"icons/013_altAfter.png"));
//	public static ImageIcon ico014 = getIconImage(getURI(from+"icons/014_forAfter.png"));
//	public static ImageIcon ico015 = getIconImage(getURI(from+"icons/015_whileAfter.png"));
//	public static ImageIcon ico016 = getIconImage(getURI(from+"icons/016_repeatAfter.png"));
//	public static ImageIcon ico017 = getIconImage(getURI(from+"icons/017_Eye.png"));
//	public static ImageIcon ico018 = getIconImage(getURI(from+"icons/018_add.png"));
//	public static ImageIcon ico019 = getIconImage(getURI(from+"icons/019_Up.png"));
//	public static ImageIcon ico020 = getIconImage(getURI(from+"icons/020_Down.png"));
//	public static ImageIcon ico021 = getIconImage(getURI(from+"icons/021_function.png"));
//	public static ImageIcon ico022 = getIconImage(getURI(from+"icons/022_program.png"));
//	public static ImageIcon ico023 = getIconImage(getURI(from+"icons/023_font.png"));
//	// START KGU#459 2017-11-19: Issue #459
//	public static ImageIcon ico024 = getIconImage(getURI(from+"icons/024_smiley.png"));
//	// END KGU#459 2017-11-19
//	
//	// START KGU#258 2016-09-25: Enh. #253
//	public static ImageIcon ico025 = getIconImage(getURI(from+"icons/025_import.png"));
//	// END KGU#258 2016-09-25
//	// START KGU#277 2016-10-13: Enh. #270
//	public static ImageIcon ico026 = getIconImage(getURI(from+"icons/026_disable.png"));
//	// END KGU#277 2016-10-13
//	// START KGU#282 2016-10-16: Enh. #272
//	public static ImageIcon ico027 = getIconImage(getURI(from+"icons/027_richTurtle.png"));
//	public static ImageIcon ico028 = getIconImage(getURI(from+"icons/028_poorTurtle.png"));
//	// END KGU#282 2016-10-16
//	// START KGU#305 2016-12-14: Enh. #305
//	public static ImageIcon ico029 = getIconImage(getURI(from+"icons/029_index.png"));
//	// END KGU#305 2016-12-14
//	// START KGU#318 2017-01-07: Enh. #319
//	public static ImageIcon ico030 = getIconImage(getURI(from+"icons/030_function_green.png"));
//	// END KGU#318 2017-01-07
//
//	public static ImageIcon ico031 = getIconImage(getURI(from+"icons/031_make_copy.png"));
//	// START KGU#486 2018-01-18: Issue #4 (icon redesign)
//	//public static ImageIcon ico032 = getIconImage(getURI(from+"icons/032_make_bmp.png"));
//	public static ImageIcon ico032 = getIconImage(getURI(from+"icons/032_export.png"));
//	// END KGU#486 2018-01-18
//	public static ImageIcon ico033 = getIconImage(getURI(from+"icons/033_font_up.png"));
//	public static ImageIcon ico034 = getIconImage(getURI(from+"icons/034_font_down.png"));
//	
//	public static ImageIcon ico038 = getIconImage(getURI(from+"icons/038_redo.png"));
//	public static ImageIcon ico039 = getIconImage(getURI(from+"icons/039_undo.png"));
//	public static ImageIcon ico040 = getIconImage(getURI(from+"icons/040_notnice.png"));
//	public static ImageIcon ico041 = getIconImage(getURI(from+"icons/041_print.png"));
//	public static ImageIcon ico042 = getIconImage(getURI(from+"icons/042_copy.png"));
//	public static ImageIcon ico043 = getIconImage(getURI(from+"icons/043_paste.png"));
//	public static ImageIcon ico044 = getIconImage(getURI(from+"icons/044_cut.png"));
//	// START KGU#318 2017-01-05: Enh. #319 - new icon for Aranger index
//	public static ImageIcon ico045 = getIconImage(getURI(from+"icons/045_remove.png"));
//	public static ImageIcon ico046 = getIconImage(getURI(from+"icons/046_covered.png"));
//	// END KGU#318 2017-01-05
//	public static ImageIcon ico047 = getIconImage(getURI(from+"icons/047_casebefore.png"));
//	public static ImageIcon ico048 = getIconImage(getURI(from+"icons/048_caseafter.png"));
//	public static ImageIcon ico049 = getIconImage(getURI(from+"icons/049_callbefore.png"));
//	public static ImageIcon ico050 = getIconImage(getURI(from+"icons/050_callafter.png"));
//	// START KGU#287 2017-01-11: Issue #81/#330
//	public static ImageIcon ico051 = getIconImage(getURI(from+"icons/051_scale_gui.png"));
//	// END KGU#287 2017-01-11
//	public static ImageIcon ico052 = getIconImage(getURI(from+"icons/052_update.png"));
//	
//	public static ImageIcon ico055 = getIconImage(getURI(from+"icons/055_jumpafter.png"));
//	public static ImageIcon ico056 = getIconImage(getURI(from+"icons/056_jumpbefore.png"));
//	
//	// START KGU#122 2016-01-03: Enhancement for collapsed elements
//	public static ImageIcon ico057 = getIconImage(getURI(from+"icons/057_conv_inst.png"));
//	public static ImageIcon ico058 = getIconImage(getURI(from+"icons/058_conv_call.png"));
//	public static ImageIcon ico059 = getIconImage(getURI(from+"icons/059_conv_jump.png"));
//	public static ImageIcon ico060 = getIconImage(getURI(from+"icons/060_conv_if.png"));
//	public static ImageIcon ico061 = getIconImage(getURI(from+"icons/061_conv_for.png"));
//	public static ImageIcon ico062 = getIconImage(getURI(from+"icons/062_conv_while.png"));
//	public static ImageIcon ico063 = getIconImage(getURI(from+"icons/063_conv_repeat.png"));
//	public static ImageIcon ico064 = getIconImage(getURI(from+"icons/064_conv_case.png"));
//	// END KGU#122 2016-01-03
//	// START KGU#363 2017-03-13: Issue #372
//	public static ImageIcon ico065 = getIconImage(getURI(from+"icons/065_paragraph.png"));
//	public static ImageIcon ico066 = getIconImage(getURI(from+"icons/066_litterbin.png"));
//	public static ImageIcon ico067 = getIconImage(getURI(from+"icons/067_commit.png"));
//	// END KGU#363 2017-03-13
//	// START KGU#365 2017-03-23: Issue #380
//	public static ImageIcon ico068 = getIconImage(getURI(from+"icons/068_seq2sub.png"));
//	// END KGU#365 2017-03-23
//	// START KGU#373 2017-03-28: Issue #387
//	public static ImageIcon ico069 = getIconImage(getURI(from+"icons/069_SaveAll.png"));
//	// END KGU#373 2017-03-28
//	// START KGU#318/KGU#376 2017-04-29: Enh. #319, #389
//	public static ImageIcon ico070 = getIconImage(getURI(from+"icons/070_program_green.png"));
//	public static ImageIcon ico071 = getIconImage(getURI(from+"icons/071_include.png"));
//	public static ImageIcon ico072 = getIconImage(getURI(from+"icons/072_include_green.png"));
//	// END KGU#318/KGU#376 2017-04-29
//	// START KGU#324 2017-06-13: Enh. #415
//	public static ImageIcon ico073 = getIconImage(getURI(from+"icons/073_binoculars.png"));
//	// END KGU#324 2017-06-13
//	
//	public static ImageIcon ico074 = getIconImage(getURI(from+"icons/074_nsd.png"));
//	// START KGU#456 2017-11-05: Issue #452
//	public static ImageIcon ico075 = getIconImage(getURI(from+"icons/075_beginner.png"));
//	// END KGU#456 2017-11-05
//	public static ImageIcon ico076 = getIconImage(getURI(from+"icons/076_latex.png"));
//	public static ImageIcon ico077 = getIconImage(getURI(from+"icons/077_bubble.png"));
//	public static ImageIcon ico078 = getIconImage(getURI(from+"icons/078_java.png"));
//	public static ImageIcon ico079 = getIconImage(getURI(from+"icons/079_marker.png"));
//	public static ImageIcon ico080 = getIconImage(getURI(from+"icons/080_pulldown.png"));
//	public static ImageIcon ico081 = getIconImage(getURI(from+"icons/081_pen.png"));
//	public static ImageIcon ico082 = getIconImage(getURI(from+"icons/082_din.png"));
//	public static ImageIcon ico083 = getIconImage(getURI(from+"icons/083_loupe.png"));
//	public static ImageIcon ico084 = getIconImage(getURI(from+"icons/084_pencil.png"));
//	// START KGU#477 2017-12-06: Enh. #487
//	public static ImageIcon ico085 = getIconImage(getURI(from+"icons/085_hide_decl.png"));
//	// END KGU#477 2017-12-06
//	// START KGU#486 2018-01-18: Issue #4 (icon redesign)
//	public static ImageIcon ico086 = getIconImage(getURI(from+"icons/086_properties.png"));
//	public static ImageIcon ico087 = getIconImage(getURI(from+"icons/087_code.png"));
//	public static ImageIcon ico088 = getIconImage(getURI(from+"icons/088_picture.png"));
//	// END KGU#486 2018-01-18
//
//	public static ImageIcon ico089 = getIconImage(getURI(from+"icons/089_paraAfter.png"));
//	public static ImageIcon ico090 = getIconImage(getURI(from+"icons/090_paraBefore.png"));
//	public static ImageIcon ico091 = getIconImage(getURI(from+"icons/091_conv_para.png"));
//	public static ImageIcon ico092 = getIconImage(getURI(from+"icons/092_SaveAs.png"));
//	//public static ImageIcon ico093 = getIconImage(getURI(from+"icons/093_pl.png"));
//	//public static ImageIcon ico094 = getIconImage(getURI(from+"icons/094_tw.png"));
//
//	public static ImageIcon ico102 = getIconImage(getURI(from+"icons/102_switch.png"));
//	
//	// START KGU 2015-10-12: New checkpoint icon
//	public static ImageIcon ico103 = getIconImage(getURI(from+"icons/103_breakpt.png"));
//	public static ImageIcon ico104 = getIconImage(getURI(from+"icons/104_nobreakpt.png"));
//	// END KGU 2015-10-12
//	// START KGU#2 2015-11-19: Arranger icon
//	public static ImageIcon ico105 = getIconImage(getURI(from+"icons/105_arranger.png"));
//	// END KGU 2015-10-12
//	// START KGU#123 2016-01-03/04: Enh. #87
//	public static ImageIcon ico106 = getIconImage(getURI(from+"icons/106_collapse.png"));
//	public static ImageIcon ico107 = getIconImage(getURI(from+"icons/107_expand.png"));
//	public static ImageIcon ico108 = getIconImage(getURI(from+"icons/108_collapse_by_wheel.png"));
//	// END KGU#123 2016-01-03/04
//	// START KGU#199 2016-07-06: Enh. #188
//	public static ImageIcon ico109 = getIconImage(getURI(from+"icons/109_wand.png"));
//	// END KGU#199 2016-07-06
//	// START KGU#208 2016-07-22: Enh. #199 - direct online user guide activation
//	public static ImageIcon ico110 = getIconImage(getURI(from+"icons/110_help.png"));
//	// END KGU#208 2016-07-22
//	// START KGU#227 2016-07-31: Enh. #128 - comments plus text display
//	public static ImageIcon ico111 = getIconImage(getURI(from+"icons/111_c_plus_t.png"));
//	// END KGU#227 2016-07-31
//	// START KGU#213 2016-08-02: Enh. #215 - breakpoint counting trigger
//	public static ImageIcon ico112 = getIconImage(getURI(from+"icons/112_stopwatch.png"));
//	public static ImageIcon ico113 = getIconImage(getURI(from+"icons/113_translater.png"));
//	// END KGU#213 2016-08-02
//	// START KGU#418 2017-12-11: Enh. #425
//	public static ImageIcon ico114 = getIconImage(getURI(from+"icons/114_down.png"));
//	public static ImageIcon ico115 = getIconImage(getURI(from+"icons/115_up.png"));
//	// END KGU#213 2017-12-11

	// START KGU 2020-04-02: Eventually removed
//	/**
//	 * This still holds the obsolete traditional turtle icon.
//	 * Use {@link #getIcon(int)} with argument 54 to obtain the topical tortoise. 
//	 */
//	@Deprecated 
//	public static ImageIcon turtle = getIconImage(getURI(from+"icons/turtle.png"));
	// END KGU 2020-04-02
	
	// START KGU#242 2016-09-05
	/** Cached locale icons */
	private static HashMap<String, ImageIcon> icoLocales = new HashMap<String, ImageIcon>();
	// END KGU#242 2016-09-05
	
	// START KGU#486 2018-01-25: Issues #4, #81
	/**
	 * New preferred icon retrieval mechanism to support qualitatively acceptable icon scaling
	 * results and to facilitate the introduction of new icons (though it's going to get harder
	 * to identify unused icons.)<br/>
	 * The first call will cause the initialization of the icon cache.
	 * 
	 * @param iconNo - the index of he requested icon
	 * @return the {@linkImageIcon} object for the requested icon if available, {@code null}
	 *    otherwise
	 * 
	 * @see #getIconImage(java.net.URL)
	 * @see #getIconImage(String, double)
	 * @see #setScaleFactor(double)
	 */
	public static ImageIcon getIcon(int iconNo)
	{
		// START KGU#577 2018-09-17: Issue #601
		//if (iconNo < 0 || iconNo >= ICON_FILES.length) {
		//	return null;
		//}
		ImageIcon icon = null;
		if (iconNo >= 0 && iconNo < ICON_FILES.length) {
		// END KGU#577 2018-09-17
			if (icons == null) {
				// Lazy initialization of the icon cache
				icons = new ImageIcon[ICON_FILES.length];
				for (int i = 0; i < ICON_FILES.length; i++) {
					String fileName = ICON_FILES[i];
					if (fileName != null) {
						icons[i] = getIconImage(fileName);
					}
					else {
						icons[i] = null;
					}
				}
			}
			icon = icons[iconNo];
		// START KGU#577 2018-09-17: Issue #601
		}
		else {
			try {
				// Force a stacktrace into the log file
				throw new Exception("Invalid icon number " + iconNo);
			}
			catch (Exception ex) {
				Logger.getLogger(IconLoader.class.getName()).log(Level.SEVERE, "Resources inconsistent", ex);
			}
		}
		if (icon == null) {
			icon = getMissingIcon();
		}
		return icon;
	}
	// END KGU#486 2018-01-25

	/**
	 * Stores the given scale factor, clears the general icon cache (such that
	 * icon retrieval refills it by lazy initialization with the new scale),
	 * and immediately rescales all separately cached icons (e.g. product icon,
	 * locale icons).
	 * 
	 * @param scale - the new scale factor.
	 */
	public static void setScaleFactor(double scale)
	{
		// START KGU#577 2018-09-18: Issue #601 replace the scaled dummy
		if (scale != scaleFactor) {
			dummyIcon = null;	// Is going to be reproduced as soon as needed
		}
		// END KGU#577 2018-09-18
		// FIXME: Consider GUIScaler.getScreenScale() and possibly some initial system font height?
		scaleFactor = scale;
		// START KGU#287 2016-11-02: Issue #81 (DPI awareness workaround)
		// START KGU#486 2018-02-06: Issue #4 (icon redesign)
		//icoNSD48 = getIconImage(getURI(from+"icons/structorizer48.png"));
		icoNSD48 = getIconImage(getURI("icons_48/000_structorizer.png"));
		// END KGU#486 2018-02-06
		// END KGU#287 2016-11-02

//		ico001 = getIconImage(getURI(from+"icons/001_New.png"));
//		ico002 = getIconImage(getURI(from+"icons/002_Open.png"));
//		ico003 = getIconImage(getURI(from+"icons/003_Save.png"));
//		ico004 = getIconImage(getURI(from+"icons/004_Make.png"));
//		ico005 = getIconImage(getURI(from+"icons/005_Delete.png"));
//		ico006 = getIconImage(getURI(from+"icons/006_update.png"));
//		ico007 = getIconImage(getURI(from+"icons/007_intBefore.png"));
//		ico008 = getIconImage(getURI(from+"icons/008_altBefore.png"));
//		ico009 = getIconImage(getURI(from+"icons/009_forBefore.png"));
//		ico010 = getIconImage(getURI(from+"icons/010_whileBefore.png"));
//		ico011 = getIconImage(getURI(from+"icons/011_repeatBefore.png"));
//		ico012 = getIconImage(getURI(from+"icons/012_intAfter.png"));
//		ico013 = getIconImage(getURI(from+"icons/013_altAfter.png"));
//		ico014 = getIconImage(getURI(from+"icons/014_forAfter.png"));
//		ico015 = getIconImage(getURI(from+"icons/015_whileAfter.png"));
//		ico016 = getIconImage(getURI(from+"icons/016_repeatAfter.png"));
//		ico017 = getIconImage(getURI(from+"icons/017_Eye.png"));
//		ico018 = getIconImage(getURI(from+"icons/018_add.png"));
//		ico019 = getIconImage(getURI(from+"icons/019_Up.png"));
//		ico020 = getIconImage(getURI(from+"icons/020_Down.png"));
//		ico021 = getIconImage(getURI(from+"icons/021_function.png"));
//		ico022 = getIconImage(getURI(from+"icons/022_program.png"));
//		ico023 = getIconImage(getURI(from+"icons/023_font.png"));
//		// START KGU#459 2017-11-19: Issue #459
//		ico024 = getIconImage(getURI(from+"icons/024_smiley.png"));
//		// END KGU#459 2017-11-19
//
//		// START KGU#258 2016-09-25: Enh. #253
//		ico025 = getIconImage(getURI(from+"icons/025_import.png"));
//		// END KGU#258 2016-09-25
//		// START KGU#277 2016-10-13: Enh. #270
//		ico026 = getIconImage(getURI(from+"icons/026_disable.png"));
//		// END KGU#277 2016-10-13
//		// START KGU#282 2016-10-16: Enh. #272
//		ico027 = getIconImage(getURI(from+"icons/027_richTurtle.png"));
//		ico028 = getIconImage(getURI(from+"icons/028_poorTurtle.png"));
//		// END KGU#282 2016-10-16
//		// START KGU#305 2016-12-14: Enh. #305
//		ico029 = getIconImage(getURI(from+"icons/029_index.png"));
//		// END KGU#305 2016-12-14
//		// START KGU#318 2017-01-07: Enh. #319
//		ico030 = getIconImage(getURI(from+"icons/030_function_green.png"));
//		// END KGU#318 2017-01-07
//
//		ico031 = getIconImage(getURI(from+"icons/031_make_copy.png"));
//		// START KGU#486 2018-01-18: Issue #4 (icon redesign)
//		//ico032 = getIconImage(getURI(from+"icons/032_make_bmp.png"));
//		ico032 = getIconImage(getURI(from+"icons/032_export.png"));
//		// END KGU#486 2018-01-18
//		ico033 = getIconImage(getURI(from+"icons/033_font_up.png"));
//		ico034 = getIconImage(getURI(from+"icons/034_font_down.png"));
//
//		ico038 = getIconImage(getURI(from+"icons/038_redo.png"));
//		ico039 = getIconImage(getURI(from+"icons/039_undo.png"));
//		ico040 = getIconImage(getURI(from+"icons/040_notnice.png"));
//		ico041 = getIconImage(getURI(from+"icons/041_print.png"));
//		ico042 = getIconImage(getURI(from+"icons/042_copy.png"));
//		ico043 = getIconImage(getURI(from+"icons/043_paste.png"));
//		ico044 = getIconImage(getURI(from+"icons/044_cut.png"));
//		// START KGU#318 2017-01-05: Enh. #319 - new icons for Arranger index
//		ico045 = getIconImage(getURI(from+"icons/045_remove.png"));
//		ico046 = getIconImage(getURI(from+"icons/046_covered.png"));
//		// END KGU#318 2017-01-05
//		ico047 = getIconImage(getURI(from+"icons/047_casebefore.png"));
//		ico048 = getIconImage(getURI(from+"icons/048_caseafter.png"));
//		ico049 = getIconImage(getURI(from+"icons/049_callbefore.png"));
//		ico050 = getIconImage(getURI(from+"icons/050_callafter.png"));
//		// START KGU#287 2017-01-11: Issue #81/#330
//		ico051 = getIconImage(getURI(from+"icons/051_scale_gui.png"));
//		// END KGU#287 2017-01-11
//		ico052 = getIconImage(getURI(from+"icons/052_update.png"));
//
//		ico055 = getIconImage(getURI(from+"icons/055_jumpafter.png"));
//		ico056 = getIconImage(getURI(from+"icons/056_jumpbefore.png"));
//
//		// START KGU#122 2016-01-03: Enhancement for collapsed elements
//		ico057 = getIconImage(getURI(from+"icons/057_conv_inst.png"));
//		ico058 = getIconImage(getURI(from+"icons/058_conv_call.png"));
//		ico059 = getIconImage(getURI(from+"icons/059_conv_jump.png"));
//		ico060 = getIconImage(getURI(from+"icons/060_conv_if.png"));
//		ico061 = getIconImage(getURI(from+"icons/061_conv_for.png"));
//		ico062 = getIconImage(getURI(from+"icons/062_conv_while.png"));
//		ico063 = getIconImage(getURI(from+"icons/063_conv_repeat.png"));
//		ico064 = getIconImage(getURI(from+"icons/064_conv_case.png"));
//		// END KGU#122 2016-01-03
//		// START KGU#363 2017-03-13: Issue #372
//		ico065 = getIconImage(getURI(from+"icons/065_paragraph.png"));
//		ico066 = getIconImage(getURI(from+"icons/066_litterbin.png"));
//		ico067 = getIconImage(getURI(from+"icons/067_commit.png"));
//		// END KGU#363 2017-03-13
//		// START KGU#365 2017-03-23: Issue #380
//		ico068 = getIconImage(getURI(from+"icons/068_seq2sub.png"));
//		// END KGU#365 2017-03-23
//		// START KGU#373 2017-03-28: Issue #387
//		ico069 = getIconImage(getURI(from+"icons/069_SaveAll.png"));
//		// END KGU#373 2017-03-28
//		// START KGU#318/KGU#376 2017-04-29: Enh. #319, #389
//		ico070 = getIconImage(getURI(from+"icons/070_program_green.png"));
//		ico071 = getIconImage(getURI(from+"icons/071_include.png"));
//		ico072 = getIconImage(getURI(from+"icons/072_include_green.png"));
//		// END KGU#318/KGU#376 2017-04-29
//		// START KGU#324 2017-06-13: Enh. #415
//		ico073 = getIconImage(getURI(from+"icons/073_binoculars.png"));
//		// END KGU#324 2017-06-13
//		ico074 = getIconImage(getURI(from+"icons/074_nsd.png"));
//		// START KGU#456 2017-11-05: Issue #452
//		ico075 = getIconImage(getURI(from+"icons/075_beginner.png"));
//		// END KGU#456 2017-11-05
//		ico076 = getIconImage(getURI(from+"icons/076_latex.png"));
//		ico077 = getIconImage(getURI(from+"icons/077_bubble.png"));
//		ico078 = getIconImage(getURI(from+"icons/078_java.png"));
//		ico079 = getIconImage(getURI(from+"icons/079_marker.png"));
//		ico080 = getIconImage(getURI(from+"icons/080_pulldown.png"));
//		ico081 = getIconImage(getURI(from+"icons/081_pen.png"));
//		ico082 = getIconImage(getURI(from+"icons/082_din.png"));
//		ico083 = getIconImage(getURI(from+"icons/083_loupe.png"));
//		ico084 = getIconImage(getURI(from+"icons/084_pencil.png"));
//		// START KGU#477 2017-12-06: Enh. #487
//		ico085 = getIconImage(getURI(from+"icons/085_hide_decl.png"));
//		// END KGU#477 2017-12-06
//		// START KGU#486 2018-01-18: Issue #4 (icon redesign)
//		ico086 = getIconImage(getURI(from+"icons/086_properties.png"));
//		ico087 = getIconImage(getURI(from+"icons/087_code.png"));
//		ico088 = getIconImage(getURI(from+"icons/088_picture.png"));
//		// END KGU#486 2018-01-18: Issue #4
//
//		// START KGU#287 2016-11-01: Issue #81: Scaling had been forgotten
//		ico089 = getIconImage(getURI(from+"icons/089_paraAfter.png"));
//		ico090 = getIconImage(getURI(from+"icons/090_paraBefore.png"));
//		ico091 = getIconImage(getURI(from+"icons/091_conv_para.png"));
//		// END KGU#287 2016-11-01
//
//		ico092 = getIconImage(getURI(from+"icons/092_SaveAs.png"));
//		//ico093 = getIconImage(getURI(from+"icons/093_pl.png"));
//		//ico094 = getIconImage(getURI(from+"icons/094_tw.png"));
//
//		// START KGU#287 2016-11-02: Issue #81: Scaling had been forgotten
//		ico102 = getIconImage(getURI(from+"icons/102_switch.png"));
//		// END KGU#287 2016-11-02            
//		// START KGU 2015-10-12: Whatever this might be good for...(?)
//		ico103 = getIconImage(getURI(from+"icons/103_breakpt.png"));
//		ico104 = getIconImage(getURI(from+"icons/104_nobreakpt.png"));
//		// END KGU 2015-10-12
//		// START KGU#2 2015-11-19
//		ico105 = getIconImage(getURI(from+"icons/105_arranger.png"));
//		// END KGU#2 2015-11-19
//		// START KGU#123 2016-01-03/04: Enh. #87
//		ico106 = getIconImage(getURI(from+"icons/106_collapse.png"));
//		ico107 = getIconImage(getURI(from+"icons/107_expand.png"));
//		ico108 = getIconImage(getURI(from+"icons/108_collapse_by_wheel.png"));
//		// END KGU#123 2016-01-03/04
//		// START KGU#199 2016-07-06: Enh. #188
//		ico109 = getIconImage(getURI(from+"icons/109_wand.png"));
//		// END KGU#199 2016-07-06
//		// START KGU#208 2016-07-22: Enh. #199 - direct online user guide activation
//		ico110 = getIconImage(getURI(from+"icons/110_help.png"));
//		// END KGU#208 2016-07-22
//		// START KGU#227 2016-07-31: Enh. #128 - comments plus text display mode
//		ico111 = getIconImage(getURI(from+"icons/111_c_plus_t.png"));
//		// END KGU#227 2016-07-31
//		// START KGU#213 2016-08-02: Enh. #215 - breakpoint counting trigger
//		ico112 = getIconImage(getURI(from+"icons/112_stopwatch.png"));
//		// END KGU#213 2016-08-02
//
//		// START KGU#287 2016-11-01: Issue #81: Scaling had been forgotten
//		ico113 = getIconImage(getURI(from+"icons/113_translater.png"));
//		// END KGU#287 2016-11-01
//
//		// START KGU#418 2017-12-11: Enh. #425
//		ico114 = getIconImage(getURI(from+"icons/114_down.png"));
//		ico115 = getIconImage(getURI(from+"icons/115_up.png"));
//		// END KGU#213 2017-12-11
		
		// Take advantage of the lazy initialization mechanism in getIcon(int) 
		icons = null;

		// START KGU 2020-04-02 Disabled now
		//turtle = getIconImage(getURI(from + "icons/turtle.png"));
		// END KGU 2020-04-02

		// START KGU#242 2016-09-05
		for (String key: icoLocales.keySet())
		{
			icoLocales.put(key, getIconImage(getURI("icons/locale_" + key + ".png")));
		}
		// END KGU#242 2016-09-05
	}

	/**
	 * Produces a new, scaled {@link IconImage} from icon file at the given {@code url}
	 * for the currently specified scale.
	 * 
	 * @param fileName - the file name of the icon file(s) in the cascaded icon folders.
	 * @return the retrieved or scaled ImageIco
	 * 
	 * @see #getIcon(int)
	 * @see #setScaleFactor(double)
	 */
	public static ImageIcon getIconImage(String fileName)
	// START KGU#486 2018-02-06: Issue #4 new opportunity to specify an extra factor
	{
		return getIconImage(fileName, 1.0);
	}
	/**
	 * Produces a new, scaled {@link IconImage} from icon file at the given {@code url}
	 * for the currently specified scale.
	 * 
	 * @param fileName - the file name of the icon file(s) in the cascaded icon folders;
	 * @param extraFactor - additional (product-internal) scaling factor
	 * @return the retrieved or scaled {@link ImageIcon}
	 * 
	 * @see #getIcon(int)
	 * @see #setScaleFactor(double)
	 */
	//@SuppressWarnings("unused")
	public static ImageIcon getIconImage(String fileName, double extraFactor)
	// END KGU#486 2018-02-06
	{
		//System.out.println("getIconImage(\"" + fileName + "\")");
		// START KGU#577 2018-09-17: Issue #601 - precautions against inconsistent resources or code
		// First we fetch the base icon (size 16 pixels = scalefactor 1)
		ImageIcon ii = null;
		try {
			ii = new ImageIcon(getURI("icons/" + fileName));
		}
		catch (Exception ex) {
			Logger.getLogger(IconLoader.class.getName()).log(Level.SEVERE, "Resources inconsistent - no icon file \"" + fileName + "\"!", ex);
			return getMissingIcon();
		}
		// END KGU#577 2018-09-17
		// We coerce the scale factor to multiples of 0.5 and compute the wanted size
		long pixels = 8 * Math.round(scaleFactor * extraFactor * 2);
		int size = 16;
		double factor = 1.0 * pixels / size;
		java.net.URL roundURL = null;
		java.net.URL largestURL = null;
		double minFactor = factor;
		for (int i = 1; i < ICON_SIZES.length && ICON_SIZES[i] <= pixels; i++) {
			size = ICON_SIZES[i];
			java.net.URL url = getURI("icons_" + size + "/" + fileName);
			if (url != null) {
				largestURL = url;
				minFactor = 1.0 * pixels / size;
				// If the file can be scaled with an integral factor, we'll cache it
				if (pixels % size == 0) { 
					roundURL = url;
					factor = pixels / size;
				}
			}
		}
		if (largestURL != null && minFactor < 1.25) {
			roundURL = largestURL;
			factor = 1.0;
		}
		if (roundURL != null && factor <= 3) {
			largestURL = roundURL;
		}
		else if (largestURL != null) {
			factor = minFactor;
		}
		else {
			factor = scaleFactor * extraFactor;
		}
		if (largestURL != null) {
			ii = new ImageIcon(largestURL);
		}
		// FIXME: Why is this signaled as dead code? (without being commented out, of course)
		//if (ii == null) {
		//	Logger.getLogger(IconLoader.class.getName()).log(Level.SEVERE, "No resource " + fileName + " with size " + extraFactor);
		//	return getMissingIcon();			
		//}
		return scale(ii, factor);
	}

	/**
	 * Produces a new, scaled {@link ImageIcon} from icon file at the given {@code url}
	 * for the currently specified scale.
	 * 
	 * @param url - the source URL for the icon file.
	 * @return A scaled {@link ImageIcon}, maybe a dummy item if the URL is null or illegal
	 * 
	 * @see #getIcon(int)
	 * @see #getIconImage(java.net.URL,boolean)
	 * @see #getIconImage(String)
	 * @see #setScaleFactor(double)
	 */
	public static ImageIcon getIconImage(java.net.URL url)
	{
		return getIconImage(url, false);
	}

	/**
	 * Produces a new, {@link ImageIcon} from icon file at the given {@code url}.
	 * If {@code fixed} is {@code true} then the icon will be scaled for the
	 * currently set {@link #scaleFactor}.
	 * 
	 * @param url - the source URL for the icon file.
	 * @param fixed - if the icon is to be scaled with the current factor
	 * @return An {@link ImageIcon}, maybe a dummy item if the URL is null or illegal
	 * 
	 * @see #getIcon(int)
	 * @see #getIconImage(java.net.URL)
	 * @see #getIconImage(String)
	 * @see #setScaleFactor(double)
	 */
	public static ImageIcon getIconImage(java.net.URL url, boolean fixed)
	{
		// START KGU#577 2018-09-18: Issue #601 
		//ImageIcon ii = new ImageIcon(url);
		//ii = scale(ii, scaleFactor);
		ImageIcon ii = null;
		try {
			ii = new ImageIcon(url);	// This will raise an exception if url = null or the file is unsuited
		}
		catch (Exception ex) {
			Logger.getLogger(IconLoader.class.getName()).log(Level.SEVERE, "*** Unable to retrieve the requested icon " + url + "!", ex);
			return getMissingIcon();
		}
		if (!fixed) {
			ii = scale(ii, scaleFactor);
		}
		// END KGU#577 2018-09-18
		return ii;
	}

	// STRT KGU#577 2018-09-18: Issue #601
	/**
	 * Produces a new, scaled {@link IconImage} from icon file at the given {@code url}
	 * for the currently specified scale.
	 * 
	 * @param url - the source URL for the icon file.
	 * @return the scaled {@link ImageIcon}
	 * 
	 * @see #getIcon(int)
	 * @see #setScaleFactor(double)
	 */
	public static ImageIcon getIconImageSafely(java.net.URL url)
	{
		ImageIcon ii = new ImageIcon(url);
		ii = scale(ii, scaleFactor);
		return ii;
	}

	// START KGU 2016-09-06
	public static ImageIcon getLocaleIconImage(String localeName)
	{
		ImageIcon ii = icoLocales.get(localeName);
		if (ii == null && Locales.isNamedLocale(localeName))
		{
			// Already comprises scaling...
			// START KGU#286 2018-02-13: Issues #4, #81
			//ii = getIconImage(getURI(from + "icons/locale_"+localeName+".png"));
			ii = getIconImage("locale_" + localeName + ".png");
			// END KGU#286 2018-02-13
		}
		return ii;
	}
	// END KGU 2016-09-06

	/**
	 * Returns an ImageIcon version of src, which is magnified by length factor
	 * {@code factor}. Uses method {@link #scaleTo(ImageIcon, int, int)}.
	 * 
	 * @param src - the source icon
	 * @param factor - the magnification factor (values < 1 ignored)
	 * @return the magnified icon
	 * 
	 * @see #scaleTo(ImageIcon, int, int)
	 */
	public static final ImageIcon scale(ImageIcon src, double factor)
	{
		//System.out.println(scaleFactor);
		if (factor > 1)
		{
			int w = (int)(factor * src.getIconWidth());
			int h = (int)(factor * src.getIconHeight());
			return scaleTo(src, w, h);
		}
		else return src;
	}


	/**
	 * Returns an ImageIcon version of src, which is magnified (or diminished) to the
	 * given width and height.
	 * 
	 * @param src - the source icon
	 * @param width - the target icon width
	 * @param height - the target icon height
	 * @return the magnified (or diminished) icon
	 * 
	 * @see #scale(ImageIcon, double)
	 */
	public static final ImageIcon scaleTo(ImageIcon src, int width, int height)
	{
		//System.out.println(scaleFactor);
		int type = BufferedImage.TYPE_INT_ARGB;
		BufferedImage dst = new BufferedImage(width, height, type);
		Graphics2D g2 = dst.createGraphics();
		g2.drawImage(src.getImage(), 0, 0, width, height, null);
		// FIXME: This may be somewhat rash as we cannot be sure drawImage was ready
		g2.dispose();
		return new ImageIcon(dst);
	}

	/** May return null if the resource is not found or not usable; otherwise an {@link java.net.URL} */
	public static java.net.URL getURI(String _filename)
	{
		IconLoader icol = new IconLoader();	// Is this to trigger the static initialization?
		return icol.getClass().getResource(_filename);
	}
	
	/**
	 * Generates a circular icon filled with the given colour {@code _color}
	 * and a thin black border. The circle fill be as large as possible within
	 * the current standard icon square of 16&nbsp;*&nbsp;{@link #scaleFactor} size.
	 * 
	 * @param _color - the fill colour
	 * @return the created circular icon
	 * 
	 * @see #generateIcon(Color, int)
	 */
	public static ImageIcon generateIcon(Color _color)
	{
		return generateIcon(_color, 0);
	}
	
	/**
	 * Generates a circular icon filled with the given colour {@code _color}
	 * and a thin black border. The circle radius will be reduced by
	 * {@code _insets} pixels with respect to the icon size of
	 * 16&nbsp;*&nbsp;{@link #scaleFactor}.
	 * 
	 * @param _color - the fill colour
	 * @param _insets - the distance of the circle circumference from the icon
	 *    border
	 * @return the created circular icon
	 * 
	 * @see #generateIcon(Color)
	 */
	public static ImageIcon generateIcon(Color _color, int _insets)
	{
		int size = (int) (16*scaleFactor);
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = (Graphics2D) image.getGraphics();
		graphics.setColor(Color.BLACK);
		// START KGU#493 2018-02-14: The colour buttons should look different from the Instruction button
		//graphics.fillRect(0,0,size,size);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setColor(Color.BLACK);
		graphics.fillOval(_insets, _insets, size - 2 * _insets, size - 2 * _insets);
		// END KGU#493 2018-02-14
		graphics.setColor(_color);
		// START KGU#493 2018-02-14: The colour buttons should look different from the Instruction button
		//graphics.fillRect(1,1,size-2,size-2);
		graphics.fillOval(_insets+1, _insets+1, size-2*(_insets+1), size-2*(_insets+1));
		// END KGU#493 2018-02-14
		// START KGU 2018-09-17 free resources no longer needed
		graphics.dispose();
		// END KGU 2018-09-17
		return new ImageIcon(image);
	}
	
	// START KGU#577 2018-09-17: Issue #601
	/** Produces a dummy icon showing that the intended icon wasn't available */
	private static ImageIcon generateMissingIcon()
	{
		int size = (int) (16 * scaleFactor);
		BasicStroke stroke = new BasicStroke((int)(2 * scaleFactor));
		int padding = (int) (5 * scaleFactor);
		
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = (Graphics2D) image.getGraphics();
        
        graphics.setColor(Color.WHITE);
        graphics.fillRect(1, 1, size-2, size-2);
        
        graphics.setColor(Color.BLACK);
        graphics.drawRect(1, 1, size-2, size-2);
        
        graphics.setColor(Color.RED);
        
        graphics.setStroke(stroke);
        graphics.drawLine(padding, padding, size - padding, size - padding);
        graphics.drawLine(padding, size - padding, size - padding, padding);
        
        graphics.dispose(); // free resources no longer needed
		return new ImageIcon(image);
	}
	
	private static ImageIcon getMissingIcon()
	{
		if (dummyIcon == null) {
			dummyIcon = generateMissingIcon();
		}
		return dummyIcon;
	}
	// END KGU#577 2018-09-17
	
	// START KGU#929 2021-02-11: Enh. #929 Added for Translator support
	/**
	 * Places a diminished version of standarc icon {@code iconNoDecor} into the
	 * upper left corner of the given icon {@code baseIcon}.
	 * 
	 * @param baseIcon - the base icon to be decorated
	 * @param iconNoDecor - the index of the decoration item
	 * @return the decorated icon
	 */
	public static ImageIcon decorateIcon(ImageIcon baseIcon, int iconNoDecor)
	{
		//System.out.println(scaleFactor);
		int type = BufferedImage.TYPE_INT_ARGB;
		int width = baseIcon.getIconWidth();
		int height = baseIcon.getIconHeight();
		BufferedImage dst = new BufferedImage(width, height, type);
		Graphics2D g2 = dst.createGraphics();
		g2.drawImage(baseIcon.getImage(), 0, 0, width, height, null);
		ImageIcon decor = getIcon(iconNoDecor);
		int size = Math.min(width, height);
		g2.drawImage(decor.getImage(), 0, 0, size, size, null);
		// FIXME: This may be somewhat rash as we cannot be sure drawImage was ready
		g2.dispose();
		return new ImageIcon(dst);
		
	}
	// END KGU#929 2021-02-11
	
// START KGU 2021-01-09: Finally disabled
//	/**
//	 * Sets a new absolute or relative base directory for the icon file retrieval. 
//	 * @param _from - an absolute or relative file path to the parent directory of
//	 * the icons and icons_&lt;pixels&gt; folder(s).
//	 */
//	@Deprecated
//	public static void setFrom(String _from)
//	{
//		from = _from;
//	}
// END KGU 2021-01-09
}

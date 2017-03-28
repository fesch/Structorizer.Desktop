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

package lu.fisch.structorizer.generators;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    This class generates XML code.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.27      First Issue
 *      Bob Fisch       2008.04.12      Added "Fields" section for generator to be used as plugin
 *      Kay Gürtzig     2015.11.08      Additional information with FOR loops (KGU#3)
 *      Kay Gürtzig     2015.12.18      Formal adaptation to Enh. #23 (KGU#78) related to break mechanism
 *      Kay Gürtzig     2015.12.21      Formal adaptation to Bugfix #41/#68/#69 (KGU#93)
 *      Kay Gürtzig     2015.12.31      Bugfix #82 (KGU#118) Inconsistent FOR loops used to obstruct saving
 *      Kay Gürtzig     2016.01.08      Bugfix #99 (KGU#134) mends mis-spelling due to fix #82
 *      Kay Gürtzig     2016.03.21-22   Enh. #84 (KGU#61) mechanisms to save FOR-IN loops adequately
 *      Kay Gürtzig     2016.09.25      Enh. #253: Root element now conveys parser preferences,
 *                                      CodeParser.keywordMap refactoring done (going to be superfluous!)
 *      Kay Gürtzig     2016.10.04      Bugfix #258: Structured FOR loop parameters weren't always preserved on saving
 *      Kay Gürtzig     2016.10.13      Enh. #270: Cared for new field "disabled"
 *      Kay Gürtzig     2016.12.21      Bugfix #317: Preserve color property of empty Subqueues
 *      Kay Gürtzig     2017.03.10      Enh. #372: Additional attributes (Simon Sobisch)
 *      Kay Gürtzig     2017.03.13      Enh. #372: License attributes/elements added (Simon Sobisch)
 *      Kay Gürtzig     2017.03.28      Enh. #370: Alternative keyword set may be saved (un-refactored diagrams)
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.util.Map;

import lu.fisch.utils.*;
import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.io.LicFilter;
import lu.fisch.structorizer.parsers.CodeParser;

public class XmlGenerator extends Generator {

	// START KGU#118 2015-12-31: Support for bugfix #82
	// START KGU#134 2016-01-08: Bugfix #99: mis-spelled attribute name
	//private static String[] forLoopAttributes = {"counterVar", "StartValue", "endValue", "stepConst"};
	private static String[] forLoopAttributes = {"counterVar", "startValue", "endValue", "stepConst"};
	// END KGU#134 2016-01-08
	// END KGU#118 2015-12-31
	
	/************ Fields ***********************/
	protected String getDialogTitle()
	{
		return "XML Code ...";
	}
	
	protected String getFileDescription()
	{
		return "XML Code";
	}
	
	protected String getIndent()
	{
		return "\t";
	}
	
	protected String[] getFileExtensions()
	{
		String[] exts = {"xml","nsd"};
		return exts;
	}
	
    // START KGU 2015-10-18: New pseudo field
    @Override
    protected String commentSymbolLeft()
    {
    	return "<!--";
    }

    @Override
    protected String commentSymbolRight()
    {
    	return "-->";
    }
    // END KGU 2015-10-18
    
	// START KGU#78 2015-12-18: Enh. #23 - Irrelevant here but necessary now
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#supportsSimpleBreak()
	 */
	@Override
	protected boolean breakMatchesCase()
	{
		return true;
	}
	// END KGU#78 2015-12-18

	// START KGU#351 2017-02-26: Enh. #346 - include / import / uses config
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getIncludePattern()
	 */
	@Override
	protected String getIncludePattern()
	{
		return null;
	}
	// END KGU#351 2017-02-26

	/************ Code Generation **************/
    
	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/**
	 * A pattern how to embed the variable (right-hand side of an input instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "$1 = (new Scanner(System.in)).nextLine();"
	 */
	protected String getInputReplacer(boolean withPrompt)
	{
		return "";
	}

	/**
	 * A pattern how to embed the expression (right-hand side of an output instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "System.out.println($1);"
	 */
	protected String getOutputReplacer()
	{
		return "";
	}

	// START KGU#93 2015-12-21: Bugfix #41/#68/#69 - no longer needed
//	/**
//	 * Transforms assignments in the given intermediate-language code line.
//	 * @param _interm - a code line in intermediate syntax
//	 * @return transformed string
//	 */
//	protected String transformAssignment(String _interm)
//	{
//		return _interm;
//	}
	// END KGU#93 2015-12-21
	// END KGU#18/KGU#23 2015-11-01
    
    
    @Override
	protected void generateCode(Instruction _inst, String _indent)
	{
		String r = "0";
		//if(_inst.rotated==true) {r="1";}
		code.add(_indent+"<instruction text=\""+BString.encodeToHtml(_inst.getText().getCommaText())+"\" comment=\""+
												BString.encodeToHtml(_inst.getComment().getCommaText())+"\" color=\""+
												_inst.getHexColor()+"\" rotated=\""+r+"\" disabled=\""+
												(_inst.disabled ? "1" : "0") + "\"></instruction>");
	}
	
    @Override
	protected void generateCode(Alternative _alt, String _indent)
	{
		code.add(_indent+"<alternative text=\""+BString.encodeToHtml(_alt.getText().getCommaText())+"\" comment=\""+
				 BString.encodeToHtml(_alt.getComment().getCommaText())+"\" color=\""+
				 _alt.getHexColor()+"\" disabled=\""+ (_alt.disabled ? "1" : "0") + "\">");
    	// START KGU 2016-12-21: Bugfix #317
		//code.add(_indent+this.getIndent()+"<qTrue>");
		//generateCode(_alt.qTrue,_indent+this.getIndent()+this.getIndent());
		//code.add(_indent+this.getIndent()+"</qTrue>");
		//code.add(_indent+this.getIndent()+"<qFalse>");
		//generateCode(_alt.qFalse,_indent+this.getIndent()+this.getIndent());
		//code.add(_indent+this.getIndent()+"</qFalse>");
		generateCode(_alt.qTrue, _indent+this.getIndent(), "qTrue");
		generateCode(_alt.qFalse, _indent+this.getIndent(), "qFalse");
	    // END KGU 2016-12-21
		code.add(_indent+"</alternative>");
	}
	
    @Override
	protected void generateCode(Case _case, String _indent)
	{
		code.add(_indent+"<case text=\""+BString.encodeToHtml(_case.getText().getCommaText())+"\" comment=\""+
				 BString.encodeToHtml(_case.getComment().getCommaText())+"\" color=\""+
				 _case.getHexColor()+"\" disabled=\""+ (_case.disabled ? "1" : "0") + "\">");
		for(int i=0;i<_case.qs.size();i++)
		{
	    	// START KGU 2016-12-21: Bugfix #317
			//code.add(_indent+this.getIndent()+"<qCase>");
			//generateCode((Subqueue) _case.qs.get(i),_indent+this.getIndent()+this.getIndent());
			//code.add(_indent+this.getIndent()+"</qCase>");
			generateCode(_case.qs.get(i), _indent+this.getIndent(), "qCase");
		    // END KGU 2016-12-21
		}
		code.add(_indent+"</case>");
	}

    @Override
    	protected void generateCode(Parallel _para, String _indent)
	{
		code.add(_indent+"<parallel text=\""+BString.encodeToHtml(_para.getText().getCommaText())+"\" comment=\""+
				 BString.encodeToHtml(_para.getComment().getCommaText())+"\" color=\""+
				 _para.getHexColor()+"\">");
		for(int i=0;i<_para.qs.size();i++)
		{
	    	// START KGU 2016-12-21: Bugfix #317
			//code.add(_indent+this.getIndent()+"<qPara>");
			//generateCode((Subqueue) _para.qs.get(i),_indent+this.getIndent()+this.getIndent());
			//code.add(_indent+this.getIndent()+"</qPara>");
			generateCode(_para.qs.get(i), _indent+this.getIndent(), "qPara");
		    // END KGU 2016-12-21
		}
		code.add(_indent+"</parallel>");
	}

    @Override
	protected void generateCode(For _for, String _indent)
	{
    	// START KGU#118 2015-12-31: Bugfix 82: "free-style" FOR loops used to obstruct saving
    	// We need some pre-processing to enhance robustness: If some of the specific fields
    	// cannot be retrieved then just omit them, they aren't strictly needed on loading.
    	String[] specificInfo = _for.splitForClause();
    	// START KGU#268 2016-10-04: Bugfix #258: The approach above turned out to be too simple
    	// The split clause is good as a basis but if the FOR loop style was identified then the
    	// specifically stored information might be better suited (particularly if e.g. parser
    	// keywords have been changed in the meantime. So override the split result if more precise
    	// data is available...
    	String info = _for.getCounterVar();
    	if (info != null && !info.isEmpty())
    	{
    		specificInfo[0] = info;
    	}
    	if (_for.style == For.ForLoopStyle.COUNTER)
    	{
        	if ((info = _for.getStartValue()) != null && !info.isEmpty())
        	{
        		specificInfo[1] = info;
        	}
        	if ((info = _for.getEndValue()) != null && !info.isEmpty())
        	{
        		specificInfo[2] = info;
        	}
       		specificInfo[3] = Integer.toString(_for.getStepConst());
    	}
    	// END KGU#268 2016-10-04
    	String specificAttributes = "";
    	for (int i = 0; i < forLoopAttributes.length; i++)
    	{
    		if (specificInfo[i] != null)
    		{
    			specificAttributes += "\" " + forLoopAttributes[i] + "=\"" + BString.encodeToHtml(specificInfo[i]);
    		}
    	}
    	code.add(_indent+"<for text=\""+BString.encodeToHtml(_for.getText().getCommaText()) +
    			"\" comment=\"" + BString.encodeToHtml(_for.getComment().getCommaText()) +
    			specificAttributes +
    			"\" style=\"" + BString.encodeToHtml(_for.style.toString()) +
    			// FIXME: No longer needed beyond version 3.25-01, except for backward compatibility (i. e. temporarily)
    			(_for.isForInLoop() ? ("\" insep=\"" + BString.encodeToHtml(CodeParser.getKeyword("postForIn"))) : "") +
    			"\" color=\"" + _for.getHexColor()+"\" disabled=\""+
    			(_for.disabled ? "1" : "0") + "\">");
    	// END KGU#118 2015-12-31
    	// START KGU 2016-12-21: Bugfix #317
		//code.add(_indent+this.getIndent()+"<qFor>");
		//generateCode(_for.q,_indent+this.getIndent()+this.getIndent());
		//code.add(_indent+this.getIndent()+"</qFor>");
		generateCode(_for.q, _indent+this.getIndent(), "qFor");
		// END KGU 2016-12-21
		code.add(_indent+"</for>");
	}
	
    @Override
	protected void generateCode(While _while, String _indent)
	{
		code.add(_indent+"<while text=\""+BString.encodeToHtml(_while.getText().getCommaText())+"\" comment=\""+
				 BString.encodeToHtml(_while.getComment().getCommaText())+"\" color=\""+
				 _while.getHexColor()+"\" disabled=\""+(_while.disabled ? "1" : "0") + "\">");
    	// START KGU 2016-12-21: Bugfix #317
		//code.add(_indent+this.getIndent()+"<qWhile>");
		//generateCode(_while.q,_indent+this.getIndent()+this.getIndent());
		//code.add(_indent+this.getIndent()+"</qWhile>");
		generateCode(_while.q, _indent+this.getIndent(), "qWhile");
	    // END KGU 2016-12-21
		code.add(_indent+"</while>");
	}
	
    @Override
	protected void generateCode(Repeat _repeat, String _indent)
	{
		code.add(_indent+"<repeat text=\""+BString.encodeToHtml(_repeat.getText().getCommaText())+"\" comment=\""+
				 BString.encodeToHtml(_repeat.getComment().getCommaText())+"\" color=\""+
				 _repeat.getHexColor()+"\" disabled=\""+ (_repeat.disabled ? "1" : "0") + "\">");
    	// START KGU 2016-12-21: Bugfix #317
		//code.add(_indent+this.getIndent()+"<qRepeat>");
		//generateCode(_repeat.q,_indent+this.getIndent()+this.getIndent());
		//code.add(_indent+this.getIndent()+"</qRepeat>");
		generateCode(_repeat.q, _indent+this.getIndent(), "qRepeat");
	    // END KGU 2016-12-21
		code.add(_indent+"</repeat>");
	}
	
    @Override
	protected void generateCode(Forever _forever, String _indent)
	{
		code.add(_indent+"<forever text=\""+BString.encodeToHtml(_forever.getText().getCommaText())+"\" comment=\""+
				 BString.encodeToHtml(_forever.getComment().getCommaText())+"\" color=\""+
				 _forever.getHexColor()+"\" disabled=\""+(_forever.disabled ? "1" : "0") + "\">");
    	// START KGU 2016-12-21: Bugfix #317
		//code.add(_indent+this.getIndent()+"<qForever>");
		//generateCode(_forever.q,_indent+this.getIndent()+this.getIndent());
		//code.add(_indent+this.getIndent()+"</qForever>");
		generateCode(_forever.q, _indent+this.getIndent(), "qForever");
	    // END KGU 2016-12-21
		code.add(_indent+"</forever>");
	}
	
    @Override
	protected void generateCode(Call _call, String _indent)
	{
		code.add(_indent+"<call text=\""+BString.encodeToHtml(_call.getText().getCommaText())+"\" comment=\""+
				 BString.encodeToHtml(_call.getComment().getCommaText())+"\" color=\""+
				 _call.getHexColor()+"\" disabled=\""+(_call.disabled ? "1" : "0") + "\"></call>");
	}
	
    @Override
	protected void generateCode(Jump _jump, String _indent)
	{
		code.add(_indent+"<jump text=\""+BString.encodeToHtml(_jump.getText().getCommaText())+"\" comment=\""+
				 BString.encodeToHtml(_jump.getComment().getCommaText())+"\" color=\""+
				 _jump.getHexColor()+"\" disabled=\""+(_jump.disabled ? "1" : "0") + "\"></jump>");
	}
	
	// START KGU 2016-12-21: Bugfix #315 - preserve the element colour of empty subqueues
	protected void generateCode(Subqueue _subqueue, String _indent, String tagName)
	{
		String colorAttr = "";
		if (_subqueue.getSize() == 0) {
			colorAttr = " color=\""+_subqueue.getHexColor() + "\"";
		}
		code.add(_indent+"<" + tagName + colorAttr + ">");
    	generateCode(_subqueue, _indent + this.getIndent());
		code.add(_indent+"</" + tagName + ">");
	}
    // END KGU 2016-12-21
	
    @Override
	public String generateCode(Root _root, String _indent)
	{
		String pr = _root.isProgram ? "program" : "sub";
		String ni = _root.isNice ? "nice" : "abbr";
		
		// START KGU#257 2016-09-25: Enh. #253
		String pp_attributes = "";
		
		for (Map.Entry<String, String> entry: CodeParser.getPropertyMap(true).entrySet())
		{
			// Empty keywords will hardly have been used in this diagram, so it's okay to omit them
			// START KGU#362 2017-03-28: Enh. #370 - Special care for un-refactored diagrams
			//if (!entry.getValue().isEmpty())
			//{
			//	pp_attributes += " " + entry.getKey() + "=\"" + BString.encodeToHtml(entry.getValue()) + "\"";
			//}
			String value = entry.getValue();
			if (_root.storedParserPrefs != null && _root.storedParserPrefs.containsKey(entry.getKey())) {
				value = _root.storedParserPrefs.get(entry.getKey()).concatenate();
			}
			if (!value.isEmpty())
			{
				pp_attributes += " " + entry.getKey() + "=\"" + BString.encodeToHtml(value) + "\"";
			}
			// END KGU#362 2017-03-28
		}
		// END KGU#257 2016-09-25
		
		// START KGU#363 2017-03-10: Enh. #372 These are no parser preferences but the mechanism is convenient
		if (_root.getAuthor() != null) {
			pp_attributes += " author=\"" + BString.encodeToHtml(_root.getAuthor()) + "\"";
		}
		if (_root.getCreated() != null) {
			pp_attributes += " created=\"" + _root.getCreatedString() + "\"";
		}
		if (_root.getModifiedBy() != null) {
			pp_attributes += " changedby=\"" + BString.encodeToHtml(_root.getModifiedBy()) + "\"";
		}
		if (_root.getModified() != null) {
			pp_attributes += " changed=\"" + _root.getModifiedString() + "\"";
		}
		// END KGU#363 3017-03-10
		// START KGU#362 2017-03-13: Enh. #372 License stuff
		String licName = _root.licenseName;
		if (licName == null && Ini.getInstance().getProperty("author", System.getProperty("user.name")).equals(_root.getAuthor())) {
			// Look for a default license
			licName = Ini.getInstance().getProperty("licenseName", "").trim();
		}
		if (licName != null && !licName.isEmpty()) {
			pp_attributes += " licenseName=\"" + BString.encodeToHtml(licName) + "\"";

			String licenseText = _root.licenseText; 
			if (licenseText == null || licenseText.trim().isEmpty()) {
				licenseText = this.loadLicenseText(licName);
			}
			if (licenseText != null) {
				pp_attributes += " license=\"" + BString.encodeToXML(licenseText) + "\"";
			}
		}
		// END KGU#362 2017-03-13

		code.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		//code.add("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
		// START KGU 2015-12-04: Might not be so bad an idea to write the product version into the file
		//code.add("<root xmlns:nsd=\"http://structorizer.fisch.lu/\" text=\""+BString.encodeToHtml(_root.getText().getCommaText())+"\" comment=\""+
		// START KGU#257 2016-09-25: Enh. #253: Add all current parser preferences
		//code.add("<root xmlns:nsd=\"http://structorizer.fisch.lu/\" version=\"" + Element.E_VERSION + "\" text=\"" + 
		code.add("<root xmlns:nsd=\"http://structorizer.fisch.lu/\" version=\"" + Element.E_VERSION + "\"" +
								pp_attributes + " text=\"" + 
		// END KGU#257 2016-09-25
								BString.encodeToHtml(_root.getText().getCommaText()) + "\" comment=\"" +
		// END KGU 2015-12-04
								BString.encodeToHtml(_root.getComment().getCommaText())+"\" color=\""+
								_root.getHexColor()+"\" type=\""+pr+"\" style=\""+ni+"\">");
		// START KGU 2016-12-21: Bugfix #317
		//code.add(_indent+"<children>");
		//generateCode(_root.children,_indent+this.getIndent());
		//code.add(_indent+"</children>");
		generateCode(_root.children, _indent, "children");
		// END KGU 2016-12-21
		code.add("</root>");
		
		return code.getText();
	}

	private String loadLicenseText(String licName) {
		String error = null;
		String content = "";
		File licDir = Ini.getIniDirectory();
		String licFileName = LicFilter.getNamePrefix() + licName + "." + LicFilter.acceptedExtension();
		File[] licFiles = licDir.listFiles(new LicFilter());
		File licFile = null; 
		for (int i = 0; licFile == null && i < licFiles.length; i++) {
			if (licFileName.equalsIgnoreCase(licFiles[i].getName())) {
				licFile = licFiles[i];
			}		
		}
		BufferedReader br = null;
		try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(licFile), "UTF-8");
			br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				content += line + '\n';
			};
		} catch (UnsupportedEncodingException e) {
			error = e.getMessage();
		} catch (FileNotFoundException e) {
			error = e.getMessage();
		} catch (IOException e) {
			error = e.getMessage();
		}
		if (br != null) {
			try {
				br.close();
			} catch (IOException e) {
				error = e.getMessage();
			}
		}
		if (error != null) {
			System.err.println("XmlGenerator.loadLicenseText(): " + error);
		}
		if (content.trim().isEmpty()) {
			content = null;
		}
		return content;	
	}

	@Override
	public String[] getReservedWords() {
		// Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCaseSignificant() {
		// Auto-generated method stub
		return false;
	}
	

}

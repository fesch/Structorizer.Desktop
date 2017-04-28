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
package lu.fisch.structorizer.parsers;

/******************************************************************************************************
 *
 *      Author:         kay
 *
 *      Description:    This is a subclass of Ralph Iden's GOLDParser version 5.0 (Au = gold),
 *                      hence a language-independent general-purpose LALR(1) parser.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.03.09      First Issue
 *      Kay Gürtzig     2017.04.27      File logging option added
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      One of the major issues inducing this subclass was a NullPointerException on loading legacy
 *      cgt file, e.g. the D/Grammar.cgt used for the Pascal import into Structorizer.
 *      The exception was caused by a missing "Comment" symbol for grouping line comments. Since it
 *      turned out impossible to generate an upgraded cgt or egt from D7Grammar.grm with the current
 *      GOLDbuild tool, I chose to apply a fix to method Parser.resolveCommentGroupsForVersion1Grammars(),
 *      which solved the problem. I wanted to avoid to many invasive code interventions in the original
 *      package, so I just changed the method visibility from private to protected and overrode it here. 
 *
 ******************************************************************************************************///

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import com.creativewidgetworks.goldparser.engine.Group;
import com.creativewidgetworks.goldparser.engine.Parser;
import com.creativewidgetworks.goldparser.engine.Reduction;
import com.creativewidgetworks.goldparser.engine.Symbol;
import com.creativewidgetworks.goldparser.engine.SymbolList;
import com.creativewidgetworks.goldparser.engine.Token;
import com.creativewidgetworks.goldparser.engine.enums.AdvanceMode;
import com.creativewidgetworks.goldparser.engine.enums.EndingMode;
import com.creativewidgetworks.goldparser.engine.enums.SymbolType;
import com.creativewidgetworks.goldparser.parser.GOLDParser;
import com.creativewidgetworks.goldparser.parser.GOLDParserBuildContext;
import com.creativewidgetworks.goldparser.parser.Scope;
import com.creativewidgetworks.goldparser.util.FormatHelper;
import com.creativewidgetworks.goldparser.util.ResourceHelper;

/**
 * General purpose wrapper class used to process source input and generate
 * a parse tree, directly based on {@link GOLDParser} with some fixes
 * version 5.0.0 by Ralph Iden (http://www.creativewidgetworks.com)
 * 
 * <br>Dependencies:
 * <ul>
 * <li>{@link GOLDParser}</li>
 * <li>{@link Parser}</li>
 * <li>{@link Reduction}</li>
 * <li>{@link Scope}</li>
 * <li>{@link FormatHelper}</li>
 * <li>{@link ResourceHelper}</li>
 *  </ul>
 * 
 * @version 3.27.0
 * @author Kay Gürtzig
 */
public class AuParser extends GOLDParser {
	
	// START KGU#354 2017-04-27: Enh. #354
	OutputStreamWriter logFile = null;
	// END KGU#354 2017-04-27

	/**
	 * 
	 */
	public AuParser() {
		// Auto-generated constructor stub
	}

	/**
	 * @param context
	 */
	public AuParser(GOLDParserBuildContext context) {
		// Auto-generated constructor stub
		super(context);
	}

	/**
	 * @param cgtFile
	 * @param rulesPackage
	 * @param trimReductions
	 */
	public AuParser(File cgtFile, String rulesPackage, boolean trimReductions) {
		// Auto-generated constructor stub
		super(cgtFile, rulesPackage, trimReductions);
	}

	/**
	 * @param cgtFile
	 * @param rulesPackage
	 * @param trimReductions
	 */
	public AuParser(InputStream cgtFile, String rulesPackage, boolean trimReductions) {
		// Auto-generated constructor stub
		super(cgtFile, rulesPackage, trimReductions);
	}

	// START KGU#354 2017-04-27: Enh. #354
	/**
	 * @param cgtFile - the (extended) compiled grammar as input stream
	 * @param rulesPackage - name/path of the compiled grammar table 
	 * @param trimReductions - whether reductions paths are to be shortened sensibly 
	 * @param logger - An open output stream for logging or null 
	 */
	public AuParser(InputStream cgtFile, String rulesPackage, boolean trimReductions, OutputStreamWriter logger) {
		super(cgtFile, rulesPackage, trimReductions);
		logFile = logger;
	}
	// END KGU#354 2017-04-27

    /**
     * Inserts Group objects into the group table so comments can be processed in a 
     * grammar.  It is assumed that version 1.0 files have a maximum of 1 closed
     * comment block and one comment line symbol.
     */	
    protected void resolveCommentGroupsForVersion1Grammars() {
        if (isVersion1Format()) {
            Group group;
            Symbol symbolStart = null;
            Symbol symbolEnd = null;
            
            // Create a new COMMENT_LINE group
            for (Symbol currentStartSymbol : symbolTable) {
                if (currentStartSymbol.getType().equals(SymbolType.COMMENT_LINE)) {
                    symbolStart = currentStartSymbol;
                    group = new Group();
                    group.setName("Comment Line");
                    // START KGU#354 2017-03-08: Bugfix for old cgt file where there was no COMMENT symbol
                    //group.setContainer(symbolTable.findByName(SymbolList.SYMBOL_COMMENT));
                    Symbol commentSymbol = symbolTable.findByName(SymbolList.SYMBOL_COMMENT);
                    if (commentSymbol == null) {
                    	// Okay then just create one...
                    	commentSymbol = new Symbol(SymbolList.SYMBOL_COMMENT, SymbolType.NOISE, symbolTable.size());
                    	symbolTable.add(commentSymbol);
                    }
                    group.setContainer(commentSymbol);
                    // END KGU#354 2017-03-08
                    group.setStart(symbolStart);
                    group.setEnd(symbolTable.findByName("NewLine"));
                    group.setAdvanceMode(AdvanceMode.TOKEN);
                    group.setEndingMode(EndingMode.OPEN);
                    groupTable.add(group);
                    symbolStart.setGroup(group);
                    break;
                }
            }

            // Create a new COMMENT_BLOCK group
            for (Symbol currentStartSymbol : symbolTable) {
                if (currentStartSymbol.getType().equals(SymbolType.GROUP_START)) {
                    symbolStart = symbolEnd = currentStartSymbol;
                    for (Symbol currentEndSymbol : symbolTable) {
                        if (currentEndSymbol.getType().equals(SymbolType.GROUP_END)) { 
                            symbolEnd = currentEndSymbol;
                            break;
                        }
                    }    
                    group = new Group();
                    group.setName("Comment Block");
                    group.setContainer(symbolTable.findByName(SymbolList.SYMBOL_COMMENT));
                    group.setStart(symbolStart);
                    group.setEnd(symbolEnd);
                    group.setAdvanceMode(AdvanceMode.TOKEN);
                    group.setEndingMode(EndingMode.CLOSED);
                    groupTable.add(group);
                    
                    symbolStart.setGroup(group);                         
                    symbolEnd.setGroup(group);                         
                    
                    break;
                }
            }
        }
    }

    protected boolean processTokenRead() {
    	if (logFile != null) {
    		try {
    			Token token = this.getCurrentToken();
    			String tokenStr = token.toString();
    			logFile.write("Token " + tokenStr + "\tat " + this.getCurrentPosition().toString().trim());
    			if (!tokenStr.equals("(NewLine)") && !tokenStr.matches("^'.'$")) {
    				logFile.write(": " + token.asString() );
    			}
    			logFile.write("\n");
    		} catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    		}
    	}
    	return false;
    }
    
    public Token getCurrentToken() {
    	return super.getCurrentToken();
    }
}

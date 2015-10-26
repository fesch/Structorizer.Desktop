package goldengine.java;

import java.io.*;
import java.util.*;

/*
 * Licensed Material - Property of Matthew Hawkins (hawkini@barclays.net)
 *
 * GOLDParser - code ported from VB - Author Devin Cook. All rights reserved.
 *
 * No modifications to this code are allowed without the permission of the author.
 */
/**-------------------------------------------------------------------------------------------<br>
 *
 *      Source File:    GenericParser.java<br>
 *
 *      Author:         Devin Cook, Matthew Hawkins<br>
 *
 *      Description:    The main parsing engine.<br>
 *
 *
 *-------------------------------------------------------------------------------------------<br>
 *
 *      Revision List<br>
 *<pre>
 *      Author          Version         Description
 *      ------          -------         -----------
 *      MPH             1.0             First Issue
 *      MPH             1.1             Added the TrimReductions property and required logic
 *      MPH             1.2             Fixed Comment bug to ignore all tokens</pre><br>
 *
 *-------------------------------------------------------------------------------------------<br>
 *
 *      IMPORT: java.io, java.util<br>
 *
 *-------------------------------------------------------------------------------------------<br>
 */
public class GOLDParser implements GPMessageConstants,
								   SymbolTypeConstants,
                                   ActionConstants,
                                   RecordIDConstants,
                                   ParseResultConstants
{
    private final String fileHeader = "GOLD Parser Tables/v1.0";

    //================================== Public Properties
    private boolean pSimplifyReductions;        //Automatically
    private final int gpReportModeAll = 1001;
    private final int gpReportReductionsOnly = 1002;

    //================================== Symbols recognized by the system
	private SymbolList pSymbolTable = new SymbolList();

    //================================== DFA. Contains FAStates.
	private Vector pDFA = new Vector();                       //FAState
	private Vector pCharacterSetTable = new Vector();         //String

    //================================== Rules. Contains Rule Objects.
	private Vector pRuleTable = new Vector();                 //Rule

	//================================== Grammar variables
	private VariableList pVariables = new VariableList();

    //================================== LALR(1) action table. Contains LRActionTables.
	private Vector pActionTable = new Vector();     //Contains LRStates

    //========================================= DFA runtime constants
	private Symbol kErrorSymbol;
	private Symbol kEndSymbol;

    //========================================= DFA runtime variables
	private int initialDFAState;

    //========================================= LALR runtime variables
    private int startSymbol;
	private int initialLALRState;
	private int currentLALR;
    private Stack stack = new Stack();

    //===================== Used for Reductions & Errors
    private Stack pTokens = new Stack();        //The set of tokens for 1. Expecting during error, 2. Reduction
    private boolean pHaveReduction;

    //===================== Properties
	//NEW 1.1
	private boolean pTrimReductions;

    //===================== Private control variables
    private boolean pTablesLoaded;
    private int pLineNumber;
    private LookAheadStream pSource = null;
    private int pCommentLevel;                    //How many levels the comments are
    private Stack pInputTokens = new Stack();     //Stack of tokens to be analyzed
    private boolean pIgnoreCase;

    //===================== Added Class Variables = MPH
    // NONE - due to modified ParseToken

    // ***************************************************************
    // DEBUG STUFF
    // ***************************************************************

    /*public void write(BufferedWriter buffW, String toWrite, boolean newLine) throws IOException
    {
        buffW.write(toWrite, 0, toWrite.length());
        if(newLine)
        {
        	buffW.newLine();
        }
        buffW.flush();
    }  */

    // ***************************************************************
    // DEBUG STUFF END
    // ***************************************************************


    /***************************************************************
 	 *
 	 * GOLDParser
 	 *
 	 * The constructor initiates some variables.
 	 ***************************************************************/
    public GOLDParser()
    {
        reset();
        pTablesLoaded = false;
        pTrimReductions = true;
    }

    private void prepareToParse()
    {
        //ver 1.1: The token stack is empty until needed
        Token start = new Token();
        start.setState(initialLALRState);
        start.setParentSymbol(pSymbolTable.getMember(startSymbol));

        stack.push(start);
    }

    /***************************************************************
 	 *
 	 * currentLineNumber
 	 *
 	 * Returns the current source file line number.
 	 * @return The current source file line number
 	 ***************************************************************/
    public int currentLineNumber()
    {
        return pLineNumber;
    }

    /***************************************************************
 	 *
 	 * closeFile
 	 *
 	 * This method will close the source file.
 	 * @throws ParserException The engine parser should deal with
     *              a problem with closing the source file.
 	 ***************************************************************/
    public void closeFile() throws ParserException
    {
        pSource.closeFile();
        pSource = null;
    }

    /***************************************************************
 	 *
 	 * currentToken
 	 *
 	 * This method returns the current Token. The current token is
     * the last token read by "retrieveToken".
 	 * @return The current Token.
 	 ***************************************************************/
    public Token currentToken()
    {
        return (Token)pInputTokens.peek();
    }

    /***************************************************************
 	 *
 	 * popInputToken
 	 *
 	 * This method should only be called if there is a lexical
     * error and you need to pop an unexpected token out of the stack.
 	 * @return The token at the top of the stack.
 	 ***************************************************************/
    public Token popInputToken()
    {
        return (Token)pInputTokens.pop();
    }

    /***************************************************************
 	 *
 	 * pushInputToken
 	 *
 	 * This method should only be used if there is a syntax error.
     * It will push a Token onto the top of the stack so that the
     * parser might have a chance of finding an correctly typed token.
 	 * @param theToken A token to push onto the stack.
 	 ***************************************************************/
    public void pushInputToken(Token theToken)
    {
        pInputTokens.push(theToken);
    }

    /***************************************************************
 	 *
 	 * getToken
 	 *
 	 * If you require access to tokens in the stack before they
     * are placed on the parse tree. Enter an index number
     * and the Token will be returned, if and only if the index number
     * is valid.
 	 * @param index The index number.
 	 * @return The Token at the index specified.
 	 ***************************************************************/
    public Token getToken(int index)
    {
        if((index >= 0) & (index < pTokens.size()))
        {
            return (Token)pTokens.elementAt(index);
        }
        else
        {
            return null;
        }
    }

    /***************************************************************
 	 *
 	 * currentReduction
 	 *
 	 * This method will return the current reduction. This will only
     * happen if the parsing engine has performed a reduction. If it
     * has, then the reduction passed back will be the last one
     * performed.
 	 * @return The last Reduction performed.
 	 ***************************************************************/
    public Reduction currentReduction()
    {
        if(pHaveReduction)
        {
            Token myRed = (Token)stack.peek();
            return (Reduction)myRed.getData();
        }
        else
        {
            return null;
        }
    }

    /***************************************************************
 	 *
 	 * setCurrentReduction
 	 *
 	 * This method will set the current reduction to the one
     * passed in.
 	 * @param value The new Reduction to set as the current one.
 	 ***************************************************************/
    public void setCurrentReduction(Reduction value)
    {
        if(pHaveReduction)
        {
            Token myTok = (Token)stack.peek();
            myTok.setData(value);
        }
    }

    // this method is not accessible. It will read until a line break
    // is found and simply discard it.
    private String discardRestOfLine() throws ParserException
    {
        return pSource.readLine();
    }

    /***************************************************************
 	 *
 	 * setTrimReductions #ver1.1#
 	 *
 	 * This method will set the whether or not the program should trim the
     * reductions.
 	 * @param value True if we should trim reductions, false if not.
 	 ***************************************************************/
    public void setTrimReductions(boolean value)
    {
        pTrimReductions = value;
    }

    /***************************************************************
 	 *
 	 * getTrimReductions #ver1.1#
 	 *
 	 * This method will get whether or not we should trim the reductions.
 	 * @return True if we should trim reductions, false if not.
 	 ***************************************************************/
    public boolean getTrimReductions()
    {
        return pTrimReductions;
    }

    /***************************************************************
 	 *
 	 * parameter
 	 *
 	 * This method will return the value of a parameter that corresponds to the
     * name passed in.
     * @param name The name of the variable.
 	 * @return The value of the variable.
 	 ***************************************************************/
    public String parameter(String name)
    {
        return pVariables.getValue(name);
    }

    /***************************************************************
 	 *
 	 * symbolTableCount
 	 *
 	 * This method returns the total number of symbols in the symbol table.
 	 * @return The number of symbols in the table.
 	 ***************************************************************/
    public int symbolTableCount()
    {
        return pSymbolTable.count();
    }

    /***************************************************************
 	 *
 	 * ruleTableCount
 	 *
 	 * This method returns the total number of rules in the rule table.
 	 * @return The number of rules in the table.
 	 ***************************************************************/
    public int ruleTableCount()
    {
        return pRuleTable.size();
    }

    /***************************************************************
 	 *
 	 * symbolTableEntry
 	 *
 	 * This method will return a Symbol at the specified index.
     * @param index The index number in the table.
 	 * @return The Symbol at the specified index.
 	 ***************************************************************/
    public Symbol symbolTableEntry(int index)
    {
        if((index >= 0) & (index < pSymbolTable.count()))
        {
            return pSymbolTable.getMember(index);
        }

        return null;
    }

    /***************************************************************
 	 *
 	 * ruleTableEntry
 	 *
 	 * This method will return a Rule at the specified index.
 	 * @param index The index number in the table.
 	 * @return The Rule at the specified index.
 	 ***************************************************************/
    public Rule ruleTableEntry(int index)
    {
        if((index >= 0) & (index < pRuleTable.size()))
        {
            return (Rule)pRuleTable.elementAt(index);
        }

        return null;
    }

    /***************************************************************
 	 *
 	 * tokenCount
 	 *
 	 * This method will return the number of tokens in the stack.
 	 * @return The number of tokens in the stack.
 	 ***************************************************************/
    public int tokenCount()
    {
        return pTokens.size();
    }

    // this method is not accessible. It will load all the information
    // contained in the .cgt file passed in as the fileName parameter.
    // It will then store this information in the relevant tables
    // and storage defined as class variables in this class.
    private boolean loadTables(String fileName) throws ParserException
    {
        SimpleDatabase sdb = new SimpleDatabase();
        boolean success = true;

        Integer nA;
        int n;

        Boolean bA;
        boolean bAccept;

        pVariables.add("Name", "", "");
	    pVariables.add("Version", "", "");
 	    pVariables.add("Author", "", "");
 	    pVariables.add("About", "", "");
        pVariables.add("Case Sensitive", "", "");
        pVariables.add("Start Symbol", "", "");

        sdb.setFileType(fileHeader);
        if(sdb.openFile(fileName))
        {
            while(!sdb.done())
            {
				//System.out.println("stuck here?");
                success = sdb.getNextRecord();
                if(!success)
                {
                    break;
                }
                Object next = sdb.retrieveNext();

                //should be byte
                Integer byte1 = (Integer)next;
                int caseSelect = byte1.intValue();

				
                switch(caseSelect)
                {
                    case recordIdParameters:        //Name, Version, Author, About, Case-Sensitive
                        pVariables.setValue("Name", (String)sdb.retrieveNext());
 		                pVariables.setValue("Version", (String)sdb.retrieveNext());
         		        pVariables.setValue("Author", (String)sdb.retrieveNext());
		                pVariables.setValue("About", (String)sdb.retrieveNext());
                        Boolean caseS = (Boolean)sdb.retrieveNext();
            		    pVariables.setValue("Case Sensitive", "" + caseS.booleanValue());
                        Integer startS = (Integer)sdb.retrieveNext();
			            pVariables.setValue("Start Symbol", "" + startS.intValue());
                        break;

                    case recordIdTableCounts:       //Symbol, CharacterSet, Rule, DFA, LALR
                        Integer tabCount = (Integer)sdb.retrieveNext();
                        pSymbolTable.reDim(tabCount.intValue());
                        tabCount = (Integer)sdb.retrieveNext();
                        pCharacterSetTable.setSize(tabCount.intValue());
                        tabCount = (Integer)sdb.retrieveNext();
                        pRuleTable.setSize(tabCount.intValue());
                        tabCount = (Integer)sdb.retrieveNext();
                        pDFA.setSize(tabCount.intValue());
                        tabCount = (Integer)sdb.retrieveNext();
                        pActionTable.setSize(tabCount.intValue());
                        break;

                    case recordIdInitial:           //DFA, LALR
                        int[] retrieved = sdb.retrieve(2);
                        initialDFAState = retrieved[0];
                        initialLALRState = retrieved[1];
                        break;

           			case recordIdSymbols:           //#, Name, Kind
                        Symbol readSymbol = new Symbol();
                        nA = (Integer)sdb.retrieveNext();
                        n = nA.intValue();

                        readSymbol.setName((String)sdb.retrieveNext());

                        nA = (Integer)sdb.retrieveNext();
                        readSymbol.setKind(nA.intValue());

                        sdb.retrieveNext();        // empty
                        readSymbol.setTableIndex(n);

                        pSymbolTable.setMember(n, readSymbol);
						break;

           			case recordIdCharSets:          //#, Characters
                        nA = (Integer)sdb.retrieveNext();
                        n = nA.intValue();

                        String sA = (String)sdb.retrieveNext();
                        pCharacterSetTable.setElementAt(sA, n);
                        break;

           			case recordIdRules:             //#, ID#, Reserved, (Symbol#,  ...)
                        Rule readRule = new Rule();
                        nA = (Integer)sdb.retrieveNext();
                        n = nA.intValue();

                        readRule.setTableIndex(n);

                        nA = (Integer)sdb.retrieveNext();
                        readRule.setRuleNonTerminal(pSymbolTable.getMember(nA.intValue()));

                        sdb.retrieveNext();             // reserved
                        while(!sdb.retrieveDone())
                        {
                            nA = (Integer)sdb.retrieveNext();
                            readRule.addItem(pSymbolTable.getMember(nA.intValue()));
                        }

                        pRuleTable.setElementAt(readRule, n);
                        break;

           			case recordIdDFAStates:         //#, Accept?, Accept#, Reserved (Edge chars, Target#, Reserved)...
                        FAState readDFA = new FAState();
                        nA = (Integer)sdb.retrieveNext();
                        n = nA.intValue();

                        bA = (Boolean)sdb.retrieveNext();
                        bAccept = bA.booleanValue();

                        if(bAccept)
                        {
                            nA = (Integer)sdb.retrieveNext();
                            readDFA.acceptSymbol = nA.intValue();
                        }
                        else
                        {
                            readDFA.acceptSymbol = -1;
                            sdb.retrieveNext();         //discard value
                        }

                        sdb.retrieveNext();          // reserved

                        while(!sdb.retrieveDone())
                        {
                            Integer aChars = (Integer)sdb.retrieveNext();
                            nA = (Integer)sdb.retrieveNext();

                            readDFA.addEdge("" + aChars.intValue(), nA.intValue());

                            sdb.retrieveNext();     //reserved
                        }

                        pDFA.setElementAt(readDFA, n);
                        break;

           			case recordIdLRTables:          //#, Reserved (Symbol#, Action, Target#, Reserved)...
						//System.out.println("8");
                        LRActionTable readLALR = new LRActionTable();
                        nA = (Integer)sdb.retrieveNext();
                        n = nA.intValue();

                        sdb.retrieveNext();         // reserved
                        Integer action, target;

                        while(!sdb.retrieveDone())
                        {
                            nA = (Integer)sdb.retrieveNext();
                            action = (Integer)sdb.retrieveNext();
                            target = (Integer)sdb.retrieveNext();
                            readLALR.addItem(pSymbolTable.getMember(nA.intValue()),
											 action.intValue(),
											 target.intValue());

                            sdb.retrieveNext();     // reserved
                        }

                        pActionTable.setElementAt(readLALR, n);
                        break;

           			default:       //RecordIDComment
               			success = false;
                }
            }

            //====== Setup internal variables to reflect the loaded data
            //Reassign the numeric value to its name
       		pVariables.setValue("Start Symbol", pSymbolTable.getMember(Integer.parseInt(pVariables.getValue("Start Symbol"))).getName());

            if(pVariables.getValue("Case Sensitive").equals("true"))
				pIgnoreCase = false;
            else 
				pIgnoreCase = true;

            sdb.closeFile();

            return success;
        }
        else
        {
        	return false;
        }
    }

    /***************************************************************
 	 *
 	 * loadCompiledGrammar
 	 *
 	 * This method will reset the GOLDParser engine before loading
     * a new .cgt file into it.
 	 * @param fileName The absolute path of the .cgt file to load.
 	 * @return True if the .cgt was loaded, false if not.
 	 * @throws ParserException If there was a stream access problem with
     *          the file.
 	 ***************************************************************/
    public boolean loadCompiledGrammar(String fileName) throws ParserException
	{
        reset();
		return loadTables(fileName);
	}

    /***************************************************************
 	 *
 	 * openFile
 	 *
 	 * This method will open the source file for reading. It will also
     * reset all information from previous source files.
 	 * @param fileName The absolute path to the source file.
 	 * @return True if the file was successfully opened, false if not.
 	 * @throws ParserException If there was a problem opening the file.
 	 ***************************************************************/
    public boolean openFile(String fileName) throws ParserException
    {
        reset();
	    pSource = new LookAheadStream();
    	pSource.openFile(fileName);
        prepareToParse();

        return true;
    }

    /***************************************************************
 	 *
 	 * clear
 	 *
 	 * This method clears every value in the parser engine.
 	 ***************************************************************/
    public void clear()
    {
        pSymbolTable.clear();
        pRuleTable.clear();
        pCharacterSetTable.clear();
        pVariables.clearValues();
        pTokens.clear();
        pInputTokens.clear();

        reset();
    }

    /***************************************************************
 	 *
 	 * reset
 	 *
 	 * This method will reset the parser engine. It initalises the
     * Error and Type End symbols, and then clears all the stacks
     * of any tokens.
 	 ***************************************************************/
    public void reset()
    {
        Token start = new Token();

        for(int i=0; i<pSymbolTable.count(); i++)
        {
            int swKind = pSymbolTable.getMember(i).getKind();
            switch(swKind)
            {
                case symbolTypeError:
                    kErrorSymbol = pSymbolTable.getMember(i);
                    break;

                case symbolTypeEnd:
                    kEndSymbol = pSymbolTable.getMember(i);
                    break;
            }
        }

        currentLALR = initialLALRState;
        pLineNumber = 1;
        //if(pSource != null)
        //{
        	//pSource.closeFile();
        //}
        pCommentLevel = 0;
        pHaveReduction = false;

        pTokens.clear();
        pInputTokens.clear();
        stack.clear();

        start.setState(initialLALRState);
        start.setParentSymbol(pSymbolTable.getMember(0));

        stack.push(start);
    }

    /***************************************************************
 	 *
 	 * parse
 	 *
     * Will parse a token.
 	 * 1. If the tables are not setup then report GPM_NotLoadedError<br>
     * 2. If parser is in comment mode then read tokens until a recognized one is found and report it<br>
     * 3. Otherwise, parser normal<br>
     *  	 a. If there are no tokens on the stack
	 *           1) Read one and trap error
	 *           2) End function with GPM_TokenRead
	 *       b. Otherwise, call ParseToken with the top of the stack.
	 *           1) If success, then Pop the value
	 *           2) Loop if the token was shifted (nothing to report)
     *
 	 * @return The result of one parse of the source file. The integer could
     *              be one of the constants defined in the interface 
	 *				GPMessageConstants.
 	 * @throws ParserException This is thrown if there are any problems
     *          reading information from the source file.
 	 ***************************************************************/
    public int parse() throws ParserException
    {
        int result = 0; 			// from interface GPMessageConstants
        boolean done = false;
        Token readToken;
        int parseResult;

        if((pActionTable.size() < 1) | (pDFA.size() < 1))
        {
            result = gpMsgNotLoadedError;
        }
        else
        {
            while(!done)
            {
                if(pInputTokens.size() == 0) // we must read a token
                {
                    readToken = retrieveToken(pSource);
                    if(readToken == null)
                    {
                        result = gpMsgInternalError;
                        done = true;
                    }
                    else
                    {
                        if(readToken.getKind() != symbolTypeWhitespace)
                        {
                            pInputTokens.push(readToken);
                            if((pCommentLevel == 0) &
							   (readToken.getKind() != symbolTypeCommentLine) &
                               (readToken.getKind() != symbolTypeCommentStart))
                            {
                                result = gpMsgTokenRead;
                                done = true;
                            }
                        }
                    }
                }
                else
                {
                    if(pCommentLevel > 0)   // we are in a block comment
                    {
                        readToken = (Token)pInputTokens.pop();

                        switch(readToken.getKind())
                        {
                            case symbolTypeCommentStart:
                                pCommentLevel++;
                                break;

                            case symbolTypeCommentEnd:
                                pCommentLevel--;
                                break;

                            case symbolTypeEnd:
                                result = gpMsgCommentError;
                                done = true;
                                break;

                            default:
                                // do nothing - ignore
                                // the 'comment line' symbol is ignored as well
                        }
                    }
                    else
                    {
                        readToken = (Token)pInputTokens.peek();

                        switch(readToken.getKind())
                        {
                            case symbolTypeCommentStart:
                                pCommentLevel++;
                                pInputTokens.pop();     // remove it
                                break;

                            case symbolTypeCommentLine:
                                pInputTokens.pop();     // remove it
                                discardRestOfLine();    // and rest of line
                                break;

                            case symbolTypeError:
                                result = gpMsgLexicalError;
                                done = true;
                                break;

                            default:                    //FINALLY, we can parse the token
                                parseResult = parseToken(readToken);
                                //NEW 12/2001: Now we are using the internal enumerated constant ParseResult

                                switch(parseResult)
                                {
                                    case parseResultAccept:
                                        result = gpMsgAccept;
                                        done = true;
                                        break;

                                    case parseResultInternalError:
                                        result = gpMsgInternalError;
                                        done = true;
                                        break;

                                    case parseResultReduceNormal:
                                        result = gpMsgReduction;
                                        done = true;
                                        break;

                                    case parseResultShift:     //A simple shift, we must continue
                                        pInputTokens.pop();    //Okay, remove the top token, it is on the stack
                                        break;

                                    case parseResultSyntaxError:
                                        result = gpMsgSyntaxError;
                                        done = true;
                                        break;

                                    default:
                                        // do nothing
                                        break;
                                }
                                break;
                        }
                    }
                }
            }
        }
        return result;
    }

    // this method is not accessible.
    private int parseToken(Token nextToken)
    {
        //This function analyzes a token and either:
	    //  1. Makes a SINGLE reduction and pushes a complete Reduction object on the stack
		//  2. Accepts the token and shifts
	    //  3. Errors and places the expected symbol indexes in the Tokens list
	    //The Token is assumed to be valid and WILL be checked
	    //If an action is performed that requires controlt to be returned to the user, the function returns true.
		//The Message parameter is then set to the type of action.

        // modified to use ParseResultConstants

        int n; boolean found; int index; int ruleIndex; Rule currentRule;
        String str; Token head; Reduction newReduction;

        int returnInt = -1;

        LRActionTable lrAct = (LRActionTable)pActionTable.elementAt(currentLALR);
        index = lrAct.actionIndexForSymbol(nextToken.getPSymbol().getTableIndex());

        if(index != -1)   //Work - shift or reduce
        {
            pHaveReduction = false;  //Will be set true if a reduction is made
            pTokens.clear();

            switch(lrAct.item(index).actionConstant)
            {
                case actionAccept:
                    pHaveReduction = true;
                    returnInt = parseResultAccept;
                    break;

                case actionShift:
                    currentLALR = lrAct.item(index).value;
                    nextToken.setState(currentLALR);
                    stack.push(nextToken);
                    returnInt = parseResultShift;
                    break;

                case actionReduce:
                    //Produce a reduction - remove as many tokens as members in the rule & push a nonterminal token
                    ruleIndex = lrAct.item(index).value;
                    currentRule = (Rule)pRuleTable.elementAt(ruleIndex);

                    //======== Create Reduction
                    if(pTrimReductions & currentRule.containsOneNonTerminal())
                    {
                    	//NEW 12/2001
	  	                //The current rule only consists of a single nonterminal and can be trimmed from the
        		        //parse tree. Usually we create a new Reduction, assign it to the Data property
		                //of Head and push it on the stack. However, in this case, the Data property of the
        		        //Head will be assigned the Data property of the reduced token (i.e. the only one
		                //on the stack).
        		        //In this case, to save code, the value popped of the stack is changed into the head.
                        head = (Token)stack.pop();
                        head.setParentSymbol(currentRule.getRuleNonTerminal());

                        returnInt = parseResultReduceEliminated;
                    }
                    else        //Build a Reduction
                    {
                        pHaveReduction = true;
                        newReduction = new Reduction();
                        newReduction.setParentRule(currentRule);
                        newReduction.setTokenCount(currentRule.getSymbolCount());

                        for(n = newReduction.getTokenCount() - 1; n >= 0; n--)
        	            {
    	                    newReduction.setToken(n, (Token)stack.pop());
	                    }

                        head = new Token();
                        head.setData(newReduction);
                        head.setParentSymbol(currentRule.getRuleNonTerminal());

                        returnInt = parseResultReduceNormal;
                    }

                    //========== Goto
                    Token topStack = (Token)stack.peek();
                    index = topStack.getState();

                    //========= If n is -1 here, then we have an Internal Table Error!!!!
                    lrAct = (LRActionTable)pActionTable.elementAt(index);
                    n = lrAct.actionIndexForSymbol(currentRule.getRuleNonTerminal().getTableIndex());

                    if(n != -1)
                    {
                        currentLALR = lrAct.item(n).value;

                        //========== Push Head
			            //======== Get new head - Take action for invalid data
                        head.setState(currentLALR);

                        stack.push(head);
                    }
                    else
                    {
                        returnInt = parseResultInternalError;
                    }
                    break;
            }
        }
        else
        {
        	//=== Syntax Error!
            pTokens.clear();

            lrAct = (LRActionTable)pActionTable.elementAt(currentLALR);
            for(n = 0; n < (lrAct.count() - 1); n++)
            {
                if(lrAct.item(n).getSymbol().getKind() == symbolTypeTerminal)
                {
                    head = new Token();
                    head.setData(new String(""));
                    head.setParentSymbol(lrAct.item(n).getSymbol());
                    pTokens.push(head);
                }
            }

            returnInt = parseResultSyntaxError;
        }

        return returnInt;
    }

    // this method is not accessible. It will throw an ParserException if there was
    // a problem reading the source file.
    private Token retrieveToken(LookAheadStream source) throws ParserException
    {
        //This function implements the DFA algorithm and returns a token to the LALR state
   		//machine

        String ch; //Temporary storage for a character read by the DFA algorithm

        int n, currentDFA;

        boolean found; //Used to exit a branch search loop. For each state in the DFA
					   //there are a finite number of branches to other states. The code
					   //under "Move to next state" searches the branches until one is
					   //found containing the character (ch).

        boolean done = false; //Used in the main loop. When the next character is not 
							  //found in the DFA, this variable is set true and the loop 
							  //exits. In addition, based on whether a matching token 
							  //could be found, the code either creates an error token or 
							  //returns the new one.

        int target = 0; //Temporary storage that used to hold the index of the next state in
					    //the DFA. In other words, the state we are now moving to.

        int charSetIndex; //Temporary storeage that is used to hold the index in the 
						  //Character Set. This isn't really necessary, but it does save 
						  //code, and thus, sanity! :-)

        int currentPosition; //This is the current read-ahead position on the source string.
							 //The DFA algorithm uses lookahead logic to analyze the souce
							 //string. Basically, I set the value of the value initially
							 //to 1 since we are looking at the next character in the
							 //input. Each time a character is matched in the algorithm
							 //and the DFA State advances, so too does the position in the
							 //source input.

        int lastAcceptState; //Whenever a state is found that accepts a token, it is marked. 
							 //Essentially this follows the algorithms desire to find the longest
							 //matching token possible.

        int lastAcceptPosition; //This is set at the same time as the LastAcceptState to
								//store the read-ahead position in the source that contains the
								//token. For instance, if parsing the string

        Token result = new Token();

        currentDFA = initialDFAState;    //The first state is almost always #1.
        currentPosition = 1;             //Next byte in the input stream
        lastAcceptState = -1;            //We have not yet accepted a character string
        lastAcceptPosition = -1;

        boolean inWhiteSpaceCharSet = false; //we need to doubly make sure!

        if(!source.done())
        {
            while(!done)
            {
                //======= This code searches all the branches of the current DFA state for the next
         		//======= character in the input stream. If found the target state is returned.
         		//======= The InStr() function searches the string pCharacterSetTable.Member(CharSetIndex)
         		//======= starting at position 1 for ch.  The pCompareMode variable determines whether
         		//======= the search is case sensitive.
                ch = source.nextChar();

                if(ch.equals("")) //End reached, do not match
                {
                    found = false;
                }
                else
                {
                    n = 0;
                    found = false;

                    FAState faTmp = (FAState)pDFA.elementAt(currentDFA);
                    while((n < faTmp.edgeCount()) & (!found))
                    {
                        charSetIndex = Integer.parseInt(faTmp.edge(n).getChars());
                        String sT = (String)pCharacterSetTable.elementAt(charSetIndex);

                        for(int i=0; i<sT.length(); i++)
                        {
                            int k = (int)sT.charAt(i);
                            int x = (int)ch.charAt(0);

                            if(k == 10)
                            {
                                // we are in whitespace land
                                inWhiteSpaceCharSet = true;
                                if(x == k)
                                {
                                    // the next character is whitespace!
                                    // but we must check to see if the character
                                    // after that is whitespace, if it is not then
                                    // we don't want to include it in our whitespace
                                    // token!
                                    if(!source.nextCharNotWhitespace())
                                    {
                                        found = true;
                                    	target = faTmp.edge(n).getTargetIndex();
                                    }
                                }
                            }
                        }

                        boolean checkerFound = false;

                        char checker = ch.charAt(0);
                        char sTChar;

                        for(int i=0; i<sT.length(); i++)
                        {
	                        sTChar = sT.charAt(i);

        	                if(pIgnoreCase)
            	            {
                		        checker = Character.toLowerCase(checker);
                        		sTChar  = Character.toLowerCase(sTChar);
	                        }

    	                    if(checker == sTChar)
        	                {
            		            checkerFound = true;
                    		    i = sT.length(); // finish the loop quickly
	                        }
                        }

                        if(checkerFound)
                        {
        	                found = true;
    		                target = faTmp.edge(n).getTargetIndex();
                        }
                        n++;
                    }
                }

                //======= This block-if statement checks whether an edge was found from the current state.
         		//======= If so, the state and current position advance. Otherwise it is time to exit the main loop
         		//======= and report the token found (if there was it fact one). If the LastAcceptState is -1,
         		//======= then we never found a match and the Error Token is created. Otherwise, a new token
         		//======= is created using the Symbol in the Accept State and all the characters that
         		//======= comprise it.

                if(found)
                {
                    //======= This code checks whether the target state accepts a token. If so, it sets the
		            //======= appropiate variables so when the algorithm in done, it can return the proper
        		    //======= token and number of characters.
                    FAState faTmp2 = (FAState)pDFA.elementAt(target);
                    if(faTmp2.acceptSymbol != -1)
                    {
                        lastAcceptState = target;
                        lastAcceptPosition = currentPosition;
                    }

                    currentDFA = target;
                    currentPosition++;
                }
                else     //No edge found
                {
                    done = true;

		            if(lastAcceptState == -1) //Tokenizer cannot recognize symbol
    		        {
                        if(pCommentLevel == 0)
                        {
		        		    result.setParentSymbol(kErrorSymbol);
    		        		result.setData(source.read(1));
                        }
                        else
                        {
                            done = false;
                        }
                	}
	                else
		            {
    			        FAState faTmp3 = (FAState)pDFA.elementAt(lastAcceptState);
        			    result.setParentSymbol(pSymbolTable.getMember(faTmp3.acceptSymbol));
            			result.setData(source.read(lastAcceptPosition));
	                }
                }
            }
        }
        else
        {
            result.setData(new String(""));   //End of file reached, create End Token
            result.setParentSymbol(kEndSymbol);
        }

        //======= Count Carriage Returns and increment the Line Number. This is done for the
		//======= Developer and is not necessary for the DFA algorithm

        String strTmp = (String)result.getData();
        for(n = 0; n < strTmp.length(); n++)
        {
            if(strTmp.charAt(n) == '\n')
            {
                pLineNumber++;
            }
        }
        return result;
    }
}
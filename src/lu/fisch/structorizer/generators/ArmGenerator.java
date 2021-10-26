/*
    Structorizer
    A little tool which you can use to create Nassi-Schneiderman Diagrams (NSD)

    Copyright (C) 2009, 2020  Bob Fisch

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
// VERSION 2.1

package lu.fisch.structorizer.generators;

/******************************************************************************************************
*
*      Author:         Alessandro Simonetta et al.
*
*      Description:    Generator class for ARM code
*
******************************************************************************************************
*
*      Revision List
*
*      Author          Date            Description
*      ------          ----            -----------
*      See @author     2021-03-25      Provided per Pull request on Enh. #967
*      A. Simonetta    2021-04-02      Several revisions as requested
*      Kay Gürtzig     2021-04-09      Syntax correction, some adaptations to fit into Structorizer environment
*      Kay Gürtzig     2021-04-14      Issue #738: Highlighting map faults mended
*      Kay Gürtzig     2021-04-15      Gnu mode now obtained from plugin option rather than Element field
*      A. Simonetta    2021-04-23      Input and output and some parsing flaws fixed
*      Kay Gürtzig     2021-04-24/26   Some corrections to the fixes of A. Simonetta
*      Kay Gürtzig     2021-04-30      Problem with too many variables fixed.
*      Kay Gürtzig     2021-05-02      Mechanisms added to support EXIT instructions, subroutines, CALLs
*      Kay Gürtzig     2021-05-11      Appended an endless loop to the end of a program
*      Kay Gürtzig     2021-10-05      Condition handling for Alternative, While, and Repeat unified and delegated
*      Kay Gürtzig     2021-10-06      Arm Instruction detection revised.
*      Kay Gürtzig     2021-10-11      Risk of NullPointerException in getVariables() averted, some
*                                      code revisions in the variable and statement detection.
*      Kay Gürtzig     2021-10-26      Bugfix #1003: Undue memory reservation for all variables.
*
******************************************************************************************************
*
*      Comment:
*      TODO: - Register recycling (e.g. via LRU) -> all variables need an address in memory/stack then
*            - Compilation of more complex expressions
*            - How to return to the OS (or to prevent main from running into the subroutines?)
*
******************************************************************************************************///

import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.parsers.CodeParser;
import lu.fisch.utils.StringList;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Description: This class generates ARM code.
 * @author Daniele De Menna
 * @author Robert Dorinel Milos
 * @author Alessandro Simonetta,
 * @author Giulio Palumbo
 * @author Maurizio Fiusco
 */
public class ArmGenerator extends Generator {

    // Instruction patterns
    // START KGU#968 2021-05-02: More general variable syntax - might this cause trouble?
    //private static final String registerPattern = " ?[Rr]([0-9]|1[0-4]) ?";
    //private static final String variablePattern = "[a-zA-Z]+[0-9]*";
    private static final String registerPattern0 = "[Rr]([0-9]|1[0-4])";
    private static final String registerPattern1 = "[Rr]([0-9]|1[0-5])";	// Includes PC
    private static final String registerPattern = " ?" + registerPattern0 + " ?";
    private static final String variablePattern = "[a-zA-Z][a-zA-Z0-9_]*";
    // END KGU#968 2021-05-02
    private static final String numberPattern = "-?[0-9]+";
    private static final String hexNumberPattern = "(0|0x|0x([0-9]|[a-fA-F])+)";
    private static final String assignmentOperators = "(<-|:=)";
    private static final String relationOperators = "(==|!=|<|>|<=|>=|=)";
    private static final String supportedOperationsPattern = "(-|\\+|\\*|and|or|&|\\||&&|\\|\\|)";
    private static final String registerVariableNumberHex = String.format("(%s|%s|%s|%s)", registerPattern, variablePattern, numberPattern, hexNumberPattern);
    private static final String negativeNumberPattern = "-[0-9]+";

    private static final Pattern assignment = Pattern.compile(String.format("(%s|%s) *%s *%s", registerPattern, variablePattern, assignmentOperators, registerVariableNumberHex));
    private static final Pattern expression = Pattern.compile(String.format("(%s|%s) *%s *%s *%s *%s", registerPattern, variablePattern, assignmentOperators, registerVariableNumberHex, supportedOperationsPattern, registerVariableNumberHex));
    private static final Pattern memoryAccess = Pattern.compile(String.format("(%s|%s) *%s *(memoria|memory)\\[(%s|%s)( *\\+ *%s)?\\]", registerPattern, variablePattern, assignmentOperators, registerPattern, variablePattern, registerVariableNumberHex));
    private static final Pattern memoryStore = Pattern.compile(String.format("(memoria|memory)\\[(%s|%s|%s)( *\\+ *%s)?\\] *%s *(%s|%s)", registerPattern, variablePattern, numberPattern, registerVariableNumberHex, assignmentOperators, registerPattern, variablePattern));
    private static final Pattern arrayExpression = Pattern.compile(String.format("(%s|%s) *%s *(%s|%s)\\[(%s|%s|%s)\\]", registerPattern, variablePattern, assignmentOperators, registerPattern, variablePattern, registerPattern, variablePattern, numberPattern));
    private static final Pattern arrayAssignment = Pattern.compile(String.format("(%s|%s)\\[(%s|%s|%s)( *\\+ *%s)?\\] *%s *(%s|%s)", registerPattern, variablePattern, registerPattern, variablePattern, numberPattern, registerVariableNumberHex, assignmentOperators, registerPattern, variablePattern));
    // START KGU#968 2021-10-11: Issue #967 it can hardly make sense to have a number on the left-hand side
    private static final Pattern arrayInitialization = Pattern.compile(String.format("(word|hword|byte|octa|quad) *(%s|%s|%s) *%s *\\{(%s|%s)(, *(%s|%s))*\\}", registerPattern, variablePattern, numberPattern, assignmentOperators, numberPattern, hexNumberPattern, numberPattern, hexNumberPattern));
    //private static final Pattern arrayInitialization = Pattern.compile(String.format("(word|hword|byte|octa|quad) +(%s|%s) *%s *\\{(%s|%s)(, *(%s|%s))*\\}", registerPattern, variablePattern, assignmentOperators, numberPattern, hexNumberPattern, numberPattern, hexNumberPattern));
    // END KGU#968 2021-10-11
    private static final Pattern address = Pattern.compile(String.format("%s *%s *(indirizzo|address)\\((%s|%s)\\)", registerPattern, assignmentOperators, registerPattern, variablePattern));
    // FIXME KGU#968: Why isn't an empty string allowed? Why are only identifier characters supported as content?
    private static final Pattern stringInitialization = Pattern.compile(String.format("(%s|%s) *%s *\"[\\w]{2,}\"", registerPattern, variablePattern, assignmentOperators));
    // START KGU#968 2021-10-11: Issue #967 Single quotes shall also be supported (preferrably even!)
    //private static final Pattern charInitialization = Pattern.compile(String.format("(%s|%s) *%s *\"[\\w]\"", registerPattern, variablePattern, assignmentOperators));
    private static final Pattern charInitialization = Pattern.compile(String.format("(%s|%s) *%s *(\"[\\w]\"|'[\\w]')", registerPattern, variablePattern, assignmentOperators));
    // END KGU#968 2021-10-11
    private static final Pattern booleanAssignmentPattern = Pattern.compile(String.format("(%s|%s) *%s *(true|false)", registerPattern, variablePattern, assignmentOperators));
    // START KGU#968 2021-05-02: More general variable syntax - might this cause trouble?
    //private final Pattern conditionPattern = Pattern.compile("(while)?\\((R([0-9]|1[0-5])|[a-zA-Z]+)(==|!=|<|>|<=|>=|=)(R([0-9]|1[0-5])|[0-9]+|[a-zA-Z]+|0x([0-9]|[a-fA-F])+|'([a-zA-Z]|[0-9])')((and|AND|or|OR|&&|\\|\\|)(R([0-9]|1[0-5])|[a-zA-Z]+)(==|!=|<|>|<=|>=|=)(R([0-9]|1[0-5])|[0-9]+|[a-zA-Z]+|0x([0-9]|[a-fA-F])+|'([a-zA-Z]|[0-9])'))*\\)");
    private static final String comparisonPattern = String.format("(%s|%s)%s(%s|[0-9]+|%s|0x[0-9a-fA-F]+|'[a-zA-Z0-9]')",
            registerPattern1, variablePattern,
            relationOperators,
            registerPattern1, variablePattern);
    private static final Pattern conditionPattern = Pattern.compile(
            String.format("\\(%s((&&|\\|\\|)%s)*\\)",
                    comparisonPattern, comparisonPattern));
    // END KGU#968 2021-05-02
    // START KGU#968 2021-10-05: Special support for [negated] registers or variables as conditions
    private static final Pattern atomicCondPattern = Pattern.compile(
            String.format("\\(!?(%s|%s)\\)",
                    registerPattern1, variablePattern));
    // END KGU#968 2021-10-05
    // START KGU#968 2021-04-24: Enh. #967 - correct keyword comparison; patterns will be set when code generation is started
    private static Pattern inputPattern = null;
    private static Pattern outputPattern = null;
    // END KGU#968 2021-04-24
    
    // START KGU#968 2021-10-06: Revised from a local variable in isArmInstruction()
    private static final String[] ARM_INSTRUCTIONS = {
            "lsl", "lsr", "asr", "ror", "rrx", "adcs", "and", "eor", "sub", "rsb", "add", "adc",
            "sbc", "rsc", "bic", "orr", "mov", "tst", "teq", "cmp", "cmn", "sel", "mul", "mla",
            "smla", "smuadx", "smlsd", "smmla", "smmls", "mrs", "msr", "b", "ldr", "str", "ldm",
            "stm", "cpsie", "cpsid", "srs", "rfe", "setend", "cdp", "ldc", "stc", "mcr", "mrc",
            "mrrc", "swi", "bkpt", "pkhbt", "pkhtb", "sxtb", "sxth", "uxtb", "uxth", "sxtab",
            "sxtah", "uxtab", "uxtah", "ssat", "usat", "rev", "clz", "cpy", "cdc"
    };
    // This set will contain all the strings from ARM_INSTRUCTIONS
    private static final HashSet<String> ARM_INSTR_LOOKUP = new HashSet<String>();
    // END KGU#968 2021-10-06

    /**
     * Enum type for getMode()
     */
    private enum ARM_OPERATIONS {
        ASSIGNMENT,
        EXPRESSION,
        MEMORY,
        ARRAY_EXPRESSION,
        ARRAY_ASSIGNMENT,
        ARRAY_INITIALIZATION,
        ADDRESS,
        BOOLEAN_ASSIGNMENT,
        STRING_ARRAY_INITIALIZATION,
        CHAR_ARRAY_INITIALIZATION,
        INSTRUCTION,
        INPUT,
        OUTPUT,
        NOT_IMPLEMENTED
    }

    // Reserved words that can't be used as variables
    private static final String[] reservedWords = {"and", "or", "memoria", "memory", "indirizzo", "address", "true", "false", "word", "hword", "bytes", "quad", "octa"/*, "input", "output", "INPUT", "OUTPUT"*/};
    // HashMap used for available registers and already assigned variables
    // FIXME: The way it is used it could as well be an array of Strings
    private static final HashMap<String, String> mVariables = new HashMap<>();
    static {
        mVariables.put("R0", "");
        mVariables.put("R1", "");
        mVariables.put("R2", "");
        mVariables.put("R3", "");
        mVariables.put("R4", "");
        mVariables.put("R5", "");
        mVariables.put("R6", "");
        mVariables.put("R7", "");
        mVariables.put("R8", "");
        mVariables.put("R9", "");
        mVariables.put("R10", "");
        mVariables.put("R11", "");
        mVariables.put("R12", "");
    }

    private static final String USER_REGISTER_TAG = "ALREADY_USED_BY_THE_USER";

    public static class Tuple<X, Y> {
        public final X variable;
        public final Y position;

        public Tuple(X x, Y y) {
            this.variable = x;
            this.position = y;
        }

        @Override
        public String toString() {
            return "Tuple{" +
                    "variable=" + variable +
                    ", position=" + position +
                    '}';
        }
    }


    /**
     * If the variable is true then the generator will translate the code for the GNU compiler
     * else the generator will translate the code for the Keil compiler
     */
    // START KGU#968 2021-04-15: Setting now comes from an export options
    //private final boolean gnuEnabled = Element.ARM_GNU;
    private boolean gnuEnabled = false;
    // END KGU#968 2021-04-15

    /**
     * Variable used for naming arrays (v_0, v_1...)
     */
    private int arrayCounter = 0;
    /**
     * Variable used for naming the different labels (then, else, default, block, for, while, do)
     */
    private int COUNTER = 0;
    // START KGU#968 2021-05-02: Support for EXIT instructions from loops (refers to jumpTable)
    private String[] breakLabels = null;
    // END KGU#968 2021-05-02

    /**
     * Stores the difference between GNU and Keil compilers<br/>
     * First index:<br/>
     * [0] - Gnu phrases<br/>
     * [1] - KEIL phrases<br/>
     * Second index:<br/>
     * [0] - label declaration postfix<br/>
     * [1] - direct operand prefix in MOV instructions<br/>
     * [2] - data area header<br/>
     * [3] - text area header<br/>
     */
    private final String[][] difference = {
            {":", "#", ".data", ".text"},
            {"", "", ";AREA data, DATA, READWRITE", ";AREA text, CODE, READONLY"}
    };

    @Override
    protected String getDialogTitle() {
        return "Export ARM ...";
    }

    @Override
    protected String getFileDescription() {
        return "ARM Assembly code";
    }

    @Override
    protected String getIndent() {
        return "\t\t";
    }

    @Override
    protected String[] getFileExtensions() {
        return new String[]{"txt"};
    }

    @Override
    protected String commentSymbolLeft() {
        return gnuEnabled ? "//" : ";";
    }

    @Override
    protected OverloadingLevel getOverloadingLevel() {
        return OverloadingLevel.OL_NO_OVERLOADING;
    }

    @Override
    protected boolean breakMatchesCase() {
        return false;
    }

    @Override
    protected String getIncludePattern() {
        return "";
    }

    @Override
    protected TryCatchSupportLevel getTryCatchLevel() {
        return TryCatchSupportLevel.TC_TRY_CATCH_FINALLY;
    }

    @Override
    protected String getInputReplacer(boolean withPrompt) {
        return "LDR $1";
    }

    @Override
    protected String getOutputReplacer() {
        return "STR $1";
    }

    @Override
    public String generateCode(Root _root, String _indent, boolean _public) {
        // START KGU#968 2021-04-15: New mechanism to retrieve gnuEnabled option
        Object optionGnu = this.getPluginOption("gnuCode", gnuEnabled);
        if (optionGnu instanceof Boolean) {
            gnuEnabled = (Boolean) optionGnu;
        }
        // END KGU#968 2021-04-15
        // START KGU#968 2021-04-24: Enh. #967 - prepare correct keyword comparison
        String inputKeyword = CodeParser.getKeywordOrDefault("input", "input");
        String outputKeyword = CodeParser.getKeywordOrDefault("output", "output");
        String procName = _root.getMethodName();
        inputPattern = Pattern.compile(getKeywordPattern(inputKeyword) + "([\\W].*|$)");
        outputPattern = Pattern.compile(getKeywordPattern(outputKeyword) + "([\\W].*|$)");
        alwaysReturns = mapJumps(_root.children);
        this.varNames = _root.retrieveVarNames().copy();
        this.isResultSet = varNames.contains("result", false);
        this.isFunctionNameSet = varNames.contains(procName);
        // END KGU#968 2021-04-24
        // START KGU#705 2021-04-14: Enh. #738 (Direct code changes compromise codeMap)
        //if (topLevel && gnuEnabled) {
        //    code.add(difference[0][2]);
        //    code.add(difference[0][3] + "\n");
        //} else if (topLevel) {
        //    code.add(difference[1][2]);
        //    code.add(difference[1][3] + "\n");
        //}
        int line0 = code.count();
        if (codeMap!= null) {
            // register the triple of start line no, end line no, and indentation depth
            // (tab chars count as 1 char for the text positioning!)
            codeMap.put(_root, new int[]{line0, line0, _indent.length()});
        }
        int variant = gnuEnabled ? 0 : 1;
        if (topLevel) {
            addCode(difference[variant][2], "", false);
            addCode(difference[variant][3], "", false);
            addCode("", "", false);	// Just a newline
        }
        String colon = difference[variant][0];
        // END KGU#705 2021-04-14

        for (Map.Entry<String, String> entry : mVariables.entrySet()) {
            // START KGU#968 2021-10-11 Reserve all register names used as variables
            //mVariables.put(entry.getKey(), "");
            String reg = entry.getKey();
            if (this.varNames.contains(reg, false)) {
                mVariables.put(reg, USER_REGISTER_TAG);
            }
            else {
                mVariables.put(reg, "");
            }
            // END KGU#968 2021-10-11
        }
        // START KGU#968 2021-05-02: EXITs, subroutines
        // Support for loop EXITs - map the ARM loop labels
        this.breakLabels = new String[this.labelCount];
        // START KGU#999 2021-10-26: Bugfix #1003 This was just a draft for an immature idea...
        //for (int i = 0; i < this.varNames.count(); i++) {
        //    String varName = varNames.get(i);
        //    if (topLevel && _root.isProgram()) {
        //        // FIXME reserved size should be type-dependant! Think of arrays in particular!
        //        insertCode(getIndent() + ".space 4", 1);
        //        insertCode(varName + colon, 1);
        //    }
        //    // FIXME - we should reserve space on stack and a register for address operations 
        //    //if (i < 12) {
        //    //    getRegister(this.varNames.get(i));
        //    //}
        //}
        // END KGU#999 2021-10-26
        if (_root.isSubroutine()) {
            addCode(procName + colon, "", false);
            // FIXME: Adhere to the GNU call standard 
            // Push all registers (FIXME: Could we reduce the register set to the actual needs?)
            addCode("STMFD SP!, {R0-R12}", getIndent(), false);
            // Now get the arguments from the stack
            StringList parNames = _root.getParameterNames();
            int nPars = parNames.count();
            for (int i = nPars - 1; i >= 0; i--) {
                String regPara = getRegister(parNames.get(i));
                addCode("LDR " + regPara + ", [SP,#" + (nPars - i + 14) + ",LSL #2]", getIndent(), false);
            }
        }
        // END KGU#968 2021-05-02

        generateBody(_root, _indent);

        // START KGU#968 2021-05-02: EXITs, subroutines
        // Get the last non-empty line
        int i = code.count() - 1;
        while (i >= 0 && code.get(i).trim().isEmpty()) {
            i--;
        }
        // Add a return mechanism if the code does not end with return anyway
        if (_root.isSubroutine() && !this.alwaysReturns && i >= 0
                && !code.get(i).trim().equals("MOVS PC, LR")) {
            // FIXME: Adhere to the GNU call standard 
            // Provide the result value if this is a function
            String regResult = "";
            if (this.isFunctionNameSet) {
                regResult = getRegister(procName);
            }
            else if (this.isResultSet) {
                int ixRes = this.varNames.indexOf("result", false);
                if (ixRes >= 0) {
                    regResult = getRegister(this.varNames.get(ixRes));
                }
            }
            if (!regResult.isEmpty()) {
                addCode(String.format("STR %s, [SP,#13,#2]", regResult), getIndent(), false);
            }
            // Pop all registers (FIXME: might be more restricted to the needs)
            addCode("LDMFD SP!, {R0-R12}", getIndent(), false);
            addCode("MOVS PC, LR", getIndent(), false);
        }
        addSepaLine();
        // END KGU#968 2021-05-02

        // START KGU#968 2021-05-11: Issue #967
        // Somehow we must end the main program, in particular if subroutines will follow
        if (_root.isProgram()) {
            // Add an endless loop to the end of a main program
            appendComment("Endless loop generated at the end of program", getIndent());
            addCode("stop" + procName + colon, "", false);
            addCode("B stop" + procName, getIndent(), false);
        }
        // END KGU#968 2021-05-02
        // START KGU#705 2021-04-14: Enh. #738
        if (codeMap != null) {
            // Update the end line no relative to the start line no
            codeMap.get(_root)[1] += (code.count() - line0);
        }
        // END KGU#705 2021-04-14
        if (topLevel) {
            this.subroutineInsertionLine = code.count();
        }
        return code.getText();
    }

    @Override
    protected void generateCode(Instruction _inst, String _indent) {
        appendComment(_inst, _indent + getIndent());

        boolean isDisabled = _inst.isDisabled(true);

        if (!appendAsComment(_inst, _indent)) {
            StringList lines = _inst.getUnbrokenText();
            for (int i = 0; i < lines.count(); i++) {
                String line = lines.get(i);
                // START KGU#968 2021-10-06: skip type definitions and declarations
                //generateInstructionLine(line, isDisabled);
                if (!Instruction.isMereDeclaration(line)) {
                    generateInstructionLine(line, isDisabled);
                }
                // END KGU#968 2021-10-06
            }
        }
    }

    @Override
    protected void generateCode(Alternative _alt, String _indent) {
        String colon = difference[gnuEnabled ? 0 : 1][0];
        
        // the local caching of the COUNTER variable is essential
        boolean isDisabled = _alt.isDisabled(true);
        appendComment(_alt, _indent + getIndent());
        // The local caching of COUNTER is essential here because multiCondition will update it
        int counter = COUNTER;

        String k = "end";

        // Check if the False branch exists (we won't empty code blocks)
        if (_alt.qFalse.getSize() != 0) {
            k = "else";
        } else {
            k = "end";
        }
        String[] keys = {k, "then"};
        // Generate the alternative code with multiCondition
        String c = processCondition(_alt, "if", keys, true);
        if (c == null) {
            return;
        }

        // START KGU#968 2021-04-25: Issue #967 c might contain newlines - which compromises line mapping
        //addCode(c, "", isDisabled);
        String[] cSplit = c.split("\\n");
        for (int i = 0; i < cSplit.length; i++) {
            addCode(cSplit[i], "", isDisabled);
        }
        // END KGU#968 2021-04-25

        if (_alt.qTrue.getSize() != 0) {
            // If "then" block is not empty then we add the label
            addCode("then_" + counter + colon, "", isDisabled);
            // Generate the code in the then block
            generateCode(_alt.qTrue, "");

            if (_alt.qFalse.getSize() != 0) {
                addCode("B end_" + counter, getIndent(), isDisabled);
            }
        }

        // Check the empty blocks for adding the right labels and the branch instructions
        // FIXME The branch statement should better be enclosed in a block.
        // We don't understand
        if (_alt.qFalse.getSize() != 0) {
            addCode("else_" + counter + colon, "", isDisabled);
            generateCode(_alt.qFalse, "");
        }
        // Adding the end labels at the end of the code
        addCode("end_" + counter + colon, "", isDisabled);
        // Remove the empty labels that were added (we could do it better)
        unifyFlow();
    }

	@Override
    protected void generateCode(Case _case, String _indent) {
        appendComment(_case, _indent + getIndent());

        String colon = difference[gnuEnabled ? 0 : 1][0];

        boolean isDisabled = _case.isDisabled(true);
        int counter = COUNTER;
        COUNTER++;

        // Extract the text in the block
        StringList lines = _case.getUnbrokenText();
        String variable = lines.get(0);	// FIXME the discriminator expression might be more complex
        // START KGU#968 2021-04-25: Issue #967 Keep structure preferences in mind
        // FIXME The discriminator expression has to be compiled...
        // We are currently not supporting complex expressions
        //variable = variable.replace(")", "").replace("(", "").replace("!", "").replace(" ", "");
        StringList tokens = Element.splitLexically(variable, true);
        Element.cutOutRedundantMarkers(tokens);
        tokens.removeAll("(");
        tokens.removeAll(")");
        //tokens.removeAll("!");	// ???
        variable = tokens.concatenate();
        // The "variable" should be replaced by a register
        variable = variablesToRegisters(variable);
        // END KGU#968 2021-04-25

        String count;

        // For each line we extract it and then translate the code
        for (int i = 0; i < _case.qs.size() - 1; i++) {
            String[] split = lines.get(i + 1).split(",");
            for (String line : split) {
                count = "" + counter + "_" + i + "";

                String operator = line;
                if (!operator.startsWith("#") && !operator.startsWith("R"))
                    operator = "#" + operator;

                String cmp = "CMP " + variable + ", " + operator;
                String branch = "BEQ block_" + count;

                // add it
                addCode(cmp, getIndent(), isDisabled);
                addCode(branch, getIndent(), isDisabled);

            }

        }

        addCode("B default_" + counter, getIndent(), isDisabled);

        // Now we need to add the labels for the block
        for (int i = 0; i < _case.qs.size() - 1; i++) {
            count = "" + counter + "_" + i + "";
            // Here we go
            addCode("block_" + count + ":" + getIndent(), "", isDisabled);
            // And then we generate the code in the block
            generateCode(_case.qs.get(i), "");

            addCode("B end_" + counter, getIndent(), isDisabled);

        }

        // Here we generate the default block
        if (!lines.get(_case.qs.size()).trim().equals("%")) {
            addCode("default_" + counter + colon, "", isDisabled);
            generateCode(_case.qs.get(_case.qs.size() - 1), "");
        }

        addCode("end_" + counter + colon, "", isDisabled);
    }

    @Override
    protected void generateCode(For _for, String _indent) {
        appendComment(_for, _indent + getIndent());

        // Check if option gnuEnabled is set on GNU or Keil.
        int variant = gnuEnabled ? 0 : 1;
        String colon = difference[variant][0];

        // START KGU 2021-04-14 Argument was wrong
        //boolean isDisabled = _for.isDisabled(true);
        boolean isDisabled = _for.isDisabled(false);
        // END KGU 2021-04-14

        // Extract all the text from the block.
        String counterStr = _for.getCounterVar();
        // START KGU#968 2021-05-02: This had to be replaced by a register
        String counterReg = getAvailableRegister();
        if (counterReg.isEmpty()) {
            // If there is no new register then perhaps a variable with same name already exists?
            counterReg = variablesToRegisters(counterStr);
        }
        else {
            mVariables.put(counterReg, USER_REGISTER_TAG);
        }
        if (!counterReg.isEmpty()) {
            counterStr = counterReg;
        }
        // END KGU#968 2021-05-02
        String c;
        int counter = COUNTER;
        COUNTER++;
        String operation = "";

        String endLabel = "end_" + counter;				// This loop's end label
        Integer labelRef = jumpTable.get(_for);
        if (labelRef != null && labelRef >= 0) {
            this.breakLabels[labelRef] = endLabel;
        }
        if (_for.isForInLoop()) {
            c = _for.getValueList();
            StringList items = this.extractForInListItems(_for);
            if (items != null) {
                c = "[" + transform(items.concatenate(", "), false) + "]";
            }
        } else {
            String startValueStr = _for.getStartValue();
            String endValueStr = _for.getEndValue();
            String stepValueStr = _for.getStepString();

            String op;
            // Understand if it's negative for or positive for
            if (stepValueStr.startsWith("-")) {
                op = "SUB";
                stepValueStr = stepValueStr.substring(1);
            } else {
                op = "ADD";
            }

            // Let's add the # if we need it
            if (!startValueStr.startsWith("R")) {
                startValueStr = "#" + startValueStr;
            }
            if (!endValueStr.startsWith("R")) {
                endValueStr = "#" + endValueStr;
            }

            stepValueStr = "#" + stepValueStr;

            //Write the code for the For
            String mov = getIndent() + "MOV " + counterStr + " , " + startValueStr + "\n";
            String cmp = "for_" + counter + colon + "\n" + getIndent() + "CMP " + counterStr + " ," + endValueStr + "\n";

            String bge = getIndent() + "BGE end_" + counter;
            operation = getIndent() + op + " " + counterStr + " , " + counterStr + ", " + stepValueStr;

            // formatting a single string
            c = mov + cmp + bge;
        }
        // Adding the code
        // START KGU#968 2021-04-25: Issue #967 c might contain newlines - which compromises line mapping
        //addCode(c, "", isDisabled);
        String[] cSplit = c.split("\\n");
        for (int i = 0; i < cSplit.length; i++) {
            addCode(cSplit[i], "", isDisabled);
        }
        // END KGU#968 2021-04-25
        // Generate the code into the block
        generateCode(_for.q, "");
        addCode(operation, "", isDisabled);

        // Adding the branch instruction and the label
        // START KGU#968 2021-05-02: Map the jumpTable entry to the end label
        //addCode(getIndent() + "B for_" + counter, "", isDisabled);
        //addCode("end_" + counter + colon, "", isDisabled);
        //int s = counter + 1;
        // This part is something similar to unifyFlow (we can do it better)
        //if (code.indexOf("end_" + counter + colon) == code.indexOf("end_" + s + colon) - 1) {
        //    code.replaceInElements("B end_" + s, "B end_" + counter);
        //    code.replaceInElements("end_" + s + colon, "");
        //    code.replaceInElements("end_" + s, "end_" + counter);
        //}
        addCode(getIndent() + "B for_" + counter, "", isDisabled);
        String endLabelPlus = "end_" + (counter + 1);		// End label of the last nested loop
        int posEnd = code.count();
        // Check whether the end label would coincide with a nested end label
        boolean endClash = posEnd > 0 && !isDisabled && code.get(posEnd-1).equals(endLabelPlus + colon);
        if (endClash) {
            // Overwrite the previous label
            code.set(posEnd-1, endLabel + colon);
            // Adapt all references
            code.replaceInElements(endLabelPlus, endLabel);
            for (int i = 0; i < breakLabels.length; i++) {
                if (breakLabels[i].equals(endLabelPlus)) {
                    breakLabels[i] = endLabel;
                }
            }
        } else {
            addCode(endLabel + colon, "", isDisabled);
        }
        if (!counterReg.isEmpty() && USER_REGISTER_TAG.equals(mVariables.get(counterReg))) {
            // Release the register
            mVariables.put(counterReg, "");
        }
        // END KGU#968 2021-05-02
    }

    @Override
    protected void generateCode(While _while, String _indent) {
        String colon = difference[gnuEnabled ? 0 : 1][0];

        boolean isDisabled = _while.isDisabled(true);
        appendComment(_while, _indent + getIndent());
        int counter = COUNTER;

        String[] keys = {"end", "code"};

        String c = processCondition(_while, "while", keys, true);
        if (c == null) {
            return;
        }

        // Add the label
        addCode("while_" + counter + colon, "", isDisabled);

        // Add the code
        // START KGU#968 2021-04-25: Issue #967 c might contain newlines - which compromises line mapping
        //addCode(c, "", isDisabled);
        String[] cSplit = c.split("\\n");
        for (int i = 0; i < cSplit.length; i++) {
            addCode(cSplit[i], "", isDisabled);
        }
        // END KGU#968 2021-04-25
        // START KGU#968 2021-05-02: Map the jumpTable entry to the end label
        Integer labelRef = jumpTable.get(_while);
        if (labelRef != null && labelRef >= 0) {
            this.breakLabels[labelRef] = "end_" + counter;
        }
        // END KGU#968 2021-05-02
        // Generate the code into the block
        generateCode(_while.q, _indent);
        // Add the label and the branch instruction
        addCode("B while_" + counter, getIndent(), isDisabled);
        addCode("end_" + counter + colon, "", isDisabled);
    }

    @Override
    protected void generateCode(Repeat _repeat, String _indent) {
        String colon = difference[gnuEnabled ? 0 : 1][0];

        boolean isDisabled = _repeat.isDisabled(true);

        appendComment(_repeat, _indent + getIndent());

        int counter = COUNTER;

        String[] keys = {"do", "continue"};
        // START KGU#968 2021-05-02: Map the jumpTable entry to the end label (and add one for breaks)
        Integer labelRef = jumpTable.get(_repeat);
        if (labelRef != null && labelRef >= 0) {
            addCode("end_" + counter + colon, "", isDisabled);
            this.breakLabels[labelRef] = "end_" + counter;
        }
        // END KGU#968 2021-05-02

        String c = processCondition(_repeat, "until", keys, false);
        if (c == null) {
            return;
        }

        addCode("do_" + counter + colon, "", isDisabled);

        generateCode(_repeat.q, "");

        // START KGU#968 2021-04-25: Issue #967 c might contain newlines - which compromises line mapping
        //addCode(c, "", isDisabled);
        String[] cSplit = c.split("\\n");
        for (int i = 0; i < cSplit.length; i++) {
            addCode(cSplit[i], "", isDisabled);
        }
        // END KGU#968 2021-04-25
    }

    @Override
    protected void generateCode(Forever _forever, String _indent) {
        String colon = difference[gnuEnabled? 0 : 1][0];

        boolean isDisabled = _forever.isDisabled(true);
        appendComment(_forever, _indent + getIndent());

        int counter = COUNTER;
        COUNTER++;

        // Create While True Block
        addCode("whileTrue_" + counter + colon, "", isDisabled);
        generateCode(_forever.q, "");

        // Add pointer to While True Block
        addCode("B whileTrue_" + counter + "\n", getIndent(), isDisabled);

        addSepaLine();
        // START KGU#968 2021-05-02: Map the jumpTable entry to the end label (and add one for breaks)
        Integer labelRef = jumpTable.get(_forever);
        if (labelRef != null && labelRef >= 0) {
            addCode("end_" + counter + colon, "", isDisabled);
            this.breakLabels[labelRef] = "end_" + counter;
        }
        // END KGU#968 2021-05-02
    }

    @Override
    protected void generateCode(Call _call, String _indent) {
        if (!appendAsComment(_call, _indent)) {
            boolean isDisabled = _call.isDisabled(true);
            appendComment(_call, _indent + getIndent());
            StringList lines = _call.getUnbrokenText();

            // START KGU#968 2021-05-02: We must pass the arguments in order of occurrence
            //String line = lines.get(0);
            //StringBuilder registers = new StringBuilder();
            //int i = 0;
            //
            //for (Map.Entry<String, String> entry : mVariables.entrySet()) {
            //    if (!entry.getValue().equals("") && i == 0) {
            //        registers.append(entry.getKey());
            //        i++;
            //    } else if (!entry.getValue().equals("")) {
            //        registers.append(", ").append(entry.getKey());
            //    }
            //}
            //String functionName = getFunction(line);
            //addCode("STMFD SP!, {" + registers + ", LR}", getIndent(), isDisabled);
            //addCode("BL " + functionName, getIndent(), isDisabled);
            //addCode("LDMFD SP!, {" + registers + ", LR}", getIndent(), isDisabled);
            //addCode("MOV PC, LR", getIndent(), isDisabled);	// This was nonsense anyway!
            Function called = _call.getCalledRoutine();
            Root caller = Element.getRoot(_call);

            if (called != null) {
                StringBuilder registers = new StringBuilder();
                Vector<Root> cands;
                if (this.routinePool != null
                        && !(cands = this.routinePool.findRoutinesBySignature(called.getName(),
                                called.paramCount(), caller, true)).isEmpty()) {
                    addCode("STMFD SP!, {LR}", getIndent(), isDisabled);
                    Root routine = cands.firstElement();
                    ArrayList<Param> params = routine.getParams();
                    for (int i = 0; i < called.paramCount(); i++) {
                        // Current argument
                        String arg = called.getParam(i);
                        // FIXME we will have to compile the expression here and may not meddle with the stack!
                        if (arg.matches(variablePattern)) {
                            arg = this.variablesToRegisters(arg);
                        } else {
                            String reg = getAvailableRegister();
                            generateInstructionLine(String.format("%s <- %s", reg, arg), isDisabled);
                            arg = reg;
                            if (!reg.isEmpty()) {
                                // Unregister the temporary register
                                mVariables.put(reg, "");
                            }
                        }
                        addCode("STR " + arg + ", [SP,#-4]!", this.getIndent(), isDisabled);
                    }
                    // Add a cell for the return value (and temporarily for default values)
                    addCode(String.format("STR R0, [SP,#-%d,LSL #2]", params.size() - called.paramCount() + 1),
                        this.getIndent(), isDisabled);
                    for (int i = called.paramCount(); i < params.size(); i++) {
                        String arg = params.get(i).getDefault();
                        this.generateInstructionLine("R0 <- " + arg, isDisabled);
                        addCode("STR R0, [SP,#4]!", this.getIndent(), isDisabled);
                    }
                    addCode("SUB SP, #4", getIndent(), isDisabled);
                    // Place a 0 value in the result field and restore R0
                    addCode("MOV R0, #0", this.getIndent(), isDisabled);
                    addCode("SWP R0, R0, [SP]", this.getIndent(), isDisabled);
                    addCode("BL " + called.getName(), getIndent(), isDisabled);
                    // Is it a function call? Then produce an assignment with variable mapping
                    if (_call.isAssignment()) {
                        // Get the result value
                        StringList tokens = Element.splitLexically(lines.get(0), true);
                        tokens.removeAll(" ");
                        String target = Call.getAssignedVarname(tokens, false);
                        target = variablesToRegisters(target);
                        appendComment("Subroutine result:", getIndent());
                        addCode(String.format("LDR %s, [SP]", target), getIndent(), isDisabled);
                    }
                    addCode(String.format("ADD SP, #%d", (params.size()+1) * 4), getIndent(), isDisabled);
                    addCode("LDMFD SP!, {LR}", getIndent(), isDisabled);
                } else {
                    // Dummy code for not retrievable subroutine (taken from the orig. code)
                    // Just stash all registers in use
                    for (Map.Entry<String, String> entry : mVariables.entrySet()) {
                        if (!entry.getValue().equals("")) {
                            registers.append(entry.getKey());
                            registers.append(", ");
                        }
                    }
                    addCode("STMFD SP!, {" + registers + "LR}", getIndent(), isDisabled);
                    addCode("BL " + called.getName(), getIndent(), isDisabled);
                    addCode("LDMFD SP!, {" + registers + "LR}", getIndent(), isDisabled);
                }
            }
            else {
                appendComment("INCORRECT CALL SYNTAX - Call ignored:", getIndent());
                for (int i = 0; i < lines.count(); i++) {
                    appendComment(lines.get(i), getIndent());
                }
            }
            // END KGU#968 2021-05-02
        }
    }

    @Override
    protected void generateCode(Jump _jump, String _indent) {
        // FIXME Implement a subroutine return, a loop exit
        String colon = difference[gnuEnabled? 0 : 1][0];
        if (!appendAsComment(_jump, _indent)) {
            boolean isDisabled = _jump.isDisabled(false);
            appendComment(_jump, _indent);
            boolean isEmpty = true;

            StringList lines = _jump.getUnbrokenText();
            // Has it already been matched with a loop? Then syntax must have been okay...
            Integer ref = this.jumpTable.get(_jump);
            if (ref != null)
            {
                String label = "__ERROR__";
                if (ref.intValue() >= 0) {
                    label = breakLabels[ref];
                } else {
                    appendComment("FIXME: Structorizer detected this illegal jump attempt:", _indent);
                    appendComment(lines.getLongString(), _indent);
                }
                addCode("B " + label + colon, getIndent(), isDisabled);
            }
            else
            {
                Root root = Element.getRoot(_jump);
                String preReturn = CodeParser.getKeywordOrDefault("preReturn", "return");
                String preExit   = CodeParser.getKeywordOrDefault("preExit", "exit");
                //String preThrow  = CodeParser.getKeywordOrDefault("preThrow", "throw");
                for (int i = 0; isEmpty && i < lines.count(); i++) {
                    String line = transform(lines.get(i)).trim();
                    if (!line.isEmpty())
                    {
                        isEmpty = false;
                    }
                    if (Jump.isReturn(line))
                    {
                        String argument = line.substring(preReturn.length()).trim();
                        if (!argument.isEmpty())
                        {
                            // FIXME The expression will have to be compiled!
                            if (argument.matches(variablePattern)) {
                                argument = this.variablesToRegisters(argument);
                            }
                            else {
                                String reg = getAvailableRegister();
                                generateInstructionLine(reg + " <- " + argument, isDisabled);
                                argument = reg;
                            }
                            addCode(String.format("STR %s, [SP,#13,#2]", argument), getIndent(), isDisabled);
                        }
                        addCode("LDMFD SP!, {R0-R12}", getIndent(), isDisabled);
                        addCode("MOVS PC, LR", getIndent(), isDisabled);
                        addCode("", getIndent(), false);
                    }
                    else if (Jump.isExit(line))
                    {
                        String argument = line.substring(preExit.length()).trim();
                        if (topLevel && root.isProgram()) {
                            String arg = "";
                            if (!argument.isEmpty()) {
                                if (argument.matches(variablePattern)) {
                                    arg = (this.variablesToRegisters(argument) + " ").split(" ")[0];
                                }
                                else {
                                    String reg = getAvailableRegister();
                                    generateInstructionLine(reg + " <- " + argument, isDisabled);
                                    arg = reg;
                                }
                            } else {
                                // Return a 0 value
                                arg = getAvailableRegister();
                                // FIXME registers might be exhausted...
                                addCode(String.format("MOV %s, #0", arg), getIndent(), isDisabled);
                            }
                            // Write the result value to the designated space on stack (+ 12 words)
                            addCode(String.format("STR %s, [SP,#13,#2]", arg), _indent, isDisabled);
                            addCode("LDMFD SP!, {R0-R12}", getIndent(), isDisabled);
                            addCode("MOVS PC, LR", getIndent(), isDisabled);
                            addCode("", getIndent(), false);
                        } else {
                            appendComment("================= NOT SUPPORTED, FIND AN EQUIVALENT =================", "");
                            appendComment(_jump.getUnbrokenText().getText(), _indent);
                        }
                    }
                    else if (Jump.isThrow(line)) {
                        appendComment("================= NOT SUPPORTED, FIND AN EQUIVALENT =================", "");
                        appendComment(_jump.getUnbrokenText().getText(), _indent);
                    }
                    else if (!isEmpty)
                    {
                        appendComment("FIXME: Structorizer detected the following illegal jump attempt:", _indent);
                        appendComment(line, _indent);
                    }
                    // END KGU#74/KGU#78 2015-11-30
                }
                if (isEmpty) {
                    appendComment("FIXME: An empty jump was found here! Cannot be translated to " +
                            this.getFileDescription(), _indent);
                }

            }
        }
    }

    /**
     * Not actually supported
     *
     * @param _para   - the {@link lu.fisch.structorizer.elements.Parallel} element to be exported
     * @param _indent - the indentation string valid for the given Instruction
     */
    @Override
    protected void generateCode(Parallel _para, String _indent) {
        appendComment(_para, _indent);

        appendComment("==========================================================", getIndent());
        appendComment("========= START PARALLEL SECTION (NOT SUPPORTED) =========", getIndent());
        appendComment("==========================================================", getIndent());

        for (int i = 0; i < _para.qs.size(); i++) {
            appendComment("---------------- START THREAD " + i + " -----------------", getIndent());
            generateCode(_para.qs.get(i), getIndent());
            appendComment("----------------- END THREAD " + i + " ------------------", getIndent());
        }

        appendComment("==========================================================", getIndent());
        appendComment("========== END PARALLEL SECTION (NOT SUPPORTED) ==========", getIndent());
        appendComment("==========================================================", getIndent());
    }

    /**
     * @param _try    - the {@link lu.fisch.structorizer.elements.Try}
     * @param _indent - the indentation string valid for the given Instruction
     */
    @Override
    protected void generateCode(Try _try, String _indent) {
        boolean isDisabled = _try.isDisabled(true);
        appendComment(_try, getIndent());
        addCode("LDMFD sp!,{R0-R12,pc}^", getIndent(), isDisabled);
        generateCode(_try.qTry, _indent);
        generateCode(_try.qCatch, _indent);
        generateCode(_try.qFinally, _indent);
    }

    /**
     * Calls {@link #getMode(String)} to get what line is and proceeds to
     * translate it accordingly.
     *
     * @param line       - the string in the block
     * @param isDisabled - whether this element or one of its ancestors is disabled
     */
    private void generateInstructionLine(String line, boolean isDisabled) {
        String newline;
        ARM_OPERATIONS mode = getMode(line);

        switch (mode) {
        case ASSIGNMENT:
            newline = variablesToRegisters(line);
            generateAssignment(newline, isDisabled);
            break;
        case EXPRESSION:
            newline = variablesToRegisters(line);
            generateExpr(newline, isDisabled);
            break;
        case MEMORY:
            newline = variablesToRegisters(line);
            generateMemoryAssignment(newline, isDisabled);
            break;
        case ARRAY_EXPRESSION:
            newline = variablesToRegisters(line);
            generateArrayExpr(newline, isDisabled);
            break;
        case ARRAY_ASSIGNMENT:
            // FIXME: This will also replace the array name by a (possibly uninitialised) register
            newline = variablesToRegisters(line);
            generateArrayAssignment(newline, isDisabled);
            break;
        case ARRAY_INITIALIZATION:
            generateArrayInitialization(line, isDisabled);
            break;
        case ADDRESS:
            generateAddressAssignment(line, isDisabled);
            break;
        case BOOLEAN_ASSIGNMENT:
            newline = variablesToRegisters(line);
            generateAssignment(newline.replace("true", "1").replace("false", "0"), isDisabled);
            break;
        case STRING_ARRAY_INITIALIZATION:
            newline = variablesToRegisters(line);
            generateString(newline, isDisabled);
            break;
        case CHAR_ARRAY_INITIALIZATION:
            newline = variablesToRegisters(line);
            generateAssignment(newline.replace("\"", "'"), isDisabled);
            break;
        case INSTRUCTION:
            newline = variablesToRegisters(line);
            addCode(newline, getIndent(), isDisabled);
        case INPUT:
            if (gnuEnabled) {
                // START KGU#968 2021-04-25: Remove the keyword and a possible prompt string
                //newline = variablesToRegisters(line);
                //String register = newline.split(" ")[1];
                StringList tokens = Element.splitLexically(line, true);
                StringList inputTokens = Element.splitLexically(CodeParser.getKeywordOrDefault("input", "input"), true);
                // Check for a prompt string literal and remove it (plus a possible comma)
                int ix = inputTokens.count();
                while (ix < tokens.count() && tokens.get(ix).trim().isEmpty()) {
                    ix++;
                }
                if (ix < tokens.count() && (tokens.get(ix).startsWith("\"") || tokens.get(ix).startsWith("'"))) {
                    tokens.remove(ix);
                }
                while (ix < tokens.count() && (tokens.get(ix).trim().isEmpty() || tokens.get(ix).equals(","))) {
                    ix++;
                }
                newline = variablesToRegisters(tokens.concatenate(null, ix));
                String register = newline.split(" ")[0];
                // END KGU#968 2021-04-25
                // START KGU#968 2021-04-24: We must not add two lines via a single call (for correct line counting)
                //addCode(String.format("LDR %s, =0xFF200050\n%sLDR %s, [%s]", register, getIndent(), register, register), getIndent(), isDisabled);
                addCode(String.format("LDR %s, =0xFF200050", register), getIndent(), isDisabled);
                addCode(String.format("LDR %s, [%s]", register, register), getIndent(), isDisabled);
                // END KGU#968 2021-04-24
            } else {
                appendComment("ERROR: INPUT operation only supported with GNU code\n" + line, getIndent());
            }
            break;
        case OUTPUT:
            if (gnuEnabled) {
                // START KGU#968 2021-04-26: Remove the keyword
                //newline = variablesToRegisters(line);
                //String register = newline.split(" ")[1];
                //String addrRegister = getAvailableRegister();
                // We must add two lines via two calls (for correct line counting)
                //addCode(String.format("LDR %s, =0xFF201000", addrRegister), getIndent(), isDisabled);
                //addCode(String.format("STR %s, [%s]", register, addrRegister), getIndent(), isDisabled);
                StringList tokens = Element.splitLexically(line, true);
                StringList outputTokens = Element.splitLexically(CodeParser.getKeywordOrDefault("output", "output"), true);
                tokens.remove(0, outputTokens.count());
                StringList exprs = Element.splitExpressionList(tokens, ",", true);
                if (exprs.count() > 1) {
                    String addrRegister = getAvailableRegister();
                    if (!addrRegister.isEmpty()) {
                        mVariables.put(addrRegister, "TEMPORARILY OCCUPIED!");
                    }
                    addCode(String.format("LDR %s, =0xFF201000", addrRegister), getIndent(), isDisabled);
                    for (int i = 0; i < exprs.count() - 1; i++) {
                        String expr = exprs.get(i);
                        String register = getAvailableRegister();
                        if (expr.matches(variablePattern)) {
                            register = variablesToRegisters(expr);
                        }
                        else {
                            generateInstructionLine(String.format("%s <- %s", register, expr), isDisabled);
                            if (!register.isEmpty()) {
                                mVariables.put(register, "");
                            }
                        }
                        addCode(String.format("STR %s, [%s]", register, addrRegister), getIndent(), isDisabled);
                    }
                    if (!addrRegister.isEmpty()) {
                        // Release the register
                        mVariables.put(addrRegister, "");
                    }
                }
                // END KGU#968 2021-04-26
            } else {
                appendComment("ERROR: OUTPUT operation only supported with GNU code\n" + line, getIndent());
            }
            break;
        case NOT_IMPLEMENTED:
            appendComment("ERROR: Not implemented yet\n" + line, getIndent());
            break;
        }
    }

    /**
     * This method uses regex tests to verify which ARM instruction is {@code line1}
     *
     * @param line1 - the string that contains the instruction to translate
     * @return string that represents what is the instruction
     */
    private ARM_OPERATIONS getMode(String line1) {
        // START KGU#968 2021-04-24: Enh. #967 - correct keyword comparison
        boolean isInput = inputPattern != null && inputPattern.matcher(line1).matches();
        boolean isOutput = outputPattern != null && outputPattern.matcher(line1).matches();
        
        // END KGU#968 2021-04-24
        String line = line1.replace(" ", "");
        ARM_OPERATIONS mode = ARM_OPERATIONS.NOT_IMPLEMENTED;

        if (booleanAssignmentPattern.matcher(line).matches()) {
            mode = ARM_OPERATIONS.BOOLEAN_ASSIGNMENT;
        } else if (assignment.matcher(line).matches()) {
            mode = ARM_OPERATIONS.ASSIGNMENT;
        } else if (expression.matcher(line).matches()) {
            mode = ARM_OPERATIONS.EXPRESSION;
        } else if (memoryAccess.matcher(line).matches()) {
            mode = ARM_OPERATIONS.MEMORY;
        } else if (memoryStore.matcher(line).matches()) {
            mode = ARM_OPERATIONS.MEMORY;
        } else if (arrayExpression.matcher(line).matches()) {
            mode = ARM_OPERATIONS.ARRAY_EXPRESSION;
        } else if (arrayAssignment.matcher(line).matches()) {
            mode = ARM_OPERATIONS.ARRAY_ASSIGNMENT;
        } else if (stringInitialization.matcher(line).matches()) {
            mode = ARM_OPERATIONS.STRING_ARRAY_INITIALIZATION;
        } else if (charInitialization.matcher(line).matches()) {
            mode = ARM_OPERATIONS.CHAR_ARRAY_INITIALIZATION;
        } else if (arrayInitialization.matcher(line).matches()) {
            mode = ARM_OPERATIONS.ARRAY_INITIALIZATION;
        } else if (address.matcher(line).matches()) {
            mode = ARM_OPERATIONS.ADDRESS;
        // START KGU#968 2021-04-24: Correct input/output detection
        //} else if (line.toLowerCase().contains("input")) {
        //    mode = ARM_OPERATIONS.INPUT;
        //} else if (line.toLowerCase().contains("output")) {
        //    mode = ARM_OPERATIONS.OUTPUT;
        } else if (isInput) {
            mode = ARM_OPERATIONS.INPUT;
        } else if (isOutput) {
            mode = ARM_OPERATIONS.OUTPUT;
        // END KGU#968 2021-04-24
        // START KGU#968 2021-10-06: line does not contain spaces, so better use line1 here
        //} else if (isArmInstruction(line)) {
        } else if (isArmInstruction(line1)) {
        // END KGU#968 2021-10-16
            mode = ARM_OPERATIONS.INSTRUCTION;
        }

        return mode;
    }

    /**
     * Translates multiple conditions, takes the condition, counter, keywords as inputs<br/>
     * EXAMPLE: condition = "{@code R0 < R1 && R1 > R2}", counter = 1, key[then, else]<br/>
     * <b>NOTE: By now only conditions with one kind of operators (and / or) can be translated!</b><br/>
     * This method increments the {@link #COUNTER} field!
     *
     * @param condition - the string that contains the condition with unified operators
     * @param reverse   - the boolean used for reversing the condition with the {@code not} operator
     * @param key       - the list of the Strings used for the labels
     */
    // FIXME Doesn't splitCondition() already replace/remove the verbose operators?
    // splitCondition doesn't remove brackets
    private String multiCondition(String condition, boolean reverse, String[] key) {
        int counter = COUNTER;
        COUNTER++;
        String c = "";

        // If there are variables we give them a free register
        condition = variablesToRegisters(condition);
        // Replacing all the chars that we don't need
        condition = condition.replace("(", "").replace(")", "").replace(" ", "");
        // Check for boolean char
        if (condition.contains("||") || condition.contains("&&") || condition.contains("and") || condition.contains("or")) {
            // call the method for split the Condition and then replace chars in the string
            c = splitCondition(condition, key);
            c = c.replace("and", "").replace("&", "").replace("or", "").replace("|", "").replace("not", "").replace("!", "");
        } else {
            String[] act = getCondition(condition, reverse);
            String cmp = getIndent() + "CMP " + act[0] + ", " + act[2] + "\n";
            String branch = getIndent() + act[1] + " " + key[0] + "_" + counter;
            c = c + cmp + branch;
        }

        return c;
    }

    /**
     * This method is used for the instruction of operator like (=, >, <, ...)
     *
     * @param condition - the condition expression string
     * @param inverse   - is the condition inverted by a {@code not} operator?
     * @return an array that contains the first operator, arm instruction, and the second operator
     */
    private String[] getCondition(String condition, boolean inverse) {
        // FIXME Is it certain that the expression contains only one relation operator? Otherwise trouble is ahead...
        //  Shouldn't it have been more sensible first to split around the original operators and then to replace them?
        // when this method is called there is just one relation operator in the condition because we split them in multiCondition()
        condition = condition.replace("==", "=");
        String op = "";
        String sep = "";

        // if inverse is false then it returns the ARM operation and the correct separator (sep)
        if (!inverse) {
            if (condition.contains(">=")) {
                op = "BGE";
                sep = ">=";
            } else if (condition.contains("<=")) {
                op = "BLE";
                sep = "<=";
            } else if (condition.contains("<") && !condition.contains("=")) {
                op = "BLT";
                sep = "<";
            } else if (condition.contains(">") && !condition.contains("=")) {
                op = "BGT";
                sep = ">";
            } else if (condition.contains("!=")) {
                op = "BNE";
                sep = "!=";
            } else if (condition.contains("=")) {
                op = "BEQ";
                sep = "=";
            }
        } // else it returns the opposite operation and the correct separator (sep)
        else {
            if (condition.contains(">=")) {
                op = "BLT";
                sep = ">=";
            } else if (condition.contains("<=")) {
                op = "BGT";
                sep = "<=";
            } else if (condition.contains("<") && !condition.contains("=")) {
                op = "BGE";
                sep = "<";
            } else if (condition.contains(">") && !condition.contains("=")) {
                op = "BLE";
                sep = ">";
            } else if (condition.contains("!=")) {
                op = "BEQ";
                sep = "!=";
            } else if (condition.contains("=")) {
                op = "BNE";
                sep = "=";
            }
        }

        condition = condition.replace(sep, "£" + op + "£");
        // We split the string
        String[] variable = condition.split("£");

        // We add the hashtag
        if (!variable[2].startsWith("R")) {
            variable[2] = "#" + variable[2] + "";
        }

        return variable;

    }

    // START KGU#968 2021-10-05
    /**
     * Processes the condition expression of the passed-in Element {@code _ele}
     * (should be an {@link Alternative}, {@link While}, or {@link Repeat} object)
     * and returns either {@code null} (in case the transformation failed) or a
     * multi-line instruction sequence as translation.
     * 
     * @param _ele - The element the code for which is to be generated
     * @param prefix - an element-class-specific prefix for the error message
     * @param keys - a pair of keys for creating the jump labels
     * @param inverse - whether the condition is to be logically inverted
     * @return the ARM instruction sequence or {@code null}
     */
    private String processCondition(Element _ele, String prefix, String[] keys, boolean inverse)
    {
        // Extract the text in the block
        String condition = _ele.getUnbrokenText().getLongString().trim();

        StringList tokens = Element.splitLexically(condition, true);
        Element.cutOutRedundantMarkers(tokens);
        tokens.removeAll(" ");
        Element.unifyOperators(tokens, false);
        condition = tokens.concatenate(null);
        if (!condition.startsWith("(") || !condition.endsWith(")")) {
            // To help the matching
            condition = "(" + condition + ")";
        }
        condition = prepareAtomicCondition(condition);
        if (!conditionPattern.matcher(condition).matches()) {
            // FIXME - this seems to be an inappropriate reaction!
            appendComment("ERROR: Unsupported condition syntax - " + _ele.getClass().getSimpleName() + " skipped!", getIndent());
            appendComment(prefix + " " + condition, getIndent());
            return null;
        }

        condition = condition.replace("(", "").replace(")", "");

        // Generate the condition code with multiCondition
        return multiCondition(condition, inverse, keys);
    }
    
    /**
     * Converts an atomic condition, i.e., a pure or negated register or variable name
     * into a comparison against 0. Other expressions remain untouched.<br/>
     * Examples:<ul>
     * <li>{@code (R4)} &rarr; {@code (R4!=0)} </li>
     * <li>{@code (!isBool)} &rarr; {@code (isBool==0)} </li>
     * </ul>
     * @param tokens - a condensed condition string <b>with already unified
     *  operators and surrounding parentheses</b>.
     * @return possibly the modified condition or the passed argument itself
     */
    private String prepareAtomicCondition(String condition) {
        if (atomicCondPattern.matcher(condition).matches()) {
            String opr = "!=";
            if (condition.charAt(1) == '!') {
                opr = "==";
                condition = "(" + condition.substring(2);
            }
            condition = condition.substring(0, condition.length()-1) + opr + "0)";
        }
        return condition;
    }
    // END KGU#968 2021-10-05

    /**
     * This method translates array initializations
     * EXAMPLE: word R1 <- {1, 2, 3}
     *
     * @param line       the string that contains the instruction to translate
     * @param isDisabled whether this element or one of its ancestors is disabled
     */
    private void generateArrayInitialization(String line, boolean isDisabled) {
        String[] tokens = line.split("<-|:=");

        String varName = tokens[0];
        String expr = tokens[1];
        String type = "";
        // FIXME: There could be a nested structure (not according to the pattern, though)!
        expr = expr.replace("{", "").replace("}", "");

        // If the assignment uses a register as an array
        if (varName.contains("R")) {
            String[] t = varName.split("R");

            if (t.length > 1) {
                type = t[0];
                varName = varName.replace(type, "").replace(" ", "");
                type = "." + type;
            }

            // GNU Compiler
            // FIXME a name V_# or v_# might collide with a user-chosen variable name
            if (gnuEnabled) {
                addToDataSection("v_" + arrayCounter + difference[0][0] + "\t" + type + "\t" + expr);
                addCode("ADR " + varName + ", v_" + arrayCounter, getIndent(), isDisabled);
            } else {
                addToDataSection("V_" + arrayCounter + getIndent() + "DCD" + expr);
                addCode("LDR " + varName + ", =V_" + arrayCounter, getIndent(), isDisabled);
            }

            arrayCounter = arrayCounter + 1;
        } // If the assignment doesn't use a register but a variable
        else {
            String[] t = varName.split(" ");
            if (t.length > 1) {
                type = t[0];
                varName = varName.replace(type, "").replace(" ", "");
                type = "." + type;
            }
            // GNU compiler
            if (gnuEnabled) {
                addToDataSection(varName + difference[0][0] + getIndent() + type + "\t" + expr);
            } else {
                addToDataSection(varName + getIndent() + "DCD" + expr);
            }
        }
    }

    /**
     * This method translates array element assignments
     * EXAMPLE: R0[0] <- 1
     *
     * @param line       - the string that contains the instruction to translate
     * @param isDisabled - whether this element or one of its ancestors is disabled
     */
    private void generateArrayAssignment(String line, boolean isDisabled) {
        String[] tokens = line.split("<-|:="); //R0[], R2

        String expr = tokens[1].trim();
        String[] arr = tokens[0].split("\\["); //R0, R1]
        String arName = arr[0].trim(); //R0

        if (!arr[1].contains("R")) { //R0[1], R2
            int index = Integer.parseInt(arr[1].replace("]", "").replace(" ", ""));
            int dim = returnDim(arName);
            if (dim > 0) {

                index = (int) (index * Math.pow(2, dim));
                addCode("STR " + expr + ", [" + arName + ", #" + index + "]", getIndent(), isDisabled);
            } else {
                appendComment("The array " + arName + " is not initialized", getIndent());
            }

        } else if (arr[1].contains("R")) {
            addCode("STR " + expr + ", " + "[" + arName + ", " + arr[1].trim(), getIndent(), isDisabled);

        } else {	// FIXME Dead code
            appendComment("ERROR, no free register or no array type specified", "");
        }
    }

    /**
     * This method translates variable or register assignments from an array
     * EXAMPLE: R0 <- R1[0]
     *
     * @param line       the string that contains the instruction to translate
     * @param isDisabled whether this element or one of its ancestors is disabled
     */
    private void generateArrayExpr(String line, boolean isDisabled) {
        line = line.replace(" ", "");
        String[] tokens = line.split("<-|:=");
        int dim = 0;
        String expr = tokens[1];
        String varName = tokens[0];
        // Divide array name from expression
        String arName = expr.split("\\[")[0];
        String index = tokens[1].split("\\[")[1].replace("]", "");
        String c = "LDR " + varName + ", [" + arName + ", ";
        // Array size
        if (!gnuEnabled) {
            dim = returnDim(arName);
        }
        // if the array is initialized or we are not in Keil mode
        if (dim > 0) {
            // If the index is not a register
            if (!index.startsWith("R") && !index.startsWith("r")) {
                int ind = Integer.parseInt(index);
                ind = (int) (ind * Math.pow(2, dim));
                c += "#" + ind + "]";
            }
            // We use the register as index
            else {
                c += index + "]";
            }
        }
        // if we are in keil mode
        else if (dim == 0) {
            // Add the hashtag if needed
            if (!index.startsWith("R") && !index.startsWith("r")) {
                c += "#";
            }
            c += index + "]";
        }
        // if the array is not initialized
        else {
            appendComment("The array is not initialized", getIndent());
            c = "";
        }

        addCode(c, getIndent(), isDisabled);
    }

    /**
     * This method translates variable or register assignments<br/>
     * EXAMPLE: {@code R0 <- 1}
     *
     * @param line       - the string that contains the instruction to translate
     * @param isDisabled - whether this element or one of its ancestors is disabled
     */
    private void generateAssignment(String line, boolean isDisabled) {
        String code;
        line = line.replace(" ", "");
        String[] tokens = line.split(assignmentOperators);

        String hashtag = difference[gnuEnabled ? 0 : 1][1];

        String firstOperator = tokens[0]; // firstOperator must be a register or a variable
        String secondOperator = tokens[1]; // secondOperator can be a register, a variable or a hex number

        // if secondOperator is a negative number then we need to use MVN and convert the number to a hex number
        if (secondOperator.matches(negativeNumberPattern)) {
            int n = Integer.parseInt(secondOperator);
            secondOperator = Integer.toHexString(n);
            code = "MVN %s, %s0x%s";
        }
        // if secondOperator is a register then we don't need to prepend the #
        else if (secondOperator.matches(registerPattern)) {
            hashtag = "";
            code = "MOV %s, %s%s";
        } else {
            code = getInstructionConstant(firstOperator, secondOperator);
        }

        addCode(String.format(code, firstOperator, hashtag, secondOperator), getIndent(), isDisabled);
    }

    /**
     * This method translates basic operations between variables and/or registers
     * EXAMPLE: R0 <- R1 + 1
     *
     * @param line - the instruction to translate as string
     */
    private void generateExpr(String line, boolean isDisabled) {
        String code = "%s %s, %s, %s";
        line = line.replace(" ", "");
        String[] tokens = line.split(assignmentOperators);

        String firstOperator = tokens[0]; // firstOperator must be a register or a variable
        String secondOperator = tokens[1]; // secondOperator is the simple expression R0 <- 1 + 1 ->> [1, +, 1]
        secondOperator = secondOperator.replace("and", "&").replace("or", "|");

        String operation = ""; // ARM operation

        String[] expression = parseExpression(secondOperator); // expression must be a simple expression: [R0, +, 1], [x, +, y], [x, and, y]

        String thirdOperator; // third value in the arm operation, (ADD R0, R1, #1)

        if ("+".equals(expression[1])) {
            operation = "ADD";
        } else if ("-".equals(expression[1])) {
            operation = "SUB";
        } else if ("*".equals(expression[1])) {
            operation = "MUL";
        } else if ("&".equals(expression[1])) {
            operation = "AND";
        } else if ("|".equals(expression[1])) {
            operation = "ORR";
        }

        // if expression[0] is a register then we don't need to prepend the #
        secondOperator = expression[0].matches(registerPattern) ? expression[0] : "#" + expression[0];

        // if expression[2] is a register then we don't need to prepend the #
        thirdOperator = expression[2].matches(registerPattern) ? expression[2] : "#" + expression[2];

        // replace MUL with LSL where possible
        if (operation.equals("MUL") && expression[0].matches(registerPattern)) {
            // if the second member of the expression is a number
            if (expression[2].matches(numberPattern)) {
                int value = Integer.parseInt(expression[2]);
                int shift;

                // if the value is a power of two
                if (isPowerOfTwo(value)) {
                    shift = (int) (Math.log(value) / Math.log(2));
                    operation = "LSL";
                    thirdOperator = "#" + shift;
                }
                // if the previous value number is a power of two
                else if (isPowerOfTwo(value - 1)) {
                    shift = (int) (Math.log(value - 1) / Math.log(2));
                    operation = "ADD";
                    thirdOperator = String.format("LSL #%s", shift);
                }
            }
            // if it's a register KGU: This seemed to be nonsense
            //else if (expression[2].matches(registerPattern)) {
            //    operation = "LSL";
            //}
        }

        addCode(String.format(code, operation, firstOperator, secondOperator, thirdOperator), getIndent(), isDisabled);
    }

    /**
     * This method translates an array's address assignment to a register using
     * indirizzo or address as keywords
     * EXAMPLE: R0 <- address(R1)
     *
     * @param line       - the instruction to translate as string
     * @param isDisabled - whether this element or one of its ancestors is disabled
     */
    private void generateAddressAssignment(String line, boolean isDisabled) {
        line = line.replace(" ", "");
        String[] tokens = line.split("<-|:=");

        String expr = tokens[1].replace("indirizzo", "").replace("address", "").replace("(", "").replace(")", "");

        if (line.contains("indirizzo") || line.contains("address")) {
            addCode("LDR " + tokens[0] + ", =" + expr, getIndent(), isDisabled);
        }
    }

    /**
     * This method translates an alternative way of using arrays (this time with memory access) using memoria or memory as keywords
     * EXAMPLES:<br/>
     * R0 <- memory[R1]<br/>
     * memory[R0] <- R1
     *
     * @param line       - the instruction to translate as string
     * @param isDisabled - whether this element or one of its ancestors is disabled
     */
    private void generateMemoryAssignment(String line, boolean isDisabled) {
        String code = "%s %s, [%s]";
        line = line.replace(" ", "");
        String[] tokens = line.split(assignmentOperators);

        String expressionOperator; // string containing the memory expression
        String registerOperator; // string containing the register
        String operation; // the ARM operation to do (LDR or STR)

        // if the square brackets come before the assignment operator then we're in this case memory[R0] <- R1 so it's a STR operation
        if (line.indexOf("[") < line.indexOf("<-") || line.indexOf("[") < line.indexOf(":=")) {
            operation = "STR";
            expressionOperator = tokens[0];
            registerOperator = tokens[1];
        }
        // else we're in this case R1 <- memory[R0] so it's a LDR operation
        else {
            operation = "LDR";
            expressionOperator = tokens[1];
            registerOperator = tokens[0];
        }

        // get everything between the square brackets and parse the expression
        String[] expression = parseExpression(expressionOperator.substring(expressionOperator.indexOf("[") + 1, expressionOperator.indexOf("]")));

        StringBuilder secondOperator = new StringBuilder();

        for (int i = 0; i < expression.length; i++) {
            if (!expression[i].matches(supportedOperationsPattern)) {
                secondOperator.append(expression[i]);

                if (i < expression.length - 1) {
                    secondOperator.append(", ");
                }
            }
        }

        addCode(String.format(code, operation, registerOperator, secondOperator), getIndent(), isDisabled);
    }

    /**
     * This method translates a string assignment into a char array
     * EXAMPLE: R0 <- "string" ---> word R0 <- {'s', 't', 'r', 'i', 'n', 'g'}
     *
     * @param line       the string that contains the instruction to translate
     * @param isDisabled whether this element or one of its ancestors is disabled
     */

    private void generateString(String line, boolean isDisabled) {

        String[] split = line.split("<- ?|:= ?");
        // FIXME: The string literal might contain escaped quotes in future!
        split[1] = split[1].replace("\"", "");
        String c = "word %s<-{%s}";
        StringBuilder array = new StringBuilder();

        for (int i = 0; i < split[1].length(); i++) {
            array.append("'").append(split[1].charAt(i)).append("'");
            if (i != split[1].length() - 1) {
                array.append(", ");
            }
        }

        generateArrayInitialization(String.format(c, split[0], array), isDisabled);
    }

    /**
     * This method translates and splits and, or conditions
     *
     * @param condition the string that contains the condition
     * @param key       an array that contains labels (then, else, block, ...)
     * @return string that represents the condition translated
     */
    private String splitCondition(String condition, String[] key) {
        String next;
        condition = condition.replace("or", "£|").replace("and", "£&").replace("||", "£|").replace("&&", "£&").replace("not", "#!").replace("!", "#!");
        condition = condition.replace(" ", "");
        String[] v = condition.split("£");
        boolean reverse = false;
        StringBuilder c = new StringBuilder();
        int counter = COUNTER - 1;
        int j;

        if (condition.contains("&") && condition.contains("|")) {
            appendComment(getIndent() + "Complex Instruction are not supported", getIndent());
            return "";
        }
        for (int i = 0; i < v.length; i++) {
            j = 0;

            if (i + 1 < v.length) {
                next = v[i + 1];
            } else {
                next = "£";
            }

            if (next.contains("&") || next.contains("£")) {
                reverse = !v[i].startsWith("!");

            } else if (next.startsWith("|")) {
                reverse = false;
                j = 1;
                if (v[i].startsWith("!")) {
                    reverse = true;
                }
            }

            String[] act = getCondition(v[i], reverse);
            String cmp = getIndent() + "CMP " + act[0] + ", " + act[2] + "\n";
            String branch = getIndent() + act[1] + " " + key[j] + "_" + counter + "\n";
            c.append(cmp).append(branch);
        }

        return c.toString();
    }

    /*----START OF UTILITIES----*/

    /**
     * This method returns array's type from the register
     *
     * @param register the string that contains the register
     * @return string that represents the array's type
     */
    // FIXME Is it sure that c[i] contains a dot? Otherwise this might raise an index range error...
    // If the line contains ":" then the generator is in GNU mode and it must contains a dot (see generateArrayInitialization)
    // if it doesn't then the generator is in Keil mode and the line doesn't contain a dot, but he will not enter the if condition.
    private String findArrayType(String register) {
        String[] c = code.getText().split("\n");
        String[] tokens;
        String arName = null;
        String type = "";
        for (int i = c.length - 1; i >= 0; i--) {
            /* If row i contains register, instruction adr and 'v'
             * then we found where the array gets assigned to a register.
             */
            if (c[i].contains(register) && c[i].contains("ADR") && c[i].contains("v")) {
                tokens = c[i].split(",");
                arName = tokens[tokens.length - 1].replace(" ", "");
            }
            /* If the row contains arName and ':' then it's the row where we assign the values
             */
            if (arName != null && c[i].contains(arName) && c[i].contains(":")) {
                type = c[i].split("\\.")[1].split(" ")[0];
                return type;
            }
        }
        return type;
    }

    /**
     * This method returns an array's size using the register
     *
     * @param register the string that represents the register
     * @return string array's size
     */
    private int returnDim(String register) {
        String r = findArrayType(register);
        if (r.contains("byte")) {
            return 0;
        } else if (r.contains("hword")) {
            return 1;
        } else if (r.contains("word")) {
            return 2;
        } else if (r.contains("quad")) {
            return 3;
        } else if (r.contains("octa")) {
            return 4;
        }
        return -1;
    }

    /**
     * This method checks if n is a power of two
     *
     * @param n initial value
     * @return boolean whether it's a power of two or not
     */
    private boolean isPowerOfTwo(int n) {
        if (n == 0) {
            return false;
        }

        double v = Math.log(n) / Math.log(2);
        return (int) (Math.ceil(v)) == (int) (Math.floor(v));
    }

    /**
     * This method removes redundant multiple labels (and abridges references to them)<br/>
     * EXAMPLES:<br/>
     * {@code end_0:}<br/>
     * {@code end_1:}<br/>
     * or<br/>
     * {@code end_0:}<br/>
     * &nbsp;{@code     B end_1}
     */
    private void unifyFlow() {
        // FIXME Are quoted commas likely to be in the code?? Why should they be equivalent to newline?
        // code contains strings like this "end_0:" so we need to remove the quotation marks
        String[] lines = code.toString().replace("\",\"", "\n").split("\n");

        for (int i = 0; i < lines.length - 1; i++) {
            if (lines[i].startsWith("end_")) {
                if (lines[i + 1].contains("B end_")) {
                    // lines[1] = end_0:
                    // lines[2] =     B end_1:
                    // Removes end_0: (replaces the label by an empty line)
                    //code.replaceInElements(lines[i], "");
                    code.replaceAll(lines[i], "");
                    // Redirects all jumps to the second label
                    code.replaceInElements(lines[i].replace(":", ""), lines[i + 1].replace(getIndent() + "B ", ""));
                }
                if (lines[i + 1].startsWith("end_")) {
                    // lines[1] = end_0:
                    // lines[2] = end_1:
                    /* FIXME Does not seem to make sense since colon or not depends on Gnu or KEIL
                     * both lines together should be: code.replaceAll(lines[i], "");
                     */
                    code.replaceAll(lines[i], lines[i] + ":");	// ???
                    code.replaceInElements(lines[i] + ":", "");	// 
                    /* FIXME There can't be any exact matching line anymore, but e.g. "Bxx end_12"
                     * might get replaced by "Bxx end_22" if lines[i] = "end_1:" and
                     * lines[i+1] = "end_2:", which can't be correct
                     */
                    code.replaceInElements(lines[i].replace(":", ""), lines[i + 1].replace(":", "").replace("\"]", ""));
                    // END KGU#968 2021-05-02
                }
            }
        }
    }

    /**
     * Adds the given line between the data section header and the text
     * section header.
     * 
     * @param line - the line to be inserted
     */
    private void addToDataSection(String line) {
        insertCode(line, 1);
    }

//    /**
//     * This method gets function's name
//     *
//     * @param _line string that contains a function
//     * @return string that contains the name
//     */
//    private String getFunction(String _line) {
//        String value;
//
//        if (_line.contains("<-") || _line.contains(":=")) {
//            String[] parts = _line.split("<-|:=");
//            value = parts[1].split("\\(")[0];
//        } else {
//            value = _line.split("\\(")[0];
//        }
//
//        return value;
//    }

    /**
     * This method translates number assignments that are too big for a register
     *
     * @param register - string that contains the register
     * @param value - string that contains the value
     * @return translated assignment as string
     */
    private String getInstructionConstant(String register, String value) {
        final int UINT12MAX = 4096;
        String c;
        try {
            if (value.contains("'")) {	// FIXME seems to be a check for char literal
                c = "MOV " + register + ", #" + value;
            } else if (Integer.parseInt(value) >= UINT12MAX) {	// FIXME what about negative values?
                c = "LDR " + register + ", =" + value;
            } else {
                c = "MOV " + register + ", #" + value;
            }
        } catch (NumberFormatException e) {
            //FIXME What if it does not comply with a hex literal, either?
            //inside generateAssignment this method should be called only if secondOperator is a number (decimal or hex)
            value = value.replace("0x", "");
            int hexValue = Integer.parseInt(value, 16);
            if (hexValue < UINT12MAX) {
                c = "MOV " + register + ", #0x" + value;
            } else {
                c = "LDR " + register + ", =0x" + value;
            }
        }
        return c;
    }

    /**
     * This method replaces variables with registers
     *
     * @param line - the string that contains the instruction to translate
     * @return the string with variables replaced with registers
     */
    private String variablesToRegisters(String line) {
        ArrayList<Tuple<String, Integer>> variables = getVariables(line);

        if (variables.size() == 0) {
            return line;
        }

        String register;
        StringBuilder replacedLine = new StringBuilder(line);

        int differenceLength = 0;

        for (Tuple<String, Integer> tuple : variables) {
            register = getRegister(tuple.variable);

            int end = tuple.position + differenceLength + 1;
            int start = end - tuple.variable.length();

            replacedLine.replace(start, end, register);
            differenceLength += register.length() - tuple.variable.length();
        }

        return replacedLine.toString();
    }

    /**
     * This method finds the variables that we need to replace with registers
     *
     * @param line - the string that contains the instruction to translate
     * @return the ArrayList that contains all variables and the respective positions in the string
     */
    private ArrayList<Tuple<String, Integer>> getVariables(String line) {
        // we need the space so if we have an ending variable it gets into the while loop
        line += " \0";
        String[] split = line.split("");

        // the tuple arraylist is useful to rebuild the string later
        ArrayList<Tuple<String, Integer>> stringPositions = new ArrayList<>();

        int i = 1;
        StringBuilder item = new StringBuilder();
        item.append(split[0]);

        while (i < split.length) {
            if (split[i].equals("\"")) {
                i++;
                while (!split[i].equals("\"") && i < split.length) {
                    i++;
                }
            }

            if (item.toString().matches(hexNumberPattern)) {
                item.append(split[i]);
            }
            // we check that the item is not a variable
            else if (!item.toString().matches(variablePattern)) {
                // as soon as it's not we remove the last non matching character
                String variable = item.substring(0, item.length() - 1);
                // if it's a register we add it as not available and, if it's already assigned to a variable, we warn the user
                if (variable.matches(registerPattern)) {
                    // START KGU#968 2021-10-11: Bugfix #967 case matters here! (Caused NullPointerExceptions)
                    variable = variable.toUpperCase();
                    // END KGU#968 2021-10-11
                    if ("".equals(mVariables.get(variable))) {
                        mVariables.put(variable, USER_REGISTER_TAG);
                    } else if (!mVariables.get(variable).equals(USER_REGISTER_TAG)) {
                        appendComment(String.format("Register: %s is already assigned to variable: %s. Be careful!\n", variable, mVariables.get(variable)), getIndent());
                    }
                }

                // if it's not a register and it's not empty and it's not in the reservedWords list we add it to the arraylist
                else if (!variable.equals("") && !Arrays.asList(reservedWords).contains(variable) && !variable.matches(hexNumberPattern)) {
                    stringPositions.add(new Tuple<>(variable, i - 2));
                }
                item = new StringBuilder(split[i]);
            } else {
                item.append(split[i]);
            }
            i++;
        }

        return stringPositions;
    }

    /**
     * Returns a register (name) not associated if there is still a vacant one <br/>
     * NOTE: Does not reserve the returned register in {@link #mVariables}!
     * 
     * @return either a register name or an empty string (if none is free)
     */
    private String getAvailableRegister() {
        String available = "";
        for (Map.Entry<String, String> entry1 : mVariables.entrySet()) {
            // we get the first available register
            if (entry1.getValue().equals("")) {
                available = entry1.getKey();
                break;
            }
        }

        return available;
    }

    /**
     * This method returns the register assigned to the given variable
     *
     * @param variable - the variable name
     * @return name of the register assigned to the variable, or an empty string
     *         if the set of variables is exhausted
     */
    private String getRegister(String variable) {
        String register = "";

        for (Map.Entry<String, String> entry : mVariables.entrySet()) {
            // if we have already a register for the variable we can skip the others
            if (entry.getValue().equals(variable)) {
                register = entry.getKey();
                break;
            }
        }

        // if there aren't any assigned registers to the variable
        if (register.equals("")) {
            register = getAvailableRegister();
            // START KGU#968 2021-04-30 We must not add an entry with empty key
            //mVariables.put(register, variable);
            if (!register.isEmpty()) {
                mVariables.put(register, variable);
            }
//            else {
//                // FIXME We should reuse another, little used register (LRU, Clock)s
//            }
            // END KGU#968 2021-04-30
        }

        return register;
    }

    /**
     * This method checks whether line is an arm instruction or not
     *
     * @param line contains the instruction
     * @return whether line is an arm instruction or not
     */
    private boolean isArmInstruction(String line) {
        // START KGU#968 2021-10-06: This was too vague and inefficient
        //String[] instruction = {
        //        "lsl", "lsr", "asr", "ror", "rrx", "adcs", "and", "eor", "sub", "rsb", "add", "adc",
        //        "sbc", "rsc", "bic", "orr", "mov", "tst", "teq", "cmp", "cmn", "sel", "mul", "mla",
        //        "smla", "smuadx", "smlsd", "smmla", "smmls", "mrs", "msr", "b", "ldr", "str", "ldm",
        //        "stm", "cpsie", "cpsid", "srs", "rfe", "setend", "cdp", "ldc", "stc", "mcr", "mrc",
        //        "mrrc", "swi", "bkpt", "pkhbt", "pkhtb", "sxtb", "sxth", "uxtb", "uxth", "sxtab",
        //        "sxtah", "uxtab", "uxtah", "ssat", "usat", "rev", "clz", "cpy", "cdc"
        //};
        //
        //if (line.contains("<-"))
        //    return false;
        //
        //String checkLine = line.split(" ")[0].toLowerCase();
        //for (String s : instruction) {
        //    if (checkLine.contains(s)) {
        //        return true;
        //    }
        //}
        if (ARM_INSTR_LOOKUP.isEmpty()) {
            // Fill the instruction name set once (lazy initialisation)
            for (String s: ARM_INSTRUCTIONS) {
                ARM_INSTR_LOOKUP.add(s);
            }
        }
        StringList tokens = Element.splitLexically(line.toLowerCase(), true);
        tokens.removeAll(" ");
        for (int i = 0; i < tokens.count(); i++) {
            // FIXME: Should we ignore the position? A variable "b" would easily provoke a false positive...
            if (ARM_INSTR_LOOKUP.contains(tokens.get(i))) {
                return true;
            }
        }
        // END KGU#968 2021-10-06
        return false;
    }

    /**
     * This method splits the current expression into a format where the second item of the array contains the operation,
     * the first and third items contain the operators
     *
     * @param expression represents the simple expression
     * @return an array containing the split expression
     */
    private String[] parseExpression(String expression) {
        //String[] expressionSplit = expression.split("");
        StringList tokens = Element.splitLexically(expression, true);
        ArrayList<String> result = new ArrayList<>();
        StringBuilder item = new StringBuilder();
        boolean afterOpr = true;

        //int i = 0;
        //while (i < expressionSplit.length) {
        for (int i = 0; i < tokens.count(); i++) {
            // If we find one of the supported operations inside the expression
            //if (expressionSplit[i].matches(supportedOperationsPattern) && !expressionSplit[i + 1].matches(numberPattern)) {
            String token = tokens.get(i);
            if (token.matches(supportedOperationsPattern) && !afterOpr) {
                result.add(item.toString()); // add the register
                //result.add(expressionSplit[i]); // add the operation
                if (token.equals("&&")) {	// add the operator symbol
                    result.add("&");	// FIXME this is an incorrect "approximation"
                }
                else if (token.equals("||")) {
                    result.add("|");	// FIXME this is an incorrect "approximation"
                }
                else {
                    result.add(token);
                }
                afterOpr = true;
                item = new StringBuilder(); // reset
            } else {
                //item.append(expressionSplit[i]);
                item.append(token);
                afterOpr = false;
            }

            //i++;
        }

        result.add(item.toString());

        return result.toArray(new String[result.size()]);
    }
}

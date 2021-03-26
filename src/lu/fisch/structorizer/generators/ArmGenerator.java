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
/**
 * @authors Daniele De Menna, Robert Dorinel Milos, Alessandro Simonetta, Giulio Palumbo, Maurizio Fiusco
 */
package lu.fisch.structorizer.generators;

import lu.fisch.structorizer.elements.*;
import lu.fisch.utils.StringList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class ArmGenerator extends Generator {

    /**
     * INSTRUCTIONS PATTERNS
     *
     */
    private final Pattern assignmentPattern = Pattern.compile("(R([0-9]|1[0-4])|[a-zA-Z]+)(<-|:=)(R([0-9]|1[0-4])|[a-zA-Z]+|-?[0-9]+|0x([0-9]|[a-f])+)", Pattern.CASE_INSENSITIVE);
    private final Pattern expressionAssignment = Pattern.compile("(R([0-9]|1[0-4])|[a-zA-Z]+)(<-|:=)(R([0-9]|1[0-4])|[a-zA-Z]+|[0-9]+|0x([0-9]|[a-f])+)(-|\\+|\\*|and|or)(R([0-9]|1[0-4])|[a-zA-Z]+|[0-9]+|0x([0-9]|[a-f])+)", Pattern.CASE_INSENSITIVE);
    private final Pattern memoryAssignmentPattern1 = Pattern.compile("(R([0-9]|1[0-4])|[a-zA-Z]+)(<-|:=)(memoria|memory)\\[(R([0-9]|1[0-4])|[a-zA-Z]+)(\\+(R([0-9]|1[0-4])|[a-zA-Z]+|[0-9]+|0x([0-9]|[a-f])+))?]", Pattern.CASE_INSENSITIVE);
    private final Pattern memoryAssignmentPattern2 = Pattern.compile("(memoria|memory)\\[(R([0-9]|1[0-4])|[a-zA-Z]+|[\\d]+)(\\+(R([0-9]|1[0-4])|[a-zA-Z]+|[0-9]+|0x([0-9]|[a-f])+))?](<-|:=)(R([0-9]|1[0-4])|[a-zA-Z]+)", Pattern.CASE_INSENSITIVE);
    private final Pattern arrayExpressionPattern = Pattern.compile("(R([0-9]|1[0-4])|[a-zA-Z]+)(<-|:=)(R([0-9]|1[0-4])|[a-zA-Z]+)\\[(R([0-9]|1[0-4])|[a-zA-Z]+|[\\d]+)(\\+(R([0-9]|1[0-4])|[a-zA-Z]+|[0-9]+|0x([0-9]|[a-f])+|[\\d]+))?]", Pattern.CASE_INSENSITIVE);
    private final Pattern arrayAssignmentPattern = Pattern.compile("(R([0-9]|1[0-4])|[a-zA-Z]+)\\[(R([0-9]|1[0-4])|[a-zA-Z]+|[\\d]+)(\\+(R([0-9]|1[0-4])|[a-zA-Z]+|[0-9]+|0x([0-9]|[a-f])+))?](<-|:=)(R([0-9]|1[0-4])|[a-zA-Z]+)", Pattern.CASE_INSENSITIVE);
    private final Pattern arrayInitializationPattern = Pattern.compile("(word|hword|byte|octa|quad)?(R([0-9]|1[0-4])|[a-zA-z]+[\\d]*)(<-|:=)\\{([\\d]+|0x([0-9]|[a-f])+)(,([\\d]+|0x([0-9]|[a-f])+))*}", Pattern.CASE_INSENSITIVE);
    private final Pattern address = Pattern.compile("R([0-9]|1[0-4])(<-|:=)(indirizzo|address)\\(([a-zA-Z]+|R([0-9]|1[0-4]))\\)", Pattern.CASE_INSENSITIVE);
    private final Pattern stringInitializationPattern = Pattern.compile("(R([0-9]|1[0-4])|[a-zA-z]+)(<-|:=)\"[\\w]{2,}\"");
    private final Pattern charInitializationPattern = Pattern.compile("(R([0-9]|1[0-4])|[a-zA-z]+)(<-|:=)(\"[\\w]\"|'[\\w]')");
    private final Pattern booleanAssignmentPattern = Pattern.compile("(R([0-9]|1[0-4])|[a-zA-z]+)(<-|:=)(true|false)");

    /**
     * ALTERNATIVE/WHILE PATTERNS
     *
     */
    private final Pattern conditionPattern = Pattern.compile("(while)?\\((R([0-9]|1[0-5])|[a-zA-Z]+)(==|!=|<|>|<=|>=|=)(R([0-9]|1[0-5])|[0-9]+|[a-zA-Z]+|0x([0-9]|[a-fA-F])+|'([a-zA-Z]|[0-9])')((and|AND|or|OR|&&|\\|\\|)(R([0-9]|1[0-5])|[a-zA-Z]+)(==|!=|<|>|<=|>=|=)(R([0-9]|1[0-5])|[0-9]+|[a-zA-Z]+|0x([0-9]|[a-fA-F])+|'([a-zA-Z]|[0-9])'))*\\)");

    private final boolean t = !Element.ARM_GNU;

    protected HashMap<String, TypeMapEntry> typeMap;
    private int arrayCounter = 0;
    private int COUNTER = 0;
    private String[] difference = {":", "#", ".data", ".text"};
    private HashMap<String, String> variables = new HashMap<>();

    /**
     * ********** Fields **********************
     */
    @Override
    protected String getDialogTitle() {
        return "Export ARM ...";
    }

    @Override
    protected String getFileDescription() {
        return ">ARM Assembly code";
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
        return t ? ";" : "//";
    }

    @Override
    protected OverloadingLevel getOverloadingLevel() {
        return OverloadingLevel.OL_NO_OVERLOADING;
    }

    protected boolean isLabelAtLoopStart() {
        return false;
    }

    @Override
    protected boolean breakMatchesCase() {
        return true;
    }

    @Override
    protected String getIncludePattern() {
        return "";
    }

    protected TryCatchSupportLevel getTryCatchLevel() {
        return TryCatchSupportLevel.TC_NO_TRY;
    }

    @Override
    protected String getInputReplacer(boolean withPrompt) {
        return "LDR $1";
    }

    @Override
    protected String getOutputReplacer() {
        return "STR $1";
    }

    protected String prepareUserIncludeItem(String _includeFileName) {
        boolean isQuoted = _includeFileName.startsWith("<") && _includeFileName.endsWith(">")
                || _includeFileName.startsWith("\"") && _includeFileName.endsWith("\"");
        if (!isQuoted) {
            _includeFileName = "\"" + _includeFileName + "\"";
        }
        return _includeFileName;
    }

    protected String prepareGeneratorIncludeItem(String _includeName) {
        _includeName = _includeName.trim();
        boolean isQuoted = _includeName.startsWith("<") && _includeName.endsWith(">")
                || _includeName.startsWith("\"") && _includeName.endsWith("\"");
        if (!isQuoted && !_includeName.endsWith(".h")) {
            _includeName = this.prepareUserIncludeItem(_includeName + ".h");
        }
        return _includeName;
    }

    protected boolean isInternalDeclarationAllowed() {
        return false;
    }

    // This method understands the type of the instruction and then calls the method for translate the line.
    protected void generateInstructionLine(String line, boolean isDisabled) {
        line = variablesToRegisters(line);
        switch (getMode(line)) {
            case "Assignment" ->
                generateAssignment(line, isDisabled);
            case "Expression" ->
                generateExpr(line, isDisabled);
            case "Memory" ->
                generateMemAss(line, isDisabled);
            case "AExpression" ->
                generateArrayExpr(line, isDisabled);
            case "AAssignment" ->
                generateArrayAssign(line, isDisabled);
            case "AInitialization" ->
                generateArrayIniz(line, isDisabled);
            case "Address" ->
                generateIndAss(line, isDisabled);
            case "BooleanAssignment" ->
                generateAssignment(line.replace("true", "1").replace("false", "0"), isDisabled);
            case "StringAInitialization" ->
                generateString(line, isDisabled);
            case "CharAInitialization" ->
                generateAssignment(line.replace("\"", "'"), isDisabled);
            case "Instruction" ->
                addCode(line, "\t\t", isDisabled);
            case "NotImplemented" ->
                appendComment("Error: Not implemented yet\n" + line, "\t\t");
        }
    }

    @Override
    public String generateCode(Root _root, String _indent, boolean _public) {
        if (t) {
            difference[2] = ";AREA data, DATA, READWRITE";
            difference[3] = ";AREA text, CODE, READONLY";
        }
        //

        if (topLevel) {
            code.add(difference[2]);
            code.add(difference[3] + "\n");
        }

        generateBody(_root, _indent);

        return code.getText();
    }

    @Override
    protected void generateCode(Instruction _inst, String _indent) {
        appendComment(_inst, _indent + "\t\t");

        if (t) {
            difference[0] = "";
            difference[1] = "";
        }

        boolean isDisabled = _inst.isDisabled(true);
        if (!appendAsComment(_inst, _indent)) {
            StringList lines = _inst.getUnbrokenText();
            for (int i = 0; i < lines.count(); i++) {

                String line = lines.get(i);

                generateInstructionLine(line, isDisabled);

            }
        }
    }

    // Generate code for If instruction
    @Override
    protected void generateCode(Alternative _alt, String _indent) {
        // Extract the text in the block
        String condition = _alt.getUnbrokenText().getLongString().trim();

        if (!conditionPattern.matcher(condition.replace(" ", "")).matches()) {
            appendComment("Wrong condition syntax", "\t\t");
            return;
        }

        // Check if the variable "t" is set on GNU or Keil.
        if (t) {
            difference[0] = "";
            difference[1] = "";
        }

        boolean isDisabled = _alt.isDisabled(true);
        appendComment(_alt, _indent + "\t\t");
        int counter = COUNTER;

        String c;
        String k;

        // Check if the q false exist (we wont empty code blocks)
        if (_alt.qFalse.getSize() != 0) {
            k = "else";
        } else {
            k = "end";
        }

        String[] key = {k, "then"};
        // Generate the alternative code with multiCondition
        c = multiCondition(condition, true, key);
        addCode(c, "", isDisabled);

        if (_alt.qTrue.getSize() != 0) {

            // If "then" block is not empty then we add the label
            addCode("then_" + counter + difference[0], "", isDisabled);
            // Generate the code in the then block
            generateCode(_alt.qTrue, "");
        }

        // Check the empty blocks for adding the right labels and the branch instructions
        if (_alt.qFalse.getSize() != 0) {
            if (_alt.qTrue.getSize() != 0) {
                addCode("B end_" + counter, "\t\t", isDisabled);
            }
            // Aggiungiamo il nome del blocco else
            addCode("else_" + counter + difference[0], "", isDisabled);
            // generiamo il codice del blocco else
            generateCode(_alt.qFalse, "");

        }
        // Adding the end labels at the end of the code
        addCode("end_" + counter + difference[0], "", isDisabled);
        // Remove the empty labels that were added (we could do it better)
        unifyFlow();
    }

    // Generate code for Switch instruction
    protected void generateCode(Case _case, String _indent) {
        appendComment(_case, _indent + "\t\t");

        if (t) {
            difference[0] = "";
            difference[1] = "";
        }

        boolean isDisabled = _case.isDisabled(true);
        int counter = COUNTER;

        // Extract the text in the block
        StringList lines = _case.getUnbrokenText();
        String variable = lines.get(0);
        String condition;

        String[] key = {"blocco", "default"};

        String count;

        // For each line we extract it and then translate the code
        for (int i = 0; i < _case.qs.size() - 1; i++) {
            count = "" + counter + "" + i + "";
            counter = Integer.parseInt(count);
            // Fromatting the string for translate it with the multiCondition method (like  Alternative block)
            condition = variable + "=" + lines.get(i + 1);
            condition = condition.replace(")", "").replace("(", "").replace("!", "").replace(" ", "");
            // Generate the code
            String c = multiCondition(condition, false, key);

            // add it
            addCode(c, "", isDisabled);

        }

        // Now we need to add the labels for the block
        for (int i = 0; i < _case.qs.size() - 1; i++) {
            count = "" + counter + "" + i + "";
            // Here we go
            addCode("blocco_" + count + ":\t\t", "", isDisabled);
            // And then we generate the code in the block
            generateCode(_case.qs.get(i), "");

            addCode("B end_" + counter, "\t\t", isDisabled);

        }

        // Here we generate the default block
        if (!lines.get(_case.qs.size()).trim().equals("%")) {
            addCode("default_" + counter + difference[0], "", isDisabled);
            generateCode(_case.qs.get(_case.qs.size() - 1), "");
        }

        addCode("end_" + counter + difference[0], "", isDisabled);
    }

    // Generate code for For instruction
    @Override
    protected void generateCode(For _for, String _indent) {
        appendComment(_for, _indent + "\t\t");

        // As always we check the t variable
        if (t) {
            difference[0] = "";
            difference[1] = "";
        }

        boolean isDisabled = _for.isDisabled(true);

        // Extract all the text from the block.
        String counterStr = _for.getCounterVar();
        String c;
        int counter = COUNTER;
        COUNTER = COUNTER + 1;
        String operation = "";

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
            if (!startValueStr.startsWith("#") && !startValueStr.startsWith("R")) {
                startValueStr = "#" + startValueStr;
            }
            if (!endValueStr.startsWith("#") && !endValueStr.startsWith("R")) {
                endValueStr = "#" + endValueStr;
            }

            if (!stepValueStr.startsWith("#") && !stepValueStr.startsWith("R")) {
                stepValueStr = "#" + stepValueStr;
            }

            //Write the code for the For
            String mov = "\t\tMOV " + counterStr + " , " + startValueStr + "\n";
            String cmp = "for_" + counter + difference[0] + "\n\t\tCMP " + counterStr + " ," + endValueStr + "\n";

            String bge = "\t\tBGE end_" + counter;
            operation = "\t\t" + op + " " + counterStr + " , " + counterStr + ", " + stepValueStr;

            // formatting a single string
            c = mov + cmp + bge;
        }
        // Adding the code
        addCode(c, "", isDisabled);
        // Generate the code into the block
        generateCode(_for.q, "");
        addCode(operation, "", isDisabled);

        // Adding the branch instruction and the label
        addCode("\t\tB for_" + counter, "", isDisabled);
        addCode("end_" + counter + difference[0], "", isDisabled);
        int s = counter + 1;

        // This part is something similar to unifyFlow (we can do it better)
        if (code.indexOf("end_" + counter + difference[0]) == code.indexOf("end_" + s + difference[0]) - 1) {
            code.replaceIfContains("B end_" + s, "B end_" + counter);
            code.replaceIfContains("end_" + s + difference[0], "");
            code.replaceIfContains("end_" + s, "end_" + counter);
        }
    }

    // Generate code for While instruction
    @Override
    protected void generateCode(While _while, String _indent) {
        // Extract the text
        String condition = _while.getUnbrokenText().getLongString().trim();

        if (!conditionPattern.matcher(condition.replace(" ", "")).matches()) {
            appendComment("Wrong condition syntax", "\t\t");
            return;
        }

        if (t) {
            difference[0] = "";
            difference[1] = "";
        }

        boolean isDisabled = _while.isDisabled(true);
        appendComment(_while, _indent + "\t\t");
        int counter = COUNTER;

        String[] key = {"end", "code"};
        // Remove all the chars that we don't need from the string
        condition = condition.replace("(", "").replace(")", "").replace("while", "").replace(" ", "");
        // Use multiCondition to translate the code again (like alternative)
        String c = multiCondition(condition, true, key);
        // Add the label
        addCode("while_" + counter + difference[0], "", isDisabled);
        // Add the code
        addCode(c, "", isDisabled);
        // Generate the code into the block
        generateCode(_while.q, _indent);
        // Add the label and the branch instruction
        addCode("B while_" + counter, "\t\t", isDisabled);
        addCode("end_" + counter + difference[0], "", isDisabled);
    }

    @Override
    protected void generateCode(Repeat _repeat, String _indent) {

        if (t) {
            difference[0] = "";
            difference[1] = "";
        }

        boolean isDisabled = _repeat.isDisabled(true);

        appendComment(_repeat, _indent + "\t\t");

        int counter = COUNTER;

        String[] key = {"do", "continue"};
        String condition = _repeat.getUnbrokenText().getLongString().trim();
        condition = condition.replace("until", "").replace("(", "").replace(")", "");

        String c = multiCondition(condition, false, key);

        addCode("do_" + counter + difference[0], "", isDisabled);

        generateCode(_repeat.q, "");

        addCode(c, "", isDisabled);
    }

    @Override
    protected void generateCode(Forever _forever, String _indent) {
        if (t) {
            difference[0] = "";
            difference[1] = "";
        }

        boolean isDisabled = _forever.isDisabled(true);
        appendComment(_forever, _indent + "\t\t");

        int counter = COUNTER;

        // Create While True Block
        addCode("whileTrue_" + counter + difference[0], "", isDisabled);
        generateCode(_forever.q, "");

        // Add pointer to While True Block
        addCode("B whileTrue_" + counter + "\n", "\t\t", isDisabled);

        if (code.count() > 0 && !code.get(code.count() - 1).isEmpty()) {
            addCode("", "", isDisabled);
        }
    }

    @Override
    protected void generateCode(Call _call, String _indent) {
        if (t) {
            difference[0] = "";
            difference[1] = "";
        }
        if (!appendAsComment(_call, _indent)) {
            boolean isDisabled = _call.isDisabled(true);
            appendComment(_call, _indent + "\t\t");
            StringList lines = _call.getUnbrokenText();
            String line = lines.get(0);
            String registers = usedRegisters();
            String functionName = getFunction(line);
            addCode("STMFD SP!, {" + registers + ", LR}", "\t\t", isDisabled);
            addCode("BL " + functionName, "\t\t", isDisabled);
            addCode("LDMFD SP!, {" + registers + ", LR}", "\t\t", isDisabled);
            addCode("MOV PC, LR", "\t\t", isDisabled);

        }
    }

    // Not supported
    @Override
    protected void generateCode(Jump _jump, String _indent) {
        appendComment("================= NOT SUPPORTED FIND AN EQUIVALENT =================", "");
    }

    // Not supported
    @Override
    protected void generateCode(Parallel _para, String _indent) {

        boolean isDisabled = _para.isDisabled(true);
        appendComment(_para, _indent + "\t\t");
        addCode("", "", isDisabled);
        appendComment("==========================================================", "");
        appendComment("================= NOT SUPPORTED FIND AN EQUIVALENT =================", "");
        appendComment("==========================================================", "");
        addCode("", "", isDisabled);
    }

    // Not supported for the moment
    @Override
    protected void generateCode(Try _try, String _indent) {

        appendComment(_try, _indent + "\t\t");
        this.appendComment("TODO: Find an equivalent for this non-supported try / catch block!", "");

    }

//////// TODO from this point we got all method used for supporting the translation (maybe we can put them in another class)
    /* Translates multiple conditions, takes the condition, counter, keywords as inputs
    EXAMPLE: condition = "R0 < R1 and R1 > R2", counter = 1, key[then, else]
    in prima posizione va il blocco contenente la keyword corrispondente al successo dell'operazione,
    in seconda posizione il blocco corrispondente al fallimento dell'operazione.

    If there are condition with "and" and "or" a good translation.
     */
    protected String multiCondition(String condition, boolean reverse, String[] key) {
        int counter = COUNTER;
        COUNTER = COUNTER + 1;
        String c = "";

        // If there are variables we give them a free register
        condition = variablesToRegisters(condition);
        // Replacing all the chars that we don't need
        condition = condition.replace("(", "").replace(")", "").replace(" ", "");
        // Chec for boolean char
        if (condition.contains("||") || condition.contains("&&") || condition.contains("and") || condition.contains("or")) {
            // call the method for split the Condition and then replace chars in the string
            c = splitCondition(condition, key);
            c = c.replace("and", "").replace("&", "").replace("or", "").replace("|", "").replace("not", "").replace("!", "");
        } else {
            String[] act = getVariable(condition, reverse);
            String cmp = "\t\tCMP " + act[0] + ", " + act[2] + "\n";
            String branch = "\t\t" + act[1] + " " + key[0] + "_" + counter;
            c = c + cmp + branch;
        }

        return c;
    }

    // This method is used for the instruction of operator like (=, >, <, ...)
    protected String[] getVariable(String condition, boolean reverse) {
        condition = condition.replace("==", "=");
        String op = "";
        String sep = "";

        // Se reverse è false allora ritorna l'operatione tradotta in ARM e il separatore di stringa corrispondente
        if (!reverse) {
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
        } // Altrimenti ritorna l'operazione opposta ed il separatore corrispondente
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

        // Mi preparo per separare tutta la stringa
        condition = condition.replace(sep, "£" + op + "£");
        // Separo la stringa
        String[] variable = condition.split("£");

        // TODO Change this for the other compiler
        // Aggiungo l'asterisco
        if (!variable[2].startsWith("R")) {
            variable[2] = "#" + variable[2] + "";
        }

        return variable;

    }


    /* Generate array code:
     * input  "word R0 = {1,2,3,4}"
     *
     */
    protected void generateArrayIniz(String line, boolean isDisabled) {
        String[] tokens = line.split("<-|:=");
        // Divido il nome della variabile e l'espressione

        String varName = tokens[0];
        String expr = tokens[1];
        String type = "";
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
            if (difference[0].equals(":")) {
                // Aggiungo il codice
                appendTop("v_" + arrayCounter + difference[0] + "\t" + type + "\t" + expr);
                addCode("ADR " + varName + ", v_" + arrayCounter, "\t\t", isDisabled);
            } else {
                appendTop("V_" + arrayCounter + "\t\tDCD" + expr);
                addCode("LDR " + varName + ", =V_" + arrayCounter, "\t\t", isDisabled);
            }

            // Aumento il contatore
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
            if (difference[0].equals(":")) {
                expr = expr.replace("{", "").replace("}", "");
                appendTop(varName + difference[0] + "\t\t" + type + "\t" + expr);
            } else {
                appendTop(varName + "\t\tDCD" + expr);
            }
        }

    }

    // Genera codice per Array:
    /*
     * input R0[R1] <- R2
     * STR R0, [R1, off]
     * LDR R0, [R1, off]
     * Single assignment
     */
    protected void generateArrayAssign(String line, boolean isDisabled) {
        String[] tokens = line.split("<-|:="); //R0[], R2

        String expr = tokens[1];
        String[] arr = tokens[0].split("\\["); //R0, R1]
        String arName = arr[0]; //R0

        if (!arr[1].contains("R")) { //R0[1], R2
            int index = Integer.parseInt(arr[1].replace("]", "").replace(" ", ""));

            if (returnDim(arName) != null) {
                int dim = Integer.parseInt(returnDim(arName));
                index = (int) (index * Math.pow(2, dim));
                addCode("STR " + expr + ", [" + arName + ", #" + index + "]", "\t\t", isDisabled);
            } else {
                appendComment("The array " + arName + " is not inizialized", "\t\t");
            }

        } else if (arr[1].contains("R")) {
            // word v <- {1, 2, 3}
            // [v]
            // [R1] == indirizzo(R1)
            // [R1, +- R0 || 1, SHIFT]
            // STR R0, [R1, R2] -> R0 -> memoria[R1+R2]
            // STR R0, [R1, R2] -> R0 -> memoria[R1+R2] array: v
            addCode("STR " + expr + ", " + "[" + arName + ", " + arr[1], "\t\t", isDisabled);

        } else {
            appendComment("Error, no free register or no ar type specified", "");
        }
    }


    /* Array
     * simple expression R0 = R1[1]
     */
    protected void generateArrayExpr(String line, boolean isDisabled) {
        line = line.replace(" ", "");
        String[] tokens = line.split("<-|:=");
        String dim = "0";

        String expr = tokens[1];
        String varName = tokens[0];

        // divide array name from expression
        String arName = expr.split("\\[")[0];

        // array size
        if (!t) {
            dim = returnDim(arName);
        }

        if (!tokens[1].split("\\[")[1].startsWith("R") && dim != null) {
            // Find index
            int index = Integer.parseInt(tokens[1].split("\\[")[1].replace("]", "").replace("#", ""));
            int d = Integer.parseInt(dim);
            // index times size
            index = (int) (index * Math.pow(2, d));

            addCode("\t\tLDR " + varName + ", [" + arName + ", " + difference[1] + index + "]", "", isDisabled);
        } else if (dim != null) {
            // Else we use the register as the index
            String rIndex = tokens[1].split("\\[")[1].replace("]", "");

            if (!rIndex.startsWith("R")) {
                rIndex = difference[1] + rIndex;
            }

            if (!dim.equals("0")) {
                addCode("LDR " + varName + ", [" + arName + ", " + rIndex + ", LSL #" + dim + "]", "\t\t", isDisabled);
            } else {
                addCode("LDR " + varName + ", [" + arName + ", " + rIndex + "]", "\t\t", isDisabled);
            }
        } else {
            appendComment("The array is not initialized", "\t\t");
        }
    }

    /* Variable assignment
     *
     */
    protected void generateAssignment(String line, boolean isDisabled) {
        line = line.replace(" ", "");

        String[] tokens = line.split("<-|:=");
        String c;

        String expr = tokens[1];    // Original expression
        String varName = tokens[0];

        if (expr.startsWith("R") || expr.startsWith("r")) {
            c = "MOV " + varName + ", " + expr;
        } else if (expr.contains("-")) {
            c = isNegative(varName, expr);
        } else {
            c = getInstructionConstant(varName, expr);
        }

        addCode(c, "\t\t", isDisabled);
    }

    // Simple expression without array
    protected void generateExpr(String line, boolean isDisabled) {
        line = line.replace(" ", "");
        String[] tokens = line.split("<-|:=");
        int op = -1;

        String expr = tokens[1];    // Original expression
        String varName = tokens[0];
        // Replace operations with ARM equivalents
        expr = expr.replace("+", "$ADD$").replace("-", "$SUB$").replace("*", "$MUL$")
                .replace("or", "$ORR$").replace("and", "$AND$");

        String[] variable = expr.split("\\$");
        // Due registers
        if (expr.contains("MUL") && variable[0].contains("R") && variable[2].contains("R")) {
            addCode("LSL " + varName + ", " + variable[0] + ", " + variable[2], "\t\t", isDisabled);
            op = 0;
        } // Variable register
        else if (expr.contains("MUL") && !variable[0].contains("R") && variable[2].contains("R")) {
            int val = Integer.parseInt(variable[0]);
            // Mi ricavo il valore per il quale shiftare
            op = multiply(val);
            if (isPowerOfTwo(val)) // aggiungo l'operazione di shift
            {
                addCode("LSL " + varName + ", " + variable[2] + ", " + "#" + op, "\t\t", isDisabled);
            } else if (isPowerOfTwo(val - 1)) // se il numero non era una potenza di due aggiungo l'operazione di add
            {
                addCode("ADD " + varName + ", " + variable[2] + ", LSL " + variable[2] + ", " + "#" + op, "\t\t", isDisabled);
            } else {
                addCode(String.format("MUL %s, %s, #%s", varName, variable[2], val), "\t\t", isDisabled);
            }
        } // Register variable
        else if (expr.contains("MUL") && variable[0].contains("R") && !variable[2].contains("R")) {
            // ricavo il valore per il quale shiftare
            int val = Integer.parseInt(variable[2]);
            boolean powerTwo = isPowerOfTwo(val);
            op = multiply(val);
            if (powerTwo) // aggiungo l'operazione di shift
            {
                addCode("LSL " + varName + ", " + variable[0] + ", " + "#" + op, "\t\t", isDisabled);
            } else if (isPowerOfTwo(val - 1)) {
                addCode("ADD " + varName + ", " + variable[0] + ", LSL " + variable[0] + ", " + "#" + op, "\t\t", isDisabled);
            } else {
                addCode(String.format("MUL %s, %s, #%s", varName, variable[0], val), "\t\t", isDisabled);
            }
        }

        line = line.replace(" ", "");

        if (variable.length > 3) {
            appendComment("\nComplex Expression are not supported for the moment check the instruction please:\n" + line, "");
            return;
        }

        // Inserisco come prima l'asterisco se non è presente e non si sta usando un registro
        if (!variable[0].startsWith("R") && !variable[0].startsWith("#")) {
            variable[0] = "#" + variable[0];
        }

        if (!variable[2].startsWith("R") && !variable[2].startsWith("#")) {
            variable[2] = "#" + variable[2];
        }

        // variable[1] contains the operation
        // varName contains the register's name
        // variabile[0] and variabile[2] the operators.
        if (op == -1) {
            addCode(variable[1] + " " + varName + ", " + variable[0] + ", " + variable[2], "\t\t", isDisabled);
        }
    }

    /*
     *  Array for address using indirizzo
     */
    protected void generateIndAss(String line, boolean isDisabled) {
        line = line.replace(" ", "");
        String[] tokens = line.split("<-|:=");

        String expr = tokens[1].replace("indirizzo", "").replace("address", "").replace("(", "").replace(")", "");

        if (line.contains("indirizzo") || line.contains("address")) {

            addCode("LDR " + tokens[0] + ", =" + expr, "\t\t", isDisabled);

        }
    }

    protected void generateMemAss(String line, boolean isDisabled) {
        String c = "";
        line = line.replace(" ", "");

        String[] tokens = line.split("<-|:=");
        String expr = tokens[1];
        String varName = tokens[0];

        String[] variable = null;

        if (expr.contains("memoria") || expr.contains("memory")) {
            expr = expr.replace("[", "").replace("]", "").replace("memoria", "").replace("memory", "");
            variable = expr.split("\\+");
            c = "LDR " + varName + ", [%s]";
        } else if (varName.contains("memoria") || varName.contains("memory")) {
            varName = varName.replace("[", "").replace("]", "").replace("memoria", "").replace("memory", "");
            variable = varName.split("\\+");
            c = "STR " + expr + ", [%s]";
        }

        assert variable != null;
        if (variable.length < 2 && !expr.contains("R")) {
            c = String.format(c, difference[1] + variable[0]);
        } else if (variable.length < 2 && expr.contains("R")) {
            c = String.format(c, variable[0]);
        } else if (!variable[0].contains("R")) {
            variable[0] = difference[1] + variable[0];
            c = String.format(c, variable[1] + ", " + variable[0]);
        } else if (!variable[1].contains("R")) {
            variable[1] = difference[1] + variable[1];
            c = String.format(c, variable[0] + ", " + variable[1]);
        } else if (variable[0].contains("R") && variable[1].contains("R")) {
            c = String.format(c, variable[0] + ", " + variable[1]);
        }

        addCode(c, "\t\t", isDisabled);
    }

    /**
     * Transforms a string in a char array
     *
     */
    public void generateString(String line, boolean isDisabled) {
        line = line.replace(" ", "");
        String[] split = line.split("<-|:=");
        split[1] = split[1].replace("\"", "");
        String c = "word %s<-{%s}";
        StringBuilder array = new StringBuilder();

        for (int i = 0; i < split[1].length(); i++) {
            array.append("'").append(split[1].charAt(i)).append("'");
            if (i != split[1].length() - 1) {
                array.append(", ");
            }
        }

        generateArrayIniz(String.format(c, split[0], array), isDisabled);
    }

    /*
     * Returns an available register
     * Else returns null
     */
    protected String notUsedRegister(String line) {
        String[] register = {"R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9", "R10", "R11"};

        for (String s : register) {
            String c = code.getText();
            if (!c.contains(s) && !line.contains(s)) {
                return s;
            }
        }
        return null;
    }

    /*
     * Finds array type
     */
    protected String findArrayType(String register) {
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


    /*
     *  Translates complex instructions, doesn't translate both and and or in the same expression
     */
    protected String splitCondition(String condition, String[] key) {
        String next;
        condition = condition.replace("or", "£|").replace("and", "£&").replace("||", "£|").replace("&&", "£&").replace("not", "#!").replace("!", "#!");
        condition = condition.replace(" ", "");
        String[] v = condition.split("£");
        boolean reverse = false;
        StringBuilder c = new StringBuilder();
        int counter = COUNTER - 1;
        int j;

        if (condition.contains("&") && condition.contains("|")) {
            appendComment("\t\tComplex Instruction are not supported", "\t\t");
            return "";
        }
        for (int i = 0; i < v.length; i++) {
            j = 0;
            // Se non è l'ultimo elemento estraiamo la successiva
            if (i + 1 < v.length) {

                next = v[i + 1];
            } // Altrimenti inseriamo un carattere di fine array
            else {
                next = "£";
            }

            // Se siamo in un and
            if (next.contains("&") || next.contains("£")) {
                // Se la condizione attuale Ã¨ in not il reverse non va effettuato
                reverse = !v[i].startsWith("!");

            } else if (next.startsWith("|")) {
                reverse = false;
                j = 1;
                if (v[i].startsWith("!")) {
                    reverse = true;
                }
            }

            String[] act = getVariable(v[i], reverse);
            String cmp = "\t\tCMP " + act[0] + ", " + act[2] + "\n";
            String branch = "\t\t" + act[1] + " " + key[j] + "_" + counter + "\n";
            c.append(cmp).append(branch);
        }

        return c.toString();
    }

    /*
     *  Returns array dim
     */
    protected String returnDim(String register) {

        String r = findArrayType(register);

        if (r.contains("byte")) {
            return "0";
        } else if (r.contains("hword")) {
            return "1";
        } else if (r.contains("word")) {
            return "2";
        } else if (r.contains("quad")) {
            return "3";
        } else if (r.contains("octa")) {
            return "4";
        }

        return null;

    }

    // Function for shift exponent
    protected int multiply(int m) {
        int count = 0;

        while (m > 0) {
            count++;
            m /= 2;
        }

        return count - 1;
    }


    /* Function to check if x is power of 2*/
    protected boolean isPowerOfTwo(int n) {
        if (n == 0) {
            return false;
        }

        double v = Math.log(n) / Math.log(2);
        return (int) (Math.ceil(v)) == (int) (Math.floor(v));
    }

    protected String getOperation(String expr) {

        if (expr.contains("+")) {
            return "+";
        }
        if (expr.contains("*")) {
            return "*";
        }
        if (expr.contains("-")) {
            return "-";
        }

        return null;
    }

    /*  getMode uses regexes to verify which ARM instruction is line1 */
    public String getMode(String line1) {
        String line = line1.replace(" ", "");
        String mode = "NotImplemented";
        if (booleanAssignmentPattern.matcher(line).matches()) {
            mode = "BooleanAssignment";
        } else if (assignmentPattern.matcher(line).matches()) {
            mode = "Assignment";
        } else if (expressionAssignment.matcher(line).matches()) {
            mode = "Expression";
        } else if (memoryAssignmentPattern1.matcher(line).matches()) {
            mode = "Memory";
        } else if (memoryAssignmentPattern2.matcher(line).matches()) {
            mode = "Memory";
        } else if (arrayExpressionPattern.matcher(line).matches()) {
            mode = "AExpression";
        } else if (arrayAssignmentPattern.matcher(line).matches()) {
            mode = "AAssignment";
        } else if (stringInitializationPattern.matcher(line).matches()) {
            mode = "StringAInitialization";
        } else if (charInitializationPattern.matcher(line).matches()) {
            mode = "CharAInitialization";
        } else if (arrayInitializationPattern.matcher(line).matches()) {
            mode = "AInitialization";
        } else if (address.matcher(line).matches()) {
            mode = "Address";
        } else if (isArmInstruction(line)) {
            mode = "Instruction";
        }
        return mode;
    }

    // Changes variables to a register
    public void setVariables(String line) {
        String Rl;
        String[] tokens = line.split("<-|:=|(R([0-9]|1[0-4])|0x([0-9]|[a-fA-F])+|[0-9]+|[+*-]|(memoria|memory)\\[|]|\\[)");

        for (String s : tokens) {
            if (!s.equals("")) {
                Rl = notUsedRegister(line);

                if (Rl == null) {
                    appendComment("NO FREE REGISTER", "\t\t");
                    return;
                }

                line = line + Rl; //i <- j + kR0R1R2

                if (!variables.containsKey(s)) {
                    variables.put(s, Rl);
                }
            }
        }
    }

    public void unifyFlow() {
        String[] lines = code.toString().replace("\",\"", "\n").split("\n");

        for (int i = 0; i < lines.length - 1; i++) {
            // lines[1] = end_0: t
            // lines[2] = B end_0:
            if (lines[i].startsWith("end_") && lines[i + 1].contains("B end_")) {
                // Removes end_0:
                code.replaceIfContains(lines[i], "");
                code.replaceIfContains(lines[i].replace(difference[0], ""), lines[i + 1].replace("\t\tB ", ""));
            }
            if (lines[i].startsWith("end_") && lines[i + 1].startsWith("end_")) {
                code.replaceAll(lines[i], lines[i] + ":");
                code.replaceIfContains(lines[i] + ":", "");
                code.replaceIfContains(lines[i].replace(difference[0], ""), lines[i + 1].replace(difference[0], "").replace("\"]", ""));
            }
        }
    }

    public void appendTop(String line) {
        code.insert(line, 1);
    }

    /*
     *   get a name of Function
     *   @Param function
     */
    protected String getFunction(String _line) {
        String value;

        if (_line.contains("<-") || _line.contains(":=")) {
            String[] parts = _line.split("<-|:=");
            value = parts[1].split("\\(")[0];
        } else {
            value = _line.split("\\(")[0];
        }

        return value;
    }

    protected String usedRegisters() {
        String[] register = {"R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9", "R10", "R11"};
        String _usedRegister = "";
        for (String s : register) {
            String c = code.getText();
            if (c.contains(s)) {
                if (_usedRegister.equals("")) {
                    _usedRegister += s;
                } else {
                    _usedRegister += ", " + s;
                }
            }
        }
        return _usedRegister;
    }

    public String isNegative(String register, String value) {
        int n;
        value = value.replace("-", "").replace("#", "");

        try {
            n = Integer.parseInt(value) - 1;
        } catch (NumberFormatException e) {
            value = value.replace("0x", "");
            n = Integer.parseInt(value, 16) - 1;
        }

        String hexValue = Integer.toHexString(n);

        return "MVN " + register + ", #0x" + hexValue;
    }

    public String getInstructionConstant(String register, String value) {
        int UINT12MAX = 4096;
        String c;
        try {
            if (value.contains("'")) {
                c = "MOV " + register + ", #" + value;
            } else if (Integer.parseInt(value) >= UINT12MAX) {
                c = "LDR " + register + ", =" + value;
            } else {
                c = "MOV " + register + ", #" + value;
            }
        } catch (NumberFormatException e) {
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

    /*
    Replaces variables with registers
     */
    public String variablesToRegisters(String line) {
        String[] lineSplit = line.split(" ?(<-|:=|\\{|}|\\[|\\(|\\)|-|\\+|\\*|<|>|>=|<=|==|=|,|!=|(indirizzo|address)\\([\\w]+\\)|(memoria|memory)\\[|]) ?");
        ArrayList<String> splitted = new ArrayList<>();

        for (String s : lineSplit) {
            if (!s.equals("")) {
                splitted.add(s);
            }
        }

        String newS = line;
        for (String s : splitted) {
            String f = checkVariables(s, newS);
            if (f != null && !isArmInstruction(line)) {
                newS = newS.replace(s, f);
            }
        }
        return newS;
    }

    /*
    Returns true if key is a register
     */
    public boolean isNotRegister(String key) {
        key = key.replace(" ", "").replace("r", "R");
        String[] registers = {"R1", "R2", "R3", "R0", "R4", "R5", "R6", "R7", "R8", "R9", "R10", "R11", "R12"};
        for (String s : registers) {
            if (key.contains(s)) {
                return false;
            }
        }

        return true;
    }

    /*
    Key contains the variable, oldKey contains already assigned variables
     */
    public void setVariables(String key, String oldKey) {
        String Rl;
        if (!key.equals("")) {
            Rl = notUsedRegister(key + oldKey);
            if (Rl == null) {
                appendComment("No Free Registers for variable: " + key, "\t\t");
                return;
            }

            if (!variables.containsKey(key)) {
                variables.put(key, Rl);
            }
        }
    }

    /*
       Returns key's register if exists, else it assigns one and returns it.
     */
    public String checkVariables(String key, String oldKey) {
        Pattern type = Pattern.compile("(word|octa|byte|hword) ?[\\w]+");

        try {
            Integer.parseInt(key);
        } catch (NumberFormatException e) {
            if (isNotRegister(key) && !key.startsWith("0x") && !key.contains("'") && !key.contains("\"") && !type.matcher(key).matches()) {
                if (!variables.containsKey(key)) {
                    setVariables(key, oldKey);
                }
                key = variables.get(key);
            }
        }

        return key;
    }

    // A list of prefixes that we can use for the debugger
    public boolean isArmInstruction(String line) {
        String[] instruction = {
            "lsl", "lsr", "asr", "ror", "rrx", "adcs", "and", "eor", "sub", "rsb", "add", "adc",
            "sbc", "rsc", "bic", "orr", "mov", "tst", "teq", "cmp", "cmn", "sel", "mul", "mla",
            "smla", "smuadx", "smlsd", "smmla", "smmls", "mrs", "msr", "b", "ldr", "str", "ldm",
            "stm", "cpsie", "cpsid", "srs", "rfe", "setend", "cdp", "ldc", "stc", "mcr", "mrc",
            "mrrc", "swi", "bkpt", "pkhbt", "pkhtb", "sxtb", "sxth", "uxtb", "uxth", "sxtab",
            "sxtah", "uxtab", "uxtah", "ssat", "usat", "rev", "clz", "cpy", "cdc"
        };
        line = line.toLowerCase();
        for (String s : instruction) {
            if (line.contains(s)) {
                return true;
            }
        }
        return false;
    }
}

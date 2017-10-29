package com.creativewidgetworks.goldparser.parser;

import static com.creativewidgetworks.goldparser.util.FileHelper.toInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import com.creativewidgetworks.goldparser.engine.Parser;
import com.creativewidgetworks.goldparser.engine.ParserException;
import com.creativewidgetworks.goldparser.engine.Position;
import com.creativewidgetworks.goldparser.engine.Production;
import com.creativewidgetworks.goldparser.engine.Reduction;
import com.creativewidgetworks.goldparser.engine.Token;
import com.creativewidgetworks.goldparser.engine.enums.ParseMessage;
import com.creativewidgetworks.goldparser.util.FormatHelper;
import com.creativewidgetworks.goldparser.util.ResourceHelper;

/**
 * General purpose wrapper class used to process source input and generate
 * a parse tree.
 * 
 * <br>Dependencies:
 * <ul>
 * <li>{@link Parser}</li>
 * <li>{@link Reduction}</li>
 * <li>{@link Scope}</li>
 * <li>{@link FormatHelper}</li>
 * <li>{@link ResourceHelper}</li>
 *  </ul>
 * 
 * @author Ralph Iden (http://www.creativewidgetworks.com)
 * @version 5.0.0 
 */
public class GOLDParser extends Parser {
    // Virtual terminal constants
    public static final String VT_INDENT_DECREASE = "IndentDecrease";
    public static final String VT_INDENT_INCREASE = "IndentIncrease";
    
    private Map<String, Class> ruleHandlers = new TreeMap<String, Class>();

    private boolean ignoreCase;
    private boolean generateTree;
    
    private Reduction root;
    
    private List<String> messages = new ArrayList<String>();
    
    private Scope currentScope;
    private Map<String, Scope> scopes;
    
    // Indentation-sensitive support
    private boolean useIndentVirtualTerminals;
    private int ivtLine;
    private Stack<Token> ivtTokens = new Stack<Token>();
    private Stack<Position> ivtIndentLevels = new Stack<Position>();
    
    /*----------------------------------------------------------------------------*/

    public GOLDParser() {
        setCurrentScope(new Scope("GLOBAL"));
    }

    /*----------------------------------------------------------------------------*/

    public GOLDParser(GOLDParserBuildContext context) {
        this();
        try {
            loadTables(context.grammar());
            loadRuleHandlers(context.ruleClasses());
            setTrimReductions(context.trimReductions());
            if (ruleHandlers.size() == 0) {
                throw new IllegalStateException(FormatHelper.formatMessage("messages", "error.handlers_none"));
            }
        } catch (Exception e) {
            addErrorMessage(FormatHelper.formatMessage("messages", "error.table_unloadable", e.getMessage()));
        }
    }

    /*----------------------------------------------------------------------------*/

    public GOLDParser(File cgtFile, String rulesPackage, boolean trimReductions) {
        this();
        try {
            loadTables(cgtFile);
            loadRuleHandlers(rulesPackage);
            setTrimReductions(trimReductions);
            if (ruleHandlers.size() == 0) {
                throw new IllegalStateException(FormatHelper.formatMessage("messages", "error.handlers_none", rulesPackage));
            }
        } catch (Exception e) { 
            addErrorMessage(FormatHelper.formatMessage("messages", "error.table_unloadable", e.getMessage()));
        }
    }

    /*----------------------------------------------------------------------------*/

    public GOLDParser(InputStream cgtFile, String rulesPackage, boolean trimReductions) {
        this();
        try {
            loadTables(cgtFile);
            loadRuleHandlers(rulesPackage);
            setTrimReductions(trimReductions);
            if (ruleHandlers.size() == 0) {
                throw new IllegalStateException(FormatHelper.formatMessage("messages", "error.handlers_none", rulesPackage));
            }
        } catch (Exception e) {
            addErrorMessage(FormatHelper.formatMessage("messages", "error.table_unloadable", e.getMessage()));
        }
    }

    /*----------------------------------------------------------------------------*/

    public void clear() {
        restart();
        getScopes().clear();
        clearProgramVariables();
        getErrorMessages().clear();
        setCurrentScope(new Scope("GLOBAL"));  
    }
    
    /*----------------------------------------------------------------------------*/

    /**
     * Clears the variable map of all values for all scopes
     */
    public void clearProgramVariables() {
        for (Scope scope : scopes.values()) {
            scope.getVariables().clear();
        }
    }

    /**
     * Clears (removes) the specified variable from the variable map starting at
     * the current scope. If the variable is found, it is removed and no further
     * scope processing is performed.
     * @param name the variable to remove.
     */
    public void clearProgramVariable(String name) {
        String key = ignoreCase ? name.toUpperCase() : name;

        Scope scope = currentScope;
        while (scope != null) {
            if (scope.getVariables().containsKey(key)) {
                scope.getVariables().remove(key);
                break;
            }
            scope = scope.getParent();
        }
    }

    /**
     * Gets the specified variable from the map starting at the current scope. If
     * the variable is found, the Variable is immediately returned and no further
     * scope processing is performed.
     * @param name of the variable to retrieve
     * @return a Variable instance or null if the variable isn't found
     */
    public Variable getProgramVariable(String name) {
        String key = ignoreCase ? name.toUpperCase() : name;

        Scope scope = currentScope;
        while (scope != null) {
            if (scope.getVariables().containsKey(key)) {
                return scope.getVariables().get(key);
            }
            scope = scope.getParent();
        }

        return null;
    }

    /**
     * Sets the specified variable in the current scope's variable map, replacing
     * any existing variable in the map.
     * @param name of the variable to set
     * @param value to set, null is not recommended (use clearProgramVariable() instead)
     */
    public void setProgramVariable(String name, Variable value) {
        if (currentScope == null) {
            currentScope = new Scope(); // create new global scope
        }
        currentScope.getVariables().put(ignoreCase ? name.toUpperCase() : name, value);
    }    
    
    /*----------------------------------------------------------------------------*/

    public Map<String, Scope> getScopes() {
        if (scopes == null) {
            scopes = new TreeMap<String, Scope>();
        }
        return scopes;
    }

    public Scope getCurrentScope() {
        return currentScope;
    }

    public Scope setCurrentScope(Scope newScope) {
        Scope oldScope = currentScope;
        currentScope = newScope;

        if (currentScope != null && !getScopes().containsKey(newScope.getName())) {
            getScopes().put(newScope.getName(), newScope);
        }

        return oldScope;
    }    
    
    /*----------------------------------------------------------------------------*/

    public boolean getGenerateTree() {
        return generateTree;
    }
    
    public void setGenerateTree(boolean generateTree) {
        this.generateTree = generateTree;
    }
    
    /*----------------------------------------------------------------------------*/

    /**
     * Parser and rule handlers should throw ParseExceptions using this method because
     * it also adds the message to the list of parser errors.  Rule handlers that 
     * throw ParserExceptions instead of calling this method cannot communicate the reason 
     * the exception was thrown.
     * @param msg to add to the errors list
     * @throws ParserException 
     */
    public void raiseParserException(String msg) throws ParserException {
        addErrorMessage(msg);
        throw new ParserException(msg);
    }

    /**
     * Parser and rule handlers should throw ParseExceptions using this method because
     * it also adds the message to the list of parser errors.  Rule handlers that 
     * throw ParserExceptions instead of calling this method cannot communicate the reason 
     * the exception was thrown.
     * @param msg to add to the errors list
     * @param cause
     * @throws ParserException 
     */
    public void raiseParserException(String msg, Throwable cause) throws ParserException {
        addErrorMessage(msg);
        throw new ParserException(msg, cause);
    }
    
    /*----------------------------------------------------------------------------*/
    
    public List<String> getErrorMessages() {
        return messages;
    }

    public String getErrorMessage() {
        int errorCount = getErrorMessages().size();
        
        StringBuilder sb = new StringBuilder();
        for (String line : getErrorMessages()) {
            sb.append(line);
            if (errorCount > 1) {
                sb.append("\r\n");
            }
        }
        
        return sb.toString();
    }
    
    public void addErrorMessage(String message) {
        messages.add(message);
    }
    
    public boolean isReady() {
        return messages.size() == 0;
    }

    /*----------------------------------------------------------------------------*/
    
    public boolean isIgnoreCaseOfVariables() {
        return ignoreCase;
    }

    public void setIgnoreCaseOfVariables(boolean ignoreCaseOfVariableNames) {
        this.ignoreCase = ignoreCaseOfVariableNames;
    }
    
    /*----------------------------------------------------------------------------*/
    
    /**
     * Initialize the parser engine using the specified grammar (CGT) file
     * @param grammarFile file to load
     * @return true if grammar file was loaded
     * @throws IOException if an invalid file was specified or the file could not be opened.
     */
    public boolean setup(File grammarFile) throws IOException {
        if (grammarFile == null) {
            throw new IOException(FormatHelper.formatMessage("messages", "error.cgt_missing"));
        }
        return loadTables(grammarFile);
    }

    /*----------------------------------------------------------------------------*/

    public boolean parseSourceStatements(String sourceStatements) {
        getErrorMessages().clear();
        
        if (sourceStatements == null || sourceStatements.trim().length() == 0) {
            addErrorMessage(FormatHelper.formatMessage("messages", "error.no_source"));
            return false;
        }
        
        return parseSourceStatements(new StringReader(sourceStatements));
    }
    
    public boolean parseSourceStatements(Reader sourceReader) {
        boolean accept = false;
        
        getErrorMessages().clear();

        if (sourceReader == null) {
            addErrorMessage(FormatHelper.formatMessage("messages", "error.no_source_reader"));
            return false;
        }
        
        // Enable support for indentation sensitive grammars
        useIndentVirtualTerminals = getSymbolByName(VT_INDENT_INCREASE) != null;
        
        open(sourceReader);
        
        boolean done = false;
        while (!done) { 
            ParseMessage response = parse();
            switch (response) {
                case ACCEPT:
                    done = accept = processAccept();
                    break;
                    
                case GROUP_ERROR:
                    done = processGroupError();
                    break;
                    
                case INTERNAL_ERROR:
                    done = processInternalError();
                    break;
                    
                case LEXICAL_ERROR:
                    done = processLexicalError();
                    break;
                    
                case NOT_LOADED_ERROR:
                    done = processNotLoadedError();
                    break;
                    
                case REDUCTION:
                    done = processReduction();
                    break;
                    
                case SYNTAX_ERROR:
                    done = processSyntaxError();
                    break;
                    
                case TOKEN_READ:
                    done = processTokenRead();
                    break;
                    
                case UNDEFINED:
                    done = true;
                    addErrorMessage(FormatHelper.formatMessage("messages", "error.invalid_result"));
                    break;
            }
        }
    
        return accept;
    }

    protected boolean processAccept() {
        root = getCurrentReduction();
        return true;
    }
    
    protected boolean processGroupError() {
        addErrorMessage(FormatHelper.formatMessage("messages", "error.group_runaway", 
            getCurrentPosition().getLineAsString(), 
            getCurrentPosition().getColumnAsString())); 
        
        return true;
    }

    public boolean processInternalError() {
        addErrorMessage(FormatHelper.formatMessage("messages", "error.internal", 
                getCurrentPosition().getLineAsString(), 
                getCurrentPosition().getColumnAsString())); 
        
        return true;
    }
    
    protected boolean processLexicalError() {
        String tokenStr = getCurrentToken().toString();
        
        addErrorMessage(FormatHelper.formatMessage("messages", "error.lexical", 
                getCurrentPosition().getLineAsString(), 
                getCurrentPosition().getColumnAsString(), 
                tokenStr)); 
        
        return true;
    }
    
    protected boolean processNotLoadedError() {
        addErrorMessage(FormatHelper.formatMessage("messages", "error.table_not_loaded"));
        return true;
    }

    /**
     * Base parser builds a tree of Reduction objects
     * Override to process reductions
     * @return Boolean to indicate if processing should stop (true) or continue (false). 
     */
    protected boolean processReduction() {
        if (!generateTree && ruleHandlers.size() > 0) {
            try {
                Reduction reduction = createInstance();
                setCurrentReduction(reduction);
            } catch (Throwable t) {
                addErrorMessage(t.getMessage());
                return true;
            }
        }
        return false;
    }
    
    protected boolean processSyntaxError() {
        String tokenStr = getCurrentToken().toString();
        
        addErrorMessage(FormatHelper.formatMessage("messages", "error.syntax", 
                getCurrentPosition().getLineAsString(), 
                getCurrentPosition().getColumnAsString(), 
                tokenStr,
                getExpectedSymbols().toString())); 
        
        return true;
    }    
    
    protected boolean processTokenRead() {
        return false;
    }
    
    /*----------------------------------------------------------------------------*/

    public String getParseTree() {
        StringBuilder tree = new StringBuilder();
        if (generateTree && root != null) {
            tree.append("+-").append(root.getParent()).append("\r\n");
            drawReduction(tree, root, 1);
        } else {
            tree.append(FormatHelper.formatMessage("messages", "error.tree_unavailable"));
        }
        return tree.toString();
    }

    private void drawReduction(StringBuilder tree, Reduction reduction, int indent) {
        StringBuilder indentStr = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            indentStr.append("| ");
        }

        for (Token token : reduction) {
            switch (token.getType()) {
                case NON_TERMINAL:
                    tree.append(indentStr).append("+-").append(token.asReduction().getParent()).append("\r\n");
                    drawReduction(tree, token.asReduction(), indent + 1);
                    break;
                    
                default:
                    tree.append(indentStr).append("+-").append(token.asString()).append("\r\n");
                    break;
            }
        }
    }
    
    /*----------------------------------------------------------------------------*/

    public void loadRuleHandlers(String packageName) {
        loadRuleHandlers(listClassesInPackage(packageName));
    }

    public void loadRuleHandlers(List<Class> ruleClasses) {
        @SuppressWarnings("rawtypes")
        Map<String, Class> mapRuleHandlers = mapRuleClasses(ruleClasses);
        if (!mapRuleHandlers.isEmpty()) {
            ruleHandlers.clear();
            ruleHandlers.putAll(mapRuleHandlers);
        }
    }

    protected List<Class> listClassesInPackage(String packageName) {
        try {
            return ResourceHelper.findClassesInPackage(packageName);
        } catch (Exception e) {
            addErrorMessage("loadRuleMappings: " + e.getMessage());
            return Collections.<Class> emptyList();
        }
    }

    protected Map<String, Class> mapRuleClasses(Iterable<Class> classes) {
        try {
            Map<String, Class> mapRuleHandlers = new HashMap<String, Class>();
            for (Class clazz : classes) {
                ProcessRule a = (ProcessRule) clazz.getAnnotation(ProcessRule.class);
                if (a != null) {
                    for (String rule : a.rule()) {
                        mapRuleHandlers.put(rule, clazz);
                    }
                }
            }
            return mapRuleHandlers;
        } catch (Exception e) {
            addErrorMessage("loadRuleMappings: " + e.getMessage());
            return Collections.emptyMap();
        }
    }
    
    public List<String> validateHandlersExist() throws ParserException {
        List<String> errors = new ArrayList<String>();
        for (Production production : productionTable) {
            String rule = production.toString();
            if (ruleHandlers.get(rule) == null && ruleHandlers.get(rule.replace("'", "")) == null) {
                errors.add(FormatHelper.formatMessage("messages", "error.handler_none", rule));
            }
        }
        return errors;
    }
    
    public void validateHandlerExists(Production rule) throws ParserException {
        if (ruleHandlers.get(rule.toString()) == null) {
            raiseParserException(FormatHelper.formatMessage("messages", "error.handler_none", rule));
        }
    }
    
    protected Reduction createInstance() throws ParserException {
        Reduction currentReduction = getCurrentReduction();
        String ruleName = currentReduction.getParent().toString();
        return createInstance(ruleName);
    }
    
    protected Reduction createInstance(String ruleName) throws ParserException {
        Reduction reduction = null;

        // Look up the handler for the rule and construct an instance of the class
        Class clazz = ruleHandlers.get(ruleName);
        if (clazz == null) {
            clazz = ruleHandlers.get(ruleName.replace("'", "")); // Try removing single quotes
        }
        if (clazz != null) {
            Constructor con = null;
            try {
                con = clazz.getConstructor(GOLDParser.class);
            } catch (SecurityException e) {
                raiseParserException(FormatHelper.formatMessage("messages", "error.handler_security", e));
            } catch (NoSuchMethodException e) {
                raiseParserException(FormatHelper.formatMessage("messages", "error.handler_constructor", clazz.getSimpleName()));
            }

            try {
                reduction = (Reduction)con.newInstance(this);
            } catch (IllegalArgumentException e) {
                raiseParserException(FormatHelper.formatMessage("messages", "error.handler_create", ruleName), e);
            } catch (InstantiationException e) {
                raiseParserException(FormatHelper.formatMessage("messages", "error.handler_create", ruleName), e);
            } catch (IllegalAccessException e) {
                raiseParserException(FormatHelper.formatMessage("messages", "error.handler_create", ruleName), e);
            } catch (InvocationTargetException e) {
                raiseParserException(FormatHelper.formatMessage("messages", "error.handler_create", ruleName), e);
            } catch (Throwable t) {
                raiseParserException(FormatHelper.formatMessage("messages", "error.handler_create", ruleName), t);
            }
        } else {
            throw new ParserException(FormatHelper.formatMessage("messages", "error.handler_none", ruleName));
        }

        return reduction;
    }
    
    /*----------------------------------------------------------------------------*/
    
    /**
     * Helper function to return the current ident virtual token index levels.  This
     * method supports the ability indent location values for languages like Python
     * where indent levels must match.
     */
    private String getIndentLevels() {
        StringBuilder sb = new StringBuilder();
        for (int i = ivtIndentLevels.size() - 1; i >= 0; i--) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(ivtIndentLevels.get(i).getColumnAsString());
        }
        return sb.toString();
    }
    
    /**
     * Return next token - insert IndentIncrease/IndentDecrease tokens as needed to support
     * grammars where indentation is important (e.g. Python, etc.)
     * @return Token
     */
    @Override
    protected Token nextToken() {
        Token token;

        // Indent virtual terminals
        if (useIndentVirtualTerminals) {
            if (ivtTokens.isEmpty()) {
                // Get next token from stream
                token = produceToken();
                
                // Token's position in source - initialize indentLevel stack
                Position position = token.getPosition();
                if (ivtIndentLevels.isEmpty()) {
                    ivtLine = position.getLine();
                    ivtIndentLevels.push(position);
                }
                
                // Trigger on change of line number
                if (position.getLine() != ivtLine) {
                    ivtLine = position.getLine();
                    int ivtColumn = ivtIndentLevels.peek().getColumn();
                    if (position.getColumn() > ivtColumn) {
                        ivtTokens.push(token);
                        ivtIndentLevels.push(position);
                        token = new Token(getSymbolByName(VT_INDENT_INCREASE), getIndentLevels(), position);
                    } else if (token.getPosition().getColumn() < ivtColumn) {
                        ivtTokens.push(token);
                        while (!ivtIndentLevels.isEmpty() && ivtIndentLevels.peek().getColumn() > position.getColumn()) {
                            ivtIndentLevels.pop();
                            ivtTokens.push(new Token(getSymbolByName(VT_INDENT_DECREASE), getIndentLevels(), position));
                        }
                        token = ivtTokens.pop();
                    }
                }
                
            } else {
                // Return token pushed on the stack because of insertion of IndentIncrease/IndentDecrease insertion
                token = ivtTokens.pop();
            }
        } else {
            // Not an indentation sensitive grammar, just get the next token from the stream
            token = produceToken();
        }
            
        return token;
    }
    
}

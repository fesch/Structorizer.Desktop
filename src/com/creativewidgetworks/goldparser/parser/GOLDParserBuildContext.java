/*
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2018-09-18      Raw types (Class etc.) replaced by type inference 
 *      
 ******************************************************************************************************
 */
package com.creativewidgetworks.goldparser.parser;

import static com.creativewidgetworks.goldparser.util.FileHelper.toInputStream;
import static com.creativewidgetworks.goldparser.util.FormatHelper.formatMessage;
import static com.creativewidgetworks.goldparser.util.ResourceHelper.findClassesInPackage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public final class GOLDParserBuildContext {
    private InputStream grammar;
    //@SuppressWarnings("rawtypes")
    private List<Class<?>> ruleClasses;
    private boolean trimReductions = true;

    public GOLDParserBuildContext() {
    }

    public static GOLDParserBuildContext newContext() {
        return new GOLDParserBuildContext();
    }

    public GOLDParserBuildContext grammar(InputStream stream) {
        this.grammar = stream;
        return this;
    }

    public GOLDParserBuildContext grammar(File file) throws IOException {
        return grammar(toInputStream(file));
    }

    //@SuppressWarnings("rawtypes")
    public GOLDParserBuildContext ruleClasses(List<Class<?>> ruleClasses) {
        if (ruleClasses.size() == 0) {
            throw new IllegalStateException(formatMessage("messages", "error.handlers_none"));
        }
        this.ruleClasses = ruleClasses;
        return this;
    }

    public GOLDParserBuildContext rulesPackage(String rulesPackage) throws ClassNotFoundException, IOException {
        return ruleClasses(findClassesInPackage(rulesPackage));
    }

    public GOLDParserBuildContext trimReductions(boolean trimReductions) {
        this.trimReductions = trimReductions;
        return this;
    }

    /**
     * @return the {@link InputStream} to use to read the grammar
     */
    public InputStream grammar() {
        return grammar;
    }

    /**
     * @return the list of {@link com.creativewidgetworks.goldparser.engine.Reduction} that are annotated with
     *         {@link ProcessRule}
     */
    public List<Class<?>> ruleClasses() {
        return ruleClasses;
    }

    public boolean trimReductions() {
        return trimReductions;
    }

}
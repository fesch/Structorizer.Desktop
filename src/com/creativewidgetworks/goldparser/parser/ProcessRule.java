package com.creativewidgetworks.goldparser.parser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)

/**
 * ProcessRule 
 *
 * Annotation definition that tags an instance of a rule handler to
 * describe which rules the class handles.
 *
 * <br>Dependencies: None
 *
 * @author Ralph Iden (http://www.creativewidgetworks.com)
 * @version 5.0.0 
 */
public @interface ProcessRule {
    public String[] rule();
}

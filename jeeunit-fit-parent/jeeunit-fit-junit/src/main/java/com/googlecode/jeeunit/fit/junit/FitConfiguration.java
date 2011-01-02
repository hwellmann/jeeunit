package com.googlecode.jeeunit.fit.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides the configuration for a suite of FIT tests run by JUnit.
 * @see FitSuite
 * 
 * @author hwellmann
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FitConfiguration {

    String inputDir() default ".";
    String outputDir() default ".";
    String[] includes() default "**/*.html";
    String[] excludes() default {};
}

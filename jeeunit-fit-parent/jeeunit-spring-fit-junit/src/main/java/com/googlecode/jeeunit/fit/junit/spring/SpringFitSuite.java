package com.googlecode.jeeunit.fit.junit.spring;

import org.junit.runners.model.InitializationError;
import org.springframework.test.context.ContextConfiguration;

import com.googlecode.jeeunit.fit.junit.FitSuite;

import fit.FixtureLoader;

/**
 * Use this class to run a suite of FIT tests under JUnit. To do so, create a class with the
 * following annotations:
 * <pre>
 * &#64;RunWith(SpringFitSuite.class)
 * &#64;FitConfiguration(
 *     inputDir = "src/test/fit", 
 *     outputDir = "target/fit", 
 *     includes = "**&#47;*.fit.html")
 * public class MyFitSuite {
 * }
 * </pre>
 * 
 * The suite first builds a list of input files to be run as FIT tests. The input files are
 * located under the {@code inputDir} root. The file patterns to be included or excluded are specified
 * by the {@code includes} and {@code excludes} properties. The default include pattern is
 * {@code **&#47;*.html}, the exclude pattern list is empty by default.
 * Each of these properties can be a list of Strings
 * interpreted as Ant file patterns.
 * <p>
 * The suite then runs all files matching at least one of the include patterns and none of the
 * exclude patterns.
 * <p>
 * Each test file will be a direct child of the suite, named by its relative path.
 * <p>
 * Each fixture class referenced from the FIT test files needs to be annotated with 
 * {@link ContextConfiguration} to specify the Spring context configuration. Spring dependency
 * injection will be performed automatically after fixture instantiation.
 * 
 * @author hwellmann
 *
 */
public class SpringFitSuite extends FitSuite {

    public SpringFitSuite(Class<?> klass) throws InitializationError {
        super(klass);
        
        FixtureLoader.setInstance(new SpringFixtureLoader());
    }

}

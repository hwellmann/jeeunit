package com.googlecode.jeeunit.mojo.fit.spring;

import org.apache.maven.plugin.MojoExecutionException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;

import com.googlecode.jeeunit.mojo.fit.ClassLoaderFixtureLoader;

import fit.Fixture;

/**
 * A fixture loader which performs Spring dependency injection on all fixtures annotated
 * with {@link ContextConfiguration}, using a Spring Test Context.
 * 
 * @author Harald Wellmann
 *
 */
public class SpringFixtureLoader extends ClassLoaderFixtureLoader {

    /**
     * Constructor.
     * @param fixtureClassLoader class loader for fixtures
     */
    public SpringFixtureLoader(ClassLoader fixtureClassLoader) {
        super(fixtureClassLoader);
    }

    /**
     * Loads a fixtures class, creates an instance and performs Spring dependency injection
     * if class is annotated with {@link ContextConfiguration}.
     * 
     * @param fixtureClassName name of fixture class
     */
    @Override
    public Fixture disgraceThenLoad(String fixtureClassName) throws Throwable {
        Fixture fixture = super.disgraceThenLoad(fixtureClassName);
        Class<? extends Fixture> clazz = fixture.getClass();
        ContextConfiguration cc = clazz.getAnnotation(ContextConfiguration.class);
        if (cc != null) {
            TestContextManager contextManager = new TestContextManager(clazz);
            try {
                contextManager.prepareTestInstance(fixture);
            }
            catch (Exception exc) {
                throw new MojoExecutionException("dependency injection failed for " + 
                        fixtureClassName, exc);
            }
        }
        return fixture;
    }
}

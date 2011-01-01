package com.googlecode.jeeunit.mojo.fit.spring;

import org.springframework.test.context.ContextConfiguration;

import com.googlecode.jeeunit.mojo.fit.FitRunnerMojo;

import fit.FixtureLoader;

/**
 * Mojo to run Fit tests which supports Spring dependency injection for fixtures.
 * 
 * The fixture classes to be handled by spring have be annotated with 
 * {@link ContextConfiguration}, specifying the location of the Spring XML configuration.
 * 
 * NOTE: By default, properties from parent Mojos are not visible to the Javadoc annotation
 * processor on derived Mojos. The maven-inherit-plugin relieves this restriction by providing
 * the {@code extendsPlugin} annotation, instructing the plugin builder to include the 
 * inherited properties.
 * 
 * @author Harald Wellmann
 * 
 * @extendsPlugin fit
 * @goal run
 * @phase integration-test
 * @requiresDependencyResolution test
 */
public class SpringFitRunnerMojo extends FitRunnerMojo {

    /**
     * Returns a fixture loader which performs dependency injection on the loaded fixture
     * instances.
     * 
     * @return fixture loader
     */
    @Override
    protected FixtureLoader getFixtureLoader() {
        return new SpringFixtureLoader(getTestClassLoader());
    }

}

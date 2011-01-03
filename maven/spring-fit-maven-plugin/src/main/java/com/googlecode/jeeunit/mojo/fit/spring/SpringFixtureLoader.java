package com.googlecode.jeeunit.mojo.fit.spring;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;

import fit.Fixture;
import fit.FixtureLoader;

public class SpringFixtureLoader extends FixtureLoader {

    @Override
    public Fixture createFixture(Class<?> klass) throws InstantiationException,
            IllegalAccessException {
        Fixture fixture = (Fixture) klass.newInstance();

        ContextConfiguration cc = klass.getAnnotation(ContextConfiguration.class);
        if (cc != null) {
            TestContextManager contextManager = new TestContextManager(klass);
            try {
                contextManager.prepareTestInstance(fixture);
            }
            catch (Exception exc) {
                throw new InstantiationException("dependency injection failed for " + klass.getName());
            }
        }
        return fixture;
    }    
}

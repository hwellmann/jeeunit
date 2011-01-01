package com.googlecode.jeeunit.mojo.fit;

import fit.FixtureLoader;
import fit.exception.NoSuchFixtureException;

/**
 * A {@link FixtureLoader} which loads fixture classes using a given class loader.
 * 
 * @author hwellmann
 *
 */
public class ClassLoaderFixtureLoader extends FixtureLoader {

    /** Class loader for loading fixtures. */
    protected ClassLoader fixtureClassLoader;

    /**
     * Constructor
     * @param fixtureClassLoader  class loader for loading fixtures
     */
    public ClassLoaderFixtureLoader(ClassLoader fixtureClassLoader) {
        this.fixtureClassLoader = fixtureClassLoader;
    }

    /**
     * Loads a fixture class with the given fully qualified class name via the
     * class loader specified in the constructor.
     */
    @Override
    public Class<?> loadFixtureClass(String fixtureName) {
        try {
            return fixtureClassLoader.loadClass(fixtureName);
        }
        catch (ClassNotFoundException e) {
            throw new NoSuchFixtureException(fixtureName);
        }
    }
}

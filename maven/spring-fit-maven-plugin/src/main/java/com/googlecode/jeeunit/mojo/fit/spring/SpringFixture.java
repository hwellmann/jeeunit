package com.googlecode.jeeunit.mojo.fit.spring;

import org.apache.maven.plugin.MojoExecutionException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;

import fit.Fixture;

public class SpringFixture extends Fixture {

	@Override
	public Fixture getFixtureInstanceOf(String classname)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		Class<?> clazz = Thread.currentThread().getContextClassLoader()
				.loadClass(classname);
		Fixture fixture = (Fixture) clazz.newInstance();

		ContextConfiguration cc = clazz
				.getAnnotation(ContextConfiguration.class);
		if (cc != null) {
			TestContextManager contextManager = new TestContextManager(clazz);
			try {
				contextManager.prepareTestInstance(fixture);
			} catch (Exception exc) {
				throw new InstantiationException(
						"dependency injection failed for " + classname);
			}
		}
		return fixture;
	}

}

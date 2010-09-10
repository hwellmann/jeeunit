/*
 * Copyright 2010 Harald Wellmann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.googlecode.jeeunit;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

/**
 * Runs all test classes specified by a {@code SuiteClasses} annotation in an embedded server.
 * The list of suite classes can be filtered by a regular expression specified in the system property
 * {@code jeeunit.testClassPattern}. If this property is set, only the classes with fully
 * qualified class name matching the pattern will be executed.
 *
 */
public class InContainerSuite extends Suite {

    public static final String KEY_TEST_CLASS_PATTERN = "jeeunit.testClassPattern";

    public InContainerSuite(Class<?> klass) throws InitializationError {
        super(new InContainerRunnerBuilder(), klass, getSuiteClasses(klass));
    }

    private static Class<?>[] getSuiteClasses(Class<?> klass) throws InitializationError {
        SuiteClasses annotation = klass.getAnnotation(SuiteClasses.class);
        if (annotation == null) {
            String msg = String.format("class '%s' must have a SuiteClasses annotation", klass
                    .getName());
            throw new InitializationError(msg);
        }

        String pattern = System.getProperty(KEY_TEST_CLASS_PATTERN);
        if (pattern == null)
            return annotation.value();

        List<Class<?>> classes = new ArrayList<Class<?>>();
        for (Class<?> testClass : annotation.value()) {
            if (testClass.getName().matches(pattern)) {
                classes.add(testClass);
            }
        }
        Class<?>[] result = new Class<?>[classes.size()];
        return classes.toArray(result);
    }

    static class InContainerRunnerBuilder extends RunnerBuilder {

        @Override
        public Runner runnerForClass(Class<?> testClass) throws Throwable {
            return new CdiJUnitRunner(testClass);
        }
    }
}

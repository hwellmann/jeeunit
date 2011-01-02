/*
 * Copyright 2011 Harald Wellmann
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
package com.googlecode.jeeunit.fit.junit.spring;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;

import fit.Fixture;
import fit.FixtureLoader;

/**
 * A fixture loader which performs Spring dependency injection on all fixtures annotated
 * with {@link ContextConfiguration}, using a Spring Test Context.
 * 
 * @author Harald Wellmann
 *
 */
public class SpringFixtureLoader extends FixtureLoader {

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
            contextManager.prepareTestInstance(fixture);
        }
        return fixture;
    }
}

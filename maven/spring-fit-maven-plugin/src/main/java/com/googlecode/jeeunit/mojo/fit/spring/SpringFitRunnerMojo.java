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
package com.googlecode.jeeunit.mojo.fit.spring;

import com.googlecode.jeeunit.mojo.fit.FitRunnerMojo;

import fit.Fixture;

/**
 * Mojo to run FIT tests which supports Spring dependency injection for fixtures.
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

    @Override
    protected Fixture createFixture() {
    	return new SpringFixture();
    }

}

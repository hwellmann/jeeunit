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

package com.googlecode.jeeunit.example.test.driver;

import java.io.File;
import java.io.IOException;

import org.glassfish.api.embedded.LifecycleException;
import org.junit.Test;

import com.googlecode.jeeunit.glassfish.GlassfishTestDriver;


public class LibraryGlassfishTest extends GlassfishTestDriver {


    public LibraryGlassfishTest() {
        File domainConfig = new File("src/test/resources/domain.xml");
        setConfiguration(domainConfig);
        
        setApplicationName("jeeunit.example");
        setContextRoot("jeeunit");

        File war = new File("target/jeeunit-example-test.war");
        setWarFile(war);
    }
    
    @Test
    public void runTests() throws IOException, LifecycleException, InterruptedException {
        
        runServer();
    }
}

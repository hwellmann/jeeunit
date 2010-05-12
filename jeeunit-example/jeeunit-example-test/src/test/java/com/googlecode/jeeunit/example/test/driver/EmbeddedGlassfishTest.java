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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.embedded.ContainerBuilder;
import org.glassfish.api.embedded.EmbeddedDeployer;
import org.glassfish.api.embedded.EmbeddedFileSystem;
import org.glassfish.api.embedded.LifecycleException;
import org.glassfish.api.embedded.Server;
import org.junit.Test;

import com.sun.jersey.api.client.Client;


public class EmbeddedGlassfishTest {


    @Test
    public void runServer() throws IOException, LifecycleException, InterruptedException {
        Server.Builder builder = new Server.Builder("IntegrationTest");
        builder.verbose(false);
        builder.logger(false);

        // Define your JDBC resources and JNDI names in this config file
        File domainConfig = new File("src/test/resources/domain.xml");

        // Build a file system for the embedded server
        EmbeddedFileSystem.Builder efsb = new EmbeddedFileSystem.Builder();
        efsb.configurationFile(domainConfig);
        EmbeddedFileSystem efs = efsb.build();
        builder.embeddedFileSystem(efs);

        // Build the server, including all containers (web, jpa, ejb, ...)
        Server server = builder.build();
        server.addContainer(ContainerBuilder.Type.all);
        server.start();

        // Deploy your test app. Make sure to use the correct path
        File war = new File("target/jeeunit-example-test.war");
        EmbeddedDeployer deployer = server.getDeployer();
        DeployCommandParameters params = new DeployCommandParameters();
        params.contextroot = "itest";
        deployer.deploy(war, params);

        // Do a GET request on the URL of your test servlet. Make sure
        // the URL matches your server, context root, and the URL pattern
        // used by your servlet
        Client client = Client.create();
        String result = client.resource("http://localhost:8080/itest/test").get(String.class);
        
        // The servlet simply returns plain text.
        assertTrue(result.contains("All tests passed"));
        
        // Stop the server so that the test driver can terminate
        deployer.undeployAll();
        server.stop();
    }
}

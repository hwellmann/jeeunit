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

		File domainConfig = new File("src/test/resources/domain.xml");

		EmbeddedFileSystem.Builder efsb = new EmbeddedFileSystem.Builder();
		efsb.configurationFile(domainConfig);
		EmbeddedFileSystem efs = efsb.build();
		builder.embeddedFileSystem(efs);

		Server server = builder.build();
		server.addContainer(ContainerBuilder.Type.all);
		server.start();

		File war = new File("target/jeeunit-example-test-0.0.1.SNAPSHOT.war");
		EmbeddedDeployer deployer = server.getDeployer();
		DeployCommandParameters params = new DeployCommandParameters();
		params.contextroot = "itest";
		deployer.deploy(war, params);

		Client client = Client.create();
		String result = client.resource("http://localhost:8080/itest/test").get(String.class);
		assertTrue(result.contains("All tests passed"));
		
		deployer.undeployAll();
		server.stop();

	}
}

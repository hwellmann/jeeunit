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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.enterprise.inject.spi.BeanManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.archive.ScatteredArchive;
import org.glassfish.embeddable.archive.ScatteredArchive.Type;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.googlecode.jeeunit.BeanManagerLookup;
import com.sun.jersey.api.client.Client;



public class ScatteredArchiveTest {


    private String applicationName;
    private String contextRoot;
    private File warFile;
    private File configuration;
    
    
    
    
    
    public ScatteredArchiveTest() {
        File domainConfig = new File("src/test/resources/domain.xml");
        setConfiguration(domainConfig);
        
        setApplicationName("jeeunit.example");
        setContextRoot("jeeunit");

        File war = new File("target/jeeunit-example-test.war");
        setWarFile(war);
    }
    
    @Test
    public void runTests() throws IOException, InterruptedException, GlassFishException {
        
        runServer();
    }
    
    protected String getApplicationName() {
        return applicationName;
    }

    /**
     * Sets the Java EE application name.
     * @param applicationName
     */
    protected void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    protected String getContextRoot() {
        return contextRoot;
    }

    /**
     * Sets the context root for the deployed test application.
     * @param contextRoot
     */
    protected void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }

    protected File getWarFile() {
        return warFile;
    }

    /**
     * Sets the WAR file with the test application to be deployed to the embedded server.
     * @param warFile
     */
    protected void setWarFile(File warFile) {
        this.warFile = warFile;
    }

    protected File getConfiguration() {
        return configuration;
    }

    /**
     * Sets the configuration file for the embedded server.
     * @param configuration
     */
    protected void setConfiguration(File configuration) {
        this.configuration = configuration;
    }

    /**
     * Reads the first port number from the domain.xml configuration.
     * @param domainConfig
     * @return
     */
    private String getPortNumber(File domainConfig) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(domainConfig);
            XPathFactory xpf = XPathFactory.newInstance();
            XPath xPath = xpf.newXPath();
            String port = xPath.evaluate("/domain/configs/config/network-config/network-listeners/network-listener/@port", doc);
            return port;
        }
        catch (ParserConfigurationException exc) {
            throw new IllegalArgumentException(exc);
        }
        catch (SAXException exc) {
            throw new IllegalArgumentException(exc);
        }
        catch (IOException exc) {
            throw new IllegalArgumentException(exc);
        }
        catch (XPathExpressionException exc) {
            throw new IllegalArgumentException(exc);
        }
    }

    protected void runServer() throws IOException, InterruptedException, GlassFishException {
        /* 
         * Define a java util logging config file that will install the SLF4J bridge for
         * java.util.logging used by Glassfish. 
         */
//        System.setProperty("java.util.logging.config.file", "src/test/resources/logging.properties");
//        System.setProperty("java.security.policy", "src/test/resources/server.policy");
        String classpath = System.getProperty("java.class.path");
        System.out.println("classpath = " + classpath);
        

        // Define your JDBC resources and JNDI names in this config file
        File domainConfig = getConfiguration();
        assertTrue(domainConfig + " not found", domainConfig.exists());
        String port = getPortNumber(domainConfig);

        GlassFishProperties gfProps = new GlassFishProperties();
        gfProps.setConfigFileURI(domainConfig.toURI().toString());
        
        GlassFish glassFish = GlassFishRuntime.bootstrap().newGlassFish(gfProps);
        glassFish.start();
        
        
        ScatteredArchive sar= new ScatteredArchive("mywar", Type.WAR);        
        String[] pathElems = classpath.split(":");
        for (String pathElem : pathElems) {
            if (pathElem.contains("glassfish-embedded"))
                continue;
            
            if (pathElem.contains("org.eclipse.osgi"))
                continue;
            
            File file = new File(pathElem);
            sar.addClassPath(file);
        }
        sar.addMetadata(new File("src/main/webapp/WEB-INF", "beans.xml"));
        URI warUri = sar.toURI();

        Deployer deployer = glassFish.getDeployer();
        String appName = deployer.deploy(warUri, 
                "--name", getApplicationName(),
                "--contextroot", getContextRoot());
        assertEquals("error deploying WAR", getApplicationName(), appName) ;
        
        CommandResult commandResult = glassFish.getCommandRunner().run("list-applications");
        System.out.println(commandResult.getOutput());

        BeanManager mgr = BeanManagerLookup.getBeanManager();
        // Do a GET request on the URL of your test servlet. Make sure
        // the URL matches your server, context root, and the URL pattern
        // used by your servlet
        Client client = Client.create();
        String url = "http://localhost:" + port + "/" + getContextRoot() + "/testrunner";
        String result = client.resource(url).
            queryParam("class", "com.googlecode.jeeunit.example.test.AuthorTest").
            get(String.class);

        // The servlet simply returns plain text.
        assertTrue(result.contains("All tests passed"));

        // Stop the server so that the test driver can terminate
        deployer.undeploy(appName);
        glassFish.stop();
    }
    
}

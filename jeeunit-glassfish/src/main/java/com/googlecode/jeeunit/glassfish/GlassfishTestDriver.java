package com.googlecode.jeeunit.glassfish;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.embedded.ContainerBuilder;
import org.glassfish.api.embedded.EmbeddedDeployer;
import org.glassfish.api.embedded.EmbeddedFileSystem;
import org.glassfish.api.embedded.LifecycleException;
import org.glassfish.api.embedded.Server;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;

/**
 * Base class for test drivers running a test suite on Embedded Glassfish. For a concrete test
 * suite, create a derived class, use the setter methods to configure this driver and then
 * call runServer() in a test method to trigger test execution.
 * 
 * @author wellmannh
 *
 */
public class GlassfishTestDriver {
    
    private String applicationName;
    private String contextRoot;
    private File warFile;
    private File configuration;
    
    
    
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

    protected void runServer() throws IOException, LifecycleException, InterruptedException {
        /* 
         * Define a java util logging config file that will install the SLF4J bridge for
         * java.util.logging used by Glassfish. 
         */
        System.setProperty("java.util.logging.config.file", "src/test/resources/logging.properties");
        System.setProperty("java.security.policy", "src/test/resources/server.policy");
        System.out.println("classpath = " + System.getProperty("java.class.path"));
        
        Server.Builder builder = new Server.Builder("jeeunit");
        builder.verbose(false);
        builder.logger(false);

        // Define your JDBC resources and JNDI names in this config file
        File domainConfig = getConfiguration();
        assertTrue(domainConfig + " not found", domainConfig.exists());
        String port = getPortNumber(domainConfig);
        // Build a file system for the embedded server
        EmbeddedFileSystem.Builder efsb = new EmbeddedFileSystem.Builder();
        
        File tmpDir = new File(System.getProperty("java.io.tmpdir"), "gf" + new SecureRandom().nextInt());
        efsb.instanceRoot(tmpDir);
        efsb.configurationFile(domainConfig);
        EmbeddedFileSystem efs = efsb.build();
        builder.embeddedFileSystem(efs);

        // Build the server, including all containers (web, jpa, ejb, ...)
        Server server = builder.build();
        server.addContainer(ContainerBuilder.Type.all);
        server.start();
        
        // Deploy your test app. Make sure to use the correct path
        File war = getWarFile();
        assertTrue(war.exists());
        EmbeddedDeployer deployer = server.getDeployer();
        DeployCommandParameters params = new DeployCommandParameters();
        params.contextroot = getContextRoot();
        params.name = getApplicationName();
        String appName = deployer.deploy(war, params);
        assertEquals("error deploying WAR", getApplicationName(), appName) ;

        // Do a GET request on the URL of your test servlet. Make sure
        // the URL matches your server, context root, and the URL pattern
        // used by your servlet
        Client client = Client.create();
        String url = "http://localhost:" + port + "/" + getContextRoot() + "/test";
        String result = client.resource(url).get(String.class);

        // The servlet simply returns plain text.
        assertTrue(result.contains("All tests passed"));

        // Stop the server so that the test driver can terminate
        deployer.undeployAll();
        server.stop();
    }
}

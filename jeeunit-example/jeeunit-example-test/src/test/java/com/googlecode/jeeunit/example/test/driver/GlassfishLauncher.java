package com.googlecode.jeeunit.example.test.driver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.archive.ScatteredArchive;
import org.glassfish.embeddable.archive.ScatteredArchive.Type;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class GlassfishLauncher {
    
    private static GlassfishLauncher instance;

    private GlassFish glassFish;

    
    private String applicationName;
    private String contextRoot;
    private File configuration;

    private WebResource webResource;
    
        
    private GlassfishLauncher() {

        File domainConfig = new File("src/test/resources/domain.xml");
        setConfiguration(domainConfig);
        
        setApplicationName("jeeunit");
        setContextRoot("jeeunit");

        try {
            launch();
            addShutdownHook();
        }
        catch (GlassFishException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static synchronized GlassfishLauncher getInstance() {
        if (instance == null) {
            instance = new GlassfishLauncher();
        }
        return instance;
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    if (glassFish != null) {
                        glassFish.getDeployer().undeploy(getApplicationName());
                        glassFish.stop();
                    }
                }
                catch (GlassFishException exc) {
                    throw new RuntimeException(exc);
                }
            }
        });
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

    public WebResource getWebResource() {
        return webResource;
    }
    
    
    private void launch() throws GlassFishException, IOException {
        String classpath = System.getProperty("java.class.path");       

        // Define your JDBC resources and JNDI names in this config file
        File domainConfig = getConfiguration();
        assertTrue(domainConfig + " not found", domainConfig.exists());

        GlassFishProperties gfProps = new GlassFishProperties();
        gfProps.setConfigFileURI(domainConfig.toURI().toString());
        
        glassFish = GlassFishRuntime.bootstrap().newGlassFish(gfProps);
        glassFish.start();
                
        URI warUri = buildWar(classpath);
        deployWar(warUri);        
        createWebClient();
    }

    private URI buildWar(String classpath) throws IOException {
        ScatteredArchive sar= new ScatteredArchive("mywar", Type.WAR);        
        String[] pathElems = classpath.split(File.pathSeparator);
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
        return warUri;
    }

    private void deployWar(URI warUri) throws GlassFishException {
        Deployer deployer = glassFish.getDeployer();
        String appName = deployer.deploy(warUri, 
                "--name", getApplicationName(),
                "--contextroot", getContextRoot());
        assertEquals("error deploying WAR", getApplicationName(), appName) ;
    }

    private void createWebClient() {
        String port = getPortNumber(configuration);
        Client client = Client.create();
        String url = "http://localhost:" + port + "/" + getContextRoot() + "/testrunner";
        webResource = client.resource(url);
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

    
}

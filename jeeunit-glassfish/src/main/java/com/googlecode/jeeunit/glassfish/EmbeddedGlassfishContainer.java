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
package com.googlecode.jeeunit.glassfish;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

import com.googlecode.jeeunit.impl.ClasspathFilter;
import com.googlecode.jeeunit.spi.ContainerLauncher;

/**
 * Singleton implementing the {@link ContainerLauncher} functionality for Embedded Glassfish.
 * 
 * @author hwellmann
 *
 */
public class EmbeddedGlassfishContainer {
    
    private static EmbeddedGlassfishContainer instance;

    private GlassFish glassFish;
    private FileFilter classpathFilter;
    
    private String applicationName;
    private String contextRoot;
    private File configuration;
    private boolean isDeployed;
    
    private List<File> metadataFiles = new ArrayList<File>();

    private File tmpBeansXml;

    private File tmpDir;

    /**
     * Default filter suppressing Glassfish and Eclipse components from the classpath when 
     * building the ad hoc WAR.
     * 
     * @author hwellmann
     *
     */
    private static String[] excludes = {"glassfish-embedded", "org.eclipse.osgi"};
    
    private EmbeddedGlassfishContainer() {

        File domainConfig = new File("src/test/resources/domain.xml");
        setConfiguration(domainConfig);
        
        setApplicationName("jeeunit");
        setContextRoot("jeeunit");
        setClasspathFilter(new ClasspathFilter(excludes));
        
        createDefaultMetadata();
    }
    
    private void createDefaultMetadata() {
        
        File webInf = new File("src/main/webapp/WEB-INF");
        metadataFiles.add(new File(webInf, "web.xml"));
        File beansXml = new File(webInf, "beans.xml");
        if (!beansXml.exists()) {
            beansXml = new File(tmpDir, "beans.xml");
            try {
                createTempDir();
                beansXml.createNewFile();
                tmpBeansXml = beansXml;
            }
            catch (IOException exc) {
                throw new RuntimeException("cannot create " + beansXml);
            }
        }
        metadataFiles.add(beansXml);
    }

    private void createTempDir()
    {
        tmpDir = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        tmpDir.mkdir();
    }

    public static synchronized EmbeddedGlassfishContainer getInstance() {
        if (instance == null) {
            instance = new EmbeddedGlassfishContainer();
        }
        return instance;
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();                
                if (tmpBeansXml != null) {
                    tmpBeansXml.delete();
                    tmpDir.delete();
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

    public void setClasspathFilter(FileFilter classpathFilter) {
        this.classpathFilter = classpathFilter;
    }
    
    
    public synchronized void launch() {
        if (glassFish != null) {
            return;
        }

        /*
         * Running under "Run as JUnit test" from Eclipse in a separate process, we do not get
         * notified when Eclipse is finished running the test suite. The shutdown hook is just
         * to be on the safe side.
         */
        addShutdownHook();

        File domainConfig = getConfiguration();
        if (!domainConfig.exists()) {
            throw new IllegalArgumentException(domainConfig + " not found");
        }

        GlassFishProperties gfProps = new GlassFishProperties();
        if (domainConfig.exists()) {
            gfProps.setConfigFileURI(domainConfig.toURI().toString());
        }

        try {
            glassFish = GlassFishRuntime.bootstrap().newGlassFish(gfProps);
            glassFish.start();
        }
        catch (GlassFishException exc) {
            throw new RuntimeException(exc);
        }
    }

    private URI buildWar() throws IOException {
        String classpath = System.getProperty("java.class.path");       
        ScatteredArchive sar= new ScatteredArchive("jeeunit-autodeploy", Type.WAR);        
        String[] pathElems = classpath.split(File.pathSeparator);

        for (String pathElem : pathElems) {
            File file = new File(pathElem);
            if (file.exists() && classpathFilter.accept(file)) {
                sar.addClassPath(file);
            }
        }
        for (File metadata : metadataFiles) {
            if (metadata.exists()) {
                sar.addMetadata(metadata);
            }
        }
        URI warUri = sar.toURI();
        return warUri;
    }

    private void deployWar(URI warUri) throws GlassFishException {
        Deployer deployer = glassFish.getDeployer();
        String appName = deployer.deploy(warUri, 
                "--name", getApplicationName(),
                "--contextroot", getContextRoot());
        if (! getApplicationName().equals(appName)) {
            throw new RuntimeException("error deploying WAR");
        }
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

    public void shutdown() {
        try {
            if (glassFish != null) {
                glassFish.getDeployer().undeploy(getApplicationName());
                glassFish.stop();
                glassFish = null;
            }
        }
        catch (GlassFishException exc) {
            throw new RuntimeException(exc);
        }
    }

    public URI autodeploy() {
        try {
            if (!isDeployed) {
                URI warUri = buildWar();
                deployWar(warUri);
                isDeployed = true;
            }
            return getContextRootUri();
        }
        catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }
    
    public URI getContextRootUri() {
        String port = getPortNumber(configuration);
        try {
            return new URI(String.format("http://localhost:%s/%s/", port, getContextRoot()));
        }
        catch (URISyntaxException exc) {
            throw new RuntimeException(exc);
        }
    }

    public void addMetadata(File file) {
        metadataFiles.add(file);
    }
}

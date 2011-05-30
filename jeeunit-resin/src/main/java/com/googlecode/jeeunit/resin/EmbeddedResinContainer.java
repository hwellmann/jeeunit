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
package com.googlecode.jeeunit.resin;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.caucho.resin.HttpEmbed;
import com.caucho.resin.ResinEmbed;
import com.caucho.resin.WebAppEmbed;
import com.googlecode.jeeunit.spi.ContainerLauncher;

/**
 * Singleton implementing the {@link ContainerLauncher} functionality for Embedded Glassfish.
 * 
 * @author hwellmann
 *
 */
public class EmbeddedResinContainer {
    
    private static EmbeddedResinContainer instance;

    private ResinEmbed resin;
    private FileFilter classpathFilter;
    
    private String applicationName;
    private String contextRoot;
    private File configuration;
    private boolean isDeployed;
    
    private List<File> metadataFiles = new ArrayList<File>();

    private File tmpBeansXml;

    /**
     * Default filter suppressing Glassfish and Eclipse components from the classpath when 
     * building the ad hoc WAR.
     * 
     * @author hwellmann
     *
     */
    private static class DefaultClasspathFilter implements FileFilter {

        @Override
        public boolean accept(File pathname) {
            String path = pathname.getPath();
            if (path.contains("shrinkwrap-"))
                return false;

            if (path.contains("resin-"))
                return false;

            // this is to filter all bundles deployed to the Eclipse runtime, e.g. the JUnit
            // integration
            return !path.contains("org.eclipse.osgi");            
        }        
    }
        
    private EmbeddedResinContainer() {

        File domainConfig = new File("src/test/resources/domain.xml");
        setConfiguration(domainConfig);
        
        setApplicationName("jeeunit");
        setContextRoot("jeeunit");
        setClasspathFilter(new DefaultClasspathFilter());
        
        createDefaultMetadata();
    }
    
    private void createDefaultMetadata() {
        File webInf = new File("src/main/webapp/WEB-INF");
        metadataFiles.add(new File(webInf, "web.xml"));
        File beansXml = new File(webInf, "beans.xml");
        if (!beansXml.exists()) {
            beansXml = new File(System.getProperty("java.io.tmpdir"), "beans.xml");
            try {
                beansXml.createNewFile();
                tmpBeansXml = beansXml;
            }
            catch (IOException exc) {
                throw new RuntimeException("cannot create " + beansXml);
            }
        }
        metadataFiles.add(beansXml);
    }

    public static synchronized EmbeddedResinContainer getInstance() {
        if (instance == null) {
            instance = new EmbeddedResinContainer();
        }
        return instance;
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                tmpBeansXml.delete();
                shutdown();                
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
        if (resin != null) {
            return;
        }
        
        resin = new ResinEmbed();
        HttpEmbed httpPort = new HttpEmbed(8080);
        resin.addPort(httpPort);
        resin.setRootDirectory("/tmp/resin");

        /*
         * Running under "Run as JUnit test" from Eclipse in a separate process, we do not get
         * notified when Eclipse is finished running the test suite. The shutdown hook is just
         * to be on the safe side.
         */
        addShutdownHook();

//        File domainConfig = getConfiguration();
//        if (!domainConfig.exists()) {
//            throw new IllegalArgumentException(domainConfig + " not found");
//        }

    }

    private File buildWar() throws IOException {
        WebArchive war = ShrinkWrap.create(WebArchive.class);
        
        String classpath = System.getProperty("java.class.path");       
        String[] pathElems = classpath.split(File.pathSeparator);

        for (String pathElem : pathElems) {
            File file = new File(pathElem);
            if (file.exists() && classpathFilter.accept(file)) {
                if (file.isDirectory()) {
                    war.as(ExplodedImporter.class).importDirectory(file);
                }
                else {
                    JavaArchive jar = ShrinkWrap.createFromZipFile(JavaArchive.class, file);
                    war.merge(jar);
                }
            }
        }
        for (File metadata : metadataFiles) {
            if (metadata.exists()) {
                war.addAsWebInfResource(metadata);
            }
        }
        File tmpWar = new File("/tmp/jeeunit.war");
        war.as(ZipExporter.class).exportTo(tmpWar, true);
        return tmpWar;
    }


    public void shutdown() {
        resin.stop();
    }

    public URI autodeploy() {
        try {
            if (!isDeployed) {
                File war = buildWar();
                WebAppEmbed webApp = new WebAppEmbed("/jeeunit");
                webApp.setArchivePath(war.getAbsolutePath());
                resin.addWebApp(webApp);
                resin.start();
                isDeployed = true;
            }
            return getContextRootUri();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    public URI getContextRootUri() {
        String port = "8080";
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

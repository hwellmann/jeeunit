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
package com.googlecode.jeeunit.jboss7;

import static com.googlecode.jeeunit.impl.Constants.*;
import static com.googlecode.jeeunit.impl.Constants.JEEUNIT_CONTEXT_ROOT;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.glassfish.embeddable.archive.ScatteredArchive;
import org.glassfish.embeddable.archive.ScatteredArchive.Type;
import org.jboss.as.embedded.EmbeddedServerFactory;
import org.jboss.as.embedded.ServerStartException;
import org.jboss.as.embedded.StandaloneServer;

import com.googlecode.jeeunit.impl.ClasspathFilter;
import com.googlecode.jeeunit.impl.Configuration;
import com.googlecode.jeeunit.impl.ConfigurationLoader;
import com.googlecode.jeeunit.impl.ZipExploder;
import com.googlecode.jeeunit.spi.ContainerLauncher;

/**
 * Singleton implementing the {@link ContainerLauncher} functionality for Embedded Tomcat 7. The
 * configuration file for the deployed web app is expected in
 * {@code src/test/resources/META-INF/context.xml}.
 * <p>
 * {@link Tomcat} does not let us start the server first and then deploy apps, so we actually
 * start the container and deploy the application in {@code autodeploy()}.
 * <p>
 * For configuring the Tomcat 7 container provide a properties file {@code jeeunit.properties}
 * in the classpath root. You can set the following properties:
 * <ul>
 * <li>{@code jeeunit.tomcat7.http.port} port for the embedded HTTP server (default: 8080)</li>
 * <li>{@code jeeunit.tomcat7.weld.listener} add Weld listener to web.xml? (default: false)</li>
 * </ul>
 *  
 * @author hwellmann
 * 
 */
public class EmbeddedJBoss7Container implements ContainerLauncher {

    
    private static EmbeddedJBoss7Container instance;
    
    private FileFilter classpathFilter;

    private String applicationName;
    private String contextRoot;
    private File configuration;
    private boolean isDeployed;

    private List<File> metadataFiles = new ArrayList<File>();

    private File tempDir;

    private File webappsDir;
    
    private File jeeunitWar;

    /**
     * Default filter suppressing Tomcat and Eclipse components from the classpath when building the
     * ad hoc WAR.
     * 
     * @author hwellmann
     * 
     */
    private static String[] excludes = { 
        "jboss", 
        "xnio", 
        "jandex", 
        ".cp", 
        "servlet-",
        "geronimo-servlet_",
        "shrinkwrap-", 
        };

    private Configuration config;

    private StandaloneServer server;

    private EmbeddedJBoss7Container() {
        tempDir = createTempDir();

        setApplicationName(JEEUNIT_APPLICATION_NAME);
        setContextRoot(JEEUNIT_CONTEXT_ROOT);
        setClasspathFilter(new ClasspathFilter(excludes));

        createDefaultMetadata();
    }

    private void createDefaultMetadata() {
        File webInf = new File("src/main/webapp/WEB-INF");
        metadataFiles.add(new File(webInf, "web.xml"));
        File beansXml = new File(webInf, "beans.xml");
        if (!beansXml.exists()) {
            beansXml = new File(tempDir, "beans.xml");
            try {
                beansXml.createNewFile();
            }
            catch (IOException exc) {
                throw new RuntimeException("cannot create " + beansXml);
            }
        }
        metadataFiles.add(beansXml);
    }

    public static synchronized EmbeddedJBoss7Container getInstance() {
        if (instance == null) {
            instance = new EmbeddedJBoss7Container();
        }
        return instance;
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });
    }

    protected String getApplicationName() {
        return applicationName;
    }

    /**
     * Sets the Java EE application name.
     * 
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
     * 
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
     * 
     * @param configuration
     */
    protected void setConfiguration(File configuration) {
        this.configuration = configuration;
    }

    public void setClasspathFilter(FileFilter classpathFilter) {
        this.classpathFilter = classpathFilter;
    }

    public synchronized void launch() {

        config = new ConfigurationLoader().load();
        prepareDirectories();

        System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
        String jbossHome = config.getServerHome();
        if (jbossHome == null)
            throw new IllegalStateException("Cannot find configuration property " + KEY_SERVER_HOME);

        File jbossHomeDir = new File(jbossHome);

        server = EmbeddedServerFactory.create(jbossHomeDir, System.getProperties(), System.getenv());
        
        /*
         * Running under "Run as JUnit test" from Eclipse in a separate process, we do not get
         * notified when Eclipse is finished running the test suite. The shutdown hook is just to be
         * on the safe side.
         */
        addShutdownHook();
        
        try {
            server.start();
        }
        catch (ServerStartException exc) {
            throw new RuntimeException(exc);
        }

    }

    private void prepareDirectories() {
        webappsDir = new File(tempDir, "webapps");
        webappsDir.mkdirs();
    }

    private URI buildWar() throws IOException {
        ScatteredArchive sar;        
        File webResourceDir = getWebResourceDir();
        if (webResourceDir.exists() && webResourceDir.isDirectory()) {
            sar = new ScatteredArchive("jeeunit-autodeploy", Type.WAR, webResourceDir);
        }
        else {
            sar = new ScatteredArchive("jeeunit-autodeploy", Type.WAR);            
        }
        String classpath = System.getProperty("java.class.path");
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
        File war = new File(warUri);
        jeeunitWar = new File(webappsDir, "jeeunit.war");
        FileUtils.copyFile(war, jeeunitWar);
        return warUri;        
    }
    
    private File getWebResourceDir() throws IOException {
        File webResourceDir;
        String warBase = config.getWarBase();
        if (warBase == null) {
            webResourceDir = new File("src/main/webapp");
        }
        else {
            ZipExploder exploder = new ZipExploder();
            webResourceDir = new File(tempDir, "exploded");
            webResourceDir.mkdir();
            exploder.processFile(new File(warBase).getAbsolutePath(), webResourceDir.getAbsolutePath());            
        }
        return webResourceDir;
    }
    
    public void shutdown() {
        server.stop();
    }

    public URI autodeploy() {
        if (!isDeployed) {
            try {
                buildWar();
                // TODO This method is deprecated, but what's the alternative?
                server.deploy(jeeunitWar);
            }
            catch (IOException exc) {
                throw new RuntimeException(exc);
            }
            catch (ExecutionException exc) {
                throw new RuntimeException(exc);
            }
            catch (InterruptedException exc) {
                throw new RuntimeException(exc);
            }
        }
        return getContextRootUri();
    }

    public URI getContextRootUri() {
        try {
            return new URI(String.format("http://localhost:%d/%s/", config.getHttpPort(), getContextRoot()));
        }
        catch (URISyntaxException exc) {
            throw new RuntimeException(exc);
        }
    }

    public void addMetadata(File file) {
        metadataFiles.add(file);
    }

    private File createTempDir() {
        File tmpRoot = FileUtils.getTempDirectory();
        File tmpDir = new File(tmpRoot, UUID.randomUUID().toString());
        tmpDir.mkdir();
        return tmpDir;
    }

}

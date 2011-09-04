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
package com.googlecode.jeeunit.tomcat6;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ContextResourceEnvRef;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Embedded;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.glassfish.embeddable.archive.ScatteredArchive;
import org.glassfish.embeddable.archive.ScatteredArchive.Type;

import com.googlecode.jeeunit.spi.ContainerLauncher;

/**
 * Singleton implementing the {@link ContainerLauncher} functionality for Embedded Tomcat 6. The
 * configuration file for the deployed web app is expected in
 * {@code src/test/resources/resin-web.xml}.
 * <p>
 * {@link Embedded} does not let us start the server first and then deploy apps, so we actually
 * start the container and deploy the application in {@code autodeploy()}.
 * <p>
 * For some reason, setting the configuration in the ResinEmbed constructor does not seem to work
 * (or maybe something was wrong with my config files). The only way that currently works for me is
 * embedding a resin-web.xml config file into the WAR and setting the HTTP port for the server
 * programmatically, which is a bit awkward and does not let us define the complete configuration in
 * a single file.
 * <p>
 * For configuring the Tomcat 6 container provide a properties file {@code jeeunit.properties}
 * in the classpath root. You can set the following properties:
 * <ul>
 * <li>{@code jeeunit.tomcat6.http.port} port for the embedded HTTP server (default: 8080)</li>
 * <li>{@code jeeunit.tomcat6.weld.listener} add Weld listener to web.xml? (default: false)</li>
 * </ul>
 *  
 * @author hwellmann
 * 
 */
public class EmbeddedTomcat6Container {

    public static final String[] CONTEXT_XML = { 
        "src/test/resources/META-INF/context.xml", 
        "src/main/webapp/META-INF/context.xml", 
    };
    public static final String BEAN_MANAGER_TYPE = "javax.enterprise.inject.spi.BeanManager";
    public static final String BEAN_MANAGER_NAME = "BeanManager";
    public static final String WELD_MANAGER_FACTORY = "org.jboss.weld.resources.ManagerObjectFactory";
    public static final String WELD_SERVLET_LISTENER = "org.jboss.weld.environment.servlet.Listener";
    public static final String SPRING_SERVLET_CLASS = "com.googlecode.jeeunit.example.spring.test.TestRunnerServlet";
    public static final String CDI_SERVLET_CLASS = "com.googlecode.jeeunit.TestRunnerServlet";
    public static final String JEEUNIT_APPLICATION_NAME = "jeeunit";
    public static final String JEEUNIT_CONTEXT_ROOT = "/" + JEEUNIT_APPLICATION_NAME;
    public static final String TESTRUNNER_NAME = "testrunner";
    public static final String TESTRUNNER_URL = "/testrunner";
    public static final String HTTP_PORT_DEFAULT = "8080";
    
    private static EmbeddedTomcat6Container instance;
    
    private Embedded tomcat;
    private FileFilter classpathFilter;

    private String applicationName;
    private String contextRoot;
    private File configuration;
    private boolean isDeployed;

    private List<File> metadataFiles = new ArrayList<File>();

    private File tempDir;
    private int httpPort;

    private File catalinaHome;

    private File webappDir;

    private File webappsDir;
    
    private File tmpDefaultWebXml;
    private boolean includeWeld;

    /**
     * Default filter suppressing Tomcat and Eclipse components from the classpath when building the
     * ad hoc WAR.
     * 
     * @author hwellmann
     * 
     */
    private static class DefaultClasspathFilter implements FileFilter {

        private static String[] excludes = { 
            "catalina-", 
            "coyote-", 
            "ecj-", 
            "el-api-", 
            "jasper-", 
            "jsp-api-", 
            "juli-", 
            ".cp", 
            "servlet-",
            "shrinkwrap-", 
            };

        @Override
        public boolean accept(File pathname) {
            String path = pathname.getPath();
            String baseName = path.substring(path.lastIndexOf('/') + 1);
            for (String exclude : excludes) {                
                if (baseName.startsWith(exclude))
                    return false;
            }
            return true;
        }
    }

    private EmbeddedTomcat6Container() {
        tempDir = createTempDir();

        setApplicationName(JEEUNIT_APPLICATION_NAME);
        setContextRoot(JEEUNIT_CONTEXT_ROOT);
        setClasspathFilter(new DefaultClasspathFilter());

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

    public static synchronized EmbeddedTomcat6Container getInstance() {
        if (instance == null) {
            instance = new EmbeddedTomcat6Container();
        }
        return instance;
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    FileUtils.deleteDirectory(tempDir);
                }
                catch (IOException exc) {
                    // ignore
                }
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
        if (tomcat != null) {
            return;
        }

        readConfiguration();
        prepareDirectories();

        tomcat = new Embedded();
        tomcat.setCatalinaHome(catalinaHome.getAbsolutePath());

        
        /*
         * Running under "Run as JUnit test" from Eclipse in a separate process, we do not get
         * notified when Eclipse is finished running the test suite. The shutdown hook is just to be
         * on the safe side.
         */
        addShutdownHook();

    }

    private void prepareDirectories() {
        webappsDir = new File(tempDir, "webapps");
        webappsDir.mkdirs();
        webappDir = new File(webappsDir, contextRoot);
        catalinaHome = new File(tempDir, "catalina");
      
    }

    private int readConfiguration() {
        Properties props = new Properties();
        InputStream is = getClass().getResourceAsStream("/jeeunit.properties");
        if (is != null) {
            try {
                props.load(is);
                
                String httpPortString = props.getProperty("jeeunit.tomcat6.http.port", HTTP_PORT_DEFAULT);
                httpPort = Integer.parseInt(httpPortString);
                
                String weldListenerString = props.getProperty("jeeunit.tomcat6.weld.listener", "false");
                includeWeld = Boolean.parseBoolean(weldListenerString);
            }
            catch (IOException exc) {
                throw new RuntimeException(exc);
            }
            catch (NumberFormatException exc) {
                // ignore
            }
        }
        return httpPort;
    }

    private URI buildWar() throws IOException {
        ScatteredArchive sar;        
        File webResourceDir = new File("src/main/webapp");
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
        copyFile(war, new File(webappsDir, "jeeunit.war"));
        return warUri;        
    }
    
    private void copyFile(File source, File target) throws IOException {
        FileInputStream is = new FileInputStream(source);
        FileOutputStream os = new FileOutputStream(target);
        IOUtils.copy(is, os);
        is.close();
        os.close();
    }

    public void shutdown() {
        try {
            tomcat.stop();
        }
        catch (LifecycleException exc) {
            throw new RuntimeException(exc);
        }
    }

    public URI autodeploy() {
        if (!isDeployed) {
            try {
                buildWar();
            }
            catch (IOException exc) {
                throw new RuntimeException(exc);
            }

            WebappLoader loader = new WebappLoader();
            createDefaultWebXml();
                        
            StandardContext appContext = (StandardContext) tomcat.createContext(
                    contextRoot, webappDir.getAbsolutePath());
            appContext.setLoader(loader);
            appContext.setReloadable(true);
            appContext.setDefaultWebXml(tmpDefaultWebXml.getAbsolutePath());

            for (String fileName : CONTEXT_XML) {
                File contextXml = new File(fileName);
                if (contextXml.exists()) {
                    appContext.setConfigFile(fileName);
                    break;
                }
            }
            
            Wrapper servlet = appContext.createWrapper();
            String servletClass = includeWeld ? CDI_SERVLET_CLASS : SPRING_SERVLET_CLASS;
            servlet.setServletClass(servletClass);
            servlet.setName(TESTRUNNER_NAME);
            servlet.setLoadOnStartup(2);
            
            appContext.addChild(servlet);
            appContext.addServletMapping(TESTRUNNER_URL, TESTRUNNER_NAME);
            
            if (includeWeld) {
                addWeldBeanManager(appContext);
            }
            
            Host localHost = tomcat.createHost("localhost",
                    webappsDir.getAbsolutePath());

            localHost.addChild(appContext);

            
            // create engine
            Engine engine = tomcat.createEngine();
            engine.setName("Catalina");
            engine.addChild(localHost);
            engine.setDefaultHost(localHost.getName());
            tomcat.addEngine(engine);

            // create http connector
            Connector httpConnector = tomcat.createConnector((InetAddress) null,
                    httpPort, false);
            tomcat.addConnector(httpConnector);

            tomcat.setAwait(true);

            // start server
            try
            {
                tomcat.start();
                isDeployed = true;
            }
            catch (LifecycleException exc)
            {
                throw new RuntimeException(exc);
            }
          
        }
        return getContextRootUri();
    }

    private void addWeldBeanManager(StandardContext appContext)
    {
        ContextResource resource = new ContextResource();
        resource.setAuth("Container");
        resource.setName(BEAN_MANAGER_NAME);
        resource.setType(BEAN_MANAGER_TYPE);
        resource.setProperty("factory", WELD_MANAGER_FACTORY);

        appContext.getNamingResources().addResource(resource);

        
        ContextResourceEnvRef resourceRef = new ContextResourceEnvRef();
        resourceRef.setName(BEAN_MANAGER_NAME);
        resourceRef.setType(BEAN_MANAGER_TYPE);
        
        appContext.getNamingResources().addResourceEnvRef(resourceRef);

        appContext.addApplicationListener(WELD_SERVLET_LISTENER);
    }

    // Default web.xml, contains JSP servlet, mime types, welcome default etc.
    private void createDefaultWebXml()
    {
        try
        {
            InputStream is = getClass().getResourceAsStream("/default-web.xml");
            tmpDefaultWebXml = new File(tempDir, "default-web.xml");
            OutputStream os = new FileOutputStream(tmpDefaultWebXml);
            IOUtils.copy(is, os);
            os.close();
        }
        catch (IOException exc)
        {
            throw new RuntimeException();
        }
    }

    public URI getContextRootUri() {
        try {
            return new URI(String.format("http://localhost:%d/%s/", httpPort, getContextRoot()));
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

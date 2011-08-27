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
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.enterprise.inject.spi.BeanManager;

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
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import com.googlecode.jeeunit.TestRunnerServlet;
import com.googlecode.jeeunit.spi.ContainerLauncher;
import com.sun.jersey.api.client.filter.ContainerListener;

/**
 * Singleton implementing the {@link ContainerLauncher} functionality for Embedded Resin. The
 * configuration file for the deployed web app is expected in
 * {@code src/test/resources/resin-web.xml}.
 * <p>
 * {@link ResinEmbed} does not let us start the server first and then deploy apps, so we actually
 * start the container and deploy the application in {@code autodeploy()}.
 * <p>
 * For some reason, setting the configuration in the ResinEmbed constructor does not seem to work
 * (or maybe something was wrong with my config files). The only way that currently works for me is
 * embedding a resin-web.xml config file into the WAR and setting the HTTP port for the server
 * programmatically, which is a bit awkward and does not let us define the complete configuration in
 * a single file.
 * <p>
 * For overriding the default port 8080, provide a classpath resource jeeunit.properties, setting
 * a property {@code jeeunit.resin.http.port} to the desired value.
 *  
 * @author hwellmann
 * 
 */
public class EmbeddedTomcat6Container {

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
    
    private String tomcatHome = "/home/hwellmann/apps/apache-tomcat-6.0.32";

    /**
     * Default filter suppressing Resin and Eclipse components from the classpath when building the
     * ad hoc WAR.
     * 
     * @author hwellmann
     * 
     */
    private static class DefaultClasspathFilter implements FileFilter {

        private static String[] excludes = { "shrinkwrap-", "catalina-", "javaee-", "jsr250-",
                "coyote-", "jasper-", "el-api-", "jsp-api-", "ecj-", "juli-", "servlet-",
                "org.eclipse.osgi" };

        @Override
        public boolean accept(File pathname) {
            String path = pathname.getPath();
            for (String exclude : excludes) {
                if (path.contains(exclude))
                    return false;
            }
            return true;
        }
    }

    private EmbeddedTomcat6Container() {
        tempDir = createTempDir();

        setApplicationName("jeeunit");
        setContextRoot("/jeeunit");
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
        metadataFiles.add(new File("src/test/resources/resin-web.xml"));
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

        prepareDirectories();
        
        tomcat = new Embedded();
        tomcat.setCatalinaHome(catalinaHome.getAbsolutePath());
        httpPort = getHttpPort();

        
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

    private int getHttpPort() {
        int httpPort = 8088;
        Properties props = new Properties();
        InputStream is = getClass().getResourceAsStream("/jeeunit.properties");
        if (is != null) {
            try {
                props.load(is);
                String httpPortString = props.getProperty("jeeunit.tomcat6.http.port");
                if (httpPortString != null) {
                    httpPort = Integer.parseInt(httpPortString);
                }
            }
            catch (IOException exc) {
                exc.printStackTrace();
            }
            catch (NumberFormatException exc) {
                // ignore
            }
        }
        return httpPort;
    }

    private File buildWar() {
        WebArchive war = ShrinkWrap.create(WebArchive.class);

        String classpath = System.getProperty("java.class.path");
        String[] pathElems = classpath.split(File.pathSeparator);

        for (String pathElem : pathElems) {
            File file = new File(pathElem);
            if (file.exists() && classpathFilter.accept(file)) {
                if (file.isDirectory()) {
                    JavaArchive jar = ShrinkWrap.create(JavaArchive.class);
                    jar.as(ExplodedImporter.class).importDirectory(file);
                    war.addAsLibrary(jar);
                }
                else {
                    JavaArchive jar = ShrinkWrap.createFromZipFile(JavaArchive.class, file);
                    war.addAsLibrary(jar);
                }
            }
        }
        for (File metadata : metadataFiles) {
            if (metadata.exists()) {
                war.addAsWebInfResource(metadata);
            }
        }
        File tmpWar = new File(webappsDir, "jeeunit.war");
        war.as(ZipExporter.class).exportTo(tmpWar, true);
        return tmpWar;
    }

    public void shutdown() {
        try
        {
            tomcat.stop();
        }
        catch (LifecycleException exc)
        {
            throw new RuntimeException(exc);
        }
    }

    public URI autodeploy() {
        if (!isDeployed) {
            File war = buildWar();
            
            
            // create webapp loader
            WebappLoader loader = new WebappLoader();


            // Default web.xml, contains JSP servlet, mime types, welcome default etc.
            String defaultWebXml = new File(tomcatHome, "conf/web.xml")
                    .getAbsolutePath();

            StandardContext appContext = (StandardContext) tomcat.createContext(
                    contextRoot, webappDir.getAbsolutePath());
            appContext.setLoader(loader);
            appContext.setReloadable(true);
            appContext.setDefaultWebXml(defaultWebXml);
            
            
            Wrapper servlet = appContext.createWrapper();
            servlet.setServletClass(TestRunnerServlet.class.getName());
            servlet.setName("testrunner");
            servlet.setLoadOnStartup(2);
            
            appContext.addChild(servlet);
            appContext.addServletMapping("/*", "testrunner");
            
            ContextResource resource = new ContextResource();
            resource.setAuth("Container");
            resource.setName("BeanManager");
            resource.setType(BeanManager.class.getName());
            resource.setProperty("factory", "org.jboss.weld.resources.ManagerObjectFactory");

            appContext.getNamingResources().addResource(resource);

            
            ContextResourceEnvRef resourceRef = new ContextResourceEnvRef();
            resourceRef.setName("BeanManager");
            resourceRef.setType(BeanManager.class.getName());
            
            appContext.getNamingResources().addResourceEnvRef(resourceRef);

            appContext.addApplicationListener("org.jboss.weld.environment.servlet.Listener");
            
            
            
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

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
package com.googlecode.jeeunit.tomcat7;

import static com.googlecode.jeeunit.impl.Constants.BEAN_MANAGER_NAME;
import static com.googlecode.jeeunit.impl.Constants.BEAN_MANAGER_TYPE;
import static com.googlecode.jeeunit.impl.Constants.CDI_SERVLET_CLASS;
import static com.googlecode.jeeunit.impl.Constants.CONTEXT_XML;
import static com.googlecode.jeeunit.impl.Constants.JEEUNIT_APPLICATION_NAME;
import static com.googlecode.jeeunit.impl.Constants.JEEUNIT_CONTEXT_ROOT;
import static com.googlecode.jeeunit.impl.Constants.SPRING_SERVLET_CLASS;
import static com.googlecode.jeeunit.impl.Constants.TESTRUNNER_NAME;
import static com.googlecode.jeeunit.impl.Constants.TESTRUNNER_URL;
import static com.googlecode.jeeunit.impl.Constants.WELD_MANAGER_FACTORY;
import static com.googlecode.jeeunit.impl.Constants.WELD_SERVLET_LISTENER;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ContextResourceEnvRef;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.FileUtils;
import org.glassfish.embeddable.archive.ScatteredArchive;
import org.glassfish.embeddable.archive.ScatteredArchive.Type;

import com.googlecode.jeeunit.impl.ClasspathFilter;
import com.googlecode.jeeunit.impl.Configuration;
import com.googlecode.jeeunit.impl.ConfigurationLoader;
import com.googlecode.jeeunit.impl.ZipExploder;
import com.googlecode.jeeunit.spi.ContainerLauncher;

/**
 * Singleton implementing the {@link ContainerLauncher} functionality for
 * Embedded Tomcat 7. The configuration file for the deployed web app is
 * expected in {@code src/test/resources/META-INF/context.xml}.
 * <p>
 * {@link Tomcat} does not let us start the server first and then deploy apps,
 * so we actually start the container and deploy the application in
 * {@code autodeploy()}.
 * <p>
 * For configuring the Tomcat 7 container provide a properties file
 * {@code jeeunit.properties} in the classpath root. You can set the following
 * properties:
 * <ul>
 * <li>{@code jeeunit.tomcat7.http.port} port for the embedded HTTP server
 * (default: 8080)</li>
 * <li>{@code jeeunit.tomcat7.weld.listener} add Weld listener to web.xml?
 * (default: false)</li>
 * </ul>
 * 
 * @author hwellmann
 * 
 */
public class EmbeddedTomcat7Container implements ContainerLauncher,
        LifecycleListener {


    private static EmbeddedTomcat7Container instance;

    private Tomcat tomcat;

    private FileFilter classpathFilter;

    private String applicationName;

    private String contextRoot;

    private File configuration;

    private boolean isDeployed;

    private List<File> metadataFiles = new ArrayList<File>();

    private File tempDir;

    private File catalinaHome;

    private File webappDir;

    private File webappsDir;

    private File jeeunitWar;

    /**
     * Default filter suppressing Tomcat and Eclipse components from the
     * classpath when building the ad hoc WAR.
     * 
     * @author hwellmann
     * 
     */
    private static String[] excludes = { 
        "tomcat-", 
        ".cp", 
        "servlet-",
        "geronimo-servlet_", 
        "shrinkwrap-", 
        "xml-apis"
    };

    private Configuration config;

    private EmbeddedTomcat7Container() {
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

    public static synchronized EmbeddedTomcat7Container getInstance() {
        if (instance == null) {
            instance = new EmbeddedTomcat7Container();
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

    @Override
    public void setClasspathFilter(FileFilter classpathFilter) {
        this.classpathFilter = classpathFilter;
    }

    @Override
    public synchronized void launch() {
        if (tomcat != null) {
            return;
        }

        config = new ConfigurationLoader().load();
        prepareDirectories();

        tomcat = new Tomcat();
        tomcat.setBaseDir(catalinaHome.getAbsolutePath());


        /*
         * Running under "Run as JUnit test" from Eclipse in a separate process,
         * we do not get notified when Eclipse is finished running the test
         * suite. The shutdown hook is just to be on the safe side.
         */
        addShutdownHook();

    }

    private void prepareDirectories() {
        webappsDir = new File(tempDir, "webapps");
        webappsDir.mkdirs();
        webappDir = new File(webappsDir, contextRoot);
        catalinaHome = new File(tempDir, "catalina");

    }

    private URI buildWar() throws IOException {
        ScatteredArchive sar;
        File webResourceDir = getWebResourceDir();
        if (webResourceDir.exists() && webResourceDir.isDirectory()) {
            sar = new ScatteredArchive("jeeunit-autodeploy", Type.WAR,
                    webResourceDir);
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
            exploder.processFile(new File(warBase).getAbsolutePath(),
                    webResourceDir.getAbsolutePath());
        }
        return webResourceDir;
    }

    @Override
    public void shutdown() {
        try {
            tomcat.stop();
        }
        catch (LifecycleException exc) {
            throw new RuntimeException(exc);
        }
    }

    @Override
    public URI autodeploy() {
        if (!isDeployed) {
            try {
                buildWar();

                WebappLoader loader = new WebappLoader();
                loader.setLoaderClass(EmbeddedWebappClassLoader.class.getName());

                StandardContext appContext = (StandardContext) tomcat
                        .addWebapp(contextRoot, webappDir.getAbsolutePath());
                appContext.setLoader(loader);
                setContextXml(appContext);
                appContext.addLifecycleListener(this);

                Wrapper servlet = appContext.createWrapper();
                String servletClass = config.isEnableWeldListener() ? CDI_SERVLET_CLASS
                        : SPRING_SERVLET_CLASS;
                servlet.setServletClass(servletClass);
                servlet.setName(TESTRUNNER_NAME);
                servlet.setLoadOnStartup(2);

                appContext.addChild(servlet);
                appContext.addServletMapping(TESTRUNNER_URL, TESTRUNNER_NAME);

                if (config.isEnableWeldListener()) {
                    addWeldBeanManager(appContext);
                }
                startServer();
            }
            catch (IOException exc) {
                throw new RuntimeException(exc);
            }
            catch (ServletException exc) {
                throw new RuntimeException(exc);
            }

        }
        return getContextRootUri();
    }

    private void startServer() {
        try {
            tomcat.enableNaming();
            tomcat.setPort(config.getHttpPort());
            tomcat.start();
            isDeployed = true;
        }
        catch (LifecycleException exc) {
            exc.printStackTrace();
            throw new RuntimeException(exc);
        }
    }

    private void setContextXml(StandardContext appContext) {
        for (String fileName : CONTEXT_XML) {
            File contextXml = new File(fileName);
            if (contextXml.exists()) {
                URL contextUrl;
                try {
                    contextUrl = contextXml.toURI().toURL();
                    appContext.setConfigFile(contextUrl);
                }
                catch (MalformedURLException exc) {
                    throw new RuntimeException(exc);
                }
                break;
            }
        }
    }

    private void addWeldBeanManager(StandardContext appContext) {
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

    @Override
    public URI getContextRootUri() {
        try {
            return new URI(String.format("http://localhost:%d/%s/",
                    config.getHttpPort(), getContextRoot()));
        }
        catch (URISyntaxException exc) {
            throw new RuntimeException(exc);
        }
    }

    @Override
    public void addMetadata(File file) {
        metadataFiles.add(file);
    }

    private File createTempDir() {
        File tmpRoot = FileUtils.getTempDirectory();
        File tmpDir = new File(tmpRoot, UUID.randomUUID().toString());
        tmpDir.mkdir();
        return tmpDir;
    }

    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        if (event.getType().equals(Lifecycle.AFTER_STOP_EVENT)) {
            try {
                FileUtils.deleteDirectory(tempDir);
            }
            catch (IOException exc) {
                // ignore
            }
        }
    }
}

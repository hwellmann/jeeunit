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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.glassfish.embeddable.archive.ScatteredArchive;
import org.glassfish.embeddable.archive.ScatteredArchive.Type;

import com.caucho.resin.HttpEmbed;
import com.caucho.resin.ResinEmbed;
import com.caucho.resin.WebAppEmbed;
import com.googlecode.jeeunit.impl.ClasspathFilter;
import com.googlecode.jeeunit.impl.Configuration;
import com.googlecode.jeeunit.impl.ConfigurationLoader;
import com.googlecode.jeeunit.spi.ContainerLauncher;

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
public class EmbeddedResinContainer implements ContainerLauncher {

    private static EmbeddedResinContainer instance;

    private ResinEmbed resin;
    private FileFilter classpathFilter;

    private String applicationName;
    private String contextRoot;
    private File configuration;
    private boolean isDeployed;

    private List<File> metadataFiles = new ArrayList<File>();

    private File tempDir;

    private static String[] excludes = { "shrinkwrap-", "resin-", "javaee-", "jsr250-",
    "org.eclipse.osgi" };

    private Configuration config;

    
    
    private EmbeddedResinContainer() {
        tempDir = createTempDir();

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
        if (resin != null) {
            return;
        }

        resin = new ResinEmbed();
        config = new ConfigurationLoader().load();
        HttpEmbed httpPortDef = new HttpEmbed(config.getHttpPort());
        resin.addPort(httpPortDef);
        resin.setRootDirectory(new File(tempDir, "serverroot").getAbsolutePath());

        /*
         * Running under "Run as JUnit test" from Eclipse in a separate process, we do not get
         * notified when Eclipse is finished running the test suite. The shutdown hook is just to be
         * on the safe side.
         */
        addShutdownHook();

    }

    private File buildWar() throws IOException {
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
        File warFile = new File(tempDir, "jeeunit.war");
        copyFile(war, warFile);
        return war;        
    }
    
    private void copyFile(File source, File target) throws IOException {
        FileInputStream is = new FileInputStream(source);
        FileOutputStream os = new FileOutputStream(target);
        IOUtils.copy(is, os);
        is.close();
        os.close();
    }

    public void shutdown() {
        resin.stop();
        resin.destroy();
    }

    public URI autodeploy() {
        if (!isDeployed) {
            File war;
            try {
                war = buildWar();
            }
            catch (IOException exc) {
                throw new RuntimeException(exc);
            }
            WebAppEmbed webApp = new WebAppEmbed("/" + getContextRoot(),
                    new File(tempDir, "jeeunit-root").getAbsolutePath());
            webApp.setArchivePath(war.getAbsolutePath());
            resin.addWebApp(webApp);
            resin.start();
            isDeployed = true;
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

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

import java.io.File;
import java.io.FileFilter;
import java.net.URI;

import javax.servlet.ServletException;

import com.googlecode.jeeunit.spi.ContainerLauncher;

/**
 * Implementation of the {@link ContainerLauncher} service provider interface for Embedded
 * Tomcat 7.x.
 * <p>
 * All API calls are simply delegated to the EmbeddedTomcat7Container singleton. This is due to
 * the fact that SPI implementations need to have a public no-arg constructor.
 * 
 * @author hwellmann
 *
 */
public class EmbeddedTomcat7Launcher implements ContainerLauncher {
    
    private EmbeddedTomcat7Container container;

    public EmbeddedTomcat7Launcher() {
        this.container = EmbeddedTomcat7Container.getInstance();
    }
    
    @Override
    public void launch() {
        container.launch();
    }

    @Override
    public void shutdown() {
        container.shutdown();
    }

    @Override
    public URI autodeploy() {
        try {
            return container.autodeploy();
        }
        catch (ServletException exc) {
            throw new RuntimeException(exc);
        }
    }

    @Override
    public void setClasspathFilter(FileFilter filter) {
        container.setClasspathFilter(filter);
    }

    @Override
    public void addMetadata(File file) {
        container.addMetadata(file);
    }

    @Override
    public URI getContextRootUri() {
        return container.getContextRootUri();
    }
}

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
package com.googlecode.jeeunit.spi;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;

/**
 * This service provider interface permits clients to launch an embedded Java EE container
 * and to build and deploy a WAR based on the current classpath with default settings.
 * 
 * Implementations of this interface shall be registered in {@code META-INF/services}.
 * Implementations may use the native container API for launching the container, seeing that
 * {@link javax.ejb.embeddable.EJBContainer} is too generic to be useful.
 * 
 * @author hwellmann
 *
 */
public interface ContainerLauncher {
    
    /** Launches an embedded Java EE container. */
    void launch();
    
    /** Shuts down the embedded container. */
    void shutdown();
    
    /** 
     * Builds an ad hoc WAR from all classpath files and folders matching a given filter 
     * and deploys it to the embedded container.
     * 
     * @return the URI of the application root context (e.g. http://localhost:8080/jeeunit/).
     */
    URI autodeploy();
    
    /**
     * Sets a classpath filter to be considered by {@link #autodeploy()}. Only the classpath
     * components matching this filter will be added to the autodeployed WAR.
     * 
     * @param filter   classpath filter
     */
    void setClasspathFilter(FileFilter filter);
    
    /**
     * Adds a file to be added to the {@code WEB-INF/} meta-data folder.
     * @param file
     */
    void addMetadata(File file);
}

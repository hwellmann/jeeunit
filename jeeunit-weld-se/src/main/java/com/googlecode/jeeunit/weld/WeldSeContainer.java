/*
 * Copyright 2012 Harald Wellmann
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
package com.googlecode.jeeunit.weld;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import com.googlecode.jeeunit.spi.ContainerLauncher;

/**
 * 
 * @author hwellmann
 * 
 */
public class WeldSeContainer implements ContainerLauncher {
    private static WeldSeContainer instance;

    private Weld weld;

    private WeldContainer weldContainer;

    public static synchronized WeldSeContainer getInstance() {
        if (instance == null) {
            instance = new WeldSeContainer();
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

    @Override
    public void launch() {
        addShutdownHook();
        weld = new Weld();
        weldContainer = weld.initialize();
    }

    @Override
    public void shutdown() {
        if (weld != null) {
            weld.shutdown();
        }
    }

    @Override
    public URI autodeploy() {
        return null;
    }

    @Override
    public void setClasspathFilter(FileFilter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addMetadata(File file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI getContextRootUri() {
        throw new UnsupportedOperationException();
    }
    
    public WeldContainer getWeldContainer() {
        return weldContainer;
    }
}

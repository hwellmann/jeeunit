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
package com.googlecode.jeeunit;

import java.util.Iterator;
import java.util.ServiceLoader;

import com.googlecode.jeeunit.spi.ContainerLauncher;

/**
 * Looks up the first {@link ContainerLauncher} implementation via the {@link ServiceLoader}.
 * Make sure to include exactly one implementation on the classpath.
 * 
 * @author hwellmann
 *
 */
public class ContainerLauncherLookup {

    private ContainerLauncherLookup() {
    }

    /**
     * Returns a ContainerLauncher implementation.
     * @return a ContainerLauncher
     * @throws IllegalStateException if no service implementation is found
     */
    public static ContainerLauncher getContainerLauncher() {
        ContainerLauncher launcher = null;
        ServiceLoader<ContainerLauncher> loader = ServiceLoader.load(ContainerLauncher.class);
        Iterator<ContainerLauncher> it = loader.iterator();

        while (launcher == null && it.hasNext()) {
            launcher = it.next();
        }
        if (launcher == null) {
            String msg = "no implementation of ContainerLauncher SPI on classpath";
            throw new IllegalStateException(msg);
        }
        return launcher;
    }
}

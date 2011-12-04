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

import com.googlecode.jeeunit.impl.DelegatingContainerLauncher;
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
public class EmbeddedJBoss7Launcher extends DelegatingContainerLauncher<EmbeddedJBoss7Container> {
    
    @Override
    public synchronized EmbeddedJBoss7Container getSingleton() {
        return EmbeddedJBoss7Container.getInstance();
    }
}

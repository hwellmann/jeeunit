/*
 * Copyright 2010 Harald Wellmann
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

package com.googlecode.jeeunit.concurrent.impl;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A factory for creating named threads belonging to a ThreadGroup.
 * 
 * @author hwellmann
 *
 */
public class NamedThreadFactory implements ThreadFactory {
    private static AtomicInteger poolNumber = new AtomicInteger(1);
    private AtomicInteger threadNumber = new AtomicInteger(1);
    private ThreadGroup group;

    /**
     * Creates a NamedThreadFactory with a given poolName
     * @param poolName  name prefix for all threads created by this factory
     */
    public NamedThreadFactory(String poolName) {
        group = new ThreadGroup(poolName + "-" + poolNumber.getAndIncrement());
    }

    /**
     * Creates a new thread for the given runnable. The thread belongs to the group wrapped
     * by this factory. 
     */
    @Override
    public Thread newThread(Runnable r) {
        return new Thread(group, r, group.getName() + "-thread-" + threadNumber.getAndIncrement(), 0);
    }
}
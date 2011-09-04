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

package com.googlecode.jeeunit.concurrent;

import java.util.List;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Parameterized;

import com.googlecode.jeeunit.concurrent.impl.ConcurrentRunnerScheduler;

/**
 * An extension of the {@code Parameterized} JUnit Runner for running parameterized tests
 * concurrently. The tests will be scheduled to an Executor with a thread pool. Use the
 * {@code @Concurrent} annotation on the class to specify the number of threads.
 * 
 * See {@link ConcurrentRunner} and {@link Parameterized} for details.
 * 
 * @author hwellmann
 * 
 */
public class ConcurrentParameterized extends Parameterized {

    private ConcurrentRunnerScheduler scheduler;

    public ConcurrentParameterized(Class<?> klass) throws Throwable {
        super(klass);
        scheduler = new ConcurrentRunnerScheduler(klass);
        setScheduler(scheduler);
    }

    @Override
    public void run(RunNotifier notifier) {
        super.run(notifier);
        scheduler.suiteFinished();
    }

    @Override
    protected List<Runner> getChildren() {
        for (Runner runner : super.getChildren()) {
            BlockJUnit4ClassRunner classRunner = (BlockJUnit4ClassRunner) runner;
            classRunner.setScheduler(scheduler);
        }
        return super.getChildren();
    }
}

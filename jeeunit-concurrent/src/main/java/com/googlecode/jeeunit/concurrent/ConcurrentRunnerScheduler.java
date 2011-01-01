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

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.runners.model.RunnerScheduler;

public class ConcurrentRunnerScheduler implements RunnerScheduler {

    private ExecutorService executorService;
    private CompletionService<Void> completionService;
    private Queue<Future<Void>> tasks = new LinkedList<Future<Void>>();

    public ConcurrentRunnerScheduler(Class<?> klass) {
        int numThreads = klass.isAnnotationPresent(Concurrent.class) ? klass
                .getAnnotation(Concurrent.class).threads() : 1;
        executorService = Executors.newFixedThreadPool(numThreads,
                new NamedThreadFactory(klass.getSimpleName()));
        completionService = new ExecutorCompletionService<Void>(executorService);
    }

    @Override
    public void schedule(Runnable childStatement) {
        tasks.offer(completionService.submit(childStatement, null));
    }

    @Override
    public void finished() {
        try {
            while (!tasks.isEmpty())
                tasks.remove(completionService.take());
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        finally {
            while (!tasks.isEmpty())
                tasks.poll().cancel(true);
            executorService.shutdownNow();
        }
    }
}

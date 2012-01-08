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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URI;

import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import com.googlecode.jeeunit.spi.ContainerLauncher;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class JeeunitRunner extends BlockJUnit4ClassRunner {

    private ContainerLauncher launcher;
    private WebResource testRunner;

    public JeeunitRunner(Class<?> klass) throws InitializationError {
        super(klass);
        System.setProperty("java.util.logging.config.file", "src/test/resources/logging.properties");
        launcher = ContainerLauncherLookup.getContainerLauncher();
        launcher.launch();
        URI contextRoot = launcher.autodeploy();
        testRunner = getTestRunner(contextRoot);
    }

    @Override
    protected Statement methodBlock(FrameworkMethod method) {
        Object test;
        try {
            test = new ReflectiveCallable() {
                @Override
                protected Object runReflectiveCall() throws Throwable {
                    return createTest();
                }
            }.run();
        }
        catch (Throwable e) {
            return new Fail(e);
        }

        Statement statement = methodInvoker(method, test);
        return statement;
    }
    
    @Override
    protected Statement methodInvoker(final FrameworkMethod method, Object test) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                Throwable result = getRemoteTestResult(method);
                if (result != null) {
                    throw result;
                }
            }            
        };
    }

    private Throwable getRemoteTestResult(FrameworkMethod method) throws IOException,
            ClassNotFoundException {
        InputStream is = testRunner.queryParam("class", getTestClass().getName())
                .queryParam("method", method.getName()).get(InputStream.class);

        ObjectInputStream ois = new ObjectInputStream(is);
        Object object = ois.readObject();
        if (object instanceof Throwable) {
            return (Throwable) object;
        }
        else if (object instanceof String) {
            return null;
        }
        throw new IllegalStateException();
    }

    private WebResource getTestRunner(URI contextRoot) {
        URI uri = contextRoot.resolve("testrunner");
        Client client = Client.create();
        return client.resource(uri);

    }
}

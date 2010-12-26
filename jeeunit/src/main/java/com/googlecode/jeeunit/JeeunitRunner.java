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
package com.googlecode.jeeunit;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.ServiceLoader;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.junit.Ignore;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import com.googlecode.jeeunit.spi.ContainerLauncher;
import com.sun.jersey.api.client.WebResource;

public class JeeunitRunner extends BlockJUnit4ClassRunner {

    private ContainerLauncher launcher;

    public JeeunitRunner(Class<?> klass) throws InitializationError {
        super(klass);
        System.setProperty("java.util.logging.config.file", "src/test/resources/logging.properties");
        if (!isEmbedded()) {
            launcher = findLauncher();
            launcher.launch();
        }
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        if (!isEmbedded()) {

            EachTestNotifier eachNotifier = makeNotifier(method, notifier);
            if (method.getAnnotation(Ignore.class) != null) {
                eachNotifier.fireTestIgnored();
                return;
            }

            eachNotifier.fireTestStarted();
            try {
                Throwable result = getRemoteTestResult(method);
                if (result instanceof AssumptionViolatedException) {
                    eachNotifier.addFailedAssumption((AssumptionViolatedException) result);                    
                }
                else if (result != null) {
                    eachNotifier.addFailure(result);
                }
            }
            catch (IOException exc) {
                eachNotifier.addFailure(exc);
            }
            catch (ClassNotFoundException exc) {
                eachNotifier.addFailure(exc);
            }
            finally {
                eachNotifier.fireTestFinished();
            }
        }
        else {
            super.runChild(method, notifier);
        }
    }

    private Throwable getRemoteTestResult(FrameworkMethod method) throws IOException,
            ClassNotFoundException {
        WebResource webResource = launcher.getTestRunner();
        InputStream is = webResource
            .queryParam("class", getTestClass().getName())
            .queryParam("method", method.getName())
            .get(InputStream.class);
        
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

    private EachTestNotifier makeNotifier(FrameworkMethod method, RunNotifier notifier) {
        Description description = describeChild(method);
        return new EachTestNotifier(notifier, description);
    }

    @Override
    protected Object createTest() throws Exception {
        if (isEmbedded()) {
            Object test = super.createTest();
            inject(test);
            return test;
        }
        else {
            return super.createTest();
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void inject(Object test) {
        BeanManager mgr = BeanManagerLookup.getBeanManager();
        AnnotatedType annotatedType = mgr.createAnnotatedType(test.getClass());
        InjectionTarget target = mgr.createInjectionTarget(annotatedType);
        CreationalContext context = mgr.createCreationalContext(null);
        target.inject(test, context);
    }

    private boolean isEmbedded() {
        final String BEAN_MANAGER_JNDI = "java:comp/BeanManager";
        try {
            InitialContext ctx = new InitialContext();
            return ctx.lookup(BEAN_MANAGER_JNDI) != null;
        }
        catch (NamingException exc) {
            return false;
        }
    }

    private ContainerLauncher findLauncher() {
        ContainerLauncher launcher = null;
        ServiceLoader<ContainerLauncher> loader = ServiceLoader.load(ContainerLauncher.class);
        Iterator<ContainerLauncher> it = loader.iterator();

        while (launcher == null && it.hasNext()) {
            launcher = it.next();
        }
        return launcher;
    }
}

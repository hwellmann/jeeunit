package com.googlecode.jeeunit.glassfish;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import com.googlecode.jeeunit.BeanManagerLookup;
import com.sun.jersey.api.client.WebResource;

public class JeeunitRunner extends BlockJUnit4ClassRunner {

    private GlassfishLauncher launcher;

    public JeeunitRunner(Class<?> klass) throws InitializationError {
        super(klass);
        if (!isEmbedded()) {
            launcher = GlassfishLauncher.getInstance();
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
                WebResource webResource = launcher.getWebResource();
                String result = webResource.queryParam("class", getTestClass().getName()).
                    queryParam("method", method.getName()).
                    get(String.class);
                Assert.assertTrue(result.contains("All tests passed"));
            }
            catch (AssumptionViolatedException e) {
                eachNotifier.addFailedAssumption(e);
            }
            catch (Throwable e) {
                eachNotifier.addFailure(e);
            }
            finally {
                eachNotifier.fireTestFinished();
            }
        }
        else {
            super.runChild(method, notifier);
        }
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
        catch (NamingException e) {
            return false;
        }
    }
}

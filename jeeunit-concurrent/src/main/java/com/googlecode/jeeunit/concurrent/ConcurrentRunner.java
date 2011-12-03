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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;

import com.googlecode.jeeunit.concurrent.impl.ConcurrentRunnerScheduler;

/**
 * A JUnit Runner for running tests concurrently. The tests will be scheduled to an Executor with a
 * thread pool. Use the {@code @Concurrent} annotation on the class to specify the number of threads.
 * Use the {@code @Repeat} annotation on the class or on the methods to specify the number of times 
 * each method will be executed. A class level {@code @Repeat} annotation provides a default value
 * for methods without a {@code @Repeat} annotation. 
 * <p>
 * Example:
 * 
 * <pre>
 * &#064;RunWith(ConcurrentRunner.class)
 * &#064;Concurrent(threads = 5)
 * public class MyConcurrentTest {
 * 
 *     &#064;Test
 *     &#064;Repeat(times = 10)
 *     public void myTestMethod() {
 *     }
 * }
 * 
 *     &#064;Test
 *     &#064;Repeat(times = 15)
 *     public void anotherMethod() {
 *     }
 * }
 * </pre>
 * 
 * @author hwellmann
 * 
 */
public class ConcurrentRunner extends BlockJUnit4ClassRunner {

    private Repeat repeatDefault;

    private class RepeatedFrameworkMethod extends FrameworkMethod {

        private int number;

        public RepeatedFrameworkMethod(Method method, int i) {
            super(method);
            this.number = i;
        }

        @Override
        public String getName() {
            return String.format("%s[%d]", super.getName(), number);
        }

    }

    public ConcurrentRunner(Class<?> klass) throws Throwable {
        super(klass);
        repeatDefault = getDescription().getAnnotation(Repeat.class);
        setScheduler(new ConcurrentRunnerScheduler(klass));
    }

    @Override
    protected List<FrameworkMethod> getChildren() {
        List<FrameworkMethod> children = new ArrayList<FrameworkMethod>();
        List<FrameworkMethod> testMethods = super.getChildren();
        for (FrameworkMethod testMethod : testMethods) {
            Repeat repeat = testMethod.getAnnotation(Repeat.class);
            if (repeat == null) {
                repeat = repeatDefault;
            }
            if (repeat == null) {
                children.add(testMethod);
            }
            else {
                for (int i = 0; i < repeat.times(); i++) {
                    children.add(new RepeatedFrameworkMethod(testMethod.getMethod(), i));
                }
            }
        }
        return children;
    }
}

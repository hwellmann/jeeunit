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

package com.googlecode.jeeunit.concurrent.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import com.googlecode.jeeunit.concurrent.Concurrent;
import com.googlecode.jeeunit.concurrent.ConcurrentParameterized;

@RunWith(ConcurrentParameterized.class)
@Concurrent(threads = 8)
public class MultithreadedParameterizedTest {

    private static final int MAX_LOOP = 5;
    private int parameter;

    public MultithreadedParameterizedTest(int parameter) {
        this.parameter = parameter;
    }

    @Parameters
    public static Collection<Object[]> getParameters() {
        List<Object[]> parameters = new ArrayList<Object[]>(10);
        for (int i = 1; i <= 4; i++) {
            parameters.add(new Object[] { i });
        }
        return parameters;
    }

    @Test
    public void methodOne() throws InterruptedException {
        for (int i = 0; i < MAX_LOOP; i++) {
            System.out.println(String.format("methodOne: parameter=%d: %d", parameter, i));
            Thread.sleep(2000);
        }
    }

    @Test
    public void methodTwo() throws InterruptedException {
        for (int i = 0; i < MAX_LOOP; i++) {
            System.out.println(String.format("methodTwo: parameter=%d: %d", parameter, i));
            Thread.sleep(2000);
        }
    }

}

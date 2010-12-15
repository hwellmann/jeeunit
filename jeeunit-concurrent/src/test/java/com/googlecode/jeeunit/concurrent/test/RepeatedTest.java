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

import org.junit.Test;
import org.junit.runner.RunWith;

import com.googlecode.jeeunit.concurrent.Concurrent;
import com.googlecode.jeeunit.concurrent.ConcurrentRunner;
import com.googlecode.jeeunit.concurrent.Repeat;

@RunWith(ConcurrentRunner.class)
@Repeat(times = 2)
@Concurrent(threads = 10)
public class RepeatedTest {

    private static final int MAX_LOOP = 5;

    @Test
    public void repeatedMethod() throws InterruptedException {
        for (int i = 0; i < MAX_LOOP; i++) {
            System.out.println(String.format("repeatedMethod: %d", i));
            Thread.sleep(500);
        }
    }

    @Test
    @Repeat(times = 3)
    public void anotherRepeatedMethod() throws InterruptedException {
        for (int i = 0; i < MAX_LOOP; i++) {
            System.out.println(String.format("anotherRepeatedMethod: %d", i));
            Thread.sleep(500);
        }
    }
}

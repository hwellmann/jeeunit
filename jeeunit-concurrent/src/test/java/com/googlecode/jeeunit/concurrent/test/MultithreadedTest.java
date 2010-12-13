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

import com.googlecode.jeeunit.concurrent.ConcurrentRunner;

@RunWith(ConcurrentRunner.class)
public class MultithreadedTest {

    private static final int MAX_LOOP = 5;

    private void runMethod(int methodNum) throws InterruptedException {
        for (int i = 0; i < MAX_LOOP; i++) {
            System.out.println(String.format("method%d: %d", methodNum, i));
            Thread.sleep(500);
        }
    }

    @Test
    public void method1() throws InterruptedException {
        runMethod(1);
    }

    @Test
    public void method2() throws InterruptedException {
        runMethod(2);
    }

    @Test
    public void method3() throws InterruptedException {
        runMethod(3);
    }

    @Test
    public void method4() throws InterruptedException {
        runMethod(4);
    }

    @Test
    public void method5() throws InterruptedException {
        runMethod(5);
    }

    @Test
    public void method6() throws InterruptedException {
        runMethod(6);
    }

    @Test
    public void method7() throws InterruptedException {
        runMethod(7);
    }

    @Test
    public void method8() throws InterruptedException {
        runMethod(8);
    }

    @Test
    public void method9() throws InterruptedException {
        runMethod(9);
    }

    @Test
    public void method10() throws InterruptedException {
        runMethod(10);
    }

}

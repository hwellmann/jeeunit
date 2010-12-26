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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.googlecode.jeeunit.concurrent.Concurrent;
import com.googlecode.jeeunit.concurrent.ConcurrentRunner;
import com.googlecode.jeeunit.concurrent.Repeat;


/** 
 * Tests the unsynchronized {@link Counter} class. Set {@code threads} to a value greater than 1
 * to see this test fail.
 */
@RunWith(ConcurrentRunner.class)
@Concurrent(threads = 1)
public class MissingSynchronisationTest {
    
    private static class Counter {

        private int value;
        
        public Counter(int value) {
            this.value = value;
        }
        
        public void increment() {
            value++;
        }
        
        public void decrement() {
            value--;
        }
        
        public int getValue() {
            return value;
        }
    }
    
    
    private static Counter counter = new Counter(10);
    
    @Test
    @Repeat(times = 10)
    public void incrementAndDecrement() throws InterruptedException {
        int initialValue = counter.getValue();
        counter.increment();
        Thread.sleep(5);
        counter.decrement();
        assertEquals(initialValue, counter.getValue());
    }
}

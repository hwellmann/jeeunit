/*
 * Copyright  2000-2005 The Apache Software Foundation
 * Modified 2010 by Harald Wellmann
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

package com.googlecode.jeeunit.report;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class FailureCollector extends RunListener
{
    private List<Failure> failures;
    
    public FailureCollector()
    {
        super();
        this.failures = new ArrayList<Failure>();
    }
    
    @Override
    public void testRunFinished(Result result) throws Exception
    {
        failures.addAll(result.getFailures());
    }
    
    public List<Failure> getFailures()
    {
        return failures;
    }
    
}

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
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.notification.Failure;

import com.googlecode.jeeunit.report.FailureCollector;
import com.googlecode.jeeunit.report.XmlFormatter;

@WebServlet(urlPatterns = "/testrunner")
public class TestRunnerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String className = request.getParameter("class");
        String methodName = request.getParameter("method");
        try {
            Class<?> clazz = getClass().getClassLoader().loadClass(className);
            response.setContentType("text/plain");
            PrintWriter writer = response.getWriter();
            writer.println("Running test suite....");
            runSuite(writer, clazz, methodName);
            writer.println("Test suite completed");
            writer.close();
        }
        catch (ClassNotFoundException exc) {
            throw new ServletException(exc);
        }
    }

    protected void runSuite(PrintWriter writer, Class<?> clazz, String methodName) {
        JUnitCore core = new JUnitCore();
        XmlFormatter formatter = new XmlFormatter();
        FailureCollector collector = new FailureCollector();
        core.addListener(formatter);
        core.addListener(collector);
        core.run(Request.method(clazz, methodName));
        List<Failure> failures = collector.getFailures();
        if (failures.isEmpty()) {
            writer.println("All tests passed");
        } else {
            writer.println(failures.size() + " test failures");
        }
    }
}

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

import static com.googlecode.jeeunit.report.XmlConstants.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;


public class XmlFormatter extends RunListener
{

    /** constant for unnnamed testsuites/cases */
    private static final String UNKNOWN = "unknown";

    private static final String REPORT_DIR = "target/surefire-reports";

    private static final String[] DEFAULT_TRACE_FILTERS = new String[] {
            "junit.framework.TestCase",
            "junit.framework.TestResult",
            "junit.framework.TestSuite",
            "junit.framework.Assert.", // don't filter AssertionFailure
            "junit.swingui.TestRunner", "junit.awtui.TestRunner",
            "junit.textui.TestRunner", "java.lang.reflect.Method.invoke(",
            "sun.reflect.", "org.apache.tools.ant.",
            // JUnit 4 support:
            "org.junit.", "junit.framework.JUnit4TestAdapter",
            // See wrapListener for reason:
            "Caused by: java.lang.AssertionError", " more", };

    private static DocumentBuilder getDocumentBuilder()
    {
        try
        {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }
        catch (Exception exc)
        {
            throw new ExceptionInInitializerError(exc);
        }
    }

    /**
     * The XML document.
     */
    private Document doc;

    /**
     * The wrapper for the whole testsuite.
     */
    private Element rootElement;

    /**
     * Element for the current test.
     */
    private Hashtable<Description, Element> testElements = new Hashtable<Description, Element>();

    /**
     * tests that failed.
     */
    private Hashtable<Description, Description> failedTests = new Hashtable<Description, Description>();

    /**
     * Timing helper.
     */
    private Hashtable<Description, Long> testStarts = new Hashtable<Description, Long>();

    /**
     * Where to write the log to.
     */
    private OutputStream out;

    private Properties props;

    private ByteArrayOutputStream errStrm;

    private ByteArrayOutputStream outStrm;

    public XmlFormatter()
    {
        File outputDir = new File(REPORT_DIR);
        outputDir.mkdirs();
    }

    public void setOutput(OutputStream out)
    {
        this.out = out;
    }

    public void setSystemOutput(String out)
    {
        formatOutput(SYSTEM_OUT, out);
    }

    public void setSystemError(String out)
    {
        formatOutput(SYSTEM_ERR, out);
    }

    /**
     * The whole testsuite started.
     * 
     * @throws FileNotFoundException
     */
    @Override
    public void testRunStarted(Description description)
        throws FileNotFoundException
    {
        doc = getDocumentBuilder().newDocument();
        rootElement = doc.createElement(TESTSUITE);
        assert description.isSuite();
        String n = description.getChildren().get(0).getDisplayName();
        rootElement.setAttribute(ATTR_NAME, n == null ? UNKNOWN : n);

        // Output properties
        Element propsElement = doc.createElement(PROPERTIES);
        rootElement.appendChild(propsElement);
        props = System.getProperties();
        if (props != null)
        {
            Enumeration<?> e = props.propertyNames();
            while (e.hasMoreElements())
            {
                String name = (String) e.nextElement();
                Element propElement = doc.createElement(PROPERTY);
                propElement.setAttribute(ATTR_NAME, name);
                propElement.setAttribute(ATTR_VALUE, props.getProperty(name));
                propsElement.appendChild(propElement);
            }
        }

        openOutputStream(n);
    }

    private void openOutputStream(String n) throws FileNotFoundException
    {
        File file = new File(REPORT_DIR, n + ".xml");
        out = new FileOutputStream(file);

        errStrm = new ByteArrayOutputStream();
        PrintStream systemError = new PrintStream(errStrm);
        System.setErr(systemError);
        outStrm = new ByteArrayOutputStream();
        PrintStream systemOut = new PrintStream(outStrm);
        System.setOut(systemOut);
    }

    /**
     * The whole testsuite ended.
     */
    @Override
    public void testRunFinished(Result result)
    {
        setSystemOutput(outStrm.toString());
        setSystemError(errStrm.toString());
        rootElement.setAttribute(ATTR_TESTS, "" + result.getRunCount());
        rootElement.setAttribute(ATTR_FAILURES, "" + result.getFailureCount());
        rootElement.setAttribute(ATTR_ERRORS, "" + result.getIgnoreCount());
        rootElement
                .setAttribute(ATTR_TIME, "" + (result.getRunTime() / 1000.0));
        if (out != null)
        {
            Writer wri = null;
            try
            {
                wri = new BufferedWriter(new OutputStreamWriter(out, "UTF8"));
                wri.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
                (new DomElementWriter()).write(rootElement, wri, 0, "  ");
                wri.flush();
            }
            catch (IOException exc)
            {
                throw new RuntimeException("Unable to write log file", exc);
            } finally
            {
                if (out != System.out && out != System.err)
                {
                    if (wri != null)
                    {
                        try
                        {
                            wri.close();
                        }
                        catch (IOException e)
                        {
                            // ignore
                        }
                    }
                }
            }
        }
    }

    /**
     * Interface TestListener.
     * 
     * <p>
     * A new Test is started.
     */
    @Override
    public void testStarted(Description description)
    {
        testStarts.put(description, new Long(System.currentTimeMillis()));
    }

    /**
     * Interface TestListener.
     * 
     * <p>
     * A Test is finished.
     */
    @Override
    public void testFinished(Description description)
    {
        // Fix for bug #5637 - if a junit.extensions.TestSetup is
        // used and throws an exception during setUp then startTest
        // would never have been called
        if (!testStarts.containsKey(description))
        {
            testStarted(description);
        }

        Element currentTest = null;
        if (!failedTests.containsKey(description))
        {
            currentTest = doc.createElement(TESTCASE);
            String n = description.getDisplayName();
            String[] parts = n.split("[\\(\\)]");
            currentTest.setAttribute(ATTR_NAME, parts[0]);
            // a TestSuite can contain Tests from multiple classes,
            // even tests with the same name - disambiguate them.
            currentTest.setAttribute(ATTR_CLASSNAME, parts[1]);
            rootElement.appendChild(currentTest);
            testElements.put(description, currentTest);
        }
        else
        {
            currentTest = testElements.get(description);
        }

        Long l = testStarts.get(description);
        currentTest.setAttribute(ATTR_TIME, ""
                + ((System.currentTimeMillis() - l.longValue()) / 1000.0));
    }

    /**
     * Interface TestListener for JUnit &lt;= 3.4.
     * 
     * <p>
     * A Test failed.
     */
    @Override
    public void testFailure(Failure f)
    {
        formatError(FAILURE, f.getDescription(), f.getException());
    }

    /**
     * Interface TestListener.
     * 
     * <p>
     * An error occurred while running the test.
     */
    @Override
    public void testIgnored(Description description)
    {
        formatError(ERROR, description, null);
    }

    private void formatError(String type, Description description, Throwable t)
    {
        if (description != null)
        {
            testFinished(description);
            failedTests.put(description, description);
        }

        Element nested = doc.createElement(type);
        Element currentTest = null;
        if (description != null)
        {
            currentTest = testElements.get(description);
        }
        else
        {
            currentTest = rootElement;
        }

        currentTest.appendChild(nested);

        if (t != null)
        {
            String message = t.getMessage();
            if (message != null && message.length() > 0)
            {
                nested.setAttribute(ATTR_MESSAGE, t.getMessage());
            }
            nested.setAttribute(ATTR_TYPE, t.getClass().getName());

            String strace = getFilteredTrace(t);
            Text trace = doc.createTextNode(strace);
            nested.appendChild(trace);
        }
    }

    private void formatOutput(String type, String output)
    {
        Element nested = doc.createElement(type);
        rootElement.appendChild(nested);
        nested.appendChild(doc.createCDATASection(output));
    }

    /**
     * Returns a filtered stack trace. This is ripped out of
     * junit.runner.BaseTestRunner.
     * 
     * @param t
     *                the exception to filter.
     * @return the filtered stack trace.
     */
    public static String getFilteredTrace(Throwable t)
    {
        String trace = getStackTrace(t);
        return filterStack(trace);
    }

    /**
     * Filters stack frames from internal JUnit and Ant classes
     * 
     * @param stack
     *                the stack trace to filter.
     * @return the filtered stack.
     */
    public static String filterStack(String stack)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        StringReader sr = new StringReader(stack);
        BufferedReader br = new BufferedReader(sr);

        String line;
        try
        {
            while ((line = br.readLine()) != null)
            {
                if (!filterLine(line))
                {
                    pw.println(line);
                }
            }
        }
        catch (Exception e)
        {
            return stack; // return the stack unfiltered
        }
        return sw.toString();
    }

    private static boolean filterLine(String line)
    {
        for (int i = 0; i < DEFAULT_TRACE_FILTERS.length; i++)
        {
            if (line.indexOf(DEFAULT_TRACE_FILTERS[i]) != -1)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Convenient method to retrieve the full stacktrace from a given exception.
     * 
     * @param t
     *                the exception to get the stacktrace from.
     * @return the stacktrace from the given exception.
     */
    public static String getStackTrace(Throwable t)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        pw.flush();
        pw.close();
        return sw.toString();
    }

}

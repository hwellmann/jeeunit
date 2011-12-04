package com.googlecode.jeeunit.impl;

public class Constants {

    public static final String[] CONTEXT_XML = { 
        "src/test/resources/META-INF/context.xml", 
        "src/main/webapp/META-INF/context.xml", 
    };
    public static final String BEAN_MANAGER_TYPE = "javax.enterprise.inject.spi.BeanManager";
    public static final String BEAN_MANAGER_NAME = "BeanManager";
    public static final String WELD_MANAGER_FACTORY = "org.jboss.weld.resources.ManagerObjectFactory";
    public static final String WELD_SERVLET_LISTENER = "org.jboss.weld.environment.servlet.Listener";
    public static final String SPRING_SERVLET_CLASS = "com.googlecode.jeeunit.spring.impl.TestRunnerServlet";
    public static final String CDI_SERVLET_CLASS = "com.googlecode.jeeunit.cdi.impl.TestRunnerServlet";
    public static final String JEEUNIT_APPLICATION_NAME = "jeeunit";
    public static final String JEEUNIT_CONTEXT_ROOT = "/" + JEEUNIT_APPLICATION_NAME;
    public static final String TESTRUNNER_NAME = "testrunner";
    public static final String TESTRUNNER_URL = "/testrunner";
    public static final String HTTP_PORT_DEFAULT = "8080";

    public static final String CONFIG_PROPERTIES = "jeeunit.properties";
    public static final String KEY_HTTP_PORT = "jeeunit.http.port";
    public static final String KEY_WELD_LISTENER = "jeeunit.weld.listener";
    public static final String KEY_WAR_BASE = "jeeunit.war.base";
    public static final String KEY_SERVER_HOME = "jeeunit.server.home";
}

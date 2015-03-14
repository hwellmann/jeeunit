# Introduction #

With Java EE 6, developing Java Enterprise applications is easier than ever before.

Test-driven development is now a common practice, so writing and running integration tests for an enterprise application should not be harder than writing plain old unit tests.

Using the [EJBContainer](http://docs.oracle.com/javaee/6/api/javax/ejb/embeddable/EJBContainer.html) API, you can launch an embedded container, your application under test and your integration tests in the same Java virtual machine.

However, this API is rather low-level and does not provide any integrations with popular test frameworks like JUnit.

jeeunit was created to fill this gap: It **encapsulates the embedded container** in a custom JUnit runner, **automatically builds a WAR** based on the current classpath and simply lets you **inject beans into your JUnit tests** from the system under test.

# More than Java EE 6 #

Initially, jeeunit was exclusively focused on Java EE 6, supporting **GlassFish 3.x**, which was the only Java EE 6 compliant server at that time. Support for **Resin 4.x** and **JBoss AS 7** has been added in subsequent releases.

The basic idea of jeeunit can be generalized: The container does not necessarily have to be a Java EE 6 container, and the dependency injection method does not necessarily have to be CDI.

As of release 0.9.0, jeeunit also supports **Tomcat 6 and 7** and **Weld SE** containers and **Spring 3.x** as an alternative dependency injection method.

Unlike Spring's own test container, jeeunit provides a full web container and lets you test features which require a web application context, e.g. the Spring Security configuration of your own application.




# Project components #

This project has three main parts:
  * **jeeunit**, a small reusable library supporting a special flavour of JUnit test suites running in a Java EE 6 container. It does not depend on any specific container.
  * **jeeunit-glassfish**, containing a test driver base class for running a jeeunit suite in Embedded Glassfish.
  * **jeeunit-example**, a simple example project with three subprojects for a persistence layer, a service layer with EJBs and a test layer.

# Prerequisites #

To work with this code, you need
  * Maven 2.2.1 or 3.0.0
  * Glassfish 3.1-b33 or higher

Note that the current version of jeeunit does **not** work with any earlier Glassfish 3.x releases or promoted builds, since the Embedded Glassfish API had incompatible changes in 3.1-b33.

If you do not work with Glassfish or Maven, you may still find the jeeunit library useful for writing your tests, but then you'll have to figure out for yourself how to deploy them to your application server and how to integrate them with your build process.
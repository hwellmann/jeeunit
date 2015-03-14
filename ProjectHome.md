# End of Life Notice #

**As of January 2013, jeeunit has reached end of life. It has been merged into [Pax Exam 3.0.0](http://team.ops4j.org/wiki/display/PAXEXAM3/), which provides equivalent and better functionality for more test containers.**

# Introduction #

With Java EE 6, developing Java Enterprise applications is easier than ever before.

Test-driven development is now a common practice, so writing and running integration tests for an enterprise application should not be harder than writing plain old unit tests.

Using the `EJBContainer` API, you can launch an embedded container, your application under test and your integration tests in the same Java virtual machine.

However, this API is rather low-level and does not provide any integrations with popular test frameworks like JUnit.

jeeunit was created to fill this gap: It **encapsulates the embedded container** in a custom JUnit runner, **automatically builds a WAR** based on the current classpath and simply lets you **inject beans into your JUnit tests** from the system under test.

# More than Java EE 6 #

Initially, jeeunit was exclusively focused on Java EE 6, supporting **GlassFish 3.x**, which was the only Java EE 6 compliant server at that time. Support for **Resin 4.x** and **JBoss AS 7** has been added in subsequent releases.

The basic idea of jeeunit can be generalized: The container does not necessarily have to be a Java EE 6 container, and the dependency injection method does not necessarily have to be CDI.

As of release 0.9.0, jeeunit also supports **Tomcat 6 and 7** and **Weld SE** containers and **Spring 3.x** as an alternative dependency injection method.

Unlike Spring's own test container, jeeunit provides a full web container and lets you test features which require a web application context, e.g. the Spring Security configuration of your own application.


Read the following Wiki pages to learn more:

  * [Testing on Java EE 6](HowItWorks.md)
  * [Testing on Tomcat and Spring](TestingOnTomcat.md)
  * [News](News.md)




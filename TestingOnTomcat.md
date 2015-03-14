# Testing on Tomcat #
## Why bother? ##

Most Java EE 6 servers are fast, lean and modular - in short, everything that J2SE 1.4 was not.

So these days, there may still be good reasons to choose Tomcat and Spring,  but the need for a "lightweight alternative to Java EE" should no longer be on the top of your list.

Anyway, there are lots of Java projects out there based on Tomcat and Spring, and despite Spring's own [Integration Testing](http://static.springsource.org/spring/docs/3.1.x/spring-framework-reference/html/testing.html#integration-testing) support, the jeeunit approach of testing in an embedded container can have some added value even for Spring projects.

## jeeunit vs. Spring Test Context ##

### Real Web Container ###

The Spring Test Context is essentially a vanilla application context supporting auto-rollback transactions, but it's not a web application context. For testing features which depend on a web application context, you need a servlet container.

jeeunit adds your test classes on the fly to a given WAR, deploys the extended test WAR to an embedded Tomcat instance and then injects Spring beans into your test classes as needed.

You can even hit your test application with HTTP requests from within the test container.

### Real Transactions ###

For testing persistence units and the related services, Spring's transactional tests make it very easy to start from a clean slate for every test. However, the transaction boundaries of Spring's `@Transactional` tests are not the same as in your production system, so you may end up with false positives.

With jeeunit, your test environment is one step closer to your production environment.

## Alternative Injection Methods: Spring or CDI ##

Tomcat (or Jetty or any other pure Servlet Container) does not support dependency injection out of the box, but you can add a dependency injection container like Spring or a stand-alone CDI implementation with servlet adapters like Weld or OpenWebBeans to your application to work with dependency injection.

Note that Tomcat + CDI is weaker than Tomcat + Spring in the sense that declarative transactions are not supported.

Working with a Servlet Container like Tomcat (as opposed to a complete Java EE 6 server, Web or Full profile), you can configure jeeunit to use either Spring or CDI for dependency injection.

Using only JSR-330 `@Inject` annotations in your test classes, your choice of a specific dependency injection method is largely transparent to your tests.
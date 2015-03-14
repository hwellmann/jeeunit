## Configuration for Tomcat ##

### Dependencies ###

jeeunit supports both Tomcat 6 and 7. Since Tomcat's embedded API differs between these two major releases, jeeunit has two separate adapter libraries `jeeunit-tomcat6` and `jeeunit-tomcat7`.

These libraries do _not_ include transitive dependencies on Tomcat itself, to leave you the choice of a specific Tomcat release or a partial setup (e.g. without JSP support).

Dependency injection via Spring or via CDI is supported by `jeeunit-spring` and `jeeunit-cdi`, respectively.

To sum up, to work with jeeunit on Tomcat, you need

  * one of `jeeunit-cdi` or `jeeunit-spring`
  * one of `jeeunit-tomcat6` or `jeeunit-tomcat7`
  * Embedded Tomcat artifacts
  * and any transitive dependencies

For more details, please have look at the POMs of the jeeunit test projects for

  * [Tomcat 6 with Spring](http://code.google.com/p/jeeunit/source/browse/jeeunit-example/jeeunit-example-spring-tomcat6/pom.xml)
  * [Tomcat 7 with Spring](http://code.google.com/p/jeeunit/source/browse/jeeunit-example/jeeunit-example-spring-tomcat7/pom.xml).
  * [Tomcat 6 with Weld](http://code.google.com/p/jeeunit/source/browse/jeeunit-example/jeeunit-example-test-tomcat6/pom.xml)
  * [Tomcat 7 with Weld](http://code.google.com/p/jeeunit/source/browse/jeeunit-example/jeeunit-example-test-tomcat7/pom.xml).


### Configuration Properties ###

Working with Tomcat, you can configure jeeunit by means of a Java properties file `jeeunit.propeties` in the root of your classpath.


```xml

jeeunit.http.port = 9090
jeeunit.weld.listener = false
jeeunit.war.base = /home/hwellmann/work/jeeunit/jeeunit-example-spring-tomcat6.war
```

`jeeunit.http.port` sets the HTTP port used by Embedded Tomcat. The default is 8080.

`jeeunit.weld.listener = true` adds the Weld Listener servlet to your test web application. The default is false. Do not set this to true when working with Spring.

`jeeunit.war.base` is the path of an existing WAR to be augmented by your test classes (and some jeeunit internals) to build the test application to be deployed to Tomcat.

This property is optional. If unset, jeeunit builds an on-the-fly WAR from the classpath contents.

A `context.xml` file for Tomcat will be looked up in the following places:

  * `src/test/resources/META-INF/context.xml`
  * `src/main/webapp/META-INF/context.xml`
  * root of classpath
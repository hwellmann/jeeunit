# News #

## 20 Jan 2012: jeeunit merged into Pax Exam 3.0.0 ##

  * [OPS4J Pax Exam 3.0.0](http://team.ops4j.org/wiki/display/PAXEXAM3/) incorporates the jeeunit test containers and provides a unified approach to embedded container testing both for OSGi and Java EE (including CDI-only and web-only scenarios.)

  * The jeeunit project will not be continued. Please check out Pax Exam and give feedback on the OPS4J mailing list ops4j@googlegroups.com.

## 25 May 2012: jeeunit 1.0.0 released ##

  * New test container `jeeunit-openwebbeans` supporting Apache OpenWebBeans 1.1.4.
  * Lots of dependency upgrades.

## 25 Jan 2012: jeeunit 0.9.1 released ##

  * Weld SE support is now better aligned with other containers. There is a `ContainerLauncher` implementation for Weld SE, and tests now work with the `JeeunitRunner` instead of the `CdiJUnitRunner`.
  * Startup and shutdown behaviour of the Weld SE container has been revised. jeeunit no longer tries to start a new container for each test method.

## 19 Dec 2011: jeeunit 0.9.0 released ##

  * jeeunit now supports JBoss AS 7 via the `jeeunit-jboss7` container adapter library.
  * In addition to CDI, Spring Dependency Injection is now supported. The corresponding injection adapters are in `jeeunit-cdi` and `jeeunit-spring`.
  * jeeunit now supports Embedded Tomcat 6 and 7 via the `jeeunit-tomcat6` and `jeeunit-tomcat7` container adapter libraries.
  * For Tomcat containers, you can choose between Spring or CDI dependency injection using the `weld-servlet` library.
  * Wiki pages revised and reorganized.
  * Example libraries have been moved to a `test` profile and are no longer pushed to Maven central.

## 6 Jun 2011: jeeunit 0.8.0 released ##

  * A new annotation `@Transactional` can be used to execute test methods wrapped in a transaction that will be rolled back automatically.

  * jeeunit now supports Caucho Resin via the `jeeunit-resin` adapter library.

## 14 Jan 2011: jeeunit 0.7.0 released ##

  * The `ContainerLauncher` has been refactored and extended for greater flexibility. The API is responsible for starting and stopping an Embedded Container and for building and autodeploying a WAR from the classpath contents. You can set filters to exclude certain files from the classpath. The changes are not compatible with 0.6.0.

  * The `ContainerLauncher` now provides the application context URI of the launched application (instead of the test runner servlet URL), which makes the launcher usable for other applications than jeeunit.

  * All examples and tests now use an Embedded Derby database instead of the Derby Network Server. Thus, it is no longer required to configure and start a Derby server before running the jeeunit Maven build.

  * Maven assembly added to create download packages automatically.

## 27 Dec 2010: jeeunit 0.6.0 released ##

  * jeeunit no longer requires you to build a test WAR to deploy to the embedded server. The test WAR is now built automatically by jeeunit from the classpath contents.

  * For running multiple test classes, you no longer need to write a suite class listing the test classes. Simply select the classes annotated with `@RunWith(JeeunitRunner.class)` and run them as you normally would (in Eclipse, with Maven Surefire etc).

  * jeeunit 0.6.0 requires Embedded Glassfish 3.1-b33 or higher. The Embedded Glassfish API in this build is not backward compatible.

  * jeeunit artifacts are available from Maven Central.
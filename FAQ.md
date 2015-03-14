### Is jeeunit stable? ###

Yes, I'm using it for testing my own Java EE 6 and Spring projects. Of course, different projects use different areas of these frameworks, so you may run into problems if you work with features I've never used. Please submit any issues via the Issue tracker.

### Which servers does jeeunit support? ###

Glassfish 3.1, JBoss AS 7, Resin 4, Tomcat 6 and 7.

### Can I use jeeunit to run tests from my IDE? ###

You can use jeeunit from Eclipse. I haven't tried Netbeans or anything else. jeeunit generates an on-the-fly WAR from all classes and resources visible on the classpath of the JUnit runner used by the IDE. This may include some unwanted stuff on other IDEs. jeeunit currently filters out any classes from the Eclipse installation.

### How does jeeunit differ from Arquillian? ###

  * jeeunit is extremely lean, whereas Arquillian pulls in about 40 JARs.

  * jeeunit automatically builds a WAR from your classpath contents. You do not have to deal with assembly and deployment à la `ShrinkWrap` (actually, I don't think it's a good idea to mimick parts of the build process in your test code).

  * jeeunit supports multiple test classes within the same run, unlike Arquillian where you currently (1.0.0.Alpha4) need to repeat the deployment code in every test class.

  * jeeunit supports transactional tests with automatic rollback, similar to `@Transactional` in Spring tests.

  * jeeunit only works in embedded mode and and supports a different set of servers.

So far, before every major addition to jeeunit, I've checked Arquillian and found it did not really match my requirements, so I kept going. Your mileage may vary, of course.
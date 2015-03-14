## Configuration for JBoss ##

### Prerequisites ###

Unlike the other servers supported by jeeunit, JBoss AS 7 does not offer self-contained embedded artifacts. The Maven artifacts listed below just allow you to bootstrap JBoss from a stand-alone installation in embedded mode.

For this reason, you need a stand-alone JBoss AS 7 installation to run jeeunit tests on JBoss.

### Dependencies ###

To work with jeeunit and Embedded JBoss AS 7, you need the following Maven dependencies:

```
    <dependency>
      <groupId>com.googlecode.jeeunit</groupId>
      <artifactId>jeeunit-jboss7</artifactId>
      <version>${jeeunit.version}</version>
      <scope>test</scope>
    </dependency>

    <dependency>
      <groupId>com.googlecode.jeeunit</groupId>
      <artifactId>jeeunit-cdi</artifactId>
      <version>${jeeunit.version}</version>
      <scope>test</scope>
    </dependency>
    
    <dependency>
      <groupId>org.jboss.as</groupId>
      <artifactId>jboss-as-embedded</artifactId>
      <version>7.0.2.Final</version>
      <scope>test</scope>
      <exclusions>
        <exclusion>
          <groupId>org.jboss.logmanager</groupId>
          <artifactId>jboss-logmanager</artifactId>
        </exclusion>
      </exclusions>
    </dependency>
        
```

**Note:** Without excluding the `jboss-logmanager`, I had an exception saying

> you must set the "java.util.logging.manager" system property
> to "org.jboss.logmanager.LogManager"

but even that did not help. I'm logging with logback and `jul-to-slf4j`.

### Configuration Properties ###

Embedded JBoss in jeeunit is configured through the usual configuration files in `$JBOSS_HOME/standalone/configuration`. Any required JDBC drivers need to be copied to `$JBOSS_HOME/standalone/deployment`.

jeeunit reads the JBoss home directory and the HTTP port from a properties file `jeeunit.properties` on the classpath, e.g.

<pre>
jeeunit.http.port = 9090<br>
jeeunit.server.home = /home/hwellmann/apps/jboss-as-7.0.2.Final<br>
</pre>

**Note:** The HTTP port currently needs to be configured both here and in `$JBOSS_HOME/standalone/configuration/standalone.xml`. This duplication is to be removed in a future release.

### Disclaimer ###

This setup is a result of experimentation and may not be optimal. Embedded JBoss AS 7 is currently undocumented, and I did not get any replies in the JBoss AS 7 forum either.
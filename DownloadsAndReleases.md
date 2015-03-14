## Current Release ##

The current release is **jeeunit 0.9.0**.

## Maven Artifacts ##

jeeunit is built and released with Maven. jeeunit releases get promoted to Maven Central via Sonatype OSS Repository hosting.

To use jeeunit in your own project, you need to add at least two dependencies to your POM, one for the injection method and one for the embedded server adapter, e.g.

```
    <properties>
      <jeeunit.version>0.8.0</jeeunit.version>
    </properties>

    <dependency>
      <groupId>com.googlecode.jeeunit</groupId>
      <artifactId>jeeunit-cdi</artifactId>
      <version>${jeeunit.version}</version>
    </dependency>
    <dependency>
      <groupId>com.googlecode.jeeunit</groupId>
      <artifactId>jeeunit-glassfish</artifactId>
      <version>${jeeunit.version}</version>
      <scope>test</scope>
    </dependency>
```

As of jeeunit 0.9.0, `jeeunit-example-*` artifacts are no longer published to Maven Central.

## Snapshots ##

To access snapshots or staged releases, add the Sonatype OSS Staging repository to your Maven settings:

```
    <repository>
      <id>sonatype-oss-staging</id>
      <name>Sonatype OSS Staging Repository</name>
      <url>http://oss.sonatype.org/content/groups/staging</url>
    </repository>
```

## Downloads on Google Code ##

Maven is the primary distribution platform for jeeunit. If you do not use Maven, get a `src` or `bin` package from the [Downloads](http://code.google.com/p/jeeunit/downloads/list) page.

The `bin` packages contain the jeeunit core and and all integration JARs, but no example code. The `src` packages contain the complete source tree.

There will be no downloads for development snapshots.
## Configuration for Glassfish ##

### Dependencies ###
To run your tests with the `JeeunitRunner` on Embedded GlassFish, you need the following Maven dependencies:

```
    <dependency>
      <groupId>com.googlecode.jeeunit</groupId>
      <artifactId>jeeunit-glassfish</artifactId>
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
      <groupId>org.glassfish.extras</groupId>
      <artifactId>glassfish-embedded-all</artifactId>
      <version>3.1.1</version>
      <scope>test</scope>
    </dependency>
    <!-- workaround for GLASSFISH-17144 -->
    <dependency>
      <groupId>org.osgi</groupId>
      <artifactId>org.osgi.core</artifactId>
      <version>4.2.0</version>
      <scope>test</scope>
    </dependency>

```


### Configuration Properties ###

The default configuration for Embedded GlassFish will be read from a file `src/test/resources/domain.xml`. This is the usual GlassFish `domain.xml` configuration file.

The logging configuration is read from a file `src/test/resources/logging.properties`.

Unlike the other containers, the jeeunit GlassFish container does not currently require or consider a `jeeunit.properties` configuration file.
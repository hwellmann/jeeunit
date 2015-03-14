## Configuration for Weld SE ##

### Dependencies ###
To run your tests with the `JeeunitRunner` on Weld SE, which provides a subset of CDI capabilities on Java SE, you need the following Maven dependencies:

```
    <dependency>
      <groupId>com.googlecode.jeeunit</groupId>
      <artifactId>jeeunit-weld-se</artifactId>
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
      <groupId>org.jboss.weld.se</groupId>
      <artifactId>weld-se</artifactId>
      <version>1.1.5.Final</version>
      <scope>test</scope>
    </dependency>

```


### Configuration Properties ###

The jeeunit Weld SE container does not currently require or consider a `jeeunit.properties` configuration file.
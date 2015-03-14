# Working with the Examples #

## Getting the Sources ##

Download a source package jeeunit-x.y.z.zip from the Downloads page, containing a source snapshot for release x.y.z, corresponding to the Mercurial tag x.y.z.

Or simply clone the Mercurial repository from this site.


## Running the jeeunit Example Test Suite ##

To see jeeunit in action, simply go to the root directory and run

```
mvn install
```

The jeeunit examples use an Embedded Derby database and Maven artifacts for embedded servers, so there is no need to download, configure or start any servers. (Exception: For JBoss AS 7, you need a stand-alone server installation.)

If you get a HTTP port conflict, please edit the `jeeunit.properties` file or the server configuration file in the corresponding test project.
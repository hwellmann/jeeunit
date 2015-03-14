## Writing a test ##

Write a JUnit test using any of the usual JUnit 4.x annotations and any of the Java EE 6 dependency injection annotations like `@Inject, @EJB, @Resource, @PersistenceContext` and so on. Since jeeunit tests run in the container, dependency injection will happen automatically. No need to look up your EJB under test from JNDI.

To run your test run within a Java EE container, add a `@RunWith(JeeunitRunner.class)` annotation.

Example:

```
@RunWith(JeeunitRunner.class)
public class AuthorTest {
	
	@Inject
	private LibraryService service;
	
	@Before
	public void setUp()
	{
		service.fillLibrary();
	}
	
	@Test
	public void byAuthor()
	{
		List<Book> books = service.findBooksByAuthor("Mann");
		assertEquals(1, books.size());
		
		Book book = books.get(0);
		assertEquals("Buddenbrooks", book.getTitle());
	}

}
```


When running this test with JUnit, the `JeeunitRunner` starts an Embedded Container, builds a WAR file on the fly containing all classes and libraries visible on the classpath (excluding the embedded container itself) and deploys this WAR. The WAR contains a `TestRunnerServlet` which can be triggered by an HTTP GET request to run a given test method within the embedded container.

Instead of directly executing test methods, the `JeeunitRunner` delegates the execution via HTTP to the `TestRunnerServlet` within the embedded container. The serialized test results are sent back in the HTTP response.

All of this is completely transparent to the outer JUnit runner (e.g. the JUnit launcher of Eclipse, or the Surefire Maven plugin).

Thus, working with Eclipse, you can easily run a whole set of jeeunit Tests by simply selecting a Java package with test classes and then launching JUnit from the context menu.

For alternative containers like Tomcat, Spring or Weld SE, the test structure is the same, only the injection method and the inner workings of jeeunit are slightly different.

## Transactions and Automatic Rollback ##

The standard rules for declarative container-managed transactions also apply to jeeunit tests. By default, every public EJB method will run in a transaction. Thus, when calling EJB methods from your tests which modify persistent entities, these modifications will be persisted.

This is undesirable in most cases: no assumptions should be made on the execution order of tests, and all tests should run on an empty or pre-populated database with fixed contents.

Since release 0.8.0, jeeunit provides a `@Transactional` annotation for test methods to wrap the annotated method in a `UserTransaction` which will be rolled back after executing the test method. This approach has the desired effect for all container managed transactions with the default transaction type `REQUIRED`: The container does not need to open a transaction for the EJB method, because a transaction is already open, nor does it have to commit or rollback the transaction, because it does not own the transaction.

Annotating a test class with `@Transactional` is a short-hand equivalent to annotating each test method.

## Supported Containers ##

jeeunit supports the following containers:

### Java EE 6 ###

  * [Embedded GlassFish](GlassfishConfiguration.md) >= 3.1
  * [Embedded Resin](ResinConfiguration.md) >= 4.0.17
  * [JBoss AS](JBossConfiguration.md) >= 7.0.2

### Non-Java EE ###

  * [Tomcat 6 and 7 with Weld Servlet or Spring 3.x](TestingOnTomcat.md)
  * Weld SE